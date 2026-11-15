package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.dom.xml.VisitableElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.api.dom.xml.render.ElementRenderer;
import org.mybatis.generator.codegen.mybatis3.IntrospectedTableMyBatis3Impl;
import org.mybatis.generator.internal.PluginAggregator;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.QueryColumnConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.VoGeneratorConfiguration;
import org.mybatis.generator.config.VoViewGeneratorConfiguration;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link ExampleWhereClauseElementGenerator} 生成的组合条件 LIKE 方言分支渲染测试：
 * 按 MyBatis 内置 {@code _databaseId} 选择谓词，未支持方言经守卫在 SQL 构建时失败。
 *
 * @author vgosoft-agent
 */
class ExampleWhereClauseDialectTest {

    private static final Map<String, Object> TOKEN_CRITERION = Map.of(
            "tokenLikeValue", true,
            "condition", "t.tags_",
            "value", "%,a,%",
            "secondValue", "%,a!,%");

    private static final Map<String, Object> LITERAL_CRITERION = Map.of(
            "literalLikeValue", true,
            "condition", "t.name_",
            "value", "%x_y%",
            "secondValue", "%x_![y%");

    @Test
    void shouldRenderTokenLikePredicateForEachSupportedDialect() {
        for (String databaseId : List.of("mysql", "mariadb")) {
            BoundSql boundSql = render(databaseId, TOKEN_CRITERION);
            assertThat(normalize(boundSql.getSql()))
                    .contains("concat(',', t.tags_, ',') like ? escape '!'");
            assertThat(parameterProperty(boundSql)).endsWith(".value");
        }
        for (String databaseId : List.of("postgresql", "h2", "oracle", "db2", "dm")) {
            BoundSql boundSql = render(databaseId, TOKEN_CRITERION);
            assertThat(normalize(boundSql.getSql()))
                    .contains("(',' || t.tags_ || ',') like ? escape '!'");
            assertThat(parameterProperty(boundSql)).endsWith(".value");
        }
        for (String databaseId : List.of("sqlserver", "sqlserver2005")) {
            BoundSql boundSql = render(databaseId, TOKEN_CRITERION);
            assertThat(normalize(boundSql.getSql()))
                    .contains("(',' + t.tags_ + ',') like ? escape '!'");
            assertThat(parameterProperty(boundSql)).endsWith(".secondValue");
        }
    }

    @Test
    void shouldRenderLiteralLikePredicateForEachSupportedDialect() {
        for (String databaseId : List.of("mysql", "mariadb", "postgresql", "h2", "oracle", "db2", "dm")) {
            BoundSql boundSql = render(databaseId, LITERAL_CRITERION);
            assertThat(normalize(boundSql.getSql()))
                    .contains("t.name_ like ? escape '!'")
                    .doesNotContain("secondValue");
            assertThat(parameterProperty(boundSql)).endsWith(".value");
        }
        for (String databaseId : List.of("sqlserver", "sqlserver2005")) {
            BoundSql boundSql = render(databaseId, LITERAL_CRITERION);
            assertThat(normalize(boundSql.getSql()))
                    .contains("t.name_ like ? escape '!'");
            assertThat(parameterProperty(boundSql)).endsWith(".secondValue");
        }
    }

    @Test
    void shouldFailAtSqlBuildForUnsupportedDialects() {
        for (String databaseId : new String[]{null, "other", "sqlite", "kingbasees"}) {
            assertThatThrownBy(() -> render(databaseId, Map.of("tokenLikeValue", true)))
                    .satisfies(e -> assertThat(causeMessages(e)).contains("TOKEN_LIKE"));
            assertThatThrownBy(() -> render(databaseId, Map.of("literalLikeValue", true)))
                    .satisfies(e -> assertThat(causeMessages(e)).contains("LITERAL_LIKE"));
        }
    }

    @Test
    void shouldNotGuardValueKindCriteria() {
        BoundSql boundSql = render(null, Map.of("singleValue", true,
                "condition", "t.score_a >", "value", 1));
        assertThat(normalize(boundSql.getSql())).contains("t.score_a > ?");
    }

    @Test
    void shouldPlaceLikeBranchesBetweenNoValueAndSingleValue() {
        String fragment = whereClauseXml();
        int noValue = fragment.indexOf("criterion.noValue");
        int literalLike = fragment.indexOf("criterion.literalLikeValue");
        int tokenLike = fragment.indexOf("criterion.tokenLikeValue");
        int singleValue = fragment.indexOf("criterion.singleValue");
        assertThat(noValue).isLessThan(literalLike);
        assertThat(literalLike).isLessThan(tokenLike);
        assertThat(tokenLike).isLessThan(singleValue);
        assertThat(fragment).contains("escape '!'", "unsupportedDialect",
                "@com.vgosoft.mybatis.query.QueryDialectGuard@unsupported(_databaseId, 'TOKEN_LIKE')",
                "@com.vgosoft.mybatis.query.QueryDialectGuard@unsupported(_databaseId, 'LITERAL_LIKE')");
    }

