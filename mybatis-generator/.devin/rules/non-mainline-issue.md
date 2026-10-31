---
description: 发现与任务主线无关的问题时，只登记到归属模块 pending-issues.md，不擅自处理
trigger: model_decision
---

发现非主线问题（bug、坏味道、配置隐患、规范违背等）时：

1. 不擅自修复、不扩大任务范围；继续操作可能造成即时风险时，暂停受影响操作并与用户沟通，不代表获准修复。
2. 登记到归属模块的 `模块\src\main\resources\docs\pending-issues.md`；项目级或归属待定问题登记到 `docs\pending-issues.md`；首次发现需登记的问题才创建文件，不建空台账。
3. 登记格式、分级与后续处理流程以唯一权威规范为准：`D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\deferred-issue-management-standard.md`
4. 可使用 `/register-issue` 技能执行登记。
