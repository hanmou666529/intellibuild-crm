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
