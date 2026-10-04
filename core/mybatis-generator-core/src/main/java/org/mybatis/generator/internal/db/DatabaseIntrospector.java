package org.mybatis.generator.internal.db;

import com.vgosoft.core.constant.SymbolConstants;
import com.vgosoft.core.db.model.metadata.Column;
import com.vgosoft.core.db.model.metadata.ForeignKey;
import com.vgosoft.core.db.model.metadata.Index;
import com.vgosoft.core.db.model.metadata.PrimaryKey;
import com.vgosoft.core.db.query.Capability;
import com.vgosoft.core.db.query.DatabaseType;
import com.vgosoft.core.db.util.JdbcUtils;
import org.mybatis.generator.dbstrategy.DatabaseIntrospectorFactory;
import org.mybatis.generator.dbstrategy.DatabaseIntrospectorStrategy;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaReservedWords;
import org.mybatis.generator.config.*;
import org.mybatis.generator.internal.ObjectFactory;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.logging.Log;
import org.mybatis.generator.logging.LogFactory;
import org.mybatis.generator.plugins.FieldJsonFormatPlugin;

import java.sql.JDBCType;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mybatis.generator.internal.util.StringUtility.*;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * @author cen_c
 */
public class DatabaseIntrospector {

    private final DatabaseMetaData databaseMetaData;

    private final JavaTypeResolver javaTypeResolver;

    private final List<String> warnings;

    private final Context context;

    private final Log logger;

    private final Connection connection;

    private final DatabaseIntrospectorStrategy strategy;
    private final DatabaseQueryMetadataAdapter metadataAdapter;

    public DatabaseIntrospector(Context context,
                                DatabaseMetaData databaseMetaData,
                                JavaTypeResolver javaTypeResolver,
                                List<String> warnings) throws SQLException {
        super();
        this.context = context;
        this.databaseMetaData = databaseMetaData;
        this.javaTypeResolver = javaTypeResolver;
        this.warnings = warnings;
        logger = LogFactory.getLog(getClass());
        this.connection = databaseMetaData.getConnection();
        this.strategy = DatabaseIntrospectorFactory.createIntrospectorStrategy(this.connection);
        this.metadataAdapter = new DatabaseQueryMetadataAdapter(this.connection);
    }

    /**
     * Returns a List of IntrospectedTable elements that matches the specified table configuration.
     *
     * @param tc the table configuration
     * @return a list of introspected tables
     * @throws SQLException if any errors in introspection
     */
    public List<AbstractIntrospectedTable> introspectTables(TableConfiguration tc)
            throws SQLException {

        // 从数据库中获取原始列
        Map<ActualTableName, List<IntrospectedColumn>> columns = getColumns(tc);

        if (columns.isEmpty()) {
            warnings.add(getString("Warning.19", tc.getCatalog(),tc.getSchema(), tc.getTableName()));
            return Collections.emptyList();
        }

        //去除忽略的列,根据配置文件<ignoreColumn column=""/>
        removeIgnoredColumns(tc, columns);
        //重命名
        calculateExtraColumnInformation(tc, columns);
        //override
        applyColumnOverrides(tc, columns);
        applyDefaultTypeHandlers(columns);
        //主键
        calculateIdentityColumns(tc, columns);

        List<AbstractIntrospectedTable> abstractIntrospectedTables = calculateIntrospectedTables(tc, columns);

        // now introspectedTables has all the columns from all the
        // tables in the configuration. Do some validation...
        // introspectedTables配置中所有表中的所有列。做一些验证

        Iterator<AbstractIntrospectedTable> iter = abstractIntrospectedTables.iterator();
        while (iter.hasNext()) {
            AbstractIntrospectedTable abstractIntrospectedTable = iter.next();

            if (!abstractIntrospectedTable.hasAnyColumns()) {
                // add warning that the table has no columns, remove from the
                // list
                String warning = getString("Warning.1", abstractIntrospectedTable.getFullyQualifiedTable().toString());
                warnings.add(warning);
                iter.remove();
            } else if (!abstractIntrospectedTable.hasPrimaryKeyColumns() && !abstractIntrospectedTable.hasBaseColumns()) {
                // add warning that the table has only BLOB columns, remove from
                // the list
                String warning = getString("Warning.18", abstractIntrospectedTable.getFullyQualifiedTable().toString());
                warnings.add(warning);
                iter.remove();
            } else {
                // now make sure that all columns called out in the
                // configuration
                // actually exist
                reportIntrospectionWarnings(abstractIntrospectedTable, tc, abstractIntrospectedTable.getFullyQualifiedTable());
            }
        }

        return abstractIntrospectedTables;
    }

