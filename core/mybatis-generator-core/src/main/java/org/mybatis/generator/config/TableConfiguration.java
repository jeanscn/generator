package org.mybatis.generator.config;

import com.vgosoft.core.constant.enums.DefultColumnNameEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.custom.RelationTypeEnum;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.messages.Messages;

import java.util.*;
import java.util.stream.Collectors;

import static org.mybatis.generator.internal.util.StringUtility.*;

public class TableConfiguration extends PropertyHolder {

    private boolean insertStatementEnabled;

    private boolean insertBatchStatementEnabled;

    private boolean updateBatchStatementEnabled;

    private boolean insertOrUpdateStatementEnabled;

    private boolean fileUploadStatementEnabled;

    private boolean selectByPrimaryKeyStatementEnabled;

    private boolean selectByExampleStatementEnabled;

    private boolean updateByPrimaryKeyStatementEnabled;

    private boolean deleteByPrimaryKeyStatementEnabled;

    private boolean deleteByExampleStatementEnabled;

    private boolean countByExampleStatementEnabled;

    private boolean updateByExampleStatementEnabled;

    private final List<ColumnOverride> columnOverrides;

    private final Map<IgnoredColumn, Boolean> ignoredColumns;

    private GeneratedKey generatedKey;

    private boolean ignore;

    private String selectByPrimaryKeyQueryId;

    private String selectByExampleQueryId;

    private String catalog;

    private String schema;

    private String tableName;

    private String domainObjectName;

    private boolean isModules;

    private String alias;

    private ModelType modelType;

    private String tableType;

    private boolean wildcardEscapingEnabled;

    private boolean delimitIdentifiers;

    private DomainObjectRenamingRule domainObjectRenamingRule;

    private ColumnRenamingRule columnRenamingRule;

    private boolean isAllColumnDelimitingEnabled;

    private String mapperName;

    private String sqlProviderName;

    private final Set<String> columnNames = new HashSet<>();

    private final Set<String> fieldNames = new HashSet<>();

    private final List<SelectByTableGeneratorConfiguration> selectByTableGeneratorConfigurations = new ArrayList<>();

    private List<SelectByColumnGeneratorConfiguration> selectByColumnGeneratorConfigurations = new ArrayList<>();

    private List<SelectBySqlMethodGeneratorConfiguration> selectBySqlMethodGeneratorConfigurations = new ArrayList<>();

    private final List<RelationGeneratorConfiguration> relationGeneratorConfigurations = new ArrayList<>();

    private final List<HtmlGeneratorConfiguration> htmlGeneratorConfigurations = new ArrayList<>();

    private JavaServiceGeneratorConfiguration javaServiceGeneratorConfiguration;

    private JavaServiceImplGeneratorConfiguration javaServiceImplGeneratorConfiguration;

    private JavaControllerGeneratorConfiguration javaControllerGeneratorConfiguration;

    private JavaModelGeneratorConfiguration javaModelGeneratorConfiguration;

    private SqlMapGeneratorConfiguration sqlMapGeneratorConfiguration;

    private JavaClientGeneratorConfiguration javaClientGeneratorConfiguration;

    private SqlSchemaGeneratorConfiguration sqlSchemaGeneratorConfiguration;

    private VOGeneratorConfiguration voGeneratorConfiguration;

    private VOCacheGeneratorConfiguration voCacheGeneratorConfiguration;

    private final List<IgnoredColumnPattern> ignoredColumnPatterns = new ArrayList<>();

    private String serviceApiBasePath;

    private String htmlBasePath;

    private Set<String> validateIgnoreColumns = new HashSet<>();

    private final Set<IntrospectedColumn> htmlHiddenColumns = new HashSet<>();

    public TableConfiguration(Context context) {
        super();
        this.modelType = context.getDefaultModelType();
        columnOverrides = new ArrayList<>();
        ignoredColumns = new HashMap<>();

        isModules = false;
        insertStatementEnabled = true;
        insertBatchStatementEnabled = true;
        insertOrUpdateStatementEnabled = true;
        fileUploadStatementEnabled = false;

        selectByPrimaryKeyStatementEnabled = true;
        selectByExampleStatementEnabled = true;
        updateByPrimaryKeyStatementEnabled = true;
        deleteByPrimaryKeyStatementEnabled = true;
        deleteByExampleStatementEnabled = true;
        countByExampleStatementEnabled = true;
        updateByExampleStatementEnabled = true;
        updateBatchStatementEnabled = true;
        tableType = "dataTable";

    }

    public boolean isDeleteByPrimaryKeyStatementEnabled() {
        return deleteByPrimaryKeyStatementEnabled;
    }

