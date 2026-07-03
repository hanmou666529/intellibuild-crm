ALTER TABLE crm_pipeline ADD COLUMN score int(11) DEFAULT NULL COMMENT 'AI 商机评分(0-100)';
ALTER TABLE crm_pipeline ADD COLUMN score_reason varchar(500) DEFAULT NULL COMMENT '评分理由';
ALTER TABLE crm_pipeline ADD COLUMN score_time datetime DEFAULT NULL COMMENT '评分时间';
