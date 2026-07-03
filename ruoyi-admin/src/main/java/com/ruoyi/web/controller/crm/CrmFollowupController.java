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
import com.ruoyi.crm.domain.CrmFollowup;
import com.ruoyi.crm.service.ICrmFollowupService;

@RestController
@RequestMapping("/crm/followup")
public class CrmFollowupController extends BaseController
{
    @Autowired
    private ICrmFollowupService crmFollowupService;

    @PreAuthorize("@ss.hasPermi('crm:followup:list')")
    @CrmDataScope(deptAlias = "c", userAlias = "c")
    @GetMapping("/list")
    public TableDataInfo list(CrmFollowup followup)
    {
        startPage();
        List<CrmFollowup> list = crmFollowupService.selectCrmFollowupList(followup);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('crm:followup:query')")
    @GetMapping(value = { "/", "/{followupId}" })
    public AjaxResult getInfo(@PathVariable(value = "followupId", required = false) Long followupId)
    {
        AjaxResult ajax = AjaxResult.success();
        if (followupId != null)
        {
            CrmFollowup followup = crmFollowupService.selectCrmFollowupById(followupId);
            ajax.put(AjaxResult.DATA_TAG, followup);
        }
        return ajax;
    }

    @PreAuthorize("@ss.hasPermi('crm:followup:query')")
    @GetMapping("/byCustomer/{customerId}")
    public AjaxResult getByCustomer(@PathVariable Long customerId)
    {
        List<CrmFollowup> list = crmFollowupService.selectCrmFollowupByCustomerId(customerId);
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('crm:followup:add')")
    @Log(title = "跟进管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CrmFollowup followup)
    {
        return toAjax(crmFollowupService.insertCrmFollowup(followup));
    }

    @PreAuthorize("@ss.hasPermi('crm:followup:edit')")
    @Log(title = "跟进管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CrmFollowup followup)
    {
        return toAjax(crmFollowupService.updateCrmFollowup(followup));
    }

    @PreAuthorize("@ss.hasPermi('crm:followup:remove')")
    @Log(title = "跟进管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{followupIds}")
    public AjaxResult remove(@PathVariable Long[] followupIds)
    {
        return toAjax(crmFollowupService.deleteCrmFollowupByIds(followupIds));
    }
}
