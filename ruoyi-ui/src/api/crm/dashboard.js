import request from '@/utils/request'

export function getStats() {
  return request({
    url: '/crm/dashboard/stats',
    method: 'get'
  })
}

export function drillDown(bizType, key) {
  return request({
    url: '/crm/dashboard/drillDown',
    method: 'get',
    params: { bizType, key }
  })
}
