<template>
  <div class="app-container mod-payment page-accent">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="订单编号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入订单编号" clearable style="width: 240px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="客户名称" prop="customerName">
        <el-input v-model="queryParams.customerName" placeholder="请输入客户名称" clearable style="width: 240px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 240px">
          <el-option v-for="dict in dict.type.crm_payment_status" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['crm:payment:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['crm:payment:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['crm:payment:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="paymentList" @selection-change="handleSelectionChange" :row-class-name="rowClassName">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="订单编号" align="center" prop="orderNo" width="180" />
      <el-table-column label="客户名称" align="center" prop="customerName" :show-overflow-tooltip="true" />
      <el-table-column label="计划金额" align="center" prop="planAmount" width="120">
        <template slot-scope="scope">¥{{ scope.row.planAmount }}</template>
      </el-table-column>
      <el-table-column label="实付金额" align="center" prop="actualAmount" width="120">
        <template slot-scope="scope">¥{{ scope.row.actualAmount }}</template>
      </el-table-column>
      <el-table-column label="计划日期" align="center" prop="planDate" width="120">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.planDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.crm_payment_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="支付方式" align="center" prop="paymentMethod" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.crm_payment_method" :value="scope.row.paymentMethod" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-check" @click="handleMarkPaid(scope.row)" v-if="scope.row.status != '1'" v-hasPermi="['crm:payment:edit']">标记已付</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['crm:payment:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['crm:payment:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="关联订单" prop="orderId">
              <el-select v-model="form.orderId" placeholder="请选择订单" clearable filterable style="width: 100%">
                <el-option v-for="item in orderOptions" :key="item.orderId" :label="item.orderNo" :value="item.orderId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划金额" prop="planAmount">
              <el-input-number v-model="form.planAmount" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="计划日期" prop="planDate">
              <el-date-picker v-model="form.planDate" type="date" placeholder="请选择计划日期" value-format="yyyy-MM-dd" style="width: 100%"></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择状态" clearable style="width: 100%">
                <el-option v-for="dict in dict.type.crm_payment_status" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
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

    <el-dialog title="标记已付" :visible.sync="paidOpen" width="500px" append-to-body>
      <el-form ref="paidForm" :model="paidForm" :rules="paidRules" label-width="100px">
        <el-form-item label="订单编号">
          <span>{{ paidForm.orderNo }}</span>
        </el-form-item>
        <el-form-item label="计划金额">
          <span>¥{{ paidForm.planAmount }}</span>
        </el-form-item>
        <el-form-item label="实付金额" prop="actualAmount">
          <el-input-number v-model="paidForm.actualAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="实付日期" prop="actualDate">
          <el-date-picker v-model="paidForm.actualDate" type="date" placeholder="请选择实付日期" value-format="yyyy-MM-dd" style="width: 100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="支付方式" prop="paymentMethod">
          <el-select v-model="paidForm.paymentMethod" placeholder="请选择支付方式" clearable style="width: 100%">
            <el-option v-for="dict in dict.type.crm_payment_method" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitPaidForm">确 定</el-button>
        <el-button @click="paidOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listPayment, getPayment, delPayment, addPayment, updatePayment, markPaid } from "@/api/crm/payment"
import { listOrder } from "@/api/crm/order"
import '@/assets/styles/crm-table.scss'

export default {
  name: "CrmPayment",
  dicts: ['crm_payment_status', 'crm_payment_method'],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      paymentList: [],
      orderOptions: [],
      title: "",
      open: false,
      paidOpen: false,
      paidForm: {},
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderNo: undefined,
        customerName: undefined,
        status: undefined
      },
      form: {},
      rules: {
        orderId: [
          { required: true, message: "请选择订单", trigger: "change" }
        ],
        planAmount: [
          { required: true, message: "计划金额不能为空", trigger: "blur" }
        ],
        planDate: [
          { required: true, message: "请选择计划日期", trigger: "change" }
        ]
      },
      paidRules: {
        actualAmount: [
          { required: true, message: "实付金额不能为空", trigger: "blur" }
        ],
        actualDate: [
          { required: true, message: "请选择实付日期", trigger: "change" }
        ],
        paymentMethod: [
          { required: true, message: "请选择支付方式", trigger: "change" }
        ]
      }
    }
  },
  created() {
    this.getList()
    this.getOrderList()
  },
  methods: {
    getList() {
      this.loading = true
      listPayment(this.queryParams).then(response => {
        this.paymentList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    getOrderList() {
      listOrder({ pageNum: 1, pageSize: 9999 }).then(response => {
        this.orderOptions = response.rows
      })
    },
    rowClassName({ row }) {
      if (row.status != '1' && row.planDate && new Date(row.planDate) < new Date()) {
        return 'overdue-row'
      }
      return ''
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        planId: undefined,
        orderId: undefined,
        planAmount: undefined,
        planDate: undefined,
        status: '0',
        remark: undefined
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
      this.ids = selection.map(item => item.planId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加回款计划"
    },
    handleUpdate(row) {
      this.reset()
      const planId = row.planId || this.ids
      getPayment(planId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改回款计划"
      })
    },
    handleDelete(row) {
      const planIds = row.planId || this.ids
      this.$modal.confirm('是否确认删除回款计划编号为"' + planIds + '"的数据项？').then(function() {
        return delPayment(planIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    handleMarkPaid(row) {
      this.paidForm = {
        planId: row.planId,
        orderNo: row.orderNo,
        planAmount: row.planAmount,
        actualAmount: row.planAmount,
        actualDate: new Date(),
        paymentMethod: undefined
      }
      this.paidOpen = true
    },
    submitPaidForm() {
      this.$refs["paidForm"].validate(valid => {
        if (valid) {
          markPaid(this.paidForm.planId, this.paidForm.actualAmount, this.paidForm.actualDate, this.paidForm.paymentMethod).then(response => {
            this.$modal.msgSuccess("标记已付成功")
            this.paidOpen = false
            this.getList()
          })
        }
      })
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.planId != undefined) {
            updatePayment(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addPayment(this.form).then(response => {
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

<style>
.el-table .overdue-row {
  background-color: #fef0f0;
}
.el-table .overdue-row:hover > td {
  background-color: #fde2e2;
}
</style>
