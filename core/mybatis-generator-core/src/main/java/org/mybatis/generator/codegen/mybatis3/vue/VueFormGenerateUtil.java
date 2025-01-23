package org.mybatis.generator.codegen.mybatis3.vue;

import com.vgosoft.core.constant.enums.core.DictTypeEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementTagTypeEnum;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.config.HtmlElementDescriptor;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.config.InnerListViewConfiguration;
import org.mybatis.generator.custom.annotations.VueFormItemMetaDesc;
import org.mybatis.generator.custom.annotations.VueFormItemRuleDesc;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

public class VueFormGenerateUtil {

    /**
     * 获取默认的placeholder
     *
     * @param elementDescriptor  元素描述
     * @param introspectedColumn 列描述
     * @return 默认的placeholder
     */
    public static String getDefaultPlaceholder(@Nullable HtmlElementDescriptor elementDescriptor, @Nullable IntrospectedColumn introspectedColumn) {
        if (elementDescriptor != null) {
            switch (HtmlElementTagTypeEnum.ofCodeName(elementDescriptor.getTagType())) {
                case INPUT:
                    return "请输入";
                case SELECT:
                case DROPDOWN_LIST:
                case DATE:
                case CASCADER:
                case TABLE_SELECT:
                    return "请选择";
                case NUMBER:
                    return "请输入数字";
                case EDITOR:
                    return "请输入内容";
                default:
                    return "";
            }
        } else {
            if (introspectedColumn != null) {
                return "请输入";
            } else {
                return "请输入";
            }
        }
    }

    public static void setComponentName(VueFormItemMetaDesc vueFormItemMetaDesc, @Nullable HtmlElementDescriptor elementDescriptor, @Nullable IntrospectedColumn introspectedColumn) {
        if (elementDescriptor != null) {
            if (HtmlElementTagTypeEnum.DROPDOWN_LIST.codeName().equals(elementDescriptor.getTagType())) {
                vueFormItemMetaDesc.setComponent(HtmlElementTagTypeEnum.SELECT.codeName());
            } else {
                vueFormItemMetaDesc.setComponent(elementDescriptor.getTagType());
            }
        } else if (introspectedColumn != null) {
            if (introspectedColumn.isNumericColumn()) {
                vueFormItemMetaDesc.setComponent(HtmlElementTagTypeEnum.NUMBER.codeName());
            } else if (introspectedColumn.isJDBCDateColumn() || introspectedColumn.isJDBCTimeStampColumn() || introspectedColumn.isJavaLocalDateColumn() || introspectedColumn.isJavaLocalDateTimeColumn()) {
                vueFormItemMetaDesc.setComponent(HtmlElementTagTypeEnum.DATE.codeName());
            } else if (introspectedColumn.isJDBCTimeColumn() || introspectedColumn.isJavaLocalTimeColumn()) {
                vueFormItemMetaDesc.setComponent(HtmlElementTagTypeEnum.TIME.codeName());
            } else if (introspectedColumn.isLongVarchar()) {
                vueFormItemMetaDesc.setComponent(HtmlElementTagTypeEnum.EDITOR.codeName());
            } else if (introspectedColumn.getFullyQualifiedJavaType().getShortName().equalsIgnoreCase("boolean")) {
                vueFormItemMetaDesc.setComponent(HtmlElementTagTypeEnum.SWITCH.codeName());
            } else {
                vueFormItemMetaDesc.setComponent(HtmlElementTagTypeEnum.INPUT.codeName());
            }
        } else {
            vueFormItemMetaDesc.setComponent(HtmlElementTagTypeEnum.INPUT.codeName());
        }
        if (HtmlElementTagTypeEnum.INPUT.codeName().equals(vueFormItemMetaDesc.getComponent()) && introspectedColumn != null) {
            if (introspectedColumn.getLength() > 499) {
                vueFormItemMetaDesc.setSpan(24);
            }
            if (introspectedColumn.getLength() > 1499) {
                vueFormItemMetaDesc.setType("textarea");
            }
        } else if (HtmlElementTagTypeEnum.EDITOR.codeName().equals(vueFormItemMetaDesc.getComponent())) {
            vueFormItemMetaDesc.setSpan(24);
        }
    }

