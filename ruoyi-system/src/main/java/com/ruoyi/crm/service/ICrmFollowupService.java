package com.ruoyi.crm.service;

import java.util.List;
import com.ruoyi.crm.domain.CrmFollowup;

public interface ICrmFollowupService
{
    public List<CrmFollowup> selectCrmFollowupList(CrmFollowup followup);

    public CrmFollowup selectCrmFollowupById(Long followupId);

    public List<CrmFollowup> selectCrmFollowupByCustomerId(Long customerId);

    public int insertCrmFollowup(CrmFollowup followup);

    public int updateCrmFollowup(CrmFollowup followup);

    public int deleteCrmFollowupByIds(Long[] followupIds);
}
