<template>
  <div class="student-exercise-question">
    <el-card class="header-card">
      <div class="header-content">
        <h2>{{ exerciseSet?.name }} - {{ chapter?.name || '直接练习' }}</h2>
        <div class="progress-info">
          <span>题目: {{ currentQuestionIndex + 1 }}/{{ questions.length }}</span>
          <span class="total-score">总分: {{ totalScore }}分</span>
        </div>
      </div>
    </el-card>

    <!-- 题目内容 -->
    <el-card class="question-card" v-loading="loading">
      <div v-if="currentQuestion">
        <div class="question-content">
          <h3>
            {{ currentQuestionIndex + 1 }}.
            {{ currentQuestion.content }}
            <el-tag size="small" :type="getQuestionTypeTag(currentQuestion.type)">
              {{ getQuestionTypeText(currentQuestion.type) }}
            </el-tag>
            <el-tag size="small" type="success">{{ currentQuestion.score }}分</el-tag>
          </h3>
        </div>

        <!-- 选择题 -->
        <div v-if="currentQuestion.type === 'choice'" class="choice-question">
          <el-radio-group
            v-model="answers[currentQuestion.id]"
            class="choice-options"
            @change="saveAnswerImmediately"
          >
            <el-radio
              v-for="(option, index) in parseOptions(currentQuestion.options)"
              :key="index"
              :label="String.fromCharCode(65 + index)"
              class="choice-option"
            >
              {{ String.fromCharCode(65 + index) }}. {{ option }}
            </el-radio>
          </el-radio-group>
        </div>

        <!-- 填空题 -->
        <div v-else-if="currentQuestion.type === 'fill'" class="fill-question">
          <el-input
            v-model="answers[currentQuestion.id]"
            type="textarea"
            :rows="3"
            placeholder="请输入答案"
            @blur="saveAnswerImmediately"
          />
        </div>

        <!-- 主观题 -->
        <div v-else-if="currentQuestion.type === 'subjective'" class="solve-question">
          <el-form-item label="答案">
            <!-- 主观题答案输入框 -->
            <el-input
              v-model="currentAnswer"
              type="textarea"
              :rows="6"
              placeholder="请输入答案"
              @blur="submitCurrentAnswer()"
            />
          </el-form-item>
          
          <div class="image-upload-section">
            <div class="upload-tip">
              <el-icon><InfoFilled /></el-icon>
              <span>主观题支持上传图片作答，上传后自动保存</span>
            </div>
            
            <ImageUpload 
              :on-success="handleImageUploadSuccess"
              :on-error="handleImageUploadError"
            />
          </div>
        </div>

      </div>

      <el-empty v-else-if="!loading" description="暂无题目" />
    </el-card>

    <!-- 答题导航 -->
    <div class="navigation-buttons">
      <el-button
        @click="prevQuestion"
        :disabled="currentQuestionIndex <= 0"
      >
        上一题
      </el-button>

      <el-button
        @click="nextQuestion"
        :disabled="currentQuestionIndex >= questions.length - 1"
      >
        下一题
      </el-button>

      <el-button
        type="primary"
        @click="submitAnswers"
        :loading="submitting"
      >
        提交答案
      </el-button>
    </div>

    <!-- 批改状态 -->
    <el-dialog
      v-model="gradingDialogVisible"
      title="正在批改"
      width="30%"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <div class="grading-status">
        <el-progress type="circle" :percentage="gradingProgress" />
        <p v-if="gradingProgress < 100">正在批改，请稍候...</p>
        <p v-else>批改完成！</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import {
  getExerciseSet,
  getStudentQuestions,
  submitStudentAnswers,
  getStudentQuestionsDirect,
  submitStudentAnswersDirect
} from '@/api/exerciseSetApi'
import { analyzeImageWithDoubao } from '@/api/doubaoApi'
import type { UploadFile, UploadUserFile } from 'element-plus'

// 数据状态
const exerciseSet = ref<any>(null)
const chapter = ref<any>(null)
const questions = ref<any[]>([])
const currentQuestionIndex = ref(0)
const answers = ref<Record<number, string>>({})
const imageFileLists = ref<Record<number, UploadUserFile[]>>({})
const aiAnalysisResults = ref<Record<number, string>>({}) // AI解析结果
const loading = ref(false)
const submitting = ref(false)
const isDirectMode = ref(false) // 是否为直接模式（跳过章节）
const gradingDialogVisible = ref(false) // 批改对话框可见性
const gradingProgress = ref(0) // 批改进度

// 路由
const route = useRoute()
const router = useRouter()

