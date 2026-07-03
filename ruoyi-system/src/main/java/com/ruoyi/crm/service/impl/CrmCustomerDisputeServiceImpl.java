package com.ruoyi.crm.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.crm.domain.CrmCustomer;
import com.ruoyi.crm.domain.CrmCustomerDispute;
import com.ruoyi.crm.mapper.CrmCustomerDisputeMapper;
import com.ruoyi.crm.service.ICrmCustomerDisputeService;
import com.ruoyi.crm.service.ICrmCustomerService;

@Service
public class CrmCustomerDisputeServiceImpl implements ICrmCustomerDisputeService {

    @Autowired
    private CrmCustomerDisputeMapper disputeMapper;

    @Autowired
    private ICrmCustomerService customerService;

    @Override
    public List<CrmCustomerDispute> selectList(CrmCustomerDispute dispute) {
        if (!SecurityUtils.isAdmin(SecurityUtils.getUserId())) {
            dispute.setInitiatorUserId(SecurityUtils.getUserId());
        }
        return disputeMapper.selectList(dispute);
    }

    @Override
    public CrmCustomerDispute selectById(Long disputeId) {
        return disputeMapper.selectById(disputeId);
    }

    @Override
    public int insert(CrmCustomerDispute dispute) {
        dispute.setInitiatorUserId(SecurityUtils.getUserId());
        dispute.setCreateBy(SecurityUtils.getUsername());
        return disputeMapper.insert(dispute);
    }

    @Override
    @Transactional
    public int handle(Long disputeId, String action, Long targetUserId, String remark) {
        CrmCustomerDispute d = disputeMapper.selectById(disputeId);
        if (d == null || !"0".equals(d.getStatus())) {
            throw new ServiceException("争议不可处理");
        }
        d.setStatus("1");
        d.setHandlerId(SecurityUtils.getUserId());
        d.setHandleTime(new Date());
        d.setRemark(remark);

        CrmCustomer customer = customerService.selectCrmCustomerById(d.getCustomerId());
        if (customer == null) {
            throw new ServiceException("客户不存在");
        }

        if ("assign".equals(action)) {
            if (targetUserId == null) {
                throw new ServiceException("请选择目标用户");
            }
            d.setTargetUserId(targetUserId);
            customer.setBelongUserId(targetUserId);
            customerService.updateCrmCustomer(customer);
        } else if ("escalate".equals(action)) {
            d.setTargetUserId(customer.getBelongUserId());
        }

        return disputeMapper.update(d);
    }

    @Override
    @Transactional
    public int arbitrate(Long disputeId, String result, String remark) {
        CrmCustomerDispute d = disputeMapper.selectById(disputeId);
        if (d == null) {
            throw new ServiceException("争议不存在");
        }
        if (!"1".equals(d.getStatus())) {
            throw new ServiceException("请先处理争议再仲裁");
        }

        CrmCustomer customer = customerService.selectCrmCustomerById(d.getCustomerId());
        if (customer == null) {
            throw new ServiceException("客户不存在");
        }

        if ("A".equals(result)) {
            if (d.getInitiatorUserId() != null) {
                customer.setBelongUserId(d.getInitiatorUserId());
            }
        } else if ("B".equals(result)) {
            customer.setBelongUserId(d.getTargetUserId());
        }
        customerService.updateCrmCustomer(customer);

        d.setStatus("2");
        d.setResult(result);
        d.setHandlerId(SecurityUtils.getUserId());
        d.setHandleTime(new Date());
        d.setRemark(remark);
        return disputeMapper.update(d);
    }
}
