package org.mybatis.generator.internal.util;

import com.vgosoft.core.db.enums.JDBCTypeTypeEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.custom.db.DatabaseDDLDialects;
import org.mybatis.generator.dbstrategy.DatabaseIntrospectorFactory;
import org.mybatis.generator.dbstrategy.DatabaseProfile;

import java.sql.JDBCType;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

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
                    col.getSqlFragmentLength(col.getActualColumnName()),
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
            notNull.append(getDefaultValueString(introspectedColumn, jdbcTypeType, compatibleDefaultValue, dialect));
        } else {
            notNull.append("NULL ");
            if (introspectedColumn.getDefaultValue()!=null) {
                notNull.append(getDefaultValueString(introspectedColumn, jdbcTypeType, compatibleDefaultValue, dialect));
            }
        }
        return notNull.toString();
    }

    private static String getDefaultValueString(IntrospectedColumn introspectedColumn, JDBCTypeTypeEnum jdbcTypeType,
                                                String defaultValue, DatabaseDDLDialects databaseDialect) {
        if (!supportsDefaultValue(introspectedColumn, databaseDialect)) {
            return " ";
        }
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
                return " DEFAULT " + resolveCurrentTemporalExpression(introspectedColumn, databaseDialect) + " ";
            } else if (jdbcTypeType.equals(JDBCTypeTypeEnum.NUMERIC)) {
                return " DEFAULT 0 ";
            } else {
                return " ";
            }
        }
    }

    private static boolean supportsDefaultValue(IntrospectedColumn introspectedColumn, DatabaseDDLDialects databaseDialect) {
        String typeName = normalizeTypeName(introspectedColumn.getActualTypeName());
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

    private static String normalizeTypeName(String actualTypeName) {
        if (!VStringUtil.stringHasValue(actualTypeName)) {
            return "";
        }
        String normalized = actualTypeName
                .toUpperCase(Locale.ROOT)
                .replace('`', ' ')
                .replace('"', ' ')
                .trim()
                .replaceAll("\\s+", " ");
        int leftParen = normalized.indexOf('(');
        if (leftParen >= 0) {
            normalized = normalized.substring(0, leftParen).trim();
        }
        return Arrays.stream(normalized.split(" "))
                .filter(VStringUtil::stringHasValue)
                .collect(Collectors.joining(" "));
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
            case TIME, TIME_WITH_TIMEZONE -> databaseDialect.currentTimeExpression();
            default -> databaseDialect.currentDateTimeExpression();
        };
    }

    public static boolean isMysqlOrMariadbDatabase(DatabaseDDLDialects databaseDialect) {
        return databaseDialect.equals(DatabaseDDLDialects.MYSQL) || databaseDialect.equals(DatabaseDDLDialects.MARIADB);
    }

}
