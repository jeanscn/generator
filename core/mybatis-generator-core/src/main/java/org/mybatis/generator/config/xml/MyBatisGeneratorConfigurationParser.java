package org.mybatis.generator.config.xml;

import com.vgosoft.core.constant.GlobalConstant;
import com.vgosoft.core.constant.enums.core.QueryModesEnum;
import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import com.vgosoft.core.constant.enums.db.FieldTypeEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementDataFormat;
import com.vgosoft.core.constant.enums.view.HtmlElementDataSourceEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementTagTypeEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.config.*;
import org.mybatis.generator.custom.HtmlDocumentTypeEnum;
import org.mybatis.generator.custom.RelationTypeEnum;
import org.mybatis.generator.custom.ViewVoUiFrameEnum;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.ObjectFactory;
import org.mybatis.generator.internal.util.JavaBeansUtil;
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
        String integrateSpringSecurity = attributes.getProperty(PropertyRegistry.CONTEXT_INTEGRATE_SPRING_SECURITY);
        context.setIntegrateSpringSecurity(!stringHasValue(integrateSpringSecurity) || Boolean.parseBoolean(integrateSpringSecurity));
        String forceUpdateScalableElement = attributes.getProperty(PropertyRegistry.CONTEXT_FORCE_UPDATE_SCALABLE_ELEMENT);
        context.setForceUpdateScalableElement(stringHasValue(forceUpdateScalableElement) && Boolean.parseBoolean(forceUpdateScalableElement));
        String jdkVersion = attributes.getProperty("jdkVersion", "8");
        context.setJdkVersion(Integer.parseInt(jdkVersion));

        String vueEndProjectPath = attributes.getProperty("vueEndProjectPath");
        if (stringHasValue(vueEndProjectPath)) {
            context.setVueEndProjectPath(vueEndProjectPath);
        }

        String onlyTables = attributes.getProperty("onlyTables");
        if (stringHasValue(onlyTables)) {
            context.setOnlyTablesGenerate(splitToList(onlyTables));
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
        if (stringHasValue(tableName)) {
            tableName = removeSpaces(tableName);
        } else {
            return;
        }
        String ignore = attributes.getProperty("ignore");
        String domainObjectName = attributes.getProperty("domainObjectName");
        if (!stringHasValue(domainObjectName)) {
            domainObjectName = JavaBeansUtil.getCamelCaseString(tableName, true);
        } else {
            domainObjectName = JavaBeansUtil.getFirstCharacterUppercase(removeSpaces(domainObjectName));
        }
        //先确认是否指定了生成范围
        List<String> tables = context.getOnlyTablesGenerate();
        if (tables.isEmpty()) {                   //未指定生成范围
            if (!stringHasValue(ignore)) {          //未指定忽略 则判断是否已经生成过
                JavaModelGeneratorConfiguration gc = context.getJavaModelGeneratorConfiguration();
                if (JavaBeansUtil.javaFileExist(gc.getTargetProject(), gc.getTargetPackage(), domainObjectName)) {
                    tc.setIgnore(true);
                    return;
                }
                tc.setIgnore(false);
            } else {
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
            tc.setAlias(removeSpaces(alias));
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
                tc.setValidateIgnoreColumns(new HashSet<>());
            } else {
                tc.setValidateIgnoreColumns(splitToSet(validateIgnoreColumns));
            }
        } else {
            tc.setValidateIgnoreColumns(new HashSet<>(Arrays.asList("delete_flag", "version_", "created_", "modified_", "created_id", "modified_id")));
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

        String enableDropTables = attributes.getProperty("enableDropTables"); //$NON-NLS-1$
        if (stringHasValue(enableDropTables)) {
            tc.setEnableDropTables(splitToSet(enableDropTables));
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
                case ("overridePropertyValue"):
                    tc.addOverridePropertyConfigurations(parseVoOverrideColumn(context, tc, childNode));
                    break;
                case ("additionalProperty"):
                    tc.addAdditionalPropertyConfigurations(parseAdditionalProperty(context, tc, childNode));
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
                    parseHtml(context, tc, childNode);
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
                    parseVO(context, tc, childNode);
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
                config.setTargetProject(getTargetProject(targetProject));
                configImpl.setTargetProject(getTargetProject(targetProject));
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
            JavaControllerGeneratorConfiguration configuration = new JavaControllerGeneratorConfiguration(context, tc);
            configuration.setGenerate(tc.getHtmlMapGeneratorConfigurations().stream().anyMatch(c -> stringHasValue(c.getViewPath())));
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
        boolean required = isTrue(attributes.getProperty("required"));
        co.setRequired(required);
        String maxLength = attributes.getProperty("maxLength");
        if (stringHasValue(maxLength)) {
            co.setMaxLength(Integer.parseInt(maxLength));
        }
        String minLength = attributes.getProperty("minLength");
        if (stringHasValue(minLength)) {
            co.setMinLength(Integer.parseInt(minLength));
        }

        String columnComment = attributes.getProperty("columnComment");
        if (stringHasValue(columnComment)) {
            co.setColumnComment(columnComment);
        }
        String columnSubComment = attributes.getProperty("columnSubComment");
        if (stringHasValue(columnSubComment)) {
            co.setColumnSubComment(columnSubComment);
        }
        String headerAlign = attributes.getProperty("headerAlign");
        if (stringHasValue(headerAlign)) {
            co.setHeaderAlign(headerAlign);
        }
        String align = attributes.getProperty("align");
        if (stringHasValue(align)) {
            co.setAlign(align);
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

        //计算子元素
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            if (childNode.getNodeName().equals(PropertyRegistry.ELEMENT_ENABLE_CACHE)) {
                EnableCacheConfiguration enableCacheConfiguration = parseEnableCache(childNode);
                selectByTableGeneratorConfiguration.getCacheConfigurationList().add(enableCacheConfiguration);
            }
        }
        tc.addSelectByTableGeneratorConfiguration(selectByTableGeneratorConfiguration);
    }

    private EnableCacheConfiguration parseEnableCache(Node node) {
        EnableCacheConfiguration enableCacheConfiguration = new EnableCacheConfiguration();
        Properties attributes = parseAttributes(node);
        String enableCache = attributes.getProperty("enableCache");
        if (stringHasValue(enableCache)) {
            enableCacheConfiguration.setEnableCache(Boolean.parseBoolean(enableCache));
        }
        String cacheNames = attributes.getProperty("cacheNames");
        if (stringHasValue(cacheNames)) {
            enableCacheConfiguration.setCacheNames(splitToList(cacheNames));
        }
        return enableCacheConfiguration;
    }

    private void parseSelectByColumn(TableConfiguration tc, Node node) {
        Properties attributes = parseAttributes(node);
        String column = attributes.getProperty("column"); //$NON-NLS-1$
        String orderByClause = attributes.getProperty("orderByClause"); //$NON-NLS-1$
        String returnType = attributes.getProperty("returnType"); //$NON-NLS-1$
        SelectByColumnGeneratorConfiguration selectByColumnGeneratorConfiguration = new SelectByColumnGeneratorConfiguration();
        selectByColumnGeneratorConfiguration.setColumnNames(splitToList(column));
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
            configuration.setParentIdColumnName(DefaultColumnNameEnum.PARENT_ID.columnName());
        }
        String idColumnName = attributes.getProperty("primaryKeyColumn");
        if (stringHasValue(idColumnName)) {
            configuration.setPrimaryKeyColumnName(idColumnName);
        } else {
            configuration.setPrimaryKeyColumnName(DefaultColumnNameEnum.ID.columnName());
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
        String initalString = attributes.getProperty(PropertyRegistry.ELEMENT_INITIALIZATION_STRING);
        if (stringHasValue(initalString)) {
            relationGeneratorConfiguration.setInitializationString(initalString);
        }
        String importType = attributes.getProperty(PropertyRegistry.ELEMENT_IMPORT_TYPE);
        if (stringHasValue(importType)) {
            relationGeneratorConfiguration.addImportTypes(importType);
        }
        tc.addRelationGeneratorConfiguration(relationGeneratorConfiguration);
    }

    private void parseHtml(Context context, TableConfiguration tc, Node node) {
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
        htmlGeneratorConfiguration.setBaseTargetPackage(htmlTargetPackage);
        if (!stringHasValue(viewPath)) {
            viewPath = tc.getTableName().toLowerCase();
        }
        htmlGeneratorConfiguration.setSimpleViewPath(viewPath);
        fullViewPath = htmlTargetPackage + "/" + viewPath;
        htmlGeneratorConfiguration.setViewPath(fullViewPath);
        htmlGeneratorConfiguration.setTargetPackage(substringBeforeLast(fullViewPath, "/"));
        htmlGeneratorConfiguration.setHtmlFileName(
                String.join(".", substringAfterLast(fullViewPath, "/"), PropertyRegistry.TABLE_HTML_FIE_SUFFIX));
        String type = attributes.getProperty("type");
        if (stringHasValue(type)) {
            htmlGeneratorConfiguration.setType(HtmlDocumentTypeEnum.getEnum(type));
        }
        String title = attributes.getProperty("title");
        if (stringHasValue(title)) {
            htmlGeneratorConfiguration.setTitle(title);
        }
        String overWriteHtmlFile = attributes.getProperty(PropertyRegistry.TABLE_OVERRIDE_HTML_FILE);
        if (stringHasValue(overWriteHtmlFile)) {
            htmlGeneratorConfiguration.setOverWriteHtmlFile(Boolean.parseBoolean(overWriteHtmlFile));
        }
        String overWriteJsFile = attributes.getProperty(PropertyRegistry.TABLE_OVERRIDE_JS_FILE);
        if (stringHasValue(overWriteJsFile)) {
            htmlGeneratorConfiguration.setOverWriteJsFile(Boolean.parseBoolean(overWriteJsFile));
        }
        String overWriteCssFile = attributes.getProperty(PropertyRegistry.TABLE_OVERRIDE_CSS_FILE);
        if (stringHasValue(overWriteCssFile)) {
            htmlGeneratorConfiguration.setOverWriteCssFile(Boolean.parseBoolean(overWriteCssFile));
        }

        String aDefault = attributes.getProperty("default");
        if (stringHasValue(aDefault)) {
            htmlGeneratorConfiguration.setDefaultConfig(Boolean.parseBoolean(aDefault));
        }

        String overWriteVueView = attributes.getProperty("overWriteVueView");
        if (stringHasValue(overWriteVueView)) {
            htmlGeneratorConfiguration.setOverWriteVueView(Boolean.parseBoolean(overWriteVueView));
        }
        String overWriteVueEdit = attributes.getProperty("overWriteVueEdit");
        if (stringHasValue(overWriteVueEdit)) {
            htmlGeneratorConfiguration.setOverWriteVueEdit(Boolean.parseBoolean(overWriteVueEdit));
        }
        String overWriteVueDetail = attributes.getProperty("overWriteVueDetail");
        if (stringHasValue(overWriteVueDetail)) {
            htmlGeneratorConfiguration.setOverWriteVueDetail(Boolean.parseBoolean(overWriteVueDetail));
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
                    parseHtmlGeneratorProperty(htmlGeneratorConfiguration, childNode);
                    parseProperty(htmlGeneratorConfiguration, childNode);
                    break;
                case PropertyRegistry.HTML_FORM_EXTEND_DESCRIPTOR:
                case PropertyRegistry.ELEMENT_HTML_ELEMENT_DESCRIPTOR:
                    HtmlElementDescriptor htmlElementDescriptor = parseHtmlElementDescriptor(childNode);
                    htmlElementDescriptor.setHtmlGeneratorConfiguration(htmlGeneratorConfiguration);
                    if (stringHasValue(htmlElementDescriptor.getName()) && stringHasValue(htmlElementDescriptor.getTagType())) {
                        htmlGeneratorConfiguration.getElementDescriptors().add(htmlElementDescriptor);
                    }
                    break;
                case PropertyRegistry.ELEMENT_HTML_TABLE_SELECT_DESCRIPTOR:
                    HtmlElementDescriptor htmlTableSelectDescriptor = parseHtmlElementDescriptor(childNode);
                    htmlTableSelectDescriptor.setTagType(HtmlElementTagTypeEnum.TABLE_SELECT.codeName());
                    htmlTableSelectDescriptor.setDataSource(HtmlElementDataSourceEnum.DICT.codeName());
                    htmlTableSelectDescriptor.setHtmlGeneratorConfiguration(htmlGeneratorConfiguration);
                    if (stringHasValue(htmlTableSelectDescriptor.getName()) && stringHasValue(htmlTableSelectDescriptor.getTagType())) {
                        htmlGeneratorConfiguration.getElementDescriptors().add(htmlTableSelectDescriptor);
                    }
                    break;
                case PropertyRegistry.ELEMENT_HTML_LAYOUT:
                    parseHtmlLayout(htmlGeneratorConfiguration, childNode);
                    break;
                case PropertyRegistry.ELEMENT_HTML_FILE_ATTACHMENT:
                    parseHtmlFileAttachment(htmlGeneratorConfiguration, childNode);
                    break;
                case PropertyRegistry.ELEMENT_APPROVAL_COMMENT:
                    parseHtmlApprovalComment(htmlGeneratorConfiguration, childNode);
                    break;
                case PropertyRegistry.ELEMENT_HTML_ELEMENT_INNER_LIST:
                    parseHtmlElementInnerList(htmlGeneratorConfiguration, childNode);
                    break;
                case (PropertyRegistry.ELEMENT_HTML_BUTTON):
                    HtmlButtonGeneratorConfiguration htmlButton = parseHtmlButton(childNode);
                    htmlGeneratorConfiguration.getHtmlButtons().add(htmlButton);
                    break;
            }
        }
        if (htmlGeneratorConfiguration.getLayoutDescriptor() == null) {
            htmlGeneratorConfiguration.setLayoutDescriptor(new HtmlLayoutDescriptor());
        }
        tc.addHtmlMapGeneratorConfigurations(htmlGeneratorConfiguration);
    }

    private void parseHtmlApprovalComment(HtmlGeneratorConfiguration htmlGeneratorConfiguration, Node childNode) {
        Properties attributes = parseAttributes(childNode);
        HtmlApprovalCommentConfiguration approvalCommentConfiguration = new HtmlApprovalCommentConfiguration();
        String generate = attributes.getProperty(PropertyRegistry.ANY_GENERATE);
        if (stringHasValue(generate)) {
            approvalCommentConfiguration.setGenerate(Boolean.parseBoolean(generate));
        }
        String elementKey = attributes.getProperty("elementKey");
        if (stringHasValue(elementKey)) {
            approvalCommentConfiguration.setElementKey(elementKey);
        }
        String afterColumn = attributes.getProperty("afterColumn");
        if (stringHasValue(afterColumn)) {
            approvalCommentConfiguration.setAfterColumn(afterColumn);
        }
        String label = attributes.getProperty("label");
        if (stringHasValue(label)) {
            approvalCommentConfiguration.setLabel(label);
        }
        String locationTag = attributes.getProperty("locationTag");
        if (stringHasValue(locationTag)) {
            approvalCommentConfiguration.setLocationTag(locationTag);
        }
        htmlGeneratorConfiguration.addHtmlApprovalCommentConfiguration(approvalCommentConfiguration);
    }

    private void parseHtmlFileAttachment(HtmlGeneratorConfiguration htmlGeneratorConfiguration, Node childNode) {
        List<HtmlFileAttachmentConfiguration> attachmentConfigurations = htmlGeneratorConfiguration.getHtmlFileAttachmentConfiguration();

        HtmlFileAttachmentConfiguration htmlFileAttachmentConfiguration = new HtmlFileAttachmentConfiguration();

        Properties attributes = parseAttributes(childNode);
        String generate = attributes.getProperty(PropertyRegistry.ANY_GENERATE);

        if (stringHasValue(generate)) {
            htmlFileAttachmentConfiguration.setGenerate(Boolean.parseBoolean(generate));
        }

        String elementKey = attributes.getProperty("elementKey");
        if (stringHasValue(elementKey)) {
            htmlFileAttachmentConfiguration.setElementKey(elementKey);
        } else {
            htmlFileAttachmentConfiguration.setElementKey("upload_tmp_field" + attachmentConfigurations.size() + 1);
        }

        String multiple = attributes.getProperty("multiple");
        if (stringHasValue(multiple)) {
            htmlFileAttachmentConfiguration.setMultiple(Boolean.parseBoolean(multiple));
        }
        String type = attributes.getProperty("type");
        if (stringHasValue(type)) {
            htmlFileAttachmentConfiguration.setType(type);
        }
        String location = attributes.getProperty("location");
        if (stringHasValue(location)) {
            htmlFileAttachmentConfiguration.setLocation(location);
        }
        String limit = attributes.getProperty("limit");
        if (stringHasValue(limit)) {
            htmlFileAttachmentConfiguration.setLimit(Integer.parseInt(limit));
        }

        String tip = attributes.getProperty("tip");
        if (stringHasValue(tip)) {
            htmlFileAttachmentConfiguration.setTip(tip);
        }

        String basePath = attributes.getProperty("restBasePath");
        if (stringHasValue(basePath)) {
            htmlFileAttachmentConfiguration.setRestBasePath(basePath);
        }
        String exclusive = attributes.getProperty("exclusive");
        if (stringHasValue(exclusive)) {
            htmlFileAttachmentConfiguration.setExclusive(Boolean.parseBoolean(exclusive));
        }
        String afterColumn = attributes.getProperty("afterColumn");
        if (stringHasValue(afterColumn)) {
            htmlFileAttachmentConfiguration.setAfterColumn(afterColumn);
        }
        String label = attributes.getProperty("label");
        if (stringHasValue(label)) {
            htmlFileAttachmentConfiguration.setLabel(label);
        }
        String order = attributes.getProperty("order");
        if (stringHasValue(order)) {
            htmlFileAttachmentConfiguration.setOrder(Integer.parseInt(order));
        }

        htmlGeneratorConfiguration.getHtmlFileAttachmentConfiguration().add(htmlFileAttachmentConfiguration);
    }

    private void parseHtmlElementInnerList(HtmlGeneratorConfiguration htmlGeneratorConfiguration, Node childNode) {
        Properties attributes = parseAttributes(childNode);
        HtmlElementInnerListConfiguration htmlElementInnerList = new HtmlElementInnerListConfiguration();
        String elementKey = attributes.getProperty("elementKey");
        if (stringHasValue(elementKey)) {
            htmlElementInnerList.setElementKey(elementKey);
        }
        String listKey = attributes.getProperty("listKey");
        if (stringHasValue(listKey)) {
            htmlElementInnerList.setListKey(listKey);
        }
        String label = attributes.getProperty("label");
        if (stringHasValue(label)) {
            htmlElementInnerList.setLabel(label);
        }
        String moduleKeyword = attributes.getProperty(PropertyRegistry.CONTEXT_MODULE_KEYWORD);
        if (stringHasValue(moduleKeyword)) {
            htmlElementInnerList.setModuleKeyword(moduleKeyword.toLowerCase());
        } else {
            htmlElementInnerList.setModuleKeyword(htmlGeneratorConfiguration.getContext().getModuleKeyword().toLowerCase());
        }
        String sourceViewPath = attributes.getProperty("sourceViewPath");
        if (stringHasValue(sourceViewPath)) {
            htmlElementInnerList.setSourceViewPath(sourceViewPath.toLowerCase());
        }
        String sourceBeanName = attributes.getProperty("sourceBeanName");
        if (stringHasValue(sourceBeanName)) {
            htmlElementInnerList.setSourceBeanName(sourceBeanName);
        }
        htmlElementInnerList.setRelationField(attributes.getProperty("relationField"));
        String sourceViewVoClass = attributes.getProperty("sourceListViewClass");
        if (stringHasValue(sourceViewVoClass)) {
            htmlElementInnerList.setSourceListViewClass(sourceViewVoClass);
        }
        String relationKey = attributes.getProperty("relationKey");
        if (stringHasValue(relationKey)) {
            htmlElementInnerList.setRelationKey(relationKey);
        }
        String tagId = attributes.getProperty("tagId");
        htmlElementInnerList.setTagId(tagId);
        String dataField = attributes.getProperty("dataField");
        if (stringHasValue(dataField)) {
            htmlElementInnerList.setDataField(dataField);
        }
        String dataUrl = attributes.getProperty("dataUrl");
        if (stringHasValue(dataUrl)) {
            htmlElementInnerList.setDataUrl(dataUrl);
        }
        String span = attributes.getProperty("span");
        if (stringHasValue(span)) {
            htmlElementInnerList.setSpan(Integer.parseInt(span));
        } else {
            htmlElementInnerList.setSpan(24);
        }
        String afterColumn = attributes.getProperty("afterColumn");
        if (stringHasValue(afterColumn)) {
            htmlElementInnerList.setAfterColumn(afterColumn);
        }
        String containerType = attributes.getProperty("containerType");
        if (stringHasValue(containerType)) {
            htmlElementInnerList.setContainerType(containerType);
        }
        String order = attributes.getProperty("order");
        if (stringHasValue(order)) {
            htmlElementInnerList.setOrder(Integer.parseInt(order));
        }
        String editMode = attributes.getProperty("editMode");
        if (stringHasValue(editMode)) {
            htmlElementInnerList.setEditMode(editMode);
        }
        String editableFields = attributes.getProperty("editableFields");
        if (stringHasValue(editableFields)) {
            htmlElementInnerList.setEditableFields(splitToSet(editableFields));
        }
        String enablePager = attributes.getProperty("enablePager");
        if (stringHasValue(enablePager)) {
            htmlElementInnerList.setEnablePager(Boolean.parseBoolean(enablePager));
        }
        String vxeListButtons = attributes.getProperty("vxeListButtons");
        if (stringHasValue(vxeListButtons)) {
            htmlElementInnerList.setVxeListButtons(splitToSet(vxeListButtons));
        }
        String defaultFilterExpr = attributes.getProperty("defaultFilterExpr");
        if (stringHasValue(defaultFilterExpr)) {
            htmlElementInnerList.setDefaultFilterExpr(defaultFilterExpr);
        }
        String batchUpdateFields = attributes.getProperty("batchUpdateFields");
        if (stringHasValue(batchUpdateFields)) {
            htmlElementInnerList.setBatchUpdateFields(splitToSet(batchUpdateFields));
        }
        String showRowNumber = attributes.getProperty("showRowNumber");
        if (stringHasValue(showRowNumber)) {
            htmlElementInnerList.setShowRowNumber(Boolean.parseBoolean(showRowNumber));
        }
        String totalRow = attributes.getProperty("totalRow");
        if (stringHasValue(totalRow)) {
            htmlElementInnerList.setTotalRow(Boolean.parseBoolean(totalRow));
        }
        String totalFields = attributes.getProperty("totalFields");
        if (stringHasValue(totalFields)) {
            htmlElementInnerList.setTotalFields(splitToSet(totalFields));
        }
        String totalText = attributes.getProperty("totalText");
        if (stringHasValue(totalText)) {
            htmlElementInnerList.setTotalText(totalText);
        }
        String restBasePath = attributes.getProperty("restBasePath");
        if (stringHasValue(restBasePath)) {
            htmlElementInnerList.setRestBasePath(restBasePath);
        }
        if (sourceBeanName != null) {
            htmlElementInnerList.setRestBasePath(moduleKeyword + "/" + VStringUtil.toHyphenCase(sourceBeanName));
        }
        htmlGeneratorConfiguration.getHtmlElementInnerListConfiguration().add(htmlElementInnerList);
    }

    private void parseHtmlLayout(HtmlGeneratorConfiguration htmlGeneratorConfiguration, Node node) {
        Properties attributes = parseAttributes(node);
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
            htmlLayoutDescriptor.setExclusiveColumns(splitToList(exclusiveColumns));
        }
        String borderWidth = attributes.getProperty("borderWidth");
        if (stringHasValue(borderWidth)) {
            htmlLayoutDescriptor.setBorderWidth(Integer.parseInt(borderWidth));
        }
        String borderColor = attributes.getProperty("borderColor");
        if (stringHasValue(borderColor)) {
            htmlLayoutDescriptor.setBorderColor(borderColor);
        }
        String labelWidth = attributes.getProperty("labelWidth");
        if (stringHasValue(labelWidth)) {
            htmlLayoutDescriptor.setLabelWidth(labelWidth);
        }
        String labelPosition = attributes.getProperty("labelPosition");
        if (stringHasValue(labelPosition)) {
            htmlLayoutDescriptor.setLabelPosition(labelPosition);
        }
        String size = attributes.getProperty("size");
        if (stringHasValue(size)) {
            htmlLayoutDescriptor.setSize(size);
        }
        String popSize = attributes.getProperty("popSize");
        if (stringHasValue(popSize)) {
            htmlLayoutDescriptor.setPopSize(popSize);
        }
        String popDraggable = attributes.getProperty("popDraggable");
        if (stringHasValue(popDraggable)) {
            htmlLayoutDescriptor.setPopDraggable(Boolean.parseBoolean(popDraggable));
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
                    parseProperty(htmlLayoutDescriptor, childNode);
                    break;
                case "groupContainer":
                    HtmlGroupContainerConfiguration htmlGroupContainerConfiguration = parseHtmlGroupContainerConfiguration(childNode);
                    if (htmlGroupContainerConfiguration != null) {
                        htmlLayoutDescriptor.getGroupContainerConfigurations().add(htmlGroupContainerConfiguration);
                    }
                    break;
            }
        }

        htmlGeneratorConfiguration.setLayoutDescriptor(htmlLayoutDescriptor);
    }

    private HtmlGroupContainerConfiguration parseHtmlGroupContainerConfiguration(Node node) {
        Properties attributes = parseAttributes(node);
        HtmlGroupContainerConfiguration htmlGroupContainerConfiguration = new HtmlGroupContainerConfiguration();
        String elementKey = attributes.getProperty("elementKey");
        if (stringHasValue(elementKey)) {
            htmlGroupContainerConfiguration.setElementKey(elementKey);
        }
        String name = attributes.getProperty("name");
        if (stringHasValue(name)) {
            htmlGroupContainerConfiguration.setName(name);
        }
        String type = attributes.getProperty("type");
        if (stringHasValue(type)) {
            htmlGroupContainerConfiguration.setType(type);
        } else {
            return null;
        }
        String title = attributes.getProperty("title");
        if (stringHasValue(title)) {
            htmlGroupContainerConfiguration.setTitle(title);
        }
        String span = attributes.getProperty("span");
        if (stringHasValue(span)) {
            htmlGroupContainerConfiguration.setSpan(Integer.parseInt(span));
        }
        String columnNum = attributes.getProperty("columnNum");
        if (stringHasValue(columnNum)) {
            htmlGroupContainerConfiguration.setColumnNum(Integer.parseInt(columnNum));
        }
        String includeElements = attributes.getProperty("includeElements");
        if (stringHasValue(includeElements)) {
            htmlGroupContainerConfiguration.setIncludeElements(splitToList(includeElements));
        }
        String afterColumn = attributes.getProperty("afterColumn");
        if (stringHasValue(afterColumn)) {
            htmlGroupContainerConfiguration.setAfterColumn(afterColumn);
        }
        String noBorder = attributes.getProperty("noBorder");
        if (stringHasValue(noBorder)) {
            htmlGroupContainerConfiguration.setNoBorder(Boolean.parseBoolean(noBorder));
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
                    parseProperty(htmlGroupContainerConfiguration, childNode);
                    break;
                case "groupContainer":
                    HtmlGroupContainerConfiguration subGroupContainerConfiguration = parseHtmlGroupContainerConfiguration(childNode);
                    if (subGroupContainerConfiguration != null) {
                        htmlGroupContainerConfiguration.getGroupContainerConfigurations().add(subGroupContainerConfiguration);
                    }
                    break;
            }
        }
        return htmlGroupContainerConfiguration;
    }

    protected void parseHtmlGeneratorProperty(HtmlGeneratorConfiguration htmlGeneratorConfiguration, Node node) {
        Properties properties = parseAttributes(node);
        String propertyName = properties.get("name").toString();
        switch (propertyName) {
            case PropertyRegistry.ANY_HTML_HIDDEN_COLUMNS:
                String htmlHiddenColumns = properties.get("value").toString();
                if (stringHasValue(htmlHiddenColumns)) {
                    htmlGeneratorConfiguration.getHiddenColumnNames().addAll(splitToList(htmlHiddenColumns));
                }
                break;
            case PropertyRegistry.ELEMENT_REQUIRED_COLUMNS:
                String required = properties.get("value").toString();
                if (stringHasValue(required)) {
                    htmlGeneratorConfiguration.getElementRequired().addAll(splitToList(required));
                }
                break;
            case PropertyRegistry.ANY_HTML_READONLY_FIELDS:
                String readonly = properties.get("value").toString();
                if (stringHasValue(readonly)) {
                    htmlGeneratorConfiguration.getReadonlyFields().addAll(splitToList(readonly));
                }
                break;
            case PropertyRegistry.ANY_HTML_DISPLAY_ONLY_FIELDS:
                String displayOnly = properties.get("value").toString();
                if (stringHasValue(displayOnly)) {
                    htmlGeneratorConfiguration.getDisplayOnlyFields().addAll(splitToList(displayOnly));
                }
                break;
        }
    }

    protected HtmlElementDescriptor parseHtmlElementDescriptor(Node node) {
        Properties attributes = parseAttributes(node);
        String column = attributes.getProperty("column");
        String tagType = attributes.getProperty("tagType");
        HtmlElementDescriptor htmlElementDescriptor = new HtmlElementDescriptor();
        htmlElementDescriptor.setName(column);
        htmlElementDescriptor.setTagType(tagType);
        String dataSource = attributes.getProperty("dataSource");
        htmlElementDescriptor.setDataSource(dataSource);
        String dataUrl = attributes.getProperty("dataUrl");
        htmlElementDescriptor.setDataUrl(dataUrl);
        String dataFormat = attributes.getProperty("dataFormat");
        htmlElementDescriptor.setDataFormat(dataFormat);
        String otherFieldName = attributes.getProperty("otherFieldName");
        if (stringHasValue(otherFieldName)) {
            htmlElementDescriptor.setOtherFieldName(otherFieldName);
        }
        String beanName = attributes.getProperty("beanName");
        htmlElementDescriptor.setBeanName(beanName);
        String applyProperty = attributes.getProperty("applyProperty");
        htmlElementDescriptor.setApplyProperty(applyProperty);
        String applyPropertyKey = attributes.getProperty("applyPropertyKey");
        if (VStringUtil.stringHasValue(applyPropertyKey)) {
            htmlElementDescriptor.setApplyPropertyKey(applyPropertyKey);
        }
        String verify = attributes.getProperty("verify");
        if (stringHasValue(verify)) {
            htmlElementDescriptor.setVerify(splitToList(verify));
        }
        String enumClassName = attributes.getProperty(PropertyRegistry.ELEMENT_ENUM_CLASS_FULL_NAME);
        if (stringHasValue(enumClassName)) {
            htmlElementDescriptor.setEnumClassName(enumClassName);
        }
        String switchText = attributes.getProperty(PropertyRegistry.ELEMENT_SWITCH_TEXT);
        if (stringHasValue(switchText)) {
            htmlElementDescriptor.setSwitchText(switchText);
        }
        String dictCode = attributes.getProperty(PropertyRegistry.ELEMENT_DICT_CODE);
        if (stringHasValue(dictCode)) {
            htmlElementDescriptor.setDictCode(dictCode);
        }
        String callback = attributes.getProperty("callback");
        if (stringHasValue(callback)) {
            htmlElementDescriptor.setCallback(callback);
        }
        String labelCss = attributes.getProperty("labelCss");
        if (stringHasValue(labelCss)) {
            htmlElementDescriptor.setLabelCss(labelCss);
        }
        String elementCss = attributes.getProperty("elementCss");
        if (stringHasValue(elementCss)) {
            htmlElementDescriptor.setElementCss(elementCss);
        }
        String dateFmt = attributes.getProperty("dateFmt");
        if (stringHasValue(dateFmt)) {
            htmlElementDescriptor.setDateFmt(dateFmt);
        }
        String dateRange = attributes.getProperty("dateRange");
        if (stringHasValue(dateRange) && "true".equals(dateRange)) {
            htmlElementDescriptor.setDateRange(true);
        }
        String listKey = attributes.getProperty("listKey");
        if (stringHasValue(listKey)) {
            htmlElementDescriptor.setListKey(listKey);
        }
        String listViewClass = attributes.getProperty("listViewClass");
        if (stringHasValue(listViewClass)) {
            htmlElementDescriptor.setListViewClass(listViewClass);
        }

        String multiple = attributes.getProperty("multiple");
        if (stringHasValue(multiple)) {
            htmlElementDescriptor.setMultiple(multiple);
        }
        String remoteApiParse = attributes.getProperty("remoteApiParse");
        if (stringHasValue(remoteApiParse) && "true".equals(remoteApiParse)) {
            htmlElementDescriptor.setRemoteApiParse(true);
        }
        String remoteToTree = attributes.getProperty("remoteToTree");
        if (stringHasValue(remoteToTree) && "true".equalsIgnoreCase(remoteToTree)) {
            htmlElementDescriptor.setRemoteToTree(true);
        }
        String remoteAsync = attributes.getProperty("remoteAsync");
        if (stringHasValue(remoteAsync) && "true".equalsIgnoreCase(remoteAsync)) {
            htmlElementDescriptor.setRemoteAsync(true);
        }

        String keyMapLabel = attributes.getProperty("keyMapLabel");
        if (stringHasValue(keyMapLabel)) {
            htmlElementDescriptor.setKeyMapLabel(keyMapLabel);
        }
        String keyMapValue = attributes.getProperty("keyMapValue");
        if (stringHasValue(keyMapValue)) {
            htmlElementDescriptor.setKeyMapValue(keyMapValue);
        }
        String parentFormKey = attributes.getProperty("parentFormKey");
        if (stringHasValue(parentFormKey)) {
            htmlElementDescriptor.setParentFormKey(parentFormKey);
        }
        String designIdField = attributes.getProperty("designIdField");
        if (stringHasValue(designIdField)) {
            htmlElementDescriptor.setDesignIdField(designIdField);
        }
        String designRestBasePath = attributes.getProperty("designRestBasePath");
        if (stringHasValue(designRestBasePath)) {
            htmlElementDescriptor.setDataUrl(designRestBasePath);
        }
        String configJsonfield = attributes.getProperty("configJsonfield");
        if (stringHasValue(configJsonfield)) {
            htmlElementDescriptor.setConfigJsonfield(configJsonfield);
        }
        String enablePager = attributes.getProperty("enablePager");
        if (stringHasValue(enablePager)) {
            htmlElementDescriptor.setEnablePager(Boolean.parseBoolean(enablePager));
        }
        String vxeListButtons = attributes.getProperty("vxeListButtons");
        if (stringHasValue(vxeListButtons)) {
            htmlElementDescriptor.setVxeListButtons(splitToSet(vxeListButtons));
        }
        String defaultFilterExpr = attributes.getProperty("defaultFilterExpr");
        if (stringHasValue(defaultFilterExpr)) {
            htmlElementDescriptor.setDefaultFilterExpr(defaultFilterExpr);
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
                    parseProperty(htmlElementDescriptor, childNode);
                    break;
                case "hrefElement":
                    HtmlHrefElementConfiguration htmlHrefElementConfiguration = parseHtmlHrefElementConfiguration(childNode);
                    if (htmlHrefElementConfiguration != null) {
                        htmlElementDescriptor.getHtmlHrefElementConfigurations().add(htmlHrefElementConfiguration);
                    }
                    break;
            }
        }
        return htmlElementDescriptor;
    }

    private HtmlHrefElementConfiguration parseHtmlHrefElementConfiguration(Node node) {
        Properties attributes = parseAttributes(node);
        String href = attributes.getProperty("href");
        if (!stringHasValue(href)) {
            return null;
        }
        HtmlHrefElementConfiguration htmlHrefElementConfiguration = new HtmlHrefElementConfiguration();
        htmlHrefElementConfiguration.setHref(href);
        String target = attributes.getProperty("target");
        if (stringHasValue(target)) {
            htmlHrefElementConfiguration.setTarget(target);
        }
        String text = attributes.getProperty("text");
        if (stringHasValue(text)) {
            htmlHrefElementConfiguration.setText(text);
        }
        String icon = attributes.getProperty("icon");
        if (stringHasValue(icon)) {
            htmlHrefElementConfiguration.setIcon(icon);
        }
        String type = attributes.getProperty("type");
        if (stringHasValue(type)) {
            htmlHrefElementConfiguration.setType(type);
        }
        String title = attributes.getProperty("title");
        if (stringHasValue(title)) {
            htmlHrefElementConfiguration.setTitle(title);
        }
        String keySelector = attributes.getProperty("keySelector");
        if (stringHasValue(keySelector)) {
            htmlHrefElementConfiguration.setKeySelector(keySelector);
        }
        return htmlHrefElementConfiguration;
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
        String baseTargetPackage = substringBeforeLast(targetPackage, ".");
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
            javaServiceGeneratorConfiguration.setTargetProject(getTargetProject(targetProject));
            javaServiceImplGeneratorConfiguration.setTargetProject(getTargetProject(targetProject));
        }
        parseAbstractConfigAttributes(attributes, javaServiceGeneratorConfiguration, node);
        parseAbstractConfigAttributes(attributes, javaServiceImplGeneratorConfiguration, node);
        //更新javaServiceImplGeneratorConfiguration的entityEvent
        String entityEvent = javaServiceImplGeneratorConfiguration.getProperty("entityEvent");
        if (stringHasValue(entityEvent)) {
            javaServiceImplGeneratorConfiguration.setEntityEvent(splitToSet(entityEvent));
        }

    }

    protected void parseGenerateController(Context context, TableConfiguration tc, Node node) {
        Properties attributes = parseAttributes(node);

        JavaControllerGeneratorConfiguration javaControllerGeneratorConfiguration = new JavaControllerGeneratorConfiguration(context, tc);

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

            switch (childNode.getNodeName()) {
                case "property":
                    parseProperty(javaControllerGeneratorConfiguration, childNode);
                    break;
                case "generateOptions":
                    parseGenerateOptions(javaControllerGeneratorConfiguration, childNode);
                    break;
                case "generateTreeViewCate":
                    parseGenerateTreeViewCate(javaControllerGeneratorConfiguration, childNode);
                    break;
            }
        }

        tc.setJavaControllerGeneratorConfiguration(javaControllerGeneratorConfiguration);
    }

    private void parseGenerateTreeViewCate(JavaControllerGeneratorConfiguration javaControllerGeneratorConfiguration, Node node) {
        TreeViewCateGeneratorConfiguration viewCateGeneratorConfiguration = new TreeViewCateGeneratorConfiguration();
        Properties attributes = parseAttributes(node);
        String expression = attributes.getProperty(PropertyRegistry.ELEMENT_SPEL_EXPRESSION);
        if (stringHasValue(expression)) {
            viewCateGeneratorConfiguration.setSPeL(expression);
        }
        String pathKeyWord = attributes.getProperty(PropertyRegistry.ELEMENT_PATH_KEYWORD);
        if (stringHasValue(pathKeyWord)) {
            viewCateGeneratorConfiguration.setPathKeyWord(pathKeyWord);
        }
        String idColumn = attributes.getProperty(PropertyRegistry.ELEMENT_ID_PROPERTY);
        if (stringHasValue(idColumn)) {
            viewCateGeneratorConfiguration.setIdProperty(idColumn);
        }
        String nameColumn = attributes.getProperty(PropertyRegistry.ELEMENT_NAME_PROPERTY);
        if (stringHasValue(nameColumn)) {
            viewCateGeneratorConfiguration.setNameProperty(nameColumn);
        }
        if (javaControllerGeneratorConfiguration.getTreeViewCateGeneratorConfigurations()
                .stream()
                .noneMatch(config -> config.getPathKeyWord().equals(viewCateGeneratorConfiguration.getPathKeyWord()))) {
            javaControllerGeneratorConfiguration.getTreeViewCateGeneratorConfigurations().add(viewCateGeneratorConfiguration);
        }
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

    private void parseVO(Context context, TableConfiguration tc, Node node) {
        VOGeneratorConfiguration configuration = new VOGeneratorConfiguration(context, tc);
        Properties attributes = parseAttributes(node);
        String generate = attributes.getProperty(PropertyRegistry.ANY_GENERATE);
        configuration.setGenerate(Boolean.parseBoolean(generate));
        String columnsName = attributes.getProperty(PropertyRegistry.ELEMENT_EXCLUDE_COLUMNS);
        if (stringHasValue(columnsName)) {
            configuration.setExcludeColumns(splitToSet(columnsName));
        }

        String property = attributes.getProperty(PropertyRegistry.ELEMENT_VALIDATE_IGNORE_COLUMNS);
        if (stringHasValue(property)) {
            configuration.setValidateIgnoreColumns(splitToSet(property));
        }

        String ehAttr = attributes.getProperty(PropertyRegistry.ANY_EQUALS_AND_HASH_CODE);
        if (stringHasValue(ehAttr)) {
            configuration.setEqualsAndHashCodeColumns(splitToList(ehAttr));
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
                    configuration.addOverrideColumnConfigurations(parseVoOverrideColumn(context, tc, childNode));
                    break;
                case ("additionalProperty"):
                    configuration.addAdditionalPropertyConfigurations(parseAdditionalProperty(context, tc, childNode));
                    break;
                case "modelVO":
                    parseVOModel(context, tc, childNode, configuration);
                    break;
                case "createVO":
                    parseVOCreate(context, tc, childNode, configuration);
                    break;
                case "updateVO":
                    parseVOUpdate(context, tc, childNode, configuration);
                    break;
                case "viewVO":
                    parseVOView(context, tc, childNode, configuration);
                    break;
                case "excelVO":
                    parseVOExcel(context, tc, childNode, configuration);
                    break;
                case "requestVO":
                    parseVORequest(context, tc, childNode, configuration);
                    break;
            }
        }
        //继承table的附件属性
        configuration.getAdditionalPropertyConfigurations().addAll(tc.getAdditionalPropertyConfigurations());
        configuration.getOverridePropertyConfigurations().addAll(tc.getJavaModelGeneratorConfiguration().getOverridePropertyConfigurations());
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
            mapstructMappingConfiguration.setSourceArguments(splitToList(sourceArguments));
        }
        String targetArguments = attributes.getProperty("targetArguments");
        if (stringHasValue(targetArguments)) {
            mapstructMappingConfiguration.setTargetArguments(splitToList(targetArguments));
        }
        configuration.addMappingConfigurations(mapstructMappingConfiguration);
    }

    private OverridePropertyValueGeneratorConfiguration parseVoOverrideColumn(Context context, TableConfiguration tc, Node node) {
        Properties attributes = parseAttributes(node);
        String sourceColumn = attributes.getProperty(PropertyRegistry.ELEMENT_SOURCE_COLUMN);
        OverridePropertyValueGeneratorConfiguration overrideColumnGeneratorConfiguration = new OverridePropertyValueGeneratorConfiguration(context, tc, sourceColumn);
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

        String initializationString = attributes.getProperty(PropertyRegistry.ELEMENT_INITIALIZATION_STRING);
        if (stringHasValue(initializationString)) {
            overrideColumnGeneratorConfiguration.setInitializationString(initializationString);
        }

        String importType = attributes.getProperty(PropertyRegistry.ELEMENT_IMPORT_TYPE);
        if (stringHasValue(importType)) {
            overrideColumnGeneratorConfiguration.addImportTypes(importType);
        }

        String remark = attributes.getProperty(PropertyRegistry.ELEMENT_FIELD_REMARK);
        if (stringHasValue(remark)) {
            overrideColumnGeneratorConfiguration.setRemark(remark);
        }
        String enumClassName = attributes.getProperty(PropertyRegistry.ELEMENT_ENUM_CLASS_FULL_NAME);
        if (stringHasValue(enumClassName)) {
            overrideColumnGeneratorConfiguration.setEnumClassName(enumClassName);
        }
        if (stringHasValue(sourceColumn)) {
            if (annotationType.equals("Dict")) {
                if (stringHasValue(beanName)) {
                    return overrideColumnGeneratorConfiguration;
                }
            } else {
                return overrideColumnGeneratorConfiguration;
            }
        }
        return null;
    }

    private VoAdditionalPropertyGeneratorConfiguration parseAdditionalProperty(Context context, TableConfiguration tc, Node childNode) {
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
                voAdditionalPropertyConfiguration.setTypeArguments(splitToList(typeArguments));
            }
            String annotations = attributes.getProperty("annotations");
            if (stringHasValue(annotations)) {
                voAdditionalPropertyConfiguration.setAnnotations(new ArrayList<>(Arrays.asList(annotations.split("\\|"))));
            }
            String initializationString = attributes.getProperty(PropertyRegistry.ELEMENT_INITIALIZATION_STRING);
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
            String importedType = attributes.getProperty("importedType");
            if (stringHasValue(importedType)) {
                voAdditionalPropertyConfiguration.getImportedTypes().add(importedType);
            }
            String importedTypes = attributes.getProperty("importedTypes");
            if (stringHasValue(importedTypes)) {
                voAdditionalPropertyConfiguration.getImportedTypes().addAll(splitToList(importedTypes));
            }
            String remark = attributes.getProperty(PropertyRegistry.ELEMENT_FIELD_REMARK);
            if (stringHasValue(remark)) {
                voAdditionalPropertyConfiguration.setRemark(remark);
            }
            return voAdditionalPropertyConfiguration;
        }
        return null;

    }

    private void parseNameFragment(Context context, TableConfiguration tc, Node childNode, AbstractModelGeneratorConfiguration configuration) {
        Properties attributes = parseAttributes(childNode);
        List<String> columnNames = splitToList(attributes.getProperty("column"));
        String fragment = attributes.getProperty("fragment");
        columnNames.forEach(columnName -> {
            VoNameFragmentGeneratorConfiguration voNameFragmentGeneratorConfiguration = new VoNameFragmentGeneratorConfiguration(context, tc);
            voNameFragmentGeneratorConfiguration.setColumn(columnName);
            if (stringHasValue(fragment)) {
                voNameFragmentGeneratorConfiguration.setFragment(fragment);
            }
            configuration.addVoNameFragmentGeneratorConfiguration(voNameFragmentGeneratorConfiguration);
        });
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
                    configuration.addOverrideColumnConfigurations(parseVoOverrideColumn(context, tc, childNode));
                    break;
                case ("additionalProperty"):
                    configuration.addAdditionalPropertyConfigurations(parseAdditionalProperty(context, tc, childNode));
                    break;
                case ("nameFragment"):
                    parseNameFragment(context, tc, childNode, configuration);
                    break;
            }
        }
    }

    private void parseInnerListView(TableConfiguration tc, Node node, VOViewGeneratorConfiguration configuration) {
        InnerListViewConfiguration innerListViewGeneratorConfiguration = new InnerListViewConfiguration();
        Properties attributes = parseAttributes(node);
        parseTableListCommon(attributes, innerListViewGeneratorConfiguration);
        String height = attributes.getProperty("height");
        if (stringHasValue(height)) {
            innerListViewGeneratorConfiguration.setHeight(height);
        }
        String width = attributes.getProperty("width");
        if (stringHasValue(width)) {
            innerListViewGeneratorConfiguration.setWidth(width);
        }
        String evenRow = attributes.getProperty("evenRow");
        if (stringHasValue(evenRow)) {
            innerListViewGeneratorConfiguration.setEven(Boolean.parseBoolean(evenRow));
        }
        String editExtendsForm = attributes.getProperty("editExtendsForm");
        if (stringHasValue(editExtendsForm)) {
            if (!editExtendsForm.equals("none")) {
                innerListViewGeneratorConfiguration.setEditExtendsForm(editExtendsForm);
            }
        } else {
            if (!tc.getHtmlMapGeneratorConfigurations().isEmpty()) {
                String simpleViewPath = tc.getHtmlMapGeneratorConfigurations().get(0).getSimpleViewPath();
                String fileName = substringAfterLast(substringAfterLast(simpleViewPath, "/"), "\\");
                innerListViewGeneratorConfiguration.setEditExtendsForm(fileName);
            }
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
                    parseProperty(innerListViewGeneratorConfiguration, childNode);
                    break;
                case PropertyRegistry.ELEMENT_HTML_ELEMENT_DESCRIPTOR:
                    HtmlElementDescriptor htmlElementDescriptor = parseHtmlElementDescriptor(childNode);
                    if (htmlElementDescriptor != null) {
                        innerListViewGeneratorConfiguration.getHtmlElements().add(htmlElementDescriptor);
                    }
                    break;
                case "listColumnDescriptor":
                    ListColumnConfiguration listColumnDescriptor = parseListColumnDescriptor(childNode);
                    innerListViewGeneratorConfiguration.getListColumnConfigurations().add(listColumnDescriptor);
                    break;
                case (PropertyRegistry.ELEMENT_HTML_BUTTON):
                    HtmlButtonGeneratorConfiguration htmlButton = parseHtmlButton(childNode);
                    innerListViewGeneratorConfiguration.getHtmlButtons().add(htmlButton);
                    break;
                case ("queryColumn"):
                    QueryColumnConfiguration queryColumnConfiguration = parseQueryColumn(tc, childNode);
                    innerListViewGeneratorConfiguration.getQueryColumnConfigurations().add(queryColumnConfiguration);
                    break;
            }
        }
        //根据属性赋值
        String htmlHiddenFields = innerListViewGeneratorConfiguration.getProperty("htmlHiddenFields");
        if (stringHasValue(htmlHiddenFields)) {
            innerListViewGeneratorConfiguration.getDefaultHiddenFields().addAll(splitToSet(htmlHiddenFields));
        }
        String htmlReadonlyFields = innerListViewGeneratorConfiguration.getProperty(PropertyRegistry.ANY_HTML_READONLY_FIELDS);
        if (stringHasValue(htmlReadonlyFields)) {
            innerListViewGeneratorConfiguration.getReadonlyFields().addAll(splitToList(htmlReadonlyFields));
        }
        String requiredColumns = innerListViewGeneratorConfiguration.getProperty(PropertyRegistry.ELEMENT_REQUIRED_COLUMNS);
        if (stringHasValue(requiredColumns)) {
            innerListViewGeneratorConfiguration.getRequiredColumns().addAll(splitToList(requiredColumns));
        }
        configuration.getInnerListViewConfigurations().add(innerListViewGeneratorConfiguration);
    }

    private ListColumnConfiguration parseListColumnDescriptor(Node node) {
        Properties attributes = parseAttributes(node);
        ListColumnConfiguration listColumnConfiguration = new ListColumnConfiguration();
        String field = attributes.getProperty("field");
        if (stringHasValue(field)) {
            listColumnConfiguration.setField(field);
        }
        String width = attributes.getProperty("width");
        if (stringHasValue(width)) {
            listColumnConfiguration.setWidth(width);
        }
        String minWidth = attributes.getProperty("minWidth");
        if (stringHasValue(minWidth)) {
            listColumnConfiguration.setMinWidth(Integer.parseInt(minWidth));
        }
        String fixed = attributes.getProperty("fixed");
        if (stringHasValue(fixed)) {
            listColumnConfiguration.setFixed(fixed);
        }
        String templet = attributes.getProperty("templet");
        if (stringHasValue(templet)) {
            listColumnConfiguration.setTemplet(templet);
        }
        String style = attributes.getProperty("style");
        if (stringHasValue(style)) {
            listColumnConfiguration.setStyle(style);
        }
        String align = attributes.getProperty("align");
        if (stringHasValue(align)) {
            listColumnConfiguration.setAlign(align);
        }
        parseChildNodeOnlyProperty(listColumnConfiguration, node);
        return listColumnConfiguration;
    }

    private void parseColumnRenderFun(Context context, TableConfiguration tc, AbstractModelGeneratorConfiguration configuration, Node childNode) {
        VoColumnRenderFunGeneratorConfiguration voColumnRenderFunGeneratorConfiguration = new VoColumnRenderFunGeneratorConfiguration(context, tc);
        Properties attributes = parseAttributes(childNode);
        String fieldNames = attributes.getProperty(PropertyRegistry.ELEMENT_FIELD_NAMES);
        if (stringHasValue(fieldNames)) {
            voColumnRenderFunGeneratorConfiguration.setFieldNames(splitToList(fieldNames));
        }
        String renderFun = attributes.getProperty(PropertyRegistry.ELEMENT_RENDER_FUN);
        if (stringHasValue(renderFun)) {
            if (PropertyRegistry.ELEMENT_EXTEND_FUNC_OTHER.equals(renderFun)) {
                String extendFuncOther = attributes.getProperty(PropertyRegistry.ELEMENT_EXTEND_FUNC_OTHER);
                if (stringHasValue(extendFuncOther)) {
                    voColumnRenderFunGeneratorConfiguration.setRenderFun(extendFuncOther);
                } else {
                    voColumnRenderFunGeneratorConfiguration.setRenderFun(renderFun);
                }
            } else {
                voColumnRenderFunGeneratorConfiguration.setRenderFun(renderFun);
            }
        }
        configuration.addVoColumnRenderFunGeneratorConfiguration(voColumnRenderFunGeneratorConfiguration);
    }


    private void parseVOModel(Context context, TableConfiguration tc, Node node, VOGeneratorConfiguration voGeneratorConfiguration) {
        Properties attributes = parseAttributes(node);
        VOModelGeneratorConfiguration vOModelGeneratorConfiguration = new VOModelGeneratorConfiguration(context, tc);
        parseColumnsList(attributes, vOModelGeneratorConfiguration, voGeneratorConfiguration);
        String includeColumns = attributes.getProperty(PropertyRegistry.ELEMENT_INCLUDE_COLUMNS);
        if (stringHasValue(includeColumns)) {
            vOModelGeneratorConfiguration.setIncludeColumns(splitToSet(includeColumns));
        }
        String excludeColumns = attributes.getProperty(PropertyRegistry.ELEMENT_EXCLUDE_COLUMNS);
        if (stringHasValue(excludeColumns)) {
            vOModelGeneratorConfiguration.setExcludeColumns(splitToSet(excludeColumns));
        }
        //EqualsAndHashCodeColumns
        String ehAttr = attributes.getProperty(PropertyRegistry.ANY_EQUALS_AND_HASH_CODE);
        if (stringHasValue(ehAttr)) {
            vOModelGeneratorConfiguration.setEqualsAndHashCodeColumns(splitToList(ehAttr));
        } else {
            vOModelGeneratorConfiguration.getEqualsAndHashCodeColumns().addAll(voGeneratorConfiguration.getEqualsAndHashCodeColumns());
        }
        if (!vOModelGeneratorConfiguration.getEqualsAndHashCodeColumns().isEmpty()) {
            List<String> distinct = vOModelGeneratorConfiguration.getEqualsAndHashCodeColumns().stream().distinct().collect(Collectors.toList());
            vOModelGeneratorConfiguration.setEqualsAndHashCodeColumns(distinct);
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
        //继承vo的附加属性
        vOModelGeneratorConfiguration.getAdditionalPropertyConfigurations().addAll(voGeneratorConfiguration.getAdditionalPropertyConfigurations());
        //添加到vo配置
        voGeneratorConfiguration.setVoModelConfiguration(vOModelGeneratorConfiguration);
    }

    private void parseVOCreate(Context context, TableConfiguration tc, Node node, VOGeneratorConfiguration voGeneratorConfiguration) {
        Properties attributes = parseAttributes(node);
        VOCreateGeneratorConfiguration configuration = new VOCreateGeneratorConfiguration(context, tc);
        parseColumnsList(attributes, configuration, voGeneratorConfiguration);
        String includeColumns = attributes.getProperty(PropertyRegistry.ELEMENT_INCLUDE_COLUMNS);
        if (stringHasValue(includeColumns)) {
            configuration.setIncludeColumns(splitToSet(includeColumns));
        }
        String requiredColumns = attributes.getProperty(PropertyRegistry.ELEMENT_REQUIRED_COLUMNS);
        if (stringHasValue(requiredColumns)) {
            configuration.setRequiredColumns(splitToSet(requiredColumns));
        }

        Set<String> validateIgnoreColumns = splitToSet(attributes.getProperty(PropertyRegistry.ELEMENT_VALIDATE_IGNORE_COLUMNS));
        if (validateIgnoreColumns.isEmpty()) {
            validateIgnoreColumns.add(DefaultColumnNameEnum.ID.columnName());
        }

        String isSelective = attributes.getProperty(PropertyRegistry.ELEMENT_ENABLE_SELECTIVE);
        if (stringHasValue(isSelective)) {
            configuration.setEnableSelective(Boolean.parseBoolean(isSelective));
        }

        //EqualsAndHashCodeColumns
        String ehAttr = attributes.getProperty(PropertyRegistry.ANY_EQUALS_AND_HASH_CODE);
        if (stringHasValue(ehAttr)) {
            configuration.setEqualsAndHashCodeColumns(splitToList(ehAttr));
        } else {
            configuration.getEqualsAndHashCodeColumns().addAll(voGeneratorConfiguration.getEqualsAndHashCodeColumns());
        }
        if (!configuration.getEqualsAndHashCodeColumns().isEmpty()) {
            List<String> distinct = configuration.getEqualsAndHashCodeColumns().stream().distinct().collect(Collectors.toList());
            configuration.setEqualsAndHashCodeColumns(distinct);
        }

        validateIgnoreColumns.addAll(tc.getValidateIgnoreColumns());
        configuration.setValidateIgnoreColumns(validateIgnoreColumns);

        parseModelChildNodeProperty(context, tc, node, configuration);
        //继承vo的附加属性
        configuration.getAdditionalPropertyConfigurations().addAll(voGeneratorConfiguration.getAdditionalPropertyConfigurations());
        //添加到vo配置
        voGeneratorConfiguration.setVoCreateConfiguration(configuration);
    }

    private void parseVOUpdate(Context context, TableConfiguration tc, Node node, VOGeneratorConfiguration voGeneratorConfiguration) {
        Properties attributes = parseAttributes(node);
        VOUpdateGeneratorConfiguration configuration = new VOUpdateGeneratorConfiguration(context, tc);
        parseColumnsList(attributes, configuration, voGeneratorConfiguration);
        String includeColumns = attributes.getProperty(PropertyRegistry.ELEMENT_INCLUDE_COLUMNS);
        if (stringHasValue(includeColumns)) {
            configuration.setIncludeColumns(splitToSet(includeColumns));
        }
        String requiredColumns = attributes.getProperty(PropertyRegistry.ELEMENT_REQUIRED_COLUMNS);
        if (stringHasValue(requiredColumns)) {
            configuration.setRequiredColumns(splitToList(requiredColumns));
        }

        Set<String> validateIgnoreColumns = splitToSet(attributes.getProperty(PropertyRegistry.ELEMENT_VALIDATE_IGNORE_COLUMNS));
        validateIgnoreColumns.addAll(tc.getValidateIgnoreColumns());
        configuration.setValidateIgnoreColumns(validateIgnoreColumns);

        String isSelective = attributes.getProperty(PropertyRegistry.ELEMENT_ENABLE_SELECTIVE);
        if (stringHasValue(isSelective)) {
            configuration.setEnableSelective(Boolean.parseBoolean(isSelective));
        }

        //EqualsAndHashCodeColumns
        String ehAttr = attributes.getProperty(PropertyRegistry.ANY_EQUALS_AND_HASH_CODE);
        if (stringHasValue(ehAttr)) {
            configuration.setEqualsAndHashCodeColumns(splitToList(ehAttr));
        } else {
            configuration.getEqualsAndHashCodeColumns().addAll(voGeneratorConfiguration.getEqualsAndHashCodeColumns());
        }
        if (!configuration.getEqualsAndHashCodeColumns().isEmpty()) {
            List<String> distinct = configuration.getEqualsAndHashCodeColumns().stream().distinct().collect(Collectors.toList());
            configuration.setEqualsAndHashCodeColumns(distinct);
        }

        parseModelChildNodeProperty(context, tc, node, configuration);
        //继承vo的附加属性
        configuration.getAdditionalPropertyConfigurations().addAll(voGeneratorConfiguration.getAdditionalPropertyConfigurations());
        //添加到vo配置
        voGeneratorConfiguration.setVoUpdateConfiguration(configuration);
    }

    private void parseVOExcel(Context context, TableConfiguration tc, Node node, VOGeneratorConfiguration voGeneratorConfiguration) {
        Properties attributes = parseAttributes(node);
        VOExcelGeneratorConfiguration vOExcelGeneratorConfiguration = new VOExcelGeneratorConfiguration(context, tc);
        parseColumnsList(attributes, vOExcelGeneratorConfiguration, voGeneratorConfiguration);
        String includeFields = attributes.getProperty(PropertyRegistry.ELEMENT_INCLUDE_FIELDS);
        if (stringHasValue(includeFields)) {
            vOExcelGeneratorConfiguration.setExportIncludeFields(splitToList(includeFields));
        }
        String excludeFields = attributes.getProperty(PropertyRegistry.ELEMENT_EXCLUDE_FIELDS);
        if (stringHasValue(excludeFields)) {
            vOExcelGeneratorConfiguration.setExportExcludeFields(splitToSet(excludeFields));
        }
        String ignoreFields = attributes.getProperty(PropertyRegistry.ELEMENT_IGNORE_FIELDS);
        if (stringHasValue(ignoreFields)) {
            vOExcelGeneratorConfiguration.setExportIgnoreFields(splitToSet(ignoreFields));
        }

        String importInclude = attributes.getProperty(PropertyRegistry.ELEMENT_IMPORT_INCLUDE_COLUMNS);
        if (stringHasValue(importInclude)) {
            vOExcelGeneratorConfiguration.setImportIncludeColumns(splitToSet(importInclude));
        }
        String importExclude = attributes.getProperty(PropertyRegistry.ELEMENT_IMPORT_EXCLUDE_COLUMNS);
        if (stringHasValue(importExclude)) {
            vOExcelGeneratorConfiguration.setImportExcludeColumns(splitToSet(importExclude));
        }
        String importIgnoreFields = attributes.getProperty(PropertyRegistry.ELEMENT_IMPORT_IGNORE_FIELDS);
        if (stringHasValue(importIgnoreFields)) {
            vOExcelGeneratorConfiguration.setImportIgnoreFields(splitToSet(importIgnoreFields));
        }

        //EqualsAndHashCodeColumns
        String ehAttr = attributes.getProperty(PropertyRegistry.ANY_EQUALS_AND_HASH_CODE);
        if (stringHasValue(ehAttr)) {
            vOExcelGeneratorConfiguration.setEqualsAndHashCodeColumns(splitToList(ehAttr));
        } else {
            vOExcelGeneratorConfiguration.getEqualsAndHashCodeColumns().addAll(voGeneratorConfiguration.getEqualsAndHashCodeColumns());
        }
        if (!vOExcelGeneratorConfiguration.getEqualsAndHashCodeColumns().isEmpty()) {
            List<String> distinct = vOExcelGeneratorConfiguration.getEqualsAndHashCodeColumns().stream().distinct().collect(Collectors.toList());
            vOExcelGeneratorConfiguration.setEqualsAndHashCodeColumns(distinct);
        }

        parseModelChildNodeProperty(context, tc, node, vOExcelGeneratorConfiguration);
        //继承vo的附加属性
        vOExcelGeneratorConfiguration.getAdditionalPropertyConfigurations().addAll(voGeneratorConfiguration.getAdditionalPropertyConfigurations());
        //添加到vo配置
        voGeneratorConfiguration.setVoExcelConfiguration(vOExcelGeneratorConfiguration);
    }

    private void parseVOView(Context context, TableConfiguration tc, Node node, VOGeneratorConfiguration voGeneratorConfiguration) {
        VOViewGeneratorConfiguration voViewGeneratorConfiguration = new VOViewGeneratorConfiguration(context, tc);
        Properties attributes = parseAttributes(node);
        parseColumnsList(attributes, voViewGeneratorConfiguration, voGeneratorConfiguration);
        parseTableListCommon(attributes, voViewGeneratorConfiguration);
        String includeColumns = attributes.getProperty(PropertyRegistry.ELEMENT_INCLUDE_COLUMNS);
        if (stringHasValue(includeColumns)) {
            voViewGeneratorConfiguration.setIncludeColumns(splitToSet(includeColumns));
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
            voViewGeneratorConfiguration.setEqualsAndHashCodeColumns(splitToList(equalsHashCode));
        } else {
            voViewGeneratorConfiguration.getEqualsAndHashCodeColumns().addAll(voGeneratorConfiguration.getEqualsAndHashCodeColumns());
        }
        if (!voViewGeneratorConfiguration.getEqualsAndHashCodeColumns().isEmpty()) {
            List<String> distinct = voViewGeneratorConfiguration.getEqualsAndHashCodeColumns().stream().distinct().collect(Collectors.toList());
            voViewGeneratorConfiguration.setEqualsAndHashCodeColumns(distinct);
        }

        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);

            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            switch (childNode.getNodeName()) {
                case "property":  //$NON-NLS-1$
                    parseProperty(voViewGeneratorConfiguration, childNode);
                    break;
                case ("overridePropertyValue"):
                    voViewGeneratorConfiguration.addOverrideColumnConfigurations(parseVoOverrideColumn(context, tc, childNode));
                    break;
                case ("additionalProperty"):
                    //继承vo的附件属性
                    voViewGeneratorConfiguration.addAdditionalPropertyConfigurations(parseAdditionalProperty(context, tc, childNode));
                    break;
                case ("columnRenderFun"):
                    parseColumnRenderFun(context, tc, voViewGeneratorConfiguration, childNode);
                    break;
                case ("innerListView"):
                    parseInnerListView(tc, childNode, voViewGeneratorConfiguration);
                    break;
                case (PropertyRegistry.ELEMENT_HTML_BUTTON):
                    HtmlButtonGeneratorConfiguration htmlButton = parseHtmlButton(childNode);
                    voViewGeneratorConfiguration.getHtmlButtons().add(htmlButton);
                    break;
                case ("queryColumn"):
                    QueryColumnConfiguration queryColumnConfiguration = parseQueryColumn(tc, childNode);
                    voViewGeneratorConfiguration.addQueryColumnConfigurations(queryColumnConfiguration);
                    break;
                case ("fieldOverrides"):
                    ViewFieldOverrideConfiguration viewFieldOverrideConfiguration = parseFieldOverrides(context, tc, childNode, voViewGeneratorConfiguration);
                    voViewGeneratorConfiguration.addViewFieldOverrideConfiguration(viewFieldOverrideConfiguration);
                    break;
            }
        }
        //继承vo的附件属性
        voViewGeneratorConfiguration.getAdditionalPropertyConfigurations().addAll(voGeneratorConfiguration.getAdditionalPropertyConfigurations());
        voGeneratorConfiguration.setVoViewConfiguration(voViewGeneratorConfiguration);
    }

    private void parseTableListCommon(Properties attributes, AbstractTableListCommonConfiguration voViewGeneratorConfiguration) {

        String listKey = attributes.getProperty("listKey");
        if (stringHasValue(listKey)) {
            voViewGeneratorConfiguration.setListKey(listKey);
        }
        String title = attributes.getProperty("title");
        if (stringHasValue(title)) {
            voViewGeneratorConfiguration.setTitle(title);
        }
        String size = attributes.getProperty("size");
        if (stringHasValue(size)) {
            voViewGeneratorConfiguration.setSize(size);
        }
        String indexColumn = attributes.getProperty("indexColumn");
        if (stringHasValue(indexColumn)) {
            voViewGeneratorConfiguration.setIndexColumn(indexColumn);
        }
        String actionColumn = attributes.getProperty("actionColumn");
        if (stringHasValue(actionColumn)) {
            voViewGeneratorConfiguration.setActionColumn(splitToList(actionColumn));
        }
        String actionColumnFixed = attributes.getProperty("actionColumnFixed");
        if (stringHasValue(actionColumnFixed)) {
            voViewGeneratorConfiguration.setActionColumnFixed(actionColumnFixed);
        }
        String indexColumnFixed = attributes.getProperty("indexColumnFixed");
        if (stringHasValue(indexColumnFixed)) {
            voViewGeneratorConfiguration.setIndexColumnFixed(indexColumnFixed);
        }
        String toolbar = attributes.getProperty("toolbar");
        if (stringHasValue(toolbar)) {
            voViewGeneratorConfiguration.setToolbar(splitToList(toolbar));
        }
        String queryColumns = attributes.getProperty("queryColumns");
        if (stringHasValue(queryColumns)) {
            voViewGeneratorConfiguration.setQueryColumns(splitToList(queryColumns));
        }
        String fuzzyColumns = attributes.getProperty("fuzzyColumns");
        if (stringHasValue(fuzzyColumns)) {
            voViewGeneratorConfiguration.setFuzzyColumns(splitToList(fuzzyColumns));
        }
        String filterColumns = attributes.getProperty("filterColumns");
        if (stringHasValue(filterColumns)) {
            voViewGeneratorConfiguration.setFilterColumns(splitToList(filterColumns));
        }
        String defaultDisplayFields = attributes.getProperty("defaultDisplayFields");
        if (stringHasValue(defaultDisplayFields)) {
            voViewGeneratorConfiguration.setDefaultDisplayFields(splitToList(defaultDisplayFields));
        }

        String defaultHiddenFields = attributes.getProperty("defaultHiddenFields");
        if (stringHasValue(defaultHiddenFields)) {
            voViewGeneratorConfiguration.setDefaultHiddenFields(splitToSet(defaultHiddenFields));
        }
        String enablePager = attributes.getProperty("enablePager");
        if (stringHasValue(enablePager)) {
            voViewGeneratorConfiguration.setEnablePager(Boolean.parseBoolean(enablePager));
        }
        String defaultToolbar = attributes.getProperty("defaultToolbar");
        if (stringHasValue(defaultToolbar)) {
            voViewGeneratorConfiguration.setDefaultToolbar(splitToList(defaultToolbar));
        }
        String parentMenuId = attributes.getProperty("parentMenuId");
        if (stringHasValue(parentMenuId)) {
            voViewGeneratorConfiguration.setParentMenuId(parentMenuId);
        }
        String viewMenuElIcon = attributes.getProperty("viewMenuElIcon");
        if (stringHasValue(viewMenuElIcon)) {
            voViewGeneratorConfiguration.setViewMenuElIcon(viewMenuElIcon);
        }
        String categoryTreeUrl = attributes.getProperty("categoryTreeUrl");
        if (stringHasValue(categoryTreeUrl)) {
            voViewGeneratorConfiguration.setCategoryTreeUrl(categoryTreeUrl);
        }

        String multiple = attributes.getProperty("categoryTreeMultiple");
        if (stringHasValue(multiple)) {
            voViewGeneratorConfiguration.setCategoryTreeMultiple(Boolean.parseBoolean(multiple));
        }

        String uiFrameType = attributes.getProperty("uiFrameType");
        if (stringHasValue(uiFrameType)) {
            voViewGeneratorConfiguration.setUiFrameType(ViewVoUiFrameEnum.getEnum(uiFrameType));
        }
        String tableType = attributes.getProperty("tableType");
        if (stringHasValue(tableType)) {
            voViewGeneratorConfiguration.setTableType(tableType);
        }
        String totalRow = attributes.getProperty("totalRow");
        if (stringHasValue(totalRow)) {
            voViewGeneratorConfiguration.setTotalRow(Boolean.parseBoolean(totalRow));
        }
        String totalFields = attributes.getProperty("totalFields");
        if (stringHasValue(totalFields)) {
            voViewGeneratorConfiguration.setTotalFields(splitToSet(totalFields));
        }
        String totalText = attributes.getProperty("totalText");
        if (stringHasValue(totalText)) {
            voViewGeneratorConfiguration.setTotalText(totalText);
        }
        String defaultFilterExpr = attributes.getProperty("defaultFilterExpr");
        if (stringHasValue(defaultFilterExpr)) {
            voViewGeneratorConfiguration.setDefaultFilterExpr(defaultFilterExpr);
        }
        String showRowNumber = attributes.getProperty("showRowNumber");
        if (stringHasValue(showRowNumber)) {
            voViewGeneratorConfiguration.setShowRowNumber(Boolean.parseBoolean(showRowNumber));
        }
    }

    private ViewFieldOverrideConfiguration parseFieldOverrides(Context context, TableConfiguration tc, Node childNode, VOViewGeneratorConfiguration voViewGeneratorConfiguration) {
        ViewFieldOverrideConfiguration viewFieldOverrideConfiguration = new ViewFieldOverrideConfiguration();
        Properties attributes = parseAttributes(childNode);
        String fields = attributes.getProperty("fields");
        if (stringHasValue(fields)) {
            viewFieldOverrideConfiguration.setFields(splitToList(fields));
        }
        String label = attributes.getProperty("label");
        if (stringHasValue(label)) {
            viewFieldOverrideConfiguration.setLabel(label);
        }
        String width = attributes.getProperty("width");
        if (stringHasValue(width)) {
            viewFieldOverrideConfiguration.setWidth(width);
        }
        String align = attributes.getProperty("align");
        if (stringHasValue(align)) {
            viewFieldOverrideConfiguration.setAlign(align);
        }
        String fixed = attributes.getProperty("fixed");
        if (stringHasValue(fixed)) {
            viewFieldOverrideConfiguration.setFixed(fixed);
        }
        String headerAlign = attributes.getProperty("headerAlign");
        if (stringHasValue(headerAlign)) {
            viewFieldOverrideConfiguration.setHeaderAlign(headerAlign);
        }
        String sort = attributes.getProperty("sort");
        if (stringHasValue(sort)) {
            viewFieldOverrideConfiguration.setSort(Boolean.parseBoolean(sort));
        }
        String hide = attributes.getProperty("hide");
        if (stringHasValue(hide)) {
            viewFieldOverrideConfiguration.setHide(Boolean.parseBoolean(hide));
        }
        String edit = attributes.getProperty("edit");
        if (stringHasValue(edit)) {
            viewFieldOverrideConfiguration.setEdit(Boolean.parseBoolean(edit));
        }
        return viewFieldOverrideConfiguration;
    }

    private QueryColumnConfiguration parseQueryColumn(TableConfiguration tc, Node childNode) {
        QueryColumnConfiguration queryColumnConfiguration = new QueryColumnConfiguration(tc);
        Properties attributes = parseAttributes(childNode);
        String column = attributes.getProperty("column");
        if (stringHasValue(column)) {
            queryColumnConfiguration.setColumn(column);
        }
        String tagName = attributes.getProperty("tagType");
        if (stringHasValue(tagName)) {
            queryColumnConfiguration.setTagName(HtmlElementTagTypeEnum.ofCodeName(tagName));
        }
        String remark = attributes.getProperty("remark");
        if (stringHasValue(remark)) {
            queryColumnConfiguration.setRemark(remark);
        }
        String queryMode = attributes.getProperty("queryMode");
        if (stringHasValue(queryMode)) {
            queryColumnConfiguration.setQueryMode(QueryModesEnum.ofCodeName(queryMode));
        }
        String fieldType = attributes.getProperty("fieldType");
        if (stringHasValue(fieldType)) {
            queryColumnConfiguration.setFieldType(FieldTypeEnum.of(fieldType));
        }
        String order = attributes.getProperty("order");
        if (stringHasValue(order)) {
            queryColumnConfiguration.setOrder(Integer.parseInt(order));
        }
        String dataFormat = attributes.getProperty("dataFormat");
        if (stringHasValue(dataFormat)) {
            queryColumnConfiguration.setDataFormat(HtmlElementDataFormat.ofCodeName(dataFormat));
        }
        String dataSource = attributes.getProperty("dataSource");
        if (stringHasValue(dataSource)) {
            queryColumnConfiguration.setDataSource(HtmlElementDataSourceEnum.getEnum(dataSource));
        }
        String enumClassFullName = attributes.getProperty("enumClassFullName");
        if (stringHasValue(enumClassFullName)) {
            queryColumnConfiguration.setEnumClassFullName(enumClassFullName);
        }
        String dictCode = attributes.getProperty("dictCode");
        if (stringHasValue(dictCode)) {
            queryColumnConfiguration.setDictCode(dictCode);
        }
        String switchText = attributes.getProperty("switchText");
        if (stringHasValue(switchText)) {
            queryColumnConfiguration.setSwitchText(switchText);
        }
        String dataUrl = attributes.getProperty("dataUrl");
        if (stringHasValue(dataUrl)) {
            queryColumnConfiguration.setDataUrl(dataUrl);
        }
        return queryColumnConfiguration;
    }

    private HtmlButtonGeneratorConfiguration parseHtmlButton(Node childNode) {
        HtmlButtonGeneratorConfiguration htmlButtonConfiguration = new HtmlButtonGeneratorConfiguration();
        Properties attributes = parseAttributes(childNode);
        String id = attributes.getProperty("id");
        htmlButtonConfiguration.setId(id);
        String text = attributes.getProperty("text");
        if (stringHasValue(text)) {
            htmlButtonConfiguration.setText(text);
        }
        String title = attributes.getProperty("title");
        if (stringHasValue(title)) {
            htmlButtonConfiguration.setTitle(title);
        }
        String icon = attributes.getProperty("icon");
        if (stringHasValue(icon)) {
            htmlButtonConfiguration.setIcon(icon);
        }
        String type = attributes.getProperty("type");
        if (stringHasValue(type)) {
            htmlButtonConfiguration.setType(type);
        }
        String classes = attributes.getProperty("classes");
        if (stringHasValue(classes)) {
            htmlButtonConfiguration.setClasses(classes);
        }
        String handler = attributes.getProperty("handler");
        if (stringHasValue(handler)) {
            htmlButtonConfiguration.setHandler(handler);
        }
        String isLink = attributes.getProperty("isLink");
        if (stringHasValue(isLink)) {
            htmlButtonConfiguration.setLink(Boolean.parseBoolean(isLink));
        }
        String isRound = attributes.getProperty("isRound");
        if (stringHasValue(isRound)) {
            htmlButtonConfiguration.setRound(Boolean.parseBoolean(isRound));
        }
        String isCircle = attributes.getProperty("isCircle");
        if (stringHasValue(isCircle)) {
            htmlButtonConfiguration.setCircle(Boolean.parseBoolean(isCircle));
        }
        String isPlain = attributes.getProperty("isPlain");
        if (stringHasValue(isPlain)) {
            htmlButtonConfiguration.setPlain(Boolean.parseBoolean(isPlain));
        }
        String css = attributes.getProperty("css");
        if (stringHasValue(css)) {
            htmlButtonConfiguration.setCss(css);
        }
        String showCondition = attributes.getProperty("showCondition");
        if (stringHasValue(showCondition)) {
            htmlButtonConfiguration.setShowCondition(showCondition);
        }
        return htmlButtonConfiguration;
    }


    private void parseVORequest(Context context, TableConfiguration tc, Node node, VOGeneratorConfiguration voGeneratorConfiguration) {
        VORequestGeneratorConfiguration voRequestGeneratorConfiguration = new VORequestGeneratorConfiguration(context, tc);
        Properties attributes = parseAttributes(node);
        parseColumnsList(attributes, voRequestGeneratorConfiguration, voGeneratorConfiguration);
        String includePageParam = attributes.getProperty("includePageParam");
        voRequestGeneratorConfiguration.setIncludePageParam(Boolean.parseBoolean(includePageParam));

        //EqualsAndHashCodeColumns
        String ehAttr = attributes.getProperty(PropertyRegistry.ANY_EQUALS_AND_HASH_CODE);
        if (stringHasValue(ehAttr)) {
            voRequestGeneratorConfiguration.setEqualsAndHashCodeColumns(splitToList(ehAttr));
        } else {
            voRequestGeneratorConfiguration.getEqualsAndHashCodeColumns().addAll(voGeneratorConfiguration.getEqualsAndHashCodeColumns());
        }
        if (!voRequestGeneratorConfiguration.getEqualsAndHashCodeColumns().isEmpty()) {
            List<String> distinct = voRequestGeneratorConfiguration.getEqualsAndHashCodeColumns().stream().distinct().collect(Collectors.toList());
            voRequestGeneratorConfiguration.setEqualsAndHashCodeColumns(distinct);
        }

        parseModelChildNodeProperty(context, tc, node, voRequestGeneratorConfiguration);
        //继承vo的附加属性
        voRequestGeneratorConfiguration.getAdditionalPropertyConfigurations().addAll(voGeneratorConfiguration.getAdditionalPropertyConfigurations());
        //添加到vo配置
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
            voCacheGeneratorConfiguration.setIncludeColumns(splitToSet(includeColumns));
        }
        String typeColumn = attributes.getProperty(PropertyRegistry.ELEMENT_TYPE_COLUMN);
        if (stringHasValue(typeColumn)) {
            voCacheGeneratorConfiguration.setTypeColumn(typeColumn);
        }
        String keyColumn = attributes.getProperty(PropertyRegistry.ELEMENT_KEY_COLUMN);
        if (stringHasValue(keyColumn)) {
            voCacheGeneratorConfiguration.setKeyColumn(keyColumn);
        } else {
            voCacheGeneratorConfiguration.setKeyColumn(DefaultColumnNameEnum.ID.columnName());
        }
        String nameColumn = attributes.getProperty(PropertyRegistry.ELEMENT_VALUE_COLUMN);
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
            modelConfiguration.setEqualsAndHashCodeColumns(splitToList(ehAttr));
        }
        //判断enableChildren
        modelConfiguration.setGenerateChildren(Boolean.parseBoolean(attributes.getProperty("enableChildren")));

        parseModelChildNodeProperty(context, tc, node, modelConfiguration);
        //继承tc的附加属性
        modelConfiguration.getAdditionalPropertyConfigurations().addAll(tc.getAdditionalPropertyConfigurations());
        //添加到tc配置
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
            abstractModelGeneratorConfiguration.setExcludeColumns(splitToSet(excludeColumns));
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
            configuration.setTargetProject(getTargetProject(targetProject));
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
