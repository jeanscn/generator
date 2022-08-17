package org.mybatis.generator.codegen.mybatis3.controller.elements;

import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;

import static org.mybatis.generator.custom.ConstantsUtil.SERVICE_RESULT;

public class UpdateElementGenerator extends AbstractControllerElementGenerator {

    public UpdateElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(SERVICE_RESULT);
        parentElement.addImportedType(entityType);
        if (isGenerateVoModel()) {
            parentElement.addImportedType(entityVoType);
            parentElement.addImportedType(entityMappings);
        }
        parentElement.addImportedType("javax.validation.Valid");

        final String methodPrefix = "update";
        Method method = createMethod(methodPrefix);
        addSystemLogAnnotation(method,parentElement);
        method.addParameter(buildMethodParameter(true,true));
        method.setReturnType(getResponseResult(false));
        addControllerMapping(method, "", "put");
        method.addBodyLine("ServiceResult<{0}> serviceResult = {1}.updateByPrimaryKeySelective({2});"
                ,entityType.getShortName()
                ,serviceBeanName
                , isGenerateVoModel()
                        ?"mappings.from"+entityVoType.getShortName()+"("+entityVoType.getShortNameFirstLowCase()+")"
                        :entityType.getShortNameFirstLowCase());
        method.addBodyLine("if (serviceResult.isSuccess()) {");
        method.addBodyLine("return success({0});"
                , isGenerateVoModel()?"mappings.to"+entityVoType.getShortName() +"(serviceResult.getResult())":"serviceResult.getResult()");
        method.addBodyLine("}else{");
        method.addBodyLine("return failure(ApiCodeEnum.FAIL_OPERATION);");
        method.addBodyLine("}");
        parentElement.addMethod(method);
    }
}
