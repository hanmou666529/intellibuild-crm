CREATE TABLE crm_contract_detail (
  detail_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
  contract_id  BIGINT       NOT NULL COMMENT '合同ID',
  product_id   BIGINT       NOT NULL COMMENT '产品ID',
  product_name VARCHAR(200) NOT NULL COMMENT '产品名称',
  product_price DECIMAL(12,2) NOT NULL COMMENT '单价',
  quantity     INT          NOT NULL COMMENT '数量',
  subtotal     DECIMAL(12,2) NOT NULL COMMENT '小计',
  remark       VARCHAR(500) DEFAULT NULL COMMENT '备注',
  del_flag     CHAR(1)      DEFAULT '0' COMMENT '删除标志',
  create_by    VARCHAR(64)  DEFAULT '' COMMENT '创建者',
  create_time  DATETIME     DEFAULT NULL COMMENT '创建时间',
  update_by    VARCHAR(64)  DEFAULT '' COMMENT '更新者',
  update_time  DATETIME     DEFAULT NULL COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同订单明细';
