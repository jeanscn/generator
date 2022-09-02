package org.mybatis.generator.codegen.mybatis3.controller;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractGenerator;
import org.mybatis.generator.config.*;
import org.mybatis.generator.custom.htmlGenerator.GenerateUtils;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;

import static org.mybatis.generator.custom.ConstantsUtil.*;
import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_PAGEHELPER_RESULT;

public abstract class AbstractControllerElementGenerator  extends AbstractGenerator {



    protected FullyQualifiedJavaType entityType;

    protected FullyQualifiedJavaType exampleType;

    protected CommentGenerator commentGenerator;

    protected String serviceBeanName;

    protected String entityNameKey;

    protected FullyQualifiedJavaType entityMappings;

    protected FullyQualifiedJavaType entityVoType;

    protected FullyQualifiedJavaType entityViewVoType;

    protected FullyQualifiedJavaType entityRequestVoType;

    protected FullyQualifiedJavaType responseResult;

    protected FullyQualifiedJavaType responsePagehelperResult;

    protected Parameter entityParameter;

    protected HtmlGeneratorConfiguration htmlGeneratorConfiguration;

    protected FullyQualifiedJavaType responseSimple;

    protected TableConfiguration tc;

    public abstract void addElements(TopLevelClass parentElement);

    public AbstractControllerElementGenerator() {
        super();
    }

