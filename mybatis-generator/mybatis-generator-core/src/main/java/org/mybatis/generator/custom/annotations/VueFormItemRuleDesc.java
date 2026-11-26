package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.VueFormItemRule;
import com.vgosoft.core.constant.enums.db.JavaTypeMapEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementTagTypeEnum;
import com.vgosoft.core.constant.enums.view.ValidatorRuleTypeEnum;
import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.config.HtmlElementDescriptor;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.config.HtmlValidatorElementConfiguration;
import org.mybatis.generator.custom.enums.ValidatorTypeEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * ElementPlus表单元数据描述注解
 *
 * @author cen_c
 * @version 6.0
 */
@Getter
@Setter
public class VueFormItemRuleDesc extends AbstractAnnotation {

    public static final String ANNOTATION_NAME = "@" + VueFormItemRule.class.getSimpleName();

    /**
     * 命名验证器映射（小写配置名 -> 前端注册名）。
     * 对应前端 vgoForm commonValidators 已注册的字符串验证器；
     * 经 validator.type 配置时按 validator 规则名下发，不再生成 type 规则。
     * async-validator 内置类型（string/number/email/url 等）不在此列，仍走 type 规则。
     */
    private static final Map<String, String> NAMED_VALIDATORS = Map.ofEntries(
            Map.entry("mobile", "mobile"),
            Map.entry("digits", "digits"),
            Map.entry("positiveinteger", "positiveInteger"),
            Map.entry("money", "money"),
            Map.entry("equalto", "equalTo"),
            Map.entry("datenotbefore", "dateNotBefore"),
            Map.entry("datenotafter", "dateNotAfter"),
            Map.entry("idcard", "idCard"),
            Map.entry("tel", "tel"),
            Map.entry("postalcode", "postalCode"),
            Map.entry("ipv4", "ipv4"),
            Map.entry("mac", "mac"),
            Map.entry("chinese", "chinese"),
            Map.entry("username", "username"),
            Map.entry("plateno", "plateNo"),
            Map.entry("password", "password"));

    private String type;
    private Boolean required;
    private String message;
    private String trigger;
    private String min;
    private String max;
    private Integer len = 0;
    private String pattern;
    private Boolean whitespace;
    private String enumList;
    private String transform;
    private String validator;
    private String ruleType;
    private String compareField;
    private String compareLabel;
    private String compareValue;
    private Boolean allowEqual;
    private String precision;
    private Boolean allowNegative;
    private Boolean requireSpecial;

    @ToString.Exclude
    private IntrospectedColumn introspectedColumn;
    @ToString.Exclude
    private HtmlElementDescriptor elementDescriptor;
    @ToString.Exclude
    private HtmlGeneratorConfiguration htmlGeneratorConfiguration;
    @ToString.Exclude
    private ValidatorRuleTypeEnum ruleTypeEnum;
    // 作用域，1-elForm表单，2-vxeTable列表
    @ToString.Exclude
    private int scope;

    public VueFormItemRuleDesc(ValidatorRuleTypeEnum typeEnum, int scope, IntrospectedColumn introspectedColumn, HtmlElementDescriptor elementDescriptor, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        super();
        this.addImports(VueFormItemRule.class.getCanonicalName());
        this.elementDescriptor = elementDescriptor;
        this.introspectedColumn = introspectedColumn;
        this.htmlGeneratorConfiguration = htmlGeneratorConfiguration;
        this.ruleTypeEnum = typeEnum;
        this.ruleType = typeEnum.getType();
        this.scope = scope;
    }

