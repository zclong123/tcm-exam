<template>
  <el-card>
    <template #header>
      <div class="card-head">
        <span class="card-title">🗂 题库管理（内置 10000+ 道中医试题）</span>
        <el-button type="primary" @click="$router.push('/teacher/question/edit')">新增题目</el-button>
      </div>
    </template>

    <!-- 筛选 -->
    <div class="filters">
      <el-select v-model="query.subjectId" placeholder="学科" clearable style="width:150px" @change="load">
        <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
      </el-select>
      <el-select v-model="query.knowledgePointId" placeholder="知识点" clearable style="width:160px" @change="load">
        <el-option v-for="k in kps" :key="k.id" :label="k.name" :value="k.id" />
      </el-select>
      <el-select v-model="query.qtype" placeholder="题型" clearable style="width:130px" @change="load">
        <el-option v-for="t in types" :key="t.value" :label="t.label" :value="t.value" />
      </el-select>
      <el-select v-model="query.difficulty" placeholder="难度" clearable style="width:100px" @change="load">
        <el-option v-for="d in 5" :key="d" :label="'难度' + d" :value="d" />
      </el-select>
      <el-select v-model="query.status" placeholder="审核状态" clearable style="width:120px" @change="load">
        <el-option label="待审核" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已驳回" :value="2" />
      </el-select>
      <el-input v-model="query.stem" placeholder="搜索题干关键词" clearable style="width:200px" @change="load" @clear="load" />
    </div>

    <el-table :data="rows" v-loading="loading" stripe>
      <el-table-column label="题型" width="80">
        <template #default="{ row }">
          <el-tag size="small" :type="typeTag(row.qtype)">{{ typeText(row.qtype) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="stem" label="题干" min-width="300">
        <template #default="{ row }">
          <span class="stem-preview">{{ row.stem }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="subjectName" label="学科" width="110" />
      <el-table-column prop="knowledgePointName" label="知识点" width="110" />
      <el-table-column label="难度" width="70">
        <template #default="{ row }">{{ '★'.repeat(row.difficulty || 1) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'danger' : 'info'" size="small">
            {{ row.status === 1 ? '通过' : row.status === 2 ? '驳回' : '待审' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="edit(row)">编辑</el-button>
          <el-button v-if="row.status !== 1" size="small" type="success" @click="audit(row, 1)">通过</el-button>
          <el-button size="small" type="danger" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="current"
      :page-size="query.size!"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="load"
      style="margin-top:16px; justify-content: flex-end"
    />
  </el-card>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageQuestions, deleteQuestion, auditQuestion, getSubjects, getKnowledgePoints } from '../../api/question'
import type { Question } from '../../types'

const router = useRouter()
const rows = ref<Question[]>([])
const subjects = ref<any[]>([])
const kps = ref<any[]>([])
const loading = ref(false)
const current = ref(1)
const total = ref(0)
const query = ref({
  current: 1,
  size: 10,
  subjectId: undefined as number | undefined,
  knowledgePointId: undefined as number | undefined,
  qtype: undefined as string | undefined,
  difficulty: undefined as number | undefined,
  status: undefined as number | undefined,
  stem: ''
})
const types = [
  { value: 'A', label: '单选题' },
  { value: 'X', label: '多选题' },
  { value: '判断', label: '判断题' },
  { value: '填空', label: '填空题' },
  { value: 'B', label: '配伍题' },
  { value: '名解', label: '名词解释' },
  { value: '简答', label: '简答题' },
  { value: '论述', label: '论述题' },
  { value: '病案', label: '病案分析' }
]

function typeText(t: string) { return types.find(x => x.value === t)?.label || t }
function typeTag(t: string) {
  return t === 'X' || t === '病案' ? 'danger' : t === '判断' ? 'success' : t === 'A' ? 'primary' : 'warning'
}

watch(() => query.value.subjectId, async (v) => {
  kps.value = (await getKnowledgePoints(v)) as any
})

async function load() {
  loading.value = true
  try {
    query.value.current = current.value
    const data = await pageQuestions(query.value) as any
    rows.value = data.records
    total.value = data.total
  } finally { loading.value = false }
}

function edit(row: Question) {
  router.push({ path: '/teacher/question/edit', query: { id: row.id } })
}
function del(row: Question) {
  ElMessageBox.confirm('确定删除该题目吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteQuestion(row.id)
    ElMessage.success('已删除')
    load()
  }).catch(() => {})
}
async function audit(row: Question, status: number) {
  await auditQuestion(row.id, status)
  ElMessage.success(status === 1 ? '已通过审核' : '已驳回')
  load()
}

onMounted(async () => {
  subjects.value = (await getSubjects()) as any
  load()
})
</script>

<style scoped>
.card-title { font-weight: 700; color: #9b3d2f; }
.card-head { display: flex; justify-content: space-between; align-items: center; }
.filters { display: flex; gap: 10px; margin-bottom: 16px; flex-wrap: wrap; }
.stem-preview { display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
</style>
