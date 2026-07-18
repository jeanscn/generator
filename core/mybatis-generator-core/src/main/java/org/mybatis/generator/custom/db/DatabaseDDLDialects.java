package org.mybatis.generator.custom.db;

import com.vgosoft.core.db.query.DatabaseType;
import lombok.Getter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author cen_c
 */

@Getter
public enum DatabaseDDLDialects {

    /**
     * 数据库方言对应的ddl语句模板
     * {0}操作类型 ADD|MODIFY|CHANGE|DROP
     * {1}列名
     * {2}列类型
     * {3}列长度
     * {4}字符集
     * {5}非空
     * {6}备注
     * {7}位置
     */

    MYSQL("{0} COLUMN {1} {2}{3}{4}{5}COMMENT ''{6}''{7}", "    {0} {1}{2}{3}{4}COMMENT ''{5}''", DatabaseType.MYSQL),
    MARIADB("{0} COLUMN {1} {2}{3}{4}{5}COMMENT ''{6}''{7}", "    {0} {1}{2}{3}{4}COMMENT ''{5}''", DatabaseType.MARIADB),
    ORACLE("{0} ({1} {2}{3}{5})", "    {0} {1}{2}{4}", DatabaseType.ORACLE),
    /**
     * db2数据库
     */
    DB2("{0} COLUMN {1} SET DATA TYPE {2}{3}", "    {0} {1}{2}{4}", DatabaseType.DB2),
    H2("{0} COLUMN {1} {2}{3}{4}", "    {0} {1}{2}{3}{4}COMMENT ''{5}''", DatabaseType.H2),
    HSQL("{0} COLUMN {1} {2}{3}{4}", "    {0} {1}{2}{3}{4}", DatabaseType.HSQL),
    SQLITE("", "    {0} {1}{2}{4}", DatabaseType.SQLITE),
    POSTGRE_SQL("{0} COLUMN {1} TYPE {2}{3}", "    {0} {1}{2}{4}", DatabaseType.POSTGRE_SQL),
    SQL_SERVER2005("{0} {1} {2}{3}{5}", "    {0} {1}{2}{4}", DatabaseType.SQL_SERVER2005, "SQLSERVER2005"),
    SQL_SERVER("{0} {1} {2}{3}{5}", "    {0} {1}{2}{4}", DatabaseType.SQL_SERVER, "SQLSERVER"),
    DM("{0} COLUMN {1} {2}{3}{4}", "    {0} {1}{2}{3}{4}", DatabaseType.DM),
    HIGHGO("{0} COLUMN {1} TYPE {2}{3}", "    {0} {1}{2}{4}", DatabaseType.HIGHGO),
    XU_GU("{0} COLUMN {1} {2}{3}{4}", "    {0} {1}{2}{4}", DatabaseType.XU_GU),
    KINGBASE_ES("{0} COLUMN {1} TYPE {2}{3}", "    {0} {1}{2}{4}", DatabaseType.KINGBASE_ES),
    PHOENIX("{0} COLUMN {1} {2}{3}{4}", "    {0} {1}{2}{4}", DatabaseType.PHOENIX),
    CACHEDB("{0} COLUMN {1} {2}{3}{4}", "    {0} {1}{2}{4}", DatabaseType.CACHEDB),
    OTHER("{0} COLUMN {1} {2}{3}{4}", "    {0} {1}{2}{4}", DatabaseType.OTHER);

    private static final Map<String, DatabaseDDLDialects> DIALECTS_BY_KEY = createDialectsByKey();

    private final String columnModifyStatement;
    private final String createStatement;
    private final Set<String> mappingKeys;

    DatabaseDDLDialects(String columnModifyStatement, String createStatement, DatabaseType databaseType,
                        String... aliases) {
        this.columnModifyStatement = columnModifyStatement;
        this.createStatement = createStatement;
        Set<String> keys = new LinkedHashSet<>();
        keys.add(databaseType.name());
        keys.add(normalizeKey(databaseType.getName()));
        for (String alias : aliases) {
            keys.add(normalizeKey(alias));
        }
        this.mappingKeys = Collections.unmodifiableSet(keys);
    }

