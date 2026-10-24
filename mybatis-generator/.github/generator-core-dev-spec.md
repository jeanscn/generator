# 生成器内核再开发补充说明（Core Dev Spec）

本文件与 `.github/copilot-instructions.md` 建立关联：  
**`copilot-instructions.md` 为 Copilot 主指令入口，本文件仅保留补充与迁移说明。**

## 1. 适用边界

- 面向本仓库生成器内核继续开发与完善
- 不用于业务项目配置编写

## 2. 主规范引用

- Copilot 执行主规范：`.github/copilot-instructions.md`
- 下游业务项目设计阶段（本项目定位：能力说明）：`src/main/resources/docs/generator-authoritative-spec.md`
- 下游业务项目实现阶段（本项目定位：使用手册）：`src/main/resources/docs/generator-config-guide.md`

## 3. 迁移说明

- 历史上“生成器权威文档”中偏内核实现/耦合/边界的内容，已迁移并汇总到 `copilot-instructions.md` 统一管理。
- 新增“内核再开发规范”优先修改 `copilot-instructions.md`，本文件只做补充说明，避免双份规范漂移。
- Service 接口与实现的方法构建遵守[主规范第 8.1 节](copilot-instructions.md#81-service-方法签名共用与实现装配规范)：签名及依赖归 `ServiceMethods`，接口生成器与实现元素复用，注解及方法体由实现元素装配；以 Excel 导入预览/确认方法为参照，正文不在此重复维护。
- 关于“多方言动态 SQL 兜底”的新增约束：以 `copilot-instructions.md` 的“6.1 动态 SQL 多方言统一兜底策略”为准（原生语法优先，不支持时实现可跨方言执行的等价兜底 SQL；重复冲突忽略语义可使用 `INSERT ... SELECT ... WHERE NOT EXISTS (...)` 作为典型方案）。
- 当前 Windows/PowerShell/Maven 的命令环境约束，也以 `copilot-instructions.md` 的“11. 当前环境与命令约束”为准。
- DTD 发布维护遵守 `copilot-instructions.md` 第 7.1 节；业务配置的版本一致及升级门禁统一见 [生成器配置指南第 1.1 节](..\mybatis-generator-core\src\main\resources\docs\generator-config-guide.md#11-生成器与配置-dtd-版本一致规则)，不在本补充说明复制规则正文。
- 当前敏感生成按配置、内省后预检、共享字段解析、注解描述、模型装配、Service elements 和 Vue3 插件分层；`custom.annotations.SensitiveCheckDesc` 只渲染注解文本并维护 imports，`SensitiveModelAnnotations` 继续负责实际字段验证、插件否决保护及去重。锁查询预检保持生成侧，共享锁 SQL 策略归 `dbstrategy`。实际类路径、插件前后调用顺序及职责唯一维护于[配置指南第 3.1 节](..\mybatis-generator-core\src\main\resources\docs\generator-config-guide.md#31-敏感字段与受管写方法180)，锁查询结构重构见[正式任务书第 7 节](..\mybatis-generator-core\src\main\resources\docs\task\select-for-update-task.md#7-生成侧职责归属调整2026-10-14)，注解描述结构调整见[第 8 节](..\mybatis-generator-core\src\main\resources\docs\task\select-for-update-task.md#8-敏感实体注解描述职责拆分2026-10-14)。
