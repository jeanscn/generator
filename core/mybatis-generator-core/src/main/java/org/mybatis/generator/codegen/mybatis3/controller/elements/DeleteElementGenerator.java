package org.mybatis.generator.codegen.mybatis3.controller.elements;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;

import static org.mybatis.generator.custom.ConstantsUtil.*;

public class DeleteElementGenerator extends AbstractControllerElementGenerator {

    public DeleteElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(entityType);
        final String methodPrefix = "delete";
        Method method = createMethod(methodPrefix);
        addSystemLogAnnotation(method,parentElement);
        Parameter parameter = new Parameter(FullyQualifiedJavaType.getStringInstance(), "id");
        parameter.addAnnotation("@PathVariable");
        method.addParameter(parameter);
        FullyQualifiedJavaType response = new FullyQualifiedJavaType(RESPONSE_RESULT);
        response.addTypeArgument(new FullyQualifiedJavaType("java.lang.Long"));
        method.setReturnType(response);
        addControllerMapping(method, "{id}", "delete");
        method.addBodyLine("int rows =  {0}.deleteByPrimaryKey(id);",serviceBeanName);
        method.addBodyLine("if (rows > 0) {");
        method.addBodyLine("return success((long) rows);");
        method.addBodyLine("} else {");
        method.addBodyLine("return failure(ApiCodeEnum.FAIL_OPERATION);");
        method.addBodyLine("}");

        parentElement.addMethod(method);
    }
}
