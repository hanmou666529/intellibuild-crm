package com.ruoyi.crm.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.crm.domain.CrmCustomerShare;

public interface CrmCustomerShareMapper
{
    public List<CrmCustomerShare> selectCrmCustomerShareList(CrmCustomerShare share);

    public List<CrmCustomerShare> selectByCustomerId(Long customerId);

    public List<Long> selectSharedCustomerIdsByUser(Long userId);

    public List<Long> selectSharedCustomerIdsByDept(Long deptId);

    public int insertCrmCustomerShare(CrmCustomerShare share);

    public int deleteCrmCustomerShareById(Long shareId);

    public int deleteByCustomerId(Long customerId);

    int updateCustomerId(@Param("oldId") Long oldId, @Param("newId") Long newId);

    public List<CrmCustomerShare> selectDeptPendingShares(@Param("deptId") Long deptId);
}
