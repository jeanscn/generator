package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethodEnum;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.config.JavaControllerGeneratorConfiguration;
import org.mybatis.generator.custom.ReturnTypeEnum;
import org.mybatis.generator.custom.annotations.ApiOperationDesc;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;
import org.mybatis.generator.custom.annotations.SystemLogDesc;

import static org.mybatis.generator.custom.ConstantsUtil.SERVICE_RESULT;


public class UpdateElementGenerator extends AbstractControllerElementGenerator {

    public UpdateElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        JavaControllerGeneratorConfiguration configuration = this.introspectedTable.getTableConfiguration().getJavaControllerGeneratorConfiguration();
        parentElement.addImportedType(SERVICE_RESULT);
        parentElement.addImportedType(entityType);
        if (introspectedTable.getRules().isGenerateVoModel()) {
            parentElement.addImportedType(entityVoType);
            parentElement.addImportedType(entityMappings);
        }

        final String methodPrefix = "update";
        Method method = createMethod(methodPrefix);

        MethodParameterDescriptor descriptor = new MethodParameterDescriptor(parentElement, "put");
        descriptor.setValid(true);
        descriptor.setRequestBody(true);
        Parameter parameter = buildMethodParameter(descriptor);
        parameter.setRemark("待更新的数据对象");
        method.addParameter(parameter);
        method.setReturnType(getResponseResult(ReturnTypeEnum.RESPONSE_RESULT_MODEL,
                introspectedTable.getRules().isGenerateVoModel() ? entityVoType : entityType,
                parentElement));
        method.setReturnRemark("更新后的数据对象");

        method.addAnnotation(new SystemLogDesc("更新了一条记录", introspectedTable), parentElement);
        method.addAnnotation(new RequestMappingDesc("", RequestMethodEnum.PUT), parentElement);
        addSecurityPreAuthorize(method, methodPrefix, "更新");
        method.addAnnotation(new ApiOperationDesc("更新一条记录", "根据主键更新数据"), parentElement);

        commentGenerator.addMethodJavaDocLine(method, "根据主键更新实体对象");

        method.addBodyLine("ServiceResult<{0}> serviceResult = {1}.{3}({2});"
                , entityType.getShortName()
                , serviceBeanName
                , getServiceMethodEntityParameter(false, "update")
                , introspectedTable.getUpdateByPrimaryKeySelectiveStatementId());

        method.addBodyLine("if (serviceResult.hasResult()) {");
        method.addBodyLine("return success({0},serviceResult.getAffectedRows());"
                , introspectedTable.getRules().isGenerateVoModel() ? "mappings.to" + entityVoType.getShortName() + "(serviceResult.getResult())" : "serviceResult.getResult()");
        method.addBodyLine("}else{");
        method.addBodyLine("return failure(ApiCodeEnum.FAIL_CUSTOM,serviceResult.getMessage());");
        method.addBodyLine("}");

        parentElement.addMethod(method);
    }
}
