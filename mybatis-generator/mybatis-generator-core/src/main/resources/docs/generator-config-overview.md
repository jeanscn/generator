# generatorConfig.xml 配置能力概要

> 本文是配置文档的**概要层**：供需求分析、设计与评审阶段快速了解「配置能生成什么、由哪个配置节点控制、能力边界在哪」，不含具体参数写法。
> 具体参数含义、配置方法、适用场景与示例见[《generatorConfig.xml 配置指南》](generator-config-guide.md)（下称「详细指南」）；生成器对外能力契约见[《生成器权威说明》](generator-authoritative-spec.md)；节点/属性的合法结构以实际执行版本的 DTD 为唯一来源。
> 阅读路径建议：需求/设计阶段读本概要与权威说明；编写或评审 `generatorConfig.xml` 时，按本文锚点进入详细指南对应章节逐项核实。

## 1. 配置模型全景

`generatorConfig.xml` 结构为 `generatorConfiguration` → `properties` / `classPathEntry` / `context+` → `table+` → 列级元素与各 `generateXxx` 生成器子节点。一张表的全部产物由其 `table` 属性与各生成开关共同决定：**未配置的节点不生成对应产物；DTD 可配 ≠ Parser 已解析 ≠ 元数据已传递 ≠ 宿主已消费**。

| 配置层 | 关键节点 | 控制的生成范围 | 详细指南 |
|---|---|---|---|
| 根 | `properties`、`classPathEntry` | 外部属性/凭据文件、JDBC 驱动扩展 | §1 |
| context | `context` 属性、`jdbcConnection`、`javaTypeResolver`、`javaModelGenerator`、`sqlMapGenerator`、`javaClientGenerator`、`reusableElements` | 数据库连接与方言、模块/菜单/生成范围开关、输出根、跨表复用元素池 | §2 |
| table | `table` 属性、`enableCasWriteContract`、`enableSelectForUpdate`、`sensitiveCheck`、`rootClass` property、各 `generateXxx` 开关 | CRUD/批量/upsert 开关、受管 CAS、锁查询、敏感写检测、实体父类约定 | §3、§16 |
| 列级 | `columnOverride`、`ignoreColumn`、`ignoreColumnsByRegex`、`generatedKey` | 列重命名/类型/逻辑 JSON、忽略列、主键生成策略 | §4 |
| 模型/VO | `generateModel`、`generateVo`（六个 VO 子节点）、`generateCachePo`、`overridePropertyValue`、`additionalProperty` | Entity/Example、各类 VO 与映射、字典缓存、值转换展示字段 | §5、§6、§13.5 |
| 查询/关联 | `selectByColumn`、`selectByTable`、`selectBySqlMethod`、`javaModelAssociation`、`javaModelCollection`、`generateTreeViewCate`、`generateOptions`、`generateDateDimStatistics`、`generateCustomStatistics` | 按列/关系表/SQL 方法查询、一对一与一对多关联级联、树、下拉选项、统计接口 | §8、§13.2 |
| 持久层 | `generateSqlMap`、`generateDao` | Mapper XML（多方言 `databaseId` 分支）、DAO/Mapper 接口 | §3 |
| 服务/Web | `generateService`（含 `disabledMethod`）、`generateController` | Service 接口/实现/单测、Controller 端点与权限 action、显式停用入口 | §3、§5.2、停用入口 |
| 页面 | `generateHtml` 及各 descriptor、`layout/groupContainer`、`pageNavigator`、`htmlElementInnerList`、`htmlFileAttachment`、`approvalComment` | Vue3 页面套件、HTML 页、表单控件、布局、附件/审批/内嵌列表/打印 | §7 |
| 数据脚本 | `generateSqlSchema`、context `updateModuleData`/`updateMenuData` | DDL 脚本、权限/菜单/模块/流程类型期初 SQL | §5.3.5、§19 |

## 2. 后端产物能力清单

