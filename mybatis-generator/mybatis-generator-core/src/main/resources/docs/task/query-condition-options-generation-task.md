# 组合查询选项化生成支持任务书（E01）

> 主体归属：`mybatis-generator-core`；协作：`vgosoft-basic`（vgosoft-core-annotation、vgosoft-web2）、`mpmm-ui / @vgosoft/framework`（E02 平行任务）。
> 上游契约：[列表组合查询选项化设计方案 §8 实现契约](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-web2\src\main\resources\docs\query-condition-options-design-overview.md)、[主体任务书](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-web2\src\main\resources\docs\task\query-condition-options-task.md)。
> 整体状态：待执行；进度：0/4；更新时间：2026-11-17。
> 需求负责人已指派本会话维护者实施；生成器执行、共享工件安装、业务配置迁移与版本库提交均不在本任务书授权范围，另行批准。

## 1. 范围与依据

在 `queryType="condition"` 的 `queryColumn` 上扩展 `<conditionOption>` 子节点：选项对应服务端固定常量谓词组，前端只提交选项标识；"全部"由 `clear="true"` 声明且不产生谓词；支持 radio/select/checkbox/dropdownlist/switch 控件与单多选基数。同时扩展生成产物的分组装配能力：Example `Criteria` 新增分组谓词方法，Mapper XML `Example_Where_Clause` 新增嵌套 OR 渲染分支，list/export 接入 `QueryConditionResolver.resolveConditions` 分组结果。

依据：[生成器指令](..\..\..\..\..\..\.github\copilot-instructions.md)、[配置指南](..\generator-config-guide.md)（§5.3.2.1 现有组合条件契约）、[core/DTD 一致规则](..\generator-config-guide.md)、[多子任务规范](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\project-design-standard.md#81-多子任务执行与任务书管理)、[全局过程门禁](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\ai-assisted-development-standard.md)。

本任务只修改生成器源码、配置 DTD、正式测试与本仓文档；不修改业务仓生成配置或受保护生成物，不执行业务表再生成，不安装或发布共享工件（工件供应方式按环境权限另行确认）。

## 2. 冻结契约要点（来自设计 §8，已于 2026-11-17 确认）

- 分组原语：新增分组 Criterion 持有 `List<List<Criterion>>`（或以 Criteria 列表承载），`Example_Where_Clause` 抽取共享 `<sql>` 片段渲染"条件列表"，顶层与嵌套 OR 组复用同一 `choose`（含 LIKE 方言分支与 `QueryDialectGuard`），形成 `(inner AND) OR (inner AND)` 单谓词再 AND 进 criteria；值全部 `#{}` 绑定，无顶层 OR 逃逸。
- 请求入口：生成 list/export 调用 `QueryConditionResolver.resolveConditions`（返回平铺 `conditions` 与 `groups`），逐 criteria 分派平铺 BoundCondition 并将各 `BoundConditionGroup` 装配为一个 OR 组谓词；视图不含选项型条件时行为与现有一致。
- 控件与基数统一解析（配置侧与运行元数据一致）：radio 单选禁 `multiple`；select 默认单选；dropdownlist 逻辑同 select；checkbox 未指定归一 `multiple=true`、显式 false 报错；switch 恰一个 clear 加一个普通选项；未指定 tagType 归一 select。
- 选项约束：条件内恰一个 `clear="true"` 且无 term；普通选项至少一个常量 `conditionTerm`（`param` 禁止）；`value` 非空唯一；普通选项数 ≤20；`conditionOption` 与 `conditionParam`、直接 `conditionTerm` 互斥。
- 兼容：`querys`/`queryConditions` 的 order 统一编号规则不变；旧 condition 配置生成物不变（配置回归覆盖）。

## 3. 子任务

| 编号 | 内容 | 依赖 | 状态 | 验收条件及结果 |
|---|---|---|---|---|
| G01 | DTD 与配置解析：`queryColumn` 内容模型增 `conditionOption` 分支，`conditionOption` 元素（`value` 必填、`remark`、`clear`），新增 `ConditionOptionConfiguration`，`QueryColumnConfiguration` 增选项集合；Parser 校验互斥、唯一 clear、常量 term、值唯一、控件基数与 switch 形态；按 core/DTD 一致规则处置版本 | 无 | 待执行 | 配置解析定向测试覆盖合法与非法矩阵 |
| G02 | 注解输出：`QueryConditionDesc`/`ViewMetaAnnotationPlugin` 输出 `options/tagName/multiple` 与 `@QueryConditionOption`；`hasQueryConditions` 语义不变 | G01、上游注解成员可用（契约替身或已供应工件） | 待执行 | 注解输出断言覆盖选项、clear、控件与基数 |
| G03 | 分组装配：`ExampleGenerator` 生成分组 Criterion 与装配方法；`ExampleWhereClauseElementGenerator` 抽取共享 `<sql>` 片段并新增嵌套 OR 渲染；`ListElementGenerator`/`ExportElementGenerator` 接入 `resolveConditions` 分派 | G02、`vgosoft-web2` 分组结果类型可用（替身或工件） | 待执行 | 生成物源码断言 + 内存编译；XML 渲染断言覆盖 OR 括号、值绑定、LIKE 方言分支与未支持方言门卫；不拼接用户值 |
| G04 | 回归与文档：旧 condition 配置生成物不变回归；`generator-config-guide.md` §5.3.2.1 修订（移除"不支持 OR 分组"边界并补选项契约）；本任务书与索引映射收尾 | G01—G03 | 待执行 | 既有相关测试全量通过，文档与实现一致 |

## 4. 关键边界与待决

- 编译期依赖：生成器自身代码可能仅需要 `HtmlElementTagTypeEnum` 等既有常量；凡需引用新注解/分组类型的测试可沿用上轮做法（内存编译契约替身），真实工件供应另行确认，不在默认假设下声称共享工件已交付。
- 业务消费：任何宿主表再生成均属 T05/业务任务，不在本任务书范围；判断产物是否具备分组接线以生成代码中 `resolveConditions` 调用为准。
- 既有测试：`QueryColumnConditionParserTest`、`QueryConditionDescTest`、`ExampleWhereClauseDialectTest`、`QueryConditionControllerElementsTest`、`TableConfigurationQueryConditionsTest`、`ViewMetaAnnotationPluginTest` 为扩展与回归基线。

## 5. 记录

- 2026-11-17：任务书创建；上游 T03/T04 本仓实施已完成（注解 `options/tagName/multiple`、`@QueryConditionOption`、`resolveConditions` 分组入口、`BoundConditionGroup`/`ResolvedConditions`）；待实施授权后启动 G01。
