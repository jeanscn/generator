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
import org.mybatis.generator.custom.annotations.ApiOperation;
import org.mybatis.generator.custom.annotations.RequestMapping;
import org.mybatis.generator.custom.annotations.SystemLog;
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

        method.addAnnotation(new SystemLog("通过表单查看或创建记录",introspectedTable),parentElement);
        method.addAnnotation(new RequestMapping("view", RequestMethod.GET),parentElement);
        addSecurityPreAuthorize(method,methodPrefix,"查看");
        method.addAnnotation(new ApiOperation("获得数据并返回页面视图（可用于普通业务在列表中新建接口）",
                "根据给定id获取单个实体，id为可选参数，当id存在时查询数据，否则直接返回视图"),parentElement);

        commentGenerator.addMethodJavaDocLine(method, "根据主键获取单个业务实例");

        //函数体
        sb.append("ModelAndView mv = new ModelAndView();");
        method.addBodyLine(sb.toString());
        method.addBodyLine("if (id != null) {");
        method.addBodyLine(format("ServiceResult<{0}> serviceResult = {1}.selectByPrimaryKey(id);",
                entityType.getShortName(), serviceBeanName));
        method.addBodyLine("if (serviceResult.isSuccess()) {");
        method.addBodyLine(format("mv.addObject(\"{0}\",{1});",this.entityNameKey,
                introspectedTable.getRules().isGenerateVoModel()?"mappings.to"+entityVoType.getShortName()+"(serviceResult.getResult())":"serviceResult.getResult()"));
        method.addBodyLine("}else{");
        method.addBodyLine("mv.addObject(\"error\", serviceResult.getMessage());");
        method.addBodyLine("}");
        method.addBodyLine("}else{");
        method.addBodyLine(format("mv.addObject(\"{0}\", new {1});",
                this.entityNameKey,introspectedTable.getRules().isGenerateVoModel()?this.entityVoType.getShortName()+"()"
                                :entityType.getShortName()+"(0)"));
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

        parentElement.addMethod(method);
    }
}
