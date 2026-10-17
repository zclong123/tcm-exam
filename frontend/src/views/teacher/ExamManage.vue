<template>
  <el-card>
    <template #header>
      <div class="card-head">
        <span class="card-title">⏰ 考试管理</span>
        <el-button type="primary" @click="openCreate">发布考试</el-button>
      </div>
    </template>

    <el-table :data="exams" v-loading="loading" stripe>
      <el-table-column prop="name" label="考试名称" min-width="160" />
      <el-table-column prop="paperId" label="试卷ID" width="90" />
      <el-table-column label="开始时间" width="170">
        <template #default="{ row }">{{ fmt(row.startTime) }}</template>
      </el-table-column>
      <el-table-column label="结束时间" width="170">
        <template #default="{ row }">{{ fmt(row.endTime) }}</template>
      </el-table-column>
      <el-table-column prop="durationMin" label="时长" width="80" />
      <el-table-column label="防切屏" width="90">
        <template #default="{ row }">
          <el-tag :type="row.antiCheat ? 'success' : 'info'" size="small">{{ row.antiCheat ? '开启' : '关闭' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" title="发布考试" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="考试名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="选择试卷">
          <el-select v-model="form.paperId" placeholder="选择试卷" style="width:100%">
            <el-option v-for="p in papers" :key="p.id" :label="`${p.name}（${p.questionCount}题/${p.totalScore}分）`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="form.startTime" type="datetime" style="width:100%" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="form.endTime" type="datetime" style="width:100%" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="时长(分钟)">
          <el-input-number v-model="form.durationMin" :min="5" :max="300" />
        </el-form-item>
        <el-form-item label="及格线">
          <el-input-number v-model="form.passScore" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="题目乱序">
          <el-switch v-model="form.shuffle" />
        </el-form-item>
        <el-form-item label="防切屏">
          <el-switch v-model="form.antiCheat" />
        </el-form-item>
        <el-form-item label="切屏上限">
          <el-input-number v-model="form.switchLimit" :min="0" :max="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">发布</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageExams, createExam, deleteExam, pagePapers } from '../../api/exam'

const exams = ref<any[]>([])
const papers = ref<any[]>([])
const loading = ref(false)
const dialog = ref(false)
const form = ref<any>({
  name: '', paperId: undefined, startTime: '', endTime: '',
  durationMin: 90, passScore: 60, shuffle: true, antiCheat: true, switchLimit: 3
})

function fmt(s?: string) { return s ? s.replace('T', ' ').slice(0, 16) : '-' }
function statusType(s: number) { return s === 1 ? 'success' : s === 2 ? 'info' : s === 0 ? 'warning' : 'info' }
function statusText(s: number) { return s === 1 ? '进行中' : s === 2 ? '已结束' : s === 0 ? '未开始' : '草稿' }

async function load() {
  loading.value = true
  try {
    const data = await pageExams({ current: 1, size: 50 }) as any
    exams.value = data.records
    const pdata = await pagePapers({ current: 1, size: 100 }) as any
    papers.value = pdata.records
  } finally { loading.value = false }
}

function openCreate() {
  form.value = { name: '', paperId: undefined, startTime: '', endTime: '', durationMin: 90, passScore: 60, shuffle: true, antiCheat: true, switchLimit: 3 }
  dialog.value = true
}

async function save() {
  if (!form.value.name || !form.value.paperId) { ElMessage.warning('请填写名称并选择试卷'); return }
  await createExam({ ...form.value, status: 1 })
  ElMessage.success('考试发布成功')
  dialog.value = false
  load()
}

function del(row: any) {
  ElMessageBox.confirm('确定删除该考试吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteExam(row.id)
    ElMessage.success('已删除')
    load()
  }).catch(() => {})
}

onMounted(load)
</script>

<style scoped>
.card-title { font-weight: 700; color: #9b3d2f; }
.card-head { display: flex; justify-content: space-between; align-items: center; }
</style>
