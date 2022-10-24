/*
 *    Copyright 2006-2022 the original author or authors.
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
package org.mybatis.generator.api;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.codegen.mybatis3.sqlschema.GeneratedSqlSchemaFile;
import org.mybatis.generator.config.*;
import org.mybatis.generator.custom.pojo.RelationGeneratorConfiguration;
import org.mybatis.generator.custom.pojo.SelectByColumnGeneratorConfiguration;
import org.mybatis.generator.custom.pojo.SelectBySqlMethodGeneratorConfiguration;
import org.mybatis.generator.custom.pojo.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.internal.rules.BaseRules;
import org.mybatis.generator.internal.rules.ConditionalModelRules;
import org.mybatis.generator.internal.rules.FlatModelRules;
import org.mybatis.generator.internal.rules.HierarchicalModelRules;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mybatis.generator.internal.util.StringUtility.isTrue;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * Base class for all code generator implementations. This class provides many
 * of the housekeeping methods needed to implement a code generator, with only
 * the actual code generation methods left unimplemented.
 * <p>
 * 所有代码生成器实现的基类。这个类提供了实现代码生成器所需的许多内务管理方法，只有实际的代码生成方法没有实现。
 *
 * @author Jeff Butler
 */
public abstract class IntrospectedTable {

    public enum TargetRuntime {
        MYBATIS3,
        MYBATIS3_DSQL
    }

    protected enum InternalAttribute {
        ATTR_PRIMARY_KEY_TYPE,
        ATTR_BASE_RECORD_TYPE,
        ATTR_RECORD_WITH_BLOBS_TYPE,
        ATTR_EXAMPLE_TYPE,
        ATTR_MYBATIS3_XML_MAPPER_PACKAGE,
        ATTR_MYBATIS3_XML_MAPPER_FILE_NAME,
        /**
         * also used as XML Mapper namespace if a Java mapper is generated.
         */
        ATTR_MYBATIS3_JAVA_MAPPER_TYPE,
        /**
         * used as XML Mapper namespace if no client is generated.
         */
        ATTR_MYBATIS3_FALLBACK_SQL_MAP_NAMESPACE,
        ATTR_FULLY_QUALIFIED_TABLE_NAME_AT_RUNTIME,
        ATTR_ALIASED_FULLY_QUALIFIED_TABLE_NAME_AT_RUNTIME,
        ATTR_COUNT_BY_EXAMPLE_STATEMENT_ID,
        ATTR_DELETE_BY_EXAMPLE_STATEMENT_ID,
        ATTR_DELETE_BY_PRIMARY_KEY_STATEMENT_ID,
        ATTR_INSERT_STATEMENT_ID,
        ATTR_INSERT_SELECTIVE_STATEMENT_ID,
        ATTR_SELECT_ALL_STATEMENT_ID,
        ATTR_SELECT_BY_EXAMPLE_STATEMENT_ID,
        ATTR_SELECT_BY_EXAMPLE_WITH_BLOBS_STATEMENT_ID,
        ATTR_SELECT_BY_PRIMARY_KEY_STATEMENT_ID,
        ATTR_UPDATE_BY_EXAMPLE_STATEMENT_ID,
        ATTR_UPDATE_BY_EXAMPLE_SELECTIVE_STATEMENT_ID,
        ATTR_UPDATE_BY_EXAMPLE_WITH_BLOBS_STATEMENT_ID,
        ATTR_UPDATE_BY_PRIMARY_KEY_STATEMENT_ID,
        ATTR_UPDATE_BY_PRIMARY_KEY_SELECTIVE_STATEMENT_ID,
        ATTR_UPDATE_BY_PRIMARY_KEY_WITH_BLOBS_STATEMENT_ID,
        ATTR_BASE_RESULT_MAP_ID,
        ATTR_RESULT_MAP_WITH_BLOBS_ID,
        ATTR_EXAMPLE_WHERE_CLAUSE_ID,
        ATTR_BASE_COLUMN_LIST_ID,
        ATTR_BLOB_COLUMN_LIST_ID,
        ATTR_MYBATIS3_UPDATE_BY_EXAMPLE_WHERE_CLAUSE_ID,
        ATTR_MYBATIS3_SQL_PROVIDER_TYPE,
        ATTR_MYBATIS_DYNAMIC_SQL_SUPPORT_TYPE,
        ATTR_KOTLIN_RECORD_TYPE,
        ATTR_MYBATIS_DYNAMIC_SQL_TABLE_OBJECT_NAME,
        /*CONTROLLER相关*/
        ATTR_CONTROL_BASE_REQUEST_MAPPING,
        ATTR_CONTROL_BEAN_NAME,
        ATTR_RELATION_RESULT_MAP_ID,
        ATTR_SELECT_BY_EXAMPLE_WITH_RELATION_STATEMENT_ID,
        ATTR_SELECT_BASE_BY_PRIMARY_KEY_STATEMENT_ID
    }

    protected TableConfiguration tableConfiguration;

    protected FullyQualifiedTable fullyQualifiedTable;

    protected Context context;

    protected BaseRules rules;

    protected TargetRuntime targetRuntime;

    protected final List<IntrospectedColumn> primaryKeyColumns = new ArrayList<>();

    protected final List<IntrospectedColumn> baseColumns = new ArrayList<>();

    protected final List<IntrospectedColumn> blobColumns = new ArrayList<>();

    protected Map<String, String> permissionDataScriptLines = new HashMap<>();

    protected List<RelationGeneratorConfiguration> relationGeneratorConfigurations = new ArrayList<>();

    protected Map<String, List<String>> topLevelClassExampleFields = new HashMap<>();

    /**
     * Attributes may be used by plugins to capture table related state between
     * the different plugin calls.
     */
    protected final Map<String, Object> attributes = new HashMap<>();


    /**
     * Internal attributes are used to store commonly accessed items by all code generators.
     */
    protected final Map<IntrospectedTable.InternalAttribute, String> internalAttributes =
            new EnumMap<>(InternalAttribute.class);
    /**
     * Table remarks retrieved from database metadata.
     */
    protected String remarks;

    /**
     * Table type retrieved from database metadata.
     */
    protected String tableType;

    protected IntrospectedTable(TargetRuntime targetRuntime) {
        this.targetRuntime = targetRuntime;
    }

    public FullyQualifiedTable getFullyQualifiedTable() {
        return fullyQualifiedTable;
    }

    public String getSelectByExampleQueryId() {
        return tableConfiguration.getSelectByExampleQueryId();
    }

    public String getSelectByPrimaryKeyQueryId() {
        return tableConfiguration.getSelectByPrimaryKeyQueryId();
    }

