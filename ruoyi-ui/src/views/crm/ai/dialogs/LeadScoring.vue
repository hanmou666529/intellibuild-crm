<template>
  <div>
    <el-alert title="AI 将基于所有客户的等级、来源、跟进状态等维度进行评分，评估成交潜力" type="info" show-icon :closable="false" />
    <div style="text-align: center; margin-top: 24px">
      <el-button type="primary" :loading="loading" size="large" @click="run" icon="el-icon-star-on">开始评分</el-button>
    </div>
    <el-divider v-if="result" />
    <el-alert v-if="result" :title="result" type="success" show-icon />
  </div>
</template>

<script>
import { runScoring } from '@/api/crm/ai'

export default {
  data() {
    return { loading: false, result: '' }
  },
  methods: {
    run() {
      this.loading = true
      this.result = ''
      runScoring().then(r => {
        this.result = r.msg || '评分完成'
      }).finally(() => { this.loading = false })
    }
  }
}
</script>
