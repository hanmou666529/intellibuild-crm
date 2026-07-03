CREATE TABLE IF NOT EXISTS sys_ai_copilot_session (
  session_id   VARCHAR(64) PRIMARY KEY,
  user_id      BIGINT NOT NULL,
  title        VARCHAR(200) DEFAULT '新对话',
  context      MEDIUMTEXT COMMENT '消息上下文 JSON 数组',
  create_time  DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI Copilot 会话';
