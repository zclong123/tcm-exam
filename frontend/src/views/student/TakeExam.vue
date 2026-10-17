<template>
  <div v-if="loading" class="loading-page">
    <el-skeleton :rows="10" animated style="max-width:800px;margin:0 auto;padding:40px" />
  </div>
  <div v-else class="exam-page">
    <!-- 顶部信息栏 -->
    <div class="top-bar">
      <div class="exam-info">
        <span class="exam-name">{{ detail?.exam.name }}</span>
        <span class="exam-meta">剩余时间：{{ timeText }}</span>
      </div>
      <div class="top-actions">
        <el-button size="small" @click="submitConfirm">交卷</el-button>
      </div>
    </div>

    <div class="exam-body">
      <!-- 题目区 -->
      <div class="question-pane">
        <el-card class="q-card">
          <div class="q-head">
            <span class="q-seq">第 {{ currentIdx + 1 }} 题</span>
            <el-tag size="small" :type="qtypeTag(current.qtype)">{{ qtypeText(current.qtype) }}</el-tag>
            <el-tag size="small" effect="plain">{{ current.score }} 分</el-tag>
            <el-tag v-if="current.difficulty" size="small" type="warning" effect="plain">
              难度 {{ '★'.repeat(current.difficulty) }}
            </el-tag>
          </div>

          <div class="q-stem" v-html="current.stem"></div>

          <!-- 客观题选项 -->
          <div v-if="current.options && current.options.length" class="q-options">
            <div v-for="opt in current.options" :key="opt.key" class="opt-item"
                 :class="{ selected: isSelected(opt.key) }" @click="toggleOption(opt.key)">
              <span class="opt-key">{{ opt.key }}.</span>
              <span class="opt-text">{{ opt.text }}</span>
            </div>
          </div>

          <!-- 主观题/填空 -->
          <div v-else class="q-textarea">
            <el-input v-model="answers[current.id]" type="textarea" :rows="6"
                      placeholder="请输入你的作答（若为填空题请直接填写）" />
          </div>

          <div class="q-actions">
            <el-button @click="prev" :disabled="currentIdx === 0">上一题</el-button>
            <el-button @click="next" :disabled="currentIdx === total - 1">下一题</el-button>
            <el-button @click="toggleMark" :type="marked ? 'warning' : 'default'">
              {{ marked ? '取消标记' : '标记本题' }}
            </el-button>
          </div>
        </el-card>
      </div>

      <!-- 答题卡 -->
      <div class="answer-sheet">
        <el-card>
          <template #header><span class="sheet-title">答题卡</span></template>
          <div class="answer-grid">
            <div v-for="(q, i) in questions" :key="q.id" class="cell"
                 :class="cellClass(q, i)" @click="currentIdx = i">
              {{ i + 1 }}
            </div>
          </div>
          <div class="legend">
            <div><span class="dot done"></span>已作答</div>
            <div><span class="dot current"></span>当前</div>
            <div><span class="dot marked"></span>标记</div>
            <div><span class="dot blank"></span>未答</div>
          </div>
          <el-button type="primary" class="submit-btn" @click="submitConfirm">交 卷</el-button>
        </el-card>
      </div>
    </div>

    <el-dialog v-model="submitDialog" title="确认交卷" width="360px">
      <p>已作答 {{ answeredCount }} / {{ total }} 题，确定要交卷吗？</p>
      <template #footer>
        <el-button @click="submitDialog = false">继续答题</el-button>
        <el-button type="primary" @click="doSubmit">确认交卷</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { startExam, saveAnswers, submitExam, reportSwitch, autoGrade } from '../../api/exam'
import type { ExamDetail, Question } from '../../types'

const route = useRoute()
const router = useRouter()
const examId = Number(route.params.examId)

const loading = ref(true)
const detail = ref<ExamDetail | null>(null)
const currentIdx = ref(0)
const answers = ref<Record<number, string>>({})
const markedSet = ref<Set<number>>(new Set())
const submitDialog = ref(false)
const timer = ref<any>(null)
const remainingSec = ref(0)
const autosaveTimer = ref<any>(null)
const switchCount = ref(0)

const questions = computed(() => detail.value?.questions || [])
const total = computed(() => questions.value.length)
const current = computed<Question>(() => questions.value[currentIdx.value] || {} as Question)
const marked = computed(() => markedSet.value.has(current.value.id))

const timeText = computed(() => {
  const m = Math.floor(remainingSec.value / 60)
  const s = remainingSec.value % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
})

const answeredCount = computed(() => questions.value.filter(q => answers.value[q.id] !== undefined && answers.value[q.id] !== '').length)

function qtypeText(t: string) {
  const map: Record<string, string> = {
    A: '单选题', X: '多选题', 判断: '判断题', 填空: '填空题',
    B: '配伍题', 名解: '名词解释', 简答: '简答题', 论述: '论述题', 病案: '病案分析'
  }
  return map[t] || t
}
function qtypeTag(t: string) {
  const obj: Record<string, any> = { A: '', X: 'warning', 判断: 'success', 填空: 'info', B: 'danger' }
  return obj[t] || ''
}
function isSelected(key: string) {
  const v = answers.value[current.value.id] || ''
  if (current.value.qtype === 'X') {
    return v.includes(key)
  }
  return v === key
}
function toggleOption(key: string) {
  const id = current.value.id
  let v = answers.value[id] || ''
  if (current.value.qtype === 'X') {
    v = v.includes(key) ? v.replace(key, '') : v + key
    answers.value[id] = v
  } else {
    answers.value[id] = v === key ? '' : key
  }
}
function cellClass(q: Question, i: number) {
  const answered = answers.value[q.id] !== undefined && answers.value[q.id] !== ''
  return {
    done: answered,
    current: i === currentIdx.value,
    marked: markedSet.value.has(q.id),
    blank: !answered
  }
}
function prev() { if (currentIdx.value > 0) currentIdx.value-- }
function next() { if (currentIdx.value < total.value - 1) currentIdx.value++ }
function toggleMark() {
  if (markedSet.value.has(current.value.id)) markedSet.value.delete(current.value.id)
  else markedSet.value.add(current.value.id)
}
function submitConfirm() { submitDialog.value = true }

