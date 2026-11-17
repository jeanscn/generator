# 组合查询选项化生成支持任务书（E01）

> 主体归属：`mybatis-generator-core`；协作：`vgosoft-basic`（vgosoft-core-annotation、vgosoft-web2）、`tmri-ui4 / packages/framework`（E02 平行任务）。
> 上游契约：[列表组合查询选项化设计方案 §8 实现契约](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-web2\src\main\resources\docs\query-condition-options-design-overview.md)、[主体任务书](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-web2\src\main\resources\docs\task\query-condition-options-task.md)。
> 整体状态：完成；进度：4/4；更新时间：2026-11-18。
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
| G01 | DTD 与配置解析：`queryColumn` 内容模型增 `conditionOption` 分支，`conditionOption` 元素（`value` 必填、`remark`、`clear`），新增 `ConditionOptionConfiguration`，`QueryColumnConfiguration` 增选项集合；Parser 校验互斥、唯一 clear、常量 term、值唯一、控件基数与 switch 形态；按 core/DTD 一致规则处置版本 | 无 | 完成 | `QueryColumnConditionParserTest` 17 项通过，覆盖合法选项、互斥/唯一性、clear/term、选项上限、控件基数及非法控件拒绝 |
| G02 | 注解输出：`QueryConditionDesc`/`ViewMetaAnnotationPlugin` 输出 `options/tagName/multiple` 与 `@QueryConditionOption`；`hasQueryConditions` 语义不变 | G01、上游注解成员可用（契约替身或已供应工件） | 完成 | `QueryConditionDescTest` 6 项、`ViewMetaAnnotationPluginTest` 2 项通过；`select` 省略 `tagName`、`multiple=false` 省略、`dropdownlist` 保留原值、`QueryConditionOption` 以字符串全类名登记 import 规避未安装工件 |
| G03 | 分组装配：`ExampleGenerator` 生成分组 Criterion 与装配方法；`ExampleWhereClauseElementGenerator` 抽取共享 `<sql>` 片段并新增嵌套 OR 渲染；`ListElementGenerator`/`ExportElementGenerator` 接入 `resolveConditions` 分派 | G02、`vgosoft-web2` 分组结果类型可用（替身或工件） | 完成 | `ExampleGeneratorOptionGroupTest` 2 项源码断言并以 `BoundCondition` 契约替身内存编译；`ExampleWhereClauseDialectTest` 13 项内存 XML 渲染（组间 OR/组内 AND/绑定参数/LIKE 方言/守卫/`UpdateByExample` 复用共享片段）；`QueryConditionControllerElementsTest` 6 项及 `ExportElementGeneratorTest` 7 项覆盖 list/export `resolveConditions` 分派 |
| G04 | 回归与文档：旧 condition 配置生成物不变回归；`generator-config-guide.md` §5.3.2.1 修订（移除"不支持 OR 分组"边界并补选项契约）；本任务书与索引映射收尾 | G01—G03 | 完成 | 八类定向测试合计 54 项全绿（含 `TableConfigurationQueryConditionsTest` 旧配置回归）；§5.3.2.1 已补选项模式契约、互斥校验矩阵与运行期语义，OR 边界改写为"仅声明式选项分组" |

## 4. 关键边界与待决

- 编译期依赖：生成器自身代码仅需要 `HtmlElementTagTypeEnum` 等既有常量；测试默认沿用内存编译契约替身。T06 起 `vgosoft-web2` 已以 test 作用域引入（见 §5 记录），真实工件集成测试限于 `QueryConditionOptionsIntegrationTest`，其余测试不依赖工件供应状态。
- 业务消费：任何宿主表再生成均属 T05/业务任务，不在本任务书范围；判断产物是否具备分组接线以生成代码中 `resolveConditions` 调用为准。
- 既有测试：`QueryColumnConditionParserTest`、`QueryConditionDescTest`、`ExampleWhereClauseDialectTest`、`QueryConditionControllerElementsTest`、`TableConfigurationQueryConditionsTest`、`ViewMetaAnnotationPluginTest` 为扩展与回归基线。

## 5. 记录

