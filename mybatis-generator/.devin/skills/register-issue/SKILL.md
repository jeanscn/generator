---
name: register-issue
description: 将非主线问题登记到归属台账 pending-issues.md
argument-hint: "<问题描述>"
allowed-tools:
  - read
  - edit
  - glob
---

1. 读 `D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\deferred-issue-management-standard.md`，分级与格式以其为准。
2. 确定归属：模块问题登记到 `模块\src\main\resources\docs\pending-issues.md`；项目级或归属待定问题用 `docs\pending-issues.md`。
3. 台账已存在则追加条目；首次发现需登记的问题才创建文件，不建空台账、不写虚构链接。
4. 只登记事实与后续处理建议，不构成修复授权；登记后在回复中向用户说明登记位置与条目编号。
