# 智营CRM — 权限体系与销售漏斗说明

---

## 一、销售漏斗（Pipeline）逻辑

### 1.1 什么是销售漏斗

销售漏斗（Pipeline）是一个看板式的销售阶段管理工具，将潜在客户按商机成熟度从左到右排列在 5 个阶段中，帮助管理者一目了然地掌握销售进展。

### 1.2 五个阶段定义

| 阶段 Key | 阶段名称 | 颜色 | 说明 |
|----------|----------|------|------|
| `clue` | 线索 | 灰色 `#909399` | 刚接触的潜在客户，尚未确认需求 |
| `intent` | 意向 | 蓝色 `#409EFF` | 客户已表达初步兴趣或需求 |
| `quote` | 报价 | 橙色 `#E6A23C` | 已发送报价单，客户正在评估 |
| `deal` | 成交 | 绿色 `#67C23A` | 客户确认购买，自动生成合同 |
| `payment` | 回款 | 红色 `#F56C6C` | 等待客户付款或分期跟进 |

> **注：阶段是硬编码的**，目前前端 `stageColumns` 对象和后端 `crm_pipeline.stage` 字段 `char(20)` 直接对应，暂不支持用户自定义阶段。如需新增阶段，需同时修改前端 vue 和后端枚举校验。

### 1.3 数据流向

```
客户管理 → 创建客户（归属用户）
    ↓
销售漏斗 → 新增管道（录入客户名、金额、概率、阶段）
    ↓
拖拽卡片 / 「变更阶段」按钮 → 更新阶段
    ↓
阶段 = deal（成交）→ 自动创建合同记录
    ↓
阶段 = payment（回款）→ 关联回款计划
```

### 1.4 关键数据表

- **`crm_pipeline`** — 管道主表，字段：
  - `pipeline_id` / `customer_name` / `stage` / `amount` / `probability`
  - `expected_deal_date` / `belong_user_name` / `remark`
  - `del_flag`（软删除）
- **`crm_contract`** — 合同表，管道阶段变为 `deal` 时自动行成

### 1.5 自动生成合同逻辑

在 `CrmPipelineServiceImpl.updateStage()` 中，当新阶段为 `"deal"` 时调用 `createContractOnDeal()`：

```java
private void createContractOnDeal(CrmPipeline pipeline) {
    // 检查是否已为此 pipeline 生成过合同
    if (contractService.countByPipelineId(pipeline.getPipelineId()) == 0) {
        CrmContract contract = new CrmContract();
        contract.setContractName(pipeline.getCustomerName() + "-销售合同");
        contract.setCustomerName(pipeline.getCustomerName());
        contract.setTotalAmount(pipeline.getAmount());
        contract.setPipelineId(pipeline.getPipelineId());
        contract.setStatus("0"); // 待审批
        contractService.insertCrmContract(contract);
    }
}
```

> **常见问题**：修改金额后不会同步更新已生成的合同，需手动编辑合同。

### 1.6 数据隔离

Pipeline 列表查询在 `CrmPipelineMapper.xml` 中通过 `LEFT JOIN crm_customer c ON p.customer_name = c.customer_name` 关联客户表，数据范围过滤使用 `c` 别名（`deptAlias="c"`, `userAlias="c"`），即：

- **Pipeline 可见性 = 关联客户的可见性**
- 如果 Pipeline 填写的客户名在 `crm_customer` 中不存在，则该条 Pipeline **对所有用户可见**（JOIN 失败但 LEFT JOIN 仍返回数据，此时 `c.belong_dept_id` 为 NULL，条件不生效）

> ⚠️ **潜在问题**：Pipeline 的 `customer_name` 是自由文本，与 `crm_customer.customer_name` 没有外键约束。如果名称不完全一致，将无法关联，数据隔离会失效。

---

## 二、权限体系详解

智营 CRM 的权限体系分为两层：**宏权限（菜单/按钮级）** 和 **微权限（数据范围级）**。

### 2.1 宏权限：菜单与按钮

基于 RuoYi 标准 RBAC 的 `sys_role_menu` 关联，通过 `@PreAuthorize` + `v-hasPermi` 实现：

#### 权限标识一览表

