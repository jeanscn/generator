package org.mybatis.generator.dbstrategy;

import com.vgosoft.core.db.query.DatabaseType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonStorageTypeResolverTest {

    @Test
    void shouldProvideTextStorageForEveryNamedDatabaseType() {
        for (DatabaseType databaseType : DatabaseType.values()) {
            if (databaseType == DatabaseType.OTHER) {
                assertThatThrownBy(() -> JsonStorageTypeResolver.resolve(databaseType, null))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("must be configured");
            } else {
                assertThat(JsonStorageTypeResolver.resolve(databaseType, null))
                        .as(databaseType.name())
                        .isNotBlank();
            }
        }
    }

    @Test
    void shouldAcceptValidatedOverrideForOtherAndRejectSqlFragments() {
        assertThat(JsonStorageTypeResolver.resolve(DatabaseType.OTHER, "CLOB")).isEqualTo("CLOB");
        assertThat(JsonStorageTypeResolver.resolve(DatabaseType.SQL_SERVER, "NVARCHAR(MAX)"))
                .isEqualTo("NVARCHAR(MAX)");
        assertThatThrownBy(() -> JsonStorageTypeResolver.resolve(DatabaseType.OTHER, "TEXT); DROP TABLE x;--"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsafe JSON storage SQL type");
    }
}
