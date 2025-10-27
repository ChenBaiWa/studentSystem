import axios from 'axios'

// 创建axios实例
const http = axios.create({
  baseURL: '/api',
  timeout: 1000000
})

// 请求拦截器：自动添加权限头
http.interceptors.request.use(
  (config) => {
    // 从localStorage或sessionStorage获取token
    const token = localStorage.getItem('token') || sessionStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器：处理权限错误
http.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    if (error.response?.status === 401) {
      // 权限错误，清除存储的token并跳转到登录页
      localStorage.removeItem('token')
      sessionStorage.removeItem('token')
      localStorage.removeItem('userRole')
      sessionStorage.removeItem('userRole')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default http
