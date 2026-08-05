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
