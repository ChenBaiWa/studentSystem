import { createRouter, createWebHashHistory } from 'vue-router'
import StudentList from '../views/StudentList.vue'
import Register from '../views/Register.vue'
import Login from '../views/Login.vue'
import GradeManagement from '../views/GradeManagement.vue'
import TextbookManagement from '../views/TextbookManagement.vue'
import ChapterManagement from '../views/ChapterManagement.vue'
import ClassManagement from '../views/ClassManagement.vue'
import AssignmentManagement from '../views/AssignmentManagement.vue'
import StudentAssignmentList from '../views/StudentAssignmentList.vue'
import DoubaoFileAnalysis from '../views/DoubaoFileAnalysis.vue'
import ExerciseSetManagement from '../views/ExerciseSetManagement.vue'
import QuestionManagement from '../views/QuestionManagement.vue'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      name: 'AssignmentManagement',
      component: AssignmentManagement
    },
    {
      path: '/register',
      name: 'Register',
      component: Register
    },
    {
      path: '/login',
      name: 'Login',
      component: Login
    },
    {
      path: '/grades',
      name: 'GradeManagement',
      component: GradeManagement
    },
    {
      path: '/textbooks',
      name: 'TextbookManagement',
      component: TextbookManagement
    },
    {
      path: '/chapters',
      name: 'ChapterManagement',
      component: ChapterManagement
    },
    {
      path: '/classes',
      name: 'ClassManagement',
      component: ClassManagement
    },
    {
      path: '/assignments',
      name: 'AssignmentManagement',
      component: AssignmentManagement
    },
    {
      path: '/student-assignments',
      name: 'StudentAssignmentList',
      component: StudentAssignmentList
    },
    {
      path: '/doubao-analysis',
      name: 'DoubaoFileAnalysis',
      component: DoubaoFileAnalysis
    },
    {
      path: '/exercise-sets',
      name: 'ExerciseSetManagement',
      component: ExerciseSetManagement
    },
    {
      path: '/exercise-sets/:id/questions',
      name: 'QuestionManagement',
      component: QuestionManagement,
      props: true
    }
  ]
})
// 路由守卫：检查权限
router.beforeEach((to, from, next) => {
  // 检查是否有token（从localStorage或sessionStorage）
  const token = localStorage.getItem('token') || sessionStorage.getItem('token')

  if (to.meta.requiresAuth && !token) {
    // 需要认证但未登录，跳转到登录页
    next('/login')
  } else if (to.path === '/login' && token) {
    // 已登录但访问登录页，跳转到首页
    next('/')
  } else {
    // 正常导航
    next()
  }
})
export default router