package com.ruoyi.crm.ai.web;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.crm.ai.agent.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/crm/ai")
public class AiController extends BaseController {
    @Autowired private CustomerIntelAgent customerIntelAgent;
    @Autowired private FollowupAgent followupAgent;
    @Autowired private LeadScoringAgent leadScoringAgent;
    @Autowired private ReportAgent reportAgent;
    @Autowired private ContractReviewAgent contractReviewAgent;

    @PostMapping("/customer/intel")
    public AjaxResult customerIntel(@RequestBody Map<String, Object> params) {
        Long customerId = Long.valueOf(params.get("customerId").toString());
        @SuppressWarnings("unchecked")
        Map<String, Object> knownFields = (Map<String, Object>) params.getOrDefault("knownFields", new java.util.HashMap<>());
        return AjaxResult.success(customerIntelAgent.complete(customerId, knownFields));
    }

    @PostMapping("/followup/generate")
    public AjaxResult followupGenerate(@RequestBody Map<String, Object> params) {
        Long customerId = Long.valueOf(params.get("customerId").toString());
        String keywords = (String) params.getOrDefault("keywords", "");
        return AjaxResult.success(followupAgent.generate(customerId, keywords));
    }

    @PostMapping("/scoring/run")
    public AjaxResult runScoring() {
        int updated = leadScoringAgent.runScoring();
        return AjaxResult.success("已更新 " + updated + " 条评分");
    }

    @PostMapping("/report/generate")
    public AjaxResult reportGenerate(@RequestBody Map<String, Object> params) {
        String type = (String) params.getOrDefault("type", "daily");
        String date = (String) params.getOrDefault("date",
            new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
        return AjaxResult.success(reportAgent.generate(type, date));
    }

    @PostMapping("/contract/review")
    public AjaxResult contractReview(@RequestBody Map<String, Object> params) {
        Long contractId = Long.valueOf(params.get("contractId").toString());
        return AjaxResult.success(contractReviewAgent.review(contractId));
    }
}
