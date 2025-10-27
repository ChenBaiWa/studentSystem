<template>
  <div class="question-management">
    <el-card class="header-card">
      <div class="header-content">
        <div>
          <h2>题目管理 - {{ exerciseSet?.name }}</h2>
          <p>学科：{{ exerciseSet?.subject }} | 题目数：{{ questions.length }}</p>
        </div>
        <div>
          <el-button type="primary" @click="handleAddQuestion">添加题目</el-button>
          <el-button 
            type="success" 
            @click="handlePublish" 
            :disabled="!exerciseSet || exerciseSet.status === 'published' || questions.length === 0"
            v-if="exerciseSet && exerciseSet.status !== 'published'"
          >
            发布习题集
          </el-button>
          <el-button 
            @click="handleUnpublish" 
            v-if="exerciseSet && exerciseSet.status === 'published'"
          >
            取消发布
          </el-button>
          <el-button @click="handleBack">返回</el-button>
        </div>
      </div>
    </el-card>

    <!-- PDF上传解析区域 -->
    <el-card class="upload-card" v-if="exerciseSet && exerciseSet.status !== 'published'">
      <template #header>
        <div class="upload-header">
          <span>上传PDF解析</span>
        </div>
      </template>
      
      <!-- 上传区域 -->
      <div v-if="!isParsing && !parsedQuestions.length">
        <el-upload
          class="upload-demo"
          drag
          :auto-upload="false"
          :on-change="handleFileChange"
          :on-remove="handleFileRemove"
          :file-list="fileList"
          accept=".pdf"
          :disabled="uploading"
        >
          <el-icon class="el-icon--upload">
            <UploadFilled />
          </el-icon>
          <div class="el-upload__text">
            将PDF文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持纯文字PDF，文件大小不超过20MB
            </div>
          </template>
        </el-upload>
        
        <div class="upload-button-group">
          <el-button 
            type="primary" 
            @click="handleUploadAndParse" 
            :loading="uploading"
            :disabled="!selectedFile || uploading"
          >
            上传并AI解析
          </el-button>
        </div>
      </div>
      
      <!-- 解析中状态 -->
      <div v-if="isParsing" class="parsing-status">
        <el-progress :percentage="parseProgress" :show-text="true" />
        <p class="parsing-text">AI解析中，约需30秒...</p>
      </div>
      
      <!-- 解析失败 -->
      <div v-if="parseFailed" class="parse-failed">
        <el-alert
          title="解析异常，可手动添加题目"
          type="error"
          :description="parseErrorMessage || 'AI解析失败，请手动添加题目'"
          show-icon
        />
        <div class="upload-button-group">
          <el-button type="primary" @click="resetUpload">重新上传</el-button>
        </div>
      </div>
    </el-card>

    <!-- 解析结果展示 -->
    <el-card class="parsed-questions-card" v-if="parsedQuestions.length > 0 && exerciseSet && exerciseSet.status !== 'published'">
      <template #header>
        <div class="parsed-header">
          <span>AI解析结果</span>
          <div>
            <el-button type="primary" @click="saveParsedQuestions" :loading="savingParsed">保存题目</el-button>
            <el-button @click="resetParsedQuestions">重新上传</el-button>
          </div>
        </div>
      </template>
      
      <!-- 按题型分组展示 -->
      <div class="question-groups">
        <!-- 选择题 -->
        <div v-if="choiceQuestions.length > 0" class="question-group">
          <h3>选择题 ({{ choiceQuestions.length }}题)</h3>
          <div v-for="(question, index) in choiceQuestions" :key="index" class="question-item">
            <el-form label-width="80px" class="question-form">
              <el-form-item label="题干">
                <el-input 
                  type="textarea" 
                  :rows="3" 
                  v-model="question.content"
                  placeholder="请输入题干内容"
                />
              </el-form-item>
                
                <el-form-item label="选项">
                  <div v-for="(option, optIndex) in question.options" :key="optIndex" class="option-item">
                    <el-input 
                      v-model="option.content" 
                      :placeholder="`选项${String.fromCharCode(65 + optIndex)}`"
                      style="width: 90%"
                    />
                    <el-button link type="danger" @click="removeOption(question, optIndex)" style="width: 10%">删除</el-button>
                  </div>
                  <el-button @click="addOption(question)" style="margin-top: 10px;">添加选项</el-button>
                </el-form-item>
                
                <el-form-item label="答案">
                  <el-select v-model="question.answer" placeholder="请选择正确答案" style="width: 100%">
                    <el-option
                      v-for="(option, optIndex) in question.options"
                      :key="optIndex"
                      :label="`${String.fromCharCode(65 + optIndex)}. ${option.content}`"
                      :value="String.fromCharCode(65 + optIndex)"
                    />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="分值" required>
                  <el-input-number 
                    v-model="question.score" 
                    :min="1" 
                    :max="10" 
                    controls-position="right"
                  />
                  <div v-if="!question.score || question.score < 1 || question.score > 10" class="score-error">分值需在1-10之间</div>
                </el-form-item>
              </el-form>
            </div>
          </div>
          
          <!-- 填空题 -->
          <div v-if="fillQuestions.length > 0" class="question-group">
            <h3>填空题 ({{ fillQuestions.length }}题)</h3>
            <div v-for="(question, index) in fillQuestions" :key="index" class="question-item">
              <el-form label-width="80px" class="question-form">
                <el-form-item label="题干">
                  <el-input 
                    type="textarea" 
                    :rows="3" 
                    v-model="question.content"
                    placeholder="请输入题干内容"
                  />
                </el-form-item>
                
                <el-form-item label="答案">
                  <el-input 
                    type="textarea" 
                    :rows="2" 
                    v-model="question.answer"
                    placeholder="请输入标准答案"
                  />
                </el-form-item>
                
                <el-form-item label="分值" required>
                  <el-input-number 
                    v-model="question.score" 
                    :min="1" 
                    :max="10" 
                    controls-position="right"
                  />
                  <div v-if="!question.score || question.score < 1 || question.score > 10" class="score-error">分值需在1-10之间</div>
                </el-form-item>
              </el-form>
            </div>
          </div>
          
          <!-- 主观题 -->
          <div v-if="subjectiveQuestions.length > 0" class="question-group">
            <h3>主观题 ({{ subjectiveQuestions.length }}题)</h3>
            <div v-for="(question, index) in subjectiveQuestions" :key="index" class="question-item">
              <el-form label-width="80px" class="question-form">
                <el-form-item label="题干">
                  <el-input 
                    type="textarea" 
                    :rows="3" 
                    v-model="question.content"
                    placeholder="请输入题干内容"
                  />
                </el-form-item>
                
                <el-form-item label="评分要点">
                  <el-input 
                    type="textarea" 
                    :rows="3" 
                    v-model="question.answer"
                    placeholder="请输入评分要点"
                  />
                </el-form-item>
                
                <el-form-item label="分值" required>
                  <el-input-number 
                    v-model="question.score" 
                    :min="1" 
                    :max="10" 
                    controls-position="right"
                  />
                  <div v-if="!question.score || question.score < 1 || question.score > 10" class="score-error">分值需在1-10之间</div>
                </el-form-item>
              </el-form>
            </div>
          </div>
        </div>
      </el-card>

    <!-- 题目列表 -->
    <el-card class="list-card">
      <el-table :data="questions" style="width: 100%" v-loading="loading">
        <el-table-column prop="sortOrder" label="#" width="60" />
        <el-table-column prop="type" label="题型" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.type === 'choice'">选择题</el-tag>
            <el-tag v-else-if="scope.row.type === 'fill'">填空题</el-tag>
            <el-tag v-else-if="scope.row.type === 'subjective'">主观题</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="题干" min-width="300">
          <template #default="scope">
            <div class="question-content">{{ scope.row.content }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分值" width="80" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleEditQuestion(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDeleteQuestion(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && questions.length === 0" description="暂无题目数据" />
    </el-card>

    <!-- 添加/编辑题目弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditQuestion ? '编辑题目' : '添加题目'"
      width="800px"
      :before-close="handleDialogClose"
    >
      <el-form
        ref="questionFormRef"
        :model="questionForm"
        :rules="questionRules"
        label-width="80px"
      >
        <el-form-item label="题型" prop="type">
          <el-select
            v-model="questionForm.type"
            placeholder="请选择题型"
            style="width: 100%"
            @change="handleQuestionTypeChange"
          >
            <el-option label="选择题" value="choice" />
            <el-option label="填空题" value="fill" />
            <el-option label="主观题" value="subjective" />
          </el-select>
        </el-form-item>

        <el-form-item label="题干" prop="content">
          <el-input
            v-model="questionForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入题干内容"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <!-- 选择题选项 -->
        <div v-if="questionForm.type === 'choice'">
          <el-form-item label="选项">
            <el-row v-for="(option, index) in choiceOptions" :key="index" style="margin-bottom: 10px;">
              <el-col :span="2">
                <span>{{ getOptionLabel(index) }}.</span>
              </el-col>
              <el-col :span="20">
                <el-input v-model="option.content" placeholder="请输入选项内容" />
              </el-col>
              <el-col :span="2">
                <el-button link @click="removeChoiceOption(index)" type="danger">删除</el-button>
              </el-col>
            </el-row>
            <el-button @click="addChoiceOption" style="margin-top: 10px;">添加选项</el-button>
          </el-form-item>
        </div>

        <el-form-item label="答案" prop="answer">
          <el-input
            v-if="questionForm.type === 'fill' || questionForm.type === 'subjective'"
            v-model="questionForm.answer"
            type="textarea"
            :rows="3"
            placeholder="请输入标准答案"
            maxlength="500"
            show-word-limit
          />
          <el-select
            v-else-if="questionForm.type === 'choice'"
            v-model="questionForm.answer"
            placeholder="请选择正确答案"
            style="width: 100%"
          >
            <el-option
              v-for="(option, index) in choiceOptions"
              :key="index"
              :label="`${getOptionLabel(index)}. ${option.content}`"
              :value="getOptionLabel(index)"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="分值" prop="score">
          <el-input-number
            v-model="questionForm.score"
            :min="1"
            :max="10"
            controls-position="right"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitQuestion" :loading="submitLoading">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 发布确认弹窗 -->
    <el-dialog
      v-model="publishDialogVisible"
      title="发布习题集"
      width="500px"
    >
      <p>发布后学生将可以查看和作答该习题集，确认发布吗？</p>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="publishDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmPublish" :loading="publishLoading">
            确认发布
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import {
  getExerciseSet,
  getQuestions,
  addQuestion,
  updateQuestion,
  deleteQuestion,
  publishExerciseSet,
  unpublishExerciseSet,
  uploadPdf,
  parsePdfWithAI,
  saveQuestions
} from '@/api/exerciseSetApi'
import type { ExerciseSet, Question } from '@/types/ExerciseSet'
import type { FormInstance, UploadFile, UploadFiles } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'

// 数据相关
const exerciseSet = ref<ExerciseSet | null>(null)
const questions = ref<Question[]>([])
const loading = ref(false)
const submitLoading = ref(false)
const publishLoading = ref(false)

// 表单相关
const dialogVisible = ref(false)
const publishDialogVisible = ref(false)
const isEditQuestion = ref(false)
const questionFormRef = ref<FormInstance>()
const editingQuestionId = ref<number | null>(null)

// PDF上传相关
const fileList = ref<UploadFiles>([])
const selectedFile = ref<File | null>(null)
const uploading = ref(false)
const isParsing = ref(false)
const parseProgress = ref(0)
const parseFailed = ref(false)
const parseErrorMessage = ref('')
const parsedQuestions = ref<any[]>([])
const savingParsed = ref(false)

// 路由
const route = useRoute()
const router = useRouter()

// 选择题选项
const choiceOptions = ref<Array<{content: string}>>([{content: ''}, {content: ''}])

const questionForm = reactive({
  type: 'choice',
  content: '',
  answer: '',
  score: 5
})

// 验证规则
const questionRules = {
  type: [{ required: true, message: '请选择题型', trigger: 'change' }],
  content: [{ required: true, message: '请输入题干内容', trigger: 'blur' }],
  answer: [{ required: true, message: '请输入标准答案', trigger: 'blur' }],
  score: [{ required: true, message: '请输入分值', trigger: 'change' }]
}

// 获取习题集ID
const exerciseSetId = Number(route.params.id)

// 按题型分组的题目
const choiceQuestions = computed(() => 
  parsedQuestions.value.filter(q => q.type === 'choice')
)

const fillQuestions = computed(() => 
  parsedQuestions.value.filter(q => q.type === 'fill')
)

const subjectiveQuestions = computed(() => 
  parsedQuestions.value.filter(q => q.type === 'subjective')
)

// 初始化
onMounted(() => {
  loadExerciseSet()
  loadQuestions()
})

// 加载习题集详情
const loadExerciseSet = async () => {
  try {
    const response = await getExerciseSet(exerciseSetId)
    if (response.success) {
      exerciseSet.value = response.data
    } else {
      ElMessage.error(response.message || '数据加载失败')
    }
  } catch (error) {
    console.error('加载习题集详情失败:', error)
    ElMessage.error('加载习题集详情失败')
  }
}

// 加载题目列表
const loadQuestions = async () => {
  try {
    loading.value = true
    const response = await getQuestions(exerciseSetId)
    if (response.success) {
      questions.value = response.data
    } else {
      ElMessage.error(response.message || '题目数据加载失败')
      questions.value = []
    }
  } catch (error) {
    console.error('加载题目列表失败:', error)
    ElMessage.error('加载题目列表失败')
    questions.value = []
  } finally {
    loading.value = false
  }
}

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
    
    // 检查文件大小（20MB限制）
    if (file.raw.size > 20 * 1024 * 1024) {
      ElMessage.error('文件大小不能超过20MB')
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
}

// 处理上传并解析
const handleUploadAndParse = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择PDF文件')
    return
  }
  
  try {
    uploading.value = true
    isParsing.value = true
    parseFailed.value = false
    parseErrorMessage.value = ''
    parseProgress.value = 0
    
    // 上传PDF文件
    const uploadResponse = await uploadPdf(exerciseSetId, selectedFile.value)
    if (!uploadResponse.success) {
      throw new Error(uploadResponse.message || '上传失败')
    }
    
    // 更新进度
    parseProgress.value = 50
    
    // 调用AI解析接口
    const parseResponse = await parsePdfWithAI(exerciseSetId, uploadResponse.data.pdfUrl)
    if (!parseResponse.success) {
      throw new Error(parseResponse.message || '解析失败')
    }
    
    // 更新进度
    parseProgress.value = 100
    
    // 设置解析结果
    parsedQuestions.value = parseResponse.data.map((q: any) => ({
      type: q.type,
      content: q.content,
      options: q.options ? q.options.map((opt: string) => ({ content: opt })) : [],
      answer: q.answer || '',
      score: q.score || 5
    }))
    
    ElMessage.success('AI解析完成')
  } catch (error: any) {
    console.error('上传并解析失败:', error)
    parseFailed.value = true
    parseErrorMessage.value = error.message || '上传并解析失败'
    ElMessage.error(error.message || '上传并解析失败')
  } finally {
    uploading.value = false
    isParsing.value = false
  }
}

