# 智驭CRM AI 助手 — 设计规格 v3（最终版）

## 1. 概述

在 CRM 中嵌入通用 AI 助手（Copilot），支持多轮对话 + CRM 数据查询。用户从任何页面点击右下角悬浮按钮调出 AI 面板，通过自然语言提问获取业务数据。

## 2. 技术选型

| 项 | 选择 | 理由 |
|----|------|------|
| LLM | GLM-4-Flash（智谱AI） | 国内直连，免费额度，Function Calling 成熟 |
| 通信方式 | SSE（SseEmitter） | Spring Boot 原生，无需额外依赖 |
| HTTP 客户端 | Hutool HttpUtil（已引入） | 0 新依赖 |
| JSON 处理 | Hutool JSONUtil（已引入） | 0 新依赖 |
| 会话存储 | MySQL tb_ai_session | 持久化，与业务库统一 |
| 前端 UI | Vue 2 + Element UI（已引入） | 与项目现有技术栈一致 |
| 前端 Markdown | marked.js + highlight.js（CDN） | 轻量，无需构建工具 |

## 3. 模块一：AiService（GLM API 调用）

### 3.1 接口定义

```java
public interface AiService {
    AiChatResponse chat(AiChatRequest request);                              // 非流式（工具循环内部用）
    void chatStream(AiChatRequest request, SseEmitter emitter);              // 流式（对外暴露）
}
```

### 3.2 请求/响应 DTO

```java
public class AiChatRequest {
    private String sessionId;
    private String message;
    private boolean stream;
}

public class AiChatResponse {
    private String content;
    private String sessionId;
}
```

### 3.3 配置

```yaml
ai:
  glm:
    api-key: ${GLM_API_KEY}       # 环境变量，不写死
    model: glm-4-flash
    temperature: 0.7
    max-tokens: 2048
```

`AiProperties.java` 读取以上配置。

### 3.4 核心调用流程

```
chatStream():
 1. 从 AiSessionManager 获取完整上下文（含 system prompt + history）
 2. 从 ToolExecutor 获取所有 tool 定义
 3. 发起 SSE POST 到 https://open.bigmodel.cn/api/paas/v4/chat/completions
 4. 逐行解析 SSE chunk：
    a. delta.content 非空 → emitter.send(token)
    b. delta.tool_calls 非空 → 收集到当前消息的 tool_calls[]
 5. 收到 finish_reason 后：
    a. "stop" → emitter.send(done) → 结束
    b. "tool_calls" → 进入工具循环
 6. 工具循环（最多 5 轮）：
    a. 收集本轮所有 tool_calls（GLM 可能返回多个并行调用）
    b. 逐个顺序执行 ToolExecutor.execute(toolName, args, user)
    c. 将结果作为 role:"tool" 追加到上下文
    d. 发起非流式二次请求让 GLM 基于结果生成回复
    e. 将最终回复通过 emitter.send(token) 逐 chunk 推送
 7. 超时/异常 → emitter.completeWithError()
```

### 3.5 SSE 数据格式

```
event: message
data: {"type":"token","content":"根据"}

event: message
data: {"type":"token","content":"查询结果"}

event: message
data: {"type":"done","sessionId":"abc123"}
```

SseEmitter 超时设为 300 秒。

### 3.6 日志策略

| 级别 | 内容 | 说明 |
|------|------|------|
| INFO | `用户[ID] 发起AI对话: "{前200字}..."` | 每次用户输入 |
| INFO | `工具调用: query_customers, 耗时 320ms` | 每次工具执行 |
| DEBUG | `[AiService] GLM request: {完整请求体}` | API 调用调试（**过滤 api-key**） |
| DEBUG | `[AiService] GLM response: {完整响应体}` | API 返回调试 |
| WARN | `GLM API 超时，重试第1次` | 异常时 |
| WARN | `工具[query_customers]执行异常: ...` | 工具异常时 |

## 4. 模块二：Function Calling 框架

### 4.1 ToolExecutor（工具执行器）

```java
@Component
public class ToolExecutor {
    private final Map<String, AiToolHandler> handlers = new HashMap<>();

    public ToolExecutor(List<AiToolHandler> handlerList) {
        for (AiToolHandler h : handlerList) {
            handlers.put(h.getName(), h);
        }
    }

    public String execute(String name, String argsJson, Account user) {
        AiToolHandler h = handlers.get(name);
        if (h == null) return "错误：未知工具 [" + name + "]";
        return h.execute(argsJson, user);
    }

    public List<Map<String, Object>> getToolDefinitions() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (AiToolHandler h : handlers.values()) {
            list.add(h.getToolDefinition());
        }
        return list;
    }
}
```

### 4.2 AiToolHandler 接口

