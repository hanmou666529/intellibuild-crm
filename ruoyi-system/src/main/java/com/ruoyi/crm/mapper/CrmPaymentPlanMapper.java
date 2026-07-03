package com.ruoyi.crm.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.crm.domain.CrmPaymentPlan;

public interface CrmPaymentPlanMapper
{
    public List<CrmPaymentPlan> selectCrmPaymentPlanList(CrmPaymentPlan plan);

    public CrmPaymentPlan selectCrmPaymentPlanById(Long planId);

    public int insertCrmPaymentPlan(CrmPaymentPlan plan);

    public int updateCrmPaymentPlan(CrmPaymentPlan plan);

    public int deleteCrmPaymentPlanByIds(Long[] planIds);

    public int markAsPaid(CrmPaymentPlan plan);

    public int countUnpaidByOrderId(Long orderId);

    public void updatePaymentPlanBelongByCustomerId(@Param("customerId") Long customerId, @Param("belongUserId") Long belongUserId, @Param("belongDeptId") Long belongDeptId);
}
