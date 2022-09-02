package org.mybatis.generator.codegen.mybatis3.controller.elements;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.config.VOExcelGeneratorConfiguration;

public class ExportElementGenerator extends AbstractControllerElementGenerator {

    public ExportElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        VOExcelGeneratorConfiguration voExcelGeneratorConfiguration = introspectedTable.getTableConfiguration().getVoExcelGeneratorConfiguration();
        if (!voExcelGeneratorConfiguration.isGenerate()) {
            return;
        }
        FullyQualifiedJavaType excelVoType = voExcelGeneratorConfiguration.getFullyQualifiedJavaType();
        parentElement.addImportedType("javax.servlet.http.HttpServletResponse");
        parentElement.addImportedType("com.vgosoft.plugins.excel.service.VgoEasyExcel");
        parentElement.addImportedType("java.io.IOException");
        parentElement.addImportedType(excelVoType);
        parentElement.addImportedType(entityType);
        parentElement.addImportedType(FullyQualifiedJavaType.getNewListInstance());

        final String methodPrefix = "export";
        Method method = createMethod(methodPrefix);
        addSystemLogAnnotation(method, parentElement);
        addControllerMapping(method, "export", "get");
        FullyQualifiedJavaType response = new FullyQualifiedJavaType("javax.servlet.http.HttpServletResponse");
        method.addParameter(new Parameter(response, "response"));
        if (introspectedTable.getRules().isGenerateRequestVO()) {
            method.addParameter(new Parameter(entityRequestVoType, entityRequestVoType.getShortNameFirstLowCase()));
            parentElement.addImportedType(entityRequestVoType);
        } else{
            method.addParameter(buildMethodParameter(false, false,parentElement));
        }
        method.addException(new FullyQualifiedJavaType("java.io.IOException"));
        Parameter actionType = new Parameter(FullyQualifiedJavaType.getStringInstance(), "actionType");
        actionType.addAnnotation("@RequestParam(required = false)");
        method.addParameter(actionType);
        addSecurityPreAuthorize(method,methodPrefix);

        String requestVOVar = entityRequestVoType.getShortNameFirstLowCase();
        method.addBodyLine("{0} example = buildExample(actionType,{1});",
                exampleType.getShortName(),
                introspectedTable.getRules().isGenerateRequestVO()?requestVOVar:
                        introspectedTable.getRules().isGenerateVoModel()?entityVoType.getShortNameFirstLowCase():entityType.getShortNameFirstLowCase());
        method.addBodyLine("List<{0}> {1}s = {2}.selectByExample(example);",
                entityType.getShortName(), entityType.getShortNameFirstLowCase(),serviceBeanName);
        method.addBodyLine("List<{0}> list = mappings.to{0}s({1}s);",
                excelVoType.getShortName(),entityType.getShortNameFirstLowCase());
        method.addBodyLine("VgoEasyExcel.write(response, \"{1}数据\", \"{1}\", {0}.class, list);",excelVoType.getShortName(),introspectedTable.getRemarks());
        parentElement.addMethod(method);
    }


}
