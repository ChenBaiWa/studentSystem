import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
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