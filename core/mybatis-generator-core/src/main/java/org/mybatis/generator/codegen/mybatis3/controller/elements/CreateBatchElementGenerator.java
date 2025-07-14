package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethodEnum;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.enums.ReturnTypeEnum;
import org.mybatis.generator.custom.annotations.ApiOperationDesc;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;
import org.mybatis.generator.custom.annotations.SystemLogDesc;

import static org.mybatis.generator.custom.ConstantsUtil.SERVICE_RESULT;

public class CreateBatchElementGenerator extends AbstractControllerElementGenerator {

    public CreateBatchElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(SERVICE_RESULT);
        parentElement.addImportedType(entityType);
        if (introspectedTable.getRules().isGenerateCreateVo()) {
            parentElement.addImportedType(entityCreateVoType);
            parentElement.addImportedType(entityMappings);
        } else if (introspectedTable.getRules().isGenerateVoModel()) {
            parentElement.addImportedType(entityVoType);
            parentElement.addImportedType(entityMappings);
        }

        final String methodPrefix = "createBatch";
        Method method = createMethod(methodPrefix);

        MethodParameterDescriptor descriptor = new MethodParameterDescriptor(parentElement, "post");
        descriptor.setValid(true);
        descriptor.setRequestBody(true);
        descriptor.setList(true);
        Parameter parameter = buildMethodParameter(descriptor);
        parameter.setRemark("接收请求待持久化的数据（对象）列表");
        method.addParameter(parameter);
        method.setReturnType(getResponseResult(ReturnTypeEnum.RESPONSE_RESULT_LIST, getMethodParameterVoType(""), parentElement));
        method.setReturnRemark("更新后的数据（对象）列表");

        method.addAnnotation(new SystemLogDesc("添加了多条记录",introspectedTable),parentElement);
        method.addAnnotation(new RequestMappingDesc("batch", RequestMethodEnum.POST),parentElement);
        addSecurityPreAuthorize(method, methodPrefix, "批量创建");
        method.addAnnotation(new ApiOperationDesc("新增多条记录", "新增多条记录,返回影响条数及消息"),parentElement);

        commentGenerator.addMethodJavaDocLine(method, "新增多条记录");

        method.addBodyLine("ServiceResult<List<{0}>> serviceResult = {1}.{2}({3});"
                , entityType.getShortName()
                , serviceBeanName
                , introspectedTable.getInsertBatchStatementId()
                , getServiceMethodEntityParameter(true, "create"));
        method.addBodyLine("if (serviceResult.hasResult()) {");
        if (introspectedTable.getRules().isGenerateVoModel()) {
            method.addBodyLine("return success(mappings.to{0}Vos(serviceResult.getResult()),serviceResult.getAffectedRows());",
                    entityType.getShortName());
        } else {
            method.addBodyLine("return success(serviceResult.getResult(),serviceResult.getAffectedRows());");
        }
        method.addBodyLine("}else{");
        method.addBodyLine("return failure(ApiCodeEnum.FAIL_CUSTOM,serviceResult.getMessage());");
        method.addBodyLine("}");

        parentElement.addMethod(method);
    }
}
