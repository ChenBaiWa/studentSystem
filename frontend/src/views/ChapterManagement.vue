<template>
  <div class="chapter-management">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">章节管理</span>
          <el-button type="primary" @click="showCreateDialog = true" :disabled="!selectedTextbookId">
            <el-icon><Plus /></el-icon>
            新建章节
          </el-button>
        </div>
      </template>

      <!-- 课本筛选 -->
      <div class="filter-section">
        <el-select
          v-model="selectedTextbookId"
          placeholder="请选择课本"
          @change="loadChapters"
          style="width: 300px; margin-bottom: 20px;"
        >
          <el-option
            v-for="textbook in textbooks"
            :key="textbook.id"
            :label="textbook.name"
            :value="textbook.id"
          />
        </el-select>
      </div>

      <!-- 章节列表 -->
      <div v-if="selectedTextbookId">
        <el-table :data="chapters" v-loading="loading" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="name" label="章节名称" />
          <el-table-column prop="textbookName" label="所属课本" />
          <el-table-column prop="creatorName" label="创建人" />
          <el-table-column prop="createTime" label="创建时间" />
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <el-button size="small" @click="editChapter(scope.row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button size="small" type="danger" @click="deleteChapter(scope.row.id!)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-empty v-else description="请先选择课本，然后在此处管理对应课本的章节" />
    </el-card>

    <!-- 新建/编辑章节对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="isEditing ? '编辑章节' : '新建章节'"
      width="500px"
    >
      <el-form
        ref="chapterFormRef"
        :model="chapterForm"
        :rules="chapterRules"
        label-width="100px"
      >
        <el-form-item label="所属课本" prop="textbookId">
          <el-select
            v-model="chapterForm.textbookId"
            placeholder="请选择课本"
            style="width: 100%"
            :disabled="isEditing"
          >
            <el-option
              v-for="textbook in textbooks"
              :key="textbook.id"
              :label="textbook.name"
              :value="textbook.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="章节名称" prop="name">
          <el-input
            v-model="chapterForm.name"
            placeholder="请输入章节名称"
            maxlength="30"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button
          type="primary"
          @click="submitChapter"
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
import { getAllChapters, createChapter, updateChapter, deleteChapter } from '@/api/chapterApi'
import { getAllTextbooks } from '@/api/textbookApi'
import type { Chapter } from '@/types/Chapter'
import type { Textbook } from '@/types/Textbook'
import {getGrades} from "@/api/gradeApi";

// 表单引用
const chapterFormRef = ref<FormInstance>()

// 数据状态
const textbooks = ref<Textbook[]>([])
const chapters = ref<Chapter[]>([])
const loading = ref(false)
const submitting = ref(false)
const showCreateDialog = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const selectedTextbookId = ref<number | null>(null)

// 表单数据
const chapterForm = reactive({
  name: '',
  textbookId: null as number | null
})

// 表单验证规则
const chapterRules = reactive<FormRules>({
  textbookId: [
    { required: true, message: '请选择所属课本', trigger: 'change' }
  ],
  name: [
    { required: true, message: '请输入章节名称', trigger: 'blur' },
    { min: 1, max: 30, message: '章节名称长度应在1-30个字符之间', trigger: 'blur' }
  ]
})

// 加载课本列表
const loadTextbooks = async () => {
  try {
    // 首先加载年级列表以获取第一个年级
    const gradeResponse = await getGrades();
    if (gradeResponse.data.success) {
      const grades = gradeResponse.data.data
      if (grades.length > 0) {
        // 使用第一个年级ID加载课本
        const response = await getAllTextbooks(grades[0].id)
        if (response.data.success) {
          textbooks.value = response.data.data

          // 默认选中第一个课本
          if (textbooks.value.length > 0 && !selectedTextbookId.value) {
            selectedTextbookId.value = textbooks.value[0].id
            loadChapters()
          }
        } else {
          ElMessage.error(response.data.message || '加载课本列表失败')
        }
      }
    } else {
      ElMessage.error(gradeResponse.data.message || '加载年级列表失败')
    }
  } catch (error) {
    ElMessage.error('加载课本列表失败')
  }
}

// 加载章节列表
const loadChapters = async () => {
  if (!selectedTextbookId.value) return

  loading.value = true
  try {
    const response = await getAllChapters(selectedTextbookId.value)
    if (response.data.success) {
      chapters.value = response.data.data
    } else {
      ElMessage.error(response.data.message || '加载章节列表失败')
    }
  } catch (error) {
    ElMessage.error('加载章节列表失败')
  } finally {
    loading.value = false
  }
}

// 提交章节表单
const submitChapter = async () => {
  if (!chapterFormRef.value) return

  try {
    const valid = await chapterFormRef.value.validate()
    if (!valid) return

    submitting.value = true

    // 设置当前选中的课本ID
    if (!chapterForm.textbookId) {
      chapterForm.textbookId = selectedTextbookId.value
    }

    let response
    if (isEditing.value && editingId.value) {
      response = await updateChapter({
        ...chapterForm,
        id: editingId.value
      })
    } else {
      response = await createChapter(chapterForm)
    }

    if (response.data.success) {
      ElMessage.success(isEditing.value ? '编辑成功' : '创建成功')
      showCreateDialog.value = false
      resetForm()
      loadChapters()
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

// 编辑章节
const editChapter = (chapter: Chapter) => {
  isEditing.value = true
  editingId.value = chapter.id || null
  chapterForm.name = chapter.name
  chapterForm.textbookId = chapter.textbookId
  showCreateDialog.value = true
}

// 删除章节
const deleteChapter = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个章节吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await deleteChapter(id)
    if (response.data.success) {
      ElMessage.success('删除成功')
      loadChapters()
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error) {
    // 用户取消删除
  }
}

// 重置表单
const resetForm = () => {
  chapterForm.name = ''
  chapterForm.textbookId = selectedTextbookId.value
  isEditing.value = false
  editingId.value = null
}

// 初始化加载数据
onMounted(() => {
  loadTextbooks()
})
</script>

<style scoped>
.chapter-management {
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
