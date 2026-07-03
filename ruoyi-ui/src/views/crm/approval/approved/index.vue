<template>
  <div class="app-container mod-approval-approved page-accent">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="approvedList">
      <el-table-column label="请求ID" align="center" prop="requestId" width="80" />
      <el-table-column label="业务类型" align="center" prop="bizType" width="100">
        <template slot-scope="scope">
          <dict-tag :options="bizTypeOptions" :value="scope.row.bizType" />
        </template>
      </el-table-column>
      <el-table-column label="模板" align="center" prop="templateName" min-width="140" />
      <el-table-column label="业务摘要" align="center" min-width="200">
        <template slot-scope="scope">
          {{ parseBizInfo(scope.row.bizInfo) }}
        </template>
      </el-table-column>
      <el-table-column label="金额" align="center" prop="amount" width="120">
        <template slot-scope="scope">¥{{ scope.row.amount }}</template>
      </el-table-column>
      <el-table-column label="提交人" align="center" prop="submitName" width="100" />
      <el-table-column label="提交时间" align="center" prop="submitTime" width="150">
        <template slot-scope="scope">{{ parseTime(scope.row.submitTime) }}</template>
      </el-table-column>
      <el-table-column label="结果" align="center" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.crm_approval_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="100" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="审批详情" :visible.sync="detailOpen" width="600px" append-to-body>
      <el-descriptions :column="2" border size="small" v-if="currentRequest">
        <el-descriptions-item label="模板">{{ currentRequest.templateName }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">{{ currentRequest.bizType }}</el-descriptions-item>
        <el-descriptions-item label="金额">¥{{ currentRequest.amount }}</el-descriptions-item>
        <el-descriptions-item label="提交人">{{ currentRequest.submitName }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ parseTime(currentRequest.submitTime) }}</el-descriptions-item>
        <el-descriptions-item label="结果">
          <dict-tag :options="dict.type.crm_approval_status" :value="currentRequest.status" />
        </el-descriptions-item>
        <el-descriptions-item label="业务摘要" :span="2">{{ parseBizInfo(currentRequest.bizInfo) }}</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">审批节点</el-divider>
      <el-steps :active="currentStepIdx" direction="vertical" v-if="nodes.length">
        <el-step v-for="(n, i) in nodes" :key="i"
          :title="n.nodeLabel"
          :description="nodeDesc(n)"
          :status="n.status === '1' ? 'success' : (n.status === '2' ? 'error' : 'wait')" />
      </el-steps>
    </el-dialog>
  </div>
</template>

<script>
import { listApproved, getApprovalRequest, getApprovalNodes } from "@/api/crm/approval"

export default {
  name: "CrmApprovalApproved",
  dicts: ['crm_approval_status'],
  data() {
    return {
      loading: true, total: 0, approvedList: [],
      showSearch: true, detailOpen: false,
      currentRequest: null, nodes: [], currentStepIdx: 0,
      bizTypeOptions: [
        { label: '合同', value: 'contract', raw: { listClass: 'primary' } },
        { label: '订单', value: 'order', raw: { listClass: 'primary' } }
      ],
      queryParams: { pageNum: 1, pageSize: 10 }
    }
  },
  created() { this.getList() },
  activated() {
    if (!this.pollTimer) {
      this.pollTimer = setInterval(() => { this.getList() }, 5000)
    }
  },
  deactivated() {
    if (this.pollTimer) {
      clearInterval(this.pollTimer)
      this.pollTimer = null
    }
  },
  methods: {
    getList() {
      this.loading = true
      listApproved(this.queryParams).then(r => { this.approvedList = r.rows; this.total = r.total; this.loading = false })
    },
    parseBizInfo(info) {
      if (!info) return '-'
      try {
        const o = JSON.parse(info)
        return o.contractName || o.contractNo || info
      } catch(e) { return info }
    },
    handleView(row) {
      this.currentRequest = null; this.nodes = []
      getApprovalRequest(row.requestId).then(r => {
        this.currentRequest = r.data
        return getApprovalNodes(row.requestId)
      }).then(r => {
        this.nodes = r.data
        this.currentStepIdx = (this.currentRequest.currentStep || 1) - 1
        this.detailOpen = true
      })
    },
    nodeDesc(n) {
      const roleMap = { manager: '经理', director: '总监', boss: '总经理' }
      const role = roleMap[n.approverRole] || n.approverRole
      if (n.status === '1') return role + ' · ' + n.approverName + (n.comment ? ' · ' + n.comment : '')
      if (n.status === '2') return role + ' · ' + n.approverName + ' · 拒绝: ' + (n.comment || '')
      return role + ' · 待审批'
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() }
  }
}
</script>
