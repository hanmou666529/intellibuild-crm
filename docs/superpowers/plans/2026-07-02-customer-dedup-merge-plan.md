# 客户查重与合并 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 新建客户时自动查重（公司名/手机号/邮箱），重复时发起撞单争议，经理协调/超管仲裁后合并客户数据

**Architecture:** 后端新增 `crm_customer_dispute` + `crm_customer_merge_log` 两张表; Service 层新增争议 CRUD + 合并核心逻辑; Controller 暴露查重/争议/合并 API; 前端新增争议管理页 + 修改客户新建弹窗 + 客户列表合并按钮

**Tech Stack:** Spring Boot + MyBatis + MySQL + Vue2 + Element UI

---

### Task 1: 数据库变更（建表 + 改表）

**Files:**
- Create: `sql/customer_dedup_merge.sql`
- Execute against MySQL

- [ ] **Step 1: 编写建表 SQL**

**`sql/customer_dedup_merge.sql`**

```sql
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
```

- [ ] **Step 2: 执行 SQL**

```bash
mysql -uroot -p"Hae147258369" ry-vue < "E:\CRM\RuoYi-Vue-master\sql\customer_dedup_merge.sql"
```

---

### Task 2: 后端 Domain 类

**Files:**
- Create: `ruoyi-system/src/main/java/com/ruoyi/crm/domain/CrmCustomerDispute.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/crm/domain/CrmCustomerMergeLog.java`
- Modify: `ruoyi-system/src/main/java/com/ruoyi/crm/domain/CrmCustomer.java`

- [ ] **Step 1: 创建 CrmCustomerDispute.java**

```java
package com.ruoyi.crm.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class CrmCustomerDispute extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Excel(name = "争议ID")
    private Long disputeId;

    @Excel(name = "客户ID")
    private Long customerId;

    @Excel(name = "发起人")
    private Long initiatorUserId;

    @Excel(name = "当前负责人")
    private Long targetUserId;

    @Excel(name = "争议原因")
    private String reason;

    @Excel(name = "状态")
    private String status;

    @Excel(name = "结果")
    private String result;

    @Excel(name = "处理人ID")
    private Long handlerId;

    @Excel(name = "处理时间")
    private Date handleTime;

    @Excel(name = "处理备注")
    private String remark;

    // 非持久化字段
    private String customerName;
    private String initiatorName;
    private String targetUserName;
    private String handlerName;

    // ... getters/setters (all fields)
}
```

- [ ] **Step 2: 创建 CrmCustomerMergeLog.java**

```java
package com.ruoyi.crm.domain;

public class CrmCustomerMergeLog {
    private Long mergeId;
    private Long keepCustomerId;
    private Long mergeCustomerId;
    private String keepCustomerName;
    private String mergeCustomerName;
    private String mergedFields;
    private String createBy;
    private Date createTime;
    // ... getters/setters
}
```

- [ ] **Step 3: 修改 CrmCustomer.java**

在 delFlag 字段后添加：
```java
/** 合并到的目标客户ID */
private Long mergeToId;
```
添加 getter/setter。

---

### Task 3: 后端 Mapper（接口 + XML + 关联表 customerId 更新）

**Files:**
- Create: `CrmCustomerDisputeMapper.java`
- Create: `CrmCustomerDisputeMapper.xml`
- Create: `CrmCustomerMergeLogMapper.java`
- Create: `CrmCustomerMergeLogMapper.xml`
- Modify: `CrmCustomerMapper.xml`（checkDuplicate + deleteByMerge + 更新 merge_to_id）
- Modify: `CrmFollowupMapper.xml`（add updateCustomerId）
- Modify: `CrmPipelineMapper.xml`（add updateCustomerId）
- Modify: `CrmContractMapper.xml`（add updateCustomerId）
- Modify: `CrmOrderMapper.xml`（add updateCustomerId）
- Modify: `CrmCustomerShareMapper.xml`（add updateCustomerId）