// 保存解析的题目
const saveParsedQuestions = async () => {
  try {
    // 验证所有题目都有必要的信息
    for (const question of parsedQuestions.value) {
      if (!question.content) {
        ElMessage.error('存在题目没有题干内容，请完善后再保存')
        return
      }
      
      if (!question.answer) {
        ElMessage.error('存在题目没有答案，请完善后再保存')
        return
      }
      
      if (!question.score || question.score < 1 || question.score > 10) {
        ElMessage.error('存在题目分值不在1-10范围内，请完善后再保存')
        return
      }
      
      // 选择题需要验证选项和答案
      if (question.type === 'choice') {
        if (!question.options || question.options.length < 2) {
          ElMessage.error('选择题至少需要2个选项')
          return
        }
        
        // 检查是否有空选项
        for (const option of question.options) {
          if (!option.content) {
            ElMessage.error('存在选项内容为空')
            return
          }
        }
        
        // 检查答案是否在选项中
        const optionLabels = question.options.map((_: any, index: number) => String.fromCharCode(65 + index))
        if (!optionLabels.includes(question.answer)) {
          ElMessage.error('选择题答案必须是有效选项')
          return
        }
      }
    }
    
    savingParsed.value = true
    
    // 构造要发送的数据
    const questionList = parsedQuestions.value.map(question => {
      const baseData: any = {
        type: question.type,
        content: question.content,
        answer: question.answer,
        score: question.score
      }
      
      // 如果是选择题，添加选项
      if (question.type === 'choice') {
        baseData.options = JSON.stringify(question.options)
      }
      
      return baseData
    })
    
    // 调用保存接口
    const response = await saveQuestions(exerciseSetId, questionList)
    if (response.success) {
      ElMessage.success('题目保存成功')
      resetParsedQuestions()
      loadQuestions()
      loadExerciseSet() // 更新题目数量
    } else {
      throw new Error(response.message || '保存失败')
    }
  } catch (error: any) {
    console.error('保存题目失败:', error)
    ElMessage.error(error.message || '保存题目失败')
  } finally {
    savingParsed.value = false
  }
}

