package com.ruoyi.crm.mapper;

import java.util.List;
import com.ruoyi.crm.domain.CrmNotification;

public interface CrmNotificationMapper
{
    public List<CrmNotification> selectCrmNotificationList(CrmNotification notification);

    public CrmNotification selectCrmNotificationById(Long notificationId);

    public int insertCrmNotification(CrmNotification notification);

    public int updateCrmNotification(CrmNotification notification);

    public int deleteCrmNotificationByIds(Long[] notificationIds);

    public int selectUnreadCountByUserId(Long userId);

    public int markAsRead(CrmNotification notification);
}
