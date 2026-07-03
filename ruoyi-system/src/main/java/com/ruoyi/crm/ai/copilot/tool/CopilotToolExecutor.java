package com.ruoyi.crm.ai.copilot.tool;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.crm.domain.CrmContract;
import com.ruoyi.crm.domain.CrmCustomer;
import com.ruoyi.crm.domain.CrmFollowup;
import com.ruoyi.crm.domain.CrmOrder;
import com.ruoyi.crm.mapper.*;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class CopilotToolExecutor {
    private final CrmCustomerMapper customerMapper;
    private final CrmOrderMapper orderMapper;
    private final CrmContractMapper contractMapper;
    private final CrmFollowupMapper followupMapper;

    public CopilotToolExecutor(CrmCustomerMapper customerMapper,
                                CrmOrderMapper orderMapper,
                                CrmContractMapper contractMapper,
                                CrmFollowupMapper followupMapper) {
        this.customerMapper = customerMapper;
        this.orderMapper = orderMapper;
        this.contractMapper = contractMapper;
        this.followupMapper = followupMapper;
    }

    public List<Map<String, Object>> getToolDefinitions() {
        List<Map<String, Object>> tools = new ArrayList<>();
        tools.add(buildTool("query_customer", "搜索客户信息，支持按客户名称或手机号模糊搜索",
            Arrays.asList("keyword"), map(
                "keyword", map("type", "string", "description", "客户名称或手机号")
            )));
        tools.add(buildTool("query_order", "查询订单信息，可按客户名称、日期范围、状态筛选",
            Collections.<String>emptyList(), map(
                "customerName", map("type", "string", "description", "客户名称"),
                "startDate", map("type", "string", "description", "开始日期 yyyy-MM-dd"),
                "endDate", map("type", "string", "description", "结束日期 yyyy-MM-dd"),
                "status", map("type", "string", "description", "订单状态")
            )));
        tools.add(buildTool("query_contract", "查询合同信息，可按客户名称、状态筛选",
            Collections.<String>emptyList(), map(
                "customerName", map("type", "string", "description", "客户名称"),
                "status", map("type", "string", "description", "合同状态")
            )));
        tools.add(buildTool("query_followup", "查询客户跟进记录",
            Collections.singletonList("customerName"), map(
                "customerName", map("type", "string", "description", "客户名称"),
                "limit", map("type", "string", "description", "返回条数，默认10")
            )));
        tools.add(buildTool("query_dashboard", "查询销售统计数据，包括客户总数、本月新增、待跟进、本月成交金额",
            Collections.<String>emptyList(), map(
                "period", map("type", "string", "description", "统计周期: daily/weekly/monthly")
            )));
        return tools;
    }

    private Map<String, Object> buildTool(String name, String desc, List<String> required,
                                           Map<String, Object> properties) {
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("type", "object");
        parameters.put("properties", properties);
        parameters.put("required", required);
        Map<String, Object> function = new LinkedHashMap<>();
        function.put("name", name);
        function.put("description", desc);
        function.put("parameters", parameters);
        Map<String, Object> tool = new LinkedHashMap<>();
        tool.put("type", "function");
        tool.put("function", function);
        return tool;
    }

    public String execute(String name, String arguments) {
        try {
            Map<String, Object> args = JSON.parseObject(arguments);
            switch (name) {
                case "query_customer": return queryCustomer(args);
                case "query_order": return queryOrder(args);
                case "query_contract": return queryContract(args);
                case "query_followup": return queryFollowup(args);
                case "query_dashboard": return queryDashboard(args);
                default: return "{\"error\": \"未知工具: " + name + "\"}";
            }
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private String queryCustomer(Map<String, Object> args) {
        try {
            CrmCustomer query = new CrmCustomer();
            List<Map<String, Object>> results = new ArrayList<>();
            customerMapper.selectCrmCustomerList(query).stream()
                .filter(c -> {
                    String keyword = (String) args.get("keyword");
                    if (keyword == null) return true;
                    String name = c.getCustomerName();
                    String phone = c.getPhone();
                    return (name != null && name.contains(keyword))
                        || (phone != null && phone.contains(keyword));
                })
                .limit(10)
                .forEach(c -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", c.getCustomerId());
                    item.put("name", c.getCustomerName());
                    item.put("phone", c.getPhone());
                    item.put("level", c.getLevel());
                    item.put("source", c.getSource());
                    item.put("industry", c.getIndustry());
                    item.put("followStatus", c.getFollowStatus());
                    item.put("belongUserId", c.getBelongUserId());
                    results.add(item);
                });
            return JSON.toJSONString(Collections.singletonMap("data", results));
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private String queryOrder(Map<String, Object> args) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            List<Map<String, Object>> results = new ArrayList<>();
            orderMapper.selectCrmOrderList(new CrmOrder()).stream()
                .filter(o -> {
                    String customerName = (String) args.get("customerName");
                    if (customerName != null && o.getCustomerName() != null
                        && !o.getCustomerName().contains(customerName)) return false;
                    String status = (String) args.get("status");
                    if (status != null && !status.equals(o.getStatus())) return false;
                    return true;
                })
                .limit(20)
                .forEach(o -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", o.getOrderId());
                    item.put("orderNo", o.getOrderNo());
                    item.put("customerName", o.getCustomerName());
                    item.put("totalAmount", o.getTotalAmount());
                    item.put("actualAmount", o.getActualAmount());
                    item.put("status", o.getStatus());
                    item.put("createTime", o.getCreateTime() != null ? sdf.format(o.getCreateTime()) : null);
                    results.add(item);
                });
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("data", results);
            result.put("total", results.size());
            if (args.containsKey("startDate") || args.containsKey("endDate")) {
                Map<String, Object> filter = new LinkedHashMap<>();
                filter.put("startDate", args.getOrDefault("startDate", ""));
                filter.put("endDate", args.getOrDefault("endDate", ""));
                result.put("filter", filter);
            }
            return JSON.toJSONString(result);
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private String queryContract(Map<String, Object> args) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            List<Map<String, Object>> results = new ArrayList<>();
            contractMapper.selectCrmContractList(new CrmContract()).stream()
                .filter(c -> {
                    String customerName = (String) args.get("customerName");
                    if (customerName != null && c.getCustomerName() != null
                        && !c.getCustomerName().contains(customerName)) return false;
                    String status = (String) args.get("status");
                    if (status != null && !status.equals(c.getStatus())) return false;
                    return true;
                })
                .limit(20)
                .forEach(c -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", c.getContractId());
                    item.put("contractNo", c.getContractNo());
                    item.put("contractName", c.getContractName());
                    item.put("customerName", c.getCustomerName());
                    item.put("amount", c.getAmount());
                    item.put("status", c.getStatus());
                    item.put("signDate", c.getSignDate() != null ? sdf.format(c.getSignDate()) : null);
                    results.add(item);
                });
            return JSON.toJSONString(Collections.singletonMap("data", results));
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private String queryFollowup(Map<String, Object> args) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            int limit = 10;
            if (args.containsKey("limit")) {
                limit = Integer.parseInt((String) args.get("limit"));
            }
            List<Map<String, Object>> results = new ArrayList<>();
            String customerName = (String) args.get("customerName");
            followupMapper.selectCrmFollowupList(new CrmFollowup()).stream()
                .filter(f -> {
                    if (customerName != null && f.getCustomerName() != null
                        && !f.getCustomerName().contains(customerName)) return false;
                    return true;
                })
                .limit(limit)
                .forEach(f -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", f.getFollowupId());
                    item.put("customerName", f.getCustomerName());
                    item.put("type", f.getFollowupMode());
                    item.put("content", f.getContent());
                    item.put("status", f.getIsEffective());
                    item.put("contactTime", f.getContactTime() != null ? sdf.format(f.getContactTime()) : null);
                    results.add(item);
                });
            return JSON.toJSONString(Collections.singletonMap("data", results));
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private String queryDashboard(Map<String, Object> args) {
        try {
            CrmCustomer dashParam = new CrmCustomer();
            dashParam.getParams().put("today", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            dashParam.getParams().put("month", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("totalCustomers", customerMapper.countTotal(dashParam));
            data.put("newToday", customerMapper.countTodayNew(dashParam));
            data.put("pendingFollowup", customerMapper.countPendingFollowup(dashParam));
            Double monthDeal = orderMapper.sumMonthDealAmount(dashParam);
            data.put("monthDealAmount", monthDeal != null ? monthDeal : 0.0);
            return JSON.toJSONString(Collections.singletonMap("data", data));
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private static Map<String, Object> map(Object... entries) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            map.put((String) entries[i], entries[i + 1]);
        }
        return map;
    }
}
