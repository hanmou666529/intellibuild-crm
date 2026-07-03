<template>
  <div class="op-guide-root">
    <div class="op-guide-trigger" @click="dialogVisible = true" title="操作指南">
      <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10"/>
        <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/>
        <line x1="12" y1="17" x2="12.01" y2="17"/>
      </svg>
    </div>

    <el-dialog
      :visible.sync="dialogVisible"
      title="操作指南"
      width="480px"
      top="8vh"
      class="guide-dialog"
      :close-on-click-modal="true"
      :append-to-body="true"
      :modal-append-to-body="true">
      <div class="guide-header">
        <span class="guide-role-badge">
          <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
            <circle cx="12" cy="7" r="4"/>
          </svg>
          {{ guideData.roleName }}
        </span>
        <span style="font-size:12px;color:#999;">以下为您的角色权限对应的操作步骤</span>
      </div>
      <div v-for="(section, i) in guideData.sections" :key="i" class="guide-section">
        <div class="guide-section-title">{{ section.title }}</div>
        <div v-for="(step, j) in section.steps" :key="j" class="guide-step">{{ step }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
const guides = {
  admin: {
    roleName: '超级管理员',
    sections: [
      { title: '系统管理', steps: ['用户管理：创建/编辑/删除系统用户并分配角色', '角色管理：定义角色权限和数据范围', '菜单管理：配置系统菜单和按钮权限', '部门管理：维护组织架构', '字典管理：管理系统数据字典'] },
      { title: 'CRM 配置', steps: ['审批模板：配置合同/订单审批流程规则', '标签管理：维护客户标签体系', '产品管理：管理产品和分类', '操作日志：查看所有操作记录'] },
      { title: '数据查看', steps: ['数据看板：查看全平台业务数据统计', '客户管理：查看并分配客户给员工', '公海客户：管理客户公海池'] }
    ]
  },
  manager: {
    roleName: '部门经理',
    sections: [
      { title: '客户管理', steps: ['查看部门客户列表', '客户分配：将公海客户分配给下属', '客户合并：合并重复客户', '争议管理：处理和仲裁客户争议'] },
      { title: '订单管理', steps: ['订单列表：查看部门订单', '标记付款：确认客户已付款', '待确认付款：审核待确认的订单'] },
      { title: '合同管理', steps: ['合同列表：查看部门合同', '合同审批：审批下属提交的合同', '终止合同：对履行中的合同进行终止操作'] },
      { title: '商机管理', steps: ['跟进记录：查看部门客户跟进情况', '销售管道：查看部门销售管道数据', '回款计划：管理部门回款计划，标记回款'] },
      { title: '审批中心', steps: ['待审批：处理待审批的合同/订单请求', '已处理：查看已审批的历史记录', '我发起的：查看自己提交的审批请求'] }
    ]
  },
  employee: {
    roleName: '普通员工',
    sections: [
      { title: '客户管理', steps: ['创建客户：录入新客户信息', '编辑客户：更新客户资料和联系方式', '客户导出：导出客户数据', '客户公海：查看公海客户并领取'] },
      { title: '商机跟进', steps: ['跟进记录：为客户创建跟进记录（电话/拜访/邮件）', '销售管道：管理销售机会阶段', '回款计划：创建和管理回款计划'] },
      { title: '订单管理', steps: ['创建订单：为客户创建新订单', '编辑订单：修改未确认的订单信息', '查看订单列表'] },
      { title: '合同管理', steps: ['创建合同：发起新合同并提交审批', '编辑合同：修改未审批的合同', '查看合同列表'] },
      { title: '基础功能', steps: ['数据看板：查看个人业绩统计', '产品查询：查看产品信息', '消息通知：查看系统通知', '我发起的：查看自己提交的审批进度'] }
    ]
  }
}

export default {
  name: 'OperationGuide',
  computed: {
    ...mapGetters(['roles']),
    currentRoleKey() {
      if (!this.roles || this.roles.length === 0) return 'employee'
      const first = this.roles[0]
      const key = typeof first === 'string' ? first : first.roleKey
      if (guides[key]) return key
      return 'employee'
    },
    guideData() {
      return guides[this.currentRoleKey]
    }
  },
  data() {
    return {
      dialogVisible: false
    }
  }
}
</script>

<style lang="scss" scoped>
.op-guide-trigger {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  cursor: pointer;
  color: rgba(0,0,0,0.45);
  transition: all 0.2s;
}
.op-guide-trigger:hover {
  background: rgba(0,0,0,0.06);
  color: #000;
}

.guide-dialog ::v-deep .el-dialog__body {
  padding: 20px 24px 28px;
}

.guide-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(0,0,0,0.06);
}

.guide-role-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 12px;
  border-radius: 999px;
  background: #f0f2f5;
  font-size: 12px;
  color: #666;
}

.guide-section {
  margin-bottom: 20px;
}
.guide-section:last-child {
  margin-bottom: 0;
}

.guide-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #000;
  margin-bottom: 8px;
  padding-left: 10px;
  border-left: 3px solid #0052D9;
  line-height: 1.4;
}

.guide-step {
  position: relative;
  padding: 5px 0 5px 28px;
  font-size: 13px;
  color: #444;
  line-height: 1.6;
}

.guide-step::before {
  content: '';
  position: absolute;
  left: 8px;
  top: 12px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #d0d5dd;
}
</style>
