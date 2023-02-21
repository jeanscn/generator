package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.RequestMethod;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.annotations.ApiOperation;
import org.mybatis.generator.custom.annotations.RequestMapping;
import org.mybatis.generator.custom.annotations.SystemLog;

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
        MethodParameterDescript descript = new MethodParameterDescript(parentElement,"get");
        Parameter parameter1 = buildMethodParameter(descript);
        parameter1.setRemark("请求数据");
        method.addParameter(parameter1);
        Parameter actionType = new Parameter(FullyQualifiedJavaType.getStringInstance(), "actionType");
        actionType.setRemark("场景类型，用来标识不同查询类型");
        actionType.addAnnotation("@RequestParam(required = false)");
        method.addParameter(actionType);
        method.addException(new FullyQualifiedJavaType("java.io.IOException"));
        method.setExceptionRemark("IO读写异常");

        method.addAnnotation(new SystemLog("数据导出",introspectedTable),parentElement);
        method.addAnnotation(new RequestMapping("export", RequestMethod.GET),parentElement);
        addSecurityPreAuthorize(method,methodPrefix,"导出");
        method.addAnnotation(new ApiOperation("Excel数据导出", "Excel数据导出接口"),parentElement);

        commentGenerator.addMethodJavaDocLine(method, "Excel数据导出");

        String requestVOVar = entityRequestVoType.getShortNameFirstLowCase();
        method.addBodyLine("{0} example = buildExample(actionType,{1});",
                exampleType.getShortName(),
                introspectedTable.getRules().isGenerateRequestVO()?requestVOVar:
                        introspectedTable.getRules().isGenerateVoModel()?entityVoType.getShortNameFirstLowCase():entityType.getShortNameFirstLowCase());
        method.addBodyLine("ServiceResult<List<{0}>> result  = {1}.selectByExample(example);",
                entityType.getShortName(), serviceBeanName);
        method.addBodyLine("List<{0}> list = mappings.to{0}s(result.getResult());",
                entityExcelVoType.getShortName());
        method.addBodyLine("VgoEasyExcel.write(response, \"{1}数据\", \"{1}\", {0}.class, list);",entityExcelVoType.getShortName(),introspectedTable.getRemarks(true));
        parentElement.addMethod(method);
    }


}
