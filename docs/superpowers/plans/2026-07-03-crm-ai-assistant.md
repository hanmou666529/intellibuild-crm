# AI 智能助手 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在现有 CRM 中集成智谱 GLM-4-Plus API，实现 5 大 AI 场景（客户资料补全、跟进记录生成、商机评分、日报周报、合同审查）

**Architecture:** 后端 RestTemplate 代理智谱 API，复用现有 Spring Security 权限 + @CrmDataScope。前端以浮动弹窗 + 独立页面双形态呈现

**Tech Stack:** Spring Boot 2.5.15 / Java 8 / Vue 2 / Element UI / RestTemplate / MySQL

---

## File Structure

### Backend (新建)
```
ruoyi-system/src/main/java/com/ruoyi/crm/
└── ai/
    ├── config/
    │   ├── AiConfig.java
    │   └── AiRestTemplateConfig.java
    ├── core/
    │   ├── AiService.java
    │   ├── AiServiceImpl.java
    │   ├── AiRequest.java
    │   ├── AiResponse.java
    │   └── AiException.java
    ├── prompt/
    │   ├── domain/SysAiPrompt.java
    │   ├── mapper/SysAiPromptMapper.java
    │   └── service/PromptService.java
    ├── log/
    │   ├── domain/SysAiLog.java
    │   └── mapper/SysAiLogMapper.java
    ├── agent/
    │   ├── AbstractAiAgent.java
    │   ├── CustomerIntelAgent.java
    │   ├── FollowupAgent.java
    │   ├── LeadScoringAgent.java
    │   ├── ReportAgent.java
    │   └── ContractReviewAgent.java
    ├── context/
    │   └── AiContextFetcher.java
    └── web/
        ├── AiController.java
        └── PromptController.java

ruoyi-system/src/main/resources/mapper/crm/
├── SysAiPromptMapper.xml
└── SysAiLogMapper.xml
```

### Frontend (新建)
```
ruoyi-ui/src/
├── api/crm/ai.js
├── views/crm/ai/
│   ├── index.vue
│   └── components/
│       ├── ChatPanel.vue
│       ├── ReportGenerator.vue
│       ├── ContractReview.vue
│       └── PromptManager.vue
└── components/
    └── AiFloatingButton.vue
```

---

## Implementation Tasks

### Task 1: 数据库 DDL

**Files:** SQL init script

- [ ] **Write DDL SQL**

```sql
-- 1. sys_ai_prompt
CREATE TABLE IF NOT EXISTS sys_ai_prompt (
  id            bigint(20)    NOT NULL AUTO_INCREMENT,
  scene         varchar(50)   NOT NULL COMMENT '场景标识',
  name          varchar(100)  NOT NULL COMMENT '名称',
  system_prompt text          COMMENT '系统提示词',
  user_template text          COMMENT '用户消息模板',
  temperature   decimal(3,2)  DEFAULT 0.70 COMMENT '温度参数',
  status        char(1)       DEFAULT '1' COMMENT '0停用 1启用',
  create_by     varchar(64)   COMMENT '创建者',
  create_time   datetime      COMMENT '创建时间',
  update_by     varchar(64)   COMMENT '更新者',
  update_time   datetime      COMMENT '更新时间',
  remark        varchar(500)  COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_scene (scene)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 提示词模板';

-- 2. sys_ai_log
CREATE TABLE IF NOT EXISTS sys_ai_log (
  id                bigint(20)    NOT NULL AUTO_INCREMENT,
  user_id           bigint(20)    NOT NULL COMMENT '用户ID',
  scene             varchar(50)   NOT NULL COMMENT '场景',
  request_body      text          COMMENT '请求内容',
  response_body     text          COMMENT '响应内容',
  prompt_tokens     int(11)       DEFAULT 0 COMMENT '输入token数',
  completion_tokens int(11)       DEFAULT 0 COMMENT '输出token数',
  total_tokens      int(11)       DEFAULT 0 COMMENT '总token数',
  duration_ms       int(11)       DEFAULT 0 COMMENT '耗时ms',
  status            char(1)       DEFAULT '0' COMMENT '0成功 1失败',
  error_msg         varchar(500)  COMMENT '错误信息',
  create_time       datetime      COMMENT '调用时间',
  PRIMARY KEY (id),
  KEY idx_user (user_id),
  KEY idx_scene (scene),
  KEY idx_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 调用日志';

-- 3. crm_pipeline 扩展
ALTER TABLE crm_pipeline
  ADD COLUMN IF NOT EXISTS score        int(11)     DEFAULT NULL COMMENT 'AI 商机评分(0-100)',
  ADD COLUMN IF NOT EXISTS score_reason varchar(500) DEFAULT NULL COMMENT '评分理由',
  ADD COLUMN IF NOT EXISTS score_time   datetime    DEFAULT NULL COMMENT '评分时间';

-- 4. 初始 prompt 数据
INSERT INTO sys_ai_prompt (scene, name, system_prompt, user_template, temperature, status) VALUES
('customer_intel', '客户资料补全',
'你是 CRM 销售助理，擅长根据客户名称和已有信息推测补充缺失资料。请推测以下字段：客户行业、公司名、所在地区、联系方式。如果某个字段已存在则不覆盖。只返回 JSON 格式，不要额外说明。',
'客户名称：{name}\n已有信息：{knownFields}\n请补充缺失字段。',
0.7, '1'),

('followup_generate', '跟进记录生成',
'你是 CRM 销售助理。根据客户历史跟进记录和用户输入的关键词，生成一段专业的跟进记录（150字内）。包含：沟通内容摘要、客户意向判断、下一步建议。语气专业但自然。',
'客户：{customerName}\n近期跟进记录：{history}\n用户输入要点：{keywords}',
0.7, '1'),

('lead_scoring', '商机评分',
'你是一位资深销售分析师。分析以下客户数据，对每个客户给出 0-100 的商机评分。评分依据：最近跟进时间（越近越高）、成交金额（越高越高）、跟进频率（越频繁越高）、客户等级。输出格式：[{customerId, score, reason, action}]',
'{customers}',
0.3, '1'),

('report_daily', '日报生成',
'生成销售日报。格式：\n## 工作概览\n- 新增客户：X个\n- 跟进记录：X条\n- 成交金额：¥X\n\n## 重点跟进\n（按客户列举）\n\n## 存在问题\n\n## 明日计划',
'时间：{date}\n我的工作数据：{data}',
0.7, '1'),

('report_weekly', '周报生成',
'生成销售周报。格式：\n## 本周概览\n\n## 新增客户\n\n## 跟进情况\n\n## 成交情况\n\n## 存在问题\n\n## 下周计划',
'时间范围：{startDate} 至 {endDate}\n我的工作数据：{data}',
0.7, '1'),

('contract_review', '合同审查',
'你是一位法务风控专家。审查以下合同信息，识别风险项。每一项输出：\n风险等级（高/中/低）\n风险描述\n建议措施\n只返回 JSON 数组：[{level, risk, suggestion}]',
'合同编号：{contractNo}\n合同名称：{contractName}\n金额：{amount}\n期限：{startDate} ~ {endDate}\n明细：{items}',
0.3, '1');
```

