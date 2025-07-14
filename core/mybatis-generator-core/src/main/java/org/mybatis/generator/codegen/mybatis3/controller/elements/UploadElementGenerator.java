package org.mybatis.generator.codegen.mybatis3.controller.elements;

import static com.vgosoft.tool.core.VStringUtil.format;
import static org.mybatis.generator.custom.ConstantsUtil.MULTIPART_FILE;
import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_RESULT;
import static org.mybatis.generator.custom.ConstantsUtil.SERVICE_RESULT;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.annotations.ApiOperationDesc;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;
import org.mybatis.generator.custom.annotations.SystemLogDesc;

import com.vgosoft.core.constant.enums.core.RequestMethodEnum;
import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;

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

        final String methodPrefix = "upload";
        Method method = createMethod(methodPrefix);

        addCacheEvictAnnotation(method,parentElement);
        Parameter multipartFileParameter = new Parameter(multipartFile, "file");
        multipartFileParameter.addAnnotation("@RequestPart(\"file\")");
        multipartFileParameter.setRemark("文件上传对象");
        method.addParameter(multipartFileParameter);
        Parameter parameter;
        if (introspectedTable.getRules().isGenerateCreateVo()) {
            parameter = new Parameter(entityCreateVoType, entityCreateVoType.getShortNameFirstLowCase());
        } else if (introspectedTable.getRules().isGenerateVoModel()) {
            parameter = new Parameter(entityVoType, entityVoType.getShortNameFirstLowCase());
        } else {
            parameter = new Parameter(entityType, entityType.getShortNameFirstLowCase());
        }
        parameter.setRemark("添加的数据对象");
        method.addParameter(parameter);

        responseResult.addTypeArgument(introspectedTable.getRules().isGenerateVoModel()?entityVoType:entityType);
        method.setReturnType(responseResult);
        method.addException(new FullyQualifiedJavaType("java.lang.Exception"));
        method.setExceptionRemark("上传处理异常，含IO读写异常");

        method.addAnnotation(new SystemLogDesc("上传记录",introspectedTable),parentElement);
        RequestMappingDesc requestMappingDesc = new RequestMappingDesc("upload", RequestMethodEnum.POST);
        requestMappingDesc.addConsumes("MediaType.MULTIPART_FORM_DATA_VALUE");
        requestMappingDesc.addProduces("MediaType.APPLICATION_JSON_VALUE");
        method.addAnnotation(requestMappingDesc,parentElement);
        parentElement.addImportedType("org.springframework.http.MediaType");
        addSecurityPreAuthorize(method,methodPrefix,"上传");
        method.addAnnotation(new ApiOperationDesc("单个文件上传", "单个文件上传接口"),parentElement);
        commentGenerator.addMethodJavaDocLine(method, "单个文件上传");
        method.addBodyLine("if (file.isEmpty()) return ResponseResult.failure(ApiCodeEnum.FAIL_CUSTOM, \"上传文件为空\");");
        if (introspectedTable.getRules().isGenerateVoModel() || introspectedTable.getRules().isGenerateCreateVo()) {
            method.addBodyLine(format("{0} {1} = mappings.from{2}({3});", entityType.getShortName(),entityType.getShortNameFirstLowCase(),parameter.getType().getShortName(),parameter.getType().getShortNameFirstLowCase()));
        }
        if (introspectedTable.getColumn(DefaultColumnNameEnum.BYTES.columnName()).isPresent() && introspectedTable.hasBLOBColumns()) {
            method.addBodyLine(format("initBlobEntityFromMultipartFile({0},file);", entityType.getShortNameFirstLowCase()));
            method.addBodyLine(format("ServiceResult<{0}> serviceResult;", entityType.getShortName()));
            method.addBodyLine(format("if (VStringUtil.stringHasValue({0}.getId())) '{'", entityType.getShortNameFirstLowCase()));
            method.addBodyLine(format("serviceResult = {0}.updateByPrimaryKeyWithBLOBs({1});", serviceBeanName, entityType.getShortNameFirstLowCase()));
        } else {
            method.addBodyLine("String uuid = StringUtils.defaultIfEmpty({0}.getId(), StringUtils.defaultIfEmpty({0}.getModelTempId(), UUID.nextUUID()));",entityType.getShortNameFirstLowCase());
            method.addBodyLine(format("initDiskFileEntityFromMultipartFile({0},file,uuid);", entityType.getShortNameFirstLowCase()));
            method.addBodyLine("{0}.setModelTempId(uuid);", entityType.getShortNameFirstLowCase());
            parentElement.addImportedType("com.vgosoft.core.util.UUID");
            method.addBodyLine(format("ServiceResult<{0}> serviceResult;", entityType.getShortName()));
            method.addBodyLine(format("if (VStringUtil.stringHasValue({0}.getId())) '{'", entityType.getShortNameFirstLowCase()));
            method.addBodyLine(format("serviceResult = {0}.updateByPrimaryKey({1});", serviceBeanName, entityType.getShortNameFirstLowCase()));
            parentElement.addImportedType("org.apache.commons.lang3.StringUtils");
        }
        method.addBodyLine("} else {");
        method.addBodyLine(format("serviceResult = {0}.insert({1});", serviceBeanName, entityType.getShortNameFirstLowCase()));
        method.addBodyLine("}");
        method.addBodyLine("if (serviceResult.hasResult()) {");
        method.addBodyLine("return ResponseResult.success(mappings.to{0}(serviceResult.getResult()),\"上传成功！\");",entityVoType.getShortName());
        method.addBodyLine("} else {");
        method.addBodyLine(format("return ResponseResult.failure(ApiCodeEnum.FAIL_CUSTOM, serviceResult.getMessage());"));
        method.addBodyLine("}");
        parentElement.addMethod(method);
    }
}
