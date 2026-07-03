package com.ruoyi.crm.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.crm.domain.CrmCustomerShare;
import com.ruoyi.crm.mapper.CrmCustomerShareMapper;
import com.ruoyi.crm.service.ICrmCustomerShareService;

@Service
public class CrmCustomerShareServiceImpl implements ICrmCustomerShareService
{
    @Autowired
    private CrmCustomerShareMapper crmCustomerShareMapper;

    @Override
    public List<CrmCustomerShare> selectCrmCustomerShareList(CrmCustomerShare share)
    {
        return crmCustomerShareMapper.selectCrmCustomerShareList(share);
    }

    @Override
    public List<CrmCustomerShare> selectByCustomerId(Long customerId)
    {
        return crmCustomerShareMapper.selectByCustomerId(customerId);
    }

    @Override
    public List<Long> getSharedCustomerIds(Long userId, Long deptId)
    {
        List<Long> ids = new ArrayList<>();
        if (userId != null)
        {
            List<Long> byUser = crmCustomerShareMapper.selectSharedCustomerIdsByUser(userId);
            if (byUser != null) ids.addAll(byUser);
        }
        if (deptId != null)
        {
            List<Long> byDept = crmCustomerShareMapper.selectSharedCustomerIdsByDept(deptId);
            if (byDept != null) ids.addAll(byDept);
        }
        return ids.stream().distinct().collect(Collectors.toList());
    }

    @Override
    @Transactional
    public int shareCustomer(Long customerId, Long fromUserId, Long toUserId)
    {
        CrmCustomerShare share = new CrmCustomerShare();
        share.setCustomerId(customerId);
        share.setFromUserId(fromUserId);
        share.setToUserId(toUserId);
        share.setShareType("manual");
        share.setCreateBy(SecurityUtils.getUsername());
        share.setCreateTime(DateUtils.getNowDate());
        return crmCustomerShareMapper.insertCrmCustomerShare(share);
    }

    @Override
    @Transactional
    public int shareCustomerToDept(Long customerId, Long fromUserId, Long toDeptId)
    {
        CrmCustomerShare share = new CrmCustomerShare();
        share.setCustomerId(customerId);
        share.setFromUserId(fromUserId);
        share.setToDeptId(toDeptId);
        share.setShareType("manual");
        share.setCreateBy(SecurityUtils.getUsername());
        share.setCreateTime(DateUtils.getNowDate());
        return crmCustomerShareMapper.insertCrmCustomerShare(share);
    }

    @Override
    @Transactional
    public int autoShare(Long customerId, Long fromUserId)
    {
        CrmCustomerShare share = new CrmCustomerShare();
        share.setCustomerId(customerId);
        share.setFromUserId(fromUserId);
        share.setToUserId(fromUserId);
        share.setShareType("auto");
        share.setCreateBy(SecurityUtils.getUsername());
        share.setCreateTime(DateUtils.getNowDate());
        return crmCustomerShareMapper.insertCrmCustomerShare(share);
    }

    @Override
    @Transactional
    public int deleteShare(Long shareId)
    {
        return crmCustomerShareMapper.deleteCrmCustomerShareById(shareId);
    }

    @Override
    public List<CrmCustomerShare> getDeptPendingShares(Long deptId)
    {
        return crmCustomerShareMapper.selectDeptPendingShares(deptId);
    }

    @Override
    @Transactional
    public int assignFromDeptPool(Long customerId, Long fromUserId, Long toUserId)
    {
        CrmCustomerShare share = new CrmCustomerShare();
        share.setCustomerId(customerId);
        share.setFromUserId(fromUserId);
        share.setToUserId(toUserId);
        share.setShareType("dept_assign");
        share.setCreateBy(SecurityUtils.getUsername());
        share.setCreateTime(DateUtils.getNowDate());
        return crmCustomerShareMapper.insertCrmCustomerShare(share);
    }
}
