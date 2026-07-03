package com.ruoyi.web.controller.crm;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.SysOperLog;
import com.ruoyi.system.service.ISysOperLogService;

@RestController
@RequestMapping("/crm/audit")
public class CrmAuditController extends BaseController
{
    private static final List<String> CRM_MODULES = Arrays.asList(
        "客户管理", "合同管理", "订单管理", "回款计划", "跟进记录", "销售漏斗", "产品管理"
    );

    @Autowired
    private ISysOperLogService sysOperLogService;

    @PreAuthorize("@ss.hasPermi('crm:audit:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysOperLog operLog)
    {
        startPage();
        operLog.getParams().put("crmModules", true);
        List<SysOperLog> list = sysOperLogService.selectOperLogList(operLog);
        List<SysOperLog> filtered = new java.util.ArrayList<>();
        for (SysOperLog log : list)
        {
            if (CRM_MODULES.contains(log.getTitle()))
            {
                filtered.add(log);
            }
        }
        return getDataTable(filtered);
    }
}
