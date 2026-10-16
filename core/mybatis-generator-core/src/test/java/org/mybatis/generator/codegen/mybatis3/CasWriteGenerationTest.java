package org.mybatis.generator.codegen.mybatis3;

import com.vgosoft.core.db.query.DatabaseType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.*;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.*;

/**
 * 经实际 Parser、内省、插件及各层生成链验证受管 CAS 的默认兼容与失败门禁。
 *
 * @author vgosoft
 */
class CasWriteGenerationTest {
    private static Configuration parse(String value, boolean customize) throws Exception {
        var parser = new ConfigurationParser(new ArrayList<>());
        var config = parser.parseConfiguration(new StringReader(CasConsumerBaselineTest.consumerXml(value)));
        if (customize) { parser.customConfig(config); }
        return config;
    }
    private static Map<String, String> generate(Configuration config) throws Exception {
        var generator = new MyBatisGenerator(config, new DefaultShellCallback(true), new ArrayList<>());
        generator.generate(null, null, null, false);
        List<GeneratedFile> files = new ArrayList<>(generator.getGeneratedJavaFiles());
        files.addAll(generator.getGeneratedXmlFiles()); files.addAll(generator.getGeneratedKotlinFiles());
        files.addAll(generator.getGeneratedHtmlFiles());
        var field = MyBatisGenerator.class.getDeclaredField("otherGeneratedFiles");
        field.setAccessible(true);
        files.addAll((List<? extends GeneratedFile>) field.get(generator));
        Map<String, String> result = new TreeMap<>();
        for (var file : files) {
            String name = (file.getTargetPackage().isEmpty() ? "" : file.getTargetPackage().replace('.', '\\') + "\\")
                    + file.getFileName();
            assertThat(result.put(name, CasConsumerBaselineTest.normalize(file.getFormattedContent()))).isNull();
        }
        return result;
    }
    private static void plugin(Configuration config, String mode) {
        var plugin = new PluginConfiguration();
        plugin.setConfigurationType(HostilePlugin.class.getName()); plugin.addProperty("mode", mode);
        config.getContexts().get(0).addPluginConfiguration(plugin);
    }

    /** 当前 DTD 和程序配置均严格解析，省略关闭；空值、大小写及未知值不能被吞为 false。 */
    @ParameterizedTest
    @ValueSource(strings = {"", " ", "TRUE", "False", "yes", "1", "no", " true ", "false "})
    void rejectsIllegalValuesBeforeMetadata(String value) {
        assertThatThrownBy(() -> parse(value, false)).isInstanceOfAny(
                org.mybatis.generator.exception.XMLParserException.class, IllegalArgumentException.class);
        var table = new TableConfiguration(new Context(null), new ArrayList<>());
        table.setTableName("probe");
        assertThatThrownBy(() -> table.parseEnableCasWriteContract(value)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("probe").hasMessageContaining("true 或 false");
    }

    /** 实施前已保存的完整原始 Parser/Generator 输出，经日期归一化后逐文件与关闭开关保持一致。 */
    @Test
    void retainsCompletePreChangeRawBaseline() throws Exception {
        Map<String, String> current = generate(parse(null, false));
        Map<String, String> before = new TreeMap<>();
        try (var input = new java.util.zip.ZipInputStream(java.util.Objects.requireNonNull(
                getClass().getResourceAsStream("/sc03/non-enabled-parser-baseline.zip")))) {
            for (var entry = input.getNextEntry(); entry != null; entry = input.getNextEntry()) {
                if (!entry.isDirectory()) {
                    before.put(entry.getName().replace('/', '\\'), CasConsumerBaselineTest.normalize(
                            new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8)));
                }
            }
        }
        assertThat(current).hasSize(75).containsOnlyKeys(before.keySet());
        for (String name : before.keySet()) { assertThat(current.get(name)).as(name).isEqualTo(before.get(name)); }
        assertThat(generate(parse("false", false))).isEqualTo(current);
    }