async function doSubmit() {
  try {
    await persistAnswers()
    await submitExam(detail.value!.record.id)
    // 自动判分
    await autoGrade(detail.value!.record.id)
    ElMessage.success('交卷成功，客观题已自动判分')
    router.push('/student/scores')
  } catch (e: any) {
    ElMessage.error('交卷失败：' + e.message)
  }
}

async function persistAnswers() {
  if (!detail.value) return
  const items = questions.value
    .filter(q => answers.value[q.id] !== undefined)
    .map(q => ({ questionId: q.id, answer: answers.value[q.id], isMarked: markedSet.value.has(q.id) }))
  if (items.length) {
    await saveAnswers(detail.value.record.id, items)
  }
}

function startTimer() {
  const dur = detail.value?.durationMin || 90
  remainingSec.value = dur * 60
  timer.value = setInterval(() => {
    remainingSec.value--
    if (remainingSec.value <= 0) {
      clearInterval(timer.value)
      doSubmit()
    }
  }, 1000)
}

// 防切屏
function onVisibility() {
  if (document.hidden && detail.value?.exam.antiCheat) {
    switchCount.value++
    reportSwitch(detail.value.record.id)
    const limit = detail.value.exam.switchLimit || 3
    if (switchCount.value > limit) {
      ElMessageBox.alert('切屏次数超过限制，系统将自动交卷！', '警告', { type: 'warning' }).then(() => {
        doSubmit()
      })
    } else {
      ElMessage.warning(`检测到切屏（${switchCount.value}/${limit} 次）`)
    }
  }
}

onMounted(async () => {
  try {
    detail.value = await startExam(examId) as any
    // 断点恢复
    const ansMap = detail.value.answers || {}
    Object.entries(ansMap).forEach(([qid, a]: any) => {
      if (a.studentAnswer) answers.value[Number(qid)] = a.studentAnswer
      if (a.isMarked) markedSet.value.add(Number(qid))
    })
    loading.value = false
    startTimer()
    document.addEventListener('visibilitychange', onVisibility)
    // 自动保存每 30s
    autosaveTimer.value = setInterval(() => persistAnswers().catch(() => {}), 30000)
    // 禁用复制/右键
    document.addEventListener('contextmenu', preventCtx)
  } catch (e: any) {
    ElMessage.error('进入考试失败：' + e.message)
    router.push('/student/exams')
  }
})

function preventCtx(e: Event) { e.preventDefault() }

onBeforeUnmount(() => {
  clearInterval(timer.value)
  clearInterval(autosaveTimer.value)
  document.removeEventListener('visibilitychange', onVisibility)
  document.removeEventListener('contextmenu', preventCtx)
})
</script>

<style scoped>
.exam-page { display: flex; flex-direction: column; height: calc(100vh - 40px); }
.top-bar {
  display: flex; align-items: center; justify-content: space-between;
  background: #2b2723; color: #f4efe6; padding: 12px 20px; border-radius: 8px; margin-bottom: 16px;
}
.exam-name { font-size: 16px; font-weight: 700; margin-right: 20px; }
.exam-meta { color: #d9c9a8; }
.exam-body { display: flex; gap: 16px; flex: 1; min-height: 0; }
.question-pane { flex: 1; overflow-y: auto; }
.answer-sheet { width: 240px; flex-shrink: 0; overflow-y: auto; }
.q-head { display: flex; align-items: center; gap: 10px; margin-bottom: 14px; }
.q-seq { font-weight: 700; color: #9b3d2f; }
.q-stem { font-size: 16px; line-height: 1.9; margin-bottom: 18px; white-space: pre-wrap; }
.opt-item {
  display: flex; align-items: flex-start; gap: 8px; padding: 10px 14px;
  border: 1px solid #e8e0d2; border-radius: 8px; margin-bottom: 10px; cursor: pointer;
  transition: all .15s;
}
.opt-item:hover { border-color: #9b3d2f; }
.opt-item.selected { background: rgba(155,61,47,.08); border-color: #9b3d2f; }
.opt-key { font-weight: 700; color: #4a7c59; flex-shrink: 0; }
.q-actions { margin-top: 20px; display: flex; gap: 10px; }
.answer-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 8px; margin-bottom: 12px; }
.cell {
  height: 34px; display: flex; align-items: center; justify-content: center;
  border: 1px solid #d9d0c0; border-radius: 6px; cursor: pointer; font-size: 13px;
  background: #fbf8f3;
}
.cell.done { background: #4a7c59; color: #fff; border-color: #4a7c59; }
.cell.current { border: 2px solid #9b3d2f; font-weight: 700; }
.cell.marked { background: #e0a990; color: #fff; border-color: #e0a990; }
.legend { display: flex; gap: 12px; font-size: 12px; color: #736b5e; margin-bottom: 14px; flex-wrap: wrap; }
.legend .dot { display: inline-block; width: 12px; height: 12px; border-radius: 3px; margin-right: 4px; vertical-align: -1px; }
.dot.done { background: #4a7c59; }
.dot.current { border: 2px solid #9b3d2f; }
.dot.marked { background: #e0a990; }
.dot.blank { border: 1px solid #d9d0c0; }
.submit-btn { width: 100%; }
.sheet-title { font-weight: 700; color: #9b3d2f; }
.loading-page { padding-top: 60px; }
</style>
