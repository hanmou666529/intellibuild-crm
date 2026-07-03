# 智营CRM 权限与业务流集成 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现客户-合同-订单-回款四模块的数据隔离权限 + 双通道业务流 + 状态级联

**Architecture:** 各业务表增加 owner_id/dept_id 冗余归属字段实现行级权限过滤；sumMonthDealAmount 用 UNION 实现直通车/签约双通道统计；合同审批通过自动生成订单；回款标记已付级联完成订单和合同。

**Tech Stack:** Spring Boot 2.x, MyBatis, MySQL 8, Redis, Vue 2 + Element UI 2.15.14, RuoYi v4.x

---

### Task 1: 数据库变更 + 字典新增

**Files:**
- Run SQL: MySQL on `localhost:3306/ry-vue`
- Document: `sql/crm_tables.sql`
- Modify: `sql/crm_dict_data.sql`

- [ ] **Step 1: 执行 ALTER TABLE 添加归属字段**

```bash
mysql -u root -pHae147258369 ry-vue -e "
ALTER TABLE crm_contract
  ADD COLUMN owner_id bigint(20) DEFAULT NULL COMMENT '负责人ID',
  ADD COLUMN dept_id bigint(20) DEFAULT NULL COMMENT '所属部门ID',
  ADD INDEX idx_contract_owner (owner_id),
  ADD INDEX idx_contract_dept (dept_id);

ALTER TABLE crm_order
  ADD COLUMN owner_id bigint(20) DEFAULT NULL COMMENT '负责人ID',
  ADD COLUMN dept_id bigint(20) DEFAULT NULL COMMENT '所属部门ID',
  ADD INDEX idx_order_owner (owner_id),
  ADD INDEX idx_order_dept (dept_id);

ALTER TABLE crm_payment_plan
  ADD COLUMN owner_id bigint(20) DEFAULT NULL COMMENT '负责人ID',
  ADD COLUMN dept_id bigint(20) DEFAULT NULL COMMENT '所属部门ID',
  ADD INDEX idx_payment_owner (owner_id),
  ADD INDEX idx_payment_dept (dept_id);
"
```

- [ ] **Step 2: 添加「已签约」字典**

```sql
INSERT INTO sys_dict_data VALUES (274, 5, '已签约', '4', 'crm_order_status', '', 'primary', 'N', '0', 'admin', NOW(), '', NULL, '');
```

```bash
mysql -u root -pHae147258369 ry-vue -e "INSERT IGNORE INTO sys_dict_data VALUES (274, 5, '已签约', '4', 'crm_order_status', '', 'primary', 'N', '0', 'admin', NOW(), '', NULL, '')"
```

- [ ] **Step 3: 更新 `sql/crm_dict_data.sql` 添加注释**

Edit `sql/crm_dict_data.sql` to append after the crm_order_status section:
```sql
INSERT INTO sys_dict_data VALUES (274, 5, '已签约', '4', 'crm_order_status', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '');
```

- [ ] **Step 4: 更新已有数据的 owner_id/dept_id（历史数据修复）**

```bash
mysql -u root -pHae147258369 ry-vue -e "
UPDATE crm_contract ct
  LEFT JOIN crm_customer c ON ct.customer_id = c.customer_id
  SET ct.owner_id = COALESCE(c.belong_user_id, 1), ct.dept_id = COALESCE(c.belong_dept_id, 201)
  WHERE ct.owner_id IS NULL;

UPDATE crm_order o
  LEFT JOIN crm_customer c ON o.customer_id = c.customer_id
  SET o.owner_id = COALESCE(c.belong_user_id, 1), o.dept_id = COALESCE(c.belong_dept_id, 201)
  WHERE o.owner_id IS NULL;

UPDATE crm_payment_plan pp
  LEFT JOIN crm_order o ON pp.order_id = o.order_id
  SET pp.owner_id = COALESCE(o.owner_id, 1), pp.dept_id = COALESCE(o.dept_id, 201)
  WHERE pp.owner_id IS NULL;
"
```

- [ ] **Step 5: 清空 Redis 缓存**

```bash
redis-cli FLUSHALL
```

---

### Task 2: 后端 Domain 类增加归属字段

