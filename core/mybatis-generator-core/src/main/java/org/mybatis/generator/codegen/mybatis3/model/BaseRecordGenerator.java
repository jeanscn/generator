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
package org.mybatis.generator.codegen.mybatis3.model;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.codegen.RootClassInfo;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.config.OverridePropertyValueGeneratorConfiguration;
import org.mybatis.generator.config.VoAdditionalPropertyGeneratorConfiguration;
import org.mybatis.generator.custom.ModelClassTypeEnum;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;
import org.mybatis.generator.internal.util.VoGenService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.mybatis.generator.internal.util.JavaBeansUtil.*;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

public class BaseRecordGenerator extends AbstractJavaGenerator {

    public BaseRecordGenerator(String project) {
        super(project);
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        List<CompilationUnit> answer = new ArrayList<>();

        JavaModelGeneratorConfiguration configuration = introspectedTable.getTableConfiguration().getJavaModelGeneratorConfiguration();
        if (configuration ==null || !configuration.isGenerate()) {
            return answer;
        }


        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString("Progress.8", table.toString())); //$NON-NLS-1$
        Plugin plugins = context.getPlugins();
        CommentGenerator commentGenerator = context.getCommentGenerator();

        //类名
        FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        TopLevelClass topLevelClass = new TopLevelClass(type);
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        //类文件注释
        commentGenerator.addJavaFileComment(topLevelClass);
        //父类
        FullyQualifiedJavaType superClass = getSuperClass();
        if (superClass != null) {
            topLevelClass.setSuperClass(superClass);
            topLevelClass.addImportedType(superClass);
            //泛型
            List<String> rootClassTypeArguments = getRootClassTypeArguments(introspectedTable);
            rootClassTypeArguments.forEach(s-> superClass.addTypeArgument(new FullyQualifiedJavaType(s)));
        }
        String superInterface = introspectedTable.getTableConfiguration().getProperty("superInterface");
        if (StringUtility.stringHasValue(superInterface)) {
            Arrays.stream(superInterface.split(","))
                    .map(FullyQualifiedJavaType::new)
                    .forEach(s->{
                        topLevelClass.addSuperInterface(s);
                        topLevelClass.addImportedType(s);
                    });
        }
        //类注释
        commentGenerator.addModelClassComment(topLevelClass, introspectedTable);

        List<IntrospectedColumn> introspectedColumns = getColumnsInThisClass();

        if (introspectedTable.isConstructorBased()) {
            addParameterizedConstructor(topLevelClass, introspectedTable.getNonBLOBColumns());

            if (includeBLOBColumns()) {
                addParameterizedConstructor(topLevelClass, introspectedTable.getAllColumns());
            }

            if (!introspectedTable.isImmutable()) {
                addDefaultConstructor(topLevelClass);
            }
        }

        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            if (RootClassInfo.getInstance(getRootClass(), warnings).containsProperty(introspectedColumn)) {
                continue;
            }

