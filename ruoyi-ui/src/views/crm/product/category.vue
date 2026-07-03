<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['crm:category:add']">新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="categoryList" row-key="categoryId" :tree-props="{ children: 'children', hasChildren: 'hasChildren' }">
      <el-table-column label="分类名称" align="center" prop="categoryName" />
      <el-table-column label="排序" align="center" prop="orderNum" width="80" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-plus" @click="handleAdd(scope.row)" v-hasPermi="['crm:category:add']">新增</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['crm:category:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['crm:category:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="上级分类" prop="parentId">
              <el-tree-select v-model="form.parentId" :data="categoryOptions" :props="{ label: 'categoryName', value: 'categoryId', children: 'children' }" placeholder="请选择上级分类" clearable filterable style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="分类名称" prop="categoryName">
              <el-input v-model="form.categoryName" placeholder="请输入分类名称" maxlength="50" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="排序" prop="orderNum">
              <el-input-number v-model="form.orderNum" :min="0" style="width: 100%" />
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
import { listCategory, getCategory, delCategory, addCategory, updateCategory, listCategoryTree } from "@/api/crm/category"

export default {
  name: "CrmCategory",
  data() {
    return {
      loading: true,
      showSearch: false,
      title: "",
      open: false,
      categoryList: [],
      categoryOptions: [],
      form: {},
      rules: {
        categoryName: [
          { required: true, message: "分类名称不能为空", trigger: "blur" }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listCategoryTree().then(response => {
        this.categoryList = response.data || []
        this.categoryOptions = JSON.parse(JSON.stringify(this.categoryList))
        this.loading = false
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        categoryId: undefined,
        parentId: undefined,
        categoryName: undefined,
        orderNum: 0
      }
      this.resetForm("form")
    },
    handleAdd(row) {
      this.reset()
      if (row && row.categoryId) {
        this.form.parentId = row.categoryId
      }
      this.open = true
      this.title = "添加产品分类"
    },
    handleUpdate(row) {
      this.reset()
      getCategory(row.categoryId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改产品分类"
      })
    },
    handleDelete(row) {
      this.$modal.confirm('是否确认删除分类"' + row.categoryName + '"的数据项？').then(function() {
        return delCategory(row.categoryId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.categoryId != undefined) {
            updateCategory(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addCategory(this.form).then(response => {
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
