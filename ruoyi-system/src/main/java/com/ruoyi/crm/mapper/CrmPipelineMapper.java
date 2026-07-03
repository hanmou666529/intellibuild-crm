package com.ruoyi.crm.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.crm.domain.CrmPipeline;

public interface CrmPipelineMapper
{
    public List<CrmPipeline> selectCrmPipelineList(CrmPipeline pipeline);

    public CrmPipeline selectCrmPipelineById(Long pipelineId);

    public CrmPipeline selectPipelineByCustomerId(Long customerId);

    public int insertCrmPipeline(CrmPipeline pipeline);

    public int updateCrmPipeline(CrmPipeline pipeline);

    public int updateStage(CrmPipeline pipeline);

    public int deleteCrmPipelineByIds(Long[] pipelineIds);

    public List<Map<String, Object>> countGroupByStage();

    int updateCustomerId(@Param("oldId") Long oldId, @Param("newId") Long newId);
}
