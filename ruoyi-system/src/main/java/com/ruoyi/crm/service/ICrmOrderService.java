package com.ruoyi.crm.service;

import java.util.List;
import com.ruoyi.crm.domain.CrmOrder;

public interface ICrmOrderService
{
    public List<CrmOrder> selectCrmOrderList(CrmOrder order);

    public CrmOrder selectCrmOrderById(Long orderId);

    public int insertCrmOrder(CrmOrder order);

    public int updateCrmOrder(CrmOrder order);

    public int deleteCrmOrderByIds(Long[] orderIds);

    public int markAsPaid(CrmOrder order);

    public void completeOrderByContract(Long contractId);
}
