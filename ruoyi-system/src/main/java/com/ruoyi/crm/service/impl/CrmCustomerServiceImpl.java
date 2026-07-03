package com.ruoyi.crm.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.crm.domain.CrmCustomer;
import com.ruoyi.crm.domain.CrmCustomerPool;
import com.ruoyi.crm.domain.CrmPipeline;
import com.ruoyi.crm.mapper.CrmContractMapper;
import com.ruoyi.crm.mapper.CrmCustomerDisputeMapper;
import com.ruoyi.crm.mapper.CrmCustomerMapper;
import com.ruoyi.crm.mapper.CrmCustomerMergeLogMapper;
import com.ruoyi.crm.mapper.CrmCustomerPoolMapper;
import com.ruoyi.crm.mapper.CrmFollowupMapper;
import com.ruoyi.crm.mapper.CrmOrderMapper;
import com.ruoyi.crm.mapper.CrmPaymentPlanMapper;
import com.ruoyi.crm.mapper.CrmPipelineMapper;
import com.ruoyi.crm.domain.CrmCustomerDispute;
import com.ruoyi.crm.domain.CrmCustomerMergeLog;
import com.ruoyi.crm.service.ICrmCustomerDisputeService;
import com.ruoyi.crm.service.ICrmCustomerService;
import com.ruoyi.crm.service.ICrmPipelineService;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.mapper.SysUserMapper;

@Service
public class CrmCustomerServiceImpl implements ICrmCustomerService
{
    private static final Logger log = LoggerFactory.getLogger(CrmCustomerServiceImpl.class);

    @Autowired
    private CrmCustomerMapper crmCustomerMapper;

    @Autowired
    private CrmCustomerPoolMapper crmCustomerPoolMapper;

    @Autowired
    private CrmPipelineMapper crmPipelineMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private CrmContractMapper crmContractMapper;

    @Autowired
    private CrmOrderMapper crmOrderMapper;

    @Autowired
    private CrmPaymentPlanMapper crmPaymentPlanMapper;

    @Autowired
    private ICrmPipelineService crmPipelineService;

    @Autowired
    private CrmFollowupMapper crmFollowupMapper;

    @Autowired
    private CrmCustomerDisputeMapper crmDisputeMapper;

    @Autowired
    private CrmCustomerMergeLogMapper mergeLogMapper;

    @Autowired
    private ICrmCustomerDisputeService disputeService;

    @Override
    public List<CrmCustomer> selectCrmCustomerList(CrmCustomer customer)
    {
        return crmCustomerMapper.selectCrmCustomerList(customer);
    }

    @Override
    public CrmCustomer selectCrmCustomerById(Long customerId)
    {
        return crmCustomerMapper.selectCrmCustomerById(customerId);
    }

    @Override
    @Transactional
    public int insertCrmCustomer(CrmCustomer customer)
    {
        customer.setCreateBy(SecurityUtils.getUsername());
        customer.setCreateTime(DateUtils.getNowDate());
        if (customer.getBelongUserId() == null)
        {
            customer.setBelongUserId(SecurityUtils.getUserId());
        }
        if (customer.getBelongDeptId() == null && customer.getBelongUserId() != null)
        {
            SysUser user = sysUserMapper.selectUserById(customer.getBelongUserId());
            if (user != null)
            {
                customer.setBelongDeptId(user.getDeptId());
            }
        }
        if (StringUtils.isNull(customer.getFollowStatus()))
        {
            customer.setFollowStatus("0");
        }
        int result = crmCustomerMapper.insertCrmCustomer(customer);

        CrmPipeline pipeline = new CrmPipeline();
        pipeline.setCustomerId(customer.getCustomerId());
        pipeline.setCustomerName(customer.getCustomerName());
        pipeline.setStage("clue");
        pipeline.setProbability(0);
        pipeline.setCreateBy(SecurityUtils.getUsername());
        pipeline.setCreateTime(DateUtils.getNowDate());
        crmPipelineService.insertCrmPipeline(pipeline);

        return result;
    }

