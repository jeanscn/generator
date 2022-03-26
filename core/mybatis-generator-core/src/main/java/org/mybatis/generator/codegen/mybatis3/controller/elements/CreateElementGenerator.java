package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;

public class CreateElementGenerator extends AbstractControllerElementGenerator {

    public CreateElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType("com.vgosoft.core.adapter.ServiceResult");
        parentElement.addImportedType("com.vgosoft.web.respone.ResponseSimple");
        parentElement.addImportedType("com.vgosoft.web.respone.ResponseSimpleImpl");
        parentElement.addImportedType(entityType);

        final String methodPrefix = "create";
        Method method = createMethod(methodPrefix);
        StringBuilder sb = new StringBuilder();

        method.addParameter(entityParameter);
        entityParameter.addAnnotation("@RequestBody");
        method.setReturnType(responseSimple);
        addControllerMapping(method, "", "post");
        method.addBodyLine("ResponseSimple responseSimple = new ResponseSimpleImpl();");
        method.addBodyLine(VStringUtil.format("ServiceResult<{0}> insert = {1}.insert({2});",
                entityType.getShortName(),serviceBeanName, entityFirstLowerShortName));
        method.addBodyLine("if (!insert.isSuccess()) {");
        method.addBodyLine("setExceptionResponse(responseSimple, insert.getException());");
        method.addBodyLine("}else{");
        method.addBodyLine("responseSimple.addAttribute(\"id\", insert.getResult().getId());");
        method.addBodyLine("responseSimple.addAttribute(\"version\", insert.getResult().getVersion());");
        method.addBodyLine("}");
        method.addBodyLine("return responseSimple;");

        parentElement.addMethod(method);
    }
}
