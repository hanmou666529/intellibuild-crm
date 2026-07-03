package com.ruoyi.crm.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excels;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.annotation.Excel.Type;

public class CrmCustomer extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long customerId;

    @Excel(name = "客户名称")
    private String customerName;

    @Excel(name = "手机号")
    private String phone;

    @Excel(name = "性别", readConverterExp = "0=未知,1=男,2=女")
    private String sex;

    @Excel(name = "客户来源", dictType = "crm_customer_source")
    private String source;

    @Excel(name = "客户等级", dictType = "crm_customer_level")
    private String level;

    @Excel(name = "所属行业")
    private String industry;

    private String tags;

    private String tag;

    @Excel(name = "跟进状态", dictType = "crm_follow_status")
    private String followStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date nextContactTime;

    private Long belongUserId;

    private Long belongDeptId;

    private String isPool;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date enterPoolTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastFollowTime;

    @Excel(name = "地址")
    private String address;

    @Excel(name = "邮箱")
    private String email;

    @Excel(name = "公司")
    private String company;

    private String position;

    private String wechat;

    private String qq;

    private String delFlag;

    /** 合并到的目标客户ID */
    private Long mergeToId;

    @Excels({ @Excel(name = "归属人", targetAttr = "userName", type = Type.EXPORT) })
    private String belongUserName;

    @Excel(name = "归属部门", targetAttr = "deptName", type = Type.EXPORT)
    private String belongDeptName;

    private String shareFromDeptName;

    private String shareActionDesc;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date shareActionTime;

    private String orderNos;

    private String contractNos;

    private String[] tagList;

    public Long getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(Long customerId)
    {
        this.customerId = customerId;
    }

    public String getCustomerName()
    {
        return customerName;
    }

    public void setCustomerName(String customerName)
    {
        this.customerName = customerName;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getSex()
    {
        return sex;
    }

    public void setSex(String sex)
    {
        this.sex = sex;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public String getLevel()
    {
        return level;
    }

    public void setLevel(String level)
    {
        this.level = level;
    }

    public String getIndustry()
    {
        return industry;
    }

    public void setIndustry(String industry)
    {
        this.industry = industry;
    }

    public String getTags()
    {
        return tags;
    }

    public void setTags(String tags)
    {
        this.tags = tags;
    }

    public String getFollowStatus()
    {
        return followStatus;
    }

    public void setFollowStatus(String followStatus)
    {
        this.followStatus = followStatus;
    }

    public Date getNextContactTime()
    {
        return nextContactTime;
    }

    public void setNextContactTime(Date nextContactTime)
    {
        this.nextContactTime = nextContactTime;
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

    public String getIsPool()
    {
        return isPool;
    }

    public void setIsPool(String isPool)
    {
        this.isPool = isPool;
    }

    public Date getEnterPoolTime()
    {
        return enterPoolTime;
    }

    public void setEnterPoolTime(Date enterPoolTime)
    {
        this.enterPoolTime = enterPoolTime;
    }

    public Date getLastFollowTime()
    {
        return lastFollowTime;
    }

    public void setLastFollowTime(Date lastFollowTime)
    {
        this.lastFollowTime = lastFollowTime;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getCompany()
    {
        return company;
    }

    public void setCompany(String company)
    {
        this.company = company;
    }

    public String getPosition()
    {
        return position;
    }

    public void setPosition(String position)
    {
        this.position = position;
    }

    public String getWechat()
    {
        return wechat;
    }

    public void setWechat(String wechat)
    {
        this.wechat = wechat;
    }

    public String getQq()
    {
        return qq;
    }

    public void setQq(String qq)
    {
        this.qq = qq;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public Long getMergeToId()
    {
        return mergeToId;
    }

    public void setMergeToId(Long mergeToId)
    {
        this.mergeToId = mergeToId;
    }

    public String getBelongUserName()
    {
        return belongUserName;
    }

    public void setBelongUserName(String belongUserName)
    {
        this.belongUserName = belongUserName;
    }

    public String getBelongDeptName()
    {
        return belongDeptName;
    }

    public void setBelongDeptName(String belongDeptName)
    {
        this.belongDeptName = belongDeptName;
    }

    public String getShareFromDeptName()
    {
        return shareFromDeptName;
    }

    public void setShareFromDeptName(String shareFromDeptName)
    {
        this.shareFromDeptName = shareFromDeptName;
    }

    public String getShareActionDesc()
    {
        return shareActionDesc;
    }

    public void setShareActionDesc(String shareActionDesc)
    {
        this.shareActionDesc = shareActionDesc;
    }

    public Date getShareActionTime()
    {
        return shareActionTime;
    }

    public void setShareActionTime(Date shareActionTime)
    {
        this.shareActionTime = shareActionTime;
    }

    public String getOrderNos()
    {
        return orderNos;
    }

    public void setOrderNos(String orderNos)
    {
        this.orderNos = orderNos;
    }

    public String getContractNos()
    {
        return contractNos;
    }

    public void setContractNos(String contractNos)
    {
        this.contractNos = contractNos;
    }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public String[] getTagList()
    {
        return tagList;
    }

    public void setTagList(String[] tagList)
    {
        this.tagList = tagList;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("customerId", getCustomerId())
            .append("customerName", getCustomerName())
            .append("phone", getPhone())
            .append("sex", getSex())
            .append("source", getSource())
            .append("level", getLevel())
            .append("industry", getIndustry())
            .append("tags", getTags())
            .append("followStatus", getFollowStatus())
            .append("nextContactTime", getNextContactTime())
            .append("belongUserId", getBelongUserId())
            .append("belongDeptId", getBelongDeptId())
            .append("isPool", getIsPool())
            .append("enterPoolTime", getEnterPoolTime())
            .append("lastFollowTime", getLastFollowTime())
            .append("address", getAddress())
            .append("email", getEmail())
            .append("company", getCompany())
            .append("position", getPosition())
            .append("wechat", getWechat())
            .append("qq", getQq())
            .append("delFlag", getDelFlag())
            .append("mergeToId", getMergeToId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("shareFromDeptName", getShareFromDeptName())
            .append("orderNos", getOrderNos())
            .append("contractNos", getContractNos())
            .toString();
    }
}