| 功能模块 | 权限标识 | 说明 |
|----------|----------|------|
| **客户管理** | `crm:customer:list` | 查看客户列表 |
| | `crm:customer:query` | 查询客户详情 |
| | `crm:customer:add` | 新增客户 |
| | `crm:customer:edit` | 修改客户 |
| | `crm:customer:remove` | 删除客户 |
| | `crm:customer:export` | 导出客户 |
| | `crm:customer:import` | 导入客户 |
| | `crm:customer:assign` | 分配客户 |
| | `crm:customer:pool` | 放入公海 |
| | `crm:customer:claim` | 领取公海客户 |
| **跟进管理** | `crm:followup:list` | 查看跟进列表 |
| | `crm:followup:query` | 查询跟进详情 |
| | `crm:followup:add` | 新增跟进记录 |
| | `crm:followup:edit` | 修改跟进记录 |
| | `crm:followup:remove` | 删除跟进记录 |
| **订单管理** | `crm:order:list` | 查看订单列表 |
| | `crm:order:query` | 查询订单详情 |
| | `crm:order:add` | 新增订单 |
| | `crm:order:edit` | 修改订单 |
| | `crm:order:remove` | 删除订单 |
| **合同管理** | `crm:contract:list` | 查看合同列表 |
| | `crm:contract:query` | 查询合同详情 |
| | `crm:contract:add` | 新增合同 |
| | `crm:contract:edit` | 修改合同 |
| | `crm:contract:remove` | 删除合同 |
| **销售管道** | `crm:pipeline:list` | 查看管道看板 |
| | `crm:pipeline:query` | 查询管道详情 |
| | `crm:pipeline:add` | 新增管道项 |
| | `crm:pipeline:edit` | 修改管道项 / 变更阶段 |
| | `crm:pipeline:remove` | 删除管道项 |
| **回款计划** | `crm:payment:list` | 查看回款计划 |
| | `crm:payment:query` | 查询回款详情 |
| | `crm:payment:add` | 新增回款计划 |
| | `crm:payment:edit` | 修改回款 / 标记已付 |
| | `crm:payment:remove` | 删除回款计划 |
| **产品管理** | `crm:product:list` | 查看产品列表 |
| | `crm:product:add` | 新增产品 |
| | `crm:product:edit` | 修改产品 |
| | `crm:product:remove` | 删除产品 |
| **产品分类** | `crm:category:list` | 查看分类树 |
| | `crm:category:add` | 新增分类 |
| | `crm:category:edit` | 修改分类 |
| | `crm:category:remove` | 删除分类 |
| **公海客户** | `crm:pool:list` | 查看公海列表 |
| **通知公告** | `crm:notification:list` | 查看通知列表 |
| | `crm:notification:add` | 新增通知 |
| | `crm:notification:edit` | 修改通知 |
| | `crm:notification:remove` | 删除通知 |
| **数据看板** | `crm:dashboard:list` | 查看仪表盘 |

### 2.2 微权限：数据范围

基于 RuoYi `sys_role.data_scope` 字段的 5 级体系，通过 `@CrmDataScope` 注解 + `CrmDataScopeAspect` 切面实现。

#### 数据范围级别

| 值 | 常量 | 说明 | SQL 片段 |
|----|------|------|----------|
| `1` | `DATA_SCOPE_ALL` | 全部数据可见 | 无过滤条件 |
| `2` | `DATA_SCOPE_CUSTOM` | 自定义部门数据 | `c.belong_dept_id IN (SELECT dept_id FROM sys_role_dept WHERE role_id = ?)` |
| `3` | `DATA_SCOPE_DEPT` | 本部门数据 | `c.belong_dept_id = {user.dept_id}` |
| `4` | `DATA_SCOPE_DEPT_AND_CHILD` | 本部门及以下数据 | `c.belong_dept_id IN (SELECT dept_id FROM sys_dept WHERE dept_id = ? OR find_in_set(?, ancestors))` |
| `5` | `DATA_SCOPE_SELF` | 仅本人数据 | `c.belong_user_id = {user.user_id}` |

#### `@CrmDataScope` 工作机制

1. Controller 方法标注 `@CrmDataScope(deptAlias="c", userAlias="c")`
2. `CrmDataScopeAspect` 拦截调用，获取当前登录用户的角色列表
3. 取**最小** `data_scope` 值（数字越小范围越大，1 最小）
4. 根据该值构建 SQL WHERE 片段
5. 通过反射注入到方法参数的 `BaseEntity.params.dataScope` 属性
6. Mapper XML 中 `${params.dataScope}` 被解析为原始 SQL

> **与标准 `@DataScope` 的区别**：`@CrmDataScope` 使用 `belong_dept_id`/`belong_user_id` 字段（而非 RuoYi 通用的 `dept_id`/`user_id`），且不依赖 `permission` 参数，对所有请求都生效。

