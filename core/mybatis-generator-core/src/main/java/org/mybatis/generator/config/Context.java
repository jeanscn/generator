package org.mybatis.generator.config;

import org.mybatis.generator.api.*;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GeneratedHtmlFile;
import org.mybatis.generator.custom.db.ValidateDatabaseTable;
import org.mybatis.generator.internal.JDBCConnectionFactory;
import org.mybatis.generator.internal.ObjectFactory;
import org.mybatis.generator.internal.PluginAggregator;
import org.mybatis.generator.internal.db.DatabaseIntrospector;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static org.mybatis.generator.internal.util.StringUtility.*;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

public class Context extends PropertyHolder {

    private String id;

    private String appKeyword;

    private String moduleKeyword;

    private String moduleName;

    private boolean integrateMybatisPlus = true;

    private boolean integrateSpringSecurity;

    private boolean forceUpdateScalableElement;

    private List<String> forceUpdateElementList = new ArrayList<>();

    private List<String> onlyTablesGenerate = new ArrayList<>();

    private boolean updateModuleData;

    private boolean updateMenuData;

    private String vueEndProjectPath;

    private JDBCConnectionConfiguration jdbcConnectionConfiguration;

    private ConnectionFactoryConfiguration connectionFactoryConfiguration;

    private SqlMapGeneratorConfiguration sqlMapGeneratorConfiguration;

    private JavaTypeResolverConfiguration javaTypeResolverConfiguration;

    private JavaModelGeneratorConfiguration javaModelGeneratorConfiguration;

    private JavaClientGeneratorConfiguration javaClientGeneratorConfiguration;

    private final ArrayList<TableConfiguration> tableConfigurations;

    private final ModelType defaultModelType;

    private String beginningDelimiter = "\""; //$NON-NLS-1$

    private String endingDelimiter = "\""; //$NON-NLS-1$

    private CommentGeneratorConfiguration commentGeneratorConfiguration;

    private CommentGenerator commentGenerator;

    private PluginAggregator pluginAggregator;

    private final List<PluginConfiguration> pluginConfigurations;

    private String targetRuntime;

    private String introspectedColumnImpl;

    private Boolean autoDelimitKeywords;

    private JavaFormatter javaFormatter;

    private KotlinFormatter kotlinFormatter;

    private XmlFormatter xmlFormatter;

    private HtmlFormatter htmlFormatter;

    private boolean isJava8Targeted = true;

    private boolean isSqlServe;

    private String parentMenuId;

    private int jdkVersion;

    protected Map<String, String> sysMenuDataScriptLines = new LinkedHashMap<>();

    protected Map<String, String> moduleCateDataScriptLines = new LinkedHashMap<>();

    protected Map<String, String> moduleDataScriptLines = new LinkedHashMap<>();

    protected Map<String, String> wfProcTypeDataScriptLines = new LinkedHashMap<>();

    public Context(ModelType defaultModelType) {
        super();
        if (defaultModelType == null) {
            this.defaultModelType = ModelType.CONDITIONAL;
        } else {
            this.defaultModelType = defaultModelType;
        }
        tableConfigurations = new ArrayList<>();
        pluginConfigurations = new ArrayList<>();
    }

    public void addTableConfiguration(TableConfiguration tc) {
        tableConfigurations.add(tc);
    }

    public JDBCConnectionConfiguration getJdbcConnectionConfiguration() {
        return jdbcConnectionConfiguration;
    }

    public JavaClientGeneratorConfiguration getJavaClientGeneratorConfiguration() {
        return javaClientGeneratorConfiguration;
    }

    public JavaModelGeneratorConfiguration getJavaModelGeneratorConfiguration() {
        return javaModelGeneratorConfiguration;
    }

    public JavaTypeResolverConfiguration getJavaTypeResolverConfiguration() {
        return javaTypeResolverConfiguration;
    }

    public SqlMapGeneratorConfiguration getSqlMapGeneratorConfiguration() {
        return sqlMapGeneratorConfiguration;
    }

    public void addPluginConfiguration(PluginConfiguration pluginConfiguration) {
        pluginConfigurations.add(pluginConfiguration);
    }

