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
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.crm.domain.CrmProduct;
import com.ruoyi.crm.service.ICrmProductService;

@RestController
@RequestMapping("/crm/product")
public class CrmProductController extends BaseController
{
    @Autowired
    private ICrmProductService crmProductService;

    @PreAuthorize("@ss.hasPermi('crm:product:list')")
    @GetMapping("/list")
    public TableDataInfo list(CrmProduct product)
    {
        startPage();
        List<CrmProduct> list = crmProductService.selectCrmProductList(product);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('crm:product:query')")
    @GetMapping(value = { "/", "/{productId}" })
    public AjaxResult getInfo(@PathVariable(value = "productId", required = false) Long productId)
    {
        AjaxResult ajax = AjaxResult.success();
        if (productId != null)
        {
            CrmProduct product = crmProductService.selectCrmProductById(productId);
            ajax.put(AjaxResult.DATA_TAG, product);
        }
        return ajax;
    }

    @PreAuthorize("@ss.hasPermi('crm:product:add')")
    @Log(title = "产品管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CrmProduct product)
    {
        return toAjax(crmProductService.insertCrmProduct(product));
    }

    @PreAuthorize("@ss.hasPermi('crm:product:edit')")
    @Log(title = "产品管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CrmProduct product)
    {
        return toAjax(crmProductService.updateCrmProduct(product));
    }

    @PreAuthorize("@ss.hasPermi('crm:product:remove')")
    @Log(title = "产品管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{productIds}")
    public AjaxResult remove(@PathVariable Long[] productIds)
    {
        return toAjax(crmProductService.deleteCrmProductByIds(productIds));
    }
}
