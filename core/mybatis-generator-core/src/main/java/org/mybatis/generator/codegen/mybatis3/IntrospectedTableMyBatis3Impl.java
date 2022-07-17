/*
 *    Copyright 2006-2021 the original author or authors.
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
package org.mybatis.generator.codegen.mybatis3;

import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.kotlin.KotlinFile;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.codegen.*;
import org.mybatis.generator.codegen.mybatis3.controller.JavaControllerGenerator;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateHtmlFiles;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.HTMLGenerator;
import org.mybatis.generator.codegen.mybatis3.javamapper.AnnotatedClientGenerator;
import org.mybatis.generator.codegen.mybatis3.javamapper.JavaMapperGenerator;
import org.mybatis.generator.codegen.mybatis3.javamapper.MixedClientGenerator;
import org.mybatis.generator.codegen.mybatis3.model.BaseRecordGenerator;
import org.mybatis.generator.codegen.mybatis3.model.ExampleGenerator;
import org.mybatis.generator.codegen.mybatis3.model.PrimaryKeyGenerator;
import org.mybatis.generator.codegen.mybatis3.model.RecordWithBLOBsGenerator;
import org.mybatis.generator.codegen.mybatis3.service.JavaServiceGenerator;
import org.mybatis.generator.codegen.mybatis3.service.JavaServiceImplGenerator;
import org.mybatis.generator.codegen.mybatis3.sqlschema.GeneratedSqlSchemaFile;
import org.mybatis.generator.codegen.mybatis3.unittest.JavaControllerUnitTestGenerator;
import org.mybatis.generator.codegen.mybatis3.unittest.JavaServiceUnitTestGenerator;
import org.mybatis.generator.codegen.mybatis3.vo.ViewObjectClassGenerator;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.XMLMapperGenerator;
import org.mybatis.generator.config.JavaControllerGeneratorConfiguration;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.config.SqlSchemaGeneratorConfiguration;
import org.mybatis.generator.custom.db.DatabaseDDLDialects;
import org.mybatis.generator.internal.ObjectFactory;
import org.mybatis.generator.internal.util.StringUtility;

import java.io.File;
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

    protected void calculateHtmlMapperGenerator(List<String> warnings,ProgressCallback progressCallback) {
        if (this.getTableConfiguration().getHtmlMapGeneratorConfigurations().size()>0) {
            htmlGenerator = new HTMLGenerator();
            initializeAbstractGenerator(htmlGenerator, warnings,progressCallback);
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

    protected void calculateJavaModelGenerators(List<String> warnings,ProgressCallback progressCallback) {
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
            initializeAbstractGenerator(javaServiceGenerator, warnings,progressCallback);
            javaGenerators.add(javaServiceGenerator);

            JavaServiceImplGenerator javaServiceImplGenerator = new JavaServiceImplGenerator(getServiceProject());
            initializeAbstractGenerator(javaServiceImplGenerator, warnings,progressCallback);
            javaGenerators.add(javaServiceImplGenerator);

            if (getRules().isGenerateServiceUnitTest()) {
                JavaServiceUnitTestGenerator javaServiceUnitTestGenerator = new JavaServiceUnitTestGenerator("src/test/java");
                initializeAbstractGenerator(javaServiceUnitTestGenerator, warnings,progressCallback);
                javaGenerators.add(javaServiceUnitTestGenerator);
            }
            if (getRules().isGenerateBaseVO()) {
                ViewObjectClassGenerator viewObjectClassGenerator = new ViewObjectClassGenerator(getModelProject());
                initializeAbstractGenerator(viewObjectClassGenerator, warnings,progressCallback);
                javaGenerators.add(viewObjectClassGenerator);
            }
        }
        if (getRules().generateController()) {
            JavaControllerGeneratorConfiguration javaControllerGeneratorConfiguration = this.getTableConfiguration().getJavaControllerGeneratorConfiguration();
            String project = StringUtility.stringHasValue(javaControllerGeneratorConfiguration.getTargetProject())?
                    javaControllerGeneratorConfiguration.getTargetProject():getClientProject();
            project = StringUtility.getTargetProject(project);
            JavaControllerGenerator javaControllerGenerator = new JavaControllerGenerator(project);
            initializeAbstractGenerator(javaControllerGenerator, warnings,progressCallback);
            javaGenerators.add(javaControllerGenerator);

            if (getRules().isGenerateControllerUnitTest()) {
                String targetProject = "src/test/java";
                String property = this.getContext().getProperty(PropertyRegistry.CONTEXT_ROOT_MODULE_NAME);
                if (StringUtility.stringHasValue(property)) {
                    String tmpStr = StringUtility.getTargetProject("$PROJECT_DIR$\\"+property);
                   targetProject = tmpStr+"\\"+targetProject;
                }
                JavaControllerUnitTestGenerator javaControllerUnitTestGenerator = new JavaControllerUnitTestGenerator(targetProject);
                initializeAbstractGenerator(javaControllerUnitTestGenerator, warnings,progressCallback);
                javaGenerators.add(javaControllerUnitTestGenerator);
            }
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

    protected String getServiceProject(){
        return context.getJavaModelGeneratorConfiguration().getTargetProject();
    }

    @Override
    public List<GeneratedXmlFile> getGeneratedXmlFiles() {
        List<GeneratedXmlFile> answer = new ArrayList<>();

        if (xmlMapperGenerator != null
                && xmlMapperGenerator.getIntrospectedTable().getTableConfiguration().getSqlMapGeneratorConfiguration()!=null
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
        GenerateHtmlFiles generateHtmlFiles = new GenerateHtmlFiles(context,this, htmlGenerator);
        return generateHtmlFiles.getGeneratedHtmlFiles();
    }

    @Override
    public List<GeneratedSqlSchemaFile> getGeneratedSqlSchemaFiles() {
        List<GeneratedSqlSchemaFile> answer = new ArrayList<>();
        SqlSchemaGeneratorConfiguration sqlSchemaGeneratorConfiguration = this.getTableConfiguration().getSqlSchemaGeneratorConfiguration();
        if (sqlSchemaGeneratorConfiguration == null) {
            sqlSchemaGeneratorConfiguration = new SqlSchemaGeneratorConfiguration(this.context,this.tableConfiguration);
            sqlSchemaGeneratorConfiguration.setGenerate(true);
        }
        if (sqlSchemaGeneratorConfiguration.isGenerate()) {
            String fileName = this.getTableConfiguration().getTableName().toLowerCase();
            if (StringUtility.stringHasValue(sqlSchemaGeneratorConfiguration.getFilePrefix())) {
                fileName = sqlSchemaGeneratorConfiguration.getFilePrefix()+fileName;
            }
            List<String> dbType = Arrays.asList("h2", "mysql");
            for (String type : dbType) {
                GeneratedSqlSchemaFile generatedSqlSchemaFile = new GeneratedSqlSchemaFile(fileName+".sql",
                        type,
                        sqlSchemaGeneratorConfiguration.getTargetProject(),
                        this,
                        new SqlScriptGenerator(this, DatabaseDDLDialects.getDatabaseDialect(type.toUpperCase())));
                answer.add(generatedSqlSchemaFile);
            }
        }
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
