package org.mybatis.generator.config;

import com.vgosoft.core.constant.enums.core.*;
import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementDataSourceEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementTagTypeEnum;
import com.vgosoft.core.db.query.DatabaseType;
import com.vgosoft.tool.core.VArrayUtil;
import com.vgosoft.tool.core.VCollectionUtil;
import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.config.factory.*;
import org.mybatis.generator.custom.enums.RelationTypeEnum;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.Mb3GenUtil;
import org.mybatis.generator.internal.util.messages.Messages;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.mybatis.generator.internal.util.StringUtility.*;

/**
 * @author cen_c
 */
@Setter
@Getter
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

    private final List<ColumnOverride> columnOverrides = new ArrayList<>();

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

    private final TreeSet<RelationGeneratorConfiguration> relationGeneratorConfigurations = new TreeSet<>(Comparator.comparing(RelationGeneratorConfiguration::getColumn));

    private final List<HtmlGeneratorConfiguration> htmlGeneratorConfigurations = new ArrayList<>();

    private JavaServiceGeneratorConfiguration javaServiceGeneratorConfiguration;

    private JavaServiceImplGeneratorConfiguration javaServiceImplGeneratorConfiguration;

    private JavaControllerGeneratorConfiguration javaControllerGeneratorConfiguration;

    private JavaModelGeneratorConfiguration javaModelGeneratorConfiguration;

    private SqlMapGeneratorConfiguration sqlMapGeneratorConfiguration;

    private JavaClientGeneratorConfiguration javaClientGeneratorConfiguration;

    private SqlSchemaGeneratorConfiguration sqlSchemaGeneratorConfiguration;

    private VoGeneratorConfiguration voGeneratorConfiguration;

    private VoCacheGeneratorConfiguration voCacheGeneratorConfiguration;

    private final List<IgnoredColumnPattern> ignoredColumnPatterns = new ArrayList<>();

    private String serviceApiBasePath;

    private String htmlBasePath;

    private Set<String> validateIgnoreColumns = new HashSet<>();

    private final Set<String> htmlHiddenFields = new HashSet<>();

    private final Set<String> htmlReadonlyFields = new HashSet<>();

    private final Set<String> htmlDisplayOnlyFields = new HashSet<>();

    private final List<OverridePropertyValueGeneratorConfiguration> overridePropertyConfigurations = new ArrayList<>();

    private final List<VoAdditionalPropertyGeneratorConfiguration> additionalPropertyConfigurations = new ArrayList<>();

    private Set<String> enableDropTables = new HashSet<>();

    private boolean cleanAllGeneratedElements = false;

    public TreeSet<DatabaseType> additionalDbTypes = new  TreeSet<>();

    private List<String> warnings;

    public TableConfiguration(Context context, List<String> warnings) {
        super();
        this.modelType = context.getDefaultModelType();
        this.warnings = warnings;
        // initialize properties
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

    public boolean isColumnIgnored(String columnName) {
        for (Map.Entry<IgnoredColumn, Boolean> entry : ignoredColumns.entrySet()) {
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

        if (!(obj instanceof TableConfiguration other)) {
            return false;
        }

        return Objects.equals(this.catalog, other.catalog) && Objects.equals(this.schema, other.schema) && Objects.equals(this.tableName, other.tableName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(catalog, schema, tableName);
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

    public boolean areAnyStatementsEnabled() {
        return selectByExampleStatementEnabled || selectByPrimaryKeyStatementEnabled || insertStatementEnabled || updateByPrimaryKeyStatementEnabled || deleteByExampleStatementEnabled || deleteByPrimaryKeyStatementEnabled || countByExampleStatementEnabled || updateByExampleStatementEnabled;
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

        for (Map.Entry<IgnoredColumn, Boolean> entry : ignoredColumns.entrySet()) {
            if (Boolean.FALSE.equals(entry.getValue())) {
                answer.add(entry.getKey().getColumnName());
            }
        }

        return answer;
    }

    public void setConfiguredModelType(String configuredModelType) {
        this.modelType = ModelType.getModelType(configuredModelType);
    }

    @Override
    public String toString() {
        return composeFullyQualifiedTableName(catalog, schema, tableName, '.');
    }

    public void validate(List<String> errors, int listPosition) {
        if (!stringHasValue(tableName)) {
            errors.add(Messages.getString("ValidationError.6", Integer.toString(listPosition)));
        }

        String fqTableName = composeFullyQualifiedTableName(catalog, schema, tableName, '.');

        if (generatedKey != null) {
            generatedKey.validate(errors, fqTableName);
        }

        // when using column indexes, either both or neither query ids
        // should be set
        if (isTrue(getProperty(PropertyRegistry.TABLE_USE_COLUMN_INDEXES)) && selectByExampleStatementEnabled && selectByPrimaryKeyStatementEnabled) {
            boolean queryId1Set = stringHasValue(selectByExampleQueryId);
            boolean queryId2Set = stringHasValue(selectByPrimaryKeyQueryId);

            if (queryId1Set != queryId2Set) {
                errors.add(Messages.getString("ValidationError.13", fqTableName));
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

    public void addSelectByColumnGeneratorConfiguration(SelectByColumnGeneratorConfiguration selectByColumnGeneratorConfiguration) {
        this.selectByColumnGeneratorConfigurations.add(selectByColumnGeneratorConfiguration);
    }

    public void addSelectBySqlMethodGeneratorConfiguration(SelectBySqlMethodGeneratorConfiguration selectBySqlMethodGeneratorConfiguration) {
        this.selectBySqlMethodGeneratorConfigurations.add(selectBySqlMethodGeneratorConfiguration);
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

    public void reprocessConfiguration(List<String> warnings, AbstractIntrospectedTable introspectedTable, Context context) {
        //根据配置更新introspectedTable的voModelType、voCreateType属性
        updateVoModelType(introspectedTable);

        // 处理复用元素的配置
        calculateReusableElementsConfiguration(introspectedTable, context, warnings);

        //更新TC与表结构相关的自定义属性
        updateTableConfiguration(introspectedTable);
        //检查并更新queryColumnConfiguration的表名及别名配置
        updateQueryColumnConfiguration(introspectedTable);
        //更新context、table中隐藏列名htmlHiddenFields 列表，计算隐藏列，只读列htmlReadonlyFields和只显示htmlDisplayOnlyFields列
        calculateHiddenReadDisplayOnlyColumns(introspectedTable);
        //对指定了dataFormat的快捷配置进行转换
        convertDataFormatConfigurations(introspectedTable);
        //为已经配置存在的html元素描述器赋值introspectedColumn,自动配置dataUrl
        assignHtmlIntrospectedColumn(introspectedTable, warnings);
        //根据表state_、parent_id和注释以”是否“开头的字段，自动生成HtmlElementDescriptor配置文件
        generateDefaultElementDescriptor(introspectedTable);
        //根据默认字段配置，生成默认的dataTables列表中column渲染默认配置
        generateDefaultColumnRenderConfiguration(context);
        //计算页内列表元素描述,包括：htmlHiddenFields、requiredColumns
        calculateInnerListElementDescriptor(introspectedTable);

        //批量处理overrideProperty的配置文档：继承、去重
        //需要先完成整理，才能后续追加自动创建的配置。
        processOverrideProperty(introspectedTable);
        //为所有的html元素描述器生成OverrideProperty属性，追加到最下层
        generateDescriptorOverrideProperty(introspectedTable);
        //生成默认的overrideProperty，追加到最下层
        generateDefaultOverrideProperty(introspectedTable);

        //计算selectByTable配置
        calculateSelectByTableProperty();
        //计算所有的selectByColumn配置，去重、计算列信息
        calculateSelectByColumnConfigurations(introspectedTable);
        //增加SelectBaseByPrimaryKey配置
        calculateSelectBaseByPrimaryKeyConfig(introspectedTable);
        //计算StatisticsGeneratorConfiguration统计方法
        calculateStatisticsGeneratorConfiguration(introspectedTable);
        calculateStatisticsCustomGeneratorConfiguration(introspectedTable);

        //根据所有配置信息，进行调整
        //enableChildren.
        calculateSelectByParentIdConfig(warnings, introspectedTable);
        //如果存在parent_id字段，则自动添加children属性，检查是否存在is_leaf字段、disable字段，如果不存在则添加
        calculateChildrenRelationConfig(introspectedTable);
        //计算selectBySql配置
        calculateSelectBySqlMethodProperty(introspectedTable);
        //计算附件属性，把全局配置分配到model和vo的配置上。
        calculateAdditionalProperty();
    }

    private void calculateReusableElementsConfiguration(AbstractIntrospectedTable introspectedTable, Context context, List<String> warnings) {
        ReusableElementsConfiguration reusableElementsConfiguration = context.getReusableElementsConfiguration();
        if (reusableElementsConfiguration.getExcludedTables().contains(this.tableName)) {
            return;
        }
        // 处理htmlElementDescriptors
        List<HtmlElementDescriptor> reusableHtmlElementDescriptors = reusableElementsConfiguration.getHtmlElementDescriptors(introspectedTable);
        if (!reusableHtmlElementDescriptors.isEmpty() && !introspectedTable.getTableConfiguration().getHtmlGeneratorConfigurations().isEmpty()) {
            introspectedTable.getTableConfiguration().getHtmlGeneratorConfigurations().forEach(htmlGeneratorConfiguration -> {
                Collection<HtmlElementDescriptor> htmlElementDescriptors = VArrayUtil.merge(htmlGeneratorConfiguration.getElementDescriptors(), reusableHtmlElementDescriptors, false);
                htmlElementDescriptors.forEach(htmlElementDescriptor -> {
                    if (htmlElementDescriptor.getHtmlGeneratorConfiguration() == null) {
                        htmlElementDescriptor.setHtmlGeneratorConfiguration(htmlGeneratorConfiguration);
                    }
                });
                htmlGeneratorConfiguration.getElementDescriptors().clear();
                htmlGeneratorConfiguration.getElementDescriptors().addAll(htmlElementDescriptors);
            });
        }


        // 处理overridePropertyValues
        List<OverridePropertyValueGeneratorConfiguration> reusableOverrideProperty = reusableElementsConfiguration.getOverridePropertyValueGeneratorConfigurations(introspectedTable);
        if (!reusableOverrideProperty.isEmpty()) {
            if (this.getVoGeneratorConfiguration() != null
                    && this.getVoGeneratorConfiguration().isGenerate()) {
                VoGeneratorConfiguration voGeneratorConfiguration = this.getVoGeneratorConfiguration();
                Collection<OverridePropertyValueGeneratorConfiguration> configurations = VArrayUtil.merge(voGeneratorConfiguration.getOverridePropertyConfigurations(), reusableOverrideProperty, false);
                voGeneratorConfiguration.getOverridePropertyConfigurations().clear();
                voGeneratorConfiguration.getOverridePropertyConfigurations().addAll(configurations);
            } else {
                JavaModelGeneratorConfiguration javaModelGeneratorConfiguration = this.getJavaModelGeneratorConfiguration();
                Collection<OverridePropertyValueGeneratorConfiguration> configurations = VArrayUtil.merge(javaModelGeneratorConfiguration.getOverridePropertyConfigurations(), reusableOverrideProperty, false);
                javaModelGeneratorConfiguration.getOverridePropertyConfigurations().clear();
                javaModelGeneratorConfiguration.getOverridePropertyConfigurations().addAll(configurations);
            }
        }

        // 处理listColumnDescriptors和filterColumnDescriptors
        if (this.getVoGeneratorConfiguration() != null
                && this.getVoGeneratorConfiguration().isGenerate()
                && this.getVoGeneratorConfiguration().getVoViewConfiguration() != null
                && this.getVoGeneratorConfiguration().getVoViewConfiguration().isGenerate()) {
            // 处理listColumnDescriptors
            if (!reusableElementsConfiguration.getListColumnDescriptors().isEmpty()) {
                List<InnerListViewConfiguration> innerListViewConfigurations = this.getVoGeneratorConfiguration().getVoViewConfiguration().getInnerListViewConfigurations();
                innerListViewConfigurations.forEach(innerListViewConfiguration -> {
                    Collection<ListColumnConfiguration> listColumnConfigurations = VArrayUtil.merge(innerListViewConfiguration.getListColumnConfigurations(), reusableElementsConfiguration.getListColumnConfigurations(innerListViewConfiguration, introspectedTable), false);
                    innerListViewConfiguration.getListColumnConfigurations().clear();
                    innerListViewConfiguration.getListColumnConfigurations().addAll(listColumnConfigurations);
                });
            }
            // 处理filterColumnDescriptors
            if (!reusableElementsConfiguration.getFilterColumnDescriptors().isEmpty()) {
                List<FilterColumnConfiguration> filterColumnsConfigurations = this.getVoGeneratorConfiguration().getVoViewConfiguration().getFilterColumnsConfigurations();
                Collection<FilterColumnConfiguration> filterColumnConfigurations = VArrayUtil.merge(filterColumnsConfigurations, reusableElementsConfiguration.getFilterColumnConfigurations(introspectedTable), false);
                filterColumnsConfigurations.clear();
                filterColumnsConfigurations.addAll(filterColumnConfigurations);
            }
        }
    }

    private void calculateStatisticsGeneratorConfiguration(AbstractIntrospectedTable abstractIntrospectedTable) {
        JavaServiceImplGeneratorConfiguration serviceImplGeneratorConfiguration = abstractIntrospectedTable.getTableConfiguration().getJavaServiceImplGeneratorConfiguration();
        if (serviceImplGeneratorConfiguration == null) {
            return;
        }
        if (serviceImplGeneratorConfiguration.getStatisticsConfigurations().isEmpty()) {
            return;
        }
        // 时间维度统计字段验证、计算方法名称
        for (StatisticsDateDimGeneratorConfiguration statisticsConfiguration : serviceImplGeneratorConfiguration.getStatisticsConfigurations()) {
            if (statisticsConfiguration.getStatColumns().isEmpty() || !VStringUtil.stringHasValue(statisticsConfiguration.getDateDimension())) {
                continue;
            }
            IntrospectedColumn statColumn = abstractIntrospectedTable.getColumn(statisticsConfiguration.getStatColumns().get(0)).orElse(null);
            IntrospectedColumn joinColumn = abstractIntrospectedTable.getColumn(statisticsConfiguration.getDateDimColumn()).orElse(null);
            if (statColumn == null || joinColumn == null) {
                this.warnings.add("警告：表 " + abstractIntrospectedTable.getFullyQualifiedTable() + " 的 StatisticsGeneratorConfiguration 配置无效，统计字段" + statisticsConfiguration.getStatColumns().get(0) + "或时间维度字段" + statisticsConfiguration.getDateDimColumn() + "在表中不存在，已被忽略。");
                continue;
            }
            List<String> nameList = Arrays.asList(statisticsConfiguration.getStatType(),
                    statColumn.getJavaProperty(),
                    joinColumn.getJavaProperty(),
                    statisticsConfiguration.getDateDimension());
            String methodName = nameList.stream().map(VStringUtil::getFirstCharacterUppercase).collect(Collectors.joining(""));
            if (VStringUtil.stringHasValue(statisticsConfiguration.getNameSuffix())) {
                methodName += statisticsConfiguration.getNameSuffix();
            }
            statisticsConfiguration.setMethodName("stat" + methodName);
        }
        Set<StatisticsDateDimGeneratorConfiguration> configurationList = serviceImplGeneratorConfiguration.getStatisticsConfigurations().stream()
                .filter(configuration -> stringHasValue(configuration.getMethodName())).collect(Collectors.toSet());
        serviceImplGeneratorConfiguration.setStatisticsConfigurations(configurationList);
    }

    private void calculateStatisticsCustomGeneratorConfiguration(AbstractIntrospectedTable abstractIntrospectedTable) {
        JavaServiceImplGeneratorConfiguration serviceImplGeneratorConfiguration = abstractIntrospectedTable.getTableConfiguration().getJavaServiceImplGeneratorConfiguration();
        if (serviceImplGeneratorConfiguration == null) {
            return;
        }
        if (serviceImplGeneratorConfiguration.getStatisticsCustomConfigurations().isEmpty()) {
            return;
        }
        // 时间维度统计字段验证、计算方法名称
        for (StatisticsCustomGeneratorConfiguration statisticsConfiguration : serviceImplGeneratorConfiguration.getStatisticsCustomConfigurations()) {
            if (statisticsConfiguration.getStatColumns().isEmpty()) {
                continue;
            }
            IntrospectedColumn statColumn = abstractIntrospectedTable.getColumn(statisticsConfiguration.getStatColumns().get(0)).orElse(null);
            if (statColumn == null) {
                this.warnings.add("警告：表 " + abstractIntrospectedTable.getFullyQualifiedTable() + " 的 StatisticsGeneratorConfiguration 配置无效，统计字段" + statisticsConfiguration.getStatColumns().get(0) + "在表中不存在，已被忽略。");
                continue;
            }
            List<JoinTableClauseGeneratorConfiguration> joinTableConfigurations = statisticsConfiguration.getJoinTableClauseGeneratorConfigurations();
            String tableNames = joinTableConfigurations.stream().map(c -> VStringUtil.toCamelCase(c.getJoinTable())).collect(Collectors.joining());
            List<String> nameList = Arrays.asList(statisticsConfiguration.getStatType(),
                    statColumn.getJavaProperty(),
                    tableNames);
            String methodName = nameList.stream().map(VStringUtil::getFirstCharacterUppercase).collect(Collectors.joining());
            if (VStringUtil.stringHasValue(statisticsConfiguration.getNameSuffix())) {
                methodName += statisticsConfiguration.getNameSuffix();
            }
            statisticsConfiguration.setMethodName("statCustom" + methodName);
        }
        Set<StatisticsCustomGeneratorConfiguration> configurationList = serviceImplGeneratorConfiguration.getStatisticsCustomConfigurations().stream()
                .filter(configuration -> stringHasValue(configuration.getMethodName())).collect(Collectors.toSet());
        serviceImplGeneratorConfiguration.setStatisticsCustomConfigurations(configurationList);
    }

    private void calculateAdditionalProperty() {
        this.getAdditionalPropertyConfigurations().forEach(additionalPropertyConfiguration -> {
            VCollectionUtil.addIfNotContains(this.getJavaModelGeneratorConfiguration().getAdditionalPropertyConfigurations(), additionalPropertyConfiguration);
            if (this.getVoGeneratorConfiguration().getVoModelConfiguration() != null) {
                this.getVoGeneratorConfiguration().getVoModelConfiguration().getAdditionalPropertyConfigurations().add(additionalPropertyConfiguration);
            }
        });
    }

    private List<QueryColumnConfiguration> getViewQueryColumnConfigurations() {
        return Optional.ofNullable(this.getVoGeneratorConfiguration())
                .flatMap(voGeneratorConfiguration -> Optional.ofNullable(voGeneratorConfiguration.getVoViewConfiguration()))
                .flatMap(voViewConfiguration -> Optional.ofNullable(voViewConfiguration.getQueryColumnConfigurations()))
                .orElse(new ArrayList<>());
    }

    private void updateQueryColumnConfiguration(AbstractIntrospectedTable abstractIntrospectedTable) {

        //viewVo中，为QueryColumnConfiguration赋值IntrospectedColumn，清除列不存在IntrospectedColumn的配置
        List<QueryColumnConfiguration> queryColumnConfigurations = getViewQueryColumnConfigurations();
        queryColumnConfigurations.forEach(queryColumnConfiguration -> queryColumnConfiguration.setIntrospectedColumn(abstractIntrospectedTable.getColumn(queryColumnConfiguration.getColumn()).orElse(null)));
        queryColumnConfigurations.removeIf(queryColumnConfiguration -> queryColumnConfiguration.getIntrospectedColumn() == null);

        //innerList中，为QueryColumnConfiguration赋值IntrospectedColumn，清除列不存在IntrospectedColumn的配置
        List<InnerListViewConfiguration> innerListViewConfigurations = Optional.ofNullable(this.getVoGeneratorConfiguration())
                .flatMap(voGeneratorConfiguration -> Optional.ofNullable(voGeneratorConfiguration.getVoViewConfiguration()))
                .flatMap(voViewConfiguration -> Optional.ofNullable(voViewConfiguration.getInnerListViewConfigurations()))
                .orElse(new ArrayList<>());
        innerListViewConfigurations.forEach(innerListViewConfiguration -> {
            innerListViewConfiguration.getQueryColumnConfigurations().forEach(queryColumnConfiguration -> queryColumnConfiguration.setIntrospectedColumn(abstractIntrospectedTable.getColumn(queryColumnConfiguration.getColumn()).orElse(null)));
            innerListViewConfiguration.getQueryColumnConfigurations().removeIf(queryColumnConfiguration -> queryColumnConfiguration.getIntrospectedColumn() == null);
        });

        //校验searchColumn
        if (this.getVoGeneratorConfiguration() != null && this.getVoGeneratorConfiguration().getVoViewConfiguration() != null) {
            if (!this.getVoGeneratorConfiguration().getVoViewConfiguration().getFuzzyColumns().isEmpty()) {
                List<String> collect = this.getVoGeneratorConfiguration().getVoViewConfiguration().getFuzzyColumns().stream()
                        .map(abstractIntrospectedTable::getColumn)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(IntrospectedColumn::getActualColumnName)
                        .collect(Collectors.toList());
                this.getVoGeneratorConfiguration().getVoViewConfiguration().setFuzzyColumns(collect);
            }
            if (!this.getVoGeneratorConfiguration().getVoViewConfiguration().getInnerListViewConfigurations().isEmpty()) {
                this.getVoGeneratorConfiguration().getVoViewConfiguration().getInnerListViewConfigurations().forEach(innerListViewConfiguration -> {
                    if (!innerListViewConfiguration.getFuzzyColumns().isEmpty()) {
                        List<String> collect = innerListViewConfiguration.getFuzzyColumns().stream()
                                .map(abstractIntrospectedTable::getColumn)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .map(IntrospectedColumn::getActualColumnName)
                                .collect(Collectors.toList());
                        innerListViewConfiguration.setFuzzyColumns(collect);
                    }
                });
            }
        }

        //校验filterColumn
        if (this.getVoGeneratorConfiguration() != null && this.getVoGeneratorConfiguration().getVoViewConfiguration() != null) {
            if (!this.getVoGeneratorConfiguration().getVoViewConfiguration().getFilterColumns().isEmpty()) {
                List<String> collect = this.getVoGeneratorConfiguration().getVoViewConfiguration().getFilterColumns().stream()
                        .map(abstractIntrospectedTable::getColumn)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(IntrospectedColumn::getActualColumnName)
                        .collect(Collectors.toList());
                this.getVoGeneratorConfiguration().getVoViewConfiguration().setFilterColumns(collect);
            }
            if (!this.getVoGeneratorConfiguration().getVoViewConfiguration().getInnerListViewConfigurations().isEmpty()) {
                this.getVoGeneratorConfiguration().getVoViewConfiguration().getInnerListViewConfigurations().forEach(innerListViewConfiguration -> {
                    if (!innerListViewConfiguration.getFilterColumns().isEmpty()) {
                        List<String> collect = innerListViewConfiguration.getFilterColumns().stream()
                                .map(abstractIntrospectedTable::getColumn)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .map(IntrospectedColumn::getActualColumnName)
                                .collect(Collectors.toList());
                        innerListViewConfiguration.setFilterColumns(collect);
                    }
                });
            }
        }

    }

    private void generateDefaultColumnRenderConfiguration(Context context) {
        if (this.getVoGeneratorConfiguration() == null || !this.getVoGeneratorConfiguration().isGenerate()
                || this.getVoGeneratorConfiguration().getVoViewConfiguration() == null
                || !this.getVoGeneratorConfiguration().getVoViewConfiguration().isGenerate()) {
            return;
        }
        List<VoColumnRenderFunGeneratorConfiguration> funGeneratorConfigurationList = this.getVoGeneratorConfiguration().getVoViewConfiguration().getVoColumnRenderFunGeneratorConfigurations();
        //获得funGeneratorConfigurationList中所有的fieldNames
        Set<String> fields = funGeneratorConfigurationList
                .stream()
                .map(VoColumnRenderFunGeneratorConfiguration::getFieldNames)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        //获得DefaultColumnNameEnum枚举的所有常量
        this.fieldNames
                .forEach(columnName -> {
                    if (!fields.contains(columnName)) {
                        DefaultColumnNameEnum.ofFieldName(columnName)
                                .ifPresent(defaultColumnNameEnum -> {
                                    if (VStringUtil.stringHasValue(defaultColumnNameEnum.columnRender())) {
                                        VoColumnRenderFunGeneratorConfiguration funGeneratorConfiguration = new VoColumnRenderFunGeneratorConfiguration(context, this);
                                        funGeneratorConfiguration.setFieldNames(Collections.singletonList(defaultColumnNameEnum.fieldName()));
                                        funGeneratorConfiguration.setRenderFun(defaultColumnNameEnum.columnRender());
                                        funGeneratorConfigurationList.add(funGeneratorConfiguration);
                                    }
                                });
                    }
                });
    }

    /**
     * 更新introspectedTable的VoModel和VoCreate属性的类名，用于生成TableMeta的参数
     *
     * @param abstractIntrospectedTable introspectedTable对象
     */
    private void updateVoModelType(AbstractIntrospectedTable abstractIntrospectedTable) {
        if (this.getVoGeneratorConfiguration() != null && this.getVoGeneratorConfiguration().getVoModelConfiguration() != null) {
            abstractIntrospectedTable.setVoModelType(this.getVoGeneratorConfiguration().getVoModelConfiguration().getFullyQualifiedJavaType());
        }
        if (this.getVoGeneratorConfiguration() != null && this.getVoGeneratorConfiguration().getVoCreateConfiguration() != null) {
            abstractIntrospectedTable.setVoCreateType(this.getVoGeneratorConfiguration().getVoCreateConfiguration().getFullyQualifiedJavaType());
        }
    }

    /**
     * 为所有的html元素描述器生成OverrideProperty属性
     *
     * @param abstractIntrospectedTable introspectedTable对象
     */
    private void generateDescriptorOverrideProperty(AbstractIntrospectedTable abstractIntrospectedTable) {
        // 为所有的html元素描述器生成OverrideProperty属性
        this.getHtmlMapGeneratorConfigurations()
                .forEach(htmlMapGeneratorConfiguration -> htmlMapGeneratorConfiguration.getElementDescriptors()
                        .forEach(elementDescriptor -> {
                            //添加附件属性
                            if (elementDescriptor.getOtherFieldName() != null && !elementDescriptor.getColumn().getJavaProperty().equals(elementDescriptor.getOtherFieldName())) {
                                addHtmlElementAdditionalAttribute(elementDescriptor, abstractIntrospectedTable);
                            }
                        }));
        // 为页内列表的所有html元素描述器生成OverrideProperty属性
        List<InnerListViewConfiguration> innerListViewConfigurations = Optional.ofNullable(this.getVoGeneratorConfiguration())
                .flatMap(voGeneratorConfiguration -> Optional.ofNullable(voGeneratorConfiguration.getVoViewConfiguration()))
                .flatMap(voViewConfiguration -> Optional.ofNullable(voViewConfiguration.getInnerListViewConfigurations()))
                .orElse(new ArrayList<>());
        innerListViewConfigurations
                .forEach(innerListViewConfiguration -> innerListViewConfiguration.getHtmlElements()
                        .forEach(elementDescriptor -> {
                            //添加附件属性
                            if (elementDescriptor.getOtherFieldName() != null && !elementDescriptor.getColumn().getJavaProperty().equals(elementDescriptor.getOtherFieldName())) {
                                addHtmlElementAdditionalAttribute(elementDescriptor, abstractIntrospectedTable);
                            }
                        }));
    }

    /**
     * 更新TC与表结构相关的自定义属性
     *
     * @param introspectedTable introspectedTable对象
     */
    private void updateTableConfiguration(final AbstractIntrospectedTable introspectedTable) {
        introspectedTable.getAllColumns().forEach(column -> {
            //1、更新tableConfiguration的FieldNames、ColumnNames列表
            this.getFieldNames().add(column.getJavaProperty());
            this.getColumnNames().add(column.getActualColumnName());

            //2、根据自定义的列长度属性，更新introspectedTable的列长度，是否需要修改注释
            ColumnOverride columnOverride = this.getColumnOverride(column.getActualColumnName());
            if (columnOverride != null) {
                column.setLength(columnOverride.getMaxLength() != null && columnOverride.getMaxLength() <= column.getLength() ? columnOverride.getMaxLength() : column.getLength());
                column.setScale(columnOverride.getScale() != null ? columnOverride.getScale() : column.getScale());
                column.setMinLength(columnOverride.getMinLength() != null && columnOverride.getMinLength() > 0 && columnOverride.getMinLength() <= column.getLength() ? columnOverride.getMinLength() : 0);
                if (stringHasValue(columnOverride.getColumnComment())) {
                    column.setRemarks(columnOverride.getColumnComment());
                }
                if (stringHasValue(columnOverride.getColumnSubComment())) {
                    column.setSubRemarks(columnOverride.getColumnSubComment());
                }
            }

            //3、设置是否需要验证的属性
            introspectedTable.getTableConfiguration().getHtmlGeneratorConfigurations().forEach(htmlGeneratorConfiguration -> htmlGeneratorConfiguration.getElementDescriptors().forEach(elementDescriptor -> {
                if (elementDescriptor.getName().equals(column.getActualColumnName()) && elementDescriptor.getVerify().contains("required")) {
                    column.setBeValidated(true);
                }
            }));
            if (isFieldNeedValidate(column, introspectedTable.getTableConfiguration().getHtmlGeneratorConfigurations())) {
                column.setBeValidated(true);
            }
        });
    }

    /**
     * 对指定了dataFormat的快捷配置进行转换
     *
     * @param abstractIntrospectedTable introspectedTable对象
     */
    private void convertDataFormatConfigurations(AbstractIntrospectedTable abstractIntrospectedTable) {
        //更新制定了dataType的html元素描述器配置
        this.getHtmlMapGeneratorConfigurations()
                .forEach(htmlGeneratorConfiguration -> htmlGeneratorConfiguration.getElementDescriptors()
                        .forEach(elementDescriptor -> {
                            if (!stringHasValue(elementDescriptor.getDataFormat())) {
                                return;
                            }
                            abstractIntrospectedTable.getColumn(elementDescriptor.getName()).ifPresent(column -> {
                                DefaultColumnNameEnum defaultColumnNameEnum = DefaultColumnNameEnum.ofColumnName(column.getActualColumnName()).orElse(null);
                                if (defaultColumnNameEnum != null) {
                                    elementDescriptor.setOtherFieldName(defaultColumnNameEnum.otherFieldName());
                                } else {
                                    elementDescriptor.setOtherFieldName(ConfigUtil.getOverrideJavaProperty(column.getJavaProperty(), elementDescriptor));
                                }
                                elementDescriptor.setColumn(column);
                                switch (elementDescriptor.getDataFormat()) {
                                    case "年":
                                    case "年月":
                                    case "年周":
                                    case "日期":
                                    case "日期时间":
                                    case "时间":
                                        if (!stringHasValue(elementDescriptor.getOtherFieldName())) {
                                            elementDescriptor.setOtherFieldName(elementDescriptor.getName());
                                        }
                                        break;
                                    case "exist":
                                    case "有":
                                    case "有无":
                                        elementDescriptor.setDataSource(HtmlElementDataSourceEnum.DICT_ENUM.getCode());
                                        elementDescriptor.setEnumClassName(ExistOrNotEnum.class.getCanonicalName());
                                        if (elementDescriptor.getTagType().equals(HtmlElementTagTypeEnum.SWITCH.codeName()) && elementDescriptor.getSwitchText() == null) {
                                            elementDescriptor.setSwitchText(ExistOrNotEnum.switchText());
                                        }
                                        elementDescriptor.setDataFormat(null);
                                        break;
                                    case "yes":
                                    case "true":
                                    case "是":
                                    case "是否":
                                        elementDescriptor.setDataSource(HtmlElementDataSourceEnum.DICT_ENUM.getCode());
                                        elementDescriptor.setEnumClassName(YesNoEnum.class.getCanonicalName());
                                        if (elementDescriptor.getTagType().equals(HtmlElementTagTypeEnum.SWITCH.codeName()) && elementDescriptor.getSwitchText() == null) {
                                            elementDescriptor.setSwitchText(YesNoEnum.switchText());
                                        }
                                        elementDescriptor.setDataFormat(null);
                                        break;
                                    case "sex":
                                    case "性别":
                                        elementDescriptor.setDataSource(HtmlElementDataSourceEnum.DICT_ENUM.getCode());
                                        elementDescriptor.setEnumClassName(GenderEnum.class.getCanonicalName());
                                        if (elementDescriptor.getTagType().equals(HtmlElementTagTypeEnum.SWITCH.codeName()) && elementDescriptor.getSwitchText() == null) {
                                            elementDescriptor.setSwitchText(GenderEnum.switchText());
                                        }
                                        elementDescriptor.setDataFormat(null);
                                        break;
                                    case "启停":
                                    case "启用停用":
                                    case "state":
                                        elementDescriptor.setDataSource(HtmlElementDataSourceEnum.DICT_ENUM.getCode());
                                        elementDescriptor.setEnumClassName(CommonStatusEnum.class.getCanonicalName());
                                        if (elementDescriptor.getTagType().equals(HtmlElementTagTypeEnum.SWITCH.codeName()) && elementDescriptor.getSwitchText() == null) {
                                            elementDescriptor.setSwitchText(CommonStatusEnum.switchText());
                                        }
                                        elementDescriptor.setDataFormat(null);
                                        break;
                                    case "急":
                                    case "缓急":
                                        elementDescriptor.setDataSource(HtmlElementDataSourceEnum.DICT_ENUM.getCode());
                                        elementDescriptor.setEnumClassName(UrgencyEnum.class.getCanonicalName());
                                        if (elementDescriptor.getTagType().equals(HtmlElementTagTypeEnum.SWITCH.codeName()) && elementDescriptor.getSwitchText() == null) {
                                            elementDescriptor.setSwitchText(UrgencyEnum.switchText());
                                        }
                                        elementDescriptor.setDataFormat(null);
                                        break;
                                    case "level":
                                    case "级别":
                                        elementDescriptor.setDataSource(HtmlElementDataSourceEnum.DICT_ENUM.getCode());
                                        elementDescriptor.setEnumClassName(LevelListEnum.class.getCanonicalName());
                                        elementDescriptor.setDataFormat(null);
                                        break;
                                    default:
                                        break;
                                }
                            });
                        }));
    }

    /**
     * 计算HtmlHiddenFields
     * 由context、table的隐藏属性配置，计算出所有的隐藏列
     * 影响的配置有：
     * 1、所有HtmlMapGeneratorConfiguration的hiddenColumns
     * 2、所有HtmlMapGeneratorConfiguration的hiddenColumnNames
     * 3、所有HtmlMapGeneratorConfiguration的elementRequired
     */
    private void calculateHiddenReadDisplayOnlyColumns(AbstractIntrospectedTable abstractIntrospectedTable) {
        Set<String> hiddenColumnNames = getHtmlAnyProperties(abstractIntrospectedTable, PropertyRegistry.ANY_HTML_HIDDEN_FIELDS);
        this.getHtmlHiddenFields().addAll(hiddenColumnNames);
        //html的隐藏列
        this.getHtmlMapGeneratorConfigurations().forEach(htmlGeneratorConfiguration -> htmlGeneratorConfiguration.getHiddenFieldNames().addAll(this.getHtmlHiddenFields()));
        //html只读列
        Set<String> readOnlyFields = getHtmlAnyProperties(abstractIntrospectedTable, PropertyRegistry.ANY_HTML_READONLY_FIELDS);
        abstractIntrospectedTable.getTableConfiguration().getHtmlReadonlyFields().addAll(readOnlyFields);
        this.getHtmlMapGeneratorConfigurations()
                .forEach(htmlGeneratorConfiguration -> htmlGeneratorConfiguration.getReadonlyFields().addAll(readOnlyFields));
        //html只显示列
        Set<String> displayOnlyFields = getHtmlAnyProperties(abstractIntrospectedTable, PropertyRegistry.ANY_HTML_DISPLAY_ONLY_FIELDS);
        abstractIntrospectedTable.getTableConfiguration().getHtmlDisplayOnlyFields().addAll(displayOnlyFields);
        this.getHtmlMapGeneratorConfigurations()
                .forEach(htmlGeneratorConfiguration -> htmlGeneratorConfiguration.getDisplayOnlyFields().addAll(displayOnlyFields));
        //html的必填列
        this.getHtmlMapGeneratorConfigurations().forEach(htmlGeneratorConfiguration -> {
            if (htmlGeneratorConfiguration.getProperties().getProperty(PropertyRegistry.ELEMENT_REQUIRED_COLUMNS) != null) {
                htmlGeneratorConfiguration.getElementRequired().addAll(VStringUtil.splitToSet(htmlGeneratorConfiguration.getProperties().getProperty(PropertyRegistry.ELEMENT_REQUIRED_COLUMNS)));
            }
            htmlGeneratorConfiguration.getElementDescriptors()
                    .forEach(elementDescriptor -> {
                        if (elementDescriptor.getVerify() != null && elementDescriptor.getVerify().contains("required")) {
                            htmlGeneratorConfiguration.getElementRequired().add(elementDescriptor.getName());
                        }
                    });
        });
        abstractIntrospectedTable.getAllColumns().forEach(column -> this.getHtmlMapGeneratorConfigurations()
                .forEach(htmlGeneratorConfiguration -> {
                    if (column.isRequired()) {
                        htmlGeneratorConfiguration.getElementRequired().add(column.getActualColumnName());
                    }
                }));

    }

    private Set<String> getHtmlAnyProperties(AbstractIntrospectedTable abstractIntrospectedTable, String propertyName) {
        Set<String> properties = new HashSet<>();
        String contextProperty = abstractIntrospectedTable.getContext().getProperty(propertyName);
        if (stringHasValue(contextProperty)) {
            properties.addAll(VStringUtil.splitToSet(contextProperty));
        }
        String tableProperty = abstractIntrospectedTable.getTableConfiguration().getProperty(propertyName);
        if (stringHasValue(tableProperty)) {
            properties.addAll(VStringUtil.splitToSet(tableProperty));
        }
        return properties;
    }

    private void generateDefaultElementDescriptor(AbstractIntrospectedTable abstractIntrospectedTable) {
        /*
         * 对于非隐藏列，根据列名自动添加页面元素
         * state_列名，自动添加页面switch（启用停用）元素
         * parent_id列名，自动添加页面select（父级）元素
         * 注释中包含“是否”，自动添加页面switch（是/否）元素
         */
        abstractIntrospectedTable.getAllColumns().stream().filter(column -> !this.getHtmlHiddenFields().contains(column.getJavaProperty()))
                .forEach(column -> this.getHtmlMapGeneratorConfigurations()
                        .forEach(htmlConfiguration -> {
                            if (htmlConfiguration.getElementDescriptors().stream().noneMatch(elementDescriptor -> elementDescriptor.getName().equals(column.getActualColumnName()))
                                    && !htmlConfiguration.getHiddenFieldNames().contains(column.getJavaProperty())) {
                                List<HtmlElementDescriptor> elementDescriptors = htmlConfiguration.getElementDescriptors();
                                HtmlElementDescriptor htmlElementDescriptor = null;
                                if (column.getActualColumnName().equalsIgnoreCase(DefaultColumnNameEnum.STATE.columnName())) {
                                    DefaultHtmlElementDescriptorFactory stateElementDescriptor = new StateElementDescriptor();
                                    htmlElementDescriptor = stateElementDescriptor.getDefaultHtmlElementDescriptor(column, abstractIntrospectedTable);
                                } else if (column.getActualColumnName().equalsIgnoreCase(DefaultColumnNameEnum.PRIORITY.columnName())) {
                                    DefaultHtmlElementDescriptorFactory priorityElementDescriptor = new PriorityElementDescriptor();
                                    htmlElementDescriptor = priorityElementDescriptor.getDefaultHtmlElementDescriptor(column, abstractIntrospectedTable);
                                } else if (column.getActualColumnName().equalsIgnoreCase(DefaultColumnNameEnum.WF_STATE.columnName())) {
                                    DefaultHtmlElementDescriptorFactory wfStateElementDescriptor = new WfStateElementDescriptor();
                                    htmlElementDescriptor = wfStateElementDescriptor.getDefaultHtmlElementDescriptor(column, abstractIntrospectedTable);
                                } else if (column.getActualColumnName().equalsIgnoreCase(DefaultColumnNameEnum.PARENT_ID.columnName())) {   //选择上级
                                    htmlElementDescriptor = new ParentIdElementDescriptor().getDefaultHtmlElementDescriptor(column, abstractIntrospectedTable);
                                } else if (column.getRemarks(true) != null && column.getRemarks(true).startsWith("是否")) { //如果列注释以“是否”开头，则自动添加页面switch元素
                                    htmlElementDescriptor = new YesNoElementDescriptor().getDefaultHtmlElementDescriptor(column, abstractIntrospectedTable);
                                } else if (column.getActualColumnName().equalsIgnoreCase(DefaultColumnNameEnum.APPLY_DEPT_ID.columnName())) {
                                    DefaultHtmlElementDescriptorFactory deptElementDescriptor = new ApplyDeptIdInputElementDescriptor();
                                    htmlElementDescriptor = deptElementDescriptor.getDefaultHtmlElementDescriptor(column, abstractIntrospectedTable);
                                } else if (column.getActualColumnName().equalsIgnoreCase(DefaultColumnNameEnum.APPLY_USER_ID.columnName())) {
                                    DefaultHtmlElementDescriptorFactory userElementDescriptor = new ApplyUserIdInputElementDescriptor();
                                    htmlElementDescriptor = userElementDescriptor.getDefaultHtmlElementDescriptor(column, abstractIntrospectedTable);
                                } else if (column.getActualColumnName().equalsIgnoreCase(DefaultColumnNameEnum.APPLY_HANDLER_ID.columnName())) {
                                    DefaultHtmlElementDescriptorFactory handlerElementDescriptor = new ApplyHandlerIdInputElementDescriptor();
                                    htmlElementDescriptor = handlerElementDescriptor.getDefaultHtmlElementDescriptor(column, abstractIntrospectedTable);
                                }
                                if (htmlElementDescriptor != null) {
                                    htmlElementDescriptor.setHtmlGeneratorConfiguration(htmlConfiguration);
                                    if (!elementDescriptors.contains(htmlElementDescriptor)) {
                                        elementDescriptors.add(htmlElementDescriptor);
                                    }
                                }
                            }
                        }));
    }

    /**
     * 批量处理overrideProperty的配置文档
     * 更新overrideProperty配置的继承关系
     * overrideProperty去重复
     */
    private void processOverrideProperty(AbstractIntrospectedTable abstractIntrospectedTable) {
        TableConfiguration tc = abstractIntrospectedTable.getTableConfiguration();
        JavaModelGeneratorConfiguration javaModelGeneratorConfig = tc.getJavaModelGeneratorConfiguration();
        VoGeneratorConfiguration voGeneratorConfig = tc.getVoGeneratorConfiguration();
        List<OverridePropertyValueGeneratorConfiguration> overrides = tc.getOverridePropertyConfigurations();
        if (voGeneratorConfig != null && voGeneratorConfig.isGenerate()) {
            VCollectionUtil.addAllIfNotContains(overrides, voGeneratorConfig.getOverridePropertyConfigurations());
            if (voGeneratorConfig.getVoModelConfiguration() != null && voGeneratorConfig.getVoModelConfiguration().isGenerate()) {
                List<OverridePropertyValueGeneratorConfiguration> voOverrides = voGeneratorConfig.getVoModelConfiguration().getOverridePropertyConfigurations();
                VCollectionUtil.addAllIfNotContains(voOverrides, overrides);
                voGeneratorConfig.getVoModelConfiguration().setOverridePropertyConfigurations(voOverrides);
            }
            if (voGeneratorConfig.getVoViewConfiguration() != null && voGeneratorConfig.getVoViewConfiguration().isGenerate()) {
                List<OverridePropertyValueGeneratorConfiguration> voViewOverrides = voGeneratorConfig.getVoViewConfiguration().getOverridePropertyConfigurations();
                VCollectionUtil.addAllIfNotContains(voViewOverrides, overrides);
                voGeneratorConfig.getVoViewConfiguration().setOverridePropertyConfigurations(voViewOverrides);
            }
            if (voGeneratorConfig.getVoExcelConfiguration() != null && voGeneratorConfig.getVoExcelConfiguration().isGenerate()) {
                List<OverridePropertyValueGeneratorConfiguration> voExcelOverrides = voGeneratorConfig.getVoExcelConfiguration().getOverridePropertyConfigurations();
                VCollectionUtil.addAllIfNotContains(voExcelOverrides, overrides);
                voGeneratorConfig.getVoExcelConfiguration().setOverridePropertyConfigurations(voExcelOverrides);
            }
        } else {
            VCollectionUtil.addAllIfNotContains(overrides, javaModelGeneratorConfig.getOverridePropertyConfigurations());
            javaModelGeneratorConfig.setOverridePropertyConfigurations(overrides);
        }
    }

    private void generateDefaultOverrideProperty(AbstractIntrospectedTable abstractIntrospectedTable) {
        //生成默认的overrideProperty
        if (abstractIntrospectedTable.getTableConfiguration().getJavaModelGeneratorConfiguration() != null) {
            abstractIntrospectedTable.getColumn(DefaultColumnNameEnum.MODULE_ID.columnName()).ifPresent(column -> {
                OverridePropertyValueGeneratorConfiguration override = new OverridePropertyValueGeneratorConfiguration(abstractIntrospectedTable.getContext(), abstractIntrospectedTable.getTableConfiguration(), column.getActualColumnName());
                override.setAnnotationType(DictTypeEnum.DICT_MODULE.getCode());
                override.setTargetPropertyName(DefaultColumnNameEnum.MODULE_ID.otherFieldName());
                override.setRemark(DefaultColumnNameEnum.MODULE_ID.comment());
                ConfigUtil.addOverridePropertyConfiguration(override, this);
            });
            abstractIntrospectedTable.getColumn(DefaultColumnNameEnum.CUR_PROCESSORS.columnName()).ifPresent(column -> {
                OverridePropertyValueGeneratorConfiguration override = new OverridePropertyValueGeneratorConfiguration(abstractIntrospectedTable.getContext(), abstractIntrospectedTable.getTableConfiguration(), column.getActualColumnName());
                override.setAnnotationType(DictTypeEnum.DICT.getCode());
                override.setBeanName(HtmlElementDataSourceEnum.USER.getBeanName());
                override.setTargetPropertyName(DefaultColumnNameEnum.CUR_PROCESSORS.otherFieldName());
                override.setRemark(DefaultColumnNameEnum.CUR_PROCESSORS.comment());
                ConfigUtil.addOverridePropertyConfiguration(override, this);
            });
        }
    }

    /**
     * 为已经配置存在的html元素描述器赋值introspectedColumn,自动配置dataUrl
     *
     * @param abstractIntrospectedTable introspectedTable对象
     * @param warnings                  警告列表
     */
    private void assignHtmlIntrospectedColumn(AbstractIntrospectedTable abstractIntrospectedTable, final List<String> warnings) {
        abstractIntrospectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations()
                .forEach(htmlGeneratorConfiguration -> htmlGeneratorConfiguration.getElementDescriptors()
                        .forEach(setHtmlElementDescriptorColumn(abstractIntrospectedTable)));
        if (this.getVoGeneratorConfiguration() != null && this.getVoGeneratorConfiguration().getVoViewConfiguration() != null
                && this.getVoGeneratorConfiguration().getVoViewConfiguration().getInnerListViewConfigurations() != null
                && !this.getVoGeneratorConfiguration().getVoViewConfiguration().getInnerListViewConfigurations().isEmpty()) {
            this.getVoGeneratorConfiguration().getVoViewConfiguration().getInnerListViewConfigurations()
                    .forEach(innerListViewConfiguration -> innerListViewConfiguration.getHtmlElements()
                            .forEach(setHtmlElementDescriptorColumn(abstractIntrospectedTable)));
        }
        //移除没有column实例的html元素描述器,并且添加警告
        abstractIntrospectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations()
                .forEach(htmlGeneratorConfiguration -> htmlGeneratorConfiguration.getElementDescriptors()
                        .removeIf(htmlElementDescriptor -> {
                            if (htmlElementDescriptor.getColumn() == null) {
                                warnings.add(String.format("table:%s,htmlElementDescriptor配置:%s,列不存在，配置被忽略", abstractIntrospectedTable.getTableConfiguration().getTableName(), htmlElementDescriptor.getName()));
                                return true;
                            }
                            return false;
                        }));
    }

    private static Consumer<HtmlElementDescriptor> setHtmlElementDescriptorColumn(AbstractIntrospectedTable abstractIntrospectedTable) {
        return elementDescriptor -> {
            if (stringHasValue(elementDescriptor.getName()) && elementDescriptor.getColumn() == null) {
                abstractIntrospectedTable.getColumn(elementDescriptor.getName()).ifPresent(elementDescriptor::setColumn);
            }
            if (!stringHasValue(elementDescriptor.getOtherFieldName())) {
                abstractIntrospectedTable.getColumn(elementDescriptor.getName()).ifPresent(c -> elementDescriptor.setOtherFieldName(ConfigUtil.getOverrideJavaProperty(c.getJavaProperty(), elementDescriptor)));
            }
            //当DataSource值分别为DictSys、DictData、DictUser，且没有配置dataUrl，且指定了DictCode，则分别自动配置dataUrl
            if (stringHasValue(elementDescriptor.getDataSource()) && !stringHasValue(elementDescriptor.getDataUrl())) {
                String camelCaseString = JavaBeansUtil.getCamelCaseString(elementDescriptor.getName(), false);
                if (DictTypeEnum.DICT_SYS.getCode().equals(elementDescriptor.getDataSource())) {
                    elementDescriptor.setDictCode(elementDescriptor.getDictCode() == null ? camelCaseString : elementDescriptor.getDictCode());
                    elementDescriptor.setDataUrl("/system/sys-cfg-dict-impl/option/" + elementDescriptor.getDictCode());
                } else if (DictTypeEnum.DICT_DATA.getCode().equals(elementDescriptor.getDataSource())) {
                    elementDescriptor.setDictCode(elementDescriptor.getDictCode() == null ? camelCaseString : elementDescriptor.getDictCode());
                    elementDescriptor.setDataUrl("/system/sys-dict-data-impl/option/" + elementDescriptor.getDictCode());
                } else if (DictTypeEnum.DICT_USER.getCode().equals(elementDescriptor.getDataSource())) {
                    elementDescriptor.setDictCode(elementDescriptor.getDictCode() == null ? camelCaseString : elementDescriptor.getDictCode());
                    elementDescriptor.setDataUrl("/system/dict-content-impl/option/" + elementDescriptor.getDictCode());
                }
            }
        };
    }

    private void calculateInnerListElementDescriptor(AbstractIntrospectedTable abstractIntrospectedTable) {
        //计算内联列表的元素描述器
        if (this.getVoGeneratorConfiguration() == null
                || this.getVoGeneratorConfiguration().getVoViewConfiguration() == null
                || this.getVoGeneratorConfiguration().getVoViewConfiguration().getInnerListViewConfigurations().isEmpty()) {
            return;
        }
        List<InnerListViewConfiguration> innerListViewConfigurations = abstractIntrospectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoViewConfiguration().getInnerListViewConfigurations();
        innerListViewConfigurations
                .forEach(innerListViewConfiguration -> {
                    if (stringHasValue(innerListViewConfiguration.getEditExtendsForm())) {
                        abstractIntrospectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().stream()
                                .filter(htmlConfiguration -> htmlConfiguration.getSimpleViewPath().equals(innerListViewConfiguration.getEditExtendsForm()))
                                .findFirst()
                                .ifPresent(htmlGeneratorConfiguration -> {
                                    innerListViewConfiguration.setHtmlGeneratorConfiguration(htmlGeneratorConfiguration);
                                    innerListViewConfiguration.getHtmlElements().addAll(htmlGeneratorConfiguration.getElementDescriptors());
                                    innerListViewConfiguration.getDefaultHiddenFields().addAll(htmlGeneratorConfiguration.getHiddenFieldNames());
                                    innerListViewConfiguration.getRequiredColumns().addAll(htmlGeneratorConfiguration.getElementRequired());
                                });
                    }
                });
    }

    private void calculateSelectByParentIdConfig(List<String> warnings, AbstractIntrospectedTable abstractIntrospectedTable) {
        String parentIdColumnName = DefaultColumnNameEnum.PARENT_ID.columnName();
        if (abstractIntrospectedTable.getTableConfiguration().getJavaModelGeneratorConfiguration().isEnableChildren() && this.getColumnNames().contains(parentIdColumnName)) {
            //如果存在parent_id字段，则自动添加selectByParentId方法
            if (this.getSelectByColumnGeneratorConfigurations().stream().noneMatch(c -> c.getColumns().stream().anyMatch(column -> parentIdColumnName.equalsIgnoreCase(column.getActualColumnName())))) {
                abstractIntrospectedTable.getColumn(parentIdColumnName).ifPresent(column -> {
                    SelectByColumnGeneratorConfiguration selectByParentId = new SelectByColumnGeneratorConfiguration(column.getActualColumnName());
                    selectByParentId.addColumn(column);
                    selectByParentId.setParameterList(false);
                    selectByParentId.setMethodName(JavaBeansUtil.byColumnMethodName(selectByParentId.getColumns(), false));
                    selectByParentId.setDeleteMethodName(JavaBeansUtil.deleteByColumnMethodName(selectByParentId.getColumns(), false));
                    this.addSelectByColumnGeneratorConfiguration(selectByParentId);
                    warnings.add("table:" + this.tableName + " 自动添加" + selectByParentId.getMethodName() + " 方法.");
                });
            }
        }
    }

    /**
     * 1、如果存在parent_id字段，则自动添加children属性及子查询
     * 2、如果存在parent_id字段，则添加childrenCount属性及selectWithChildrenCount方法
     */
    private void calculateChildrenRelationConfig(AbstractIntrospectedTable abstractIntrospectedTable) {
        String parentIdColumnName = DefaultColumnNameEnum.PARENT_ID.columnName();
        abstractIntrospectedTable.getColumn(parentIdColumnName).ifPresent(column -> {
            if (this.getRelationGeneratorConfigurations().stream().noneMatch(c -> DefaultColumnNameEnum.LEAF.fieldName().equalsIgnoreCase(c.getPropertyName()))) {
                VoAdditionalPropertyGeneratorConfiguration leafAdditionalPropertyConfiguration = Mb3GenUtil.generateAdditionalPropertyFromDefaultColumnNameEnum(abstractIntrospectedTable,
                        DefaultColumnNameEnum.LEAF,
                        "false",
                        Collections.singletonList("@TableField(exist = false)"));
                leafAdditionalPropertyConfiguration.getImportedTypes().add("com.baomidou.mybatisplus.annotation.TableField");
                this.addAdditionalPropertyConfigurations(leafAdditionalPropertyConfiguration);
            }
            if (this.getRelationGeneratorConfigurations().stream().noneMatch(c -> DefaultColumnNameEnum.DISABLED.fieldName().equalsIgnoreCase(c.getPropertyName()))) {
                VoAdditionalPropertyGeneratorConfiguration disabledAdditionalPropertyConfiguration = Mb3GenUtil.generateAdditionalPropertyFromDefaultColumnNameEnum(abstractIntrospectedTable,
                        DefaultColumnNameEnum.DISABLED,
                        "false",
                        Collections.singletonList("@TableField(exist = false)"));
                disabledAdditionalPropertyConfiguration.getImportedTypes().add("com.baomidou.mybatisplus.annotation.TableField");
                this.addAdditionalPropertyConfigurations(disabledAdditionalPropertyConfiguration);
            }

            if (abstractIntrospectedTable.getTableConfiguration().getJavaModelGeneratorConfiguration().isEnableChildren()) {
                if (this.getRelationGeneratorConfigurations().stream().noneMatch(c -> DefaultColumnNameEnum.CHILDREN.fieldName().equalsIgnoreCase(c.getPropertyName()))) {
                    RelationGeneratorConfiguration relationGeneratorConfiguration = new RelationGeneratorConfiguration();
                    relationGeneratorConfiguration.setRemark("子集合");
                    relationGeneratorConfiguration.setPropertyName(DefaultColumnNameEnum.CHILDREN.fieldName());
                    relationGeneratorConfiguration.setColumn(DefaultColumnNameEnum.ID.columnName());

                    StringBuilder sb = new StringBuilder();
                    sb.append(this.getJavaModelGeneratorConfiguration().getTargetPackage());
                    sb.append(".");
                    sb.append(this.getDomainObjectName());
                    relationGeneratorConfiguration.setModelTye(sb.toString());
                    if (this.getVoGeneratorConfiguration() != null && this.getVoGeneratorConfiguration().getVoModelConfiguration() != null && this.getVoGeneratorConfiguration().getVoModelConfiguration().isGenerate()) {
                        String voType = substringBeforeLast(abstractIntrospectedTable.getContext().getJavaModelGeneratorConfiguration().getTargetPackage(), ".") + ".pojo.vo." + this.getDomainObjectName() + "Vo";
                        relationGeneratorConfiguration.setVoModelTye(voType);
                    } else {
                        relationGeneratorConfiguration.setVoModelTye(sb.toString());
                    }
                    sb.setLength(0);
                    sb.append(this.getJavaClientGeneratorConfiguration().getTargetPackage()).append(".");
                    sb.append(this.getDomainObjectName()).append("Mapper").append(".");
                    sb.append(JavaBeansUtil.byColumnMethodName(Collections.singletonList(column), false));
                    relationGeneratorConfiguration.setSelect(sb.toString());
                    relationGeneratorConfiguration.setType(RelationTypeEnum.collection);
                    relationGeneratorConfiguration.setInitializationString("new ArrayList<>()");
                    relationGeneratorConfiguration.addImportTypes("java.util.ArrayList");
                    this.addRelationGeneratorConfiguration(relationGeneratorConfiguration);
                }
                if (this.getRelationGeneratorConfigurations().stream().noneMatch(c -> DefaultColumnNameEnum.CHILDREN_COUNT.fieldName().equalsIgnoreCase(c.getPropertyName()))) {
                    IntrospectedColumn childrenCountColumn = abstractIntrospectedTable.getColumn(DefaultColumnNameEnum.CHILDREN_COUNT.columnName()).orElse(null);
                    if (childrenCountColumn == null) {
                        VoAdditionalPropertyGeneratorConfiguration childrenCount = Mb3GenUtil.generateAdditionalPropertyFromDefaultColumnNameEnum(abstractIntrospectedTable,
                                DefaultColumnNameEnum.CHILDREN_COUNT,
                                "0",
                                Collections.singletonList("@TableField(exist = false)"));
                        childrenCount.setType(FullyQualifiedJavaType.getIntegerInstance().getFullyQualifiedName());
                        childrenCount.getImportedTypes().add("com.baomidou.mybatisplus.annotation.TableField");
                        this.addAdditionalPropertyConfigurations(childrenCount);
                    }
                }
            }
        });
    }

    private void calculateSelectBySqlMethodProperty(AbstractIntrospectedTable abstractIntrospectedTable) {
        //计算SelectBySqlMethod列
        for (SelectBySqlMethodGeneratorConfiguration bySqlMethodGeneratorConfiguration : this.getSelectBySqlMethodGeneratorConfigurations()) {
            bySqlMethodGeneratorConfiguration.setParentIdColumn(abstractIntrospectedTable.getColumn(bySqlMethodGeneratorConfiguration.getParentIdColumnName()).orElse(null));
            bySqlMethodGeneratorConfiguration.setPrimaryKeyColumn(abstractIntrospectedTable.getColumn(bySqlMethodGeneratorConfiguration.getPrimaryKeyColumnName()).orElse(null));
        }
        List<SelectBySqlMethodGeneratorConfiguration> collect = this.getSelectBySqlMethodGeneratorConfigurations().stream().peek(c -> {
            c.setParentIdColumn(abstractIntrospectedTable.getColumn(c.getParentIdColumnName()).orElse(null));
            c.setPrimaryKeyColumn(abstractIntrospectedTable.getColumn(c.getPrimaryKeyColumnName()).orElse(null));
        }).filter(c -> c.getPrimaryKeyColumn() != null && c.getParentIdColumn() != null).collect(Collectors.toList());
        this.setSelectBySqlMethodGeneratorConfigurations(collect);

        //生成SelectByTable基于关系表主键的查询方法
        if (!this.getSelectByTableGeneratorConfiguration().isEmpty()) {
            for (SelectByTableGeneratorConfiguration selectByTableGeneratorConfiguration : this.getSelectByTableGeneratorConfiguration()) {
                selectByTableGeneratorConfiguration.setParameterName(JavaBeansUtil.getCamelCaseString(selectByTableGeneratorConfiguration.getOtherPrimaryKeyColumn(), false));
            }
        }
    }

    private void calculateSelectByColumnConfigurations(AbstractIntrospectedTable abstractIntrospectedTable) {
        //生成selectByColumn查询方法
        if (!this.getSelectByColumnGeneratorConfigurations().isEmpty()) {
            for (SelectByColumnGeneratorConfiguration configuration : this.getSelectByColumnGeneratorConfigurations()) {
                configuration.getColumnNames().forEach(n -> abstractIntrospectedTable.getColumn(n).ifPresent(configuration::addColumn));
                configuration.setMethodName(JavaBeansUtil.byColumnMethodName(configuration.getColumns(), configuration.getParameterList()));
                configuration.setDeleteMethodName(JavaBeansUtil.deleteByColumnMethodName(configuration.getColumns(), configuration.getParameterList()));
            }
        }
        List<SelectByColumnGeneratorConfiguration> collect1 = this.getSelectByColumnGeneratorConfigurations().stream()
                .filter(config -> !config.getColumns().isEmpty() && config.getColumnNames().size() == config.getColumns().size())
                .distinct().collect(Collectors.toList());
        this.setSelectByColumnGeneratorConfigurations(collect1);
    }

    private void calculateSelectBaseByPrimaryKeyConfig(AbstractIntrospectedTable abstractIntrospectedTable) {
        if (this.getRelationGeneratorConfigurations().stream().anyMatch(RelationGeneratorConfiguration::isSubSelected)) {
            //追加一个基于主键的查询，用来区分selectByPrimaryKey方法，避免过多查询
            if (abstractIntrospectedTable.getPrimaryKeyColumns().isEmpty()) {
                return;
            }
            final String methodName = "selectBaseByPrimaryKey";
            if (this.getSelectByColumnGeneratorConfigurations().stream().noneMatch(t -> methodName.equals(t.getMethodName()))) {
                SelectByColumnGeneratorConfiguration selectByColumnGeneratorConfiguration = new SelectByColumnGeneratorConfiguration();
                for (IntrospectedColumn column : abstractIntrospectedTable.getPrimaryKeyColumns()) {
                    if (selectByColumnGeneratorConfiguration.addColumnName(column.getActualColumnName())) {
                        selectByColumnGeneratorConfiguration.addColumn(column);
                    }
                }
                selectByColumnGeneratorConfiguration.setParameterList(false);
                selectByColumnGeneratorConfiguration.setMethodName(methodName);
                selectByColumnGeneratorConfiguration.setEnableDelete(false);
                selectByColumnGeneratorConfiguration.setReturnType(1);
                this.getSelectByColumnGeneratorConfigurations().add(selectByColumnGeneratorConfiguration);
            }
        }
        if (this.getJavaControllerGeneratorConfiguration().generate && this.getJavaControllerGeneratorConfiguration().isEnableSelectByPrimaryKeys()) {
            if (!abstractIntrospectedTable.getPrimaryKeyColumns().isEmpty()) {
                String methodName = JavaBeansUtil.byColumnMethodName(abstractIntrospectedTable.getPrimaryKeyColumns(), true);
                if (this.getSelectByColumnGeneratorConfigurations().stream().noneMatch(t -> methodName.equals(t.getMethodName()))) {
                    SelectByColumnGeneratorConfiguration selectByColumnGeneratorConfiguration = new SelectByColumnGeneratorConfiguration();
                    for (IntrospectedColumn column : abstractIntrospectedTable.getPrimaryKeyColumns()) {
                        if (selectByColumnGeneratorConfiguration.addColumnName(column.getActualColumnName())) {
                            selectByColumnGeneratorConfiguration.addColumn(column);
                        }
                    }
                    selectByColumnGeneratorConfiguration.setMethodName(methodName);
                    selectByColumnGeneratorConfiguration.setEnableDelete(false);
                    selectByColumnGeneratorConfiguration.setReturnType(0);
                    selectByColumnGeneratorConfiguration.setParameterList(true);
                    selectByColumnGeneratorConfiguration.setGenControllerMethod(true);
                    this.getSelectByColumnGeneratorConfigurations().add(selectByColumnGeneratorConfiguration);
                }
            }
        }
    }

    private void addHtmlElementAdditionalAttribute(HtmlElementDescriptor elementDescriptor, AbstractIntrospectedTable abstractIntrospectedTable) {
        //不需要转换的数据源，直接返回
        if (!stringHasValue(elementDescriptor.getDataSource())) {
            return;
        }
        //javaModel或者voModel中不存在对应的属性，需要追加
        OverridePropertyValueGeneratorConfiguration overrideConfiguration = ConfigUtil.createOverridePropertyConfiguration(elementDescriptor, abstractIntrospectedTable);
        ConfigUtil.addOverridePropertyConfiguration(overrideConfiguration, this);
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

    public void addOverridePropertyConfigurations(OverridePropertyValueGeneratorConfiguration overridePropertyConfiguration) {
        this.getOverridePropertyConfigurations().add(overridePropertyConfiguration);
    }

    public void addAdditionalPropertyConfigurations(VoAdditionalPropertyGeneratorConfiguration additionalPropertyConfiguration) {
        for (VoAdditionalPropertyGeneratorConfiguration propertyConfiguration : this.getAdditionalPropertyConfigurations()) {
            if (propertyConfiguration.getName().equals(additionalPropertyConfiguration.getName())) {
                return;
            }
        }
        this.getAdditionalPropertyConfigurations().add(additionalPropertyConfiguration);
    }

    /**
     * 内部方法，字段是否需要提供验证
     */
    private boolean isFieldNeedValidate(IntrospectedColumn column, List<HtmlGeneratorConfiguration> htmlGeneratorConfigurations) {
        if (this.getValidateIgnoreColumns().contains(column.getActualColumnName())) {
            return false;
        }
        if (column.isRequired()) {
            return true;
        }
        if (htmlGeneratorConfigurations != null && !htmlGeneratorConfigurations.isEmpty()) {
            for (HtmlGeneratorConfiguration htmlGeneratorConfiguration : htmlGeneratorConfigurations) {
                HtmlElementDescriptor htmlElementDescriptor = htmlGeneratorConfiguration.getHtmlElementDescriptor(column.getActualColumnName());
                if (htmlElementDescriptor != null && htmlElementDescriptor.getVerify().contains("required")) {
                    return true;
                }
            }
        }
        return false;
    }
}
