import request from '@/utils/request'
import { parseStrEmpty } from "@/utils/ruoyi";

export function listCustomer(query) {
  return request({
    url: '/crm/customer/list',
    method: 'get',
    params: query
  })
}

export function getCustomer(customerId) {
  return request({
    url: '/crm/customer/' + parseStrEmpty(customerId),
    method: 'get'
  })
}

export function addCustomer(data) {
  return request({
    url: '/crm/customer',
    method: 'post',
    data: data
  })
}

export function updateCustomer(data) {
  return request({
    url: '/crm/customer',
    method: 'put',
    data: data
  })
}

export function delCustomer(customerId) {
  return request({
    url: '/crm/customer/' + customerId,
    method: 'delete'
  })
}

export function exportCustomer(query) {
  return request({
    url: '/crm/customer/export',
    method: 'post',
    params: query,
    responseType: 'blob'
  })
}

export function importCustomer(data) {
  return request({
    url: '/crm/customer/importData',
    method: 'post',
    headers: { 'Content-Type': 'multipart/form-data' },
    data: data
  })
}

export function importTemplate() {
  return request({
    url: '/crm/customer/importTemplate',
    method: 'get',
    responseType: 'blob'
  })
}

export function assignCustomer(customerId, userId) {
  const data = {
    customerId,
    belongUserId: userId
  }
  return request({
    url: '/crm/customer/assign',
    method: 'put',
    data: data
  })
}

export function assignCheck(customerId) {
  return request({
    url: '/crm/customer/assignCheck/' + customerId,
    method: 'get'
  })
}

export function putToPool(customerId, reason) {
  const data = {
    customerId,
    remark: reason
  }
  return request({
    url: '/crm/customer/pool',
    method: 'put',
    data: data
  })
}

export function claimFromPool(customerId) {
  return request({
    url: '/crm/customer/claim',
    method: 'put',
    data: { customerId }
  })
}

export function checkPhoneUnique(phone) {
  return request({
    url: '/crm/customer/checkPhoneUnique',
    method: 'get',
    params: { phone }
  })
}

export function addToPipeline(customerId) {
  return request({
    url: '/crm/customer/' + customerId + '/pipeline',
    method: 'post'
  })
}

export function batchAssignCustomers(data) {
  return request({
    url: '/crm/customer/batchAssign',
    method: 'post',
    data: data
  })
}

export function batchAssignCheck(data) {
  return request({
    url: '/crm/customer/batchAssignCheck',
    method: 'post',
    data: data
  })
}

export function shareToUser(data) {
  return request({
    url: '/crm/share/toUser',
    method: 'post',
    data: data
  })
}

export function shareToDept(data) {
  return request({
    url: '/crm/share/toDept',
    method: 'post',
    data: data
  })
}

export function getShares(customerId) {
  return request({
    url: '/crm/share/list/' + customerId,
    method: 'get'
  })
}

export function deleteShare(shareId) {
  return request({
    url: '/crm/share/' + shareId,
    method: 'delete'
  })
}

export function getCrmUsers(roleKey) {
  return request({
    url: '/crm/common/users',
    method: 'get',
    params: { roleKey: roleKey || undefined }
  })
}

export function getCrmDepts() {
  return request({
    url: '/crm/common/depts',
    method: 'get'
  })
}

export function getCrmShareDepts() {
  return request({
    url: '/crm/common/shareDepts',
    method: 'get'
  })
}

export function checkDuplicate(query) {
  return request({
    url: '/crm/customer/checkDuplicate',
    method: 'get',
    params: query
  })
}

export function mergeCustomer(data) {
  return request({
    url: '/crm/customer/merge',
    method: 'post',
    data: data
  })
}

export function getPendingDeptShares() {
  return request({
    url: '/crm/share/pending',
    method: 'get'
  })
}

export function assignFromDept(data) {
  return request({
    url: '/crm/share/assignFromDept',
    method: 'post',
    data: data
  })
}
