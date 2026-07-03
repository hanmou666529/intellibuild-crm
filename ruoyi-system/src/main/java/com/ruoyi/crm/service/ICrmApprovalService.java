package com.ruoyi.crm.service;

import java.util.List;
import com.ruoyi.crm.domain.CrmApprovalTemplate;
import com.ruoyi.crm.domain.CrmApprovalRequest;
import com.ruoyi.crm.domain.CrmApprovalNode;

public interface ICrmApprovalService
{
    public List<CrmApprovalTemplate> selectCrmApprovalTemplateList(CrmApprovalTemplate template);
    public CrmApprovalTemplate selectCrmApprovalTemplateById(Long templateId);
    public int insertCrmApprovalTemplate(CrmApprovalTemplate template);
    public int updateCrmApprovalTemplate(CrmApprovalTemplate template);
    public int deleteCrmApprovalTemplateByIds(Long[] templateIds);

    public List<CrmApprovalRequest> selectCrmApprovalRequestList(CrmApprovalRequest request);
    public CrmApprovalRequest selectCrmApprovalRequestById(Long requestId);
    public List<CrmApprovalRequest> selectPendingApprovals(Long userId);
    public List<CrmApprovalRequest> selectMyRequests(String submitBy);
    public List<CrmApprovalRequest> selectProcessedRequests();

    public CrmApprovalRequest submitForApproval(String bizType, Long bizId, Double amount, String bizInfo);
    public void approveNode(Long nodeId, String comment);
    public void rejectNode(Long nodeId, String comment);
    public void cancelRequest(Long requestId);

    public List<CrmApprovalNode> selectNodesByRequestId(Long requestId);
}
