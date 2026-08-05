# 生成器权威文档

本文档以源码和 `mybatis-generator-config_1_7_0.dtd` 为准，描述当前生成器的真实能力边界、输出规则、扩展点与耦合点。

## 1. 生成链路

### 1.1 输入

输入来自三层：

1. **配置文件**：`generatorConfiguration -> context -> table`，DTD 已定义根结构和属性（`mybatis-generator-config_1_7_0.dtd:L18-L18`, `L64-L81`, `L191-L248`）。
2. **数据库元数据**：`Context.introspectTables()` 通过 JDBC 连接触发 `DatabaseIntrospector`，获取表、列、主键、索引、外键等信息（`Context.java:L365-L405`, `DatabaseIntrospector.java:L73-L125`）。
3. **补充属性**：`additionalDbTypes`、`rootClass`、`htmlTargetProject`、`htmlTargetPackage`、`forceUpdateScalableElement` 等配置在 parser 中落入 `Context/TableConfiguration`（`MyBatisGeneratorConfigurationParser.java:L613-L626`, `L627-L699`, `L1059-L1106`）。

### 1.2 规则计算

规则计算分三步：

1. **解析与归一化**：XML parser 先把 context/table 属性写入配置对象，再把 `additionalDbTypes` 合并为 `TableConfiguration.additionalDbTypes`；合法值通过 `DatabaseType.getType()` 解析（`MyBatisGeneratorConfigurationParser.java:L613-L626`）。
2. **内省与过滤**：`DatabaseIntrospector` 读取列、主键、索引、外键，随后执行忽略列、重命名、列覆盖、类型处理、主键计算（`DatabaseIntrospector.java:L77-L95`）。
3. **生成规则**：`BaseRules` 根据表级开关、字段存在性、默认字段、是否 modelOnly、是否启用 security/mybatis-plus 等计算是否产出某个工件（`BaseRules.java:L119-L144`, `L201-L259`, `L317-L369`, `L444-L557`, `L606-L699`）。

### 1.3 模板渲染

当前核心不是外部模板文件驱动，而是“生成器类 + DOM/字符串渲染”：

1. **Java**：各 `AbstractJavaGenerator` 子类生成 `CompilationUnit`，再由 `JavaFormatter` 格式化（`AbstractJavaGenerator.java:L24-L32`, `GeneratedJavaFile.java:L25-L49`）。
2. **XML**：各 `AbstractXmlGenerator` 子类生成 `Document`/`XmlElement`，再由 `XmlFormatter` 格式化（`AbstractXmlGenerator.java:L5-L7`, `GeneratedXmlFile.java:L20-L57`）。
3. **HTML**：`HTMLGenerator` 生成 HTML `Document`，`GeneratedHtmlFile` 通过 `HtmlFormatter` 输出（`HTMLGenerator.java:L41-L50`, `GeneratedHtmlFile.java:L20-L49`）。
4. **SQL 脚本**：`GeneratedSqlSchemaFile` 直接由脚本生成器拼接字符串（`GeneratedSqlSchemaFile.java:L13-L29`, `SqlSchemaScriptGenerator.java:L44-L175`）。

### 1.4 输出

`Context.generateFiles()` 汇总所有生成器结果，再由 `MyBatisGenerator` 统一写盘（`Context.java:L423-L478`, `MyBatisGenerator.java:L270-L314`）。

---

## 2. 后端与前端输出清单

### 2.1 后端输出