    @Override
    public String toAnnotation() {
        // 生成注解内容
        List<HtmlValidatorElementConfiguration> validatorConfig = elementDescriptor != null ? elementDescriptor.getHtmlValidatorElementConfigurations().stream().filter(vConfig -> vConfig.getScope() == 0 || vConfig.getScope() == this.scope).collect(Collectors.toList()) : new ArrayList<>();
        String validatorType;
        List<HtmlValidatorElementConfiguration> typeConfigs = getValidatorElementConfiguration(validatorConfig, config -> config.getType() != null);
        if (!typeConfigs.isEmpty() && VStringUtil.stringHasValue(typeConfigs.get(0).getType())) {
            validatorType = typeConfigs.get(0).getType();
        } else if (isArrayValueElement()) {
            // checkbox/checkboxGroup 在前端表单模型中固定以数组存值，校验类型须派生为 array，不能沿用列 Java 类型
            validatorType = ValidatorTypeEnum.ARRAY.code();
        } else {
            validatorType = JavaTypeMapEnum.ofJava(introspectedColumn.getFullyQualifiedJavaType().getShortName()).validateType();
        }
        if (VStringUtil.stringHasValue(validatorType)) {
            validatorType = validatorType.toLowerCase();
        }
        this.setRuleType(ruleType);
        switch (ruleTypeEnum) {
            case TRANS_FORM:
                HtmlValidatorElementConfiguration configuration = validatorConfig.stream()
                        .filter(config -> VStringUtil.stringHasValue(config.getTransform())).findFirst().orElse(null);
                if (configuration == null) {
                    return null;
                } else {
                    this.setTransform(configuration.getTransform());
                    return getAnnotationName();
                }
            case REQUIRED:
                // 设置是否必填
                if (!introspectedColumn.isNullable()) {
                    this.setRequired(true);
                    setTriggerType(scope);
                } else {
                    List<HtmlValidatorElementConfiguration> requiredConfigs = getValidatorElementConfiguration(validatorConfig, config -> config.getRequired() != null && config.getRequired());
                    if (!requiredConfigs.isEmpty()) {
                        HtmlValidatorElementConfiguration validatorElementConfiguration = requiredConfigs.get(0);
                        this.setRequired(true);
                        if (VStringUtil.stringHasValue(validatorElementConfiguration.getMessage())) {
                            setMessageProps(requiredConfigs.get(0).getMessage());
                        }
                        setTriggerType(scope, requiredConfigs.get(0).getTrigger());
                    } else {
                        if (htmlGeneratorConfiguration != null && htmlGeneratorConfiguration.getElementRequired().contains(introspectedColumn.getActualColumnName())) {
                            this.setRequired(true);
                        }
                        setTriggerType(scope);
                    }
                }
                if (this.required != null && this.required) {
                    // required 规则未声明 type 时 async-validator 默认按 string 校验，
                    // 数字/布尔/数组绑定值会误报 "xxx is not a string"，按绑定类型补 type
                    String requiredRuleType = resolveRequiredRuleType(validatorType);
                    if (requiredRuleType != null) {
                        this.setType(requiredRuleType);
                    }
                    if (!VStringUtil.stringHasValue(this.getMessage())) {
                        setMessageProps("{0}不能为空");
                    }
                    return getAnnotationName();
                }
                return null;
            case TYPE:
                // 命名验证器：按 validator 规则名下发，参数随同一配置透传
                if (!typeConfigs.isEmpty() && NAMED_VALIDATORS.containsKey(validatorType)) {
                    HtmlValidatorElementConfiguration namedConfig = typeConfigs.get(0);
                    this.setValidator(NAMED_VALIDATORS.get(validatorType));
                    applyValidatorParams(namedConfig);
                    if (VStringUtil.stringHasValue(namedConfig.getMessage())) {
                        setMessageProps(namedConfig.getMessage());
                    }
                    setTriggerType(scope, namedConfig.getTrigger());
                    return getAnnotationName();
                }
                // 设置验证类型
                if (VStringUtil.stringHasValue(validatorType)) {
                    this.setType(validatorType);
                }
                if (VStringUtil.stringHasValue(this.getType())) {
                    if (!typeConfigs.isEmpty() && VStringUtil.stringHasValue(typeConfigs.get(0).getMessage())) {
                        setMessageProps(typeConfigs.get(0).getMessage());
                    } else {
                        setMessageProps(ValidatorTypeEnum.ofCode(this.type).message());
                    }
                    if (!typeConfigs.isEmpty()) {
                        setTriggerType(scope, typeConfigs.get(0).getTrigger());
                    } else {
                        setTriggerType(scope);
                    }
                    return getAnnotationName();
                }
                return null;
            case LENGTH:
                // length验证
                if ("string".equals(validatorType) || "array".equals(validatorType)) {
                    List<HtmlValidatorElementConfiguration> lengthConfigs = getValidatorElementConfiguration(validatorConfig, config -> config.getLen() != null && config.getLen() > 0);
                    if (!lengthConfigs.isEmpty()) {
                        if (VStringUtil.stringHasValue(lengthConfigs.get(0).getMessage())) {
                            setMessageProps(lengthConfigs.get(0).getMessage(), this.getLen().toString());
                        }
                        this.setLen(lengthConfigs.get(0).getLen());
                        setTriggerType(scope, lengthConfigs.get(0).getTrigger());
                    } else {
                        setTriggerType(scope);
                    }
                }
                if (this.getLen() > 0) {
                    if (!VStringUtil.stringHasValue(this.getMessage())) {
                        setMessageProps("{0}长度必须为{1}位", this.getLen().toString());
                    }
                    this.setType(validatorType);
                    return this.getAnnotationName();
                }
                return null;
            case MIN_MAX:
                // min和max,验证配置优先,会覆盖列配置
                if (introspectedColumn.getMinLength() > 0) {
                    this.setMin(String.valueOf(introspectedColumn.getMinLength()));
                }
                if ("string".equals(validatorType)) {
                    if (introspectedColumn.getLength() > 0) {
                        this.setMax(String.valueOf(introspectedColumn.getLength()));
                    }
                }
                // 使用配置的min和max进行覆盖
                List<HtmlValidatorElementConfiguration> minMaxConfigs = getValidatorElementConfiguration(validatorConfig, config -> VStringUtil.stringHasValue(config.getMin()) || VStringUtil.stringHasValue(config.getMax()));
                if (!minMaxConfigs.isEmpty()) {
                    HtmlValidatorElementConfiguration minMaxConfig = minMaxConfigs.get(0);
                    if (VStringUtil.stringHasValue(minMaxConfig.getMin())) {
                        this.setMin(minMaxConfig.getMin());
                    }
                    if (VStringUtil.stringHasValue(minMaxConfig.getMax())) {
                        this.setMax(minMaxConfig.getMax());
                    }
                    if (VStringUtil.stringHasValue(minMaxConfig.getMessage())) {
                        setMessageProps(minMaxConfig.getMessage(), this.getMin(), this.getMax());
                    }
                    setTriggerType(scope, minMaxConfigs.get(0).getTrigger());
                } else {
                    setTriggerType(scope);
                }
                if (VStringUtil.stringHasValue(this.getMin()) || VStringUtil.stringHasValue(this.getMax())) {
                    if (!VStringUtil.stringHasValue(this.getMessage())) {
                        this.setMessage(getMinMaxMessage(validatorType, this.getMin(), this.getMax()));
                    }
                    this.setType(validatorType);
                    return getAnnotationName();
                }
                return null;
            case PATTERN:
                List<HtmlValidatorElementConfiguration> patternConfigs = getValidatorElementConfiguration(validatorConfig, config -> VStringUtil.stringHasValue(config.getPattern()));
                if (!patternConfigs.isEmpty()) {
                    this.setPattern(patternConfigs.get(0).getPattern());
                    if (VStringUtil.stringHasValue(patternConfigs.get(0).getMessage())) {
                        setMessageProps(patternConfigs.get(0).getMessage());
                    } else {
                        setMessageProps("{0}格式不正确");
                    }
                    setTriggerType(scope, patternConfigs.get(0).getTrigger());
                    return getAnnotationName();
                }
                return null;
            case ENUM:
                List<HtmlValidatorElementConfiguration> enumListConfigs = getValidatorElementConfiguration(validatorConfig, config -> config.getEnumList() != null);
                if (!enumListConfigs.isEmpty()) {
                    this.setEnumList(enumListConfigs.get(0).getPattern());
                    if (VStringUtil.stringHasValue(enumListConfigs.get(0).getMessage())) {
                        setMessageProps(enumListConfigs.get(0).getMessage(), enumListConfigs.get(0).getEnumList());
                    } else {
                        setMessageProps("{0}值必须是{1}中的一个", enumListConfigs.get(0).getEnumList());
                    }
                    setTriggerType(scope, enumListConfigs.get(0).getTrigger());
                    return getAnnotationName();
                }
                return null;
            case VALIDATOR:
                List<HtmlValidatorElementConfiguration> validatorConfigs = getValidatorElementConfiguration(validatorConfig, config -> VStringUtil.stringHasValue(config.getValidator()));
                if (!validatorConfigs.isEmpty()) {
                    HtmlValidatorElementConfiguration validatorElementConfiguration = validatorConfigs.get(0);
                    this.setTriggerType(scope, validatorElementConfiguration.getTrigger());
                    this.setValidator(validatorElementConfiguration.getValidator());
                    applyValidatorParams(validatorElementConfiguration);
                    if (VStringUtil.stringHasValue(validatorElementConfiguration.getMessage())) {
                        setMessageProps(validatorElementConfiguration.getMessage());
                    }
                    return getAnnotationName();
                }
                return null;
            default:
                throw new IllegalArgumentException("不支持的验证规则类型: " + ruleTypeEnum);
        }
    }

