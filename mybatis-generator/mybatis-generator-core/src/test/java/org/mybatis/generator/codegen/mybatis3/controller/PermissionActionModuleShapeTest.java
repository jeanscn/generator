package org.mybatis.generator.codegen.mybatis3.controller;

import com.vgosoft.tool.core.VMD5Util;
import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.codegen.mybatis3.IntrospectedTableMyBatis3Impl;
import org.mybatis.generator.config.HtmlButtonGeneratorConfiguration;
import org.mybatis.generator.internal.util.Mb3GenUtil;
import org.mybatis.generator.offline.ManagedRelationOfflineRegenerator;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 按钮级 data-permission-action-*.sql 的表级模块节点形态：
 * 首级行与接口权限共用同一 module:bean 标识（同 code/name/id），parent_id 直挂菜单；
 * 按钮叶子 code 保持 bean:button 不变，parent_id 指向表级模块节点 id；
 * 生成 Vue 的 permissionKey 与该模块节点 id 一致。
 */
class PermissionActionModuleShapeTest {

    @Test
    void actionRowsShareTableModuleNodeAndKeepLeafButtonCodes() {
        IntrospectedTableMyBatis3Impl table = fixture();
        Set<HtmlButtonGeneratorConfiguration> buttons = new LinkedHashSet<>();
        buttons.add(button("view-row-view", "行查看"));
        buttons.add(button("top-refresh", "刷新"));
        Mb3GenUtil.genHtmlButtonAnnotationDescFromKeys(table,
                List.of("view-row-view", "top-refresh"), buttons, "1777340268517920770");

        String moduleCode = "system:sysmenuimpl";
        String moduleRowId = VMD5Util.MD5_15(moduleCode);
        // 2 个 configurable 按钮共享 1 行表级模块行，共 3 行
        assertThat(table.getPermissionActionDataScriptLines().values()).hasSize(3);
        // 表级模块行：code/name/id 与接口权限 module:bean 行一致，直挂菜单
        assertThat(table.getPermissionActionDataScriptLines().values())
                .anySatisfy(sql -> assertThat(sql)
                        .contains("'" + moduleCode + "'")
                        .contains("'system:菜单管理'")
                        .contains("'1777340268517920770'"));
        // 按钮叶子：code 仍为 bean:button，parent_id 为表级模块节点 id
        assertThat(table.getPermissionActionDataScriptLines().values().stream()
                .filter(sql -> !sql.contains("'" + moduleCode + "'")).toList())
                .hasSize(2)
                .anySatisfy(sql -> assertThat(sql).contains("'sysmenuimpl:view-row-view'"))
                .anySatisfy(sql -> assertThat(sql).contains("'sysmenuimpl:top-refresh'"))
                .allSatisfy(sql -> assertThat(sql).contains("'" + moduleRowId + "'"));
        // 前端 permissionKey 取同一表级模块节点 id
        assertThat(Mb3GenUtil.getPermissionModuleId(table)).isEqualTo(moduleRowId);
    }

    private static IntrospectedTableMyBatis3Impl fixture() {
        IntrospectedTableMyBatis3Impl table =
                ManagedRelationOfflineRegenerator.fixture("system", "SysMenu", "sys_menu", null);
        table.setRemarks("菜单管理");
        // 离线 fixture 不走完整 initialize，反射补齐 controllerBeanName 内部属性
        try {
            java.lang.reflect.Method method =
                    AbstractIntrospectedTable.class.getDeclaredMethod("calculateControllerAttributes");
            method.setAccessible(true);
            method.invoke(table);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        return table;
    }

    private static HtmlButtonGeneratorConfiguration button(String id, String title) {
        HtmlButtonGeneratorConfiguration configuration = new HtmlButtonGeneratorConfiguration(id);
        configuration.setTitle(title);
        configuration.setConfigurable(true);
        return configuration;
    }
}
