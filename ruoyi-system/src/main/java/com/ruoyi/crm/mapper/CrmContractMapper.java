package com.ruoyi.crm.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.crm.domain.CrmContract;

public interface CrmContractMapper
{
    public List<CrmContract> selectCrmContractList(CrmContract contract);

    public CrmContract selectCrmContractById(Long contractId);

    public int insertCrmContract(CrmContract contract);

    public int updateCrmContract(CrmContract contract);

    public int deleteCrmContractByIds(Long[] contractIds);

    public int countUnpaidByContractId(Long contractId);

    public List<CrmContract> selectExpiredContracts();

    public void updateContractBelongByCustomerId(@Param("customerId") Long customerId, @Param("belongUserId") Long belongUserId, @Param("belongDeptId") Long belongDeptId);

    int updateCustomerId(@Param("oldId") Long oldId, @Param("newId") Long newId);

    List<Map<String, Object>> countContractByStatus(CrmContract contract);

    Integer countPendingApproval(CrmContract contract);

    List<Map<String, Object>> sumMonthAmount(CrmContract contract);
}
