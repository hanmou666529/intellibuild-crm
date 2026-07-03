<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="职位" prop="jobTitle">
        <el-input
          v-model="queryParams.jobTitle"
          placeholder="请输入职位"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="城市" prop="city">
        <el-input
          v-model="queryParams.city"
          placeholder="请输入城市"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="区域" prop="district">
        <el-input
          v-model="queryParams.district"
          placeholder="请输入区域"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="经验" prop="exp">
        <el-input
          v-model="queryParams.exp"
          placeholder="请输入经验"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="学历" prop="education">
        <el-input
          v-model="queryParams.education"
          placeholder="请输入学历"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="公司" prop="company">
        <el-input
          v-model="queryParams.company"
          placeholder="请输入公司"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="最低薪资(k)" prop="salaryLow">
        <el-input
          v-model="queryParams.salaryLow"
          placeholder="请输入最低薪资(k)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="最高薪资(k)" prop="salaryHigh">
        <el-input
          v-model="queryParams.salaryHigh"
          placeholder="请输入最高薪资(k)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="领域1" prop="field1">
        <el-input
          v-model="queryParams.field1"
          placeholder="请输入领域1"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="领域2" prop="field2">
        <el-input
          v-model="queryParams.field2"
          placeholder="请输入领域2"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="领域3" prop="field3">
        <el-input
          v-model="queryParams.field3"
          placeholder="请输入领域3"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="领域4" prop="field4">
        <el-input
          v-model="queryParams.field4"
          placeholder="请输入领域4"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['system:raw:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:raw:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:raw:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:raw:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="rawList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="${comment}" align="center" prop="id" />
      <el-table-column label="职位" align="center" prop="jobTitle" />
      <el-table-column label="城市" align="center" prop="city" />
      <el-table-column label="区域" align="center" prop="district" />
      <el-table-column label="经验" align="center" prop="exp" />
      <el-table-column label="学历" align="center" prop="education" />
      <el-table-column label="公司" align="center" prop="company" />
      <el-table-column label="最低薪资(k)" align="center" prop="salaryLow" />
      <el-table-column label="最高薪资(k)" align="center" prop="salaryHigh" />
      <el-table-column label="领域1" align="center" prop="field1" />
      <el-table-column label="领域2" align="center" prop="field2" />
      <el-table-column label="领域3" align="center" prop="field3" />
      <el-table-column label="领域4" align="center" prop="field4" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:raw:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:raw:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改招聘原始数据对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="职位" prop="jobTitle">
          <el-input v-model="form.jobTitle" placeholder="请输入职位" />
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="form.city" placeholder="请输入城市" />
        </el-form-item>
        <el-form-item label="区域" prop="district">
          <el-input v-model="form.district" placeholder="请输入区域" />
        </el-form-item>
        <el-form-item label="经验" prop="exp">
          <el-input v-model="form.exp" placeholder="请输入经验" />
        </el-form-item>
        <el-form-item label="学历" prop="education">
          <el-input v-model="form.education" placeholder="请输入学历" />
        </el-form-item>
        <el-form-item label="公司" prop="company">
          <el-input v-model="form.company" placeholder="请输入公司" />
        </el-form-item>
        <el-form-item label="最低薪资(k)" prop="salaryLow">
          <el-input v-model="form.salaryLow" placeholder="请输入最低薪资(k)" />
        </el-form-item>
        <el-form-item label="最高薪资(k)" prop="salaryHigh">
          <el-input v-model="form.salaryHigh" placeholder="请输入最高薪资(k)" />
        </el-form-item>
        <el-form-item label="领域1" prop="field1">
          <el-input v-model="form.field1" placeholder="请输入领域1" />
        </el-form-item>
        <el-form-item label="领域2" prop="field2">
          <el-input v-model="form.field2" placeholder="请输入领域2" />
        </el-form-item>
        <el-form-item label="领域3" prop="field3">
          <el-input v-model="form.field3" placeholder="请输入领域3" />
        </el-form-item>
        <el-form-item label="领域4" prop="field4">
          <el-input v-model="form.field4" placeholder="请输入领域4" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listRaw, getRaw, delRaw, addRaw, updateRaw } from "@/api/system/raw"

export default {
  name: "Raw",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 招聘原始数据表格数据
      rawList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        jobTitle: null,
        city: null,
        district: null,
        exp: null,
        education: null,
        company: null,
        salaryLow: null,
        salaryHigh: null,
        field1: null,
        field2: null,
        field3: null,
        field4: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        jobTitle: [
          { required: true, message: "职位不能为空", trigger: "blur" }
        ],
        city: [
          { required: true, message: "城市不能为空", trigger: "blur" }
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询招聘原始数据列表 */
    getList() {
      this.loading = true
      listRaw(this.queryParams).then(response => {
        this.rawList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        jobTitle: null,
        city: null,
        district: null,
        exp: null,
        education: null,
        company: null,
        salaryLow: null,
        salaryHigh: null,
        field1: null,
        field2: null,
        field3: null,
        field4: null
      }
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加招聘原始数据"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getRaw(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改招聘原始数据"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateRaw(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addRaw(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除招聘原始数据编号为"' + ids + '"的数据项？').then(function() {
        return delRaw(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/raw/export', {
        ...this.queryParams
      }, `raw_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
