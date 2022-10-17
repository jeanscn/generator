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
package org.mybatis.generator.codegen.mybatis3.javamapper;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractHtmlGenerator;
import org.mybatis.generator.codegen.AbstractJavaClientGenerator;
import org.mybatis.generator.codegen.AbstractXmlGenerator;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.HTMLGenerator;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.*;
import org.mybatis.generator.codegen.mybatis3.service.ServiceMethods;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.XMLMapperGenerator;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.DeleteByColumnElementGenerator;
import org.mybatis.generator.config.JavaClientGeneratorConfiguration;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.custom.ReturnTypeEnum;
import org.mybatis.generator.custom.ScalableElementEnum;
import org.mybatis.generator.custom.htmlGenerator.GenerateUtils;
import org.mybatis.generator.custom.pojo.SelectByColumnGeneratorConfiguration;
import org.mybatis.generator.custom.pojo.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mybatis.generator.custom.ConstantsUtil.MBG_MAPPER_BLOB_INTERFACE;
import static org.mybatis.generator.custom.ConstantsUtil.MBG_MAPPER_INTERFACE;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

public class JavaMapperGenerator extends AbstractJavaClientGenerator {

    protected FullyQualifiedJavaType entityType;
    protected FullyQualifiedJavaType exampleType;
    protected  ServiceMethods serviceMethods;

    public JavaMapperGenerator(String project) {
        this(project, true);
    }

