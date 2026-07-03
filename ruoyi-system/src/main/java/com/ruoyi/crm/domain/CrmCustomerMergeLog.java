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
