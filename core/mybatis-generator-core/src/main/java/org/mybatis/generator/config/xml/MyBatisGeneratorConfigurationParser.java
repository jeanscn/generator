/*
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
package org.mybatis.generator.config.xml;

import com.vgosoft.core.constant.GlobalConstant;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.config.*;
import org.mybatis.generator.custom.RelationTypeEnum;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.ObjectFactory;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static org.mybatis.generator.internal.util.StringUtility.*;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * This class parses configuration files into the new Configuration API.
 *
 * @author Jeff Butler
 */
public class MyBatisGeneratorConfigurationParser {
    private final Properties extraProperties;
    private final Properties configurationProperties;

    public MyBatisGeneratorConfigurationParser(Properties extraProperties) {
        super();
        if (extraProperties == null) {
            this.extraProperties = new Properties();
        } else {
            this.extraProperties = extraProperties;
        }
        configurationProperties = new Properties();
    }

    public Configuration parseConfiguration(Element rootNode)
            throws XMLParserException {

        Configuration configuration = new Configuration();

        NodeList nodeList = rootNode.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);

            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            switch (childNode.getNodeName()) {
                case "properties":  //$NON-NLS-1$
                    parseProperties(childNode);
                    break;
                case "classPathEntry":  //$NON-NLS-1$
                    parseClassPathEntry(configuration, childNode);
                    break;
                case "context":  //$NON-NLS-1$
                    parseContext(configuration, childNode);
                    break;
            }
        }

        return configuration;
    }

    protected void parseProperties(Node node)
            throws XMLParserException {
        Properties attributes = parseAttributes(node);
        String resource = attributes.getProperty("resource"); //$NON-NLS-1$
        String url = attributes.getProperty("url"); //$NON-NLS-1$

        if (!stringHasValue(resource)
                && !stringHasValue(url)) {
            throw new XMLParserException(getString("RuntimeError.14")); //$NON-NLS-1$
        }

        if (stringHasValue(resource)
                && stringHasValue(url)) {
            throw new XMLParserException(getString("RuntimeError.14")); //$NON-NLS-1$
        }

        URL resourceUrl;

        try {
            if (stringHasValue(resource)) {
                resourceUrl = ObjectFactory.getResource(resource);
                if (resourceUrl == null) {
                    throw new XMLParserException(getString(
                            "RuntimeError.15", resource)); //$NON-NLS-1$
                }
            } else {
                resourceUrl = new URL(url);
            }

            InputStream inputStream = resourceUrl.openConnection()
                    .getInputStream();

            configurationProperties.load(inputStream);
            inputStream.close();
        } catch (IOException e) {
            if (stringHasValue(resource)) {
                throw new XMLParserException(getString(
                        "RuntimeError.16", resource)); //$NON-NLS-1$
            } else {
                throw new XMLParserException(getString(
                        "RuntimeError.17", url)); //$NON-NLS-1$
            }
        }
    }

    private void parseContext(Configuration configuration, Node node) {

        Properties attributes = parseAttributes(node);
        String defaultModelType = attributes.getProperty("defaultModelType"); //$NON-NLS-1$
        String targetRuntime = attributes.getProperty("targetRuntime"); //$NON-NLS-1$
        String introspectedColumnImpl = attributes
                .getProperty("introspectedColumnImpl"); //$NON-NLS-1$
        String id = attributes.getProperty("id"); //$NON-NLS-1$

        ModelType mt = defaultModelType == null ? null : ModelType
                .getModelType(defaultModelType);

        Context context = new Context(mt);
        context.setId(id);
        if (stringHasValue(introspectedColumnImpl)) {
            context.setIntrospectedColumnImpl(introspectedColumnImpl);
        }
        if (stringHasValue(targetRuntime)) {
            context.setTargetRuntime(targetRuntime);
        }

        String appKeyword = attributes.getProperty(PropertyRegistry.CONTEXT_APPLICATION_KEYWORD);
        context.setAppKeyword(appKeyword);

        String moduleKeyword = attributes.getProperty(PropertyRegistry.CONTEXT_MODULE_KEYWORD);
        context.setModuleKeyword(moduleKeyword);

        String moduleName = attributes.getProperty(PropertyRegistry.CONTEXT_MODULE_NAME);
        context.setModuleName(moduleName);

        context.setParentMenuId(attributes.getProperty("parentMenuId"));
        String integrateMybatisPlus = attributes.getProperty(PropertyRegistry.CONTEXT_INTEGRATE_MYBATIS_PLUS);
        context.setIntegrateMybatisPlus(!stringHasValue(integrateMybatisPlus) || Boolean.parseBoolean(integrateMybatisPlus));
        String integrateSpringSecurity = attributes.getProperty(PropertyRegistry.CONTEXT_INTEGRATE_SPRING_SECURITY);
        context.setIntegrateSpringSecurity(!stringHasValue(integrateSpringSecurity) || Boolean.parseBoolean(integrateSpringSecurity));
        String forceUpdateScalableElement = attributes.getProperty(PropertyRegistry.CONTEXT_FORCE_UPDATE_SCALABLE_ELEMENT);
        context.setForceUpdateScalableElement(stringHasValue(forceUpdateScalableElement) && Boolean.parseBoolean(forceUpdateScalableElement));
        String jdkVersion = attributes.getProperty("jdkVersion", "8");
        context.setJdkVersion(Integer.parseInt(jdkVersion));

        String onlyTables = attributes.getProperty("onlyTables");
        if (stringHasValue(onlyTables)) {
            context.setOnlyTablesGenerate(spiltToList(onlyTables));
        }

        String updateModuleData = attributes.getProperty("updateModuleData", "false");
        context.setUpdateModuleData(Boolean.parseBoolean(updateModuleData));

        String updateMenuData = attributes.getProperty("updateMenuData", "false");
        context.setUpdateMenuData(Boolean.parseBoolean(updateMenuData));

        configuration.addContext(context);

        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);

            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            switch (childNode.getNodeName()) {
                case "property":  //$NON-NLS-1$
                    parseProperty(context, childNode);
                    break;
                case "plugin":  //$NON-NLS-1$
                    parsePlugin(context, childNode);
                    break;
                case "commentGenerator":  //$NON-NLS-1$
                    parseCommentGenerator(context, childNode);
                    break;
                case "jdbcConnection":  //$NON-NLS-1$
                    parseJdbcConnection(context, childNode);
                    break;
                case "connectionFactory":  //$NON-NLS-1$
                    parseConnectionFactory(context, childNode);
                    break;
                case "javaModelGenerator":  //$NON-NLS-1$
                    parseJavaModelGenerator(context, childNode);
                    break;
                case "javaTypeResolver":  //$NON-NLS-1$
                    parseJavaTypeResolver(context, childNode);
                    break;
                case "sqlMapGenerator":  //$NON-NLS-1$
                    parseSqlMapGenerator(context, childNode);
                    break;
                case "javaClientGenerator":  //$NON-NLS-1$
                    parseJavaClientGenerator(context, childNode);
                    break;
                case "table":  //$NON-NLS-1$
                    parseTable(context, childNode);
                    break;
            }
        }
    }

    private List<String> spiltToList(String str) {
        List<String> ret = new ArrayList<>();
        if (stringHasValue(str)) {
            String[] split = str.split("[,;，；、]");
            Collections.addAll(ret, split);
        }
        return ret;
    }

    protected void parseSqlMapGenerator(Context context, Node node) {
        SqlMapGeneratorConfiguration sqlMapGeneratorConfiguration = new SqlMapGeneratorConfiguration();

        context.setSqlMapGeneratorConfiguration(sqlMapGeneratorConfiguration);

        Properties attributes = parseAttributes(node);
        String targetPackage = attributes.getProperty(PropertyRegistry.ANY_TARGET_PACKAGE); //$NON-NLS-1$
        String targetProject = attributes.getProperty(PropertyRegistry.ANY_TARGET_PROJECT); //$NON-NLS-1$

        sqlMapGeneratorConfiguration.setTargetPackage(targetPackage);
        sqlMapGeneratorConfiguration.setTargetProject(targetProject);

        parseChildNodeOnlyProperty(sqlMapGeneratorConfiguration, node);
    }

    protected void parseTable(Context context, Node node) {
        //未配置的context的必须项目，使用默认值
        context.initDefault();

        TableConfiguration tc = new TableConfiguration(context);
        Properties attributes = parseAttributes(node);
        String tableName = attributes.getProperty("tableName");
        String ignore = attributes.getProperty("ignore");
        String domainObjectName = attributes.getProperty("domainObjectName");
        if (!stringHasValue(domainObjectName)) {
            domainObjectName = JavaBeansUtil.getCamelCaseString(tableName, true);
        }
        //先确认是否指定了生成范围
        List<String> tables = context.getOnlyTablesGenerate();
        if (tables.size() == 0) {                   //未指定生成范围
            if (!stringHasValue(ignore)) {          //未指定忽略 则判断是否已经生成过
                JavaModelGeneratorConfiguration gc = context.getJavaModelGeneratorConfiguration();
                if (JavaBeansUtil.javaFileExist(gc.getTargetProject(), gc.getTargetPackage(), domainObjectName)) {
                    tc.setIgnore(true);
                    return;
                }
                tc.setIgnore(false);
            }else{
                tc.setIgnore(isTrue(ignore));
                if (Boolean.parseBoolean(ignore)) {
                    return;
                }
            }
        } else if (!tables.contains(tableName)) {   //指定了生成范围但是未不包含当前表
            return;
        }
        context.addTableConfiguration(tc);
        //表名相关
        String catalog = attributes.getProperty("catalog"); //$NON-NLS-1$
        if (stringHasValue(catalog)) {
            tc.setCatalog(catalog);
        }
        String schema = attributes.getProperty("schema"); //$NON-NLS-1$
        if (stringHasValue(schema)) {
            tc.setSchema(schema);
        }
        if (stringHasValue(tableName)) {
            tc.setTableName(tableName);
        }
        if (stringHasValue(domainObjectName)) {
            tc.setDomainObjectName(domainObjectName);
        }
        String alias = attributes.getProperty("alias"); //$NON-NLS-1$
        if (stringHasValue(alias)) {
            tc.setAlias(alias);
        }

        String tableType = attributes.getProperty("tableType"); //$NON-NLS-1$
        if (stringHasValue(tableType)) {
            tc.setTableType(tableType);
        }
        String isModules = attributes.getProperty("isModules");
        if (stringHasValue(isModules)) {
            tc.setModules(isTrue(isModules));
        }

        String validateIgnoreColumns = attributes.getProperty("validateIgnoreColumns");
        if (validateIgnoreColumns != null) {
            if (VStringUtil.isEmpty(validateIgnoreColumns)) {
                tc.setValidateIgnoreColumns(new ArrayList<>());
            } else {
                List<String> collect = spiltToList(validateIgnoreColumns).stream().distinct().collect(Collectors.toList());
                tc.setValidateIgnoreColumns(collect);
            }
        } else {
            tc.setValidateIgnoreColumns(Arrays.asList("delete_flag", "version_", "created_", "modified_", "created_id", "modified_id"));
        }

        //service及HTML根路径
        String serviceApiBasePath = Optional.ofNullable(context.getModuleKeyword())
                .orElse(Optional.ofNullable(context.getProperty(PropertyRegistry.CONTEXT_HTML_TARGET_PACKAGE)).orElse("app"));
        tc.setServiceApiBasePath(VStringUtil.toHyphenCase(serviceApiBasePath));
        String htmlBasePath = Optional.ofNullable(context.getProperty(PropertyRegistry.CONTEXT_HTML_TARGET_PACKAGE))
                .orElse(Optional.ofNullable(context.getModuleKeyword()).orElse("html"));
        tc.setHtmlBasePath(htmlBasePath.toLowerCase());

        String enableInsert = attributes.getProperty("enableInsert"); //$NON-NLS-1$
        if (stringHasValue(enableInsert)) {
            tc.setInsertStatementEnabled(isTrue(enableInsert));
        }

        String enableSelectByPrimaryKey = attributes
                .getProperty("enableSelectByPrimaryKey"); //$NON-NLS-1$
        if (stringHasValue(enableSelectByPrimaryKey)) {
            tc.setSelectByPrimaryKeyStatementEnabled(
                    isTrue(enableSelectByPrimaryKey));
        }

        String enableSelectByExample = attributes
                .getProperty("enableSelectByExample"); //$NON-NLS-1$
        if (stringHasValue(enableSelectByExample)) {
            tc.setSelectByExampleStatementEnabled(
                    isTrue(enableSelectByExample));
        }

        String enableUpdateByPrimaryKey = attributes
                .getProperty("enableUpdateByPrimaryKey"); //$NON-NLS-1$
        if (stringHasValue(enableUpdateByPrimaryKey)) {
            tc.setUpdateByPrimaryKeyStatementEnabled(
                    isTrue(enableUpdateByPrimaryKey));
        }

        String enableDeleteByPrimaryKey = attributes
                .getProperty("enableDeleteByPrimaryKey"); //$NON-NLS-1$
        if (stringHasValue(enableDeleteByPrimaryKey)) {
            tc.setDeleteByPrimaryKeyStatementEnabled(
                    isTrue(enableDeleteByPrimaryKey));
        }

        String enableDeleteByExample = attributes
                .getProperty("enableDeleteByExample"); //$NON-NLS-1$
        if (stringHasValue(enableDeleteByExample)) {
            tc.setDeleteByExampleStatementEnabled(
                    isTrue(enableDeleteByExample));
        }

        String enableCountByExample = attributes
                .getProperty("enableCountByExample"); //$NON-NLS-1$
        if (stringHasValue(enableCountByExample)) {
            tc.setCountByExampleStatementEnabled(
                    isTrue(enableCountByExample));
        }

        String enableUpdateByExample = attributes
                .getProperty("enableUpdateByExample"); //$NON-NLS-1$
        if (stringHasValue(enableUpdateByExample)) {
            tc.setUpdateByExampleStatementEnabled(
                    isTrue(enableUpdateByExample));
        }

        String enableUpdateBatch = attributes.getProperty(PropertyRegistry.TABLE_ENABLE_UPDATE_BATCH);
        if (stringHasValue(enableUpdateBatch)) {
            tc.setUpdateBatchStatementEnabled(Boolean.parseBoolean(enableUpdateBatch));
        }

        String enableInsertBatch = attributes.getProperty(PropertyRegistry.TABLE_ENABLE_INSERT_BATCH);
        if (stringHasValue(enableInsertBatch)) {
            tc.setInsertBatchStatementEnabled(Boolean.parseBoolean(enableInsertBatch));
        }

        String enableInsertOrUpdate = attributes.getProperty(PropertyRegistry.TABLE_ENABLE_INSERT_OR_UPDATE);
        if (stringHasValue(enableInsertOrUpdate)) {
            tc.setInsertOrUpdateStatementEnabled(Boolean.parseBoolean(enableInsertOrUpdate));
        }

        String enableFileUpload = attributes.getProperty(PropertyRegistry.TABLE_ENABLE_FILE_UPLOAD);
        if (stringHasValue(enableFileUpload)) {
            tc.setFileUploadStatementEnabled(Boolean.parseBoolean(enableFileUpload));
        }

        String selectByPrimaryKeyQueryId = attributes
                .getProperty("selectByPrimaryKeyQueryId"); //$NON-NLS-1$
        if (stringHasValue(selectByPrimaryKeyQueryId)) {
            tc.setSelectByPrimaryKeyQueryId(selectByPrimaryKeyQueryId);
        }

        String selectByExampleQueryId = attributes
                .getProperty("selectByExampleQueryId"); //$NON-NLS-1$
        if (stringHasValue(selectByExampleQueryId)) {
            tc.setSelectByExampleQueryId(selectByExampleQueryId);
        }

        String modelType = attributes.getProperty("modelType"); //$NON-NLS-1$
        if (stringHasValue(modelType)) {
            tc.setConfiguredModelType(modelType);
        }

        //避免通配符，设置为true可以帮助抵御SQL注入
        String escapeWildcards = attributes.getProperty("escapeWildcards"); //$NON-NLS-1$
        if (stringHasValue(escapeWildcards)) {
            tc.setWildcardEscapingEnabled(isTrue(escapeWildcards));
        }

        String delimitIdentifiers = attributes
                .getProperty("delimitIdentifiers"); //$NON-NLS-1$
        if (stringHasValue(delimitIdentifiers)) {
            tc.setDelimitIdentifiers(isTrue(delimitIdentifiers));
        }

        String delimitAllColumns = attributes.getProperty("delimitAllColumns"); //$NON-NLS-1$
        if (stringHasValue(delimitAllColumns)) {
            tc.setAllColumnDelimitingEnabled(isTrue(delimitAllColumns));
        }

        String mapperName = attributes.getProperty("mapperName"); //$NON-NLS-1$
        if (stringHasValue(mapperName)) {
            tc.setMapperName(mapperName);
        }

        String sqlProviderName = attributes.getProperty("sqlProviderName"); //$NON-NLS-1$
        if (stringHasValue(sqlProviderName)) {
            tc.setSqlProviderName(sqlProviderName);
        }

        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);

            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            switch (childNode.getNodeName()) {
                case "property":  //$NON-NLS-1$
                    parseProperty(tc, childNode);
                    break;
                case "columnOverride":  //$NON-NLS-1$
                    parseColumnOverride(tc, childNode);
                    break;
                case "ignoreColumn":  //$NON-NLS-1$
                    parseIgnoreColumn(tc, childNode);
                    break;
                case "ignoreColumnsByRegex":  //$NON-NLS-1$
                    parseIgnoreColumnByRegex(tc, childNode);
                    break;
                case "generatedKey":  //$NON-NLS-1$
                    parseGeneratedKey(tc, childNode);
                    break;
                case "domainObjectRenamingRule":  //$NON-NLS-1$
                    parseDomainObjectRenamingRule(tc, childNode);
                    break;
                case "columnRenamingRule":  //$NON-NLS-1$
                    parseColumnRenamingRule(tc, childNode);
                    break;
                case "selectByTable":  //$NON-NLS-1$
                    parseSelectByTable(tc, childNode);
                    break;
                case "selectByColumn":  //$NON-NLS-1$
                    parseSelectByColumn(tc, childNode);
                    break;
                case "selectBySqlMethod":  //$NON-NLS-1$
                    parseSelectBySqlMethod(tc, childNode);
                    break;
                case "javaModelAssociation":  //$NON-NLS-1$
                    parseJavaModelRelation(tc, childNode, RelationTypeEnum.association);
                    break;
                case "javaModelCollection":  //$NON-NLS-1$
                    parseJavaModelRelation(tc, childNode, RelationTypeEnum.collection);
                    break;
                case "generateHtml":  //$NON-NLS-1$
                    parseGenerateHtml(context, tc, childNode);
                    break;
                case "generateService":
                    parseGenerateService(context, tc, childNode);
                    break;
                case "generateController":
                    parseGenerateController(context, tc, childNode);
                    break;
                case "generateDao":
                    parseGenerateDao(context, tc, childNode);
                    break;
                case "generateModel":
                    parseGenerateModel(context, tc, childNode);
                    break;
                case "generateSqlMap":
                    parseGenerateSqlMap(context, tc, childNode);
                    break;
                case "generateSqlSchema":
                    parseGenerateSqlSchema(context, tc, childNode);
                    break;
                case "generateVO":
                    parseGenerateVO(context, tc, childNode);
                    break;
                case "generateCachePO":
                    parseGenerateCachePO(context, tc, childNode);
                    break;
            }
        }
        //如果未指定，则设置缺省父类
        if (tc.getProperty("rootClass") == null) {
            tc.addProperty("rootClass", "com.vgosoft.core.entity.abs.AbstractEntity");
        }
        //如果未指定，则设置JavaModelGenerator默认值
        if (tc.getJavaModelGeneratorConfiguration() == null) {
            JavaModelGeneratorConfiguration modelConfiguration = new JavaModelGeneratorConfiguration();
            //继承context的配置
            JavaModelGeneratorConfiguration contextConfiguration = context.getJavaModelGeneratorConfiguration();
            modelConfiguration.setTargetPackage(contextConfiguration.getTargetPackage());
            modelConfiguration.setTargetProject(contextConfiguration.getTargetProject());
            modelConfiguration.setBaseTargetPackage(contextConfiguration.getBaseTargetPackage());
            modelConfiguration.setTargetPackageGen(contextConfiguration.getTargetPackageGen());
            modelConfiguration.setGenerate(true);
            modelConfiguration.setBaseTargetPackage(context.getJavaModelGeneratorConfiguration().getBaseTargetPackage());
            tc.setJavaModelGeneratorConfiguration(modelConfiguration);
        }

        //如果未指定，则设置JavaClientGenerator默认值
        if (tc.getSqlMapGeneratorConfiguration() == null) {
            SqlMapGeneratorConfiguration sqlMapGeneratorConfiguration = new SqlMapGeneratorConfiguration();
            sqlMapGeneratorConfiguration.setGenerate(true);
            sqlMapGeneratorConfiguration.setTargetPackage(context.getSqlMapGeneratorConfiguration().getTargetPackage());
            sqlMapGeneratorConfiguration.setTargetProject(context.getSqlMapGeneratorConfiguration().getTargetProject());
            sqlMapGeneratorConfiguration.setBaseTargetPackage(context.getJavaModelGeneratorConfiguration().getBaseTargetPackage());
            ;
            tc.setSqlMapGeneratorConfiguration(sqlMapGeneratorConfiguration);
        }

        //如果未指定，则设置SqlSchemaGenerator默认值
        if (tc.getSqlSchemaGeneratorConfiguration() == null) {
            SqlSchemaGeneratorConfiguration configuration = new SqlSchemaGeneratorConfiguration(context, tc);
            configuration.setGenerate(true);
            configuration.setBaseTargetPackage(context.getJavaModelGeneratorConfiguration().getBaseTargetPackage());
            tc.setSqlSchemaGeneratorConfiguration(configuration);
        }
        //如果未指定，则设置JavaModelGenerator默认值
        if (tc.getJavaServiceGeneratorConfiguration() == null) {
            JavaServiceGeneratorConfiguration config = new JavaServiceGeneratorConfiguration(context);
            config.setGenerate(true);
            config.setSubTargetPackage("service");
            config.setBaseTargetPackage(context.getJavaModelGeneratorConfiguration().getBaseTargetPackage());
            JavaServiceImplGeneratorConfiguration configImpl = new JavaServiceImplGeneratorConfiguration(context);
            configImpl.setGenerate(true);
            configImpl.setSubTargetPackage("service.impl");
            configImpl.setGenerateUnitTest(false);
            configImpl.setBaseTargetPackage(context.getJavaModelGeneratorConfiguration().getBaseTargetPackage());
            if (context.getJavaModelGeneratorConfiguration().getTargetPackage() != null) {
                String baseTargetPackage = substringBeforeLast(context.getJavaModelGeneratorConfiguration().getTargetPackage(), ".");
                config.setBaseTargetPackage(baseTargetPackage);
                configImpl.setBaseTargetPackage(baseTargetPackage);
            }
            configImpl.setNoServiceAnnotation(false);
            if (context.getJavaModelGeneratorConfiguration().getTargetProject() != null) {
                String targetProject = context.getJavaModelGeneratorConfiguration().getTargetProject();
                config.setTargetProject(StringUtility.getTargetProject(targetProject));
                configImpl.setTargetProject(StringUtility.getTargetProject(targetProject));
            }
            tc.setJavaServiceGeneratorConfiguration(config);
            tc.setJavaServiceImplGeneratorConfiguration(configImpl);
        }
        //如果未指定，则设置generateDao默认值
        if (tc.getJavaClientGeneratorConfiguration() == null) {
            JavaClientGeneratorConfiguration configuration = new JavaClientGeneratorConfiguration(context);
            configuration.setGenerate(true);
            tc.setJavaClientGeneratorConfiguration(configuration);
        }
        //如果未指定，则设置generateController默认值
        if (tc.getJavaControllerGeneratorConfiguration() == null) {
            JavaControllerGeneratorConfiguration configuration = new JavaControllerGeneratorConfiguration(context);
            if (tc.getHtmlMapGeneratorConfigurations().stream().anyMatch(c->stringHasValue(c.getViewPath()))) {
                configuration.setGenerate(true);
            }else{
                configuration.setGenerate(false);
            }
            configuration.setGenerateUnitTest(false);
            tc.setJavaControllerGeneratorConfiguration(configuration);
        }
        //如果未指定，则设置generateVO默认值
        if (tc.getVoGeneratorConfiguration() == null) {
            VOGeneratorConfiguration configuration = new VOGeneratorConfiguration(context, tc);
            configuration.setGenerate(false);
            tc.setVoGeneratorConfiguration(configuration);
        }
        //如果未指定，则设置generateCachePO默认值
        if (tc.getVoCacheGeneratorConfiguration() == null) {
            VOCacheGeneratorConfiguration configuration = new VOCacheGeneratorConfiguration(context, tc);
            configuration.setGenerate(false);
            tc.setVoCacheGeneratorConfiguration(configuration);
        }
    }

    private void parseColumnOverride(TableConfiguration tc, Node node) {
        Properties attributes = parseAttributes(node);
        String column = attributes.getProperty("column"); //$NON-NLS-1$

        ColumnOverride co = new ColumnOverride(column);

        String property = attributes.getProperty("property"); //$NON-NLS-1$
        if (stringHasValue(property)) {
            co.setJavaProperty(property);
        }

        String javaType = attributes.getProperty("javaType"); //$NON-NLS-1$
        if (stringHasValue(javaType)) {
            co.setJavaType(javaType);
        }

        String jdbcType = attributes.getProperty("jdbcType"); //$NON-NLS-1$
        if (stringHasValue(jdbcType)) {
            co.setJdbcType(jdbcType);
        }

        String typeHandler = attributes.getProperty("typeHandler"); //$NON-NLS-1$
        if (stringHasValue(typeHandler)) {
            co.setTypeHandler(typeHandler);
        }

        String delimitedColumnName = attributes
                .getProperty("delimitedColumnName"); //$NON-NLS-1$
        if (stringHasValue(delimitedColumnName)) {
            co.setColumnNameDelimited(isTrue(delimitedColumnName));
        }

        String isGeneratedAlways = attributes.getProperty("isGeneratedAlways"); //$NON-NLS-1$
        if (stringHasValue(isGeneratedAlways)) {
            co.setGeneratedAlways(Boolean.parseBoolean(isGeneratedAlways));
        }
        parseChildNodeOnlyProperty(co, node);
        tc.addColumnOverride(co);
    }

    private void parseGeneratedKey(TableConfiguration tc, Node node) {
        Properties attributes = parseAttributes(node);
        String column = attributes.getProperty("column");
        boolean identity = isTrue(attributes.getProperty("identity"));
        String sqlStatement = attributes.getProperty("sqlStatement");
        String type = attributes.getProperty("type");
        GeneratedKey gk = new GeneratedKey(column, sqlStatement, identity, type);

        tc.setGeneratedKey(gk);
    }

    private void parseIgnoreColumn(TableConfiguration tc, Node node) {
        Properties attributes = parseAttributes(node);
        String column = attributes.getProperty("column"); //$NON-NLS-1$
        String delimitedColumnName = attributes
                .getProperty("delimitedColumnName"); //$NON-NLS-1$

        IgnoredColumn ic = new IgnoredColumn(column);

        if (stringHasValue(delimitedColumnName)) {
            ic.setColumnNameDelimited(isTrue(delimitedColumnName));
        }

        tc.addIgnoredColumn(ic);
    }

    private void parseIgnoreColumnByRegex(TableConfiguration tc, Node node) {
        Properties attributes = parseAttributes(node);
        String pattern = attributes.getProperty("pattern"); //$NON-NLS-1$

        IgnoredColumnPattern icPattern = new IgnoredColumnPattern(pattern);

        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);

            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            if ("except".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseException(icPattern, childNode);
            }
        }

        tc.addIgnoredColumnPattern(icPattern);
    }

    private void parseException(IgnoredColumnPattern icPattern, Node node) {
        Properties attributes = parseAttributes(node);
        String column = attributes.getProperty("column"); //$NON-NLS-1$
        String delimitedColumnName = attributes
                .getProperty("delimitedColumnName"); //$NON-NLS-1$

        IgnoredColumnException exception = new IgnoredColumnException(column);

        if (stringHasValue(delimitedColumnName)) {
            exception.setColumnNameDelimited(isTrue(delimitedColumnName));
        }

        icPattern.addException(exception);
    }

    private void parseDomainObjectRenamingRule(TableConfiguration tc, Node node) {
        Properties attributes = parseAttributes(node);
        String searchString = attributes.getProperty("searchString"); //$NON-NLS-1$
        String replaceString = attributes.getProperty("replaceString"); //$NON-NLS-1$

        DomainObjectRenamingRule dorr = new DomainObjectRenamingRule();

        dorr.setSearchString(searchString);

        if (stringHasValue(replaceString)) {
            dorr.setReplaceString(replaceString);
        }

        tc.setDomainObjectRenamingRule(dorr);
    }

    private void parseColumnRenamingRule(TableConfiguration tc, Node node) {
        Properties attributes = parseAttributes(node);
        String searchString = attributes.getProperty("searchString"); //$NON-NLS-1$
        String replaceString = attributes.getProperty("replaceString"); //$NON-NLS-1$

        ColumnRenamingRule crr = new ColumnRenamingRule();

        crr.setSearchString(searchString);

        if (stringHasValue(replaceString)) {
            crr.setReplaceString(replaceString);
        }

        tc.setColumnRenamingRule(crr);
    }

    private void parseSelectByTable(TableConfiguration tc, Node node) {
        Properties attributes = parseAttributes(node);
        String table = attributes.getProperty("table"); //$NON-NLS-1$
        String thisColumn = attributes.getProperty("thisColumn"); //$NON-NLS-1$
        String otherColumn = attributes.getProperty("otherColumn"); //$NON-NLS-1$
        String methodSuffix = attributes.getProperty("methodSuffix"); //$NON-NLS-1$
        String returnType = attributes.getProperty("returnType"); //$NON-NLS-1$
        SelectByTableGeneratorConfiguration selectByTableGeneratorConfiguration = new SelectByTableGeneratorConfiguration();
        selectByTableGeneratorConfiguration.setTableName(table);
        selectByTableGeneratorConfiguration.setPrimaryKeyColumn(thisColumn);
        selectByTableGeneratorConfiguration.setOtherPrimaryKeyColumn(otherColumn);
        selectByTableGeneratorConfiguration.setMethodSuffix(methodSuffix);

        String orderByClause = attributes.getProperty("orderByClause"); //$NON-NLS-1$
        if (stringHasValue(orderByClause)) {
            selectByTableGeneratorConfiguration.setOrderByClause(orderByClause);
        }
        String additionClause = attributes.getProperty("additionClause"); //$NON-NLS-1$
        if (stringHasValue(additionClause)) {
            selectByTableGeneratorConfiguration.setAdditionCondition(additionClause);
        }
        if (stringHasValue(returnType)) {
            selectByTableGeneratorConfiguration.setReturnTypeParam(returnType);
        }
        String enableSplit = attributes.getProperty("enableSplit");
        if (stringHasValue(enableSplit)) {
            selectByTableGeneratorConfiguration.setEnableSplit(Boolean.parseBoolean(enableSplit));
        }
        String enableUnion = attributes.getProperty("enableUnion");
        if (stringHasValue(enableUnion)) {
            selectByTableGeneratorConfiguration.setEnableUnion(Boolean.parseBoolean(enableUnion));
        }

        String parameterType = attributes.getProperty("parameterType");
        if (stringHasValue(parameterType)) {
            selectByTableGeneratorConfiguration.setParameterType(parameterType);
        }

        tc.addSelectByTableGeneratorConfiguration(selectByTableGeneratorConfiguration);
    }

    private void parseSelectByColumn(TableConfiguration tc, Node node) {
        Properties attributes = parseAttributes(node);
        String column = attributes.getProperty("column"); //$NON-NLS-1$
        String orderByClause = attributes.getProperty("orderByClause"); //$NON-NLS-1$
        String returnType = attributes.getProperty("returnType"); //$NON-NLS-1$
        SelectByColumnGeneratorConfiguration selectByColumnGeneratorConfiguration = new SelectByColumnGeneratorConfiguration();
        selectByColumnGeneratorConfiguration.setColumnNames(spiltToList(column));
        selectByColumnGeneratorConfiguration.setOrderByClause(orderByClause);
        if (stringHasValue(returnType)) {
            selectByColumnGeneratorConfiguration.setReturnTypeParam(returnType);
        }
        String parameterType = attributes.getProperty("parameterType");
        if (stringHasValue(parameterType)) {
            selectByColumnGeneratorConfiguration.setParameterType(parameterType);
        }
        String enableDelete = attributes.getProperty("enableDelete", "false");
        selectByColumnGeneratorConfiguration.setEnableDelete(Boolean.parseBoolean(enableDelete));
        tc.addSelectByColumnGeneratorConfiguration(selectByColumnGeneratorConfiguration);
    }

    private void parseSelectBySqlMethod(TableConfiguration tc, Node node) {
        Properties attributes = parseAttributes(node);
        SelectBySqlMethodGeneratorConfiguration configuration = new SelectBySqlMethodGeneratorConfiguration();
        String sqlMethod = attributes.getProperty("sqlMethod");
        configuration.setSqlMethod(sqlMethod);
        String parentIdColumn = attributes.getProperty("parentIdColumn");
        if (stringHasValue(parentIdColumn)) {
            configuration.setParentIdColumnName(parentIdColumn);
        } else {
            configuration.setParentIdColumnName("parent_id");
        }
        String idColumnName = attributes.getProperty("primaryKeyColumn");
        if (stringHasValue(idColumnName)) {
            configuration.setPrimaryKeyColumnName(idColumnName);
        } else {
            configuration.setPrimaryKeyColumnName(PropertyRegistry.DEFAULT_PRIMARY_KEY);
        }
        tc.addSelectBySqlMethodGeneratorConfiguration(configuration);
    }

    private void parseJavaModelRelation(TableConfiguration tc, Node node, RelationTypeEnum relationTypeEnum) {
        RelationGeneratorConfiguration relationGeneratorConfiguration = new RelationGeneratorConfiguration();
        Properties attributes = parseAttributes(node);
        String fieldName = attributes.getProperty("fieldName");
        relationGeneratorConfiguration.setPropertyName(fieldName);
        String modelType = attributes.getProperty("modelType");
        relationGeneratorConfiguration.setModelTye(modelType);

        String voModelType = attributes.getProperty("voModelType");
        relationGeneratorConfiguration.setVoModelTye(voModelType);

        String whereColumn = attributes.getProperty("whereColumn");
        if (stringHasValue(whereColumn)) {
            relationGeneratorConfiguration.setColumn(whereColumn);
        }
        String mapperMethod = attributes.getProperty("mapperMethod");
        if (stringHasValue(mapperMethod)) {
            relationGeneratorConfiguration.setSelect(mapperMethod);
        }
        relationGeneratorConfiguration.setType(relationTypeEnum);
        String enableInsert = attributes.getProperty("enableInsert");
        if (stringHasValue(enableInsert)) {
            relationGeneratorConfiguration.setEnableInsert(Boolean.parseBoolean(enableInsert));
        }
        String enableUpdate = attributes.getProperty("enableUpdate");
        if (stringHasValue(enableUpdate)) {
            relationGeneratorConfiguration.setEnableUpdate(Boolean.parseBoolean(enableUpdate));
        }
        String enableDelete = attributes.getProperty("enableDelete");
        if (stringHasValue(enableDelete)) {
            relationGeneratorConfiguration.setEnableDelete(Boolean.parseBoolean(enableDelete));
        }
        String enableInsertOrUpdate = attributes.getProperty("enableInsertOrUpdate");
        if (stringHasValue(enableInsertOrUpdate)) {
            relationGeneratorConfiguration.setEnableInsertOrUpdate(Boolean.parseBoolean(enableInsertOrUpdate));
        }

        String beanClass = attributes.getProperty("beanClassFullName");
        if (stringHasValue(beanClass)) {
            relationGeneratorConfiguration.setBeanClassFullName(beanClass);
        }
        String relationProperty = attributes.getProperty("relationProperty");
        if (stringHasValue(relationProperty)) {
            relationGeneratorConfiguration.setRelationProperty(relationProperty);
        }
        String relationPropertyIsBoolean = attributes.getProperty("relationPropertyIsBoolean");
        if (stringHasValue(relationPropertyIsBoolean)) {
            relationGeneratorConfiguration.setRelationPropertyIsBoolean(Boolean.parseBoolean(relationPropertyIsBoolean));
        }
        String remark = attributes.getProperty(PropertyRegistry.ELEMENT_FIELD_REMARK);
        if (stringHasValue(remark)) {
            relationGeneratorConfiguration.setRemark(remark);
        }
        tc.addRelationGeneratorConfiguration(relationGeneratorConfiguration);
    }

    private void parseGenerateHtml(Context context, TableConfiguration tc, Node node) {
        Properties attributes = parseAttributes(node);
        HtmlGeneratorConfiguration htmlGeneratorConfiguration = new HtmlGeneratorConfiguration(context, tc);
        htmlGeneratorConfiguration.setGenerate(Boolean.parseBoolean(attributes.getProperty(PropertyRegistry.ANY_GENERATE)));
        String targetProject = Optional.ofNullable(attributes.getProperty(PropertyRegistry.ANY_TARGET_PROJECT)).orElse(
                Optional.ofNullable(context.getProperty(PropertyRegistry.CONTEXT_HTML_TARGET_PROJECT))
                        .orElse("src/main/resources/templates"));
        htmlGeneratorConfiguration.setTargetProject(targetProject);
        // 生成的viewPath路径及模板文件名
        String viewPath = attributes.getProperty(PropertyRegistry.TABLE_VIEW_PATH);
        String fullViewPath;
        String htmlTargetPackage = Optional.ofNullable(attributes.getProperty(PropertyRegistry.ANY_TARGET_PACKAGE))
                .orElse(Optional.ofNullable(context.getProperty(PropertyRegistry.CONTEXT_HTML_TARGET_PACKAGE))
                        .orElse(Optional.ofNullable(context.getModuleKeyword()).orElse("html")));
        if (stringHasValue(viewPath)) {
            fullViewPath = htmlTargetPackage + "/" + viewPath;
            htmlGeneratorConfiguration.setViewPath(fullViewPath);
            htmlGeneratorConfiguration.setTargetPackage(StringUtility.substringBeforeLast(fullViewPath, "/"));
            htmlGeneratorConfiguration.setHtmlFileName(
                    String.join(".", StringUtility.substringAfterLast(fullViewPath, "/"), PropertyRegistry.TABLE_HTML_FIE_SUFFIX));
        } else {
            htmlGeneratorConfiguration.setTargetPackage(htmlTargetPackage);
        }
        String overWriteFile = attributes.getProperty(PropertyRegistry.TABLE_OVERRIDE_FILE);
        if (stringHasValue(overWriteFile)) {
            htmlGeneratorConfiguration.setOverWriteFile(Boolean.parseBoolean(overWriteFile));
        }
        //先计算全局隐藏字段
        String contextHtmlHiddenColumns = context.getProperty(PropertyRegistry.ANY_HTML_HIDDEN_COLUMNS);
        if (stringHasValue(contextHtmlHiddenColumns)) {
            htmlGeneratorConfiguration.getHiddenColumns().addAll(spiltToList(contextHtmlHiddenColumns));
        }
        //计算属性及子元素
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            switch (childNode.getNodeName()) {
                case "property":
                    parseHtmlMapGeneratorProperty(htmlGeneratorConfiguration, childNode, context);
                    parseProperty(htmlGeneratorConfiguration, childNode);
                    break;
                case PropertyRegistry.ELEMENT_HTML_ELEMENT_DESCRIPTOR:
                    parseHtmlElementDescriptor(htmlGeneratorConfiguration, childNode);
                    break;
                case PropertyRegistry.ELEMENT_HTML_LAYOUT:
                    parseHtmlLayout(htmlGeneratorConfiguration, childNode);
                    break;
            }
        }
        if (htmlGeneratorConfiguration.getLayoutDescriptor() == null) {
            htmlGeneratorConfiguration.setLayoutDescriptor(new HtmlLayoutDescriptor());
        }
        tc.addHtmlMapGeneratorConfigurations(htmlGeneratorConfiguration);
    }

    private void parseHtmlLayout(HtmlGeneratorConfiguration htmlGeneratorConfiguration, Node childNode) {
        Properties attributes = parseAttributes(childNode);
        HtmlLayoutDescriptor htmlLayoutDescriptor = new HtmlLayoutDescriptor();
        String loadingFrameType = attributes.getProperty("loadingFrameType");
        if (stringHasValue(loadingFrameType)) {
            htmlLayoutDescriptor.setLoadingFrameType(loadingFrameType);
        }
        String barPosition = attributes.getProperty("barPosition");
        if (stringHasValue(barPosition)) {
            htmlLayoutDescriptor.setBarPosition(barPosition);
        }
        String uiFrameType = attributes.getProperty("uiFrameType");
        if (stringHasValue(uiFrameType)) {
            htmlLayoutDescriptor.setUiFrameType(uiFrameType);
        }
        String pageColumnsNum = attributes.getProperty("pageColumnsNum");
        if (stringHasValue(pageColumnsNum)) {
            htmlLayoutDescriptor.setPageColumnsNum(Integer.parseInt(pageColumnsNum));
        }
        String exclusiveColumns = attributes.getProperty("exclusiveColumns");
        if (stringHasValue(exclusiveColumns)) {
            htmlLayoutDescriptor.setExclusiveColumns(spiltToList(exclusiveColumns));
        }
        String borderWidth = attributes.getProperty("borderWidth");
        if (stringHasValue(borderWidth)) {
            htmlLayoutDescriptor.setBorderWidth(Integer.parseInt(borderWidth));
        }
        String borderColor = attributes.getProperty("borderColor");
        if (stringHasValue(borderColor)) {
            htmlLayoutDescriptor.setBorderColor(borderColor);
        }
        htmlGeneratorConfiguration.setLayoutDescriptor(htmlLayoutDescriptor);
    }

    protected void parseHtmlMapGeneratorProperty(HtmlGeneratorConfiguration htmlGeneratorConfiguration, Node node, Context context) {
        Properties properties = parseAttributes(node);
        String propertyName = properties.get("name").toString();
        switch (propertyName) {
            case PropertyRegistry.ANY_HTML_HIDDEN_COLUMNS:
                String htmlHiddenColumns = properties.get("value").toString();
                if (stringHasValue(htmlHiddenColumns)) {
                    htmlGeneratorConfiguration.getHiddenColumns().addAll(spiltToList(htmlHiddenColumns));
                }
                break;
            case PropertyRegistry.TABLE_HTML_REQUIRED_COLUMNS:
                String required = properties.get("value").toString();
                if (stringHasValue(required)) {
                    htmlGeneratorConfiguration.getElementRequired().addAll(spiltToList(required));
                }
                break;
        }
    }

    protected void parseHtmlElementDescriptor(HtmlGeneratorConfiguration htmlGeneratorConfiguration, Node node) {
        Properties attributes = parseAttributes(node);
        String column = attributes.getProperty("column");
        String tagType = attributes.getProperty("tagType");
        HtmlElementDescriptor htmlElementDescriptor = new HtmlElementDescriptor();
        htmlElementDescriptor.setName(column);
        htmlElementDescriptor.setTagType(tagType);
        String dataSource = attributes.getProperty("dataSource");
        if (dataSource != null) {
            htmlElementDescriptor.setDataSource(dataSource);
        }
        String dataUrl = attributes.getProperty("dataUrl");
        if (dataUrl != null) {
            htmlElementDescriptor.setDataUrl(dataUrl);
        }
        String dataFormat = attributes.getProperty("dataFormat");
        if (dataFormat != null) {
            htmlElementDescriptor.setDataFormat(dataFormat);
            switch (dataFormat) {
                case "department":
                    htmlElementDescriptor.setDataSource("department");
                    break;
                case "user":
                    htmlElementDescriptor.setDataSource("user");
                    break;
            }
        }
        String otherFieldName = attributes.getProperty("otherFieldName");
        htmlElementDescriptor.setOtherFieldName(otherFieldName);

        String beanName = attributes.getProperty("beanName");
        htmlElementDescriptor.setBeanName(beanName);

        String applyProperty = attributes.getProperty("applyProperty");
        htmlElementDescriptor.setApplyProperty(applyProperty);

        htmlGeneratorConfiguration.addElementDescriptors(htmlElementDescriptor);
    }

    protected void parseJavaTypeResolver(Context context, Node node) {
        JavaTypeResolverConfiguration javaTypeResolverConfiguration = new JavaTypeResolverConfiguration();

        context.setJavaTypeResolverConfiguration(javaTypeResolverConfiguration);

        Properties attributes = parseAttributes(node);
        String type = attributes.getProperty("type"); //$NON-NLS-1$

        if (stringHasValue(type)) {
            javaTypeResolverConfiguration.setConfigurationType(type);
        }
        parseChildNodeOnlyProperty(javaTypeResolverConfiguration, node);
    }

    private void parsePlugin(Context context, Node node) {
        PluginConfiguration pluginConfiguration = new PluginConfiguration();
        context.addPluginConfiguration(pluginConfiguration);
        Properties attributes = parseAttributes(node);
        String type = attributes.getProperty("type"); //$NON-NLS-1$
        pluginConfiguration.setConfigurationType(type);
        parseChildNodeOnlyProperty(pluginConfiguration, node);
    }

    protected void parseJavaModelGenerator(Context context, Node node) {
        Properties attributes = parseAttributes(node);
        JavaModelGeneratorConfiguration javaModelGeneratorConfiguration = new JavaModelGeneratorConfiguration();
        javaModelGeneratorConfiguration.setTargetPackage(attributes.getProperty(PropertyRegistry.ANY_TARGET_PROJECT));
        String targetPackage = attributes.getProperty(PropertyRegistry.ANY_TARGET_PACKAGE);
        javaModelGeneratorConfiguration.setTargetPackage(targetPackage);
        String baseTargetPackage = StringUtility.substringBeforeLast(targetPackage, ".");
        javaModelGeneratorConfiguration.setBaseTargetPackage(baseTargetPackage);
        javaModelGeneratorConfiguration.setTargetPackageGen(String.join(baseTargetPackage, "codegen.entity"));
        context.setJavaModelGeneratorConfiguration(javaModelGeneratorConfiguration);
        parseAbstractConfigAttributes(attributes, javaModelGeneratorConfiguration, node);
    }

    private void parseJavaClientGenerator(Context context, Node node) {
        JavaClientGeneratorConfiguration javaClientGeneratorConfiguration = new JavaClientGeneratorConfiguration();

        context.setJavaClientGeneratorConfiguration(javaClientGeneratorConfiguration);

        Properties attributes = parseAttributes(node);
        String type = attributes.getProperty("type");
        javaClientGeneratorConfiguration.setConfigurationType(type);
        parseAbstractConfigAttributes(attributes, javaClientGeneratorConfiguration, node);
    }

    /**
     * -----------------------------------
     * 解析table
     */
    protected void parseGenerateService(Context context, TableConfiguration tc, Node node) {
        Properties attributes = parseAttributes(node);

        JavaServiceGeneratorConfiguration javaServiceGeneratorConfiguration = new JavaServiceGeneratorConfiguration(context);
        javaServiceGeneratorConfiguration.setSubTargetPackage("service");
        tc.setJavaServiceGeneratorConfiguration(javaServiceGeneratorConfiguration);

        JavaServiceImplGeneratorConfiguration javaServiceImplGeneratorConfiguration = new JavaServiceImplGeneratorConfiguration(context);
        javaServiceImplGeneratorConfiguration.setSubTargetPackage("service.impl");

        String generateUnitTest = attributes.getProperty("generateUnitTest");
        if (stringHasValue(generateUnitTest)) {
            javaServiceImplGeneratorConfiguration.setGenerateUnitTest(Boolean.parseBoolean(generateUnitTest));
        } else {
            javaServiceImplGeneratorConfiguration.setGenerateUnitTest(true);
        }

        String noServiceAnnotation = attributes.getProperty(PropertyRegistry.SERVICE_NO_SERVICE_ANNOTATION);
        if (stringHasValue(noServiceAnnotation)) {
            javaServiceImplGeneratorConfiguration.setNoServiceAnnotation(Boolean.parseBoolean(noServiceAnnotation));
        }
        tc.setJavaServiceImplGeneratorConfiguration(javaServiceImplGeneratorConfiguration);

        if (context.getJavaModelGeneratorConfiguration().getTargetPackage() != null) {
            String baseTargetPackage = substringBeforeLast(context.getJavaModelGeneratorConfiguration().getTargetPackage(), ".");
            javaServiceGeneratorConfiguration.setBaseTargetPackage(baseTargetPackage);
            javaServiceImplGeneratorConfiguration.setBaseTargetPackage(baseTargetPackage);
        }
        if (context.getJavaModelGeneratorConfiguration().getTargetProject() != null) {
            String targetProject = context.getJavaModelGeneratorConfiguration().getTargetProject();
            javaServiceGeneratorConfiguration.setTargetProject(StringUtility.getTargetProject(targetProject));
            javaServiceImplGeneratorConfiguration.setTargetProject(StringUtility.getTargetProject(targetProject));
        }
        parseAbstractConfigAttributes(attributes, javaServiceGeneratorConfiguration, node);
        parseAbstractConfigAttributes(attributes, javaServiceImplGeneratorConfiguration, node);
    }

    protected void parseGenerateController(Context context, TableConfiguration tc, Node node) {
        Properties attributes = parseAttributes(node);

        JavaControllerGeneratorConfiguration javaControllerGeneratorConfiguration = new JavaControllerGeneratorConfiguration(context);

        String property = context.getProperty(PropertyRegistry.CONTEXT_SPRING_BOOT_APPLICATION_CLASS);
        if (stringHasValue(property)) {
            javaControllerGeneratorConfiguration.setSpringBootApplicationClass(property);
        }

        String generateUnitTest = attributes.getProperty("generateUnitTest");
        if (stringHasValue(generateUnitTest)) {
            javaControllerGeneratorConfiguration.setGenerateUnitTest(Boolean.parseBoolean(generateUnitTest));
        } else {
            javaControllerGeneratorConfiguration.setGenerateUnitTest(true);
        }

        String noSwaggerAnnotation = attributes.getProperty(PropertyRegistry.ANY_NO_SWAGGER_ANNOTATION);
        if (stringHasValue(noSwaggerAnnotation)) {
            javaControllerGeneratorConfiguration.setNoSwaggerAnnotation(Boolean.parseBoolean(noSwaggerAnnotation));
        }
        parseAbstractConfigAttributes(attributes, javaControllerGeneratorConfiguration, node);
        if (!stringHasValue(javaControllerGeneratorConfiguration.getSubTargetPackage())) {
            javaControllerGeneratorConfiguration.setSubTargetPackage("controller");
        }
        if (!stringHasValue(javaControllerGeneratorConfiguration.getTargetPackage())
                && context.getJavaModelGeneratorConfiguration().getTargetPackage() != null) {
            String baseTargetPackage = substringBeforeLast(context.getJavaModelGeneratorConfiguration().getTargetPackage(), ".");
            javaControllerGeneratorConfiguration.setBaseTargetPackage(baseTargetPackage);
            javaControllerGeneratorConfiguration.setTargetPackage(javaControllerGeneratorConfiguration.getBaseTargetPackage()
                    + "."
                    + javaControllerGeneratorConfiguration.getSubTargetPackage());
        }
        if (!stringHasValue(javaControllerGeneratorConfiguration.getTargetProject())
                && context.getJavaModelGeneratorConfiguration().getTargetProject() != null) {
            javaControllerGeneratorConfiguration.setTargetProject(context.getJavaModelGeneratorConfiguration().getTargetProject());
        }

        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);

            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            if ("property".equals(childNode.getNodeName())) {
                parseProperty(javaControllerGeneratorConfiguration, childNode);
            } else if ("generateOptions".equals(childNode.getNodeName())) {
                parseGenerateOptions(javaControllerGeneratorConfiguration, childNode);
            }
        }

        tc.setJavaControllerGeneratorConfiguration(javaControllerGeneratorConfiguration);
    }

    private void parseGenerateOptions(JavaControllerGeneratorConfiguration javaControllerGeneratorConfiguration, Node node) {
        Properties attributes = parseAttributes(node);
        String nameColumn = attributes.getProperty("nameColumn");
        FormOptionGeneratorConfiguration formOptionGeneratorConfiguration = new FormOptionGeneratorConfiguration(nameColumn);
        String type = attributes.getProperty("type");
        formOptionGeneratorConfiguration.setDataType("tree".equalsIgnoreCase(type) ? 1 : 0);
        String idColumn = attributes.getProperty("idColumn");
        if (stringHasValue(idColumn)) {
            formOptionGeneratorConfiguration.setIdColumn(idColumn);
        }
        javaControllerGeneratorConfiguration.addFormOptionGeneratorConfigurations(formOptionGeneratorConfiguration);
    }

    private void parseGenerateDao(Context context, TableConfiguration tc, Node node) {
        Properties attributes = parseAttributes(node);
        JavaClientGeneratorConfiguration javaClientGeneratorConfiguration = new JavaClientGeneratorConfiguration(context);
        if (context.getJavaClientGeneratorConfiguration().getTargetPackage() != null) {
            javaClientGeneratorConfiguration.setBaseTargetPackage(context.getJavaClientGeneratorConfiguration().getTargetPackage());
            javaClientGeneratorConfiguration.setTargetProject(context.getJavaClientGeneratorConfiguration().getTargetProject());
        } else if (context.getJavaModelGeneratorConfiguration().getTargetPackage() != null) {
            javaClientGeneratorConfiguration.setBaseTargetPackage(substringBeforeLast(context.getJavaModelGeneratorConfiguration().getTargetPackage(), "."));
            javaClientGeneratorConfiguration.setTargetProject(context.getJavaModelGeneratorConfiguration().getTargetProject());
        }
        tc.setJavaClientGeneratorConfiguration(javaClientGeneratorConfiguration);

        parseAbstractConfigAttributes(attributes, javaClientGeneratorConfiguration, node);
    }

    private void parseGenerateSqlMap(Context context, TableConfiguration tc, Node node) {
        Properties attributes = parseAttributes(node);
        SqlMapGeneratorConfiguration sqlMapGeneratorConfiguration = new SqlMapGeneratorConfiguration();
        sqlMapGeneratorConfiguration.setTargetPackage(context.getSqlMapGeneratorConfiguration().getTargetPackage());
        sqlMapGeneratorConfiguration.setTargetProject(context.getSqlMapGeneratorConfiguration().getTargetProject());
        tc.setSqlMapGeneratorConfiguration(sqlMapGeneratorConfiguration);
        parseAbstractConfigAttributes(attributes, sqlMapGeneratorConfiguration, node);
    }

    private void parseGenerateSqlSchema(Context context, TableConfiguration tc, Node node) {
        Properties attributes = parseAttributes(node);
        SqlSchemaGeneratorConfiguration sqlSchemaGeneratorConfiguration = new SqlSchemaGeneratorConfiguration(context, tc);
        String generate = attributes.getProperty(PropertyRegistry.ANY_GENERATE);
        sqlSchemaGeneratorConfiguration.setGenerate(Boolean.parseBoolean(generate));
        parseChildNodeOnlyProperty(sqlSchemaGeneratorConfiguration, node);
        tc.setSqlSchemaGeneratorConfiguration(sqlSchemaGeneratorConfiguration);
    }

    private void parseGenerateVO(Context context, TableConfiguration tc, Node node) {
        VOGeneratorConfiguration configuration = new VOGeneratorConfiguration(context, tc);
        Properties attributes = parseAttributes(node);
        String generate = attributes.getProperty(PropertyRegistry.ANY_GENERATE);
        configuration.setGenerate(Boolean.parseBoolean(generate));
        String columnsName = attributes.getProperty(PropertyRegistry.ELEMENT_EXCLUDE_COLUMNS);
        if (stringHasValue(columnsName)) {
            configuration.setExcludeColumns(spiltToList(columnsName));
        }
        String ehAttr = attributes.getProperty(PropertyRegistry.ANY_EQUALS_AND_HASH_CODE);
        if (stringHasValue(ehAttr)) {
            configuration.setEqualsAndHashCodeColumns(spiltToList(ehAttr));
        }

        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);
            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            switch (childNode.getNodeName()) {
                case "property":
                    parseProperty(configuration, childNode);
                    break;
                case ("mapstructMapping"):
                    parseMapstructMapping(childNode, configuration);
                    break;
                case ("overridePropertyValue"):
                    parseVoOverrideColumn(context, tc, childNode, configuration);
                    break;
                case ("additionalProperty"):
                    parseAdditionalProperty(context, tc, childNode, configuration);
                    break;
                case "modelVO":
                    parseGenerateModelVO(context, tc, childNode, configuration);
                    break;
                case "createVO":
                    parseGenerateCreateVO(context, tc, childNode, configuration);
                    break;
                case "updateVO":
                    parseGenerateUpdateVO(context, tc, childNode, configuration);
                    break;
                case "viewVO":
                    parseGenerateViewVO(context, tc, childNode, configuration);
                    break;
                case "excelVO":
                    parseGenerateExcelVO(context, tc, childNode, configuration);
                    break;
                case "requestVO":
                    parseGenerateRequestVO(context, tc, childNode, configuration);
                    break;
            }
        }
        tc.setVoGeneratorConfiguration(configuration);
    }

    private void parseMapstructMapping(Node node, VOGeneratorConfiguration configuration) {
        Properties attributes = parseAttributes(node);
        String source = attributes.getProperty("sourceType");
        String target = attributes.getProperty("targetType");
        MapstructMappingConfiguration mapstructMappingConfiguration = new MapstructMappingConfiguration(source, target);
        String type = attributes.getProperty("type");
        if (stringHasValue(type)) {
            mapstructMappingConfiguration.setType(type);
        }
        String sourceArguments = attributes.getProperty("sourceArguments");
        if (stringHasValue(sourceArguments)) {
            mapstructMappingConfiguration.setSourceArguments(spiltToList(sourceArguments));
        }
        String targetArguments = attributes.getProperty("targetArguments");
        if (stringHasValue(targetArguments)) {
            mapstructMappingConfiguration.setTargetArguments(spiltToList(targetArguments));
        }
        configuration.addMappingConfigurations(mapstructMappingConfiguration);
    }

    private void parseVoOverrideColumn(Context context, TableConfiguration tc, Node node, AbstractModelGeneratorConfiguration configuration) {
        OverridePropertyValueGeneratorConfiguration overrideColumnGeneratorConfiguration = new OverridePropertyValueGeneratorConfiguration(context, tc);
        Properties attributes = parseAttributes(node);
        String sourceColumn = attributes.getProperty(PropertyRegistry.ELEMENT_SOURCE_COLUMN);
        if (stringHasValue(sourceColumn)) {
            overrideColumnGeneratorConfiguration.setSourceColumnName(sourceColumn);
        }
        String targetColumn = attributes.getProperty(PropertyRegistry.ELEMENT_TARGET_COLUMN);
        if (stringHasValue(targetColumn)) {
            overrideColumnGeneratorConfiguration.setTargetColumnName(targetColumn);
        }
        String targetProperty = attributes.getProperty(PropertyRegistry.ELEMENT_TARGET_PROPERTY);
        if (stringHasValue(targetProperty)) {
            overrideColumnGeneratorConfiguration.setTargetPropertyName(targetProperty);
        }
        String targetPropertyType = attributes.getProperty(PropertyRegistry.ELEMENT_TARGET_PROPERTY_TYPE
                , FullyQualifiedJavaType.getStringInstance().getFullyQualifiedName());
        overrideColumnGeneratorConfiguration.setTargetPropertyType(targetPropertyType);

        String typeValue = attributes.getProperty(PropertyRegistry.ELEMENT_TYPE_VALUE);
        if (stringHasValue(typeValue)) {
            overrideColumnGeneratorConfiguration.setTypeValue(typeValue);
        }

        String annotationType = attributes.getProperty(PropertyRegistry.ELEMENT_ANNOTATION_TYPE);
        if (stringHasValue(annotationType)) {
            overrideColumnGeneratorConfiguration.setAnnotationType(annotationType);
        }

        String beanName = attributes.getProperty(PropertyRegistry.ELEMENT_ANNOTATION_BEAN_NAME);
        if (stringHasValue(beanName)) {
            overrideColumnGeneratorConfiguration.setBeanName(beanName);
        }

        String applyProperty = attributes.getProperty(PropertyRegistry.ELEMENT_APPLY_PROPERTY_VALUE);
        if (stringHasValue(applyProperty)) {
            overrideColumnGeneratorConfiguration.setApplyProperty(applyProperty);
        }

        String remark = attributes.getProperty(PropertyRegistry.ELEMENT_FIELD_REMARK);
        if (stringHasValue(remark)) {
            overrideColumnGeneratorConfiguration.setRemark(remark);
        }
        if (stringHasValue(sourceColumn)) {
            if (annotationType.equals("Dict")) {
                if (stringHasValue(beanName)) {
                    configuration.addOverrideColumnConfigurations(overrideColumnGeneratorConfiguration);
                }
            } else {
                configuration.addOverrideColumnConfigurations(overrideColumnGeneratorConfiguration);
            }
        }
    }

    private void parseAdditionalProperty(Context context, TableConfiguration tc, Node childNode, AbstractModelGeneratorConfiguration configuration) {
        VoAdditionalPropertyGeneratorConfiguration voAdditionalPropertyConfiguration = new VoAdditionalPropertyGeneratorConfiguration(context, tc);
        Properties attributes = parseAttributes(childNode);
        String name = attributes.getProperty("name");
        String type = attributes.getProperty("type");
        if (!stringHasValue(type)) {
            type = FullyQualifiedJavaType.getStringInstance().getFullyQualifiedName();
        }
        if (stringHasValue(name)) {
            voAdditionalPropertyConfiguration.setName(name);
            voAdditionalPropertyConfiguration.setType(type);
            String typeArguments = attributes.getProperty("typeArguments");
            if (stringHasValue(typeArguments)) {
                voAdditionalPropertyConfiguration.setTypeArguments(spiltToList(typeArguments));
            }
            String annotations = attributes.getProperty("annotations");
            if (stringHasValue(annotations)) {
                voAdditionalPropertyConfiguration.setAnnotations(new ArrayList<>(Arrays.asList(annotations.split("\\|"))));
            }
            String initializationString = attributes.getProperty("initializationString");
            if (stringHasValue(initializationString)) {
                voAdditionalPropertyConfiguration.setInitializationString(initializationString);
            }

            String isFinal = attributes.getProperty("isFinal");
            if (stringHasValue(isFinal)) {
                voAdditionalPropertyConfiguration.setFinal(Boolean.parseBoolean(isFinal));
            }

            String visibility = attributes.getProperty("visibility");
            if (stringHasValue(visibility)) {
                voAdditionalPropertyConfiguration.setVisibility(visibility);
            }
            String importedTypes = attributes.getProperty("importedTypes");
            if (stringHasValue(importedTypes)) {
                voAdditionalPropertyConfiguration.setImportedTypes(spiltToList(importedTypes));
            }
            String remark = attributes.getProperty(PropertyRegistry.ELEMENT_FIELD_REMARK);
            if (stringHasValue(remark)) {
                voAdditionalPropertyConfiguration.setRemark(remark);
            }
            configuration.addAdditionalPropertyConfigurations(voAdditionalPropertyConfiguration);
        }

    }

    private void parseNameFragment(Context context, TableConfiguration tc, Node childNode, AbstractModelGeneratorConfiguration configuration) {
        VoNameFragmentGeneratorConfiguration voNameFragmentGeneratorConfiguration = new VoNameFragmentGeneratorConfiguration(context, tc);
        Properties attributes = parseAttributes(childNode);
        String column = attributes.getProperty("column");
        if (stringHasValue(column)) {
            voNameFragmentGeneratorConfiguration.setColumn(column);
        }
        String fragment = attributes.getProperty("fragment");
        if (stringHasValue(fragment)) {
            voNameFragmentGeneratorConfiguration.setFragment(fragment);
        }
        configuration.addVoNameFragmentGeneratorConfiguration(voNameFragmentGeneratorConfiguration);
    }

    private void parseModelChildNodeProperty(Context context, TableConfiguration tc, Node node, AbstractModelGeneratorConfiguration configuration) {
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);

            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            switch (childNode.getNodeName()) {
                case "property":  //$NON-NLS-1$
                    parseProperty(configuration, childNode);
                    break;
                case ("overridePropertyValue"):
                    parseVoOverrideColumn(context, tc, childNode, configuration);
                    break;
                case ("additionalProperty"):
                    parseAdditionalProperty(context, tc, childNode, configuration);
                    break;
                case ("nameFragment"):
                    parseNameFragment(context, tc, childNode, configuration);
                    break;
                case ("columnRenderFun"):
                    parseColumnRenderFun(context, tc, configuration, childNode);
                    break;
            }
        }
    }

    private void parseColumnRenderFun(Context context, TableConfiguration tc, AbstractModelGeneratorConfiguration configuration, Node childNode) {
        VoColumnRenderFunGeneratorConfiguration voColumnRenderFunGeneratorConfiguration = new VoColumnRenderFunGeneratorConfiguration(context, tc);
        Properties attributes = parseAttributes(childNode);
        String column = attributes.getProperty("column");
        if (stringHasValue(column)) {
            voColumnRenderFunGeneratorConfiguration.setColumn(column);
        }
        String renderFun = attributes.getProperty("renderFun");
        if (stringHasValue(renderFun)) {
            voColumnRenderFunGeneratorConfiguration.setRenderFun(renderFun);
        }
        configuration.addVoColumnRenderFunGeneratorConfiguration(voColumnRenderFunGeneratorConfiguration);
    }


    private void parseGenerateModelVO(Context context, TableConfiguration tc, Node node, VOGeneratorConfiguration voGeneratorConfiguration) {
        Properties attributes = parseAttributes(node);
        VOModelGeneratorConfiguration vOModelGeneratorConfiguration = new VOModelGeneratorConfiguration(context, tc);
        parseColumnsList(attributes, vOModelGeneratorConfiguration, voGeneratorConfiguration);
        String includeColumns = attributes.getProperty(PropertyRegistry.ELEMENT_INCLUDE_COLUMNS);
        if (stringHasValue(includeColumns)) {
            vOModelGeneratorConfiguration.setIncludeColumns(spiltToList(includeColumns));
        }
        //EqualsAndHashCodeColumns
        String ehAttr = attributes.getProperty(PropertyRegistry.ANY_EQUALS_AND_HASH_CODE);
        if (stringHasValue(ehAttr)) {
            vOModelGeneratorConfiguration.setEqualsAndHashCodeColumns(spiltToList(ehAttr));
        } else {
            vOModelGeneratorConfiguration.getEqualsAndHashCodeColumns().addAll(voGeneratorConfiguration.getEqualsAndHashCodeColumns());
        }
        if (vOModelGeneratorConfiguration.getEqualsAndHashCodeColumns().size() > 0) {
            List<String> distinct = vOModelGeneratorConfiguration.getEqualsAndHashCodeColumns().stream().distinct().collect(Collectors.toList());
            vOModelGeneratorConfiguration.setEqualsAndHashCodeColumns(distinct);
        }

        // 继承model的overridePropertyValue、additionalProperty
        if (tc.getJavaModelGeneratorConfiguration() != null) {
            vOModelGeneratorConfiguration.getOverridePropertyConfigurations().addAll(tc.getJavaModelGeneratorConfiguration().getOverridePropertyConfigurations());
            vOModelGeneratorConfiguration.getAdditionalPropertyConfigurations().addAll(tc.getJavaModelGeneratorConfiguration().getAdditionalPropertyConfigurations());
        }

        // 继承model的javaModelCollection创建的属性
        tc.getRelationGeneratorConfigurations().forEach(relationConfiguration -> {
            VoAdditionalPropertyGeneratorConfiguration additionalPropertyGeneratorConfiguration = new VoAdditionalPropertyGeneratorConfiguration(context, tc);
            additionalPropertyGeneratorConfiguration.setName(relationConfiguration.getPropertyName());
            additionalPropertyGeneratorConfiguration.setRemark(relationConfiguration.getRemark());
            String type = relationConfiguration.getVoModelTye() != null ? relationConfiguration.getVoModelTye() : relationConfiguration.getJavaType();
            if (relationConfiguration.getType().equals(RelationTypeEnum.collection)) {
                additionalPropertyGeneratorConfiguration.setType("java.util.List<" + type + ">");
                additionalPropertyGeneratorConfiguration.getImportedTypes().add("java.util.List");
            } else {
                additionalPropertyGeneratorConfiguration.setType(type);
            }
            additionalPropertyGeneratorConfiguration.getImportedTypes().add(type);
        });
        parseModelChildNodeProperty(context, tc, node, vOModelGeneratorConfiguration);

        voGeneratorConfiguration.setVoModelConfiguration(vOModelGeneratorConfiguration);
    }

    private void parseGenerateCreateVO(Context context, TableConfiguration tc, Node node, VOGeneratorConfiguration voGeneratorConfiguration) {
        Properties attributes = parseAttributes(node);
        VOCreateGeneratorConfiguration configuration = new VOCreateGeneratorConfiguration(context, tc);
        parseColumnsList(attributes, configuration, voGeneratorConfiguration);
        String includeColumns = attributes.getProperty(PropertyRegistry.ELEMENT_INCLUDE_COLUMNS);
        if (stringHasValue(includeColumns)) {
            configuration.setIncludeColumns(spiltToList(includeColumns));
        }
        String requiredColumns = attributes.getProperty(PropertyRegistry.ELEMENT_REQUIRED_COLUMNS);
        if (stringHasValue(requiredColumns)) {
            configuration.setRequiredColumns(spiltToList(requiredColumns));
        }

        List<String> validateIgnoreColumns = spiltToList(attributes.getProperty(PropertyRegistry.ELEMENT_VALIDATE_IGNORE_COLUMNS));
        if (validateIgnoreColumns.size() == 0) {
            validateIgnoreColumns.add(PropertyRegistry.DEFAULT_PRIMARY_KEY);
        }

        String isSelective = attributes.getProperty(PropertyRegistry.ELEMENT_ENABLE_SELECTIVE);
        if (stringHasValue(isSelective)) {
            configuration.setEnableSelective(Boolean.parseBoolean(isSelective));
        }

        //EqualsAndHashCodeColumns
        String ehAttr = attributes.getProperty(PropertyRegistry.ANY_EQUALS_AND_HASH_CODE);
        if (stringHasValue(ehAttr)) {
            configuration.setEqualsAndHashCodeColumns(spiltToList(ehAttr));
        } else {
            configuration.getEqualsAndHashCodeColumns().addAll(voGeneratorConfiguration.getEqualsAndHashCodeColumns());
        }
        if (configuration.getEqualsAndHashCodeColumns().size() > 0) {
            List<String> distinct = configuration.getEqualsAndHashCodeColumns().stream().distinct().collect(Collectors.toList());
            configuration.setEqualsAndHashCodeColumns(distinct);
        }

        validateIgnoreColumns.addAll(tc.getValidateIgnoreColumns());
        configuration.setValidateIgnoreColumns(validateIgnoreColumns);

        parseModelChildNodeProperty(context, tc, node, configuration);
        voGeneratorConfiguration.setVoCreateConfiguration(configuration);
    }

    private void parseGenerateUpdateVO(Context context, TableConfiguration tc, Node node, VOGeneratorConfiguration voGeneratorConfiguration) {
        Properties attributes = parseAttributes(node);
        VOUpdateGeneratorConfiguration configuration = new VOUpdateGeneratorConfiguration(context, tc);
        parseColumnsList(attributes, configuration, voGeneratorConfiguration);
        String includeColumns = attributes.getProperty(PropertyRegistry.ELEMENT_INCLUDE_COLUMNS);
        if (stringHasValue(includeColumns)) {
            configuration.setIncludeColumns(spiltToList(includeColumns));
        }
        String requiredColumns = attributes.getProperty(PropertyRegistry.ELEMENT_REQUIRED_COLUMNS);
        if (stringHasValue(requiredColumns)) {
            configuration.setRequiredColumns(spiltToList(requiredColumns));
        }

        List<String> validateIgnoreColumns = spiltToList(attributes.getProperty(PropertyRegistry.ELEMENT_VALIDATE_IGNORE_COLUMNS));
        validateIgnoreColumns.addAll(tc.getValidateIgnoreColumns());
        configuration.setValidateIgnoreColumns(validateIgnoreColumns);

        String isSelective = attributes.getProperty(PropertyRegistry.ELEMENT_ENABLE_SELECTIVE);
        if (stringHasValue(isSelective)) {
            configuration.setEnableSelective(Boolean.parseBoolean(isSelective));
        }

        //EqualsAndHashCodeColumns
        String ehAttr = attributes.getProperty(PropertyRegistry.ANY_EQUALS_AND_HASH_CODE);
        if (stringHasValue(ehAttr)) {
            configuration.setEqualsAndHashCodeColumns(spiltToList(ehAttr));
        } else {
            configuration.getEqualsAndHashCodeColumns().addAll(voGeneratorConfiguration.getEqualsAndHashCodeColumns());
        }
        if (configuration.getEqualsAndHashCodeColumns().size() > 0) {
            List<String> distinct = configuration.getEqualsAndHashCodeColumns().stream().distinct().collect(Collectors.toList());
            configuration.setEqualsAndHashCodeColumns(distinct);
        }

        parseModelChildNodeProperty(context, tc, node, configuration);
        voGeneratorConfiguration.setVoUpdateConfiguration(configuration);
    }

    private void parseGenerateExcelVO(Context context, TableConfiguration tc, Node node, VOGeneratorConfiguration voGeneratorConfiguration) {
        Properties attributes = parseAttributes(node);
        VOExcelGeneratorConfiguration vOExcelGeneratorConfiguration = new VOExcelGeneratorConfiguration(context, tc);
        parseColumnsList(attributes, vOExcelGeneratorConfiguration, voGeneratorConfiguration);
        String includeColumns = attributes.getProperty(PropertyRegistry.ELEMENT_INCLUDE_COLUMNS);
        if (stringHasValue(includeColumns)) {
            vOExcelGeneratorConfiguration.setIncludeColumns(spiltToList(includeColumns));
        }

        //EqualsAndHashCodeColumns
        String ehAttr = attributes.getProperty(PropertyRegistry.ANY_EQUALS_AND_HASH_CODE);
        if (stringHasValue(ehAttr)) {
            vOExcelGeneratorConfiguration.setEqualsAndHashCodeColumns(spiltToList(ehAttr));
        } else {
            vOExcelGeneratorConfiguration.getEqualsAndHashCodeColumns().addAll(voGeneratorConfiguration.getEqualsAndHashCodeColumns());
        }
        if (vOExcelGeneratorConfiguration.getEqualsAndHashCodeColumns().size() > 0) {
            List<String> distinct = vOExcelGeneratorConfiguration.getEqualsAndHashCodeColumns().stream().distinct().collect(Collectors.toList());
            vOExcelGeneratorConfiguration.setEqualsAndHashCodeColumns(distinct);
        }

        parseModelChildNodeProperty(context, tc, node, vOExcelGeneratorConfiguration);
        voGeneratorConfiguration.setVoExcelConfiguration(vOExcelGeneratorConfiguration);
    }

    private void parseGenerateViewVO(Context context, TableConfiguration tc, Node node, VOGeneratorConfiguration voGeneratorConfiguration) {
        VOViewGeneratorConfiguration voViewGeneratorConfiguration = new VOViewGeneratorConfiguration(context, tc);
        Properties attributes = parseAttributes(node);
        parseColumnsList(attributes, voViewGeneratorConfiguration, voGeneratorConfiguration);
        String includeColumns = attributes.getProperty(PropertyRegistry.ELEMENT_INCLUDE_COLUMNS);
        if (stringHasValue(includeColumns)) {
            voViewGeneratorConfiguration.setIncludeColumns(spiltToList(includeColumns));
        }
        String indexColumn = attributes.getProperty("indexColumn");
        if (stringHasValue(indexColumn)) {
            voViewGeneratorConfiguration.setIndexColumn(indexColumn);
        }
        String actionColumn = attributes.getProperty("actionColumn");
        if (stringHasValue(actionColumn)) {
            voViewGeneratorConfiguration.setActionColumn(spiltToList(actionColumn));
        }
        String queryColumns = attributes.getProperty("queryColumns");
        if (stringHasValue(queryColumns)) {
            voViewGeneratorConfiguration.setQueryColumns(spiltToList(queryColumns));
        }
        String parentMenuId = attributes.getProperty("parentMenuId");
        if (stringHasValue(parentMenuId)) {
            voViewGeneratorConfiguration.setParentMenuId(parentMenuId);
        }
        String defaultDisplayFields = attributes.getProperty("defaultDisplayFields");
        if (stringHasValue(defaultDisplayFields)) {
            voViewGeneratorConfiguration.setDefaultDisplayFields(spiltToList(defaultDisplayFields));
        }

        String defaultHiddenFields = attributes.getProperty("defaultHiddenFields");
        if (stringHasValue(defaultHiddenFields)) {
            voViewGeneratorConfiguration.setDefaultHiddenFields(spiltToList(defaultHiddenFields));
        }

        String viewIcon = attributes.getProperty("viewMenuIcon");
        if (stringHasValue(viewIcon)) {
            voViewGeneratorConfiguration.setViewMenuIcon(viewIcon);
        } else {
            voViewGeneratorConfiguration.setViewMenuIcon(GlobalConstant.VIEW_VO_DEFAULT_ICON);
        }

        //EqualsAndHashCodeColumns
        String equalsHashCode = attributes.getProperty(PropertyRegistry.ANY_EQUALS_AND_HASH_CODE);
        if (stringHasValue(equalsHashCode)) {
            voViewGeneratorConfiguration.setEqualsAndHashCodeColumns(spiltToList(equalsHashCode));
        } else {
            voViewGeneratorConfiguration.getEqualsAndHashCodeColumns().addAll(voGeneratorConfiguration.getEqualsAndHashCodeColumns());
        }
        if (voViewGeneratorConfiguration.getEqualsAndHashCodeColumns().size() > 0) {
            List<String> distinct = voViewGeneratorConfiguration.getEqualsAndHashCodeColumns().stream().distinct().collect(Collectors.toList());
            voViewGeneratorConfiguration.setEqualsAndHashCodeColumns(distinct);
        }

        parseModelChildNodeProperty(context, tc, node, voViewGeneratorConfiguration);
        voGeneratorConfiguration.setVoViewConfiguration(voViewGeneratorConfiguration);
    }


    private void parseGenerateRequestVO(Context context, TableConfiguration tc, Node node, VOGeneratorConfiguration voGeneratorConfiguration) {
        VORequestGeneratorConfiguration voRequestGeneratorConfiguration = new VORequestGeneratorConfiguration(context, tc);
        Properties attributes = parseAttributes(node);
        parseColumnsList(attributes, voRequestGeneratorConfiguration, voGeneratorConfiguration);
        String includePageParam = attributes.getProperty("includePageParam");
        voRequestGeneratorConfiguration.setIncludePageParam(Boolean.parseBoolean(includePageParam));

        //EqualsAndHashCodeColumns
        String ehAttr = attributes.getProperty(PropertyRegistry.ANY_EQUALS_AND_HASH_CODE);
        if (stringHasValue(ehAttr)) {
            voRequestGeneratorConfiguration.setEqualsAndHashCodeColumns(spiltToList(ehAttr));
        } else {
            voRequestGeneratorConfiguration.getEqualsAndHashCodeColumns().addAll(voGeneratorConfiguration.getEqualsAndHashCodeColumns());
        }
        if (voRequestGeneratorConfiguration.getEqualsAndHashCodeColumns().size() > 0) {
            List<String> distinct = voRequestGeneratorConfiguration.getEqualsAndHashCodeColumns().stream().distinct().collect(Collectors.toList());
            voRequestGeneratorConfiguration.setEqualsAndHashCodeColumns(distinct);
        }

        parseModelChildNodeProperty(context, tc, node, voRequestGeneratorConfiguration);
        voGeneratorConfiguration.setVoRequestConfiguration(voRequestGeneratorConfiguration);
    }

    /**
     * 解析CachePO子节点属性
     *
     * @param context 上下文
     * @param tc      表配置
     * @param node    节点
     */
    private void parseGenerateCachePO(Context context, TableConfiguration tc, Node node) {
        VOCacheGeneratorConfiguration voCacheGeneratorConfiguration = new VOCacheGeneratorConfiguration(context, tc);
        Properties attributes = parseAttributes(node);
        String generate = attributes.getProperty(PropertyRegistry.ANY_GENERATE, "false");
        voCacheGeneratorConfiguration.setGenerate(Boolean.parseBoolean(generate));
        String includeColumns = attributes.getProperty(PropertyRegistry.ELEMENT_INCLUDE_COLUMNS);
        if (stringHasValue(includeColumns)) {
            voCacheGeneratorConfiguration.setIncludeColumns(spiltToList(includeColumns));
        }
        String typeColumn = attributes.getProperty(PropertyRegistry.ELEMENT_TYPE_COLUMN);
        if (stringHasValue(typeColumn)) {
            voCacheGeneratorConfiguration.setTypeColumn(typeColumn);
        }
        String codeColumn = attributes.getProperty(PropertyRegistry.ELEMENT_CODE_COLUMN);
        if (stringHasValue(codeColumn)) {
            voCacheGeneratorConfiguration.setCodeColumn(codeColumn);
        }
        String nameColumn = attributes.getProperty(PropertyRegistry.ELEMENT_NAME_COLUMN);
        if (stringHasValue(nameColumn)) {
            voCacheGeneratorConfiguration.setValueColumn(nameColumn);
        }
        tc.setVoCacheGeneratorConfiguration(voCacheGeneratorConfiguration);

    }

    /**
     * 解析PO Model子节点属性
     *
     * @param context 上下文
     * @param tc      表配置
     * @param node    节点
     */
    private void parseGenerateModel(Context context, TableConfiguration tc, Node node) {
        JavaModelGeneratorConfiguration modelConfiguration = new JavaModelGeneratorConfiguration();
        //继承context的配置
        JavaModelGeneratorConfiguration contextConfiguration = context.getJavaModelGeneratorConfiguration();
        modelConfiguration.setTargetPackage(contextConfiguration.getTargetPackage());
        modelConfiguration.setTargetProject(contextConfiguration.getTargetProject());
        modelConfiguration.setBaseTargetPackage(contextConfiguration.getBaseTargetPackage());
        modelConfiguration.setTargetPackageGen(contextConfiguration.getTargetPackageGen());
        //解析子节点属性
        Properties attributes = parseAttributes(node);
        String generate = attributes.getProperty(PropertyRegistry.ANY_GENERATE);
        modelConfiguration.setGenerate(Boolean.parseBoolean(generate));
        String noMetaAnnotation = attributes.getProperty(PropertyRegistry.ANY_NO_META_ANNOTATION);
        if (stringHasValue(noMetaAnnotation)) {
            modelConfiguration.setNoMetaAnnotation(Boolean.parseBoolean(noMetaAnnotation));
        }
        String ehAttr = attributes.getProperty(PropertyRegistry.ANY_EQUALS_AND_HASH_CODE);
        if (stringHasValue(ehAttr)) {
            modelConfiguration.setEqualsAndHashCodeColumns(spiltToList(ehAttr));
        }
        //判断enableChildren
        modelConfiguration.setGenerateChildren(Boolean.parseBoolean(attributes.getProperty("enableChildren")));

        parseModelChildNodeProperty(context, tc, node, modelConfiguration);

        tc.setJavaModelGeneratorConfiguration(modelConfiguration);
        parseAbstractConfigAttributes(attributes, modelConfiguration, node);
    }

    /**
     * 解析VO子节点通用属性
     */
    private void parseColumnsList(Properties attributes, AbstractModelGeneratorConfiguration abstractModelGeneratorConfiguration, VOGeneratorConfiguration voGeneratorConfiguration) {
        if (!voGeneratorConfiguration.isGenerate()) {
            abstractModelGeneratorConfiguration.setGenerate(false);
        } else {
            String generate = attributes.getProperty(PropertyRegistry.ANY_GENERATE);
            abstractModelGeneratorConfiguration.setGenerate(Boolean.parseBoolean(generate));
        }
        String excludeColumns = attributes.getProperty(PropertyRegistry.ELEMENT_EXCLUDE_COLUMNS);
        if (stringHasValue(excludeColumns)) {
            abstractModelGeneratorConfiguration.setExcludeColumns(spiltToList(excludeColumns));
        }
    }

    /**
     * 解析通用属性
     *
     * @param attributes    节点属性
     * @param configuration 配置
     * @param node          节点
     */
    private void parseAbstractConfigAttributes(Properties attributes, AbstractGeneratorConfiguration configuration, Node node) {

        String generate = attributes.getProperty(PropertyRegistry.ANY_GENERATE);
        String targetProject = attributes.getProperty(PropertyRegistry.ANY_TARGET_PROJECT);
        String targetPackage = attributes.getProperty(PropertyRegistry.ANY_TARGET_PACKAGE);
        String targetSubPackage = attributes.getProperty(PropertyRegistry.ANY_TARGET_SUB_PACKAGE);

        if (stringHasValue(generate)) {
            configuration.setGenerate(Boolean.parseBoolean(generate));
        }

        if (stringHasValue(targetProject)) {
            configuration.setTargetProject(StringUtility.getTargetProject(targetProject));
        }
        if (stringHasValue(targetPackage)) {
            configuration.setTargetPackage(targetPackage);
        } else if (stringHasValue(targetSubPackage) && stringHasValue(configuration.getBaseTargetPackage())) {
            configuration.setTargetPackage(String.join(".", configuration.getBaseTargetPackage(), targetSubPackage));
        }
        parseChildNodeOnlyProperty(configuration, node);
    }

    private void parseChildNodeOnlyProperty(PropertyHolder propertyHolder, Node node) {
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);

            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            if ("property".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseProperty(propertyHolder, childNode);
            }
        }
    }

    protected void parseJdbcConnection(Context context, Node node) {
        JDBCConnectionConfiguration jdbcConnectionConfiguration = new JDBCConnectionConfiguration();

        context.setJdbcConnectionConfiguration(jdbcConnectionConfiguration);

        Properties attributes = parseAttributes(node);
        String driverClass = attributes.getProperty("driverClass"); //$NON-NLS-1$
        String connectionURL = attributes.getProperty("connectionURL"); //$NON-NLS-1$

        jdbcConnectionConfiguration.setDriverClass(driverClass);
        jdbcConnectionConfiguration.setConnectionURL(connectionURL);

        String userId = attributes.getProperty("userId"); //$NON-NLS-1$
        if (stringHasValue(userId)) {
            jdbcConnectionConfiguration.setUserId(userId);
        }

        String password = attributes.getProperty("password"); //$NON-NLS-1$
        if (stringHasValue(password)) {
            jdbcConnectionConfiguration.setPassword(password);
        }
        parseChildNodeOnlyProperty(jdbcConnectionConfiguration, node);
    }

    protected void parseClassPathEntry(Configuration configuration, Node node) {
        Properties attributes = parseAttributes(node);

        configuration.addClasspathEntry(attributes.getProperty("location")); //$NON-NLS-1$
    }

    protected void parseProperty(PropertyHolder propertyHolder, Node node) {
        Properties attributes = parseAttributes(node);

        String name = attributes.getProperty("name"); //$NON-NLS-1$
        String value = attributes.getProperty("value"); //$NON-NLS-1$
        propertyHolder.addProperty(name, value);
    }

    protected Properties parseAttributes(Node node) {
        Properties attributes = new Properties();
        NamedNodeMap nnm = node.getAttributes();
        for (int i = 0; i < nnm.getLength(); i++) {
            Node attribute = nnm.item(i);
            String value = parsePropertyTokens(attribute.getNodeValue());
            attributes.put(attribute.getNodeName(), value);
        }

        return attributes;
    }

    String parsePropertyTokens(String s) {
        final String OPEN = "${"; //$NON-NLS-1$
        final String CLOSE = "}"; //$NON-NLS-1$
        int currentIndex = 0;

        List<String> answer = new ArrayList<>();

        int markerStartIndex = s.indexOf(OPEN);
        if (markerStartIndex < 0) {
            // no parameter markers
            answer.add(s);
            currentIndex = s.length();
        }

        while (markerStartIndex > -1) {
            if (markerStartIndex > currentIndex) {
                // add the characters before the next parameter marker
                answer.add(s.substring(currentIndex, markerStartIndex));
                currentIndex = markerStartIndex;
            }

            int markerEndIndex = s.indexOf(CLOSE, currentIndex);
            int nestedStartIndex = s.indexOf(OPEN, markerStartIndex + OPEN.length());
            while (nestedStartIndex > -1 && markerEndIndex > -1 && nestedStartIndex < markerEndIndex) {
                nestedStartIndex = s.indexOf(OPEN, nestedStartIndex + OPEN.length());
                markerEndIndex = s.indexOf(CLOSE, markerEndIndex + CLOSE.length());
            }

            if (markerEndIndex < 0) {
                // no closing delimiter, just move to the end of the string
                answer.add(s.substring(markerStartIndex));
                currentIndex = s.length();
                break;
            }

            // we have a valid property marker...
            String property = s.substring(markerStartIndex + OPEN.length(), markerEndIndex);
            String propertyValue = resolveProperty(parsePropertyTokens(property));
            if (propertyValue == null) {
                // add the property marker back into the stream
                answer.add(s.substring(markerStartIndex, markerEndIndex + 1));
            } else {
                answer.add(propertyValue);
            }

            currentIndex = markerEndIndex + CLOSE.length();
            markerStartIndex = s.indexOf(OPEN, currentIndex);
        }

        if (currentIndex < s.length()) {
            answer.add(s.substring(currentIndex));
        }

        return String.join("", answer);
    }

    protected void parseCommentGenerator(Context context, Node node) {
        CommentGeneratorConfiguration commentGeneratorConfiguration = new CommentGeneratorConfiguration();

        context.setCommentGeneratorConfiguration(commentGeneratorConfiguration);

        Properties attributes = parseAttributes(node);
        String type = attributes.getProperty("type"); //$NON-NLS-1$

        if (stringHasValue(type)) {
            commentGeneratorConfiguration.setConfigurationType(type);
        }
        parseChildNodeOnlyProperty(commentGeneratorConfiguration, node);
    }

    protected void parseConnectionFactory(Context context, Node node) {
        ConnectionFactoryConfiguration connectionFactoryConfiguration = new ConnectionFactoryConfiguration();

        context.setConnectionFactoryConfiguration(connectionFactoryConfiguration);

        Properties attributes = parseAttributes(node);
        String type = attributes.getProperty("type"); //$NON-NLS-1$

        if (stringHasValue(type)) {
            connectionFactoryConfiguration.setConfigurationType(type);
        }
        parseChildNodeOnlyProperty(connectionFactoryConfiguration, node);
    }

    /**
     * This method resolve a property from one of the three sources: system properties,
     * properties loaded from the &lt;properties&gt; configuration element, and
     * "extra" properties that may be supplied by the Maven or Ant environments.
     *
     * <p>If there is a name collision, system properties take precedence, followed by
     * configuration properties, followed by extra properties.
     *
     * @param key property key
     * @return the resolved property.  This method will return null if the property is
     * undefined in any of the sources.
     */
    private String resolveProperty(String key) {
        String property = System.getProperty(key);

        if (property == null) {
            property = configurationProperties.getProperty(key);
        }

        if (property == null) {
            property = extraProperties.getProperty(key);
        }

        return property;
    }
}