- [ ] **Step 1: CrmCustomerDisputeMapper 接口**

```java
@Repository
public interface CrmCustomerDisputeMapper {
    List<CrmCustomerDispute> selectList(CrmCustomerDispute dispute);
    CrmCustomerDispute selectById(Long disputeId);
    int insert(CrmCustomerDispute dispute);
    int update(CrmCustomerDispute dispute);
    int closeByCustomerId(Long customerId);
}
```

- [ ] **Step 2: CrmCustomerDisputeMapper.xml**

```xml
<mapper namespace="com.ruoyi.crm.mapper.CrmCustomerDisputeMapper">
    <resultMap type="CrmCustomerDispute" id="CrmCustomerDisputeResult">
        <result property="disputeId" column="dispute_id"/>
        <result property="customerId" column="customer_id"/>
        <result property="initiatorUserId" column="initiator_user_id"/>
        <result property="targetUserId" column="target_user_id"/>
        <result property="reason" column="reason"/>
        <result property="status" column="status"/>
        <result property="result" column="result"/>
        <result property="handlerId" column="handler_id"/>
        <result property="handleTime" column="handle_time"/>
        <result property="remark" column="remark"/>
        <result property="createBy" column="create_by"/>
        <result property="createTime" column="create_time"/>
        <result property="updateBy" column="update_by"/>
        <result property="updateTime" column="update_time"/>
        <result property="customerName" column="customer_name"/>
        <result property="initiatorName" column="initiator_name"/>
        <result property="targetUserName" column="target_user_name"/>
        <result property="handlerName" column="handler_name"/>
    </resultMap>

    <sql id="selectVo">
        select d.*, cu.customer_name,
               u1.user_name as initiator_name,
               u2.user_name as target_user_name,
               u3.user_name as handler_name
        from crm_customer_dispute d
        left join crm_customer cu on d.customer_id = cu.customer_id
        left join sys_user u1 on d.initiator_user_id = u1.user_id
        left join sys_user u2 on d.target_user_id = u2.user_id
        left join sys_user u3 on d.handler_id = u3.user_id
    </sql>

    <select id="selectList" parameterType="CrmCustomerDispute" resultMap="CrmCustomerDisputeResult">
        <include refid="selectVo"/>
        <where>
            <if test="status != null and status != ''"> and d.status = #{status}</if>
            <if test="initiatorUserId != null"> and d.initiator_user_id = #{initiatorUserId}</if>
        </where>
        order by d.create_time desc
    </select>

    <select id="selectById" resultMap="CrmCustomerDisputeResult">
        <include refid="selectVo"/> where d.dispute_id = #{disputeId}
    </select>

    <insert id="insert" parameterType="CrmCustomerDispute" useGeneratedKeys="true" keyProperty="disputeId">
        insert into crm_customer_dispute (<include refid="insertColumns"/>) values (<include refid="insertValues"/>)
    </insert>

    <update id="update" parameterType="CrmCustomerDispute">
        update crm_customer_dispute
        <set>
            <if test="status != null">status = #{status},</if>
            <if test="result != null">result = #{result},</if>
            <if test="handlerId != null">handler_id = #{handlerId},</if>
            <if test="handleTime != null">handle_time = #{handleTime},</if>
            <if test="remark != null">remark = #{remark},</if>
            update_time = sysdate()
        </set>
        where dispute_id = #{disputeId}
    </update>

    <update id="closeByCustomerId">
        update crm_customer_dispute set status = '3', update_time = sysdate() where customer_id = #{customerId} and status = '0'
    </update>
</mapper>
```

- [ ] **Step 3: CrmCustomerMergeLog 相关 Mapper**（简单 insert + selectList）

- [ ] **Step 4: CrmCustomerMapper.xml 加查重 SQL**