    /**
     * 将验证器配置中的命名验证器参数透传到本注解描述。
     * 对 type/validator 指定命名验证器的同一配置生效：min/max、compare*、precision、allow*、requireSpecial。
     *
     * @param config 提供命名验证器或其参数的 validator 配置
     */
    private void applyValidatorParams(HtmlValidatorElementConfiguration config) {
        if (VStringUtil.stringHasValue(config.getCompareField())) {
            this.setCompareField(resolveCompareField(config.getCompareField()));
        }
        String label = config.getCompareLabel();
        if (!VStringUtil.stringHasValue(label) && VStringUtil.stringHasValue(this.getCompareField())) {
            // 未配置对照标签时取对照列注释，仍无列可解析则回退 compareField 原值
            label = resolveCompareLabel(config.getCompareField());
        }
        if (VStringUtil.stringHasValue(label)) {
            this.setCompareLabel(label);
        }
        if (VStringUtil.stringHasValue(config.getCompareValue())) {
            this.setCompareValue(config.getCompareValue());
        }
        if (config.getAllowEqual() != null) {
            this.setAllowEqual(config.getAllowEqual());
        }
        if (VStringUtil.stringHasValue(config.getPrecision())) {
            this.setPrecision(config.getPrecision());
        }
        if (config.getAllowNegative() != null) {
            this.setAllowNegative(config.getAllowNegative());
        }
        if (config.getRequireSpecial() != null) {
            this.setRequireSpecial(config.getRequireSpecial());
        }
        if (VStringUtil.stringHasValue(config.getMin())) {
            this.setMin(config.getMin());
        }
        if (VStringUtil.stringHasValue(config.getMax())) {
            this.setMax(config.getMax());
        }
    }

