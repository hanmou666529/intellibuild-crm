# AI 智能助手 — 设计文档

> 2026-07-03  
> 项目：智营 CRM (RuoYi-Vue)  
> 状态：设计稿 v1

---

## 1. 概述

在现有 CRM 系统中集成大语言模型（智谱 GLM-4-Plus），通过后端 API 代理模式实现五大 AI 场景：客户资料补全、跟进记录生成、商机评分、日报/周报生成、合同风控审查。

### 核心原则

- **零侵入**：AI 模块以弹窗 + 独立页面形式存在，不修改现有业务页面的核心逻辑
- **权限对齐**：AI 获取的数据上下文受 `@CrmDataScope` 控制，超级管理员/经理/员工各见其范围
- **安全**：API Key 仅在后端，前端不接触

---

## 2. 技术架构

```
┌─────────────────────────────────────────────────────┐
│                   前端 (Vue 2)                       │
│  AI浮动按钮  │  AI工作台页面  │  各页面AI触发入口    │
└───────────────────┬─────────────────────────────────┘
                    │ POST /crm/ai/*
                    │ JWT + 权限注解
┌───────────────────▼─────────────────────────────────┐
│              后端 Controller (Spring Boot)           │
│  AiController.java                                   │
│  @PreAuthorize + @CrmDataScope                      │
└───────────────────┬─────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────┐
│              Agent 层 (业务Agent)                    │
│  CustomerIntelAgent │ FollowupAgent │ LeadScoring   │
│  ReportAgent       │ ContractReviewAgent            │
│       ↓ 通过 AiContextFetcher 查询数据库              │
│       ↓ 复用现有 Mapper + dataScope                  │
└───────────────────┬─────────────────────────────────┘
                    │ AiRequest { model, messages, temp }
┌───────────────────▼─────────────────────────────────┐
│              AiService (LLM 调用层)                  │
│  RestTemplate → 智谱API (OpenAI 协议)                │
│  https://open.bigmodel.cn/api/coding/paas/v4        │
└─────────────────────────────────────────────────────┘
```

---

## 3. 配置

```yaml
# application.yml
ai:
  enabled: true
  provider: zhipu
  api-key: 28274b2a795a4e6a93fc7d8628586d81.Jx606ij9LTKpTQ9o
  base-url: https://open.bigmodel.cn/api/coding/paas/v4
  model: glm-4-plus
  max-tokens: 4096
  temperature: 0.7
  timeout: 30000
```

---

## 4. 数据库变更

### 4.1 新增表：sys_ai_prompt

```sql
CREATE TABLE sys_ai_prompt (
  id            bigint(20)    NOT NULL AUTO_INCREMENT,
  scene         varchar(50)   NOT NULL COMMENT '场景标识',
  name          varchar(100)  NOT NULL COMMENT '名称',
  system_prompt text          COMMENT '系统提示词',
  user_template text          COMMENT '用户消息模板（{param} 占位符）',
  temperature   decimal(3,2)  DEFAULT 0.7 COMMENT '温度参数',
  status        char(1)       DEFAULT '1' COMMENT '0停用 1启用',
  create_by     varchar(64)   COMMENT '创建者',
  create_time   datetime      COMMENT '创建时间',
  update_by     varchar(64)   COMMENT '更新者',
  update_time   datetime      COMMENT '更新时间',
  remark        varchar(500)  COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_scene (scene)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 提示词模板';
```

### 4.2 新增表：sys_ai_log

```sql
CREATE TABLE sys_ai_log (
  id              bigint(20)    NOT NULL AUTO_INCREMENT,
  user_id         bigint(20)    NOT NULL COMMENT '用户ID',
  scene           varchar(50)   NOT NULL COMMENT '场景',
  request_body    text          COMMENT '请求内容',
  response_body   text          COMMENT '响应内容',
  prompt_tokens   int(11)       DEFAULT 0 COMMENT '输入token数',
  completion_tokens int(11)     DEFAULT 0 COMMENT '输出token数',
  total_tokens    int(11)       DEFAULT 0 COMMENT '总token数',
  duration_ms     int(11)       DEFAULT 0 COMMENT '耗时ms',
  status          char(1)       DEFAULT '0' COMMENT '0成功 1失败',
  error_msg       varchar(500)  COMMENT '错误信息',
  create_time     datetime      COMMENT '调用时间',
  PRIMARY KEY (id),
  KEY idx_user (user_id),
  KEY idx_scene (scene),
  KEY idx_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 调用日志';
```

### 4.3 扩展表：crm_pipeline

```sql
ALTER TABLE crm_pipeline
  ADD COLUMN score        int(11)     DEFAULT NULL COMMENT 'AI 商机评分(0-100)',
  ADD COLUMN score_reason varchar(500) DEFAULT NULL COMMENT '评分理由',
  ADD COLUMN score_time   datetime    DEFAULT NULL COMMENT '评分时间';
```

