package com.ruoyi.crm.ai.copilot.mapper;

import com.ruoyi.crm.ai.copilot.session.CopilotSession;
import java.util.List;

public interface CopilotSessionMapper {
    CopilotSession selectBySessionId(String sessionId);
    List<CopilotSession> selectByUserId(Long userId);
    int insert(CopilotSession session);
    int updateContext(CopilotSession session);
    int updateTitle(CopilotSession session);
    int deleteBySessionId(String sessionId);
    int deleteByUserId(Long userId);
}