    /**
     * 解析对照字段：配置值为数据库列名时转为 Java 属性名，否则按表单属性名原样返回。
     */
    private String resolveCompareField(String configuredField) {
        AbstractIntrospectedTable table = introspectedColumn.getIntrospectedTable();
        if (table == null) {
            return configuredField;
        }
        return table.getColumn(configuredField)
                .map(IntrospectedColumn::getJavaProperty)
                .orElse(configuredField);
    }

    /**
     * 解析对照字段标签：优先取对照列注释，无法解析时回退原配置值。
     */
    private String resolveCompareLabel(String configuredField) {
        AbstractIntrospectedTable table = introspectedColumn.getIntrospectedTable();
        if (table == null) {
            return configuredField;
        }
        return table.getColumn(configuredField)
                .map(column -> column.getRemarks(true))
                .filter(VStringUtil::stringHasValue)
                .orElse(configuredField);
    }

    /**
     * 推断 required 规则应携带的校验 type。
     * 仅对绑定值恒为非字符串标量/数组的类型补 type：number/integer/float 归一为
     * number（required 只承担非空与数值类型检查，精度语义仍由独立 type 规则承担，
     * 避免"不能为空"文案误作格式提示），boolean、array 按原类型。
     * date、url/email 等格式类型与命名验证器（money 等）的绑定值为字符串或不可判定，
     * 返回 null 不补（由运行时按绑定值类型兜底推断）。
     *
     * @param validatorType 已推导的校验类型（配置 type 优先，其次组件派生、列 Java 类型映射）
     */
    private String resolveRequiredRuleType(String validatorType) {
        if (!VStringUtil.stringHasValue(validatorType)) {
            return null;
        }
        return switch (validatorType) {
            case "integer", "float", "number" -> "number";
            case "boolean" -> "boolean";
            case "array" -> "array";
            default -> null;
        };
    }

    /**
     * 判断表单元素是否为数组取值控件。
     * checkbox/checkboxGroup 在前端表单模型中固定以数组持有（perOnload 拆分数组、preSubmit 合并逗号串）。
     */
    private boolean isArrayValueElement() {
        if (elementDescriptor == null) {
            return false;
        }
        HtmlElementTagTypeEnum tagType = HtmlElementTagTypeEnum.ofCodeName(elementDescriptor.getTagType());
        return tagType == HtmlElementTagTypeEnum.CHECKBOX || tagType == HtmlElementTagTypeEnum.CHECKBOX_GROUP;
    }

    private String getMinMaxMessage(String validatorType, String min, String max) {
        String remarks = introspectedColumn.getRemarks(true);
        if ("string".equals(validatorType) || "array".equals(validatorType)) {
            if (VStringUtil.stringHasValue(min) && VStringUtil.stringHasValue(max)) {
                return VStringUtil.format("{0}的长度必须在{1}到{2}之间", remarks, min, max);
            } else if (VStringUtil.stringHasValue(min)) {
                return VStringUtil.format("{0}的长度必须大于{1}", remarks, min);
            } else if (VStringUtil.stringHasValue(max)) {
                return VStringUtil.format("{0}的长度必须小于{1}", remarks, max);
            }
            return "长度验证失败";
        } else {
            if (VStringUtil.stringHasValue(min) && VStringUtil.stringHasValue(max)) {
                return VStringUtil.format("{0}必须在{1}到{2}之间", remarks, min, max);
            } else if (VStringUtil.stringHasValue(min)) {
                return VStringUtil.format("{0}必须大于{1}", remarks, min);
            } else if (VStringUtil.stringHasValue(max)) {
                return VStringUtil.format("{0}必须小于{1}", remarks, max);
            }
            return "数据验证失败";
        }
    }

