package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethodEnum;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;
import org.mybatis.generator.custom.annotations.SystemLogDesc;
import org.mybatis.generator.custom.annotations.swagger3.OperationDesc;
import org.mybatis.generator.custom.enums.ReturnTypeEnum;

import static org.mybatis.generator.custom.ConstantsUtil.MULTIPART_FILE;

/**
 * 生成 Excel 两阶段导入的预览与确认接口。
 *
 * @author vgosoft-agent
 * @author vgosoft
 */
public class ImportWorkflowElementGenerator extends AbstractControllerElementGenerator {

    @Override
    public void addElements(TopLevelClass parentElement) {
        addPreviewElement(parentElement);
        addConfirmElement(parentElement);
    }

    private void addPreviewElement(TopLevelClass parentElement) {
        FullyQualifiedJavaType multipartFile = new FullyQualifiedJavaType(MULTIPART_FILE);
        FullyQualifiedJavaType previewType =
                new FullyQualifiedJavaType("com.vgosoft.plugins.excel.pojo.vo.VgoExcelImportPreviewVo");
        previewType.addTypeArgument(entityExcelImportVoType);
        Method method = createMethod("importPreview");
        method.setReturnType(getResponseResult(ReturnTypeEnum.RESPONSE_RESULT_MODEL, previewType, parentElement));
        method.addException(new FullyQualifiedJavaType("java.io.IOException"));

        Parameter file = new Parameter(multipartFile, "file");
        file.addAnnotation("@RequestPart(\"file\")");
        file.setRemark("待预览的 Excel 文件");
        method.addParameter(file);
        method.addAnnotation(new SystemLogDesc("Excel 导入预览", introspectedTable), parentElement);
        RequestMappingDesc mapping = new RequestMappingDesc("import/preview", RequestMethodEnum.POST);
        mapping.addConsumes("MediaType.MULTIPART_FORM_DATA_VALUE");
        mapping.addProduces("MediaType.APPLICATION_JSON_VALUE");
        method.addAnnotation(mapping, parentElement);
        addSecurityPreAuthorize(method, "import", "导入预览");
        method.addAnnotation(new OperationDesc("Excel 导入预览", "解析并校验 Excel 导入内容"), parentElement);
        commentGenerator.addMethodJavaDocLine(method, "预览 Excel 导入内容");

        method.addBodyLine("if (file == null || file.isEmpty()) {");
        method.addBodyLine("return ResponseResult.failure(ApiCodeEnum.FAIL_VALIDATION, \"请选择有效的 Excel 文件\");");
        method.addBodyLine("}");
        method.addBodyLine("{0} preview = {1}.previewExcelImport(file);",
                previewType.getShortName(), serviceBeanName);
        method.addBodyLine("if (preview == null) {");
        method.addBodyLine("return ResponseResult.failure(ApiCodeEnum.FAIL_VALIDATION, \"Excel 导入预览未返回结果\");");
        method.addBodyLine("}");
        method.addBodyLine("return ResponseResult.success(preview);");

        parentElement.addImportedType(multipartFile);
        parentElement.addImportedType("org.springframework.http.MediaType");
        parentElement.addImportedType("com.vgosoft.core.adapter.ResponseResult");
        parentElement.addImportedType("com.vgosoft.core.constant.enums.core.ApiCodeEnum");
        parentElement.addImportedType("com.vgosoft.plugins.excel.pojo.vo.VgoExcelImportPreviewVo");
        parentElement.addImportedType(entityExcelImportVoType);
        parentElement.addMethod(method);
    }

    private void addConfirmElement(TopLevelClass parentElement) {
        FullyQualifiedJavaType confirmType =
                new FullyQualifiedJavaType("com.vgosoft.plugins.excel.pojo.vo.VgoExcelImportConfirmVo");
        FullyQualifiedJavaType resultType =
                new FullyQualifiedJavaType("com.vgosoft.plugins.excel.pojo.vo.VgoExcelImportResultVo");
        Method method = createMethod("importConfirm");
        method.setReturnType(getResponseResult(ReturnTypeEnum.RESPONSE_RESULT_MODEL, resultType, parentElement));

        Parameter request = new Parameter(confirmType, "request");
        request.addAnnotation("@Valid");
        request.addAnnotation("@RequestBody");
        request.setRemark("导入预览确认凭证");
        method.addParameter(request);
        method.addAnnotation(new SystemLogDesc("Excel 导入确认", introspectedTable), parentElement);
        RequestMappingDesc mapping = new RequestMappingDesc("import/confirm", RequestMethodEnum.POST);
        mapping.addConsumes("MediaType.APPLICATION_JSON_VALUE");
        mapping.addProduces("MediaType.APPLICATION_JSON_VALUE");
        method.addAnnotation(mapping, parentElement);
        addSecurityPreAuthorize(method, "import", "导入确认");
        method.addAnnotation(new OperationDesc("Excel 导入确认", "提交已校验的 Excel 导入预览"), parentElement);
        commentGenerator.addMethodJavaDocLine(method, "确认 Excel 导入预览");

        method.addBodyLine("{0} result = {1}.confirmExcelImport(request);",
                resultType.getShortName(), serviceBeanName);
        method.addBodyLine("if (result == null || result.getImportedRows() < 0) {");
        method.addBodyLine("return ResponseResult.failure(ApiCodeEnum.FAIL_VALIDATION, \"Excel 导入确认未返回有效结果\");");
        method.addBodyLine("}");
        method.addBodyLine("return ResponseResult.success(result);");

        parentElement.addImportedType(confirmType);
        parentElement.addImportedType(resultType);
        parentElement.addImportedType("jakarta.validation.Valid");
        parentElement.addImportedType("org.springframework.web.bind.annotation.RequestBody");
        parentElement.addImportedType("org.springframework.http.MediaType");
        parentElement.addImportedType("com.vgosoft.core.adapter.ResponseResult");
        parentElement.addImportedType("com.vgosoft.core.constant.enums.core.ApiCodeEnum");
        parentElement.addMethod(method);
    }
}