    public void setDeleteByPrimaryKeyStatementEnabled(
            boolean deleteByPrimaryKeyStatementEnabled) {
        this.deleteByPrimaryKeyStatementEnabled = deleteByPrimaryKeyStatementEnabled;
    }

    public boolean isInsertStatementEnabled() {
        return insertStatementEnabled;
    }

    public void setInsertStatementEnabled(boolean insertStatementEnabled) {
        this.insertStatementEnabled = insertStatementEnabled;
    }

    public boolean isInsertBatchStatementEnabled() {
        return insertBatchStatementEnabled;
    }

    public void setInsertBatchStatementEnabled(boolean insertBatchStatementEnabled) {
        this.insertBatchStatementEnabled = insertBatchStatementEnabled;
    }

    public boolean isSelectByPrimaryKeyStatementEnabled() {
        return selectByPrimaryKeyStatementEnabled;
    }

    public void setSelectByPrimaryKeyStatementEnabled(
            boolean selectByPrimaryKeyStatementEnabled) {
        this.selectByPrimaryKeyStatementEnabled = selectByPrimaryKeyStatementEnabled;
    }

    public boolean isUpdateByPrimaryKeyStatementEnabled() {
        return updateByPrimaryKeyStatementEnabled;
    }

    public void setUpdateByPrimaryKeyStatementEnabled(
            boolean updateByPrimaryKeyStatementEnabled) {
        this.updateByPrimaryKeyStatementEnabled = updateByPrimaryKeyStatementEnabled;
    }

    public boolean isColumnIgnored(String columnName) {
        for (Map.Entry<IgnoredColumn, Boolean> entry : ignoredColumns
                .entrySet()) {
            if (entry.getKey().matches(columnName)) {
                entry.setValue(Boolean.TRUE);
                return true;
            }
        }

        for (IgnoredColumnPattern ignoredColumnPattern : ignoredColumnPatterns) {
            if (ignoredColumnPattern.matches(columnName)) {
                return true;
            }
        }

        return false;
    }

    public void addIgnoredColumn(IgnoredColumn ignoredColumn) {
        ignoredColumns.put(ignoredColumn, Boolean.FALSE);
    }

    public void addIgnoredColumnPattern(IgnoredColumnPattern ignoredColumnPattern) {
        ignoredColumnPatterns.add(ignoredColumnPattern);
    }

