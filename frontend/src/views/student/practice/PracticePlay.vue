<template>
  <div v-if="loading" class="loading-page">
    <el-skeleton :rows="8" animated style="max-width:800px;margin:0 auto;padding:40px" />
  </div>

  <!-- 结束统计 -->
  <div v-else-if="finished" class="result-page">
    <el-card style="max-width:560px;margin:0 auto;text-align:center">
      <div class="result-title">练习完成 🎉</div>
      <div class="result-stats">
        <div class="stat"><div class="num">{{ total }}</div><div class="lbl">总题数</div></div>
        <div class="stat"><div class="num good">{{ correctCount }}</div><div class="lbl">答对</div></div>
        <div class="stat"><div class="num bad">{{ wrongCount }}</div><div class="lbl">答错</div></div>
        <div class="stat"><div class="num gray">{{ subjectiveCount }}</div><div class="lbl">主观题</div></div>
      </div>
      <div class="result-actions">
        <el-button type="primary" @click="restart">再来一组</el-button>
        <el-button @click="goHome">返回练习</el-button>
      </div>
    </el-card>
  </div>

  <!-- 答题 -->
  <div v-else class="play-page">
    <div class="top-bar">
      <div class="play-info">
        <span class="play-name">{{ modeText }} · {{ currentIdx + 1 }}/{{ total }} 题</span>
        <el-tag size="small" effect="plain">已答 {{ answeredCount }} / {{ total }}</el-tag>
      </div>
      <div class="top-actions">
        <el-button size="small" @click="quit">退出</el-button>
      </div>
    </div>

    <el-card class="q-card">
      <div class="q-head">
        <span class="q-seq">第 {{ currentIdx + 1 }} 题</span>
        <el-tag size="small" :type="qtypeTag(current.qtype)">{{ qtypeText(current.qtype) }}</el-tag>
        <el-tag v-if="current.difficulty" size="small" type="warning" effect="plain">难度 {{ '★'.repeat(current.difficulty) }}</el-tag>
      </div>

      <div class="q-stem" v-html="current.stem"></div>

      <!-- 客观题选项 -->
      <div v-if="current.options && current.options.length" class="q-options">
        <div v-for="opt in current.options" :key="opt.key" class="opt-item"
             :class="optClass(opt.key)" @click="onOptionClick(opt.key)">
          <span class="opt-key">{{ opt.key }}.</span>
          <span class="opt-text">{{ opt.text }}</span>
          <el-tag v-if="submitted && feedback && opt.key === normKey(feedback.correctAnswer)" type="success" size="small">正确答案</el-tag>
        </div>
      </div>

      <!-- 填空/主观题 -->
      <div v-else class="q-textarea">
        <el-input v-model="currentAnswer" type="textarea" :rows="6" :disabled="submitted"
                  placeholder="请输入你的作答（若为填空题请直接填写）" />
      </div>

      <div class="q-actions">
        <template v-if="!submitted">
          <el-button v-if="manualSubmit" type="primary" :disabled="!canSubmit" @click="doSubmit">提交本题</el-button>
          <span v-else class="auto-hint">点击选项后将自动判题</span>
        </template>
        <template v-else>
          <el-button @click="next" :disabled="currentIdx === total - 1">下一题</el-button>
          <el-button v-if="currentIdx === total - 1" type="primary" @click="finish">结束练习</el-button>
        </template>
      </div>
    </el-card>

    <!-- 立即反馈 -->
    <el-card v-if="submitted && feedback" class="feedback-card" :class="feedback.correct ? 'fb-good' : 'fb-bad'">
      <div class="fb-head">
        <el-tag v-if="feedback.correct === true" type="success">✓ 回答正确</el-tag>
        <el-tag v-else-if="feedback.correct === false" type="danger">✗ 回答错误</el-tag>
        <el-tag v-else type="info">主观题 · 请对照参考答案自查</el-tag>
        <span v-if="feedback.score" class="fb-score">得分 {{ feedback.score }} 分</span>
      </div>
      <div v-if="feedback.correctAnswer" class="fb-answer">
        <h4>正确答案</h4>
        <p>{{ feedback.correctAnswer }}</p>
      </div>
      <div v-if="feedback.analysis" class="fb-analysis">
        <h4>解析</h4>
        <p>{{ feedback.analysis }}</p>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPracticeSession, submitPracticeAnswer } from '../../../api/practice'
import type { PracticeFeedback, Question } from '../../../types'

const route = useRoute()
const router = useRouter()
const sessionId = String(route.query.sid || '')

const loading = ref(true)
const questions = ref<Question[]>([])
const currentIdx = ref(0)
const answers = ref<Record<number, string>>({})
const feedbacks = ref<Record<number, PracticeFeedback>>({})
const submitting = ref(false)
const finished = ref(false)

const current = computed<Question>(() => questions.value[currentIdx.value] || {} as Question)
const total = computed(() => questions.value.length)
const submitted = computed(() => !!feedbacks.value[current.value.id])
const feedback = computed(() => feedbacks.value[current.value.id] || null)
const currentAnswer = computed({
  get: () => answers.value[current.value.id] || '',
  set: (v) => { answers.value[current.value.id] = v }
})
const answeredCount = computed(() => Object.values(feedbacks.value).length)
const canSubmit = computed(() => {
  if (submitted.value) return false
  const v = answers.value[current.value.id]
  if (!v || !String(v).trim()) return false
  if (current.value.qtype === 'X') return String(v).length > 0
  return true
})
const hasOptions = computed(() => !!(current.value.options && current.value.options.length))
const manualSubmit = computed(() => !hasOptions.value || current.value.qtype === 'X')

