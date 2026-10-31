---
name: commit-msg
description: 按规范为当前改动生成中文 commit message（只输出文本不提交）
allowed-tools:
  - read
  - exec
  - grep
permissions:
  allow:
    - Exec(git status)
    - Exec(git diff)
    - Exec(git log)
    - Exec(svn status)
    - Exec(svn diff)
    - Exec(svn log)
---

1. 读唯一权威规范 `D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\commit-message-standard.md`。
2. 识别版本库类型：有 `.git` 用 `git status` / `git diff`（staged 与 unstaged 都看）+ `git log -5 --oneline`；有 `.svn` 用 `svn status` / `svn diff` + `svn log -l 5`，对照本仓提交风格。
3. 按规范格式输出中文 commit message：聚焦"为什么改"而非罗列文件；未验证的改动标注范围与原因。
4. 只输出 message 文本，不执行任何提交或版本库写操作；改动跨多个仓库时按仓库分别给出。
