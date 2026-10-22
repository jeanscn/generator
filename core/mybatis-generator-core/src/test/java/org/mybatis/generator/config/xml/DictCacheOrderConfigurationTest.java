package org.mybatis.generator.config.xml;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.TableConfiguration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证字典预取排序配置的 DTD、Parser 与兼容默认值。
 *
 * @author vgosoft
 */
class DictCacheOrderConfigurationTest {

    private static final Path ORGANIZATION_CONFIG = Path.of(
            "D:\\java\\idea_projs7\\project-vgosoft\\vgosoft-organization\\src\\main\\resources\\generatorConfig.xml");

    /** 验证组织机构配置通过当前 DTD 解析并保留 opt-in 的列顺序。 */
    @Test
    void parsesOptInOrderColumnsFromConsumerConfiguration() throws Exception {
        Configuration configuration = parse(Files.readString(ORGANIZATION_CONFIG));

        TableConfiguration orgUser = configuration.getContexts().get(0).getTableConfigurations().stream()
                .filter(table -> "org_user".equals(table.getTableName()))
                .findFirst()
                .orElseThrow();
        assertThat(orgUser.getVoCacheGeneratorConfiguration().getDictCacheOrderColumns())
                .containsExactly("sort_", "created_", "id_");
    }

    /** 验证未配置排序元数据时保持旧默认输出，非法终止键在配置阶段被拒绝。 */
    @Test
    void keepsLegacyDefaultAndRejectsUnstableOrderConfiguration() throws Exception {
        String configured = Files.readString(ORGANIZATION_CONFIG);
        String optIn = "dictCacheOrderColumns=\"sort_,created_,id_\"";
        assertThat(configured).contains(optIn);

        Configuration legacy = parse(configured.replace(optIn, ""));
        TableConfiguration legacyOrgUser = legacy.getContexts().get(0).getTableConfigurations().stream()
                .filter(table -> "org_user".equals(table.getTableName()))
                .findFirst()
                .orElseThrow();
        assertThat(legacyOrgUser.getVoCacheGeneratorConfiguration().getDictCacheOrderColumns()).isEmpty();

        String unstable = configured.replace(optIn, "dictCacheOrderColumns=\"sort_,created_\"");
        TableConfiguration unstableOrgUser = parse(unstable).getContexts().get(0)
                .getTableConfigurations().stream()
                .filter(table -> "org_user".equals(table.getTableName()))
                .findFirst()
                .orElseThrow();
        List<String> errors = new ArrayList<>();
        unstableOrgUser.validate(errors, 0);
        assertThat(errors).anyMatch(error -> error.contains("final dictCacheOrderColumns entry"));
    }

    private static Configuration parse(String xml) throws Exception {
        return new ConfigurationParser(new ArrayList<>())
                .parseConfiguration(new java.io.StringReader(xml));
    }
}
