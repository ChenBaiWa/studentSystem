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
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { parsePdfToQuestions } from '@/api/doubaoApi'
import type { UploadFile, UploadFiles } from 'element-plus'

// 定义事件发射
const emit = defineEmits<{
  (e: 'analysisComplete', result: any): void
}>()

// 文件相关状态
const fileList = ref<UploadFiles>([])
const selectedFile = ref<File | null>(null)
const uploading = ref(false)

// 解析结果
const analysisResult = ref('')

// 处理文件选择
const handleFileChange = (file: UploadFile, files: UploadFiles) => {
  // 只保留最后一个文件
  if (files.length > 1) {
    files.splice(0, files.length - 1)
  }
  
  // 检查文件类型
  if (file.raw) {
    if (file.raw.type !== 'application/pdf') {
      ElMessage.error('请上传PDF格式文件')
      fileList.value = []
      selectedFile.value = null
      return
    }
    
    // 检查文件大小（10MB限制）
    if (file.raw.size > 10 * 1024 * 1024) {
      ElMessage.error('文件大小不能超过10MB')
      fileList.value = []
      selectedFile.value = null
      return
    }
    
    selectedFile.value = file.raw
  }
}

// 处理文件移除
const handleFileRemove = () => {
  selectedFile.value = null
  analysisResult.value = ''
}

// 上传并AI解析
const handleUploadAndAnalyze = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择PDF文件')
    return
  }
  
  try {
    uploading.value = true
    
    // 使用新的API方法解析PDF
    const result = await parsePdfToQuestions(selectedFile.value)
    
    if (result.success) {
      // 将解析结果转换为字符串显示
      analysisResult.value = JSON.stringify(result.questions, null, 2)
      ElMessage.success('AI解析完成')
    } else {
      throw new Error('AI解析失败')
    }
  } catch (error: any) {
    console.error('上传并解析失败:', error)
    ElMessage.error(error.message || '上传并解析失败')
  } finally {
    uploading.value = false
  }
}

// 使用解析结果
const useAnalysisResult = () => {
  // 这里可以将解析结果传递给父组件或跳转到题目编辑页面
  ElMessage.success('解析结果已保存')
  
  try {
    // 尝试解析存储的JSON结果
    const parsedResult = JSON.parse(analysisResult.value)
    // 发射事件，将结果传递给父组件
    emit('analysisComplete', parsedResult)
  } catch (error) {
    // 如果解析失败，发送原始字符串
    emit('analysisComplete', analysisResult.value)
  }
}

// 重置表单
const resetForm = () => {
  fileList.value = []
  selectedFile.value = null
  analysisResult.value = ''
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
</style>