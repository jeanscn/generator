# 生成器权威文档（设计阶段）

本文档用于**基于生成器的新项目设计阶段**：回答“能做什么、适合做什么、边界在哪里”。
实现细节与具体 XML 写法请转到 `generator-config-guide.md`。

---

## 1. 文档定位

- **面向对象**：方案设计者、架构师、AI 生成代理
- **阶段定位**：需求分析 / 方案设计 / 能力评估
- **核心目标**：减少两类误判
  1) 本可配置生成，却误判成“必须二开”
  2) 配置无法覆盖，却误判成“可直接生成”

---

## 2. 能力总览（What）

## 2.1 后端可生成能力

| 能力域 | 可直接生成内容 | 设计含义 |
|---|---|---|
| 基础数据访问 | Entity / PrimaryKey / Example / Mapper XML / Mapper 接口 | 标准 CRUD 与 Example 条件查询可直接落地 |
| 应用层 | Service / ServiceImpl / Controller（含常用接口） | 列表、详情、新增、修改、删除、批量、导入导出等骨架统一 |
| DTO/VO | modelVo/createVo/updateVo/requestVo/viewVo/excelVo | 读写分离、查询入参、展示出参可统一建模 |
| 关系查询 | `selectByColumn` / `selectByTable` / `selectBySqlMethod` | 可覆盖按列、经中间表、多层级 SQL 方法查询 |
| 统计 | `generateDateDimStatistics` / `generateCustomStatistics` | 常规统计口径可配置化 |
| 值转换 | `overridePropertyValue` / `columnRenderFun` | “存值-显文案”与展示态渲染可配置 |

启用 `excelVo` 时，单列字符串主键表的 Controller 保留旧 `GET export?ids=...`，并生成 `GET export/options` 元数据及 `POST export` 个性化导出。POST 的 `query` 使用该表实际列表查询类型，复用 `buildExample` 与 `filterParam`；强制恢复权限/逻辑删除约束，清除客户端原始 SQL 条件及排序，仅允许内省列白名单排序并追加主键稳定排序。`all` 用 PageHelper 每批最多 500 行同步消费，不把全部结果集合驻留内存；`page/selected` 最多 500 个不同字符串主键，验证全部记录可见后按请求顺序或白名单排序导出，再截取 `maxRows`。PDF 上限 5000 行，其他格式 50000 行；格式、列选择和文件输出复用插件公共契约，不由生成器实现。无主键、复合或非字符串主键表跳过导出端点并输出生成警告。接入要求见[配置指南](generator-config-guide.md)第 5.1 节。

个性化导出通过生成列限制排除内省 BLOB/LONGVARCHAR 字段及配置的相关派生字段；元数据不广告未被 `selectByExample` 填充的大字段，客户端请求这些字段在查询前失败，不静默输出空值。

## 2.2 Vue3 前端可生成能力

| 能力域 | 可直接生成内容 | 设计含义 |
|---|---|---|
| 页面骨架 | 列表页、编辑页、详情页、弹窗页（dialog/drawer） | 中后台标准模块可快速成型 |
| 表单/列表配置 | 表格列、查询区、过滤区、按钮区、默认排序 | 大多数结构化交互可通过配置表达 |
| 复杂部件 | innerList、附件、审批意见、树面板入口 | 主子表/流程类页面可直接产出主干 |
| 类型与扩展 | TS 类型文件、共享变量、Hooks 扩展文件 | 生成后可在稳定扩展点二次开发 |

## 2.3 多方言能力

| 能力 | 机制 | 设计要求 |
|---|---|---|
| 一套配置多方言生成 | 基于 `additionalDbTypes` 循环生成 `databaseId` 分支 SQL | 不能只按当前连接数据库设计 |
| 语义分组 | 按 SQL 语义能力分组（如 upsert/分页/函数） | 不按历史模板机械分组 |

## 2.4 AI 约定优先规则（Convention First）

> 这一节用于给 AI 做“默认推断”，减少重复确认。

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
