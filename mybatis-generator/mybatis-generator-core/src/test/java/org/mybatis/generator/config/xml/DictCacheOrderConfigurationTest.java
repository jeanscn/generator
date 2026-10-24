package org.mybatis.generator.config.xml;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.VoCacheGeneratorConfiguration;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 使用仓内离线配置验证当前 DTD 和字典排序配置，不依赖消费项目。
 *
 * @author vgosoft-agent
 */
class DictCacheOrderConfigurationTest {

    /** 默认排序留给内省解析，显式主键可位于中间且不要求字典键在末尾。 */
    @Test
    void parsesDefaultAndExplicitOrderWithoutTerminalKeyRestriction() throws Exception {
        assertThat(config("").getDictCacheOrderColumns()).isEmpty();
        VoCacheGeneratorConfiguration explicit = config("dictCacheOrderColumns=\"sort_,id_,created_\"");
        assertThat(explicit.getDictCacheOrderColumns()).containsExactly("sort_", "id_", "created_");
        List<String> errors = new ArrayList<>();
        explicit.validate(errors, "offline");
        assertThat(errors).noneMatch(error -> error.contains("dictCacheOrderColumns"));
    }

    /** 重复原始列名和关闭生成却声明排序配置仍须明确报错。 */
    @Test
    void rejectsDuplicateColumnsAndDisabledExplicitOrder() throws Exception {
        List<String> errors = new ArrayList<>();
        config("dictCacheOrderColumns=\"id_,id_\"").validate(errors, "offline");
        assertThat(errors).anyMatch(error -> error.contains("duplicate columns"));
        VoCacheGeneratorConfiguration disabled = config("dictCacheOrderColumns=\"sort_\"");
        disabled.setGenerate(false);
        errors.clear();
        disabled.validate(errors, "offline");
        assertThat(errors).anyMatch(error -> error.contains("requires generate=\"true\""));
    }

    private VoCacheGeneratorConfiguration config(String attributes) throws Exception {
        String xml;
        try (var stream = getClass().getResourceAsStream("/dict-prefetch-generator.xml")) {
            xml = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        Configuration configuration = new ConfigurationParser(new ArrayList<>()).parseConfiguration(
                new StringReader(xml.replace("includeColumns=\"name_\"", "includeColumns=\"name_\" " + attributes)));
        return configuration.getContexts().get(0).getTableConfigurations().get(0).getVoCacheGeneratorConfiguration();
    }
}
