package org.mybatis.generator.codegen.mybatis3;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.GeneratedFile;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * save-batch（子表变更集统一保存）生成物的定向验证。
 * <p>
 * 复用 sc03 消费配置快照与元数据替身，不建立数据库连接；
 * 覆盖端点、服务接口、服务实现、disabledMethod 委托停用门及 apiPermissions 白名单。
 * </p>
 *
 * @author vgosoft-agent
 */
class SaveBatchGenerationTest {

    /**
     * 三个批量写原语齐备且未停用时，Controller 端点、Service 接口与实现方法一并生成。
     */
    @Test
    void generatesSaveBatchArtifacts() throws Exception {
        List<GeneratedFile> files = generate(CasConsumerBaselineTest.consumerSnapshotTestXml());

        String controller = content(files, "AbstractSensitiveCategoryController.java");
        assertThat(controller)
                .as("save-batch端点与变更集参数")
                .contains("save-batch")
                .contains("VgoBatchChangeVo<SensitiveCategoryCreateVo, SensitiveCategoryUpdateVo>")
                .contains("saveBatchSensitiveCategory(")
                .contains("changes.getInserts()")
                .contains("changes.getUpdates()")
                .contains("changes.getDeleteIds()")
                .contains("mappings.fromSensitiveCategoryCreateVos")
                .contains("mappings.fromSensitiveCategoryUpdateVos");

        String service = content(files, "IGenSensitiveCategory.java");
        assertThat(service)
                .as("Service接口saveBatch签名")
                .contains("saveBatch(List<SensitiveCategory> inserts, List<SensitiveCategory> updates, List<String> deleteIds)");

        String impl = content(files, "AbstractSensitiveCategoryImpl.java");
        assertThat(impl)
                .as("Service实现saveBatch方法")
                .contains("saveBatch(List<SensitiveCategory> inserts, List<SensitiveCategory> updates, List<String> deleteIds)")
                .contains("@Transactional")
                .contains("this.deleteByExample(example)")
                .contains("this.updateBatchByPrimaryKey(updates)")
                .contains("this.insertBatch(inserts)")
                .contains("setRollbackOnly()")
                .contains("ServiceResult.success(savedEntities, affectedRows)")
                .doesNotContain("'{'");
    }

    /**
     * 任一委托原语被 disabledMethod 停用时不得生成 saveBatch，
     * 否则委托调用会命中拒绝入口。
     */
    @Test
    void skipsSaveBatchWhenDelegationDisabled() throws Exception {
        System.setProperty("sc03.disabled", "true");
        String xml;
        try {
            xml = CasConsumerBaselineTest.consumerSnapshotTestXml();
        } finally {
            System.clearProperty("sc03.disabled");
        }
        List<GeneratedFile> files = generate(xml);

        String categoryImpl = content(files, "AbstractSensitiveCategoryImpl.java");
        assertThat(categoryImpl).doesNotContain("saveBatch(");
        String categoryService = content(files, "IGenSensitiveCategory.java");
        assertThat(categoryService).doesNotContain("saveBatch(");
        String categoryController = content(files, "AbstractSensitiveCategoryController.java");
        assertThat(categoryController).doesNotContain("save-batch");
    }

    /**
     * apiPermissions 白名单命中 saveBatch 时生成权限注解与权限数据；未配置白名单时不生成。
     */
    @Test
    void emitsSaveBatchPermissionOnlyWhenWhitelisted() throws Exception {
        // 基线（未配置 apiPermissions）：端点生成但无 @PreAuthorize 与接口级权限数据
        // （按钮级 data-permission-action-*.sql 不受白名单影响，不属于断言范围）
        List<GeneratedFile> baseline = generate(CasConsumerBaselineTest.consumerSnapshotTestXml());
        String baselineController = content(baseline, "AbstractSensitiveCategoryController.java");
        assertThat(baselineController)
                .contains("save-batch")
                .doesNotContain("savebatch'");
        assertThat(fileNames(baseline)).noneMatch(SaveBatchGenerationTest::isApiPermissionFile);

        // 白名单注入 saveBatch：生成权限注解及 data-permission 数据
        String xml = CasConsumerBaselineTest.consumerSnapshotTestXml()
                .replaceAll("(<generateController\\b[^>]*[^/]>)",
                        "$1<property name=\"apiPermissions\" value=\"saveBatch\"/>");
        List<GeneratedFile> files = generate(xml);
        String controller = content(files, "AbstractSensitiveCategoryController.java");
        assertThat(controller)
                .contains("@PreAuthorize")
                .contains("savebatch')");
        assertThat(fileNames(files)).anyMatch(SaveBatchGenerationTest::isApiPermissionFile);
        String permissionSql = files.stream()
                .filter(f -> isApiPermissionFile(f.getFileName()))
                .map(SaveBatchGenerationTest::formatted)
                .reduce(String::concat).orElse("");
        assertThat(permissionSql).contains("savebatch");
    }

    private List<GeneratedFile> generate(String xml) throws Exception {
        List<String> warnings = new ArrayList<>();
        var parser = new ConfigurationParser(warnings);
        var config = parser.parseConfiguration(new StringReader(xml));
        parser.customConfig(config);
        var generator = new MyBatisGenerator(config, new DefaultShellCallback(true), warnings);
        generator.generate(null, null, null, false);
        List<GeneratedFile> output = new ArrayList<>();
        output.addAll(generator.getGeneratedJavaFiles());
        output.addAll(generator.getGeneratedXmlFiles());
        output.addAll(generator.getGeneratedKotlinFiles());
        output.addAll(generator.getGeneratedHtmlFiles());
        var other = MyBatisGenerator.class.getDeclaredField("otherGeneratedFiles");
        other.setAccessible(true);
        output.addAll((List<? extends GeneratedFile>) other.get(generator));
        return output;
    }

    private String content(List<GeneratedFile> files, String fileName) {
        return files.stream().filter(f -> f.getFileName().equals(fileName))
                .map(SaveBatchGenerationTest::formatted).findFirst()
                .orElseThrow(() -> new AssertionError("缺少生成文件：" + fileName));
    }

    private List<String> fileNames(List<GeneratedFile> files) {
        return files.stream().map(GeneratedFile::getFileName).toList();
    }

    /**
     * 接口级权限数据文件为 data-permission-<表名>.sql，
     * 按钮级 data-permission-action-*.sql 与 apiPermissions 白名单无关，需排除。
     */
    private static boolean isApiPermissionFile(String fileName) {
        return fileName.startsWith("data-permission-") && !fileName.startsWith("data-permission-action-");
    }

    private static String formatted(GeneratedFile file) {
        try {
            return file.getFormattedContent();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
