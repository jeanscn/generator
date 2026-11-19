package org.mybatis.generator.codegen.mybatis3.controller;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.IntrospectedTableMyBatis3Impl;
import org.mybatis.generator.config.EnableCacheConfiguration;
import org.mybatis.generator.config.JavaControllerGeneratorConfiguration;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.config.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.codegen.mybatis3.controller.elements.DeleteByTableGenerator;
import org.mybatis.generator.codegen.mybatis3.controller.elements.InsertByTableGenerator;
import org.mybatis.generator.internal.rules.ConditionalModelRules;
import org.mybatis.generator.offline.ManagedRelationOfflineRegenerator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 接口权限白名单（generateController/apiPermissions）的离线验证：
 * 动态关系端点的保留值放行、按动态权限 code 精确放行，以及 Spring Security 总开关的抑制。
 * 固定前缀白名单、all、未配置与大小写行为由 {@code ExportElementGeneratorTest} 覆盖。
 */
class ApiPermissionWhitelistGenerationTest {

    @Test
    void relationTokenCoversAllDynamicRelationEndpoints() {
        IntrospectedTableMyBatis3Impl table = fixture("relation");
        TopLevelClass controller = controller(table);
        String unionCode = "system:sysmenuimpl:" + relation(table).getUnionMethodName().toLowerCase();
        String splitCode = "system:sysmenuimpl:" + relation(table).getSplitMethodName().toLowerCase();
        assertThat(controller.getMethods()).hasSize(2);
        assertThat(controller.getMethods().stream()
                .flatMap(method -> method.getAnnotations().stream()))
                .contains("@PreAuthorize(\"@uss.hasPermission('" + unionCode + "')\")",
                        "@PreAuthorize(\"@uss.hasPermission('" + splitCode + "')\")");
        assertThat(table.getPermissionDataScriptLines().values())
                .anyMatch(sql -> sql.contains(unionCode) && sql.contains("添加角色关联"))
                .anyMatch(sql -> sql.contains(splitCode) && sql.contains("删除角色关联"));
    }

    @Test
    void dynamicPermissionCodeMatchesSingleRelationEndpoint() {
        SelectByTableGeneratorConfiguration relation = relation();
        IntrospectedTableMyBatis3Impl table = fixture(relation.getUnionMethodName().toLowerCase());
        TopLevelClass controller = controller(table);
        assertThat(controller.getMethods()).hasSize(2);
        Method union = controller.getMethods().stream()
                .filter(method -> method.getName().equals(relation.getUnionMethodName())).findFirst().orElseThrow();
        Method split = controller.getMethods().stream()
                .filter(method -> method.getName().equals(relation.getSplitMethodName())).findFirst().orElseThrow();
        assertThat(union.getAnnotations().toString()).contains("@PreAuthorize");
        assertThat(split.getAnnotations().toString()).doesNotContain("@PreAuthorize");
        assertThat(table.getPermissionDataScriptLines().values())
                .allMatch(sql -> !sql.contains(relation.getSplitMethodName().toLowerCase()));
    }

    @Test
    void springSecuritySwitchSuppressesWhitelist() {
        IntrospectedTableMyBatis3Impl table = fixture("all");
        table.getContext().setIntegrateSpringSecurity(false);
        table.setRules(new ConditionalModelRules(table));
        TopLevelClass controller = controller(table);
        assertThat(controller.getMethods()).hasSize(2);
        for (Method method : controller.getMethods()) {
            assertThat(method.getAnnotations().toString()).doesNotContain("@PreAuthorize");
        }
        assertThat(table.getPermissionDataScriptLines()).isEmpty();
    }

    private static IntrospectedTableMyBatis3Impl fixture(String apiPermissions) {
        IntrospectedTableMyBatis3Impl table =
                ManagedRelationOfflineRegenerator.fixture("system", "SysMenu", "sys_menu", relation());
        table.setRemarks("菜单管理");
        JavaControllerGeneratorConfiguration controllerConfiguration =
                new JavaControllerGeneratorConfiguration(table.getContext(), table.getTableConfiguration());
        controllerConfiguration.addProperty(PropertyRegistry.CONTROLLER_API_PERMISSIONS, apiPermissions);
        table.getTableConfiguration().setJavaControllerGeneratorConfiguration(controllerConfiguration);
        table.setRules(new ConditionalModelRules(table));
        return table;
    }

    private static TopLevelClass controller(IntrospectedTableMyBatis3Impl table) {
        TopLevelClass controller = new TopLevelClass("system.controller.AbstractSysMenuController");
        for (AbstractControllerElementGenerator generator : List.of(new InsertByTableGenerator(), new DeleteByTableGenerator())) {
            ManagedRelationOfflineRegenerator.configure(generator, table);
            generator.setCommentGenerator(table.getContext().getCommentGenerator());
            generator.setServiceBeanName("sysMenuImpl");
            generator.addElements(controller);
        }
        return controller;
    }

    private static SelectByTableGeneratorConfiguration relation() {
        SelectByTableGeneratorConfiguration relation = new SelectByTableGeneratorConfiguration();
        relation.setRelationIdKey("org_role_menu");
        relation.setTableName("org_role_menu");
        relation.setMethodSuffix("ForRole");
        relation.setPrimaryKeyColumn("menu_id");
        relation.setOtherPrimaryKeyColumn("role_id");
        relation.setThisColumn(ManagedRelationOfflineRegenerator.column("menu_id", "menuId", "当前ID", false));
        IntrospectedColumn role = ManagedRelationOfflineRegenerator.column("role_id", "roleId", "角色", false);
        relation.setOtherColumn(role);
        relation.setParameterName(role.getJavaProperty());
        EnableCacheConfiguration cache = new EnableCacheConfiguration();
        cache.setCacheNames(List.of("orgRoleMenuRelations#72h"));
        relation.getCacheConfigurationList().add(cache);
        return relation;
    }

    private static SelectByTableGeneratorConfiguration relation(IntrospectedTableMyBatis3Impl table) {
        return table.getTableConfiguration().getSelectByTableGeneratorConfiguration().get(0);
    }
}