const correctCount = computed(() => Object.values(feedbacks.value).filter(f => f.correct === true).length)
const wrongCount = computed(() => Object.values(feedbacks.value).filter(f => f.correct === false).length)
const subjectiveCount = computed(() => Object.values(feedbacks.value).filter(f => f.correct === null).length)

const modeText = computed(() => {
  const s = route.query.mode || ''
  return s === 'CHAPTER' ? '章节练习' : '随机练习'
})

function normKey(s: string) {
  return (s || '').trim().toUpperCase()
}

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
  if (current.value.qtype === 'X') return v.includes(key)
  return v === key
}
function onOptionClick(key: string) {
  if (submitted.value || submitting.value) return
  const id = current.value.id
  if (!id) return
  if (current.value.qtype === 'X') {
    const v = answers.value[id] || ''
    answers.value[id] = v.includes(key) ? v.replace(key, '') : v + key
  } else {
    answers.value[id] = key
    doSubmit()
  }
}
function optClass(key: string) {
  if (!submitted.value) return { selected: isSelected(key) }
  const fb = feedback.value
  const ck = normKey(fb?.correctAnswer)
  if (fb?.correct === true) return { correct: normKey(key) === ck }
  return { wrongPick: isSelected(key) && normKey(key) !== ck, correct: normKey(key) === ck }
}

let autoNextTimer: number | undefined
function clearAutoNext() {
  if (autoNextTimer !== undefined) { window.clearTimeout(autoNextTimer); autoNextTimer = undefined }
}
function scheduleAutoNext() {
  clearAutoNext()
  autoNextTimer = window.setTimeout(() => {
    autoNextTimer = undefined
    if (!submitted.value) return
    if (currentIdx.value < total.value - 1) currentIdx.value++
    else finished.value = true
  }, 800)
}

async function doSubmit() {
  if (!current.value.id || submitted.value || submitting.value) return
  submitting.value = true
  try {
    const fb = await submitPracticeAnswer(sessionId, current.value.id, answers.value[current.value.id] || '')
    feedbacks.value = { ...feedbacks.value, [current.value.id]: fb }
    if (fb.correct === true) scheduleAutoNext()
  } catch (e: any) {
    ElMessage.error('提交失败：' + e.message)
    if (e.message?.includes('会话')) {
      quit()
    }
  } finally { submitting.value = false }
}

function next() { clearAutoNext(); if (currentIdx.value < total.value - 1) currentIdx.value++ }

function finish() { clearAutoNext(); finished.value = true }

function restart() {
  router.push('/student/practice')
}
function goHome() { router.push('/student/practice') }

function quit() {
  ElMessageBox.confirm('退出后本次练习进度将不保留，确定退出吗？', '提示', { type: 'warning' })
    .then(() => router.push('/student/practice'))
    .catch(() => {})
}

onMounted(async () => {
  if (!sessionId) {
    ElMessage.warning('练习会话不存在')
    router.push('/student/practice')
    return
  }
  try {
    const s = await getPracticeSession(sessionId)
    questions.value = s.questions || []
    if (!questions.value.length) {
      ElMessage.warning('练习会话已失效，请重新开始')
      router.push('/student/practice')
      return
    }
  } catch (e: any) {
    ElMessage.error('加载练习失败：' + e.message)
    router.push('/student/practice')
    return
  } finally { loading.value = false }
})

onBeforeUnmount(clearAutoNext)
</script>

<style scoped>
.play-page { display: flex; flex-direction: column; max-width: 860px; margin: 0 auto; }
.top-bar {
  display: flex; align-items: center; justify-content: space-between;
  background: #2b2723; color: #f4efe6; padding: 12px 20px; border-radius: 8px; margin-bottom: 16px;
}
.play-name { font-size: 16px; font-weight: 700; margin-right: 14px; }
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
.opt-item.correct { border-color: #4a7c59; background: rgba(74,124,89,.1); }
.opt-item.wrongPick { border-color: #c0392b; background: rgba(192,57,43,.08); }
.opt-key { font-weight: 700; color: #4a7c59; flex-shrink: 0; }
.q-actions { margin-top: 20px; display: flex; gap: 10px; align-items: center; }
.auto-hint { color: #736b5e; font-size: 13px; }
.q-textarea { margin-top: 6px; }
.feedback-card { margin-top: 16px; border-left: 4px solid #4a7c59; }
.feedback-card.fb-bad { border-left-color: #c0392b; }
.fb-head { display: flex; align-items: center; gap: 12px; margin-bottom: 10px; }
.fb-score { color: #736b5e; font-size: 13px; }
.fb-answer, .fb-analysis { margin-top: 10px; padding: 12px 14px; background: #f4efe6; border-radius: 8px; }
.fb-answer h4, .fb-analysis h4 { color: #4a7c59; margin-bottom: 6px; }
.loading-page { padding-top: 60px; }
.result-page { padding-top: 60px; }
.result-title { font-size: 22px; font-weight: 700; color: #9b3d2f; margin-bottom: 24px; }
.result-stats { display: flex; justify-content: center; gap: 40px; margin-bottom: 24px; }
.stat .num { font-size: 32px; font-weight: 700; color: #2b2723; }
.stat .num.good { color: #4a7c59; }
.stat .num.bad { color: #c0392b; }
.stat .num.gray { color: #999; }
.stat .lbl { color: #736b5e; font-size: 13px; margin-top: 4px; }
.result-actions { display: flex; justify-content: center; gap: 12px; }
</style>
