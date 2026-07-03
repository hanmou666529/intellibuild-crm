package com.ruoyi.raw.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.raw.domain.JobRaw;
import com.ruoyi.raw.service.IJobRawService;

/**
 * 可视化数据 Controller
 * 提供前端大屏所需的聚合数据接口
 */
@RestController
@RequestMapping("/visualization")
public class JobVisualController extends BaseController {

	@Autowired
	private IJobRawService jobRawService;

	/**
	 * 学历分布（饼图）
	 * GET /visualization/job/education
	 */
	@GetMapping("/job/education")
	public AjaxResult education() {
		List<JobRaw> list = jobRawService.selectJobRawList(new JobRaw());
		Map<String, Long> counts = list.stream()
				.collect(Collectors.groupingBy(j -> {
					String e = j.getEducation();
					if (e == null) return "未知";
					e = e.trim();
					return e.isEmpty() ? "未知" : e;
				}, Collectors.counting()));

		List<Map<String, Object>> data = counts.entrySet().stream()
				.map(en -> {
					Map<String, Object> m = new HashMap<>();
					m.put("education", en.getKey());
					m.put("count", en.getValue());
					return m;
				}).collect(Collectors.toList());

		return success(data);
	}

	/**
	 * 城市招聘 Top10
	 * GET /visualization/job/cityTop10
	 */
	@GetMapping("/job/cityTop10")
	public AjaxResult cityTop10() {
		List<JobRaw> list = jobRawService.selectJobRawList(new JobRaw());
		Map<String, Long> counts = list.stream()
				.collect(Collectors.groupingBy(j -> {
					String c = j.getCity();
					if (c == null) return "未知";
					c = c.trim();
					return c.isEmpty() ? "未知" : c;
				}, Collectors.counting()));

		List<Map<String, Object>> data = counts.entrySet().stream()
				.sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
				.limit(10)
				.map(en -> {
					Map<String, Object> m = new HashMap<>();
					m.put("city", en.getKey());
					m.put("count", en.getValue());
					return m;
				}).collect(Collectors.toList());

		return success(data);
	}

	/**
	 * 所有城市岗位数量（用于地图展示）
	 * GET /visualization/job/allCities
	 */
	@GetMapping("/job/allCities")
	public AjaxResult allCities() {
		List<JobRaw> list = jobRawService.selectJobRawList(new JobRaw());
		Map<String, Long> counts = list.stream()
				.collect(Collectors.groupingBy(j -> {
					String c = j.getCity();
					if (c == null) return "未知";
					c = c.trim();
					return c.isEmpty() ? "未知" : c;
				}, Collectors.counting()));

		List<Map<String, Object>> data = counts.entrySet().stream()
				.map(en -> {
					Map<String, Object> m = new HashMap<>();
					m.put("city", en.getKey());
					m.put("count", en.getValue());
					return m;
				}).collect(Collectors.toList());

		return success(data);
	}

	/**
	 * 每个城市的岗位数量及学历分布（用于地图展示）
	 * GET /visualization/job/cityEducationDistribution
	 */
	@GetMapping("/job/cityEducationDistribution")
	public AjaxResult cityEducationDistribution() {
		List<JobRaw> list = jobRawService.selectJobRawList(new JobRaw());

		// Group by city
		Map<String, List<JobRaw>> jobsByCity = list.stream()
				.collect(Collectors.groupingBy(j -> {
					String c = j.getCity();
					if (c == null) return "未知";
					c = c.trim();
					return c.isEmpty() ? "未知" : c;
				}));

		List<Map<String, Object>> result = new ArrayList<>();
		for (Map.Entry<String, List<JobRaw>> cityEntry : jobsByCity.entrySet()) {
			String city = cityEntry.getKey();
			List<JobRaw> cityJobs = cityEntry.getValue();

			// Count total jobs in this city
			long totalCityJobs = cityJobs.size();

			// Group by education within this city
			Map<String, Long> educationCounts = cityJobs.stream()
					.collect(Collectors.groupingBy(j -> {
						String e = j.getEducation();
						if (e == null) return "未知";
						e = e.trim();
						return e.isEmpty() ? "未知" : e;
					}, Collectors.counting()));

			List<Map<String, Object>> educationDistribution = educationCounts.entrySet().stream()
					.map(eduEntry -> {
						Map<String, Object> eduMap = new HashMap<>();
						eduMap.put("education", eduEntry.getKey());
						eduMap.put("count", eduEntry.getValue());
						return eduMap;
					})
					.collect(Collectors.toList());

			Map<String, Object> cityData = new HashMap<>();
			cityData.put("city", city);
			cityData.put("count", totalCityJobs);
			cityData.put("educationDistribution", educationDistribution);
			result.add(cityData);
		}

		return success(result);
	}

