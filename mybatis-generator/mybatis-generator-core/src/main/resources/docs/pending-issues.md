# mybatis-generator-core 待处理问题台账

> 所属项目：`generator/mybatis-generator`
> 模块/范围：`mybatis-generator-core`
> 规范：[非主线问题登记与后续处理规范](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\deferred-issue-management-standard.md)
> 更新时间：2026-11-20

## 1. 问题总览

| ID | 优先级 | 类别 | 标题 | 证据状态 | 处理状态 | 复核状态 | 发现日期 | 关联任务/主记录 |
|---|---|---|---|---|---|---|---|---|
| generator-core-ISSUE-0001 | 中 | 其他 | CAS/逻辑删除回归依赖可变消费配置导致历史基线漂移 | 已复现并固定当前输入 | 已处理（当前快照版本化） | — | 2026-10-26 | [Service 停用任务书](D:\java\idea_projs7\project-vgosoft\vgosoft-sensitive\src\main\resources\docs\task\service-entry-disable-contract-task.md) |
| generator-core-ISSUE-0002 | 中 | 数据一致性 | 根实体结构同步可能为主键生成可空列变更 | 已修复并回归 | 已处理（主键强制 NOT NULL） | — | 2026-10-26 | [Service 停用任务书](D:\java\idea_projs7\project-vgosoft\vgosoft-sensitive\src\main\resources\docs\task\service-entry-disable-contract-task.md) |
| generator-core-ISSUE-0003 | 中 | 其他 | 未启用停用配置的业务消费者缺真实再生成编译测试证据 | core 1.7→1.8 同输入生成对照、消费编译及 Service 测试已验证；Controller 测试受隔离上下文依赖阻塞 | 部分完成，仍待处理（跨仓库，需 application/root 维护方提供隔离测试配置） | — | 2026-10-26 | [遗留项核查](D:\java\idea_projs7\project-vgosoft\vgosoft-sensitive\src\main\resources\docs\task\service-entry-disable-contract-task.md#5-后续遗留项核查2026-10-26) |
| generator-core-ISSUE-0004 | 中 | 测试与基线 | 锁查询离线输出基线漂移（Excel 限额拒绝已修复） | 已定位根因并更新基线（2026-11-07） | 已处理（基线更新为 codegen 子包设计后的当前输出） | — | 2026-10-26 | 无（生成器关联回归发现） |
| generator-core-ISSUE-0005 | 中 | 测试与基线 | 消费方完整输出基线测试存在既有失败（前端产物重复键） | 已定位根因并修复（2026-11-07） | 已处理（测试 key 构造纳入 targetProject 尾部目录） | — | 2026-10-26 | 无（import 登记侧审计的验证旁路发现） |
| generator-core-ISSUE-0006 | 中 | 测试与基线 | CAS 消费者生成 fixture 的 TIMESTAMP 类型与受管契约不匹配 | 已定位根因并修复（2026-11-07） | 已处理（测试补 customConfig，与真实 Maven 入口一致） | — | 2026-10-28 | Sensitive SC05 隔离生成回归阻塞 |
| generator-core-ISSUE-0007 | 低 | 测试与基线 | SensitiveGenerationTest 的 FreeMarker include 缺少模板加载器 | 已定位根因并修复（2026-11-07） | 已处理（render 补 ClassTemplateLoader） | — | 2026-10-28 | 无（同批生成器回归中的非主线问题） |
| generator-core-ISSUE-0008 | 中 | 数据一致性 | 根实体结构同步 DDL 失败仅告警且生成继续 | 已复现 | 待处理（2026-11-07 确认保持登记，不实现 fail-fast） | — | 2026-10-29 | [根实体结构同步检查任务书](task/root-class-schema-sync-task.md) |
| generator-core-ISSUE-0009 | 中 | 依赖兼容 | `vgosoft-tool:7.0.0-SNAPSHOT` 的 `VArrayUtil.merge` 新增受检异常导致生成器无法全量编译 | 已复现 | 已解决（生成器适配异常契约） | 复核通过 | 2026-11-07 | [审计字段自动填充任务书](task/audit-field-filling-task.md) T08 |
| generator-core-ISSUE-0010 | 中 | 权限 | `generateController` 缺少按接口简称排除 `@PreAuthorize` 的配置 | 已实现并回归 | 已处理（apiPermissions 白名单） | — | 2026-11-20 | [接口权限白名单任务书](task/api-permission-whitelist-task.md) |
| generator-core-ISSUE-0011 | 中 | 测试与基线 | 全量测试中既有基线漂移与 Excel 导入夹具失败 | 已定位根因并修复（2026-11-15） | 已处理（基线重录 + 夹具固定快照 + 孤儿 import 生成逻辑修复） | — | 2026-11-15 | VgoTable 查询表单 W2 |
| generator-core-ISSUE-0012 | 低 | 测试与基线 | `pageNavigatorGeneratesMatchingEditAndDetailAnchorsWithComponentDefaults` 期望 width 220/zIndex 60 与当前默认 150/120 不符 | 已复现 | 待处理（基线断言需随默认值演进更新） | — | 2026-11-16 | [表单 Markdown 字段支持任务书](task/markdown-form-field-task.md) |
| generator-core-ISSUE-0013 | 中 | 测试与基线 | Service Impl 输出演进导致 CAS 基线 3 个 Impl 文件与两个哈希基线的服务实现层漂移 | 已复现并归因（合入提交 5c98f5e9 等） | 待处理（需负责人批准重录基线/摘要） | — | 2026-11-20 | [接口权限白名单任务书](task/api-permission-whitelist-task.md) 回归旁路发现 |

## 2. 问题详情

### generator-core-ISSUE-0001 CAS/逻辑删除回归依赖可变消费配置导致历史基线漂移

- 发现来源：显式 Service 停用配置实施后的关联兼容回归，2026-10-26。
- 位置与证据：原 `consumerXml` 从消费仓当前 `generatorConfig.xml` 读取配置，导致输入漂移。当前配置生成 80 个原始文件，而旧历史 ZIP 仅含 75 个；配置差异还使 `LogicalDeleteGenerationTest` 重复添加 `selectByColumn` 并触发重复删除方法。历史原始 XML/DDL 已无法取得，因此按批准方案新建当前输入快照 `src\test\resources\sc03\consumer-current-v1\` 及其 80 文件基线 `consumer-current-v1-parser-baseline.zip`；旧 `non-enabled-parser-baseline.zip` 未修改。
- 问题现象与触发条件：配置输入随外部消费仓变化，导致当前输出与旧历史快照不可直接比较；同一已内省配置被再次生成时，逻辑删除条件列也可能丢失。本次 CAS/逻辑删除回归改用固定快照和固定 DDL；逻辑删除测试替换既有 `selectByColumn`，避免重复注入；重复生成场景重新解析配置。未连接远程数据库。
- 影响与分级依据：原问题影响测试可重复性及失败归因。新快照明确标识为当前消费配置版本，不声称复原旧历史输入；旧 ZIP 继续保留作历史证据，避免以不明来源覆盖历史。
- 与当前任务的关系：`CasWriteGenerationTest`、`LogicalDeleteGenerationTest` 与快照捕获测试共 69 项通过。当前快照原始输出逐文件匹配新版本 ZIP；CAS 关闭/开启输出差异断言保持有效。CAS 专项测试配置关闭无关 HTML/ExcelImport 入口，以隔离 CAS 行为；历史旧 ZIP 与当前快照之间的差异未被重录或掩盖。
- 后续约束：今后修改消费配置时，新建带版本号的去敏 XML、DDL/元数据和输出基线，不覆盖旧版本；任何新基线须审阅并说明逐文件差异。旧 ZIP 对应的原始输入不可得，故不再用它断言当前消费者输出与历史配置等价。
- 责任与关联：生成器测试维护方。当前输入固定问题已处理；旧历史输入无法复原的限制已记录。
- 更新与关闭记录：2026-10-26 首次登记；2026-10-27 新增当前快照 v1、保留旧 ZIP、修复配置重复注入及重复内省使用，聚焦回归通过。

### generator-core-ISSUE-0002 根实体结构同步可能为主键生成可空列变更

- 发现来源：2026-10-26 使用六列 H2/MySQL 模式执行 revision Service 隔离生成。
- 位置与证据：`custom.db.ValidateDatabaseTable.isModifyColumn/columnBuilder` 原按 `ColumnMeta.nullable()` 比较并构造变更，未对主键不可空约束作独立保护。新增 `isNullable` 将实际主键或根实体声明主键的列按不可空处理，并在注解标为可空时写入 warning。新增 `RootClassSchemaSynchronizationTest` 覆盖仅有主键 nullable 差异不发起 DDL，以及其他元数据变化仍生成 `NOT NULL` 的 DDL。
- 问题现象与触发条件：根实体 nullable 标注与 JDBC 元数据不一致时，原逻辑可生成主键可空变更。现在该差异不会单独触发列修改；如果长度等其他元数据需要变更，目标列仍保持 `NOT NULL`。隔离测试通过拒绝执行的 Statement 记录 SQL；未连接或写入真实数据库。
- 影响与分级依据：主键 DDL 不再因根实体 nullable 标注被降为可空；对不一致标注显式记录 warning，避免静默忽略。测试覆盖 MySQL DDL 渲染和通用主键规则；未对其他真实数据库执行验证。
- 与当前任务的关系：为满足获准离线生成的零结构写入要求，新增默认兼容的程序 API `Context.setRootClassSchemaSynchronizationEnabled(false)`，驱动已启用，实际 H2 重新运行零警告。该开关不修复默认同步路径的主键 DDL 语义，不将警告当作成功验收。
- 建议后续处理：主键约束优先于根实体 nullable 标注；本修复不更改基础实体注解或关闭默认结构同步。若未来扩展列同步语义，继续使用拒绝执行写入的隔离测试验证 SQL。
- 责任与关联：生成器同步能力维护方；根实体注解归属方无需为本修复修改业务模型。
- 更新与关闭记录：2026-10-26 首次登记；2026-10-27 按主键语义保护 nullable、增加隔离 SQL 回归，`RootClassSchemaSynchronizationTest` 2 项及 `DisabledServiceEntryGenerationTest` 57 项通过，问题关闭。

### generator-core-ISSUE-0003 未启用停用配置的业务消费者缺真实再生成编译测试证据

- 发现来源：2026-10-26 用户提出 P3；2026-10-27 在获准范围内继续验证 `vgosoft-application`，不修改其生产源文件、生成配置或 CI。
- 位置与证据：`D:\java\idea_projs7\project-vgosoft\vgosoft-application\src\main\resources\generatorConfig.xml:74-75,120-123` 两表启用 Service/Controller 单元测试且没有 `disabledMethod`。application 的有效依赖为 `mybatis-generator-core:1.8.0-SNAPSHOT`，有效 Maven 插件为 `mybatis-generator-maven-plugin:1.8.0-SNAPSHOT`；插件 POM 对 core 使用 `${project.version}`。`pom.xml` 声明 core 为 optional，因此单看 application 运行时依赖不能替代插件版本核验。
- 版本控制核实：从 `D:\java\idea_projs14\generator\mybatis-generator` 执行 `git rev-parse --show-toplevel` 返回父目录 `D:\java\idea_projs14\generator`，因此“子目录无 .git 即不在版本控制”的判断不成立。现有 `git ls-files` 包含新停用配置/实现/测试以及既有生成器文件；`git status` 显示部分新增文件在暂存区、部分修改未暂存，不能把 Git 索引中的 A/AM 当成已提交快照。本轮不提交、撤销或改动暂存区。
- 改动归属：此前任务已明确 `AbstractUnitTestGenerator.java` 是既有 dirty，未纳入其实施；本轮 `ExampleGenerator.java` 在暂存和未暂存区域均无差异。不能把它们仅因文件名列入本次停用变更；其他工作副本变更仍需提交前逐项归属审阅。
- 同输入再生成：在 application 的 `target\issue-0003-validation` 临时副本中，使用原配置解析/customConfig、两张固定本地 DDL、同一离线元数据适配器，分别以 core `1.7.0-SNAPSHOT` 和 `1.8.0-SNAPSHOT` 生成；不连接数据库，关闭菜单/模块更新，输出只写临时目录。两版各 66 个完整输出项，无新增/删除、12 项内容变化。变化涉及 Controller OpenAPI 描述/模板清理、分类表 Excel 从 Alibaba EasyExcel 迁移到 Fesod、分类 Service 增加字典预取接口与缓存键支持、生成测试头部/导入、Request VO 删除未用导入、权限 SQL 排序号 15→16、Mapper XML 增加显式 NULL 排序及稳定次序。application 配置未声明停用入口，这些差异不属于停用拒绝输出；该版本间对照不能替代同一 1.8 代码在停用功能改动前后的精确源码对照。
- 与现有消费源码对比的解释：早期列出的 10 个源文件差异不是有效的回归结论。离线元数据解析器把未写宽度的 INT/TIMESTAMP 记为 `COLUMN_SIZE=0`，导致 `@Digits` 与 ColumnMeta 尺寸变化；固定 DDL 的 TEXT 与现有实体 MEDIUMTEXT/LONGTEXT 注释不一致；`parent_id` 的字符串默认值保留了引号；Request VO 的 `TableField` 是生成后可清理的未用导入。故这些现有源码差异不能用来替代上面的同输入版本比较。
- 消费编译与测试：隔离 application 副本的主代码及全部测试源 `test-compile` 成功；当前 1.8 输出对应的两个生成 Service Mockito 测试各 26 项通过（合计 52）。生成的两组 Controller 测试已在隔离配置下执行，但均在测试方法前因完整上下文缺少运行期依赖而报错：application 副本缺少 `RoleServiceAdapter<?, SysPermission, SysMenu>`；再把测试放入临时 root 模块副本后，缺少 `IWorkflowProcessorResolver<IUser, ?>`。两组共 22 个测试方法未实际执行，不能记为运行通过。测试上下文强制 H2、未加载远程 profile 配置，Redis 仅指向 `127.0.0.1:1`；不再通过不断添加业务 stub 伪造完整应用上下文。
- 安全边界：当前 core 通过 API 关闭根实体结构同步，拦截记录只有 `SELECT DATABASE()`；旧 core 不具备该 API，触发的两条 `ALTER TABLE` 均被离线工厂拒绝并记录。所有验证均未连接真实业务数据库，菜单/模块更新关闭，application 原工作源文件未修改。临时生成归档和测试依赖仅在 `target\issue-0003-validation`，结束时精确清理。
- 影响与分级依据：中。新能力和 generator-core 本地测试已有证据，application 的同输入再生成、主/测试编译及 Service 测试已补齐；Controller 测试因消费方完整上下文依赖不齐仍缺运行通过证据。不把环境阻塞解释成生成器功能失败或已验收。
- 与当前任务的关系：本轮不重开 revision 问题；跨消费者交付证据作为独立后续项，不通过重录历史基线掩盖 generator-core-ISSUE-0001。
- 建议后续处理：由 application/root 模块维护方提供可复用的隔离 Controller 测试配置或完整测试依赖（H2、RoleServiceAdapter、IWorkflowProcessorResolver 等运行期 Bean 均明确本地化），再执行两组生成 Controller 测试；不得加载真实远程配置。要严格归因停用特性本身，还需从停用功能改动前的 1.8 源码构建 core，并与当前 1.8 对同一配置/DDL 再生成比较。不得触发目标库结构同步、菜单/权限更新或直接覆盖工作副本。
- 责任与关联：生成器维护方与 application/root 消费模块维护方；版本提交及基线来源由仓库负责人确认。
- 更新与关闭记录：2026-10-26 首次登记，中/源码已定位/待处理；2026-10-27 补齐 application 有效版本、core 1.7→1.8 同输入输出比较、主/测试编译和 52 项 Service 测试。Controller 测试 22 项因缺失 root 上下文 bean 未通过，ISSUE 保持部分完成、未关闭。2026-11-07 确认属跨仓库问题：缺失 `RoleServiceAdapter`、`IWorkflowProcessorResolver` 等运行期 Bean 需 application/root 模块维护方提供可复用隔离测试配置，超出生成器仓库范围，本次仅登记不处理。

### generator-core-ISSUE-0004 锁查询离线输出基线漂移（Excel 限额拒绝已修复）

- 发现来源：2026-10-26 扩大 Service 生成相关回归范围时发现，原记录包含锁查询基线漂移与 Excel 限额拒绝校验失效两个现象。
- 位置与证据：锁查询项为 `SelectForUpdateGenerationTest.offlineOutputBaseline`（第 85 行），断言「重构前真实输出基线：模型 0，层 3」期望摘要 `e611db88837710b8c92f58ffbe4cdfbf0a07b9abc4312fd94ab1730a9fdd6665`，最近一次记录的实际摘要为 `c7b11865021a59a04bc5e152773ef379210225632722e50e82b73a5b8213c4f7`（2026-10-28，17 项中 1 失败）。Excel 限额项位于 `ExcelImportWorkflowGenerationTest.rejectsEnabledWorkflowWithoutExplicitResourceLimits`；2026-10-29 已修正解析器对缺失限额使用默认值的问题，六项资源限制现要求显式提供并校验，`ExcelImportWorkflowGenerationTest` 全类 11 项通过。未连接数据库。
- 问题现象与触发条件：锁查询离线输出摘要与已记录的历史基线不一致；本轮未重新执行该非主线基线测试。Excel 限额遗漏不再复现；工作流启用时缺任一限额、限额非正或边界不一致均应被拒绝。
- 影响与分级依据：中。剩余问题限于历史离线输出基线漂移，不影响本轮两阶段导入资源限制验收；不得通过改写期望摘要或删除断言通过。
- 与当前任务的关系：Excel 限额校验缺陷与本轮显式两阶段工作流契约紧密相关，已通过解析器修正和 11 项回归覆盖解决；锁查询基线属于独立回归问题，本轮未改动或重验。
- 建议后续处理：独立安排后固定去敏输入与基线版本，核实锁查询摘要不匹配是否与 generator-core-ISSUE-0001 同源；说明真实差异后再决定修复或更新快照，不调整期望值掩盖问题。
- 责任与关联：生成器测试维护方，负责人待指定；锁查询余项仍待处理。
- 更新与关闭记录：2026-10-26 首次登记；2026-10-28 锁查询摘要仍不一致。2026-10-29 修复 Excel 限额解析和校验，`ExcelImportWorkflowGenerationTest` 11 项通过；该 ID 部分解决，锁查询基线余项仍待处理，未关闭。2026-11-07 定位根因：`SelectForUpdateGuardGenerator` 经 `getTargetPackageGen()` 把 guard 类落到 `codegen.dao` 子包（权威规范「codegen 生成区」+ `select-for-update-task.md` 已批准的设计），包名/类名较「重构前基线」漂移；与 ISSUE-0001（CAS/逻辑删除消费配置漂移）不同源。按负责人批准更新 `SelectForUpdateGenerationTest.offlineOutputBaseline` 三个 variant 的层 3 期望摘要为当前输出，问题关闭。

### generator-core-ISSUE-0005 消费方完整输出基线测试存在既有失败（前端产物重复键）

- 发现来源：2026-10-26 import 登记侧审计（`ImportScopeAuditTest`）扩大回归范围时发现；与 import 改动无关。
- 位置与证据：`CasConsumerBaselineTest.capturesCompleteConsumerOutputWithoutExternalWrites`（第 87 行）断言 `digests` 不含重复键失败，实际输出中 `PrivateTableColumnSlots.vue` 作为键出现两次（`assertThat(digests).doesNotContainKey("PrivateTableColumnSlots.vue")`）。改前对照：临时回退本次全部源码到 HEAD 后执行该测试，失败项与数量完全一致（1 项失败），证据 `mybatis-generator-core\target\surefire-reports`。未连接任何数据库。
- 问题现象与触发条件：直接执行 `CasConsumerBaselineTest` 即失败；疑似两个表配置各自生成同名前端片段文件且目标包相同，或同一文件被重复加入输出清单。Java/XML 产物的 import 内容与本失败无关。
- 影响与分级依据：中。属于既有回归的可重复性缺陷，不影响本次 import 交付；不得通过删除断言或改写基线通过。
- 与当前任务的关系：本次 import 改动的回归（`DisabledServiceEntryGenerationTest` 57 项、`SensitiveGenerationTest` 20 项含 1 项既有跳过、`CacheAwareBaseMethodsElementTest` 2 项、`DictPrefetchGenerationTest` 7 项、`ImportScopeAuditTest` 1 项）全部通过；本问题不通过修改断言、重录基线或放宽校验处理。
- 建议后续处理：独立批准后定位 `PrivateTableColumnSlots.vue` 的重复生成来源（表配置或前端元素装配），确认是应去重还是输出路径应区分，说明差异原因后方可调整。
- 责任与关联：生成器测试维护方，负责人待指定；当前任务只登记，不修复、不更新快照、不执行提交。
- 更新与关闭记录：2026-10-26 首次登记，中/已复现/待处理。2026-10-28 当前 CAS fixture 生成命令在生成阶段先因 TIMESTAMP 元数据类型不符合受管契约而失败，未执行到重复文件键断言；此前记录的重复键仍待独立复验。2026-11-07 定位根因：`GeneratedVueFile` 的 `targetPackage` 恒为空、两张管理表同名 `PrivateTableColumnSlots.vue` 的真实区分在 `targetProject` 尾部 modelPath（`sensitivecategory`/`sensitiveword`），测试 digests key 用 `targetPackage+fileName` 把两个不同文件误判为重复。按「输出路径应区分」结论修正测试 key 构造（targetPackage 为空时纳入 targetProject 尾部目录），问题关闭。

### generator-core-ISSUE-0006 CAS 消费者生成 fixture 的 TIMESTAMP 类型与受管契约不匹配

- 发现来源：2026-10-28 为 Sensitive SC05 运行任务书规定的隔离生成器与 Plus 工厂两步回归。
- 位置与证据：`src\test\java\org\mybatis\generator\codegen\mybatis3\CasConsumerBaselineTest.java` 的 `capturesCompleteConsumerOutputWithoutExternalWrites` 在 `generator.generate(...)` 期间失败；Surefire 错误为 `sensitive_category` 的 `created_` 列类型/可写性不受支持：`java.util.Date/TIMESTAMP`。同批 `CasWriteGenerationTest` 的 43 项通过；`GeneratedCasPlusTest` 随后因所需 `target\sc03-final-generated\output\META-INF\vgosoft\write-contracts\sensitive.xml` 未生成，在 Plus 测试入口断言失败。全程使用隔离生成配置和测试元数据，未连接业务数据库。
- 问题现象与触发条件：按已记录命令设置 `sc03.cas=true` 并执行消费者快照生成时，测试元数据解析出的审计时间 Java 类型与受管契约校验允许类型不一致，导致生成中止，无法提供真实生成文件给 Plus 组合测试。尚未证明是 metadata fixture 缺少类型覆盖、消费配置形状变化或契约校验问题。
- 影响与分级依据：中。阻断本次所需的真实生成文件组合回归，但不构成生产运行故障证据；可通过核对测试元数据、消费配置和契约类型规则后确认，且没有数据库副作用。
- 与当前任务的关系：SC05 的共享门禁手工 fixture 回归 152 项通过；真实生成 Plus 组合验收仍未完成。未修改测试、生成器配置或业务生成产物，也未用手工 fixture 替代生成输出。
- 建议后续处理：由生成器维护方核对固定消费配置的 TIMESTAMP Java 类型、离线 `DatabaseMetaData` 替身及 `CasWriteContractValidator` 的审计类型约束；确认唯一正确契约后，修复隔离 fixture 或实现并重新运行两步测试。不得放宽校验以掩盖不兼容，也不得连接业务数据库或覆盖 Sensitive 工作副本。
- 责任与关联：生成器测试/契约维护方；关联 Sensitive SC05/SC03 当前隔离验证。
- 更新与关闭记录：2026-10-28 首次登记，低/已复现/待处理。2026-10-29 完整运行 `SensitiveGenerationTest` 再次复现两项模板渲染错误；同批 5 项 Sensitive 写支持矩阵方法通过。未修改测试加载器或模板，本问题仍待单独处理。2026-11-07 定位根因：`CasConsumerBaselineTest.capturesCompleteConsumerOutputWithoutExternalWrites` 解析配置后遗漏真实 Maven 入口（`MyBatisGeneratorMojo`）本会调用的 `parser.customConfig(config)`，故 `useJSR310Types` 未生效、TIMESTAMP→`java.util.Date`，与父类 `AbstractPersistenceObjectEntity` 的 `LocalDateTime` 契约不符。修复为补 `customConfig` 调用（与真实生成链一致），同步调整 targetRuntime 断言；`sc03.cas=true` 与默认场景均通过，问题关闭。

### generator-core-ISSUE-0007 SensitiveGenerationTest 的 FreeMarker include 缺少模板加载器

- 发现来源：2026-10-28 执行 SC05 隔离生成器回归时，附带的 `SensitiveGenerationTest` 两项模板渲染用例失败。
- 位置与证据：`src\test\java\org\mybatis\generator\codegen\mybatis3\SensitiveGenerationTest.java` 的 `render`（第 662 行）创建包含 `sensitive-types` 的 FreeMarker `Template`；模板包含 `vue_module_type_imports.ts.ftl`，当前测试配置未能解析该 include。Surefire 中 `rendersEscapedFrontendMappingAndKeepsUnconfiguredTemplateBytes` 与 `offlineOutputBaseline` 均报 `TemplateNotFoundException`，提示 `template_loader` 未设置。
- 问题现象与触发条件：运行该测试类的相应模板用例时，模板处理遇到 include 即失败；本轮未修改模板、测试或生成配置。
- 影响与分级依据：低。仅确认测试辅助渲染路径无法完成，未发现生产生成或运行故障证据；问题可通过补齐测试模板加载配置并复验确认。
- 与当前任务的关系：该类的失败不解释 CAS Consumer 的 TIMESTAMP 契约错误，也不是形成 SC05 Plus fixture 的单独替代路径；按非主线只登记，不处理。
- 建议后续处理：由生成器测试维护方核对 include 所需的 classpath/file template loader，并以测试资源验证两项用例；不删除 include 断言或降低输出基线校验。
- 责任与关联：生成器测试维护方；无当前 Sensitive 业务实现依赖。
- 更新与关闭记录：2026-10-28 首次登记，低/已复现/待处理；本轮不修复。2026-11-07 定位根因：`render` 创建 FreeMarker `Configuration` 后未设 template loader，模板内 `#include "vue_module_type_imports.ts.ftl"` 无法解析。修复为设置 `ClassTemplateLoader(getClass(), "/templates/vue3")`，两项模板用例（`rendersEscapedFrontendMappingAndKeepsUnconfiguredTemplateBytes`、`offlineOutputBaseline` 层 2）恢复，问题关闭。

### generator-core-ISSUE-0008 根实体结构同步 DDL 失败仅告警且生成继续

- 发现来源：2026-10-29 检查 `ValidateDatabaseTable.executeUpdate()` 的异常处理与 Maven Mojo warning 处理，并运行既有隔离回归。
- 位置与证据：`mybatis-generator-core/src/main/java/org/mybatis/generator/custom/db/ValidateDatabaseTable.java` 捕获 DDL 执行的 `SQLException` 后仅加入 warning；`mybatis-generator-core/src/main/java/org/mybatis/generator/config/Context.java` 随后继续正常生成；`mybatis-generator-maven-plugin/src/main/java/org/mybatis/generator/maven/MyBatisGeneratorMojo.java` 将 warning 逐条记录为 WARN。`RootClassSchemaSynchronizationTest.keepsPrimaryKeyNotNullWhenAnotherMetadataChangeRequiresModify` 以拒绝 DDL 的 Statement 替身确认错误被记录，生成调用未因该异常终止。未连接真实数据库。
- 问题现象与触发条件：当根实体结构同步生成的 DDL 执行失败时，异常不会向生成入口传播；本次内存内省列也不会按失败操作更新，但后续生成仍可使用原内省元数据继续输出。Maven warning 不会单独使 Mojo 失败。
- 影响与分级依据：中。特定 DDL/权限/方言失败会使数据库结构与生成物契约不一致；通过检查 warning 和复核实际 schema 可规避，未证明发生过真实数据库数据损坏。
- 与当前任务的关系：结构同步的失败语义已纳入当前任务书及权威/配置说明；受控消费者生成后必须审阅全部结构同步 warning。当前仅获准检查和说明该行为，不改变生成失败契约。
- 建议后续处理：独立评审是否需要可配置 fail-fast 或失败后阻止生成的策略；若获批，补充异常/输出契约、DDL 拒绝回归及 Maven Mojo 验收。不得仅吞掉 warning 或把未完成结构同步报告为成功。
- 责任与关联：`mybatis-generator-core` 结构同步能力维护方；关联[根实体结构同步检查任务书](task/root-class-schema-sync-task.md)。
- 更新与关闭记录：2026-10-29 首次登记，中/已复现/待处理；本轮不修改代码。2026-11-07 经负责人确认，本次保持登记、不实现可配置 fail-fast 策略（该决策独立于本次台账处理，若需实现另立专项）。

### generator-core-ISSUE-0009 vgosoft-tool:7.0.0 的 VArrayUtil.merge 受检异常导致生成器无法全量编译

- 发现来源：2026-11-07 执行审计字段自动填充任务书 T08（生成器清理 getter 回退）时，`clean compile` 全量重编暴露。
- 位置与证据：`vgosoft-tool:7.0.0-SNAPSHOT` 的 `com.vgosoft.tool.core.VArrayUtil.merge(Collection, Collection, boolean)` 签名为 `throws InvocationTargetException, IllegalAccessException`（basic 已提交代码，本地 jar `vgosoft-tool-7.0.0-SNAPSHOT.jar` 为 2026-11-05 install）。生成器 `TableConfiguration.calculateReusableElementsConfiguration` 的五处调用位于当前源码 L577、L598、L603、L620、L631；直接调用由方法声明传播，lambda 内调用在各自位置转换为带中文上下文的 `IllegalStateException`，方法入口 `reprocessConfiguration`（L429-L430）统一包装传播出的受检异常。此前 `mvn clean compile` 因未处理受检异常失败。
- 问题现象与触发条件：全量重编（`clean compile`）即失败；`vgosoft-tool` 5.3.0/6.0.0 的 `merge` 无受检异常，7.0.0 起新增。生成器 dependencyManagement 未锁定 `vgosoft-tool` 版本，传递解析到 7.0.0-SNAPSHOT 后触发。
- 影响与分级依据：中。原问题阻塞生成器全量编译与后续 T12 回归；`merge` 反射异常在配置合并路径被转为运行时失败信号，不会静默跳过合并或形成成功输出。
- 与当前任务的关系：2026-11-07 需求负责人明确批准将 `TableConfiguration` 兼容修复纳入 T08，选择生成器适配受检异常契约，不调整 `vgosoft-tool` 依赖版本。修复覆盖全部五处调用，并随 T08 代码提交 `c1c0a318` 落地。
- 建议后续处理：无需后续处理。若 `VArrayUtil.merge` 的异常签名再次变化，按编译器诊断更新调用边界和上下文错误信息。
- 责任与关联：生成器维护方；关联 basic `vgosoft-tool` 模块。
- 更新与关闭记录：2026-11-07 首次登记，中/已复现/待处理；同日需求负责人批准纳入 T08，Copilot 独立检查五处调用与异常包装并确认通过；使用 JDK 17 执行 T08 `clean test`，`AuditFieldInitGenerationTest` 3 项通过且生成器模块完整编译通过，问题关闭。异常分支未通过故障注入做运行期复现，关闭依据为编译器契约验证与调用边界源码复核。

### generator-core-ISSUE-0010 `generateController` 缺少按接口简称排除 `@PreAuthorize` 的配置

- 发现来源：用户于 2026-11-12 提出，在生成器的 `generateController` 中增加 property 配置，按逗号分隔的接口简称（生成方法名）指定不生成权限过滤注解。
- 位置与证据：`mybatis-generator-core\src\main\java\org\mybatis\generator\config\xml\MyBatisGeneratorConfigurationParser.java:2461-2490` 解析 `generateController` 并处理通用 property；`mybatis-generator-core\src\main\java\org\mybatis\generator\codegen\mybatis3\controller\AbstractControllerElementGenerator.java:271-285` 的 `addSecurityPreAuthorize` 在启用 Spring Security 时按调用方提供的方法前缀生成 `@PreAuthorize("@uss.hasPermission(...)")`，当前没有按接口名称排除注解的配置。
- 问题现象与触发条件：启用 `integrateSpringSecurity` 时，调用 `addSecurityPreAuthorize` 的生成接口都会按既有规则添加权限注解；目前不能通过 `generateController` property 按“Controller 可生成接口清单”中的简称选择性省略该注解。
- 影响与分级依据：中。缺少按接口粒度配置权限注解的能力，消费者无法通过生成配置表达指定接口不使用该权限过滤；该配置会影响生成路由上的授权过滤边界，实际宿主访问策略仍须单独评估，因此登记为后续需求，不作为已确认的安全漏洞。
- 与当前任务的关系：本次只将用户提出的能力需求登记到台账，不修改解析器、DTD、Controller 生成逻辑或接口清单。
- 建议后续处理：在 `generateController` 下增加 property，值为逗号分隔的 Controller 生成方法简称；生成时将其与接口清单中的简称一致匹配，对命中接口省略 `@PreAuthorize`，未命中接口继续保持现有行为。后续实施时同步评估 DTD/property 解析、生成配置指南、权限 key 清单及覆盖默认行为的生成回归。
- 责任与关联：`mybatis-generator-core` 配置解析及 Controller 生成能力维护方；需求来源为用户，本次未获授权实施功能。
- 更新与关闭记录：2026-11-12 首次登记，中/源码已定位/待处理；当前仅记录需求，未实现或验证该能力。2026-11-20 按确认方案落地：接口权限改为默认不生成，`generateController` 新增 `apiPermissions` property 白名单（`all` 全量、逗号前缀、`relation` 保留值、动态 code 精确匹配），注解与权限 SQL 同生同灭；`PreAuthorize` import 按实际注解存在性引入；见[接口权限白名单任务书](task/api-permission-whitelist-task.md)，问题关闭。

### generator-core-ISSUE-0011 全量测试中既有基线漂移与 Excel 导入夹具失败

- 发现来源：2026-11-15 VgoTable 查询表单 W2（生成器组合条件）收尾时执行 `mvn -pl mybatis-generator-core test` 全量回归。
- 位置与证据：首次全量 679 项中 8 项失败；加入查询元数据兼容回归后复跑为 681 项中 8 项失败、5 项跳过：`CasWriteGenerationTest.retainsCompleteVersionedRawBaseline`（原始输出与 CAS 基线差异）、`SensitiveGenerationTest.offlineOutputBaseline`（场景 0 层 0 模型输出摘要不符）、`ImportScopeAuditTest.keepsEveryGeneratedImportReferencedWithoutGlobalFallback`（`SensitiveWordExcelVo.java` 存在未使用 import `com.vgosoft.core.annotation.Dict`/`DictEnum`）、`ExcelImportWorkflowGenerationTest` 1 失败 4 错误（`keepsOnlyExplicitWorkflowImportForSensitiveTables` 断言缺 `import/preview`、`import/confirm`；4 项报 `Sensitive word legacy Excel VO configuration was not found`）。对照证据：用 `git stash` 暂存 W2 改动后单独重跑这 4 个测试类，失败项与数量完全一致（77 项中 4 失败 4 错误）；本轮新增的 `ViewMetaAnnotationPluginTest` 与其他组合条件定向测试共 28 项全部通过。
- 问题现象与触发条件：直接执行上述测试类即失败；输出基线/导入审计/Excel 夹具的期望与当前生成输出不一致，非本次 queryColumn 组合条件改动引入。
- 影响与分级依据：中。属于既有回归漂移或夹具配置缺陷；不阻塞 W2 交付，但全量回归无法整体通过。生成 Example 新增的 `andBoundCondition`/`andLiteralLike`/`andTokenLike` 方法会使既有输出基线进一步漂移，后续修复基线时应一并重录。
- 与当前任务的关系：W2 组合条件及普通查询兼容定向测试 28 项全部通过；全量失败集中在既有 CAS/Sensitive 基线与 Excel 导入 fixture 路径。本轮不重录基线、不改断言、不修复 Excel 夹具。
- 建议后续处理：独立排查各基线漂移来源（可能是工作区其他未提交改动或上游未同步的生成逻辑变更）与 `SensitiveWordExcelVo` 无效 import、legacy Excel VO 夹具缺失配置；说明真实差异后再决定修复生成逻辑或更新快照。
- 责任与关联：`mybatis-generator-core` 测试维护方；关联 CAS/敏感信息/Excel 导入工作流相关生成能力。

### generator-core-ISSUE-0012 pageNavigator 默认值演进后测试断言未同步

- 发现来源：2026-11-16 执行[表单 Markdown 字段支持任务书](task/markdown-form-field-task.md) 回归时，`VueUiConventionAuditTest` 7 项中 1 项失败，与 markdown 改动无关。
- 位置与证据：`VueUiConventionAuditTest.pageNavigatorGeneratesMatchingEditAndDetailAnchorsWithComponentDefaults`（第 99 行起）断言 `width: 220`、`zIndex: 60`；当前 `PageNavigatorConfiguration` 默认值为 `width = 150`、`zIndex = 120`（源码第 28、44 行），生成产物输出 `width: 150`、`zIndex: 120`。断言期望落后于默认值演进，非生成缺陷。
- 问题现象与触发条件：直接执行该测试即失败；打印页/重复列等相邻断言不受影响。
- 影响与分级依据：低。属测试基线漂移，不影响生成行为；不得通过回退默认值掩盖，应将断言更新为当前既定默认（width 150、zIndex 120）并说明演进原因。
- 与当前任务的关系：markdown 字段相关两项定向测试（`markdownFieldEmitsMarkdownComponentInGeneratedVo`、`markdownFieldRejectedOnPrintPages`）均通过；本问题只登记，不改断言。
- 建议后续处理：把该用例的默认属性断言更新为 width 150 / zIndex 120，并与 pageNavigator 任务书 §4 中「edge 默认 8、滚动容器外预留」等后续演进记录对齐。
- 责任与关联：`mybatis-generator-core` 测试维护方；关联[页面导航器生成任务书](task/page-navigator-generation-task.md)。
- 更新与关闭记录：2026-11-16 首次登记，低/已复现/待处理；本轮只登记，不修改断言或默认值。
- 根因与修复（2026-11-15 关闭）：8 项失败分三类。①基线漂移（2 项）：已提交的 query-condition 组合条件特性将 Example 的 `andXxxAnyCondition` 改为 `andXxxRegexp`、实体 `anyWhereCondition` 描述由「任意过滤条件」改为「受限条件表达式…」、Controller `andAnyCondition` 改为 `andRegexp`/`andTrustedCondition` 并引入 `parseCondition`/`parseFilterMap` 列白名单，属合法变更，按批准方案重录 `consumer-current-v1-parser-baseline.zip`（16 个文件逐项核对均属上述变更）与 `SensitiveGenerationTest.offlineOutputBaseline` 四个场景×三层摘要（实体层与 TS 层变化，服务实现层不变）。②Excel 夹具（6 项）：`ExcelImportWorkflowGenerationTest` 与 `ImportScopeAuditTest` 通过 `consumerXml` 读取漂移的实时消费配置（`sensitive_word` 的 `excelVo` 由 `generate="false"` 改为 `generate="true" includeFields="…"`），改为固定快照 `consumerSnapshotXml` 消除配置漂移。③孤儿 import（生成逻辑缺陷）：`VoExcelGenerator` 经 `buildOverrideColumn` 为派生转换字段（如 `categoryText`/`strategyText`）登记 `@Dict`/`@DictEnum` 导入后，字段又被 `exportIncludeFields` 过滤掉，遗留孤儿 import；修复为仅在 include 过滤下构建会被导出的转换属性。全量复跑 684 项 0 失败 5 跳过通过。
- 后续约束：`DisabledServiceEntryGenerationTest` 与 `VueUiConventionAuditTest` 仍通过 `consumerXml` 读取实时消费配置，当前不失败但存在后续漂移风险，宜在后续统一固定到快照；`VoExcelGenerator` 孤儿 import 修复的确定性回归建议由既有 `ImportScopeAuditTest`（revision 场景含 `excelVo` 路径）覆盖，若消费配置再次关闭 `excelVo` 需另立固定夹具。
- 更新与关闭记录：2026-11-15 首次登记，中/已复现/待处理；同日全量复跑 681 项，8 项失败仍为原失败集合，新增兼容测试及组合条件定向测试 28 项全部通过。2026-11-15 定位根因并按批准方案修复：重录 CAS 原始输出基线、更新敏感离线基线摘要、将 Excel/Import 审计夹具固定到快照配置、修复 `VoExcelGenerator` 孤儿 import；全量 684 项 0 失败 5 跳过通过，问题关闭。

### generator-core-ISSUE-0013 Service Impl 输出演进导致 CAS 基线 3 个 Impl 文件与两个哈希基线的服务实现层漂移

- 发现来源：2026-11-20 执行[接口权限白名单任务书](task/api-permission-whitelist-task.md) 全量回归时，`mvn -o -pl mybatis-generator-core test` 722 项中 4 项失败，其中 3 项与本任务无关。
- 位置与证据：`CasWriteGenerationTest.retainsCompleteVersionedRawBaseline` 差异仅剩 `AbstractSensitiveCategoryImpl.java`、`AbstractSensitiveRuleRevisionImpl.java`、`AbstractSensitiveWordImpl.java` 三个 Service Impl 文件（权限 SQL 差异已按本任务批准的权限显示名方案对齐基线后消除）；`SelectForUpdateGenerationTest.offlineOutputBaseline` 场景 0 层 2（Service Impl）摘要不符；`SensitiveGenerationTest.offlineOutputBaseline` 场景 0 层 1（Service Impl）摘要不符。
- 问题现象与触发条件：直接执行上述测试即失败。差异内容为 Service Impl 输出演进：写操作异常由 `throw new VgoException(ServiceCodeEnum.FAIL.code(), e.getMessage())` 变为 `throw new VgoException(e.getMessage(), e).setCode(...)`（已合入提交 `5c98f5e9 fix(generator): 保留服务写操作异常原因链`，2026-11-19）、`Example` 包迁至 `entity.example.*` 子包、生成文件头注释更新。三个失败用例的 Service Impl 生成路径与本任务（Controller 接口权限白名单）无交集，且两个哈希用例的夹具 `generateController="false"`，可确证为既有漂移。
- 影响与分级依据：中。属既有基线/摘要漂移，不影响生成行为；但全量回归无法整体通过，且 CAS 基线 zip 与哈希摘要的更新均须先说明真实差异并经负责人确认（参照 generator-core-ISSUE-0004 关闭先例），本次未获授权，只登记不重录。
- 与当前任务的关系：本任务在 `CasWriteGenerationTest.parse` 注入 `apiPermissions=all` 保持 CAS 夹具的历史全量权限输出语义，并按批准的权限显示名方案对齐了基线 zip 中两个 `data-permission-*.sql` 条目；剩余 3 个 Impl 差异与上述两个哈希用例失败均属本项漂移。`retainsAllDisabledOutputsAndLimitsOptInDiff` 与本任务相关用例全部通过。
- 建议后续处理：独立核对三个 Impl 差异是否全部为 `5c98f5e9` 与 example 子包迁移/文件头演进的合法输出；确认后按批准范围重录 `consumer-current-v1-parser-baseline.zip` 对应 3 个条目及两个哈希基线的 Service Impl 层摘要，不回退生成逻辑掩盖。
- 责任与关联：`mybatis-generator-core` 测试维护方；与 generator-core-ISSUE-0001（消费配置漂移）不同源，与 generator-core-ISSUE-0004（锁查询基线历史漂移）同测试。
- 更新与关闭记录：2026-11-20 首次登记，中/已复现并归因/待处理；本轮只登记，不修改基线与断言。
