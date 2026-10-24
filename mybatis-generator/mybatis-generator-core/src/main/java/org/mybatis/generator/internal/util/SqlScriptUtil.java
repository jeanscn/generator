package org.mybatis.generator.internal.util;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.custom.db.DatabaseDDLDialects;
import org.mybatis.generator.dbstrategy.DatabaseIntrospectorFactory;
import org.mybatis.generator.dbstrategy.DatabaseProfile;

import java.sql.JDBCType;
import java.util.List;
import java.util.Set;

import static com.vgosoft.core.db.enums.JDBCTypeTypeEnum.*;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * @author cen_c
 */
public class SqlScriptUtil {
    private static final Set<String> MYSQL_UNSUPPORTED_DEFAULT_TYPES = Set.of(
            "TINYBLOB", "BLOB", "MEDIUMBLOB", "LONGBLOB",
            "TINYTEXT", "TEXT", "MEDIUMTEXT", "LONGTEXT",
            "JSON",
            "GEOMETRY", "POINT", "LINESTRING", "POLYGON",
            "MULTIPOINT", "MULTILINESTRING", "MULTIPOLYGON", "GEOMETRYCOLLECTION"
    );
    private static final Set<String> SQL_SERVER_UNSUPPORTED_DEFAULT_TYPES = Set.of("TEXT", "NTEXT", "IMAGE");
    private static final Set<String> ORACLE_UNSUPPORTED_DEFAULT_TYPES = Set.of("BLOB", "CLOB", "NCLOB", "BFILE", "LONG", "LONG RAW");
    private static final Set<String> DB2_UNSUPPORTED_DEFAULT_TYPES = Set.of("BLOB", "CLOB", "DBCLOB", "XML");

    private SqlScriptUtil() {}


