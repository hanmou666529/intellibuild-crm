<template>
  <div class="app-container mod-notification page-accent">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入标题" clearable style="width: 240px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="通知类型" clearable style="width: 240px">
          <el-option v-for="dict in dict.type.crm_notification_type" :key="dict.value" :label="dict.label" :value="dict.value" />
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

    <el-table v-loading="loading" :data="notificationList" @row-click="handleRowClick">
      <el-table-column label="标题" align="center" prop="title" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <span :style="{ fontWeight: scope.row.isRead === 'N' ? 'bold' : 'normal' }">{{ scope.row.title }}</span>
        </template>
      </el-table-column>
      <el-table-column label="内容" align="center" prop="content" :show-overflow-tooltip="true" />
      <el-table-column label="类型" align="center" prop="type" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.crm_notification_type" :value="scope.row.type" />
        </template>
      </el-table-column>
      <el-table-column label="是否已读" align="center" prop="isRead" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.isRead === 'Y' ? 'info' : 'warning'">{{ scope.row.isRead === 'Y' ? '已读' : '未读' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="通知详情" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" label-width="80px" size="mini">
        <el-form-item label="标题：">{{ form.title }}</el-form-item>
        <el-form-item label="类型：">
          <dict-tag :options="dict.type.crm_notification_type" :value="form.type" />
        </el-form-item>
        <el-form-item label="内容：">
          <div style="white-space: pre-wrap; line-height: 1.6;">{{ form.content }}</div>
        </el-form-item>
        <el-form-item label="创建时间：">{{ parseTime(form.createTime) }}</el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="open = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listNotification, getNotification, markRead } from "@/api/crm/notification"
import '@/assets/styles/crm-table.scss'

export default {
  name: "CrmNotification",
  dicts: ['crm_notification_type'],
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      notificationList: [],
      title: "",
      open: false,
      form: {},
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: undefined,
        type: undefined
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listNotification(this.queryParams).then(response => {
        this.notificationList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    handleRowClick(row) {
      getNotification(row.notificationId).then(response => {
        this.form = response.data
        this.open = true
        if (row.isRead === 'N') {
          markRead(row.notificationId).then(() => {
            row.isRead = 'Y'
            this.getList()
          })
        }
      })
    }
  }
}
</script>
