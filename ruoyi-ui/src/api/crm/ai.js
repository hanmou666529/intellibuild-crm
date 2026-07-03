import request from '@/utils/request'

export function customerIntel(data) {
  return request({
    url: '/crm/ai/customer/intel',
    method: 'post',
    data
  })
}

export function followupGenerate(data) {
  return request({
    url: '/crm/ai/followup/generate',
    method: 'post',
    data
  })
}

export function runScoring() {
  return request({
    url: '/crm/ai/scoring/run',
    method: 'post'
  })
}

export function reportGenerate(data) {
  return request({
    url: '/crm/ai/report/generate',
    method: 'post',
    data
  })
}

export function contractReview(data) {
  return request({
    url: '/crm/ai/contract/review',
    method: 'post',
    data
  })
}

export function listPrompt(query) {
  return request({
    url: '/crm/ai/prompt/list',
    method: 'get',
    params: query
  })
}

export function getPrompt(id) {
  return request({
    url: '/crm/ai/prompt/' + id,
    method: 'get'
  })
}

export function addPrompt(data) {
  return request({
    url: '/crm/ai/prompt',
    method: 'post',
    data
  })
}

export function updatePrompt(data) {
  return request({
    url: '/crm/ai/prompt',
    method: 'put',
    data
  })
}

export function delPrompt(ids) {
  return request({
    url: '/crm/ai/prompt/' + ids,
    method: 'delete'
  })
}
