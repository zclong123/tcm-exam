<template>
  <div class="login-page">
    <div class="login-card">
      <div class="brand">
        <div class="brand-icon">🏥</div>
        <h1>中医考试系统</h1>
        <p>中医院校教学考核 · 执业医师模拟 · 学生自学练习</p>
      </div>

      <el-tabs v-model="tab" class="login-tabs">
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" @keyup.enter="doLogin" label-position="top">
            <el-form-item label="用户名">
              <el-input v-model="loginForm.username" placeholder="请输入用户名" size="large" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" size="large" show-password />
            </el-form-item>
            <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="doLogin">
              登 录
            </el-button>
          </el-form>
          <div class="demo-tip">
            <p>演示账号（密码均为 123456）：</p>
            <p>admin / teacher / student1</p>
          </div>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form :model="regForm" label-position="top">
            <el-form-item label="用户名">
              <el-input v-model="regForm.username" placeholder="3-30位" size="large" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="regForm.password" type="password" placeholder="6-32位" size="large" show-password />
            </el-form-item>
            <el-form-item label="姓名">
              <el-input v-model="regForm.realName" placeholder="真实姓名" size="large" />
            </el-form-item>
            <el-form-item label="选择班级">
              <el-select v-model="regForm.clazzId" placeholder="请选择班级" size="large" style="width:100%">
                <el-option v-for="c in clazzes" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
            <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="doRegister">
              注 册
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'
import { register } from '../../api/auth'
import { getClazzes } from '../../api/stats'

const router = useRouter()
const userStore = useUserStore()

const tab = ref('login')
const loading = ref(false)
const clazzes = ref<any[]>([])

const loginForm = reactive({ username: '', password: '' })
const regForm = reactive({ username: '', password: '', realName: '', clazzId: undefined as number | undefined })

async function doLogin() {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const data = await userStore.login(loginForm.username, loginForm.password)
    ElMessage.success('登录成功')
    const role = data.roleCode
    if (role === 'ADMIN') router.push('/admin/overview')
    else if (role === 'TEACHER') router.push('/teacher/questions')
    else router.push('/student/exams')
  } finally {
    loading.value = false
  }
}

async function doRegister() {
  if (!regForm.username || !regForm.password) {
    ElMessage.warning('请填写用户名和密码')
    return
  }
  loading.value = true
  try {
    await register(regForm)
    ElMessage.success('注册成功，请登录')
    tab.value = 'login'
    loginForm.username = regForm.username
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    clazzes.value = (await getClazzes()) as any[]
  } catch { /* ignore */ }
})
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #2b2723 0%, #3d3530 60%, #4a7c59 160%);
}
.login-card {
  width: 420px;
  background: #fbf8f3;
  border-radius: 14px;
  padding: 36px 40px 28px;
  box-shadow: 0 20px 60px rgba(0,0,0,.35);
}
.brand { text-align: center; margin-bottom: 20px; }
.brand-icon { font-size: 44px; }
.brand h1 {
  font-family: "Noto Serif SC", "Songti SC", serif;
  color: #9b3d2f;
  font-size: 26px;
  margin: 8px 0 4px;
}
.brand p { color: #736b5e; font-size: 13px; }
.submit-btn { width: 100%; margin-top: 8px; }
.demo-tip {
  margin-top: 16px;
  padding: 10px 12px;
  background: #f4efe6;
  border-radius: 8px;
  font-size: 12px;
  color: #736b5e;
}
.demo-tip p { margin: 2px 0; }
</style>
