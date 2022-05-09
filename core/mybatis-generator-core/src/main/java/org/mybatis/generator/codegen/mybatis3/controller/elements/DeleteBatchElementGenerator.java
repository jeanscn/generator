package org.mybatis.generator.codegen.mybatis3.controller.elements;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;

import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_SIMPLE;
import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_SIMPLE_IMPL;

public class DeleteBatchElementGenerator extends AbstractControllerElementGenerator {

    public DeleteBatchElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(RESPONSE_SIMPLE);
        parentElement.addImportedType(RESPONSE_SIMPLE_IMPL);
        parentElement.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        parentElement.addImportedType(entityType);
        parentElement.addImportedType(exampleType);

        final String methodPrefix = "deleteBatch";
        Method method = createMethod(methodPrefix);
        addSystemLogAnnotation(method,parentElement);

        Parameter parameter = new Parameter(new FullyQualifiedJavaType("List<String>"), "ids");
        parameter.addAnnotation("@RequestBody");
        method.addParameter(parameter);
        method.setReturnType(responseSimple);
        addControllerMapping(method, null, "delete");
        method.addBodyLine("ResponseSimple responseSimple = new ResponseSimpleImpl();");
        StringBuilder sb = new StringBuilder();
        sb.append(exampleType.getShortName()).append(" example = new ");
        sb.append(exampleType.getShortName()).append("();");
        method.addBodyLine(sb.toString());
        method.addBodyLine("example.createCriteria().andIdIn(ids);");
        method.addBodyLine("try {");
        sb.setLength(0);
        sb.append("int rows =  ").append(serviceBeanName).append(".deleteByExample(example);");
        method.addBodyLine(sb.toString());
        sb.setLength(0);
        sb.append("responseSimple.addAttribute(\"rows\", String.valueOf(rows));");
        method.addBodyLine(sb.toString());
        addExceptionAndReturn(method);
        parentElement.addMethod(method);
    }
}