```java
public interface AiToolHandler {
    String getName();
    String getDescription();
    Map<String, Object> getParameters();        // JSON Schema
    String execute(String argumentsJson, Account user);

    default List<String> getRequiredParams() {
        return Collections.emptyList();
    }

    default Map<String, Object> getToolDefinition() {
        Map<String, Object> func = new HashMap<>();
        func.put("name", getName());
        func.put("description", getDescription());

        Map<String, Object> params = new HashMap<>();
        params.put("type", "object");
        params.put("properties", getParameters());

        // ★ null 安全：getRequiredParams() 可能返回 null
        List<String> required = getRequiredParams();
        if (required != null && !required.isEmpty()) {
            params.put("required", required);
        }

        func.put("parameters", params);

        Map<String, Object> tool = new HashMap<>();
        tool.put("type", "function");
        tool.put("function", func);
        return tool;
    }
}
```

### 4.3 一期工具清单（7 个）

| 工具名 | Handler 类 | 对应 Service | RBAC 过滤规则 | 说明 |
|--------|-----------|-------------|-------------|------|
| `query_customers` | QueryCustomerTool | UserService | owner_id → 同部门 → 管理员全量 | 按姓名/手机/状态查客户 |
| `query_orders` | QueryOrderTool | SalesOrderService | 同上 | 按日期/客户/状态查订单 |
| `query_opportunities` | QueryOpportunityTool | SalesOpportunityService | 同上 | 按阶段/客户查商机 |
| `query_follow_ups` | QueryFollowUpTool | CustomerFollowService | 同上 | 按客户查跟进记录 |
| `query_products` | QueryProductTool | ProductService | 全量可见 | 按名称/分类查产品 |
| `query_receipts` | QueryReceiptTool | ReceiptPlanService | owner_id 过滤 | 按客户/状态查回款 |
| `get_statistics` | QueryStatisticsTool | ReportService | owner_id 过滤 | 客户数/订单额/成交率 |

### 4.4 工具实现示例

```java
@Component
public class QueryCustomerTool implements AiToolHandler {
    private final UserService userService;

    public QueryCustomerTool(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getName() { return "query_customers"; }

    @Override
    public String getDescription() { return "按条件查询客户列表，支持客户名/手机号/状态筛选"; }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> props = new HashMap<>();
        props.put("name", param("string", "客户名称（模糊匹配）"));
        props.put("phone", param("string", "客户手机号"));
        props.put("status", param("string", "跟进状态", new String[]{"已成交","跟进中","待跟进","已流失"}));
        props.put("page", param("integer", "页码，默认1"));
        props.put("pageSize", param("integer", "每页条数，默认10"));
        return props;
    }

    @Override
    public String execute(String argsJson, Account user) {
        UserSearchDto dto = JSONUtil.toBean(argsJson, UserSearchDto.class);
        dto.setOwnerId(user.getId());
        PageInfo<User> result = userService.searchUsers(dto);

        List<User> list = result.getList();
        // 数据截断：超过 10 条附说明
        String data = JSONUtil.toJsonPrettyStr(list.size() > 10 ? list.subList(0, 10) : list);
        if (list.size() > 10) {
            data += "\n（共 " + list.size() + " 条，仅展示前 10 条）";
        }
        return data;
    }
}
```

```java
// 参数构造辅助方法
default Map<String, Object> param(String type, String description) {
    Map<String, Object> p = new HashMap<>();
    p.put("type", type);
    p.put("description", description);
    return p;
}
default Map<String, Object> param(String type, String description, String[] enumValues) {
    Map<String, Object> p = param(type, description);
    p.put("enum", enumValues);
    return p;
}
```

### 4.5 RBAC 数据安全

- 工具方法只负责传参，**不拼接 SQL**，所有查询走现有 Service 层
- Service 层已有权限过滤逻辑（按 owner_id → 同部门可见 → 管理员全量）
- 工具执行时通过 `Account user` 参数注入当前登录用户身份
- **禁止** AI 工具返回其他用户的业务数据

## 5. 模块三：Session 会话管理

### 5.1 表结构

```sql
CREATE TABLE tb_ai_session (
    session_id   VARCHAR(64)  PRIMARY KEY COMMENT 'UUID',
    account_id   INT          NOT NULL COMMENT '用户ID',
    title        VARCHAR(200) DEFAULT '新对话' COMMENT '自动生成：取第一句前20字',
    context      MEDIUMTEXT   COMMENT '完整消息上下文 JSON 数组',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_account (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI助手会话表';
```

`context` 字段存储格式示例：

