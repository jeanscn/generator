# generatorConfig.xml 配置指南

> 读法约定：**DTD 定义结构**，**源码定义真实行为**。当两者不一致时，以源码行为为准。

## 1. 根结构

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
| `enableInsert` 等 | CRUD/批量/upsert 开关 | 对应 `insert/select/update/delete/count/batch`。 |
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

`logicalType` 当前仅接受 `JSON`（大小写不敏感），且对应 Java 类型必须为 `String`；其他非空值明确报错。`FieldJsonFormatPlugin` 对这些列生成 `tools.jackson.databind.annotation.JsonSerialize(using = JsonRecordSerializer.class)`，不改变物理类型、JDBC 类型、数据库结构或长度验证。未配置的普通 TEXT 列不自动视为 JSON；物理 JSON 列保留原有自动识别。日期格式注解仍使用官方 `com.fasterxml.jackson.annotation.JsonFormat`。

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
| `selectByTable` | 中间表查询 | 适合多对多。 |
| `selectBySqlMethod` | SQL 方法查询 | 适合树/祖先/后代。 |
| `javaModelAssociation` | 一对一 | 级联行为需配 `relationProperty` / `beanClassFullName`。 |
| `javaModelCollection` | 一对多 | 常用于 `children` / 子查询。 |
| `generateTreeViewCate` | 树接口 | 依赖 `id/name/parentId` 语义。 |
| `generateOptions` | 下拉选项接口 | `flat` / `tree`。 |
| `generateDateDimStatistics` | 时间维度统计 | `total/yoy/mom`。 |
| `generateCustomStatistics` | 自定义统计 | 支持 `joinTableClause`。 |
| `joinTableClause` | 统计关联表 | 连接表达式必须是 SQL 表达式。 |

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
| 关系设计  | 是否需要 `selectByTable`（多对多）、`selectByColumn`、`javaModelCollection`。 |
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

## 17. 补充：与 `mybatis-generator-config_1_7_0.dtd` 的对照关系

本指南是“可执行解释”，DTD 是“结构定义源”。两者配套使用时，建议按“先 DTD 定位节点，再回本指南看行为与案例”的顺序。

### 17.1 DTD 文件位置

- `mybatis-generator-core/src/main/resources/org/mybatis/generator/config/xml/mybatis-generator-config_1_7_0.dtd`

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
