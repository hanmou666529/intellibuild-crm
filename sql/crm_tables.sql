-- =============================================
-- 智营CRM 业务表
-- =============================================

-- 1. 客户信息表
DROP TABLE IF EXISTS `crm_customer`;
CREATE TABLE `crm_customer` (
  `customer_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '客户ID',
  `customer_name` varchar(50) NOT NULL COMMENT '客户姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `sex` char(1) DEFAULT '0' COMMENT '性别（0未知 1男 2女）',
  `source` varchar(50) DEFAULT NULL COMMENT '客户来源（字典）',
  `level` varchar(50) DEFAULT NULL COMMENT '客户等级（字典）',
  `industry` varchar(50) DEFAULT NULL COMMENT '所属行业（字典）',
  `tags` varchar(500) DEFAULT NULL COMMENT '标签（逗号分隔）',
  `follow_status` char(1) DEFAULT '0' COMMENT '跟进状态（0待跟进 1跟进中 2已成交 3已流失）',
  `next_contact_time` datetime DEFAULT NULL COMMENT '下次联系时间',
  `belong_user_id` bigint(20) DEFAULT NULL COMMENT '归属人ID',
  `belong_dept_id` bigint(20) DEFAULT NULL COMMENT '归属部门ID',
  `is_pool` char(1) DEFAULT '0' COMMENT '是否公海客户（0否 1是）',
  `enter_pool_time` datetime DEFAULT NULL COMMENT '进入公海时间',
  `last_follow_time` datetime DEFAULT NULL COMMENT '最后跟进时间',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `company` varchar(200) DEFAULT NULL COMMENT '公司名称',
  `position` varchar(100) DEFAULT NULL COMMENT '职位',
  `wechat` varchar(100) DEFAULT NULL COMMENT '微信',
  `qq` varchar(20) DEFAULT NULL COMMENT 'QQ',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`customer_id`),
  KEY `idx_phone` (`phone`),
  KEY `idx_belong_user` (`belong_user_id`),
  KEY `idx_belong_dept` (`belong_dept_id`),
  KEY `idx_follow_status` (`follow_status`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='客户信息表';

-- 2. 跟进记录表
DROP TABLE IF EXISTS `crm_followup`;
CREATE TABLE `crm_followup` (
  `followup_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '跟进ID',
  `customer_id` bigint(20) NOT NULL COMMENT '客户ID',
  `contact_time` datetime DEFAULT NULL COMMENT '联系时间',
  `followup_mode` varchar(50) DEFAULT NULL COMMENT '跟进方式（字典：电话/上门/微信/邮件等）',
  `content` text COMMENT '跟进内容',
  `next_contact_time` datetime DEFAULT NULL COMMENT '下次联系时间',
  `is_effective` char(1) DEFAULT '1' COMMENT '是否有效跟进（1是 0否）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`followup_id`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='跟进记录表';

-- 3. 销售漏斗阶段表
DROP TABLE IF EXISTS `crm_pipeline`;
CREATE TABLE `crm_pipeline` (
  `pipeline_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '漏斗ID',
  `customer_id` bigint(20) DEFAULT NULL COMMENT '客户ID',
  `order_id` bigint(20) DEFAULT NULL COMMENT '关联订单ID',
  `stage` varchar(50) NOT NULL COMMENT '阶段（clue/intent/quote/deal/payment）',
  `amount` decimal(12,2) DEFAULT '0.00' COMMENT '预计金额',
  `probability` int(11) DEFAULT '0' COMMENT '成交概率（%）',
  `expected_close_date` datetime DEFAULT NULL COMMENT '预计成交日期',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`pipeline_id`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_stage` (`stage`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='销售漏斗表';

-- 4. 合同表
DROP TABLE IF EXISTS `crm_contract`;
CREATE TABLE `crm_contract` (
  `contract_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '合同ID',
  `contract_no` varchar(50) NOT NULL COMMENT '合同编号',
  `contract_name` varchar(200) NOT NULL COMMENT '合同名称',
  `customer_id` bigint(20) DEFAULT NULL COMMENT '客户ID',
  `order_id` bigint(20) DEFAULT NULL COMMENT '关联订单ID',
  `amount` decimal(12,2) DEFAULT '0.00' COMMENT '合同金额',
  `sign_date` datetime DEFAULT NULL COMMENT '签订日期',
  `start_date` datetime DEFAULT NULL COMMENT '生效日期',
  `end_date` datetime DEFAULT NULL COMMENT '到期日期',
  `status` char(1) DEFAULT '0' COMMENT '状态（0待审核 1已生效 2已完成 3已终止）',
  `attachment` varchar(500) DEFAULT NULL COMMENT '附件路径',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`contract_id`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='合同表';

-- 5. 订单表
DROP TABLE IF EXISTS `crm_order`;
CREATE TABLE `crm_order` (
  `order_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单编号',
  `customer_id` bigint(20) DEFAULT NULL COMMENT '客户ID',
  `contract_id` bigint(20) DEFAULT NULL COMMENT '关联合同ID',
  `total_amount` decimal(12,2) DEFAULT '0.00' COMMENT '订单总金额',
  `discount_amount` decimal(12,2) DEFAULT '0.00' COMMENT '折扣金额',
  `actual_amount` decimal(12,2) DEFAULT '0.00' COMMENT '实付金额',
  `status` char(1) DEFAULT '0' COMMENT '状态（0待付款 1已付款 2已完成 3已取消）',
  `paid_amount` decimal(12,2) DEFAULT '0.00' COMMENT '已回款金额',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`order_id`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 6. 订单明细表
DROP TABLE IF EXISTS `crm_order_item`;
CREATE TABLE `crm_order_item` (
  `item_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
  `product_id` bigint(20) DEFAULT NULL COMMENT '产品ID',
  `product_name` varchar(200) DEFAULT NULL COMMENT '产品名称',
  `product_price` decimal(12,2) DEFAULT '0.00' COMMENT '产品单价',
  `quantity` int(11) DEFAULT '1' COMMENT '数量',
  `subtotal` decimal(12,2) DEFAULT '0.00' COMMENT '小计',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`item_id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- 7. 回款计划表
DROP TABLE IF EXISTS `crm_payment_plan`;
CREATE TABLE `crm_payment_plan` (
  `plan_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '计划ID',
  `order_id` bigint(20) DEFAULT NULL COMMENT '关联订单ID',
  `contract_id` bigint(20) DEFAULT NULL COMMENT '关联合同ID',
  `plan_amount` decimal(12,2) DEFAULT '0.00' COMMENT '计划回款金额',
  `actual_amount` decimal(12,2) DEFAULT '0.00' COMMENT '实际回款金额',
  `plan_date` datetime DEFAULT NULL COMMENT '计划回款日期',
  `actual_date` datetime DEFAULT NULL COMMENT '实际回款日期',
  `status` char(1) DEFAULT '0' COMMENT '状态（0待回款 1已回款 2逾期）',
  `payment_method` varchar(50) DEFAULT NULL COMMENT '回款方式（字典）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`plan_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_plan_date` (`plan_date`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='回款计划表';

-- 8. 产品分类表
DROP TABLE IF EXISTS `crm_product_category`;
CREATE TABLE `crm_product_category` (
  `category_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` bigint(20) DEFAULT '0' COMMENT '父分类ID',
  `category_name` varchar(100) NOT NULL COMMENT '分类名称',
  `order_num` int(11) DEFAULT '0' COMMENT '排序',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='产品分类表';

-- 9. 产品表
DROP TABLE IF EXISTS `crm_product`;
CREATE TABLE `crm_product` (
  `product_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '产品ID',
  `product_name` varchar(200) NOT NULL COMMENT '产品名称',
  `category_id` bigint(20) DEFAULT NULL COMMENT '分类ID',
  `price` decimal(12,2) DEFAULT '0.00' COMMENT '价格',
  `cost_price` decimal(12,2) DEFAULT '0.00' COMMENT '成本价',
  `unit` varchar(20) DEFAULT '个' COMMENT '单位',
  `stock` int(11) DEFAULT '0' COMMENT '库存',
  `description` text COMMENT '产品描述',
  `status` char(1) DEFAULT '0' COMMENT '状态（0上架 1下架）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`product_id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='产品表';

-- 10. 通知消息表
DROP TABLE IF EXISTS `crm_notification`;
CREATE TABLE `crm_notification` (
  `notification_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `content` text COMMENT '内容',
  `type` varchar(50) DEFAULT NULL COMMENT '类型（assign/todo/overdue/system）',
  `to_user_id` bigint(20) DEFAULT NULL COMMENT '接收人ID',
  `to_dept_id` bigint(20) DEFAULT NULL COMMENT '接收部门ID',
  `biz_type` varchar(50) DEFAULT NULL COMMENT '业务类型（customer/followup/order/payment/contract）',
  `biz_id` bigint(20) DEFAULT NULL COMMENT '业务ID',
  `is_read` char(1) DEFAULT '0' COMMENT '是否已读（0未读 1已读）',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`notification_id`),
  KEY `idx_to_user` (`to_user_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='通知消息表';

-- 11. 客户公海记录表
DROP TABLE IF EXISTS `crm_customer_pool_log`;
CREATE TABLE `crm_customer_pool_log` (
  `log_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `customer_id` bigint(20) NOT NULL COMMENT '客户ID',
  `action` char(1) NOT NULL COMMENT '动作（0放入公海 1领取 2分配）',
  `from_user_id` bigint(20) DEFAULT NULL COMMENT '来源用户ID',
  `to_user_id` bigint(20) DEFAULT NULL COMMENT '目标用户ID',
  `reason` varchar(500) DEFAULT NULL COMMENT '原因',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_customer_id` (`customer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='客户公海日志表';
