import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 60000 // 将超时时间从10秒增加到60秒
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

// 获取教师的习题集列表
export const getExerciseSets = () => {
  return api.get('/exercise-sets').then(response => response.data)
}

// 获取学生可做的习题集列表（已发布的）
export const getPublishedExerciseSets = () => {
  return api.get('/student/exercise-sets').then(response => response.data)
}

// 获取习题集详情
export const getExerciseSet = (id: number) => {
  return api.get(`/exercise-sets/${id}`).then(response => response.data)
}

// 获取学生端章节列表
export const getStudentChapters = (exerciseSetId: number) => {
  return api.get(`/student/exercise-sets/${exerciseSetId}/chapters`).then(response => response.data)
}

// 创建习题集
export const createExerciseSet = (exerciseSet: any) => {
  return api.post('/exercise-sets', exerciseSet).then(response => response.data)
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

// 上传PDF文件并进行AI解析
export const uploadAndParsePdf = (exerciseSetId: number, file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('exerciseSetId', exerciseSetId.toString())
  
  return api.post(`/upload`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 120000 // 为上传PDF单独设置120秒超时时间
  }).then(response => response.data)
}

// 获取习题集的题目列表（教师端）
export const getQuestions = (exerciseSetId: number) => {
  return api.get(`/exercise-sets/${exerciseSetId}/questions`).then(response => response.data)
}

// 获取习题集的题目列表（学生端 - 章节模式）
export const getStudentQuestions = (exerciseSetId: number, chapterId: number) => {
  return api.get(`/student/exercise-sets/${exerciseSetId}/chapters/${chapterId}/questions`).then(response => response.data)
}

// 获取习题集的题目列表（学生端 - 直接模式）
export const getStudentQuestionsDirect = (exerciseSetId: number) => {
  return api.get(`/student/exercise-sets/${exerciseSetId}/direct`).then(response => response.data)
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

// 批量保存题目
export const saveQuestions = (exerciseSetId: number, questions: any[]) => {
  return api.post(`/exercise-sets/${exerciseSetId}/questions/batch`, questions).then(response => response.data)
}

// 提交答题（章节模式）
export const submitStudentAnswers = (exerciseSetId: number, chapterId: number, answers: any[]) => {
  return api.post(`/student/exercise-sets/${exerciseSetId}/chapters/${chapterId}/answers`, answers).then(response => response.data)
}

// 提交答题（直接模式）
export const submitStudentAnswersDirect = (exerciseSetId: number, answers: any[]) => {
  return api.post(`/student/exercise-sets/${exerciseSetId}/direct/answers`, answers).then(response => response.data)
}

// 获取批改结果（章节模式）
export const getGradingResults = (exerciseSetId: number, chapterId: number) => {
  return api.get(`/student/exercise-sets/${exerciseSetId}/chapters/${chapterId}/results`).then(response => response.data)
}

// 获取批改结果（直接模式）
export const getDirectGradingResults = (exerciseSetId: number) => {
  return api.get(`/student/exercise-sets/${exerciseSetId}/direct/results`).then(response => response.data)
}