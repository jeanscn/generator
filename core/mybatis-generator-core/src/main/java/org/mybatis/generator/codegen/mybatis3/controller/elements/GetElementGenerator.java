package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;

import static org.mybatis.generator.custom.ConstantsUtil.SERVICE_RESULT;

public class GetElementGenerator extends AbstractControllerElementGenerator {

    public GetElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(SERVICE_RESULT);
        parentElement.addImportedType(entityType);
        if (isGenerateVoModel()) {
            parentElement.addImportedType(entityVoType);
            parentElement.addImportedType(entityMappings);
        }

        final String methodPrefix = "get";
        Method method = createMethod(methodPrefix);
        addSystemLogAnnotation(method,parentElement);
        Parameter parameter = new Parameter(FullyQualifiedJavaType.getStringInstance(), "id");
        parameter.addAnnotation("@PathVariable");
        method.addParameter(parameter);
        method.setReturnType(getResponseResult(false));
        addControllerMapping(method, "{id}", "get");
        //函数体
        method.addBodyLine(VStringUtil.format("ServiceResult<{0}> serviceResult = {1}.selectByPrimaryKey(id);",
                entityType.getShortName(), serviceBeanName));
        method.addBodyLine("if (serviceResult.isSuccess()) {");
        method.addBodyLine("return success({0});",
                isGenerateVoModel()?"mappings.to"+entityVoType.getShortName()+"(serviceResult.getResult())":"serviceResult.getResult()");
        method.addBodyLine("}else{");
        method.addBodyLine("return failure(ApiCodeEnum.FAIL_NOT_FOUND);");
        method.addBodyLine("}");
        parentElement.addMethod(method);

    }
}
