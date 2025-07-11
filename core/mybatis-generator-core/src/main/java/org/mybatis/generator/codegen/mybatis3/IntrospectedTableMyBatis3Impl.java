package org.mybatis.generator.codegen.mybatis3;

import com.vgosoft.mybatis.generate.GenerateSqlTemplate;
import com.vgosoft.mybatis.sqlbuilder.InsertSqlBuilder;
import com.vgosoft.tool.core.VMD5Util;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.kotlin.KotlinFile;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.codegen.*;
import org.mybatis.generator.codegen.mybatis3.controller.JavaControllerGenerator;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateHtmlFiles;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GeneratedHtmlFile;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.HTMLGenerator;
import org.mybatis.generator.codegen.mybatis3.javamapper.AnnotatedClientGenerator;
import org.mybatis.generator.codegen.mybatis3.javamapper.JavaMapperGenerator;
import org.mybatis.generator.codegen.mybatis3.javamapper.MixedClientGenerator;
import org.mybatis.generator.codegen.mybatis3.model.BaseRecordGenerator;
import org.mybatis.generator.codegen.mybatis3.model.ExampleGenerator;
import org.mybatis.generator.codegen.mybatis3.model.PrimaryKeyGenerator;
import org.mybatis.generator.codegen.mybatis3.model.RecordWithBLOBsGenerator;
import org.mybatis.generator.codegen.mybatis3.other.JavaEntityListenerGenerator;
import org.mybatis.generator.codegen.mybatis3.other.JavaFlowableListenerGenerator;
import org.mybatis.generator.codegen.mybatis3.po.CachePoClassGenerator;
import org.mybatis.generator.codegen.mybatis3.service.JavaServiceGenerator;
import org.mybatis.generator.codegen.mybatis3.service.JavaServiceImplGenerator;
import org.mybatis.generator.codegen.mybatis3.sqlschema.GeneratedSqlSchemaFile;
import org.mybatis.generator.codegen.mybatis3.sqlschema.SqlDataPermissionActionScriptGenerator;
import org.mybatis.generator.codegen.mybatis3.sqlschema.SqlDataPermissionScriptGenerator;
import org.mybatis.generator.codegen.mybatis3.sqlschema.SqlSchemaScriptGenerator;
import org.mybatis.generator.codegen.mybatis3.unittest.JavaControllerUnitTestGenerator;
import org.mybatis.generator.codegen.mybatis3.unittest.JavaServiceUnitTestGenerator;
import org.mybatis.generator.codegen.mybatis3.vo.ViewObjectClassGenerator;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.XMLMapperGenerator;
import org.mybatis.generator.config.JavaControllerGeneratorConfiguration;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.config.SqlSchemaGeneratorConfiguration;
import org.mybatis.generator.custom.db.DatabaseDDLDialects;
import org.mybatis.generator.internal.ObjectFactory;
import org.mybatis.generator.internal.util.Mb3GenUtil;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Introspected table implementation for generating MyBatis3 artifacts.
 *
 * @author Jeff Butler
 */
public class IntrospectedTableMyBatis3Impl extends IntrospectedTable {

    protected final List<AbstractJavaGenerator> javaGenerators = new ArrayList<>();

    protected final List<AbstractKotlinGenerator> kotlinGenerators = new ArrayList<>();

    protected AbstractXmlGenerator xmlMapperGenerator;

    protected AbstractHtmlGenerator htmlGenerator;

    public IntrospectedTableMyBatis3Impl() {
        super(TargetRuntime.MYBATIS3);
    }

    @Override
    public void calculateGenerators(List<String> warnings,
                                    ProgressCallback progressCallback) {
        calculateJavaModelGenerators(warnings, progressCallback);

        AbstractJavaClientGenerator javaClientGenerator =
                calculateClientGenerators(warnings, progressCallback);

        calculateXmlMapperGenerator(javaClientGenerator, warnings, progressCallback);
        calculateHtmlMapperGenerator(warnings, progressCallback);

        //增加一条模块分类数据
        if (context.isUpdateModuleData()) {
            addModuleDataCateToMap();
            addModuleDataToMap();
        }
    }

