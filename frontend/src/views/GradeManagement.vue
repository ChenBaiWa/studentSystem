<template>
  <div class="grade-management">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">年级管理</span>
          <el-button type="primary" @click="showCreateDialog = true" v-if="hasPermission">
            <el-icon><Plus /></el-icon>
            新建年级
          </el-button>
        </div>
      </template>

      <el-alert
        v-if="!hasPermission"
        title="权限不足，仅老师可访问年级管理功能"
        type="error"
        show-icon
        :closable="false"
      />

      <el-table :data="grades" v-loading="loading" style="width: 100%" v-if="hasPermission">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="年级名称" />
        <el-table-column prop="creatorName" label="创建人" />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="editGrade(scope.row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button size="small" type="danger" @click="deleteGrade(scope.row.id)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新建/编辑年级对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="isEditing ? '编辑年级' : '新建年级'"
      width="500px"
    >
      <el-form
        ref="gradeFormRef"
        :model="gradeForm"
        :rules="gradeRules"
        label-width="100px"
      >
        <el-form-item label="年级名称" prop="name">
          <el-input
            v-model="gradeForm.name"
            placeholder="请输入年级名称，如：高一"
            maxlength="10"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button
          type="primary"
          @click="submitGrade"
          :loading="submitting"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { getGrades, createGrade, updateGrade, deleteGrade } from '@/api/gradeApi'
import type { Grade } from '@/types/Grade'

// 表单引用
const gradeFormRef = ref<FormInstance>()

// 数据状态
const grades = ref<Grade[]>([])
const loading = ref(false)
const submitting = ref(false)
const showCreateDialog = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)

// 获取当前用户信息
const getCurrentUserInfo = () => {
  // 检查是否有有效的token
  const token = localStorage.getItem('token') || sessionStorage.getItem('token')
  if (!token) {
    // 如果没有token，重定向到登录页
    window.location.href = '/login'
    return { userId: 0, realName: '未知用户', userRole: 2 } // 默认值，但实际上会重定向
  }
  
  const userId = parseInt(localStorage.getItem('userId') || sessionStorage.getItem('userId') || '0')
  const realName = localStorage.getItem('realName') || sessionStorage.getItem('realName') || '未知用户'
  const userRole = parseInt(localStorage.getItem('userRole') || sessionStorage.getItem('userRole') || '2')
  return { userId, realName, userRole }
}

const { userId, realName, userRole } = getCurrentUserInfo()

// 检查是否有权限（仅老师有权限）
const hasPermission = computed(() => userRole === 1)

// 表单数据
const gradeForm = reactive({
  name: '',
  creatorId: userId,
  creatorName: realName
})

// 表单验证规则
const gradeRules = reactive<FormRules>({
  name: [
    { required: true, message: '请输入年级名称', trigger: 'blur' },
    { min: 2, max: 10, message: '年级名称长度应在2-10个字符之间', trigger: 'blur' }
  ]
})

// 加载年级列表
const loadGrades = async () => {
  if (!hasPermission.value) return
  
  loading.value = true
  try {
    const response = await getGrades()
    if (response.data.success) {
      grades.value = response.data.data
    } else {
      ElMessage.error(response.data.message || '加载年级列表失败')
    }
  } catch (error: any) {
    if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('加载年级列表失败')
    }
  } finally {
    loading.value = false
  }
}

// 编辑年级
const editGrade = (grade: Grade) => {
  isEditing.value = true
  editingId.value = grade.id || null
  gradeForm.name = grade.name
  showCreateDialog.value = true
}

// 删除年级
const deleteGradeHandler = async (id: number) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该年级吗？删除后关联的班级、课本也会受影响！',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 调用API删除年级
    const response = await deleteGrade(id)
    if (response.data.success) {
      ElMessage.success('删除成功')
      loadGrades()
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error: any) {
    if (error.message) {
      ElMessage.error(error.message)
    } else if (error.response && error.response.data && error.response.data.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('删除失败')
    }
  }
}

// 提交年级表单
const submitGrade = async () => {
  if (!gradeFormRef.value) return

  try {
    const valid = await gradeFormRef.value.validate()
    if (!valid) return

    submitting.value = true

    // 确保创建者信息是最新的
    gradeForm.creatorId = userId
    gradeForm.creatorName = realName

    let response
    if (isEditing.value && editingId.value) {
      // 调用API更新年级
      response = await updateGrade(editingId.value, {
        ...gradeForm,
        id: editingId.value
      })
    } else {
      // 调用API创建年级
      response = await createGrade(gradeForm)
    }

    if (response.data.success) {
      ElMessage.success(isEditing.value ? '编辑成功' : '创建成功')
      showCreateDialog.value = false
      resetForm()
      loadGrades()
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error: any) {
    if (error.message) {
      ElMessage.error(error.message)
    } else if (error.response && error.response.data && error.response.data.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('操作失败')
    }
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  gradeForm.name = ''
  gradeForm.creatorId = userId
  gradeForm.creatorName = realName
  isEditing.value = false
  editingId.value = null
}

// 初始化加载数据
onMounted(() => {
  loadGrades()
})
</script>

<style scoped>
.grade-management {
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
</style>