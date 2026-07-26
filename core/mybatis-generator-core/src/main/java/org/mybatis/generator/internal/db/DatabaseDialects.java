package org.mybatis.generator.internal.db;

import com.vgosoft.core.db.query.DatabaseType;
import lombok.Getter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Typesafe enum of known database dialects.
 *
 * @author Jeff Butler
 */
@Getter
public enum DatabaseDialects {

    /**
     * DB2 database dialect.
     */
    MYSQL("SELECT LAST_INSERT_ID()", DatabaseType.MYSQL),
    MARIADB("SELECT LAST_INSERT_ID()", DatabaseType.MARIADB),
    ORACLE(null, DatabaseType.ORACLE),
    DB2("VALUES IDENTITY_VAL_LOCAL()", DatabaseType.DB2),
    H2("CALL IDENTITY()", DatabaseType.H2),
    HSQL("CALL IDENTITY()", DatabaseType.HSQL, "HSQLDB"),
    SQLITE("SELECT last_insert_rowid()", DatabaseType.SQLITE),
    POSTGRE_SQL("SELECT LASTVAL()", DatabaseType.POSTGRE_SQL),
    SQL_SERVER2005("SELECT SCOPE_IDENTITY()", DatabaseType.SQL_SERVER2005, "SQLSERVER2005"),
    SQL_SERVER("SELECT SCOPE_IDENTITY()", DatabaseType.SQL_SERVER, "SQLSERVER"),
    DM(null, DatabaseType.DM),
    HIGHGO("SELECT LASTVAL()", DatabaseType.HIGHGO),
    XU_GU(null, DatabaseType.XU_GU),
    KINGBASE_ES("SELECT LASTVAL()", DatabaseType.KINGBASE_ES),
    PHOENIX(null, DatabaseType.PHOENIX),
    CACHEDB(null, DatabaseType.CACHEDB),
    OTHER(null, DatabaseType.OTHER);

    private static final Map<String, DatabaseDialects> DIALECTS_BY_KEY = createDialectsByKey();

    private final String identityRetrievalStatement;
    private final Set<String> mappingKeys;

    DatabaseDialects(String identityRetrievalStatement, DatabaseType databaseType, String... aliases) {
        this.identityRetrievalStatement = identityRetrievalStatement;
        Set<String> keys = new LinkedHashSet<>();
        keys.add(databaseType.name());
        keys.add(normalizeKey(databaseType.getCode()));
        for (String mappingKey : aliases) {
            keys.add(normalizeKey(mappingKey));
        }
        this.mappingKeys = Collections.unmodifiableSet(keys);
    }

    /**
     * Gets the database dialect.
     *
     * @param database
     *            the database
     * @return the database dialect for the selected database. May return null if there is no known dialect for the
     *         selected db
     */
    public static DatabaseDialects getDatabaseDialect(String database) {
        if (database == null) {
            return null;
        }
        DatabaseDialects dialect = DIALECTS_BY_KEY.get(normalizeKey(database));
        return dialect == null || dialect.identityRetrievalStatement == null ? null : dialect;
    }

    public static DatabaseDialects getDatabaseDialect(DatabaseType databaseType) {
        if (databaseType == null) {
            return null;
        }
        DatabaseDialects dialect = DIALECTS_BY_KEY.get(databaseType.name());
        return dialect == null || dialect.identityRetrievalStatement == null ? null : dialect;
    }

    private static Map<String, DatabaseDialects> createDialectsByKey() {
        Map<String, DatabaseDialects> dialectsByKey = new LinkedHashMap<>();
        for (DatabaseDialects dialect : values()) {
            for (String key : dialect.mappingKeys) {
                dialectsByKey.put(key, dialect);
            }
        }
        return Collections.unmodifiableMap(dialectsByKey);
    }

    private static String normalizeKey(String key) {
        return key == null ? null : key.trim().toUpperCase().replace(' ', '_').replace('-', '_');
    }
}
