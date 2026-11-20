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
 * 在内存中验证敏感配置、实体、受检写方法标记和前端映射，不连接数据库或写业务产物。
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
                  <generateService generate="true" generateUnitTest="false">
                    %s
                  </generateService>
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
        List<String> reasons = List.of("受检写方法必须是可代理的 public 非静态、非 final 具体方法",
                "只支持 (Entity)、(List<Entity>) 或 (Entity, Example) 参数形态",
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
                .hasMessage("表 article_，列/方法 *：插件禁止生成 Service，无法落地受检写方法标记");
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
        // 实体与 TS 摘要随 query-condition 组合条件（anyWhereCondition 受限条件描述）与 vue3 类型模板升级而更新。
        String[][] expected = {
                {"62b907620c85f26f6d241048800c46edb59397e53b71aa76e39fe7d09aced9fd",
                 "a2b6b8f2f0493750c1423825da8bb310ff129045dd6dd1ae167920311bed6bab",
                 "0477a00c992498b8b2aaf849dba3f064672a4716c922836d807f21fe5a271f76"},
                {"2c068069c2faa2db34d7e2e52ee4d811bd8eac8a139acb8d16956f5d9b96e16c",
                 "d1038747e8ce2c3630351a24ea38e9f5cc2888339f22722e97c459764e76d911",
                 "2e92f4ea524d661056bc47754ae0f513ab012c3204a9a58bb7cdfad985680ad1"},
                {"2c068069c2faa2db34d7e2e52ee4d811bd8eac8a139acb8d16956f5d9b96e16c",
                 "d1038747e8ce2c3630351a24ea38e9f5cc2888339f22722e97c459764e76d911",
                 "2e92f4ea524d661056bc47754ae0f513ab012c3204a9a58bb7cdfad985680ad1"},
                {"6f7c611fd162cc59eedaf8257cd458990017f6dcb1e357d095ebf124874934c4",
                 "a2b6b8f2f0493750c1423825da8bb310ff129045dd6dd1ae167920311bed6bab",
                 "994c4d17c94eda72d8382d30e70f91ded8bcb767dc142777f3eec52c331b9ad3"}
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
        var richCheck = parse(CHECK.replace("/>", " contentType=\"RICH_TEXT\"/>"))
                .getTableConfigurations().get(0).getSensitiveCheckConfigurations().get(0);
        assertThat(richCheck.getContentType()).isEqualTo("RICH_TEXT");
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
        SensitiveGenerationValidator.validate(table);
        table.getTableConfiguration().getJavaModelGeneratorConfiguration().setGenerate(false);
        assertThatThrownBy(() -> SensitiveGenerationValidator.validate(table)).hasMessageContaining("模型");
        table.getTableConfiguration().getJavaModelGeneratorConfiguration().setGenerate(true);
        table.setRules(new HierarchicalModelRules(table));
        assertThatThrownBy(() -> SensitiveGenerationValidator.validate(table)).hasMessageContaining("分层模型");
    }

    /** 主子表级联包含多实体写入时仍拒绝启用敏感检测。 */
    @Test
    void rejectsMasterDetailCascadingWrites() throws Exception {
        IntrospectedTableMyBatis3Impl table = fixture(CHECK);
        var relation = new org.mybatis.generator.config.RelationGeneratorConfiguration();
        relation.setColumn("id_");
        relation.setSelect("sample.dao.DetailMapper.selectByArticleId");
        relation.setEnableUpdate(true);
        table.getTableConfiguration().getRelationGeneratorConfigurations().add(relation);
        assertThatThrownBy(() -> SensitiveGenerationValidator.validate(table))
                .hasMessageContaining("主子表级联");
    }

    /** 验证单实体、批量、upsert 写方法均生成敏感标记且重生成稳定。 */
    @Test
    void marksAllGeneratedWriteMethodsAndStaysIdempotent() throws Exception {
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
        List<String> writes = List.of("insert", "insertSelective", "updateByPrimaryKey",
                "updateByPrimaryKeySelective", "insertBatch", "updateBatchByPrimaryKey",
                "insertOrUpdate", "insertOrUpdateSelective");
        for (String name : writes) {
            Method method = service.getMethods().stream().filter(item -> name.equals(item.getName())).findFirst().orElseThrow();
            assertThat(method.getAnnotations()).contains(
                    "@SensitiveWrite(operationKey = \"sample.entity.Article:" + name + "\", entityArgumentIndex = 0)");
            assertThat(method.getAnnotations().stream().filter(value -> value.startsWith("@Transactional")).toList())
                    .hasSize(1).allSatisfy(value -> assertThat(value).contains("rollbackFor = Exception.class"));
        }
        String source = format(service);
        StaticJavaParser.parse(source);
        SensitiveWriteMethodsElement.addWriteAnnotations(service, table);
        assertThat(format(service)).isEqualTo(source);
        assertThat(format(service(fixture(CHECK)))).isEqualTo(source);
    }

    /** 验证 updateByExample* 服务方法生成时同样获得敏感标记（此类方法仅在缓存同步或关联更新时出现，这里直接补入模拟）。 */
    @Test
    void marksUpdateByExampleMethodsWhenGenerated() throws Exception {
        IntrospectedTableMyBatis3Impl table = fixture(CHECK);
        TopLevelClass service = service(table);
        FullyQualifiedJavaType entity = new FullyQualifiedJavaType(table.getBaseRecordType());
        FullyQualifiedJavaType example = new FullyQualifiedJavaType(table.getExampleType());
        for (String name : List.of("updateByExampleSelective", "updateByExampleSelectiveAndSelect")) {
            Method method = new Method(name);
            method.setVisibility(org.mybatis.generator.api.dom.java.JavaVisibility.PUBLIC);
            method.addParameter(new Parameter(entity, "record"));
            method.addParameter(new Parameter(example, "example"));
            service.addMethod(method);
        }
        SensitiveWriteMethodsElement.addWriteAnnotations(service, table);
        for (Method method : service.getMethods().stream()
                .filter(item -> item.getName().startsWith("updateByExample")).toList()) {
            assertThat(method.getParameters()).hasSize(2);
            assertThat(method.getAnnotations()).contains(
                    "@SensitiveWrite(operationKey = \"sample.entity.Article:" + method.getName()
                            + "\", entityArgumentIndex = 0)");
        }
    }

    /** 验证显式停用配置仍可封闭入口，被停用方法不获得敏感标记。 */
    @Test
    void skipsAnnotationForExplicitlyDisabledWriteMethods() throws Exception {
        IntrospectedTableMyBatis3Impl table = fixture(CHECK,
                "<disabledMethod signature=\"insertBatch(java.util.List)\" reason=\"业务决定封闭批量\"/>");
        Method batch = service(table).getMethods().stream()
                .filter(method -> "insertBatch".equals(method.getName()))
                .findFirst().orElseThrow();
        assertThat(batch.getAnnotations()).noneMatch(value -> value.startsWith("@SensitiveWrite"));
        assertThat(batch.getBodyLines()).anyMatch(value -> value.contains("VgoServiceEntryDisabledException"));
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
        batch.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "record"));
        assertThatThrownBy(() -> SensitiveWriteMethodsElement.addWriteAnnotations(service, table))
                .hasMessageContaining("insertBatch").hasMessageContaining("参数形态");
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
                        "updateByPrimaryKeySelective").contains(item.getName())).toList()) {
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
            assertThat(context.getTableConfigurations()).hasSize(4).allSatisfy(table ->
                    assertThat(table.getSensitiveCheckConfigurations()).isEmpty());
            var word = context.getTableConfigurations().stream().filter(table -> "sensitive_word".equals(table.getTableName()))
                    .findFirst().orElseThrow();
            assertThat(word.getColumnOverride("word_").getMaxLength()).isEqualTo(256);
        }
    }

    private Context parse(String check) throws Exception {
        return new ConfigurationParser(new ArrayList<>()).parseConfiguration(new StringReader(XML.formatted("", check)))
                .getContexts().get(0);
    }

    private IntrospectedTableMyBatis3Impl fixture(String check) throws Exception {
        return fixture(check, "");
    }

    private IntrospectedTableMyBatis3Impl fixture(String check, String disabledWrites)
            throws Exception {
        String configuration = XML.formatted(disabledWrites, check);
        Context context = new ConfigurationParser(new ArrayList<>()).parseConfiguration(
                new StringReader(configuration)).getContexts().get(0);
        java.lang.reflect.Field plugins = Context.class.getDeclaredField("pluginAggregator");
        plugins.setAccessible(true);
        plugins.set(context, new PluginAggregator());
        IntrospectedTableMyBatis3Impl table = (IntrospectedTableMyBatis3Impl)
                org.mybatis.generator.internal.ObjectFactory.createIntrospectedTable(
                        context.getTableConfigurations().get(0), new FullyQualifiedTable(null, null, "article_", "Article", null,
                                true, null, null, null, false, null, context), context);
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
        configuration.setTemplateLoader(new freemarker.cache.ClassTemplateLoader(getClass(), "/templates/vue3"));
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
