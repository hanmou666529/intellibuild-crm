package com.ruoyi.crm.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class CrmFollowup extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long followupId;

    private Long customerId;

    @Excel(name = "联系时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date contactTime;

    @Excel(name = "跟进方式", dictType = "crm_followup_mode")
    private String followupMode;

    @Excel(name = "跟进内容")
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date nextContactTime;

    @Excel(name = "是否有效", readConverterExp = "0=有效,1=无效")
    private String isEffective;

    private String delFlag;

    @Excel(name = "客户名称")
    private String customerName;

    public Long getFollowupId()
    {
        return followupId;
    }

    public void setFollowupId(Long followupId)
    {
        this.followupId = followupId;
    }

    public Long getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(Long customerId)
    {
        this.customerId = customerId;
    }

    public Date getContactTime()
    {
        return contactTime;
    }

    public void setContactTime(Date contactTime)
    {
        this.contactTime = contactTime;
    }

    public String getFollowupMode()
    {
        return followupMode;
    }

    public void setFollowupMode(String followupMode)
    {
        this.followupMode = followupMode;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public Date getNextContactTime()
    {
        return nextContactTime;
    }

    public void setNextContactTime(Date nextContactTime)
    {
        this.nextContactTime = nextContactTime;
    }

    public String getIsEffective()
    {
        return isEffective;
    }

    public void setIsEffective(String isEffective)
    {
        this.isEffective = isEffective;
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

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("followupId", getFollowupId())
            .append("customerId", getCustomerId())
            .append("contactTime", getContactTime())
            .append("followupMode", getFollowupMode())
            .append("content", getContent())
            .append("nextContactTime", getNextContactTime())
            .append("isEffective", getIsEffective())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
