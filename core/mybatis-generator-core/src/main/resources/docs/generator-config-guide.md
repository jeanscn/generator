# generatorConfig.xml 配置指南

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
| `integrateMybatisPlus` | 是否集成 MP | 默认 `true`。 |
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

### `additionalDbTypes` 说明

- 这是 **表级/上下文级** 方言扩展集合。
- 合法值以 `DatabaseType.code` 为准，DTD 中的枚举只是兼容提示。
- 生成 SQL 时不能只写单方言分支，必须遍历 `additionalDbTypes`。

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

### 表级歧义修正

| DTD/注释 | 源码真实含义 |
|---|---|
| `sortColumn` | 实际属性是 `orderByClause`。 |
| `enableSelectByIds` | 实际属性是 `enableSelectByPrimaryKeys`。 |
| `excludedTable` | 实际属性是 `excludedTables`。 |

## 4. 列级配置

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

| 节点 | 含义 | 备注 |
|---|---|---|
| `generateModel` | 实体类生成 | `enableChildren` 依赖 `parent_id`。 |
| `generateVo` | VO 总控 | 控制 `modelVo/createVo/updateVo/viewVo/excelVo/requestVo`。 |
| `modelVo` | 查询/展示 VO | 默认排除抽象父类字段。 |
| `createVo` | 新增参数 VO | 可指定必填列、选择性创建。 |
| `updateVo` | 更新参数 VO | 可指定必填列、选择性编辑。 |
| `requestVo` | 查询参数 VO | 支持分页参数和查询片段。 |
| `viewVo` | 列表/详情 VO | 控制列表列、查询列、默认排序。 |
| `excelVo` | 导入导出 VO | 支持 include/exclude/ignore/import 配置。 |
| `generateCachePo` | 缓存 PO | 常用于字典值映射。 |
| `overridePropertyValue` | 值转换字段 | 生成 `Text`/展示字段或注解映射。 |
| `additionalProperty` | 附加属性 | 给 VO 增加新字段。 |

### 5.1 个性化导出接口与接入

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

### `generateHtml`

| 属性 | 含义 |
|---|---|
| `generate` | 是否生成页面。 |
| `type` | `editable` / `viewOnly` / `print`。 |
| `viewPath` | 页面路径。 |
| `title` | 页面标题。 |
| `overWriteHtmlFile` / `overWriteJsFile` / `overWriteCssFile` | 覆盖策略。 |
| `targetPackage` / `targetProject` | 页面包路径与项目路径。 |
| `default` | 默认页面。 |

### `layout`

| 属性 | 含义 |
|---|---|
| `loadingFrameType` | `pop` / `inner` / `full`。 |
| `pageColumnsNum` | 页面列数。 |
| `barPosition` | 工具条位置。 |
| `uiFrameType` | `layui` / `zui` / `elPlus` / `antd`。 |
| `exclusiveColumns` | 独占整行字段。 |
| `labelWidth` | 标签宽度。 |
| `labelPosition` | 左/右/上。 |

### `groupContainer`

| 属性 | 含义 |
|---|---|
| `elementKey` | 容器唯一标识。 |
| `type` | `border` / `tab` / `card` / `collapse`。 |
| `includeElements` | 包含哪些元素。 |
| `hideExpression` | 显隐表达式。 |

### `htmlElementDescriptor`

| 属性 | 含义 |
|---|---|
| `column` | 字段名。 |
| `tagType` | 控件类型。 |
| `dataSource` | 字典/枚举/用户/部门等数据源。 |
| `dataFormat` | 快捷格式，如 `是否`、`启停`、`日期`。 |
| `otherFieldName` | 回填中文值字段。 |
| `beanName` | 数据源 bean。 |
| `applyProperty` / `applyPropertyKey` | label/value 映射。 |
| `dictCode` | 字典编码。 |
| `callback` | 选择后回调。 |
| `switchText` | 开关文案。 |
| `verify` | 校验规则。 |
| `renderHref` | 只读时转链接。 |

### 源码补充行为

- `tagType` 缺失或 `column` 缺失时，元素通常不会被纳入生成。
- `dataFormat` 可被解析成默认枚举/字典配置。
- `DictSys` / `DictData` / `DictUser` 在未显式给 `dataUrl` 时，可由源码自动补齐数据源路径。

### `htmlSwitchDescriptor`

| 属性 | 含义 |
|---|---|
| `column` | 字段名。 |
| `dataSource` | 一般是 `DictEnum` / `DictSys` 等。 |
| `dictCode` | 字典编码。 |
| `switchText` | 开关显示文案。 |

### `htmlTableSelectDescriptor`

| 属性 | 含义 |
|---|---|
| `column` | 字段名。 |
| `dataUrl` | 选择数据接口。 |
| `beanName` | 数据源 bean。 |
| `listKey` / `listViewClass` | 内嵌列表来源。 |
| `multiple` | 是否多选。 |
| `defaultFilterExpr` | 默认过滤 SQL。 |

### `htmlFileAttachment`

| 属性 | 含义 |
|---|---|
| `generate` | 是否生成附件控件。 |
| `elementKey` | 组件唯一标识。 |
| `multiple` | 是否多附件。 |
| `type` | `text` / `picture` / `picture-card`。 |
| `location` | `table` / `local` / `oss`。 |
| `restBasePath` | 存储/访问基础路径。 |

### `htmlElementInnerList`

| 属性 | 含义 |
|---|---|
| `elementKey` | 组件唯一标识。 |
| `sourceBeanName` | 来源表 bean。 |
| `relationField` | 关联字段。 |
| `dataField` / `dataUrl` | 数据来源二选一。 |
| `editMode` | `row` / `cell` / `none`。 |
| `vxeListButtons` | 内嵌列表按钮。 |
| `defaultFilterExpr` | 默认过滤 SQL。 |
| `defaultSort` | 默认排序。 |
| `totalRow` / `totalFields` | 合计行配置。 |

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

用途：`dataSource` 决定数据来源，`multiple="true"` 支持多选，`dataUrlParams` 支持按当前表单字段动态传参。

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
<generateCachePo valueColumn="name_" includeColumns="name_,nick_name,username_" generate="true"/>
```

用途：生成缓存 PO，常用于字典值映射或名称查找。`valueColumn` 是翻译结果字段，`includeColumns` 决定缓存模型包含哪些列。

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
| `parentMenuId`                        | 视需求      | 菜单父级。模块在菜单中显示时需要，如没有指定，则默认使用业务顶级菜单“1628062089249689600” |
| `updateModuleData` / `updateMenuData` | 视需求      | 是否产出模块/菜单数据 SQL。  没有指定，则默认使用“true”                      |
| `onlyTables`                          | 建议       | 多人协作时建议限制本次生成表范围。 没有指定，则默认为空                            |
| `vueEndProjectPath`                   | 前后端联动时必配 | Vue 项目根路径（不含 `src`）。                                    |
| `additionalDbTypes`                   | 多库场景必配   | 目标数据库方言集合。 没有指定，则默认为空                                   |

### 14.2 Table 级准备清单

| 项     | 建议                                                                |
|-------|-------------------------------------------------------------------|
| 表角色   | 先确定是 `dataTable` 还是 `relationTable`。                              |
| 模块主表  | 是否 `isModules=true`。 需要显示在菜单中则为true,  parentMenuId不为空             |
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
| 7 页面生成 | `generateHtml`、`layout`、`groupContainer`、`htmlElementDescriptor`、`htmlSwitchDescriptor`、`htmlTableSelectDescriptor`、`htmlFileAttachment`、`htmlElementInnerList` |
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
