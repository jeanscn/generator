package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethodEnum;
import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.annotations.ApiOperationDesc;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;
import org.mybatis.generator.custom.annotations.SystemLogDesc;

import static com.vgosoft.tool.core.VStringUtil.format;
import static org.mybatis.generator.custom.ConstantsUtil.SERVICE_RESULT;

public class DownloadElementGenerator extends AbstractControllerElementGenerator {

    public DownloadElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(SERVICE_RESULT);
        parentElement.addImportedType(entityType);
        parentElement.addImportedType("javax.servlet.http.HttpServletResponse");
        parentElement.addImportedType("org.springframework.util.Assert");

        final String methodPrefix = "download";
        Method method = createMethod(methodPrefix);

        Parameter idParameter = new Parameter(FullyQualifiedJavaType.getStringInstance(), "id");
        idParameter.addAnnotation("@PathVariable");
        idParameter.setRemark("唯一标识");
        method.addParameter(idParameter);
        Parameter typeParameter = new Parameter(FullyQualifiedJavaType.getStringInstance(), "type");
        typeParameter.addAnnotation("@PathVariable");
        typeParameter.setRemark("下载后展示方式：1-下载，0-浏览器中打开");
        method.addParameter(typeParameter);
        method.setReturnType(new FullyQualifiedJavaType("ResponseEntity<byte[]>"));
        method.addAnnotation("@PermitAll");
        method.addAnnotation(new SystemLogDesc("下载（预览）",introspectedTable),parentElement);
        RequestMappingDesc requestMappingDesc = new RequestMappingDesc("download/{type}/{id}", RequestMethodEnum.GET);
        method.addAnnotation(requestMappingDesc,parentElement);
        method.addAnnotation(new ApiOperationDesc("单个文件下载（预览）", "单个文件下载(预览)接口"),parentElement);
        commentGenerator.addMethodJavaDocLine(method, "单个文件下载(预览)接口");
        method.addException(new FullyQualifiedJavaType("java.lang.Exception"));
        method.addBodyLine("Assert.notNull(id, \"资源的id非法！\");");
        method.addBodyLine(format("ServiceResult<{0}> serviceResult = {1}.selectByPrimaryKey(id);", entityType.getShortName(),this.serviceBeanName));
        method.addBodyLine("if (serviceResult.hasResult()) {");
        method.addBodyLine(format("{0} {1} = serviceResult.getResult();", entityType.getShortName(),entityType.getShortNameFirstLowCase()));
        if (introspectedTable.getColumn(DefaultColumnNameEnum.BYTES.columnName()).isPresent()  && introspectedTable.hasBLOBColumns()) {
            method.addBodyLine("byte[] bytes = {0}.getBytes();", entityType.getShortNameFirstLowCase());
        } else {
            method.addBodyLine("File file = new File({0}.getFullPath());", entityType.getShortNameFirstLowCase());
            method.addBodyLine("if (!file.exists()) {");
            method.addBodyLine("return new ResponseEntity<>(HttpStatus.NOT_FOUND);");
            method.addBodyLine("}");
            method.addBodyLine("byte[] bytes = FileUtils.readFileToByteArray(file);");
            parentElement.addImportedType("java.io.File");
            parentElement.addImportedType("org.apache.commons.io.FileUtils");
        }
        method.addBodyLine(format("HttpHeaders headers = getHeaders({0}, Boolean.parseBoolean(type),bytes);", entityType.getShortNameFirstLowCase()));
        method.addBodyLine("return new ResponseEntity<>(bytes, headers, HttpStatus.OK);");
        method.addBodyLine("} else {");
        method.addBodyLine("return new ResponseEntity<>(HttpStatus.NOT_FOUND);");
        method.addBodyLine("}");
        parentElement.addMethod(method);
        parentElement.addImportedType("org.springframework.http.HttpHeaders");
        parentElement.addImportedType("org.springframework.http.HttpStatus");
        parentElement.addImportedType("org.springframework.http.ResponseEntity");
        parentElement.addImportedType("javax.annotation.security.PermitAll");
    }
}
