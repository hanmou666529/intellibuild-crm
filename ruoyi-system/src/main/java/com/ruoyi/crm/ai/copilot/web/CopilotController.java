package com.ruoyi.crm.ai.copilot.web;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.crm.ai.config.AiConfig;
import com.ruoyi.crm.ai.copilot.session.CopilotSession;
import com.ruoyi.crm.ai.copilot.session.CopilotSessionManager;
import com.ruoyi.crm.ai.copilot.tool.CopilotToolExecutor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/crm/ai/copilot")
public class CopilotController extends BaseController {
    private final RestTemplate restTemplate;
    private final AiConfig aiConfig;
    private final CopilotSessionManager sessionManager;
    private final CopilotToolExecutor toolExecutor;
    private final ExecutorService asyncExecutor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "copilot-stream");
        t.setDaemon(true);
        return t;
    });

    public CopilotController(@Qualifier("aiRestTemplate") RestTemplate restTemplate, AiConfig aiConfig,
                             CopilotSessionManager sessionManager,
                             CopilotToolExecutor toolExecutor) {
        this.restTemplate = restTemplate;
        this.aiConfig = aiConfig;
        this.sessionManager = sessionManager;
        this.toolExecutor = toolExecutor;
    }

    @GetMapping("/chat/stream")
    public SseEmitter stream(@RequestParam String sessionId,
                             @RequestParam String message) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long userId = loginUser.getUserId();
        String username = loginUser.getUsername();

        SseEmitter emitter = new SseEmitter(300000L);

        asyncExecutor.execute(() -> {
            try {
                CopilotSession session = sessionManager.getOrCreate(sessionId, userId);
                String sid = session.getSessionId();

                sessionManager.appendMessage(sid, "user", message);

                JSONArray context = sessionManager.getContext(sid);
                List<Map<String, Object>> messages = new ArrayList<>();

                Map<String, Object> sysMsg = new LinkedHashMap<>();
                sysMsg.put("role", "system");
                sysMsg.put("content", buildSystemPrompt(username));
                messages.add(sysMsg);

                for (int i = 0; i < context.size(); i++) {
                    JSONArray msg = context.getJSONArray(i);
                    if (msg.size() >= 2) {
                        Map<String, Object> ctxMsg = new LinkedHashMap<>();
                        ctxMsg.put("role", msg.getString(0));
                        ctxMsg.put("content", msg.getString(1));
                        messages.add(ctxMsg);
                    }
                }

                Map<String, Object> userMsg = new LinkedHashMap<>();
                userMsg.put("role", "user");
                userMsg.put("content", message);
                messages.add(userMsg);

                String fullResponse = doStreamChat(messages, emitter, sid);

                if (fullResponse != null) {
                    sessionManager.appendMessage(sid, "assistant", fullResponse);
                }

                emitter.send(SseEmitter.event().name("message").data("{\"type\":\"done\"}"));
                emitter.complete();
            } catch (Exception e) {
                try {
                    String errorMsg = e.getMessage() != null ? e.getMessage().replace("\"", "'") : "未知错误";
                    emitter.send(SseEmitter.event().name("message")
                        .data("{\"type\":\"error\",\"content\":\"" + errorMsg + "\"}"));
                } catch (Exception ignored) {}
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    private String doStreamChat(List<Map<String, Object>> messages,
                                 SseEmitter emitter, String sessionId) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiConfig.getModel());
        requestBody.put("stream", true);
        requestBody.put("messages", messages);
        requestBody.put("tools", toolExecutor.getToolDefinitions());
        requestBody.put("temperature", aiConfig.getTemperature());
        requestBody.put("max_tokens", aiConfig.getMaxTokens());

        StringBuilder contentAccumulator = new StringBuilder();
        StringBuilder toolCallAccumulator = new StringBuilder();
        String[] toolName = {null};
        String[] toolId = {null};
        boolean[] hasToolCall = {false};

        String jsonBody = JSON.toJSONString(requestBody);

        String urlStr = aiConfig.getBaseUrl() + "/chat/completions";
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + aiConfig.getApiKey());
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(aiConfig.getTimeout());
        conn.setReadTimeout(aiConfig.getTimeout());

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data: ")) {
                    String data = line.substring(6).trim();
                    if ("[DONE]".equals(data)) break;

                    try {
                        JSONObject json = JSON.parseObject(data);
                        JSONObject delta = json.getJSONArray("choices")
                            .getJSONObject(0).getJSONObject("delta");

                        String content = delta.getString("content");

                        JSONArray toolCalls = delta.getJSONArray("tool_calls");
                        if (toolCalls != null && !toolCalls.isEmpty()) {
                            hasToolCall[0] = true;
                            for (int i = 0; i < toolCalls.size(); i++) {
                                JSONObject tc = toolCalls.getJSONObject(i);
                                if (tc.containsKey("function")) {
                                    JSONObject func = tc.getJSONObject("function");
                                    if (func.containsKey("name") && func.getString("name") != null) {
                                        toolName[0] = func.getString("name");
                                    }
                                    if (func.containsKey("arguments")) {
                                        toolCallAccumulator.append(func.getString("arguments"));
                                    }
                                }
                                if (tc.containsKey("id")) {
                                    toolId[0] = tc.getString("id");
                                }
                            }
                        }

                        if (content != null && !content.isEmpty()) {
                            contentAccumulator.append(content);
                            CopilotResponse resp = new CopilotResponse("token", content);
                            emitter.send(SseEmitter.event().name("message")
                                .data(JSON.toJSONString(resp)));
                        }

                    } catch (Exception ignored) {}
                }
            }
        }

        if (hasToolCall[0] && toolName[0] != null && toolCallAccumulator.length() > 0) {
            String arguments = toolCallAccumulator.toString();
            String name = toolName[0];
            String id = toolId[0];
            CopilotResponse toolResp = new CopilotResponse("tool_start",
                "正在查询" + describeTool(name) + "...");
            emitter.send(SseEmitter.event().name("message").data(JSON.toJSONString(toolResp)));

            String result = toolExecutor.execute(name, arguments);

            Map<String, Object> assistantMsg = new LinkedHashMap<>();
            assistantMsg.put("role", "assistant");
            assistantMsg.put("content", null);
            assistantMsg.put("tool_calls", Collections.singletonList(
                buildToolCall(id, name, arguments)
            ));
            messages.add(assistantMsg);

            Map<String, Object> toolMsg = new LinkedHashMap<>();
            toolMsg.put("role", "tool");
            toolMsg.put("content", result);
            toolMsg.put("tool_call_id", id != null ? id : "call_1");
            messages.add(toolMsg);

            String followUpContent = doFollowUpChat(messages, emitter);
            return followUpContent;
        }

        return contentAccumulator.length() > 0 ? contentAccumulator.toString() : null;
    }

    private Map<String, Object> buildToolCall(String id, String name, String arguments) {
        Map<String, Object> function = new LinkedHashMap<>();
        function.put("name", name);
        function.put("arguments", arguments);
        Map<String, Object> toolCall = new LinkedHashMap<>();
        toolCall.put("id", id != null ? id : "call_1");
        toolCall.put("type", "function");
        toolCall.put("function", function);
        return toolCall;
    }

    private String doFollowUpChat(List<Map<String, Object>> messages,
                                   SseEmitter emitter) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiConfig.getModel());
        requestBody.put("stream", true);
        requestBody.put("messages", messages);
        requestBody.put("temperature", aiConfig.getTemperature());
        requestBody.put("max_tokens", aiConfig.getMaxTokens());

        StringBuilder contentAccumulator = new StringBuilder();

        String jsonBody = JSON.toJSONString(requestBody);
        String urlStr = aiConfig.getBaseUrl() + "/chat/completions";
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + aiConfig.getApiKey());
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(aiConfig.getTimeout());
        conn.setReadTimeout(aiConfig.getTimeout());

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data: ")) {
                    String data = line.substring(6).trim();
                    if ("[DONE]".equals(data)) break;
                    try {
                        JSONObject json = JSON.parseObject(data);
                        JSONObject delta = json.getJSONArray("choices")
                            .getJSONObject(0).getJSONObject("delta");
                        String content = delta.getString("content");
                        if (content != null && !content.isEmpty()) {
                            contentAccumulator.append(content);
                            CopilotResponse resp = new CopilotResponse("token", content);
                            emitter.send(SseEmitter.event().name("message")
                                .data(JSON.toJSONString(resp)));
                        }
                    } catch (Exception ignored) {}
                }
            }
        }

        return contentAccumulator.toString();
    }

    private String buildSystemPrompt(String username) {
        return "你是 CRM 系统的 AI 销售助手，精通客户关系管理。你的职责是帮助销售人员高效管理客户、跟进、订单和合同。\n\n"
            + "当前用户: " + username + "\n\n"
            + "你可以使用以下工具查询数据:\n"
            + "1. query_customer - 搜索客户（名称/手机号）\n"
            + "2. query_order - 查询订单（客户名称/日期/状态）\n"
            + "3. query_contract - 查询合同（客户名称/状态）\n"
            + "4. query_followup - 查询跟进记录（客户名称）\n"
            + "5. query_dashboard - 销售数据统计\n\n"
            + "回答要求：\n"
            + "- 使用友好的语气，简洁明了\n"
            + "- 涉及数据时，优先使用工具查询实时数据\n"
            + "- 数据用表格或列表呈现，清晰易读\n"
            + "- 可以主动给出建议和洞察\n"
            + "- 注意数据安全和权限";
    }

    private String describeTool(String toolName) {
        Map<String, String> desc = new LinkedHashMap<>();
        desc.put("query_customer", "客户信息");
        desc.put("query_order", "订单数据");
        desc.put("query_contract", "合同数据");
        desc.put("query_followup", "跟进记录");
        desc.put("query_dashboard", "销售统计");
        return desc.getOrDefault(toolName, "数据");
    }

    static class CopilotResponse {
        private String type;
        private String content;
        public CopilotResponse() {}
        public CopilotResponse(String type, String content) { this.type = type; this.content = content; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    @GetMapping("/session/list")
    public AjaxResult listSessions() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        List<Map<String, Object>> result = new ArrayList<>();
        for (CopilotSession s : sessionManager.listSessions(loginUser.getUserId())) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("sessionId", s.getSessionId());
            item.put("title", s.getTitle());
            item.put("updateTime", s.getUpdateTime());
            result.add(item);
        }
        return AjaxResult.success(result);
    }

    @PostMapping("/session/create")
    public AjaxResult createSession() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        CopilotSession session = sessionManager.getOrCreate(null, loginUser.getUserId());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("sessionId", session.getSessionId());
        return AjaxResult.success(data);
    }

    @DeleteMapping("/session/{sessionId}")
    public AjaxResult deleteSession(@PathVariable String sessionId) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        sessionManager.deleteSession(sessionId, loginUser.getUserId());
        return AjaxResult.success();
    }
}
