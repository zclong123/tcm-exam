import request from '../utils/request'
import type { LoginResponse, SysUser } from '../types'

export const login = (username: string, password: string) =>
  request.post('/auth/login', { username, password }) as Promise<LoginResponse>

export const register = (data: { username: string; password: string; realName?: string; clazzId?: number }) =>
  request.post('/auth/register', data)

export const getMe = () => request.get('/user/me') as Promise<SysUser>
