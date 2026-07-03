# 智营CRM：AI驱动的客户关系管理系统 — 整体实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use subagent-driven-development (recommended) or executing-plans to implement this plan task-by-task.

**目标：** 将 RuoYi-Vue 框架系统全面改造为「智营CRM：AI驱动的客户关系管理」系统，保留系统管理基础能力，新增客户管理、跟进销售、产品服务、数据报表、通知消息五大核心模块。

**架构：** 后端 Spring Boot + MyBatis + MySQL，前端 Vue 2 + Element UI。复用 RuoYi 现有框架能力（权限、日志、数据字典、导入导出），新增 crm_ 前缀的业务表。

**技术栈：** Java 8, Spring Boot 2.x, MyBatis, MySQL 5.7+, Vue 2, Element UI, ECharts, Redis

---

## 文件结构总览

```
ruoyi-admin/src/main/java/com/ruoyi/web/controller/crm/
├── CrmCustomerController.java          # 客户管理
├── CrmFollowupController.java          # 跟进记录
├── CrmContractController.java          # 合同管理
├── CrmOrderController.java             # 订单管理
├── CrmPaymentPlanController.java       # 回款计划
├── CrmProductController.java           # 产品管理
├── CrmProductCategoryController.java   # 产品分类
├── CrmDashboardController.java         # 数据看板
├── CrmNotificationController.java      # 通知消息
├── CrmCustomerPoolController.java      # 客户公海

ruoyi-system/src/main/java/com/ruoyi/crm/
├── domain/
│   ├── CrmCustomer.java
│   ├── CrmFollowup.java
│   ├── CrmContract.java
│   ├── CrmOrder.java
│   ├── CrmPaymentPlan.java
│   ├── CrmProduct.java
│   ├── CrmProductCategory.java
│   ├── CrmCustomerPool.java
│   ├── CrmNotification.java
│   └── CrmDashboard.java
├── mapper/
│   ├── CrmCustomerMapper.java
│   ├── CrmFollowupMapper.java
│   ├── CrmContractMapper.java
│   ├── CrmOrderMapper.java
│   ├── CrmPaymentPlanMapper.java
│   ├── CrmProductMapper.java
│   ├── CrmProductCategoryMapper.java
│   ├── CrmCustomerPoolMapper.java
│   └── CrmNotificationMapper.java
├── service/
│   ├── ICrmCustomerService.java
│   ├── ICrmFollowupService.java
│   ├── ICrmContractService.java
│   ├── ICrmOrderService.java
│   ├── ICrmPaymentPlanService.java
│   ├── ICrmProductService.java
│   ├── ICrmProductCategoryService.java
│   ├── ICrmCustomerPoolService.java
│   ├── ICrmNotificationService.java
│   └── ICrmDashboardService.java
└── service/impl/
    ├── CrmCustomerServiceImpl.java
    ├── CrmFollowupServiceImpl.java
    ├── CrmContractServiceImpl.java
    ├── CrmOrderServiceImpl.java
    ├── CrmPaymentPlanServiceImpl.java
    ├── CrmProductServiceImpl.java
    ├── CrmProductCategoryServiceImpl.java
    ├── CrmCustomerPoolServiceImpl.java
    ├── CrmNotificationServiceImpl.java
    └── CrmDashboardServiceImpl.java

ruoyi-system/src/main/resources/mapper/crm/
├── CrmCustomerMapper.xml
├── CrmFollowupMapper.xml
├── CrmContractMapper.xml
├── CrmOrderMapper.xml
├── CrmPaymentPlanMapper.xml
├── CrmProductMapper.xml
├── CrmProductCategoryMapper.xml
├── CrmCustomerPoolMapper.xml
└── CrmNotificationMapper.xml

ruoyi-ui/src/
├── api/crm/
│   ├── customer.js
│   ├── followup.js
│   ├── contract.js
│   ├── order.js
│   ├── payment.js
│   ├── product.js
│   ├── category.js
│   ├── pool.js
│   ├── dashboard.js
│   └── notification.js
├── views/crm/
│   ├── customer/
│   │   ├── index.vue
│   │   ├── detail.vue
│   │   └── assign.vue
│   ├── followup/
│   │   └── index.vue
│   ├── contract/
│   │   ├── index.vue
│   │   └── detail.vue
│   ├── order/
│   │   ├── index.vue
│   │   └── detail.vue
│   ├── payment/
│   │   └── index.vue
│   ├── product/
│   │   ├── index.vue
│   │   └── category.vue
│   ├── pool/
│   │   └── index.vue
│   ├── dashboard/
│   │   └── index.vue
│   └── notification/
│       └── index.vue
├── layout/
│   └── components/
│       └── Sidebar/  (修改 Logo 和标题)
└── router/
    └── index.js (新增CRM路由)
```

---

## 实施任务

---

### 阶段 1：数据库层 & 系统品牌改造

---

### Task 1.1: 创建 CRM 业务表 SQL

**文件：**
- 创建: `sql/crm_tables.sql`

- [ ] **Step 1: 编写 CRM 完整建表 SQL**

```sql
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

-- 3. 销售漏斗阶段表（线索->意向->报价->成交->回款）
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

-- 12. 字典数据补充（CRM相关）
INSERT INTO `sys_dict_type` VALUES (100, '客户来源', 'crm_customer_source', '', '2024-01-01 00:00:00');
INSERT INTO `sys_dict_type` VALUES (101, '客户等级', 'crm_customer_level', '', '2024-01-01 00:00:00');
INSERT INTO `sys_dict_type` VALUES (102, '跟进方式', 'crm_followup_mode', '', '2024-01-01 00:00:00');
INSERT INTO `sys_dict_type` VALUES (103, '跟进状态', 'crm_follow_status', '', '2024-01-01 00:00:00');
INSERT INTO `sys_dict_type` VALUES (104, '销售阶段', 'crm_pipeline_stage', '', '2024-01-01 00:00:00');
INSERT INTO `sys_dict_type` VALUES (105, '回款方式', 'crm_payment_method', '', '2024-01-01 00:00:00');
INSERT INTO `sys_dict_type` VALUES (106, '合同状态', 'crm_contract_status', '', '2024-01-01 00:00:00');
INSERT INTO `sys_dict_type` VALUES (107, '订单状态', 'crm_order_status', '', '2024-01-01 00:00:00');
INSERT INTO `sys_dict_type` VALUES (108, '通知类型', 'crm_notification_type', '', '2024-01-01 00:00:00');

INSERT INTO `sys_dict_data` VALUES (100, '100', '电话咨询', 'phone', 'crm_customer_source', '', 'info', 'N', '0', '2024-01-01 00:00:00');
INSERT INTO `sys_dict_data` VALUES (101, '101', '在线咨询', 'online', 'crm_customer_source', '', 'info', 'N', '0', '2024-01-01 00:00:00');
INSERT INTO `sys_dict_data` VALUES (102, '102', '市场活动', 'marketing', 'crm_customer_source', '', 'info', 'N', '0', '2024-01-01 00:00:00');
INSERT INTO `sys_dict_data` VALUES (103, '103', '转介绍', 'referral', 'crm_customer_source', '', 'info', 'N', '0', '2024-01-01 00:00:00');
INSERT INTO `sys_dict_data` VALUES (104, '104', '广告投放', 'ad', 'crm_customer_source', '', 'info', 'N', '0', '2024-01-01 00:00:00');
INSERT INTO `sys_dict_data` VALUES (105, '105', '合作伙伴', 'partner', 'crm_customer_source', '', 'info', 'N', '0', '2024-01-01 00:00:00');
INSERT INTO `sys_dict_data` VALUES (106, '106', '其他', 'other', 'crm_customer_source', '', 'info', 'N', '0', '2024-01-01 00:00:00');

INSERT INTO `sys_dict_data` VALUES (110, '110', '高潜力', 'high', 'crm_customer_level', '', 'danger', 'N', '0', '2024-01-01 00:00:00');
INSERT INTO `sys_dict_data` VALUES (111, '111', '中潜力', 'medium', 'crm_customer_level', '', 'warning', 'N', '0', '2024-01-01 00:00:00');
INSERT INTO `sys_dict_data` VALUES (112, '112', '低潜力', 'low', 'crm_customer_level', '', 'info', 'N', '0', '2024-01-01 00:00:00');
INSERT INTO `sys_dict_data` VALUES (113, '113', '已成交', 'won', 'crm_customer_level', '', 'success', 'N', '0', '2024-01-01 00:00:00');

INSERT INTO `sys_dict_data` VALUES (120, '120', '电话', 'phone', 'crm_followup_mode', '', 'info', 'N', '0', '2024-01-01 00:00:00');
INSERT INTO `sys_dict_data` VALUES (121, '121', '上门拜访', 'visit', 'crm_followup_mode', '', 'info', 'N', '0', '2024-01-01 00:00:00');
INSERT INTO `sys_dict_data` VALUES (122, '122', '微信', 'wechat', 'crm_followup_mode', '', 'info', 'N', '0', '2024-01-01 00:00:00');
INSERT INTO `sys_dict_data` VALUES (123, '123', '邮件', 'email', 'crm_followup_mode', '', 'info', 'N', '0', '2024-01-01 00:00:00');

INSERT INTO `sys_dict_data` VALUES (130, '130', '待跟进', 'pending', 'crm_follow_status', '', 'info', 'N', '0', '2024-01-01 00:00:00');
INSERT INTO `sys_dict_data` VALUES (131, '131', '跟进中', 'following', 'crm_follow_status', '', 'primary', 'N', '0', '2024-01-01 00:00:00');
INSERT INTO `sys_dict_data` VALUES (132, '132', '已成交', 'won', 'crm_follow_status', '', 'success', 'N', '0', '2024-01-01 00:00:00');
INSERT INTO `sys_dict_data` VALUES (133, '133', '已流失', 'lost', 'crm_follow_status', '', 'danger', 'N', '0', '2024-01-01 00:00:00');

INSERT INTO `sys_dict_data` VALUES (140, '140', '线索', 'clue', 'crm_pipeline_stage', '', 'info', 'N', '0', '2024-01-01 00:00:00');
INSERT INTO `sys_dict_data` VALUES (141, '141', '意向', 'intent', 'crm_pipeline_stage', '', 'primary', 'N', '0', '2024-01-01 00:00:00');
INSERT INTO `sys_dict_data` VALUES (142, '142', '报价', 'quote', 'crm_pipeline_stage', '', 'warning', 'N', '0', '2024-01-01 00:00:00');
INSERT INTO `sys_dict_data` VALUES (143, '143', '成交', 'deal', 'crm_pipeline_stage', '', 'success', 'N', '0', '2024-01-01 00:00:00');
INSERT INTO `sys_dict_data` VALUES (144, '144', '回款', 'payment', 'crm_pipeline_stage', '', '', 'N', '0', '2024-01-01 00:00:00');
```

