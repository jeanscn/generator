package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethodEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;
import org.mybatis.generator.custom.annotations.swagger3.OperationDesc;
import org.mybatis.generator.custom.enums.ReturnTypeEnum;

import static org.mybatis.generator.custom.ConstantsUtil.SERVICE_RESULT;

/**
 * save-batch端点元素：接收子表变更集并在单事务内统一持久化。
 *
 * @author vgosoft-agent
 */
public class SaveBatchElementGenerator extends AbstractControllerElementGenerator {

    private static final String BATCH_CHANGE_VO = "com.vgosoft.core.pojo.parameter.VgoBatchChangeVo";

    public SaveBatchElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(SERVICE_RESULT);
        parentElement.addImportedType(entityType);

        FullyQualifiedJavaType createVoType = getMethodParameterVoType("post");
        FullyQualifiedJavaType updateVoType = getMethodParameterVoType("put");
        FullyQualifiedJavaType changeVoType = new FullyQualifiedJavaType(BATCH_CHANGE_VO);
        changeVoType.addTypeArgument(createVoType);
        changeVoType.addTypeArgument(updateVoType);
        parentElement.addImportedType(changeVoType);
        parentElement.addImportedType(createVoType);
        parentElement.addImportedType(updateVoType);
        if (introspectedTable.getRules().isGenerateCreateVo()
                || introspectedTable.getRules().isGenerateUpdateVo()
                || introspectedTable.getRules().isGenerateVoModel()) {
            parentElement.addImportedType(entityMappings);
        }

        final String methodPrefix = "saveBatch";
        Method method = createMethod(methodPrefix);

        Parameter parameter = new Parameter(changeVoType, "changes");
        parameter.addAnnotation("@Validated");
        parentElement.addImportedType("org.springframework.validation.annotation.Validated");
        parameter.addAnnotation("@RequestBody");
        parentElement.addImportedType("org.springframework.web.bind.annotation.RequestBody");
        parameter.setRemark("待提交的增删改变更集");
        method.addParameter(parameter);
        method.setReturnType(getResponseResult(ReturnTypeEnum.RESPONSE_RESULT_LIST,
                getMethodParameterVoType(""), parentElement));
        method.setReturnRemark("保存后的数据（对象）列表");

        addSystemLogAnnotation(method, parentElement, "提交了一条或多条记录的变更", methodPrefix);
        method.addAnnotation(new RequestMappingDesc("save-batch", RequestMethodEnum.POST), parentElement);
        addSecurityPreAuthorize(method, methodPrefix, "批量保存");
        method.addAnnotation(new OperationDesc("批量保存变更集", "单事务内统一提交子表的新增、更新与删除变更"), parentElement);

        commentGenerator.addMethodJavaDocLine(method, "批量保存变更集（新增、更新、删除）");

        method.addBodyLine("ServiceResult<List<{0}>> result =  {1}.saveBatch({2}, {3}, changes.getDeleteIds());"
                , entityType.getShortName()
                , serviceBeanName
                , changeSetServiceArg("create")
                , changeSetServiceArg("update"));
        method.addBodyLine("if (result.hasResult()) {");
        if (introspectedTable.getRules().isGenerateVoModel()) {
            method.addBodyLine("return success(mappings.to{0}Vos(result.getResult()),result.getAffectedRows());",
                    entityType.getShortName());
        } else {
            method.addBodyLine("return success(result.getResult(),result.getAffectedRows());");
        }
        method.addBodyLine("} else {");
        method.addBodyLine("return failure(ApiCodeEnum.FAIL_CUSTOM,result.getMessage());");
        method.addBodyLine("}");

        parentElement.addMethod(method);
    }

    /**
     * 构造 saveBatch 服务调用中新增/更新列表参数表达式，
     * 存在对应 VO 时经 mappings 转换，否则直接使用请求对象列表。
     */
    private String changeSetServiceArg(String methodType) {
        String getter = "create".equals(methodType) ? "changes.getInserts()" : "changes.getUpdates()";
        if ("create".equals(methodType) && introspectedTable.getRules().isGenerateCreateVo()) {
            return VStringUtil.format("mappings.from{0}CreateVos({1})", entityType.getShortName(), getter);
        }
        if ("update".equals(methodType) && introspectedTable.getRules().isGenerateUpdateVo()) {
            return VStringUtil.format("mappings.from{0}UpdateVos({1})", entityType.getShortName(), getter);
        }
        if (introspectedTable.getRules().isGenerateVoModel()) {
            return VStringUtil.format("mappings.from{0}Vos({1})", entityType.getShortName(), getter);
        }
        return getter;
    }
}
