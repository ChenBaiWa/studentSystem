import axios from 'axios'
import api from './http'

export interface StudentAssignment {
  id?: number
  assignmentId: number
  studentId?: number
  studentName?: string
  answer?: string
  score?: number
  feedback?: string
  status?: 'submitted' | 'graded'
  submitTime?: string
  createTime?: string
  updateTime?: string
}

// 获取所有学生作业
export const getAllStudentAssignments = () => {
  return api.get('/student-assignments').then(response => response.data)
}

// 根据ID获取学生作业
export const getStudentAssignmentById = (id: number) => {
  return api.get(`/student-assignments/${id}`).then(response => response.data)
}

// 提交学生作业
export const submitStudentAssignment = (assignmentId: number, answer: string) => {
  return api.post('/student-assignments/submit', { assignmentId, answer }).then(response => response.data)
}

// 创建学生作业
export const createStudentAssignment = (studentAssignment: any) => {
  return api.post('/student-assignments', studentAssignment).then(response => response.data)
}

// 更新学生作业
export const updateStudentAssignment = (id: number, studentAssignment: any) => {
  return api.put(`/student-assignments/${id}`, studentAssignment).then(response => response.data)
}

// 删除学生作业
export const deleteStudentAssignment = (id: number) => {
  return api.delete(`/student-assignments/${id}`).then(response => response.data)
}

// 获取作业的学生提交列表
export const getStudentSubmissions = (assignmentId: number) => {
  return api.get(`/assignments/${assignmentId}/submissions`).then(response => response.data)
}
