<template>
  <el-card>
    <template #header><span class="card-title">🎲 智能组卷</span></template>

    <el-form label-width="90px" style="max-width: 720px">
      <el-form-item label="试卷名称">
        <el-input v-model="form.name" placeholder="如：中医基础理论阶段测试" />
      </el-form-item>
      <el-form-item label="学科">
        <el-select v-model="form.subjectId" placeholder="选择学科" style="width:100%" @change="onSubjectChange">
          <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="整体难度">
        <el-select v-model="form.difficulty" placeholder="倾向难度" style="width:200px">
          <el-option v-for="d in 5" :key="d" :label="'难度' + d" :value="d" />
        </el-select>
      </el-form-item>

      <el-form-item label="题型规则">
        <div style="width:100%">
          <div v-for="(rule, i) in form.typeRules" :key="i" class="rule-row">
            <el-select v-model="rule.qtype" style="width:120px">
              <el-option label="单选" value="A" />
              <el-option label="多选" value="X" />
              <el-option label="判断" value="判断" />
              <el-option label="填空" value="填空" />
              <el-option label="配伍" value="B" />
            </el-select>
            <el-input-number v-model="rule.count" :min="0" :max="100" placeholder="数量" style="width:130px" />
            <el-input-number v-model="rule.score" :min="0.5" :step="0.5" placeholder="分值" style="width:130px" />
            <el-select v-model="rule.difficulty" placeholder="难度" clearable style="width:100px">
              <el-option v-for="d in 5" :key="d" :label="'难度' + d" :value="d" />
            </el-select>
            <el-button type="danger" text @click="form.typeRules.splice(i, 1)">删除</el-button>
          </div>
          <el-button size="small" @click="addRule">添加题型规则</el-button>
        </div>
      </el-form-item>

      <el-form-item label="必考知识点">
        <el-select v-model="form.knowledgePoints" multiple placeholder="可选，不选则全范围抽题" style="width:100%">
          <el-option v-for="k in kps" :key="k.id" :label="k.name" :value="k.id" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="generating" @click="generate">生成试卷</el-button>
        <el-button @click="$router.back()">返回</el-button>
      </el-form-item>
    </el-form>

    <el-alert v-if="result" type="success" :closable="false" style="max-width:720px">
      <template #title>
        组卷成功！试卷「{{ result.name }}」，共 {{ result.questionCount }} 题，总分 {{ result.totalScore }} 分
      </template>
    </el-alert>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { smartPaper } from '../../api/exam'
import { getSubjects, getKnowledgePoints } from '../../api/question'

const subjects = ref<any[]>([])
const kps = ref<any[]>([])
const generating = ref(false)
const result = ref<any>(null)

const form = ref<any>({
  name: '',
  subjectId: undefined,
  difficulty: 2,
  typeRules: [
    { qtype: 'A', count: 20, score: 1, difficulty: undefined },
    { qtype: '判断', count: 10, score: 1, difficulty: undefined },
    { qtype: 'X', count: 5, score: 2, difficulty: undefined },
    { qtype: '填空', count: 5, score: 1, difficulty: undefined }
  ],
  knowledgePoints: []
})

function addRule() {
  form.value.typeRules.push({ qtype: 'A', count: 5, score: 1, difficulty: undefined })
}

async function onSubjectChange(v: number) {
  kps.value = (await getKnowledgePoints(v)) as any
}

async function generate() {
  if (!form.value.name) { ElMessage.warning('请输入试卷名称'); return }
  if (!form.value.typeRules.some((r: any) => r.count > 0)) {
    ElMessage.warning('请至少配置一个题目数量大于0的题型'); return
  }
  generating.value = true
  try {
    result.value = await smartPaper({
      name: form.value.name,
      subjectId: form.value.subjectId,
      difficulty: form.value.difficulty,
      typeRules: form.value.typeRules.filter((r: any) => r.count > 0),
      knowledgePoints: form.value.knowledgePoints
    }) as any
    ElMessage.success('组卷成功')
  } finally { generating.value = false }
}

onMounted(async () => {
  subjects.value = (await getSubjects()) as any
})
</script>

<style scoped>
.card-title { font-weight: 700; color: #9b3d2f; }
.rule-row { display: flex; gap: 10px; align-items: center; margin-bottom: 10px; }
</style>
