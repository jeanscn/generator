package org.mybatis.generator.codegen.mybatis3.sqlschema;

import com.vgosoft.core.constant.Empty;
import com.vgosoft.core.db.enums.JDBCTypeTypeEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IndexInfo;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.codegen.mybatis3.sqlschema.AbstractSqlScriptGenerator;
import org.mybatis.generator.config.SqlSchemaGeneratorConfiguration;
import org.mybatis.generator.custom.db.DatabaseDDLDialects;

import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.List;

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

        SqlSchemaGeneratorConfiguration sqlSchemaGeneratorConfiguration = this.introspectedTable.getTableConfiguration().getSqlSchemaGeneratorConfiguration();
        String tableName = introspectedTable.getTableConfiguration().getTableName();
        String COLUMN_STATEMENT = databaseDDLDialects.getCreateStatement();

        List<String> columnSql = new ArrayList<>();
        ret.add("-- ----------------------------");
        ret.add(VStringUtil.format("-- Table structure for {0}", tableName));
        ret.add("-- ----------------------------");
        ret.add("");
        ret.add(VStringUtil.format("CREATE TABLE IF NOT EXISTS `{0}`  (", tableName));

        for (IntrospectedColumn col : this.introspectedTable.getAllColumns()) {

            String character = Empty.EMPTY_STRING;
            if (this.databaseDDLDialects.equals(DatabaseDDLDialects.MYSQL) && !VStringUtil.toCamelCase(col.getActualTypeName()).equals("JSON")) {
                character = JDBCTypeTypeEnum.getJDBCTypeType(JDBCType.valueOf(col.getJdbcType())).equals(JDBCTypeTypeEnum.CHARACTER)?" CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci ":" ";
            }

            String rowSql = VStringUtil.format(COLUMN_STATEMENT,
                    col.getActualColumnName(),
                    VStringUtil.toCamelCase(col.getActualTypeName()),
                    col.getSqlFragmentLength(),
                    character,
                    col.getSqlFragmentNotNull(),
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
                String indexSql = VStringUtil.format("    INDEX `{0}` (`{1}`) USING BTREE", index.getName(), StringUtils.join(index.getColumnNames(), "`,`"));
                if (VStringUtil.stringHasValue(index.getComments())) {
                    indexSql = VStringUtil.format("    INDEX `{0}` (`{1}`) USING BTREE COMMENT '{2}'", index.getName(), StringUtils.join(index.getColumnNames(), "`,`"), index.getComments());
                }
                columnSql.add(indexSql);
            }
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
