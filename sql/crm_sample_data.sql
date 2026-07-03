-- =============================================
-- 智营CRM 示例数据
-- =============================================

-- 1. 产品分类（带树形层级）
INSERT INTO crm_product_category (category_id, parent_id, category_name, order_num, remark, create_by, create_time, update_by, update_time) VALUES
(1, 0, '软件产品', 1, '', 'admin', sysdate(), '', null),
(2, 0, '硬件设备', 2, '', 'admin', sysdate(), '', null),
(3, 0, '咨询服务', 3, '', 'admin', sysdate(), '', null),
(4, 1, 'CRM系统', 1, '', 'admin', sysdate(), '', null),
(5, 1, 'ERP系统', 2, '', 'admin', sysdate(), '', null),
(6, 1, 'OA系统', 3, '', 'admin', sysdate(), '', null),
(7, 2, '服务器', 1, '', 'admin', sysdate(), '', null),
(8, 2, '网络设备', 2, '', 'admin', sysdate(), '', null),
(9, 2, '终端设备', 3, '', 'admin', sysdate(), '', null);

-- 2. 产品
INSERT INTO crm_product (product_id, product_name, category_id, price, cost_price, unit, stock, description, status, remark, del_flag, create_by, create_time, update_by, update_time) VALUES
(1, '智营CRM-标准版', 4, 98000.00, 50000.00, '套', 999, '适合中小企业的CRM系统，包含客户管理、跟进、订单、合同等核心模块', '0', '', '0', 'admin', sysdate(), '', null),
(2, '智营CRM-企业版', 4, 198000.00, 100000.00, '套', 999, '适合大型企业的CRM系统，包含全部模块+自定义报表+API接口', '0', '', '0', 'admin', sysdate(), '', null),
(3, '智营ERP-标准版', 5, 158000.00, 80000.00, '套', 999, '适合制造企业的ERP系统，包含进销存、生产、财务等模块', '0', '', '0', 'admin', sysdate(), '', null),
(4, 'OA办公系统', 6, 58000.00, 30000.00, '套', 999, '企业协同办公系统，包含流程审批、考勤、文档等功能', '0', '', '0', 'admin', sysdate(), '', null),
(5, '云服务器 ECS-4核8G', 7, 5999.00, 4000.00, '台/年', 50, '4核8G内存，200G SSD，5M带宽', '0', '', '0', 'admin', sysdate(), '', null),
(6, '云服务器 ECS-8核16G', 7, 12999.00, 9000.00, '台/年', 30, '8核16G内存，500G SSD，10M带宽', '0', '', '0', 'admin', sysdate(), '', null),
(7, '企业路由器 RG-EG3000', 8, 8800.00, 6000.00, '台', 100, '千兆企业路由器，支持VPN、AC管理', '0', '', '0', 'admin', sysdate(), '', null),
(8, '企业级交换机 RG-S2928G', 8, 3500.00, 2200.00, '台', 200, '24口千兆交换机，4个SFP光口', '0', '', '0', 'admin', sysdate(), '', null),
(9, '智能门禁一体机', 9, 2800.00, 1800.00, '台', 500, '人脸识别+IC卡+密码，支持远程开门', '0', '', '0', 'admin', sysdate(), '', null),
(10, 'IT系统运维咨询', 3, 50000.00, 30000.00, '次', 999, '企业IT系统健康检查、架构优化、安全评估服务', '0', '', '0', 'admin', sysdate(), '', null),
(11, '数字化转型咨询', 3, 120000.00, 70000.00, '次', 999, '企业数字化转型战略规划、流程再造、技术选型咨询', '0', '', '0', 'admin', sysdate(), '', null),
(12, '数据迁移服务', 3, 30000.00, 15000.00, '次', 999, '支持Oracle/SQL Server/MySQL等数据库迁移上云服务', '0', '', '0', 'admin', sysdate(), '', null);

