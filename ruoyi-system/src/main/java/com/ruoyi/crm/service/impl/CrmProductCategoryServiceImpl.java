package com.ruoyi.crm.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.crm.domain.CrmProductCategory;
import com.ruoyi.crm.mapper.CrmProductCategoryMapper;
import com.ruoyi.crm.service.ICrmProductCategoryService;

@Service
public class CrmProductCategoryServiceImpl implements ICrmProductCategoryService
{
    @Autowired
    private CrmProductCategoryMapper crmProductCategoryMapper;

    @Override
    public List<CrmProductCategory> selectCrmProductCategoryList(CrmProductCategory category)
    {
        return crmProductCategoryMapper.selectCrmProductCategoryList(category);
    }

    @Override
    public CrmProductCategory selectCrmProductCategoryById(Long categoryId)
    {
        return crmProductCategoryMapper.selectCrmProductCategoryById(categoryId);
    }

    @Override
    @Transactional
    public int insertCrmProductCategory(CrmProductCategory category)
    {
        category.setCreateBy(SecurityUtils.getUsername());
        category.setCreateTime(DateUtils.getNowDate());
        return crmProductCategoryMapper.insertCrmProductCategory(category);
    }

    @Override
    @Transactional
    public int updateCrmProductCategory(CrmProductCategory category)
    {
        category.setUpdateBy(SecurityUtils.getUsername());
        category.setUpdateTime(DateUtils.getNowDate());
        return crmProductCategoryMapper.updateCrmProductCategory(category);
    }

    @Override
    @Transactional
    public int deleteCrmProductCategoryByIds(Long[] categoryIds)
    {
        return crmProductCategoryMapper.deleteCrmProductCategoryByIds(categoryIds);
    }
}