    public static void setDateTimeTypFormat(VueFormItemMetaDesc vueFormItemMetaDesc, @Nullable HtmlElementDescriptor elementDescriptor, @Nullable IntrospectedColumn introspectedColumn) {
        if (HtmlElementTagTypeEnum.DATE.codeName().equals(vueFormItemMetaDesc.getComponent()) || HtmlElementTagTypeEnum.TIME.codeName().equals(vueFormItemMetaDesc.getComponent())) {
            if (elementDescriptor != null && elementDescriptor.getDataFormat() != null) {
                switch (elementDescriptor.getDataFormat()) {
                    case "年":
                        if (elementDescriptor.isDateRange()) {
                            vueFormItemMetaDesc.setDateRange(true);
                            vueFormItemMetaDesc.setType("yearrange");
                        } else if (elementDescriptor.isMultiple()) {
                            vueFormItemMetaDesc.setMultiple(true);
                            vueFormItemMetaDesc.setType("years");
                        } else {
                            vueFormItemMetaDesc.setType("year");
                        }
                        vueFormItemMetaDesc.setValueFormat("YYYY");
                        break;
                    case "年月":
                        if (elementDescriptor.isDateRange()) {
                            vueFormItemMetaDesc.setDateRange(true);
                            vueFormItemMetaDesc.setType("monthrange");
                        } else if (elementDescriptor.isMultiple()) {
                            vueFormItemMetaDesc.setMultiple(true);
                            vueFormItemMetaDesc.setType("months");
                        } else {
                            vueFormItemMetaDesc.setType("month");
                        }
                        vueFormItemMetaDesc.setValueFormat("YYYY-MM");
                        break;
                    case "年周":
                        vueFormItemMetaDesc.setType("week");
                        vueFormItemMetaDesc.setValueFormat("YYYY-WW");
                        break;
                    case "日期":
                        if (elementDescriptor.isDateRange()) {
                            vueFormItemMetaDesc.setDateRange(true);
                            vueFormItemMetaDesc.setType("daterange");
                        } else if (elementDescriptor.isMultiple()) {
                            vueFormItemMetaDesc.setMultiple(true);
                            vueFormItemMetaDesc.setType("dates");
                        } else {
                            vueFormItemMetaDesc.setType("date");
                        }
                        vueFormItemMetaDesc.setValueFormat("YYYY-MM-DD");
                        break;
                    case "日期时间":
                        if (elementDescriptor.isDateRange()) {
                            vueFormItemMetaDesc.setDateRange(true);
                            vueFormItemMetaDesc.setType("datetimerange");
                        } else {
                            vueFormItemMetaDesc.setType("datetime");
                        }
                        vueFormItemMetaDesc.setValueFormat("YYYY-MM-DD HH:mm:ss");
                        break;
                    case "时间":
                        vueFormItemMetaDesc.setType("time");
                        vueFormItemMetaDesc.setValueFormat("HH:mm:ss");
                        break;
                    default:
                        vueFormItemMetaDesc.setType("date");
                        vueFormItemMetaDesc.setValueFormat(elementDescriptor.getDataFormat());
                        break;
                }
            } else {
                if (introspectedColumn != null) {
                    if (introspectedColumn.isJDBCDateColumn() || introspectedColumn.isJavaLocalDateColumn()) {
                        vueFormItemMetaDesc.setType("date");
                        vueFormItemMetaDesc.setValueFormat("YYYY-MM-DD");
                    } else if (introspectedColumn.isJDBCTimeStampColumn() || introspectedColumn.isJavaLocalDateTimeColumn()) {
                        vueFormItemMetaDesc.setType("datetime");
                        vueFormItemMetaDesc.setValueFormat("YYYY-MM-DD HH:mm:ss");
                    } else if (introspectedColumn.isJDBCTimeColumn() || introspectedColumn.isJavaLocalTimeColumn()) {
                        vueFormItemMetaDesc.setType("time");
                        vueFormItemMetaDesc.setValueFormat("HH:mm:ss");
                    } else {
                        vueFormItemMetaDesc.setType("date");
                        vueFormItemMetaDesc.setValueFormat("YYYY-MM-DD");
                    }
                } else {
                    vueFormItemMetaDesc.setType("date");
                    vueFormItemMetaDesc.setValueFormat("yyyy-MM-dd");
                }

            }
        } else if (vueFormItemMetaDesc.getComponent().equals(HtmlElementTagTypeEnum.SWITCH.codeName())) {
            if (introspectedColumn != null) {
                switch (introspectedColumn.getFullyQualifiedJavaType().getShortName().toLowerCase()) {
                    case "boolean":
                    case "bool":
                    case "bit":
                        vueFormItemMetaDesc.setValueFormat("boolean");
                        break;
                    case "integer":
                    case "int":
                    case "long":
                    case "short":
                    case "byte":
                    case "double":
                    case "float":
                        vueFormItemMetaDesc.setValueFormat("number");
                        break;
                    default:
                        vueFormItemMetaDesc.setValueFormat("string");
                        break;
                }
            } else {
                vueFormItemMetaDesc.setValueFormat("string");
            }
        }

    }