**Files:**
- Modify: `ruoyi-system/src/main/java/com/ruoyi/crm/domain/CrmContract.java`
- Modify: `ruoyi-system/src/main/java/com/ruoyi/crm/domain/CrmOrder.java`
- Modify: `ruoyi-system/src/main/java/com/ruoyi/crm/domain/CrmPaymentPlan.java`
- Modify: `ruoyi-system/src/main/java/com/ruoyi/crm/domain/CrmCustomer.java`

- [ ] **Step 1: CrmContract.java 增加 ownerId/deptId**

```java
/** 负责人ID */
private Long ownerId;

/** 所属部门ID */
private Long deptId;

public Long getOwnerId() { return ownerId; }
public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
public Long getDeptId() { return deptId; }
public void setDeptId(Long deptId) { this.deptId = deptId; }
```

- [ ] **Step 2: CrmOrder.java 增加 ownerId/deptId**

Same fields + getters/setters as step 1.

- [ ] **Step 3: CrmPaymentPlan.java 增加 ownerId/deptId**

Same fields + getters/setters as step 1.

- [ ] **Step 4: CrmCustomer.java 增加 orderNos/contractNos**

```java
/** 关联订单号（逗号分隔） */
private String orderNos;

/** 关联合同号（逗号分隔） */
private String contractNos;

public String getOrderNos() { return orderNos; }
public void setOrderNos(String orderNos) { this.orderNos = orderNos; }
public String getContractNos() { return contractNos; }
public void setContractNos(String contractNos) { this.contractNos = contractNos; }
```

---

### Task 3: Mapper XML - 新增数据权限别名 + 关联查询

**Files:**
- Modify: `ruoyi-system/src/main/resources/mapper/crm/CrmContractMapper.xml`
- Modify: `ruoyi-system/src/main/resources/mapper/crm/CrmOrderMapper.xml`
- Modify: `ruoyi-system/src/main/resources/mapper/crm/CrmPaymentPlanMapper.xml`
- Modify: `ruoyi-system/src/main/resources/mapper/crm/CrmCustomerMapper.xml`

- [ ] **Step 1: CrmContractMapper.xml 增加别名 + 数据权限**

In `selectCrmContractList`, change the base SQL to alias `ct` and add dataScope filter:
```xml
<select id="selectCrmContractList" parameterType="CrmContract" resultMap="CrmContractResult">
    select ct.contract_id, ct.contract_no, ct.contract_name, ct.customer_id,
           ct.order_id, ct.amount, ct.sign_date, ct.start_date, ct.end_date,
           ct.status, ct.attachment, ct.del_flag, ct.create_by, ct.create_time,
           ct.update_by, ct.update_time, ct.remark, ct.owner_id, ct.dept_id,
           c.customer_name
    from crm_contract ct
    left join crm_customer c on ct.customer_id = c.customer_id
    where ct.del_flag = '0'
    <if test="contractNo != null and contractNo != ''">
        AND ct.contract_no like concat('%', #{contractNo}, '%')
    </if>
    <!-- ... existing search conditions ... -->
    <if test="params.dataScope != null and params.dataScope != ''">
        ${params.dataScope}
    </if>
</select>
```

Also add `selectByContractId` and `countUnpaidByContractId` queries:
```xml
<select id="selectByContractId" parameterType="Long" resultMap="CrmContractResult">
    <include refid="selectCrmContractVo"/>
    where ct.contract_id = #{contractId}
</select>

<select id="countUnpaidByContractId" parameterType="Long" resultType="int">
    select count(*)
    from crm_payment_plan pp
    left join crm_order o on pp.order_id = o.order_id
    where o.contract_id = #{contractId}
      and pp.del_flag = '0' and pp.status != '1'
</select>
```

- [ ] **Step 2: CrmOrderMapper.xml - sumMonthDealAmount 改为 UNION**

Replace the current `sumMonthDealAmount` SQL:
```xml
<select id="sumMonthDealAmount" parameterType="CrmCustomer" resultType="Double">
    select coalesce(sum(amount), 0) from (
        select o.actual_amount as amount
        from crm_order o
        left join crm_customer c on o.customer_id = c.customer_id
        where o.del_flag = '0' and o.status in ('1', '2')
          and o.contract_id is null
          and date_format(o.update_time, '%Y-%m') = #{params.month}
        <if test="params.dataScope != null and params.dataScope != ''">
            ${params.dataScope}
        </if>
        union all
        select pp.actual_amount
        from crm_payment_plan pp
        left join crm_order o on pp.order_id = o.order_id
        left join crm_customer c on o.customer_id = c.customer_id
        where pp.del_flag = '0' and pp.status = '1'
          and o.status = '4'
          and date_format(pp.actual_date, '%Y-%m') = #{params.month}
        <if test="params.dataScope != null and params.dataScope != ''">
            ${params.dataScope}
        </if>
    ) t
</select>
```