```json
[
  {"role": "system", "content": "你是「智驭CRM」的AI助手..."},
  {"role": "user", "content": "上个月成交了多少订单？"},
  {"role": "assistant", "content": "上个月共成交 23 笔订单..."},
  {"role": "tool", "tool_call_id": "call_xxx", "content": "[{\"orderId\":...}]"},
  {"role": "tool", "tool_call_id": "call_yyy", "content": "[{\"name\":...}]"}
]
```

### 5.2 AiSessionManager 核心方法

```java
@Service
public class AiSessionManager {

    public AiSession createSession(Integer accountId)                       // 新建
    public List<AiSession> getSessions(Integer accountId)                   // 列表（按更新时间降序）
    public void deleteSession(String sessionId, Integer accountId)          // 删除（校验所属用户）
    public void appendMessages(String sessionId, Message... msgs)           // 追加消息
    public List<Map<String, Object>> getContext(String sessionId, Account user) // 构建完整上下文
    public void truncateIfExceeds(String sessionId)                         // 超长截断
    public String generateTitle(String firstMsg)                            // 取前20字去特殊字符
}
```

### 5.3 上下文构建流程

```
getContext(sessionId, user):
 1. 从 DB 读出 context（JSON 字符串）
 2. 反序列化为 List<Map<String, Object>>
 3. 在最前插入 system message:
    {
      "role": "system",
      "content": "你是「智驭CRM」的AI助手。当前用户：{user.realName}，部门：{user.deptName}，时间：{now}。你可以查询客户、订单、商机、产品等数据。请用中文回答。"
    }
 4. 若总消息轮数超过 20 轮（system 不计）:
    - 保留 system
    - 保留最近 20 轮消息
 5. 返回完整列表
```

### 5.4 清理策略

```sql
-- 定时任务，每天凌晨 3:00 执行
DELETE FROM tb_ai_session WHERE update_time < NOW() - INTERVAL 30 DAY;
```

## 6. 模块四：前端 AI 面板

### 6.1 集成方式

在 `main.html` 中挂载独立 Vue 实例：

```html
<div id="aiApp">
  <ai-chat-panel :visible.sync="panelVisible" :user="currentUser"></ai-chat-panel>
  <el-button class="ai-float-btn" icon="el-icon-s-promotion" circle
             @click="panelVisible = !panelVisible"></el-button>
</div>

<script src="/resources/js/ai/ai-chat.js"></script>
```

**关键：** `#aiApp` 是独立容器，`new Vue({ el: '#aiApp' })` 与页面内其他 Vue 实例互不干扰。面板展开/收起状态存 `sessionStorage`，切换页面后保持。

### 6.2 布局结构

```
右下角 fixed 悬浮按钮（圆形，AI 图标）
  ↓ 点击
右侧滑出抽屉面板（420px × 100vh, z-index: 9999）
  ├── 顶部栏：「AI 助手」+ 关闭按钮
  │
  ├── 会话列表（折叠式，点击切换）:
  │   ├── ✦ 上月成交分析    ← 当前会话高亮
  │   ├── ○ 客户A跟进情况
  │   └── ○ 产品报价查询
  │   └── [+ 新建会话]
  │
  ├── 对话区（flex:1, overflow-y: auto）:
  │   ├── AI 消息（左侧, 含头像 + Markdown 内容）
  │   ├── 用户消息（右侧）
  │   ├── 工具调用提示（折叠，显示 🔍 查询中...）
  │   ├── 流式打字指示器（三个跳动 ●）
  │   └── 错误/重试提示条
  │
  └── 输入区:
      ├── <el-input type="textarea" :rows="2" placeholder="输入问题..."/>
      ├── <el-button type="primary" :loading="streaming">发送</el-button>
      └── 快捷指令标签: 📊本月成交 | 📋待跟进客户 | 🔻销售漏斗
```

### 6.3 前端状态机

```
IDLE ──► 用户点击发送
  │
  ├─► SENDING (按钮 loading, 输入框禁用)
  │     │
  │     ├─ SSE 正常接收数据 ──► STREAMING
  │     │     ├─ 逐 token 追加到当前 AI 消息气泡
  │     │     └─ 收到 done ──► IDLE
  │     │
  │     ├─ SSE 30s 无响应 ──► THINKING
  │     │     └─ 显示「AI 正在思考，请稍候...」
  │     │         └─ 60s 无响应 ──► TIMEOUT
  │     │               └─ 显示「连接超时」+ 重试按钮
  │     │
  │     └─ 网络错误 ──► ERROR
  │           └─ 显示「连接断开」+ 重试按钮
  │
  └─ SSE 连接异常 ──► ERROR ──► IDLE（点击重试回到 SENDING）
```

### 6.4 SSE 前端处理

