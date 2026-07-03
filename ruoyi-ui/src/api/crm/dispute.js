import request from '@/utils/request'

export function listDispute(query) {
  return request({
    url: '/crm/dispute/list',
    method: 'get',
    params: query
  })
}

export function getDispute(disputeId) {
  return request({
    url: '/crm/dispute/' + disputeId,
    method: 'get'
  })
}

export function addDispute(data) {
  return request({
    url: '/crm/dispute/add',
    method: 'post',
    data: data
  })
}

export function handleDispute(data) {
  return request({
    url: '/crm/dispute/handle',
    method: 'put',
    data: data
  })
}

export function arbitrateDispute(data) {
  return request({
    url: '/crm/dispute/arbitrate',
    method: 'put',
    data: data
  })
}
