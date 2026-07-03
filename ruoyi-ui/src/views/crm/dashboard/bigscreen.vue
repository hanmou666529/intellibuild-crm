<template>
  <div class="bigscreen" v-loading="loading">
    <div class="bigscreen-inner">
      <div class="header">
        <div class="header-left">
          <span class="header-icon">◆</span>
          <h1 class="header-title">数据大屏</h1>
        </div>
        <div class="header-right">
          <span class="clock">{{ clock }}</span>
          <span class="refresh-badge" :class="{ refreshing }">
            {{ refreshing ? '刷新中...' : `上次更新 ${lastUpdated}` }}
          </span>
          <el-button size="small" plain @click="goBack">返回看板</el-button>
        </div>
      </div>

      <div class="stats-row">
        <div class="stat-card" v-for="card in statCards" :key="card.label">
          <div class="stat-accent" :style="{ background: card.color }"></div>
          <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </div>
      </div>

      <div class="charts-row">
        <div class="chart-wrapper">
          <div class="chart-header">
            <span class="chart-dot" style="background:#0052D9"></span>
            <span class="chart-title">客户来源</span>
          </div>
          <div id="bs_sourceChart" class="chart-body"></div>
        </div>
        <div class="chart-wrapper">
          <div class="chart-header">
            <span class="chart-dot" style="background:#FF6B35"></span>
            <span class="chart-title">订单状态</span>
          </div>
          <div id="bs_orderStatusChart" class="chart-body"></div>
        </div>
        <div class="chart-wrapper">
          <div class="chart-header">
            <span class="chart-dot" style="background:#7C3AED"></span>
            <span class="chart-title">合同概览</span>
          </div>
          <div id="bs_contractStatusChart" class="chart-body"></div>
        </div>
        <div class="chart-wrapper">
          <div class="chart-header">
            <span class="chart-dot" style="background:#0891B2"></span>
            <span class="chart-title">月度新增</span>
          </div>
          <div id="bs_monthNewChart" class="chart-body"></div>
        </div>
      </div>

      <div class="charts-row">
        <div class="chart-wrapper flex-2">
          <div class="chart-header">
            <span class="chart-dot" style="background:#00A86B"></span>
            <span class="chart-title">合同金额趋势</span>
          </div>
          <div id="bs_contractAmountChart" class="chart-body"></div>
        </div>
        <div class="chart-wrapper">
          <div class="chart-header">
            <span class="chart-dot" style="background:#D97706"></span>
            <span class="chart-title">产品销量排行</span>
          </div>
          <div id="bs_topProductChart" class="chart-body"></div>
        </div>
        <div class="chart-wrapper">
          <div class="chart-header">
            <span class="chart-dot" style="background:#DC2626"></span>
            <span class="chart-title">销售漏斗</span>
          </div>
          <div id="bs_funnelChart" class="chart-body"></div>
        </div>
      </div>

      <div class="charts-row">
        <div class="chart-wrapper flex-1">
          <div class="chart-header">
            <span class="chart-dot" style="background:#0052D9"></span>
            <span class="chart-title">跟进趋势 (14天)</span>
          </div>
          <div id="bs_trendChart" class="chart-body"></div>
        </div>
      </div>

      <div class="footer">
        <span>数据每30秒自动刷新 · 点击图表查看明细</span>
      </div>
    </div>

    <el-dialog :title="drillTitle" :visible.sync="drillVisible" width="800px" top="5vh">
      <el-table :data="drillData" v-loading="drillLoading" border stripe max-height="500" size="small">
        <el-table-column v-for="col in drillColumns" :key="col.prop" :prop="col.prop" :label="col.label" :min-width="col.width || 120" show-overflow-tooltip />
      </el-table>
      <span slot="footer">
        <el-button @click="drillVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getStats, drillDown } from "@/api/crm/dashboard"
import * as echarts from 'echarts'

const chartColors = ['#0052D9', '#00A86B', '#FF6B35', '#7C3AED', '#0891B2', '#D97706', '#DC2626', '#0D9488']
const CHARTS_CONFIG = [
  { id: 'bs_sourceChart', key: 'sourceChart', dataKey: 'sourceDistribution', type: 'pie' },
  { id: 'bs_orderStatusChart', key: 'orderStatus', dataKey: 'orderStatus', type: 'pie' },
  { id: 'bs_contractStatusChart', key: 'contractStatus', dataKey: 'contractStatus', type: 'pie' },
  { id: 'bs_monthNewChart', key: 'monthNew', dataKey: 'monthNewCustomers', type: 'bar' },
  { id: 'bs_contractAmountChart', key: 'contractAmount', dataKey: 'monthContractAmount', type: 'line' },
  { id: 'bs_topProductChart', key: 'topProduct', dataKey: 'topProducts', type: 'hbar' },
  { id: 'bs_funnelChart', key: 'funnel', dataKey: 'pipelineDistribution', type: 'funnel' },
  { id: 'bs_trendChart', key: 'trend', dataKey: 'followupTrend', type: 'trend' }
]

