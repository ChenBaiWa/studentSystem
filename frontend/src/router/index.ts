import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import AssignmentManagement from '@/views/AssignmentManagement.vue'
import StudentAssignmentList from '@/views/StudentAssignmentList.vue'
import ClassManagement from '@/views/ClassManagement.vue'
import StudentList from '@/views/StudentList.vue'
import TextbookManagement from '@/views/TextbookManagement.vue'
import ChapterManagement from '@/views/ChapterManagement.vue'
import GradeManagement from '@/views/GradeManagement.vue'
import ExerciseSetManagement from '@/views/ExerciseSetManagement.vue'
import QuestionManagement from '@/views/QuestionManagement.vue'
import PdfUpload from '@/views/PdfUpload.vue'
import DoubaoPdfAnalysis from '@/views/DoubaoPdfAnalysis.vue'
import DoubaoFileAnalysis from '@/views/DoubaoFileAnalysis.vue'
import DoubaoImageAnalysis from '@/views/DoubaoImageAnalysis.vue'
import StudentExerciseSetList from '@/views/StudentExerciseSetList.vue'
import StudentExerciseChapterList from '@/views/StudentExerciseChapterList.vue'
import StudentExerciseQuestion from '@/views/StudentExerciseQuestion.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login },
  { path: '/register', component: Register },
  
  // 教师端路由
  { path: '/assignments', component: AssignmentManagement },
  { path: '/classes', component: ClassManagement },
  { path: '/students', component: StudentList },
  { path: '/textbooks', component: TextbookManagement },
  { path: '/chapters', component: ChapterManagement },
  { path: '/grades', component: GradeManagement },
  { path: '/exercise-sets', component: ExerciseSetManagement },
  { path: '/exercise-sets/:exerciseSetId/questions', component: QuestionManagement, props: true },
  { path: '/pdf-upload', component: PdfUpload },
  { path: '/doubao-pdf-analysis', component: DoubaoPdfAnalysis },
  { path: '/doubao-file-analysis', component: DoubaoFileAnalysis },
  { path: '/doubao-image-analysis', component: DoubaoImageAnalysis },
  
  // 学生端路由
  { path: '/student/assignments', component: StudentAssignmentList },
  { path: '/student-assignments', redirect: '/student/assignments' }, // 兼容旧路由
  { path: '/student/exercise-sets', component: StudentExerciseSetList },
  { path: '/student/exercise-sets/:exerciseSetId/chapters', component: StudentExerciseChapterList, props: true },
  { path: '/student/exercise-sets/:exerciseSetId/chapters/:chapterId/questions', component: StudentExerciseQuestion, props: true },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  // 检查是否有有效的token
  const token = localStorage.getItem('token') || sessionStorage.getItem('token')
  
  // 不需要登录的页面
  const noAuthRoutes = ['/login', '/register']
  
  if (!token && !noAuthRoutes.includes(to.path)) {
    // 未登录且访问需要登录的页面，跳转到登录页
    next('/login')
  } else if (token && noAuthRoutes.includes(to.path)) {
    // 已登录且访问登录页，根据角色跳转到对应首页
    const userRole = localStorage.getItem('userRole') || sessionStorage.getItem('userRole')
    if (userRole === '1') {
      // 老师
      next('/assignments')
    } else {
      // 学生
      next('/student/assignments')
    }
  } else {
    // 其他情况正常跳转
    next()
  }
})

export default router