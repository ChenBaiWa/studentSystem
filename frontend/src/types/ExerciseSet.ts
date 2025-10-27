export interface ExerciseSet {
  id?: number
  name: string
  subject: string
  creatorId?: number
  creatorName?: string
  status: 'editing' | 'published'
  questionCount?: number
  createTime?: string
  updateTime?: string
}

export interface Question {
  id?: number
  exerciseSetId?: number
  type: 'choice' | 'fill' | 'subjective'
  content: string
  options?: string
  answer: string
  score: number
  sortOrder?: number
  createTime?: string
  updateTime?: string
}