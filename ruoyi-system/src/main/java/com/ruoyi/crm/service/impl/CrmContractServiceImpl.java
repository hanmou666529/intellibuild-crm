package com.ruoyi.crm.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

import com.ruoyi.crm.domain.CrmContract;
import com.ruoyi.crm.domain.CrmContractDetail;
import com.ruoyi.crm.domain.CrmCustomer;
import com.ruoyi.crm.domain.CrmOrder;
import com.ruoyi.crm.domain.CrmApprovalRequest;
import com.ruoyi.crm.domain.CrmOrderItem;
import com.ruoyi.crm.mapper.CrmApprovalRequestMapper;
import com.ruoyi.crm.mapper.CrmContractDetailMapper;
import com.ruoyi.crm.mapper.CrmContractMapper;
import com.ruoyi.crm.mapper.CrmCustomerMapper;
import com.ruoyi.crm.mapper.CrmOrderItemMapper;
import com.ruoyi.crm.mapper.CrmOrderMapper;
import com.ruoyi.crm.service.ICrmApprovalService;
import com.ruoyi.crm.service.ICrmContractService;
import com.ruoyi.crm.service.ICrmOrderService;

@Service
public class CrmContractServiceImpl implements ICrmContractService
{
    private static final Logger log = LoggerFactory.getLogger(CrmContractServiceImpl.class);

    @Autowired
    private CrmContractMapper crmContractMapper;

    @Autowired
    private CrmCustomerMapper crmCustomerMapper;

    @Autowired
    private CrmOrderMapper crmOrderMapper;

    @Autowired
    private ICrmOrderService crmOrderService;

    @Autowired
    private ICrmApprovalService crmApprovalService;

    @Autowired
    private CrmApprovalRequestMapper crmApprovalRequestMapper;

    @Autowired
    private CrmContractDetailMapper crmContractDetailMapper;

    @Autowired
    private CrmOrderItemMapper crmOrderItemMapper;

    @Override
    public List<CrmContract> selectCrmContractList(CrmContract contract)
    {
        return crmContractMapper.selectCrmContractList(contract);
    }

    @Override
    public CrmContract selectCrmContractById(Long contractId)
    {
        CrmContract contract = crmContractMapper.selectCrmContractById(contractId);
        if (contract != null)
        {
            contract.setDetails(crmContractDetailMapper.selectCrmContractDetailByContractId(contractId));
            List<CrmOrder> orders = crmOrderMapper.selectByContractId(contractId);
            boolean locked = orders.stream()
                    .anyMatch(o -> "1".equals(o.getStatus()) || "2".equals(o.getStatus()));
            contract.setHasLockedOrder(locked);
        }
        return contract;
    }

    @Override
    @Transactional
    public int insertCrmContract(CrmContract contract)
    {
        List<CrmContractDetail> details = contract.getDetails();
        if (details == null || details.isEmpty())
        {
            throw new ServiceException("请添加至少一条合同明细");
        }

        double totalAmount = details.stream()
                .mapToDouble(d -> d.getProductPrice() != null && d.getQuantity() != null
                        ? d.getProductPrice() * d.getQuantity() : 0.0)
                .sum();
        contract.setAmount(totalAmount);

        contract.setCreateBy(SecurityUtils.getUsername());
        contract.setCreateTime(DateUtils.getNowDate());
        if (contract.getContractNo() == null || "".equals(contract.getContractNo()))
        {
            contract.setContractNo("CT" + DateUtils.dateTimeNow("yyyyMMddHHmmss")
                    + String.format("%04d", (int)(Math.random() * 10000)));
        }
        inheritCustomerBelong(contract);
        contract.setStatus("0");
        int result = crmContractMapper.insertCrmContract(contract);

        for (CrmContractDetail d : details)
        {
            d.setContractId(contract.getContractId());
            d.setSubtotal(d.getProductPrice() * d.getQuantity());
            d.setCreateBy(SecurityUtils.getUsername());
            d.setCreateTime(DateUtils.getNowDate());
        }
        crmContractDetailMapper.insertCrmContractDetails(details);

        String bizInfo = "{\"contractName\":\"" + contract.getContractName()
                + "\",\"contractNo\":\"" + contract.getContractNo() + "\"}";
        crmApprovalService.submitForApproval("contract", contract.getContractId(),
                contract.getAmount(), bizInfo);

        return result;
    }