// 当前题目
const currentQuestion = computed(() => {
  return questions.value[currentQuestionIndex.value]
})

// 总分
const totalScore = computed(() => {
  return questions.value.reduce((sum, question) => sum + question.score, 0)
})

// 防抖定时器
const debounceTimers = ref<Record<number, number>>({})

// 初始化
onMounted(() => {
  const exerciseSetId = Number(route.params.exerciseSetId)

  if (!exerciseSetId || isNaN(exerciseSetId)) {
    ElMessage.error('参数错误')
    router.push('/student/exercise-sets')
    return
  }

  loadExerciseSet(exerciseSetId)

  // 判断是否为直接模式（路径中包含direct）
  if (route.path.includes('/direct')) {
    isDirectMode.value = true
    loadQuestionsDirect(exerciseSetId)
  } else {
    const chapterId = Number(route.params.chapterId)
    if (!chapterId || isNaN(chapterId)) {
      ElMessage.error('参数错误')
      router.push('/student/exercise-sets')
      return
    }
    loadChapter(exerciseSetId, chapterId)
    loadQuestions(exerciseSetId, chapterId)
  }
})

// 监听题目变化，初始化答案
watch(() => questions.value, (newQuestions) => {
  if (newQuestions.length > 0) {
    // 初始化答案
    newQuestions.forEach(question => {
      answers.value[question.id] = answers.value[question.id] || ''
      imageFileLists.value[question.id] = imageFileLists.value[question.id] || []
    })
  }
}, { immediate: true })

// 加载习题集信息
const loadExerciseSet = async (exerciseSetId: number) => {
  try {
    const response = await getExerciseSet(exerciseSetId)
    if (response.success) {
      exerciseSet.value = response.data
    } else {
      ElMessage.error(response.message || '习题集信息加载失败')
    }
  } catch (error: any) {
    console.error('加载习题集信息失败:', error)
    ElMessage.error(error.message || '加载习题集信息失败')
  }
}

// 加载章节信息
const loadChapter = async (exerciseSetId: number, chapterId: number) => {
  try {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token')
    const response = await fetch(`/api/student/exercise-sets/${exerciseSetId}/chapters`, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
    const result = await response.json()

    if (result.success) {
      const chapters = result.data
      chapter.value = chapters.find((c: any) => c.id === chapterId)
    } else {
      ElMessage.error(result.message || '章节信息加载失败')
    }
  } catch (error: any) {
    console.error('加载章节信息失败:', error)
    ElMessage.error(error.message || '加载章节信息失败')
  }
}

// 加载题目（章节模式）
const loadQuestions = async (exerciseSetId: number, chapterId: number) => {
  try {
    loading.value = true
    const response = await getStudentQuestions(exerciseSetId, chapterId)

    if (response.success) {
      questions.value = response.data
      // 初始化答案
      questions.value.forEach(question => {
        answers.value[question.id] = answers.value[question.id] || ''
        imageFileLists.value[question.id] = imageFileLists.value[question.id] || []
      })
    } else {
      ElMessage.error(response.message || '题目加载失败')
    }
  } catch (error: any) {
    console.error('加载题目失败:', error)
    ElMessage.error(error.message || '加载题目失败')
  } finally {
    loading.value = false
  }
}

// 加载题目（直接模式）
const loadQuestionsDirect = async (exerciseSetId: number) => {
  try {
    loading.value = true
    const response = await getStudentQuestionsDirect(exerciseSetId)

    if (response.success) {
      questions.value = response.data
      // 初始化答案
      questions.value.forEach(question => {
        answers.value[question.id] = answers.value[question.id] || ''
        imageFileLists.value[question.id] = imageFileLists.value[question.id] || []
      })
    } else {
      ElMessage.error(response.message || '题目加载失败')
    }
  } catch (error: any) {
    console.error('加载题目失败:', error)
    ElMessage.error(error.message || '加载题目失败')
  } finally {
    loading.value = false
  }
}

// 解析选择题选项
const parseOptions = (optionsStr: string) => {
  if (!optionsStr) return []
  try {
    return JSON.parse(optionsStr)
  } catch (e) {
    console.error('解析选项失败:', e)
    return []
  }
}

// 获取题型文本
const getQuestionTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    'choice': '选择题',
    'fill': '填空题',
    'subjective': '主观题'
  }
  return typeMap[type] || '未知题型'
}

// 获取题目类型标签
const getQuestionTypeTag = (type: string) => {
  const tagMap: Record<string, string> = {
    'choice': 'primary',
    'fill': 'success',
    'subjective': 'warning'
  }
  return tagMap[type] || 'info'
}

