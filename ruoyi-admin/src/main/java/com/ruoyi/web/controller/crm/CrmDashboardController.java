package com.ruoyi.web.controller.crm;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.CrmDataScope;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.crm.domain.CrmCustomer;
import com.ruoyi.crm.service.ICrmDashboardService;

@RestController
@RequestMapping("/crm/dashboard")
public class CrmDashboardController extends BaseController
{
    @Autowired
    private ICrmDashboardService crmDashboardService;

    @PreAuthorize("@ss.hasPermi('crm:dashboard:list')")
    @CrmDataScope(deptAlias = "c", userAlias = "c")
    @GetMapping("/stats")
    public AjaxResult stats(CrmCustomer customer)
    {
        Map<String, Object> stats = crmDashboardService.getStats(customer);
        return success(stats);
    }

    @PreAuthorize("@ss.hasPermi('crm:dashboard:list')")
    @CrmDataScope(deptAlias = "c", userAlias = "c")
    @GetMapping("/drillDown")
    public AjaxResult drillDown(@RequestParam String bizType, @RequestParam String key, CrmCustomer customer)
    {
        List<?> list = crmDashboardService.drillDown(bizType, key, customer);
        return success(list);
    }
}