```xml
<select id="checkDuplicate" parameterType="CrmCustomer" resultMap="CrmCustomerResult">
    <include refid="selectCrmCustomerVo"/>
    where c.del_flag = '0'
      and (
          <if test="customerName != null and customerName != ''"> c.customer_name = #{customerName} or</if>
          <if test="phone != null and phone != ''"> c.phone = #{phone} or</if>
          <if test="email != null and email != ''"> c.email = #{email} or</if>
          <if test="company != null and company != ''"> c.company = #{company} or</if>
          1=0
      )
    limit 5
</select>

<update id="deleteByMerge">
    update crm_customer set del_flag = '3', merge_to_id = #{mergeToId}, update_time = sysdate()
    where customer_id = #{customerId}
</update>
```

- [ ] **Step 5: 各关联表加 updateCustomerId**

每个 Mapper XML 加：
```xml
<update id="updateCustomerId">
    update ${tableName} set customer_id = #{newId}, update_time = sysdate()
    where customer_id = #{oldId}
</update>
```
涉及的 Mapper 接口加方法：`int updateCustomerId(@Param("oldId") Long oldId, @Param("newId") Long newId);`
涉及的文件：CrmFollowupMapper.xm， CrmPipelineMapper.xml, CrmContractMapper.xml, CrmOrderMapper.xml, CrmCustomerShareMapper.xml（不含 update_time 字段），CrmCustomerPoolLogMapper.xml

---

### Task 4: 后端 Service 接口

**Files:**
- Create: `ICrmCustomerDisputeService.java`

- [ ] **Step 1: 创建接口**

```java
public interface ICrmCustomerDisputeService {
    List<CrmCustomerDispute> selectList(CrmCustomerDispute dispute);
    CrmCustomerDispute selectById(Long disputeId);
    int insert(CrmCustomerDispute dispute);
    int handle(Long disputeId, String action, Long targetUserId, String remark);
    int arbitrate(Long disputeId, String result, String remark);
}
```

- [ ] **Step 2: ICrmCustomerService 加方法**

```java
List<CrmCustomer> checkDuplicate(CrmCustomer customer);
void mergeCustomer(Long keepCustomerId, Long mergeCustomerId);
```

---

### Task 5: 后端 Service 实现

**Files:**
- Create: `CrmCustomerDisputeServiceImpl.java`
- Modify: `CrmCustomerServiceImpl.java`

- [ ] **Step 1: 实现 CrmCustomerDisputeServiceImpl**

```java
@Service
public class CrmCustomerDisputeServiceImpl implements ICrmCustomerDisputeService {
    @Autowired
    private CrmCustomerDisputeMapper disputeMapper;
    @Autowired
    private ICrmCustomerService customerService;

    @Override
    public List<CrmCustomerDispute> selectList(CrmCustomerDispute dispute) {
        if (!SecurityUtils.isAdmin(SecurityUtils.getUserId())) {
            dispute.setInitiatorUserId(SecurityUtils.getUserId());
        }
        return disputeMapper.selectList(dispute);
    }

    @Override
    @Transactional
    public int handle(Long disputeId, String action, Long targetUserId, String remark) {
        CrmCustomerDispute d = disputeMapper.selectById(disputeId);
        if (d == null || !"0".equals(d.getStatus())) throw new ServiceException("争议不可处理");
        d.setStatus("1");
        d.setHandlerId(SecurityUtils.getUserId());
        d.setHandleTime(new Date());
        d.setRemark(remark);
        if ("assign".equals(action) && targetUserId != null) {
            d.setResult("A".equals(targetUserId.toString()) ? "A" : "B");
            // TODO: 争议处理后是否自动变更负责人？等待合并时一起处理
        }
        return disputeMapper.update(d);
    }

    @Override
    @Transactional
    public int arbitrate(Long disputeId, String result, String remark) {
        CrmCustomerDispute d = disputeMapper.selectById(disputeId);
        if (d == null) throw new ServiceException("争议不存在");
        d.setStatus("2");
        d.setResult(result);
        d.setHandlerId(SecurityUtils.getUserId());
        d.setHandleTime(new Date());
        d.setRemark(remark);
        disputeMapper.update(d);

        // 仲裁结果决定合并方向
        CrmCustomer customer = customerService.selectCrmCustomerById(d.getCustomerId());
        if (customer == null) throw new ServiceException("客户不存在");

        if ("A".equals(result) && d.getTargetUserId() != null) {
            // 归发起人 → 保留客户，负责人改为发起人
            customer.setBelongUserId(d.getInitiatorUserId());
            customerService.updateCrmCustomer(customer);
        }
        // 归原负责人则不变
        return 1;
    }
}
```

