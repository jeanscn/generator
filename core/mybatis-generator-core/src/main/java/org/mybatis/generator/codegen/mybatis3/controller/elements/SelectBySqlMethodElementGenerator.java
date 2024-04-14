package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethodEnum;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.ReturnTypeEnum;
import org.mybatis.generator.custom.annotations.ApiOperationDesc;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;
import org.mybatis.generator.custom.annotations.SystemLogDesc;

import static org.mybatis.generator.custom.ConstantsUtil.SERVICE_RESULT;

public class SelectBySqlMethodElementGenerator extends AbstractControllerElementGenerator {

    public SelectBySqlMethodElementGenerator() {
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

        final String methodPrefix = "get";
        Method method = createMethod(methodPrefix);

        MethodParameterDescript descript = new MethodParameterDescript(parentElement, "put");
        descript.setValid(true);
        descript.setRequestBody(true);
        descript.setList(true);
        method.addParameter(buildMethodParameter(descript));
        method.setReturnType(getResponseResult(ReturnTypeEnum.RESPONSE_RESULT_LIST,
                getMethodParameterVOType(""),
                parentElement));

        method.addAnnotation(new SystemLogDesc("获取上级或下级标识",introspectedTable),parentElement);
        method.addAnnotation(new RequestMappingDesc("", RequestMethodEnum.GET),parentElement);
        addSecurityPreAuthorize(method, methodPrefix, "调用获取上级或下级标识接口");
        method.addAnnotation(new ApiOperationDesc("集成sql方法的查询", "获取所有父级或子级记录"),parentElement);

        commentGenerator.addMethodJavaDocLine(method, "获取上级或下级标识");

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