    private void addModuleDataCateToMap() {
        String id = Mb3GenUtil.getModelCateId(context);
        InsertSqlBuilder sqlBuilder = GenerateSqlTemplate.insertSqlForModuleCate();
        sqlBuilder.updateStringValues("id_", id);
        sqlBuilder.updateStringValues("name_", context.getModuleName());
        context.addModuleCateDataScriptLine(id, sqlBuilder.toSql() + ";");
    }

    private void addModuleDataToMap() {
        String moduleKey = StringUtils.lowerCase(context.getModuleKeyword());
        String id = Mb3GenUtil.getModelCateId(context);
        int size = context.getModuleDataScriptLines().size() + 1;
        InsertSqlBuilder sqlBuilder = GenerateSqlTemplate.insertSqlForModule();
        sqlBuilder.updateStringValues("id_", id);
        sqlBuilder.updateStringValues("code_", moduleKey);
        sqlBuilder.updateStringValues("name_", context.getModuleName());
        sqlBuilder.updateStringValues("parent_id", "0");
        sqlBuilder.updateValues("sort_", String.valueOf(size));
        sqlBuilder.updateValues("wf_apply", "0");
        sqlBuilder.updateStringValues("category_", id);
        context.addModuleDataScriptLine(id, sqlBuilder.toSql() + ";");
    }

    protected void calculateXmlMapperGenerator(AbstractJavaClientGenerator javaClientGenerator,
                                               List<String> warnings,
                                               ProgressCallback progressCallback) {
        if (javaClientGenerator == null) {
            if (context.getSqlMapGeneratorConfiguration() != null) {
                xmlMapperGenerator = new XMLMapperGenerator();
            }
        } else {
            xmlMapperGenerator = javaClientGenerator.getMatchedXMLGenerator();
        }

        initializeAbstractGenerator(xmlMapperGenerator, warnings,
                progressCallback);
    }

    protected void calculateHtmlMapperGenerator(List<String> warnings, ProgressCallback progressCallback) {
        if (!this.getTableConfiguration().getHtmlMapGeneratorConfigurations().isEmpty()) {
            htmlGenerator = new HTMLGenerator();
            initializeAbstractGenerator(htmlGenerator, warnings, progressCallback);
        }
    }

    protected AbstractJavaClientGenerator calculateClientGenerators(List<String> warnings,
                                                                    ProgressCallback progressCallback) {
        if (!rules.generateJavaClient()) {
            return null;
        }

        AbstractJavaClientGenerator javaGenerator = createJavaClientGenerator();
        if (javaGenerator == null) {
            return null;
        }

        initializeAbstractGenerator(javaGenerator, warnings, progressCallback);
        javaGenerators.add(javaGenerator);

        return javaGenerator;
    }

    /**
     * 在这里做一些定制内容的计算
     *
     * @param warnings 生成警告
     * @param callback 进度回调
     */
    public void calculateCustom(List<String> warnings, ProgressCallback callback) {



    }

    protected AbstractJavaClientGenerator createJavaClientGenerator() {
        if (context.getJavaClientGeneratorConfiguration() == null) {
            return null;
        }

        String type = context.getJavaClientGeneratorConfiguration().getConfigurationType();

        AbstractJavaClientGenerator javaGenerator;
        if ("XMLMAPPER".equalsIgnoreCase(type)) { //$NON-NLS-1$
            javaGenerator = new JavaMapperGenerator(getClientProject());
        } else if ("MIXEDMAPPER".equalsIgnoreCase(type)) { //$NON-NLS-1$
            javaGenerator = new MixedClientGenerator(getClientProject());
        } else if ("ANNOTATEDMAPPER".equalsIgnoreCase(type)) { //$NON-NLS-1$
            javaGenerator = new AnnotatedClientGenerator(getClientProject());
        } else if ("MAPPER".equalsIgnoreCase(type)) { //$NON-NLS-1$
            javaGenerator = new JavaMapperGenerator(getClientProject());
        } else {
            javaGenerator = (AbstractJavaClientGenerator) ObjectFactory.createInternalObject(type);
        }

        return javaGenerator;
    }

