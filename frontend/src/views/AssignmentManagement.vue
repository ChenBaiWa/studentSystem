<template>
  <div class="assignment-management">
    <el-card class="header-card">
      <div class="header-content">
        <h2>作业管理</h2>
        <el-button type="primary" @click="handlePublish">发布作业</el-button>
      </div>
    </el-card>

    <!-- 筛选条件 -->
<!--    <el-card class="filter-card">-->
<!--      <el-form :inline="true" :model="filterForm" class="filter-form">-->
<!--        <el-form-item label="班级">-->
<!--          <el-select v-model="filterForm.classId" placeholder="请选择班级" clearable>-->
<!--            <el-option-->
<!--              v-for="item in classes"-->
<!--              :key="item.id"-->
<!--              :label="item.name"-->
<!--              :value="item.id"-->
<!--            />-->
<!--          </el-select>-->
<!--        </el-form-item>-->
<!--        <el-form-item label="状态">-->
<!--          <el-select v-model="filterForm.status" placeholder="请选择状态" clearable>-->
<!--            <el-option label="全部" value="" />-->
<!--            <el-option label="待批改" value="pending" />-->
<!--            <el-option label="已完成" value="completed" />-->
<!--          </el-select>-->
<!--        </el-form-item>-->
<!--        <el-form-item>-->
<!--          <el-button type="primary" @click="loadAssignments">搜索</el-button>-->
<!--          <el-button @click="resetFilter">重置</el-button>-->
<!--        </el-form-item>-->
<!--      </el-form>-->
<!--    </el-card>-->

    <!-- 作业列表 -->
    <el-card class="list-card">
      <el-table :data="assignments" style="width: 100%" v-loading="loading">
        <el-table-column prop="title" label="作业标题" min-width="150" />
        <el-table-column prop="className" label="所属班级" width="120" />
        <el-table-column prop="chapterName" label="所属章节" width="150" />
        <el-table-column prop="totalScore" label="总分" width="80" />
        <el-table-column prop="deadline" label="截止时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.deadline) }}
          </template>
        </el-table-column>
        <el-table-column label="提交情况" width="150">
          <template #default="scope">
            <span>{{ scope.row.submissionCount || 0 }}/{{ scope.row.totalStudents || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewDetails(scope.row)">查看详情</el-button>
            <el-button
              size="small"
              @click="handleEdit(scope.row)"
              :disabled="isPastDeadline(scope.row.deadline)"
            >
              编辑
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDelete(scope.row)"
              :disabled="scope.row.submissionCount > 0"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-if="total > 0"
          :current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next, jumper"
          @current-change="handlePageChange"
        />
      </div>
      <el-empty v-if="!loading && assignments.length === 0" description="暂无作业数据" />
    </el-card>

    <!-- 发布作业弹窗 -->
    <el-dialog
      v-model="publishDialogVisible"
      title="发布作业"
      width="600px"
      :before-close="handlePublishClose"
    >
      <el-steps :active="publishStep" finish-status="success" simple>
        <el-step title="基本信息" />
        <el-step title="作业内容" />
      </el-steps>

      <el-form
        ref="publishFormRef"
        :model="publishForm"
        :rules="publishRules"
        label-width="100px"
        class="publish-form"
      >
        <!-- 第一步：基本信息 -->
        <div v-show="publishStep === 0">
          <el-form-item label="作业标题" prop="title">
            <el-input
              v-model="publishForm.title"
              placeholder="请输入作业标题，如：1.1 正数和负数练习"
              maxlength="30"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="所属班级" prop="classIds">
            <el-select
              v-model="publishForm.classIds"
              placeholder="请选择班级"
              multiple
              style="width: 100%"
            >
              <el-option
                v-for="item in classes"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="所属年级" prop="gradeId">
            <el-select
              v-model="publishForm.gradeId"
              placeholder="请选择年级"
              style="width: 100%"
              @change="handleGradeChange"
            >
              <el-option
                v-for="item in grades"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="所属课本" prop="textbookId">
            <el-select
              v-model="publishForm.textbookId"
              placeholder="请选择课本"
              style="width: 100%"
              @change="handleTextbookChange"
              :disabled="!publishForm.gradeId"
              filterable
            >
              <el-option
                v-for="item in textbooks"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="所属章节" prop="chapterId">
            <el-select
              v-model="publishForm.chapterId"
              placeholder="请选择章节"
              style="width: 100%"
              :disabled="!publishForm.textbookId"
              filterable
            >
              <el-option
                v-for="item in chapters"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="作业总分" prop="totalScore">
            <el-input-number
              v-model="publishForm.totalScore"
              :min="1"
              :max="100"
              controls-position="right"
            />
          </el-form-item>

          <el-form-item label="截止时间" prop="deadline">
            <el-date-picker
              v-model="publishForm.deadline"
              type="datetime"
              placeholder="请选择截止时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              :disabled-date="disabledDate"
              style="width: 100%"
            />
          </el-form-item>
        </div>

        <!-- 第二步：作业内容 -->
        <div v-show="publishStep === 1">
          <el-form-item label="作业内容" prop="content">
            <el-input
              v-model="publishForm.content"
              type="textarea"
              :rows="8"
              placeholder="请输入作业内容，如：完成课本 P20 页第 1-5 题，拍照提交手写答案"
              maxlength="1000"
              show-word-limit
            />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button v-if="publishStep > 0" @click="publishStep--">上一步</el-button>
          <el-button
            v-if="publishStep < 1"
            type="primary"
            @click="nextStep"
          >
            下一步
          </el-button>
          <el-button
            v-if="publishStep === 1"
            type="primary"
            @click="submitPublish"
            :loading="publishLoading"
          >
            确认发布
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 作业预览弹窗 -->
    <el-dialog
      v-model="previewDialogVisible"
      title="作业预览"
      width="600px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="作业标题">{{ publishForm.title }}</el-descriptions-item>
        <el-descriptions-item label="所属班级">
          <el-tag
            v-for="classId in publishForm.classIds"
            :key="classId"
            style="margin-right: 5px;"
          >
            {{ getClassNameById(classId) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="所属章节">{{ getChapterNameById(publishForm.chapterId) }}</el-descriptions-item>
        <el-descriptions-item label="作业总分">{{ publishForm.totalScore }}分</el-descriptions-item>
        <el-descriptions-item label="截止时间">{{ publishForm.deadline }}</el-descriptions-item>
        <el-descriptions-item label="作业内容">
          <div style="white-space: pre-wrap;">{{ publishForm.content }}</div>
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="previewDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 作业详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="作业详情"
      width="800px"
    >
      <div v-if="currentAssignment">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="作业标题">{{ currentAssignment.title }}</el-descriptions-item>
          <el-descriptions-item label="所属班级">{{ currentAssignment.className }}</el-descriptions-item>
          <el-descriptions-item label="所属章节">{{ currentAssignment.chapterName }}</el-descriptions-item>
          <el-descriptions-item label="总分">{{ currentAssignment.totalScore }}分</el-descriptions-item>
          <el-descriptions-item label="截止时间">{{ formatDate(currentAssignment.deadline) }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ formatDate(currentAssignment.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="作业内容" :span="2">
            <div class="assignment-content">{{ currentAssignment.content }}</div>
          </el-descriptions-item>
        </el-descriptions>

        <h3 style="margin-top: 20px;">学生提交情况</h3>
        <el-table :data="studentSubmissions" style="width: 100%">
          <el-table-column prop="studentName" label="学生姓名" width="100" />
          <el-table-column prop="submitTime" label="提交时间" width="180">
            <template #default="scope">
              {{ scope.row.submitTime ? formatDate(scope.row.submitTime) : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="批改状态" width="100">
            <template #default="scope">
              <el-tag v-if="scope.row.status === 'submitted'" type="warning">待批改</el-tag>
              <el-tag v-else-if="scope.row.status === 'graded'" type="success">已批改</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="score" label="得分" width="80">
            <template #default="scope">
              {{ scope.row.score !== null ? scope.row.score : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button
                size="small"
                type="primary"
                @click="viewSubmission(scope.row)"
                :disabled="!scope.row.answer"
              >
                查看
              </el-button>
            </template>
          </el-table-column>
        </el-table>
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
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  getAssignments,
  getAssignment,
  publishAssignment,
  updateAssignment,
  deleteAssignment,
  getStudentSubmissions
} from '@/api/assignmentApi';
import { getTeacherClassList, getStudentClassList } from '@/api/classApi';
import { getAllChapters } from '@/api/chapterApi';
import { getAllTextbooks } from '@/api/textbookApi';
import { getGrades } from '@/api/gradeApi';

import type { FormInstance } from 'element-plus';

interface Assignment {
    id?: number;
    title: string;
    classId: number;
    className?: string;
    chapterId: number;
    chapterName?: string;
    content: string;
    totalScore: number;
    deadline: string;
    creatorId?: number;
    creatorName?: string;
    createTime?: string;
    updateTime?: string;
}

interface StudentAssignment {
    id?: number;
    assignmentId: number;
    studentId: number;
    studentName?: string;
    answer?: string;
    score?: number;
    feedback?: string;
    status?: 'submitted' | 'graded';
    submitTime?: string;
    gradeTime?: string;
    createTime?: string;
    updateTime?: string;
}

interface Class {
  id?: number
  name: string
  classCode?: string
  verificationCode?: string
  gradeId: number
  gradeName?: string
  creatorId?: number
  creatorName?: string
  createTime?: string
  updateTime?: string
}

interface Grade {
  id?: number
  name: string
  creatorId?: number
  creatorName?: string
  createTime?: string
  updateTime?: string
}

interface Textbook {
  id?: number
  name: string
  gradeId: number
  gradeName?: string
  creatorId?: number
  creatorName?: string
  createTime?: string
  updateTime?: string
}

interface Chapter {
  id?: number
  name: string
  textbookId: number
  textbookName?: string
  creatorId?: number
  creatorName?: string
  createTime?: string
  updateTime?: string
}

// 数据相关
const assignments = ref<Assignment[]>([]);
const classes = ref<Class[]>([]);
const grades = ref<Grade[]>([]);
const textbooks = ref<Textbook[]>([]);
const chapters = ref<Chapter[]>([]);
const studentSubmissions = ref<StudentAssignment[]>([]);
const loading = ref(false);
const publishLoading = ref(false);

// 分页相关
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

// 筛选表单
const filterForm = reactive({
  classId: undefined as number | undefined,
  status: ''
});

// 发布作业相关
const publishDialogVisible = ref(false);
const previewDialogVisible = ref(false);
const publishStep = ref(0);
const publishFormRef = ref<FormInstance>();
const detailDialogVisible = ref(false);
const currentAssignment = ref<Assignment | null>(null);

const publishForm = reactive({
  title: '',
  classIds: [] as number[],
  gradeId: undefined as number | undefined,
  textbookId: undefined as number | undefined,
  chapterId: undefined as number | undefined,
  content: '',
  totalScore: 100,
  deadline: ''
});

// 验证规则
const publishRules = {
  title: [{ required: true, message: '请输入作业标题', trigger: 'blur' }],
  classIds: [{ required: true, message: '请选择班级', trigger: 'change' }],
  gradeId: [{ required: true, message: '请选择年级', trigger: 'change' }],
  textbookId: [{ required: true, message: '请选择课本', trigger: 'change' }],
  chapterId: [{ required: true, message: '请选择章节', trigger: 'change' }],
  totalScore: [{ required: true, message: '请输入总分', trigger: 'change' }],
};

// 初始化
onMounted(() => {
  loadAssignments();
  loadClasses();
  loadGrades(); // 加载年级列表
});

// 加载作业列表
const loadAssignments = async () => {
    const params = {
      classId: filterForm.classId,
      status: filterForm.status || undefined
    };
    const response = await getAssignments(params);
    console.log(response)
    assignments.value = response;
    let count =0;
    for (var item of response){
      count++
    }
    total.value =count;
};

// 加载班级列表
const loadClasses = async () => {
  try {
    const response = await getTeacherClassList();
    if (response.data && response.data.success) {
      classes.value = response.data.data;
    } else {
      ElMessage.error(response.data?.message || '加载班级列表失败');
    }
  } catch (error) {
    console.error('加载班级列表失败:', error);
    ElMessage.error('加载班级列表失败');
  }
};

// 加载年级列表
const loadGrades = async () => {
  try {
    const response = await getGrades();
    if (response.data && response.data.success) {
      grades.value = response.data.data;
      // 默认加载第一个年级的课本
      if (grades.value.length > 0) {
        loadTextbooks(grades.value[0].id);
      }
    } else {
      ElMessage.error(response.data?.message || '加载年级列表失败');
    }
  } catch (error) {
    ElMessage.error('加载年级列表失败');
  }
};

// 加载课本列表
const loadTextbooks = async (gradeId: number) => {
  try {
    const response = await getAllTextbooks(gradeId);
    if (response.data && response.data.success) {
      textbooks.value = response.data.data;
    } else {
      ElMessage.error(response.data?.message || '加载课本列表失败');
      textbooks.value = [];
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载课本列表失败');
    textbooks.value = [];
  }
};

// 加载章节列表
const loadChapters = async (textbookId: number) => {
  try {
    const response = await getAllChapters(textbookId);
    if (response.data && response.data.success) {
      chapters.value = response.data.data;
    } else {
      ElMessage.error(response.data?.message || '加载章节列表失败');
      chapters.value = [];
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载章节列表失败');
    chapters.value = [];
  }
};

// 处理年级变更
const handleGradeChange = (gradeId: number) => {
  // 清空之前的课本和章节选择
  publishForm.textbookId = undefined;
  publishForm.chapterId = undefined;
  textbooks.value = [];
  chapters.value = [];

  // 如果选择了年级，则加载对应的课本列表
  if (gradeId) {
    loadTextbooks(gradeId);
  }
};

// 处理课本变更
const handleTextbookChange = (textbookId: number) => {
  // 清空之前的章节选择
  publishForm.chapterId = undefined;
  chapters.value = [];

  // 如果选择了课本，则加载对应的章节列表
  if (textbookId) {
    loadChapters(textbookId);
  }
};

// 重置筛选
const resetFilter = () => {
  filterForm.classId = undefined;
  filterForm.status = '';
  loadAssignments();
};

// 处理分页变化
const handlePageChange = (page: number) => {
  currentPage.value = page;
  loadAssignments();
};

// 处理发布作业
const handlePublish = () => {
  publishDialogVisible.value = true;
  publishStep.value = 0;
  resetPublishForm();
};

// 重置发布表单
const resetPublishForm = () => {
  publishForm.title = '';
  publishForm.classIds = [];
  publishForm.gradeId = undefined;
  publishForm.textbookId = undefined;
  publishForm.chapterId = undefined;
  publishForm.content = '';
  publishForm.totalScore = 100;
  publishForm.deadline = '';
  chapters.value = [];
};

// 下一步
const nextStep = async () => {
  if (!publishFormRef.value) return;

  try {
    await publishFormRef.value.validate();
    publishStep.value++;
  } catch (error) {
    console.log('表单验证失败:', error);
  }
};

// 预览作业
const previewAssignment = () => {
  previewDialogVisible.value = true;
};

// 获取班级名称
const getClassNameById = (classId: number) => {
  const cls = classes.value.find(c => c.id === classId);
  return cls ? cls.name : '';
};

// 获取章节名称
const getChapterNameById = (chapterId: number) => {
  const chapter = chapters.value.find(c => c.id === chapterId);
  return chapter ? chapter.name : '';
};

// 提交发布
const submitPublish = async () => {
  if (!publishFormRef.value) return;

  try {
    await publishFormRef.value.validate();
    publishLoading.value = true;

    const assignment = {
      title: publishForm.title,
      chapterId: publishForm.chapterId!,
      content: publishForm.content,
      totalScore: publishForm.totalScore,
      deadline: publishForm.deadline
    };

    const response = await publishAssignment(assignment, publishForm.classIds);
      ElMessage.success('作业发布成功');
      publishDialogVisible.value = false;
      await loadAssignments();
  }  finally {
    publishLoading.value = false;
  }
};

// 关闭发布弹窗
const handlePublishClose = () => {
  publishDialogVisible.value = false;
  resetPublishForm();
};

// 查看详情
const viewDetails = async (assignment: Assignment) => {
  try {
    currentAssignment.value = assignment;
    const response = await getStudentSubmissions(assignment.id!);
    studentSubmissions.value = response.data;
    detailDialogVisible.value = true;
  } catch (error) {
    ElMessage.error('加载作业详情失败');
  }
};

// 处理编辑
const handleEdit = async (assignment: Assignment) => {
  try {
    const response = await getAssignment(assignment.id!);
    currentAssignment.value = response.data;
    // 这里应该打开编辑弹窗，为简化直接提示
    ElMessage.info('编辑功能待实现');
  } catch (error) {
    ElMessage.error('加载作业信息失败');
  }
};

// 处理删除
const handleDelete = (assignment: Assignment) => {
  ElMessageBox.confirm(
    `确定要删除作业 "${assignment.title}" 吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteAssignment(assignment.id!);
      ElMessage.success('删除成功');
      loadAssignments();
    } catch (error: any) {
      ElMessage.error(error.response?.data || '删除失败');
    }
  }).catch(() => {
    // 用户取消删除
  });
};

// 查看学生提交
const viewSubmission = (submission: StudentAssignment) => {
  ElMessageBox.alert(submission.answer || '', '学生答案', {
    confirmButtonText: '确定'
  });
};

// 格式化日期
const formatDate = (dateString?: string) => {
  if (!dateString) return '-';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN');
};

// 禁用过去日期
const disabledDate = (date: Date) => {
  return date.getTime() < Date.now() - 86400000; // 禁用昨天之前的日期
};

// 判断是否超过截止时间
const isPastDeadline = (deadline: string) => {
  return new Date(deadline).getTime() < Date.now();
};
</script>

<style scoped>
.assignment-management {
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

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 15px;
}

.list-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.assignment-content {
  white-space: pre-wrap;
  line-height: 1.5;
}
</style>
