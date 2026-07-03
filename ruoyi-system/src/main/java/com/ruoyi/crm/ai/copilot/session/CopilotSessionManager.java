package com.ruoyi.crm.ai.copilot.session;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.ruoyi.crm.ai.copilot.mapper.CopilotSessionMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class CopilotSessionManager {
    private final CopilotSessionMapper mapper;

    public CopilotSessionManager(CopilotSessionMapper mapper) {
        this.mapper = mapper;
    }

    public CopilotSession getSession(String sessionId) {
        return mapper.selectBySessionId(sessionId);
    }

    public List<CopilotSession> listSessions(Long userId) {
        return mapper.selectByUserId(userId);
    }

    @Transactional
    public CopilotSession getOrCreate(String sessionId, Long userId) {
        if (sessionId != null) {
            CopilotSession session = mapper.selectBySessionId(sessionId);
            if (session != null && session.getUserId().equals(userId)) {
                return session;
            }
        }
        CopilotSession session = new CopilotSession();
        session.setSessionId(UUID.randomUUID().toString().replace("-", ""));
        session.setUserId(userId);
        session.setTitle("新对话");
        session.setContext("[]");
        session.setCreateTime(new Date());
        session.setUpdateTime(new Date());
        mapper.insert(session);
        return session;
    }

    @Transactional
    public void appendMessage(String sessionId, String role, String content) {
        CopilotSession session = mapper.selectBySessionId(sessionId);
        if (session == null) return;

        JSONArray context;
        try {
            context = JSON.parseArray(session.getContext());
        } catch (Exception e) {
            context = new JSONArray();
        }

        JSONArray message = new JSONArray();
        message.add(role);
        message.add(content);
        context.add(message);

        if (context.size() > 20) {
            context = new JSONArray(context.subList(context.size() - 20, context.size()));
        }

        session.setContext(JSON.toJSONString(context));
        session.setUpdateTime(new Date());
        mapper.updateContext(session);

        if ("user".equals(role) && context.size() <= 2) {
            String title = content.length() > 40 ? content.substring(0, 40) + "..." : content;
            title = title.replaceAll("[\\n\\r]", " ");
            session.setTitle(title);
            mapper.updateTitle(session);
        }
    }

    @Transactional
    public void deleteSession(String sessionId, Long userId) {
        CopilotSession session = mapper.selectBySessionId(sessionId);
        if (session != null && session.getUserId().equals(userId)) {
            mapper.deleteBySessionId(sessionId);
        }
    }

    public JSONArray getContext(String sessionId) {
        CopilotSession session = mapper.selectBySessionId(sessionId);
        if (session == null) return new JSONArray();
        try {
            return JSON.parseArray(session.getContext());
        } catch (Exception e) {
            return new JSONArray();
        }
    }
}
