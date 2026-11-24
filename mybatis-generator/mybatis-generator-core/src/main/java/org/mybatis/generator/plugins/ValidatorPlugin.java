package org.mybatis.generator.plugins;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.ColumnOverride;
import org.mybatis.generator.config.HtmlElementDescriptor;
import org.mybatis.generator.config.HtmlValidatorElementConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.VoGeneratorConfiguration;
import org.mybatis.generator.config.VoUpdateGeneratorConfiguration;
import org.mybatis.generator.custom.annotations.AbstractAnnotation;
import org.mybatis.generator.custom.annotations.validate.DecimalMax;
import org.mybatis.generator.custom.annotations.validate.DecimalMin;
import org.mybatis.generator.custom.annotations.validate.NotEmpty;
import org.mybatis.generator.custom.annotations.validate.NotNull;
import org.mybatis.generator.custom.annotations.validate.ValidateDigits;
import org.mybatis.generator.custom.annotations.validate.ValidateEmail;
import org.mybatis.generator.custom.annotations.validate.ValidatePattern;
import org.mybatis.generator.custom.annotations.validate.ValidatePositive;
import org.mybatis.generator.custom.annotations.validate.ValidateSize;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 添加日期属性的JsonFormat
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-04-19 14:51
 * @version 3.0
 */
public class ValidatorPlugin extends PluginAdapter {

    public static final String VALIDATE_INSERT = "com.vgosoft.core.valid.ValidateInsert";
    public static final String VALIDATE_UPDATE = "com.vgosoft.core.valid.ValidateUpdate";

    public static final String[] INSERT_VALIDATE = new String[]{"ValidateInsert.class"};
    public static final String[] UPDATE_VALIDATE = new String[]{"ValidateUpdate.class"};
    public static final String[] INSERT_AND_UPDATE_VALIDATE = new String[]{"ValidateInsert.class", "ValidateUpdate.class"};

    private static final String IPV4_SEGMENT = "(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)";

    private static final String URL_REGEXP = "^[a-zA-Z][a-zA-Z0-9+.-]*://\\S+$";

    private static final String PLATE_PREFIX = "[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z]";

    private static final String PLATE_NO_REGEXP = "^(" + PLATE_PREFIX + "[A-HJ-NP-Z0-9]{4}[A-HJ-NP-Z0-9挂学警港澳]"
            + "|" + PLATE_PREFIX + "([0-9]{5}[DF]|[DF][A-HJ-NP-Z0-9][0-9]{4}))$";

