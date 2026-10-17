<template>
  <el-card>
    <template #header><span class="card-title">📊 成绩查询</span></template>
    <el-table :data="scores" v-loading="loading" stripe>
      <el-table-column prop="examName" label="考试名称" min-width="160" />
      <el-table-column prop="totalScore" label="得分" width="100">
        <template #default="{ row }">
          <span :class="{ pass: row.isPass, fail: !row.isPass }">{{ row.totalScore }}</span>
        </template>
      </el-table-column>
      <el-table-column label="是否及格" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isPass ? 'success' : 'danger'" size="small">{{ row.isPass ? '及格' : '不及格' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="rankNo" label="排名" width="90" />
      <el-table-column label="考试时间" width="170">
        <template #default="{ row }">{{ fmt(row.createdAt) }}</template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && scores.length === 0" description="暂无成绩记录，快去参加考试吧" />
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getMyScores } from '../../api/stats'

const scores = ref<any[]>([])
const loading = ref(false)
function fmt(s?: string) { return s ? s.replace('T', ' ').slice(0, 16) : '-' }

onMounted(async () => {
  loading.value = true
  try { scores.value = await getMyScores() as any } finally { loading.value = false }
})
</script>

<style scoped>
.card-title { font-weight: 700; color: #9b3d2f; }
.pass { color: #4a7c59; font-weight: 700; }
.fail { color: #9b3d2f; font-weight: 700; }
</style>
