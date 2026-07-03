# Task 2: 后端 Domain 类

## What to do
Create 2 new domain classes and modify 1 existing domain class.

### 1. Create `ruoyi-system/src/main/java/com/ruoyi/crm/domain/CrmCustomerDispute.java`

```java
package com.ruoyi.crm.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class CrmCustomerDispute extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long disputeId;
    private Long customerId;
    private Long initiatorUserId;
    private Long targetUserId;
    private String reason;
    private String status;
    private String result;
    private Long handlerId;
    private Date handleTime;
    private String remark;

    // 非持久化（关联查询）
    private String customerName;
    private String initiatorName;
    private String targetUserName;
    private String handlerName;

    public Long getDisputeId() { return disputeId; }
    public void setDisputeId(Long disputeId) { this.disputeId = disputeId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getInitiatorUserId() { return initiatorUserId; }
    public void setInitiatorUserId(Long initiatorUserId) { this.initiatorUserId = initiatorUserId; }
    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public Long getHandlerId() { return handlerId; }
    public void setHandlerId(Long handlerId) { this.handlerId = handlerId; }
    public Date getHandleTime() { return handleTime; }
    public void setHandleTime(Date handleTime) { this.handleTime = handleTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getInitiatorName() { return initiatorName; }
    public void setInitiatorName(String initiatorName) { this.initiatorName = initiatorName; }
    public String getTargetUserName() { return targetUserName; }
    public void setTargetUserName(String targetUserName) { this.targetUserName = targetUserName; }
    public String getHandlerName() { return handlerName; }
    public void setHandlerName(String handlerName) { this.handlerName = handlerName; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("disputeId", getDisputeId())
            .append("customerId", getCustomerId())
            .append("status", getStatus())
            .toString();
    }
}
```

### 2. Create `ruoyi-system/src/main/java/com/ruoyi/crm/domain/CrmCustomerMergeLog.java`

```java
package com.ruoyi.crm.domain;

import java.util.Date;

public class CrmCustomerMergeLog {
    private static final long serialVersionUID = 1L;

    private Long mergeId;
    private Long keepCustomerId;
    private Long mergeCustomerId;
    private String keepCustomerName;
    private String mergeCustomerName;
    private String mergedFields;
    private String createBy;
    private Date createTime;

    public Long getMergeId() { return mergeId; }
    public void setMergeId(Long mergeId) { this.mergeId = mergeId; }
    public Long getKeepCustomerId() { return keepCustomerId; }
    public void setKeepCustomerId(Long keepCustomerId) { this.keepCustomerId = keepCustomerId; }
    public Long getMergeCustomerId() { return mergeCustomerId; }
    public void setMergeCustomerId(Long mergeCustomerId) { this.mergeCustomerId = mergeCustomerId; }
    public String getKeepCustomerName() { return keepCustomerName; }
    public void setKeepCustomerName(String keepCustomerName) { this.keepCustomerName = keepCustomerName; }
    public String getMergeCustomerName() { return mergeCustomerName; }
    public void setMergeCustomerName(String mergeCustomerName) { this.mergeCustomerName = mergeCustomerName; }
    public String getMergedFields() { return mergedFields; }
    public void setMergedFields(String mergedFields) { this.mergedFields = mergedFields; }
    public String getCreateBy() { return createBy; }
    public void setCreateBy(String createBy) { this.createBy = createBy; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
```

### 3. Modify `ruoyi-system/src/main/java/com/ruoyi/crm/domain/CrmCustomer.java`

Add a `mergeToId` field after `delFlag`:
```java
/** 合并到的目标客户ID */
private Long mergeToId;

public Long getMergeToId() { return mergeToId; }
public void setMergeToId(Long mergeToId) { this.mergeToId = mergeToId; }
```

## Context
- Project root: `E:\CRM\RuoYi-Vue-master`
- Existing domain classes are in `ruoyi-system/src/main/java/com/ruoyi/crm/domain/`
- Follow the exact same style as the existing CrmCustomer.java (BaseEntity extension, ToStringBuilder, etc.)
- All fields must have getter/setter methods

## Deliverables
Create/modify the three Java files listed above.
