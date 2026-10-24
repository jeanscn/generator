package org.mybatis.generator.codegen.mybatis3;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.DefaultJavaFormatter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.api.dom.xml.render.ElementRenderer;
import org.mybatis.generator.codegen.mybatis3.po.PoCacheGenerator;
import org.mybatis.generator.codegen.mybatis3.service.JavaServiceGenerator;
import org.mybatis.generator.codegen.mybatis3.service.JavaServiceImplGenerator;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.ObjectFactory;
import org.mybatis.generator.internal.PluginAggregator;
import org.mybatis.generator.internal.rules.FlatModelRules;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.SelectByKeysDictElementGenerator;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 以内省替身真实渲染字典 SQL、模型和服务，并离线编译公开入口及共用查询。
 *
 * @author vgosoft-agent
 */
class DictPrefetchGenerationTest {

    /** 默认收集排序和创建时间，再追加全部主键；新增元数据不改变原业务字段可见性。 */
    @Test
    void rendersDefaultOrderAndPreservesBusinessJsonVisibility() throws Exception {
        var table = fixture();
        assertOrder(table, "sort_", "created_", "id_");
        assertThat(sql(table)).contains(
                "ORDER BY CASE WHEN sort_ IS NULL THEN 0 ELSE 1 END ASC, sort_ ASC, "
                        + "CASE WHEN created_ IS NULL THEN 0 ELSE 1 END ASC, created_ ASC, "
                        + "CASE WHEN id_ IS NULL THEN 0 ELSE 1 END ASC, id_ ASC");
        TopLevelClass po = po(table);
        assertThat(field(po, "sort").getAnnotations()).contains("@JsonIgnore");
        assertThat(field(po, "created").getAnnotations()).contains("@JsonIgnore");
        assertThat(field(po, "id").getAnnotations()).doesNotContain("@JsonIgnore");
        assertThat(po.getMethods()).extracting(Method::getName).doesNotContain("compareTo");
        var config = table.getTableConfiguration().getVoCacheGeneratorConfiguration();
        config.getIncludeColumns().add("sort_");
        assertThat(field(po(table), "sort").getAnnotations()).doesNotContain("@JsonIgnore");
        config.getIncludeColumns().clear();
        assertThat(field(po(table), "created").getAnnotations()).doesNotContain("@JsonIgnore");
        assertThat(format(implementation(table))).contains(
                "orderProperties = {\"sort\", \"created\", \"id\"}", "uniqueOrderProperties = {\"id\"}");
    }

    /** 显式排序不移动中间主键，并按内省顺序补全联合主键；显式隐藏行为保留。 */
    @Test
    void preservesExplicitOrderAndAppendsOnlyMissingCompositePrimaryKeys() throws Exception {
        var table = fixture();
        table.addPrimaryKeyColumn("type_");
        var config = table.getTableConfiguration().getVoCacheGeneratorConfiguration();
        config.setDictCacheOrderColumns(List.of("SORT_", "id_", "created_"));
        assertOrder(table, "sort_", "id_", "created_", "type_");
        assertThat(sql(table)).contains("created_ ASC, CASE WHEN type_ IS NULL");
        assertThat(format(implementation(table))).contains(
                "orderProperties = {\"sort\", \"id\", \"created\", \"type\"}",
                "uniqueOrderProperties = {\"id\", \"type\"}");
        assertThat(field(po(table), "sort").getAnnotations()).contains("@JsonIgnore");
    }

    /** 丢失列及同一物理列的不同大小写重复都必须失败，不能在三个渲染层产生不同解释。 */
    @Test
    void rejectsMissingDuplicateAndMissingKeyColumns() throws Exception {
        var table = fixture();
        var config = table.getTableConfiguration().getVoCacheGeneratorConfiguration();
        for (List<String> names : List.of(List.of("missing_"), List.of("sort_", "SORT_"))) {
            config.setDictCacheOrderColumns(names);
            assertThatThrownBy(() -> DictCacheOrderResolver.resolve(table)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> sql(table)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> po(table)).isInstanceOf(IllegalArgumentException.class);
        }
        config.setDictCacheOrderColumns(List.of());
        config.setKeyColumn("missing_");
        assertThatThrownBy(() -> DictCacheOrderResolver.resolve(table)).hasMessageContaining("keyColumn");
    }

