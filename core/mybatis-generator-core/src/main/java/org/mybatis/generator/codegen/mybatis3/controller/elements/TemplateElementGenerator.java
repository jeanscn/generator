package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethodEnum;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.annotations.ApiOperationDesc;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;
import org.mybatis.generator.custom.annotations.SystemLogDesc;

public class TemplateElementGenerator extends AbstractControllerElementGenerator {

    public TemplateElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType("javax.servlet.http.HttpServletResponse");
        parentElement.addImportedType("com.vgosoft.plugins.excel.service.VgoEasyExcel");
        parentElement.addImportedType("java.io.IOException");
        parentElement.addImportedType(entityExcelVoType);
        parentElement.addImportedType(FullyQualifiedJavaType.getNewListInstance());

        final String methodPrefix = "template";
        Method method = createMethod(methodPrefix);

        FullyQualifiedJavaType response = new FullyQualifiedJavaType("javax.servlet.http.HttpServletResponse");
        Parameter parameter = new Parameter(response, "response");
        parameter.setRemark("http响应");
        method.addParameter(parameter);
        method.addException(new FullyQualifiedJavaType("java.io.IOException"));
        method.setExceptionRemark("IO读写异常");

        method.addAnnotation(new SystemLogDesc("下载数据导入模板",introspectedTable),parentElement);
        RequestMappingDesc requestMappingDesc = new RequestMappingDesc("import/template", RequestMethodEnum.GET);
        requestMappingDesc.addProduces("MediaType.APPLICATION_OCTET_STREAM_VALUE");
        method.addAnnotation(requestMappingDesc,parentElement);
        parentElement.addImportedType("org.springframework.http.MediaType");
        addSecurityPreAuthorize(method,methodPrefix,"导入模板");
        method.addAnnotation(new ApiOperationDesc("Excel导入模板", "下载Excel导入模板接口"),parentElement);

        commentGenerator.addMethodJavaDocLine(method, "下载数据导入模板");

        method.addBodyLine("List<{0}> list = buildTemplateSampleData();",entityExcelImportVoType.getShortName());
        method.addBodyLine("VgoEasyExcel.write(response, \"{1}导入模板\", \"{1}\", {0}.class, list, getDefaultColumnWidthStyleStrategy(\"template\"));",entityExcelImportVoType.getShortName(),introspectedTable.getRemarks(true));

        parentElement.addMethod(method);
    }
}
