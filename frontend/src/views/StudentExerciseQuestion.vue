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
            @change="saveAnswerTemporarily"
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
            @blur="saveAnswerTemporarily"
          />
        </div>

        <!-- 主观题 -->
        <div v-else-if="currentQuestion.type === 'subjective'" class="subjective-question">
          <el-input 
            v-model="answers[currentQuestion.id]" 
            type="textarea" 
            :rows="5"
            placeholder="请输入答案"
            @blur="saveAnswerTemporarily"
          />
          
          <!-- 图片上传 -->
          <div class="image-upload-section">
            <p>上传解题过程图片:</p>
            <el-upload
              :auto-upload="false"
              list-type="picture-card"
              :file-list="imageFileLists[currentQuestion.id] || []"
              :on-change="(file, fileList) => handleImageChange(currentQuestion.id, file, fileList)"
              :on-remove="(file, fileList) => handleImageChange(currentQuestion.id, file, fileList)"
              :limit="3"
              :on-exceed="() => ElMessage.warning('最多只能上传3张图片')"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
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
import type { UploadFile, UploadUserFile } from 'element-plus'

// 数据状态
const exerciseSet = ref<any>(null)
const chapter = ref<any>(null)
const questions = ref<any[]>([])
const currentQuestionIndex = ref(0)
const answers = ref<Record<number, string>>({})
const imageFileLists = ref<Record<number, UploadUserFile[]>>({})
const loading = ref(false)
const submitting = ref(false)
const isDirectMode = ref(false) // 是否为直接模式（跳过章节）

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
  return typeMap[type] || type
}

// 获取题型标签类型
const getQuestionTypeTag = (type: string) => {
  const tagMap: Record<string, string> = {
    'choice': '',
    'fill': 'warning',
    'subjective': 'danger'
  }
  return tagMap[type] || ''
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
      saveAnswerTemporarily()
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

// 临时保存答案
const saveAnswerTemporarily = () => {
  // 在实际应用中，这里可以将答案保存到本地存储或发送到服务器
  console.log('临时保存答案:', answers.value)
  console.log('图片列表:', imageFileLists.value)
  ElMessage.success({ message: '答案已暂存', duration: 1000 })
}

// 上一题
const prevQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    // 切换题目前提示保存
    if (isCurrentQuestionAnswered()) {
      ElMessageBox.confirm('切换题目将自动保存当前答案，是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        saveAnswerTemporarily()
        currentQuestionIndex.value--
      }).catch(() => {
        // 用户取消切换
      })
    } else {
      currentQuestionIndex.value--
    }
  }
}

// 下一题
const nextQuestion = () => {
  if (currentQuestionIndex.value < questions.value.length - 1) {
    // 切换题目前提示保存
    if (isCurrentQuestionAnswered()) {
      ElMessageBox.confirm('切换题目将自动保存当前答案，是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        saveAnswerTemporarily()
        currentQuestionIndex.value++
      }).catch(() => {
        // 用户取消切换
      })
    } else {
      currentQuestionIndex.value++
    }
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
        answerType: question.type === 'subjective' ? 2 : 1 // 1=文本, 2=图片
      }
      
      if (question.type === 'subjective') {
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
      ElMessage.success('答案提交成功')
      // 跳转到习题集列表页
      router.push('/student/exercise-sets')
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
</style>