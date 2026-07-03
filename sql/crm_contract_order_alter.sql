ALTER TABLE crm_contract
  ADD COLUMN approval_count    INT       DEFAULT 0 COMMENT '累计审批通过次数',
  ADD COLUMN last_approval_time DATETIME DEFAULT NULL COMMENT '最后一次审批通过时间';

ALTER TABLE crm_order
  ADD COLUMN source VARCHAR(20) DEFAULT 'manual' COMMENT '订单来源: manual-手动创建, contract-合同生成';
