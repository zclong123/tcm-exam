import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/login/Login.vue')
    },
    {
      path: '/',
      component: () => import('../components/Layout.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('../views/dashboard/Dashboard.vue'),
          meta: { title: '首页', roles: ['STUDENT', 'TEACHER', 'ADMIN'] }
        },
        // ---- 学生端 ----
        {
          path: 'student/exams',
          name: 'StudentExams',
          component: () => import('../views/student/ExamList.vue'),
          meta: { title: '我的考试', roles: ['STUDENT'] }
        },
        {
          path: 'student/practice',
          name: 'PracticeHome',
          component: () => import('../views/student/practice/PracticeHome.vue'),
          meta: { title: '在线练习', roles: ['STUDENT'] }
        },
        {
          path: 'student/practice/play',
          name: 'PracticePlay',
          component: () => import('../views/student/practice/PracticePlay.vue'),
          meta: { title: '练习答题', roles: ['STUDENT'], noLayout: true }
        },
        {
          path: 'student/practice/exam-play',
          name: 'PracticeExamPlay',
          component: () => import('../views/student/practice/PracticeExamPlay.vue'),
          meta: { title: '随机组卷考试', roles: ['STUDENT'], noLayout: true }
        },
        {
          path: 'student/exam/:examId',
          name: 'TakeExam',
          component: () => import('../views/student/TakeExam.vue'),
          meta: { title: '在线答题', roles: ['STUDENT'], noLayout: true }
        },
        {
          path: 'student/scores',
          name: 'StudentScores',
          component: () => import('../views/student/ScoreList.vue'),
          meta: { title: '成绩查询', roles: ['STUDENT'] }
        },
        {
          path: 'student/wrong',
          name: 'WrongBook',
          component: () => import('../views/student/WrongBook.vue'),
          meta: { title: '错题本', roles: ['STUDENT'] }
        },
        // ---- 教师端 ----
        {
          path: 'teacher/questions',
          name: 'QuestionBank',
          component: () => import('../views/teacher/QuestionBank.vue'),
          meta: { title: '题库管理', roles: ['TEACHER', 'ADMIN'] }
        },
        {
          path: 'teacher/question/edit',
          name: 'QuestionEdit',
          component: () => import('../views/teacher/QuestionEdit.vue'),
          meta: { title: '编辑题目', roles: ['TEACHER', 'ADMIN'] }
        },
        {
          path: 'teacher/paper',
          name: 'PaperManage',
          component: () => import('../views/teacher/PaperManage.vue'),
          meta: { title: '组卷管理', roles: ['TEACHER', 'ADMIN'] }
        },
        {
          path: 'teacher/paper/smart',
          name: 'SmartPaper',
          component: () => import('../views/teacher/SmartPaper.vue'),
          meta: { title: '智能组卷', roles: ['TEACHER', 'ADMIN'] }
        },
        {
          path: 'teacher/exams',
          name: 'ExamManage',
          component: () => import('../views/teacher/ExamManage.vue'),
          meta: { title: '考试管理', roles: ['TEACHER', 'ADMIN'] }
        },
        {
          path: 'teacher/grading',
          name: 'Grading',
          component: () => import('../views/teacher/Grading.vue'),
          meta: { title: '阅卷台', roles: ['TEACHER', 'ADMIN'] }
        },
        {
          path: 'teacher/stats',
          name: 'TeacherStats',
          component: () => import('../views/teacher/StatsAnalysis.vue'),
          meta: { title: '成绩分析', roles: ['TEACHER', 'ADMIN'] }
        },
        // ---- 管理员端 ----
        {
          path: 'admin/users',
          name: 'UserManage',
          component: () => import('../views/admin/UserManage.vue'),
          meta: { title: '用户管理', roles: ['ADMIN'] }
        },
        {
          path: 'admin/org',
          name: 'OrgManage',
          component: () => import('../views/admin/OrgManage.vue'),
          meta: { title: '院系班级', roles: ['ADMIN'] }
        },
        {
          path: 'admin/overview',
          name: 'AdminOverview',
          component: () => import('../views/admin/Overview.vue'),
          meta: { title: '统计看板', roles: ['ADMIN'] }
        }
      ]
    },
    { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
  ]
})

router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()
  if (to.path === '/login') {
    return next()
  }
  if (!userStore.isLoggedIn) {
    return next('/login')
  }
  const roles = to.meta?.roles as string[] | undefined
  if (roles && !roles.includes(userStore.role)) {
    return next('/dashboard')
  }
  next()
})

export default router
