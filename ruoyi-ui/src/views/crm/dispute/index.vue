<template>
  <div class="app-container mod-dispute page-accent">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="纠纷状态" clearable style="width: 200px">
          <el-option label="待处理" :value="0" />
          <el-option label="已处理" :value="1" />
          <el-option label="已仲裁" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button size="mini" :type="statusTab === null ? 'primary' : ''" @click="switchTab(null)">全部</el-button>
        <el-button size="mini" :type="statusTab === 0 ? 'primary' : ''" @click="switchTab(0)">待处理</el-button>
        <el-button size="mini" :type="statusTab === 1 ? 'primary' : ''" @click="switchTab(1)">已处理</el-button>
        <el-button size="mini" :type="statusTab === 2 ? 'primary' : ''" @click="switchTab(2)">已仲裁</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" icon="el-icon-plus" size="mini" @click="handleAdd">新建争议</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="disputeList">
      <el-table-column label="客户名称" align="center" prop="customerName" min-width="140" :show-overflow-tooltip="true" />
      <el-table-column label="发起人" align="center" prop="initiatorName" width="100" />
      <el-table-column label="目标负责人" align="center" prop="targetUserName" width="100" />
      <el-table-column label="原因" align="center" prop="reason" min-width="200" :show-overflow-tooltip="true" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <el-tag :type="statusTagType(scope.row.status)" effect="plain">{{ statusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="处理人" align="center" prop="handlerName" width="100" />
      <el-table-column label="处理时间" align="center" prop="handleTime" width="150">
        <template slot-scope="scope">{{ parseTime(scope.row.handleTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="220" fixed="right">
        <template slot-scope="scope">
          <el-button v-if="scope.row.status === 0" size="mini" type="primary" icon="el-icon-edit" v-hasPermi="['crm:dispute:handle']" @click="handleOpen(scope.row)">处理</el-button>
          <el-button v-if="scope.row.status !== 2" size="mini" type="warning" icon="el-icon-s-operation" v-hasPermi="['crm:dispute:arbitrate']" @click="arbitrateOpen(scope.row)">仲裁</el-button>
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="纠纷处理" :visible.sync="handleDialogVisible" width="500px" append-to-body>
      <el-form ref="handleForm" :model="handleForm" :rules="handleRules" label-width="120px">
        <el-form-item label="客户名称">
          <span>{{ currentRow ? currentRow.customerName : '' }}</span>
        </el-form-item>
        <el-form-item label="处理方式" prop="action">
          <el-radio-group v-model="handleForm.action">
            <el-radio label="assign">分配给用户</el-radio>
            <el-radio label="escalate">上报超管</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="handleForm.action === 'assign'" label="目标用户" prop="targetUserId">
          <el-select v-model="handleForm.targetUserId" placeholder="请选择用户" clearable filterable style="width: 100%">
            <el-option v-for="item in userOptions" :key="item.userId" :label="item.label || item.nickName" :value="item.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="handleForm.remark" type="textarea" placeholder="请输入备注" :rows="3" maxlength="500" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitHandle">确 定</el-button>
        <el-button @click="handleDialogVisible = false">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="纠纷仲裁" :visible.sync="arbitrateDialogVisible" width="500px" append-to-body>
      <el-form ref="arbitrateForm" :model="arbitrateForm" :rules="arbitrateRules" label-width="120px">
        <el-form-item label="客户名称">
          <span>{{ currentRow ? currentRow.customerName : '' }}</span>
        </el-form-item>
        <el-form-item label="仲裁结果" prop="result">
          <el-radio-group v-model="arbitrateForm.result">
            <el-radio label="A">归发起人</el-radio>
            <el-radio label="B">归原负责人</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="arbitrateForm.remark" type="textarea" placeholder="请输入备注" :rows="3" maxlength="500" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitArbitrate">确 定</el-button>
        <el-button @click="arbitrateDialogVisible = false">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="发起争议" :visible.sync="addDialogVisible" width="500px" append-to-body>
      <el-form ref="addForm" :model="addForm" :rules="addRules" label-width="100px">
        <el-form-item label="客户" prop="customerName">
          <el-select v-model="addForm.customerId" placeholder="请搜索客户" filterable remote :remote-method="searchCustomer" clearable style="width: 100%">
            <el-option v-for="item in customerOptions" :key="item.customerId" :label="item.customerName + (item.phone ? ' (' + item.phone + ')' : '')" :value="item.customerId" />
          </el-select>
        </el-form-item>
        <el-form-item label="原因" prop="reason">
          <el-input v-model="addForm.reason" type="textarea" placeholder="请描述冲突原因" :rows="3" maxlength="500" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitAdd">确 定</el-button>
        <el-button @click="addDialogVisible = false">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="纠纷详情" :visible.sync="detailDialogVisible" width="600px" append-to-body>
      <el-descriptions :column="2" border size="small" v-if="detailData">
        <el-descriptions-item label="客户名称">{{ detailData.customerName }}</el-descriptions-item>
        <el-descriptions-item label="发起人">{{ detailData.initiatorName }}</el-descriptions-item>
        <el-descriptions-item label="目标负责人">{{ detailData.targetUserName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(detailData.status)" effect="plain">{{ statusLabel(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="原因" :span="2">{{ detailData.reason }}</el-descriptions-item>
        <el-descriptions-item label="处理人">{{ detailData.handlerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理时间">{{ parseTime(detailData.handleTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="仲裁结果" v-if="detailData.result">{{ detailData.result === 'A' ? '归发起人' : '归原负责人' }}</el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listDispute, getDispute, addDispute, handleDispute, arbitrateDispute } from "@/api/crm/dispute"
import { listUser } from "@/api/system/user"

export default {
  name: "CrmDispute",
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      disputeList: [],
      statusTab: null,
      userOptions: [],
      customerOptions: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        status: undefined
      },
      currentRow: null,
      handleDialogVisible: false,
      handleForm: {
        action: 'assign',
        targetUserId: undefined,
        remark: ''
      },
      handleRules: {
        action: [{ required: true, message: "请选择处理方式", trigger: "change" }],
        targetUserId: [{ required: true, message: "请选择目标用户", trigger: "change" }]
      },
      arbitrateDialogVisible: false,
      arbitrateForm: {
        result: undefined,
        remark: ''
      },
      arbitrateRules: {
        result: [{ required: true, message: "请选择仲裁结果", trigger: "change" }]
      },
      detailDialogVisible: false,
      detailData: null,
      addDialogVisible: false,
      addForm: {
        customerId: undefined,
        customerName: '',
        reason: ''
      },
      addRules: {
        customerId: [{ required: true, message: "请选择客户", trigger: "change" }],
        reason: [{ required: true, message: "请输入冲突原因", trigger: "blur" }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      const params = { ...this.queryParams }
      if (this.statusTab !== null) {
        params.status = this.statusTab
      }
      listDispute(params).then(response => {
        this.disputeList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    switchTab(status) {
      this.statusTab = status
      this.queryParams.pageNum = 1
      this.getList()
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm("queryForm")
      this.statusTab = null
      this.handleQuery()
    },
    statusLabel(status) {
      const map = { 0: '待处理', 1: '已处理', 2: '已仲裁' }
      return map[status] || '未知'
    },
    statusTagType(status) {
      const map = { 0: 'danger', 1: 'success', 2: 'info' }
      return map[status] || ''
    },
    handleOpen(row) {
      this.currentRow = row
      this.handleForm = { action: 'assign', targetUserId: undefined, remark: '' }
      this.handleDialogVisible = true
      this.getUserList()
      this.$nextTick(() => { if (this.$refs.handleForm) this.$refs.handleForm.clearValidate() })
    },
    getUserList() {
      listUser({ pageNum: 1, pageSize: 999 }).then(response => {
        this.userOptions = response.rows || []
      }).catch(() => { this.userOptions = [] })
    },
    submitHandle() {
      this.$refs["handleForm"].validate(valid => {
        if (!valid) return
        if (this.handleForm.action === 'assign' && !this.handleForm.targetUserId) {
          this.$modal.msgError("请选择目标用户")
          return
        }
        const data = {
          disputeId: this.currentRow.disputeId,
          action: this.handleForm.action,
          remark: this.handleForm.remark
        }
        if (this.handleForm.action === 'assign') {
          data.targetUserId = this.handleForm.targetUserId
        }
        handleDispute(data).then(() => {
          this.$modal.msgSuccess("处理成功")
          this.handleDialogVisible = false
          this.getList()
        }).catch(() => {})
      })
    },
    arbitrateOpen(row) {
      this.currentRow = row
      this.arbitrateForm = { result: undefined, remark: '' }
      this.arbitrateDialogVisible = true
      this.$nextTick(() => { if (this.$refs.arbitrateForm) this.$refs.arbitrateForm.clearValidate() })
    },
    submitArbitrate() {
      this.$refs["arbitrateForm"].validate(valid => {
        if (!valid) return
        this.$modal.confirm('确认仲裁结果为「' + (this.arbitrateForm.result === 'A' ? '归发起人' : '归原负责人') + '」？').then(() => {
          const data = {
            disputeId: this.currentRow.disputeId,
            result: this.arbitrateForm.result,
            remark: this.arbitrateForm.remark
          }
          arbitrateDispute(data).then(() => {
            this.$modal.msgSuccess("仲裁成功")
            this.arbitrateDialogVisible = false
            this.getList()
          }).catch(() => {})
        }).catch(() => {})
      })
    },
    handleView(row) {
      getDispute(row.disputeId).then(response => {
        this.detailData = response.data
        this.detailDialogVisible = true
      })
    },
    handleAdd() {
      this.addForm = { customerId: undefined, customerName: '', reason: '' }
      this.customerOptions = []
      this.addDialogVisible = true
      this.$nextTick(() => { if (this.$refs.addForm) this.$refs.addForm.clearValidate() })
    },
    searchCustomer(query) {
      if (!query) return
      import('@/api/crm/customer').then(mod => {
        mod.listCustomer({ pageNum: 1, pageSize: 20, customerName: query }).then(res => {
          this.customerOptions = res.rows || []
        })
      })
    },
    submitAdd() {
      this.$refs["addForm"].validate(valid => {
        if (!valid) return
        const customer = this.customerOptions.find(c => c.customerId === this.addForm.customerId)
        addDispute({
          customerId: this.addForm.customerId,
          customerName: customer ? customer.customerName : '',
          reason: this.addForm.reason
        }).then(() => {
          this.$modal.msgSuccess("发起成功")
          this.addDialogVisible = false
          this.getList()
        }).catch(() => {})
      })
    }
  }
}
</script>
