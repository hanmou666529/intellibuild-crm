package com.ruoyi.crm.service;

import java.util.List;
import com.ruoyi.crm.domain.CrmCustomerShare;

public interface ICrmCustomerShareService
{
    public List<CrmCustomerShare> selectCrmCustomerShareList(CrmCustomerShare share);

    public List<CrmCustomerShare> selectByCustomerId(Long customerId);

    public List<Long> getSharedCustomerIds(Long userId, Long deptId);

    public int shareCustomer(Long customerId, Long fromUserId, Long toUserId);

    public int shareCustomerToDept(Long customerId, Long fromUserId, Long toDeptId);

    public int autoShare(Long customerId, Long fromUserId);

    public int deleteShare(Long shareId);

    public List<CrmCustomerShare> getDeptPendingShares(Long deptId);

    public int assignFromDeptPool(Long customerId, Long fromUserId, Long toUserId);
}
