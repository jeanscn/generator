package org.mybatis.generator.codegen.mybatis3.controller.elements;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.ReturnTypeEnum;

import static org.mybatis.generator.custom.ConstantsUtil.SERVICE_RESULT;

public class UpdateBatchElementGenerator extends AbstractControllerElementGenerator {

    public UpdateBatchElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(SERVICE_RESULT);
        if (introspectedTable.getRules().isGenerateVoModel()) {
            parentElement.addImportedType(entityVoType);
            parentElement.addImportedType(entityMappings);
        } else {
            parentElement.addImportedType(entityType);
        }

        final String methodPrefix = "updateBatch";
        Method method = createMethod(methodPrefix);
        addSystemLogAnnotation(method, parentElement);
        MethodParameterDescript descript = new MethodParameterDescript(parentElement, "put");
        descript.setValid(true);
        descript.setRequestBody(true);
        descript.setList(true);
        method.addParameter(buildMethodParameter(descript));

        method.setReturnType(getResponseResult(ReturnTypeEnum.RESPONSE_RESULT_LIST,
                getMethodParameterVOType(""),
                parentElement));
        addControllerMapping(method, "batch", "put");
        addSecurityPreAuthorize(method, methodPrefix, "批量更新");

        method.addBodyLine("ServiceResult<List<{0}>> result =  {1}.{2}({3});"
                ,entityType.getShortName()
                , serviceBeanName
                ,introspectedTable.getUpdateBatchStatementId()
                , descript.getReturnFqt().getFullyQualifiedName().equals(entityType.getFullyQualifiedName())
                        ? entityType.getShortNameFirstLowCase() + "s"
                        : "mappings.from" + descript.getReturnFqt().getShortName() + "s(" + descript.getReturnFqt().getShortNameFirstLowCase() + "s)"
                );
        method.addBodyLine("if (result.isSuccess()) {");
        if (introspectedTable.getRules().isGenerateVoModel()) {
            method.addBodyLine("return success(mappings.to{0}VOs(result.getResult()),result.getAffectedRows());"
                    ,entityType.getShortName());
        }else{
            method.addBodyLine("return success(result.getResult(),result.getAffectedRows());");
        }
        method.addBodyLine("}else{");
        method.addBodyLine("return failure(ApiCodeEnum.FAIL_OPERATION);");
        method.addBodyLine("}");

        parentElement.addMethod(method);
    }
}
