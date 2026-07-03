-- 审批流引擎 建表 + 菜单 + 字典
-- ===========================================================================

-- 0. 字典数据
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time)
VALUES ('审批状态', 'crm_approval_status', '0', 'admin', sysdate());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time)
VALUES (1, '审批中', '0', 'crm_approval_status', '', 'warning', 'N', '0', 'admin', sysdate());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time)
VALUES (2, '已通过', '1', 'crm_approval_status', '', 'success', 'N', '0', 'admin', sysdate());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time)
VALUES (3, '已拒绝', '2', 'crm_approval_status', '', 'danger', 'N', '0', 'admin', sysdate());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time)
VALUES (4, '已撤销', '3', 'crm_approval_status', '', 'info', 'N', '0', 'admin', sysdate());

DROP TABLE IF EXISTS crm_approval_node;
DROP TABLE IF EXISTS crm_approval_request;
DROP TABLE IF EXISTS crm_approval_template;

-- 1. 审批模板
CREATE TABLE crm_approval_template (
    template_id     bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    template_name   varchar(100)  NOT NULL                COMMENT '模板名称',
    biz_type        varchar(50)   NOT NULL                COMMENT '业务类型(contract/order)',
    rules           text          NOT NULL                COMMENT '规则定义JSON',
    status          char(1)       DEFAULT '0'             COMMENT '状态(0启用1停用)',
    del_flag        char(1)       DEFAULT '0'             COMMENT '删除标志',
    create_by       varchar(64)   DEFAULT ''              COMMENT '创建者',
    create_time     datetime                              COMMENT '创建时间',
    update_by       varchar(64)   DEFAULT ''              COMMENT '更新者',
    update_time     datetime                              COMMENT '更新时间',
    remark          varchar(500)  DEFAULT NULL            COMMENT '备注',
    PRIMARY KEY (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='审批模板';

-- 2. 审批请求
CREATE TABLE crm_approval_request (
    request_id      bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '请求ID',
    template_id     bigint(20)    NOT NULL                COMMENT '模板ID',
    biz_type        varchar(50)   NOT NULL                COMMENT '业务类型',
    biz_id          bigint(20)    NOT NULL                COMMENT '业务记录ID',
    biz_info        varchar(500)  DEFAULT NULL            COMMENT '业务摘要(JSON)',
    amount          decimal(12,2) DEFAULT 0.00            COMMENT '金额(用于条件路由)',
    status          char(1)       DEFAULT '0'             COMMENT '状态(0审批中1已通过2已拒绝3已撤销)',
    current_step    int(11)       DEFAULT 1               COMMENT '当前节点序号',
    submit_by       varchar(64)   DEFAULT ''              COMMENT '提交人工号',
    submit_name     varchar(64)   DEFAULT ''              COMMENT '提交人姓名',
    submit_time     datetime                              COMMENT '提交时间',
    del_flag        char(1)       DEFAULT '0'             COMMENT '删除标志',
    create_by       varchar(64)   DEFAULT ''              COMMENT '创建者',
    create_time     datetime                              COMMENT '创建时间',
    update_by       varchar(64)   DEFAULT ''              COMMENT '更新者',
    update_time     datetime                              COMMENT '更新时间',
    remark          varchar(500)  DEFAULT NULL            COMMENT '备注',
    PRIMARY KEY (request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='审批请求';

-- 3. 审批节点实例
CREATE TABLE crm_approval_node (
    node_id         bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '节点ID',
    request_id      bigint(20)    NOT NULL                COMMENT '请求ID',
    step_order      int(11)       DEFAULT 1               COMMENT '节点顺序',
    node_label      varchar(100)  DEFAULT NULL            COMMENT '节点名称',
    approver_role   varchar(50)   DEFAULT NULL            COMMENT '审批角色',
    approver_user_id bigint(20)   DEFAULT NULL            COMMENT '审批人ID',
    approver_name   varchar(64)   DEFAULT NULL            COMMENT '审批人姓名',
    status          char(1)       DEFAULT '0'             COMMENT '状态(0待审批1已通过2已拒绝)',
    comment         varchar(500)  DEFAULT NULL            COMMENT '审批意见',
    operate_time    datetime                              COMMENT '操作时间',
    create_time     datetime                              COMMENT '创建时间',
    PRIMARY KEY (node_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='审批节点实例';

-- 4. 默认审批模板（合同审批：<5万仅经理，>=5万经理+总监）
INSERT INTO crm_approval_template (template_name, biz_type, rules, status, remark) VALUES
('合同审批(默认)', 'contract',
'[{"step":1,"label":"经理审批","approveRole":"manager","condition":null},{"step":2,"label":"总监审批","approveRole":"director","condition":{"field":"amount","op":">=","value":50000}}]',
'0', '默认合同审批模板，5万以下仅经理审批，5万及以上需经理+总监审批');

-- 5. 菜单数据（parent_id=2000 智营CRM）
-- 审批流管理 3010
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3010, '审批流管理', 2060, 1, 'approval', '', 'CrmApprovalRoot', '', 1, 0, 'M', 0, 0, '', 'check', 'admin', sysdate());

-- 审批模板 3011
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3011, '审批模板', 3010, 1, 'template', 'crm/approval/template/index', 'CrmApprovalTemplate', '', 1, 0, 'C', 0, 0, 'crm:approval:template:list', '#', 'admin', sysdate());

-- 待审批 3012
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3012, '待审批', 3010, 2, 'pending', 'crm/approval/pending/index', 'CrmApprovalPending', '', 1, 0, 'C', 0, 0, 'crm:approval:pending:list', '#', 'admin', sysdate());

-- 我发起的 3013
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3013, '我发起的', 3010, 3, 'my', 'crm/approval/my/index', 'CrmApprovalMy', '', 1, 0, 'C', 0, 0, 'crm:approval:my:list', '#', 'admin', sysdate());

-- 新增权限按钮 for 3011
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3014, '审批模板查询', 3011, 1, '', '', '', '', 1, 0, 'F', 0, 0, 'crm:approval:template:query', '#', 'admin', sysdate());
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3015, '审批模板新增', 3011, 2, '', '', '', '', 1, 0, 'F', 0, 0, 'crm:approval:template:add', '#', 'admin', sysdate());
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3016, '审批模板修改', 3011, 3, '', '', '', '', 1, 0, 'F', 0, 0, 'crm:approval:template:edit', '#', 'admin', sysdate());
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3017, '审批模板删除', 3011, 4, '', '', '', '', 1, 0, 'F', 0, 0, 'crm:approval:template:remove', '#', 'admin', sysdate());

