import axios from 'axios'

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

// 获取作业列表
export const getAssignments = (params?: any) => {
  return api.get('/assignments', { params }).then(response => response.data)
}

// 获取作业详情
export const getAssignment = (id: number) => {
  return api.get(`/assignments/${id}`).then(response => response.data)
}

// 发布作业
export const publishAssignment = (assignment: any, classIds: number[]) => {
  // 将classIds数组作为请求体的一部分，而不是查询参数
  const data = {
    ...assignment,
    classIds: classIds
  };
  return api.post('/assignments/publish', data).then(response => {
    if (response.data.success === false && response.data.message) {
      return Promise.reject(new Error(response.data.message))
    }
    return response.data
  })
}

// 更新作业
export const updateAssignment = (id: number, assignment: any) => {
  return api.put(`/assignments/${id}`, assignment).then(response => response.data)
}

// 删除作业
export const deleteAssignment = (id: number) => {
  return api.delete(`/assignments/${id}`).then(response => response.data)
}

// 获取学生提交情况
export const getStudentSubmissions = (assignmentId: number) => {
  return api.get(`/assignments/${assignmentId}/submissions`).then(response => {
    // 确保返回正确的数据结构
    if (response.data.success === false && response.data.message) {
      return Promise.reject(new Error(response.data.message))
    }
    return response.data
  })
}