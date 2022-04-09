package org.mybatis.generator.codegen.mybatis3.controller.elements;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;

public class DownloadElementGenerator extends AbstractControllerElementGenerator {

    public DownloadElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType("com.vgosoft.core.adapter.ServiceResult");
        parentElement.addImportedType("com.vgosoft.web.respone.ResponseSimple");
        parentElement.addImportedType("com.vgosoft.web.respone.ResponseSimpleImpl");
        parentElement.addImportedType(entityType);
        parentElement.addImportedType("org.apache.commons.lang3.StringUtils");
        parentElement.addImportedType("org.springframework.web.multipart.MultipartFile");
        parentElement.addImportedType("org.springframework.http.MediaType");
        parentElement.addImportedType("org.apache.commons.lang3.StringUtils");
        parentElement.addImportedType("javax.servlet.http.HttpServletResponse");
        parentElement.addImportedType("org.springframework.util.Assert");
        parentElement.addImportedType("org.apache.commons.lang3.BooleanUtils");

        final String methodPrefix = "download";
        Method method = createMethod(methodPrefix);
        addSystemLogAnnotation(method,parentElement);

        Parameter idParameter = new Parameter(FullyQualifiedJavaType.getStringInstance(), "id");
        idParameter.addAnnotation("@PathVariable");
        method.addParameter(idParameter);
        Parameter typeParameter = new Parameter(FullyQualifiedJavaType.getStringInstance(), "type");
        typeParameter.addAnnotation("@PathVariable");
        method.addParameter(typeParameter);
        FullyQualifiedJavaType response = new FullyQualifiedJavaType("javax.servlet.http.HttpServletResponse");
        method.addParameter(new Parameter(response, "response"));
        StringBuilder sb = new StringBuilder();
        sb.append("@GetMapping(value = \"");
        sb.append(StringUtils.lowerCase(this.serviceBeanName));
        sb.append("/download/{type}/{id}\",produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)");
        method.addAnnotation(sb.toString());
        method.addBodyLine("try {");
        method.addBodyLine("Assert.notNull(id, \"资源的id非法！\");");
        sb.setLength(0);
        sb.append(entityType.getShortName()).append(" ");
        sb.append(entityFirstLowerShortName).append("=");
        sb.append(this.serviceBeanName).append(".selectByPrimaryKey(id);");
        method.addBodyLine(sb.toString());
        method.addBodyLine(String.format("Assert.notNull(%s, \"获取文件失败！\");", entityFirstLowerShortName));
        method.addBodyLine(String.format("byte[] bytes = %s.getBytes();", entityFirstLowerShortName));
        method.addBodyLine(String.format("String fileName = %s.getName() == null ? id : %s.getName();", entityFirstLowerShortName, entityFirstLowerShortName));
        method.addBodyLine("setResponseContent(fileName, response, bytes, BooleanUtils.toBoolean(type));");
        method.addBodyLine("} catch (Exception e) {");
        method.addBodyLine("response.setStatus(404);");
        method.addBodyLine("e.printStackTrace();");
        method.addBodyLine("}");

        parentElement.addMethod(method);
    }
}
