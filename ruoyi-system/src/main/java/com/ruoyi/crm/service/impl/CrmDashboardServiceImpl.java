package com.ruoyi.crm.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.crm.domain.CrmContract;
import com.ruoyi.crm.domain.CrmCustomer;
import com.ruoyi.crm.domain.CrmOrder;
import com.ruoyi.crm.mapper.CrmContractMapper;
import com.ruoyi.crm.mapper.CrmCustomerMapper;
import com.ruoyi.crm.mapper.CrmFollowupMapper;
import com.ruoyi.crm.mapper.CrmOrderItemMapper;
import com.ruoyi.crm.mapper.CrmOrderMapper;
import com.ruoyi.crm.mapper.CrmPipelineMapper;
import com.ruoyi.crm.service.ICrmDashboardService;

@Service
public class CrmDashboardServiceImpl implements ICrmDashboardService
{
    @Autowired
    private CrmCustomerMapper crmCustomerMapper;

    @Autowired
    private CrmFollowupMapper crmFollowupMapper;

    @Autowired
    private CrmOrderMapper crmOrderMapper;

    @Autowired
    private CrmPipelineMapper crmPipelineMapper;

    @Autowired
    private CrmContractMapper crmContractMapper;

    @Autowired
    private CrmOrderItemMapper crmOrderItemMapper;

    private void copyDataScope(Map<String, Object> target, CrmCustomer source)
    {
        if (source != null && source.getParams().get("dataScope") != null)
        {
            target.put("dataScope", source.getParams().get("dataScope"));
        }
    }

