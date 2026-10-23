# Excel 导入模板表头说明与固定选项任务书

> 主体归属：`mybatis-generator-core`；协作：`vgosoft-plugins/vgosoft-excel`。
> 整体状态：完成；进度：3/3；更新时间：2026-10-23。

## 1. 目标、范围与确认决议

为生成器产生的 Excel 导入模板（`excelImport` 两阶段工作流及 `excelVo` 导入模板）补充字段录入说明和固定选项单元格下拉。用户确认采用以下约定：

- 适用于通用 `<excelImport>` 生成链，不限于 Sensitive 业务模块；不新增重复的模板字段配置。
- 说明写在表头单元格批注，不插入说明行或改变导入表头结构。
- 优先使用字段对应 `htmlElementDescriptor.tips`；未配置时回退到物理列注释；无注释或非物理字段不生成回退说明。
- 固定选项复用 `htmlElementDescriptor` 已解析的静态枚举配置；下拉项保存值与实际导入值一致。
- 动态数据源（如字典记录、组织或用户）不生成下拉。

## 2. 实施归属与验收

生成器负责按导入 VO 字段解析字段说明及固定枚举值，并将字段名及结果传给模板写出入口；`vgosoft-excel` 负责把元数据写成 XLSX 表头批注及数据有效性下拉。不得修改业务模块 `codegen` 生成产物，不连接数据库、不执行业务再生成、不安装或发布工件。

| 编号 | 工作内容 | 依赖 | 状态 | 验收条件 |
|---|---|---|---|---|
| E01 | 扩展 Excel 模板写出能力，生成表头批注与选项下拉并增加 XLSX 回归测试 | 无 | 完成 | 完成时间：2026-10-23；Excel 3 项定向测试通过，验证表头批注、隐藏选项页与下拉范围，以及无元数据兼容行为 |
| G01 | 解析生成配置中的字段 tips、物理列注释和静态枚举选项，并接入导入模板生成代码及回归 | E01 | 完成 | 生成器回归覆盖两种模板路径、tips 优先级、列注释回退、非物理附加字段不回退、固定枚举及动态数据源；新测试通过 |
| V01 | 同步权威说明/配置指南并执行跨模块定向验证、记录边界 | G01 | 完成 | Excel 插件定向测试通过；生成器新增回归通过；完整测试类仅有既有资源限额缺省断言失败，未扩大范围修复；未运行数据库或消费者业务再生成 |

## 3. 临时资产与最终结果

Excel 插件验证命令（退出码 0）：

```powershell
mvn -q -f 'D:\java\idea_projs6\vgosoft-plugins\pom.xml' -pl vgosoft-excel "-Dtest=VgoFesodSheetTemplateWriteTest" "-Denforcer.skip=true" test
```

生成器新增回归命令（退出码 0）：

```powershell
mvn -q -f 'D:\java\idea_projs14\generator\core\pom.xml' -pl mybatis-generator-core "-Dtest=ExcelImportWorkflowGenerationTest#generatesTemplateInstructionsAndOnlyFixedEnumOptions" "-Denforcer.skip=true" test
```

完整生成器测试类命令：

```powershell
mvn -q -f 'D:\java\idea_projs14\generator\core\pom.xml' -pl mybatis-generator-core "-Dtest=ExcelImportWorkflowGenerationTest" "-Denforcer.skip=true" test
```

该测试类共 8 项，其中两项新增模板元数据测试均通过，另 6 项中 5 项通过；既有 `rejectsEnabledWorkflowWithoutExplicitResourceLimits` 仍因解析器对缺省限额使用默认值、未抛出异常而失败。该断言与本次模板功能无关，未修改。

已同步 Excel 插件权威说明/能力概览及生成器权威说明/配置指南。未连接数据库、未执行消费者业务再生成、未安装或发布工件。临时资产：无。
