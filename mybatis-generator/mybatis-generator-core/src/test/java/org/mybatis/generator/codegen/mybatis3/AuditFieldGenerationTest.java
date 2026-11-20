package org.mybatis.generator.codegen.mybatis3;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.GeneratedFile;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证消费配置生成物中的审计字段初始化、VO 映射与更新 SQL 契约。
 *
 * @author vgosoft-agent
 */
class AuditFieldGenerationTest {
    private static Map<String, String> generatedFiles;

    @BeforeAll
    static void generateConsumerSnapshot() throws Exception {
        var parser = new ConfigurationParser(new ArrayList<>());
        var config = parser.parseConfiguration(new StringReader(CasConsumerBaselineTest.consumerSnapshotXml()));
        parser.customConfig(config);
        // 固化基线对应“全量接口权限”语义；快照未配置 apiPermissions 时按 all 注入。
        config.getContexts().forEach(context -> context.getTableConfigurations().forEach(tc -> {
            var controller = tc.getJavaControllerGeneratorConfiguration();
            if (controller != null && controller.isGenerate()) {
                controller.addProperty(PropertyRegistry.CONTROLLER_API_PERMISSIONS, "all");
            }
        }));
        generatedFiles = generate(config);
    }

    static Map<String, String> generate(Configuration config) throws Exception {
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
            // targetPackage 为空的产物（如各表 modelPath 下的 PrivateTableColumnSlots.vue）须按
            // targetProject 尾部目录区分，避免同名覆盖与重复键误报。
            if (file.getTargetPackage().isEmpty()) {
                String project = file.getTargetProject().replace('/', '\\');
                int last = project.lastIndexOf('\\');
                name = (last >= 0 ? project.substring(last + 1) + "\\" : "") + name;
            }
            assertThat(result.put(name, CasConsumerBaselineTest.normalize(file.getFormattedContent()))).isNull();
        }
        return result;
    }

    @Test
    void entityAndCreateVoDoNotInitializeServerOwnedAuditFields() {
        for (String fileName : List.of("SensitiveCategory.java", "SensitiveCategoryCreateVo.java")) {
            String source = output(fileName);
            assertThat(source).as(fileName)
                    .doesNotContain("VCurrentUserUtil", "getCreatedId", "getModifiedId",
                            "this.created =", "this.modified =", "this.createdId =",
                            "this.modifiedId =", "this.tenantId =");
        }
    }

    @Test
    void voToEntityMappingsIgnoreServerOwnedAuditFields() {
        String mappings = output("SensitiveCategoryMappings.java");

        assertThat(mappings).contains(
                "@Mapping(target = \"created\", ignore = true)",
                "@Mapping(target = \"createdId\", ignore = true)",
                "@Mapping(target = \"modified\", ignore = true)",
                "@Mapping(target = \"modifiedId\", ignore = true)",
                "@Mapping(target = \"version\", ignore = true)");

        int updateMethod = mappings.indexOf("fromSensitiveCategoryUpdateVo");
        int methodStart = mappings.lastIndexOf("/**", updateMethod);
        int methodEnd = mappings.indexOf(';', updateMethod);
        assertThat(mappings.substring(methodStart, methodEnd))
                .doesNotContain("@Mapping(target = \"version\", ignore = true)");
    }

    @Test
    void mapperUpdatesProtectCreationAndTenantFieldsAndIncrementVersion() {
        String mapper = output("SensitiveCategoryMapper.xml");
        Matcher matcher = Pattern.compile("(?s)<update\\b[^>]*>.*?</update>").matcher(mapper);
        StringBuilder updates = new StringBuilder();
        while (matcher.find()) {
            updates.append(matcher.group()).append('\n');
        }

        assertThat(updates).isNotEmpty()
                .contains("modified_ =", "modified_id =", "version_ = version_ + 1")
                .doesNotContain("created_ =", "created_id =", "tenant_id =");
    }

    private static String output(String fileName) {
        return generatedFiles.entrySet().stream()
                .filter(entry -> entry.getKey().endsWith("\\" + fileName))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new AssertionError("缺少生成文件：" + fileName));
    }
}
