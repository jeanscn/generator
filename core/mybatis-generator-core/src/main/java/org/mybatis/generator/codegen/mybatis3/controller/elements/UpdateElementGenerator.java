package org.mybatis.generator.codegen.mybatis3.controller.elements;

import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.ReturnTypeEnum;

import static org.mybatis.generator.custom.ConstantsUtil.SERVICE_RESULT;

public class UpdateElementGenerator extends AbstractControllerElementGenerator {

    public UpdateElementGenerator() {
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

        final String methodPrefix = "update";
        Method method = createMethod(methodPrefix);
        addSystemLogAnnotation(method,parentElement);
        MethodParameterDescript descript = new MethodParameterDescript(parentElement,"put");
        descript.setValid(true);
        descript.setRequestBody(true);
        method.addParameter(buildMethodParameter(descript));
        method.setReturnType(getResponseResult(ReturnTypeEnum.RESPONSE_RESULT_MODEL,
                introspectedTable.getRules().isGenerateVoModel()?entityVoType:entityType,
                parentElement));
        addControllerMapping(method, "", "put");
        addSecurityPreAuthorize(method,methodPrefix,"更新");

        method.addBodyLine("ServiceResult<{0}> serviceResult = {1}.updateByPrimaryKeySelective({2});"
                ,entityType.getShortName()
                ,serviceBeanName
                ,descript.getReturnFqt().getFullyQualifiedName().equals(entityType.getFullyQualifiedName())
                        ?entityType.getShortNameFirstLowCase()
                        :"mappings.from"+descript.getReturnFqt().getShortName()+"("+descript.getReturnFqt().getShortNameFirstLowCase()+")");
        method.addBodyLine("if (serviceResult.isSuccess()) {");
        method.addBodyLine("return success({0},serviceResult.getAffectedRows());"
                , introspectedTable.getRules().isGenerateVoModel()?"mappings.to"+entityVoType.getShortName() +"(serviceResult.getResult())":"serviceResult.getResult()");
        method.addBodyLine("}else{");
        method.addBodyLine("return failure(ApiCodeEnum.FAIL_OPERATION);");
        method.addBodyLine("}");

        parentElement.addMethod(method);
    }
}
