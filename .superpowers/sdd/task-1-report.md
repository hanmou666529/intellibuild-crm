# Task 1 Report: 数据库变更

## 实现内容
- 创建 `sql/customer_dedup_merge.sql`，包含 2 张新表 + 1 个 ALTER TABLE

## 变更文件
| 文件 | 操作 |
|------|------|
| `sql/customer_dedup_merge.sql` | 新建 |
| `ry-vue.crm_customer_merge_log` | 新建表 |
| `ry-vue.crm_customer_dispute` | 新建表 |
| `ry-vue.crm_customer.merge_to_id` | 新增字段 |

## 验证结果
```
crm_customer_merge_log: True
crm_customer_dispute: True
crm_customer.merge_to_id: True
```

## 问题和关注点
无。所有 SQL 执行成功，建表、建索引、ALTER 均通过。