- [ ] **Step 2: CrmCustomerServiceImpl 加合并核心方法**

```java
@Override
@Transactional
public void mergeCustomer(Long keepCustomerId, Long mergeCustomerId) {
    if (keepCustomerId.equals(mergeCustomerId)) throw new ServiceException("不能合并相同客户");
    CrmCustomer keep = crmCustomerMapper.selectCrmCustomerById(keepCustomerId);
    CrmCustomer merge = crmCustomerMapper.selectCrmCustomerById(mergeCustomerId);
    if (keep == null || merge == null) throw new ServiceException("客户不存在");
    if (!"0".equals(keep.getDelFlag()) || !"0".equals(merge.getDelFlag())) throw new ServiceException("客户已删除");

    // 转移关联数据
    crmFollowupMapper.updateCustomerId(mergeCustomerId, keepCustomerId);
    crmPipelineMapper.updateCustomerId(mergeCustomerId, keepCustomerId);
    crmContractMapper.updateCustomerId(mergeCustomerId, keepCustomerId);
    crmOrderMapper.updateCustomerId(mergeCustomerId, keepCustomerId);
    crmCustomerShareMapper.updateCustomerId(mergeCustomerId, keepCustomerId);

    // 软删被合并客户
    crmCustomerMapper.deleteByMerge(mergeCustomerId, keepCustomerId);

    // 关闭争议
    crmDisputeMapper.closeByCustomerId(mergeCustomerId);

    // 记录合并日志
    CrmCustomerMergeLog log = new CrmCustomerMergeLog();
    log.setKeepCustomerId(keepCustomerId);
    log.setMergeCustomerId(mergeCustomerId);
    log.setKeepCustomerName(keep.getCustomerName());
    log.setMergeCustomerName(merge.getCustomerName());
    log.setCreateBy(SecurityUtils.getUsername());
    mergeLogMapper.insert(log);
}
```

查重方法：
```java
@Override
public List<CrmCustomer> checkDuplicate(CrmCustomer customer) {
    return crmCustomerMapper.checkDuplicate(customer);
}
```

---

### Task 6: 后端 Controller

**Files:**
- Create: `CrmCustomerDisputeController.java`
- Modify: `CrmCustomerController.java`

- [ ] **Step 1: CrmCustomerDisputeController.java**

```java
@RestController
@RequestMapping("/crm/dispute")
public class CrmCustomerDisputeController extends BaseController {
    @Autowired
    private ICrmCustomerDisputeService disputeService;

    @PreAuthorize("@ss.hasPermi('crm:dispute:list')")
    @GetMapping("/list")
    public TableDataInfo list(CrmCustomerDispute dispute) {
        startPage();
        return getDataTable(disputeService.selectList(dispute));
    }

    @PreAuthorize("@ss.hasPermi('crm:dispute:handle')")
    @Log(title = "争议管理", businessType = BusinessType.UPDATE)
    @PutMapping("/handle")
    public AjaxResult handle(@RequestBody Map<String, Object> params) {
        Long disputeId = Long.valueOf(params.get("disputeId").toString());
        String action = (String) params.get("action");
        Long targetUserId = params.get("targetUserId") != null ? Long.valueOf(params.get("targetUserId").toString()) : null;
        String remark = (String) params.get("remark");
        return toAjax(disputeService.handle(disputeId, action, targetUserId, remark));
    }

    @PreAuthorize("@ss.hasPermi('crm:dispute:arbitrate')")
    @Log(title = "争议管理", businessType = BusinessType.UPDATE)
    @PutMapping("/arbitrate")
    public AjaxResult arbitrate(@RequestBody Map<String, Object> params) {
        Long disputeId = Long.valueOf(params.get("disputeId").toString());
        String result = (String) params.get("result");
        String remark = (String) params.get("remark");
        return toAjax(disputeService.arbitrate(disputeId, result, remark));
    }
}
```

