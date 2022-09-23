package org.mybatis.generator.codegen.mybatis3.controller.elements;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.ReturnTypeEnum;

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

        final String methodPrefix = "createBatch";
        Method method = createMethod(methodPrefix);
        addSystemLogAnnotation(method, parentElement);
        MethodParameterDescript descript = new MethodParameterDescript(parentElement,"post");
        descript.setValid(true);
        descript.setRequestBody(true);
        descript.setList(true);
        method.addParameter(buildMethodParameter(descript));
        method.setReturnType(getResponseResult(ReturnTypeEnum.RESPONSE_RESULT_LIST,getMethodParameterVOType(""),parentElement));

        addControllerMapping(method, "batch", "post");
        addSecurityPreAuthorize(method,methodPrefix,"批量创建");

        method.addBodyLine("ServiceResult<List<{0}>> serviceResult = {1}.{2}({3});"
                ,entityType.getShortName()
                , serviceBeanName
                ,introspectedTable.getInsertBatchStatementId()
                , introspectedTable.getRules().isGenerateCreateVO()
                        ?"mappings.from" + entityCreateVoType.getShortName() + "s(" + entityCreateVoType.getShortNameFirstLowCase() + "s)":
                        introspectedTable.getRules().isGenerateVoModel()
                                ? "mappings.from" + entityVoType.getShortName() + "s(" + entityVoType.getShortNameFirstLowCase() + "s)"
                                : entityType.getShortNameFirstLowCase()+"s"
                );
        method.addBodyLine("if (!serviceResult.isSuccess()) {");
        method.addBodyLine("return failure(ApiCodeEnum.FAIL_OPERATION);");
        method.addBodyLine("}else{");
        if (introspectedTable.getRules().isGenerateVoModel()) {
            method.addBodyLine("return success(mappings.to{0}VOs(serviceResult.getResult()),serviceResult.getAffectedRows());",
                    entityType.getShortName());
        }else{
            method.addBodyLine("return success(serviceResult.getResult(),serviceResult.getAffectedRows());");
        }
        method.addBodyLine("}");

        parentElement.addMethod(method);
    }
}
