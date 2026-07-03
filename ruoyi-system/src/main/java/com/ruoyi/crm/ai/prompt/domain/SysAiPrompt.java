package com.ruoyi.crm.ai.prompt.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import javax.validation.constraints.NotBlank;

public class SysAiPrompt extends BaseEntity {
    private Long id;
    @NotBlank private String scene;
    @NotBlank private String name;
    private String systemPrompt;
    private String userTemplate;
    private Double temperature;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getScene() { return scene; }
    public void setScene(String scene) { this.scene = scene; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSystemPrompt() { return systemPrompt; }
    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }
    public String getUserTemplate() { return userTemplate; }
    public void setUserTemplate(String userTemplate) { this.userTemplate = userTemplate; }
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
