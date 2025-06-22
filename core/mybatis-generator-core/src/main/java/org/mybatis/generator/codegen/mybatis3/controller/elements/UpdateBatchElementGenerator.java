package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethodEnum;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.ReturnTypeEnum;
import org.mybatis.generator.custom.annotations.ApiOperationDesc;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;
import org.mybatis.generator.custom.annotations.SystemLogDesc;

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

        MethodParameterDescriptor descriptor = new MethodParameterDescriptor(parentElement, "put");
        descriptor.setValid(true);
        descriptor.setRequestBody(true);
        descriptor.setList(true);
        Parameter parameter = buildMethodParameter(descriptor);
        parameter.setRemark("待更新的数据对象列表");
        method.addParameter(parameter);
        method.setReturnType(getResponseResult(ReturnTypeEnum.RESPONSE_RESULT_LIST,
                getMethodParameterVOType(""),
                parentElement));
        method.setReturnRemark("更新后的数据对象列表");

        method.addAnnotation(new SystemLogDesc("更新了多条记录",introspectedTable),parentElement);
        method.addAnnotation(new RequestMappingDesc("batch", RequestMethodEnum.PUT),parentElement);
        addSecurityPreAuthorize(method, methodPrefix, "批量更新");
        method.addAnnotation(new ApiOperationDesc("批量更新数据", "根据主键批量更新数据"),parentElement);

        commentGenerator.addMethodJavaDocLine(method, "根据主键批量更新实体对象");

        method.addBodyLine("ServiceResult<List<{0}>> result =  {1}.{2}({3});"
                , entityType.getShortName()
                , serviceBeanName
                , introspectedTable.getUpdateBatchStatementId()
                , getServiceMethodEntityParameter(true, "update"));
        method.addBodyLine("if (result.hasResult()) {");
        if (introspectedTable.getRules().isGenerateVoModel()) {
            method.addBodyLine("return success(mappings.to{0}VOs(result.getResult()),result.getAffectedRows());"
                    , entityType.getShortName());
        } else {
            method.addBodyLine("return success(result.getResult(),result.getAffectedRows());");
        }
        method.addBodyLine("}else{");
        method.addBodyLine("return failure(ApiCodeEnum.FAIL_CUSTOM,result.getMessage());");
        method.addBodyLine("}");

        parentElement.addMethod(method);
    }
}