- [ ] **Step 2: CrmCustomerController 加查重 + 合并**

```java
@PreAuthorize("@ss.hasPermi('crm:customer:list')")
@GetMapping("/checkDuplicate")
public AjaxResult checkDuplicate(CrmCustomer customer) {
    if (StringUtils.isAllEmpty(customer.getCustomerName(), customer.getPhone(), customer.getEmail(), customer.getCompany())) {
        return error("至少提供一个查重字段");
    }
    List<CrmCustomer> list = crmCustomerService.checkDuplicate(customer);
    Map<String, Object> result = new HashMap<>();
    result.put("duplicate", !list.isEmpty());
    result.put("matches", list);
    return success(result);
}

@PreAuthorize("@ss.hasPermi('crm:customer:merge')")
@Log(title = "客户管理", businessType = BusinessType.UPDATE)
@PutMapping("/merge")
public AjaxResult merge(@RequestBody Map<String, Object> params) {
    Long keepCustomerId = Long.valueOf(params.get("keepCustomerId").toString());
    Long mergeCustomerId = Long.valueOf(params.get("mergeCustomerId").toString());
    crmCustomerService.mergeCustomer(keepCustomerId, mergeCustomerId);
    return success("合并成功");
}
```

- [ ] **Step 3: 修改 add 方法支持争议创建**

在 `CrmCustomerController.add()` 中加逻辑：如果请求参数 `initiateDispute` 为 true 且 customerId 不为空说明要创建争议。

实际上更简单：前端传 `initiateDispute: true` + `duplicateCustomerId`，后端创建客户后自动建争议记录。

修改 `add` 方法：
```java
@PostMapping
public AjaxResult add(@RequestBody CrmCustomer customer) {
    customer.setCreateBy(getUsername());
    int result = crmCustomerService.insertCrmCustomer(customer);
    // 如果标记了争议，创建争议记录
    if (result > 0 && customer.getParams().get("initiateDispute") != null
            && Boolean.TRUE.equals(customer.getParams().get("initiateDispute"))) {
        Long targetCustomerId = customer.getParams().get("duplicateCustomerId") != null
                ? Long.valueOf(customer.getParams().get("duplicateCustomerId").toString()) : null;
        if (targetCustomerId != null) {
            CrmCustomer target = crmCustomerService.selectCrmCustomerById(targetCustomerId);
            if (target != null) {
                CrmCustomerDispute dispute = new CrmCustomerDispute();
                dispute.setCustomerId(customer.getCustomerId());
                dispute.setInitiatorUserId(SecurityUtils.getUserId());
                dispute.setTargetUserId(target.getBelongUserId());
                dispute.setReason("新建客户时发现重复，发起撞单争议");
                dispute.setStatus("0");
                dispute.setCreateBy(getUsername());
                disputeService.insert(dispute);
            }
        }
    }
    return toAjax(result);
}
```

---

### Task 7: 前端 API

**Files:**
- Create: `ruoyi-ui/src/api/crm/dispute.js`
- Modify: `ruoyi-ui/src/api/crm/customer.js`

- [ ] **Step 1: ruoyi-ui/src/api/crm/dispute.js**

