# 生成器项目 Copilot 指令（统一入口）

本文档作为当前 MyBatis Generator 项目“再开发与完善”的 **Copilot 主指令文件**。  
`generator-core-dev-spec.md` 作为补充说明文件，与本文件保持一致，不得冲突。

## 1. 适用范围

- 修改/新增生成器能力（Java/XML/HTML/SQL 生成逻辑）
- 修改配置解析器（DTD、parser、配置对象）
- 新增数据库方言或调整 SQL 方言分支
- 调整扩展点（plugin hook、生成流程接入）

## 2. 核心链路与关注点

1. 生成链路：解析 -> 内省 -> 规则 -> 渲染 -> 写盘
2. 输出契约：命名、路径、前后端字段契约
3. 多方言一致性：`additionalDbTypes` 循环与 `databaseId` 分支
4. 覆盖策略：merge / overwrite / skip
5. 可扩展性：插件优先，避免硬改已生成产物

## 3. 多数据库与方言来源

- 项目支持多数据库方言，方言枚举为 `DatabaseType`（包名：`com.vgosoft.core.db.query`）。
- 当前仓库中，方言集合来自 `TableConfiguration.additionalDbTypes`：
  - 配置解析阶段：`MyBatisGeneratorConfigurationParser` 会合并 context/table 上的 `additionalDbTypes`。
  - 运行阶段：`DatabaseIntrospector` 会把当前连接识别出的数据库类型追加进 `additionalDbTypes`；识别失败时追加 `OTHER`。
- 结论：**生成逻辑必须按 `additionalDbTypes` 遍历，不要只按“当前数据库”单分支写死**。

## 4. MyBatis XML 动态 SQL 生成位置

- 主要目录：`mybatis-generator-core/src/main/java/org/mybatis/generator/codegen/mybatis3/xmlmapper/elements`
- 规则：一个 SQL 能力通常对应一个 `*ElementGenerator` 类（例如 `SelectByPrimaryKeyElementGenerator`）。
- 需要多方言差异时，在 XML 节点上使用 `databaseId` 分支生成。

## 5. databaseId 生成机制（关键）

- `AbstractXmlElementGenerator.buildInitialInsert(...)` 在 `databaseType != null` 时会写入：
  - `databaseId = databaseType.getCode()`
- 当 `additionalDbTypes` 有多个方言时，会生成多个同 id 语句，依赖 `databaseId` 区分。
- 结论：**新增/修改多方言 SQL 时，必须保证每个分支都能生成合法 SQL，并且 `databaseId` 与方言代码一致**。

## 6. 方言分组判定规则（按 SQL 语义，不按固定模板）

- `InsertOrUpdateSelectiveElementGenerator` 的分组只是一个已存在案例，**不能作为所有方法的固定分组模板**。
- 每个 `*ElementGenerator` 都需要先判断“该 SQL 是否存在方言语法差异”：
  - 无语法差异：不分组，复用一套 SQL。
  - 有语法差异：按 SQL 语义能力分组（例如 upsert、merge、分页/锁语法、函数差异），不是按历史类名机械分组。
- 分组要求：
  - 同一分组内必须能产出等价语义 SQL。
  - 分组边界必须能说明原因（语法不兼容点）。
  - 若某方言无法直接使用目标语法，必须显式声明并实现等价兜底策略，不可静默忽略。
- `insertOrUpdateSelective` 可作为“upsert 语义”参考分组，但仅在目标方法也属于同类语义时可借鉴。

### 6.1 动态 SQL 多方言统一兜底策略（新增）

- 适用范围：**所有需要多方言分支的动态 SQL**，不只限于“重复冲突忽略”语义。
- 总体原则：
  - 优先使用方言原生语法实现目标语义。
  - 原生语法缺失时，必须提供可跨方言执行的等价兜底 SQL，保证执行结果语义一致。
- `switch` 的 `default` 分支应返回通用兜底分组（或其他明确等价分组），不得静默 `break/return`。
- 典型示例（重复冲突忽略语义）：
  - 原生：`insert ignore`、`on conflict do nothing`
  - 兜底：`INSERT ... SELECT ... WHERE NOT EXISTS (...)`（必要时使用 `dual`/`SYSIBM.SYSDUMMY1` 等单行来源表）

## 7. additionalDbTypes 可选值（以 DatabaseType.code 为准）

- `additionalDbTypes` 的合法值必须与 `DatabaseType` 枚举的 `code` 属性严格一致。
- DTD 中的可选值列表仅作兼容性参考，可能滞后；出现不一致时，以 `DatabaseType.code` 为最终标准。
- 任何新增/调整数据库类型时，需要同步评估：
  - `DatabaseType` 枚举定义（code）
  - 配置解析（`additionalDbTypes` -> `DatabaseType.getType(...)`）
  - SQL 生成分支（`databaseId = databaseType.getCode()`）

## 8. 任务执行要求（给 Copilot 的硬约束）

- 涉及 SQL 生成改动时，按以下顺序输出与实现：
  1. 先判断该 SQL 是否需要方言分组；若需要，再按 SQL 语义给出分组与兼容策略。
  2. 再列出将修改的 `*ElementGenerator`/相关配置文件。
  3. 实现时保证 `additionalDbTypes` 多方言循环可用，避免漏分支。
  4. 若某方言暂不支持，必须显式说明影响范围与回退策略（不能静默忽略）。
- 不允许只改单一数据库分支而不评估其它分组影响。
- 若改动涉及方言名或可选值，同步检查 DTD/配置解析/SQL 生成三处一致性。
- `switch` 语句要求：必须包含 `default` 分支，且 `default` 不允许静默 `break/return`，应路由到明确兜底分组或显式失败信号。
- 枚举定义要求：枚举字段（常量）必须有语义注释，说明其用途/对应 SQL 能力。
- 不支持方言优先提供等价兜底实现；仅在确实无法保证语义一致时才允许显式失败，并需说明影响范围。

## 9. 数据库建设规范（必须遵守）

- 规范来源：依赖包 `com.vgosoft:vgosoft-core-db`。
- 参考文档：`docs/databaseConstructionSpec.md`（位于 `vgosoft-core-db` 源码包内）。
- 版本规则：以当前项目实际依赖的 `vgosoft-core-db` 版本内文档为准。
- 列名约束：列名必须至少包含一个 `_`；单个单词的列名必须在末尾补 `_`，例如 `name_`、`sort_`。
- 方言覆盖：支持的数据库方言必须覆盖 `DatabaseType`（包名：`com.vgosoft.core.db.query`）枚举所有项；新增枚举项时，必须同步补齐解析、DDL、SQL 生成和文档约束。
- 默认字段：必须优先使用 `DefaultColumnNameEnum`（包名：`com.vgosoft.core.constant.enums.db`），不得随意更名或用自定义字段替代其语义。
- 生成约束：涉及 SQL 生成时必须先判断是否需要方言分组，并按 SQL 语义而不是类名进行分组。

## 10. 关联文档

说明：以下“设计阶段/实现阶段”是**相对使用本生成器的新业务项目**而言；  
相对当前生成器仓库，这两份文档分别定位为“能力说明”和“使用手册”。

- 核心再开发补充说明：`.github/generator-core-dev-spec.md`
- 下游业务项目设计阶段（本项目定位：能力说明）：`src/main/resources/docs/generator-authoritative-spec.md`
- 下游业务项目实现阶段（本项目定位：使用手册）：`src/main/resources/docs/generator-config-guide.md`