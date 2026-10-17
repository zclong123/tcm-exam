import request from '../utils/request'
import type { PageResult } from '../types'

export const getMyScores = () => request.get('/stats/my-scores')

export const getClassStats = (examId?: number, clazzId?: number) =>
  request.get('/stats/class', { params: { examId, clazzId } })

export const getKnowledgeMastery = (examId?: number, clazzId?: number) =>
  request.get('/stats/knowledge-mastery', { params: { examId, clazzId } })

export const getQuestionStats = (subjectId?: number) =>
  request.get('/stats/question', { params: { subjectId } })

export const pageWrong = (params: { current?: number; size?: number; knowledgePointId?: number; mastered?: boolean }) =>
  request.get('/wrong/page', { params }) as Promise<PageResult<any>>

export const markMastered = (wrongId: number) =>
  request.post(`/wrong/${wrongId}/mastered`)

export const redoWrong = (wrongId: number) =>
  request.post(`/wrong/${wrongId}/redo`)

export const getUsers = (params: { current?: number; size?: number; keyword?: string; roleCode?: string; clazzId?: number }) =>
  request.get('/user/page', { params }) as Promise<PageResult<any>>

export const createUser = (data: any) => request.post('/user', data)
export const updateUser = (data: any) => request.put('/user', data)
export const deleteUser = (id: number) => request.delete(`/user/${id}`)
export const updateUserStatus = (id: number, status: number) =>
  request.put(`/user/${id}/status`, null, { params: { status } })

export const getDepartments = () => request.get('/org/departments')
export const getClazzes = (departmentId?: number) =>
  request.get('/org/clazzes', { params: { departmentId } })
