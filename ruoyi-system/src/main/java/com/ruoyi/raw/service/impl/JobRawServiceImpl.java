package com.ruoyi.raw.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.raw.mapper.JobRawMapper;
import com.ruoyi.raw.domain.JobRaw;
import com.ruoyi.raw.service.IJobRawService;

/**
 * 招聘原始数据Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-11-24
 */
@Service
public class JobRawServiceImpl implements IJobRawService 
{
    @Autowired
    private JobRawMapper jobRawMapper;

    /**
     * 查询招聘原始数据
     * 
     * @param id 招聘原始数据主键
     * @return 招聘原始数据
     */
    @Override
    public JobRaw selectJobRawById(Long id)
    {
        return jobRawMapper.selectJobRawById(id);
    }

    /**
     * 查询招聘原始数据列表
     * 
     * @param jobRaw 招聘原始数据
     * @return 招聘原始数据
     */
    @Override
    public List<JobRaw> selectJobRawList(JobRaw jobRaw)
    {
        return jobRawMapper.selectJobRawList(jobRaw);
    }

    /**
     * 新增招聘原始数据
     * 
     * @param jobRaw 招聘原始数据
     * @return 结果
     */
    @Override
    public int insertJobRaw(JobRaw jobRaw)
    {
        return jobRawMapper.insertJobRaw(jobRaw);
    }

    /**
     * 修改招聘原始数据
     * 
     * @param jobRaw 招聘原始数据
     * @return 结果
     */
    @Override
    public int updateJobRaw(JobRaw jobRaw)
    {
        return jobRawMapper.updateJobRaw(jobRaw);
    }

    /**
     * 批量删除招聘原始数据
     * 
     * @param ids 需要删除的招聘原始数据主键
     * @return 结果
     */
    @Override
    public int deleteJobRawByIds(Long[] ids)
    {
        return jobRawMapper.deleteJobRawByIds(ids);
    }

    /**
     * 删除招聘原始数据信息
     * 
     * @param id 招聘原始数据主键
     * @return 结果
     */
    @Override
    public int deleteJobRawById(Long id)
    {
        return jobRawMapper.deleteJobRawById(id);
    }

    /**
     * 查询每个城市的岗位数量及学历分布
     * @return 城市岗位数量及学历分布列表
     */
    @Override
    public List<java.util.Map<String, Object>> selectCityEducationDistribution() {
        List<JobRaw> list = jobRawMapper.selectJobRawList(new JobRaw());

        // Group by city
        java.util.Map<String, List<JobRaw>> jobsByCity = list.stream()
                .collect(java.util.stream.Collectors.groupingBy(j -> {
                    String c = j.getCity();
                    if (c == null) return "未知";
                    c = c.trim();
                    return c.isEmpty() ? "未知" : c;
                }));

        List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (java.util.Map.Entry<String, List<JobRaw>> cityEntry : jobsByCity.entrySet()) {
            String city = cityEntry.getKey();
            List<JobRaw> cityJobs = cityEntry.getValue();

            // Count total jobs in this city
            long totalCityJobs = cityJobs.size();

            // Group by education within this city
            java.util.Map<String, Long> educationCounts = cityJobs.stream()
                    .collect(java.util.stream.Collectors.groupingBy(j -> {
                        String e = j.getEducation();
                        if (e == null) return "未知";
                        e = e.trim();
                        return e.isEmpty() ? "未知" : e;
                    }, java.util.stream.Collectors.counting()));

            List<java.util.Map<String, Object>> educationDistribution = educationCounts.entrySet().stream()
                    .map(eduEntry -> {
                        java.util.Map<String, Object> eduMap = new java.util.HashMap<>();
                        eduMap.put("education", eduEntry.getKey());
                        eduMap.put("count", eduEntry.getValue());
                        return eduMap;
                    })
                    .collect(java.util.stream.Collectors.toList());

            java.util.Map<String, Object> cityData = new java.util.HashMap<>();
            cityData.put("city", city);
            cityData.put("count", totalCityJobs);
            cityData.put("educationDistribution", educationDistribution);
            result.add(cityData);
        }

        return result;
    }
}