    @Override
    @Transactional
    public int addToPipeline(Long customerId)
    {
        CrmCustomer customer = crmCustomerMapper.selectCrmCustomerById(customerId);
        if (customer == null)
        {
            throw new ServiceException("客户不存在");
        }

        CrmPipeline existing = crmPipelineMapper.selectPipelineByCustomerId(customerId);
        if (existing != null)
        {
            return 2;
        }

        CrmPipeline pipeline = new CrmPipeline();
        pipeline.setCustomerId(customerId);
        pipeline.setCustomerName(customer.getCustomerName());
        pipeline.setStage("clue");
        pipeline.setProbability(0);
        pipeline.setCreateBy(SecurityUtils.getUsername());
        pipeline.setCreateTime(DateUtils.getNowDate());
        crmPipelineService.insertCrmPipeline(pipeline);
        return 1;
    }

    @Override
    @Transactional
    public int updateCrmCustomer(CrmCustomer customer)
    {
        if (customer.getBelongUserId() != null && customer.getBelongDeptId() == null)
        {
            SysUser user = sysUserMapper.selectUserById(customer.getBelongUserId());
            if (user != null)
            {
                customer.setBelongDeptId(user.getDeptId());
            }
        }
        customer.setUpdateBy(SecurityUtils.getUsername());
        customer.setUpdateTime(DateUtils.getNowDate());
        return crmCustomerMapper.updateCrmCustomer(customer);
    }

    @Override
    @Transactional
    public int deleteCrmCustomerByIds(Long[] customerIds)
    {
        return crmCustomerMapper.deleteCrmCustomerByIds(customerIds);
    }

