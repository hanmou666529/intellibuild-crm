-- 智营CRM 字典数据
-- sys_dict_type: (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
-- sys_dict_data: (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)

INSERT INTO sys_dict_type VALUES (100, '客户来源', 'crm_customer_source', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_type VALUES (101, '客户等级', 'crm_customer_level', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_type VALUES (102, '跟进方式', 'crm_followup_mode', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_type VALUES (103, '跟进状态', 'crm_follow_status', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_type VALUES (104, '销售阶段', 'crm_pipeline_stage', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_type VALUES (105, '回款方式', 'crm_payment_method', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_type VALUES (106, '合同状态', 'crm_contract_status', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_type VALUES (107, '订单状态', 'crm_order_status', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_type VALUES (108, '通知类型', 'crm_notification_type', '0', 'admin', sysdate(), '', null, '');

-- 客户来源
INSERT INTO sys_dict_data VALUES (200, 1, '电话咨询', 'phone', 'crm_customer_source', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (201, 2, '在线咨询', 'online', 'crm_customer_source', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (202, 3, '市场活动', 'marketing', 'crm_customer_source', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (203, 4, '转介绍', 'referral', 'crm_customer_source', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (204, 5, '广告投放', 'ad', 'crm_customer_source', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (205, 6, '合作伙伴', 'partner', 'crm_customer_source', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (206, 7, '其他', 'other', 'crm_customer_source', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '');

-- 客户等级
INSERT INTO sys_dict_data VALUES (210, 1, '高潜力', 'high', 'crm_customer_level', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (211, 2, '中潜力', 'medium', 'crm_customer_level', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (212, 3, '低潜力', 'low', 'crm_customer_level', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (213, 4, '已成交', 'won', 'crm_customer_level', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');

-- 跟进方式
INSERT INTO sys_dict_data VALUES (220, 1, '电话', 'phone', 'crm_followup_mode', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (221, 2, '上门拜访', 'visit', 'crm_followup_mode', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (222, 3, '微信', 'wechat', 'crm_followup_mode', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (223, 4, '邮件', 'email', 'crm_followup_mode', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '');

-- 跟进状态
INSERT INTO sys_dict_data VALUES (230, 1, '待跟进', 'pending', 'crm_follow_status', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (231, 2, '跟进中', 'following', 'crm_follow_status', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (232, 3, '已成交', 'won', 'crm_follow_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (233, 4, '已流失', 'lost', 'crm_follow_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '');

-- 销售阶段
INSERT INTO sys_dict_data VALUES (240, 1, '线索', 'clue', 'crm_pipeline_stage', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (241, 2, '意向', 'intent', 'crm_pipeline_stage', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (242, 3, '报价', 'quote', 'crm_pipeline_stage', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (243, 4, '成交', 'deal', 'crm_pipeline_stage', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (244, 5, '回款', 'payment', 'crm_pipeline_stage', '', '', 'N', '0', 'admin', sysdate(), '', null, '');

-- 回款方式
INSERT INTO sys_dict_data VALUES (250, 1, '银行转账', 'bank', 'crm_payment_method', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (251, 2, '支付宝', 'alipay', 'crm_payment_method', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (252, 3, '微信支付', 'wxpay', 'crm_payment_method', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (253, 4, '现金', 'cash', 'crm_payment_method', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '');

-- 合同状态
INSERT INTO sys_dict_data VALUES (260, 1, '待审核', '0', 'crm_contract_status', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (261, 2, '已生效', '1', 'crm_contract_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (262, 3, '已完成', '2', 'crm_contract_status', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (263, 4, '已终止', '3', 'crm_contract_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '');

-- 订单状态
INSERT INTO sys_dict_data VALUES (270, 1, '待付款', '0', 'crm_order_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (271, 2, '已付款', '1', 'crm_order_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (272, 3, '已完成', '2', 'crm_order_status', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (273, 4, '已取消', '3', 'crm_order_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '');

-- 通知类型
INSERT INTO sys_dict_data VALUES (280, 1, '客户分配', 'assign', 'crm_notification_type', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (281, 2, '待办提醒', 'todo', 'crm_notification_type', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (282, 3, '逾期提醒', 'overdue', 'crm_notification_type', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (283, 4, '系统通知', 'system', 'crm_notification_type', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '');

-- 订单状态追加：已签约
INSERT INTO sys_dict_data VALUES (274, 5, '已签约', '4', 'crm_order_status', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '');

-- 回款状态
INSERT INTO sys_dict_type VALUES (109, '回款状态', 'crm_payment_status', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (290, 1, '未付', '0', 'crm_payment_status', '', 'warning', 'Y', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (291, 2, '已付', '1', 'crm_payment_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');
