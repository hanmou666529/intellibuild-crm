package com.ruoyi.crm.ai.core;

import java.util.Map;

public interface AiService {
    String chat(String systemPrompt, String userMessage);
    String chatWithContext(String systemPrompt, String jsonContext, String userInstruction);
    Map<String, Object> chatWithDetail(String systemPrompt, String userMessage);
}
