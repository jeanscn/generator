# 字典预取排序与共用查询实施任务书

> 主体归属：`mybatis-generator-core`；协作：`vgosoft-basic` 的 `vgosoft-core-annotation`、`vgosoft-web2`。
> 整体状态：完成；进度：3/3；更新时间/完成时间：2026-10-23。

## 1. 范围与依据

用户于 2026-10-23 确认：CachePo 开启时同时生成常规与预取方法；有效排序列按默认字段和全部主键统一解析；生成字典键、排序元数据；两个公开入口委托生成的私有查询方法；取消字典键必须位于排序末尾的限制，保留比较安全、稳定顺序和字典键唯一校验。

依据：[生成器指令](..\..\..\..\..\..\.github\copilot-instructions.md)、[配置指南](..\generator-config-guide.md)、[全局过程门禁](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\ai-assisted-development-standard.md)、[多子任务规范](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\project-design-standard.md#81-多子任务执行与任务书管理)。

选择扩展生成器，而非修改受保护业务产物。仅修改生成器源码、注解、Web 适配、正式测试及相关文档；不连接数据库、不执行业务再生成、不安装或发布共享工件。

## 2. 设计与验收

- 有效排序列为显式配置，或存在的 `sort_`、`created_`；按内省顺序补齐缺失主键。SQL、CachePo、注解共用解析结果，升序、NULLS FIRST。
- `keyProperty` 用于字典匹配，`orderProperties` 用于顺序，新增可选 `uniqueOrderProperties` 声明排序中的非空唯一组合（生成器自动填写全部主键）。旧注解在排序包含字典键时沿用键唯一依据，不限制位置。
- 未能声明唯一依据时不启用预取；无主键且配置排序不含字典键时，方法仍可生成，但 Web 跳过预取并记录原因。
- 多批合并前逐行逐列检查全部排序值，保留精确数值、明确时间类型及规范小写 UUID 字典键的保守比较范围；不自动放开普通字符串或非字典键字符串主键。运行时拒绝空/重复唯一组合、未知/空/重复字典键；失败不提交部分缓存并回退。
- Service 常规入口保留签名、缓存注解与键转换，预取入口接收字符串键；私有方法统一查询和映射，不承载缓存注解。
- 真实数据库 collation、宿主性能、业务再生成和工件供应不属于本轮离线验收，不声明已交付至消费者。

## 3. 子任务

| 编号 | 内容 | 依赖 | 状态 | 验收条件及结果 |
|---|---|---|---|---|
| G01 | 生成器统一排序、签名与私有查询、配置/DTD/文档和回归 | 无 | 完成 | 2026-10-23：11 项生成器测试及源码编译通过；真实接口/实现渲染后经 JDK 内存编译，覆盖 String、BigDecimal、Long，默认/显式/联合主键排序、配置校验及 CachePo 可见性 |
| W01 | 注解唯一排序元数据、Web 安全校验及回归、能力文档 | 无 | 完成 | 2026-10-23：注解/Web reactor 联动 40 项通过；包含新增排序契约 19 项、新旧注解、键非末项、独立/联合主键、全字段比较检查及无部分缓存 |
| V01 | 联动检查、文档入口、交付与清理 | G01、W01 | 完成 | 2026-10-23：最终复跑生成器 11 项、注解/Web 40 项全部通过；生成器相关 diff 空白检查通过；双仓索引/映射、能力文档、配置指南、当前 DTD 及列表方案已同步；无待清理临时资产 |

## 4. 临时资产与最终结果

未创建一次性脚本或测试工具，无待清理临时资产；Maven `target` 为既有构建输出，不作为临时交付文件删除。正式源码、测试 fixture、问题台账与本任务书均为持久交付资产。

Web 命令（2026-10-23，退出码 0）：

```powershell
mvn -o -q -pl vgosoft-core-annotation,vgosoft-web2 "-Dtest=DictPrefetchOrderContractTest,DictPrefetchResponseBodyAdviceTest,DictionaryRequestCacheTest,RequestCacheTest,JacksonCustomConfigTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Denforcer.skip=true" "-DskipTests=false" "-Dmaven.test.skip=false" test
```

报告：`DictPrefetchOrderContractTest` 19、`DictPrefetchResponseBodyAdviceTest` 5、`DictionaryRequestCacheTest` 5、`RequestCacheTest` 4、`JacksonCustomConfigTest` 7，均零失败/错误/跳过。该 reactor 直接消费本次注解源码，未安装工件。

首次使用 `-pl vgosoft-web2 -am` 的扩大 reactor 因既有 `vgosoft-system` 测试引用缺失类型而在上游 `testCompile` 失败，未修改无关测试。问题为中优先级，已登记 [vgosoft-system-ISSUE-0001](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-system\src\main\resources\docs\pending-issues.md)，定向联动不受影响；未宣称完整 reactor 通过。

生成器命令（目录 `D:\java\idea_projs14\generator\core`，2026-10-23，均退出码 0）：

```powershell
mvn -q -o -pl mybatis-generator-core "-Dtest=DictPrefetchGenerationTest,DictCacheOrderConfigurationTest,PoCacheGeneratorTest" "-Denforcer.skip=true" test
mvn -q -o -pl mybatis-generator-core "-DskipTests" "-Denforcer.skip=true" compile
```

11 项测试通过；生成器统一渲染接口、实现、SQL、CachePo，并对生成 Java 执行内存编译。生成 Java 编译使用包含新成员的注解契约替身，不能表述为共享工件已供应；Web 定向 reactor 使用实际新注解源码。新配置 fixture 为仓内正式测试资源 `src\test\resources\dict-prefetch-generator.xml`，不依赖组织机构仓路径或数据库。

兼容性：常规入口的 `types == null` 不添加类型条件；BigDecimal 类型及键转换使用 `toPlainString()`，以实际 Java 类型判断，其他数值键沿用 `String.valueOf`；字符串空键原样传递。CachePo 关闭不新增字典方法。原有缓存键、缓存注解及 Mapper 插件接点保留；默认排序、空值次序及新增预取方法是本次批准的有意变化。

未覆盖真实数据库/驱动/collation、宿主代理缓存和性能、业务产物再生成及成套工件升级；未发布或安装工件，不声称业务消费者已接入。

最终交付：生成器源码及配置/输出回归、实际注解及 Web 消费回归完成；两仓仅更新各自能力及过程文档入口，未改业务受保护生成物，未处理既有无关源码变动。消费者须成套升级注解、Web、生成器，再按批准范围受控再生成；本任务完成范围为源码、离线验证及文档，不包括上述消费部署验收。
