### Task 1: 数据库 DDL

**Goal:** Create SQL init script with DDL for AI module tables + insert initial prompt data

**Context:** This CRM project uses MySQL 8.0. Database name: `ry-vue`. MySQL path: `D:\mysql\bin\mysql.exe`. Password: `Hae147258369`.

**Files:**
- Create: `E:\CRM\RuoYi-Vue-master\sql\crm_ai_ddl.sql`

### Requirements (copy verbatim)

```sql
-- 1. sys_ai_prompt
CREATE TABLE IF NOT EXISTS sys_ai_prompt (
  id            bigint(20)    NOT NULL AUTO_INCREMENT,
  scene         varchar(50)   NOT NULL COMMENT '场景标识',
  name          varchar(100)  NOT NULL COMMENT '名称',
  system_prompt text          COMMENT '系统提示词',
  user_template text          COMMENT '用户消息模板',
  temperature   decimal(3,2)  DEFAULT 0.70 COMMENT '温度参数',
  status        char(1)       DEFAULT '1' COMMENT '0停用 1启用',
  create_by     varchar(64)   COMMENT '创建者',
  create_time   datetime      COMMENT '创建时间',
  update_by     varchar(64)   COMMENT '更新者',
  update_time   datetime      COMMENT '更新时间',
  remark        varchar(500)  COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_scene (scene)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 提示词模板';

-- 2. sys_ai_log
CREATE TABLE IF NOT EXISTS sys_ai_log (
  id                bigint(20)    NOT NULL AUTO_INCREMENT,
  user_id           bigint(20)    NOT NULL COMMENT '用户ID',
  scene             varchar(50)   NOT NULL COMMENT '场景',
  request_body      text          COMMENT '请求内容',
  response_body     text          COMMENT '响应内容',
  prompt_tokens     int(11)       DEFAULT 0 COMMENT '输入token数',
  completion_tokens int(11)       DEFAULT 0 COMMENT '输出token数',
  total_tokens      int(11)       DEFAULT 0 COMMENT '总token数',
  duration_ms       int(11)       DEFAULT 0 COMMENT '耗时ms',
  status            char(1)       DEFAULT '0' COMMENT '0成功 1失败',
  error_msg         varchar(500)  COMMENT '错误信息',
  create_time       datetime      COMMENT '调用时间',
  PRIMARY KEY (id),
  KEY idx_user (user_id),
  KEY idx_scene (scene),
  KEY idx_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 调用日志';

-- 3. crm_pipeline 扩展
ALTER TABLE crm_pipeline
  ADD COLUMN IF NOT EXISTS score        int(11)     DEFAULT NULL COMMENT 'AI 商机评分(0-100)',
  ADD COLUMN IF NOT EXISTS score_reason varchar(500) DEFAULT NULL COMMENT '评分理由',
  ADD COLUMN IF NOT EXISTS score_time   datetime    DEFAULT NULL COMMENT '评分时间';
```

Then insert initial prompt data (6 rows) for scenes: customer_intel, followup_generate, lead_scoring, report_daily, report_weekly, contract_review. Use the system_prompt and user_template values from the plan.

**Steps:**
1. Create SQL file with all DDL + INSERT
2. Apply it via MySQL CLI
3. Verify tables exist by running a SELECT COUNT(1) on each
