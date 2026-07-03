import request from '@/utils/request'

export function listTag(query) {
  return request({ url: '/crm/tag/list', method: 'get', params: query })
}

export function listActiveTag() {
  return request({ url: '/crm/tag/active', method: 'get' })
}

export function getTag(tagId) {
  return request({ url: '/crm/tag/' + tagId, method: 'get' })
}

export function addTag(data) {
  return request({ url: '/crm/tag', method: 'post', data: data })
}

export function updateTag(data) {
  return request({ url: '/crm/tag', method: 'put', data: data })
}

export function delTag(tagIds) {
  return request({ url: '/crm/tag/' + tagIds, method: 'delete' })
}
