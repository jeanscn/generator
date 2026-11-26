# Markdown 编辑器默认上传接口与 htmlMarkDownDescriptor 任务书

> 主体归属：`mybatis-generator-core`
> 协作仓库：`vgosoft-basic / vgosoft-core-annotation`（`VueFormItemMeta` 注解）、`vgosoft-basic / vgosoft-web-vue`（`FormItem`）、`vgosoft-basic / vgosoft-web2`（`view_md_attachment` 表与生成配置）、`tmri-ui4 / @vgosoft/framework`（`VgoMarkdownEditor`、`RenderFormItem`）
> 需求负责人：用户
> 整体状态：已完成
> 上游任务：[表单 Markdown 字段支持任务书](markdown-form-field-task.md)
> 更新时间：2026-11-26

## 1. 目标与范围

1. `vgosoft-web2` 新增附件表 `view_md_attachment`（含 `bytes_` LONGBLOB 字段，rootClass=`AbstractTableFilesMgr`），生成器产出 upload/download 端口，作为 `VgoMarkdownEditor` 的默认上传兜底接口。
2. `mybatis-generator-config_1_8_0.dtd` 新增 `htmlMarkDownDescriptor(property*,validator*)` 独立描述符，解析阶段并入 `htmlElementDescriptor` 配置（tagType 固定为 markdown）。
3. 描述符属性尽量映射 vditor 原生选项：`height`/`minHeight`/`mode`/`cdn`/`placeholder`/`upload.url`；`maxHeight` 非 vditor 原生项，由组件 CSS 约束实现（用户明确要求）；`readonly`/`hideExpression`/`disabledExpression`/`tips` 属表单层机制。

## 2. 设计要点

- `readonly="true"`：解析进 `HtmlElementDescriptor.readonly`，列解析阶段把对应 javaProperty 并入 `htmlGeneratorConfiguration.readonlyFields`（沿用 `VueFormMeta.readonlyFields` 既有链路）。
- `uploadUrl`/`height`/`minHeight`/`maxHeight`/`mode`/`cdn`/`placeholder`：`HtmlElementDescriptor` 新字段 → `VueFormItemMeta` 新注解属性 → `FormItem.props`（`uploadUrl` 映射为 `props.upload.url`）。
- `VgoMarkdownEditor` 默认上传：`upload` 缺省或缺 `url` 时兜底为 `webcore/view-md-attachment-impl/upload`，`fieldName=file`，附 token 头，`format` 将 `ResponseResult.data.id` 映射为 `download/0/{id}` 链接；消费侧显式 `upload` 仍优先。
- `minHeight`/`maxHeight`：新组件 props，`minHeight` 透传 vditor 选项，`maxHeight` 经 CSS `max-height` 约束 `.vditor`。
- print 页面沿用既有拒绝语义（`generateHtml.type="print"` + markdown → 明确失败）。

## 3. 子任务与验收

| 编号 | 内容 | 仓库 | 状态 |
|---|---|---|---|
| G01 | DTD `htmlMarkDownDescriptor` 元素/属性/注释 + generateHtml·reusableElements 子元素 | mybatis-generator-core | 完成 |
| G02 | `HtmlElementDescriptor` 新字段 + Parser 并入 + `readonly`→`readonlyFields` | mybatis-generator-core | 完成 |
| G03 | `VueFormItemMetaDesc`/`VueHtmMetaAnnotationPlugin` 输出新注解属性 | mybatis-generator-core | 完成 |
| B01 | `VueFormItemMeta` 注解属性 + `FormItem` markdown→props 映射 | vgosoft-basic | 完成 |
| B02 | `view_md_attachment` 表配置（enableFileUpLoad、rootClass、record_id）与三方言 DDL | vgosoft-web2 | 完成 |
| F01 | `VgoMarkdownEditor` 默认上传兜底 + minHeight/maxHeight + `RenderFormItem` 透传 + 测试 | tmri-ui4 | 完成 |
| V01 | 编译/定向测试/文档同步；副作用清单（建表、再生成）待批准 | 全部 | 完成 |

## 4. 副作用清单（待按批次申请授权）

| 副作用 | 环境 | 状态 |
|---|---|---|
| 数据库 `tmri_v4` 执行 `view_md_attachment` 建表 DDL（mysql） | ln.vgosoft.com:3306 | 已执行（2026-11-26，jshell+JDBC，`bytes_ LONGBLOB` 核验通过） |
| `mvn install` 生成器 core/plugin 至本地仓 | 本机 | 已执行（退出码 0） |
| vgosoft-web2 执行 `mvn mybatis-generator:generate`（含结构同步 DDL 与 updateMenuData/updateModuleData DML） | 本机+DB | 已执行（BUILD SUCCESS；`webcore/view-md-attachment-impl` upload/download 端点生成，`vgosoft-web2` 编译通过，存量产物覆盖无差异） |