```javascript
import request from '@/utils/request'

export function listDispute(query) {
  return request({
    url: '/crm/dispute/list',
    method: 'get',
    params: query
  })
}

export function handleDispute(data) {
  return request({
    url: '/crm/dispute/handle',
    method: 'put',
    data: data
  })
}

export function arbitrateDispute(data) {
  return request({
    url: '/crm/dispute/arbitrate',
    method: 'put',
    data: data
  })
}
```

- [ ] **Step 2: ruoyi-ui/src/api/crm/customer.js 加两方法**

```javascript
export function checkDuplicate(query) {
  return request({
    url: '/crm/customer/checkDuplicate',
    method: 'get',
    params: query
  })
}

export function mergeCustomer(data) {
  return request({
    url: '/crm/customer/merge',
    method: 'put',
    data: data
  })
}
```

---

### Task 8: 前端争议管理页

**Files:**
- Create: `ruoyi-ui/src/views/crm/dispute/index.vue`

- [ ] **Step 1: 创建页面**

```vue
<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="争议状态" clearable style="width: 240px">
          <el-option label="待处理" value="0" />
          <el-option label="经理已处理" value="1" />
          <el-option label="超管已仲裁" value="2" />
          <el-option label="已关闭" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="disputeList">
      <el-table-column label="客户名称" align="center" prop="customerName" />
      <el-table-column label="发起人" align="center" prop="initiatorName" />
      <el-table-column label="当前负责人" align="center" prop="targetUserName" />
      <el-table-column label="原因" align="center" prop="reason" :show-overflow-tooltip="true" />
      <el-table-column label="状态" align="center" prop="status" width="120">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '0' ? 'warning' : scope.row.status === '1' ? 'info' : scope.row.status === '2' ? 'success' : 'info'">
            {{ { '0': '待处理', '1': '经理已处理', '2': '超管已仲裁', '3': '已关闭' }[scope.row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="140">
        <template slot-scope="scope">{{ parseTime(scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleDetail(scope.row)">详情</el-button>
          <el-button size="mini" type="warning" text icon="el-icon-s-check" @click="handleDispute(scope.row)" v-if="scope.row.status === '0' && hasPermi('crm:dispute:handle')">处理</el-button>
          <el-button size="mini" type="danger" text icon="el-icon-scale-to-original" @click="handleArbitrate(scope.row)" v-if="scope.row.status !== '2' && scope.row.status !== '3' && hasPermi('crm:dispute:arbitrate')">仲裁</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="争议详情" :visible.sync="detailOpen" width="600px">
      <el-form label-width="100px" size="mini">
        <el-form-item label="客户名称：">{{ detailForm.customerName }}</el-form-item>
        <el-form-item label="发起人：">{{ detailForm.initiatorName }}</el-form-item>
        <el-form-item label="当前负责人：">{{ detailForm.targetUserName }}</el-form-item>
        <el-form-item label="争议原因：">{{ detailForm.reason }}</el-form-item>
        <el-form-item label="处理备注：">{{ detailForm.remark || '无' }}</el-form-item>
      </el-form>
    </el-dialog>

    <el-dialog title="处理争议" :visible.sync="handleOpen" width="500px">
      <el-form ref="handleForm" :model="handleForm" :rules="handleRules" label-width="100px">
        <el-form-item label="操作">
          <el-radio-group v-model="handleForm.action">
            <el-radio label="assign">指定归属</el-radio>
            <el-radio label="escalate">上报超管</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="归属人" v-if="handleForm.action === 'assign'" prop="targetUserId">
          <el-select v-model="handleForm.targetUserId" placeholder="请选择" filterable style="width: 100%">
            <el-option label="归发起人" :value="handleForm.initiatorUserId" />
            <el-option label="归原负责人" :value="handleForm.targetUserId" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input type="textarea" v-model="handleForm.remark" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button type="primary" @click="submitHandle">确 定</el-button>
        <el-button @click="handleOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="仲裁" :visible.sync="arbitrateOpen" width="500px">
      <el-form ref="arbitrateForm" :model="arbitrateForm" :rules="arbitrateRules" label-width="100px">
        <el-form-item label="仲裁结果" prop="result">
          <el-radio-group v-model="arbitrateForm.result">
            <el-radio label="A">归发起人（{{ arbitrateForm.initiatorName }}）</el-radio>
            <el-radio label="B">归原负责人（{{ arbitrateForm.targetUserName }}）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="arbitrateForm.remark" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button type="primary" @click="submitArbitrate">确 定</el-button>
        <el-button @click="arbitrateOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listDispute, handleDispute, arbitrateDispute } from "@/api/crm/dispute"

export default {
  name: "CrmDispute",
  dicts: [],
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      disputeList: [],
      detailOpen: false,
      detailForm: {},
      handleOpen: false,
      handleForm: { action: 'assign', targetUserId: undefined, initiatorUserId: undefined, originalUserId: undefined, remark: undefined },
      handleRules: {},
      arbitrateOpen: false,
      arbitrateForm: { result: undefined, remark: undefined, initiatorName: undefined, targetUserName: undefined },
      arbitrateRules: { result: [{ required: true, message: '请选择仲裁结果', trigger: 'change' }] },
      queryParams: { pageNum: 1, pageSize: 10, status: undefined }
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listDispute(this.queryParams).then(res => { this.disputeList = res.rows; this.total = res.total; this.loading = false })
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    handleDetail(row) { this.detailForm = row; this.detailOpen = true },
    handleDispute(row) {
      this.handleForm = { disputeId: row.disputeId, action: 'assign', targetUserId: undefined, initiatorUserId: row.initiatorUserId, originalUserId: row.targetUserId, remark: undefined }
      this.handleOpen = true
    },
    submitHandle() {
      handleDispute(this.handleForm).then(() => { this.$modal.msgSuccess("处理成功"); this.handleOpen = false; this.getList() })
    },
    handleArbitrate(row) {
      this.arbitrateForm = { disputeId: row.disputeId, result: undefined, remark: undefined, initiatorName: row.initiatorName, targetUserName: row.targetUserName }
      this.arbitrateOpen = true
    },
    submitArbitrate() {
      this.$refs["arbitrateForm"].validate(valid => {
        if (valid) {
          arbitrateDispute(this.arbitrateForm).then(() => { this.$modal.msgSuccess("仲裁成功"); this.arbitrateOpen = false; this.getList() })
        }
      })
    },
    hasPermi(perm) { return this.$store.getters.permissions?.includes(perm) }
  }
}
</script>
```