	/**
	 * 薪资 VS 经验 箱线图数据
	 * GET /visualization/job/salaryExpBoxplot
	 */
	@GetMapping("/job/salaryExpBoxplot")
	public AjaxResult salaryExpBoxplot() {
		List<JobRaw> list = jobRawService.selectJobRawList(new JobRaw());
		// group by exp
		Map<String, List<Double>> groups = list.stream()
			.collect(Collectors.groupingBy(j -> {
				String e = j.getExp();
				if (e == null) return "未知";
				e = e.trim();
				return e.isEmpty() ? "未知" : e;
			}, Collectors.mapping(j -> {
				// compute salary midpoint if possible (salary stored as BigDecimal)
				java.math.BigDecimal low = j.getSalaryLow();
				java.math.BigDecimal high = j.getSalaryHigh();
				double v = 0d;
				if (low != null && high != null) {
					if (low.compareTo(java.math.BigDecimal.ZERO) > 0 && high.compareTo(java.math.BigDecimal.ZERO) > 0) {
						v = (low.doubleValue() + high.doubleValue()) / 2.0;
					} else if (high.compareTo(java.math.BigDecimal.ZERO) > 0) {
						v = high.doubleValue();
					} else if (low.compareTo(java.math.BigDecimal.ZERO) > 0) {
						v = low.doubleValue();
					}
				} else if (high != null) {
					if (high.compareTo(java.math.BigDecimal.ZERO) > 0) v = high.doubleValue();
				} else if (low != null) {
					if (low.compareTo(java.math.BigDecimal.ZERO) > 0) v = low.doubleValue();
				}
				return v;
			}, Collectors.toList())));

		List<Map<String, Object>> result = groups.entrySet().stream()
			.map(en -> {
				List<Double> vals = en.getValue().stream().filter(d -> d != null && d > 0).collect(Collectors.toList());
				Collections.sort(vals);
				Map<String, Object> m = new HashMap<>();
				m.put("exp", en.getKey());
				if (vals.isEmpty()) {
					m.put("min", 0);
					m.put("q1", 0);
					m.put("median", 0);
					m.put("q3", 0);
					m.put("max", 0);
				} else {
					int n = vals.size();
					double min = vals.get(0);
					double max = vals.get(n - 1);
					double q1 = percentile(vals, 25);
					double median = percentile(vals, 50);
					double q3 = percentile(vals, 75);
					m.put("min", min);
					m.put("q1", q1);
					m.put("median", median);
					m.put("q3", q3);
					m.put("max", max);
				}
				m.put("count", en.getValue().size());
				return m;
			})
			.sorted((a, b) -> Long.compare(((Number)b.get("count")).longValue(), ((Number)a.get("count")).longValue()))
			.collect(Collectors.toList());

		return success(result);
	}

