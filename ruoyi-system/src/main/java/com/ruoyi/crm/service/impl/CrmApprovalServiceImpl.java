package com.ruoyi.crm.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.crm.domain.CrmApprovalNode;
import com.ruoyi.crm.domain.CrmApprovalRequest;
import com.ruoyi.crm.domain.CrmApprovalTemplate;
import com.ruoyi.crm.mapper.CrmApprovalNodeMapper;
import com.ruoyi.crm.mapper.CrmApprovalRequestMapper;
import com.ruoyi.crm.mapper.CrmApprovalTemplateMapper;
import com.ruoyi.crm.service.ICrmApprovalService;
import com.ruoyi.crm.service.ICrmContractService;

@Service
public class CrmApprovalServiceImpl implements ICrmApprovalService
{
    private static final Logger log = LoggerFactory.getLogger(CrmApprovalServiceImpl.class);

    @Autowired
    private CrmApprovalTemplateMapper approvalTemplateMapper;

    @Autowired
    private CrmApprovalRequestMapper approvalRequestMapper;

    @Autowired
    private CrmApprovalNodeMapper approvalNodeMapper;

    @Autowired
    @Lazy
    private ICrmContractService crmContractService;

    // ====== 模板 CRUD ======

    @Override
    public List<CrmApprovalTemplate> selectCrmApprovalTemplateList(CrmApprovalTemplate template)
    {
        return approvalTemplateMapper.selectCrmApprovalTemplateList(template);
    }

    @Override
    public CrmApprovalTemplate selectCrmApprovalTemplateById(Long templateId)
    {
        return approvalTemplateMapper.selectCrmApprovalTemplateById(templateId);
    }

    @Override
    @Transactional
    public int insertCrmApprovalTemplate(CrmApprovalTemplate template)
    {
        template.setCreateBy(SecurityUtils.getUsername());
        template.setStatus("0");
        return approvalTemplateMapper.insertCrmApprovalTemplate(template);
    }

    @Override
    @Transactional
    public int updateCrmApprovalTemplate(CrmApprovalTemplate template)
    {
        template.setUpdateBy(SecurityUtils.getUsername());
        return approvalTemplateMapper.updateCrmApprovalTemplate(template);
    }

    @Override
    @Transactional
    public int deleteCrmApprovalTemplateByIds(Long[] templateIds)
    {
        return approvalTemplateMapper.deleteCrmApprovalTemplateByIds(templateIds);
    }

    // ====== 请求查询 ======

    @Override
    public List<CrmApprovalRequest> selectCrmApprovalRequestList(CrmApprovalRequest request)
    {
        return approvalRequestMapper.selectCrmApprovalRequestList(request);
    }

    @Override
    public CrmApprovalRequest selectCrmApprovalRequestById(Long requestId)
    {
        return approvalRequestMapper.selectCrmApprovalRequestById(requestId);
    }

    @Override
    public List<CrmApprovalRequest> selectPendingApprovals(Long userId)
    {
        return approvalRequestMapper.selectPendingByApproverId(userId);
    }

    @Override
    public List<CrmApprovalRequest> selectProcessedRequests()
    {
        return approvalRequestMapper.selectProcessedRequests();
    }

    @Override
    public List<CrmApprovalRequest> selectMyRequests(String submitBy)
    {
        CrmApprovalRequest q = new CrmApprovalRequest();
        q.setSubmitBy(submitBy);
        return approvalRequestMapper.selectCrmApprovalRequestList(q);
    }

    // ====== 审批引擎核心 ======