function fmtTime(d) {
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

export default {
  name: "CrmDashboardBigscreen",
  data() {
    return {
      loading: true,
      refreshing: false,
      clock: '',
      lastUpdated: '',
      stats: {},
      statCards: [],
      timer: null,
      clockTimer: null,
      drillVisible: false,
      drillLoading: false,
      drillTitle: '',
      drillData: [],
      drillColumns: [],
      charts: {}
    }
  },
  created() {
    this.fetchData()
  },
  mounted() {
    this.clockTimer = setInterval(() => { this.clock = fmtTime(new Date()) }, 1000)
    this.clock = fmtTime(new Date())
    this.timer = setInterval(() => this.fetchData(), 30000)
  },
  beforeDestroy() {
    clearInterval(this.timer)
    clearInterval(this.clockTimer)
    Object.values(this.charts).forEach(c => { if (c) c.dispose() })
  },
  methods: {
    fetchData() {
      this.refreshing = true
      getStats().then(res => {
        this.stats = res.data || {}
        this.statCards = [
          { value: this.stats.todayNewCustomer ?? 0, label: '今日新增客户', color: '#0052D9' },
          { value: this.stats.totalCustomer ?? 0, label: '总客户数', color: '#00A86B' },
          { value: '¥' + ((this.stats.monthDealAmount ?? 0).toLocaleString()), label: '本月成交金额', color: '#D97706' },
          { value: this.stats.pendingFollowup ?? 0, label: '待跟进数', color: '#E11D48' }
        ]
        this.lastUpdated = fmtTime(new Date()).slice(11)
        this.$nextTick(() => CHARTS_CONFIG.forEach(c => this.renderChart(c)))
      }).finally(() => { this.loading = false; this.refreshing = false })
    },
    renderChart(cfg) {
      const dom = document.getElementById(cfg.id)
      if (!dom) return
      if (this.charts[cfg.key]) this.charts[cfg.key].dispose()
      const chart = echarts.init(dom, null, { renderer: 'canvas' })
      this.charts[cfg.key] = chart
      chart.setOption(this.buildOption(cfg), true)
      chart.off('click')
      chart.on('click', params => this.onChartClick(cfg, params))
    },
    buildOption(cfg) {
      const data = this.stats[cfg.dataKey]
      const base = {
        tooltip: { trigger: 'axis' },
        grid: { left: '6%', right: '4%', bottom: '18%', containLabel: true }
      }
      if (cfg.type === 'pie') {
        const items = (data && data.length) ? data : [{ name: '暂无数据', value: 1, _empty: true }]
        return { ...base, tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' }, legend: { bottom: '0%' }, series: [{ type: 'pie', radius: ['35%', '65%'], center: ['50%', '42%'], data: items, label: { show: true, formatter: '{b}', fontSize: 11 }, emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.1)' } } }], color: chartColors }
      }
      if (cfg.type === 'bar') {
        const months = (data || []).map(d => d.month ? d.month.slice(5) : '')
        const values = (data || []).map(d => d.cnt || 0)
        return { ...base, xAxis: { type: 'category', data: months }, yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f0f0' } } }, series: [{ type: 'bar', barWidth: '40%', data: values, itemStyle: { color: '#0052D9', borderRadius: [4,4,0,0] } }] }
      }
      if (cfg.type === 'line') {
        const months = (data || []).map(d => d.month ? d.month.slice(5) : '')
        const values = (data || []).map(d => d.total || 0)
        return { ...base, tooltip: { trigger: 'axis', valueFormatter: v => '¥' + (v || 0).toLocaleString() }, xAxis: { type: 'category', data: months }, yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f0f0' } }, axisLabel: { formatter: '¥{value}' } }, series: [{ type: 'line', smooth: true, data: values, areaStyle: { color: 'rgba(0,168,107,0.1)' }, lineStyle: { color: '#00A86B', width: 2 }, itemStyle: { color: '#00A86B' } }] }
      }
      if (cfg.type === 'hbar') {
        const names = (data || []).map(d => d.name || '')
        const values = (data || []).map(d => d.value || 0)
        return { ...base, xAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f0f0' } } }, yAxis: { type: 'category', data: names.slice(0).reverse(), axisLabel: { fontSize: 10 } }, series: [{ type: 'bar', data: values.slice(0).reverse().map((v, i) => ({ value: v, itemStyle: { color: chartColors[i % chartColors.length], borderRadius: [0,4,4,0] } })) }] }
      }
      if (cfg.type === 'funnel') {
        const items = (data && data.length) ? data : [{ name:'线索',value:0 },{ name:'意向',value:0 },{ name:'报价',value:0 },{ name:'成交',value:0 },{ name:'回款',value:0 }]
        return { tooltip: { trigger: 'item' }, legend: { bottom: '0%' }, color: chartColors, series: [{ type: 'funnel', left: '10%', right: '10%', top: '5%', bottom: '18%', minSize: '0%', maxSize: '100%', data: items, label: { show: true, formatter: '{b}: {c}', fontSize: 11 } }] }
      }
      if (cfg.type === 'trend') {
        const raw = data || {}
        const dates = Array.isArray(raw.dates) ? raw.dates : []
        const counts = Array.isArray(raw.counts) ? raw.counts : []
        return { ...base, xAxis: { type: 'category', boundaryGap: false, data: dates }, yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f0f0' } } }, series: [{ type: 'line', smooth: true, data: counts, areaStyle: { color: 'rgba(0,82,217,0.08)' }, lineStyle: { color: '#0052D9', width: 2 }, itemStyle: { color: '#0052D9' } }] }
      }
      return {}
    },
    goBack() {
      this.$emit('close')
    },
    onChartClick(cfg, params) {
      const map = { sourceDistribution: 'customer_source', orderStatus: 'order_status', contractStatus: 'contract_status', topProducts: 'top_product' }
      const bizType = map[cfg.dataKey]
      if (!bizType) return
      const key = (params.data && params.data._raw) || params.name || ''
      this.drillTitle = `${bizType === 'top_product' ? '产品' : '分类'}: ${params.name || key}`
      this.drillVisible = true
      this.drillLoading = true
      this.drillData = []
      this.drillColumns = []
      drillDown(bizType, key).then(res => {
        const list = res.data || []
        if (!list.length) { this.drillData = [{ _empty: '暂无数据' }]; this.drillColumns = [{ prop: '_empty', label: '提示', width: 200 }]; return }
        const cols = Object.keys(list[0]).filter(k => !k.startsWith('del_') && !k.startsWith('params') && k !== 'itemList').slice(0, 8)
        this.drillColumns = cols.map(k => ({ prop: k, label: k.replace(/([A-Z])/g,' $1').replace(/^./,s=>s.toUpperCase()), width: /customer|contract|orderNo/.test(k) ? 140 : 100 }))
        this.drillData = list
      }).finally(() => { this.drillLoading = false })
    }
  }
}
</script>

<style scoped>
.bigscreen {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: #f5f7fa;
  color: #303133;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  overflow: hidden;
  z-index: 1000;
}
.bigscreen-inner {
  display: flex;
  flex-direction: column;
  height: 100vh;
  padding: 16px 24px 10px;
  box-sizing: border-box;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  flex-shrink: 0;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.header-icon {
  font-size: 20px;
  color: #0052D9;
}
.header-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  color: #303133;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
}
.clock {
  font-family: 'JetBrains Mono', 'Consolas', monospace;
  font-size: 15px;
  color: #606266;
  letter-spacing: 0.5px;
}
.refresh-badge {
  font-size: 12px;
  color: #909399;
}
.refresh-badge.refreshing {
  color: #0052D9;
}

.stats-row {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
  flex-shrink: 0;
}
.stat-card {
  flex: 1;
  position: relative;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 14px 16px;
  text-align: center;
  overflow: hidden;
}
.stat-accent {
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 2px;
  opacity: 0.5;
}
.stat-value {
  font-family: 'JetBrains Mono', 'Consolas', monospace;
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 2px;
}
.stat-label {
  font-size: 12px;
  color: #909399;
}

.charts-row {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
  flex: 1;
  min-height: 0;
}
.chart-wrapper {
  flex: 1;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 10px 12px 4px;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.chart-wrapper.flex-2 { flex: 2; }
.chart-wrapper.flex-1 { flex: 1; }
.chart-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
  flex-shrink: 0;
}
.chart-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.chart-title {
  font-size: 12px;
  font-weight: 600;
  color: #606266;
}
.chart-body {
  flex: 1;
  min-height: 0;
}

.footer {
  display: flex;
  justify-content: center;
  padding-top: 8px;
  border-top: 1px solid #ebeef5;
  flex-shrink: 0;
}
.footer span {
  font-size: 11px;
  color: #c0c4cc;
}
</style>
