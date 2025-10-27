import axios from 'axios'
import type { Grade } from '@/types/Grade'

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

// 获取所有年级
export const getGrades = (): Promise<any> => {
  return api.get('/grades').then(response => {
    if (response.data.success === false && response.data.message) {
      return Promise.reject(new Error(response.data.message))
    }
    return response
  })
}

// 根据ID获取年级
export const getGradeById = (id: number): Promise<Grade> => {
  return api.get(`/grades/${id}`).then(response => response.data)
}

// 创建年级
export const createGrade = (grade: Grade): Promise<any> => {
  return api.post('/grades', grade).then(response => {
    if (response.data.success === false && response.data.message) {
      return Promise.reject(new Error(response.data.message))
    }
    return response
  })
}

// 更新年级
export const updateGrade = (id: number, grade: Grade): Promise<any> => {
  return api.put(`/grades/${id}`, grade).then(response => {
    if (response.data.success === false && response.data.message) {
      return Promise.reject(new Error(response.data.message))
    }
    return response
  })
}

// 删除年级
export const deleteGrade = (id: number): Promise<any> => {
  return api.delete(`/grades/${id}`).then(response => {
    if (response.data.success === false && response.data.message) {
      return Promise.reject(new Error(response.data.message))
    }
    return response
  })
}
