---
description: 修改生成逻辑、配置 DTD 或运行生成前的边界与一致性检查
trigger: model_decision
---

本仓是生成产物保护规则的唯一权威所在，改动前执行：

1. 先读 `.github/copilot-instructions.md`：§2.1 生成产物保护/覆盖/人工扩展统一规则、§7.1 配置 DTD 版本维护规则、§8 任务执行硬约束。
2. 核实实际 core 版本与 DTD 一致（`mybatis-generator-core\src\main\resources\docs\generator-config-guide.md` §1.1）。
3. 消费项目侧产物禁止下游手改；本仓修改生成逻辑后须给出"改动 + 重新生成"闭环说明。
4. 未经用户明确批准，不执行数据库更新、权限/菜单更新或部署。