-- 3. 客户（已分配）
INSERT INTO crm_customer (customer_id, customer_name, phone, sex, source, level, industry, tags, follow_status, next_contact_time, belong_user_id, belong_dept_id, is_pool, enter_pool_time, last_follow_time, address, email, company, position, wechat, qq, remark, del_flag, create_by, create_time) VALUES
(1, '张伟', '13800138001', '1', 'crm_source_online', 'crm_level_vip', 'crm_industry_tech', '高意向,制造业', '1', '2026-08-01 10:00:00', 1, 103, '0', null, '2026-07-01 09:00:00', '北京市海淀区中关村大街1号', 'zhangwei@example.com', '华为技术有限公司', '采购总监', 'zhangwei1985', '10001', '华为采购部门负责人，有数字化转型需求', '0', 'admin', '2026-06-15 08:00:00'),
(2, '李芳', '13800138002', '2', 'crm_source_referral', 'crm_level_high', 'crm_industry_finance', '高净值,金融', '1', '2026-07-20 14:00:00', 1, 103, '0', null, '2026-06-30 15:30:00', '上海市浦东新区陆家嘴环路166号', 'lifang@example.com', '招商银行', 'IT部总经理', 'lifang_bank', '20002', '招商银行IT部负责人，对OA系统有采购意向', '0', 'admin', '2026-06-10 10:00:00'),
(3, '王强', '13800138003', '1', 'crm_source_exhibition', 'crm_level_medium', 'crm_industry_edu', '教育行业,有预算', '0', '2026-07-25 09:00:00', 2, 105, '0', null, '2026-07-01 11:00:00', '广州市天河区五山路381号', 'wangqiang@edu.cn', '华南理工大学', '信息中心主任', 'wq_scut', '30003', '华工信息中心主任，正在选型CRM系统', '0', 'admin', '2026-06-20 09:00:00'),
(4, '赵敏', '13800138004', '2', 'crm_source_online', 'crm_level_vip', 'crm_industry_medical', '医疗,急迫', '2', null, 1, 103, '0', null, '2026-06-28 16:00:00', '深圳市南山区科技园南区', 'zhaomin@hospital.com', '华大基因', '信息化总监', 'zhaomin_bgi', '40004', '已成交客户，购买智营CRM企业版', '0', 'admin', '2026-05-01 08:00:00'),
(5, '孙磊', '13800138005', '1', 'crm_source_ads', 'crm_level_low', 'crm_industry_manufacturing', '制造业,初步接触', '0', '2026-08-05 10:00:00', 2, 105, '0', null, '2026-07-01 14:00:00', '杭州市滨江区网商路599号', 'sunlei@byd.com', '比亚迪', 'IT经理', 'sunlei_byd', '50005', '比亚迪IT经理，咨询ERP系统方案', '0', 'admin', '2026-06-25 10:00:00'),
(6, '陈曦', '13800138006', '2', 'crm_source_referral', 'crm_level_medium', 'crm_industry_retail', '零售,连锁', '0', '2026-07-28 15:00:00', 1, 103, '0', null, null, '成都市锦江区红星路三段1号', 'chenxi@scly.com', '四川郎酒集团', '信息部主管', 'chenxi_lang', '60006', '郎酒集团信息部主管，关注CRM+ERP整合方案', '0', 'admin', '2026-06-18 14:00:00'),
(7, '刘洋', '13800138007', '1', 'crm_source_online', 'crm_level_high', 'crm_industry_tech', '互联网,A轮融资', '1', '2026-07-18 11:00:00', 1, 103, '0', null, '2026-07-01 10:30:00', '北京市朝阳区望京SOHO T1', 'liuyang@techstar.com', '星辰科技', 'COO', 'liuyang_star', '70007', '星辰科技COO，创业公司需要全套信息化方案', '0', 'admin', '2026-06-12 11:00:00'),
(8, '杨雪', '13800138008', '2', 'crm_source_ads', 'crm_level_low', 'crm_industry_edu', '培训,咨询', '3', null, 1, 103, '0', null, '2026-05-20 09:00:00', '武汉市洪山区珞喻路1037号', 'yangxue@hust.com', '华中科技大学', '行政主任', 'yangxue_hust', '80008', '已流失客户，因预算不足未成交', '0', 'admin', '2026-04-01 10:00:00');

