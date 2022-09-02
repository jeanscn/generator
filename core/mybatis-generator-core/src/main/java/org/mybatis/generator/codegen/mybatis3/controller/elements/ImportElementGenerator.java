package org.mybatis.generator.codegen.mybatis3.controller.elements;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.config.VOExcelGeneratorConfiguration;

import static org.mybatis.generator.custom.ConstantsUtil.MULTIPART_FILE;
import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_RESULT;

public class ImportElementGenerator extends AbstractControllerElementGenerator {

    public ImportElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        VOExcelGeneratorConfiguration voExcelGeneratorConfiguration = introspectedTable.getTableConfiguration().getVoExcelGeneratorConfiguration();
        if (!voExcelGeneratorConfiguration.isGenerate()) {
            return;
        }
        FullyQualifiedJavaType responseResult = new FullyQualifiedJavaType(RESPONSE_RESULT);
        FullyQualifiedJavaType multipartFile = new FullyQualifiedJavaType(MULTIPART_FILE);
        FullyQualifiedJavaType excelVoType = voExcelGeneratorConfiguration.getFullyQualifiedJavaType();
        parentElement.addImportedType("org.springframework.http.MediaType");
        parentElement.addImportedType("com.vgosoft.plugins.excel.service.VgoEasyExcel");
        parentElement.addImportedType(excelVoType);
        parentElement.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        parentElement.addImportedType(multipartFile);
        parentElement.addImportedType(responseResult);

        String listEntityVar = entityType.getShortNameFirstLowCase() + "s";
        final String methodPrefix = "import";
        Method method = createMethod(methodPrefix);
        addSystemLogAnnotation(method, parentElement);
        String sb = "@PostMapping(value = \"" +
                StringUtils.lowerCase(this.serviceBeanName) +
                "/import\",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)";
        method.addAnnotation(sb);
        Parameter multipartFileParameter = new Parameter(multipartFile, "file");
        multipartFileParameter.addAnnotation("@RequestPart(\"file\")");
        method.addParameter(multipartFileParameter);
        responseResult.addTypeArgument(new FullyQualifiedJavaType("java.lang.Integer"));
        method.setReturnType(responseResult);
        method.addException(new FullyQualifiedJavaType("java.lang.Exception"));
        addSecurityPreAuthorize(method,methodPrefix);

        //方法体
        method.addBodyLine("int ret=0;");
        method.addBodyLine("List<{0}> excelVOS = VgoEasyExcel.read(file, {0}.class);",excelVoType.getShortName());
        method.addBodyLine("List<{0}> {1} = mappings.from{2}s(excelVOS);",entityType.getShortName(),listEntityVar,excelVoType.getShortName());

        method.addBodyLine("for ({0} {1} : {1}s) '{'\n" +
                "            ServiceResult<{0}> insert = {1}Impl.insert({1});\n" +
                "            if (insert.isSuccess()) '{'\n" +
                "                ret = ret+1;\n" +
                "            '}'\n" +
                "        '}'\n" +
                "        return ResponseResult.success(ret,\"导入成功\"+ret+\"/\"+excelVOS.size());",entityType.getShortName(),entityType.getShortNameFirstLowCase());
        parentElement.addMethod(method);
    }


}
