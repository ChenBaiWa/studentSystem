<template>
  <div class="student-assignment-list">
    <el-card class="header-card">
      <div class="header-content">
        <h2>我的作业</h2>
      </div>
    </el-card>

    <!-- 作业列表 -->
    <el-card class="list-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="待提交" name="pending">
          <el-empty v-if="pendingAssignments.length === 0" description="暂无待提交的作业" />
          <el-table v-else :data="pendingAssignments" style="width: 100%">
            <el-table-column prop="title" label="作业标题" min-width="150" />
            <el-table-column prop="className" label="所属班级" width="120" />
            <el-table-column prop="chapterName" label="所属章节" width="150" />
            <el-table-column prop="createTime" label="发布时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="deadline" label="截止时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.deadline) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="scope">
                <el-button size="small" type="primary" @click="submitAssignment(scope.row)">
                  去完成
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="已提交" name="submitted">
          <el-empty v-if="submittedAssignments.length === 0" description="暂无已提交的作业" />
          <el-table v-else :data="submittedAssignments" style="width: 100%">
            <el-table-column prop="title" label="作业标题" min-width="150" />
            <el-table-column prop="className" label="所属班级" width="120" />
            <el-table-column prop="chapterName" label="所属章节" width="150" />
            <el-table-column prop="submitTime" label="提交时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.submitTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="批改状态" width="100">
              <template #default="scope">
                <el-tag v-if="scope.row.status === 'submitted'" type="warning">待批改</el-tag>
                <el-tag v-else-if="scope.row.status === 'graded'" type="success">已批改</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="scope">
                <el-button size="small" @click="viewSubmissionDetails(scope.row)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 提交作业弹窗 -->
    <el-dialog
      v-model="submitDialogVisible"
      title="提交作业"
      width="600px"
      :before-close="handleCloseSubmitDialog"
    >
      <div v-if="currentAssignment">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="作业标题">{{ currentAssignment.title }}</el-descriptions-item>
          <el-descriptions-item label="所属班级">{{ currentAssignment.className }}</el-descriptions-item>
          <el-descriptions-item label="所属章节">{{ currentAssignment.chapterName }}</el-descriptions-item>
          <el-descriptions-item label="作业内容">{{ currentAssignment.content }}</el-descriptions-item>
          <el-descriptions-item label="总分">{{ currentAssignment.totalScore }}分</el-descriptions-item>
          <el-descriptions-item label="截止时间">{{ formatDate(currentAssignment.deadline) }}</el-descriptions-item>
        </el-descriptions>

        <div class="upload-section">
          <h4>上传答案</h4>
          <div class="upload-area">
            <el-upload
              ref="uploadRef"
              v-model:file-list="fileList"
              list-type="picture-card"
              :auto-upload="false"
              :limit="5"
              :on-exceed="handleExceed"
              :on-preview="handlePicturePreview"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
            <p class="upload-tips">最多上传5张图片，支持JPG/PNG格式，单张不超过5MB</p>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="submitDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            @click="submitAssignmentAnswer"
            :loading="submitLoading"
            :disabled="fileList.length === 0"
          >
            提交
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 图片预览 -->
    <el-dialog v-model="previewVisible" title="图片预览">
      <img w-full :src="previewUrl" alt="预览图片" />
    </el-dialog>

    <!-- 作业详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="作业详情"
      width="800px"
    >
      <div v-if="currentSubmission">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="作业标题">{{ currentSubmission.assignmentTitle }}</el-descriptions-item>
          <el-descriptions-item label="所属班级">{{ currentSubmission.className }}</el-descriptions-item>
          <el-descriptions-item label="所属章节">{{ currentSubmission.chapterName }}</el-descriptions-item>
          <el-descriptions-item label="总分">{{ currentSubmission.totalScore }}分</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ formatDate(currentSubmission.submitTime) }}</el-descriptions-item>
          <el-descriptions-item label="截止时间">{{ formatDate(currentSubmission.deadline) }}</el-descriptions-item>
          <el-descriptions-item label="作业内容" :span="2">{{ currentSubmission.content }}</el-descriptions-item>
        </el-descriptions>

        <div class="answer-section">
          <h4>我的答案</h4>
          <div class="answer-images">
            <el-image
              v-for="(img, index) in submissionImages"
              :key="index"
              :src="img"
              :preview-src-list="submissionImages"
              :initial-index="index"
              fit="cover"
              class="answer-image"
              lazy
            />
          </div>
        </div>

        <div class="feedback-section">
          <h4>批改结果</h4>
          <div v-if="currentSubmission.status === 'submitted'" class="pending-feedback">
            <el-alert title="等待老师批改" type="warning" show-icon />
          </div>
          <div v-else-if="currentSubmission.status === 'graded'" class="graded-feedback">
            <el-descriptions :column="1">
              <el-descriptions-item label="得分">
                <el-tag type="success">{{ currentSubmission.score }}分</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="AI批注">{{ currentSubmission.feedback || '无' }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllStudentAssignments, getStudentAssignmentById, submitStudentAssignment } from '@/api/studentAssignmentApi'
import type { StudentAssignment } from '@/types/StudentAssignment'
import type { UploadFile, UploadFiles, UploadInstance } from 'element-plus'

// 数据相关
const activeTab = ref('pending')
const allAssignments = ref<StudentAssignment[]>([])
const loading = ref(false)

