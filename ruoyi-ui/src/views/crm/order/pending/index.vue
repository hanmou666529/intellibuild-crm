<template>
  <div class="app-container mod-order-pending">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="订单编号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入订单编号" clearable style="width: 240px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="客户名称" prop="customerName">
        <el-input v-model="queryParams.customerName" placeholder="请输入客户名称" clearable style="width: 240px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="orderList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="订单编号" align="center" prop="orderNo" width="180" />
      <el-table-column label="客户名称" align="center" prop="customerName" :show-overflow-tooltip="true" />
      <el-table-column label="订单总额" align="center" prop="totalAmount" width="120">
        <template slot-scope="scope">¥{{ scope.row.totalAmount }}</template>
      </el-table-column>
      <el-table-column label="实付金额" align="center" prop="actualAmount" width="120">
        <template slot-scope="scope">¥{{ scope.row.actualAmount }}</template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="140">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="success" icon="el-icon-check" v-hasPermi="['crm:order:paid']" @click="handleMarkPaid(scope.row)">标记付款</el-button>
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleDetail(scope.row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="标记付款" :visible.sync="paidOpen" width="500px" append-to-body>
      <el-form ref="paidForm" :model="paidForm" :rules="paidRules" label-width="100px">
        <el-form-item label="订单编号">
          <span>{{ paidForm.orderNo }}</span>
        </el-form-item>
        <el-form-item label="订单金额">
          <span>¥{{ paidForm.actualAmount }}</span>
        </el-form-item>
        <el-form-item label="实付金额" prop="paidAmount">
          <el-input-number v-model="paidForm.paidAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="付款日期" prop="lastPaymentTime">
          <el-date-picker v-model="paidForm.lastPaymentTime" type="date" placeholder="请选择付款日期" style="width: 100%"></el-date-picker>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitPaidForm">确 定</el-button>
        <el-button @click="paidOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="订单详情" :visible.sync="detailOpen" width="800px" append-to-body>
      <el-form ref="detailForm" :model="detailForm" label-width="100px" size="mini">
        <el-row>
          <el-col :span="12">
            <el-form-item label="订单编号：">{{ detailForm.orderNo }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户名称：">{{ detailForm.customerName }}</el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="总金额：">¥{{ detailForm.totalAmount }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实付金额：">¥{{ detailForm.actualAmount }}</el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listOrder, getOrder, markOrderPaid } from "@/api/crm/order"

export default {
  name: "CrmOrderPending",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      orderList: [],
      detailOpen: false,
      detailForm: {},
      paidOpen: false,
      paidForm: {},
      paidRules: {
        paidAmount: [
          { required: true, message: "实付金额不能为空", trigger: "blur" }
        ],
        lastPaymentTime: [
          { required: true, message: "请选择付款日期", trigger: "change" }
        ]
      },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderNo: undefined,
        customerName: undefined,
        status: '0,4'
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listOrder(this.queryParams).then(response => {
        this.orderList = response.rows
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
      this.queryParams.status = '0,4'
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.orderId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    handleDetail(row) {
      getOrder(row.orderId).then(response => {
        this.detailForm = response.data
        this.detailOpen = true
      })
    },
    handleMarkPaid(row) {
      this.paidForm = {
        orderId: row.orderId,
        orderNo: row.orderNo,
        actualAmount: row.actualAmount,
        paidAmount: row.actualAmount,
        lastPaymentTime: new Date()
      }
      this.paidOpen = true
    },
    submitPaidForm() {
      this.$refs["paidForm"].validate(valid => {
        if (valid) {
          markOrderPaid(this.paidForm).then(response => {
            this.$modal.msgSuccess("标记付款成功")
            this.paidOpen = false
            this.getList()
          })
        }
      })
    }
  }
}
</script>
