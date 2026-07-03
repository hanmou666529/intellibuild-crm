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
import com.ruoyi.crm.domain.CrmOrder;
import com.ruoyi.crm.service.ICrmOrderService;

@RestController
@RequestMapping("/crm/order")
public class CrmOrderController extends BaseController
{
    @Autowired
    private ICrmOrderService crmOrderService;

    @PreAuthorize("@ss.hasPermi('crm:order:list')")
    @CrmDataScope(deptAlias = "o", userAlias = "o")
    @GetMapping("/list")
    public TableDataInfo list(CrmOrder order)
    {
        startPage();
        List<CrmOrder> list = crmOrderService.selectCrmOrderList(order);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('crm:order:query')")
    @GetMapping(value = { "/", "/{orderId}" })
    public AjaxResult getInfo(@PathVariable(value = "orderId", required = false) Long orderId)
    {
        AjaxResult ajax = AjaxResult.success();
        if (orderId != null)
        {
            CrmOrder order = crmOrderService.selectCrmOrderById(orderId);
            ajax.put(AjaxResult.DATA_TAG, order);
        }
        return ajax;
    }

    @PreAuthorize("@ss.hasPermi('crm:order:add')")
    @Log(title = "订单管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CrmOrder order)
    {
        return toAjax(crmOrderService.insertCrmOrder(order));
    }

    @PreAuthorize("@ss.hasPermi('crm:order:edit')")
    @Log(title = "订单管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CrmOrder order)
    {
        return toAjax(crmOrderService.updateCrmOrder(order));
    }

    @PreAuthorize("@ss.hasPermi('crm:order:paid')")
    @Log(title = "订单管理", businessType = BusinessType.UPDATE)
    @PutMapping("/markPaid")
    public AjaxResult markPaid(@RequestBody CrmOrder order)
    {
        return toAjax(crmOrderService.markAsPaid(order));
    }

    @PreAuthorize("@ss.hasPermi('crm:order:remove')")
    @Log(title = "订单管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{orderIds}")
    public AjaxResult remove(@PathVariable Long[] orderIds)
    {
        return toAjax(crmOrderService.deleteCrmOrderByIds(orderIds));
    }
}