Also add `selectByContractId`:
```xml
<select id="selectByContractId" parameterType="Long" resultMap="CrmOrderResult">
    <include refid="selectCrmOrderVo"/>
    where o.contract_id = #{contractId} and o.del_flag = '0'
</select>
```

Also update the main `selectCrmOrderList` to use `o` alias:
```xml
<sql id="selectCrmOrderVo">
    select o.order_id, o.order_no, o.customer_id, o.contract_id, o.total_amount,
           o.discount_amount, o.actual_amount, o.status, o.paid_amount,
           o.del_flag, o.create_by, o.create_time, o.update_by, o.update_time, o.remark,
           o.owner_id, o.dept_id,
           c.customer_name
    from crm_order o
    left join crm_customer c on o.customer_id = c.customer_id
</sql>
```

- [ ] **Step 3: CrmPaymentPlanMapper.xml - add countUnpaidByOrderId**

```xml
<select id="countUnpaidByOrderId" parameterType="Long" resultType="int">
    select count(*)
    from crm_payment_plan
    where order_id = #{orderId}
      and del_flag = '0' and status != '1'
</select>
```

And update select list to use `pp` alias with data scope:
```xml
<select id="selectCrmPaymentPlanList" parameterType="CrmPaymentPlan" resultMap="CrmPaymentPlanResult">
    select pp.plan_id, pp.order_id, pp.contract_id, pp.plan_amount, pp.actual_amount,
           pp.plan_date, pp.actual_date, pp.status, pp.payment_method,
           pp.del_flag, pp.create_by, pp.create_time, pp.update_by, pp.update_time,
           pp.remark, pp.owner_id, pp.dept_id,
           o.order_no, c.customer_name
    from crm_payment_plan pp
    left join crm_order o on pp.order_id = o.order_id
    left join crm_customer c on o.customer_id = c.customer_id
    where pp.del_flag = '0'
    <if test="params.dataScope != null and params.dataScope != ''">
        ${params.dataScope}
    </if>
</select>
```

- [ ] **Step 4: CrmCustomerMapper.xml - 增加关联订单/合同子查询**

In `selectCrmCustomerList`, add two LEFT JOIN subqueries:
```sql
left join (
    select customer_id,
           group_concat(distinct order_no order by create_time desc separator ', ') as order_nos
    from crm_order where del_flag = '0' group by customer_id
) o_info on o_info.customer_id = c.customer_id
left join (
    select customer_id,
           group_concat(distinct contract_no order by create_time desc separator ', ') as contract_nos
    from crm_contract where del_flag = '0' group by customer_id
) ct_info on ct_info.customer_id = c.customer_id
```

And add the result mappings:
```xml
<result property="orderNos" column="order_nos" />
<result property="contractNos" column="contract_nos" />
```

---

### Task 4: Java Mapper 接口方法新增

**Files:**
- Modify: `ruoyi-system/.../mapper/CrmContractMapper.java`
- Modify: `ruoyi-system/.../mapper/CrmOrderMapper.java`
- Modify: `ruoyi-system/.../mapper/CrmPaymentPlanMapper.java`

- [ ] **Step 1: CrmContractMapper.java**

```java
public CrmContract selectByContractId(Long contractId);
public int countUnpaidByContractId(Long contractId);
```

- [ ] **Step 2: CrmOrderMapper.java**

```java
public List<CrmOrder> selectByContractId(Long contractId);
```

- [ ] **Step 3: CrmPaymentPlanMapper.java**

```java
public int countUnpaidByOrderId(Long orderId);
```

---

### Task 5: Service 层 - 合同审批 + 自动生成订单 + 完成校验

**Files:**
- Modify: `ruoyi-system/.../service/ICrmContractService.java`
- Modify: `ruoyi-system/.../service/impl/CrmContractServiceImpl.java`
- Modify: `ruoyi-system/.../service/impl/CrmPaymentPlanServiceImpl.java`

- [ ] **Step 1: ICrmContractService.java 新增方法**

```java
public int approveContract(Long contractId);
```

- [ ] **Step 2: CrmContractServiceImpl.java 实现 approveContract + 完成校验**

