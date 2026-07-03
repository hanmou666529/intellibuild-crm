<template>
  <div class="ai-workbench">
    <el-row :gutter="16">
      <el-col :span="6" v-for="card in cards" :key="card.key">
        <el-card :body-style="{ padding: '20px' }" shadow="hover" class="ai-card" @click.native="openDialog(card.key)">
          <div class="card-icon" :style="{ background: card.color + '15' }">
            <i :class="card.icon" :style="{ color: card.color, fontSize: '28px' }"></i>
          </div>
          <div class="card-title">{{ card.title }}</div>
          <div class="card-desc">{{ card.desc }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog :title="(currentCard || {}).title" :visible.sync="dialogVisible" width="700px" :close-on-click-modal="false">
      <component :is="currentComponent" ref="compRef" @done="handleDone" />
    </el-dialog>
  </div>
</template>

<script>
import CustomerIntel from './dialogs/CustomerIntel'
import FollowupGenerate from './dialogs/FollowupGenerate'
import LeadScoring from './dialogs/LeadScoring'
import ReportGenerate from './dialogs/ReportGenerate'
import ContractReview from './dialogs/ContractReview'

export default {
  name: 'AiWorkbench',
  components: { CustomerIntel, FollowupGenerate, LeadScoring, ReportGenerate, ContractReview },
  data() {
    return {
      dialogVisible: false,
      currentKey: null,
      cards: [
        { key: 'intel', title: '客户资料补全', icon: 'el-icon-document-copy', color: '#409EFF', desc: 'AI 分析已知信息，智能补充客户画像字段' },
        { key: 'followup', title: '跟进记录生成', icon: 'el-icon-edit-outline', color: '#67C23A', desc: '根据沟通关键词自动生成专业跟进记录' },
        { key: 'scoring', title: '商机评分', icon: 'el-icon-star-on', color: '#E6A23C', desc: 'AI 评估所有客户成交潜力并排序' },
        { key: 'report', title: '日报/周报生成', icon: 'el-icon-document', color: '#909399', desc: '自动总结工作内容生成汇报文档' },
        { key: 'contract', title: '合同审查', icon: 'el-icon-view', color: '#F56C6C', desc: '智能识别合同条款风险与漏洞' }
      ]
    }
  },
  computed: {
    currentCard() {
      return this.cards.find(c => c.key === this.currentKey)
    },
    currentComponent() {
      const map = { intel: 'CustomerIntel', followup: 'FollowupGenerate', scoring: 'LeadScoring', report: 'ReportGenerate', contract: 'ContractReview' }
      return map[this.currentKey]
    }
  },
  methods: {
    openDialog(key) {
      this.currentKey = key
      this.dialogVisible = true
    },
    handleDone() {
      this.dialogVisible = false
      this.$message.success('操作完成')
    }
  }
}
</script>

<style>
.result-box { background: #f5f7fa; border-radius: 8px; padding: 16px; }
.result-section { display: flex; gap: 12px; padding: 8px 0; border-bottom: 1px solid #e8eaed; }
.result-section:last-child { border-bottom: none; }
.result-field-label { width: 100px; flex-shrink: 0; font-weight: 600; font-size: 13px; color: #606266; }
.result-field-value { flex: 1; font-size: 13px; color: #303133; white-space: pre-wrap; }
.result-card { background: #f0f9eb; border: 1px solid #e1f3d8; border-radius: 8px; padding: 16px; }
.result-card .result-section { border-bottom-color: #e1f3d8; }
.section-icon { font-size: 18px; line-height: 1.5; }
.section-body { flex: 1; white-space: pre-wrap; line-height: 1.6; color: #303133; }
</style>
