<template>
  <div>
  <div class="dash-container mod-dashboard page-accent">
    <div class="dash-inner">
      <div class="dash-header anim-title delay-1">
        <div>
          <h2 class="dash-greeting">数据看板</h2>
          <p class="dash-desc">实时业务数据总览</p>
        </div>
        <div class="dash-tags">
          <span class="pill pill-light" style="background: var(--accent-light); color: var(--accent);">Overview</span>
          <span class="pill pill-light">Real-time</span>
          <el-button type="primary" size="mini" icon="el-icon-full-screen" @click="showBigscreen = true" style="margin-left:8px">大屏</el-button>
        </div>
      </div>

      <el-row :gutter="16" class="mb8">
        <el-col :xs="12" :sm="6" v-for="(card, i) in statCards" :key="i">
          <div class="stat-card anim-content" :style="{animationDelay: `${0.3 + i * 0.1}s`, '--card-accent': card.color}">
            <div class="stat-accent-bar" :style="{background: card.color}"></div>
            <div class="stat-value" :style="{color: card.color}">{{ card.value }}</div>
            <div class="stat-label">{{ card.label }}</div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="16" class="mb8">
        <el-col :xs="24" :sm="12">
          <div class="chart-card anim-content delay-3">
            <div class="chart-title">客户来源分布</div>
            <div id="sourceChart" style="width: 100%; height: 280px"></div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12">
          <div class="chart-card anim-content delay-3">
            <div class="chart-title">订单状态分布</div>
            <div id="orderStatusChart" style="width: 100%; height: 280px"></div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="16" class="mb8">
        <el-col :xs="24" :sm="12">
          <div class="chart-card anim-content delay-4">
            <div class="chart-title">月度新增趋势</div>
            <div id="monthNewChart" style="width: 100%; height: 280px"></div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12">
          <div class="chart-card anim-content delay-4">
            <div class="chart-title">合同金额趋势</div>
            <div id="contractAmountChart" style="width: 100%; height: 280px"></div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="16" class="mb8">
        <el-col :xs="24" :sm="12">
          <div class="chart-card anim-content delay-4">
            <div class="chart-title">产品销量排行</div>
            <div id="topProductChart" style="width: 100%; height: 280px"></div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12">
          <div class="chart-card anim-content delay-4">
            <div class="chart-title">合同概览</div>
            <div id="contractStatusChart" style="width: 100%; height: 280px"></div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :xs="24" :sm="12">
          <div class="chart-card anim-content delay-5">
            <div class="chart-title">销售漏斗</div>
            <div id="funnelChart" style="width: 100%; height: 280px"></div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12">
          <div class="chart-card anim-content delay-5">
            <div class="chart-title">跟进趋势</div>
            <div id="trendChart" style="width: 100%; height: 280px"></div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
  <bigscreen v-if="showBigscreen" @close="showBigscreen = false" />
  </div>
</template>

<script>
import { getStats } from "@/api/crm/dashboard"
import * as echarts from 'echarts'
import Bigscreen from './bigscreen'

const chartColors = ['#0052D9', '#00A86B', '#FF6B35', '#7C3AED', '#0891B2', '#D97706', '#DC2626', '#0D9488']

