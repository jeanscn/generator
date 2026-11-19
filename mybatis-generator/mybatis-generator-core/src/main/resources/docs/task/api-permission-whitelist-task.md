# Controller 接口权限白名单生成任务书

> 主体归属：`mybatis-generator-core`；协作：无跨仓改动（消费项目重新生成不在本任务范围）。
> 整体状态：完成（全量回归余项为既有基线漂移，已登记 generator-core-ISSUE-0013）；进度：5/5；更新时间：2026-11-20。

## 1. 范围与确认

用户确认将 Controller 接口权限从"集成 Spring Security 即全量生成"改为"默认不生成、按白名单生成"：

- 配置落点：`<generateController>` 内 `<property name="apiPermissions" value="..."/>`（逗号分隔方法前缀白名单）；未配置或为空时不生成 `@PreAuthorize` 与 `data-permission-*.sql` 权限数据；保留值 `all` 表示全量；保留值 `relation` 匹配关系维护类端点（权限 code 仍取动态方法名，亦可按动态 code 精确匹配单个关系端点）。
- `integrateSpringSecurity` 仍为总开关；`PreAuthorize` import 按方法上实际存在的注解决定是否引入，零权限输出不留未使用 import。
- 接口权限注解与权限数据同生同灭（唯一发射点为 `AbstractControllerElementGenerator.addSecurityPreAuthorize`）；按钮级 `permission_action` 仍由 `isConfigurable` 驱动，不在本次范围。
- 权限显示名按已确认清单统一调整（如"数据列表"→"列表查询"、"调用获取上级或下级标识接口"→"上级/下级标识查询"、关系端点改为"添加/删除{对方列备注}关联"）。
- 不考虑存量项目迁移；DTD 不变（`generateController` 已允许 `property` 子元素，仅更新 DTD 注释补全取值清单）。

依据：[生成器指令](../../../../../../.github/copilot-instructions.md)、[生成器权威说明 §2.1.2](../generator-authoritative-spec.md)、[生成器配置指南](../generator-config-guide.md)、[设计规范 §8.1](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\project-design-standard.md)。

## 2. 子任务

| 编号 | 内容 | 依赖 | 状态 | 验收条件及结果 |
|---|---|---|---|---|
| G01 | `PropertyRegistry` 新增 `CONTROLLER_API_PERMISSIONS`；`Rules`/`BaseRules`/`RulesDelegate` 新增 `getApiPermissionPrefixes()`（解析 `<generateController>` property 为小写集合） | 无 | 完成 | 编译通过；未配置返回空集合；`RulesDelegate` 如实透传底层白名单 |
| G02 | `AbstractControllerElementGenerator` 增加白名单判定与 `relation` 匹配通道（4 参重载 `permissionKey`）；`JavaControllerGenerator` 的 `PreAuthorize` import 改为在全部元素生成后按方法实际注解扫描引入 | G01 | 完成 | 白名单未命中/含未命中 token 时不加注解、不产 SQL、不留 import |
| G03 | 全部 Controller element 的 `nameKey` 按确认清单调整；`InsertByTable`/`DeleteByTable` 改为 `添加/删除{otherColumn.getRemarks(true)}关联` 并传 `relation` 匹配键；`OptionElementGenerator` 传 `option` 匹配键 | G02 | 完成 | 27 个调用点逐一核对，名称与确认清单一致 |
| G04 | 更新 `ExportElementGeneratorTest` 权限用例并新增白名单用例；新增 `ApiPermissionWhitelistGenerationTest`（relation 保留值、动态 code 精确匹配、总开关抑制）；CAS 测试注入 `all` 保持原夹具语义；`mvn -pl mybatis-generator-core compile` 通过 | G02、G03 | 完成 | 定向 14 项全部通过；全量回归失败均为既有漂移（见 §4） |
| G05 | 同步 `generator-config-guide.md`、`generator-authoritative-spec.md`、DTD `1_8_0` 注释（仅留可拷贝的逗号取值清单，对照表归配置指南）；doc-map 登记；台账 ISSUE-0010 关闭、ISSUE-0013 登记；任务书收尾 | G04 | 完成 | 文档描述与实现一致 |

## 3. 临时资产与清理

无一次性脚本或临时资产；测试输出限定在标准构建目录（`target/`）。任务书完成后无遗留临时文件待清理。

## 4. 验证记录

- 2026-11-20 编译：`mvn -o -pl mybatis-generator-core compile` 通过。
- 定向测试：`ExportElementGeneratorTest` 11/11、`ApiPermissionWhitelistGenerationTest` 3/3、`CasConsumerBaselineTest` 1/1、`LogicalDeleteGenerationTest`/`CasWriteGenerationTest` 组合 47/47 通过（当时批次）。
- 端到端产物（`capturesCompleteConsumerOutputWithoutExternalWrites`，快照未配置 `apiPermissions`）：102 个文件，Controller 0 处 `@PreAuthorize`、0 处 `PreAuthorize` import、无 `data-permission-<table>.sql`；`data-permission-action-*.sql` 正常产出（按钮级权限不受白名单影响）。
- `all` 模式产物（快照临时注入验证后已还原）：15 处注解、接口权限 SQL 按新显示名产出、code/id 不变。
- 全量回归 722 项：失败 4 项全部归因——`retainsCompleteVersionedRawBaseline`（3 个 Service Impl 既有漂移，登记 generator-core-ISSUE-0013）、`SelectForUpdateGenerationTest.offlineOutputBaseline` 与 `SensitiveGenerationTest.offlineOutputBaseline`（Service Impl 层摘要漂移，同属 ISSUE-0013，两夹具 `generateController="false"` 与本任务无交集）、`VueUiConventionAuditTest`（generator-core-ISSUE-0012 已登记）。`retainsAllDisabledOutputsAndLimitsOptInDiff` 通过。
- 基线对齐：`consumer-current-v1-parser-baseline.zip` 中两个 `data-permission-*.sql` 按批准的显示名方案更新（sensitive_category 经 logical-delete 路径、sensitive_word 经 raw 路径生成核对）；3 个 Impl 漂移文件未改，登记待负责人批准重录。
- 2026-11-20 追加需求并落地：①权限 `id_` 为 `MD5_15(code)` 确定性哈希，重复生成 id 不变（既有行为，确认无需改动）；②指定 `parentMenuId` 时接口权限仅产末级行直挂菜单（menu→action，code/name 仍按三级累计），未指定时保持 `module→bean→action` 且 module `parent_id='0'`——`setPermissionSqlData` 增 `parentMenuId` 参数、`getParentMenuId` 改 null-safe、发射点解析 view→context 兜底。新增 `parentMenuIdHangsLeafPermissionRowDirectlyUnderMenu` 用例，定向 4/4 通过；sc03 快照配 `parentMenuId` 基线两个权限 SQL 已按新形态重录（id_ 与旧末级行一致）。
- 下游待办（不在本任务执行）：业务项目需在 `<generateController>` 配置 `apiPermissions` 后重新生成 Controller 与权限 SQL，并同步权限数据入库。