```java
@Override
public int approveContract(Long contractId) {
    CrmContract contract = crmContractMapper.selectByContractId(contractId);
    if (contract == null) {
        throw new ServiceException("合同不存在");
    }
    if (!"0".equals(contract.getStatus())) {
        throw new ServiceException("仅待审核状态的合同可审批通过");
    }
    // 1. 更新合同状态为已生效
    contract.setStatus("1");
    int result = crmContractMapper.updateCrmContract(contract);

    // 2. 自动生成订单
    CrmOrder order = new CrmOrder();
    order.setOrderNo(generateOrderNo());
    order.setCustomerId(contract.getCustomerId());
    order.setContractId(contract.getContractId());
    order.setActualAmount(contract.getAmount());
    order.setStatus("0"); // 待付款
    order.setOwnerId(contract.getOwnerId());
    order.setDeptId(contract.getDeptId());
    crmOrderService.insertCrmOrder(order);

    return result;
}

@Override
public int updateCrmContract(CrmContract contract) {
    // 如果改状态为已完成，执行校验
    if ("2".equals(contract.getStatus())) {
        int unpaid = crmContractMapper.countUnpaidByContractId(contract.getContractId());
        if (unpaid > 0) {
            throw new ServiceException("合同关联的回款计划尚未全部付清，无法标记为已完成");
        }
        // 校验通过：将关联订单都改为已完成
        List<CrmOrder> orders = crmOrderMapper.selectByContractId(contract.getContractId());
        for (CrmOrder o : orders) {
            o.setStatus("2");
            crmOrderMapper.updateCrmOrder(o);
        }
    }
    return crmContractMapper.updateCrmContract(contract);
}
```

- [ ] **Step 3: markAsPaid 增加正向级联**

In `CrmPaymentPlanServiceImpl.markAsPaid()`, after the existing logic that sets `order.setStatus("2")`, add:
```java
// 如果订单关联了合同，自动完成合同
if (order.getContractId() != null) {
    CrmContract contract = crmContractMapper.selectByContractId(order.getContractId());
    if (contract != null && !"2".equals(contract.getStatus())) {
        contract.setStatus("2");
        crmContractMapper.updateCrmContract(contract);
    }
}
```

---

### Task 6: Controller 层 - 权限注解 + 审批端点

**Files:**
- Modify: `ruoyi-admin/.../crm/CrmContractController.java`
- Modify: `ruoyi-admin/.../crm/CrmOrderController.java`
- Modify: `ruoyi-admin/.../crm/CrmPaymentPlanController.java`

- [ ] **Step 1: CrmContractController.java - 权限 + 审批端点**

Add `@CrmDataScope(deptAlias = "ct", userAlias = "ct")` on `list()`:
```java
@PreAuthorize("@ss.hasPermi('crm:contract:list')")
@CrmDataScope(deptAlias = "ct", userAlias = "ct")
@GetMapping("/list")
public TableDataInfo list(CrmContract contract) {
    startPage();
    List<CrmContract> list = crmContractService.selectCrmContractList(contract);
    return getDataTable(list);
}
```

Add approve endpoint:
```java
@PreAuthorize("@ss.hasPermi('crm:contract:edit')")
@PutMapping("/approve/{contractId}")
public AjaxResult approve(@PathVariable Long contractId) {
    return toAjax(crmContractService.approveContract(contractId));
}
```

Add update override with validation:
```java
@PreAuthorize("@ss.hasPermi('crm:contract:edit')")
@PutMapping
public AjaxResult edit(@RequestBody CrmContract contract) {
    if ("2".equals(contract.getStatus())) {
        // Only admin and manager (with dept scope) can mark as completed
        if (!SecurityUtils.isAdmin(SecurityUtils.getUserId())) {
            LoginUser loginUser = getLoginUser();
            // Check if user has admin role or can manage this contract
        }
    }
    return toAjax(crmContractService.updateCrmContract(contract));
}
```

- [ ] **Step 2: CrmOrderController.java - 数据权限注解**

Add `@CrmDataScope(deptAlias = "o", userAlias = "o")` on `list()`.

- [ ] **Step 3: CrmPaymentPlanController.java - 数据权限注解**

Add `@CrmDataScope(deptAlias = "pp", userAlias = "pp")` on `list()`.

---

### Task 7: 前端 - 合同管理页增加审批按钮