-- 4. 客户（公海客户）
INSERT INTO crm_customer (customer_id, customer_name, phone, sex, source, level, industry, tags, follow_status, next_contact_time, belong_user_id, belong_dept_id, is_pool, enter_pool_time, last_follow_time, address, email, company, position, wechat, qq, remark, del_flag, create_by, create_time) VALUES
(9, '黄磊', '13800138009', '1', 'crm_source_online', 'crm_level_low', 'crm_industry_tech', '软件,待验证', '0', null, null, null, '1', '2026-06-30 08:00:00', null, '南京市雨花台区软件大道168号', 'huanglei@njsoft.com', '南京软件园', '项目经理', 'huanglei_nj', '90009', '通过官网注册，尚未分配归属人', '0', 'admin', '2026-06-28 16:00:00'),
(10, '周杰', '13800138010', '1', 'crm_source_exhibition', 'crm_level_medium', 'crm_industry_manufacturing', '展会,主动咨询', '0', null, null, null, '1', '2026-06-29 10:00:00', null, '苏州市苏州工业园区星湖街328号', 'zhoujie@szepc.com', '苏州工业园区管委会', '信息化专员', 'zhoujie_sz', '10010', '展会收集名片，待跟进', '0', 'admin', '2026-06-26 09:00:00'),
(11, '林诗雅', '13800138011', '2', 'crm_source_referral', 'crm_level_high', 'crm_industry_medical', '医药,大客户', '0', null, null, null, '1', '2026-06-27 14:00:00', null, '重庆市渝中区民族路188号', 'linsy@cqpharm.com', '重庆医药集团', 'IT总监', 'linsy_cq', '11011', '老客户推荐，有明确采购意向', '0', 'admin', '2026-06-25 15:00:00');

-- 5. 跟进记录
INSERT INTO crm_followup (followup_id, customer_id, contact_time, followup_mode, content, next_contact_time, is_effective, del_flag, create_by, create_time) VALUES
(1, 1, '2026-07-01 09:00:00', 'crm_followup_visit', '拜访华为采购总监张伟，演示智营CRM企业版功能，重点展示了定制报表和API接口能力。客户对系统集成能力表示认可，要求提供技术方案书。', '2026-08-01 10:00:00', '1', '0', 'admin', '2026-07-01 09:30:00'),
(2, 2, '2026-06-30 15:30:00', 'crm_followup_phone', '电话沟通招商银行IT部李芳总经理，了解其OA系统需求。客户希望系统支持信创环境，需提供国产化适配方案。已安排下周上门演示。', '2026-07-20 14:00:00', '1', '0', 'admin', '2026-06-30 16:00:00'),
(3, 3, '2026-07-01 11:00:00', 'crm_followup_wechat', '微信回复华南理工王强主任关于CRM系统的功能咨询，发送了产品资料和报价单。客户反馈价格在预算范围内，下周安排校内评审。', '2026-07-25 09:00:00', '1', '0', 'ry', '2026-07-01 11:30:00'),
(4, 4, '2026-06-20 14:00:00', 'crm_followup_visit', '华大基因赵敏总监签署智营CRM企业版合同，合同金额19.8万元。实施计划已确认，预计7月中旬上线。', null, '1', '0', 'admin', '2026-06-20 15:00:00'),
(5, 5, '2026-07-01 14:00:00', 'crm_followup_phone', '电话联系比亚迪孙磊经理，了解ERP需求详情。客户需要进销存+生产管理模块，已发送定制方案和报价。', '2026-08-05 10:00:00', '1', '0', 'ry', '2026-07-01 14:30:00'),
(6, 6, '2026-06-30 10:00:00', 'crm_followup_email', '邮件回复郎酒集团陈曦主管关于CRM+ERP整合方案的技术问题，附件发送了整合架构图。', '2026-07-28 15:00:00', '1', '0', 'admin', '2026-06-30 10:30:00'),
(7, 7, '2026-07-01 10:30:00', 'crm_followup_visit', '拜访星辰科技COO刘洋，公司刚完成A轮融资，需要全套信息化方案。推荐CRM企业版+OA系统打包方案，总报价25.6万元。客户表示会尽快内部决策。', '2026-07-18 11:00:00', '1', '0', 'admin', '2026-07-01 11:00:00'),
(8, 1, '2026-06-20 14:00:00', 'crm_followup_phone', '首次电话联系华为张伟总监，简单介绍了公司产品线，客户表示对CRM系统感兴趣，约了上门演示时间。', '2026-07-01 09:00:00', '1', '0', 'admin', '2026-06-20 14:30:00'),
(9, 7, '2026-06-25 16:00:00', 'crm_followup_wechat', '微信初步沟通星辰科技需求，了解公司背景和融资情况，确认了上门拜访时间。', '2026-07-01 10:30:00', '1', '0', 'admin', '2026-06-25 16:30:00'),
(10, 8, '2026-05-10 09:00:00', 'crm_followup_visit', '拜访华中科技大学杨雪主任，演示OA系统。客户表示学校经费审批流程复杂，需走招标流程。', '2026-06-01 10:00:00', '1', '0', 'admin', '2026-05-10 10:00:00'),
(11, 8, '2026-06-01 10:00:00', 'crm_followup_phone', '跟进华中科技大学杨雪主任招标进展。客户反馈因预算压缩，本项目被推迟到明年，暂无法推进。', null, '1', '0', 'admin', '2026-06-01 10:30:00');

