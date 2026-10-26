# mybatis-generator-core 待处理问题台账

> 所属项目：`generator/mybatis-generator`
> 模块/范围：`mybatis-generator-core`
> 规范：[非主线问题登记与后续处理规范](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\deferred-issue-management-standard.md)
> 更新时间：2026-10-26

## 1. 问题总览

| ID | 优先级 | 类别 | 标题 | 证据状态 | 处理状态 | 发现日期 | 关联任务/主记录 |
|---|---|---|---|---|---|---|---|
| generator-core-ISSUE-0001 | 中 | 其他 | CAS/逻辑删除回归依赖可变消费配置导致历史基线漂移 | 已复现 | 待处理 | 2026-10-26 | [Service 停用任务书](D:\java\idea_projs7\project-vgosoft\vgosoft-sensitive\src\main\resources\docs\task\service-entry-disable-contract-task.md) |
| generator-core-ISSUE-0002 | 中 | 数据一致性 | 根实体结构同步可能为主键生成可空列变更 | 已复现 | 待处理 | 2026-10-26 | [Service 停用任务书](D:\java\idea_projs7\project-vgosoft\vgosoft-sensitive\src\main\resources\docs\task\service-entry-disable-contract-task.md) |
| generator-core-ISSUE-0003 | 中 | 其他 | 未启用停用配置的业务消费者缺真实再生成编译测试证据 | 源码已定位 | 待处理 | 2026-10-26 | [遗留项核查](D:\java\idea_projs7\project-vgosoft\vgosoft-sensitive\src\main\resources\docs\task\service-entry-disable-contract-task.md#5-后续遗留项核查2026-10-26) |

## 2. 问题详情

### generator-core-ISSUE-0001 CAS/逻辑删除回归依赖可变消费配置导致历史基线漂移

- 发现来源：显式 Service 停用配置实施后的关联兼容回归，2026-10-26。
- 位置与证据：`src\test\java\org\mybatis\generator\codegen\mybatis3\CasConsumerBaselineTest.java` 的 `consumerXml` 从消费仓当前 `generatorConfig.xml` 读取配置。`CasWriteGenerationTest` 在当前输入下执行 43 项，5 失败、1 异常；历史完整原始基线断言期望 75 文件而实际 80，另有固定配置二次生成时逻辑删除条件列不存在。`LogicalDeleteGenerationTest` 执行 25 项，2 失败、3 异常，包含重复 `deleteByColumnIdAndVersion` 和条件列 `[id_, version_]` 不存在。
- 问题现象与触发条件：消费配置增加 revision Service/测试及逻辑删除配置后，历史快照、输出文件数与测试重复添加的配置不再匹配；复用已被内省处理的配置二次生成也会触发条件列校验。报告位于 `target\surefire-reports`；未连接远程数据库。
- 影响与分级依据：影响历史兼容测试的可重复性及失败定位，不能由上述失败直接认定当前生成器发生默认行为回归；存在独立配置测试规避，故为中优先级。
- 与当前任务的关系：当前显式停用独立回归 41 项通过，敏感生成回归 20 项无失败（1 项既有跳过）、缓存服务回归 2 项通过。此问题不通过重录历史 ZIP、修改业务配置或放宽断言掩盖；原完整基线验证仍标记未通过。
- 建议后续处理：独立批准后固定各测试的完整去敏输入、DDL/元数据和基线版本；涉及二次生成的场景重新解析配置，并分别验证历史兼容和当前消费者配置。不得仅更新计数或覆盖历史快照。
- 责任与关联：生成器测试维护方，负责人待指定；当前任务只登记并向父任务报告，未修复该历史测试问题。
- 更新与关闭记录：2026-10-26 首次登记，状态待处理。

### generator-core-ISSUE-0002 根实体结构同步可能为主键生成可空列变更

- 发现来源：2026-10-26 使用六列 H2/MySQL 模式执行 revision Service 隔离生成。
- 位置与证据：`config.Context.introspectTables` 原有根实体同步路径、`custom.db.ValidateDatabaseTable.isModifyColumn/columnBuilder` 按 `ColumnMeta.nullable()` 比较并构造变更，未对主键的不可空约束作独立保护。H2 实际尝试 `ALTER TABLE sensitive_rule_revision MODIFY COLUMN id_ VARCHAR(36)`，因主键不可改为 nullable 报错并转为 warning；日志在 `target\revision-driver-confirmed.log`。未连接真实业务数据库。
- 问题现象与触发条件：根实体注解期望、JDBC 元数据或根字段其他描述不一致时触发结构同步；构造出的目标列允许空值，与现存主键约束不一致。注解与元数据偏差的最终归属仍须独立确认。
- 影响与分级依据：默认同步入口可能产生不合法 DDL 并仅记录警告；当前已在 H2 复现失败，其他数据库未验证。有明确离线规避方式，故为中优先级。
- 与当前任务的关系：为满足获准离线生成的零结构写入要求，新增默认兼容的程序 API `Context.setRootClassSchemaSynchronizationEnabled(false)`，驱动已启用，实际 H2 重新运行零警告。该开关不修复默认同步路径的主键 DDL 语义，不将警告当作成功验收。
- 建议后续处理：独立评审根实体 ColumnMeta 与实际数据库类型、主键及 nullable 约束的优先级；验证同步 DDL 的主键保护和失败传播，不未经批准修改基础实体注解或业务数据库。
- 责任与关联：生成器同步能力维护方，注解归属方待联合确认；当前只修复离线驱动隔离接点。
- 更新与关闭记录：2026-10-26 首次登记，默认同步语义问题仍待处理；隔离驱动退出码0，证据 `target\revision-driver-no-schema-sync.log`。

### generator-core-ISSUE-0003 未启用停用配置的业务消费者缺真实再生成编译测试证据

- 发现来源：2026-10-26 用户提出 P3；本轮仅核实、登记与提出方案，不执行消费者再生成或修改生产/CI。
- 位置与证据：主体 [停用任务书](D:\java\idea_projs7\project-vgosoft\vgosoft-sensitive\src\main\resources\docs\task\service-entry-disable-contract-task.md) 的 G01 包含默认兼容/敏感生成/缓存定向回归，S01 只落地 revision 两项文件并执行 Sensitive 测试，没有 vgosoft-application 再生成后编译测试记录。`D:\java\idea_projs7\project-vgosoft\vgosoft-application\src\main\resources\generatorConfig.xml:74-75,120-123` 两表启用生成 Service/Controller 单元测试。当前证据证明该交付缺口，未证明实际生成输出已回归。
- 版本控制核实：从 `D:\java\idea_projs14\generator\mybatis-generator` 执行 `git rev-parse --show-toplevel` 返回父目录 `D:\java\idea_projs14\generator`，因此“子目录无 .git 即不在版本控制”的判断不成立。现有 `git ls-files` 包含新停用配置/实现/测试以及既有生成器文件；`git status` 显示部分新增文件在暂存区、部分修改未暂存，不能把 Git 索引中的 A/AM 当成已提交快照。本轮不提交、撤销或改动暂存区。
- 改动归属：此前任务已明确 `AbstractUnitTestGenerator.java` 是既有 dirty，未纳入其实施；本轮 `ExampleGenerator.java` 在暂存和未暂存区域均无差异。不能把它们仅因文件名列入本次停用变更；其他工作副本变更仍需提交前逐项归属审阅。
- 影响与分级依据：中。新能力及 revision 已有局部证据，但其他实际消费者的默认兼容完整链证据不足；不把缺验收等同已发生故障。
- 与当前任务的关系：本轮不重开 revision 问题；跨消费者交付证据作为独立后续项，不通过重录历史基线掩盖 generator-core-ISSUE-0001。
- 建议后续处理：获批后固定去敏配置、相同版本元数据、生成器/core/DTD/插件和依赖，优先选择 vgosoft-application 两表，在隔离目录用改前/改后生成器对相同输入执行 parse/customConfig/完整生成并保存全输出清单与逐文件差异；未配置停用时检查无意外差异。再将新生成输出放入隔离消费副本，按真实依赖编译主代码与测试并显式执行 Service/Controller 生成测试。对手写扩展也核对覆盖保护。差异须解释，不能强求有预期变更的输出字节完全一致；不得触发目标库结构同步、菜单/权限更新或直接覆盖工作副本。
- 责任与关联：生成器维护方与 application 消费模块维护方；版本提交及基线来源由仓库负责人确认。
- 更新与关闭记录：2026-10-26 首次登记，中/源码已定位/待处理；版本控制疑点已澄清，不建立“不受版本控制”缺陷。关闭要求为同输入改前/改后生成差异、消费主代码/测试编译与运行证据齐备。
