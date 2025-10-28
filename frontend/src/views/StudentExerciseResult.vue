<template>
  <div class="student-exercise-result">
    <el-card class="header-card">
      <div class="header-content">
        <h2>{{ result?.exerciseSetName }} - 批改结果</h2>
        <div class="result-summary">
          <span class="total-score">总分: {{ result?.totalScore }}分</span>
          <span class="submit-time">答题时间: {{ formatDate(result?.submitTime) }}</span>
        </div>
      </div>
    </el-card>

    <!-- 题目批改结果 -->
    <el-card class="result-card" v-loading="loading">
      <div v-if="result && result.questions">
        <div 
          v-for="(questionResult, index) in result.questions" 
          :key="questionResult.question.id"
          class="question-result-item"
        >
          <div class="question-header">
            <h3>
              {{ index + 1 }}. {{ questionResult.question.content }}
              <el-tag size="small" :type="getQuestionTypeTag(questionResult.question.type)">
                {{ getQuestionTypeText(questionResult.question.type) }}
              </el-tag>
              <el-tag size="small" type="success">{{ questionResult.question.score }}分</el-tag>
              <el-tag 
                size="small" 
                :type="questionResult.correctStatus === 1 ? 'success' : 'danger'"
              >
                {{ questionResult.correctStatus === 1 ? '正确' : '错误' }}
              </el-tag>
            </h3>
          </div>
          
          <div class="answer-section">
            <div class="student-answer">
              <p class="section-title">你的答案:</p>
              <div v-if="questionResult.question.type === 'choice'">
                <p>{{ getChoiceAnswerText(questionResult.studentAnswer) }}</p>
              </div>
              <div v-else-if="questionResult.question.type === 'subjective' && questionResult.question.answerType === 2">
                <el-image 
                  v-for="(url, imgIndex) in parseImageUrls(questionResult.studentAnswer)" 
                  :key="imgIndex"
                  :src="url" 
                  class="answer-image"
                  :preview-src-list="parseImageUrls(questionResult.studentAnswer)"
                  fit="cover"
                />
              </div>
              <div v-else>
                <p>{{ questionResult.studentAnswer || '未作答' }}</p>
              </div>
            </div>
            
            <div class="grading-result">
              <p class="section-title">批改结果:</p>
              <p>得分: {{ questionResult.score }}分</p>
              <p>评语: {{ questionResult.remark }}</p>
            </div>
          </div>
        </div>
      </div>
      
      <el-empty v-else-if="!loading" description="暂无批改结果" />
    </el-card>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button @click="goBack">返回习题集列表</el-button>
      <el-button type="primary" @click="retryExercise">重新做题</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { getGradingResults, getDirectGradingResults } from '@/api/exerciseSetApi'

// 数据状态
const result = ref<any>(null)
const loading = ref(false)

// 路由
const route = useRoute()
const router = useRouter()

// 初始化
onMounted(() => {
  loadGradingResults()
})

// 加载批改结果
const loadGradingResults = async () => {
  try {
    loading.value = true
    const exerciseSetId = Number(route.params.exerciseSetId)
    
    let response
    // 根据模式选择不同的获取结果方法
    if (route.path.includes('/direct')) {
      // 直接模式
      response = await getDirectGradingResults(exerciseSetId)
    } else {
      // 章节模式
      const chapterId = Number(route.params.chapterId)
      response = await getGradingResults(exerciseSetId, chapterId)
    }
    
    if (response.success) {
      result.value = response.data
    } else {
      ElMessage.error(response.message || '批改结果加载失败')
    }
  } catch (error: any) {
    console.error('加载批改结果失败:', error)
    ElMessage.error(error.message || '加载批改结果失败')
  } finally {
    loading.value = false
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

// 获取选择题答案文本
const getChoiceAnswerText = (answer: string) => {
  if (!answer) return '未作答'
  return `${answer}. ${String.fromCharCode(65 + answer.charCodeAt(0) - 'A'.charCodeAt(0))}`
}

// 解析图片URL
const parseImageUrls = (imageUrls: string) => {
  if (!imageUrls) return []
  try {
    return JSON.parse(imageUrls)
  } catch (e) {
    return []
  }
}

// 格式化时间
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`
}

// 返回习题集列表
const goBack = () => {
  router.push('/student/exercise-sets')
}

// 重新做题
const retryExercise = () => {
  const exerciseSetId = Number(route.params.exerciseSetId)
  
  if (route.path.includes('/direct')) {
    // 直接模式
    router.push(`/student/exercise-sets/${exerciseSetId}/direct`)
  } else {
    // 章节模式
    const chapterId = Number(route.params.chapterId)
    router.push(`/student/exercise-sets/${exerciseSetId}/chapters/${chapterId}`)
  }
}
</script>

<style scoped>
.student-exercise-result {
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

.result-summary {
  text-align: right;
}

.total-score {
  font-size: 18px;
  font-weight: bold;
  color: #e6a23c;
}

.submit-time {
  display: block;
  margin-top: 5px;
  font-size: 14px;
  color: #909399;
}

.result-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.question-result-item {
  border-bottom: 1px solid #eee;
  padding: 20px 0;
}

.question-result-item:last-child {
  border-bottom: none;
}

.question-header h3 {
  margin-bottom: 15px;
}

.section-title {
  font-weight: bold;
  margin-bottom: 5px;
}

.answer-section {
  display: flex;
  gap: 30px;
}

.student-answer, .grading-result {
  flex: 1;
}

.answer-image {
  width: 100px;
  height: 100px;
  margin-right: 10px;
  margin-bottom: 10px;
}

.action-buttons {
  text-align: center;
}

.action-buttons .el-button {
  margin: 0 10px;
}
</style>