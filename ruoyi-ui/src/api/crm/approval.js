import request from '@/utils/request'

export function listTemplate(query) {
  return request({ url: '/crm/approval/template/list', method: 'get', params: query })
}

export function getTemplate(templateId) {
  return request({ url: '/crm/approval/template/' + templateId, method: 'get' })
}

export function addTemplate(data) {
  return request({ url: '/crm/approval/template', method: 'post', data: data })
}

export function updateTemplate(data) {
  return request({ url: '/crm/approval/template', method: 'put', data: data })
}

export function delTemplate(templateIds) {
  return request({ url: '/crm/approval/template/' + templateIds, method: 'delete' })
}

export function listApproved(query) {
  return request({ url: '/crm/approval/processed/list', method: 'get', params: query })
}

export function listPending(query) {
  return request({ url: '/crm/approval/pending/list', method: 'get', params: query })
}

export function listMyApprovals(query) {
  return request({ url: '/crm/approval/my/list', method: 'get', params: query })
}

export function getApprovalRequest(requestId) {
  return request({ url: '/crm/approval/my/' + requestId, method: 'get' })
}

export function getApprovalNodes(requestId) {
  return request({ url: '/crm/approval/' + requestId + '/nodes', method: 'get' })
}

export function approveNode(nodeId, comment) {
  return request({ url: '/crm/approval/approve', method: 'put', params: { nodeId, comment } })
}

export function rejectNode(nodeId, comment) {
  return request({ url: '/crm/approval/reject', method: 'put', params: { nodeId, comment } })
}

export function cancelApproval(requestId) {
  return request({ url: '/crm/approval/cancel/' + requestId, method: 'put' })
}
