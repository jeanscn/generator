package org.mybatis.generator.codegen.mybatis3.controller.elements;

import static com.vgosoft.tool.core.VStringUtil.format;
import static org.mybatis.generator.custom.ConstantsUtil.*;

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
        parentElement.addImportedType(SERVICE_RESULT);
        parentElement.addImportedType(RESPONSE_SIMPLE);
        parentElement.addImportedType(RESPONSE_SIMPLE_IMPL);
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
        method.addBodyLine(format("ServiceResult<{0}> serviceResult = {1}.selectByPrimaryKey(id);", entityType.getShortName(),this.serviceBeanName));
        method.addBodyLine(format("{0} {1} = serviceResult.getResult();", entityType.getShortName(),entityFirstLowerShortName));
        method.addBodyLine(format("Assert.notNull({0}, \"获取文件失败！\");", entityFirstLowerShortName));
        method.addBodyLine(format("byte[] bytes = {0}.getBytes();", entityFirstLowerShortName));
        method.addBodyLine(format("String fileName = {0}.getName() == null ? id : {1}.getName();", entityFirstLowerShortName, entityFirstLowerShortName));
        method.addBodyLine("setResponseContent(fileName, response, bytes, BooleanUtils.toBoolean(type));");
        method.addBodyLine("} catch (Exception e) {");
        method.addBodyLine("response.setStatus(404);");
        method.addBodyLine("e.printStackTrace();");
        method.addBodyLine("}");

        parentElement.addMethod(method);
    }
}
