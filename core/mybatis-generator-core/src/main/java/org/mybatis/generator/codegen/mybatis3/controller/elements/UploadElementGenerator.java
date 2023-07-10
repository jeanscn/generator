package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethod;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.annotations.ApiOperationDesc;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;
import org.mybatis.generator.custom.annotations.SystemLogDesc;

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
        parentElement.addImportedType("org.apache.commons.lang3.StringUtils");

        final String methodPrefix = "upload";
        Method method = createMethod(methodPrefix);

        addCacheEvictAnnotation(method,parentElement);
        Parameter multipartFileParameter = new Parameter(multipartFile, "file");
        multipartFileParameter.addAnnotation("@RequestPart(\"file\")");
        multipartFileParameter.setRemark("文件上传对象");
        method.addParameter(multipartFileParameter);
        Parameter parameter;
        if (introspectedTable.getRules().isGenerateVoModel()) {
            parameter = new Parameter(entityVoType, entityVoType.getShortNameFirstLowCase());
        }else{
            parameter = new Parameter(entityType, entityType.getShortNameFirstLowCase());
        }
        parameter.setRemark("添加的数据对象");
        method.addParameter(parameter);
        responseResult.addTypeArgument(FullyQualifiedJavaType.getStringInstance());
        method.setReturnType(responseResult);
        method.addException(new FullyQualifiedJavaType("java.lang.Exception"));
        method.setExceptionRemark("上传处理异常，含IO读写异常");

        method.addAnnotation(new SystemLogDesc("上传记录",introspectedTable),parentElement);
        RequestMappingDesc requestMappingDesc = new RequestMappingDesc("upload", RequestMethod.POST);
        requestMappingDesc.addConsumes("MediaType.MULTIPART_FORM_DATA_VALUE");
        requestMappingDesc.addProduces("MediaType.APPLICATION_JSON_VALUE");
        method.addAnnotation(requestMappingDesc,parentElement);
        parentElement.addImportedType("org.springframework.http.MediaType");
        addSecurityPreAuthorize(method,methodPrefix,"上传");
        method.addAnnotation(new ApiOperationDesc("单个文件上传", "单个文件上传接口"),parentElement);

        commentGenerator.addMethodJavaDocLine(method, "单个文件上传");

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