- [ ] **Step 2: 验证 SQL 语法正确性**

Run: 手动检查无语法错误即可（待后续执行）

---

### Task 1.2: 系统品牌改造（前端 Logo + 标题 + 登录页）

**文件：**
- 修改: `ruoyi-ui/src/layout/components/Sidebar/Logo.vue`
- 修改: `ruoyi-ui/src/layout/components/Navbar.vue`
- 修改: `ruoyi-ui/src/utils/auth.js` (或 settings.js) 中的系统标题
- 修改: `ruoyi-ui/src/settings.js`
- 修改: 登录页相关

- [ ] **Step 1: 修改 settings.js**

```javascript
// ruoyi-ui/src/settings.js
module.exports = {
  title: '智营CRM',
  logo: 'https://img.alicdn.com/imgextra/i4/O1CN01Kj3e2G1deJvr64Kf2_!!6000000003767-2-tps-200-200.png',
  logoWidth: 32,
  logoHeight: 32,
  showSettings: true,
  tagsView: true,
  fixedHeader: false,
  sidebarLogo: true,
  supportPinyinSearch: true,
  errorLog: 'production'
}
```

- [ ] **Step 2: 修改 Logo.vue**

```vue
<template>
  <div class="sidebar-logo-container" :class="{'collapse': collapse}" @click="$router.push('/')">
    <transition name="sidebarLogoFade">
      <router-link key="collapse" class="sidebar-logo-link" to="/">
        <img :src="logoUrl" class="sidebar-logo" />
        <h1 class="sidebar-title" v-show="!collapse">{{ title }}</h1>
      </router-link>
    </transition>
  </div>
</template>

<script>
import logoImg from '@/assets/logo/logo.png'
export default {
  name: 'SidebarLogo',
  props: { collapse: { type: Boolean, required: true } },
  data() {
    return {
      title: '智营CRM',
      logoUrl: logoImg
    }
  }
}
</script>
```

- [ ] **Step 3: 修改登录页标题**

修改 `ruoyi-ui/src/views/login.vue` 中的系统名称文本为"智营CRM"

---

### 阶段 2：后端 Domain + Mapper 层

---

### Task 2.1: 创建 CRM Domain 实体类

**文件：**
- 创建: `ruoyi-system/src/main/java/com/ruoyi/crm/domain/CrmCustomer.java`
- 创建: `ruoyi-system/src/main/java/com/ruoyi/crm/domain/CrmFollowup.java`
- 创建: `ruoyi-system/src/main/java/com/ruoyi/crm/domain/CrmContract.java`
- 创建: `ruoyi-system/src/main/java/com/ruoyi/crm/domain/CrmOrder.java`
- 创建: `ruoyi-system/src/main/java/com/ruoyi/crm/domain/CrmOrderItem.java`
- 创建: `ruoyi-system/src/main/java/com/ruoyi/crm/domain/CrmPaymentPlan.java`
- 创建: `ruoyi-system/src/main/java/com/ruoyi/crm/domain/CrmProduct.java`
- 创建: `ruoyi-system/src/main/java/com/ruoyi/crm/domain/CrmProductCategory.java`
- 创建: `ruoyi-system/src/main/java/com/ruoyi/crm/domain/CrmCustomerPool.java`
- 创建: `ruoyi-system/src/main/java/com/ruoyi/crm/domain/CrmNotification.java`
- 创建: `ruoyi-system/src/main/java/com/ruoyi/crm/domain/CrmPipeline.java`

- [ ] **Step 1: 创建 CrmCustomer.java**

```java
package com.ruoyi.crm.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

public class CrmCustomer extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long customerId;

    @Excel(name = "客户姓名")
    private String customerName;

    @Excel(name = "手机号")
    private String phone;

    @Excel(name = "性别", readConverterExp = "0=未知,1=男,2=女")
    private String sex;

    @Excel(name = "客户来源", dictType = "crm_customer_source")
    private String source;

    @Excel(name = "客户等级", dictType = "crm_customer_level")
    private String level;

    @Excel(name = "所属行业")
    private String industry;

    private String tags;

    @Excel(name = "跟进状态", dictType = "crm_follow_status")
    private String followStatus;

    private Date nextContactTime;

    private Long belongUserId;

    private Long belongDeptId;

    private String isPool;

    private Date enterPoolTime;

    private Date lastFollowTime;

    @Excel(name = "地址")
    private String address;

    @Excel(name = "邮箱")
    private String email;

    @Excel(name = "公司名称")
    private String company;

    private String position;

    private String wechat;

    private String qq;

    private String delFlag;

    /** 关联字段：归属人名称 */
    @Excels({ @Excel(name = "归属人", targetAttr = "userName", type = Excel.Type.EXPORT) })
    private String belongUserName;

    /** 关联字段：备注标签列表 */
    private String[] tagList;

    public CrmCustomer() {}

    // --- getters / setters ---
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getFollowStatus() { return followStatus; }
    public void setFollowStatus(String followStatus) { this.followStatus = followStatus; }

    public Date getNextContactTime() { return nextContactTime; }
    public void setNextContactTime(Date nextContactTime) { this.nextContactTime = nextContactTime; }

    public Long getBelongUserId() { return belongUserId; }
    public void setBelongUserId(Long belongUserId) { this.belongUserId = belongUserId; }

    public Long getBelongDeptId() { return belongDeptId; }
    public void setBelongDeptId(Long belongDeptId) { this.belongDeptId = belongDeptId; }

    public String getIsPool() { return isPool; }
    public void setIsPool(String isPool) { this.isPool = isPool; }

    public Date getEnterPoolTime() { return enterPoolTime; }
    public void setEnterPoolTime(Date enterPoolTime) { this.enterPoolTime = enterPoolTime; }

    public Date getLastFollowTime() { return lastFollowTime; }
    public void setLastFollowTime(Date lastFollowTime) { this.lastFollowTime = lastFollowTime; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getWechat() { return wechat; }
    public void setWechat(String wechat) { this.wechat = wechat; }

    public String getQq() { return qq; }
    public void setQq(String qq) { this.qq = qq; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    public String getBelongUserName() { return belongUserName; }
    public void setBelongUserName(String belongUserName) { this.belongUserName = belongUserName; }

    public String[] getTagList() { return tagList; }
    public void setTagList(String[] tagList) { this.tagList = tagList; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("customerId", getCustomerId())
            .append("customerName", getCustomerName())
            .append("phone", getPhone())
            .toString();
    }
}
```

- [ ] **Step 2: 创建 CrmFollowup.java**

```java
package com.ruoyi.crm.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

public class CrmFollowup extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long followupId;

    private Long customerId;

    @Excel(name = "联系时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date contactTime;

    @Excel(name = "跟进方式", dictType = "crm_followup_mode")
    private String followupMode;

    @Excel(name = "跟进内容")
    private String content;

    private Date nextContactTime;

    @Excel(name = "是否有效", readConverterExp = "1=是,0=否")
    private String isEffective;

    private String delFlag;

    @Excel(name = "客户名称")
    private String customerName;

    public CrmFollowup() {}

    public Long getFollowupId() { return followupId; }
    public void setFollowupId(Long followupId) { this.followupId = followupId; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Date getContactTime() { return contactTime; }
    public void setContactTime(Date contactTime) { this.contactTime = contactTime; }

    public String getFollowupMode() { return followupMode; }
    public void setFollowupMode(String followupMode) { this.followupMode = followupMode; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Date getNextContactTime() { return nextContactTime; }
    public void setNextContactTime(Date nextContactTime) { this.nextContactTime = nextContactTime; }

    public String getIsEffective() { return isEffective; }
    public void setIsEffective(String isEffective) { this.isEffective = isEffective; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("followupId", getFollowupId())
            .append("customerName", getCustomerName())
            .toString();
    }
}
```

- [ ] **Step 3: 创建 CrmContract.java**

```java
package com.ruoyi.crm.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

public class CrmContract extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long contractId;

    @Excel(name = "合同编号")
    private String contractNo;

    @Excel(name = "合同名称")
    private String contractName;

    private Long customerId;

    private Long orderId;

    @Excel(name = "合同金额")
    private Double amount;

    @Excel(name = "签订日期", dateFormat = "yyyy-MM-dd")
    private Date signDate;

    @Excel(name = "生效日期", dateFormat = "yyyy-MM-dd")
    private Date startDate;

    @Excel(name = "到期日期", dateFormat = "yyyy-MM-dd")
    private Date endDate;

    @Excel(name = "合同状态", dictType = "crm_contract_status")
    private String status;

    private String attachment;

    private String delFlag;

    @Excel(name = "客户名称")
    private String customerName;

    public CrmContract() {}

    // getters/setters...
    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }
    public String getContractNo() { return contractNo; }
    public void setContractNo(String contractNo) { this.contractNo = contractNo; }
    public String getContractName() { return contractName; }
    public void setContractName(String contractName) { this.contractName = contractName; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public Date getSignDate() { return signDate; }
    public void setSignDate(Date signDate) { this.signDate = signDate; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAttachment() { return attachment; }
    public void setAttachment(String attachment) { this.attachment = attachment; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("contractId", getContractId())
            .append("contractNo", getContractNo())
            .append("contractName", getContractName())
            .toString();
    }
}
```

