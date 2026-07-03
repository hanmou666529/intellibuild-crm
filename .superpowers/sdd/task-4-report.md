# Task 4: 后端 Service 接口 - 完成报告

## 创建的文件
- `ruoyi-system/src/main/java/com/ruoyi/crm/service/ICrmCustomerDisputeService.java`
  - 包含方法：`selectList`, `selectById`, `insert`, `handle`, `arbitrate`

## 修改的文件
- `ruoyi-system/src/main/java/com/ruoyi/crm/service/ICrmCustomerService.java`
  - 新增方法：`checkDuplicate(CrmCustomer)`, `mergeCustomer(Long, Long)`
  - 无需额外导入（`List` 和 `CrmCustomer` 已存在）

## 问题
无