    public static void addDataUrl(HtmlElement element, HtmlElementDescriptor htmlElementDescriptor, String defaultDataUrl) {
        if (htmlElementDescriptor == null) {
            return;
        }
        if (htmlElementDescriptor.getDataUrl() != null) {
            element.addAttribute(new Attribute("data-url", htmlElementDescriptor.getDataUrl()));
        } else if (htmlElementDescriptor.getDataSource() != null && htmlElementDescriptor.getDataSource().equals(DictTypeEnum.DICT_ENUM.getCode()) && stringHasValue(htmlElementDescriptor.getEnumClassName())) {
            element.addAttribute(new Attribute("data-url", "/system/enum/options/" + htmlElementDescriptor.getEnumClassName()));
        } else if (defaultDataUrl != null) {
            element.addAttribute(new Attribute("data-url", defaultDataUrl));
        }
    }

    public static String getRules(VueFormItemMetaDesc vueFormItemMetaDesc, IntrospectedColumn introspectedColumn, HtmlElementDescriptor elementDescriptor, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        if (introspectedColumn == null) {
            return null;
        }
        List<FormItemRule> formItemRules = new ArrayList<>();
        if (elementDescriptor != null) {
            if (!elementDescriptor.getVerify().isEmpty()) {
                for (String verify : elementDescriptor.getVerify()) {
                    if ("required".equals(verify)) {
                        FormItemRule formItemRule = new FormItemRule(vueFormItemMetaDesc);
                        formItemRule.setRequired(true);
                        formItemRule.setMin(1);
                        formItemRule.setMessage(introspectedColumn.getRemarks(true) + "不能为空");
                        formItemRules.add(formItemRule);
                    } else if ("date".equals(verify)) {
                        FormItemRule formItemRule = new FormItemRule(vueFormItemMetaDesc);
                        formItemRule.setType("date");
                        formItemRule.setMessage(introspectedColumn.getRemarks(true) + "必须为日期");
                        formItemRules.add(formItemRule);
                    } else if ("limit".equals(verify)) {
                        FormItemRule formItemRule = new FormItemRule(vueFormItemMetaDesc);
                        formItemRule.setMax(introspectedColumn.getLength());
                        formItemRule.setMessage(introspectedColumn.getRemarks(true) + "最大长度为" + introspectedColumn.getLength());
                        formItemRules.add(formItemRule);
                    }
                }
            }
        } else {
            //根据introspectedColumn的类型生成rules
            if (!introspectedColumn.isNullable()
                    || introspectedColumn.isJDBCDateColumn() || introspectedColumn.isJDBCTimeStampColumn() || introspectedColumn.isJava8TimeColumn() || introspectedColumn.isJDBCTimeColumn()
                    || introspectedColumn.isJdbcCharacterColumn()) {
                if (!introspectedColumn.isNullable()) {
                    FormItemRule formItemRule = new FormItemRule(vueFormItemMetaDesc);
                    formItemRule.setRequired(true);
                    formItemRule.setMessage(introspectedColumn.getRemarks(true) + "不能为空");
                    formItemRules.add(formItemRule);
                }
                if (introspectedColumn.isJDBCDateColumn() || introspectedColumn.isJDBCTimeStampColumn() || introspectedColumn.isJava8TimeColumn() || introspectedColumn.isJDBCTimeColumn()) {
                    FormItemRule formItemRule = new FormItemRule(vueFormItemMetaDesc);
                    formItemRule.setType("date");
                    formItemRule.setMessage(introspectedColumn.getRemarks(true) + "必须为日期");
                    formItemRules.add(formItemRule);
                }
                if (introspectedColumn.isStringColumn() && introspectedColumn.getLength() > 0 && introspectedColumn.getLength() < 5000) {
                    FormItemRule formItemRule = new FormItemRule(vueFormItemMetaDesc);
                    formItemRule.setMax(introspectedColumn.getLength());
                    formItemRule.setMessage(introspectedColumn.getRemarks(true) + "最大长度为" + introspectedColumn.getLength());
                    formItemRules.add(formItemRule);
                }
            }
        }

        //是否为requiredColumns的中的字段
        //如果formItemRules中没有required的规则，且htmlGeneratorConfiguration中配置了该字段为必填，则生成required的规则
        if (formItemRules.stream().noneMatch(r -> r.getVueFormItemMetaDesc().getFieldName().equalsIgnoreCase(introspectedColumn.getJavaProperty()) && r.isRequired())) {
            if (htmlGeneratorConfiguration.getElementRequired().contains(introspectedColumn.getActualColumnName())) {
                FormItemRule formItemRule = new FormItemRule(vueFormItemMetaDesc);
                formItemRule.setRequired(true);
                formItemRule.setMin(1);
                formItemRule.setMessage(introspectedColumn.getRemarks(true) + "不能为空");
                formItemRules.add(formItemRule);
            }
        }

        //生成rules注解
        return formItemRules.stream().map(r -> new VueFormItemRuleDesc(r).toAnnotation()).collect(Collectors.joining("\n                    , "));
    }