#### 数据范围与表关系

```sql
-- @CrmDataScope 最终生成的 SQL 示例（以 data_scope=4 部门及以下为例）
AND c.belong_dept_id IN (
    SELECT dept_id FROM sys_dept
    WHERE dept_id = 206
       OR find_in_set(206, ancestors)
)
```

各模块的数据范围过滤均通过 `crm_customer` 表实现：

| 模块 | 数据表 | 过滤字段 | 实现方式 |
|------|--------|----------|----------|
| 客户管理 | `crm_customer c` | `c.belong_dept_id` / `c.belong_user_id` | 直接在 `selectCrmCustomerList` 中追加 |
| 跟进管理 | `crm_followup` | 通过 JOIN `crm_customer c` | `followupMapper.xml` 中已有 JOIN，追加 `params.dataScope` |
| 订单管理 | `crm_order` | 通过 JOIN `crm_customer c` | `orderMapper.xml` 中已有 JOIN，追加 `params.dataScope` |
| 销售管道 | `crm_pipeline` | 通过 JOIN `crm_customer c` | `pipelineMapper.xml` 中已有 JOIN，追加 `params.dataScope` |
| **公海** | `crm_customer` | **不应用数据范围** | 公海 Controller **未标注** `@CrmDataScope` |
| **仪表盘** | `crm_customer` | **不应用数据范围** | 仪表盘统计未标注 `@CrmDataScope` |

> **公海的设计原则**：公海客户 (`is_pool='1'`) 对所有有公海查看权限的用户开放，不按部门隔离。这是为了让销售可以自由从公海领取客户。

---

## 三、部门与权限关联

### 3.1 部门结构

| dept_id | 部门名称 | 父部门 | 描述 |
|---------|----------|--------|------|
| 201 | 总经办 | 0 | 公司管理层 |
| 202 | 销售部 | 0 | 销售团队 |
| 203 | 市场部 | 0 | 市场推广 |
| 204 | 客服部 | 0 | 客户服务 |
| 205 | 财务部 | 0 | 财务管理 |
| 206 | 产品部 | 0 | 产品研发 |

### 3.2 角色定义

| role_id | 角色名称 | data_scope | 说明 | 适用对象 |
|---------|----------|------------|------|----------|
| 1 | 超级管理员 | 1 (ALL) | 技术管理员，保留角色不可删除 | developer |
| — | 董事长 | 1 (ALL) | 查看所有 CRM 数据 | 老板 |
| 3 | 经理 | 4 (DEPT_AND_CHILD) | 查看本部门及下属部门数据 | 部门负责人 |
| 4 | 员工 | 5 (SELF) | 仅查看自己负责的数据 | 一线销售/客服 |
| 5 | HR专员 | 3 (DEPT) | 查看本部门数据 | 人力资源 |

> **注意**：`role_id=1` 在 RuoYi 中是超级管理员（`is_admin=1`），`CrmDataScopeAspect` 会跳过数据范围过滤。董事长角色应在系统管理中创建新角色（data_scope=1），而非使用 role_id=1。

### 3.3 角色-权限-数据范围矩阵

| 操作 \ 角色 | 董事长 (ALL) | 经理 (DEPT&CHILD) | 员工 (SELF) | HR专员 (DEPT) |
|-------------|:-----------:|:-----------------:|:-----------:|:-------------:|
| 查看全部客户 | ✅ | ❌ | ❌ | ❌ |
| 查看本部门客户 | ✅ | ✅ | ❌ | ✅ |
| 查看下属部门客户 | ✅ | ✅ | ❌ | ❌ |
| 查看自己负责的客户 | ✅ | ✅ | ✅ | ✅ |
| 查看公海 | ✅ | ✅ | ✅ | ✅ |
| 分配客户给他人 | ✅ 有权限 | 需配 `assign` | ❌ | ❌ |
| 放入公海 | ✅ 有权限 | 需配 `pool` | 需配 `pool` | ❌ |
| 领取公海客户 | ✅ 有权限 | 需配 `claim` | ✅ 已配 `claim` | ❌ |
| 新增客户 | ✅ | ✅ | ✅ | ✅ |
| 修改客户 | ✅ | ✅ | 仅自己可用 | ✅ |
| 删除客户 | ✅ | ✅ | ❌ | ❌ |
| 查看跟进/订单/管道 | ✅ | 按数据范围 | 仅自己 | 按数据范围 |

### 3.4 用户归属与数据范围的关系