| 类别 | 产物 | 生成入口 |
|---|---|---|
| 实体/Example | `PrimaryKey`, `BaseRecord`, `RecordWithBLOBs`, `Example` | `IntrospectedTableMyBatis3Impl.calculateJavaModelGenerators()`（`IntrospectedTableMyBatis3Impl.java:L190-L214`） |
| DAO / Mapper 接口 | `JavaMapperGenerator` / `SimpleJavaClientGenerator` / `AnnotatedClientGenerator` 产物 | `calculateClientGenerators()`（`IntrospectedTableMyBatis3Impl.java:L137-L152`, `L167-L187`） |
| XML Mapper | `<table>Mapper.xml` | `getGeneratedXmlFiles()`（`IntrospectedTableMyBatis3Impl.java:L355-L371`） |
| Service / ServiceImpl | `I<Entity>`、`<Entity>ServiceImpl` | `generateServiceAbout()`（`IntrospectedTableMyBatis3Impl.java:L253-L280`） |
| Controller | `Abstract<Entity>Controller` / 具体控制器 | `generateControllerAbout()`（`IntrospectedTableMyBatis3Impl.java:L229-L250`） |
| VO / Cache PO | `Vo`, `CreateVo`, `UpdateVo`, `RequestVo`, `ViewVo`, `ExcelVo`, `CachePo` | `calculateJavaModelGenerators()` 与规则门控（`BaseRules.java:L635-L699`） |
| 单元测试 | `src/test/java/...` 下控制器/服务测试类 | `generateControllerAbout()` / `generateServiceAbout()`（`IntrospectedTableMyBatis3Impl.java:L239-L248`, `L275-L279`） |
| SQL Schema / 初始化脚本 | `*.sql` | `getGeneratedSqlSchemaFiles()` / `getGeneratedPermissionSqlDataFiles()` / `getGeneratedPermissionActionSqlDataFiles()`（`IntrospectedTableMyBatis3Impl.java:L381-L442`） |

### 2.2 前端输出

| 类别 | 产物 | 说明 |
|---|---|---|
| Thymeleaf HTML | `*.html` | 当前 core 的前端主产物；由 `HTMLGenerator` + `GeneratedHtmlFile` 生成（`HTMLGenerator.java:L41-L50`, `GenerateHtmlFiles.java:L26-L45`） |
| 前端附加文件 | Web/Vue 其他文件 | 由插件钩子追加，不是 core 固定产物（`Plugin.java:L195-L231`, `Context.java:L465-L478`） |

**说明**：当前 core 只明确写出 HTML 文档；`overWriteJsFile/overWriteCssFile` 已在配置层存在，但在已审源码链路中未看到独立 JS/CSS 产物写盘逻辑。

---

## 3. 命名规则与路径规则

### 3.1 Java

1. 文件名 = `CompilationUnit` 的短类名 + `.java`（`GeneratedJavaFile.java:L47-L49`）。
2. 包路径 = `CompilationUnit` 的 packageName（`GeneratedJavaFile.java:L51-L54`）。
3. 写盘目录 = `targetProject + packagePath`，由 `ShellCallback.getDirectory()` 决定（`MyBatisGenerator.java:L317-L340`）。

### 3.2 XML

1. 文件名 = `getMyBatis3XmlMapperFileName()`。
2. 包路径 = `getMyBatis3XmlMapperPackage()`。
3. 输出目录 = `sqlMapGeneratorConfiguration.targetProject + targetPackage`（`IntrospectedTableMyBatis3Impl.java:L361-L366`）。

### 3.3 HTML

1. `viewPath` 默认取表名小写；若配置 `htmlTargetPackage`，则前缀为该包（`MyBatisGeneratorConfigurationParser.java:L1063-L1082`）。
2. `htmlFileName` = `viewPath` 最后一段 + `.html`（`MyBatisGeneratorConfigurationParser.java:L1080-L1082`）。
3. 输出目录 = `html targetProject + targetPackage`（`GenerateHtmlFiles.java:L30-L38`, `GeneratedHtmlFile.java:L20-L26`）。
4. 控制器会把 `htmlFileName` 写入 `ModelAndView`，前后端契约必须保持一致（`ViewElementGenerator.java:L92-L121`）。

### 3.4 SQL 脚本

1. schema 文件名 = `tableName.toLowerCase() + ".sql"`，可加 `filePrefix`（`IntrospectedTableMyBatis3Impl.java:L388-L405`）。
2. dialect 子目录 = `databaseType.getCode().toLowerCase()`（`IntrospectedTableMyBatis3Impl.java:L398-L404`）。
3. 初始化/权限脚本固定落到 `src/main/resources/sql/init`（`IntrospectedTableMyBatis3Impl.java:L416-L440`）。

---

## 4. 覆盖策略与跳过策略

### 4.1 覆盖策略

