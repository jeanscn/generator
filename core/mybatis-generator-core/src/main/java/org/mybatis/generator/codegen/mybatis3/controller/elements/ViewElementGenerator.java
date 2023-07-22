package org.mybatis.generator.codegen.mybatis3.controller.elements;

import static com.vgosoft.tool.core.VStringUtil.format;
import static org.mybatis.generator.custom.ConstantsUtil.SERVICE_RESULT;
import static org.mybatis.generator.custom.ConstantsUtil.V_STRING_UTIL;

import com.vgosoft.core.constant.enums.core.RequestMethod;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.annotations.ApiOperationDesc;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;
import org.mybatis.generator.custom.annotations.SystemLogDesc;
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

        Parameter parameter = new Parameter(FullyQualifiedJavaType.getStringInstance(), "id");
        parameter.addAnnotation("@RequestParam(required = false)");
        parameter.setRemark("可选参数，存在时查询数据；否则直接返回视图，用于打开表单。");
        method.addParameter(parameter);
        Parameter viewStatus = new Parameter(FullyQualifiedJavaType.getStringInstance(), "viewStatus");
        viewStatus.addAnnotation("@RequestParam(required = false)");
        viewStatus.setRemark("可选参数，打开方式。1-编辑，0-只读");
        method.addParameter(viewStatus);
        Parameter prefix = new Parameter(FullyQualifiedJavaType.getStringInstance(), "prefix");
        prefix.addAnnotation("@RequestParam(required = false)");
        prefix.setRemark("可选参数，页面文件前缀。常用于打开非数据管理页面之外的页面，比如打印页面，统计页面等。");
        method.addParameter(prefix);
        method.setReturnType(new FullyQualifiedJavaType("ModelAndView"));

        method.addAnnotation(new SystemLogDesc("通过表单查看或创建记录",introspectedTable),parentElement);
        method.addAnnotation(new RequestMappingDesc("view", RequestMethod.GET),parentElement);
        addSecurityPreAuthorize(method,methodPrefix,"查看");
        method.addAnnotation(new ApiOperationDesc("获得数据并返回页面视图（可用于普通业务在列表中新建接口）",
                "根据给定id获取单个实体，id为可选参数，当id存在时查询数据，否则直接返回视图"),parentElement);

        commentGenerator.addMethodJavaDocLine(method, "根据主键获取单个业务实例");

        String entityName = introspectedTable.getRules().isGenerateVoModel()?this.entityVoType.getShortName():entityType.getShortName();
        //函数体
        sb.append("ModelAndView mv = new ModelAndView();");
        method.addBodyLine(sb.toString());
        method.addBodyLine("JsonUtil.init(SpringContextHolder.getBean(ObjectMapper.class));");
        method.addBodyLine("{0} object = null;",entityName);
        method.addBodyLine("if (id != null) {");
        method.addBodyLine(format("ServiceResult<{0}> serviceResult = {1}.selectByPrimaryKey(id);",entityType.getShortName(), serviceBeanName));
        method.addBodyLine("if (serviceResult.hasResult()) {");
        if (introspectedTable.getRules().isGenerateVoModel()) {
            method.addBodyLine("object = mappings.to{0}VO(serviceResult.getResult());",entityType.getShortName());
        }else{
            method.addBodyLine("object = serviceResult.getResult();",entityType.getShortName());
        }
        method.addBodyLine("}else{");
        method.addBodyLine("mv.addObject(\"error\", serviceResult.getMessage());");
        method.addBodyLine("}");
        method.addBodyLine("}else{");
        method.addBodyLine("object = updateNewInstanceDefaultValue(new {0}());",entityName);
        method.addBodyLine("}");
        method.addBodyLine("if (object != null) {");
        method.addBodyLine("{0} {1} = JsonUtil.serializeObject(object);",entityName,entityType.getShortNameFirstLowCase());
        method.addBodyLine("mv.addObject(\"{0}\", {1});",this.entityNameKey,entityType.getShortNameFirstLowCase());
        method.addBodyLine("}");
        method.addBodyLine("mv.addObject(\"viewStatus\", Optional.ofNullable(viewStatus).orElse(\"1\"));");
        sb.setLength(0);
        sb.append("String viewName = VStringUtil.format(\"");
        sb.append(introspectedTable.getTableConfiguration().getHtmlBasePath()).append("/");
        sb.append("{0}");
        sb.append(StringUtility.substringBeforeLast(this.htmlGeneratorConfiguration.getHtmlFileName(),"."));
        sb.append("\",Optional.ofNullable(prefix).orElse(\"\"));");
        method.addBodyLine(sb.toString());
        method.addBodyLine("mv.setViewName(viewName);");
        method.addBodyLine("return mv;");

        parentElement.addImportedType("com.vgosoft.web.utils.JsonUtil");
        parentElement.addImportedType("com.vgosoft.core.util.SpringContextHolder");
        parentElement.addImportedType("com.fasterxml.jackson.databind.ObjectMapper");

        parentElement.addMethod(method);
    }
}
