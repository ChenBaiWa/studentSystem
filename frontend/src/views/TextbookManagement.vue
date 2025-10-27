<template>
  <div class="textbook-management">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">课本管理</span>
          <el-button type="primary" @click="showCreateDialog = true">
            <el-icon><Plus /></el-icon>
            新建课本
          </el-button>
        </div>
      </template>

      <!-- 年级筛选 -->
      <div class="filter-section">
        <el-select
          v-model="selectedGradeId"
          placeholder="请选择年级"
          @change="loadTextbooks"
          style="width: 200px; margin-bottom: 20px;"
        >
          <el-option
            v-for="grade in grades"
            :key="grade.id"
            :label="grade.name"
            :value="grade.id"
          />
        </el-select>
      </div>

      <!-- 课本列表 -->
      <el-table :data="textbooks" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="课本名称" />
        <el-table-column prop="gradeName" label="所属年级" />
        <el-table-column prop="creatorName" label="创建人" />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="editTextbook(scope.row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button size="small" type="danger" @click="deleteTextbook(scope.row.id)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新建/编辑课本对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="isEditing ? '编辑课本' : '新建课本'"
      width="500px"
    >
      <el-form
        ref="textbookFormRef"
        :model="textbookForm"
        :rules="textbookRules"
        label-width="100px"
      >
        <el-form-item label="所属年级" prop="gradeId">
          <el-select
            v-model="textbookForm.gradeId"
            placeholder="请选择年级"
            style="width: 100%"
          >
            <el-option
              v-for="grade in grades"
              :key="grade.id"
              :label="grade.name"
              :value="grade.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="课本名称" prop="name">
          <el-input
            v-model="textbookForm.name"
            placeholder="请输入课本名称，如：苏教版语文八年级下册"
            maxlength="30"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button
          type="primary"
          @click="submitTextbook"
          :loading="submitting"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { getAllTextbooks, createTextbook, updateTextbook, deleteTextbook } from '@/api/textbookApi'
import { getGrades } from '@/api/gradeApi'
import type { Textbook } from '@/types/Textbook'
import type { Grade } from '@/types/Grade'

// 表单引用
const textbookFormRef = ref<FormInstance>()

// 数据状态
const textbooks = ref<Textbook[]>([])
const grades = ref<Grade[]>([])
const loading = ref(false)
const submitting = ref(false)
const showCreateDialog = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const selectedGradeId = ref<number | null>(null)

// 获取当前用户信息
const getCurrentUserInfo = () => {
  // 检查是否有有效的token
  const token = localStorage.getItem('token') || sessionStorage.getItem('token')
  if (!token) {
    // 如果没有token，重定向到登录页
    window.location.href = '/login'
    return { userId: 0, realName: '未知用户' } // 默认值，但实际上会重定向
  }
  
  const userId = parseInt(localStorage.getItem('userId') || sessionStorage.getItem('userId') || '0')
  const realName = localStorage.getItem('realName') || sessionStorage.getItem('realName') || '未知用户'
  return { userId, realName }
}

const { userId, realName } = getCurrentUserInfo()

// 表单数据
const textbookForm = reactive({
  name: '',
  gradeId: null as number | null,
  gradeName: '',
  creatorId: userId,
  creatorName: realName
})

// 表单验证规则
const textbookRules = reactive<FormRules>({
  gradeId: [
    { required: true, message: '请选择所属年级', trigger: 'change' }
  ],
  name: [
    { required: true, message: '请输入课本名称', trigger: 'blur' },
    { min: 2, max: 30, message: '课本名称长度应在2-30个字符之间', trigger: 'blur' }
  ]
})

// 加载年级列表
const loadGrades = async () => {
  try {
    const response = await getGrades()
    if (response.data.success) {
      grades.value = response.data.data
      // 默认加载第一个年级的课本
      if (grades.value.length > 0 && !selectedGradeId.value) {
        selectedGradeId.value = grades.value[0].id!
        loadTextbooks(grades.value[0].id!)
      }
    } else {
      ElMessage.error(response.data.message || '加载年级列表失败')
    }
  } catch (error: any) {
    if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('加载年级列表失败')
    }
  }
}

// 加载课本列表
const loadTextbooks = async (gradeId: number) => {
  try {
    selectedGradeId.value = gradeId
    loading.value = true
    const response = await getAllTextbooks(gradeId)
    if (response.data.success) {
      textbooks.value = response.data.data
    } else {
      ElMessage.error(response.data.message || '加载课本列表失败')
    }
  } catch (error: any) {
    if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('加载课本列表失败')
    }
  } finally {
    loading.value = false
  }
}

// 编辑课本
const editTextbook = (textbook: Textbook) => {
  isEditing.value = true
  editingId.value = textbook.id || null
  textbookForm.name = textbook.name
  textbookForm.gradeId = textbook.gradeId
  showCreateDialog.value = true
}

// 提交表单
const submitTextbook = async () => {
  if (!textbookFormRef.value) return

  try {
    const valid = await textbookFormRef.value.validate()
    if (!valid) return

    submitting.value = true

    // 设置创建者信息
    textbookForm.creatorId = userId
    textbookForm.creatorName = realName
    textbookForm.gradeName = grades.value.find(g => g.id === textbookForm.gradeId)?.name || ''

    let response
    if (isEditing.value && editingId.value) {
      // 更新课本
      response = await updateTextbook(editingId.value, {
        ...textbookForm,
        id: editingId.value
      })
    } else {
      // 创建课本
      response = await createTextbook(textbookForm)
    }

    if (response.data.success) {
      ElMessage.success(isEditing.value ? '更新成功' : '创建成功')
      showCreateDialog.value = false
      resetForm()
      if (selectedGradeId.value) {
        loadTextbooks(selectedGradeId.value)
      }
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error: any) {
    if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('操作失败')
    }
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  textbookForm.name = ''
  textbookForm.gradeId = selectedGradeId.value
  textbookForm.creatorId = userId
  textbookForm.creatorName = realName
  isEditing.value = false
  editingId.value = null
}

// 删除课本
const deleteTextbookHandler = async (id: number) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该课本吗？删除后关联的章节也会被删除！',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await deleteTextbook(id)
    if (response.data.success) {
      ElMessage.success('删除成功')
      if (selectedGradeId.value) {
        loadTextbooks(selectedGradeId.value)
      }
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error: any) {
    if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('删除失败')
    }
  }
}

// 初始化加载数据
onMounted(() => {
  loadGrades()
})
</script>

<style scoped>
.textbook-management {
  padding: 20px;
}

.main-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: bold;
}

.filter-section {
  margin-bottom: 20px;
}
</style>