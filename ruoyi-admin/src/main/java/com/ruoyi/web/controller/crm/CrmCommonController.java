package com.ruoyi.web.controller.crm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.TreeSelect;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.system.service.ISysDeptService;
import com.ruoyi.system.service.ISysRoleService;
import com.ruoyi.system.service.ISysUserService;

@RestController
@RequestMapping("/crm/common")
public class CrmCommonController extends BaseController
{
    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysDeptService deptService;

    @Autowired
    private SysDeptMapper deptMapper;

    @Autowired
    private ISysRoleService roleService;

    @PreAuthorize("@ss.hasPermi('crm:customer:query')")
    @GetMapping("/users")
    public AjaxResult users(@RequestParam(required = false) String roleKey)
    {
        SysUser param = new SysUser();
        if (StringUtils.isNotBlank(roleKey))
        {
            List<com.ruoyi.common.core.domain.entity.SysRole> roles = roleService.selectRoleList(new com.ruoyi.common.core.domain.entity.SysRole());
            for (com.ruoyi.common.core.domain.entity.SysRole r : roles)
            {
                if (roleKey.equals(r.getRoleKey()))
                {
                    param.setRoleId(r.getRoleId());
                    break;
                }
            }
        }
        List<SysUser> list = userService.selectUserList(param);
        List<Map<String, Object>> result = new ArrayList<>();
        for (SysUser u : list)
        {
            SysDept dept = u.getDept();
            String deptName = dept != null && dept.getDeptName() != null ? dept.getDeptName() : "";
            String label = deptName.isEmpty() ? u.getNickName() : deptName + " - " + u.getNickName();
            Map<String, Object> item = new HashMap<>();
            item.put("userId", u.getUserId());
            item.put("nickName", u.getNickName());
            item.put("deptName", deptName);
            item.put("label", label);
            result.add(item);
        }
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('crm:customer:query')")
    @GetMapping("/depts")
    public AjaxResult depts()
    {
        List<TreeSelect> tree = deptService.buildDeptTreeSelect(deptService.selectDeptList(new SysDept()));
        return success(tree);
    }

    @PreAuthorize("@ss.hasPermi('crm:customer:query')")
    @GetMapping("/shareDepts")
    public AjaxResult shareDepts()
    {
        SysDept param = new SysDept();
        List<SysDept> list = deptMapper.selectDeptList(param);
        List<TreeSelect> tree = deptService.buildDeptTreeSelect(list);
        return success(tree);
    }
}
