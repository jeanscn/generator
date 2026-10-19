package org.mybatis.generator.codegen.mybatis3;

import com.github.javaparser.StaticJavaParser;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.DefaultJavaFormatter;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.model.BaseRecordGenerator;
import org.mybatis.generator.codegen.mybatis3.model.SensitiveModelAnnotations;
import org.mybatis.generator.codegen.mybatis3.service.JavaServiceImplGenerator;
import org.mybatis.generator.codegen.mybatis3.service.elements.SensitiveWriteMethodsElement;
import org.mybatis.generator.custom.annotations.AbstractAnnotation;
import org.mybatis.generator.custom.annotations.SensitiveCheckDesc;
import org.mybatis.generator.plugins.vue3.Vue3OtherFilesPlugin;
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
              <context id="sensitive-test" defaultModelType="flat"
                appKeyword="sample" moduleKeyword="sample" moduleName="测试" jdkVersion="17" integrateMybatisPlus="false">
                <commentGenerator><property name="suppressDate" value="true"/></commentGenerator>
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
    void keepsValidationAndFieldResolutionSeparateFromLayerRendering() {
        for (Class<?> validator : List.of(SensitiveGenerationValidator.class, SelectForUpdateValidator.class)) {
            assertThat(validator.getPackageName()).isEqualTo("org.mybatis.generator.codegen.mybatis3");
            assertThat(java.util.Arrays.stream(validator.getDeclaredMethods()).filter(method ->
                    java.lang.reflect.Modifier.isPublic(method.getModifiers())).map(java.lang.reflect.Method::getName))
                    .containsExactly("validate");
        }
        assertThat(java.util.Arrays.stream(SensitiveFieldResolver.class.getDeclaredMethods()).filter(method ->
                java.lang.reflect.Modifier.isPublic(method.getModifiers())).map(java.lang.reflect.Method::getName))
                .containsExactlyInAnyOrder("resolve", "isEnabled");
        assertThat(org.mybatis.generator.config.SensitiveCheckConfiguration.class.getPackageName())
                .isEqualTo("org.mybatis.generator.config");
        assertThat(org.mybatis.generator.dbstrategy.SelectForUpdateDialect.class.getPackageName())
                .isEqualTo("org.mybatis.generator.dbstrategy");
        assertThat(SensitiveModelAnnotations.class.getPackageName()).isEqualTo("org.mybatis.generator.codegen.mybatis3.model");
        assertThat(SensitiveModelAnnotations.class.getSuperclass()).isEqualTo(Object.class);
        assertThat(SensitiveCheckDesc.class.getSuperclass()).isEqualTo(AbstractAnnotation.class);
        assertThat(SensitiveWriteMethodsElement.class.getSuperclass())
                .isEqualTo(org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator.class);
        assertThat(SensitiveWriteMethodsElement.class.getPackageName())
                .isEqualTo("org.mybatis.generator.codegen.mybatis3.service.elements");
        assertThat(Vue3OtherFilesPlugin.class.getPackageName()).isEqualTo("org.mybatis.generator.plugins.vue3");
    }

    @Test
    void preservesRealPluginOrderingAndRejectsPostPluginMutations() throws Exception {
        IntrospectedTableMyBatis3Impl table = fixture(CHECK);
        SensitiveProbePlugin probe = new SensitiveProbePlugin();
        ((PluginAggregator) table.getContext().getPlugins()).addPlugin(probe);
        assertThat(format(model(table))).contains("@SensitiveCheck");
        assertThat(format(service(table))).contains("@SensitiveWrite");
        assertThat(probe.calls).containsExactly("model", "service");

        IntrospectedTableMyBatis3Impl alteredModel = fixture(CHECK);
        SensitiveProbePlugin modelPlugin = new SensitiveProbePlugin();
        modelPlugin.modelChange = model -> model.getFields().stream()
                .filter(field -> "bodyText".equals(field.getName())).findFirst().orElseThrow().setFinal(true);
        ((PluginAggregator) alteredModel.getContext().getPlugins()).addPlugin(modelPlugin);
        assertThatThrownBy(() -> model(alteredModel))
                .hasMessage("表 article_，列/方法 content_：实体字段类型或可写属性被插件改变");

        List<java.util.function.Consumer<Method>> mutations = List.of(
                method -> method.setFinal(true),
                method -> {
                    method.getParameters().clear();
                    method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "record"));
                },
                method -> {
                    method.getAnnotations().removeIf(value -> value.startsWith("@Transactional"));
                    method.addAnnotation("@Transactional(propagation = Propagation.NOT_SUPPORTED)");
                });
        List<String> reasons = List.of("受管写方法必须是可代理的 public 非静态、非 final 具体方法",
                "只支持单实体或同类型有序 List 参数",
                "事务声明不是已验证的 REQUIRED/Exception 回滚模板，须单独评审");
        for (int index = 0; index < mutations.size(); index++) {
            IntrospectedTableMyBatis3Impl alteredService = fixture(CHECK);
            SensitiveProbePlugin servicePlugin = new SensitiveProbePlugin();
            var mutation = mutations.get(index);
            servicePlugin.serviceChange = service -> mutation.accept(service.getMethods().stream()
                    .filter(method -> "insert".equals(method.getName())).findFirst().orElseThrow());
            ((PluginAggregator) alteredService.getContext().getPlugins()).addPlugin(servicePlugin);
            assertThatThrownBy(() -> service(alteredService)).hasMessage("表 article_，列/方法 insert：" + reasons.get(index));
        }
    }

    @Test
    void preservesActualPluginVetoFailuresAndDisabledBypass() throws Exception {
        IntrospectedTableMyBatis3Impl enabled = fixture(CHECK);
        SensitiveProbePlugin veto = new SensitiveProbePlugin();
        veto.approve = false;
        ((PluginAggregator) enabled.getContext().getPlugins()).addPlugin(veto);
        assertThatThrownBy(() -> modelOutput(enabled))
                .hasMessage("表 article_，列/方法 *：插件禁止生成实体，无法落地敏感字段元数据");
        assertThatThrownBy(() -> serviceOutput(enabled))
                .hasMessage("表 article_，列/方法 *：插件禁止生成 Service，无法落地受管写方法标记");
        for (String configuration : List.of("", "<sensitiveCheck columns=\"missing_\" enable=\"false\"/>")) {
            IntrospectedTableMyBatis3Impl disabled = fixture(configuration);
            SensitiveProbePlugin disabledVeto = new SensitiveProbePlugin();
            disabledVeto.approve = false;
            ((PluginAggregator) disabled.getContext().getPlugins()).addPlugin(disabledVeto);
            assertThat(modelOutput(disabled)).isEmpty();
            assertThat(serviceOutput(disabled)).allSatisfy(unit ->
                    assertThat(new DefaultJavaFormatter().getFormattedContent(unit)).doesNotContain("@SensitiveWrite"));
            assertThat(disabledVeto.calls).containsExactly("model", "service");
            assertThat(Vue3OtherFilesPlugin.frontendFields(disabled)).isEmpty();
        }
    }

    @Test
    void rendersSensitiveMappingThroughActualVuePluginAndPreservesItsGates() throws Exception {
        Vue3OtherFilesPlugin plugin = new Vue3OtherFilesPlugin();
        for (String configuration : List.of(CHECK, "", "<sensitiveCheck columns=\"content_\" enable=\"false\"/>")) {
            IntrospectedTableMyBatis3Impl table = fixture(configuration);
            plugin.setContext(table.getContext());
            assertThat(plugin.contextGenerateVue3OtherFiles(table)).isEmpty();
            table.getContext().setVueEndProjectPath("mybatis-generator-core\\target\\sensitive-offline-unused");
            assertThat(plugin.contextGenerateVue3OtherFiles(table)).isEmpty();
            table.getTableConfiguration().getVoGeneratorConfiguration().setGenerate(true);
            table.getTableConfiguration().getVoGeneratorConfiguration().setVoModelConfiguration(
                    new org.mybatis.generator.config.VoModelGeneratorConfiguration(table.getContext(), table.getTableConfiguration()));
            assertThat(plugin.contextGenerateVue3OtherFiles(table)).isEmpty();
            var field = new org.mybatis.generator.custom.FieldItem("bodyText", "String");
            field.setRemarks("内容");
            table.getVoModelFields().add(field);
            var files = plugin.contextGenerateVue3OtherFiles(table);
            assertThat(files).hasSize(1);
            assertThat(files.get(0).getFileName()).isEqualTo("TArticle.ts");
            String output = files.get(0).getFormattedContent();
            assertThat(output).contains("bodyText", "string");
            if (SensitiveFieldResolver.isEnabled(table)) {
                assertThat(output).contains("TArticleSensitiveFields", "article_", "sample.entity.Article", "PLAIN_TEXT");
            } else {
                assertThat(output).doesNotContain("SensitiveFields");
            }
        }
    }

    @Test
    void offlineOutputBaseline() throws Exception {
        // 摘要来自本轮重构前完整输出，固定日期配置并仅统一换行；顺序为实体、服务实现和 TS。
        String[][] expected = {
                {"74026c280c266dd956676cef0e76761a423fdb053085bff10f2b1191df84fd01",
                 "771475078833aa35bf73362bc20f57547c0467d71d03548baf764661cb91f211",
                 "a6eb9e4ebc39bd2853cc2dca923ff10f4466417c2dbb88d7d6d9ecf06ee24fd8"},
                {"97d151e5249fb343d492d86a912667a6f206467a46220ffb7c8c9763ff2203a4",
                 "628d52092059d592cbff08ccbf1611f10a219196190c4129393a6506e8db0dcb",
                 "e452649e1a626f3f81460b3b036799ee647430c544f34225f0126dbd60788b75"},
                {"97d151e5249fb343d492d86a912667a6f206467a46220ffb7c8c9763ff2203a4",
                 "628d52092059d592cbff08ccbf1611f10a219196190c4129393a6506e8db0dcb",
                 "e452649e1a626f3f81460b3b036799ee647430c544f34225f0126dbd60788b75"},
                {"8a597a12b151f57431500184f0bf5cc0f589b6f60b91185f3943cfdc57638a90",
                 "771475078833aa35bf73362bc20f57547c0467d71d03548baf764661cb91f211",
                 "682ebf7f150cf5789d132804e39ea70626d5a510971bbbf28a5ad0e87a7349b7"}
        };
        List<String> configurations = List.of(CHECK, "",
                "<sensitiveCheck columns=\"content_\" enable=\"false\" strategy=\"WARN\"/>",
                "<sensitiveCheck columns=\"content_\" strategy=\"BLOCK\" contentType=\"PLAIN_TEXT\"/>"
                        + "<sensitiveCheck columns=\"summary_\" strategy=\"WARN\"/>");
        for (int variant = 0; variant < configurations.size(); variant++) {
            IntrospectedTableMyBatis3Impl table = fixture(variant == 3 ? "" : configurations.get(variant));
            if (variant == 3) {
                table.addColumn(column("summary_", "summary"));
                table.getTableConfiguration().getSensitiveCheckConfigurations().addAll(
                        parse(configurations.get(variant)).getTableConfigurations().get(0).getSensitiveCheckConfigurations());
                SensitiveGenerationValidator.validate(table);
            }
            List<String> outputs = List.of(format(model(table)), format(service(table)),
                    render(Vue3OtherFilesPlugin.frontendFields(table)));
            for (int layer = 0; layer < outputs.size(); layer++) {
                String digest = java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                        .digest(outputs.get(layer).replace("\r\n", "\n").getBytes(StandardCharsets.UTF_8)));
                assertThat(digest).as("重构前完整敏感输出：场景 %s，层 %s", variant, layer)
                        .isEqualTo(expected[variant][layer]);
            }
        }
    }

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
        SensitiveGenerationValidator.validate(table);
        assertThat(Vue3OtherFilesPlugin.frontendFields(table).get(0).get("property")).isEqualTo("bodyText");
        for (String name : List.of("missing_", "ignored_", "bodyText")) {
            assertThatThrownBy(() -> fixture("<sensitiveCheck columns=\"" + name + "\"/>"))
                    .hasMessageContaining("article_").hasMessageContaining(name);
        }
        table.getBaseColumns().get(0).setFullyQualifiedJavaType(FullyQualifiedJavaType.getIntegerInstance());
        assertThatThrownBy(() -> SensitiveGenerationValidator.validate(table)).hasMessageContaining("String");
        IntrospectedTableMyBatis3Impl duplicate = fixture(CHECK);
        duplicate.getTableConfiguration().getSensitiveCheckConfigurations()
                .add(new SensitiveCheckConfiguration("content_", null, "WARN", null));
        assertThatThrownBy(() -> SensitiveGenerationValidator.validate(duplicate)).hasMessageContaining("重复");
        for (String property : List.of("class", "id", "not-valid")) {
            IntrospectedTableMyBatis3Impl invalidProperty = fixture(CHECK);
            invalidProperty.getBaseColumns().get(0).setJavaProperty(property);
            assertThatThrownBy(() -> SensitiveGenerationValidator.validate(invalidProperty)).hasMessageContaining("Java 属性");
        }
    }

    @Test
    void rejectsInheritedFieldsUnsupportedModelsServicesAndUnrestrictedExampleUpdates() throws Exception {
        assertThatThrownBy(() -> fixture("<sensitiveCheck columns=\"id_\"/>"))
                .hasMessageContaining("父类").hasMessageContaining("id_");
        IntrospectedTableMyBatis3Impl table = fixture(CHECK);
        table.getTableConfiguration().getJavaServiceImplGeneratorConfiguration().setGenerate(false);
        assertThatThrownBy(() -> SensitiveGenerationValidator.validate(table)).hasMessageContaining("Service");
        table.getTableConfiguration().getJavaServiceImplGeneratorConfiguration().setGenerate(true);
        table.getTableConfiguration().setUpdateByExampleStatementEnabled(true);
        assertThatThrownBy(() -> SensitiveGenerationValidator.validate(table)).hasMessageContaining("updateByExample");
        table.getTableConfiguration().setUpdateByExampleStatementEnabled(false);
        table.getTableConfiguration().getJavaModelGeneratorConfiguration().setGenerate(false);
        assertThatThrownBy(() -> SensitiveGenerationValidator.validate(table)).hasMessageContaining("模型");
        table.getTableConfiguration().getJavaModelGeneratorConfiguration().setGenerate(true);
        table.setRules(new HierarchicalModelRules(table));
        assertThatThrownBy(() -> SensitiveGenerationValidator.validate(table)).hasMessageContaining("分层模型");
    }

    @Test
    void rendersActualModelAndServiceWithCompleteMethodMatrixAndStableRegeneration() throws Exception {
        IntrospectedTableMyBatis3Impl table = fixture(CHECK);
        TopLevelClass model = model(table);
        Field field = model.getFields().stream().filter(item -> "bodyText".equals(item.getName())).findFirst().orElseThrow();
        assertThat(field.getAnnotations()).contains(
                "@SensitiveCheck(enable = true, strategy = SensitiveCheckStrategy.INHERIT, contentType = SensitiveContentType.PLAIN_TEXT)");
        String before = format(model);
        SensitiveModelAnnotations.addModelAnnotations(model, table);
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
        SensitiveWriteMethodsElement.addWriteAnnotations(service, table);
        assertThat(format(service)).isEqualTo(source);
        assertThat(format(service(fixture(CHECK)))).isEqualTo(source);
    }

    /** 受管 CAS 启用时仅真实 CAS 单行方法获得敏感标记，旧 disabled 更新与 upsert 不得标记。 */
    @Test
    void marksCasSingleRowMethodsInsteadOfDisabledUpdateMethods() throws Exception {
        IntrospectedTableMyBatis3Impl table = fixture(CHECK, true, false);
        TopLevelClass service = service(table);
        for (String name : List.of("updateByPrimaryKeyCas", "updateByPrimaryKeySelectiveCas")) {
            Method method = service.getMethods().stream()
                    .filter(item -> name.equals(item.getName())).findFirst().orElseThrow();
            assertThat(method.getParameters()).hasSize(2);
            assertThat(method.getAnnotations()).contains(
                    "@SensitiveWrite(operationKey = \"sample.entity.Article:" + name + "\", entityArgumentIndex = 0)",
                    "@org.springframework.transaction.annotation.Transactional(propagation = "
                            + "org.springframework.transaction.annotation.Propagation.MANDATORY, rollbackFor = Exception.class)");
        }
        for (String name : List.of("updateByPrimaryKey", "updateByPrimaryKeySelective", "insertOrUpdate")) {
            service.getMethods().stream().filter(item -> name.equals(item.getName())).findFirst().ifPresent(method -> {
                assertThat(method.getAnnotations()).noneMatch(value -> value.startsWith("@SensitiveWrite"));
                assertThat(method.getBodyLines()).contains("throw new IllegalStateException(\"ENTRY_DISABLED\");");
            });
        }
    }

    /** CAS 配置仍要求生成 update batch 时必须在服务输出前明确拒绝敏感受管组合。 */
    @Test
    void rejectsSensitiveCasWhenUpdateBatchIsEnabled() throws Exception {
        assertThatThrownBy(() -> fixture(CHECK, true, true))
                .hasMessageContaining("没有受支持的 CAS 批量更新");
    }

    @Test
    void ignoresUnconfiguredAndDisabledSensitiveOutput() throws Exception {
        IntrospectedTableMyBatis3Impl plain = fixture("");
        TopLevelClass model = model(plain);
        TopLevelClass service = service(plain);
        String modelBefore = format(model);
        String serviceBefore = format(service);
        SensitiveModelAnnotations.addModelAnnotations(model, plain);
        SensitiveWriteMethodsElement.addWriteAnnotations(service, plain);
        assertThat(format(model)).isEqualTo(modelBefore).doesNotContain("Sensitive");
        assertThat(format(service)).isEqualTo(serviceBefore).doesNotContain("Sensitive");
        IntrospectedTableMyBatis3Impl disabled = fixture("<sensitiveCheck columns=\"content_\" enable=\"false\" strategy=\"WARN\"/>");
        TopLevelClass disabledModel = model(disabled);
        TopLevelClass disabledService = service(disabled);
        SensitiveModelAnnotations.addModelAnnotations(disabledModel, disabled);
        SensitiveWriteMethodsElement.addWriteAnnotations(disabledService, disabled);
        assertThat(format(disabledModel)).doesNotContain("Sensitive");
        assertThat(format(disabledService)).doesNotContain("Sensitive");
    }

    @Test
    void refusesSuppressedFieldsAndWrongBatchParameterRoles() throws Exception {
        IntrospectedTableMyBatis3Impl table = fixture(CHECK);
        assertThatThrownBy(() -> SensitiveModelAnnotations.addModelAnnotations(new TopLevelClass("sample.Article"), table))
                .hasMessageContaining("content_").hasMessageContaining("未实际生成");
        assertThatThrownBy(() -> SensitiveModelAnnotations.requireModelGenerated(table)).hasMessageContaining("插件");
        assertThatThrownBy(() -> SensitiveWriteMethodsElement.requireServiceGenerated(table)).hasMessageContaining("插件");
        TopLevelClass service = service(table);
        Method batch = service.getMethods().stream().filter(item -> "insertBatch".equals(item.getName())).findFirst().orElseThrow();
        batch.getParameters().clear();
        batch.addParameter(new Parameter(new FullyQualifiedJavaType("java.util.List<java.lang.String>"), "records"));
        assertThatThrownBy(() -> SensitiveWriteMethodsElement.addWriteAnnotations(service, table))
                .hasMessageContaining("insertBatch").hasMessageContaining("List");
    }

    @Test
    void rendersEscapedFrontendMappingAndKeepsUnconfiguredTemplateBytes() throws Exception {
        IntrospectedTableMyBatis3Impl table = fixture(CHECK);
        table.getBaseColumns().get(0).setRemarks("内容\"\\\n标签");
        List<Map<String, String>> mapping = Vue3OtherFilesPlugin.frontendFields(table);
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
        SensitiveGenerationValidator.validate(table);
        assertThat(format(model(table))).contains("@SensitiveCheck", "String bodyText");
        content.setFullyQualifiedJavaType(new FullyQualifiedJavaType("byte[]"));
        assertThatThrownBy(() -> SensitiveGenerationValidator.validate(table)).hasMessageContaining("String");
    }

    @Test
    void rejectsLogicalJsonDerivedRepresentationsAndGeneratedAlwaysColumns() throws Exception {
        IntrospectedTableMyBatis3Impl table = fixture(CHECK);
        ColumnOverride override = new ColumnOverride("content_");
        override.addProperty("logicalType", "JSON");
        table.getTableConfiguration().addColumnOverride(override);
        assertThatThrownBy(() -> SensitiveGenerationValidator.validate(table)).hasMessageContaining("适配器");
        table.getTableConfiguration().getColumnOverrides().clear();
        table.getTableConfiguration().getOverridePropertyConfigurations().add(
                new OverridePropertyValueGeneratorConfiguration(table.getContext(), table.getTableConfiguration(), "content_"));
        assertThatThrownBy(() -> SensitiveGenerationValidator.validate(table)).hasMessageContaining("双表示");
        table.getTableConfiguration().getOverridePropertyConfigurations().clear();
        table.getBaseColumns().get(0).setGeneratedAlways(true);
        assertThatThrownBy(() -> SensitiveGenerationValidator.validate(table)).hasMessageContaining("可写列");
    }

    @Test
    void rejectsNonProxyableMethodsAndUnverifiedTransactionPropagation() throws Exception {
        IntrospectedTableMyBatis3Impl table = fixture(CHECK);
        TopLevelClass generated = service(table);
        Method insert = generated.getMethods().stream().filter(method -> "insert".equals(method.getName())).findFirst().orElseThrow();
        insert.setFinal(true);
        assertThatThrownBy(() -> SensitiveWriteMethodsElement.addWriteAnnotations(generated, table)).hasMessageContaining("可代理");
        insert.setFinal(false);
        insert.getAnnotations().removeIf(value -> value.startsWith("@Transactional"));
        insert.addAnnotation("@Transactional(propagation = Propagation.NOT_SUPPORTED)");
        assertThatThrownBy(() -> SensitiveWriteMethodsElement.addWriteAnnotations(generated, table)).hasMessageContaining("事务声明");
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
            assertThat(context.getTargetRuntime()).isNull();
            assertThat(context.getEffectiveTargetRuntime()).isEqualTo("MyBatis3");
            assertThat(org.mybatis.generator.internal.ObjectFactory.createIntrospectedTableForValidation(context))
                    .isExactlyInstanceOf(IntrospectedTableMyBatis3Impl.class);
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
        return fixture(check, false, false);
    }

    private IntrospectedTableMyBatis3Impl fixture(String check, boolean casContract, boolean updateBatch)
            throws Exception {
        String configuration = XML.formatted(check);
        if (casContract) {
            configuration = configuration.replace("tableName=\"article_\"",
                    "tableName=\"article_\" enableCasWriteContract=\"true\"")
                    .replace("value=\"com.vgosoft.core.entity.abs.AbstractEntity\"",
                            "value=\"com.vgosoft.core.entity.abs.AbstractPersistenceLockEntity\"")
                    .replace("<commentGenerator>",
                            "<property name=\"additionalDbTypes\" value=\"mysql,h2\"/><commentGenerator>");
        }
        Context context = new ConfigurationParser(new ArrayList<>()).parseConfiguration(
                new StringReader(configuration)).getContexts().get(0);
        java.lang.reflect.Field plugins = Context.class.getDeclaredField("pluginAggregator");
        plugins.setAccessible(true);
        plugins.set(context, new PluginAggregator());
        IntrospectedTableMyBatis3Impl table = (IntrospectedTableMyBatis3Impl)
                org.mybatis.generator.internal.ObjectFactory.createIntrospectedTable(
                        context.getTableConfigurations().get(0), new FullyQualifiedTable(null, null, "article_", "Article", null,
                                true, null, null, null, false, null, context), context);
        if (casContract) {
            table.getTableConfiguration().setUpdateBatchStatementEnabled(updateBatch);
        }
        table.setRemarks("文章");
        table.addColumn(column("id_", "id"));
        table.addPrimaryKeyColumn("id_");
        if (casContract) {
            IntrospectedColumn version = column("version_", "version");
            version.setFullyQualifiedJavaType(new FullyQualifiedJavaType("java.lang.Integer"));
            version.setJdbcType(Types.INTEGER);
            version.setJdbcTypeName("INTEGER");
            version.setNullable(false);
            table.addColumn(version);
            for (String[] audit : List.of(
                    new String[]{"created_", "created"},
                    new String[]{"modified_", "modified"})) {
                IntrospectedColumn timestamp = column(audit[0], audit[1]);
                timestamp.setFullyQualifiedJavaType(new FullyQualifiedJavaType("java.time.LocalDateTime"));
                timestamp.setJdbcType(Types.TIMESTAMP);
                timestamp.setJdbcTypeName("TIMESTAMP");
                table.addColumn(timestamp);
            }
            table.addColumn(column("created_id", "createdId"));
            table.addColumn(column("modified_id", "modifiedId"));
        }
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
        return (TopLevelClass) modelOutput(table).get(0);
    }

    private List<CompilationUnit> modelOutput(IntrospectedTableMyBatis3Impl table) {
        BaseRecordGenerator generator = new BaseRecordGenerator("src");
        generator.setContext(table.getContext());
        generator.setIntrospectedTable(table);
        generator.setProgressCallback(new SilentProgress());
        generator.setWarnings(new ArrayList<>());
        return generator.getCompilationUnits();
    }

    private TopLevelClass service(IntrospectedTableMyBatis3Impl table) {
        return (TopLevelClass) serviceOutput(table).get(0);
    }

    private List<CompilationUnit> serviceOutput(IntrospectedTableMyBatis3Impl table) {
        JavaServiceImplGenerator generator = new JavaServiceImplGenerator("src");
        generator.setContext(table.getContext());
        generator.setIntrospectedTable(table);
        generator.setProgressCallback(new SilentProgress());
        generator.setWarnings(new ArrayList<>());
        return generator.getCompilationUnits();
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
     * 在真实模型和服务回调中记录执行顺序，并模拟插件否决或修改。
     *
     * @author vgosoft
     */
    private static final class SensitiveProbePlugin extends PluginAdapter {
        private final List<String> calls = new ArrayList<>();
        private boolean approve = true;
        private java.util.function.Consumer<TopLevelClass> modelChange = model -> { };
        private java.util.function.Consumer<TopLevelClass> serviceChange = service -> { };

        @Override
        public boolean validate(List<String> warnings) {
            return true;
        }

        @Override
        public boolean modelBaseRecordClassGenerated(TopLevelClass model, AbstractIntrospectedTable table) {
            calls.add("model");
            assertThat(model.getFields().stream().flatMap(field -> field.getAnnotations().stream()))
                    .noneMatch(annotation -> annotation.startsWith("@SensitiveCheck"));
            modelChange.accept(model);
            return approve;
        }

        @Override
        public boolean serviceImplGenerated(TopLevelClass service, AbstractIntrospectedTable table) {
            calls.add("service");
            if (SensitiveFieldResolver.isEnabled(table)) {
                assertThat(service.getMethods().stream().map(Method::getName)).contains("insertOrUpdateSelective");
            }
            assertThat(service.getMethods().stream().flatMap(method -> method.getAnnotations().stream()))
                    .noneMatch(annotation -> annotation.startsWith("@SensitiveWrite"));
            serviceChange.accept(service);
            return approve;
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
