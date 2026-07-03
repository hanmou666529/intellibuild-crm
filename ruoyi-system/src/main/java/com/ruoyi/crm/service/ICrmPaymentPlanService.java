package com.ruoyi.crm.service;

import java.util.List;
import com.ruoyi.crm.domain.CrmPaymentPlan;

public interface ICrmPaymentPlanService
{
    public List<CrmPaymentPlan> selectCrmPaymentPlanList(CrmPaymentPlan plan);

    public CrmPaymentPlan selectCrmPaymentPlanById(Long planId);

    public int insertCrmPaymentPlan(CrmPaymentPlan plan);

    public int updateCrmPaymentPlan(CrmPaymentPlan plan);

    public int deleteCrmPaymentPlanByIds(Long[] planIds);

    public int markAsPaid(CrmPaymentPlan plan);
}
