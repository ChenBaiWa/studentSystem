<template>
  <div class="class-management">
    <h2>班级管理</h2>

    <!-- 老师端视图 -->
    <div v-if="userRole === 1" class="teacher-view">
      <div class="toolbar">
        <el-button type="primary" @click="showCreateDialog">新建班级</el-button>
      </div>

      <el-table :data="classList" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="班级名称" />
        <el-table-column prop="classCode" label="班级号" />
        <el-table-column prop="verificationCode" label="验证码" />
        <el-table-column prop="gradeName" label="所属年级" />
        <el-table-column prop="creatorName" label="创建人" />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
<!--            <el-button size="small" @click="showStudentList(scope.row)">查看学生</el-button>-->
            <el-button size="small" @click="showEditDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteClassHandler(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 学生端视图 -->
    <div v-else class="student-view">
      <!-- 加入班级区域 -->
      <div class="join-class-form">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>加入班级</span>
            </div>
          </template>
          <el-form :model="joinForm" :rules="joinRules" ref="joinFormRef" label-width="100px">
            <el-form-item label="班级号" prop="classCode">
              <el-input
                v-model="joinForm.classCode"
                placeholder="请输入6位班级号"
                maxlength="6"
                @input="onClassCodeInput"
              />
            </el-form-item>
            <el-form-item label="验证码" prop="verificationCode">
              <el-input
                v-model="joinForm.verificationCode"
                placeholder="请输入4位验证码"
                maxlength="4"
                @input="onVerificationCodeInput"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                @click="joinClassHandler"
                :loading="joinLoading"
              >
                加入
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>

      <!-- 已加入班级列表 -->
      <div class="joined-classes">
        <h3>已加入班级</h3>
        <el-table :data="joinedClassList" style="width: 100%" v-loading="loading">
          <el-table-column prop="name" label="班级名称" />
          <el-table-column prop="classCode" label="班级号" />
          <el-table-column prop="gradeName" label="所属年级" />
          <el-table-column prop="creatorName" label="任课老师" />
        </el-table>
      </div>
    </div>

    <!-- 新建/编辑班级对话框 -->
    <el-dialog :title="dialogTitle" v-model="classDialogVisible" width="500px">
      <el-form :model="currentClass" :rules="classRules" ref="classForm">
        <el-form-item label="所属年级" prop="gradeId">
          <el-select v-model="currentClass.gradeId"  placeholder="请选择年级" style="width: 100%">
            <el-option
              v-for="grade in gradeList"
              :key="grade.id"
              :label="grade.name"
              :value="grade.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="班级名称" prop="name">
          <el-input v-model="currentClass.name" placeholder="请输入班级名称（2-20字）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="classDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveClass">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看学生列表对话框 -->
    <el-dialog title="班级学生列表" v-model="studentDialogVisible" width="600px">
      <el-table :data="studentList" style="width: 100%">
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="phone" label="手机号" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import {
  getTeacherClassList,
  createClass,
  updateClass,
  deleteClass,
  joinClass,
  getStudentClassList,
  getClassStudents
} from '@/api/classApi'
import { getGrades } from '@/api/gradeApi'
import type { Class, JoinClassRequest } from '@/types/Class'
import type { Grade } from '@/types/Grade'

// 从localStorage或sessionStorage获取用户角色
const getUserRole = () => {
  // 检查是否有有效的token
  const token = localStorage.getItem('token') || sessionStorage.getItem('token')
  if (!token) {
    // 如果没有token，重定向到登录页
    window.location.href = '/login'
    return 2 // 默认返回学生角色，但实际上会重定向
  }

  const userRole = localStorage.getItem('userRole') || sessionStorage.getItem('userRole')
  return userRole ? parseInt(userRole) : 2 // 默认为学生角色
}

const userRole = ref(getUserRole()) // 1: 老师, 2: 学生

// 老师端数据
const classList = ref<Class[]>([])
const gradeList = ref<Grade[]>([]) // 年级列表

// 学生端数据
const joinedClassList = ref<Class[]>([])
const joinForm = reactive<JoinClassRequest>({
  classCode: '',
  verificationCode: ''
})

// 表单引用
const joinFormRef = ref<FormInstance>()
const classForm = ref<FormInstance>()

// 对话框相关
const classDialogVisible = ref(false)
const studentDialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const currentClass = reactive<Partial<Class>>({
  name: '',
  gradeId: undefined
})

// 学生列表
const studentList = ref([])

// 加载状态
const loading = ref(false)
const joinLoading = ref(false)

// 表单验证规则
const classRules = {
  name: [
    { required: true, message: '请输入班级名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  gradeId: [
    { required: true, message: '请选择所属年级', trigger: 'change' }
  ]
}

const joinRules = {
  classCode: [
    { required: true, message: '请输入班级号', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '班级号必须是6位数字', trigger: 'blur' }
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{4}$/, message: '验证码必须是4位数字', trigger: 'blur' }
  ]
}

