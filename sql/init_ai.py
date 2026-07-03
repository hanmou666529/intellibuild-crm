# -*- coding: utf-8 -*-
import subprocess
import sys

DDL_SQL = r"""CREATE TABLE IF NOT EXISTS sys_ai_prompt (
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
"""

ALTER_SQL = r"""ALTER TABLE crm_pipeline ADD COLUMN score int(11) DEFAULT NULL COMMENT 'AI 商机评分(0-100)';
ALTER TABLE crm_pipeline ADD COLUMN score_reason varchar(500) DEFAULT NULL COMMENT '评分理由';
ALTER TABLE crm_pipeline ADD COLUMN score_time datetime DEFAULT NULL COMMENT '评分时间';
"""

INSERT_SQL = r"""DELETE FROM sys_ai_prompt WHERE scene IN ('customer_intel','followup_generate','lead_scoring','report_daily','report_weekly','contract_review');
INSERT INTO sys_ai_prompt (scene, name, system_prompt, user_template, temperature, status, create_time) VALUES
('customer_intel', '客户资料补全',
'你是 CRM 销售助理，擅长根据客户名称和已有信息推测补充缺失资料。请推测以下字段：客户行业、公司名、所在地区、联系方式。如果某个字段已存在则不覆盖。只返回 JSON 格式，不要额外说明。',
'客户名称：{name}\n已有信息：{knownFields}\n请补充缺失字段。',
0.7, '1', NOW()),
('followup_generate', '跟进记录生成',
'你是 CRM 销售助理。根据客户历史跟进记录和用户输入的关键词，生成一段专业的跟进记录（150字内）。包含：沟通内容摘要、客户意向判断、下一步建议。语气专业但自然。',
'客户：{customerName}\n近期跟进记录：{history}\n用户输入要点：{keywords}',
0.7, '1', NOW()),
('lead_scoring', '商机评分',
'你是一位资深销售分析师。分析以下客户数据，对每个客户给出 0-100 的商机评分。评分依据：最近跟进时间（越近越高）、成交金额（越高越高）、跟进频率（越频繁越高）、客户等级。输出格式：[{customerId, score, reason, action}]',
'{customers}',
0.3, '1', NOW()),
('report_daily', '日报生成',
'生成销售日报。格式：\n## 工作概览\n- 新增客户：X个\n- 跟进记录：X条\n- 成交金额：¥X\n\n## 重点跟进\n（按客户列举）\n\n## 存在问题\n\n## 明日计划',
'时间：{date}\n我的工作数据：{data}',
0.7, '1', NOW()),
('report_weekly', '周报生成',
'生成销售周报。格式：\n## 本周概览\n\n## 新增客户\n\n## 跟进情况\n\n## 成交情况\n\n## 存在问题\n\n## 下周计划',
'时间范围：{startDate} 至 {endDate}\n我的工作数据：{data}',
0.7, '1', NOW()),
('contract_review', '合同审查',
'你是一位法务风控专家。审查以下合同信息，识别风险项。每一项输出：\n风险等级（高/中/低）\n风险描述\n建议措施\n只返回 JSON 数组：[{level, risk, suggestion}]',
'合同编号：{contractNo}\n合同名称：{contractName}\n金额：{amount}\n期限：{startDate} ~ {endDate}\n明细：{items}',
0.3, '1', NOW());
"""

MYSQL = "D:\\mysql\\bin\\mysql.exe"
ARGS = ["-u", "root", "-pHae147258369", "ry-vue", "--default-character-set=utf8mb4"]

def run_sql(sql, desc):
    print(f"Running: {desc}...")
    p = subprocess.run([MYSQL] + ARGS, input=sql.encode("utf-8"), capture_output=True)
    if p.returncode != 0:
        err = p.stderr.decode("utf-8", errors="replace")
        if "Duplicate column name" in err or "already exists" in err:
            print(f"  Warning (ignored): {err[:100]}")
        else:
            print(f"  Error: {err[:200]}")
    else:
        print("  OK")

run_sql(DDL_SQL, "CREATE TABLE sys_ai_prompt + sys_ai_log")
run_sql(ALTER_SQL, "ALTER TABLE crm_pipeline ADD COLUMNS")
run_sql(INSERT_SQL, "INSERT initial prompts")

p = subprocess.run([MYSQL] + ARGS + ["-N", "-e", "SELECT scene, name FROM sys_ai_prompt"], capture_output=True)
out = p.stdout.decode("utf-8", errors="replace")
print(f"\nVerification:\n{out}")
