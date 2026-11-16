# 表单 Markdown 字段（tagType="markdown"）支持任务书

> 主体归属：`mybatis-generator-core`
> 协作仓库：`vgosoft-basic / vgosoft-core-constant`（`HtmlElementTagTypeEnum` 枚举）、`tmri-ui4 / @vgosoft/framework`（表单运行时与 `VgoMarkdownEditor` 组件）
> 需求负责人：用户
> 整体状态：已完成
> 进度：5/5 子任务完成
> 更新时间：2026-11-16
> 下游任务：[vgosoft-bizcore 操作指引文档开发任务书](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-bizcore\src\main\resources\docs\task\manual-doc-development-task.md)（K01 已定方案 A）
> 下游设计：[操作指引文档设计总览 §3.1](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-bizcore\src\main\resources\docs\manual-doc-design-overview.md)
> 临时文件/工具：无
> 清理状态：无临时资产

## 1. 目标与范围

让生成器支持表单字段 `tagType="markdown"`：配置后 Vue3 表单元数据输出 `component='markdown'`，Framework 表单渲染 `VgoMarkdownEditor`（编辑态 vditor，查看态只读预览）。首个消费字段是 `vbiz_manual_doc.content_md`。

范围内：

1. `HtmlElementTagTypeEnum`（vgosoft-core-constant）增加 `markdown` 枚举项。
2. `mybatis-generator-config_1_8_0.dtd` 的 `tagType` 增加 `markdown`；配置解析/校验与 Vue3 生成输出 `component='markdown'`（editable / viewOnly）。
3. Framework：`EFormComponentType` 增加 `markdown`、`RenderFormItem` 增加分支、新增 `VgoMarkdownEditor` 组件。
4. 生成器权威说明、配置指南、Framework 组件契约文档同步。

范围外：manual-doc 自身的建表、生成与页面接线（下游任务书 M02/M07）；vditor 上传图片的具体接口绑定（属消费侧 `extraData`/`format` 配置）；mpmm-ui 回流（登记待确认）。

## 2. 依据

- [操作指引文档设计总览 §3.1](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-bizcore\src\main\resources\docs\manual-doc-design-overview.md)：K01 缺口描述与方案对比，2026-11-16 负责人确认方案 A。
- [生成器指令](..\..\..\..\..\..\.github\copilot-instructions.md)：§2.1 产物保护、§7.1 DTD 版本一致性；新增/修改枚举须按设计规范 §6.1 加中文语义注释。
- [生成器权威说明](..\generator-authoritative-spec.md)、[生成器配置指南](..\generator-config-guide.md) §1.1（core 与 DTD 版本核实）。
- [页面导航器生成任务书](page-navigator-generation-task.md)：同结构跨仓任务先例（DTD 演进、editable/viewOnly/print 分支策略、验证方式）。
- vditor 版本已由负责人确认为 `3.11.3`（2026-08-11 发布，发布满 7 天），后续再评估升级 4.x；`4.0.2-alpha.*` 预发布不使用。

## 3. 评审结论

2026-11-16，需求负责人确认：K01 采用方案 A——补齐通用生成能力（枚举 + DTD + Vue3 输出 + Framework 表单类型），不做 Edit.vue 人工扩展兜底。

## 4. 设计要点

1. `HtmlElementTagTypeEnum` 增加 `markdown`，附中文语义注释（“Markdown 编辑器”）。
2. DTD `tagType` 增加 `markdown`：仅 editable / viewOnly 有效；print 页面配置时明确失败（与 pageNavigator 先例一致）。实施前按配置指南 §1.1 核实当前 core 与 1.8.0 DTD 的版本对应关系，不新建历史 DTD。
3. Vue3 输出：`htmlElementDescriptor tagType="markdown"` 映射为表单 `component='markdown'`，editable 编辑态与 detail 查看态同一组件（查看态由组件内部切只读预览）。具体元数据契约（属性名、透传 props 的机制）以 G02 评审为准。
4. `VgoMarkdownEditor`（Framework 新组件，按 tmri-ui4 `/new-component` 规范）：
   - 编辑态 `new Vditor()`，查看态 `Vditor.preview()`，统一 `cdn: '/vditor'`；`mode`、工具栏、上传参数等通过组件 props 由消费侧注入，组件不内置业务上传地址。
   - 预览开启 sanitize（与下游导出“原始 HTML 不透传”口径一致）。
   - vditor 依赖版本 `3.11.3`，加入 tmri-ui4（具体加在 `packages/framework` 还是应用根 package.json，按 Framework 现有第三方依赖约定执行，F01 时确认）；`vditor/dist` 拷贝到 `public/vditor` 属应用侧工作，在下游 M07 完成。
5. 验证沿用 pageNavigator 先例：生成器走只读元数据替身回归，不连接数据库、不执行业务模块再生成；Framework 走 `node --test` 定向测试 + Vite 构建。

## 5. 当前状态表

