package org.mybatis.generator.internal.util;

import com.vgosoft.core.db.enums.JDBCTypeTypeEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.custom.db.DatabaseDDLDialects;
import org.mybatis.generator.dbstrategy.DatabaseIntrospectorFactory;

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
            DatabaseDDLDialects databaseDialect = DatabaseIntrospectorFactory
                    .createProfile(databaseProductName, null)
                    .ddlDialect();
            if (databaseDialect == null) {
                throw new IllegalArgumentException("Unsupported database dialect: " + databaseProductName);
            }
            String columnStatement = databaseDialect.getColumnModifyStatement();
            if (!stringHasValue(columnStatement)) {
                throw new IllegalArgumentException("Column modify statement is not configured for dialect: " + databaseDialect.name());
            }
            String character = " ";
            if ((isMysqlOrMariadbDatabase(databaseDialect))
                    && JDBCTypeTypeEnum.getJdbcTypeType(JDBCType.valueOf(col.getJdbcType())).equals(JDBCTypeTypeEnum.CHARACTER)) {
                character = " CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci ";
            }
            String position = (isMysqlOrMariadbDatabase(databaseDialect))
                    && stringHasValue(col.getPosition()) ? " " + col.getPosition() : "";
            String onAddRow = VStringUtil.format(columnStatement,
                    actionKey,
                    databaseDialect.quoteIdentifier(col.getActualColumnName()),
                    col.getActualTypeName(),
                    col.getSqlFragmentLength(),
                    character,
                    SqlScriptUtil.getSqlFragmentNotNull(col, databaseDialect),
                    databaseDialect.escapeSqlLiteral(col.getRemarks(false)),
                    position);
            if (!sb.isEmpty()) {
                sb.append(",").append("\n");
            }
            sb.append(onAddRow);
        }
        return sb.toString();
    }

    public static String getSqlFragmentNotNull(IntrospectedColumn introspectedColumn) {
        return getSqlFragmentNotNull(introspectedColumn, DatabaseDDLDialects.OTHER);
    }

    public static String getSqlFragmentNotNull(IntrospectedColumn introspectedColumn, DatabaseDDLDialects databaseDialect) {
        JDBCTypeTypeEnum jdbcTypeType = JDBCTypeTypeEnum.getJdbcTypeType(JDBCType.valueOf(introspectedColumn.getJdbcType()));
        DatabaseDDLDialects dialect = databaseDialect == null ? DatabaseDDLDialects.OTHER : databaseDialect;
        String compatibleDefaultValue = isMysqlOrMariadbDatabase(dialect)
                ? introspectedColumn.getDefaultValue()
                : introspectedColumn.getDialectCompatibleDefaultValue();
        StringBuilder notNull = new StringBuilder();
        if (!introspectedColumn.isNullable()) {
            notNull.append("NOT NULL ");
            notNull.append(getDefaultValueString(jdbcTypeType, compatibleDefaultValue, dialect));
        } else {
            notNull.append("NULL ");
            if (introspectedColumn.getDefaultValue()!=null) {
                notNull.append(getDefaultValueString(jdbcTypeType, compatibleDefaultValue, dialect));
            }
        }
        return notNull.toString();
    }

    private static String getDefaultValueString(JDBCTypeTypeEnum jdbcTypeType, String defaultValue, DatabaseDDLDialects databaseDialect) {
        if (VStringUtil.stringHasValue(defaultValue)) {
            if (jdbcTypeType.equals(JDBCTypeTypeEnum.CHARACTER)) {
                if (defaultValue.startsWith("'") && defaultValue.endsWith("'")) {
                    return " DEFAULT " + defaultValue + " ";
                } else {
                    return " DEFAULT '" + databaseDialect.escapeSqlLiteral(defaultValue) + "' ";
                }
            } else if (jdbcTypeType.equals(JDBCTypeTypeEnum.DATETIME)) {
                String normalizedExpr = resolveCurrentDateTimeExpression(defaultValue, databaseDialect);
                if (normalizedExpr != null) {
                    return " DEFAULT " + normalizedExpr + " ";
                }
                if (defaultValue.startsWith("'") && defaultValue.endsWith("'")) {
                    return " DEFAULT " + defaultValue + " ";
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
                return " DEFAULT " + databaseDialect.currentDateTimeExpression() + " ";
            } else if (jdbcTypeType.equals(JDBCTypeTypeEnum.NUMERIC)) {
                return " DEFAULT 0 ";
            } else {
                return " ";
            }
        }
    }

    private static String resolveCurrentDateTimeExpression(String defaultValue, DatabaseDDLDialects databaseDialect) {
        if (!VStringUtil.stringHasValue(defaultValue)) {
            return null;
        }
        String trimmed = defaultValue.trim();
        if (trimmed.startsWith("(") && trimmed.endsWith(")") && trimmed.length() > 2) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }
        String token = trimmed;
        if (token.startsWith("'") && token.endsWith("'") && token.length() > 1) {
            token = token.substring(1, token.length() - 1).trim();
        }
        String normalized = token.toUpperCase();
        if (normalized.endsWith("()")) {
            normalized = normalized.substring(0, normalized.length() - 2);
        }

        if ("NOW".equals(normalized) || "CURRENT_TIMESTAMP".equals(normalized) || "LOCALTIMESTAMP".equals(normalized)
                || "CURRENT_DATETIME".equals(normalized)) {
            return databaseDialect.currentDateTimeExpression();
        }
        if ("CURDATE".equals(normalized) || "CURRENT_DATE".equals(normalized) || "TODAY".equals(normalized)) {
            return databaseDialect.currentDateExpression();
        }
        if ("CURTIME".equals(normalized) || "CURRENT_TIME".equals(normalized)) {
            return databaseDialect.currentTimeExpression();
        }
        return normalized.startsWith("CURRENT_") ? token : null;
    }

    public static boolean isMysqlOrMariadbDatabase(DatabaseDDLDialects databaseDialect) {
        return databaseDialect.equals(DatabaseDDLDialects.MYSQL) || databaseDialect.equals(DatabaseDDLDialects.MARIADB);
    }

}
