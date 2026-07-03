<template>
  <div class="app-container mod-followup page-accent">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="客户名称" prop="customerName">
        <el-input v-model="queryParams.customerName" placeholder="请输入客户名称" clearable style="width: 240px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="跟进方式" prop="followupMode">
        <el-select v-model="queryParams.followupMode" placeholder="跟进方式" clearable style="width: 240px">
          <el-option v-for="dict in dict.type.crm_followup_mode" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="是否有效" prop="isEffective">
        <el-select v-model="queryParams.isEffective" placeholder="是否有效" clearable style="width: 240px">
          <el-option v-for="dict in dict.type.sys_yes_no" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['crm:followup:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['crm:followup:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['crm:followup:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="followupList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="客户名称" align="center" prop="customerName" :show-overflow-tooltip="true" />
      <el-table-column label="联系时间" align="center" prop="contactTime" width="140">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.contactTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="跟进方式" align="center" prop="followupMode" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.crm_followup_mode" :value="scope.row.followupMode" />
        </template>
      </el-table-column>
      <el-table-column label="跟进内容" align="center" prop="content" :show-overflow-tooltip="true" />
      <el-table-column label="是否有效" align="center" prop="isEffective" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_yes_no" :value="scope.row.isEffective" />
        </template>
      </el-table-column>
      <el-table-column label="创建人" align="center" prop="createBy" width="100" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="140">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['crm:followup:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['crm:followup:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="客户名称" prop="customerId">
              <el-select v-model="form.customerId" placeholder="请选择客户" clearable filterable style="width: 100%">
                <el-option v-for="item in customerOptions" :key="item.customerId" :label="item.customerName" :value="item.customerId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系时间" prop="contactTime">
              <el-date-picker v-model="form.contactTime" type="datetime" placeholder="请选择联系时间" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%"></el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="跟进方式" prop="followupMode">
              <el-select v-model="form.followupMode" placeholder="请选择跟进方式" clearable style="width: 100%">
                <el-option v-for="dict in dict.type.crm_followup_mode" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否有效" prop="isEffective">
              <el-select v-model="form.isEffective" placeholder="请选择" clearable style="width: 100%">
                <el-option v-for="dict in dict.type.sys_yes_no" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="跟进内容" prop="content">
              <el-input v-model="form.content" type="textarea" placeholder="请输入跟进内容" maxlength="1000" :rows="4" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listFollowup, getFollowup, delFollowup, addFollowup, updateFollowup, listFollowupByCustomer } from "@/api/crm/followup"
import { listCustomer } from "@/api/crm/customer"
import '@/assets/styles/crm-table.scss'

export default {
  name: "CrmFollowup",
  dicts: ['crm_followup_mode', 'sys_yes_no'],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      followupList: [],
      customerOptions: [],
      title: "",
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        customerName: undefined,
        followupMode: undefined,
        isEffective: undefined,
        customerId: undefined
      },
      form: {},
      rules: {
        customerId: [
          { required: true, message: "请选择客户", trigger: "change" }
        ],
        contactTime: [
          { required: true, message: "请选择联系时间", trigger: "change" }
        ],
        followupMode: [
          { required: true, message: "请选择跟进方式", trigger: "change" }
        ],
        content: [
          { required: true, message: "跟进内容不能为空", trigger: "blur" }
        ]
      }
    }
  },
  created() {
    const customerId = this.$route.query.customerId
    if (customerId) {
      this.queryParams.customerId = customerId
    }
    this.getList()
    this.getCustomerList()
  },
  methods: {
    getList() {
      this.loading = true
      listFollowup(this.queryParams).then(response => {
        this.followupList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    getCustomerList() {
      listCustomer({ pageNum: 1, pageSize: 9999 }).then(response => {
        this.customerOptions = response.rows
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        followupId: undefined,
        customerId: this.$route.query.customerId || undefined,
        contactTime: undefined,
        followupMode: undefined,
        isEffective: "Y",
        content: undefined
      }
      this.resetForm("form")
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.followupId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加跟进记录"
    },
    handleUpdate(row) {
      this.reset()
      const followupId = row.followupId || this.ids
      getFollowup(followupId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改跟进记录"
      })
    },
    handleDelete(row) {
      const followupIds = row.followupId || this.ids
      this.$modal.confirm('是否确认删除跟进记录编号为"' + followupIds + '"的数据项？').then(function() {
        return delFollowup(followupIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.followupId != undefined) {
            updateFollowup(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addFollowup(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    }
  }
}
</script>
