import request from '@/utils/request'

export function listPool(query) {
  return request({
    url: '/crm/pool/list',
    method: 'get',
    params: query
  })
}

export function listPoolLog(query) {
  return request({
    url: '/crm/pool/logList',
    method: 'get',
    params: query
  })
}
