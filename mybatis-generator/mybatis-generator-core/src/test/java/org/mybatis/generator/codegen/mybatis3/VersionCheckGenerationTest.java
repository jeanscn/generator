package org.mybatis.generator.codegen.mybatis3;

import com.github.javaparser.StaticJavaParser;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.GeneratedFile;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.PluginConfiguration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.StringReader;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 验证旧链乐观锁配置、生成输出与 H2 版本条件执行结果。
 *
 * @author vgosoft-agent
 */
class VersionCheckGenerationTest {
    private static final String TABLE = "sensitive_category";
    private static final String MAPPER_FILE = "SensitiveCategoryMapper.xml";

    private static Configuration parse(String value) throws Exception {
        String xml = CasConsumerBaselineTest.consumerSnapshotTestXml(null);
        if (value != null) {
            for (String tableName : List.of("sensitive_category", "sensitive_word")) {
                int tableStart = xml.indexOf("tableName=\"" + tableName + "\"");
                int tableEnd = xml.indexOf("</table>", tableStart);
                if (tableStart < 0 || tableEnd < 0) {
                    throw new IllegalStateException("消费配置缺少表：" + tableName);
                }
                int tableOpenEnd = xml.indexOf('>', tableStart);
                if (tableOpenEnd < 0 || tableOpenEnd > tableEnd) {
                    throw new IllegalStateException("消费配置中的表元素不完整：" + tableName);
                }
                xml = xml.substring(0, tableOpenEnd + 1)
                        + "<property name=\"versionCheck\" value=\"" + value + "\"/>"
                        + xml.substring(tableOpenEnd + 1);
            }
        }
        var parser = new ConfigurationParser(new ArrayList<>());
        var configuration = parser.parseConfiguration(new StringReader(xml));
        parser.customConfig(configuration);
        return configuration;
    }

    private static Map<String, String> generate(Configuration configuration) throws Exception {
        var generator = new MyBatisGenerator(configuration, new DefaultShellCallback(true), new ArrayList<>());
        generator.generate(null, null, null, false);
        List<GeneratedFile> files = new ArrayList<>(generator.getGeneratedJavaFiles());
        files.addAll(generator.getGeneratedXmlFiles());
        files.addAll(generator.getGeneratedHtmlFiles());
        files.addAll(generator.getGeneratedKotlinFiles());
        var other = MyBatisGenerator.class.getDeclaredField("otherGeneratedFiles");
        other.setAccessible(true);
        files.addAll((List<? extends GeneratedFile>) other.get(generator));
        Map<String, String> outputs = new TreeMap<>();
        for (var file : files) {
            String key = file.getTargetPackage().replace('.', '\\') + "\\" + file.getFileName();
            assertThat(outputs.put(key, CasConsumerBaselineTest.normalize(file.getFormattedContent()))).isNull();
        }
        return outputs;
    }

    private static String output(Map<String, String> files, String filename) {
        return files.entrySet().stream().filter(entry -> entry.getKey().endsWith("\\" + filename))
                .findFirst().orElseThrow(() -> new AssertionError("缺少生成文件：" + filename)).getValue();
    }

    private static String updateStatement(String mapperXml, String statementId) {
        Matcher matcher = Pattern.compile("(?s)<update id=\"" + Pattern.quote(statementId) + "\"[^>]*>.*?</update>")
                .matcher(mapperXml);
        assertThat(matcher.find()).as("生成 SQL 语句 %s", statementId).isTrue();
        return matcher.group();
    }

    private static String mapperDocument(String statement, String parameterType) {
        String configured = statement.replaceFirst("parameterType=\"[^\"]*\"",
                "parameterType=\"" + parameterType + "\"");
        return "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" "
                + "\"https://mybatis.org/dtd/mybatis-3-mapper.dtd\">"
                + "<mapper namespace=\"VersionCheckMapper\">" + configured + "</mapper>";
    }

    @Test
    void omittedAndFalsePropertiesPreserveTheT09Outputs() throws Exception {
        var omitted = generate(parse(null));
        var disabled = generate(parse("false"));

        assertThat(disabled).isEqualTo(omitted);
        String xml = output(omitted, MAPPER_FILE);
        assertThat(updateStatement(xml, "updateByPrimaryKeySelective"))
                .doesNotContain("and version_ =");
    }

    @Test
    void truePropertyAddsSingleAndPerRowOldVersionPredicatesAndServiceRollback() throws Exception {
        var generated = generate(parse("true"));
        String mapper = output(generated, MAPPER_FILE);
        assertThat(updateStatement(mapper, "updateByPrimaryKeySelective"))
                .contains("where id_ = #{id,jdbcType=VARCHAR}", "and version_ = #{version,jdbcType=INTEGER}");

        String batch = updateStatement(mapper, "updateBatchByPrimaryKey");
        assertThat(batch).contains("where (", "id_ = #{item.id,jdbcType=VARCHAR}",
                "version_ = #{item.version,jdbcType=INTEGER}")
                .doesNotContain("where id_ in");

        String service = output(generated, "AbstractSensitiveCategoryImpl.java");
        assertThat(service).contains("@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)",
                "if (i <= 0)", "if (i != sensitiveCategorys.size())",
                "TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()",
                "ServiceResult.failure(ServiceCodeEnum.WARN, \"版本冲突，请刷新后重试\")",
                "ServiceResult.failure(ServiceCodeEnum.WARN, \"批量更新存在版本冲突，请刷新后重试\")");
    }

