package org.mybatis.generator.codegen.mybatis3.controller;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractGenerator;
import org.mybatis.generator.config.HtmlMapGeneratorConfiguration;
import org.mybatis.generator.config.JavaControllerGeneratorConfiguration;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.custom.htmlGenerator.GenerateUtils;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;

import static org.mybatis.generator.custom.ConstantsUtil.*;

public abstract class AbstractControllerElementGenerator  extends AbstractGenerator {



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

    protected void addSystemLogAnnotation(Method method,TopLevelClass parentElement){
        JavaControllerGeneratorConfiguration javaControllerGeneratorConfiguration = introspectedTable.getTableConfiguration().getJavaControllerGeneratorConfiguration();
        String property = javaControllerGeneratorConfiguration.getProperty(PropertyRegistry.CONTROLLER_ENABLE_SYSLOG_ANNOTATION);
        if (StringUtility.stringHasValue(property)) {
            if (Boolean.parseBoolean(property)) {
                StringBuilder sb = new StringBuilder();
                FullyQualifiedJavaType record = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
                sb.append(introspectedTable.getRemarks()).append("：");
                if(("view"+record.getShortName()).equals(method.getName())){
                    sb.append("通过表单查看或创建记录！");
                }else if(("get"+record.getShortName()).equals(method.getName())){
                    sb.append("根据主键查询单条！");
                }else if(("list"+record.getShortName()).equals(method.getName())){
                    sb.append("查看数据列表！");
                }else if(("create"+record.getShortName()).equals(method.getName())){
                    sb.append("添加了一条记录！");
                }else if(("upload"+record.getShortName()).equals(method.getName())){
                    sb.append("上传记录！");
                }else if(("download"+record.getShortName()).equals(method.getName())){
                    sb.append("下载数据！");
                }else if(("update"+record.getShortName()).equals(method.getName())){
                    sb.append("更新了一条记录！");
                }else if(("delete"+record.getShortName()).equals(method.getName())){
                    sb.append("删除了一条记录！");
                }else if(("deleteBatch"+record.getShortName()).equals(method.getName())){
                    sb.append("删除了一条或多条记录！");
                }else{
                    sb.append("执行操作！");
                }
                method.addAnnotation("@SystemLog(value=\""+ sb +"\")");
                parentElement.addImportedType(ANNOTATION_SYSTEM_LOG);
                //增加事务
                method.addAnnotation("@Transactional");
                parentElement.addImportedType(ANNOTATION_TRANSACTIONAL);

            }
        }
    }
}
