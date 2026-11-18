# 页面导航器生成任务书

> 主体归属：`mybatis-generator-core`；协作：`mpmm-ui / @vgosoft/framework`。
> 整体状态：原任务已完成；进度：3/3；多页面覆盖修复离线验证完成，业务重新生成待另行确认；更新时间：2026-11-18。

## 1. 范围与确认

用户确认在 `generateHtml` 下增加 `pageNavigator`，以逗号分隔的数据库列名生成 `PageNavItem`，并在对应表单字段外层增加匹配锚点。实施范围限当前生成器 core `1.8.0` 与 mpmm-ui Framework；保留并完善用户已添加的 `mybatis-generator-config_1_8_0.dtd` 初稿，不改历史 DTD。

- 仅支持 editable/viewOnly；print 页面配置导航器时明确失败。
- `columns` 必填；每项生成 `id="nav-<实际列名>"`、列注释作为 `title`（无注释时回退列名）、`level=1`。
- `title` 默认空字符串，`trigger` 默认 `auto`，`placement` 默认 `right`；其余标量属性映射到 `VgoPageNav`。`items` 和 `activeId` 不由 XML 独立配置。
- 无效、重复、未知或静态上未渲染的导航列必须明确失败；运行时条件隐藏的字段同步从导航列表移除。
- 不连接数据库、不执行业务模块再生成、不安装或发布工件、不修改业务生成产物。