---

## 5. 后端模块

### 5.1 包结构

```
com.ruoyi.crm.ai
├── config
│   ├── AiConfig.java                    # @ConfigurationProperties(prefix="ai")
│   └── AiRestTemplateConfig.java        # RestTemplate Bean
├── core
│   ├── AiService.java                   # 接口
│   ├── AiServiceImpl.java               # RestTemplate 调智谱 API
│   ├── AiRequest.java / AiResponse.java # DTO
│   └── AiException.java                 # 自定义异常
├── prompt
│   ├── domain/SysAiPrompt.java          # 实体
│   ├── mapper/SysAiPromptMapper.java    # Mapper
│   ├── service/IPromptService.java      # 接口
│   └── service/impl/PromptServiceImpl.java
├── agent
│   ├── AbstractAiAgent.java             # 模板方法：组装prompt→调LLM→解析结果
│   ├── CustomerIntelAgent.java
│   ├── FollowupAgent.java
│   ├── LeadScoringAgent.java
│   ├── ReportAgent.java
│   └── ContractReviewAgent.java
├── context
│   └── AiContextFetcher.java            # 查询数据库获取上下文（带 dataScope）
└── web
    ├── AiController.java                # 前端入口
    └── PromptController.java            # Prompt 管理 CRUD
```

### 5.2 AiService 核心接口

```java
public interface AiService {
    /** 简单对话 */
    String chat(String systemPrompt, String userMessage);

    /** 带结构上下文的对话 */
    String chatWithContext(String systemPrompt, String jsonContext, String userInstruction);

    /** 带重试的对话 */
    String chatWithRetry(String system, String user, int maxRetries);
}
```

### 5.3 Agent 统一模式

所有 Agent 继承 `AbstractAiAgent`，模板方法：

```java
public abstract class AbstractAiAgent {
    @Autowired protected AiService aiService;
    @Autowired protected PromptManager promptManager;
    @Autowired protected AiContextFetcher contextFetcher;

    // 子类实现
    protected abstract String getScene();         // 返回 scene 标识
    protected abstract Object fetchContext(...);   // 查数据库
    protected abstract Object parseResult(String aiOutput); // 解析LLM输出

    // 模板方法
    public Object execute(...) {
        String prompt = promptManager.getPrompt(getScene());         // 1. 获取提示词
        Object context = fetchContext(...);                          // 2. 查数据
        String contextJson = JsonUtil.toJson(context);               // 3. 转JSON
        String result = aiService.chatWithContext(                   // 4. 调LLM
            prompt.getSystem(), contextJson, prompt.getUserTemplate());
        return parseResult(result);                                  // 5. 解析返回
    }
}
```

### 5.4 AiContextFetcher — 数据权限关键

所有数据库查询通过此组件进行，自动注入 `dataScope`：

```java
public class AiContextFetcher {
    @Autowired private CrmCustomerMapper customerMapper;
    @Autowired private CrmOrderMapper orderMapper;
    // ...

    public List<CrmCustomer> getActiveCustomers(CrmCustomer param) {
        // param 中已由 Controller 通过 @CrmDataScope 注入了 dataScope
        return customerMapper.selectCrmCustomerList(param);
    }
}
```

Controller 上标注 `@CrmDataScope`，框架自动注入当前用户的 data scope SQL 片段。

### 5.5 AI 日志记录

`AiServiceImpl.chat()` 内部用 AOP 或环绕拦截记录 `sys_ai_log`：

```java
// AiService 的方法执行后记录日志
SysAiLog log = new SysAiLog();
log.setUserId(SecurityUtils.getUserId());
log.setScene(scene);
log.setPromptTokens(response.getUsage().getPromptTokens());
log.setCompletionTokens(response.getUsage().getCompletionTokens());
log.setTotalTokens(...);
log.setDurationMs(...);
log.setStatus("0");
aiLogMapper.insert(log);
```

---

## 6. 五大 Agent 详细规格

### 6.1 客户资料智能补全 (CustomerIntelAgent)

| 项目 | 规格 |
|------|------|
| scene | `customer_intel` |
| API | `POST /crm/ai/customer/intel` |
| 请求 | `{ customerId, knownFields: { name, phone, ... } }` |
| 响应 | `{ name, industry, company, region, email, phone }` |
| 触发入口 | 客户表单「AI 补全」按钮 |
| 权限 | `crm:customer:edit` |

**System Prompt**:
```
你是 CRM 销售助理，擅长根据客户名称和已有信息推测补充缺失资料。
请推测以下字段：客户行业、公司名、所在地区、联系方式。
如果某个字段已存在则不覆盖。
只返回 JSON 格式，不要额外说明。
```

### 6.2 跟进记录生成 (FollowupAgent)