| 子任务 | 内容 | 依赖 | 状态 | 关闭条件 | 下一动作 |
|---|---|---|---|---|---|
| G01 | `HtmlElementTagTypeEnum` 增加 `markdown`（中文注释） | 无 | 已完成 | `MARKDOWN(26,"markdown","Markdown编辑器")` 已加入并注释；core-constant 已 install 至本地仓库供生成器依赖链读取 | — |
| G02 | DTD `tagType` 增加 `markdown` + 解析/校验（editable/viewOnly 支持，print 明确失败） | G01 | 已完成 | `mybatis-generator-config_1_8_0.dtd`（与 core 1.8.0-SNAPSHOT 一致）已加入值；Parser 解析 descriptor 时规范化 tagType，print+markdown 抛出 `generateHtml.type="print" 不支持 tagType="markdown"` | — |
| G03 | Vue3 输出 `component='markdown'`（editable + detail），Edit/Detail 模板分支，生成器回归 | G02 | 已完成 | `VueFormGenerateUtil` 默认 placeholder“请输入内容”、component 透传 markdown；`VueUiConventionAuditTest` 新增两条断言（editable 产物含 `component = "markdown"`、print 拒绝），2/2 通过 | — |
| F01 | Framework `EFormComponentType.markdown` + `RenderFormItem` 分支 + `VgoMarkdownEditor`（vditor 3.11.3） | G03 | 已完成 | `RenderFormItem.test.mjs` 24/24 通过；Framework Vite 构建通过（vditor 已入产物）；触碰文件 lint/typecheck 无新增诊断 | — |
| V01 | 文档同步（生成器权威说明、配置指南 tagType 表、Framework 组件契约）+ 跨仓验证记录 | G03、F01 | 已完成 | 配置指南 §7.3.1/§7.3.3/§15.1 与新增 §7.3.6、DTD 内联说明、Framework 组件文档 §50 已同步；doc-index/doc-map 已登记 | — |

## 6. 验收条件

1. `generatorConfig.xml` 中 `htmlElementDescriptor tagType="markdown"` 能通过配置解析并生成 `component='markdown'` 的表单字段。
2. print 页面配置 `markdown` 字段时生成器明确失败；非法 tagType 仍被 DTD 拒绝。
3. Framework 表单按 `component='markdown'` 渲染 `VgoMarkdownEditor`：编辑态可编辑，查看态只读渲染，离线资源路径可配置。
4. 不连接业务数据库、不执行业务模块生成、不修改业务生成产物；生成器回归与 Framework 定向测试通过。
5. vditor 依赖为 `3.11.3`；不引入预发布版本。

## 7. 副作用清单（待按批次申请授权）

| 副作用 | 环境 | 所属子任务 | 授权方式 |
|---|---|---|---|
| tmri-ui4 新增 `vditor@3.11.3` 依赖 | tmri-ui4 | F01 | 版本已确认，安装时执行 |
| core-constant / 生成器 / Framework 本地构建测试 | 本机 | G01–F01 | 常规开发操作 |
| 业务模块再生成（验证 tagType 输出） | 本机 | 不在本任务 | 禁止；由下游 M02 承担 |

## 8. 待确认事项

| 编号 | 事项 | 建议 |
|---|---|---|
| G-Q01 | `component='markdown'` 透传 props 的元数据契约（编辑器 mode、高度、上传配置如何下发） | **已定**：`RenderFormItem` 从表单项 `props` 透传 `height`/`mode`/`cdn`/`upload`/`vditor` 五键，组件内合并为 vditor options；上传 extraData 回填经组件 `updateUploadExtraData` 完成，不在注解元数据中承诺 |
| G-Q02 | vditor 依赖落点：`packages/framework/package.json` 还是应用根 | **已定应用根 `package.json`**：Framework 经 tsconfig 路径别名解析到根 `node_modules`，与现有第三方依赖约定一致；已安装 `3.11.3` |
| G-Q03 | 本能力是否同步回流 `mpmm-ui` 模板 | 与下游 Q04 一并登记，首期只在 tmri-ui4 实施 |

## 9. 完成结果

- 生成器侧：`HtmlElementTagTypeEnum.MARKDOWN(26)`、DTD `%tagType%` 增加 `markdown`、Parser 规范化 + print 拒绝、`VueFormGenerateUtil` 输出 `component='markdown'`。
- Framework 侧：`EFormComponentType.markdown`、`RenderFormItem` 分支（编辑态 VgoMarkdownEditor / 只读 preview / tableCell 纯文本）、新组件 `vgoMarkdownEditor/index.vue`（vditor 3.11.3、本地 cdn、sanitize、上传 format/extraData、updateUploadExtraData）。
- 验证：`VueUiConventionAuditTest` 全类 7 项 6 过 1 败（新增 2 条 markdown 断言均通过）；`RenderFormItem.test.mjs` 24/24 通过；`mvn -pl mybatis-generator-core -am -DskipTests compile` 通过；Framework Vite 构建通过（vditor 入产物）；触碰文件 lint 无警告、typecheck 无新增诊断。
- 遗留：`VueUiConventionAuditTest.pageNavigatorGeneratesMatchingEditAndDetailAnchorsWithComponentDefaults` 存在与本轮无关的基线漂移失败（`width: 220` 期望 vs `150` 实值，`zIndex: 60` vs `120`），已登记生成器仓 `pending-issues.md`；tmri-ui4 全量 typecheck 存在既有错误，未处理。

## 10. 进度记录

- 2026-11-16：建立任务书；K01 已定方案 A、vditor 定 `3.11.3`（下游任务书 §8 同步关闭两项待确认）。
- 2026-11-16：G01–F01 实施完成并验证通过；V01 文档同步完成（配置指南 §7.3.6、DTD 内联说明、Framework 组件文档 §50）；G-Q01/G-Q02 随实施定案；任务关闭。
