/**
 *    Copyright 2006-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.custom.htmlGenerator;

import org.apache.commons.lang3.ClassUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.config.HtmlMapGeneratorConfiguration;
import org.mybatis.generator.config.PropertyRegistry;

import java.util.List;

import static org.mybatis.generator.custom.ConstantsUtil.*;

/**
 *  工具类
 * @author <a href="mailto:cjj@vip.sina.com">ChenJJ</a>
 *  2020-07-29 10:53
 * @version 3.0
 */
public class GenerateUtils {

    /*判断当前属性是否为隐藏属性*/
     public static boolean isHiddenColumn(IntrospectedColumn introspectedColumn, HtmlMapGeneratorConfiguration htmlMapGeneratorConfiguration){
         if (htmlMapGeneratorConfiguration == null) {
             return false;
         }
        List<String> hiddenColumn = htmlMapGeneratorConfiguration.getHiddenColumns();
        return hiddenColumn.contains(introspectedColumn.getActualColumnName().toUpperCase());
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
        sb.append("/" + type + "/");
        if (path != null && path.length() > 0) {
            if (path.indexOf(".") > -1) {
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

    public static Boolean isLongVarchar(IntrospectedColumn introspectedColumn){
        return introspectedColumn.getJdbcType()==-1 || introspectedColumn.getJdbcType()==-16;
    }
}