`crm_customer` 表的两个关键字段：

- **`belong_user_id`** — 负责该客户的用户
- **`belong_dept_id`** — 负责用户所属的部门（分配时自动填充）

```
分配客户时：
  assignCustomer(customerId, userId)
    → 查询 sys_user.user_id = userId → 获取 dept_id
    → crm_customer.belong_user_id = userId
    → crm_customer.belong_dept_id = dept_id

放入公海时：
  putToPool(customerId)
    → crm_customer.belong_user_id = NULL
    → crm_customer.belong_dept_id = NULL
    → crm_customer.is_pool = '1'

领取公海时：
  claimFromPool(customerId)
    → crm_customer.belong_user_id = 当前用户.id
    → crm_customer.belong_dept_id = 当前用户.dept_id
    → crm_customer.is_pool = '0'
```

### 3.5 前端按钮可见性

前端通过 `v-hasPermi` 控制按钮显示，不与数据范围联动：

- **「分配」按钮**：依赖 `crm:customer:edit` 权限 → 董事长/经理可见；员工**不可见**
- **「放入公海」按钮**：依赖 `crm:customer:pool` 权限 → 董事长/经理/员工（需分配）可见
- **「领取」按钮**：依赖 `crm:customer:claim` 权限 → 仅公海页面，员工默认有该权限

> 后端同时有 `@PreAuthorize` 保护，即使前端按钮不显示，直接调用 API 也会被拦截。

---

## 四、常见问题

### Q1: 放入公海后，原因字段不显示

**问题**：前端填写了原因，但查看公海日志时 `reason` 为空。

**根因**：前后端字段名不一致。
- 前端 API 调用：`putToPool(customerId, reason)` → 发送 JSON `{customerId, reason}`
- 后端 `CrmCustomer` 实体无 `reason` 字段，只有继承自 `BaseEntity` 的 `remark`
- Jackson 反序列化忽略 `reason`，导致 `getRemark()` 始终返回 `null`

**修复**（已实施）：前端 `putToPool()` API 改为发送 `remark: reason`。

```
// crm/customer.js - 修复后
putToPool(data) {
  return request({
    url: '/crm/customer/pool',
    method: 'put',
    data: { customerId: data.customerId, remark: data.reason }
  })
}
```

### Q2: 用户管理页面，点击部门树后用户列表消失

**问题**：点击部门节点后，筛选出的用户无法恢复全量显示。

**根因**（已修复）：
- 部门树点击后 `deptId` 被设置，但无取消方式
- 用户不知道「重置」按钮可以清除部门筛选
- `resetQuery` 方法未清除部门筛选状态

**修复方案**：
1. 部门树支持 toggle 取消（再次点击同一部门取消选中）
2. 在部门树上方添加蓝色 **「全部」** 按钮，一键清除部门筛选
3. 添加状态分类 Tab（全部/正常/停用），替代仅 dropdown 的方式
4. 右侧显示可视化标签 `当前部门：XX [×]`，点击 × 可清除
5. `resetQuery` 加强：同步重置 `statusTab`/`currentDeptName`/`deptId`

### Q3: 员工账号看不到「分配」「放入公海」按钮

**问题**：以员工角色登录后，客户管理页面缺少某些按钮。

**原因**：
- **「分配」**：使用 `crm:customer:edit` 权限标识，员工角色 (`role_id=4`) 未分配此菜单权限
- **「放入公海」**（修复前）：使用 `crm:customer:edit`，员工没有此权限
- **「放入公海」**（修复后）：使用 `crm:customer:pool`，员工有此权限

**解决方案**：
- 给员工角色分配 `crm:customer:pool` 菜单权限（`INSERT INTO sys_role_menu VALUES (4, 2029)`）
- 放入公海按钮单独使用 `crm:customer:pool` 标识，与编辑解耦

> 如果员工仍看不到按钮，检查 `sys_role_menu` 中是否已为该角色分配相关菜单 ID。

### Q4: 分配客户后，接收方看不到客户

**问题**：经理将客户分配给员工 ash，但 ash 登录后看不到该客户。

**可能原因**：
1. `belong_dept_id` 未正确设置 — `assignCustomer()` 中通过 `sys_user.dept_id` 获取，检查用户是否有所属部门
2. 数据范围过滤 — 员工角色 `data_scope=5(SELF)`，只能看到 `belong_user_id` 等于自己的记录
3. 确认 ash 的 `user_id` 是否正确写入 `crm_customer.belong_user_id`
4. 查看 `crm_customer_pool_log` 确认分配记录