-- 6. 角色菜单关联 (admin角色 role_id=1)
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3010);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3011);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3012);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3013);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3014);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3015);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3016);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3017);

-- 7. 订单管理改为目录 + 新增待确认付款菜单
-- 订单管理 2050 改为目录类型
UPDATE sys_menu SET menu_type='M', component=NULL, perms='' WHERE menu_id=2050;

-- 订单列表 3018（原订单管理页面移至子菜单）
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3018, '订单列表', 2050, 1, 'index', 'crm/order/index', 'CrmOrderList', '', 1, 0, 'C', 0, 0, 'crm:order:list', 'list', 'admin', sysdate());

-- 待确认付款 3019
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3019, '待确认付款', 2050, 2, 'pending', 'crm/order/pending/index', 'CrmOrderPending', '', 1, 0, 'C', 0, 0, 'crm:order:pending:list', 'time', 'admin', sysdate());

-- 旧订单按钮权限移至订单列表
UPDATE sys_menu SET parent_id=3018 WHERE menu_id IN (2051,2052,2053,2054);

-- 待确认付款权限按钮
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3020, '确认付款', 3019, 1, '', '', '', '', 1, 0, 'F', 0, 0, 'crm:order:pending:paid', '#', 'admin', sysdate());
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3021, '待确认查询', 3019, 2, '', '', '', '', 1, 0, 'F', 0, 0, 'crm:order:pending:query', '#', 'admin', sysdate());

-- 新菜单关联admin角色
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3018);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3019);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3020);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3021);

-- 8. 合同管理改为目录（审批流作为其子菜单，路由才能正确生成）
UPDATE sys_menu SET menu_type='M', component=NULL, perms='' WHERE menu_id=2060;

-- 合同列表 3022（原合同管理页面移至子菜单）
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3022, '合同列表', 2060, 1, 'index', 'crm/contract/index', 'CrmContractList', '', 1, 0, 'C', 0, 0, 'crm:contract:list', 'list', 'admin', sysdate());

-- 旧合同按钮权限移至合同列表
UPDATE sys_menu SET parent_id=3022 WHERE menu_id IN (2061,2062,2063,2064,3001,3002);

-- 审批流管理排在合同列表之后
UPDATE sys_menu SET order_num=2 WHERE menu_id=3010;

INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3022);