    @Override
    @Transactional
    public CrmApprovalRequest submitForApproval(String bizType, Long bizId, Double amount, String bizInfo)
    {
        CrmApprovalTemplate template = approvalTemplateMapper.selectTemplateByBizType(bizType);
        if (template == null)
        {
            log.warn("无可用审批模板，跳过审批流程 bizType={} bizId={}", bizType, bizId);
            return null;
        }

        JSONArray rules = JSON.parseArray(template.getRules());
        List<JSONObject> activeRules = filterRulesByCondition(rules, "amount", amount);

        if (activeRules.isEmpty())
        {
            log.info("条件路由后无可审批节点 bizType={} amount={}", bizType, amount);
            return null;
        }

        String username = SecurityUtils.getUsername();
        String nickname = SecurityUtils.getLoginUser().getUser().getNickName();

        CrmApprovalRequest req = new CrmApprovalRequest();
        req.setTemplateId(template.getTemplateId());
        req.setBizType(bizType);
        req.setBizId(bizId);
        req.setBizInfo(bizInfo);
        req.setAmount(amount);
        req.setStatus("0");
        req.setCurrentStep(1);
        req.setSubmitBy(username);
        req.setSubmitName(nickname);
        req.setSubmitTime(DateUtils.getNowDate());
        req.setCreateBy(username);
        approvalRequestMapper.insertCrmApprovalRequest(req);

        List<CrmApprovalNode> nodes = new ArrayList<>();
        for (JSONObject rule : activeRules)
        {
            CrmApprovalNode n = new CrmApprovalNode();
            n.setRequestId(req.getRequestId());
            n.setStepOrder(rule.getInteger("step"));
            n.setNodeLabel(rule.getString("label"));
            n.setApproverRole(rule.getString("approveRole"));
            n.setStatus("0");
            nodes.add(n);
        }
        if (!nodes.isEmpty())
        {
            approvalNodeMapper.insertBatch(nodes);
        }

        log.info("提交审批成功 requestId={} bizType={} bizId={} nodes={}",
                req.getRequestId(), bizType, bizId, nodes.size());
        return req;
    }

    @Override
    @Transactional
    public void approveNode(Long nodeId, String comment)
    {
        CrmApprovalNode node = approvalNodeMapper.selectCrmApprovalNodeById(nodeId);
        if (node == null) throw new ServiceException("审批节点不存在");
        if (!"0".equals(node.getStatus())) throw new ServiceException("该节点已审批");

        CrmApprovalRequest req = approvalRequestMapper.selectCrmApprovalRequestById(node.getRequestId());
        if (req == null || !"0".equals(req.getStatus())) throw new ServiceException("审批请求已处理");

        if (node.getApproverUserId() != null
                && !node.getApproverUserId().equals(SecurityUtils.getUserId()))
        {
            throw new ServiceException("您不是当前节点的审批人");
        }

        String username = SecurityUtils.getUsername();
        String nickname = SecurityUtils.getLoginUser().getUser().getNickName();

        node.setApproverUserId(SecurityUtils.getUserId());
        node.setApproverName(nickname);
        node.setStatus("1");
        node.setComment(comment);
        node.setOperateTime(DateUtils.getNowDate());
        approvalNodeMapper.updateCrmApprovalNode(node);

        List<CrmApprovalNode> allNodes = approvalNodeMapper.selectNodesByRequestId(req.getRequestId());
        boolean allApproved = true;
        boolean hasNext = false;
        for (CrmApprovalNode n : allNodes)
        {
            if ("0".equals(n.getStatus())) { allApproved = false; hasNext = true; break; }
            if ("2".equals(n.getStatus())) { allApproved = false; }
        }

        if (allApproved)
        {
            req.setStatus("1");
            req.setUpdateBy(username);
            approvalRequestMapper.updateCrmApprovalRequest(req);
            log.info("审批全部通过 requestId={}", req.getRequestId());
            onApprovalComplete(req);
        }
        else if (hasNext)
        {
            req.setCurrentStep(node.getStepOrder() + 1);
            req.setUpdateBy(username);
            approvalRequestMapper.updateCrmApprovalRequest(req);
        }
    }

