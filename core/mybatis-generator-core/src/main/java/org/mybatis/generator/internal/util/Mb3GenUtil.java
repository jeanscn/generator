package org.mybatis.generator.internal.util;

import com.vgosoft.core.constant.GlobalConstant;
import com.vgosoft.tool.core.VMD5Util;
import com.vgosoft.tool.core.VStringUtil;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.config.*;
import org.mybatis.generator.custom.ConstantsUtil;

import javax.annotation.Nullable;

import static com.vgosoft.tool.core.VStringUtil.stringHasValue;
import static com.vgosoft.tool.core.VStringUtil.toHyphenCase;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-03 14:53
 * @version 3.0
 */
public class Mb3GenUtil {

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

    public static String getDefaultViewId(IntrospectedTable introspectedTable) {
        String aCase = StringUtils.lowerCase(introspectedTable.getControllerBeanName() + GlobalConstant.DEFAULT_VIEW_ID_SUFFIX);
        if (VStringUtil.stringHasValue(aCase)) {
            return VMD5Util.MD5_15(aCase);
        }
        return null;
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
        if (listConfiguration!=null) {
            String listKey = stringHasValue(listConfiguration.getListKey())?"_"+listConfiguration.getListKey():"";
            return listConfiguration.getSourceViewPath()+listKey+"_"+ ConstantsUtil.SUFFIX_INNER_LIST_FRAGMENTS+".html";
        }else{
            return introspectedTable.getTableConfiguration().getTableName()+"_"+ ConstantsUtil.SUFFIX_INNER_LIST_FRAGMENTS+".html";
        }
    }

    public static String getHtmlInnerListFragmentFileName(@Nullable InnerListViewConfiguration innerListViewConfiguration, IntrospectedTable introspectedTable) {
        if (innerListViewConfiguration!=null) {
            String listKey = stringHasValue(innerListViewConfiguration.getListKey())?"_"+innerListViewConfiguration.getListKey():"";
            return innerListViewConfiguration.getEditExtendsForm()+listKey+"_"+ ConstantsUtil.SUFFIX_INNER_LIST_FRAGMENTS+".html";
        }else{
            return introspectedTable.getTableConfiguration().getTableName()+"_"+ ConstantsUtil.SUFFIX_INNER_LIST_FRAGMENTS+".html";
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
        if (htmlElementDescriptor != null && StringUtility.stringHasValue(htmlElementDescriptor.getDateFmt())) {
            return htmlElementDescriptor.getDateFmt();
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
}
