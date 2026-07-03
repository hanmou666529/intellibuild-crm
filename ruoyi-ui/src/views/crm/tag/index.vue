<template>
  <div class="app-container mod-tag page-accent">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="标签名称" prop="tagName">
        <el-input v-model="queryParams.tagName" placeholder="标签名称" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="标签类型" prop="tagType">
        <el-select v-model="queryParams.tagType" placeholder="标签类型" clearable style="width: 160px">
          <el-option v-for="dict in dict.type.crm_tag_type" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['crm:tag:add']">新增标签</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['crm:tag:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="tagList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="标签名称" align="center" prop="tagName" width="150">
        <template slot-scope="scope">
          <el-tag :color="scope.row.color" style="color:#fff">{{ scope.row.tagName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="标签类型" align="center" width="120">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.crm_tag_type" :value="scope.row.tagType" />
        </template>
      </el-table-column>
      <el-table-column label="颜色" align="center" prop="color" width="120">
        <template slot-scope="scope">
          <span :style="{display:'inline-block',width:'20px',height:'20px',background:scope.row.color,borderRadius:'4px',verticalAlign:'middle'}"></span>
          <span style="margin-left:4px">{{ scope.row.color }}</span>
        </template>
      </el-table-column>
      <el-table-column label="排序" align="center" prop="sortOrder" width="60" />
      <el-table-column label="状态" align="center" width="80">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_normal_disable" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['crm:tag:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['crm:tag:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标签名称" prop="tagName">
          <el-input v-model="form.tagName" placeholder="请输入标签名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="标签类型" prop="tagType">
          <el-select v-model="form.tagType" placeholder="请选择标签类型" clearable style="width: 100%">
            <el-option v-for="dict in dict.type.crm_tag_type" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签颜色" prop="color">
          <el-color-picker v-model="form.color" show-alpha :predefine="['#f5222d','#fa8c16','#1890ff','#52c41a','#722ed1','#13c2c2','#eb2f96','#000']" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in dict.type.sys_normal_disable" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
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
import { listTag, getTag, addTag, updateTag, delTag } from "@/api/crm/tag"

export default {
  name: "CrmTag",
  dicts: ['crm_tag_type', 'sys_normal_disable'],
  data() {
    return {
      loading: true, ids: [], single: true, multiple: true,
      showSearch: true, total: 0, tagList: [], title: "", open: false,
      queryParams: { pageNum: 1, pageSize: 10, tagName: undefined, tagType: undefined },
      form: {},
      rules: {
        tagName: [{ required: true, message: "标签名称不能为空", trigger: "blur" }],
        tagType: [{ required: true, message: "请选择标签类型", trigger: "change" }]
      }
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listTag(this.queryParams).then(response => {
        this.tagList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    cancel() { this.open = false; this.reset() },
    reset() {
      this.form = { tagId: undefined, tagName: undefined, tagType: undefined, color: '#1890ff', sortOrder: 0, status: '0' }
      this.resetForm("form")
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    handleSelectionChange(selection) {
      this.ids = selection.map(i => i.tagId); this.single = selection.length != 1; this.multiple = !selection.length
    },
    handleAdd() { this.reset(); this.open = true; this.title = "新增标签" },
    handleUpdate(row) {
      this.reset()
      getTag(row.tagId).then(response => { this.form = response.data; this.open = true; this.title = "修改标签" })
    },
    handleDelete(row) {
      const ids = row.tagId || this.ids
      this.$modal.confirm('确认删除？').then(() => delTag(ids)).then(() => { this.getList(); this.$modal.msgSuccess("删除成功") }).catch(() => {})
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) return
        const fn = this.form.tagId ? updateTag : addTag
        fn(this.form).then(() => { this.$modal.msgSuccess("操作成功"); this.open = false; this.getList() })
      })
    }
  }
}
</script>
