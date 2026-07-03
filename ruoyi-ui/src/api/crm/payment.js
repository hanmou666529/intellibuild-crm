import request from '@/utils/request'
import { parseStrEmpty } from "@/utils/ruoyi";

export function listPayment(query) {
  return request({
    url: '/crm/payment/list',
    method: 'get',
    params: query
  })
}

export function getPayment(planId) {
  return request({
    url: '/crm/payment/' + parseStrEmpty(planId),
    method: 'get'
  })
}

export function addPayment(data) {
  return request({
    url: '/crm/payment',
    method: 'post',
    data: data
  })
}

export function updatePayment(data) {
  return request({
    url: '/crm/payment',
    method: 'put',
    data: data
  })
}

export function delPayment(planId) {
  return request({
    url: '/crm/payment/' + planId,
    method: 'delete'
  })
}

export function markPaid(planId, actualAmount, actualDate, paymentMethod) {
  const data = {
    planId,
    actualAmount,
    actualDate,
    paymentMethod
  }
  return request({
    url: '/crm/payment/markPaid',
    method: 'put',
    data: data
  })
}
