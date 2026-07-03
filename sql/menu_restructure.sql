-- ===========================================================================
-- 菜单结构优化：CRM 模块重组
-- 分组：数据看板/客户管理/商机管理/订单管理/合同管理/审批管理/产品管理/系统设置
-- ===========================================================================

-- ===========================================================================
-- A. 新建目录菜单
-- ===========================================================================

-- A1. 商机管理 (order=3, 位于客户管理之后)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3030, '商机管理', 2000, 3, 'opportunity', '', 'CrmOpportunity', '', 1, 0, 'M', '0', '0', '', 'trend-chart', 'admin', sysdate());

-- A2. 客户列表 (2020 客户管理改为 M 后需要的子页)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3032, '客户列表', 2020, 1, 'index', 'crm/customer/index', 'CrmCustomerList', '', 1, 0, 'C', '0', '0', 'crm:customer:list', 'list', 'admin', sysdate());

-- A3. 产品列表 (2080 产品管理改为 M 后需要的子页)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3031, '产品列表', 2080, 1, 'index', 'crm/product/index', 'CrmProductList', '', 1, 0, 'C', '0', '0', 'crm:product:list', 'list', 'admin', sysdate());

-- A4. 系统设置 (order=8)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3040, '系统设置', 2000, 8, 'settings', '', 'CrmSettings', '', 1, 0, 'M', '0', '0', '', 'setting', 'admin', sysdate());

-- ===========================================================================
-- B. 类型转换 C→M
-- ===========================================================================

-- B1. 客户管理: C→M
UPDATE sys_menu SET menu_type='M', component=NULL, perms='' WHERE menu_id=2020;

-- B2. 产品管理: C→M
UPDATE sys_menu SET menu_type='M', component=NULL, perms='' WHERE menu_id=2080;

-- ===========================================================================
-- C. 新建争议/合并菜单 (Phase 3 缺失)
-- ===========================================================================

-- C1. 争议管理 (parent=2020 客户管理)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3023, '争议管理', 2020, 3, 'dispute', 'crm/dispute/index', 'CrmDispute', '', 1, 0, 'C', '0', '0', 'crm:dispute:list', 'exclamation-triangle', 'admin', sysdate());

-- C2. 争议按钮权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3024, '争议查询', 3023, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'crm:dispute:query', '#', 'admin', sysdate());
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3025, '争议新增', 3023, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'crm:dispute:add', '#', 'admin', sysdate());
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3026, '争议处理', 3023, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'crm:dispute:handle', '#', 'admin', sysdate());
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3027, '争议仲裁', 3023, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'crm:dispute:arbitrate', '#', 'admin', sysdate());

-- C3. 客户合并按钮 (parent=3032 客户列表)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3028, '客户合并', 3032, 10, '', '', '', '', 1, 0, 'F', '0', '0', 'crm:customer:merge', '#', 'admin', sysdate());

-- ===========================================================================
-- D. 搬迁菜单 (UPDATE parent_id)
-- ===========================================================================

-- D1. 客户公海 2100 → 客户管理 2020
UPDATE sys_menu SET parent_id=2020, order_num=2 WHERE menu_id=2100;

-- D2. 跟进记录 2030 → 商机管理 3030
UPDATE sys_menu SET parent_id=3030, order_num=1 WHERE menu_id=2030;

-- D3. 销售漏斗 2040 → 商机管理 3030 + 改名销售管道
UPDATE sys_menu SET parent_id=3030, order_num=2, menu_name='销售管道' WHERE menu_id=2040;

-- D4. 回款计划 2070 → 商机管理 3030
UPDATE sys_menu SET parent_id=3030, order_num=3 WHERE menu_id=2070;

-- D5. 审批流管理 3010 → 智营CRM 2000 (从合同下提级)
UPDATE sys_menu SET parent_id=2000, order_num=6 WHERE menu_id=3010;

-- D6. 产品分类 2090 → 产品管理 2080
UPDATE sys_menu SET parent_id=2080, order_num=2 WHERE menu_id=2090;

-- D7. 消息通知 2110 → 系统设置 3040
UPDATE sys_menu SET parent_id=3040, order_num=1 WHERE menu_id=2110;

-- D8. 操作日志 3003 → 系统设置 3040
UPDATE sys_menu SET parent_id=3040, order_num=2 WHERE menu_id=3003;

-- D9. 标签管理 3005 → 系统设置 3040
UPDATE sys_menu SET parent_id=3040, order_num=3 WHERE menu_id=3005;

-- D10. 客户按钮 2021-2029 → 客户列表 3032
UPDATE sys_menu SET parent_id=3032 WHERE menu_id BETWEEN 2021 AND 2029;

-- D11. 产品按钮 2081-2084 → 产品列表 3031
UPDATE sys_menu SET parent_id=3031 WHERE menu_id BETWEEN 2081 AND 2084;

-- ===========================================================================
-- E. 全局重新排序 (智营CRM 根级)
-- ===========================================================================

UPDATE sys_menu SET order_num=1 WHERE menu_id=2010;  -- 数据看板
UPDATE sys_menu SET order_num=2 WHERE menu_id=2020;  -- 客户管理
-- 3030 已设 order_num=3 (INSERT 时)
UPDATE sys_menu SET order_num=4 WHERE menu_id=2050;  -- 订单管理
UPDATE sys_menu SET order_num=5 WHERE menu_id=2060;  -- 合同管理
-- 3010 已设 order_num=6 (D5 时)
UPDATE sys_menu SET order_num=7 WHERE menu_id=2080;  -- 产品管理
-- 3040 已设 order_num=8 (INSERT 时)

-- ===========================================================================
-- F. 角色菜单关联 (admin 角色 role_id=1)
-- ===========================================================================

INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3023);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3024);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3025);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3026);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3027);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3028);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3030);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3031);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3032);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3040);
