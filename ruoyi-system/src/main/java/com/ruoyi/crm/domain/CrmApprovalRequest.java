package com.ruoyi.crm.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class CrmApprovalRequest extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long requestId;

    private Long templateId;

    @Excel(name = "业务类型")
    private String bizType;

    private Long bizId;

    private String bizInfo;

    @Excel(name = "金额")
    private Double amount;

    @Excel(name = "状态", dictType = "crm_approval_status")
    private String status;

    private Integer currentStep;

    private String submitBy;

    @Excel(name = "提交人")
    private String submitName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    private String delFlag;

    @Excel(name = "模板名称")
    private String templateName;

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }

    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }

    public Long getBizId() { return bizId; }
    public void setBizId(Long bizId) { this.bizId = bizId; }

    public String getBizInfo() { return bizInfo; }
    public void setBizInfo(String bizInfo) { this.bizInfo = bizInfo; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getCurrentStep() { return currentStep; }
    public void setCurrentStep(Integer currentStep) { this.currentStep = currentStep; }

    public String getSubmitBy() { return submitBy; }
    public void setSubmitBy(String submitBy) { this.submitBy = submitBy; }

    public String getSubmitName() { return submitName; }
    public void setSubmitName(String submitName) { this.submitName = submitName; }

    public Date getSubmitTime() { return submitTime; }
    public void setSubmitTime(Date submitTime) { this.submitTime = submitTime; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("requestId", getRequestId())
            .append("templateId", getTemplateId())
            .append("bizType", getBizType())
            .append("bizId", getBizId())
            .append("bizInfo", getBizInfo())
            .append("amount", getAmount())
            .append("status", getStatus())
            .append("currentStep", getCurrentStep())
            .append("submitBy", getSubmitBy())
            .append("submitName", getSubmitName())
            .append("submitTime", getSubmitTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