1. **Java**：存在同名文件时，优先 merge；不支持 merge 时，若 shell 允许 overwrite 则覆盖，否则生成唯一后缀文件（`MyBatisGenerator.java:L317-L344`）。
2. **XML**：`GeneratedXmlFile.isMergeable()` 为 true 时走 XML merge；否则同 Java 逻辑回退（`GeneratedXmlFile.java:L46-L58`, `MyBatisGenerator.java:L389-L423`）。
3. **HTML**：`GeneratedHtmlFile` 默认不 merge；若 `overwriteFile=false` 且目标已存在，则直接跳过并给出 warning 110（`GenerateHtmlFiles.java:L38-L40`, `MyBatisGenerator.java:L425-L456`）。
4. **SQL 脚本/其他 GeneratedFile**：默认 `isOverWriteFile=true`，存在时会按 `writeGeneratedFile()` 逻辑处理；若被显式设为 false，则可阻止覆盖（`GeneratedFile.java:L21-L25`, `MyBatisGenerator.java:L346-L377`）。

### 4.2 跳过策略

1. `cleanAllGeneratedElements=true` 的表会在生成前被从待生成列表移除（`MyBatisGenerator.java:L239-L245`）。
2. `onlyTables` 仅允许名单内的表进入生成（`MyBatisGeneratorConfigurationParser.java:L179-L182`, `Context.java:L380-L387`）。
3. `table.ignore=true` 的表会在内省阶段保留表信息，但不会进入后续数据校验/写库步骤（`Context.java:L397-L405`）。
4. 某些工件依赖条件不满足时不会生成：
   - 无主键 / 无基础列 / 无 BLOB 时，对应 SQL/ResultMap 直接不出（`BaseRules.java:L201-L350`, `L390-L441`）。
   - `parent_id` 不存在时，子节点计数/树形 SQL 不生成（`XMLMapperGenerator.java:L107-L113`, `L177-L188`）。
   - `html.isGenerate=false` 时，不生成 HTML（`GenerateHtmlFiles.java:L28-L42`）。
   - `additionalDbTypes` 为空时，不出 SQL schema 变体（`IntrospectedTableMyBatis3Impl.java:L381-L405`）。

### 4.3 强制刷新

`forceUpdateScalableElement` + `forceUpdateElementList` 用于强制重建可扩展元素，适用于 dao/service/controller/modelVo/createVo/excelVo/requestVo/updateVo/viewVo/maps 等受控产物（`BaseRules.java:L548-L557`，DTD 注释 `mybatis-generator-config_1_7_0.dtd:L38-L48`）。

---

## 5. 可扩展点：子类、模板、钩子、插件

| 扩展点 | 适用场景 | 限制 |
|---|---|---|
| 子类化 `AbstractJavaGenerator` / `AbstractXmlGenerator` / `AbstractHtmlGenerator` / 各 `*ElementGenerator` | 新增实体、SQL、Controller、HTML 片段 | 必须自行接入 `IntrospectedTableMyBatis3Impl` 的生成列表 |
| 重写 `addElements()` / `getCompilationUnits()` / `getDocument()` | 改单个工件的结构 | 需维持上下游命名、包路径和契约字段 |
| `Plugin` 钩子（`initialized`、`clientGenerated`、`sqlMapGenerated`、`htmlMapGenerated`、`contextGenerateAdditional*`） | 插入额外方法、文件、注解、脚本 | 不能假定所有默认生成器都会调用你的逻辑；默认返回值可能短路后续插件（`Plugin.java:L83-L231`, `CompositePlugin` 调用链） |
| `contextGenerateAdditionalWebFiles()` / `contextGenerateVue3OtherFiles()` | 批量追加前端或 Vue 文件 | 属于插件扩展，不是 core 固定产物（`Context.java:L462-L478`, `Plugin.java:L228-L246`） |
| 配置驱动的表级/上下文级属性 | 控制生成开关、路径、方言分支 | 属性名必须和 `PropertyRegistry` / DTD 一致 |

**推荐顺序**：先插件，后子类，再改核心生成器；不要直接手改已生成文件作为长期方案。

---

## 6. 与默认字段、方言分支、前后端契约的耦合点

### 6.1 默认字段