| 能力域 | 可生成的内容 | 配置入口 |
|---|---|---|
| 实体与查询模型 | Entity/主键类/WithBLOBs/Example；`@TableMeta`/`@ColumnMeta` 表列元数据注解，供运行期反射读取表与列映射 | `generateModel`、`rootClass` |
| Mapper 与 SQL | Mapper 接口、Mapper XML；标准 CRUD/批量/upsert/按列/关系/子查询语句，按 `additionalDbTypes` 产出多方言 `databaseId` 分支 | `generateDao`、`generateSqlMap`、`additionalDbTypes`、各 `selectByXxx` |
| Service | 接口与抽象/具体实现、可选单测；CRUD、关系维护、统计、Excel 工作流方法；事务/缓存/事件注解模板 | `generateService` |
| 受管写入与并发控制 | 受管 CAS 乐观锁 `updateByPrimaryKey(Selective)Cas` 与写契约清单；`versionCheck` 旧链乐观锁；按列逻辑删除（`selectByColumn` 的 `isLogicalDelete` property）；显式停用入口 `disabledMethod`（公开拒绝 + 可选 protected 内部原语） | `enableCasWriteContract`、table `versionCheck` property、`selectByColumn`/`generateService` 子配置；详见 §受管 CAS 输出、§3.1、§8.1、停用入口 |
| 敏感字段写检测 | 实体敏感注解、受管写方法 `@SensitiveWrite` 标记、事务模板、前端敏感字段常量；batch/upsert 入口须以 `disabledMethod` 封闭 | `sensitiveCheck`（§3.1） |
| 锁定查询 | `selectByPrimaryKeyForUpdate` / `selectByExampleForUpdate`（Mapper+XML+Service）及事务/条件/分页/缓存门禁类；多方言锁语法 | `enableSelectForUpdate`（§3.2） |
| Controller 端点 | 基础 CRUD/list/get/view、按列/关系/树/选项/统计/字典/Excel/视图端点、权限 action 与系统日志声明 | `generateController`、查询节点 `genControllerMethod`、`generateOptions`（§3、权威说明 §2.1.2） |
| VO 家族 | `ModelVo`/`CreateVo`/`UpdateVo`/`ViewVo`/`ExcelVo`/`ExcelImportVo`/`RequestVo` 及 MapStruct 映射方法 | `generateVo` 各子节点（§5） |
| 列表视图元数据 | `@ViewTableMeta`/`@ViewColumnMeta`/`@CompositeQuery`/`@ViewFuzzyColumnMeta`/`@QueryCondition`：默认显示列、复合查询、过滤、模糊检索、自定义组合条件、工具栏/行操作、默认排序、分类树、菜单元数据 | `viewVo` 及 `queryColumn`/`filterColumn`/`fieldOverrides`/`htmlButton`（§5.3、§5.3.2.1） |
| Excel 导入导出 | 旧 `GET export`、个性化 `GET export/options` + `POST export`（字段范围/顺序、scope/排序/行数限制/主键范围）、旧式逐行 `POST import`、两阶段 `import/preview`+`import/confirm` 工作流（资源限额必填）、模板表头批注与固定枚举下拉 | `excelVo.includeFields`、`excludeFields`、`ignoreFields`、`excelImport`（§5.1–5.2） |
| 字典缓存 | `{Domain}CachePo`、`selectByKeysDict`（含预取排序）、`/dict` 查询端点、缓存注解 | `generateCachePo`（§13.5） |
| 审计字段 | 实体/VO 审计字段约定输出、更新 SQL 审计列处理；运行时依赖宿主 `vgosoft.mybatis.audit.mode=strict` | 默认行为 + `rootClass`（§审计字段输出、§16） |

## 3. 前端与页面产物能力清单

- **Vue3 页面套件**：列表 `.vue`、`{Domain}Edit/Detail/Modal.vue`、`T{Domain}.ts` 类型、`PrivateUseFormHooks.ts`、`sharedVariables.ts`、`PrivateTableColumnSlots.vue`。触发条件：context `vueEndProjectPath` 非空 + `generateVo/viewVo` 启用 + 至少一个启用的 `editable`（或 `viewOnly`）`generateHtml`。表单/字段/容器/上传/内嵌列表/审批元数据经 VO 注解由 Framework `vgoForm` 消费（§7.7）。
- **HTML 旧链路**：Thymeleaf 页面与 JQuery JS/CSS；`viewPath`/`targetPackage`/`*overWrite*File` 控制路径与覆盖（§7.1、§7.7.2）。
- **表单控件**：`htmlElementDescriptor`（input/select/dropdownlist/switch/date/number/editor/markdown/tableselect/upload 等 tagType；Dict/DictData/DictSys/DictUser/DictEnum/Department/User/Organ 等数据源；`dataFormat` 快捷语义、`dataUrl`/`dataUrlParams`/`remoteApiParse`/`keyMap*` 远程选项、`hideExpression`/`disabledExpression`/`watchFields` 联动）、`htmlTableSelectDescriptor`（表格选择）、`htmlFormExtendDescriptor`（表单设计/渲染）。页面直接子节点 `htmlSwitchDescriptor` 当前未被解析，开关使用普通 descriptor 的 `tagType="switch"`（§7.3）。
- **布局与导航**：`layout`（列数/独占列/标签/弹窗）、`groupContainer`（border/tab/card/collapse 可嵌套，`includeElements` 决定字段顺序）、`pageNavigator`（锚点导航）（§7.1.3、§7.2）。
- **校验**：`validator`/`verify`/`requiredColumns`/`property` 字段状态 → Vue 表单规则注解；前端校验不替代服务端约束（§7.1.2、§7.4.1）。
- **其他组件**：附件 `htmlFileAttachment`、审批意见 `approvalComment`、内嵌列表 `htmlElementInnerList`（含编辑/分页/合计/打印）、打印页 `type="print"`、链接 `renderHref`/`hrefElement`、按钮 `htmlButton`、值转换 `overridePropertyValue`（Dict*/DictEnum 注解转换 + Text 展示属性）、列表渲染 `columnRenderFun`（§6、§7.4–7.6）。
- **复用**：context 级 `reusableElements` 提供跨表共享的字段/控件/列/过滤器描述（§13.1）。

