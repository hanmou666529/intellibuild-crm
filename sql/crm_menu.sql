-- 智营CRM 菜单权限数据
-- sys_menu: (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)

-- 1. CRM 根目录
INSERT INTO sys_menu VALUES (2000, '智营CRM', 0, 5, 'crm', NULL, '', '', 1, 0, 'M', '0', '0', '', 'star', 'admin', sysdate(), '', null, 'CRM根目录');

-- 2. 子菜单（C 类型）
INSERT INTO sys_menu VALUES (2010, '数据看板', 2000, 1, 'dashboard', 'crm/dashboard/index', '', '', 1, 0, 'C', '0', '0', 'crm:dashboard:list', 'dashboard', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2020, '客户管理', 2000, 2, 'customer', 'crm/customer/index', '', '', 1, 0, 'C', '0', '0', 'crm:customer:list', 'user', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2030, '跟进记录', 2000, 3, 'followup', 'crm/followup/index', '', '', 1, 0, 'C', '0', '0', 'crm:followup:list', 'chat-line-square', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2040, '销售漏斗', 2000, 4, 'pipeline', 'crm/pipeline/index', '', '', 1, 0, 'C', '0', '0', 'crm:pipeline:list', 'trend-chart', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2050, '订单管理', 2000, 5, 'order', 'crm/order/index', '', '', 1, 0, 'C', '0', '0', 'crm:order:list', 'shopping-cart', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2060, '合同管理', 2000, 6, 'contract', 'crm/contract/index', '', '', 1, 0, 'C', '0', '0', 'crm:contract:list', 'document', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2070, '回款计划', 2000, 7, 'payment', 'crm/payment/index', '', '', 1, 0, 'C', '0', '0', 'crm:payment:list', 'money', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2080, '产品管理', 2000, 8, 'product', 'crm/product/index', '', '', 1, 0, 'C', '0', '0', 'crm:product:list', 'goods', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2090, '产品分类', 2000, 9, 'category', 'crm/product/category', '', '', 1, 0, 'C', '0', '0', 'crm:category:list', 's-grid', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2100, '客户公海', 2000, 10, 'pool', 'crm/pool/index', '', '', 1, 0, 'C', '0', '0', 'crm:pool:list', 'basketball', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2110, '消息通知', 2000, 11, 'notification', 'crm/notification/index', '', '', 1, 0, 'C', '0', '0', 'crm:notification:list', 'bell', 'admin', sysdate(), '', null, '');

-- 3. 按钮权限（F 类型）

-- 客户管理按钮
INSERT INTO sys_menu VALUES (2021, '客户查询', 2020, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:customer:query', '', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2022, '客户新增', 2020, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:customer:add', '', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2023, '客户修改', 2020, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:customer:edit', '', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2024, '客户删除', 2020, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:customer:remove', '', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2025, '客户导出', 2020, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:customer:export', '', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2026, '客户导入', 2020, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:customer:import', '', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2027, '客户分配', 2020, 7, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:customer:assign', '', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2028, '放入公海', 2020, 8, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:customer:pool', '', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2029, '领取客户', 2020, 9, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:customer:claim', '', 'admin', sysdate(), '', null, '');

-- 跟进记录按钮
INSERT INTO sys_menu VALUES (2031, '跟进查询', 2030, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:followup:query', '', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2032, '跟进新增', 2030, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:followup:add', '', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2033, '跟进修改', 2030, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:followup:edit', '', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2034, '跟进删除', 2030, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:followup:remove', '', 'admin', sysdate(), '', null, '');

-- 销售漏斗按钮
INSERT INTO sys_menu VALUES (2041, '漏斗查询', 2040, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:pipeline:query', '', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2042, '漏斗新增', 2040, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:pipeline:add', '', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2043, '漏斗修改', 2040, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:pipeline:edit', '', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2044, '漏斗删除', 2040, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:pipeline:remove', '', 'admin', sysdate(), '', null, '');

-- 订单按钮
INSERT INTO sys_menu VALUES (2051, '订单查询', 2050, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:order:query', '', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2052, '订单新增', 2050, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:order:add', '', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2053, '订单修改', 2050, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:order:edit', '', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2054, '订单删除', 2050, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:order:remove', '', 'admin', sysdate(), '', null, '');

-- 合同按钮
INSERT INTO sys_menu VALUES (2061, '合同查询', 2060, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:contract:query', '', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2062, '合同新增', 2060, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:contract:add', '', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2063, '合同修改', 2060, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:contract:edit', '', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2064, '合同删除', 2060, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:contract:remove', '', 'admin', sysdate(), '', null, '');

-- 回款按钮
INSERT INTO sys_menu VALUES (2071, '回款查询', 2070, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:payment:query', '', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2072, '回款新增', 2070, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:payment:add', '', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2073, '回款修改', 2070, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:payment:edit', '', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2074, '回款删除', 2070, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:payment:remove', '', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2075, '标记回款', 2070, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:payment:edit', '', 'admin', sysdate(), '', null, '');

-- 产品按钮
INSERT INTO sys_menu VALUES (2081, '产品查询', 2080, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:product:query', '', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2082, '产品新增', 2080, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:product:add', '', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2083, '产品修改', 2080, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:product:edit', '', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2084, '产品删除', 2080, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:product:remove', '', 'admin', sysdate(), '', null, '');

-- 分类按钮
INSERT INTO sys_menu VALUES (2091, '分类查询', 2090, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:category:query', '', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2092, '分类新增', 2090, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:category:add', '', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2093, '分类修改', 2090, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:category:edit', '', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2094, '分类删除', 2090, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:category:remove', '', 'admin', sysdate(), '', null, '');

-- 公海按钮
INSERT INTO sys_menu VALUES (2101, '公海查看', 2100, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:pool:list', '', 'admin', sysdate(), '', null, '');

-- 通知按钮
INSERT INTO sys_menu VALUES (2111, '通知查询', 2110, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'crm:notification:query', '', 'admin', sysdate(), '', null, '');