    /** 没有默认列时只用主键；无主键和无排序时仍生成两个入口并明确输出空唯一依据。 */
    @Test
    void supportsMissingDefaultsAndTablesWithoutPrimaryKeys() throws Exception {
        var table = fixture();
        table.getBaseColumns().removeIf(column -> List.of("sort_", "created_").contains(column.getActualColumnName()));
        assertOrder(table, "id_");
        table.getBaseColumns().addAll(table.getPrimaryKeyColumns());
        table.getPrimaryKeyColumns().clear();
        assertOrder(table);
        assertThat(sql(table)).doesNotContain("ORDER BY");
        assertThat(format(implementation(table))).contains(
                "orderProperties = {}", "uniqueOrderProperties = {}", "selectByKeysDictForPrefetch");
        table.getTableConfiguration().getVoCacheGeneratorConfiguration().setDictCacheOrderColumns(List.of("name_"));
        assertOrder(table, "name_");
        assertThat(format(implementation(table))).contains("uniqueOrderProperties = {}");
    }

    /** 排序与条件一样复用物理列转义和别名，不增加方言分组。 */
    @Test
    void escapesDelimitedOrderColumnsAndAliases() throws Exception {
        var table = fixture();
        IntrospectedColumn sort = table.getColumn("sort_").orElseThrow();
        sort.setColumnNameDelimited(true);
        table.getContext().addProperty("beginningDelimiter", "\"");
        table.getContext().addProperty("endingDelimiter", "\"");
        table.getTableConfiguration().setAlias("a");
        sort.setTableAlias("a");
        assertThat(sql(table)).contains("CASE WHEN a.\"sort_\" IS NULL THEN 0 ELSE 1 END ASC, a.\"sort_\" ASC");
    }

    /** 接口与实现共用签名、保留缓存，仅私有查询访问 Mapper，并编译字符串和数值类型组合。 */
    @Test
    void compilesBothEntrypointsWithOnePrivateQueryAndTypedConversions() throws Exception {
        for (String type : List.of("java.lang.String", "java.math.BigDecimal", "java.lang.Long")) {
            var table = fixture();
            for (String name : List.of("id_", "type_")) {
                IntrospectedColumn column = table.getColumn(name).orElseThrow();
                column.setFullyQualifiedJavaType(new FullyQualifiedJavaType(type));
                column.setJdbcType(type.equals("java.lang.String") ? Types.VARCHAR : Types.NUMERIC);
            }
            TopLevelClass implementation = implementation(table);
            Interface contract = contract(table);
            List<Method> methods = implementation.getMethods();
            assertThat(methods.stream().filter(m -> !m.getName().equals("vgosoftCacheKeyJson"))).extracting(Method::getName)
                    .containsExactly("selectByKeysDict", "selectByKeysDictForPrefetch", "queryByKeysDict");
            assertThat(method(methods, "selectByKeysDict").getAnnotations().toString()).contains("@Cacheable");
            Method prefetch = method(methods, "selectByKeysDictForPrefetch");
            assertThat(prefetch.getAnnotations().toString()).doesNotContain("@Cacheable");
            assertThat(prefetch.getBodyLines()).containsExactly("return queryByKeysDict(types, keys);");
            assertThat(method(methods, "queryByKeysDict").getVisibility()).isEqualTo(JavaVisibility.PRIVATE);
            assertThat(method(methods, "queryByKeysDict").getAnnotations()).isEmpty();
            assertThat(format(implementation).split("mapper.selectByKeysDict", -1)).hasSize(2);
            for (Method declaration : contract.getMethods()) {
                Method actual = method(methods, declaration.getName());
                assertThat(actual.getReturnType()).isEqualTo(declaration.getReturnType());
                assertThat(actual.getParameters().stream().map(p -> p.getType().getFullyQualifiedName() + " " + p.getName()))
                        .containsExactlyElementsOf(declaration.getParameters().stream()
                                .map(p -> p.getType().getFullyQualifiedName() + " " + p.getName()).toList());
                assertThat(actual.getExceptions()).isEqualTo(declaration.getExceptions());
            }
            if (type.equals("java.math.BigDecimal")) {
                assertThat(format(implementation)).contains("keys.toPlainString()", "types.map(value -> value.toPlainString())");
            } else if (type.equals("java.lang.String")) {
                assertThat(method(methods, "selectByKeysDict").getBodyLines())
                        .containsExactly("return queryByKeysDict(types, keys);");
            } else {
                assertThat(format(implementation)).contains("String.valueOf(keys)", "types.map(value -> String.valueOf(value))");
            }
            compile(contract, implementation);
        }
    }