	/**
	 * 词云数据（按 field1/field2/field3 优先级聚合标签）
	 * GET /visualization/job/wordCloud
	 */
	@GetMapping("/job/wordCloud")
	public AjaxResult wordCloud() {
		List<JobRaw> list = jobRawService.selectJobRawList(new JobRaw());
		Map<String, Long> counts = list.stream()
			.collect(Collectors.groupingBy(j -> {
				String tag = null;
				if (j.getField1() != null && !j.getField1().trim().isEmpty()) tag = j.getField1().trim();
				else if (j.getField2() != null && !j.getField2().trim().isEmpty()) tag = j.getField2().trim();
				else if (j.getField3() != null && !j.getField3().trim().isEmpty()) tag = j.getField3().trim();
				else tag = "未知";
				return tag;
			}, Collectors.counting()));

		List<Map<String, Object>> data = counts.entrySet().stream()
			.sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
			.limit(50)
			.map(en -> {
				Map<String, Object> m = new HashMap<>();
				m.put("name", en.getKey());
				m.put("value", en.getValue());
				return m;
			}).collect(Collectors.toList());

		return success(data);
	}

	/**
	 * 薪资按学历堆叠柱状图数据
	 * GET /visualization/job/salaryEduStack
	 * 返回结构：{ categories: [edu...], series: [{name: bucket, data: [counts...]}, ...] }
	 */
	@GetMapping("/job/salaryEduStack")
	public AjaxResult salaryEduStack() {
		List<JobRaw> list = jobRawService.selectJobRawList(new JobRaw());

		// 定义薪资分段（单位与数据相同，示例：k）
		double[] bounds = new double[] {0, 5, 10, 15, 20, 30};
		String[] bucketLabels = new String[] {"<=5k", "5-10k", "10-15k", "15-20k", "20-30k", ">30k"};

		// 收集所有学历并保证稳定顺序（按出现顺序）
		List<String> educations = new ArrayList<>();
		for (JobRaw j : list) {
			String e = j.getEducation();
			if (e == null) e = "未知";
			e = e.trim();
			if (e.isEmpty()) e = "未知";
			if (!educations.contains(e)) educations.add(e);
		}

		// map education -> counts per bucket
		Map<String, long[]> map = new HashMap<>();
		for (String edu : educations) map.put(edu, new long[bucketLabels.length]);

		for (JobRaw j : list) {
			String e = j.getEducation();
			if (e == null) e = "未知";
			e = e.trim();
			if (e.isEmpty()) e = "未知";

			BigDecimal low = j.getSalaryLow();
			BigDecimal high = j.getSalaryHigh();
			double v = 0d;
			if (low != null && high != null && low.compareTo(BigDecimal.ZERO) > 0 && high.compareTo(BigDecimal.ZERO) > 0) {
				v = (low.doubleValue() + high.doubleValue()) / 2.0;
			} else if (high != null && high.compareTo(BigDecimal.ZERO) > 0) {
				v = high.doubleValue();
			} else if (low != null && low.compareTo(BigDecimal.ZERO) > 0) {
				v = low.doubleValue();
			} else {
				continue;
			}

			int idx = bucketLabels.length - 1; // default last
			for (int i = 0; i < bounds.length - 1; i++) {
				double hi = bounds[i+1];
				if (v <= hi) { idx = i; break; }
			}

			long[] arr = map.get(e);
			if (arr == null) {
				arr = new long[bucketLabels.length];
				map.put(e, arr);
			}
			arr[idx] = arr[idx] + 1;
		}

		// build series list
		List<Map<String, Object>> series = new ArrayList<>();
		for (int b = 0; b < bucketLabels.length; b++) {
			Map<String, Object> s = new HashMap<>();
			s.put("name", bucketLabels[b]);
			List<Long> data = new ArrayList<>();
			for (String edu : educations) {
				long[] arr = map.get(edu);
				data.add(arr == null ? 0L : arr[b]);
			}
			s.put("data", data);
			series.add(s);
		}

		Map<String, Object> out = new HashMap<>();
		out.put("categories", educations);
		out.put("series", series);
		return success(out);
	}