-- 6. 销售漏斗
INSERT INTO crm_pipeline (pipeline_id, customer_id, order_id, stage, amount, probability, expected_close_date, remark, del_flag, create_by, create_time) VALUES
(1, 1, null, 'quote', 198000.00, 60, '2026-08-15 00:00:00', '华为智营CRM企业版，方案已提交，正在内部评审', '0', 'admin', '2026-07-01 09:00:00'),
(2, 2, null, 'intent', 58000.00, 35, '2026-08-30 00:00:00', '招商银行OA系统，有信创需求，需提供国产化方案', '0', 'admin', '2026-06-30 15:00:00'),
(3, 3, null, 'clue', 98000.00, 25, '2026-09-01 00:00:00', '华南理工CRM系统，正在选型阶段，需要校内评审', '0', 'ry', '2026-07-01 11:00:00'),
(4, 5, null, 'intent', 158000.00, 30, '2026-09-15 00:00:00', '比亚迪ERP系统，需求明确，已发送定制方案', '0', 'ry', '2026-07-01 14:00:00'),
(5, 6, null, 'clue', 256000.00, 20, '2026-09-30 00:00:00', '郎酒集团CRM+ERP整合方案，预算较大', '0', 'admin', '2026-06-30 10:00:00'),
(6, 7, null, 'quote', 256000.00, 55, '2026-08-10 00:00:00', '星辰科技CRM+OA打包方案，A轮融资后预算充足', '0', 'admin', '2026-07-01 10:30:00'),
(7, 4, null, 'deal', 198000.00, 100, '2026-06-20 00:00:00', '华大基因已签约，进入实施阶段', '0', 'admin', '2026-06-20 14:00:00');

-- 7. 合同
INSERT INTO crm_contract (contract_id, contract_no, contract_name, customer_id, order_id, amount, sign_date, start_date, end_date, status, remark, del_flag, create_by, create_time) VALUES
(1, 'CT20260620001', '华大基因-智营CRM企业版采购合同', 4, null, 198000.00, '2026-06-20 00:00:00', '2026-06-20 00:00:00', '2026-12-31 00:00:00', '1', '智营CRM企业版，含3年许可及实施服务', '0', 'admin', '2026-06-20 14:00:00');

-- 8. 订单
INSERT INTO crm_order (order_id, order_no, customer_id, contract_id, total_amount, discount_amount, actual_amount, status, paid_amount, remark, del_flag, create_by, create_time) VALUES
(1, 'ORD20260620001', 4, 1, 198000.00, 0.00, 198000.00, '1', 198000.00, '华大基因-智营CRM企业版订单（已全额付款）', '0', 'admin', '2026-06-20 14:00:00'),
(2, 'ORD20260701001', 7, null, 256000.00, 10000.00, 246000.00, '0', 0.00, '星辰科技CRM+OA打包订单（待付款）', '0', 'admin', '2026-07-01 15:00:00');

