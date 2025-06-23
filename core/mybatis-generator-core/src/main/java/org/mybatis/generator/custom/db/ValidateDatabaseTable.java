package org.mybatis.generator.custom.db;

import com.vgosoft.core.annotation.ColumnMeta;
import com.vgosoft.core.db.types.JavaTypeResolver;
import com.vgosoft.core.db.types.JavaTypeResolverDefaultImpl;
import com.vgosoft.core.db.types.JdbcTypeInformation;
import com.vgosoft.tool.core.VReflectionUtil;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.internal.ObjectFactory;
import org.mybatis.generator.internal.util.SqlScriptUtil;
import org.mybatis.generator.internal.util.StringUtility;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-05-01 04:33
 * @version 3.0
 */
public class ValidateDatabaseTable {

    private final List<String> warnings;

    private final IntrospectedTable introspectedTable;

    private final Connection connection;

    private final List<IntrospectedColumn> addColumns;

    private final List<IntrospectedColumn> updateColumns;

    private String databaseProductName;

    public ValidateDatabaseTable(IntrospectedTable introspectedTable, Connection connection, List<String> warnings) {
        this.introspectedTable = introspectedTable;
        this.warnings = warnings;
        this.connection = connection;
        this.addColumns = new ArrayList<>();
        this.updateColumns = new ArrayList<>();
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            this.databaseProductName = metaData.getDatabaseProductName();
        } catch (SQLException e) {
            this.databaseProductName = "";
        }
    }

    public void executeUpdate() {
        checkTable();
        String sql = getSql();
        if (stringHasValue(sql)) {
            warnings.add("更新数据库Sql：" + sql);
            try {
                Statement statement = connection.createStatement();
                statement.execute(sql);
                //成功后更新List<IntrospectedTable>
                updateTableIntrospectedTables();
            } catch (SQLException e) {
                warnings.add("更新数据库时发生错误：(" + e.getMessage()+") " + sql);
            }
        }
    }

    private void updateTableIntrospectedTables() {
        //处理增加的
        Map<String, List<IntrospectedColumn>> addMap = groupColumns(addColumns);
        Map<String, List<IntrospectedColumn>> updMap = groupColumns(updateColumns);
        for (Map.Entry<String, List<IntrospectedColumn>> stringListEntry : addMap.entrySet()) {
            List<IntrospectedColumn> value = stringListEntry.getValue();
            IntrospectedTable introspectedTable = value.get(0).getIntrospectedTable();
            List<IntrospectedColumn> baseColumns = introspectedTable.getBaseColumns();
            List<IntrospectedColumn> primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
            List<IntrospectedColumn> blobColumns = introspectedTable.getBLOBColumns();
            for (IntrospectedColumn introspectedColumn : value) {
                if (introspectedColumn.isBLOBColumn()) {
                    blobColumns.add(introspectedColumn);
                } else if (introspectedColumn.isPrimaryKey()) {
                    primaryKeyColumns.add(introspectedColumn);
                } else {
                    baseColumns.add(introspectedColumn);
                }
            }
        }
        for (Map.Entry<String, List<IntrospectedColumn>> stringListEntry : updMap.entrySet()) {
            List<IntrospectedColumn> value = stringListEntry.getValue();
            IntrospectedTable introspectedTable = value.get(0).getIntrospectedTable();
            List<IntrospectedColumn> baseColumns = introspectedTable.getBaseColumns();
            List<IntrospectedColumn> primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
            List<IntrospectedColumn> blobColumns = introspectedTable.getBLOBColumns();
            for (IntrospectedColumn introspectedColumn : value) {
                for (IntrospectedColumn baseColumn : baseColumns) {
                    if (baseColumn.getActualColumnName().equals(introspectedColumn.getActualColumnName())) {
                        copyColumnProperty(introspectedColumn, baseColumn);
                    }
                }
                for (IntrospectedColumn primaryKeyColumn : primaryKeyColumns) {
                    if (primaryKeyColumn.getActualColumnName().equals(introspectedColumn.getActualColumnName())) {
                        copyColumnProperty(introspectedColumn, primaryKeyColumn);
                    }
                }
                for (IntrospectedColumn blobColumn : blobColumns) {
                    if (blobColumn.getActualColumnName().equals(introspectedColumn.getActualColumnName())) {
                        copyColumnProperty(introspectedColumn, blobColumn);
                    }
                }
            }

        }
    }

    private void copyColumnProperty(IntrospectedColumn source, IntrospectedColumn target) {
        target.setIdentity(source.isIdentity());
        target.setLength(source.getLength());
        target.setRemarks(source.getRemarks(false));
        target.setSubRemarks(source.getSubRemarks());
        target.setDefaultValue(source.getDefaultValue());
        target.setJdbcTypeName(source.getJdbcTypeName());
        target.setNullable(source.isNullable());
        target.setScale(source.getScale());
    }

    private void checkTable() {
        JavaTypeResolverDefaultImpl javaTypeResolverDefault = new JavaTypeResolverDefaultImpl(databaseProductName);
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        String rootClassName = tc.getProperty(PropertyRegistry.ROOT_CLASS);
        if (!stringHasValue(rootClassName)) {
            return;
        }
        for (Field declaredField : getClassFieldsSorted(rootClassName)) {
            ColumnMeta columnMeta = declaredField.getAnnotation(ColumnMeta.class);
            boolean isPk = introspectedTable.getPrimaryKeyColumns().stream()
                    .anyMatch(t -> columnMeta.value().equalsIgnoreCase(t.getActualColumnName()));
            if (introspectedTable.getColumn(columnMeta.value()).isPresent()) {
                //比较是否需要更新
                IntrospectedColumn introspectedColumn = introspectedTable.getColumn(columnMeta.value()).get();
                JdbcTypeInformation jdbcTypeInformation = javaTypeResolverDefault
                        .getJdbcTypeInformation(JDBCType.valueOf(introspectedColumn.getJdbcType()));
                String remark = stringHasValue(columnMeta.remarks()) ? columnMeta.remarks() : columnMeta.description();//字段完整注释
                if (!introspectedColumn.isBLOBColumn()) {
                    jdbcTypeInformation.setColumnLength(columnMeta.size());
                    int columnLength = jdbcTypeInformation.getColumnLength();//字段长度
                    if (!introspectedColumn.getActualColumnName().equalsIgnoreCase(columnMeta.value()) //字段名称
                            || introspectedColumn.getJdbcType() != columnMeta.type().getVendorTypeNumber() //类型
                            || (columnMeta.size() > 0 && (introspectedColumn.getLength() != columnLength))
                            || (columnMeta.scale() > 0 && (columnMeta.scale() != introspectedColumn.getScale())) //小数点位数
                            || !introspectedColumn.getRemarks(false).equals(remark)
                            || isPk != columnMeta.pkid()
                            || (introspectedColumn.isNullable() != columnMeta.nullable())
                            || (!columnMeta.pkid() && VStringUtil.stringHasValue(columnMeta.defaultValue()) && (!columnMeta.defaultValue().equalsIgnoreCase(introspectedColumn.getDefaultValue()) && !columnMeta.defaultValue().replace("'", "").equalsIgnoreCase(introspectedColumn.getDefaultValue())))//默认值
                    ) {
                        IntrospectedColumn newColumn = columnBuilder(columnMeta, javaTypeResolverDefault, declaredField, introspectedTable,introspectedColumn);
                        newColumn.setIntrospectedTable(introspectedTable);
                        updateColumns.add(newColumn);
                    }
                }else{
                    if (!introspectedColumn.getActualColumnName().equalsIgnoreCase(columnMeta.value()) //字段名称
                            || !introspectedColumn.getRemarks(false).equals(remark)
                            || isPk != columnMeta.pkid()
                            || (introspectedColumn.isNullable() != columnMeta.nullable())
                    ) {
                        IntrospectedColumn newColumn = columnBuilder(columnMeta, javaTypeResolverDefault, declaredField, introspectedTable,introspectedColumn);
                        newColumn.setIntrospectedTable(introspectedTable);
                        updateColumns.add(newColumn);
                    }
                }
            } else {
                //需要创建的列
                IntrospectedColumn introspectedColumn = columnBuilder(columnMeta, javaTypeResolverDefault, declaredField, introspectedTable,null);
                introspectedColumn.setIntrospectedTable(introspectedTable);
                addColumns.add(introspectedColumn);
            }
        }

    }

    private IntrospectedColumn columnBuilder(ColumnMeta columnMeta, JavaTypeResolver javaTypeResolver, Field declaredField, IntrospectedTable table,IntrospectedColumn originalColumn) {
        JdbcTypeInformation jdbcTypeInformation = javaTypeResolver.getJdbcTypeInformation(columnMeta.type());
        jdbcTypeInformation.setColumnLength(columnMeta.size());
        IntrospectedColumn introspectedColumn = new IntrospectedColumn();
        introspectedColumn.setActualColumnName(columnMeta.value());
        introspectedColumn.setJdbcType(columnMeta.type().getVendorTypeNumber());
        introspectedColumn.setActualTypeName(jdbcTypeInformation.getActualTypeName());
        introspectedColumn.setLength(columnMeta.size());
        introspectedColumn.setScale(columnMeta.scale());
        String remark = stringHasValue(columnMeta.remarks()) ? columnMeta.remarks() : columnMeta.description();
        introspectedColumn.setRemarks(remark);
        introspectedColumn.setDefaultValue(columnMeta.defaultValue());
        introspectedColumn.setNullable(columnMeta.nullable());
        introspectedColumn.setJdbcTypeName(columnMeta.type().getName());
        introspectedColumn.setPrimaryKey(columnMeta.pkid());
        introspectedColumn.setPosition(columnMeta.position());

        introspectedColumn.setJavaProperty(declaredField.getName());
        introspectedColumn.setFullyQualifiedJavaType(new FullyQualifiedJavaType(declaredField.getType().getCanonicalName()));
        introspectedColumn.setTableAlias(table.getTableConfiguration().getAlias());
        introspectedColumn.setContext(table.getContext());
        introspectedColumn.setIntrospectedTable(table);
        if (originalColumn != null) {
            introspectedColumn.setTypeHandler(originalColumn.getTypeHandler());
            introspectedColumn.setSequenceColumn(originalColumn.isSequenceColumn());
            introspectedColumn.setColumnNameDelimited(originalColumn.isColumnNameDelimited());
            introspectedColumn.setAutoIncrement(originalColumn.isAutoIncrement());
            introspectedColumn.setGeneratedColumn(originalColumn.isGeneratedColumn());
            introspectedColumn.setGeneratedAlways(originalColumn.isGeneratedAlways());
            introspectedColumn.setIdentity(originalColumn.isIdentity());
        } else {
            introspectedColumn.setTypeHandler(null);
            introspectedColumn.setSequenceColumn(false);
            introspectedColumn.setColumnNameDelimited(false);
            introspectedColumn.setAutoIncrement(false);
            introspectedColumn.setGeneratedColumn(false);
            introspectedColumn.setGeneratedAlways(false);
            introspectedColumn.setIdentity(false);
        }
        return introspectedColumn;
    }

    private String getSql() {
        Map<String, List<IntrospectedColumn>> addMap = groupColumns(addColumns);
        Map<String, List<IntrospectedColumn>> updMap = groupColumns(updateColumns);
        StringBuilder ret = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        String sqlTableName = StringUtility.composeFullyQualifiedTableName(tc.getCatalog(), tc.getSchema(), tc.getTableName(), '.');
        if (addMap.containsKey(tc.getTableName()) || updMap.containsKey(tc.getTableName())) {
            if (addMap.containsKey(tc.getTableName())) {
                List<IntrospectedColumn> introspectedColumns = addMap.get(tc.getTableName());
                String collect = introspectedColumns.stream().map(IntrospectedColumn::getActualColumnName).collect(Collectors.joining(","));
                warnings.add(VStringUtil.format("需要添加的字段：{0}->{1}", tc.getTableName(), collect));
                sb.append(SqlScriptUtil.getColumnSql(introspectedColumns, "ADD", this.databaseProductName));
            }
            if (updMap.containsKey(tc.getTableName())) {
                List<IntrospectedColumn> introspectedColumns = updMap.get(tc.getTableName());
                String collect = introspectedColumns.stream().map(IntrospectedColumn::getActualColumnName).collect(Collectors.joining(","));
                warnings.add(VStringUtil.format("需要更新的字段：{0}->{1}", tc.getTableName(), collect));
                if (sb.length() > 0) {
                    sb.append(",\n");
                }
                sb.append(SqlScriptUtil.getColumnSql(updMap.get(tc.getTableName()), "MODIFY", this.databaseProductName));
            }
            //增加主键
            List<String> affectPKs = Stream.of(addColumns.stream(), updateColumns.stream())
                    .flatMap(Function.identity())
                    .filter(IntrospectedColumn::isPrimaryKey)
                    .map(IntrospectedColumn::getActualColumnName)
                    .collect(Collectors.toList());
            List<String> pkColumnName = introspectedTable.getPrimaryKeyColumns().stream()
                    .map(IntrospectedColumn::getActualColumnName)
                    .collect(Collectors.toList());
            List<String> collect = affectPKs.stream()
                    .filter(t -> !pkColumnName.contains(t))
                    .collect(Collectors.toList());
            if (!collect.isEmpty()) {
                warnings.add(VStringUtil.format("需要设置为主键的字段：{0}->{1}", tc.getTableName(), String.join(",", collect)));
                if (sb.length() > 0) sb.append(",\n");
                sb.append("ADD PRIMARY KEY (");
                for (int i = 0; i < collect.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append("`").append(collect.get(i)).append("`");
                }
                sb.append(")");
            }
        }
        if (sb.length() > 0) {
            ret.append("ALTER TABLE `");
            ret.append(sqlTableName);
            ret.append("`\n");
            ret.append(sb);
        }

        if (ret.length() > 0) {
            ret.append(";");
        }
        return ret.toString();
    }

    /**
     * 按表名分组列列表
     */
    private Map<String, List<IntrospectedColumn>> groupColumns(List<IntrospectedColumn> addColumns) {
        return addColumns.stream()
                .collect(Collectors.groupingBy(t -> t.getIntrospectedTable().getTableConfiguration().getTableName()));
    }

    private Field[] getClassFieldsSorted(String className) {
        try {
            Class<?> aClass = ObjectFactory.externalClassForName(className);
            return VReflectionUtil.getAnnotationFields(aClass, ColumnMeta.class);
        } catch (ClassNotFoundException e) {
            warnings.add(VStringUtil.format("类{0}不存在", className));
        }
        return new Field[0];
    }
}
