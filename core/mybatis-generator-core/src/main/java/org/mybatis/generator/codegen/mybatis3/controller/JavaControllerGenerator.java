package org.mybatis.generator.codegen.mybatis3.controller;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.codegen.mybatis3.controller.elements.*;
import org.mybatis.generator.config.*;
import org.mybatis.generator.custom.pojo.FormOptionGeneratorConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.custom.ConstantsUtil.*;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

public class JavaControllerGenerator  extends AbstractJavaGenerator{

    public JavaControllerGenerator(String project) {
        super(project);
          }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        String voTargetPackage = context.getJavaModelGeneratorConfiguration()
                .getBaseTargetPackage()+".pojo";
        FullyQualifiedJavaType exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        FullyQualifiedJavaType entityType1 = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        FullyQualifiedJavaType entityVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage, "vo", entityType1.getShortName() + "VO"));
        FullyQualifiedJavaType entityRequestVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage, "vo", entityType1.getShortName() + "RequestVO"));

        List<CompilationUnit> answer = new ArrayList<>();
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString("Progress.48", table.toString()));
        CommentGenerator commentGenerator = context.getCommentGenerator();
        JavaControllerGeneratorConfiguration javaControllerGeneratorConfiguration = tc.getJavaControllerGeneratorConfiguration();
        JavaServiceGeneratorConfiguration javaServiceGeneratorConfiguration = tc.getJavaServiceGeneratorConfiguration();
        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        String controllerName = "Gen"+ entityType.getShortName() + "Controller";
        StringBuilder sb = new StringBuilder();
        sb.append(javaControllerGeneratorConfiguration.getTargetPackageGen());
        sb.append(".").append(controllerName);
        FullyQualifiedJavaType conClazzType = new FullyQualifiedJavaType(sb.toString());
        TopLevelClass conTopClazz = new TopLevelClass(conClazzType);
        conTopClazz.setVisibility(JavaVisibility.PUBLIC);
        commentGenerator.addJavaFileComment(conTopClazz);
        FullyQualifiedJavaType supClazzType = new FullyQualifiedJavaType(ABSTRACT_BASE_CONTROLLER);
        conTopClazz.setSuperClass(supClazzType);

        sb.setLength(0);
        sb.append(javaServiceGeneratorConfiguration.getTargetPackage()).append(".I");
        sb.append(entityType.getShortName());
        String infName = sb.toString();
        conTopClazz.addImportedType(infName);
        conTopClazz.addImportedType(supClazzType);
        conTopClazz.addImportedType("org.springframework.web.bind.annotation.*");
        conTopClazz.addStaticImport(RESPONSE_RESULT+".*");
        conTopClazz.addImportedType(API_CODE_ENUM);
        FullyQualifiedJavaType bizInfType = new FullyQualifiedJavaType(infName);
        Field field = new Field(introspectedTable.getControllerBeanName(), bizInfType);
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setFinal(true);
        conTopClazz.addField(field);

        //构造器
        Method method = new Method(controllerName);
        method.addParameter(new Parameter(bizInfType, introspectedTable.getControllerBeanName()));
        method.setConstructor(true);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addBodyLine("this.{0} = {0};",introspectedTable.getControllerBeanName());
        //增加Mappings属性
        FullyQualifiedJavaType entityMappings = new FullyQualifiedJavaType(
                String.join(".",
                        voTargetPackage,"maps",entityType.getShortName()+"Mappings"));
        if (isGenerateVoModel()) {
            Field mappings = new Field("mappings", entityMappings);
            mappings.setFinal(true);
            //mappings.setInitializationString(String.join(".", entityMappings.getShortName(),"INSTANCE"));
            mappings.setVisibility(JavaVisibility.PRIVATE);
            conTopClazz.addField(mappings);
            conTopClazz.addImportedType(entityMappings);
            method.addParameter(new Parameter(entityMappings, "mappings"));
            method.addBodyLine("this.mappings = mappings;");
        }
        conTopClazz.addMethod(method);

        String viewpath = null;
        if (tc.getHtmlMapGeneratorConfigurations().size()>0) {
            viewpath = tc.getHtmlMapGeneratorConfigurations().get(0).getViewPath();
        }
        if (viewpath != null) {
            addViewElement(conTopClazz);
        }
        addGetElement(conTopClazz);
        addListElement(conTopClazz);
        addCreateElement(conTopClazz);
        addUpdateElement(conTopClazz);
        addDeleteElement(conTopClazz);
        addDeleteBatchElement(conTopClazz);

        if (isGenerateVoView()) {
            addGetDefaultViewConfigElement(conTopClazz);
            addGetDefaultViewElement(conTopClazz);
        }

        if (introspectedTable.hasBLOBColumns()) {
            addUploadElement(conTopClazz);
            addDownloadElement(conTopClazz);
        }
        if (tc.getVoExcelGeneratorConfiguration()!=null && tc.getVoExcelGeneratorConfiguration().isGenerate()) {
            addTemplateElement(conTopClazz);
            addImportElement(conTopClazz);
        }

        if (tc.getJavaControllerGeneratorConfiguration().getFormOptionGeneratorConfigurations().size()>0) {
            addOptionElement(conTopClazz);
        }

        //追加一个example构造方法
        Method buildExample = new Method("buildExample");
        buildExample.setVisibility(JavaVisibility.PROTECTED);
        buildExample.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "actionType"));
        if (introspectedTable.getRules().isGenerateRequestVO()) {
            buildExample.addParameter(new Parameter(entityRequestVoType, entityRequestVoType.getShortNameFirstLowCase()));
        }else if(introspectedTable.getRules().isGenerateVoModel()){
            buildExample.addParameter(new Parameter(entityVoType, entityVoType.getShortNameFirstLowCase()));
        }else{
            buildExample.addParameter(new Parameter(entityType,entityType.getShortNameFirstLowCase()));
        }
        buildExample.setReturnType(exampleType);
        buildExample.addBodyLine("return new {0}();", exampleType.getShortName());
        conTopClazz.addMethod(buildExample);

        //追加到列表
        if (context.getPlugins().controllerGenerated(conTopClazz, introspectedTable)) {
            answer.add(conTopClazz);
        }

        //生成子类
        String subControllerName = entityType.getShortName() + "Controller";
        sb.setLength(0);
        sb.append(javaControllerGeneratorConfiguration.getTargetPackage());
        sb.append(".").append(subControllerName);
        FullyQualifiedJavaType conSubClazzType = new FullyQualifiedJavaType(sb.toString());
        TopLevelClass conSubTopClazz = new TopLevelClass(conSubClazzType);
        conSubTopClazz.setVisibility(JavaVisibility.PUBLIC);
        conSubTopClazz.setSuperClass(conClazzType);
        conSubTopClazz.addImportedType(conClazzType);
        conSubTopClazz.addImportedType("org.springframework.web.bind.annotation.*");
        conSubTopClazz.addAnnotation("@RestController");
        conSubTopClazz.addAnnotation("@RequestMapping(value = \"/" + introspectedTable.getControllerSimplePackage() + "\")");
        //构造器
        Method conMethod = new Method(subControllerName);
        conMethod.addParameter(new Parameter(bizInfType, introspectedTable.getControllerBeanName()));
        conSubTopClazz.addImportedType(bizInfType);
        conMethod.setConstructor(true);
        conMethod.setVisibility(JavaVisibility.PUBLIC);
        if (isGenerateVoModel()) {
            conSubTopClazz.addImportedType(entityMappings);
            conMethod.addParameter(new Parameter(entityMappings, "mappings"));
            conMethod.addBodyLine("super({0}, mappings);",introspectedTable.getControllerBeanName());
        }else{
            conMethod.addBodyLine("super({0});",introspectedTable.getControllerBeanName());
        }
        conSubTopClazz.addMethod(conMethod);
        boolean forceGenerateScalableElement = introspectedTable.getRules().isForceGenerateScalableElement();
        boolean fileNotExist = JavaBeansUtil.javaFileNotExist(javaControllerGeneratorConfiguration.getTargetProject(), conSubClazzType.getPackageName(), subControllerName);
        if (forceGenerateScalableElement || fileNotExist) {
            if (context.getPlugins().subControllerGenerated(conSubTopClazz, introspectedTable)){
                answer.add(conSubTopClazz);
            }
        }
        return answer;
    }

    private void addViewElement(TopLevelClass parentElement) {
        List<HtmlGeneratorConfiguration> htmlGeneratorConfigurations = introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations();
        long count = htmlGeneratorConfigurations.stream().filter(h->h.isGenerate() && stringHasValue(h.getViewPath())).count();
        if (count>0) {
            AbstractControllerElementGenerator elementGenerator = new ViewElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addGetElement(TopLevelClass parentElement) {
        AbstractControllerElementGenerator elementGenerator = new GetElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }

    private void addListElement(TopLevelClass parentElement) {
        AbstractControllerElementGenerator elementGenerator = new ListElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }

    private void addCreateElement(TopLevelClass parentElement) {
        AbstractControllerElementGenerator elementGenerator = new CreateElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }

    private void addUpdateElement(TopLevelClass parentElement) {
        AbstractControllerElementGenerator elementGenerator = new UpdateElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }

    private void addDeleteElement(TopLevelClass parentElement) {
        AbstractControllerElementGenerator elementGenerator = new DeleteElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }

    private void addDeleteBatchElement(TopLevelClass parentElement) {
        AbstractControllerElementGenerator elementGenerator = new DeleteBatchElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }

    private void addUploadElement(TopLevelClass parentElement) {
        AbstractControllerElementGenerator elementGenerator = new UploadElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }

    private void addDownloadElement(TopLevelClass parentElement) {
        AbstractControllerElementGenerator elementGenerator = new DownloadElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }

    private void addGetDefaultViewElement(TopLevelClass parentElement){
        AbstractControllerElementGenerator elementGenerator = new GetDefaultViewElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }

    private void addGetDefaultViewConfigElement(TopLevelClass parentElement){
        AbstractControllerElementGenerator elementGenerator = new GetDefaultViewConfigElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }

    private void addOptionElement(TopLevelClass parentElement) {
        for (FormOptionGeneratorConfiguration formOptionGeneratorConfiguration : introspectedTable.getTableConfiguration().getJavaControllerGeneratorConfiguration().getFormOptionGeneratorConfigurations()) {
            AbstractControllerElementGenerator elementGenerator = new OptionElementGenerator(formOptionGeneratorConfiguration);
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addTemplateElement(TopLevelClass parentElement) {
        AbstractControllerElementGenerator elementGenerator = new TemplateElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }

    private void addImportElement(TopLevelClass parentElement) {
        AbstractControllerElementGenerator elementGenerator = new ImportElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }

    protected void initializeAndExecuteGenerator(
            AbstractControllerElementGenerator elementGenerator,
            TopLevelClass parentElement) {
        elementGenerator.setContext(context);
        elementGenerator.setIntrospectedTable(introspectedTable);
        elementGenerator.setProgressCallback(progressCallback);
        elementGenerator.setWarnings(warnings);
        elementGenerator.initGenerator();
        elementGenerator.addElements(parentElement);
    }

    protected boolean isGenerateVoModel(){
        VOModelGeneratorConfiguration voModelGeneratorConfiguration = introspectedTable.getTableConfiguration().getVoModelGeneratorConfiguration();
        return voModelGeneratorConfiguration !=null && voModelGeneratorConfiguration.isGenerate();
    }

    protected boolean isGenerateVoView(){
        VOViewGeneratorConfiguration voViewGeneratorConfiguration = introspectedTable.getTableConfiguration().getVoViewGeneratorConfiguration();
        return voViewGeneratorConfiguration!=null && voViewGeneratorConfiguration.isGenerate();
    }
}
