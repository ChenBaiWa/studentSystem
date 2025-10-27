<template>
  <div class="doubao-file-analysis">
    <el-card class="header-card">
      <div class="header-content">
        <h2>豆包AI文件分析</h2>
      </div>
    </el-card>

    <!-- 文件上传和分析 -->
    <el-card class="analysis-card">
      <el-tabs v-model="activeTab">
        <!-- 文件上传标签页 -->
        <el-tab-pane label="文件上传" name="upload">
          <div class="upload-section">
            <el-upload
              class="upload-demo"
              drag
              :auto-upload="false"
              :show-file-list="true"
              :on-change="handleFileChange"
              :limit="1"
              accept=".pdf,.jpg,.jpeg,.png"
            >
              <el-icon class="el-icon--upload"><upload-filled /></el-icon>
              <div class="el-upload__text">
                将文件拖到此处，或<em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  支持PDF、JPG、PNG格式文件，且不超过10MB
                </div>
              </template>
            </el-upload>
            
            <div class="upload-actions">
              <el-button 
                type="primary" 
                @click="uploadFile" 
                :loading="uploading"
                :disabled="!selectedFile"
              >
                {{ uploading ? '上传中...' : '上传文件' }}
              </el-button>
              <el-button @click="resetUpload">重置</el-button>
            </div>
          </div>
        </el-tab-pane>
        
        <!-- URL分析标签页 -->
        <el-tab-pane label="URL分析" name="url">
          <el-form :model="urlForm" label-width="100px">
            <el-form-item label="文件URL">
              <el-input v-model="urlForm.fileUrl" placeholder="请输入文件URL（支持PDF、图片等）" />
            </el-form-item>
            
            <el-form-item label="分析问题">
              <el-input 
                v-model="urlForm.question" 
                type="textarea"
                :rows="3"
                placeholder="请输入要问AI的问题" />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="analyzeByUrl" :loading="analyzing">
                {{ analyzing ? '分析中...' : '开始分析' }}
              </el-button>
              <el-button @click="resetUrlForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      
      <!-- 上传结果 -->
      <div v-if="uploadResult" class="upload-result">
        <el-alert
          title="文件上传成功"
          type="success"
          description="文件已成功上传到豆包，您可以开始分析内容了"
          show-icon
        />
        <div class="file-info">
          <p><strong>文件ID:</strong> {{ uploadResult.documentId }}</p>
          <p><strong>文件名:</strong> {{ uploadResult.fileName }}</p>
        </div>
        <el-form :model="analysisForm" label-width="100px" class="analysis-form">
          <el-form-item label="分析问题">
            <el-input 
              v-model="analysisForm.question" 
              type="textarea"
              :rows="3"
              placeholder="请输入要问AI的问题" />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="analyzeUploadedFile" :loading="analyzing">
              {{ analyzing ? '分析中...' : '开始分析' }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 分析结果 -->
      <div v-if="analysisResult" class="result-section">
        <h3>分析结果</h3>
        <el-card class="result-card">
          <div class="result-content">{{ analysisResult }}</div>
        </el-card>
      </div>
      
      <!-- 错误信息 -->
      <div v-if="error" class="error-section">
        <el-alert :title="error" type="error" show-icon />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { uploadFileToDoubao, parseImageWithDoubao, parsePdfWithDoubao } from '@/api/doubaoApi'

// 标签页控制
const activeTab = ref('upload')

// 文件上传相关
const selectedFile = ref<File | null>(null)
const uploading = ref(false)
const uploadResult = ref<any>(null)

// URL分析相关
const urlForm = reactive({
  fileUrl: '',
  question: '请分析这个文件的主要内容'
})

// 分析相关
const analysisForm = reactive({
  question: '请分析这个文件的主要内容'
})

const analyzing = ref(false)
const analysisResult = ref<string | null>(null)
const error = ref<string | null>(null)

// 处理文件选择
const handleFileChange = (file: any) => {
  selectedFile.value = file.raw
  error.value = null
}

// 上传文件
const uploadFile = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请选择文件')
    return
  }
  
  // 检查文件大小（限制为10MB）
  if (selectedFile.value.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过10MB')
    return
  }
  
  try {
    uploading.value = true
    error.value = null
    
    const result = await uploadFileToDoubao(selectedFile.value)
    console.log('文件上传结果:', result)
    
    uploadResult.value = {
      documentId: result.documentId || 'unknown',
      fileName: selectedFile.value.name
    }
    
    ElMessage.success('文件上传成功')
  } catch (err: any) {
    console.error('上传失败:', err)
    error.value = err.message || '文件上传失败'
    ElMessage.error(error.value)
  } finally {
    uploading.value = false
  }
}

