package com.ruoyi.crm.service;

import java.util.List;
import com.ruoyi.crm.domain.CrmPipeline;

public interface ICrmPipelineService
{
    public List<CrmPipeline> selectCrmPipelineList(CrmPipeline pipeline);

    public CrmPipeline selectCrmPipelineById(Long pipelineId);

    public int insertCrmPipeline(CrmPipeline pipeline);

    public int updateCrmPipeline(CrmPipeline pipeline);

    public int updateStage(CrmPipeline pipeline);

    public int deleteCrmPipelineByIds(Long[] pipelineIds);
}
