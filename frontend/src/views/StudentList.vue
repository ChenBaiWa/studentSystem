node<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学生列表</span>
          <el-button type="primary" @click="showDialog = true">添加学生</el-button>
        </div>
      </template>

      <el-table :data="students" v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="gender" label="性别" width="80" />
        <el-table-column prop="age" label="年龄" width="80" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="电话" />
        <el-table-column prop="address" label="地址" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="editStudent(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteStudent(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="showDialog"
      :title="isEditing ? '编辑学生' : '添加学生'"
      width="500px"
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="性别" required>
          <el-select v-model="form.gender" placeholder="请选择性别">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="年龄" required>
          <el-input-number v-model="form.age" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" type="textarea" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllStudents, createStudent, updateStudent, deleteStudent } from '@/api/studentApi'
import type { Student } from '@/types/Student'

const students = ref<Student[]>([])
const loading = ref(false)
const showDialog = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)

const form = ref<Student>({
  name: '',
  gender: '',
  age: 18,
  email: '',
  phone: '',
  address: ''
})

// 加载学生列表
const loadStudents = async () => {
  loading.value = true
  try {
    students.value = await getAllStudents()
  } catch (error) {
    ElMessage.error('加载学生列表失败')
  } finally {
    loading.value = false
  }
}

// 编辑学生
const editStudent = (student: Student) => {
  isEditing.value = true
  editingId.value = student.id!
  form.value = { ...student }
  showDialog.value = true
}

// 删除学生
const deleteStudent = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个学生吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteStudent(id)
    ElMessage.success('删除成功')
    loadStudents()
  } catch (error) {
    // 用户取消删除
  }
}

// 提交表单
const submitForm = async () => {
  try {
    if (isEditing.value && editingId.value) {
      await updateStudent(editingId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await createStudent(form.value)
      ElMessage.success('添加成功')
    }

    showDialog.value = false
    resetForm()
    loadStudents()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 重置表单
const resetForm = () => {
  form.value = {
    name: '',
    gender: '',
    age: 18,
    email: '',
    phone: '',
    address: ''
  }
  isEditing.value = false
  editingId.value = null
}

// 初始化加载数据
onMounted(() => {
  loadStudents()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
