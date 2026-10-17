<template>
  <div class="stats-page">
    <el-card class="filter-card">
      <div class="filters">
        <el-select v-model="examId" placeholder="选择考试" clearable style="width:220px" @change="loadClass">
          <el-option v-for="e in exams" :key="e.id" :label="e.name" :value="e.id" />
        </el-select>
        <el-select v-model="clazzId" placeholder="选择班级" clearable style="width:180px" @change="loadClass">
          <el-option v-for="c in clazzes" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </div>
    </el-card>

    <div v-if="classStats" class="stat-cards">
      <el-card class="stat-card">
        <div class="stat-num">{{ classStats.studentCount }}</div>
        <div class="stat-label">参考人数</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-num">{{ classStats.avgScore }}</div>
        <div class="stat-label">平均分</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-num">{{ classStats.maxScore }}</div>
        <div class="stat-label">最高分</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-num">{{ (classStats.passRate * 100).toFixed(1) }}%</div>
        <div class="stat-label">通过率</div>
      </el-card>
    </div>

    <el-row :gutter="16" v-if="classStats">
      <el-col :span="12">
        <el-card>
          <template #header><span class="card-title">📊 成绩分布</span></template>
          <div ref="scoreChart" style="height: 320px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span class="card-title">🎯 知识点掌握度雷达图</span></template>
          <div ref="radarChart" style="height: 320px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card v-if="classStats" style="margin-top:16px">
      <template #header><span class="card-title">🏆 班级成绩排行</span></template>
      <el-table :data="classStats.allScores" stripe size="small">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="studentName" label="姓名" width="120" />
        <el-table-column prop="clazzName" label="班级" width="160" />
        <el-table-column prop="totalScore" label="得分" width="100" />
        <el-table-column label="及格" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isPass ? 'success' : 'danger'" size="small">{{ row.isPass ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getClassStats, getKnowledgeMastery } from '../../api/stats'
import { pageExams } from '../../api/exam'
import { getClazzes } from '../../api/stats'

const exams = ref<any[]>([])
const clazzes = ref<any[]>([])
const examId = ref<number | undefined>()
const clazzId = ref<number | undefined>()
const classStats = ref<any>(null)
const scoreChart = ref<HTMLElement>()
const radarChart = ref<HTMLElement>()

async function loadClass() {
  if (!examId.value && !clazzId.value) return
  classStats.value = await getClassStats(examId.value, clazzId.value) as any
  await nextTick()
  renderScoreChart()
  renderRadar()
}

function renderScoreChart() {
  if (!classStats.value || !scoreChart.value) return
  const chart = echarts.init(scoreChart.value)
  const scores = classStats.value.allScores.map((s: any) => Number(s.totalScore))
  const bins = [0, 60, 70, 80, 90, 100]
  const labels = ['<60', '60-70', '70-80', '80-90', '90-100']
  const data = bins.map((b, i) => {
    if (i === bins.length - 1) return scores.filter(s => s >= 90).length
    return scores.filter(s => s >= b && s < bins[i + 1]).length
  })
  chart.setOption({
    tooltip: {},
    xAxis: { type: 'category', data: labels },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      type: 'bar',
      data,
      itemStyle: { color: '#9b3d2f' },
      barWidth: '50%'
    }]
  })
}

async function renderRadar() {
  if (!examId.value || !radarChart.value) return
  const mastery = await getKnowledgeMastery(examId.value, clazzId.value) as any
  if (!mastery.length) return
  const chart = echarts.init(radarChart.value)
  const names = mastery.slice(0, 8).map((m: any) => String(m.knowledgePointName).slice(0, 6))
  const values = mastery.slice(0, 8).map((m: any) => Math.round(Number(m.correctRate) * 100))
  chart.setOption({
    tooltip: {},
    radar: {
      indicator: names.map(n => ({ name: n, max: 100 }))
    },
    series: [{
      type: 'radar',
      data: [{ value: values, name: '掌握度', areaStyle: { color: 'rgba(74,124,89,.2)' } }],
      lineStyle: { color: '#4a7c59' },
      itemStyle: { color: '#4a7c59' }
    }]
  })
}

onMounted(async () => {
  const [edata, cdata] = await Promise.all([
    pageExams({ current: 1, size: 50 }),
    getClazzes()
  ]) as any
  exams.value = edata.records
  clazzes.value = cdata
})
</script>

<style scoped>
.filters { display: flex; gap: 12px; }
.stat-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 16px; }
.stat-card { text-align: center; }
.stat-num { font-size: 32px; font-weight: 700; color: #9b3d2f; }
.stat-label { color: #736b5e; font-size: 13px; margin-top: 4px; }
.card-title { font-weight: 700; color: #9b3d2f; }
</style>
