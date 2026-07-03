<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="操作模块" prop="title">
        <el-select v-model="queryParams.title" placeholder="操作模块" clearable style="width: 160px">
          <el-option v-for="m in crmModules" :key="m" :label="m" :value="m" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作类型" prop="businessType">
        <el-select v-model="queryParams.businessType" placeholder="操作类型" clearable style="width: 160px">
          <el-option label="新增" :value="1" />
          <el-option label="修改" :value="2" />
          <el-option label="删除" :value="3" />
          <el-option label="导出" :value="5" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作人" prop="operName">
        <el-input v-model="queryParams.operName" placeholder="操作人" clearable style="width: 160px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['crm:audit:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="auditList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="操作模块" align="center" prop="title" width="100" />
      <el-table-column label="操作类型" align="center" width="80">
        <template slot-scope="scope">
          <dict-tag :options="sys_oper_type" :value="scope.row.businessType" />
        </template>
      </el-table-column>
      <el-table-column label="操作人" align="center" prop="operName" width="100" />
      <el-table-column label="所属部门" align="center" prop="deptName" width="120" />
      <el-table-column label="请求参数" align="center" :show-overflow-tooltip="true">
        <template slot-scope="scope">{{ formatParam(scope.row.operParam) }}</template>
      </el-table-column>
      <el-table-column label="IP" align="center" prop="operIp" width="130" />
      <el-table-column label="状态" align="center" width="70">
        <template slot-scope="scope">
          <dict-tag :options="sys_common_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作时间" align="center" prop="operTime" width="160" />
      <el-table-column label="耗时" align="center" width="70">
        <template slot-scope="scope">{{ scope.row.costTime }}ms</template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script>
import { listAudit, delAudit } from "@/api/crm/audit"

export default {
  name: "CrmAudit",
  dicts: ['sys_oper_type', 'sys_common_status'],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      auditList: [],
      crmModules: ['客户管理','合同管理','订单管理','回款计划','跟进记录','销售漏斗','产品管理'],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: undefined,
        businessType: undefined,
        operName: undefined
      }
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listAudit(this.queryParams).then(response => {
        this.auditList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    formatParam(param) {
      if (!param) return '-'
      try {
        const obj = typeof param === 'string' ? JSON.parse(param) : param
        return JSON.stringify(obj)
      } catch {
        return param
      }
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.operId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    handleDelete(row) {
      const operIds = row.operId || this.ids
      this.$modal.confirm('确认删除所选操作日志？').then(() => {
        return delAudit(operIds)
      }).then(() => { this.getList(); this.$modal.msgSuccess("删除成功") }).catch(() => {})
    }
  }
}
</script>
