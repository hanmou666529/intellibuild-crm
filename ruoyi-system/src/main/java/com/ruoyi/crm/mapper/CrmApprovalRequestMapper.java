package com.ruoyi.crm.mapper;

import java.util.List;
import com.ruoyi.crm.domain.CrmApprovalRequest;

public interface CrmApprovalRequestMapper
{
    public List<CrmApprovalRequest> selectCrmApprovalRequestList(CrmApprovalRequest request);
    public CrmApprovalRequest selectCrmApprovalRequestById(Long requestId);
    public List<CrmApprovalRequest> selectProcessedRequests();
    public List<CrmApprovalRequest> selectPendingByApproverId(Long approverUserId);
    public int insertCrmApprovalRequest(CrmApprovalRequest request);
    public int updateCrmApprovalRequest(CrmApprovalRequest request);
}
