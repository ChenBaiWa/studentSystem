<template>
  <div class="register-container">
    <!-- 步骤1: 角色选择 -->
    <div v-if="step === 1" class="role-selection">
      <h2>请选择您的角色</h2>
      <div class="role-buttons">
        <el-button 
          type="primary" 
          size="large" 
          :class="{ 'active-role': selectedRole === 1 }"
          @click="selectRole(1)"
        >
          我是老师
        </el-button>
        <el-button 
          type="success" 
          size="large" 
          :class="{ 'active-role': selectedRole === 2 }"
          @click="selectRole(2)"
        >
          我是学生
        </el-button>
      </div>
      <el-button 
        type="primary" 
        size="large" 
        :disabled="!selectedRole" 
        @click="step = 2"
        class="next-btn"
      >
        下一步
      </el-button>
    </div>

    <!-- 步骤2: 注册信息填写 -->
    <div v-if="step === 2" class="register-form">
      <h2>填写注册信息</h2>
      <el-form 
        ref="registerForm" 
        :model="formData" 
        :rules="formRules" 
        label-width="100px"
      >
        <el-form-item label="手机号" prop="phone">
          <el-input 
            v-model="formData.phone" 
            placeholder="请输入11位手机号"
            maxlength="11"
            @input="validatePhone"
          />
          <div v-if="phoneError" class="error-text">{{ phoneError }}</div>
          <div v-else class="input-tip">请输入11位有效手机号</div>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="formData.password" 
            type="password" 
            placeholder="6-16位字母/数字/符号"
            show-password
          />
          <div class="input-tip">密码长度需6-16位</div>
        </el-form-item>

        <el-form-item label="真实姓名" prop="realName">
          <el-input 
            v-model="formData.realName" 
            placeholder="2-10位汉字/字母"
          />
        </el-form-item>

        <el-form-item>
          <el-button @click="step = 1">上一步</el-button>
          <el-button 
            type="primary" 
            @click="submitRegister"
            :loading="loading"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 注册结果弹窗 -->
    <el-dialog 
      v-model="resultDialogVisible" 
      :title="resultDialogTitle" 
      width="30%"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <span>{{ resultDialogMessage }}</span>
      <template #footer>
        <el-button type="primary" @click="handleResultDialog">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { authApi } from '@/api/authApi'

const router = useRouter()
const registerForm = ref<FormInstance>()

// 步骤控制
const step = ref(1)
const selectedRole = ref<number | null>(null)

// 表单数据
const formData = reactive({
  phone: '',
  password: '',
  realName: ''
})

// 表单验证规则
const formRules = reactive<FormRules>({
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (!/^1[3-9]\d{9}$/.test(value)) {
          callback(new Error('请输入11位有效手机号'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value.length < 6 || value.length > 16) {
          callback(new Error('密码长度需6-16位'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (!value || value.trim().length === 0) {
          callback(new Error('请输入真实姓名'))
        } else if (value.length < 2 || value.length > 10) {
          callback(new Error('姓名长度需2-10位'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

// 手机号实时验证
const phoneError = ref('')
const validatePhone = () => {
  if (!/^1[3-9]\d{9}$/.test(formData.phone)) {
    phoneError.value = '请输入11位有效手机号'
  } else {
    phoneError.value = ''
  }
}

// 注册状态
const loading = ref(false)
const resultDialogVisible = ref(false)
const resultDialogTitle = ref('')
const resultDialogMessage = ref('')

// 选择角色
const selectRole = (role: number) => {
  selectedRole.value = role
}

// 提交注册
const submitRegister = async () => {
  if (!registerForm.value) return
  
  try {
    // 验证表单
    const valid = await registerForm.value.validate()
    if (!valid) return
    
    loading.value = true
    
    // 调用注册API
    const response = await authApi.register({
      ...formData,
      userRole: selectedRole.value!
    })
    
    if (response.success) {
      // 注册成功处理
      resultDialogTitle.value = '注册成功'
      resultDialogMessage.value = '注册成功，即将跳转登录页'
      resultDialogVisible.value = true
      
      // 3秒后自动跳转
      setTimeout(() => {
        router.push('/login')
      }, 3000)
    } else {
      // 注册失败处理
      resultDialogTitle.value = '注册失败'
      resultDialogMessage.value = response.message || '系统错误，请稍后重试'
      resultDialogVisible.value = true
      formData.password = '' // 清空密码
    }
  } catch (error) {
    // 表单验证失败
    ElMessage.error('表单验证失败，请检查输入')
  } finally {
    loading.value = false
  }
}

// 处理结果弹窗
const handleResultDialog = () => {
  resultDialogVisible.value = false
  if (resultDialogTitle.value === '注册成功') {
    router.push('/login')
  }
}
</script>

<style scoped>
.register-container {
  max-width: 500px;
  margin: 50px auto;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.role-selection, .register-form {
  text-align: center;
}

.role-buttons {
  margin: 30px 0;
  display: flex;
  justify-content: center;
  gap: 20px;
}

.active-role {
  transform: scale(1.05);
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);
}

.next-btn {
  margin-top: 20px;
}

.error-text {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 5px;
}

.input-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 5px;
  text-align: left;
}
</style>