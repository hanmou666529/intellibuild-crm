package com.ruoyi.crm.mapper;

import java.util.List;
import com.ruoyi.crm.domain.CrmProduct;

public interface CrmProductMapper
{
    public List<CrmProduct> selectCrmProductList(CrmProduct product);

    public CrmProduct selectCrmProductById(Long productId);

    public int insertCrmProduct(CrmProduct product);

    public int updateCrmProduct(CrmProduct product);

    public int deleteCrmProductByIds(Long[] productIds);
}
