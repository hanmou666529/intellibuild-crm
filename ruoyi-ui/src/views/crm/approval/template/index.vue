<template>
  <div class="app-container mod-approval-template page-accent">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="模板名称" prop="templateName">
        <el-input v-model="queryParams.templateName" placeholder="模板名称" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="业务类型" prop="bizType">
        <el-select v-model="queryParams.bizType" placeholder="业务类型" clearable style="width: 160px">
          <el-option label="合同" value="contract" />
          <el-option label="订单" value="order" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['crm:approval:template:add']">新增模板</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['crm:approval:template:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="templateList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="模板名称" align="center" prop="templateName" min-width="160" />
      <el-table-column label="业务类型" align="center" prop="bizType" width="100">
        <template slot-scope="scope">
          <dict-tag :options="bizTypeOptions" :value="scope.row.bizType" />
        </template>
      </el-table-column>
      <el-table-column label="规则" align="center" min-width="300">
        <template slot-scope="scope">
          <el-tag v-for="(r, i) in parseRules(scope.row.rules)" :key="i" size="mini" style="margin:2px">
            {{ r.label }}{{ r.condition ? '(' + r.condition.field + r.condition.op + r.condition.value + ')' : '' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="80">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_normal_disable" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['crm:approval:template:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['crm:approval:template:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="700px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="form.templateName" placeholder="请输入模板名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="业务类型" prop="bizType">
          <el-select v-model="form.bizType" placeholder="请选择" clearable style="width:100%">
            <el-option label="合同" value="contract" />
            <el-option label="订单" value="order" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批节点" prop="rules">
          <div v-for="(item, idx) in ruleList" :key="idx" style="display:flex;margin-bottom:8px;gap:8px;align-items:center;flex-wrap:wrap">
            <span style="white-space:nowrap">第{{ idx+1 }}步</span>
            <el-input v-model="item.label" placeholder="节点名称" style="width:120px" size="small" />
            <el-select v-model="item.approveRole" placeholder="审批角色" style="width:130px" size="small">
              <el-option label="经理" value="manager" />
              <el-option label="总监" value="director" />
              <el-option label="总经理" value="boss" />
            </el-select>
            <el-checkbox v-model="item.hasCond" @change="onCondChange(idx)">条件路由</el-checkbox>
            <template v-if="item.hasCond">
              <el-select v-model="item.condition.field" style="width:90px" size="small">
                <el-option label="金额" value="amount" />
              </el-select>
              <el-select v-model="item.condition.op" style="width:70px" size="small">
                <el-option label=">=" value=">=" />
                <el-option label=">" value=">" />
                <el-option label="<=" value="<=" />
                <el-option label="<" value="<" />
              </el-select>
              <el-input-number v-model="item.condition.value" :min="0" style="width:120px" size="small" />
            </template>
            <el-button type="danger" icon="el-icon-delete" circle size="mini" @click="removeRule(idx)" />
          </div>
          <el-button type="primary" icon="el-icon-plus" size="mini" @click="addRule">添加节点</el-button>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="备注" maxlength="500" />
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
import { listTemplate, getTemplate, addTemplate, updateTemplate, delTemplate } from "@/api/crm/approval"

export default {
  name: "CrmApprovalTemplate",
  dicts: ['sys_normal_disable'],
  data() {
    return {
      loading: true, ids: [], single: true, multiple: true,
      showSearch: true, total: 0, templateList: [], title: "", open: false,
      bizTypeOptions: [
        { label: '合同', value: 'contract', raw: { listClass: 'primary' } },
        { label: '订单', value: 'order', raw: { listClass: 'primary' } }
      ],
      queryParams: { pageNum: 1, pageSize: 10, templateName: undefined, bizType: undefined },
      ruleList: [],
      form: {},
      rules: {
        templateName: [{ required: true, message: "模板名称不能为空", trigger: "blur" }],
        bizType: [{ required: true, message: "请选择业务类型", trigger: "change" }]
      }
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listTemplate(this.queryParams).then(r => { this.templateList = r.rows; this.total = r.total; this.loading = false })
    },
    parseRules(rules) {
      if (!rules) return []
      try { return JSON.parse(rules) } catch(e) { return [] }
    },
    addRule() {
      this.ruleList.push({ step: this.ruleList.length + 1, label: '', approveRole: 'manager', hasCond: false, condition: { field: 'amount', op: '>=', value: 50000 } })
    },
    removeRule(idx) { this.ruleList.splice(idx, 1); this.reindex() },
    onCondChange(idx) { if (!this.ruleList[idx].condition) { this.$set(this.ruleList[idx], 'condition', { field: 'amount', op: '>=', value: 50000 }) } },
    reindex() { this.ruleList.forEach((r, i) => r.step = i + 1) },
    cancel() { this.open = false; this.reset() },
    reset() {
      this.form = { templateId: undefined, templateName: undefined, bizType: 'contract', rules: undefined, remark: undefined, status: '0' }
      this.ruleList = []
      this.resetForm("form")
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    handleSelectionChange(s) { this.ids = s.map(i => i.templateId); this.single = s.length != 1; this.multiple = !s.length },
    handleAdd() { this.reset(); this.addRule(); this.open = true; this.title = "新增审批模板" },
    handleUpdate(row) {
      this.reset()
      getTemplate(row.templateId).then(r => {
        this.form = r.data
        const rules = this.parseRules(r.data.rules)
        this.ruleList = rules.map(rr => ({ ...rr, hasCond: !!rr.condition }))
        if (!this.ruleList.length) this.addRule()
        this.open = true; this.title = "修改审批模板"
      })
    },
    handleDelete(row) {
      const ids = row.templateId || this.ids
      this.$modal.confirm('确认删除？').then(() => delTemplate(ids)).then(() => { this.getList(); this.$modal.msgSuccess("删除成功") }).catch(() => {})
    },
    submitForm() {
      this.$refs["form"].validate(v => {
        if (!v) return
        this.form.rules = JSON.stringify(this.ruleList.map(r => ({
          step: r.step, label: r.label, approveRole: r.approveRole,
          condition: r.hasCond ? r.condition : null
        })))
        const fn = this.form.templateId ? updateTemplate : addTemplate
        fn(this.form).then(() => { this.$modal.msgSuccess("操作成功"); this.open = false; this.getList() })
      })
    }
  }
}
</script>
