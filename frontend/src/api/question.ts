import request from '../utils/request'
import type { PageResult, Question } from '../types'

export interface QuestionQuery {
  subjectId?: number
  knowledgePointId?: number
  qtype?: string
  difficulty?: number
  stem?: string
  status?: number
  current?: number
  size?: number
}

export const pageQuestions = (params: QuestionQuery) =>
  request.get('/question/page', { params }) as Promise<PageResult<Question>>

export const getQuestion = (id: number) =>
  request.get(`/question/${id}`) as Promise<Question>

export const getQuestionsBatch = (ids: number[]) =>
  request.get('/question/batch', { params: { ids: ids.join(',') } }) as Promise<Question[]>

export const createQuestion = (data: Question) =>
  request.post('/question', data) as Promise<number>

export const updateQuestion = (data: Question) =>
  request.put('/question', data)

export const deleteQuestion = (id: number) =>
  request.delete(`/question/${id}`)

export const auditQuestion = (id: number, status: number, remark?: string) =>
  request.put(`/question/${id}/audit`, null, { params: { status, remark } })

export const getSubjects = () => request.get('/knowledge/subjects')

export const getKnowledgePoints = (subjectId?: number) =>
  request.get('/knowledge/points', { params: { subjectId } })
