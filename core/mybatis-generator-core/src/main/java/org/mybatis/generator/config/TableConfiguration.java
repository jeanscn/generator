package org.mybatis.generator.config;

import com.vgosoft.core.constant.enums.core.*;
import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementDataSourceEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementTagTypeEnum;
import com.vgosoft.tool.core.VCollectionUtil;
import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.config.factory.*;
import org.mybatis.generator.custom.RelationTypeEnum;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.Mb3GenUtil;
import org.mybatis.generator.internal.util.messages.Messages;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.vgosoft.tool.core.VStringUtil.stringHasValue;
import static org.mybatis.generator.internal.util.StringUtility.*;

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

    private VOGeneratorConfiguration voGeneratorConfiguration;

    private VOCacheGeneratorConfiguration voCacheGeneratorConfiguration;

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

    public TableConfiguration(Context context) {
        super();
        this.modelType = context.getDefaultModelType();
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

        if (!(obj instanceof TableConfiguration)) {
            return false;
        }

        TableConfiguration other = (TableConfiguration) obj;

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

    public boolean isModules() {
        return isModules;
    }

    public void setModules(boolean modules) {
        isModules = modules;
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
            errors.add(Messages.getString("ValidationError.6", Integer.toString(listPosition))); //$NON-NLS-1$
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

    public boolean isAllColumnDelimitingEnabled() {
        return isAllColumnDelimitingEnabled;
    }

    public void setAllColumnDelimitingEnabled(boolean isAllColumnDelimitingEnabled) {
        this.isAllColumnDelimitingEnabled = isAllColumnDelimitingEnabled;
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

    public void reprocessConfiguration(List<String> warnings, IntrospectedTable introspectedTable, Context context) {
        //根据配置更新introspectedTable的voModelType、voCreateType属性
        updateVoModelType(introspectedTable);

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
        //增加SelectBaseByPrimaryKey配置
        calculateSelectBaseByPrimaryKeyConfig(introspectedTable);
        //计算所有的selectByColumn配置，去重、计算列信息
        calculateSelectByColumnConfigurations(introspectedTable);
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

    private void updateQueryColumnConfiguration(IntrospectedTable introspectedTable) {

        //viewVo中，为QueryColumnConfiguration赋值IntrospectedColumn，清除列不存在IntrospectedColumn的配置
        List<QueryColumnConfiguration> queryColumnConfigurations = getViewQueryColumnConfigurations();
        queryColumnConfigurations.forEach(queryColumnConfiguration -> queryColumnConfiguration.setIntrospectedColumn(introspectedTable.getColumn(queryColumnConfiguration.getColumn()).orElse(null)));
        queryColumnConfigurations.removeIf(queryColumnConfiguration -> queryColumnConfiguration.getIntrospectedColumn() == null);

        //innerList中，为QueryColumnConfiguration赋值IntrospectedColumn，清除列不存在IntrospectedColumn的配置
        List<InnerListViewConfiguration> innerListViewConfigurations = Optional.ofNullable(this.getVoGeneratorConfiguration())
                .flatMap(voGeneratorConfiguration -> Optional.ofNullable(voGeneratorConfiguration.getVoViewConfiguration()))
                .flatMap(voViewConfiguration -> Optional.ofNullable(voViewConfiguration.getInnerListViewConfigurations()))
                .orElse(new ArrayList<>());
        innerListViewConfigurations.forEach(innerListViewConfiguration -> {
            innerListViewConfiguration.getQueryColumnConfigurations().forEach(queryColumnConfiguration -> queryColumnConfiguration.setIntrospectedColumn(introspectedTable.getColumn(queryColumnConfiguration.getColumn()).orElse(null)));
            innerListViewConfiguration.getQueryColumnConfigurations().removeIf(queryColumnConfiguration -> queryColumnConfiguration.getIntrospectedColumn() == null);
        });

        //校验searchColumn
        if (this.getVoGeneratorConfiguration() != null && this.getVoGeneratorConfiguration().getVoViewConfiguration() != null) {
            if (!this.getVoGeneratorConfiguration().getVoViewConfiguration().getFuzzyColumns().isEmpty()) {
                List<String> collect = this.getVoGeneratorConfiguration().getVoViewConfiguration().getFuzzyColumns().stream().map(introspectedTable::getColumn).filter(Optional::isPresent).map(col -> col.get().getActualColumnName()).collect(Collectors.toList());
                this.getVoGeneratorConfiguration().getVoViewConfiguration().setFuzzyColumns(collect);
            }
            if (!this.getVoGeneratorConfiguration().getVoViewConfiguration().getInnerListViewConfigurations().isEmpty()) {
                this.getVoGeneratorConfiguration().getVoViewConfiguration().getInnerListViewConfigurations().forEach(innerListViewConfiguration -> {
                    if (!innerListViewConfiguration.getFuzzyColumns().isEmpty()) {
                        List<String> collect = innerListViewConfiguration.getFuzzyColumns().stream().map(introspectedTable::getColumn).filter(Optional::isPresent).map(col -> col.get().getActualColumnName()).collect(Collectors.toList());
                        innerListViewConfiguration.setFuzzyColumns(collect);
                    }
                });
            }
        }

        //校验filterColumn
        if (this.getVoGeneratorConfiguration() != null && this.getVoGeneratorConfiguration().getVoViewConfiguration() != null) {
            if (!this.getVoGeneratorConfiguration().getVoViewConfiguration().getFilterColumns().isEmpty()) {
                List<String> collect = this.getVoGeneratorConfiguration().getVoViewConfiguration().getFilterColumns().stream().map(introspectedTable::getColumn).filter(Optional::isPresent).map(col -> col.get().getActualColumnName()).collect(Collectors.toList());
                this.getVoGeneratorConfiguration().getVoViewConfiguration().setFilterColumns(collect);
            }
            if (!this.getVoGeneratorConfiguration().getVoViewConfiguration().getInnerListViewConfigurations().isEmpty()) {
                this.getVoGeneratorConfiguration().getVoViewConfiguration().getInnerListViewConfigurations().forEach(innerListViewConfiguration -> {
                    if (!innerListViewConfiguration.getFilterColumns().isEmpty()) {
                        List<String> collect = innerListViewConfiguration.getFilterColumns().stream().map(introspectedTable::getColumn).filter(Optional::isPresent).map(col -> col.get().getActualColumnName()).collect(Collectors.toList());
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
     * @param introspectedTable introspectedTable对象
     */
    private void updateVoModelType(IntrospectedTable introspectedTable) {
        if (this.getVoGeneratorConfiguration() != null && this.getVoGeneratorConfiguration().getVoModelConfiguration() != null) {
            introspectedTable.setVoModelType(this.getVoGeneratorConfiguration().getVoModelConfiguration().getFullyQualifiedJavaType());
        }
        if (this.getVoGeneratorConfiguration() != null && this.getVoGeneratorConfiguration().getVoCreateConfiguration() != null) {
            introspectedTable.setVoCreateType(this.getVoGeneratorConfiguration().getVoCreateConfiguration().getFullyQualifiedJavaType());
        }
    }

    //为所有的html元素描述器生成OverrideProperty属性
    private void generateDescriptorOverrideProperty(IntrospectedTable introspectedTable) {
        // 为所有的html元素描述器生成OverrideProperty属性
        this.getHtmlMapGeneratorConfigurations()
                .forEach(htmlMapGeneratorConfiguration -> htmlMapGeneratorConfiguration.getElementDescriptors()
                        .forEach(elementDescriptor -> {
                            //添加附件属性
                            if (elementDescriptor.getOtherFieldName() != null && !elementDescriptor.getColumn().getJavaProperty().equals(elementDescriptor.getOtherFieldName())) {
                                addHtmlElementAdditionalAttribute(elementDescriptor, introspectedTable);
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
                                addHtmlElementAdditionalAttribute(elementDescriptor, introspectedTable);
                            }
                        }));
    }

    //更新TC与表结构相关的自定义属性
    private void updateTableConfiguration(final IntrospectedTable introspectedTable) {
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
            if ((column.isRequired() || column.getLength() > 0) && !this.getValidateIgnoreColumns().contains(column.getActualColumnName())) {
                column.setBeValidated(true);
            }
        });
    }

    //对指定了dataFormat的快捷配置进行转换
    private void convertDataFormatConfigurations(IntrospectedTable introspectedTable) {
        //更新制定了dataType的html元素描述器配置
        this.getHtmlMapGeneratorConfigurations()
                .forEach(htmlGeneratorConfiguration -> htmlGeneratorConfiguration.getElementDescriptors()
                        .forEach(elementDescriptor -> {
                            if (!stringHasValue(elementDescriptor.getDataFormat())) {
                                return;
                            }
                            introspectedTable.getColumn(elementDescriptor.getName()).ifPresent(column -> {
                                DefaultColumnNameEnum defaultColumnNameEnum = DefaultColumnNameEnum.ofColumnName(column.getActualColumnName()).orElse(null);
                                if (defaultColumnNameEnum != null) {
                                    elementDescriptor.setOtherFieldName(defaultColumnNameEnum.otherFieldName());
                                } else {
                                    elementDescriptor.setOtherFieldName(ConfigUtil.getOverrideJavaProperty(column.getJavaProperty(),elementDescriptor));
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

    /*
     * 计算HtmlHiddenFields
     * 由context、table的隐藏属性配置，计算出所有的隐藏列
     * 影响的配置有：
     * 1、所有HtmlMapGeneratorConfiguration的hiddenColumns
     * 2、所有HtmlMapGeneratorConfiguration的hiddenColumnNames
     * 3、所有HtmlMapGeneratorConfiguration的elementRequired
     */
    private void calculateHiddenReadDisplayOnlyColumns(IntrospectedTable introspectedTable) {
        Set<String> hiddenColumnNames = getHtmlAnyProperties(introspectedTable, PropertyRegistry.ANY_HTML_HIDDEN_FIELDS);
        this.getHtmlHiddenFields().addAll(hiddenColumnNames);
        //html的隐藏列
        this.getHtmlMapGeneratorConfigurations().forEach(htmlGeneratorConfiguration -> htmlGeneratorConfiguration.getHiddenFieldNames().addAll(this.getHtmlHiddenFields()));
        //html只读列
        Set<String> readOnlyFields = getHtmlAnyProperties(introspectedTable, PropertyRegistry.ANY_HTML_READONLY_FIELDS);
        introspectedTable.getTableConfiguration().getHtmlReadonlyFields().addAll(readOnlyFields);
        this.getHtmlMapGeneratorConfigurations()
                .forEach(htmlGeneratorConfiguration -> htmlGeneratorConfiguration.getReadonlyFields().addAll(readOnlyFields));
        //html只显示列
        Set<String> displayOnlyFields = getHtmlAnyProperties(introspectedTable, PropertyRegistry.ANY_HTML_DISPLAY_ONLY_FIELDS);
        introspectedTable.getTableConfiguration().getHtmlDisplayOnlyFields().addAll(displayOnlyFields);
        this.getHtmlMapGeneratorConfigurations()
                .forEach(htmlGeneratorConfiguration -> htmlGeneratorConfiguration.getDisplayOnlyFields().addAll(displayOnlyFields));
        //html的必填列
        this.getHtmlMapGeneratorConfigurations().forEach(htmlGeneratorConfiguration -> {
            if (htmlGeneratorConfiguration.getProperties().getProperty(PropertyRegistry.ELEMENT_REQUIRED_COLUMNS) != null) {
                htmlGeneratorConfiguration.getElementRequired().addAll(splitToSet(htmlGeneratorConfiguration.getProperties().getProperty(PropertyRegistry.ELEMENT_REQUIRED_COLUMNS)));
            }
            htmlGeneratorConfiguration.getElementDescriptors()
                    .forEach(elementDescriptor -> {
                        if (elementDescriptor.getVerify() != null && elementDescriptor.getVerify().contains("required")) {
                            htmlGeneratorConfiguration.getElementRequired().add(elementDescriptor.getName());
                        }
                    });
        });
        introspectedTable.getAllColumns().forEach(column -> this.getHtmlMapGeneratorConfigurations()
                .forEach(htmlGeneratorConfiguration -> {
                    if (column.isRequired()) {
                        htmlGeneratorConfiguration.getElementRequired().add(column.getActualColumnName());
                    } else {

                    }
                }));

    }

    private Set<String> getHtmlAnyProperties(IntrospectedTable introspectedTable, String propertyName) {
        Set<String> properties = new HashSet<>();
        String contextProperty = introspectedTable.getContext().getProperty(propertyName);
        if (stringHasValue(contextProperty)) {
            properties.addAll(splitToSet(contextProperty));
        }
        String tableProperty = introspectedTable.getTableConfiguration().getProperty(propertyName);
        if (stringHasValue(tableProperty)) {
            properties.addAll(splitToSet(tableProperty));
        }
        return properties;
    }

    private void generateDefaultElementDescriptor(IntrospectedTable introspectedTable) {
        /*
         * 对于非隐藏列，根据列名自动添加页面元素
         * state_列名，自动添加页面switch（启用停用）元素
         * parent_id列名，自动添加页面select（父级）元素
         * 注释中包含“是否”，自动添加页面switch（是/否）元素
         */
        introspectedTable.getAllColumns().stream().filter(column -> !this.getHtmlHiddenFields().contains(column.getJavaProperty()))
                .forEach(column -> this.getHtmlMapGeneratorConfigurations()
                        .forEach(htmlConfiguration -> {
                            if (htmlConfiguration.getElementDescriptors().stream().noneMatch(elementDescriptor -> elementDescriptor.getName().equals(column.getActualColumnName()))
                                    && !htmlConfiguration.getHiddenFieldNames().contains(column.getJavaProperty())) {
                                List<HtmlElementDescriptor> elementDescriptors = htmlConfiguration.getElementDescriptors();
                                HtmlElementDescriptor htmlElementDescriptor = null;
                                if (column.getActualColumnName().equalsIgnoreCase(DefaultColumnNameEnum.STATE.columnName())) {
                                    DefaultHtmlElementDescriptorFactory stateElementDescriptor = new StateElementDescriptor();
                                    htmlElementDescriptor = stateElementDescriptor.getDefaultHtmlElementDescriptor(column, introspectedTable);
                                } else if (column.getActualColumnName().equalsIgnoreCase(DefaultColumnNameEnum.PRIORITY.columnName())) {
                                    DefaultHtmlElementDescriptorFactory priorityElementDescriptor = new PriorityElementDescriptor();
                                    htmlElementDescriptor = priorityElementDescriptor.getDefaultHtmlElementDescriptor(column, introspectedTable);
                                } else if (column.getActualColumnName().equalsIgnoreCase(DefaultColumnNameEnum.WF_STATE.columnName())) {
                                    DefaultHtmlElementDescriptorFactory wfStateElementDescriptor = new WfStateElementDescriptor();
                                    htmlElementDescriptor = wfStateElementDescriptor.getDefaultHtmlElementDescriptor(column, introspectedTable);
                                } else if (column.getActualColumnName().equalsIgnoreCase(DefaultColumnNameEnum.PARENT_ID.columnName())) {   //选择上级
                                    htmlElementDescriptor = new ParentIdElementDescriptor().getDefaultHtmlElementDescriptor(column, introspectedTable);
                                } else if (column.getRemarks(true) != null && column.getRemarks(true).startsWith("是否")) { //如果列注释以“是否”开头，则自动添加页面switch元素
                                    htmlElementDescriptor = new YesNoElementDescriptor().getDefaultHtmlElementDescriptor(column, introspectedTable);
                                } else if (column.getActualColumnName().equalsIgnoreCase(DefaultColumnNameEnum.APPLY_DEPT_ID.columnName())) {
                                    DefaultHtmlElementDescriptorFactory deptElementDescriptor = new ApplyDeptIdInputElementDescriptor();
                                    htmlElementDescriptor = deptElementDescriptor.getDefaultHtmlElementDescriptor(column, introspectedTable);
                                } else if (column.getActualColumnName().equalsIgnoreCase(DefaultColumnNameEnum.APPLY_USER_ID.columnName())) {
                                    DefaultHtmlElementDescriptorFactory userElementDescriptor = new ApplyUserIdInputElementDescriptor();
                                    htmlElementDescriptor = userElementDescriptor.getDefaultHtmlElementDescriptor(column, introspectedTable);
                                } else if (column.getActualColumnName().equalsIgnoreCase(DefaultColumnNameEnum.APPLY_HANDLER_ID.columnName())) {
                                    DefaultHtmlElementDescriptorFactory handlerElementDescriptor = new ApplyHandlerIdInputElementDescriptor();
                                    htmlElementDescriptor = handlerElementDescriptor.getDefaultHtmlElementDescriptor(column, introspectedTable);
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

    /*
     * 批量处理overrideProperty的配置文档
     * 更新overrideProperty配置的继承关系
     * overrideProperty去重复
     */
    private void processOverrideProperty(IntrospectedTable introspectedTable) {
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        JavaModelGeneratorConfiguration javaModelGeneratorConfig = tc.getJavaModelGeneratorConfiguration();
        VOGeneratorConfiguration voGeneratorConfig = tc.getVoGeneratorConfiguration();
        List<OverridePropertyValueGeneratorConfiguration> overrides =tc.getOverridePropertyConfigurations();
        if (voGeneratorConfig != null && voGeneratorConfig.isGenerate()) {
            VCollectionUtil.addAllIfNotContains(overrides,voGeneratorConfig.getOverridePropertyConfigurations());
            if (voGeneratorConfig.getVoModelConfiguration() != null && voGeneratorConfig.getVoModelConfiguration().isGenerate()) {
                List<OverridePropertyValueGeneratorConfiguration> voOverrides = voGeneratorConfig.getVoModelConfiguration().getOverridePropertyConfigurations();
                VCollectionUtil.addAllIfNotContains(voOverrides,overrides);
                voGeneratorConfig.getVoModelConfiguration().setOverridePropertyConfigurations(voOverrides);
            }
            if (voGeneratorConfig.getVoViewConfiguration() != null && voGeneratorConfig.getVoViewConfiguration().isGenerate()) {
                List<OverridePropertyValueGeneratorConfiguration> voViewOverrides = voGeneratorConfig.getVoViewConfiguration().getOverridePropertyConfigurations();
                VCollectionUtil.addAllIfNotContains(voViewOverrides,overrides);
                voGeneratorConfig.getVoViewConfiguration().setOverridePropertyConfigurations(voViewOverrides);
            }
            if (voGeneratorConfig.getVoExcelConfiguration() != null && voGeneratorConfig.getVoExcelConfiguration().isGenerate()) {
                List<OverridePropertyValueGeneratorConfiguration> voExcelOverrides = voGeneratorConfig.getVoExcelConfiguration().getOverridePropertyConfigurations();
                VCollectionUtil.addAllIfNotContains(voExcelOverrides,overrides);
                voGeneratorConfig.getVoExcelConfiguration().setOverridePropertyConfigurations(voExcelOverrides);
            }
        } else {
            VCollectionUtil.addAllIfNotContains(overrides,javaModelGeneratorConfig.getOverridePropertyConfigurations());
            javaModelGeneratorConfig.setOverridePropertyConfigurations(overrides);
        }
    }

    private void generateDefaultOverrideProperty(IntrospectedTable introspectedTable) {
        //生成默认的overrideProperty
        if (introspectedTable.getTableConfiguration().getJavaModelGeneratorConfiguration() != null) {
            introspectedTable.getColumn(DefaultColumnNameEnum.MODULE_ID.columnName()).ifPresent(column -> {
                OverridePropertyValueGeneratorConfiguration override = new OverridePropertyValueGeneratorConfiguration(introspectedTable.getContext(), introspectedTable.getTableConfiguration(), column.getActualColumnName());
                override.setAnnotationType(DictTypeEnum.DICT_MODULE.getCode());
                override.setTargetPropertyName(DefaultColumnNameEnum.MODULE_ID.otherFieldName());
                override.setRemark(DefaultColumnNameEnum.MODULE_ID.comment());
                ConfigUtil.addOverridePropertyConfiguration(override, this);
            });
            introspectedTable.getColumn(DefaultColumnNameEnum.CUR_PROCESSORS.columnName()).ifPresent(column -> {
                OverridePropertyValueGeneratorConfiguration override = new OverridePropertyValueGeneratorConfiguration(introspectedTable.getContext(), introspectedTable.getTableConfiguration(), column.getActualColumnName());
                override.setAnnotationType(DictTypeEnum.DICT.getCode());
                override.setBeanName(HtmlElementDataSourceEnum.USER.getBeanName());
                override.setTargetPropertyName(DefaultColumnNameEnum.CUR_PROCESSORS.otherFieldName());
                override.setRemark(DefaultColumnNameEnum.CUR_PROCESSORS.comment());
                ConfigUtil.addOverridePropertyConfiguration(override, this);
            });
        }
    }

    //为已经配置存在的html元素描述器赋值introspectedColumn,自动配置dataUrl
    private void assignHtmlIntrospectedColumn(IntrospectedTable introspectedTable, final List<String> warnings) {
        introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations()
                .forEach(htmlGeneratorConfiguration -> htmlGeneratorConfiguration.getElementDescriptors()
                        .forEach(setHtmlElementDescriptorColumn(introspectedTable)));
        if (this.getVoGeneratorConfiguration() != null && this.getVoGeneratorConfiguration().getVoViewConfiguration() != null
                && this.getVoGeneratorConfiguration().getVoViewConfiguration().getInnerListViewConfigurations() != null
                && !this.getVoGeneratorConfiguration().getVoViewConfiguration().getInnerListViewConfigurations().isEmpty()) {
            this.getVoGeneratorConfiguration().getVoViewConfiguration().getInnerListViewConfigurations()
                    .forEach(innerListViewConfiguration -> innerListViewConfiguration.getHtmlElements()
                            .forEach(setHtmlElementDescriptorColumn(introspectedTable)));
        }
        //移除没有column实例的html元素描述器,并且添加警告
        introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations()
                .forEach(htmlGeneratorConfiguration -> htmlGeneratorConfiguration.getElementDescriptors()
                        .removeIf(htmlElementDescriptor -> {
                            if (htmlElementDescriptor.getColumn() == null) {
                                warnings.add(String.format("table:%s,htmlElementDescriptor配置:%s,列不存在，配置被忽略", introspectedTable.getTableConfiguration().getTableName(), htmlElementDescriptor.getName()));
                                return true;
                            }
                            return false;
                        }));
    }

    private static Consumer<HtmlElementDescriptor> setHtmlElementDescriptorColumn(IntrospectedTable introspectedTable) {
        return elementDescriptor -> {
            if (stringHasValue(elementDescriptor.getName()) && elementDescriptor.getColumn() == null) {
                introspectedTable.getColumn(elementDescriptor.getName()).ifPresent(elementDescriptor::setColumn);
            }
            if (!stringHasValue(elementDescriptor.getOtherFieldName())) {
                if (HtmlElementTagTypeEnum.INPUT.codeName().equals(elementDescriptor.getTagType())) {
                    introspectedTable.getColumn(elementDescriptor.getName()).ifPresent(c -> elementDescriptor.setOtherFieldName(c.getJavaProperty()));
                } else {
                    introspectedTable.getColumn(elementDescriptor.getName()).ifPresent(c -> elementDescriptor.setOtherFieldName(ConfigUtil.getOverrideJavaProperty(c.getJavaProperty(),elementDescriptor)));
                }
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

    private void calculateInnerListElementDescriptor(IntrospectedTable introspectedTable) {
        //计算内联列表的元素描述器
        if (this.getVoGeneratorConfiguration() == null
                || this.getVoGeneratorConfiguration().getVoViewConfiguration() == null
                || this.getVoGeneratorConfiguration().getVoViewConfiguration().getInnerListViewConfigurations().isEmpty()) {
            return;
        }
        List<InnerListViewConfiguration> innerListViewConfigurations = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoViewConfiguration().getInnerListViewConfigurations();
        innerListViewConfigurations
                .forEach(innerListViewConfiguration -> {
                    if (stringHasValue(innerListViewConfiguration.getEditExtendsForm())) {
                        introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().stream()
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

    private void calculateSelectByParentIdConfig(List<String> warnings, IntrospectedTable introspectedTable) {
        String parentIdColumnName = DefaultColumnNameEnum.PARENT_ID.columnName();
        if (introspectedTable.getTableConfiguration().getJavaModelGeneratorConfiguration().isGenerateChildren() && this.getColumnNames().contains(parentIdColumnName)) {
            //如果存在parent_id字段，则自动添加selectByParentId方法
            if (this.getSelectByColumnGeneratorConfigurations().stream().noneMatch(c -> c.getColumns().stream().anyMatch(column -> parentIdColumnName.equalsIgnoreCase(column.getActualColumnName())))) {
                introspectedTable.getColumn(parentIdColumnName).ifPresent(column -> {
                    SelectByColumnGeneratorConfiguration selectByParentId = new SelectByColumnGeneratorConfiguration(column.getActualColumnName());
                    selectByParentId.addColumn(column);
                    selectByParentId.setParameterList(false);
                    selectByParentId.setMethodName(JavaBeansUtil.byColumnMethodName(selectByParentId.getColumns(),false));
                    selectByParentId.setDeleteMethodName(JavaBeansUtil.deleteByColumnMethodName(selectByParentId.getColumns(),false));
                    this.addSelectByColumnGeneratorConfiguration(selectByParentId);
                    warnings.add("table:" + this.tableName + " 自动添加" + selectByParentId.getMethodName() + " 方法.");
                });
            }
        }
    }

    /*
     * 1、如果存在parent_id字段，则自动添加children属性及子查询
     * 2、如果存在parent_id字段，则添加childrenCount属性及selectWithChildrenCount方法
     */
    private void calculateChildrenRelationConfig(IntrospectedTable introspectedTable) {
        String parentIdColumnName = DefaultColumnNameEnum.PARENT_ID.columnName();
        introspectedTable.getColumn(parentIdColumnName).ifPresent(column -> {
            if (this.getRelationGeneratorConfigurations().stream().noneMatch(c -> DefaultColumnNameEnum.LEAF.fieldName().equalsIgnoreCase(c.getPropertyName()))) {
                VoAdditionalPropertyGeneratorConfiguration leafAdditionalPropertyConfiguration = Mb3GenUtil.generateAdditionalPropertyFromDefaultColumnNameEnum(introspectedTable,
                        DefaultColumnNameEnum.LEAF,
                        "false",
                        Collections.singletonList("@TableField(exist = false)"));
                leafAdditionalPropertyConfiguration.getImportedTypes().add("com.baomidou.mybatisplus.annotation.TableField");
                this.addAdditionalPropertyConfigurations(leafAdditionalPropertyConfiguration);
            }
            if (this.getRelationGeneratorConfigurations().stream().noneMatch(c -> DefaultColumnNameEnum.DISABLED.fieldName().equalsIgnoreCase(c.getPropertyName()))) {
                VoAdditionalPropertyGeneratorConfiguration disabledAdditionalPropertyConfiguration = Mb3GenUtil.generateAdditionalPropertyFromDefaultColumnNameEnum(introspectedTable,
                        DefaultColumnNameEnum.DISABLED,
                        "false",
                        Collections.singletonList("@TableField(exist = false)"));
                disabledAdditionalPropertyConfiguration.getImportedTypes().add("com.baomidou.mybatisplus.annotation.TableField");
                this.addAdditionalPropertyConfigurations(disabledAdditionalPropertyConfiguration);
            }

            if (introspectedTable.getTableConfiguration().getJavaModelGeneratorConfiguration().isGenerateChildren()) {
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
                        String voType = substringBeforeLast(introspectedTable.getContext().getJavaModelGeneratorConfiguration().getTargetPackage(), ".") + ".pojo.vo." + this.getDomainObjectName() + "VO";
                        relationGeneratorConfiguration.setVoModelTye(voType);
                    } else {
                        relationGeneratorConfiguration.setVoModelTye(sb.toString());
                    }
                    sb.setLength(0);
                    sb.append(this.getJavaClientGeneratorConfiguration().getTargetPackage()).append(".");
                    sb.append(this.getDomainObjectName()).append("Mapper").append(".");
                    sb.append(JavaBeansUtil.byColumnMethodName(Collections.singletonList(column),false));
                    relationGeneratorConfiguration.setSelect(sb.toString());
                    relationGeneratorConfiguration.setType(RelationTypeEnum.collection);
                    relationGeneratorConfiguration.setInitializationString("new ArrayList<>()");
                    relationGeneratorConfiguration.addImportTypes("java.util.ArrayList");
                    this.addRelationGeneratorConfiguration(relationGeneratorConfiguration);
                }
                if (this.getRelationGeneratorConfigurations().stream().noneMatch(c -> DefaultColumnNameEnum.CHILDREN_COUNT.fieldName().equalsIgnoreCase(c.getPropertyName()))) {
                    IntrospectedColumn childrenCountColumn = introspectedTable.getColumn(DefaultColumnNameEnum.CHILDREN_COUNT.columnName()).orElse(null);
                    if (childrenCountColumn == null) {
                        VoAdditionalPropertyGeneratorConfiguration childrenCount = Mb3GenUtil.generateAdditionalPropertyFromDefaultColumnNameEnum(introspectedTable,
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

    private void calculateSelectBySqlMethodProperty(IntrospectedTable introspectedTable) {
        //计算SelectBySqlMethod列
        for (SelectBySqlMethodGeneratorConfiguration bySqlMethodGeneratorConfiguration : this.getSelectBySqlMethodGeneratorConfigurations()) {
            bySqlMethodGeneratorConfiguration.setParentIdColumn(introspectedTable.getColumn(bySqlMethodGeneratorConfiguration.getParentIdColumnName()).orElse(null));
            bySqlMethodGeneratorConfiguration.setPrimaryKeyColumn(introspectedTable.getColumn(bySqlMethodGeneratorConfiguration.getPrimaryKeyColumnName()).orElse(null));
        }
        List<SelectBySqlMethodGeneratorConfiguration> collect = this.getSelectBySqlMethodGeneratorConfigurations().stream().peek(c -> {
            c.setParentIdColumn(introspectedTable.getColumn(c.getParentIdColumnName()).orElse(null));
            c.setPrimaryKeyColumn(introspectedTable.getColumn(c.getPrimaryKeyColumnName()).orElse(null));
        }).filter(c -> c.getPrimaryKeyColumn() != null && c.getParentIdColumn() != null).collect(Collectors.toList());
        this.setSelectBySqlMethodGeneratorConfigurations(collect);

        //生成SelectByTable基于关系表主键的查询方法
        if (!this.getSelectByTableGeneratorConfiguration().isEmpty()) {
            for (SelectByTableGeneratorConfiguration selectByTableGeneratorConfiguration : this.getSelectByTableGeneratorConfiguration()) {
                selectByTableGeneratorConfiguration.setParameterName(JavaBeansUtil.getCamelCaseString(selectByTableGeneratorConfiguration.getOtherPrimaryKeyColumn(), false));
            }
        }
    }

    private void calculateSelectByColumnConfigurations(IntrospectedTable introspectedTable) {
        //生成selectByColumn查询方法
        if (!this.getSelectByColumnGeneratorConfigurations().isEmpty()) {
            for (SelectByColumnGeneratorConfiguration configuration : this.getSelectByColumnGeneratorConfigurations()) {
                configuration.getColumnNames().forEach(n -> introspectedTable.getColumn(n).ifPresent(configuration::addColumn));
                configuration.setMethodName(JavaBeansUtil.byColumnMethodName(configuration.getColumns(),configuration.getParameterList()));
                configuration.setDeleteMethodName(JavaBeansUtil.deleteByColumnMethodName(configuration.getColumns(),configuration.getParameterList()));
            }
        }
        List<SelectByColumnGeneratorConfiguration> collect1 = this.getSelectByColumnGeneratorConfigurations().stream()
                .filter(config->!config.getColumns().isEmpty() && config.getColumnNames().size()== config.getColumns().size())
                .distinct().collect(Collectors.toList());
        this.setSelectByColumnGeneratorConfigurations(collect1);
    }

    private void calculateSelectBaseByPrimaryKeyConfig(IntrospectedTable introspectedTable) {
        if (this.getRelationGeneratorConfigurations().stream().anyMatch(RelationGeneratorConfiguration::isSubSelected)) {
            //追加一个基于主键的查询，用来区分selectByPrimaryKey方法，避免过多查询
            if (introspectedTable.getPrimaryKeyColumns().isEmpty()) return;
            final String methodName = "selectBaseByPrimaryKey";
            if (this.getSelectByColumnGeneratorConfigurations().stream().noneMatch(t -> methodName.equals(t.getMethodName()))) {
                SelectByColumnGeneratorConfiguration selectByColumnGeneratorConfiguration = new SelectByColumnGeneratorConfiguration();
                for (IntrospectedColumn column : introspectedTable.getPrimaryKeyColumns()) {
                    if (selectByColumnGeneratorConfiguration.addColumnName(column.getActualColumnName()))
                        selectByColumnGeneratorConfiguration.addColumn(column);
                }
                selectByColumnGeneratorConfiguration.setParameterList(false);
                selectByColumnGeneratorConfiguration.setMethodName(methodName);
                selectByColumnGeneratorConfiguration.setEnableDelete(false);
                selectByColumnGeneratorConfiguration.setReturnType(1);
                this.getSelectByColumnGeneratorConfigurations().add(selectByColumnGeneratorConfiguration);
            }
        }
        if (this.getJavaControllerGeneratorConfiguration().generate && this.getJavaControllerGeneratorConfiguration().isEnableSelectByPrimaryKeys()) {
            if (!introspectedTable.getPrimaryKeyColumns().isEmpty()) {
                String methodName = JavaBeansUtil.byColumnMethodName(introspectedTable.getPrimaryKeyColumns(),true);
                if (this.getSelectByColumnGeneratorConfigurations().stream().noneMatch(t -> methodName.equals(t.getMethodName()))) {
                    SelectByColumnGeneratorConfiguration selectByColumnGeneratorConfiguration = new SelectByColumnGeneratorConfiguration();
                    for (IntrospectedColumn column : introspectedTable.getPrimaryKeyColumns()) {
                        if (selectByColumnGeneratorConfiguration.addColumnName(column.getActualColumnName()))
                            selectByColumnGeneratorConfiguration.addColumn(column);
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

    private void addHtmlElementAdditionalAttribute(HtmlElementDescriptor elementDescriptor, IntrospectedTable introspectedTable) {
        //不需要转换的数据源，直接返回
        if (!stringHasValue(elementDescriptor.getDataSource())) {
            return;
        }
        //javaModel或者voModel中不存在对应的属性，需要追加
        OverridePropertyValueGeneratorConfiguration overrideConfiguration = ConfigUtil.createOverridePropertyConfiguration(elementDescriptor, introspectedTable);
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
}