1. `DefaultColumnNameEnum.PARENT_ID` 直接决定是否生成树形/子节点相关 SQL（`XMLMapperGenerator.java:L107-L113`, `L177-L188`）。
2. `AbstractJavaGenerator.addInitialization()` 会读取实体父类默认字段，避免重复初始化，并对 `DbFiledDefaultValueEnum` 做懒加载 getter（`AbstractJavaGenerator.java:L128-L217`）。
3. Controller/HTML 生成会围绕 `viewParam`, `htmlFileName`, `viewStatus` 等契约字段联动，字段名变更会影响前后端（`ViewElementGenerator.java:L92-L153`）。

### 6.2 方言分支

1. `additionalDbTypes` 由 context/table 合并后产生，且仅在 `DatabaseType.getType()` 成功时纳入（`MyBatisGeneratorConfigurationParser.java:L613-L626`）。
2. `insertOrUpdateSelective` 按方言分组生成不同 SQL：MySQL/MariaDB、PostgreSQL 家族、Oracle 类、DB2/SQLServer 类、Phoenix（`InsertOrUpdateSelectiveElementGenerator.java:L36-L100`）。
3. 不支持的方言必须显式失败；该类默认 `throw IllegalStateException`，不能静默降级（`InsertOrUpdateSelectiveElementGenerator.java:L83-L100`）。

### 6.3 前后端契约

1. `htmlFileName` 是后端控制器和前端模板的共同契约字段（`ViewElementGenerator.java:L92-L121`）。
2. `htmlTargetPackage` / `viewPath` 决定页面目录结构，控制器会按该值挑选视图（`MyBatisGeneratorConfigurationParser.java:L1067-L1082`, `ViewElementGenerator.java:L92-L127`）。
3. `HtmlDocumentTypeEnum` 决定控制器如何选择 `editable/print/viewonly` 分支（`ViewElementGenerator.java:L92-L127`, `BaseRules.java:L611-L632`）。

---

## 7. 能力边界

### 7.1 可生成

1. 基于已内省表的 Java/XML/HTML/SQL 脚本。
2. 基于表级开关控制的 CRUD、批量、树形、统计、导入导出、权限脚本。
3. 基于插件的附加 Java/XML/HTML/Kotlin/Web/Vue 产物。

### 7.2 不可生成 / 不应默认承诺

1. **未被核心链路显式写出的 JS/CSS 独立文件**：当前只看到 HTML 文档写盘，没有对应独立生产器。
2. **未覆盖方言的 UPSERT/DDL 语法**：应显式失败，不能假装兼容。
3. **未配置 targetProject/targetPackage 的工件**：配置不完整时应在 validate 阶段暴露，而不是运行时吞掉。
4. **不在 `additionalDbTypes` 内的方言变体**：不会自动补出对应 SQL 版本。

### 7.3 推荐补齐路径

1. 新增产物：优先做 `Plugin.contextGenerateAdditional*`。
2. 新增单表工件：优先加新的 `*ElementGenerator`，再接入 `IntrospectedTableMyBatis3Impl`。
3. 新增方言：先补 `DatabaseType` / `DatabaseIntrospectorFactory` / SQL 分支，再补 DTD 与 parser。

---

## 8. 反模式

1. 只改单一数据库分支，忽略 `additionalDbTypes` 循环。
2. 直接硬编码包名/路径，跳过 `targetProject`、`targetPackage`、`viewPath` 计算。
3. 在生成后手工修改核心产物，替代插件或子类扩展。
4. 忽略 `forceUpdateScalableElement` 和 `cleanAllGeneratedElements` 的语义，导致重复生成或遗留旧文件。
5. 把 DTD 里的可选值当作唯一真相；`additionalDbTypes` 的合法值以 `DatabaseType.code` 为准。

---

## 9. 发布前检查清单

1. `context/table` 的必填属性已通过 validate。
2. `additionalDbTypes` 与 `DatabaseType.code` 一致。
3. `onlyTables`、`ignore`、`cleanAllGeneratedElements` 的跳过逻辑符合预期。
4. `htmlFileName`、`viewPath`、`htmlTargetPackage` 与控制器视图选择一致。
5. 所有需要的方言分支都已覆盖，unsupported 分支显式失败。
6. 插件返回值不会误伤后续默认生成。
7. 目标目录、覆盖策略、merge 策略已确认。
8. 新增扩展点优先走插件，不直接改已生成文件。

