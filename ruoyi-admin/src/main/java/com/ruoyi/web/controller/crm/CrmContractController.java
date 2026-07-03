package com.ruoyi.web.controller.crm;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.CrmDataScope;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.crm.domain.CrmContract;
import com.ruoyi.crm.service.ICrmContractService;

@RestController
@RequestMapping("/crm/contract")
public class CrmContractController extends BaseController
{
    @Autowired
    private ICrmContractService crmContractService;

    @PreAuthorize("@ss.hasPermi('crm:contract:list')")
    @CrmDataScope(deptAlias = "ct", userAlias = "ct")
    @GetMapping("/list")
    public TableDataInfo list(CrmContract contract)
    {
        startPage();
        List<CrmContract> list = crmContractService.selectCrmContractList(contract);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('crm:contract:query')")
    @GetMapping(value = { "/", "/{contractId}" })
    public AjaxResult getInfo(@PathVariable(value = "contractId", required = false) Long contractId)
    {
        AjaxResult ajax = AjaxResult.success();
        if (contractId != null)
        {
            CrmContract contract = crmContractService.selectCrmContractById(contractId);
            ajax.put(AjaxResult.DATA_TAG, contract);
        }
        return ajax;
    }

    @PreAuthorize("@ss.hasPermi('crm:contract:add')")
    @Log(title = "合同管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CrmContract contract)
    {
        return toAjax(crmContractService.insertCrmContract(contract));
    }

    @PreAuthorize("@ss.hasPermi('crm:contract:edit')")
    @Log(title = "合同管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CrmContract contract)
    {
        return toAjax(crmContractService.updateCrmContract(contract));
    }

    @PreAuthorize("@ss.hasPermi('crm:contract:remove')")
    @Log(title = "合同管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{contractIds}")
    public AjaxResult remove(@PathVariable Long[] contractIds)
    {
        return toAjax(crmContractService.deleteCrmContractByIds(contractIds));
    }

    @PreAuthorize("@ss.hasPermi('crm:contract:approve')")
    @Log(title = "合同管理", businessType = BusinessType.UPDATE)
    @PutMapping("/approve/{contractId}")
    public AjaxResult approve(@PathVariable Long contractId)
    {
        return toAjax(crmContractService.approveContract(contractId));
    }

    @PreAuthorize("@ss.hasPermi('crm:contract:edit')")
    @Log(title = "合同管理", businessType = BusinessType.UPDATE)
    @PutMapping("/reSubmitApproval/{contractId}")
    public AjaxResult reSubmitApproval(@PathVariable Long contractId)
    {
        crmContractService.reSubmitApproval(contractId);
        return success();
    }

    /**
     * 合同附件上传
     */
    @PreAuthorize("@ss.hasPermi('crm:contract:edit')")
    @Log(title = "合同附件", businessType = BusinessType.UPDATE)
    @PostMapping("/upload")
    public AjaxResult uploadAttachment(@RequestParam("file") MultipartFile file, @RequestParam("contractId") Long contractId)
    {
        try
        {
            // 上传文件
            String fileName = crmContractService.uploadContractAttachment(file, contractId);
            AjaxResult ajax = AjaxResult.success();
            ajax.put("fileName", fileName);
            ajax.put("url", "/common/download?fileName=" + fileName);
            return ajax;
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 合同附件下载
     */
    @GetMapping("/download")
    public void downloadAttachment(@RequestParam("fileName") String fileName, HttpServletResponse response)
    {
        try
        {
            // 使用通用下载功能
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            java.io.File file = new java.io.File(com.ruoyi.common.config.RuoYiConfig.getUploadPath() + fileName);
            java.io.FileInputStream fis = new java.io.FileInputStream(file);
            org.apache.commons.io.IOUtils.copy(fis, response.getOutputStream());
            fis.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
