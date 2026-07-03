package com.ruoyi.crm.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

public class CrmPipeline extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long pipelineId;

    private Long customerId;

    private Long orderId;

    private String stage;

    private BigDecimal amount;

    private Integer probability;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expectedCloseDate;

    private String delFlag;

    private String customerName;

    private String orderNo;

    private Integer score;

    private String scoreReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date scoreTime;

    public Long getPipelineId()
    {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId)
    {
        this.pipelineId = pipelineId;
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

    public String getStage()
    {
        return stage;
    }

    public void setStage(String stage)
    {
        this.stage = stage;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public Integer getProbability()
    {
        return probability;
    }

    public void setProbability(Integer probability)
    {
        this.probability = probability;
    }

    public Date getExpectedCloseDate()
    {
        return expectedCloseDate;
    }

    public void setExpectedCloseDate(Date expectedCloseDate)
    {
        this.expectedCloseDate = expectedCloseDate;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getCustomerName()
    {
        return customerName;
    }

    public void setCustomerName(String customerName)
    {
        this.customerName = customerName;
    }

    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    public Integer getScore()
    {
        return score;
    }

    public void setScore(Integer score)
    {
        this.score = score;
    }

    public String getScoreReason()
    {
        return scoreReason;
    }

    public void setScoreReason(String scoreReason)
    {
        this.scoreReason = scoreReason;
    }

    public Date getScoreTime()
    {
        return scoreTime;
    }

    public void setScoreTime(Date scoreTime)
    {
        this.scoreTime = scoreTime;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("pipelineId", getPipelineId())
            .append("customerId", getCustomerId())
            .append("orderId", getOrderId())
            .append("stage", getStage())
            .append("amount", getAmount())
            .append("probability", getProbability())
            .append("expectedCloseDate", getExpectedCloseDate())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
