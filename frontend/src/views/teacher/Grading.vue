<template>
  <el-card>
    <template #header><span class="card-title">✅ 阅卷台（主观题人工批阅）</span></template>

    <el-table :data="rows" v-loading="loading" stripe>
      <el-table-column prop="id" label="作答ID" width="80" />
      <el-table-column prop="recordId" label="记录ID" width="80" />
      <el-table-column prop="questionId" label="题目ID" width="80" />
      <el-table-column prop="studentAnswer" label="学生作答" min-width="240">
        <template #default="{ row }">
          <span class="ans-preview">{{ row.studentAnswer || '（未作答）' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="reviewStatus" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.reviewStatus === 'REVIEWED' ? 'success' : 'warning'" size="small">
            {{ row.reviewStatus === 'REVIEWED' ? '已批' : '待批' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openReview(row)">批阅</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="current"
      :page-size="pageSize"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="load"
      style="margin-top:16px; justify-content: flex-end"
    />

    <el-dialog v-model="reviewDialog" title="批阅" width="560px">
      <div v-if="currentRow" class="review-body">
        <div class="r-label">学生作答：</div>
        <div class="r-content">{{ currentRow.studentAnswer || '（未作答）' }}</div>
        <div class="r-label" style="margin-top:12px">批阅得分：</div>
        <el-input-number v-model="reviewScore" :min="0" :max="20" :step="0.5" />
        <div class="r-label" style="margin-top:12px">评语：</div>
        <el-input v-model="reviewComment" type="textarea" :rows="3" placeholder="可选评语" />
      </div>
      <template #footer>
        <el-button @click="reviewDialog = false">取消</el-button>
        <el-button type="primary" @click="doReview">提交批阅</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { reviewList, review } from '../../api/exam'

const rows = ref<any[]>([])
const loading = ref(false)
const current = ref(1)
const pageSize = 10
const total = ref(0)
const reviewDialog = ref(false)
const currentRow = ref<any>(null)
const reviewScore = ref(0)
const reviewComment = ref('')

async function load() {
  loading.value = true
  try {
    const data = await reviewList({ current: current.value, size: pageSize }) as any
    rows.value = data.records
    total.value = data.total
  } finally { loading.value = false }
}

function openReview(row: any) {
  currentRow.value = row
  reviewScore.value = row.score || 0
  reviewComment.value = row.reviewComment || ''
  reviewDialog.value = true
}

async function doReview() {
  await review(currentRow.value.id, reviewScore.value, reviewComment.value)
  ElMessage.success('批阅成功')
  reviewDialog.value = false
  load()
}

onMounted(load)
</script>

<style scoped>
.card-title { font-weight: 700; color: #9b3d2f; }
.ans-preview { display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.r-label { font-weight: 700; color: #4a7c59; margin-bottom: 4px; }
.r-content { padding: 10px; background: #f4efe6; border-radius: 6px; line-height: 1.8; }
</style>
