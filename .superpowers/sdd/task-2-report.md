# Task 2 Report: 后端 Domain 类

## Files Created
- `ruoyi-system/src/main/java/com/ruoyi/crm/domain/CrmCustomerDispute.java` — 客户查重申诉 domain 类
- `ruoyi-system/src/main/java/com/ruoyi/crm/domain/CrmCustomerMergeLog.java` — 客户合并日志 domain 类

## Files Modified
- `ruoyi-system/src/main/java/com/ruoyi/crm/domain/CrmCustomer.java`
  - Added `mergeToId` field after `delFlag`
  - Added `getMergeToId()` / `setMergeToId()` getter/setter
  - Added `mergeToId` to `toString()`

## Issues
None
