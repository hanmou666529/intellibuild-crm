package com.ruoyi.crm.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.crm.domain.CrmCustomer;

public interface CrmCustomerMapper
{
    public List<CrmCustomer> selectCrmCustomerList(CrmCustomer customer);

    public CrmCustomer selectCrmCustomerById(Long customerId);

    public CrmCustomer checkPhoneUnique(String phone);

    public CrmCustomer checkCustomerNameUnique(String customerName);

    public int insertCrmCustomer(CrmCustomer customer);

    public int updateCrmCustomer(CrmCustomer customer);

    public int deleteCrmCustomerByIds(Long[] customerIds);

    public int updateCustomerBelong(CrmCustomer customer);

    public int putToPool(CrmCustomer customer);

    public int claimFromPool(CrmCustomer customer);

    public Integer countTotal(CrmCustomer customer);

    public Integer countTodayNew(CrmCustomer customer);

    public Integer countPendingFollowup(CrmCustomer customer);

    public List<Map<String, Object>> countGroupBySource(CrmCustomer customer);

    public List<Map<String, Object>> countGroupByLevel(CrmCustomer customer);

    public List<Map<String, Object>> countMonthNewCustomers(CrmCustomer customer);

    List<CrmCustomer> checkDuplicate(CrmCustomer customer);

    int deleteByMerge(@Param("customerId") Long customerId, @Param("mergeToId") Long mergeToId);
}
