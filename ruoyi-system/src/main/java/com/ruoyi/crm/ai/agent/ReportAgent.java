package com.ruoyi.crm.ai.agent;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.crm.domain.CrmCustomer;
import com.ruoyi.crm.domain.CrmFollowup;
import com.ruoyi.crm.domain.CrmOrder;
import com.ruoyi.crm.mapper.CrmCustomerMapper;
import com.ruoyi.crm.mapper.CrmFollowupMapper;
import com.ruoyi.crm.mapper.CrmOrderMapper;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class ReportAgent extends AbstractAiAgent {
    @Autowired private CrmCustomerMapper customerMapper;
    @Autowired private CrmFollowupMapper followupMapper;
    @Autowired private CrmOrderMapper orderMapper;

    @Override
    protected String getScene() { return "report_daily"; }

    public Map<String, Object> generate(String type, String date) {
        String sceneKey = "report_daily";
        if ("weekly".equals(type)) sceneKey = "report_weekly";

        Long userId = SecurityUtils.getUserId();
        Map<String, Object> data = new HashMap<>();

        CrmCustomer customerParam = new CrmCustomer();
        customerParam.getParams().put("createBy", String.valueOf(userId));
        data.put("newCustomers", customerMapper.selectCrmCustomerList(customerParam).size());

        CrmFollowup followupParam = new CrmFollowup();
        followupParam.setCreateBy(SecurityUtils.getUsername());
        data.put("followups", followupMapper.selectCrmFollowupList(followupParam).size());

        CrmOrder orderParam = new CrmOrder();
        data.put("orders", orderMapper.selectCrmOrderList(orderParam).size());

        String json = JSON.toJSONString(data);
        String result = callAi(json, null);

        Map<String, Object> res = new HashMap<>();
        res.put("type", type);
        res.put("date", date);
        res.put("content", result);
        return res;
    }
}
