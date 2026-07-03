package com.ruoyi.crm.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.crm.domain.CrmContract;
import com.ruoyi.crm.domain.CrmPipeline;
import com.ruoyi.crm.mapper.CrmContractMapper;
import com.ruoyi.crm.mapper.CrmPipelineMapper;
import com.ruoyi.crm.service.ICrmPipelineService;

@Service
public class CrmPipelineServiceImpl implements ICrmPipelineService
{
    @Autowired
    private CrmPipelineMapper crmPipelineMapper;

    @Autowired
    private CrmContractMapper crmContractMapper;

    @Override
    public List<CrmPipeline> selectCrmPipelineList(CrmPipeline pipeline)
    {
        return crmPipelineMapper.selectCrmPipelineList(pipeline);
    }

    @Override
    public CrmPipeline selectCrmPipelineById(Long pipelineId)
    {
        return crmPipelineMapper.selectCrmPipelineById(pipelineId);
    }

    @Override
    @Transactional
    public int insertCrmPipeline(CrmPipeline pipeline)
    {
        pipeline.setCreateBy(SecurityUtils.getUsername());
        pipeline.setCreateTime(DateUtils.getNowDate());
        return crmPipelineMapper.insertCrmPipeline(pipeline);
    }

    @Override
    @Transactional
    public int updateCrmPipeline(CrmPipeline pipeline)
    {
        pipeline.setUpdateBy(SecurityUtils.getUsername());
        pipeline.setUpdateTime(DateUtils.getNowDate());
        int result = crmPipelineMapper.updateCrmPipeline(pipeline);
        createContractOnDeal(pipeline);
        return result;
    }

    @Override
    @Transactional
    public int updateStage(CrmPipeline pipeline)
    {
        pipeline.setUpdateBy(SecurityUtils.getUsername());
        pipeline.setUpdateTime(DateUtils.getNowDate());
        int result = crmPipelineMapper.updateStage(pipeline);
        createContractOnDeal(pipeline);
        return result;
    }

    private void createContractOnDeal(CrmPipeline pipeline)
    {
        if (!"deal".equals(pipeline.getStage()))
        {
            return;
        }
        CrmPipeline full = crmPipelineMapper.selectCrmPipelineById(pipeline.getPipelineId());
        if (full == null || full.getCustomerId() == null || full.getAmount() == null)
        {
            return;
        }
        CrmContract contract = new CrmContract();
        contract.setCustomerId(full.getCustomerId());
        contract.setAmount(full.getAmount().doubleValue());
        contract.setContractName(full.getCustomerName() != null ? full.getCustomerName() + "-销售合同" : "销售合同");
        contract.setSignDate(new Date());
        contract.setStartDate(new Date());
        contract.setStatus("0");
        contract.setCreateBy(SecurityUtils.getUsername());
        contract.setCreateTime(DateUtils.getNowDate());
        crmContractMapper.insertCrmContract(contract);
    }

    @Override
    @Transactional
    public int deleteCrmPipelineByIds(Long[] pipelineIds)
    {
        return crmPipelineMapper.deleteCrmPipelineByIds(pipelineIds);
    }
}