---

## 10. doc-index / doc-map 条目建议

建议入口项目补两条索引：

1. **doc-index**：`generator-authoritative-spec.md` → 生成器权威文档（核心入口，覆盖链路、输出、扩展点、边界）。
2. **doc-map**：关键词建议 `mybatis-generator / generator / additionalDbTypes / databaseId / HTML / plugin / SQL schema`，关联到 `src/main/resources/docs/generator-authoritative-spec.md`。

### 10.1 建议摘要

- **标题**：生成器权威文档
- **用途**：作为定制 MBG 的唯一生成规范入口
- **范围**：配置、内省、规则、渲染、输出、扩展、边界
- **关联源码**：`MyBatisGenerator.java`、`Context.java`、`TableConfiguration.java`、`BaseRules.java`、`MyBatisGeneratorConfigurationParser.java`、`XMLMapperGenerator.java`、`HTMLGenerator.java`

---

## 11. generatorConfig.xml 配置速查

> 读法约定：**DTD 定义结构**，**源码定义真实行为**。当两者不一致时，以源码行为为准。

### 11.1 根结构

| 节点 | 作用 | 关键点 |
|---|---|---|
| `generatorConfiguration` | 根节点 | 包含 `properties?`、`classPathEntry*`、`context+`。 |
| `properties` | 外部属性文件 | `resource` / `url` 二选一。用于占位符替换。 |
| `classPathEntry` | JDBC 驱动扩展 | `location` 必填。 |

### 11.2 `context`

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

**`additionalDbTypes` 说明**

- 这是 **表级/上下文级** 方言扩展集合。
- 合法值以 `DatabaseType.code` 为准，DTD 中的枚举只是兼容提示。
- 生成 SQL 时不能只写单方言分支，必须遍历 `additionalDbTypes`。

### 11.3 `table`

| 属性/子项 | 含义 | 默认/行为 |
|---|---|---|
| `tableName` | 表名 | 必填。 |
| `ignore` | 是否忽略生成 | `true` 时不进入后续生成。 |
| `domainObjectName` | 实体名 | 为空则按表名推导。 |
| `alias` | 表别名 | 影响 SQL 片段。 |
| `tableType` | `dataTable` / `relationTable` | 影响关联/关系表语义。 |
| `isModules` | 是否模块主表 | `true` 时可生成模块数据。 |
| `validateIgnoreColumns` | 免校验列 | 为空有默认忽略列。空字符串表示不忽略。 |
| `enableInsert` 等 | CRUD/批量/上upsert 开关 | 对应 `insert/select/update/delete/count/batch`。 |
| `enableDropTables` | 可清除业务表 | 需放在主模块表。 |
| `cleanAllGeneratedElements` | 清理所有生成元素 | 适合重建。 |
| `generatedKey` | 主键生成策略 | 序列/identity 等。 |
| `domainObjectRenamingRule` | 实体名重命名 | 字符串替换。 |
| `columnRenamingRule` | 列名重命名 | 字段命名预处理。 |
| `columnOverride*` | 列覆盖 | 改类型、注释、长度、必填、属性名等。 |
| `ignoreColumn*` | 忽略列 | 不生成 SQL/属性。 |
| `selectByColumn*` | 按列查询 | 可生成 controller 方法。 |
| `selectByTable*` | 中间表查询 | 用于多对多。 |
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

**表级歧义修正**

| DTD/注释 | 源码真实含义 |
|---|---|
| `sortColumn` | 实际属性是 `orderByClause`。 |
| `enableSelectByIds` | 实际属性是 `enableSelectByPrimaryKeys`。 |
| `excludedTable` | 实际属性是 `excludedTables`。 |

### 11.4 列级配置

| 节点 | 含义 | 真实行为 |
|---|---|---|
| `columnOverride` | 覆盖列元数据 | 可改 `property`、`javaType`、`jdbcType`、`typeHandler`、长度、注释、对齐。 |
| `ignoreColumn` | 忽略单列 | 不出 SQL / 不出属性。 |
| `ignoreColumnsByRegex` | 正则忽略列 | 支持 `except` 白名单。 |
| `generatedKey` | 生成列策略 | 适合序列/identity。 |

