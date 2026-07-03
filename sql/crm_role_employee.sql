-- ===========================================================================
-- 员工/经理 角色 + 菜单权限
-- 使用场景：新建普通员工(role_id=2) + 已有部门经理(role_id=3) 补充菜单
-- 注意：所有 CRM 相关菜单ID 见 crm_menu.sql / approval_engine.sql / menu_restructure.sql
-- ===========================================================================

-- ===========================================================================
-- 1. 新增权限按钮：订单标记付款 crm:order:paid（用于按钮级区分）
-- ===========================================================================
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3035, '标记付款', 3018, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'crm:order:paid', '#', 'admin', sysdate());

-- ===========================================================================
-- 2. 新建角色：普通员工
--    data_scope=5: 仅本人数据
-- ===========================================================================
INSERT IGNORE INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time)
VALUES (2, '普通员工', 'employee', 2, '5', 1, 1, '0', '0', 'admin', sysdate());

-- ===========================================================================
-- 3. 更新经理角色 data_scope（确保为 4: 部门及以下）
-- ===========================================================================
UPDATE sys_role SET data_scope='4' WHERE role_id=3 AND data_scope!=4;

-- ===========================================================================
-- 4. 员工菜单权限 (role_id=2)
--    仅分配 CRUD + 基础查询，不含分配/公海/审批/标记付款/合并等管理操作
-- ===========================================================================
-- 根目录
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2000); -- 智营CRM

-- 数据看板
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2010); -- 数据看板

-- 客户管理 (M 目录)
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2020);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 3032); -- 客户列表
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2021); -- 客户查询
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2022); -- 客户新增
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2023); -- 客户修改
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2024); -- 客户删除
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2025); -- 客户导出
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2026); -- 客户导入
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2100); -- 客户公海
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2101); -- 公海查看
-- 争议: 员工可查看、新增，不可处理和仲裁
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 3023); -- 争议管理
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 3024); -- 争议查询
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 3025); -- 争议新增

-- 商机管理 (M 目录)
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 3030);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2030); -- 跟进记录
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2031); -- 跟进查询
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2032); -- 跟进新增
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2033); -- 跟进修改
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2034); -- 跟进删除
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2040); -- 销售管道
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2041); -- 漏斗查询
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2042); -- 漏斗新增
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2043); -- 漏斗修改
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2044); -- 漏斗删除
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2070); -- 回款计划
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2071); -- 回款查询
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2072); -- 回款新增
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2073); -- 回款修改
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2074); -- 回款删除

-- 订单管理 (M 目录)
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2050);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 3018); -- 订单列表
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2051); -- 订单查询
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2052); -- 订单新增
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2053); -- 订单修改
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2054); -- 订单删除

-- 合同管理 (M 目录)
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2060);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 3022); -- 合同列表
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2061); -- 合同查询
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2062); -- 合同新增
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2063); -- 合同修改
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2064); -- 合同删除

-- 产品管理 (M 目录)
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2080);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 3031); -- 产品列表
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2081); -- 产品查询
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2082); -- 产品新增
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2083); -- 产品修改
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2084); -- 产品删除
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2090); -- 产品分类
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2091); -- 分类查询
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2092); -- 分类新增
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2093); -- 分类修改
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2094); -- 分类删除

-- 审批流 (仅我发起的)
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 3010); -- 审批流管理 (M 目录)
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 3013); -- 我发起的

-- 系统设置
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 3040); -- 系统设置
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2110); -- 消息通知
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2111); -- 通知查询
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 3003); -- 操作日志
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 3005); -- 标签管理
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 3006); -- 标签查询
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 3007); -- 标签新增
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 3008); -- 标签修改
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 3009); -- 标签删除

-- AI 人工智能
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 3042); -- AI 人工智能 (M 目录)
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 3043); -- AI 控制台

-- ===========================================================================
-- 5. 经理菜单补全 (role_id=3)
--    role_id=3 已有大多数菜单，补充尚未分配的项
-- ===========================================================================
-- 新建的标记付款按钮
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (3, 3035);

-- 领取客户
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (3, 2029);

-- 终止合同
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (3, 3002);

-- 已处理（审批流子页）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (3, 3041);

-- ===========================================================================
-- 6. 管理员补充 (role_id=1)
-- ===========================================================================
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3035);