    /** 真实 Maven 自定义配置链的省略与 false 全输出一致，仅两表 true 改变必需接口、SQL、VO 和单一资源。 */
    @Test
    void retainsAllDisabledOutputsAndLimitsOptInDiff() throws Exception {
        var omitted = generate(parse(null, true));
        assertThat(omitted).hasSize(79).isEqualTo(generate(parse("false", true)));
        var enabled = generate(parse("true", true));
        assertThat(enabled).hasSize(80);
        var changed = enabled.keySet().stream().filter(k -> !enabled.get(k).equals(omitted.get(k))).toList();
        assertThat(changed).hasSize(11).allMatch(k -> k.endsWith("sensitive.xml")
                || k.endsWith("Mapper.xml") || k.contains("\\codegen\\dao\\GenSensitive")
                || k.contains("\\codegen\\service\\") || k.contains("\\pojo\\abs\\AbstractSensitive"));
        assertThat(enabled.get("META-INF\\vgosoft\\write-contracts\\sensitive.xml"))
                .contains("managed-cas-1", "ENTRY_DISABLED").doesNotContain("sensitive_rule_revision");
    }

    /** 缺少任何必需层均明确失败，不把部分文件当作成功 CAS 输出。 */
    @ParameterizedTest
    @ValueSource(strings = {"model", "mapper", "xml", "service", "impl"})
    void requiresAllOutputLayers(String layer) throws Exception {
        var config = parse("true", true);
        var tc = config.getContexts().get(0).getTableConfigurations().get(0);
        switch (layer) {
            case "model" -> tc.getJavaModelGeneratorConfiguration().setGenerate(false);
            case "mapper" -> tc.getJavaClientGeneratorConfiguration().setGenerate(false);
            case "xml" -> tc.getSqlMapGeneratorConfiguration().setGenerate(false);
            case "service" -> tc.getJavaServiceGeneratorConfiguration().setGenerate(false);
            case "impl" -> tc.getJavaServiceImplGeneratorConfiguration().setGenerate(false);
            default -> throw new IllegalArgumentException(layer);
        }
        assertThatThrownBy(() -> generate(config)).hasMessageContaining("sensitive_category").hasMessageContaining("输出");
    }

