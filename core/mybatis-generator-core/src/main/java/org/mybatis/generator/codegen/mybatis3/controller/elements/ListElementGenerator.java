package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;

import static org.mybatis.generator.custom.ConstantsUtil.*;

public class ListElementGenerator extends AbstractControllerElementGenerator {

    public ListElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(RESPONSE_SIMPLE);
        parentElement.addImportedType(RESPONSE_LIST);
        parentElement.addImportedType(RESPONSE_SIMPLE_LIST);
        parentElement.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        parentElement.addImportedType(entityType);
        parentElement.addImportedType(exampleType);

        final String methodPrefix = "list";
        Method method = createMethod(methodPrefix);
        addSystemLogAnnotation(method,parentElement);
        StringBuilder sb = new StringBuilder();

        method.addParameter(new Parameter(entityType, entityFirstLowerShortName));
        method.setReturnType(responseSimple);
        addControllerMapping(method, "", "get");
        method.addBodyLine("ResponseList responseSimple = new ResponseSimpleList();");
        sb.setLength(0);
        sb.append(exampleType.getShortName()).append(" example = new ");
        sb.append(exampleType.getShortName()).append("();");
        method.addBodyLine(sb.toString());
        method.addBodyLine("try {");
        sb.setLength(0);
        sb.append("List<").append(entityType.getShortName()).append("> ");
        sb.append(entityFirstLowerShortName).append("s");
        sb.append(" = ").append(serviceBeanName).append(".selectByExample(example);");
        method.addBodyLine(sb.toString());
        method.addBodyLine(VStringUtil.format("responseSimple.setList(mappings.to{0}s({1}s));",
                entityVoType.getShortName(),entityType.getShortNameFirstLowCase()));
        addExceptionAndReturn(method);

        parentElement.addMethod(method);
        parentElement.addImportedType(entityVoType);
        parentElement.addImportedType(entityMappings);
    }
}
