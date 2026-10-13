# 独立锁定查询生成扩展任务书

## 1. 归属、批准与范围

- 主体：`mybatis-generator-core`，执行 core `1.8.0-SNAPSHOT`，配置 DTD `mybatis-generator-config_1_8_0.dtd`。
- 批准依据：2026-10-14 用户明确批准 table `enableSelectForUpdate` 默认关闭的生成器扩展，要求完整 Mapper/XML/Service、非只读既有事务、缓存绕过、多方言预检与正式离线测试。
- 权威引用：[生成器权威说明](..\generator-authoritative-spec.md)、[配置指南](..\generator-config-guide.md)、[生成器指令](..\..\..\..\..\..\.github\copilot-instructions.md)。
- 过程引用：[全局过程规范](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\ai-assisted-development-standard.md)、[任务书规范](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\project-design-standard.md#81-多子任务执行与任务书管理)。
- 未发现本仓既有原子递增任务书；本任务不改变用户确认正常的原子递增功能，保留其工作树预存修改。Sensitive 过程资产由父任务维护，不复制此处通用契约。
- 禁止范围：业务产物手工修改、业务数据库连接、在线生成、DDL、权限、菜单及部署。

## 2. 批准方案与实现决策

1. 仅修改当前 DTD；Parser 严格接受小写 `true/false`。普通查询开关和模型规则决定独立 `selectByPrimaryKeyForUpdate`、`selectByExampleForUpdate`，不改普通查询。
2. 主键类、复合主键、BLOB 分层模型使用实际类型和结果映射。要求可变 MyBatis3/XMLMAPPER 及 Model/Mapper/XML/Service 全套生成；其他 runtime/模式、无主键却开启主键查询、全部查询关闭显式失败。
3. 已核对原生锁语法分组：MySQL/MariaDB/PostgreSQL/Oracle/H2 使用 `FOR UPDATE`；SQL Server/SQL Server 2005 使用 `WITH (UPDLOCK, HOLDLOCK)`。逐一预检 `additionalDbTypes` 并输出 `databaseId`。其他方言（包括 SQLite）拒绝生成通用锁查询；不借用受管关系依赖前置 INSERT 的 SQLite 特殊方案，不改已有关系行为。
4. Service 返回 `ServiceResult`，具体实现 `MANDATORY` / `rollbackFor=Exception`，不吞数据库异常，不加 Service 缓存注解。XML `useCache=false/flushCache=true`，同时绕过二级缓存及既有一级缓存。
5. 生成 DAO `codegen` 下独立 `Gen{Domain}MapperLockQueryGuard` 类；XML bind 与 Service 共用事务、read-only、PageHelper、Example 条件门禁。离线验证发现当前 MyBatis OGNL 不支持接口静态方法访问，已改用独立类并纳入真实 MyBatis 解析回归，而非降级忽略门禁。
6. Example 限制为非空有效条件、内省列标准操作符白名单；拒绝 DISTINCT、任意非空动态排序、原始 SQL、空集合条件。PageHelper 线程本地分页或排序状态显式拒绝，不自动清除/恢复调用方状态。
7. SQL 语法核对和真实隔离语义验收分离；不宣称跨方言 REPEATABLE READ 总能返回最新提交版本。

## 3. 进度与验收

整体状态：完成；进度：3/3；更新时间及完成时间：2026-10-14。

| 编号 | 内容 | 依赖 | 状态 | 验收与结果 |
|---|---|---|---|---|
| L01 | 当前 DTD/Parser、开关与配置预检 | 无 | 完成 | 默认关闭、true/false/非法值、全方言拒绝预检已通过离线用例；2026-10-14 |
| L02 | Mapper/XML/Service/门禁完整输出 | L01 | 完成 | 复合PK/主键类/BLOB、中文契约、原普通查询不变、Java编译、事务/分页门禁通过；2026-10-14 |
| L03 | 定向回归、权威/config文档与验收归档 | L02 | 完成 | 12项新增正式回归全部通过；邻近敏感/受管关系回归通过，独立compile及范围内diff检查通过；2026-10-14 |

## 4. 执行证据与运行边界

- 初次 compile 报 BLOB 类型 getter 大小写错误，已修正为实际 `getRecordWithBlobsType()`；后续 test 生命周期完成主源码与测试编译。
- 配置预检经定向用例确认 Client 实际类型来自 Context 而不是 table；已按生成流程真实来源校验。
- 当前正式测试 `SelectForUpdateGenerationTest` 人工构建元数据、JavaCompiler 内存编译与 MyBatis XML 解析，不连接任何数据库。
- 最终命令（工作目录 `D:\java\idea_projs14\generator\core`）：
  - `mvn -q -pl mybatis-generator-core "-Dtest=SelectForUpdateGenerationTest,SensitiveGenerationTest,ManagedRelationGenerationTest" "-Djava.io.tmpdir=D:\java\idea_projs14\generator\core\mybatis-generator-core\target" "-Denforcer.skip=true" test`：成功。锁查询 12/12 通过；敏感 13 项中 12 通过、1 项因未传业务配置路径跳过；受管关系 15 项中 14 通过、1 项因未传业务再生成路径跳过。合计 40 项、38 通过、2 跳过、0 失败/错误；没有传入消费者路径，没有执行业务再生成。
  - `mvn -q -pl mybatis-generator-core "-DskipTests" "-Denforcer.skip=true" compile`：成功。
  - `git --no-pager diff --check -- mybatis-generator-core`：成功，无空白错误。
- 新增测试还通过真实 Spring 代理/无资源事务管理器验证 MANDATORY、既有非只读事务正常返回及 read-only 拒绝；通过无数据库 MyBatis BaseExecutor/CachingExecutor 验证重复锁查询每次到达执行器且不写二级缓存；真实 PageInterceptor 在分页上下文中拒绝执行，零数据库调用。动态 SQL bind 对直接 Mapper 路径同样有效。
- 使用已安装依赖，未修改 POM、未执行 dependency restore；显式跳过仓库 Maven enforcer 门槛，非跳过编译/测试。新增生成源码和测试均为正式资产，未提交 git commit。
- 真实数据库锁竞争、死锁、超时、驱动、数据库/宿主事务隔离及其他自定义拦截器行为属于消费方集成验收，未执行且不得表述为已验证。
- 发布更新 core 后，消费者启用当前 DTD 的新开关并受控重新生成 Mapper/XML/Service/门禁类；不得仅手改业务 XML 或仅替换生成 Service。人工扩展文件仍遵守覆盖标记边界。

## 5. 清理登记

清理状态：完成。本任务未创建临时脚本、临时工具、数据库/业务产物或临时计划文档。内存编译结果随测试释放；最终回归将现有测试临时输出限定在仓内 `mybatis-generator-core\target`，JUnit 自动清理其作用域输出；`target` 是常规构建目录，不作为待提交资产。保留正式测试、本任务书及权威文档；不删除其他工作树改动。

## 6. 已批准保持行为的结构重构

- 2026-10-14 用户批准：将集中式跨层装配迁入既有 Mapper/XML/Service elements 与 Rules，保留配置、方法、门禁及 SQL 行为，不修改业务消费方。
- 整体状态：完成；进度：3/3；更新时间及完成时间：2026-10-14。
- R01：离线生成基线与职责拆分，完成（2026-10-14），重构前运行正式测试获取三种模型、五层共 15 个 SHA-256，重构后全部一致；R02：各层初始化执行接入、Rules 与插件兼容回归，完成（2026-10-14），15 项锁查询用例全部通过；R03：定向测试、compile、diff check 及架构文档归档，完成（2026-10-14），最终三套回归、独立 compile 及 diff check 再次通过，正式文档和本任务书已归档。
- 初次结构测试编译发现 Mapper 新方法插入位置位于既有方法体内，已修正；随后锁查询 13 项（原 12 项 + 基线）全部通过，未更改基线摘要。
- 仅运行人工元数据离线生成，不连接业务数据库、不执行消费方生成、DDL、部署、依赖安装/发布或 commit。基线摘要以正式测试固定，构建产物仅在仓内 target。

### 6.1 本次精确文件范围

以下清单相对开始重构时的工作树，而非相对 Git HEAD；既有锁查询实现及其他未提交修改不回退。Java 路径前缀为 `src\main\java\org\mybatis\generator\`，全部归属 `mybatis-generator-core`。

新增 10 个 Java 文件：

- `codegen\mybatis3\SelectForUpdateValidator.java`（现路径，第 7 节仅更名预检类）
- `dbstrategy\SelectForUpdateDialect.java`（现路径，第 7 节仅迁移共享策略包）
- `codegen\mybatis3\javamapper\SelectForUpdateGuardGenerator.java`
- `codegen\mybatis3\javamapper\elements\SelectByPrimaryKeyForUpdateMethodGenerator.java`
- `codegen\mybatis3\javamapper\elements\SelectByExampleForUpdateMethodGenerator.java`
- `codegen\mybatis3\xmlmapper\elements\AbstractSelectForUpdateElementGenerator.java`
- `codegen\mybatis3\xmlmapper\elements\SelectByPrimaryKeyForUpdateElementGenerator.java`
- `codegen\mybatis3\xmlmapper\elements\SelectByExampleForUpdateElementGenerator.java`
- `codegen\mybatis3\service\elements\SelectByPrimaryKeyForUpdateElement.java`
- `codegen\mybatis3\service\elements\SelectByExampleForUpdateElement.java`

删除/拆分 2 个原文件：

- `codegen\mybatis3\SelectForUpdateGeneration.java`：职责拆入配置预检、方言、门禁生成、ServiceMethods 和各层 elements。
- `codegen\mybatis3\xmlmapper\elements\SelectForUpdateElementGenerator.java`：XML 结构迁入共享抽象基类，主键与条件元素独立。

修改 10 个主源码文件：

- `api\AbstractIntrospectedTable.java`、`api\Plugin.java`、`api\CompositePlugin.java`
- `internal\rules\Rules.java`、`internal\rules\RulesDelegate.java`
- `codegen\mybatis3\javamapper\JavaMapperGenerator.java`
- `codegen\mybatis3\xmlmapper\XMLMapperGenerator.java`
- `codegen\mybatis3\service\JavaServiceGenerator.java`
- `codegen\mybatis3\service\JavaServiceImplGenerator.java`
- `codegen\mybatis3\service\ServiceMethods.java`

另修改 4 个正式资产：`src\test\java\org\mybatis\generator\codegen\mybatis3\SelectForUpdateGenerationTest.java`、`src\main\resources\docs\generator-authoritative-spec.md`、`src\main\resources\docs\generator-config-guide.md`、本任务书。DTD、Parser、POM、Sensitive 消费配置/产物和原子递增均未修改。

### 6.2 接入、兼容与一致性证据

- Mapper 的两个 `AbstractJavaMapperMethodGenerator`、XML 的两个元素及 Service 的两个 `AbstractServiceElementGenerator` 均由对应父生成器初始化 context/table/progress/warnings 后执行；Service 元素同时执行既有 `initGenerator`。接口统一从 `ServiceMethods` 获得实际模型、完整主键/Example 参数、返回与原中文契约。
- Rules 新增默认方法，Flat/Conditional/Hierarchical 及既有第三方实现无需新增抽象方法；RulesDelegate 尊重被代理的锁查询否决及当前代理对普通查询的限制。当前 table 的既有 setRules 接受 BaseRules，测试不为安装 RulesDelegate 而改变该 API，而是分别验证实际模型子类跨层接入与代理规则否决传递。
- 新增 4 个专用 select 插件回调，默认 `true`，由 CompositePlugin 顺序短路传播；不调用普通 select/update/batch 回调。真实元素入口测试覆盖两种方法、两个方言、否决顺序和旧插件默认兼容；完整契约仅在配置指南维护。
- 重构前真实执行 `mvn -q -pl mybatis-generator-core "-Dtest=SelectForUpdateGenerationTest#offlineOutputBaseline" "-Djava.io.tmpdir=D:\java\idea_projs14\generator\core\mybatis-generator-core\target" "-Denforcer.skip=true" test`：成功，采集单 PK、复合 PK、主键类+BLOB 三种模型的 Mapper、Service 接口、Service 实现、门禁与锁查询 XML，共 15 个 SHA-256。正式 `offlineOutputBaseline` 固定这些摘要，重构后全部相同，仅规范化 CRLF 为 LF；没有方法、SQL、注释、格式或方言输出顺序差异。
- 配置默认关闭、完整预检、不支持方言拒绝、MANDATORY/read-only、PageHelper/Example、XML 缓存属性及 guard 类名沿用原实现；原 12 项回归继续覆盖运行门禁、无数据库缓存执行器与 MyBatis 解析。

### 6.3 实际验证与边界

- 初次 Mapper 接入编译失败因方法插入到方法体内，已修正。新增测试编译随后发现测试方法插入位置及 table.setRules 的 BaseRules 限制，均已按现有 API 修正；未修改生产 Rules API 类型以迁就测试。
- 最终三套测试命令沿用第 4 节：锁查询 15/15，Sensitive 13 项中 12 通过、1 跳过，ManagedRelation 15 项中 14 通过、1 跳过；总 43 项、41 通过、2 跳过、0 失败/错误。跳过原因仍为未提供业务配置及业务再生成路径；未传消费方路径，未执行业务生成。
- 独立 `mvn -q -pl mybatis-generator-core "-DskipTests" "-Denforcer.skip=true" compile` 成功；`git --no-pager diff --check -- mybatis-generator-core` 成功。使用既有依赖，显式跳过 Maven enforcer 门槛，不跳过主源码编译或正式测试。
- 未覆盖：真实数据库锁竞争、死锁、超时、驱动/隔离级别、宿主多数据源路由及自定义拦截器；未运行全仓测试、业务数据库/生成/DDL/部署、依赖安装或发布、git commit。
- 清理：没有创建本次专用临时文件/脚本；基线为正式测试常量，验证报告留在常规 `target\surefire-reports`，不作为待提交资产。其他工作树改动保留。

## 7. 生成侧职责归属调整（2026-10-14）

- 用户已批准纯结构调整：生成侧锁查询预检更名为 Validator，锁语法共享策略移入 `dbstrategy`；敏感配置对象保留 `config`，独立内省后 Validator，仅字段解析机制跨层共享，实体、Service 和前端处理回归对应生成层。
- 依据：本任务书及 Sensitive 既有[功能实施任务书 W04](D:\java\idea_projs7\project-vgosoft\vgosoft-sensitive\src\main\resources\docs\task\sensitive-functional-implementation-task.md)。当前 Sensitive 业务自身不启用敏感检测；本轮不改消费配置或产物。
- 整体状态：完成；进度：3/3；更新时间及完成时间：2026-10-14。

| 编号 | 内容 | 依赖 | 状态 | 验收条件 |
|---|---|---|---|---|
| S01 | 采集本轮重构前完整离线输出摘要 | 无 | 完成 | 2026-10-14：重新运行锁查询 15 摘要；采集敏感四场景三层完整输出 12 摘要并固定于正式测试 |
| S02 | 按调用方拆分及正式结构/安全回归 | S01 | 完成 | 2026-10-14：27 个完整输出摘要一致；新增分层结构、真实插件顺序/变异/否决/关闭及真实 Vue3 入口回归均通过 |
| S03 | 文档引用更新、定向 test/compile/diff 与清理归档 | S02 | 完成 | 2026-10-14：48 项中 46 通过/2 授权边界跳过；独立 compile、最终 diff check、旧类移除和引用检索通过，清理完成 |

- 严禁 DB/DDL、消费方生成、依赖安装/发布、部署和 commit；保留开始时全部其他工作树改动，包括原子递增及既有锁查询 elements/Rules/hook 结构。
- 清理登记：不创建专用临时文件，摘要采集输出位于常规 `target\surefire-reports`，正式测试保留基线常量；不删除 `target` 或其他任务资产。
- 重构前采集命令：`mvn -q -pl mybatis-generator-core "-Dtest=SelectForUpdateGenerationTest#offlineOutputBaseline,SensitiveGenerationTest#offlineOutputBaseline" "-Djava.io.tmpdir=D:\java\idea_projs14\generator\core\mybatis-generator-core\target" "-Denforcer.skip=true" test`。最终采集两项通过；首次多字段样例误设未支持的内容类型，已改为既有 PLAIN_TEXT；重复采集发现默认实体日期注释不稳定，离线测试配置显式 suppressDate，随后重构前再次成功采集完整文本摘要，不删除或屏蔽其他输出内容。

### 7.1 精确文件范围与职责

清单相对本轮开始时的工作树；不是相对 Git HEAD。全部既有其他未提交修改保留。Java 路径前缀为 `mybatis-generator-core\src\main\java\org\mybatis\generator\`。

更名/迁移两个既有类（列当前有效路径，不保留旧源码或兼容空壳）：

- `codegen\mybatis3\SelectForUpdateValidator.java`：内省后生成能力预检，仅更名，仍在生成侧。
- `dbstrategy\SelectForUpdateDialect.java`：由生成侧共享类迁入方言策略包，类名及锁语法分组保持不变。

新增四个职责文件：

- `codegen\mybatis3\SensitiveFieldResolver.java`：唯一跨层共享的启用配置/最终字段解析机制，无模型、Service 或前端装配方法。
- `codegen\mybatis3\SensitiveGenerationValidator.java`：模型支持范围、父类字段及受管 Service 配置预检。
- `codegen\mybatis3\model\SensitiveModelAnnotations.java`：实际实体字段注解与模型插件否决保护。
- `codegen\mybatis3\service\elements\SensitiveWriteMethodsElement.java`：具体继承写方法、实际 Service 参数/代理/事务安全检查与注解、服务插件否决保护，沿用 `AbstractServiceElementGenerator`。

删除一个原跨层敏感装配源码，职责已归入上述四个文件和现有 Vue3 插件；没有空壳兼容入口。

修改十三个主源码入口：

- `api\AbstractIntrospectedTable.java`：更新两个内省后 Validator 入口，先敏感再锁查询的顺序不变。
- `codegen\mybatis3\javamapper\JavaMapperGenerator.java`
- `codegen\mybatis3\javamapper\elements\SelectByPrimaryKeyForUpdateMethodGenerator.java`
- `codegen\mybatis3\javamapper\elements\SelectByExampleForUpdateMethodGenerator.java`
- `codegen\mybatis3\xmlmapper\XMLMapperGenerator.java`
- `codegen\mybatis3\xmlmapper\elements\SelectByPrimaryKeyForUpdateElementGenerator.java`
- `codegen\mybatis3\xmlmapper\elements\SelectByExampleForUpdateElementGenerator.java`
- `codegen\mybatis3\xmlmapper\elements\AbstractSelectForUpdateElementGenerator.java`
- `codegen\mybatis3\service\elements\SelectByPrimaryKeyForUpdateElement.java`
- `codegen\mybatis3\service\elements\SelectByExampleForUpdateElement.java`
- `codegen\mybatis3\model\BaseRecordGenerator.java`
- `codegen\mybatis3\service\JavaServiceImplGenerator.java`
- `plugins\vue3\Vue3OtherFilesPlugin.java`

锁查询入口仅更改 Validator/方言引用，不改既有 PK/Example elements、Rules、ServiceMethods、hook 或 guard 结构。模型仍在插件回调之后装配敏感字段。Service 仍在关系注解/插件前补继承写方法，在插件通过后通过既有 elements 初始化/执行流程校验并标注；否决仍报原错误。前端元数据小方法直接归入现有 `Vue3OtherFilesPlugin.frontendFields`，未额外创建前端类或改模板。

修改两个正式测试：`mybatis-generator-core\src\test\java\org\mybatis\generator\codegen\mybatis3\SelectForUpdateGenerationTest.java`、同目录 `SensitiveGenerationTest.java`。

修改四个正式文档：`.github\generator-core-dev-spec.md`、`mybatis-generator-core\src\main\resources\docs\generator-authoritative-spec.md`、同目录 `generator-config-guide.md` 及本任务书。

本轮未修改配置类/Parser/DTD、Rules/hook/ServiceMethods、原子递增、ManagedRelation 实现或测试、POM、业务配置/产物；清单中已有改动文件仅追加本轮职责调整。

### 7.2 保持行为证据及最终验证

- 重构前/后完整内容基线：锁查询三种模型的 Mapper、Service 接口/实现、guard 与锁 XML 共 15 个 SHA-256；敏感默认启用、未配置、全部关闭、多字段 BLOCK/WARN 四场景的完整实体、受管 Service 实现与 TS 模板输出共 12 个 SHA-256。正式测试固定摘要，重构后 27/27 一致。仅在离线 fixture 中固定生成日期并规范化 CRLF 为 LF，不截取方法或 SQL 片段，不改业务配置。
- Sensitive 本轮新增 5 项正式测试：完整输出基线；分层结构与共享解析接口职责；真实模型/Service 插件顺序及字段 final、方法 final/错误参数/错误事务变异；真实插件否决及未配置/关闭绕过；真实 Vue3 生成入口的目标路径/VO/空字段门控及启用/未配置/关闭映射。失败文本用完整字符串验证，既有八种写方法、缓存/事件、父类、类型、双表示等回归继续执行。
- 新增 Vue3 fixture 首次失败因未配置 VO 子对象，随后发现字段描述器需要 Java 短类型名而非全限定名；均仅修正测试元数据，未为通过测试改变生产映射规则。
- 最终命令（工作目录 `D:\java\idea_projs14\generator\core`）：
  - `mvn -q -pl mybatis-generator-core "-Dtest=SelectForUpdateGenerationTest,SensitiveGenerationTest,ManagedRelationGenerationTest" "-Djava.io.tmpdir=D:\java\idea_projs14\generator\core\mybatis-generator-core\target" "-Denforcer.skip=true" test`：成功，锁查询 15/15，Sensitive 18 项中 17 通过/1 跳过，ManagedRelation 15 项中 14 通过/1 跳过；合计 48 项、46 通过、2 跳过、0 失败/错误。
  - `mvn -q -pl mybatis-generator-core "-DskipTests" "-Denforcer.skip=true" compile`：独立成功。
  - `git --no-pager diff --check -- .github\generator-core-dev-spec.md mybatis-generator-core`：成功。
- 跳过项：`parsesConsumerManagementConfigurationWithoutDatabaseOrGeneration` 未传 `sensitive.consumer.config`；`regeneratesSelectedBusinessOutputsOfflineWhenExplicitlyRequested` 未传业务再生成路径。不传业务路径、不执行业务再生成是本轮授权边界，并非测试失败；显式 enforcer.skip 仅绕过 Maven 环境版本门槛，不跳过编译或测试。
- 已检索本仓 Java/Markdown/XML/FTL 源码、测试与文档，无被替代类或旧方言包路径引用；规范、使用指南与架构入口均指向当前职责路径。
- 未覆盖：真实数据库锁竞争、隔离/死锁/超时、驱动、宿主多数据源及自定义拦截器、敏感运行时 Aspect/业务端到端、受控业务再生成与产物保护、全仓测试。新增测试为人工元数据离线生成/真实插件调用，不提供业务 DB/DDL/部署或生成授权。
- 清理：没有任务专用临时文件/脚本；真实 Vue3 用例仅在内存渲染，没有创建其测试目的路径 `mybatis-generator-core\target\sensitive-offline-unused`，最终存在性检查为 false；正式测试保留，报告在常规 target，不删除其他资产或 target 整体。未安装/发布依赖，未提交 commit，止于生成器职责重构。
