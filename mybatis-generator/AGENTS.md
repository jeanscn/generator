# mybatis-generator 项目固定上下文（Devin 入口）

> 本文件是 Devin 会话常驻入口。项目固定上下文唯一正文为 `.github/copilot-instructions.md`，
> 本文件不复制其内容；规范变更只改唯一权威正文，不在这里维护副本。

## 1. 会话开始必读

1. `.github/copilot-instructions.md` —— 项目固定上下文（必读清单、外部引用锚点、项目规则）
2. `.github/doc-index.md` —— 文档索引
3. `.github/doc-map.md` —— 文档映射

## 2. 硬规则摘要（正文以引用文档为准）

- 全程中文：回复、注释、接口文档、commit message、报错说明一律中文；代码标识符与框架关键字保持英文。
- 非主线问题：只登记到归属台账 `pending-issues.md`，不擅自处理；见 `.devin/rules/non-mainline-issue.md`。
- 收尾：按 commit-message-standard 在回复末尾提供中文 commit message，只给文本不执行提交；见 `.devin/rules/commit-message.md`。
- 本仓是生成产物保护/覆盖/人工扩展规则的唯一权威所在（`.github/copilot-instructions.md` §2.1）；消费项目产物禁止下游手改。
- 修改生成逻辑或配置前核实 core 与 DTD 版本一致（§7.1、`generator-config-guide.md` §1.1）；见 `.devin/rules/codegen-boundary.md`。
- 技术栈：JDK / Maven；生成器核心在 `mybatis-generator-core`

## 3. Devin 按需能力

- 技能（`.devin/skills/`）：`/onboarding` `/commit-msg` `/register-issue` `/doc-locate` `/task-book` `/generator-config`
- 条件规则（`.devin/rules/`）：`non-mainline-issue`、`commit-message`、`codegen-boundary`

## 4. 环境

- Windows + Devin Desktop Local 模式；默认 shell 为 Git Bash，必要时可切 PowerShell。
- 构建：`mvn -pl <模块> -am compile`；测试：`mvn -pl <模块> test`。
- 外部规范与源码仓均为本机绝对路径，定位失败时按 `.github/copilot-instructions.md` 锚点节回退检索，不推测。