    @Override
    @Transactional
    public int updateCrmContract(CrmContract contract)
    {
        CrmContract old = crmContractMapper.selectCrmContractById(contract.getContractId());
        contract.setUpdateBy(SecurityUtils.getUsername());
        contract.setUpdateTime(DateUtils.getNowDate());

        // 正向锁定：已支付订单的合同禁止编辑
        List<CrmOrder> existingOrders = crmOrderMapper.selectByContractId(contract.getContractId());
        boolean hasPaidOrCompleted = existingOrders.stream()
                .anyMatch(o -> "1".equals(o.getStatus()) || "2".equals(o.getStatus()));
        if (hasPaidOrCompleted)
        {
            throw new ServiceException("该合同有关联订单已付款/已完成，无法修改");
        }

        // 处理明细更新
        List<CrmContractDetail> details = contract.getDetails();
        if (details != null && !details.isEmpty())
        {
            double totalAmount = details.stream()
                    .mapToDouble(d -> d.getProductPrice() != null && d.getQuantity() != null
                            ? d.getProductPrice() * d.getQuantity() : 0.0)
                    .sum();
            contract.setAmount(totalAmount);

            crmContractDetailMapper.deleteCrmContractDetailByContractId(contract.getContractId());
            for (CrmContractDetail d : details)
            {
                d.setContractId(contract.getContractId());
                d.setSubtotal(d.getProductPrice() * d.getQuantity());
                d.setCreateBy(SecurityUtils.getUsername());
                d.setCreateTime(DateUtils.getNowDate());
            }
            crmContractDetailMapper.insertCrmContractDetails(details);
        }

        if ("2".equals(contract.getStatus()))
        {
            int unpaid = crmContractMapper.countUnpaidByContractId(contract.getContractId());
            if (unpaid > 0)
            {
                throw new ServiceException("合同关联的回款计划尚未全部付清，无法标记为已完成");
            }
        }

        // 已生效 → 待审核：回滚关联订单并重新提交审批
        if ("0".equals(contract.getStatus()) && old != null && "1".equals(old.getStatus()))
        {
            for (CrmOrder o : existingOrders)
            {
                if ("1".equals(o.getStatus()))
                {
                    throw new ServiceException("该合同已有关联订单已付款，无法修改为待审批");
                }
            }
            for (CrmOrder o : existingOrders)
            {
                o.setStatus("3");
                o.setUpdateBy(SecurityUtils.getUsername());
                crmOrderMapper.updateCrmOrder(o);
            }
            CrmApprovalRequest reqParam = new CrmApprovalRequest();
            reqParam.setBizType("contract");
            reqParam.setBizId(contract.getContractId());
            List<CrmApprovalRequest> reqs = crmApprovalRequestMapper.selectCrmApprovalRequestList(reqParam);
            for (CrmApprovalRequest req : reqs)
            {
                if ("0".equals(req.getStatus()) || "1".equals(req.getStatus()))
                {
                    req.setStatus("3");
                    req.setUpdateBy(SecurityUtils.getUsername());
                    crmApprovalRequestMapper.updateCrmApprovalRequest(req);
                }
            }
            String bizInfo = "{\"contractName\":\"" + contract.getContractName()
                    + "\",\"contractNo\":\"" + (contract.getContractNo() != null ? contract.getContractNo() : "") + "\"}";
            crmApprovalService.submitForApproval("contract", contract.getContractId(),
                    contract.getAmount(), bizInfo);
        }

        int result = crmContractMapper.updateCrmContract(contract);

        if ("3".equals(contract.getStatus()) && old != null && !"3".equals(old.getStatus()))
        {
            crmOrderService.completeOrderByContract(contract.getContractId());
        }

        return result;
    }

    @Override
    @Transactional
    public int deleteCrmContractByIds(Long[] contractIds)
    {
        return crmContractMapper.deleteCrmContractByIds(contractIds);
    }

