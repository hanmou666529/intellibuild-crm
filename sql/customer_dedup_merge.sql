-- 客户查重与合并
-- ===========================================================================

-- 1. 客户合并记录表
DROP TABLE IF EXISTS crm_customer_merge_log;
CREATE TABLE crm_customer_merge_log (
    merge_id          bigint(20) NOT NULL AUTO_INCREMENT COMMENT '合并ID',
    keep_customer_id  bigint(20) NOT NULL COMMENT '保留客户ID',
    merge_customer_id bigint(20) NOT NULL COMMENT '被合并客户ID',
    keep_customer_name varchar(100) DEFAULT NULL COMMENT '保留客户名称',
    merge_customer_name varchar(100) DEFAULT NULL COMMENT '被合客户名称',
    merged_fields     json DEFAULT NULL COMMENT '合并的字段清单',
    create_by         varchar(64) DEFAULT '' COMMENT '操作人',
    create_time       datetime DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (merge_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='客户合并记录';

-- 2. 客户撞单争议表
DROP TABLE IF EXISTS crm_customer_dispute;
CREATE TABLE crm_customer_dispute (
    dispute_id        bigint(20) NOT NULL AUTO_INCREMENT COMMENT '争议ID',
    customer_id       bigint(20) NOT NULL COMMENT '客户ID',
    initiator_user_id bigint(20) NOT NULL COMMENT '发起人',
    target_user_id    bigint(20) DEFAULT NULL COMMENT '当前负责人',
    reason            varchar(500) DEFAULT NULL COMMENT '争议原因',
    status            char(1) DEFAULT '0' COMMENT '0=待处理 1=经理已处理 2=超管已仲裁 3=已关闭',
    result            char(1) DEFAULT NULL COMMENT 'A=归发起人 B=归原负责人',
    handler_id        bigint(20) DEFAULT NULL COMMENT '处理人ID',
    handle_time       datetime DEFAULT NULL COMMENT '处理时间',
    remark            varchar(500) DEFAULT NULL COMMENT '处理备注',
    create_by         varchar(64) DEFAULT '' COMMENT '创建人',
    create_time       datetime DEFAULT NULL COMMENT '创建时间',
    update_by         varchar(64) DEFAULT '' COMMENT '更新人',
    update_time       datetime DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (dispute_id),
    KEY idx_customer_id (customer_id),
    KEY idx_status (status)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='客户撞单争议';

-- 3. crm_customer 新增 merge_to_id 字段
ALTER TABLE crm_customer ADD COLUMN merge_to_id bigint(20) DEFAULT NULL COMMENT '合并到的目标客户ID' AFTER del_flag;

-- ===========================================================================
-- 4. 菜单权限
-- ===========================================================================

-- 4a. 争议管理菜单 (parent_id=2000 智营CRM)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3023, '争议管理', 2000, 13, 'dispute', 'crm/dispute/index', 'CrmDispute', '', 1, 0, 'C', 0, 0, 'crm:dispute:list', 'exclamation-triangle', 'admin', sysdate());

-- 4b. 争议管理按钮权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3024, '争议查询', 3023, 1, '', '', '', '', 1, 0, 'F', 0, 0, 'crm:dispute:query', '#', 'admin', sysdate());
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3025, '争议新增', 3023, 2, '', '', '', '', 1, 0, 'F', 0, 0, 'crm:dispute:add', '#', 'admin', sysdate());
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3026, '争议处理', 3023, 3, '', '', '', '', 1, 0, 'F', 0, 0, 'crm:dispute:handle', '#', 'admin', sysdate());
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3027, '争议仲裁', 3023, 4, '', '', '', '', 1, 0, 'F', 0, 0, 'crm:dispute:arbitrate', '#', 'admin', sysdate());

-- 4c. 客户合并按钮 (parent_id=2020 客户管理)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3028, '客户合并', 2020, 10, '', '', '', '', 1, 0, 'F', 0, 0, 'crm:customer:merge', '#', 'admin', sysdate());

-- 5. 角色菜单关联 (admin角色 role_id=1)
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3023);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3024);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3025);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3026);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3027);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3028);
