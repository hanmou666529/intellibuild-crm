package com.ruoyi.crm.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.crm.domain.CrmCustomer;
import com.ruoyi.crm.domain.CrmFollowup;
import com.ruoyi.crm.mapper.CrmCustomerMapper;
import com.ruoyi.crm.mapper.CrmFollowupMapper;
import com.ruoyi.crm.service.ICrmFollowupService;

@Service
public class CrmFollowupServiceImpl implements ICrmFollowupService
{
    @Autowired
    private CrmFollowupMapper crmFollowupMapper;

    @Autowired
    private CrmCustomerMapper crmCustomerMapper;

    @Override
    public List<CrmFollowup> selectCrmFollowupList(CrmFollowup followup)
    {
        return crmFollowupMapper.selectCrmFollowupList(followup);
    }

    @Override
    public CrmFollowup selectCrmFollowupById(Long followupId)
    {
        return crmFollowupMapper.selectCrmFollowupById(followupId);
    }

    @Override
    public List<CrmFollowup> selectCrmFollowupByCustomerId(Long customerId)
    {
        return crmFollowupMapper.selectCrmFollowupByCustomerId(customerId);
    }

    @Override
    @Transactional
    public int insertCrmFollowup(CrmFollowup followup)
    {
        followup.setCreateBy(SecurityUtils.getUsername());
        followup.setCreateTime(DateUtils.getNowDate());
        int result = crmFollowupMapper.insertCrmFollowup(followup);

        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(followup.getCustomerId());
        customer.setLastFollowTime(followup.getContactTime());
        customer.setFollowStatus("following");
        customer.setNextContactTime(followup.getNextContactTime());
        customer.setUpdateBy(SecurityUtils.getUsername());
        crmCustomerMapper.updateCrmCustomer(customer);

        return result;
    }

    @Override
    @Transactional
    public int updateCrmFollowup(CrmFollowup followup)
    {
        followup.setUpdateBy(SecurityUtils.getUsername());
        followup.setUpdateTime(DateUtils.getNowDate());
        return crmFollowupMapper.updateCrmFollowup(followup);
    }

    @Override
    @Transactional
    public int deleteCrmFollowupByIds(Long[] followupIds)
    {
        return crmFollowupMapper.deleteCrmFollowupByIds(followupIds);
    }
}
