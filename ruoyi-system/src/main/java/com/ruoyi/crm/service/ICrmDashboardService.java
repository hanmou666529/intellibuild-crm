package com.ruoyi.crm.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.crm.domain.CrmCustomer;

public interface ICrmDashboardService
{
    public Map<String, Object> getStats(CrmCustomer customer);

    public List<?> drillDown(String bizType, String key, CrmCustomer customer);
}