// 重置解析结果
const resetParsedQuestions = () => {
  parsedQuestions.value = []
  fileList.value = []
  selectedFile.value = null
}

// 重置上传
const resetUpload = () => {
  fileList.value = []
  selectedFile.value = null
  parseFailed.value = false
  parseErrorMessage.value = ''
}

// 添加选项
const addOption = (question: any) => {
  question.options.push({ content: '' })
}

// 删除选项
const removeOption = (question: any, index: number) => {
  if (question.options.length <= 2) {
    ElMessage.warning('至少需要2个选项')
    return
  }
  question.options.splice(index, 1)
  
  // 如果删除的是答案选项，清空答案
  const answerLetter = question.answer
  const removedLetter = String.fromCharCode(65 + index)
  if (answerLetter === removedLetter) {
    question.answer = ''
  }
}

// 处理添加题目
const handleAddQuestion = () => {
  isEditQuestion.value = false
  editingQuestionId.value = null
  resetQuestionForm()
  dialogVisible.value = true
}

// 处理编辑题目
const handleEditQuestion = (question: Question) => {
  isEditQuestion.value = true
  editingQuestionId.value = question.id || null
  questionForm.type = question.type
  questionForm.content = question.content
  questionForm.answer = question.answer || ''
  questionForm.score = question.score || 5
  
  // 处理选择题选项
  if (question.type === 'choice' && question.options) {
    try {
      choiceOptions.value = JSON.parse(question.options)
    } catch (e) {
      choiceOptions.value = [{content: ''}, {content: ''}]
    }
  } else {
    choiceOptions.value = [{content: ''}, {content: ''}]
  }
  
  dialogVisible.value = true
}

