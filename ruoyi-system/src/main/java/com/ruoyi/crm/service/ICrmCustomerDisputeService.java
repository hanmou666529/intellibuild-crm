package com.ruoyi.crm.service;

import java.util.List;
import com.ruoyi.crm.domain.CrmCustomerDispute;

public interface ICrmCustomerDisputeService
{
    List<CrmCustomerDispute> selectList(CrmCustomerDispute dispute);

    CrmCustomerDispute selectById(Long disputeId);

    int insert(CrmCustomerDispute dispute);

    int handle(Long disputeId, String action, Long targetUserId, String remark);

    int arbitrate(Long disputeId, String result, String remark);
}
