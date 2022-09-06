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

import org.mybatis.generator.config.*;
import org.mybatis.generator.custom.RelationTypeEnum;
import org.mybatis.generator.custom.pojo.*;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

            if ("properties".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseProperties(childNode);
            } else if ("classPathEntry".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseClassPathEntry(configuration, childNode);
            } else if ("context".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseContext(configuration, childNode);
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

        String moduleKeyword = attributes.getProperty(PropertyRegistry.CONTEXT_MODULE_KEYWORD);
        if (stringHasValue(moduleKeyword)) {
            context.setModuleKeyword(moduleKeyword);
        }
        String integrateMybatisPlus = attributes.getProperty(PropertyRegistry.CONTEXT_INTEGRATE_MYBATIS_PLUS);
        context.setIntegrateMybatisPlus(!stringHasValue(integrateMybatisPlus) || Boolean.parseBoolean(integrateMybatisPlus));
        String integrateSpringSecurity = attributes.getProperty(PropertyRegistry.CONTEXT_INTEGRATE_SPRING_SECURITY);
        context.setIntegrateSpringSecurity(!stringHasValue(integrateSpringSecurity) || Boolean.parseBoolean(integrateSpringSecurity));
        String forceUpdateScalableElement = attributes.getProperty(PropertyRegistry.CONTEXT_FORCE_GENERATE_SCALABLE_ELEMENT);
        context.setForceUpdateScalableElement(stringHasValue(forceUpdateScalableElement) && Boolean.parseBoolean(forceUpdateScalableElement));
        configuration.addContext(context);

        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);

            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            if ("property".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseProperty(context, childNode);
            } else if ("plugin".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parsePlugin(context, childNode);
            } else if ("commentGenerator".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseCommentGenerator(context, childNode);
            } else if ("jdbcConnection".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseJdbcConnection(context, childNode);
            } else if ("connectionFactory".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseConnectionFactory(context, childNode);
            } else if ("javaModelGenerator".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseJavaModelGenerator(context, childNode);
            } else if ("javaTypeResolver".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseJavaTypeResolver(context, childNode);
            } else if ("sqlMapGenerator".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseSqlMapGenerator(context, childNode);
            } else if ("javaClientGenerator".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseJavaClientGenerator(context, childNode);
            } else if ("table".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseTable(context, childNode);
            }
        }
        //补充property = "moduleKeyword"到context
        if (context.getModuleKeyword() == null) {
            String s = Optional.ofNullable(context.getProperty(PropertyRegistry.CONTEXT_MODULE_KEYWORD))
                    .orElse(Optional.ofNullable(context.getProperty(PropertyRegistry.CONTEXT_HTML_TARGET_PACKAGE)).orElse(null));
            if (stringHasValue(s)) {
                context.setModuleKeyword(s);
            }
        }
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
        TableConfiguration tc = new TableConfiguration(context);
        Properties attributes = parseAttributes(node);
        String ignore = attributes.getProperty("ignore");
        String domainObjectName = attributes.getProperty("domainObjectName");
        if (!stringHasValue(ignore) && stringHasValue(domainObjectName)) {
            JavaModelGeneratorConfiguration gc = context.getJavaModelGeneratorConfiguration();
            if (JavaBeansUtil.javaFileExist(gc.getTargetProject(), gc.getTargetPackage(), domainObjectName)) {
                return;
            }
        }
        tc.setIgnore(isTrue(ignore));
        if (Boolean.parseBoolean(ignore)) {
            return;
        }
        context.addTableConfiguration(tc);
        String catalog = attributes.getProperty("catalog"); //$NON-NLS-1$
        if (stringHasValue(catalog)) {
            tc.setCatalog(catalog);
        }

        String schema = attributes.getProperty("schema"); //$NON-NLS-1$
        if (stringHasValue(schema)) {
            tc.setSchema(schema);
        }

        String tableName = attributes.getProperty("tableName"); //$NON-NLS-1$
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

            if ("property".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseProperty(tc, childNode);
            } else if ("columnOverride".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseColumnOverride(tc, childNode);
            } else if ("ignoreColumn".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseIgnoreColumn(tc, childNode);
            } else if ("ignoreColumnsByRegex".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseIgnoreColumnByRegex(tc, childNode);
            } else if ("generatedKey".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseGeneratedKey(tc, childNode);
            } else if ("domainObjectRenamingRule".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseDomainObjectRenamingRule(tc, childNode);
            } else if ("columnRenamingRule".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseColumnRenamingRule(tc, childNode);
            } else if ("selectByTable".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseSelectByTable(tc, childNode);
            } else if ("selectByColumn".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseSelectByColumn(tc, childNode);
            } else if ("javaModelAssociation".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseJavaModelRelation(tc, childNode, RelationTypeEnum.association);
            } else if ("javaModelCollection".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseJavaModelRelation(tc, childNode, RelationTypeEnum.collection);
            } else if ("generateHtml".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseGenerateHtml(context, tc, childNode);
            } else if ("generateService".equals(childNode.getNodeName())) {
                parseGenerateService(context, tc, childNode);
            } else if ("generateController".equals(childNode.getNodeName())) {
                parseGenerateController(context, tc, childNode);
            } else if ("generateDao".equals(childNode.getNodeName())) {
                parseGenerateDao(context, tc, childNode);
            } else if ("generateModel".equals(childNode.getNodeName())) {
                parseGenerateModel(context, tc, childNode);
            } else if ("generateSqlMap".equals(childNode.getNodeName())) {
                parseGenerateSqlMap(context, tc, childNode);
            } else if ("generateSqlSchema".equals(childNode.getNodeName())) {
                parseGenerateSqlSchema(context, tc, childNode);
            } else if ("generateModelVO".equals(childNode.getNodeName())) {
                parseGenerateModelVO(context, tc, childNode);
            } else if ("generateViewVO".equals(childNode.getNodeName())) {
                parseGenerateViewVO(context, tc, childNode);
            } else if ("generateExcelVO".equals(childNode.getNodeName())) {
                parseGenerateExcelVO(context, tc, childNode);
            }else if ("generateRequestVO".equals(childNode.getNodeName())) {
                parseGenerateRequestVO(context, tc, childNode);
            }
        }
        //根据所有配置信息，进行调整
        //1、enableChildren
        if (tc.getJavaModelGeneratorConfiguration().isGenerateChildren()) {
            long childrenCount = tc.getRelationPropertyHolders().stream()
                    .filter(c -> "children".equalsIgnoreCase(c.getPropertyName()))
                    .count();
            long selectByColumnParentIdCount = tc.getSelectByColumnGeneratorConfigurations().stream()
                    .filter(c -> "PARENT_ID".equalsIgnoreCase(c.getColumnName()))
                    .count();
            if (selectByColumnParentIdCount==0) {
                tc.addSelectByColumnProperties(new SelectByColumnGeneratorConfiguration("PARENT_ID"));
            }
            if (childrenCount==0) {
                RelationGeneratorConfiguration relationGeneratorConfiguration = new RelationGeneratorConfiguration();
                relationGeneratorConfiguration.setPropertyName("children");
                relationGeneratorConfiguration.setColumn("ID_");
                StringBuilder sb = new StringBuilder(tc.getJavaModelGeneratorConfiguration().getTargetPackage());
                sb.append(".").append(tc.getDomainObjectName());
                relationGeneratorConfiguration.setModelTye(sb.toString());
                sb.setLength(0);
                sb.append(tc.getJavaClientGeneratorConfiguration().getTargetPackage()).append(".");
                sb.append(tc.getDomainObjectName()).append("Mapper").append(".");
                sb.append("selectByColumnParentId");
                relationGeneratorConfiguration.setSelect(sb.toString());
                relationGeneratorConfiguration.setType(RelationTypeEnum.collection);
                tc.addRelationPropertyHolders(relationGeneratorConfiguration);
            }
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
        String orderByClause = attributes.getProperty("orderByClause"); //$NON-NLS-1$
        String additionClause = attributes.getProperty("additionClause"); //$NON-NLS-1$
        String returnType = attributes.getProperty("returnType"); //$NON-NLS-1$
        SelectByTableGeneratorConfiguration selectByTableGeneratorConfiguration = new SelectByTableGeneratorConfiguration();
        selectByTableGeneratorConfiguration.setTableName(table);
        selectByTableGeneratorConfiguration.setPrimaryKeyColumn(thisColumn);
        selectByTableGeneratorConfiguration.setOtherPrimaryKeyColumn(otherColumn);
        selectByTableGeneratorConfiguration.setMethodName("selectByTable" + methodSuffix);
        selectByTableGeneratorConfiguration.setOrderByClause(orderByClause);
        selectByTableGeneratorConfiguration.setAdditionCondition(additionClause);
        if (stringHasValue(returnType)) {
            selectByTableGeneratorConfiguration.setReturnTypeParam(returnType);
        }
        tc.addSelectByTableGeneratorConfiguration(selectByTableGeneratorConfiguration);
    }

    private void parseSelectByColumn(TableConfiguration tc, Node node) {
        Properties attributes = parseAttributes(node);
        String column = attributes.getProperty("column"); //$NON-NLS-1$
        String orderByClause = attributes.getProperty("orderByClause"); //$NON-NLS-1$
        String returnType = attributes.getProperty("returnType"); //$NON-NLS-1$
        SelectByColumnGeneratorConfiguration selectByColumnGeneratorConfiguration = new SelectByColumnGeneratorConfiguration(column);
        selectByColumnGeneratorConfiguration.setOrderByClause(orderByClause);
        if (stringHasValue(returnType)) {
            selectByColumnGeneratorConfiguration.setReturnTypeParam(returnType);
        }
        tc.addSelectByColumnProperties(selectByColumnGeneratorConfiguration);
    }

    private void parseJavaModelRelation(TableConfiguration tc, Node node, RelationTypeEnum relationTypeEnum) {
        Properties attributes = parseAttributes(node);
        String fieldName = attributes.getProperty("fieldName"); //$NON-NLS-1$
        String whereColumn = attributes.getProperty("whereColumn"); //$NON-NLS-1$
        String modelType = attributes.getProperty("modelType"); //$NON-NLS-1$
        String mapperMethod = attributes.getProperty("mapperMethod"); //$NON-NLS-1$
        RelationGeneratorConfiguration relationGeneratorConfiguration = new RelationGeneratorConfiguration();
        relationGeneratorConfiguration.setPropertyName(fieldName);
        relationGeneratorConfiguration.setColumn(whereColumn);
        relationGeneratorConfiguration.setModelTye(modelType);
        relationGeneratorConfiguration.setSelect(mapperMethod);
        relationGeneratorConfiguration.setType(relationTypeEnum);
        tc.addRelationPropertyHolders(relationGeneratorConfiguration);
    }

    private void parseGenerateHtml(Context context, TableConfiguration tc, Node node) {
        Properties attributes = parseAttributes(node);
        HtmlGeneratorConfiguration htmlGeneratorConfiguration = new HtmlGeneratorConfiguration(context, tc);
        htmlGeneratorConfiguration.setGenerate(Boolean.parseBoolean(attributes.getProperty(PropertyRegistry.ANY_GENERATE)));

        String targetProject = Optional.ofNullable(attributes.getProperty(PropertyRegistry.ANY_TARGET_PROJECT)).orElse(
                Optional.ofNullable(context.getProperty(PropertyRegistry.CONTEXT_HTML_TARGET_PROJECT))
                        .orElse("src/main/resources/templates"));
        htmlGeneratorConfiguration.setTargetProject(targetProject);

        String htmlTargetPackage = Optional.ofNullable(attributes.getProperty(PropertyRegistry.ANY_TARGET_PACKAGE))
                .orElse(Optional.ofNullable(context.getModuleKeyword())
                        .orElse(Optional.ofNullable(context.getProperty(PropertyRegistry.CONTEXT_HTML_TARGET_PACKAGE)).orElse("html")));

        htmlGeneratorConfiguration.setTargetPackage(htmlTargetPackage);

        String viewPath = attributes.getProperty(PropertyRegistry.TABLE_VIEW_PATH);
        String tmpFullPath = htmlGeneratorConfiguration.getTargetPackage() + "/" + viewPath;
        String fullViewPath = Arrays.stream(tmpFullPath.split("[/\\\\]")).filter(StringUtility::stringHasValue).collect(Collectors.joining("/"));
        htmlGeneratorConfiguration.setViewPath(fullViewPath);
        htmlGeneratorConfiguration.setTargetPackage(StringUtility.substringBeforeLast(fullViewPath, "/"));

        htmlGeneratorConfiguration.setHtmlFileName(
                String.join(".", StringUtility.substringAfterLast(fullViewPath, "/"), PropertyRegistry.TABLE_HTML_FIE_SUFFIX));

        String loadingFrameType = Optional.ofNullable(attributes.getProperty("loadingFrameType"))
                .orElse(Optional.ofNullable(context.getProperty(PropertyRegistry.CONTEXT_HTML_LOADING_FRAME_TYPE)).orElse("full"));
        htmlGeneratorConfiguration.setLoadingFrameType(loadingFrameType);

        String barPosition = Optional.ofNullable(attributes.getProperty("barPosition"))
                .orElse(Optional.ofNullable(context.getProperty(PropertyRegistry.CONTEXT_HTML_BAR_POSITION)).orElse("bottom"));
        htmlGeneratorConfiguration.setBarPosition(barPosition);

        String uiFrameType = Optional.ofNullable(attributes.getProperty("uiFrameType"))
                .orElse(Optional.ofNullable(context.getProperty(PropertyRegistry.CONTEXT_HTML_UI_FRAME)).orElse("layui"));
        htmlGeneratorConfiguration.setUiFrameType(uiFrameType);

        String pageColumnsNum = Optional.ofNullable(attributes.getProperty("pageColumnsNum"))
                .orElse(Optional.ofNullable(context.getProperty(PropertyRegistry.CONTEXT_HTML_PAGE_COLUMNS_NUM)).orElse("2"));
        htmlGeneratorConfiguration.setPageColumnsNum(Integer.parseInt(pageColumnsNum));

        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            if ("property".equals(childNode.getNodeName())) {
                parseHtmlMapGeneratorProperty(htmlGeneratorConfiguration, childNode, context);
                parseProperty(htmlGeneratorConfiguration, childNode);
            } else if (PropertyRegistry.ELEMENT_HTML_ELEMENT_DESCRIPTOR.equals(childNode.getNodeName())) {
                parseHtmlElementDescriptor(htmlGeneratorConfiguration, childNode);
            }
        }
        tc.addHtmlMapGeneratorConfigurations(htmlGeneratorConfiguration);
    }

    protected void parseHtmlMapGeneratorProperty(HtmlGeneratorConfiguration htmlGeneratorConfiguration, Node node, Context context) {
        Properties properties = parseAttributes(node);
        String propertyName = properties.get("name").toString();
        //计算全局隐藏
        List<String> hiddenColunms = new ArrayList<>();
        String contextHtmlHiddenColumns = context.getProperty(PropertyRegistry.ANY_HTML_HIDDEN_COLUMNS);
        if (stringHasValue(contextHtmlHiddenColumns)) {
            hiddenColunms = Arrays.stream(contextHtmlHiddenColumns.split(",")).collect(Collectors.toList());
        }
        switch (propertyName) {
            case PropertyRegistry.ANY_HTML_HIDDEN_COLUMNS:
                String htmlHiddenColumns = properties.get("value").toString();
                if (stringHasValue(htmlHiddenColumns)) {
                    hiddenColunms = Stream.of(Arrays.stream(htmlHiddenColumns.split(",")), hiddenColunms.stream())
                            .flatMap(Function.identity()).distinct().collect(Collectors.toList());
                }
                break;

            case PropertyRegistry.TABLE_HTML_REQUIRED_COLUMNS:
                String required = properties.get("value").toString();
                List<String> collect = Arrays.stream(required.split(","))
                        .map(String::toUpperCase).distinct().collect(Collectors.toList());
                htmlGeneratorConfiguration.setElementRequired(collect);
                break;
        }
        htmlGeneratorConfiguration.setHiddenColumns(hiddenColunms);
    }

    protected void parseHtmlElementDescriptor(HtmlGeneratorConfiguration htmlGeneratorConfiguration, Node node) {
        Properties attributes = parseAttributes(node);
        String column = attributes.getProperty("column");
        String tagType = attributes.getProperty("tagType");
        String dataUrl = attributes.getProperty("dataUrl");
        String dataFormat = attributes.getProperty("dataFormat");
        HtmlElementDescriptor htmlElementDescriptor = new HtmlElementDescriptor();
        htmlElementDescriptor.setName(column);
        htmlElementDescriptor.setTagType(tagType);
        if (dataUrl != null) {
            htmlElementDescriptor.setDataUrl(dataUrl);
        }
        if (dataFormat != null) {
            htmlElementDescriptor.setDataFormat(dataFormat);
        }
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
        formOptionGeneratorConfiguration.setDataType("tree".equalsIgnoreCase(type)?1:0);
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

    private void parseGenerateModelVO(Context context, TableConfiguration tc, Node node) {
        Properties attributes = parseAttributes(node);
        VOModelGeneratorConfiguration vOModelGeneratorConfiguration = new VOModelGeneratorConfiguration(context,tc);
        parseColumnsList(attributes, vOModelGeneratorConfiguration);
        String includeColumns = attributes.getProperty(PropertyRegistry.ELEMENT_INCLUDE_COLUMNS);
        if (stringHasValue(includeColumns)) {
            vOModelGeneratorConfiguration.setIncludeColumns(Arrays.asList(includeColumns.split(",")));
        }
        parseChildNodeOnlyProperty(vOModelGeneratorConfiguration, node);
        tc.setVoModelGeneratorConfiguration(vOModelGeneratorConfiguration);
    }

    private void parseGenerateExcelVO(Context context, TableConfiguration tc, Node node) {
        Properties attributes = parseAttributes(node);
        VOExcelGeneratorConfiguration vOExcelGeneratorConfiguration = new VOExcelGeneratorConfiguration(context,tc);
        parseColumnsList(attributes, vOExcelGeneratorConfiguration);
        String includeColumns = attributes.getProperty(PropertyRegistry.ELEMENT_INCLUDE_COLUMNS);
        if (stringHasValue(includeColumns)) {
            vOExcelGeneratorConfiguration.setIncludeColumns(Arrays.asList(includeColumns.split(",")));
        }
        parseChildNodeOnlyProperty(vOExcelGeneratorConfiguration, node);
        tc.setVoExcelGeneratorConfiguration(vOExcelGeneratorConfiguration);
    }

    private void parseGenerateViewVO(Context context, TableConfiguration tc, Node node) {
        VOViewGeneratorConfiguration voViewGeneratorConfiguration = new VOViewGeneratorConfiguration(context,tc);
        Properties attributes = parseAttributes(node);
        parseColumnsList(attributes, voViewGeneratorConfiguration);
        String includeColumns = attributes.getProperty(PropertyRegistry.ELEMENT_INCLUDE_COLUMNS);
        if (stringHasValue(includeColumns)) {
            voViewGeneratorConfiguration.setIncludeColumns(Arrays.asList(includeColumns.split(",")));
        }
        String indexColumn = attributes.getProperty("indexColumn");
        if (stringHasValue(indexColumn)) {
            voViewGeneratorConfiguration.setIndexColumn(indexColumn);
        }
        String actionColumn = attributes.getProperty("actionColumn");
        if (stringHasValue(actionColumn)) {
            voViewGeneratorConfiguration.setActionColumn(Arrays.asList(actionColumn.split(",")));
        }
        String queryColumns = attributes.getProperty("queryColumns");
        if (stringHasValue(queryColumns)) {
            voViewGeneratorConfiguration.setQueryColumns(Arrays.asList(queryColumns.split(",")));
        }
        parseChildNodeOnlyProperty(voViewGeneratorConfiguration, node);
        tc.setVoViewGeneratorConfiguration(voViewGeneratorConfiguration);
    }


    private void parseGenerateRequestVO(Context context, TableConfiguration tc, Node node) {
        VORequestGeneratorConfiguration voRequestGeneratorConfiguration = new VORequestGeneratorConfiguration(context,tc);
        Properties attributes = parseAttributes(node);
        parseColumnsList(attributes, voRequestGeneratorConfiguration);
        String includePageParam = attributes.getProperty("includePageParam");
        voRequestGeneratorConfiguration.setIncludePageParam(Boolean.parseBoolean(includePageParam));
        parseChildNodeOnlyProperty(voRequestGeneratorConfiguration, node);
        tc.setVoRequestGeneratorConfiguration(voRequestGeneratorConfiguration);
    }

    private void parseGenerateModel(Context context, TableConfiguration tc, Node node) {
        JavaModelGeneratorConfiguration javaModelGeneratorConfiguration = context.getJavaModelGeneratorConfiguration();
        Properties attributes = parseAttributes(node);
        String generate = attributes.getProperty(PropertyRegistry.ANY_GENERATE);
        javaModelGeneratorConfiguration.setGenerate(Boolean.parseBoolean(generate));
        String noMetaAnnotation = attributes.getProperty(PropertyRegistry.ANY_NO_META_ANNOTATION);
        if (stringHasValue(noMetaAnnotation)) {
            javaModelGeneratorConfiguration.setNoMetaAnnotation(Boolean.parseBoolean(noMetaAnnotation));
        }
        //判断enableChildren
        javaModelGeneratorConfiguration.setGenerateChildren(Boolean.parseBoolean(attributes.getProperty("enableChildren")));

        tc.setJavaModelGeneratorConfiguration(javaModelGeneratorConfiguration);
        parseAbstractConfigAttributes(attributes, javaModelGeneratorConfiguration, node);
    }

    private void parseColumnsList(Properties attributes, VOGeneratorConfiguration vOGeneratorConfiguration) {
        String generate = attributes.getProperty(PropertyRegistry.ANY_GENERATE);
        vOGeneratorConfiguration.setGenerate(Boolean.parseBoolean(generate));
        String excludeColumns = attributes.getProperty(PropertyRegistry.ELEMENT_EXCLUDE_COLUMNS);
        if (stringHasValue(excludeColumns)) {
            vOGeneratorConfiguration.setExcludeColumns(Arrays.asList(excludeColumns.split(",")));
        }
    }

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
