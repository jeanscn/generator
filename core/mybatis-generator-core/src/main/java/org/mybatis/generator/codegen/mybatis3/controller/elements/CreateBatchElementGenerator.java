package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethod;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.ReturnTypeEnum;
import org.mybatis.generator.custom.annotations.ApiOperation;
import org.mybatis.generator.custom.annotations.RequestMapping;
import org.mybatis.generator.custom.annotations.SystemLog;

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
        } else if (introspectedTable.getRules().isGenerateVoModel()) {
            parentElement.addImportedType(entityVoType);
            parentElement.addImportedType(entityMappings);
        }

        final String methodPrefix = "createBatch";
        Method method = createMethod(methodPrefix);

        MethodParameterDescript descript = new MethodParameterDescript(parentElement, "post");
        descript.setValid(true);
        descript.setRequestBody(true);
        descript.setList(true);
        Parameter parameter = buildMethodParameter(descript);
        parameter.setRemark("接收请求待持久化的数据（对象）列表");
        method.addParameter(parameter);
        method.setReturnType(getResponseResult(ReturnTypeEnum.RESPONSE_RESULT_LIST, getMethodParameterVOType(""), parentElement));
        method.setReturnRemark("更新后的数据（对象）列表");

        method.addAnnotation(new SystemLog("添加了多条记录",introspectedTable),parentElement);
        method.addAnnotation(new RequestMapping("batch", RequestMethod.POST),parentElement);
        addSecurityPreAuthorize(method, methodPrefix, "批量创建");
        method.addAnnotation(new ApiOperation("新增多条记录", "新增多条记录,返回影响条数及消息"),parentElement);

        commentGenerator.addMethodJavaDocLine(method, "新增多条记录");

        method.addBodyLine("ServiceResult<List<{0}>> serviceResult = {1}.{2}({3});"
                , entityType.getShortName()
                , serviceBeanName
                , introspectedTable.getInsertBatchStatementId()
                , getServiceMethodEntityParameter(true, "create"));
        method.addBodyLine("if (!serviceResult.isSuccess()) {");
        method.addBodyLine("return failure(ApiCodeEnum.FAIL_OPERATION);");
        method.addBodyLine("}else{");
        if (introspectedTable.getRules().isGenerateVoModel()) {
            method.addBodyLine("return success(mappings.to{0}VOs(serviceResult.getResult()),serviceResult.getAffectedRows());",
                    entityType.getShortName());
        } else {
            method.addBodyLine("return success(serviceResult.getResult(),serviceResult.getAffectedRows());");
        }
        method.addBodyLine("}");

        parentElement.addMethod(method);
    }
}
