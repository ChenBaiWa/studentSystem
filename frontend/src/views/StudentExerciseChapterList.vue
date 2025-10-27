<template>
  <div class="student-exercise-chapter-list">
    <el-card class="header-card">
      <div class="header-content">
        <h2>{{ exerciseSet?.name }}</h2>
        <el-button type="primary" @click="goBack">返回</el-button>
      </div>
    </el-card>

    <!-- 章节列表 -->
    <el-card class="list-card">
      <el-table :data="chapters" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="章节名称" min-width="200" />
        <el-table-column prop="questionCount" label="题目数" width="100" />
        <el-table-column prop="totalScore" label="总分" width="100" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button 
              size="small" 
              type="primary" 
              @click="startExercise(scope.row)"
            >
              开始做题
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && chapters.length === 0" description="暂无章节数据" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { getExerciseSet, getStudentChapters } from '@/api/exerciseSetApi'

// 数据状态
const exerciseSet = ref<any>(null)
const chapters = ref<any[]>([])
const loading = ref(false)

// 路由
const route = useRoute()
const router = useRouter()

// 初始化
onMounted(() => {
  const exerciseSetId = Number(route.params.exerciseSetId)
  if (exerciseSetId && !isNaN(exerciseSetId)) {
    loadExerciseSet(exerciseSetId)
    loadChapters(exerciseSetId)
  } else {
    ElMessage.error('参数错误')
    goBack()
  }
})

// 加载习题集信息
const loadExerciseSet = async (exerciseSetId: number) => {
  try {
    const response = await getExerciseSet(exerciseSetId)
    if (response.success) {
      exerciseSet.value = response.data
    } else {
      ElMessage.error(response.message || '习题集信息加载失败')
    }
  } catch (error: any) {
    console.error('加载习题集信息失败:', error)
    ElMessage.error(error.message || '加载习题集信息失败')
  }
}

// 加载章节列表
const loadChapters = async (exerciseSetId: number) => {
  try {
    loading.value = true
    const response = await getStudentChapters(exerciseSetId)
    
    if (response.success) {
      chapters.value = response.data
      console.log('章节数据:', response.data)
    } else {
      ElMessage.error(response.message || '章节数据加载失败')
      chapters.value = []
    }
  } catch (error: any) {
    console.error('加载章节列表失败:', error)
    ElMessage.error(error.message || '加载章节列表失败')
    chapters.value = []
  } finally {
    loading.value = false
  }
}

// 开始做题
const startExercise = (chapter: any) => {
  router.push(`/student/exercise-sets/${chapter.exerciseSetId}/chapters/${chapter.id}`)
}

// 返回上一页
const goBack = () => {
  router.push('/student/exercise-sets')
}
</script>

<style scoped>
.student-exercise-chapter-list {
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

.list-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
</style>