- [ ] **Step 4: 创建 CrmOrder.java + CrmOrderItem.java**

**CrmOrder.java:**
```java
package com.ruoyi.crm.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;
import java.util.List;

public class CrmOrder extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long orderId;

    @Excel(name = "订单编号")
    private String orderNo;

    private Long customerId;

    private Long contractId;

    @Excel(name = "订单总金额")
    private Double totalAmount;

    @Excel(name = "折扣金额")
    private Double discountAmount;

    @Excel(name = "实付金额")
    private Double actualAmount;

    @Excel(name = "订单状态", dictType = "crm_order_status")
    private String status;

    private Double paidAmount;

    private String delFlag;

    @Excel(name = "客户名称")
    private String customerName;

    /** 订单明细列表 */
    private List<CrmOrderItem> itemList;

    public CrmOrder() {}

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public Double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(Double discountAmount) { this.discountAmount = discountAmount; }
    public Double getActualAmount() { return actualAmount; }
    public void setActualAmount(Double actualAmount) { this.actualAmount = actualAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(Double paidAmount) { this.paidAmount = paidAmount; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public List<CrmOrderItem> getItemList() { return itemList; }
    public void setItemList(List<CrmOrderItem> itemList) { this.itemList = itemList; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("orderId", getOrderId())
            .append("orderNo", getOrderNo())
            .toString();
    }
}
```

**CrmOrderItem.java:**
```java
package com.ruoyi.crm.domain;

public class CrmOrderItem {
    private Long itemId;
    private Long orderId;
    private Long productId;
    private String productName;
    private Double productPrice;
    private Integer quantity;
    private Double subtotal;
    private String remark;

    // getters/setters...
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Double getProductPrice() { return productPrice; }
    public void setProductPrice(Double productPrice) { this.productPrice = productPrice; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
```

- [ ] **Step 5: 创建 CrmPaymentPlan.java**

```java
package com.ruoyi.crm.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

public class CrmPaymentPlan extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long planId;
    private Long orderId;
    private Long contractId;

    @Excel(name = "计划回款金额")
    private Double planAmount;

    @Excel(name = "实际回款金额")
    private Double actualAmount;

    @Excel(name = "计划回款日期", dateFormat = "yyyy-MM-dd")
    private Date planDate;

    @Excel(name = "实际回款日期", dateFormat = "yyyy-MM-dd")
    private Date actualDate;

    @Excel(name = "状态", dictType = "crm_payment_status")
    private String status;

    @Excel(name = "回款方式", dictType = "crm_payment_method")
    private String paymentMethod;

    private String delFlag;

    private String orderNo;
    private String customerName;

    // getters/setters...
    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }
    public Double getPlanAmount() { return planAmount; }
    public void setPlanAmount(Double planAmount) { this.planAmount = planAmount; }
    public Double getActualAmount() { return actualAmount; }
    public void setActualAmount(Double actualAmount) { this.actualAmount = actualAmount; }
    public Date getPlanDate() { return planDate; }
    public void setPlanDate(Date planDate) { this.planDate = planDate; }
    public Date getActualDate() { return actualDate; }
    public void setActualDate(Date actualDate) { this.actualDate = actualDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("planId", getPlanId()).append("orderNo", getOrderNo()).toString();
    }
}
```

- [ ] **Step 6: 创建 CrmProduct.java + CrmProductCategory.java**

**CrmProduct.java:**
```java
package com.ruoyi.crm.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class CrmProduct extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long productId;

    @Excel(name = "产品名称")
    private String productName;

    private Long categoryId;

    @Excel(name = "价格")
    private Double price;

    private Double costPrice;

    @Excel(name = "单位")
    private String unit;

    @Excel(name = "库存")
    private Integer stock;

    @Excel(name = "产品描述")
    private String description;

    @Excel(name = "状态", readConverterExp = "0=上架,1=下架")
    private String status;

    private String delFlag;

    @Excel(name = "分类名称")
    private String categoryName;

    // getters/setters...
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Double getCostPrice() { return costPrice; }
    public void setCostPrice(Double costPrice) { this.costPrice = costPrice; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}
```

**CrmProductCategory.java:**
```java
package com.ruoyi.crm.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class CrmProductCategory extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long categoryId;
    private Long parentId;

    @Excel(name = "分类名称")
    private String categoryName;

    @Excel(name = "排序")
    private Integer orderNum;

    // getters/setters...
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Integer getOrderNum() { return orderNum; }
    public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }
}
```

- [ ] **Step 7: 创建 CrmNotification.java**

```java
package com.ruoyi.crm.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;

public class CrmNotification extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long notificationId;
    private String title;
    private String content;
    private String type;
    private Long toUserId;
    private Long toDeptId;
    private String bizType;
    private Long bizId;
    private String isRead;
    private Date readTime;
    private String delFlag;

    // getters/setters...
    public Long getNotificationId() { return notificationId; }
    public void setNotificationId(Long notificationId) { this.notificationId = notificationId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getToUserId() { return toUserId; }
    public void setToUserId(Long toUserId) { this.toUserId = toUserId; }
    public Long getToDeptId() { return toDeptId; }
    public void setToDeptId(Long toDeptId) { this.toDeptId = toDeptId; }
    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public Long getBizId() { return bizId; }
    public void setBizId(Long bizId) { this.bizId = bizId; }
    public String getIsRead() { return isRead; }
    public void setIsRead(String isRead) { this.isRead = isRead; }
    public Date getReadTime() { return readTime; }
    public void setReadTime(Date readTime) { this.readTime = readTime; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
```

- [ ] **Step 8: 创建 CrmCustomerPool.java（用于公海日志）**

```java
package com.ruoyi.crm.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;

public class CrmCustomerPool extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long logId;
    private Long customerId;
    private String action;
    private Long fromUserId;
    private Long toUserId;
    private String reason;

    // 关联
    private String customerName;
    private String fromUserName;
    private String toUserName;

    // getters/setters...
    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Long getFromUserId() { return fromUserId; }
    public void setFromUserId(Long fromUserId) { this.fromUserId = fromUserId; }
    public Long getToUserId() { return toUserId; }
    public void setToUserId(Long toUserId) { this.toUserId = toUserId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getFromUserName() { return fromUserName; }
    public void setFromUserName(String fromUserName) { this.fromUserName = fromUserName; }
    public String getToUserName() { return toUserName; }
    public void setToUserName(String toUserName) { this.toUserName = toUserName; }
}
```

- [ ] **Step 9: 创建 CrmPipeline.java**

```java
package com.ruoyi.crm.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;
import java.util.Date;

public class CrmPipeline extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long pipelineId;
    private Long customerId;
    private Long orderId;
    private String stage;
    private BigDecimal amount;
    private Integer probability;
    private Date expectedCloseDate;
    private String delFlag;

    private String customerName;
    private String orderNo;

    // getters/setters...
    public Long getPipelineId() { return pipelineId; }
    public void setPipelineId(Long pipelineId) { this.pipelineId = pipelineId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Integer getProbability() { return probability; }
    public void setProbability(Integer probability) { this.probability = probability; }
    public Date getExpectedCloseDate() { return expectedCloseDate; }
    public void setExpectedCloseDate(Date expectedCloseDate) { this.expectedCloseDate = expectedCloseDate; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
}
```

---

### Task 2.2: 创建 MyBatis Mapper XML

**文件：**
- 创建: `ruoyi-system/src/main/resources/mapper/crm/CrmCustomerMapper.xml`
- 创建: `ruoyi-system/src/main/resources/mapper/crm/CrmFollowupMapper.xml`
- 创建: `ruoyi-system/src/main/resources/mapper/crm/CrmContractMapper.xml`
- 创建: `ruoyi-system/src/main/resources/mapper/crm/CrmOrderMapper.xml`
- 创建: `ruoyi-system/src/main/resources/mapper/crm/CrmPaymentPlanMapper.xml`
- 创建: `ruoyi-system/src/main/resources/mapper/crm/CrmProductMapper.xml`
- 创建: `ruoyi-system/src/main/resources/mapper/crm/CrmProductCategoryMapper.xml`
- 创建: `ruoyi-system/src/main/resources/mapper/crm/CrmNotificationMapper.xml`
- 创建: `ruoyi-system/src/main/resources/mapper/crm/CrmCustomerPoolMapper.xml`
- 创建: `ruoyi-system/src/main/resources/mapper/crm/CrmPipelineMapper.xml`

