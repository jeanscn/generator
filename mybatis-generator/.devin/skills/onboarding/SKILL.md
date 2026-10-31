---
name: onboarding
description: 新会话或新开发阶段的项目最小必读与上下文自检
allowed-tools:
  - read
  - grep
  - glob
---

按以下步骤完成项目上下文装载，输出"已读清单"：

1. 读 `.github/copilot-instructions.md` 的必读清单节（"先读什么"/"每次会话必读"），结合当前任务阶段，按
   `D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\ai-assisted-development-standard.md` 的阶段导航定位最小必读集，不逐条全读外部正文。
2. 读 `.github/doc-index.md`、`.github/doc-map.md` 中与当前任务相关的条目。
3. 任务涉及持久化实现、生成配置或生成产物扩展时，必须先读评审门禁（ai-assisted-development-standard §1.1）。
4. 输出：本任务适用的必读文档清单、已读/未读状态、命中的边界规则（生成产物 / 台账 / commit message）。
