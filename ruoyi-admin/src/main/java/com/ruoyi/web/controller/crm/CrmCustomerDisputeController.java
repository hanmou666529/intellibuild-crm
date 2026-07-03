package com.ruoyi.web.controller.crm;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.ruoyi.crm.domain.CrmCustomerDispute;
import com.ruoyi.crm.service.ICrmCustomerDisputeService;

@RestController
@RequestMapping("/crm/dispute")
public class CrmCustomerDisputeController extends BaseController
{
    @Autowired
    private ICrmCustomerDisputeService crmCustomerDisputeService;

    @PreAuthorize("@ss.hasPermi('crm:dispute:list')")
    @GetMapping("/list")
    public TableDataInfo list(CrmCustomerDispute dispute)
    {
        startPage();
        List<CrmCustomerDispute> list = crmCustomerDisputeService.selectList(dispute);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('crm:dispute:query')")
    @GetMapping(value = { "/", "/{disputeId}" })
    public AjaxResult getInfo(@PathVariable(value = "disputeId", required = false) Long disputeId)
    {
        AjaxResult ajax = AjaxResult.success();
        if (disputeId != null)
        {
            CrmCustomerDispute dispute = crmCustomerDisputeService.selectById(disputeId);
            ajax.put(AjaxResult.DATA_TAG, dispute);
        }
        return ajax;
    }

    @PreAuthorize("@ss.hasPermi('crm:dispute:add')")
    @Log(title = "客户冲突", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public AjaxResult add(@RequestBody CrmCustomerDispute dispute)
    {
        return toAjax(crmCustomerDisputeService.insert(dispute));
    }

    @PreAuthorize("@ss.hasPermi('crm:dispute:handle')")
    @Log(title = "客户冲突", businessType = BusinessType.UPDATE)
    @PutMapping("/handle")
    public AjaxResult handle(@RequestBody Map<String, Object> params)
    {
        Long disputeId = Long.valueOf(params.get("disputeId").toString());
        String action = (String) params.get("action");
        Long targetUserId = params.containsKey("targetUserId") ? Long.valueOf(params.get("targetUserId").toString()) : null;
        String remark = (String) params.get("remark");
        return toAjax(crmCustomerDisputeService.handle(disputeId, action, targetUserId, remark));
    }

    @PreAuthorize("@ss.hasPermi('crm:dispute:arbitrate')")
    @Log(title = "客户冲突", businessType = BusinessType.UPDATE)
    @PutMapping("/arbitrate")
    public AjaxResult arbitrate(@RequestBody Map<String, Object> params)
    {
        Long disputeId = Long.valueOf(params.get("disputeId").toString());
        String result = (String) params.get("result");
        String remark = (String) params.get("remark");
        return toAjax(crmCustomerDisputeService.arbitrate(disputeId, result, remark));
    }
}
