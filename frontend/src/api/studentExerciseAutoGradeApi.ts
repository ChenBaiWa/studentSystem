import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 60000
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

/**
 * 提交答案并开始自动批改
 * @param questionId 题目ID
 * @param answer 学生答案
 * @param answerType 答案类型：1=文本，2=图片
 * @param imageUrls 图片URL列表（JSON格式）
 * @returns 批改结果
 */
export const submitAnswer = (questionId: number, answer: string, answerType: number, imageUrls?: string) => {
  return api.post('/student/exercise-auto-grade/submit-answer', {
    questionId,
    answer,
    answerType,
    imageUrls
  }).then(response => response.data)
}

/**
 * 获取批改结果
 * @param questionId 题目ID
 * @returns 批改结果
 */
export const getGradingResult = (questionId: number) => {
  return api.get(`/student/exercise-auto-grade/result/${questionId}`).then(response => response.data)
}

/**
 * 获取习题集的完整批改结果
 * @param exerciseSetId 习题集ID
 * @returns 批改结果
 */
export const getExerciseSetGradingResults = (exerciseSetId: number) => {
  return api.get(`/student/exercise-auto-grade/results/${exerciseSetId}`).then(response => response.data)
}