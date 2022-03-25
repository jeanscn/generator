package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;

public class ViewElementGenerator extends AbstractControllerElementGenerator {

    public ViewElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType("com.vgosoft.core.adapter.ServiceResult");
        parentElement.addImportedType("com.vgosoft.tool.core.VStringUtil");
        parentElement.addImportedType("java.util.Optional");
        parentElement.addImportedType("org.springframework.web.servlet.ModelAndView");
        parentElement.addImportedType(entityType);

        final String methodPrefix = "view";
        StringBuilder sb = new StringBuilder();
        Method method = createMethod(methodPrefix);
        Parameter parameter = new Parameter(FullyQualifiedJavaType.getStringInstance(), "id");
        parameter.addAnnotation("@RequestParam(required = false)");
        method.addParameter(parameter);
        Parameter viewStatus = new Parameter(FullyQualifiedJavaType.getStringInstance(), "viewStatus");
        viewStatus.addAnnotation("@RequestParam(required = false)");
        method.addParameter(viewStatus);
        Parameter prefix = new Parameter(FullyQualifiedJavaType.getStringInstance(), "prefix");
        prefix.addAnnotation("@RequestParam(required = false)");
        method.addParameter(prefix);
        method.setReturnType(new FullyQualifiedJavaType("ModelAndView"));
        addControllerMapping(method, methodPrefix, "get");
        //函数体
        sb.append("ModelAndView mv = new ModelAndView();");
        method.addBodyLine(sb.toString());
        method.addBodyLine("if (id != null) {");
        method.addBodyLine(VStringUtil.format("ServiceResult<{0}> serviceResult = {1}.selectByPrimaryKey(id);",
                entityType.getShortName(), serviceBeanName));
        method.addBodyLine("if (serviceResult.isSuccess()) {");
        method.addBodyLine(VStringUtil.format("mv.addObject(\"{0}\",serviceResult.getResult());",this.entityNameKey));
        method.addBodyLine("}else{");
        method.addBodyLine("mv.addObject(\"error\", serviceResult.getMessage());");
        method.addBodyLine("}");
        method.addBodyLine("}else{");
        method.addBodyLine(VStringUtil.format("mv.addObject(\"{0}\", new {1}(0));", this.entityNameKey,entityType.getShortName()));
        method.addBodyLine("}");
        method.addBodyLine("mv.addObject(\"viewStatus\", Optional.ofNullable(viewStatus).orElse(\"1\"));");
        sb.setLength(0);
        sb.append("String viewName = VStringUtil.format(\"");
        sb.append(htmlDescriptor.getTargetPackage()).append("/");
        sb.append("{0}");
        sb.append(htmlDescriptor.getViewPath());
        sb.append("\",Optional.ofNullable(prefix).orElse(\"\"));");
        method.addBodyLine(sb.toString());
        method.addBodyLine("mv.setViewName(viewName);");
        method.addBodyLine("return mv;");
        parentElement.addMethod(method);
    }
}
