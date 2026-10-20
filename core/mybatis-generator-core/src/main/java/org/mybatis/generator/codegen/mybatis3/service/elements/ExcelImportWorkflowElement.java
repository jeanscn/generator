package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.config.VoExcelImportWorkflowConfiguration;
import org.mybatis.generator.internal.util.Mb3GenUtil;

/**
 * 生成两阶段 Excel 导入工作流的服务方法与业务扩展钩子。
 *
 * @author vgosoft
 */
public class ExcelImportWorkflowElement extends AbstractServiceElementGenerator {

    @Override
    public void addElements(TopLevelClass parentElement) {
        VoExcelImportWorkflowConfiguration configuration =
                tc.getVoGeneratorConfiguration().getVoExcelImportWorkflowConfiguration();
        FullyQualifiedJavaType importVo = configuration.getExcelImportType();
        FullyQualifiedJavaType previewType =
                new FullyQualifiedJavaType("com.vgosoft.plugins.excel.pojo.vo.VgoExcelImportPreviewVo");
        previewType.addTypeArgument(importVo);
        FullyQualifiedJavaType confirmType =
                new FullyQualifiedJavaType("com.vgosoft.plugins.excel.pojo.vo.VgoExcelImportConfirmVo");
        FullyQualifiedJavaType resultType =
                new FullyQualifiedJavaType("com.vgosoft.plugins.excel.pojo.vo.VgoExcelImportResultVo");
        FullyQualifiedJavaType multipartFile = new FullyQualifiedJavaType("org.springframework.web.multipart.MultipartFile");
        FullyQualifiedJavaType listType = FullyQualifiedJavaType.getNewListInstance();
        listType.addTypeArgument(importVo);

        addPreviewMethod(parentElement, configuration, importVo, previewType, multipartFile);
        addPreviewHook(parentElement, listType, previewType);
        addConfirmMethod(parentElement, configuration, confirmType, resultType);
        addConfirmHook(parentElement, confirmType, resultType);

        parentElement.addImportedType(importVo);
        parentElement.addImportedType(previewType);
        parentElement.addImportedType(confirmType);
        parentElement.addImportedType(resultType);
        parentElement.addImportedType(multipartFile);
        parentElement.addImportedType(listType);
        parentElement.addImportedType("com.vgosoft.plugins.excel.service.VgoFesodSheet");
        parentElement.addImportedType("com.vgosoft.plugins.excel.listener.DefaultReadListener");
        parentElement.addImportedType("java.io.IOException");
    }

    private void addPreviewMethod(TopLevelClass parentElement,
                                  VoExcelImportWorkflowConfiguration configuration,
                                  FullyQualifiedJavaType importVo,
                                  FullyQualifiedJavaType previewType,
                                  FullyQualifiedJavaType multipartFile) {
        Method preview = new Method("previewExcelImport");
        preview.setVisibility(JavaVisibility.PUBLIC);
        preview.addAnnotation("@Override");
        preview.setReturnType(previewType);
        Parameter file = new Parameter(multipartFile, "file");
        file.setRemark("待预览的 Excel 文件");
        preview.addParameter(file);
        preview.addException(new FullyQualifiedJavaType("java.io.IOException"));
        preview.setExceptionRemark("文件读取或 Excel 解析失败");
        commentGenerator.addMethodJavaDocLine(preview, "按配置限额解析 Excel 文件并交由业务服务生成预览");
        preview.addBodyLine("if (file == null || file.isEmpty()) {");
        preview.addBodyLine("throw new IllegalArgumentException(\"Excel 文件不能为空\");");
        preview.addBodyLine("}");
        preview.addBodyLine("VgoFesodSheet.BoundedReadResult<" + importVo.getShortName()
                + "> parsed = VgoFesodSheet.readBounded(");
        preview.addBodyLine("        file, " + importVo.getShortName() + ".class, new DefaultReadListener<>(),");
        preview.addBodyLine("        new VgoFesodSheet.ImportReadLimits("
                + configuration.getMaxInputBytes() + "L, "
                + configuration.getMaxExpandedBytes() + "L, "
                + configuration.getMaxEntryBytes() + "L, "
                + configuration.getMaxZipEntries() + ", "
                + configuration.getMaxRows() + ", "
                + configuration.getMaxErrorMessages() + "));");
        preview.addBodyLine(previewType.getShortName()
                + " result = createExcelImportPreview(parsed.rows(), parsed.fileSha256());");
        preview.addBodyLine("if (result == null || result.getRowCount() < 0 || result.getRowCount() > "
                + configuration.getMaxRows());
        preview.addBodyLine("        || result.getRows() == null || result.getRows().size() > "
                + configuration.getMaxRows());
        preview.addBodyLine("        || result.getErrors() == null || result.getErrors().size() > "
                + configuration.getMaxErrorMessages() + ") {");
        preview.addBodyLine("throw new IllegalStateException(\"Excel 导入预览结果超过配置限额或未返回有效结果\");");
        preview.addBodyLine("}");
        preview.addBodyLine("return result;");
        parentElement.addMethod(preview);
    }

