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
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.crm.domain.CrmNotification;
import com.ruoyi.crm.service.ICrmNotificationService;

@RestController
@RequestMapping("/crm/notification")
public class CrmNotificationController extends BaseController
{
    @Autowired
    private ICrmNotificationService crmNotificationService;

    @PreAuthorize("@ss.hasPermi('crm:notification:list')")
    @GetMapping("/list")
    public TableDataInfo list(CrmNotification notification)
    {
        startPage();
        List<CrmNotification> list = crmNotificationService.selectCrmNotificationList(notification);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('crm:notification:query')")
    @GetMapping(value = { "/", "/{notificationId}" })
    public AjaxResult getInfo(@PathVariable(value = "notificationId", required = false) Long notificationId)
    {
        AjaxResult ajax = AjaxResult.success();
        if (notificationId != null)
        {
            CrmNotification notification = crmNotificationService.selectCrmNotificationById(notificationId);
            ajax.put(AjaxResult.DATA_TAG, notification);
        }
        return ajax;
    }

    @PreAuthorize("@ss.hasPermi('crm:notification:add')")
    @Log(title = "通知管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CrmNotification notification)
    {
        return toAjax(crmNotificationService.insertCrmNotification(notification));
    }

    @PreAuthorize("@ss.hasPermi('crm:notification:edit')")
    @Log(title = "通知管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CrmNotification notification)
    {
        return toAjax(crmNotificationService.updateCrmNotification(notification));
    }

    @PreAuthorize("@ss.hasPermi('crm:notification:remove')")
    @Log(title = "通知管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{notificationIds}")
    public AjaxResult remove(@PathVariable Long[] notificationIds)
    {
        return toAjax(crmNotificationService.deleteCrmNotificationByIds(notificationIds));
    }

    @GetMapping("/unreadCount")
    public AjaxResult unreadCount()
    {
        Long userId = SecurityUtils.getUserId();
        int count = crmNotificationService.selectUnreadCountByUserId(userId);
        return success(count);
    }

    @PreAuthorize("@ss.hasPermi('crm:notification:edit')")
    @Log(title = "通知管理", businessType = BusinessType.UPDATE)
    @PutMapping("/markRead/{notificationId}")
    public AjaxResult markRead(@PathVariable Long notificationId)
    {
        return toAjax(crmNotificationService.markAsRead(notificationId));
    }
}
