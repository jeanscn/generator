/**
 * Copyright 2006-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mybatis.generator.custom.controllerGenerator;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.custom.htmlGenerator.GenerateUtils;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;

public class GenerateJavaController {

    private final IntrospectedTable introspectedTable;
    private final FullyQualifiedJavaType entityType;
    private final FullyQualifiedJavaType exampleType;
    private final String entityLowerShortName;
    private final String entityFirstLowerShortName;
    private final FullyQualifiedJavaType responseSimple;
    private final String serviceBeanName;
    private final CommentGenerator commentGenerator;
    private final Parameter entityParameter;
    private final String viewPath;
    private final String entityNameKey;

    public GenerateJavaController(IntrospectedTable introspectedTable) {
        this.introspectedTable = introspectedTable;
        this.entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        this.exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        this.entityLowerShortName = entityType.getShortName().toLowerCase();
        this.responseSimple = new FullyQualifiedJavaType("com.vgosoft.web.respone.ResponseSimple");
        this.serviceBeanName = introspectedTable.getControllerBeanName();
        commentGenerator = introspectedTable.getContext().getCommentGenerator();
        this.entityParameter = new Parameter(entityType, JavaBeansUtil.getFirstCharacterLowercase(entityType.getShortName()));
        this.entityParameter.addAnnotation("@RequestBody");
        this.entityFirstLowerShortName = JavaBeansUtil.getFirstCharacterLowercase(entityType.getShortName());
        this.viewPath = introspectedTable.getMyBatis3HtmlMapperViewName();
        this.entityNameKey = GenerateUtils.getEntityKeyStr(introspectedTable);
    }

    /**
     * viewXXXXXX
     */
    public Method viewGenerate() {
        final String methodPrefix = "view";
        StringBuilder sb = new StringBuilder();
        Method method = createMethod(methodPrefix);
        Parameter parameter = new Parameter(new FullyQualifiedJavaType("String"), "id");
        parameter.addAnnotation("@RequestParam(required = false)");
        method.addParameter(parameter);
        Parameter viewStatus = new Parameter(new FullyQualifiedJavaType("String"), "viewStatus");
        viewStatus.addAnnotation("@RequestParam(required = false)");
        method.addParameter(viewStatus);
        Parameter prefix = new Parameter(new FullyQualifiedJavaType("String"), "prefix");
        prefix.addAnnotation("@RequestParam(required = false)");
        method.addParameter(prefix);
        method.setReturnType(new FullyQualifiedJavaType("ModelAndView"));
        addControllerMapping(method, methodPrefix, "get");
        //函数体
        sb.append("ModelAndView mv = new ModelAndView();");
        method.addBodyLine(sb.toString());
        method.addBodyLine("try {");
        method.addBodyLine("if (id != null) {");
        sb.setLength(0);
        sb.append(entityType.getShortName()).append(" ").append(entityLowerShortName);
        sb.append(" = ");
        sb.append(serviceBeanName).append(".selectByPrimaryKey(id);");
        method.addBodyLine(sb.toString());
        sb.setLength(0);
        sb.append("mv.addObject(\"");
        sb.append(this.entityNameKey);
        sb.append("\",").append(entityLowerShortName).append(");");
        method.addBodyLine(sb.toString());
        method.addBodyLine("}");
        method.addBodyLine("mv.addObject(\"viewStatus\", Objects.requireNonNullElse(viewStatus, 1));");
        String[] split = StringUtils.split(viewPath, "/");
        split[split.length - 1] = "{0}" + split[split.length - 1];
        String mString = StringUtils.join(split, "/");
        method.addBodyLine("if (prefix != null) {");
        sb.setLength(0);
        sb.append("mv.setViewName(\"");
        String replace = mString.replace("{0}", "\"+prefix+\"");
        sb.append(replace);
        sb.append("\");");
        method.addBodyLine(sb.toString());
        method.addBodyLine("} else {");
        sb.setLength(0);
        sb.append("mv.setViewName(\"");
        sb.append(viewPath);
        sb.append("\");");
        method.addBodyLine(sb.toString());
        method.addBodyLine("}");
        method.addBodyLine("} catch (Exception e) {");
        method.addBodyLine("mv.setViewName(\"page/500\");");
        method.addBodyLine("mv.addObject(\"error\", e.getMessage());");
        method.addBodyLine("}");
        method.addBodyLine("return mv;");
        return method;
    }

    /**
     * getXXXXXX
     */
    public Method getGenerate() {
        final String methodPrefix = "get";
        StringBuilder sb = new StringBuilder();
        Method method = createMethod(methodPrefix);
        Parameter parameter = new Parameter(new FullyQualifiedJavaType("String"), "id");
        parameter.addAnnotation("@PathVariable");
        method.addParameter(parameter);
        method.setReturnType(responseSimple);
        addControllerMapping(method, "{id}", "get");
        //函数体
        method.addBodyLine("ResponseSimple responseSimple = new ResponseSimpleImpl();");
        method.addBodyLine("try {");
        sb.setLength(0);
        sb.append(entityType.getShortName()).append(" ").append(entityFirstLowerShortName).append(" = ");
        sb.append(serviceBeanName).append(".selectByPrimaryKey(id);");
        method.addBodyLine(sb.toString());
        sb.setLength(0);
        sb.append("responseSimple.addAttribute(\"");
        sb.append(this.entityNameKey);
        sb.append("\", ");
        sb.append(entityFirstLowerShortName).append(");");
        method.addBodyLine(sb.toString());
        addExceptionAndreturn(method);
        return method;
    }

    /**
     * listXXXXXX
     */
    public Method listGenerate() {
        final String methodPrefix = "list";
        StringBuilder sb = new StringBuilder();
        Method method = createMethod(methodPrefix);
        method.addParameter(new Parameter(entityType, entityFirstLowerShortName));
        method.setReturnType(responseSimple);
        addControllerMapping(method, "", "get");
        method.addBodyLine("ResponseList responseSimple = new ResponseSimpleList();");
        sb.setLength(0);
        sb.append(exampleType.getShortName()).append(" example = new ");
        sb.append(exampleType.getShortName()).append("();");
        method.addBodyLine(sb.toString());
        method.addBodyLine("try {");
        sb.setLength(0);
       /* sb.append("if (").append(entityFirstLowerShortName).append(" != null) {");
        method.addBodyLine(sb.toString());
        sb.setLength(0);
        sb.append("List<Field> list = getFieldsIsNotEmpty(").append(entityFirstLowerShortName).append(");");
        method.addBodyLine(sb.toString());
        sb.setLength(0);
        method.addBodyLine("if (list.size() > 0) {");
        method.addBodyLine(sb.toString());
        method.addBodyLine("}");
        method.addBodyLine("}");*/
        sb.append("List<").append(entityType.getShortName()).append("> ");
        sb.append(entityFirstLowerShortName).append("s");
        sb.append(" = ").append(serviceBeanName).append(".selectByExample(example);");
        method.addBodyLine(sb.toString());
        sb.setLength(0);
        sb.append("responseSimple.setList(").append(entityFirstLowerShortName).append("s);");
        method.addBodyLine(sb.toString());
        addExceptionAndreturn(method);
        return method;
    }

    public Method uploadGenerate() {
        final String methodPrefix = "upload";
        Method method = createMethod(methodPrefix);
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
        addExceptionAndreturn(method);
        return method;
    }

    public Method downloadGenerate() {
        final String methodPrefix = "download";
        Method method = createMethod(methodPrefix);
        Parameter idParameter = new Parameter(new FullyQualifiedJavaType("String"), "id");
        idParameter.addAnnotation("@PathVariable");
        method.addParameter(idParameter);
        Parameter typeParameter = new Parameter(new FullyQualifiedJavaType("String"), "type");
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
        return method;
    }

    /**
     * createXXXXXX
     */
    public Method createGenerate() {
        final String methodPrefix = "create";
        StringBuilder sb = new StringBuilder();
        Method method = createMethod(methodPrefix);
        method.addParameter(entityParameter);
        method.setReturnType(responseSimple);
        addControllerMapping(method, "", "post");
        method.addBodyLine("ResponseSimple responseSimple = new ResponseSimpleImpl();");
        method.addBodyLine("try {");
        sb.setLength(0);
        sb.append("insert(").append(serviceBeanName).append(",")
                .append(entityFirstLowerShortName)
                .append(", responseSimple);");
        method.addBodyLine(sb.toString());
        addExceptionAndreturn(method);
        return method;
    }

    /**
     * updateXXXXXX
     */
    public Method updateGenerate() {
        final String methodPrefix = "update";
        StringBuilder sb = new StringBuilder();
        Method method = createMethod(methodPrefix);
        method.addParameter(entityParameter);
        method.setReturnType(responseSimple);
        addControllerMapping(method, "", "put");
        method.addBodyLine("ResponseSimple responseSimple = new ResponseSimpleImpl();");
        method.addBodyLine("try {");
        sb.setLength(0);
        sb.append("int rows =  ").append(serviceBeanName).append(".updateByPrimaryKey(");
        sb.append(entityFirstLowerShortName).append(");");
        method.addBodyLine(sb.toString());
        sb.setLength(0);
        sb.append("responseSimple.addAttribute(\"rows\", String.valueOf(rows));");
        method.addBodyLine(sb.toString());
        addExceptionAndreturn(method);
        return method;
    }

    /**
     * deleteXXXXXX
     */
    public Method deleteGenerate() {
        final String methodPrefix = "delete";
        StringBuilder sb = new StringBuilder();
        Method method = createMethod(methodPrefix);
        Parameter parameter = new Parameter(new FullyQualifiedJavaType("String"), "id");
        parameter.addAnnotation("@PathVariable");
        method.addParameter(parameter);
        method.setReturnType(responseSimple);
        addControllerMapping(method, "{id}", "delete");
        method.addBodyLine("ResponseSimple responseSimple = new ResponseSimpleImpl();");
        method.addBodyLine("try {");
        sb.setLength(0);
        sb.append("int rows =  ").append(serviceBeanName).append(".deleteByPrimaryKey(id);");
        method.addBodyLine(sb.toString());
        sb.setLength(0);
        sb.append("responseSimple.addAttribute(\"rows\", String.valueOf(rows));");
        method.addBodyLine(sb.toString());
        addExceptionAndreturn(method);
        return method;
    }

    /**
     * deleteBatchXXXXXX
     */
    public Method deleteBatchGenerate() {
        final String methodPrefix = "deleteBatch";
        Method method = createMethod(methodPrefix);
        Parameter parameter = new Parameter(new FullyQualifiedJavaType("List<String>"), "ids");
        parameter.addAnnotation("@RequestBody");
        method.addParameter(parameter);
        method.setReturnType(responseSimple);
        addControllerMapping(method, null, "delete");
        method.addBodyLine("ResponseSimple responseSimple = new ResponseSimpleImpl();");
        StringBuilder sb = new StringBuilder();
        sb.append(exampleType.getShortName()).append(" example = new ");
        sb.append(exampleType.getShortName()).append("();");
        method.addBodyLine(sb.toString());
        method.addBodyLine("example.createCriteria().andIdIn(ids);");
        method.addBodyLine("try {");
        sb.setLength(0);
        sb.append("int rows =  ").append(serviceBeanName).append(".deleteByExample(example);");
        method.addBodyLine(sb.toString());
        sb.setLength(0);
        sb.append("responseSimple.addAttribute(\"rows\", String.valueOf(rows));");
        method.addBodyLine(sb.toString());
        addExceptionAndreturn(method);
        return method;
    }

    private Method createMethod(String methodPrefix) {
        Method method = new Method(methodPrefix + entityType.getShortName());
        method.setVisibility(JavaVisibility.PUBLIC);
        commentGenerator.addGeneralMethodComment(method, introspectedTable);
        return method;
    }

    /**
     * 内部方法
     * 生成Controller时添加方法的catch和return语句
     *
     * @param method
     */
    private void addExceptionAndreturn(Method method) {
        method.addBodyLine("} catch (Exception e) {");
        method.addBodyLine("setExceptionResponse(responseSimple, e);");
        method.addBodyLine("}");
        method.addBodyLine("return responseSimple;");
    }

    private void addControllerMapping(Method method, String otherKey, String methodType) {
        StringBuilder sb = new StringBuilder();
        String mappingPrefix = JavaBeansUtil.getFirstCharacterUppercase(methodType);
        sb.append("@").append(mappingPrefix).append("Mapping(value = \"");
        sb.append(this.serviceBeanName);
        if (StringUtility.stringHasValue(otherKey)) {
            sb.append("/" + otherKey + "\")");
        } else {
            sb.append("\")");
        }
        method.addAnnotation(sb.toString());
    }
}
