package com.ruoyi.crm.domain;

import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class CrmOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long orderId;

    @Excel(name = "订单编号")
    private String orderNo;

    private Long customerId;

    private Long contractId;

    @Excel(name = "订单总金额")
    private Double totalAmount;

    @Excel(name = "折扣金额")
    private Double discountAmount;

    @Excel(name = "实付金额")
    private Double actualAmount;

    @Excel(name = "订单状态", dictType = "crm_order_status")
    private String status;

    private Double paidAmount;

    private Integer paymentCount;

    private Date lastPaymentTime;

    private String delFlag;

    private Long belongUserId;

    private Long belongDeptId;

    @Excel(name = "客户名称")
    private String customerName;

    @Excel(name = "订单来源", dictType = "crm_order_source")
    private String source;

    private String contractName;

    private String contractNo;

    private List<CrmOrderItem> itemList;

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    public Long getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(Long customerId)
    {
        this.customerId = customerId;
    }

    public Long getContractId()
    {
        return contractId;
    }

    public void setContractId(Long contractId)
    {
        this.contractId = contractId;
    }

    public Double getTotalAmount()
    {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount)
    {
        this.totalAmount = totalAmount;
    }

    public Double getDiscountAmount()
    {
        return discountAmount;
    }

    public void setDiscountAmount(Double discountAmount)
    {
        this.discountAmount = discountAmount;
    }

    public Double getActualAmount()
    {
        return actualAmount;
    }

    public void setActualAmount(Double actualAmount)
    {
        this.actualAmount = actualAmount;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Double getPaidAmount()
    {
        return paidAmount;
    }

    public void setPaidAmount(Double paidAmount)
    {
        this.paidAmount = paidAmount;
    }

    public Integer getPaymentCount()
    {
        return paymentCount;
    }

    public void setPaymentCount(Integer paymentCount)
    {
        this.paymentCount = paymentCount;
    }

    public Date getLastPaymentTime()
    {
        return lastPaymentTime;
    }

    public void setLastPaymentTime(Date lastPaymentTime)
    {
        this.lastPaymentTime = lastPaymentTime;
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

    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public String getContractName()
    {
        return contractName;
    }

    public void setContractName(String contractName)
    {
        this.contractName = contractName;
    }

    public String getContractNo()
    {
        return contractNo;
    }

    public void setContractNo(String contractNo)
    {
        this.contractNo = contractNo;
    }

    public List<CrmOrderItem> getItemList()
    {
        return itemList;
    }

    public void setItemList(List<CrmOrderItem> itemList)
    {
        this.itemList = itemList;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("orderId", getOrderId())
            .append("orderNo", getOrderNo())
            .append("customerId", getCustomerId())
            .append("contractId", getContractId())
            .append("totalAmount", getTotalAmount())
            .append("discountAmount", getDiscountAmount())
            .append("actualAmount", getActualAmount())
            .append("status", getStatus())
            .append("paidAmount", getPaidAmount())
            .append("paymentCount", getPaymentCount())
            .append("lastPaymentTime", getLastPaymentTime())
            .append("source", getSource())
            .append("contractName", getContractName())
            .append("contractNo", getContractNo())
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