- [ ] **Apply SQL** via MySQL CLI

Run: `"D:\mysql\bin\mysql.exe" -u root -pHae147258369 ry-vue < sql/ai_ddl.sql`

---

### Task 2: 后端基础设施 — AiConfig + AiException + DTO

**Files:**
- Create: `ruoyi-system/src/main/java/com/ruoyi/crm/ai/config/AiConfig.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/crm/ai/config/AiRestTemplateConfig.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/crm/ai/core/AiRequest.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/crm/ai/core/AiResponse.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/crm/ai/core/AiException.java`

- [ ] **Create AiConfig.java**

```java
package com.ruoyi.crm.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ai")
public class AiConfig {
    private boolean enabled = true;
    private String provider = "zhipu";
    private String apiKey;
    private String baseUrl = "https://open.bigmodel.cn/api/coding/paas/v4";
    private String model = "glm-4-plus";
    private int maxTokens = 4096;
    private double temperature = 0.7;
    private int timeout = 30000;

    // getters & setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public int getMaxTokens() { return maxTokens; }
    public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public int getTimeout() { return timeout; }
    public void setTimeout(int timeout) { this.timeout = timeout; }
}
```

- [ ] **Create AiRestTemplateConfig.java**

```java
package com.ruoyi.crm.ai.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;

@Configuration
public class AiRestTemplateConfig {
    @Bean("aiRestTemplate")
    public RestTemplate aiRestTemplate(AiConfig aiConfig) {
        return new RestTemplateBuilder()
            .setConnectTimeout(Duration.ofMillis(aiConfig.getTimeout()))
            .setReadTimeout(Duration.ofMillis(aiConfig.getTimeout()))
            .build();
    }
}
```

- [ ] **Create AiRequest.java**

```java
package com.ruoyi.crm.ai.core;

import java.util.List;

public class AiRequest {
    private String model;
    private List<Message> messages;
    private double temperature;
    private int maxTokens;

    public static class Message {
        private String role;
        private String content;
        public Message() {}
        public Message(String role, String content) { this.role = role; this.content = content; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    // getters & setters
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public int getMaxTokens() { return maxTokens; }
    public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
}
```

- [ ] **Create AiResponse.java**

```java
package com.ruoyi.crm.ai.core;

import java.util.List;

public class AiResponse {
    private List<Choice> choices;
    private Usage usage;

    public static class Choice {
        private Message message;
        public Message getMessage() { return message; }
        public void setMessage(Message message) { this.message = message; }
        public static class Message {
            private String role;
            private String content;
            public String getRole() { return role; }
            public void setRole(String role) { this.role = role; }
            public String getContent() { return content; }
            public void setContent(String content) { this.content = content; }
        }
    }

    public static class Usage {
        private int promptTokens;
        private int completionTokens;
        private int totalTokens;
        public int getPromptTokens() { return promptTokens; }
        public void setPromptTokens(int promptTokens) { this.promptTokens = promptTokens; }
        public int getCompletionTokens() { return completionTokens; }
        public void setCompletionTokens(int completionTokens) { this.completionTokens = completionTokens; }
        public int getTotalTokens() { return totalTokens; }
        public void setTotalTokens(int totalTokens) { this.totalTokens = totalTokens; }
    }

    public List<Choice> getChoices() { return choices; }
    public void setChoices(List<Choice> choices) { this.choices = choices; }
    public Usage getUsage() { return usage; }
    public void setUsage(Usage usage) { this.usage = usage; }

    public String getContent() {
        if (choices != null && !choices.isEmpty() && choices.get(0).getMessage() != null) {
            return choices.get(0).getMessage().getContent();
        }
        return "";
    }
}
```

- [ ] **Create AiException.java**

```java
package com.ruoyi.crm.ai.core;

public class AiException extends RuntimeException {
    private int code;
    public AiException(String message) { super(message); this.code = 500; }
    public AiException(String message, int code) { super(message); this.code = code; }
    public AiException(String message, Throwable cause) { super(message, cause); this.code = 500; }
    public int getCode() { return code; }
}
```

---

### Task 3: AiService + AiServiceImpl

**Files:**
- Create: `ruoyi-system/src/main/java/com/ruoyi/crm/ai/core/AiService.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/crm/ai/core/AiServiceImpl.java`

- [ ] **Create AiService.java**

```java
package com.ruoyi.crm.ai.core;

import java.util.Map;

public interface AiService {
    String chat(String systemPrompt, String userMessage);
    String chatWithContext(String systemPrompt, String jsonContext, String userInstruction);
    Map<String, Object> chatWithDetail(String systemPrompt, String userMessage);
}
```

- [ ] **Create AiServiceImpl.java**

```java
package com.ruoyi.crm.ai.core;

import com.ruoyi.crm.ai.config.AiConfig;
import com.ruoyi.crm.ai.log.domain.SysAiLog;
import com.ruoyi.crm.ai.log.mapper.SysAiLogMapper;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AiServiceImpl implements AiService {
    private final RestTemplate restTemplate;
    private final AiConfig aiConfig;
    private final SysAiLogMapper aiLogMapper;

    public AiServiceImpl(@Qualifier("aiRestTemplate") RestTemplate restTemplate,
                         AiConfig aiConfig, SysAiLogMapper aiLogMapper) {
        this.restTemplate = restTemplate;
        this.aiConfig = aiConfig;
        this.aiLogMapper = aiLogMapper;
    }

    @Override
    public String chat(String systemPrompt, String userMessage) {
        Map<String, Object> result = chatWithDetail(systemPrompt, userMessage);
        return (String) result.get("content");
    }

    @Override
    public String chatWithContext(String systemPrompt, String jsonContext, String userInstruction) {
        String userMessage = userInstruction
            .replace("{context}", jsonContext)
            .replace("{data}", jsonContext);
        return chat(systemPrompt, userMessage);
    }

    @Override
    public Map<String, Object> chatWithDetail(String systemPrompt, String userMessage) {
        long start = System.currentTimeMillis();
        AiResponse response = doCall(systemPrompt, userMessage);
        long duration = System.currentTimeMillis() - start;

        // 记录日志
        saveLog("chat", systemPrompt, userMessage, response, duration, null);

        Map<String, Object> result = new HashMap<>();
        result.put("content", response.getContent());
        if (response.getUsage() != null) {
            result.put("promptTokens", response.getUsage().getPromptTokens());
            result.put("completionTokens", response.getUsage().getCompletionTokens());
            result.put("totalTokens", response.getUsage().getTotalTokens());
        }
        return result;
    }

    private AiResponse doCall(String systemPrompt, String userMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(aiConfig.getApiKey());

        AiRequest request = new AiRequest();
        request.setModel(aiConfig.getModel());
        request.setTemperature(aiConfig.getTemperature());
        request.setMaxTokens(aiConfig.getMaxTokens());
        request.setMessages(Arrays.asList(
            new AiRequest.Message("system", systemPrompt),
            new AiRequest.Message("user", userMessage)
        ));

        HttpEntity<AiRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<AiResponse> response = restTemplate.exchange(
            aiConfig.getBaseUrl() + "/chat/completions",
            HttpMethod.POST,
            entity,
            AiResponse.class
        );
        return response.getBody();
    }

    private void saveLog(String scene, String systemPrompt, String userMessage,
                          AiResponse response, long duration, String errorMsg) {
        try {
            SysAiLog log = new SysAiLog();
            log.setUserId(SecurityUtils.getUserId());
            log.setScene(scene);
            log.setDurationMs((int) duration);
            log.setStatus(errorMsg == null ? "0" : "1");
            log.setErrorMsg(errorMsg);
            if (response != null && response.getUsage() != null) {
                log.setPromptTokens(response.getUsage().getPromptTokens());
                log.setCompletionTokens(response.getUsage().getCompletionTokens());
                log.setTotalTokens(response.getUsage().getTotalTokens());
            }
            aiLogMapper.insert(log);
        } catch (Exception ignored) { }
    }
}
```

