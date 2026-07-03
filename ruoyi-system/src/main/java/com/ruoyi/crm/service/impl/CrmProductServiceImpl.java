package com.ruoyi.crm.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.crm.domain.CrmProduct;
import com.ruoyi.crm.mapper.CrmProductMapper;
import com.ruoyi.crm.service.ICrmProductService;

@Service
public class CrmProductServiceImpl implements ICrmProductService
{
    @Autowired
    private CrmProductMapper crmProductMapper;

    @Override
    public List<CrmProduct> selectCrmProductList(CrmProduct product)
    {
        return crmProductMapper.selectCrmProductList(product);
    }

    @Override
    public CrmProduct selectCrmProductById(Long productId)
    {
        return crmProductMapper.selectCrmProductById(productId);
    }

    @Override
    @Transactional
    public int insertCrmProduct(CrmProduct product)
    {
        product.setCreateBy(SecurityUtils.getUsername());
        product.setCreateTime(DateUtils.getNowDate());
        return crmProductMapper.insertCrmProduct(product);
    }

    @Override
    @Transactional
    public int updateCrmProduct(CrmProduct product)
    {
        product.setUpdateBy(SecurityUtils.getUsername());
        product.setUpdateTime(DateUtils.getNowDate());
        return crmProductMapper.updateCrmProduct(product);
    }

    @Override
    @Transactional
    public int deleteCrmProductByIds(Long[] productIds)
    {
        return crmProductMapper.deleteCrmProductByIds(productIds);
    }
}
