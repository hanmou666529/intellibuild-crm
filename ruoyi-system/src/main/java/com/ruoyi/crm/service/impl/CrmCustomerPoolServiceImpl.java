package com.ruoyi.crm.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.crm.domain.CrmCustomerPool;
import com.ruoyi.crm.mapper.CrmCustomerPoolMapper;
import com.ruoyi.crm.service.ICrmCustomerPoolService;

@Service
public class CrmCustomerPoolServiceImpl implements ICrmCustomerPoolService
{
    @Autowired
    private CrmCustomerPoolMapper crmCustomerPoolMapper;

    @Override
    public List<CrmCustomerPool> selectCrmCustomerPoolList(CrmCustomerPool poolLog)
    {
        return crmCustomerPoolMapper.selectCrmCustomerPoolList(poolLog);
    }

    @Override
    @Transactional
    public int insertCrmCustomerPool(CrmCustomerPool poolLog)
    {
        poolLog.setCreateBy(SecurityUtils.getUsername());
        poolLog.setCreateTime(DateUtils.getNowDate());
        return crmCustomerPoolMapper.insertCrmCustomerPool(poolLog);
    }
}