依据：[生成器指令](..\..\..\..\..\..\.github\copilot-instructions.md)、[生成器权威说明](..\generator-authoritative-spec.md)、[生成器配置指南](..\generator-config-guide.md)、[设计规范 §3.1.1、§6.1、§6.2、§8.1](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\project-design-standard.md)、[全局开发过程 §1.3](D:\java\idea_projs0\trunk\vgosoft-basic\vgosoft-doc-spec\src\main\resources\docs\ai-assisted-development-standard.md#13-生成基础能力与业务重写的实施门禁)、[Framework 组件复用指南 §45](D:\vscode\mpmm-ui\packages\framework\docs\components-reuse-capability-overview.md#45-vgopagenav)。

## 2. 子任务

| 编号 | 内容 | 依赖 | 状态 | 验收条件及结果 |
|---|---|---|---|---|
| G01 | 完成 pageNavigator DTD、配置解析/校验、Vue3 编辑/详情模板输出及生成器回归 | 无 | 完成 | `VueUiConventionAuditTest` 通过；覆盖默认值和全部可配置属性、editable/edit detail 输出、print/重复/空数值/未知列/静态隐藏/布局排除拒绝；使用只读元数据替身，未连接数据库或执行业务生成 |
| F01 | Framework 表单接收导航配置、渲染字段锚点并同步过滤运行时隐藏字段 | G01 | 完成 | `RenderFormItem.test.mjs` 19/19 通过；覆盖锚点映射、静态未渲染字段过滤及字段/布局条件隐藏恢复；Framework Vite 构建通过 |
| V01 | 更新生成器/Framework 文档并完成跨仓定向验证与归档 | G01、F01 | 完成（类型检查受既有诊断阻断） | 已同步配置指南、生成器权威说明、Framework 组件契约及本任务书。`npm --prefix packages/framework run typecheck` 因仓库既有 TypeScript 诊断失败（包含 `LayoutItems.vue` 未标注回调参数）；新增导航辅助逻辑、VgoForm 新属性及 VgoPageNav 变更未见诊断，未扩大修复无关错误。 |

## 3. 临时资产与清理

目前未创建一次性脚本或临时资产。测试输出限定在各自仓库的标准构建目录；不删除现存构建产物或其他任务资产。

## 4. 验证记录

- 容器外预留方案最终复验：Framework 两个定向测试文件共 44/44 通过，Vite 构建通过；编辑弹窗与详情抽屉的左右预留、隐藏/卸载恢复、展开不挤压、窗口调整、滚动固定及锚点跳转均已实测，修复并复验首次拖拽产生 transform 时的定位偏移。全量类型检查初次复验输出与既有基线相同的 206 行诊断，导航目录及 `vgoForm/index.vue` 无诊断；补齐祖先样式监听后，最终复验工具两次以 `-1073740791` 异常退出且无诊断输出，未获得最终完整类型检查结果，不能宣称全量类型检查通过。本轮生成器仅调整说明与 DTD 注释，未重跑 Java 构建或生成器回归。
- 生成器：`mvn -q -pl mybatis-generator-core -Dtest=VueUiConventionAuditTest -Dsurefire.failIfNoSpecifiedTests=false test` 通过；其中包含生成模板约定审计与页面导航生成回归。
- Framework：`node --test packages/framework/src/components/vgoForm/items/RenderFormItem.test.mjs` 通过（19/19）；`packages/framework` 的 Vite 生产构建通过。
- 类型检查：Framework 全量 `vue-tsc --noEmit` 仍失败，诊断分布于既有 BPMN、表单及工具文件（包含 `LayoutItems.vue` 未标注回调参数）；新增导航辅助逻辑、VgoForm 新属性及 VgoPageNav 变更未出现诊断。未修复与本需求无关的既有类型错误。
- 构建/生成均使用本地或只读元数据替身；未连接业务数据库、未运行消费者模块生成、未安装或发布工件。
- 浏览器验收修复（2026-11-16）：用户反馈弹窗内导航按视口定位且点击不滚动。Framework 改为由 `VgoForm` 自动解析表单实际滚动容器，`VgoPageNav` 对齐局部容器可视区并兼容拖拽弹窗 transform。本地开发服务（sysadmin）中“其他会议纪要”编辑弹窗与详情抽屉已验证：导航位于内容区内，点击后目标滚动至容器顶部或底部兜底，激活项同步；拖拽后位置仍正确。定向测试 20/20 通过，Vite 构建通过。
- 贴边固定调整（2026-11-16）：按用户确认，局部滚动容器模式下导航水平方向改为贴宿主外框（弹窗/抽屉/页面主体）边缘并覆盖内容区滚动条，垂直方向仍对齐滚动容器可视区，内容滚动时导航固定不动；`edge` 默认值由 14 调整为 8（Framework 组件与生成器 `PageNavigatorConfiguration` 同步）。浏览器验证：编辑弹窗导航右缘距弹窗外框 14px（现有生成产物仍显式 `edge: 14`，重新生成后为 8），超出内容区右缘覆盖滚动条，内容滚动 200px 后导航纵向位置不变；详情抽屉导航右缘距抽屉外框 14px。生成器回归、Framework 定向测试 20/20 与 Vite 构建通过。
- 滚动容器外预留调整（2026-11-16）：按用户进一步确认，以上“覆盖滚动条”方式替换为实际滚动容器在导航侧连同滚动条一起内缩 36px，导航移至滚动容器外，撤销此前误加的表单内部留白；宿主外框、标题栏和底部操作区不变，展开面板不再次挤压容器，隐藏或卸载时恢复原边距。浏览器实测详情抽屉宽度由 1170px 减至 1134px，左右切换均准确预留 36px，隐藏后恢复 1170px；调整视口后预留区仍为 36px，滚动 150px 时导航固定。编辑弹窗滚动区宽度由 1138px 减至 1102px，滚动 200px 时导航固定；首次拖拽产生 transform 后导航仍对齐预留区，锚点跳转及激活项正确。仅更新 Framework 实现与两仓相关说明，不改变生成器声明、默认值或生成逻辑，不重新生成业务页面、不保存业务数据。

## 5. 多页面配置覆盖导航修复（2026-11-18）

- 消费现象：tmri_v4 的 `tmri_requirements`、`tmri_bid_docs`、`tmri_supplier_confirm` 均在 editable 配置中声明导航，后接 print 配置；tmri-ui4 对应六个 Edit/Detail 组件均未包含 `pageNavigator` 绑定。
- 根因：`Context.generateFiles` 对每个 `generateHtml` 调用插件，`Vue3FormFilesPlugin` 原先每次都输出同名 Edit/Detail，后置 print 配置会生成无导航版本。离线回归已复现同名 Edit 组件数量为 2。
- 授权：用户确认仅修复生成器并做离线回归；不连接数据库、不安装或发布工件、不执行业务重新生成。业务三表重新生成范围另行确认。
- 实现：core `1.8.0-SNAPSHOT`、当前 DTD `1.8.0` 不变；保留原配置校验，仅让与 VO 表单元数据一致的配置输出 Edit/Detail。未修改 Framework 或业务生成产物。
- 回归：在既有 `VueUiConventionAuditTest` 增加 editable 后接 print、非默认 print 在前而默认 editable 在后两种顺序，断言每个 Edit/Detail 仅生成一次且包含导航绑定、导航项和字段锚点映射。使用只读元数据替身，禁止写盘生成。
- 验证状态：修复前新增两例均因重复组件失败（退出码 1）；修复后 `mvn -o -q -pl mybatis-generator-core -Dtest=VueUiConventionAuditTest -Dsurefire.failIfNoSpecifiedTests=false test` 为 9 项中 8 项通过、1 项失败（退出码 1），唯一失败是已登记的 `pageNavigatorGeneratesMatchingEditAndDetailAnchorsWithComponentDefaults` 默认宽度/层级断言漂移（`generator-core-ISSUE-0012`），本轮未修改。显式选择其余 8 个测试方法重跑，8/8 通过、失败/错误/跳过均为 0（退出码 0）；包含新增两例、配置属性映射、无效配置拒绝、UI 约定、详情间距及 Markdown 两例。`git diff --check` 通过。
- 验收边界：以上仅为生成器离线回归，不代表整体测试类通过或业务页面已修复上线；未连接业务数据库、未安装/发布工件、未改写六个业务组件，三表真实重新生成与浏览器导航显示/跳转尚未执行。
