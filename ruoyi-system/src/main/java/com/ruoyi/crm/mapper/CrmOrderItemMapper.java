package com.ruoyi.crm.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.crm.domain.CrmOrderItem;

public interface CrmOrderItemMapper
{
    public List<CrmOrderItem> selectCrmOrderItemByOrderId(Long orderId);

    public int insertCrmOrderItem(CrmOrderItem orderItem);

    public int insertCrmOrderItems(List<CrmOrderItem> orderItems);

    public int updateCrmOrderItem(CrmOrderItem orderItem);

    public int deleteCrmOrderItemByOrderId(Long orderId);

    public int deleteCrmOrderItemByIds(Long[] itemIds);

    public List<Map<String, Object>> countTopProducts(@Param("limit") int limit);

    public List<Map<String, Object>> selectByProductName(@Param("productName") String productName);
}
