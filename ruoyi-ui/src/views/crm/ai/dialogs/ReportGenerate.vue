<template>
  <div>
    <el-form label-width="80px">
      <el-form-item label="报告类型">
        <el-radio-group v-model="type">
          <el-radio label="daily">日报</el-radio>
          <el-radio label="weekly">周报</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="日期">
        <el-date-picker v-model="date" type="date" placeholder="选择日期" value-format="yyyy-MM-dd" />
      </el-form-item>
    </el-form>
    <div v-if="result" style="margin-top: 12px">
      <el-divider />
      <div style="white-space: pre-wrap; background: #f5f7fa; padding: 12px; border-radius: 4px;">{{ result }}</div>
      <div style="margin-top: 12px; text-align: right">
        <el-button type="success" size="small" @click="copyText">复制内容</el-button>
      </div>
    </div>
    <div slot="footer" class="dialog-footer" style="margin-top: 16px; text-align: right">
      <el-button @click="$emit('done')">取消</el-button>
      <el-button type="primary" :loading="loading" @click="submit">生成报告</el-button>
    </div>
  </div>
</template>

<script>
import { reportGenerate } from '@/api/crm/ai'

export default {
  data() {
    return {
      type: 'daily',
      date: '',
      result: '',
      loading: false
    }
  },
  methods: {
    submit() {
      this.loading = true
      reportGenerate({ type: this.type, date: this.date || undefined })
        .then(r => { this.result = r.data?.content || JSON.stringify(r.data) })
        .finally(() => { this.loading = false })
    },
    copyText() {
      if (this.result) {
        const ta = document.createElement('textarea')
        ta.value = this.result
        document.body.appendChild(ta)
        ta.select()
        document.execCommand('copy')
        document.body.removeChild(ta)
        this.$message.success('已复制')
      }
    }
  }
}
</script>
