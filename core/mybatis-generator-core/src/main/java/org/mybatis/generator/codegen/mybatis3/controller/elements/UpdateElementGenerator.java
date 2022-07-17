package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import static org.mybatis.generator.custom.ConstantsUtil.*;

public class UpdateElementGenerator extends AbstractControllerElementGenerator {

    public UpdateElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(SERVICE_RESULT);
        parentElement.addImportedType(RESPONSE_SIMPLE);
        parentElement.addImportedType(RESPONSE_SIMPLE_IMPL);
        parentElement.addImportedType(entityType);

        final String methodPrefix = "update";
        Method method = createMethod(methodPrefix);
        addSystemLogAnnotation(method,parentElement);

        StringBuilder sb = new StringBuilder();

        method.addParameter(entityParameter);
        entityParameter.addAnnotation("@RequestBody");
        method.setReturnType(responseSimple);
        addControllerMapping(method, "", "put");
        method.addBodyLine("ResponseSimple responseSimple = new ResponseSimpleImpl();");
        method.addBodyLine(VStringUtil.format("ServiceResult<{0}> serviceResult = {1}.updateByPrimaryKey({2});",
                entityType.getShortName(),serviceBeanName,entityFirstLowerShortName));
        method.addBodyLine("if (serviceResult.isSuccess()) {");
        if (JavaBeansUtil.isAssignable("com.vgosoft.core.entity.IPersistenceLock",entityType.getFullyQualifiedName(), introspectedTable)) {
            method.addBodyLine("responseSimple.addAttribute(\"version\", serviceResult.getResult().getVersion());");
        }
        method.addBodyLine(VStringUtil.format("responseSimple.addAttribute(\"{0}\",serviceResult.getResult());",this.entityNameKey));
        method.addBodyLine("}else{");
        method.addBodyLine("responseSimple.setStatus(1);");
        method.addBodyLine("responseSimple.setMessage(serviceResult.getMessage());");
        method.addBodyLine("}");
        method.addBodyLine("return responseSimple;");

        parentElement.addMethod(method);
    }
}
