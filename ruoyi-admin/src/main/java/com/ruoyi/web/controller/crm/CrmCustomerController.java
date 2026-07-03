package com.ruoyi.web.controller.crm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map;
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
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.crm.domain.CrmCustomer;
import com.ruoyi.crm.service.ICrmCustomerService;

@RestController
@RequestMapping("/crm/customer")
public class CrmCustomerController extends BaseController
{
    @Autowired
    private ICrmCustomerService crmCustomerService;

    @PreAuthorize("@ss.hasPermi('crm:customer:list')")
    @CrmDataScope(deptAlias = "c", userAlias = "c")
    @GetMapping("/list")
    public TableDataInfo list(CrmCustomer customer)
    {
        startPage();
        customer.getParams().put("currentUserId", SecurityUtils.getUserId());
        customer.getParams().put("currentDeptId", SecurityUtils.getDeptId());
        List<CrmCustomer> list = crmCustomerService.selectCrmCustomerList(customer);
        return getDataTable(list);
    }

    @Log(title = "客户管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('crm:customer:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, CrmCustomer customer)
    {
        List<CrmCustomer> list = crmCustomerService.selectCrmCustomerList(customer);
        ExcelUtil<CrmCustomer> util = new ExcelUtil<CrmCustomer>(CrmCustomer.class);
        util.exportExcel(response, list, "客户数据");
    }

    @Log(title = "客户管理", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('crm:customer:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<CrmCustomer> util = new ExcelUtil<CrmCustomer>(CrmCustomer.class);
        List<CrmCustomer> customerList = util.importExcel(file.getInputStream());
        String operName = getUsername();
        String message = crmCustomerService.importCustomer(customerList, updateSupport, operName);
        return success(message);
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<CrmCustomer> util = new ExcelUtil<CrmCustomer>(CrmCustomer.class);
        util.importTemplateExcel(response, "客户数据");
    }

    @PreAuthorize("@ss.hasPermi('crm:customer:query')")
    @CrmDataScope(deptAlias = "c", userAlias = "c")
    @GetMapping(value = { "/", "/{customerId}" })
    public AjaxResult getInfo(@PathVariable(value = "customerId", required = false) Long customerId)
    {
        AjaxResult ajax = AjaxResult.success();
        if (customerId != null)
        {
            CrmCustomer customer = new CrmCustomer();
            customer.getParams().put("currentUserId", SecurityUtils.getUserId());
            customer.getParams().put("currentDeptId", SecurityUtils.getDeptId());
            customer.setCustomerId(customerId);
            List<CrmCustomer> list = crmCustomerService.selectCrmCustomerList(customer);
            if (list != null && !list.isEmpty())
            {
                ajax.put(AjaxResult.DATA_TAG, list.get(0));
            }
        }
        return ajax;
    }

    @PreAuthorize("@ss.hasPermi('crm:customer:add')")
    @Log(title = "客户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CrmCustomer customer)
    {
        customer.setCreateBy(getUsername());
        return toAjax(crmCustomerService.insertCrmCustomer(customer));
    }

    @PreAuthorize("@ss.hasPermi('crm:customer:edit')")
    @Log(title = "客户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CrmCustomer customer)
    {
        customer.setUpdateBy(getUsername());
        return toAjax(crmCustomerService.updateCrmCustomer(customer));
    }

    @PreAuthorize("@ss.hasPermi('crm:customer:remove')")
    @Log(title = "客户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{customerIds}")
    public AjaxResult remove(@PathVariable Long[] customerIds)
    {
        return toAjax(crmCustomerService.deleteCrmCustomerByIds(customerIds));
    }

    @PreAuthorize("@ss.hasPermi('crm:customer:assign')")
    @Log(title = "客户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/assign")
    public AjaxResult assign(@RequestBody CrmCustomer customer)
    {
        return toAjax(crmCustomerService.assignCustomer(customer));
    }

    @PreAuthorize("@ss.hasPermi('crm:customer:assign')")
    @GetMapping("/assignCheck/{customerId}")
    public AjaxResult assignCheck(@PathVariable Long customerId)
    {
        Map<String, Object> result = new HashMap<>();
        CrmCustomer customer = crmCustomerService.selectCrmCustomerById(customerId);
        if (customer != null)
        {
            result.put("customerName", customer.getCustomerName());
            result.put("belongUserId", customer.getBelongUserId());
            result.put("belongUserName", customer.getBelongUserName());
        }
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('crm:customer:assign')")
    @Log(title = "客户管理", businessType = BusinessType.UPDATE)
    @PostMapping("/batchAssign")
    public AjaxResult batchAssign(@RequestBody Map<String, Object> params)
    {
        @SuppressWarnings("unchecked")
        List<Integer> customerIdList = (List<Integer>) params.get("customerIds");
        Integer targetUserId = (Integer) params.get("targetUserId");
        if (customerIdList == null || customerIdList.isEmpty())
        {
            return error("请选择要分配的客户");
        }
        if (targetUserId == null)
        {
            return error("请选择目标用户");
        }
        boolean force = params.containsKey("force") && Boolean.TRUE.equals(params.get("force"));
        Long[] ids = customerIdList.stream().map(Integer::longValue).toArray(Long[]::new);
        return success(crmCustomerService.batchAssignCustomers(ids, targetUserId.longValue(), force));
    }

    @PreAuthorize("@ss.hasPermi('crm:customer:assign')")
    @PostMapping("/batchAssignCheck")
    public AjaxResult batchAssignCheck(@RequestBody Map<String, Object> params)
    {
        @SuppressWarnings("unchecked")
        List<Integer> customerIdList = (List<Integer>) params.get("customerIds");
        if (customerIdList == null || customerIdList.isEmpty())
        {
            return error("请选择要分配的客户");
        }
        Long[] ids = customerIdList.stream().map(Integer::longValue).toArray(Long[]::new);
        return success(crmCustomerService.checkBatchAssignCustomers(ids));
    }

    @PreAuthorize("@ss.hasPermi('crm:customer:pool')")
    @Log(title = "客户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/pool")
    public AjaxResult putToPool(@RequestBody CrmCustomer customer)
    {
        return toAjax(crmCustomerService.putToPool(customer));
    }

    @PreAuthorize("@ss.hasPermi('crm:customer:claim')")
    @Log(title = "客户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/claim")
    public AjaxResult claimFromPool(@RequestBody CrmCustomer customer)
    {
        return toAjax(crmCustomerService.claimFromPool(customer.getCustomerId()));
    }

    @PreAuthorize("@ss.hasPermi('crm:pipeline:add')")
    @Log(title = "客户管理", businessType = BusinessType.INSERT)
    @PostMapping("/{customerId}/pipeline")
    public AjaxResult addToPipeline(@PathVariable Long customerId)
    {
        int result = crmCustomerService.addToPipeline(customerId);
        if (result == 2)
        {
            return success("该客户已在销售管道中");
        }
        return success("已加入销售管道");
    }

    @PreAuthorize("@ss.hasPermi('crm:customer:query')")
    @GetMapping("/checkPhoneUnique")
    public AjaxResult checkPhoneUnique(CrmCustomer customer)
    {
        return crmCustomerService.checkPhoneUnique(customer) ? success("true") : error("手机号码已存在");
    }

    @PreAuthorize("@ss.hasPermi('crm:customer:add')")
    @GetMapping("/checkDuplicate")
    public AjaxResult checkDuplicate(@RequestParam(required = false) String customerName,
            @RequestParam(required = false) String phone, @RequestParam(required = false) String email,
            @RequestParam(required = false) String company)
    {
        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerName(customerName);
        customer.setPhone(phone);
        customer.setEmail(email);
        customer.setCompany(company);
        List<CrmCustomer> list = crmCustomerService.checkDuplicate(customer);
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('crm:customer:merge')")
    @Log(title = "客户管理", businessType = BusinessType.UPDATE)
    @PostMapping("/merge")
    public AjaxResult merge(@RequestBody Map<String, Object> params)
    {
        Long keepCustomerId = Long.valueOf(params.get("keepCustomerId").toString());
        Long mergeCustomerId = Long.valueOf(params.get("mergeCustomerId").toString());
        crmCustomerService.mergeCustomer(keepCustomerId, mergeCustomerId);
        return success();
    }
}
