package com.ruoyi.raw.mapper;

import java.util.List;
import com.ruoyi.raw.domain.JobRaw;

/**
 * 招聘原始数据Mapper接口
 * 
 * @author ruoyi
 * @date 2025-11-24
 */
public interface JobRawMapper 
{
    /**
     * 查询招聘原始数据
     * 
     * @param id 招聘原始数据主键
     * @return 招聘原始数据
     */
    public JobRaw selectJobRawById(Long id);

    /**
     * 查询招聘原始数据列表
     * 
     * @param jobRaw 招聘原始数据
     * @return 招聘原始数据集合
     */
    public List<JobRaw> selectJobRawList(JobRaw jobRaw);

    /**
     * 新增招聘原始数据
     * 
     * @param jobRaw 招聘原始数据
     * @return 结果
     */
    public int insertJobRaw(JobRaw jobRaw);

    /**
     * 修改招聘原始数据
     * 
     * @param jobRaw 招聘原始数据
     * @return 结果
     */
    public int updateJobRaw(JobRaw jobRaw);

    /**
     * 删除招聘原始数据
     * 
     * @param id 招聘原始数据主键
     * @return 结果
     */
    public int deleteJobRawById(Long id);

    /**
     * 批量删除招聘原始数据
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteJobRawByIds(Long[] ids);
    
}
