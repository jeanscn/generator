# 生成器权威文档（设计阶段）

本文档用于**基于生成器的新项目设计阶段**：回答“能做什么、适合做什么、边界在哪里”。
实现细节与具体 XML 写法请转到 `generator-config-guide.md`。

当前 1.8.0 经用户批准，将省略、null、空字符串及纯空白 `targetRuntime` 的默认运行时从 DynamicSql 改为 MyBatis3。这是全局生成兼容性变化，不仅影响锁查询；需要原动态 SQL 行为时必须显式指定 `MyBatis3DynamicSql`。显式四种内置运行时及自定义实现类继续由同一工厂解析，未知值仍失败。缺省解析入口为 `Context.getEffectiveTargetRuntime()`，验证与实际内省共用工厂；锁查询按实际 MyBatis3 内省实现与各项输出配置分别校验。版本、历史 DTD 保留边界及迁移说明见[配置指南第 2.1 节](generator-config-guide.md#21-默认运行时兼容性变更180)。

---

## 字典预取排序与共用查询（1.8.0 源码契约）

CachePo 开启即生成常规缓存方法和无缓存预取方法，接口及实现复用 `ServiceMethods` 签名；两个公开入口委托一个私有查询，不以显式排序配置决定方法存在性。SQL、CachePo 字段及预取注解共用 `DictCacheOrderResolver`：默认采用存在的 `sort_`、`created_`，显式保留原顺序，两者均补齐缺失的全部主键；列不存在或重复即失败。SQL 使用统一别名/标识符转义及 CASE NULLS FIRST + ASC，不改变其他查询默认排序。

注解自动声明字典 `keyProperty`、有效 `orderProperties`、全部主键 `uniqueOrderProperties`；无主键仍可生成并输出空数组，Web 再验证唯一依据及比较安全。CachePo 默认排序仅隐藏新增元数据，保留原业务字段/原主键 JSON 可见性；显式非 key/type/value 排序字段延续隐藏行为。配置、兼容影响及消费验证边界见[配置指南 §13.5](generator-config-guide.md#135-generatecachepo)，本次批准范围及结果见[任务书](task/dict-prefetch-order-contract-task.md)。离线渲染/编译不等同于消费者再生成、真实数据库或宿主验收。

**能力收益：** CachePo 启用后无需另配排序开关即可同时获得常规缓存查询和无缓存预取入口；共享私有查询实现减少两条入口的 SQL/映射行为分叉。默认或显式排序自动补齐主键，并将 SQL、CachePo 与注解元数据统一起来，为 Web 对多批查询结果作确定性合并提供稳定排序及唯一性依据；字典键不必是排序末项。此生成能力为请求级批量预取提供可消费契约，但不单独保证 Web 一定启用预取、任意列均可安全比较或取得固定性能提升，仍须通过消费者的类型/唯一性校验与宿主验收。

## 受管 CAS 生成能力（1.8.0，源码已实现，尚未发布）

table 属性 `enableCasWriteContract` 省略默认关闭，仅字面 `true`/`false` 合法，空白和大小写变体拒绝。它不控制宿主是否使用 MyBatis-Plus，也不改变 `integrateMybatisPlus` 的现有生成行为。仅承诺可变 MyBatis3/XMLMAPPER、已核验的继承 Integer version 模型、单字符串 id_ 主键及 mysql/h2；缺输出、缺列、错误类型、关系级联、生成键及未知方言明确失败。

启用表输出独立 `record + expectedVersion` 的完整/选择性主键 CAS Mapper/Service；新增固定 version=1，更新 version 自增并保持原 NULL 语义。旧无版本更新、CASE、Example、upsert、物理删除和原始 SQL 写入口显式拒绝；已有 insertBatch 是受控一条多值 SQL。显式配置的按列逻辑删除可增加单值 id/version 的 `LOGICAL_DELETE`，不放开旧物理删除；必须同步更新支持该操作的 basic 运行工件。服务异常传播以保留外层事务回滚，生产业务摘要/revision/墓碑不由通用生成器承担。

每个 context 聚合一份 `META-INF\vgosoft\write-contracts\<moduleKeyword>.xml`，协议为 basic 的 `managed-cas-1`；必需 Model、Mapper、XML、Service、已配置 VO/MapStruct 输出在插件回调之后再核验，否决或篡改不得部分成功。资源需要在原工厂显式加载，不生成生产自动扫描或审计装配。详细配置及验证入口见[配置指南：受管 CAS](generator-config-guide.md#受管-cas-输出180)；运行契约唯一来源为 [basic MyBatis 能力 §6.4](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-mybatis\src\main\resources\docs\vgosoft-mybatis-capability-overview.md#64-生成器受管-cas-契约managed-cas-1未发布)。

## 显式 Service 入口停用（1.8.0，源码契约）

`generateService/disabledMethod` 按无空格的完整擦除参数签名关闭公开入口；省略时不改变原有输出。服务与测试共用一个有效策略，复用 `ServiceMethods` 方法描述及真实父类泛型绑定，覆盖没有本地生成体的继承 CRUD。生成公开、非 final 的覆盖方法，立即抛出基础 `VgoServiceEntryDisabledException(signature, reason)`；拒绝方法不保留事务、缓存、敏感写及事件等方法注解。

只有显式配置 `internalMethod` 才复制该方法既有生成原语为 protected，并清除 `@Override` 及全部方法拦截注解；原 private final Mapper 字段保持不变，事务、缓存和事件边界由业务入口负责。禁止接口暴露、名称遮蔽及把 CAS 已拒绝入口重新开放。未配置时不产生内部旁路；继承方法没有可复制生成体时明确失败，不猜测其底层实现。未知、重复、不可覆盖或歧义签名均拒绝。既有 CAS 的默认 `IllegalStateException("ENTRY_DISABLED")` 不变；显式配置可增强为具备完整 operation/reason 的拒绝，不放开 Mapper 写契约。

生成测试返回完整签名到原因的映射，沿实际业务 Service 实例验证；基类已覆盖的入口不重复生成测试，其余使用非空有效参数补充测试。全部 context 插件结束后核验接口、拒绝体、内部原语、映射与测试输出，缺失、否决和篡改均失败。此能力不是授权拦截器，也不禁止已接管覆盖保护的业务扩展；实际业务重写后仍须运行生成测试验收。配置、支持边界及隔离回归入口见[配置指南：显式 Service 入口停用](generator-config-guide.md#显式-service-入口停用180)。

独立离线驱动可通过程序 API `Context.setRootClassSchemaSynchronizationEnabled(false)` 禁止内省后的根实体结构同步，不改变继承关系或 Service 输出；默认 true 保持既有正式入口行为，无新增 DTD 属性。`generate(..., false)` 本身仅禁止文件写盘，不能当作数据库只读保证；隔离步骤及元数据偏差排查仍见上述指南。

## 1. 文档定位

- **面向对象**：方案设计者、架构师、AI 生成代理
- **阶段定位**：需求分析 / 方案设计 / 能力评估
- **核心目标**：减少两类误判
  1) 本可配置生成，却误判成“必须二开”
  2) 配置无法覆盖，却误判成“可直接生成”

### 1.1 列标志内省契约（1.8.0）

列适配路径通过 core-db `Column.getIsGeneratedColumn()`、`getIsAutoIncrement()` 读取标志，不再反射查找不存在的 `getGeneratedColumn`/`getAutoIncrement`。所有方言采用相同布尔标志解释（YES/Y/TRUE/1 为真；缺失为 false），默认值仍独立保存；false 在缺失标志时仅表示内省未取得真值，不代表数据库能力保证。

MySQL、MariaDB、TiDB、PolarDB、TDSQL 的 JDBC 兜底也必须调用 core-db `JdbcUtils.getMySqlGeneratedColumns`，按结果集的实际 catalog/table 分组只读补充 EXTRA，并与适配路径共享识别规则，不能再次信任驱动把 `DEFAULT_GENERATED` 误报成 YES 的值。查询失败或实际列缺失显式抛出 SQLException；不允许以错误标志继续生成。其他方言保留 JDBC 标志语义。适配器失败后的部分结果清空后才开始 JDBC 兜底，避免重复列；兜底结果集异常时也会关闭。

无需变更配置或 DTD；须先更新本地/供应仓的 `vgosoft-core-db:7.0.0-SNAPSHOT`，再构建/安装 `mybatis-generator-core:1.8.0-SNAPSHOT`，核实 Maven 插件实际加载的依赖，而非只核对业务主依赖。此修复可能使旧版本被静默置为 false 的真实生成列/自增列恢复真值；不自动批准业务再生成。

正式回归入口：`internal.db.DatabaseIntrospectorColumnFlagsTest`（真实查询工厂/适配器及 JDBC 代理覆盖双路径、五种 MySQL 系方言、未知标志和异常），以及既有 `DatabaseIntrospectorTypeResolutionTest`、`DatabaseQueryMetadataAdapterTest`。元数据归一化权威语义见 [数据库规范 §1.1](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-core-db\src\main\resources\docs\database-authoritative-spec.md#11-列元数据标志契约)；离线结果不代替真实数据库、业务再生成或宿主验收。

---

## 2. 能力总览（What）

## 2.1 后端可生成能力

| 能力域 | 可直接生成内容 | 设计含义 |
|---|---|---|
| 基础数据访问 | Entity / PrimaryKey / Example / Mapper XML / Mapper 接口 | 标准 CRUD 与 Example 条件查询可直接落地 |
| 独立锁定查询 | table `enableSelectForUpdate` 默认关闭；生成独立主键/Example Mapper、XML、Service 与执行门禁 | 用于非只读既有事务中的先锁后改；不改变普通查询，不承诺跨数据库隔离级别的“最新读” |
| 应用层 | Service / ServiceImpl / Controller（含常用接口） | 列表、详情、新增、修改、删除、批量、导入导出等骨架统一 |
| DTO/VO | modelVo/createVo/updateVo/requestVo/viewVo/excelVo | 读写分离、查询入参、展示出参可统一建模 |
| 关系查询 | `selectByColumn` / `selectByTable` / `selectBySqlMethod` | 可覆盖按列、经中间表、多层级 SQL 方法查询 |
| 按列逻辑删除 | `selectByColumn` 的显式 `property isLogicalDelete=true` | 默认仍物理删除；MyBatis3/XMLMAPPER 下原子更新 delete_flag 与可选版本；受管 CAS 保持可信审计、参数隔离及事务门禁，业务墓碑/revision/引用规则由服务重写；配置与边界见[指南 §8.1](generator-config-guide.md#81-按列逻辑删除) |
| 统计 | `generateDateDimStatistics` / `generateCustomStatistics` | 常规统计口径可配置化 |
| 值转换 | `overridePropertyValue` / `columnRenderFun` | “存值-显文案”与展示态渲染可配置 |
| 敏感检测元数据 | table `sensitiveCheck`、实体字段注解、具体 Service 写方法/事务标记、TS 字段映射 | 当前 1.8.0 源码已实现并通过无数据库回归；只有 opt-in 表改变输出，检测运行时与业务受控再生成须独立接入/验收 |

敏感元数据使用基础 `com.vgosoft.core.annotation.sensitive` 注解，不反向依赖敏感业务模块。支持单实体可变 MyBatis3 模型的 String/LONGVARCHAR 字段、重命名和标准新增/主键更新/批量/upsert；受管 CAS 启用时敏感标记改落到真实 CAS 单行更新方法，旧 disabled 更新/upsert 不标记，若同时要求 update batch 则生成期拒绝。父类持有字段、分层模型、JSON/双表示、级联多实体和未受控 `updateByExample*` 明确失败，不静默漏检。SQL builder、直接 Mapper/JDBC、自调用与宿主 Aspect/事务顺序不属于生成自动保证范围。配置语法、实际方法矩阵和限制见[配置指南第 3.1 节](generator-config-guide.md#31-敏感字段与受管写方法180)。

配置对象 `config.SensitiveCheckConfiguration` 与生成侧 `codegen.mybatis3.SensitiveGenerationValidator` 独立；唯一跨层共享解析由 `codegen.mybatis3.SensitiveFieldResolver` 提供。实体字段查找、类型/可写属性验证、插件否决保护和注解去重归 `codegen.mybatis3.model.SensitiveModelAnnotations`；注解文本与 imports 由 `custom.annotations.SensitiveCheckDesc` 描述，且重复渲染无副作用。Service 方法/注解归 `codegen.mybatis3.service.elements.SensitiveWriteMethodsElement`：CAS 模式标记 `updateByPrimaryKeyCas`/`updateByPrimaryKeySelectiveCas` 的实体参数 0，并校验独立 `expectedVersion` 参数 1；CAS disabled 更新/upsert 不加标记，Sensitive+CAS+update batch 组合由 `SensitiveGenerationValidator` 拒绝。CAS 方法保留 MANDATORY 事务契约。前端映射归 `plugins.vue3.Vue3OtherFilesPlugin`（包前缀均为 `org.mybatis.generator`）。对应层仍在既有插件回调之后验证实际字段/方法，插件否决仍显式失败。

启用 `excelVo` 时，单列字符串主键表的 Controller 保留旧 `GET export?ids=...`，并生成 `GET export/options` 元数据及 `POST export` 个性化导出。POST 的 `query` 使用该表实际列表查询类型，复用 `buildExample` 与 `filterParam`；强制恢复权限/逻辑删除约束，清除客户端原始 SQL 条件及排序，仅允许内省列白名单排序并追加主键稳定排序。`all` 用 PageHelper 每批最多 500 行同步消费，不把全部结果集合驻留内存；`page/selected` 最多 500 个不同字符串主键，验证全部记录可见后按请求顺序或白名单排序导出，再截取 `maxRows`。PDF 上限 5000 行，其他格式 50000 行；格式、列选择和文件输出复用插件公共契约，不由生成器实现。无主键、复合或非字符串主键表跳过导出端点并输出生成警告。接入要求见[配置指南](generator-config-guide.md)第 5.1 节。

个性化导出通过生成列限制排除内省 BLOB/LONGVARCHAR 字段及配置的相关派生字段；元数据不广告未被 `selectByExample` 填充的大字段，客户端请求这些字段在查询前失败，不静默输出空值。

两阶段导入由 `<generateVo><excelImport generate="true">` 独立 opt-in，与 `excelVo` 的直接导入/导出能力解耦。生成器可生成 `<Entity>ExcelImportVo`、模板、带导入权限和日志声明的 `POST import/preview`、`POST import/confirm`，以及 Service 接口和业务策略钩子；两个端点均使用 multipart，确认请求同时携带重传文件和 JSON 预览凭证。预览基础实现使用 Excel 插件 `VgoFesodSheet.readBounded` 和配置的显式资源限额；确认 Controller 在调用事务 Service 前按相同限额重新解析文件，Service 与业务钩子接收凭证、有界行和文件 SHA-256。确认 Service 包装方法在事务中调用业务钩子，钩子负责验证文件/行摘要、消费预览凭证及整批业务写入。通用层不生成 token 生命周期、操作者/权限归属、revision 校验或 Entity upsert；钩子未实现时显式失败。启用两阶段流程且同时启用 `excelVo` 时保留导出、抑制旧逐行直接导入。配置约束、扩展方法及兼容边界见[配置指南第 5.1.1 节](generator-config-guide.md#511-显式两阶段-excel-导入工作流)。

`excelVo` 旧导入模板与 `excelImport` 两阶段模板共用表头元数据生成规则：`htmlElementDescriptor.tips` 优先作为表头批注，没有 `tips` 时物理列 remarks 作为说明回退。`dataSource="DictEnum"` 的固定枚举配置生成实际导入值下拉（实现 `IBaseEnum` 时取 `code()`，否则取枚举常量名）；动态字典、组织及用户等数据源不生成静态候选值。模板输出契约和配置说明见[配置指南第 5.1 节](generator-config-guide.md#51-excelvo-导入字段与生成行为)及[Excel 插件权威说明](D:\java\idea_projs6\vgosoft-plugins\vgosoft-excel\src\main\resources\docs\vgosoft-excel-authoritative-spec.md)。

共享按钮枚举提供 `WF_IMPORT`（主列表）和 `INNER_WF_IMPORT`（内嵌列表）作为两阶段导入操作的默认配置 ID。生成器可按既有按钮映射将其输出为按钮元数据；ID 本身不等于前端工作流交互已实现，宿主/业务前端仍需提供预览与确认处理。

### 2.1.1 实体物理表与列元数据注解

生成的 Entity 默认通过基础注解 `com.vgosoft.core.annotation.TableMeta` 和 `com.vgosoft.core.annotation.ColumnMeta` 暴露物理表/列描述，适用于设计和开发阶段从实体类型读取已生成的表映射、字段映射及部分数据库建模属性，避免仅凭 Java 类名或属性名猜测物理标识。`TableMetaAnnotationPlugin` 在模型类与模型字段生成回调中装配注解；`generateModel.noMetaAnnotation=true` 时不装配（默认关闭该抑制，即生成元数据注解）。实体字段有 `ColumnMeta`；该插件也会向生成 VO 字段添加 `ColumnMeta`，但类级 `TableMeta` 应以具体生成 Entity 为表身份入口。

**`TableMeta` 元素**

| 元素 | 元数据含义 | 当前生成来源/使用提示 |
|---|---|---|
| `value` | 物理表名 | `TableConfiguration.tableName`；是表名，不含独立 catalog/schema 字段 |
| `alias` | 表别名 | 当前 table 配置别名 |
| `descript` | 表描述 | 当前内省表备注 |
| `beanname` | 生成的 Controller Bean 名称 | 生成器解析的 Controller Bean 名称，不是数据库对象标识 |
| `summary` | 表级摘要标志 | 当前描述器默认为 `true`；不能用它推断表是否参与某个查询或输出 |
| `voClass` | 生成 VO 的全限定名 | VO 类型存在时写入 |
| `superClass`、`superInterfaces` | 实际生成模型的父类/接口 | 反映 Java 模型继承结构，不是数据库继承或表关系 |

**`ColumnMeta` 元素**

| 元素 | 元数据含义 | 当前生成来源/使用提示 |
|---|---|---|
| `value` | 物理列名 | 内省列的实际列名；`Field.getName()` 是相应 Java property |
| `description` | 短字段说明 | 来自列备注；缺少说明时生成器回退到物理列名 |
| `remarks` | 完整列备注 | 来自内省列完整备注 |
| `pkid` | 是否主键 | 来自当前内省列的主键判定 |
| `type` | JDBC 类型 | 来自内省列 JDBC 类型；未显式写出的属性由注解默认值补足 |
| `nullable` | 是否允许 NULL | 来自内省列 nullable |
| `size`、`scale` | 列长度与小数位数 | 来自内省列长度、scale |
| `order` | 生成的列顺序元数据 | 来自内省列顺序；相同或默认顺序时不得假定其单独构成稳定排序键 |
| `dataFormat` | 日期显示格式提示 | 来自内省列日期格式配置 |
| `summary`、`sort` | 摘要/排序提示 | 当前描述器默认均为 `true`；不代表查询授权或 SQL 排序已经启用 |
| `charSet`、`defaultValue`、`position` | 字符集、默认值、DDL 位置描述 | 当前生成描述器固定为 `utf8mb4`、空默认值、空位置；不能据此断定数据库实际字符集、默认表达式或列位置 |

注解定义及具体默认值以基础包 `vgosoft-core-annotation` 中的 `TableMeta`、`ColumnMeta` 源码为准；生成器映射以 `TableMetaAnnotationPlugin`、`TableMetaDesc`、`ColumnMetaDesc` 为准。反射读取建议如下：

1. 使用明确的具体 Entity 类型；读取该类声明的 `TableMeta`，以 `value` 确认物理表名，以 `alias` 等字段取得生成元数据。`TableMeta` 带 `@Inherited`，若使用继承查找，须确认继承到的表身份确实适用于该类。
2. 从具体 Entity 起逐层遍历 `getDeclaredFields()` 至 `Object`，只收集实际带 `ColumnMeta` 的字段；字段名作为 Java property，`ColumnMeta.value()` 作为物理列名。字段注解上的 `@Inherited` 不会使父类字段注解自动复制到子类，因此不能只扫描子类声明字段，也不能依赖 `Class.getFields()`。
3. 如需确定顺序，可按 `ColumnMeta.order()` 排序，并使用物理列名/property 作稳定 tie-break；应按业务目的区分字段展示顺序与 SQL/数据库物理顺序。
4. 读取注解前检查缺失、重复物理表/列映射和映射完整性；对受支持能力无法确认的输入明确失败或回退到正式生成配置/数据库权威元数据，不静默猜测。

这些注解是由内省结果及生成设置形成的**模型元数据快照**，适合设计盘点、实体到表/列映射、属性说明展示，以及可由实体反射驱动的字段级逻辑。它们不是完整 `generatorConfig.xml` 的序列化，也不是数据库 schema 的完整镜像：不含 catalog/schema、索引与唯一约束、外键、identity/generated-always、所有生成开关、SQL statement/databaseId、插件改写结果、权限或 Mapper 写入白/黑名单。某些元素可能采用描述器默认值，而非从数据库或配置显式读取。因此：

- 需要回答“这个 Entity/属性对应哪个表/物理列、有哪些已记录的列描述”时，可优先从类/字段注解读取。
- 需要决定输出哪些 Mapper/Service/页面、具体写 SQL、受管 CAS/逻辑删除或 statement 是否允许执行时，仍须读取实际生成配置、生成产物及其运行契约。
- 需要判断数据库真实 DDL、约束、默认表达式或目标环境状态时，仍以数据库权威规范、迁移/DDL 和获准的实际数据库核验为准，不能仅凭注解下结论。

已有运行时复用实例：`vgosoft-mybatis` 的 `AbstractMybatisBGService` 在查询构建器未显式指定表/字段时，从 Entity 的 `TableMeta.value/alias` 和 Entity/父类的 `ColumnMeta` 取得默认表及字段列表。这证明元数据可支撑按 Entity 描述构建基础表列映射；它不表示注解可生成或验证任意 SQL。

注解也不是 `ViewTableMeta`/`ViewColumnMeta` 的替代品：页面列可见性、编辑、过滤、排序和布局等视图契约读取对应 View VO 的视图注解及配置。

## 2.2 Vue3 前端可生成能力

| 能力域 | 可直接生成内容 | 设计含义 |
|---|---|---|
| 页面骨架 | 列表页、编辑页、详情页、弹窗页 | Vue3 模板使用 Framework 的 `VgoTableView` / `vgoForm`；结构化业务页可先生成骨架 |
| 列表视图元数据 | `viewVo` 生成的 Java VO 与视图注解元数据：列表标识、查询/过滤字段、默认排序、列显示及工具栏等 | Vue3 列表依赖 ViewVo 配置和宿主运行时；元数据生成不等同于页面布局、服务端过滤或按钮权限实现 |
| 表单视图元数据 | 新增/编辑/只读表单的 VO 类型与配置化表单装配 | 编辑/详情模板通过 Framework 表单配置能力装配；复杂联动仍须评估扩展点 |
| 复杂部件 | 内嵌列表相关接入、附件/审批相关模板能力 | 可生成主干或提供接入位置，不表示任意业务交互均自动完成 |
| 类型与扩展 | TypeScript 类型、共享变量、Hooks 和列表列插槽文件 | 部分文件是扩展入口；其生成条件与覆盖策略并不相同，见下文 |

### 2.2.1 `viewVo` 视图元数据

当前 1.8.0 配置解析中，`viewVo` 默认 `generate=false`，并且需要父级 `generateVo` 启用才会参与生成。启用后生成 ViewVo Java 类及 `ViewMetaAnnotationPlugin` 添加的字段/类视图元数据；列表模板将其作为运行时视图信息来源之一。它不是 Vue 页面布局描述，也不自动实现自定义查询、服务端业务操作或权限控制。

生成的 `@ViewTableMeta.value` 默认由 `Mb3GenUtil.getDefaultViewId(...)` 计算，即 controller bean 名与默认视图后缀拼接后转小写并取 `MD5_15`。XML `viewVo.listKey` 与该默认 ID 不同；当前 `ViewMetaAnnotationPlugin` 不会将 `listKey`、`title` 或 `size` 传入默认 `ViewTableMeta`，不可将 DTD 注释对 listKey 的旧描述误作 `value` 的算法或假定这些属性已进入默认视图注解。`title` 会作为生成菜单标题，未配置时回退到表备注。视图数据 URL 由生成器按 controller 基础路由构造为 `{controllerBasePath}/getdtdata`；实际路由、数据查询和元数据消费仍由宿主运行时提供。

`viewVo` 的配置能力包括：

| 能力 | 配置来源 | 设计边界 |
|---|---|---|
| 列表字段与列元数据 | `includeColumns` / `excludeColumns`、`defaultDisplayFields` / `defaultHiddenFields`、`fieldOverrides`、`overridePropertyValue`、`columnRenderFun` | 字段范围还受父级 VO 与公共抽象 VO 字段规则影响；`defaultDisplayFields` 以 ViewVo 属性名控制列顺序/默认显示；显式检查生成的 ViewVo 字段及列注解 |
| 复合查询与过滤 | `queryColumns`、`fuzzyColumns`、`filterColumns`、子节点 `queryColumn` / `filterColumn` | 生成查询/过滤描述，不替代 RequestVo 字段、查询接口及服务端条件实现；显式 query/filter 列配置应与字段集合对齐。无显式 query 描述时，不存在的 query 列会被忽略；不存在的 filter 列会抛出异常 |
| 工具栏与行操作 | `toolbar`、`actionColumn`、子节点 `htmlButton` | 能配置标准动作或自定义按钮元数据；自定义 handler、服务端动作和权限由宿主/业务提供，前端显示条件不是授权边界 |
| 表格及展示行为 | `size`、`indexColumn`、固定列、默认排序、总计、`tableType`、`uiFrameType`、分类树及表单展示模式等 | 以当前 DTD 与解析/消费链路为准；并非每个属性都由 `@ViewTableMeta` 或所有前端表格实现消费 |
| 菜单期初数据 | ViewVo 标题、图标、`enableShortcut`、table/context 菜单设置 | 只有 ViewVo 本次实际生成且插件回调允许、Context `updateMenuData=true`、table `isModules=true`、有效 `parentMenuId` 非空时才收集默认视图菜单 INSERT；ViewVo 父 ID 优先，否则回退到 Context，无隐式父菜单默认值。两项布尔开关缺省均为 `false`；已有 ViewVo 被生成策略跳过时不会因其已存在而收集菜单行。 |
| 附加视图结构 | `additionalProperty`、`innerListView` 及其他 DTD 允许的 `viewVo` 子节点 | 按实际插件和模板验证各子配置的效果；配置存在不代表跨宿主统一的交互承诺 |

DTD/Parser 接受属性并不代表它会进入默认 `@ViewTableMeta`。当前插件显式传递 `tableType`、合计行、表单打开方式、`showActionColumn`、`defaultSort`、`indexColumn`、查询/过滤及按钮描述、`categoryTreeUrl`、包含/排除列、类名和路由等；列默认显示及顺序写入字段级 `@ViewColumnMeta`。例如 `listKey`、`title`、`size`、`enablePager`、固定列属性和 `showRowNumber` 并未由该插件传入默认视图注解；其中 `title` 另用于菜单。详见配置指南第 5.2 节的版本限制与源码入口。

版本注意：当前 DTD 对 `viewVo.enablePager` 接受 `true|false|simple|auto`，但当前 Parser 将其交给 `Boolean.parseBoolean`，因此只有文本 `true` 会解析为真，`simple` / `auto` 实际解析为假。`enablePager` 也不是 `ViewTableMeta` 注解成员，不应把它描述为 Vue3 运行时分页模式配置。依赖分页显示模式的项目须先核对实际模板/宿主契约；若要改变通用生成语义，应修正并测试生成器，而不是依赖 DTD 允许值推测效果。

菜单数据收集后，仅在数据集合非空且 `updateMenuData=true` 时输出 `src/main/resources/sql/init/data-menu-<moduleKeyword小写>.sql`；该产物只是 SQL，不创建父菜单、不写数据库，也不代表 Spring 已执行。局部生成可能使脚本不完整，必须核对最终部署资源集。配置示例与条件检查见[配置指南第 5.2.5 节](generator-config-guide.md#525-菜单期初数据生成)；涵盖 DDL、权限、菜单、存储过程等资源的首次 Spring 初始化步骤和 AI / 开发人员验收要求见[配置指南第 19 节](generator-config-guide.md#19-spring-首次初始化)。Context 菜单开关见配置指南第 2 节；项目级菜单构建规范另见[菜单构建规范](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\project-menu-construction-standard.md)。

### Vue3 产物与生成条件（当前 1.8.0 源码）

Vue3 产物由 `Vue3OtherFilesPlugin` 与 `Vue3FormFilesPlugin` 输出。共同门控是 Context 配置了非空 `vueEndProjectPath`；输出根目录为 `{vueEndProjectPath}/src/modules/{moduleKeyword}`。`generateHtml` 的 `type="editable"` / `type="viewOnly"`、各 VO 开关和只读/编辑规则共同决定页面产物。被识别为 model-only 的表不生成 Vue3 编辑/只读页面。

| 默认产物 | 源码生成条件 |
|---|---|
| `views/{Domain}.vue` | editable 页面规则成立，且 `viewVo` 已启用 |
| `{domainLower}/components/{Domain}Edit.vue` | editable 页面规则成立 |
| `{domainLower}/components/{Domain}Detail.vue` | editable 或 viewOnly 页面规则成立 |
| `modals/{Domain}Modal.vue` | editable 页面规则成立 |
| `{domainLower}/types/T{Domain}.ts` | editable 页面规则或任一 VO 规则成立，且收集到的 VO 字段非空 |
| `{domainLower}/PrivateUseFormHooks.ts`、`sharedVariables.ts` | editable 页面规则成立 |
| `{domainLower}/PrivateTableColumnSlots.vue` | editable 页面规则成立，且 `viewVo` 已启用 |

此处的 editable 页面规则表示至少有一个 `generateHtml generate="true" type="editable"` 配置；viewOnly 规则同理由 `type="viewOnly"` 配置触发。Vue3 列表不是只读页面开关的同义产物：列表还要求 `viewVo`。因此，仅配置 `vueEndProjectPath` 不会使所有页面都生成；仅启用 `viewVo` 也不保证生成 Vue3 页面。

`generateHtml` 同时参与 HTML 输出与 Vue 表单元数据生成，不能将其所有子配置归为“仅 HTML”。`VueHtmMetaAnnotationPlugin` 会将默认页面的布局、字段控件和附加组件配置写入 Java VO 的 `VueForm*Meta` 注解；后端配置服务再将其交给 Framework 表单。Vue3 列表则主要依赖 ViewVo/运行时表格配置。HTML 文件路径/覆盖、旧 HTML 页面打开方式及打印设置，不是 Vue3 文件路径/覆盖或定制模板开关。

### 2.2.2 页面配置的设计边界与阅读入口

属性细表统一维护于[配置指南第 7 章](generator-config-guide.md#7-页面生成)，采用“属性、含义、默认值或值来源、影响、备注”五列。影响区分 vue3、html、vue3和html；vue3 包含 VO 注解的间接影响，不保证各宿主组件等效实现。设计阶段按下表定向阅读，不重复维护另一套默认值表。

| 设计问题 | 配置入口 | 能力与边界 |
|---|---|---|
| 启用哪类页面、选哪个共用表单配置、输出到哪里 | [7.1 通用配置](generator-config-guide.md#71-通用配置) | 文件门控检查整表启用的 editable/viewOnly；表单注解却取第一个 default=true 的配置（全为 false 时取第一个），不按 generate/type 过滤。default 实际省略为 true，不能把 generate=false 当元数据总开关。 |
| 字段列数、独占行、容器、标签及弹窗 | [7.2 页面布局](generator-config-guide.md#72-页面布局) | 字段 span 通常为 24/pageColumnsNum，独占列为 24，长文本/编辑器还可调整；容器由嵌套 XML 计算父键。groupContainer.columnNum 当前默认 1；HTML uiFrameType 当前默认 layui，并非选择 Vue3 技术栈的开关。 |
| 控件、字典、引用选择、联动与自定义表单 | [7.3 输入组件配置](generator-config-guide.md#73-输入组件配置) | 普通描述器、TableSelect、FormExtend 可参与 Vue 字段注解；快捷格式/默认列规则会计算数据来源。页面直接子节点 htmlSwitchDescriptor 当前未解析，应使用普通 descriptor 的 tagType=switch。 |
| 校验、字段链接与动作 | [7.4 校验、链接与按钮](generator-config-guide.md#74-校验链接与按钮) | Vue validator 与 HTML verify/hrefElement 不等效；动作元数据不自动实现业务 handler。当前 validator.max/enum 等存在传值偏差，设计不能依赖声明即生效。 |
| 附件与审批意见 | [7.5 附件与审批意见](generator-config-guide.md#75-附件与审批意见) | 可生成组件元数据，不自动实现存储/审批业务；Vue 路径未以子项 generate 过滤，附件 order/隐藏条件等也未完整传递。 |
| 子列表加载、编辑、汇总和打印 | [7.6 内嵌列表与打印](generator-config-guide.md#76-内嵌列表与打印) | 依赖来源 VO、关系字段和实际服务；dataField/dataUrl 的选择不由 Parser 强制校验。restBasePath 当前存在重算偏差，部分交互属性未传递；HTML 打印不等于 Vue3 打印。 |
| Vue 文件条件、接入、覆盖与 HTML 附带输出 | [7.7 Vue3 页面生成、接入与覆盖边界](generator-config-guide.md#77-vue3-页面生成接入与覆盖边界) | Vue 文件使用独立模板与覆盖策略；启用 generateHtml 还需核对 HTML/静态资源输出范围。 |
| 后续如何补充/核验文档 | [7.8 源码核对入口与补充规则](generator-config-guide.md#78-源码核对入口与补充规则) | 分别核对 DTD、解析、计算、传递和宿主消费；DTD 注释、配置对象初值、basic 注解缺省和前端运行时缺省不得混写。 |

以上偏差是当前源码边界，不是本次新增能力或修复承诺。需要依赖这些行为的项目，应先评估生成器修正及回归，或选择受保护的业务扩展点；不能直接修改持续覆盖的生成物绕过。

Vue3 生成模板与宿主 Framework 的复用依据见 mpmm-ui 的[Framework 能力总览](D:\vscode\mpmm-ui\packages\framework\docs\framework-capability-overview.md)和[组件开发与复用能力指南](D:\vscode\mpmm-ui\packages\framework\docs\components-reuse-capability-overview.md)。mpmm-ui 的[OrgRole 列表页](D:\vscode\mpmm-ui\src\modules\organization\views\OrgRole.vue)、[编辑组件](D:\vscode\mpmm-ui\src\modules\organization\orgrole\components\OrgRoleEdit.vue)、[表单 Hooks](D:\vscode\mpmm-ui\src\modules\organization\orgrole\PrivateUseFormHooks.ts)及[列表列插槽](D:\vscode\mpmm-ui\src\modules\organization\orgrole\PrivateTableColumnSlots.vue)仅是宿主接入示例，不是生成器对所有项目的页面实现承诺。

### Vue3 文件覆盖边界

在当前 Vue3 插件源码中：

| 覆盖策略 | 文件 |
|---|---|
| 已存在时检查前 10 行的完整 `@VCF.overridable` 标记；有标记才允许重写 | 列表页、编辑组件、详情组件、Hooks 文件、列插槽文件 |
| 插件显式允许重写，不以该标记保护已有文件 | TypeScript 类型文件、Modal 文件、`sharedVariables.ts` |

具体行为还受 MyBatis Generator 的写盘回调及目标文件是否存在影响。不要把所有 Vue 生成物都称为可扩展/可覆盖文件；业务人工维护前须逐文件核对生成器项目指令中的产物保护约定。`The content below is generated by vgosoft's code factory.` 等来源说明本身不是覆盖开关。扩展约定见 [generator-config-guide.md 页面生成](generator-config-guide.md#7-页面生成)及本项目 `.github/copilot-instructions.md` 第 2.1 节。

## 2.3 多方言能力

| 能力 | 机制 | 设计要求 |
|---|---|---|
| 一套配置多方言生成 | 基于 `additionalDbTypes` 循环生成 `databaseId` 分支 SQL | 不能只按当前连接数据库设计 |
| 语义分组 | 按 SQL 语义能力分组（如 upsert/分页/函数） | 不按历史模板机械分组 |

### 2.3.1 独立锁定查询（1.8.0）

已批准的 table `enableSelectForUpdate=true` 生成 `selectByPrimaryKeyForUpdate` 和/或 `selectByExampleForUpdate`，分别受对应普通查询开关及模型规则控制；支持复合主键、主键类和 BLOB 分层模型。Mapper 返回实际实体/实体列表；生成 Service 接口与实现返回对应 `ServiceResult`。实现使用 `MANDATORY` 及 `rollbackFor=Exception`，明确拒绝没有事务、只读事务和 PageHelper 分页/排序上下文；数据库异常直接抛出，不转换为失败返回。

生成判断由 Rules 统一提供，两种查询分别经既有 Mapper/XML/Service 元素初始化流程执行；服务接口签名复用 `ServiceMethods`，内省后预检归 `org.mybatis.generator.codegen.mybatis3.SelectForUpdateValidator`，预检及 XML 共享方言能力归 `org.mybatis.generator.dbstrategy.SelectForUpdateDialect`，门禁生成机制独立维护，不再采用跨层集中装配。锁定 select 插件回调默认兼容既有插件，与普通 select 及 update/batch 回调隔离；具体源码入口和回调契约见[配置指南第 3.2.4 节](generator-config-guide.md#324-验证与再生成)。这是保持产物行为的结构调整，不新增业务能力。

XML 与 Service 共用生成的 `Gen{Domain}MapperLockQueryGuard` 类。XML 执行前 bind 门禁覆盖直接 Mapper 调用，`useCache=false/flushCache=true` 避免二级缓存及既有一级缓存结果，Service 不生成缓存注解。Example 必须有实际非空标准条件，只接受内省非 BLOB 列的标准条件操作符；拒绝 DISTINCT、任何动态排序、原始 SQL 和空集合条件。条件非空不等于影响范围小，业务仍须验证索引、授权和预期锁范围。

SQL 语法核对分组：MySQL/MariaDB/PostgreSQL/Oracle/H2 使用 `FOR UPDATE`；SQL Server/SQL Server 2005 使用 `WITH (UPDLOCK, HOLDLOCK)`。生成前遍历全部 `additionalDbTypes`，逐方言输出 `databaseId`；其他方言，包括 SQLite，显式拒绝通用锁定查询，不沿用依赖前置写入的受管关系特殊方案。此扩展不改变既有受管关系方法。

这些分组是生成 SQL 语法契约，不是跨方言事务隔离等价承诺。MySQL/InnoDB 锁定读与 PostgreSQL REPEATABLE READ 的行为不同，后者可能因并发更新失败而非返回更新后的版本；调用方负责重试、死锁/超时处理与宿主隔离级别验收。离线回归覆盖 Java 产物编译、运行时门禁、MyBatis XML/databaseId/缓存设置解析；未连接业务数据库或验证真实跨数据库锁竞争。详细配置、运行边界及受控再生成要求见[配置指南第 3.2 节](generator-config-guide.md#32-独立锁定查询180)，验收记录见[实施任务书](task\select-for-update-task.md)。

## 2.4 AI 约定优先规则（Convention First）

> 这一节用于给 AI 与设计开发人员提供有前提的默认推断，减少重复配置；不以列名或单个配置推定完整业务行为。

实践操作统一见[配置指南第 20 章](generator-config-guide.md#20-约定驱动配置与实践建议)，其中按主题连接字段长度与控件、语义字段默认值、排序分组、多列布局及复用边界。业务语义相符时优先利用约定；不相符时显式配置或评审扩展，不反向扭曲数据库设计。字段默认值的赋值层次及 `apply_date` 实例由[数据库规范 §4.5、§4.6](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-core-db\src\main\resources\docs\database-authoritative-spec.md#45-面向生成与业务使用的字段设计实践)维护，不能由日期字段名称推断所有写入口均会赋当前日期。

沟通或代码阅读中发现值得复用的约定及非代码设计思想，按[全局过程 §1.5](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\ai-assisted-development-standard.md#15-实践指导的持续发现确认与沉淀)提案并获许可后更新归属文档，具体行为仍以实际版本、生成链路及宿主消费为准。

| 约定主题 | 默认行为（源码口径） | AI 使用建议 |
|---|---|---|
| `bytes_` 字段 | VO 默认排除 `bytes_`（避免大字段直接进入常规 VO） | 需要前端显示时，优先走附件/下载链路，不直接暴露原始二进制字段 |
| 上传/下载接口 | 仅当 table 启用 `enableFileUpload` 时生成 upload/download 接口；若存在 `bytes_` 且为 blob 列走字节流分支，否则走磁盘路径分支 | 设计时先判定“库内存储”还是“文件路径存储”，再决定接口与页面方案 |
| 大字段展示 | Vue 列表模板对 blob/long text 字段提供 `v-safe-html` 展示插槽条件 | “富文本编辑”不是纯自动推断，需在 `htmlElementDescriptor` 明确控件类型/插件 |
| 默认值注入 | `new-instance` 与 VO 初始化会读取列默认值；`CURRENT_*` 与 `DbFiledDefaultValueEnum` 标记可触发自动赋值 | `*_user_id`、`tenant_id`、`org_id` 等请以“默认值标记”驱动，不要仅靠列名假设 |
| 字典/枚举复用 | `DictSys/DictData/DictEnum/DictUser` 可通过 `overridePropertyValue` 与 `dataSource` 复用；部分场景可自动补 dataUrl | 先建统一字典策略，再按字段批量套用，避免每表重复定义 |
| 可复用配置池 | `reusableElements` 可复用 `htmlElementDescriptor`、`overridePropertyValue`、`listColumnDescriptor`、`fieldOverrides`、`filterColumn` | 跨模块一致规则一律先放 `reusableElements`，表内只放差异项 |

---

## 3. “可直接配置实现”的典型模式（重点）

先按“**业务语义模式**”选能力，再落到具体 XML。
如果一个需求能映射到以下模式，通常应优先走配置生成。

## 3.1 多对多关系（`selectByTable + selectByTable`）

通过中间表分别从两端生成查询和关系维护。每个 `selectByTable` 配置挂在“查询结果所属实体”的 table 下：

1. `thisColumn` 是关系表中存放当前实体主键的列；`otherColumn` 是关系表中存放对端实体主键的列。
2. A 的配置以 `otherColumn` 上的 B 主键查询 A；在 B 的 table 下交换两列并配置同一关系表，即可反向查询。
3. `enableSplit` / `enableUnion` 分别生成按当前 A 主键和一组 B 主键解除/新增关系的方法；它们操作的是所选关系行，不是全量替换关系集合。

| 维度 | 设计判定 |
|---|---|
| 最小可行输入 | 双侧实体 + 关系表 + 两侧主键列映射；设计上建议关系表两侧键非空并建立联合唯一约束，且须核对实际 DDL 是否已落实 |
| 直接产物 | 双侧 Service/Mapper 查询方法；可选生成关系维护方法和 Controller 端点 |
| 常见低估 | 误以为“多对多都要手写 Mapper” |
| 常见高估 | 把 `javaModelCollection` 当作多对多关系自动映射，或未定义关系表两侧键语义却期望自动推断 |

例如，`sys_menu` 与 `org_role` 可在各自 table 下对同一关系表交换 `thisColumn` / `otherColumn` 配置查询与关系维护。未配置 `relationIdKey` 时保持原有批量 SQL、缓存和标识策略，不自动添加关系约束或发现跨 Context 缓存。显式配置 `relationIdKey` 后启用受管关系：配置值是稳定逻辑关系标识，单行插入参数 `relationId` 仍是运行时生成的随机关系行主键，业务幂等由数据库两侧键联合唯一约束保证；生成 Service 调用 `RelationWriteSupport`，通过 `RelationMutation` 的原 `relation` 字段声明逻辑关系及提交后失效范围。实体写入影响多个受管关系时，生成 `RelationMutations` 容器声明各关系及独立缓存范围；相同逻辑关系的描述合并缓存范围。基础包在同一事务中记录各关系的 outbox 任务，任一任务入库失败导致整体回滚，提交后分别派发；单关系注解保持兼容。部署多关系生成产物前须同步升级支持该容器的 `vgosoft-mybatis`。默认不启用查询缓存；启用缓存时，两端可使用相同 `relationIdKey` 自动得到 `<relationIdKey>#72h` 共享缓存名，也可显式配置相同共享缓存区。仅失效该关系共享区域，不生成旧空间兼容失效；实体原缓存及非受管关系自己的缓存保留。不自动跨 Context 推断。关系表 table 上的可选 `relationKeyColumns` 是独立的 DDL 建模覆盖，生成非空两侧键、联合唯一索引和反向索引；它不迁移已有数据库。仅支持单列字符串实体主键及字符串关系列，不带租户或外键。通用配置与生成边界见 [`generator-config-guide.md` 第 13.2.1 节](generator-config-guide.md)；组织项目的[角色—菜单关系设计详文](D:\java\idea_projs7\project-vgosoft\vgosoft-organization\src\main\resources\docs\vgosoft-organization-design-overview.md)记录实际配置、DDL 与缓存证据，[前后端接口契约第 6.5 节](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-web2\src\main\resources\docs\frontend-business-capabilities-spec.md)记录当前 HTTP 参数及返回契约。

**设计结论**：中后台常见“角色-菜单”“用户-岗位”“组织-标签”等多对多，通常无需手写 DAO 基础代码。

## 3.2 复合列表查询（`selectByExample` 主链路）

列表接口默认基于 `buildExample(...)` 组装条件，核心可覆盖：

- 按字段等值/区间（`between` 片段）查询
- 模糊或多值字符串条件
- 动态 `orderByClause`
- POST 列表里的 `filterParam/filterMap` 条件扩展
- 可选级联结果：`selectByExampleWithRelation`
- 树/层级场景可选子数量：`selectByExampleWithChildrenCount`（依赖层级字段语义）

| 维度 | 设计判定 |
|---|---|
| 最小可行输入 | requestVo/viewVo 字段、查询片段（如 between）、排序策略 |
| 直接产物 | GET/POST 列表接口 + Example 条件构造方法 |
| 可增强路径 | `actionType` 场景化条件、`filterParam` 复杂过滤 |
| 常见低估 | 把“组合筛选”过早判定为手写 SQL |
| 常见高估 | 期望在配置层完成复杂运行时决策引擎 |

**设计结论**：大多数“组合筛选 + 分页 + 排序 + 可选关系展开”场景，可先按配置落地，不必先假设要手写复杂 SQL。

## 3.3 状态/字典中文显示（值转换链路）

典型模式：`状态值列 -> 展示文本列`，通过 `overridePropertyValue`（如 `DictSys/DictData/DictEnum`）配置完成，前后端同时受益（VO 字段 + 页面渲染联动）。

| 维度 | 设计判定 |
|---|---|
| 最小可行输入 | sourceColumn + annotationType + typeValue + targetProperty |
| 直接产物 | VO 展示字段（如 `statusText`）+ 页面可直接展示 |
| 常见低估 | 误以为字典中文必须在 service 手写转换 |
| 常见高估 | 把复杂业务规则映射误当成静态字典映射 |

**设计结论**：只要是“值域稳定、映射规则明确”的显示转换，优先走配置而非业务代码硬编码。

## 3.4 主子表编辑与内嵌列表

`htmlElementInnerList + innerListView + viewVo` 可生成主子协同页面主干，并提供 Hook 扩展点处理个性逻辑。

| 维度 | 设计判定 |
|---|---|
| 最小可行输入 | 主子关系字段 + innerList 数据来源 + 列表展示字段 |
| 直接产物 | 编辑/详情页面的内嵌子表骨架与基础交互 |
| 常见低估 | 误以为主子表页面必须全手写前端 |
| 常见高估 | 期望模板直接覆盖复杂跨子表联动规则 |

**设计结论**：流程单据、明细行编辑类场景可先生成 70%~90% 骨架，再补业务规则。

## 3.5 统计与看板接口（配置化统计）

`generateDateDimStatistics` + `generateCustomStatistics` 可覆盖常见趋势/汇总统计，并支持 join 条件扩展。

| 维度 | 设计判定 |
|---|---|
| 最小可行输入 | 指标列 + 时间维度列（或自定义聚合表达式） |
| 直接产物 | 统计 service 方法与可选 controller 接口 |
| 常见低估 | 误以为统计必须另建手写查询模块 |
| 常见高估 | 把跨域复杂指标平台能力等同于单模块统计配置 |

---

## 4. “需谨慎评估/可能二开”的边界

1. 高度定制的富交互（复杂拖拽、画布、强实时联动）
2. 强数据库方言特性且语义难等价（需显式降级策略）
3. 业务规则依赖运行时复杂上下文，难以仅靠配置描述
4. 超出现有模板结构的前端交互协议

---

## 5. 设计阶段必填输入（Input）

生成器版本与配置 DTD 必须一并确定，并与实际执行的 core 版本一致。当前 `1.8.0-SNAPSHOT` 对应 `mybatis-generator-config_1_8_0.dtd`；升级须同步配置、说明及设计文档，并确认发布包资源与 parser 解析。命名、DOCTYPE 示例、兼容边界和执行前门禁统一见 [配置指南第 1.1 节](generator-config-guide.md#11-生成器与配置-dtd-版本一致规则)。

1. **领域关系**：主表、关系表、树形层级、主子表边界
2. **查询策略**：是否需要按列查询、中间表查询、统计查询、关系展开
3. **展示策略**：哪些字段做值转换、哪些字段做列渲染
4. **页面策略**：列表/编辑/详情/打印，是否需要 innerList/附件/审批意见
5. **多方言策略**：`additionalDbTypes` 集合与语义等价方案
6. **扩展策略**：哪些点保留为 Hook/子类重写，而不是生成器硬编码

---

## 6. 设计防误判清单

## 6.1 低估风险（本可生成，却被误判为要二开）

| 检查项 | 通过标准 |
|---|---|
| 多对多关系 | 已评估 `selectByTable` 双向配置及 split/union 维护接口 |
| 复合查询 | 已评估 `selectByExample + requestVo + filterParam` 组合 |
| 值转文案 | 已评估 `overridePropertyValue` 是否可覆盖 |
| 字典枚举复用 | 已评估 `reusableElements + Dict*`，避免逐表重复配置 |
| 主子页面 | 已评估 `innerList` 是否可先产出骨架 |
| 统计接口 | 已评估 dateDim/customStatistics 是否满足核心指标 |
| 文件能力 | 已判断是否启用 `enableFileUpload`，并确认 `bytes_`/文件路径存储模式 |
| 默认值策略 | 已确认关键字段默认值来自 DB 默认值/`DbFiledDefaultValueEnum` 标记，而非仅列名猜测 |

## 6.2 高估风险（配置难覆盖，却被误判为可直接生成）

| 检查项 | 失败信号 |
|---|---|
| 交互复杂度 | 依赖拖拽编排/强实时协同/复杂画布交互 |
| 规则复杂度 | 条件依赖跨上下文运行时状态，无法静态配置 |
| 方言差异 | 目标方言语义不等价，却未定义降级/禁用策略 |
| 关系建模 | 中间表键语义不完整，仍要求自动推断 |
| 默认值认知 | 仅凭 `xxx_user_id` 列名就断言可自动赋值，未核对默认值标记机制 |

## 6.3 评审结论模板（建议固化）

1. 该需求属于哪一种语义模式（3.1~3.5）？
2. 最小可行输入是否齐全（字段/关系/字典/方言）？
3. 可生成范围与手写范围是否已明确分界？
4. 是否给每个目标方言写明“生成/降级/禁用”策略？

---

## 7. 与实施文档的关系（How）

- **本文件**：回答“能做什么（What）”
- **`generator-config-guide.md`**：回答“如何做（How）”
- **`.github\generator-core-dev-spec.md`**：回答“内核继续开发如何做（Develop）”

建议流程：

1. 先用本文件判定“是否可生成、可生成到什么程度”
2. 再用 `generator-config-guide.md` 编写 `generatorConfig.xml`
3. 若能力仍不足，再进入 `.github\generator-core-dev-spec.md` 做内核改造

---

## 8. 标准模块结构约定（生成优先版）

本节用于规范“一个业务模块在落地时应该长什么样”。新模块设计必须先按本结构划分生成区与扩展区，再讨论生成器配置。

### 8.1 标准模块目录

```text
vgosoft-<domain>
├─ src/main/java/com/vgosoft/<domain>
│  ├─ codegen
│  │  ├─ controller
│  │  ├─ dao
│  │  └─ service
│  │      └─ impl
│  ├─ config
│  ├─ controller
│  ├─ dao
│  ├─ entity
│  ├─ listener
│  ├─ pojo
│  │  ├─ abs
│  │  ├─ maps
│  │  └─ vo
│  └─ service
│     └─ impl
└─ src/main/resources
   ├─ mappers
   ├─ sql
   └─ generatorConfig.xml
```

### 8.2 目录职责

| 目录 | 性质 | 约束 |
|---|---|---|
| `codegen/` | 生成区 | `controller/dao/service` 的生成实现，允许每次重新生成覆盖 |
| `entity/` | 生成区 | 数据实体，每次重新生成覆盖 |
| `pojo/abs` | 生成区 | 抽象 VO/POJO 基类，每次重新生成覆盖 |
| `pojo/maps` | 生成区 | MapStruct/映射实现，每次重新生成覆盖 |
| `pojo/vo` | 生成区 | 对外 VO、Create/Update/View/Request/Excel 等，每次重新生成覆盖 |
| `mappers/` | 生成区 | Mapper XML，每次重新生成覆盖 |
| `sql/` | 生成区 | 生成的 DDL 与初始化数据脚本，每次重新生成覆盖 |
| `controller/` | 扩展区 | 首次生成空类，后续只允许添加项目个性化定制代码 |
| `dao/` | 扩展区 | 首次生成空接口或扩展接口，后续只允许补充个性化方法 |
| `service/impl/` | 扩展区 | 首次生成空实现或继承实现，后续只允许补充个性化逻辑 |
| `listener/` | 扩展区 | 生命周期消息处理类，首次生成空方法，后续补充事件处理逻辑 |
| `config/` | 扩展区 | 配置装配 |
| `dto/`、`enums/`、其他 | 按需新增 | 仅在生成器现有模型无法覆盖时新增 |

### 8.3 生成与扩展边界

1. `codegen/`、`entity/`、`pojo/`、`mappers/`、`sql/` 属于**可覆写生成产物**，设计时不得把人工维护内容放入其中。
2. `controller/`、`dao/`、`service/`、`listener/` 属于**扩展子类层**，只做个性化增强，不承担生成器应产出的基础 CRUD。
3. 如果生成器已能输出某个能力，优先把能力放回生成器配置，不在扩展层重复实现。
4. 如果生成器暂时无法覆盖，先在扩展层止血，再回到生成器补能力，形成“生成器改造 + 重新生成”闭环。

### 8.3.1 产物保护标记对照（`@mbg.generated` 与 `@VCF.overridable`）

两个标记常被统称为“生成产物保护标志”，但**语义相反**：一个表示禁止手改，一个表示允许覆盖。判定产物能否人工修改前，必须先区分是哪一个标记，不能凭“文件含生成注释”一概而论。

| 维度 | `@mbg.generated` | `@VCF.overridable` |
|---|---|---|
| 语义 | **禁止手改**，重新生成必定覆盖 | **保留完整标记 = 允许覆盖** |
| 常量定义 | `org.mybatis.generator.config.MergeConstants.NEW_ELEMENT_TAG` | `org.mybatis.generator.custom.ConstantsUtil.OVERRIDABLE_FLAG` |
| 主要落点 | `*Example.java` 查询条件对象（Java 侧唯一落点）、Mapper XML 中的生成语句/片段（XML 侧，节点级） | `codegen` 之外允许扩展的产物：后端 `*Mapper.java` / `*Controller.java` / `*Listener.java`、前端页面与组件、Hooks、列插槽文件 |
| 人工扩展方式 | `*Example.java`：**不允许任何就地修改**，需改变则改生成器生成逻辑后重新生成；Mapper XML：允许在同一文件内**新增不带该标记的语句节点**做扩展，但以生成片段为蓝本复制改写时必须删掉标记 | 扩展前把完整标记改为无法严格匹配的形式（如 `@VCF.###.overridable`），并确认文件中不再残留完整字符串，随后转人工维护 |
| 误处理后果 | 手改内容在下次生成时丢失（XML 侧还包括保留了标记的复制片段） | 未改标记即写入业务代码，重新生成时被覆盖 |
| 恢复标记 | 不适用 | 恢复完整标记＝再次允许覆盖，必须先确认人工扩展已迁出或明确允许丢弃 |

判定补充：

1. `codegen` 包/目录下的纯生成产物**每次生成都覆盖，不看标记**，禁止手改，也不得通过修改标记将其转为人工维护文件。
2. 两种标记的覆盖**粒度不同**，不能互相套用：`@mbg.generated` 在 Java 侧是**文件级整体覆盖**（`*Example.java`），在 XML 侧是**节点级覆盖**；`@VCF.overridable` 始终是**文件级**判定。详见 §8.3.2。
3. 外层文件允许扩展**不等于**其中每个生成片段都可手改：Mapper XML 中带 `@mbg.generated` 的语句/片段仍由生成器维护；但同一文件中**不带标记的节点不会被覆盖**，可作为动态 SQL 扩展落点。
4. `The content below is generated by vgosoft's code factory.` 只是**来源说明**，不是覆盖开关，不得据此推出“凡含生成注释一律禁止手改”。
5. 生成器自身源码与测试中出现的同名字符串（如 `MergeConstants`、`DefaultCommentGenerator`、`CommentGenerator`、`CasWriteElementGenerator` 及生成回归测试）**不是产物**，不适用本节规则。
6. 本节提供**标记语义对照与常量出处**，规则正文以[生成器项目指令 §2.1](..\..\..\..\..\.github\copilot-instructions.md#21-生成产物保护覆盖与人工扩展规则前后端统一)为准；两者表述冲突时以指令 §2.1 为准。消费项目只在固定指令、索引与映射中提供引用入口，不复制规则正文或本对照表。

### 8.3.2 `@mbg.generated` 的两个落点与覆盖机制

`@mbg.generated` 只有一个语义：**禁止手改**。但它只有两个落点，覆盖粒度不同，判定前必须先分清是哪一个：

| 落点 | 产物 | 覆盖粒度 | 是否允许人工修改 |
|---|---|---|---|
| Java 侧 | 实体 `*Example.java`（查询条件对象） | **文件级整体覆盖**，不做成员级合并 | **完全不允许**，任何修改下次生成即丢失 |
| XML 侧 | Mapper XML 根元素直接子节点的生成语句/片段 | **节点级**，仅替换带标记的节点 | 带标记节点不允许；同文件内**新增不带标记的节点**允许 |

机制与代码出处：

1. **Java 侧**：`MyBatisGenerator.writeGeneratedJavaFile` 在目标文件已存在时先判 `ShellCallback.isMergeSupported()`；接口默认返回 `false`，各工程统一使用 `DefaultShellCallback(true)`，因此走覆盖分支（日志 `Warning.11`）整体重写文件。`*Example.java` 内的 `@mbg.generated do_not_delete_during_merge` 只在启用 Java 合并时才起作用，当前不生效。结论：`*Example.java` 是查询条件对象，**任何时候都是生成可覆盖的，不允许修改**。
2. **XML 侧**：`XmlFileMergerJaxp.getMergedSource` 遍历已有根元素的子节点，`isGeneratedNode` 命中的节点（元素节点且其首个 `COMMENT` 子节点包含该标记，或 id 命中旧前缀）先删除，再把新生成节点插入根节点前部；**未命中判定的节点原样保留**。这就是“同一 Mapper XML 内可以并存生成方法与人工扩展的动态 SQL 方法”的原因。

操作规则：

1. `*Example.java` 禁止手改；需要新增查询条件能力时，改生成器的 Example 生成逻辑后重新生成。
2. 在 Mapper XML 中扩展动态 SQL：新增语句节点时**不要复制**生成片段的「提示 - `@mbg.generated`」注释块；若以生成语句为蓝本复制改写，**必须删掉该注释块**，否则该节点仍被判定为生成节点，改动会在下次生成时被覆盖。
3. 现状（2026-10-25 扫描，排除 `target/`）：`vgosoft-basic` + `mpmm` + `project-vgosoft` 的 Mapper XML 共 3234 个语句节点，其中 3224 个带标记、10 个无标记。这 10 个即人工扩展的动态 SQL（如 `WfDelegateMapper.selectResolvedDelegate`、`WfProcTypeMapper.selectByMapPrincipals`、`SysCfgModuleMapper.truncateTable`），是本节规则的正例。

### 8.4 新模块设计顺序

1. 先定数据库表和字段语义。
2. 再定生成器输入，包括模块名、实体名、VO 划分、是否需要 listener、是否需要 DDL/初始化脚本。
3. 再定生成区与扩展区边界。
4. 最后才补充 controller/service 的个性化接口。

### 8.5 设计判定模板

新模块设计评审时，必须明确回答以下问题：

1. 哪些目录由生成器覆盖？
2. 哪些目录只允许首次生成空壳？
3. 哪些能力必须落在扩展层？
4. 哪些能力需要回到生成器补齐？
5. 是否存在 `dto`、`enums`、`config` 等额外目录需求？

### 8.6 Maven 插件强制约定（可生成模块）

如果模块需要使用 MyBatis Generator 生成代码，则该模块 `pom.xml` 必须包含以下构建插件配置：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.mybatis.generator</groupId>
            <artifactId>mybatis-generator-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

约束：

1. 该配置属于可生成模块的必选项，禁止省略。
2. 未配置该插件的模块，不得标记为“生成器可落地模块”。