    public Optional<GeneratedKey> getGeneratedKey() {
        return tableConfiguration.getGeneratedKey();
    }

    public Optional<IntrospectedColumn> getColumn(String columnName) {
        return Stream.of(primaryKeyColumns.stream(), baseColumns.stream(), blobColumns.stream())
                .flatMap(Function.identity())
                .filter(ic -> columnMatches(ic, columnName))
                .findFirst();
    }

    private boolean columnMatches(IntrospectedColumn introspectedColumn, String columnName) {
        if (introspectedColumn.isColumnNameDelimited()) {
            return introspectedColumn.getActualColumnName().equals(columnName);
        } else {
            return introspectedColumn.getActualColumnName().equalsIgnoreCase(columnName);
        }
    }

    /**
     * Returns true if any of the columns in the table are JDBC Dates (as
     * opposed to timestamps).
     *
     * @return true if the table contains DATE columns
     */
    public boolean hasJDBCDateColumns() {
        return Stream.of(primaryKeyColumns.stream(), baseColumns.stream())
                .flatMap(Function.identity())
                .anyMatch(IntrospectedColumn::isJDBCDateColumn);
    }

    /**
     * Returns true if any of the columns in the table are JDBC Times (as
     * opposed to timestamps).
     *
     * @return true if the table contains TIME columns
     */
    public boolean hasJDBCTimeColumns() {
        return Stream.of(primaryKeyColumns.stream(), baseColumns.stream())
                .flatMap(Function.identity())
                .anyMatch(IntrospectedColumn::isJDBCTimeColumn);
    }

    /**
     * Returns the columns in the primary key. If the generatePrimaryKeyClass()
     * method returns false, then these columns will be iterated as the
     * parameters of the selectByPrimaryKay and deleteByPrimaryKey methods
     *
     * @return a List of ColumnDefinition objects for columns in the primary key
     */
    public List<IntrospectedColumn> getPrimaryKeyColumns() {
        return primaryKeyColumns;
    }

    public boolean hasPrimaryKeyColumns() {
        return !primaryKeyColumns.isEmpty();
    }

    public List<IntrospectedColumn> getBaseColumns() {
        return baseColumns;
    }

