package org.mybatis.generator.codegen.mybatis3;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mybatis.generator.api.GeneratedFile;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.PluginConfiguration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.*;

/**
 * 验证按列逻辑删除的真实配置解析、完整生成链及受管输出，不连接外部数据库。
 *
 * @author vgosoft-agent
 */
class LogicalDeleteGenerationTest {
    private static final String METHOD = "deleteByColumnIdAndVersion";

    private static Configuration parse(String value, String columns, String parameterType) throws Exception {
        String xml = CasConsumerBaselineTest.consumerSnapshotTestXml();
        String property = value == null ? "" : "<property name=\"isLogicalDelete\" value=\"" + value + "\"/>";
        String select = "<selectByColumn column=\"" + columns + "\" parameterType=\"" + parameterType
                + "\" enableDelete=\"true\" genControllerMethod=\"true\">" + property + "</selectByColumn>";
        for (String table : List.of("sensitive_category", "sensitive_word")) {
            int start = xml.indexOf("tableName=\"" + table + "\"");
            int tableEnd = xml.indexOf("</table>", start);
            int selectStart = xml.indexOf("<selectByColumn", start);
            int selectEnd = xml.indexOf("</selectByColumn>", selectStart);
            if (start < 0 || tableEnd < 0 || selectStart < 0 || selectEnd < 0 || selectEnd > tableEnd) {
                throw new IllegalStateException("固定消费快照缺少 " + table + " 的 selectByColumn 配置");
            }
            selectEnd += "</selectByColumn>".length();
            xml = xml.substring(0, selectStart) + select + xml.substring(selectEnd);
            tableEnd = xml.indexOf("</table>", start);
            int controllerStart = xml.indexOf("<generateController", start);
            if (controllerStart < 0 || controllerStart > tableEnd) {
                throw new IllegalStateException("固定消费快照缺少 " + table + " 的 generateController 配置");
            }
            int openEnd = xml.indexOf(">", controllerStart) + 1;
            xml = xml.substring(0, openEnd)
                    + "<property name=\"apiPermissions\" value=\"all\"/>"
                    + xml.substring(openEnd);
        }
        var parser = new ConfigurationParser(new ArrayList<>());
        var result = parser.parseConfiguration(new StringReader(xml));
        parser.customConfig(result);
        return result;
    }

    private static Map<String, String> generate(Configuration configuration) throws Exception {
        var generator = new MyBatisGenerator(configuration, new DefaultShellCallback(true), new ArrayList<>());
        generator.generate(null, null, null, false);
        List<GeneratedFile> files = new ArrayList<>(generator.getGeneratedJavaFiles());
        files.addAll(generator.getGeneratedXmlFiles());
        files.addAll(generator.getGeneratedHtmlFiles());
        files.addAll(generator.getGeneratedKotlinFiles());
        var other = MyBatisGenerator.class.getDeclaredField("otherGeneratedFiles");
        other.setAccessible(true);
        files.addAll((List<? extends GeneratedFile>) other.get(generator));
        Map<String, String> outputs = new TreeMap<>();
        for (var file : files) {
            String key = file.getTargetPackage().replace('.', '\\') + "\\" + file.getFileName();
            assertThat(outputs.put(key, CasConsumerBaselineTest.normalize(file.getFormattedContent()))).isNull();
        }
        return outputs;
    }

    private static String file(Map<String, String> output, String filename) {
        return output.entrySet().stream().filter(e -> e.getKey().endsWith("\\" + filename))
                .findFirst().orElseThrow().getValue();
    }

    /** 省略与显式 false 的所有输出逐文件相同，既有物理删除 SQL 和 GET 路由不变。 */
    @Test
    void preservesCompletePhysicalDeleteOutputs() throws Exception {
        var omitted = generate(parse(null, "id_,version_", "single"));
        assertThat(generate(parse("false", "id_,version_", "single"))).isEqualTo(omitted);
        assertThat(file(omitted, "SensitiveCategoryMapper.xml")).contains("<delete id=\"" + METHOD + "\"");
        assertThat(file(omitted, "AbstractSensitiveCategoryController.java"))
                .contains("@GetMapping", "delete-by-column-id-and-version");
    }

    /** 真实当前 DTD 接受 property 子节点，非法布尔值不能被静默当作物理删除。 */
    @ParameterizedTest
    @ValueSource(strings = {"", " ", "TRUE", "False", "yes", "1", " true "})
    void rejectsInvalidLogicalDeleteProperty(String value) {
        assertThatThrownBy(() -> parse(value, "id_,version_", "single"))
                .hasMessageContaining("isLogicalDelete");
    }

    /** 普通版本表输出 POST、显式 Mapper 参数和同条 UPDATE 中的标志与版本递增。 */
    @Test
    void generatesVersionedLogicalDeleteAcrossAllLayers() throws Exception {
        var output = generate(parse("true", "id_,version_", "single"));
        assertThat(file(output, "SensitiveCategoryMapper.xml"))
                .contains("<update id=\"" + METHOD + "\"", "set delete_flag = 1, version_ = version_ + 1",
                        "delete_flag = 0", "version_ &gt; 0 and version_ &lt; 2147483647");
        assertThat(file(output, "GenSensitiveCategoryMapper.java"))
                .contains(METHOD + "(@Param(\"id\") String id, @Param(\"version\") Integer version)");
        assertThat(file(output, "AbstractSensitiveCategoryImpl.java"))
                .contains("Propagation.REQUIRED", "LOGICAL_DELETE_CONFLICT", "affected != 1");
        String controller = file(output, "AbstractSensitiveCategoryController.java");
        assertThat(controller).contains("@PostMapping(value = {\"delete-by-column-id-and-version\"})",
                "@PreAuthorize", "hasPermission('sensitive:sensitivecategoryimpl:delete')");
        assertThat(controller).contains("@RequestParam(value = \"version\") Integer version");
    }

