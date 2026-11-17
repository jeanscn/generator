# MyBatis Generator Core 文档索引

> 本项目会话入口位于 `.github\copilot-instructions.md`；每次会话必读本文、文档映射及其中引用的跨项目规范。

设计与编码共同入口：[设计规范 §6.3](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\project-design-standard.md#63-适度设计校验责任与主流程可读性)规定适度设计、校验责任、函数抽取与主流程可读性；[全局过程规范 §1.6](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\ai-assisted-development-standard.md#16-复杂度范围与工具流程变更控制)规定额外复杂度、范围及工具流程变更的确认要求。唯一正文均归属 `vgosoft-doc-spec`。

1. [生成器权威说明](..\mybatis-generator-core\src\main\resources\docs\generator-authoritative-spec.md)
   用途：生成器能力、输入输出、扩展点与限制；§2.1.1 说明 Entity 的 `@TableMeta`/`@ColumnMeta` 元数据契约及适用边界。
2. [生成器配置能力概要](..\mybatis-generator-core\src\main\resources\docs\generator-config-overview.md)
   用途：配置可生成能力全貌、产物清单与关键边界的速览入口；供需求/设计阶段使用，逐项参数、配置方法与示例指向配置指南对应章节。
3. [生成器配置指南](..\mybatis-generator-core\src\main\resources\docs\generator-config-guide.md)  
   用途：配置、DTD 版本与生成行为说明（详细指南）；§5.0 指引设计/开发阶段由生成 Entity 类和字段注解读取表列映射；第 20 章为约定驱动实践索引，§20.4 以 tmri_v4 消费配置串联需求、配置、生成物、业务扩展与验收；§7.2.3 为容器、顺序及多列布局建议，§7.3.1 为文本控件阈值与设计影响，§13.5.1 为分类/业务字典引用选项及中文展示转换实践。
4. [生成器再开发补充说明](generator-core-dev-spec.md)  
   用途：本仓库生成器实现补充约定；Service 签名工厂、接口/实现复用与元素装配遵守[主规范第 8.1 节](copilot-instructions.md#81-service-方法签名共用与实现装配规范)。
5. [非主线问题登记与后续处理规范](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\deferred-issue-management-standard.md)  
   用途：跨项目唯一权威规范；非主线问题只登记、不擅自处理。本项目 Java 模块台账按 `<module>\src\main\resources\docs\pending-issues.md` 定位，项目级或归属待定问题按 `docs\pending-issues.md` 定位；首次发现需登记的问题时创建，尚未创建的台账不作为已存在文档链接。
6. [实践指导的持续发现、确认与沉淀](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\ai-assisted-development-standard.md#15-实践指导的持续发现确认与沉淀)
   用途：`vgosoft-doc-spec` 维护的跨项目全局协作要求；任何阶段发现可复用指导，提案获许可后更新归属文档。
7. [约定驱动的字段与页面设计实践](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\project-design-standard.md#356-约定驱动的字段与页面设计实践)
   用途：`vgosoft-doc-spec` 维护的设计决策、字段矩阵和项目协作要求。
8. [面向生成的字段建模与默认值实例](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-core-db\src\main\resources\docs\database-authoritative-spec.md#45-面向生成与业务使用的字段设计实践)
   用途：`vgosoft-core-db` 维护的建模建议及 `apply_date` 分层默认值事实；生成器指南只回链该正文。
9. [字典预取排序与共用查询任务书](..\mybatis-generator-core\src\main\resources\docs\task\dict-prefetch-order-contract-task.md)
   用途：本次跨仓批准方案、G01/W01/V01 进度和验收证据；通用配置语义见配置指南 §13.5。
10. [版本库提交说明（commit message）规范](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\commit-message-standard.md)  
    用途：跨项目唯一权威规范，代码/文档/配置与生成产物改动收尾一律在回复末尾提供中文 commit message；改了就给，不附加验证前置条件，只提供 message 不执行提交，未验证时说明范围与原因。
11. [文档体系总览](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\doc-system-overview.md)  
    用途：四层文档结构与使用规则；建/改文档、索引、映射前按此确认分层与归属。
12. [全局文档编写提示词](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\global-authoring-prompt.md)  
    用途：编写任何 md 前必读；强制原则与分阶段最小读取流程。
13. [跨项目文档标准](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\cross-project-doc-standard.md)  
    用途：规范资产与能力/业务项目的文档分工；本仓库只维护 instructions、index、map 入口，不复制外部权威正文。
14. [最终命名规范](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\final-naming-standard.md)  
    用途：文档命名、路径引用与内容边界；外部权威正文保持原文件名，不做跨项目改名。
15. [跨项目工程体系与协作架构总览](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\cross-project-architecture-overview.md)  
    用途：跨项目唯一权威，项目工程构成公式、工程分层与职责、依赖方向、跨项目工作规则与易混淆点；跨工程改动或判断能力归属前必读。
16. [mybatis-generator-core 问题台账](..\mybatis-generator-core\src\main\resources\docs\pending-issues.md)
    用途：生成器模块项目问题台账；登记可变消费配置造成的历史测试基线漂移，不代表已批准修复。
17. [根实体字段数据库结构同步检查任务书](..\mybatis-generator-core\src\main\resources\docs\task\root-class-schema-sync-task.md)
    用途：记录默认结构同步触发范围、DDL 执行/失败语义、既有隔离回归及生成器权威说明与配置指南更新结果。
18. [业务表单数据链路与响应式优化任务书](D:\vscode\tmri-ui4\packages\framework\docs\task\form-data-reactivity-optimization-task.md)
    用途：tmri-ui4 主体维护的跨仓任务进度、Edit 模板 1.0.9 分支/竞态回归、批准单文件离线生成及验证边界；模板契约见生成器权威说明 §2.2。
19. [页面导航器生成任务书](..\mybatis-generator-core\src\main\resources\docs\task\page-navigator-generation-task.md)
    用途：记录 `generateHtml/pageNavigator` 跨生成器与 Framework 的确认范围、子任务依赖、进度及隔离验证证据。
20. [生成器配置指南业务契合度完善任务书](..\mybatis-generator-core\src\main\resources\docs\task\configuration-guide-business-fit-task.md)
    用途：记录基于 tmri_v4 采购需求与真实生成配置完善配置指南的范围、进度和文档验收边界；不表示业务配置已生成或功能已验收。
21. [表单 Markdown 字段支持任务书](..\mybatis-generator-core\src\main\resources\docs\task\markdown-form-field-task.md)
    用途：记录 `tagType="markdown"` 的枚举、DTD、Vue3 输出与 Framework `VgoMarkdownEditor` 的跨仓范围、子任务依赖与验证边界；不表示业务配置已生成或功能已验收。
22. [组合查询选项化生成支持任务书](..\mybatis-generator-core\src\main\resources\docs\task\query-condition-options-generation-task.md)
    用途：E01 归属任务书；`conditionOption` 配置/DTD/Parser、选项注解输出、Example 分组 Criterion 与 XML 嵌套 OR 装配、list/export 接线的子任务与验证边界；不表示业务配置已生成或功能已验收。