export default {
  name: "CrmDashboard",
  components: { Bigscreen },
  data() {
    return {
      showBigscreen: false,
      stats: {},
      statCards: [
        { value: '--', label: '今日新增客户', color: '#0052D9' },
        { value: '--', label: '总客户数', color: '#00A86B' },
        { value: '--', label: '本月成交金额', color: '#D97706' },
        { value: '--', label: '待跟进数', color: '#E11D48' }
      ],
      sourceChartInstance: null,
      orderStatusChartInstance: null,
      monthNewChartInstance: null,
      contractAmountChartInstance: null,
      topProductChartInstance: null,
      contractStatusChartInstance: null,
      funnelChartInstance: null,
      trendChartInstance: null
    }
  },
  created() {
    this.getStats()
  },
  watch: {
    showBigscreen(val) {
      if (!val) {
        this.getStats()
      }
    }
  },
  beforeDestroy() {
    this._resizeHandlers && this._resizeHandlers.forEach(fn => window.removeEventListener('resize', fn))
    this.allInstances().forEach(c => { if (c) c.dispose() })
  },
  methods: {
    allInstances() {
      return [
        this.sourceChartInstance, this.orderStatusChartInstance,
        this.monthNewChartInstance, this.contractAmountChartInstance,
        this.topProductChartInstance, this.contractStatusChartInstance,
        this.funnelChartInstance, this.trendChartInstance
      ]
    },
    getStats() {
      getStats().then(response => {
        this.stats = response.data || {}
        this.statCards = [
          { value: this.stats.todayNewCustomer || 0, label: '今日新增客户', color: '#0052D9' },
          { value: this.stats.totalCustomer || 0, label: '总客户数', color: '#00A86B' },
          { value: '¥' + (this.stats.monthDealAmount || 0), label: '本月成交金额', color: '#D97706' },
          { value: this.stats.pendingFollowup || 0, label: '待跟进数', color: '#E11D48' }
        ]
        this.$nextTick(() => { this.initCharts() })
      })
    },
    initCharts() {
      this.initPie('sourceChart', 'sourceChartInstance', this.stats.sourceDistribution || [])
      this.initPie('orderStatusChart', 'orderStatusChartInstance', this.stats.orderStatus || [])
      this.initPie('contractStatusChart', 'contractStatusChartInstance', this.stats.contractStatus || [])
      this.initBar('monthNewChart', 'monthNewChartInstance', this.stats.monthNewCustomers || [])
      this.initLine('contractAmountChart', 'contractAmountChartInstance', this.stats.monthContractAmount || [])
      this.initHorizontalBar('topProductChart', 'topProductChartInstance', this.stats.topProducts || [])
      this.initFunnel()
      this.initTrend()
    },
    initPie(domId, instKey, data) {
      const dom = document.getElementById(domId)
      if (!dom) return
      if (this[instKey]) this[instKey].dispose()
      const chart = echarts.init(dom, null, { renderer: 'canvas' })
      this[instKey] = chart
      chart.setOption({
        color: chartColors,
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: { bottom: '0%', textStyle: { fontSize: 11 } },
        series: [{
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['50%', '45%'],
          data: data.length ? data : [{ name: '暂无数据', value: 1 }],
          label: { show: true, formatter: '{b}: {d}%', fontSize: 11 },
          emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.2)' } },
          animationDuration: 800,
          animationEasing: 'cubicOut'
        }]
      })
      const handler = () => chart.resize()
      window.addEventListener('resize', handler)
      if (!this._resizeHandlers) this._resizeHandlers = []
      this._resizeHandlers.push(handler)
    },
    initBar(domId, instKey, data) {
      const dom = document.getElementById(domId)
      if (!dom) return
      if (this[instKey]) this[instKey].dispose()
      const chart = echarts.init(dom, null, { renderer: 'canvas' })
      this[instKey] = chart
      const months = data.map(d => d.month ? d.month.slice(5) : '')
      const values = data.map(d => d.cnt || 0)
      chart.setOption({
        color: chartColors,
        tooltip: { trigger: 'axis' },
        grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
        xAxis: { type: 'category', data: months.length ? months : [], axisLabel: { fontSize: 11 } },
        yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f0f0' } } },
        series: [{
          type: 'bar', barWidth: '40%',
          data: values,
          itemStyle: { color: '#0052D9', borderRadius: [4, 4, 0, 0] },
          animationDuration: 600
        }]
      })
      const handler = () => chart.resize()
      window.addEventListener('resize', handler)
      if (!this._resizeHandlers) this._resizeHandlers = []
      this._resizeHandlers.push(handler)
    },
    initLine(domId, instKey, data) {
      const dom = document.getElementById(domId)
      if (!dom) return
      if (this[instKey]) this[instKey].dispose()
      const chart = echarts.init(dom, null, { renderer: 'canvas' })
      this[instKey] = chart
      const months = data.map(d => d.month ? d.month.slice(5) : '')
      const values = data.map(d => d.total || 0)
      chart.setOption({
        color: chartColors,
        tooltip: { trigger: 'axis', valueFormatter: v => '¥' + v.toLocaleString() },
        grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
        xAxis: { type: 'category', data: months.length ? months : [], axisLabel: { fontSize: 11 } },
        yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f0f0' } }, axisLabel: { formatter: '¥{value}' } },
        series: [{
          type: 'line', smooth: true,
          data: values,
          areaStyle: { color: 'rgba(0,168,107,0.08)' },
          lineStyle: { color: '#00A86B', width: 2 },
          itemStyle: { color: '#00A86B' },
          animationDuration: 600
        }]
      })
      const handler = () => chart.resize()
      window.addEventListener('resize', handler)
      if (!this._resizeHandlers) this._resizeHandlers = []
      this._resizeHandlers.push(handler)
    },
    initHorizontalBar(domId, instKey, data) {
      const dom = document.getElementById(domId)
      if (!dom) return
      if (this[instKey]) this[instKey].dispose()
      const chart = echarts.init(dom, null, { renderer: 'canvas' })
      this[instKey] = chart
      const names = data.map(d => d.name || '')
      const values = data.map(d => d.value || 0)
      chart.setOption({
        color: chartColors,
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true },
        xAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f0f0' } } },
        yAxis: { type: 'category', data: names.length ? names.reverse() : [], axisLabel: { fontSize: 11 } },
        series: [{
          type: 'bar',
          data: values.length ? values.reverse().map((v, i) => ({
            value: v,
            itemStyle: { color: chartColors[i % chartColors.length], borderRadius: [0, 4, 4, 0] }
          })) : [],
          animationDuration: 600
        }]
      })
      const handler = () => chart.resize()
      window.addEventListener('resize', handler)
      if (!this._resizeHandlers) this._resizeHandlers = []
      this._resizeHandlers.push(handler)
    },
    initFunnel() {
      const dom = document.getElementById('funnelChart')
      if (!dom) return
      if (this.funnelChartInstance) this.funnelChartInstance.dispose()
      this.funnelChartInstance = echarts.init(dom)
      const data = this.stats.pipelineDistribution || []
      this.funnelChartInstance.setOption({
        color: chartColors,
        tooltip: { trigger: 'item' },
        legend: { bottom: '0%', textStyle: { fontSize: 11 } },
        series: [{
          type: 'funnel',
          left: '10%', right: '10%', top: '10%', bottom: '15%',
          minSize: '0%', maxSize: '100%',
          data: data.length ? data : [
            { name: '线索', value: 0 }, { name: '意向', value: 0 },
            { name: '报价', value: 0 }, { name: '成交', value: 0 }, { name: '回款', value: 0 }
          ],
          label: { show: true, formatter: '{b}: {c}', fontSize: 11 },
          emphasis: { lineStyle: { width: 4 } },
          animationDuration: 1000,
          animationEasing: 'elasticOut'
        }]
      })
      const handler = () => this.funnelChartInstance && this.funnelChartInstance.resize()
      window.addEventListener('resize', handler)
      if (!this._resizeHandlers) this._resizeHandlers = []
      this._resizeHandlers.push(handler)
    },
    initTrend() {
      const dom = document.getElementById('trendChart')
      if (!dom) return
      if (this.trendChartInstance) this.trendChartInstance.dispose()
      this.trendChartInstance = echarts.init(dom)
      const raw = this.stats.followupTrend
      const dates = raw && Array.isArray(raw.dates) ? raw.dates : []
      const counts = raw && Array.isArray(raw.counts) ? raw.counts : []
      this.trendChartInstance.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
        xAxis: {
          type: 'category', boundaryGap: false,
          axisLine: { lineStyle: { color: '#ddd' } },
          axisLabel: { fontSize: 11 },
          data: dates.length ? dates : []
        },
        yAxis: {
          type: 'value',
          splitLine: { lineStyle: { color: '#f0f0f0' } }
        },
        series: [{
          name: '跟进次数', type: 'line', smooth: true,
          data: counts.length ? counts : [],
          areaStyle: { color: 'rgba(0,82,217,0.08)' },
          lineStyle: { color: '#0052D9', width: 2 },
          itemStyle: { color: '#0052D9' },
          animationDuration: 600
        }]
      })
      const handler = () => this.trendChartInstance && this.trendChartInstance.resize()
      window.addEventListener('resize', handler)
      if (!this._resizeHandlers) this._resizeHandlers = []
      this._resizeHandlers.push(handler)
    }
  }
}
</script>

