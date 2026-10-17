import request from '../utils/request'
import type { PracticeExamDetail, PracticeFeedback, PracticeSession, PracticeSessionRequest, TypeRule } from '../types'

export const startPracticeSession = (data: PracticeSessionRequest) =>
  request.post('/practice/session', data) as Promise<PracticeSession>

export const getPracticeSession = (sessionId: string) =>
  request.get(`/practice/session/${sessionId}`) as Promise<PracticeSession>

export const submitPracticeAnswer = (sessionId: string, questionId: number, answer: string) =>
  request.post(`/practice/session/${sessionId}/answer`, { questionId, answer }) as Promise<PracticeFeedback>

export const startPracticeExam = (data: { name?: string; subjectId?: number; difficulty?: number; typeRules: TypeRule[] }) =>
  request.post('/practice/exam', data) as Promise<PracticeExamDetail>
