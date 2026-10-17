<template>
  <el-card>
    <template #header><span class="card-title">📖 在线练习</span></template>

    <el-tabs v-model="tab">
      <!-- 章节练习 -->
      <el-tab-pane label="章节练习" name="chapter">
        <el-form label-width="90px" style="max-width: 640px">
          <el-form-item label="学科">
            <el-select v-model="chapter.subjectId" placeholder="选择学科" style="width:100%" @change="onSubjectChange">
              <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="章节/知识点">
            <el-select v-model="chapter.knowledgePointId" placeholder="选择章节（可选）" clearable style="width:100%">
              <el-option v-for="k in kps" :key="k.id" :label="k.name" :value="k.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="题型">
            <el-select v-model="chapter.qtype" placeholder="全部题型" clearable style="width:220px">
              <el-option v-for="t in qtypes" :key="t.value" :label="t.label" :value="t.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="题目数量">
            <el-input-number v-model="chapter.count" :min="1" :max="50" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="starting" @click="startChapter">开始练习</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 随机练习 -->
      <el-tab-pane label="随机练习" name="random">
        <el-form label-width="90px" style="max-width: 640px">
          <el-form-item label="学科范围">
            <el-select v-model="random.subjectId" placeholder="全部学科" clearable style="width:100%">
              <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="题型">
            <el-select v-model="random.qtype" placeholder="混合题型" clearable style="width:220px">
              <el-option v-for="t in qtypes" :key="t.value" :label="t.label" :value="t.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="题目数量">
            <el-input-number v-model="random.count" :min="1" :max="50" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="starting" @click="startRandom">开始练习</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 随机组卷考试 -->
      <el-tab-pane label="随机组卷考试" name="exam">
        <el-form label-width="90px" style="max-width: 720px">
          <el-form-item label="练习名称">
            <el-input v-model="exam.name" placeholder="如：随机练习卷" />
          </el-form-item>
          <el-form-item label="学科">
            <el-select v-model="exam.subjectId" placeholder="全部学科" clearable style="width:100%">
              <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="整体难度">
            <el-select v-model="exam.difficulty" placeholder="倾向难度" clearable style="width:200px">
              <el-option v-for="d in 5" :key="d" :label="'难度' + d" :value="d" />
            </el-select>
          </el-form-item>
          <el-form-item label="题型规则">
            <div style="width:100%">
              <div v-for="(rule, i) in exam.typeRules" :key="i" class="rule-row">
                <el-select v-model="rule.qtype" style="width:120px">
                  <el-option v-for="t in qtypes" :key="t.value" :label="t.label" :value="t.value" />
                </el-select>
                <el-input-number v-model="rule.count" :min="0" :max="100" placeholder="数量" style="width:130px" />
                <el-input-number v-model="rule.score" :min="0.5" :step="0.5" placeholder="分值" style="width:130px" />
                <el-button type="danger" text @click="exam.typeRules.splice(i, 1)">删除</el-button>
              </div>
              <el-button size="small" @click="addRule">添加题型规则</el-button>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="starting" @click="startExam">生成并开始考试</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { startPracticeSession, startPracticeExam } from '../../../api/practice'
import { getSubjects, getKnowledgePoints } from '../../../api/question'

const router = useRouter()
const tab = ref('chapter')

const qtypes = [
  { label: '单选题', value: 'A' },
  { label: '多选题', value: 'X' },
  { label: '判断题', value: '判断' },
  { label: '填空题', value: '填空' },
  { label: '配伍题', value: 'B' },
  { label: '名词解释', value: '名解' },
  { label: '简答题', value: '简答' },
  { label: '论述题', value: '论述' },
  { label: '病案分析', value: '病案' }
]

const subjects = ref<any[]>([])
const kps = ref<any[]>([])
const starting = ref(false)

const chapter = ref<any>({ subjectId: undefined, knowledgePointId: undefined, qtype: undefined, count: 20 })
const random = ref<any>({ subjectId: undefined, qtype: undefined, count: 20 })
const exam = ref<any>({
  name: '',
  subjectId: undefined,
  difficulty: undefined,
  typeRules: [{ qtype: 'A', count: 5, score: 1 }, { qtype: '判断', count: 5, score: 1 }]
})

async function onSubjectChange(v: number) {
  chapter.value.knowledgePointId = undefined
  kps.value = (await getKnowledgePoints(v)) as any
}

function addRule() {
  exam.value.typeRules.push({ qtype: 'A', count: 5, score: 1 })
}

async function startChapter() {
  if (!chapter.value.subjectId) { ElMessage.warning('请选择学科'); return }
  starting.value = true
  try {
    const s = await startPracticeSession({
      mode: 'CHAPTER',
      subjectId: chapter.value.subjectId,
      knowledgePointId: chapter.value.knowledgePointId || undefined,
      qtype: chapter.value.qtype || undefined,
      count: chapter.value.count
    })
    router.push(`/student/practice/play?sid=${s.sessionId}`)
  } catch (e: any) { ElMessage.error('创建练习失败：' + e.message) }
  finally { starting.value = false }
}

async function startRandom() {
  starting.value = true
  try {
    const s = await startPracticeSession({
      mode: 'RANDOM',
      subjectId: random.value.subjectId || undefined,
      qtype: random.value.qtype || undefined,
      count: random.value.count
    })
    router.push(`/student/practice/play?sid=${s.sessionId}`)
  } catch (e: any) { ElMessage.error('创建练习失败：' + e.message) }
  finally { starting.value = false }
}

async function startExam() {
  const rules = exam.value.typeRules.filter((r: any) => r.count > 0)
  if (!rules.length) { ElMessage.warning('请至少配置一个题目数量大于0的题型'); return }
  starting.value = true
  try {
    const d = await startPracticeExam({
      name: exam.value.name || '随机练习卷',
      subjectId: exam.value.subjectId || undefined,
      difficulty: exam.value.difficulty || undefined,
      typeRules: rules
    })
    router.push(`/student/practice/exam-play?rid=${d.record.id}&dur=${d.durationMin}`)
  } catch (e: any) { ElMessage.error('生成练习失败：' + e.message) }
  finally { starting.value = false }
}

onMounted(async () => {
  subjects.value = (await getSubjects()) as any
})
</script>

<style scoped>
.card-title { font-weight: 700; color: #9b3d2f; }
.rule-row { display: flex; gap: 10px; align-items: center; margin-bottom: 10px; }
</style>
