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
import com.ruoyi.crm.domain.CrmTag;
import com.ruoyi.crm.service.ICrmTagService;

@RestController
@RequestMapping("/crm/tag")
public class CrmTagController extends BaseController
{
    @Autowired
    private ICrmTagService crmTagService;

    @PreAuthorize("@ss.hasPermi('crm:tag:list')")
    @GetMapping("/list")
    public TableDataInfo list(CrmTag tag)
    {
        startPage();
        List<CrmTag> list = crmTagService.selectCrmTagList(tag);
        return getDataTable(list);
    }

    @GetMapping("/active")
    public AjaxResult activeList()
    {
        return success(crmTagService.selectActiveTags());
    }

    @PreAuthorize("@ss.hasPermi('crm:tag:query')")
    @GetMapping("/{tagId}")
    public AjaxResult getInfo(@PathVariable Long tagId)
    {
        return success(crmTagService.selectCrmTagById(tagId));
    }

    @PreAuthorize("@ss.hasPermi('crm:tag:add')")
    @Log(title = "标签管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CrmTag tag)
    {
        return toAjax(crmTagService.insertCrmTag(tag));
    }

    @PreAuthorize("@ss.hasPermi('crm:tag:edit')")
    @Log(title = "标签管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CrmTag tag)
    {
        return toAjax(crmTagService.updateCrmTag(tag));
    }

    @PreAuthorize("@ss.hasPermi('crm:tag:remove')")
    @Log(title = "标签管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{tagIds}")
    public AjaxResult remove(@PathVariable Long[] tagIds)
    {
        return toAjax(crmTagService.deleteCrmTagByIds(tagIds));
    }
}