// 处理图片上传变化
const handleImageChange = async (questionId: number, file: UploadFile, fileList: UploadUserFile[]) => {
  // 更新文件列表
  imageFileLists.value[questionId] = fileList

  // 检查是否有新上传的文件
  if (file.status === 'ready') {
    // 模拟上传图片到服务器
    try {
      // 这里应该调用实际的上传API
      // 暂时模拟上传成功
      await new Promise(resolve => setTimeout(resolve, 1000))

      // 模拟返回图片URL
      const imageUrl = 'https://example.com/uploaded-image.jpg'

      // 更新文件对象
      const index = fileList.findIndex(f => f.uid === file.uid)
      if (index !== -1) {
        fileList[index].url = imageUrl
        fileList[index].status = 'success'
      }

      ElMessage.success('图片上传成功')

      // 自动保存答案
      saveAnswerImmediately()

      // 调用AI解析图片
      await analyzeImageWithAI(questionId, imageUrl)
    } catch (error) {
      ElMessage.error('图片上传失败')

      // 更新文件状态
      const index = fileList.findIndex(f => f.uid === file.uid)
      if (index !== -1) {
        fileList[index].status = 'fail'
      }
    }
  }

  // 更新文件列表
  imageFileLists.value[questionId] = [...fileList]
}

// 调用AI解析图片
const analyzeImageWithAI = async (questionId: number, imageUrl: string) => {
  try {
    const question = questions.value.find(q => q.id === questionId)
    if (!question) return

    const prompt = `请分析这张图片中的解题过程，题目是：${question.content}。请给出评价和建议。`
    const result = await analyzeImageWithDoubao(imageUrl, prompt)

    if (result.choices && result.choices.length > 0) {
      const content = result.choices[0].message?.content || 'AI分析完成'
      aiAnalysisResults.value[questionId] = content
      ElMessage.success('AI解析完成')
    }
  } catch (error: any) {
    console.error('AI解析失败:', error)
    ElMessage.error('AI解析失败: ' + (error.message || '未知错误'))
  }
}

// 立即保存答案
const saveAnswerImmediately = () => {
  // 在实际应用中，这里可以将答案保存到服务器
  console.log('立即保存答案:', answers.value)
  console.log('图片列表:', imageFileLists.value)
  ElMessage.success({ message: '答案已保存', duration: 1000 })
}

// 防抖保存答案
const saveAnswerDebounced = (value: string) => {
  const questionId = currentQuestion.value.id
  // 清除之前的定时器
  if (debounceTimers.value[questionId]) {
    clearTimeout(debounceTimers.value[questionId])
  }

  // 设置新的定时器
  debounceTimers.value[questionId] = window.setTimeout(() => {
    saveAnswerImmediately()
    delete debounceTimers.value[questionId]
  }, 1000) // 1秒防抖
}

// 上一题
const prevQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    // 直接保存当前答案
    saveAnswerImmediately()
    currentQuestionIndex.value--
  }
}

// 下一题
const nextQuestion = () => {
  if (currentQuestionIndex.value < questions.value.length - 1) {
    // 直接保存当前答案
    saveAnswerImmediately()
    currentQuestionIndex.value++
  }
}

// 检查当前题目是否已作答
const isCurrentQuestionAnswered = () => {
  if (!currentQuestion.value) return false

  const answer = answers.value[currentQuestion.value.id]
  const images = imageFileLists.value[currentQuestion.value.id]

  // 对于选择题和填空题，检查是否有答案
  if (currentQuestion.value.type === 'choice' || currentQuestion.value.type === 'fill') {
    return !!answer
  }

  // 对于主观题，检查是否有答案或图片
  return !!answer || (images && images.length > 0)
}

