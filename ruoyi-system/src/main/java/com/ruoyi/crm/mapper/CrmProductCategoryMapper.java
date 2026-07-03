package com.ruoyi.crm.mapper;

import java.util.List;
import com.ruoyi.crm.domain.CrmProductCategory;

public interface CrmProductCategoryMapper
{
    public List<CrmProductCategory> selectCrmProductCategoryList(CrmProductCategory category);

    public CrmProductCategory selectCrmProductCategoryById(Long categoryId);

    public int insertCrmProductCategory(CrmProductCategory category);

    public int updateCrmProductCategory(CrmProductCategory category);

    public int deleteCrmProductCategoryById(Long categoryId);

    public int deleteCrmProductCategoryByIds(Long[] categoryIds);
}
