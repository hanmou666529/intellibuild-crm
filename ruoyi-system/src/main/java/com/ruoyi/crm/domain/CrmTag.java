package com.ruoyi.crm.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class CrmTag extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long tagId;

    @Excel(name = "标签名称")
    private String tagName;

    @Excel(name = "标签类型", dictType = "crm_tag_type")
    private String tagType;

    @Excel(name = "标签颜色")
    private String color;

    @Excel(name = "排序")
    private Integer sortOrder;

    private String status;

    private String delFlag;

    public Long getTagId() { return tagId; }
    public void setTagId(Long tagId) { this.tagId = tagId; }
    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }
    public String getTagType() { return tagType; }
    public void setTagType(String tagType) { this.tagType = tagType; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("tagId", getTagId()).append("tagName", getTagName())
            .append("tagType", getTagType()).append("color", getColor())
            .append("sortOrder", getSortOrder()).append("status", getStatus())
            .append("createBy", getCreateBy()).append("createTime", getCreateTime()).toString();
    }
}