### 11.5 Java 模型 / VO

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

### 11.6 值转换 / 渲染

| 节点 | 用途 | 说明 |
|---|---|---|
| `overridePropertyValue` | 字段值转换 | 适合 `state_ -> stateText`、字典中文回填等。 |
| `columnRenderFun` | 列渲染函数 | 适合列表展示态格式化。 |
| `htmlElementDescriptor` | 表单控件定义 | 兼顾编辑态和展示态。 |

**`overridePropertyValue` 关键属性**

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

**常见用法**

```xml
<overridePropertyValue sourceColumn="f"
                       annotationType="DictSys"
                       typeValue="c"
                       targetProperty="fText"
                       targetPropertyType="java.lang.String"/>
```

### 11.7 页面生成

#### `generateHtml`

| 属性 | 含义 |
|---|---|
| `generate` | 是否生成页面。 |
| `type` | `editable` / `viewOnly` / `print`。 |
| `viewPath` | 页面路径。 |
| `title` | 页面标题。 |
| `overWriteHtmlFile` / `overWriteJsFile` / `overWriteCssFile` | 覆盖策略。 |
| `targetPackage` / `targetProject` | 页面包路径与项目路径。 |
| `default` | 默认页面。 |

#### `layout`

| 属性 | 含义 |
|---|---|
| `loadingFrameType` | `pop` / `inner` / `full`。 |
| `pageColumnsNum` | 页面列数。 |
| `barPosition` | 工具条位置。 |
| `uiFrameType` | `layui` / `zui` / `elPlus` / `antd`。 |
| `exclusiveColumns` | 独占整行字段。 |
| `labelWidth` | 标签宽度。 |
| `labelPosition` | 左/右/上。 |

#### `groupContainer`

| 属性 | 含义 |
|---|---|
| `elementKey` | 容器唯一标识。 |
| `type` | `border` / `tab` / `card` / `collapse`。 |
| `includeElements` | 包含哪些元素。 |
| `hideExpression` | 显隐表达式。 |

#### `htmlElementDescriptor`

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

**源码补充行为**

- `tagType` 缺失或 `column` 缺失时，元素通常不会被纳入生成。
- `dataFormat` 可被解析成默认枚举/字典配置。
- `DictSys` / `DictData` / `DictUser` 在未显式给 `dataUrl` 时，可由源码自动补齐数据源路径。

#### `htmlSwitchDescriptor`

| 属性 | 含义 |
|---|---|
| `column` | 字段名。 |
| `dataSource` | 一般是 `DictEnum` / `DictSys` 等。 |
| `dictCode` | 字典编码。 |
| `switchText` | 开关显示文案。 |

#### `htmlTableSelectDescriptor`

| 属性 | 含义 |
|---|---|
| `column` | 字段名。 |
| `dataUrl` | 选择数据接口。 |
| `beanName` | 数据源 bean。 |
| `listKey` / `listViewClass` | 内嵌列表来源。 |
| `multiple` | 是否多选。 |
| `defaultFilterExpr` | 默认过滤 SQL。 |

#### `htmlFileAttachment`

| 属性 | 含义 |
|---|---|
| `generate` | 是否生成附件控件。 |
| `elementKey` | 组件唯一标识。 |
| `multiple` | 是否多附件。 |
| `type` | `text` / `picture` / `picture-card`。 |
| `location` | `table` / `local` / `oss`。 |
| `restBasePath` | 存储/访问基础路径。 |

#### `htmlElementInnerList`

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

### 11.8 查询 / 关联 / 统计

| 节点 | 含义 | 备注 |
|---|---|---|
| `selectByColumn` | 按列查询 | `column` 支持多列，`genControllerMethod` 可生成接口。 |
| `selectByTable` | 中间表查询 | 适合多对多。 |
| `selectBySqlMethod` | SQL 方法查询 | 适合树/祖先/后代。 |
| `javaModelAssociation` | 一对一 | 级联行为需配 `relationProperty` / `beanClassFullName`。 |
| `javaModelCollection` | 一对多 | 常用于 `children` / 子查询。 |
| `generateTreeViewCate` | 树接口 | 依赖 `id/name/parentId` 语义。 |
| `generateOptions` | 下拉选项接口 | `flat` / `tree`。 |
| `generateDateDimStatistics` | 时间维度统计 | `total/yoy/mom`。 |
| `generateCustomStatistics` | 自定义统计 | 支持 joinTableClause。 |
| `joinTableClause` | 统计关联表 | 连接表达式必须是 SQL 表达式。 |

