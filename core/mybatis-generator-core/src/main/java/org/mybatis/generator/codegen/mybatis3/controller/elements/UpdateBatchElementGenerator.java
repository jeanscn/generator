package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.RequestMethod;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.ReturnTypeEnum;
import org.mybatis.generator.custom.annotations.ApiOperation;
import org.mybatis.generator.custom.annotations.RequestMapping;
import org.mybatis.generator.custom.annotations.SystemLog;

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

        MethodParameterDescript descript = new MethodParameterDescript(parentElement, "put");
        descript.setValid(true);
        descript.setRequestBody(true);
        descript.setList(true);
        Parameter parameter = buildMethodParameter(descript);
        parameter.setRemark("待更新的数据对象列表");
        method.addParameter(parameter);
        method.setReturnType(getResponseResult(ReturnTypeEnum.RESPONSE_RESULT_LIST,
                getMethodParameterVOType(""),
                parentElement));
        method.setReturnRemark("更新后的数据对象列表");

        method.addAnnotation(new SystemLog("更新了多条记录",introspectedTable),parentElement);
        method.addAnnotation(new RequestMapping("batch", RequestMethod.PUT),parentElement);
        addSecurityPreAuthorize(method, methodPrefix, "批量更新");
        method.addAnnotation(new ApiOperation("批量更新数据", "根据主键批量更新数据"),parentElement);

        commentGenerator.addMethodJavaDocLine(method, "根据主键批量更新实体对象");

        method.addBodyLine("ServiceResult<List<{0}>> result =  {1}.{2}({3});"
                , entityType.getShortName()
                , serviceBeanName
                , introspectedTable.getUpdateBatchStatementId()
                , getServiceMethodEntityParameter(true, "update"));
        method.addBodyLine("if (result.isSuccess()) {");
        if (introspectedTable.getRules().isGenerateVoModel()) {
            method.addBodyLine("return success(mappings.to{0}VOs(result.getResult()),result.getAffectedRows());"
                    , entityType.getShortName());
        } else {
            method.addBodyLine("return success(result.getResult(),result.getAffectedRows());");
        }
        method.addBodyLine("}else{");
        method.addBodyLine("return failure(ApiCodeEnum.FAIL_OPERATION);");
        method.addBodyLine("}");

        parentElement.addMethod(method);
    }
}
