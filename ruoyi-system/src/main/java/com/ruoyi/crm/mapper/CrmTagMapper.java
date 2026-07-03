package com.ruoyi.crm.mapper;

import java.util.List;
import com.ruoyi.crm.domain.CrmTag;

public interface CrmTagMapper
{
    List<CrmTag> selectCrmTagList(CrmTag tag);
    CrmTag selectCrmTagById(Long tagId);
    int insertCrmTag(CrmTag tag);
    int updateCrmTag(CrmTag tag);
    int deleteCrmTagByIds(Long[] tagIds);
    List<CrmTag> selectCrmTagByType(String tagType);
}