    private void calculatePrimaryKey(FullyQualifiedTable table,
                                     AbstractIntrospectedTable abstractIntrospectedTable) {
        if (metadataAdapter.supports(Capability.PRIMARY_KEY)) {
            try {
                // keep primary columns in key sequence order
                Map<Short, String> keyColumns = new TreeMap<>();
                for (PrimaryKey primaryKey : metadataAdapter.getPrimaryKeys(table.getIntrospectedTableName())) {
                    String columnName = primaryKey.getColumnName();
                    short keySeq = safeParseShort(primaryKey.getKeySeq(), (short) 0);
                    keyColumns.put(keySeq, columnName);
                }
                for (String columnName : keyColumns.values()) {
                    abstractIntrospectedTable.addPrimaryKeyColumn(columnName);
                }
                return;
            } catch (RuntimeException e) {
                warnings.add(getString("Warning.27", e.getMessage()));
            }
        }

        ResultSet rs;
        try {
            rs = databaseMetaData.getPrimaryKeys(
                    table.getIntrospectedCatalog(), table
                            .getIntrospectedSchema(), table
                            .getIntrospectedTableName());
        } catch (SQLException e) {
            warnings.add(getString("Warning.15"));
            return;
        }

        try {
            // keep primary columns in key sequence order
            Map<Short, String> keyColumns = new TreeMap<>();
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                short keySeq = rs.getShort("KEY_SEQ");
                keyColumns.put(keySeq, columnName);
            }

            for (String columnName : keyColumns.values()) {
                abstractIntrospectedTable.addPrimaryKeyColumn(columnName);
            }
        } catch (SQLException e) {
            // ignore the primary key if there's any error
        } finally {
            closeResultSet(rs);
        }
    }

    private void calculateIndex(FullyQualifiedTable table, AbstractIntrospectedTable abstractIntrospectedTable) {
        Map<String, String> indexTypes = new HashMap<>();
        Map<String, String> indexComments = new HashMap<>();
        try {
            strategy.getIndexDetails(this.connection, table, indexComments, indexTypes);
            if (indexComments.isEmpty()) {
                indexComments.putAll(strategy.getIndexComments(this.connection, table));
            }
        } catch (SQLException e) {
            warnings.add(getString("Warning.27", e.getMessage()));
        }

        if (metadataAdapter.supports(Capability.INDEX)) {
            try {
                Map<String, IndexInfo> indexInfoMap = new HashMap<>();
                for (Index index : metadataAdapter.getIndexes(table.getIntrospectedTableName())) {
                    String indexName = index.getIndexName();
                    if (!stringHasValue(indexName) || "PRIMARY".equalsIgnoreCase(indexName)) {
                        continue;
                    }

                    String columnName = index.getColumnName();
                    if (!stringHasValue(columnName)) {
                        continue;
                    }

                    boolean nonUnique = isTruthy(index.getNonUnique());
                    short ordinalPosition = safeParseShort(index.getOrdinalPosition(), (short) 0);
                    String ascOrDesc = index.getAscOrDesc();

                    String indexType = indexTypes.get(indexName);
                    if (!stringHasValue(indexType)) {
                        indexType = index.getType();
                    }

                    String indexComment = indexComments.get(indexName);
                    if (!stringHasValue(indexComment)) {
                        indexComment = stringHasValue(index.getComment())
                                ? index.getComment()
                                : "Index on " + columnName;
                    }

                    IndexInfo indexInfo = indexInfoMap.computeIfAbsent(indexName, k -> new IndexInfo(indexName, !nonUnique));
                    indexInfo.setType(indexType);
                    indexInfo.setComments(indexComment);
                    indexInfo.addColumn(columnName, ordinalPosition, ascOrDesc);
                }

                for (IndexInfo indexInfo : indexInfoMap.values()) {
                    abstractIntrospectedTable.addIndex(indexInfo);
                }
                return;
            } catch (RuntimeException e) {
                warnings.add(getString("Warning.27", e.getMessage()));
            }
        }

        ResultSet rs = null;
        try {
            rs = databaseMetaData.getIndexInfo(
                    table.getIntrospectedCatalog(),
                    table.getIntrospectedSchema(),
                    table.getIntrospectedTableName(),
                    false,
                    true);
            // 使用Map存储索引信息，以索引名为键
            Map<String, IndexInfo> indexInfoMap = new HashMap<>();
            while (rs.next()) {
                String indexName = rs.getString("INDEX_NAME");
                // 过滤掉主键索引和空索引名
                if (indexName == null || "PRIMARY".equals(indexName)) {
                    continue;
                }
                // 获取索引信息
                boolean nonUnique = rs.getBoolean("NON_UNIQUE");
                short ordinalPosition = rs.getShort("ORDINAL_POSITION");
                String columnName = rs.getString("COLUMN_NAME");
                // 可能为null
                String ascOrDesc = rs.getString("ASC_OR_DESC");

                // 获取索引类型
                short type = rs.getShort("TYPE");
                String indexType = strategy.getIndexTypeString(type);
                if (indexTypes.containsKey(indexName)) {
                    indexType = indexTypes.get(indexName);
                }
                // 索引注释
                String indexComment = "Index on " + columnName;
                if (indexComments.containsKey(indexName)) {
                    indexComment = indexComments.get(indexName);
                }
                // 获取或创建索引信息对象
                IndexInfo indexInfo = indexInfoMap.computeIfAbsent(indexName, k ->
                        new IndexInfo(indexName, !nonUnique));
                indexInfo.setType(indexType);
                indexInfo.setComments(indexComment);
                // 添加列信息
                indexInfo.addColumn(columnName, ordinalPosition, ascOrDesc);
            }

            // 将收集到的索引信息添加到IntrospectedTable
            for (IndexInfo indexInfo : indexInfoMap.values()) {
                abstractIntrospectedTable.addIndex(indexInfo);
            }

        } catch (SQLException e) {
            warnings.add(getString("Warning.27", e.getMessage()));
        } finally {
            closeResultSet(rs);
        }
    }

    private void calculateForeignKeys(FullyQualifiedTable table, AbstractIntrospectedTable abstractIntrospectedTable) {
        if (metadataAdapter.supports(Capability.FOREIGN_KEY)) {
            try {
                Map<String, List<ForeignKeyInfo>> foreignKeys = new HashMap<>();
                for (ForeignKey foreignKey : metadataAdapter.getForeignKeys(table.getIntrospectedTableName())) {
                    String fkName = foreignKey.getFkName();
                    String pkTableName = foreignKey.getPkTableName();
                    String pkColumnName = foreignKey.getPkColumnName();
                    String fkColumnName = foreignKey.getFkColumnName();
                    short keySeq = safeParseShort(foreignKey.getKeySeq(), (short) 0);
                    ForeignKeyInfo fkInfo = new ForeignKeyInfo(
                            fkName,
                            pkTableName,
                            pkColumnName,
                            fkColumnName,
                            (short) DatabaseMetaData.importedKeyNoAction,
                            (short) DatabaseMetaData.importedKeyNoAction,
                            keySeq);
                    foreignKeys.computeIfAbsent(fkName, k -> new ArrayList<>()).add(fkInfo);
                    abstractIntrospectedTable.getColumn(fkColumnName).ifPresent(column -> {
                        column.setForeignKey(true);
                        column.addProperty("FK_NAME", fkName);
                        column.addProperty("PK_TABLE_NAME", pkTableName);
                        column.addProperty("PK_COLUMN_NAME", pkColumnName);
                    });
                }

                foreignKeys.forEach((fkName, fkInfos) -> {
                    fkInfos.sort(Comparator.comparing(ForeignKeyInfo::getKeySeq));
                    abstractIntrospectedTable.addForeignKey(fkName, fkInfos);
                });
                return;
            } catch (RuntimeException e) {
                warnings.add(getString("Warning.16", e.getMessage()));
            }
        }

        ResultSet rs = null;
        try {
            rs = databaseMetaData.getImportedKeys(
                    table.getIntrospectedCatalog(),
                    table.getIntrospectedSchema(),
                    table.getIntrospectedTableName());
            // Use a Map to group FK columns by FK name
            Map<String, List<ForeignKeyInfo>> foreignKeys = new HashMap<>();

            while (rs.next()) {
                String fkName = rs.getString("FK_NAME");
                String pkTableName = rs.getString("PKTABLE_NAME");
                String pkColumnName = rs.getString("PKCOLUMN_NAME");
                String fkColumnName = rs.getString("FKCOLUMN_NAME");
                short keySeq = rs.getShort("KEY_SEQ");
                short updateRule = rs.getShort("UPDATE_RULE");
                short deleteRule = rs.getShort("DELETE_RULE");
                ForeignKeyInfo fkInfo = new ForeignKeyInfo(
                        fkName, pkTableName, pkColumnName, fkColumnName, updateRule, deleteRule, keySeq);
                foreignKeys.computeIfAbsent(fkName, k -> new ArrayList<>()).add(fkInfo);
                // Mark the column as foreign key column
                abstractIntrospectedTable.getColumn(fkColumnName).ifPresent(column -> {
                    column.setForeignKey(true);
                    column.addProperty("FK_NAME", fkName);
                    column.addProperty("PK_TABLE_NAME", pkTableName);
                    column.addProperty("PK_COLUMN_NAME", pkColumnName);
                });
            }
            // Add the foreign key information to the introspected table
            foreignKeys.forEach((fkName, fkInfos) -> {
                // Sort by key sequence
                fkInfos.sort(Comparator.comparing(ForeignKeyInfo::getKeySeq));
                abstractIntrospectedTable.addForeignKey(fkName, fkInfos);
            });

        } catch (SQLException e) {
            warnings.add(getString("Warning.16", e.getMessage()));
        } finally {
            closeResultSet(rs);
        }
    }

    private void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    private void reportIntrospectionWarnings(
            AbstractIntrospectedTable abstractIntrospectedTable,
            TableConfiguration tableConfiguration, FullyQualifiedTable table) {
        // make sure that every column listed in column overrides
        // actually exists in the table
        for (ColumnOverride columnOverride : tableConfiguration
                .getColumnOverrides()) {
            if (abstractIntrospectedTable.getColumn(columnOverride.getColumnName()).isEmpty()) {
                warnings.add(getString("Warning.3",
                        columnOverride.getColumnName(), table.toString()));
            }
        }

        // make sure that every column listed in ignored columns
        // actually exists in the table
        for (String string : tableConfiguration.getIgnoredColumnsInError()) {
            warnings.add(getString("Warning.4",
                    string, table.toString()));
        }

        tableConfiguration.getGeneratedKey().ifPresent(generatedKey -> {
            if (abstractIntrospectedTable.getColumn(generatedKey.getColumn()).isEmpty()) {
                if (generatedKey.isIdentity()) {
                    warnings.add(getString("Warning.5",
                            generatedKey.getColumn(), table.toString()));
                } else {
                    warnings.add(getString("Warning.6",
                            generatedKey.getColumn(), table.toString()));
                }
            }
        });

        for (IntrospectedColumn ic : abstractIntrospectedTable.getAllColumns()) {
            if (JavaReservedWords.containsWord(ic.getJavaProperty())) {
                warnings.add(getString("Warning.26",
                        ic.getActualColumnName(), table.toString()));
            }
        }
    }



    private void removeIgnoredColumns(TableConfiguration tc,
                                      Map<ActualTableName, List<IntrospectedColumn>> columns) {
        for (Map.Entry<ActualTableName, List<IntrospectedColumn>> entry : columns
                .entrySet()) {
            Iterator<IntrospectedColumn> tableColumns = entry.getValue()
                    .iterator();
            while (tableColumns.hasNext()) {
                IntrospectedColumn introspectedColumn = tableColumns.next();
                if (tc
                        .isColumnIgnored(introspectedColumn
                                .getActualColumnName())) {
                    tableColumns.remove();
                    if (logger.isDebugEnabled()) {
                        logger.debug(getString("Tracing.3",
                                introspectedColumn.getActualColumnName(), entry
                                        .getKey().toString()));
                    }
                }
            }
        }
    }

    private void calculateExtraColumnInformation(TableConfiguration tc,
                                                 Map<ActualTableName, List<IntrospectedColumn>> columns) {
        StringBuilder sb = new StringBuilder();
        Pattern pattern = null;
        String replaceString = null;
        if (tc.getColumnRenamingRule() != null) {
            pattern = Pattern.compile(tc.getColumnRenamingRule()
                    .getSearchString());
            replaceString = tc.getColumnRenamingRule().getReplaceString();
            replaceString = replaceString == null ? "" : replaceString;
        }

        for (Map.Entry<ActualTableName, List<IntrospectedColumn>> entry : columns
                .entrySet()) {
            for (IntrospectedColumn introspectedColumn : entry.getValue()) {
                String calculatedColumnName;
                if (pattern == null) {
                    calculatedColumnName = introspectedColumn
                            .getActualColumnName();
                } else {
                    Matcher matcher = pattern.matcher(introspectedColumn
                            .getActualColumnName());
                    calculatedColumnName = matcher.replaceAll(replaceString);
                }

                if (isTrue(tc
                        .getProperty(PropertyRegistry.TABLE_USE_ACTUAL_COLUMN_NAMES))) {
                    introspectedColumn.setJavaProperty(
                            JavaBeansUtil.getValidPropertyName(calculatedColumnName));
                } else if (isTrue(tc
                        .getProperty(PropertyRegistry.TABLE_USE_COMPOUND_PROPERTY_NAMES))) {
                    sb.setLength(0);
                    sb.append(calculatedColumnName);
                    sb.append('_');
                    sb.append(JavaBeansUtil.getCamelCaseString(
                            introspectedColumn.getRemarks(false), true));
                    introspectedColumn.setJavaProperty(
                            JavaBeansUtil.getValidPropertyName(sb.toString()));
                } else {
                    introspectedColumn.setJavaProperty(
                            JavaBeansUtil.getCamelCaseString(calculatedColumnName, false));
                }

                FullyQualifiedJavaType fullyQualifiedJavaType = javaTypeResolver
                        .calculateJavaType(introspectedColumn);

                if (fullyQualifiedJavaType != null) {
                    introspectedColumn
                            .setFullyQualifiedJavaType(fullyQualifiedJavaType);
                    introspectedColumn.setJdbcTypeName(javaTypeResolver.calculateJdbcTypeName(introspectedColumn));
                } else {
                    // type cannot be resolved. Check for ignored or overridden
                    boolean warn = !tc.isColumnIgnored(introspectedColumn.getActualColumnName());

                    ColumnOverride co = tc.getColumnOverride(introspectedColumn
                            .getActualColumnName());
                    if (co != null
                            && stringHasValue(co.getJavaType())) {
                        warn = false;
                    }

                    // if the type is not supported, then we'll report a warning
                    if (warn) {
                        introspectedColumn
                                .setFullyQualifiedJavaType(FullyQualifiedJavaType
                                        .getObjectInstance());
                        introspectedColumn.setJdbcTypeName("OTHER");

                        String warning = getString("Warning.14",
                                Integer.toString(introspectedColumn.getJdbcType()),
                                entry.getKey().toString(),
                                introspectedColumn.getActualColumnName());

                        warnings.add(warning);
                    }
                }

                if (context.autoDelimitKeywords()
                        && SqlReservedWords.containsWord(introspectedColumn
                        .getActualColumnName())) {
                    introspectedColumn.setColumnNameDelimited(true);
                }

                if (tc.isAllColumnDelimitingEnabled()) {
                    introspectedColumn.setColumnNameDelimited(true);
                }
            }
        }
    }

    private void calculateIdentityColumns(TableConfiguration tc,
                                          Map<ActualTableName, List<IntrospectedColumn>> columns) {
        tc.getGeneratedKey().ifPresent(gk -> {
            for (Map.Entry<ActualTableName, List<IntrospectedColumn>> entry : columns.entrySet()) {
                for (IntrospectedColumn introspectedColumn : entry.getValue()) {
                    if (isMatchedColumn(introspectedColumn, gk)) {
                        if (gk.isIdentity() || gk.isJdbcStandard()) {
                            introspectedColumn.setIdentity(true);
                            introspectedColumn.setSequenceColumn(false);
                        } else {
                            introspectedColumn.setIdentity(false);
                            introspectedColumn.setSequenceColumn(true);
                        }
                    }
                }
            }
        });
    }

    private boolean isMatchedColumn(IntrospectedColumn introspectedColumn, GeneratedKey gk) {
        if (introspectedColumn.isColumnNameDelimited()) {
            return introspectedColumn.getActualColumnName().equals(gk.getColumn());
        } else {
            return introspectedColumn.getActualColumnName().equalsIgnoreCase(gk.getColumn());
        }
    }

    private void applyColumnOverrides(TableConfiguration tc,
                                      Map<ActualTableName, List<IntrospectedColumn>> columns) {
        for (Map.Entry<ActualTableName, List<IntrospectedColumn>> entry : columns
                .entrySet()) {
            for (IntrospectedColumn introspectedColumn : entry.getValue()) {
                ColumnOverride columnOverride = tc
                        .getColumnOverride(introspectedColumn
                                .getActualColumnName());

                if (columnOverride != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(getString("Tracing.4",
                                introspectedColumn.getActualColumnName(), entry
                                        .getKey().toString()));
                    }

                    if (stringHasValue(columnOverride.getJavaProperty())) {
                        introspectedColumn.setJavaProperty(columnOverride.getJavaProperty());
                    }

                    if (stringHasValue(columnOverride.getJavaType())) {
                        introspectedColumn.setFullyQualifiedJavaType(new FullyQualifiedJavaType(columnOverride.getJavaType()));
                    }

                    if (stringHasValue(columnOverride.getJdbcType())) {
                        introspectedColumn.setJdbcTypeName(columnOverride.getJdbcType());
                    }

                    if (stringHasValue(columnOverride.getTypeHandler())) {
                        introspectedColumn.setTypeHandler(columnOverride.getTypeHandler());
                    }

                    if (columnOverride.isColumnNameDelimited()) {
                        introspectedColumn.setColumnNameDelimited(true);
                    }

                    introspectedColumn.setGeneratedAlways(columnOverride.isGeneratedAlways());

                    introspectedColumn.setProperties(columnOverride.getProperties());

                }
                String logicalType = introspectedColumn.getProperties()
                        .getProperty(FieldJsonFormatPlugin.LOGICAL_TYPE_PROPERTY);
                if ("JSON".equalsIgnoreCase(logicalType)
                        || "JSON".equalsIgnoreCase(introspectedColumn.getActualTypeName())) {
                    FieldJsonFormatPlugin.ensureJsonTypeHandler(introspectedColumn);
                }
            }
        }
    }

    private void applyDefaultTypeHandlers(Map<ActualTableName, List<IntrospectedColumn>> columns) {
        for (List<IntrospectedColumn> introspectedColumns : columns.values()) {
            for (IntrospectedColumn introspectedColumn : introspectedColumns) {
                applyDefaultTypeHandler(introspectedColumn);
            }
        }
    }

    static void applyDefaultTypeHandler(IntrospectedColumn introspectedColumn) {
        if (introspectedColumn == null
                || stringHasValue(introspectedColumn.getTypeHandler())
                || introspectedColumn.getFullyQualifiedJavaType() == null
                || !introspectedColumn.getFullyQualifiedJavaType().isArray()
                || !"byte[]".equals(introspectedColumn.getFullyQualifiedJavaType().getFullyQualifiedName())) {
            return;
        }

        introspectedColumn.setTypeHandler("org.apache.ibatis.type.ByteArrayTypeHandler");
    }

    private Map<ActualTableName, List<IntrospectedColumn>> getColumns(TableConfiguration tc) throws SQLException {
        String localCatalog;
        String localSchema;
        String localTableName;

        boolean delimitIdentifiers = tc.isDelimitIdentifiers()
                || stringContainsSpace(tc.getCatalog())
                || stringContainsSpace(tc.getSchema())
                || stringContainsSpace(tc.getTableName());

        if (delimitIdentifiers) {
            localCatalog = tc.getCatalog();
            localSchema = tc.getSchema();
            localTableName = tc.getTableName();
        } else if (databaseMetaData.storesLowerCaseIdentifiers()) {
            localCatalog = tc.getCatalog() == null ? null : tc.getCatalog().toLowerCase();
            localSchema = tc.getSchema() == null ? null : tc.getSchema().toLowerCase();
            localTableName = tc.getTableName().toLowerCase();
        } else if (databaseMetaData.storesUpperCaseIdentifiers()) {
            localCatalog = tc.getCatalog() == null ? null : tc.getCatalog().toUpperCase();
            localSchema = tc.getSchema() == null ? null : tc.getSchema().toUpperCase();
            localTableName = tc.getTableName().toUpperCase();
        } else {
            localCatalog = tc.getCatalog();
            localSchema = tc.getSchema();
            localTableName = tc.getTableName();
        }

        if (tc.isWildcardEscapingEnabled()) {
            String escapeString = databaseMetaData.getSearchStringEscape();

            if (localSchema != null) {
                localSchema = escapeName(localSchema, escapeString);
            }

            localTableName = escapeName(localTableName, escapeString);
        }

        Map<ActualTableName, List<IntrospectedColumn>> answer = new HashMap<>();

        if (logger.isDebugEnabled()) {
            String fullTableName = composeFullyQualifiedTableName(localCatalog, localSchema, localTableName, '.');
            logger.debug(getString("Tracing.1", fullTableName));
        }

        Set<String> fkColumnNames = new HashSet<>();
        if (metadataAdapter.supports(Capability.FOREIGN_KEY)) {
            try {
                for (ForeignKey foreignKey : metadataAdapter.getForeignKeys(localTableName)) {
                    if (stringHasValue(foreignKey.getFkColumnName())) {
                        fkColumnNames.add(foreignKey.getFkColumnName());
                    }
                }
            } catch (RuntimeException e) {
                warnings.add(getString("Warning.16", e.getMessage()));
            }
        }
        if (fkColumnNames.isEmpty()) {
            ResultSet importedKeys = databaseMetaData.getImportedKeys(localCatalog, localSchema, localTableName);
            while (importedKeys.next()) {
                String fkColumnName = importedKeys.getString("FKCOLUMN_NAME");
                if (stringHasValue(fkColumnName)) {
                    fkColumnNames.add(fkColumnName);
                }
            }
            closeResultSet(importedKeys);
        }

        Map<String, String> remarks = strategy.getColumnRemarks(connection, localTableName);
        int order = 10;
        if (metadataAdapter.supports(Capability.COLUMN)) {
            try {
                for (Column column : metadataAdapter.getTableColumns(localTableName)) {
                    IntrospectedColumn introspectedColumn = ObjectFactory.createIntrospectedColumn(context);
                    introspectedColumn.setTableAlias(tc.getAlias());
                    introspectedColumn.setJdbcType(resolveJdbcType(column));
                    introspectedColumn.setActualTypeName(column.getTypeName());
                    introspectedColumn.setLength(safeParseInt(column.getColumnSize(), 0));
                    introspectedColumn.setActualColumnName(column.getColumnName());
                    introspectedColumn.setNullable(isNullable(column.getNullable()));
                    introspectedColumn.setScale(safeParseInt(column.getDecimalDigits(), 0));
                    if (fkColumnNames.contains(introspectedColumn.getActualColumnName())) {
                        introspectedColumn.setForeignKey(true);
                    }
                    String remark = remarks.get(introspectedColumn.getActualColumnName());
                    introspectedColumn.setRemarks(remark != null ? remark : column.getRemarks());
                    introspectedColumn.setDefaultValue(column.getColumnDef());
                    introspectedColumn.setAutoIncrement(isTruthy(invokeGetter(column, "getAutoIncrement")));
                    introspectedColumn.setGeneratedColumn(isTruthy(invokeGetter(column, "getGeneratedColumn")));

                    ActualTableName atn = new ActualTableName(
                            stringHasValue(invokeGetter(column, "getTableCat")) ? invokeGetter(column, "getTableCat") : localCatalog,
                            stringHasValue(invokeGetter(column, "getTableSchem")) ? invokeGetter(column, "getTableSchem") : localSchema,
                            stringHasValue(column.getTableName()) ? column.getTableName() : localTableName);

                    List<IntrospectedColumn> columns = answer.computeIfAbsent(atn, k -> new ArrayList<>());
                    introspectedColumn.setOrder(order);
                    columns.add(introspectedColumn);
                    order++;

                    if (logger.isDebugEnabled()) {
                        logger.debug(getString(
                                "Tracing.2",
                                introspectedColumn.getActualColumnName(),
                                Integer.toString(introspectedColumn.getJdbcType()),
                                atn.toString()));
                    }
                }
                return handleMultipleTableWarning(tc, answer, localSchema, localTableName);
            } catch (RuntimeException e) {
                warnings.add(getString("Warning.27", e.getMessage()));
            }
        }

        ResultSet rs = databaseMetaData.getColumns(localCatalog, localSchema, localTableName, "%");

        boolean supportsIsAutoIncrement = false;
        boolean supportsIsGeneratedColumn = false;
        ResultSetMetaData rsmd = rs.getMetaData();
        int colCount = rsmd.getColumnCount();
        for (int i = 1; i <= colCount; i++) {
            if ("IS_AUTOINCREMENT".equals(rsmd.getColumnName(i))) {
                supportsIsAutoIncrement = true;
            }
            if ("IS_GENERATEDCOLUMN".equals(rsmd.getColumnName(i))) {
                supportsIsGeneratedColumn = true;
            }
        }

        while (rs.next()) {
            IntrospectedColumn introspectedColumn = ObjectFactory.createIntrospectedColumn(context);

            introspectedColumn.setTableAlias(tc.getAlias());
            introspectedColumn.setJdbcType(rs.getInt("DATA_TYPE"));
            introspectedColumn.setActualTypeName(rs.getString("TYPE_NAME"));
            introspectedColumn.setLength(rs.getInt("COLUMN_SIZE"));
            introspectedColumn.setActualColumnName(rs.getString("COLUMN_NAME"));
            introspectedColumn.setNullable(rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
            introspectedColumn.setScale(rs.getInt("DECIMAL_DIGITS"));
            if (fkColumnNames.contains(introspectedColumn.getActualColumnName())) {
                introspectedColumn.setForeignKey(true);
            }
            String remark = remarks.get(introspectedColumn.getActualColumnName());
            if (remark != null) {
                introspectedColumn.setRemarks(remark);
            } else {
                introspectedColumn.setRemarks(rs.getString("REMARKS"));
            }
            introspectedColumn.setDefaultValue(rs.getString("COLUMN_DEF"));

            if (supportsIsAutoIncrement) {
                introspectedColumn.setAutoIncrement("YES".equals(rs.getString("IS_AUTOINCREMENT")));
            }

            if (supportsIsGeneratedColumn) {
                introspectedColumn.setGeneratedColumn("YES".equals(rs.getString("IS_GENERATEDCOLUMN")));
            }

            ActualTableName atn = new ActualTableName(
                    rs.getString("TABLE_CAT"),
                    rs.getString("TABLE_SCHEM"),
                    rs.getString("TABLE_NAME"));

            List<IntrospectedColumn> columns = answer.computeIfAbsent(atn, k -> new ArrayList<>());

            introspectedColumn.setOrder(order);

            columns.add(introspectedColumn);

            order++;

            if (logger.isDebugEnabled()) {
                logger.debug(getString(
                        "Tracing.2",
                        introspectedColumn.getActualColumnName(), Integer
                                .toString(introspectedColumn.getJdbcType()),
                        atn.toString()));
            }
        }

        closeResultSet(rs);

        return handleMultipleTableWarning(tc, answer, localSchema, localTableName);
    }

    private Map<ActualTableName, List<IntrospectedColumn>> handleMultipleTableWarning(
            TableConfiguration tc,
            Map<ActualTableName, List<IntrospectedColumn>> answer,
            String localSchema,
            String localTableName) {
        if (answer.size() > 1
                && !stringContainsSqlWildcard(localSchema)
                && !stringContainsSqlWildcard(localTableName)) {
            // issue a warning if there is more than one table and
            // no wildcards were used
            ActualTableName inputAtn = new ActualTableName(tc.getCatalog(), tc
                    .getSchema(), tc.getTableName());

            StringBuilder sb = new StringBuilder();
            boolean comma = false;
            for (ActualTableName atn : answer.keySet()) {
                if (comma) {
                    sb.append(',');
                } else {
                    comma = true;
                }
                sb.append(atn.toString());
            }

            warnings.add(getString("Warning.25",
                    inputAtn.toString(), sb.toString()));
        }
        return answer;
    }

    private int safeParseInt(String value, int defaultValue) {
        if (!stringHasValue(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private short safeParseShort(String value, short defaultValue) {
        if (!stringHasValue(value)) {
            return defaultValue;
        }
        try {
            return Short.parseShort(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private int resolveJdbcType(Column column) {
        int value = safeParseInt(invokeGetter(column, "getDataType"), Integer.MIN_VALUE);
        if (value != Integer.MIN_VALUE) {
            return value;
        }
        value = safeParseInt(invokeGetter(column, "getJdbcType"), Integer.MIN_VALUE);
        if (value != Integer.MIN_VALUE) {
            return value;
        }
        value = safeParseInt(column.getColumnType(), Integer.MIN_VALUE);
        if (value != Integer.MIN_VALUE) {
            return value;
        }

        value = resolveJdbcTypeFromName(column.getTypeName());
        if (value != Types.OTHER) {
            return value;
        }

        return resolveJdbcTypeFromName(column.getColumnType());
    }

    private int resolveJdbcTypeFromName(String typeName) {
        if (!stringHasValue(typeName)) {
            return Types.OTHER;
        }

        String normalized = typeName.trim().toUpperCase(Locale.ROOT);
        int bracketIndex = normalized.indexOf('(');
        if (bracketIndex > 0) {
            normalized = normalized.substring(0, bracketIndex).trim();
        }
        normalized = normalized.replace(" UNSIGNED", "").trim();

        int spaceIndex = normalized.indexOf(' ');
        if (spaceIndex > 0) {
            normalized = normalized.substring(0, spaceIndex);
        }

        try {
            return JDBCType.valueOf(normalized).getVendorTypeNumber();
        } catch (IllegalArgumentException e) {
            // fall through
        }

        return switch (normalized) {
            case "INT" -> Types.INTEGER;
            case "DATETIME" -> Types.TIMESTAMP;
            case "BOOL" -> Types.BOOLEAN;
            case "NUMBER" -> Types.NUMERIC;
            case "TEXT", "LONGTEXT", "MEDIUMTEXT", "TINYTEXT", "VARCHAR2", "NVARCHAR2", "UUID" -> Types.VARCHAR;
            case "BYTEA" -> Types.BINARY;
            default -> Types.OTHER;
        };
    }

    private boolean isTruthy(String value) {
        if (!stringHasValue(value)) {
            return false;
        }
        String normalized = value.trim();
        if ("1".equals(normalized)) {
            return true;
        }
        if ("0".equals(normalized)) {
            return false;
        }
        return "Y".equalsIgnoreCase(normalized)
                || "YES".equalsIgnoreCase(normalized)
                || "TRUE".equalsIgnoreCase(normalized);
    }

    private boolean isNullable(String value) {
        if (!stringHasValue(value)) {
            return false;
        }
        String normalized = value.trim();
        if ("1".equals(normalized)) {
            return true;
        }
        if ("0".equals(normalized)) {
            return false;
        }
        return "Y".equalsIgnoreCase(normalized)
                || "YES".equalsIgnoreCase(normalized)
                || "TRUE".equalsIgnoreCase(normalized)
                || "NULLABLE".equalsIgnoreCase(normalized);
    }

    private String invokeGetter(Object source, String methodName) {
        try {
            Object value = source.getClass().getMethod(methodName).invoke(source);
            return value == null ? null : String.valueOf(value);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private String escapeName(String localName, String escapeString) {
        StringTokenizer st = new StringTokenizer(localName, "_%", true);
        StringBuilder sb = new StringBuilder();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (SymbolConstants.UNDERLINE.equals(token)
                    || SymbolConstants.PERCENT.equals(token)) {
                sb.append(escapeString);
            }
            sb.append(token);
        }
        return sb.toString();
    }

    private List<AbstractIntrospectedTable> calculateIntrospectedTables(TableConfiguration tc, Map<ActualTableName, List<IntrospectedColumn>> columns) throws SQLException {
        boolean delimitIdentifiers = tc.isDelimitIdentifiers()
                || stringContainsSpace(tc.getCatalog())
                || stringContainsSpace(tc.getSchema())
                || stringContainsSpace(tc.getTableName());

        List<AbstractIntrospectedTable> answer = new ArrayList<>();

        for (Map.Entry<ActualTableName, List<IntrospectedColumn>> entry : columns.entrySet()) {
            ActualTableName atn = entry.getKey();

            // we only use the returned catalog and schema if something was
            // actually
            // specified on the table configuration. If something was returned
            // from the DB for these fields, but nothing was specified on the
            // table
            // configuration, then some sort of DB default is being returned
            // we don't want that in our SQL
            FullyQualifiedTable table = new FullyQualifiedTable(
                    stringHasValue(tc.getCatalog()) ? atn.getCatalog() : null,
                    stringHasValue(tc.getSchema()) ? atn.getSchema() : null,
                    atn.getTableName(),
                    tc.getDomainObjectName(),
                    tc.getAlias(),
                    isTrue(tc.getProperty(PropertyRegistry.TABLE_IGNORE_QUALIFIERS_AT_RUNTIME)),
                    tc.getProperty(PropertyRegistry.TABLE_RUNTIME_CATALOG),
                    tc.getProperty(PropertyRegistry.TABLE_RUNTIME_SCHEMA),
                    tc.getProperty(PropertyRegistry.TABLE_RUNTIME_TABLE_NAME),
                    delimitIdentifiers,
                    tc.getDomainObjectRenamingRule(),
                    context);

            AbstractIntrospectedTable introspectedTable = ObjectFactory.createIntrospectedTable(tc, table, context);

            String productName = this.databaseMetaData.getDatabaseProductName();
            String jdbcUrl = this.databaseMetaData.getURL();
            DatabaseType dbType = JdbcUtils.getDbType(productName, jdbcUrl);
            if (dbType != null) {
                introspectedTable.setDbType(dbType);
                tc.getAdditionalDbTypes().add(dbType);
            } else {
                introspectedTable.setDbType(DatabaseType.OTHER);
                tc.getAdditionalDbTypes().add(DatabaseType.OTHER);
            }

            for (IntrospectedColumn introspectedColumn : entry.getValue()) {
                introspectedTable.addColumn(introspectedColumn);
            }

            calculatePrimaryKey(table, introspectedTable);

            calculateIndex(table, introspectedTable);

            calculateForeignKeys(table, introspectedTable);

            enhanceIntrospectedTable(introspectedTable);


            //针对Sql Server更新remark
            try {
                strategy.updateTableRemark(introspectedTable,connection);
            } catch (SQLException e) {
                warnings.add("waring 0030 " + e.getMessage());
            }

            answer.add(introspectedTable);
        }

        return answer;
    }

    /**
     * Calls database metadata to retrieve extra information about the table
     * such as remarks associated with the table and the type.
     *
     * <p>If there is any error, we just add a warning and continue.
     *
     * @param abstractIntrospectedTable the introspected table to enhance
     */
    private void enhanceIntrospectedTable(AbstractIntrospectedTable abstractIntrospectedTable) {
        try {
            FullyQualifiedTable fqt = abstractIntrospectedTable.getFullyQualifiedTable();
            if (metadataAdapter.supports(Capability.TABLE)
                    && metadataAdapter.supports(Capability.REMARK)) {
                metadataAdapter.findTable(fqt.getIntrospectedTableName())
                        .ifPresent(table -> {
                            if (stringHasValue(table.getRemarks())) {
                                abstractIntrospectedTable.setRemarks(table.getRemarks());
                            }
                        });
            }

            ResultSet rs = databaseMetaData.getTables(fqt.getIntrospectedCatalog(), fqt.getIntrospectedSchema(),
                    fqt.getIntrospectedTableName(), null);
            if (rs.next()) {
                String remarks = rs.getString("REMARKS");
                String tableType = rs.getString("TABLE_TYPE");
                if (!stringHasValue(abstractIntrospectedTable.getRemarks(true))) {
                    abstractIntrospectedTable.setRemarks(remarks);
                }
                abstractIntrospectedTable.setTableType(tableType);
            }
            closeResultSet(rs);
        } catch (SQLException e) {
            warnings.add(getString("Warning.27", e.getMessage()));
        }
    }
}
