package org.mybatis.generator.codegen.mybatis3.model;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.java.render.TopLevelClassRenderer;
import org.mybatis.generator.codegen.mybatis3.IntrospectedTableMyBatis3Impl;
import org.mybatis.generator.config.ConditionOptionConfiguration;
import org.mybatis.generator.config.ConditionTermConfiguration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.QueryColumnConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.VoGeneratorConfiguration;
import org.mybatis.generator.config.VoViewGeneratorConfiguration;
import org.mybatis.generator.internal.PluginAggregator;

import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.vgosoft.core.constant.enums.core.QueryConditionOperatorEnum;
import com.vgosoft.core.constant.enums.core.QueryConditionValueTypeEnum;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ExampleGenerator} 选项型组合条件分组 Criterion 与装配方法的生成测试：
 * 选项存在时生成 {@code orGroups}/{@code groupValue} 谓词成员与 {@code andBoundConditionGroup}，
 * 旧参数型条件不生成任何分组成员。
 *
 * @author vgosoft-agent
 */
class ExampleGeneratorOptionGroupTest {

    @Test
    void shouldEmitGroupCriterionMembersAndAssemblyMethod() {
        String source = render(optionTable(true));
        assertThat(source).contains(
                "private List<List<Criterion>> orGroups;",
                "private boolean groupValue;",
                "protected Criterion(List<List<Criterion>> orGroups)",
                "this.orGroups = orGroups;",
                "this.groupValue = true;",
                "public Criteria andBoundConditionGroup(List<List<BoundCondition>> orGroups)",
                "if (orGroups == null || orGroups.isEmpty()) {",
                "List<List<Criterion>> groups = new ArrayList<>();",
                "case VALUE -> atoms.add(new Criterion(bound.condition(), bound.value()));",
                "case LITERAL_LIKE -> atoms.add(new Criterion(bound.condition(), bound.value(), bound.sqlServerValue(), false));",
                "case TOKEN_LIKE -> atoms.add(new Criterion(bound.condition(), bound.value(), bound.sqlServerValue(), true));",
                "criteria.add(new Criterion(groups));");
        assertThat(source).contains("import com.vgosoft.web.utils.BoundCondition;");
        compileInMemory(source);
    }

    @Test
    void shouldNotEmitGroupMembersWithoutOptionConditions() {
        String source = render(optionTable(false));
        assertThat(source).doesNotContain("orGroups", "groupValue", "andBoundConditionGroup");
    }

    private String render(IntrospectedTableMyBatis3Impl table) {
        ExampleGenerator generator = new ExampleGenerator("src");
        generator.setContext(table.getContext());
        generator.setIntrospectedTable(table);
        generator.setProgressCallback(new SilentProgress());
        generator.setWarnings(new ArrayList<>());
        TopLevelClass topLevel = (TopLevelClass) generator.getCompilationUnits().get(0);
        return new TopLevelClassRenderer().render(topLevel);
    }

    private void compileInMemory(String source) {
        Map<String, String> sources = Map.of(
                "sample.pojo.RecordExample", source,
                "com.vgosoft.web.utils.BoundCondition", """
                        package com.vgosoft.web.utils;
                        public record BoundCondition(Kind kind, String condition, Object value, String sqlServerValue) {
                            public enum Kind { VALUE, LITERAL_LIKE, TOKEN_LIKE }
                        }
                        """);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertThat(compiler).as("生成代码编译回归必须使用完整 JDK").isNotNull();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager standard = compiler.getStandardFileManager(diagnostics, null, null);
             var manager = new ForwardingJavaFileManager<StandardJavaFileManager>(standard) {
                 @Override
                 public JavaFileObject getJavaFileForOutput(Location location, String name,
                                                            JavaFileObject.Kind kind, FileObject sibling) {
                     return new SimpleJavaFileObject(
                             URI.create("memory:///" + name.replace('.', '/') + kind.extension), kind) {
                         @Override
                         public OutputStream openOutputStream() {
                             return new ByteArrayOutputStream();
                         }
                     };
                 }
             }) {
            List<JavaFileObject> inputs = sources.entrySet().stream().map(entry -> (JavaFileObject)
                    new SimpleJavaFileObject(URI.create("string:///" + entry.getKey().replace('.', '/') + ".java"),
                            JavaFileObject.Kind.SOURCE) {
                        @Override
                        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                            return entry.getValue();
                        }
                    }).toList();
            assertThat(compiler.getTask(null, manager, diagnostics,
                    List.of("-proc:none", "-classpath", System.getProperty("java.class.path")), null, inputs).call())
                    .withFailMessage("Example 选项分组生成源码编译失败：%s", diagnostics.getDiagnostics())
                    .isTrue();
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Example 选项分组生成源码编译失败", e);
        }
    }

    private IntrospectedTableMyBatis3Impl optionTable(boolean withOptions) {
        Context context = new Context(ModelType.CONDITIONAL);
        try {
            var plugins = Context.class.getDeclaredField("pluginAggregator");
            plugins.setAccessible(true);
            plugins.set(context, new PluginAggregator());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
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
        view.getQueryColumns().add("state_filter");
        QueryColumnConfiguration condition = new QueryColumnConfiguration(tc);
        condition.setQueryType("condition");
        condition.setKey("state_filter");
        if (withOptions) {
            ConditionOptionConfiguration clear = new ConditionOptionConfiguration();
            clear.setValue("all");
            clear.setClear(true);
            ConditionOptionConfiguration done = new ConditionOptionConfiguration();
            done.setValue("done");
            ConditionTermConfiguration term = new ConditionTermConfiguration();
            term.setColumn("phase_state");
            term.setOperator(QueryConditionOperatorEnum.GT);
            term.setValue("7");
            term.setValueType(QueryConditionValueTypeEnum.NUMBER);
            done.getConditionTermConfigurations().add(term);
            condition.getConditionOptionConfigurations().add(clear);
            condition.getConditionOptionConfigurations().add(done);
        }
        view.addQueryColumnConfigurations(condition);
        vo.setVoViewConfiguration(view);
        tc.setVoGeneratorConfiguration(vo);
        IntrospectedTableMyBatis3Impl table = new IntrospectedTableMyBatis3Impl();
        table.setContext(context);
        table.setTableConfiguration(tc);
        table.setFullyQualifiedTable(new FullyQualifiedTable(null, null, "record_", "Record", null,
                false, null, null, null, false, null, context));
        table.addColumn(column(context, "phase_state", "phaseState"));
        table.initialize();
        return table;
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
