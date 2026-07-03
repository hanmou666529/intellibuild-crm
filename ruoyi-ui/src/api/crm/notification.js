import request from '@/utils/request'
import { parseStrEmpty } from "@/utils/ruoyi";

export function listNotification(query) {
  return request({
    url: '/crm/notification/list',
    method: 'get',
    params: query
  })
}

export function getNotification(notificationId) {
  return request({
    url: '/crm/notification/' + parseStrEmpty(notificationId),
    method: 'get'
  })
}

export function unreadCount() {
  return request({
    url: '/crm/notification/unreadCount',
    method: 'get'
  })
}

export function markRead(notificationId) {
  return request({
    url: '/crm/notification/markRead/' + notificationId,
    method: 'put'
  })
}
