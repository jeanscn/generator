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

public class ExportElementGenerator extends AbstractControllerElementGenerator {

    public ExportElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType("javax.servlet.http.HttpServletResponse");
        parentElement.addImportedType("com.vgosoft.plugins.excel.service.VgoEasyExcel");
        parentElement.addImportedType("java.io.IOException");
        parentElement.addImportedType(entityExcelVoType);
        parentElement.addImportedType(entityType);
        parentElement.addImportedType(FullyQualifiedJavaType.getNewListInstance());

        final String methodPrefix = "export";
        Method method = createMethod(methodPrefix);

        FullyQualifiedJavaType response = new FullyQualifiedJavaType("javax.servlet.http.HttpServletResponse");
        Parameter parameter = new Parameter(response, "response");
        parameter.setRemark("http响应");
        method.addParameter(parameter);
        MethodParameterDescriptor descriptor = new MethodParameterDescriptor(parentElement,"get");
        FullyQualifiedJavaType listInstance = FullyQualifiedJavaType.getNewListInstance();
        listInstance.addTypeArgument(FullyQualifiedJavaType.getStringInstance());
        Parameter idsParam = new Parameter(listInstance, "ids");
        idsParam.setRemark("需要导出的数据id列表");
        idsParam.addAnnotation("@RequestParam");
        idsParam.addAnnotation("@RequestParamSplit");
        parentElement.addImportedType("com.vgosoft.web.resolver.annotation.RequestParamSplit");
        method.addParameter(idsParam);
        method.addException(new FullyQualifiedJavaType("java.io.IOException"));
        method.setExceptionRemark("IO读写异常");

        method.addAnnotation(new SystemLogDesc("数据导出",introspectedTable),parentElement);
        method.addAnnotation(new RequestMappingDesc("export", RequestMethodEnum.GET),parentElement);
        addSecurityPreAuthorize(method,methodPrefix,"导出");
        method.addAnnotation(new ApiOperationDesc("Excel数据导出", "Excel数据导出接口"),parentElement);

        commentGenerator.addMethodJavaDocLine(method, "Excel数据导出");

        String requestVOVar = entityRequestVoType.getShortNameFirstLowCase();
        method.addBodyLine("{0} example = new {0}();", exampleType.getShortName());
        method.addBodyLine("example.createCriteria().andIdIn(ids);");
        method.addBodyLine("ServiceResult<List<{0}>> result = {1}.selectByExample(example);",
                entityType.getShortName(), serviceBeanName);
        method.addBodyLine("List<{0}> list = mappings.to{0}s(result.getResult()).stream()",
                entityExcelVoType.getShortName());
        method.addBodyLine("        .map(JsonUtil::serializeObject)\n" +
                "                .collect(Collectors.toList());");
        method.addBodyLine("VgoEasyExcel.write(response, \"{1}数据\", \"{1}\", {0}.class, list, getDefaultColumnWidthStyleStrategy(\"export\"));",entityExcelVoType.getShortName(),introspectedTable.getRemarks(true));
        parentElement.addMethod(method);
        parentElement.addImportedType("java.util.stream.Collectors");
        parentElement.addImportedType("com.vgosoft.tool.core.JsonUtil");
    }


}
