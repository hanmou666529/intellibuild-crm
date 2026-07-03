package com.ruoyi.crm.mapper;

import java.util.List;
import com.ruoyi.crm.domain.CrmApprovalTemplate;

public interface CrmApprovalTemplateMapper
{
    public List<CrmApprovalTemplate> selectCrmApprovalTemplateList(CrmApprovalTemplate template);
    public CrmApprovalTemplate selectCrmApprovalTemplateById(Long templateId);
    public CrmApprovalTemplate selectTemplateByBizType(String bizType);
    public int insertCrmApprovalTemplate(CrmApprovalTemplate template);
    public int updateCrmApprovalTemplate(CrmApprovalTemplate template);
    public int deleteCrmApprovalTemplateByIds(Long[] templateIds);
}