    /** 实际元数据在缺列、错误类型、可空版本、自增或复合主键、未知父类及非支持方言时必须拒绝。 */
    @ParameterizedTest
    @ValueSource(strings = {"missing-version", "missing-id", "wrong-java", "wrong-jdbc", "nullable-version",
            "auto-key", "composite-key", "parent", "dialect", "missing-audit", "wrong-audit", "generated-key"})
    void validatesActualMetadataAndInheritance(String mode) throws Exception {
        var config = parse("true", true);
        plugin(config, mode);
        assertThatThrownBy(() -> generate(config)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sensitive_category");
    }

    /** 插件否决任一必需输出或修改写骨架、版本参数、添加未知写语句时，都必须在写盘前失败。 */
    @ParameterizedTest
    @ValueSource(strings = {"veto-model", "veto-mapper", "veto-xml", "veto-service", "veto-impl", "veto-update-vo",
            "tamper-version", "unknown-write", "duplicate-write", "tamper-signature",
            "swallow-conflict", "remove-transaction", "enable-old-service"})
    void rejectsPluginVetoOrCorruption(String mode) throws Exception {
        var config = parse("true", true);
        plugin(config, mode);
        assertThatThrownBy(() -> generate(config)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("enableCasWriteContract").hasMessageContaining("sensitive_category");
    }

    /** 可覆盖服务骨架经原写盘器替换，移除严格标记后人工扩展既不进入输出也不被改写。 */
    @Test
    void preservesProtectedExtensionThroughActualGenerationAndWriter() throws Exception {
        var config = parse("true", true);
        var tc = config.getContexts().get(0).getTableConfigurations().get(0);
        Path root = Path.of("target", "sc03-extension-protection").toAbsolutePath();
        config.getContexts().get(0).getJavaModelGeneratorConfiguration().setTargetProject(root.toString());
        var service = tc.getJavaServiceImplGeneratorConfiguration();
        service.setTargetProject(root.toString());
        Path file = root.resolve(service.getTargetPackage().replace('.', '\\')).resolve("SensitiveCategoryImpl.java");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "/** @VCF.overridable */\nclass SensitiveCategoryImpl {}");
        var generator = new MyBatisGenerator(config, new DefaultShellCallback(true), new ArrayList<>());
        generator.generate(null, null, null, false);
        var output = generator.getGeneratedJavaFiles().stream()
                .filter(f -> f.getFileName().equals("SensitiveCategoryImpl.java")).findFirst().orElseThrow();
        var writer = MyBatisGenerator.class.getDeclaredMethod("writeGeneratedJavaFile", GeneratedJavaFile.class, ProgressCallback.class);
        writer.setAccessible(true);
        writer.invoke(generator, output, new ProgressCallback() { });
        assertThat(Files.readString(file)).contains("extends AbstractSensitiveCategoryImpl");
        String human = "/** 人工扩展，已移除可覆盖标记。 */\nclass SensitiveCategoryImpl { int manual = 1; }";
        Files.writeString(file, human);
        var protectedGeneration = new MyBatisGenerator(config, new DefaultShellCallback(true), new ArrayList<>());
        protectedGeneration.generate(null, null, null, false);
        assertThat(protectedGeneration.getGeneratedJavaFiles())
                .noneMatch(f -> f.getFileName().equals("SensitiveCategoryImpl.java"));
        assertThat(Files.readString(file)).isEqualTo(human);
    }

    /** 显式强制覆盖清单仍只开放指定 VO 类型，移除 modelVo 后既有人工文件保持且不进入输出。 */
    @Test
    void preservesExistingForceUpdateElementSelection() throws Exception {
        var config = parse("true", true);
        var context = config.getContexts().get(0);
        var vo = context.getTableConfigurations().get(0).getVoGeneratorConfiguration();
        Path root = Path.of("target", "sc03-vo-protection").toAbsolutePath();
        context.getJavaModelGeneratorConfiguration().setTargetProject(root.toString());
        vo.setTargetProject(root.toString());
        Path file = root.resolve(vo.getTargetPackage().replace('.', '\\')).resolve("SensitiveCategoryVo.java");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "/** 人工 VO。 */\nclass SensitiveCategoryVo {}");
        assertThat(generate(config)).containsKey("com\\vgosoft\\sensitive\\pojo\\vo\\SensitiveCategoryVo.java");
        context.addProperty(PropertyRegistry.CONTEXT_FORCE_UPDATE_ELEMENT_LIST,
                context.getProperty(PropertyRegistry.CONTEXT_FORCE_UPDATE_ELEMENT_LIST).replace("modelVo,", ""));
        assertThat(generate(config)).doesNotContainKey("com\\vgosoft\\sensitive\\pojo\\vo\\SensitiveCategoryVo.java");
        assertThat(Files.readString(file)).isEqualTo("/** 人工 VO。 */\nclass SensitiveCategoryVo {}");
    }

