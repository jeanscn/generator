package org.mybatis.generator.custom.htmlGenerator;

import org.apache.commons.lang3.ClassUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.config.PropertyRegistry;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mybatis.generator.custom.ConstantsUtil.*;

/**
 *  工具类
 * @author <a href="mailto:cjj@vip.sina.com">ChenJJ</a>
 *  2020-07-29 10:53
 * @version 3.0
 */
public class GenerateUtils {

    /*判断当前属性是否为隐藏属性*/
     public static boolean isHiddenColumn(IntrospectedTable introspectedTable,IntrospectedColumn introspectedColumn, HtmlGeneratorConfiguration htmlGeneratorConfiguration){
         if (htmlGeneratorConfiguration == null) {
             return false;
         }
        Set<String> hiddenColumn = htmlGeneratorConfiguration.getHiddenColumns();
         Set<String> collect = introspectedTable.getTableConfiguration().getHtmlHiddenColumns()
                 .stream()
                 .map(IntrospectedColumn::getActualColumnName)
                 .collect(Collectors.toSet());
            hiddenColumn.addAll(collect);
         return hiddenColumn.contains(introspectedColumn.getActualColumnName());
    };

    public static String getLocalCssFilePath(String path, String filename) {
        return GenerateUtils.genLocalFilePath(path, filename, "css");
    }

    public static String getLocalJsFilePath(String path, String filename) {
        return GenerateUtils.genLocalFilePath(path, filename, "js");
    }

    /*构造文件路径*/
    public static String genLocalFilePath(String path, String filename, String type) {
        StringBuilder sb = new StringBuilder();
        sb.append("/").append(type).append("/");
        if (path != null && path.length() > 0) {
            if (path.contains(".")) {
                path = path.replace(".", "/");
            }
            sb.append(path).append("/");
        }
        if (filename != null && filename.length() > 0) {
            sb.append(filename).append(".").append(type);
        }
        return sb.toString();
    }

    /**
     * 生成的实体是否工作流子类
     * @param introspectedTable 代码生成的基类
     * @return 布尔值 true为工作流对象，否则 false
     */
    public static Boolean isWorkflowInstance(IntrospectedTable introspectedTable){
        String rootClass = introspectedTable.getTableConfigurationProperty(PropertyRegistry.ANY_ROOT_CLASS);
        try {
            if (rootClass != null) {
                Class<?> aClass = Class.forName(rootClass);
                Class<?> pClass = Class.forName(I_WORK_FLOW_BASE_ENTITY);
                return  ClassUtils.isAssignable(aClass,pClass);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public static String getEntityKeyStr(IntrospectedTable introspectedTable){
        return GenerateUtils.isWorkflowInstance(introspectedTable)?"business":"entity";
    }

    /**
     * 生成的实体是否为含有大字段的对象
     * @param introspectedTable 代码生成的基类
     * @return 布尔值 true为工含有大字段，否则 false
     */
    public static Boolean isBlobInstance(IntrospectedTable introspectedTable){
        String rootClass = introspectedTable.getTableConfigurationProperty(PropertyRegistry.ANY_ROOT_CLASS);
        try {
            if (rootClass != null) {
                Class<?> aClass = Class.forName(rootClass);
                Class<?> pClass = Class.forName(I_PERSISTENCE_BLOB);
                return  ClassUtils.isAssignable(aClass,pClass);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * 生成的实体是否为业务对象
     * @param introspectedTable 代码生成的基类
     * @return 布尔值 true为业务对象，否则 false
     */
    public static Boolean isBusinessInstance(IntrospectedTable introspectedTable){
        String rootClass = introspectedTable.getTableConfigurationProperty(PropertyRegistry.ANY_ROOT_CLASS);
        try {
            if (rootClass != null) {
                Class<?> aClass = Class.forName(rootClass);
                Class<?> pClass = Class.forName(I_BUSINESS_ENTITY);
                return  ClassUtils.isAssignable(aClass,pClass);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public static boolean isDateType(IntrospectedColumn introspectedColumn) {
        return introspectedColumn.getJdbcType() == 91 || introspectedColumn.getJdbcType() == 92 || introspectedColumn.getJdbcType() == 93;
    }
}
