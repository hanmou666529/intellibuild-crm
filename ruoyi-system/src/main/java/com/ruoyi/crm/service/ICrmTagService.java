package com.ruoyi.crm.service;

import java.util.List;
import com.ruoyi.crm.domain.CrmTag;

public interface ICrmTagService
{
    List<CrmTag> selectCrmTagList(CrmTag tag);
    CrmTag selectCrmTagById(Long tagId);
    int insertCrmTag(CrmTag tag);
    int updateCrmTag(CrmTag tag);
    int deleteCrmTagByIds(Long[] tagIds);
    List<CrmTag> selectActiveTags();
}
