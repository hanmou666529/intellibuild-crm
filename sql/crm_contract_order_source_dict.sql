-- 订单来源字典
INSERT INTO sys_dict_type VALUES (111, '订单来源', 'crm_order_source', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (300, 1, '手动创建', 'manual', 'crm_order_source', '', 'info', 'Y', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES (301, 2, '合同生成', 'contract', 'crm_order_source', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');