## 5. 进度记录

- 2026-11-26：建立任务书。
- 2026-11-26：G01-G03、B01、B02、F01 完成并验证：`mybatis-generator-core` 编译通过，`VueUiConventionAuditTest` 12 用例中本次新增 3 个 markdown 描述符用例全部通过（`pageNavigatorGeneratesMatchingEditAndDetailAnchorsWithComponentDefaults` 属既有默认值漂移失败，已登记 generator-core-ISSUE-0019）；`vgosoft-core-annotation`+`vgosoft-web-vue` 编译通过；`vgoMarkdownEditor/index.test.mjs` 7/7 通过，`RenderFormItem.test.mjs` markdown 2 用例通过（布局 harness 缺 `../validators` mock 属既有问题，已登记 framework-ISSUE-0052）；`tmri-ui4` 根 `vue-tsc` 退出码 0。
- 2026-11-26：副作用清单经批准后全部执行——`tmri_v4` 建表 `view_md_attachment`（`bytes_ LONGBLOB`，SHOW COLUMNS 核验）；`mybatis-generator` core+plugin `mvn install` 成功；`vgosoft-web2` 全量再生成 BUILD SUCCESS，`ViewMdAttachmentController`（`webcore/view-md-attachment-impl`）含 `POST upload`（`@RequestPart("file")`，BLOB 路径 `initBlobEntityFromMultipartFile`）与 `GET download/{type}/{id}`（`@PermitAll`），`vgosoft-web2 -am compile` 通过；生成 schema 三方言文件与手写 DDL 一致，存量产物覆盖无内容差异。
- 2026-11-27：应用运行时验收通过（宿主 tmri_v4 @ 127.0.0.1:8090，vgosoft-basic 三改动模块 `mvn install` 后重启生效）——`new-instance`/`upload`/`download`/`delete` 全链路：png 上传返回 `status:0`+`data.id`，DB 落 `record_id/mime_type/size/bytes_`；`download/0` 返回 `Content-Disposition:inline`、`download/1` 返回 `attachment`，字节与源文件一致；`DELETE {id}` 影响 1 行并清理测试数据；`viewmgr/webcore-view-manual-tips-impl/el-form-config` 返回 `tips_` 字段 `component:"markdown"`+默认占位。上传响应结构与前端 `defaultUploadFormat` 契约一致。
- 2026-11-27：端到端实证（htmlMarkDownDescriptor 双路径）——`vbiz_manual_doc.content_md` 改 `htmlMarkDownDescriptor uploadUrl="bizcore/vbiz-manual-attachment-impl/upload"`（独立附件表），`view_manual_tips.tips_` 改 `htmlMarkDownDescriptor`（无 uploadUrl，走默认 view_md_attachment）；组件 `defaultUploadFormat` 改为按生效上传 URL 推导 `{base}/download/{type}/{id}`，upload.url 相对路径补 API_URL 前缀，测试 8/8。再生成两模块并 install；`VbizManualDocEdit.vue` 恢复 `@VCF.overridable` 后被覆盖回标准模板（145 行，手工上传装配移除）。运行时验收：`contentMd` props.upload.url 正确下发、`tips` 无 upload 键；vbiz/upload 返回 `data.id`、download/0|1 字节一致、delete 影响 1 行；view_md 端点同验证，两表测试数据已清理（view_md_attachment=0 行，vbiz_manual_attachment 剩 3 行历史数据）。
- 2026-11-27（构建问题）：本地仓 `mybatis-generator-maven-plugin:1.8.0-SNAPSHOT` 胖 jar 内嵌 11-26 旧版 core（`target/classes` 混入 core 资源），导致 `htmlMarkDownDescriptor` 解析失败；`mvn -pl mybatis-generator-maven-plugin -am clean install -o` 重装后内嵌 DTD 更新，恢复正常。
- 2026-11-27（清理）：删除孤儿文件 `vbizmanualdoc/manualDocUpload.ts`/`manualDocUpload.test.mjs`；`PrivateUseFormHooks.ts` 移除 `updateManualDocUploadExtraData`/`TMarkdownFormItemRef` 并恢复 `@VCF.overridable`（回到生成模板形态）。遗留说明：标准编辑页上传不再携带 `recordId`/`type_`/`accept`/草稿前置校验——`record_id`/`type_` 在 `vbiz_manual_attachment` 为可空列，URL 引用即完成关联；如需恢复附件-文档归属可在描述符/组件层扩展 extraData 绑定。
