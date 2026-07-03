package com.ruoyi.web.controller.crm;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.crm.domain.CrmCustomerShare;
import com.ruoyi.crm.service.ICrmCustomerShareService;

@RestController
@RequestMapping("/crm/share")
public class CrmCustomerShareController extends BaseController
{
    @Autowired
    private ICrmCustomerShareService crmCustomerShareService;

    @PreAuthorize("@ss.hasPermi('crm:customer:list')")
    @GetMapping("/list/{customerId}")
    public AjaxResult list(@PathVariable Long customerId)
    {
        CrmCustomerShare share = new CrmCustomerShare();
        share.setCustomerId(customerId);
        List<CrmCustomerShare> list = crmCustomerShareService.selectCrmCustomerShareList(share);
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('crm:customer:edit')")
    @Log(title = "客户共享", businessType = BusinessType.INSERT)
    @PostMapping("/toUser")
    public AjaxResult shareToUser(@RequestBody CrmCustomerShare share)
    {
        share.setFromUserId(SecurityUtils.getUserId());
        share.setShareType("manual");
        return toAjax(crmCustomerShareService.shareCustomer(share.getCustomerId(), share.getFromUserId(), share.getToUserId()));
    }

    @PreAuthorize("@ss.hasPermi('crm:customer:edit')")
    @Log(title = "客户共享", businessType = BusinessType.INSERT)
    @PostMapping("/toDept")
    public AjaxResult shareToDept(@RequestBody CrmCustomerShare share)
    {
        share.setFromUserId(SecurityUtils.getUserId());
        share.setShareType("manual");
        return toAjax(crmCustomerShareService.shareCustomerToDept(share.getCustomerId(), share.getFromUserId(), share.getToDeptId()));
    }

    @PreAuthorize("@ss.hasPermi('crm:customer:edit')")
    @Log(title = "客户共享", businessType = BusinessType.DELETE)
    @DeleteMapping("/{shareId}")
    public AjaxResult remove(@PathVariable Long shareId)
    {
        return toAjax(crmCustomerShareService.deleteShare(shareId));
    }

    @PreAuthorize("@ss.hasPermi('crm:customer:list')")
    @GetMapping("/pending")
    public AjaxResult pending()
    {
        Long deptId = SecurityUtils.getDeptId();
        return success(crmCustomerShareService.getDeptPendingShares(deptId));
    }

    @PreAuthorize("@ss.hasPermi('crm:customer:edit')")
    @Log(title = "客户共享", businessType = BusinessType.INSERT)
    @PostMapping("/assignFromDept")
    public AjaxResult assignFromDept(@RequestBody Map<String, Object> params)
    {
        Long customerId = Long.valueOf(params.get("customerId").toString());
        Long fromUserId = Long.valueOf(params.get("fromUserId").toString());
        Long toUserId = Long.valueOf(params.get("toUserId").toString());
        return toAjax(crmCustomerShareService.assignFromDeptPool(customerId, fromUserId, toUserId));
    }
}
