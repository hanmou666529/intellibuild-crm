package com.ruoyi.crm.ai.agent;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.crm.domain.CrmCustomer;
import com.ruoyi.crm.domain.CrmPipeline;
import com.ruoyi.crm.mapper.CrmCustomerMapper;
import com.ruoyi.crm.mapper.CrmPipelineMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class LeadScoringAgent extends AbstractAiAgent {
    @Autowired private CrmCustomerMapper customerMapper;
    @Autowired private CrmPipelineMapper pipelineMapper;

    @Override
    protected String getScene() { return "lead_scoring"; }

    @SuppressWarnings("unchecked")
    public int runScoring() {
        CrmCustomer param = new CrmCustomer();
        List<CrmCustomer> customers = customerMapper.selectCrmCustomerList(param);
        List<Map<String, Object>> customerData = new ArrayList<>();
        for (CrmCustomer c : customers) {
            Map<String, Object> item = new HashMap<>();
            item.put("customerId", c.getCustomerId());
            item.put("name", c.getCustomerName());
            item.put("level", c.getLevel());
            item.put("source", c.getSource());
            item.put("followStatus", c.getFollowStatus());
            customerData.add(item);
        }

        String json = JSON.toJSONString(customerData);
        String result = callAi(json, null);

        List<Map<String, Object>> scores;
        try {
            scores = JSON.parseObject(result, List.class);
        } catch (Exception e) {
            return 0;
        }

        int updated = 0;
        for (Map<String, Object> s : scores) {
            Object id = s.get("customerId");
            if (id == null) continue;
            Long customerId = Long.valueOf(id.toString());
            Integer score = s.get("score") instanceof Number
                ? ((Number) s.get("score")).intValue() : null;
            String reason = (String) s.get("reason");
            if (score == null) continue;

            CrmPipeline pipeline = new CrmPipeline();
            pipeline.setCustomerId(customerId);
            List<CrmPipeline> existing = pipelineMapper.selectCrmPipelineList(pipeline);
            if (!existing.isEmpty()) {
                CrmPipeline p = existing.get(0);
                p.setScore(score);
                p.setScoreReason(reason);
                p.setScoreTime(new Date());
                pipelineMapper.updateCrmPipeline(p);
                updated++;
            }
        }
        return updated;
    }
}