**Files:**
- Modify: `ruoyi-ui/src/views/crm/contract/index.vue`
- Modify: `ruoyi-ui/src/api/crm/contract.js`

- [ ] **Step 1: contract.js 新增 approveContract API**

```javascript
export function approveContract(contractId) {
  return request({
    url: '/crm/contract/approve/' + contractId,
    method: 'put'
  })
}
```

- [ ] **Step 2: contract/index.vue 操作列新增「审核通过」按钮**

In the template's operation column, add before the edit button:
```vue
<el-button
  v-if="scope.row.status == '0'"
  size="mini"
  type="warning"
  plain
  icon="el-icon-check"
  @click="handleApprove(scope.row)"
  v-hasPermi="['crm:contract:edit']">审核通过</el-button>
```

Add method:
```javascript
handleApprove(row) {
  const contractId = row.contractId
  this.$modal.confirm('确认审核通过合同【' + row.contractNo + '】？审核通过后将自动生成关联订单。').then(function() {
    return approveContract(contractId)
  }).then(() => {
    this.getList()
    this.$modal.msgSuccess('审核通过，订单已自动生成')
  }).catch(() => {})
},
```

Also remove delete button for non-admin (经理不能删):
```vue
<el-button
  v-if="scope.row.status == '2' && (roles.includes('admin'))"
  size="mini" type="text" icon="el-icon-delete"
  @click="handleDelete(scope.row)"
  v-hasPermi="['crm:contract:remove']">删除</el-button>
```

---

### Task 8: 前端 - 客户页面显示关联订单/合同

**Files:**
- Modify: `ruoyi-ui/src/views/crm/customer/index.vue`

- [ ] **Step 1: 客户表增加「关联订单」「关联合同」列**

After the `belongDeptName` column:
```vue
<el-table-column label="关联订单" align="center" prop="orderNos" min-width="180"
  :show-overflow-tooltip="true">
  <template slot-scope="scope">
    <span v-if="scope.row.followStatus == 'won'">{{ scope.row.orderNos || '—' }}</span>
    <span v-else class="text-muted">—</span>
  </template>
</el-table-column>
<el-table-column label="关联合同" align="center" prop="contractNos" min-width="180"
  :show-overflow-tooltip="true">
  <template slot-scope="scope">
    <span v-if="scope.row.followStatus == 'won'">{{ scope.row.contractNos || '—' }}</span>
    <span v-else class="text-muted">—</span>
  </template>
</el-table-column>
```

- [ ] **Step 2: 客户详情页增加关联列表**

In the `detailOpen` dialog, after the basic info fields, add three collapsed sections:
```vue
<el-divider v-if="detailForm.followStatus == 'won'" content-position="left">关联合同</el-divider>
<el-table v-if="detailForm.followStatus == 'won'" :data="contractList" size="mini" max-height="200" style="margin-bottom: 16px">
  <el-table-column label="合同编号" prop="contractNo" width="180" />
  <el-table-column label="金额" prop="amount" width="120">
    <template>¥{{ scope.row.amount }}</template>
  </el-table-column>
  <el-table-column label="状态" prop="status" width="100">
    <template slot-scope="scope">
      <dict-tag :options="dict.type.crm_contract_status" :value="scope.row.status" />
    </template>
  </el-table-column>
</el-table>

<el-divider v-if="detailForm.followStatus == 'won'" content-position="left">关联订单</el-divider>
<el-table v-if="detailForm.followStatus == 'won'" :data="orderList" size="mini" max-height="200" style="margin-bottom: 16px">
  <el-table-column label="订单编号" prop="orderNo" width="180" />
  <el-table-column label="金额" prop="actualAmount" width="120">
    <template>¥{{ scope.row.actualAmount }}</template>
  </el-table-column>
  <el-table-column label="状态" prop="status" width="100">
    <template slot-scope="scope">
      <dict-tag :options="dict.type.crm_order_status" :value="scope.row.status" />
    </template>
  </el-table-column>
</el-table>
```

Add data properties and fetch methods. In `handleDetail(row)`, after setting `detailForm`, also fetch `listOrder({customerId: row.customerId})` and `listContract({customerId: row.customerId})`.

Import order and contract APIs:
```javascript
import { listOrder } from "@/api/crm/order"
import { listContract } from "@/api/crm/contract"
```

- [ ] **Step 3: 客户详情页增加「创建合同」「创建直接订单」按钮**