```javascript
function startSSE(sessionId, message) {
    const url = '/ai/chat/stream?sessionId=' + sessionId
              + '&message=' + encodeURIComponent(message);
    const source = new EventSource(url);
    let content = '';

    source.onmessage = function(e) {
        var data = JSON.parse(e.data);
        switch (data.type) {
            case 'token':
                content += data.content;
                updateAIMessage(content);
                break;
            case 'done':
                source.close();
                finalizeAIMessage();
                break;
            case 'error':
                source.close();
                showError(data.message);
                break;
        }
    };

    source.onerror = function() {
        source.close();
        showRetryButton();
    };

    return source;
}
```

### 6.5 Markdown 渲染

```html
<!-- main.html 引入 CDN -->
<script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
<script src="https://cdn.jsdelivr.net/gh/highlightjs/cdn-release/build/highlight.min.js"></script>
```

```javascript
function renderMarkdown(text) {
    return marked.parse(text || '', {
        breaks: true,
        gfm: true,
        highlight: function(code, lang) {
            try {
                return hljs.highlightAuto(code).value;
            } catch(e) {
                return code;
            }
        }
    });
}
```

使用 `DOMPurify.sanitize()` 或手动过滤 `<script>` 标签防止 XSS。

## 7. API 端点汇总

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| POST | `/ai/chat` | session | 非流式对话（内部工具循环使用） |
| GET | `/ai/chat/stream` | session | SSE 流式对话 |
| POST | `/ai/session/create` | session | 新建会话 |
| GET | `/ai/session/list` | session | 获取会话列表 |
| DELETE | `/ai/session/{sessionId}` | session | 删除会话（校验所属用户） |

所有端点复用 `AccountController` 现有的 session 鉴权机制，未登录返回 401。

## 8. 集成测试策略

使用 **okhttp3 mockwebserver** 模拟 GLM API，覆盖三类场景：

| 测试场景 | 核心验证点 |
|----------|-----------|
| **正常流式对话** | SSE 逐 token 推送，最终收到 done |
| **工具调用循环** | mock 返回 tool_calls → 工具执行 → GLM 二次生成 → 最终回复 |
| **错误处理** | API 超时 → 重试 1 次 → 友好错误提示 |

```xml
<!-- pom.xml 测试依赖 -->
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>mockwebserver</artifactId>
    <scope>test</scope>
</dependency>
```

## 9. 边界与错误处理汇总

| 场景 | 处理方式 |
|------|---------|
| GLM API 超时 | 重试 1 次，仍失败 → 回复「AI 服务暂时不可用」 |
| 工具调用异常 | 返回错误信息给 GLM，由其组织自然语言回复 |
| 并行工具调用 | 每轮最多接受 5 个，顺序执行 |
| 工具循环超 5 轮 | 中断 → 回复「查询路径过长，请简化问题」 |
| 工具返回 >10 条 | 截断并附说明「共 N 条，仅展示前 10 条」 |
| SSE 连接断开 | 前端 30s 无响应 → 提示，60s → 显示重试 |
| 会话所属人校验 | 删除/查询时校验 `account_id` 与当前用户匹配 |
| 用户未登录 | 返回 401 |
| context 数据损坏 | JSON 解析异常 → 重建空会话 |
| API Key 未配置 | 启动日志 WARN，AI 端点返回 503 |
| System prompt 注入 | 用户姓名/部门中特殊字符需过滤 |

## 10. 文件结构总览

```
src/main/java/com/ruiyi/
├── ai/
│   ├── AiService.java
│   ├── AiServiceImpl.java
│   ├── AiController.java
│   ├── AiProperties.java
│   ├── dto/
│   │   ├── AiChatRequest.java
│   │   └── AiChatResponse.java
│   ├── session/
│   │   ├── AiSession.java
│   │   ├── AiSessionMapper.java
│   │   └── AiSessionManager.java
│   └── tool/
│       ├── ToolExecutor.java
│       ├── AiToolHandler.java
│       ├── QueryCustomerTool.java
│       ├── QueryOrderTool.java
│       ├── QueryOpportunityTool.java
│       ├── QueryFollowUpTool.java
│       ├── QueryProductTool.java
│       ├── QueryReceiptTool.java
│       └── QueryStatisticsTool.java

src/main/resources/
├── mapper/
│   └── AiSessionMapper.xml
├── static/resources/js/ai/
│   ├── ai-chat.js
│   └── ai-utils.js
└── templates/ai/
    └── ai-panel.html

src/test/java/com/ruiyi/ai/
└── AiServiceImplTest.java
```

**新增文件：** 18 个 Java 文件 + 1 个 XML + 3 个前端文件 = 22 个文件

**修改文件：**
- `pom.xml` — 添加 mockwebserver 测试依赖
- `application.yml` — 添加 `ai.glm.*` 配置项
- `main.html` — 嵌入 AI 面板容器 + 引入 JS 和 CSS
