<template>
  <div class="app-container mod-pool page-accent">
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="公海客户" name="pool">
        <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
          <el-form-item label="客户名称" prop="customerName">
            <el-input v-model="queryParams.customerName" placeholder="请输入客户名称" clearable style="width: 240px" @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="手机号码" prop="phone">
            <el-input v-model="queryParams.phone" placeholder="请输入手机号码" clearable style="width: 240px" @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="客户来源" prop="source">
            <el-select v-model="queryParams.source" placeholder="客户来源" clearable style="width: 240px">
              <el-option v-for="dict in dict.type.crm_customer_source" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="poolList">
          <el-table-column label="客户名称" align="center" prop="customerName" :show-overflow-tooltip="true" />
          <el-table-column label="手机号码" align="center" prop="phone" width="120" />
          <el-table-column label="客户来源" align="center" prop="source" width="100">
            <template slot-scope="scope">
              <dict-tag :options="dict.type.crm_customer_source" :value="scope.row.source" />
            </template>
          </el-table-column>
          <el-table-column label="进入公海时间" align="center" prop="enterPoolTime" width="140">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.enterPoolTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" align="center" prop="createTime" width="140">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
            <template slot-scope="scope">
              <el-button size="mini" type="text" icon="el-icon-plus" @click="handleClaim(scope.row)" v-hasPermi="['crm:customer:claim']">领取</el-button>
            </template>
          </el-table-column>
        </el-table>

        <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
      </el-tab-pane>
      <el-tab-pane label="公海日志" name="log">
        <el-table v-loading="logLoading" :data="logList">
          <el-table-column label="客户名称" align="center" prop="customerName" :show-overflow-tooltip="true" />
          <el-table-column label="操作类型" align="center" prop="action" width="120">
            <template slot-scope="scope">
              <el-tag :type="scope.row.action === '0' ? 'danger' : 'success'">{{ {0:'放入公海',1:'领取',2:'分配'}[scope.row.action] || scope.row.action }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作人" align="center" prop="createBy" width="100" />
          <el-table-column label="原因" align="center" prop="reason" :show-overflow-tooltip="true" />
          <el-table-column label="操作时间" align="center" prop="createTime" width="140">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
        </el-table>

        <pagination v-show="logTotal > 0" :total="logTotal" :page.sync="logQuery.pageNum" :limit.sync="logQuery.pageSize" @pagination="getLogList" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { listPool, listPoolLog } from "@/api/crm/pool"
import { claimFromPool as claimCustomer } from "@/api/crm/customer"
import '@/assets/styles/crm-table.scss'

export default {
  name: "CrmPool",
  dicts: ['crm_customer_source'],
  data() {
    return {
      activeTab: 'pool',
      showSearch: true,
      loading: true,
      total: 0,
      poolList: [],
      logLoading: false,
      logTotal: 0,
      logList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        customerName: undefined,
        phone: undefined,
        source: undefined
      },
      logQuery: {
        pageNum: 1,
        pageSize: 10
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listPool(this.queryParams).then(response => {
        this.poolList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    getLogList() {
      this.logLoading = true
      listPoolLog(this.logQuery).then(response => {
        this.logList = response.rows
        this.logTotal = response.total
        this.logLoading = false
      })
    },
    handleTabClick(tab) {
      if (tab.name === 'log') {
        this.getLogList()
      }
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    handleClaim(row) {
      this.$modal.confirm('是否确认领取客户"' + row.customerName + '"？').then(function() {
        return claimCustomer(row.customerId)
      }).then(() => {
        this.$modal.msgSuccess("领取成功")
        this.getList()
      }).catch(() => {})
    }
  }
}
</script>
