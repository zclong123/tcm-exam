// 全局类型定义
export interface Result<T = any> {
  code: number
  message: string
  data: T
}

export interface PageResult<T = any> {
  total: number
  current: number
  size: number
  records: T[]
}

export interface LoginResponse {
  token: string
  userId: number
  username: string
  realName: string
  roleCode: string
  clazzId?: number
}

export interface SysUser {
  id: number
  username: string
  realName: string
  email?: string
  phone?: string
  clazzId?: number
  roleCode: string
  status: number
}

export interface OptionItem {
  key: string
  text: string
  correct?: boolean
}

export interface AnswerItem {
  type: string
  text: string
}

export interface Question {
  id: number
  subjectId: number
  subjectName?: string
  knowledgePointId?: number
  knowledgePointName?: string
  qtype: string
  difficulty: number
  cognitiveLevel: string
  stem: string
  score: number
  status: number
  options?: OptionItem[]
  answers?: AnswerItem[]
  analysis?: string
}

export interface Paper {
  id: number
  name: string
  paperType: string
  totalScore: number
  questionCount: number
  subjectId?: number
  difficulty?: number
  status: number
}

export interface Exam {
  id: number
  name: string
  paperId: number
  clazzId?: number
  startTime: string
  endTime: string
  durationMin: number
  passScore: number
  shuffle: boolean
  antiCheat: boolean
  switchLimit: number
  status: number
}

export interface ExamRecord {
  id: number
  examId: number
  studentId: number
  startTime: string
  submitTime?: string
  totalScore?: number
  status: string
  switchCount: number
}

export interface ExamAnswer {
  id: number
  recordId: number
  questionId: number
  studentAnswer?: string
  isMarked: boolean
  isCorrect?: boolean
  score?: number
  reviewStatus: string
  reviewComment?: string
}

export interface ExamDetail {
  exam: Exam
  record: ExamRecord
  questions: Question[]
  answers: Record<string, ExamAnswer>
  durationMin: number
  shuffle: boolean
}

export interface TypeRule {
  qtype: string
  count: number
  score: number
  difficulty?: number
}

// ---- 在线练习 ----
export interface PracticeSession {
  sessionId: string
  mode: string
  count: number
  questions: Question[]
}

export interface PracticeFeedback {
  questionId: number
  correct: boolean | null
  correctAnswer: string
  analysis: string
  score?: number
}

export interface PracticeExamDetail {
  record: ExamRecord
  questions: Question[]
  durationMin: number
}

export interface PracticeSessionRequest {
  mode: string
  subjectId?: number
  knowledgePointId?: number
  qtype?: string
  count?: number
}
