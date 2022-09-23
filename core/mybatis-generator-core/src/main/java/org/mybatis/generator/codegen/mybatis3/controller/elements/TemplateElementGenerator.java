package org.mybatis.generator.codegen.mybatis3.controller.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.VOExcelGeneratorConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;

public class TemplateElementGenerator extends AbstractControllerElementGenerator {

    public TemplateElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType("org.springframework.http.MediaType");
        parentElement.addImportedType("javax.servlet.http.HttpServletResponse");
        parentElement.addImportedType("com.vgosoft.plugins.excel.service.VgoEasyExcel");
        parentElement.addImportedType("java.io.IOException");
        parentElement.addImportedType(entityExcelVoType);
        parentElement.addImportedType(FullyQualifiedJavaType.getNewListInstance());

        final String methodPrefix = "template";
        Method method = createMethod(methodPrefix);
        addSystemLogAnnotation(method, parentElement);
        FullyQualifiedJavaType response = new FullyQualifiedJavaType("javax.servlet.http.HttpServletResponse");
        method.addParameter(new Parameter(response, "response"));
        addControllerMapping(method, "import/template", "get");
        method.addException(new FullyQualifiedJavaType("java.io.IOException"));
        addSecurityPreAuthorize(method,methodPrefix,"导入模板");

        method.addBodyLine("List<{0}> list = buildTemplateSampleData();",entityExcelVoType.getShortName());
        method.addBodyLine("VgoEasyExcel.write(response, \"{1}导入模板\", \"{1}\", {0}.class, list);",entityExcelVoType.getShortName(),introspectedTable.getRemarks(true));

        parentElement.addMethod(method);
    }
}
