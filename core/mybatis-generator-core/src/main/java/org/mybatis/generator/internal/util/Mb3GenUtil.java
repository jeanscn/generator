package org.mybatis.generator.internal.util;

import com.vgosoft.core.constant.GlobalConstant;
import com.vgosoft.tool.core.VMD5Util;
import com.vgosoft.tool.core.VStringUtil;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.config.ConfigUtil;
import org.mybatis.generator.config.Context;

import static com.vgosoft.tool.core.VStringUtil.toHyphenCase;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-03 14:53
 * @version 3.0
 */
public class Mb3GenUtil {

    public static String getControllerBaseMappingPath(IntrospectedTable introspectedTable){
        String beanName = ConfigUtil.getIntrospectedTableBeanName(introspectedTable.getTableConfiguration());
        String basePath = introspectedTable.getTableConfiguration().getServiceApiBasePath();
        if (StringUtility.stringHasValue(basePath)) {
            return basePath + "/" + toHyphenCase(beanName);
        }else {
            return toHyphenCase(beanName);
        }
    }

    public static String getModelCateId(Context context){
        String aCase = StringUtils.lowerCase(context.getModuleKeyword());
        if (VStringUtil.stringHasValue(aCase)) {
            return VMD5Util.MD5_15(aCase);
        }
        return null;
    }

    public static String getModelKey(IntrospectedTable introspectedTable){
        return StringUtils.lowerCase(introspectedTable.getContext().getModuleKeyword() + "_" + introspectedTable.getTableConfiguration().getDomainObjectName());
    }

    public static String getDefaultViewId(IntrospectedTable introspectedTable){
        String aCase = StringUtils.lowerCase(introspectedTable.getControllerBeanName()+ GlobalConstant.DEFAULT_VIEW_ID_SUFFIX);
        if (VStringUtil.stringHasValue(aCase)) {
            return VMD5Util.MD5_15(aCase);
        }
        return null;
    }

    public boolean isRequiredColumn(IntrospectedColumn introspectedColumn) {
        return !introspectedColumn.isNullable() && introspectedColumn.getDefaultValue() == null && !introspectedColumn.isAutoIncrement() && !introspectedColumn.isGeneratedColumn() && !introspectedColumn.isSequenceColumn();
    }
}
