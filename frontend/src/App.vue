<template>
  <div id="app">
    <!-- 登录/注册页面布局 -->
    <router-view v-if="isAuthRoute" />

    <!-- 管理系统页面布局 -->
    <el-container v-else class="layout-container">
      <el-header class="header">
        <div class="logo">学生管理系统</div>
        <div class="user-info">
          <el-dropdown @command="handleUserCommand">
            <span class="el-dropdown-link">
              <el-avatar size="small" :icon="userRole === 1 ? 'User' : 'UserFilled'" />
              <span class="username">{{ realName }}</span>
              <el-icon class="el-icon--right">
                <arrow-down />
              </el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-container>
        <el-aside width="200px">
          <sidebar-menu />
        </el-aside>

        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown } from '@element-plus/icons-vue'
import SidebarMenu from '@/components/SidebarMenu.vue'

const route = useRoute()
const router = useRouter()

// 用户信息
const userRole = ref(0)
const realName = ref('')

// 判断是否为认证相关路由（登录/注册）
const isAuthRoute = computed(() => {
  return route.path === '/login' || route.path === '/register'
})

// 获取用户信息
const getUserInfo = () => {
  userRole.value = parseInt(localStorage.getItem('userRole') || sessionStorage.getItem('userRole') || '0')
  realName.value = localStorage.getItem('realName') || sessionStorage.getItem('realName') || '未知用户'
}

// 监听用户登录事件
const handleUserLogin = () => {
  getUserInfo()
}

onMounted(() => {
  // 检查是否有有效的登录状态
  const token = localStorage.getItem('token') || sessionStorage.getItem('token')
  if (!token && !isAuthRoute.value) {
    // 如果没有token且不在认证路由上，跳转到登录页
    router.push('/login')
  } else if (token) {
    // 获取用户信息
    getUserInfo()
  }
  
  // 监听用户登录事件
  window.addEventListener('user-login', handleUserLogin)
})

// 在组件卸载时移除事件监听器
onUnmounted(() => {
  window.removeEventListener('user-login', handleUserLogin)
})

const handleUserCommand = (command: string) => {
  if (command === 'logout') {
    // 清除登录状态
    localStorage.removeItem('token')
    sessionStorage.removeItem('token')
    localStorage.removeItem('userRole')
    sessionStorage.removeItem('userRole')
    localStorage.removeItem('realName')
    sessionStorage.removeItem('realName')
    localStorage.removeItem('userId')
    sessionStorage.removeItem('userId')
    // 更新用户信息显示
    userRole.value = 0
    realName.value = '未知用户'
    // 跳转到登录页
    router.push('/login')
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  height: 100vh;
}

.layout-container {
  height: 100vh;
}

.header {
  background-color: #409EFF;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  z-index: 100;
}

.logo {
  font-size: 20px;
  font-weight: bold;
}

.user-info {
  display: flex;
  align-items: center;
}

.username {
  margin: 0 10px;
}

.el-main {
  padding: 20px;
  background-color: #f5f5f5;
}

.el-aside {
  background-color: #409EFF;
  color: #333;
}
</style>