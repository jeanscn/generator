-- MySQL 8.0.16+ initial schema. Run only when all eight tables are absent.
-- DDL implicitly commits. Never use this script to overwrite an existing schema.
SET NAMES utf8mb4;

CREATE TABLE `sensitive_category` (
  `id_` VARCHAR(36) NOT NULL COMMENT '编码（系统生成的唯一标识）',
  `delete_flag` INT NOT NULL DEFAULT 0 COMMENT '删除状态（0：有效，1：删除）',
  `created_` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（系统自动生成）',
  `modified_` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间（系统自动维护）',
  `created_id` VARCHAR(36) NULL COMMENT '创建人（关联用户标识，系统自动填充）',
  `modified_id` VARCHAR(36) NULL COMMENT '修改人（关联用户标识，系统自动填充）',
  `version_` INT NOT NULL DEFAULT 1 COMMENT '数据版本（乐观锁版本，初始为1，更新时递增）',
  `sort_` INT NOT NULL COMMENT '排序（分类显示顺序，实体初始值为0）',
  `state_` INT NOT NULL DEFAULT 1 COMMENT '启用状态（1：启用，0：停用）',
  `code_` VARCHAR(64) NOT NULL COMMENT '分类编码',
  `code_hash_` CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '分类编码摘要（NFC规范化编码的UTF-8 SHA-256小写十六进制，删除时改为墓碑摘要）',
  `name_` VARCHAR(128) NOT NULL COMMENT '分类名称',
  `notes_` VARCHAR(1000) NULL COMMENT '备注',
  PRIMARY KEY (`id_`),
  UNIQUE KEY `uk_sensitive_category_code` (`code_hash_`),
  KEY `idx_sensitive_category_state` (`delete_flag`, `state_`, `sort_`),
  CONSTRAINT `ck_sensitive_category_flags` CHECK (`delete_flag` IN (0,1) AND `state_` IN (0,1)),
  CONSTRAINT `ck_sensitive_category_version` CHECK (`version_` >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='敏感词分类管理（维护敏感词分类、启用状态和显示顺序）';

CREATE TABLE `sensitive_word` (
  `id_` VARCHAR(36) NOT NULL COMMENT '编码（系统生成的唯一标识）',
  `delete_flag` INT NOT NULL DEFAULT 0 COMMENT '删除状态（0：有效，1：删除）',
  `created_` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（系统自动生成）',
  `modified_` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间（系统自动维护）',
  `created_id` VARCHAR(36) NULL COMMENT '创建人（关联用户标识，系统自动填充）',
  `modified_id` VARCHAR(36) NULL COMMENT '修改人（关联用户标识，系统自动填充）',
  `version_` INT NOT NULL DEFAULT 1 COMMENT '数据版本（乐观锁版本，初始为1，更新时递增）',
  `state_` INT NOT NULL DEFAULT 1 COMMENT '启用状态（1：启用，0：停用）',
  `word_` VARCHAR(1024) NOT NULL COMMENT '敏感词（待匹配词面，应用上限为256个UTF-16单元）',
  `word_hash_` CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '敏感词摘要（NFC规范化词面的UTF-8 SHA-256小写十六进制，删除时改为墓碑摘要）',
  `category_id` VARCHAR(36) NULL COMMENT '所属分类（关联敏感词分类标识，空值表示未分类，不建物理外键）',
  `strategy_` VARCHAR(16) COLLATE utf8mb4_bin NULL COMMENT '处理策略（空值：继承全局，BLOCK：阻断，CONFIRM：确认后放行，REPLACE：替换后保存，WARN：告警放行）',
  `notes_` VARCHAR(1000) NULL COMMENT '备注',
  `category_cascade_disabled_` INT NOT NULL DEFAULT 0 COMMENT '分类级联停用来源标记（0：无，1：由分类停用级联设置）',
  PRIMARY KEY (`id_`),
  UNIQUE KEY `uk_sensitive_word_hash` (`word_hash_`),
  KEY `idx_sensitive_word_category` (`category_id`, `delete_flag`, `state_`),
  KEY `idx_sensitive_word_state` (`delete_flag`, `state_`),
  CONSTRAINT `ck_sensitive_word_flags` CHECK (`delete_flag` IN (0,1) AND `state_` IN (0,1)),
  CONSTRAINT `ck_sensitive_word_version` CHECK (`version_` >= 1),
  CONSTRAINT `ck_sensitive_word_strategy` CHECK (`strategy_` IS NULL OR `strategy_` IN ('BLOCK','CONFIRM','REPLACE','WARN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='敏感词管理（维护词条、所属分类、启用状态及命中处理策略）';

CREATE TABLE `sensitive_rule_revision` (
  `id_` VARCHAR(36) NOT NULL COMMENT '编码（固定为00000000-0000-0000-0000-000000000001，仅保留单行）',
  `created_` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（系统自动生成）',
  `modified_` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间（规则变更时由系统自动维护）',
  `created_id` VARCHAR(36) NULL COMMENT '创建人（关联用户标识，系统初始化时可为空）',
  `modified_id` VARCHAR(36) NULL COMMENT '修改人（关联规则变更操作者标识）',
  `revision_` BIGINT NOT NULL DEFAULT 1 COMMENT '规则版本（初始为1，与有效规则变更同事务单调递增）',
  PRIMARY KEY (`id_`),
  CONSTRAINT `ck_sensitive_revision_id` CHECK (`id_` = '00000000-0000-0000-0000-000000000001'),
  CONSTRAINT `ck_sensitive_revision_value` CHECK (`revision_` >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='敏感规则版本管理（维护平台共享词库及规则配置的发布版本）';

CREATE TABLE `sensitive_confirmation` (
  `id_` VARCHAR(36) NOT NULL COMMENT '编码（系统生成的确认凭证唯一标识）',
  `created_` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（系统自动生成）',
  `modified_` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间（系统自动维护）',
  `tenant_id` VARCHAR(36) NULL COMMENT '所属租户（从受信上下文绑定租户标识，非租户环境为空）',
  `token_hash_` CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '凭证摘要（确认凭证的SHA-256小写十六进制，不保存凭证明文）',
  `token_nonce_` CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '随机数（256位安全随机nonce的小写十六进制）',
  `signing_key_id_` VARCHAR(64) NOT NULL COMMENT '签名密钥版本（外部密钥版本标识，不保存密钥内容）',
  `user_id` VARCHAR(36) NOT NULL COMMENT '凭证持有人（关联受信用户标识）',
  `principal_scope_hash_` CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '身份范围摘要（规范化用户和租户的SHA-256，空租户使用固定空值标记）',
  `operation_key_` VARCHAR(256) NOT NULL COMMENT '操作标识（服务端已登记的稳定业务写入操作键）',
  `operation_hash_` CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '操作摘要（操作标识的UTF-8 SHA-256小写十六进制）',
  `payload_hash_` CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '受检内容摘要（确定性编码入参的SHA-256，不保存原文）',
  `rule_revision_` BIGINT NOT NULL COMMENT '规则版本（签发确认凭证时绑定的规则版本）',
  `issued_` TIMESTAMP NOT NULL COMMENT '签发时间（服务端赋值，精度：秒）',
  `expires_` TIMESTAMP NOT NULL COMMENT '到期时间（晚于签发时间，精度：秒）',
  `confirmation_status_` VARCHAR(16) COLLATE utf8mb4_bin NOT NULL COMMENT '确认状态（ISSUED：已签发，CONSUMED：已消费，EXPIRED：已过期）',
  `request_key_hash_` CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '幂等请求摘要（UUID格式Idempotency-Key的SHA-256小写十六进制）',
  `result_ref_` VARCHAR(64) NULL COMMENT '完成结果引用（用于安全识别既有完成结果，不保存原响应全文）',
  PRIMARY KEY (`id_`),
  UNIQUE KEY `uk_sensitive_confirm_token` (`token_hash_`),
  UNIQUE KEY `uk_sensitive_confirm_request` (`principal_scope_hash_`, `operation_hash_`, `request_key_hash_`),
  KEY `idx_sensitive_confirm_expiry` (`confirmation_status_`, `expires_`),
  CONSTRAINT `ck_sensitive_confirm_status` CHECK (`confirmation_status_` IN ('ISSUED','CONSUMED','EXPIRED')),
  CONSTRAINT `ck_sensitive_confirm_revision` CHECK (`rule_revision_` >= 1),
  CONSTRAINT `ck_sensitive_confirm_expiry` CHECK (`expires_` > `issued_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='敏感内容确认管理（保存绑定用户、内容和规则版本的一次性确认凭证）';

CREATE TABLE `sensitive_import_preview` (
  `id_` VARCHAR(36) NOT NULL COMMENT '编码（系统生成的导入预览唯一标识）',
  `created_` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（系统自动生成）',
  `modified_` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间（系统自动维护）',
  `tenant_id` VARCHAR(36) NULL COMMENT '所属租户（从受信上下文绑定租户标识，非租户环境为空）',
  `user_id` VARCHAR(36) NOT NULL COMMENT '预览持有人（关联受信用户标识）',
  `file_hash_` CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '文件摘要（上传文件的SHA-256，不保存文件全文）',
  `rows_hash_` CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '导入内容摘要（规范化导入行的SHA-256，不保存词条全文）',
  `rule_revision_` BIGINT NOT NULL COMMENT '规则版本（导入预览校验时使用的规则版本）',
  `row_count_` INT NOT NULL COMMENT '预览行数（实际导入行数，非负且不超过导入上限）',
  `expires_` TIMESTAMP NOT NULL COMMENT '到期时间（服务端计算的预览有效期截止时间，精度：秒）',
  `import_status_` VARCHAR(16) COLLATE utf8mb4_bin NOT NULL COMMENT '导入状态（READY：待提交，COMMITTED：已导入，EXPIRED：已过期）',
  PRIMARY KEY (`id_`),
  KEY `idx_sensitive_preview_owner` (`user_id`, `expires_`),
  KEY `idx_sensitive_preview_expiry` (`import_status_`, `expires_`),
  CONSTRAINT `ck_sensitive_preview_status` CHECK (`import_status_` IN ('READY','COMMITTED','EXPIRED')),
  CONSTRAINT `ck_sensitive_preview_counts` CHECK (`row_count_` >= 0 AND `rule_revision_` >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='敏感词导入预览（保存导入校验摘要、持有人及提交状态，不保存上传全文）';

CREATE TABLE `sensitive_hit_log` (
  `id_` VARCHAR(36) NOT NULL COMMENT '编码（系统生成的审计记录唯一标识）',
  `created_` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（审计入库时系统自动生成）',
  `created_id` VARCHAR(36) NULL COMMENT '创建人（检测事件操作者标识，无用户的系统事件可为空）',
  `tenant_id` VARCHAR(36) NULL COMMENT '所属租户（检测事件的租户标识，非租户环境为空）',
  `request_id_` VARCHAR(36) NOT NULL COMMENT '检测事件标识（服务端生成的唯一关联键，用于审计幂等）',
  `operation_key_` VARCHAR(256) NOT NULL COMMENT '操作标识（已登记的业务写入或运行异常操作键）',
  `entity_type_` VARCHAR(256) NULL COMMENT '实体类型（本次受检业务实体的类型名称）',
  `entity_id_` VARCHAR(36) NULL COMMENT '业务记录（关联单条受检业务记录标识，批次可为空）',
  `rule_revision_` BIGINT NULL COMMENT '规则版本（本次检测已知版本，规则加载前异常为空）',
  `decision_` VARCHAR(32) COLLATE utf8mb4_bin NOT NULL COMMENT '检测决策（BLOCK：阻断，NEED_CONFIRM：待确认，ALLOW：允许，ERROR：检测异常，NO_HIT：未命中）',
  `outcome_` VARCHAR(32) COLLATE utf8mb4_bin NOT NULL COMMENT '处理结果（BLOCKED：已阻断，AWAITING_CONFIRMATION：等待确认，COMMITTED：已提交，FAILED：失败，ROLLED_BACK：已回滚，UNKNOWN：结果未知，NON_TRANSACTIONAL_COMPLETED：非事务完成，NO_HIT：未命中）',
  `occurred_` TIMESTAMP NOT NULL COMMENT '检测时间（实际事件发生时间，不以异步入库时间替代）',
  `details_truncated_` INT NOT NULL DEFAULT 0 COMMENT '明细是否截断（0：完整，1：截断）',
  PRIMARY KEY (`id_`),
  UNIQUE KEY `uk_sensitive_hit_request` (`request_id_`),
  KEY `idx_sensitive_hit_occurred` (`occurred_`, `id_`),
  CONSTRAINT `ck_sensitive_hit_decision` CHECK (`decision_` IN ('BLOCK','NEED_CONFIRM','ALLOW','ERROR','NO_HIT')),
  CONSTRAINT `ck_sensitive_hit_outcome` CHECK (`outcome_` IN ('BLOCKED','AWAITING_CONFIRMATION','COMMITTED','FAILED','ROLLED_BACK','UNKNOWN','NON_TRANSACTIONAL_COMPLETED','NO_HIT')),
  CONSTRAINT `ck_sensitive_hit_flags` CHECK (`details_truncated_` IN (0,1) AND (`rule_revision_` IS NULL OR `rule_revision_` >= 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='敏感内容检测审计（记录检测决策、处理结果及操作者，不保存原文）';

CREATE TABLE `sensitive_hit_log_detail` (
  `id_` VARCHAR(36) NOT NULL COMMENT '编码（系统生成的审计明细唯一标识）',
  `created_` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（明细入库时系统自动生成）',
  `request_id_` VARCHAR(36) NOT NULL COMMENT '检测事件标识（关联审计主记录的检测事件标识，不建物理外键）',
  `detail_seq_` INT NOT NULL COMMENT '明细序号（同一检测事件内的稳定序号，从0开始）',
  `batch_index_` INT NOT NULL COMMENT '批次索引（受检记录在批次中的位置，从0开始，单条为0）',
  `field_path_` VARCHAR(512) NOT NULL COMMENT '字段路径（命中敏感词的受检实体字段路径）',
  `field_label_` VARCHAR(256) NULL COMMENT '字段名称（受检字段的页面展示名称）',
  `word_id_` VARCHAR(36) NULL COMMENT '命中词条（关联原敏感词标识，历史记录不级联删除）',
  `word_hash_` CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '命中词摘要（命中时规范化词面的SHA-256，不保存原文）',
  `strategy_` VARCHAR(16) COLLATE utf8mb4_bin NOT NULL COMMENT '处理策略（最终策略，BLOCK：阻断，CONFIRM：确认后放行，REPLACE：替换后保存，WARN：告警放行）',
  `start_offset_` INT NOT NULL COMMENT '命中起点（原文UTF-16单元偏移，左闭，从0开始）',
  `end_offset_` INT NOT NULL COMMENT '命中终点（原文UTF-16单元偏移，右开，必须大于起点）',
  PRIMARY KEY (`id_`),
  UNIQUE KEY `uk_sensitive_detail_seq` (`request_id_`, `detail_seq_`),
  KEY `idx_sensitive_detail_batch` (`request_id_`, `batch_index_`),
  CONSTRAINT `ck_sensitive_detail_positions` CHECK (`detail_seq_` >= 0 AND `batch_index_` >= 0 AND `start_offset_` >= 0 AND `end_offset_` > `start_offset_`),
  CONSTRAINT `ck_sensitive_detail_strategy` CHECK (`strategy_` IN ('BLOCK','CONFIRM','REPLACE','WARN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='敏感词命中明细（记录命中字段、词条摘要、处理策略及原文位置）';

CREATE TABLE `sensitive_audit_outbox` (
  `id_` VARCHAR(36) NOT NULL COMMENT '编码（系统生成的审计消息唯一标识）',
  `created_` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（审计消息事务入队时系统自动生成）',
  `modified_` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间（投递状态变更时系统自动维护）',
  `request_id_` VARCHAR(36) NOT NULL COMMENT '检测事件标识（关联业务写入成功的检测事件）',
  `event_type_` VARCHAR(32) COLLATE utf8mb4_bin NOT NULL COMMENT '事件类型（SENSITIVE_WRITE_COMMITTED：敏感内容受检写入已提交）',
  `payload_` MEDIUMTEXT NOT NULL COMMENT '审计消息内容（有版本的JSON文本，仅保存允许的元数据，UTF-8上限为1MiB）',
  `processed_` INT NOT NULL DEFAULT 0 COMMENT '投递是否完成（0：待投递，1：完成，失败保持0并告警）',
  PRIMARY KEY (`id_`),
  UNIQUE KEY `uk_sensitive_outbox_request` (`request_id_`, `event_type_`),
  KEY `idx_sensitive_outbox_pending` (`processed_`, `created_`, `id_`),
  CONSTRAINT `ck_sensitive_outbox_processed` CHECK (`processed_` IN (0,1)),
  CONSTRAINT `ck_sensitive_outbox_type` CHECK (`event_type_` = 'SENSITIVE_WRITE_COMMITTED'),
  CONSTRAINT `ck_sensitive_outbox_payload` CHECK (OCTET_LENGTH(`payload_`) <= 1048576)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='敏感检测审计消息（与业务写入同事务入队，供审计幂等投递）';

INSERT INTO `sensitive_rule_revision` (`id_`, `revision_`)
VALUES ('00000000-0000-0000-0000-000000000001', 1);