	/**
	 * 在招职位总数（含昨日对比）
	 * GET /visualization/job/totalCount
	 */
	@GetMapping("/job/totalCount")
	public AjaxResult totalCount() {
		List<JobRaw> list = jobRawService.selectJobRawList(new JobRaw());
		int total = list.size();

		LocalDate today = LocalDate.now();
		Date startToday = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
		Date startYesterday = Date.from(today.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

		long todayNew = list.stream().filter(j -> j.getCreateTime() != null && !j.getCreateTime().before(startToday)).count();
		long yesterdayNew = list.stream().filter(j -> {
			Date ct = j.getCreateTime();
			return ct != null && !ct.before(startYesterday) && ct.before(startToday);
		}).count();

		long prevTotal = total - todayNew + yesterdayNew; // approximate yesterday total assuming no deletions

		Map<String, Object> out = new HashMap<>();
		out.put("value", total);
		out.put("prev", prevTotal);
		return success(out);
	}

	/**
	 * 平均最高薪（单位：k），返回当日平均与昨日平均
	 * GET /visualization/job/avgHighSalary
	 */
	@GetMapping("/job/avgHighSalary")
	public AjaxResult avgHighSalary() {
		List<JobRaw> list = jobRawService.selectJobRawList(new JobRaw());
		LocalDate today = LocalDate.now();
		Date startToday = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
		Date startYesterday = Date.from(today.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

		// helper to compute average from a stream
		double overallAvg = list.stream()
			.filter(j -> j.getSalaryHigh() != null && j.getSalaryHigh().compareTo(BigDecimal.ZERO) > 0)
			.mapToDouble(j -> j.getSalaryHigh().doubleValue()).average().orElse(0d);

		double todayAvg = list.stream()
			.filter(j -> j.getCreateTime() != null && !j.getCreateTime().before(startToday))
			.filter(j -> j.getSalaryHigh() != null && j.getSalaryHigh().compareTo(BigDecimal.ZERO) > 0)
			.mapToDouble(j -> j.getSalaryHigh().doubleValue()).average().orElse(Double.NaN);

		double yesterdayAvg = list.stream()
			.filter(j -> j.getCreateTime() != null && !j.getCreateTime().before(startYesterday) && j.getCreateTime().before(startToday))
			.filter(j -> j.getSalaryHigh() != null && j.getSalaryHigh().compareTo(BigDecimal.ZERO) > 0)
			.mapToDouble(j -> j.getSalaryHigh().doubleValue()).average().orElse(Double.NaN);

		// if today's avg missing, fallback to overallAvg
		double cur = Double.isNaN(todayAvg) ? overallAvg : todayAvg;
		double prev = Double.isNaN(yesterdayAvg) ? Double.NaN : yesterdayAvg;

		Map<String, Object> out = new HashMap<>();
		out.put("value", Math.round(cur));
		out.put("prev", Double.isNaN(prev) ? null : Math.round(prev));
		return success(out);
	}

	/**
	 * 高薪急缺职位总数及占比（salary_high >= 20k）
	 * GET /visualization/job/highSalaryUrgentCount
	 */
	@GetMapping("/job/highSalaryUrgentCount")
	public AjaxResult highSalaryUrgentCount() {
		List<JobRaw> list = jobRawService.selectJobRawList(new JobRaw());
		long highSalaryJobsCount = list.stream()
			.filter(j -> j.getSalaryHigh() != null && j.getSalaryHigh().compareTo(new BigDecimal(20)) >= 0)
			.count();

		long totalJobs = list.size();
		double proportion = 0.0;
		if (totalJobs > 0) {
			proportion = (double) highSalaryJobsCount / totalJobs;
		}

		Map<String, Object> out = new HashMap<>();
		out.put("value", highSalaryJobsCount);
		out.put("proportion", proportion);
		return success(out);
	}

	/**
	 * 统计 salaryHigh >= 20 的职位数量占总职位数量的比例
	 * GET /visualization/job/highSalaryProportion
	 */
	@GetMapping("/job/highSalaryProportion")
	public AjaxResult getHighSalaryProportion() {
		List<JobRaw> list = jobRawService.selectJobRawList(new JobRaw());
		long totalJobs = list.size();

		long highSalaryJobs = list.stream()
			.filter(j -> j.getSalaryHigh() != null && j.getSalaryHigh().compareTo(new BigDecimal(20)) >= 0)
			.count();

		double proportion = 0.0;
		if (totalJobs > 0) {
			proportion = (double) highSalaryJobs / totalJobs;
		}

		Map<String, Object> out = new HashMap<>();
		out.put("highSalaryJobs", highSalaryJobs);
		out.put("totalJobs", totalJobs);
		out.put("proportion", proportion);
		return success(out);
	}

	/**
	 * 经验-薪资弹性系数（回归线）
	 * GET /visualization/job/salaryExpRegression
	 */
	@GetMapping("/job/salaryExpRegression")
	public AjaxResult salaryExpRegression() {
		List<JobRaw> list = jobRawService.selectJobRawList(new JobRaw());

		List<double[]> dataPoints = new ArrayList<>();
		Map<String, Double> expMap = new HashMap<>();
		expMap.put("不限", 0.0);
		expMap.put("应届生", 0.5);
		expMap.put("1年以内", 0.5);
		expMap.put("1-3年", 2.0);
		expMap.put("3-5年", 4.0);
		expMap.put("5-10年", 7.5);
		expMap.put("10年以上", 12.0);
		expMap.put("在校/实习", 0.0);

		for (JobRaw j : list) {
			String expStr = j.getExp();
			if (expStr == null || expStr.trim().isEmpty()) continue;
			expStr = expStr.trim();

			Double expVal = expMap.get(expStr);
			if (expVal == null) {
				// Try to parse if it's a direct number like "2年"
				if (expStr.endsWith("年")) {
					try {
						expVal = Double.parseDouble(expStr.substring(0, expStr.length() - 1));
					} catch (NumberFormatException e) {
						// ignore
					}
				}
				if (expVal == null) continue; // Skip if experience cannot be mapped
			}

			java.math.BigDecimal low = j.getSalaryLow();
			java.math.BigDecimal high = j.getSalaryHigh();
			double salaryMidpoint = 0d;

			if (low != null && high != null && low.compareTo(java.math.BigDecimal.ZERO) > 0 && high.compareTo(java.math.BigDecimal.ZERO) > 0) {
				salaryMidpoint = (low.doubleValue() + high.doubleValue()) / 2.0;
			} else if (high != null && high.compareTo(java.math.BigDecimal.ZERO) > 0) {
				salaryMidpoint = high.doubleValue();
			} else if (low != null && low.compareTo(java.math.BigDecimal.ZERO) > 0) {
				salaryMidpoint = low.doubleValue();
			} else {
				continue; // Skip if salary is invalid
			}

			if (salaryMidpoint > 0) {
				dataPoints.add(new double[]{expVal, salaryMidpoint});
			}
		}

		if (dataPoints.size() < 2) { // Need at least 2 points for regression
			return success(new HashMap<String, Object>() {{
				put("scatterData", Collections.emptyList());
				put("regressionLine", Collections.emptyList());
				put("slope", 0.0);
				put("intercept", 0.0);
			}});
		}

		double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
		for (double[] point : dataPoints) {
			sumX += point[0];
			sumY += point[1];
			sumXY += point[0] * point[1];
			sumX2 += point[0] * point[0];
		}

		int n = dataPoints.size();
		double slope = 0;
		double intercept = 0;

		double denominator = (n * sumX2 - sumX * sumX);
		if (denominator != 0) {
			slope = (n * sumXY - sumX * sumY) / denominator;
			intercept = (sumY - slope * sumX) / n;
		}

		// Prepare scatter data for ECharts
		List<List<Double>> scatterData = dataPoints.stream()
				.map(point -> {
					List<Double> p = new ArrayList<>();
					p.add(point[0]);
					p.add(point[1]);
					return p;
				})
				.collect(Collectors.toList());

		// Prepare regression line data for ECharts
		double minExp = dataPoints.stream().mapToDouble(p -> p[0]).min().orElse(0.0);
		double maxExp = dataPoints.stream().mapToDouble(p -> p[0]).max().orElse(0.0);

		List<List<Double>> regressionLine = new ArrayList<>();
		List<Double> linePoint1 = new ArrayList<>();
		linePoint1.add(minExp);
		linePoint1.add(slope * minExp + intercept);
		regressionLine.add(linePoint1);

		List<Double> linePoint2 = new ArrayList<>();
		linePoint2.add(maxExp);
		linePoint2.add(slope * maxExp + intercept);
		regressionLine.add(linePoint2);

		Map<String, Object> out = new HashMap<>();
		out.put("scatterData", scatterData);
		out.put("regressionLine", regressionLine);
		out.put("slope", slope);
		out.put("intercept", intercept);

		return success(out);
	}
    

	/**
	 * 企业薪资-规模四象限气泡图数据
	 * GET /visualization/job/companyBubble
	 * 返回: [{company, avgSalary, jobCount, scale}, ...]
	 */
	@GetMapping("/job/companyBubble")
	public AjaxResult companyBubble() {
		List<JobRaw> list = jobRawService.selectJobRawList(new JobRaw());
		
		// 按公司聚合数据
		Map<String, List<JobRaw>> companyMap = list.stream()
			.filter(j -> j.getCompany() != null && !j.getCompany().trim().isEmpty())
			.collect(Collectors.groupingBy(j -> j.getCompany().trim()));
		
		List<Map<String, Object>> result = new ArrayList<>();
		
		for (Map.Entry<String, List<JobRaw>> entry : companyMap.entrySet()) {
			String company = entry.getKey();
			List<JobRaw> jobs = entry.getValue();
			
			// 计算该公司的平均最高薪
			double avgHighSalary = jobs.stream()
				.filter(j -> j.getSalaryHigh() != null && j.getSalaryHigh().compareTo(BigDecimal.ZERO) > 0)
				.mapToDouble(j -> j.getSalaryHigh().doubleValue())
				.average()
				.orElse(0.0);
			
			// 岗位数量（规模）
			int jobCount = jobs.size();
			
			// 跳过无效数据
			if (avgHighSalary <= 0 || jobCount <= 0) continue;
			
			Map<String, Object> companyData = new HashMap<>();
			companyData.put("company", company);
			companyData.put("avgSalary", Math.round(avgHighSalary * 100) / 100.0); // 保留2位小数
			companyData.put("jobCount", jobCount);
			companyData.put("scale", jobCount); // 气泡大小 = 岗位数
			
			result.add(companyData);
		}
		
		// 按岗位数量降序排序，取前50家公司（避免数据过多）
		result = result.stream()
			.sorted((a, b) -> Integer.compare((Integer)b.get("jobCount"), (Integer)a.get("jobCount")))
			.limit(100)
			.collect(Collectors.toList());
		
		return success(result);
	}

	/**
	 * 岗位竞争力雷达图数据（5维评分）
	 * GET /visualization/job/competitivenessRadar
	 * 返回: [{name, value, jobCount, minSalary, maxSalary, avgLowSalary, avgHighSalary, totalScoreLow, totalScoreHigh, quadrant}, ...]
	 */
	@GetMapping("/job/competitivenessRadar")
	public AjaxResult competitivenessRadar() {
		List<JobRaw> list = jobRawService.selectJobRawList(new JobRaw());
		
		// 按职位领域分组（使用 field1 作为主要分类）
		Map<String, List<JobRaw>> fieldMap = list.stream()
			.filter(j -> j.getField1() != null && !j.getField1().trim().isEmpty())
			.collect(Collectors.groupingBy(j -> j.getField1().trim()));
		
		List<Map<String, Object>> result = new ArrayList<>();
		
		// 计算全局最大值用于归一化（分别计算最低薪和最高薪的最大值）
		double maxLowSalary = list.stream()
			.filter(j -> j.getSalaryLow() != null && j.getSalaryLow().compareTo(BigDecimal.ZERO) > 0)
			.mapToDouble(j -> j.getSalaryLow().doubleValue())
			.max().orElse(100.0);
		
		// ① 先算 P90（只算一次）
		double maxHighSalary = list.stream()
			.filter(j -> j.getSalaryHigh() != null && j.getSalaryHigh().compareTo(BigDecimal.ZERO) > 0)
			.mapToDouble(j -> j.getSalaryHigh().doubleValue())
			.sorted()
			.skip((long) (list.size() * 0.9))
			.findFirst()
			.orElse(100.0);
		
		// 经验映射
		Map<String, Double> expScoreMap = new HashMap<>();
		expScoreMap.put("不限", 20.0);
		expScoreMap.put("应届生", 30.0);
		expScoreMap.put("1年以内", 30.0);
		expScoreMap.put("1-3年", 50.0);
		expScoreMap.put("3-5年", 70.0);
		expScoreMap.put("5-10年", 85.0);
		expScoreMap.put("10年以上", 100.0);
		expScoreMap.put("在校/实习", 15.0);
		
		// 学历映射
		Map<String, Double> eduScoreMap = new HashMap<>();
		eduScoreMap.put("不限", 40.0);
		eduScoreMap.put("大专", 50.0);
		eduScoreMap.put("本科", 70.0);
		eduScoreMap.put("硕士", 90.0);
		eduScoreMap.put("博士", 100.0);
		eduScoreMap.put("中专", 30.0);
		eduScoreMap.put("高中", 25.0);
		
		// 取TOP 30职位领域
		fieldMap.entrySet().stream()
			.sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
			.limit(30)
			.forEach(entry -> {
				String fieldName = entry.getKey();
				List<JobRaw> jobs = entry.getValue();
				
				// 1. 薪资分（0-100）：分别计算最低薪和最高薪的平均值
				double avgLowSalary = jobs.stream()
					.filter(j -> j.getSalaryLow() != null && j.getSalaryLow().compareTo(BigDecimal.ZERO) > 0)
					.mapToDouble(j -> j.getSalaryLow().doubleValue())
					.average().orElse(0.0);
				// 分段阶梯 - 最低薪
				double salaryScoreLow =
					avgLowSalary < 6 ? 40 :
					avgLowSalary < 10 ? 60 :
					avgLowSalary < 15 ? 80 :
					avgLowSalary < 20 ? 90 : 100;
				
				double avgHighSalary = jobs.stream()
					.filter(j -> j.getSalaryHigh() != null && j.getSalaryHigh().compareTo(BigDecimal.ZERO) > 0)
					.mapToDouble(j -> j.getSalaryHigh().doubleValue())
					.average().orElse(0.0);
				double salaryScoreHigh = Math.min(100, (avgHighSalary / maxHighSalary) * 100);
				
				// 2. 经验分（0-100）：基于经验要求
				double avgExpScore = jobs.stream()
					.filter(j -> j.getExp() != null && !j.getExp().trim().isEmpty())
					.mapToDouble(j -> expScoreMap.getOrDefault(j.getExp().trim(), 50.0))
					.average().orElse(50.0);
				
				// 3. 学历分（0-100）：基于学历要求
				double avgEduScore = jobs.stream()
					.filter(j -> j.getEducation() != null && !j.getEducation().trim().isEmpty())
					.mapToDouble(j -> eduScoreMap.getOrDefault(j.getEducation().trim(), 50.0))
					.average().orElse(50.0);
				
				// 4. 行业热度分（0-100）：基于岗位数量
				long jobCount = jobs.size();
				double maxCount = fieldMap.values().stream()
					.mapToLong(List::size)
					.max().orElse(1);
				double hotScore = Math.min(100, (jobCount / maxCount) * 100);
				
				// 5. 技能深度分（0-100）：基于技能标签数量
				double avgSkillDepth = jobs.stream()
					.mapToDouble(j -> {
						int skillCount = 0;
						if (j.getField1() != null && !j.getField1().trim().isEmpty()) skillCount++;
						if (j.getField2() != null && !j.getField2().trim().isEmpty()) skillCount++;
						if (j.getField3() != null && !j.getField3().trim().isEmpty()) skillCount++;
						return skillCount;
					})
					.average().orElse(1.0);
				double skillScore = Math.min(100, (avgSkillDepth / 3.0) * 100);
				
				// 计算综合得分（分别计算基于最低薪和最高薪的综合得分）
				double totalScoreLow = (salaryScoreLow + avgExpScore + avgEduScore + hotScore + skillScore) / 5.0;
				double totalScoreHigh = (salaryScoreHigh + avgExpScore + avgEduScore + hotScore + skillScore) / 5.0;
				
				// 判断象限（基于薪资和热度，使用最高薪作为默认判断）
				String quadrant;
				if (salaryScoreHigh >= 60 && hotScore >= 60) {
					quadrant = "高薪高热";
				} else if (salaryScoreHigh >= 60 && hotScore < 60) {
					quadrant = "高薪冷门";
				} else if (salaryScoreHigh < 60 && hotScore >= 60) {
					quadrant = "低薪热门";
				} else {
					quadrant = "低薪冷门";
				}
				
				// 计算薪资区间（实际最小和最大值）
				double minSalary = jobs.stream()
					.filter(j -> j.getSalaryLow() != null && j.getSalaryLow().compareTo(BigDecimal.ZERO) > 0)
					.mapToDouble(j -> j.getSalaryLow().doubleValue())
					.min().orElse(0.0);
				double maxSalaryActual = jobs.stream()
					.filter(j -> j.getSalaryHigh() != null && j.getSalaryHigh().compareTo(BigDecimal.ZERO) > 0)
					.mapToDouble(j -> j.getSalaryHigh().doubleValue())
					.max().orElse(0.0);
				
				Map<String, Object> item = new HashMap<>();
				item.put("name", fieldName);
				
				// 保留1位小数 - 基于最低薪的5维评分
				List<Double> valuesLow = new ArrayList<>();
				valuesLow.add(Math.round(salaryScoreLow * 10) / 10.0);
				valuesLow.add(Math.round(avgExpScore * 10) / 10.0);
				valuesLow.add(Math.round(avgEduScore * 10) / 10.0);
				valuesLow.add(Math.round(hotScore * 10) / 10.0);
				valuesLow.add(Math.round(skillScore * 10) / 10.0);
				
				// 保留1位小数 - 基于最高薪的5维评分
				List<Double> valuesHigh = new ArrayList<>();
				valuesHigh.add(Math.round(salaryScoreHigh * 10) / 10.0);
				valuesHigh.add(Math.round(avgExpScore * 10) / 10.0);
				valuesHigh.add(Math.round(avgEduScore * 10) / 10.0);
				valuesHigh.add(Math.round(hotScore * 10) / 10.0);
				valuesHigh.add(Math.round(skillScore * 10) / 10.0);
				
				item.put("valueLow", valuesLow);   // 基于最低薪的评分
				item.put("valueHigh", valuesHigh); // 基于最高薪的评分
				item.put("jobCount", jobCount);
				item.put("minSalary", Math.round(minSalary * 10) / 10.0);           // 实际最低薪资
				item.put("maxSalary", Math.round(maxSalaryActual * 10) / 10.0);     // 实际最高薪资
				item.put("avgLowSalary", Math.round(avgLowSalary * 10) / 10.0);     // 平均最低薪
				item.put("avgHighSalary", Math.round(avgHighSalary * 10) / 10.0);   // 平均最高薪
				item.put("totalScoreLow", Math.round(totalScoreLow * 10) / 10.0);   // 基于最低薪的综合得分
				item.put("totalScoreHigh", Math.round(totalScoreHigh * 10) / 10.0); // 基于最高薪的综合得分
				item.put("quadrant", quadrant);
				
				result.add(item);
			});
		
		return success(result);
	}

	/**
	 * 计算列表的百分位数（线性插值）
	 */
	private static double percentile(List<Double> sorted, double percent) {
		if (sorted == null || sorted.isEmpty()) return 0d;
		double p = percent / 100.0;
		int n = sorted.size();
		double pos = (n - 1) * p;
		int idx = (int) Math.floor(pos);
		double diff = pos - idx;
		if (idx + 1 < n) {
			return sorted.get(idx) + diff * (sorted.get(idx + 1) - sorted.get(idx));
		} else {
			return sorted.get(idx);
		}
	}
    
}

