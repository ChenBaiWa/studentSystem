<template>
  <div class="doubao-pdf-analysis">
    <el-card class="header-card">
      <div class="header-content">
        <h2>豆包AI PDF分析</h2>
      </div>
    </el-card>

    <!-- PDF上传和分析 -->
    <el-card class="analysis-card">
      <el-form :model="form" label-width="100px">
        <el-form-item label="PDF URL">
          <el-input v-model="form.pdfUrl" placeholder="请输入PDF文件URL" />
        </el-form-item>
        
        <el-form-item label="分析问题">
          <el-input 
            v-model="form.question" 
            type="textarea"
            :rows="3"
            placeholder="请输入要问AI的问题，例如：请总结这个PDF的主要内容" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="analyzePdf" :loading="loading">
            {{ loading ? '分析中...' : '开始分析' }}
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
      
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
import { parsePdfWithDoubao } from '@/api/doubaoApi'

// 表单数据
const form = reactive({
  pdfUrl: '',
  question: '请总结这个PDF文档的主要内容'
})

// 状态管理
const loading = ref(false)
const result = ref<string | null>(null)
const error = ref<string | null>(null)

// 分析PDF
const analyzePdf = async () => {
  if (!form.pdfUrl) {
    ElMessage.warning('请输入PDF文件URL')
    return
  }
  
  try {
    loading.value = true
    error.value = null
    
    const response = await parsePdfWithDoubao(form.pdfUrl, form.question)
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

// 重置表单
const resetForm = () => {
  form.pdfUrl = ''
  form.question = '请总结这个PDF文档的主要内容'
  result.value = null
  error.value = null
}
</script>

<style scoped>
.doubao-pdf-analysis {
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