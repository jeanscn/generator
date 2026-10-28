# 根实体字段数据库结构同步检查任务书

> 主体归属：`mybatis-generator-core`；协作：`mybatis-generator-maven-plugin`。
> 整体状态：完成；进度：3/3；更新时间/完成时间：2026-10-29。

## 1. 目标、范围与依据

检查生成器根据配置 `rootClass` 的 `@ColumnMeta` 同步数据库物理字段的默认触发条件、DDL 范围、执行结果和离线隔离方式，并将已核实的行为补充到生成器权威说明及配置指南。用户已授权按固定消费者配置执行全部未忽略表的自动结构同步 DDL；本任务仅检查和记录该能力，不修改结构同步实现或扩展配置开关。

依据：[生成器权威说明](..\generator-authoritative-spec.md)、[配置指南第 16 章](..\generator-config-guide.md#16-rootclass-范围父类关系与影响)、[生成器指令](..\..\..\..\..\..\.github\copilot-instructions.md)、[非主线问题登记规范](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\deferred-issue-management-standard.md)、[多子任务任务书规范](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\project-design-standard.md#81-多子任务执行与任务书管理)。

## 2. 已确认行为与边界

- Context 的 `rootClassSchemaSynchronizationEnabled` 默认 `true`。表被当前生成范围选中、启用生成语句且未标记 `ignore` 后，数据库元数据内省完成时会调用 `ValidateDatabaseTable.executeUpdate()`；普通 Maven 生成入口不主动关闭该行为。
- 同步字段来自该表配置的 `rootClass` 类及其父类中带 `@ColumnMeta` 的字段。缺列可生成 `ADD`，元数据不一致可生成 `MODIFY`；标为主键且数据库内省未识别为主键时，生成逻辑还可能将其加入 `ADD PRIMARY KEY`。该路径不生成删除列语句。
- DDL 通过当前 JDBC Connection 的 `Statement.execute()` 直接执行；成功后更新本次生成使用的内存列元数据。SQL 执行抛出 `SQLException` 时只加入 warning，不向生成调用传播异常，Maven Mojo 将 warning 记录为 WARN 后继续。
- `generate(..., false)` 仅禁止生成文件写盘，不提供数据库只读保证。独立离线驱动可通过 `Context.setRootClassSchemaSynchronizationEnabled(false)` 关闭结构同步；该开关没有 XML/DTD 属性。Mojo 的可选 `sqlScript` 是另一条独立脚本执行路径。
- 既有隔离回归使用记录并拒绝 DDL 的 Statement 验证 SQL 与失败警告，不连接真实数据库；未验证真实数据库的 DDL 兼容性或事务/自动提交行为。

若 DDL 执行失败，生成仍可能继续并按旧内省元数据输出，因此消费方再生成验收必须检查 generator warnings，并核对数据库实际结构。此行为作为 `generator-core-ISSUE-0008` 登记，当前不改变生成失败契约。

## 3. 子任务与验收

| 编号 | 内容 | 依赖 | 状态 | 验收条件及结果 |
|---|---|---|---|---|
| R01 | 检查触发范围、根实体字段匹配、DDL 构造/执行、失败处理和既有回归覆盖 | 无 | 完成 | 2026-10-29：源码确认 Context 默认值、非 ignore 范围、ADD/MODIFY/主键处理、失败仅 warning、成功后更新内存元数据及 Mojo 告警行为；既有隔离测试通过，未连接真实数据库。 |
| D01 | 更新生成器权威说明、配置指南、文档索引/映射及问题台账 | R01 | 完成 | 2026-10-29：权威说明 §2.1.1、配置指南 §16.4、索引/映射已更新；失败仅告警问题登记为 `generator-core-ISSUE-0008`，未修改生成器代码。 |
| V01 | 审阅文档一致性、链接与差异并归档验证证据 | D01 | 完成 | 2026-10-29：相关相对路径已核对；`git diff --check` 退出码 0；回归报告为 59 项通过、0 失败/错误/跳过。 |

验证命令（生成器仓库根目录 `D:\java\idea_projs14\generator\mybatis-generator`）：

```powershell
mvn -o -f D:\java\idea_projs14\generator\mybatis-generator\pom.xml -pl mybatis-generator-core -am "-Dtest=RootClassSchemaSynchronizationTest,DisabledServiceEntryGenerationTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Djava.io.tmpdir=D:\java\idea_projs14\generator\mybatis-generator\mybatis-generator-core\target" "-Denforcer.skip=true" test -q
```

2026-10-29 执行成功：`RootClassSchemaSynchronizationTest` 2 项、`DisabledServiceEntryGenerationTest` 57 项，均 0 失败/错误/跳过。验证使用离线数据库元数据与拦截 JDBC Statement 的测试替身，不执行真实 DDL。

## 4. 临时资产与清理

未创建一次性脚本、临时工具或数据库文件。Maven 报告及构建输出位于仓内既有 `mybatis-generator-core\target`，作为常规构建目录保留，不作为本任务临时交付物删除。消费者固定配置再生成属于 Sensitive 主任务，不纳入本任务范围。
