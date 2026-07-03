# Task 5: 后端 Service 实现

## Created
- `ruoyi-system/src/main/java/com/ruoyi/crm/service/impl/CrmCustomerDisputeServiceImpl.java`

## Modified
- `ruoyi-system/src/main/java/com/ruoyi/crm/service/impl/CrmCustomerServiceImpl.java`

## Method call approach
**Direct method calls** — `updateCustomerId` is already defined in all four mappers (`CrmFollowupMapper`, `CrmPipelineMapper`, `CrmContractMapper`, `CrmOrderMapper`), so direct calls are used instead of reflection.

## New methods in CrmCustomerServiceImpl
- `checkDuplicate(CrmCustomer)` — delegates to mapper
- `mergeCustomer(Long keep, Long merge)` — validates, transfers associated data via `updateCustomerId` on all four mappers, soft-deletes merged customer via `deleteByMerge`, closes disputes via `closeByCustomerId`, logs merge via `CrmCustomerMergeLogMapper.insert`

## New autowired deps in CrmCustomerServiceImpl
- `CrmFollowupMapper crmFollowupMapper` (was missing)
- `CrmCustomerDisputeMapper crmDisputeMapper`
- `CrmCustomerMergeLogMapper mergeLogMapper`
- `ICrmCustomerDisputeService disputeService`
