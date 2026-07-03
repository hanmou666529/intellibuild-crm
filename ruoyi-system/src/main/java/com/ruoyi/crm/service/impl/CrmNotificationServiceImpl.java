package com.ruoyi.crm.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.crm.domain.CrmNotification;
import com.ruoyi.crm.mapper.CrmNotificationMapper;
import com.ruoyi.crm.service.ICrmNotificationService;

@Service
public class CrmNotificationServiceImpl implements ICrmNotificationService
{
    @Autowired
    private CrmNotificationMapper crmNotificationMapper;

    @Override
    public List<CrmNotification> selectCrmNotificationList(CrmNotification notification)
    {
        return crmNotificationMapper.selectCrmNotificationList(notification);
    }

    @Override
    public CrmNotification selectCrmNotificationById(Long notificationId)
    {
        return crmNotificationMapper.selectCrmNotificationById(notificationId);
    }

    @Override
    @Transactional
    public int insertCrmNotification(CrmNotification notification)
    {
        notification.setCreateBy(SecurityUtils.getUsername());
        notification.setCreateTime(DateUtils.getNowDate());
        return crmNotificationMapper.insertCrmNotification(notification);
    }

    @Override
    @Transactional
    public int updateCrmNotification(CrmNotification notification)
    {
        notification.setUpdateBy(SecurityUtils.getUsername());
        notification.setUpdateTime(DateUtils.getNowDate());
        return crmNotificationMapper.updateCrmNotification(notification);
    }

    @Override
    @Transactional
    public int deleteCrmNotificationByIds(Long[] notificationIds)
    {
        return crmNotificationMapper.deleteCrmNotificationByIds(notificationIds);
    }

    @Override
    public int selectUnreadCountByUserId(Long userId)
    {
        return crmNotificationMapper.selectUnreadCountByUserId(userId);
    }

    @Override
    @Transactional
    public int markAsRead(Long notificationId)
    {
        CrmNotification notification = new CrmNotification();
        notification.setNotificationId(notificationId);
        return crmNotificationMapper.markAsRead(notification);
    }

    @Override
    @Transactional
    public void createNotification(CrmNotification notification)
    {
        notification.setIsRead("0");
        notification.setCreateBy(SecurityUtils.getUsername());
        notification.setCreateTime(DateUtils.getNowDate());
        crmNotificationMapper.insertCrmNotification(notification);
    }
}
