package com.ruoyi.web.controller.crm;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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
import com.ruoyi.crm.domain.CrmProductCategory;
import com.ruoyi.crm.service.ICrmProductCategoryService;

@RestController
@RequestMapping("/crm/category")
public class CrmProductCategoryController extends BaseController
{
    @Autowired
    private ICrmProductCategoryService crmProductCategoryService;

    @PreAuthorize("@ss.hasPermi('crm:category:list')")
    @GetMapping("/list")
    public TableDataInfo list(CrmProductCategory category)
    {
        startPage();
        List<CrmProductCategory> list = crmProductCategoryService.selectCrmProductCategoryList(category);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('crm:category:list')")
    @GetMapping("/treeList")
    public AjaxResult treeList()
    {
        CrmProductCategory query = new CrmProductCategory();
        List<CrmProductCategory> all = crmProductCategoryService.selectCrmProductCategoryList(query);
        List<CrmProductCategory> tree = buildTree(all);
        return AjaxResult.success(tree);
    }

    @PreAuthorize("@ss.hasPermi('crm:category:query')")
    @GetMapping(value = { "/", "/{categoryId}" })
    public AjaxResult getInfo(@PathVariable(value = "categoryId", required = false) Long categoryId)
    {
        AjaxResult ajax = AjaxResult.success();
        if (categoryId != null)
        {
            CrmProductCategory category = crmProductCategoryService.selectCrmProductCategoryById(categoryId);
            ajax.put(AjaxResult.DATA_TAG, category);
        }
        return ajax;
    }

    @PreAuthorize("@ss.hasPermi('crm:category:add')")
    @Log(title = "产品分类", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CrmProductCategory category)
    {
        return toAjax(crmProductCategoryService.insertCrmProductCategory(category));
    }

    @PreAuthorize("@ss.hasPermi('crm:category:edit')")
    @Log(title = "产品分类", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CrmProductCategory category)
    {
        return toAjax(crmProductCategoryService.updateCrmProductCategory(category));
    }

    @PreAuthorize("@ss.hasPermi('crm:category:remove')")
    @Log(title = "产品分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{categoryIds}")
    public AjaxResult remove(@PathVariable Long[] categoryIds)
    {
        return toAjax(crmProductCategoryService.deleteCrmProductCategoryByIds(categoryIds));
    }

    private List<CrmProductCategory> buildTree(List<CrmProductCategory> list)
    {
        List<CrmProductCategory> roots = list.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0)
                .collect(Collectors.toList());
        for (CrmProductCategory root : roots)
        {
            buildChildren(root, list);
        }
        return roots;
    }

    private void buildChildren(CrmProductCategory parent, List<CrmProductCategory> all)
    {
        List<CrmProductCategory> children = all.stream()
                .filter(c -> parent.getCategoryId().equals(c.getParentId()))
                .collect(Collectors.toList());
        if (!children.isEmpty())
        {
            parent.setChildren(children);
            for (CrmProductCategory child : children)
            {
                buildChildren(child, all);
            }
        }
    }
}
