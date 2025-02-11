package org.mybatis.generator.internal.util;

import com.vgosoft.core.constant.GlobalConstant;
import com.vgosoft.core.constant.enums.IBaseEnum;
import com.vgosoft.tool.core.ObjectFactory;
import com.vgosoft.tool.core.VMD5Util;
import com.vgosoft.tool.core.VStringUtil;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.config.*;
import org.mybatis.generator.custom.ConstantsUtil;

import javax.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.vgosoft.tool.core.VStringUtil.stringHasValue;
import static com.vgosoft.tool.core.VStringUtil.toHyphenCase;
import static org.mybatis.generator.custom.ConstantsUtil.ANNOTATION_TRANSACTIONAL;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-03 14:53
 * @version 3.0
 */
public class Mb3GenUtil {

    private Mb3GenUtil() {
    }

    public static String getControllerBaseMappingPath(IntrospectedTable introspectedTable) {
        String beanName = ConfigUtil.getIntrospectedTableBeanName(introspectedTable.getTableConfiguration());
        String basePath = introspectedTable.getTableConfiguration().getServiceApiBasePath();
        if (StringUtility.stringHasValue(basePath)) {
            return basePath + "/" + toHyphenCase(beanName);
        } else {
            return toHyphenCase(beanName);
        }
    }

    public static String getModelCateId(Context context) {
        String aCase = StringUtils.lowerCase(context.getModuleKeyword());
        if (VStringUtil.stringHasValue(aCase)) {
            return VMD5Util.MD5_15(aCase);
        }
        return null;
    }

    public static String getModelKey(IntrospectedTable introspectedTable) {
        return StringUtils.lowerCase(introspectedTable.getContext().getModuleKeyword() + "_" + introspectedTable.getTableConfiguration().getDomainObjectName());
    }

    public static String getModelId(IntrospectedTable introspectedTable) {
        String modelKey = getModelKey(introspectedTable);
        return VMD5Util.MD5_15(modelKey);
    }


    public static String getDefaultViewId(IntrospectedTable introspectedTable) {
        String aCase = StringUtils.lowerCase(introspectedTable.getControllerBeanName() + GlobalConstant.DEFAULT_VIEW_ID_SUFFIX);
        if (VStringUtil.stringHasValue(aCase)) {
            return VMD5Util.MD5_15(aCase);
        }
        return null;
    }

    public static String getDefaultHtmlKey(IntrospectedTable introspectedTable) {
        String controllerBaseMappingPath = Mb3GenUtil.getControllerBaseMappingPath(introspectedTable);
        return controllerBaseMappingPath.replace("/", "-");
    }

    public static boolean isRequiredColumn(IntrospectedColumn introspectedColumn) {
        return !introspectedColumn.isNullable() && introspectedColumn.getDefaultValue() == null && !introspectedColumn.isAutoIncrement() && !introspectedColumn.isGeneratedColumn() && !introspectedColumn.isSequenceColumn();
    }

    public static boolean isInDefaultFields(IntrospectedTable introspectedTable, String fieldName) {
        if (GenerateUtils.isWorkflowInstance(introspectedTable) && ConstantsUtil.DEFAULT_WORKFLOW_FIELDS.contains(fieldName)) {
            return true;
        } else return ConstantsUtil.DEFAULT_CORE_FIELDS.contains(fieldName);
    }

    public static String getInnerListFragmentFileName(@Nullable HtmlElementInnerListConfiguration listConfiguration, IntrospectedTable introspectedTable) {
        if (listConfiguration != null) {
            String listKey = stringHasValue(listConfiguration.getListKey()) ? "_" + listConfiguration.getListKey() : "";
            return listConfiguration.getSourceViewPath() + listKey + "_" + ConstantsUtil.SUFFIX_INNER_LIST_FRAGMENTS + ".html";
        } else {
            return introspectedTable.getTableConfiguration().getTableName() + "_" + ConstantsUtil.SUFFIX_INNER_LIST_FRAGMENTS + ".html";
        }
    }

    public static String getHtmlInnerListFragmentFileName(@Nullable InnerListViewConfiguration innerListViewConfiguration, IntrospectedTable introspectedTable) {
        if (innerListViewConfiguration != null) {
            String listKey = stringHasValue(innerListViewConfiguration.getListKey()) ? "_" + innerListViewConfiguration.getListKey() : "";
            return innerListViewConfiguration.getEditExtendsForm() + listKey + "_" + ConstantsUtil.SUFFIX_INNER_LIST_FRAGMENTS + ".html";
        } else {
            return introspectedTable.getTableConfiguration().getTableName() + "_" + ConstantsUtil.SUFFIX_INNER_LIST_FRAGMENTS + ".html";
        }
    }