<style scoped>
.dash-container {
  background: #fff;
  margin: -20px;
  padding: 24px;
  min-height: calc(100vh - 86px);
}
.dash-inner {
  max-width: 1400px;
  margin: 0 auto;
}

.dash-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding-top: 8px;
  margin-bottom: 28px;
}
.dash-greeting {
  font-size: 22px;
  font-weight: 500;
  margin: 0;
  color: #000;
}
.dash-desc {
  font-size: 13px;
  color: rgba(0,0,0,0.4);
  margin: 4px 0 0 0;
}
.dash-tags {
  display: flex;
  gap: 6px;
}
@media (max-width: 767px) {
  .dash-tags { display: none; }
}

.stat-card {
  position: relative;
  background: #fff;
  border: 1px solid rgba(0,0,0,0.08);
  border-radius: 12px;
  padding: 20px 16px;
  text-align: center;
  overflow: hidden;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
}
.stat-card:hover {
  border-color: var(--card-accent, #000);
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
  transform: translateY(-2px);
}
.stat-accent-bar {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  border-radius: 12px 12px 0 0;
  opacity: 0.7;
}
.stat-value {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 4px;
  transition: color 0.2s;
}
.stat-label {
  font-size: 12px;
  color: rgba(0,0,0,0.45);
  font-weight: 400;
}

.chart-card {
  background: #fff;
  border: 1px solid rgba(0,0,0,0.08);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  transition: all 0.25s ease;
}
.chart-card:hover {
  border-color: var(--accent, rgba(0,0,0,0.2));
  box-shadow: 0 4px 20px rgba(0,0,0,0.04);
}
.chart-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--accent, rgba(0,0,0,0.55));
  margin-bottom: 12px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding-left: 10px;
  border-left: 2px solid var(--accent, #000);
}
</style>
