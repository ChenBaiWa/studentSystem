export interface ExerciseSet {
  id?: number
  name: string // 习题集名称
  subject: string // 学科
  creatorId?: number // 创建人ID(老师ID)
  creatorName?: string // 创建人姓名
  status?: 'editing' | 'published' // 状态: editing(编辑中) / published(已发布)
  questionCount?: number // 题目数量
  createTime?: string // 创建时间
  updateTime?: string // 更新时间
}

export interface Question {
  id?: number
  exerciseSetId?: number // 习题集ID
  type: 'choice' | 'fill' | 'subjective' // 题型: choice(选择题) / fill(填空题) / subjective(主观题)
  content: string // 题干内容
  options?: string // 选项（JSON格式存储，适用于选择题）
  answer?: string // 标准答案
  score?: number // 分值
  sortOrder?: number // 排序
  createTime?: string // 创建时间
  updateTime?: string // 更新时间
}