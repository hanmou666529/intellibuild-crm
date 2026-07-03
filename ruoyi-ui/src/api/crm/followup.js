import request from '@/utils/request'
import { parseStrEmpty } from "@/utils/ruoyi";

export function listFollowup(query) {
  return request({
    url: '/crm/followup/list',
    method: 'get',
    params: query
  })
}

export function getFollowup(followupId) {
  return request({
    url: '/crm/followup/' + parseStrEmpty(followupId),
    method: 'get'
  })
}

export function addFollowup(data) {
  return request({
    url: '/crm/followup',
    method: 'post',
    data: data
  })
}

export function updateFollowup(data) {
  return request({
    url: '/crm/followup',
    method: 'put',
    data: data
  })
}

export function delFollowup(followupId) {
  return request({
    url: '/crm/followup/' + followupId,
    method: 'delete'
  })
}

export function listFollowupByCustomer(customerId) {
  return request({
    url: '/crm/followup/byCustomer/' + customerId,
    method: 'get'
  })
}