    public static String innerListItemRules(InnerListViewConfiguration listViewConfiguration, IntrospectedColumn introspectedColumn) {
        if (introspectedColumn == null) {
            return null;
        }
        //根据introspectedColumn的类型生成rules
        Set<FormItemRule> formItemRules = new HashSet<>();
        if (!introspectedColumn.isNullable()
                || introspectedColumn.isJDBCDateColumn() || introspectedColumn.isJDBCTimeStampColumn() || introspectedColumn.isJava8TimeColumn() || introspectedColumn.isJDBCTimeColumn()
                || introspectedColumn.isJdbcCharacterColumn()) {
            if (!introspectedColumn.isNullable()) {
                FormItemRule formItemRule = new FormItemRule(introspectedColumn.getRemarks(true) + "不能为空");
                formItemRule.setRequired(true);
                formItemRules.add(formItemRule);
            }
            if (introspectedColumn.isJDBCDateColumn() || introspectedColumn.isJDBCTimeStampColumn() || introspectedColumn.isJava8TimeColumn() || introspectedColumn.isJDBCTimeColumn()) {
                FormItemRule formItemRule = new FormItemRule(introspectedColumn.getRemarks(true) + "必须为日期");
                formItemRule.setType("date");
                formItemRules.add(formItemRule);
            }
            if (introspectedColumn.isStringColumn() && introspectedColumn.getLength() > 0 && introspectedColumn.getLength() < 5000) {
                FormItemRule formItemRule = new FormItemRule(introspectedColumn.getRemarks(true) + "最大长度为" + introspectedColumn.getLength());
                formItemRule.setMax(introspectedColumn.getLength());
                formItemRules.add(formItemRule);
            }
        }

        //是否为requiredColumns的中的字段
        if (introspectedColumn.isNullable() && listViewConfiguration.getRequiredColumns().contains(introspectedColumn.getActualColumnName())) {
            FormItemRule formItemRule = new FormItemRule(introspectedColumn.getRemarks(true) + "不能为空");
            formItemRule.setRequired(true);
            formItemRule.setMin(1);
            formItemRules.add(formItemRule);
        }

        //生成rules注解
        return formItemRules.stream().map(r -> new VueFormItemRuleDesc(r).toAnnotation()).collect(Collectors.joining("\n                    , "));
    }

    public static void setRemoteValueType(VueFormItemMetaDesc vueFormItemMetaDesc, IntrospectedColumn introspectedColumn, HtmlElementDescriptor elementDescriptor) {
        if (elementDescriptor != null && stringHasValue(elementDescriptor.getRemoteValueType())) {
            vueFormItemMetaDesc.setRemoteValueType(elementDescriptor.getRemoteValueType());
        } else {
            if (introspectedColumn != null) {
                if (introspectedColumn.isBooleanColumn()) {
                    vueFormItemMetaDesc.setRemoteValueType("boolean");
                } else if (introspectedColumn.isNumericColumn()) {
                    vueFormItemMetaDesc.setRemoteValueType("number");
                } else if (introspectedColumn.isJDBCDateColumn() || introspectedColumn.isJDBCTimeStampColumn() || introspectedColumn.isJavaLocalDateColumn() || introspectedColumn.isJavaLocalDateTimeColumn()) {
                    vueFormItemMetaDesc.setRemoteValueType("date");
                } else if (introspectedColumn.isJDBCTimeColumn() || introspectedColumn.isJavaLocalTimeColumn()) {
                    vueFormItemMetaDesc.setRemoteValueType("time");
                } else {
                    vueFormItemMetaDesc.setRemoteValueType("string");
                }
            } else {
                vueFormItemMetaDesc.setRemoteValueType("string");
            }
        }

    }
}