- 2026-11-17：任务书创建；上游 T03/T04 本仓实施已完成（注解 `options/tagName/multiple`、`@QueryConditionOption`、`resolveConditions` 分组入口、`BoundConditionGroup`/`ResolvedConditions`）；待实施授权后启动 G01。
- 2026-11-18：按需求负责人指派启动 E01 实施。当前 core 为 `1.8.0-SNAPSHOT`，当前配置 DTD 为 `mybatis-generator-config_1_8_0.dtd`；本轮不运行生成器、不安装共享工件、不迁移业务配置、不提交版本库。
- 2026-11-18：G01 完成。当前版本 DTD、`ConditionOptionConfiguration`、`QueryColumnConfiguration`、Parser 结构/控件校验及合法/非法矩阵测试已具备；执行 `mvn -q -pl mybatis-generator-core "-Dtest=QueryColumnConditionParserTest" "-Dsurefire.failIfNoSpecifiedTests=false" test`，17 项通过。
- 2026-11-18：G02 完成。`QueryConditionDesc` 输出 `options/tagName/multiple` 与 `@QueryConditionOption`；`ConditionOptionConfiguration` term 列校验复用 `assertTermColumn`；`QueryConditionDescTest` 6 项、`ViewMetaAnnotationPluginTest` 2 项通过。
- 2026-11-18：G03 完成。`TableConfiguration.hasOptionQueryConditions()` 新增；`ExampleGenerator` 生成 `orGroups`/`groupValue` 谓词成员与 `andBoundConditionGroup(List<List<BoundCondition>>)` 装配方法；`ExampleWhereClauseElementGenerator` 抽取共享 `Example_Where_Clause_Criterion_Choose` 片段，无类型列表按 `groupValue` 分派嵌套 OR 渲染（UpdateByExample 变体复用同一片段、不重复登记）；list/export 按 `hasOptionQueryConditions()` 分派 `resolveConditions` 并将 `resolved.groups()` 装配为 OR 组谓词。`ExampleGeneratorOptionGroupTest` 2 项（含 `BoundCondition` 契约替身内存编译）、`ExampleWhereClauseDialectTest` 13 项、`QueryConditionControllerElementsTest` 6 项、`ExportElementGeneratorTest` 7 项通过。
- 2026-11-18：G04 完成。`generator-config-guide.md` §5.3.2.1 增补选项模式契约（元素表、属性速查、互斥校验、请求载荷、语义边界与运行期规则）；八类定向测试合计 54 项全部通过：`mvn -q -pl mybatis-generator-core "-Dtest=QueryColumnConditionParserTest,QueryConditionDescTest,QueryConditionControllerElementsTest,ExampleWhereClauseDialectTest,ExampleGeneratorOptionGroupTest,ExportElementGeneratorTest,ViewMetaAnnotationPluginTest,TableConfigurationQueryConditionsTest" "-Dsurefire.failIfNoSpecifiedTests=false" test`。任务书已在 `.github/doc-index.md` 与 `.github/doc-map.md` 登记，无需改动索引映射。E01 收尾；业务表再生成与共享工件安装仍待另行授权。
- 2026-11-18：上游 T06 本地集成验收执行（用户已授权并确认 7.0.0-SNAPSHOT 工件安装）。新增 `org.mybatis.generator.codegen.mybatis3.QueryConditionOptionsIntegrationTest` 4 项，以已安装真实工件贯通「真实注解夹具 → `resolveConditions` → `ResolvedConditions`/`BoundConditionGroup` → 内存编译生成 Example 反射装配 `andBoundCondition`/`andBoundConditionGroup` → 生成 `<sql>` 片段经 `XMLMapperBuilder`/`BoundSql` 渲染」：实测 `t.del_flag=?and((t.phase_state>?andt.apply_state=?)or(t.phase_state<?))`、四值全绑定；clear/省略无谓词；未知/混传/重复/参数型带 options 四类非法提交显式失败；list 生成体接线断言与空分组防御路径通过。`mybatis-generator-core/pom.xml` 增 `vgosoft-web2` test 作用域依赖（注释标明仅测试用，编译/运行不依赖）。契约替身编译路径保留于 `ExampleGeneratorOptionGroupTest` 未移除。命令：`mvn -q -pl mybatis-generator-core test "-Dtest=QueryConditionOptionsIntegrationTest" "-Dsurefire.failIfNoSpecifiedTests=false"` → 4/4；合并八类定向共 51 项全绿。证据层级与未覆盖范围登记于主任务书 T06 条目（无实库/浏览器/宿主验证）。
