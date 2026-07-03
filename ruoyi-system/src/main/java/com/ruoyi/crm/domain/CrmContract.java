package com.ruoyi.crm.domain;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class CrmContract extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long contractId;

    @Excel(name = "合同编号")
    private String contractNo;

    @Excel(name = "合同名称")
    private String contractName;

    private Long customerId;

    private Long orderId;

    @Excel(name = "合同金额")
    private Double amount;

    @Excel(name = "签约日期", dateFormat = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date signDate;

    @Excel(name = "开始日期", dateFormat = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    @Excel(name = "结束日期", dateFormat = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    @Excel(name = "合同状态", dictType = "crm_contract_status")
    private String status;

    private String attachment;

    private String delFlag;

    private Long belongUserId;

    private Long belongDeptId;

    @Excel(name = "客户名称")
    private String customerName;

    @Excel(name = "累计审批次数")
    private Integer approvalCount;

    @Excel(name = "最后审批时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastApprovalTime;

    private List<CrmContractDetail> details;

    private Boolean hasLockedOrder;

    public Long getContractId()
    {
        return contractId;
    }

    public void setContractId(Long contractId)
    {
        this.contractId = contractId;
    }

    public String getContractNo()
    {
        return contractNo;
    }

    public void setContractNo(String contractNo)
    {
        this.contractNo = contractNo;
    }

    public String getContractName()
    {
        return contractName;
    }

    public void setContractName(String contractName)
    {
        this.contractName = contractName;
    }

    public Long getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(Long customerId)
    {
        this.customerId = customerId;
    }

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public Double getAmount()
    {
        return amount;
    }

    public void setAmount(Double amount)
    {
        this.amount = amount;
    }

    public Date getSignDate()
    {
        return signDate;
    }

    public void setSignDate(Date signDate)
    {
        this.signDate = signDate;
    }

    public Date getStartDate()
    {
        return startDate;
    }

    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }

    public Date getEndDate()
    {
        return endDate;
    }

    public void setEndDate(Date endDate)
    {
        this.endDate = endDate;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getAttachment()
    {
        return attachment;
    }

    public void setAttachment(String attachment)
    {
        this.attachment = attachment;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public Long getBelongUserId()
    {
        return belongUserId;
    }

    public void setBelongUserId(Long belongUserId)
    {
        this.belongUserId = belongUserId;
    }

    public Long getBelongDeptId()
    {
        return belongDeptId;
    }

    public void setBelongDeptId(Long belongDeptId)
    {
        this.belongDeptId = belongDeptId;
    }

    public String getCustomerName()
    {
        return customerName;
    }

    public void setCustomerName(String customerName)
    {
        this.customerName = customerName;
    }

    public Integer getApprovalCount()
    {
        return approvalCount;
    }

    public void setApprovalCount(Integer approvalCount)
    {
        this.approvalCount = approvalCount;
    }

    public Date getLastApprovalTime()
    {
        return lastApprovalTime;
    }

    public void setLastApprovalTime(Date lastApprovalTime)
    {
        this.lastApprovalTime = lastApprovalTime;
    }

    public List<CrmContractDetail> getDetails()
    {
        return details;
    }

    public void setDetails(List<CrmContractDetail> details)
    {
        this.details = details;
    }

    public Boolean getHasLockedOrder()
    {
        return hasLockedOrder;
    }

    public void setHasLockedOrder(Boolean hasLockedOrder)
    {
        this.hasLockedOrder = hasLockedOrder;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("contractId", getContractId())
            .append("contractNo", getContractNo())
            .append("contractName", getContractName())
            .append("customerId", getCustomerId())
            .append("orderId", getOrderId())
            .append("amount", getAmount())
            .append("signDate", getSignDate())
            .append("startDate", getStartDate())
            .append("endDate", getEndDate())
            .append("status", getStatus())
            .append("attachment", getAttachment())
            .append("delFlag", getDelFlag())
            .append("belongUserId", getBelongUserId())
            .append("belongDeptId", getBelongDeptId())
            .append("approvalCount", getApprovalCount())
            .append("lastApprovalTime", getLastApprovalTime())
            .append("details", getDetails())
            .append("hasLockedOrder", getHasLockedOrder())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