    @Override
    public boolean checkPhoneUnique(CrmCustomer customer)
    {
        Long customerId = StringUtils.isNull(customer.getCustomerId()) ? -1L : customer.getCustomerId();
        CrmCustomer info = crmCustomerMapper.checkPhoneUnique(customer.getPhone());
        if (StringUtils.isNotNull(info) && info.getCustomerId().longValue() != customerId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    @Override
    public boolean checkCustomerNameUnique(CrmCustomer customer)
    {
        Long customerId = StringUtils.isNull(customer.getCustomerId()) ? -1L : customer.getCustomerId();
        CrmCustomer info = crmCustomerMapper.checkCustomerNameUnique(customer.getCustomerName());
        if (StringUtils.isNotNull(info) && info.getCustomerId().longValue() != customerId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    @Override
    @Transactional
    public int assignCustomer(CrmCustomer customer)
    {
        if (customer.getBelongUserId() == null)
        {
            throw new ServiceException("目标负责人不能为空");
        }

        if (customer.getBelongDeptId() == null)
        {
            SysUser user = sysUserMapper.selectUserById(customer.getBelongUserId());
            if (user != null)
            {
                customer.setBelongDeptId(user.getDeptId());
            }
        }
        customer.setUpdateBy(SecurityUtils.getUsername());
        int result = crmCustomerMapper.updateCustomerBelong(customer);

        cascadeCustomerBelong(customer.getCustomerId(), customer.getBelongUserId(), customer.getBelongDeptId());

        CrmCustomer full = crmCustomerMapper.selectCrmCustomerById(customer.getCustomerId());
        CrmCustomerPool log = new CrmCustomerPool();
        log.setCustomerId(customer.getCustomerId());
        log.setAction("2");
        log.setFromUserId(SecurityUtils.getUserId());
        log.setToUserId(customer.getBelongUserId());
        log.setCustomerName(full != null ? full.getCustomerName() : null);
        log.setCreateBy(SecurityUtils.getUsername());
        log.setCreateTime(DateUtils.getNowDate());
        crmCustomerPoolMapper.insertCrmCustomerPool(log);

        return result;
    }

    @Override
    @Transactional
    public int putToPool(CrmCustomer customer)
    {
        customer.setUpdateBy(SecurityUtils.getUsername());
        int result = crmCustomerMapper.putToPool(customer);

        CrmCustomerPool log = new CrmCustomerPool();
        log.setCustomerId(customer.getCustomerId());
        log.setAction("0");
        log.setFromUserId(SecurityUtils.getUserId());
        log.setReason(customer.getRemark());
        log.setCustomerName(customer.getCustomerName());
        log.setCreateBy(SecurityUtils.getUsername());
        log.setCreateTime(DateUtils.getNowDate());
        crmCustomerPoolMapper.insertCrmCustomerPool(log);

        return result;
    }

    @Override
    @Transactional
    public int claimFromPool(Long customerId)
    {
        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(customerId);
        customer.setBelongUserId(SecurityUtils.getUserId());
        customer.setBelongDeptId(SecurityUtils.getLoginUser().getDeptId());
        customer.setUpdateBy(SecurityUtils.getUsername());
        int result = crmCustomerMapper.claimFromPool(customer);

        cascadeCustomerBelong(customerId, customer.getBelongUserId(), customer.getBelongDeptId());

        CrmCustomer full = crmCustomerMapper.selectCrmCustomerById(customerId);
        CrmCustomerPool log = new CrmCustomerPool();
        log.setCustomerId(customerId);
        log.setAction("1");
        log.setToUserId(SecurityUtils.getUserId());
        log.setCustomerName(full != null ? full.getCustomerName() : null);
        log.setCreateBy(SecurityUtils.getUsername());
        log.setCreateTime(DateUtils.getNowDate());
        crmCustomerPoolMapper.insertCrmCustomerPool(log);

        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> batchAssignCustomers(Long[] customerIds, Long targetUserId, boolean force)
    {
        SysUser targetUser = sysUserMapper.selectUserById(targetUserId);
        if (targetUser == null)
        {
            throw new ServiceException("目标用户不存在");
        }

        int success = 0;
        int skipped = 0;
        int failed = 0;
        StringBuilder skippedNames = new StringBuilder();

        for (Long customerId : customerIds)
        {
            try
            {
                CrmCustomer customer = crmCustomerMapper.selectCrmCustomerById(customerId);
                if (customer == null)
                {
                    failed++;
                    continue;
                }
                if (!force && customer.getBelongUserId() != null)
                {
                    skipped++;
                    if (skippedNames.length() > 0) skippedNames.append("、");
                    skippedNames.append(customer.getCustomerName());
                    continue;
                }
                CrmCustomer assign = new CrmCustomer();
                assign.setCustomerId(customerId);
                assign.setBelongUserId(targetUserId);
                assign.setBelongDeptId(targetUser.getDeptId());
                assign.setUpdateBy(SecurityUtils.getUsername());
                crmCustomerMapper.updateCustomerBelong(assign);
                cascadeCustomerBelong(customerId, targetUserId, targetUser.getDeptId());

                CrmCustomerPool log = new CrmCustomerPool();
                log.setCustomerId(customerId);
                log.setAction("2");
                log.setFromUserId(SecurityUtils.getUserId());
                log.setToUserId(targetUserId);
                log.setCustomerName(customer.getCustomerName());
                log.setCreateBy(SecurityUtils.getUsername());
                log.setCreateTime(DateUtils.getNowDate());
                crmCustomerPoolMapper.insertCrmCustomerPool(log);
                success++;
            }
            catch (Exception e)
            {
                failed++;
                log.error("批量分配客户 {} 失败", customerId, e);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("skipped", skipped);
        result.put("failed", failed);
        if (skippedNames.length() > 0)
        {
            result.put("skippedNames", skippedNames.toString());
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> checkBatchAssignCustomers(Long[] customerIds)
    {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Long customerId : customerIds)
        {
            CrmCustomer customer = crmCustomerMapper.selectCrmCustomerById(customerId);
            if (customer != null && customer.getBelongUserId() != null)
            {
                Map<String, Object> item = new HashMap<>();
                item.put("customerId", customer.getCustomerId());
                item.put("customerName", customer.getCustomerName());
                item.put("belongUserName", customer.getBelongUserName());
                list.add(item);
            }
        }
        return list;
    }

    @Override
    public String importCustomer(List<CrmCustomer> customerList, boolean updateSupport, String operName)
    {
        if (StringUtils.isNull(customerList) || customerList.size() == 0)
        {
            throw new ServiceException("导入客户数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (CrmCustomer customer : customerList)
        {
            try
            {
                CrmCustomer u = crmCustomerMapper.checkPhoneUnique(customer.getPhone());
                if (StringUtils.isNull(u))
                {
                    customer.setCreateBy(operName);
                    customer.setCreateTime(DateUtils.getNowDate());
                    if (StringUtils.isNull(customer.getFollowStatus()))
                    {
                        customer.setFollowStatus("0");
                    }
                    this.insertCrmCustomer(customer);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、客户 " + customer.getCustomerName() + " 导入成功");
                }
                else if (updateSupport)
                {
                    customer.setCustomerId(u.getCustomerId());
                    customer.setUpdateBy(operName);
                    this.updateCrmCustomer(customer);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、客户 " + customer.getCustomerName() + " 更新成功");
                }
                else
                {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、客户 " + customer.getCustomerName() + " 已存在");
                }
            }
            catch (Exception e)
            {
                failureNum++;
                String msg = "<br/>" + failureNum + "、客户 " + customer.getCustomerName() + " 导入失败：";
                failureMsg.append(msg + e.getMessage());
                log.error(msg, e);
            }
        }
        if (failureNum > 0)
        {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new ServiceException(failureMsg.toString());
        }
        else
        {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }

    @Override
    public List<CrmCustomer> checkDuplicate(CrmCustomer customer)
    {
        return crmCustomerMapper.checkDuplicate(customer);
    }

    @Override
    @Transactional
    public void mergeCustomer(Long keepCustomerId, Long mergeCustomerId)
    {
        if (keepCustomerId.equals(mergeCustomerId))
        {
            throw new ServiceException("不能合并相同客户");
        }
        CrmCustomer keep = crmCustomerMapper.selectCrmCustomerById(keepCustomerId);
        CrmCustomer merge = crmCustomerMapper.selectCrmCustomerById(mergeCustomerId);
        if (keep == null || merge == null)
        {
            throw new ServiceException("客户不存在");
        }
        if (!"0".equals(keep.getDelFlag()) || !"0".equals(merge.getDelFlag()))
        {
            throw new ServiceException("客户已删除");
        }

        crmFollowupMapper.updateCustomerId(mergeCustomerId, keepCustomerId);
        crmPipelineMapper.updateCustomerId(mergeCustomerId, keepCustomerId);
        crmContractMapper.updateCustomerId(mergeCustomerId, keepCustomerId);
        crmOrderMapper.updateCustomerId(mergeCustomerId, keepCustomerId);

        crmCustomerMapper.deleteByMerge(mergeCustomerId, keepCustomerId);

        crmDisputeMapper.closeByCustomerId(mergeCustomerId);

        CrmCustomerMergeLog log = new CrmCustomerMergeLog();
        log.setKeepCustomerId(keepCustomerId);
        log.setMergeCustomerId(mergeCustomerId);
        log.setKeepCustomerName(keep.getCustomerName());
        log.setMergeCustomerName(merge.getCustomerName());
        log.setCreateBy(SecurityUtils.getUsername());
        mergeLogMapper.insert(log);
    }

    private void cascadeCustomerBelong(Long customerId, Long belongUserId, Long belongDeptId)
    {
        crmContractMapper.updateContractBelongByCustomerId(customerId, belongUserId, belongDeptId);
        crmOrderMapper.updateOrderBelongByCustomerId(customerId, belongUserId, belongDeptId);
        crmPaymentPlanMapper.updatePaymentPlanBelongByCustomerId(customerId, belongUserId, belongDeptId);
    }
}
