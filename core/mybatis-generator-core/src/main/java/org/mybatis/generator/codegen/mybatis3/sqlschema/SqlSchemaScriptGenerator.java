package org.mybatis.generator.codegen.mybatis3.sqlschema;

import com.vgosoft.core.constant.BaseEmpty;
import com.vgosoft.core.db.enums.JDBCTypeTypeEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.ForeignKeyInfo;
import org.mybatis.generator.api.IndexInfo;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
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

    private final IntrospectedTable introspectedTable;
    private final DatabaseDDLDialects databaseDDLDialects;

    public SqlSchemaScriptGenerator(IntrospectedTable introspectedTable, DatabaseDDLDialects databaseDDLDialects) {
        this.introspectedTable = introspectedTable;
        this.databaseDDLDialects = databaseDDLDialects;
    }

    @Override
    public String getSqlScript() {
        List<String> ret = new ArrayList<>();
        String tableName = introspectedTable.getTableConfiguration().getTableName();
        String createStatement = databaseDDLDialects.getCreateStatement();

        List<String> columnSql = new ArrayList<>();
        ret.add("-- ----------------------------");
        ret.add(VStringUtil.format("-- Table structure for {0}", tableName));
        ret.add("-- ----------------------------");
        ret.add("");
        ret.add(VStringUtil.format("CREATE TABLE IF NOT EXISTS `{0}`  (", tableName));

        for (IntrospectedColumn col : this.introspectedTable.getAllColumns()) {

            String character = BaseEmpty.EMPTY_STRING;
            if (this.databaseDDLDialects.equals(DatabaseDDLDialects.MYSQL) && !VStringUtil.toCamelCase(col.getActualTypeName()).equals("JSON")) {
                character = JDBCTypeTypeEnum.getJDBCTypeType(JDBCType.valueOf(col.getJdbcType())).equals(JDBCTypeTypeEnum.CHARACTER)?" CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci ":" ";
            }

            String rowSql = VStringUtil.format(createStatement,
                    col.getActualColumnName(),
                    VStringUtil.toCamelCase(col.getActualTypeName()),
                    col.getSqlFragmentLength(),
                    character,
                    SqlScriptUtil.getSqlFragmentNotNull(col),
                    col.getRemarks(false));
            columnSql.add(rowSql);
        }
        for (IntrospectedColumn primaryKeyColumn : introspectedTable.getPrimaryKeyColumns()) {
            if (this.databaseDDLDialects.equals(DatabaseDDLDialects.MYSQL)) {
                columnSql.add("    PRIMARY KEY (`"+primaryKeyColumn.getActualColumnName()+"`) USING BTREE");
            }else{
                columnSql.add("    PRIMARY KEY (`"+primaryKeyColumn.getActualColumnName()+"`)");
            }
        }
        //生成添加索引的sql
        if (!introspectedTable.getIndexes().isEmpty()) {
            for (IndexInfo index : introspectedTable.getIndexes()) {
                String indexSql = VStringUtil.format("    INDEX `{0}` (`{1}`) USING {2}", index.getName(), StringUtils.join(index.getColumnNames(), "`,`"),VStringUtil.stringHasValue(index.getType())?index.getType() : "BTREE");
                if (VStringUtil.stringHasValue(index.getComments())) {
                    indexSql = VStringUtil.format("{0} COMMENT {1}", indexSql, "'"+index.getComments()+"'" );
                }
                columnSql.add(indexSql);
            }
        }
        // 生成外键的sql：CONSTRAINT `fk_role_id` FOREIGN KEY (`role_id`) REFERENCES `org_role` (`id_`) ON DELETE CASCADE ON UPDATE RESTRICT
        if (!introspectedTable.getForeignKeys().isEmpty()) {
            String formatSql = "    CONSTRAINT `{0}` FOREIGN KEY (`{1}`) REFERENCES `{2}` (`{3}`) ON DELETE {4} ON UPDATE {5}";
            introspectedTable.getForeignKeys().forEach((foreignKeyName, foreignKeyInfos) -> {
                String foreignKeySql = VStringUtil.format(formatSql,
                        foreignKeyName,
                        foreignKeyInfos.stream().map(ForeignKeyInfo::getFkColumnName).collect(Collectors.joining("`,`")),
                        foreignKeyInfos.get(0).getPkTableName(),
                        foreignKeyInfos.stream().map(ForeignKeyInfo::getPkColumnName).collect(Collectors.joining("`,`")),
                        foreignKeyInfos.get(0).getDeleteRule().codeName(),
                        foreignKeyInfos.get(0).getUpdateRule().codeName());
                columnSql.add(foreignKeySql);
            });
        }
        ret.add(String.join(",\n", columnSql));
        if (this.databaseDDLDialects.equals(DatabaseDDLDialects.MYSQL)) {
            ret.add(") ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '"+introspectedTable.getRemarks(false)+"' ROW_FORMAT = Dynamic;");
        } else {
            ret.add(") COMMENT =  '"+introspectedTable.getRemarks(false)+"';");
        }
        return String.join("\n", ret);
    }

}
