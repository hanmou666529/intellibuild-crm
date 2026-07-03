<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" icon="el-icon-plus" size="mini" @click="handleAdd">新增</el-button>
      </el-col>
    </el-row>

    <el-table :data="list" stripe border>
      <el-table-column type="index" label="序号" width="50" />
      <el-table-column prop="scene" label="场景标识" width="160" />
      <el-table-column prop="promptName" label="名称" width="160" />
      <el-table-column prop="promptContent" label="Prompt内容" min-width="300" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="80" :formatter="fmtStatus" />
      <el-table-column label="操作" width="180" fixed="right">
        <template slot-scope="{ row }">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleEdit(row)">编辑</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" style="color:#F56C6C" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="query.pageNum" :limit.sync="query.pageSize" @pagination="getList" />

    <el-dialog :title="form.promptId ? '编辑Prompt' : '新增Prompt'" :visible.sync="dialogVisible" width="700px">
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="场景标识" prop="scene">
          <el-input v-model="form.scene" placeholder="如 customer_intel，不可重复" />
        </el-form-item>
        <el-form-item label="名称" prop="promptName">
          <el-input v-model="form.promptName" placeholder="Prompt 名称" />
        </el-form-item>
        <el-form-item label="Prompt内容" prop="promptContent">
          <el-input type="textarea" v-model="form.promptContent" :rows="12" placeholder="请输入 Prompt 内容" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">启用</el-radio>
            <el-radio :label="1">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listPrompt, getPrompt, addPrompt, updatePrompt, delPrompt } from '@/api/crm/ai'

export default {
  data() {
    return {
      list: [],
      total: 0,
      query: { pageNum: 1, pageSize: 10 },
      dialogVisible: false,
      saving: false,
      form: { scene: '', promptName: '', promptContent: '', status: 0 },
      rules: {
        scene: [{ required: true, message: '场景标识不能为空', trigger: 'blur' }],
        promptName: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
        promptContent: [{ required: true, message: 'Prompt内容不能为空', trigger: 'blur' }]
      }
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      listPrompt(this.query).then(r => {
        this.list = r.rows || []
        this.total = r.total || 0
      })
    },
    handleAdd() {
      this.form = { scene: '', promptName: '', promptContent: '', status: 0 }
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.form?.clearValidate())
    },
    handleEdit(row) {
      getPrompt(row.promptId).then(r => {
        this.form = r.data
        this.dialogVisible = true
        this.$nextTick(() => this.$refs.form?.clearValidate())
      })
    },
    handleDelete(row) {
      this.$confirm('确认删除该 Prompt？').then(() => delPrompt(row.promptId).then(() => {
        this.getList()
        this.$message.success('删除成功')
      }))
    },
    save() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.saving = true
        const action = this.form.promptId ? updatePrompt(this.form) : addPrompt(this.form)
        action.then(() => {
          this.$message.success('保存成功')
          this.dialogVisible = false
          this.getList()
        }).finally(() => { this.saving = false })
      })
    },
    fmtStatus(row) {
      return row.status === 0 ? '启用' : '禁用'
    }
  }
}
</script>