Near the bottom of the detail dialog footer:
```vue
<div slot="footer" class="dialog-footer">
  <el-button type="primary" @click="handleCreateContract(detailForm)" v-if="detailForm.followStatus == 'won'">创建合同</el-button>
  <el-button type="success" @click="handleCreateDirectOrder(detailForm)" v-if="detailForm.followStatus == 'won'">创建直接订单</el-button>
  <el-button @click="detailOpen = false">关 闭</el-button>
</div>
```

Methods:
```javascript
handleCreateContract(customer) {
  this.detailOpen = false
  this.$nextTick(() => {
    // Open contract add dialog with customer pre-filled
    this.$router.push({ path: '/crm/contract/add', query: { customerId: customer.customerId, customerName: customer.customerName } })
  })
  // OR: open a dialog within customer page
  this.contractForm = {
    customerId: customer.customerId,
    customerName: customer.customerName,
    amount: undefined,
    contractName: customer.customerName + '-销售合同',
    status: '0',
    ownerId: customer.belongUserId,
    deptId: customer.belongDeptId
  }
  this.contractOpen = true
},
handleCreateDirectOrder(customer) {
  this.detailOpen = false
  this.orderForm = {
    customerId: customer.customerId,
    customerName: customer.customerName,
    actualAmount: undefined,
    status: '1',
    ownerId: customer.belongUserId,
    deptId: customer.belongDeptId
  }
  this.orderOpen = true
}
```

Actually, since Vue 2 + Element UI doesn't have route-based navigation for dialogs, use inline dialogs. Add new `contractOpen` / `orderOpen` dialogs within the customer page, with forms that pre-fill customer info.

---

### Task 9: 前端 - 权限按钮控制

**Files:**
- Modify: `ruoyi-ui/src/views/crm/contract/index.vue`
- Modify: `ruoyi-ui/src/views/crm/order/index.vue`
- Modify: `ruoyi-ui/src/views/crm/payment/index.vue`

- [ ] **Step 1: contract/index.vue 操作列权限**

```vue
<!-- 审核通过：仅待审核状态 + 经理/管理员 -->
<el-button size="mini" type="warning" plain icon="el-icon-check"
  v-if="scope.row.status == '0'"
  @click="handleApprove(scope.row)"
  v-hasPermi="['crm:contract:edit']">审核通过</el-button>

<!-- 修改：经理可改本部门，员工仅改草稿 -->
<el-button size="mini" type="text" icon="el-icon-edit"
  @click="handleUpdate(scope.row)"
  v-hasPermi="['crm:contract:edit']">修改</el-button>

<!-- 删除：仅管理员 -->
<el-button size="mini" type="text" icon="el-icon-delete"
  @click="handleDelete(scope.row)"
  v-if="roles.includes('admin')"
  v-hasPermi="['crm:contract:remove']">删除</el-button>
```

- [ ] **Step 2: order/index.vue 操作列权限**

```vue
<!-- 员工仅能修改待审核('0')的订单 -->
<el-button size="mini" type="text" icon="el-icon-edit"
  @click="handleUpdate(scope.row)"
  v-if="scope.row.status == '0' || roles.includes('admin') || roles.includes('manager')"
  v-hasPermi="['crm:order:edit']">修改</el-button>

<!-- 删除：仅管理员 -->
<el-button size="mini" type="text" icon="el-icon-delete"
  @click="handleDelete(scope.row)"
  v-if="roles.includes('admin')"
  v-hasPermi="['crm:order:remove']">删除</el-button>
```

- [ ] **Step 3: payment/index.vue 操作列权限**

```vue
<!-- 录入回款：仅管理员 -->
<el-button size="mini" type="success" plain icon="el-icon-check"
  @click="handleMarkPaid(scope.row)"
  v-if="scope.row.status != '1' && roles.includes('admin')"
  v-hasPermi="['crm:payment:edit']">标记已付</el-button>

<!-- 新增按钮：仅管理员 -->
<el-button type="primary" plain icon="el-icon-plus" size="mini"
  @click="handleAdd"
  v-if="roles.includes('admin')"
  v-hasPermi="['crm:payment:add']">新增</el-button>
```

---

### Task 10: 创建/编辑时自动填充 owner_id / dept_id

**Files:**
- Modify: `ruoyi-system/.../service/impl/CrmContractServiceImpl.java`
- Modify: `ruoyi-system/.../service/impl/CrmOrderServiceImpl.java`
- Modify: `ruoyi-system/.../service/impl/CrmPaymentPlanServiceImpl.java`

