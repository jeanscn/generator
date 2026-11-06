package org.mybatis.generator.codegen.mybatis3.vo;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.DefaultJavaFormatter;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.IntrospectedTableMyBatis3Impl;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.ObjectFactory;
import org.mybatis.generator.internal.PluginAggregator;
import org.mybatis.generator.internal.rules.FlatModelRules;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 审计字段映射忽略声明的生成与编译验证，对应任务书 T10。
 *
 * <p>使用离线内省替身，不连接数据库、不执行生成器、不写业务工程产物。</p>
 *
 * @author vgosoft-agent
 */
class VoMappingIgnoreGenerationTest {

    private static final String[] FULL_COLUMNS = {
            "id_:id", "name_:name", "created_:created", "modified_:modified",
            "created_id:createdId", "modified_id:modifiedId", "delete_flag:deleteFlag",
            "version_:version", "tenant_id:tenantId"
    };

    /** A 类字段口径与方案 §4.1 一致，UpdateVo 保留 version 供 C05 使用。 */
    @Test
    void auditFieldSetsMatchContract() {
        assertThat(VoGeneratorUtil.auditIgnoreFields()).containsExactly(
                "created", "createdId", "deleteFlag", "modified", "modifiedId", "tenantId", "version");
        assertThat(VoGeneratorUtil.auditIgnoreFieldsForUpdate()).containsExactly(
                "created", "createdId", "deleteFlag", "modified", "modifiedId", "tenantId");
    }

    /** 目标实体具备全部审计属性时，VO→实体映射逐字段生成忽略声明。 */
    @Test
    void addsIgnoreForEveryExistingAuditProperty() throws Exception {
        var table = fixture(FULL_COLUMNS);
        Method method = VoGeneratorUtil.addMappingMethod(createVoType(), entityType(table), false, table,
                VoGeneratorUtil.auditIgnoreFields());

        assertThat(method.getName()).isEqualTo("fromArticleCreateVo");
        assertThat(method.getAnnotations()).containsExactly(
                annotation("created"), annotation("createdId"), annotation("deleteFlag"),
                annotation("modified"), annotation("modifiedId"), annotation("tenantId"), annotation("version"));
    }

    /** 集合映射方法同样生成忽略声明，且 UpdateVo 不忽略 version。 */
    @Test
    void updateMappingKeepsVersionForListMethod() throws Exception {
        var table = fixture(FULL_COLUMNS);
        var updateVo = new FullyQualifiedJavaType("sample.pojo.vo.ArticleUpdateVo");
        Method method = VoGeneratorUtil.addMappingMethod(updateVo, entityType(table), true, table,
                VoGeneratorUtil.auditIgnoreFieldsForUpdate());

        assertThat(method.getName()).isEqualTo("fromArticleUpdateVos");
        assertThat(method.getAnnotations()).contains(annotation("created"), annotation("tenantId"))
                .doesNotContain(annotation("version"));
        assertThat(method.getReturnType().get().getFullyQualifiedName()).startsWith("java.util.List");
    }

    /** 目标实体不存在的属性不输出忽略声明，根实体父类声明的属性仍可识别。 */
    @Test
    void skipsAuditPropertiesAbsentFromEntity() throws Exception {
        var table = fixture("id_:id", "name_:name");
        Method method = VoGeneratorUtil.addMappingMethod(createVoType(), entityType(table), false, table,
                VoGeneratorUtil.auditIgnoreFields());

        assertThat(method.getAnnotations()).containsExactly(annotation("deleteFlag"));
    }

    /** 未传入忽略字段的原重载输出保持不变，不影响实体→VO 等其他映射方向。 */
    @Test
    void legacyOverloadKeepsOriginalOutput() throws Exception {
        var table = fixture(FULL_COLUMNS);
        Method fromVo = VoGeneratorUtil.addMappingMethod(createVoType(), entityType(table), false, table);
        Method toVo = VoGeneratorUtil.addMappingMethod(entityType(table), createVoType(), false, table);

        assertThat(fromVo.getAnnotations()).isEmpty();
        assertThat(toVo.getAnnotations()).isEmpty();
        assertThat(toVo.getName()).isEqualTo("toArticleCreateVo");
    }

