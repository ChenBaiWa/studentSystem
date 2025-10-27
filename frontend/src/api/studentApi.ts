import axios from 'axios'
import type { Student } from '@/types/Student'

const api = axios.create({
  baseURL: '/api',
  timeout: 1000000
})

// 添加请求拦截器
api.interceptors.request.use(
  (config) => {
    // 从 localStorage 或 sessionStorage 获取 token
    const token = localStorage.getItem('token') || sessionStorage.getItem('token')
    if (token) {
      // 在每个请求中添加 Authorization 头
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 添加响应拦截器
api.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    if (error.response?.status === 401) {
      // token 过期或无效，清除本地存储并跳转到登录页
      localStorage.removeItem('token')
      sessionStorage.removeItem('token')
      localStorage.removeItem('userRole')
      sessionStorage.removeItem('userRole')
      localStorage.removeItem('realName')
      sessionStorage.removeItem('realName')
      localStorage.removeItem('userId')
      sessionStorage.removeItem('userId')
      
      // 如果当前不在登录页，则跳转到登录页
      if (window.location.pathname !== '/login' && window.location.pathname !== '/register') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

// 获取所有学生
export const getAllStudents = (): Promise<Student[]> => {
  return api.get('/students').then(response => response.data)
}

// 根据ID获取学生
export const getStudentById = (id: number): Promise<Student> => {
  return api.get(`/students/${id}`).then(response => response.data)
}

// 创建学生
export const createStudent = (student: Student): Promise<Student> => {
  return api.post('/students', student).then(response => response.data)
}

// 更新学生
export const updateStudent = (id: number, student: Student): Promise<Student> => {
  return api.put(`/students/${id}`, student).then(response => response.data)
}

// 删除学生
export const deleteStudent = (id: number): Promise<void> => {
  return api.delete(`/students/${id}`)
}