    public static DatabaseDDLDialects getDatabaseDialect(String database) {
        if (database == null) {
            return null;
        }
        return DIALECTS_BY_KEY.get(normalizeKey(database));
    }

    public static DatabaseDDLDialects getDatabaseDialect(DatabaseType databaseType) {
        if (databaseType == null) {
            return null;
        }
        return DIALECTS_BY_KEY.get(databaseType.name());
    }

    private static Map<String, DatabaseDDLDialects> createDialectsByKey() {
        Map<String, DatabaseDDLDialects> dialectsByKey = new LinkedHashMap<>();
        for (DatabaseDDLDialects dialect : values()) {
            for (String key : dialect.mappingKeys) {
                dialectsByKey.put(key, dialect);
            }
        }
        return Collections.unmodifiableMap(dialectsByKey);
    }

    private static String normalizeKey(String key) {
        return key == null ? null : key.trim().toUpperCase().replace(' ', '_').replace('-', '_');
    }

    public String quoteIdentifier(String identifier) {
        if (identifier == null) {
            return "";
        }
        return switch (this) {
            case SQL_SERVER, SQL_SERVER2005 -> "[" + identifier + "]";
            case ORACLE, DB2, POSTGRE_SQL, HIGHGO, KINGBASE_ES, XU_GU, DM, PHOENIX, CACHEDB -> "\"" + identifier + "\"";
            default -> "`" + identifier + "`";
        };
    }

    public String escapeSqlLiteral(String value) {
        return value == null ? "" : value.replace("'", "''");
    }

    public boolean supportsCreateTableIfNotExists() {
        return switch (this) {
            case MYSQL, MARIADB, H2, HSQL, SQLITE -> true;
            default -> false;
        };
    }

    public boolean supportsInlineColumnComment() {
        return switch (this) {
            case MYSQL, MARIADB, H2 -> true;
            default -> false;
        };
    }

    public boolean supportsInlineTableComment() {
        return switch (this) {
            case MYSQL, MARIADB, H2 -> true;
            default -> false;
        };
    }

    public boolean supportsCommentOnStatement() {
        return switch (this) {
            case ORACLE, DB2, POSTGRE_SQL, HIGHGO, KINGBASE_ES, DM, XU_GU -> true;
            default -> false;
        };
    }

    public boolean supportsIndexUsingClause() {
        return switch (this) {
            case MYSQL, MARIADB -> true;
            default -> false;
        };
    }

    public boolean supportsInlineIndexDefinition() {
        return switch (this) {
            case MYSQL, MARIADB -> true;
            default -> false;
        };
    }

    public boolean supportsIndexCommentClause() {
        return switch (this) {
            case MYSQL, MARIADB -> true;
            default -> false;
        };
    }

    public String createTableSuffix(String tableRemark) {
        String safeRemark = escapeSqlLiteral(tableRemark);
        return switch (this) {
            case MYSQL, MARIADB -> " ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '" + safeRemark + "' ROW_FORMAT = Dynamic";
            case H2 -> " COMMENT '" + safeRemark + "'";
            default -> "";
        };
    }

    public String currentDateTimeExpression() {
        return "CURRENT_TIMESTAMP";
    }

    public String currentDateExpression() {
        return switch (this) {
            case MYSQL, MARIADB -> "(CURRENT_DATE)";
            case SQL_SERVER, SQL_SERVER2005 -> "CAST(CURRENT_TIMESTAMP AS DATE)";
            default -> "CURRENT_DATE";
        };
    }

    public String currentTimeExpression() {
        return switch (this) {
            case MYSQL, MARIADB -> "(CURRENT_TIME)";
            case SQL_SERVER, SQL_SERVER2005 -> "CAST(CURRENT_TIMESTAMP AS TIME)";
            case ORACLE -> "CURRENT_TIMESTAMP";
            default -> "CURRENT_TIME";
        };
    }
}