    /** 校验各类生成写方法保留失败码、原始消息及同一异常 cause。 */
    @Test
    void generatedWriteFailuresPreserveCodeMessageAndCause() throws Exception {
        String service = output(generate(parse(null)), "AbstractSensitiveCategoryImpl.java");
        var type = StaticJavaParser.parse(service).getClassByName("AbstractSensitiveCategoryImpl").orElseThrow();

        for (String methodName : List.of("insert", "insertSelective", "insertOrUpdate",
                "updateBatchByPrimaryKey", "updateByPrimaryKey", "updateByPrimaryKeySelective")) {
            var method = type.getMethodsByName(methodName).stream().findFirst().orElseThrow();
            assertThat(method.getBody().orElseThrow().toString())
                    .as("生成方法 %s 的失败包装", methodName)
                    .contains("new VgoException(e.getMessage(), e).setCode(ServiceCodeEnum.FAIL.code())");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "TRUE", "False", "yes", "1", " true "})
    void rejectsNonCanonicalBooleanValues(String value) {
        assertThatThrownBy(() -> generate(parse(value)))
                .hasMessageContaining("versionCheck")
                .hasMessageContaining("只接受 true 或 false");
    }

    @Test
    void rejectsGeneratedVersionColumn() throws Exception {
        var configuration = parse("true");
        var plugin = new PluginConfiguration();
        plugin.setConfigurationType(GeneratedVersionColumnPlugin.class.getName());
        configuration.getContexts().get(0).addPluginConfiguration(plugin);

        assertThatThrownBy(() -> generate(configuration))
                .hasMessageContaining("version_")
                .hasMessageContaining("可写 Integer/INTEGER 或 Long/BIGINT");
    }

    @Test
    void h2MapperStatementsRejectStaleSingleAndBatchVersions() throws Exception {
        var generated = generate(parse("true"));
        String mapper = output(generated, MAPPER_FILE);
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:versionCheck;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute("create table " + TABLE + " ("
                    + "id_ varchar(64) primary key, created_ timestamp, created_id varchar(64), tenant_id varchar(64), "
                    + "modified_ timestamp, modified_id varchar(64), version_ integer not null, "
                    + "delete_flag integer, state_ varchar(64))");
            statement.execute("insert into " + TABLE + " (id_, created_id, tenant_id, version_, state_) "
                    + "values ('single', 'creator', 'tenant', 4, 'old')");
            statement.execute("insert into " + TABLE + " (id_, created_id, tenant_id, version_, state_) "
                    + "values ('batch-one', 'creator', 'tenant', 8, 'old')");
            statement.execute("insert into " + TABLE + " (id_, created_id, tenant_id, version_, state_) "
                    + "values ('batch-two', 'creator', 'tenant', 11, 'old')");
        }

        var mybatisConfiguration = new org.apache.ibatis.session.Configuration(
                new Environment("version-check", new JdbcTransactionFactory(), dataSource));
        String singleXml = mapperDocument(updateStatement(mapper, "updateByPrimaryKeySelective"), "map");
        new XMLMapperBuilder(new StringReader(singleXml), mybatisConfiguration,
                "version-check-single.xml", mybatisConfiguration.getSqlFragments()).parse();
        String batchXml = mapperDocument(updateStatement(mapper, "updateBatchByPrimaryKey"), "java.util.List");
        new XMLMapperBuilder(new StringReader(batchXml), mybatisConfiguration,
                "version-check-batch.xml", mybatisConfiguration.getSqlFragments()).parse();
        var sessionFactory = new SqlSessionFactoryBuilder().build(mybatisConfiguration);

        try (var session = sessionFactory.openSession(false)) {
            Map<String, Object> current = Map.of("id", "single", "version", 4,
                    "state", "current", "modified", Timestamp.from(Instant.parse("2026-01-01T00:00:00Z")),
                    "modifiedId", "actor");
            assertThat(session.update("VersionCheckMapper.updateByPrimaryKeySelective", current)).isEqualTo(1);
            session.commit();
        }
        try (var session = sessionFactory.openSession(false)) {
            Map<String, Object> stale = Map.of("id", "single", "version", 4, "state", "stale",
                    "modified", Timestamp.from(Instant.parse("2026-01-02T00:00:00Z")), "modifiedId", "actor");
            assertThat(session.update("VersionCheckMapper.updateByPrimaryKeySelective", stale)).isZero();
            session.commit();
        }
        assertThat(state(dataSource, "single")).containsExactly(5, "current");

        try (var session = sessionFactory.openSession(false)) {
            List<Map<String, Object>> rows = List.of(
                    Map.of("id", "batch-one", "version", 8, "state", "should-rollback"),
                    Map.of("id", "batch-two", "version", 10, "state", "stale"));
            assertThat(session.update("VersionCheckMapper.updateBatchByPrimaryKey", rows)).isEqualTo(1);
            session.rollback();
        }
        assertThat(state(dataSource, "batch-one")).containsExactly(8, "old");
        assertThat(state(dataSource, "batch-two")).containsExactly(11, "old");
    }

    private static List<Object> state(JdbcDataSource dataSource, String id) throws Exception {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement("select version_, state_ from " + TABLE + " where id_ = ?")) {
            statement.setString(1, id);
            try (var result = statement.executeQuery()) {
                assertThat(result.next()).isTrue();
                return List.of(result.getInt(1), result.getString(2));
            }
        }
    }

    /**
     * 将版本列标记为数据库生成列，验证启用版本检查时会拒绝该结构。
     *
     * @author vgosoft-agent
     */
    public static class GeneratedVersionColumnPlugin extends PluginAdapter {
        @Override
        public boolean validate(List<String> warnings) {
            return true;
        }

        @Override
        public void initialized(AbstractIntrospectedTable table) {
            table.getColumn("version_").ifPresent(column -> column.setGeneratedColumn(true));
            VersionCheckGenerationValidator.validate(table);
        }
    }
}
