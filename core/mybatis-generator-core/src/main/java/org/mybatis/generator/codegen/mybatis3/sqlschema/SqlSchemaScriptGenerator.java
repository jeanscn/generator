package org.mybatis.generator.codegen.mybatis3.sqlschema;

import com.vgosoft.core.constant.BaseEmpty;
import com.vgosoft.core.db.enums.JDBCTypeTypeEnum;
import com.vgosoft.core.db.types.JdbcTypeInformation;
import com.vgosoft.core.db.util.JdbcUtils;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.ForeignKeyInfo;
import org.mybatis.generator.api.IndexInfo;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.custom.db.DatabaseDDLDialects;
import org.mybatis.generator.internal.util.SqlScriptUtil;

import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * sql脚本生成器
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-06-21 18:53
 * @version 3.0
 */
public class SqlSchemaScriptGenerator extends AbstractSqlScriptGenerator {

    private final AbstractIntrospectedTable introspectedTable;
    private final DatabaseDDLDialects databaseDDLDialects;

    public SqlSchemaScriptGenerator(AbstractIntrospectedTable introspectedTable, DatabaseDDLDialects databaseDdlDialects) {
        this.introspectedTable = introspectedTable;
        this.databaseDDLDialects = databaseDdlDialects;
    }

    @Override
    public String getSqlScript() {
        List<String> ret = new ArrayList<>();
        String tableName = introspectedTable.getTableConfiguration().getTableName();
        String createStatement = databaseDDLDialects.getCreateStatement();
        String quotedTableName = databaseDDLDialects.quoteIdentifier(tableName);
        String escapedTableRemark = databaseDDLDialects.escapeSqlLiteral(introspectedTable.getRemarks(false));

        List<String> columnSql = new ArrayList<>();
        List<String> postStatements = new ArrayList<>();
        ret.add("-- ----------------------------");
        ret.add(VStringUtil.format("-- Table structure for {0}", tableName));
        ret.add("-- ----------------------------");
        ret.add("");

        if (this.databaseDDLDialects.supportsCreateTableIfNotExists()) {
            ret.add(VStringUtil.format("CREATE TABLE IF NOT EXISTS {0}  (", quotedTableName));
        } else {
            ret.add(VStringUtil.format("CREATE TABLE {0}  (", quotedTableName));
        }

        for (IntrospectedColumn col : this.introspectedTable.getAllColumns()) {

            String character = BaseEmpty.EMPTY_STRING;
            if ((SqlScriptUtil.isMysqlOrMariadbDatabase(this.databaseDDLDialects)) && !"JSON".equalsIgnoreCase(VStringUtil.toCamelCase(col.getActualTypeName()))) {
                character = JDBCTypeTypeEnum.getJdbcTypeType(JDBCType.valueOf(col.getJdbcType())).equals(JDBCTypeTypeEnum.CHARACTER)?" CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci ":" ";
            }
            String remarks = databaseDDLDialects.escapeSqlLiteral(col.getRemarks(false));
            //JdbcTypeInformation jdbcTypeInformation = new JdbcTypeInformation(JDBCType.valueOf(col.getJdbcType()), col.ja(), databaseDDLDialects.get);
            String rowSql = VStringUtil.format(createStatement,
                    databaseDDLDialects.quoteIdentifier(col.getActualColumnName()),
                    VStringUtil.toCamelCase(col.getActualTypeName()),
                    col.getSqlFragmentLength(),
                    character,
                    SqlScriptUtil.getSqlFragmentNotNull(col),
                    remarks);
            columnSql.add(rowSql);
        }
        if (!introspectedTable.getPrimaryKeyColumns().isEmpty()) {
            String primaryKeys = introspectedTable.getPrimaryKeyColumns().stream()
                    .map(IntrospectedColumn::getActualColumnName)
                    .map(databaseDDLDialects::quoteIdentifier)
                    .collect(Collectors.joining(","));
            if (this.databaseDDLDialects.supportsIndexUsingClause()) {
                columnSql.add("    PRIMARY KEY (" + primaryKeys + ") USING BTREE");
            } else {
                columnSql.add("    PRIMARY KEY (" + primaryKeys + ")");
            }
        }
        //生成添加索引的sql
        if (!introspectedTable.getIndexes().isEmpty()) {
            for (IndexInfo index : introspectedTable.getIndexes()) {
                String indexColumns = index.getColumnNames().stream()
                        .map(databaseDDLDialects::quoteIdentifier)
                        .collect(Collectors.joining(","));
                String indexName = databaseDDLDialects.quoteIdentifier(index.getName());
                String indexType = VStringUtil.stringHasValue(index.getType()) ? index.getType() : "BTREE";
                if (databaseDDLDialects.supportsInlineIndexDefinition()) {
                    String indexSql = VStringUtil.format("    INDEX {0} ({1})",
                            indexName,
                            indexColumns);
                    if (databaseDDLDialects.supportsIndexUsingClause()) {
                        indexSql = VStringUtil.format("{0} USING {1}", indexSql, indexType);
                    }
                    if (databaseDDLDialects.supportsIndexCommentClause() && VStringUtil.stringHasValue(index.getComments())) {
                        indexSql = VStringUtil.format("{0} COMMENT {1}", indexSql, "'" + databaseDDLDialects.escapeSqlLiteral(index.getComments()) + "'" );
                    }
                    columnSql.add(indexSql);
                } else {
                    String indexSql = VStringUtil.format("CREATE INDEX {0} ON {1} ({2})",
                            indexName,
                            quotedTableName,
                            indexColumns);
                    if (databaseDDLDialects.supportsIndexUsingClause()) {
                        indexSql = VStringUtil.format("{0} USING {1}", indexSql, indexType);
                    }
                    postStatements.add(indexSql + ";");
                }
            }
        }
        // 生成外键的sql：CONSTRAINT `fk_role_id` FOREIGN KEY (`role_id`) REFERENCES `org_role` (`id_`) ON DELETE CASCADE ON UPDATE RESTRICT
        if (!introspectedTable.getForeignKeys().isEmpty()) {
            String formatSql = "    CONSTRAINT {0} FOREIGN KEY ({1}) REFERENCES {2} ({3}) ON DELETE {4} ON UPDATE {5}";
            introspectedTable.getForeignKeys().forEach((foreignKeyName, foreignKeyInfos) -> {
                String foreignKeySql = VStringUtil.format(formatSql,
                        databaseDDLDialects.quoteIdentifier(foreignKeyName),
                        foreignKeyInfos.stream().map(ForeignKeyInfo::getFkColumnName).map(databaseDDLDialects::quoteIdentifier).collect(Collectors.joining(",")),
                        databaseDDLDialects.quoteIdentifier(foreignKeyInfos.get(0).getPkTableName()),
                        foreignKeyInfos.stream().map(ForeignKeyInfo::getPkColumnName).map(databaseDDLDialects::quoteIdentifier).collect(Collectors.joining(",")),
                        foreignKeyInfos.get(0).getDeleteRule().codeName(),
                        foreignKeyInfos.get(0).getUpdateRule().codeName());
                columnSql.add(foreignKeySql);
            });
        }
        ret.add(String.join(",\n", columnSql));
        String createTableSuffix = databaseDDLDialects.createTableSuffix(escapedTableRemark);
        if (VStringUtil.stringHasValue(createTableSuffix)) {
            ret.add(")" + createTableSuffix + ";");
        } else {
            ret.add(");");
        }

        if (databaseDDLDialects.supportsCommentOnStatement()) {
            if (VStringUtil.stringHasValue(escapedTableRemark)) {
                postStatements.add("COMMENT ON TABLE " + quotedTableName + " IS '" + escapedTableRemark + "';");
            }
            introspectedTable.getAllColumns().stream()
                    .filter(column -> VStringUtil.stringHasValue(column.getRemarks(false)))
                    .forEach(column -> postStatements.add(
                            "COMMENT ON COLUMN "
                                    + quotedTableName + "."
                                    + databaseDDLDialects.quoteIdentifier(column.getActualColumnName())
                                    + " IS '"
                                    + databaseDDLDialects.escapeSqlLiteral(column.getRemarks(false))
                                    + "';"
                    ));
        }
        if (!postStatements.isEmpty()) {
            ret.add("");
            ret.addAll(postStatements);
        }
        return String.join("\n", ret);
    }

}
