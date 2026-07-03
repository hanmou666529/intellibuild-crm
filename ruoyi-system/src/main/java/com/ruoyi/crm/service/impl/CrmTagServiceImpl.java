package com.ruoyi.crm.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.crm.domain.CrmTag;
import com.ruoyi.crm.mapper.CrmTagMapper;
import com.ruoyi.crm.service.ICrmTagService;

@Service
public class CrmTagServiceImpl implements ICrmTagService
{
    @Autowired
    private CrmTagMapper crmTagMapper;

    @Override
    public List<CrmTag> selectCrmTagList(CrmTag tag)
    {
        return crmTagMapper.selectCrmTagList(tag);
    }

    @Override
    public CrmTag selectCrmTagById(Long tagId)
    {
        return crmTagMapper.selectCrmTagById(tagId);
    }

    @Override
    @Transactional
    public int insertCrmTag(CrmTag tag)
    {
        tag.setCreateBy(SecurityUtils.getUsername());
        tag.setCreateTime(DateUtils.getNowDate());
        if (tag.getColor() == null) tag.setColor("#1890ff");
        if (tag.getStatus() == null) tag.setStatus("0");
        return crmTagMapper.insertCrmTag(tag);
    }

    @Override
    @Transactional
    public int updateCrmTag(CrmTag tag)
    {
        tag.setUpdateBy(SecurityUtils.getUsername());
        tag.setUpdateTime(DateUtils.getNowDate());
        return crmTagMapper.updateCrmTag(tag);
    }

    @Override
    @Transactional
    public int deleteCrmTagByIds(Long[] tagIds)
    {
        return crmTagMapper.deleteCrmTagByIds(tagIds);
    }

    @Override
    public List<CrmTag> selectActiveTags()
    {
        return crmTagMapper.selectCrmTagByType(null);
    }
}
