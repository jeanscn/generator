# MyBatis Generator Core 文档索引

> 本项目会话入口位于 `.github\copilot-instructions.md`；每次会话必读本文、文档映射及其中引用的跨项目规范。

1. [生成器权威说明](..\mybatis-generator-core\src\main\resources\docs\generator-authoritative-spec.md)  
   用途：生成器能力、输入输出、扩展点与限制；§2.1.1 说明 Entity 的 `@TableMeta`/`@ColumnMeta` 元数据契约及适用边界。
2. [生成器配置指南](..\mybatis-generator-core\src\main\resources\docs\generator-config-guide.md)  
   用途：配置、DTD 版本与生成行为说明；§5.0 指引设计/开发阶段由生成 Entity 类和字段注解读取表列映射；第 20 章为约定驱动实践索引，§7.2.3 为容器、顺序及多列布局建议，§7.3.1 为文本控件阈值与设计影响，§13.5.1 为分类/业务字典引用选项及中文展示转换实践。
3. [生成器再开发补充说明](generator-core-dev-spec.md)  
   用途：本仓库生成器实现补充约定；Service 签名工厂、接口/实现复用与元素装配遵守[主规范第 8.1 节](copilot-instructions.md#81-service-方法签名共用与实现装配规范)。
4. [非主线问题登记与后续处理规范](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\deferred-issue-management-standard.md)  
   用途：跨项目唯一权威规范；非主线问题只登记、不擅自处理。本项目 Java 模块台账按 `<module>\src\main\resources\docs\pending-issues.md` 定位，项目级或归属待定问题按 `docs\pending-issues.md` 定位；首次发现需登记的问题时创建，尚未创建的台账不作为已存在文档链接。
5. [实践指导的持续发现、确认与沉淀](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\ai-assisted-development-standard.md#15-实践指导的持续发现确认与沉淀)
   用途：`vgosoft-doc-spec` 维护的跨项目全局协作要求；任何阶段发现可复用指导，提案获许可后更新归属文档。
6. [约定驱动的字段与页面设计实践](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\project-design-standard.md#356-约定驱动的字段与页面设计实践)
   用途：`vgosoft-doc-spec` 维护的设计决策、字段矩阵和项目协作要求。
7. [面向生成的字段建模与默认值实例](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-core-db\src\main\resources\docs\database-authoritative-spec.md#45-面向生成与业务使用的字段设计实践)
   用途：`vgosoft-core-db` 维护的建模建议及 `apply_date` 分层默认值事实；生成器指南只回链该正文。
8. [字典预取排序与共用查询任务书](..\mybatis-generator-core\src\main\resources\docs\task\dict-prefetch-order-contract-task.md)
   用途：本次跨仓批准方案、G01/W01/V01 进度和验收证据；通用配置语义见配置指南 §13.5。
9. [版本库提交说明（commit message）规范](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\commit-message-standard.md)  
   用途：跨项目唯一权威规范，代码/文档/配置与生成产物改动收尾一律在回复末尾提供中文 commit message；改了就给，不附加验证前置条件，只提供 message 不执行提交，未验证时说明范围与原因。
10. [文档体系总览](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\doc-system-overview.md)  
    用途：四层文档结构与使用规则；建/改文档、索引、映射前按此确认分层与归属。
11. [全局文档编写提示词](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\global-authoring-prompt.md)  
    用途：编写任何 md 前必读；强制原则与分阶段最小读取流程。
12. [跨项目文档标准](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\cross-project-doc-standard.md)  
    用途：规范资产与能力/业务项目的文档分工；本仓库只维护 instructions、index、map 入口，不复制外部权威正文。
13. [最终命名规范](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\final-naming-standard.md)  
    用途：文档命名、路径引用与内容边界；外部权威正文保持原文件名，不做跨项目改名。
14. [跨项目工程体系与协作架构总览](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\cross-project-architecture-overview.md)  
    用途：跨项目唯一权威，项目工程构成公式、工程分层与职责、依赖方向、跨项目工作规则与易混淆点；跨工程改动或判断能力归属前必读。
