package com.ruoyi.crm.mapper;

import java.util.List;
import com.ruoyi.crm.domain.CrmApprovalNode;

public interface CrmApprovalNodeMapper
{
    public List<CrmApprovalNode> selectNodesByRequestId(Long requestId);
    public CrmApprovalNode selectCrmApprovalNodeById(Long nodeId);
    public int insertCrmApprovalNode(CrmApprovalNode node);
    public int insertBatch(List<CrmApprovalNode> nodes);
    public int updateCrmApprovalNode(CrmApprovalNode node);
}
