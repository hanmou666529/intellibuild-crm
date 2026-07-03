package com.ruoyi.crm.service;

import java.util.List;
import com.ruoyi.crm.domain.CrmNotification;

public interface ICrmNotificationService
{
    public List<CrmNotification> selectCrmNotificationList(CrmNotification notification);

    public CrmNotification selectCrmNotificationById(Long notificationId);

    public int insertCrmNotification(CrmNotification notification);

    public int updateCrmNotification(CrmNotification notification);

    public int deleteCrmNotificationByIds(Long[] notificationIds);

    public int selectUnreadCountByUserId(Long userId);

    public int markAsRead(Long notificationId);

    public void createNotification(CrmNotification notification);
}