### Q5: Pipeline 列表数据范围过滤不生效

**问题**：员工能看到不属于自己的 Pipeline 卡片。

**可能原因**：
1. Pipeline 的 `customer_name` 与 `crm_customer.customer_name` **不完全一致**
   - Pipeline 写入 "张三科技"，客户表存 "张三科技有限公司" → JOIN 失败，数据范围不生效
2. Pipeline 关联的客户已被放入公海（`is_pool='1'`）— 但 Pipeline 显示不受影响
3. 当前账号是超级管理员（`is_admin=1`）— 数据范围被跳过

**建议**：
- Pipeline 新增/编辑时，使用客户选择器而非自由文本输入
- 考虑在 `crm_pipeline` 表中增加 `customer_id` 外键字段直接关联客户

### Q6: 如何确认当前用户的数据范围

**排查步骤**：

1. **查看角色配置**：
   ```sql
   SELECT r.role_id, r.role_name, r.data_scope
   FROM sys_user_role ur
   JOIN sys_role r ON ur.role_id = r.role_id
   WHERE ur.user_id = (SELECT user_id FROM sys_user WHERE user_name = 'ash')
   ```

2. **查看角色菜单权限**：
   ```sql
   SELECT rm.menu_id, m.perms
   FROM sys_role_menu rm
   JOIN sys_menu m ON rm.menu_id = m.menu_id
   WHERE rm.role_id = 4
     AND m.perms LIKE 'crm:%'
   ```

3. **查看客户归属**：
   ```sql
   SELECT customer_id, customer_name, belong_user_id, belong_dept_id, is_pool
   FROM crm_customer
   WHERE customer_id = 1;
   ```

### Q7: 前端权限标识与后端不一致

**问题**：前端按钮显示与后端实际校验不匹配。

**排查**：
- 前端 `v-hasPermi` 使用的权限字符串 → 检查对应 vue 文件
- 后端 `@PreAuthorize` 使用的权限字符串 → 检查对应 Controller
- 数据库 `sys_menu.perms` → 检查菜单管理中配置的权限标识
- 以上三者**必须完全一致**（大小写敏感）

已知修复的 DB 权限标识：
| 修复前 | 修复后 |
|--------|--------|
| `crm:paymentPlan:xxx` | `crm:payment:xxx` |
| `crm:productCategory:xxx` | `crm:category:xxx` |
| `crm:customerPool:xxx` | `crm:pool:xxx` |

### Q8: 如何新增一个部门/角色

**步骤**：

1. **新增部门**：系统管理 → 部门管理 → 新增（注意 `ancestors` 和 `order_num`）
2. **新增角色**：
   - 系统管理 → 角色管理 → 新增
   - 设置角色名称、权限字符
   - 选择数据范围（1-5）
   - 分配 CRM 相关菜单权限
3. **关联用户**：系统管理 → 用户管理 → 编辑用户 → 选择部门 + 分配角色

> 新增角色后，需在 `CrmDataScopeAspect` 中确认数据范围处理逻辑是否满足需求（目前支持全部 5 级通用）。

---

## 附录：关键代码文件索引

| 功能 | 文件路径 |
|------|----------|
| CRM 数据范围注解 | `ruoyi-common/.../annotation/CrmDataScope.java` |
| CRM 数据范围切面 | `ruoyi-framework/.../aspectj/CrmDataScopeAspect.java` |
| 客户 Controller | `ruoyi-admin/.../crm/CrmCustomerController.java` |
| 跟进 Controller | `ruoyi-admin/.../crm/CrmFollowupController.java` |
| 订单 Controller | `ruoyi-admin/.../crm/CrmOrderController.java` |
| 管道 Controller | `ruoyi-admin/.../crm/CrmPipelineController.java` |
| 客户 Service | `ruoyi-system/.../impl/CrmCustomerServiceImpl.java` |
| 客户 Mapper XML | `ruoyi-system/.../crm/CrmCustomerMapper.xml` |
| 管道 Mapper XML | `ruoyi-system/.../crm/CrmPipelineMapper.xml` |
| 客户前端页面 | `ruoyi-ui/.../customer/index.vue` |
| 公海前端页面 | `ruoyi-ui/.../pool/index.vue` |
| 管道前端页面 | `ruoyi-ui/.../pipeline/index.vue` |
| 用户管理前端 | `ruoyi-ui/.../user/index.vue` |
| CRM 前端 API | `ruoyi-ui/.../crm/customer.js` |