    /** 缺版本、漏主键、未知列及版本化独立多列表都须在写盘前失败，不能降级成宽条件删除。 */
    @ParameterizedTest
    @ValueSource(strings = {"id_", "version_", "id_,missing_", "", "id_,id_,version_"})
    void rejectsIncompleteVersionedConditions(String columns) {
        assertThatThrownBy(() -> generate(parse("true", columns, "single")))
                .hasMessageContaining("isLogicalDelete");
    }

    /** 多列列表是各列 IN 的交集，不是逐项 id/version 配对，因此版本表不允许这种批量删除。 */
    @Test
    void rejectsIndependentVersionLists() {
        assertThatThrownBy(() -> generate(parse("true", "id_,version_", "list")))
                .hasMessageContaining("不支持独立多值列表");
    }

    /** 无版本表允许按单值或各列 IN 逻辑删除，不凭空引入版本列，也不改变按列交集语义。 */
    @ParameterizedTest
    @ValueSource(strings = {"single", "list"})
    void supportsNonVersionedTables(String parameterType) throws Exception {
        var config = parse("true", "id_", parameterType);
        var plugin = new PluginConfiguration();
        plugin.setConfigurationType(LogicalDeletePlugin.class.getName());
        plugin.addProperty("mode", "no-version");
        config.getContexts().get(0).addPluginConfiguration(plugin);
        String mapper = file(generate(config), "SensitiveCategoryMapper.xml");
        String id = parameterType.equals("single") ? "deleteByColumnId" : "deleteByColumnIds";
        var statement = java.util.regex.Pattern.compile("(?s)<update id=\"" + id + "\">(.*?)</update>")
                .matcher(mapper);
        assertThat(statement.find()).isTrue();
        assertThat(statement.group(1)).contains("set delete_flag = 1", "delete_flag = 0").doesNotContain("version_");
        if (parameterType.equals("list")) { assertThat(statement.group(1)).contains("<foreach", "collection=\"ids\""); }
        Path root = Path.of("target", "logical-delete-generated");
        Files.createDirectories(root);
        Files.writeString(root.resolve("unversioned-" + parameterType + ".xml"),
                "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" "
                        + "\"https://mybatis.org/dtd/mybatis-3-mapper.dtd\"><mapper namespace=\"sample.Logical\">"
                        + statement.group() + "</mapper>");
    }

    /** 逻辑删除不能脱离 enableDelete 单独配置，防止用户误以为产生了受控删除入口。 */
    @Test
    void requiresDeleteGenerationToBeEnabled() throws Exception {
        var config = parse("true", "id_,version_", "single");
        config.getContexts().get(0).getTableConfigurations().get(0)
                .getSelectByColumnGeneratorConfigurations().get(0).setEnableDelete(false);
        assertThatThrownBy(() -> generate(config)).hasMessageContaining("enableDelete=true");
    }

    /**
     * 在实际插件回调中破坏逻辑删除必需产物。
     *
     * @author vgosoft-agent
     */
    public static class LogicalDeletePlugin extends PluginAdapter {
        @Override
        public boolean validate(List<String> warnings) { return true; }
        @Override
        public void initialized(AbstractIntrospectedTable table) {
            if ("column".equals(properties.getProperty("mode"))) {
                table.getColumn("delete_flag").ifPresent(c -> c.setNullable(true));
            } else if ("no-version".equals(properties.getProperty("mode"))) {
                table.getColumn("version_").ifPresent(c -> table.getBaseColumns().remove(c));
            } else if ("missing-flag".equals(properties.getProperty("mode"))) {
                table.getColumn("delete_flag").ifPresent(c -> table.getBaseColumns().remove(c));
            } else if ("generated-flag".equals(properties.getProperty("mode"))) {
                table.getColumn("delete_flag").ifPresent(c -> c.setGeneratedColumn(true));
            } else if ("version-type".equals(properties.getProperty("mode"))) {
                table.getColumn("version_").ifPresent(c -> c.setJdbcTypeName("VARCHAR"));
            }
        }
        @Override
        public boolean sqlMapDocumentGenerated(Document document, AbstractIntrospectedTable table) {
            if ("sql".equals(properties.getProperty("mode"))) {
                document.getRootElement().getElements().stream().filter(e -> e instanceof XmlElement)
                        .map(e -> (XmlElement) e).filter(e -> e.getAttributes().stream()
                                .anyMatch(a -> a.getName().equals("id") && a.getValue().equals(METHOD)))
                        .findFirst().ifPresent(e -> e.getElements().clear());
            }
            return true;
        }
        @Override
        public boolean serviceImplGenerated(TopLevelClass unit, AbstractIntrospectedTable table) {
            if ("service".equals(properties.getProperty("mode"))) {
                unit.getMethods().stream().filter(m -> m.getName().equals(METHOD)).forEach(m -> m.getBodyLines().clear());
            }
            return true;
        }
    }
}