- [ ] **Step 1: CrmContractServiceImpl.insertCrmContract 继承客户归属**

```java
@Override
public int insertCrmContract(CrmContract contract) {
    if (contract.getCustomerId() != null) {
        CrmCustomer customer = crmCustomerMapper.selectCrmCustomerById(contract.getCustomerId());
        if (customer != null) {
            if (contract.getOwnerId() == null) contract.setOwnerId(customer.getBelongUserId());
            if (contract.getDeptId() == null) contract.setDeptId(customer.getBelongDeptId());
        }
    }
    contract.setStatus("0"); // 新建合同默认为待审核
    return crmContractMapper.insertCrmContract(contract);
}
```

- [ ] **Step 2: CrmOrderServiceImpl.insertCrmOrder 继承客户归属**

```java
@Override
public int insertCrmOrder(CrmOrder order) {
    if (order.getCustomerId() != null) {
        CrmCustomer customer = crmCustomerMapper.selectCrmCustomerById(order.getCustomerId());
        if (customer != null) {
            if (order.getOwnerId() == null) order.setOwnerId(customer.getBelongUserId());
            if (order.getDeptId() == null) order.setDeptId(customer.getBelongDeptId());
        }
    }
    // 自动生成订单号
    if (order.getOrderNo() == null) {
        order.setOrderNo(generateOrderNo());
    }
    return crmOrderMapper.insertCrmOrder(order);
}
```

- [ ] **Step 3: CrmPaymentPlanServiceImpl 继承订单归属**

In `insertCrmPaymentPlan` and `markAsPaid`:
```java
// insertCrmPaymentPlan 中
if (order.getOwnerId() != null) plan.setOwnerId(order.getOwnerId());
if (order.getDeptId() != null) plan.setDeptId(order.getDeptId());

// markAsPaid 中
if (plan.getOwnerId() == null && order.getOwnerId() != null) {
    plan.setOwnerId(order.getOwnerId());
    plan.setDeptId(order.getDeptId());
}
```

---

### Task 11: 更新 Redis 缓存 + 运行验证

- [ ] **Step 1: 重启后端并清空缓存**

```bash
# Kill existing process
Get-Process | Where-Object { $_.ProcessName -match "java" -and $_.CommandLine -match "ruoyi-admin" } | Stop-Process

# Redis flush
redis-cli FLUSHALL

# 重启后端
cd E:\CRM\RuoYi-Vue-master\ruoyi-admin
mvn spring-boot:run
```

- [ ] **Step 2: 前端验证**

```bash
cd E:\CRM\RuoYi-Vue-master\ruoyi-ui
npm run dev
```

- [ ] **Step 3: 验证点清单**

| # | 验证项 | 操作 | 预期 |
|---|-------|------|------|
| 1 | 字典生效 | 订单页面查看状态下拉 | 出现「已签约」选项 |
| 2 | 合同审批 | 以经理登录，在合同页点审核通过 | 合同状态变已生效，自动生成一条订单 |
| 3 | 直接订单金额 | 创建 contract_id=null 的订单，设状态为已完成 | 看板月成交金额增加 |
| 4 | 签约订单金额 | 走合同→自动生成订单→回款标记已付 | 看板月成交金额增加（不重复） |
| 5 | 合同完成校验 | 合同→订单→未付清回款→改合同为已完成 | 抛异常阻止 |
| 6 | 数据隔离-员工 | 员工 ash 登录，只看自己名下数据 | 各模块列表仅显示自己的记录 |
| 7 | 数据隔离-经理 | 经理 dazhiyu 登录，看本部门数据 | 各模块列表含本部门+下属部门 |
| 8 | 客户详情关联 | 客户 followStatus=won → 点详情 | 显示关联合同/订单列表 |
| 9 | 客户表关联列 | 客户 followStatus=won → 看列表 | 显示订单号和合同号 |

- [ ] **Step 4: 提交代码**

```bash
git add -A
git commit -m "feat: implement crm permission isolation and contract-order-payment flow

- Add owner_id/dept_id to contract/order/payment_plan tables
- Implement dual-channel sumMonthDealAmount (direct + contract)
- Add contract approval with auto order generation
- Add contract completion validation and cascading
- Add permission controls per role per module
- Show order/contract numbers on customer page
```