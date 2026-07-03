<template>
  <div class="app-container mod-order page-accent">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="订单编号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入订单编号" clearable style="width: 240px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="客户名称" prop="customerName">
        <el-input v-model="queryParams.customerName" placeholder="请输入客户名称" clearable style="width: 240px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="订单状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="订单状态" clearable style="width: 240px">
          <el-option v-for="dict in dict.type.crm_order_status" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['crm:order:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['crm:order:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['crm:order:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['crm:order:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

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
      <el-table-column label="订单来源" align="center" prop="source" width="110">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.source === 'contract'" type="success" size="mini">合同生成</el-tag>
          <el-tag v-else type="info" size="mini">手动创建</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="订单状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.crm_order_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="140">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="320" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleDetail(scope.row)">详情</el-button>
          <el-button size="mini" type="text" icon="el-icon-check" @click="handleMarkPaid(scope.row)" v-if="scope.row.status != '1' && scope.row.status != '2' && scope.row.status != '3'" v-hasPermi="['crm:order:paid']">标记付款</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['crm:order:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-close" @click="handleCancel(scope.row)" v-if="['0','4'].includes(scope.row.status)" v-hasPermi="['crm:order:edit']">取消订单</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-if="scope.row.status != '3'" v-hasPermi="['crm:order:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="客户名称" prop="customerId">
              <el-select v-model="form.customerId" placeholder="请选择客户" clearable filterable style="width: 100%">
                <el-option v-for="item in customerOptions" :key="item.customerId" :label="item.customerName" :value="item.customerId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="订单状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择订单状态" clearable style="width: 100%" :disabled="['2','4'].includes(form.status)">
                <el-option v-for="dict in formStatusOptions" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="产品明细">
              <el-button type="primary" size="mini" @click="handleAddProduct">添加产品</el-button>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-table :data="form.orderItems" border style="width: 100%">
              <el-table-column label="产品名称" align="center" prop="productName" width="180">
                <template slot-scope="scope">
                  <el-select v-model="scope.row.productId" placeholder="请选择产品" filterable style="width: 100%" @change="(val) => handleProductChange(scope.$index, val)">
                    <el-option v-for="item in productOptions" :key="item.productId" :label="item.productName" :value="item.productId" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="单价" align="center" prop="price" width="120">
                <template slot-scope="scope">
                  <el-input-number v-model="scope.row.price" :min="0" :precision="2" controls-position="right" style="width: 100px" @change="() => calcSubtotal(scope.$index)" />
                </template>
              </el-table-column>
              <el-table-column label="数量" align="center" prop="quantity" width="100">
                <template slot-scope="scope">
                  <el-input-number v-model="scope.row.quantity" :min="1" controls-position="right" style="width: 80px" @change="() => calcSubtotal(scope.$index)" />
                </template>
              </el-table-column>
              <el-table-column label="小计" align="center" prop="subtotal" width="120">
                <template slot-scope="scope">¥{{ scope.row.subtotal }}</template>
              </el-table-column>
              <el-table-column label="操作" align="center" width="60">
                <template slot-scope="scope">
                  <el-button size="mini" type="text" icon="el-icon-delete" @click="form.orderItems.splice(scope.$index, 1)"></el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-col>
        </el-row>
        <el-row style="margin-top: 10px">
          <el-col :span="8">
            <el-form-item label="总金额" prop="totalAmount">
              <el-input-number v-model="form.totalAmount" :min="0" :precision="2" :disabled="true" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="折扣金额" prop="discountAmount">
              <el-input-number v-model="form.discountAmount" :min="0" :precision="2" style="width: 100%" @change="calcActualAmount" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="实付金额" prop="actualAmount">
              <el-input-number v-model="form.actualAmount" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
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
            <el-form-item label="关联合同：">{{ detailForm.contractName || '-' }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合同编号：">{{ detailForm.contractNo || '-' }}</el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="订单状态：">
              <dict-tag :options="dict.type.crm_order_status" :value="detailForm.status" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="总金额：">¥{{ detailForm.totalAmount }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="订单来源：">{{ detailForm.source === 'contract' ? '合同生成' : '手动创建' }}</el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="折扣金额：">¥{{ detailForm.discountAmount }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实付金额：">¥{{ detailForm.actualAmount }}</el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="产品明细："></el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-table :data="detailForm.itemList" border style="width: 100%">
              <el-table-column label="产品名称" align="center" prop="productName" />
              <el-table-column label="单价" align="center" prop="productPrice" />
              <el-table-column label="数量" align="center" prop="quantity" />
              <el-table-column label="小计" align="center" prop="subtotal" />
            </el-table>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailOpen = false">关 闭</el-button>
      </div>
    </el-dialog>

    <el-dialog title="标记付款" :visible.sync="paidOpen" width="500px" append-to-body>
      <el-form ref="paidForm" :model="paidForm" :rules="paidRules" label-width="100px">
        <el-form-item label="订单编号">
          <span>{{ paidForm.orderNo }}</span>
        </el-form-item>
        <el-form-item label="订单金额">
          <span>¥{{ paidForm.actualAmount }}</span>
        </el-form-item>
        <el-form-item label="实付金额" prop="actualAmount">
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
  </div>
</template>

<script>
import { listOrder, getOrder, delOrder, addOrder, updateOrder, exportOrder, markOrderPaid } from "@/api/crm/order"
import { listCustomer } from "@/api/crm/customer"
import { listProduct } from "@/api/crm/product"
import '@/assets/styles/crm-table.scss'

export default {
  name: "CrmOrder",
  dicts: ['crm_order_status', 'crm_payment_method'],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      orderList: [],
      customerOptions: [],
      productOptions: [],
      title: "",
      open: false,
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
        status: undefined
      },
      form: {
        orderItems: []
      },
      rules: {
        customerId: [
          { required: true, message: "请选择客户", trigger: "change" }
        ],
        status: [
          { required: true, message: "请选择订单状态", trigger: "change" }
        ]
      }
    }
  },
  computed: {
    formStatusOptions() {
      return this.dict.type.crm_order_status.filter(d => ['0', '1', '3', '4'].includes(d.value))
    }
  },
  created() {
    this.getList()
    this.getCustomerList()
    this.getProductList()
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
      listOrder(this.queryParams).then(response => {
        this.orderList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    getCustomerList() {
      listCustomer({ pageNum: 1, pageSize: 9999 }).then(response => {
        this.customerOptions = response.rows
      })
    },
    getProductList() {
      listProduct({ pageNum: 1, pageSize: 9999 }).then(response => {
        this.productOptions = response.rows
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        orderId: undefined,
        customerId: undefined,
        status: undefined,
        totalAmount: 0,
        discountAmount: 0,
        actualAmount: 0,
        orderItems: []
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
      this.ids = selection.map(item => item.orderId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加订单"
    },
    handleUpdate(row) {
      this.reset()
      const orderId = row.orderId || this.ids
      getOrder(orderId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改订单"
      })
    },
    handleDelete(row) {
      const orderIds = row.orderId || this.ids
      this.$modal.confirm('是否确认删除订单编号为"' + orderIds + '"的数据项？').then(function() {
        return delOrder(orderIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    handleExport() {
      this.download('crm/order/export', {
        ...this.queryParams
      }, `order_${new Date().getTime()}.xlsx`)
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
    },
    handleCancel(row) {
      this.$modal.confirm('确认取消订单 "' + row.orderNo + '"？').then(function() {
        return updateOrder({ orderId: row.orderId, status: '3' })
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("取消成功")
      }).catch(() => {})
    },
    handleAddProduct() {
      if (!this.form.orderItems) {
        this.form.orderItems = []
      }
      this.form.orderItems.push({
        productId: undefined,
        productName: undefined,
        price: 0,
        quantity: 1,
        subtotal: 0
      })
    },
    handleProductChange(index, productId) {
      const product = this.productOptions.find(p => p.productId === productId)
      if (product) {
        this.form.orderItems[index].productName = product.productName
        this.form.orderItems[index].price = product.price
        this.calcSubtotal(index)
      }
    },
    calcSubtotal(index) {
      const item = this.form.orderItems[index]
      item.subtotal = (item.price || 0) * (item.quantity || 0)
      this.calcTotalAmount()
    },
    calcTotalAmount() {
      let total = 0
      for (const item of this.form.orderItems) {
        total += item.subtotal || 0
      }
      this.form.totalAmount = total
      this.calcActualAmount()
    },
    calcActualAmount() {
      this.form.actualAmount = (this.form.totalAmount || 0) - (this.form.discountAmount || 0)
      if (this.form.actualAmount < 0) {
        this.form.actualAmount = 0
      }
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.orderId != undefined) {
            updateOrder(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addOrder(this.form).then(response => {
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
