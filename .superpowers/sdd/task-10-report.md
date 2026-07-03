# Task 10: 菜单+权限+Router 配置 — Report

## Files Modified

### 1. `ruoyi-ui/src/router/index.js`
Added route for 争议管理 in `dynamicRoutes`:

```javascript
{
  path: '/crm/dispute',
  component: Layout,
  hidden: false,
  children: [
    {
      path: 'index',
      component: () => import('@/views/crm/dispute/index'),
      name: 'CrmDispute',
      meta: { title: '争议管理', icon: 'exclamation-triangle' }
    }
  ]
}
```

Placed after the `/crm/approval` route block.

### 2. `sql/customer_dedup_merge.sql`
Appended menu + role_menu SQL.

### Files Created
None — both files already existed and were modified.

---

## SQL Added

### 争议管理菜单 (C type, parent=2000 智营CRM)
| menu_id | menu_name | parent_id | order_num | type | perms |
|---------|-----------|-----------|-----------|------|-------|
| 3023 | 争议管理 | 2000 | 13 | C | `crm:dispute:list` |

### 争议管理按钮权限 (F type, parent=3023)
| menu_id | menu_name | order_num | perms |
|---------|-----------|-----------|-------|
| 3024 | 争议查询 | 1 | `crm:dispute:query` |
| 3025 | 争议新增 | 2 | `crm:dispute:add` |
| 3026 | 争议处理 | 3 | `crm:dispute:handle` |
| 3027 | 争议仲裁 | 4 | `crm:dispute:arbitrate` |

### 客户合并按钮 (F type, parent=2020 客户管理)
| menu_id | menu_name | order_num | perms |
|---------|-----------|-----------|-------|
| 3028 | 客户合并 | 10 | `crm:customer:merge` |

### sys_role_menu (role_id=1)
All 6 menu_ids (3023–3028) granted to admin role via `INSERT IGNORE INTO sys_role_menu`.