- [ ] **Step 1: 创建 CrmCustomerMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.ruoyi.crm.mapper.CrmCustomerMapper">

    <resultMap type="CrmCustomer" id="CrmCustomerResult">
        <id property="customerId" column="customer_id" />
        <result property="customerName" column="customer_name" />
        <result property="phone" column="phone" />
        <result property="sex" column="sex" />
        <result property="source" column="source" />
        <result property="level" column="level" />
        <result property="industry" column="industry" />
        <result property="tags" column="tags" />
        <result property="followStatus" column="follow_status" />
        <result property="nextContactTime" column="next_contact_time" />
        <result property="belongUserId" column="belong_user_id" />
        <result property="belongDeptId" column="belong_dept_id" />
        <result property="isPool" column="is_pool" />
        <result property="enterPoolTime" column="enter_pool_time" />
        <result property="lastFollowTime" column="last_follow_time" />
        <result property="address" column="address" />
        <result property="email" column="email" />
        <result property="company" column="company" />
        <result property="position" column="position" />
        <result property="wechat" column="wechat" />
        <result property="qq" column="qq" />
        <result property="remark" column="remark" />
        <result property="delFlag" column="del_flag" />
        <result property="createBy" column="create_by" />
        <result property="createTime" column="create_time" />
        <result property="updateBy" column="update_by" />
        <result property="updateTime" column="update_time" />
        <result property="belongUserName" column="belong_user_name" />
    </resultMap>

    <sql id="selectCrmCustomerVo">
        select c.customer_id, c.customer_name, c.phone, c.sex, c.source, c.level, c.industry,
               c.tags, c.follow_status, c.next_contact_time, c.belong_user_id, c.belong_dept_id,
               c.is_pool, c.enter_pool_time, c.last_follow_time, c.address, c.email, c.company,
               c.position, c.wechat, c.qq, c.remark, c.del_flag, c.create_by, c.create_time,
               c.update_by, c.update_time,
               u.user_name as belong_user_name
        from crm_customer c
        left join sys_user u on c.belong_user_id = u.user_id
    </sql>

    <select id="selectCrmCustomerList" parameterType="CrmCustomer" resultMap="CrmCustomerResult">
        <include refid="selectCrmCustomerVo" />
        where c.del_flag = '0'
        <if test="customerName != null and customerName != ''">
            and c.customer_name like concat('%', #{customerName}, '%')
        </if>
        <if test="phone != null and phone != ''">
            and c.phone like concat('%', #{phone}, '%')
        </if>
        <if test="source != null and source != ''">
            and c.source = #{source}
        </if>
        <if test="level != null and level != ''">
            and c.level = #{level}
        </if>
        <if test="followStatus != null and followStatus != ''">
            and c.follow_status = #{followStatus}
        </if>
        <if test="isPool != null and isPool != ''">
            and c.is_pool = #{isPool}
        </if>
        <if test="belongUserId != null">
            and c.belong_user_id = #{belongUserId}
        </if>
        order by c.create_time desc
    </select>

    <select id="selectCrmCustomerById" parameterType="Long" resultMap="CrmCustomerResult">
        <include refid="selectCrmCustomerVo" />
        where c.customer_id = #{customerId}
    </select>

    <select id="checkCrmCustomerPhoneUnique" parameterType="String" resultMap="CrmCustomerResult">
        <include refid="selectCrmCustomerVo" />
        where c.phone = #{phone} and c.del_flag = '0' limit 1
    </select>

    <select id="checkCrmCustomerNameUnique" parameterType="String" resultMap="CrmCustomerResult">
        <include refid="selectCrmCustomerVo" />
        where c.customer_name = #{customerName} and c.del_flag = '0' limit 1
    </select>

    <insert id="insertCrmCustomer" parameterType="CrmCustomer" useGeneratedKeys="true" keyProperty="customerId">
        insert into crm_customer
        <trim prefix="(" suffix=")" suffixOverrides=",">
            <if test="customerName != null">customer_name,</if>
            <if test="phone != null">phone,</if>
            <if test="sex != null">sex,</if>
            <if test="source != null">source,</if>
            <if test="level != null">level,</if>
            <if test="industry != null">industry,</if>
            <if test="tags != null">tags,</if>
            <if test="followStatus != null">follow_status,</if>
            <if test="nextContactTime != null">next_contact_time,</if>
            <if test="belongUserId != null">belong_user_id,</if>
            <if test="belongDeptId != null">belong_dept_id,</if>
            <if test="address != null">address,</if>
            <if test="email != null">email,</if>
            <if test="company != null">company,</if>
            <if test="position != null">position,</if>
            <if test="wechat != null">wechat,</if>
            <if test="qq != null">qq,</if>
            <if test="remark != null">remark,</if>
            <if test="createBy != null">create_by,</if>
            <if test="createTime != null">create_time,</if>
            <if test="updateBy != null">update_by,</if>
            <if test="updateTime != null">update_time,</if>
        </trim>
        <trim prefix="values (" suffix=")" suffixOverrides=",">
            <if test="customerName != null">#{customerName},</if>
            <if test="phone != null">#{phone},</if>
            <if test="sex != null">#{sex},</if>
            <if test="source != null">#{source},</if>
            <if test="level != null">#{level},</if>
            <if test="industry != null">#{industry},</if>
            <if test="tags != null">#{tags},</if>
            <if test="followStatus != null">#{followStatus},</if>
            <if test="nextContactTime != null">#{nextContactTime},</if>
            <if test="belongUserId != null">#{belongUserId},</if>
            <if test="belongDeptId != null">#{belongDeptId},</if>
            <if test="address != null">#{address},</if>
            <if test="email != null">#{email},</if>
            <if test="company != null">#{company},</if>
            <if test="position != null">#{position},</if>
            <if test="wechat != null">#{wechat},</if>
            <if test="qq != null">#{qq},</if>
            <if test="remark != null">#{remark},</if>
            <if test="createBy != null">#{createBy},</if>
            <if test="createTime != null">#{createTime},</if>
            <if test="updateBy != null">#{updateBy},</if>
            <if test="updateTime != null">#{updateTime},</if>
        </trim>
    </insert>

    <update id="updateCrmCustomer" parameterType="CrmCustomer">
        update crm_customer
        <trim prefix="SET" suffixOverrides=",">
            <if test="customerName != null">customer_name = #{customerName},</if>
            <if test="phone != null">phone = #{phone},</if>
            <if test="sex != null">sex = #{sex},</if>
            <if test="source != null">source = #{source},</if>
            <if test="level != null">level = #{level},</if>
            <if test="industry != null">industry = #{industry},</if>
            <if test="tags != null">tags = #{tags},</if>
            <if test="followStatus != null">follow_status = #{followStatus},</if>
            <if test="nextContactTime != null">next_contact_time = #{nextContactTime},</if>
            <if test="belongUserId != null">belong_user_id = #{belongUserId},</if>
            <if test="belongDeptId != null">belong_dept_id = #{belongDeptId},</if>
            <if test="isPool != null">is_pool = #{isPool},</if>
            <if test="enterPoolTime != null">enter_pool_time = #{enterPoolTime},</if>
            <if test="lastFollowTime != null">last_follow_time = #{lastFollowTime},</if>
            <if test="address != null">address = #{address},</if>
            <if test="email != null">email = #{email},</if>
            <if test="company != null">company = #{company},</if>
            <if test="position != null">position = #{position},</if>
            <if test="wechat != null">wechat = #{wechat},</if>
            <if test="qq != null">qq = #{qq},</if>
            <if test="remark != null">remark = #{remark},</if>
            <if test="updateBy != null">update_by = #{updateBy},</if>
            <if test="updateTime != null">update_time = #{updateTime},</if>
        </trim>
        where customer_id = #{customerId}
    </update>

    <update id="deleteCrmCustomerById" parameterType="Long">
        update crm_customer set del_flag = '2' where customer_id = #{customerId}
    </update>

    <update id="deleteCrmCustomerByIds" parameterType="String">
        update crm_customer set del_flag = '2' where customer_id in
        <foreach item="customerId" collection="array" open="(" separator="," close=")">#{customerId}</foreach>
    </update>
</mapper>
```

- [ ] **Step 2: 创建 CrmFollowupMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.ruoyi.crm.mapper.CrmFollowupMapper">

    <resultMap type="CrmFollowup" id="CrmFollowupResult">
        <id property="followupId" column="followup_id" />
        <result property="customerId" column="customer_id" />
        <result property="contactTime" column="contact_time" />
        <result property="followupMode" column="followup_mode" />
        <result property="content" column="content" />
        <result property="nextContactTime" column="next_contact_time" />
        <result property="isEffective" column="is_effective" />
        <result property="delFlag" column="del_flag" />
        <result property="createBy" column="create_by" />
        <result property="createTime" column="create_time" />
        <result property="updateBy" column="update_by" />
        <result property="updateTime" column="update_time" />
        <result property="customerName" column="customer_name" />
    </resultMap>

    <sql id="selectCrmFollowupVo">
        select f.followup_id, f.customer_id, f.contact_time, f.followup_mode, f.content,
               f.next_contact_time, f.is_effective, f.del_flag, f.create_by, f.create_time,
               f.update_by, f.update_time, c.customer_name
        from crm_followup f
        left join crm_customer c on f.customer_id = c.customer_id
    </sql>

    <select id="selectCrmFollowupList" parameterType="CrmFollowup" resultMap="CrmFollowupResult">
        <include refid="selectCrmFollowupVo" />
        where f.del_flag = '0'
        <if test="customerId != null">
            and f.customer_id = #{customerId}
        </if>
        <if test="customerName != null and customerName != ''">
            and c.customer_name like concat('%', #{customerName}, '%')
        </if>
        <if test="followupMode != null and followupMode != ''">
            and f.followup_mode = #{followupMode}
        </if>
        <if test="isEffective != null and isEffective != ''">
            and f.is_effective = #{isEffective}
        </if>
        order by f.contact_time desc
    </select>

    <select id="selectCrmFollowupById" parameterType="Long" resultMap="CrmFollowupResult">
        <include refid="selectCrmFollowupVo" />
        where f.followup_id = #{followupId}
    </select>

    <insert id="insertCrmFollowup" parameterType="CrmFollowup" useGeneratedKeys="true" keyProperty="followupId">
        insert into crm_followup
        <trim prefix="(" suffix=")" suffixOverrides=",">
            <if test="customerId != null">customer_id,</if>
            <if test="contactTime != null">contact_time,</if>
            <if test="followupMode != null">followup_mode,</if>
            <if test="content != null">content,</if>
            <if test="nextContactTime != null">next_contact_time,</if>
            <if test="isEffective != null">is_effective,</if>
            <if test="createBy != null">create_by,</if>
            <if test="createTime != null">create_time,</if>
        </trim>
        <trim prefix="values (" suffix=")" suffixOverrides=",">
            <if test="customerId != null">#{customerId},</if>
            <if test="contactTime != null">#{contactTime},</if>
            <if test="followupMode != null">#{followupMode},</if>
            <if test="content != null">#{content},</if>
            <if test="nextContactTime != null">#{nextContactTime},</if>
            <if test="isEffective != null">#{isEffective},</if>
            <if test="createBy != null">#{createBy},</if>
            <if test="createTime != null">#{createTime},</if>
        </trim>
    </insert>

    <update id="updateCrmFollowup" parameterType="CrmFollowup">
        update crm_followup
        <trim prefix="SET" suffixOverrides=",">
            <if test="customerId != null">customer_id = #{customerId},</if>
            <if test="contactTime != null">contact_time = #{contactTime},</if>
            <if test="followupMode != null">followup_mode = #{followupMode},</if>
            <if test="content != null">content = #{content},</if>
            <if test="nextContactTime != null">next_contact_time = #{nextContactTime},</if>
            <if test="isEffective != null">is_effective = #{isEffective},</if>
            <if test="updateBy != null">update_by = #{updateBy},</if>
            <if test="updateTime != null">update_time = #{updateTime},</if>
        </trim>
        where followup_id = #{followupId}
    </update>

    <delete id="deleteCrmFollowupById" parameterType="Long">
        update crm_followup set del_flag = '2' where followup_id = #{followupId}
    </delete>

    <delete id="deleteCrmFollowupByIds" parameterType="String">
        update crm_followup set del_flag = '2' where followup_id in
        <foreach item="followupId" collection="array" open="(" separator="," close=")">#{followupId}</foreach>
    </delete>
</mapper>
```

- [ ] **Step 3: 创建其余 Mapper XML 文件（CrmContractMapper.xml, CrmOrderMapper.xml, CrmPaymentPlanMapper.xml, CrmProductMapper.xml, CrmProductCategoryMapper.xml, CrmNotificationMapper.xml, CrmCustomerPoolMapper.xml, CrmPipelineMapper.xml）**

所有 Mapper XML 遵循相同的模式：
- `<resultMap>` 定义字段映射
- `<sql id="selectVo">` 定义基本查询语句（含关联表 LEFT JOIN）
- `selectList` 带条件查询（分页）
- `selectById` 根据主键查询
- `insert` 动态插入（userGeneratedKeys）
- `update` 动态更新
- `deleteByIds` 逻辑删除（del_flag = '2'）

**各 Mapper 特殊需求：**

`CrmOrderMapper.xml`：需同时查询订单明细（通过 `collection` 关联）
```xml
<resultMap type="CrmOrder" id="CrmOrderResult" extends="BaseResultMap">
    <collection property="itemList" ofType="CrmOrderItem" column="order_id"
        select="com.ruoyi.crm.mapper.CrmOrderMapper.selectCrmOrderItemsByOrderId" />
</resultMap>
```

`CrmNotificationMapper.xml`：
- `selectUnreadCount` 统计未读数量
- `markAsRead` 标记已读

`CrmCustomerPoolMapper.xml`：
- 在 `insertCrmCustomerPoolLog` 记录公海操作日志

### Task 2.3: 创建 Mapper Java 接口

**文件：**
- 创建: `ruoyi-system/src/main/java/com/ruoyi/crm/mapper/CrmCustomerMapper.java`
- 创建: `ruoyi-system/src/main/java/com/ruoyi/crm/mapper/CrmFollowupMapper.java`
- 创建: `ruoyi-system/src/main/java/com/ruoyi/crm/mapper/CrmContractMapper.java`
- 创建: `ruoyi-system/src/main/java/com/ruoyi/crm/mapper/CrmOrderMapper.java`
- 创建: `ruoyi-system/src/main/java/com/ruoyi/crm/mapper/CrmPaymentPlanMapper.java`
- 创建: `ruoyi-system/src/main/java/com/ruoyi/crm/mapper/CrmProductMapper.java`
- 创建: `ruoyi-system/src/main/java/com/ruoyi/crm/mapper/CrmProductCategoryMapper.java`
- 创建: `ruoyi-system/src/main/java/com/ruoyi/crm/mapper/CrmNotificationMapper.java`
- 创建: `ruoyi-system/src/main/java/com/ruoyi/crm/mapper/CrmCustomerPoolMapper.java`
- 创建: `ruoyi-system/src/main/java/com/ruoyi/crm/mapper/CrmPipelineMapper.java`

所有 Mapper 遵循标准模式，例如：

```java
package com.ruoyi.crm.mapper;

import com.ruoyi.crm.domain.CrmCustomer;
import java.util.List;

public interface CrmCustomerMapper {
    List<CrmCustomer> selectCrmCustomerList(CrmCustomer customer);
    CrmCustomer selectCrmCustomerById(Long customerId);
    CrmCustomer checkCrmCustomerPhoneUnique(String phone);
    CrmCustomer checkCrmCustomerNameUnique(String customerName);
    int insertCrmCustomer(CrmCustomer customer);
    int updateCrmCustomer(CrmCustomer customer);
    int deleteCrmCustomerById(Long customerId);
    int deleteCrmCustomerByIds(Long[] customerIds);
}
```

特殊 Mapper：
- `CrmOrderMapper` 多一个 `selectCrmOrderItemsByOrderId(Long orderId)` 和 `batchInsertOrderItems(List<CrmOrderItem> items)`
- `CrmNotificationMapper` 多一个 `selectUnreadCount(Long userId)` 和 `markAsRead(Long notificationId)`
- `CrmCustomerPoolMapper` 多一个 `insertCrmCustomerPoolLog(CrmCustomerPool log)`

---

### 阶段 3：后端 Service 层

---

### Task 3.1: 创建 Service 接口

**文件：**
- 创建: 所有 `ICrmXxxService.java` 接口文件

每个 Service 接口包含标准 CRUD 方法，例如：

```java
package com.ruoyi.crm.service;

import com.ruoyi.crm.domain.CrmCustomer;
import java.util.List;

public interface ICrmCustomerService {
    List<CrmCustomer> selectCrmCustomerList(CrmCustomer customer);
    CrmCustomer selectCrmCustomerById(Long customerId);
    int insertCrmCustomer(CrmCustomer customer);
    int updateCrmCustomer(CrmCustomer customer);
    int deleteCrmCustomerByIds(Long[] customerIds);
    String checkPhoneUnique(String phone);
    String checkNameUnique(String customerName);
    // 客户分配
    int assignCustomer(Long customerId, Long userId);
    // 放入/领取公海
    int putToPool(Long customerId, String reason);
    int claimFromPool(Long customerId);
    // 导入
    String importCustomer(List<CrmCustomer> customerList, boolean updateSupport, String operName);
}
```

各 Service 接口特有方法：

**ICrmFollowupService:**
```java
List<CrmFollowup> selectCrmFollowupByCustomerId(Long customerId);
```

**ICrmOrderService:**
```java
CrmOrder selectCrmOrderByNo(String orderNo);
int insertCrmOrder(CrmOrder order); // 含明细
```

**ICrmNotificationService:**
```java
int selectUnreadCount(Long userId);
int markAsRead(Long notificationId);
int createNotification(String title, String content, String type, Long toUserId, String bizType, Long bizId);
```

**ICrmCustomerPoolService:**
```java
List<CrmCustomerPool> selectCrmCustomerPoolLogList(CrmCustomerPool log);
```

**ICrmDashboardService:**
```java
// 客户统计
int selectTodayNewCustomerCount();
int selectTotalCustomerCount();
List<Map<String, Object>> selectCustomerSourceStats();
List<Map<String, Object>> selectCustomerLevelStats();
List<Map<String, Object>> selectCustomerFollowStatusStats();

// 销售统计
List<Map<String, Object>> selectSalesStatsByUser(Long deptId);
List<Map<String, Object>> selectSalesStatsByDept();
Double selectTotalDealAmount();
Double selectMonthDealAmount();

// 跟进统计
List<Map<String, Object>> selectFollowupStatsByUser();
List<Map<String, Object>> selectFollowupCountByDate(String startDate, String endDate);

// 待办统计
int selectPendingFollowupCount(Long userId);
int selectPendingPaymentCount(Long userId);
int selectOverdueFollowupCount();
```

---

### Task 3.2: 创建 Service 实现类

**文件：**
- 创建: 所有 `CrmXxxServiceImpl.java` 实现类

所有 Service 实现类模式：

```java
package com.ruoyi.crm.service.impl;

import com.ruoyi.crm.domain.CrmCustomer;
import com.ruoyi.crm.mapper.CrmCustomerMapper;
import com.ruoyi.crm.service.ICrmCustomerService;
import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class CrmCustomerServiceImpl implements ICrmCustomerService {

    @Autowired
    private CrmCustomerMapper crmCustomerMapper;

    @Override
    public List<CrmCustomer> selectCrmCustomerList(CrmCustomer customer) {
        return crmCustomerMapper.selectCrmCustomerList(customer);
    }

    @Override
    public CrmCustomer selectCrmCustomerById(Long customerId) {
        return crmCustomerMapper.selectCrmCustomerById(customerId);
    }

    @Override
    @Transactional
    public int insertCrmCustomer(CrmCustomer customer) {
        customer.setCreateBy(SecurityUtils.getUsername());
        customer.setCreateTime(DateUtils.getNowDate());
        // 默认自己为归属人
        if (customer.getBelongUserId() == null) {
            customer.setBelongUserId(SecurityUtils.getUserId());
        }
        if (customer.getBelongDeptId() == null) {
            customer.setBelongDeptId(SecurityUtils.getDeptId());
        }
        customer.setFollowStatus("0"); // 默认待跟进
        return crmCustomerMapper.insertCrmCustomer(customer);
    }

    @Override
    @Transactional
    public int updateCrmCustomer(CrmCustomer customer) {
        customer.setUpdateBy(SecurityUtils.getUsername());
        customer.setUpdateTime(DateUtils.getNowDate());
        return crmCustomerMapper.updateCrmCustomer(customer);
    }

    @Override
    @Transactional
    public int deleteCrmCustomerByIds(Long[] customerIds) {
        return crmCustomerMapper.deleteCrmCustomerByIds(customerIds);
    }

    @Override
    public String checkPhoneUnique(String phone) {
        if (StringUtils.isEmpty(phone)) return UserConstants.UNIQUE;
        CrmCustomer exist = crmCustomerMapper.checkCrmCustomerPhoneUnique(phone);
        return exist == null ? UserConstants.UNIQUE : UserConstants.NOT_UNIQUE;
    }

    @Override
    public String checkNameUnique(String customerName) {
        if (StringUtils.isEmpty(customerName)) return UserConstants.UNIQUE;
        CrmCustomer exist = crmCustomerMapper.checkCrmCustomerNameUnique(customerName);
        return exist == null ? UserConstants.UNIQUE : UserConstants.NOT_UNIQUE;
    }

    @Override
    @Transactional
    public int assignCustomer(Long customerId, Long userId) {
        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(customerId);
        customer.setBelongUserId(userId);
        customer.setIsPool("0");
        customer.setUpdateBy(SecurityUtils.getUsername());
        customer.setUpdateTime(DateUtils.getNowDate());
        return crmCustomerMapper.updateCrmCustomer(customer);
    }

    @Override
    @Transactional
    public int putToPool(Long customerId, String reason) {
        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(customerId);
        customer.setBelongUserId(null);
        customer.setIsPool("1");
        customer.setEnterPoolTime(DateUtils.getNowDate());
        customer.setUpdateBy(SecurityUtils.getUsername());
        customer.setUpdateTime(DateUtils.getNowDate());
        return crmCustomerMapper.updateCrmCustomer(customer);
    }

    @Override
    @Transactional
    public int claimFromPool(Long customerId) {
        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(customerId);
        customer.setBelongUserId(SecurityUtils.getUserId());
        customer.setIsPool("0");
        customer.setEnterPoolTime(null);
        customer.setUpdateBy(SecurityUtils.getUsername());
        customer.setUpdateTime(DateUtils.getNowDate());
        return crmCustomerMapper.updateCrmCustomer(customer);
    }

    @Override
    @Transactional
    public String importCustomer(List<CrmCustomer> customerList, boolean updateSupport, String operName) {
        if (customerList == null || customerList.size() == 0) return "0";
        int successNum = 0;
        int failureNum = 0;
        StringBuilder failureMsg = new StringBuilder();
        for (CrmCustomer customer : customerList) {
            try {
                CrmCustomer exist = crmCustomerMapper.checkCrmCustomerPhoneUnique(customer.getPhone());
                if (exist != null && updateSupport) {
                    customer.setCustomerId(exist.getCustomerId());
                    crmCustomerMapper.updateCrmCustomer(customer);
                    successNum++;
                } else if (exist == null) {
                    customer.setCreateBy(operName);
                    customer.setCreateTime(DateUtils.getNowDate());
                    crmCustomerMapper.insertCrmCustomer(customer);
                    successNum++;
                } else {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、手机号 " + customer.getPhone() + " 已存在");
                }
            } catch (Exception e) {
                failureNum++;
                failureMsg.append("<br/>" + failureNum + "、客户 " + customer.getCustomerName() + " 导入失败: " + e.getMessage());
            }
        }
        if (failureNum > 0) {
            failureMsg.insert(0, "导入失败" + failureNum + "条，错误信息：");
            throw new ServiceException(failureMsg.toString());
        }
        return Integer.toString(successNum);
    }
}
```

---

### 阶段 4：后端 Controller 层

---

### Task 4.1: 创建 CrmCustomerController

**文件：**
- 创建: `ruoyi-admin/src/main/java/com/ruoyi/web/controller/crm/CrmCustomerController.java`

```java
package com.ruoyi.web.controller.crm;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.crm.domain.CrmCustomer;
import com.ruoyi.crm.service.ICrmCustomerService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/crm/customer")
public class CrmCustomerController extends BaseController {

    @Autowired
    private ICrmCustomerService crmCustomerService;

    @PreAuthorize("@ss.hasPermi('crm:customer:list')")
    @GetMapping("/list")
    public TableDataInfo list(CrmCustomer customer) {
        startPage();
        List<CrmCustomer> list = crmCustomerService.selectCrmCustomerList(customer);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('crm:customer:query')")
    @GetMapping(value = { "/", "/{customerId}" })
    public AjaxResult getInfo(@PathVariable(value = "customerId", required = false) Long customerId) {
        return success(crmCustomerService.selectCrmCustomerById(customerId));
    }

    @PreAuthorize("@ss.hasPermi('crm:customer:add')")
    @Log(title = "客户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody CrmCustomer customer) {
        if (UserConstants.NOT_UNIQUE.equals(crmCustomerService.checkPhoneUnique(customer.getPhone()))) {
            return warn("手机号已存在，请检查");
        }
        return toAjax(crmCustomerService.insertCrmCustomer(customer));
    }

    @PreAuthorize("@ss.hasPermi('crm:customer:edit')")
    @Log(title = "客户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody CrmCustomer customer) {
        return toAjax(crmCustomerService.updateCrmCustomer(customer));
    }

    @PreAuthorize("@ss.hasPermi('crm:customer:remove')")
    @Log(title = "客户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{customerIds}")
    public AjaxResult remove(@PathVariable Long[] customerIds) {
        return toAjax(crmCustomerService.deleteCrmCustomerByIds(customerIds));
    }

    @Log(title = "客户管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('crm:customer:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, CrmCustomer customer) {
        List<CrmCustomer> list = crmCustomerService.selectCrmCustomerList(customer);
        ExcelUtil<CrmCustomer> util = new ExcelUtil<>(CrmCustomer.class);
        util.exportExcel(response, list, "客户数据");
    }

    @Log(title = "客户管理", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('crm:customer:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception {
        ExcelUtil<CrmCustomer> util = new ExcelUtil<>(CrmCustomer.class);
        List<CrmCustomer> customerList = util.importExcel(file.getInputStream());
        String operName = getUsername();
        String message = crmCustomerService.importCustomer(customerList, updateSupport, operName);
        return success(message);
    }

    @GetMapping("/importTemplate")
    public AjaxResult importTemplate(HttpServletResponse response) {
        ExcelUtil<CrmCustomer> util = new ExcelUtil<>(CrmCustomer.class);
        util.importTemplateExcel(response, "客户数据");
    }

    @PreAuthorize("@ss.hasPermi('crm:customer:edit')")
    @Log(title = "客户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/assign")
    public AjaxResult assign(@RequestParam Long customerId, @RequestParam Long userId) {
        return toAjax(crmCustomerService.assignCustomer(customerId, userId));
    }

    @PreAuthorize("@ss.hasPermi('crm:customer:edit')")
    @Log(title = "客户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/pool")
    public AjaxResult putToPool(@RequestParam Long customerId, @RequestParam(required = false) String reason) {
        return toAjax(crmCustomerService.putToPool(customerId, reason));
    }

    @PreAuthorize("@ss.hasPermi('crm:customer:edit')")
    @Log(title = "客户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/claim")
    public AjaxResult claimFromPool(@RequestParam Long customerId) {
        return toAjax(crmCustomerService.claimFromPool(customerId));
    }

    @PreAuthorize("@ss.hasPermi('crm:customer:query')")
    @GetMapping("/checkPhoneUnique")
    public AjaxResult checkPhoneUnique(String phone) {
        return success(crmCustomerService.checkPhoneUnique(phone));
    }
}
```

---

### Task 4.2: 创建其余 Controller

**文件：**
- `CrmFollowupController.java` — `/crm/followup` 下有 list / getInfo / add / edit / remove / 按客户查询
- `CrmContractController.java` — `/crm/contract` 下有 list / getInfo / add / edit / remove / export
- `CrmOrderController.java` — `/crm/order` 下有 list / getInfo / add（含明细）/ edit / remove / export
- `CrmPaymentPlanController.java` — `/crm/payment` 下有 list / getInfo / add / edit / remove / markPaid
- `CrmProductController.java` — `/crm/product` 下有 list / getInfo / add / edit / remove / export
- `CrmProductCategoryController.java` — `/crm/category` 下有 list / getInfo / add / edit / remove / treeList
- `CrmCustomerPoolController.java` — `/crm/pool` 下有 list / claim / logList
- `CrmNotificationController.java` — `/crm/notification` 下有 list / getInfo / markRead / unreadCount
- `CrmDashboardController.java` — `/crm/dashboard` 下有各种统计接口
- `CrmPipelineController.java` — `/crm/pipeline` 下有 list / getInfo / add / edit / remove / updateStage

所有 Controller 遵循：
- 继承 `BaseController`
- `@PreAuthorize("@ss.hasPermi('crm:xxx:action')")` 权限注解
- `@Log` 操作日志注解
- RESTful URL 风格
- `startPage()` + `getDataTable()` 分页

---

### 阶段 5：前端 API 层

---

### Task 5.1: 创建前端 API 模块

**文件：** `ruoyi-ui/src/api/crm/` 下创建所有 JS API 文件

每个 API 文件模式（以 customer.js 为例）：

```javascript
import request from '@/utils/request'

// 查询客户列表
export function listCustomer(query) {
  return request({ url: '/crm/customer/list', method: 'get', params: query })
}

// 查询客户详细
export function getCustomer(customerId) {
  return request({ url: '/crm/customer/' + customerId, method: 'get' })
}

// 新增客户
export function addCustomer(data) {
  return request({ url: '/crm/customer', method: 'post', data })
}

// 修改客户
export function updateCustomer(data) {
  return request({ url: '/crm/customer', method: 'put', data })
}

// 删除客户
export function delCustomer(customerId) {
  return request({ url: '/crm/customer/' + customerId, method: 'delete' })
}

// 导出客户
export function exportCustomer(query) {
  return request({ url: '/crm/customer/export', method: 'post', params: query, responseType: 'blob' })
}

// 导入客户
export function importCustomer(data) {
  return request({ url: '/crm/customer/importData', method: 'post', data, headers: { 'Content-Type': 'multipart/form-data' } })
}

// 下载导入模板
export function importTemplate() {
  return request({ url: '/crm/customer/importTemplate', method: 'get', responseType: 'blob' })
}

// 分配客户
export function assignCustomer(customerId, userId) {
  return request({ url: '/crm/customer/assign', method: 'put', params: { customerId, userId } })
}

// 放入公海
export function putToPool(customerId, reason) {
  return request({ url: '/crm/customer/pool', method: 'put', params: { customerId, reason } })
}

// 领取客户
export function claimFromPool(customerId) {
  return request({ url: '/crm/customer/claim', method: 'put', params: { customerId } })
}

// 检查手机号唯一
export function checkPhoneUnique(phone) {
  return request({ url: '/crm/customer/checkPhoneUnique', method: 'get', params: { phone } })
}
```

其他 API 文件相似，方法名遵循 `listXxx`, `getXxx`, `addXxx`, `updateXxx`, `delXxx` 命名规范。

**Dashboard API（dashboard.js）：**
```javascript
import request from '@/utils/request'

export function getCustomerStats() {
  return request({ url: '/crm/dashboard/customerStats', method: 'get' })
}
export function getSalesStats() {
  return request({ url: '/crm/dashboard/salesStats', method: 'get' })
}
export function getFollowupStats() {
  return request({ url: '/crm/dashboard/followupStats', method: 'get' })
}
export function getTodoStats() {
  return request({ url: '/crm/dashboard/todoStats', method: 'get' })
}
```

---

### 阶段 6：前端视图页面

---

### Task 6.1: 客户管理页面

**文件：** `ruoyi-ui/src/views/crm/customer/index.vue`

实现功能：
- 查询区：客户姓名、手机号、来源、等级、跟进状态
- 新增/编辑 Dialog（含所有字段，手机号唯一校验）
- 批量删除
- 导入/导出按钮
- 分配客户弹窗（选择用户）
- 放入公海/领取
- 表格字段：客户姓名、手机号、来源、等级、跟进状态、归属人、下次联系时间、创建时间
- 操作列：跟进记录、编辑、删除、分配、放入公海

```vue
<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="客户姓名" prop="customerName">
        <el-input v-model="queryParams.customerName" placeholder="请输入客户姓名" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="客户来源" prop="source">
        <el-select v-model="queryParams.source" placeholder="客户来源" clearable>
          <el-option v-for="d in dict.type.crm_customer_source" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="客户等级" prop="level">
        <el-select v-model="queryParams.level" placeholder="客户等级" clearable>
          <el-option v-for="d in dict.type.crm_customer_level" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="跟进状态" prop="followStatus">
        <el-select v-model="queryParams.followStatus" placeholder="跟进状态" clearable>
          <el-option v-for="d in dict.type.crm_follow_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['crm:customer:add']">新增</el-button>
      <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['crm:customer:edit']">修改</el-button>
      <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['crm:customer:remove']">删除</el-button>
      <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['crm:customer:export']">导出</el-button>
      <el-button type="info" plain icon="el-icon-upload2" size="mini" @click="handleImport" v-hasPermi="['crm:customer:import']">导入</el-button>
      <el-button type="primary" plain icon="el-icon-user" size="mini" :disabled="single" @click="handleAssign" v-hasPermi="['crm:customer:edit']">分配</el-button>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="customerList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="客户ID" align="center" prop="customerId" width="80" />
      <el-table-column label="客户姓名" align="center" prop="customerName" />
      <el-table-column label="手机号" align="center" prop="phone" width="120" />
      <el-table-column label="来源" align="center" prop="source" :formatter="sourceFormat" />
      <el-table-column label="等级" align="center" prop="level" :formatter="levelFormat" />
      <el-table-column label="跟进状态" align="center" prop="followStatus" :formatter="followStatusFormat" />
      <el-table-column label="归属人" align="center" prop="belongUserName" />
      <el-table-column label="下次联系" align="center" prop="nextContactTime" width="140" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="140" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="250">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-chat-line-square" @click="handleFollowup(scope.row)">跟进</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['crm:customer:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['crm:customer:remove']">删除</el-button>
          <el-dropdown size="mini" @command="(cmd) => handleCommand(cmd, scope.row)">
            <el-button size="mini" type="text" icon="el-icon-more">更多</el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="assign" icon="el-icon-user" v-hasPermi="['crm:customer:edit']">分配</el-dropdown-item>
              <el-dropdown-item command="pool" icon="el-icon-basketball" v-hasPermi="['crm:customer:edit']">放入公海</el-dropdown-item>
              <el-dropdown-item command="detail" icon="el-icon-document">详情</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
  </div>
</template>
```

后续在 `<script>` 部分实现所有方法，遵循标准 RuoYi Vue 模式。

---

### Task 6.2: 跟进记录页面

**文件：** `ruoyi-ui/src/views/crm/followup/index.vue`

- 查询区：客户名称、跟进方式、是否有效
- 列表页含：客户名称、联系时间、跟进方式、跟进内容、是否有效、创建人、操作
- 新增/编辑 Dialog
- 同时支持从客户详情页内嵌

---

### Task 6.3: 销售漏斗页面

**文件：** `ruoyi-ui/src/views/crm/pipeline/index.vue`

- 看板式卡片布局（Kanban），按阶段分列
- 拖拽改变阶段
- 每个阶段显示客户 + 金额 + 概率
- 阶段列：线索 → 意向 → 报价 → 成交 → 回款

---

### Task 6.4: 订单/合同管理页面

**文件：** `ruoyi-ui/src/views/crm/order/index.vue`, `ruoyi-ui/src/views/crm/contract/index.vue`

- 订单列表：编号、客户、金额、状态、创建时间
- 新增订单 Dialog（含选择产品、数量、自动计算金额）
- 合同列表：编号、名称、客户、金额、状态、签订日期
- 附件上传

---

### Task 6.5: 回款计划页面

**文件：** `ruoyi-ui/src/views/crm/payment/index.vue`

- 列表：订单号、客户、计划金额、实际金额、计划日期、状态
- 新增计划，标记已回款
- 逾期高亮显示

---

### Task 6.6: 产品管理页面

**文件：** `ruoyi-ui/src/views/crm/product/index.vue`

- 列表：名称、分类、价格、库存、状态
- 新增/编辑 Dialog
- 包含分类下拉选择

---

### Task 6.7: 产品分类页面

**文件：** `ruoyi-ui/src/views/crm/product/category.vue`

- 树形表格展示分类
- 新增/编辑/删除

---

### Task 6.8: 客户公海页面

**文件：** `ruoyi-ui/src/views/crm/pool/index.vue`

- 类似客户列表，但只展示 `isPool = '1'` 的客户
- 操作列：领取
- 查看公海日志

---

### Task 6.9: 数据看板页面

**文件：** `ruoyi-ui/src/views/crm/dashboard/index.vue`

- 顶部：卡片统计（今日新增客户、总客户数、本月成交金额、待跟进数）
- 中间行：客户来源分布（饼图）、客户等级分布（饼图）
- 中间行：销售漏斗图、个人业绩排行（柱状图）
- 底部：跟进趋势（折线图）
- 使用 ECharts（RuoYi 已内置）

---

### Task 6.10: 通知消息页面

**文件：** `ruoyi-ui/src/views/crm/notification/index.vue`

- 列表：标题、内容、类型、时间、已读/未读
- 点击标记已读
- 顶部导航显示未读数量

---

### 阶段 7：路由配置 & 菜单权限

---

### Task 7.1: 添加 CRM 路由

**文件：**
- 修改: `ruoyi-ui/src/router/index.js`

在前端路由中添加 CRM 模块的路由定义（dynamicRoutes）：

```javascript
{
  path: '/crm',
  component: Layout,
  hidden: true,
  children: [
    { path: 'customer', component: () => import('@/views/crm/customer/index'), name: 'CrmCustomer', meta: { title: '客户管理', icon: 'user' } },
    { path: 'customer/detail/:id', component: () => import('@/views/crm/customer/detail'), name: 'CrmCustomerDetail', meta: { title: '客户详情', activeMenu: '/crm/customer' } },
    { path: 'followup', component: () => import('@/views/crm/followup/index'), name: 'CrmFollowup', meta: { title: '跟进记录', icon: 'chat-line-square' } },
    { path: 'pipeline', component: () => import('@/views/crm/pipeline/index'), name: 'CrmPipeline', meta: { title: '销售漏斗', icon: 'trend-chart' } },
    { path: 'order', component: () => import('@/views/crm/order/index'), name: 'CrmOrder', meta: { title: '订单管理', icon: 'shopping-cart' } },
    { path: 'contract', component: () => import('@/views/crm/contract/index'), name: 'CrmContract', meta: { title: '合同管理', icon: 'document' } },
    { path: 'payment', component: () => import('@/views/crm/payment/index'), name: 'CrmPayment', meta: { title: '回款计划', icon: 'money' } },
    { path: 'product', component: () => import('@/views/crm/product/index'), name: 'CrmProduct', meta: { title: '产品管理', icon: 'goods' } },
    { path: 'product/category', component: () => import('@/views/crm/product/category'), name: 'CrmProductCategory', meta: { title: '产品分类', icon: 's-grid' } },
    { path: 'pool', component: () => import('@/views/crm/pool/index'), name: 'CrmPool', meta: { title: '客户公海', icon: 'basketball' } },
    { path: 'dashboard', component: () => import('@/views/crm/dashboard/index'), name: 'CrmDashboard', meta: { title: '数据看板', icon: 'data-board' } },
    { path: 'notification', component: () => import('@/views/crm/notification/index'), name: 'CrmNotification', meta: { title: '通知消息', icon: 'bell' } },
  ]
}
```

---

### Task 7.2: 创建菜单权限 SQL

**文件：**
- 创建: `sql/crm_menu.sql`

```sql
-- 智营CRM 菜单权限数据
-- 父菜单ID范围：2000-2099

-- 1. CRM 总目录
INSERT INTO sys_menu VALUES (2000, '客户管理', 0, 1, 'crm', NULL, NULL, 'crm', 'M', '0', '0', '1', 'user', 'admin', '2024-01-01 00:00:00', '', NULL, '');

-- 2. CRM 子菜单
INSERT INTO sys_menu VALUES (2010, '数据看板', 2000, 1, 'dashboard', 'crm/dashboard/index', NULL, 'crm:dashboard:list', 'C', '0', '0', '1', 'data-board', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO sys_menu VALUES (2020, '客户管理', 2000, 2, 'customer', 'crm/customer/index', NULL, 'crm:customer:list', 'C', '0', '0', '1', 'user', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO sys_menu VALUES (2030, '跟进记录', 2000, 3, 'followup', 'crm/followup/index', NULL, 'crm:followup:list', 'C', '0', '0', '1', 'chat-line-square', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO sys_menu VALUES (2040, '销售漏斗', 2000, 4, 'pipeline', 'crm/pipeline/index', NULL, 'crm:pipeline:list', 'C', '0', '0', '1', 'trend-chart', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO sys_menu VALUES (2050, '订单管理', 2000, 5, 'order', 'crm/order/index', NULL, 'crm:order:list', 'C', '0', '0', '1', 'shopping-cart', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO sys_menu VALUES (2060, '合同管理', 2000, 6, 'contract', 'crm/contract/index', NULL, 'crm:contract:list', 'C', '0', '0', '1', 'document', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO sys_menu VALUES (2070, '回款计划', 2000, 7, 'payment', 'crm/payment/index', NULL, 'crm:payment:list', 'C', '0', '0', '1', 'money', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO sys_menu VALUES (2080, '产品管理', 2000, 8, 'product', 'crm/product/index', NULL, 'crm:product:list', 'C', '0', '0', '1', 'goods', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO sys_menu VALUES (2090, '产品分类', 2000, 9, 'category', 'crm/product/category', NULL, 'crm:category:list', 'C', '0', '0', '1', 's-grid', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO sys_menu VALUES (2100, '客户公海', 2000, 10, 'pool', 'crm/pool/index', NULL, 'crm:pool:list', 'C', '0', '0', '1', 'basketball', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO sys_menu VALUES (2110, '通知消息', 2000, 11, 'notification', 'crm/notification/index', NULL, 'crm:notification:list', 'C', '0', '0', '1', 'bell', 'admin', '2024-01-01 00:00:00', '', NULL, '');

-- 3. 按钮权限
-- 客户管理按钮
INSERT INTO sys_menu VALUES (2021, '客户新增', 2020, 1, '#', '', NULL, 'crm:customer:add', 'F', '0', '0', '1', '', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO sys_menu VALUES (2022, '客户修改', 2020, 2, '#', '', NULL, 'crm:customer:edit', 'F', '0', '0', '1', '', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO sys_menu VALUES (2023, '客户删除', 2020, 3, '#', '', NULL, 'crm:customer:remove', 'F', '0', '0', '1', '', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO sys_menu VALUES (2024, '客户导出', 2020, 4, '#', '', NULL, 'crm:customer:export', 'F', '0', '0', '1', '', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO sys_menu VALUES (2025, '客户导入', 2020, 5, '#', '', NULL, 'crm:customer:import', 'F', '0', '0', '1', '', 'admin', '2024-01-01 00:00:00', '', NULL, '');

-- 跟进记录按钮
INSERT INTO sys_menu VALUES (2031, '跟进新增', 2030, 1, '#', '', NULL, 'crm:followup:add', 'F', '0', '0', '1', '', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO sys_menu VALUES (2032, '跟进修改', 2030, 2, '#', '', NULL, 'crm:followup:edit', 'F', '0', '0', '1', '', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO sys_menu VALUES (2033, '跟进删除', 2030, 3, '#', '', NULL, 'crm:followup:remove', 'F', '0', '0', '1', '', 'admin', '2024-01-01 00:00:00', '', NULL, '');

-- 销售漏斗按钮
INSERT INTO sys_menu VALUES (2041, '漏斗新增', 2040, 1, '#', '', NULL, 'crm:pipeline:add', 'F', '0', '0', '1', '', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO sys_menu VALUES (2042, '漏斗修改', 2040, 2, '#', '', NULL, 'crm:pipeline:edit', 'F', '0', '0', '1', '', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO sys_menu VALUES (2043, '漏斗删除', 2040, 3, '#', '', NULL, 'crm:pipeline:remove', 'F', '0', '0', '1', '', 'admin', '2024-01-01 00:00:00', '', NULL, '');

-- 订单按钮
INSERT INTO sys_menu VALUES (2051, '订单新增', 2050, 1, '#', '', NULL, 'crm:order:add', 'F', '0', '0', '1', '', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO sys_menu VALUES (2052, '订单修改', 2050, 2, '#', '', NULL, 'crm:order:edit', 'F', '0', '0', '1', '', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO sys_menu VALUES (2053, '订单删除', 2050, 3, '#', '', NULL, 'crm:order:remove', 'F', '0', '0', '1', '', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO sys_menu VALUES (2054, '订单导出', 2050, 4, '#', '', NULL, 'crm:order:export', 'F', '0', '0', '1', '', 'admin', '2024-01-01 00:00:00', '', NULL, '');

-- 合同按钮
INSERT INTO sys_menu VALUES (2061, '合同新增', 2060, 1, '#', '', NULL, 'crm:contract:add', 'F', '0', '0', '1', '', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO sys_menu VALUES (2062, '合同修改', 2060, 2, '#', '', NULL, 'crm:contract:edit', 'F', '0', '0', '1', '', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO sys_menu VALUES (2063, '合同删除', 2060, 3, '#', '', NULL, 'crm:contract:remove', 'F', '0', '0', '1', '', 'admin', '2024-01-01 00:00:00', '', NULL, '');

-- 回款按钮
INSERT INTO sys_menu VALUES (2071, '回款新增', 2070, 1, '#', '', NULL, 'crm:payment:add', 'F', '0', '0', '1', '', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO sys_menu VALUES (2072, '回款修改', 2070, 2, '#', '', NULL, 'crm:payment:edit', 'F', '0', '0', '1', '', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO sys_menu VALUES (2073, '回款删除', 2070, 3, '#', '', NULL, 'crm:payment:remove', 'F', '0', '0', '1', '', 'admin', '2024-01-01 00:00:00', '', NULL, '');

-- 产品按钮
INSERT INTO sys_menu VALUES (2081, '产品新增', 2080, 1, '#', '', NULL, 'crm:product:add', 'F', '0', '0', '1', '', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO sys_menu VALUES (2082, '产品修改', 2080, 2, '#', '', NULL, 'crm:product:edit', 'F', '0', '0', '1', '', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO sys_menu VALUES (2083, '产品删除', 2080, 3, '#', '', NULL, 'crm:product:remove', 'F', '0', '0', '1', '', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO sys_menu VALUES (2084, '产品导出', 2080, 4, '#', '', NULL, 'crm:product:export', 'F', '0', '0', '1', '', 'admin', '2024-01-01 00:00:00', '', NULL, '');

-- 分类按钮
INSERT INTO sys_menu VALUES (2091, '分类新增', 2090, 1, '#', '', NULL, 'crm:category:add', 'F', '0', '0', '1', '', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO sys_menu VALUES (2092, '分类修改', 2090, 2, '#', '', NULL, 'crm:category:edit', 'F', '0', '0', '1', '', 'admin', '2024-01-01 00:00:00', '', NULL, '');
INSERT INTO sys_menu VALUES (2093, '分类删除', 2090, 3, '#', '', NULL, 'crm:category:remove', 'F', '0', '0', '1', '', 'admin', '2024-01-01 00:00:00', '', NULL, '');

-- 公海按钮
INSERT INTO sys_menu VALUES (2101, '公海领取', 2100, 1, '#', '', NULL, 'crm:pool:claim', 'F', '0', '0', '1', '', 'admin', '2024-01-01 00:00:00', '', NULL, '');
```

---

### Task 7.3: MyBatis 配置（确保扫描到 crm 包）

**文件：**
- 修改: `ruoyi-admin/src/main/resources/application.yml`

确保 MyBatis 的 typeAliasesPackage 和 mapperLocations 包含了 crm 模块：

```yaml
mybatis:
  typeAliasesPackage: com.ruoyi.**.domain
  mapperLocations: classpath*:mapper/**/*.xml
```

确认现在的配置已经覆盖了 `com.ruoyi.crm.domain` 和 `classpath:mapper/crm/*.xml`（通配符 `**/*` 已经覆盖）。

---

### 阶段 8：MyBatis 配置与验证

---

### Task 8.1: 确保 Spring Boot 扫描到 crm 包

**文件：**
- 检查: `ruoyi-admin/src/main/java/com/ruoyi/RuoYiApplication.java`

确保 `@SpringBootApplication` 和 `@MapperScan` 覆盖了 `com.ruoyi.crm` 包。

如果 `@MapperScan` 当前是 `com.ruoyi.**.mapper` 则无需修改。

---

### Task 8.2: 编译验证

```bash
mvn compile -pl ruoyi-system -am
mvn compile -pl ruoyi-admin -am
```

确认无编译错误。

---

## 需求覆盖检查

| 需求 | 对应任务 |
|------|---------|
| 系统管理（用户/角色/部门/日志） | RuoYi 已有，无需额外实现 |
| 客户信息 CRUD | Task 2.1, 2.2, 2.3, 3.1, 3.2, 4.1, 5.1, 6.1 |
| 客户导入/导出 Excel | Task 4.1（import/export 方法）, Task 6.1 |
| 客户分配 | Task 4.1（/assign）, Task 6.1 |
| 客户查重（手机号/姓名） | Task 3.2（checkPhoneUnique/checkNameUnique） |
| 客户公海 | Task 4.1（/pool, /claim）, Task 6.8 |
| 跟进记录 | Task 2.1（CrmFollowup）, Task 4.2, Task 6.2 |
| 销售漏斗 | Task 2.1（CrmPipeline）, Task 4.2, Task 6.3 |
| 订单/合同管理 | Task 2.1（CrmOrder/CrmContract）, Task 4.2, Task 6.4 |
| 回款计划 | Task 2.1（CrmPaymentPlan）, Task 4.2, Task 6.5 |
| 待办提醒 | Task 3.1（Dashboard 统计接口）, Task 6.9 |
| 产品库 | Task 2.1（CrmProduct）, Task 4.2, Task 6.6 |
| 产品分类 | Task 2.1（CrmProductCategory）, Task 4.2, Task 6.7 |
| 数据统计（客户/销售/跟进） | Task 3.1（Dashboard Service）, Task 4.2, Task 6.9 |
| 可视化图表 | Task 6.9（ECharts） |
| 站内消息通知 | Task 2.1（CrmNotification）, Task 4.2, Task 6.10 |
| 系统品牌改造 | Task 1.2 |
