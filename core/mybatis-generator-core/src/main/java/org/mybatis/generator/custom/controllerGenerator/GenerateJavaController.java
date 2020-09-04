/**
 *    Copyright 2006-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.custom.controllerGenerator;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;

public class GenerateJavaController {

    private IntrospectedTable introspectedTable;
    private FullyQualifiedJavaType entityType;
    private FullyQualifiedJavaType exampleType;
    private String  entityLowerShortName;
    private String  entityFirstLowerShortName;
    private FullyQualifiedJavaType responseSimple;
    private String serviceBeanName;
    private String serviceInterfaceName;
    CommentGenerator commentGenerator;
    private Parameter entityParamter;

    public GenerateJavaController(IntrospectedTable introspectedTable) {
        this.introspectedTable = introspectedTable;
        this.entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        this.exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        this.entityLowerShortName = entityType.getShortName().toLowerCase();
        this.responseSimple = new FullyQualifiedJavaType("com.vgosoft.web.ResponseSimple");
        this.serviceBeanName = introspectedTable.getControllerBeanName();
        this.serviceInterfaceName = "I"+entityType.getShortName();
        commentGenerator = introspectedTable.getContext().getCommentGenerator();
        this.entityParamter = new Parameter(entityType, JavaBeansUtil.getFirstCharacterLowercase(entityType.getShortName()));
        this.entityParamter.addAnnotation("@RequestBody");
        this.entityFirstLowerShortName = JavaBeansUtil.getFirstCharacterLowercase(entityType.getShortName());
    }

    /**
    * viewXXXXXX
    * */
    public Method viewGenerate() {
        final String methodPrefix = "view";
        StringBuilder sb = new StringBuilder();
        Method method = ctreateMethod(methodPrefix);
        Parameter parameter = new Parameter(new FullyQualifiedJavaType("String"), "id");
        parameter.addAnnotation("@RequestParam(required = false)");
        method.addParameter(parameter);
        method.setReturnType(new FullyQualifiedJavaType("ModelAndView"));
        addControllerMapping(method,methodPrefix,"get");
        //函数体
        introspectedTable.getMyBatis3HtmlMapperViewName();
        sb.append("ModelAndView mv = new ModelAndView(\"");
        sb.append(introspectedTable.getMyBatis3HtmlMapperViewName());
        sb.append("\");");
        method.addBodyLine(sb.toString());
        method.addBodyLine("try {");
        method.addBodyLine("if (id != null) {");
        sb.setLength(0);
        sb.append(entityType.getShortName()).append(" ").append(entityLowerShortName);
        sb.append(" = ");
        sb.append(serviceBeanName).append(".selectByPrimaryKey(id);");
        method.addBodyLine(sb.toString());
        sb.setLength(0);
        sb.append("mv.addObject(\"entity\",").append(entityLowerShortName).append(");");
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
     * */
    public Method getGenerate() {
        final String methodPrefix = "get";
        StringBuilder sb = new StringBuilder();
        Method method = ctreateMethod(methodPrefix);
        Parameter parameter = new Parameter(new FullyQualifiedJavaType("String"), "id");
        parameter.addAnnotation("@PathVariable");
        method.addParameter(parameter);
        method.setReturnType(responseSimple);
        addControllerMapping(method,"{id}","get");
        //函数体
        method.addBodyLine("ResponseSimple responseSimple = new ResponseSimpleImpl();");
        method.addBodyLine("try {");
        sb.setLength(0);
        sb.append(entityType.getShortName()).append(" ").append(entityFirstLowerShortName).append(" = ");
        sb.append(serviceBeanName).append(".selectByPrimaryKey(id);");
        method.addBodyLine(sb.toString());
        sb.setLength(0);
        sb.append("responseSimple.addAttribute(\"entity\", ").append(entityFirstLowerShortName).append(");");
        method.addBodyLine(sb.toString());
        addExceptionAndreturn(method);
        return method;
    }

    public Method listGenerate() {
        final String methodPrefix = "list";
        StringBuilder sb = new StringBuilder();
        Method method = ctreateMethod(methodPrefix);
        method.addParameter(new Parameter(entityType, entityFirstLowerShortName));
        method.setReturnType(responseSimple);
        addControllerMapping(method,"","get");
        method.addBodyLine("ResponseSimpleList responseSimple = new ResponseSimpleList();");
        sb.setLength(0);
        sb.append(exampleType.getShortName()).append(" example = new ");
        sb.append(exampleType.getShortName()).append("();");
        method.addBodyLine(sb.toString());
        method.addBodyLine("try {");
        sb.setLength(0);
        sb.append("if (").append(entityFirstLowerShortName).append(" != null) {");
        method.addBodyLine(sb.toString());
        sb.setLength(0);
        sb.append("List<Field> list = getFieldsIsNotEmpty(").append(entityFirstLowerShortName).append(");");
        method.addBodyLine(sb.toString());
        sb.setLength(0);
        method.addBodyLine("if (list.size() > 0) {");
        method.addBodyLine(sb.toString());
        method.addBodyLine("}");
        method.addBodyLine("}");
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

    private Method ctreateMethod(String methodPrefix) {
        Method method = new Method(methodPrefix + entityType.getShortName());
        method.setVisibility(JavaVisibility.PUBLIC);
        commentGenerator.addGeneralMethodComment(method, introspectedTable);
        return method;
    }

    private void addControllerMapping(Method method, String otherKey, String methodType) {
        StringBuilder sb = new StringBuilder();
        String mappingPrefix = JavaBeansUtil.getFirstCharacterUppercase(methodType);
        sb.append("@").append(JavaBeansUtil.getFirstCharacterUppercase(methodType)).append("Mapping(value = \"");
        sb.append(this.serviceBeanName);
        if (StringUtility.stringHasValue(otherKey)) {
            sb.append("/"+otherKey+"\")");
        }else{
            sb.append("\")");
        }
        method.addAnnotation(sb.toString());
    }

    /**
     * 内部方法
     * 生成Controller时添加方法的catch和return语句
     *
     * @param method
     */
    private void addExceptionAndreturn(Method method) {
        method.addBodyLine("}catch(Exception e){");
        method.addBodyLine("setExceptionResponse(responseSimple,e);");
        method.addBodyLine("}");
        method.addBodyLine("return responseSimple;");
    }


    public Method createGenerate() {
        final String methodPrefix = "create";
        StringBuilder sb = new StringBuilder();
        Method method = ctreateMethod(methodPrefix);
        method.addParameter(entityParamter);

        method.setReturnType(responseSimple);
        addControllerMapping(method,"","post");
        method.addBodyLine("ResponseSimple responseSimple = new ResponseSimpleImpl();");
        method.addBodyLine("try {");
        sb.setLength(0);
        sb.append("int rows =  ").append(serviceBeanName).append(".insert(");
        sb.append(entityFirstLowerShortName).append(");");
        method.addBodyLine(sb.toString());
        sb.setLength(0);
        sb.append("responseSimple.addAttribute(\"rows\",String.valueOf(rows));");
        method.addBodyLine(sb.toString());
        addExceptionAndreturn(method);
        return method;
    }

    public Method updateGenerate() {
        final String methodPrefix = "update";
        StringBuilder sb = new StringBuilder();
        Method method = ctreateMethod(methodPrefix);
        method.addParameter(entityParamter);
        method.setReturnType(responseSimple);
        addControllerMapping(method,"","put");
        method.addBodyLine("ResponseSimple responseSimple = new ResponseSimpleImpl();");
        method.addBodyLine("try {");
        sb.setLength(0);
        sb.append("int rows =  ").append(serviceBeanName).append(".updateByPrimaryKey(");
        sb.append(entityFirstLowerShortName).append(");");
        method.addBodyLine(sb.toString());
        sb.setLength(0);
        sb.append("responseSimple.addAttribute(\"rows\",String.valueOf(rows));");
        method.addBodyLine(sb.toString());
        addExceptionAndreturn(method);
        return method;
    }

    public Method deleteGenerate() {
        final String methodPrefix = "delete";
        StringBuilder sb = new StringBuilder();
        Method method = ctreateMethod(methodPrefix);
        Parameter parameter = new Parameter(new FullyQualifiedJavaType("String"),"id");
        parameter.addAnnotation("@PathVariable");
        method.addParameter(parameter);
        method.setReturnType(responseSimple);
        addControllerMapping(method,"{id}","delete");
        method.addBodyLine("ResponseSimple responseSimple = new ResponseSimpleImpl();");
        method.addBodyLine("try {");
        sb.setLength(0);
        sb.append("int rows =  ").append(serviceBeanName).append(".deleteByPrimaryKey(id);");
        method.addBodyLine(sb.toString());
        sb.setLength(0);
        sb.append("responseSimple.addAttribute(\"rows\",String.valueOf(rows));");
        method.addBodyLine(sb.toString());
        addExceptionAndreturn(method);
        return method;
    }
}
