package org.mybatis.generator.codegen.mybatis3.controller;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractGenerator;
import org.mybatis.generator.config.HtmlMapGeneratorConfiguration;
import org.mybatis.generator.custom.htmlGenerator.GenerateUtils;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;

public abstract class AbstractControllerElementGenerator  extends AbstractGenerator {

    private static final String RESPONSE_SIMPLE = "com.vgosoft.web.respone.ResponseSimple";

    protected FullyQualifiedJavaType entityType;

    protected FullyQualifiedJavaType exampleType;

    protected CommentGenerator commentGenerator;

    protected String serviceBeanName;

    protected String entityNameKey;

    protected String entityFirstLowerShortName;

    protected Parameter entityParameter;

    protected HtmlMapGeneratorConfiguration htmlMapGeneratorConfiguration;

    protected FullyQualifiedJavaType responseSimple;

    public abstract void addElements(TopLevelClass parentElement);

    public AbstractControllerElementGenerator() {
        super();
    }

    protected void initGenerator(){
        entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        commentGenerator = context.getCommentGenerator();
        serviceBeanName = introspectedTable.getControllerBeanName();
        entityNameKey = GenerateUtils.isWorkflowInstance(introspectedTable)?"business":"entity";
        if (introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().size()>0) {
            htmlMapGeneratorConfiguration = introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().get(0);
        }
        entityParameter = new Parameter(entityType, JavaBeansUtil.getFirstCharacterLowercase(entityType.getShortName()));
        responseSimple = new FullyQualifiedJavaType(RESPONSE_SIMPLE);
        entityFirstLowerShortName = JavaBeansUtil.getFirstCharacterLowercase(entityType.getShortName());
    }

    protected Method createMethod(String methodPrefix) {
        Method method = new Method(methodPrefix + entityType.getShortName());
        method.setVisibility(JavaVisibility.PUBLIC);
        commentGenerator.addGeneralMethodComment(method, introspectedTable);
        return method;
    }

    protected void addControllerMapping(Method method, String otherKey, String methodType) {
        StringBuilder sb = new StringBuilder();
        String mappingPrefix = JavaBeansUtil.getFirstCharacterUppercase(methodType);
        sb.append("@").append(mappingPrefix).append("Mapping(value = \"");
        sb.append(this.serviceBeanName);
        if (StringUtility.stringHasValue(otherKey)) {
            sb.append("/").append(otherKey).append("\")");
        } else {
            sb.append("\")");
        }
        method.addAnnotation(sb.toString());
    }

    /**
     * 内部方法
     * 生成Controller时添加方法的catch和return语句
     *
     */
    protected void addExceptionAndReturn(Method method) {
        method.addBodyLine("} catch (Exception e) {");
        method.addBodyLine("setExceptionResponse(responseSimple, e);");
        method.addBodyLine("}");
        method.addBodyLine("return responseSimple;");
    }
}