---

### Task 9: 前端客户管理页改动

**Files:**
- Modify: `ruoyi-ui/src/views/crm/customer/index.vue`

- [ ] **Step 1: submitForm 中加查重逻辑**

在 `handleAdd` 和 `submitForm` 之间加查重步骤。修改 `submitForm` 方法：

```javascript
submitForm() {
  this.$refs["form"].validate(valid => {
    if (valid) {
      // 编辑模式直接提交
      if (this.form.customerId != undefined) {
        this.submitCustomer()
        return
      }
      // 新增模式 - 先查重
      const dedupData = {
        customerName: this.form.customerName,
        phone: this.form.phone,
        email: this.form.email,
        company: this.form.company
      }
      checkDuplicate(dedupData).then(res => {
        if (res.duplicate && res.matches.length > 0) {
          const match = res.matches[0]
          this.$alert(
            `<p>该公司名/手机号/邮箱已被 <b>${match.customerName}</b> 使用</p>
             <p>负责人：<b>${match.belongUserName || '未知'}</b></p>`,
            '客户重复提示',
            {
              dangerouslyUseHTMLString: true,
              confirmButtonText: '发起争议',
              cancelButtonText: '取消新建',
              showCancelButton: true,
              type: 'warning'
            }
          ).then(() => {
            // 发起争议 - 携带标记
            this.form.params = {
              initiateDispute: true,
              duplicateCustomerId: match.customerId
            }
            this.submitCustomer()
          }).catch(() => {
            // 取消新建
          })
        } else {
          this.submitCustomer()
        }
      })
    }
  })
},
submitCustomer() {
  const data = { ...this.form }
  // ... 现有逻辑
  addCustomer(data).then(response => {
    this.$modal.msgSuccess("新增成功")
    this.open = false
    this.getList()
  })
}
```

