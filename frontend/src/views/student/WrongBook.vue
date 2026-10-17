<template>
  <el-card>
    <template #header>
      <div class="card-head">
        <span class="card-title">📕 错题本</span>
        <div class="filters">
          <el-checkbox v-model="showMastered" @change="load">显示已掌握</el-checkbox>
        </div>
      </div>
    </template>

    <el-table :data="questions" v-loading="loading" stripe>
      <el-table-column label="题型" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="typeTag(row.qtype)">{{ typeText(row.qtype) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="stem" label="题目" min-width="320">
        <template #default="{ row }">
          <span class="stem-preview">{{ row.stem }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="knowledgePointName" label="知识点" width="140" />
      <el-table-column label="难度" width="80">
        <template #default="{ row }">{{ '★'.repeat(row.difficulty || 1) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="showDetail(row)">查看解析</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && questions.length === 0" description="错题本空空如也，太棒了！" />

    <el-pagination
      v-if="total > pageSize"
      v-model:current-page="current"
      :page-size="pageSize"
      :total="total"
      layout="prev, pager, next"
      @current-change="load"
      style="margin-top:16px; justify-content: flex-end"
    />

    <el-drawer v-model="detailDrawer" :title="'题目解析'" size="50%">
      <div v-if="currentQ" class="detail-content">
        <div class="q-stem">{{ currentQ.stem }}</div>
        <div v-if="currentQ.options?.length" class="q-options">
          <div v-for="opt in currentQ.options" :key="opt.key" class="opt-item"
               :class="{ correct: opt.correct }">
            <span class="opt-key">{{ opt.key }}.</span>
            <span>{{ opt.text }}</span>
            <el-tag v-if="opt.correct" type="success" size="small">正确</el-tag>
          </div>
        </div>
        <div class="ans-box">
          <h4>标准答案</h4>
          <p>{{ currentQ.answers?.map(a => a.text).join('') || '见解析' }}</p>
        </div>
        <div v-if="currentQ.analysis" class="analysis-box">
          <h4>解析</h4>
          <p>{{ currentQ.analysis }}</p>
        </div>
      </div>
    </el-drawer>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { pageWrong } from '../../api/stats'
import type { Question } from '../../types'

const questions = ref<Question[]>([])
const loading = ref(false)
const current = ref(1)
const pageSize = 10
const total = ref(0)
const showMastered = ref(false)
const detailDrawer = ref(false)
const currentQ = ref<Question | null>(null)

function typeText(t: string) {
  const map: Record<string, string> = { A: '单选', X: '多选', 判断: '判断', 填空: '填空', B: '配伍', 名解: '名解', 简答: '简答', 论述: '论述', 病案: '病案' }
  return map[t] || t
}
function typeTag(t: string) {
  return t === 'X' || t === '病案' ? 'danger' : t === '判断' ? 'success' : 'primary'
}
function showDetail(q: Question) {
  currentQ.value = q
  detailDrawer.value = true
}

async function load() {
  loading.value = true
  try {
    const data = await pageWrong({ current: current.value, size: pageSize, mastered: showMastered.value ? undefined : false }) as any
    questions.value = data.records
    total.value = data.total
  } finally { loading.value = false }
}

onMounted(load)
</script>

<style scoped>
.card-title { font-weight: 700; color: #9b3d2f; }
.card-head { display: flex; justify-content: space-between; align-items: center; }
.stem-preview { display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.detail-content .q-stem { font-size: 15px; line-height: 1.9; margin-bottom: 16px; }
.opt-item { display: flex; gap: 8px; align-items: center; padding: 8px 12px; border: 1px solid #e8e0d2; border-radius: 6px; margin-bottom: 8px; }
.opt-item.correct { border-color: #4a7c59; background: rgba(74,124,89,.08); }
.ans-box, .analysis-box { margin-top: 16px; padding: 14px; background: #f4efe6; border-radius: 8px; }
.ans-box h4, .analysis-box h4 { color: #4a7c59; margin-bottom: 6px; }
</style>