    protected void calculateJavaModelGenerators(List<String> warnings, ProgressCallback progressCallback) {
        if (getRules().generateExampleClass()) {
            AbstractJavaGenerator javaGenerator = new ExampleGenerator(getExampleProject());
            initializeAbstractGenerator(javaGenerator, warnings, progressCallback);
            javaGenerators.add(javaGenerator);
        }

        if (getRules().generatePrimaryKeyClass()) {
            AbstractJavaGenerator javaGenerator = new PrimaryKeyGenerator(getModelProject());
            initializeAbstractGenerator(javaGenerator, warnings, progressCallback);
            javaGenerators.add(javaGenerator);
        }

        if (getRules().generateBaseRecordClass()) {
            AbstractJavaGenerator javaGenerator = new BaseRecordGenerator(getModelProject());
            initializeAbstractGenerator(javaGenerator, warnings, progressCallback);
            javaGenerators.add(javaGenerator);
        }

        if (getRules().generateRecordWithBLOBsClass()) {
            AbstractJavaGenerator javaGenerator = new RecordWithBLOBsGenerator(getModelProject());
            initializeAbstractGenerator(javaGenerator, warnings, progressCallback);
            javaGenerators.add(javaGenerator);
        }
        if (getRules().generateService()) {
            JavaServiceGenerator javaServiceGenerator = new JavaServiceGenerator(getServiceProject());
            initializeAbstractGenerator(javaServiceGenerator, warnings, progressCallback);
            javaGenerators.add(javaServiceGenerator);

            JavaServiceImplGenerator javaServiceImplGenerator = new JavaServiceImplGenerator(getServiceProject());
            initializeAbstractGenerator(javaServiceImplGenerator, warnings, progressCallback);
            javaGenerators.add(javaServiceImplGenerator);

            if (getRules().isGenerateEventListener()) {
                JavaEntityListenerGenerator javaEventListenerGenerator = new JavaEntityListenerGenerator(getServiceProject());
                initializeAbstractGenerator(javaEventListenerGenerator, warnings, progressCallback);
                javaGenerators.add(javaEventListenerGenerator);
            }

            if (getRules().isGenerateWfEventListener()) {
                JavaFlowableListenerGenerator javaWfEventListenerGenerator = new JavaFlowableListenerGenerator(getServiceProject());
                initializeAbstractGenerator(javaWfEventListenerGenerator, warnings, progressCallback);
                javaGenerators.add(javaWfEventListenerGenerator);
            }

            if (getRules().isGenerateServiceUnitTest()) {
                JavaServiceUnitTestGenerator javaServiceUnitTestGenerator = new JavaServiceUnitTestGenerator("src/test/java");
                initializeAbstractGenerator(javaServiceUnitTestGenerator, warnings, progressCallback);
                javaGenerators.add(javaServiceUnitTestGenerator);
            }
        }
        if (getRules().isGenerateVO()) {
            ViewObjectClassGenerator viewObjectClassGenerator = new ViewObjectClassGenerator(getModelProject());
            initializeAbstractGenerator(viewObjectClassGenerator, warnings, progressCallback);
            javaGenerators.add(viewObjectClassGenerator);
        }
        if (getRules().generateController()) {
            JavaControllerGeneratorConfiguration javaControllerGeneratorConfiguration = this.getTableConfiguration().getJavaControllerGeneratorConfiguration();
            String project = StringUtility.stringHasValue(javaControllerGeneratorConfiguration.getTargetProject()) ?
                    javaControllerGeneratorConfiguration.getTargetProject() : getClientProject();
            project = StringUtility.getTargetProject(project);
            JavaControllerGenerator javaControllerGenerator = new JavaControllerGenerator(project);
            initializeAbstractGenerator(javaControllerGenerator, warnings, progressCallback);
            javaGenerators.add(javaControllerGenerator);

            if (getRules().isGenerateControllerUnitTest()) {
                String targetProject = "src/test/java";
                String property = this.getContext().getProperty(PropertyRegistry.CONTEXT_ROOT_MODULE_NAME);
                if (StringUtility.stringHasValue(property)) {
                    String tmpStr = StringUtility.getTargetProject("$PROJECT_DIR$\\" + property);
                    targetProject = tmpStr + "\\" + targetProject;
                }
                JavaControllerUnitTestGenerator javaControllerUnitTestGenerator = new JavaControllerUnitTestGenerator(targetProject);
                initializeAbstractGenerator(javaControllerUnitTestGenerator, warnings, progressCallback);
                javaGenerators.add(javaControllerUnitTestGenerator);
            }
        }

        if (getRules().isGenerateCachePO()) {
            CachePoClassGenerator cachePoClassGenerator = new CachePoClassGenerator(getModelProject());
            initializeAbstractGenerator(cachePoClassGenerator, warnings, progressCallback);
            javaGenerators.add(cachePoClassGenerator);
        }
    }

