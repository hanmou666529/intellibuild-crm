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
import com.ruoyi.crm.domain.CrmPipeline;
import com.ruoyi.crm.service.ICrmPipelineService;

@RestController
@RequestMapping("/crm/pipeline")
public class CrmPipelineController extends BaseController
{
    @Autowired
    private ICrmPipelineService crmPipelineService;

    @PreAuthorize("@ss.hasPermi('crm:pipeline:list')")
    @CrmDataScope(deptAlias = "c", userAlias = "c")
    @GetMapping("/list")
    public TableDataInfo list(CrmPipeline pipeline)
    {
        startPage();
        List<CrmPipeline> list = crmPipelineService.selectCrmPipelineList(pipeline);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('crm:pipeline:query')")
    @GetMapping(value = { "/", "/{pipelineId}" })
    public AjaxResult getInfo(@PathVariable(value = "pipelineId", required = false) Long pipelineId)
    {
        AjaxResult ajax = AjaxResult.success();
        if (pipelineId != null)
        {
            CrmPipeline pipeline = crmPipelineService.selectCrmPipelineById(pipelineId);
            ajax.put(AjaxResult.DATA_TAG, pipeline);
        }
        return ajax;
    }

    @PreAuthorize("@ss.hasPermi('crm:pipeline:add')")
    @Log(title = "销售管道", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CrmPipeline pipeline)
    {
        return toAjax(crmPipelineService.insertCrmPipeline(pipeline));
    }

    @PreAuthorize("@ss.hasPermi('crm:pipeline:edit')")
    @Log(title = "销售管道", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CrmPipeline pipeline)
    {
        return toAjax(crmPipelineService.updateCrmPipeline(pipeline));
    }

    @PreAuthorize("@ss.hasPermi('crm:pipeline:edit')")
    @Log(title = "销售管道", businessType = BusinessType.UPDATE)
    @PutMapping("/stage")
    public AjaxResult stage(@RequestBody CrmPipeline pipeline)
    {
        return toAjax(crmPipelineService.updateStage(pipeline));
    }

    @PreAuthorize("@ss.hasPermi('crm:pipeline:remove')")
    @Log(title = "销售管道", businessType = BusinessType.DELETE)
    @DeleteMapping("/{pipelineIds}")
    public AjaxResult remove(@PathVariable Long[] pipelineIds)
    {
        return toAjax(crmPipelineService.deleteCrmPipelineByIds(pipelineIds));
    }
}
