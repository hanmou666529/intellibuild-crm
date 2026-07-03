package com.ruoyi.crm.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.crm.domain.CrmFollowup;

public interface CrmFollowupMapper
{
    public List<CrmFollowup> selectCrmFollowupList(CrmFollowup followup);

    public CrmFollowup selectCrmFollowupById(Long followupId);

    public List<CrmFollowup> selectCrmFollowupByCustomerId(Long customerId);

    public int insertCrmFollowup(CrmFollowup followup);

    public int updateCrmFollowup(CrmFollowup followup);

    public int deleteCrmFollowupByIds(Long[] followupIds);

    public List<Map<String, Object>> countFollowupTrend(Map<String, Object> params);

    int updateCustomerId(@Param("oldId") Long oldId, @Param("newId") Long newId);
}
