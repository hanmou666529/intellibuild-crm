package com.ruoyi.crm.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.crm.domain.CrmContract;
import com.ruoyi.crm.domain.CrmCustomer;
import com.ruoyi.crm.domain.CrmOrder;
import com.ruoyi.crm.domain.CrmOrderItem;
import com.ruoyi.crm.mapper.CrmContractMapper;
import com.ruoyi.crm.mapper.CrmCustomerMapper;
import com.ruoyi.crm.mapper.CrmOrderMapper;
import com.ruoyi.crm.mapper.CrmOrderItemMapper;
import com.ruoyi.crm.service.ICrmOrderService;

@Service
public class CrmOrderServiceImpl implements ICrmOrderService
{
    @Autowired
    private CrmOrderMapper crmOrderMapper;

    @Autowired
    private CrmOrderItemMapper crmOrderItemMapper;

    @Autowired
    private CrmCustomerMapper crmCustomerMapper;

    @Autowired
    private CrmContractMapper crmContractMapper;

    @Override
    public List<CrmOrder> selectCrmOrderList(CrmOrder order)
    {
        return crmOrderMapper.selectCrmOrderList(order);
    }

    @Override
    public CrmOrder selectCrmOrderById(Long orderId)
    {
        CrmOrder order = crmOrderMapper.selectCrmOrderById(orderId);
        if (order != null)
        {
            List<CrmOrderItem> itemList = crmOrderItemMapper.selectCrmOrderItemByOrderId(orderId);
            order.setItemList(itemList);
            if (order.getContractId() != null)
            {
                CrmContract contract = crmContractMapper.selectCrmContractById(order.getContractId());
                if (contract != null)
                {
                    order.setContractName(contract.getContractName());
                    order.setContractNo(contract.getContractNo());
                }
            }
        }
        return order;
    }

    @Override
    @Transactional
    public int insertCrmOrder(CrmOrder order)
    {
        order.setCreateBy(SecurityUtils.getUsername());
        order.setCreateTime(DateUtils.getNowDate());
        if (order.getOrderNo() == null || "".equals(order.getOrderNo()))
        {
            order.setOrderNo("ORD" + DateUtils.dateTimeNow("yyyyMMddHHmmss")
                    + String.format("%04d", (int)(Math.random() * 10000)));
        }
        inheritCustomerBelong(order);
        calcOrderAmount(order);
        int rows = crmOrderMapper.insertCrmOrder(order);
        if (order.getItemList() != null && order.getItemList().size() > 0)
        {
            for (CrmOrderItem item : order.getItemList())
            {
                item.setOrderId(order.getOrderId());
                crmOrderItemMapper.insertCrmOrderItem(item);
            }
        }
        return rows;
    }

    @Override
    @Transactional
    public int updateCrmOrder(CrmOrder order)
    {
        order.setUpdateBy(SecurityUtils.getUsername());
        order.setUpdateTime(DateUtils.getNowDate());
        if (order.getItemList() != null)
        {
            crmOrderItemMapper.deleteCrmOrderItemByOrderId(order.getOrderId());
            for (CrmOrderItem item : order.getItemList())
            {
                item.setOrderId(order.getOrderId());
                crmOrderItemMapper.insertCrmOrderItem(item);
            }
        }
        return crmOrderMapper.updateCrmOrder(order);
    }

    private void calcOrderAmount(CrmOrder order)
    {
        if (order.getItemList() == null || order.getItemList().isEmpty())
        {
            return;
        }
        double total = 0;
        for (CrmOrderItem item : order.getItemList())
        {
            double price = item.getProductPrice() != null ? item.getProductPrice() : 0;
            int qty = item.getQuantity() != null ? item.getQuantity() : 0;
            double sub = price * qty;
            item.setSubtotal(sub);
            total += sub;
        }
        order.setTotalAmount(total);
        double discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : 0;
        order.setActualAmount(total - discount);
    }

    @Override
    @Transactional
    public int deleteCrmOrderByIds(Long[] orderIds)
    {
        for (Long orderId : orderIds)
        {
            crmOrderItemMapper.deleteCrmOrderItemByOrderId(orderId);
        }
        return crmOrderMapper.deleteCrmOrderByIds(orderIds);
    }

    @Override
    @Transactional
    public int markAsPaid(CrmOrder order)
    {
        CrmOrder current = crmOrderMapper.selectCrmOrderById(order.getOrderId());
        if (current == null || "1".equals(current.getStatus()))
        {
            return 0;
        }
        current.setStatus("1");
        current.setPaidAmount(order.getActualAmount() != null ? order.getActualAmount() : current.getActualAmount());
        current.setPaymentCount(current.getPaymentCount() != null ? current.getPaymentCount() + 1 : 1);
        current.setLastPaymentTime(order.getLastPaymentTime() != null ? order.getLastPaymentTime() : DateUtils.getNowDate());
        current.setUpdateBy(SecurityUtils.getUsername());
        crmOrderMapper.updateCrmOrder(current);

        if (current.getContractId() != null)
        {
            CrmContract contract = crmContractMapper.selectCrmContractById(current.getContractId());
            if (contract != null && !"2".equals(contract.getStatus()) && !"3".equals(contract.getStatus()))
            {
                contract.setStatus("2");
                contract.setUpdateBy(SecurityUtils.getUsername());
                crmContractMapper.updateCrmContract(contract);
            }
        }

        return 1;
    }

    @Override
    @Transactional
    public void completeOrderByContract(Long contractId)
    {
        List<CrmOrder> orders = crmOrderMapper.selectByContractId(contractId);
        for (CrmOrder order : orders)
        {
            if (!"2".equals(order.getStatus()))
            {
                order.setStatus("2");
                order.setUpdateBy(SecurityUtils.getUsername());
                crmOrderMapper.updateCrmOrder(order);
            }
        }
    }

    private void inheritCustomerBelong(CrmOrder order)
    {
        if (order.getCustomerId() != null && order.getBelongUserId() == null)
        {
            CrmCustomer customer = crmCustomerMapper.selectCrmCustomerById(order.getCustomerId());
            if (customer != null)
            {
                order.setBelongUserId(customer.getBelongUserId());
                order.setBelongDeptId(customer.getBelongDeptId());
            }
        }
    }
}
