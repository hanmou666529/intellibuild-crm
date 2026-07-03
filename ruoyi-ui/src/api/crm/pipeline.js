import request from '@/utils/request'
import { parseStrEmpty } from "@/utils/ruoyi";

export function listPipeline(query) {
  return request({
    url: '/crm/pipeline/list',
    method: 'get',
    params: query
  })
}

export function getPipeline(pipelineId) {
  return request({
    url: '/crm/pipeline/' + parseStrEmpty(pipelineId),
    method: 'get'
  })
}

export function addPipeline(data) {
  return request({
    url: '/crm/pipeline',
    method: 'post',
    data: data
  })
}

export function updatePipeline(data) {
  return request({
    url: '/crm/pipeline',
    method: 'put',
    data: data
  })
}

export function delPipeline(pipelineId) {
  return request({
    url: '/crm/pipeline/' + pipelineId,
    method: 'delete'
  })
}

export function updateStage(pipelineId, stage) {
  const data = {
    pipelineId,
    stage
  }
  return request({
    url: '/crm/pipeline/stage',
    method: 'put',
    data: data
  })
}
