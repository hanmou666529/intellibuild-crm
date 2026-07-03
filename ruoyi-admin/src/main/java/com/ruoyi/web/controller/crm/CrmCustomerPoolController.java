package com.ruoyi.web.controller.crm;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.crm.domain.CrmCustomer;
import com.ruoyi.crm.domain.CrmCustomerPool;
import com.ruoyi.crm.service.ICrmCustomerPoolService;
import com.ruoyi.crm.service.ICrmCustomerService;

@RestController
@RequestMapping("/crm/pool")
public class CrmCustomerPoolController extends BaseController
{
    @Autowired
    private ICrmCustomerPoolService crmCustomerPoolService;

    @Autowired
    private ICrmCustomerService crmCustomerService;

    @PreAuthorize("@ss.hasPermi('crm:pool:list')")
    @GetMapping("/list")
    public TableDataInfo list(CrmCustomer customer)
    {
        startPage();
        customer.setIsPool("1");
        List<CrmCustomer> list = crmCustomerService.selectCrmCustomerList(customer);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('crm:pool:list')")
    @GetMapping("/logList")
    public TableDataInfo logList(CrmCustomerPool poolLog)
    {
        startPage();
        List<CrmCustomerPool> list = crmCustomerPoolService.selectCrmCustomerPoolList(poolLog);
        return getDataTable(list);
    }
}
