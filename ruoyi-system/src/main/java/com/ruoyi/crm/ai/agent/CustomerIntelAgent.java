package com.ruoyi.crm.ai.agent;

import com.ruoyi.crm.domain.CrmCustomer;
import com.ruoyi.crm.mapper.CrmCustomerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import java.util.*;

@Component
public class CustomerIntelAgent extends AbstractAiAgent {
    @Autowired private CrmCustomerMapper customerMapper;

    @Override
    protected String getScene() { return "customer_intel"; }

    @SuppressWarnings("unchecked")
    public Map<String, Object> complete(Long customerId, Map<String, Object> knownFields) {
        CrmCustomer customer = customerMapper.selectCrmCustomerById(customerId);
        if (customer == null) throw new RuntimeException("客户不存在");

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("name", customer.getCustomerName());
        ctx.put("knownFields", knownFields);

        String json = "{\"name\":\"" + customer.getCustomerName() + "\",\"knownFields\":" + JSON.toJSONString(knownFields) + "}";
        String result = callAi(json, null);
        try {
            return JSON.parseObject(result);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "AI 返回格式异常");
            return err;
        }
    }
}
