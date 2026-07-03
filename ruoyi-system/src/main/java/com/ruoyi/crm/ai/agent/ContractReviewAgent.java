package com.ruoyi.crm.ai.agent;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.crm.domain.CrmContract;
import com.ruoyi.crm.domain.CrmContractDetail;
import com.ruoyi.crm.mapper.CrmContractDetailMapper;
import com.ruoyi.crm.mapper.CrmContractMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class ContractReviewAgent extends AbstractAiAgent {
    @Autowired private CrmContractMapper contractMapper;
    @Autowired private CrmContractDetailMapper contractDetailMapper;

    @Override
    protected String getScene() { return "contract_review"; }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> review(Long contractId) {
        CrmContract contract = contractMapper.selectCrmContractById(contractId);
        if (contract == null) throw new RuntimeException("合同不存在");

        List<CrmContractDetail> details = contractDetailMapper.selectCrmContractDetailByContractId(contractId);

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("contractNo", contract.getContractNo());
        ctx.put("contractName", contract.getContractName());
        ctx.put("amount", contract.getAmount());
        ctx.put("startDate", String.valueOf(contract.getStartDate()));
        ctx.put("endDate", String.valueOf(contract.getEndDate()));
        ctx.put("items", details);

        String json = JSON.toJSONString(ctx);
        String result = callAi(json, null);

        try {
            return JSON.parseObject(result, List.class);
        } catch (Exception e) {
            List<Map<String, Object>> fallback = new ArrayList<>();
            Map<String, Object> err = new HashMap<>();
            err.put("level", "低");
            err.put("risk", "AI 审查服务暂时不可用");
            err.put("suggestion", "建议人工审查合同条款");
            fallback.add(err);
            return fallback;
        }
    }
}
