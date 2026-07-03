package com.ruoyi.crm.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.crm.domain.CrmContract;

public interface ICrmContractService
{
    public List<CrmContract> selectCrmContractList(CrmContract contract);

    public CrmContract selectCrmContractById(Long contractId);

    public int insertCrmContract(CrmContract contract);

    public int updateCrmContract(CrmContract contract);

    public int deleteCrmContractByIds(Long[] contractIds);

    public int approveContract(Long contractId);

    public void processApprovedContract(Long contractId);

    public String uploadContractAttachment(MultipartFile file, Long contractId);
    public void reSubmitApproval(Long contractId);
}
