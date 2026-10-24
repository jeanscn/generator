package org.mybatis.generator.codegen.mybatis3.controller.elements;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.DefaultJavaFormatter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.IntrospectedTableMyBatis3Impl;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.internal.DefaultCommentGenerator;
import org.mybatis.generator.internal.rules.ConditionalModelRules;

import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ExportElementGeneratorTest {

    @Test
    void shouldPassLegacyTemplateExportResourceContextWithoutSelectionGuard() throws Exception {
        try (var input = getClass().getResourceAsStream("/templates/vue3/vue_module_list.all.vue.ftl")) {
            assertThat(input).isNotNull();
            String template = new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            assertThat(template).contains(":exportBasePath=\"restBasePath\"",
                            "restBasePath.value = _tableConfigProps.value!.restBasePath")
                    .doesNotContain("exportSelect", "case 'top-custom-export'");
        }
    }

    @Test
    void shouldRetainGetAndGenerateTypedPostMetadataAndSafeQuery() {
        Fixture fixture = fixture(true);
        fixture.generator.addElements(fixture.output);
        Method post = method(fixture, "exportPostRecord");
        String body = String.join("\n", post.getBodyLines());
        assertThat(post.getParameters().get(0).getType().getShortName())
                .isEqualTo("VgoExportRequestVo<RecordRequestVo>");
        assertThat(post.getParameters().get(0).getAnnotations())
                .contains("@Validated({Default.class, ValidateQuery.class})");
        assertThat(post.getAnnotations().toString()).contains("@PostMapping", "export", "@SystemLog", "@Operation");
        assertThat(method(fixture, "exportRecord").getAnnotations().toString()).contains("@GetMapping", "export");
        assertThat(method(fixture, "exportOptionsRecord").getAnnotations().toString())
                .contains("@GetMapping", "export/options");
        assertThat(String.join("\n", method(fixture, "exportOptionsRecord").getBodyLines()))
                .contains("options.setMaxScopeIds(500)");
        for (String reset : List.of("query.setIgnorePermissionAnnotation(false);", "query.setIgnoreDeleteFlag(false);",
                "query.setAnyWhereCondition(null);", "query.setOrderByClause(null);")) {
            assertThat(body.indexOf(reset)).isGreaterThanOrEqualTo(0).isLessThan(body.indexOf("buildExample(query)"));
        }
        assertThat(body).contains("ParameterUtil.parseFilterMap(filterParam, \"r\")",
                "example.getOredCriteria().forEach(criteria -> criteria.andRecordKeyIn(ids))",
                "ExportSupport.restoreOrder(records, ids, Record::getRecordKey)",
                "records.size() != ids.size()", "5000 : 50000", "PageHelper.clearPage()")
                .doesNotContain("andIdIn", "limit ", "catch (", "new ArrayList");
        assertThat(body).contains("ids.size() > 500",
                "(long) ids.size() * example.getOredCriteria().size() > 1000",
                "if (options.getSorts() == null || options.getSorts().isEmpty()) {",
                "List<Record> records = exportBatchRecord(example, 0, ids.size());");
        assertThat(body.indexOf("records.size() != ids.size()"))
                .isLessThan(body.indexOf("ExportSupport.restoreOrder"));
        assertThat(body.indexOf("ExportSupport.restoreOrder"))
                .isLessThan(body.indexOf("records.subList(0, Math.min(records.size(), options.getMaxRows()))"));
        assertThat(String.join("\n", method(fixture, "exportSortColumnsRecord").getBodyLines()))
                .contains("columns.put(\"recordKey\", \"r.\\\"record_key\\\"\")");
        assertThat(String.join("\n", method(fixture, "exportBatchRecord").getBodyLines()))
                .contains("PageHelper.offsetPage(offset, size, false).setReasonable(false)",
                        "!result.isSuccess()", "finally", "PageHelper.clearPage()");
    }

    @Test
    void shouldExcludeBlobFieldsFromMetadataAndRejectRequestsBeforeQuery() {
        Fixture fixture = fixture(true);
        IntrospectedColumn blob = column(fixture.context, "content_", "content");
        blob.setJdbcTypeName("LONGVARCHAR");
        fixture.table.getBlobColumns().add(blob);
        fixture.generator.addElements(fixture.output);
        assertThat(String.join("\n", method(fixture, "exportAllowedColumnsRecord").getBodyLines()))
                .contains("Set.of(\"content\")", "ExportSupport.schema", "!unsupported.contains(field)");
        assertThat(String.join("\n", method(fixture, "exportOptionsRecord").getBodyLines()))
                .contains("options.setColumns", "allowed.contains(column.field())");
        String body = String.join("\n", method(fixture, "exportPostRecord").getBodyLines());
        assertThat(body).contains("exportAllowedColumnsRecord().containsAll(options.getColumns())");
        assertThat(body.indexOf("containsAll(options.getColumns())")).isLessThan(body.indexOf("buildExample(query)"));
    }

    @Test
    void shouldShareExportPermissionAcrossAllEndpoints() {
        Fixture fixture = fixture(true);
        fixture.context.setModuleKeyword("sample");
        fixture.context.setModuleName("测试");
        fixture.table.setRules(new ConditionalModelRules(fixture.table) {
            @Override
            public boolean isGenerateRequestVo() { return true; }

            @Override
            public boolean isIntegrateSpringSecurity() { return true; }
        });
        fixture.generator.addElements(fixture.output);
        for (String name : List.of("exportRecord", "exportOptionsRecord", "exportPostRecord")) {
            assertThat(method(fixture, name).getAnnotations()).contains(
                    "@PreAuthorize(\"@uss.hasPermission('sample:service:export')\")");
        }
    }

    @Test
    void shouldUseActualFallbackQueryTypeAndNotEmitMissingAccessors() {
        Fixture fixture = fixture(false);
        fixture.generator.addElements(fixture.output);
        Method post = method(fixture, "exportPostRecord");
        assertThat(post.getParameters().get(0).getType().getShortName())
                .isEqualTo("VgoExportRequestVo<Record>");
        assertThat(String.join("\n", post.getBodyLines())).doesNotContain("query.set", "query.getFilterParam()");
    }

    @Test
    void shouldWarnAndSkipUnsupportedPrimaryKeys() {
        for (int count : List.of(0, 1, 2)) {
            Fixture fixture = fixture(true);
            fixture.table.getPrimaryKeyColumns().clear();
            for (int i = 0; i < count; i++) {
                IntrospectedColumn column = column(fixture.context, "key_" + i, "key" + i);
                column.setFullyQualifiedJavaType(FullyQualifiedJavaType.getIntInstance());
                fixture.table.getPrimaryKeyColumns().add(column);
            }
            fixture.generator.addElements(fixture.output);
            assertThat(fixture.output.getMethods()).isEmpty();
            assertThat(fixture.generator.getWarnings()).singleElement().asString()
                    .contains("single String primary key");
        }
    }

    @Test
    void shouldCompileRenderedControllerWithTypedBatchContract() throws Exception {
        Fixture fixture = fixture(true);
        IntrospectedColumn content = column(fixture.context, "content_", "content");
        content.setJdbcTypeName("LONGVARCHAR");
        fixture.table.getBlobColumns().add(content);
        fixture.generator.addElements(fixture.output);
        fixture.output.addImportedType("sample.ServiceResult");
        fixture.output.addImportedType("sample.Support.Record");
        fixture.output.addImportedType("sample.Support.RecordRequestVo");
        fixture.output.addImportedType("sample.Support.RecordExcelVo");
        fixture.output.addImportedType("sample.Support.RecordExample");
        fixture.output.addImportedType("org.springframework.web.bind.annotation.RequestParam");
        Method dependencies = new Method("buildExample");
        dependencies.setReturnType(new FullyQualifiedJavaType("sample.Support.RecordExample"));
        dependencies.addParameter(new org.mybatis.generator.api.dom.java.Parameter(
                new FullyQualifiedJavaType("sample.Support.RecordRequestVo"), "query"));
        dependencies.addBodyLine("return new RecordExample();");
        fixture.output.addMethod(dependencies);
        Method width = new Method("getDefaultColumnWidthStyleStrategy");
        width.setReturnType(new FullyQualifiedJavaType("org.apache.fesod.sheet.write.handler.CellWriteHandler"));
        fixture.output.addImportedType("org.apache.fesod.sheet.write.handler.CellWriteHandler");
        width.addParameter(new org.mybatis.generator.api.dom.java.Parameter(FullyQualifiedJavaType.getStringInstance(), "name"));
        width.addBodyLine("return null;");
        fixture.output.addMethod(width);
        var service = new org.mybatis.generator.api.dom.java.Field("service",
                new FullyQualifiedJavaType("sample.Support.Service"));
        var mappings = new org.mybatis.generator.api.dom.java.Field("mappings",
                new FullyQualifiedJavaType("sample.Support.Mappings"));
        fixture.output.addField(service);
        fixture.output.addField(mappings);
        String rendered = new DefaultJavaFormatter().getFormattedContent(fixture.output);
        assertThat(rendered).contains("description = ").doesNotContain("notes = ");
        Map<String, String> sources = contracts();
        if (System.getProperty("export.contract.classpath") != null
                || System.getProperty("export.contract.classpath-file") != null) {
            sources.keySet().removeIf(name -> name.startsWith("com.vgosoft.plugins.excel.")
                    || name.equals("org.apache.fesod.sheet.write.handler.CellWriteHandler")
                    || name.equals("jakarta.servlet.http.HttpServletResponse"));
        }
        sources.put("sample.Controller", rendered);
        sources.put("sample.Support", """
                package sample;
                import java.util.*;
                public class Support {
                public static class Record { public String getRecordKey() { return ""; } }
                public static class RecordExcelVo {}
                public static class RecordRequestVo {
                public void setIgnorePermissionAnnotation(boolean b) {}
                public void setIgnoreDeleteFlag(boolean b) {}
                public void setAnyWhereCondition(String s) {}
                public void setOrderByClause(String s) {}
                public com.vgosoft.core.adapter.web.FilterParam getFilterParam() { return null; }
                }
                public static class RecordExample {
                private final List<Criteria> ored = new ArrayList<>();
                public void setIgnorePermissionAnnotation(boolean b) {}
                public void setIgnoreDeleteFlag(boolean b) {}
                public void setOrderByClause(String s) {}
                public List<Criteria> getOredCriteria() { return ored; }
                public Criteria createCriteria() { Criteria c = new Criteria(); ored.add(c); return c; }
                public static class Criteria {
                public Criteria andRecordKeyIn(List<String> ids) { return this; }
                public Criteria andAnyCondition(String sql) { return this; }
                }
                }
                public static class Service {
                public ServiceResult<List<Record>> selectByExample(RecordExample example) { return null; }
                }
                public static class Mappings {
                public List<RecordExcelVo> toRecordExcelVos(List<Record> records) { return null; }
                }
                }
                """);
        sources.put("sample.ServiceResult", """
                package sample;
                public class ServiceResult<T> {
                public boolean isSuccess() { return true; }
                public T getResult() { return null; }
                public String getMessage() { return ""; }
                }
                """);
        compileInMemory(sources);
    }

    private static Map<String, String> contracts() {
        Map<String, String> sources = new LinkedHashMap<>();
        sources.put("org.apache.fesod.sheet.write.handler.CellWriteHandler", """
                package org.apache.fesod.sheet.write.handler;
                public interface CellWriteHandler {}
                """);
        sources.put("jakarta.servlet.http.HttpServletResponse", """
                package jakarta.servlet.http;
                public interface HttpServletResponse {}
                """);
        sources.put("com.vgosoft.web.resolver.annotation.RequestParamSplit", """
                package com.vgosoft.web.resolver.annotation;
                public @interface RequestParamSplit {}
                """);
        sources.put("com.vgosoft.web.utils.ParameterUtil", """
                package com.vgosoft.web.utils;
                public class ParameterUtil {
                public static String parseFilterMap(com.vgosoft.core.adapter.web.FilterParam p, String alias) { return ""; }
                }
                """);
        sources.put("com.vgosoft.plugins.excel.pojo.vo.VgoExportRequestVo", """
                package com.vgosoft.plugins.excel.pojo.vo;
                public class VgoExportRequestVo<Q> {
                public Q getQuery() { return null; }
                public VgoExportOptionsVo getOptions() { return null; }
                }
                """);
        sources.put("com.vgosoft.plugins.excel.pojo.vo.VgoExportOptionsVo", """
                package com.vgosoft.plugins.excel.pojo.vo;
                public class VgoExportOptionsVo {
                public String getFormat() { return ""; }
                public String getScope() { return ""; }
                public int getMaxRows() { return 10000; }
                public java.util.List<Object> getSorts() { return null; }
                public java.util.List<String> getColumns() { return java.util.List.of(); }
                }
                """);
        sources.put("com.vgosoft.plugins.excel.pojo.vo.VgoExportOptionsViewVo", """
                package com.vgosoft.plugins.excel.pojo.vo;
                public class VgoExportOptionsViewVo {
                public void setMaxScopeIds(int maxScopeIds) {}
                public java.util.List<com.vgosoft.plugins.excel.export.ExportColumn> getColumns() { return java.util.List.of(); }
                public void setColumns(java.util.List<com.vgosoft.plugins.excel.export.ExportColumn> columns) {}
                }
                """);
        sources.put("com.vgosoft.plugins.excel.export.ExportColumn", """
                package com.vgosoft.plugins.excel.export;
                public record ExportColumn(String field, String label) {}
                """);
        sources.put("com.vgosoft.plugins.excel.export.ExportSupport", """
                package com.vgosoft.plugins.excel.export;
                import java.util.*;
                import java.util.function.Function;
                import com.vgosoft.plugins.excel.pojo.vo.*;
                public class ExportSupport {
                public static List<ExportColumn> schema(Class<?> type) { return List.of(); }
                public static VgoExportOptionsViewVo metadata(Class<?> type, Map<String,String> columns) { return null; }
                public static void validate(VgoExportOptionsVo options, Class<?> type, Map<String,String> columns) {}
                public static String orderBy(VgoExportOptionsVo o, Map<String,String> c, String d, String p) { return ""; }
                public static List<String> ids(VgoExportOptionsVo options) { return List.of(); }
                public static <T> List<T> restoreOrder(List<T> rows, List<String> ids, Function<T,String> key) { return rows; }
                }
                """);
        sources.put("com.vgosoft.plugins.excel.service.VgoFesodSheet", """
                package com.vgosoft.plugins.excel.service;
                import java.util.*;
                import java.io.IOException;
                import jakarta.servlet.http.HttpServletResponse;
                import com.vgosoft.plugins.excel.pojo.vo.VgoExportOptionsVo;
                public class VgoFesodSheet {
                public static <T> void export(HttpServletResponse r,VgoExportOptionsVo o,Class<T> c,Iterable<List<T>> rows)
                throws IOException {}
                public static <T> void write(HttpServletResponse r,String f,String s,Class<T> c,List<T> rows,
                org.apache.fesod.sheet.write.handler.CellWriteHandler strategy)
                throws IOException {}
                }
                """);
        sources.put("com.vgosoft.core.adapter.web.respone.ResponseResult", """
                package com.vgosoft.core.adapter.web.respone;
                public class ResponseResult<T> {
                public static <T> ResponseResult<T> success(T result) { return null; }
                }
                """);
        return sources;
    }

    private static void compileInMemory(Map<String, String> sources) throws Exception {
        var compiler = ToolProvider.getSystemJavaCompiler();
        assertThat(compiler).isNotNull();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager standard = compiler.getStandardFileManager(diagnostics, null, null);
             var manager = new ForwardingJavaFileManager<StandardJavaFileManager>(standard) {
                 @Override
                 public JavaFileObject getJavaFileForOutput(Location location, String className,
                                                           JavaFileObject.Kind kind, FileObject sibling) {
                     return new SimpleJavaFileObject(URI.create("memory:///" + className + kind.extension), kind) {
                         @Override
                         public OutputStream openOutputStream() { return new ByteArrayOutputStream(); }
                     };
                 }
             }) {
            List<JavaFileObject> units = sources.entrySet().stream().map(entry -> (JavaFileObject)
                    new SimpleJavaFileObject(URI.create("string:///" + entry.getKey().replace('.', '/')
                            + JavaFileObject.Kind.SOURCE.extension), JavaFileObject.Kind.SOURCE) {
                        @Override
                        public CharSequence getCharContent(boolean ignoreEncodingErrors) { return entry.getValue(); }
                    }).toList();
            String classpath = System.getProperty("java.class.path");
            String contractClasspath = System.getProperty("export.contract.classpath");
            String contractClasspathFile = System.getProperty("export.contract.classpath-file");
            if (contractClasspathFile != null) {
                contractClasspath = System.getProperty("export.contract.classes")
                        + java.io.File.pathSeparator
                        + java.nio.file.Files.readString(java.nio.file.Path.of(contractClasspathFile)).strip();
            }
            if (contractClasspath != null) {
                classpath = contractClasspath + java.io.File.pathSeparator + classpath;
            }
            boolean success = compiler.getTask(null, manager, diagnostics,
                    List.of("-proc:none", "--release", "17", "-classpath", classpath),
                    null, units).call();
            assertThat(success).withFailMessage("%s", diagnostics.getDiagnostics()).isTrue();
        }
    }

    private static Method method(Fixture fixture, String name) {
        return fixture.output.getMethods().stream().filter(method -> method.getName().equals(name))
                .findFirst().orElseThrow();
    }

    private static Fixture fixture(boolean requestVo) {
        Context context = new Context(ModelType.CONDITIONAL);
        TableConfiguration configuration = new TableConfiguration(context, new ArrayList<>());
        configuration.setTableName("record_");
        configuration.setDomainObjectName("Record");
        configuration.setAlias("r");
        IntrospectedTableMyBatis3Impl table = new IntrospectedTableMyBatis3Impl();
        table.setContext(context);
        table.setTableConfiguration(configuration);
        table.setBaseRecordType("sample.Support.Record");
        table.setExampleType("sample.Support.RecordExample");
        table.setRemarks("测试记录");
        table.setRules(new ConditionalModelRules(table) {
            @Override
            public boolean isGenerateRequestVo() { return requestVo; }

            @Override
            public boolean isIntegrateSpringSecurity() { return false; }
        });
        IntrospectedColumn key = column(context, "record_key", "recordKey");
        table.getPrimaryKeyColumns().add(key);
        table.getBaseColumns().add(column(context, "name_", "name"));
        table.getTopLevelClassExampleFields().put("RecordRequestVo", List.of("ignorePermissionAnnotation",
                "ignoreDeleteFlag", "anyWhereCondition", "orderByClause", "filterParam"));
        ExportElementGenerator generator = new ExportElementGenerator();
        generator.setContext(context);
        generator.setIntrospectedTable(table);
        generator.setWarnings(new ArrayList<>());
        generator.setEntityType(new FullyQualifiedJavaType("sample.Support.Record"));
        generator.setExampleType(new FullyQualifiedJavaType("sample.Support.RecordExample"));
        generator.setEntityExcelVoType(new FullyQualifiedJavaType("sample.Support.RecordExcelVo"));
        generator.setEntityRequestVoType(new FullyQualifiedJavaType("sample.Support.RecordRequestVo"));
        generator.setServiceBeanName("service");
        generator.setCommentGenerator(new DefaultCommentGenerator());
        return new Fixture(context, table, generator, new TopLevelClass("sample.Controller"));
    }

    private static IntrospectedColumn column(Context context, String physical, String property) {
        IntrospectedColumn column = new IntrospectedColumn();
        column.setContext(context);
        column.setActualColumnName(physical);
        column.setJavaProperty(property);
        column.setTableAlias("r");
        column.setColumnNameDelimited(true);
        column.setFullyQualifiedJavaType(FullyQualifiedJavaType.getStringInstance());
        column.setJdbcTypeName("VARCHAR");
        return column;
    }

    private record Fixture(Context context, IntrospectedTableMyBatis3Impl table,
                           ExportElementGenerator generator, TopLevelClass output) {}
}
