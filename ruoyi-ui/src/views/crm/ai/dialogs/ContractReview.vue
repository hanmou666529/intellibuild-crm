<template>
  <div>
    <el-form label-width="100px">
      <el-form-item label="选择合同">
        <el-select v-model="contractId" filterable remote :remote-method="searchContract" placeholder="输入合同编号/名称搜索" style="width: 100%">
          <el-option v-for="c in contracts" :key="c.contractId" :label="c.contractNo + ' - ' + c.contractName" :value="c.contractId" />
        </el-select>
      </el-form-item>
    </el-form>
    <div v-if="resultList.length > 0" style="margin-top: 12px">
      <el-divider />
      <el-table :data="resultList" border stripe size="small">
        <el-table-column label="风险等级" width="100">
          <template slot-scope="{ row }">
            <el-tag :type="levelType(row.level)" size="small" effect="dark">{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="risk" label="风险描述" min-width="200" />
        <el-table-column prop="suggestion" label="建议措施" min-width="200" />
      </el-table>
    </div>
    <div slot="footer" class="dialog-footer" style="margin-top: 16px; text-align: right">
      <el-button @click="$emit('done')">取消</el-button>
      <el-button type="primary" :loading="loading" @click="submit">开始审查</el-button>
    </div>
  </div>
</template>

<script>
import { contractReview } from '@/api/crm/ai'
import { listContract } from '@/api/crm/contract'

export default {
  data() {
    return {
      contractId: null, resultList: [], loading: false, contracts: []
    }
  },
  methods: {
    searchContract(query) {
      if (!query) return
      listContract({ contractName: query, pageSize: 20 }).then(r => { this.contracts = r.rows || [] })
    },
    submit() {
      if (!this.contractId) { this.$message.warning('请选择合同'); return }
      this.loading = true
      contractReview({ contractId: this.contractId })
        .then(r => { this.resultList = r.data || [] })
        .finally(() => { this.loading = false })
    },
    levelType(level) {
      if (level === '高' || level === 'high') return 'danger'
      if (level === '中' || level === 'medium') return 'warning'
      return 'success'
    }
  }
}
</script>
