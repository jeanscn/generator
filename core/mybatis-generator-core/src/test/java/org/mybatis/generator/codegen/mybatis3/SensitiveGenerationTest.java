package org.mybatis.generator.codegen.mybatis3;

import com.github.javaparser.StaticJavaParser;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.DefaultJavaFormatter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.model.BaseRecordGenerator;
import org.mybatis.generator.codegen.mybatis3.service.JavaServiceImplGenerator;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.SensitiveCheckConfiguration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.internal.PluginAggregator;
import org.mybatis.generator.internal.rules.FlatModelRules;
import org.mybatis.generator.internal.rules.HierarchicalModelRules;
import org.mybatis.generator.config.EnableCacheConfiguration;
import org.mybatis.generator.config.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.config.ColumnOverride;
import org.mybatis.generator.config.OverridePropertyValueGeneratorConfiguration;

import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * 在内存中验证敏感配置、实体、受管写方法和前端映射，不连接数据库或写业务产物。
 *
 * @author vgosoft
 */
class SensitiveGenerationTest {
    private static final String XML = """
            <!DOCTYPE generatorConfiguration PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
              "mybatis-generator-config_1_8_0.dtd">
            <generatorConfiguration>
              <context id="sensitive-test" defaultModelType="flat" targetRuntime="MyBatis3"
                appKeyword="sample" moduleKeyword="sample" moduleName="测试" jdkVersion="17" integrateMybatisPlus="false">
                <jdbcConnection driverClass="must.not.connect.Driver" connectionURL="jdbc:must-not-connect:test"/>
                <javaModelGenerator targetPackage="sample.entity" targetProject="src"/>
                <sqlMapGenerator targetPackage="sample.mapper" targetProject="src"/>
                <javaClientGenerator targetPackage="sample.dao" targetProject="src" type="XMLMAPPER"/>
                <table tableName="article_" domainObjectName="Article" enableUpdateByExample="false">
                  <property name="rootClass" value="com.vgosoft.core.entity.abs.AbstractEntity"/>
                  <generateModel generate="true"/>
                  <generateService generate="true" generateUnitTest="false"/>
                  <generateController generate="false"/>
                  <generateSqlSchema generate="false"/>
                  <generateVo generate="false"/>
                  %s
                </table>
              </context>
            </generatorConfiguration>
            """;
    private static final String CHECK = "<sensitiveCheck columns=\"content_\"/>";