    private void addPreviewHook(TopLevelClass parentElement,
                               FullyQualifiedJavaType listType,
                               FullyQualifiedJavaType previewType) {
        Method previewHook = new Method("createExcelImportPreview");
        previewHook.setVisibility(JavaVisibility.PROTECTED);
        previewHook.setReturnType(previewType);
        Parameter rows = new Parameter(listType, "rows");
        rows.setRemark("有界解析得到的导入行");
        previewHook.addParameter(rows);
        Parameter fileSha256 = new Parameter(FullyQualifiedJavaType.getStringInstance(), "fileSha256");
        fileSha256.setRemark("原始文件的 SHA-256 摘要");
        previewHook.addParameter(fileSha256);
        commentGenerator.addMethodJavaDocLine(previewHook,
                "由业务子类校验导入行并持久化预览凭证，不得在此执行业务数据提交");
        previewHook.addBodyLine("throw new UnsupportedOperationException(\"请在业务服务子类中实现 Excel 导入预览策略\");");
        parentElement.addMethod(previewHook);
    }

    private void addConfirmMethod(TopLevelClass parentElement,
                                  VoExcelImportWorkflowConfiguration configuration,
                                  FullyQualifiedJavaType confirmType,
                                  FullyQualifiedJavaType resultType) {
        Method confirm = new Method("confirmExcelImport");
        confirm.setVisibility(JavaVisibility.PUBLIC);
        confirm.addAnnotation("@Override");
        confirm.setReturnType(resultType);
        Parameter request = new Parameter(confirmType, "request");
        request.setRemark("导入预览确认凭证");
        confirm.addParameter(request);
        commentGenerator.addMethodJavaDocLine(confirm, "在事务边界内调用业务策略确认并提交 Excel 导入");
        Mb3GenUtil.addTransactionalAnnotation(parentElement, confirm, "READ_COMMITTED");
        confirm.addBodyLine("if (request == null || request.getPreviewToken() == null");
        confirm.addBodyLine("        || request.getPreviewToken().trim().isEmpty()) {");
        confirm.addBodyLine("throw new IllegalArgumentException(\"Excel 导入预览凭证不能为空\");");
        confirm.addBodyLine("}");
        confirm.addBodyLine(resultType.getShortName() + " result = doConfirmExcelImport(request);");
        confirm.addBodyLine("if (result == null || result.getImportedRows() < 0 || result.getImportedRows() > "
                + configuration.getMaxRows() + ") {");
        confirm.addBodyLine("throw new IllegalStateException(\"Excel 导入确认策略未返回有效结果\");");
        confirm.addBodyLine("}");
        confirm.addBodyLine("return result;");
        parentElement.addMethod(confirm);
    }

    private void addConfirmHook(TopLevelClass parentElement,
                               FullyQualifiedJavaType confirmType,
                               FullyQualifiedJavaType resultType) {
        Method confirmHook = new Method("doConfirmExcelImport");
        confirmHook.setVisibility(JavaVisibility.PROTECTED);
        confirmHook.setReturnType(resultType);
        Parameter confirmRequest = new Parameter(confirmType, "request");
        confirmRequest.setRemark("导入预览确认凭证");
        confirmHook.addParameter(confirmRequest);
        commentGenerator.addMethodJavaDocLine(confirmHook,
                "由业务子类在当前事务中校验并消费预览凭证，完成整批业务写入");
        confirmHook.addBodyLine("throw new UnsupportedOperationException(\"请在业务服务子类中实现 Excel 导入确认策略\");");
        parentElement.addMethod(confirmHook);
    }
}
