package org.mybatis.generator.codegen.mybatis3.controller.elements;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.ReturnTypeEnum;

import static org.mybatis.generator.custom.ConstantsUtil.SERVICE_RESULT;

public class CreateElementGenerator extends AbstractControllerElementGenerator {

    public CreateElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(SERVICE_RESULT);
        parentElement.addImportedType(entityType);
        if (introspectedTable.getRules().isGenerateCreateVO()) {
            parentElement.addImportedType(entityCreateVoType);
            parentElement.addImportedType(entityMappings);
        } else if (introspectedTable.getRules().isGenerateVoModel()) {
            parentElement.addImportedType(entityVoType);
            parentElement.addImportedType(entityMappings);
        }

        final String methodPrefix = "create";
        Method method = createMethod(methodPrefix);
        addSystemLogAnnotation(method, parentElement);

        MethodParameterDescript descript = new MethodParameterDescript(parentElement,"post");
        descript.setValid(true);
        descript.setRequestBody(true);
        method.addParameter(buildMethodParameter(descript));

        method.setReturnType(getResponseResult(ReturnTypeEnum.MODEL,
                introspectedTable.getRules().isGenerateVoModel() ? entityVoType : entityType,
                parentElement));

        addControllerMapping(method, "", "post");
        addSecurityPreAuthorize(method, methodPrefix, "创建");

        if (introspectedTable.getRules().isGenerateCreateVO()) {
            method.addBodyLine("ServiceResult<{0}> serviceResult;", entityType.getShortName());
            method.addBodyLine("if ({0}CreateVO.isSelectiveUpdate()) '{'", entityType.getShortNameFirstLowCase());
            method.addBodyLine("serviceResult = {0}.insertOrUpdate({1});"
                    , serviceBeanName
                    , introspectedTable.getRules().isGenerateCreateVO()
                            ? "mappings.from" + entityCreateVoType.getShortName() + "(" + entityCreateVoType.getShortNameFirstLowCase() + ")" :
                            introspectedTable.getRules().isGenerateVoModel()
                                    ? "mappings.from" + entityVoType.getShortName() + "(" + entityVoType.getShortNameFirstLowCase() + ")"
                                    : entityType.getShortNameFirstLowCase());
            method.addBodyLine("}else{");
            method.addBodyLine("serviceResult = {1}.insert({2});"
                    , entityType.getShortName()
                    , serviceBeanName
                    , introspectedTable.getRules().isGenerateCreateVO()
                            ? "mappings.from" + entityCreateVoType.getShortName() + "(" + entityCreateVoType.getShortNameFirstLowCase() + ")" :
                            introspectedTable.getRules().isGenerateVoModel()
                                    ? "mappings.from" + entityVoType.getShortName() + "(" + entityVoType.getShortNameFirstLowCase() + ")"
                                    : entityType.getShortNameFirstLowCase());
            method.addBodyLine("}");
        } else {
            method.addBodyLine("ServiceResult<{0}> serviceResult = {1}.insert({2});"
                    , entityType.getShortName()
                    , serviceBeanName
                    , introspectedTable.getRules().isGenerateCreateVO()
                            ? "mappings.from" + entityCreateVoType.getShortName() + "(" + entityCreateVoType.getShortNameFirstLowCase() + ")" :
                            introspectedTable.getRules().isGenerateVoModel()
                                    ? "mappings.from" + entityVoType.getShortName() + "(" + entityVoType.getShortNameFirstLowCase() + ")"
                                    : entityType.getShortNameFirstLowCase());
        }
        method.addBodyLine("if (!serviceResult.isSuccess()) {");
        method.addBodyLine("return failure(ApiCodeEnum.FAIL_OPERATION);");
        method.addBodyLine("}else{");
        method.addBodyLine("return success({0},serviceResult.getAffectedRows());"
                , introspectedTable.getRules().isGenerateVoModel() ? "mappings.to" + entityVoType.getShortName() + "(serviceResult.getResult())" : "serviceResult.getResult()");
        method.addBodyLine("}");

        parentElement.addMethod(method);
    }


}
