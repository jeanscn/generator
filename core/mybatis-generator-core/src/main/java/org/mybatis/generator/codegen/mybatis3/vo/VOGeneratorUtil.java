package org.mybatis.generator.codegen.mybatis3.vo;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.internal.util.JavaBeansUtil;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-03-29 18:39
 * @version 3.0
 */
public class VOGeneratorUtil {

    public static Method addMappingMethod(FullyQualifiedJavaType fromType, FullyQualifiedJavaType toType, boolean isList, IntrospectedTable introspectedTable) {
        String methodName;
        Method method;
        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        if (entityType.getFullyQualifiedName().equalsIgnoreCase(toType.getFullyQualifiedName())) {
            methodName = "from" + fromType.getShortNameWithoutTypeArguments();
        } else if (entityType.getFullyQualifiedName().equalsIgnoreCase(fromType.getFullyQualifiedName())) {
            methodName = "to" + toType.getShortNameWithoutTypeArguments();
        } else {
            methodName = "from" + fromType.getShortNameWithoutTypeArguments() + "To" + toType.getShortNameWithoutTypeArguments();
        }
        if (isList) {
            methodName = methodName + "s";
            method = new Method(methodName);
            FullyQualifiedJavaType listInstanceFrom = FullyQualifiedJavaType.getNewListInstance();
            listInstanceFrom.addTypeArgument(fromType);
            method.addParameter(new Parameter(listInstanceFrom, JavaBeansUtil.getFirstCharacterLowercase(fromType.getShortNameWithoutTypeArguments()) + "s"));
            FullyQualifiedJavaType listInstanceTo = FullyQualifiedJavaType.getNewListInstance();
            listInstanceTo.addTypeArgument(toType);
            method.setReturnType(listInstanceTo);
        } else {
            method = new Method(methodName);
            method.addParameter(new Parameter(fromType, JavaBeansUtil.getFirstCharacterLowercase(fromType.getShortNameWithoutTypeArguments())));
            method.setReturnType(toType);
        }
        method.setAbstract(true);
        return method;
    }

}