// 提交作业相关
const submitDialogVisible = ref(false)
const submitLoading = ref(false)
const currentAssignment = ref<StudentAssignment | null>(null)
const fileList = ref<UploadFile[]>([])
const uploadRef = ref<UploadInstance>()

// 详情相关
const detailDialogVisible = ref(false)
const currentSubmission = ref<StudentAssignment | null>(null)
const previewVisible = ref(false)
const previewUrl = ref('')

// 计算属性：待提交作业
const pendingAssignments = computed(() => {
  console.log('过滤前的所有作业:', allAssignments.value)
  const result = allAssignments.value.filter(a => {
    const isPending = a.status === 'pending'
    const notExpired = new Date(a.deadline).getTime() >= Date.now()
    console.log(`作业 ${a.title} - status: ${a.status}, deadline: ${a.deadline}, isPending: ${isPending}, notExpired: ${notExpired}`)
    return isPending && notExpired
  })
  console.log('过滤后的待提交作业:', result)
  return result
})

// 计算属性：已提交作业
const submittedAssignments = computed(() => {
  const result = allAssignments.value.filter(a => a.status === 'submitted' || a.status === 'graded')
  console.log('过滤后的已提交作业:', result)
  return result
})

// 格式化日期
const formatDate = (dateString?: string) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

// 初始化
onMounted(() => {
  loadAssignments()
})

// 加载作业列表
const loadAssignments = async () => {
  try {
    loading.value = true
    const response = await getAllStudentAssignments()
    console.log('API返回的原始数据:', response)
    
    // 处理后端返回的数据结构 { pending: [...], submitted: [...], expired: [...] }
    if (response.data) {
      const { pending = [], submitted = [], expired = [] } = response.data;
      
      // 为每个作业项添加状态字段
      const pendingWithStatus = pending.map((item: any) => ({
        ...item,
        status: 'pending'
      }));
      
      const submittedWithStatus = submitted.map((item: any) => ({
        ...item,
        status: 'submitted'
      }));
      
      const expiredWithStatus = expired.map((item: any) => ({
        ...item,
        status: 'expired'
      }));
      
      // 合并所有作业
      allAssignments.value = [...pendingWithStatus, ...submittedWithStatus, ...expiredWithStatus];
      console.log("allAssignments.value")
      console.log(allAssignments.value)
    } else {
      allAssignments.value = [];
    }
  } catch (error) {
    console.error('加载作业列表失败:', error)
    ElMessage.error('加载作业列表失败')
    allAssignments.value = []
  } finally {
    loading.value = false
  }
}

// 处理标签页切换
const handleTabChange = (tabName: string) => {
  activeTab.value = tabName
}

// 提交作业
const submitAssignment = (assignment: StudentAssignment) => {
  currentAssignment.value = assignment
  fileList.value = []
  submitDialogVisible.value = true
}

// 查看提交详情
const viewSubmissionDetails = (submission: StudentAssignment) => {
  currentSubmission.value = submission
  detailDialogVisible.value = true
}

// 文件超出限制
const handleExceed = (files: File[], uploadFiles: UploadFiles) => {
  ElMessage.warning(`最多上传5张图片，当前已上传${uploadFiles.length}张`)
}

// 图片预览
const handlePicturePreview = (uploadFile: UploadFile) => {
  previewUrl.value = uploadFile.url!
  previewVisible.value = true
}

// 提交作业答案
const submitAssignmentAnswer = async () => {
  if (!currentAssignment.value) return
  
  if (fileList.value.length === 0) {
    ElMessage.warning('请至少上传一张图片')
    return
  }
  
  // 检查截止时间
  if (new Date(currentAssignment.value.deadline).getTime() < Date.now()) {
    ElMessage.error('作业已截止，无法提交')
    return
  }
  
  try {
    submitLoading.value = true
    
    // 模拟图片上传，实际项目中应上传到服务器并获取URL
    const imageUrls = []
    for (let i = 0; i < fileList.value.length; i++) {
      // 在实际项目中，这里应该是真实的上传逻辑
      // 暂时使用模拟URL
      imageUrls.push(`/upload/homework/${Date.now()}_${i}.jpg`)
    }
    
    // 调用提交作业API
    const answer = imageUrls.join(',')
    await submitStudentAssignment(currentAssignment.value.id!, answer)
    
    ElMessage.success('作业提交成功')
    submitDialogVisible.value = false
    loadAssignments()
  } catch (error: any) {
    ElMessage.error(error.message || '作业提交失败')
  } finally {
    submitLoading.value = false
  }
}

// 关闭提交对话框
const handleCloseSubmitDialog = () => {
  submitDialogVisible.value = false
}

// 计算提交答案的图片URL列表
const submissionImages = computed(() => {
  if (!currentSubmission.value?.answer) return []
  return currentSubmission.value.answer.split(',').filter(url => url.trim() !== '')
})
</script>

<style scoped>
.student-assignment-list {
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
  margin-bottom: 20px;
}

.upload-section {
  margin-top: 20px;
}

.upload-area {
  text-align: center;
  padding: 20px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  background-color: #fafafa;
}

.upload-tips {
  margin-top: 10px;
  color: #999;
  font-size: 12px;
}

.answer-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.answer-image {
  width: 100px;
  height: 100px;
  border-radius: 4px;
}

.feedback-section {
  margin-top: 20px;
}

.pending-feedback,
.graded-feedback {
  margin-top: 10px;
}
</style>
