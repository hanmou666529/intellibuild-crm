package com.ruoyi.crm.ai.web;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.crm.ai.prompt.domain.SysAiPrompt;
import com.ruoyi.crm.ai.prompt.service.PromptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/crm/ai/prompt")
public class PromptController extends BaseController {
    @Autowired private PromptService promptService;

    @PreAuthorize("@ss.hasPermi('crm:ai:prompt')")
    @GetMapping("/list")
    public TableDataInfo list(SysAiPrompt query) {
        startPage();
        List<SysAiPrompt> list = promptService.selectList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('crm:ai:prompt')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return AjaxResult.success(promptService.selectById(id));
    }

    @PreAuthorize("@ss.hasPermi('crm:ai:prompt')")
    @Log(title = "Prompt管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SysAiPrompt prompt) {
        return toAjax(promptService.insert(prompt));
    }

    @PreAuthorize("@ss.hasPermi('crm:ai:prompt')")
    @Log(title = "Prompt管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysAiPrompt prompt) {
        return toAjax(promptService.update(prompt));
    }

    @PreAuthorize("@ss.hasPermi('crm:ai:prompt')")
    @Log(title = "Prompt管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(promptService.delete(ids));
    }
}
