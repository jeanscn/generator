# mybatis-generator-core 待处理问题台账

> 所属项目：`generator/mybatis-generator`
> 模块/范围：`mybatis-generator-core`
> 规范：[非主线问题登记与后续处理规范](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\deferred-issue-management-standard.md)
> 更新时间：2026-10-28

## 1. 问题总览

| ID | 优先级 | 类别 | 标题 | 证据状态 | 处理状态 | 发现日期 | 关联任务/主记录 |
|---|---|---|---|---|---|---|---|
| generator-core-ISSUE-0001 | 中 | 其他 | CAS/逻辑删除回归依赖可变消费配置导致历史基线漂移 | 已复现并固定当前输入 | 已处理（当前快照版本化） | 2026-10-26 | [Service 停用任务书](D:\java\idea_projs7\project-vgosoft\vgosoft-sensitive\src\main\resources\docs\task\service-entry-disable-contract-task.md) |
| generator-core-ISSUE-0002 | 中 | 数据一致性 | 根实体结构同步可能为主键生成可空列变更 | 已修复并回归 | 已处理（主键强制 NOT NULL） | 2026-10-26 | [Service 停用任务书](D:\java\idea_projs7\project-vgosoft\vgosoft-sensitive\src\main\resources\docs\task\service-entry-disable-contract-task.md) |
| generator-core-ISSUE-0003 | 中 | 其他 | 未启用停用配置的业务消费者缺真实再生成编译测试证据 | core 1.7→1.8 同输入生成对照、消费编译及 Service 测试已验证；Controller 测试受隔离上下文依赖阻塞 | 部分完成，仍待处理 | 2026-10-26 | [遗留项核查](D:\java\idea_projs7\project-vgosoft\vgosoft-sensitive\src\main\resources\docs\task\service-entry-disable-contract-task.md#5-后续遗留项核查2026-10-26) |
| generator-core-ISSUE-0004 | 中 | 测试与基线 | 锁查询离线输出基线与 Excel 资源限额拒绝测试存在既有失败 | 已复现 | 待处理 | 2026-10-26 | 无（Service 停用 import 范围修改的验证旁路发现） |
| generator-core-ISSUE-0005 | 中 | 测试与基线 | 消费方完整输出基线测试存在既有失败（前端产物重复键） | 已复现 | 待处理 | 2026-10-26 | 无（import 登记侧审计的验证旁路发现） |
| generator-core-ISSUE-0006 | 中 | 测试与基线 | CAS 消费者生成 fixture 的 TIMESTAMP 类型与受管契约不匹配 | 已复现 | 待处理 | 2026-10-28 | Sensitive SC05 隔离生成回归阻塞 |
| generator-core-ISSUE-0007 | 低 | 测试与基线 | SensitiveGenerationTest 的 FreeMarker include 缺少模板加载器 | 已复现 | 待处理 | 2026-10-28 | 无（同批生成器回归中的非主线问题） |

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
- 更新与关闭记录：2026-10-26 首次登记，中/源码已定位/待处理；2026-10-27 补齐 application 有效版本、core 1.7→1.8 同输入输出比较、主/测试编译和 52 项 Service 测试。Controller 测试 22 项因缺失 root 上下文 bean 未通过，ISSUE 保持部分完成、未关闭。

### generator-core-ISSUE-0004 锁查询离线输出基线与 Excel 资源限额拒绝测试存在既有失败

- 发现来源：2026-10-26 修改 Service 实现类生成 import 范围后扩大回归范围时发现；与本次 import 改动无关。
- 位置与证据：`SelectForUpdateGenerationTest.offlineOutputBaseline`（第 85 行）断言「重构前真实输出基线：模型 0，层 3」期望摘要 `e611db88837710b8c92f58ffbe4cdfbf0a07b9abc4312fd94ab1730a9fdd6665`，实际 `c7b11865021a59a04bc5e152773ef379210225632722e50e82b73a5b8213c4f7`；`ExcelImportWorkflowGenerationTest.rejectsEnabledWorkflowWithoutExplicitResourceLimits`（第 106 行）期望抛出可抛对象而实际未抛出。改前对照：临时回退本次四份源码到 HEAD 后执行同两条测试，失败项与数量完全一致（`SelectForUpdateGenerationTest` 17 项 1 失败、`ExcelImportWorkflowGenerationTest` 8 项 1 失败），证据 `mybatis-generator-core\target\surefire-reports`。未连接任何数据库。
- 问题现象与触发条件：直接执行上述两个测试类即失败；离线内省与固定消费配置为输入。若 `SelectForUpdateGenerationTest` 同样读取消费仓当前 `generatorConfig.xml`，则与 generator-core-ISSUE-0001 同源；`ExcelImportWorkflowGenerationTest` 的资源限额拒绝路径是否为同一根因尚未核实。
- 影响与分级依据：中。属于历史/离线回归的可重复性缺陷，不影响本次 import 交付；不得通过改写期望摘要或删除断言通过。
- 与当前任务的关系：本次 import 改动的三组回归（`DisabledServiceEntryGenerationTest` 57 项、`SensitiveGenerationTest` 20 项含 1 项既有跳过、`CacheAwareBaseMethodsElementTest` 2 项）全部通过；本问题不通过修改断言、重录基线或放宽校验处理。
- 建议后续处理：独立批准后固定去敏输入与基线版本，分别核实两个失败是自基线漂移还是有真实行为回归；确认后再决定是修正生成器还是更新基线，并须说明差异原因后方可调整快照。
- 责任与关联：生成器测试维护方，负责人待指定；当前任务只登记，不修复、不更新快照、不执行提交。
- 更新与关闭记录：2026-10-26 首次登记，中/已复现/待处理；与 generator-core-ISSUE-0001 的归属关系待核实后决定是否合并。2026-10-28 按 Sensitive SC05 所需生成器 fixture 命令复验，`SelectForUpdateGenerationTest.offlineOutputBaseline` 仍为同一摘要不匹配（17 项中 1 失败）；未改写基线。

### generator-core-ISSUE-0005 消费方完整输出基线测试存在既有失败（前端产物重复键）

- 发现来源：2026-10-26 import 登记侧审计（`ImportScopeAuditTest`）扩大回归范围时发现；与 import 改动无关。
- 位置与证据：`CasConsumerBaselineTest.capturesCompleteConsumerOutputWithoutExternalWrites`（第 87 行）断言 `digests` 不含重复键失败，实际输出中 `PrivateTableColumnSlots.vue` 作为键出现两次（`assertThat(digests).doesNotContainKey("PrivateTableColumnSlots.vue")`）。改前对照：临时回退本次全部源码到 HEAD 后执行该测试，失败项与数量完全一致（1 项失败），证据 `mybatis-generator-core\target\surefire-reports`。未连接任何数据库。
- 问题现象与触发条件：直接执行 `CasConsumerBaselineTest` 即失败；疑似两个表配置各自生成同名前端片段文件且目标包相同，或同一文件被重复加入输出清单。Java/XML 产物的 import 内容与本失败无关。
- 影响与分级依据：中。属于既有回归的可重复性缺陷，不影响本次 import 交付；不得通过删除断言或改写基线通过。
- 与当前任务的关系：本次 import 改动的回归（`DisabledServiceEntryGenerationTest` 57 项、`SensitiveGenerationTest` 20 项含 1 项既有跳过、`CacheAwareBaseMethodsElementTest` 2 项、`DictPrefetchGenerationTest` 7 项、`ImportScopeAuditTest` 1 项）全部通过；本问题不通过修改断言、重录基线或放宽校验处理。
- 建议后续处理：独立批准后定位 `PrivateTableColumnSlots.vue` 的重复生成来源（表配置或前端元素装配），确认是应去重还是输出路径应区分，说明差异原因后方可调整。
- 责任与关联：生成器测试维护方，负责人待指定；当前任务只登记，不修复、不更新快照、不执行提交。
- 更新与关闭记录：2026-10-26 首次登记，中/已复现/待处理。2026-10-28 当前 CAS fixture 生成命令在生成阶段先因 TIMESTAMP 元数据类型不符合受管契约而失败，未执行到重复文件键断言；此前记录的重复键仍待独立复验。

### generator-core-ISSUE-0006 CAS 消费者生成 fixture 的 TIMESTAMP 类型与受管契约不匹配

- 发现来源：2026-10-28 为 Sensitive SC05 运行任务书规定的隔离生成器与 Plus 工厂两步回归。
- 位置与证据：`src\test\java\org\mybatis\generator\codegen\mybatis3\CasConsumerBaselineTest.java` 的 `capturesCompleteConsumerOutputWithoutExternalWrites` 在 `generator.generate(...)` 期间失败；Surefire 错误为 `sensitive_category` 的 `created_` 列类型/可写性不受支持：`java.util.Date/TIMESTAMP`。同批 `CasWriteGenerationTest` 的 43 项通过；`GeneratedCasPlusTest` 随后因所需 `target\sc03-final-generated\output\META-INF\vgosoft\write-contracts\sensitive.xml` 未生成，在 Plus 测试入口断言失败。全程使用隔离生成配置和测试元数据，未连接业务数据库。
- 问题现象与触发条件：按已记录命令设置 `sc03.cas=true` 并执行消费者快照生成时，测试元数据解析出的审计时间 Java 类型与受管契约校验允许类型不一致，导致生成中止，无法提供真实生成文件给 Plus 组合测试。尚未证明是 metadata fixture 缺少类型覆盖、消费配置形状变化或契约校验问题。
- 影响与分级依据：中。阻断本次所需的真实生成文件组合回归，但不构成生产运行故障证据；可通过核对测试元数据、消费配置和契约类型规则后确认，且没有数据库副作用。
- 与当前任务的关系：SC05 的共享门禁手工 fixture 回归 152 项通过；真实生成 Plus 组合验收仍未完成。未修改测试、生成器配置或业务生成产物，也未用手工 fixture 替代生成输出。
- 建议后续处理：由生成器维护方核对固定消费配置的 TIMESTAMP Java 类型、离线 `DatabaseMetaData` 替身及 `CasWriteContractValidator` 的审计类型约束；确认唯一正确契约后，修复隔离 fixture 或实现并重新运行两步测试。不得放宽校验以掩盖不兼容，也不得连接业务数据库或覆盖 Sensitive 工作副本。
- 责任与关联：生成器测试/契约维护方；关联 Sensitive SC05/SC03 当前隔离验证。
- 更新与关闭记录：2026-10-28 首次登记，中/已复现/待处理；本轮不修复、不重新生成业务产物、不安装发布。

### generator-core-ISSUE-0007 SensitiveGenerationTest 的 FreeMarker include 缺少模板加载器

- 发现来源：2026-10-28 执行 SC05 隔离生成器回归时，附带的 `SensitiveGenerationTest` 两项模板渲染用例失败。
- 位置与证据：`src\test\java\org\mybatis\generator\codegen\mybatis3\SensitiveGenerationTest.java` 的 `render`（第 662 行）创建包含 `sensitive-types` 的 FreeMarker `Template`；模板包含 `vue_module_type_imports.ts.ftl`，当前测试配置未能解析该 include。Surefire 中 `rendersEscapedFrontendMappingAndKeepsUnconfiguredTemplateBytes` 与 `offlineOutputBaseline` 均报 `TemplateNotFoundException`，提示 `template_loader` 未设置。
- 问题现象与触发条件：运行该测试类的相应模板用例时，模板处理遇到 include 即失败；本轮未修改模板、测试或生成配置。
- 影响与分级依据：低。仅确认测试辅助渲染路径无法完成，未发现生产生成或运行故障证据；问题可通过补齐测试模板加载配置并复验确认。
- 与当前任务的关系：该类的失败不解释 CAS Consumer 的 TIMESTAMP 契约错误，也不是形成 SC05 Plus fixture 的单独替代路径；按非主线只登记，不处理。
- 建议后续处理：由生成器测试维护方核对 include 所需的 classpath/file template loader，并以测试资源验证两项用例；不删除 include 断言或降低输出基线校验。
- 责任与关联：生成器测试维护方；无当前 Sensitive 业务实现依赖。
- 更新与关闭记录：2026-10-28 首次登记，低/已复现/待处理；本轮不修复。
