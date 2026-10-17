<template>
  <div class="dashboard">
    <el-card class="welcome-card">
      <h2>欢迎，{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</h2>
      <p>
        角色：
        {{ roleName }}
        <el-tag v-if="userStore.isStudent" type="success" size="small">学生</el-tag>
        <el-tag v-if="userStore.isTeacher" type="warning" size="small">教师</el-tag>
        <el-tag v-if="userStore.isAdmin" type="danger" size="small">管理员</el-tag>
      </p>
      <p class="sub">中医考试系统 · 覆盖 {{ questionTotal }}+ 道内置试题，九大题型</p>
    </el-card>

    <div class="cards">
      <el-card v-if="userStore.isStudent" class="feature-card">
        <template #header><span>📝 我的考试</span></template>
        <p>参加考试、查看成绩与解析</p>
        <el-button type="primary" size="small" @click="$router.push('/student/exams')">去考试</el-button>
      </el-card>
      <el-card v-if="userStore.isStudent" class="feature-card">
        <template #header><span>📖 在线练习</span></template>
        <p>按章节/随机练习，立即查看答案与解析</p>
        <el-button size="small" type="success" @click="$router.push('/student/practice')">去练习</el-button>
      </el-card>
      <el-card v-if="userStore.isStudent" class="feature-card">
        <template #header><span>📕 错题本</span></template>
        <p>自动收集错题，间隔重复复习</p>
        <el-button size="small" @click="$router.push('/student/wrong')">查看错题</el-button>
      </el-card>
      <el-card v-if="userStore.isTeacher" class="feature-card">
        <template #header><span>🗂 题库管理</span></template>
        <p>管理 10000+ 道中医试题，支持审核流转</p>
        <el-button type="primary" size="small" @click="$router.push('/teacher/questions')">管理题库</el-button>
      </el-card>
      <el-card v-if="userStore.isTeacher" class="feature-card">
        <template #header><span>🎲 智能组卷</span></template>
        <p>按题型/难度/知识点规则自动抽题</p>
        <el-button size="small" @click="$router.push('/teacher/paper')">去组卷</el-button>
      </el-card>
      <el-card v-if="userStore.isAdmin" class="feature-card">
        <template #header><span>👥 用户管理</span></template>
        <p>管理学生/教师/管理员账号</p>
        <el-button type="primary" size="small" @click="$router.push('/admin/users')">管理用户</el-button>
      </el-card>
      <el-card v-if="userStore.isAdmin" class="feature-card">
        <template #header><span>📊 统计看板</span></template>
        <p>全局数据统计与可视化</p>
        <el-button size="small" @click="$router.push('/admin/overview')">查看统计</el-button>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const questionTotal = '10000'

const roleName = computed(() => {
  if (userStore.isAdmin) return '管理员'
  if (userStore.isTeacher) return '教师'
  return '学生'
})
</script>

<style scoped>
.welcome-card { margin-bottom: 20px; }
.welcome-card h2 { color: #9b3d2f; font-family: "Noto Serif SC", serif; margin-bottom: 8px; }
.welcome-card .sub { color: #736b5e; font-size: 13px; margin-top: 8px; }
.cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 16px; }
.feature-card p { color: #736b5e; font-size: 13px; margin-bottom: 12px; min-height: 36px; }
</style>
