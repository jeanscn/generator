# MyBatis Generator Core 文档映射

> 本映射登记本仓库文档归属及非主线问题台账定位规则；通用规范正文由其权威项目维护。

| 能力类别 | 文档路径 | 所属项目 | 性质 | 备注 |
|---|---|---|---|---|
| Service 方法生成结构 | [主规范第 8.1 节](copilot-instructions.md#81-service-方法签名共用与实现装配规范) | `generator/core` | 再开发规范正文 | `ServiceMethods` 统一签名与依赖，接口生成器及实现元素复用；实现注解、方法体与业务钩子留在元素层 |
| 字典预取排序与共用查询 | [任务书](..\mybatis-generator-core\src\main\resources\docs\task\dict-prefetch-order-contract-task.md) | `mybatis-generator-core` | 跨仓过程与验收资产 | 批准范围、G01/W01/V01 进度及离线验证证据；规则正文仍归配置指南 §13.5 |
| 生成器能力 | `mybatis-generator-core\src\main\resources\docs\generator-authoritative-spec.md` | `mybatis-generator-core` | 权威正文 | 生成器能力、输入输出、扩展点与限制；§2.1.1 定义 Entity `@TableMeta`/`@ColumnMeta` 的元数据契约及边界 |
| 生成器配置 | `mybatis-generator-core\src\main\resources\docs\generator-config-guide.md` | `mybatis-generator-core` | 使用手册 | 配置、DTD 版本与生成行为；§5.0 提供从 Entity 类/字段注解读取物理表列映射的设计/开发指引 |
| 生成配置实践 | [配置指南第 20 章](..\mybatis-generator-core\src\main\resources\docs\generator-config-guide.md#20-约定驱动配置与实践建议) | `mybatis-generator-core` | 实践决策索引 | 具体操作在主题章节维护；§7.2.3 布局、§7.3.1 文本输入、§13.5.1 分类/业务字典引用 |
| 实践指导持续沉淀 | [全局过程 §1.5](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\ai-assisted-development-standard.md#15-实践指导的持续发现确认与沉淀) | `vgosoft-doc-spec` | 外部跨项目过程规范 | 发现、提案、许可与唯一正文归属 |
| 约定驱动设计 | [设计规范 §3.5.6](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\project-design-standard.md#356-约定驱动的字段与页面设计实践) | `vgosoft-doc-spec` | 外部跨项目设计规范 | 字段/页面取舍、配置与项目记录 |
| 生成输入建模实践 | [数据库规范 §4.5、§4.6](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-core-db\src\main\resources\docs\database-authoritative-spec.md#45-面向生成与业务使用的字段设计实践) | `vgosoft-core-db` | 外部数据库规范 | 字段设计及 apply_date 默认值分层事实 |
| 非主线问题登记规范 | [非主线问题登记与后续处理规范](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\deferred-issue-management-standard.md) | `vgosoft-doc-spec` | 外部跨项目过程规范 | 非主线问题登记与后续处理唯一权威；本仓库仅维护引用入口，不复制规则正文、分级表或模板 |
| 版本库提交说明规范 | [版本库提交说明（commit message）规范](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\commit-message-standard.md) | `vgosoft-doc-spec` | 外部跨项目过程规范 | 中文 commit message 交付时机、格式与类型、验证状态标注及多仓库组织唯一权威；本仓库仅维护引用入口，不复制规则正文或模板 |
| 文档体系总览 | [文档体系总览](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\doc-system-overview.md) | `vgosoft-doc-spec` | 全局规范 | 四层文档结构与使用规则；建/改文档、索引、映射前确认分层与归属 |
| 全局文档编写提示词 | [全局文档编写提示词](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\global-authoring-prompt.md) | `vgosoft-doc-spec` | 全局规范 | 编写任何 md 前必读；强制原则与分阶段最小读取流程 |
| 跨项目文档标准 | [跨项目文档标准](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\cross-project-doc-standard.md) | `vgosoft-doc-spec` | 全局规范 | 规范资产与能力/业务项目的文档分工；只维护入口，不复制外部权威正文 |
| 最终命名规范 | [最终命名规范](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\final-naming-standard.md) | `vgosoft-doc-spec` | 全局规范 | 文档命名、路径引用与内容边界；外部权威正文保持原文件名，不做跨项目改名 |
| Java 模块问题台账 | `<module>\src\main\resources\docs\pending-issues.md` | `generator/core` | 模块问题台账定位规则 | 复用模块现有文档目录；首次发现需登记的问题时创建，尚未创建的不写为现存文档链接 |
| 项目级/归属待定问题台账 | `docs\pending-issues.md` | `generator/core` | 项目问题台账定位规则 | 仅项目级或尚未确定模块归属的问题使用；首次发现需登记的问题时创建 |
