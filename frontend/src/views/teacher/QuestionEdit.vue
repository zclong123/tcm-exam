<template>
  <el-card>
    <template #header><span class="card-title">✏️ {{ isEdit ? '编辑题目' : '新增题目' }}</span></template>

    <el-form :model="form" label-width="90px" style="max-width: 900px">
      <el-row :gutter="16">
        <el-col :span="8">
          <el-form-item label="学科">
            <el-select v-model="form.subjectId" placeholder="选择学科" style="width:100%" @change="onSubjectChange">
              <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="知识点">
            <el-select v-model="form.knowledgePointId" placeholder="选择知识点" style="width:100%">
              <el-option v-for="k in kps" :key="k.id" :label="k.name" :value="k.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="题型">
            <el-select v-model="form.qtype" placeholder="选择题型" style="width:100%" @change="onTypeChange">
              <el-option v-for="t in types" :key="t.value" :label="t.label" :value="t.value" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="8">
          <el-form-item label="难度">
            <el-select v-model="form.difficulty" style="width:100%">
              <el-option v-for="d in 5" :key="d" :label="'难度' + d" :value="d" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="认知层次">
            <el-select v-model="form.cognitiveLevel" style="width:100%">
              <el-option label="记忆" value="记忆" />
              <el-option label="理解" value="理解" />
              <el-option label="应用" value="应用" />
              <el-option label="分析" value="分析" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="分值">
            <el-input-number v-model="form.score" :min="0.5" :step="0.5" style="width:100%" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="题干">
        <el-input v-model="form.stem" type="textarea" :rows="4" placeholder="输入题干（支持富文本）" />
      </el-form-item>

      <!-- 客观题选项 -->
      <template v-if="showOptions">
        <el-form-item label="选项">
          <div v-for="(opt, i) in form.options" :key="i" class="opt-row">
            <span class="opt-label">{{ opt.key }}.</span>
            <el-input v-model="opt.text" placeholder="选项内容" style="flex:1" />
            <el-radio v-if="singleChoice" v-model="answerKey" :label="opt.key" :value="opt.key">正确</el-radio>
            <el-checkbox v-else v-model="opt.correct">正确</el-checkbox>
            <el-button v-if="form.options.length > 2" type="danger" text @click="form.options.splice(i, 1)">删除</el-button>
          </div>
          <el-button size="small" @click="addOption">添加选项</el-button>
        </el-form-item>
      </template>

      <el-form-item label="标准答案">
        <el-input v-model="form.answers[0].text" type="textarea" :rows="3"
                  :placeholder="answerPlaceholder" />
      </el-form-item>

      <el-form-item label="解析">
        <el-input v-model="analysisText" type="textarea" :rows="3" placeholder="答案解析" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
        <el-button @click="$router.back()">返回</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getSubjects, getKnowledgePoints, getQuestion, createQuestion, updateQuestion } from '../../api/question'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.query.id

const subjects = ref<any[]>([])
const kps = ref<any[]>([])
const saving = ref(false)
const answerKey = ref('A')

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

const form = ref<any>({
  id: undefined,
  subjectId: undefined,
  knowledgePointId: undefined,
  qtype: 'A',
  difficulty: 2,
  cognitiveLevel: '记忆',
  score: 1,
  stem: '',
  options: [
    { key: 'A', text: '', correct: true },
    { key: 'B', text: '', correct: false },
    { key: 'C', text: '', correct: false },
    { key: 'D', text: '', correct: false }
  ],
  answers: [{ type: 'A', text: '' }]
})
const analysisText = ref('')

const showOptions = computed(() => ['A', 'X', '判断', 'B'].includes(form.value.qtype))
const singleChoice = computed(() => form.value.qtype === 'A' || form.value.qtype === 'B' || form.value.qtype === '判断')
const answerPlaceholder = computed(() => {
  if (form.value.qtype === 'X') return '多个正确选项的字母，如 ABD'
  if (form.value.qtype === '判断') return '填写：对 或 错'
  if (form.value.qtype === '填空') return '填写答案内容'
  if (showOptions.value) return '填写正确选项字母，如 A'
  return '填写标准答案'
})

function addOption() {
  const keys = 'ABCDEFGH'
  const next = keys[form.value.options.length]
  form.value.options.push({ key: next, text: '', correct: false })
}

function onTypeChange() {
  // 判断题仅两个选项
  if (form.value.qtype === '判断') {
    form.value.options = [
      { key: 'A', text: '对', correct: true },
      { key: 'B', text: '错', correct: false }
    ]
  } else if (form.value.options.length < 4) {
    form.value.options = [
      { key: 'A', text: '', correct: true },
      { key: 'B', text: '', correct: false },
      { key: 'C', text: '', correct: false },
      { key: 'D', text: '', correct: false }
    ]
  }
  form.value.answers = [{ type: form.value.qtype, text: '' }]
}

async function onSubjectChange(v: number) {
  kps.value = (await getKnowledgePoints(v)) as any
}

async function save() {
  if (!form.value.stem) { ElMessage.warning('请输入题干'); return }
  if (showOptions.value && form.value.options.some(o => !o.text)) {
    ElMessage.warning('请填写所有选项内容'); return
  }
  // 客观题答案与选项一致性
  if (showOptions.value) {
    if (singleChoice.value) {
      form.value.options.forEach(o => o.correct = (o.key === answerKey.value))
    }
    form.value.answers = [{
      type: form.value.qtype,
      text: form.value.options.filter(o => o.correct).map(o => o.key).sort().join('')
    }]
  }
  saving.value = true
  try {
    const payload = {
      ...form.value,
      analysis: analysisText.value,
      options: showOptions.value ? form.value.options : []
    }
    if (isEdit) {
      await updateQuestion(payload)
    } else {
      await createQuestion(payload)
    }
    ElMessage.success('保存成功')
    router.back()
  } finally { saving.value = false }
}

onMounted(async () => {
  subjects.value = (await getSubjects()) as any
  if (isEdit) {
    const q = await getQuestion(Number(route.query.id)) as any
    form.value = {
      id: q.id,
      subjectId: q.subjectId,
      knowledgePointId: q.knowledgePointId,
      qtype: q.qtype,
      difficulty: q.difficulty,
      cognitiveLevel: q.cognitiveLevel,
      score: q.score,
      stem: q.stem,
      options: q.options?.length ? q.options.map((o: any) => ({ key: o.key, text: o.text, correct: o.correct })) : [],
      answers: q.answers?.length ? q.answers : [{ type: q.qtype, text: '' }]
    }
    analysisText.value = q.analysis || ''
    if (singleChoice.value && form.value.options.length) {
      answerKey.value = form.value.options.find((o: any) => o.correct)?.key || 'A'
    }
    if (form.value.subjectId) onSubjectChange(form.value.subjectId)
  }
})
</script>

<style scoped>
.card-title { font-weight: 700; color: #9b3d2f; }
.opt-row { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.opt-label { font-weight: 700; color: #4a7c59; }
</style>
