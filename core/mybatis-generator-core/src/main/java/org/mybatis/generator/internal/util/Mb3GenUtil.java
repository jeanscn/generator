package org.mybatis.generator.internal.util;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.internal.util.StringUtility;

import static com.vgosoft.tool.core.VStringUtil.toHyphenCase;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-03 14:53
 * @version 3.0
 */
public class Mb3GenUtil {

    public static String getControllerBaseMappingPath(IntrospectedTable introspectedTable){
        String basePath = introspectedTable.getTableConfiguration().getServiceApiBasePath();
        if (StringUtility.stringHasValue(basePath)) {
            return basePath + "/" + toHyphenCase(introspectedTable.getControllerBeanName());
        }else {
            return toHyphenCase(introspectedTable.getControllerBeanName());
        }
    }

}
