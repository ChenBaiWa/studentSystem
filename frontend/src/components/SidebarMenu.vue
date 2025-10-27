<template>
  <div class="sidebar-menu">
    <el-menu
      :default-active="activeIndex"
      class="el-menu-vertical"
      @select="handleSelect"
      :unique-opened="true"
    >
      <el-sub-menu index="1" v-if="userRole === 1">
        <template #title>
          <el-icon><OfficeBuilding /></el-icon>
          <span>教学管理</span>
        </template>
        <el-menu-item index="/grades">年级管理</el-menu-item>
        <el-menu-item index="/textbooks">课本管理</el-menu-item>
        <el-menu-item index="/chapters">章节管理</el-menu-item>
        <el-menu-item index="/exercise-sets">习题集管理</el-menu-item>
      </el-sub-menu>

      <el-menu-item index="/classes" >
        <el-icon><House /></el-icon>
        <span>班级管理</span>
      </el-menu-item>

      <el-menu-item index="/assignments" v-if="userRole === 1">
        <el-icon><Document /></el-icon>
        <span>作业管理</span>
      </el-menu-item>

      <el-menu-item index="/student-assignments" v-if="userRole === 2">
        <el-icon><Document /></el-icon>
        <span>我的作业</span>
      </el-menu-item>
      
      <el-menu-item index="/pdf-upload" v-if="userRole === 1">
        <el-icon><Upload /></el-icon>
        <span>PDF上传解析</span>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { OfficeBuilding, House, Document, Upload } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 从本地存储获取用户角色，1为老师，2为学生
const userRole = ref(Number(localStorage.getItem('userRole')) || Number(sessionStorage.getItem('userRole')) || 1)

const activeIndex = ref(route.path)

// 监听路由变化
watch(route, () => {
  activeIndex.value = route.path
})

// 监听用户登录事件
const handleUserLogin = () => {
  userRole.value = Number(localStorage.getItem('userRole')) || Number(sessionStorage.getItem('userRole')) || 1
}

onMounted(() => {
  // 添加事件监听器
  window.addEventListener('user-login', handleUserLogin)
})

onUnmounted(() => {
  // 移除事件监听器
  window.removeEventListener('user-login', handleUserLogin)
})

const handleSelect = (index: string) => {
  router.push(index)
}
</script>

<style scoped>
.sidebar-menu {
  height: 100%;
  overflow-y: auto;
}

.el-menu-vertical {
  height: 100%;
  border-right: none;
}

.el-menu-item,
.el-sub-menu__title {
  font-size: 14px;
}

.el-menu-item .el-icon,
.el-sub-menu__title .el-icon {
  margin-right: 10px;
}

.el-menu-item:hover {
  background-color: #66b1ff !important;
}

.el-sub-menu .el-menu-item {
  padding-left: 48px !important;
}
</style>