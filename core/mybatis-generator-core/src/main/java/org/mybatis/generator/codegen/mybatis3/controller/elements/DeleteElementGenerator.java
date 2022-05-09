package org.mybatis.generator.codegen.mybatis3.controller.elements;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;

import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_SIMPLE;
import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_SIMPLE_IMPL;

public class DeleteElementGenerator extends AbstractControllerElementGenerator {

    public DeleteElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(RESPONSE_SIMPLE);
        parentElement.addImportedType(RESPONSE_SIMPLE_IMPL);
        parentElement.addImportedType(entityType);

        final String methodPrefix = "delete";
        Method method = createMethod(methodPrefix);
        addSystemLogAnnotation(method,parentElement);

        StringBuilder sb = new StringBuilder();

        Parameter parameter = new Parameter(FullyQualifiedJavaType.getStringInstance(), "id");
        parameter.addAnnotation("@PathVariable");
        method.addParameter(parameter);
        method.setReturnType(responseSimple);
        addControllerMapping(method, "{id}", "delete");
        method.addBodyLine("ResponseSimple responseSimple = new ResponseSimpleImpl();");
        method.addBodyLine("try {");
        sb.setLength(0);
        sb.append("int rows =  ").append(serviceBeanName).append(".deleteByPrimaryKey(id);");
        method.addBodyLine(sb.toString());
        sb.setLength(0);
        sb.append("responseSimple.addAttribute(\"rows\", String.valueOf(rows));");
        method.addBodyLine(sb.toString());
        addExceptionAndReturn(method);

        parentElement.addMethod(method);
    }
}
