package com.ruoyi.crm.mapper;

import java.util.List;
import org.springframework.stereotype.Repository;
import com.ruoyi.crm.domain.CrmCustomerMergeLog;

@Repository
public interface CrmCustomerMergeLogMapper {
    int insert(CrmCustomerMergeLog log);
    List<CrmCustomerMergeLog> selectList();
}
