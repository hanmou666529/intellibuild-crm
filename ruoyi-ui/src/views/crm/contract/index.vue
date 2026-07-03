<template>
  <div class="app-container mod-contract page-accent">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="合同编号" prop="contractNo">
        <el-input v-model="queryParams.contractNo" placeholder="请输入合同编号" clearable style="width: 240px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="合同名称" prop="contractName">
        <el-input v-model="queryParams.contractName" placeholder="请输入合同名称" clearable style="width: 240px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="客户名称" prop="customerName">
        <el-input v-model="queryParams.customerName" placeholder="请输入客户名称" clearable style="width: 240px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="合同状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="合同状态" clearable style="width: 240px">
          <el-option v-for="dict in dict.type.crm_contract_status" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['crm:contract:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['crm:contract:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['crm:contract:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['crm:contract:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="contractList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="合同编号" align="center" prop="contractNo" width="180" />
      <el-table-column label="合同名称" align="center" prop="contractName" :show-overflow-tooltip="true" />
      <el-table-column label="客户名称" align="center" prop="customerName" :show-overflow-tooltip="true" />
      <el-table-column label="合同金额" align="center" prop="amount" width="120">
        <template slot-scope="scope">¥{{ scope.row.amount }}</template>
      </el-table-column>
      <el-table-column label="合同状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.crm_contract_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="审批次数" align="center" prop="approvalCount" width="80" />
      <el-table-column label="最后审批时间" align="center" prop="lastApprovalTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.lastApprovalTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="签订日期" align="center" prop="signDate" width="120">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.signDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="附件" align="center" width="60">
        <template slot-scope="scope">
          <el-link type="primary" :href="attachmentUrl(scope.row.attachment)" target="_blank" :underline="false" v-if="scope.row.attachment" title="下载附件">
            <i class="el-icon-paperclip"></i>
          </el-link>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="280" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-check" @click="handleApprove(scope.row)" v-if="scope.row.status === '0'" v-hasPermi="['crm:contract:approve']">审批通过</el-button>
          <el-button size="mini" type="text" icon="el-icon-download" @click="handleDownload(scope.row)" v-if="scope.row.attachment">附件</el-button>
          <el-button size="mini" type="text" icon="el-icon-refresh" @click="handleReSubmit(scope.row)" v-if="scope.row.status === '1'" v-hasPermi="['crm:contract:edit']">重新提交审批</el-button>
          <el-button size="mini" type="text" icon="el-icon-circle-close" @click="handleTerminate(scope.row)" v-if="scope.row.status === '1'" v-hasPermi="['crm:contract:terminate']">终止合同</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['crm:contract:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['crm:contract:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
    <el-dialog :title="title" :visible.sync="open" width="900px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="合同名称" prop="contractName">
              <el-input v-model="form.contractName" placeholder="请输入合同名称" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户名称" prop="customerId">
              <el-select v-model="form.customerId" placeholder="请选择客户" clearable filterable style="width: 100%">
                <el-option v-for="item in customerOptions" :key="item.customerId" :label="item.customerName" :value="item.customerId" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="合同金额" prop="amount">
              <el-input-number v-model="form.amount" :min="0" :precision="2" style="width: 100%" :disabled="form.details && form.details.length > 0" />
              <el-tag v-if="form.details && form.details.length > 0" type="info" size="mini">由明细自动计算</el-tag>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="签订日期" prop="signDate">
              <el-date-picker v-model="form.signDate" type="date" placeholder="请选择签订日期" value-format="yyyy-MM-dd" style="width: 100%"></el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">订单明细</el-divider>
        <el-button size="mini" icon="el-icon-plus" @click="handleSelectProduct" style="margin-bottom: 10px">选择产品</el-button>
        <el-table :data="form.details" size="small" border style="margin-bottom: 10px" v-if="form.details && form.details.length > 0">
          <el-table-column label="产品名称" min-width="160" prop="productName" />
          <el-table-column label="单价" width="140">
            <template slot-scope="scope">
              <el-input-number v-model="scope.row.productPrice" :min="0" :precision="2" size="mini" style="width: 100%" @change="calcAmount" />
            </template>
          </el-table-column>
          <el-table-column label="数量" width="100">
            <template slot-scope="scope">
              <el-input-number v-model="scope.row.quantity" :min="1" size="mini" style="width: 100%" @change="calcAmount" />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="120">
            <template slot-scope="scope">
              {{ ((scope.row.productPrice || 0) * (scope.row.quantity || 0)).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="60">
            <template slot-scope="scope">
              <el-button size="mini" type="text" icon="el-icon-delete" @click="removeDetail(scope.$index)" />
            </template>
          </el-table-column>
        </el-table>
        <el-row>
          <el-col :span="12" v-if="form.contractId">
            <el-form-item label="合同状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择合同状态" clearable style="width: 100%" :disabled="form.hasLockedOrder">
                <el-option v-for="dict in dict.type.crm_contract_status" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
              <el-tag v-if="form.hasLockedOrder" type="warning" size="mini">关联订单已付款/已完成，状态锁定</el-tag>
            </el-form-item>
          </el-col>
          <el-col :span="12" :offset="form.contractId ? 0 : 12">
            <el-form-item label="附件" prop="attachment">
              <el-upload ref="upload" :limit="1" accept=".pdf,.doc,.docx,.xls,.xlsx" :headers="upload.headers" :action="upload.action" :auto-upload="true" :on-success="handleUploadSuccess" :on-remove="handleUploadRemove" :file-list="uploadFileList">
                <el-button size="small" type="primary">选择文件</el-button>
                <div slot="tip" class="el-upload__tip">支持 pdf/doc/docx/xls/xlsx</div>
              </el-upload>
              <el-link type="primary" :href="attachmentUrl(form.attachment)" target="_blank" v-if="form.attachment && uploadFileList.length === 0" :underline="false"><i class="el-icon-link"></i> 当前附件</el-link>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" maxlength="500" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="选择产品" :visible.sync="productSelectVisible" width="700px" append-to-body @open="loadProductList">
      <el-form :inline="true" size="small">
        <el-form-item label="产品名称">
          <el-input v-model="productQuery.productName" placeholder="搜索产品" clearable style="width: 200px" @keyup.enter.native="loadProductList" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="loadProductList">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="resetProductQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="allProductOptions" @selection-change="handleProductSelectionChange" ref="productTable" height="350">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="产品名称" prop="productName" min-width="160" />
        <el-table-column label="分类" prop="categoryName" width="120" />
        <el-table-column label="单价" prop="price" width="100" />
        <el-table-column label="单位" prop="unit" width="80" />
      </el-table>
      <pagination v-show="productTotal > 0" :total="productTotal" :page.sync="productQuery.pageNum" :limit.sync="productQuery.pageSize" @pagination="loadProductList" />
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="confirmProductSelection">确 定</el-button>
        <el-button @click="productSelectVisible = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listContract, getContract, delContract, addContract, updateContract, exportContract, approveContract, reSubmitApproval } from "@/api/crm/contract"
import { listCustomer } from "@/api/crm/customer"
import { listProduct } from "@/api/crm/product"
import { getToken } from "@/utils/auth"
import '@/assets/styles/crm-table.scss'
export default {
  name: "CrmContract",
  dicts: ['crm_contract_status'],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      contractList: [],
      customerOptions: [],
      productSelectVisible: false,
      allProductOptions: [],
      selectedProducts: [],
      productQuery: { pageNum: 1, pageSize: 10, productName: undefined },
      productTotal: 0,
      uploadFileList: [],
      title: "",
      open: false,
      upload: {
        headers: { Authorization: "Bearer " + getToken() },
        action: process.env.VUE_APP_BASE_API + "/common/upload"
      },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        contractNo: undefined,
        contractName: undefined,
        customerName: undefined,
        status: undefined
      },
      form: {},
      rules: {
        contractName: [
          { required: true, message: "合同名称不能为空", trigger: "blur" }
        ],
        customerId: [
          { required: true, message: "请选择客户", trigger: "change" }
        ],
        amount: [
          { required: true, message: "合同金额不能为空", trigger: "blur" }
        ],
        signDate: [
          { required: true, message: "请选择签订日期", trigger: "change" }
        ]
      }
    }
  },
  created() {
    this.getList()
    this.getCustomerList()
    this.$nextTick(() => {
      if (this.$route.query.customerId) {
        this.handleAdd()
        this.form.customerId = Number(this.$route.query.customerId)
      }
    })
  },
  methods: {
    getList() {
      this.loading = true
      listContract(this.queryParams).then(response => {
        this.contractList = response.rows
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
        contractId: undefined,
        contractName: undefined,
        customerId: undefined,
        amount: undefined,
        signDate: undefined,
        status: undefined,
        attachment: undefined,
        details: [],
        remark: undefined
      }
      this.uploadFileList = []
      this.allProductOptions = []
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
      this.ids = selection.map(item => item.contractId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加合同"
    },
    handleApprove(row) {
      this.$modal.confirm('确认审批通过合同 "' + row.contractName + '"？审批通过将自动创建关联订单。').then(() => {
        return approveContract(row.contractId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("审批通过")
      }).catch(() => {})
    },
    handleTerminate(row) {
      this.$modal.confirm('确认终止合同 "' + row.contractName + '"？终止后关联订单将自动变为已完成。').then(() => {
        return updateContract({ contractId: row.contractId, status: '3' })
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("合同已终止")
      }).catch(() => {})
    },
    handleReSubmit(row) {
      this.$modal.confirm('确认重新提交审批 "' + row.contractName + '"？关联的订单将被自动取消，审批通过后将重新创建订单。').then(() => {
        return reSubmitApproval(row.contractId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("已提交审批")
      }).catch(() => {})
    },
    attachmentUrl(attachment) {
      if (!attachment) return ''
      return attachment.startsWith('http') ? attachment : (process.env.VUE_APP_BASE_API + attachment)
    },
    handleDownload(row) {
      window.open(this.attachmentUrl(row.attachment), '_blank')
    },
    handleUpdate(row) {
      this.reset()
      const contractId = row.contractId || this.ids
      getContract(contractId).then(response => {
        this.form = response.data
        if (this.form.attachment) {
          const name = this.form.attachment.split('/').pop() || '附件'
          this.uploadFileList = [{ name: name, url: this.form.attachment }]
        }
        this.open = true
        this.title = "修改合同"
      })
    },
    handleDelete(row) {
      const contractIds = row.contractId || this.ids
      this.$modal.confirm('是否确认删除合同编号为"' + contractIds + '"的数据项？').then(() => {
        return delContract(contractIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    handleExport() {
      this.download('crm/contract/export', {
        ...this.queryParams
      }, `contract_${new Date().getTime()}.xlsx`)
    },
    // 上传成功回调
    handleUploadSuccess(res, file) {
      this.form.attachment = res.url
      this.uploadFileList = [{ name: file.name, url: res.url }]
    },
    // 删除附件回调
    handleUploadRemove() {
      this.form.attachment = undefined
      this.uploadFileList = []
    },
    handleSelectProduct() {
      this.productSelectVisible = true
    },
    loadProductList() {
      listProduct(this.productQuery).then(response => {
        this.allProductOptions = response.rows
        this.productTotal = response.total
      })
    },
    resetProductQuery() {
      this.productQuery = { pageNum: 1, pageSize: 10, productName: undefined }
      this.loadProductList()
    },
    handleProductSelectionChange(selection) {
      this.selectedProducts = selection
    },
    confirmProductSelection() {
      if (!this.selectedProducts || this.selectedProducts.length === 0) {
        this.$modal.msgWarning("请至少选择一个产品")
        return
      }
      if (!this.form.details) {
        this.form.details = []
      }
      for (const p of this.selectedProducts) {
        const existing = this.form.details.find(d => d.productId === p.productId)
        if (existing) {
          existing.quantity += 1
        } else {
          this.form.details.push({
            productId: p.productId,
            productName: p.productName,
            productPrice: p.price,
            quantity: 1,
            subtotal: p.price
          })
        }
      }
      this.calcAmount()
      this.productSelectVisible = false
    },
    removeDetail(index) {
      this.form.details.splice(index, 1)
      this.calcAmount()
    },
    calcAmount() {
      if (this.form.details && this.form.details.length > 0) {
        let total = 0
        for (const d of this.form.details) {
          total += (d.productPrice || 0) * (d.quantity || 0)
        }
        this.form.amount = Math.round(total * 100) / 100
      }
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          const data = {
            contractName: this.form.contractName,
            customerId: this.form.customerId,
            amount: this.form.amount,
            signDate: this.form.signDate,
            status: this.form.status,
            remark: this.form.remark || '',
            attachment: this.form.attachment,
            details: this.form.details || []
          }
          if (this.form.contractId) {
            data.contractId = this.form.contractId
          }
          if (!data.details || data.details.length === 0) {
            this.$modal.msgError("请添加至少一条合同明细")
            return
          }
          if (this.form.contractId != undefined) {
            updateContract(data).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addContract(data).then(response => {
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