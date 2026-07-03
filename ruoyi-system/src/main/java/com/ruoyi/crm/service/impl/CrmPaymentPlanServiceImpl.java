package com.ruoyi.crm.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import java.math.BigDecimal;
import com.ruoyi.crm.domain.CrmContract;
import com.ruoyi.crm.domain.CrmOrder;
import com.ruoyi.crm.domain.CrmPaymentPlan;
import com.ruoyi.crm.mapper.CrmContractMapper;
import com.ruoyi.crm.mapper.CrmOrderMapper;
import com.ruoyi.crm.mapper.CrmPaymentPlanMapper;
import com.ruoyi.crm.service.ICrmPaymentPlanService;

@Service
public class CrmPaymentPlanServiceImpl implements ICrmPaymentPlanService
{
    @Autowired
    private CrmPaymentPlanMapper crmPaymentPlanMapper;

    @Autowired
    private CrmOrderMapper crmOrderMapper;

    @Autowired
    private CrmContractMapper crmContractMapper;

    @Override
    public List<CrmPaymentPlan> selectCrmPaymentPlanList(CrmPaymentPlan plan)
    {
        return crmPaymentPlanMapper.selectCrmPaymentPlanList(plan);
    }

    @Override
    public CrmPaymentPlan selectCrmPaymentPlanById(Long planId)
    {
        return crmPaymentPlanMapper.selectCrmPaymentPlanById(planId);
    }

    @Override
    @Transactional
    public int insertCrmPaymentPlan(CrmPaymentPlan plan)
    {
        plan.setCreateBy(SecurityUtils.getUsername());
        plan.setCreateTime(DateUtils.getNowDate());
        inheritOrderBelong(plan);
        return crmPaymentPlanMapper.insertCrmPaymentPlan(plan);
    }

    private void inheritOrderBelong(CrmPaymentPlan plan)
    {
        if (plan.getOrderId() != null && plan.getBelongUserId() == null)
        {
            CrmOrder order = crmOrderMapper.selectCrmOrderById(plan.getOrderId());
            if (order != null)
            {
                plan.setBelongUserId(order.getBelongUserId());
                plan.setBelongDeptId(order.getBelongDeptId());
            }
        }
    }

    @Override
    @Transactional
    public int updateCrmPaymentPlan(CrmPaymentPlan plan)
    {
        plan.setUpdateBy(SecurityUtils.getUsername());
        plan.setUpdateTime(DateUtils.getNowDate());
        return crmPaymentPlanMapper.updateCrmPaymentPlan(plan);
    }

    @Override
    @Transactional
    public int deleteCrmPaymentPlanByIds(Long[] planIds)
    {
        return crmPaymentPlanMapper.deleteCrmPaymentPlanByIds(planIds);
    }

    @Override
    @Transactional
    public int markAsPaid(CrmPaymentPlan plan)
    {
        plan.setUpdateBy(SecurityUtils.getUsername());
        int result = crmPaymentPlanMapper.markAsPaid(plan);

        CrmPaymentPlan current = crmPaymentPlanMapper.selectCrmPaymentPlanById(plan.getPlanId());
        if (current != null && current.getOrderId() != null)
        {
            crmOrderMapper.updateOrderPaymentTrack(current.getOrderId(),
                    plan.getActualDate() != null ? plan.getActualDate() : DateUtils.getNowDate());

            CrmOrder order = crmOrderMapper.selectCrmOrderById(current.getOrderId());
            if (order != null && order.getContractId() != null)
            {
                int unpaid = crmContractMapper.countUnpaidByContractId(order.getContractId());
                if (unpaid == 0)
                {
                    CrmContract contract = crmContractMapper.selectCrmContractById(order.getContractId());
                    if (contract != null && !"2".equals(contract.getStatus()) && !"3".equals(contract.getStatus()))
                    {
                        contract.setStatus("2");
                        contract.setUpdateBy(SecurityUtils.getUsername());
                        crmContractMapper.updateCrmContract(contract);
                    }
                }
            }
        }

        return result;
    }
}
