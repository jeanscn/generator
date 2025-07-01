package org.mybatis.generator.codegen.mybatis3.htmlmapper;

import static org.mybatis.generator.custom.ConstantsUtil.I_BUSINESS_ENTITY;
import static org.mybatis.generator.custom.ConstantsUtil.I_PERSISTENCE_BLOB;
import static org.mybatis.generator.custom.ConstantsUtil.I_WORK_FLOW_BASE_ENTITY;

import java.util.Set;

import org.apache.commons.lang3.ClassUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.internal.ObjectFactory;

import cn.hutool.core.util.ObjectUtil;

/**
 * 工具类
 */
public class GenerateUtils {

    /*判断当前属性是否为隐藏属性*/
    public static boolean isHiddenColumn(IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        if (htmlGeneratorConfiguration == null) {
            return false;
        }
        Set<String> hiddenColumn = htmlGeneratorConfiguration.getHiddenFieldNames();
        return hiddenColumn.contains(introspectedColumn.getJavaProperty());
    }
    public static String getLocalCssFilePath(String path, String filename) {
        String css = GenerateUtils.genLocalFilePath(path, filename, "css");
        return css.replace(".css", ".min.css");
    }

    /*构造文件路径*/
    public static String genLocalFilePath(String path, String filename, String type) {
        StringBuilder sb = new StringBuilder();
        sb.append("/").append(type).append("/");
        if (path != null && !path.isEmpty()) {
            if (path.contains(".")) {
                path = path.replace(".", "/");
            }
            sb.append(path).append("/");
        }
        if (filename != null && !filename.isEmpty()) {
            sb.append(filename).append(".").append(type);
        }
        return sb.toString();
    }

    public static String getEntityKeyStr(IntrospectedTable introspectedTable) {
        return GenerateUtils.isWorkflowInstance(introspectedTable) ? "business" : "entity";
    }

    /**
     * 生成的实体是否工作流子类
     *
     * @param introspectedTable 代码生成的基类
     * @return 布尔值 true为工作流对象，否则 false
     */
    public static Boolean isWorkflowInstance(IntrospectedTable introspectedTable) {
        String rootClass = introspectedTable.getTableConfigurationProperty(PropertyRegistry.ROOT_CLASS);
        return isAssignable(rootClass, I_WORK_FLOW_BASE_ENTITY);
    }

    /**
     * 生成的实体是否为含有大字段的对象
     *
     * @param introspectedTable 代码生成的基类
     * @return 布尔值 true为工含有大字段，否则 false
     */
    public static Boolean isBlobInstance(IntrospectedTable introspectedTable) {
        String rootClass = introspectedTable.getTableConfigurationProperty(PropertyRegistry.ROOT_CLASS);
        return isAssignable(rootClass, I_PERSISTENCE_BLOB);
    }

    /**
     * 生成的实体是否为业务对象
     *
     * @param introspectedTable 代码生成的基类
     * @return 布尔值 true为业务对象，否则 false
     */
    public static Boolean isBusinessInstance(IntrospectedTable introspectedTable) {
        String rootClass = introspectedTable.getTableConfigurationProperty(PropertyRegistry.ROOT_CLASS);
        return isAssignable(rootClass, I_BUSINESS_ENTITY);
    }

    public static boolean isDateType(IntrospectedColumn introspectedColumn) {
        return introspectedColumn.getJdbcType() == 91 || introspectedColumn.getJdbcType() == 92 || introspectedColumn.getJdbcType() == 93;
    }

    /**
     * 判断给定的类是否为rootClass的子类
     *
     * @param subClassName    子类名称
     * @param parentClassName 父类名称
     * @return 布尔值 true为子类，否则 false
     */
    public static boolean isAssignable(String subClassName, String parentClassName) {
        if (ObjectUtil.isEmpty(subClassName) || ObjectUtil.isEmpty(parentClassName)) {
            return false;
        }
        try {
            Class<?> aClass = ObjectFactory.internalClassForName(subClassName);
            Class<?> pClass = ObjectFactory.internalClassForName(parentClassName);
            return ClassUtils.isAssignable(aClass, pClass);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
