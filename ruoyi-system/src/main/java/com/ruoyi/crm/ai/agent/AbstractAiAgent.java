package com.ruoyi.crm.ai.agent;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.crm.ai.core.AiService;
import com.ruoyi.crm.ai.prompt.domain.SysAiPrompt;
import com.ruoyi.crm.ai.prompt.service.PromptService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

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
        String userInput;
        if (userMessage != null) {
            userInput = userMessage;
        } else if (prompt.getUserTemplate() != null) {
            String template = prompt.getUserTemplate();
            template = template.replace("{data}", contextJson).replace("{context}", contextJson);
            try {
                Map<String, Object> ctx = JSON.parseObject(contextJson);
                for (Map.Entry<String, Object> entry : ctx.entrySet()) {
                    String val = entry.getValue() instanceof String
                        ? (String) entry.getValue()
                        : JSON.toJSONString(entry.getValue());
                    template = template.replace("{" + entry.getKey() + "}", val);
                }
            } catch (Exception ignored) { }
            userInput = template;
        } else {
            userInput = contextJson;
        }
        return aiService.chat(prompt.getSystemPrompt(), userInput);
    }
}