// 处理删除题目
const handleDeleteQuestion = (question: Question) => {
  ElMessageBox.confirm(
    `确定要删除该题目吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const response = await deleteQuestion(exerciseSetId, question.id!)
      if (response.success) {
        ElMessage.success('删除成功')
        loadQuestions()
      } else {
        ElMessage.error(response.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 用户取消删除
  })
}

// 处理题型变化
const handleQuestionTypeChange = (value: string) => {
  // 重置答案
  questionForm.answer = ''
  
  // 如果是选择题，初始化选项
  if (value === 'choice') {
    choiceOptions.value = [{content: ''}, {content: ''}]
  }
}

// 添加选择题选项
const addChoiceOption = () => {
  choiceOptions.value.push({content: ''})
}

// 删除选择题选项
const removeChoiceOption = (index: number) => {
  if (choiceOptions.value.length <= 2) {
    ElMessage.warning('至少需要2个选项')
    return
  }
  choiceOptions.value.splice(index, 1)
}

// 获取选项标签（A, B, C...）
const getOptionLabel = (index: number) => {
  return String.fromCharCode(65 + index) // 65是字母A的ASCII码
}

// 重置题目表单
const resetQuestionForm = () => {
  questionForm.type = 'choice'
  questionForm.content = ''
  questionForm.answer = ''
  questionForm.score = 5
  choiceOptions.value = [{content: ''}, {content: ''}]
}

// 处理弹窗关闭
const handleDialogClose = (done: () => void) => {
  resetQuestionForm()
  done()
}

// 提交题目
const submitQuestion = async () => {
  if (!questionFormRef.value) return

  try {
    await questionFormRef.value.validate()
    submitLoading.value = true

    // 准备题目数据
    const questionData: any = {
      type: questionForm.type,
      content: questionForm.content,
      answer: questionForm.answer,
      score: questionForm.score
    }

    // 如果是选择题，保存选项
    if (questionForm.type === 'choice') {
      questionData.options = JSON.stringify(choiceOptions.value)
    }

    let response
    if (isEditQuestion.value && editingQuestionId.value) {
      // 更新题目
      response = await updateQuestion(exerciseSetId, editingQuestionId.value, questionData)
    } else {
      // 添加题目
      response = await addQuestion(exerciseSetId, questionData)
    }

    if (response.success) {
      ElMessage.success(isEditQuestion.value ? '更新成功' : '添加成功')
      dialogVisible.value = false
      loadQuestions()
      loadExerciseSet() // 更新题目数量
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

// 处理发布
const handlePublish = () => {
  publishDialogVisible.value = true
}

// 确认发布
const confirmPublish = async () => {
  try {
    publishLoading.value = true
    const response = await publishExerciseSet(exerciseSetId)
    if (response.success) {
      ElMessage.success('发布成功')
      publishDialogVisible.value = false
      loadExerciseSet()
    } else {
      ElMessage.error(response.message || '发布失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '发布失败')
  } finally {
    publishLoading.value = false
  }
}

// 处理取消发布
const handleUnpublish = async () => {
  try {
    const response = await unpublishExerciseSet(exerciseSetId)
    if (response.success) {
      ElMessage.success('取消发布成功')
      loadExerciseSet()
    } else {
      ElMessage.error(response.message || '取消发布失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '取消发布失败')
  }
}

// 返回上一页
const handleBack = () => {
  router.push('/exercise-sets')
}
</script>

<style scoped>
.question-management {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.header-content p {
  margin: 5px 0;
  color: #666;
}

.upload-card {
  margin-bottom: 20px;
}

.upload-header {
  font-weight: bold;
  font-size: 16px;
}

.upload-button-group {
  text-align: center;
  margin-top: 20px;
}

.parsing-status {
  text-align: center;
  padding: 20px 0;
}

.parsing-text {
  margin-top: 10px;
  color: #666;
}

.parse-failed {
  padding: 20px 0;
}

.parsed-questions-card {
  margin-bottom: 20px;
}

.parsed-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
}

.question-groups {
  margin-top: 20px;
}

.question-group {
  margin-bottom: 30px;
}

.question-group h3 {
  margin-bottom: 15px;
  color: #333;
}

.question-item {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 15px;
  background-color: #f9f9f9;
}

.question-form {
  margin-bottom: 10px;
}

.option-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.score-error {
  color: #f56c6c;
  font-size: 12px;
  line-height: 1;
  padding-top: 4px;
  display: block;
}

.list-card {
  margin-bottom: 20px;
}

.question-content {
  white-space: pre-wrap;
  line-height: 1.5;
}
</style>