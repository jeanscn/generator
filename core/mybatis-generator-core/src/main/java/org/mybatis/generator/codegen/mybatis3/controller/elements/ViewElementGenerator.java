package org.mybatis.generator.codegen.mybatis3.controller.elements;

import static com.vgosoft.tool.core.VStringUtil.format;
import static org.mybatis.generator.custom.ConstantsUtil.SERVICE_RESULT;
import static org.mybatis.generator.custom.ConstantsUtil.V_STRING_UTIL;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.internal.util.StringUtility;

public class ViewElementGenerator extends AbstractControllerElementGenerator {

    public ViewElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(SERVICE_RESULT);
        parentElement.addImportedType(V_STRING_UTIL);
        parentElement.addImportedType("java.util.Optional");
        parentElement.addImportedType("org.springframework.web.servlet.ModelAndView");
        parentElement.addImportedType(entityType);

        final String methodPrefix = "view";
        StringBuilder sb = new StringBuilder();
        Method method = createMethod(methodPrefix);
        addSystemLogAnnotation(method,parentElement);

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
        method.addBodyLine(format("ServiceResult<{0}> serviceResult = {1}.selectByPrimaryKey(id);",
                entityType.getShortName(), serviceBeanName));
        method.addBodyLine("if (serviceResult.isSuccess()) {");
        method.addBodyLine(format("mv.addObject(\"{0}\",serviceResult.getResult());",this.entityNameKey));
        method.addBodyLine("}else{");
        method.addBodyLine("mv.addObject(\"error\", serviceResult.getMessage());");
        method.addBodyLine("}");
        method.addBodyLine("}else{");
        method.addBodyLine(format("mv.addObject(\"{0}\", new {1}(0));", this.entityNameKey,entityType.getShortName()));
        method.addBodyLine("}");
        method.addBodyLine("mv.addObject(\"viewStatus\", Optional.ofNullable(viewStatus).orElse(\"1\"));");
        sb.setLength(0);
        sb.append("String viewName = VStringUtil.format(\"");
        sb.append(this.htmlMapGeneratorConfiguration.getTargetPackage()).append("/");
        sb.append("{0}");
        sb.append(StringUtility.substringBeforeLast(this.htmlMapGeneratorConfiguration.getHtmlFileName(),"."));
        sb.append("\",Optional.ofNullable(prefix).orElse(\"\"));");
        method.addBodyLine(sb.toString());
        method.addBodyLine("mv.setViewName(viewName);");
        method.addBodyLine("return mv;");
        parentElement.addMethod(method);
    }
}
