import request from '@/utils/request'

export function listAudit(query) {
  return request({ url: '/crm/audit/list', method: 'get', params: query })
}

export function delAudit(operIds) {
  return request({ url: '/system/operlog/' + operIds, method: 'delete' })
}
