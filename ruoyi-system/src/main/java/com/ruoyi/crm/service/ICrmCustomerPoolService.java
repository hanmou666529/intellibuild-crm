package com.ruoyi.crm.service;

import java.util.List;
import com.ruoyi.crm.domain.CrmCustomerPool;

public interface ICrmCustomerPoolService
{
    public List<CrmCustomerPool> selectCrmCustomerPoolList(CrmCustomerPool poolLog);

    public int insertCrmCustomerPool(CrmCustomerPool poolLog);
}
