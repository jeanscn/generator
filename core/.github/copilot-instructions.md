# 项目固定上下文（已按当前代码校验）

## 1. 多数据库与方言来源

- 项目支持多数据库方言，方言枚举为 `DatabaseType`（包名：`com.vgosoft.core.db.query`）。
- 当前仓库中，方言集合来自 `TableConfiguration.additionalDbTypes`：
  - 配置解析阶段：`MyBatisGeneratorConfigurationParser` 会合并 context/table 上的 `additionalDbTypes`。
  - 运行阶段：`DatabaseIntrospector` 会把当前连接识别出的数据库类型追加进 `additionalDbTypes`；识别失败时追加 `OTHER`。
- 结论：**生成逻辑必须按 `additionalDbTypes` 遍历，不要只按“当前数据库”单分支写死**。

## 2. MyBatis XML 动态 SQL 生成位置

- 主要目录：`mybatis-generator-core/src/main/java/org/mybatis/generator/codegen/mybatis3/xmlmapper/elements`
- 规则：一个 SQL 能力通常对应一个 `*ElementGenerator` 类（例如 `SelectByPrimaryKeyElementGenerator`）。
- 需要多方言差异时，在 XML 节点上使用 `databaseId` 分支生成。

## 3. databaseId 生成机制（关键）

- `AbstractXmlElementGenerator.buildInitialInsert(...)` 在 `databaseType != null` 时会写入：
  - `databaseId = databaseType.getCode()`
- 当 `additionalDbTypes` 有多个方言时，会生成多个同 id 语句，依赖 `databaseId` 区分。
- 结论：**新增/修改多方言 SQL 时，必须保证每个分支都能生成合法 SQL，并且 `databaseId` 与方言代码一致**。

## 4. 方言分组判定规则（按 SQL 语义，不按固定模板）

- `InsertOrUpdateSelectiveElementGenerator` 的分组只是一个已存在案例，**不能作为所有方法的固定分组模板**。
- 每个 `*ElementGenerator` 都需要先判断“该 SQL 是否存在方言语法差异”：
  - 无语法差异：不分组，复用一套 SQL。
  - 有语法差异：按 SQL 语义能力分组（例如 upsert、merge、分页/锁语法、函数差异），不是按历史类名机械分组。
- 分组要求：
  - 同一分组内必须能产出等价语义 SQL。
  - 分组边界必须能说明原因（语法不兼容点）。
  - 若某方言无法等价支持，必须显式声明策略（降级/禁用/抛出生成期提示）。
- `insertOrUpdateSelective` 可作为“upsert 语义”参考分组，但仅在目标方法也属于同类语义时可借鉴。

## 5. additionalDbTypes 可选值（以 DatabaseType.code 为准）

- `additionalDbTypes` 的合法值必须与 `DatabaseType` 枚举的 `code` 属性严格一致。
- DTD 中的可选值列表仅作兼容性参考，可能滞后；出现不一致时，以 `DatabaseType.code` 为最终标准。
- 任何新增/调整数据库类型时，需要同步评估：
  - `DatabaseType` 枚举定义（code）
  - 配置解析（`additionalDbTypes` -> `DatabaseType.getType(...)`）
  - SQL 生成分支（`databaseId = databaseType.getCode()`）

## 6. 任务执行要求（给 Copilot 的硬约束）

- 涉及 SQL 生成改动时，按以下顺序输出与实现：
  1. 先判断该 SQL 是否需要方言分组；若需要，再按 SQL 语义给出分组与兼容策略。
  2. 再列出将修改的 `*ElementGenerator`/相关配置文件。
  3. 实现时保证 `additionalDbTypes` 多方言循环可用，避免漏分支。
  4. 若某方言暂不支持，必须显式说明影响范围与回退策略（不能静默忽略）。
- 不允许只改单一数据库分支而不评估其它分组影响。
- 若改动涉及方言名或可选值，同步检查 DTD/配置解析/SQL 生成三处一致性。