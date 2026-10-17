import request from '../utils/request'
import type { Exam, ExamDetail, ExamRecord, PageResult, TypeRule } from '../types'

// ---- 学生 ----
export const getMyExams = () => request.get('/exam/mine') as Promise<Exam[]>

export const startExam = (examId: number) =>
  request.post(`/exam/start/${examId}`) as Promise<ExamDetail>

export const saveAnswers = (recordId: number, answers: any[]) =>
  request.post(`/exam/record/${recordId}/save`, { answers })

export const submitExam = (recordId: number) =>
  request.post(`/exam/record/${recordId}/submit`)

export const reportSwitch = (recordId: number) =>
  request.post(`/exam/record/${recordId}/switch`)

export const getRecord = (recordId: number) =>
  request.get(`/exam/record/${recordId}`) as Promise<ExamRecord>

export const autoGrade = (recordId: number) =>
  request.post(`/grading/auto/${recordId}`)

// ---- 教师/管理员 ----
export const createExam = (data: Partial<Exam>) => request.post('/exam', data)

export const updateExam = (data: Partial<Exam>) => request.put('/exam', data)

export const deleteExam = (id: number) => request.delete(`/exam/${id}`)

export const pageExams = (params: { current?: number; size?: number; name?: string; clazzId?: number }) =>
  request.get('/exam/page', { params }) as Promise<PageResult<Exam>>

// ---- 组卷 ----
export const smartPaper = (data: { name: string; subjectId?: number; difficulty?: number; typeRules: TypeRule[]; knowledgePoints?: number[] }) =>
  request.post('/paper/smart', data)

export const manualPaper = (name: string, subjectId: number | undefined, questionIds: number[]) =>
  request.post('/paper/manual', questionIds, { params: { name, subjectId } })

export const pagePapers = (params: { current?: number; size?: number; name?: string }) =>
  request.get('/paper/page', { params }) as Promise<PageResult<any>>

export const getPaperQuestions = (paperId: number) =>
  request.get(`/paper/${paperId}/questions`)

export const deletePaper = (id: number) => request.delete(`/paper/${id}`)

// ---- 阅卷 ----
export const reviewList = (params: { current?: number; size?: number; examId?: number; recordId?: number }) =>
  request.get('/grading/review-list', { params }) as Promise<PageResult<any>>

export const review = (answerId: number, score: number, comment?: string) =>
  request.post('/grading/review', null, { params: { answerId, score, comment } })