    public static String getDateType(@Nullable HtmlElementDescriptor htmlElementDescriptor, IntrospectedColumn introspectedColumn) {
        String dateType = htmlElementDescriptor != null && htmlElementDescriptor.getDataFormat() != null ? htmlElementDescriptor.getDataFormat() : null;
        if (introspectedColumn.getJdbcType() == 93) {
            dateType = "datetime";
        } else if (introspectedColumn.getJdbcType() == 91) {
            dateType = "date";
        } else if (introspectedColumn.getJdbcType() == 92) {
            dateType = "time";
        } else {
            if (StringUtility.stringHasValue(dateType)) {
                //年|年月|日期|日期时间|时间
                switch (dateType) {
                    case "年":
                        dateType = "year";
                        break;
                    case "年月":
                        dateType = "month";
                        break;
                    case "年周":
                        dateType = "week";
                        break;
                    case "日期":
                        dateType = "date";
                        break;
                    case "日期时间":
                        dateType = "datetime";
                        break;
                    case "时间":
                        dateType = "time";
                        break;
                    default:
                        dateType = "date";
                        break;
                }
            } else {
                dateType = "date";
            }
        }
        return dateType;
    }

    public static String getDateFormat(@Nullable HtmlElementDescriptor htmlElementDescriptor, IntrospectedColumn introspectedColumn) {
        if (htmlElementDescriptor != null && StringUtility.stringHasValue(htmlElementDescriptor.getDataFmt())) {
            return htmlElementDescriptor.getDataFmt();
        } else {
            if (introspectedColumn.getJdbcType() == 93) {
                return "yyyy-MM-dd HH:mm:ss";
            } else if (introspectedColumn.getJdbcType() == 91) {
                return "yyyy-MM-dd";
            } else if (introspectedColumn.getJdbcType() == 92) {
                return "HH:mm:ss";
            } else {
                return "yyyy-MM-dd";
            }
        }
    }

    public static String getEnumClassNameByDataFmt(String dataFormat) {
        switch (dataFormat) {
            case "exist":
            case "有":
            case "有无":
                return "com.vgosoft.core.constant.enums.core.ExistOrNotEnum";
            case "yes":
            case "true":
            case "是":
            case "是否":
                return "com.vgosoft.core.constant.enums.core.YesNoEnum";
            case "sex":
            case "性别":
                return "com.vgosoft.core.constant.enums.core.GenderEnum";
            case "启停":
            case "启用停用":
            case "state":
                return "com.vgosoft.core.constant.enums.core.CommonStatusEnum";
            case "急":
            case "缓急":
                return "com.vgosoft.core.constant.enums.core.UrgencyEnum";
            case "level":
            case "级别":
                return "com.vgosoft.core.constant.enums.core.LevelListEnum";
            default:
                return null;
        }
    }

    //获得switch的lay-text属性值
    public static String getSwitchTextByDataFmt(String dataFormat) {
        return getSwitchTextByEnumClassName(getEnumClassNameByDataFmt(dataFormat));
    }

    public static String getSwitchTextByEnumClassName(String enumClassName) {
        if (enumClassName != null) {
            Class<?> aClass = ObjectFactory.internalClassForName(enumClassName);
            if (aClass.isEnum() && IBaseEnum.class.isAssignableFrom(aClass)) {
                Object[] enumConstants = aClass.getEnumConstants();
                if (enumConstants.length > 1) {
                    return ((IBaseEnum<?>) enumConstants[0]).codeName() + "|" + ((IBaseEnum<?>) enumConstants[1]).codeName();
                }
            }
        }
        return null;
    }

    public static Map<String,String> getColumnRenderFunMap(IntrospectedTable introspectedTable) {
        // 列渲染
        Map<String, String> columnRenderFunMap = new HashMap<>();
        if (introspectedTable.getRules().isGenerateViewVO()) {
            VOViewGeneratorConfiguration viewConfiguration = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoViewConfiguration();
            viewConfiguration.getVoColumnRenderFunGeneratorConfigurations().forEach(config -> {
                config.getFieldNames().forEach(fieldName -> {
                    columnRenderFunMap.putIfAbsent(fieldName, config.getRenderFun());
                });
            });
        }
        return columnRenderFunMap;
    }

    /**
     * 添加事务注解
     *
     * @param parentElement 类
     * @param method        方法
     * @param isolation     事务隔离级别：DEFAULT，READ_UNCOMMITTED，READ_COMMITTED，REPEATABLE_READ,SERIALIZABLE
     */
    public static void addTransactionalAnnotation(TopLevelClass parentElement, Method method, @Nullable String isolation) {
        parentElement.addImportedType(ANNOTATION_TRANSACTIONAL);
        parentElement.addImportedType("java.lang.Exception");
        if (isolation != null && !"DEFAULT".equals(isolation)) {
            parentElement.addImportedType("org.springframework.transaction.annotation.Isolation");
            method.addAnnotation("@Transactional(rollbackFor = Exception.class, isolation = Isolation." + isolation + ")");
        } else {
            method.addAnnotation("@Transactional(rollbackFor = Exception.class)" );
        }

    }
}
