package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.VueFormItemRule;
import com.vgosoft.core.constant.enums.db.JavaTypeMapEnum;
import com.vgosoft.core.constant.enums.view.ValidatorRuleTypeEnum;
import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.config.HtmlElementDescriptor;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.config.HtmlValidatorElementConfiguration;
import org.mybatis.generator.custom.enums.ValidatorTypeEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * ElementPlus表单元数据描述注解
 *
 * @version 6.0
 */
@Getter
@Setter
public class VueFormItemRuleDesc extends AbstractAnnotation {

    public static final String ANNOTATION_NAME = "@" + VueFormItemRule.class.getSimpleName();

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
        List<HtmlValidatorElementConfiguration> validatorConfig = elementDescriptor != null ? elementDescriptor.getHtmlValidatorElementConfigurations().stream().filter(vConfig-> vConfig.getScope() == 0 || vConfig.getScope() == this.scope).collect(Collectors.toList()) : new ArrayList<>();
        String validatorType;
        List<HtmlValidatorElementConfiguration> typeConfigs = getValidatorElementConfiguration(validatorConfig, config -> config.getType() != null);
        if (!typeConfigs.isEmpty() && VStringUtil.stringHasValue(typeConfigs.get(0).getType())) {
            validatorType = typeConfigs.get(0).getType();
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
                    if (!VStringUtil.stringHasValue(this.getMessage())) {
                        setMessageProps("{0}不能为空");
                    }
                    return getAnnotationName();
                }
                return null;
            case TYPE:
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
                    this.setTriggerType(scope, validatorConfigs.get(0).getTrigger());
                    this.setValidator(validatorConfigs.get(0).getValidator());
                    return getAnnotationName();
                }
                return null;
            default:
                throw new IllegalArgumentException("不支持的验证规则类型: " + ruleTypeEnum);
        }
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
            this.setTrigger("change"); // vxeTable列表默认触发方式
        } else if (scope == 1) {
            this.setTrigger("blur"); // elForm表单默认触发方式
        }
    }
    private void setTriggerType(int scope, String trigger) {
        if (VStringUtil.stringHasValue(trigger)) {
            this.setTrigger(trigger);
        } else{
            setTriggerType(scope);
        }
    }

}
