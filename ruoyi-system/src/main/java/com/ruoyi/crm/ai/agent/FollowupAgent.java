package com.ruoyi.crm.ai.agent;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.crm.domain.CrmCustomer;
import com.ruoyi.crm.domain.CrmFollowup;
import com.ruoyi.crm.mapper.CrmCustomerMapper;
import com.ruoyi.crm.mapper.CrmFollowupMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class FollowupAgent extends AbstractAiAgent {
    @Autowired private CrmCustomerMapper customerMapper;
    @Autowired private CrmFollowupMapper followupMapper;

    @Override
    protected String getScene() { return "followup_generate"; }

    public Map<String, Object> generate(Long customerId, String keywords) {
        CrmCustomer customer = customerMapper.selectCrmCustomerById(customerId);
        if (customer == null) throw new RuntimeException("客户不存在");

        CrmFollowup param = new CrmFollowup();
        param.setCustomerId(customerId);
        List<CrmFollowup> history = followupMapper.selectCrmFollowupList(param);
        List<Map<String, Object>> recentHistory = new ArrayList<>();
        for (int i = 0; i < Math.min(history.size(), 3); i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("content", history.get(i).getContent());
            item.put("time", String.valueOf(history.get(i).getCreateTime()));
            recentHistory.add(item);
        }

        String json = "{\"customerName\":\"" + customer.getCustomerName()
            + "\",\"history\":" + JSON.toJSONString(recentHistory)
            + ",\"keywords\":\"" + keywords + "\"}";
        String result = callAi(json, null);

        Map<String, Object> res = new HashMap<>();
        res.put("content", result);
        return res;
    }
}
