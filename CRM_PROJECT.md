# 智营CRM：AI驱动的客户关系管理

## 项目概述

基于 RuoYi-Vue 3.9.0 框架改造的 CRM 系统，前后端分离架构。

- **后端**: Spring Boot 2.x + MyBatis + MySQL 8.0
- **前端**: Vue 2.x + Element UI + ECharts
- **数据库**: `localhost:3306/ry-vue`，用户 `root`，密码 `Hae147258369`
- **构建**: Maven (`mvn clean install -DskipTests`) / npm (`npm run build:prod`)
- **启动**: `java -jar ruoyi-admin/target/ruoyi-admin.jar`
- **MySQL**: `D:\mysql\bin\mysql.exe`，服务名 `MySQL80`

---

## 数据库表结构 (11张)

| # | 表名 | 说明 |
|---|------|------|
| 1 | `crm_customer` | 客户信息（含 `is_pool` 公海标记、`belong_user_id` 归属人） |
| 2 | `crm_followup` | 跟进记录 |
| 3 | `crm_order` | 订单 |
| 4 | `crm_order_item` | 订单明细 |
| 5 | `crm_contract` | 合同 |
| 6 | `crm_pipeline` | 销售管道（线索/意向/报价/成交/回款） |
| 7 | `crm_product` | 产品 |
| 8 | `crm_product_category` | 产品分类 |
| 9 | `crm_notification` | 通知消息 |
| 10 | `crm_payment_plan` | 回款计划 |
| 11 | `crm_customer_pool_log` | 公海操作日志（`action`: 0=放入公海, 1=领取, 2=分配） |

---

## 后端代码结构

### Controller (11个) — `ruoyi-admin/.../controller/crm/`

| 文件 | 端点前缀 |
|------|---------|
| `CrmCustomerController.java` | `/crm/customer` |
| `CrmCustomerPoolController.java` | `/crm/pool` |
| `CrmFollowupController.java` | `/crm/followup` |
| `CrmOrderController.java` | `/crm/order` |
| `CrmContractController.java` | `/crm/contract` |
| `CrmPipelineController.java` | `/crm/pipeline` |
| `CrmProductController.java` | `/crm/product` |
| `CrmProductCategoryController.java` | `/crm/productCategory` |
| `CrmNotificationController.java` | `/crm/notification` |
| `CrmPaymentPlanController.java` | `/crm/paymentPlan` |
| `CrmDashboardController.java` | `/crm/dashboard` |

### Domain (11个) — `ruoyi-system/.../crm/domain/`

`CrmCustomer` `CrmFollowup` `CrmOrder` `CrmOrderItem` `CrmContract` `CrmPipeline` `CrmProduct` `CrmProductCategory` `CrmNotification` `CrmPaymentPlan` `CrmCustomerPool`

### Mapper (11个接口 + 11个 XML) — `ruoyi-system/.../crm/mapper/` + `resources/mapper/crm/`

### Service (11接口 + 11实现) — `ruoyi-system/.../crm/service/`

### 关键业务逻辑

- **客户管理**: `CrmCustomerServiceImpl` — CRUD、分配客户(`assignCustomer`)、放入公海(`putToPool`)、领取(`claimFromPool`)
- **Dashboard**: `CrmDashboardServiceImpl.getStats()` — 返回统计总览、来源/等级分布、销售漏斗、跟进趋势
- **公海池**: `CrmCustomerPoolServiceImpl` — 公海客户列表 + 日志查询

---

## 前端代码结构

### 页面 — `ruoyi-ui/src/views/crm/`

| 目录 | 页面 |
|------|------|
| `dashboard/` | 数据看板（4 stat 卡片 + 4 图表） |
| `customer/` | 客户管理列表 |
| `pool/` | 公海客户 + 公海日志（Tabs） |
| `followup/` | 跟进记录 |
| `order/` | 订单管理 |
| `contract/` | 合同管理 |
| `pipeline/` | 销售管道 |
| `product/` | 产品管理 |
| `notification/` | 通知消息 |
| `payment/` | 回款计划 |

