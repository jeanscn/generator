package org.mybatis.generator.codegen.mybatis3.controller.elements;

import static com.vgosoft.tool.core.VStringUtil.format;
import static org.mybatis.generator.custom.ConstantsUtil.*;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;

public class UploadElementGenerator extends AbstractControllerElementGenerator {

    public UploadElementGenerator() {
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
        parentElement.addImportedType("javax.servlet.http.HttpServletResponse");
        parentElement.addImportedType("org.springframework.util.Assert");
        parentElement.addImportedType("org.apache.commons.lang3.BooleanUtils");

        final String methodPrefix = "upload";
        Method method = createMethod(methodPrefix);
        addSystemLogAnnotation(method,parentElement);

        FullyQualifiedJavaType multipartFile = new FullyQualifiedJavaType("org.springframework.web.multipart.MultipartFile");
        Parameter multipartFileParameter = new Parameter(multipartFile, "file");
        multipartFileParameter.addAnnotation("@RequestPart(\"file\")");
        method.addParameter(multipartFileParameter);
        method.addParameter(new Parameter(entityType, entityFirstLowerShortName));
        method.setReturnType(responseSimple);
        StringBuilder sb = new StringBuilder();
        sb.append("@PostMapping(value = \"");
        sb.append(StringUtils.lowerCase(this.serviceBeanName));
        sb.append("/upload\",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)");
        method.addAnnotation(sb.toString());
        method.addBodyLine("ResponseSimple responseSimple = new ResponseSimpleImpl();");
        method.addBodyLine("try {");
        method.addBodyLine(format("ServiceResult<{0}> serviceResult;",entityType.getShortName()));
        method.addBodyLine(format("initBlobEntityFromMultipartFile({0},file);",entityFirstLowerShortName));
        method.addBodyLine(format("if (StringUtils.isNotBlank({0}.getId())) '{'",entityFirstLowerShortName));
        method.addBodyLine(format("serviceResult = {0}.updateByPrimaryKey({1});",serviceBeanName,entityFirstLowerShortName));
        method.addBodyLine("} else {");
        method.addBodyLine(format("serviceResult = {0}.insert({1});",serviceBeanName,entityFirstLowerShortName));
        method.addBodyLine("}");
        method.addBodyLine("if (!serviceResult.isSuccess()) {");
        method.addBodyLine("responseSimple.setStatus(1);");
        method.addBodyLine("responseSimple.setMessage(serviceResult.getMessage());");
        method.addBodyLine(" } else {");
        method.addBodyLine("responseSimple.addAttribute(\"id\"," + entityFirstLowerShortName + ".getId());");
        method.addBodyLine("responseSimple.setMessage(\"上传成功！\");");
        method.addBodyLine("}");
        addExceptionAndReturn(method);

        parentElement.addMethod(method);
    }
}