// 通过URL分析文件
const analyzeByUrl = async () => {
  if (!urlForm.fileUrl) {
    ElMessage.warning('请输入文件URL')
    return
  }
  
  try {
    analyzing.value = true
    error.value = null
    analysisResult.value = null
    
    let response
    // 根据文件类型调用不同的API
    if (urlForm.fileUrl.toLowerCase().endsWith('.pdf')) {
      response = await parsePdfWithDoubao(urlForm.fileUrl, urlForm.question)
    } else {
      response = await parseImageWithDoubao(urlForm.fileUrl, urlForm.question)
    }
    
    console.log('豆包API响应:', response)
    
    // 提取AI回答内容
    if (response.choices && response.choices.length > 0) {
      analysisResult.value = response.choices[0].message.content
    } else {
      analysisResult.value = '未获取到分析结果'
    }
  } catch (err: any) {
    console.error('分析失败:', err)
    error.value = err.message || '分析失败，请稍后重试'
    ElMessage.error(error.value)
  } finally {
    analyzing.value = false
  }
}

// 分析已上传的文件
const analyzeUploadedFile = async () => {
  if (!uploadResult.value) {
    ElMessage.warning('请先上传文件')
    return
  }
  
  try {
    analyzing.value = true
    error.value = null
    analysisResult.value = null
    
    // 这里需要根据实际的API进行调整
    // 假设我们可以通过documentId来获取文件并分析
    ElMessage.info('功能待完善：需要根据实际API调整分析已上传文件的逻辑')
    
    // 示例代码（需要根据实际API调整）
    /*
    const response = await analyzeDocumentById(uploadResult.value.documentId, analysisForm.question)
    if (response.choices && response.choices.length > 0) {
      analysisResult.value = response.choices[0].message.content
    } else {
      analysisResult.value = '未获取到分析结果'
    }
    */
  } catch (err: any) {
    console.error('分析失败:', err)
    error.value = err.message || '分析失败，请稍后重试'
    ElMessage.error(error.value)
  } finally {
    analyzing.value = false
  }
}

// 重置上传
const resetUpload = () => {
  selectedFile.value = null
  uploadResult.value = null
  error.value = null
}

// 重置URL表单
const resetUrlForm = () => {
  urlForm.fileUrl = ''
  urlForm.question = '请分析这个文件的主要内容'
  analysisResult.value = null
  error.value = null
}
</script>

<style scoped>
.doubao-file-analysis {
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

.analysis-card {
  margin-bottom: 20px;
}

.upload-section {
  text-align: center;
}

.upload-actions {
  margin-top: 20px;
}

.upload-result {
  margin-top: 20px;
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: #f5f5f5;
}

.file-info {
  margin: 15px 0;
  padding: 10px;
  background-color: white;
  border-radius: 4px;
}

.analysis-form {
  margin-top: 20px;
}

.result-section {
  margin-top: 20px;
}

.result-card {
  background-color: #f5f5f5;
}

.result-content {
  white-space: pre-wrap;
  line-height: 1.5;
}

.error-section {
  margin-top: 20px;
}
</style>