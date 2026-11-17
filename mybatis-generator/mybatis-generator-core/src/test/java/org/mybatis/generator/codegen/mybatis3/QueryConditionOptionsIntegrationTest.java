package org.mybatis.generator.codegen.mybatis3;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.java.render.TopLevelClassRenderer;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.api.dom.xml.render.ElementRenderer;
import org.mybatis.generator.api.dom.xml.VisitableElement;
import org.mybatis.generator.codegen.mybatis3.controller.elements.ListElementGenerator;
import org.mybatis.generator.codegen.mybatis3.model.ExampleGenerator;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.ExampleWhereClauseElementGenerator;
import org.mybatis.generator.config.ConditionOptionConfiguration;
import org.mybatis.generator.config.ConditionTermConfiguration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.QueryColumnConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.VoGeneratorConfiguration;
import org.mybatis.generator.config.VoRequestGeneratorConfiguration;
import org.mybatis.generator.config.VoViewGeneratorConfiguration;
import org.mybatis.generator.internal.DefaultCommentGenerator;
import org.mybatis.generator.internal.PluginAggregator;
import org.mybatis.generator.internal.rules.ConditionalModelRules;

import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vgosoft.core.adapter.web.FilterParam;
import com.vgosoft.core.annotation.QueryCondition;
import com.vgosoft.core.annotation.QueryConditionOption;
import com.vgosoft.core.annotation.QueryConditionTerm;
import com.vgosoft.core.annotation.ViewTableMeta;
import com.vgosoft.core.constant.enums.core.QueryConditionOperatorEnum;
import com.vgosoft.core.constant.enums.core.QueryConditionValueTypeEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementTagTypeEnum;
import com.vgosoft.web.utils.BoundCondition;
import com.vgosoft.web.utils.BoundConditionGroup;
import com.vgosoft.web.utils.QueryConditionResolver;
import com.vgosoft.web.utils.ResolvedConditions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * T06 本地集成验收：配置 → 注解 → 真实 {@link QueryConditionResolver#resolveConditions}
 * → 内存编译的生成 Example 反射装配 → 生成 XML 渲染 BoundSql 的端到端链路。
 * 真实类（注解、BoundCondition/ResolvedConditions、resolver）来自已安装的 7.0.0-SNAPSHOT 工件。
 *
 * @author vgosoft-agent
 */
class QueryConditionOptionsIntegrationTest {

    /**
     * 与生成器产出同形的真实注解夹具：选项条件 phase_group（checkbox 多选）
     * 与固定条件 del_flag_on（常量 term、无参）。
     */
    @ViewTableMeta(value = "t06", listName = "验收", beanName = "t06", queryConditions = {
            @QueryCondition(key = "phase_group", remark = "项目进度",
                    tagName = HtmlElementTagTypeEnum.CHECKBOX, multiple = true,
                    options = {@QueryConditionOption(value = "all", remark = "全部", clear = true),
                            @QueryConditionOption(value = "completed", remark = "已完成",
                                    terms = {@QueryConditionTerm(column = "phase_state",
                                            operator = QueryConditionOperatorEnum.GT,
                                            value = "7", valueType = QueryConditionValueTypeEnum.NUMBER),
                                            @QueryConditionTerm(column = "apply_state",
                                                    operator = QueryConditionOperatorEnum.EQ,
                                                    value = "0", valueType = QueryConditionValueTypeEnum.NUMBER)}),
                            @QueryConditionOption(value = "not_started", remark = "未启动",
                                    terms = @QueryConditionTerm(column = "phase_state",
                                            operator = QueryConditionOperatorEnum.LT,
                                            value = "3", valueType = QueryConditionValueTypeEnum.NUMBER))}),
            @QueryCondition(key = "del_flag_on", remark = "未删除",
                    terms = @QueryConditionTerm(column = "del_flag",
                            operator = QueryConditionOperatorEnum.EQ,
                            value = "0", valueType = QueryConditionValueTypeEnum.NUMBER))})
    static class OptionViewVo {
    }

    @Test
    void shouldResolveAssembleAndRenderOptionGroupEndToEnd() throws Exception {
        IntrospectedTableMyBatis3Impl table = table();
        ClassLoader loader = compileExample(table);
        Object example = newExample(loader);
        Object criteria = createCriteria(example);

        // 真实解析：多选 completed + not_started，叠加固定条件 del_flag_on
        ResolvedConditions resolved = QueryConditionResolver.resolveConditions(OptionViewVo.class,
                List.of(optionItem("phase_group", "completed", "not_started"), item("del_flag_on")), "t");
        assertThat(resolved.groups()).hasSize(1);
        assertThat(resolved.conditions()).hasSize(1);

        applyResolved(criteria, resolved);
        BoundSql boundSql = render(table, oredCriteria(example), "mysql");
        String sql = boundSql.getSql().replaceAll("\\s+", "");
        // 组间 OR、组内 AND、平铺谓词以 AND 叠加；全部绑定参数，选项标识不进入 SQL
        assertThat(sql).contains("t.del_flag=?and((t.phase_state>?andt.apply_state=?)or(t.phase_state<?))");
        assertThat(sql).doesNotContain("completed", "not_started", "all");
        // del_flag + phase_state×2 + apply_state：四值全部绑定
        assertThat(boundSql.getParameterMappings()).hasSize(4);
    }

    @Test
    void shouldYieldNoPredicateForClearOnlyAndOmittedSubmission() throws Exception {
        IntrospectedTableMyBatis3Impl table = table();
        Object example = newExample(compileExample(table));
        Object criteria = createCriteria(example);

        ResolvedConditions cleared = QueryConditionResolver.resolveConditions(OptionViewVo.class,
                List.of(optionItem("phase_group", "all")), "t");
        assertThat(cleared.groups()).isEmpty();
        ResolvedConditions empty = QueryConditionResolver.resolveConditions(OptionViewVo.class, null, "t");
        assertThat(empty.groups()).isEmpty();
        assertThat(empty.conditions()).isEmpty();

        applyResolved(criteria, cleared);
        BoundSql boundSql = render(table, oredCriteria(example), "mysql");
        // 仅选“全部”：分组谓词不产出，SQL 不含选项列谓词
        assertThat(boundSql.getSql().replaceAll("\\s+", "")).doesNotContain("phase_state");
    }

    @Test
    void shouldRejectInvalidSubmissionsThroughRealResolver() {
        // A07：未知选项、clear 混传、重复选项、参数型携带 options 均明确失败
        assertThatThrownBy(() -> QueryConditionResolver.resolveConditions(OptionViewVo.class,
                List.of(optionItem("phase_group", "ghost")), "t"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("未知");
        assertThatThrownBy(() -> QueryConditionResolver.resolveConditions(OptionViewVo.class,
                List.of(optionItem("phase_group", "all", "completed")), "t"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("清空");
        assertThatThrownBy(() -> QueryConditionResolver.resolveConditions(OptionViewVo.class,
                List.of(optionItem("phase_group", "completed", "completed")), "t"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("重复");
        FilterParam.ConditionItem withOptions = item("del_flag_on");
        withOptions.setOptions(List.of("on"));
        assertThatThrownBy(() -> QueryConditionResolver.resolveConditions(OptionViewVo.class,
                List.of(withOptions), "t"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("不接受选项");
    }

    @Test
    void shouldEmitWiringAndTolerateEmptyGroup() throws Exception {
        // A11：list 生成语句接入 resolveConditions 分派与分组应用
        TopLevelClass controller = new TopLevelClass("sample.Controller");
        ListElementGenerator generator = new ListElementGenerator();
        generator.setContext(listTable().getContext());
        generator.setIntrospectedTable(listTable());
        generator.setEntityType(new FullyQualifiedJavaType("sample.Support.Record"));
        generator.setExampleType(new FullyQualifiedJavaType("sample.Support.RecordExample"));
        generator.setEntityVoType(new FullyQualifiedJavaType("sample.Support.RecordVo"));
        generator.setEntityViewVoType(new FullyQualifiedJavaType("sample.Support.RecordViewVo"));
        generator.setEntityRequestVoType(new FullyQualifiedJavaType("sample.Support.RecordRequestVo"));
        generator.setEntityMappings(new FullyQualifiedJavaType("sample.Support.RecordMappings"));
        generator.setServiceBeanName("service");
        generator.setCommentGenerator(new DefaultCommentGenerator());
        generator.addElements(controller);
        String body = String.join("\n", controller.getMethods().stream()
                .filter(m -> m.getName().equals("listPostRecord")).findFirst().orElseThrow().getBodyLines());
        assertThat(body).contains(
                "ResolvedConditions resolved = QueryConditionResolver.resolveConditions(RecordViewVo.class",
                "List<BoundCondition> conditions = resolved.conditions();",
                "andBoundConditionGroup(group.orGroups())");

        // 空/缺省分组输入不产生谓词（防御路径）
        Object criteria = createCriteria(newExample(compileExample(table())));
        Method group = criteria.getClass().getMethod("andBoundConditionGroup", List.class);
        group.invoke(criteria, (Object) null);
        group.invoke(criteria, List.of());
        group.invoke(criteria, List.of(List.of()));
        assertThat((List<?>) criteria.getClass().getMethod("getCriteria").invoke(criteria)).isEmpty();
    }

    // ===== 真实解析结果 → 生成 Example 反射装配 =====

    private void applyResolved(Object criteria, ResolvedConditions resolved) throws Exception {
        Class<?> criteriaClass = criteria.getClass();
        Method value = criteriaClass.getMethod("andBoundCondition", String.class, Object.class);
        Method literal = criteriaClass.getMethod("andLiteralLike", String.class, String.class, String.class);
        Method token = criteriaClass.getMethod("andTokenLike", String.class, String.class, String.class);
        for (BoundCondition bound : resolved.conditions()) {
            switch (bound.kind()) {
                case VALUE -> value.invoke(criteria, bound.condition(), bound.value());
                case LITERAL_LIKE ->
                        literal.invoke(criteria, bound.condition(), (String) bound.value(), bound.sqlServerValue());
                case TOKEN_LIKE ->
                        token.invoke(criteria, bound.condition(), (String) bound.value(), bound.sqlServerValue());
            }
        }
        Method group = criteriaClass.getMethod("andBoundConditionGroup", List.class);
        for (BoundConditionGroup boundGroup : resolved.groups()) {
            group.invoke(criteria, boundGroup.orGroups());
        }
    }

    // ===== 生成 Example 内存编译（真实 BoundCondition 依赖） =====

    private ClassLoader compileExample(IntrospectedTableMyBatis3Impl table) throws Exception {
        ExampleGenerator generator = new ExampleGenerator("src");
        generator.setContext(table.getContext());
        generator.setIntrospectedTable(table);
        generator.setProgressCallback(new SilentProgress());
        generator.setWarnings(new ArrayList<>());
        TopLevelClass topLevel = (TopLevelClass) generator.getCompilationUnits().get(0);
        String source = new TopLevelClassRenderer().render(topLevel);
        return compile(Map.of(topLevel.getType().getFullyQualifiedName(), source));
    }

    private ClassLoader compile(Map<String, String> sources) throws Exception {
        var compiler = ToolProvider.getSystemJavaCompiler();
        assertThat(compiler).as("必须使用完整 JDK 验证生成代码").isNotNull();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        Map<String, ByteArrayOutputStream> bytes = new HashMap<>();
        try (var manager = new ForwardingJavaFileManager<StandardJavaFileManager>(
                compiler.getStandardFileManager(diagnostics, null, null)) {
            @Override
            public JavaFileObject getJavaFileForOutput(Location location, String name,
                                                       JavaFileObject.Kind kind, FileObject sibling) {
                var output = new ByteArrayOutputStream();
                bytes.put(name, output);
                return new SimpleJavaFileObject(
                        URI.create("memory:///" + name.replace('.', '/') + kind.extension), kind) {
                    @Override
                    public OutputStream openOutputStream() {
                        return output;
                    }
                };
            }
        }) {
            List<JavaFileObject> files = new ArrayList<>();
            sources.forEach((name, source) -> files.add(new SimpleJavaFileObject(
                    URI.create("string:///" + name.replace('.', '/') + JavaFileObject.Kind.SOURCE.extension),
                    JavaFileObject.Kind.SOURCE) {
                @Override
                public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                    return source;
                }
            }));
            assertThat(compiler.getTask(null, manager, diagnostics,
                    List.of("-proc:none", "-classpath", System.getProperty("java.class.path")),
                    null, files).call())
                    .withFailMessage("生成代码编译失败：%s", diagnostics.getDiagnostics()).isTrue();
        }
        return new ClassLoader(getClass().getClassLoader()) {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                var output = bytes.get(name);
                if (output == null) {
                    return super.findClass(name);
                }
                var content = output.toByteArray();
                return defineClass(name, content, 0, content.length);
            }
        };
    }

    private Object newExample(ClassLoader loader) throws Exception {
        return loader.loadClass("sample.pojo.RecordExample").getDeclaredConstructor().newInstance();
    }

    private Object createCriteria(Object example) throws Exception {
        return example.getClass().getMethod("createCriteria").invoke(example);
    }

    private Object oredCriteria(Object example) throws Exception {
        return example.getClass().getMethod("getOredCriteria").invoke(example);
    }

    // ===== 生成 XML → BoundSql 渲染 =====

    private BoundSql render(IntrospectedTableMyBatis3Impl table, Object oredCriteria, String databaseId) {
        ExampleWhereClauseElementGenerator generator = new ExampleWhereClauseElementGenerator(false);
        generator.setContext(table.getContext());
        generator.setIntrospectedTable(table);
        XmlElement parent = new XmlElement("mapper");
        generator.addElements(parent);
        ElementRenderer renderer = new ElementRenderer();
        StringBuilder xml = new StringBuilder();
        for (VisitableElement element : parent.getElements()) {
            element.accept(renderer).forEach(line -> xml.append(line).append('\n'));
        }
        Configuration configuration = new Configuration();
        configuration.setDatabaseId(databaseId);
        String mapper = """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
                <mapper namespace="t.T">
                %s
                <select id="s" resultType="map">
                <include refid="Example_Where_Clause"/>
                </select>
                </mapper>
                """.formatted(xml);
        XMLMapperBuilder builder = new XMLMapperBuilder(
                new ByteArrayInputStream(mapper.getBytes(StandardCharsets.UTF_8)),
                configuration, "t06-integration.xml", configuration.getSqlFragments());
        builder.parse();
        MappedStatement statement = configuration.getMappedStatement("t.T.s");
        return statement.getBoundSql(Map.of("oredCriteria", oredCriteria));
    }

    // ===== 夹具 =====

    private FilterParam.ConditionItem optionItem(String key, String... values) {
        FilterParam.ConditionItem item = item(key);
        item.setOptions(new ArrayList<>(List.of(values)));
        return item;
    }

    private FilterParam.ConditionItem item(String key) {
        FilterParam.ConditionItem item = new FilterParam.ConditionItem();
        item.setKey(key);
        return item;
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

    /**
     * Example/XML 集成夹具：选项型组合条件 phase_group，where 子句片段标识齐备。
     */
    private IntrospectedTableMyBatis3Impl table() {
        Context context = context();
        JavaModelGeneratorConfiguration model = new JavaModelGeneratorConfiguration(context);
        model.setTargetPackage("sample.pojo");
        model.setTargetProject("target/test-project");
        model.setGenerate(true);
        context.setJavaModelGeneratorConfiguration(model);
        TableConfiguration tc = new TableConfiguration(context, new ArrayList<>());
        tc.setTableName("record_");
        tc.setDomainObjectName("Record");
        tc.setJavaModelGeneratorConfiguration(model);
        VoGeneratorConfiguration vo = new VoGeneratorConfiguration(context, tc);
        VoViewGeneratorConfiguration view = new VoViewGeneratorConfiguration(context, tc);
        view.getQueryColumns().add("phase_group");
        view.addQueryColumnConfigurations(optionConfig(tc));
        vo.setVoViewConfiguration(view);
        tc.setVoGeneratorConfiguration(vo);
        IntrospectedTableMyBatis3Impl table = new IntrospectedTableMyBatis3Impl();
        table.setContext(context);
        table.setTableConfiguration(tc);
        table.setFullyQualifiedTable(new FullyQualifiedTable(null, null, "record_", "Record", null,
                false, null, null, null, false, null, context));
        table.setExampleWhereClauseId("Example_Where_Clause");
        table.setMyBatis3UpdateByExampleWhereClauseId("Update_By_Example_Where_Clause");
        table.addColumn(column(context, "phase_state", "phaseState"));
        table.addColumn(column(context, "apply_state", "applyState"));
        table.addColumn(column(context, "del_flag", "delFlag"));
        table.initialize();
        return table;
    }

    private QueryColumnConfiguration optionConfig(TableConfiguration tc) {
        QueryColumnConfiguration condition = new QueryColumnConfiguration(tc);
        condition.setQueryType("condition");
        condition.setKey("phase_group");
        condition.setTagName(HtmlElementTagTypeEnum.CHECKBOX);
        condition.setMultiple(true);
        ConditionOptionConfiguration clear = new ConditionOptionConfiguration();
        clear.setValue("all");
        clear.setClear(true);
        ConditionOptionConfiguration completed = new ConditionOptionConfiguration();
        completed.setValue("completed");
        completed.getConditionTermConfigurations().add(term("phase_state", QueryConditionOperatorEnum.GT, "7"));
        completed.getConditionTermConfigurations().add(term("apply_state", QueryConditionOperatorEnum.EQ, "0"));
        ConditionOptionConfiguration notStarted = new ConditionOptionConfiguration();
        notStarted.setValue("not_started");
        notStarted.getConditionTermConfigurations().add(term("phase_state", QueryConditionOperatorEnum.LT, "3"));
        condition.getConditionOptionConfigurations().add(clear);
        condition.getConditionOptionConfigurations().add(completed);
        condition.getConditionOptionConfigurations().add(notStarted);
        return condition;
    }

    /**
     * 控制器接线夹具：与 QueryConditionControllerElementsTest 的 listFixture 同形。
     */
    private IntrospectedTableMyBatis3Impl listTable() {
        Context context = context();
        JavaModelGeneratorConfiguration model = new JavaModelGeneratorConfiguration(context);
        model.setTargetPackage("sample.pojo");
        model.setTargetProject("target/test-project");
        context.setJavaModelGeneratorConfiguration(model);
        TableConfiguration tc = new TableConfiguration(context, new ArrayList<>());
        tc.setTableName("record_");
        tc.setDomainObjectName("Record");
        tc.setAlias("r");
        VoGeneratorConfiguration vo = new VoGeneratorConfiguration(context, tc);
        VoRequestGeneratorConfiguration request = new VoRequestGeneratorConfiguration(context, tc);
        request.setIncludePageParam(false);
        vo.setVoRequestConfiguration(request);
        VoViewGeneratorConfiguration view = new VoViewGeneratorConfiguration(context, tc);
        view.getQueryColumns().add("phase_group");
        view.addQueryColumnConfigurations(optionConfig(tc));
        vo.setVoViewConfiguration(view);
        tc.setVoGeneratorConfiguration(vo);
        IntrospectedTableMyBatis3Impl table = new IntrospectedTableMyBatis3Impl();
        table.setContext(context);
        table.setTableConfiguration(tc);
        table.setBaseRecordType("sample.Support.Record");
        table.setExampleType("sample.Support.RecordExample");
        table.setRules(new ConditionalModelRules(table) {
            @Override
            public boolean isGenerateRequestVo() {
                return true;
            }

            @Override
            public boolean isGenerateViewVo() {
                return true;
            }

            @Override
            public boolean isIntegrateSpringSecurity() {
                return false;
            }
        });
        IntrospectedColumn name = column(context, "name_", "name");
        name.setTableAlias("r");
        name.setColumnNameDelimited(true);
        table.getBaseColumns().add(name);
        return table;
    }

    private ConditionTermConfiguration term(String column, QueryConditionOperatorEnum operator, String value) {
        ConditionTermConfiguration term = new ConditionTermConfiguration();
        term.setColumn(column);
        term.setOperator(operator);
        term.setValue(value);
        term.setValueType(QueryConditionValueTypeEnum.NUMBER);
        return term;
    }

    private IntrospectedColumn column(Context context, String physical, String property) {
        IntrospectedColumn column = new IntrospectedColumn();
        column.setContext(context);
        column.setActualColumnName(physical);
        column.setJavaProperty(property);
        column.setFullyQualifiedJavaType(FullyQualifiedJavaType.getStringInstance());
        column.setJdbcTypeName("VARCHAR");
        return column;
    }

    /**
     * 静默进度回调。
     */
    private static final class SilentProgress implements org.mybatis.generator.api.ProgressCallback {
    }
}