    public void addColumnOverride(ColumnOverride columnOverride) {
        columnOverrides.add(columnOverride);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof TableConfiguration)) {
            return false;
        }

        TableConfiguration other = (TableConfiguration) obj;

        return Objects.equals(this.catalog, other.catalog)
                && Objects.equals(this.schema, other.schema)
                && Objects.equals(this.tableName, other.tableName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(catalog, schema, tableName);
    }

    public boolean isSelectByExampleStatementEnabled() {
        return selectByExampleStatementEnabled;
    }

    public void setSelectByExampleStatementEnabled(
            boolean selectByExampleStatementEnabled) {
        this.selectByExampleStatementEnabled = selectByExampleStatementEnabled;
    }

    /**
     * May return null if the column has not been overridden.
     *
     * @param columnName the column name
     * @return the column override (if any) related to this column
     */
    public ColumnOverride getColumnOverride(String columnName) {
        for (ColumnOverride co : columnOverrides) {
            if (co.isColumnNameDelimited()) {
                if (columnName.equals(co.getColumnName())) {
                    return co;
                }
            } else {
                if (columnName.equalsIgnoreCase(co.getColumnName())) {
                    return co;
                }
            }
        }

        return null;
    }

    public Optional<GeneratedKey> getGeneratedKey() {
        return Optional.ofNullable(generatedKey);
    }

    public String getSelectByExampleQueryId() {
        return selectByExampleQueryId;
    }

    public void setSelectByExampleQueryId(String selectByExampleQueryId) {
        this.selectByExampleQueryId = selectByExampleQueryId;
    }

    public String getSelectByPrimaryKeyQueryId() {
        return selectByPrimaryKeyQueryId;
    }

    public void setSelectByPrimaryKeyQueryId(String selectByPrimaryKeyQueryId) {
        this.selectByPrimaryKeyQueryId = selectByPrimaryKeyQueryId;
    }

    public boolean isDeleteByExampleStatementEnabled() {
        return deleteByExampleStatementEnabled;
    }

    public void setDeleteByExampleStatementEnabled(
            boolean deleteByExampleStatementEnabled) {
        this.deleteByExampleStatementEnabled = deleteByExampleStatementEnabled;
    }

    public boolean areAnyStatementsEnabled() {
        return selectByExampleStatementEnabled
                || selectByPrimaryKeyStatementEnabled || insertStatementEnabled
                || updateByPrimaryKeyStatementEnabled
                || deleteByExampleStatementEnabled
                || deleteByPrimaryKeyStatementEnabled
                || countByExampleStatementEnabled
                || updateByExampleStatementEnabled;
    }

    public void setGeneratedKey(GeneratedKey generatedKey) {
        this.generatedKey = generatedKey;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getDomainObjectName() {
        return domainObjectName;
    }

    public void setDomainObjectName(String domainObjectName) {
        this.domainObjectName = domainObjectName;
    }

    public boolean isModules() {
        return isModules;
    }

    public void setModules(boolean modules) {
        isModules = modules;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public List<ColumnOverride> getColumnOverrides() {
        return columnOverrides;
    }

    public Set<String> getValidateIgnoreColumns() {
        return validateIgnoreColumns;
    }

    public void setValidateIgnoreColumns(Set<String> validateIgnoreColumns) {
        this.validateIgnoreColumns = validateIgnoreColumns;
    }

    /**
     * Returns a List of Strings. The values are the columns
     * that were specified to be ignored in the table, but do not exist in the
     * table.
     *
     * @return a List of Strings - the columns that were improperly configured
     * as ignored columns
     */
    public List<String> getIgnoredColumnsInError() {
        List<String> answer = new ArrayList<>();

        for (Map.Entry<IgnoredColumn, Boolean> entry : ignoredColumns
                .entrySet()) {
            if (Boolean.FALSE.equals(entry.getValue())) {
                answer.add(entry.getKey().getColumnName());
            }
        }

        return answer;
    }

    public ModelType getModelType() {
        return modelType;
    }

    public void setConfiguredModelType(String configuredModelType) {
        this.modelType = ModelType.getModelType(configuredModelType);
    }

    public boolean isWildcardEscapingEnabled() {
        return wildcardEscapingEnabled;
    }

    public void setWildcardEscapingEnabled(boolean wildcardEscapingEnabled) {
        this.wildcardEscapingEnabled = wildcardEscapingEnabled;
    }

    @Override
    public String toString() {
        return composeFullyQualifiedTableName(catalog, schema,
                tableName, '.');
    }

    public boolean isDelimitIdentifiers() {
        return delimitIdentifiers;
    }

    public void setDelimitIdentifiers(boolean delimitIdentifiers) {
        this.delimitIdentifiers = delimitIdentifiers;
    }

    public boolean isCountByExampleStatementEnabled() {
        return countByExampleStatementEnabled;
    }

    public void setCountByExampleStatementEnabled(
            boolean countByExampleStatementEnabled) {
        this.countByExampleStatementEnabled = countByExampleStatementEnabled;
    }

    public boolean isUpdateByExampleStatementEnabled() {
        return updateByExampleStatementEnabled;
    }

    public void setUpdateByExampleStatementEnabled(
            boolean updateByExampleStatementEnabled) {
        this.updateByExampleStatementEnabled = updateByExampleStatementEnabled;
    }

    public boolean isUpdateBatchStatementEnabled() {
        return updateBatchStatementEnabled;
    }

    public void setUpdateBatchStatementEnabled(boolean updateBatchStatementEnabled) {
        this.updateBatchStatementEnabled = updateBatchStatementEnabled;
    }

    public boolean isInsertOrUpdateStatementEnabled() {
        return insertOrUpdateStatementEnabled;
    }

    public void setInsertOrUpdateStatementEnabled(boolean insertOrUpdateStatementEnabled) {
        this.insertOrUpdateStatementEnabled = insertOrUpdateStatementEnabled;
    }

    public boolean isFileUploadStatementEnabled() {
        return fileUploadStatementEnabled;
    }

    public void setFileUploadStatementEnabled(boolean fileUploadStatementEnabled) {
        this.fileUploadStatementEnabled = fileUploadStatementEnabled;
    }

    public void validate(List<String> errors, int listPosition) {
        if (!stringHasValue(tableName)) {
            errors.add(Messages.getString(
                    "ValidationError.6", Integer.toString(listPosition))); //$NON-NLS-1$
        }

        String fqTableName = composeFullyQualifiedTableName(
                catalog, schema, tableName, '.');

        if (generatedKey != null) {
            generatedKey.validate(errors, fqTableName);
        }

        // when using column indexes, either both or neither query ids
        // should be set
        if (isTrue(getProperty(PropertyRegistry.TABLE_USE_COLUMN_INDEXES))
                && selectByExampleStatementEnabled
                && selectByPrimaryKeyStatementEnabled) {
            boolean queryId1Set = stringHasValue(selectByExampleQueryId);
            boolean queryId2Set = stringHasValue(selectByPrimaryKeyQueryId);

            if (queryId1Set != queryId2Set) {
                errors.add(Messages.getString("ValidationError.13", //$NON-NLS-1$
                        fqTableName));
            }
        }

        if (domainObjectRenamingRule != null) {
            domainObjectRenamingRule.validate(errors, fqTableName);
        }

        if (columnRenamingRule != null) {
            columnRenamingRule.validate(errors, fqTableName);
        }

        for (ColumnOverride columnOverride : columnOverrides) {
            columnOverride.validate(errors, fqTableName);
        }

        for (IgnoredColumn ignoredColumn : ignoredColumns.keySet()) {
            ignoredColumn.validate(errors, fqTableName);
        }

        for (IgnoredColumnPattern ignoredColumnPattern : ignoredColumnPatterns) {
            ignoredColumnPattern.validate(errors, fqTableName);
        }
    }

    public DomainObjectRenamingRule getDomainObjectRenamingRule() {
        return domainObjectRenamingRule;
    }

    public void setDomainObjectRenamingRule(DomainObjectRenamingRule domainObjectRenamingRule) {
        this.domainObjectRenamingRule = domainObjectRenamingRule;
    }

    public ColumnRenamingRule getColumnRenamingRule() {
        return columnRenamingRule;
    }

    public void setColumnRenamingRule(ColumnRenamingRule columnRenamingRule) {
        this.columnRenamingRule = columnRenamingRule;
    }

    public boolean isAllColumnDelimitingEnabled() {
        return isAllColumnDelimitingEnabled;
    }

    public void setAllColumnDelimitingEnabled(
            boolean isAllColumnDelimitingEnabled) {
        this.isAllColumnDelimitingEnabled = isAllColumnDelimitingEnabled;
    }

    public String getMapperName() {
        return mapperName;
    }

    public void setMapperName(String mapperName) {
        this.mapperName = mapperName;
    }

    public String getSqlProviderName() {
        return sqlProviderName;
    }

    public void setSqlProviderName(String sqlProviderName) {
        this.sqlProviderName = sqlProviderName;
    }

    public String getDynamicSqlSupportClassName() {
        return getProperty(PropertyRegistry.TABLE_DYNAMIC_SQL_SUPPORT_CLASS_NAME);
    }

    public String getDynamicSqlTableObjectName() {
        return getProperty(PropertyRegistry.TABLE_DYNAMIC_SQL_TABLE_OBJECT_NAME);
    }

    public List<SelectByTableGeneratorConfiguration> getSelectByTableGeneratorConfiguration() {
        return selectByTableGeneratorConfigurations;
    }

    public void addSelectByTableGeneratorConfiguration(SelectByTableGeneratorConfiguration selectByTableGeneratorConfiguration) {
        this.selectByTableGeneratorConfigurations.add(selectByTableGeneratorConfiguration);
    }

    public List<SelectByColumnGeneratorConfiguration> getSelectByColumnGeneratorConfigurations() {
        return selectByColumnGeneratorConfigurations;
    }

    public void addSelectByColumnGeneratorConfiguration(SelectByColumnGeneratorConfiguration selectByColumnGeneratorConfiguration) {
        this.selectByColumnGeneratorConfigurations.add(selectByColumnGeneratorConfiguration);
    }

    public void setSelectByColumnGeneratorConfigurations(List<SelectByColumnGeneratorConfiguration> selectByColumnGeneratorConfigurations) {
        this.selectByColumnGeneratorConfigurations = selectByColumnGeneratorConfigurations;
    }

    public List<SelectBySqlMethodGeneratorConfiguration> getSelectBySqlMethodGeneratorConfigurations() {
        return selectBySqlMethodGeneratorConfigurations;
    }

    public void addSelectBySqlMethodGeneratorConfiguration(SelectBySqlMethodGeneratorConfiguration selectBySqlMethodGeneratorConfiguration) {
        this.selectBySqlMethodGeneratorConfigurations.add(selectBySqlMethodGeneratorConfiguration);
    }

    public void setSelectBySqlMethodGeneratorConfigurations(List<SelectBySqlMethodGeneratorConfiguration> selectBySqlMethodGeneratorConfigurations) {
        this.selectBySqlMethodGeneratorConfigurations = selectBySqlMethodGeneratorConfigurations;
    }

    public List<RelationGeneratorConfiguration> getRelationGeneratorConfigurations() {
        return relationGeneratorConfigurations;
    }

    public void addRelationGeneratorConfiguration(RelationGeneratorConfiguration relationGeneratorConfiguration) {
        this.relationGeneratorConfigurations.add(relationGeneratorConfiguration);
    }

    public List<HtmlGeneratorConfiguration> getHtmlMapGeneratorConfigurations() {
        return htmlGeneratorConfigurations;
    }

    public void addHtmlMapGeneratorConfigurations(HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        this.htmlGeneratorConfigurations.add(htmlGeneratorConfiguration);
    }

    public JavaServiceGeneratorConfiguration getJavaServiceGeneratorConfiguration() {
        return javaServiceGeneratorConfiguration;
    }

    public void setJavaServiceGeneratorConfiguration(JavaServiceGeneratorConfiguration javaServiceGeneratorConfiguration) {
        this.javaServiceGeneratorConfiguration = javaServiceGeneratorConfiguration;
    }

    public JavaServiceImplGeneratorConfiguration getJavaServiceImplGeneratorConfiguration() {
        return javaServiceImplGeneratorConfiguration;
    }

    public void setJavaServiceImplGeneratorConfiguration(JavaServiceImplGeneratorConfiguration javaServiceImplGeneratorConfiguration) {
        this.javaServiceImplGeneratorConfiguration = javaServiceImplGeneratorConfiguration;
    }

    public JavaControllerGeneratorConfiguration getJavaControllerGeneratorConfiguration() {
        return javaControllerGeneratorConfiguration;
    }

    public void setJavaControllerGeneratorConfiguration(JavaControllerGeneratorConfiguration javaControllerGeneratorConfiguration) {
        this.javaControllerGeneratorConfiguration = javaControllerGeneratorConfiguration;
    }

    public JavaModelGeneratorConfiguration getJavaModelGeneratorConfiguration() {
        return javaModelGeneratorConfiguration;
    }

    public void setJavaModelGeneratorConfiguration(JavaModelGeneratorConfiguration javaModelGeneratorConfiguration) {
        this.javaModelGeneratorConfiguration = javaModelGeneratorConfiguration;
    }

    public SqlMapGeneratorConfiguration getSqlMapGeneratorConfiguration() {
        return sqlMapGeneratorConfiguration;
    }

    public void setSqlMapGeneratorConfiguration(SqlMapGeneratorConfiguration sqlMapGeneratorConfiguration) {
        this.sqlMapGeneratorConfiguration = sqlMapGeneratorConfiguration;
    }

    public JavaClientGeneratorConfiguration getJavaClientGeneratorConfiguration() {
        return javaClientGeneratorConfiguration;
    }

    public void setJavaClientGeneratorConfiguration(JavaClientGeneratorConfiguration javaClientGeneratorConfiguration) {
        this.javaClientGeneratorConfiguration = javaClientGeneratorConfiguration;
    }

    public SqlSchemaGeneratorConfiguration getSqlSchemaGeneratorConfiguration() {
        return sqlSchemaGeneratorConfiguration;
    }

    public void setSqlSchemaGeneratorConfiguration(SqlSchemaGeneratorConfiguration sqlSchemaGeneratorConfiguration) {
        this.sqlSchemaGeneratorConfiguration = sqlSchemaGeneratorConfiguration;
    }

    public VOGeneratorConfiguration getVoGeneratorConfiguration() {
        return voGeneratorConfiguration;
    }

    public void setVoGeneratorConfiguration(VOGeneratorConfiguration voGeneratorConfiguration) {
        this.voGeneratorConfiguration = voGeneratorConfiguration;
    }

    public VOCacheGeneratorConfiguration getVoCacheGeneratorConfiguration() {
        return voCacheGeneratorConfiguration;
    }

    public void setVoCacheGeneratorConfiguration(VOCacheGeneratorConfiguration voCacheGeneratorConfiguration) {
        this.voCacheGeneratorConfiguration = voCacheGeneratorConfiguration;
    }

    public boolean isIgnore() {
        return ignore;
    }

    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
    }

    public String getServiceApiBasePath() {
        return serviceApiBasePath;
    }

    public void setServiceApiBasePath(String serviceApiBasePath) {
        this.serviceApiBasePath = serviceApiBasePath;
    }

    public String getHtmlBasePath() {
        return htmlBasePath;
    }

    public void setHtmlBasePath(String htmlBasePath) {
        this.htmlBasePath = htmlBasePath;
    }


    public Set<String> getColumnNames() {
        return columnNames;
    }

    public Set<String> getFieldNames() {
        return fieldNames;
    }

    public Set<IntrospectedColumn> getHtmlHiddenColumns() {
        return htmlHiddenColumns;
    }

    public void validateConfig(List<String> warnings, IntrospectedTable introspectedTable) {
        //先更新TC与表结构相关的自定义属性
        this.updateTableConfiguration(introspectedTable);

        calculateSelectByTableProperty();

        //计算html
        calculateHtmlAttributes(introspectedTable);

        //增加SelectBaseByPrimaryKey配置
        calculateSelectBaseByPrimaryKeyConfig(introspectedTable);

        //计算所有的selectByColumn配置，去重、计算列信息
        calculateSelectByColumnConfigurations(introspectedTable);

        //根据所有配置信息，进行调整
        //1、enableChildren.
        calculateSelectByParentIdConfig(warnings, introspectedTable);
        //如果存在parent_id字段，则自动添加children属性
        calculateChildrenRelationConfig(introspectedTable);

        calculateSelectBySqlMethodProperty(introspectedTable);
    }

    private void calculateSelectByParentIdConfig(List<String> warnings, IntrospectedTable introspectedTable) {
        String parentIdColumnName = DefultColumnNameEnum.PARENT_ID.columnName();
        if (introspectedTable.getTableConfiguration().getJavaModelGeneratorConfiguration().isGenerateChildren()
                && this.getColumnNames().contains(parentIdColumnName)) {
            //如果存在parent_id字段，则自动添加selectByParentId方法
            if (this.getSelectByColumnGeneratorConfigurations().stream()
                    .noneMatch(c -> c.getColumns().stream().anyMatch(column -> parentIdColumnName.equalsIgnoreCase(column.getActualColumnName())))) {
                introspectedTable.getColumn(parentIdColumnName).ifPresent(column -> {
                    SelectByColumnGeneratorConfiguration selectByParentId = new SelectByColumnGeneratorConfiguration(column.getActualColumnName());
                    selectByParentId.addColumn(column);
                    selectByParentId.setMethodName(JavaBeansUtil.byColumnMethodName(selectByParentId.getColumns()));
                    selectByParentId.setDeleteMethodName(JavaBeansUtil.deleteByColumnMethodName(selectByParentId.getColumns()));
                    this.addSelectByColumnGeneratorConfiguration(selectByParentId);
                    warnings.add("table:" + this.tableName + " 自动添加" + selectByParentId.getMethodName() + " 方法.");
                });
            }
        }
    }

    private void calculateChildrenRelationConfig(IntrospectedTable introspectedTable) {
        String parentIdColumnName = DefultColumnNameEnum.PARENT_ID.columnName();
        if (introspectedTable.getTableConfiguration().getJavaModelGeneratorConfiguration().isGenerateChildren()) {
            introspectedTable.getColumn(parentIdColumnName).ifPresent(column -> {
                if (this.getRelationGeneratorConfigurations().stream().noneMatch(c -> DefultColumnNameEnum.CHILDREN.fieldName().equalsIgnoreCase(c.getPropertyName()))) {
                    RelationGeneratorConfiguration relationGeneratorConfiguration = new RelationGeneratorConfiguration();
                    relationGeneratorConfiguration.setRemark("子集合");
                    relationGeneratorConfiguration.setPropertyName(DefultColumnNameEnum.CHILDREN.fieldName());
                    relationGeneratorConfiguration.setColumn(DefultColumnNameEnum.ID.columnName());

                    StringBuilder sb = new StringBuilder();
                    sb.append(this.getJavaModelGeneratorConfiguration().getTargetPackage());
                    sb.append(".");
                    sb.append(this.getDomainObjectName());
                    relationGeneratorConfiguration.setModelTye(sb.toString());
                    if (this.getVoGeneratorConfiguration() != null
                            && this.getVoGeneratorConfiguration().getVoModelConfiguration() != null
                            && this.getVoGeneratorConfiguration().getVoModelConfiguration().isGenerate()) {
                        String voType = substringBeforeLast(introspectedTable.getContext().getJavaModelGeneratorConfiguration().getTargetPackage(), ".") +
                                ".pojo.vo."
                                + this.getDomainObjectName() + "VO";
                        relationGeneratorConfiguration.setVoModelTye(voType);
                    } else {
                        relationGeneratorConfiguration.setVoModelTye(sb.toString());
                    }
                    sb.setLength(0);
                    sb.append(this.getJavaClientGeneratorConfiguration().getTargetPackage()).append(".");
                    sb.append(this.getDomainObjectName()).append("Mapper").append(".");
                    sb.append(JavaBeansUtil.byColumnMethodName(Collections.singletonList(column)));
                    relationGeneratorConfiguration.setSelect(sb.toString());
                    relationGeneratorConfiguration.setType(RelationTypeEnum.collection);
                    this.addRelationGeneratorConfiguration(relationGeneratorConfiguration);
                }
            });


        }
    }


    //更新TC与表结构相关的自定义属性
    private void updateTableConfiguration(final IntrospectedTable introspectedTable) {
        String property = introspectedTable.getContext().getProperty(PropertyRegistry.ANY_HTML_HIDDEN_COLUMNS);
        final List<String> hiddenColumnNames = new ArrayList<>();
        if (stringHasValue(property)) {
            hiddenColumnNames.addAll(spiltToSet(property));
        }
        String property1 = introspectedTable.getTableConfiguration().getProperty(PropertyRegistry.ANY_HTML_HIDDEN_COLUMNS);
        if (stringHasValue(property1)) {
            hiddenColumnNames.addAll(spiltToSet(property1));
        }

        introspectedTable.getAllColumns().forEach(column -> {
            //1、更新tableConfiguration的FieldNames、ColumnNames列表
            this.getFieldNames().add(column.getJavaProperty());
            this.getColumnNames().add(column.getActualColumnName());

            //2、更新context、table中htmlHiddenColumns列表
            if (hiddenColumnNames.contains(column.getActualColumnName())) {
                this.getHtmlHiddenColumns().add(column);
            }

            //3、更新HTMLHiddenColumns列表
            this.getHtmlMapGeneratorConfigurations()
                    .forEach(htmlGeneratorConfiguration -> htmlGeneratorConfiguration.getHiddenColumns().addAll(hiddenColumnNames));

            //3、根据自定义的列长度属性，更新introspectedTable的列长度
            ColumnOverride columnOverride = this.getColumnOverride(column.getActualColumnName());
            if (columnOverride != null) {
                column.setLength(columnOverride.getMaxLength() != null && columnOverride.getMaxLength() <= column.getLength() ? columnOverride.getMaxLength() : column.getLength());
                column.setScale(columnOverride.getScale() != null ? columnOverride.getScale() : column.getScale());
                column.setMinLength(columnOverride.getMinLength() != null && columnOverride.getMinLength() > 0 && columnOverride.getMinLength() <= column.getLength() ? columnOverride.getMinLength() : 0);
            }
            //4、设置是否需要验证的属性
            if ((column.isNullable() || column.getLength()>0)
                    && !column.isAutoIncrement()
                    && !this.getValidateIgnoreColumns().contains(column.getActualColumnName())) {
                column.setBeValidated(true);
            }
        });
    }

    private void calculateSelectBySqlMethodProperty(IntrospectedTable introspectedTable) {
        //计算SelectBySqlMethod列
        for (SelectBySqlMethodGeneratorConfiguration bySqlMethodGeneratorConfiguration : this.getSelectBySqlMethodGeneratorConfigurations()) {
            bySqlMethodGeneratorConfiguration.setParentIdColumn(introspectedTable.getColumn(bySqlMethodGeneratorConfiguration.getParentIdColumnName()).orElse(null));
            bySqlMethodGeneratorConfiguration.setPrimaryKeyColumn(introspectedTable.getColumn(bySqlMethodGeneratorConfiguration.getPrimaryKeyColumnName()).orElse(null));
        }
        List<SelectBySqlMethodGeneratorConfiguration> collect = this.getSelectBySqlMethodGeneratorConfigurations().stream()
                .peek(c -> {
                    c.setParentIdColumn(introspectedTable.getColumn(c.getParentIdColumnName()).orElse(null));
                    c.setPrimaryKeyColumn(introspectedTable.getColumn(c.getPrimaryKeyColumnName()).orElse(null));
                })
                .filter(c -> c.getPrimaryKeyColumn() != null && c.getParentIdColumn() != null)
                .collect(Collectors.toList());
        this.setSelectBySqlMethodGeneratorConfigurations(collect);

        //生成SelectByTable基于关系表主键的查询方法
        if (this.getSelectByTableGeneratorConfiguration().size() > 0) {
            for (SelectByTableGeneratorConfiguration selectByTableGeneratorConfiguration : this.getSelectByTableGeneratorConfiguration()) {
                selectByTableGeneratorConfiguration.setParameterName(JavaBeansUtil.getCamelCaseString(selectByTableGeneratorConfiguration.getOtherPrimaryKeyColumn(), false));
            }
        }
    }

    private void calculateSelectByColumnConfigurations(IntrospectedTable introspectedTable) {
        //生成selectByColumn查询方法
        if (this.getSelectByColumnGeneratorConfigurations().size() > 0) {
            for (SelectByColumnGeneratorConfiguration configuration : this.getSelectByColumnGeneratorConfigurations()) {
                configuration.getColumnNames().forEach(n -> introspectedTable.getColumn(n).ifPresent(configuration::addColumn));
                configuration.setMethodName(JavaBeansUtil.byColumnMethodName(configuration.getColumns()) + (configuration.getParameterList() ? "s" : ""));
                configuration.setDeleteMethodName(JavaBeansUtil.deleteByColumnMethodName(configuration.getColumns()) + (configuration.getParameterList() ? "s" : ""));
            }
        }
        List<SelectByColumnGeneratorConfiguration> collect1 = this.getSelectByColumnGeneratorConfigurations().stream().distinct().collect(Collectors.toList());
        this.setSelectByColumnGeneratorConfigurations(collect1);
    }

    private void calculateSelectBaseByPrimaryKeyConfig(IntrospectedTable introspectedTable) {
        if (this.getRelationGeneratorConfigurations().stream().anyMatch(RelationGeneratorConfiguration::isSubSelected)) {
            //追加一个基于主键的查询，用来区分selectByPrimaryKey方法，避免过多查询
            if (introspectedTable.getPrimaryKeyColumns().size() == 0) return;
            final String methodName = "selectBaseByPrimaryKey";
            if (this.getSelectByColumnGeneratorConfigurations().stream().noneMatch(t -> methodName.equals(t.getMethodName()))) {
                SelectByColumnGeneratorConfiguration selectByColumnGeneratorConfiguration = new SelectByColumnGeneratorConfiguration();
                for (IntrospectedColumn column : introspectedTable.getPrimaryKeyColumns()) {
                    if (selectByColumnGeneratorConfiguration.addColumnName(column.getActualColumnName()))
                        selectByColumnGeneratorConfiguration.addColumn(column);
                }
                selectByColumnGeneratorConfiguration.setMethodName(methodName);
                selectByColumnGeneratorConfiguration.setEnableDelete(false);
                selectByColumnGeneratorConfiguration.setReturnType(1);
                this.getSelectByColumnGeneratorConfigurations().add(selectByColumnGeneratorConfiguration);
            }
        }
    }

    protected void calculateHtmlAttributes(IntrospectedTable introspectedTable) {
        //重新计算不为空字段，根据数据库字段不为空属性，追加数据库表不允许空的字段
        introspectedTable.getAllColumns().stream()
                .filter(c -> !c.isNullable())
                .map(IntrospectedColumn::getActualColumnName)
                .forEach(c -> this.getHtmlMapGeneratorConfigurations()
                        .forEach(htmlConfiguration -> htmlConfiguration.getElementRequired().add(c)));
        //获取所有HtmlGeneratorConfiguration中所有的HtmlElementDescriptor
        this.getHtmlMapGeneratorConfigurations().forEach(htmlConfiguration -> htmlConfiguration.getElementDescriptors().forEach(elementDescriptor -> {
            //如果tagType为select的，需要计算dataFormat对应的数据
            //如果elementDescriptor的otherFieldName为null，则通过当前字段属性计算补齐otherFieldName
            if (!stringHasValue(elementDescriptor.getOtherFieldName())) {
                introspectedTable.getColumn(elementDescriptor.getName())
                        .ifPresent(c -> elementDescriptor.setOtherFieldName(ConfigUtil.getOverrideJavaProperty(c.getJavaProperty())));
            }
            //添加附件属性
            if (elementDescriptor.getOtherFieldName() != null) {
                addHtmlElementAddtionalAttribute(elementDescriptor, introspectedTable);
            }
        }));

    }

    private void addHtmlElementAddtionalAttribute(HtmlElementDescriptor elementDescriptor, IntrospectedTable introspectedTable) {
        //如果已经存在，不再追加
        if (ConfigUtil.javaPropertyExist(elementDescriptor.getOtherFieldName(), introspectedTable) || !VStringUtil.stringHasValue(elementDescriptor.getDataSource())) {
            return;
        }
        //javaModel或者voModel中不存在对应的属性，需要追加
        OverridePropertyValueGeneratorConfiguration overrideConfiguration = ConfigUtil.createOverridePropertyConfiguration(elementDescriptor, introspectedTable);
        if (this.getVoGeneratorConfiguration() != null) {
            this.getVoGeneratorConfiguration().getOverridePropertyConfigurations().add(overrideConfiguration);
        } else {
            this.getJavaModelGeneratorConfiguration().getOverridePropertyConfigurations().add(overrideConfiguration);
        }
    }

    private void calculateSelectByTableProperty() {
        this.getSelectByTableGeneratorConfiguration().forEach(c -> {
            IntrospectedColumn thisColumn = new IntrospectedColumn();
            thisColumn.setFullyQualifiedJavaType(FullyQualifiedJavaType.getStringInstance());
            thisColumn.setJavaProperty(JavaBeansUtil.getCamelCaseString(c.getPrimaryKeyColumn(), false));
            thisColumn.setJdbcTypeName("VARCHAR");
            thisColumn.setRemarks("当前对象主键标识");
            thisColumn.setActualColumnName(c.getPrimaryKeyColumn());
            c.setThisColumn(thisColumn);

            IntrospectedColumn otherColumn = new IntrospectedColumn();
            otherColumn.setFullyQualifiedJavaType(FullyQualifiedJavaType.getStringInstance());
            otherColumn.setJavaProperty(JavaBeansUtil.getCamelCaseString(c.getOtherPrimaryKeyColumn(), false));
            otherColumn.setJdbcTypeName("VARCHAR");
            otherColumn.setRemarks("关联数据表的主键标识");
            otherColumn.setActualColumnName(c.getOtherPrimaryKeyColumn());
            c.setOtherColumn(otherColumn);
        });
    }
}
