package org.mybatis.generator.codegen.mybatis3.controller.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.config.VOExcelGeneratorConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;

public class TemplateElementGenerator extends AbstractControllerElementGenerator {

    public TemplateElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        VOExcelGeneratorConfiguration voExcelGeneratorConfiguration = introspectedTable.getTableConfiguration().getVoExcelGeneratorConfiguration();
        if (!voExcelGeneratorConfiguration.isGenerate()) {
            return;
        }
        FullyQualifiedJavaType excelVoType = voExcelGeneratorConfiguration.getFullyQualifiedJavaType();
        parentElement.addImportedType("org.springframework.http.MediaType");
        parentElement.addImportedType("javax.servlet.http.HttpServletResponse");
        parentElement.addImportedType("com.vgosoft.plugins.excel.service.VgoEasyExcel");
        parentElement.addImportedType("java.io.IOException");
        parentElement.addImportedType(excelVoType);
        parentElement.addImportedType(FullyQualifiedJavaType.getNewListInstance());

        final String methodPrefix = "template";
        Method method = createMethod(methodPrefix);
        addSystemLogAnnotation(method, parentElement);
        FullyQualifiedJavaType response = new FullyQualifiedJavaType("javax.servlet.http.HttpServletResponse");
        method.addParameter(new Parameter(response, "response"));
        addControllerMapping(method, "import/template", "get");
        method.addException(new FullyQualifiedJavaType("java.io.IOException"));

        method.addBodyLine("List<{0}> list = List.of(",excelVoType.getShortName());
        method.addBodyLine("        {0}.builder()",excelVoType.getShortName());
        for (IntrospectedColumn excelVOColumn : JavaBeansUtil.getExcelVOColumns(introspectedTable)) {
            method.addBodyLine("                .{0}({1})",
                    excelVOColumn.getJavaProperty(),
                    JavaBeansUtil.getColumnExampleValue(excelVOColumn));
        }
        method.addBodyLine("                .build());");
        method.addBodyLine("VgoEasyExcel.write(response, \"{1}导入模板\", \"{1}\", {0}.class, list);",excelVoType.getShortName(),introspectedTable.getRemarks());

        parentElement.addMethod(method);
    }


}