    /**
     * 在真实回调位置破坏指定能力，验证最终结果而非模拟校验器返回值。
     *
     * @author vgosoft
     */
    public static class HostilePlugin extends PluginAdapter {
        private String mode;
        @Override
        public boolean validate(List<String> warnings) { mode = properties.getProperty("mode"); return true; }
        @Override
        public void initialized(AbstractIntrospectedTable table) {
            if (!table.getTableConfiguration().isCasWriteContractEnabled()) { return; }
            var version = table.getColumn("version_").orElseThrow();
            switch (mode) {
                case "missing-version" -> table.getBaseColumns().remove(version);
                case "missing-id" -> table.getPrimaryKeyColumns().clear();
                case "wrong-java" -> version.setFullyQualifiedJavaType(new FullyQualifiedJavaType("java.lang.Long"));
                case "wrong-jdbc" -> version.setJdbcTypeName("BIGINT");
                case "nullable-version" -> version.setNullable(true);
                case "auto-key" -> table.getPrimaryKeyColumns().get(0).setAutoIncrement(true);
                case "composite-key" -> table.addPrimaryKeyColumn("state_");
                case "parent" -> table.getTableConfiguration().addProperty(PropertyRegistry.ROOT_CLASS, "java.lang.Object");
                case "dialect" -> table.getTableConfiguration().getAdditionalDbTypes().add(DatabaseType.POSTGRE_SQL);
                case "missing-audit" -> table.getBaseColumns().remove(table.getColumn("modified_id").orElseThrow());
                case "wrong-audit" -> table.getColumn("created_").orElseThrow()
                        .setFullyQualifiedJavaType(new FullyQualifiedJavaType("java.util.Date"));
                case "generated-key" -> table.getTableConfiguration()
                        .setGeneratedKey(new GeneratedKey("id_", "JDBC", true, null));
                default -> { /* 其他模式在相应产物回调中处理。 */ }
            }
        }
        @Override
        public boolean clientGenerated(Interface unit, AbstractIntrospectedTable table) {
            if (mode.equals("tamper-signature")) {
                unit.getMethods().stream().filter(m -> m.getName().equals("updateByPrimaryKeyCas"))
                        .forEach(m -> m.getParameters().remove(1));
            }
            return !mode.equals("veto-mapper");
        }
        @Override
        public boolean modelBaseRecordClassGenerated(TopLevelClass unit, AbstractIntrospectedTable table) { return !mode.equals("veto-model"); }
        @Override
        public boolean serviceGenerated(Interface unit, AbstractIntrospectedTable table) { return !mode.equals("veto-service"); }
        @Override
        public boolean serviceImplGenerated(TopLevelClass unit, AbstractIntrospectedTable table) {
            for (var method : unit.getMethods()) {
                if (method.getName().equals("updateByPrimaryKeyCas")) {
                    if (mode.equals("swallow-conflict")) {
                        method.getBodyLines().clear();
                        method.addBodyLine("try { mapper.updateByPrimaryKeyCas(record, expectedVersion); } catch (RuntimeException ignored) { }");
                        method.addBodyLine("return ServiceResult.success(record, 1);");
                    } else if (mode.equals("remove-transaction")) { method.getAnnotations().clear(); }
                } else if (method.getName().equals("updateBatchByPrimaryKey") && mode.equals("enable-old-service")) {
                    method.getBodyLines().clear();
                    method.addBodyLine("return null;");
                }
            }
            return !mode.equals("veto-impl");
        }
        @Override
        public boolean voModelUpdateClassGenerated(TopLevelClass unit, AbstractIntrospectedTable table) { return !mode.equals("veto-update-vo"); }
        @Override
        public boolean sqlMapDocumentGenerated(Document document, AbstractIntrospectedTable table) {
            if (!table.getTableConfiguration().isCasWriteContractEnabled()) { return true; }
            if (mode.equals("unknown-write")) {
                var node = new XmlElement("update");
                node.addAttribute(new org.mybatis.generator.api.dom.xml.Attribute("id", "unknownWrite"));
                node.addElement(new TextElement("update sensitive_category set version_=1"));
                document.getRootElement().addElement(node);
            } else if (mode.equals("duplicate-write")) {
                var node = document.getRootElement().getElements().stream().filter(e -> e instanceof XmlElement x
                        && x.getName().equals("update")).findFirst().orElseThrow();
                document.getRootElement().addElement(node);
            } else if (mode.equals("tamper-version")) {
                for (var e : document.getRootElement().getElements()) {
                    if (e instanceof XmlElement x && x.getName().equals("update")) {
                        x.addElement(new TextElement("and version_=0"));
                    }
                }
            }
            return !mode.equals("veto-xml");
        }
    }
}