    @Override
    @Transactional
    public int approveContract(Long contractId)
    {
        CrmContract contract = crmContractMapper.selectCrmContractById(contractId);
        if (contract == null)
        {
            throw new ServiceException("合同不存在");
        }
        if (!"0".equals(contract.getStatus()))
        {
            throw new ServiceException("仅待审核状态的合同可审批通过");
        }
        contract.setStatus("1");
        contract.setUpdateBy(SecurityUtils.getUsername());
        int result = crmContractMapper.updateCrmContract(contract);

        CrmOrder order = new CrmOrder();
        order.setCustomerId(contract.getCustomerId());
        order.setContractId(contract.getContractId());
        order.setTotalAmount(contract.getAmount());
        order.setActualAmount(contract.getAmount());
        order.setStatus("4");
        order.setSource("contract");
        order.setBelongUserId(contract.getBelongUserId());
        order.setBelongDeptId(contract.getBelongDeptId());
        crmOrderService.insertCrmOrder(order);

        List<CrmContractDetail> details = crmContractDetailMapper.selectCrmContractDetailByContractId(contractId);
        if (details != null && !details.isEmpty())
        {
            List<CrmOrderItem> items = new java.util.ArrayList<>();
            for (CrmContractDetail d : details)
            {
                CrmOrderItem item = new CrmOrderItem();
                item.setOrderId(order.getOrderId());
                item.setProductId(d.getProductId());
                item.setProductName(d.getProductName());
                item.setProductPrice(d.getProductPrice());
                item.setQuantity(d.getQuantity());
                item.setSubtotal(d.getSubtotal());
                items.add(item);
            }
            crmOrderItemMapper.insertCrmOrderItems(items);
        }

        contract.setContractNo("CON-" + order.getOrderNo());
        contract.setApprovalCount(contract.getApprovalCount() != null ? contract.getApprovalCount() + 1 : 1);
        contract.setLastApprovalTime(new Date());
        contract.setUpdateBy(SecurityUtils.getUsername());
        crmContractMapper.updateCrmContract(contract);

        CrmApprovalRequest reqParam = new CrmApprovalRequest();
        reqParam.setBizType("contract");
        reqParam.setBizId(contractId);
        List<CrmApprovalRequest> reqs = crmApprovalRequestMapper.selectCrmApprovalRequestList(reqParam);
        for (CrmApprovalRequest req : reqs) {
            if ("0".equals(req.getStatus())) {
                req.setStatus("1");
                req.setUpdateBy(SecurityUtils.getUsername());
                crmApprovalRequestMapper.updateCrmApprovalRequest(req);
            }
        }

        return result;
    }

    @Override
    @Transactional
    public void processApprovedContract(Long contractId)
    {
        CrmContract contract = crmContractMapper.selectCrmContractById(contractId);
        if (contract == null) throw new ServiceException("合同不存在");
        if (!"0".equals(contract.getStatus())) return;

        contract.setStatus("1");
        contract.setUpdateBy(SecurityUtils.getUsername());
        crmContractMapper.updateCrmContract(contract);

        CrmOrder order = new CrmOrder();
        order.setCustomerId(contract.getCustomerId());
        order.setContractId(contract.getContractId());
        order.setTotalAmount(contract.getAmount());
        order.setActualAmount(contract.getAmount());
        order.setStatus("4");
        order.setSource("contract");
        order.setBelongUserId(contract.getBelongUserId());
        order.setBelongDeptId(contract.getBelongDeptId());
        crmOrderService.insertCrmOrder(order);

        List<CrmContractDetail> details = crmContractDetailMapper.selectCrmContractDetailByContractId(contractId);
        if (details != null && !details.isEmpty())
        {
            List<CrmOrderItem> items = new java.util.ArrayList<>();
            for (CrmContractDetail d : details)
            {
                CrmOrderItem item = new CrmOrderItem();
                item.setOrderId(order.getOrderId());
                item.setProductId(d.getProductId());
                item.setProductName(d.getProductName());
                item.setProductPrice(d.getProductPrice());
                item.setQuantity(d.getQuantity());
                item.setSubtotal(d.getSubtotal());
                items.add(item);
            }
            crmOrderItemMapper.insertCrmOrderItems(items);
        }

        contract.setContractNo("CON-" + order.getOrderNo());
        contract.setApprovalCount(contract.getApprovalCount() != null ? contract.getApprovalCount() + 1 : 1);
        contract.setLastApprovalTime(new Date());
        contract.setUpdateBy(SecurityUtils.getUsername());
        crmContractMapper.updateCrmContract(contract);

        log.info("审批引擎回调: 合同审批通过 contractId={} orderNo={}", contractId, order.getOrderNo());
    }

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void autoTerminateExpiredContracts()
    {
        List<CrmContract> expired = crmContractMapper.selectExpiredContracts();
        for (CrmContract contract : expired)
        {
            contract.setStatus("3");
            contract.setUpdateBy("system");
            contract.setUpdateTime(new Date());
            crmContractMapper.updateCrmContract(contract);
            crmOrderService.completeOrderByContract(contract.getContractId());
            log.info("合同到期自动终止: contractId={}, contractNo={}", contract.getContractId(), contract.getContractNo());
        }
        if (!expired.isEmpty())
        {
            log.info("合同到期自动终止任务完成，共处理 {} 条", expired.size());
        }
    }

