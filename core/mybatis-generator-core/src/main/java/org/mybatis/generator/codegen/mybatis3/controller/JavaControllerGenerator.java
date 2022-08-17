package org.mybatis.generator.codegen.mybatis3.controller;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.codegen.mybatis3.controller.elements.*;
import org.mybatis.generator.config.*;
import org.mybatis.generator.custom.pojo.FormOptionGeneratorConfiguration;

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

        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString("Progress.48", table.toString()));

        CommentGenerator commentGenerator = context.getCommentGenerator();

        JavaControllerGeneratorConfiguration javaControllerGeneratorConfiguration = introspectedTable.getTableConfiguration().getJavaControllerGeneratorConfiguration();
        JavaServiceGeneratorConfiguration javaServiceGeneratorConfiguration = introspectedTable.getTableConfiguration().getJavaServiceGeneratorConfiguration();
        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());

        StringBuilder sb = new StringBuilder();
        sb.append(javaControllerGeneratorConfiguration.getTargetPackage());
        sb.append(".").append(entityType.getShortName()).append("Controller");
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
        conTopClazz.addImportedType("lombok.RequiredArgsConstructor");
        conTopClazz.addImportedType("org.springframework.web.bind.annotation.*");
        conTopClazz.addStaticImport(RESPONSE_RESULT+".*");
        conTopClazz.addImportedType(API_CODE_ENUM);
        conTopClazz.addAnnotation("@RequiredArgsConstructor");
        conTopClazz.addAnnotation("@RestController");

        conTopClazz.addAnnotation("@RequestMapping(value = \"/" + introspectedTable.getControllerSimplePackage() + "\")");

        FullyQualifiedJavaType bizInfType = new FullyQualifiedJavaType(infName);
        Field field = new Field(introspectedTable.getControllerBeanName(), bizInfType);
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setFinal(true);
        conTopClazz.addField(field);
        //增加Mappings属性
        if (isGenerateVoModel()) {
            String voTargetPackage = introspectedTable.getTableConfiguration()
                    .getVoGeneratorConfiguration()
                    .getTargetPackage();
            FullyQualifiedJavaType entityMappings = new FullyQualifiedJavaType(
                    String.join(".",
                            voTargetPackage,"maps",entityType.getShortName()+"Mappings"));
            Field mappings = new Field("mappings", entityMappings);
            mappings.setFinal(true);
            mappings.setInitializationString(String.join(".", entityMappings.getShortName(),"INSTANCE"));
            mappings.setVisibility(JavaVisibility.PRIVATE);
            conTopClazz.addField(mappings);
            conTopClazz.addImportedType(entityMappings);
        }

        String viewpath = null;
        if (introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().size()>0) {
            viewpath = introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().get(0).getViewPath();
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

        if (introspectedTable.getTableConfiguration().getJavaControllerGeneratorConfiguration().getFormOptionGeneratorConfigurations().size()>0) {
            addOptionElement(conTopClazz);
        }

        List<CompilationUnit> answer = new ArrayList<>();
        if (context.getPlugins().ControllerGenerated(conTopClazz, introspectedTable)) {
            answer.add(conTopClazz);
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
        VOGeneratorConfiguration voGeneratorConfiguration = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration();
        return voGeneratorConfiguration!=null && voGeneratorConfiguration.isGenerate();
    }

    protected boolean isGenerateVoView(){
        VOViewGeneratorConfiguration voViewGeneratorConfiguration = introspectedTable.getTableConfiguration().getVoViewGeneratorConfiguration();
        return voViewGeneratorConfiguration!=null && voViewGeneratorConfiguration.isGenerate();
    }
}