### 11.9 其他辅助配置

| 节点 | 含义 |
|---|---|
| `htmlButton` | 自定义按钮。 |
| `queryColumn` | 查询列配置。 |
| `filterColumn` | 过滤器列配置。 |
| `nameFragment` | `requestVo` 查询片段映射。 |
| `mapstructMapping` | DTO/VO 映射。 |
| `reusableElements` | 表级复用配置池。 |

### 11.10 常见歧义 / 纠错

| 文档写法 | 建议按源码理解为 |
|---|---|
| `sortColumn` | `orderByClause` |
| `enableSelectByIds` | `enableSelectByPrimaryKeys` |
| `excludedTable` | `excludedTables` |
| `Organization` / `Organ` | 以源码枚举/实现为准，不以注释单词判断 |
| `queryColumn` 中 `switchText` | 该属性不在 DTD 中，不能直接配置 |

### 11.11 快速配置策略

1. **先看表语义**：数据表 / 关系表 / 树表 / 模块主表。
2. **再看方言**：是否需要 `additionalDbTypes` 分支。
3. **再定输出**：实体、VO、XML、DAO、Service、Controller、HTML。
4. **最后补页面和映射**：字典、展示字段、渲染函数、校验、附件、内嵌列表。

### 11.12 推荐使用方式

- 配置前先按本节找对应节点。
- 对于“显示中文/字典映射/状态渲染”，优先判断是：
  - **值转换**：用 `overridePropertyValue`
  - **列表渲染**：用 `columnRenderFun`
  - **编辑控件**：用 `htmlElementDescriptor`
- 对于不确定项，优先以 **源码行为** 覆盖注释表述。

### 11.13 实际案例摘录（已脱敏）

#### 11.13.1 `reusableElements` + 列覆盖

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

#### 11.13.2 多对多 `selectByTable`

来自组织模块的角色-用户关系配置：

```xml
<selectByTable table="org_r_user_role" thisColumn="role_id" otherColumn="user_id"
               methodSuffix="ForUser" enableSplit="true" enableUnion="true">
    <enableCache/>
</selectByTable>

<selectByTable table="org_r_user_role" thisColumn="role_id" otherColumn="user_id"
               methodSuffix="IdsForUser" enableSplit="false" enableUnion="false"
               returnType="primaryKey"/>
```

用途：

| 配置 | 作用 |
|---|---|
| `enableSplit="true"` | 允许解除关联。 |
| `enableUnion="true"` | 允许新增关联。 |
| `returnType="primaryKey"` | 返回主键值而不是模型。 |
| `<enableCache/>` | 生成缓存支持。 |

#### 11.13.3 `htmlElementDescriptor` 的字典/组织/用户联动

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

#### 11.13.4 `columnRenderFun` 的列表展示渲染

```xml
<columnRenderFun fieldNames="name,username" renderFun="colDefsAsLink"/>
<columnRenderFun fieldNames="state" renderFun="colDefsState"/>
```

用途：

- `colDefsAsLink`：把文本列渲染成链接。
- `colDefsState`：把状态列渲染成启停样式。

#### 11.13.5 `generateCachePo`

```xml
<generateCachePo valueColumn="name_" includeColumns="name_,nick_name,username_" generate="true"/>
```

用途：生成缓存 PO，常用于字典值映射或名称查找。`valueColumn` 是翻译结果字段，`includeColumns` 决定缓存模型包含哪些列。

#### 11.13.6 `generateHtml` + 附件/打印页

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

#### 11.13.7 `javaModelCollection`

来自示例项目的子集合配置：

```xml
<javaModelCollection fieldName="members"
                     modelType="String"
                     whereColumn="id_"/>
```

用途：给实体添加一对多集合属性，适合子表、成员列表、明细集合等场景。
