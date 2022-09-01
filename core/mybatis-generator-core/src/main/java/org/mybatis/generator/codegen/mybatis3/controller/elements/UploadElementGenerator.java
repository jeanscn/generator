package org.mybatis.generator.codegen.mybatis3.controller.elements;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;

import static com.vgosoft.tool.core.VStringUtil.format;
import static org.mybatis.generator.custom.ConstantsUtil.*;

public class UploadElementGenerator extends AbstractControllerElementGenerator {

    public UploadElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        FullyQualifiedJavaType multipartFile = new FullyQualifiedJavaType(MULTIPART_FILE);
        FullyQualifiedJavaType responseResult = new FullyQualifiedJavaType(RESPONSE_RESULT);
        parentElement.addImportedType(SERVICE_RESULT);
        parentElement.addImportedType(responseResult);
        parentElement.addImportedType(entityType);
        if (introspectedTable.getRules().isGenerateVoModel()) {
            parentElement.addImportedType(entityVoType);
            parentElement.addImportedType(entityMappings);
        }
        parentElement.addImportedType(multipartFile);
        parentElement.addImportedType("org.springframework.util.Assert");
        parentElement.addImportedType("org.springframework.http.MediaType");
        parentElement.addImportedType("org.apache.commons.lang3.StringUtils");

        final String methodPrefix = "upload";
        Method method = createMethod(methodPrefix);
        addSystemLogAnnotation(method, parentElement);

        Parameter multipartFileParameter = new Parameter(multipartFile, "file");
        multipartFileParameter.addAnnotation("@RequestPart(\"file\")");
        method.addParameter(multipartFileParameter);
        if (introspectedTable.getRules().isGenerateVoModel()) {
            method.addParameter(new Parameter(entityVoType, entityVoType.getShortNameFirstLowCase()));
        }else{
            method.addParameter(new Parameter(entityType, entityType.getShortNameFirstLowCase()));
        }
        responseResult.addTypeArgument(FullyQualifiedJavaType.getStringInstance());
        method.setReturnType(responseResult);
        method.addException(new FullyQualifiedJavaType("java.lang.Exception"));
        String sb = "@PostMapping(value = \"" +
                StringUtils.lowerCase(this.serviceBeanName) +
                "/upload\",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)";
        method.addAnnotation(sb);
        if (introspectedTable.getRules().isGenerateVoModel()) {
            method.addBodyLine(format("{0} {1} = mappings.from{2}({3});", entityType.getShortName(),entityType.getShortNameFirstLowCase(),entityVoType.getShortName(),entityVoType.getShortNameFirstLowCase()));
        }
        method.addBodyLine(format("initBlobEntityFromMultipartFile({0},file);", entityType.getShortNameFirstLowCase()));
        method.addBodyLine(format("ServiceResult<{0}> serviceResult;", entityType.getShortName()));
        method.addBodyLine(format("if (StringUtils.isNotBlank({0}.getId())) '{'", entityType.getShortNameFirstLowCase()));
        method.addBodyLine(format("serviceResult = {0}.updateByPrimaryKeySelective({1});", serviceBeanName, entityType.getShortNameFirstLowCase()));
        method.addBodyLine("} else {");
        method.addBodyLine(format("serviceResult = {0}.insert({1});", serviceBeanName, entityType.getShortNameFirstLowCase()));
        method.addBodyLine("}");
        method.addBodyLine("if (!serviceResult.isSuccess()) {");
        method.addBodyLine("return ResponseResult.failure(ApiCodeEnum.FAIL,serviceResult.getMessage());");
        method.addBodyLine("} else {");
        method.addBodyLine(format("return ResponseResult.success({0}.getId(),\"上传成功！\");",entityType.getShortNameFirstLowCase()));
        method.addBodyLine("}");
        parentElement.addMethod(method);
    }
}
