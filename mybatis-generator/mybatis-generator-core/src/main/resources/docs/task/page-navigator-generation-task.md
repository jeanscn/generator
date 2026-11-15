# 页面导航器生成任务书

> 主体归属：`mybatis-generator-core`；协作：`mpmm-ui / @vgosoft/framework`。
> 整体状态：已完成；进度：3/3；更新时间：2026-11-16。

## 1. 范围与确认

用户确认在 `generateHtml` 下增加 `pageNavigator`，以逗号分隔的数据库列名生成 `PageNavItem`，并在对应表单字段外层增加匹配锚点。实施范围限当前生成器 core `1.8.0` 与 mpmm-ui Framework；保留并完善用户已添加的 `mybatis-generator-config_1_8_0.dtd` 初稿，不改历史 DTD。

- 仅支持 editable/viewOnly；print 页面配置导航器时明确失败。
- `columns` 必填；每项生成 `id="nav-<实际列名>"`、列注释作为 `title`（无注释时回退列名）、`level=1`。
- `title` 默认空字符串，`trigger` 默认 `auto`，`placement` 默认 `right`；其余标量属性映射到 `VgoPageNav`。`items` 和 `activeId` 不由 XML 独立配置。
- 无效、重复、未知或静态上未渲染的导航列必须明确失败；运行时条件隐藏的字段同步从导航列表移除。
- 不连接数据库、不执行业务模块再生成、不安装或发布工件、不修改业务生成产物。

依据：[生成器指令](..\..\..\..\..\..\.github\copilot-instructions.md)、[生成器权威说明](..\generator-authoritative-spec.md)、[生成器配置指南](..\generator-config-guide.md)、[设计规范 §3.1.1、§6.1、§6.2、§8.1](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\project-design-standard.md)、[全局开发过程 §1.3](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\ai-assisted-development-standard.md#13-生成基础能力与业务重写的实施门禁)、[Framework 组件复用指南 §45](D:\vscode\mpmm-ui\packages\framework\docs\components-reuse-capability-overview.md#45-vgopagenav)。

## 2. 子任务

| 编号 | 内容 | 依赖 | 状态 | 验收条件及结果 |
|---|---|---|---|---|
| G01 | 完成 pageNavigator DTD、配置解析/校验、Vue3 编辑/详情模板输出及生成器回归 | 无 | 完成 | `VueUiConventionAuditTest` 通过；覆盖默认值和全部可配置属性、editable/edit detail 输出、print/重复/空数值/未知列/静态隐藏/布局排除拒绝；使用只读元数据替身，未连接数据库或执行业务生成 |
| F01 | Framework 表单接收导航配置、渲染字段锚点并同步过滤运行时隐藏字段 | G01 | 完成 | `RenderFormItem.test.mjs` 19/19 通过；覆盖锚点映射、静态未渲染字段过滤及字段/布局条件隐藏恢复；Framework Vite 构建通过 |
| V01 | 更新生成器/Framework 文档并完成跨仓定向验证与归档 | G01、F01 | 完成（类型检查受既有诊断阻断） | 已同步配置指南、生成器权威说明、Framework 组件契约及本任务书。`npm --prefix packages/framework run typecheck` 因仓库既有 TypeScript 诊断失败（包含 `LayoutItems.vue` 未标注回调参数）；新增导航辅助逻辑、VgoForm 新属性及 VgoPageNav 变更未见诊断，未扩大修复无关错误。 |

## 3. 临时资产与清理

目前未创建一次性脚本或临时资产。测试输出限定在各自仓库的标准构建目录；不删除现存构建产物或其他任务资产。

## 4. 验证记录

- 生成器：`mvn -q -pl mybatis-generator-core -Dtest=VueUiConventionAuditTest -Dsurefire.failIfNoSpecifiedTests=false test` 通过；其中包含生成模板约定审计与页面导航生成回归。
- Framework：`node --test packages/framework/src/components/vgoForm/items/RenderFormItem.test.mjs` 通过（19/19）；`packages/framework` 的 Vite 生产构建通过。
- 类型检查：Framework 全量 `vue-tsc --noEmit` 仍失败，诊断分布于既有 BPMN、表单及工具文件（包含 `LayoutItems.vue` 未标注回调参数）；新增导航辅助逻辑、VgoForm 新属性及 VgoPageNav 变更未出现诊断。未修复与本需求无关的既有类型错误。
- 构建/生成均使用本地或只读元数据替身；未连接业务数据库、未运行消费者模块生成、未安装或发布工件。