            Field field = getJavaBeansField(introspectedColumn, context, introspectedTable);
            if (plugins.modelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable,
                    Plugin.ModelClassType.BASE_RECORD)) {
                topLevelClass.addField(field);
                topLevelClass.addImportedType(field.getType());
            }

            // 生成getter和setter方法
            Method method = getJavaBeansGetter(introspectedColumn, context, introspectedTable);
            if (plugins.modelGetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable,
                    Plugin.ModelClassType.BASE_RECORD)) {
                StringBuilder sb = new StringBuilder(introspectedColumn.getJavaProperty());
                if (sb.length()>1 && Character.isUpperCase(sb.charAt(1))) {
                    topLevelClass.addMethod(method);
                }
            }
            if (!introspectedTable.isImmutable()) {
                method = getJavaBeansSetter(introspectedColumn, context, introspectedTable);
                if (plugins.modelSetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable,
                        Plugin.ModelClassType.BASE_RECORD)) {
                    StringBuilder sb = new StringBuilder(introspectedColumn.getJavaProperty());
                    if (sb.length()>1 && Character.isUpperCase(sb.charAt(1))) {
                        topLevelClass.addMethod(method);
                    }
                }
            }
        }


        //增加映射
        VoGenService voGenService = new VoGenService(introspectedTable);
        List<OverridePropertyValueGeneratorConfiguration> overrideProperty = configuration.getOverridePropertyConfigurations();
        voGenService.buildOverrideColumn(overrideProperty, topLevelClass, ModelClassTypeEnum.modelClass).forEach(f->{
            plugins.modelFieldGenerated(f, topLevelClass, null, introspectedTable,Plugin.ModelClassType.BASE_RECORD);
        });

        //附加属性
        List<VoAdditionalPropertyGeneratorConfiguration> additionalProperty = configuration.getAdditionalPropertyConfigurations();
        List<Field> addtionalPropertiesFields = topLevelClass.getAddtionalPropertiesFields(additionalProperty);
        addtionalPropertiesFields.forEach(f->{
            if (plugins.modelFieldGenerated(f, topLevelClass, null, introspectedTable,
                    Plugin.ModelClassType.BASE_RECORD)) {
                topLevelClass.addField(f);
                topLevelClass.addImportedType(f.getType());
            }
        });

        if (context.getPlugins().modelBaseRecordClassGenerated(topLevelClass, introspectedTable)) {
            answer.add(topLevelClass);
        }
        return answer;
    }

    /**
     * 内部方法：获得父类
     */
    private FullyQualifiedJavaType getSuperClass() {
        if (introspectedTable.getRules().generatePrimaryKeyClass()) {
            return new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
        } else {
            String rootClass = JavaBeansUtil.getRootClass(introspectedTable);
            if (rootClass != null) {
                return new FullyQualifiedJavaType(rootClass);
            }
        }

        return null;
    }

    /**
     * 内部方法：是否包含主键列
     */
    private boolean includePrimaryKeyColumns() {
        return !introspectedTable.getRules().generatePrimaryKeyClass() && introspectedTable.hasPrimaryKeyColumns();
    }

    /**
     * 内部方法：是否包含大字段列
     */
    private boolean includeBLOBColumns() {
        return !introspectedTable.getRules().generateRecordWithBLOBsClass()
                && introspectedTable.hasBLOBColumns();
    }

    /**
     * 内部方法：增加带参构造器
     */
    private void addParameterizedConstructor(TopLevelClass topLevelClass, List<IntrospectedColumn> constructorColumns) {
        Method method = new Method(topLevelClass.getType().getShortName());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setConstructor(true);
        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

        for (IntrospectedColumn introspectedColumn : constructorColumns) {
            method.addParameter(new Parameter(introspectedColumn.getFullyQualifiedJavaType(),
                    introspectedColumn.getJavaProperty()));
            topLevelClass.addImportedType(introspectedColumn.getFullyQualifiedJavaType());
        }

        StringBuilder sb = new StringBuilder();
        List<String> superColumns = new LinkedList<>();
        if (introspectedTable.getRules().generatePrimaryKeyClass()) {
            boolean comma = false;
            sb.append("super("); //$NON-NLS-1$
            for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
                if (comma) {
                    sb.append(", "); //$NON-NLS-1$
                } else {
                    comma = true;
                }
                sb.append(introspectedColumn.getJavaProperty());
                superColumns.add(introspectedColumn.getActualColumnName());
            }
            sb.append(");"); //$NON-NLS-1$
            method.addBodyLine(sb.toString());
        }

        for (IntrospectedColumn introspectedColumn : constructorColumns) {
            if (!superColumns.contains(introspectedColumn.getActualColumnName())) {
                sb.setLength(0);
                sb.append("this."); //$NON-NLS-1$
                sb.append(introspectedColumn.getJavaProperty());
                sb.append(" = "); //$NON-NLS-1$
                sb.append(introspectedColumn.getJavaProperty());
                sb.append(';');
                method.addBodyLine(sb.toString());
            }
        }

        topLevelClass.addMethod(method);
    }

    /**
     * 内部方法：获得生成属性的列
     */
    private List<IntrospectedColumn> getColumnsInThisClass() {
        List<IntrospectedColumn> introspectedColumns;
        if (includePrimaryKeyColumns()) {
            if (includeBLOBColumns()) {
                introspectedColumns = introspectedTable.getAllColumns();
            } else {
                introspectedColumns = introspectedTable.getNonBLOBColumns();
            }
        } else {
            if (includeBLOBColumns()) {
                introspectedColumns = introspectedTable.getNonPrimaryKeyColumns();
            } else {
                introspectedColumns = introspectedTable.getBaseColumns();
            }
        }

        return introspectedColumns;
    }
}