    /** 关闭 CachePo 时真实装配入口不得生成公开或私有字典查询。 */
    @Test
    void omitsDictionaryMethodsWhenCachePoIsDisabled() throws Exception {
        var table = fixture();
        table.getTableConfiguration().getVoCacheGeneratorConfiguration().setGenerate(false);
        assertThat(implementation(table).getMethods()).isEmpty();
        assertThat(contract(table).getMethods()).isEmpty();
    }

    private IntrospectedTableMyBatis3Impl fixture() throws Exception {
        String xml;
        try (var stream = getClass().getResourceAsStream("/dict-prefetch-generator.xml")) {
            xml = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        Context context = new ConfigurationParser(new ArrayList<>()).parseConfiguration(new StringReader(xml))
                .getContexts().get(0);
        var plugins = Context.class.getDeclaredField("pluginAggregator");
        plugins.setAccessible(true);
        plugins.set(context, new PluginAggregator());
        var table = (IntrospectedTableMyBatis3Impl) ObjectFactory.createIntrospectedTable(
                context.getTableConfigurations().get(0), new FullyQualifiedTable(null, null, "article_", "Article",
                        null, true, null, null, null, false, null, context), context);
        table.setRemarks("文章");
        for (String property : List.of("id", "type", "name", "sort", "created")) {
            IntrospectedColumn column = new IntrospectedColumn();
            column.setActualColumnName(property + "_");
            column.setJavaProperty(property);
            column.setFullyQualifiedJavaType(property.equals("sort")
                    ? FullyQualifiedJavaType.getIntegerInstance() : FullyQualifiedJavaType.getStringInstance());
            column.setJdbcType(property.equals("sort") ? Types.INTEGER : Types.VARCHAR);
            column.setJdbcTypeName(property.equals("sort") ? "INTEGER" : "VARCHAR");
            column.setRemarks("测试列");
            column.setContext(context);
            table.addColumn(column);
        }
        table.addPrimaryKeyColumn("id_");
        table.initialize();
        table.setRules(new FlatModelRules(table));
        return table;
    }

    private void assertOrder(IntrospectedTableMyBatis3Impl table, String... names) {
        assertThat(DictCacheOrderResolver.resolve(table)).extracting(IntrospectedColumn::getActualColumnName)
                .containsExactly(names);
    }

    private TopLevelClass implementation(IntrospectedTableMyBatis3Impl table) throws Exception {
        JavaServiceImplGenerator generator = new JavaServiceImplGenerator("src");
        generator.setContext(table.getContext());
        generator.setIntrospectedTable(table);
        generator.setProgressCallback(new ProgressCallback() { });
        generator.setWarnings(new ArrayList<>());
        TopLevelClass target = new TopLevelClass("sample.DictService");
        target.setVisibility(JavaVisibility.PUBLIC);
        target.addImportedType(table.getBaseRecordType());
        var add = JavaServiceImplGenerator.class.getDeclaredMethod("addSelectByKeysDictElement", TopLevelClass.class);
        add.setAccessible(true);
        add.invoke(generator, target);
        return target;
    }

    private Interface contract(IntrospectedTableMyBatis3Impl table) {
        JavaServiceGenerator generator = new JavaServiceGenerator("src");
        generator.setContext(table.getContext());
        generator.setIntrospectedTable(table);
        generator.setProgressCallback(new ProgressCallback() { });
        generator.setWarnings(new ArrayList<>());
        Interface generated = (Interface) generator.getCompilationUnits().get(0);
        Interface target = new Interface("sample.DictContract");
        target.setVisibility(JavaVisibility.PUBLIC);
        target.addImportedTypes(generated.getImportedTypes());
        generated.getMethods().stream().filter(m -> m.getName().startsWith("selectByKeysDict")).forEach(target::addMethod);
        return target;
    }

    private TopLevelClass po(IntrospectedTableMyBatis3Impl table) {
        return new PoCacheGenerator(table, "src", new ProgressCallback() { }, new ArrayList<>(),
                new Interface("sample.pojo.maps.ArticleCachePoMappings")).generate();
    }

    private String sql(IntrospectedTableMyBatis3Impl table) {
        SelectByKeysDictElementGenerator generator = new SelectByKeysDictElementGenerator();
        generator.setContext(table.getContext());
        generator.setIntrospectedTable(table);
        XmlElement root = new XmlElement("mapper");
        generator.addElements(root);
        return new ElementRenderer().visit(root).collect(java.util.stream.Collectors.joining("\n"));
    }

    private Field field(TopLevelClass po, String name) {
        return po.getFields().stream().filter(field -> field.getName().equals(name)).findFirst().orElseThrow();
    }

    private Method method(List<Method> methods, String name) {
        return methods.stream().filter(method -> method.getName().equals(name)).findFirst().orElseThrow();
    }

    private String format(CompilationUnit unit) {
        return new DefaultJavaFormatter().getFormattedContent(unit);
    }

    private void compile(Interface contract, TopLevelClass implementation) throws Exception {
        implementation.addSuperInterface(contract.getType());
        Field mapper = new Field("mapper", new FullyQualifiedJavaType("sample.Mapper"));
        implementation.addField(mapper);
        Map<String, String> sources = new java.util.LinkedHashMap<>();
        sources.put("sample.DictContract", format(contract));
        sources.put("sample.DictService", format(implementation));
        sources.put("sample.Mapper", """
                package sample;
                public class Mapper {
                    public java.util.List<sample.entity.Article> selectByKeysDict(
                            com.vgosoft.core.pojo.parameter.SelDictByKeysParam param) { return java.util.List.of(); }
                }
                """);
        sources.put("sample.entity.Article", "package sample.entity; public class Article {}");
        sources.put("sample.entity.ArticleExample", "package sample.entity; public class ArticleExample {}");
        sources.put("sample.pojo.po.ArticleCachePo", "package sample.pojo.po; public class ArticleCachePo {}");
        sources.put("sample.pojo.maps.ArticleCachePoMappings", """
                package sample.pojo.maps;
                public interface ArticleCachePoMappings {
                    default java.util.List<sample.pojo.po.ArticleCachePo> toArticleCachePos(
                            java.util.List<sample.entity.Article> rows) { return java.util.List.of(); }
                }
                """);
        sources.put("com.vgosoft.core.annotation.DictPrefetchProvider", """
                package com.vgosoft.core.annotation;
                public @interface DictPrefetchProvider {
                    String keyProperty(); String[] orderProperties(); String[] uniqueOrderProperties() default {};
                }
                """);
        var compiler = ToolProvider.getSystemJavaCompiler();
        assertThat(compiler).isNotNull();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (var standard = compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8);
             var manager = new ForwardingJavaFileManager<StandardJavaFileManager>(standard) {
                 @Override
                 public JavaFileObject getJavaFileForOutput(Location location, String name,
                                                            JavaFileObject.Kind kind, FileObject sibling) {
                     return new SimpleJavaFileObject(URI.create("memory:///" + name.replace('.', '/') + kind.extension), kind) {
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
                    .withFailMessage("字典服务渲染结果编译失败：%s", diagnostics.getDiagnostics()).isTrue();
        }
    }
}
