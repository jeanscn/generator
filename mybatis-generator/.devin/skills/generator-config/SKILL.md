---
name: generator-config
description: 生成器 core/DTD 一致性核实与受控运行前检查
allowed-tools:
  - read
  - grep
  - glob
---

## 验收凭据来源与生成配置边界

AI 验收/测试所需的数据库、浏览器和 API 地址及用户名、密码/token 统一且仅从 `D:\java\achievable_connection_information.yml` 读取；文件缺项时停止相关验收并沟通，不得回退读取其他本机配置。此处关于 `generatorConfig.xml` 与本地 YAML 的说明仅指生成器实际运行的技术输入，不是 AI 验收凭据来源。

修改生成逻辑、配置 DTD 或运行生成验证之前：

1. 读 `.github/copilot-instructions.md` §2.1（生成产物保护/覆盖/人工扩展）与 §7.1（DTD 版本维护规则）。
2. 读 `mybatis-generator-core\src\main\resources\docs\generator-config-guide.md` §1.1，核实实际 core 版本、DTD 与输出覆盖策略一致。
3. `generatorConfig.xml` 数据库凭据作为生成器实际配置输入从本地 YAML 读取，不落库、不入仓、不写入会话。
4. 未经用户明确批准，不执行数据库更新、权限/菜单更新或部署；需要时先输出操作清单待确认。