    protected void initializeAbstractGenerator(AbstractGenerator abstractGenerator, List<String> warnings,
                                               ProgressCallback progressCallback) {
        if (abstractGenerator == null) {
            return;
        }

        abstractGenerator.setContext(context);
        abstractGenerator.setIntrospectedTable(this);
        abstractGenerator.setProgressCallback(progressCallback);
        abstractGenerator.setWarnings(warnings);
    }

    @Override
    public List<GeneratedJavaFile> getGeneratedJavaFiles() {
        List<GeneratedJavaFile> answer = new ArrayList<>();

        for (AbstractJavaGenerator javaGenerator : javaGenerators) {
            List<CompilationUnit> compilationUnits = javaGenerator.getCompilationUnits();
            for (CompilationUnit compilationUnit : compilationUnits) {
                GeneratedJavaFile gjf = new GeneratedJavaFile(compilationUnit,
                        javaGenerator.getProject(),
                        context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING),
                        context.getJavaFormatter());
                answer.add(gjf);
            }
        }

        return answer;
    }

    @Override
    public List<GeneratedKotlinFile> getGeneratedKotlinFiles() {
        List<GeneratedKotlinFile> answer = new ArrayList<>();

        for (AbstractKotlinGenerator kotlinGenerator : kotlinGenerators) {
            List<KotlinFile> kotlinFiles = kotlinGenerator.getKotlinFiles();
            for (KotlinFile kotlinFile : kotlinFiles) {
                GeneratedKotlinFile gjf = new GeneratedKotlinFile(kotlinFile,
                        kotlinGenerator.getProject(),
                        context.getProperty(PropertyRegistry.CONTEXT_KOTLIN_FILE_ENCODING),
                        context.getKotlinFormatter());
                answer.add(gjf);
            }
        }

        return answer;
    }

    protected String getClientProject() {
        return context.getJavaClientGeneratorConfiguration().getTargetProject();
    }

    protected String getModelProject() {
        return context.getJavaModelGeneratorConfiguration().getTargetProject();
    }

    protected String getExampleProject() {
        String project = context.getJavaModelGeneratorConfiguration().getProperty(
                PropertyRegistry.MODEL_GENERATOR_EXAMPLE_PROJECT);

        if (StringUtility.stringHasValue(project)) {
            return project;
        } else {
            return getModelProject();
        }
    }

    protected String getServiceProject() {
        return context.getJavaModelGeneratorConfiguration().getTargetProject();
    }

    @Override
    public List<GeneratedXmlFile> getGeneratedXmlFiles() {
        List<GeneratedXmlFile> answer = new ArrayList<>();

        if (xmlMapperGenerator != null
                && xmlMapperGenerator.getIntrospectedTable().getTableConfiguration().getSqlMapGeneratorConfiguration() != null
                && xmlMapperGenerator.getIntrospectedTable().getTableConfiguration().getSqlMapGeneratorConfiguration().isGenerate()) {
            Document document = xmlMapperGenerator.getDocument();
            GeneratedXmlFile gxf = new GeneratedXmlFile(document,
                    getMyBatis3XmlMapperFileName(), getMyBatis3XmlMapperPackage(),
                    context.getSqlMapGeneratorConfiguration().getTargetProject(),
                    true, context.getXmlFormatter());
            if (context.getPlugins().sqlMapGenerated(gxf, this)) {
                answer.add(gxf);
            }
        }

        return answer;
    }

    @Override
    public List<GeneratedHtmlFile> getGeneratedHtmlFiles() {
        GenerateHtmlFiles generateHtmlFiles = new GenerateHtmlFiles(context, this, htmlGenerator);
        return generateHtmlFiles.getGeneratedHtmlFiles();
    }

    @Override
    public List<GeneratedSqlSchemaFile> getGeneratedSqlSchemaFiles() {
        List<GeneratedSqlSchemaFile> answer = new ArrayList<>();
        SqlSchemaGeneratorConfiguration sqlSchemaGeneratorConfiguration = this.getTableConfiguration().getSqlSchemaGeneratorConfiguration();
        if (sqlSchemaGeneratorConfiguration == null) {
            sqlSchemaGeneratorConfiguration = new SqlSchemaGeneratorConfiguration(this.context, this.tableConfiguration);
            sqlSchemaGeneratorConfiguration.setGenerate(true);
        }
        if (sqlSchemaGeneratorConfiguration.isGenerate()) {
            String fileName = this.getTableConfiguration().getTableName().toLowerCase();
            if (StringUtility.stringHasValue(sqlSchemaGeneratorConfiguration.getFilePrefix())) {
                fileName = sqlSchemaGeneratorConfiguration.getFilePrefix() + fileName;
            }
            List<String> dbType = Arrays.asList("h2", this.getDbType());
            for (String type : dbType) {
                GeneratedSqlSchemaFile generatedSqlSchemaFile = new GeneratedSqlSchemaFile(fileName + ".sql",
                        type,
                        sqlSchemaGeneratorConfiguration.getTargetProject(),
                        this,
                        new SqlSchemaScriptGenerator(this, DatabaseDDLDialects.getDatabaseDialect(type.toUpperCase())));
                answer.add(generatedSqlSchemaFile);
            }
        }
        return answer;
    }

    @Override
    public List<GeneratedSqlSchemaFile> getGeneratedPermissionSqlDataFiles() {
        List<GeneratedSqlSchemaFile> answer = new ArrayList<>();
        if (this.getPermissionDataScriptLines().isEmpty()) {
            return answer;
        }
        String fileName = "data-permission-" + this.getTableConfiguration().getTableName().toLowerCase() + ".sql";
        GeneratedSqlSchemaFile generatedSqlSchemaFile = new GeneratedSqlSchemaFile(fileName,
                "init",
                "src/main/resources/sql",
                this,
                new SqlDataPermissionScriptGenerator(this, DatabaseDDLDialects.getDatabaseDialect("MYSQL")));
        answer.add(generatedSqlSchemaFile);
        return answer;
    }

    @Override
    public List<GeneratedSqlSchemaFile> getGeneratedPermissionActionSqlDataFiles() {
        List<GeneratedSqlSchemaFile> answer = new ArrayList<>();
        if (this.getPermissionActionDataScriptLines().isEmpty()) {
            return answer;
        }
        String fileName = "data-permission-action-" + this.getTableConfiguration().getTableName().toLowerCase() + ".sql";
        GeneratedSqlSchemaFile generatedSqlSchemaFile = new GeneratedSqlSchemaFile(fileName,
                "init",
                "src/main/resources/sql",
                this,
                new SqlDataPermissionActionScriptGenerator(this, DatabaseDDLDialects.getDatabaseDialect("MYSQL")));
        answer.add(generatedSqlSchemaFile);
        return answer;
    }

    @Override
    public int getGenerationSteps() {
        return javaGenerators.size() + (xmlMapperGenerator == null ? 0 : 1);
    }

    @Override
    public boolean requiresXMLGenerator() {
        AbstractJavaClientGenerator javaClientGenerator = createJavaClientGenerator();

        if (javaClientGenerator == null) {
            return false;
        } else {
            return javaClientGenerator.requiresXMLGenerator();
        }
    }
}