    /**
     * 命名验证器名（小写） -> 后端等价 @Pattern 正则（原始正则，输出时转义为 Java 字符串字面量）。
     * 与前端 vgoForm commonValidators 注册的验证器保持一致；
     * 参数化验证器（positiveInteger/money/password）与 url 由构建方法处理，
     * 跨字段验证器（equalTo/dateNotBefore/dateNotAfter）无 jakarta 对应能力，不生成。
     */
    private static final Map<String, String> NAMED_VALIDATOR_REGEXPS = Map.ofEntries(
            Map.entry("mobile", "^1[3-9]\\d{9}$"),
            Map.entry("digits", "^\\d+$"),
            Map.entry("idcard", "^(\\d{15}|[1-9]\\d{5}(19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx])$"),
            Map.entry("tel", "^(0\\d{2,3}-?)?\\d{7,8}(-\\d{1,5})?$"),
            Map.entry("postalcode", "^[1-9]\\d{5}$"),
            Map.entry("ipv4", "^" + IPV4_SEGMENT + "(\\." + IPV4_SEGMENT + "){3}$"),
            Map.entry("mac", "^([0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2}$"),
            Map.entry("chinese", "^[\\u4e00-\\u9fa5·]+$"),
            Map.entry("username", "^[a-zA-Z][a-zA-Z0-9_]*$"),
            Map.entry("plateno", PLATE_NO_REGEXP));

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable, ModelClassType modelClassType) {
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        Set<String> ignoreColumns = tc.getValidateIgnoreColumns();
        if (introspectedColumn == null) {
            return true;
        }
        if (!introspectedTable.getRules().isGenerateVoModel()) {
            if (introspectedColumn.isPrimaryKey()) {
                addNotNullValidate(field, topLevelClass, introspectedColumn, UPDATE_VALIDATE, introspectedTable, ignoreColumns);
            } else {
                addNotNullValidate(field, topLevelClass, introspectedColumn, INSERT_AND_UPDATE_VALIDATE, introspectedTable, ignoreColumns);
            }
            addLengthValidate(field, topLevelClass, introspectedColumn, INSERT_AND_UPDATE_VALIDATE, introspectedTable, ignoreColumns);
            addConfiguredValidate(field, topLevelClass, introspectedColumn, INSERT_AND_UPDATE_VALIDATE, introspectedTable, ignoreColumns);
        }
        return true;
    }

    @Override
    public boolean voAbstractFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable) {
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        Set<String> ignoreColumns = tc.getValidateIgnoreColumns();
        ignoreColumns.addAll(tc.getVoGeneratorConfiguration().getValidateIgnoreColumns());
        addNotNullValidate(field, topLevelClass, introspectedColumn, introspectedColumn.isPrimaryKey() ? UPDATE_VALIDATE : INSERT_AND_UPDATE_VALIDATE, introspectedTable, ignoreColumns);
        addLengthValidate(field, topLevelClass, introspectedColumn, INSERT_AND_UPDATE_VALIDATE, introspectedTable, ignoreColumns);
        addConfiguredValidate(field, topLevelClass, introspectedColumn, INSERT_AND_UPDATE_VALIDATE, introspectedTable, ignoreColumns);
        return true;
    }

    @Override
    public boolean voModelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable) {
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        Set<String> ignoreColumns = tc.getValidateIgnoreColumns();
        VoGeneratorConfiguration voGeneratorConfiguration = tc.getVoGeneratorConfiguration();
        ignoreColumns.addAll(voGeneratorConfiguration.getValidateIgnoreColumns());
        return addVoModelValidate(field, topLevelClass, introspectedColumn, introspectedTable, ignoreColumns);
    }

    @Override
    public boolean voModelGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable) {
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        Set<String> ignoreColumns = tc.getValidateIgnoreColumns();
        VoGeneratorConfiguration voGeneratorConfiguration = tc.getVoGeneratorConfiguration();
        ignoreColumns.addAll(voGeneratorConfiguration.getValidateIgnoreColumns());
        return addVoModelValidate(method, topLevelClass, introspectedColumn, introspectedTable, ignoreColumns);
    }

    /**
     * 添加非空校验
     * @param field 生成的属性
     * @param topLevelClass 生成的类
     * @param introspectedColumn 表字段
     * @param introspectedTable 表配置
     * @return 是否生成成功
     */
    @Override
    public boolean voCreateFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable) {
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        Set<String> ignoreColumns = tc.getValidateIgnoreColumns();
        VoGeneratorConfiguration voGeneratorConfiguration = tc.getVoGeneratorConfiguration();
        ignoreColumns.addAll(voGeneratorConfiguration.getValidateIgnoreColumns());
        Set<String> validateIgnoreColumns = tc.getVoGeneratorConfiguration().getVoCreateConfiguration().getValidateIgnoreColumns();
        ignoreColumns.addAll(validateIgnoreColumns);
        return addCreateValidate(field, topLevelClass, introspectedColumn, introspectedTable, ignoreColumns);
    }

    @Override
    public boolean voCreateGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable) {
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        Set<String> validateIgnoreColumns = tc.getVoGeneratorConfiguration().getVoCreateConfiguration().getValidateIgnoreColumns();
        return addCreateValidate(method, topLevelClass, introspectedColumn, introspectedTable, validateIgnoreColumns);
    }

    @Override
    public boolean voUpdateGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable) {
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        Set<String> validateIgnoreColumns = tc.getVoGeneratorConfiguration().getVoUpdateConfiguration().getValidateIgnoreColumns();
        return addUpdateValidate(method, topLevelClass, introspectedColumn, introspectedTable, validateIgnoreColumns);
    }

    @Override
    public boolean voUpdateFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable) {
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        Set<String> validateIgnoreColumns = tc.getVoGeneratorConfiguration().getVoUpdateConfiguration().getValidateIgnoreColumns();
        return addUpdateValidate(field, topLevelClass, introspectedColumn, introspectedTable, validateIgnoreColumns);
    }

    private boolean addVoModelValidate(JavaElement javaElement,
                                       TopLevelClass topLevelClass,
                                       IntrospectedColumn introspectedColumn,
                                       AbstractIntrospectedTable introspectedTable,
                                       Set<String> ignoreColumns) {
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        ignoreColumns.addAll(tc.getVoGeneratorConfiguration().getVoModelConfiguration().getValidateIgnoreColumns());
        if (introspectedColumn == null || ignoreColumns.contains(introspectedColumn.getActualColumnName())) {
            return true;
        }
        boolean generateCreateVo = introspectedTable.getRules().isGenerateCreateVo();
        boolean generateUpdateVo = introspectedTable.getRules().isGenerateUpdateVo();
        if (!(generateCreateVo || generateUpdateVo)) {
            addNotNullValidate(javaElement, topLevelClass, introspectedColumn, introspectedColumn.isPrimaryKey() ? UPDATE_VALIDATE : INSERT_AND_UPDATE_VALIDATE, introspectedTable, ignoreColumns);
        } else if (!generateCreateVo) {
            addCreateValidate(javaElement, topLevelClass, introspectedColumn, introspectedTable, ignoreColumns);
        } else if (!generateUpdateVo) {
            addUpdateValidate(javaElement, topLevelClass, introspectedColumn, introspectedTable, ignoreColumns);
        } else {
            return true;
        }
        addLengthValidate(javaElement, topLevelClass, introspectedColumn, INSERT_AND_UPDATE_VALIDATE, introspectedTable, ignoreColumns);
        addConfiguredValidate(javaElement, topLevelClass, introspectedColumn, INSERT_AND_UPDATE_VALIDATE, introspectedTable, ignoreColumns);
        return true;
    }

    private boolean addCreateValidate(JavaElement javaElement,
                                      TopLevelClass topLevelClass,
                                      IntrospectedColumn introspectedColumn,
                                      AbstractIntrospectedTable introspectedTable,
                                      Set<String> ignoreColumns) {
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        if (introspectedTable.getRules().isGenerateCreateVo()) {
            ignoreColumns.addAll(tc.getVoGeneratorConfiguration().getVoCreateConfiguration().getValidateIgnoreColumns());
        }
        if (introspectedColumn == null || ignoreColumns.contains(introspectedColumn.getActualColumnName()) || introspectedColumn.isPrimaryKey()) {
            return true;
        }
        addNotNullValidate(javaElement, topLevelClass, introspectedColumn, INSERT_VALIDATE, introspectedTable, ignoreColumns);
        addLengthValidate(javaElement, topLevelClass, introspectedColumn, INSERT_VALIDATE, introspectedTable, ignoreColumns);
        addConfiguredValidate(javaElement, topLevelClass, introspectedColumn, INSERT_VALIDATE, introspectedTable, ignoreColumns);
        return true;
    }

    private boolean addUpdateValidate(JavaElement javaElement,
                                      TopLevelClass topLevelClass,
                                      IntrospectedColumn introspectedColumn,
                                      AbstractIntrospectedTable introspectedTable,
                                      Set<String> ignoreColumns) {
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        if (introspectedTable.getRules().isGenerateUpdateVo()) {
            VoUpdateGeneratorConfiguration voUpdateConfiguration = tc.getVoGeneratorConfiguration().getVoUpdateConfiguration();
            ignoreColumns.addAll(voUpdateConfiguration.getValidateIgnoreColumns());
        }
        if (introspectedColumn == null || ignoreColumns.contains(introspectedColumn.getActualColumnName())) {
            return true;
        }
        addNotNullValidate(javaElement, topLevelClass, introspectedColumn, UPDATE_VALIDATE, introspectedTable, ignoreColumns);
        addLengthValidate(javaElement, topLevelClass, introspectedColumn, UPDATE_VALIDATE, introspectedTable, ignoreColumns);
        addConfiguredValidate(javaElement, topLevelClass, introspectedColumn, UPDATE_VALIDATE, introspectedTable, ignoreColumns);
        return true;
    }

    /**
     * 添加长度校验
     *
     * @param element            生成的方法或者属性
     * @param topLevelClass      生成的类
     * @param introspectedColumn 表字段
     * @param validateGroups     校验组
     */
    private void addLengthValidate(JavaElement element,
                                   TopLevelClass topLevelClass,
                                   IntrospectedColumn introspectedColumn,
                                   String[] validateGroups,
                                   AbstractIntrospectedTable introspectedTable, Set<String> validateIgnoreColumns) {
        //校验字符串长度
        if (introspectedColumn.getLength() == 0
                || validateIgnoreColumns.contains(introspectedColumn.getActualColumnName())
                || introspectedColumn.isBlobColumn()
                || introspectedColumn.getLength() > 5000) {
            return;
        }
        if (introspectedColumn.isStringColumn()) {
            ValidateSize validateSize = new ValidateSize();
            // validator 配置的 len/min/max 优先于列长；仅配置单边时另一边回落列长
            List<HtmlValidatorElementConfiguration> configs = getColumnValidatorConfigs(introspectedTable, introspectedColumn);
            HtmlValidatorElementConfiguration lenConfig = configs.stream()
                    .filter(c -> c.getLen() != null && c.getLen() > 0).findFirst().orElse(null);
            HtmlValidatorElementConfiguration minMaxConfig = configs.stream()
                    .filter(c -> VStringUtil.stringHasValue(c.getMin()) || VStringUtil.stringHasValue(c.getMax()))
                    .findFirst().orElse(null);
            if (lenConfig != null) {
                validateSize.setMin(String.valueOf(lenConfig.getLen()));
                validateSize.setMax(String.valueOf(lenConfig.getLen()));
                if (VStringUtil.stringHasValue(lenConfig.getMessage())) {
                    validateSize.setMessage(lenConfig.getMessage());
                } else {
                    validateSize.setMessage(VStringUtil.format("{0}的长度必须为{1}位"
                            , introspectedColumn.getRemarks(true), lenConfig.getLen()));
                }
            } else {
                String min = minMaxConfig != null && VStringUtil.stringHasValue(minMaxConfig.getMin())
                        ? minMaxConfig.getMin()
                        : (introspectedColumn.getMinLength() > 0 ? String.valueOf(introspectedColumn.getMinLength()) : null);
                String max = minMaxConfig != null && VStringUtil.stringHasValue(minMaxConfig.getMax())
                        ? minMaxConfig.getMax()
                        : (introspectedColumn.getLength() > 0 ? String.valueOf(introspectedColumn.getLength()) : null);
                validateSize.setMin(min);
                validateSize.setMax(max);
                if (minMaxConfig != null && VStringUtil.stringHasValue(minMaxConfig.getMessage())) {
                    validateSize.setMessage(minMaxConfig.getMessage());
                } else if (VStringUtil.stringHasValue(min) && VStringUtil.stringHasValue(max)) {
                    validateSize.setMessage(VStringUtil.format("{0}的长度应该在{1}-{2}之间"
                            , introspectedColumn.getRemarks(true), min, max));
                } else if (VStringUtil.stringHasValue(min)) {
                    validateSize.setMessage(VStringUtil.format("{0}的长度不能少于{1}"
                            , introspectedColumn.getRemarks(true), min));
                } else {
                    validateSize.setMessage(VStringUtil.format("{0}的长度不能超过{1}"
                            , introspectedColumn.getRemarks(true), max));
                }
            }
            if (VStringUtil.stringHasValue(validateSize.getMin()) || VStringUtil.stringHasValue(validateSize.getMax())) {
                validateSize.setGroups(validateGroups);
                element.addAnnotation(validateSize.toAnnotation());
                topLevelClass.addImportedTypes(validateSize.getImportedTypes());
                topLevelClass.addImportedTypes(getImportedTypes(validateGroups));
            }
        } else if (introspectedColumn.isNumericColumn()) {
            ValidateDigits validateDigits = new ValidateDigits();
            validateDigits.setInteger(String.valueOf(introspectedColumn.getLength()));
            validateDigits.setFraction(String.valueOf(introspectedColumn.getScale()));
            if (introspectedColumn.getScale() > 0) {
                validateDigits.setMessage(VStringUtil.format("{0}的整数位数不能超过{1}，小数位数不能超过{2}"
                        , introspectedColumn.getRemarks(true)
                        , introspectedColumn.getLength()
                        , introspectedColumn.getScale()));
            } else {
                validateDigits.setMessage(VStringUtil.format("{0}的整数位数不能超过{1}"
                        , introspectedColumn.getRemarks(true)
                        , introspectedColumn.getLength()));
            }
            validateDigits.setGroups(validateGroups);
            element.addAnnotation(validateDigits.toAnnotation());
            topLevelClass.addImportedTypes(validateDigits.getImportedTypes());
            topLevelClass.addImportedTypes(getImportedTypes(validateGroups));
        }
    }

    /**
     * 添加非空校验
     *
     * @param element            生成的方法或者属性
     * @param topLevelClass      生成的类
     * @param introspectedColumn 表字段
     * @param validateGroups     校验组
     */
    private void addNotNullValidate(JavaElement element,
                                    TopLevelClass topLevelClass,
                                    IntrospectedColumn introspectedColumn,
                                    String[] validateGroups,
                                    AbstractIntrospectedTable introspectedTable, Set<String> validateIgnoreColumns) {
        ColumnOverride override = introspectedTable.getTableConfiguration().getColumnOverrides().stream().filter(columnOverride -> columnOverride.getColumnName().equals(introspectedColumn.getActualColumnName())).findFirst().orElse(null);
        boolean isOverrideRequired = override != null && override.isRequired();
        if (validateIgnoreColumns.contains(introspectedColumn.getActualColumnName()) || (introspectedColumn.isNullable() && !introspectedColumn.isForeignKey() && !isOverrideRequired)) {
            return;
        }
        String message = introspectedColumn.getRemarks(true) + "不能为空";
        if ("String".equalsIgnoreCase(introspectedColumn.getFullyQualifiedJavaType().getShortName())) {
            NotEmpty notEmpty = NotEmpty.create(message, validateGroups);
            element.addAnnotation(notEmpty.toAnnotation());
            topLevelClass.addImportedTypes(notEmpty.getImportedTypes());
        } else {
            NotNull notNull = NotNull.create(message, validateGroups);
            element.addAnnotation(notNull.toAnnotation());
            topLevelClass.addImportedTypes(notNull.getImportedTypes());
        }
        topLevelClass.addImportedTypes(getImportedTypes(validateGroups));
    }

    /**
     * 按 validator 元素配置补充后端 jakarta.validation 校验注解。
     * 覆盖 scope 0/1（表单级）配置；scope=2 面向内嵌 vxe 表格渲染规则，不落到主表字段。
     * 命名验证器映射到 @Pattern/@Email/@Positive/@DecimalMin/@DecimalMax；
     * 跨字段验证器（equalTo/dateNotBefore/dateNotAfter）无 jakarta 对应能力，仅前端生效。
     */
    private void addConfiguredValidate(JavaElement element,
                                       TopLevelClass topLevelClass,
                                       IntrospectedColumn introspectedColumn,
                                       String[] validateGroups,
                                       AbstractIntrospectedTable introspectedTable,
                                       Set<String> validateIgnoreColumns) {
        if (introspectedColumn == null || validateIgnoreColumns.contains(introspectedColumn.getActualColumnName())) {
            return;
        }
        List<HtmlValidatorElementConfiguration> configs = getColumnValidatorConfigs(introspectedTable, introspectedColumn);
        if (configs.isEmpty()) {
            return;
        }
        String remarks = introspectedColumn.getRemarks(true);
        List<AbstractAnnotation> annotations = new ArrayList<>();
        // 显式 required=true：仅兜底逻辑未覆盖（可空、非外键、无列覆盖必填）时补非空约束
        HtmlValidatorElementConfiguration requiredConfig = configs.stream()
                .filter(c -> Boolean.TRUE.equals(c.getRequired())).findFirst().orElse(null);
        if (requiredConfig != null) {
            ColumnOverride override = introspectedTable.getTableConfiguration().getColumnOverrides().stream()
                    .filter(o -> o.getColumnName().equals(introspectedColumn.getActualColumnName())).findFirst().orElse(null);
            boolean fallbackMissed = introspectedColumn.isNullable() && !introspectedColumn.isForeignKey()
                    && !(override != null && override.isRequired());
            if (fallbackMissed) {
                String message = VStringUtil.stringHasValue(requiredConfig.getMessage())
                        ? requiredConfig.getMessage() : remarks + "不能为空";
                annotations.add(introspectedColumn.isStringColumn()
                        ? NotEmpty.create(message, validateGroups)
                        : NotNull.create(message, validateGroups));
            }
        }
        // pattern 配置：正则透传（仅字符串字段，@Pattern 只接受 CharSequence）
        HtmlValidatorElementConfiguration patternConfig = configs.stream()
                .filter(c -> VStringUtil.stringHasValue(c.getPattern())).findFirst().orElse(null);
        if (patternConfig != null && introspectedColumn.isStringColumn()) {
            annotations.add(ValidatePattern.create(escapeJavaLiteral(patternConfig.getPattern()),
                    VStringUtil.stringHasValue(patternConfig.getMessage()) ? patternConfig.getMessage() : remarks + "格式不正确",
                    validateGroups));
        }
        // 数值列的 min/max 配置 -> @DecimalMin/@DecimalMax（字符串列的长度边界已由 @Size 处理）
        HtmlValidatorElementConfiguration minMaxConfig = configs.stream()
                .filter(c -> VStringUtil.stringHasValue(c.getMin()) || VStringUtil.stringHasValue(c.getMax()))
                .findFirst().orElse(null);
        if (minMaxConfig != null && introspectedColumn.isNumericColumn()) {
            if (VStringUtil.stringHasValue(minMaxConfig.getMin())) {
                annotations.add(DecimalMin.create(minMaxConfig.getMin(),
                        VStringUtil.stringHasValue(minMaxConfig.getMessage()) ? minMaxConfig.getMessage()
                                : VStringUtil.format("{0}不能小于{1}", remarks, minMaxConfig.getMin()),
                        validateGroups));
            }
            if (VStringUtil.stringHasValue(minMaxConfig.getMax())) {
                annotations.add(DecimalMax.create(minMaxConfig.getMax(),
                        VStringUtil.stringHasValue(minMaxConfig.getMessage()) ? minMaxConfig.getMessage()
                                : VStringUtil.format("{0}不能大于{1}", remarks, minMaxConfig.getMax()),
                        validateGroups));
            }
        }
        // 命名验证器：type 直接命中命名名，或 validator 属性填写命名名
        for (HtmlValidatorElementConfiguration config : configs) {
            collectNamedValidatorAnnotation(annotations, config.getType(), config, introspectedColumn, remarks, validateGroups);
            collectNamedValidatorAnnotation(annotations, config.getValidator(), config, introspectedColumn, remarks, validateGroups);
        }
        Set<String> emitted = new LinkedHashSet<>();
        for (AbstractAnnotation annotation : annotations) {
            String text = annotation.toAnnotation();
            if (text != null && emitted.add(text)) {
                element.addAnnotation(text);
                topLevelClass.addImportedTypes(annotation.getImportedTypes());
            }
        }
        if (!emitted.isEmpty()) {
            topLevelClass.addImportedTypes(getImportedTypes(validateGroups));
        }
    }

    /**
     * 命名验证器 -> 后端校验注解。type/validator 配置值命中命名名（不区分大小写）时收集。
     */
    private void collectNamedValidatorAnnotation(List<AbstractAnnotation> annotations,
                                                 String configuredName,
                                                 HtmlValidatorElementConfiguration config,
                                                 IntrospectedColumn column,
                                                 String remarks,
                                                 String[] validateGroups) {
        if (!VStringUtil.stringHasValue(configuredName)) {
            return;
        }
        String name = configuredName.toLowerCase();
        String configMessage = VStringUtil.stringHasValue(config.getMessage()) ? config.getMessage() : null;
        switch (name) {
            case "email":
                if (column.isStringColumn()) {
                    annotations.add(ValidateEmail.create(
                            configMessage != null ? configMessage : remarks + "必须是有效的电子邮箱", validateGroups));
                }
                return;
            case "url":
                if (column.isStringColumn()) {
                    annotations.add(ValidatePattern.create(escapeJavaLiteral(URL_REGEXP),
                            configMessage != null ? configMessage : remarks + "必须是有效的网址", validateGroups));
                }
                return;
            case "positiveinteger":
                if (column.isNumericColumn()) {
                    annotations.add(ValidatePositive.create(
                            configMessage != null ? configMessage : remarks + "必须大于0", validateGroups));
                } else if (column.isStringColumn()) {
                    annotations.add(ValidatePattern.create(escapeJavaLiteral("^\\d*[1-9]\\d*$"),
                            configMessage != null ? configMessage : remarks + "必须是正整数", validateGroups));
                }
                return;
            case "money":
                if (column.isStringColumn()) {
                    annotations.add(ValidatePattern.create(escapeJavaLiteral(buildMoneyRegexp(config)),
                            configMessage != null ? configMessage : remarks + "金额格式不正确", validateGroups));
                } else if (column.isNumericColumn() && !Boolean.TRUE.equals(config.getAllowNegative())) {
                    // 数值列金额默认非负；小数位由列长 @Digits 兜底覆盖
                    annotations.add(DecimalMin.create("0",
                            configMessage != null ? configMessage : remarks + "不能小于0", validateGroups));
                }
                return;
            case "password":
                if (column.isStringColumn()) {
                    annotations.add(ValidatePattern.create(escapeJavaLiteral(buildPasswordRegexp(config)),
                            configMessage != null ? configMessage : remarks + "密码格式不符合要求", validateGroups));
                }
                return;
            case "equalto":
            case "datenotbefore":
            case "datenotafter":
                // 跨字段验证器无 jakarta 对应能力，仅前端规则生效
                return;
            default:
                String regexp = NAMED_VALIDATOR_REGEXPS.get(name);
                if (regexp != null && column.isStringColumn()) {
                    annotations.add(ValidatePattern.create(escapeJavaLiteral(regexp),
                            configMessage != null ? configMessage : remarks + "格式不正确", validateGroups));
                }
        }
    }

    /**
     * 金额正则：与前端 money 验证器一致，precision 控制小数位（默认2），allowNegative 允许负号。
     */
    private String buildMoneyRegexp(HtmlValidatorElementConfiguration config) {
        String sign = Boolean.TRUE.equals(config.getAllowNegative()) ? "-?" : "";
        int precision = parseIntOr(config.getPrecision(), 2);
        return "^" + sign + "\\d+" + (precision > 0 ? "(\\.\\d{1," + precision + "})?" : "") + "$";
    }

    /**
     * 密码强度正则：与前端 password 验证器一致，默认6-20位须含字母+数字，requireSpecial 追加特殊字符要求。
     */
    private String buildPasswordRegexp(HtmlValidatorElementConfiguration config) {
        int min = parseIntOr(config.getMin(), 6);
        int max = parseIntOr(config.getMax(), 20);
        String special = Boolean.TRUE.equals(config.getRequireSpecial()) ? "(?=.*[^a-zA-Z0-9])" : "";
        return "^(?=.*[a-zA-Z])(?=.*\\d)" + special + "[\\x21-\\x7e]{" + min + "," + max + "}$";
    }

    private int parseIntOr(String value, int defaultValue) {
        if (VStringUtil.stringHasValue(value)) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    /**
     * 查找列对应的表单级 validator 配置（scope!=2 的全局/表单规则）。
     * 同一列在多个 htmlMap 配置中出现时按声明顺序合并。
     */
    private List<HtmlValidatorElementConfiguration> getColumnValidatorConfigs(AbstractIntrospectedTable introspectedTable,
                                                                              IntrospectedColumn column) {
        if (column == null) {
            return new ArrayList<>();
        }
        return introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().stream()
                .flatMap(htmlConfig -> htmlConfig.getElementDescriptors().stream())
                .filter(descriptor -> descriptor.getColumn() != null
                        && column.getActualColumnName().equals(descriptor.getColumn().getActualColumnName()))
                .flatMap(descriptor -> descriptor.getHtmlValidatorElementConfigurations().stream())
                .filter(config -> config.getScope() != 2)
                .collect(Collectors.toList());
    }

    /**
     * 将原始正则转义为 Java 字符串字面量文本（反斜杠与双引号转义），保证生成源码可编译。
     */
    private String escapeJavaLiteral(String regexp) {
        return regexp.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void addDecimalMax(JavaElement element,
                               TopLevelClass topLevelClass,
                               IntrospectedColumn introspectedColumn,
                               String[] validateGroups,
                               AbstractIntrospectedTable introspectedTable, Set<String> validateIgnoreColumns){
        String shortName = introspectedColumn.getFullyQualifiedJavaType().getShortName();
    }

    private Set<FullyQualifiedJavaType> getImportedTypes(String[] validateGroups) {
        Set<FullyQualifiedJavaType> importedTypes = new HashSet<>();
        for (String validateGroup : validateGroups) {
            if ("ValidateInsert.class".equals(validateGroup)) {
                importedTypes.add(new FullyQualifiedJavaType(VALIDATE_INSERT));
            } else if ("ValidateUpdate.class".equals(validateGroup)) {
                importedTypes.add(new FullyQualifiedJavaType(VALIDATE_UPDATE));
            }
        }
        return importedTypes;
    }
}
