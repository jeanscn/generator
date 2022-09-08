package org.mybatis.generator.codegen.mybatis3.controller.elements;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;

import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_RESULT;
import static org.mybatis.generator.custom.ConstantsUtil.SERVICE_RESULT;

public class CreateBatchElementGenerator extends AbstractControllerElementGenerator {

    public CreateBatchElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(SERVICE_RESULT);
        parentElement.addImportedType(entityType);
        if (introspectedTable.getRules().isGenerateCreateVO()) {
            parentElement.addImportedType(entityCreateVoType);
            parentElement.addImportedType(entityMappings);
        }else if (introspectedTable.getRules().isGenerateVoModel()) {
            parentElement.addImportedType(entityVoType);
            parentElement.addImportedType(entityMappings);
        }
        parentElement.addImportedType("javax.validation.Valid");

        final String methodPrefix = "createBatch";
        Method method = createMethod(methodPrefix);
        FullyQualifiedJavaType listInstance = FullyQualifiedJavaType.getNewListInstance();
        FullyQualifiedJavaType parameterType = introspectedTable.getRules().isGenerateCreateVO()?
                entityCreateVoType:introspectedTable.getRules().isGenerateVoModel()?entityVoType:entityType;
        listInstance.addTypeArgument(parameterType);
        Parameter parameter = new Parameter(listInstance, parameterType.getShortNameFirstLowCase()+"s");
        parameter.addAnnotation("@Valid");
        parentElement.addImportedType("javax.validation.Valid");
        parameter.addAnnotation("@RequestBody");
        parentElement.addImportedType("org.springframework.web.bind.annotation.RequestBody");
        method.addParameter(parameter);

        FullyQualifiedJavaType response = new FullyQualifiedJavaType(RESPONSE_RESULT);
        response.addTypeArgument(new FullyQualifiedJavaType("java.lang.Integer"));
        method.setReturnType(response);
        addSystemLogAnnotation(method, parentElement);
        addControllerMapping(method, "batch", "post");
        addSecurityPreAuthorize(method,methodPrefix,"批量创建");
        method.addBodyLine("ServiceResult<Integer> serviceResult = {0}.insertBatch({1});"
                , serviceBeanName
                , introspectedTable.getRules().isGenerateCreateVO()
                        ?"mappings.from" + entityCreateVoType.getShortName() + "s(" + entityCreateVoType.getShortNameFirstLowCase() + "s)":
                        introspectedTable.getRules().isGenerateVoModel()
                                ? "mappings.from" + entityVoType.getShortName() + "s(" + entityVoType.getShortNameFirstLowCase() + "s)"
                                : entityType.getShortNameFirstLowCase()+"s");
        method.addBodyLine("if (!serviceResult.isSuccess()) {");
        method.addBodyLine("return failure(ApiCodeEnum.FAIL_OPERATION);");
        method.addBodyLine("}else{");
        method.addBodyLine("return success(serviceResult.getResult());");
        method.addBodyLine("}");

        parentElement.addMethod(method);
    }
}