    @Test
    void parsesCurrentDtdDefaultsAndRejectsInvalidValues() throws Exception {
        var table = parse(CHECK).getTableConfigurations().get(0);
        var check = table.getSensitiveCheckConfigurations().get(0);
        assertThat(check.getColumns()).containsExactly("content_");
        assertThat(check.isEnable()).isTrue();
        assertThat(check.getStrategy()).isEqualTo("INHERIT");
        assertThat(check.getContentType()).isEqualTo("PLAIN_TEXT");
        for (String invalid : List.of("enable=\"yes\"", "strategy=\"BLOCKED\"", "contentType=\"HTML\"")) {
            assertThatThrownBy(() -> parse(CHECK.replace("/>", " " + invalid + "/>")))
                    .isInstanceOf(XMLParserException.class);
        }
        assertThatThrownBy(() -> parse("<sensitiveCheck columns=\"content_,content_\"/>"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("article_").hasMessageContaining("重复");
        assertThatThrownBy(() -> new SensitiveCheckConfiguration("content_", "yes", null, null))
                .hasMessageContaining("enable");
    }

    @Test
    void resolvesRenamedPropertiesAndRejectsMissingIgnoredDuplicateAndNonStringColumns() throws Exception {
        IntrospectedTableMyBatis3Impl table = fixture(CHECK);
        table.getAllColumns().stream().filter(column -> "content_".equals(column.getActualColumnName()))
                .findFirst().orElseThrow().setJavaProperty("bodyText");
        SensitiveGeneration.validate(table);
        assertThat(SensitiveGeneration.frontendFields(table).get(0).get("property")).isEqualTo("bodyText");
        for (String name : List.of("missing_", "ignored_", "bodyText")) {
            assertThatThrownBy(() -> fixture("<sensitiveCheck columns=\"" + name + "\"/>"))
                    .hasMessageContaining("article_").hasMessageContaining(name);
        }
        table.getBaseColumns().get(0).setFullyQualifiedJavaType(FullyQualifiedJavaType.getIntegerInstance());
        assertThatThrownBy(() -> SensitiveGeneration.validate(table)).hasMessageContaining("String");
        IntrospectedTableMyBatis3Impl duplicate = fixture(CHECK);
        duplicate.getTableConfiguration().getSensitiveCheckConfigurations()
                .add(new SensitiveCheckConfiguration("content_", null, "WARN", null));
        assertThatThrownBy(() -> SensitiveGeneration.validate(duplicate)).hasMessageContaining("重复");
        for (String property : List.of("class", "id", "not-valid")) {
            IntrospectedTableMyBatis3Impl invalidProperty = fixture(CHECK);
            invalidProperty.getBaseColumns().get(0).setJavaProperty(property);
            assertThatThrownBy(() -> SensitiveGeneration.validate(invalidProperty)).hasMessageContaining("Java 属性");
        }
    }

    @Test
    void rejectsInheritedFieldsUnsupportedModelsServicesAndUnrestrictedExampleUpdates() throws Exception {
        assertThatThrownBy(() -> fixture("<sensitiveCheck columns=\"id_\"/>"))
                .hasMessageContaining("父类").hasMessageContaining("id_");
        IntrospectedTableMyBatis3Impl table = fixture(CHECK);
        table.getTableConfiguration().getJavaServiceImplGeneratorConfiguration().setGenerate(false);
        assertThatThrownBy(() -> SensitiveGeneration.validate(table)).hasMessageContaining("Service");
        table.getTableConfiguration().getJavaServiceImplGeneratorConfiguration().setGenerate(true);
        table.getTableConfiguration().setUpdateByExampleStatementEnabled(true);
        assertThatThrownBy(() -> SensitiveGeneration.validate(table)).hasMessageContaining("updateByExample");
        table.getTableConfiguration().setUpdateByExampleStatementEnabled(false);
        table.getTableConfiguration().getJavaModelGeneratorConfiguration().setGenerate(false);
        assertThatThrownBy(() -> SensitiveGeneration.validate(table)).hasMessageContaining("模型");
        table.getTableConfiguration().getJavaModelGeneratorConfiguration().setGenerate(true);
        table.setRules(new HierarchicalModelRules(table));
        assertThatThrownBy(() -> SensitiveGeneration.validate(table)).hasMessageContaining("分层模型");
    }

    @Test
    void rendersActualModelAndServiceWithCompleteMethodMatrixAndStableRegeneration() throws Exception {
        IntrospectedTableMyBatis3Impl table = fixture(CHECK);
        TopLevelClass model = model(table);
        Field field = model.getFields().stream().filter(item -> "bodyText".equals(item.getName())).findFirst().orElseThrow();
        assertThat(field.getAnnotations()).contains(
                "@SensitiveCheck(enable = true, strategy = SensitiveCheckStrategy.INHERIT, contentType = SensitiveContentType.PLAIN_TEXT)");
        String before = format(model);
        SensitiveGeneration.addModelAnnotations(model, table);
        assertThat(format(model)).isEqualTo(before);
        StaticJavaParser.parse(before);

        TopLevelClass service = service(table);
        List<String> writes = List.of("insert", "insertSelective", "insertBatch", "updateByPrimaryKey",
                "updateByPrimaryKeySelective", "updateBatchByPrimaryKey", "insertOrUpdate", "insertOrUpdateSelective");
        for (String name : writes) {
            Method method = service.getMethods().stream().filter(item -> name.equals(item.getName())).findFirst().orElseThrow();
            assertThat(method.getAnnotations()).contains(
                    "@SensitiveWrite(operationKey = \"sample.entity.Article:" + name + "\", entityArgumentIndex = 0)");
            assertThat(method.getAnnotations().stream().filter(value -> value.startsWith("@Transactional")).toList())
                    .hasSize(1).allSatisfy(value -> assertThat(value).contains("rollbackFor = Exception.class"));
        }
        assertThat(service.getMethods().stream().filter(method -> !writes.contains(method.getName()))
                .flatMap(method -> method.getAnnotations().stream())).noneMatch(value -> value.startsWith("@SensitiveWrite"));
        String source = format(service);
        StaticJavaParser.parse(source);
        SensitiveGeneration.addWriteAnnotations(service, table);
        assertThat(format(service)).isEqualTo(source);
        assertThat(format(service(fixture(CHECK)))).isEqualTo(source);
    }

    @Test
    void ignoresUnconfiguredAndDisabledSensitiveOutput() throws Exception {
        IntrospectedTableMyBatis3Impl plain = fixture("");
        TopLevelClass model = model(plain);
        TopLevelClass service = service(plain);
        String modelBefore = format(model);
        String serviceBefore = format(service);
        SensitiveGeneration.addModelAnnotations(model, plain);
        SensitiveGeneration.addWriteAnnotations(service, plain);
        assertThat(format(model)).isEqualTo(modelBefore).doesNotContain("Sensitive");
        assertThat(format(service)).isEqualTo(serviceBefore).doesNotContain("Sensitive");
        IntrospectedTableMyBatis3Impl disabled = fixture("<sensitiveCheck columns=\"content_\" enable=\"false\" strategy=\"WARN\"/>");
        TopLevelClass disabledModel = model(disabled);
        TopLevelClass disabledService = service(disabled);
        SensitiveGeneration.addModelAnnotations(disabledModel, disabled);
        SensitiveGeneration.addWriteAnnotations(disabledService, disabled);
        assertThat(format(disabledModel)).doesNotContain("Sensitive");
        assertThat(format(disabledService)).doesNotContain("Sensitive");
    }

    @Test
    void refusesSuppressedFieldsAndWrongBatchParameterRoles() throws Exception {
        IntrospectedTableMyBatis3Impl table = fixture(CHECK);
        assertThatThrownBy(() -> SensitiveGeneration.addModelAnnotations(new TopLevelClass("sample.Article"), table))
                .hasMessageContaining("content_").hasMessageContaining("未实际生成");
        assertThatThrownBy(() -> SensitiveGeneration.requireModelGenerated(table)).hasMessageContaining("插件");
        assertThatThrownBy(() -> SensitiveGeneration.requireServiceGenerated(table)).hasMessageContaining("插件");
        TopLevelClass service = service(table);
        Method batch = service.getMethods().stream().filter(item -> "insertBatch".equals(item.getName())).findFirst().orElseThrow();
        batch.getParameters().clear();
        batch.addParameter(new Parameter(new FullyQualifiedJavaType("java.util.List<java.lang.String>"), "records"));
        assertThatThrownBy(() -> SensitiveGeneration.addWriteAnnotations(service, table))
                .hasMessageContaining("insertBatch").hasMessageContaining("List");
    }

    @Test
    void rendersEscapedFrontendMappingAndKeepsUnconfiguredTemplateBytes() throws Exception {
        IntrospectedTableMyBatis3Impl table = fixture(CHECK);
        table.getBaseColumns().get(0).setRemarks("内容\"\\\n标签");
        List<Map<String, String>> mapping = SensitiveGeneration.frontendFields(table);
        assertThat(mapping).hasSize(1);
        assertThat(mapping.get(0)).containsEntry("table", "article_").containsEntry("entity", "sample.entity.Article")
                .containsEntry("property", "bodyText").containsEntry("contentType", "PLAIN_TEXT");
        assertThat(render(mapping)).contains("export const TArticleSensitiveFields", "内容\\\"\\\\\\n标签");
        assertThat(render(List.of())).isEqualTo(render(null)).doesNotContain("SensitiveFields");
    }

    @Test
    void supportsPlainTextLongvarcharWithoutConvertingBytesOrAnnotatingVoFields() throws Exception {
        IntrospectedTableMyBatis3Impl table = fixture(CHECK);
        IntrospectedColumn content = table.getBaseColumns().remove(0);
        content.setJdbcType(Types.LONGVARCHAR);
        content.setJdbcTypeName("LONGVARCHAR");
        table.addColumn(content);
        SensitiveGeneration.validate(table);
        assertThat(format(model(table))).contains("@SensitiveCheck", "String bodyText");
        content.setFullyQualifiedJavaType(new FullyQualifiedJavaType("byte[]"));
        assertThatThrownBy(() -> SensitiveGeneration.validate(table)).hasMessageContaining("String");
    }

    @Test
    void rejectsLogicalJsonDerivedRepresentationsAndGeneratedAlwaysColumns() throws Exception {
        IntrospectedTableMyBatis3Impl table = fixture(CHECK);
        ColumnOverride override = new ColumnOverride("content_");
        override.addProperty("logicalType", "JSON");
        table.getTableConfiguration().addColumnOverride(override);
        assertThatThrownBy(() -> SensitiveGeneration.validate(table)).hasMessageContaining("适配器");
        table.getTableConfiguration().getColumnOverrides().clear();
        table.getTableConfiguration().getOverridePropertyConfigurations().add(
                new OverridePropertyValueGeneratorConfiguration(table.getContext(), table.getTableConfiguration(), "content_"));
        assertThatThrownBy(() -> SensitiveGeneration.validate(table)).hasMessageContaining("双表示");
        table.getTableConfiguration().getOverridePropertyConfigurations().clear();
        table.getBaseColumns().get(0).setGeneratedAlways(true);
        assertThatThrownBy(() -> SensitiveGeneration.validate(table)).hasMessageContaining("可写列");
    }

    @Test
    void rejectsNonProxyableMethodsAndUnverifiedTransactionPropagation() throws Exception {
        IntrospectedTableMyBatis3Impl table = fixture(CHECK);
        TopLevelClass generated = service(table);
        Method insert = generated.getMethods().stream().filter(method -> "insert".equals(method.getName())).findFirst().orElseThrow();
        insert.setFinal(true);
        assertThatThrownBy(() -> SensitiveGeneration.addWriteAnnotations(generated, table)).hasMessageContaining("可代理");
        insert.setFinal(false);
        insert.getAnnotations().removeIf(value -> value.startsWith("@Transactional"));
        insert.addAnnotation("@Transactional(propagation = Propagation.NOT_SUPPORTED)");
        assertThatThrownBy(() -> SensitiveGeneration.addWriteAnnotations(generated, table)).hasMessageContaining("事务声明");
    }

    @Test
    void preservesCacheAndEntityEventAnnotationsOnManagedWrites() throws Exception {
        IntrospectedTableMyBatis3Impl table = fixture(CHECK);
        table.getTableConfiguration().getJavaServiceImplGeneratorConfiguration().getEntityEvent()
                .addAll(List.of("PRE_INSERT", "INSERTED", "PRE_UPDATE", "UPDATED"));
        SelectByTableGeneratorConfiguration query = new SelectByTableGeneratorConfiguration();
        query.setTableName("article_tag");
        query.setEnableSplit(false);
        query.setEnableUnion(false);
        query.setThisColumn(column("id_", "id"));
        query.setParameterName("recordIds");
        EnableCacheConfiguration cache = new EnableCacheConfiguration();
        cache.setCacheNames(List.of("articleTags#72h"));
        query.getCacheConfigurationList().add(cache);
        table.getTableConfiguration().getSelectByTableGeneratorConfiguration().add(query);
        TopLevelClass generated = service(table);
        for (Method method : generated.getMethods().stream()
                .filter(item -> List.of("insert", "insertSelective", "updateByPrimaryKey",
                        "insertOrUpdateSelective").contains(item.getName())).toList()) {
            assertThat(method.getAnnotations().toString()).contains("@SensitiveWrite", "@Transactional", "articleTags#72h");
            assertThat(method.getAnnotations().stream().filter(value -> value.startsWith("@Transactional")).count()).isEqualTo(1);
        }
        assertThat(format(generated)).contains("publisher.publishEvent", "EntityEventEnum.PRE_INSERT");
        StaticJavaParser.parse(format(generated));
    }

    @Test
    void compilesRenderedAnnotationMetadataAgainstActualConsumerClasspathInMemory() throws Exception {
        IntrospectedTableMyBatis3Impl table = fixture(CHECK);
        Field field = model(table).getFields().stream().filter(item -> "bodyText".equals(item.getName())).findFirst().orElseThrow();
        StringBuilder source = new StringBuilder("""
                package sample;
                import com.vgosoft.core.annotation.sensitive.*;
                import org.springframework.transaction.annotation.*;
                /**
                 * 验证生成注解和事务元数据可由基础依赖消费。
                 * @author vgosoft
                 */
                public class MetadataProbe {
                """);
        source.append(String.join("\n", field.getAnnotations().stream().filter(value -> value.startsWith("@SensitiveCheck")).toList()));
        source.append("\nprivate String bodyText;\n");
        for (Method method : service(table).getMethods()) {
            if (method.getAnnotations().stream().noneMatch(value -> value.startsWith("@SensitiveWrite"))) {
                continue;
            }
            source.append(String.join("\n", method.getAnnotations().stream()
                    .filter(value -> value.startsWith("@SensitiveWrite") || value.startsWith("@Transactional")).toList()));
            String parameter = method.getName().contains("Batch") ? "java.util.List<MetadataProbe>" : "MetadataProbe";
            source.append("\npublic void ").append(method.getName()).append("(").append(parameter).append(" records) {}\n");
        }
        source.append("}\n");
        var compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (var manager = new MemoryFileManager(compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8))) {
            boolean success = compiler.getTask(null, manager, diagnostics,
                    List.of("-proc:none", "--release", "17", "-classpath", System.getProperty("java.class.path")),
                    null, List.of(new MemorySource(source.toString()))).call();
            assertThat(success).withFailMessage(diagnostics.getDiagnostics().toString()).isTrue();
            assertThat(manager.bytecode.size()).isPositive();
        }
    }

    @Test
    void parsesConsumerManagementConfigurationWithoutDatabaseOrGeneration() throws Exception {
        String path = System.getProperty("sensitive.consumer.config");
        assumeTrue(path != null, "仅在明确传入业务配置路径时校验消费者配置");
        try (var reader = Files.newBufferedReader(Path.of(path), StandardCharsets.UTF_8)) {
            Context context = new ConfigurationParser(new ArrayList<>()).parseConfiguration(reader).getContexts().get(0);
            assertThat(context.getTableConfigurations()).hasSize(8).allSatisfy(table ->
                    assertThat(table.getSensitiveCheckConfigurations()).isEmpty());
            var word = context.getTableConfigurations().stream().filter(table -> "sensitive_word".equals(table.getTableName()))
                    .findFirst().orElseThrow();
            assertThat(word.getColumnOverride("word_").getMaxLength()).isEqualTo(256);
            assertThat(word.getVoGeneratorConfiguration().getVoModelConfiguration().getExcludeColumns()).contains("category_cascade_disabled_");
            assertThat(word.getVoGeneratorConfiguration().getVoViewConfiguration().getExcludeColumns()).contains("category_cascade_disabled_");
            assertThat(word.getVoGeneratorConfiguration().getVoRequestConfiguration().getExcludeColumns()).contains("category_cascade_disabled_");
        }
    }

    private Context parse(String check) throws Exception {
        return new ConfigurationParser(new ArrayList<>()).parseConfiguration(new StringReader(XML.formatted(check)))
                .getContexts().get(0);
    }

    private IntrospectedTableMyBatis3Impl fixture(String check) throws Exception {
        Context context = parse(check);
        java.lang.reflect.Field plugins = Context.class.getDeclaredField("pluginAggregator");
        plugins.setAccessible(true);
        plugins.set(context, new PluginAggregator());
        IntrospectedTableMyBatis3Impl table = new IntrospectedTableMyBatis3Impl();
        table.setContext(context);
        table.setTableConfiguration(context.getTableConfigurations().get(0));
        table.setFullyQualifiedTable(new FullyQualifiedTable(null, null, "article_", "Article", null,
                true, null, null, null, false, null, context));
        table.setRemarks("文章");
        table.addColumn(column("id_", "id"));
        table.addPrimaryKeyColumn("id_");
        table.addColumn(column("content_", "bodyText"));
        table.initialize();
        table.setRules(new FlatModelRules(table));
        return table;
    }

    private IntrospectedColumn column(String name, String property) {
        IntrospectedColumn column = new IntrospectedColumn();
        column.setActualColumnName(name);
        column.setJavaProperty(property);
        column.setFullyQualifiedJavaType(FullyQualifiedJavaType.getStringInstance());
        column.setJdbcType(Types.VARCHAR);
        column.setJdbcTypeName("VARCHAR");
        column.setLength(128);
        column.setRemarks("内容");
        return column;
    }

    private TopLevelClass model(IntrospectedTableMyBatis3Impl table) {
        BaseRecordGenerator generator = new BaseRecordGenerator("src");
        generator.setContext(table.getContext());
        generator.setIntrospectedTable(table);
        generator.setProgressCallback(new SilentProgress());
        generator.setWarnings(new ArrayList<>());
        return (TopLevelClass) generator.getCompilationUnits().get(0);
    }

    private TopLevelClass service(IntrospectedTableMyBatis3Impl table) {
        JavaServiceImplGenerator generator = new JavaServiceImplGenerator("src");
        generator.setContext(table.getContext());
        generator.setIntrospectedTable(table);
        generator.setProgressCallback(new SilentProgress());
        generator.setWarnings(new ArrayList<>());
        return (TopLevelClass) generator.getCompilationUnits().get(0);
    }

    private String format(TopLevelClass type) {
        return new DefaultJavaFormatter().getFormattedContent(type);
    }

    private String render(List<Map<String, String>> mapping) throws Exception {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_31);
        try (var reader = new InputStreamReader(getClass().getResourceAsStream(
                "/templates/vue3/vue_module_type.ts.ftl"), StandardCharsets.UTF_8)) {
            Template template = new Template("sensitive-types", reader, configuration);
            Map<String, Object> data = new java.util.HashMap<>();
            data.put("OVERRIDABLE_FLAG", "@VCF.overridable");
            data.put("tableRemark", "文章");
            data.put("typeName", "TArticle");
            data.put("fields", List.of());
            data.put("tsTypeDescriptors", List.of());
            if (mapping != null) {
                data.put("sensitiveFields", mapping);
            }
            StringWriter output = new StringWriter();
            template.process(data, output);
            return output.toString();
        }
    }

