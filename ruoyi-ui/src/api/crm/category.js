import request from '@/utils/request'
import { parseStrEmpty } from "@/utils/ruoyi";

export function listCategory(query) {
  return request({
    url: '/crm/category/list',
    method: 'get',
    params: query
  })
}

export function getCategory(categoryId) {
  return request({
    url: '/crm/category/' + parseStrEmpty(categoryId),
    method: 'get'
  })
}

export function addCategory(data) {
  return request({
    url: '/crm/category',
    method: 'post',
    data: data
  })
}

export function updateCategory(data) {
  return request({
    url: '/crm/category',
    method: 'put',
    data: data
  })
}

export function delCategory(categoryId) {
  return request({
    url: '/crm/category/' + categoryId,
    method: 'delete'
  })
}

export function listCategoryTree() {
  return request({
    url: '/crm/category/treeList',
    method: 'get'
  })
}