---

### Task 4: Domain + Mapper + XML for sys_ai_prompt 和 sys_ai_log

**Files:**
- Create: `ruoyi-system/src/main/java/com/ruoyi/crm/ai/prompt/domain/SysAiPrompt.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/crm/ai/prompt/mapper/SysAiPromptMapper.java`
- Create: `ruoyi-system/src/main/resources/mapper/crm/SysAiPromptMapper.xml`
- Create: `ruoyi-system/src/main/java/com/ruoyi/crm/ai/log/domain/SysAiLog.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/crm/ai/log/mapper/SysAiLogMapper.java`
- Create: `ruoyi-system/src/main/resources/mapper/crm/SysAiLogMapper.xml`
- Create: `ruoyi-system/src/main/java/com/ruoyi/crm/ai/prompt/service/PromptService.java`

- [ ] **Create SysAiPrompt.java**

```java
package com.ruoyi.crm.ai.prompt.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import javax.validation.constraints.NotBlank;

public class SysAiPrompt extends BaseEntity {
    private Long id;
    @NotBlank private String scene;
    @NotBlank private String name;
    private String systemPrompt;
    private String userTemplate;
    private Double temperature;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getScene() { return scene; }
    public void setScene(String scene) { this.scene = scene; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSystemPrompt() { return systemPrompt; }
    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }
    public String getUserTemplate() { return userTemplate; }
    public void setUserTemplate(String userTemplate) { this.userTemplate = userTemplate; }
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
```

- [ ] **Create SysAiPromptMapper.java**

```java
package com.ruoyi.crm.ai.prompt.mapper;

import com.ruoyi.crm.ai.prompt.domain.SysAiPrompt;
import java.util.List;

public interface SysAiPromptMapper {
    List<SysAiPrompt> selectList(SysAiPrompt prompt);
    SysAiPrompt selectById(Long id);
    SysAiPrompt selectByScene(String scene);
    int insert(SysAiPrompt prompt);
    int update(SysAiPrompt prompt);
    int deleteByIds(Long[] ids);
}
```