- [ ] **Step 2: 客户列表加合并按钮**

在按钮行加：
```vue
<el-col :span="1.5">
  <el-button type="warning" plain icon="el-icon-sort" size="mini" :disabled="multiple" @click="handleMerge" v-hasPermi="['crm:customer:merge']">合并客户</el-button>
</el-col>
```

- [ ] **Step 3: 加合并对话框和合并方法**

```javascript
handleMerge() {
  if (this.ids.length !== 2) {
    this.$modal.msgWarning("请选择两条客户记录进行合并")
    return
  }
  const c1 = this.customerList.find(c => c.customerId === this.ids[0])
  const c2 = this.customerList.find(c => c.customerId === this.ids[1])
  if (!c1 || !c2) return
  this.mergeForm = {
    option1: { ...c1 },
    option2: { ...c2 },
    keepCustomerId: c1.customerId
  }
  this.mergeOpen = true
},
submitMerge() {
  this.$modal.confirm(
    `确认将"${this.mergeForm.option2.customerName}"合并到"${this.mergeForm.option1.customerName}"？此操作不可撤销。`
  ).then(() => {
    const data = {
      keepCustomerId: this.mergeForm.keepCustomerId,
      mergeCustomerId: this.mergeForm.keepCustomerId === this.mergeForm.option1.customerId
        ? this.mergeForm.option2.customerId : this.mergeForm.option1.customerId
    }
    mergeCustomer(data).then(() => {
      this.$modal.msgSuccess("合并成功")
      this.mergeOpen = false
      this.getList()
    })
  })
}
```

---

### Task 10: 菜单 + 权限 + Router

**Files:**
- Modify: `ruoyi-ui/src/router/index.js`

- [ ] **Step 1: 争议管理加到 dynamicRoutes**

```javascript
{
  path: '/crm/dispute',
  component: Layout,
  hidden: false,
  children: [
    {
      path: 'index',
      component: () => import('@/views/crm/dispute/index'),
      name: 'CrmDispute',
      meta: { title: '争议管理', icon: 'warning' }
    }
  ]
}
```

放在 /crm/approval 后面。

- [ ] **Step 2: 执行菜单 SQL**

```sql
-- 争议管理菜单 (parent=2000 智营CRM)
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3023, '争议管理', 2000, 12, 'dispute', 'crm/dispute/index', 'CrmDispute', '', 1, 0, 'C', 0, 0, 'crm:dispute:list', 'warning', 'admin', sysdate());

-- 按钮权限
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3024, '争议处理', 3023, 1, '', '', '', '', 1, 0, 'F', 0, 0, 'crm:dispute:handle', '#', 'admin', sysdate());
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (3025, '争议仲裁', 3023, 2, '', '', '', '', 1, 0, 'F', 0, 0, 'crm:dispute:arbitrate', '#', 'admin', sysdate());

-- admin 角色关联
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3023);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3024);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 3025);
```

---

### Task 11: 编译 + 部署 + SQL 文件归档

**Files:**
- Modify: `sql/approval_engine.sql`（追加新 SQL）

- [ ] **Step 1: 执行菜单 SQL**
- [ ] **Step 2: 追加新表/菜单到 `sql/customer_dedup_merge.sql` 并归档**
- [ ] **Step 3: 编译后端** `mvn compile -pl ruoyi-system -am -q`
- [ ] **Step 4: 重启后端 + FLUSHALL**
- [ ] **Step 5: 用户重新登录验证**
