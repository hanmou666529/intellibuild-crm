<template>
  <div class="ai-copilot">
    <div class="copilot-float-btn" @click="openDrawer">
      <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M12 2L2 7l10 5 10-5-10-5z"/>
        <path d="M2 17l10 5 10-5"/>
        <path d="M2 12l10 5 10-5"/>
      </svg>
    </div>

    <el-drawer
      :visible.sync="drawerVisible"
      direction="rtl"
      size="480px"
      :with-header="false"
      :before-close="handleClose">
      <div class="copilot-container">
        <div class="copilot-header">
          <div class="copilot-title">AI 助手</div>
          <div class="copilot-actions">
            <el-button size="mini" type="text" @click="showSessions = !showSessions">
              {{ showSessions ? '隐藏会话' : '历史会话' }}
            </el-button>
            <el-button size="mini" type="text" @click="newSession">新建会话</el-button>
          </div>
        </div>

        <div class="copilot-body" :class="{ 'has-sessions': showSessions }">
          <div v-if="showSessions" class="session-list">
            <div v-for="s in sessions" :key="s.sessionId"
              class="session-item"
              :class="{ active: s.sessionId === currentSessionId }"
              @click="switchSession(s.sessionId)">
              <div class="session-title">{{ s.title }}</div>
              <div class="session-time">{{ formatTime(s.updateTime) }}</div>
            </div>
          </div>

          <div class="chat-area" ref="chatArea">
            <div v-if="messages.length === 0" class="chat-welcome">
              <div class="welcome-icon">🤖</div>
              <div class="welcome-text">你好！我是 AI 销售助理<br>有什么可以帮助你的？</div>
              <div class="quick-actions">
                <el-button size="small" plain @click="sendQuick('上个月成交了多少订单？')">📊 本月成交</el-button>
                <el-button size="small" plain @click="sendQuick('有哪些待跟进的客户？')">📋 待跟进</el-button>
                <el-button size="small" plain @click="sendQuick('本月销售数据怎么样？')">📈 销售概览</el-button>
              </div>
            </div>

            <div v-for="(msg, idx) in messages" :key="idx" class="message-wrapper" :class="msg.role">
              <div class="message-avatar">{{ msg.role === 'user' ? '我' : '🤖' }}</div>
              <div class="message-content">
                <div v-if="msg.role === 'tool'" class="tool-notice">
                  {{ msg.content }}
                </div>
                <div v-else-if="msg.role === 'error'" class="error-notice">
                  {{ msg.content }}
                </div>
                <div v-else v-html="renderMarkdown(msg.content)"></div>
              </div>
            </div>

            <div v-if="loading" class="message-wrapper assistant">
              <div class="message-avatar">🤖</div>
              <div class="message-content">
                <span class="typing-dots"><span>.</span><span>.</span><span>.</span></span>
              </div>
            </div>
          </div>
        </div>

        <div class="copilot-footer">
          <el-input
            ref="inputRef"
            v-model="inputMessage"
            type="textarea"
            :rows="2"
            placeholder="输入消息，Enter 发送..."
            @keydown.enter.native.prevent="sendMessage"
            :disabled="loading"/>
          <el-button type="primary" :disabled="!inputMessage.trim() || loading"
            @click="sendMessage" class="send-btn">发送</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { listSession, createSession, deleteSession, streamChat } from '@/api/crm/copilot'