    /**
     * 为内存生成回归提供无输出的进度回调。
     *
     * @author vgosoft
     */
    private static final class SilentProgress implements ProgressCallback {
    }

    /**
     * 向编译器提供内存中的注解消费源码。
     *
     * @author vgosoft
     */
    private static final class MemorySource extends SimpleJavaFileObject {
        private final String source;

        private MemorySource(String source) {
            super(URI.create("string:///sample/MetadataProbe.java"), Kind.SOURCE);
            this.source = source;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return source;
        }
    }

    /**
     * 将编译器生成的字节码保存在内存中，避免写盘。
     *
     * @author vgosoft
     */
    private static final class MemoryBytecode extends SimpleJavaFileObject {
        private final ByteArrayOutputStream bytes;

        private MemoryBytecode(String name, Kind kind, ByteArrayOutputStream bytes) {
            super(URI.create("memory:///" + name.replace('.', '/') + kind.extension), kind);
            this.bytes = bytes;
        }

        @Override
        public OutputStream openOutputStream() {
            return bytes;
        }
    }

    /**
     * 将 Java 编译结果定向到内存，不创建临时目录或业务输出。
     *
     * @author vgosoft
     */
    private static final class MemoryFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
        private final ByteArrayOutputStream bytecode = new ByteArrayOutputStream();

        private MemoryFileManager(StandardJavaFileManager manager) {
            super(manager);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className,
                                                   JavaFileObject.Kind kind, FileObject sibling) {
            return new MemoryBytecode(className, kind, bytecode);
        }
    }
}
