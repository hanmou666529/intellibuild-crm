package com.ruoyi.crm.mapper;

import java.util.List;
import com.ruoyi.crm.domain.CrmContractDetail;

public interface CrmContractDetailMapper
{
    public List<CrmContractDetail> selectCrmContractDetailByContractId(Long contractId);

    public int insertCrmContractDetail(CrmContractDetail detail);

    public int insertCrmContractDetails(List<CrmContractDetail> details);

    public int updateCrmContractDetail(CrmContractDetail detail);

    public int deleteCrmContractDetailByContractId(Long contractId);

    public int deleteCrmContractDetailByIds(Long[] detailIds);
}