export default {
  name: 'AiCopilot',
  data() {
    return {
      drawerVisible: false,
      showSessions: false,
      sessions: [],
      currentSessionId: null,
      messages: [],
      inputMessage: '',
      loading: false
    }
  },
  mounted() {
    document.addEventListener('keydown', this.handleGlobalShortcut)
  },
  beforeDestroy() {
    document.removeEventListener('keydown', this.handleGlobalShortcut)
  },
  methods: {
    renderMarkdown(text) {
      if (!text) return ''
      let html = text
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
      html = html
        .replace(/```(\w*)\n([\s\S]*?)```/g, '<pre><code>$2</code></pre>')
        .replace(/`([^`]+)`/g, '<code>$1</code>')
        .replace(/### (.+)/g, '<h3>$1</h3>')
        .replace(/## (.+)/g, '<h2>$1</h2>')
        .replace(/# (.+)/g, '<h1>$1</h1>')
        .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
        .replace(/\*(.+?)\*/g, '<em>$1</em>')
        .replace(/\[(.+?)\]\((.+?)\)/g, '<a href="$2" target="_blank">$1</a>')
        .replace(/^- (.+)/gm, '<li>$1</li>')
        .replace(/(<li>.*<\/li>)/s, '<ul>$1</ul>')
        .replace(/\n/g, '<br>')
      return html
    },

    openDrawer() {
      this.drawerVisible = true
      this.$nextTick(() => {
        this.scrollToBottom()
      })
    },

    handleClose(done) {
      done()
    },

    newSession() {
      createSession().then(res => {
        this.currentSessionId = res.data.sessionId
        this.messages = []
        this.loadSessions()
      })
    },

    switchSession(sessionId) {
      this.currentSessionId = sessionId
      this.messages = []
      this.showSessions = false
    },

    loadSessions() {
      listSession().then(res => {
        this.sessions = res.data || []
        if (!this.currentSessionId && this.sessions.length > 0) {
          this.currentSessionId = this.sessions[0].sessionId
        }
      })
    },

    sendQuick(text) {
      this.inputMessage = text
      this.sendMessage()
    },

    async sendMessage() {
      const message = this.inputMessage.trim()
      if (!message || this.loading) return
      this.inputMessage = ''
      this.loading = true

      if (!this.currentSessionId) {
        try {
          const res = await createSession()
          this.currentSessionId = res.data.sessionId
          this.loadSessions()
        } catch (e) {
          this.loading = false
          return
        }
      }

      this.messages.push({ role: 'user', content: message })

      this.$nextTick(() => this.scrollToBottom())

      const timeoutTimer = setTimeout(() => { this.loading = false }, 120000)

      await streamChat(this.currentSessionId, message, {
        onToken: (token) => {
          const last = this.messages[this.messages.length - 1]
          if (last && last.role === 'assistant') {
            last.content += token
          } else {
            this.messages.push({ role: 'assistant', content: token })
          }
          this.$nextTick(() => this.scrollToBottom())
        },
        onToolStart: (msg) => {
          this.messages.push({ role: 'tool', content: msg })
          this.$nextTick(() => this.scrollToBottom())
        },
        onDone: () => {
          clearTimeout(timeoutTimer)
          this.loading = false
          this.loadSessions()
          this.$nextTick(() => this.scrollToBottom())
        },
        onError: (msg) => {
          clearTimeout(timeoutTimer)
          this.messages.push({ role: 'error', content: msg })
          this.loading = false
          this.$nextTick(() => this.scrollToBottom())
        }
      })

      if (this.loading) {
        this.loading = false
      }
    },

    formatTime(dateStr) {
      if (!dateStr) return ''
      const d = new Date(dateStr)
      const now = new Date()
      const diff = now - d
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
      return (d.getMonth() + 1) + '/' + d.getDate()
    },

    scrollToBottom() {
      const el = this.$refs.chatArea
      if (el) {
        this.$nextTick(() => {
          el.scrollTop = el.scrollHeight
        })
      }
    },

    handleGlobalShortcut(e) {
      if (e.altKey && e.key === 'c') {
        this.drawerVisible = !this.drawerVisible
      }
    }
  }
}
</script>

<style scoped>
.copilot-float-btn {
  position: fixed;
  bottom: 32px;
  right: 32px;
  z-index: 9999;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: #409EFF;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(64,158,255,0.4);
  transition: transform 0.2s, box-shadow 0.2s;
}
.copilot-float-btn:hover {
  transform: scale(1.08);
  box-shadow: 0 6px 24px rgba(64,158,255,0.5);
}

.copilot-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f7fa;
}

.copilot-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  flex-shrink: 0;
}
.copilot-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.copilot-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}
.copilot-body.has-sessions .session-list {
  width: 220px;
  border-right: 1px solid #e4e7ed;
  overflow-y: auto;
  flex-shrink: 0;
}

.session-list {
  width: 0;
  overflow: hidden;
  transition: width 0.3s;
  background: #fff;
}
.session-item {
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #f2f3f5;
  transition: background 0.2s;
}
.session-item:hover, .session-item.active {
  background: #ecf5ff;
}
.session-title {
  font-size: 13px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.session-time {
  font-size: 11px;
  color: #909399;
  margin-top: 4px;
}

.chat-area {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
}

.chat-welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
}
.welcome-icon {
  font-size: 48px;
  margin-bottom: 12px;
}
.welcome-text {
  font-size: 15px;
  color: #606266;
  margin-bottom: 24px;
  line-height: 1.6;
}
.quick-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
}

.message-wrapper {
  display: flex;
  margin-bottom: 16px;
  align-items: flex-start;
}
.message-wrapper.user {
  flex-direction: row-reverse;
}
.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  flex-shrink: 0;
  margin: 0 8px;
}
.message-wrapper.user .message-avatar {
  background: #409EFF;
  color: #fff;
}
.message-wrapper.assistant .message-avatar {
  background: #e6f1ff;
}
.message-content {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}
.message-wrapper.user .message-content {
  background: #409EFF;
  color: #fff;
  border-bottom-right-radius: 4px;
}
.message-wrapper.assistant .message-content {
  background: #fff;
  color: #303133;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.tool-notice {
  color: #909399;
  font-style: italic;
  font-size: 13px;
}
.error-notice {
  color: #F56C6C;
  font-size: 13px;
}

.typing-dots span {
  display: inline-block;
  animation: blink 1.4s infinite both;
  font-size: 24px;
  line-height: 1;
}
.typing-dots span:nth-child(2) { animation-delay: 0.2s; }
.typing-dots span:nth-child(3) { animation-delay: 0.4s; }
@keyframes blink {
  0%, 80%, 100% { opacity: 0; }
  40% { opacity: 1; }
}

.copilot-footer {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  padding: 12px 16px;
  background: #fff;
  border-top: 1px solid #e4e7ed;
  flex-shrink: 0;
}
.copilot-footer .el-input {
  flex: 1;
}
.send-btn {
  height: 36px;
  flex-shrink: 0;
}
</style>
