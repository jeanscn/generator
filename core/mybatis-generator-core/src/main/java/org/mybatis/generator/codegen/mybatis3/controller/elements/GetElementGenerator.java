package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;

public class GetElementGenerator extends AbstractControllerElementGenerator {

    public GetElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType("com.vgosoft.core.adapter.ServiceResult");
        parentElement.addImportedType("com.vgosoft.web.respone.ResponseSimple");
        parentElement.addImportedType("com.vgosoft.web.respone.ResponseSimpleImpl");
        parentElement.addImportedType("org.springframework.web.servlet.ModelAndView");
        parentElement.addImportedType(entityType);

        final String methodPrefix = "get";
        Method method = createMethod(methodPrefix);
        StringBuilder sb = new StringBuilder();
        Parameter parameter = new Parameter(FullyQualifiedJavaType.getStringInstance(), "id");
        parameter.addAnnotation("@PathVariable");
        method.addParameter(parameter);
        method.setReturnType(responseSimple);
        addControllerMapping(method, "{id}", "get");
        //函数体
        method.addBodyLine("ResponseSimple responseSimple = new ResponseSimpleImpl();");
        method.addBodyLine(VStringUtil.format("ServiceResult<{0}> serviceResult = {1}.selectByPrimaryKey(id);",
                entityType.getShortName(), serviceBeanName));
        method.addBodyLine("if (serviceResult.isSuccess()) {");
        method.addBodyLine(VStringUtil.format("responseSimple.addAttribute(\"{0}\",serviceResult.getResult());",this.entityNameKey));
        method.addBodyLine("}else{");
        method.addBodyLine("responseSimple.addAttribute(\"error\", serviceResult.getMessage());");
        method.addBodyLine("}");
        method.addBodyLine("return responseSimple;");

        parentElement.addMethod(method);
    }
}