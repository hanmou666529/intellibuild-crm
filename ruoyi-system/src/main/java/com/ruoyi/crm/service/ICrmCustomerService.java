package com.ruoyi.crm.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.crm.domain.CrmCustomer;

public interface ICrmCustomerService
{
    public List<CrmCustomer> selectCrmCustomerList(CrmCustomer customer);

    public CrmCustomer selectCrmCustomerById(Long customerId);

    public int insertCrmCustomer(CrmCustomer customer);

    public int updateCrmCustomer(CrmCustomer customer);

    public int deleteCrmCustomerByIds(Long[] customerIds);

    public boolean checkPhoneUnique(CrmCustomer customer);

    public boolean checkCustomerNameUnique(CrmCustomer customer);

    public int assignCustomer(CrmCustomer customer);

    public int putToPool(CrmCustomer customer);

    public int claimFromPool(Long customerId);

    public String importCustomer(List<CrmCustomer> customerList, boolean updateSupport, String operName);

    public int addToPipeline(Long customerId);

    public Map<String, Object> batchAssignCustomers(Long[] customerIds, Long targetUserId, boolean force);

    public List<Map<String, Object>> checkBatchAssignCustomers(Long[] customerIds);

    public List<CrmCustomer> checkDuplicate(CrmCustomer customer);

    public void mergeCustomer(Long keepCustomerId, Long mergeCustomerId);
}