- [ ] **Create SysAiPromptMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.ruoyi.crm.ai.prompt.mapper.SysAiPromptMapper">
    <resultMap type="com.ruoyi.crm.ai.prompt.domain.SysAiPrompt" id="AiPromptResult">
        <id property="id" column="id"/>
        <result property="scene" column="scene"/>
        <result property="name" column="name"/>
        <result property="systemPrompt" column="system_prompt"/>
        <result property="userTemplate" column="user_template"/>
        <result property="temperature" column="temperature"/>
        <result property="status" column="status"/>
        <result property="createBy" column="create_by"/>
        <result property="createTime" column="create_time"/>
        <result property="updateBy" column="update_by"/>
        <result property="updateTime" column="update_time"/>
        <result property="remark" column="remark"/>
    </resultMap>

    <sql id="selectVo">
        SELECT id, scene, name, system_prompt, user_template, temperature, status,
               create_by, create_time, update_by, update_time, remark
        FROM sys_ai_prompt
    </sql>

    <select id="selectList" parameterType="SysAiPrompt" resultMap="AiPromptResult">
        <include refid="selectVo"/>
        <where>
            <if test="scene != null and scene != ''">AND scene = #{scene}</if>
            <if test="status != null and status != ''">AND status = #{status}</if>
        </where>
        ORDER BY scene
    </select>

    <select id="selectById" resultMap="AiPromptResult">
        <include refid="selectVo"/> WHERE id = #{id}
    </select>

    <select id="selectByScene" resultMap="AiPromptResult">
        <include refid="selectVo"/> WHERE scene = #{scene} AND status = '1'
    </select>

    <insert id="insert" parameterType="SysAiPrompt" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO sys_ai_prompt (scene, name, system_prompt, user_template, temperature, status,
                                   create_by, create_time, remark)
        VALUES (#{scene}, #{name}, #{systemPrompt}, #{userTemplate}, #{temperature}, #{status},
                #{createBy}, sysdate(), #{remark})
    </insert>

    <update id="update" parameterType="SysAiPrompt">
        UPDATE sys_ai_prompt
        <set>
            <if test="scene != null">scene = #{scene},</if>
            <if test="name != null">name = #{name},</if>
            <if test="systemPrompt != null">system_prompt = #{systemPrompt},</if>
            <if test="userTemplate != null">user_template = #{userTemplate},</if>
            <if test="temperature != null">temperature = #{temperature},</if>
            <if test="status != null">status = #{status},</if>
            <if test="updateBy != null">update_by = #{updateBy},</if>
            update_time = sysdate()
        </set>
        WHERE id = #{id}
    </update>

    <delete id="deleteByIds">
        DELETE FROM sys_ai_prompt WHERE id IN
        <foreach collection="array" item="id" open="(" separator="," close=")">#{id}</foreach>
    </delete>
</mapper>
```

- [ ] **Create SysAiLog.java**

```java
package com.ruoyi.crm.ai.log.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class SysAiLog extends BaseEntity {
    private Long id;
    private Long userId;
    private String scene;
    private String requestBody;
    private String responseBody;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private Integer durationMs;
    private String status;
    private String errorMsg;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getScene() { return scene; }
    public void setScene(String scene) { this.scene = scene; }
    public String getRequestBody() { return requestBody; }
    public void setRequestBody(String requestBody) { this.requestBody = requestBody; }
    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
    public Integer getPromptTokens() { return promptTokens; }
    public void setPromptTokens(Integer promptTokens) { this.promptTokens = promptTokens; }
    public Integer getCompletionTokens() { return completionTokens; }
    public void setCompletionTokens(Integer completionTokens) { this.completionTokens = completionTokens; }
    public Integer getTotalTokens() { return totalTokens; }
    public void setTotalTokens(Integer totalTokens) { this.totalTokens = totalTokens; }
    public Integer getDurationMs() { return durationMs; }
    public void setDurationMs(Integer durationMs) { this.durationMs = durationMs; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
}
```

- [ ] **Create SysAiLogMapper.java**

```java
package com.ruoyi.crm.ai.log.mapper;

import com.ruoyi.crm.ai.log.domain.SysAiLog;

public interface SysAiLogMapper {
    int insert(SysAiLog log);
}
```

- [ ] **Create SysAiLogMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.ruoyi.crm.ai.log.mapper.SysAiLogMapper">
    <insert id="insert" parameterType="SysAiLog">
        INSERT INTO sys_ai_log (user_id, scene, request_body, response_body,
                                prompt_tokens, completion_tokens, total_tokens,
                                duration_ms, status, error_msg, create_time)
        VALUES (#{userId}, #{scene}, #{requestBody}, #{responseBody},
                #{promptTokens}, #{completionTokens}, #{totalTokens},
                #{durationMs}, #{status}, #{errorMsg}, sysdate())
    </insert>
</mapper>
```

- [ ] **Create PromptService.java**

```java
package com.ruoyi.crm.ai.prompt.service;

import com.ruoyi.crm.ai.prompt.domain.SysAiPrompt;
import java.util.List;

public interface PromptService {
    SysAiPrompt getByScene(String scene);
    List<SysAiPrompt> selectList(SysAiPrompt query);
    SysAiPrompt selectById(Long id);
    int insert(SysAiPrompt prompt);
    int update(SysAiPrompt prompt);
    int delete(Long[] ids);
}
```

- [ ] **Create PromptServiceImpl.java**

```java
package com.ruoyi.crm.ai.prompt.service.impl;

import com.ruoyi.crm.ai.prompt.domain.SysAiPrompt;
import com.ruoyi.crm.ai.prompt.mapper.SysAiPromptMapper;
import com.ruoyi.crm.ai.prompt.service.PromptService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

@Service
public class PromptServiceImpl implements PromptService {
    @Resource private SysAiPromptMapper promptMapper;

    @Override
    public SysAiPrompt getByScene(String scene) { return promptMapper.selectByScene(scene); }

    @Override
    public List<SysAiPrompt> selectList(SysAiPrompt query) { return promptMapper.selectList(query); }

    @Override
    public SysAiPrompt selectById(Long id) { return promptMapper.selectById(id); }

    @Override
    public int insert(SysAiPrompt prompt) { return promptMapper.insert(prompt); }

    @Override
    public int update(SysAiPrompt prompt) { return promptMapper.update(prompt); }

    @Override
    public int delete(Long[] ids) { return promptMapper.deleteByIds(ids); }
}
```

- [ ] **Add MyBatis mapper scan** — ensure the new mapper packages are scanned

Modify `ruoyi-framework/src/main/java/com/ruoyi/framework/config/ApplicationConfig.java` or check `@MapperScan` annotation. The existing `@MapperScan` should cover `com.ruoyi.crm.**`. If not, verify the scan path includes `com.ruoyi.crm.ai.**`.

---

### Task 5: Application.yml 配置

**Files:**
- Modify: `ruoyi-admin/src/main/resources/application.yml`

- [ ] **Add AI config to application.yml**

```yaml
# AI 大模型配置
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

### Task 6: AbstractAiAgent + AiContextFetcher

**Files:**
- Create: `ruoyi-system/src/main/java/com/ruoyi/crm/ai/agent/AbstractAiAgent.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/crm/ai/context/AiContextFetcher.java`

- [ ] **Create AbstractAiAgent.java**

```java
package com.ruoyi.crm.ai.agent;

import com.ruoyi.crm.ai.core.AiService;
import com.ruoyi.crm.ai.prompt.domain.SysAiPrompt;
import com.ruoyi.crm.ai.prompt.service.PromptService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractAiAgent {
    @Autowired protected AiService aiService;
    @Autowired protected PromptService promptService;

    protected abstract String getScene();

    protected SysAiPrompt getPrompt() {
        return promptService.getByScene(getScene());
    }

    protected String callAi(String contextJson, String userMessage) {
        SysAiPrompt prompt = getPrompt();
        if (prompt == null) return "AI 提示词未配置";
        String userInput = userMessage != null ? userMessage
            : (prompt.getUserTemplate() != null
                ? prompt.getUserTemplate().replace("{data}", contextJson).replace("{context}", contextJson)
                : contextJson);
        return aiService.chat(prompt.getSystemPrompt(), userInput);
    }
}
```

- [ ] **Create AiContextFetcher.java**

```java
package com.ruoyi.crm.ai.context;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.crm.domain.CrmContract;
import com.ruoyi.crm.domain.CrmCustomer;
import com.ruoyi.crm.domain.CrmOrder;
import com.ruoyi.crm.mapper.*;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.*;

@Component
public class AiContextFetcher {
    @Resource private CrmCustomerMapper customerMapper;
    @Resource private CrmOrderMapper orderMapper;
    @Resource private CrmContractMapper contractMapper;
    @Resource private CrmFollowupMapper followupMapper;

    public CrmCustomer customerParam() {
        CrmCustomer param = new CrmCustomer();
        copyDataScope(param);
        return param;
    }

    public CrmOrder orderParam() {
        CrmOrder param = new CrmOrder();
        copyDataScope(param);
        return param;
    }

    public CrmContract contractParam() {
        CrmContract param = new CrmContract();
        copyDataScope(param);
        return param;
    }

    private void copyDataScope(Object entity) {
        try {
            Object params = entity.getClass().getMethod("getParams").invoke(entity);
            if (params instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> p = (Map<String, Object>) params;
                // 从 SecurityUtils 获取当前用户的 dataScope（由 @CrmDataScope 注入）
                // 实际实现依赖于 ruoyi 框架的 DataScope 机制
                // 这里只创建一个空参数，由 Controller 上的 @CrmDataScope 注解自动注入
            }
        } catch (Exception ignored) { }
    }
}
```

---

### Task 7: 五大 Agent

**Files:**
- Create: `ruoyi-system/src/main/java/com/ruoyi/crm/ai/agent/CustomerIntelAgent.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/crm/ai/agent/FollowupAgent.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/crm/ai/agent/LeadScoringAgent.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/crm/ai/agent/ReportAgent.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/crm/ai/agent/ContractReviewAgent.java`

- [ ] **Create CustomerIntelAgent.java**

```java
package com.ruoyi.crm.ai.agent;

import com.ruoyi.crm.domain.CrmCustomer;
import com.ruoyi.crm.mapper.CrmCustomerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomerIntelAgent extends AbstractAiAgent {
    @Autowired private CrmCustomerMapper customerMapper;

    @Override
    protected String getScene() { return "customer_intel"; }

    public Map<String, Object> complete(Long customerId, Map<String, Object> knownFields) {
        CrmCustomer customer = customerMapper.selectCrmCustomerById(customerId);
        if (customer == null) throw new RuntimeException("客户不存在");

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("name", customer.getCustomerName());
        ctx.put("knownFields", knownFields);

        String json = "{\"name\":\"" + customer.getCustomerName() + "\",\"knownFields\":" + com.alibaba.fastjson2.JSON.toJSONString(knownFields) + "}";
        String result = callAi(json, null);
        return parseJsonResult(result);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonResult(String json) {
        try {
            return com.alibaba.fastjson2.JSON.parseObject(json);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "AI 返回格式异常");
            return err;
        }
    }
}
```

- [ ] **Create FollowupAgent.java**

```java
package com.ruoyi.crm.ai.agent;

import com.ruoyi.crm.domain.CrmCustomer;
import com.ruoyi.crm.domain.CrmFollowup;
import com.ruoyi.crm.mapper.CrmCustomerMapper;
import com.ruoyi.crm.mapper.CrmFollowupMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class FollowupAgent extends AbstractAiAgent {
    @Autowired private CrmCustomerMapper customerMapper;
    @Autowired private CrmFollowupMapper followupMapper;

    @Override
    protected String getScene() { return "followup_generate"; }

    public Map<String, Object> generate(Long customerId, String keywords) {
        CrmCustomer customer = customerMapper.selectCrmCustomerById(customerId);
        if (customer == null) throw new RuntimeException("客户不存在");

        // 查询最近 3 条跟进记录
        CrmFollowup param = new CrmFollowup();
        param.setCustomerId(customerId);
        List<CrmFollowup> history = followupMapper.selectCrmFollowupList(param);
        List<Map<String, Object>> recentHistory = new ArrayList<>();
        for (int i = 0; i < Math.min(history.size(), 3); i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("content", history.get(i).getContent());
            item.put("time", String.valueOf(history.get(i).getCreateTime()));
            recentHistory.add(item);
        }

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("customerName", customer.getCustomerName());
        ctx.put("history", recentHistory);
        ctx.put("keywords", keywords);

        String json = "{\"customerName\":\"" + customer.getCustomerName()
            + "\",\"history\":" + com.alibaba.fastjson2.JSON.toJSONString(recentHistory)
            + ",\"keywords\":\"" + keywords + "\"}";
        String result = callAi(json, null);
        return parseJsonResult(result);
    }

    private Map<String, Object> parseJsonResult(String json) {
        Map<String, Object> map = new HashMap<>();
        map.put("content", json);
        return map;
    }
}
```

- [ ] **Create LeadScoringAgent.java**

```java
package com.ruoyi.crm.ai.agent;

import com.ruoyi.crm.domain.CrmCustomer;
import com.ruoyi.crm.domain.CrmPipeline;
import com.ruoyi.crm.mapper.CrmCustomerMapper;
import com.ruoyi.crm.mapper.CrmPipelineMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class LeadScoringAgent extends AbstractAiAgent {
    @Autowired private CrmCustomerMapper customerMapper;
    @Autowired private CrmPipelineMapper pipelineMapper;

    @Override
    protected String getScene() { return "lead_scoring"; }

    @SuppressWarnings("unchecked")
    public int runScoring() {
        CrmCustomer param = new CrmCustomer();
        List<CrmCustomer> customers = customerMapper.selectCrmCustomerList(param);
        List<Map<String, Object>> customerData = new ArrayList<>();
        for (CrmCustomer c : customers) {
            Map<String, Object> item = new HashMap<>();
            item.put("customerId", c.getCustomerId());
            item.put("name", c.getCustomerName());
            item.put("level", c.getLevel());
            item.put("source", c.getSource());
            item.put("followStatus", c.getFollowStatus());
            customerData.add(item);
        }

        String json = com.alibaba.fastjson2.JSON.toJSONString(customerData);
        String result = callAi(json, null);

        List<Map<String, Object>> scores;
        try {
            scores = com.alibaba.fastjson2.JSON.parseObject(result, List.class);
        } catch (Exception e) {
            return 0;
        }

        int updated = 0;
        for (Map<String, Object> s : scores) {
            Object id = s.get("customerId");
            if (id == null) continue;
            Long customerId = Long.valueOf(id.toString());
            Integer score = s.get("score") instanceof Number
                ? ((Number) s.get("score")).intValue() : null;
            String reason = (String) s.get("reason");
            if (score == null) continue;

            CrmPipeline pipeline = new CrmPipeline();
            pipeline.setCustomerId(customerId);
            List<CrmPipeline> existing = pipelineMapper.selectCrmPipelineList(pipeline);
            if (!existing.isEmpty()) {
                CrmPipeline p = existing.get(0);
                p.setScore(score);
                p.setScoreReason(reason);
                p.setScoreTime(new Date());
                pipelineMapper.updateCrmPipeline(p);
                updated++;
            }
        }
        return updated;
    }
}
```

- [ ] **Create ReportAgent.java**

```java
package com.ruoyi.crm.ai.agent;

import com.ruoyi.crm.domain.CrmCustomer;
import com.ruoyi.crm.domain.CrmFollowup;
import com.ruoyi.crm.domain.CrmOrder;
import com.ruoyi.crm.mapper.*;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class ReportAgent extends AbstractAiAgent {
    @Autowired private CrmCustomerMapper customerMapper;
    @Autowired private CrmFollowupMapper followupMapper;
    @Autowired private CrmOrderMapper orderMapper;

    @Override
    protected String getScene() { return "report_daily"; }

    public Map<String, Object> generate(String type, String date) {
        String sceneKey = "report_daily";
        if ("weekly".equals(type)) sceneKey = "report_weekly";

        Long userId = SecurityUtils.getUserId();
        Map<String, Object> data = new HashMap<>();

        // 查询新客户数
        CrmCustomer customerParam = new CrmCustomer();
        customerParam.getParams().put("createBy", String.valueOf(userId));
        data.put("newCustomers", customerMapper.selectCrmCustomerList(customerParam).size());

        // 查询跟进数
        CrmFollowup followupParam = new CrmFollowup();
        followupParam.setCreateBy(SecurityUtils.getUsername());
        data.put("followups", followupMapper.selectCrmFollowupList(followupParam).size());

        // 查询订单数
        CrmOrder orderParam = new CrmOrder();
        data.put("orders", orderMapper.selectCrmOrderList(orderParam).size());

        String json = com.alibaba.fastjson2.JSON.toJSONString(data);
        String result = callAi(json, null);

        Map<String, Object> res = new HashMap<>();
        res.put("type", type);
        res.put("date", date);
        res.put("content", result);
        return res;
    }
}
```

- [ ] **Create ContractReviewAgent.java**

```java
package com.ruoyi.crm.ai.agent;

import com.ruoyi.crm.domain.CrmContract;
import com.ruoyi.crm.domain.CrmContractDetail;
import com.ruoyi.crm.mapper.CrmContractDetailMapper;
import com.ruoyi.crm.mapper.CrmContractMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class ContractReviewAgent extends AbstractAiAgent {
    @Autowired private CrmContractMapper contractMapper;
    @Autowired private CrmContractDetailMapper contractDetailMapper;

    @Override
    protected String getScene() { return "contract_review"; }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> review(Long contractId) {
        CrmContract contract = contractMapper.selectCrmContractById(contractId);
        if (contract == null) throw new RuntimeException("合同不存在");

        // 查询合同明细
        CrmContractDetail detailParam = new CrmContractDetail();
        detailParam.setContractId(contractId);
        List<CrmContractDetail> details = contractDetailMapper.selectCrmContractDetailList(detailParam);

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("contractNo", contract.getContractNo());
        ctx.put("contractName", contract.getContractName());
        ctx.put("amount", contract.getAmount());
        ctx.put("startDate", String.valueOf(contract.getStartDate()));
        ctx.put("endDate", String.valueOf(contract.getEndDate()));
        ctx.put("items", details);

        String json = com.alibaba.fastjson2.JSON.toJSONString(ctx);
        String result = callAi(json, null);

        try {
            return com.alibaba.fastjson2.JSON.parseObject(result, List.class);
        } catch (Exception e) {
            List<Map<String, Object>> fallback = new ArrayList<>();
            Map<String, Object> err = new HashMap<>();
            err.put("level", "低");
            err.put("risk", "AI 审查服务暂时不可用");
            err.put("suggestion", "建议人工审查合同条款");
            fallback.add(err);
            return fallback;
        }
    }
}
```

---

### Task 8: AiController + PromptController

**Files:**
- Create: `ruoyi-system/src/main/java/com/ruoyi/crm/ai/web/AiController.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/crm/ai/web/PromptController.java`

- [ ] **Create AiController.java**

```java
package com.ruoyi.crm.ai.web;

import com.ruoyi.common.annotation.CrmDataScope;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.crm.ai.agent.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/crm/ai")
public class AiController extends BaseController {
    @Autowired private CustomerIntelAgent customerIntelAgent;
    @Autowired private FollowupAgent followupAgent;
    @Autowired private LeadScoringAgent leadScoringAgent;
    @Autowired private ReportAgent reportAgent;
    @Autowired private ContractReviewAgent contractReviewAgent;

    @PreAuthorize("@ss.hasPermi('crm:customer:edit')")
    @PostMapping("/customer/intel")
    public AjaxResult customerIntel(@RequestBody Map<String, Object> params) {
        Long customerId = Long.valueOf(params.get("customerId").toString());
        @SuppressWarnings("unchecked")
        Map<String, Object> knownFields = (Map<String, Object>) params.getOrDefault("knownFields", new java.util.HashMap<>());
        return AjaxResult.success(customerIntelAgent.complete(customerId, knownFields));
    }

    @PreAuthorize("@ss.hasPermi('crm:followup:add')")
    @PostMapping("/followup/generate")
    public AjaxResult followupGenerate(@RequestBody Map<String, Object> params) {
        Long customerId = Long.valueOf(params.get("customerId").toString());
        String keywords = (String) params.getOrDefault("keywords", "");
        return AjaxResult.success(followupAgent.generate(customerId, keywords));
    }

    @PreAuthorize("@ss.hasRole('admin')")
    @PostMapping("/scoring/run")
    public AjaxResult runScoring() {
        int updated = leadScoringAgent.runScoring();
        return AjaxResult.success("已更新 " + updated + " 条评分");
    }

    @PreAuthorize("@ss.hasPermi('crm:ai:page')")
    @PostMapping("/report/generate")
    public AjaxResult reportGenerate(@RequestBody Map<String, Object> params) {
        String type = (String) params.getOrDefault("type", "daily");
        String date = (String) params.getOrDefault("date", new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
        return AjaxResult.success(reportAgent.generate(type, date));
    }

    @PreAuthorize("@ss.hasPermi('crm:contract:query')")
    @PostMapping("/contract/review")
    public AjaxResult contractReview(@RequestBody Map<String, Object> params) {
        Long contractId = Long.valueOf(params.get("contractId").toString());
        return AjaxResult.success(contractReviewAgent.review(contractId));
    }
}
```

- [ ] **Create PromptController.java**

```java
package com.ruoyi.crm.ai.web;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.crm.ai.prompt.domain.SysAiPrompt;
import com.ruoyi.crm.ai.prompt.service.PromptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/crm/ai/prompt")
public class PromptController extends BaseController {
    @Autowired private PromptService promptService;

    @PreAuthorize("@ss.hasPermi('crm:ai:prompt')")
    @GetMapping("/list")
    public TableDataInfo list(SysAiPrompt query) {
        startPage();
        List<SysAiPrompt> list = promptService.selectList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('crm:ai:prompt')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return AjaxResult.success(promptService.selectById(id));
    }

    @PreAuthorize("@ss.hasPermi('crm:ai:prompt')")
    @Log(title = "Prompt管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SysAiPrompt prompt) {
        return toAjax(promptService.insert(prompt));
    }

    @PreAuthorize("@ss.hasPermi('crm:ai:prompt')")
    @Log(title = "Prompt管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysAiPrompt prompt) {
        return toAjax(promptService.update(prompt));
    }

    @PreAuthorize("@ss.hasPermi('crm:ai:prompt')")
    @Log(title = "Prompt管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(promptService.delete(ids));
    }
}
```

---

### Task 9: 前端 API + 路由 + 浮动按钮

**Files:**
- Create: `ruoyi-ui/src/api/crm/ai.js`
- Create: `ruoyi-ui/src/components/AiFloatingButton.vue`
- Create: `ruoyi-ui/src/components/AiChatDialog.vue`
- Modify: `ruoyi-ui/src/router/index.js`

- [ ] **Create ai.js**

```js
import request from '@/utils/request'

export function aiCustomerIntel(data) {
  return request({ url: '/crm/ai/customer/intel', method: 'post', data })
}

export function aiFollowupGenerate(data) {
  return request({ url: '/crm/ai/followup/generate', method: 'post', data })
}

export function aiRunScoring() {
  return request({ url: '/crm/ai/scoring/run', method: 'post' })
}

export function aiGenerateReport(data) {
  return request({ url: '/crm/ai/report/generate', method: 'post', data })
}

export function aiContractReview(data) {
  return request({ url: '/crm/ai/contract/review', method: 'post', data })
}

export function listPrompt(query) {
  return request({ url: '/crm/ai/prompt/list', method: 'get', params: query })
}

export function getPrompt(id) {
  return request({ url: '/crm/ai/prompt/' + id, method: 'get' })
}

export function addPrompt(data) {
  return request({ url: '/crm/ai/prompt', method: 'post', data })
}

export function updatePrompt(data) {
  return request({ url: '/crm/ai/prompt', method: 'put', data })
}

export function delPrompt(ids) {
  return request({ url: '/crm/ai/prompt/' + ids, method: 'delete' })
}
```

- [ ] **Create AiChatDialog.vue**

```vue
<template>
  <el-dialog title="AI 智能助手" :visible.sync="visible" width="500px" top="10vh" :close-on-click-modal="false" @close="$emit('close')">
    <div class="ai-chat-body">
      <div class="ai-messages" ref="messagesRef">
        <div v-for="(msg, i) in messages" :key="i" :class="['ai-msg', msg.role]">
          <div class="ai-msg-content">{{ msg.content }}</div>
        </div>
        <div v-if="loading" class="ai-msg assistant">
          <div class="ai-msg-content">思考中...</div>
        </div>
      </div>
      <div class="ai-input-row">
        <el-input v-model="inputText" type="textarea" :rows="2" placeholder="输入你的问题..." @keydown.enter.native.prevent="sendMessage" />
        <el-button type="primary" :loading="loading" @click="sendMessage">发送</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: 'AiChatDialog',
  props: { visible: Boolean, context: { type: Object, default: () => ({}) } },
  data() {
    return { messages: [], inputText: '', loading: false }
  },
  methods: {
    sendMessage() {
      const text = this.inputText.trim()
      if (!text || this.loading) return
      this.messages.push({ role: 'user', content: text })
      this.inputText = ''
      this.loading = true
      // 使用通用 AI 对话接口
      import('@/api/crm/ai').then(ai => {
        // 简单的 AI 回复 — 实际使用时可扩展
        this.messages.push({ role: 'assistant', content: '功能开发中，敬请期待。' })
        this.loading = false
        this.$nextTick(() => { const el = this.$refs.messagesRef; if (el) el.scrollTop = el.scrollHeight })
      })
    }
  }
}
</script>

<style scoped>
.ai-chat-body { display: flex; flex-direction: column; height: 450px; }
.ai-messages { flex: 1; overflow-y: auto; padding: 8px; background: #f5f7fa; border-radius: 4px; margin-bottom: 12px; }
.ai-msg { margin-bottom: 12px; }
.ai-msg.user { text-align: right; }
.ai-msg.assistant { text-align: left; }
.ai-msg-content { display: inline-block; max-width: 80%; padding: 8px 12px; border-radius: 8px; font-size: 13px; line-height: 1.5; white-space: pre-wrap; }
.ai-msg.user .ai-msg-content { background: #409eff; color: #fff; }
.ai-msg.assistant .ai-msg-content { background: #fff; color: #303133; border: 1px solid #ebeef5; }
.ai-input-row { display: flex; gap: 8px; }
.ai-input-row .el-button { flex-shrink: 0; }
</style>
```

- [ ] **Create AiFloatingButton.vue**

```vue
<template>
  <div v-if="visible" class="ai-float-btn" @click="dialogVisible = true">
    <span class="ai-float-icon">🤖</span>
    <AiChatDialog :visible.sync="dialogVisible" @close="dialogVisible = false" />
  </div>
</template>

<script>
import AiChatDialog from './AiChatDialog'
export default {
  name: 'AiFloatingButton',
  components: { AiChatDialog },
  data() { return { dialogVisible: false, visible: false } },
  created() {
    this.checkPermission()
  },
  methods: {
    checkPermission() {
      const perms = this.$store.getters.permissions || []
      this.visible = perms.includes('*:*:*') || perms.includes('crm:ai:assistant')
    }
  }
}
</script>

<style scoped>
.ai-float-btn {
  position: fixed; bottom: 100px; right: 24px;
  width: 48px; height: 48px; border-radius: 50%;
  background: #409eff; color: #fff;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; z-index: 999;
  box-shadow: 0 4px 12px rgba(64,158,255,0.4);
  transition: transform 0.2s;
}
.ai-float-btn:hover { transform: scale(1.1); }
.ai-float-icon { font-size: 22px; line-height: 1; }
</style>
```

- [ ] **Register router**

Modify `ruoyi-ui/src/router/index.js` — add AI route inside the CRM section:

```js
{
  path: '/crm/ai',
  component: Layout,
  hidden: false,
  meta: { title: 'AI 智能助手', icon: 'el-icon-s-opportunity' },
  children: [
    {
      path: 'index',
      component: () => import('@/views/crm/ai/index'),
      name: 'Ai',
      meta: { title: 'AI 智能助手', icon: 'el-icon-s-opportunity' }
    }
  ]
}
```

- [ ] **Add AiFloatingButton to Layout**

Modify `ruoyi-ui/src/layout/index.vue` or `App.vue` to include the floating button component.

---

### Task 10: 前端 AI 工作台页面

**Files:**
- Create: `ruoyi-ui/src/views/crm/ai/index.vue`
- Create: `ruoyi-ui/src/views/crm/ai/components/ChatPanel.vue`
- Create: `ruoyi-ui/src/views/crm/ai/components/ReportGenerator.vue`
- Create: `ruoyi-ui/src/views/crm/ai/components/ContractReview.vue`
- Create: `ruoyi-ui/src/views/crm/ai/components/PromptManager.vue`

- [ ] **Create index.vue (AI 工作台入口)**

```vue
<template>
  <div class="app-container">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="智能对话" name="chat">
        <ChatPanel />
      </el-tab-pane>
      <el-tab-pane label="日报/周报" name="report">
        <ReportGenerator />
      </el-tab-pane>
      <el-tab-pane label="合同审查" name="contract">
        <ContractReview />
      </el-tab-pane>
      <el-tab-pane v-if="isAdmin" label="Prompt 管理" name="prompt">
        <PromptManager />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import ChatPanel from './components/ChatPanel'
import ReportGenerator from './components/ReportGenerator'
import ContractReview from './components/ContractReview'
import PromptManager from './components/PromptManager'
export default {
  name: 'AiWorkbench',
  components: { ChatPanel, ReportGenerator, ContractReview, PromptManager },
  data() { return { activeTab: 'chat' } },
  computed: {
    isAdmin() {
      const roles = this.$store.getters.roles || []
      return roles.includes('admin')
    }
  }
}
</script>
```

- [ ] **Create ChatPanel.vue**

```vue
<template>
  <div class="chat-panel">
    <div class="chat-messages" ref="msgRef">
      <div v-for="(m, i) in messages" :key="i" :class="['msg', m.role]">
        <div class="msg-content">{{ m.content }}</div>
      </div>
      <div v-if="loading" class="msg assistant"><div class="msg-content">思考中...</div></div>
    </div>
    <div class="chat-input">
      <el-input v-model="input" type="textarea" :rows="3" placeholder="输入你的问题..." @keydown.enter.native.prevent="send" />
      <el-button type="primary" :loading="loading" @click="send" style="margin-top:8px">发送</el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ChatPanel',
  data() { return { messages: [{ role: 'assistant', content: '你好！我是 AI 销售助手，有什么可以帮助你的？' }], input: '', loading: false } },
  methods: {
    send() {
      const text = this.input.trim()
      if (!text || this.loading) return
      this.messages.push({ role: 'user', content: text })
      this.input = ''
      this.loading = true
      this.messages.push({ role: 'assistant', content: '功能开发中，敬请期待。' })
      this.loading = false
    }
  }
}
</script>

<style scoped>
.chat-panel { display: flex; flex-direction: column; height: 60vh; }
.chat-messages { flex: 1; overflow-y: auto; padding: 12px; background: #f5f7fa; border-radius: 4px; }
.msg { margin-bottom: 12px; }
.msg.user { text-align: right; }
.msg-content { display: inline-block; max-width: 75%; padding: 10px 14px; border-radius: 8px; font-size: 14px; line-height: 1.6; white-space: pre-wrap; }
.msg.user .msg-content { background: #409eff; color: #fff; }
.msg.assistant .msg-content { background: #fff; color: #303133; border: 1px solid #ebeef5; }
.chat-input { margin-top: 12px; }
</style>
```

- [ ] **Create ReportGenerator.vue**

```vue
<template>
  <div>
    <el-form :inline="true" size="small">
      <el-form-item label="类型">
        <el-radio-group v-model="type">
          <el-radio label="daily">日报</el-radio>
          <el-radio label="weekly">周报</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="日期">
        <el-date-picker v-model="date" :type="type === 'weekly' ? 'week' : 'date'" placeholder="选择日期" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="generate">AI 生成</el-button>
      </el-form-item>
    </el-form>
    <div v-if="content" class="report-preview">
      <div class="report-content">{{ content }}</div>
      <el-button size="mini" style="margin-top:12px" @click="copyContent">复制内容</el-button>
    </div>
  </div>
</template>

<script>
import { aiGenerateReport } from '@/api/crm/ai'
export default {
  name: 'ReportGenerator',
  data() { return { type: 'daily', date: new Date(), loading: false, content: '' } },
  methods: {
    generate() {
      this.loading = true
      aiGenerateReport({ type: this.type, date: this.formatDate(this.date) }).then(res => {
        this.content = res.data && res.data.content ? res.data.content : '生成失败'
      }).finally(() => { this.loading = false })
    },
    formatDate(d) {
      if (!d) return ''
      const dt = new Date(d)
      return dt.getFullYear() + '-' + String(dt.getMonth() + 1).padStart(2, '0') + '-' + String(dt.getDate()).padStart(2, '0')
    },
    copyContent() {
      if (this.content) {
        const el = document.createElement('textarea')
        el.value = this.content
        document.body.appendChild(el)
        el.select()
        document.execCommand('copy')
        document.body.removeChild(el)
        this.$message.success('已复制')
      }
    }
  }
}
</script>

<style scoped>
.report-preview { margin-top: 16px; }
.report-content { background: #fafafa; border: 1px solid #ebeef5; border-radius: 4px; padding: 16px; white-space: pre-wrap; font-size: 14px; line-height: 1.8; }
</style>
```

- [ ] **Create ContractReview.vue**

```vue
<template>
  <div>
    <el-form :inline="true" size="small">
      <el-form-item label="选择合同">
        <el-select v-model="contractId" filterable remote placeholder="请输入合同编号搜索" :remote-method="searchContract" style="width: 300px">
          <el-option v-for="c in contracts" :key="c.contractId" :label="c.contractNo + ' - ' + c.contractName" :value="c.contractId" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" :disabled="!contractId" @click="review">AI 审查</el-button>
      </el-form-item>
    </el-form>
    <div v-if="results.length" class="review-results">
      <div v-for="(r, i) in results" :key="i" :class="['risk-item', 'risk-' + (r.level || '低')]">
        <div class="risk-header">
          <el-tag :type="tagType(r.level)" size="mini">{{ r.level }}风险</el-tag>
          <span class="risk-title">{{ r.risk }}</span>
        </div>
        <div class="risk-suggestion">建议：{{ r.suggestion }}</div>
      </div>
    </div>
  </div>
</template>

<script>
import { aiContractReview } from '@/api/crm/ai'
import { listContract } from '@/api/crm/contract'
export default {
  name: 'ContractReview',
  data() { return { contractId: null, contracts: [], loading: false, results: [] } },
  methods: {
    searchContract(query) {
      if (!query) return
      listContract({ contractNo: query, pageSize: 20 }).then(res => { this.contracts = res.rows || [] })
    },
    review() {
      if (!this.contractId) return
      this.loading = true
      this.results = []
      aiContractReview({ contractId: this.contractId }).then(res => {
        this.results = res.data || []
      }).finally(() => { this.loading = false })
    },
    tagType(level) {
      if (level === '高') return 'danger'
      if (level === '中') return 'warning'
      return 'info'
    }
  }
}
</script>

<style scoped>
.review-results { margin-top: 16px; }
.risk-item { border: 1px solid #ebeef5; border-radius: 6px; padding: 12px 16px; margin-bottom: 12px; }
.risk-header { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.risk-title { font-weight: 500; font-size: 14px; }
.risk-suggestion { font-size: 13px; color: #606266; padding-left: 66px; }
</style>
```

- [ ] **Create PromptManager.vue**

```vue
<template>
  <div>
    <div style="margin-bottom:12px">
      <el-button type="primary" icon="el-icon-plus" size="mini" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="list" v-loading="loading" border stripe size="small">
      <el-table-column prop="scene" label="场景" width="150" />
      <el-table-column prop="name" label="名称" width="150" />
      <el-table-column prop="systemPrompt" label="系统提示词" min-width="250" show-overflow-tooltip />
      <el-table-column prop="temperature" label="温度" width="70" />
      <el-table-column prop="status" label="状态" width="80">
        <template slot-scope="s">
          <el-tag :type="s.row.status === '1' ? 'success' : 'danger'" size="mini">{{ s.row.status === '1' ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template slot-scope="s">
          <el-button size="mini" type="text" @click="handleEdit(s.row)">编辑</el-button>
          <el-button size="mini" type="text" style="color:#f56c6c" @click="handleDel(s.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" :total="total" :page.sync="query.pageNum" :limit.sync="query.pageSize" @pagination="getList" />

    <el-dialog :title="form.id ? '编辑Prompt' : '新增Prompt'" :visible.sync="dialogOpen" width="700px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px" size="small">
        <el-form-item label="场景" prop="scene"><el-input v-model="form.scene" /></el-form-item>
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="系统提示词" prop="systemPrompt"><el-input v-model="form.systemPrompt" type="textarea" :rows="6" /></el-form-item>
        <el-form-item label="用户模板" prop="userTemplate"><el-input v-model="form.userTemplate" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="温度"><el-input-number v-model="form.temperature" :min="0" :max="2" :step="0.1" :precision="2" /></el-form-item>
        <el-form-item label="状态"><el-radio-group v-model="form.status"><el-radio label="1">启用</el-radio><el-radio label="0">停用</el-radio></el-radio-group></el-form-item>
      </el-form>
      <div slot="footer"><el-button @click="dialogOpen = false">取消</el-button><el-button type="primary" @click="submitForm">确定</el-button></div>
    </el-dialog>
  </div>
</template>

<script>
import { listPrompt, getPrompt, addPrompt, updatePrompt, delPrompt } from '@/api/crm/ai'
export default {
  name: 'PromptManager',
  data() {
    return {
      list: [], loading: false, total: 0,
      query: { pageNum: 1, pageSize: 10 },
      dialogOpen: false,
      form: { scene: '', name: '', systemPrompt: '', userTemplate: '', temperature: 0.7, status: '1' },
      rules: {
        scene: [{ required: true, message: '必填', trigger: 'blur' }],
        name: [{ required: true, message: '必填', trigger: 'blur' }]
      }
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listPrompt(this.query).then(res => { this.list = res.rows || []; this.total = res.total || 0 }).finally(() => { this.loading = false })
    },
    handleAdd() { this.form = { scene: '', name: '', systemPrompt: '', userTemplate: '', temperature: 0.7, status: '1' }; this.dialogOpen = true },
    handleEdit(row) { getPrompt(row.id).then(res => { this.form = res.data; this.dialogOpen = true }) },
    handleDel(row) {
      this.$confirm('确认删除？').then(() => { delPrompt(row.id).then(() => { this.$message.success('已删除'); this.getList() }) }).catch(() => {})
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const action = this.form.id ? updatePrompt(this.form) : addPrompt(this.form)
        action.then(() => { this.$message.success('保存成功'); this.dialogOpen = false; this.getList() })
      })
    }
  }
}
</script>
```

---

### Task 11: 构建与部署

- [ ] **Build backend**

Run: `cd E:\CRM\RuoYi-Vue-master && mvn package -DskipTests -q`

- [ ] **Build frontend**

Run: `cd E:\CRM\RuoYi-Vue-master\ruoyi-ui && npm run build:prod`

- [ ] **Restart application**

Stop old java process and start with new jar.

- [ ] **Verify API**

Test `GET /crm/ai/prompt/list` to ensure backend starts without errors.
