<template>
  <div>
    <el-form label-width="100px">
      <el-form-item label="选择客户">
        <el-select v-model="customerId" filterable remote :remote-method="searchCustomer" placeholder="输入客户名称搜索" style="width: 100%">
          <el-option v-for="c in customers" :key="c.customerId" :label="c.customerName" :value="c.customerId" />
        </el-select>
      </el-form-item>
      <el-form-item label="沟通关键词">
        <el-input type="textarea" v-model="keywords" placeholder="输入沟通要点，如：讨论了Q3续约计划，对方对新功能很感兴趣，但价格还需要协商" :rows="3" />
      </el-form-item>
    </el-form>
    <div v-if="result" style="margin-top: 12px">
      <el-divider />
      <div class="result-card">
        <div class="result-section">
          <div class="section-icon">📝</div>
          <div class="section-body">{{ result }}</div>
        </div>
      </div>
      <div style="margin-top: 12px; text-align: right">
        <el-button size="small" @click="copyText">复制文本</el-button>
      </div>
    </div>
    <div slot="footer" class="dialog-footer" style="margin-top: 16px; text-align: right">
      <el-button @click="$emit('done')">取消</el-button>
      <el-button type="primary" :loading="loading" @click="submit">生成记录</el-button>
    </div>
  </div>
</template>

<script>
import { followupGenerate } from '@/api/crm/ai'
import { listCustomer } from '@/api/crm/customer'

export default {
  data() {
    return {
      customerId: null, keywords: '', result: '', loading: false, customers: []
    }
  },
  methods: {
    searchCustomer(query) {
      if (!query) return
      listCustomer({ customerName: query, pageSize: 20 }).then(r => { this.customers = r.rows || [] })
    },
    submit() {
      if (!this.customerId) { this.$message.warning('请选择客户'); return }
      this.loading = true; this.result = ''
      followupGenerate({ customerId: this.customerId, keywords: this.keywords })
        .then(r => { this.result = r.data?.content || JSON.stringify(r.data) })
        .finally(() => { this.loading = false })
    },
    copyText() {
      const ta = document.createElement('textarea')
      ta.value = this.result
      document.body.appendChild(ta); ta.select(); document.execCommand('copy')
      document.body.removeChild(ta)
      this.$message.success('已复制')
    }
  }
}
</script>
