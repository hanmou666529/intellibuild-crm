# Task 6: 后端 Controller — 报告

## 创建的文件
- \uoyi-admin/src/main/java/com/ruoyi/web/controller/crm/CrmCustomerDisputeController.java\
  - \list\ (GET /crm/dispute/list) — 分页查询冲突列表
  - \getInfo\ (GET /crm/dispute/{disputeId}) — 查询冲突详情
  - \dd\ (POST /crm/dispute) — 新增冲突
  - \handle\ (PUT /crm/dispute/handle) — 处理冲突（分配/归还等）
  - \rbitrate\ (PUT /crm/dispute/arbitrate) — 仲裁冲突（管理员）

## 修改的文件
- \uoyi-admin/src/main/java/com/ruoyi/web/controller/crm/CrmCustomerController.java\
  - 新增 \checkDuplicate\ (GET /crm/customer/checkDuplicate) — 查重
  - 新增 \merge\ (POST /crm/customer/merge) — 合并客户

## 问题
- 无。编译通过，零错误。
