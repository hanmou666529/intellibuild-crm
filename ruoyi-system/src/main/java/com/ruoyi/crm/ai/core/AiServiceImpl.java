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
