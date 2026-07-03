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
