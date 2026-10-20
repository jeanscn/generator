package org.mybatis.generator.codegen.mybatis3;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.GeneratedFile;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 验证两阶段 Excel 导入配置及生成的 Controller、Service 契约。
 *
 * @author vgosoft-agent
 */
class ExcelImportWorkflowGenerationTest {

    private static final String EXCEL_IMPORT = "<excelImport generate=\"true\" "
            + "maxInputBytes=\"1048576\" maxExpandedBytes=\"8388608\" maxEntryBytes=\"4194304\" "
            + "maxZipEntries=\"100\" maxRows=\"100\" maxErrorMessages=\"50\"/>";

    /** 启用独立工作流后应生成受限解析、业务策略钩子及事务确认服务。 */
    @Test
    void generatesBoundedPreviewAndTransactionalBusinessHooksWhenEnabled() throws Exception {
        Configuration configuration = parseWithWorkflow(false);
        Map<String, String> generated = generate(configuration);

        String controller = generated.values().stream()
                .filter(source -> source.contains("import/preview") && source.contains("import/confirm"))
                .findFirst().orElseThrow();
        assertThat(controller)
                .contains("previewExcelImport(file)", "confirmExcelImport(request)")
                .doesNotContain("VgoFesodSheet.read(");

        String service = generated.values().stream()
                .filter(source -> source.contains("VgoFesodSheet.readBounded"))
                .findFirst().orElseThrow();
        assertThat(service)
                .contains("new VgoFesodSheet.ImportReadLimits(1048576L, 8388608L, 4194304L, 100, 100, 50)",
                        "createExcelImportPreview(parsed.rows(), parsed.fileSha256())",
                        "doConfirmExcelImport(request)",
                        "@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)",
                        "请在业务服务子类中实现 Excel 导入预览策略",
                        "请在业务服务子类中实现 Excel 导入确认策略");

        String serviceContract = generated.values().stream()
                .filter(source -> source.contains("previewExcelImport(MultipartFile file)")
                        && !source.contains("VgoFesodSheet.readBounded"))
                .findFirst().orElseThrow();
        assertThat(serviceContract)
                .contains("confirmExcelImport(VgoExcelImportConfirmVo request)")
                .doesNotContain("ImportReadLimits limits");
    }

    /** 新工作流与旧 excelVo 同时启用时保留导出能力，但不生成逐行 upsert 导入端点。 */
    @Test
    void keepsLegacyExportButSuppressesDirectImportWhenBothFeaturesAreEnabled() throws Exception {
        Map<String, String> generated = generate(parseWithWorkflow(true));
        String controller = generated.values().stream()
                .filter(source -> source.contains("import/preview") && source.contains("import/confirm"))
                .findFirst().orElseThrow();
        assertThat(controller)
                .contains("exportPost")
                .doesNotContain("VgoFesodSheet.read(");
    }

    /** 启用工作流但遗漏必需资源限额时，配置解析必须失败并指出缺失属性。 */
    @Test
    void rejectsEnabledWorkflowWithoutExplicitResourceLimits() throws Exception {
        String xml = workflowXml(false).replace(" maxRows=\"100\"", "");
        assertThatThrownBy(() -> parse(xml)).hasMessageContaining("maxRows");
    }

    /** 未配置新工作流时，旧生成输出不得出现预览或确认入口。 */
    @Test
    void leavesLegacyExcelImportDisabledWhenWorkflowIsNotConfigured() throws Exception {
        Map<String, String> generated = generate(parse(CasConsumerBaselineTest.consumerXml(null)));
        assertThat(generated.values())
                .noneMatch(source -> source.contains("previewExcelImport")
                        || source.contains("confirmExcelImport")
                        || source.contains("import/preview")
                        || source.contains("import/confirm"));
    }

    private static Configuration parseWithWorkflow(boolean legacyExcelVo) throws Exception {
        return parse(workflowXml(legacyExcelVo));
    }

    private static String workflowXml(boolean legacyExcelVo) throws Exception {
        String xml = CasConsumerBaselineTest.consumerXml(null);
        String legacy = "<excelVo generate=\"false\"/>";
        String replacement = "<excelVo generate=\"" + legacyExcelVo + "\"/>" + EXCEL_IMPORT;
        if (!xml.contains(legacy)) {
            throw new IllegalStateException("Expected legacy Excel VO configuration was not found");
        }
        return xml.replace(legacy, replacement);
    }

    private static Configuration parse(String xml) throws Exception {
        ConfigurationParser parser = new ConfigurationParser(new ArrayList<>());
        Configuration configuration = parser.parseConfiguration(new StringReader(xml));
        parser.customConfig(configuration);
        return configuration;
    }

    private static Map<String, String> generate(Configuration configuration) throws Exception {
        MyBatisGenerator generator = new MyBatisGenerator(
                configuration, new DefaultShellCallback(true), new ArrayList<>());
        generator.generate(null, null, null, false);
        List<GeneratedFile> output = new ArrayList<>(generator.getGeneratedJavaFiles());
        output.addAll(generator.getGeneratedXmlFiles());
        Map<String, String> generated = new TreeMap<>();
        for (GeneratedFile file : output) {
            String name = file.getTargetPackage() + "." + file.getFileName();
            generated.put(name, file.getFormattedContent());
        }
        return generated;
    }
}
