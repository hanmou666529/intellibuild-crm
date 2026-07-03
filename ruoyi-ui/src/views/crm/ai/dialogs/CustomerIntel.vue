<template>
  <div>
    <el-form label-width="100px">
      <el-form-item label="选择客户">
        <el-select v-model="customerId" filterable remote :remote-method="searchCustomer" placeholder="输入客户名称搜索" style="width: 100%">
          <el-option v-for="c in customers" :key="c.customerId" :label="c.customerName" :value="c.customerId" />
        </el-select>
      </el-form-item>
      <el-form-item label="额外信息">
        <el-input type="textarea" v-model="extraInfo" placeholder="补充你知道的客户信息，如：最近了解到他们拿到了B轮融资，主营跨境电商" :rows="3" />
      </el-form-item>
    </el-form>
    <div v-if="result">
      <el-divider />
      <div class="result-box">
        <div class="result-section" v-for="(block, i) in parsedBlocks" :key="i">
          <div class="result-field-label">{{ block.label }}</div>
          <div class="result-field-value">{{ block.value }}</div>
        </div>
      </div>
    </div>
    <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" style="margin-top:12px" />
    <div slot="footer" class="dialog-footer" style="margin-top: 16px; text-align: right">
      <el-button @click="$emit('done')">取消</el-button>
      <el-button type="primary" :loading="loading" @click="submit">开始分析</el-button>
    </div>
  </div>
</template>

<script>
import { customerIntel } from '@/api/crm/ai'
import { listCustomer } from '@/api/crm/customer'

export default {
  data() {
    return {
      customerId: null, extraInfo: '', result: '', error: '', loading: false, customers: []
    }
  },
  computed: {
    parsedBlocks() {
      if (!this.result) return []
      try {
        const obj = JSON.parse(this.result)
        return Object.entries(obj).map(([k, v]) => ({ label: k, value: String(v) }))
      } catch { return [{ label: '分析结果', value: this.result }] }
    }
  },
  methods: {
    searchCustomer(query) {
      if (!query) return
      listCustomer({ customerName: query, pageSize: 20 }).then(r => { this.customers = r.rows || [] })
    },
    submit() {
      if (!this.customerId) { this.$message.warning('请选择客户'); return }
      this.loading = true; this.error = ''; this.result = ''
      customerIntel({ customerId: this.customerId, knownFields: this.extraInfo ? { note: this.extraInfo } : {} })
        .then(r => { this.result = r.data?.content || JSON.stringify(r.data) })
        .catch(e => { this.error = e.msg || e.message || '请求失败' })
        .finally(() => { this.loading = false })
    }
  }
}
</script>
