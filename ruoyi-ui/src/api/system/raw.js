import request from '@/utils/request'

// 查询招聘原始数据列表
export function listRaw(query) {
  return request({
    url: '/system/raw/list',
    method: 'get',
    params: query
  })
}

// 查询招聘原始数据详细
export function getRaw(id) {
  return request({
    url: '/system/raw/' + id,
    method: 'get'
  })
}

// 新增招聘原始数据
export function addRaw(data) {
  return request({
    url: '/system/raw',
    method: 'post',
    data: data
  })
}

// 修改招聘原始数据
export function updateRaw(data) {
  return request({
    url: '/system/raw',
    method: 'put',
    data: data
  })
}

// 删除招聘原始数据
export function delRaw(id) {
  return request({
    url: '/system/raw/' + id,
    method: 'delete'
  })
}
