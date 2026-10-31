---
description: 代码、文档或配置改动收尾时，在回复末尾提供中文 commit message
trigger: model_decision
---

每次改动收尾时，在回复末尾按规范提供可直接提交到版本仓库的中文 commit message：

- 改了就给，不附加验证前置条件；只提供 message 文本，不执行提交动作。
- 未验证时说明验证范围与原因；改动涉及多个仓库时按仓库分别给出。
- 唯一权威规范：`D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\commit-message-standard.md`
- 可使用 `/commit-msg` 技能生成。