    private void inheritCustomerBelong(CrmContract contract)
    {
        if (contract.getCustomerId() != null && contract.getBelongUserId() == null)
        {
            CrmCustomer customer = crmCustomerMapper.selectCrmCustomerById(contract.getCustomerId());
            if (customer != null)
            {
                contract.setBelongUserId(customer.getBelongUserId());
                contract.setBelongDeptId(customer.getBelongDeptId());
            }
        }
    }

    @Override
    @Transactional
    public void reSubmitApproval(Long contractId)
    {
        CrmContract contract = crmContractMapper.selectCrmContractById(contractId);
        if (contract == null) throw new ServiceException("合同不存在");
        if (!"1".equals(contract.getStatus())) throw new ServiceException("仅已生效状态的合同可重新提交审批");

        List<CrmOrder> orders = crmOrderMapper.selectByContractId(contractId);
        for (CrmOrder o : orders)
        {
            if ("1".equals(o.getStatus()))
                throw new ServiceException("该合同已有关联订单已付款，无法重新提交审批");
        }
        for (CrmOrder o : orders)
        {
            o.setStatus("3");
            o.setUpdateBy(SecurityUtils.getUsername());
            crmOrderMapper.updateCrmOrder(o);
        }

        CrmApprovalRequest reqParam = new CrmApprovalRequest();
        reqParam.setBizType("contract");
        reqParam.setBizId(contractId);
        List<CrmApprovalRequest> reqs = crmApprovalRequestMapper.selectCrmApprovalRequestList(reqParam);
        for (CrmApprovalRequest req : reqs)
        {
            if ("0".equals(req.getStatus()) || "1".equals(req.getStatus()))
            {
                req.setStatus("3");
                req.setUpdateBy(SecurityUtils.getUsername());
                crmApprovalRequestMapper.updateCrmApprovalRequest(req);
            }
        }

        contract.setStatus("0");
        contract.setContractNo(null);
        contract.setUpdateBy(SecurityUtils.getUsername());
        crmContractMapper.updateCrmContract(contract);

        String bizInfo = "{\"contractName\":\"" + contract.getContractName()
                + "\",\"contractNo\":\"\"}";
        crmApprovalService.submitForApproval("contract", contractId, contract.getAmount(), bizInfo);

        log.info("合同重新提交审批: contractId={} contractName={}", contractId, contract.getContractName());
    }

    @Override
    public String uploadContractAttachment(MultipartFile file, Long contractId)
    {
        try
        {
            // 验证合同是否存在
            CrmContract contract = crmContractMapper.selectCrmContractById(contractId);
            if (contract == null)
            {
                throw new ServiceException("合同不存在");
            }

            // 获取上传路径
            String uploadPath = RuoYiConfig.getUploadPath();
            Path uploadDir = Paths.get(uploadPath);
            
            // 确保上传目录存在
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            // 生成文件名：合同ID_时间戳.原始扩展名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = StringUtils.substringAfterLast(originalFilename, ".");
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
            String newFilename = "contract_" + contractId + "_" + timestamp + "." + fileExtension;
            
            // 保存文件
            Path filePath = uploadDir.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath);
            
            // 更新合同的attachment字段
            contract.setAttachment(newFilename);
            contract.setUpdateBy(SecurityUtils.getUsername());
            contract.setUpdateTime(DateUtils.getNowDate());
            crmContractMapper.updateCrmContract(contract);
            
            return newFilename;
        }
        catch (IOException e)
        {
            throw new ServiceException("上传合同附件失败：" + e.getMessage());
        }
    }
}
