package org.mybatis.generator.codegen.mybatis3.controller.elements;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;

public class ListElementGenerator extends AbstractControllerElementGenerator {

    public ListElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        parentElement.addImportedType(entityType);
        parentElement.addImportedType(exampleType);
        parentElement.addImportedType(responseResult);
        if (isGenerateVoModel()) {
            parentElement.addImportedType(entityVoType);
            parentElement.addImportedType(entityMappings);
        }

        final String methodPrefix = "list";
        Method method = createMethod(methodPrefix);
        addSystemLogAnnotation(method,parentElement);
        method.addParameter(buildMethodParameter(false,false));
        method.setReturnType(getResponseResult(true));
        addControllerMapping(method, "", "get");

        String listEntityVar = entityFirstLowerShortName+"s";
        method.addBodyLine("{0} example = new {0}();"
                ,exampleType.getShortName());
        method.addBodyLine("List<{0}> {1} = {2}.selectByExample(example);",
                entityType.getShortName(),listEntityVar,serviceBeanName);
        method.addBodyLine("return success({0});",
                isGenerateVoModel()
                        ?"mappings.to"+entityVoType.getShortName()+"s("+listEntityVar+")"
                        :listEntityVar);
        parentElement.addMethod(method);
    }
}
