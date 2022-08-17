package org.mybatis.generator.codegen.mybatis3.controller.elements;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;

import static org.mybatis.generator.custom.ConstantsUtil.*;

public class DeleteBatchElementGenerator extends AbstractControllerElementGenerator {

    public DeleteBatchElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        parentElement.addImportedType(entityType);
        parentElement.addImportedType(exampleType);
        final String methodPrefix = "deleteBatch";
        Method method = createMethod(methodPrefix);
        addSystemLogAnnotation(method,parentElement);
        Parameter parameter = new Parameter(new FullyQualifiedJavaType("List<String>"), "ids");
        parameter.addAnnotation("@RequestBody");
        method.addParameter(parameter);
        FullyQualifiedJavaType response = new FullyQualifiedJavaType(RESPONSE_RESULT);
        response.addTypeArgument(new FullyQualifiedJavaType("java.lang.Long"));
        method.setReturnType(response);
        addControllerMapping(method, null, "delete");
        method.addBodyLine("{0} example = new {0}();", exampleType.getShortName());
        method.addBodyLine("example.createCriteria().andIdIn(ids);");
        method.addBodyLine("int rows =  {0}.deleteByExample(example);",serviceBeanName);
        method.addBodyLine("if (rows > 0) {");
        method.addBodyLine("return success((long) rows);");
        method.addBodyLine("} else {");
        method.addBodyLine("return failure(ApiCodeEnum.FAIL_OPERATION);");
        method.addBodyLine("}");
        parentElement.addMethod(method);
    }
}