-- 9. 订单明细
INSERT INTO crm_order_item (item_id, order_id, product_id, product_name, product_price, quantity, subtotal, remark) VALUES
(1, 1, 2, '智营CRM-企业版', 198000.00, 1, 198000.00, '含3年许可'),
(2, 2, 2, '智营CRM-企业版', 198000.00, 1, 198000.00, ''),
(3, 2, 4, 'OA办公系统', 58000.00, 1, 58000.00, '');

-- 10. 回款计划
INSERT INTO crm_payment_plan (plan_id, order_id, contract_id, plan_amount, actual_amount, plan_date, actual_date, status, payment_method, remark, del_flag, create_by, create_time) VALUES
(1, 1, 1, 198000.00, 198000.00, '2026-06-20 00:00:00', '2026-06-20 16:00:00', '1', 'crm_payment_transfer', '华大基因全款到账', '0', 'admin', '2026-06-20 14:00:00'),
(2, 2, null, 256000.00, 0.00, '2026-07-31 00:00:00', null, '0', null, '星辰科技合同约定付款日期', '0', 'admin', '2026-07-01 15:00:00');

-- 11. 通知消息
INSERT INTO crm_notification (notification_id, title, content, type, to_user_id, to_dept_id, biz_type, biz_id, is_read, read_time, del_flag, create_by, create_time) VALUES
(1, '客户分配通知', '系统管理员将客户"张伟"分配给您，请及时跟进。', 'assign', 1, null, 'customer', 1, '1', '2026-06-15 09:00:00', '0', 'admin', '2026-06-15 08:00:00'),
(2, '客户分配通知', '系统管理员将客户"李芳"分配给您，请及时跟进。', 'assign', 1, null, 'customer', 2, '1', '2026-06-10 11:00:00', '0', 'admin', '2026-06-10 10:00:00'),
(3, '待办提醒', '客户"张伟"的下次联系时间（2026-08-01）即将到来，请提前准备。', 'todo', 1, null, 'customer', 1, '0', null, '0', 'admin', '2026-07-01 09:00:00'),
(4, '回款逾期提醒', '订单ORD20260701001（星辰科技）回款计划已逾期，请尽快催款。', 'overdue', 1, null, 'payment', 2, '0', null, '0', 'admin', '2026-08-01 00:00:00'),
(5, '系统通知', '智营CRM V3.0版本已发布，新增数据分析看板功能。', 'system', null, 103, null, null, '0', null, '0', 'admin', '2026-07-01 08:00:00'),
(6, '订单创建通知', '客户"星辰科技"的新订单ORD20260701001已创建，金额256,000元。', 'system', 1, null, 'order', 2, '0', null, '0', 'admin', '2026-07-01 15:00:00'),
(7, '合同生效通知', '合同"华大基因-智营CRM企业版采购合同"已生效，金额198,000元。', 'system', 1, null, 'contract', 1, '1', '2026-06-21 09:00:00', '0', 'admin', '2026-06-20 16:00:00'),
(8, '客户分配通知', '系统管理员将客户"孙磊"分配给ry，请及时跟进。', 'assign', 2, null, 'customer', 5, '0', null, '0', 'admin', '2026-06-25 10:00:00'),
(9, '客户分配通知', '系统管理员将客户"王强"分配给ry，请及时跟进。', 'assign', 2, null, 'customer', 3, '0', null, '0', 'admin', '2026-06-20 09:00:00'),
(10, '待办提醒', '客户"王强"的下次联系时间（2026-07-25）即将到来，请提前准备。', 'todo', 2, null, 'customer', 3, '0', null, '0', 'admin', '2026-07-01 11:00:00');

-- 12. 公海日志
INSERT INTO crm_customer_pool_log (log_id, customer_id, action, from_user_id, to_user_id, reason, create_by, create_time) VALUES
(1, 9, '0', 1, null, '客户信息不完整，需补充后重新分配', 'admin', '2026-06-30 08:00:00'),
(2, 10, '0', 1, null, '展会注册客户，暂未分配归属人', 'admin', '2026-06-29 10:00:00'),
(3, 11, '0', null, null, '老客户推荐，暂放入公海统一分配', 'admin', '2026-06-27 14:00:00');
