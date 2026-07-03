package com.ruoyi.raw.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import java.math.BigDecimal;

/**
 * 招聘原始数据对象 job_raw
 */
public class JobRaw extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String jobTitle;
    private String city;
    private String district;
    private String exp;
    private String education;
    private String company;

    /** 最低薪资(k) */
    @Excel(name = "最低薪资(k)")
    private BigDecimal salaryLow;

    /** 最高薪资(k) */
    @Excel(name = "最高薪资(k)")
    private BigDecimal salaryHigh;

    private String field1;
    private String field2;
    private String field3;
    private String field4;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getExp() { return exp; }
    public void setExp(String exp) { this.exp = exp; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public BigDecimal getSalaryLow() { return salaryLow; }
    public void setSalaryLow(Object salaryLow) {
        if (salaryLow instanceof String) {
            String salaryStr = (String) salaryLow;
            if (salaryStr != null && !salaryStr.isEmpty()) {
                try {
                    String cleanedSalary = salaryStr.trim().replaceAll("[^\\d.]", "");
                    this.salaryLow = new BigDecimal(cleanedSalary);
                } catch (NumberFormatException e) {
                    System.err.println("Error converting salaryLow: " + salaryStr + " to BigDecimal. " + e.getMessage());
                    this.salaryLow = null;
                }
            } else {
                this.salaryLow = null;
            }
        } else if (salaryLow instanceof BigDecimal) {
            this.salaryLow = (BigDecimal) salaryLow;
        } else {
            this.salaryLow = null;
        }
    }

    public BigDecimal getSalaryHigh() { return salaryHigh; }
    public void setSalaryHigh(Object salaryHigh) {
        if (salaryHigh instanceof String) {
            String salaryStr = (String) salaryHigh;
            if (salaryStr != null && !salaryStr.isEmpty()) {
                try {
                    String cleanedSalary = salaryStr.trim().replaceAll("[^\\d.]", "");
                    this.salaryHigh = new BigDecimal(cleanedSalary);
                } catch (NumberFormatException e) {
                    System.err.println("Error converting salaryHigh: " + salaryStr + " to BigDecimal. " + e.getMessage());
                    this.salaryHigh = null;
                }
            } else {
                this.salaryHigh = null;
            }
        } else if (salaryHigh instanceof BigDecimal) {
            this.salaryHigh = (BigDecimal) salaryHigh;
        } else {
            this.salaryHigh = null;
        }
    }

    public String getField1() { return field1; }
    public void setField1(String field1) { this.field1 = field1; }

    public String getField2() { return field2; }
    public void setField2(String field2) { this.field2 = field2; }

    public String getField3() { return field3; }
    public void setField3(String field3) { this.field3 = field3; }

    public String getField4() { return field4; }
    public void setField4(String field4) { this.field4 = field4; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("jobTitle", getJobTitle())
            .append("city", getCity())
            .append("district", getDistrict())
            .append("exp", getExp())
            .append("education", getEducation())
            .append("company", getCompany())
            .append("salaryLow", getSalaryLow())
            .append("salaryHigh", getSalaryHigh())
            .append("field1", getField1())
            .append("field2", getField2())
            .append("field3", getField3())
            .append("field4", getField4())
            .toString();
    }
}