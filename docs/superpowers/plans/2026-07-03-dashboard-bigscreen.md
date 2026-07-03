# 数据大屏 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 将现有看板升级为 1920×1080 满屏深色大屏，带自动刷新和点击下钻

**Architecture:** 前端新建 `bigscreen.vue` + 路由，复用 `/crm/dashboard/stats` API；后端新增 drill-down 接口用于图表下钻

**Tech Stack:** Vue 2 + ECharts dark theme + Element UI Dialog

---

### Task 1：后端 Drill-Down API

**Files:**
- Modify: `ruoyi-system/src/main/java/com/ruoyi/crm/service/ICrmDashboardService.java`
- Modify: `ruoyi-system/src/main/java/com/ruoyi/crm/service/impl/CrmDashboardServiceImpl.java`
- Modify: `ruoyi-admin/src/main/java/com/ruoyi/web/controller/crm/CrmDashboardController.java`

- [ ] **Step 1: ICrmDashboardService 接口新增方法**

```java
public List<?> drillDown(String bizType, String key, CrmCustomer customer);
```

- [ ] **Step 2: CrmDashboardServiceImpl 实现 drillDown**

```java
@Override
public List<?> drillDown(String bizType, String key, CrmCustomer customer)
{
    if ("customer_source".equals(bizType))
    {
        CrmCustomer param = new CrmCustomer();
        if (customer != null && customer.getParams().get("dataScope") != null)
            param.getParams().put("dataScope", customer.getParams().get("dataScope"));
        param.getParams().put("source", key);
        return crmCustomerMapper.selectCrmCustomerList(param);
    }
    else if ("order_status".equals(bizType))
    {
        CrmOrder param = new CrmOrder();
        if (customer != null && customer.getParams().get("dataScope") != null)
            param.getParams().put("dataScope", customer.getParams().get("dataScope"));
        param.setStatus(key);
        return crmOrderMapper.selectCrmOrderList(param);
    }
    else if ("contract_status".equals(bizType))
    {
        CrmContract param = new CrmContract();
        if (customer != null && customer.getParams().get("dataScope") != null)
            param.getParams().put("dataScope", customer.getParams().get("dataScope"));
        param.setStatus(key);
        return crmContractMapper.selectCrmContractList(param);
    }
    else if ("top_product".equals(bizType))
    {
        // 返回该产品下的订单明细
        return crmOrderItemMapper.selectByProductName(key);
    }
    return new ArrayList<>();
}
```

- [ ] **Step 3: CrmOrderItemMapper 新增 selectByProductName**

CrmOrderItemMapper.java:
```java
public List<CrmOrderItem> selectByProductName(@Param("productName") String productName);
```

CrmOrderItemMapper.xml:
```xml
<select id="selectByProductName" resultMap="CrmOrderItemResult">
    SELECT i.*, o.order_no, p.product_name
    FROM crm_order_item i
    JOIN crm_order o ON i.order_id = o.order_id
    JOIN crm_product p ON i.product_id = p.product_id
    WHERE p.product_name = #{productName}
    LIMIT 50
</select>
```

- [ ] **Step 4: Controller 新增端点**

```java
@PreAuthorize("@ss.hasPermi('crm:dashboard:list')")
@CrmDataScope(deptAlias = "c", userAlias = "c")
@GetMapping("/drillDown")
public AjaxResult drillDown(@RequestParam String bizType, @RequestParam String key, CrmCustomer customer)
{
    List<?> list = crmDashboardService.drillDown(bizType, key, customer);
    return success(list);
}
```

- [ ] **Step 5: 验证编译**

Run: `mvn compile -pl ruoyi-admin -am -q`

---

### Task 2：大屏前端组件 bigscreen.vue

**Files:**
- Create: `ruoyi-ui/src/views/crm/dashboard/bigscreen.vue`
- Modify: `ruoyi-ui/src/router/index.js`
- Modify: `ruoyi-ui/src/api/crm/dashboard.js`

- [ ] **Step 1: API 新增 drillDown 方法**

`ruoyi-ui/src/api/crm/dashboard.js`:
```js
export function drillDown(bizType, key) {
  return request({
    url: '/crm/dashboard/drillDown',
    method: 'get',
    params: { bizType, key }
  })
}
```

- [ ] **Step 2: 路由添加大屏入口**

`ruoyi-ui/src/router/index.js` 在 `/crm/dashboard` children 中追加：

```js
{
  path: 'bigscreen',
  component: () => import('@/views/crm/dashboard/bigscreen'),
  name: 'CrmDashboardBigscreen',
  meta: { title: '数据大屏', icon: 'dashboard' }
}
```

- [ ] **Step 3: 新建 bigscreen.vue**

完整代码见下一节。

- [ ] **Step 4: 验证前端编译**

Run: `cd ruoyi-ui; npm run build:prod`

---

### Task 3：构建和部署

**Files：** 无

- [ ] **Step 1: 后端构建**

Run: `mvn clean package -DskipTests -q`

- [ ] **Step 2: 停止旧进程、复制 Jar、重启**

```bash
Get-Process -Name java -ErrorAction SilentlyContinue | ForEach-Object { taskkill /F /PID $_.Id }
Copy-Item -LiteralPath "ruoyi-admin/target/ruoyi-admin.jar" -Destination "ruoyi-admin.jar" -Force
Start-Process -NoNewWindow -FilePath "java" -ArgumentList "-jar","ruoyi-admin.jar"
```

- [ ] **Step 3: 验证**

```bash
# 登录获取 token
$r = curl.exe -s -X POST http://localhost:8080/login -H "Content-Type: application/json" -d "@$env:TEMP\login.json"
$token = ($r | ConvertFrom-Json).token
# 验证 drillDown
curl.exe -s "http://localhost:8080/crm/dashboard/drillDown?bizType=order_status&key=0" -H "Authorization: Bearer $token"
# 验证大屏路由可访问
curl.exe -s -o /dev/null -w "%{http_code}" "http://localhost:8080/#/crm/dashboard/bigscreen"
```