    @Override
    @Transactional
    public void rejectNode(Long nodeId, String comment)
    {
        CrmApprovalNode node = approvalNodeMapper.selectCrmApprovalNodeById(nodeId);
        if (node == null) throw new ServiceException("审批节点不存在");
        if (!"0".equals(node.getStatus())) throw new ServiceException("该节点已审批");

        CrmApprovalRequest req = approvalRequestMapper.selectCrmApprovalRequestById(node.getRequestId());
        if (req == null || !"0".equals(req.getStatus())) throw new ServiceException("审批请求已处理");

        if (node.getApproverUserId() != null
                && !node.getApproverUserId().equals(SecurityUtils.getUserId()))
        {
            throw new ServiceException("您不是当前节点的审批人");
        }

        node.setApproverUserId(SecurityUtils.getUserId());
        node.setApproverName(SecurityUtils.getLoginUser().getUser().getNickName());
        node.setStatus("2");
        node.setComment(comment);
        node.setOperateTime(DateUtils.getNowDate());
        approvalNodeMapper.updateCrmApprovalNode(node);

        req.setStatus("2");
        req.setUpdateBy(SecurityUtils.getUsername());
        approvalRequestMapper.updateCrmApprovalRequest(req);
        log.info("审批已拒绝 requestId={} nodeId={}", req.getRequestId(), nodeId);

        onApprovalRejected(req);
    }

    @Override
    @Transactional
    public void cancelRequest(Long requestId)
    {
        CrmApprovalRequest req = approvalRequestMapper.selectCrmApprovalRequestById(requestId);
        if (req == null) throw new ServiceException("审批请求不存在");
        if (!"0".equals(req.getStatus())) throw new ServiceException("仅审批中的请求可撤销");
        if (!req.getSubmitBy().equals(SecurityUtils.getUsername())) throw new ServiceException("仅提交人可撤销");

        req.setStatus("3");
        req.setUpdateBy(SecurityUtils.getUsername());
        approvalRequestMapper.updateCrmApprovalRequest(req);
        log.info("审批已撤销 requestId={}", requestId);
    }

    @Override
    public List<CrmApprovalNode> selectNodesByRequestId(Long requestId)
    {
        return approvalNodeMapper.selectNodesByRequestId(requestId);
    }

    // ====== 条件路由引擎 ======

    private List<JSONObject> filterRulesByCondition(JSONArray rules, String field, Double value)
    {
        List<JSONObject> result = new ArrayList<>();
        for (int i = 0; i < rules.size(); i++)
        {
            JSONObject rule = rules.getJSONObject(i);
            JSONObject cond = rule.getJSONObject("condition");
            if (cond == null)
            {
                result.add(rule);
                continue;
            }
            String op = cond.getString("op");
            Object condVal = cond.get("value");
            if (">=".equals(op) && value >= toDouble(condVal))
            {
                result.add(rule);
            }
            else if ("<=".equals(op) && value <= toDouble(condVal))
            {
                result.add(rule);
            }
            else if (">".equals(op) && value > toDouble(condVal))
            {
                result.add(rule);
            }
            else if ("<".equals(op) && value < toDouble(condVal))
            {
                result.add(rule);
            }
        }
        return result;
    }

    private double toDouble(Object val)
    {
        if (val instanceof Number) return ((Number) val).doubleValue();
        return Double.parseDouble(val.toString());
    }

    // ====== 业务回调 ======

    private void onApprovalComplete(CrmApprovalRequest req)
    {
        log.info("审批完成回调: requestId={}, bizType={}, bizId={}", req.getRequestId(), req.getBizType(), req.getBizId());
        if ("contract".equals(req.getBizType()))
        {
            crmContractService.processApprovedContract(req.getBizId());
        }
    }

    private void onApprovalRejected(CrmApprovalRequest req)
    {
        log.info("审批拒绝回调: requestId={}, bizType={}, bizId={}", req.getRequestId(), req.getBizType(), req.getBizId());
    }
}