    /** 包装方法登记 Mapping 导入，渲染出的映射接口带忽略声明且可通过 JDK 编译。 */
    @Test
    void wrapperRegistersMappingImportAndRenderedInterfaceCompiles() throws Exception {
        var table = fixture(FULL_COLUMNS);
        var entityType = entityType(table);
        var createVo = createVoType();
        Interface mappings = new Interface("sample.pojo.maps.ArticleMappings");
        mappings.setVisibility(JavaVisibility.PUBLIC);
        mappings.addImportedType(entityType);
        mappings.addImportedType(createVo);

        AbstractVoGenerator generator = new AbstractVoGenerator(table, "src",
                new ProgressCallback() { }, new ArrayList<>(), mappings) {
            @Override
            protected TopLevelClass generate() {
                return null;
            }
        };
        mappings.addMethod(generator.addMappingMethod(createVo, entityType, false,
                VoGeneratorUtil.auditIgnoreFields()));

        String rendered = format(mappings);
        assertThat(rendered).contains("import org.mapstruct.Mapping;")
                .contains(annotation("created"), annotation("version"), annotation("tenantId"));
        compile(Map.of(
                "sample.pojo.maps.ArticleMappings", rendered,
                "sample.entity.Article", "package sample.entity; public class Article {}",
                "sample.pojo.vo.ArticleCreateVo", "package sample.pojo.vo; public class ArticleCreateVo {}",
                "org.mapstruct.Mapping", """
                        package org.mapstruct;
                        import java.lang.annotation.*;
                        @Retention(RetentionPolicy.SOURCE)
                        @Target(ElementType.METHOD)
                        @Repeatable(Mappings.class)
                        public @interface Mapping {
                            String target();
                            boolean ignore() default false;
                        }
                        """,
                "org.mapstruct.Mappings", """
                        package org.mapstruct;
                        import java.lang.annotation.*;
                        @Retention(RetentionPolicy.SOURCE)
                        @Target(ElementType.METHOD)
                        public @interface Mappings {
                            Mapping[] value();
                        }
                        """));
    }

    /** 三个 VO 生成器只在 VO→实体方向输出忽略声明，实体→VO 方向保持原样。 */
    @Test
    void voGeneratorsApplyIgnoreOnlyToEntityBoundMappings() throws Exception {
        var table = fixture(FULL_COLUMNS);
        Interface mappings = new Interface("sample.pojo.maps.ArticleMappings");
        mappings.setVisibility(JavaVisibility.PUBLIC);
        var callback = new ProgressCallback() { };
        var warnings = new ArrayList<String>();
        new VoCreateGenerator(table, "src", callback, warnings, mappings).generate();
        new VoUpdateGenerator(table, "src", callback, warnings, mappings).generate();
        new VoExcelImportGenerator(table, "src", callback, warnings, mappings).generate();

        List<Method> withIgnore = mappings.getMethods().stream()
                .filter(m -> !m.getAnnotations().isEmpty()).toList();
        assertThat(withIgnore).isNotEmpty().allMatch(m -> m.getName().startsWith("from"));
        assertThat(withIgnore).anyMatch(m -> m.getName().contains("CreateVo")
                && m.getAnnotations().contains(annotation("version")));
        assertThat(withIgnore.stream().filter(m -> m.getName().contains("UpdateVo")).toList())
                .isNotEmpty().allMatch(m -> !m.getAnnotations().contains(annotation("version")));
        assertThat(mappings.getMethods().stream().filter(m -> m.getName().startsWith("to")).toList())
                .isNotEmpty().allMatch(m -> m.getAnnotations().isEmpty());
    }

    private static String annotation(String field) {
        return String.format("@Mapping(target = \"%s\", ignore = true)", field);
    }

    private static FullyQualifiedJavaType createVoType() {
        return new FullyQualifiedJavaType("sample.pojo.vo.ArticleCreateVo");
    }

    private static FullyQualifiedJavaType entityType(IntrospectedTableMyBatis3Impl table) {
        return new FullyQualifiedJavaType(table.getBaseRecordType());
    }

    /** 构造离线内省替身，列以「列名:属性名」给出，rootClass 沿用配置中的 AbstractEntity。 */
    private IntrospectedTableMyBatis3Impl fixture(String... columnSpecs) throws Exception {
        String xml;
        try (var stream = getClass().getResourceAsStream("/dict-prefetch-generator.xml")) {
            assertThat(stream).as("离线生成配置资源").isNotNull();
            xml = new String(stream.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("<generateVo generate=\"false\"/>", """
                            <generateVo generate="true">
                              <createVo/>
                              <updateVo/>
                              <excelVo/>
                            </generateVo>
                            """);
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
        for (String spec : columnSpecs) {
            String[] parts = spec.split(":", 2);
            IntrospectedColumn column = new IntrospectedColumn();
            column.setActualColumnName(parts[0]);
            column.setJavaProperty(parts[1]);
            column.setFullyQualifiedJavaType(FullyQualifiedJavaType.getStringInstance());
            column.setJdbcType(Types.VARCHAR);
            column.setJdbcTypeName("VARCHAR");
            column.setRemarks("测试列");
            column.setContext(context);
            table.addColumn(column);
        }
        table.addPrimaryKeyColumn("id_");
        table.initialize();
        table.setRules(new FlatModelRules(table));
        return table;
    }

    private static String format(CompilationUnit unit) {
        return new DefaultJavaFormatter().getFormattedContent(unit);
    }

    /** 用内存源码编译渲染结果，只校验 Java 语法与导入，不启用注解处理器。 */
    private static void compile(Map<String, String> sources) throws Exception {
        var compiler = ToolProvider.getSystemJavaCompiler();
        assertThat(compiler).isNotNull();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (var standard = compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8);
             var manager = new ForwardingJavaFileManager<StandardJavaFileManager>(standard) {
                 @Override
                 public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location, String name,
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
                    .withFailMessage("映射接口渲染结果编译失败：%s", diagnostics.getDiagnostics()).isTrue();
        }
    }
}
