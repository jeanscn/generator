# 生成器配置指南业务契合度完善任务书

> 主体归属：`mybatis-generator-core`；消费实例：`tmri_v4 / tmri-core / tmri-serv`。
> 整体状态：完成；进度：3/3；完成时间：2026-11-16；更新时间：2026-11-16。

## 1. 目标与范围

依据采购系统升级需求及 tmri_v4 当前真实生成配置，完善生成器配置指南中的设计到配置、生成输出、业务扩展与验收之间的映射。重点涉及 `generateHtml`、`layout`、`htmlElementDescriptor` 及变种、`generateCachePo`、`viewVo` 与双侧 `selectByTable` 关系查询。

本任务只维护生成器使用指南及本仓库文档导航/任务书，不修改生成器源码、DTD、tmri_v4 业务配置或代码；不运行生成器、不访问数据库、不生成或部署任何产物。tmri_v4 仅作为已核对的消费配置案例，业务事实与具体需求仍由 tmri_v4 项目文档维护。

## 2. 依据

- [生成器配置指南](..\generator-config-guide.md)
- [生成器权威说明](..\generator-authoritative-spec.md)
- [生成器项目指令](..\..\..\..\..\..\.github\copilot-instructions.md)
- [跨项目需求驱动设计规范 §3.5、§8.1、§8.2](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\project-design-standard.md)
- [AI 辅助开发过程规范 §1.1、§1.3](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\ai-assisted-development-standard.md)
- [采购管理系统升级需求清单](D:\java\idea_projs10\tmri_v4\tmri-docs\doc\采购管理系统升级需求清单.md)
- [采购管理系统功能需求整理](D:\java\idea_projs10\tmri_v4\tmri-docs\doc\procurement-system-functional-requirements.md)
- [tmri-core 实际生成配置](D:\java\idea_projs10\tmri_v4\tmri-core\src\main\resources\generatorConfig.xml)
- [tmri-serv 实际生成配置](D:\java\idea_projs10\tmri_v4\tmri-serv\src\main\resources\generatorConfig.xml)
- [采购管理系统设计总览](D:\java\idea_projs10\tmri_v4\tmri-docs\doc\procurement-system-design-overview.md)

## 3. 子任务

| 编号 | 内容 | 依赖 | 状态 | 验收条件 |
|---|---|---|---|---|
| G01 | 对照采购需求与 tmri 实际配置，提炼配置节点、生成器契约和业务实现边界 | 无 | 完成 | 已核对两份配置的 1.8.0 DTD、主数据页面/VO/缓存配置、服务侧共享控件及需求中的统计/导出/计划类型/季度要求；未将配置文本外推为运行实现 |
| G02 | 在配置指南中补充需求→字段/关系→配置→生成物→业务扩展→验收闭环，涵盖页面、组件、CachePo、ViewVo 和双侧关系 | G01 | 完成 | 已增加消费配置对照表、业务/生成器职责边界、计划类型与季度的未验证口径、统计/导出字段路径，以及 `selectByTable` 双侧适用判定；未将静态配置写成运行验收 |
| G03 | 更新生成器文档索引/映射并检查链接、命名、内容边界和修改差异 | G02 | 完成 | 已更新 doc-index/doc-map；指南新增章节、索引/映射和任务书引用路径均存在；`git diff --check` 通过；未执行生成、数据库或部署操作 |

## 4. 当前核对结论

- tmri-core 与 tmri-serv 两份配置均引用 `mybatis-generator-config_1_8_0.dtd`，配置了 Context、Table、页面、VO 和缓存字典等生成项。
- tmri-core 的预算、计划、项目主数据配置包含 `generateHtml/layout`、`viewVo` 与 `generateCachePo` 示例；tmri-serv 的 `reusableElements` 包含部门、用户、字典、金额校验、回调及 `plan_type` 控件描述。
- 对上述两份配置静态检索未发现 `<selectByTable>`、`<javaModelAssociation>` 或 `<javaModelCollection>`；不把预算—计划—项目的主从/审批链路误写成双向多对多关系。
- 最新升级需求的快速统计、自定义导出、采购计划类型与季度等需求不能仅凭页面字段、`viewVo` 或工具栏声明视为已完成业务实现；指南应要求追踪后端查询/导出契约及真实字段来源。
- 当前配置未出现 `quarter_`，仅据此不能判断真实数据库或生成模型不存在该字段；需要明确该项是配置文本观察，数据库结构与运行产物未在本任务验证。

## 5. 验收及清理

- 文档验证：相对链接、外部链接路径、章节锚点与索引入口静态检查；复核最终差异。
- 不执行 Maven 构建、生成器运行、消费者生成、数据库访问/更新、菜单权限变更、部署或版本库提交。
- 临时文件/脚本：无；不清理其他任务或用户现有资产。

## 6. 完成结果

- 已在配置指南 §20.4 增加采购需求到数据/配置/生成输出/业务实现/验收的闭环对照，覆盖页面布局、控件变种、CachePo、ViewVo、统计/导出、审批流程及关系查询。
- 已明确 `selectByTable` 是多对多双侧关系配置，tmri 预算—计划—项目与项目明细链路不可据此推断为多对多；对两份实际配置的检索结论限定为静态观察。
- 已在生成器 `.github/doc-index.md` 与 `doc-map.md` 登记配置指南新增内容及本任务书。
- 新增章节、索引/映射和任务书内引用路径检查通过；目标文档 `git diff --check` 通过。未做生成器构建、业务再生成、数据库或宿主运行验收。
- 本次仅修改生成器指南、文档索引/映射及本任务书；未修改用户原有 `.idea` 等工作区变更。
