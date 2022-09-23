package org.mybatis.generator.codegen.mybatis3.controller.elements;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.ReturnTypeEnum;

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
        parentElement.addImportedType(responsePagehelperResult);
        if (introspectedTable.getRules().isGenerateVoModel()) {
            parentElement.addImportedType(entityVoType);
            parentElement.addImportedType(entityMappings);
        }

        final String methodPrefix = "list";
        Method method = createMethod(methodPrefix);
        addSystemLogAnnotation(method, parentElement);
        MethodParameterDescript descript = new MethodParameterDescript(parentElement,"get");
        method.addParameter(buildMethodParameter(descript));
        Parameter actionType = new Parameter(FullyQualifiedJavaType.getStringInstance(), "actionType");
        actionType.addAnnotation("@RequestParam(required = false)");
        method.addParameter(actionType);
        method.setReturnType(getResponseResult(ReturnTypeEnum.RESPONSE_RESULT_LIST,
                introspectedTable.getRules().isGenerateVoModel()?entityVoType:entityType,
                parentElement));
        addControllerMapping(method, "", "get");
        addSecurityPreAuthorize(method,methodPrefix,"数据列表");

        String listEntityVar = entityType.getShortNameFirstLowCase() + "s";
        selectByExampleWithPagehelper(parentElement, method);
        if (introspectedTable.getRules().isGenerateVoModel()) {
            method.addBodyLine("return ResponsePagehelperResult.success(mappings.to{0}s({1}),page);",entityVoType.getShortName(),listEntityVar);
        }else{
            method.addBodyLine("return ResponsePagehelperResult.success({0},page);",listEntityVar);
        }

        parentElement.addMethod(method);
    }


}
