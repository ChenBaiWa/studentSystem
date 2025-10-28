<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>学生管理系统</h2>
        <p>用户登录</p>
      </div>
      
      <el-form 
        ref="loginForm" 
        :model="loginData" 
        :rules="loginRules" 
        label-width="0px"
        class="login-form"
      >
        <el-form-item prop="phone">
          <el-input 
            v-model="loginData.phone" 
            placeholder="请输入手机号"
            maxlength="11"
            @input="validatePhone"
            size="large"
          >
            <template #prefix>
              <el-icon><Iphone /></el-icon>
            </template>
          </el-input>
          <div v-if="phoneError" class="error-text">{{ phoneError }}</div>
        </el-form-item>

        <el-form-item prop="password">
          <el-input 
            v-model="loginData.password" 
            :type="passwordVisible ? 'text' : 'password'"
            placeholder="请输入密码"
            @keyup.enter="handleLogin"
            size="large"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
            <template #suffix>
              <el-icon @click="togglePasswordVisibility">
                <View v-if="passwordVisible" />
                <Hide v-else />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="rememberMe">记住我</el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button 
            type="primary" 
            class="login-btn"
            @click="handleLogin"
            :loading="loading"
            :disabled="loading"
            size="large"
          >
            登录
          </el-button>
        </el-form-item>

        <div class="links">
          <el-link type="primary" @click="$router.push('/register')">还没有账号？立即注册</el-link>
          <el-link type="info" @click="handleForgotPassword">忘记密码？</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Iphone, Lock, View, Hide } from '@element-plus/icons-vue'
import { authApi } from '@/api/authApi'

const router = useRouter()
const loginForm = ref<FormInstance>()

// 登录表单数据
const loginData = reactive({
  phone: '',
  password: ''
})

// 登录状态
const loading = ref(false)
const passwordVisible = ref(false)
const rememberMe = ref(false)
const phoneError = ref('')

// 表单验证规则
const loginRules = reactive<FormRules>({
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (!/^1[3-9]\d{9}$/.test(value)) {
          callback(new Error('请输入正确的手机号'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
})

// 切换密码可见性
const togglePasswordVisibility = () => {
  passwordVisible.value = !passwordVisible.value
}

// 手机号实时验证
const validatePhone = () => {
  if (loginData.phone && !/^1[3-9]\d{9}$/.test(loginData.phone)) {
    phoneError.value = '请输入正确手机号'
  } else {
    phoneError.value = ''
  }
}

// 处理登录
const handleLogin = async () => {
  if (!loginForm.value) return
  
  try {
    const valid = await loginForm.value.validate()
    if (!valid) return
    
    loading.value = true
    
    // 调用登录API
    const response = await authApi.login({
      phone: loginData.phone,
      password: loginData.password
    })
    
    // 在handleLogin方法中，登录成功后添加：
  if (response.success) {
  // 登录成功处理
  ElMessage.success('登录成功')
  
  // 根据角色存储登录状态
  if (rememberMe.value) {
    // 记住我 - 存储在localStorage中
    localStorage.setItem('token', response.data?.token || '')
    localStorage.setItem('userRole', response.data?.userRole.toString() || '')
    localStorage.setItem('userId', response.data?.id.toString() || '')
    localStorage.setItem('realName', response.data?.realName || '')
  } else {
    // 不记住我 - 存储在sessionStorage中
    sessionStorage.setItem('token', response.data?.token || '')
    sessionStorage.setItem('userRole', response.data?.userRole.toString() || '')
    sessionStorage.setItem('userId', response.data?.id.toString() || '')
    sessionStorage.setItem('realName', response.data?.realName || '')
  }
  
  // 通知其他组件更新用户信息
  window.dispatchEvent(new Event('user-login'))
  
  // 根据角色跳转到不同首页
  if (response.data?.userRole === 1) {
    // 老师角色 - 跳转到年级管理页面
    router.push('/grades')
  } else if (response.data?.userRole === 2) {
    // 学生角色 - 跳转到学生习题集列表页面
    router.push('/student/exercise-sets')
  }
}
  } catch (error) {
    ElMessage.error('登录过程中发生错误')
  } finally {
    loading.value = false
  }
}

// 忘记密码处理
const handleForgotPassword = () => {
  ElMessageBox.alert('请联系管理员重置密码', '忘记密码', {
    confirmButtonText: '确定',
    type: 'info'
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
}

.login-box {
  width: 400px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.login-header {
  padding: 30px 0 20px;
  text-align: center;
  background: #409EFF;
  color: white;
}

.login-header h2 {
  margin: 0 0 10px;
  font-size: 24px;
}

.login-header p {
  margin: 0;
  font-size: 16px;
  opacity: 0.9;
}

.login-form {
  padding: 30px;
}

.login-btn {
  width: 100%;
}

.links {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
}

.error-text {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 5px;
  text-align: left;
}

.el-icon {
  cursor: pointer;
}
</style>