| 项目 | 规格 |
|------|------|
| scene | `followup_generate` |
| API | `POST /crm/ai/followup/generate` |
| 请求 | `{ customerId, keywords }` |
| 响应 | `{ content, nextAction, nextContactTime }` |
| 触发入口 | 跟进表单「AI 生成」按钮 |
| 权限 | `crm:followup:add` |

**上下文**：自动查询该客户最近 3 条跟进记录 + 客户基本信息。

### 6.3 商机评分 (LeadScoringAgent)

| 项目 | 规格 |
|------|------|
| scene | `lead_scoring` |
| API | `POST /crm/ai/scoring/run` |
| 请求 | 无参数（扫描全量活跃客户） |
| 响应 | `{ updated: N }` |
| 触发 | 定时任务（每日1AM）+ 手动按钮 |
| 权限 | 仅管理员/经理 |

**上下文**：批量获取活跃客户的：最近跟进时间、总成交金额、来源渠道、跟进次数、等级。

**输出**：更新 `crm_pipeline.score`、`score_reason`、`score_time`。

### 6.4 日报/周报生成 (ReportAgent)

| 项目 | 规格 |
|------|------|
| scene | `report_daily` / `report_weekly` |
| API | `POST /crm/ai/report/generate` |
| 请求 | `{ type: 'daily'|'weekly', date: '2026-07-03' }` |
| 响应 | `{ title, content, summary }` |
| 触发入口 | `/crm/ai/report` 页面 |
| 权限 | 员工看自己，经理看部门，管理员看全部 |

**上下文**：当前用户（或指定部门）在时间范围内的跟进记录、新增客户、订单变化。

### 6.5 合同风控审查 (ContractReviewAgent)

| 项目 | 规格 |
|------|------|
| scene | `contract_review` |
| API | `POST /crm/ai/contract/review` |
| 请求 | `{ contractId }` |
| 响应 | `[{ level: '高'|'中'|'低', risk: '风险描述', suggestion: '建议' }]` |
| 触发入口 | 合同详情页「AI 审查」按钮 |
| 权限 | `crm:contract:query` |

---

## 7. 前端模块

### 7.1 浮动 AI 按钮 (AiFloatingButton.vue)

- 注册在所有页面（`App.vue` 或 `Layout.vue`）
- 权限控制：有 `crm:ai:assistant` 权限者可见
- 点击弹出 `AiChatDialog.vue`，上下文自动感知当前路由

### 7.2 AI 对话弹窗 (AiChatDialog.vue)

- 接收当前页面上下文（路由 + 查询参数）
- 对话历史保存在 Vuex 或 sessionStorage
- 发送消息 → 后端统一 AI 接口 → 显示回复

### 7.3 AI 工作台页面 `/crm/ai`

- 顶部 Tab：智能对话 | 日报/周报 | 合同审查
- 智能对话：同弹窗逻辑，带历史记录
- 日报/周报 Tab：选择类型 + 日期 → 生成 → 预览 → 复制/导出
- 合同审查 Tab：选择合同 → AI 审查 → 结果展示
- Prompt 管理 Tab（仅管理员）：CRUD 提示词模板

### 7.4 API 封装 (api/crm/ai.js)

```js
// 客户资料补全
export function aiCustomerIntel(data) { ... }
// 跟进记录生成
export function aiFollowupGenerate(data) { ... }
// 运行商机评分
export function aiRunScoring() { ... }
// 生成报告
export function aiGenerateReport(data) { ... }
// 合同审查
export function aiContractReview(contractId) { ... }
```

---

## 8. 菜单与权限

| 菜单名 | 路由 | 权限标识 | 父菜单 | 类型 |
|--------|------|---------|--------|------|
| AI 智能助手 | `/crm/ai` | `crm:ai:page` | CRM | 目录 |
| Prompt 管理 | — | `crm:ai:prompt` | AI 智能助手 | 按钮 |
| AI 浮动按钮 | — | `crm:ai:assistant` | — | 按钮 |

---

## 9. 定时任务

使用已有 `ruoyi-quartz` 模块：

| 任务名 | 调度 | 说明 |
|--------|------|------|
| `AiLeadScoringTask` | 每日 01:00 | 自动运行商机评分 |
| `AiLogCleanTask` | 每周日 03:00 | 清理 30 天前的 AI 日志 |

---

## 10. 非功能设计

### 限流
- 每用户每分钟最多 20 次 AI 调用（可在 `AiServiceImpl` 中基于 Redis 实现）
- 每次调用超时 30s

### 错误处理
- 智谱 API 失败 → 重试 2 次（指数退避）
- 重试仍失败 → 返回友好提示「AI 服务暂时不可用，请稍后再试」
- 日志记录失败详情到 `sys_ai_log`

### 测试策略
- `AiService` 单元测试（Mock RestTemplate）
- 各 Agent 单元测试（Mock AiService）
- 集成测试（调用真实智谱 API，仅开发环境）
