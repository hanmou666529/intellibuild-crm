package com.ruoyi.crm.mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.crm.domain.CrmCustomer;
import com.ruoyi.crm.domain.CrmOrder;

public interface CrmOrderMapper
{
    public List<CrmOrder> selectCrmOrderList(CrmOrder order);

    public CrmOrder selectCrmOrderById(Long orderId);

    public int insertCrmOrder(CrmOrder order);

    public int updateCrmOrder(CrmOrder order);

    public int deleteCrmOrderByIds(Long[] orderIds);

    public Double sumMonthDealAmount(CrmCustomer customer);

    public List<CrmOrder> selectByContractId(Long contractId);

    public void updateOrderPaymentTrack(@Param("orderId") Long orderId, @Param("lastPaymentTime") Date lastPaymentTime);

    public void updateOrderBelongByCustomerId(@Param("customerId") Long customerId, @Param("belongUserId") Long belongUserId, @Param("belongDeptId") Long belongDeptId);

    int updateCustomerId(@Param("oldId") Long oldId, @Param("newId") Long newId);

    List<Map<String, Object>> countGroupByStatus(CrmOrder order);
}
