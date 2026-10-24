package org.mybatis.generator.dbstrategy;

import com.vgosoft.core.db.query.DatabaseType;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Resolves the portable text storage type used for JSON columns in generated DDL.
 * @author cen_c
 */
public final class JsonStorageTypeResolver {
    private static final Pattern SAFE_SQL_TYPE = Pattern.compile(
            "(?i)[A-Z][A-Z0-9_]*(?:\\s*\\(\\s*(?:MAX|[0-9]+)\\s*\\))?");
    private static final Map<DatabaseType, String> DEFAULT_TYPES = Map.ofEntries(
            Map.entry(DatabaseType.MYSQL, "LONGTEXT"),
            Map.entry(DatabaseType.MARIADB, "LONGTEXT"),
            Map.entry(DatabaseType.TIDB, "LONGTEXT"),
            Map.entry(DatabaseType.POLARDB, "LONGTEXT"),
            Map.entry(DatabaseType.TDSQL, "LONGTEXT"),
            Map.entry(DatabaseType.ORACLE, "CLOB"),
            Map.entry(DatabaseType.POSTGRE_SQL, "TEXT"),
            Map.entry(DatabaseType.HIGHGO, "TEXT"),
            Map.entry(DatabaseType.KINGBASE_ES, "TEXT"),
            Map.entry(DatabaseType.GAUSS_DB, "TEXT"),
            Map.entry(DatabaseType.UXDB, "TEXT"),
            Map.entry(DatabaseType.SQL_SERVER, "NVARCHAR(MAX)"),
            Map.entry(DatabaseType.SQL_SERVER2005, "NVARCHAR(MAX)"),
            Map.entry(DatabaseType.DM, "CLOB"),
            Map.entry(DatabaseType.XU_GU, "CLOB"),
            Map.entry(DatabaseType.GBASE, "CLOB"),
            Map.entry(DatabaseType.DB2, "CLOB"),
            Map.entry(DatabaseType.H2, "CLOB"),
            Map.entry(DatabaseType.HSQL, "CLOB"),
            Map.entry(DatabaseType.SQLITE, "TEXT"),
            Map.entry(DatabaseType.PHOENIX, "VARCHAR"),
            Map.entry(DatabaseType.CACHEDB, "CLOB"),
            Map.entry(DatabaseType.INFORMIX, "CLOB"),
            Map.entry(DatabaseType.CLOUDSCAPE, "CLOB"),
            Map.entry(DatabaseType.SYBASE, "TEXT"));

    private JsonStorageTypeResolver() {
    }

    public static String resolve(DatabaseType databaseType, String configuredType) {
        if (configuredType != null && !configuredType.isBlank()) {
            String normalized = configuredType.trim();
            if (!SAFE_SQL_TYPE.matcher(normalized).matches()) {
                throw new IllegalArgumentException("Unsafe JSON storage SQL type: " + configuredType);
            }
            return normalized;
        }
        String type = DEFAULT_TYPES.get(databaseType);
        if (type == null) {
            throw new IllegalArgumentException("JSON text storage type must be configured for database type: "
                    + databaseType);
        }
        return type;
    }
}