    public JavaMapperGenerator(String project, boolean requiresMatchedXMLGenerator) {
        super(project, requiresMatchedXMLGenerator);
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {

        exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        serviceMethods = new ServiceMethods(context, introspectedTable);
        List<CompilationUnit> answer = new ArrayList<>();
        if (introspectedTable.getTableConfiguration().getJavaClientGeneratorConfiguration() == null
                || !introspectedTable.getTableConfiguration().getJavaClientGeneratorConfiguration().isGenerate()) {
            return answer;
        }
        progressCallback.startTask(getString("Progress.17", //$NON-NLS-1$
                introspectedTable.getFullyQualifiedTable().toString()));
        CommentGenerator commentGenerator = context.getCommentGenerator();
        String targetPackageGen = introspectedTable.getTableConfiguration().getJavaClientGeneratorConfiguration().getTargetPackageGen();
        String mapperFullName = String.join(".", targetPackageGen, "Gen" + introspectedTable.getTableConfiguration().getDomainObjectName() + "Mapper");

        FullyQualifiedJavaType type = new FullyQualifiedJavaType(mapperFullName);
        Interface interfaze = new Interface(type);
        interfaze.setVisibility(JavaVisibility.PUBLIC);
        commentGenerator.addJavaFileComment(interfaze);


        String rootInterface = introspectedTable.getTableConfigurationProperty(PropertyRegistry.ANY_ROOT_INTERFACE);
        if (!stringHasValue(rootInterface)) {
            rootInterface = context.getJavaClientGeneratorConfiguration().getProperty(PropertyRegistry.ANY_ROOT_INTERFACE);
        }

        if (stringHasValue(rootInterface)) {
            FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(rootInterface);
            interfaze.addSuperInterface(fqjt);
            interfaze.addImportedType(fqjt);
        } else {
            FullyQualifiedJavaType infSuperType = new FullyQualifiedJavaType(getMapperInterface(introspectedTable));
            infSuperType.addTypeArgument(entityType);
            infSuperType.addTypeArgument(exampleType);
            interfaze.addSuperInterface(infSuperType);
            interfaze.addImportedType(infSuperType);
        }

//        addCountByExampleMethod(interfaze);
//        addDeleteByExampleMethod(interfaze);
//        addDeleteByPrimaryKeyMethod(interfaze);
//        addInsertMethod(interfaze);
//        addInsertSelectiveMethod(interfaze);
//        addSelectByPrimaryKeyMethod(interfaze);
//        addUpdateByExampleSelectiveMethod(interfaze);
//        addUpdateByPrimaryKeySelectiveMethod(interfaze);
//        addUpdateByExampleWithBLOBsMethod(interfaze);
//        addSelectByExampleWithBLOBsMethod(interfaze);
//        addSelectByExampleWithoutBLOBsMethod(interfaze);
//        addUpdateByPrimaryKeyWithBLOBsMethod(interfaze);
//        addUpdateByExampleWithoutBLOBsMethod(interfaze);
//        addUpdateByPrimaryKeyWithoutBLOBsMethod(interfaze);

        addInsertBatchMethod(interfaze);
        addInsertOrUpdateMethod(interfaze);
        addSelectByExampleWithRelationMethod(interfaze);
        addUpdateBatchMethod(interfaze);
        addSelectByColumnMethods(interfaze);
        addDeleteByColumnMethods(interfaze);
        addSelectBySqlMethodMethods(interfaze);
        addSelectByTableMethods(interfaze);
        addSelectByKeysDictMethod(interfaze);
        addDeleteByTableMethod(interfaze);
        addInsertByTableMethod(interfaze);

        if (context.getPlugins().clientGenerated(interfaze, introspectedTable)) {
            answer.add(interfaze);
        }

        List<CompilationUnit> extraCompilationUnits = getExtraCompilationUnits();
        if (extraCompilationUnits != null) {
            answer.addAll(extraCompilationUnits);
        }

        //生成子类
        JavaClientGeneratorConfiguration javaClientGeneratorConfiguration = introspectedTable.getTableConfiguration().getJavaClientGeneratorConfiguration();
        String daoName = introspectedTable.getTableConfiguration().getDomainObjectName() + "Mapper";
        String sb = javaClientGeneratorConfiguration.getTargetPackage() + "." + daoName;
        Interface subInterface = new Interface(sb);
        subInterface.setVisibility(JavaVisibility.PUBLIC);
        subInterface.addAnnotation("@Mapper");
        subInterface.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Mapper"));
        subInterface.addSuperInterface(type);
        subInterface.addImportedType(type);

        boolean forceGenerateScalableElement = introspectedTable.getRules().isForceGenerateScalableElement(ScalableElementEnum.dao.name());
        boolean fileNotExist = JavaBeansUtil.javaFileNotExist(javaClientGeneratorConfiguration.getTargetProject(), javaClientGeneratorConfiguration.getTargetPackage(), daoName);
        if (forceGenerateScalableElement || fileNotExist) {
            if (context.getPlugins().subClientGenerated(subInterface, introspectedTable)) {
                answer.add(subInterface);
            }
        }

        return answer;
    }

    private void addDeleteByTableMethod(Interface interfaze) {
        for (SelectByTableGeneratorConfiguration configuration : introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration()) {
            if (configuration.isEnableSplit()) {
                AbstractJavaMapperMethodGenerator methodGenerator = new DeleteByTableMethodGenerator(false, configuration);
                initializeAndExecuteGenerator(methodGenerator, interfaze);
            }
        }
    }

    private void addInsertByTableMethod(Interface interfaze) {
        for (SelectByTableGeneratorConfiguration configuration : introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration()) {
            if (configuration.isEnableUnion()) {
                AbstractJavaMapperMethodGenerator methodGenerator = new InsertByTableMethodGenerator(false, configuration);
                initializeAndExecuteGenerator(methodGenerator, interfaze);
            }
        }
    }

    protected void addSelectByKeysDictMethod(Interface interfaze) {
        if (introspectedTable.getRules().isGenerateCachePOWithMultiKey()) {
            AbstractJavaMapperMethodGenerator methodGenerator = new SelectByKeysDictMethodGenerator();
            initializeAndExecuteGenerator(methodGenerator, interfaze);
        }
    }

    protected void addInsertBatchMethod(Interface interfaze) {
        if (introspectedTable.getRules().generateInsertBatch()) {
            AbstractJavaMapperMethodGenerator methodGenerator = new InsertBatchMethodGenerator(false);
            initializeAndExecuteGenerator(methodGenerator, interfaze);
        }
    }

    protected void addInsertOrUpdateMethod(Interface interfaze) {
        if (introspectedTable.getRules().generateInsertOrUpdate()) {
            AbstractJavaMapperMethodGenerator methodGenerator = new InsertOrUpdateMethodGenerator(false);
            initializeAndExecuteGenerator(methodGenerator, interfaze);
        }
    }

    //增加relation方法
    protected void addSelectByExampleWithRelationMethod(Interface interfaze) {
        if (introspectedTable.getRules().generateRelationWithSubSelected()) {
            Method example = serviceMethods.getMethodByType(introspectedTable.getSelectByExampleWithRelationStatementId()
                    , ReturnTypeEnum.LIST,
                    entityType,
                    introspectedTable.getRemarks(true)+"对象列表",
                    exampleType, "example", "查询条件对象", true, interfaze);
            context.getCommentGenerator().addMethodJavaDocLine(example, "带所有子查询（集）的查询方法，该查询方法将执行所有子查询，大量数据返回时慎用。","如果无需返回子查询请使用{@link #selectByExample}方法。");
            interfaze.addMethod(example);
            interfaze.addImportedType(exampleType);
        }
    }

    protected void addUpdateBatchMethod(Interface interfaze) {
        if (introspectedTable.getRules().generateUpdateBatch()) {
            AbstractJavaMapperMethodGenerator methodGenerator = new UpdateBatchMethodGenerator(false);
            initializeAndExecuteGenerator(methodGenerator, interfaze);
        }
    }

    protected void addDeleteByColumnMethods(Interface interfaze){
        introspectedTable.getTableConfiguration().getSelectByColumnGeneratorConfigurations().stream()
                .filter(SelectByColumnGeneratorConfiguration::isEnableDelete)
                .forEach(c->{
                    Method method = serviceMethods.getDeleteByColumnMethod(interfaze, c, true);
                    interfaze.addMethod(method);
                });
    }

    protected void addSelectByColumnMethods(Interface interfaze) {
        if (introspectedTable.getTableConfiguration().getSelectByColumnGeneratorConfigurations().size() > 0) {
            for (SelectByColumnGeneratorConfiguration config : introspectedTable.getTableConfiguration().getSelectByColumnGeneratorConfigurations()) {
                Method method = serviceMethods.getSelectByColumnMethod(entityType, interfaze, config, true, false);
                 interfaze.addMethod(method);
            }
        }
    }

    protected void addSelectBySqlMethodMethods(Interface interfaze) {
        //增加附加选择方法
        introspectedTable.getTableConfiguration().getSelectBySqlMethodGeneratorConfigurations()
                .forEach(config -> {
                    Method method = serviceMethods.getMethodByType(config.getMethodName(),
                            ReturnTypeEnum.LIST,
                            entityType,
                            introspectedTable.getRemarks(true)+"对象列表",
                            FullyQualifiedJavaType.getStringInstance(),
                            config.getParentIdColumn().getJavaProperty(),
                            config.getParentIdColumn().getRemarks(false),
                            true,
                            interfaze);
                    context.getCommentGenerator().addMethodJavaDocLine(method,"基于sql函数["+config.getSqlMethod()+"]的查询");
                    interfaze.addMethod(method);
                });
    }

    protected void addSelectByTableMethods(Interface interfaze) {
        if (introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration() != null
                && introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration().size() > 0) {
            for (SelectByTableGeneratorConfiguration configuration : introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration()) {
                Method selectByTable = serviceMethods.getSelectByTableMethod(entityType, interfaze, configuration, true, false);
                /*Method selectByTable = serviceMethods.getMethodByType(configuration.getMethodName(),
                        ReturnTypeEnum.LIST,
                        configuration.isReturnPrimaryKey() ? FullyQualifiedJavaType.getStringInstance() : entityType,
                        introspectedTable.getRemarks(true)+(configuration.isReturnPrimaryKey() ?"唯一标识列表":"对象列表"),
                        FullyQualifiedJavaType.getStringInstance(),
                        configuration.getParameterName(),
                        "中间表中来自其他表的查询键值",
                        true,
                        interfaze);*/
                /*context.getCommentGenerator().addMethodJavaDocLine(selectByTable,"基于中间表["+configuration.getTableName()+"]的查询");*/
                interfaze.addMethod(selectByTable);
            }
        }
    }

    protected void addCountByExampleMethod(Interface interfaze) {
        if (introspectedTable.getRules().generateCountByExample()) {
            AbstractJavaMapperMethodGenerator methodGenerator = new CountByExampleMethodGenerator();
            initializeAndExecuteGenerator(methodGenerator, interfaze);
        }
    }

    protected void addDeleteByExampleMethod(Interface interfaze) {
        if (introspectedTable.getRules().generateDeleteByExample()) {
            AbstractJavaMapperMethodGenerator methodGenerator = new DeleteByExampleMethodGenerator();
            initializeAndExecuteGenerator(methodGenerator, interfaze);
        }
    }

    protected void addDeleteByPrimaryKeyMethod(Interface interfaze) {
        if (introspectedTable.getRules().generateDeleteByPrimaryKey()) {
            AbstractJavaMapperMethodGenerator methodGenerator = new DeleteByPrimaryKeyMethodGenerator(false);
            initializeAndExecuteGenerator(methodGenerator, interfaze);
        }
    }

    protected void addInsertMethod(Interface interfaze) {
        if (introspectedTable.getRules().generateInsert()) {
            AbstractJavaMapperMethodGenerator methodGenerator = new InsertMethodGenerator(false);
            initializeAndExecuteGenerator(methodGenerator, interfaze);
        }
    }

    protected void addInsertSelectiveMethod(Interface interfaze) {
        if (introspectedTable.getRules().generateInsertSelective()) {
            AbstractJavaMapperMethodGenerator methodGenerator = new InsertSelectiveMethodGenerator();
            initializeAndExecuteGenerator(methodGenerator, interfaze);
        }
    }

    protected void addSelectByExampleWithBLOBsMethod(Interface interfaze) {
        if (introspectedTable.getRules().generateSelectByExampleWithBLOBs()) {
            AbstractJavaMapperMethodGenerator methodGenerator = new SelectByExampleWithBLOBsMethodGenerator();
            initializeAndExecuteGenerator(methodGenerator, interfaze);
        }
    }

    protected void addSelectByExampleWithoutBLOBsMethod(Interface interfaze) {
        if (introspectedTable.getRules().generateSelectByExampleWithoutBLOBs()) {
            AbstractJavaMapperMethodGenerator methodGenerator = new SelectByExampleWithoutBLOBsMethodGenerator();
            initializeAndExecuteGenerator(methodGenerator, interfaze);
        }
    }

    protected void addSelectByPrimaryKeyMethod(Interface interfaze) {
        if (introspectedTable.getRules().generateSelectByPrimaryKey()) {
            AbstractJavaMapperMethodGenerator methodGenerator = new SelectByPrimaryKeyMethodGenerator(false);
            initializeAndExecuteGenerator(methodGenerator, interfaze);
        }
    }

    protected void addUpdateByExampleSelectiveMethod(Interface interfaze) {
        if (introspectedTable.getRules().generateUpdateByExampleSelective()) {
            AbstractJavaMapperMethodGenerator methodGenerator = new UpdateByExampleSelectiveMethodGenerator();
            initializeAndExecuteGenerator(methodGenerator, interfaze);
        }
    }

    protected void addUpdateByExampleWithBLOBsMethod(Interface interfaze) {
        if (introspectedTable.getRules().generateUpdateByExampleWithBLOBs()) {
            AbstractJavaMapperMethodGenerator methodGenerator = new UpdateByExampleWithBLOBsMethodGenerator();
            initializeAndExecuteGenerator(methodGenerator, interfaze);
        }
    }

    protected void addUpdateByExampleWithoutBLOBsMethod(Interface interfaze) {
        if (introspectedTable.getRules().generateUpdateByExampleWithoutBLOBs()) {
            AbstractJavaMapperMethodGenerator methodGenerator = new UpdateByExampleWithoutBLOBsMethodGenerator();
            initializeAndExecuteGenerator(methodGenerator, interfaze);
        }
    }

    protected void addUpdateByPrimaryKeySelectiveMethod(Interface interfaze) {
        if (introspectedTable.getRules().generateUpdateByPrimaryKeySelective()) {
            AbstractJavaMapperMethodGenerator methodGenerator = new UpdateByPrimaryKeySelectiveMethodGenerator();
            initializeAndExecuteGenerator(methodGenerator, interfaze);
        }
    }

    protected void addUpdateByPrimaryKeyWithBLOBsMethod(Interface interfaze) {
        if (introspectedTable.getRules().generateUpdateByPrimaryKeyWithBLOBs()) {
            AbstractJavaMapperMethodGenerator methodGenerator = new UpdateByPrimaryKeyWithBLOBsMethodGenerator();
            initializeAndExecuteGenerator(methodGenerator, interfaze);
        }
    }

    protected void addUpdateByPrimaryKeyWithoutBLOBsMethod(Interface interfaze) {
        if (introspectedTable.getRules()
                .generateUpdateByPrimaryKeyWithoutBLOBs()) {
            AbstractJavaMapperMethodGenerator methodGenerator = new UpdateByPrimaryKeyWithoutBLOBsMethodGenerator();
            initializeAndExecuteGenerator(methodGenerator, interfaze);
        }
    }

    protected void initializeAndExecuteGenerator(AbstractJavaMapperMethodGenerator methodGenerator,
                                                 Interface interfaze) {
        methodGenerator.setContext(context);
        methodGenerator.setIntrospectedTable(introspectedTable);
        methodGenerator.setProgressCallback(progressCallback);
        methodGenerator.setWarnings(warnings);
        methodGenerator.addInterfaceElements(interfaze);
    }

    public List<CompilationUnit> getExtraCompilationUnits() {
        return Collections.emptyList();
    }

    @Override
    public AbstractXmlGenerator getMatchedXMLGenerator() {
        return new XMLMapperGenerator();
    }

    @Override
    public AbstractHtmlGenerator getMatchedHTMLGenerator() {
        return new HTMLGenerator();
    }

    /**
     * 获得mapper接口
     */
    private String getMapperInterface(IntrospectedTable introspectedTable) {
        if (GenerateUtils.isBlobInstance(introspectedTable)) {
            return MBG_MAPPER_BLOB_INTERFACE;
        }
        return MBG_MAPPER_INTERFACE;
    }
}
