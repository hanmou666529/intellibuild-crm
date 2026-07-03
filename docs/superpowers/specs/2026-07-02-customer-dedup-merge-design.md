# 客户查重与合并设计

## 痛点
两个员工录入了同一个客户，导致撞单、抢单。

## 功能概述
1. 新建客户时自动查重（公司名、手机号、邮箱）
2. 重复时引导发起争议/合并
3. 超级管理员可执行客户合并
4. 撞单争议由经理协调，协调不了由超级管理员仲裁

---

## 一、数据模型

### 1.1 新建表：`crm_customer_merge_log`
```sql
CREATE TABLE crm_customer_merge_log (
    merge_id         bigint(20) NOT NULL AUTO_INCREMENT COMMENT '合并ID',
    keep_customer_id bigint(20) NOT NULL COMMENT '保留客户ID',
    merge_customer_id bigint(20) NOT NULL COMMENT '被合并客户ID',
    keep_customer_name varchar(100) DEFAULT NULL COMMENT '保留客户名称',
    merge_customer_name varchar(100) DEFAULT NULL COMMENT '被合客户名称',
    merged_fields    json DEFAULT NULL COMMENT '合并字段',
    create_by        varchar(64) DEFAULT '' COMMENT '操作人',
    create_time      datetime DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (merge_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户合并记录';
```

### 1.2 新建表：`crm_customer_dispute`
```sql
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户撞单争议';
```

### 1.3 现有表改动：`crm_customer`
- 新增 `merge_to_id` bigint(20) DEFAULT NULL COMMENT '合并到的目标客户ID'

---

## 二、后端 API

### 2.1 查重 `GET /crm/customer/checkDuplicate`

**参数：** `customerName`、`phone`、`email`、`company`（至少一个）
**返回：**
```json
{
  "duplicate": true,
  "matches": [
    { "customerId": 1, "customerName": "XXX科技", "phone": "13800138000", "company": "XXX科技", "email": "xxx@test.com", "belongUserName": "张三" }
  ]
}
```
**逻辑：** OR 条件匹配（任一字段匹配即视为重复），排除 `del_flag != '0'`。

### 2.2 新建客户 `POST /crm/customer`
- 前端先调 `checkDuplicate`，有重复时弹窗让用户选择
- 用户选择"发起争议" → 提交时带 `initiateDispute: true` 参数
- 后端收到带争议标记 → 创建客户后自动插入 `crm_customer_dispute`，状态 `pending`

### 2.3 争议列表 `GET /crm/dispute/list`
- 经理角色：查全部
- 普通用户：只查自己发起的
- 支持按状态筛选

### 2.4 争议处理 `PUT /crm/dispute/handle`
- 权限：经理（`crm:dispute:handle`）
- 参数：`disputeId`、`action`（`assign`=指定归属 / `escalate`=上报超管）、`targetUserId`（归属人，选填）
- 经理指定归属 → 状态变 '1' + 记录 result → 自动触发合并
- 经理上报超管 → 状态变 '1'，`remark` 标注已上报

### 2.5 争议仲裁 `PUT /crm/dispute/arbitrate`
- 权限：超管（`crm:customer:merge`）
- 参数：`disputeId`、`result`（`A`=归发起人 / `B`=归原负责人）
- 自动触发 `mergeCustomer`

### 2.6 手动合并 `PUT /crm/customer/merge`
- 权限：超管（`crm:customer:merge`）
- 参数：`keepCustomerId`、`mergeCustomerId`
- 直接触发 `mergeCustomer` 逻辑

### 2.7 核心方法 `mergeCustomer(keepId, mergeId)`

```java
@Transactional
public void mergeCustomer(Long keepId, Long mergeId) {
    // 1. 校验两个客户都存在且未删除
    // 2. 转移跟进记录
    crmFollowupMapper.updateCustomerId(mergeId, keepId);
    // 3. 转移管道
    crmPipelineMapper.updateCustomerId(mergeId, keepId);
    // 4. 转移合同
    crmContractMapper.updateCustomerId(mergeId, keepId);
    // 5. 转移订单
    crmOrderMapper.updateCustomerId(mergeId, keepId);
    // 6. 转移共享
    crmCustomerShareMapper.updateCustomerId(mergeId, keepId);
    // 7. 记录合并日志
    insertMergeLog(keepId, mergeId, mergedFields);
    // 8. 软删被合并客户
    crmCustomerMapper.deleteByMerge(mergeId, keepId);
    // 9. 如有争议记录 → 关闭
    crmDisputeMapper.closeByCustomer(mergeId);
}
```

**需要新增的 Mapper 方法：**
- 每个关联表加 `updateCustomerId(@Param("oldId") Long oldId, @Param("newId") Long newId)`

---

## 三、前端界面

### 3.1 新建客户弹窗改动

在 `submitForm` 中，提交前加查重步骤：
1. 收集 form 中的 customerName / phone / email / company
2. 调 `checkDuplicate` API
3. 无重复 → 正常提交
4. 有重复 → 弹出自定义对话框：
   - 内容："该公司名/手机号/邮箱已被 **{customerName}** 使用，负责人：**{belongUserName}**"
   - 按钮：「取消新建」/「发起争议」
5. 点击「发起争议」→ 提交 `addCustomer`（带 `initiateDispute: true`）
6. 创建成功后自动跳转到争议管理页

### 3.2 争议管理页面 `src/views/crm/dispute/index.vue`
- 表格列：发起人 | 客户名 | 当前负责人 | 原因 | 状态 | 创建时间 | 操作
- 操作列：
  - 经理看到：「指定归属」「上报超管」
  - 超管看到：「仲裁（A归发起人/B归原负责人）」
  - 普通用户：无操作按钮
- 状态标签：待处理(warning) / 经理已处理(info) / 超管已仲裁(success) / 已关闭(default)

### 3.3 合并入口（客户列表页）
- 新增按钮「合并客户」，选中两条记录后可用
- 弹窗让用户选择保留客户（radio，展示客户名称/负责人/电话）
- 确认后调 `merge` API
- 权限：`crm:customer:merge`（仅超管）

---

## 四、权限

| 权限标识 | 说明 | 分配给 |
|---|---|---|
| `crm:customer:merge` | 执行客户合并 | 超管 |
| `crm:dispute:list` | 查看争议列表 | 经理/超管 |
| `crm:dispute:handle` | 处理争议（指定归属） | 经理 |
| `crm:dispute:arbitrate` | 仲裁争议 | 超管 |

---

## 五、菜单结构

```
智营CRM (2000)
├── 客户管理 (2020)
│   └── ... 现有按钮
├── 争议管理 (3023, type=C, path='crm/dispute/index')
├── ... 其余现有菜单
└── 合同管理 (2060)
    ├── 合同列表 (3022)
    ├── 审批流管理 (3010)
    │   ├── 审批模板
    │   ├── 待审批
    │   └── 我发起的
    └── ... 按钮
```

---

## 六、实现步骤

### Phase 1：查重拦截 + 争议管理
1. 建表（`crm_customer_merge_log` + `crm_customer_dispute` + `crm_customer.merge_to_id`）
2. 后端查重 API + 争议 CRUD API + mergeCustomer 方法
3. 前端：新建查重弹窗 + 争议管理页面
4. 菜单 + 权限 + SQL 文件

### Phase 2：合并能力
1. 后端手动合并 API
2. 前端客户列表合并按钮 + 合并弹窗
3. 争议仲裁自动触发合并
4. 合并日志查看
