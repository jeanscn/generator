package org.mybatis.generator.codegen.mybatis3.vue;

import com.vgosoft.core.constant.enums.core.DictTypeEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementTagTypeEnum;
import com.vgosoft.core.constant.enums.view.ValidatorRuleTypeEnum;
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
import java.util.List;

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
                case TABLE_SELECT_POP:
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
            if (HtmlElementTagTypeEnum.DATE.codeName().equals(vueFormItemMetaDesc.getComponent())) {
                vueFormItemMetaDesc.setTsType("date");
            } else {
                vueFormItemMetaDesc.setTsType("time");
            }

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

    public static String getRules(IntrospectedColumn introspectedColumn, HtmlElementDescriptor elementDescriptor, HtmlGeneratorConfiguration htmlGeneratorConfiguration,int scope) {
        if (introspectedColumn == null) {
            return null;
        }
        //生成vueFormItemMetaDesc,顺序为transform、required、type、 length、minMax、pattern、enum
        List<String> vueFormItemRuleDecList = new ArrayList<>();
        String tranForm = new VueFormItemRuleDesc(ValidatorRuleTypeEnum.TRANS_FORM, scope,introspectedColumn, elementDescriptor, htmlGeneratorConfiguration).toAnnotation();
        if (tranForm != null) {
            vueFormItemRuleDecList.add(tranForm);
        }
        String required = new VueFormItemRuleDesc(ValidatorRuleTypeEnum.REQUIRED, scope,introspectedColumn, elementDescriptor, htmlGeneratorConfiguration).toAnnotation();
        if (required != null) {
            vueFormItemRuleDecList.add(required);
        }
        String type = new VueFormItemRuleDesc(ValidatorRuleTypeEnum.TYPE, scope,introspectedColumn, elementDescriptor, htmlGeneratorConfiguration).toAnnotation();
        if (type != null) {
            vueFormItemRuleDecList.add(type);
        }
        String length = new VueFormItemRuleDesc(ValidatorRuleTypeEnum.LENGTH, scope,introspectedColumn, elementDescriptor,htmlGeneratorConfiguration).toAnnotation();
        if (length != null) {
            vueFormItemRuleDecList.add(length);
        }
        String mimMax = new VueFormItemRuleDesc(ValidatorRuleTypeEnum.MIN_MAX, scope,introspectedColumn, elementDescriptor, htmlGeneratorConfiguration).toAnnotation();
        if (mimMax != null) {
            vueFormItemRuleDecList.add(mimMax);
        }
        String pattern = new VueFormItemRuleDesc(ValidatorRuleTypeEnum.PATTERN, scope,introspectedColumn, elementDescriptor, htmlGeneratorConfiguration).toAnnotation();
        if (pattern != null) {
            vueFormItemRuleDecList.add(pattern);
        }
        String enumList = new VueFormItemRuleDesc(ValidatorRuleTypeEnum.ENUM, scope,introspectedColumn, elementDescriptor, htmlGeneratorConfiguration).toAnnotation();
        if (enumList != null) {
            vueFormItemRuleDecList.add(enumList);
        }
        String validator = new VueFormItemRuleDesc(ValidatorRuleTypeEnum.VALIDATOR, scope,introspectedColumn, elementDescriptor, htmlGeneratorConfiguration).toAnnotation();
        if (validator != null) {
            vueFormItemRuleDecList.add(validator);
        }
        //如果vueFormItemRuleDecList为空，则返回null
        if (vueFormItemRuleDecList.isEmpty()) {
            return null;
        }
        //如果vueFormItemRuleDecList不为空，则返回rules注解
        return String.join("\n                    , ", vueFormItemRuleDecList);
    }

    public static String innerListItemRules(InnerListViewConfiguration listViewConfiguration, IntrospectedColumn introspectedColumn) {
        if (introspectedColumn == null) {
            return null;
        }
        HtmlGeneratorConfiguration htmlGeneratorConfiguration = listViewConfiguration.getHtmlGeneratorConfiguration();
        HtmlElementDescriptor htmlElementDescriptor = listViewConfiguration.getElementDescriptorMap().get(introspectedColumn.getJavaProperty());
        return getRules(introspectedColumn, htmlElementDescriptor, htmlGeneratorConfiguration,2);
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
