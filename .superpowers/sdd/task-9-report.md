# Task 9: 前端客户管理页改动 — 报告

## 修改的文件

- **`ruoyi-ui/src/views/crm/customer/index.vue`** (modified)

## 改动内容

### 1. 合并按钮
在操作按钮栏（删除按钮之后）添加了"合并"按钮，带权限控制 `v-hasPermi="['crm:customer:merge']"`。

### 2. 合并客户对话框
新增合并客户弹窗（`mergeOpen`），包含：
- 保留客户（远程搜索下拉）
- 被合并客户（远程搜索下拉）
- 表单校验（必填）
- 调用 `mergeCustomer` API

### 3. 重复检测对话框
在 `submitForm` 中添加逻辑：
- **新增**：先调用 `checkDuplicate` API 检测重复
- 无重复：直接 `addCustomer`
- 有重复：弹出"发现重复客户"对话框，展示重复列表及匹配字段
- 可"继续创建"（跳过检测直接保存）、"取消创建"（关闭并重置）、"发起争议"

### 4. 发起争议对话框
在争议弹窗中可选择目标负责人并填写原因，调用 `addDispute` API。

### 5. 新增依赖（已有 API）
- `checkDuplicate` — `@/api/crm/customer`
- `mergeCustomer` — `@/api/crm/customer`
- `addDispute` — `@/api/crm/dispute`
- `listUser` — `@/api/system/user`

## 问题
无。所有 API 均已存在，无需创建新文件。