    /**
     * Returns all columns in the table (for use by the select by primary key and
     * select by example with BLOBs methods).
     *
     * @return a List of ColumnDefinition objects for all columns in the table
     */
    public List<IntrospectedColumn> getAllColumns() {
        return Stream.of(primaryKeyColumns.stream(), baseColumns.stream(), blobColumns.stream())
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    /**
     * Returns all columns except BLOBs (for use by the select by example without BLOBs method).
     *
     * @return a List of ColumnDefinition objects for columns in the table that are non BLOBs
     */
    public List<IntrospectedColumn> getNonBLOBColumns() {
        return Stream.of(primaryKeyColumns.stream(), baseColumns.stream())
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    public int getNonBLOBColumnCount() {
        return primaryKeyColumns.size() + baseColumns.size();
    }

    public List<IntrospectedColumn> getNonPrimaryKeyColumns() {
        return Stream.of(baseColumns.stream(), blobColumns.stream())
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    public List<IntrospectedColumn> getBLOBColumns() {
        return blobColumns;
    }

    public boolean hasBLOBColumns() {
        return !blobColumns.isEmpty();
    }

    public boolean hasBaseColumns() {
        return !baseColumns.isEmpty();
    }

    public BaseRules getRules() {
        return rules;
    }

    public String getTableConfigurationProperty(String property) {
        return tableConfiguration.getProperty(property);
    }

    public String getPrimaryKeyType() {
        return internalAttributes.get(InternalAttribute.ATTR_PRIMARY_KEY_TYPE);
    }

    /**
     * Gets the base record type.
     *
     * @return the type for the record (the class that holds non-primary key and non-BLOB fields). Note that the value
     * will be calculated regardless of whether the table has these columns or not.
     */
    public String getBaseRecordType() {
        return internalAttributes.get(InternalAttribute.ATTR_BASE_RECORD_TYPE);
    }

    public String getKotlinRecordType() {
        return internalAttributes.get(InternalAttribute.ATTR_KOTLIN_RECORD_TYPE);
    }

    /**
     * Gets the example type.
     *
     * @return the type for the example class.
     */
    public String getExampleType() {
        return internalAttributes.get(InternalAttribute.ATTR_EXAMPLE_TYPE);
    }

    /**
     * Gets the record with blo bs type.
     *
     * @return the type for the record with BLOBs class. Note that the value will be calculated regardless of whether
     * the table has BLOB columns or not.
     */
    public String getRecordWithBLOBsType() {
        return internalAttributes
                .get(InternalAttribute.ATTR_RECORD_WITH_BLOBS_TYPE);
    }

    public String getMyBatis3SqlMapNamespace() {
        String namespace = getMyBatis3JavaMapperType();
        if (namespace == null) {
            namespace = getMyBatis3FallbackSqlMapNamespace();
        }

        return namespace;
    }

    public String getMyBatis3FallbackSqlMapNamespace() {
        return internalAttributes
                .get(InternalAttribute.ATTR_MYBATIS3_FALLBACK_SQL_MAP_NAMESPACE);
    }

    public boolean hasAnyColumns() {
        return hasPrimaryKeyColumns() || hasBaseColumns() || hasBLOBColumns();
    }

    public void setTableConfiguration(TableConfiguration tableConfiguration) {
        this.tableConfiguration = tableConfiguration;
    }

    public void setFullyQualifiedTable(FullyQualifiedTable fullyQualifiedTable) {
        this.fullyQualifiedTable = fullyQualifiedTable;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void addColumn(IntrospectedColumn introspectedColumn) {
        if (introspectedColumn.isBLOBColumn()) {
            blobColumns.add(introspectedColumn);
        } else {
            baseColumns.add(introspectedColumn);
        }

        introspectedColumn.setIntrospectedTable(this);
    }

    public void addPrimaryKeyColumn(String columnName) {
        boolean found = false;
        // first search base columns
        Iterator<IntrospectedColumn> iter = baseColumns.iterator();
        while (iter.hasNext()) {
            IntrospectedColumn introspectedColumn = iter.next();
            if (introspectedColumn.getActualColumnName().equals(columnName)) {
                primaryKeyColumns.add(introspectedColumn);
                iter.remove();
                found = true;
                break;
            }
        }

        // search blob columns in the weird event that a blob is the primary key
        if (!found) {
            iter = blobColumns.iterator();
            while (iter.hasNext()) {
                IntrospectedColumn introspectedColumn = iter.next();
                if (introspectedColumn.getActualColumnName().equals(columnName)) {
                    primaryKeyColumns.add(introspectedColumn);
                    iter.remove();
                    break;
                }
            }
        }
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public void initialize() {

        calculateJavaClientAttributes();
        calculateModelAttributes();
        calculateXmlAttributes();
        calculateSelectByTableProperty();
        calculateControllerAttributes();
        calculateHtmlAttributes();
        calculateRelationProperty();
        calculateSelectBySqlMethodProperty();

        if (tableConfiguration.getModelType() == ModelType.HIERARCHICAL) {
            rules = new HierarchicalModelRules(this);
        } else if (tableConfiguration.getModelType() == ModelType.FLAT) {
            rules = new FlatModelRules(this);
        } else {
            rules = new ConditionalModelRules(this);
        }

        context.getPlugins().initialized(this);
    }

    private void calculateSelectByTableProperty() {
        this.getTableConfiguration().getSelectByTableGeneratorConfiguration().forEach(c -> {
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

    protected void calculateRelationProperty() {
        TableConfiguration tableConfiguration = this.getTableConfiguration();
        if (tableConfiguration.getRelationGeneratorConfigurations().size() > 0) {
            this.getRelationGeneratorConfigurations().addAll(tableConfiguration.getRelationGeneratorConfigurations());

        }
    }

    protected void calculateSelectBySqlMethodProperty() {
        //计算SelectBySqlMethod列
        for (SelectBySqlMethodGeneratorConfiguration bySqlMethodGeneratorConfiguration : tableConfiguration.getSelectBySqlMethodGeneratorConfigurations()) {
            bySqlMethodGeneratorConfiguration.setParentIdColumn(this.getColumn(bySqlMethodGeneratorConfiguration.getParentIdColumnName()).orElse(null));
            bySqlMethodGeneratorConfiguration.setPrimaryKeyColumn(this.getColumn(bySqlMethodGeneratorConfiguration.getPrimaryKeyColumnName()).orElse(null));
        }
        List<SelectBySqlMethodGeneratorConfiguration> collect = tableConfiguration.getSelectBySqlMethodGeneratorConfigurations().stream()
                .peek(c -> {
                    c.setParentIdColumn(this.getColumn(c.getParentIdColumnName()).orElse(null));
                    c.setPrimaryKeyColumn(this.getColumn(c.getPrimaryKeyColumnName()).orElse(null));
                })
                .filter(c -> c.getPrimaryKeyColumn() != null && c.getParentIdColumn() != null)
                .collect(Collectors.toList());
        tableConfiguration.setSelectBySqlMethodGeneratorConfigurations(collect);

        //生成SelectByTable基于关系表主键的查询方法
        if (tableConfiguration.getSelectByTableGeneratorConfiguration().size() > 0) {
            for (SelectByTableGeneratorConfiguration selectByTableGeneratorConfiguration : tableConfiguration.getSelectByTableGeneratorConfiguration()) {
                selectByTableGeneratorConfiguration.setParameterName(JavaBeansUtil.getCamelCaseString(selectByTableGeneratorConfiguration.getOtherPrimaryKeyColumn(), false));
            }
        }

        //生成selectByColumn查询方法
        if (tableConfiguration.getSelectByColumnGeneratorConfigurations().size() > 0) {
            for (SelectByColumnGeneratorConfiguration configuration : tableConfiguration.getSelectByColumnGeneratorConfigurations()) {
                configuration.getColumnNames().forEach(n-> getColumn(n).ifPresent(configuration::addColumn));
                configuration.setMethodName(JavaBeansUtil.byColumnMethodName(configuration.getColumns()) + (configuration.getParameterList() ? "s" : ""));
                configuration.setDeleteMethodName(JavaBeansUtil.deleteByColumnMethodName(configuration.getColumns()) + (configuration.getParameterList() ? "s" : ""));
            }
        }

        //追加一个基于主键的查询，用来区分selectByPrimaryKey方法，避免过多查询
        long relationCount = this.getRelationGeneratorConfigurations().stream().filter(RelationGeneratorConfiguration::isSubSelected).count();
        if (relationCount > 0) {
            internalAttributes.put(InternalAttribute.ATTR_SELECT_BASE_BY_PRIMARY_KEY_STATEMENT_ID, "selectBaseByPrimaryKey");
            String selectBaseByPrimaryKeyStatementId = this.getSelectBaseByPrimaryKeyStatementId();
            if (this.getPrimaryKeyColumns().size() == 1) {
                String actualColumnName = this.getPrimaryKeyColumns().get(0).getActualColumnName();
                long count = 0;
                if (tableConfiguration.getSelectByColumnGeneratorConfigurations().size() > 0) {
                    count = tableConfiguration.getSelectByColumnGeneratorConfigurations().stream().filter(t -> t.getMethodName().equals(selectBaseByPrimaryKeyStatementId)).count();
                }
                if (count == 0) {
                    SelectByColumnGeneratorConfiguration selectByColumnGeneratorConfiguration = new SelectByColumnGeneratorConfiguration(actualColumnName);
                    selectByColumnGeneratorConfiguration.addColumn(this.getPrimaryKeyColumns().get(0));
                    selectByColumnGeneratorConfiguration.setMethodName(selectBaseByPrimaryKeyStatementId);
                    selectByColumnGeneratorConfiguration.setReturnType(1);
                    tableConfiguration.getSelectByColumnGeneratorConfigurations().add(selectByColumnGeneratorConfiguration);
                }
            }
        }

        List<SelectByColumnGeneratorConfiguration> collect1 = tableConfiguration.getSelectByColumnGeneratorConfigurations().stream().distinct().collect(Collectors.toList());
        tableConfiguration.setSelectByColumnGeneratorConfigurations(collect1);

        //再看其他

    }

    protected void calculateControllerAttributes() {
        //base Package
        internalAttributes.put(InternalAttribute.ATTR_CONTROL_BASE_REQUEST_MAPPING, context.getModuleKeyword().toLowerCase());
        String entityName;
        if (fullyQualifiedTable.getDomainObjectName() != null) {
            entityName = JavaBeansUtil.getFirstCharacterLowercase(fullyQualifiedTable.getDomainObjectName());
        } else {
            entityName = JavaBeansUtil.getCamelCaseString(fullyQualifiedTable.getIntrospectedTableName(), true);
        }
        String beanName = entityName + "Impl";
        internalAttributes.put(InternalAttribute.ATTR_CONTROL_BEAN_NAME, beanName);

    }

    protected void calculateHtmlAttributes() {
        //重新计算不为空字段，根据数据库字段不为空属性，追加数据库表不允许空的字段
        final List<IntrospectedColumn> noNullableColumns = this.getAllColumns().stream().filter(c -> !c.isNullable()).collect(Collectors.toList());
        if (noNullableColumns.size() > 0) {
            List<String> noNullableColumnNames = noNullableColumns.stream().map(c -> c.getActualColumnName().toUpperCase()).collect(Collectors.toList());
            this.getTableConfiguration().getHtmlMapGeneratorConfigurations()
                    .forEach(htmlConfiguration -> htmlConfiguration.getElementRequired().addAll(noNullableColumnNames));
        }
    }

    protected void calculateXmlAttributes() {
        setMyBatis3XmlMapperFileName(calculateMyBatis3XmlMapperFileName());
        setMyBatis3XmlMapperPackage(calculateSqlMapPackage());

        setMyBatis3FallbackSqlMapNamespace(calculateMyBatis3FallbackSqlMapNamespace());

        setSqlMapFullyQualifiedRuntimeTableName(calculateSqlMapFullyQualifiedRuntimeTableName());
        setSqlMapAliasedFullyQualifiedRuntimeTableName(calculateSqlMapAliasedFullyQualifiedRuntimeTableName());

        setCountByExampleStatementId("countByExample"); //$NON-NLS-1$
        setDeleteByExampleStatementId("deleteByExample"); //$NON-NLS-1$
        setDeleteByPrimaryKeyStatementId("deleteByPrimaryKey"); //$NON-NLS-1$
        setInsertStatementId("insert"); //$NON-NLS-1$
        setInsertSelectiveStatementId("insertSelective"); //$NON-NLS-1$
        setSelectAllStatementId("selectAll"); //$NON-NLS-1$
        setSelectByExampleStatementId("selectByExample"); //$NON-NLS-1$
        setSelectByExampleWithBLOBsStatementId("selectByExampleWithBLOBs"); //$NON-NLS-1$
        setSelectByPrimaryKeyStatementId("selectByPrimaryKey"); //$NON-NLS-1$
        setUpdateByExampleStatementId("updateByExample"); //$NON-NLS-1$
        setUpdateByExampleSelectiveStatementId("updateByExampleSelective"); //$NON-NLS-1$
        setUpdateByExampleWithBLOBsStatementId("updateByExampleWithBLOBs"); //$NON-NLS-1$
        setUpdateByPrimaryKeyStatementId("updateByPrimaryKey"); //$NON-NLS-1$
        setUpdateByPrimaryKeySelectiveStatementId("updateByPrimaryKeySelective"); //$NON-NLS-1$
        setUpdateByPrimaryKeyWithBLOBsStatementId("updateByPrimaryKeyWithBLOBs"); //$NON-NLS-1$
        setBaseResultMapId("BaseResultMap"); //$NON-NLS-1$
        setRelationResultMapId("ResultMapRelation");
        setResultMapWithBLOBsId("ResultMapWithBLOBs"); //$NON-NLS-1$
        setExampleWhereClauseId("Example_Where_Clause"); //$NON-NLS-1$
        setBaseColumnListId("Base_Column_List"); //$NON-NLS-1$
        setBlobColumnListId("Blob_Column_List"); //$NON-NLS-1$
        setMyBatis3UpdateByExampleWhereClauseId("Update_By_Example_Where_Clause");
        setSelectByExampleWithRelationStatementId("selectByExampleWithRelation");
    }

    public void setBlobColumnListId(String s) {
        internalAttributes.put(InternalAttribute.ATTR_BLOB_COLUMN_LIST_ID, s);
    }

    public void setBaseColumnListId(String s) {
        internalAttributes.put(InternalAttribute.ATTR_BASE_COLUMN_LIST_ID, s);
    }

    public void setExampleWhereClauseId(String s) {
        internalAttributes.put(InternalAttribute.ATTR_EXAMPLE_WHERE_CLAUSE_ID,
                s);
    }

    public void setMyBatis3UpdateByExampleWhereClauseId(String s) {
        internalAttributes.put(
                InternalAttribute.ATTR_MYBATIS3_UPDATE_BY_EXAMPLE_WHERE_CLAUSE_ID,
                s);
    }

    public void setResultMapWithBLOBsId(String s) {
        internalAttributes.put(InternalAttribute.ATTR_RESULT_MAP_WITH_BLOBS_ID,
                s);
    }

    public void setBaseResultMapId(String s) {
        internalAttributes.put(InternalAttribute.ATTR_BASE_RESULT_MAP_ID, s);
    }

    public void setRelationResultMapId(String s) {
        internalAttributes.put(InternalAttribute.ATTR_RELATION_RESULT_MAP_ID, s);
    }

    public void setUpdateByPrimaryKeyWithBLOBsStatementId(String s) {
        internalAttributes.put(InternalAttribute.ATTR_UPDATE_BY_PRIMARY_KEY_WITH_BLOBS_STATEMENT_ID, s);
    }

    public void setUpdateByPrimaryKeySelectiveStatementId(String s) {
        internalAttributes
                .put(
                        InternalAttribute.ATTR_UPDATE_BY_PRIMARY_KEY_SELECTIVE_STATEMENT_ID,
                        s);
    }

    public void setUpdateByPrimaryKeyStatementId(String s) {
        internalAttributes.put(
                InternalAttribute.ATTR_UPDATE_BY_PRIMARY_KEY_STATEMENT_ID, s);
    }

    public void setUpdateByExampleWithBLOBsStatementId(String s) {
        internalAttributes.put(
                InternalAttribute.ATTR_UPDATE_BY_EXAMPLE_WITH_BLOBS_STATEMENT_ID,
                s);
    }

    public void setUpdateByExampleSelectiveStatementId(String s) {
        internalAttributes
                .put(
                        InternalAttribute.ATTR_UPDATE_BY_EXAMPLE_SELECTIVE_STATEMENT_ID,
                        s);
    }

    public void setUpdateByExampleStatementId(String s) {
        internalAttributes.put(
                InternalAttribute.ATTR_UPDATE_BY_EXAMPLE_STATEMENT_ID, s);
    }

    public void setSelectByPrimaryKeyStatementId(String s) {
        internalAttributes.put(
                InternalAttribute.ATTR_SELECT_BY_PRIMARY_KEY_STATEMENT_ID, s);
    }

    public void setSelectByExampleWithBLOBsStatementId(String s) {
        internalAttributes
                .put(
                        InternalAttribute.ATTR_SELECT_BY_EXAMPLE_WITH_BLOBS_STATEMENT_ID,
                        s);
    }

    public void setSelectAllStatementId(String s) {
        internalAttributes.put(
                InternalAttribute.ATTR_SELECT_ALL_STATEMENT_ID, s);
    }

    public void setSelectByExampleStatementId(String s) {
        internalAttributes.put(InternalAttribute.ATTR_SELECT_BY_EXAMPLE_STATEMENT_ID, s);
    }

    public void setSelectByExampleWithRelationStatementId(String s) {
        internalAttributes.put(InternalAttribute.ATTR_SELECT_BY_EXAMPLE_WITH_RELATION_STATEMENT_ID, s);
    }

    public void setInsertSelectiveStatementId(String s) {
        internalAttributes.put(
                InternalAttribute.ATTR_INSERT_SELECTIVE_STATEMENT_ID, s);
    }

    public void setInsertStatementId(String s) {
        internalAttributes.put(InternalAttribute.ATTR_INSERT_STATEMENT_ID, s);
    }

    public void setDeleteByPrimaryKeyStatementId(String s) {
        internalAttributes.put(
                InternalAttribute.ATTR_DELETE_BY_PRIMARY_KEY_STATEMENT_ID, s);
    }

    public void setDeleteByExampleStatementId(String s) {
        internalAttributes.put(
                InternalAttribute.ATTR_DELETE_BY_EXAMPLE_STATEMENT_ID, s);
    }

    public void setCountByExampleStatementId(String s) {
        internalAttributes.put(
                InternalAttribute.ATTR_COUNT_BY_EXAMPLE_STATEMENT_ID, s);
    }

    public String getControllerBeanName() {
        return internalAttributes.get(InternalAttribute.ATTR_CONTROL_BEAN_NAME);
    }

    public String getControllerSimplePackage() {
        return internalAttributes.get(InternalAttribute.ATTR_CONTROL_BASE_REQUEST_MAPPING);
    }

    public String getBlobColumnListId() {
        return internalAttributes.get(InternalAttribute.ATTR_BLOB_COLUMN_LIST_ID);
    }

    public String getBaseColumnListId() {
        return internalAttributes
                .get(InternalAttribute.ATTR_BASE_COLUMN_LIST_ID);
    }

    public String getExampleWhereClauseId() {
        return internalAttributes
                .get(InternalAttribute.ATTR_EXAMPLE_WHERE_CLAUSE_ID);
    }

    public String getMyBatis3UpdateByExampleWhereClauseId() {
        return internalAttributes
                .get(InternalAttribute.ATTR_MYBATIS3_UPDATE_BY_EXAMPLE_WHERE_CLAUSE_ID);
    }

    public String getResultMapWithBLOBsId() {
        return internalAttributes
                .get(InternalAttribute.ATTR_RESULT_MAP_WITH_BLOBS_ID);
    }

    public String getBaseResultMapId() {
        return internalAttributes.get(InternalAttribute.ATTR_BASE_RESULT_MAP_ID);
    }

    public String getRelationResultMapId() {
        return internalAttributes.get(InternalAttribute.ATTR_RELATION_RESULT_MAP_ID);
    }

    public String getUpdateByPrimaryKeyWithBLOBsStatementId() {
        return internalAttributes
                .get(InternalAttribute.ATTR_UPDATE_BY_PRIMARY_KEY_WITH_BLOBS_STATEMENT_ID);
    }

    public String getUpdateByPrimaryKeySelectiveStatementId() {
        return internalAttributes
                .get(InternalAttribute.ATTR_UPDATE_BY_PRIMARY_KEY_SELECTIVE_STATEMENT_ID);
    }

    public String getUpdateByPrimaryKeyStatementId() {
        return internalAttributes
                .get(InternalAttribute.ATTR_UPDATE_BY_PRIMARY_KEY_STATEMENT_ID);
    }

    public String getUpdateByExampleWithBLOBsStatementId() {
        return internalAttributes
                .get(InternalAttribute.ATTR_UPDATE_BY_EXAMPLE_WITH_BLOBS_STATEMENT_ID);
    }

    public String getUpdateByExampleSelectiveStatementId() {
        return internalAttributes
                .get(InternalAttribute.ATTR_UPDATE_BY_EXAMPLE_SELECTIVE_STATEMENT_ID);
    }

    public String getUpdateByExampleStatementId() {
        return internalAttributes
                .get(InternalAttribute.ATTR_UPDATE_BY_EXAMPLE_STATEMENT_ID);
    }

    public String getSelectByPrimaryKeyStatementId() {
        return internalAttributes.get(InternalAttribute.ATTR_SELECT_BY_PRIMARY_KEY_STATEMENT_ID);
    }

    public String getSelectBaseByPrimaryKeyStatementId() {
        return internalAttributes.get(InternalAttribute.ATTR_SELECT_BASE_BY_PRIMARY_KEY_STATEMENT_ID);
    }

    public String getSelectByExampleWithBLOBsStatementId() {
        return internalAttributes
                .get(InternalAttribute.ATTR_SELECT_BY_EXAMPLE_WITH_BLOBS_STATEMENT_ID);
    }

    public String getSelectAllStatementId() {
        return internalAttributes
                .get(InternalAttribute.ATTR_SELECT_ALL_STATEMENT_ID);
    }

    public String getSelectByExampleStatementId() {
        return internalAttributes.get(InternalAttribute.ATTR_SELECT_BY_EXAMPLE_STATEMENT_ID);
    }

    public String getSelectByExampleWithRelationStatementId() {
        return internalAttributes.get(InternalAttribute.ATTR_SELECT_BY_EXAMPLE_WITH_RELATION_STATEMENT_ID);
    }

    public String getInsertBatchStatementId() {
        return "insertBatch";
    }

    public String getUpdateBatchStatementId() {
        return "updateBatchByPrimaryKey";
    }

    public String getInsertOrUpdateStatementId() {
        return "insertOrUpdate";
    }

    public String getSelectByKeysDictStatementId() {
        return "SelectByKeysDict";
    }

    public String getUpdateBySqlStatementId() {
        return "updateBySql";
    }

    public String getInsertSelectiveStatementId() {
        return internalAttributes
                .get(InternalAttribute.ATTR_INSERT_SELECTIVE_STATEMENT_ID);
    }

    public String getInsertStatementId() {
        return internalAttributes
                .get(InternalAttribute.ATTR_INSERT_STATEMENT_ID);
    }

    public String getDeleteByPrimaryKeyStatementId() {
        return internalAttributes
                .get(InternalAttribute.ATTR_DELETE_BY_PRIMARY_KEY_STATEMENT_ID);
    }

    public String getDeleteByExampleStatementId() {
        return internalAttributes
                .get(InternalAttribute.ATTR_DELETE_BY_EXAMPLE_STATEMENT_ID);
    }

    public String getCountByExampleStatementId() {
        return internalAttributes
                .get(InternalAttribute.ATTR_COUNT_BY_EXAMPLE_STATEMENT_ID);
    }

    public String getMyBatisDynamicSQLTableObjectName() {
        return internalAttributes.get(InternalAttribute.ATTR_MYBATIS_DYNAMIC_SQL_TABLE_OBJECT_NAME);
    }

    public void setMyBatisDynamicSQLTableObjectName(String name) {
        internalAttributes.put(InternalAttribute.ATTR_MYBATIS_DYNAMIC_SQL_TABLE_OBJECT_NAME, name);
    }

    private boolean isSubPackagesEnabled(PropertyHolder propertyHolder) {
        return isTrue(propertyHolder.getProperty(PropertyRegistry.ANY_ENABLE_SUB_PACKAGES));
    }

    protected String calculateJavaClientInterfacePackage() {
        JavaClientGeneratorConfiguration config = context
                .getJavaClientGeneratorConfiguration();
        if (config == null) {
            return null;
        }

        return config.getTargetPackage()
                + fullyQualifiedTable.getSubPackageForClientOrSqlMap(isSubPackagesEnabled(config));
    }

    protected String calculateDynamicSqlSupportPackage() {
        JavaClientGeneratorConfiguration config = context
                .getJavaClientGeneratorConfiguration();
        if (config == null) {
            return null;
        }

        String cds_package = config.getProperty(PropertyRegistry.CLIENT_DYNAMIC_SQL_SUPPORT_PACKAGE);
        if (stringHasValue(cds_package)) {
            return cds_package + fullyQualifiedTable.getSubPackageForClientOrSqlMap(isSubPackagesEnabled(config));
        } else {
            return calculateJavaClientInterfacePackage();
        }
    }

    protected void calculateJavaClientAttributes() {
        if (context.getJavaClientGeneratorConfiguration() == null) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(calculateJavaClientInterfacePackage());
        sb.append('.');
        if (stringHasValue(tableConfiguration.getMapperName())) {
            sb.append(tableConfiguration.getMapperName());
        } else {
            if (stringHasValue(fullyQualifiedTable.getDomainObjectSubPackage())) {
                sb.append(fullyQualifiedTable.getDomainObjectSubPackage());
                sb.append('.');
            }
            sb.append(fullyQualifiedTable.getDomainObjectName());
            sb.append("Mapper"); //$NON-NLS-1$
        }
        setMyBatis3JavaMapperType(sb.toString());

        sb.setLength(0);
        sb.append(calculateJavaClientInterfacePackage());
        sb.append('.');
        if (stringHasValue(tableConfiguration.getSqlProviderName())) {
            sb.append(tableConfiguration.getSqlProviderName());
        } else {
            if (stringHasValue(fullyQualifiedTable.getDomainObjectSubPackage())) {
                sb.append(fullyQualifiedTable.getDomainObjectSubPackage());
                sb.append('.');
            }
            sb.append(fullyQualifiedTable.getDomainObjectName());
            sb.append("SqlProvider"); //$NON-NLS-1$
        }
        setMyBatis3SqlProviderType(sb.toString());

        sb.setLength(0);
        sb.append(calculateDynamicSqlSupportPackage());
        sb.append('.');
        if (stringHasValue(tableConfiguration.getDynamicSqlSupportClassName())) {
            sb.append(tableConfiguration.getDynamicSqlSupportClassName());
        } else {
            if (stringHasValue(fullyQualifiedTable.getDomainObjectSubPackage())) {
                sb.append(fullyQualifiedTable.getDomainObjectSubPackage());
                sb.append('.');
            }
            sb.append(fullyQualifiedTable.getDomainObjectName());
            sb.append("DynamicSqlSupport"); //$NON-NLS-1$
        }
        setMyBatisDynamicSqlSupportType(sb.toString());

        if (stringHasValue(tableConfiguration.getDynamicSqlTableObjectName())) {
            setMyBatisDynamicSQLTableObjectName(tableConfiguration.getDynamicSqlTableObjectName());
        } else {
            setMyBatisDynamicSQLTableObjectName(fullyQualifiedTable.getDomainObjectName());
        }
    }

    protected String calculateJavaModelPackage() {
        JavaModelGeneratorConfiguration config = context
                .getJavaModelGeneratorConfiguration();

        return config.getTargetPackage()
                + fullyQualifiedTable.getSubPackageForModel(isSubPackagesEnabled(config));
    }

    protected void calculateModelAttributes() {
        String jm_package = calculateJavaModelPackage();

        StringBuilder sb = new StringBuilder();
        sb.append(jm_package);
        sb.append('.');
        sb.append(fullyQualifiedTable.getDomainObjectName());
        sb.append("Key"); //$NON-NLS-1$
        setPrimaryKeyType(sb.toString());

        sb.setLength(0);
        sb.append(jm_package);
        sb.append('.');
        sb.append(fullyQualifiedTable.getDomainObjectName());
        setBaseRecordType(sb.toString());

        sb.setLength(0);
        sb.append(jm_package);
        sb.append('.');
        sb.append(fullyQualifiedTable.getDomainObjectName());
        setKotlinRecordType(sb.toString());

        sb.setLength(0);
        sb.append(jm_package);
        sb.append('.');
        sb.append(fullyQualifiedTable.getDomainObjectName());
        sb.append("WithBLOBs"); //$NON-NLS-1$
        setRecordWithBLOBsType(sb.toString());

        String exampleTargetPackage = calculateJavaModelExamplePackage();
        sb.setLength(0);
        sb.append(exampleTargetPackage);
        sb.append('.');
        sb.append(fullyQualifiedTable.getDomainObjectName());
        sb.append("Example"); //$NON-NLS-1$
        setExampleType(sb.toString());
    }

    /**
     * If property exampleTargetPackage specified for example use the specified value, else
     * use default value (targetPackage).
     *
     * @return the calculated package
     */
    protected String calculateJavaModelExamplePackage() {
        JavaModelGeneratorConfiguration config = context.getJavaModelGeneratorConfiguration();
        String exampleTargetPackage = config.getProperty(PropertyRegistry.MODEL_GENERATOR_EXAMPLE_PACKAGE);
        if (!stringHasValue(exampleTargetPackage)) {
            return calculateJavaModelPackage();
        }

        return exampleTargetPackage
                + fullyQualifiedTable.getSubPackageForModel(isSubPackagesEnabled(config));
    }

    protected String calculateSqlMapPackage() {
        StringBuilder sb = new StringBuilder();
        SqlMapGeneratorConfiguration config = context
                .getSqlMapGeneratorConfiguration();

        // config can be null if the Java client does not require XML
        if (config != null) {
            sb.append(config.getTargetPackage());
            sb.append(fullyQualifiedTable.getSubPackageForClientOrSqlMap(isSubPackagesEnabled(config)));
            if (stringHasValue(tableConfiguration.getMapperName())) {
                String mapperName = tableConfiguration.getMapperName();
                int ind = mapperName.lastIndexOf('.');
                if (ind != -1) {
                    sb.append('.').append(mapperName, 0, ind);
                }
            } else if (stringHasValue(fullyQualifiedTable.getDomainObjectSubPackage())) {
                sb.append('.').append(fullyQualifiedTable.getDomainObjectSubPackage());
            }
        }

        return sb.toString();
    }

    protected String calculateMyBatis3XmlMapperFileName() {
        StringBuilder sb = new StringBuilder();
        if (stringHasValue(tableConfiguration.getMapperName())) {
            String mapperName = tableConfiguration.getMapperName();
            int ind = mapperName.lastIndexOf('.');
            if (ind == -1) {
                sb.append(mapperName);
            } else {
                sb.append(mapperName.substring(ind + 1));
            }
            sb.append(".xml"); //$NON-NLS-1$
        } else {
            sb.append(fullyQualifiedTable.getDomainObjectName());
            sb.append("Mapper.xml"); //$NON-NLS-1$
        }
        return sb.toString();
    }

    protected String calculateMyBatis3FallbackSqlMapNamespace() {
        StringBuilder sb = new StringBuilder();
        sb.append(calculateSqlMapPackage());
        sb.append('.');
        if (stringHasValue(tableConfiguration.getMapperName())) {
            sb.append(tableConfiguration.getMapperName());
        } else {
            sb.append(fullyQualifiedTable.getDomainObjectName());
            sb.append("Mapper"); //$NON-NLS-1$
        }
        return sb.toString();
    }

    protected String calculateSqlMapFullyQualifiedRuntimeTableName() {
        return fullyQualifiedTable.getFullyQualifiedTableNameAtRuntime();
    }

    protected String calculateSqlMapAliasedFullyQualifiedRuntimeTableName() {
        return fullyQualifiedTable.getAliasedFullyQualifiedTableNameAtRuntime();
    }

    public String getFullyQualifiedTableNameAtRuntime() {
        return internalAttributes
                .get(InternalAttribute.ATTR_FULLY_QUALIFIED_TABLE_NAME_AT_RUNTIME);
    }

    public String getAliasedFullyQualifiedTableNameAtRuntime() {
        return internalAttributes
                .get(InternalAttribute.ATTR_ALIASED_FULLY_QUALIFIED_TABLE_NAME_AT_RUNTIME);
    }

    /**
     * This method can be used to initialize the generators before they will be called.
     *
     * <p>This method is called after all the setX methods, but before getNumberOfSubtasks(), getGeneratedJavaFiles, and
     * getGeneratedXmlFiles.
     *
     * @param warnings         the warnings
     * @param progressCallback the progress callback
     */
    public abstract void calculateGenerators(List<String> warnings,
                                             ProgressCallback progressCallback);

    /**
     * This method should return a list of generated Java files related to this
     * table. This list could include various types of model classes, as well as
     * DAO classes.
     *
     * @return the list of generated Java files for this table
     */
    public abstract List<GeneratedJavaFile> getGeneratedJavaFiles();

    /**
     * This method should return a list of generated XML files related to this
     * table. Most implementations will only return one file - the generated
     * SqlMap file.
     *
     * @return the list of generated XML files for this table
     */
    public abstract List<GeneratedXmlFile> getGeneratedXmlFiles();


    public abstract List<GeneratedHtmlFile> getGeneratedHtmlFiles();

    public abstract List<GeneratedSqlSchemaFile> getGeneratedSqlSchemaFiles();

    public abstract List<GeneratedSqlSchemaFile> getGeneratedPermissionSqlDataFiles();

    //public abstract List<GeneratedSqlSchemaFile> getGeneratedSysMenuSqlDataFiles();

    /**
     * This method should return a list of generated Kotlin files related to this
     * table. This list could include a data classes, a mapper interface, extension methods, etc.
     *
     * @return the list of generated Kotlin files for this table
     */
    public abstract List<GeneratedKotlinFile> getGeneratedKotlinFiles();

    /**
     * This method should return the number of progress messages that will be
     * send during the generation phase.
     *
     * @return the number of progress messages
     */
    public abstract int getGenerationSteps();

    /**
     * This method exists to give plugins the opportunity to replace the calculated rules if necessary.
     *
     * @param rules the new rules
     */
    public void setRules(BaseRules rules) {
        this.rules = rules;
    }

    public TableConfiguration getTableConfiguration() {
        return tableConfiguration;
    }

    public void setPrimaryKeyType(String primaryKeyType) {
        internalAttributes.put(InternalAttribute.ATTR_PRIMARY_KEY_TYPE,
                primaryKeyType);
    }

    public void setBaseRecordType(String baseRecordType) {
        internalAttributes.put(InternalAttribute.ATTR_BASE_RECORD_TYPE,
                baseRecordType);
    }

    public void setKotlinRecordType(String kotlinRecordType) {
        internalAttributes.put(InternalAttribute.ATTR_KOTLIN_RECORD_TYPE,
                kotlinRecordType);
    }

    public void setRecordWithBLOBsType(String recordWithBLOBsType) {
        internalAttributes.put(InternalAttribute.ATTR_RECORD_WITH_BLOBS_TYPE,
                recordWithBLOBsType);
    }

    public void setExampleType(String exampleType) {
        internalAttributes
                .put(InternalAttribute.ATTR_EXAMPLE_TYPE, exampleType);
    }

    public void setMyBatis3FallbackSqlMapNamespace(String sqlMapNamespace) {
        internalAttributes.put(
                InternalAttribute.ATTR_MYBATIS3_FALLBACK_SQL_MAP_NAMESPACE,
                sqlMapNamespace);
    }

    public void setSqlMapFullyQualifiedRuntimeTableName(
            String fullyQualifiedRuntimeTableName) {
        internalAttributes.put(
                InternalAttribute.ATTR_FULLY_QUALIFIED_TABLE_NAME_AT_RUNTIME,
                fullyQualifiedRuntimeTableName);
    }

    public void setSqlMapAliasedFullyQualifiedRuntimeTableName(
            String aliasedFullyQualifiedRuntimeTableName) {
        internalAttributes
                .put(
                        InternalAttribute.ATTR_ALIASED_FULLY_QUALIFIED_TABLE_NAME_AT_RUNTIME,
                        aliasedFullyQualifiedRuntimeTableName);
    }

    public String getMyBatis3XmlMapperPackage() {
        return internalAttributes
                .get(InternalAttribute.ATTR_MYBATIS3_XML_MAPPER_PACKAGE);
    }

    public void setMyBatis3XmlMapperPackage(String mybatis3XmlMapperPackage) {
        internalAttributes.put(
                InternalAttribute.ATTR_MYBATIS3_XML_MAPPER_PACKAGE,
                mybatis3XmlMapperPackage);
    }

    public String getMyBatis3XmlMapperFileName() {
        return internalAttributes
                .get(InternalAttribute.ATTR_MYBATIS3_XML_MAPPER_FILE_NAME);
    }

    public void setMyBatis3XmlMapperFileName(String mybatis3XmlMapperFileName) {
        internalAttributes.put(
                InternalAttribute.ATTR_MYBATIS3_XML_MAPPER_FILE_NAME,
                mybatis3XmlMapperFileName);
    }

    public String getMyBatis3JavaMapperType() {
        return internalAttributes
                .get(InternalAttribute.ATTR_MYBATIS3_JAVA_MAPPER_TYPE);
    }

    public void setMyBatis3JavaMapperType(String mybatis3JavaMapperType) {
        internalAttributes.put(
                InternalAttribute.ATTR_MYBATIS3_JAVA_MAPPER_TYPE,
                mybatis3JavaMapperType);
    }

    public String getMyBatis3SqlProviderType() {
        return internalAttributes
                .get(InternalAttribute.ATTR_MYBATIS3_SQL_PROVIDER_TYPE);
    }

    public void setMyBatis3SqlProviderType(String mybatis3SqlProviderType) {
        internalAttributes.put(
                InternalAttribute.ATTR_MYBATIS3_SQL_PROVIDER_TYPE,
                mybatis3SqlProviderType);
    }

    public String getMyBatisDynamicSqlSupportType() {
        return internalAttributes.get(InternalAttribute.ATTR_MYBATIS_DYNAMIC_SQL_SUPPORT_TYPE);
    }

    public void setMyBatisDynamicSqlSupportType(String s) {
        internalAttributes.put(InternalAttribute.ATTR_MYBATIS_DYNAMIC_SQL_SUPPORT_TYPE, s);
    }

    public TargetRuntime getTargetRuntime() {
        return targetRuntime;
    }

    public boolean isImmutable() {
        Properties properties;

        if (tableConfiguration.getProperties().containsKey(PropertyRegistry.ANY_IMMUTABLE)) {
            properties = tableConfiguration.getProperties();
        } else {
            properties = context.getJavaModelGeneratorConfiguration().getProperties();
        }

        return isTrue(properties.getProperty(PropertyRegistry.ANY_IMMUTABLE));
    }

    public boolean isConstructorBased() {
        if (isImmutable()) {
            return true;
        }

        Properties properties;

        if (tableConfiguration.getProperties().containsKey(PropertyRegistry.ANY_CONSTRUCTOR_BASED)) {
            properties = tableConfiguration.getProperties();
        } else {
            properties = context.getJavaModelGeneratorConfiguration().getProperties();
        }

        return isTrue(properties.getProperty(PropertyRegistry.ANY_CONSTRUCTOR_BASED));
    }

    /**
     * Should return true if an XML generator is required for this table. This method will be called during validation
     * of the configuration, so it should not rely on database introspection. This method simply tells the validator if
     * an XML configuration is normally required for this implementation.
     *
     * @return true, if successful
     */
    public abstract boolean requiresXMLGenerator();

    public Context getContext() {
        return context;
    }

    /**
     * 获得表注释
     *
     * @param simple 是否格式化为短标签。
     *               false-获得完整注释
     *               true-格式为短标签。
     */
    public String getRemarks(boolean simple) {
        return simple ? StringUtility.remarkLeft(remarks) : remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public List<RelationGeneratorConfiguration> getRelationGeneratorConfigurations() {
        return relationGeneratorConfigurations;
    }

    /**
     * 获得配置表的属性值
     */
    public String getConfigPropertyValue(String propertyRegistry, PropertyScope scope, String defaultValue) {
        Optional<String> property;
        if (PropertyScope.any.equals(scope)) {
            property = Optional.ofNullable(tableConfiguration.getProperty(propertyRegistry));
            if (!property.isPresent()) {
                property = Optional.ofNullable(context.getProperty(propertyRegistry));
                if (!property.isPresent()) {
                    return defaultValue;
                }
            }
        } else if (PropertyScope.table.equals(scope)) {
            property = Optional.ofNullable(tableConfiguration.getProperty(propertyRegistry));
            if (!property.isPresent()) {
                return defaultValue;
            }
        } else {
            property = Optional.ofNullable(context.getProperty(propertyRegistry));
            if (!property.isPresent()) {
                return defaultValue;
            }
        }
        return property.get();
    }

    public String getConfigPropertyValue(String propertyRegistry, PropertyScope scope) {
        return getConfigPropertyValue(propertyRegistry, scope, propertyRegistryDefaultValue(propertyRegistry));
    }

    public String getConfigPropertyValue(String propertyRegistry) {
        return getConfigPropertyValue(propertyRegistry, PropertyScope.any, propertyRegistryDefaultValue(propertyRegistry));
    }

    public Map<String, String> getPermissionDataScriptLines() {
        return permissionDataScriptLines;
    }

    public void addPermissionDataScriptLines(String id, String permissionDataScriptLines) {
        this.permissionDataScriptLines.put(id, permissionDataScriptLines);
    }

    public Map<String, List<String>> getTopLevelClassExampleFields() {
        return topLevelClassExampleFields;
    }

    private String propertyRegistryDefaultValue(String propertyRegistry) {
        switch (propertyRegistry) {
            case PropertyRegistry.CONTEXT_HTML_TARGET_PROJECT:
                return "src/main/resources/templates";
            case PropertyRegistry.CONTEXT_HTML_TARGET_PACKAGE:
                String modelTarget = context.getJavaModelGeneratorConfiguration().getTargetProject();
                return StringUtility.substringAfterLast(StringUtility.substringBeforeLast(modelTarget, "."), ".");
            case PropertyRegistry.TABLE_JAVA_MODEL_BYTE_STREAM_OUTPUT_MODE:
                return "bytes";
            default:
                return null;
        }
    }
}
