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

// 获取习题集列表
export const getExerciseSets = () => {
  return api.get('/exercise-sets').then(response => response.data)
}

// 创建习题集
export const createExerciseSet = (exerciseSet: any) => {
  return api.post('/exercise-sets', exerciseSet).then(response => response.data)
}

// 获取习题集详情
export const getExerciseSet = (id: number) => {
  return api.get(`/exercise-sets/${id}`).then(response => response.data)
}

// 更新习题集
export const updateExerciseSet = (id: number, exerciseSet: any) => {
  return api.put(`/exercise-sets/${id}`, exerciseSet).then(response => response.data)
}

// 发布习题集
export const publishExerciseSet = (id: number) => {
  return api.post(`/exercise-sets/${id}/publish`).then(response => response.data)
}

// 取消发布习题集
export const unpublishExerciseSet = (id: number) => {
  return api.post(`/exercise-sets/${id}/unpublish`).then(response => response.data)
}

// 删除习题集
export const deleteExerciseSet = (id: number) => {
  return api.delete(`/exercise-sets/${id}`).then(response => response.data)
}

// 上传PDF文件
export const uploadPdf = (id: number, file: File) => {
  const formData = new FormData()
  formData.append('pdfFile', file)
  return api.post(`/exercise-sets/${id}/upload-pdf`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }).then(response => response.data)
}

// AI解析PDF
export const parsePdfWithAI = (id: number, pdfUrl: string) => {
  return api.post(`/ai/parse-pdf`, {
    setId: id,
    pdfUrl: pdfUrl
  }).then(response => response.data)
}

// 获取习题集的题目列表
export const getQuestions = (exerciseSetId: number) => {
  return api.get(`/exercise-sets/${exerciseSetId}/questions`).then(response => response.data)
}

// 添加题目
export const addQuestion = (exerciseSetId: number, question: any) => {
  return api.post(`/exercise-sets/${exerciseSetId}/questions`, question).then(response => response.data)
}

// 更新题目
export const updateQuestion = (exerciseSetId: number, id: number, question: any) => {
  return api.put(`/exercise-sets/${exerciseSetId}/questions/${id}`, question).then(response => response.data)
}

// 删除题目
export const deleteQuestion = (exerciseSetId: number, id: number) => {
  return api.delete(`/exercise-sets/${exerciseSetId}/questions/${id}`).then(response => response.data)
}

// 保存题目列表
export const saveQuestions = (setId: number, questionList: any[]) => {
  return api.post(`/exercise-set/save-questions`, {
    setId: setId,
    questionList: questionList
  }).then(response => response.data)
}