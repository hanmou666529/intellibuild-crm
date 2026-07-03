<template>
  <div class="app-container mod-pipeline page-accent">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['crm:pipeline:add']">新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <div class="kanban-board" v-loading="loading">
      <el-row :gutter="16">
        <el-col :span="4" v-for="(column, key) in stageColumns" :key="key">
          <div class="kanban-column">
            <div class="kanban-header" :style="{ background: column.color }">
              <span class="kanban-title">{{ column.label }}</span>
              <el-tag size="mini" type="plain" class="kanban-count">{{ column.list.length }}</el-tag>
            </div>
            <draggable v-model="column.list" :group="{ name: 'pipeline', pull: false, put: false }" :animation="200" class="kanban-body" @change="(evt) => onDragChange(evt, key)">
              <div class="kanban-card" v-for="item in column.list" :key="item.pipelineId" @click="handleUpdate(item)">
                <div class="card-title">{{ item.customerName }}</div>
                <div class="card-amount">¥{{ item.amount }}</div>
                <div class="card-probability">
                  <el-progress :percentage="item.probability || 0" :stroke-width="6" size="small"></el-progress>
                </div>
                <div class="card-stage-actions">
                  <el-dropdown size="mini" @command="(command) => changeStage(item, command)">
                    <el-button size="mini" type="text">变更阶段</el-button>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item v-for="(st, sk) in stageColumns" :key="sk" :command="sk" :disabled="sk === key">{{ st.label }}</el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                  <el-button size="mini" type="text" icon="el-icon-delete" @click.stop="handleDelete(item)"></el-button>
                </div>
              </div>
            </draggable>
          </div>
        </el-col>
      </el-row>
    </div>

    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="客户名称" prop="customerName">
              <el-input v-model="form.customerName" placeholder="请输入客户名称" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属阶段" prop="stage">
              <el-select v-model="form.stage" placeholder="请选择阶段" clearable style="width: 100%">
                <el-option v-for="(column, key) in stageColumns" :key="key" :label="column.label" :value="key" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="预计金额" prop="amount">
              <el-input-number v-model="form.amount" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成交概率(%)" prop="probability">
              <el-input-number v-model="form.probability" :min="0" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="预计成交日期" prop="expectedDealDate">
              <el-date-picker v-model="form.expectedDealDate" type="date" placeholder="请选择日期" value-format="yyyy-MM-dd" style="width: 100%"></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人" prop="belongUserName">
              <el-input v-model="form.belongUserName" placeholder="请输入负责人" maxlength="50" />
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
  </div>
</template>

<script>
import { listPipeline, getPipeline, delPipeline, addPipeline, updatePipeline, updateStage } from "@/api/crm/pipeline"
import draggable from 'vuedraggable'
import '@/assets/styles/crm-table.scss'

export default {
  name: "CrmPipeline",
  components: { draggable },
  data() {
    return {
      loading: true,
      showSearch: false,
      title: "",
      open: false,
      stageColumns: {
        clue: { label: '线索', color: '#909399', list: [] },
        intent: { label: '意向', color: '#409EFF', list: [] },
        quote: { label: '报价', color: '#E6A23C', list: [] },
        deal: { label: '成交', color: '#67C23A', list: [] },
        payment: { label: '回款', color: '#F56C6C', list: [] }
      },
      form: {},
      rules: {
        customerName: [
          { required: true, message: "客户名称不能为空", trigger: "blur" }
        ],
        stage: [
          { required: true, message: "请选择阶段", trigger: "change" }
        ],
        amount: [
          { required: true, message: "预计金额不能为空", trigger: "blur" }
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
      listPipeline({ pageNum: 1, pageSize: 9999 }).then(response => {
        const rows = response.rows || []
        for (const key in this.stageColumns) {
          this.stageColumns[key].list = rows.filter(item => item.stage === key)
        }
        this.loading = false
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        pipelineId: undefined,
        customerName: undefined,
        stage: 'clue',
        amount: undefined,
        probability: 0,
        expectedDealDate: undefined,
        belongUserName: undefined,
        remark: undefined
      }
      this.resetForm("form")
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加管道"
    },
    handleUpdate(row) {
      this.reset()
      getPipeline(row.pipelineId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改管道"
      })
    },
    handleDelete(row) {
      this.$modal.confirm('是否确认删除管道"' + row.customerName + '"的数据项？').then(function() {
        return delPipeline(row.pipelineId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    changeStage(item, newStage) {
      updateStage(item.pipelineId, newStage).then(response => {
        this.$modal.msgSuccess("阶段变更成功")
        this.getList()
      })
    },
    onDragChange(evt, stage) {
      if (evt.added) {
        const movedItem = evt.added.element
        if (movedItem.stage !== stage) {
          updateStage(movedItem.pipelineId, stage).then(response => {
            this.$modal.msgSuccess("阶段变更成功")
            this.getList()
          })
        }
      }
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.pipelineId != undefined) {
            updatePipeline(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addPipeline(this.form).then(response => {
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

<style scoped>
.kanban-board {
  min-height: 400px;
  padding-top: 10px;
}
.kanban-column {
  background: #f5f7fa;
  border-radius: 4px;
  min-height: 400px;
}
.kanban-header {
  padding: 12px 16px;
  border-radius: 4px 4px 0 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #fff;
}
.kanban-title {
  font-size: 14px;
  font-weight: bold;
}
.kanban-count {
  background: rgba(255,255,255,0.3);
  border: none;
  color: #fff;
}
.kanban-body {
  padding: 8px;
  min-height: 350px;
}
.kanban-card {
  background: #fff;
  border-radius: 4px;
  padding: 12px;
  margin-bottom: 8px;
  cursor: pointer;
  box-shadow: 0 1px 2px rgba(0,0,0,0.1);
  transition: box-shadow 0.2s;
}
.kanban-card:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}
.card-title {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 6px;
}
.card-amount {
  font-size: 16px;
  color: #F56C6C;
  font-weight: bold;
  margin-bottom: 6px;
}
.card-probability {
  margin-bottom: 6px;
}
.card-stage-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
