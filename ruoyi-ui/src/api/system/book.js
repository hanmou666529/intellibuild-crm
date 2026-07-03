import request from '@/utils/request'

// 查询图书列表
export function listBook(query) {
  return request({
    url: '/system/book/list',
    method: 'get',
    params: query
  })
}

// 查询图书详细
export function getBook(bookId) {
  return request({
    url: '/system/book/' + bookId,
    method: 'get'
  })
}

// 新增图书
export function addBook(data) {
  return request({
    url: '/system/book',
    method: 'post',
    data: data
  })
}

// 修改图书
export function updateBook(data) {
  return request({
    url: '/system/book',
    method: 'put',
    data: data
  })
}

// 删除图书
export function delBook(bookId) {
  return request({
    url: '/system/book/' + bookId,
    method: 'delete'
  })
}