    public static String getColumnSql(List<IntrospectedColumn> columns, String actionKey, String databaseProductName) {
        StringBuilder sb = new StringBuilder();
        for (IntrospectedColumn col : columns) {
            /*
             * sql模板
             * 如：ADD COLUMN `col_varchar` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'varchar字段' AFTER `parent_id`;
             * */
            DatabaseProfile databaseProfile = DatabaseIntrospectorFactory.createProfile(databaseProductName, null);
            DatabaseDDLDialects databaseDialect = databaseProfile.ddlDialect();
            if (databaseDialect == null) {
                throw new IllegalArgumentException("Unsupported database dialect: " + databaseProductName);
            }
            String columnStatement = databaseDialect.getColumnModifyStatement();
            if (!stringHasValue(columnStatement)) {
                throw new IllegalArgumentException("Column modify statement is not configured for dialect: " + databaseDialect.name());
            }
            String character = " ";
            String typeName = normalizeTypeName(col.getActualTypeName());
            if ((isMysqlOrMariadbDatabase(databaseDialect))  && isCharacterType(resolveJdbcType(typeName))) {
                character = " CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci ";
            }
            String position = (isMysqlOrMariadbDatabase(databaseDialect))
                    && stringHasValue(col.getPosition()) ? " " + col.getPosition() : "";
            String onAddRow = VStringUtil.format(columnStatement,
                    actionKey,
                    databaseDialect.quoteIdentifier(col.getActualColumnName()),
                    typeName,
                    col.getSqlFragmentLength(typeName),
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
        return getSqlFragmentNotNull(introspectedColumn, databaseDialect, introspectedColumn.getActualTypeName());
    }

    public static String getSqlFragmentNotNull(IntrospectedColumn introspectedColumn, DatabaseDDLDialects databaseDialect, String actualTypeName) {
        JDBCType jdbcType = JDBCType.valueOf(introspectedColumn.getJdbcType());
        DatabaseDDLDialects dialect = databaseDialect == null ? DatabaseDDLDialects.OTHER : databaseDialect;
        String compatibleDefaultValue = isMysqlOrMariadbDatabase(dialect)
                ? introspectedColumn.getDefaultValue()
                : introspectedColumn.getDialectCompatibleDefaultValue();
        String normalizedTypeName = normalizeTypeName(actualTypeName);
        StringBuilder notNull = new StringBuilder();
        if (!introspectedColumn.isNullable()) {
            notNull.append("NOT NULL ");
            notNull.append(getDefaultValueString(introspectedColumn, jdbcType, compatibleDefaultValue, dialect, normalizedTypeName));
        } else {
            notNull.append("NULL ");
            if (introspectedColumn.getDefaultValue()!=null) {
                notNull.append(getDefaultValueString(introspectedColumn, jdbcType, compatibleDefaultValue, dialect, normalizedTypeName));
            }
        }
        return notNull.toString();
    }

    private static String getDefaultValueString(IntrospectedColumn introspectedColumn, JDBCType jdbcType,
                                                String defaultValue, DatabaseDDLDialects databaseDialect, String normalizedTypeName) {
        if (!supportsDefaultValue(normalizedTypeName, databaseDialect)) {
            return " ";
        }
        if (VStringUtil.stringHasValue(defaultValue)) {
            if (isSqlNullToken(defaultValue)) {
                return introspectedColumn.isNullable() ? " DEFAULT NULL " : " ";
            }
            String booleanExpression = resolveBooleanDefaultExpression(defaultValue, normalizedTypeName);
            if (booleanExpression != null) {
                return " DEFAULT " + booleanExpression + " ";
            }
            if (isCharacterType(jdbcType)) {
                if (defaultValue.startsWith("'") && defaultValue.endsWith("'")) {
                    return " DEFAULT " + defaultValue + " ";
                } else {
                    return " DEFAULT '" + databaseDialect.escapeSqlLiteral(defaultValue) + "' ";
                }
            } else if (isDateTimeType(jdbcType) || isDateType(jdbcType) || isTimeType(jdbcType)) {
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
            if (isTemporalJdbcType(jdbcType)) {
                return " DEFAULT " + resolveCurrentTemporalExpression(introspectedColumn, databaseDialect) + " ";
            }
            return " ";
        }
    }

    private static boolean isSqlNullToken(String defaultValue) {
        return "NULL".equalsIgnoreCase(defaultValue.trim());
    }

    private static boolean supportsDefaultValue(String typeName, DatabaseDDLDialects databaseDialect) {
        if (!VStringUtil.stringHasValue(typeName)) {
            return true;
        }
        Set<String> unsupportedTypes = switch (databaseDialect) {
            case MYSQL, MARIADB -> MYSQL_UNSUPPORTED_DEFAULT_TYPES;
            case SQL_SERVER, SQL_SERVER2005 -> SQL_SERVER_UNSUPPORTED_DEFAULT_TYPES;
            case ORACLE, DM -> ORACLE_UNSUPPORTED_DEFAULT_TYPES;
            case DB2 -> DB2_UNSUPPORTED_DEFAULT_TYPES;
            default -> Set.of();
        };
        if (unsupportedTypes.isEmpty()) {
            return true;
        }
        return unsupportedTypes.stream().noneMatch(type -> typeName.equals(type) || typeName.startsWith(type + " "));
    }

    private static String resolveBooleanDefaultExpression(String defaultValue, String normalizedTypeName) {
        if (!"BOOLEAN".equalsIgnoreCase(normalizedTypeName) && !"BOOL".equalsIgnoreCase(normalizedTypeName)) {
            return null;
        }
        String normalizedToken = normalizeDefaultToken(defaultValue);
        return switch (normalizedToken) {
            case "1", "TRUE", "T", "YES", "Y", "ON" -> "TRUE";
            case "0", "FALSE", "F", "NO", "N", "OFF" -> "FALSE";
            default -> null;
        };
    }

    private static String normalizeDefaultToken(String defaultValue) {
        String token = defaultValue.trim();
        if (token.startsWith("(") && token.endsWith(")") && token.length() > 2) {
            token = token.substring(1, token.length() - 1).trim();
        }
        if (token.startsWith("'") && token.endsWith("'") && token.length() > 1) {
            token = token.substring(1, token.length() - 1).trim();
        }
        if (token.endsWith("()") && token.length() > 2) {
            token = token.substring(0, token.length() - 2).trim();
        }
        return token.toUpperCase();
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

        return switch (normalized) {
            case "NOW", "CURRENT_TIMESTAMP", "LOCALTIMESTAMP", "CURRENT_DATETIME" ->
                    databaseDialect.currentDateTimeExpression();
            case "CURDATE", "CURRENT_DATE", "TODAY" -> databaseDialect.currentDateExpression();
            case "CURTIME", "CURRENT_TIME" -> databaseDialect.currentTimeExpression();
            default -> normalized.startsWith("CURRENT_") ? token : null;
        };
    }

    private static String resolveCurrentTemporalExpression(IntrospectedColumn introspectedColumn, DatabaseDDLDialects databaseDialect) {
        JDBCType jdbcType = JDBCType.valueOf(introspectedColumn.getJdbcType());
        return switch (jdbcType) {
            case DATE -> databaseDialect.currentDateExpression();
            case TIME -> databaseDialect.currentTimeExpression();
            case TIME_WITH_TIMEZONE -> databaseDialect.currentTimeExpression();
            case TIMESTAMP_WITH_TIMEZONE -> databaseDialect.currentDateTimeExpression();
            default -> databaseDialect.currentDateTimeExpression();
        };
    }

    private static boolean isTemporalJdbcType(JDBCType jdbcType) {
        return isDateTimeType(jdbcType) || isDateType(jdbcType) || isTimeType(jdbcType)
                || jdbcType == JDBCType.TIMESTAMP_WITH_TIMEZONE || jdbcType == JDBCType.TIME_WITH_TIMEZONE;
    }

    public static boolean isMysqlOrMariadbDatabase(DatabaseDDLDialects databaseDialect) {
        return databaseDialect.equals(DatabaseDDLDialects.MYSQL) || databaseDialect.equals(DatabaseDDLDialects.MARIADB);
    }

}