    /**
     * This method does a simple validate, it makes sure that all required fields have been filled in. It does not do
     * any more complex operations such as validating that database tables exist or validating that named columns exist
     *
     * @param errors the errors
     */
    public void validate(List<String> errors) {
        if (!stringHasValue(id)) {
            errors.add(getString("ValidationError.16")); //$NON-NLS-1$
        }

        if (jdbcConnectionConfiguration == null && connectionFactoryConfiguration == null) {
            // must specify one
            errors.add(getString("ValidationError.10", id)); //$NON-NLS-1$
        } else if (jdbcConnectionConfiguration != null && connectionFactoryConfiguration != null) {
            // must not specify both
            errors.add(getString("ValidationError.10", id)); //$NON-NLS-1$
        } else if (jdbcConnectionConfiguration != null) {
            jdbcConnectionConfiguration.validate(errors);
        } else {
            connectionFactoryConfiguration.validate(errors);
        }

        if (javaModelGeneratorConfiguration == null) {
            errors.add(getString("ValidationError.8", id)); //$NON-NLS-1$
        } else {
            javaModelGeneratorConfiguration.validate(errors, id);
        }

        if (javaClientGeneratorConfiguration != null) {
            javaClientGeneratorConfiguration.validate(errors, id);
        }

        IntrospectedTable it = null;
        try {
            it = ObjectFactory.createIntrospectedTableForValidation(this);
        } catch (Exception e) {
            errors.add(getString("ValidationError.25", id)); //$NON-NLS-1$
        }

        if (it != null && it.requiresXMLGenerator()) {
            if (sqlMapGeneratorConfiguration == null) {
                errors.add(getString("ValidationError.9", id)); //$NON-NLS-1$
            } else {
                sqlMapGeneratorConfiguration.validate(errors, id);
            }
        }

        if (tableConfigurations.isEmpty()) {
            errors.add(getString("ValidationError.3", id)); //$NON-NLS-1$
        } else {
            for (int i = 0; i < tableConfigurations.size(); i++) {
                TableConfiguration tc = tableConfigurations.get(i);

                tc.validate(errors, i);
            }
        }

        for (PluginConfiguration pluginConfiguration : pluginConfigurations) {
            pluginConfiguration.validate(errors, id);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setJavaClientGeneratorConfiguration(
            JavaClientGeneratorConfiguration javaClientGeneratorConfiguration) {
        this.javaClientGeneratorConfiguration = javaClientGeneratorConfiguration;
    }

    public void setJavaModelGeneratorConfiguration(
            JavaModelGeneratorConfiguration javaModelGeneratorConfiguration) {
        this.javaModelGeneratorConfiguration = javaModelGeneratorConfiguration;
    }

    public void setJavaTypeResolverConfiguration(
            JavaTypeResolverConfiguration javaTypeResolverConfiguration) {
        this.javaTypeResolverConfiguration = javaTypeResolverConfiguration;
    }

    public void setJdbcConnectionConfiguration(
            JDBCConnectionConfiguration jdbcConnectionConfiguration) {
        this.jdbcConnectionConfiguration = jdbcConnectionConfiguration;
    }

    public void setSqlMapGeneratorConfiguration(
            SqlMapGeneratorConfiguration sqlMapGeneratorConfiguration) {
        this.sqlMapGeneratorConfiguration = sqlMapGeneratorConfiguration;
    }

    public ModelType getDefaultModelType() {
        return defaultModelType;
    }

    public List<TableConfiguration> getTableConfigurations() {
        return tableConfigurations;
    }

    public String getBeginningDelimiter() {
        return beginningDelimiter;
    }

    public String getEndingDelimiter() {
        return endingDelimiter;
    }

    @Override
    public void addProperty(String name, String value) {
        super.addProperty(name, value);

        if (PropertyRegistry.CONTEXT_BEGINNING_DELIMITER.equals(name)) {
            beginningDelimiter = value;
        } else if (PropertyRegistry.CONTEXT_ENDING_DELIMITER.equals(name)) {
            endingDelimiter = value;
        } else if (PropertyRegistry.CONTEXT_AUTO_DELIMIT_KEYWORDS.equals(name)
                && stringHasValue(value)) {
            autoDelimitKeywords = isTrue(value);
        } else if (PropertyRegistry.CONTEXT_TARGET_JAVA8.equals(name)
                && stringHasValue(value)) {
            isJava8Targeted = isTrue(value);
        }
    }

    public CommentGenerator getCommentGenerator() {
        if (commentGenerator == null) {
            commentGenerator = ObjectFactory.createCommentGenerator(this);
        }
        return commentGenerator;
    }

    public JavaFormatter getJavaFormatter() {
        if (javaFormatter == null) {
            javaFormatter = ObjectFactory.createJavaFormatter(this);
        }

        return javaFormatter;
    }

    public KotlinFormatter getKotlinFormatter() {
        if (kotlinFormatter == null) {
            kotlinFormatter = ObjectFactory.createKotlinFormatter(this);
        }

        return kotlinFormatter;
    }

    public XmlFormatter getXmlFormatter() {
        if (xmlFormatter == null) {
            xmlFormatter = ObjectFactory.createXmlFormatter(this);
        }

        return xmlFormatter;
    }

    public HtmlFormatter getHtmlFormatter() {
        if (htmlFormatter == null) {
            htmlFormatter = ObjectFactory.createHtmlFormatter(this);
        }

        return htmlFormatter;
    }

    public CommentGeneratorConfiguration getCommentGeneratorConfiguration() {
        return commentGeneratorConfiguration;
    }

    public void setCommentGeneratorConfiguration(
            CommentGeneratorConfiguration commentGeneratorConfiguration) {
        this.commentGeneratorConfiguration = commentGeneratorConfiguration;
    }

    public Plugin getPlugins() {
        return pluginAggregator;
    }

    public String getTargetRuntime() {
        return targetRuntime;
    }

    public void setTargetRuntime(String targetRuntime) {
        this.targetRuntime = targetRuntime;
    }

    public String getIntrospectedColumnImpl() {
        return introspectedColumnImpl;
    }

    public void setIntrospectedColumnImpl(String introspectedColumnImpl) {
        this.introspectedColumnImpl = introspectedColumnImpl;
    }

    // methods related to code generation.
    //
    // Methods should be called in this order:
    //
    // 1. getIntrospectionSteps()
    // 2. introspectTables()
    // 3. getGenerationSteps()
    // 4. generateFiles()
    //

    private final List<IntrospectedTable> introspectedTables = new ArrayList<>();

    /**
     * This method could be useful for users that use the library for introspection only
     * and not for code generation.
     *
     * @return a list containing the results of table introspection. The list will be empty
     * if this method is called before introspectTables(), or if no tables are found that
     * match the configuration
     */
    public List<IntrospectedTable> getIntrospectedTables() {
        return introspectedTables;
    }

    public int getIntrospectionSteps() {
        int steps = 0;

        steps++; // connect to database

        // for each table:
        //
        // 1. Create introspected table implementation

        steps += tableConfigurations.size();

        return steps;
    }

    /**
     * Introspect tables based on the configuration specified in the
     * constructor. This method is long running.
     *
     * @param callback                 a progress callback if progress information is desired, or
     *                                 <code>null</code>
     * @param warnings                 any warning generated from this method will be added to the
     *                                 List. Warnings are always Strings.
     * @param fullyQualifiedTableNames a set of table names to generate. The elements of the set must
     *                                 be Strings that exactly match what's specified in the
     *                                 configuration. For example, if table name = "foo" and schema =
     *                                 "bar", then the fully qualified table name is "foo.bar". If
     *                                 the Set is null or empty, then all tables in the configuration
     *                                 will be used for code generation.
     * @throws SQLException         if some error arises while introspecting the specified
     *                              database tables.
     * @throws InterruptedException if the progress callback reports a cancel
     */
    public void introspectTables(ProgressCallback callback, List<String> warnings, Set<String> fullyQualifiedTableNames)
            throws SQLException, InterruptedException {

        introspectedTables.clear();
        JavaTypeResolver javaTypeResolver = ObjectFactory.createJavaTypeResolver(this, warnings);

        Connection connection = null;

        try {
            callback.startTask(getString("Progress.0")); //$NON-NLS-1$
            connection = getConnection();

            DatabaseIntrospector databaseIntrospector = new DatabaseIntrospector(
                    this, connection.getMetaData(), javaTypeResolver, warnings);

            for (TableConfiguration tc : tableConfigurations) {
                String tableName = composeFullyQualifiedTableName(tc.getCatalog(), tc.getSchema(), tc.getTableName(), '.');

                if (fullyQualifiedTableNames != null
                        && !fullyQualifiedTableNames.isEmpty()
                        && !fullyQualifiedTableNames.contains(tableName)) {
                    continue;
                }

                if (!tc.areAnyStatementsEnabled()) {
                    warnings.add(getString("Warning.0", tableName)); //$NON-NLS-1$
                    continue;
                }

                callback.startTask(getString("Progress.1", tableName)); //$NON-NLS-1$
                List<IntrospectedTable> tables = databaseIntrospector.introspectTables(tc);

                if (tables != null) {
                    introspectedTables.addAll(tables);
                    for (IntrospectedTable table : tables) {
                        if (!table.getTableConfiguration().isIgnore()) {
                            ValidateDatabaseTable validateDatabaseTables = new ValidateDatabaseTable(table, connection, warnings);
                            validateDatabaseTables.executeUpdate();
                        }
                    }
                }
                callback.checkCancel();
            }
        } finally {
            closeConnection(connection);
        }
    }

    public int getGenerationSteps() {
        int steps = 0;

        for (IntrospectedTable introspectedTable : introspectedTables) {
            steps += introspectedTable.getGenerationSteps();
        }

        return steps;
    }

    public void generateFiles(ProgressCallback callback,
                              List<GeneratedJavaFile> generatedJavaFiles,
                              List<GeneratedXmlFile> generatedXmlFiles,
                              List<GeneratedHtmlFile> generatedHtmlFiles,
                              List<GeneratedKotlinFile> generatedKotlinFiles,
                              List<GeneratedFile> otherGeneratedFiles,
                              List<String> warnings)
            throws InterruptedException {

        pluginAggregator = new PluginAggregator();
        for (PluginConfiguration pluginConfiguration : pluginConfigurations) {
            Plugin plugin = ObjectFactory.createPlugin(this, pluginConfiguration);
            if (plugin.validate(warnings)) {
                pluginAggregator.addPlugin(plugin);
            } else {
                warnings.add(getString("Warning.24", pluginConfiguration.getConfigurationType(), id));
            }
        }

        // initialize everything first before generating. This allows plugins to know about other
        // items in the configuration.
        for (IntrospectedTable introspectedTable : introspectedTables) {
            callback.checkCancel();
            introspectedTable.initialize();
            introspectedTable.calculateCustom(warnings, callback);
            introspectedTable.calculateGenerators(warnings, callback);
        }

        for (IntrospectedTable introspectedTable : introspectedTables) {
            callback.checkCancel();
            generatedJavaFiles.addAll(introspectedTable.getGeneratedJavaFiles());
            generatedXmlFiles.addAll(introspectedTable.getGeneratedXmlFiles());
            generatedHtmlFiles.addAll(introspectedTable.getGeneratedHtmlFiles());
            generatedKotlinFiles.addAll(introspectedTable.getGeneratedKotlinFiles());

            otherGeneratedFiles.addAll(introspectedTable.getGeneratedSqlSchemaFiles());
            otherGeneratedFiles.addAll(introspectedTable.getGeneratedPermissionSqlDataFiles());
            otherGeneratedFiles.addAll(introspectedTable.getGeneratedPermissionActionSqlDataFiles());

            generatedJavaFiles.addAll(pluginAggregator.contextGenerateAdditionalJavaFiles(introspectedTable));
            generatedXmlFiles.addAll(pluginAggregator.contextGenerateAdditionalXmlFiles(introspectedTable));
            introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().forEach(htmlMapGeneratorConfiguration -> {
                otherGeneratedFiles.addAll(pluginAggregator.contextGenerateAdditionalWebFiles(introspectedTable,htmlMapGeneratorConfiguration));
            });

            generatedKotlinFiles.addAll(pluginAggregator.contextGenerateAdditionalKotlinFiles(introspectedTable));
            otherGeneratedFiles.addAll(pluginAggregator.contextGenerateAdditionalFiles(introspectedTable));
        }
        generatedJavaFiles.addAll(pluginAggregator.contextGenerateAdditionalJavaFiles());
        generatedXmlFiles.addAll(pluginAggregator.contextGenerateAdditionalXmlFiles());
        generatedHtmlFiles.addAll(pluginAggregator.contextGenerateAdditionalHtmlFiles());
        generatedKotlinFiles.addAll(pluginAggregator.contextGenerateAdditionalKotlinFiles());
        otherGeneratedFiles.addAll(pluginAggregator.contextGenerateAdditionalFiles());

    }


    /**
     * This method creates a new JDBC connection from the values specified in the configuration file.
     * If you call this method, then you are responsible
     * for closing the connection (See {@link Context#closeConnection(Connection)}). If you do not
     * close the connection, then there could be connection leaks.
     *
     * @return a new connection created from the values in the configuration file
     * @throws SQLException if any error occurs while creating the connection
     */
    public Connection getConnection() throws SQLException {
        ConnectionFactory connectionFactory;
        if (jdbcConnectionConfiguration != null) {
            connectionFactory = new JDBCConnectionFactory(jdbcConnectionConfiguration);
        } else {
            connectionFactory = ObjectFactory.createConnectionFactory(this);
        }
        if (connectionFactory.getConnection() != null) {
            this.setSqlServe(connectionFactory.getConnection().getMetaData().getDriverName().toUpperCase().contains("SQL SERVER"));
        }
        return connectionFactory.getConnection();
    }

    /**
     * This method closes a JDBC connection and ignores any errors. If the passed connection is null,
     * then the method does nothing.
     *
     * @param connection a JDBC connection to close, may be null
     */
    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    public boolean autoDelimitKeywords() {
        return autoDelimitKeywords != null
                && autoDelimitKeywords;
    }

    public ConnectionFactoryConfiguration getConnectionFactoryConfiguration() {
        return connectionFactoryConfiguration;
    }

    public void setConnectionFactoryConfiguration(ConnectionFactoryConfiguration connectionFactoryConfiguration) {
        this.connectionFactoryConfiguration = connectionFactoryConfiguration;
    }

    public boolean isJava8Targeted() {
        return isJava8Targeted;
    }

    public void setJava8Targeted(boolean isJava8Targeted) {
        this.isJava8Targeted = isJava8Targeted;
    }

    public int getJdkVersion() {
        return jdkVersion;
    }

    public void setJdkVersion(int jdkVersion) {
        this.jdkVersion = jdkVersion;
    }

    public boolean isSqlServe() {
        return isSqlServe;
    }

    public void setSqlServe(boolean sqlServe) {
        isSqlServe = sqlServe;
    }

    public boolean getAnyPropertyBoolean(String propertyName, String defaultVale, PropertyHolder... propertyHolder) {
        return Boolean.parseBoolean(getAnyPropertyValue(propertyName, defaultVale, propertyHolder));
    }

    public String getAnyPropertyValue(String propertyName, String defaultVale, PropertyHolder... propertyHolder) {
        for (PropertyHolder holder : propertyHolder) {
            if (holder.getProperties().containsKey(propertyName)) {
                return holder.getProperty(propertyName);
            }
        }
        return defaultVale;
    }

    public String getModuleKeyword() {
        return moduleKeyword;
    }

    public void setModuleKeyword(String moduleKeyword) {
        this.moduleKeyword = moduleKeyword;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public boolean isIntegrateMybatisPlus() {
        return integrateMybatisPlus;
    }

    public void setIntegrateMybatisPlus(boolean integrateMybatisPlus) {
        this.integrateMybatisPlus = integrateMybatisPlus;
    }

    public boolean isIntegrateSpringSecurity() {
        return integrateSpringSecurity;
    }

    public void setIntegrateSpringSecurity(boolean integrateSpringSecurity) {
        this.integrateSpringSecurity = integrateSpringSecurity;
    }

    public boolean isForceUpdateScalableElement() {
        return forceUpdateScalableElement;
    }

    public void setForceUpdateScalableElement(boolean forceUpdateScalableElement) {
        this.forceUpdateScalableElement = forceUpdateScalableElement;
    }

    public List<String> getForceUpdateElementList() {
        return forceUpdateElementList;
    }

    public void setForceUpdateElementList(List<String> forceUpdateElementList) {
        this.forceUpdateElementList = forceUpdateElementList;
    }

    public String getParentMenuId() {
        return parentMenuId;
    }

    public void setParentMenuId(String parentMenuId) {
        this.parentMenuId = parentMenuId;
    }


    public Map<String, String> getSysMenuDataScriptLines() {
        return sysMenuDataScriptLines;
    }

    public void addSysMenuDataScriptLines(String id, String sysMenuDataScriptLine) {
        this.sysMenuDataScriptLines.put(id, sysMenuDataScriptLine);
    }

    public Map<String, String> getModuleDataScriptLines() {
        return moduleDataScriptLines;
    }

    public void addModuleDataScriptLine(String id, String moduleDataScriptLine) {
        this.moduleDataScriptLines.put(id, moduleDataScriptLine);
    }

    public Map<String, String> getModuleCateDataScriptLines() {
        return moduleCateDataScriptLines;
    }

    public void addModuleCateDataScriptLine(String id, String moduleCateDataScriptLine) {
        this.moduleCateDataScriptLines.put(id, moduleCateDataScriptLine);
    }

    public Map<String, String> getWfProcTypeDataScriptLines() {
        return wfProcTypeDataScriptLines;
    }

    public void addWfProcTypeDataScriptLines(String id,String wfProcTypeDataScriptLine) {
        this.wfProcTypeDataScriptLines.put(id, wfProcTypeDataScriptLine);
    }

    public List<String> getOnlyTablesGenerate() {
        return onlyTablesGenerate;
    }

    public void setOnlyTablesGenerate(List<String> onlyTablesGenerate) {
        this.onlyTablesGenerate = onlyTablesGenerate;
    }

    public boolean isUpdateModuleData() {
        return updateModuleData;
    }

    public void setUpdateModuleData(boolean updateModuleData) {
        this.updateModuleData = updateModuleData;
    }

    public String getModuleDataFileName() {
        return "data-module-" + this.getModuleKeyword().toLowerCase() + ".sql";
    }

    public File getModuleDataSqlFile() {
        return new File("src/main/resources/sql/init/" + getModuleDataFileName());
    }

    public boolean isUpdateMenuData() {
        return updateMenuData;
    }

    public void setUpdateMenuData(boolean updateMenuData) {
        this.updateMenuData = updateMenuData;
    }

    public String getMenuDataFileName() {
        return "data-menu-" + this.getModuleKeyword().toLowerCase() + ".sql";
    }

    public File getMenuDataSqlFile() {
        return new File("src/main/resources/sql/init/" + getMenuDataFileName());
    }

    public String getAppKeyword() {
        return appKeyword;
    }

    public void setAppKeyword(String appKeyword) {
        this.appKeyword = appKeyword;
    }

    public String getVueEndProjectPath() {
        return vueEndProjectPath;
    }

    public void setVueEndProjectPath(String vueEndProjectPath) {
        this.vueEndProjectPath = vueEndProjectPath;
    }

    public void validateTableConfig(ProgressCallback callback, List<String> warnings, Context context) {
        if (tableConfigurations == null || this.getIntrospectedTables().size()==0) {
            return;
        }
        this.getIntrospectedTables().forEach(introspectedTable -> {
            introspectedTable.getTableConfiguration().reprocessConfiguration(warnings, introspectedTable,context);
        });
    }

    public void initDefault() {
        String basePackage = String.join(".", "com.vgosoft", this.appKeyword, this.moduleKeyword);
        //设置默认的javaModelGenerator
        if (this.getJavaModelGeneratorConfiguration()==null) {
            JavaModelGeneratorConfiguration javaModelGeneratorConfiguration = new JavaModelGeneratorConfiguration();
            javaModelGeneratorConfiguration.setTargetPackage(String.join(".", basePackage,"entity"));
            javaModelGeneratorConfiguration.setTargetProject("src/main/java");
            javaModelGeneratorConfiguration.setBaseTargetPackage(basePackage);
            javaModelGeneratorConfiguration.setTargetPackageGen(String.join(".", basePackage,"codegen.entity"));
            this.setJavaModelGeneratorConfiguration(javaModelGeneratorConfiguration);
        }
        //设置默认的sqlMapGenerator
        if (this.getSqlMapGeneratorConfiguration()==null) {
            SqlMapGeneratorConfiguration sqlMapGeneratorConfiguration = new SqlMapGeneratorConfiguration();
            sqlMapGeneratorConfiguration.setTargetPackage("mappers");
            sqlMapGeneratorConfiguration.setTargetProject("src/main/resources");
            this.setSqlMapGeneratorConfiguration(sqlMapGeneratorConfiguration);
        }
        //设置默认的javaClientGenerator
        if (this.getJavaClientGeneratorConfiguration()==null) {
            JavaClientGeneratorConfiguration javaClientGeneratorConfiguration = new JavaClientGeneratorConfiguration();
            javaClientGeneratorConfiguration.setTargetPackage(String.join(".", basePackage,"dao"));
            javaClientGeneratorConfiguration.setTargetProject("src/main/java");
            javaClientGeneratorConfiguration.setBaseTargetPackage(basePackage);
            javaClientGeneratorConfiguration.setTargetPackageGen(String.join(".", basePackage,"codegen.dao"));
            javaClientGeneratorConfiguration.setConfigurationType("XMLMAPPER");
            this.setJavaClientGeneratorConfiguration(javaClientGeneratorConfiguration);
        }
    }


}
