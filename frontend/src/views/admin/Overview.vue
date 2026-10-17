<template>
  <div class="overview">
    <div class="stat-cards">
      <el-card class="stat-card">
        <div class="stat-icon">📚</div>
        <div class="stat-num">10000+</div>
        <div class="stat-label">内置试题</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-icon">🧑‍🎓</div>
        <div class="stat-num">{{ userTotal }}</div>
        <div class="stat-label">系统用户</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-icon">📝</div>
        <div class="stat-num">{{ examTotal }}</div>
        <div class="stat-label">考试场次</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-icon">🧩</div>
        <div class="stat-num">9</div>
        <div class="stat-label">题型种类</div>
      </el-card>
    </div>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card>
          <template #header><span class="card-title">📖 六大学科题库分布</span></template>
          <div ref="subjectChart" style="height: 340px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span class="card-title">📐 九大题型分布</span></template>
          <div ref="typeChart" style="height: 340px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getUsers } from '../../api/stats'
import { pageExams } from '../../api/exam'

const userTotal = ref(0)
const examTotal = ref(0)
const subjectChart = ref<HTMLElement>()
const typeChart = ref<HTMLElement>()

onMounted(async () => {
  try {
    const udata = await getUsers({ current: 1, size: 1 }) as any
    userTotal.value = udata.total
    const edata = await pageExams({ current: 1, size: 1 }) as any
    examTotal.value = edata.total
  } catch { /* ignore */ }

  await nextTick()

  // 学科分布
  const sChart = echarts.init(subjectChart.value!)
  sChart.setOption({
    tooltip: {},
    xAxis: { type: 'category', data: ['基础理论', '诊断学', '中药学', '方剂学', '针灸学', '内科学'] },
    yAxis: { type: 'value' },
    series: [{
      type: 'bar',
      data: [138, 248, 4750, 2541, 1702, 622],
      itemStyle: { color: '#9b3d2f' }
    }]
  })

  // 题型分布
  const tChart = echarts.init(typeChart.value!)
  tChart.setOption({
    tooltip: {},
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['35%', '65%'],
      data: [
        { value: 4576, name: '单选' },
        { value: 2883, name: '判断' },
        { value: 1440, name: '填空' },
        { value: 544, name: '多选' },
        { value: 320, name: '配伍' },
        { value: 104, name: '名解' },
        { value: 70, name: '简答' },
        { value: 20, name: '论述' },
        { value: 44, name: '病案' }
      ],
      itemStyle: { borderRadius: 4 }
    }]
  })
})
</script>

<style scoped>
.stat-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 16px; }
.stat-card { text-align: center; }
.stat-icon { font-size: 28px; }
.stat-num { font-size: 28px; font-weight: 700; color: #9b3d2f; margin: 6px 0 2px; }
.stat-label { color: #736b5e; font-size: 13px; }
.card-title { font-weight: 700; color: #9b3d2f; }
</style>