// 班级号输入处理
const onClassCodeInput = (value: string) => {
  joinForm.classCode = value.replace(/\D/g, '') // 只允许数字
}

// 验证码输入处理
const onVerificationCodeInput = (value: string) => {
  joinForm.verificationCode = value.replace(/\D/g, '') // 只允许数字
}

// 获取老师创建的班级列表
const loadTeacherClassList = async () => {
  loading.value = true
  try {
    const res = await getTeacherClassList()
    if (res.data.success) {
      classList.value = res.data.data
    } else {
      ElMessage.error(res.data.message || '获取班级列表失败')
    }
  } catch (err) {
    ElMessage.error('获取班级列表失败')
  } finally {
    loading.value = false
  }
}

// 获取老师创建的年级列表
const loadGradeList = async () => {
  try {
    const res = await getGrades()
    if (res.data.success) {
      gradeList.value = res.data.data
    } else {
      ElMessage.error(res.data.message || '获取年级列表失败')
    }
  } catch (err) {
    ElMessage.error('获取年级列表失败')
  }
}

// 获取学生加入的班级列表
const loadStudentClassList = async () => {
  loading.value = true
  try {
    const res = await getStudentClassList()
    if (res.data.success) {
      joinedClassList.value = res.data.data
    } else {
      ElMessage.error(res.data.message || '获取班级列表失败')
    }
  } catch (err) {
    ElMessage.error('获取班级列表失败')
  } finally {
    loading.value = false
  }
}

// 显示创建班级对话框
const showCreateDialog = () => {
  dialogTitle.value = '新建班级'
  isEdit.value = false
  Object.assign(currentClass, {
    name: '',
    gradeId: undefined
  })
  classDialogVisible.value = true
}

// 显示编辑班级对话框
const showEditDialog = (clazz: Class) => {
  dialogTitle.value = '编辑班级'
  isEdit.value = true
  Object.assign(currentClass, clazz)
  classDialogVisible.value = true
}

// 保存班级（新建或编辑）
const saveClass = async () => {
  if (!classForm.value) return

  await classForm.value.validate(async (valid) => {
    if (valid) {
      try {
        // 设置年级名称
        if (currentClass.gradeId) {
          const grade = gradeList.value.find(g => g.id === currentClass.gradeId)
          if (grade) {
            currentClass.gradeName = grade.name
          }
        }

        let res
        if (isEdit.value) {
          res = await updateClass(currentClass as Class)
        } else {
          res = await createClass(currentClass as Class)
        }

        if (res.data.success) {
          ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
          classDialogVisible.value = false
          loadTeacherClassList()

          // 如果是新建班级，显示班级号和验证码
          if (!isEdit.value && res.data.data) {
            const newClass = res.data.data
            ElMessage.success(`班级创建成功！班级号：${newClass.classCode}，验证码：${newClass.verificationCode}`)
          }
        } else {
          ElMessage.error(res.data.message || (isEdit.value ? '更新失败' : '创建失败'))
        }
      } catch (err) {
        ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
      }
    }
  })
}

// 删除班级
const deleteClassHandler = (clazz: Class) => {
  ElMessageBox.confirm(`确定要删除班级 "${clazz.name}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteClass(clazz.id!)
      if (res.data.success) {
        ElMessage.success('删除成功')
        loadTeacherClassList()
      } else {
        ElMessage.error(res.data.message || '删除失败')
      }
    } catch (err) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 用户取消删除
  })
}

// 显示班级学生列表
const showStudentList = async (clazz: Class) => {
  try {
    const res = await getClassStudents(clazz.id!)
    if (res.success) {
      studentList.value = res.data.data
      studentDialogVisible.value = true
    } else {
      ElMessage.error(res.data.message || '获取学生列表失败')
    }
  } catch (err) {
    ElMessage.error('获取学生列表失败')
  }
}

// 学生加入班级
const joinClassHandler = async () => {
  if (!joinFormRef.value) return

  await joinFormRef.value.validate(async (valid) => {
    if (valid) {
      joinLoading.value = true
      try {
        const res = await joinClass(joinForm)
        if (res.data.success) {
          ElMessage.success('加入班级成功！')
          loadStudentClassList()
          // 清空表单
          joinForm.classCode = ''
          joinForm.verificationCode = ''
        } else {
          ElMessage.error(res.data.message || '加入班级失败')
        }
      } catch (err) {
        ElMessage.error('加入班级失败')
      } finally {
        joinLoading.value = false
      }
    }
  })
}

// 页面加载时初始化数据
onMounted(() => {
  if (userRole.value === 1) {
    loadTeacherClassList()
    loadGradeList()
  } else {
    loadStudentClassList()
  }
})
</script>

<style scoped>
.class-management {
  padding: 20px;
}

.toolbar {
  margin-bottom: 20px;
}

.join-class-form {
  margin-bottom: 30px;
  max-width: 500px;
}

.card-header {
  font-weight: bold;
}

.joined-classes {
  margin-top: 30px;
}

.joined-classes h3 {
  margin-bottom: 15px;
}
</style>