## 4. 数据脚本与期初数据

- `generateSqlSchema` → `sql/<方言>/schema_<table>.sql`；关系表 `relationKeyColumns` 生成联合唯一/非空/反向索引（§13.2.1）。
- `updateMenuData="true"` + `isModules="true"` + 有效 `parentMenuId` + viewVo 启用 → `sql/init/data-menu-<moduleKeyword>.sql`；`updateModuleData` → 模块数据；Controller 端点 → 权限与权限动作 SQL；工作流根类 + `processDefinedKey` → 流程类型期初脚本（§5.3.5、§19.1）。
- **生成 SQL 文件不等于已执行入库**；初始化资源盘点、执行顺序、迁移接入与验收步骤见详细指南 §19。

## 5. 关键边界与常见误判

1. **版本一致**：配置 DOCTYPE 的 DTD 文件名必须与实际执行的 core 版本一致（当前 `mybatis-generator-config_1_8_0.dtd`），升级按详细指南 §1.1 规则核对。
2. **DTD 声明 ≠ 实际行为**：默认值与消费范围以 Parser/生成器源码为准；详细指南标注「未生效/已知偏差」的属性（如 `viewVo.enablePager`、页面直接子节点 `htmlSwitchDescriptor`、`hrefElement.method`、`validator` 的 `enum`/`whitespace`、`htmlFileAttachment` 的 `verify`/`hideExpression` 等）不可依赖。
3. **三个查询集合互相独立**：`queryColumns`（复合查询）、`fuzzyColumns`（模糊检索）、`filterColumns`（过滤器）须各自显式配置，不互相推导；组合条件 `queryType="condition"` 仅支持 `viewVo` 主视图（§5.3.2.1）。
4. **元数据 ≠ 业务实现**：`toolbar`/`actionColumn`/`htmlButton`/菜单/权限 SQL 只是声明，handler、服务端动作、授权与初始化执行须另行验证；「XML 可解析」「注解已输出」「页面可打开」均不等于行为生效。
5. **生成产物保护**：`codegen` 目录与 `@mbg.generated` 片段禁止手改；`@VCF.overridable` 为允许覆盖标记，人工扩展须先破坏标记（生成器仓 `.github/copilot-instructions.md` §2.1）。
6. **生成范围与副作用**：`onlyTables` 仅用于调试，全量再生成与 SQL 应用分别需要授权；`rootClassSchemaSynchronizationEnabled` 默认开启，生成时会向数据库执行 `ALTER`（§2、§16.5）。
7. **rootClass 是继承契约**：决定实体约定字段（id/审计/版本/排序/单号/申请人套件/工作流三要素）、字段去重、结构同步与 workflow/BLOB/business 生成分支，先选父类再定表结构（§16）。
8. **运行时依赖**：缓存、敏感检测、审计严格模式、锁定查询门禁、两阶段导入、受管关系写等均要求宿主提供对应基础组件与运行时配置；生成产物齐全不代表运行时能力已安装。

## 6. 需求到配置的决策路径

1. 表语义：数据表/关系表/树表/模块主表 → `tableType`、`isModules`、关系节点。
2. 方言：是否需要 `additionalDbTypes` 多方言输出。
3. 输出层次：实体 → VO → XML/DAO → Service → Controller → 页面 → 数据脚本，逐层选开关。
4. 字段语义：每个字段的业务含义 → 控件类型、数据源、校验、值转换、查询/过滤归属。
5. 约定适用性：默认列描述、自动控件推断、rootClass 约定字段是否满足业务；不满足再显式配置或评审扩展。
6. 核对证据链：物理列 → Entity/VO → XML/Mapper/Service/Controller → 元数据与宿主消费 → 权限/事务/错误 → 实际运行结果。配置前准备清单与实践决策入口见详细指南 §14、§20。

## 7. 文档地图

| 文档 | 定位 | 适用阶段 |
|---|---|---|
| [generator-config-guide.md](generator-config-guide.md) | 详细配置指南（使用手册）：全部参数含义、配置方法、适用场景与示例 | 编写/评审配置、生成实施 |
| [generator-authoritative-spec.md](generator-authoritative-spec.md) | 生成器对外能力契约与产物边界 | 需求/设计/评审 |
| `mybatis-generator-config_1_8_0.dtd` | 节点/属性合法结构的唯一来源 | 配置编写与校验 |
| 各业务模块 `*-generator-config-guide.md` 与 `generatorConfig.xml` | 消费项目的实际配置事实与保护策略 | 项目配置实施 |
| mpmm-ui `packages/framework/docs/` | Framework 表单/列表元数据的宿主消费契约 | 前端消费核验 |
