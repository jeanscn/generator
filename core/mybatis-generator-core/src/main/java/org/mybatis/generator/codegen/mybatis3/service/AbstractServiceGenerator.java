package org.mybatis.generator.codegen.mybatis3.service;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;

import java.util.List;

public abstract class AbstractServiceGenerator extends AbstractJavaGenerator {

    public AbstractServiceGenerator(String project) {
        super(project);
    }

    @Override
    public abstract List<CompilationUnit> getCompilationUnits();

    protected String getInterfaceClassShortName(String targetPackage,String entityTypeShortName){
        StringBuilder sb = new StringBuilder();
        sb.append(targetPackage);
        sb.append(".").append("I").append(entityTypeShortName);
        return sb.toString();
    }

    protected Method getMethodByColumn(FullyQualifiedJavaType returnType, IntrospectedColumn parameterColumn, String methodName, boolean isAbstract) {
        return getMethodByType(methodName, returnType, parameterColumn.getFullyQualifiedJavaType(),
                parameterColumn.getJavaProperty(), isAbstract, parameterColumn.getRemarks());
    }

    protected Method getMethodByType(String methodName, FullyQualifiedJavaType returnType, FullyQualifiedJavaType parameterFullyQualifiedJavaType, String parameterName, boolean isAbstract, String remark) {
        Method method = new Method(methodName);
        if (isAbstract) {
            method.setAbstract(true);
        } else {
            method.setVisibility(JavaVisibility.PUBLIC);
        }
        method.addParameter(new Parameter(parameterFullyQualifiedJavaType, parameterName));
        if (methodName.equals("selectBaseByPrimaryKey")) {
            method.setReturnType(returnType);
        }else{
            FullyQualifiedJavaType listType = FullyQualifiedJavaType.getNewListInstance();
            listType.addTypeArgument(returnType);
            method.setReturnType(listType);
        }
        context.getCommentGenerator().addMethodJavaDocLine(method, false, "提示 - @mbg.generated",
                "这个抽象方法通过定制版Mybatis Generator自动生成",
                VStringUtil.format("@param {0} {1}", parameterName, remark));
        return method;
    }
}
