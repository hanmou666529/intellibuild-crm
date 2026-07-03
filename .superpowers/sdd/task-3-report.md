# Task 3 Report: 后端 Mapper（接口 + XML + 关联表 updateCustomerId）

## Files Created

| File | Status |
|------|--------|
| `ruoyi-system/src/main/java/com/ruoyi/crm/mapper/CrmCustomerDisputeMapper.java` | ✅ Created |
| `ruoyi-system/src/main/resources/mapper/crm/CrmCustomerDisputeMapper.xml` | ✅ Created |
| `ruoyi-system/src/main/java/com/ruoyi/crm/mapper/CrmCustomerMergeLogMapper.java` | ✅ Created |
| `ruoyi-system/src/main/resources/mapper/crm/CrmCustomerMergeLogMapper.xml` | ✅ Created |

## Files Modified

| File | Changes |
|------|---------|
| `CrmCustomerMapper.xml` | Added `checkDuplicate` select + `deleteByMerge` update |
| `CrmCustomerMapper.java` | Added `@Param` import + `checkDuplicate` + `deleteByMerge` methods |
| `CrmFollowupMapper.xml` | Added `updateCustomerId` |
| `CrmFollowupMapper.java` | Added `@Param` import + `updateCustomerId` method |
| `CrmPipelineMapper.xml` | Added `updateCustomerId` |
| `CrmPipelineMapper.java` | Added `@Param` import + `updateCustomerId` method |
| `CrmContractMapper.xml` | Added `updateCustomerId` |
| `CrmContractMapper.java` | Added `updateCustomerId` method |
| `CrmOrderMapper.xml` | Added `updateCustomerId` |
| `CrmOrderMapper.java` | Added `updateCustomerId` method |
| `CrmCustomerShareMapper.xml` | Added `updateCustomerId` (file already existed) |
| `CrmCustomerShareMapper.java` | Added `@Param` import + `updateCustomerId` method |

## Issues / Notes

- `CrmCustomerShareMapper.xml` already existed (not created from scratch). Added the `updateCustomerId` statement to the existing file.
- `CrmCustomerShareMapper.java` already existed — added `@Param` import and `updateCustomerId` method.
- `CrmFollowupMapper.java` and `CrmPipelineMapper.java` did not have `@Param` imports — added them.
- `CrmCustomerMapper.java` already used the existing RuoYi coding style (`public ...`), but the new methods follow concise style per spec. Both work with MyBatis.
- Task step 6f (CrmCustomerPoolLogMapper) was skipped per spec instructions.
