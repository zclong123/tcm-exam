<template>
  <el-card>
    <template #header>
      <div class="card-head">
        <span class="card-title">🎲 组卷管理</span>
        <el-button type="primary" @click="$router.push('/teacher/paper/smart')">智能组卷</el-button>
      </div>
    </template>

    <el-table :data="papers" v-loading="loading" stripe>
      <el-table-column prop="name" label="试卷名称" min-width="200" />
      <el-table-column label="类型" width="90">
        <template #default="{ row }">
          <el-tag :type="row.paperType === 'SMART' ? 'primary' : 'warning'" size="small">
            {{ row.paperType === 'SMART' ? '智能' : '手工' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="questionCount" label="题目数" width="90" />
      <el-table-column prop="totalScore" label="总分" width="90" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '已发布' : '草稿' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="preview(row)">预览</el-button>
          <el-button size="small" type="danger" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="previewDialog" title="试卷预览" width="70%">
      <div v-if="previewQs" class="preview-body">
        <div v-for="(q, i) in previewQs" :key="q.id" class="pv-item">
          <span class="pv-no">{{ i + 1 }}.</span>
          <div>
            <div class="pv-stem">{{ q.stem }}</div>
            <div v-if="q.options?.length" class="pv-opts">
              <div v-for="opt in q.options" :key="opt.key">{{ opt.key }}. {{ opt.text }}</div>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无题目" />
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pagePapers, getPaperQuestions } from '../../api/exam'
import { getQuestionsBatch } from '../../api/question'

const papers = ref<any[]>([])
const loading = ref(false)
const previewDialog = ref(false)
const previewQs = ref<any[]>([])

async function load() {
  loading.value = true
  try {
    const data = await pagePapers({ current: 1, size: 50 }) as any
    papers.value = data.records
  } finally { loading.value = false }
}

async function preview(row: any) {
  const pqs = await getPaperQuestions(row.id) as any
  const ids = pqs.map((p: any) => p.questionId)
  const qs = await getQuestionsBatch(ids)
  previewQs.value = qs
  previewDialog.value = true
}

async function del(row: any) {
  ElMessageBox.confirm('确定删除该试卷吗？', '提示', { type: 'warning' }).then(async () => {
    await import('../../api/exam').then(m => (m as any).deletePaper(row.id))
    ElMessage.success('已删除')
    load()
  }).catch(() => {})
}

onMounted(load)
</script>

<style scoped>
.card-title { font-weight: 700; color: #9b3d2f; }
.card-head { display: flex; justify-content: space-between; align-items: center; }
.pv-item { display: flex; gap: 10px; margin-bottom: 16px; }
.pv-no { font-weight: 700; color: #9b3d2f; }
.pv-stem { line-height: 1.8; }
.pv-opts { margin-top: 6px; color: #555; }
</style>
