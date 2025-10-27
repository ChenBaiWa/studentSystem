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

// 获取所有班级
export const getAllClasses = (): Promise<any> => {
  return api.get('/classes').then(response => {
    if (response.data.success === false && response.data.message) {
      return Promise.reject(new Error(response.data.message))
    }
    return response
  })
}

// 根据ID获取班级
export const getClassById = (id: number) => {
  return api.get(`/classes/${id}`).then(response => response.data)
}

// 创建班级
export const createClass = (classData: any): Promise<any> => {
  return api.post('/classes', classData).then(response => {
    if (response.data.success === false && response.data.message) {
      return Promise.reject(new Error(response.data.message))
    }
    return response
  })
}

// 更新班级
export const updateClass = (id: number, classData: any): Promise<any> => {
  return api.put(`/classes/${id}`, classData).then(response => {
    if (response.data.success === false && response.data.message) {
      return Promise.reject(new Error(response.data.message))
    }
    return response
  })
}

// 删除班级
export const deleteClass = (id: number): Promise<any> => {
  return api.delete(`/classes/${id}`).then(response => {
    if (response.data.success === false && response.data.message) {
      return Promise.reject(new Error(response.data.message))
    }
    return response
  })
}

// 获取老师创建的班级列表
export const getTeacherClassList = (): Promise<any> => {
  return api.get('/classes').then(response => {
    if (response.data.success === false && response.data.message) {
      return Promise.reject(new Error(response.data.message))
    }
    return response
  })
}

// 获取学生加入的班级列表
export const getStudentClassList = (): Promise<any> => {
  return api.get('/classes/joined').then(response => {
    if (response.data.success === false && response.data.message) {
      return Promise.reject(new Error(response.data.message))
    }
    return response
  })
}

// 学生加入班级
export const joinClass = (joinData: any): Promise<any> => {
  return api.post('/classes/join', joinData).then(response => {
    if (response.data.success === false && response.data.message) {
      return Promise.reject(new Error(response.data.message))
    }
    return response
  })
}

// 获取班级学生列表
export const getClassStudents = (classId: number) => {
  return api.get(`/classes/${classId}/students`).then(response => response.data)
}