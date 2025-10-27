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

    <!-- 题目列表 -->
    <el-card class="list-card">
      <el-table :data="questions" style="width: 100%" v-loading="loading">
        <el-table-column prop="sortOrder" label="#" width="60" />
        <el-table-column prop="type" label="题型" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.type === 'choice'">选择题</el-tag>
            <el-tag v-else-if="scope.row.type === 'fill'">填空题</el-tag>
            <el-tag v-else-if="scope.row.type === 'solve'">解答题</el-tag>
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
            <el-option label="解答题" value="solve" />
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
            v-if="questionForm.type === 'fill' || questionForm.type === 'solve'"
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
            :max="questionForm.type === 'solve' ? 20 : 10"
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getExerciseSet,
  getQuestions,
  addQuestion,
  updateQuestion,
  deleteQuestion,
  publishExerciseSet,
  unpublishExerciseSet
} from '@/api/exerciseSetApi'
import type { ExerciseSet, Question } from '@/types/ExerciseSet'
import type { FormInstance } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'

// 路由
const route = useRoute()
const router = useRouter()

// 获取习题集ID
const exerciseSetId = Number(route.params.id)

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
  
  // 设置默认分值
  if (value === 'solve') {
    questionForm.score = 10
  } else {
    questionForm.score = 5
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
  
  // 如果删除的是答案选项，清空答案
  const answerLetter = questionForm.answer
  const removedLetter = String.fromCharCode(65 + index)
  if (answerLetter === removedLetter) {
    questionForm.answer = ''
  }
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

.list-card {
  margin-bottom: 20px;
}
</style>