    protected void initGenerator(){
        tc = introspectedTable.getTableConfiguration();
        entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        commentGenerator = context.getCommentGenerator();
        serviceBeanName = introspectedTable.getControllerBeanName();
        entityNameKey = GenerateUtils.isWorkflowInstance(introspectedTable)?"business":"entity";
        if (introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().size()>0) {
            htmlGeneratorConfiguration = introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().get(0);
        }
        entityParameter = new Parameter(entityType, JavaBeansUtil.getFirstCharacterLowercase(entityType.getShortName()));
        responseSimple = new FullyQualifiedJavaType(RESPONSE_SIMPLE);
        responseResult = new FullyQualifiedJavaType(RESPONSE_RESULT);
        responsePagehelperResult = new FullyQualifiedJavaType(RESPONSE_PAGEHELPER_RESULT);
        String voTargetPackage = introspectedTable.getTableConfiguration()
                .getVoModelGeneratorConfiguration()
                .getBaseTargetPackage();
        entityMappings = new FullyQualifiedJavaType(String.join(".", voTargetPackage,"maps",entityType.getShortName()+"Mappings"));
        entityVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage,"vo",entityType.getShortName()+"VO"));
        entityViewVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage,"vo",entityType.getShortName()+"ViewVO"));
        entityRequestVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage,"vo",entityType.getShortName()+"RequestVO"));
    }

    protected Method createMethod(String methodPrefix) {
        Method method = new Method(methodPrefix + entityType.getShortName());
        method.setVisibility(JavaVisibility.PROTECTED);
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
   /* protected void addExceptionAndReturn(Method method) {
        method.addBodyLine("} catch (Exception e) {");
        method.addBodyLine("setExceptionResponse(responseSimple, e);");
        method.addBodyLine("}");
        method.addBodyLine("return responseSimple;");
    }*/

    protected void addSystemLogAnnotation(Method method,TopLevelClass parentElement){
        JavaControllerGeneratorConfiguration javaControllerGeneratorConfiguration = introspectedTable.getTableConfiguration().getJavaControllerGeneratorConfiguration();
        String property = javaControllerGeneratorConfiguration.getProperty(PropertyRegistry.CONTROLLER_ENABLE_SYSLOG_ANNOTATION);
        if (StringUtility.stringHasValue(property)) {
            if (Boolean.parseBoolean(property)) {
                StringBuilder sb = new StringBuilder();
                FullyQualifiedJavaType record = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
                sb.append(introspectedTable.getRemarks()).append("：");
                if(("view"+record.getShortName()).equals(method.getName())){
                    sb.append("通过表单查看或创建").append(introspectedTable.getRemarks()).append("记录");
                }else if(("get"+record.getShortName()).equals(method.getName())){
                    sb.append("根据主键查询单条");
                }else if(("list"+record.getShortName()).equals(method.getName())){
                    sb.append("查看数据列表");
                }else if(("create"+record.getShortName()).equals(method.getName())){
                    sb.append("添加了一条记录");
                }else if(("upload"+record.getShortName()).equals(method.getName())){
                    sb.append("上传记录");
                }else if(("download"+record.getShortName()).equals(method.getName())){
                    sb.append("下载数据");
                }else if(("update"+record.getShortName()).equals(method.getName())){
                    sb.append("更新了一条记录");
                }else if(("delete"+record.getShortName()).equals(method.getName())){
                    sb.append("删除了一条记录");
                }else if(("deleteBatch"+record.getShortName()).equals(method.getName())){
                    sb.append("删除了一条或多条记录");
                }else if(("getDefaultView"+record.getShortName()).equals(method.getName())){
                    sb.append("查看").append(introspectedTable.getRemarks()).append("表默认视图");
                }else if(("getDefaultViewConfig"+record.getShortName()).equals(method.getName())){
                    sb.append("查看").append(introspectedTable.getRemarks()).append("表默认视图配置");
                }else if(("template"+record.getShortName()).equals(method.getName())) {
                    sb.append("下载").append(introspectedTable.getRemarks()).append("导入模板");
                }else if(("import"+record.getShortName()).equals(method.getName())) {
                    sb.append("Excel").append(introspectedTable.getRemarks()).append("数据导入");
                }else if(("export"+record.getShortName()).equals(method.getName())) {
                    sb.append("Excel").append(introspectedTable.getRemarks()).append("数据导出");
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

    protected FullyQualifiedJavaType getResponseResult(boolean isListResult) {
        FullyQualifiedJavaType response = new FullyQualifiedJavaType(RESPONSE_RESULT);
        FullyQualifiedJavaType result = new FullyQualifiedJavaType(introspectedTable.getRules().isGenerateVoModel()
                ?entityVoType.getFullyQualifiedName():entityType.getFullyQualifiedName());
        if (isListResult) {
            FullyQualifiedJavaType listInstance = FullyQualifiedJavaType.getNewListInstance();
            listInstance.addTypeArgument(result);
            response.addTypeArgument(listInstance);
        }else{
            response.addTypeArgument(result);
        }
        return response;
    }

    protected Parameter buildMethodParameter(boolean isValid,boolean isRequestBody,TopLevelClass parentElement){
        Parameter parameter;
        if (introspectedTable.getRules().isGenerateVoModel()) {
            parameter = new Parameter(entityVoType, entityVoType.getShortNameFirstLowCase());
            parentElement.addImportedType(entityVoType);
        }else{
            parameter = new Parameter(entityType,entityType.getShortNameFirstLowCase());
            parentElement.addImportedType(entityType);
        }
        if (isValid) {
            parameter.addAnnotation("@Valid");
            parentElement.addImportedType("javax.validation.Valid");
        }
        if (isRequestBody) {
            parameter.addAnnotation("@RequestBody");
            parentElement.addImportedType("org.springframework.web.bind.annotation.RequestBody");
        }
        return parameter;
    }

    protected void selectByExampleWithPagehelper(TopLevelClass parentElement, Method method) {
        String listEntityVar = entityType.getShortNameFirstLowCase()+"s";
        String requestVOVar = entityRequestVoType.getShortNameFirstLowCase();
        method.addBodyLine("{0} example = buildExample(actionType,{1});",
                exampleType.getShortName(),
                introspectedTable.getRules().isGenerateRequestVO()?requestVOVar:
                        introspectedTable.getRules().isGenerateVoModel()?entityVoType.getShortNameFirstLowCase():entityType.getShortNameFirstLowCase());
        if (introspectedTable.getRules().isGenerateRequestVO()) {
            method.addBodyLine("if ({0}.getPageNo()>0 && {0}.getPageSize()>0) '{'\n" +
                    "            PageHelper.startPage({0}.getPageNo(), {0}.getPageSize());\n" +
                    "        '}'",requestVOVar);
            parentElement.addImportedType("com.github.pagehelper.PageHelper");
        }
        if (introspectedTable.getRules().isGenerateRequestVO() && introspectedTable.getRules().generateRelationWithSubSelected()) {
            method.addBodyLine("List<{0}> {1};\n" +
                            "        if ({3}.isCascadeResult()) '{'\n" +
                            "            {1} = {2}.selectByExampleWithRelation(example);\n" +
                            "        '}'else'{'\n" +
                            "            {1} = {2}.selectByExample(example);\n" +
                            "        '}'",
                    entityType.getShortName(), listEntityVar,serviceBeanName,requestVOVar);
        }else{
            method.addBodyLine("List<{0}> {1} = {2}.selectByExample(example);",
                    entityType.getShortName(), listEntityVar,serviceBeanName);
        }
        method.addBodyLine("Page<{0}> page = (Page<{0}>){1};",entityType.getShortName(), listEntityVar);
        parentElement.addImportedType("com.github.pagehelper.Page");
    }

    protected void addSecurityPreAuthorize(Method method,String methodPrefix) {
        if (introspectedTable.getRules().isIntegrateSpringSecurity()) {
            StringBuilder sb = new StringBuilder("@PreAuthorize(\"");
            sb.append("@uss.hasPermission('");
            sb.append(introspectedTable.getControllerSimplePackage().toLowerCase());
            sb.append(":");
            sb.append(serviceBeanName.toLowerCase());
            sb.append(":");
            sb.append(methodPrefix.toLowerCase());
            sb.append("')\")");
            method.addAnnotation(sb.toString());
        }
    }
}
