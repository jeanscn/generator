package org.mybatis.generator.internal.db;

import com.vgosoft.core.db.model.metadata.Column;
import com.vgosoft.core.db.model.metadata.ForeignKey;
import com.vgosoft.core.db.model.metadata.Index;
import com.vgosoft.core.db.model.metadata.PrimaryKey;
import org.junit.jupiter.api.Test;
import org.mybatis.generator.SqlScriptRunner;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseQueryMetadataAdapterTest {
    private static final String DRIVER = "org.hsqldb.jdbcDriver";
    private static final String URL = "jdbc:hsqldb:mem:metaadapter";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    @Test
    void shouldMatchJdbcMetadataForColumnsPrimaryKeysForeignKeysAndIndexes() throws Exception {
        createDatabase();
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            DatabaseMetaData metaData = connection.getMetaData();
            DatabaseQueryMetadataAdapter adapter = new DatabaseQueryMetadataAdapter(connection);
            String tableName = "METAADAPTERCHILD";

            assertThat(columnNames(adapter.getTableColumns(tableName)))
                    .isEqualTo(jdbcColumnNames(metaData, tableName));
            assertThat(primaryKeyNames(adapter.getPrimaryKeys(tableName)))
                    .isEqualTo(jdbcPrimaryKeyNames(metaData, tableName));
            assertThat(foreignKeyColumns(adapter.getForeignKeys(tableName)))
                    .isEqualTo(jdbcForeignKeyColumns(metaData, tableName));
            assertThat(indexColumns(adapter.getIndexes(tableName)))
                    .isEqualTo(jdbcIndexColumns(metaData, tableName));
        }
    }

    private void createDatabase() throws Exception {
        SqlScriptRunner scriptRunner = new SqlScriptRunner(
                getClass().getResourceAsStream("/scripts/CreateDB.sql"),
                DRIVER,
                URL,
                USER,
                PASSWORD);
        scriptRunner.executeScript();
    }

    private List<String> columnNames(List<? extends Column> columns) {
        return columns.stream()
                .map(Column::getColumnName)
                .collect(Collectors.toList());
    }

    private List<String> primaryKeyNames(List<? extends PrimaryKey> primaryKeys) {
        return primaryKeys.stream()
                .sorted((left, right) -> parseShort(left.getKeySeq()) - parseShort(right.getKeySeq()))
                .map(PrimaryKey::getColumnName)
                .collect(Collectors.toList());
    }

    private Map<String, List<String>> foreignKeyColumns(List<? extends ForeignKey> foreignKeys) {
        Map<String, List<ForeignKey>> grouped = foreignKeys.stream()
                .collect(Collectors.groupingBy(ForeignKey::getFkName, LinkedHashMap::new, Collectors.toList()));

        Map<String, List<String>> answer = new LinkedHashMap<>();
        for (Map.Entry<String, List<ForeignKey>> entry : grouped.entrySet()) {
            List<String> columns = entry.getValue().stream()
                    .sorted((left, right) -> parseShort(left.getKeySeq()) - parseShort(right.getKeySeq()))
                    .map(ForeignKey::getFkColumnName)
                    .collect(Collectors.toList());
            answer.put(entry.getKey(), columns);
        }
        return answer;
    }

    private Map<String, List<String>> indexColumns(List<? extends Index> indexes) {
        Map<String, List<Index>> grouped = indexes.stream()
                .filter(index -> index.getIndexName() != null && !index.getIndexName().equalsIgnoreCase("PRIMARY"))
                .collect(Collectors.groupingBy(Index::getIndexName, LinkedHashMap::new, Collectors.toList()));

        Map<String, List<String>> answer = new LinkedHashMap<>();
        for (Map.Entry<String, List<Index>> entry : grouped.entrySet()) {
            List<String> columns = entry.getValue().stream()
                    .sorted((left, right) -> parseShort(left.getOrdinalPosition()) - parseShort(right.getOrdinalPosition()))
                    .map(Index::getColumnName)
                    .collect(Collectors.toList());
            answer.put(entry.getKey(), columns);
        }
        return answer;
    }

    private List<String> jdbcColumnNames(DatabaseMetaData metaData, String tableName) throws SQLException {
        List<String> names = new ArrayList<>();
        try (ResultSet rs = metaData.getColumns(null, null, tableName, "%")) {
            while (rs.next()) {
                names.add(rs.getString("COLUMN_NAME"));
            }
        }
        return names;
    }

    private List<String> jdbcPrimaryKeyNames(DatabaseMetaData metaData, String tableName) throws SQLException {
        Map<Short, String> ordered = new LinkedHashMap<>();
        try (ResultSet rs = metaData.getPrimaryKeys(null, null, tableName)) {
            while (rs.next()) {
                ordered.put(rs.getShort("KEY_SEQ"), rs.getString("COLUMN_NAME"));
            }
        }
        return new ArrayList<>(ordered.values());
    }

    private Map<String, List<String>> jdbcForeignKeyColumns(DatabaseMetaData metaData, String tableName) throws SQLException {
        Map<String, List<JdbcForeignKeyRow>> grouped = new LinkedHashMap<>();
        try (ResultSet rs = metaData.getImportedKeys(null, null, tableName)) {
            while (rs.next()) {
                String fkName = rs.getString("FK_NAME");
                grouped.computeIfAbsent(fkName, key -> new ArrayList<>())
                        .add(new JdbcForeignKeyRow(
                                rs.getShort("KEY_SEQ"),
                                rs.getString("FKCOLUMN_NAME")));
            }
        }

        Map<String, List<String>> answer = new LinkedHashMap<>();
        for (Map.Entry<String, List<JdbcForeignKeyRow>> entry : grouped.entrySet()) {
            List<String> columns = entry.getValue().stream()
                    .sorted((left, right) -> left.keySeq - right.keySeq)
                    .map(JdbcForeignKeyRow::columnName)
                    .collect(Collectors.toList());
            answer.put(entry.getKey(), columns);
        }
        return answer;
    }

    private Map<String, List<String>> jdbcIndexColumns(DatabaseMetaData metaData, String tableName) throws SQLException {
        Map<String, List<JdbcIndexRow>> grouped = new LinkedHashMap<>();
        try (ResultSet rs = metaData.getIndexInfo(null, null, tableName, false, true)) {
            while (rs.next()) {
                String indexName = rs.getString("INDEX_NAME");
                String columnName = rs.getString("COLUMN_NAME");
                if (indexName == null || columnName == null || "PRIMARY".equalsIgnoreCase(indexName)) {
                    continue;
                }
                grouped.computeIfAbsent(indexName, key -> new ArrayList<>())
                        .add(new JdbcIndexRow(
                                rs.getShort("ORDINAL_POSITION"),
                                columnName));
            }
        }

        Map<String, List<String>> answer = new LinkedHashMap<>();
        for (Map.Entry<String, List<JdbcIndexRow>> entry : grouped.entrySet()) {
            List<String> columns = entry.getValue().stream()
                    .sorted((left, right) -> left.ordinalPosition - right.ordinalPosition)
                    .map(JdbcIndexRow::columnName)
                    .collect(Collectors.toList());
            answer.put(entry.getKey(), columns);
        }
        return answer;
    }

    private short parseShort(String value) {
        return value == null ? 0 : Short.parseShort(value);
    }

    private record JdbcForeignKeyRow(short keySeq, String columnName) {
    }

    private record JdbcIndexRow(short ordinalPosition, String columnName) {
    }
}
