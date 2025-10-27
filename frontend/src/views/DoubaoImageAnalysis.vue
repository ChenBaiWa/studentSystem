<template>
  <div class="doubao-analysis">
    <el-card class="header-card">
      <div class="header-content">
        <h2>豆包AI内容分析</h2>
      </div>
    </el-card>

    <!-- 文件上传和分析 -->
    <el-card class="analysis-card">
      <el-tabs v-model="activeTab">
        <!-- 图片分析标签页 -->
        <el-tab-pane label="图片分析" name="image">
          <el-form :model="imageForm" label-width="100px">
            <el-form-item label="图片URL">
              <el-input v-model="imageForm.imageUrl" placeholder="请输入图片URL" />
            </el-form-item>
            
            <el-form-item label="分析问题">
              <el-input 
                v-model="imageForm.question" 
                type="textarea"
                :rows="3"
                placeholder="请输入要问AI的问题" />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="analyzeImage" :loading="loading">
                {{ loading ? '分析中...' : '开始分析' }}
              </el-button>
              <el-button @click="resetImageForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <!-- PDF分析标签页 -->
        <el-tab-pane label="PDF分析" name="pdf">
          <el-form :model="pdfForm" label-width="100px">
            <el-form-item label="PDF URL">
              <el-input v-model="pdfForm.pdfUrl" placeholder="请输入PDF文件URL" />
            </el-form-item>
            
            <el-form-item label="分析问题">
              <el-input 
                v-model="pdfForm.question" 
                type="textarea"
                :rows="3"
                placeholder="请输入要问AI的问题，例如：请总结这个PDF的主要内容" />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="analyzePdf" :loading="loading">
                {{ loading ? '分析中...' : '开始分析' }}
              </el-button>
              <el-button @click="resetPdfForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      
      <!-- 结果展示 -->
      <div v-if="result" class="result-section">
        <h3>分析结果</h3>
        <el-card class="result-card">
          <div class="result-content">{{ result }}</div>
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
import { parseImageWithDoubao, parsePdfWithDoubao } from '@/api/doubaoApi'

// 标签页控制
const activeTab = ref('image')

// 图片表单数据
const imageForm = reactive({
  imageUrl: '',
  question: '图片主要讲了什么?'
})

// PDF表单数据
const pdfForm = reactive({
  pdfUrl: '',
  question: '请总结这个PDF文档的主要内容'
})

// 状态管理
const loading = ref(false)
const result = ref<string | null>(null)
const error = ref<string | null>(null)

// 分析图片
const analyzeImage = async () => {
  if (!imageForm.imageUrl) {
    ElMessage.warning('请输入图片URL')
    return
  }
  
  try {
    loading.value = true
    error.value = null
    result.value = null
    
    const response = await parseImageWithDoubao(imageForm.imageUrl, imageForm.question)
    console.log('豆包API响应:', response)
    
    // 提取AI回答内容
    if (response.choices && response.choices.length > 0) {
      result.value = response.choices[0].message.content
    } else {
      result.value = '未获取到分析结果'
    }
  } catch (err: any) {
    console.error('分析失败:', err)
    error.value = err.message || '分析失败，请稍后重试'
    ElMessage.error(error.value)
  } finally {
    loading.value = false
  }
}

// 分析PDF
const analyzePdf = async () => {
  if (!pdfForm.pdfUrl) {
    ElMessage.warning('请输入PDF文件URL')
    return
  }
  
  try {
    loading.value = true
    error.value = null
    result.value = null
    
    const response = await parsePdfWithDoubao(pdfForm.pdfUrl, pdfForm.question)
    console.log('豆包API响应:', response)
    
    // 提取AI回答内容
    if (response.choices && response.choices.length > 0) {
      result.value = response.choices[0].message.content
    } else {
      result.value = '未获取到分析结果'
    }
  } catch (err: any) {
    console.error('分析失败:', err)
    error.value = err.message || '分析失败，请稍后重试'
    ElMessage.error(error.value)
  } finally {
    loading.value = false
  }
}

// 重置图片表单
const resetImageForm = () => {
  imageForm.imageUrl = ''
  imageForm.question = '图片主要讲了什么?'
  result.value = null
  error.value = null
}

// 重置PDF表单
const resetPdfForm = () => {
  pdfForm.pdfUrl = ''
  pdfForm.question = '请总结这个PDF文档的主要内容'
  result.value = null
  error.value = null
}
</script>

<style scoped>
.doubao-analysis {
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