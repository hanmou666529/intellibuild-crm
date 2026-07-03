package com.ruoyi.crm.mapper;

import java.util.List;
import org.springframework.stereotype.Repository;
import com.ruoyi.crm.domain.CrmCustomerDispute;

@Repository
public interface CrmCustomerDisputeMapper {
    List<CrmCustomerDispute> selectList(CrmCustomerDispute dispute);
    CrmCustomerDispute selectById(Long disputeId);
    int insert(CrmCustomerDispute dispute);
    int update(CrmCustomerDispute dispute);
    int closeByCustomerId(Long customerId);
}
