package com.ruoyi.raw.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.raw.domain.JobRaw;
import com.ruoyi.raw.service.IJobRawService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 招聘原始数据Controller
 * 
 * @author ruoyi
 * @date 2025-11-24
 */
@RestController
@RequestMapping("/system/raw")
public class JobRawController extends BaseController
{
    @Autowired
    private IJobRawService jobRawService;

    /**
     * 查询招聘原始数据列表
     */
    @PreAuthorize("@ss.hasPermi('system:raw:list')")
    @GetMapping("/list")
    public TableDataInfo list(JobRaw jobRaw)
    {
        startPage();
        List<JobRaw> list = jobRawService.selectJobRawList(jobRaw);
        return getDataTable(list);
    }

    /**
     * 导出招聘原始数据列表
     */
    @PreAuthorize("@ss.hasPermi('system:raw:export')")
    @Log(title = "招聘原始数据", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, JobRaw jobRaw)
    {
        List<JobRaw> list = jobRawService.selectJobRawList(jobRaw);
        ExcelUtil<JobRaw> util = new ExcelUtil<JobRaw>(JobRaw.class);
        util.exportExcel(response, list, "招聘原始数据数据");
    }

    /**
     * 获取招聘原始数据详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:raw:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(jobRawService.selectJobRawById(id));
    }

    /**
     * 新增招聘原始数据
     */
    @PreAuthorize("@ss.hasPermi('system:raw:add')")
    @Log(title = "招聘原始数据", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody JobRaw jobRaw)
    {
        return toAjax(jobRawService.insertJobRaw(jobRaw));
    }

    /**
     * 修改招聘原始数据
     */
    @PreAuthorize("@ss.hasPermi('system:raw:edit')")
    @Log(title = "招聘原始数据", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody JobRaw jobRaw)
    {
        return toAjax(jobRawService.updateJobRaw(jobRaw));
    }

    /**
     * 删除招聘原始数据
     */
    @PreAuthorize("@ss.hasPermi('system:raw:remove')")
    @Log(title = "招聘原始数据", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(jobRawService.deleteJobRawByIds(ids));
    }
}
