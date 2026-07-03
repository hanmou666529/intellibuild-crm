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