    private void setMessageProps(String message, String... params) {
        this.setMessage(VStringUtil.format(message, introspectedColumn.getRemarks(true), params));
    }

    private List<HtmlValidatorElementConfiguration> getValidatorElementConfiguration(List<HtmlValidatorElementConfiguration> validatorConfig, Predicate<HtmlValidatorElementConfiguration> predicate) {
        if (validatorConfig.isEmpty()) {
            return new ArrayList<>();
        }
        return validatorConfig.stream().filter(predicate).collect(Collectors.toList());
    }

    private String getAnnotationName() {
        // 生成注解
        if (VStringUtil.stringHasValue(this.getTransform())) {
            items.add(VStringUtil.format("transform = \"{0}\"", this.getTransform()));
        }
        if (this.getRequired() != null && this.getRequired()) {
            items.add("required = true");
        }
        if (VStringUtil.stringHasValue(this.getType())) {
            items.add(VStringUtil.format("type = \"{0}\"", this.getType()));
        }
        if (VStringUtil.stringHasValue(this.getMin())) {
            items.add(VStringUtil.format("min = \"{0}\"", this.getMin()));
        }
        if (VStringUtil.stringHasValue(this.getMax())) {
            items.add(VStringUtil.format("max = \"{0}\"", this.getMax()));
        }
        if (this.getLen() != null && this.getLen() > 0) {
            items.add(VStringUtil.format("len = {0}", String.valueOf(this.getLen())));
        }
        if (VStringUtil.stringHasValue(this.getPattern())) {
            items.add(VStringUtil.format("pattern = \"{0}\"", this.getPattern()));
        }
        if (this.getWhitespace() != null && this.getWhitespace()) {
            items.add("whitespace = true");
        }
        if (VStringUtil.stringHasValue(this.getEnumList())) {
            items.add(VStringUtil.format("enumList = \"{0}\"", this.getEnumList()));
        }

        if (VStringUtil.stringHasValue(this.getValidator())) {
            items.add(VStringUtil.format("validator = \"{0}\"", this.getValidator()));
        }
        if (VStringUtil.stringHasValue(this.getCompareField())) {
            items.add(VStringUtil.format("compareField = \"{0}\"", this.getCompareField()));
        }
        if (VStringUtil.stringHasValue(this.getCompareLabel())) {
            items.add(VStringUtil.format("compareLabel = \"{0}\"", this.getCompareLabel()));
        }
        if (VStringUtil.stringHasValue(this.getCompareValue())) {
            items.add(VStringUtil.format("compareValue = \"{0}\"", this.getCompareValue()));
        }
        if (this.getAllowEqual() != null && !this.getAllowEqual()) {
            items.add("allowEqual = false");
        }
        if (VStringUtil.stringHasValue(this.getPrecision())) {
            items.add(VStringUtil.format("precision = \"{0}\"", this.getPrecision()));
        }
        if (this.getAllowNegative() != null && this.getAllowNegative()) {
            items.add("allowNegative = true");
        }
        if (this.getRequireSpecial() != null && this.getRequireSpecial()) {
            items.add("requireSpecial = true");
        }
        if (VStringUtil.stringHasValue(this.getMessage())) {
            items.add(VStringUtil.format("message = \"{0}\"", this.getMessage()));
        }
        if (VStringUtil.stringHasValue(this.getTrigger())) {
            items.add(VStringUtil.format("trigger = \"{0}\"", this.getTrigger()));
        }
        return ANNOTATION_NAME + "(" + String.join(", ", items.toArray(new String[0])) + ")";
    }

    private void setTriggerType(int scope) {
        if (scope == 2) {
            // vxeTable列表默认触发方式
            this.setTrigger("change");
        } else if (scope == 1) {
            // elForm表单默认触发方式
            this.setTrigger("blur");
        }
    }

    private void setTriggerType(int scope, String trigger) {
        if (VStringUtil.stringHasValue(trigger)) {
            this.setTrigger(trigger);
        } else {
            setTriggerType(scope);
        }
    }
}
