package org.mybatis.generator.codegen.mybatis3.vo;

import com.vgosoft.core.constant.enums.db.DbFiledDefaultValueEnum;
import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.DefaultJavaFormatter;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.IntrospectedTableMyBatis3Impl;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.custom.annotations.ColumnMetaDesc;
import org.mybatis.generator.internal.ObjectFactory;
import org.mybatis.generator.internal.PluginAggregator;
import org.mybatis.generator.internal.rules.FlatModelRules;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.Types;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 审计字段初始化与 getter 回退清理的生成验证，对应任务书 T08。
 *
 * <p>验证 {@code AbstractJavaGenerator.addInitialization} 对 A/A' 类审计字段与命中
 * {@link DbFiledDefaultValueEnum} 的约定默认字段跳过初始化、删除 getter 回退，以及
 * {@link ColumnMetaDesc} 为约定默认字段输出 {@code defaultValue}。</p>
 *
 * <p>使用离线内省替身，不连接数据库、不写业务工程产物。</p>
 *
 * @author vgosoft-agent
 */
class AuditFieldInitGenerationTest {

    /** A 类字段（跳过初始化）与约定默认字段（跳过初始化 + 输出 defaultValue），deleteFlag 例外保留。 */
    @Test
    void createVoInitializationSkipsAuditAndDefaultValueFields() throws Exception {
        var table = fixture();
        Interface mappings = new Interface("sample.pojo.maps.ArticleMappings");
        mappings.setVisibility(org.mybatis.generator.api.dom.java.JavaVisibility.PUBLIC);
        var callback = new ProgressCallback() { };
        var warnings = new ArrayList<String>();
        TopLevelClass createVo = new VoCreateGenerator(table, "src", callback, warnings, mappings).generate();
        String rendered = format(createVo);

        // 初始化块不再出现 A 类审计字段（created/modified/version/createdId/modifiedId/tenantId）
        assertThat(rendered).doesNotContain("this.created =", "this.modified =", "this.version =",
                "this.createdId =", "this.modifiedId =", "this.tenantId =");
        // 不再生成 getter 回退（无 VCurrentUserUtil 导入、无 getCreatedId/getModifiedId 重写）
        assertThat(rendered).doesNotContain("VCurrentUserUtil", "getCreatedId", "getModifiedId");
        // 命中约定默认值枚举的字段（createdId/applyUserId）不再字面量初始化
        assertThat(rendered).doesNotContain("this.createdId = \"'currentUserId'\"",
                "this.applyUserId = \"'currentUserId'\"");
    }

    /** deleteFlag 的 = 0 初始化语义保留（方案 §5.2/§6.2），普通字段的字面量初始化保留。 */
    @Test
    void deleteFlagAndPlainFieldsKeepInitialization() throws Exception {
        var table = fixture();
        Interface mappings = new Interface("sample.pojo.maps.ArticleMappings");
        mappings.setVisibility(org.mybatis.generator.api.dom.java.JavaVisibility.PUBLIC);
        var callback = new ProgressCallback() { };
        var warnings = new ArrayList<String>();
        TopLevelClass createVo = new VoCreateGenerator(table, "src", callback, warnings, mappings).generate();
        String rendered = format(createVo);

        // deleteFlag 保留初始化（= 0 语义，类型由列类型决定）；普通字面量字段保留初始化
        assertThat(rendered).contains("this.deleteFlag =");
        assertThat(rendered).contains("this.applyDate = \"2026-01-01\";");
    }

    /** ColumnMetaDesc 只对命中 DbFiledDefaultValueEnum 的列输出规范化 defaultValue（去单引号）。 */
    @Test
    void columnMetaDescEmitsNormalizedDefaultValue() {
        ColumnMetaDesc withDefault = new ColumnMetaDesc(column("created_id", "createdId", "'currentUserId'"));
        assertThat(withDefault.getDefaultValue()).isEqualTo("currentUserId");
        assertThat(withDefault.toAnnotation()).contains("defaultValue = \"currentUserId\"");

        ColumnMetaDesc plain = new ColumnMetaDesc(column("name_", "name", ""));
        assertThat(plain.getDefaultValue()).isEqualTo("");
        assertThat(plain.toAnnotation()).doesNotContain("defaultValue = ");
    }

    private static String format(CompilationUnit unit) {
        return new DefaultJavaFormatter().getFormattedContent(unit);
    }

    private static IntrospectedColumn column(String columnName, String property, String defaultValue) {
        IntrospectedColumn column = new IntrospectedColumn();
        column.setActualColumnName(columnName);
        column.setJavaProperty(property);
        column.setFullyQualifiedJavaType(FullyQualifiedJavaType.getStringInstance());
        column.setJdbcType(Types.VARCHAR);
        column.setJdbcTypeName("VARCHAR");
        column.setRemarks("测试列");
        if (defaultValue != null && !defaultValue.isEmpty()) {
            column.setDefaultValue(defaultValue);
        }
        return column;
    }

    /** 构造离线内省替身，覆盖 A 类、A' 主键、B 约定默认与普通字段，rootClass 沿用配置中的 AbstractEntity。 */
    private IntrospectedTableMyBatis3Impl fixture() throws Exception {
        String xml;
        try (var stream = getClass().getResourceAsStream("/dict-prefetch-generator.xml")) {
            assertThat(stream).as("离线生成配置资源").isNotNull();
            xml = new String(stream.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("<generateVo generate=\"false\"/>", """
                            <generateVo generate="true">
                              <createVo/>
                              <updateVo/>
                              <excelVo/>
                            </generateVo>
                            """);
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
        // A 类审计字段：created/modified 用 CURRENT_TIMESTAMP，version 用字面量 1
        table.addColumn(column("created_", "created", "CURRENT_TIMESTAMP"));
        table.addColumn(column("modified_", "modified", "CURRENT_TIMESTAMP"));
        table.addColumn(column("version_", "version", "1"));
        // A 类身份字段：createdId/modifiedId 命中约定默认值枚举
        table.addColumn(column("created_id", "createdId", "'currentUserId'"));
        table.addColumn(column("modified_id", "modifiedId", "'currentUserId'"));
        table.addColumn(column("tenant_id", "tenantId", ""));
        table.addColumn(column("delete_flag", "deleteFlag", "0"));
        // B 约定默认字段：applyUserId 命中约定默认值枚举；applyDate 普通字面量日期
        table.addColumn(column("apply_user_id", "applyUserId", "'currentUserId'"));
        table.addColumn(column("apply_date", "applyDate", "2026-01-01"));
        // 普通字段
        table.addColumn(column("name_", "name", ""));
        table.addPrimaryKeyColumn("id_");
        table.initialize();
        table.setRules(new FlatModelRules(table));
        return table;
    }
}
