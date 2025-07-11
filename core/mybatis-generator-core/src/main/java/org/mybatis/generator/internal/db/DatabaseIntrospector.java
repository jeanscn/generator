package org.mybatis.generator.internal.db;

import org.mybatis.generator.DBStrategy.DatabaseIntrospectorFactory;
import org.mybatis.generator.DBStrategy.DatabaseIntrospectorStrategy;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaReservedWords;
import org.mybatis.generator.config.*;
import org.mybatis.generator.internal.ObjectFactory;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.logging.Log;
import org.mybatis.generator.logging.LogFactory;

import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mybatis.generator.internal.util.StringUtility.*;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

public class DatabaseIntrospector {

    private final DatabaseMetaData databaseMetaData;

    private final JavaTypeResolver javaTypeResolver;

    private final List<String> warnings;

    private final Context context;

    private final Log logger;

    private final Connection connection;

    private final DatabaseIntrospectorStrategy strategy;

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
    }

    private void calculatePrimaryKey(FullyQualifiedTable table,
                                     IntrospectedTable introspectedTable) {
        ResultSet rs;
        try {
            rs = databaseMetaData.getPrimaryKeys(
                    table.getIntrospectedCatalog(), table
                            .getIntrospectedSchema(), table
                            .getIntrospectedTableName());
        } catch (SQLException e) {
            warnings.add(getString("Warning.15")); //$NON-NLS-1$
            return;
        }

        try {
            // keep primary columns in key sequence order
            Map<Short, String> keyColumns = new TreeMap<>();
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME"); //$NON-NLS-1$
                short keySeq = rs.getShort("KEY_SEQ"); //$NON-NLS-1$
                keyColumns.put(keySeq, columnName);
            }

            for (String columnName : keyColumns.values()) {
                introspectedTable.addPrimaryKeyColumn(columnName);
            }
        } catch (SQLException e) {
            // ignore the primary key if there's any error
        } finally {
            closeResultSet(rs);
        }
    }

    private void calculateIndex(FullyQualifiedTable table, IntrospectedTable introspectedTable) {
        ResultSet rs = null;
        try {
            rs = databaseMetaData.getIndexInfo(
                    table.getIntrospectedCatalog(),
                    table.getIntrospectedSchema(),
                    table.getIntrospectedTableName(),
                    false, // unique=false获取所有索引(包括非唯一索引)
                    true); // approximate
            // 使用Map存储索引信息，以索引名为键
            Map<String, IndexInfo> indexInfoMap = new HashMap<>();
            Map<String, String> indexTypes = new HashMap<>();
            Map<String, String> indexComments = strategy.getIndexComments(this.connection,table);
            strategy.getIndexDetails(this.connection, table, indexComments, indexTypes);
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
                String ascOrDesc = rs.getString("ASC_OR_DESC"); // 可能为null

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
                introspectedTable.addIndex(indexInfo);
            }

        } catch (SQLException e) {
            warnings.add(getString("Warning.27", e.getMessage()));
        } finally {
            closeResultSet(rs);
        }
    }

    private void calculateForeignKeys(FullyQualifiedTable table, IntrospectedTable introspectedTable) {
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
                introspectedTable.getColumn(fkColumnName).ifPresent(column -> {
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
                introspectedTable.addForeignKey(fkName, fkInfos);
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
            IntrospectedTable introspectedTable,
            TableConfiguration tableConfiguration, FullyQualifiedTable table) {
        // make sure that every column listed in column overrides
        // actually exists in the table
        for (ColumnOverride columnOverride : tableConfiguration
                .getColumnOverrides()) {
            if (!introspectedTable.getColumn(columnOverride.getColumnName()).isPresent()) {
                warnings.add(getString("Warning.3", //$NON-NLS-1$
                        columnOverride.getColumnName(), table.toString()));
            }
        }

        // make sure that every column listed in ignored columns
        // actually exists in the table
        for (String string : tableConfiguration.getIgnoredColumnsInError()) {
            warnings.add(getString("Warning.4", //$NON-NLS-1$
                    string, table.toString()));
        }

        tableConfiguration.getGeneratedKey().ifPresent(generatedKey -> {
            if (!introspectedTable.getColumn(generatedKey.getColumn()).isPresent()) {
                if (generatedKey.isIdentity()) {
                    warnings.add(getString("Warning.5", //$NON-NLS-1$
                            generatedKey.getColumn(), table.toString()));
                } else {
                    warnings.add(getString("Warning.6", //$NON-NLS-1$
                            generatedKey.getColumn(), table.toString()));
                }
            }
        });

        for (IntrospectedColumn ic : introspectedTable.getAllColumns()) {
            if (JavaReservedWords.containsWord(ic.getJavaProperty())) {
                warnings.add(getString("Warning.26", //$NON-NLS-1$
                        ic.getActualColumnName(), table.toString()));
            }
        }
    }

    /**
     * Returns a List of IntrospectedTable elements that matches the specified table configuration.
     *
     * @param tc the table configuration
     * @return a list of introspected tables
     * @throws SQLException if any errors in introspection
     */
    public List<IntrospectedTable> introspectTables(TableConfiguration tc)
            throws SQLException {

        // 从数据库中获取原始列
        Map<ActualTableName, List<IntrospectedColumn>> columns = getColumns(tc);

        if (columns.isEmpty()) {
            warnings.add(getString("Warning.19", tc.getCatalog(), //$NON-NLS-1$
                    tc.getSchema(), tc.getTableName()));
            return Collections.emptyList();
        }

        //去除忽略的列,根据配置文件<ignoreColumn column=""/>
        removeIgnoredColumns(tc, columns);
        //重命名
        calculateExtraColumnInformation(tc, columns);
        //override
        applyColumnOverrides(tc, columns);
        //主键
        calculateIdentityColumns(tc, columns);

        List<IntrospectedTable> introspectedTables = calculateIntrospectedTables(tc, columns);

        // now introspectedTables has all the columns from all the
        // tables in the configuration. Do some validation...
        // introspectedTables配置中所有表中的所有列。做一些验证

        Iterator<IntrospectedTable> iter = introspectedTables.iterator();
        while (iter.hasNext()) {
            IntrospectedTable introspectedTable = iter.next();

            if (!introspectedTable.hasAnyColumns()) {
                // add warning that the table has no columns, remove from the
                // list
                String warning = getString("Warning.1", introspectedTable.getFullyQualifiedTable().toString()); //$NON-NLS-1$
                warnings.add(warning);
                iter.remove();
            } else if (!introspectedTable.hasPrimaryKeyColumns() && !introspectedTable.hasBaseColumns()) {
                // add warning that the table has only BLOB columns, remove from
                // the list
                String warning = getString("Warning.18", introspectedTable.getFullyQualifiedTable().toString()); //$NON-NLS-1$
                warnings.add(warning);
                iter.remove();
            } else {
                // now make sure that all columns called out in the
                // configuration
                // actually exist
                reportIntrospectionWarnings(introspectedTable, tc, introspectedTable.getFullyQualifiedTable());
            }
        }

        return introspectedTables;
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
                        logger.debug(getString("Tracing.3", //$NON-NLS-1$
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
            replaceString = replaceString == null ? "" : replaceString; //$NON-NLS-1$
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
                        introspectedColumn.setJdbcTypeName("OTHER"); //$NON-NLS-1$

                        String warning = getString("Warning.14", //$NON-NLS-1$
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
                        logger.debug(getString("Tracing.4", //$NON-NLS-1$
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
            }
        }
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
            logger.debug(getString("Tracing.1", fullTableName)); //$NON-NLS-1$
        }

        Set<String> fkColumnNames = new HashSet<>();
        ResultSet importedKeys = databaseMetaData.getImportedKeys(localCatalog, localSchema, localTableName);
        if (importedKeys.next()) {
            String fkColumnName = importedKeys.getString("FKCOLUMN_NAME");
            fkColumnNames.add(fkColumnName);
        }
        closeResultSet(importedKeys);

        ResultSet rs = databaseMetaData.getColumns(localCatalog, localSchema, localTableName, "%"); //$NON-NLS-1$

        boolean supportsIsAutoIncrement = false;
        boolean supportsIsGeneratedColumn = false;
        ResultSetMetaData rsmd = rs.getMetaData();
        int colCount = rsmd.getColumnCount();
        for (int i = 1; i <= colCount; i++) {
            if ("IS_AUTOINCREMENT".equals(rsmd.getColumnName(i))) { //$NON-NLS-1$
                supportsIsAutoIncrement = true;
            }
            if ("IS_GENERATEDCOLUMN".equals(rsmd.getColumnName(i))) { //$NON-NLS-1$
                supportsIsGeneratedColumn = true;
            }
        }

        Map<String, String> remarks = strategy.getColumnRemarks(connection,localTableName);
        int order = 10;
        while (rs.next()) {
            IntrospectedColumn introspectedColumn = ObjectFactory.createIntrospectedColumn(context);

            introspectedColumn.setTableAlias(tc.getAlias());
            introspectedColumn.setJdbcType(rs.getInt("DATA_TYPE")); //$NON-NLS-1$
            introspectedColumn.setActualTypeName(rs.getString("TYPE_NAME")); //$NON-NLS-1$
            introspectedColumn.setLength(rs.getInt("COLUMN_SIZE")); //$NON-NLS-1$
            introspectedColumn.setActualColumnName(rs.getString("COLUMN_NAME")); //$NON-NLS-1$
            introspectedColumn.setNullable(rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable); //$NON-NLS-1$
            introspectedColumn.setScale(rs.getInt("DECIMAL_DIGITS")); //$NON-NLS-1$
            if (fkColumnNames.contains(introspectedColumn.getActualColumnName())) {
                introspectedColumn.setForeignKey(true);
            }
            //introspectedColumn.setRemarks(rs.getString("REMARKS")); //$NON-NLS-1$
            String remark = null;
            if (!remarks.isEmpty()) {
                remark = remarks.get(introspectedColumn.getActualColumnName());
            }
            if (remark != null) {
                introspectedColumn.setRemarks(remark); //$NON-NLS-1$
            } else {
                introspectedColumn.setRemarks(rs.getString("REMARKS")); //$NON-NLS-1$
            }
            introspectedColumn.setDefaultValue(rs.getString("COLUMN_DEF")); //$NON-NLS-1$

            if (supportsIsAutoIncrement) {
                introspectedColumn.setAutoIncrement("YES".equals(rs.getString("IS_AUTOINCREMENT"))); //$NON-NLS-1$ //$NON-NLS-2$
            }

            if (supportsIsGeneratedColumn) {
                introspectedColumn.setGeneratedColumn("YES".equals(rs.getString("IS_GENERATEDCOLUMN"))); //$NON-NLS-1$ //$NON-NLS-2$
            }

            ActualTableName atn = new ActualTableName(
                    rs.getString("TABLE_CAT"), //$NON-NLS-1$
                    rs.getString("TABLE_SCHEM"), //$NON-NLS-1$
                    rs.getString("TABLE_NAME")); //$NON-NLS-1$

            List<IntrospectedColumn> columns = answer.computeIfAbsent(atn, k -> new ArrayList<>());

            introspectedColumn.setOrder(order);

            columns.add(introspectedColumn);

            order++;

            if (logger.isDebugEnabled()) {
                logger.debug(getString(
                        "Tracing.2", //$NON-NLS-1$
                        introspectedColumn.getActualColumnName(), Integer
                                .toString(introspectedColumn.getJdbcType()),
                        atn.toString()));
            }
        }

        closeResultSet(rs);

        if (answer.size() > 1
                && !stringContainsSQLWildcard(localSchema)
                && !stringContainsSQLWildcard(localTableName)) {
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

            warnings.add(getString("Warning.25", //$NON-NLS-1$
                    inputAtn.toString(), sb.toString()));
        }

        return answer;
    }

    private String escapeName(String localName, String escapeString) {
        StringTokenizer st = new StringTokenizer(localName, "_%", true); //$NON-NLS-1$
        StringBuilder sb = new StringBuilder();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.equals("_") //$NON-NLS-1$
                    || token.equals("%")) { //$NON-NLS-1$
                sb.append(escapeString);
            }
            sb.append(token);
        }
        return sb.toString();
    }

    private List<IntrospectedTable> calculateIntrospectedTables(TableConfiguration tc, Map<ActualTableName, List<IntrospectedColumn>> columns) throws SQLException {
        boolean delimitIdentifiers = tc.isDelimitIdentifiers()
                || stringContainsSpace(tc.getCatalog())
                || stringContainsSpace(tc.getSchema())
                || stringContainsSpace(tc.getTableName());

        List<IntrospectedTable> answer = new ArrayList<>();

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

            IntrospectedTable introspectedTable = ObjectFactory.createIntrospectedTable(tc, table, context);

            String dbType = this.databaseMetaData.getDatabaseProductName().toLowerCase();
            introspectedTable.setDbType(dbType);

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
     * @param introspectedTable the introspected table to enhance
     */
    private void enhanceIntrospectedTable(IntrospectedTable introspectedTable) {
        try {
            FullyQualifiedTable fqt = introspectedTable.getFullyQualifiedTable();

            ResultSet rs = databaseMetaData.getTables(fqt.getIntrospectedCatalog(), fqt.getIntrospectedSchema(),
                    fqt.getIntrospectedTableName(), null);
            if (rs.next()) {
                String remarks = rs.getString("REMARKS"); //$NON-NLS-1$
                String tableType = rs.getString("TABLE_TYPE"); //$NON-NLS-1$
                introspectedTable.setRemarks(remarks);
                introspectedTable.setTableType(tableType);
            }
            closeResultSet(rs);
        } catch (SQLException e) {
            warnings.add(getString("Warning.27", e.getMessage())); //$NON-NLS-1$
        }
    }
}