// 提交答案
const submitAnswers = async () => {
  try {
    await ElMessageBox.confirm('确定要提交答案吗？提交后不可修改。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    submitting.value = true

    // 构造提交数据
    const submitData = questions.value.map(question => {
      const answerData: any = {
        questionId: question.id,
        answerType: question.type === 'solve' ? 2 : 1 // 1=文本, 2=图片
      }

      if (question.type === 'solve') {
        // 主观题提交答案和图片
        answerData.answer = answers.value[question.id] || ''

        // 获取图片URL列表
        const imageUrls = imageFileLists.value[question.id]
          ?.filter(file => file.status === 'success')
          ?.map(file => file.url) || []
        answerData.imageUrls = JSON.stringify(imageUrls)
      } else {
        // 其他题型提交文本答案
        answerData.answer = answers.value[question.id] || ''
      }

      return answerData
    })

    // 显示批改对话框
    gradingDialogVisible.value = true
    gradingProgress.value = 0

    // 模拟批改进度
    const progressInterval = setInterval(() => {
      if (gradingProgress.value < 90) {
        gradingProgress.value += 10
      } else {
        clearInterval(progressInterval)
      }
    }, 300)

    let response;
    // 根据模式选择不同的提交方法
    if (isDirectMode.value) {
      // 直接模式提交
      response = await submitStudentAnswersDirect(
        Number(route.params.exerciseSetId),
        submitData
      )
    } else {
      // 章节模式提交
      response = await submitStudentAnswers(
        Number(route.params.exerciseSetId),
        Number(route.params.chapterId),
        submitData
      )
    }

    if (response.success) {
      // 完成批改
      clearInterval(progressInterval)
      gradingProgress.value = 100

      // 延迟一段时间后跳转到结果页面
      setTimeout(() => {
        gradingDialogVisible.value = false

        if (isDirectMode.value) {
          // 直接模式跳转到结果页面
          router.push(`/student/exercise-sets/${route.params.exerciseSetId}/direct/results`)
        } else {
          // 章节模式跳转到结果页面
          router.push(`/student/exercise-sets/${route.params.exerciseSetId}/chapters/${route.params.chapterId}/results`)
        }
      }, 1000)
    } else {
      throw new Error(response.message || '提交失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('提交失败:', error)
      ElMessage.error(error.message || '提交失败')
    }
  } finally {
    submitting.value = false
  }
}

// 提交当前题目答案
const submitCurrentAnswer = async (nextQuestionIndex?: number) => {
  if (!currentQuestion.value) return

  try {
    // 校验答案是否填写
    if (currentQuestion.value.type === 'choice' && !currentAnswer.value) {
      ElMessage.warning('请选择答案')
      return
    }
    
    if ((currentQuestion.value.type === 'fill' || currentQuestion.value.type === 'subjective') && 
        !currentAnswer.value.trim()) {
      ElMessage.warning('请填写答案')
      return
    }
    
    // 保存当前题目答案
    const answerData = {
      questionId: currentQuestion.value.id,
      answer: currentAnswer.value,
      answerType: currentQuestion.value.type === 'subjective' ? 2 : 1 // 1=文本, 2=图片
    }
    
    const response = await saveStudentAnswer(answerData)
    if (!response.success) {
      throw new Error(response.message || '保存答案失败')
    }
    
    // 更新本地答案记录
    const answerIndex = studentAnswers.value.findIndex(a => a.questionId === currentQuestion.value!.id)
    if (answerIndex >= 0) {
      studentAnswers.value[answerIndex] = response.data
    } else {
      studentAnswers.value.push(response.data)
    }
    
    // 如果有下一个题目索引，则跳转到该题目
    if (nextQuestionIndex !== undefined && nextQuestionIndex >= 0 && nextQuestionIndex < questions.value.length) {
      currentQuestionIndex.value = nextQuestionIndex
      loadCurrentAnswer()
    }
    
    ElMessage.success('答案保存成功')
  } catch (error: any) {
    console.error('保存答案失败:', error)
    ElMessage.error(error.message || '保存答案失败')
  }
}

// 处理图片上传成功
const handleImageUploadSuccess = (response: any) => {
  if (response.success) {
    // 直接保存图片答案
    currentAnswer.value = response.data.url
    // 图片上传成功后自动保存答案
    submitCurrentAnswer()
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

// 处理题目导航
const navigateToQuestion = async (index: number) => {
  if (index < 0 || index >= questions.value.length) return

  // 先保存当前答案
  if (currentQuestion.value) {
    await submitCurrentAnswer(index)
  } else {
    currentQuestionIndex.value = index
    loadCurrentAnswer()
  }
}

</script>

<style scoped>
.student-exercise-question {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.progress-info {
  font-size: 14px;
  color: #666;
}

.total-score {
  margin-left: 20px;
  font-weight: bold;
}

.question-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.question-content h3 {
  margin-bottom: 20px;
}

.choice-option {
  display: block;
  margin-bottom: 10px;
}

.solve-textarea {
  margin-bottom: 20px;
}

.navigation-buttons {
  margin-top: 30px;
  text-align: center;
}

.navigation-buttons .el-button {
  margin: 0 10px;
}

.image-upload-section {
  margin-top: 20px;
}

.image-upload-section p {
  margin-bottom: 10px;
  font-weight: bold;
}

.grading-status {
  text-align: center;
}

.grading-status p {
  margin-top: 20px;
  font-size: 16px;
}

.ai-analysis-result {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.ai-analysis-result h4 {
  margin-top: 0;
  margin-bottom: 10px;
}
</style>
