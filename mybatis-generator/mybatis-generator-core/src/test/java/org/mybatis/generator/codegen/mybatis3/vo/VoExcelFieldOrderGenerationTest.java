package org.mybatis.generator.codegen.mybatis3.vo;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.codegen.mybatis3.IntrospectedTableMyBatis3Impl;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.ObjectFactory;
import org.mybatis.generator.internal.PluginAggregator;
import org.mybatis.generator.internal.rules.FlatModelRules;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 验证 Excel VO 按显式导出字段顺序生成。
 *
 * @author vgosoft-agent
 */
class VoExcelFieldOrderGenerationTest {

    /** 显式导出顺序应覆盖内省列顺序及 includeFields 的筛选顺序。 */
    @Test
    void generatesExcelVoFieldsInIncludeOrder() throws Exception {
        var table = fixture("<excelVo generate=\"true\" includeFields=\"name,id\"/>", "id_:id", "name_:name");
        var generated = new VoExcelGenerator(table, "src", new ProgressCallback() { },
                new ArrayList<>(), new Interface("sample.pojo.maps.ArticleMappings")).generate();

        assertThat(generated.getFields().stream()
                .map(field -> field.getName())
                .filter(name -> !name.equals("serialVersionUID")))
                .containsExactly("name", "id");
    }

    /** 未配置 includeFields 时，既有的内省字段顺序保持不变。 */
    @Test
    void preservesIntrospectedOrderWithoutIncludeFields() throws Exception {
        var table = fixture("<excelVo generate=\"true\"/>", "id_:id", "name_:name");
        var generated = new VoExcelGenerator(table, "src", new ProgressCallback() { },
                new ArrayList<>(), new Interface("sample.pojo.maps.ArticleMappings")).generate();

        assertThat(generated.getFields().stream()
                .map(field -> field.getName())
                .filter(name -> !name.equals("serialVersionUID")))
                .containsExactly("id", "name");
    }

    /** 缺项、重复字段及非导出字段均不能形成有效的导出字段顺序。 */
    @Test
    void rejectsDuplicateOrUnknownIncludedFields() {
        for (String includedFields : List.of("id,id", "id,name,missing")) {
            assertThatThrownBy(() -> {
                var table = fixture("<excelVo generate=\"true\" includeFields=\"" + includedFields + "\"/>",
                        "id_:id", "name_:name");
                new VoExcelGenerator(table, "src", new ProgressCallback() { },
                        new ArrayList<>(), new Interface("sample.pojo.maps.ArticleMappings")).generate();
            }).isInstanceOf(IllegalStateException.class).hasMessageContaining("includeFields");
        }
    }

    /** 使用固定离线配置和内省列构造 Excel VO，不连接数据库或写入生成产物。 */
    private IntrospectedTableMyBatis3Impl fixture(String excelVoElement, String... columnSpecs) throws Exception {
        String xml;
        try (var stream = getClass().getResourceAsStream("/dict-prefetch-generator.xml")) {
            assertThat(stream).as("离线生成配置资源").isNotNull();
            xml = new String(stream.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("<generateVo generate=\"false\"/>", """
                            <generateVo generate="true">
                              %s
                            </generateVo>
                            """.formatted(excelVoElement));
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
            column.setFullyQualifiedJavaType(org.mybatis.generator.api.dom.java.FullyQualifiedJavaType.getStringInstance());
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
}
