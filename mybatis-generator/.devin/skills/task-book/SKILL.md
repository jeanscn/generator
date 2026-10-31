---
name: task-book
description: 为多子任务工作创建 / 更新任务书并维护进度
allowed-tools:
  - read
  - edit
  - glob
  - exec
---

适用：需求可分解为多个子任务的所有阶段（权威：`D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\project-design-standard.md` §8.1）。

1. 先读上述规范 §8.1，遵守任务书、进度更新与完成清理要求。
2. 任务书放 `mybatis-generator-core\src\main\resources\docs\task\<主题>-task.md`。
3. 记录：任务清单/依赖、评审结论、跨仓改动、验收证据与状态；子任务推进时同步更新，不依赖聊天记录追踪进度。
4. 全部完成后按规范清理或归档，并在 `.github/doc-map.md` 登记 / 更新映射条目。
