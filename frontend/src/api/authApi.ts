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

export interface RegisterRequest {
  phone: string
  password: string
  realName: string
  userRole: number // 1:老师, 2:学生
}

export interface RegisterResponse {
  success: boolean
  message: string
  data?: {
    id: number
    phone: string
    realName: string
    userRole: number
  }
}

export interface LoginRequest {
  phone: string
  password: string
}

export interface LoginResponse {
  success: boolean
  message: string
  data?: {
    id: number
    phone: string
    realName: string
    userRole: number
    token: string
  }
}

export const authApi = {
  // 用户注册
  register: async (data: RegisterRequest): Promise<RegisterResponse> => {
    try {
      const response = await api.post('/auth/register', data)
      return response.data
    } catch (error: any) {
      if (error.response?.data) {
        return error.response.data
      }
      return {
        success: false,
        message: '网络错误，请稍后重试'
      }
    }
  },

  // 用户登录
  login: async (data: LoginRequest): Promise<LoginResponse> => {
    try {
      const response = await api.post('/auth/login', data)
      return response.data
    } catch (error: any) {
      if (error.response?.data) {
        return error.response.data
      }
      return {
        success: false,
        message: '网络错误，请稍后重试'
      }
    }
  },

  // 检查手机号是否已注册
  checkPhone: async (phone: string): Promise<boolean> => {
    try {
      const response = await api.get(`/auth/check-phone?phone=${phone}`)
      return response.data.exists
    } catch (error) {
      return false
    }
  },

  // 用户登出
  logout: () => {
    // 清除所有存储的认证信息
    localStorage.removeItem('token')
    sessionStorage.removeItem('token')
    localStorage.removeItem('userRole')
    sessionStorage.removeItem('userRole')
    localStorage.removeItem('userId')
    sessionStorage.removeItem('userId')
    localStorage.removeItem('realName')
    sessionStorage.removeItem('realName')
    
    // 注意：这里无法删除api实例的默认headers，但在新架构中这不是问题
    // 因为每次请求都会重新设置Authorization头
  },

  // 获取当前用户信息
  getCurrentUser: async () => {
    try {
      const response = await api.get('/auth/me')
      return response.data
    } catch (error) {
      return null
    }
  }
}
