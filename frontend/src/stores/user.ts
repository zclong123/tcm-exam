import { defineStore } from 'pinia'
import request from '../utils/request'
import type { LoginResponse, SysUser } from '../types'

interface UserState {
  token: string
  userInfo: SysUser | null
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null')
  }),
  getters: {
    isLoggedIn: (s) => !!s.token,
    role: (s) => s.userInfo?.roleCode || '',
    isAdmin: (s) => s.userInfo?.roleCode === 'ADMIN',
    isTeacher: (s) => s.userInfo?.roleCode === 'TEACHER',
    isStudent: (s) => s.userInfo?.roleCode === 'STUDENT'
  },
  actions: {
    async login(username: string, password: string) {
      const data = await request.post('/auth/login', { username, password }) as LoginResponse
      this.token = data.token
      localStorage.setItem('token', data.token)
      this.userInfo = {
        id: data.userId,
        username: data.username,
        realName: data.realName,
        roleCode: data.roleCode,
        clazzId: data.clazzId
      }
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
      return data
    },
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }
})
