package org.mybatis.generator.codegen.mybatis3;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
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
        generatedFiles = CasWriteGenerationTest.generate(CasWriteGenerationTest.parse(null, false));
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
