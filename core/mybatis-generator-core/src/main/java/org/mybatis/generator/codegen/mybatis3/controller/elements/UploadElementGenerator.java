package org.mybatis.generator.codegen.mybatis3.controller.elements;

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
        method.addBodyLine("initBlobEntityFromMultipartFile(" + entityFirstLowerShortName + ",file);");
        method.addBodyLine("int rows;");
        method.addBodyLine("if (StringUtils.isNotBlank(" + entityFirstLowerShortName + ".getId())) {");
        method.addBodyLine("rows = " + serviceBeanName + ".updateByPrimaryKey(" + entityFirstLowerShortName + ");");
        method.addBodyLine("} else {");
        method.addBodyLine("rows = " + serviceBeanName + ".insert(" + entityFirstLowerShortName + ");");
        method.addBodyLine("}");
        method.addBodyLine("if (rows < 1) {");
        method.addBodyLine("responseSimple.setMessage(\"上传失败！\");");
        method.addBodyLine(" } else {");
        method.addBodyLine("responseSimple.addAttribute(\"rows\", String.valueOf(rows));");
        method.addBodyLine("responseSimple.addAttribute(\"id\"," + entityFirstLowerShortName + ".getId());");
        method.addBodyLine("responseSimple.setMessage(\"上传成功！\");");
        method.addBodyLine("}");
        addExceptionAndReturn(method);

        parentElement.addMethod(method);
    }
}
