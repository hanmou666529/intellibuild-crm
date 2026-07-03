package com.ruoyi.crm.mapper;

import java.util.List;
import com.ruoyi.crm.domain.CrmCustomerPool;

public interface CrmCustomerPoolMapper
{
    public List<CrmCustomerPool> selectCrmCustomerPoolList(CrmCustomerPool poolLog);

    public int insertCrmCustomerPool(CrmCustomerPool poolLog);
}
