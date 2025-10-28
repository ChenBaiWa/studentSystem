<template>
  <div class="pdf-upload">
    <el-card class="upload-card">
      <template #header>
        <div class="card-header">
          <span>PDF上传与AI解析</span>
        </div>
      </template>
      
      <!-- 文件上传 -->
      <el-upload
        class="upload-demo"
        drag
        :auto-upload="false"
        :on-change="handleFileChange"
        :on-remove="handleFileRemove"
        :file-list="fileList"
        accept=".pdf"
      >
        <el-icon class="el-icon--upload">
          <UploadFilled />
        </el-icon>
        <div class="el-upload__text">
          将PDF文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            请上传PDF格式的习题文件，文件大小不超过10MB
          </div>
        </template>
      </el-upload>
      
      <!-- 操作按钮 -->
      <div class="button-group">
        <el-button 
          type="primary" 
          @click="handleUploadAndAnalyze" 
          :loading="uploading"
          :disabled="!selectedFile || uploading"
        >
          上传并AI解析
        </el-button>
        <el-button @click="resetForm">重置</el-button>
      </div>
      
      <!-- 解析结果展示 -->
      <div v-if="analysisResult" class="analysis-result">
        <el-divider>AI解析结果</el-divider>
        <el-input
          type="textarea"
          :rows="10"
          placeholder="AI解析结果"
          v-model="analysisResult"
          readonly
        />
        <div class="button-group">
          <el-button type="success" @click="useAnalysisResult">使用解析结果</el-button>
        </div>
      </div>
      
      <!-- 错误信息展示 -->
      <div v-if="error" class="error-message">
        <el-alert
          :title="error"
          type="error"
          show-icon
          closable
          @close="error = ''"
        />
      </div>
      
      <!-- 解析进度 -->
      <div v-if="uploading && !analysisResult" class="parsing-progress">
        <el-progress :percentage="progress" :show-text="true" />
        <p class="progress-text">AI正在解析中，请稍候...</p>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { uploadPdfFile } from '@/api/fileUploadApi'
import type { UploadFile, UploadFiles } from 'element-plus'

// 定义事件发射
const emit = defineEmits<{
  (e: 'analysisComplete', result: any[]): void
}>()

// 文件相关状态
const fileList = ref<UploadFiles>([])
const selectedFile = ref<File | null>(null)
const uploading = ref(false)
const progress = ref(0)

// 解析结果
const analysisResult = ref('')
const error = ref('')

// 处理文件选择
const handleFileChange = (file: UploadFile, files: UploadFiles) => {
  try {
    // 清除之前的错误信息
    error.value = ''
    
    // 只保留最后一个文件
    if (files.length > 1) {
      files.splice(0, files.length - 1)
    }
    
    // 检查文件类型
    if (file.raw) {
      if (file.raw.type !== 'application/pdf') {
        throw new Error('请上传PDF格式文件')
      }
      
      // 检查文件大小（10MB限制）
      if (file.raw.size > 10 * 1024 * 1024) {
        throw new Error('文件大小不能超过10MB')
      }
      
      selectedFile.value = file.raw
    }
  } catch (err: any) {
    error.value = err.message
    ElMessage.error(err.message)
    fileList.value = []
    selectedFile.value = null
  }
}

// 处理文件移除
const handleFileRemove = () => {
  selectedFile.value = null
  analysisResult.value = ''
  error.value = ''
  progress.value = 0
}

// 上传并AI解析
const handleUploadAndAnalyze = async () => {
  if (!selectedFile.value) {
    const errorMsg = '请先选择PDF文件'
    error.value = errorMsg
    ElMessage.warning(errorMsg)
    return
  }
  
  try {
    uploading.value = true
    error.value = ''
    progress.value = 0
    
    // 模拟进度更新
    const progressInterval = setInterval(() => {
      if (progress.value < 90) {
        progress.value += 10;
      }
    }, 500);
    
    // 上传PDF文件到服务器并获取解析结果
    const result = await uploadPdfFile(selectedFile.value)
    console.log('文件上传和解析成功，结果:', result)
    
    // 停止进度更新
    clearInterval(progressInterval);
    progress.value = 100;
    
    // 检查是否成功
    if (result.success) {
      // 将解析结果转换为字符串显示
      analysisResult.value = JSON.stringify(result.data, null, 2)
      
      // 自动处理解析结果
      setTimeout(() => {
        useAnalysisResult()
      }, 1000)
    } else {
      throw new Error(result.message || 'AI解析失败')
    }
  } catch (err: any) {
    console.error('上传并解析失败:', err)
    const errorMsg = err.message || '上传并解析失败'
    error.value = errorMsg
    ElMessage.error(errorMsg)
  } finally {
    uploading.value = false
  }
}

// 使用解析结果
const useAnalysisResult = () => {
  try {
    // 尝试解析存储的JSON结果
    const parsedResult = JSON.parse(analysisResult.value)
    
    // 将AI返回的数据转换为后端需要的格式
    const questions = convertAiDataToQuestions(parsedResult)
    
    // 发射事件，将结果传递给父组件
    emit('analysisComplete', questions)
    ElMessage.success('解析结果已保存到习题集')
  } catch (err) {
    console.error('解析结果处理失败:', err)
    ElMessage.error('解析结果处理失败: ' + (err instanceof Error ? err.message : String(err)))
  }
}

// 将AI返回的数据转换为后端需要的格式
const convertAiDataToQuestions = (aiData: any) => {
  // 检查aiData是否有效
  if (!aiData) {
    console.warn('AI数据为空')
    return []
  }
  
  const questions: any[] = []
  let sortOrder = 1
  
  // 处理选择题
  if (aiData.choice && Array.isArray(aiData.choice)) {
    aiData.choice.forEach((question: any) => {
      questions.push({
        ...question,
        type: 'choice',
        sortOrder: sortOrder++
      })
    })
  }
  
  // 处理填空题
  if (aiData.fill && Array.isArray(aiData.fill)) {
    aiData.fill.forEach((question: any) => {
      questions.push({
        ...question,
        type: 'fill',
        sortOrder: sortOrder++
      })
    })
  }
  
  // 处理主观题 (将solve统一改为subjective)
  if (aiData.subjective && Array.isArray(aiData.subjective)) {
    aiData.subjective.forEach((question: any) => {
      questions.push({
        ...question,
        type: 'subjective',
        sortOrder: sortOrder++
      })
    })
  }
  
  // 兼容旧的solve类型
  if (aiData.solve && Array.isArray(aiData.solve)) {
    aiData.solve.forEach((question: any) => {
      questions.push({
        ...question,
        type: 'subjective', // 统一转换为subjective
        sortOrder: sortOrder++
      })
    })
  }
  
  return questions
}

// 重置表单
const resetForm = () => {
  fileList.value = []
  selectedFile.value = null
  analysisResult.value = ''
  error.value = ''
  progress.value = 0
}
</script>

<style scoped>
.pdf-upload {
  padding: 20px;
}

.upload-card {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.button-group {
  margin-top: 20px;
  text-align: center;
}

.button-group .el-button {
  margin: 0 10px;
}

.analysis-result {
  margin-top: 30px;
}

.error-message {
  margin-top: 20px;
}

.parsing-progress {
  margin-top: 30px;
  text-align: center;
}

.progress-text {
  margin-top: 10px;
  color: #666;
}
</style>