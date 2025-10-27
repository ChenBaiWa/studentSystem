<template>
  <div class="exercise-set-management">
    <el-card class="header-card">
      <div class="header-content">
        <h2>习题集管理</h2>
        <div class="header-buttons">
          <el-button type="primary" @click="handleCreate">新建习题集</el-button>
          <el-button type="success" @click="handlePdfUpload">PDF上传解析</el-button>
        </div>
      </div>
    </el-card>

    <!-- 习题集列表 -->
    <el-card class="list-card">
      <el-table :data="exerciseSets" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="习题集名称" min-width="150" />
        <el-table-column prop="subject" label="学科" width="120" />
        <el-table-column prop="questionCount" label="题目数" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 'editing'" type="warning">编辑中</el-tag>
            <el-tag v-else-if="scope.row.status === 'published'" type="success">已发布</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button
              size="small"
              type="primary"
              @click="handleManageQuestions(scope.row)"
            >
              管理题目
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDelete(scope.row)"
              :disabled="scope.row.status === 'published'"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && exerciseSets.length === 0" description="暂无习题集数据" />
    </el-card>

    <!-- 新建/编辑习题集弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑习题集' : '新建习题集'"
      width="500px"
    >
      <el-form
        ref="exerciseSetFormRef"
        :model="exerciseSetForm"
        :rules="exerciseSetRules"
        label-width="100px"
      >
        <el-form-item label="习题集名称" prop="name">
          <el-input
            v-model="exerciseSetForm.name"
            placeholder="请输入习题集名称，如：高一物理力学练习"
            maxlength="30"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="学科" prop="subject">
          <el-select
            v-model="exerciseSetForm.subject"
            placeholder="请选择学科"
            style="width: 100%"
          >
            <el-option label="数学" value="数学" />
            <el-option label="语文" value="语文" />
            <el-option label="英语" value="英语" />
            <el-option label="物理" value="物理" />
            <el-option label="化学" value="化学" />
            <el-option label="生物" value="生物" />
            <el-option label="历史" value="历史" />
            <el-option label="地理" value="地理" />
            <el-option label="政治" value="政治" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitExerciseSet" :loading="submitLoading">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- PDF上传解析弹窗 -->
    <el-dialog
      v-model="pdfDialogVisible"
      title="PDF上传与AI解析"
      width="800px"
      @close="handlePdfDialogClose"
    >
      <PdfUpload @analysisComplete="handlePdfAnalysisComplete" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getExerciseSets,
  createExerciseSet,
  updateExerciseSet,
  deleteExerciseSet
} from '@/api/exerciseSetApi'
import type { ExerciseSet } from '@/types/ExerciseSet'
import type { FormInstance } from 'element-plus'
import { useRouter } from 'vue-router'
import PdfUpload from './PdfUpload.vue'

// 数据相关
const exerciseSets = ref<ExerciseSet[]>([])
const loading = ref(false)
const submitLoading = ref(false)

// 表单相关
const dialogVisible = ref(false)
const pdfDialogVisible = ref(false)
const isEdit = ref(false)
const exerciseSetFormRef = ref<FormInstance>()
const editingId = ref<number | null>(null)

// 路由
const router = useRouter()

const exerciseSetForm = reactive({
  name: '',
  subject: ''
})

// 验证规则
const exerciseSetRules = {
  name: [
    { required: true, message: '请输入习题集名称', trigger: 'blur' },
    { min: 2, max: 30, message: '长度在 2 到 30 个字符', trigger: 'blur' }
  ],
  subject: [{ required: true, message: '请选择学科', trigger: 'change' }]
}

// 初始化
onMounted(() => {
  loadExerciseSets()
})

// 加载习题集列表
const loadExerciseSets = async () => {
  try {
    loading.value = true
    const response = await getExerciseSets()
    if (response.success) {
      exerciseSets.value = response.data
    } else {
      ElMessage.error(response.message || '数据加载失败')
      exerciseSets.value = []
    }
  } catch (error) {
    console.error('加载习题集列表失败:', error)
    ElMessage.error('加载习题集列表失败')
    exerciseSets.value = []
  } finally {
    loading.value = false
  }
}

// 处理新建
const handleCreate = () => {
  isEdit.value = false
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

// 处理PDF上传
const handlePdfUpload = () => {
  pdfDialogVisible.value = true
}

// 处理PDF对话框关闭
const handlePdfDialogClose = () => {
  // 可以在这里添加关闭时的清理逻辑
}

// 处理PDF解析完成
const handlePdfAnalysisComplete = (result: string) => {
  // 处理PDF解析完成的逻辑
  console.log('PDF解析完成:', result)
  ElMessage.success('PDF解析完成，可以开始创建习题集了')
  pdfDialogVisible.value = false
  
  // 可以在这里自动创建一个习题集并将解析结果用于题目创建
  // 例如：
  // 1. 创建一个新的习题集
  // 2. 跳转到题目管理页面
  // 3. 将解析结果传递过去用于自动创建题目
}

// 处理编辑
const handleEdit = (exerciseSet: ExerciseSet) => {
  isEdit.value = true
  editingId.value = exerciseSet.id || null
  exerciseSetForm.name = exerciseSet.name
  exerciseSetForm.subject = exerciseSet.subject
  dialogVisible.value = true
}

// 处理管理题目
const handleManageQuestions = (exerciseSet: ExerciseSet) => {
  router.push(`/exercise-sets/${exerciseSet.id}/questions`)
}

// 处理删除
const handleDelete = (exerciseSet: ExerciseSet) => {
  ElMessageBox.confirm(
    `确定要删除习题集 "${exerciseSet.name}" 吗？此操作不可恢复！`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const response = await deleteExerciseSet(exerciseSet.id!)
      if (response.success) {
        ElMessage.success('删除成功')
        loadExerciseSets()
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

// 重置表单
const resetForm = () => {
  exerciseSetForm.name = ''
  exerciseSetForm.subject = ''
}

// 提交表单
const submitExerciseSet = async () => {
  if (!exerciseSetFormRef.value) return

  try {
    await exerciseSetFormRef.value.validate()
    submitLoading.value = true

    let response
    if (isEdit.value && editingId.value) {
      // 更新习题集
      response = await updateExerciseSet(editingId.value, {
        name: exerciseSetForm.name,
        subject: exerciseSetForm.subject
      })
    } else {
      // 创建习题集
      response = await createExerciseSet({
        name: exerciseSetForm.name,
        subject: exerciseSetForm.subject
      })
    }

    if (response.success) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadExerciseSets()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

// 格式化日期
const formatDate = (dateString?: string) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}
</script>

<style scoped>
.exercise-set-management {
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

.header-buttons {
  display: flex;
  gap: 10px;
}

.list-card {
  margin-bottom: 20px;
}
</style>