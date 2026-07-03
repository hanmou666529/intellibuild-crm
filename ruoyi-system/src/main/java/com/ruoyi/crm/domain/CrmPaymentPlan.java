package com.ruoyi.crm.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class CrmPaymentPlan extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long planId;

    private Long orderId;

    private Long contractId;

    @Excel(name = "计划金额")
    private Double planAmount;

    @Excel(name = "实付金额")
    private Double actualAmount;

    @Excel(name = "计划日期", dateFormat = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date planDate;

    @Excel(name = "实际日期", dateFormat = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date actualDate;

    @Excel(name = "状态", dictType = "crm_payment_status")
    private String status;

    @Excel(name = "付款方式", dictType = "crm_payment_method")
    private String paymentMethod;

    private Long belongUserId;

    private Long belongDeptId;

    private String delFlag;

    private String orderNo;

    private String customerName;

    public Long getPlanId()
    {
        return planId;
    }

    public void setPlanId(Long planId)
    {
        this.planId = planId;
    }

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public Long getContractId()
    {
        return contractId;
    }

    public void setContractId(Long contractId)
    {
        this.contractId = contractId;
    }

    public Double getPlanAmount()
    {
        return planAmount;
    }

    public void setPlanAmount(Double planAmount)
    {
        this.planAmount = planAmount;
    }

    public Double getActualAmount()
    {
        return actualAmount;
    }

    public void setActualAmount(Double actualAmount)
    {
        this.actualAmount = actualAmount;
    }

    public Date getPlanDate()
    {
        return planDate;
    }

    public void setPlanDate(Date planDate)
    {
        this.planDate = planDate;
    }

    public Date getActualDate()
    {
        return actualDate;
    }

    public void setActualDate(Date actualDate)
    {
        this.actualDate = actualDate;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getPaymentMethod()
    {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod)
    {
        this.paymentMethod = paymentMethod;
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

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    public String getCustomerName()
    {
        return customerName;
    }

    public void setCustomerName(String customerName)
    {
        this.customerName = customerName;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("planId", getPlanId())
            .append("orderId", getOrderId())
            .append("contractId", getContractId())
            .append("planAmount", getPlanAmount())
            .append("actualAmount", getActualAmount())
            .append("planDate", getPlanDate())
            .append("actualDate", getActualDate())
            .append("status", getStatus())
            .append("paymentMethod", getPaymentMethod())
            .append("delFlag", getDelFlag())
            .append("belongUserId", getBelongUserId())
            .append("belongDeptId", getBelongDeptId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
