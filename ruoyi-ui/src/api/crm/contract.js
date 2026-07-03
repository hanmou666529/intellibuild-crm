import request from '@/utils/request'
import { parseStrEmpty } from "@/utils/ruoyi";

export function listContract(query) {
  return request({
    url: '/crm/contract/list',
    method: 'get',
    params: query
  })
}

export function getContract(contractId) {
  return request({
    url: '/crm/contract/' + parseStrEmpty(contractId),
    method: 'get'
  })
}

export function addContract(data) {
  return request({
    url: '/crm/contract',
    method: 'post',
    data: data
  })
}

export function updateContract(data) {
  return request({
    url: '/crm/contract',
    method: 'put',
    data: data
  })
}

export function delContract(contractId) {
  return request({
    url: '/crm/contract/' + contractId,
    method: 'delete'
  })
}

export function exportContract(query) {
  return request({
    url: '/crm/contract/export',
    method: 'post',
    params: query,
    responseType: 'blob'
  })
}

export function approveContract(contractId) {
  return request({
    url: '/crm/contract/approve/' + contractId,
    method: 'put'
  })
}

export function reSubmitApproval(contractId) {
  return request({
    url: '/crm/contract/reSubmitApproval/' + contractId,
    method: 'put'
  })
}

export function uploadContractAttachment(data) {
  return request({
    url: '/crm/contract/upload',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function downloadContractAttachment(fileName) {
  return request({
    url: '/crm/contract/download',
    method: 'get',
    params: { fileName: fileName },
    responseType: 'blob'
  })
}
