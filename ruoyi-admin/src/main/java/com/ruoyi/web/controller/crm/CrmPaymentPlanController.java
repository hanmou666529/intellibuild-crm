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
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.CrmDataScope;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.crm.domain.CrmPaymentPlan;
import com.ruoyi.crm.service.ICrmPaymentPlanService;

@RestController
@RequestMapping("/crm/payment")
public class CrmPaymentPlanController extends BaseController
{
    @Autowired
    private ICrmPaymentPlanService crmPaymentPlanService;

    @PreAuthorize("@ss.hasPermi('crm:payment:list')")
    @CrmDataScope(deptAlias = "pp", userAlias = "pp", shareAlias = "o")
    @GetMapping("/list")
    public TableDataInfo list(CrmPaymentPlan plan)
    {
        startPage();
        List<CrmPaymentPlan> list = crmPaymentPlanService.selectCrmPaymentPlanList(plan);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('crm:payment:query')")
    @GetMapping(value = { "/", "/{planId}" })
    public AjaxResult getInfo(@PathVariable(value = "planId", required = false) Long planId)
    {
        AjaxResult ajax = AjaxResult.success();
        if (planId != null)
        {
            CrmPaymentPlan plan = crmPaymentPlanService.selectCrmPaymentPlanById(planId);
            ajax.put(AjaxResult.DATA_TAG, plan);
        }
        return ajax;
    }

    @PreAuthorize("@ss.hasPermi('crm:payment:add')")
    @Log(title = "回款计划", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CrmPaymentPlan plan)
    {
        return toAjax(crmPaymentPlanService.insertCrmPaymentPlan(plan));
    }

    @PreAuthorize("@ss.hasPermi('crm:payment:edit')")
    @Log(title = "回款计划", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CrmPaymentPlan plan)
    {
        return toAjax(crmPaymentPlanService.updateCrmPaymentPlan(plan));
    }

    @PreAuthorize("@ss.hasPermi('crm:payment:edit')")
    @Log(title = "回款计划", businessType = BusinessType.UPDATE)
    @PutMapping("/markPaid")
    public AjaxResult markPaid(@RequestBody CrmPaymentPlan plan)
    {
        return toAjax(crmPaymentPlanService.markAsPaid(plan));
    }

    @PreAuthorize("@ss.hasPermi('crm:payment:remove')")
    @Log(title = "回款计划", businessType = BusinessType.DELETE)
    @DeleteMapping("/{planIds}")
    public AjaxResult remove(@PathVariable Long[] planIds)
    {
        return toAjax(crmPaymentPlanService.deleteCrmPaymentPlanByIds(planIds));
    }
}
