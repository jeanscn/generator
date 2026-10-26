package org.mybatis.generator.plugins.vue3;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.codegen.mybatis3.IntrospectedTableMyBatis3Impl;
import org.mybatis.generator.codegen.mybatis3.freemaker.vue3.GeneratedVueFile;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.TypeScriptTypeDescriptor;
import org.mybatis.generator.offline.Vue3TypeImportRegenerator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 验证同一模块内实体泛型依赖的导入和真实类型模板输出，不连接数据库。
 *
 * @author vgosoft
 */
class Vue3TypeImportsTest {
    @Test
    void importsReferencedEntityButNotSelfFrameworkBaseOrUnusedEntities() {
        Context context = context();
        List<TypeScriptTypeDescriptor> descriptors = List.of(
                descriptor("OrgDepartment", "AbstractDepartmentPersistence<OrgDepartment, OrgUser>"),
                descriptor("OrgDepartmentVo", "AbstractOrgDepartmentVo"));

        assertThat(Vue3TypeImports.resolve(context, descriptors)).containsExactly(
                Map.of("name", "OrgUser", "path", "../../orguser/types/TOrgUser"));
    }

    @Test
    void deduplicatesNestedGenericReferencesAndMatchesWholeNames() {
        Context context = context();
        List<TypeScriptTypeDescriptor> descriptors = List.of(
                descriptor("OrgGroup", "AbstractGroupPersistence<OrgGroup, Array<OrgUser>>"),
                descriptor("OrgGroupVo", "Base<OrgUser, OrgUserExtra>"));

        assertThat(Vue3TypeImports.resolve(context, descriptors)).hasSize(1);
        assertThat(Vue3TypeImports.resolve(context,
                List.of(descriptor("OrgRole", "Base<OrgUserExtra>")))).isEmpty();
    }

    @Test
    void rendersTypeOnlyImportsThroughProductionTypeTemplate() {
        Context context = context();
        TypeScriptTypeDescriptor descriptor =
                descriptor("OrgDepartment", "AbstractDepartmentPersistence<OrgDepartment, OrgUser>");
        IntrospectedTableMyBatis3Impl table = new IntrospectedTableMyBatis3Impl();
        table.setContext(context);
        String output = new GeneratedVueFile("TOrgDepartment.ts", ".", "", table,
                "vue_module_type.ts.ftl", Map.of(
                        "OVERRIDABLE_FLAG", "@VCF.overridable",
                        "tableRemark", "部门管理",
                        "typeName", "TOrgDepartment",
                        "fields", List.of(),
                        "tsTypeDescriptors", List.of(descriptor),
                        "typeImports", Vue3TypeImports.resolve(context, List.of(descriptor))))
                .getFormattedContent();

        assertThat(output).contains(
                "import type { OrgUser } from '../../orguser/types/TOrgUser';",
                "extends AbstractDepartmentPersistence<OrgDepartment, OrgUser>");
        assertThat(output).doesNotContain("import type { OrgDepartment }", "import type { Abstract");
    }

    @Test
    void keepsLegacyTemplateInputsWithoutDependenciesCompatible() {
        IntrospectedTableMyBatis3Impl table = new IntrospectedTableMyBatis3Impl();
        String output = new GeneratedVueFile("TExample.ts", ".", "", table,
                "vue_module_type.ts.ftl", Map.of(
                        "OVERRIDABLE_FLAG", "@VCF.overridable", "tableRemark", "示例",
                        "typeName", "TExample", "fields", List.of(), "tsTypeDescriptors", List.of()))
                .getFormattedContent();
        assertThat(output).contains("export type TExample").doesNotContain("import type");
    }

    @Test
    void regeneratesOnlyImportsIdempotentlyAndRejectsConfigDriftOrProtectedExtensions() throws Exception {
        Path root = Path.of("target", "vue3-type-import-test");
        Files.createDirectories(root);
        try {
            StringBuilder configuration = new StringBuilder(
                    "<generatorConfiguration><context moduleKeyword=\"organization\">"
                            + "<table tableName=\"org_user\" domainObjectName=\"OrgUser\"/>");
            for (String domain : List.of("OrgDepartment", "OrgGroup", "OrgRole", "OrgTeam")) {
                configuration.append("<table domainObjectName=\"").append(domain)
                        .append("\"><property name=\"rootClass\" value=\"example.Base\"/>")
                        .append("<property name=\"rootClassTypeArguments\" value=\"example.")
                        .append(domain).append(",example.OrgUser\"/></table>");
                Path file = typeFile(root, domain);
                Files.createDirectories(file.getParent());
                Files.writeString(file, "/** @VCF.overridable */\r\nexport interface " + domain
                        + " extends Base<" + domain + ", OrgUser> {\r\n    name?: string;\r\n}\r\n");
            }
            configuration.append("</context></generatorConfiguration>");
            Path config = root.resolve("generatorConfig.xml");
            Files.writeString(config, configuration);
            Map<Path, String> outputs = Vue3TypeImportRegenerator.regenerate(config, root);
            assertThat(outputs).hasSize(4);
            for (Map.Entry<Path, String> output : outputs.entrySet()) {
                String original = Files.readString(output.getKey());
                assertThat(output.getValue().replace(
                        "import type { OrgUser } from '../../orguser/types/TOrgUser';\r\n", ""))
                        .isEqualTo(original);
                Files.writeString(output.getKey(), output.getValue());
            }
            assertThat(Vue3TypeImportRegenerator.regenerate(config, root)).isEqualTo(outputs);
            Path department = typeFile(root, "OrgDepartment");
            Files.writeString(department, Files.readString(department).replace("Base<", "DifferentBase<"));
            assertThatThrownBy(() -> Vue3TypeImportRegenerator.regenerate(config, root))
                    .hasMessageContaining("继承声明不一致");
            Files.writeString(department, outputs.get(department).replace(
                    "@VCF.overridable", "@VCF.###.overridable"));
            assertThatThrownBy(() -> Vue3TypeImportRegenerator.regenerate(config, root))
                    .hasMessageContaining("不允许覆盖");
        } finally {
            try (var files = Files.walk(root)) {
                for (Path path : files.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(path);
                }
            }
        }
    }

    private static Path typeFile(Path root, String domain) {
        return root.resolve("src\\modules\\organization").resolve(domain.toLowerCase(Locale.ROOT))
                .resolve("types\\T" + domain + ".ts");
    }

    private static Context context() {
        Context context = new Context(ModelType.FLAT);
        for (String name : List.of("OrgDepartment", "OrgGroup", "OrgRole", "OrgUser", "Unused")) {
            TableConfiguration table = new TableConfiguration(context, new java.util.ArrayList<>());
            table.setTableName(name.toLowerCase(java.util.Locale.ROOT));
            table.setDomainObjectName(name);
            context.addTableConfiguration(table);
        }
        return context;
    }

    private static TypeScriptTypeDescriptor descriptor(String name, String superclass) {
        TypeScriptTypeDescriptor descriptor = new TypeScriptTypeDescriptor(name, "interface", "测试");
        descriptor.setExtendsName(superclass);
        return descriptor;
    }
}
