<template>
  <div class="student-exercise-set-list">
    <el-card class="header-card">
      <div class="header-content">
        <h2>习题集列表</h2>
      </div>
    </el-card>

    <!-- 习题集列表 -->
    <el-card class="list-card">
      <el-table :data="exerciseSets" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="习题集名称" min-width="200" />
        <el-table-column prop="subject" label="学科" width="120" />
        <el-table-column prop="questionCount" label="题目数" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
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
      <el-empty v-if="!loading && exerciseSets.length === 0" description="暂无习题集数据" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getPublishedExerciseSets } from '@/api/exerciseSetApi'

// 数据状态
const exerciseSets = ref<any[]>([])
const loading = ref(false)

// 路由
const router = useRouter()

// 初始化
onMounted(() => {
  loadExerciseSets()
})

// 加载习题集列表
const loadExerciseSets = async () => {
  try {
    loading.value = true
    const response = await getPublishedExerciseSets()
    
    if (response.success) {
      exerciseSets.value = response.data
    } else {
      ElMessage.error(response.message || '习题集数据加载失败')
      exerciseSets.value = []
    }
  } catch (error: any) {
    console.error('加载习题集列表失败:', error)
    ElMessage.error(error.message || '加载习题集列表失败')
    exerciseSets.value = []
  } finally {
    loading.value = false
  }
}

// 开始做题
const startExercise = (exerciseSet: any) => {
  // 直接跳转到做题页面，跳过章节选择步骤
  router.push(`/student/exercise-sets/${exerciseSet.id}/direct`)
}

// 格式化时间
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`
}
</script>

<style scoped>
.student-exercise-set-list {
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