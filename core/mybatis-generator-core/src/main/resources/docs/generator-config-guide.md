# generatorConfig.xml 配置指南

## 受管 CAS 输出（1.8.0）

- 当前 DTD 的 table 属性 `enableCasWriteContract="true"` 为显式 opt-in；省略或 false 保持旧输出。DTD 空白归一化前再按统一表规则严格校验，空字符串、纯空白、`TRUE`、` true ` 均拒绝。该开关与 context Plus 生成开关及宿主工厂无控制关系。
- 首期只接受可变 MyBatis3/XMLMAPPER、已核验的 `AbstractPersistenceLockEntity` / `AbstractPersistenceLockSortableEntity`、单非自增 String/VARCHAR id_、Integer/INTEGER 非空 version_、LocalDateTime/TIMESTAMP 审计时间、String/VARCHAR 审计身份，以及 mysql/h2。缺 Model/Mapper/XML/Service、generatedKey、未知父类、复合主键、关系级联、错误类型及其他方言预检失败，不静默修正配置。
- 复用实际 Maven 入口调用的 `ConfigurationParser.customConfig`（既有插件、JSR310 等）；只调用原始 Parser 的输出不是完整 Maven 自定义链。测试分别保留这两条路径，不能临时补 runtime 或输出开关伪装消费者配置。
- 增加 `updateByPrimaryKeyCas(record, expectedVersion)` / `updateByPrimaryKeySelectiveCas(record, expectedVersion)`；Mapper 返回 int 并使用两 `@Param`，Service 返回 `ServiceResult<T>`。旧版本从 Update VO 独立传入，不从 Entity 默认 1 推导。Update 校验组为 version 生成 `@NotNull`，Update 不初始化、Create 保留 1；完整 NULL/selective 非空语义不改。
- insert/insertSelective/insertBatch 保留签名且 SQL 固定版本 1。CAS 服务用 MANDATORY 事务、不吞冲突；旧 Example/CASE/upsert/删除/无版本写服务显式拒绝，Mapper 所有其他写节点均变为受管固定 `ENTRY_DISABLED`。MyBatis BatchExecutor 仍不支持，insertBatch 是一条多值 SQL。
- 显式 `selectByColumn/property isLogicalDelete=true` 可增加受管 `LOGICAL_DELETE`，仅允许单值 id_/version_；其他删除仍拒绝。配置、Controller 参数形状、默认兼容和运行依赖要求见第 8.1 节。
- 按 context.moduleKeyword 聚合唯一 `META-INF\vgosoft\write-contracts\<moduleKeyword>.xml`，协议 `managed-cas-1`，不硬编码业务名。逐表和 context 插件回调之后验证必需 Java 文件/签名、服务事务与异常传播及全部非 SELECT SQL；插件否决、重复、未知写或语义篡改失败。资源须在原工厂显式装载，没有生产扫描或审计自动装配。
- `CasWriteGenerationTest` 对比完整原始输出基线及默认/false 全输出，检验真实 metadata 替身和插件负例。`CasConsumerBaselineTest` 保留实际八表配置及开关形状，去敏替换连接/驱动、重定向输出，`generate(..., false)` 后仅捕获 target 产物。`sc03.cas=true`、`sc03.h2=true`、`sc03.baseline.name=sc03-final-generated` 用于两步跨仓测试；业务配置不加 H2、不运行业务生成入口。精确证据见 [Sensitive SC03 任务书](D:\java\idea_projs7\project-vgosoft\vgosoft-sensitive\src\main\resources\docs\task\sensitive-functional-implementation-task.md)；运行契约见 [basic 能力 §6.4](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-mybatis\src\main\resources\docs\vgosoft-mybatis-capability-overview.md#64-生成器受管-cas-契约managed-cas-1未发布)。

> 读法约定：**DTD 定义结构**，**源码定义真实行为**。当两者不一致时，以源码行为为准。

## 1. 根结构

### 1.1 生成器与配置 DTD 版本一致规则

`generatorConfig.xml`、`generator.xml` 等生成配置必须引用与**实际执行的生成器 core 版本**一致的 DTD，不能仅因旧 DTD 仍可解析而继续使用旧版本。当前版本 `1.8.0-SNAPSHOT` 对应 `mybatis-generator-config_1_8_0.dtd`：去掉 `-SNAPSHOT` 后，将版本号中的点号替换为下划线。其他版本须核实发布包内的实际文件，不凭命名规则假定资源存在。

```xml
<!DOCTYPE generatorConfiguration
        PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
        "mybatis-generator-config_1_8_0.dtd">
```

PUBLIC 标识保持既有值，版本由 SYSTEM 文件名表达。执行前核对 Maven 插件实际加载的 core 版本或直接调用入口的运行 classpath，而非只看业务 POM 中声明的依赖；确认对应 DTD 位于生成器 JAR 的 `org\mybatis\generator\config\xml` 下，并使用该 DTD 校验结构。

升级时同步生成器版本、配置 DOCTYPE、配置说明和相关设计文档，复核 parser 的资源解析及新增配置行为。当前 `ParserEntityResolver` 对 `mybatis-generator-config_1_8_0.dtd` 选择对应资源，其他文件名走历史 `1_7_0` 分支；这种兼容行为不是版本一致校验，也不保证任意未来版本会被正确解析。不得通过只改文件名、沿用旧 core 或依赖兼容回退声称升级完成。

DTD 维护只修改当前版本文件，保留历史版本契约；元素、属性、默认值或约束变更同时更新对应注释。生成器维护者按 `.github\copilot-instructions.md` 第 7.1 节维护发布资源；业务项目引用本节，不复制通用规则正文。

### 1.2 根节点配置

| 节点 | 作用 | 关键点 |
|---|---|---|
| `generatorConfiguration` | 根节点 | 包含 `properties?`、`classPathEntry*`、`context+`。 |
| `properties` | 外部属性文件 | `resource` / `url` 二选一。用于占位符替换。 |
| `classPathEntry` | JDBC 驱动扩展 | `location` 必填。 |

## 2. `context`

| 属性/子项 | 含义 | 默认/行为 |
|---|---|---|
| `id` | 上下文 ID | 必填。 |
| `appKeyword` | 应用标识 | 影响包路径 / 代码归属。 |
| `moduleKeyword` | 模块标识 | 影响 controller 路由、页面路径、菜单。 |
| `moduleName` | 模块中文名 | 用于展示/模块数据。 |
| `jdkVersion` | JDK 版本 | 必填，取值 `8/9/11/17/21`。 |
| `targetRuntime` | 生成运行时 | 当前 1.8.0：省略、null、空字符串或纯空白默认 `MyBatis3`；显式内置名称忽略大小写，其他非空值按实现类名解析。 |
| `integrateMybatisPlus` | 当前 `context` 下生成物是否包含 MyBatis-Plus 相关内容 | 默认 `true`；仅控制生成输出，不决定宿主工程是否集成 MyBatis-Plus。 |
| `integrateSpringSecurity` | 是否集成安全组件 | 默认 `true`。 |
| `forceUpdateScalableElement` | 是否强制覆盖可扩展元素 | 配合 `forceUpdateElementList` 使用。 |
| `parentMenuId` | 菜单父级 | 生成 view 时可继承。 |
| `updateModuleData` | 是否更新模块数据 | 默认 `false`。 |
| `updateMenuData` | 是否更新菜单数据 | 默认 `false`。 |
| `onlyTables` | 限制仅生成指定表 | 多表用逗号分隔。 |
| `vueEndProjectPath` | Vue 前端项目路径 | 仅项目根路径，不含 `src`。 |
| `plugin*` | 插件 | 额外扩展点。 |
| `javaTypeResolver` | 类型解析器 | 影响 JDBC -> Java 类型。 |
| `javaModelGenerator` | Java Model 生成器 | 实体/主键类/Example 类。 |
| `sqlMapGenerator` | XML Mapper 生成器 | Mapper XML 输出。 |
| `javaClientGenerator` | Java Client 生成器 | Mapper/DAO 接口。 |
| `reusableElements` | 可复用表配置 | 供多表共享的 UI / 值转换 / 列配置。 |

`integrateMybatisPlus` 是 `context` 级生成选项，对该 context 下的表生成物生效。例如设为 `false` 时，不生成由该选项控制的 MyBatis-Plus 专属内容，如 Mapper 接口上的 Plus `Mapper<T>` 继承、模型/字段上的 Plus 注解。它不表示消费宿主未依赖或未启用 MyBatis-Plus，也不控制宿主的依赖、自动配置、`SqlSessionFactory` 或插件；宿主运行时集成情况须独立核对。DTD 接受该属性不等于切换或排除宿主运行框架。

### `additionalDbTypes` 说明

- 这是 **表级/上下文级** 方言扩展集合。
- 合法值以 `DatabaseType.code` 为准，DTD 中的枚举只是兼容提示。
- 生成 SQL 时不能只写单方言分支，必须遍历 `additionalDbTypes`。

### 2.1 默认运行时兼容性变更（1.8.0）

经用户明确批准，当前 core 将缺省运行时从 `MyBatis3DynamicSql` 改为 `MyBatis3`。这是**全局默认变化**，不仅影响锁查询：未指定运行时的普通生成现在走 MyBatis3 的 Model/Example/Mapper/XML 链路，XMLMAPPER 配置需具备 `sqlMapGenerator`；即使 `enableSelectForUpdate=false` 也会受到默认运行时变更影响。需要保留动态 SQL 产物的消费者必须显式设置 `targetRuntime="MyBatis3DynamicSql"`。已有显式动态 SQL 示例无需修改。

共享入口 `Context.getEffectiveTargetRuntime()` 只处理缺省；保留原始 `getTargetRuntime()` 值。验证及实际内省共用 `ObjectFactory.createIntrospectedTableForValidation(...)` 的别名/反射分支。四种内置运行时的大小写兼容、显式自定义实现类及其构造异常仍保持工厂原行为；未知非空值不回退，不修剪带前后空格的非空名称。当前 `StringUtility.stringHasValue` 仅处理 null/empty，因此有效运行时入口额外识别 `trim()` 后为空的值，不改变其他配置的字符串语义。

DTD 属性仍为 `#IMPLIED`，仅当前 `mybatis-generator-config_1_8_0.dtd` 增补说明，历史 DTD/版本契约不改；使用旧 core 的历史默认未被追溯改写。当前 core 解析旧配置资源时仍会使用当前代码默认，因此不能以旧 DOCTYPE 保留动态 SQL 行为，应保持 core/DTD 版本一致并显式选择运行时。

## 3. `table`

| 属性/子项 | 含义 | 默认/行为 |
|---|---|---|
| `tableName` | 表名 | 必填。 |
| `ignore` | 是否忽略生成 | `true` 时不进入后续生成。 |
| `domainObjectName` | 实体名 | 为空则按表名推导。 |
| `alias` | 表别名 | 影响 SQL 片段。 |
| `tableType` | `dataTable` / `relationTable` | 影响关联/关系表语义。 |
| `isModules` | 是否模块主表 | `true` 时可生成模块数据。 |
| `validateIgnoreColumns` | 免校验列 | 为空有默认忽略列。空字符串表示不忽略。 |
| `enableInsert` 等 | 基础 CRUD/批量/upsert 开关 | 控制普通表方法；与 `selectByTable` 的 `enableSplit` / `enableUnion` 关系维护开关无关。 |
| `enableSelectForUpdate` | 独立主键/Example 锁定查询 | 默认 `false`，当前 1.8.0 DTD；事务、条件、分页及方言限制见第 3.2 节。 |
| `enableDropTables` | 可清除业务表 | 需放在主模块表。 |
| `cleanAllGeneratedElements` | 清理所有生成元素 | 适合重建。 |
| `generatedKey` | 主键生成策略 | 序列/identity 等。 |
| `domainObjectRenamingRule` | 实体名重命名 | 字符串替换。 |
| `columnRenamingRule` | 列名重命名 | 字段命名预处理。 |
| `columnOverride*` | 列覆盖 | 改类型、注释、长度、必填、属性名等。 |
| `ignoreColumn*` | 忽略列 | 不生成 SQL/属性。 |
| `selectByColumn*` | 按列查询 | 可生成 controller 方法。 |
| `selectByTable*` | 中间表查询及可选关系维护 | 用于多对多；两端分别配置，列方向、生成方法及幂等边界见第 8、13.2 节。 |
| `selectBySqlMethod*` | SQL 函数查询 | 主要用于父子层级 ids。 |
| `javaModelAssociation*` | 一对一关联 | 级联 insert/update/delete。 |
| `javaModelCollection*` | 一对多关联 | 子查询 / 级联。 |
| `generateModel` | 实体模型开关 | `enableChildren` 依赖 `parent_id`。 |
| `generateSqlMap` | XML Mapper 开关 | 生成 `mapper.xml`。 |
| `generateDao` | DAO 开关 | 可指定子包。 |
| `generateService` | Service 开关 | 可生成测试类、事件监听配置。 |
| `generateController` | Controller 开关 | 可生成测试类、树接口、选项接口。 |
| `generateHtml*` | 页面生成 | 可生成多个页面定义。 |
| `generateSqlSchema` | DDL 脚本 | 可按方言生成变体。 |
| `generateVo` | VO 总开关 | 子 VO 共用父配置。 |
| `generateCachePo` | 缓存模型 | 主要用于字典/映射。 |

#### `generateController` 与 `generateOptions`

`<generateOptions>` 必须放在已启用的 `<generateController generate="true">` 内；仅写该子节点不会独立生成 Controller。当前 DTD 要求显式提供 `nameColumn`。每个节点按 `nameColumn` 指定的**物理列名**增加 GET 选项接口。接口尾段由列的 Java 属性名转为短横线形式，例如 `name_` 通常生成 `{controllerBasePath}/option/name`；应以生成的 Controller 注解为准，不要只按 XML 节点名推断路由。生成器以 `id_` 为默认值列、`type="flat"` 为默认结构；`type="tree"` 会使用树选项 DTO 和模型 `children` 关系，须确保模型/父子字段与宿主树组件均支持。显式 `idColumn` 可改变返回值来源。

```xml
<generateController generate="true">
    <generateOptions nameColumn="name_"/>
</generateController>
```

查询列必须存在且能映射到生成实体属性；当前 `OptionElementGenerator` 在找不到 `nameColumn` 时会跳过该方法，因此生成后须检查路由确实存在。该端点返回 `FormSelectOption` / `FormSelectTreeOption` 列表，包含 ID、显示名称及可选 `selected` 回显；平面选项在存在 `sort_` 时排序，树选项递归使用 `sort` 排序，须确保对应模型属性存在。分页响应取决于 RequestVo 的 `includePageParam` 和请求分页参数。控制器方法会生成独立的数据选项权限 action（权限键由模块、Controller Bean 名及小写化的方法前缀组成，如 `optionname`）；静态权限 SQL 生成不等于权限数据已部署或用户已获权。接口本身不意味着响应来自 `generateCachePo` 缓存。

### 表级歧义修正

| DTD/注释 | 源码真实含义 |
|---|---|
| `sortColumn` | 实际属性是 `orderByClause`。 |
| `enableSelectByIds` | 实际属性是 `enableSelectByPrimaryKeys`。 |
| `excludedTable` | 实际属性是 `excludedTables`。 |

### 3.1 敏感字段与受管写方法（1.8.0）

`table` 末尾的可重复 `sensitiveCheck` 子节点按**精确物理列名**声明字段；不接受 Java 属性名、别名、通配符或大小写模糊匹配。节点与 `columnOverride`/忽略列节点共享末尾位置，字段重命名仍由 `columnOverride.property` 完成。不同声明不得重复引用同一列，包括 enable 值不同的声明。

```xml
<table tableName="article_" domainObjectName="Article" enableUpdateByExample="false">
    <generateModel generate="true"/>
    <generateService generate="true"/>
    <columnOverride column="content_" property="bodyText"/>
    <sensitiveCheck columns="content_,description_"
                    enable="true" strategy="INHERIT" contentType="PLAIN_TEXT"/>
</table>
```

| 配置 | 默认值 | 契约 |
|---|---|---|
| `columns` | 必填 | 非空、逗号分隔的物理列名；内省后必须唯一存在且未忽略 |
| `enable` | `true` | 仅 `enable=true` 参与敏感生成；`enable=false` 或未配置 `sensitiveCheck` 时完全忽略敏感生成，不改变原有输出 |
| `strategy` | `INHERIT` | `INHERIT/BLOCK/CONFIRM/REPLACE/WARN`，区分大小写 |
| `contentType` | `PLAIN_TEXT` | 本期仅支持纯文本，未知类型明确失败 |

内省后验证最终 Java 类型为 `java.lang.String`、可写列、唯一属性和实际实体字段。支持单实体模型中的 VARCHAR/LONGVARCHAR String 及列重命名；父类持有字段、分层主键/BLOB 模型、不可变模型、其他 runtime、逻辑 JSON/派生双表示字段、被插件删除/改为静态或 final 的字段均明确报表/列错误，不静默漏标。启用声明要求同时生成实体和 Service，不能配置 `modelOnly=true`。未确认全部子实体写前检测的级联写入不支持。

#### 3.1.1 生成阻止提示（配置向导）

以下条件一旦命中，生成器会直接失败并提示“表/列/方法”原因；建议在向导中逐项预检：

| 阻止类别 | 命中条件 | 配置建议 |
|---|---|---|
| 列定位失败 | `columns` 对应列不存在、被忽略或匹配不唯一 | 只填真实物理列名；先确认未被 `ignoreColumn`/正则忽略 |
| 列声明冲突 | 同一列被多个 `sensitiveCheck` 重复声明 | 合并为一次声明，避免策略冲突 |
| 列类型不支持 | 最终 Java 类型不是 `String`，或列为 generated-always | 仅对可写纯文本列启用敏感检测 |
| 逻辑类型冲突 | 配置了 `columnOverride.logicalType` 且非空 | 先移除逻辑 JSON/双表示，或暂不启用敏感检测 |
| 双表示/派生映射 | 命中 `overridePropertyConfigurations` 的源列 | 先完成“持久值=检测文本”一致性方案 |
| Java 属性异常 | 属性名缺失、非法、关键字或重名 | 先修复列到属性映射，再启用敏感检测 |
| 模型形态不支持 | 非 MyBatis3 可变单实体，或分层主键/BLOB/immutable | 切回单实体可变模型，或暂不启用 |
| 父类字段承载 | 目标字段在 rootClass 父类中 | 将字段留在当前实体类，确保可落注解 |
| Service 不可落地 | 未生成 ServiceImpl 或 `modelOnly=true` | 启用 Service 生成并关闭 `modelOnly` |
| 写入口未受控 | 开启 `enableUpdateByExample` | 关闭后再启用敏感检测 |
| 多实体级联写 | relation 开启 insert/update/upsert | 先拆分为可受控单实体写，或补齐多实体检测方案 |
| 方法签名异常 | 写方法重复/不可代理/参数不是实体、`List<Entity>` 或受管 CAS 的 `(Entity, Integer expectedVersion)` | 保持标准方法签名，避免自定义破坏参数角色 |
| CAS 与 update batch 冲突 | Sensitive opt-in 与受管 CAS 同时启用，且配置要求生成 `updateBatchByPrimaryKey` | 关闭 update batch；当前没有受管 CAS 批量更新入口 |
| 事务声明不在白名单 | 已有 `@Transactional` 非已验证模板 | 统一为受支持事务模板后再启用 |
| 方法矩阵不完整 | 标准写方法未完整装配 | 保证标准写方法生成开关与规则一致 |

> 开关语义：未配置 `sensitiveCheck`，或所有声明均为 `enable=false`，不会触发上述阻止检查，也不会生成任何敏感相关内容。

实体生成 `com.vgosoft.core.annotation.sensitive` 包的 `SensitiveCheck`/策略/内容类型 import；仅实体字段生成规则注解，不在 VO 中复制可被客户端覆盖的策略。

具体 Service 写方法按实际源码生成矩阵标记：

| 方法 | 实体参数索引 | 生成行为 |
|---|---:|---|
| `insert`、`insertSelective` | 0 | 为 opt-in 表装配具体 override 并标记 |
| `updateByPrimaryKey`、`updateByPrimaryKeySelective` | 0 | CAS 关闭时为 opt-in 表装配具体 override 并标记；受管 CAS 开启时旧方法为 `ENTRY_DISABLED` 且不加标记 |
| `updateByPrimaryKeyCas`、`updateByPrimaryKeySelectiveCas` | 0（`expectedVersion` 为 1） | 仅受管 CAS 开启时标记真实单行方法；要求 `(Entity, Integer expectedVersion)`，使用 CAS 已生成的 MANDATORY 事务模板 |
| `insertBatch` | 0 | 配置启用时标记；仅同类型有序 `List<Entity>` |
| `updateBatchByPrimaryKey` | 0 | CAS 关闭时配置启用可标记；CAS 与 Sensitive 同时启用且 update batch 被要求时，生成期拒绝 |
| `insertOrUpdate`、`insertOrUpdateSelective` | 0 | CAS 关闭时按配置标记；受管 CAS 开启时保持 disabled 且不加敏感标记 |
| `updateByExample*` | — | 未证实受限影响范围，生成期拒绝启用该配置；须关闭 `enableUpdateByExample` 后单独评审接入 |
| select/delete/纯关系维护、SQL builder、直接 Mapper/JDBC | — | 不生成敏感标记；非标准内容写入口必须显式适配或限制入口 |

`operationKey = 实体全限定名 + ":" + 实际方法名`，同一输入重生成稳定，长度不超过 256；实体包名/方法名改变时必须重新核对操作注册和未过期确认凭证。CAS 方法仍以 `entityArgumentIndex=0` 标记实体，独立 `expectedVersion` 位于参数索引 1；Sensitive 运行时必须把该参数一并绑定到确认请求。非 CAS 写方法继续按实体或同类型有序 `List<Entity>` 验证参数。重复方法、参数角色不明确或不能被 Spring 代理的受管写方法，以及已启用但未完整装配的方法均生成失败。当前 opt-in 基线按配置/事务模式要求完整装配标准写方法；CAS 模式必须生成两个 CAS 单行更新方法，不得把 disabled 旧方法标记为可写。

CAS 关闭时，每个受标记方法在没有已有事务声明时添加 `@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)`；已有受支持的 REQUIRED/Exception 回滚模板事务声明保留，不重复添加，其他自定义事务声明明确失败并要求评审。受管 CAS 方法保留 CAS 生成器提供的 `MANDATORY`、`rollbackFor = Exception.class` 事务声明，拒绝其他模板。既有缓存失效、关系元数据和事件语句保留，标记只加在具体方法而不是仅接口。事务 Advisor 必须在检测 Aspect 外层；批次原子性、失败结果、缓存提交时序和事件处理仍须宿主集成验收，生成元数据不是已安装敏感运行时。

满足既有 Vue3 TypeScript 类型生成条件时，在 `T{Domain}.ts` 附加只读 `T{Domain}SensitiveFields` 常量，包含 `table/entity/property/label/contentType`，字符串采用 JS 转义。映射仅辅助显示，不授权客户端覆盖后端策略；没有 Vue 目标路径/VO 类型产物时不强行创建前端项目。没有敏感配置的表不改变输出；全部 disabled 时 Service/前端映射保持无配置输出。增量/强制覆盖仍遵守原覆盖规则，标记重建由生成器完成，不能手改受保护产物。

源码职责入口（包路径均相对 `org.mybatis.generator`）：

| 职责 | 源码与调用入口 |
|---|---|
| 配置值及 XML 解析 | `config.SensitiveCheckConfiguration`、`config.xml.MyBatisGeneratorConfigurationParser`；配置对象不承担内省后预检或渲染 |
| 内省后支持范围预检 | `codegen.mybatis3.SensitiveGenerationValidator.validate`，由 `api.AbstractIntrospectedTable.initialize` 调用；模型/Service 插件执行后的渲染再次预检 |
| 唯一跨层共享字段解析 | `codegen.mybatis3.SensitiveFieldResolver`，仅解析启用配置、精确物理列及最终 Java 字段，保持配置顺序和原失败文本 |
| 注解文本和 imports 描述 | `custom.annotations.SensitiveCheckDesc`，从 `SensitiveCheckConfiguration` 读取注解属性；`toAnnotation()` 不修改实例状态，可重复渲染 |
| 实体字段验证、注解装配与插件否决保护 | `codegen.mybatis3.model.SensitiveModelAnnotations`，由 `BaseRecordGenerator` 的模型插件回调后执行；负责匹配实际字段、校验类型和可写性并去重，不承担注解描述渲染 |
| 受管写方法和注解 | `codegen.mybatis3.service.elements.SensitiveWriteMethodsElement`；`JavaServiceImplGenerator` 在关系注解/服务插件前补继承方法，插件通过后沿既有 elements 初始化流程验证实际方法并添加敏感及事务注解，否决时仍显式失败 |
| 前端字段元数据 | `plugins.vue3.Vue3OtherFilesPlugin.frontendFields` 在既有 TS 生成入口填充模板上下文，交由 `vue_module_type.ts.ftl` 转义并渲染；不独立创建前端项目 |

模型注解描述职责调整见[正式任务书第 8 节](task\select-for-update-task.md#8-敏感实体注解描述职责拆分2026-10-14)。仅拆分文本/imports 描述与字段验证/装配，不改变配置、生成产物、方法签名、开关、执行顺序及插件安全门禁；不保留兼容装配空壳。当前 DTD 仍为 `mybatis-generator-config_1_8_0.dtd`，历史 DTD 未修改。

无数据库定向回归入口：

```powershell
Set-Location 'D:\java\idea_projs14\generator\core'
mvn -q -pl mybatis-generator-core "-Dtest=SensitiveGenerationTest,CacheConfigurationResolverTest,CacheEnableAnnotationTest,JavaBeansUtilTest" "-Denforcer.skip=true" test
```

测试使用内存列元数据、实际 Model/Service 渲染、当前 DTD 和 TS 模板；注解消费编译也在内存完成。不运行业务生成器、不连接数据库、不写业务产物。业务受控再生成及宿主拦截/事务验证另行授权。

### 3.2 独立锁定查询（1.8.0）

本节是 `enableSelectForUpdate` 配置及消费契约入口；按第 1.1 节匹配实际 core 与当前 DTD。该 table attribute 默认为 `false`，严格接受小写 `true/false`，不修改历史 DTD。

```xml
<table tableName="dictionary_" domainObjectName="Dictionary"
       enableSelectForUpdate="true"
       enableSelectByPrimaryKey="true" enableSelectByExample="true">
    <generateModel generate="true"/>
    <generateSqlMap generate="true"/>
    <generateDao generate="true"/>
    <generateService generate="true"/>
</table>
```

Context 的有效运行时必须由工厂解析为标准 `IntrospectedTableMyBatis3Impl`（可省略 `targetRuntime`，显式内置名称忽略大小写，也可直接指定该标准实现的完整类名），且使用 `javaClientGenerator type="XMLMAPPER"`，配置模型、XML、DAO 和 Service 输出。预检使用实际工厂解析而非原始字符串精确比较；Simple/DynamicSql/Kotlin 虽然继承该基类，其实际生成链路不同，仍拒绝锁查询。自定义派生实现继续可用于普通生成，但不自动授予锁查询输出契约。支持可变 flat/conditional/hierarchical 模型，含复合主键、主键类和 BLOB 类；immutable、model-only、其他 runtime/Client 类型、缺失任一必需生成输出均分别失败，例如 `必须启用 Mapper/DAO 输出（generateDao）`、`必须启用 Service 接口输出（generateService）`，错误均带表名及 `enableSelectForUpdate`。启用主键查询却无主键、启用 Example 查询却不能生成 Example、两个普通查询开关都关闭时也失败，不静默漏生成锁方法。

#### 3.2.1 方法签名与产物

设完整查询实体类型为 `Record`（生成 BLOB 类时使用实际 `RecordWithBLOBs`），Example 为 `RecordExample`，主键类型为 `K1/K2` 或 `RecordKey`：

| 模型 | Mapper | 生成 Service 接口及实现 |
|---|---|---|
| 单主键 | `Record selectByPrimaryKeyForUpdate(K1 id)` | `ServiceResult<Record> selectByPrimaryKeyForUpdate(K1 id)` |
| 复合主键，无主键类 | `Record selectByPrimaryKeyForUpdate(@Param("key1") K1 key1, @Param("key2") K2 key2)` | `ServiceResult<Record> selectByPrimaryKeyForUpdate(K1 key1, K2 key2)` |
| 生成主键类 | `Record selectByPrimaryKeyForUpdate(RecordKey key)` | `ServiceResult<Record> selectByPrimaryKeyForUpdate(RecordKey key)` |
| Example | `List<Record> selectByExampleForUpdate(RecordExample example)` | `ServiceResult<List<Record>> selectByExampleForUpdate(RecordExample example)` |

实际参数名/类型以列内省结果为准。主键锁方法受普通 `enableSelectByPrimaryKey` 控制，Example 锁方法受普通 `enableSelectByExample` 控制；普通方法和 SQL 保持原样。独立 XML 返回普通基础/BLOB resultMap，不使用关联扩展 resultMap，避免通过关联查询隐含扩大锁查询范围。

额外生成 DAO `codegen` 包中的 `Gen{Domain}MapperLockQueryGuard`，所有产物需要一起部署。Service 方法与 XML bind 均调用该门禁；它是生成器维护的产物，不得人工修改。具体方法带中文参数、返回、事务/异常和缓存说明。

#### 3.2.2 事务、条件、缓存及分页

- 具体 Service 方法声明 `@Transactional(propagation = Propagation.MANDATORY, rollbackFor = Exception.class)`；锁在调用方既有事务结束时释放，不新建事务、不改变其隔离级别。门禁同时检测实际事务是否开启以及 `isCurrentTransactionReadOnly`，无事务或 read-only 时抛 `IllegalStateException`。宿主必须通过 Spring 代理并将目标 Mapper 路由到该事务的同一数据源；线程事务标志不能验证跨数据源路由、连接真实事务及被代理方法的自调用。
- Mapper 返回实体/列表，Service 仅以 `ServiceResult.success(...)` 包装正常返回；数据库异常直接向外传播并由事务处理，不能捕获后返回失败假装成功提交。
- Example 必须非空且至少有一个有效非空条件组。不接受 `distinct=true`、任何非 null `orderByClause`（包括空字符串）、原始 SQL 条件或空列表 `IN/NOT IN`。条件仅接受内省非 BLOB 列与标准 `is null/is not null/=/<>/>/>=/</<=/like/not like/in/not in/between/not between` 的精确组合，列别名和分隔符按生成 Example 一致处理。业务使用标准生成 Criteria 方法；禁止通过自定义 Example/Criteria 子类、可变条件并发修改或插件替换 where 片段来规避门禁。
- 非空条件不保证锁范围小，不自动添加租户、逻辑删除或数据权限约束；业务负责明确条件、授权、索引、行数上限与锁顺序。禁止以 `is not null` 等宽泛条件当作“已自动限制行数”。
- XML `useCache=false` 禁用 MyBatis 二级查询缓存，`flushCache=true` 在每次执行前清理既有一级/二级缓存，防止普通查询缓存命中跳过数据库锁。Service 无 `Cacheable` 等缓存注解；宿主自定义缓存/查询替换拦截器须另行验收。
- XML bind 也校验门禁，直接 Mapper 调用不能绕过事务或条件校验。`PageHelper.getLocalPage()!=null` 时明确拒绝，包括 `startPage` 与 `orderBy` 的线程状态，不自动清除调用方状态。调用方完成/清理原分页作用域后，才可调用锁定查询。生成方法不含分页参数，不生成 RowBounds 变体；使用原始 SqlSession 显式 RowBounds、其他分页插件/自定义拦截器不属于受支持调用契约，不得将此边界理解为通用插件安全保证。

#### 3.2.3 方言与隔离边界

| 方言 | 生成锁语法 | 已完成验证 |
|---|---|---|
| MySQL、MariaDB、PostgreSQL、Oracle、H2 | 查询末尾 `FOR UPDATE` | 离线生成字符串分支与 `databaseId` 检查；MySQL 分支完成实际 MyBatis XML/动态 SQL 解析 |
| SQL Server、SQL Server 2005 | 表引用 `WITH (UPDLOCK, HOLDLOCK)` | 离线生成及 SQL Server 分支实际 MyBatis XML/动态 SQL 解析 |
| 其他 `DatabaseType`，含 SQLite、OTHER | 无通用等价回退，明确拒绝 | 遍历枚举全部拒绝分支；不借用受管关系先 INSERT 获取 SQLite 写锁的特殊实现 |

预检遍历 Context/table 合并并由内省加入当前连接方言后的完整 `additionalDbTypes`。每个受支持方言产生明确 `databaseId`，消费方须配置匹配 `DatabaseType.code` 的 MyBatis databaseIdProvider；不提供没有 databaseId 的危险默认分支。配置任一未核对方言会在生成前失败，并提示表及方言。

此处验证的是生成语法与执行门禁，不是数据库服务器实测。MySQL/InnoDB 锁定读不能推广为所有方言 REPEATABLE READ 均返回最新提交版本；例如 PostgreSQL REPEATABLE READ 遇到并发版本变化可能抛序列化失败。SQL Server HOLDLOCK 也可能影响锁范围及隔离语义，不等同于其他数据库的行锁行为。业务必须在实际驱动、隔离级别、索引和数据源事务配置下验证竞争、超时、死锁、可见版本及重试策略。

#### 3.2.4 验证与再生成

正式离线 suite：`SelectForUpdateGenerationTest`，15 项用例覆盖 DTD/无 DTD Parser、默认关闭与普通产物不变、配置失败、所有方言预检、复合主键/主键类/BLOB、中文契约、JavaCompiler 内存编译、真实 Spring 代理的非只读既有事务、原始条件/分页拒绝、真实 PageInterceptor，以及 MyBatis XML/databaseId 和无数据库执行器的一级/二级缓存绕过。另验证 Rules 的全部模型实现、代理否决传递、独立元素接入、插件默认兼容及短路顺序，并以重构前真实输出的 15 个 SHA-256 验证三种模型的五层生成文本（仅统一换行）保持一致。过程与命令记录见[实施任务书](task\select-for-update-task.md)。

升级实际执行 core 后，业务消费者先评审需要启用的表和方言，再用当前 DTD 受控重新生成 Model/Example（按需）、Mapper、XML、Service 和新增门禁类。不得只手改业务生成 XML 或遗漏门禁类；已取消完整覆盖标记的人工 Service 扩展仍需人工按新契约适配。该扩展不自动替换业务 revision 路径，不执行业务生成、DDL、权限、菜单或部署。

架构入口：

- `Rules.generateSelectByPrimaryKeyForUpdate/generateSelectByExampleForUpdate` 统一生成判断；接口默认实现保持既有 Rules 实现兼容，`RulesDelegate` 传递独立锁方法否决并尊重代理的普通查询限制。
- `JavaMapperGenerator` 初始化并执行 `SelectByPrimaryKeyForUpdateMethodGenerator`、`SelectByExampleForUpdateMethodGenerator`；`XMLMapperGenerator` 按既有方言顺序初始化对应两个 `*ElementGenerator`，共用 `AbstractSelectForUpdateElementGenerator` 的 XML 结构。
- `JavaServiceGenerator` 经 `ServiceMethods` 获取签名；`JavaServiceImplGenerator` 通过 `AbstractServiceElementGenerator` 的初始化流程执行 `SelectByPrimaryKeyForUpdateElement`、`SelectByExampleForUpdateElement`。服务实现仍只添加强制事务、门禁及 Mapper 调用，不复用写入或批处理回调。
- `org.mybatis.generator.codegen.mybatis3.SelectForUpdateValidator` 仅负责内省后完整配置预检，保留生成侧而非配置模型层；`org.mybatis.generator.dbstrategy.SelectForUpdateDialect` 维护预检与 XML 共享的锁 SQL 分组；`SelectForUpdateGuardGenerator` 仅维护门禁产物及稳定命名。签名、实际模型和参数逻辑归 `ServiceMethods`，不存在跨层集中装配器。配置入口仍为 `TableConfiguration` 与 `MyBatisGeneratorConfigurationParser`。

锁定查询专用插件回调为 `clientSelectByPrimaryKeyForUpdateMethodGenerated`、`clientSelectByExampleForUpdateMethodGenerated`、`sqlMapSelectByPrimaryKeyForUpdateElementGenerated`、`sqlMapSelectByExampleForUpdateElementGenerated`。均为 `Plugin` 默认返回 `true` 的方法，由 `CompositePlugin` 按配置顺序传播，首次 `false` 短路；两个 XML 回调逐方言执行。普通 select 回调不接收锁定方法，避免已有缓存/分页插件改变锁查询契约，也不借用 update/batch 回调；既有整接口/整文档/整服务生成回调不变。插件主动否决时需自行协调 Mapper/XML/Service 产物，保持事务、缓存、条件及分页门禁，不以单层删方法替代完整生成配置评审。

## 4. 列级配置

**实践入口：** 字段类型、长度、默认值与页面/业务行为应联合评审，不能只填 XML 属性。按[第 20 章](#20-约定驱动配置与实践建议)选择复用约定、显式配置或扩展；字段命名及 `apply_date` 的分层默认值事实统一见[数据库规范 §4.5、§4.6](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-core-db\src\main\resources\docs\database-authoritative-spec.md#45-面向生成与业务使用的字段设计实践)。

| 节点 | 含义 | 真实行为 |
|---|---|---|
| `columnOverride` | 覆盖列元数据 | 可改 `property`、`javaType`、`jdbcType`、`typeHandler`、长度、注释、对齐。 |
| `ignoreColumn` | 忽略单列 | 不出 SQL / 不出属性。 |
| `ignoreColumnsByRegex` | 正则忽略列 | 支持 `except` 白名单。 |
| `generatedKey` | 生成列策略 | 适合序列/identity。 |

### 4.1 文本列的逻辑 JSON 契约（Jackson 3）

物理 TEXT/VARCHAR 列如承载现有 JSON 展示契约，使用已有 `columnOverride/property` 扩展配置：

```xml
<columnOverride column="json_content">
    <property name="logicalType" value="JSON"/>
</columnOverride>
```

`logicalType` 当前仅接受 `JSON`（大小写不敏感），其他非空值明确报错。若 Java 类型为 `String`，保留现有 `JsonRecordSerializer` 序列化契约，不增加数据库 TypeHandler；若 Java 类型为 Jackson 3 `JsonNode` 或 DTO，生成器配置 `JacksonJsonTypeHandler`，由 MyBatis 按类型读写 JSON 文本。未配置的普通 TEXT 列不自动视为 JSON；物理 JSON 列保留自动识别。日期格式注解仍使用官方 `com.fasterxml.jackson.annotation.JsonFormat`。

### 4.1.1 跨方言结构化 JSON 字段

结构化 JSON 使用明确的 Java 类型和 JDBC 文本类型，并通过 columnOverride 现有属性接入生成器：

```xml
<columnOverride column="payload"
                javaType="tools.jackson.databind.JsonNode"
                jdbcType="LONGVARCHAR">
    <property name="logicalType" value="JSON"/>
</columnOverride>
```

非 `String` JSON 列缺省绑定 `com.vgosoft.mybatis.typehandler.JacksonJsonTypeHandler`；显式 `typeHandler` 可覆盖默认值。该 Handler 通过 Jackson 3 编解码，并将畸形 JSON 作为 JDBC 映射错误抛出。`String` 字段保持既有行为，不会自动转换为树对象。

JSON Schema 输出按目标 `DatabaseType` 将逻辑 JSON 映射到文本型列，例如 MySQL 系列 `LONGTEXT`、PostgreSQL 系列 `TEXT`、SQL Server `NVARCHAR(MAX)`、Oracle/DM/DB2 系列 `CLOB`。其他未能分类的 `OTHER` 必须配置 `jsonStorageType`，例如：

```xml
<columnOverride column="payload" javaType="tools.jackson.databind.JsonNode" jdbcType="LONGVARCHAR">
    <property name="logicalType" value="JSON"/>
    <property name="jsonStorageType" value="CLOB"/>
</columnOverride>
```

`DatabaseIntrospectorFactory` 为全部枚举 `DatabaseType` 提供 DDL profile；未具备专用 SQL 方言实现的类型使用通用 DDL 模板，目标数据库仍须以其厂商文档/隔离环境验证输出脚本。文本存储的基础读写不代表具备原生 JSON 运算符、路径查询或索引支持；这些能力需另行按方言验证。

对于保留既有字段/注解及业务扩展的局部迁移，测试源码中的 `org.mybatis.generator.codegen.mybatis3.vo.OfflineJsonVoGenerator` 提供无连接的离线 bootstrap 入口：

```text
java -cp "<generator test classpath>" org.mybatis.generator.codegen.mybatis3.vo.OfflineJsonVoGenerator <generatorConfig.xml> <schema.sql> <existingVo.java> <stagingVo.java>
```

它只读取单表 `CREATE TABLE IF NOT EXISTS` schema 文本及列级配置，以带生成标记的已有 VO 作为扩展模板，通过同一插件重新生成 JSON 序列化注解。它不调用 JDBC，不执行 SQL，不解析外部 DTD，不执行配置中的前端/SQL/其他生成开关。模板字段必须能匹配 schema，配置的逻辑 JSON 列必须全部匹配单独声明的 String 字段及已有 `JsonRecordSerializer` 契约；自定义序列化器、缺列、未匹配配置均拒绝覆盖。

输出必须使用不同于输入的 staging 文件。应用前必须比较差异并检查源文件未被并发修改；非序列化注解、字段、方法、长度/展示规则、生成标记保持原样。此入口是局部契约再生成，不是全模型生成，也不能用于把历史 VO 元数据宣称为当前物理 schema；它不能重建缺失的业务扩展模板。

### 4.2 授权业务扩展快照的 Jakarta 再生成

只有明确授权当前完整业务实现作为离线模板时，才可使用测试源码入口 `org.mybatis.generator.codegen.mybatis3.vo.OfflineJakartaTemplateGenerator`。它使用 JavaParser 保留词法布局、Java DOM 类型输出，仅支持显式 `javax.annotation.Resource`、`javax.validation.constraints.AssertTrue` import 迁移到对应 Jakarta 类型；保留方法体、注解参数和生成标记，不改 JDK javax 类型、JSR305、字符串或注释。

```text
java -cp "<generator test classpath>" org.mybatis.generator.codegen.mybatis3.vo.OfflineJakartaTemplateGenerator stage <businessRoot> <snapshotRoot> <stagingRoot> <manifest.properties>
java -cp "<generator test classpath>" org.mybatis.generator.codegen.mybatis3.vo.OfflineJakartaTemplateGenerator apply <manifest.properties>
```

`stage` 只处理 `src/main/java` 下具有生成标记且引用上述类型的源码，创建完整快照、生成结果及 SHA-256 manifest；输出目录必须与业务目录隔离，不覆盖已有快照。检查 staging diff 后才能运行 `apply`：它重新验证整个批次的源文件、快照、结果哈希，并重新执行生成验证后受控复制。源文件被其他流程修改时拒绝覆盖。此流程不连接数据库、不执行 XML 配置、不生成 Mapper/SQL/前端；普通生成模板继续采用现有 Jakarta 类型输出。

## 5. Java 模型 / VO

### 5.0 使用生成 Entity 注解读取表与列元数据

设计或开发阶段若要从已有实体确认“实体对应哪张物理表、Java 属性对应哪个物理列、列上记录了哪些描述/建模属性”，优先检查具体生成 Entity 的 `@TableMeta` 与字段 `@ColumnMeta`，无需仅凭类名、属性名或 VO 命名推测。属性的完整含义、当前生成来源与限制见[生成器权威说明 §2.1.1](generator-authoritative-spec.md#211-实体物理表与列元数据注解)。

- `entityClass.getDeclaredAnnotation(TableMeta.class)` 获取该 Entity 声明的表元数据；`value()` 为物理表名，`alias()` 为生成配置中的表别名。优先读取声明注解，避免 `@Inherited` 将父类表注解误用于具体实体。其余 `descript`、`beanname`、`summary`、`voClass`、`superClass`、`superInterfaces` 是生成器提供的描述或模型信息，按字段语义使用。
- `ColumnMeta` 位于字段，不是类注解。遍历具体 Entity 及各父类的 `getDeclaredFields()`，每个带 `ColumnMeta` 的字段以 Java 字段名作为 property、以 `ColumnMeta.value()` 作为物理列名；`type()`、`nullable()`、`size()`、`scale()`、`pkid()`、`description()`、`remarks()` 等提供已生成的列描述。`ColumnMeta` 上的 `@Inherited` 不会继承字段注解，故不能只扫描子类声明字段。
- 有基础工具依赖时，可复用 `VReflectionUtil.getAnnotationFields(entityClass, ColumnMeta.class)` 获取类及父类的注解字段；否则按上述层级遍历实现。对需稳定顺序的场景按 `order()` 排序并补物理列名/property tie-break；`order` 是元数据顺序，不保证 SQL 参数顺序。
- 读取前确认目标为持久化 Entity，并检查 `TableMeta` 缺失、重复物理列映射和父类映射完整性。生成的 VO 字段也可能带 `ColumnMeta`，但通常没有类级 `TableMeta`，不能把 VO 当作表身份入口。

JDK 反射的最小读取形状如下；正式实现还应校验注解存在、重复映射及字段是否属于持久化模型：

```java
TableMeta tableMeta = entityClass.getDeclaredAnnotation(TableMeta.class);
if (tableMeta == null) {
    throw new IllegalStateException("实体缺少 TableMeta");
}
for (Class<?> type = entityClass; type != null && type != Object.class; type = type.getSuperclass()) {
    for (Field field : type.getDeclaredFields()) {
        ColumnMeta columnMeta = field.getDeclaredAnnotation(ColumnMeta.class);
        if (columnMeta != null) {
            String property = field.getName();
            String physicalColumn = columnMeta.value();
        }
    }
}
```

适用场景包括：需求/设计阶段盘点既有 Entity 的表列映射和列说明；实现通用的实体元数据展示或依据持久化字段作字段级处理；检查某实体是否具备所需列描述。基础包已有 `AbstractMybatisBGService` 作为复用实例：查询构建器未显式指定时，从实体注解取得表与字段默认映射。若类/字段缺少注解（例如在 `generateModel` 上配置 `noMetaAnnotation=true`），明确转查生成配置、生成源码或数据库权威元数据，不从 Java 命名规则猜测。

**能力边界：** 这些注解是表/列元数据快照，不是完整生成配置或数据库 schema。它们不能单独说明 catalog/schema、索引/唯一约束、外键、identity/generated-always、所有 table/context 开关、最终 SQL、`databaseId`、插件变更或 Mapper statement 的 allow/reject 执行规则。判断要生成哪些文件/方法、实际写入列、CAS/逻辑删除行为、SQL 执行许可时，仍须检查实际 `generatorConfig.xml`、生成产物及运行契约；判断数据库真实结构或目标环境状态时，仍以 DDL/数据库权威来源与获准核验为准。页面展示/编辑/过滤/排序布局应读 View VO 上的 `ViewTableMeta`/`ViewColumnMeta` 与视图配置，不从 Entity 的 `ColumnMeta` 推导。

| 节点 | 含义 | 备注 |
|---|---|---|
| `generateModel` | 实体类生成 | `enableChildren` 依赖 `parent_id`。 |
| `generateVo` | VO 总控 | 控制 `modelVo/createVo/updateVo/viewVo/excelVo/requestVo`。 |
| `modelVo` | 查询/展示 VO | 默认排除抽象父类字段。 |
| `createVo` | 新增参数 VO | 可指定必填列、选择性创建。 |
| `updateVo` | 更新参数 VO | 可指定必填列、选择性编辑。 |
| `requestVo` | 查询参数 VO | 支持分页参数和查询片段。 |
| `viewVo` | 列表/详情 VO 与视图元数据 | 控制 ViewVo 字段及列表列、查询/过滤、默认排序和菜单元数据；详见第 5.3 节。 |
| `excelVo` | 导入导出 VO | 导入字段与导入流程见第 5.1 节，导出字段与接口见第 5.2 节。 |
| `excelImport` | 两阶段 Excel 导入工作流 | 独立 opt-in；资源限制、预览/确认端点及业务 Service 钩子见第 5.1.1 节。 |
| `generateCachePo` | 缓存 PO | 常用于字典值映射。 |
| `overridePropertyValue` | 值转换字段 | 生成 `Text`/展示字段或注解映射。 |
| `additionalProperty` | 附加属性 | 给 VO 增加新字段。 |

### 5.1 `excelVo` 导入字段与生成行为

`excelVo` 是导入与导出的共同开关，不是独立的“仅导入”开关。必须同时启用父级 `<generateVo generate="true">`；`excelVo generate="false"` 时，导入/导出 VO、导入模板、导入端点及导出端点均不生成。首次配置时建议显式写出 `generate` 和导入字段范围：

```xml
<generateVo generate="true">
    <excelVo generate="true"
             importIncludeColumns="code,name,category_id"
             importIgnoreFields="id,version"/>
</generateVo>
```

`importIncludeColumns` / `importExcludeColumns` 使用数据库物理列名；`importIgnoreFields` 使用 Java 属性名。它们与 `includeFields`、`excludeFields`、`ignoreFields` 不同，后者控制导出 Excel VO：

| 属性 | 作用 | 生效规则 |
|---|---|---|
| `importIncludeColumns` | 指定导入 VO 的数据库列集合 | 非空时只按所列物理列生成导入 VO 字段，并按配置顺序处理；不存在的列不生成。全局 VO 排除列仍然生效，不能通过 include 重新加入。 |
| `importExcludeColumns` | 从导入 VO 排除数据库列 | 仅在 `importIncludeColumns` 为空时生效；配置 include 后此项不再筛选。 |
| `importIgnoreFields` | 标记不参与 Excel 导入的导入 VO 属性 | 使用 Java 属性名；生成字段仍保留在 VO 中。启用 `EasyExcelAnnotationPlugin` 时，字段添加 `@ExcelIgnore`；生成的模板样例也跳过这些属性。它不等同于从 Entity 映射或数据库写入列中移除字段。 |

除上述筛选外，`generateVo` 的全局排除列仍参与导入字段筛选；`excelVo` 自身的附加属性配置也可能增加导入 VO 字段。修改配置后应检查实际生成的 `ExcelImportVo`、模板字段和 mappings，不仅依据配置文本推断最终字段集合。

启用后，生成器在 Controller 基础路由下提供 `GET import/template` 和 `POST import`，并生成 `<Entity>ExcelImportVo`、模板样例方法及 VO/Entity 映射。模板样例方法可在生成的可扩展 Controller 子类中重写；导入监听器工厂 `getImportReadListener` 也可在子类扩展。以上扩展点不改变默认导入流程的事务或业务语义。

**默认导入边界：** 当前生成的 `POST import` 保存上传文件到本地 `upload` 路径后解析 Excel，再将行映射为 Entity 并逐条调用 `insertOrUpdate`；成功数按实际成功项计数。因此它可能更新已有数据，且代码没有为整份文件提供全有或全无事务保证，某些行成功、其他行失败时已成功行不会因该接口而自动回滚。默认配置也没有通过上述字段属性定义预览/确认、仅新增、业务级重复检查、文件/行数限制或预览过期控制；不能把通用导入端点直接当作这些业务契约的实现。需要预览后确认、严格整批原子性或特定业务校验时，应先设计可验证的共享生成器扩展契约和业务实现，不要仅开启 `excelVo`。

### 5.1.1 显式两阶段 Excel 导入工作流

`excelImport` 是与 `excelVo` 分离、默认关闭的导入工作流配置。启用时仍须启用父级 `<generateVo generate="true">`，并生成 Java Controller、Service 及 Service 实现；当前 `mybatis-generator-config_1_8_0.dtd` 中的全部资源限制均为必填正数。`maxInputBytes` 不能超过 `Integer.MAX_VALUE`，`maxEntryBytes` 不能超过 `maxExpandedBytes`：

```xml
<generateVo generate="true">
    <excelImport generate="true"
                 importIncludeColumns="code,name,category_id"
                 importIgnoreFields="id,version"
                 maxInputBytes="10485760"
                 maxExpandedBytes="52428800"
                 maxEntryBytes="10485760"
                 maxZipEntries="256"
                 maxRows="1000"
                 maxErrorMessages="100"/>
</generateVo>
```

限制值必须由使用方根据上传并发、内存预算和业务容量显式确定；示例值不是通用安全默认值。`VgoFesodSheet` 会按原始文件字节上限暂存文件，因此 `maxInputBytes` 同时是单个导入文件的内存上限。ZIP 展开字节、单条目字节和条目数限制作用于 XLSX 预检；数据行数和解析错误条数由有界读取器限制。当前 Excel 插件的有界接口仅接受 XLS/XLSX 文件签名。

`importIncludeColumns` / `importExcludeColumns` 使用数据库物理列名，`importIgnoreFields` 使用 Java 属性名；筛选及全局 VO 排除规则与 `excelVo` 导入 VO 一致。`excelImport` 生成独立工作流使用的 `<Entity>ExcelImportVo`、映射方法、导入模板和两个 Controller 端点：`POST import/preview`（multipart 文件）与 `POST import/confirm`（JSON 确认凭证）。两个端点沿用导入权限及系统日志声明。业务层必须依赖提供 `VgoFesodSheet.readBounded` 和 `com.vgosoft.plugins.excel.pojo.vo` 导入 DTO 的 Excel 插件版本。

生成的 Service 接口包含 `previewExcelImport(MultipartFile)` 与 `confirmExcelImport(VgoExcelImportConfirmVo)`。生成的 Service 基类在预览入口按配置构造 `ImportReadLimits` 并调用有界解析，再委托 `createExcelImportPreview(rows, fileSha256)`；确认入口使用 `READ_COMMITTED`、`rollbackFor = Exception.class` 的事务包装，并委托 `doConfirmExcelImport(request)`。两个受保护业务钩子默认明确抛出 `UnsupportedOperationException`，业务子类必须实现预览校验/凭证签发与确认提交逻辑。只重写受保护钩子，不要重写公开事务包装方法；确认钩子必须在失败时抛出异常，以使整批业务写入回滚。事务仍依赖通过 Spring 代理从外部调用，生成器不替业务实现预览所有权、令牌过期/一次性消费、revision 校验或具体数据写入。

未配置或关闭 `excelImport` 时，既有 `excelVo` 输出不变。若同时启用 `excelVo` 和 `excelImport`，Excel 导出能力仍由 `excelVo` 提供，但旧的逐行 `POST import` 被抑制，避免同一资源同时暴露不具备事务保证的直接导入和两阶段工作流。

### 5.2 个性化导出接口与接入

当规则启用 `excelVo` 时，`ExportElementGenerator` 为单列 `String` 主键表生成以下端点（均相对于 Controller 基础路由）：

| 方法/路径 | 输入/输出 | 兼容性 |
|---|---|---|
| `GET export` | 原 `ids` 列表，旧 Excel 下载 | 保留原调用；主键属性从内省获取，不写死 `id` |
| `GET export/options` | `ResponseResult<VgoExportOptionsViewVo>`，包含 columns、sortFields、formats、maxRowsByFormat、defaultMaxRows、scopes、maxScopeIds（500） | 与 POST 共享原 `export` 权限、系统日志和 OpenAPI 声明 |
| `POST export` | `VgoExportRequestVo<该表实际列表查询类型>`；响应为文件流 | 同时验证 Default/ValidateQuery 组并沿公共 VO 的 `@Valid` 级联；`query` 和 `options` 必填且再次显式判空；新增接口，不改变列表接口 |

宿主须升级到提供 `com.vgosoft.plugins.excel.pojo.vo` 导出 VO、`ExportSupport` 与 `VgoFesodSheet.export(response, options, ExcelVo.class, Iterable<List<ExcelVo>>)` 的 Excel 插件。生成器只组装查询、内省白名单与批次，不实现格式编码；格式与列 schema 以[Excel 插件权威说明](D:\java\idea_projs6\vgosoft-plugins\vgosoft-excel\src\main\resources\docs\vgosoft-excel-authoritative-spec.md)为准。

- `query` 按 RequestVo → modelVo → entity 的现有列表规则选择；存在 `filterParam` 时沿用 POST 列表的解析入口。执行 `buildExample` 前清除 `anyWhereCondition/orderByClause`，并将已有权限/删除绕过字段设为 `false`；构造后的 Example 也强制设为 `false`。业务子类重写 `buildExample` 必须继续维护授权与逻辑删除语义。
- 个性化导出继续使用不含 BLOB 列的 `selectByExample`；内省归类为 BLOB 的列（包含 LONGVARCHAR）及 Excel 配置中以其为来源的派生字段从接口元数据中排除，请求这些字段会在查询前报错。实际允许列是公共 Excel schema 与生成的可查询字段限制的交集；不得仅依据 ExcelVo 注解承诺大字段已查询。旧 GET Excel 接口不扩展为大字段加载接口。
- `options.scope` 为 `all/page/selected`；页面、选中范围分别传 `pageIds/selectedIds`。元数据 `maxScopeIds=500` 明确约束 ID 范围为 1–500 个不重复主键（还须满足格式配置上限），客户端不能用 `maxRowsByFormat` 推导选中 ID 上限；全部 OR 分支追加同一主键约束。在同一数据库查询内执行白名单排序，不分块后用 Java 排序，避免改变数据库排序规则。先查询并核对全部 ID，缺失、删除或不可见记录直接报错，不导出剩余子集；无自定义排序时恢复客户端 ID 顺序，再应用 `maxRows`。该 ID 上限限制单次 IN 参数量，不代替宿主对其他复杂筛选参数总量的数据库限制。
- 多个 OR 分支会重复绑定 ID 参数；生成器额外检查 `ID 数 × OR 分支数 <= 1000`，超出则在查询前明确报错，不通过截断 ID 或改变条件范围降级。任意自定义复杂筛选仍须由宿主约束其他参数数量。
- `options.sorts` 只接收属性名和方向，不接收 SQL。属性映射到内省获得的物理列（保留别名与配置分隔符），由公共 helper 验证方向和生成 ORDER BY；默认顺序取 sort/create_time/update_time 列，并追加主键，保证分页稳定。
- `maxRows` 默认值由公共 VO 提供（10000）；PDF 请求必须主动设为不超过 5000，其他格式不超过 50000。校验发生在查询前。`all` 每批最多 500 行，通过 PageHelper 分页，不生成方言专属 LIMIT；输出必须在当前请求同步消费批次。查询异常向外抛出，每次查询与输出退出均清理 PageHelper 线程上下文。此机制不是异步导出任务，也不承诺并发修改期间的数据库快照一致性。
- 无主键、复合或非字符串主键表不会生成这些导出端点，生成日志给出警告；不得用固定 `id` 回退。现有 Vue3 主列表模板使用框架 `VgoTableView`，导出上下文由框架统一收集；不手改已生成的 UI 或新增旧版 `ServiceApi.exportSelect` 调用。
- 旧完整列表模板 `vue_module_list.all.vue.ftl` 给 `vgo-table` 显式绑定 `exportBasePath=restBasePath`；该路径来自表配置，不能从 `apiObj` 函数反推。框架处理 `top-custom-export` 时通过 `tableRef.openExport(basePath?)` 打开选择器，空选择仍能打开并选择全量/当前页范围，模板不添加“必须选中行”的前置判断。框架统一收集当前 query、pageIds、selectedIds；导出计划资源键使用去掉首尾斜杠的标准资源路径。

回归入口：`mvn -pl mybatis-generator-core "-Dtest=ExportElementGeneratorTest" "-Denforcer.skip=true" test`，覆盖类型回退、主键边界、SQL/权限清理、批次与实际渲染 Java 的内存编译。

跨项目 API 兼容验证可为同一测试传入 `-Dexport.contract.classes=<Excel 模块已编译 classes 目录>` 与 `-Dexport.contract.classpath-file=<Excel dependency:build-classpath 输出文件>`。此模式移除插件契约替身，将实际渲染 Controller 直接对当前编译的 `VgoExportRequestVo/OptionsVo/OptionsViewVo`、`ExportSupport` 与 `VgoFesodSheet` 类型检查；常规独立运行仍使用内存契约替身，不要求另一仓库存在。

### 5.3 `viewVo` 默认列表视图配置

`viewVo` 必须放在 `<generateVo>` 内。当前配置构造器默认关闭它；要生成 ViewVo，必须显式写 `generate="true"`，并同时启用父级 `<generateVo generate="true">`。启用后生成的是后端 ViewVo 和视图元数据，不等同于 Vue 页面布局，也不会单独实现自定义查询、按钮服务端逻辑或权限。

#### 5.3.1 主要属性

| 属性 | 配置作用 | 设计/使用要点 |
|---|---|---|
| `generate` | 启用 ViewVo；缺省为 `false` | 必须同时满足父级 `generateVo` 启用；既有文件是否跳过/强制更新还受生成策略影响 |
| `includeColumns` / `excludeColumns` | ViewVo 可用字段范围 | 与父级 `generateVo` 及其他 VO 字段规则共同影响最终字段；显式检查生成类，不从 Entity 或其他 VO 的字段列表推断 |
| `listKey` | 列表配置标识 | 当前 `ViewMetaAnnotationPlugin` 不会将它写入默认 `ViewTableMeta`，也不改变默认 `value` 视图 ID；只有确认具体消费方支持时才配置 |
| `title` | 生成菜单标题 | 未配置时菜单标题回退到表备注；`ViewMetaAnnotationPlugin` 不会将其写入默认 `ViewTableMeta` |
| `size` | 列表大小 | DTD/Parser 接受此属性，但当前 `ViewMetaAnnotationPlugin` 不会将其传入默认 `ViewTableMeta`；确认具体消费方后再依赖其效果 |
| `indexColumn`、`indexColumnFixed` | 行选择/序号列及固定方式 | `indexColumn` 会进入视图元数据；固定方式以当前 DTD 和目标 UI 框架消费逻辑核验 |
| `toolbar`、`actionColumn`、`actionColumnFixed`、`actionColumnWidth` | 工具栏及行操作列 | 标准动作需与生成接口匹配；自定义 ID 需有按钮描述、前端 handler、服务端动作和权限实现 |
| `queryColumns`、`fuzzyColumns`、`filterColumns` | 复合查询、模糊查询和过滤字段集合 | 与 RequestVo 和后端列表查询实现一致；需细配的字段使用对应子节点 |
| `defaultDisplayFields`、`defaultHiddenFields` | 默认显示/隐藏字段 | 使用 ViewVo 属性名；控制列元数据的顺序/默认显示标志，核验生成注解和运行时实际可见性 |
| `defaultSort`、`totalRow`、`totalFields`、`totalText` | 默认排序及合计行 | 排序字段应映射为支持的列；合计字段需为适合聚合的字段 |
| `tableType`、`uiFrameType` | 表格类型与 UI 框架类型 | 使用当前 DTD 枚举值；不能只因某个示例设置就推定其他宿主具备相同组件 |
| `categoryTreeUrl`、`categoryTreeMultiple` | 分类树接口及多选 | 确认接口、参数、权限及宿主对分类树元数据的消费方式 |
| `defaultFilterExpr` | 默认过滤表达式 | 属于生成器/后端查询表达式，不是可信客户端权限条件；核验别名、列名、占位符及方言 |
| `parentMenuId`、`viewMenuIcon`、`viewMenuElIcon`、`enableShortcut` | 默认视图菜单元数据 | 菜单只在完整菜单生成条件满足时追加；父节点必须已存在并纳入初始化链路 |
| `editFormIn`、`detailFormIn` | 编辑和详情的打开方式元数据 | 按 DTD 的 `inner` / `dialog` / `drawer` 配置，并与宿主组件支持方式一致 |

属性解析成功不等于进入默认视图注解。当前 `ViewMetaAnnotationPlugin` 显式传递 `tableType`、合计行、表单打开方式、`showActionColumn`、`defaultSort`、`indexColumn`、查询/过滤与按钮描述、分类树 URL、包含/排除列、类名及路由；默认显示及列顺序写入字段级 `@ViewColumnMeta`。`listKey`、`title`、`size`、`enablePager`、固定列属性、`showRowNumber` 等没有被该插件传入默认 `ViewTableMeta`；`title` 会独立用于生成菜单标题。不要只依据 DTD 声明推断实际产物。

`defaultDisplayFields` 是 ViewVo 属性名列表，只影响已有展示字段的默认列展示与顺序；它不创建关联字段、不执行 ID 到名称转换，也不替代后端查询/映射。例如 `defaultDisplayFields="categoryText"` 只有在 ViewVo 实际含有 `categoryText`（并有可靠的值来源或字典转换声明）时才成立。通常仍须保留 `categoryId` 作为编辑表单保存的 ID 字段，再另有 `categoryText` 展示属性；对 `categoryText` 加 `@ViewColumnMeta(columnName="category_id")` 以绑定原业务列，并确认其 Dict/MapStruct 转换链。若列表或详情也要显示中文，检查后端返回值上该转换能实际执行，不能只通过前端把 ID 换成 option label。

默认视图 ID 由生成器根据 controller bean 名计算：小写的 bean 名与默认视图后缀拼接后取 `MD5_15`，生成在 `ViewTableMeta.value` 中。XML `listKey` 与该 ID 相互独立，且当前 ViewMetaAnnotationPlugin 不将 `listKey` 写入默认 `ViewTableMeta`；不要按 DTD 中过期的 listKey 描述手工设置或覆盖默认视图 ID。视图数据 URL 按生成器 controller 基础路由构造为 `{controllerBasePath}/getdtdata`，路由及数据查询由宿主运行时提供。

#### 5.3.2 子节点与列级元数据

| 子节点 | 作用 | 关键约束 |
|---|---|---|
| `fieldOverrides` | 覆盖指定 ViewVo 字段的标签、宽度、对齐、固定、排序、隐藏及编辑标记 | `fields` 必填，按 VO 属性名匹配；优先核对生成的 `ViewColumnMeta` |
| `queryColumn` | 补充查询字段描述 | `column` 必填；可配置 tag/query mode、类型、顺序、格式、数据源、枚举/字典或 dataUrl。该描述不自动添加 RequestVo 字段或服务端筛选 |
| `filterColumn` | 补充过滤字段描述 | `column` 必填；可配置控件、query mode、顺序、重复/多选/范围及 `operators`；操作符和值域必须与后端解析支持一致 |
| `htmlButton` | 描述自定义工具栏/行操作按钮 | 用 `toolbar` / `actionColumn` 引用相应按钮 ID；描述按钮不等于实现 handler、服务端动作或授权 |
| `overridePropertyValue` / `columnRenderFun` | 配置显示值转换/列表渲染 | 核对来源字段、目标 ViewVo 属性、值来源及实际前端渲染支持 |
| `additionalProperty` | 增加展示属性 | 新字段需核验类型、赋值来源和 ViewVo 生成结果 |
| `innerListView` | 配置关联/内嵌列表视图 | 需单独设计关系键、数据来源及保存行为；具体生成行为按内嵌列表配置规则检查 |

DTD 允许 `viewVo` 含上述及相关子节点；当前 Parser 对 `queryColumn`、`filterColumn`、`fieldOverrides` 等分别解析。`queryColumn` / `filterColumn` 描述只有同时列入 `queryColumns` / `filterColumns` 时才参与对应元数据；无显式 `queryColumn` 描述时，不存在的 query 字段会被忽略，显式描述须匹配真实列；不存在的 filter 字段会抛出异常。子节点合法仅表示可被配置解析，不表示宿主页面一定消费或服务端已有对应行为。

#### 5.3.3 最小视图配置示例

以下片段放在既有 `<table>` 的 `<generateVo>` 中，字段名必须替换为真实表列/VO 属性：

```xml
<generateVo generate="true">
    <createVo generate="true"/>
    <updateVo generate="true"/>
    <viewVo generate="true"
            title="示例管理"
            includeColumns="name_,state_,created_"
            queryColumns="name_"
            fuzzyColumns="name_"
            filterColumns="state_"
            toolbar="CREATE,REMOVE"
            actionColumn="ROW_VIEW,ROW_EDIT"
            defaultDisplayFields="name_,state_,created_"
            defaultSort="created_ desc">
        <queryColumn column="name_" remark="名称"/>
        <fieldOverrides fields="name_" label="名称" minWidth="140"/>
        <filterColumn column="state_" remark="状态"
                      operators="EqualTo,NotEqualTo"/>
    </viewVo>
    <requestVo generate="true"/>
</generateVo>
```

本示例只建立 VO/视图元数据的配置起点。生成前后都应核对目标字段是否在 ViewVo 中、查询是否由 RequestVo/后端消费、过滤操作符是否受支持、工具栏和行操作是否有实现及权限、列元数据是否被目标宿主消费。

#### 5.3.4 分页属性与菜单数据限制

当前 1.8.0 DTD 的 `viewVo.enablePager` 声明接受 `true`、`false`、`simple`、`auto`，而 Parser 使用 `Boolean.parseBoolean` 解析，且配置对象默认值为 `true`；因此只有 `true` 会解析为真，`simple` 和 `auto` 都会变成假。它也不是 `ViewTableMeta` 注解成员，不可将其当作 Vue3 表格分页模式的可靠配置。需使用分页显示模式时，先按实际模板和宿主运行时核验；不要依赖 DTD 的枚举声明推断行为。

#### 5.3.5 菜单期初数据生成

本节只说明生成器能够保证的菜单配置与脚本边界。所有数据库期初资源（包括 DDL、权限数据、菜单数据和存储过程等）的汇总、执行顺序及 Spring 首次写库流程统一见[第 19 节「Spring 首次初始化」](#19-spring-首次初始化)。生成器本身只产出对应 SQL 资源，不连接应用数据库执行，也不自动触发 Spring 初始化。

##### 生成前配置

默认视图菜单 SQL 仅在以下条件全部满足时产生：

| 条件 | 配置 / 判定 |
|---|---|
| ViewVo 启用 | `<generateVo generate="true">` 下配置 `<viewVo generate="true">`。 |
| ViewVo 本次实际生成 | ViewVo 生成规则通过，且目标文件不存在或被强制重新生成；若已有 ViewVo 文件被跳过，则不会在该次生成路径中收集菜单数据。插件回调也必须允许生成。 |
| 菜单数据开关启用 | Context 显式配置 `updateMenuData="true"`；缺省为 `false`。 |
| 表参与模块菜单 | 对应 `<table>` 配置 `isModules="true"`；缺省为 `false`。 |
| 父菜单 ID 有效 | ViewVo 的 `parentMenuId` 优先；未配置时使用 Context 的 `parentMenuId`。两处都未提供时没有隐式默认父菜单，菜单 SQL 不会生成。 |

示例中的父菜单 ID、模块名及表名都应替换为目标项目已确认的值：

```xml
<context id="example"
         moduleKeyword="example"
         updateMenuData="true"
         parentMenuId="EXISTING_PARENT_MENU_ID">
    <!-- 其他 context 配置 -->
    <table tableName="biz_example" domainObjectName="BizExample" isModules="true">
        <!-- 其他 table 配置 -->
        <generateVo generate="true">
            <viewVo generate="true"
                    title="示例管理"
                    parentMenuId="EXISTING_PARENT_MENU_ID"
                    enableShortcut="false"/>
        </generateVo>
    </table>
</context>
```

如果所有表使用同一个父节点，可只在 Context 配置 `parentMenuId`，ViewVo 级配置可省略；需要将某个视图放到不同父节点时，在该 ViewVo 单独配置并覆盖 Context 值。`title` 未配置时回退到表备注；菜单 ID 使用生成器计算的默认视图 ID。菜单项记录的名称、图标、父 ID、排序、标题、组件、路径、URL、备注、审计和租户字段来自生成器模板；启用快捷入口时还会写入相应字段。生成器不会替项目创建 `EXISTING_PARENT_MENU_ID` 对应的菜单行。

##### 生成结果检查

生成成功后检查目标项目中的：

```text
src/main/resources/sql/init/data-menu-<moduleKeyword小写>.sql
```

该文件只有在收集到菜单数据且 `updateMenuData="true"` 时才输出。逐条检查菜单 ID、父菜单 ID、标题、路径 / URL、数据库方言及项目必需的租户/审计字段；确认父菜单及其依赖数据会先于本脚本写入。按表或模块局部生成时，不能把本次输出误认为已包含其他 Context / 其他生成批次的菜单项；部署前应核对最终汇总脚本是否完整。文件存在只证明生成器输出了 SQL，不证明 SQL 已被打包、Spring 已执行或数据库已成功写入。

配置前依据完整属性与子节点结构核对当前版本 `mybatis-generator-config_1_8_0.dtd` 的 `<viewVo>` 声明及 `MyBatisGeneratorConfigurationParser.parseVoView`、`parseTableListCommon`；生成结果则检查 `ViewObjectClassGenerator`、`ViewMetaAnnotationPlugin` 与目标宿主运行时，不得把旧 DTD 注释或 mpmm-ui 单一实现当成通用契约。

## 6. 值转换 / 渲染

| 节点 | 用途 | 说明 |
|---|---|---|
| `overridePropertyValue` | 字段值转换 | 适合 `state_ -> stateText`、字典中文回填等。 |
| `columnRenderFun` | 列渲染函数 | 适合列表展示态格式化。 |
| `htmlElementDescriptor` | 表单控件定义 | 兼顾编辑态和展示态。 |

### `overridePropertyValue` 关键属性

| 属性 | 含义 |
|---|---|
| `sourceColumn` | 原始列。 |
| `annotationType` | `DictUser` / `DictSys` / `DictModule` / `Dict` / `DictData` / `DictEnum`。 |
| `beanName` | `Dict` 时必配。 |
| `targetColumn` | 目标列名已存在时复用该列。 |
| `targetProperty` | 生成的新展示属性名。 |
| `targetPropertyType` | 新属性类型，默认 `java.lang.String`。 |
| `typeValue` | 字典类型/编码。 |
| `applyProperty` | 映射结果取值属性。 |
| `enumClassFullName` | 枚举类全名。 |

### 常见用法

```xml
<overridePropertyValue sourceColumn="f"
                       annotationType="DictSys"
                       typeValue="c"
                       targetProperty="fText"
                       targetPropertyType="java.lang.String"/>
```

## 7. 页面生成

本章按当前 1.8.0 的 **DTD → Parser → 配置计算 → 生成消费点** 核对，不将 DTD 注释直接当作实际默认值。权威文档的能力边界见[第 2.2 节](generator-authoritative-spec.md#22-vue3-前端可生成能力)，具体属性统一在本章维护。

表中“影响”使用 `vue3`、`html`、`vue3和html`：`vue3` 包括写入 Java VO 的 `VueForm*Meta` 注解，再由后端配置接口交给 Framework 表单消费的**间接影响**，不只指 `.vue` 文件文本；`html` 指 Thymeleaf/静态资源生成链路。`vue3和html` 不表示各框架、页面类型逐项等效支持。当前未解析、未传递的属性仍列在其配置归属范围下，但备注明确标为**未生效**，不可据“影响”列推断已实现。未设置的字符串一般为 `null`、集合一般为空；注解未输出的成员由所用 basic 版本的注解默认值及宿主运行时决定，不应把配置对象初值冒充运行时默认值。

### 7.1 通用配置

#### 7.1.1 `generateHtml` 属性

| 属性 | 含义 | 默认值或值来源 | 影响 | 备注 |
|---|---|---|---|---|
| `generate` | 是否启用页面生成 | 必填，无 DTD 默认；Parser 用 `Boolean.parseBoolean` | vue3和html | 参与 editable/viewOnly 页面产物门控；并不是所有 VO 表单元数据的总开关，见下文。 |
| `type` | 页面类型 | `editable`（配置对象构造值） | vue3和html | 可选 `editable` / `viewOnly` / `print`；print 不触发 Vue3 编辑或详情规则。 |
| `viewPath` | HTML 相对视图路径及文件名主体 | 未填取数据库表名小写 | html | 完整路径为基础包路径 + `/` + viewPath，末段加 `.html`；可带子目录，不决定 Vue3 文件名。 |
| `title` | HTML 页面标题 | 未填时由 HTML 文档生成器使用表简要备注 | html | `none` 不生成可见标题；不是 Vue3 列表标题，后者见 `viewVo.title`。 |
| `targetProject` | HTML 输出根目录 | 本属性 → Context `htmlTargetProject` → `src/main/resources/templates` | html | 不代替 `vueEndProjectPath`，也不控制 JQuery 静态 JS 根目录。 |
| `targetPackage` | HTML 基础包路径 | 本属性 → Context `htmlTargetPackage` → `moduleKeyword` → `html`（模块关键字为 null 时） | html | Parser 再按完整 viewPath 计算实际子目录。 |
| `overWriteHtmlFile` | 已有 HTML 文件是否允许覆盖 | `false` | html | 不控制 Vue3；最终还受写盘回调影响。 |
| `overWriteJsFile` | 已有静态 JS 文件是否允许覆盖 | `false` | html | 作用于 JQuery 插件的 JS/min.js。 |
| `overWriteCssFile` | 已有 CSS 文件是否允许覆盖 | `false` | html | 仅在实际生成样式文件时有意义。 |
| `default` | 选择共用表单元数据的页面配置 | **`true`**（`defaultConfig` 初值） | vue3和html | Vue 表单注解取第一个 default=true 的配置；若全为 false 则取第一个。多个页面应显式设定，不能按 DTD 注释推断省略为 false。 |
| `key` | DTD 预留页面键 | 无；`parseHtml` 未读取 | vue3和html | **未生效**，不用于选择 Vue3 页面或表单。 |

**多页面配置的两个不同选择规则**：Vue3 文件是否生成按整张表“是否存在已启用的 editable/viewOnly”判断；`VueHtmMetaAnnotationPlugin` 则从所有页面配置中选择上述默认配置，**不按 generate/type 过滤**。因此 `generate="false"` 仍可能成为 VO 表单注解来源；改为 print 也不等于关闭表单元数据。表单注解还要求对应 VO/字段实际生成且插件生效，不能只看前端输出目录。

#### 7.1.2 `property` 与字段状态

`property` 使用必填属性 `name`（属性名）和 `value`（属性值），均无默认值；影响取决于具体 name。以下四项按逗号分隔，使用数据库列名，并由配置计算阶段转入相关字段集合；不要与 Java/TS 属性名混写。

| 属性 | 含义 | 默认值或值来源 | 影响 | 备注 |
|---|---|---|---|---|
| `name="htmlHiddenFields"` 的 `value` | 隐藏列 | 空；合并 Context、Table 及本页面集合 | vue3和html | 合并为集合而非子级替换；还可能影响列表字段计算。Vue 字段注解生成亦会筛选隐藏列，不保证仅产生一个前端 hidden 标志。 |
| `name="requiredColumns"` 的 `value` | 表单必填列 | 空；另合并列必填标志及 descriptor.verify 中的 required | vue3和html | Vue 校验还读取列 nullable；前端校验不能代替服务端约束。 |
| `name="htmlReadonlyFields"` 的 `value` | 只读列 | 空；合并 Context、Table 及本页面集合 | vue3和html | Vue 写入表单级 readonlyFields；并非后端禁止更新。 |
| `name="htmlDisplayOnlyFields"` 的 `value` | 仅展示列 | 空；合并 Context、Table 及本页面集合 | vue3和html | 不等于隐藏或只读；需核对宿主表单提交行为。 |

### 7.2 页面布局

#### 7.2.1 `layout`

省略节点时 Parser 仍建立 `HtmlLayoutDescriptor`。Vue3 不是直接套用 HTML DOM，而是从布局配置生成表单/字段/容器注解。

| 属性 | 含义 | 默认值或值来源 | 影响 | 备注 |
|---|---|---|---|---|
| `loadingFrameType` | HTML 页面打开方式 | `pop` | html | `pop` / `inner` / `full`；不控制 Vue3 弹窗。 |
| `pageColumnsNum` | 每行字段数 | `2` | vue3和html | `1,2,3,4,6,12`；Vue 普通字段 span=`24/列数`，长文本/编辑器还可调整为 24。 |
| `barPosition` | HTML 工具条位置 | `bottom` | html | `bottom` / `top`。 |
| `uiFrameType` | HTML 渲染分支 | **`layui`**（当前类初值） | html | DTD 接受 layui/zui/elPlus/antd；当前 `JavaClientGeneratePlugins` 的 HTML 文档分支仅实现 layui、zui，其余返回 false。不能按 DTD 旧注释认为默认 elPlus，或认为设置 elPlus 才生成 Vue3。 |
| `exclusiveColumns` | 独占一行的数据库列 | 空列表 | vue3和html | 逗号分隔；Vue 字段 span=24。 |
| `borderWidth` | HTML 内容边框宽度 | `1`（`ConstantsUtil.HTML_BORDER_WIDTH`） | html | Parser 用整数解析，填 `1` 而非 `1px`；偏离默认边框配置会触发样式文件生成。 |
| `borderColor` | HTML 边框颜色 | `#eee` | html | 与宽度共同决定是否输出 CSS。 |
| `labelWidth` | 表单标签宽度 | `120px` | vue3和html | 写入 VueFormMeta，也用于 HTML 内嵌列表 form 打印标签宽度；如 `auto`、`100px`，不等于 labelCss。 |
| `labelPosition` | 标签位置 | `right` | vue3 | `left` / `right` / `top`，由宿主表单解释。 |
| `size` | 控件大小 | `default` | vue3 | `large` / `default` / `small`。 |
| `popSize` | 表单弹窗大小 | `default` | vue3 | `largeX` / `large` / `default` / `small`。 |
| `popDraggable` | 弹窗可拖动 | `true` | vue3 | 写入表单元数据，不控制 HTML loadingFrameType。 |

#### 7.2.2 `groupContainer`

位于 `layout` 下，可递归嵌套。以下属性由 `VueFormContainerMetaDesc` 转为容器注解，不能据此保证旧 HTML 模板同样实现 tab/card/collapse。

| 属性 | 含义 | 默认值或值来源 | 影响 | 备注 |
|---|---|---|---|---|
| `elementKey` | 容器唯一键 | DTD 必填 | vue3 | 不与列名、属性名、其他组件键重名；供 includeElements 等引用。 |
| `name` | 容器名称 | 未设置不输出 | vue3 | 与唯一键、标题区分。 |
| `type` | 容器类型 | DTD 必填；Parser 缺失则丢弃该容器 | vue3 | `border` / `tab` / `card` / `collapse`。 |
| `title` | 容器标题 | 未设置不输出 | vue3 | 不改变内嵌列表 label 的配置值。 |
| `span` | 容器占用栅格 | `24` | vue3 | 24 栅格制。 |
| `columnNum` | 容器内字段列数 | **`1`**（不是 DTD 注释中的 2） | vue3 | `1,2,3,4,6,12`；不要与 span 混淆。 |
| `afterColumn` | 放置在指定元素后 | 未设置不输出 | vue3 | 最终定位由运行时布局处理。 |
| `includeElements` | 容器包含的元素 | 空列表 | vue3 | 逗号分隔列名或组件 elementKey；配置引用应实际存在。 |
| `noBorder` | 无边框 | `false` | vue3 | 写入容器元数据。 |
| `hideExpression` | 隐藏条件 | 未设置不输出 | vue3 | 表单表达式，不是 SQL；见 7.3.2。 |
| `className` | 容器 CSS 类 | 未设置不输出 | vue3 | 类样式须由宿主提供。 |

父容器键由 XML 嵌套层级计算：顶层无父键，子层使用直接父容器 elementKey；当前 DTD **没有** `parentElementKey` 属性，不能照旧注释手填。

#### 7.2.3 布局实践建议与宿主边界

**推荐实践：** 普通表单优先评估增加一个 `type="border"`、`noBorder="true"` 且不设置 `title` 的外层 `groupContainer`，统一管理顺序、分组和列数；这是设计建议，不是强制增加的生成默认值。小而简单的表单可不设容器，但仍须检查最终顺序与对齐。

字段按业务操作习惯排列，而非机械照搬表结构。无显式编排时，数据库列经内省和生成后的顺序会影响初始字段顺序；这不保证与物理列顺序完全相同，继承、隐藏和附加组件也会参与。**需要重新排序或稳定业务顺序时，必须在对应容器的 `includeElements` 中显式列出所需字段/组件顺序**，使用数据库列名或组件 `elementKey`，不为页面排序调整数据库物理列。生成器将该列表按序写入容器元数据，最终显示须由目标宿主消费并核对。

字段多时可按业务主题/信息类型设置多个容器，短字段适度多列，说明、富文本、附件和子表按需独立区域。分别确认 `layout.pageColumnsNum`、容器 `columnNum`、容器 `span` 和字段独占行；容器 `columnNum` 当前默认 1，不继承“页面默认两列”的假设。

**已知宿主限制：** 当前示例 Framework 的 `LayoutItems.vue#getSpan` 在容器 `columnNum > 0` 时，对普通字段直接返回 `24/columnNum`，优先于字段 span。因此生成器为长 input、editor 或 `exclusiveColumns` 输出 span=24，不保证其在多列容器里仍独占一行。需要整行显示时，可将长文本放入 `span="24"`、`columnNum="1"` 的独立子容器；若业务要求同一多列容器混排且保留字段独占行，先评审 Framework 能力，不把配置建议冒充已经实现的运行行为。该观察只针对当前示例源码，不推定所有宿主相同。

以下为放入既有 `generateHtml` 的布局示意；表须实际包含 `subject_`、`apply_date`、`remark_`，否则先替换为真实列。外层无标题，短字段两列，长文本区域单列：

```xml
<layout pageColumnsNum="2" exclusiveColumns="remark_">
    <groupContainer elementKey="mainGroup" type="border"
                    noBorder="true" columnNum="2"
                    includeElements="subject_,apply_date,descriptionGroup">
        <groupContainer elementKey="descriptionGroup" type="border"
                        noBorder="true" span="24" columnNum="1"
                        includeElements="remark_"/>
    </groupContainer>
</layout>
```

**结果检查：** 核对 `VueFormContainerMeta` 中的包含顺序与父键、`VueFormItemMeta` 的 span，以及宿主实际表单中的排列、整行区域、条件隐藏、标签对齐和弹窗/窄屏效果。不能只检查 XML 合法；列表列排序和查询区布局也不由此片段自动确定。源码入口见 §7.8；示例运行时依据为 `D:\vscode\mpmm-ui\packages\framework\src\components\vgoForm\items\LayoutItems.vue` 的 `getElementIds/getSpan`。

### 7.3 输入组件配置

#### 7.3.1 `htmlElementDescriptor`：字段、控件与数据源

| 属性 | 含义 | 默认值或值来源 | 影响 | 备注 |
|---|---|---|---|---|
| `column` | 绑定数据库列 | DTD 必填 | vue3和html | Parser 用作 name；内省绑定失败会警告并移除配置，不是新增虚拟字段的方法。 |
| `tagType` | 控件类型 | DTD 必填；显式节点不自动补齐 | vue3和html | 可选值见当前 DTD `%tagType;`；Vue 将 dropdownlist 转为 select。省略整个 descriptor 时才可能由列规则推导控件。 |
| `dataSource` | 字典/枚举/实体等来源 | 未设置；可被 dataFormat、自动列规则补齐 | vue3和html | 取当前 DTD `%dataSource;` 值，如 Dict/DictData/DictSys/DictUser/DictEnum/Department/User/Organization/InnerList；不是任意 bean 标识。 |
| `dataUrl` | 选项/数据接口 | 显式值优先；字典来源可计算，见下文 | vue3和html | getter 对非空且无前导 `/` 的值补 `/`，不应未经核对就写外部绝对 URL。 |
| `dataFormat` | 快捷语义/日期格式 | 未设置 | vue3和html | 是否/有无/性别/启停/缓急/级别会转换为对应枚举来源；日期类保留给日期控件处理。该转换可覆盖原 dataSource/enumClass，避免矛盾配置。 |
| `otherFieldName` | 关联显示值属性 | 显式值优先；普通输入/日期等通常取原 Java 属性；其他常去掉末尾 Id 再加 Text，否则属性名加 Text | vue3和html | `ConfigUtil.getOverrideJavaProperty` 计算，默认列还可能有专用映射；必须核对实际 VO 字段。 |
| `beanName` | 引用数据的 bean/类型 | 未设置；部分自动列描述有专用来源 | vue3和html | Dict 引用须与后端转换及选项服务对应，不自动实现该服务。 |
| `applyProperty` | 引用对象显示属性 | 未设置，无 Parser 通用默认 | vue3和html | 与 keyMapLabel 不同：前者描述引用对象转换，后者描述远程选项字段映射。 |
| `applyPropertyKey` | 引用对象值属性 | 未设置，无 Parser 通用默认 | vue3和html | 应与保存字段类型及后端关联键一致。 |
| `enumClassFullName` | 枚举全类名 | 显式值，或由 dataFormat 推导 | vue3和html | DictEnum 使用；枚举类必须在实际运行 classpath。 |
| `dictCode` | 字典编码 | 未设置；DictSys/DictData/DictUser 无 dataUrl 时缺省取列名转小驼峰 | vue3和html | 有显式 dataUrl 时不执行这段补值逻辑。 |
| `callback` | 选择回调名 | 未设置 | vue3和html | HTML 可与关系方法后缀关联；Vue 写入字段注解，不代表业务回调已自动实现。 |
| `switchText` | 开关文案 | 显式值 → dataFormat 对应枚举文案；否则可为空 | vue3和html | 不将 DTD 的“启用/禁用”注释当所有控件的统一缺省。 |
| `verify` | 简写校验列表 | 空；通过 basic 的 `VStringUtil.splitToList` 拆分去重 | vue3和html | 当前 basic 源码支持逗号、管道、分号等分隔；如 `required,number`。required 会汇入必填列，复杂 Vue 规则优先用 validator。 |
| `labelCss` / `elementCss` | 标签/组件附加样式 | 未设置 | vue3和html | Vue 写入字段元数据；实际样式支持依组件实现。 |
| `dataFmt` | 旧 HTML 控件格式 | 未设置 | html | 不同于 dataFormat；Vue 日期格式计算读取 dataFormat/列类型，未将 dataFmt 作为 valueFormat 传递。 |
| `dateRange` | 日期范围选择 | `false` | vue3 | 与日期 dataFormat 联合计算 yearrange/monthrange/daterange/datetimerange。 |
| `multiple` | 多选 | `false` | vue3和html | 日期可转 years/months/dates，引用选择为多值；保存类型/序列化须匹配。 |
| `listKey` | 引用列表标识 | 未设置 | vue3和html | 不会自动创建相应列表配置。 |
| `listViewClass` | 引用列表 VO 类 | 未设置 | vue3和html | Vue 写为 sourceListViewClass；必须有可用列表元数据。 |

字典 URL 的实际补值（`TableConfiguration.setHtmlElementDescriptorColumn`）：DictSys → `/system/sys-cfg-dict-impl/option/{dictCode}`，DictData → `/system/sys-dict-data-impl/option/{dictCode}`，DictUser → `/system/dict-content-impl/option/{dictCode}`。仅当 dataSource 匹配且 dataUrl 为空时执行；这些是当前生成器约定，并非所有宿主已有对应接口。

无显式描述时，`TableConfiguration.generateDefaultElementDescriptor` 的列规则会为 state_、priority_、wf_state、parent_id、备注以“是否”开头等列建立描述；Vue 的后备类型推断为数值→number、日期/时间→date/time、长文本→editor、Boolean→switch、其他→input。普通 input 长度大于 499 占满行，大于 1499 转 textarea；editor 占满行。不要把自动列规则等同于任意业务字段都已有正确字典、权限和联动。

##### 文本输入实践建议

以下为当前 1.8.0 源码 `VueFormGenerateUtil.setComponentName` 的行为；前提是最终组件为 `input` 且绑定了内省列。textarea 是 input 的 `type`，并非独立 `tagType`：

| 内省列长度 | 该分支的输出 | 设计影响 |
|---|---|---|
| 499 及以下 | 不因长度强制 span=24，也不设置 textarea | 是否单行仍按业务需求判断 |
| 500～1499 | span=24，不因长度设置 textarea | 单行输入也可能占满一行，不要只按多列数估算布局 |
| 1500 及以上 | span=24、type=textarea | 适用于容量确实较大的多行纯文本；边界是大于 1499，不是大于等于 1499 |

LONGVARCHAR 等被识别为 `isLongVarchar()` 的列，在无显式描述时先走 editor 推断，不经过普通 input 阈值分支；显式选择其他组件也不适用此表。字段 span 在容器中的实际呈现还受 §7.2.3 的宿主规则影响。

**推荐实践：** 先确定业务容量和纯文本/富文本语义，再利用相符的类型、长度约定。不要只为生成 textarea 把原本较短的字段放大到 1500，也不要将“能输入换行”当成必须存储富文本。当前 DTD 的 `tagType` 不含 textarea，`htmlElementDescriptor` 也没有通用 `type` 属性；短字段需要多行时，先核对所用版本是否已有完整配置接点，没有时评审生成器通用完善或受保护的 Framework/业务扩展，不能编造 `tagType="textarea"`、`type="textarea"` 或未经消费验证的 property 配置。

**结果检查：** 检查内省类型/长度、生成的 component/type/span、前后端长度校验，以及换行保存后重新打开、详情展示和窄屏布局；不要把字段容量、输入控件和最终占宽视为同一个配置。

#### 7.3.2 远程选项、联动、显隐与只读链接

以下为 `htmlElementDescriptor` 的其余属性；标注 vue3 的项已进入 Vue 字段元数据，不据此承诺旧 HTML 分支支持相同表达式。

| 属性 | 含义 | 默认值或值来源 | 影响 | 备注 |
|---|---|---|---|---|
| `remoteApiParse` | 使用远程解析 | `false` | vue3 | 生成字段标志；接口响应须符合宿主协议。 |
| `remoteToTree` | 远程选项转树 | `false` | vue3 | 树关系字段及转换规则由运行时决定。 |
| `remoteAsync` | 远程异步模式 | `false` | vue3 | 不是生成时请求远程数据。 |
| `keyMapLabel` / `keyMapValue` | 远程选项 label/value 字段映射 | 配置对象为空；注解描述器分别以 name/id 为省略比较值 | vue3 | DTD 注释写 label/value，不能当作 Parser 默认；最终默认核对 basic 注解和宿主。 |
| `enablePager` | 引用列表分页 | **`false`**（继承配置类布尔初值） | vue3 | TableSelect 也共用此初值，不是其 DTD 注释所写 true。 |
| `vxeListButtons` | 引用列表工具栏动作键 | 空列表 | vue3 | 逗号分隔；转换为 toolbarActions，不是自动实现 handler。 |
| `defaultFilterExpr` | 引用列表默认过滤 SQL | 未设置 | vue3 | SQL 用 `=`；不应接受客户端任意 SQL，业务授权须在服务端实现。 |
| `hideExpression` | 隐藏条件 | 未设置 | vue3 | 表单表达式如 `$.state == 1`；按 Java/TS 属性名引用，而不是数据库列名。 |
| `disabledExpression` | 禁用条件 | 未设置 | vue3 | 与隐藏不同；禁用不构成后端写权限校验。 |
| `dataUrlParams` | 数据请求附加参数 | 未设置 | vue3 | 占位符替换依宿主协议；XML 中 `&` 须写作 `&amp;`。 |
| `watchFields` | 选项刷新监听字段 | 空集合 | vue3 | 逗号分隔表单属性名；只对消费此配置的组件生效。 |
| `renderHref` | 只读字段链接化 | `false` | vue3 | 不同于 HTML 子节点 hrefElement；还依赖宿主路由。 |
| `hrefDataKeyField` | 链接目标数据键属性 | 未设置 | vue3 | 当前显示值不是记录键时显式提供；配合 renderHref。 |
| `tips` | 字段提示 | 未设置 | vue3 | 写入 Vue 字段元数据。 |

显隐表达式与 SQL 过滤表达式是两种语言。DTD 提示可引用 `$`（当前表单）、viewStatus、currentUser，但变量注入及求值由宿主实现；本章不保证所有 HTML/Vue 控件均支持，也不把显示条件当权限规则。

#### 7.3.3 `htmlTableSelectDescriptor`

Parser 复用普通 descriptor 解析，随后强制 dataSource=Dict，tagType 缺省 tableselect。下表覆盖其专有声明；其余已声明属性按 7.3.1、7.3.2 对应行读取，不是继承普通节点的全部可选属性。

| 属性 | 含义 | 默认值或值来源 | 影响 | 备注 |
|---|---|---|---|---|
| `column` | 保存选择值的列 | DTD 必填 | vue3和html | 仍须绑定真实列。 |
| `tagType` | 表格选择形式 | `tableselect` | vue3和html | 还可为 tableSelectPop；不改变强制 Dict 来源。 |
| `dataUrl` / `beanName` / `listKey` / `listViewClass` | 接口、引用类型、列表标识与 VO 类 | **四项在 DTD 均必填** | vue3和html | 即使某运行时可能推导，验证型 XML 也不能省略。 |
| `otherFieldName` / `applyProperty` / `applyPropertyKey` / `callback` | 回填与回调 | 同普通 descriptor | vue3和html | 不会自动提供引用查询业务。 |
| `multiple` | 多选 | `false` | vue3和html | 默认单选。 |
| `enablePager` | 分页 | `false` | vue3 | 显式配置 true 才能依靠生成标志开启。 |
| `dataUrlParams` / `defaultFilterExpr` / `hideExpression` / `disabledExpression` | 参数、过滤、显隐、禁用 | 未设置 | vue3 | 语义与 7.3.2 一致。 |
| `renderHref` / `hrefDataKeyField` / `tips` | 只读链接及提示 | false / 未设置 / 未设置 | vue3 | 语义与 7.3.2 一致。 |

#### 7.3.4 `htmlSwitchDescriptor` 的当前限制

**当前 `parseHtml` 没有处理直接子节点 htmlSwitchDescriptor 的分支**，会记录未知节点警告；不能把该 DTD 节点描述为可用的页面开关简写。页面中改用 `<htmlElementDescriptor column="..." tagType="switch" .../>`。reusableElements 有同名分支，但并不能据此证明页面直接子节点生效。

| 属性 | 含义 | 默认值或值来源 | 影响 | 备注 |
|---|---|---|---|---|
| `column` | 开关绑定列 | DTD 必填 | vue3和html | 当前页面直接子节点整体未生效，以下亦同。 |
| `dataUrl` / `dataFormat` / `dataSource` | 数据接口、快捷格式、来源 | 普通描述器逻辑；直接子节点未解析 | vue3和html | dataFormat 限启停/有无/是否，dataSource 限 Dict/DictData/DictUser/DictSys/DictEnum。 |
| `otherFieldName` / `beanName` / `applyProperty` / `applyPropertyKey` / `dictCode` | 回填与字典映射 | 普通描述器逻辑；直接子节点未解析 | vue3和html | 不额外支持 enumClassFullName 属性。 |
| `callback` / `switchText` | 回调与文案 | 普通描述器逻辑；直接子节点未解析 | vue3和html | 使用普通 descriptor 才走页面消费链路。 |
| `hideExpression` / `disabledExpression` / `watchFields` / `tips` | 状态、联动与提示 | 普通描述器逻辑；直接子节点未解析 | vue3和html | 不能以 DTD 验证通过代替行为验收。 |

#### 7.3.5 `htmlFormExtendDescriptor`

| 属性 | 含义 | 默认值或值来源 | 影响 | 备注 |
|---|---|---|---|---|
| `column` | 设计 JSON / 数据 JSON 保存列 | DTD 必填 | vue3 | 按真实数据库列绑定，不会建立新持久化字段。 |
| `tagType` | 自定义表单设计/渲染控件 | DTD 必填 | vue3 | formDesign / formRender；复用普通 descriptor 解析后写入 Vue 字段注解。 |
| `parentFormKey` | 设计时父表单键 | 未设置 | vue3 | 运行时用以定位父表单容器。 |
| `designIdField` | 渲染时设计 ID 的属性 | 未设置 | vue3 | 应是宿主可读取的表单属性。 |
| `designRestBasePath` | 设计资源接口基础路径 | 未设置；Parser 保存到 dataUrl | vue3 | 读取时同样可能补前导 `/`。 |
| `configJsonfield` | 设计配置 JSON 属性名 | `jsonContent` | vue3 | 大小写以此拼写为准；不是 dataUrl 成员。 |
| `hideExpression` / `disabledExpression` | 隐藏/禁用条件 | 未设置 | vue3 | 同 7.3.2。 |

DTD 的子内容模型为 `properties*`，而 Parser 只识别 `property`；不要在此节点内配置 property 并期待同时通过当前 DTD 验证及解析。此处只记录限制，本次不修改 DTD/Parser。

### 7.4 校验、链接与按钮

#### 7.4.1 `validator`

可作为普通 descriptor、Switch/TableSelect 的子节点；但 Switch 的页面解析限制仍适用。下表描述 Vue 校验注解链路，不表示 HTML Layui 的 verify 字符串与其等效。

| 属性 | 含义 | 默认值或值来源 | 影响 | 备注 |
|---|---|---|---|---|
| `scope` | 规则适用范围 | `0`（未指定，通用）；DTD 显式仅允许 1/2 | vue3 | 1 为表单，2 为内嵌表格编辑；按匹配 scope 或 0 筛选。 |
| `required` | 必填 | Boolean 未设置；规则先看列非空，再看 validator=true/requiredColumns | vue3 | false 不能取消非空列必填。 |
| `type` | 验证值类型 | 显式规则优先，否则由 JavaTypeMapEnum 按字段类型推导 | vue3 | 值域见 `%validatorType;`，不要与 tagType 或 verify 规则名混用。 |
| `min` | 下限/最短长度 | 列 minLength>0 时可推导，再由显式规则覆盖 | vue3 | Parser 要求可解析为数值，否则添加警告。 |
| `max` | 上限/最长长度 | 字符串列长度可推导 | vue3 | **已知实现偏差**：Parser 校验 max 后却执行 setMax(min)，因此显式 max 不能按声明值保证生效。 |
| `len` | 精确长度 | 配置对象未设置 | vue3 | string/array 使用；应核对生成规则而非仅看 XML。 |
| `pattern` | 正则 | 未设置 | vue3 | 注意 XML 与 Java 注解字符串的转义。 |
| `enum` | 允许值集合 | 未设置 | vue3 | **已知实现偏差**：规则描述器 ENUM 分支读取 pattern 而非 enumList，不能保证按声明值输出。 |
| `whitespace` | 仅空白输入的校验标志 | Boolean 未设置 | vue3 | Parser 读取，但当前规则描述器未传递该配置，不应承诺生效。 |
| `transform` | 校验前转换函数 | 未设置 | vue3 | 作为规则文本输出；宿主须支持。 |
| `validator` | 自定义校验函数 | 未设置 | vue3 | 需匹配表单/内嵌表格的函数签名；不会生成业务函数实现。 |
| `message` | 校验提示 | 显式配置或按列备注/规则类型生成 | vue3 | 各规则分支取值不完全一致，不是所有规则的全局覆盖值。 |
| `trigger` | 校验触发方式 | 采用显式值的规则分支优先取配置；否则 scope=1 为 blur、scope=2 为 change | vue3 | DTD 为 blur/change/submit/blurChange；非空列必填等分支直接采用范围默认值，不读取本项。 |

#### 7.4.2 `hrefElement`

普通 descriptor 下可配置一个，用于 HTML 字段附加链接；Vue 字段链接请用 renderHref/hrefDataKeyField。

| 属性 | 含义 | 默认值或值来源 | 影响 | 备注 |
|---|---|---|---|---|
| `href` | 链接地址 | DTD 必填；Parser 空值则丢弃 | html | 不自动创建目标路由。 |
| `target` | 打开目标 | 配置类 **`_blank`**；DTD 显式值 blank/self/parent/top | html | 省略值与显式值拼写不同，需核对实际 HTML。 |
| `type` | 滑出/弹窗方式 | **`slideLeft`**（不是注释中的 slideRight） | html | 还支持 slideRight/slideDown/slideUp/pop。 |
| `text` / `title` | 文案/提示标题 | 配置对象未设置 | html | 不将 DTD 注释中的列备注回退视为 Parser 行为。 |
| `icon` | 图标 | 未设置 | html | 图标资源由宿主提供。 |
| `keySelector` | 取记录键的选择器 | `#id` | html | 配合页面实际 DOM。 |
| `hideExpression` | 隐藏条件 | 未设置 | html | 具体消费以 HTML 控件实现为准。 |
| `method` / `disabledExpression` | 回调/禁用条件 | DTD 声明但 Parser 未读取 | html | **当前未生效**，不要假定配置后自动创建回调。 |

#### 7.4.3 `htmlButton`

generateHtml、普通 descriptor、innerList 下可声明按钮；Vue 列表按钮另见第 5.3 节。按钮定义不等于自动加入所有工具栏，需有对应动作键引用/模板消费；innerList 的 vxeListButtons/actionColumn 会参与动作描述转换。

| 属性 | 含义 | 默认值或值来源 | 影响 | 备注 |
|---|---|---|---|---|
| `id` | 动作键 | DTD 必填 | vue3和html | 应与工具栏/行操作动作键匹配。 |
| `label` / `title` | 文案/标题 | 未设置 | vue3和html | 标准动作可能另有既有描述；自定义动作应明确提供。 |
| `handler` | 处理函数 | 未设置 | vue3和html | 引用宿主实现，不会自动实现业务。 |
| `icon` / `elIcon` | 图标 | 未设置 | vue3和html | 按实际前端实现选用，不能保证两种图标通用。 |
| `type` | 按钮语义样式 | `primary` | vue3和html | primary/success/warning/danger/info。 |
| `text` / `isLink` / `isRound` / `isCircle` / `isPlain` | 文本、链接、圆角、圆形、朴素样式 | 均 `false` | vue3和html | 是否呈现取决于动作注解/模板及组件支持。 |
| `classes` / `css` | 类名/内联样式 | 未设置 | vue3和html | 不自动生成样式资源。 |
| `showCondition` | 显示条件 | 字符串 `true` | vue3和html | 不同于 hideExpression 的隐藏语义。 |
| `disabledCondition` | 禁用条件 | 字符串 `false` | vue3和html | 不是服务端授权。 |
| `configurable` | 是否可配置 | `false` | vue3和html | 由消费动作元数据的宿主解释。 |
| `localeKey` | 国际化键 | 未设置 | vue3和html | 语言资源须存在。 |
| `componentType` | 动作组件类型 | `button` | vue3和html | icon/text/popSelect/button。 |
| `uiFrameType` | DTD 预留框架选择 | Parser 未读取 | vue3和html | **当前未生效**，不切换按钮生成分支。 |

### 7.5 附件与审批意见

#### 7.5.1 `htmlFileAttachment`

| 属性 | 含义 | 默认值或值来源 | 影响 | 备注 |
|---|---|---|---|---|
| `generate` | HTML 附件生成标志 | `true` | html | **Vue 元数据插件遍历所有附件，不检查此标志**；false 不保证移除 Vue 上传组件。 |
| `elementKey` | 上传组件唯一键 | DTD 必填 | vue3和html | Parser 缺省分支按字符串拼接产生 upload_tmp_field01、upload_tmp_field11 等，但验证型配置不可省略。 |
| `multiple` | 多附件 | `true` | vue3和html | 单文件须显式 false。 |
| `type` | 文件列表展示形式 | `text` | vue3 | text/picture/picture-card；不等于文件后缀限制。 |
| `location` | 存储类型 | `table` | vue3和html | DTD 接受 file/table/local/oss；不表示各宿主均实现所有类型。 |
| `limit` | 数量上限 | `0` | vue3 | 按协议表示不限制；Parser 解析整数。 |
| `tips` | 上传提示 | 空字符串 | vue3 | 非服务端文件校验规则。 |
| `restBasePath` | 上传/访问基础路由 | `/bizcore/vbiz-file-attachment-impl` | vue3和html | 是框架约定路径，不是文件系统目录；本地/OSS 方案须匹配宿主服务。 |
| `label` | 标题 | `附件` | vue3和html | Vue 非默认值才显式输出。 |
| `afterColumn` | 定位到指定列后 | 未设置 | vue3和html | HTML 未命中位置通常追加，Vue 依运行时布局。 |
| `exclusive` | 独占行 | `true` | html | Vue 上传描述器未读取该字段，span 固定初值 24。 |
| `order` | 附加组件顺序 | `10` | html | **Vue 上传描述器未复制配置 order**，仍按自身 10 判断，不保证此值改变 Vue 排序。 |
| `hideExpression` | 隐藏条件 | 未设置 | vue3和html | Parser 保存，但 VueFormUploadMetaDesc 未传递；不可依此隐藏 Vue 上传组件，HTML 同样需核对具体模板。 |
| `verify` | 必填/不校验 | DTD 声明 none/required，Parser 未读取 | vue3和html | **当前未生效**。 |

#### 7.5.2 `approvalComment`

| 属性 | 含义 | 默认值或值来源 | 影响 | 备注 |
|---|---|---|---|---|
| `generate` | 审批组件生成标志 | **`false`**（Java 布尔初值，不是 DTD 注释中的 true） | html | Vue 规则只判断配置列表非空，描述器未检查 generate；false 不保证移除 Vue 审批组件。 |
| `elementKey` | 组件键 | DTD 必填 | vue3和html | 不自动实现审批业务。 |
| `afterColumn` | 放置位置 | 未设置 | vue3和html | 使用实际存在的列。 |
| `label` | 标题 | `审批意见` | vue3和html | 对应组件文案。 |
| `placeholder` | 输入提示 | `请输入审批意见` | vue3和html | 不属于必填校验。 |
| `locationTag` | 业务位置标记 | `审批意见` | vue3和html | Vue 输出为 busLocation。 |
| `rows` | 文本行数 | `3` | vue3和html | Parser 整数解析。 |
| `tips` | 提示 | 空字符串 | vue3 | 写入审批元数据。 |
| `hideExpression` / `verify` | 隐藏/校验 | DTD 声明，Parser 未读取 | vue3和html | **当前未生效**；verify 为 none/required。 |

Parser 还读取 approvalComment 的 span（默认 24），但当前 DTD 未声明，不能在验证型配置中添加此属性。

### 7.6 内嵌列表与打印

#### 7.6.1 `htmlElementInnerList`：来源与布局

| 属性 | 含义 | 默认值或值来源 | 影响 | 备注 |
|---|---|---|---|---|
| `elementKey` | 组件键 | DTD 必填 | vue3和html | Vue 描述器的备用算法为模块 + 默认 HTML 键 + 序号，但不是可省略 DTD 必填属性的依据。 |
| `listKey` | 列表标识 | 未设置，不自动生成 | vue3和html | 不同于组件键；应对应真实列表配置。 |
| `label` | 列表标题 | 配置初值空；Vue 回退为**当前主表**简要备注 | vue3和html | 不应声称自动读取来源表备注。 |
| `showTitle` | 显示标题 | **`false`**（配置类初值） | vue3 | 描述器会据此输出 showTitle=false。 |
| `moduleKeyword` | 来源模块 | 显式值转小写；否则取 Context 模块转小写 | vue3和html | Context 必须提供有效模块关键字。 |
| `sourceBeanName` | 来源 bean | DTD 必填 | vue3和html | Parser 同时计算短横线形式，供路由使用。 |
| `relationField` | 关联字段 | DTD 必填 | vue3和html | 不自动增加数据库外键或实现级联保存。 |
| `sourceListViewClass` | 来源列表 VO 全类名 | 未设置 | vue3和html | 必须有可供宿主读取的列表元数据。 |
| `relationKey` | 关联记录键 | `id` | vue3和html | 与主表/来源对象键保持一致。 |
| `tagId` | 前端列表引用标识 | DTD 必填 | vue3和html | 不与其他列表引用冲突。 |
| `dataField` | 主表响应中的子列表属性 | 未设置 | vue3和html | 与 dataUrl 选择合适加载方式；Parser 不校验二选一，需人工保证数据契约。 |
| `dataUrl` | 子列表请求地址 | 未设置；getter 可补 `/` | vue3和html | 不自动生成该接口。 |
| `span` | 栅格宽度 | `24` | vue3 | 不等于字段列数。 |
| `afterColumn` | 放置位置 | 未设置 | vue3和html | 指定已有列。 |
| `containerType` | 列表容器 | 未设置，不是自动 none | vue3和html | none/tab/border；与 layout/groupContainer 分开配置。 |
| `order` | 附加组件顺序 | `10` | vue3和html | Vue 内嵌列表描述器会复制此值，与附件不同。 |
| `restBasePath` | 来源 REST 基础路径 | 未设置 | vue3和html | **当前 Parser 偏差**：显式非空且 sourceBeanName 非 null 时，反而重算为“局部 moduleKeyword 属性值 + `/` + bean 短横线名”；未填时仍为空。仅靠 Context 回退可能得到 null 前缀，不可假定总按配置值/自动路由生效。 |
| `sourceViewPath` | HTML 来源视图名 | 初值空；显式转小写，getter 取末段 | html | 不控制 Vue 组件文件路径。 |
| `title` | DTD 预留标题 | Parser 未读取 | vue3和html | **当前未生效**；使用 label 或容器 title。 |

#### 7.6.2 `htmlElementInnerList`：交互、操作与汇总

| 属性 | 含义 | 默认值或值来源 | 影响 | 备注 |
|---|---|---|---|---|
| `editMode` | 行/单元格/禁编辑 | `row` | vue3 | row/cell/none；不等于保存接口权限。 |
| `editableFields` | 可编辑字段 | 空集合 | vue3 | 逗号分隔；与来源 VO 字段对齐。 |
| `enablePager` | 分页 | `false` | vue3 | 不是主列表 viewVo 的分页配置。 |
| `vxeListButtons` | 工具栏动作键 | 空列表 | vue3 | 逗号分隔，输出 toolbarActions。 |
| `actionColumn` | 行操作动作键 | 空列表 | vue3 | 逗号分隔，输出 columnActions；可配子 htmlButton。 |
| `showActionColumn` | 行操作列显示策略 | `default` | vue3 | **当前 VueFormInnerListMetaDesc 未复制/输出此字段**，不能保证生效。 |
| `actionColumnWidth` | 操作列宽度 | 未设置 | vue3 | 作为字符串进入元数据。 |
| `defaultFilterExpr` | 默认过滤 SQL | 未设置 | vue3 | 不是隐藏表达式，也不是可信服务端授权条件。 |
| `batchUpdateColumns` | 批量修改列 | 空集合 | vue3 | 配套接口、字段白名单及权限仍需实现。 |
| `defaultSort` | 默认排序 | 未设置 | vue3 | 传入列表元数据；服务端仍需约束可排序字段。 |
| `showRowNumber` | 显示行号 | `true` | vue3 | false 时显式输出。 |
| `enableEdit` | 编辑策略 | 字符串 `default` | vue3 | default/true/false，不是 boolean 属性。 |
| `totalRow` | 合计行 | `false` | vue3 | 不自动生成数据库统计。 |
| `totalFields` | 合计字段 | 空集合 | vue3 | 逗号分隔；字段应支持汇总。 |
| `totalText` | 合计文案 | `合计` | vue3 | 与合计数据计算分开。 |
| `editFormIn` / `detailFormIn` | 编辑/详情打开方式 | 配置对象未设置；描述器以 dialog/drawer 为省略比较值 | vue3 | inner/dialog/drawer；最终缺省由 basic 注解与宿主决定。 |
| `hideExpression` / `disabledExpression` | 隐藏/禁用条件 | 未设置 | vue3 | 两者均进入内嵌列表注解。 |
| `dataUrlParams` | 请求参数 | 未设置 | vue3 | **Parser 保存但 Vue 内嵌列表描述器未传递**，不能依此保证请求携带参数。 |
| `verify` | 必填/不验证 | 配置初值空列表；DTD 为 none/required | vue3 | 注解描述器默认 none，实际配置 required 会输出；仍需服务端校验。 |

#### 7.6.3 `htmlElementInnerList`：打印

| 属性 | 含义 | 默认值或值来源 | 影响 | 备注 |
|---|---|---|---|---|
| `printMode` | HTML 打印布局 | `table` | html | table/form，不触发 Vue3 打印组件。 |
| `printFormColumnsNum` | 表单式打印列数 | `2` | html | Parser 整数解析。 |
| `printFields` | 打印字段 | 空列表；getter 去重 | html | 逗号分隔，按打印模板支持范围消费。 |

### 7.7 Vue3 页面生成、接入与覆盖边界

Vue3 文件由 `Vue3OtherFilesPlugin` 与 `Vue3FormFilesPlugin` 生成；对应源码和模板分别位于 `src/main/java/org/mybatis/generator/plugins/vue3/` 与 `src/main/resources/templates/vue3/`。与 `generateHtml` 相关的规则共用表级页面配置：**至少配置一个已启用的 editable 页面才能触发编辑型 Vue3 页面**，只读页面配置则可触发详情组件。除此以外，必须在 Context 设置非空 `vueEndProjectPath`，且 model-only 表不会生成页面。

当前版本的生成条件及输出如下。`{Domain}` 为解析后的 `domainObjectName`，`{domainLower}` 为其小写形式：

| 文件（相对于 `src/modules/{moduleKeyword}/`） | 生成条件 |
|---|---|
| `views/{Domain}.vue` | 至少一个 editable 页面配置启用，且 `generateVo/viewVo` 启用 |
| `{domainLower}/components/{Domain}Edit.vue` | 至少一个 editable 页面配置启用 |
| `{domainLower}/components/{Domain}Detail.vue` | editable 或 viewOnly 页面配置启用 |
| `modals/{Domain}Modal.vue` | 至少一个 editable 页面配置启用 |
| `{domainLower}/types/T{Domain}.ts` | editable 页面配置启用或任一 VO 生成规则启用，且有收集到的 VO 字段 |
| `{domainLower}/PrivateUseFormHooks.ts`、`{domainLower}/sharedVariables.ts` | 至少一个 editable 页面配置启用 |
| `{domainLower}/PrivateTableColumnSlots.vue` | editable 页面配置启用，且 `viewVo` 启用 |

以上 Vue3 产物的完整清单与权威设计边界见[生成器权威说明第 2.2 节](generator-authoritative-spec.md#22-vue3-前端可生成能力)。在多 `generateHtml` 配置场景下，检查是否存在有效 `generate="true"` 且 `type="editable"` / `type="viewOnly"` 的配置；不要只看 XML 中是否出现了 `generateHtml` 节点。

#### 7.7.1 Vue3 与 `generateHtml` 的职责边界

1. `generateHtml` 是表级页面配置节点。它配置的 `type`（`editable`、`viewOnly`、`print`）及 `generate` 值参与 HTML 页面规则判断，也参与 Vue3 editable/viewOnly 产物规则判断；`generate="false"` 或 model-only 不能触发相应页面。
2. 页面配置存在两条消费链：一条输出 HTML/静态资源；另一条经 `VueHtmMetaAnnotationPlugin` 生成 VO 的表单、字段、容器、上传、内嵌列表及审批注解，再由后端配置服务与 Framework `vgoForm` 消费。`layout` 的字段列数/独占列、标签/弹窗配置，以及多数输入控件和附加组件配置会通过第二条链路影响 Vue3；HTML loadingFrameType/barPosition/uiFrameType、文件路径/覆盖、打印项不能据此当作 Vue3 配置。逐项影响与未传递限制以 7.1～7.6 表格为准，尤其不能用直接子节点 htmlSwitchDescriptor 代替有效普通 descriptor。
3. Vue3 列表需要 `viewVo`，其视图标识、标题、列/查询/过滤、工具栏、默认排序和菜单元数据等配置按第 5 节 `viewVo` 配置及当前 DTD 生效。`generateVo` 父级必须启用，`viewVo` 子项必须实际启用；仅启用 `generateHtml` 不会生成列表页。
4. Vue3 编辑/详情模板本身是组件骨架；表单字段定义、数据源和控件如何装配，应通过已生成的 VO/表单元数据及服务端返回的配置确认。不要仅因某列存在于 Entity 或 TypeScript 类型中，就假定它会出现在查询区、表格列或表单中。

#### 7.7.2 `generateHtml` 的附带 HTML 产物

`generateHtml generate="true"` 不只是 Vue3 的触发标志：同一配置也会进入生成器的 HTML 输出路径。当前 core 的解析器将 HTML 根目录默认为 `context.htmlTargetProject`，未配置时为 `src/main/resources/templates`；HTML 包路径默认为 `context.htmlTargetPackage`，未配置时取 `moduleKeyword`，最终 HTML 路径由包路径和 `viewPath` 拼成。未显式设置 `viewPath` 时使用表名小写。

对 `type="editable"`，当前插件还会在 `src/main/resources/static/js/{moduleKeyword}` 输出以 `viewPath` 最后一段命名的 `.js` 与 `.min.js` 文件。HTML、JS、CSS 的 `overWrite*File` 缺省均为 `false`；已有文件是否被跳过或覆盖，须按当前 core 的写文件逻辑和运行 Diff 确认。CSS 另受布局边框样式是否偏离默认值约束，不应仅因启用 editable 就预期必然产生 CSS。

因此，启用 editable 以取得 Vue3 页面时，授权范围和生成前清单必须同时列出 Vue3 输出及此处的 HTML/静态资源输出；不能只核对 `vueEndProjectPath`。该结论来自当前 Parser、HTML 文件收集和 JQuery/CSS 插件源码，仍须在实际目标配置生成后核对文件清单及覆盖结果。

Framework API/组件契约以 mpmm-ui 的[Framework 能力总览](D:\vscode\mpmm-ui\packages\framework\docs\framework-capability-overview.md)和[组件开发与复用能力指南](D:\vscode\mpmm-ui\packages\framework\docs\components-reuse-capability-overview.md)为复用依据；mpmm-ui 的[OrgRole 列表页](D:\vscode\mpmm-ui\src\modules\organization\views\OrgRole.vue)及其[编辑组件](D:\vscode\mpmm-ui\src\modules\organization\orgrole\components\OrgRoleEdit.vue)、[Hooks](D:\vscode\mpmm-ui\src\modules\organization\orgrole\PrivateUseFormHooks.ts)、[列插槽](D:\vscode\mpmm-ui\src\modules\organization\orgrole\PrivateTableColumnSlots.vue)仅用于核对真实宿主接入方式。示例项目的业务实现不能取代本生成器的 DTD、配置解析、插件/模板源码，也不构成其他宿主必须照搬的方案。

#### 7.7.3 最小配置片段

下面是放入既有 `<context>` 的 `<table>` 配置片段，不是完整可执行的 `generatorConfig.xml`。`vueEndProjectPath` 应配置在 `<context>` 上，值为 Vue 项目根目录（不含 `src`）；表配置顺序遵循当前 DTD：

```xml
<table tableName="biz_example" domainObjectName="BizExample">
    <generateHtml generate="true" type="editable"/>
    <generateVo generate="true">
        <createVo generate="true"/>
        <updateVo generate="true"/>
        <viewVo generate="true"
                title="示例管理"
                queryColumns="name_"
                fuzzyColumns="name_"
                filterColumns="state_"
                defaultSort="created_ desc"
                defaultDisplayFields="name_,state_,created_"/>
        <requestVo generate="true"/>
    </generateVo>
</table>
```

这是启动 Vue3 列表、编辑、详情及弹窗骨架的配置起点，不代表页面功能完整。根据需求补齐 VO 字段范围、必填/忽略校验、`queryColumn` / `filterColumn` 细项、`fieldOverrides`、列表按钮、值转换、附件和 innerList 等；每项均以当前 DTD 可接受的节点/属性及解析源码为准。仅需要只读详情时可配置启用的 `type="viewOnly"` 页面，但不会因此得到列表页或编辑/弹窗文件。`layout` 中已传入表单注解的配置会影响 Vue3 运行时布局，但不会生成任意定制 Vue 模板；HTML 文件覆盖属性不控制 Vue3 覆盖。

#### 7.7.4 页面与配置项对应关系

| 设计目标 | 优先检查的配置 | 说明 |
|---|---|---|
| 列表字段、默认显示和排序 | `generateVo/viewVo` 的 `includeColumns` / `excludeColumns`、`defaultDisplayFields` / `defaultHiddenFields`、`defaultSort`、`fieldOverrides` | ViewVo 是列表页面视图的主要配置入口；核对属性名与数据库列名的转换关系 |
| 复合查询和模糊查询 | `viewVo.queryColumns`、`fuzzyColumns`、`queryColumn` | 查询字段必须与 RequestVo、接口查询实现相匹配 |
| 过滤器 | `viewVo.filterColumns`、`filterColumn` | 明确字段、控件/操作和值域；需要跨表共用时可评估 `reusableElements` |
| 列表和行操作 | `viewVo.toolbar`、`actionColumn`、`htmlButton` | 确认按钮 ID 对应已生成动作或已实现的业务扩展，不可只配置前端按钮 |
| 新增/编辑/详情数据模型 | `generateVo` 及 `modelVo`、`createVo`、`updateVo`、`viewVo` | 列表/表单用途不同，分别核对字段白名单、必填项与校验忽略项 |
| 表单控件和页面组件 | `htmlElementDescriptor`、`htmlTableSelectDescriptor`、`htmlFormExtendDescriptor`、`htmlFileAttachment`、`approvalComment`、`htmlElementInnerList` | 对照 7.3～7.6 的属性传递链与限制；页面直接子节点 htmlSwitchDescriptor 当前未解析，开关用普通 descriptor 的 tagType=switch |
| 多表复用 | `reusableElements` 中受支持的字段、控件、列和过滤器描述 | 仅复用指南和当前 DTD/源码实际支持的描述，表级配置保留差异项 |
| 页面菜单及权限 | `viewVo.parentMenuId`、ViewVo 菜单属性，以及 Context/Table 的模块和菜单数据配置 | 确认初始化 SQL 纳入部署链路、父节点和权限数据正确；具体生成条件见项目菜单规范及生成器菜单配置说明 |

#### 7.7.5 Vue3 产物覆盖策略

Vue3 列表、编辑、详情、Hooks 和列插槽文件，仅当既有目标文件前 10 行包含完整 `@VCF.overridable` 标记时才被插件标记为可覆盖；没有该标记的既有文件会保留。`T{Domain}.ts`、`{Domain}Modal.vue` 和 `sharedVariables.ts` 在当前插件中显式设为覆盖，不能依赖标记保护人工内容。最终写盘还受运行时 `ShellCallback` 覆盖模式影响。

开始人工维护之前，逐文件核实生成策略，并遵守 `.github/copilot-instructions.md` 第 2.1 节：对于允许扩展且按标记控制的文件，开始人工扩展时先将完整标记改为无法严格匹配的形式，确认标记不再存在；恢复标记前须确认扩展已迁出或可以丢弃。对显式覆盖文件应将必要扩展放入已验证的 Hooks、列插槽或宿主模块扩展点，不应直接依赖文件标记保留内容。生成器升级或修改插件后，重新核验本节条件和生成物差异。

#### 7.7.6 生成 HTML view 路由的权限边界

当前 `ViewElementGenerator` 为生成 Controller 的 GET `view` 方法添加 `@PermitAll`。该方法可按可选 ID 读取实体并把 VO 放入页面模型；在会把 `@PermitAll` 转换为匿名 URL 放行规则的宿主安全配置中，这不等价于“已登录用户可访问”或“具有模块管理权限”。任何启用该路由的管理页面都必须核对实际安全配置和读数据路径，并在业务扩展层实施合适的权限保护；不得只依赖列表页按钮权限或前端菜单隐藏。不要直接修改 `codegen` Controller，优先在允许维护的 Controller 扩展壳覆盖路由，并以实际宿主 HTTP 请求验收匿名拒绝和授权访问。该行为来自生成器 Controller 源码；具体宿主如何解释 `@PermitAll` 仍须在对应安全配置中核实。

### 7.8 源码核对入口与补充规则

以下路径相对于 core 模块 `src\main\`。本章记录当前实现，不授权业务重新生成，也不在本次文档补齐中修复已标出的生成器偏差。

| 核对内容 | 源码入口 |
|---|---|
| 合法节点、属性、顺序及必填项 | `resources\org\mybatis\generator\config\xml\mybatis-generator-config_1_8_0.dtd` |
| 属性读取、类型转换、未处理子节点 | `java\org\mybatis\generator\config\xml\MyBatisGeneratorConfigurationParser.java`：parseHtml、parseHtmlLayout、parseHtmlElementDescriptor、各附加组件解析方法 |
| 初值、字典 URL、快捷格式、默认列与字段状态合并 | `java\org\mybatis\generator\config\Html*Configuration.java`、`HtmlLayoutDescriptor.java`、`HtmlElementDescriptor.java`、`AbstractHtmlElementDescriptor.java`、`TableConfiguration.java`、`ConfigUtil.java` |
| Vue 元数据默认页面选择、字段 span 和属性复制 | `java\org\mybatis\generator\plugins\VueHtmMetaAnnotationPlugin.java` |
| 注解实际输出及省略成员 | `java\org\mybatis\generator\custom\annotations\VueForm*MetaDesc.java`、`VueFormItemRuleDesc.java` |
| Vue 控件/日期格式/校验推导 | `java\org\mybatis\generator\codegen\mybatis3\vue\VueFormGenerateUtil.java` |
| HTML 框架分支、打印与附加组件 | `java\org\mybatis\generator\plugins\JavaClientGeneratePlugins.java`、`java\org\mybatis\generator\codegen\mybatis3\htmlmapper\document\` |
| Vue 文件产物、模板和覆盖 | `java\org\mybatis\generator\plugins\vue3\`、`resources\templates\vue3\` |

后续补充每个属性时，分别确认“DTD 可配置”“Parser 已读取”“计算后最终值”“描述器/模板已传递”“宿主实际消费”五个环节；未输出的注解成员再核对项目实际依赖的 basic 注解默认值。配置存在、生成成功、页面能打开，都不能单独证明某个交互行为已生效。

## 8. 查询 / 关联 / 统计

| 节点 | 含义 | 备注 |
|---|---|---|
| `selectByColumn` | 按列查询 | `column` 支持多列，`genControllerMethod` 可生成接口。 |
| `selectByTable` | 中间表查询及可选关系维护 | 适合多对多；配置语义与 `javaModelCollection` 不同，详见本节下文及第 13.2 节。 |
| `selectBySqlMethod` | SQL 方法查询 | 适合树/祖先/后代。 |
| `javaModelAssociation` | 一对一 | 级联行为需配 `relationProperty` / `beanClassFullName`。 |
| `javaModelCollection` | 一对多 | 常用于 `children` / 子查询。 |
| `generateTreeViewCate` | 树接口 | 依赖 `id/name/parentId` 语义。 |
| `generateOptions` | 下拉选项接口 | `flat` / `tree`。 |
| `generateDateDimStatistics` | 时间维度统计 | `total/yoy/mom`。 |
| `generateCustomStatistics` | 自定义统计 | 支持 `joinTableClause`。 |
| `joinTableClause` | 统计关联表 | 连接表达式必须是 SQL 表达式。 |

### 8.1 按列逻辑删除

1.8.0 源码已实现，尚未安装或发布。

```xml
<selectByColumn column="id_,version_" enableDelete="true"
                parameterType="single" genControllerMethod="true">
    <property name="isLogicalDelete" value="true"/>
</selectByColumn>
```

当前 DTD 允许 `selectByColumn` 包含 `property*`，Parser 复用既有属性解析。`isLogicalDelete` 仅接受字面 `true` / `false`，省略或 false 保持原物理删除全部输出；空值、空白及大小写变体明确失败，不静默退回硬删除。使用旧版 core/DTD 不支持该配置，须同步供应当前生成器及运行依赖。

| 项目 | 显式逻辑删除契约 |
| --- | --- |
| 适用范围 | MyBatis3/XMLMAPPER；实际表须含非空、可写的 Integer/INTEGER `delete_flag`，固定 0 有效、1 删除 |
| 无版本表 | 按配置列等值或 IN 交集更新 `delete_flag=1`，附加 `delete_flag=0`；列表不是逐行条件配对 |
| 有 `version_` 的表 | 必须提交完整主键及旧版本，单值参数；版本列须为非空可写 Integer/INTEGER 或 Long/BIGINT；同条 UPDATE 更新标志和版本加一，拒绝不完整条件、版本化列表及溢出 |
| Controller | 仅 `genControllerMethod=true` 输出，示例为 `POST {basePath}/delete-by-column-id-and-version`；`@RequestParam` 接收 String id、Integer version，可用 query/form 传参，不是 JSON 请求体；复用既有 delete 权限及操作日志，未启用 Spring Security 时遵循现有开关 |
| Service/Mapper | `int deleteByColumnIdAndVersion(String id, Integer version)`，Mapper 参数显式 `@Param`；Service 不捕获失败，版本表要求影响行数严格为 1，重复删除/旧版本抛出冲突，不伪装幂等成功 |
| 事务/缓存/事件 | 普通逻辑删除 Service 为 REQUIRED、Exception 回滚；沿用 mutation cache eviction。新分支不发布旧物理 PRE_DELETE/DELETED 事件，业务如需要应在扩展层单独编排 |
| 默认兼容 | 不修改普通删除、旧 GET/POST 物理删除、其他表及查询语义；历史 DTD 不变。不要以省略新开关误启用逻辑删除 |

SQL 使用普通 UPDATE、等值/IN 和算术自增，无方言专属函数，不需新增方言分组；受管 CAS 仍按原有 mysql/h2 支持范围逐 `additionalDbTypes` 输出，不扩大运行支持矩阵。

当 table 同时启用 `enableCasWriteContract=true` 时，仅允许 id_/version_ 两个单值条件（配置顺序可以调整），新方法输出明确的 `LOGICAL_DELETE` 声明、StaticWriteLanguageDriver 及 MANDATORY 服务事务；在既有工厂显式装载更新后的契约/门禁，审计由服务端 Provider 提供。SQL 固定 `delete_flag=1`、version 自增、可信修改信息，并以 id/旧 version/`delete_flag=0` 限定一行。原物理删除和其他危险写仍为 ENTRY_DISABLED；直接 Mapper/statementId 同样核验版本、参数隔离、行数及整事务回滚。**该组合需要包含 LOGICAL_DELETE 支持的当前 `vgosoft-mybatis` 工件**，旧 SNAPSHOT 运行包不支持新操作；不能只更新 generator。

通用逻辑删除不计算墓碑摘要、不推进领域 revision、不检查业务引用。Sensitive 后续须在已接管覆盖标记的 Service 扩展壳重写同名方法：统一事务中先取得 revision 门禁、读取并校验，再构造含墓碑的记录，通过既有完整/选择性 CAS 一次写入摘要和删除状态；不能先调用通用逻辑删除再补第二条无版本摘要 UPDATE。Controller 调用的是服务接口，重写后仍可复用路由；业务墓碑/恢复、旧删除路由迁移、批量配对及权限部署另行验收。

源码归属：`SelectByColumnGeneratorConfiguration` / Parser 负责配置，`LogicalDeleteGenerationValidator` 负责实际列/模型预检；沿用 `DeleteByColumnGenerator`、`DeleteByColumnElement`、`JavaMapperGenerator`、`DeleteByColumnElementGenerator` 的分层输出；受管声明与最终插件校验仍由既有 CAS classes 负责。正式回归为 `LogicalDeleteGenerationTest`、basic `LogicalDeleteContractTest` 及 Plus `GeneratedLogicalDeletePlusTest`；隔离生成不代表用户业务生成或宿主已经验收。

### 8.2 关系查询说明

`selectByTable` 的 `thisColumn` 是关系表中属于当前 table 主键的一侧，`otherColumn` 是对端主键的一侧。查询当前实体时，以一个或多个对端 ID 过滤关系表；双向查询需在另一实体下使用相同关系表并交换两列。`javaModelCollection` 是实体上的一对多集合/子查询/级联配置，不会由 `selectByTable` 自动创建或填充关系集合属性。

可选 `relationIdKey` 显式启用受管关系生成；两侧都必须配置相同稳定逻辑关系标识和显式共享 `<enableCache cacheNames="..."/>`。关系表 table 的 `relationKeyColumns` 单独声明 DDL 约束覆盖。未选择该模式的关系保持旧行为；完整契约与离线再生成见第 13.2.1 节。

该节点不接受 `genControllerMethod`、`enableInsert` 或 `enableDelete`。配置后生成 Service/Mapper/XML 查询；`enableSplit` 和 `enableUnion`（默认均为 `true`）分别控制关系删除/插入方法及对应 Controller 端点。只要其中任一开关为真，也会生成该配置的 select Controller 端点；两者都为假时，查询 Service/Mapper/XML 仍生成，但不生成这些 Controller 端点。普通 CRUD 开关与这组关系开关互不替代。

## 9. 其他辅助配置

| 节点 | 含义 |
|---|---|
| `htmlButton` | 自定义按钮。 |
| `queryColumn` | 查询列配置。 |
| `filterColumn` | 过滤器列配置。 |
| `nameFragment` | `requestVo` 查询片段映射。 |
| `mapstructMapping` | DTO/VO 映射。 |
| `reusableElements` | 表级复用配置池。 |

## 10. 常见歧义 / 纠错

| 文档写法 | 建议按源码理解为 |
|---|---|
| `sortColumn` | `orderByClause` |
| `enableSelectByIds` | `enableSelectByPrimaryKeys` |
| `excludedTable` | `excludedTables` |
| `Organization` / `Organ` | 以源码枚举/实现为准，不以注释单词判断 |
| `queryColumn` 中 `switchText` | 该属性不在 DTD 中，不能直接配置 |

## 11. 快速配置策略

1. **先看表语义**：数据表 / 关系表 / 树表 / 模块主表。
2. **再看方言**：是否需要 `additionalDbTypes` 分支。
3. **再定输出**：实体、VO、XML、DAO、Service、Controller、HTML。
4. **最后补页面和映射**：字典、展示字段、渲染函数、校验、附件、内嵌列表。

## 12. 推荐使用方式

- 配置前先按本文件找对应节点。
- 对于“显示中文/字典映射/状态渲染”，优先判断是：
  - **值转换**：用 `overridePropertyValue`
  - **列表渲染**：用 `columnRenderFun`
  - **编辑控件**：用 `htmlElementDescriptor`
- 对于不确定项，优先以 **源码行为** 覆盖注释表述。

## 13. 实际案例摘录（已脱敏）

### 13.1 `reusableElements` + 列覆盖

来自 `vgosoft-organization` 的用户表配置：

```xml
<reusableElements>
    <fieldOverrides fields="sort" align="center" width="100"/>
    <fieldOverrides fields="state" align="center" width="110"/>
    <fieldOverrides fields="username_" align="center" sort="true" width="140"/>
    <fieldOverrides fields="mainDeptText" align="center" sort="true" width="160"/>
</reusableElements>
```

用途：把跨表通用的列表列宽、对齐、排序规则集中复用，避免每个表重复写。

### 13.2 多对多 `selectByTable`

下面以 `sys_menu` 通过 `org_role_menu` 关联角色为例。该配置放在 `sys_menu` 的 table 下；反向查询配置放在 `org_role` 下并交换两列。以下为两侧配置片段，分别合入各自项目的生成配置，不是完整可运行的配置文件：

```xml
<table tableName="sys_menu">
    <selectByTable table="org_role_menu"
                   thisColumn="menu_id" otherColumn="role_id"
                   methodSuffix="ForRole"
                   orderByClause="sort_"
                   enableSplit="true" enableUnion="true"/>
</table>

<table tableName="org_role">
    <selectByTable table="org_role_menu"
                   thisColumn="role_id" otherColumn="menu_id"
                   methodSuffix="ForMenu"
                   returnType="primaryKey"
                   enableSplit="true" enableUnion="true"/>
</table>
```

`sys_menu` 侧的方向和生成结果：

| 配置/调用 | 方向与结果 |
|---|---|
| `thisColumn="menu_id"` | 关系表中指向当前菜单（当前 table）的键。 |
| `otherColumn="role_id"` | 关系表中指向角色（对端）的键。 |
| `selectByTableForRole(roleId)` | 按 `org_role_menu.role_id` 查菜单；默认 `parameterType="single"`，Service 返回菜单实体列表并按 `sort_` 排序。Controller 是单个 ID 查询参数。 |
| `split/for-role(menuId, roleIds)` | 接收当前菜单 ID 和 `@RequestBody List<String>`；只删除 `menu_id = menuId` 且 `role_id` 属于 `roleIds` 的关系行。 |
| `union/for-role(menuId, roleIds)` | 接收当前菜单 ID 和 `@RequestBody List<String>`；为该菜单新增与 `roleIds` 中角色的关系，只做增量添加，不删除未提交的其他关系，也不是全量替换。 |
| `selectByTableForMenu(menuId)`（角色侧） | 按菜单 ID 查询角色主键字符串列表；`returnType="primaryKey"` 改为返回当前实体 ID，而不是角色实体。Controller 查询仍是单个 ID 参数。 |

列名会转换为 Java 参数属性名：维护方法的第一个参数是当前侧 ID，第二个参数是对端 ID 列表。查询的 `parameterType="list"` 可将对端查询键改为列表；`returnType="primaryKey"` 将查询结果改为当前实体主键值列表（Java 类型为 `String`），默认 `returnType="model"` 返回实体列表。DTD 允许的 `returnType` 为 `model|primaryKey`，`parameterType` 为 `single|list`。当前生成实现按字符串 ID 处理关系键，查询 join 也使用当前表的首个主键列；因此应按单列字符串主键使用，不能假设复合主键可被正确映射。

`methodSuffix` 生成 `selectByTable` / `deleteByTable` / `insertByTable` 方法名后缀，同时用于 Controller 的 `select/`、`split/`、`union/` 路径片段（如 `ForRole` 对应 `for-role`）。Controller 中 select 端点受 `enableSplit` 或 `enableUnion` 控制，不使用 `genControllerMethod`；该属性属于其他节点（如 `selectByColumn`），不是 `selectByTable` 的 DTD 属性。table 的普通 CRUD 开关（如 `enableInsert`、`enableDeleteByPrimaryKey`）也不控制关系维护方法。

可选 SQL 属性的边界：

| 属性 | 生成位置与限制 |
|---|---|
| `additionClause` | 作为静态 SQL 片段追加到查询的关系表 JOIN `ON` 条件；不是参数化动态筛选，不作用于 split/union。只应配置可信、适用于目标方言的 SQL 片段。 |
| `orderByClause` | 直接追加到查询 SQL 的 `order by`；不作用于 split/union。 |

以下 SQL 和缓存描述适用于**未配置 `relationIdKey` 的旧模式**；受管模式见第 13.2.1 节。维护关系依赖关系表键和标识列的数据库约束。`split` 使用当前 ID 相等条件加对端 ID `IN` 列表，split/union 调用均须提供有效的当前 ID 和有效且非空的对端 ID 列表；空数组不是清空全部关系指令，现有 SQL 未为其生成无操作分支。旧模式 `union` 生成关系行标识时使用 `MD5(CONCAT(thisId, otherId))`，多个数据库类型按 `additionalDbTypes` 生成相应 SQL：

| 方言组 | 插入形式 |
|---|---|
| MySQL / MariaDB | `INSERT IGNORE`。 |
| PostgreSQL / KingbaseES / HighGo / SQLite | `ON CONFLICT DO NOTHING`。 |
| Oracle / DM / Xugu / CacheDB | `INSERT ... SELECT ... FROM dual WHERE NOT EXISTS`。 |
| DB2 | `INSERT ... SELECT ... FROM SYSIBM.SYSDUMMY1 WHERE NOT EXISTS`。 |
| 其他方言 | 通用 `INSERT ... SELECT ... WHERE NOT EXISTS`。 |

关系写入的重复与并发边界应分别评估：

| 场景 | 生成行为与边界 |
|---|---|
| 同一方向重复提交 | 对相同的非空两侧键和拼接顺序，重复关系得到相同 `id_`。原生冲突语法按数据库唯一/冲突约束处理；兜底 SQL 对本次来源用 `SELECT DISTINCT` 去重，并按两侧键检查已有关系。MySQL `INSERT IGNORE` 还可能忽略非重复键错误。 |
| 从反向配置写入同一关系 | 两端 `thisColumn` / `otherColumn` 顺序相反，`MD5(CONCAT(...))` 的输入顺序也相反，因此不能保证生成相同 `id_`；仅有 `PRIMARY KEY(id_)` 不能去重同一业务关系。 |
| 并发写入 | `NOT EXISTS` 检查与插入不是原子操作；无数据库联合唯一约束时，并发调用仍可能插入重复关系。 |

**建设建议**：关系表两侧键按业务语义设为非空，并建立两侧键的联合唯一约束；清理存量重复关系并使约束生效后，才能据此阻止重复关系并避免查询返回重复实体。**生成物与现状边界**：仅配置 `selectByTable` 不会自动推导上述约束；须显式建模 `relationKeyColumns`，或提供已有约束的内省元数据。组织项目角色—菜单样例现已选择受管模式并离线生成 MySQL/PostgreSQL/DM 新建表 DDL，不代表存量数据库已完成迁移；详见[组织项目关系设计 3.6](D:\java\idea_projs7\project-vgosoft\vgosoft-organization\src\main\resources\docs\vgosoft-organization-design-overview.md)。旧模式仍固定使用 `MD5(CONCAT(...))`，不能把拼接哈希当作无歧义的复合键编码。

完整配置可按实际缓存需要添加 `<enableCache>`：

```xml
<selectByTable table="org_role_menu"
               thisColumn="menu_id" otherColumn="role_id"
               methodSuffix="ForRole" enableSplit="true" enableUnion="true">
    <enableCache/>
</selectByTable>
```

用途：

| 配置 | 作用 |
|---|---|
| `enableSplit="true"` | 生成关系解除方法和 split Controller 端点。 |
| `enableUnion="true"` | 生成关系新增方法和 union Controller 端点。 |
| `returnType="primaryKey"` | 查询时返回当前实体主键字符串列表，而不是实体列表。 |
| `<enableCache/>` | 生成缓存支持。 |

`<enableCache>` 可用 `enable="false"` 禁用当前查询缓存（兼容别名 `enableCache="false"`，同时指定时别名优先），也可用 `cacheNames="nameA,nameB"` 指定一个或多个缓存区。完全省略 `<enableCache>` 时不生成查询缓存，也不为该查询增加缓存失效目标。显式缓存名可带 `#<正整数><s|m|h|d>` TTL（如 `userRole#12h`），数值不超过 `Integer.MAX_VALUE`，只允许一个 `#`；毫秒、零、负数及重复后缀均报错。带合法后缀时按原值输出；未指定 TTL 的显式名称默认追加 `#72h`。启用缓存但未指定 `cacheNames` 时，配置了 `relationIdKey` 的受管关系使用 `<relationIdKey>#72h` 作为默认缓存名，双侧配置相同 `relationIdKey` 即共享缓存区；未配置 `relationIdKey` 的旧模式仍按当前查询方向生成默认名称。受管关系省略缓存配置后，关系写入仍保留事务与幂等处理，但不执行缓存失效。关系新增/解除所生成的缓存清理仅能覆盖当前生成器 Context 中可发现的查询配置；分属不同模块/Context 时，不会自动发现并清理另一侧的查询缓存。跨模块启用双侧查询缓存时，两端必须配置相同 `relationIdKey`；若指定 `cacheNames`，两端还必须确保缓存名称一致，否则可能短暂不一致。角色—菜单样例的实际缓存名和调用方向见[组织项目关系设计 3.6](D:\java\idea_projs7\project-vgosoft\vgosoft-organization\src\main\resources\docs\vgosoft-organization-design-overview.md)；当前 HTTP 请求的 query 参数、JSON body 及返回值契约见[前后端接口契约第 6.5 节](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-web2\src\main\resources\docs\frontend-business-capabilities-spec.md)。

同一 `selectByTable` 可配置多个 `<enableCache>`；生成器去重后使用单个 `@Cacheable` 或 `@Caching` 组合注解，避免重复声明不可重复的注解。

查询缓存键使用独立的方法标识、当前租户及带类型的参数结构化序列化；数组、集合和 `Optional` 也按值结构序列化，避免不同方法或不同参数边界相互命中。生成的实体写操作清理实体缓存（默认 `#8h`）及配置可识别的关系查询缓存；SQL builder 明确指定其它表时不会按当前实体推断并清理缓存。

当前缓存键采用 v3 嵌套类型元组，例如 `["vgosoft-cache-key-v3","SysDictData","com.vgosoft.system.entity.SysDictData#selectByKeysDict","10000",["java.util.Optional<java.lang.String>","present","idCardType"],["java.lang.String","1"]]`。普通参数使用 `[声明类型,原始值]`，`Optional` 使用 `[声明类型,"null"|"empty"|"present",原始值或null]`，自定义 SpEL key 使用 `["custom",表达式原始结果]`；字符串不预先编码为 JSON 字符串。

生成的缓存查询类包含一个静态复用的独立 Jackson 3 `JsonMapper` 及序列化方法，SpEL 通过 `#root.target` 调用；整个元组仅序列化一次，输出紧凑 JSON，并固定 Map 键及对象属性排序。不读取应用共享 ObjectMapper、缩进配置或默认类型信息配置，不依赖生成器运行时类，也不逐参数或逐查询新建 mapper。应用特有的共享 mapper 模块/序列化器不会用于缓存键；参数必须能由独立 Jackson 3 mapper 序列化。

迁移时须重新生成对应缓存查询类（保留人工扩展的覆盖保护规则），v3 不读取 v2/更早格式的键；建议发布时清理受影响缓存区，或等待原 TTL 自然淘汰。混合版本部署期间不同版本分别建键；已有写操作的全缓存区失效语义及 TTL 保持不变。

缓存键通过核心门面 `com.vgosoft.core.util.VCurrentUserUtil.getCurrentTenantId()` 获取租户，由已有 `CurrentUserProvider` 提供上下文，不直接依赖 `vgosoft-security`。

关系 ResultMap 的已配置 Mapper 子查询依赖也参与失效解析，支持沿已知 Mapper 依赖传递；未配置的手写 Mapper SQL 不做推断。继承写方法的包装器保持 `public` 签名并调用原 `super` 实现；抽象实现类声明缓存管理器，使保留的扩展子类也能获得生成层配置。

实体写操作清理自身全部已启用的关系查询缓存，包括仅返回主键的查询：实体删除或重新插入会改变经关系表连接返回的主键集合，即使没有 `CachePo` 也生成对应写包装器。

`insertBySql` 和 `updateBySql` 共用目标表失效条件：同时识别调用前空表名、实体表名及基础服务填充的 `表名 as 配置别名`，确保调用后的条件判断仍清理默认目标缓存；明确指定无关表或其它别名时不匹配。

### 13.2.1 显式受管关系与离线再生成

```xml
<!-- 菜单侧；角色侧交换 thisColumn/otherColumn，并改为 ForMenu、returnType="primaryKey" -->
<selectByTable table="org_role_menu" thisColumn="menu_id" otherColumn="role_id"
               methodSuffix="ForRole" relationIdKey="org_role_menu"
               enableSplit="true" enableUnion="true">
    <enableCache cacheNames="orgRoleMenuRelations#72h"/>
</selectByTable>
<!-- 放在生成关系表 DDL 的 table 上；不自动从 selectByTable 推断 -->
<table tableName="org_role_menu" relationKeyColumns="role_id,menu_id">
    <!-- 原有 generateSqlSchema 及其它开关保持不变 -->
</table>
```

| 配置/产物 | 受管模式契约 |
|---|---|
| `relationIdKey` | 两端相同的稳定逻辑关系标识，不是关系行随机主键；不包含租户，不跨 Context 自动发现。实体要求单 String 主键，两关系列也是不同的 String 列。 |
| `cacheNames` | 启用缓存时，两端显式声明同一共享缓存；若省略名称，则按相同 `relationIdKey` 推导 `<relationIdKey>#72h`。关系读与提交后失效仅使用该共享区域，不生成旧区域兼容失效。省略 `<enableCache>` 时不缓存且不执行缓存失效。 |
| Service union | 保留原公开签名及返回计数，调用 `RelationWriteSupport.union`；随机 UUID 由基础包生成，逐行普通 INSERT 由运行时 NESTED 保存点保护。仅唯一键冲突且回滚后确认同一关系已存在时算作幂等重复；其它失败抛出并回滚。 |
| Mapper / XML | 新增 `int insertByTable<Suffix>Row(thisId,otherId,relationId)` 和 `String selectRelationIdByTable<Suffix>(thisId,otherId)`；插入参数 `relationId` 是随机关系行主键，与配置 `relationIdKey` 不同。后者当前/锁定读取关系 `id_`，不存在返回 null，Service predicate 为 `!= null`。受管关系不保留旧危险批量 Mapper 插入入口。普通 INSERT 使用通用 `INSERT ... VALUES`，无 MD5、函数依赖或忽略错误语法。 |
| Service split | 保留增量删除；调用 `RelationWriteSupport.normalize` 校验当前 ID 和非空/非空白列表并去重；空数组绝非清空指令。 |
| `RelationMutation` | `relation` 字段仍使用逻辑关系标识，并仅声明共享关系缓存；基础包负责 REQUIRED 事务、提交前登记持久 outbox 与提交后失效，零行写入也登记失效。不生成普通关系写 `CacheEvict`。 |
| 受管实体 CRUD | 当前实体已有 CacheEvict 的 CRUD/批量/SQL 写包装器另声明全部受管关系：单个逻辑关系仍使用 `RelationMutation`，多个逻辑关系使用 `RelationMutations` 容器。相同 `relationIdKey` 的描述合并缓存范围，各关系的缓存范围保持独立；同一关系首个描述未启用缓存时，也会合并后续描述的缓存。全部关系均未启用缓存时不增加实体失效注解。保留实体普通缓存和非受管关系各自缓存清理，并在外层提交后再次失效共享关系缓存。SQL builder 指向其它表时允许保守多清理；不改变方法体、异常返回或增加主表删除后的关系清理。非受管关系写方法不自动标注。 |
| `Cacheable` | 复用 `CacheAnnotationDesc` 原有完整 v3 key，保留实体、方法、当前租户及带类型的参数；无租户业务键不代表查询上下文无租户。`condition` 在事务内绕过缓存读和填充，不引入额外读注解。实体写入通过现有缓存解析器包含共享缓存，并声明同一 `RelationMutation` 提交后失效，不执行关系数据自动清理。提交后即时清理及延迟补清只降低并发旧查询晚填风险，不保证线性一致。共享 TTL 保持 `#72h`。 |
| Controller | 保留 select/split/union 路由与返回类型；当前 ID 为 query 参数，对端列表只能是 JSON body，与是否生成 Vue 无关。不生成全量原子 save 入口或 form 兼容入口。 |
| `relationKeyColumns` | 显式两列模型覆盖；Schema 生成器复用 `IndexInfo` 生成 NOT NULL、联合唯一索引和反向索引。保留代理主键和已有其它元数据，不添加租户、外键或执行迁移。 |

部署前必须人工清理无效/重复关系并使联合唯一、非空约束生效；保留有效旧行 ID，无需批量换成 UUID。部署基础包的 outbox 表及事务能力验证后再启用双侧写入。目标数据库必须提供关系表事务和 JDBC 保存点，不支持时由运行时显式报错，不能退化为忽略错误。主表删除后的关系清理不在本次模式内。

冲突保存点回滚后的关系验证不能使用普通 COUNT：MySQL 默认 REPEATABLE_READ 可能沿用旧快照。按两侧键读取 `id_`，仅在重复键异常发生后调用，不预先探测；验证语句声明 `useCache="false"` / `flushCache="true"`，避免 MyBatis 查询缓存绕过实际读取。

| 当前读取方言组 | SQL / 边界 |
|---|---|
| MySQL、MariaDB、Oracle、PostgreSQL、DM、HighGo、KingbaseES、GaussDB、UXDB、H2、HSQL | `SELECT id_ FROM ... WHERE pair FOR UPDATE`；事务/保存点与隔离能力仍须由运行时验证。 |
| SQL Server / SQL Server 2005 | `SELECT id_ FROM table WITH (UPDLOCK,HOLDLOCK) WHERE pair`。 |
| SQLite | 不生成 FOR UPDATE；前一 INSERT 已建立单写者事务，冲突回滚后的读取在同一事务内执行。 |
| 其它 DatabaseType | 受管 union 生成显式报错，整个目标方言集合预检后再输出，绝不退化为普通快照 COUNT。未选择受管模式的旧生成行为不变。 |

TiDB、PolarDB、TDSQL、DB2、Xugu、GBase、Phoenix、CacheDB、Informix、Cloudscape、Sybase、OTHER 尚未证明当前读与保存点组合语义，不宣称受管 union 支持；补充等价能力及测试后才能加入。`additionalDbTypes` 包含这些类型时失败提示对应 code；本实例只生成 MySQL/PostgreSQL/DM。目标数据库必须提供关系表事务和 JDBC 保存点，不支持时由运行时显式报错，不能退化为忽略错误。主表删除后的关系清理不在本次模式内。

基础包 `vgosoft.relation.enabled=true` 仅可在人工创建 outbox 结构后开启；缺少配置时受管服务所需写支持不能静默降级。重试与延迟补清的实际调度及故障语义以基础包运行时文档为准；不可把默认约 30 秒的延迟补清当成陈旧数据上界，晚完成的并发读仍可能回填旧值，健康 TTL 淘汰机制下通常以最后一次旧值填充后的 `#72h` 到期为最终边界。目标数据库必须提供关系表事务和 JDBC 保存点，不支持时由运行时显式报错，不能退化为忽略错误。主表删除后的关系清理不在本次模式内。
运行时部署入口为[基础包关系维护指南](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-mybatis\src\main\resources\docs\relation-maintenance-guide.md)；角色—菜单的备份映射、最小有效旧 ID 去重和约束补齐见[组织迁移手册](D:\java\idea_projs7\project-vgosoft\vgosoft-organization\src\main\resources\docs\org-role-menu-migration-runbook.md)。仅维护引用，不复制运行时权威正文。

禁止为了更新这组保护产物运行会连接业务库的普通内省流程。已提供 `src\test\java\org\mybatis\generator\offline\ManagedRelationOfflineRegenerator.java`：读取两份配置中的选定关系，使用人工快照元数据直接调用现有 Service/Mapper/XML/Controller DOM 与 Schema 生成器；先把选定文件写到 `target\offline-relations`，再机械替换生成方法/语句块，保留其它内容和换行。不会执行 `DatabaseIntrospector`、`ValidateDatabaseTable` 或更新 init 数据。

在生成器根目录、JDK 21 / Maven 3.9.12 下执行（PowerShell 每个 `-D` 参数加引号）：

```powershell
mvn -q -pl mybatis-generator-core "-Denforcer.skip=true" `
  "-Dtest=ManagedRelationGenerationTest,PrimaryKeyRelationCacheGenerationTest" `
  "-Doffline.basicRoot=D:\java\idea_projs0\trunk\vgosoft-basic" `
  "-Doffline.organizationModule=D:\java\idea_projs7\project-vgosoft\vgosoft-organization" test
```

上述命令仅生成待审文件；审查差异后在同一命令增加 `"-Doffline.write=true"` 才落地。快照只覆盖当前角色—菜单两侧和三种 DDL，不应将其当作任意业务表的完整离线内省替代。扩展到其它关系必须补充相应已验证元数据与保护边界测试。

### 13.3 `htmlElementDescriptor` 的字典/组织/用户联动

来自组织模块的用户编辑页：

```xml
<htmlElementDescriptor column="role_type" tagType="dropdownlist" dataSource="DictEnum"/>
<htmlElementDescriptor column="job_status" tagType="dropdownlist" dataSource="DictEnum"/>
<htmlElementDescriptor column="id_card_type" tagType="dropdownlist" dataSource="DictData"/>
<htmlElementDescriptor column="main_dept_id" tagType="select" dataSource="Department"/>
<htmlElementDescriptor column="other_dept_id" tagType="select" dataSource="Department" multiple="true"/>
<htmlElementDescriptor column="mobile_phone" tagType="input" verify="mobile"/>
```

再比如组织角色页中的部门联动：

```xml
<htmlElementDescriptor column="data_scope_dept_ids"
                       tagType="select"
                       dataSource="Department"
                       dataUrlParams="organId=#organId#"/>
```

用途：`dataSource` 决定数据来源，`multiple="true"` 支持多选，`dataUrlParams` 支持按当前表单字段动态传参。业务表作为引用选项时，需显式配置与目标 Controller 路由对应的 `dataUrl`；启用 `remoteApiParse="true"` 后，远端响应按选项字段映射转换，缺少自定义 key 映射时按当前注解默认 `name`/`id` 读取。表单保存值仍应是引用 ID，显示标签与提交字段不能混为一谈。复杂父子级联还要确认数据是否具有真实层级结构、生成接口返回的 flat/tree DTO 与 Framework 组件协议是否匹配；普通 `<select>` 配置本身不产生级联规则。

### 13.4 `columnRenderFun` 的列表展示渲染

```xml
<columnRenderFun fieldNames="name,username" renderFun="colDefsAsLink"/>
<columnRenderFun fieldNames="state" renderFun="colDefsState"/>
```

用途：

- `colDefsAsLink`：把文本列渲染成链接。
- `colDefsState`：把状态列渲染成启停样式。

### 13.5 `generateCachePo`

```xml
<generateCachePo generate="true" valueColumn="name_"/>
```

用途：启用缓存字典模型与按键名称查询链路，常用于把引用 ID 转为名称。该节点缺省关闭；`generate="true"` 会生成 `{Domain}CachePo`、对应 MapStruct 转换方法和 Service `selectByKeysDict` 缓存查询方法；同时启用 Controller 生成时，会增加 `/dict` 查询端点及缓存配置。当前 DTD 即使 `generate` 缺省为 `false`，仍要求显式填写 `valueColumn`。该列指向用作翻译文本的物理列，生成器把它映射到缓存 PO 的 `dictValueText`；应配置为业务展示名称列（常见为 `name_`），并确认列存在及生成映射正确。

`keyColumn` 缺省为 `id_`，`typeColumn` 可选；配置后查询键/类型语义随之变化，需同时核对调用方 `@Dict(beanName=...)`、接口参数与唯一性范围。`includeColumns` 不是简单的全部字段白名单：源码将主键、配置的 key/type/value 列始终纳入；指定非空 include 集合时再加所选基础列，省略或解析为空时回退为所有基础列。若希望 PO 最小化，应先确认其转换、缓存调用和后续业务映射需要哪些列，并查看生成的 CachePo/Mapper 映射。

此缓存字典查询与 `generateOptions` 的表单选项 GET 路由是两套用途不同的接口：前者通过 Service 的缓存查询按键取翻译值（Controller 查询路由为 `/dict`），后者查询记录并投影 `FormSelectOption`（如 `/option/name`）。不要因启用了 CachePo 就假定 `/option/name` 已被缓存；也不要因下拉接口可用，就假定列表 ViewVo 的 `categoryText` 已能完成 ID 到名称的后端转换。实际缓存还依赖生成的缓存注解配置与宿主 Spring Cache/缓存实现及失效链路，须检查运行时配置，不能仅以生成了 CachePo 宣称缓存已生效。

### 13.5.1 分类/业务字典作为引用选项与展示转换

此场景包含两条相互配合、但不能互相替代的链路：表单通过选项 API 将显示名称选为引用 ID；列表/详情通过后端字典转换将引用 ID 显示为名称。建议按以下顺序完成配置，再逐步核对生成物和宿主行为。示例以引用方字段 `category_id`、分类主键 `id_`、显示列 `name_` 为例；应替换为实际表名、列名、模块路由和 Service Bean 名。

**步骤 1：在被引用表启用缓存字典转换。** 在分类/字典表的 table 配置下启用 Cache PO：

```xml
<generateCachePo generate="true" valueColumn="name_"/>
```

`valueColumn` 是用于 ID→名称转换的物理列，生成器会将其映射为缓存 PO 的 `dictValueText`。`keyColumn` 缺省为 `id_`；只有键不是主键时才显式配置。若同一字典需按类型区分，再配置 `typeColumn`，并确保 `@Dict` 调用方提供匹配的类型值。当前 DTD 即使 `generate` 缺省关闭，也要求 `valueColumn`。生成后检查 Cache PO 字段、MapStruct 转换、`selectByKeysDict` 查询及缓存注解；缓存真正可用还依赖宿主 Spring Cache 配置和缓存失效链路。

**步骤 2：在同一被引用表生成选项 API。** 确认 Controller 已启用，并在其中增加 `generateOptions`：

```xml
<generateController generate="true">
    <generateOptions nameColumn="name_"/>
</generateController>
```

`nameColumn` 必须是存在且可映射到实体属性的物理列，`idColumn` 缺省为 `id_`。以 `name_` 为例，通常生成 `{controllerBasePath}/option/name` 的 GET 方法，返回平面 `FormSelectOption` 列表；该选项 API 查询记录并投影选项，**不会因为步骤 1 启用了 Cache PO 就自动使用字典缓存**。生成后核对真实 Controller 路由、请求参数、返回类型及权限 action；当前生成器在找不到 `nameColumn` 时会跳过该方法。

**步骤 3：在引用方页面配置选择控件。** 在引用方表的对应 `generateHtml` 下，把 `htmlElementDescriptor.column` 指向实际引用 ID 列，并使用步骤 2 核对过的 URL：

```xml
<htmlElementDescriptor column="category_id"
                       tagType="select"
                       dataSource="Dict"
                       beanName="sensitiveCategoryImpl"
                       remoteApiParse="true"
                       dataUrl="/sensitive/sensitive-category-impl/option/name"/>
```

`beanName` 用于 ID→名称的字典转换，必须对应步骤 1 所生成并注册的缓存字典 Service；`dataUrl` 则用于获取表单选项，必须对应步骤 2 的真实路由。`remoteApiParse="true"` 要求前端解析远端响应，默认选项字段映射为 `name`/`id`；响应契约不同时，按当前配置能力显式映射并验证。表单提交值仍应是 ID。该配置不实现服务端引用有效性、状态/租户过滤或级联校验。

**步骤 4：在引用方 ViewVo 配置中文展示。** 确保 `categoryText` 是最终 ViewVo 的实际属性，具有可靠的字典转换来源，再把它列入默认显示字段：

```xml
<viewVo generate="true"
        defaultDisplayFields="categoryText,name,created"/>
```

例如，ViewVo 可为 `categoryText` 配置 `@Dict(beanName="sensitiveCategoryImpl", source="categoryId")`，并以 `@ViewColumnMeta(value="categoryText", columnName="category_id")` 关联引用列。`defaultDisplayFields` 使用 ViewVo 属性名，仅决定已有字段的默认展示/顺序；它不创建属性、不增加 SQL JOIN，也不执行 ID 翻译。若项目使用 `overridePropertyValue`、MapStruct 或人工扩展实现转换，生成后都应确认最终 VO 字段、映射调用和接口响应。

**完成检查：** 先核对 ID、显示列及可选类型键的实际语义；再分别验证选项接口路由/权限/响应和字典缓存 Bean/key/value。最后验证编辑表单回显名称且提交 ID、列表/详情能将 ID 转成名称，并覆盖空引用、已删除或停用引用、无权限及缓存更新/失效等场景。若需求是级联选择，还须另行配置和验证父子关系、树选项接口、响应结构及 Framework 级联行为；平面 `select` 配置不会自动形成级联。

### 13.6 `generateHtml` + 附件/打印页

来自 `mpmm-serv` 的申请单配置：

```xml
<generateHtml generate="true" type="editable" viewPath="nddc_generic_apply" overWriteHtmlFile="true">
    <htmlFileAttachment elementKey="edit_file_attachment" generate="true" label="附件"
                        afterColumn="note_"/>
    <approvalComment elementKey="edit_approval_comment" generate="true" label="审批意见"
                     locationTag="审批意见"
                     afterColumn="note_"/>
</generateHtml>

<generateHtml generate="true" type="print" viewPath="print_nddc_generic_apply" overWriteHtmlFile="true"
              overWriteJsFile="false"
              overWriteCssFile="true">
    <property name="htmlHiddenFields"
              value="id,deleteFlag,created,fileCategory,wfState,modified,createdId,modifiedId,version,regDocNumber,numberRule,fldSerNumber,procTypeId,curProcessors,subject,moduleId,author,applyOrgId"/>
    <property name="requiredColumns" value="apply_contact"/>
    <layout pageColumnsNum="3"/>
    <htmlFileAttachment elementKey="print_file_attachment" generate="true" label="附件"
                        afterColumn="note_"/>
    <approvalComment elementKey="print_approval_comment" generate="true" label="审批意见"
                     locationTag="审批意见"
                     afterColumn="note_"/>
</generateHtml>
```

用途：同一张表可以定义多个页面；编辑页和打印页可以分别控制隐藏列、必填列、布局和附件组件。

### 13.7 `javaModelCollection`

来自示例项目的子集合配置：

```xml
<javaModelCollection fieldName="members"
                     modelType="String"
                     whereColumn="id_"/>
```

用途：给实体添加一对多集合属性，适合子表、成员列表、明细集合等场景。

## 14. 配置前准备与数据收集（建议先做）

在开始写 `generatorConfig.xml` 前，先收集下面信息，能显著减少反复修改。

### 14.1 Context 级准备清单

| 项                                     | 必填/建议    | 说明                                                      |
|---------------------------------------|----------|---------------------------------------------------------|
| `id`                                  | 必填       | 本次生成上下文标识。默认使用context001                                |
| `appKeyword`                          | 必填       | 应用关键字，影响包路径归属。如没有指定，则默认使用模块名称（全字符)                      |
| `moduleKeyword`                       | 必填       | 模块关键字，影响接口根路径和前端模块路径。如没有指定，则默认使用模块名称（全字符)               |
| `moduleName`                          | 必填       | 模块中文名，用于模块/菜单数据。如没有指定，则默认使用“业务”                         |
| `jdkVersion`                          | 必填       | `8/9/11/17/21`。 。如没有指定，则默认使用21                          |
| `parentMenuId`                        | 视需求      | 菜单父级。ViewVo 级 `parentMenuId` 优先，否则使用 Context 值；没有配置时无隐式默认父菜单。 |
| `updateModuleData` / `updateMenuData` | 视需求      | 是否产出模块/菜单数据 SQL；两项缺省均为 `false`，需按需显式开启。 |
| `onlyTables`                          | 建议       | 多人协作时建议限制本次生成表范围。 没有指定，则默认为空                            |
| `vueEndProjectPath`                   | 前后端联动时必配 | Vue 项目根路径（不含 `src`）。                                    |
| `additionalDbTypes`                   | 多库场景必配   | 目标数据库方言集合。 没有指定，则默认为空                                   |

### 14.2 Table 级准备清单

| 项     | 建议                                                                |
|-------|-------------------------------------------------------------------|
| 表角色   | 先确定是 `dataTable` 还是 `relationTable`。                              |
| 模块主表  | 需要生成默认视图菜单时配置 `isModules=true`，并确保 Context 或 ViewVo 有有效 `parentMenuId`；另须显式启用 Context `updateMenuData=true`。 |
| 别名    | `alias` 是否统一（影响 SQL/filter 拼接）。                                   |
| 上传下载  | 是否启用 `enableFileUpLoad`。 没有指定，则默认为false                           |
| 关系设计  | 是否需要双侧 `selectByTable`（多对多）、`selectByColumn` 或独立的一对多 `javaModelCollection`；确定关系表两侧键和维护方向。 |
| 校验忽略列 | `validateIgnoreColumns` 是否按项目规范统一。                                |
| 清理策略  | 是否需要 `cleanAllGeneratedElements=true`（重建场景）。                      |

### 14.3 列级准备清单

| 项 | 建议 |
|---|---|
| 字段展示策略 | 哪些字段走字典/枚举转换（`overridePropertyValue`）。 |
| 查询策略 | 哪些字段用于 query/filter/fuzzy/between。 |
| 页面控件策略 | 每个字段的 `tagType/dataSource/dataFormat` 是否明确。 |
| 大字段策略 | `bytes_`/BLOB/LONGTEXT 字段是否走上传下载、富文本、或仅详情展示。 |
| 默认值策略 | 列默认值是否可用于 `new-instance` 与 VO 初始化。 |

准备时同时填写采用的默认约定、业务适用性、显式配置/扩展位置、默认值赋值时机、字段业务顺序、容器列数与长文本区域；按[第 20 章](#20-约定驱动配置与实践建议)说明例外及检查场景，避免只收集技术属性而遗漏交互和业务语义。

## 15. 常量选项与决策参考（字典/枚举/控件）

### 15.1 DTD 内置可选值（优先参考）

| 类别 | 可选值 |
|---|---|
| `tagType` | `input/radio/checkbox/switch/date/time/datetime/select/title/cascader/checkboxGroup/slider/number/color/rate/tableselect/upload/editor/dropdownlist/signArea` |
| `dataSource` | `Dict/DictData/DictUser/DictSys/DictEnum/DictModule/Organ/Department/User/Role/InnerList/DateRange` |
| `dataFormat` | `启停/有无/是否/性别/缓急/级别/年/年月/年周/日期/日期时间/时间` |
| `annotationType`（`overridePropertyValue`） | `DictUser/DictSys/DictModule/Dict/DictData/DictEnum` |

### 15.2 “字典还是枚举”如何选

| 场景 | 推荐 | 关键配置 |
|---|---|---|
| 值集稳定、随代码版本发布 | `DictEnum` | `enumClassFullName`（必配） |
| 值集需运维在线维护 | `DictData` / `DictSys` | `dictCode` 或 `typeValue` |
| 特殊来源由业务 Bean 提供 | `Dict` | `beanName`（必配） |

### 15.3 `DictEnum` 可用枚举来源建议

1. 首选项目依赖中的业务枚举（通常在 `com.vgosoft.*.enums`）。
2. DTD 注释已给出典型示例：
   - `priority_` -> `com.vgosoft.core.constant.enums.core.UrgencyEnum`
   - `wf_state` -> `com.vgosoft.workflow.enums.WorkFlowStatusEnum`
3. 若不确定，优先先用 `DictData` 落地，再逐步收敛到 `DictEnum`。

## 16. `rootClass` 范围、父类关系与影响

### 16.1 关键行为（源码口径）

| 行为 | 说明 |
|---|---|
| 默认父类 | table 未配置 `rootClass` 时，默认 `com.vgosoft.core.entity.abs.AbstractEntity`。 |
| 作用域 | `rootClass` 可在 table property 配置；也可由 context 的 model properties 继承。 |
| 泛型支持 | 可通过 `rootClassTypeArguments` 追加父类泛型参数。 |
| 字段去重 | rootClass 已有字段不会在子类重复生成（`RootClassInfo.containsProperty`）。 |
| 语义分支 | 是否工作流/业务/BLOB 实体会根据 rootClass 继承关系触发不同生成分支。 |

### 16.2 常用父类（DTD 注释）

| 父类 | 典型附加字段语义 |
|---|---|
| `AbstractEntity` | `id_`、`delete_flag` |
| `AbstractPersistenceObjectEntity` | + `created_`、`modified_`、`created_id`、`modified_id` |
| `AbstractPersistenceLockEntity` | + `version_` |
| `AbstractBusinessEntity` | + `subject_`、`priority_`、`module_id`、`file_category` 等业务字段 |
| `AbstractWorkflowBusinessEntity` | + `business_key`、`wf_state`、`cur_processors` |
| `AbstractWorkflowSampleEntity` | + `apply_dept_id`、`apply_user_id`、`apply_org_id` 等流程申请字段 |
| `AbstractBlob*` 系列 | 包含 `bytes_` 等大字段语义 |

### 16.3 设计建议

1. 先选父类，再定表结构；不要先建字段后再“硬塞”父类。
2. `rootClass` 一旦确定，VO/页面默认字段、校验、查询能力都会受影响。
3. 涉及流程、附件、大字段时，优先用能表达该语义的父类，避免后续大量补丁配置。

## 17. 补充：与 `mybatis-generator-config_1_8_0.dtd` 的对照关系

本指南是“可执行解释”，DTD 是“结构定义源”。两者配套使用时，建议按“先 DTD 定位节点，再回本指南看行为与案例”的顺序。

### 17.1 DTD 文件位置

- `mybatis-generator-core/src/main/resources/org/mybatis/generator/config/xml/mybatis-generator-config_1_8_0.dtd`

该路径对应当前 `1.8.0-SNAPSHOT`，不是所有版本通用的固定文件；配置版本选择与升级门禁见第 1.1 节。

### 17.2 本文章节与 DTD 节点映射

| 本文章节 | 对应 DTD 节点/实体 |
|---|---|
| 1 根结构 | `generatorConfiguration`、`properties`、`classPathEntry` |
| 2 context | `context`、`jdbcConnection`、`connectionFactory`、`javaTypeResolver`、`javaModelGenerator`、`sqlMapGenerator`、`javaClientGenerator` |
| 3 table | `table`、`generatedKey`、`domainObjectRenamingRule`、`columnRenamingRule` |
| 4 列级配置 | `columnOverride`、`ignoreColumn`、`ignoreColumnsByRegex`、`except` |
| 5 Java 模型/VO | `generateModel`、`generateVo`、`modelVo/createVo/updateVo/viewVo/excelVo/requestVo`、`additionalProperty`、`generateCachePo` |
| 6 值转换/渲染 | `overridePropertyValue`、`columnRenderFun`、`htmlElementDescriptor` |
| 7 页面生成 | `generateHtml`、`property`、`layout`、`groupContainer`、`htmlElementDescriptor`、`htmlSwitchDescriptor`、`htmlTableSelectDescriptor`、`htmlFormExtendDescriptor`、`validator`、`hrefElement`、`htmlButton`、`htmlFileAttachment`、`approvalComment`、`htmlElementInnerList`；逐项解析/传递限制见第 7 章 |
| 8 查询/关联/统计 | `selectByColumn`、`selectByTable`、`selectBySqlMethod`、`javaModelAssociation`、`javaModelCollection`、`generateTreeViewCate`、`generateOptions`、`generateDateDimStatistics`、`generateCustomStatistics` |
| 9 其他辅助 | `htmlButton`、`queryColumn`、`filterColumn`、`nameFragment`、`mapstructMapping`、`reusableElements` |
| 10 常见歧义/纠错 | DTD 注释 + Parser/Generator 源码校准（如 `excludedTables`、`orderByClause` 等） |
| 15 常量选项 | DTD 实体：`%tagType;`、`%dataSource;`、`%dataFormat;`、`%validatorType;` |

### 17.3 使用规则（AI/人工统一）

1. 先按 DTD 确认“节点是否允许、属性名是否存在、属性值枚举是否合法”。
2. 再按本指南确认“实际生成行为、默认值、联动影响、已知歧义修正”。
3. DTD 注释与生成结果冲突时，以源码实现为准，并在配置中采用已校准字段名（例如 `excludedTables`、`orderByClause`、`enableSelectByPrimaryKeys`）。

## 18. 生成物命名规则

本节列出生成代码、页面及辅助文件中的主要默认命名。表中的“可配置”包含直接配置名称、配置派生名称，以及由目标包/输出路径配置影响的情况；“条件生成”表示仅在相应生成器启用或满足列、关联等条件时出现。自定义插件、模板或配置中直接填写的名称无法在此穷举。

| 类型 | 默认命名 | 是否可配置 | 命名依据说明 | 主要源码 |
|---|---|---|---|---|
| 实体类名 | 表名转 PascalCase，如 `sys_user` → `SysUser` | 是 | 优先使用 `domainObjectName`；未配置时从运行时表名或数据库表名转换。未配置实体名时可应用 `domainObjectRenamingRule`。 | `api\FullyQualifiedTable.java` |
| 主键模型类 | `<Entity>Key` | 间接可配置 | 实体名后追加 `Key`；是否生成取决于模型规则及主键列。 | `api\AbstractIntrospectedTable.java`、`codegen\mybatis3\model\PrimaryKeyGenerator.java` |
| 普通模型类 | `<Entity>` | 间接可配置 | 使用实体名作为基础记录类名；目标包受 Java model 配置及子包规则影响。 | `api\AbstractIntrospectedTable.java`、`codegen\mybatis3\model\BaseRecordGenerator.java` |
| BLOB 模型类 | `<Entity>WithBLOBs` | 间接可配置 | 实体名后追加 `WithBLOBs`；有 BLOB 列且相应生成规则开启时生成。 | `api\AbstractIntrospectedTable.java`、`codegen\mybatis3\model\RecordWithBLOBsGenerator.java` |
| Example 类 | `<Entity>Example` | 间接可配置 | 实体名后追加 `Example`；Example 目标包可单独配置。 | `api\AbstractIntrospectedTable.java`、`codegen\mybatis3\model\ExampleGenerator.java` |
| Java 字段及访问器 | 字段使用 `javaProperty`；访问器为 `get<Property>` / `set<Property>` | 是 | 属性名由列名转换及 `columnOverride.property` 等配置决定；布尔属性 getter 可能使用 `is<Property>`。 | `internal\db\DatabaseIntrospector.java`、`internal\util\JavaBeansUtil.java` |
| Mapper 接口 | `<Entity>Mapper` | 是 | 默认由实体名加 `Mapper`；`mapperName` 可覆盖，包名受 Java client target package 和子包规则影响。 | `api\AbstractIntrospectedTable.java` |
| SQL Provider 类 | `<Entity>SqlProvider` | 是 | 默认由实体名加 `SqlProvider`；可由 `sqlProviderName` 覆盖。 | `api\AbstractIntrospectedTable.java` |
| Dynamic SQL 支持类 / 表对象 | `<Entity>DynamicSqlSupport` / `<Entity>` | 是 | 默认分别由实体名加 `DynamicSqlSupport`、实体名构成；类名和表对象名均有配置覆盖入口。 | `api\AbstractIntrospectedTable.java` |
| Mapper XML 文件及命名空间 | `<Entity>Mapper.xml`；namespace 默认优先使用 Mapper 全限定名 | 是 | 默认 Mapper 文件名由 Mapper 名称确定；`mapperName` 可覆盖文件名及 Mapper 类型。包和 namespace 还受 target package、表子包及实体子包影响。 | `api\AbstractIntrospectedTable.java`、`codegen\mybatis3\IntrospectedTableMyBatis3Impl.java` |
| 标准 Mapper statement / resultMap ID | 如 `selectByPrimaryKey`、`insertSelective`、`BaseResultMap`、`Base_Column_List` | 名称固定；生成项有条件 | ID 由生成器设定；具体语句是否生成仍受模型类型、列类型和生成规则影响。 | `api\AbstractIntrospectedTable.java`、`codegen\mybatis3\xmlmapper\elements\*` |
| 扩展 Mapper statement ID | 如 `insertBatch`、`updateBatchByPrimaryKey`、`selectByKeysDict`、`selectByExampleWithChildrenCount` | 部分可配置 | 部分 ID 固定，部分由自定义 generator 配置提供；批量、关系、子表等功能决定是否生成。 | `api\AbstractIntrospectedTable.java`、`codegen\mybatis3\xmlmapper\elements\*` |
| Service 接口 | `IGen<Entity>`、`I<Entity>` | 间接可配置 | 分别生成在 gen 包和普通包；类名由实体名拼出，目标包可配置。 | `codegen\mybatis3\service\AbstractServiceGenerator.java`、`JavaServiceGenerator.java` |
| Service 实现类 | `Abstract<Entity>Impl`、`<Entity>Impl` | 间接可配置 | 抽象实现类由实体名拼成；具体实现类由 Service Bean 名首字母大写得到。生成包可配置。 | `codegen\mybatis3\service\JavaServiceImplGenerator.java` |
| Controller / Service Bean 名 | 表名转 lowerCamelCase + `Impl`，如 `sys_user` → `sysUserImpl` | 间接可配置 | 配置 `domainObjectName` 时取其首字母小写后加 `Impl`；否则表名转 lowerCamelCase 后加 `Impl`。 | `config\ConfigUtil.java`、`api\AbstractIntrospectedTable.java` |
| Controller 类 | `Abstract<Entity>Controller`、`<Entity>Controller` | 间接可配置 | 抽象类在 gen 包，具体类在普通包；类名由实体名拼接固定前后缀，包路径可配置。 | `codegen\mybatis3\controller\JavaControllerGenerator.java` |
| Controller 基础请求路径 | `<serviceApiBasePath>/<Bean名转中划线>`，如 `sysUserImpl` → `sys-user-impl` | 是 | 前缀默认取 `moduleKeyword`；缺省时回退到 HTML target package 或 `app`。`appKeyword` 用于包名构造，不是此路径前缀。 | `config\xml\MyBatisGeneratorConfigurationParser.java`、`internal\util\Mb3GenUtil.java`、`codegen\mybatis3\controller\JavaControllerGenerator.java` |
| Controller 固定端点 / 方法 | 如 `list`、`get`、`create`、`update`、`delete`、`upload`、`download`、`import`、`export`、`dict`、`tree` | 部分条件生成 | 端点名由对应生成器固定；回收站、Excel、视图、树等端点取决于配置和列/关联条件。 | `codegen\mybatis3\controller\JavaControllerGenerator.java`、`codegen\mybatis3\controller\elements\*` |
| Controller 配置型端点 | 配置方法名转中划线；URL 可带 `select/`、`split/`、`union/`、`option/` 前缀 | 是 | 列查询、删除、统计、表拆分/合并等端点会使用配置的方法名或后缀；URL 前缀及字段名由相应生成器决定。 | `codegen\mybatis3\controller\elements\SelectByColumnGenerator.java`、`DeleteByColumnGenerator.java`、`SelectByTableGenerator.java`、`OptionElementGenerator.java` |
| Service 扩展方法 | 如 `insertBatch`、`updateBatch`、`insertOrUpdate`、`selectByColumn`、`selectByTable`、`selectBySqlMethod` | 部分可配置 | 基础方法名固定；SQL 方法、列/表查询和统计方法可由配置生成，部分方法名直接来自配置。 | `codegen\mybatis3\service\ServiceMethods.java`、`codegen\mybatis3\service\JavaServiceGenerator.java` |
| VO / PO 类名 | `<Entity>Vo`、`<Entity>RequestVo`、`<Entity>CreateVo`、`<Entity>UpdateVo`、`<Entity>ViewVo`、`<Entity>ExcelVo`、`<Entity>ExcelImportVo`、`<Entity>CachePo` | 条件生成；后缀固定 | 由实体名加固定后缀生成；是否生成由各 VO 配置及生成规则决定。 | `codegen\mybatis3\vo\*Generator.java`、`codegen\mybatis3\controller\AbstractControllerElementGenerator.java` |
| VO 字段及转换属性 | 数据列字段使用 `javaProperty`；默认文本属性为 `...Text`，如 `userId` → `userText` | 是 | 属性以 `Id` 结尾时去掉 `Id` 再加 `Text`，否则直接加 `Text`。HTML 元素的 `otherFieldName` 可覆盖；部分元素类型沿用原属性名。 | `config\ConfigUtil.java`、`internal\util\VoGenService.java` |
| VO 附加 / 覆盖属性 | 附加属性使用配置的 `name`；覆盖属性使用配置的目标属性名 | 是 | `additionalProperty`、`overridePropertyValue` 等配置可自定义属性名和类型；否则转换属性按默认规则推导。 | `config\ConfigUtil.java`、`internal\util\VoGenService.java` |
| VO 映射方法 | `from<X>`、`to<Y>`、`from<X>To<Y>`；列表映射通常追加 `s` | 由类型关系推导 | 方法名根据源、目标类型与实体类型的关系确定；列表映射会改变参数/返回类型并可能追加 `s`。 | `codegen\mybatis3\vo\VoGeneratorUtil.java` |
| Vue 文件名 | `<Entity>.vue`、`<Entity>Edit.vue`、`<Entity>Detail.vue`、`<Entity>Modal.vue`、`T<Entity>.ts` | 条件生成；输出路径可配置 | 文件名以实体名为基础；按列表、编辑、详情、表单等生成功能决定生成哪些文件。 | `plugins\vue3\Vue3OtherFilesPlugin.java`、`Vue3FormFilesPlugin.java` |
| Vue 固定文件名 | `PrivateTableColumnSlots.vue`、`sharedVariables.ts`、`PrivateUseFormHooks.ts` | 条件生成 | 文件名固定；是否生成取决于 Vue 页面、编辑功能和 VO 配置。 | `plugins\vue3\Vue3OtherFilesPlugin.java` |
| Vue 模块目录与 key | 模块目录名为实体名小写；REST path 使用 Controller 基础路径 | 间接可配置 | 目录和路径由实体名、Bean 名及模块配置派生；permission key 则由小写 Bean 名计算哈希值。 | `plugins\vue3\Vue3OtherFilesPlugin.java`、`Vue3FormFilesPlugin.java`、`internal\util\Mb3GenUtil.java` |
| Layui 内嵌列表文件名 | `<sourceViewPath>[_<listKey>]_inner_list_fragments.html`；缺省为 `<tableName>_inner_list_fragments.html` | 是 | `sourceViewPath`、`listKey` 可改变文件名；引用的片段名和模块名由配置或 context 提供。 | `internal\util\Mb3GenUtil.java`、`codegen\mybatis3\htmlmapper\document\LayuiDocumentGenerated.java` |
| Layui JS / CSS 文件名 | `<viewPath最后一段>.js`、`.min.js`、`.css`、`.min.css` | 间接可配置 | 基名取 `viewPath` 最后一段；是否生成取决于视图类型、生成开关和布局设置。 | `plugins\html\JQueryPlugin.java`、`StyleFileGeneratePlugin.java` |
| Layui HTML / JS 标识符 | 固定片段 ID 如 `TPL-inner-list-row-toolbar`；列模板及输入 ID 使用 `fieldName` | 部分可配置 | 固定模板 ID 写在模板中；列相关 ID 由字段名拼接得到。 | `resources\templates\layui\app_html_fragments.html.ftl`、`app_main_wf_js.js.ftl` |
| 数据库 schema SQL 文件 | `<tableName小写>.sql`，可加 `filePrefix` | 是 | 表名转小写作为基础文件名；可配置前缀，并可为额外数据库类型生成方言文件。 | `codegen\mybatis3\IntrospectedTableMyBatis3Impl.java` |
| 权限 SQL 文件 | `data-permission-<tableName小写>.sql`、`data-permission-action-<tableName小写>.sql` | 表名派生；条件生成 | 有相应权限数据时生成，固定前缀后追加小写表名。 | `codegen\mybatis3\IntrospectedTableMyBatis3Impl.java` |

常见命名依赖链为：**表名 → `domainObjectName` → 实体派生类名、Bean 名、请求路径及前端文件名**；列相关名称以数据库列名转换得到的 **`javaProperty`** 为基础，并可受列覆盖、VO 属性配置或 HTML 元素配置影响。

## 19. Spring 首次初始化

本节统一指导将生成器及项目提供的数据库初始化资源部署到目标数据库。范围包括数据库结构（DDL）、存储过程 / 函数等数据库程序对象、权限及权限动作数据、菜单 / 模块 / 流程类型等期初数据，以及项目自有的其他 SQL。生成器输出 SQL 文件不等于 Spring 已执行或数据库已写入；`src/main/resources/sql` 下的文件也不会仅因位于该目录而自动执行。

### 19.1 初始化资源盘点

生成完成后，先对**本次完整生成范围**和项目既有初始化目录做资源清单，记录文件来源、数据库方言、目标 schema、前置依赖、是否可重复执行及负责人。至少核对以下类型：

| 资源 | 当前生成器中的常见位置 / 命名 | 检查要点 |
|---|---|---|
| 表结构 DDL | 默认位于 `src/main/resources/sql/<方言>/schema_<tableName小写>.sql`；可受 `filePrefix`、目标路径及方言配置影响 | 只执行与目标数据库匹配的方言脚本；核对表、索引、约束和已有 schema 的迁移策略。 |
| 权限与权限动作期初数据 | `src/main/resources/sql/init/data-permission-<tableName小写>.sql`、`data-permission-action-<tableName小写>.sql` | 仅在生成器收集到相应数据时输出；确认主体权限、动作及其关联 ID 与现有菜单/资源一致。 |
| 菜单数据 | `src/main/resources/sql/init/data-menu-<moduleKeyword小写>.sql` | 受 `updateMenuData` 及 ViewVo 等条件控制；父菜单、权限数据及本次脚本覆盖范围见第 5.3.5 节。 |
| 模块数据 | `src/main/resources/sql/init/data-module-<moduleKeyword小写>.sql` | 受 `updateModuleData` 及模块数据收集条件控制；检查其他期初记录对模块 ID 的引用。 |
| 流程类型等业务数据 | 例如 `src/main/resources/sql/init/data-wf-proc-type-<moduleKeyword小写>.sql` | 按流程定义及业务数据依赖顺序执行。 |
| 存储过程、函数、触发器及自定义 SQL | 由项目、数据库迁移工具或自定义生成插件提供；不应假定上述默认产物已经包含这些程序对象 | 核实方言、分隔符、创建/替换语法、权限、依赖表以及执行器对脚本分隔符的支持。 |

上述路径和文件名是当前实现中的默认模式，不替代生成后检查；其他插件、配置和项目脚本可能增加或改变资源。逐个确认实际生成目录、构建产物中的 classpath 路径以及目标数据库方言，不要仅依赖文件名推断。

### 19.2 建立执行顺序与重复执行策略

执行顺序必须从真实 SQL 的外键、引用和程序对象依赖得出，不存在对所有业务项目都适用的固定序列。通常先准备数据库/schema，再创建表和基础约束；需要依赖表的存储过程 / 函数随后部署；再写入被其他记录引用的基础数据（例如顶级菜单、模块、基础角色/权限）；最后写入依赖这些记录的菜单项、权限动作及其他业务期初数据。若项目的 DDL 或数据库平台要求其他顺序，以目标数据库和既有迁移规范为准。明确记录依赖图，确保外键目标、父菜单、模块 ID、权限资源 ID 和存储过程依赖在引用者之前可用。

对每个脚本确认首次执行和重跑行为。生成的 INSERT 不应默认视为幂等；若脚本会重复插入、覆盖业务变更或与已有 ID 冲突，应由迁移版本记录、一次性门禁或经过审核的方言级幂等策略控制，不要直接重启反复执行。DDL 的“已存在”处理、事务支持、存储过程替换语义和失败后的部分提交均依数据库方言而异，必须在隔离数据库验证。

### 19.3 接入 Spring / 数据库迁移机制

先检查宿主实际使用的 Spring / Spring Boot 版本、DataSource、初始化配置、资源打包、迁移工具（如 Flyway / Liquibase）和既有启动钩子。**优先使用项目已有的版本化数据库迁移流程**：把审核后的 DDL、程序对象和数据脚本放入受控迁移，并显式保证顺序和版本记录。不要为了单个菜单脚本另外建立与项目现有流程并行的 Spring 初始化入口。

如果项目没有迁移工具，且使用的 Spring Boot 版本支持 `spring.sql.init.*`，可在确认兼容后通过 `schema-locations` 和 `data-locations` 显式配置简单 schema/data 脚本。例如，以下仅示意配置形状，必须替换为项目实际存在、已审核且顺序正确的 classpath 资源：

```yaml
spring:
  sql:
    init:
      mode: always
      schema-locations: classpath:sql/<方言>/<schema-script>.sql
      data-locations: classpath:sql/init/<ordered-data-script>.sql
      continue-on-error: false
```

该配置不是一次性门禁：`mode: always` 会在每次应用启动时尝试执行所配置的脚本。只用于受控的一次性初始化时，成功后必须关闭/移除该配置；长期部署应使用迁移版本记录，避免普通重启再次执行。属性中声明的脚本需确认实际版本支持、资源能被 classloader 读取，并且 Spring SQL initializer 对脚本格式和分隔符的处理适用。存储过程等含内部分号或数据库专用 delimiter 的脚本，不应假定简单 SQL 初始化器可正确解析；优先使用迁移工具或经项目验证、能指定 separator 的执行器。`continue-on-error` 保持 `false`，确保失败显式暴露，不得把部分初始化当成成功。

非 Spring Boot 项目或已有自定义初始化器，应使用项目现有的 Spring JDBC / `DataSource` 执行机制，明确配置资源、数据库连接、一次性标记、事务边界、失败回滚或补偿策略。不要仅凭“由 Spring 启动”推断其会自动扫描并执行任意 SQL 资源。

### 19.4 首次初始化操作与验收

1. **盘点与确认：** 选定目标环境、数据库/schema 和方言；收齐本次全部生成文件及项目自有脚本，核对资源打包路径、迁移状态和所有前置依赖。
2. **排序与审核：** 按依赖整理执行序列；检查 DDL、存储过程、权限、菜单及其他数据脚本的实际 SQL、ID、引用、冲突和重跑策略。菜单条件及父菜单验证见第 5.3.5 节。
3. **隔离环境演练：** 使用与目标环境匹配的数据库版本和权限，在空库或可恢复的隔离库执行完整序列；确认脚本格式、方言、事务/提交行为和失败处理。
4. **接入正式执行入口：** 使用项目统一迁移工具或经确认的 Spring 初始化机制；确保数据源指向目标数据库、执行顺序正确、错误不会被忽略，且资源实际打包进运行时 classpath。
5. **首次执行并核验：** 检查迁移/启动日志及退出结果；核验表结构、程序对象、权限关联、菜单父子关系和业务期初数据；任一步骤失败时停止后续初始化，按数据库实际提交状态修复或恢复后再重试。
6. **关闭一次性开关：** 成功后关闭一次性 `mode: always` 配置或确认迁移版本已被记录；使用同一应用配置做普通重启，验证不会重新执行非幂等脚本。

**AI 自主执行要求：** 先确认当前生成器版本、DTD 与宿主启动/迁移配置；盘点并读取脚本，而不是仅看目录名称；区分生成器产物与项目自有存储过程等资源；建立并验证依赖顺序、方言、运行时 classpath 和重复执行策略；只有实际完成 Spring/迁移执行并核对数据库结果后，才可报告“已初始化”。只生成或接入脚本时，应准确报告“脚本已生成/配置，尚未确认执行入库”。

**开发人员验收要求：** 审核资源清单及差异，确认目标库、备份/恢复方案、依赖次序和迁移版本；在匹配的隔离环境演练后执行正式初始化；检查实际数据库对象与记录、日志错误和重启行为，并归档已执行脚本版本及结果。生成器版本、目标数据库、Spring 初始化实现和迁移机制未核实时，不得把示例 YAML 直接作为生产配置。

## 20. 约定驱动配置与实践建议

### 20.1 使用方法与性质

本章是实践决策和阅读索引，具体配置行为仍在对应主题章节唯一维护。目的不是让数据库和业务迁就生成器，而是在业务语义正确的前提下利用约定，减少重复配置、重复代码和跨人员理解成本。

配置前按以下顺序判断：**业务目标与字段语义 → 默认约定及其前提 → 是否适用 → 显式配置或扩展归属 → 数据库/后端/页面联动 → 生成结果与宿主检查**。已有行为须有版本及源码依据；推荐实践允许按场景取舍；强制要求按相应规范执行。配置合法、注解已输出和页面实际生效是不同证据。

### 20.2 实践主题与决策入口

| 场景 | 推荐操作与必须确认的事项 | 规则归属/操作入口 |
|---|---|---|
| 分类/业务字典引用 | 将选择 API、缓存字典转换、引用方控件和 ViewVo 展示字段分开配置；核对 beanName、key/value、完整路由、响应映射、权限、缓存失效和父子级联，不把 `defaultDisplayFields` 当转换器 | 本文 §3 `generateOptions`、§5.2 `defaultDisplayFields`、§7.3、§13.5、§13.5.1 |
| 多行文本或大容量说明 | 先定容量及纯文本/富文本，匹配默认控件；短字段多行不靠虚增长度实现，核对配置或扩展接点 | 本文 §7.3.1“文本输入实践建议”；数据库规范 §4.5 |
| `apply_date` 等语义字段 | 核对标准名称、父类、实际类型、默认值来源及切面条件；明确新增缺省、显式值、编辑保留、草稿与提交赋值差异 | [数据库规范 §4.6](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-core-db\src\main\resources\docs\database-authoritative-spec.md#46-apply_date语义默认值与页面的联动实例)；本文 §7.3.1 日期控件/格式及 §7.8 源码入口 |
| 页面顺序、分组与多列 | 推荐评估无边框无标题外层容器，用 `includeElements` 表达业务顺序；长文本/富文本/显式整行单独评估，注意宿主容器列数优先级 | 本文 §7.2.3；不要靠数据库列迁移调整页面 |
| 状态、字典和关联字段 | 按值集维护方式选择枚举/字典/引用选择，统一保存值与展示值；检查数据源接口、回填属性、权限及联动清空 | 本文第 6 章、§7.3、§15.2；来源稳定不等于所有宿主都已提供接口 |
| 跨表复用 | 真正同语义、同来源、同交互的配置优先进入 `reusableElements`，表内保留差异；不可只因字段同名就合并 | 本文 §13.1；复用前检查列绑定、覆盖和最终 VO/页面元数据 |
| 新增、编辑、详情与系统字段 | 分开确定可见/可写/必填/默认值；用户可编辑字段与审计、租户等可信系统字段分开；前端隐藏/禁用不替代后端约束 | 本文 §7.1.2、§7.4；项目设计记录维护方及服务端校验 |
| 列表展示与查询 | 列表、查询、过滤、表单和详情分别选字段，长内容按需摘要/详情展示，避免整表字段平铺 | 本文 §5.2、第 6 章；字段进入 VO 不等于有查询实现 |
| 标题、说明与提示 | 复用数据库短标题，说明和页面提示另行设计；不把技术长度或内部标识堆进标签 | [数据库规范 §2.5](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-core-db\src\main\resources\docs\database-authoritative-spec.md#25-字段中文标题与说明强制)；本文 `columnSubComment`、`tips` 等配置 |
| 配置不能表达的业务行为 | 先评审已有生成能力、基础组件及合法扩展点；人工代码进入受保护扩展区，并保持接口/实现同步 | [全局过程 §1.1](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\ai-assisted-development-standard.md#11-持久化实现与生成扩展评审门禁)；本仓指令 §2.1 |

### 20.3 协作记录与持续维护

生成配置向导记录本项目采用的约定、配置节点、输出和保护范围；设计说明书记录业务取舍、默认值时机、布局及扩展职责；已有任务书记录许可、实际结果和未覆盖项。同一事实只维护一处，其他记录回链，不能只在聊天中约定。

AI 与开发人员在配置评审时应主动指出适用的实践建议和重要连带影响，例如“字段加长将改变输入控件和占宽”“增加容器后须重新确认列数与长文本整行”，而非只报告 XML 可解析。检查至少覆盖新增与编辑、历史值保留、边界长度、条件隐藏、保存后重开及实际宿主布局。

沟通交流或阅读代码时发现新的实践指导，按[全局过程 §1.5](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\ai-assisted-development-standard.md#15-实践指导的持续发现确认与沉淀)先说明候选内容、依据、价值和归属，获得许可后补充；当前清单不是封闭目录，也不是随时自动修改文档、代码或生成产物的授权。