    @Override
    public Map<String, Object> getStats(CrmCustomer customer)
    {
        Map<String, Object> stats = new HashMap<>();

        String today = DateUtils.getDate();
        stats.put("totalCustomer", crmCustomerMapper.countTotal(customer));
        if (customer != null) customer.getParams().put("today", today);
        stats.put("todayNewCustomer", crmCustomerMapper.countTodayNew(customer));
        stats.put("pendingFollowup", crmCustomerMapper.countPendingFollowup(customer));

        String month = today.substring(0, 7);
        if (customer != null) customer.getParams().put("month", month);
        Double monthDeal = crmOrderMapper.sumMonthDealAmount(customer);
        stats.put("monthDealAmount", monthDeal != null ? monthDeal : 0D);

        // Source distribution
        List<Map<String, Object>> sourceData = new ArrayList<>();
        Map<String, String> sourceLabel = new HashMap<>();
        sourceLabel.put("phone", "电话咨询");
        sourceLabel.put("online", "在线咨询");
        sourceLabel.put("marketing", "市场活动");
        sourceLabel.put("referral", "转介绍");
        sourceLabel.put("ad", "广告投放");
        sourceLabel.put("partner", "合作伙伴");
        sourceLabel.put("other", "其他");
        for (Map<String, Object> row : crmCustomerMapper.countGroupBySource(customer))
        {
            Map<String, Object> item = new HashMap<>();
            item.put("_raw", row.get("name"));
            item.put("name", sourceLabel.getOrDefault((String) row.get("name"), (String) row.get("name")));
            item.put("value", row.get("value"));
            sourceData.add(item);
        }
        stats.put("sourceDistribution", sourceData);

        // Level distribution (kept for reference but no longer shown as dedicated chart)
        List<Map<String, Object>> levelData = new ArrayList<>();
        Map<String, String> levelLabel = new HashMap<>();
        levelLabel.put("high", "高潜力");
        levelLabel.put("medium", "中潜力");
        levelLabel.put("low", "低潜力");
        levelLabel.put("won", "已成交");
        for (Map<String, Object> row : crmCustomerMapper.countGroupByLevel(customer))
        {
            Map<String, Object> item = new HashMap<>();
            item.put("name", levelLabel.getOrDefault((String) row.get("name"), (String) row.get("name")));
            item.put("value", row.get("value"));
            levelData.add(item);
        }
        stats.put("levelDistribution", levelData);

        // Pipeline funnel
        List<Map<String, Object>> funnelData = new ArrayList<>();
        Map<String, String> stageNames = new HashMap<>();
        stageNames.put("clue", "线索");
        stageNames.put("intent", "意向");
        stageNames.put("quote", "报价");
        stageNames.put("deal", "成交");
        stageNames.put("payment", "回款");
        String[] stages = {"clue", "intent", "quote", "deal", "payment"};
        Map<String, Object> stageMap = new HashMap<>();
        for (Map<String, Object> row : crmPipelineMapper.countGroupByStage())
        {
            stageMap.put((String) row.get("name"), row.get("value"));
        }
        for (String stage : stages)
        {
            Map<String, Object> item = new HashMap<>();
            item.put("name", stageNames.get(stage));
            item.put("value", stageMap.getOrDefault(stage, 0));
            funnelData.add(item);
        }
        stats.put("pipelineDistribution", funnelData);

        // Followup trend — 14 days
        Map<String, Object> trendData = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        String[] lastDays = new String[14];
        for (int i = 13; i >= 0; i--)
        {
            cal.setTime(Calendar.getInstance().getTime());
            cal.add(Calendar.DAY_OF_YEAR, -i);
            lastDays[i] = DateUtils.parseDateToStr("yyyy-MM-dd", cal.getTime());
        }

        Map<String, Object> trendParams = new HashMap<>();
        trendParams.put("startDate", lastDays[13] + " 00:00:00");
        trendParams.put("endDate", lastDays[0] + " 23:59:59");
        Map<String, Integer> trendMap = new HashMap<>();
        for (Map<String, Object> row : crmFollowupMapper.countFollowupTrend(trendParams))
        {
            Object count = row.get("count");
            trendMap.put((String) row.get("date"), count instanceof Number ? ((Number) count).intValue() : 0);
        }
        for (int i = 13; i >= 0; i--)
        {
            dates.add(lastDays[i].substring(5));
            counts.add(trendMap.getOrDefault(lastDays[i], 0));
        }
        trendData.put("dates", dates);
        trendData.put("counts", counts);
        stats.put("followupTrend", trendData);

        // Month new customers (monthly bar)
        List<Map<String, Object>> monthNewData = crmCustomerMapper.countMonthNewCustomers(customer);
        stats.put("monthNewCustomers", monthNewData);

        // Contract status distribution
        CrmContract contractParam = new CrmContract();
        copyDataScope(contractParam.getParams(), customer);
        List<Map<String, Object>> contractStatus = crmContractMapper.countContractByStatus(contractParam);
        Map<String, String> contractLabels = new HashMap<>();
        contractLabels.put("0", "待审批");
        contractLabels.put("1", "已通过");
        contractLabels.put("2", "已锁定");
        contractLabels.put("3", "已完成");
        List<Map<String, Object>> contractStatusData = new ArrayList<>();
        for (Map<String, Object> row : contractStatus)
        {
            Map<String, Object> item = new HashMap<>();
            item.put("_raw", row.get("name"));
            item.put("name", contractLabels.getOrDefault((String) row.get("name"), (String) row.get("name")));
            item.put("value", row.get("value"));
            contractStatusData.add(item);
        }
        stats.put("contractStatus", contractStatusData);

        // Pending approval count
        Integer pendingApproval = crmContractMapper.countPendingApproval(contractParam);
        stats.put("pendingApproval", pendingApproval != null ? pendingApproval : 0);

        // Monthly contract amount trend
        List<Map<String, Object>> monthContractAmount = crmContractMapper.sumMonthAmount(contractParam);
        stats.put("monthContractAmount", monthContractAmount);

        // Order status distribution
        CrmOrder orderParam = new CrmOrder();
        copyDataScope(orderParam.getParams(), customer);
        List<Map<String, Object>> orderStatus = crmOrderMapper.countGroupByStatus(orderParam);
        Map<String, String> orderLabels = new HashMap<>();
        orderLabels.put("0", "待付款");
        orderLabels.put("1", "已付款");
        orderLabels.put("2", "已完成");
        orderLabels.put("3", "已取消");
        orderLabels.put("4", "草稿");
        List<Map<String, Object>> orderStatusData = new ArrayList<>();
        for (Map<String, Object> row : orderStatus)
        {
            Map<String, Object> item = new HashMap<>();
            item.put("_raw", row.get("name"));
            item.put("name", orderLabels.getOrDefault((String) row.get("name"), (String) row.get("name")));
            item.put("value", row.get("value"));
            orderStatusData.add(item);
        }
        stats.put("orderStatus", orderStatusData);

        // Top 10 products
        List<Map<String, Object>> topProducts = crmOrderItemMapper.countTopProducts(10);
        stats.put("topProducts", topProducts);

        return stats;
    }

    @Override
    public List<?> drillDown(String bizType, String key, CrmCustomer customer)
    {
        if ("customer_source".equals(bizType))
        {
            CrmCustomer param = new CrmCustomer();
            copyDataScopeMap(param.getParams(), customer);
            param.setSource(key);
            return crmCustomerMapper.selectCrmCustomerList(param);
        }
        else if ("order_status".equals(bizType))
        {
            CrmOrder param = new CrmOrder();
            copyDataScopeMap(param.getParams(), customer);
            param.setStatus(key);
            return crmOrderMapper.selectCrmOrderList(param);
        }
        else if ("contract_status".equals(bizType))
        {
            CrmContract param = new CrmContract();
            copyDataScopeMap(param.getParams(), customer);
            param.setStatus(key);
            return crmContractMapper.selectCrmContractList(param);
        }
        else if ("top_product".equals(bizType))
        {
            return crmOrderItemMapper.selectByProductName(key);
        }
        return new ArrayList<>();
    }

    private void copyDataScopeMap(Map<String, Object> target, CrmCustomer source)
    {
        if (source != null && source.getParams().get("dataScope") != null)
        {
            target.put("dataScope", source.getParams().get("dataScope"));
        }
    }
}