### API — `ruoyi-ui/src/api/crm/` (11个文件)

### 核心样式

| 文件 | 作用 |
|------|------|
| `crm-minimal.scss` | 全局黑白极简覆盖 + Element UI pill 化 + 模块色彩系统 |
| `crm-table.scss` | 紧凑表格样式 |
| `element-variables.scss` | Element 主题色 #000 |
| `variables.scss` | 菜单全白 + 黑色文字 |

---

## 前端风格：黑白极简 (Black & White Minimal)

- **主色**: `#000` / `#fff` / `#f4f4f6`
- **字体**: Inter (300/400/500/600) via Google Fonts
- **导航**: 固定顶部毛玻璃导航 + pill 风格 TopNav（非 el-menu）
- **登录页**: 视频背景 + 毛玻璃卡片 + 渐变光晕 orbs + 浮动方块 + 鼠标视差
- **首页**: 全屏视频 Hero + 渐隐 footer + CTA
- **按钮/输入框/标签**: pill 圆角风格
- **表格**: 无边框 + 大写表头

### 模块色彩系统

每个模块拥有独立强调色 `--accent`:

| 模块 | CSS 类 | 强调色 |
|------|--------|--------|
| Dashboard | `mod-dashboard` | 蓝 `#0052D9` |
| 客户 | `mod-customer` | 绿 `#00A86B` |
| 订单 | `mod-order` | 紫 `#7C3AED` |
| 合同 | `mod-contract` | 青 `#0891B2` |
| 跟进 | `mod-followup` | 琥珀 `#D97706` |
| 管道 | `mod-pipeline` | 橙 `#FF6B35` |
| 产品 | `mod-product` | 玫红 `#DC2626` |
| 通知 | `mod-notification` | 灰蓝 `#4F46E5` |
| 回款 | `mod-payment` | 碧绿 `#0D9488` |
| 公海 | `mod-pool` | 靛蓝 `#4338CA` |

每个模块页面顶部有 3px 色条 + 标题左线条 + 卡片 hover 变色。

---

## 已知问题 & 今天的修复记录 (2026-07-01)

### 修复列表

| # | 问题 | 文件 | 修复 |
|---|------|------|------|
| 1 | `getOrDefault` 编译错误 | `CrmDashboardServiceImpl.java:58,73` | `row.get("name")` 强转为 `(String)` |
| 2 | `BigDecimal` 无法转 `Double` | `CrmPipelineServiceImpl.java:81` | 加 `.doubleValue()` |
| 3 | 跟进趋势图无数据 | `CrmDashboardServiceImpl.java:114-115` | 交换 `startDate`/`endDate`（索引 0 和 6 颠倒了） |
| 4 | 跟进趋势日期倒序 | `CrmDashboardServiceImpl.java:122-125` | `for (String date : last7Days)` → `for (int i = 6; i >= 0; i--)` |
| 5 | 跟进趋势前端渲染失败 | `dashboard/index.vue:153-181` | 改用 `notMerge` + 数据验证 + 避免 dispose/init 循环 |
| 6 | 放入公海 `Data too long for 'action'` | `CrmCustomerServiceImpl.java:110,130,155` | `"assign"/"enter"/"claim"` 改为 `"2"/"0"/"1"`（列类型 `char(1)`） |
| 7 | 公海日志 `operationType` 不显示 | `pool/index.vue:57-61` | `scope.row.operationType` → `scope.row.action`，映射 `0/1/2` |
| 8 | 公海客户在客户管理中仍出现 | `CrmCustomerMapper.xml:72-74` | 未传 `isPool` 时默认追加 `AND is_pool='0'` |

---

## 常用命令

```bash
# 后端构建
mvn clean install -DskipTests

# 后端单独编译（不打包）
mvn compile -pl ruoyi-system -am

# 前端构建
cd ruoyi-ui && npm run build:prod

# 启动后端
java -jar ruoyi-admin/target/ruoyi-admin.jar

# 查询 MySQL
"D:\mysql\bin\mysql.exe" -u root -p"Hae147258369" -D "ry-vue" -e "SELECT ..."
```
