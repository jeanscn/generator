package org.mybatis.generator.codegen;

import com.vgosoft.core.constant.Empty;
import com.vgosoft.core.db.enums.JDBCTypeTypeEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.config.SqlSchemaGeneratorConfiguration;
import org.mybatis.generator.custom.db.DatabaseDDLDialects;

import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * sql脚本生成器
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-06-21 18:53
 * @version 3.0
 */
public class SqlScriptGenerator extends AbstractGenerator {

    private final IntrospectedTable introspectedTable;
    private final DatabaseDDLDialects databaseDDLDialects;

    public SqlScriptGenerator(IntrospectedTable introspectedTable, DatabaseDDLDialects databaseDDLDialects) {
        this.introspectedTable = introspectedTable;
        this.databaseDDLDialects = databaseDDLDialects;
    }

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

        StringBuilder not_null = new StringBuilder();
        StringBuilder sb_length = new StringBuilder();
        for (IntrospectedColumn col : this.introspectedTable.getAllColumns()) {
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

            String character = Empty.EMPTY_STRING;
            if (this.databaseDDLDialects.equals(DatabaseDDLDialects.MYSQL)) {
                character = JDBCTypeTypeEnum.getJDBCTypeType(JDBCType.valueOf(col.getJdbcType())).equals(JDBCTypeTypeEnum.CHARACTER)?" CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci ":" ";
            }
            String rowSql = VStringUtil.format(COLUMN_STATEMENT,
                    col.getActualColumnName(),
                    col.getActualTypeName(),
                    sb_length.toString(),
                    character,
                    not_null.toString(),
                    col.getRemarks());
            columnSql.add(rowSql);
        }
        for (IntrospectedColumn primaryKeyColumn : introspectedTable.getPrimaryKeyColumns()) {
            if (this.databaseDDLDialects.equals(DatabaseDDLDialects.MYSQL)) {
                columnSql.add("    PRIMARY KEY (`"+primaryKeyColumn.getActualColumnName()+"`) USING BTREE");
            }else{
                columnSql.add("    PRIMARY KEY (`"+primaryKeyColumn.getActualColumnName()+"`)");
            }
        }
        ret.add(String.join(",\n", columnSql));
        if (this.databaseDDLDialects.equals(DatabaseDDLDialects.MYSQL)) {
            ret.add(") ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '"+introspectedTable.getRemarks()+"' ROW_FORMAT = Dynamic;");
        } else {
            ret.add(") COMMENT =  '"+introspectedTable.getRemarks()+"';");
        }
        return String.join("\n", ret);
    }

}
