package com.ruoyi.crm.service;

import java.util.List;
import com.ruoyi.crm.domain.CrmProductCategory;

public interface ICrmProductCategoryService
{
    public List<CrmProductCategory> selectCrmProductCategoryList(CrmProductCategory category);

    public CrmProductCategory selectCrmProductCategoryById(Long categoryId);

    public int insertCrmProductCategory(CrmProductCategory category);

    public int updateCrmProductCategory(CrmProductCategory category);

    public int deleteCrmProductCategoryByIds(Long[] categoryIds);
}
