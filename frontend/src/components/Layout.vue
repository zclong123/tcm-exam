<template>
  <el-container class="layout">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <span class="logo-icon">🏥</span>
        <span class="logo-text">中医考试系统</span>
      </div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#2b2723"
        text-color="#d8cdb8"
        active-text-color="#e8a090"
        class="menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>

        <!-- 学生菜单 -->
        <template v-if="userStore.isStudent">
          <el-menu-item index="/student/exams">
            <el-icon><EditPen /></el-icon>
            <span>我的考试</span>
          </el-menu-item>
          <el-menu-item index="/student/practice">
            <el-icon><Notebook /></el-icon>
            <span>在线练习</span>
          </el-menu-item>
          <el-menu-item index="/student/scores">
            <el-icon><DataLine /></el-icon>
            <span>成绩查询</span>
          </el-menu-item>
          <el-menu-item index="/student/wrong">
            <el-icon><Document /></el-icon>
            <span>错题本</span>
          </el-menu-item>
        </template>

        <!-- 教师菜单 -->
        <template v-if="userStore.isTeacher || userStore.isAdmin">
          <el-menu-item index="/teacher/questions">
            <el-icon><Collection /></el-icon>
            <span>题库管理</span>
          </el-menu-item>
          <el-menu-item index="/teacher/paper">
            <el-icon><Files /></el-icon>
            <span>组卷管理</span>
          </el-menu-item>
          <el-menu-item index="/teacher/exams">
            <el-icon><AlarmClock /></el-icon>
            <span>考试管理</span>
          </el-menu-item>
          <el-menu-item index="/teacher/grading">
            <el-icon><Checked /></el-icon>
            <span>阅卷台</span>
          </el-menu-item>
          <el-menu-item index="/teacher/stats">
            <el-icon><TrendCharts /></el-icon>
            <span>成绩分析</span>
          </el-menu-item>
        </template>

        <!-- 管理员菜单 -->
        <template v-if="userStore.isAdmin">
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/org">
            <el-icon><OfficeBuilding /></el-icon>
            <span>院系班级</span>
          </el-menu-item>
          <el-menu-item index="/admin/overview">
            <el-icon><Odometer /></el-icon>
            <span>统计看板</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="page-title">{{ $route.meta.title || '' }}</div>
        <div class="header-right">
          <el-dropdown @command="onCommand">
            <span class="user-chip">
              <el-avatar :size="32" class="avatar">{{ (userStore.userInfo?.realName || '用')[0] }}</el-avatar>
              <span class="uname">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

function onCommand(cmd: string) {
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout { height: 100vh; }
.sidebar {
  background: #2b2723;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 18px 16px;
  border-bottom: 1px solid rgba(216,205,184,.15);
}
.logo-icon { font-size: 24px; }
.logo-text {
  color: #f4efe6;
  font-family: "Noto Serif SC", "Songti SC", serif;
  font-weight: 700;
  font-size: 17px;
}
.menu { border-right: none; flex: 1; overflow-y: auto; }
.menu .el-menu-item { height: 46px; line-height: 46px; }
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fbf8f3;
  border-bottom: 1px solid #e8e0d2;
}
.page-title {
  font-family: "Noto Serif SC", "Songti SC", serif;
  font-size: 18px;
  font-weight: 700;
  color: #9b3d2f;
}
.header-right { display: flex; align-items: center; }
.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
}
.user-chip:hover { background: #f0eadf; }
.avatar {
  background: #4a7c59;
  color: #fff;
}
.uname { font-size: 14px; color: #2b2723; }
.main { background: #f5f2ec; padding: 20px; overflow-y: auto; }
</style>
