import request from '@/utils/request'
import { parseStrEmpty } from "@/utils/ruoyi";

export function listOrder(query) {
  return request({
    url: '/crm/order/list',
    method: 'get',
    params: query
  })
}

export function getOrder(orderId) {
  return request({
    url: '/crm/order/' + parseStrEmpty(orderId),
    method: 'get'
  })
}

export function addOrder(data) {
  return request({
    url: '/crm/order',
    method: 'post',
    data: data
  })
}

export function updateOrder(data) {
  return request({
    url: '/crm/order',
    method: 'put',
    data: data
  })
}

export function delOrder(orderId) {
  return request({
    url: '/crm/order/' + orderId,
    method: 'delete'
  })
}

export function exportOrder(query) {
  return request({
    url: '/crm/order/export',
    method: 'post',
    params: query,
    responseType: 'blob'
  })
}

export function markOrderPaid(data) {
  return request({
    url: '/crm/order/markPaid',
    method: 'put',
    data: data
  })
}
