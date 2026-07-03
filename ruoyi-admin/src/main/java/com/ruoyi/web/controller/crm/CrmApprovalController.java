package com.ruoyi.web.controller.crm;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.crm.domain.CrmApprovalNode;
import com.ruoyi.crm.domain.CrmApprovalRequest;
import com.ruoyi.crm.domain.CrmApprovalTemplate;
import com.ruoyi.crm.service.ICrmApprovalService;

@RestController
@RequestMapping("/crm/approval")
public class CrmApprovalController extends BaseController
{
    @Autowired
    private ICrmApprovalService crmApprovalService;

    // ====== 模板管理 ======

    @PreAuthorize("@ss.hasPermi('crm:approval:template:list')")
    @GetMapping("/template/list")
    public TableDataInfo templateList(CrmApprovalTemplate template)
    {
        startPage();
        List<CrmApprovalTemplate> list = crmApprovalService.selectCrmApprovalTemplateList(template);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('crm:approval:template:query')")
    @GetMapping("/template/{templateId}")
    public AjaxResult getTemplate(@PathVariable Long templateId)
    {
        return success(crmApprovalService.selectCrmApprovalTemplateById(templateId));
    }

    @PreAuthorize("@ss.hasPermi('crm:approval:template:add')")
    @Log(title = "审批模板", businessType = BusinessType.INSERT)
    @PostMapping("/template")
    public AjaxResult addTemplate(@RequestBody CrmApprovalTemplate template)
    {
        return toAjax(crmApprovalService.insertCrmApprovalTemplate(template));
    }

    @PreAuthorize("@ss.hasPermi('crm:approval:template:edit')")
    @Log(title = "审批模板", businessType = BusinessType.UPDATE)
    @PutMapping("/template")
    public AjaxResult editTemplate(@RequestBody CrmApprovalTemplate template)
    {
        return toAjax(crmApprovalService.updateCrmApprovalTemplate(template));
    }

    @PreAuthorize("@ss.hasPermi('crm:approval:template:remove')")
    @Log(title = "审批模板", businessType = BusinessType.DELETE)
    @DeleteMapping("/template/{templateIds}")
    public AjaxResult removeTemplate(@PathVariable Long[] templateIds)
    {
        return toAjax(crmApprovalService.deleteCrmApprovalTemplateByIds(templateIds));
    }

    // ====== 审批请求 & 待审批 ======

    @PreAuthorize("@ss.hasPermi('crm:approval:pending:list')")
    @GetMapping("/pending/list")
    public TableDataInfo pendingList()
    {
        startPage();
        List<CrmApprovalRequest> list = crmApprovalService.selectPendingApprovals(SecurityUtils.getUserId());
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('crm:approval:processed:list')")
    @GetMapping("/processed/list")
    public TableDataInfo processedList()
    {
        startPage();
        List<CrmApprovalRequest> list = crmApprovalService.selectProcessedRequests();
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('crm:approval:my:list')")
    @GetMapping("/my/list")
    public TableDataInfo myList()
    {
        startPage();
        List<CrmApprovalRequest> list = crmApprovalService.selectMyRequests(SecurityUtils.getUsername());
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('crm:approval:my:list')")
    @GetMapping("/my/{requestId}")
    public AjaxResult getRequest(@PathVariable Long requestId)
    {
        return success(crmApprovalService.selectCrmApprovalRequestById(requestId));
    }

    @PreAuthorize("@ss.hasPermi('crm:approval:my:list')")
    @GetMapping("/{requestId}/nodes")
    public AjaxResult getNodes(@PathVariable Long requestId)
    {
        return success(crmApprovalService.selectNodesByRequestId(requestId));
    }

    @PreAuthorize("@ss.hasPermi('crm:approval:my:list')")
    @Log(title = "审批管理", businessType = BusinessType.UPDATE)
    @PutMapping("/approve")
    public AjaxResult approve(@RequestParam Long nodeId, @RequestParam(required = false) String comment)
    {
        crmApprovalService.approveNode(nodeId, comment);
        return success("审批通过");
    }

    @PreAuthorize("@ss.hasPermi('crm:approval:my:list')")
    @Log(title = "审批管理", businessType = BusinessType.UPDATE)
    @PutMapping("/reject")
    public AjaxResult reject(@RequestParam Long nodeId, @RequestParam String comment)
    {
        crmApprovalService.rejectNode(nodeId, comment);
        return success("已拒绝");
    }

    @PreAuthorize("@ss.hasPermi('crm:approval:my:list')")
    @Log(title = "审批管理", businessType = BusinessType.UPDATE)
    @PutMapping("/cancel/{requestId}")
    public AjaxResult cancel(@PathVariable Long requestId)
    {
        crmApprovalService.cancelRequest(requestId);
        return success("已撤销");
    }
}
