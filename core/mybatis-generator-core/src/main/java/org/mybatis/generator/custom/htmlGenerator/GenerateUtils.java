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

import org.mybatis.generator.api.IntrospectedColumn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *  工具类
 * @author <a href="mailto:cjj@vip.sina.com">ChenJJ</a>
 *  2020-07-29 10:53
 * @version 3.0
 */
public class GenerateUtils {

    /*判断当前属性是否为隐藏属性*/
    public static boolean isHiddenJavaProperty(String propertyName){
        List<String> propertis = new ArrayList<>(Arrays.asList(
                "id","version","created","modified"
        ));
        return propertis.contains(propertyName);
    }

    public static boolean isHiddenColumn(IntrospectedColumn introspectedColumn){
        return isHiddenJavaProperty(introspectedColumn.getJavaProperty());
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

}
