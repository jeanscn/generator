package org.mybatis.generator.codegen.mybatis3;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.ConnectionFactory;
import org.mybatis.generator.api.GeneratedFile;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import javax.sql.rowset.RowSetMetaDataImpl;
import javax.sql.rowset.RowSetProvider;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 以消费配置的八表结构和只读元数据替身保存完整隔离生成基线，不建立数据库连接。
 *
 * @author vgosoft
 */
class CasConsumerBaselineTest {
    private static final Path CONSUMER = Path.of(System.getProperty("sc03.consumer",
            "D:\\java\\idea_projs7\\project-vgosoft\\vgosoft-sensitive\\src\\main\\resources"));

    // 保留八表全部输出层和默认运行时，仅替换连接、驱动路径和输出根目录。
    @Test
    void capturesCompleteConsumerOutputWithoutExternalWrites() throws Exception {
        String xml = consumerSnapshotXml(System.getProperty("sc03.cas"));
        Path root = Path.of("target", System.getProperty("sc03.baseline.name", "sc03-after-omitted"));
        Files.createDirectories(root);
        Files.writeString(root.resolve("sanitized-config.xml"), xml);
        String ddl = snapshotSchema();
        var memoryTables = Pattern.compile("(?s)(CREATE TABLE `sensitive_(?:category|word)` \\(.*?)\\) ENGINE=.*?;").matcher(ddl);
        StringBuilder memorySchema = new StringBuilder();
        while (memoryTables.find()) {
            memorySchema.append(memoryTables.group(1).replaceAll(" CHARACTER SET \\w+", "")
                    .replaceAll(" COLLATE \\w+", "")).append(");\n");
        }
        Files.writeString(root.resolve("h2-schema.sql"), memorySchema);
        List<String> warnings = new ArrayList<>();
        var parser = new ConfigurationParser(warnings);
        var config = parser.parseConfiguration(new StringReader(xml));
        assertThat(config.getContexts()).hasSize(1);
        assertThat(config.getContexts().get(0).getTableConfigurations()).hasSize(8);
        assertThat(config.getContexts().get(0).getTargetRuntime()).isNull();
        if (Boolean.getBoolean("sc03.h2")) {
            config.getContexts().get(0).getTableConfigurations().stream().limit(2)
                    .forEach(t -> t.getAdditionalDbTypes().add(com.vgosoft.core.db.query.DatabaseType.H2));
        }
        var generator = new MyBatisGenerator(config, new DefaultShellCallback(true), warnings);
        generator.generate(null, null, null, false);
        List<GeneratedFile> output = new ArrayList<>();
        output.addAll(generator.getGeneratedJavaFiles());
        output.addAll(generator.getGeneratedXmlFiles());
        output.addAll(generator.getGeneratedKotlinFiles());
        output.addAll(generator.getGeneratedHtmlFiles());
        var other = MyBatisGenerator.class.getDeclaredField("otherGeneratedFiles");
        other.setAccessible(true);
        output.addAll((List<? extends GeneratedFile>) other.get(generator));
        assertThat(output).isNotEmpty();
        Map<String, String> digests = new java.util.TreeMap<>();
        for (GeneratedFile file : output) {
            String relative = (file.getTargetPackage().isEmpty() ? ""
                    : file.getTargetPackage().replace('.', '\\') + "\\") + file.getFileName();
            Path target = root.resolve("output").resolve(relative);
            assertThat(target.normalize().startsWith(root.normalize()))
                    .as("输出目标必须隔离：%s / %s", file.getTargetPackage(), file.getFileName()).isTrue();
            Files.createDirectories(target.getParent());
            String text = normalize(file.getFormattedContent());
            assertThat(digests).doesNotContainKey(relative);
            Files.writeString(target, text, StandardCharsets.UTF_8);
            digests.put(relative, java.util.HexFormat.of().formatHex(
                    java.security.MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8))));
        }
        Files.write(root.resolve("manifest.sha256"), digests.entrySet().stream()
                .map(e -> e.getValue() + "  " + e.getKey()).toList());
        Files.write(root.resolve("warnings.txt"), warnings);
        assertThat(digests.keySet()).anyMatch(n -> n.endsWith("GenSensitiveWordMapper.java"))
                .anyMatch(n -> n.endsWith("SensitiveCategoryMapper.xml"));
        System.out.println("SC03 完整基线文件数：" + output.size());
    }

    static String normalize(String source) {
        return source.replace("\r\n", "\n")
                .replaceAll("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}", "2000-01-01 00:00:00")
                .replaceAll("\\d{4}-\\d{2}-\\d{2}", "2000-01-01")
                .replaceAll("(@mbg.generated(?: do_not_delete_during_merge)?) [A-Z][a-z]{2} [A-Z][a-z]{2} \\d{2} \\d{2}:\\d{2}:\\d{2} \\S+ \\d{4}",
                        "$1 2000-01-01 00:00:00")
                .replaceAll("(This element was generated on )[A-Z][a-z]{2} [A-Z][a-z]{2} \\d{2} \\d{2}:\\d{2}:\\d{2} \\S+ \\d{4}",
                        "$1" + "2000-01-01 00:00:00");
    }

    static String consumerXml(String enabled) throws Exception {
        String xml = Files.readString(CONSUMER.resolve("generatorConfig.xml"));
        xml = xml.replaceAll("(?s)<classPathEntry\\b[^>]*/>", "")
                .replaceAll("(?s)<jdbcConnection\\b.*?</jdbcConnection>",
                        java.util.regex.Matcher.quoteReplacement(
                                "<connectionFactory type=\"" + MetadataFactory.class.getName() + "\"/>"));
        xml = xml.replaceAll("targetProject=\"[^\"]*\"", "targetProject=\"target/sc03-baseline\"");
        xml = xml.replaceAll("vueEndProjectPath=\"[^\"]*\"", "vueEndProjectPath=\"target/sc03-baseline/web\"");
        xml = xml.replaceAll(" enableCasWriteContract=\"(?:true|false)\"", "");
        if (!Boolean.getBoolean("sc03.disabled")) {
            xml = xml.replaceAll("(?s)\\s*<disabledMethod\\b[^>]*/>", "");
        }
        if (enabled != null) {
            xml = xml.replace("tableName=\"sensitive_category\"", "tableName=\"sensitive_category\" enableCasWriteContract=\"" + enabled + "\"")
                    .replace("tableName=\"sensitive_word\"", "tableName=\"sensitive_word\" enableCasWriteContract=\"" + enabled + "\"");
        }
        return xml;
    }

    static String consumerSnapshotXml(String enabled) throws Exception {
        String xml = snapshotFile("generatorConfig.xml")
                .replaceAll(" enableCasWriteContract=\"(?:true|false)\"", "");
        if (!Boolean.getBoolean("sc03.disabled")) {
            xml = xml.replaceAll("(?s)\\s*<disabledMethod\\b[^>]*/>", "");
        }
        if (enabled != null) {
            xml = xml.replace("tableName=\"sensitive_category\"", "tableName=\"sensitive_category\" enableCasWriteContract=\"" + enabled + "\"")
                    .replace("tableName=\"sensitive_word\"", "tableName=\"sensitive_word\" enableCasWriteContract=\"" + enabled + "\"");
        }
        return xml;
    }

    static String consumerSnapshotTestXml(String enabled) throws Exception {
        String xml = consumerSnapshotXml(enabled).replaceAll(
                "(?s)<generateHtml\\b(?=[^>]*\\bgenerate=\"true\")[^>]*>.*?</generateHtml>",
                "<generateHtml generate=\"false\"/>");
        return xml.replaceAll("(<excelImport\\b[^>]*?)generate=\"true\"", "$1generate=\"false\"");
    }

    static String snapshotSchema() throws Exception {
        return snapshotFile("sql/mysql/schema_sensitive.sql");
    }

    private static String snapshotFile(String name) throws Exception {
        try (InputStream input = CasConsumerBaselineTest.class.getResourceAsStream(
                "/sc03/consumer-current-v1/" + name)) {
            if (input == null) {
                throw new IllegalStateException("缺少固定消费配置快照资源：" + name);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * 从已提交 DDL 构造 JDBC 元数据，所有 SQL 都仅返回替身结果，禁止执行写入。
     *
     * @author vgosoft
     */
    public static final class MetadataFactory implements ConnectionFactory {
        @Override
        public void addConfigurationProperties(Properties properties) { }

        @Override
        public Connection getConnection() throws SQLException {
            try {
                return connection();
            } catch (Exception failure) {
                throw new SQLException("构建隔离元数据失败", failure);
            }
        }

        private static Connection connection() throws Exception {
            Map<String, List<Map<String, Object>>> columns = new LinkedHashMap<>();
            Map<String, String> remarks = new LinkedHashMap<>();
            String ddl = snapshotSchema();
            var tables = Pattern.compile("(?s)CREATE TABLE `([^`]+)` \\((.*?)\\) ENGINE=.*?COMMENT='([^']*)';").matcher(ddl);
            while (tables.find()) {
                String table = tables.group(1);
                remarks.put(table, tables.group(3));
                List<Map<String, Object>> rows = new ArrayList<>();
                for (String line : tables.group(2).split("\\R")) {
                    var col = Pattern.compile("^\\s*`([^`]+)` (VARCHAR|CHAR|INT|BIGINT|TIMESTAMP|MEDIUMTEXT)(?:\\((\\d+)\\))?(.*)").matcher(line);
                    if (!col.matches()) { continue; }
                    String type = col.group(2);
                    int jdbc = switch (type) {
                        case "INT" -> Types.INTEGER;
                        case "BIGINT" -> Types.BIGINT;
                        case "TIMESTAMP" -> Types.TIMESTAMP;
                        case "CHAR" -> Types.CHAR;
                        case "MEDIUMTEXT", "VARCHAR" -> Types.VARCHAR;
                        default -> throw new IllegalArgumentException(type);
                    };
                    var comment = Pattern.compile("COMMENT '([^']*)'").matcher(col.group(4));
                    var defaults = Pattern.compile("DEFAULT (\\S+)").matcher(col.group(4));
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("TABLE_CAT", "sc03"); row.put("TABLE_SCHEM", null); row.put("TABLE_NAME", table);
                    row.put("COLUMN_NAME", col.group(1)); row.put("DATA_TYPE", jdbc); row.put("TYPE_NAME", type);
                    row.put("COLUMN_SIZE", col.group(3) == null ? 0 : Integer.parseInt(col.group(3)));
                    row.put("DECIMAL_DIGITS", 0); row.put("NULLABLE", col.group(4).contains("NOT NULL") ? 0 : 1);
                    row.put("REMARKS", comment.find() ? comment.group(1) : "");
                    row.put("COLUMN_DEF", defaults.find() ? defaults.group(1) : null);
                    row.put("IS_AUTOINCREMENT", "NO"); row.put("IS_GENERATEDCOLUMN", "NO");
                    row.put("COLUMN_TYPE", type); row.put("COLUMN_LENGTH", col.group(3));
                    rows.add(row);
                }
                columns.put(table, rows);
            }
            assertThat(columns).hasSize(8);
            Connection[] connection = new Connection[1];
            DatabaseMetaData metadata = proxy(DatabaseMetaData.class, (p, method, args) -> switch (method.getName()) {
                case "getConnection" -> connection[0];
                case "getDatabaseProductName" -> "MySQL";
                case "getDatabaseProductVersion" -> "8.0.36";
                case "getDriverName" -> "SC03 Metadata Substitute";
                case "getURL" -> "jdbc:mysql://metadata-substitute/sc03";
                case "getIdentifierQuoteString" -> "`";
                case "getSearchStringEscape" -> "\\";
                case "getColumns" -> result(columns.getOrDefault(args[2], List.of()));
                case "getPrimaryKeys" -> result(List.of(Map.of("COLUMN_NAME", "id_", "KEY_SEQ", 1,
                        "TABLE_NAME", args[2], "PK_NAME", "PRIMARY", "TABLE_CAT", "sc03")));
                case "getTables" -> result(remarks.entrySet().stream()
                        .filter(e -> "%".equals(args[2]) || e.getKey().equals(args[2]))
                        .map(e -> Map.<String, Object>of("TABLE_NAME", e.getKey(), "TABLE_TYPE", "TABLE",
                                "TABLE_CAT", "sc03", "REMARKS", e.getValue())).toList());
                case "getImportedKeys", "getExportedKeys", "getIndexInfo" -> result(List.of());
                default -> primitive(method.getReturnType());
            });
            connection[0] = proxy(Connection.class, (p, method, args) -> switch (method.getName()) {
                case "getMetaData" -> metadata;
                case "getCatalog" -> "sc03";
                case "getSchema" -> null;
                case "prepareStatement" -> proxy(PreparedStatement.class, (q, m, a) -> {
                    if (m.getName().equals("executeQuery")) {
                        String sql = (String) args[0];
                        List<Map<String, Object>> rows = new ArrayList<>();
                        if (sql.contains("INFORMATION_SCHEMA.COLUMNS")) {
                            columns.forEach((name, list) -> {
                                if (!sql.contains("A.TABLE_NAME = '") || sql.contains("A.TABLE_NAME = '" + name + "'")) {
                                    rows.addAll(list);
                                }
                            });
                        }
                        return result(rows);
                    }
                    if (m.getName().startsWith("execute")) { throw new SQLException("替身禁止 SQL 写入"); }
                    return primitive(m.getReturnType());
                });
                case "createStatement" -> throw new SQLException("替身禁止 SQL 写入");
                default -> primitive(method.getReturnType());
            });
            return connection[0];
        }

        private static ResultSet result(List<Map<String, Object>> rows) throws SQLException {
            var result = RowSetProvider.newFactory().createCachedRowSet();
            List<String> names = rows.isEmpty() ? List.of("EMPTY") : new ArrayList<>(rows.get(0).keySet());
            var meta = new RowSetMetaDataImpl();
            meta.setColumnCount(names.size());
            for (int i = 0; i < names.size(); i++) {
                meta.setColumnName(i + 1, names.get(i)); meta.setColumnLabel(i + 1, names.get(i));
                Object sample = rows.isEmpty() ? null : rows.get(0).get(names.get(i));
                meta.setColumnType(i + 1, sample instanceof Integer ? Types.INTEGER : Types.VARCHAR);
            }
            result.setMetaData(meta);
            for (int r = rows.size() - 1; r >= 0; r--) {
                result.moveToInsertRow();
                for (int i = 0; i < names.size(); i++) { result.updateObject(i + 1, rows.get(r).get(names.get(i))); }
                result.insertRow(); result.moveToCurrentRow();
            }
            result.beforeFirst();
            return result;
        }

        private static <T> T proxy(Class<T> type, java.lang.reflect.InvocationHandler handler) {
            return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
        }

        private static Object primitive(Class<?> type) {
            if (type == boolean.class) { return false; }
            if (type == int.class) { return 0; }
            if (type == short.class) { return (short) 0; }
            if (type == long.class) { return 0L; }
            return null;
        }
    }
}
