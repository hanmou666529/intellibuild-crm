import request from '@/utils/request'
import { parseStrEmpty } from "@/utils/ruoyi";

export function listProduct(query) {
  return request({
    url: '/crm/product/list',
    method: 'get',
    params: query
  })
}

export function getProduct(productId) {
  return request({
    url: '/crm/product/' + parseStrEmpty(productId),
    method: 'get'
  })
}

export function addProduct(data) {
  return request({
    url: '/crm/product',
    method: 'post',
    data: data
  })
}

export function updateProduct(data) {
  return request({
    url: '/crm/product',
    method: 'put',
    data: data
  })
}

export function delProduct(productId) {
  return request({
    url: '/crm/product/' + productId,
    method: 'delete'
  })
}

export function exportProduct(query) {
  return request({
    url: '/crm/product/export',
    method: 'post',
    params: query,
    responseType: 'blob'
  })
}
