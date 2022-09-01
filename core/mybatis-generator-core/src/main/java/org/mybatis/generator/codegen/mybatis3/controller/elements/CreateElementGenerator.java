package org.mybatis.generator.codegen.mybatis3.controller.elements;

import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;

import static org.mybatis.generator.custom.ConstantsUtil.SERVICE_RESULT;

public class CreateElementGenerator extends AbstractControllerElementGenerator {

    public CreateElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(SERVICE_RESULT);
        parentElement.addImportedType(entityType);
        if (introspectedTable.getRules().isGenerateVoModel()) {
            parentElement.addImportedType(entityVoType);
            parentElement.addImportedType(entityMappings);
        }
        parentElement.addImportedType("javax.validation.Valid");

        final String methodPrefix = "create";
        Method method = createMethod(methodPrefix);
        method.addParameter(buildMethodParameter(true, true,parentElement));
        method.setReturnType(getResponseResult(false));
        addSystemLogAnnotation(method, parentElement);
        addControllerMapping(method, "", "post");
        method.addBodyLine("ServiceResult<{0}> serviceResult = {1}.insert({2});"
                , entityType.getShortName()
                , serviceBeanName
                , introspectedTable.getRules().isGenerateVoModel()
                        ? "mappings.from" + entityVoType.getShortName() + "(" + entityVoType.getShortNameFirstLowCase() + ")"
                        : entityType.getShortNameFirstLowCase());
        method.addBodyLine("if (!serviceResult.isSuccess()) {");
        method.addBodyLine("return failure(ApiCodeEnum.FAIL_OPERATION);");
        method.addBodyLine("}else{");
        method.addBodyLine("return success({0});"
                , introspectedTable.getRules().isGenerateVoModel() ? "mappings.to" + entityVoType.getShortName() + "(serviceResult.getResult())" : "serviceResult.getResult()");
        method.addBodyLine("}");

        parentElement.addMethod(method);
    }
}
