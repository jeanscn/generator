package org.mybatis.generator.custom.db;

import com.vgosoft.core.annotation.ColumnMeta;
import com.vgosoft.core.db.enums.JDBCTypeTypeEnum;
import com.vgosoft.core.db.types.JavaTypeResolver;
import com.vgosoft.core.db.types.JavaTypeResolverDefaultImpl;
import com.vgosoft.core.db.types.JdbcTypeInformation;
import com.vgosoft.core.util.VReflectionUtils;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.internal.ObjectFactory;
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
public class ValidateDatabaseTables {

    private final List<String> warnings;

    private final List<IntrospectedTable> tables;

    private final Connection connection;

    private final List<IntrospectedColumn> addColumns;

    private final List<IntrospectedColumn> updateColumns;

    private String databaseProductName;

    public ValidateDatabaseTables(List<IntrospectedTable> tables, Connection connection, List<String> warnings) {
       this.tables = tables;
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
        checkTables();
        String sql = getSql();
        if (stringHasValue(sql)) {
            try {
                Statement statement = connection.createStatement();
                statement.execute(sql);
                //成功后更新List<IntrospectedTable>
                updateTableIntrospectedTables();
            } catch (SQLException e) {
                warnings.add("更新数据库时发生错误："+e.getMessage());
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
                }else if(introspectedColumn.isIdentity()){
                    primaryKeyColumns.add(introspectedColumn);
                }else{
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
                        copyColumnProperty(introspectedColumn,baseColumn);
                    }
                }
                for (IntrospectedColumn primaryKeyColumn : primaryKeyColumns) {
                    if (primaryKeyColumn.getActualColumnName().equals(introspectedColumn.getActualColumnName())) {
                        copyColumnProperty(introspectedColumn,primaryKeyColumn);
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

    private void copyColumnProperty(IntrospectedColumn source,IntrospectedColumn target){
        target.setIdentity(source.isIdentity());
        target.setLength(source.getLength());
        target.setRemarks(source.getRemarks());
        target.setDefaultValue(source.getDefaultValue());
        target.setJdbcTypeName(source.getJdbcTypeName());
        target.setNullable(source.isNullable());
        target.setScale(source.getScale());
    }

    private void checkTables(){
        JavaTypeResolverDefaultImpl javaTypeResolverDefault = new JavaTypeResolverDefaultImpl(databaseProductName);
        for (IntrospectedTable table : tables) {
            TableConfiguration tc = table.getTableConfiguration();
            String property = tc.getProperty(PropertyRegistry.ANY_ROOT_CLASS);
            if (!stringHasValue(property)) {
                return;
            }
            try {
                Class<?> aClass = ObjectFactory.externalClassForName(property);
                Field[] annotationFields = VReflectionUtils.getAnnotationFields(aClass, ColumnMeta.class);
                for (Field declaredField : annotationFields) {
                    ColumnMeta columnMeta = declaredField.getAnnotation(ColumnMeta.class);
                    long count = table.getPrimaryKeyColumns().stream()
                            .filter(t -> columnMeta.value().equals(t.getActualColumnName()))
                            .count();
                    boolean isPk = count>0;
                    if (table.getColumn(columnMeta.value()).isPresent()) {
                        //比较是否需要更新
                        IntrospectedColumn introspectedColumn = table.getColumn(columnMeta.value()).get();
                        JdbcTypeInformation jdbcTypeInformation = javaTypeResolverDefault
                                .getJdbcTypeInformation(JDBCType.valueOf(introspectedColumn.getJdbcType()));
                        jdbcTypeInformation.setColumnLength(columnMeta.size());
                        String remark = stringHasValue(columnMeta.remarks())?columnMeta.remarks():columnMeta.description();//字段完整注释
                        int columnLength = jdbcTypeInformation.getColumnLength();//字段长度
                        if (!introspectedColumn.getActualColumnName().equalsIgnoreCase(columnMeta.value()) //字段名称
                                || introspectedColumn.getJdbcType()!=columnMeta.type().getVendorTypeNumber() //类型
                                || (columnMeta.size()>0 && (introspectedColumn.getLength() != columnLength))
                                || (columnMeta.scale()>0 && (columnMeta.scale()!= introspectedColumn.getScale())) //小数点位数
                                || !introspectedColumn.getRemarks().equals(remark)
                                || isPk!=columnMeta.pkid()
                                || (introspectedColumn.isNullable() != columnMeta.nullable())
                                || (!columnMeta.nullable() && !columnMeta.pkid()
                                        && !columnMeta.defaultValue().equals(introspectedColumn.getDefaultValue()))
                        ) {
                            IntrospectedColumn newColumn = columnBuilder(columnMeta,javaTypeResolverDefault,declaredField,table);
                            newColumn.setIntrospectedTable(table);
                            updateColumns.add(newColumn);
                        }
                    }else{
                        //需要创建的列
                        IntrospectedColumn introspectedColumn = columnBuilder(columnMeta,javaTypeResolverDefault,declaredField,table);
                        introspectedColumn.setIntrospectedTable(table);
                        addColumns.add(introspectedColumn);
                    }
                }
            } catch (ClassNotFoundException e) {
                warnings.add("加载父类发生错误，未找到类："+property);
            }
        }
    }

    private IntrospectedColumn columnBuilder(ColumnMeta columnMeta, JavaTypeResolver javaTypeResolver,Field declaredField,IntrospectedTable table){
        JdbcTypeInformation jdbcTypeInformation = javaTypeResolver.getJdbcTypeInformation(columnMeta.type());
        jdbcTypeInformation.setColumnLength(columnMeta.size());
        IntrospectedColumn introspectedColumn = new IntrospectedColumn();
        introspectedColumn.setActualColumnName(columnMeta.value());
        introspectedColumn.setJdbcType(columnMeta.type().getVendorTypeNumber());
        introspectedColumn.setActualTypeName(jdbcTypeInformation.getActualTypeName());
        introspectedColumn.setLength(columnMeta.size());
        introspectedColumn.setScale(columnMeta.scale());
        String remark = stringHasValue(columnMeta.remarks())?columnMeta.remarks():columnMeta.description();
        introspectedColumn.setRemarks(remark);
        String defaultValue = columnMeta.nullable()?null:columnMeta.defaultValue();
        introspectedColumn.setDefaultValue(defaultValue);
        introspectedColumn.setNullable(columnMeta.nullable());
        introspectedColumn.setJdbcTypeName(columnMeta.type().getName());
        introspectedColumn.setIdentity(columnMeta.pkid());
        introspectedColumn.setJavaProperty(declaredField.getName());
        introspectedColumn.setFullyQualifiedJavaType(new FullyQualifiedJavaType(declaredField.getType().getCanonicalName()));

        introspectedColumn.setTableAlias(table.getTableConfiguration().getAlias());
        introspectedColumn.setContext(table.getContext());

        introspectedColumn.setTypeHandler(null);
        introspectedColumn.setSequenceColumn(false);
        introspectedColumn.setColumnNameDelimited(false);
        introspectedColumn.setAutoIncrement(false);
        introspectedColumn.setGeneratedColumn(false);
        introspectedColumn.setGeneratedAlways(false);

        introspectedColumn.setPosition(columnMeta.position());

        return introspectedColumn;
    }

    private String getSql(){
        Map<String, List<IntrospectedColumn>> addMap = groupColumns(addColumns);
        Map<String, List<IntrospectedColumn>> updMap = groupColumns(updateColumns);
        StringBuilder ret = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        for (IntrospectedTable table : tables) {
            TableConfiguration tc = table.getTableConfiguration();
            String sqlTableName = StringUtility.composeFullyQualifiedTableName(tc.getCatalog(), tc.getSchema(), tc.getTableName(), '.');
            if (addMap.containsKey(tc.getTableName()) || updMap.containsKey(tc.getTableName())) {
                if (addMap.containsKey(tc.getTableName())) {
                    List<IntrospectedColumn> introspectedColumns = addMap.get(tc.getTableName());
                    String collect = introspectedColumns.stream().map(IntrospectedColumn::getActualColumnName).collect(Collectors.joining(","));
                    warnings.add(VStringUtil.format("需要添加的字段：{0}->{1}",tc.getTableName(),collect));
                    sb.append(getColumnSql(introspectedColumns, "ADD"));
                }
                if (updMap.containsKey(tc.getTableName())) {
                    List<IntrospectedColumn> introspectedColumns = updMap.get(tc.getTableName());
                    String collect = introspectedColumns.stream().map(IntrospectedColumn::getActualColumnName).collect(Collectors.joining(","));
                    warnings.add(VStringUtil.format("需要更新的字段：{0}->{1}",tc.getTableName(),collect));
                    if (sb.length()>0) {
                        sb.append(",\n");
                    }
                    sb.append(getColumnSql(updMap.get(tc.getTableName()), "MODIFY"));
                }
                //增加主键
                List<String> collect1 = Stream.of(addColumns.stream(), updateColumns.stream())
                        .flatMap(Function.identity())
                        .filter(IntrospectedColumn::isIdentity)
                        .map(IntrospectedColumn::getActualColumnName)
                        .collect(Collectors.toList());
                List<String> pkColumnName = table.getPrimaryKeyColumns().stream()
                        .map(IntrospectedColumn::getActualColumnName)
                        .collect(Collectors.toList());
                List<String> collect = collect1.stream()
                        .filter(t -> !pkColumnName.contains(t))
                        .collect(Collectors.toList());
                if (collect.size()>0) {
                    warnings.add(VStringUtil.format("需要设置为主键的字段：{0}->{1}", tc.getTableName(),String.join(",", collect)));
                    if (sb.length()>0) sb.append(",\n");
                    sb.append("ADD PRIMARY KEY (");
                    for (int i = 0; i < collect.size(); i++) {
                        if (i>0) sb.append(",");
                        sb.append("`").append(collect.get(i)).append("`");
                    }
                    sb.append(")");
                }
            }
            if (sb.length()>0) {
                ret.append("ALTER TABLE `");
                ret.append(sqlTableName);
                ret.append("`\n");
                ret.append(sb);
            }
        }
        if (ret.length()>0) {
            ret.append(";");
        }
        return ret.toString();
    }

    private String getColumnSql(List<IntrospectedColumn> columns,String actionKey){
        StringBuilder sb = new StringBuilder();
        StringBuilder not_null = new StringBuilder();
        StringBuilder sb_length = new StringBuilder();
        for (IntrospectedColumn col : columns) {
            sb_length.setLength(0);
            not_null.setLength(0);
            //长度
            if (JDBCType.DATE.getVendorTypeNumber() != col.getJdbcType()) {
                sb_length.append("(");
                if (JDBCType.TIMESTAMP.getVendorTypeNumber()==col.getJdbcType()) {
                    sb_length.append("0");
                }else{
                    sb_length.append(col.getLength());
                }
                if (col.getScale()>0) {
                    sb_length.append(",").append(col.getScale());
                }
                sb_length.append(") ");
            }else{
                sb_length.append(" ");
            }
            //不允许空
            if (!col.isNullable() || stringHasValue(col.getDefaultValue())) {
                not_null.append("NOT NULL ");
                if (stringHasValue(col.getDefaultValue())) {
                    JDBCTypeTypeEnum jdbcTypeType = JDBCTypeTypeEnum.getJDBCTypeType(JDBCType.valueOf(col.getJdbcType()));
                    if (jdbcTypeType.equals(JDBCTypeTypeEnum.CHARACTER)) {
                        not_null.append(" DEFAULT '").append(col.getDefaultValue()).append("' ");
                    }else{
                        not_null.append(" DEFAULT ").append(col.getDefaultValue()).append(" ");
                    }
                }
            }

            /*
             * sql模板
             * 如：ADD COLUMN `col_varchar` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'varchar字段' AFTER `PARENT_ID`;
             * */
            DatabaseDDLDialects databaseDialect = DatabaseDDLDialects.getDatabaseDialect(this.databaseProductName);
            String COLUMN_STATEMENT = databaseDialect.getColumnModifyStatement();
            String character = JDBCTypeTypeEnum.getJDBCTypeType(JDBCType.valueOf(col.getJdbcType())).equals(JDBCTypeTypeEnum.CHARACTER)?" CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci ":" ";
            String position = stringHasValue(col.getPosition())?" "+col.getPosition():"";
            String onAddRow = VStringUtil.format(COLUMN_STATEMENT,
                    actionKey,
                    col.getActualColumnName(),
                    col.getActualTypeName(),
                    sb_length.toString(),
                    character,
                    not_null.toString(),
                    col.getRemarks(),
                    position);
            if (sb.length()>0) {
                sb.append(",").append("\n");
            }
            sb.append(onAddRow);
        }
        return sb.toString();
    }

    /**
     * 按表名分组列列表
     * */
    private Map<String, List<IntrospectedColumn>> groupColumns(List<IntrospectedColumn> addColumns) {
        return addColumns.stream()
                .collect(Collectors.groupingBy(t->t.getIntrospectedTable().getTableConfiguration().getTableName()));
    }
}