    @Test
    void shouldEmitSameBranchesForUpdateByExampleClause() {
        ExampleWhereClauseElementGenerator generator = new ExampleWhereClauseElementGenerator(true);
        generator.setContext(context());
        generator.setIntrospectedTable(table());
        XmlElement parent = new XmlElement("mapper");
        generator.addElements(parent);
        String content = format(parent);
        assertThat(content).contains("criterion.literalLikeValue", "criterion.tokenLikeValue", "_databaseId");
    }

    @Test
    void shouldNotEmitConditionBranchesWithoutConditionConfiguration() {
        ExampleWhereClauseElementGenerator generator = new ExampleWhereClauseElementGenerator(false);
        generator.setContext(context());
        generator.setIntrospectedTable(tableWithoutConditions());
        XmlElement parent = new XmlElement("mapper");
        generator.addElements(parent);
        assertThat(format(parent)).doesNotContain("criterion.literalLikeValue", "criterion.tokenLikeValue");
    }

    private String whereClauseXml() {
        ExampleWhereClauseElementGenerator generator = new ExampleWhereClauseElementGenerator(false);
        generator.setContext(context());
        generator.setIntrospectedTable(table());
        XmlElement parent = new XmlElement("mapper");
        generator.addElements(parent);
        return format(parent);
    }

    private String format(XmlElement parent) {
        ElementRenderer renderer = new ElementRenderer();
        StringBuilder sb = new StringBuilder();
        for (VisitableElement element : parent.getElements()) {
            element.accept(renderer).forEach(line -> sb.append(line).append('\n'));
        }
        return sb.toString();
    }

    private Context context() {
        Context context = new Context(ModelType.CONDITIONAL);
        try {
            var plugins = Context.class.getDeclaredField("pluginAggregator");
            plugins.setAccessible(true);
            plugins.set(context, new PluginAggregator());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        return context;
    }

    private IntrospectedTableMyBatis3Impl table() {
        Context context = new Context(ModelType.CONDITIONAL);
        JavaModelGeneratorConfiguration model = new JavaModelGeneratorConfiguration(context);
        model.setTargetPackage("sample.pojo");
        model.setTargetProject("target/test-project");
        context.setJavaModelGeneratorConfiguration(model);
        TableConfiguration tc = new TableConfiguration(context, new ArrayList<>());
        tc.setTableName("record_");
        VoGeneratorConfiguration vo = new VoGeneratorConfiguration(context, tc);
        VoViewGeneratorConfiguration view = new VoViewGeneratorConfiguration(context, tc);
        view.getQueryColumns().add("tags_condition");
        QueryColumnConfiguration condition = new QueryColumnConfiguration(tc);
        condition.setQueryType("condition");
        condition.setKey("tags_condition");
        view.addQueryColumnConfigurations(condition);
        vo.setVoViewConfiguration(view);
        tc.setVoGeneratorConfiguration(vo);
        IntrospectedTableMyBatis3Impl table = new IntrospectedTableMyBatis3Impl();
        table.setContext(context);
        table.setTableConfiguration(tc);
        table.setExampleWhereClauseId("Example_Where_Clause");
        table.setMyBatis3UpdateByExampleWhereClauseId("Update_By_Example_Where_Clause");
        return table;
    }

    private IntrospectedTableMyBatis3Impl tableWithoutConditions() {
        Context context = new Context(ModelType.CONDITIONAL);
        TableConfiguration tc = new TableConfiguration(context, new ArrayList<>());
        tc.setTableName("record_");
        IntrospectedTableMyBatis3Impl table = new IntrospectedTableMyBatis3Impl();
        table.setContext(context);
        table.setTableConfiguration(tc);
        table.setExampleWhereClauseId("Example_Where_Clause");
        table.setMyBatis3UpdateByExampleWhereClauseId("Update_By_Example_Where_Clause");
        return table;
    }

    private BoundSql render(String databaseId, Map<String, Object> criterion) {
        Configuration configuration = new Configuration();
        if (databaseId != null) {
            configuration.setDatabaseId(databaseId);
        }
        String mapper = """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
                <mapper namespace="t.T">
                %s
                <select id="s" resultType="map">
                <include refid="Example_Where_Clause"/>
                </select>
                </mapper>
                """.formatted(whereClauseXml());
        XMLMapperBuilder builder = new XMLMapperBuilder(
                new ByteArrayInputStream(mapper.getBytes(StandardCharsets.UTF_8)),
                configuration, "query-condition-dialect-test.xml", configuration.getSqlFragments());
        builder.parse();
        MappedStatement statement = configuration.getMappedStatement("t.T.s");
        Map<String, Object> parameter = Map.of("oredCriteria",
                List.of(Map.of("valid", true, "criteria", List.of(criterion))));
        return statement.getBoundSql(parameter);
    }

    private String normalize(String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }

    private String parameterProperty(BoundSql boundSql) {
        assertThat(boundSql.getParameterMappings()).isNotEmpty();
        return boundSql.getParameterMappings().get(0).getProperty();
    }

    private String causeMessages(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        for (Throwable e = throwable; e != null; e = e.getCause()) {
            sb.append(e.getClass().getSimpleName()).append(':').append(String.valueOf(e.getMessage())).append('|');
        }
        return sb.toString();
    }
}
