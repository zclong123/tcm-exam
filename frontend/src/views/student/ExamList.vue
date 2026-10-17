<template>
  <div>
    <el-card>
      <template #header><span class="card-title">📝 我的考试</span></template>
      <el-table :data="exams" v-loading="loading" stripe>
        <el-table-column prop="name" label="考试名称" min-width="180" />
        <el-table-column label="开始时间" width="170">
          <template #default="{ row }">{{ fmt(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="结束时间" width="170">
          <template #default="{ row }">{{ fmt(row.endTime) }}</template>
        </el-table-column>
        <el-table-column prop="durationMin" label="时长(分钟)" width="110" />
        <el-table-column prop="passScore" label="及格线" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row)" size="small">{{ statusText(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" size="small" :disabled="!canStart(row)" @click="go(row)">
              {{ canStart(row) ? '开始考试' : '不可参加' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && exams.length === 0" description="暂无可参加的考试" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyExams } from '../../api/exam'
import type { Exam } from '../../types'

const router = useRouter()
const exams = ref<Exam[]>([])
const loading = ref(false)

function fmt(s?: string) {
  if (!s) return '-'
  return s.replace('T', ' ').slice(0, 16)
}
function canStart(e: Exam) {
  const now = Date.now()
  const start = new Date(e.startTime).getTime()
  const end = new Date(e.endTime).getTime()
  return now >= start && now <= end
}
function statusType(e: Exam) {
  const now = Date.now()
  if (now < new Date(e.startTime).getTime()) return 'info'
  if (now > new Date(e.endTime).getTime()) return 'warning'
  return 'success'
}
function statusText(e: Exam) {
  const now = Date.now()
  if (now < new Date(e.startTime).getTime()) return '未开始'
  if (now > new Date(e.endTime).getTime()) return '已结束'
  return '进行中'
}
function go(e: Exam) {
  router.push(`/student/exam/${e.id}`)
}

onMounted(async () => {
  loading.value = true
  try {
    exams.value = await getMyExams() as any
  } catch { /* handled */ } finally {
    loading.value = false
  }
})
</script>
