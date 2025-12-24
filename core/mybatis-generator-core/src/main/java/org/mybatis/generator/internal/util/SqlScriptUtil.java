package org.mybatis.generator.internal.util;

import com.vgosoft.core.constant.enums.db.DDLDefaultValueEnum;
import com.vgosoft.core.db.enums.JDBCTypeTypeEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.custom.db.DatabaseDDLDialects;

import java.sql.JDBCType;
import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * @author cen_c
 */
public class SqlScriptUtil {

    private SqlScriptUtil() {}


    public static String getColumnSql(List<IntrospectedColumn> columns, String actionKey, String databaseProductName) {
        StringBuilder sb = new StringBuilder();
        for (IntrospectedColumn col : columns) {
            /*
             * sql模板
             * 如：ADD COLUMN `col_varchar` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'varchar字段' AFTER `parent_id`;
             * */
            DatabaseDDLDialects databaseDialect = DatabaseDDLDialects.getDatabaseDialect(databaseProductName);
            String columnStatement = databaseDialect.getColumnModifyStatement();
            String character = JDBCTypeTypeEnum.getJdbcTypeType(JDBCType.valueOf(col.getJdbcType())).equals(JDBCTypeTypeEnum.CHARACTER) ? " CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci " : " ";
            String position = stringHasValue(col.getPosition()) ? " " + col.getPosition() : "";
            String onAddRow = VStringUtil.format(columnStatement,
                    actionKey,
                    col.getActualColumnName(),
                    col.getActualTypeName(),
                    col.getSqlFragmentLength(),
                    character,
                    SqlScriptUtil.getSqlFragmentNotNull(col),
                    col.getRemarks(false),
                    position);
            if (sb.length()>0) {
                sb.append(",").append("\n");
            }
            sb.append(onAddRow);
        }
        return sb.toString();
    }

    public static String getSqlFragmentNotNull(IntrospectedColumn introspectedColumn) {
        JDBCTypeTypeEnum jdbcTypeType = JDBCTypeTypeEnum.getJdbcTypeType(JDBCType.valueOf(introspectedColumn.getJdbcType()));
        StringBuilder notNull = new StringBuilder();
        if (!introspectedColumn.isNullable()) {
            notNull.append("NOT NULL ");
            notNull.append(getDefaultValueString(jdbcTypeType, introspectedColumn.getDefaultValue()));
        } else {
            notNull.append("NULL ");
            if (introspectedColumn.getDefaultValue()!=null) {
                notNull.append(getDefaultValueString(jdbcTypeType, introspectedColumn.getDefaultValue()));
            }
        }
        return notNull.toString();
    }

    private static String getDefaultValueString(JDBCTypeTypeEnum jdbcTypeType, String defaultValue) {
        if (VStringUtil.stringHasValue(defaultValue)) {
            if (jdbcTypeType.equals(JDBCTypeTypeEnum.CHARACTER)) {
                if (defaultValue.startsWith("'") && defaultValue.endsWith("'")) {
                    return " DEFAULT " + defaultValue + " ";
                } else {
                    return " DEFAULT '" + defaultValue + "' ";
                }
            } else if (jdbcTypeType.equals(JDBCTypeTypeEnum.DATETIME)) {
                if (defaultValue.startsWith("CURRENT_")) {
                    return " DEFAULT (" + defaultValue + ") ";
                } else if (defaultValue.startsWith("'now")) {
                    return " DEFAULT "+ DDLDefaultValueEnum.CURRENT_DATETIME_EXPR.code() +" ";
                } else if (defaultValue.startsWith("'curdate")) {
                    return " DEFAULT "+ DDLDefaultValueEnum.CURRENT_DATE_EXPR.code() +" ";
                } else if (defaultValue.startsWith("'curtime")) {
                    return " DEFAULT "+ DDLDefaultValueEnum.CURRENT_TIME_EXPR.code() +" ";
                } else {
                    return " DEFAULT '" + defaultValue + "' ";
                }
            } else {
                return " DEFAULT " + defaultValue + " ";
            }
        } else {
            if (jdbcTypeType.equals(JDBCTypeTypeEnum.CHARACTER)) {
                return " DEFAULT '' ";
            } else if (jdbcTypeType.equals(JDBCTypeTypeEnum.DATETIME)) {
                return " DEFAULT " + DDLDefaultValueEnum.CURRENT_DATETIME_EXPR.code() + " ";
            } else if (jdbcTypeType.equals(JDBCTypeTypeEnum.NUMERIC)) {
                return " DEFAULT 0 ";
            } else {
                return " ";
            }
        }
    }

}
