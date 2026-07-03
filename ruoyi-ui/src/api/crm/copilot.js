import request from '@/utils/request'
import { getToken } from '@/utils/auth'

export function listSession() {
  return request({
    url: '/crm/ai/copilot/session/list',
    method: 'get'
  })
}

export function createSession() {
  return request({
    url: '/crm/ai/copilot/session/create',
    method: 'post'
  })
}

export function deleteSession(sessionId) {
  return request({
    url: '/crm/ai/copilot/session/' + sessionId,
    method: 'delete'
  })
}

export async function streamChat(sessionId, message, callbacks) {
  const token = getToken()
  const base = process.env.VUE_APP_BASE_API || ''
  const url = `${base}/crm/ai/copilot/chat/stream?sessionId=${encodeURIComponent(sessionId)}&message=${encodeURIComponent(message)}`

  let response
  try {
    response = await fetch(url, {
      headers: { 'Authorization': 'Bearer ' + token }
    })
  } catch (e) {
    callbacks.onError('网络连接失败: ' + e.message)
    return
  }

  if (!response.ok) {
    callbacks.onError('请求失败: HTTP ' + response.status)
    return
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''

  try {
    while (true) {
      const { done, value } = await reader.read()
      if (done) {
        if (buffer.trim()) {
          processLine(buffer, callbacks)
        }
        break
      }
      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''
      for (const line of lines) {
        processLine(line, callbacks)
      }
    }
  } catch (e) {
    if (e.name !== 'AbortError') {
      callbacks.onError('读取流失败: ' + e.message)
    }
  }
}

function processLine(line, callbacks) {
  const trimmed = line.trim()
  if (!trimmed) return
  if (trimmed.startsWith('data:')) {
    const data = trimmed.substring(5).trim()
    if (data === '[DONE]') {
      callbacks.onDone()
      return
    }
    try {
      const parsed = JSON.parse(data)
      if (parsed.type === 'token') {
        callbacks.onToken(parsed.content)
      } else if (parsed.type === 'done') {
        callbacks.onDone()
      } else if (parsed.type === 'error') {
        callbacks.onError(parsed.content)
      } else if (parsed.type === 'tool_start') {
        callbacks.onToolStart(parsed.content)
      }
    } catch (e) {}
  }
}
