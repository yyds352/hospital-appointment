<template>
  <div class="register-container">
    <div class="register-card">
      <!-- 左侧欢迎区域 -->
      <div class="welcome-section">
        <div class="welcome-content">
          <div class="logo-container">
            <el-icon size="64" color="#1890ff"><FirstAidKit /></el-icon>
          </div>
          <h1>欢迎加入</h1>
          <p class="welcome-text">华商学院校医院系统</p>
          <div class="features">
            <div class="feature-item">
              <el-icon size="24" color="#52c41a"><CircleCheck /></el-icon>
              <span>便捷预约</span>
            </div>
            <div class="feature-item">
              <el-icon size="24" color="#1890ff"><Clock /></el-icon>
              <span>节省时间</span>
            </div>
            <div class="feature-item">
              <el-icon size="24" color="#fa8c16"><Star /></el-icon>
              <span>专业服务</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧注册表单 -->
      <div class="form-section">
        <div class="form-header">
          <h2>创建账户</h2>
          <p class="subtitle">请填写您的个人信息</p>
        </div>

        <el-form 
          ref="registerFormRef" 
          :model="registerForm" 
          :rules="registerRules" 
          class="register-form"
          @keyup.enter="handleRegister"
        >
          <!-- 账户信息 -->
          <div class="form-group">
            <h3 class="group-title">账户信息</h3>
            <div class="group-content">
              <el-form-item prop="username">
                <div class="input-group">
                  <el-icon class="input-icon"><User /></el-icon>
                  <el-input 
                    v-model="registerForm.username" 
                    placeholder="请输入用户名"
                    class="custom-input"
                    size="large"
                  />
                </div>
              </el-form-item>

              <el-form-item prop="password">
                <div class="input-group">
                  <el-icon class="input-icon"><Lock /></el-icon>
                  <el-input 
                    v-model="registerForm.password" 
                    type="password" 
                    placeholder="请输入密码"
                    class="custom-input"
                    size="large"
                    show-password
                  />
                </div>
              </el-form-item>

              <el-form-item prop="confirmPassword">
                <div class="input-group">
                  <el-icon class="input-icon"><Lock /></el-icon>
                  <el-input 
                    v-model="registerForm.confirmPassword" 
                    type="password" 
                    placeholder="请再次输入密码"
                    class="custom-input"
                    size="large"
                    show-password
                  />
                </div>
              </el-form-item>
            </div>
          </div>

          <!-- 个人信息 -->
          <div class="form-group">
            <h3 class="group-title">个人信息</h3>
            <div class="group-content">
              <el-form-item prop="name">
                <div class="input-group">
                  <el-icon class="input-icon"><Avatar /></el-icon>
                  <el-input 
                    v-model="registerForm.name" 
                    placeholder="请输入您的真实姓名"
                    class="custom-input"
                    size="large"
                  />
                </div>
              </el-form-item>

              <el-form-item prop="role">
                <div class="input-group">
                  <el-icon class="input-icon"><UserFilled /></el-icon>
                  <el-select 
                    v-model="registerForm.role" 
                    placeholder="请选择角色"
                    class="custom-select"
                    size="large"
                    style="width: 100%"
                  >
                    <el-option label="学生" value="STUDENT" />
                    <el-option label="教师" value="TEACHER" />
                    <el-option label="医生" value="DOCTOR" />
                  </el-select>
                </div>
              </el-form-item>

              <el-form-item prop="phone">
                <div class="input-group">
                  <el-icon class="input-icon"><Iphone /></el-icon>
                  <el-input 
                    v-model="registerForm.phone" 
                    placeholder="请输入手机号"
                    class="custom-input"
                    size="large"
                  />
                </div>
              </el-form-item>

              <el-form-item prop="email">
                <div class="input-group">
                  <el-icon class="input-icon"><Message /></el-icon>
                  <el-input 
                    v-model="registerForm.email" 
                    placeholder="请输入邮箱"
                    class="custom-input"
                    size="large"
                  />
                </div>
              </el-form-item>
            </div>
          </div>

          <!-- 医生专业信息（仅当选择医生角色时显示） -->
          <div class="form-group" v-if="registerForm.role === 'DOCTOR'">
            <h3 class="group-title">专业信息</h3>
            <div class="group-content">
              <el-form-item prop="title">
                <div class="input-group">
                  <el-icon class="input-icon"><Medal /></el-icon>
                  <el-input 
                    v-model="registerForm.title" 
                    placeholder="请输入职称（如：主任医师）"
                    class="custom-input"
                    size="large"
                  />
                </div>
              </el-form-item>

              <el-form-item prop="departmentId">
                <div class="input-group">
                  <el-icon class="input-icon"><OfficeBuilding /></el-icon>
                  <el-select 
                    v-model="registerForm.departmentId" 
                    placeholder="请选择科室"
                    class="custom-select"
                    size="large"
                    style="width: 100%"
                  >
                    <el-option 
                      v-for="dept in departmentOptions" 
                      :key="dept.value" 
                      :label="dept.label" 
                      :value="dept.value" 
                    />
                  </el-select>
                </div>
              </el-form-item>

              <el-form-item prop="specialty">
                <div class="input-group">
                  <el-icon class="input-icon"><Star /></el-icon>
                  <el-input 
                    v-model="registerForm.specialty" 
                    placeholder="请输入专业特长"
                    class="custom-input"
                    size="large"
                  />
                </div>
              </el-form-item>

              <el-form-item prop="introduction">
                <div class="input-group">
                  <el-icon class="input-icon"><Document /></el-icon>
                  <el-input 
                    v-model="registerForm.introduction" 
                    type="textarea"
                    placeholder="请输入个人简介"
                    class="custom-textarea"
                    size="large"
                    :rows="3"
                  />
                </div>
              </el-form-item>
            </div>
          </div>

          <el-form-item>
            <el-button 
              type="primary" 
              :loading="loading" 
              @click="handleRegister"
              class="register-button"
              size="large"
            >
              <el-icon><CircleCheck /></el-icon>
              立即注册
            </el-button>
          </el-form-item>
        </el-form>

        <div class="form-footer">
          <p class="login-text">
            已有账号？
            <a @click="$router.push('/login')" class="login-link">立即登录</a>
          </p>
        </div>
      </div>
    </div>

    <!-- 背景动画元素 -->
    <div class="background-animation">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
      <div class="circle circle-4"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import {
  User,
  Lock,
  Iphone,
  Message,
  FirstAidKit,
  CircleCheck,
  Clock,
  Star,
  Avatar,
  UserFilled,
  Male,
  Female,
  OfficeBuilding,
  Document,
  Medal
} from '@element-plus/icons-vue'
import { ROLE_MAP } from '../api/user'
import { getDepartmentList } from '@/api/department'

const router = useRouter()
const store = useStore()
const registerFormRef = ref()
const loading = ref(false)
const departmentOptions = ref([])

const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: '',
  name: '',
  role: '',
  phone: '',
  email: '',
  // 医生特有字段
  title: '',
  departmentId: '',
  specialty: '',
  introduction: ''
})

// 获取科室列表
const fetchDepartments = async () => {
  try {
    console.log('开始获取科室列表...')
    const response = await getDepartmentList()
    console.log('科室API响应:', response)
    
    // 由于响应拦截器已经返回了res.data，所以这里直接处理返回的数据
    if (response) {
      console.log('科室数据:', response)
      
      // 检查是否是分页数据格式
      if (response.content && Array.isArray(response.content)) {
        console.log('使用分页格式，科室数量:', response.content.length)
        departmentOptions.value = response.content.map(dept => ({
          label: dept.name,
          value: dept.id
        }))
        console.log('科室选项:', departmentOptions.value)
      } else if (Array.isArray(response)) {
        // 如果不是分页格式，直接使用数组
        console.log('使用数组格式，科室数量:', response.length)
        departmentOptions.value = response.map(dept => ({
          label: dept.name,
          value: dept.id
        }))
        console.log('科室选项:', departmentOptions.value)
      } else {
        console.error('科室数据格式不正确:', response)
        ElMessage.error('科室数据格式不正确')
      }
    } else {
      console.error('获取科室列表失败，响应为空')
      ElMessage.error('获取科室列表失败')
    }
  } catch (error) {
    console.error('获取科室列表失败:', error)
    ElMessage.error('获取科室列表失败')
  }
}

const roleOptions = [
  { label: '学生', value: 'STUDENT' },
  { label: '教师', value: 'TEACHER' },
  { label: '医生', value: 'DOCTOR' }
]

const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (registerForm.value.confirmPassword !== '') {
      registerFormRef.value.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.value.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

const validateEmail = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入邮箱'))
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
    callback(new Error('请输入正确的邮箱格式'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { validator: validatePass, trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validatePass2, trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { validator: validatePhone, trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { validator: validateEmail, trigger: 'blur' }
  ],
  // 医生特有字段验证
  title: [
    { required: true, message: '请输入职称', trigger: 'blur' },
    { min: 2, max: 50, message: '职称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  departmentId: [
    { required: true, message: '请选择科室', trigger: 'change' }
  ],
  specialty: [
    { required: true, message: '请输入专业特长', trigger: 'blur' },
    { min: 2, max: 200, message: '专业特长长度在 2 到 200 个字符', trigger: 'blur' }
  ],
  introduction: [
    { max: 500, message: '个人简介长度不能超过 500 个字符', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  try {
    await registerFormRef.value.validate()
    loading.value = true
    
    // 基础用户信息
    const registerData = {
      username: registerForm.value.username,
      password: registerForm.value.password,
      name: registerForm.value.name,
      role: registerForm.value.role,
      phone: registerForm.value.phone,
      email: registerForm.value.email
    }
    
    // 确保role字段正确设置
    if (!ROLE_MAP[registerData.role]) {
      throw new Error('无效的角色选择')
    }
    
    // 添加role_id字段
    registerData.role_id = ROLE_MAP[registerData.role]
    registerData.roleId = ROLE_MAP[registerData.role]
    
    // 如果是医生角色，添加医生特有信息
    if (registerForm.value.role === 'DOCTOR') {
      registerData.doctorInfo = {
        title: registerForm.value.title,
        departmentId: parseInt(registerForm.value.departmentId),
        specialty: registerForm.value.specialty,
        introduction: registerForm.value.introduction
      }
    }
    
    console.log('Register form data:', registerData)
    
    await store.dispatch('user/register', registerData)
    
    ElMessage.success('注册成功！正在跳转登录页面...')
    setTimeout(() => {
      router.push('/login')
    }, 1500)
  } catch (error) {
    console.error('注册失败:', error)
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('注册失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

// 组件挂载时获取科室列表
onMounted(() => {
  fetchDepartments()
})
</script>

<style lang="scss" scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  font-family: 'Segoe UI', 'Helvetica Neue', sans-serif;
  position: relative;
  overflow: hidden;
}

.register-card {
  display: flex;
  width: 1000px;
  height: 650px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(10px);
  overflow: hidden;
  position: relative;
  z-index: 10;
}

/* 左侧欢迎区域 */
.welcome-section {
  flex: 1;
  background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 40px;
  position: relative;
  
  .welcome-content {
    text-align: center;
    color: white;
    max-width: 300px;
    
    .logo-container {
      margin-bottom: 30px;
      
      .el-icon {
        animation: pulse 2s ease-in-out infinite;
      }
    }
    
    h1 {
      font-size: 36px;
      font-weight: 700;
      margin-bottom: 15px;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
    }
    
    .welcome-text {
      font-size: 18px;
      opacity: 0.9;
      margin-bottom: 40px;
      line-height: 1.6;
    }
    
    .features {
      display: flex;
      flex-direction: column;
      gap: 20px;
      
      .feature-item {
        display: flex;
        align-items: center;
        gap: 12px;
        background: rgba(255, 255, 255, 0.2);
        padding: 12px 20px;
        border-radius: 12px;
        backdrop-filter: blur(10px);
        animation: slideInLeft 0.8s ease-out;
        
        &:nth-child(2) {
          animation-delay: 0.2s;
        }
        
        &:nth-child(3) {
          animation-delay: 0.4s;
        }
        
        span {
          font-size: 16px;
          font-weight: 500;
        }
      }
    }
  }
}

/* 右侧表单区域 */
.form-section {
  flex: 1.2;
  padding: 40px 50px;
  overflow-y: auto;
  
  .form-header {
    text-align: center;
    margin-bottom: 30px;
    
    h2 {
      font-size: 28px;
      font-weight: 700;
      color: #2c3e50;
      margin-bottom: 10px;
    }
    
    .subtitle {
      color: #7f8c8d;
      font-size: 16px;
      margin: 0;
    }
  }
}

.register-form {
  .form-group {
    margin-bottom: 30px;
    
    .group-title {
      font-size: 18px;
      font-weight: 600;
      color: #2c3e50;
      margin-bottom: 20px;
      padding-bottom: 10px;
      border-bottom: 2px solid #f0f0f0;
    }
    
    .group-content {
      padding-left: 10px;
    }
  }
  
  .input-group {
    position: relative;
    display: flex;
    align-items: center;
    
    .input-icon {
      position: absolute;
      left: 15px;
      z-index: 1;
      color: #909399;
      font-size: 18px;
    }
    
    .custom-input, .custom-textarea {
      width: 100%;
      
      :deep(.el-input__inner), :deep(textarea) {
        padding-left: 45px;
        height: 50px;
        border-radius: 12px;
        border: 2px solid #e8e8e8;
        font-size: 16px;
        transition: all 0.3s ease;
        
        &:focus {
          border-color: #1890ff;
          box-shadow: 0 0 0 4px rgba(24, 144, 255, 0.15);
          transform: translateY(-1px);
        }
        
        &::placeholder {
          color: #bfbfbf;
        }
      }
      
      :deep(textarea) {
        height: auto;
        min-height: 80px;
        padding-top: 12px;
      }
    }
  }
  
  .custom-select {
    width: 100%;
    
    :deep(.el-select__wrapper) {
      padding-left: 45px;
      height: 50px;
      border-radius: 12px;
      border: 2px solid #e8e8e8;
      font-size: 16px;
      transition: all 0.3s ease;
      
      &:hover {
        border-color: #1890ff;
      }
      
      &.is-focus {
        border-color: #1890ff;
        box-shadow: 0 0 0 4px rgba(24, 144, 255, 0.15);
      }
    }
  }
  
  .gender-group {
    display: flex;
    gap: 20px;
    
    :deep(.el-radio) {
      margin-right: 0;
      
      .el-radio__label {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 16px;
      }
    }
  }
  
  .register-button {
    width: 100%;
    height: 50px;
    border-radius: 12px;
    font-size: 16px;
    font-weight: 600;
    background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
    border: none;
    transition: all 0.3s ease;
    margin-top: 20px;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 25px rgba(24, 144, 255, 0.4);
    }
    
    &:active {
      transform: translateY(0);
    }
    
    .el-icon {
      margin-right: 8px;
    }
  }
}

.form-footer {
  text-align: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
  
  .login-text {
    color: #595959;
    font-size: 14px;
    margin: 0;
    
    .login-link {
      color: #1890ff;
      text-decoration: none;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s ease;
      
      &:hover {
        color: #36cfc9;
        text-decoration: underline;
      }
    }
  }
}

/* 背景动画 */
.background-animation {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  
  .circle {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);
    animation: float 6s ease-in-out infinite;
    
    &.circle-1 {
      width: 80px;
      height: 80px;
      top: 15%;
      left: 10%;
      animation-delay: 0s;
    }
    
    &.circle-2 {
      width: 120px;
      height: 120px;
      top: 65%;
      right: 15%;
      animation-delay: 2s;
    }
    
    &.circle-3 {
      width: 60px;
      height: 60px;
      bottom: 25%;
      left: 15%;
      animation-delay: 4s;
    }
    
    &.circle-4 {
      width: 100px;
      height: 100px;
      top: 40%;
      right: 25%;
      animation-delay: 1s;
    }
  }
}

/* 动画定义 */
@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-20px);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

@keyframes slideInLeft {
  0% {
    transform: translateX(-30px);
    opacity: 0;
  }
  100% {
    transform: translateX(0);
    opacity: 1;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-card {
    flex-direction: column;
    width: 90%;
    height: auto;
    max-width: 400px;
  }
  
  .welcome-section {
    padding: 30px 20px;
    min-height: 200px;
    
    .welcome-content {
      h1 {
        font-size: 28px;
      }
      
      .welcome-text {
        font-size: 16px;
      }
      
      .features {
        flex-direction: row;
        justify-content: center;
        flex-wrap: wrap;
        gap: 10px;
        
        .feature-item {
          flex-direction: column;
          text-align: center;
          padding: 8px 12px;
          
          span {
            font-size: 14px;
          }
        }
      }
    }
  }
  
  .form-section {
    padding: 30px 20px;
  }
  
  .form-header h2 {
    font-size: 24px;
  }
  
  .form-group .group-title {
    font-size: 16px;
  }
}

/* 表单验证样式优化 */
:deep(.el-form-item) {
  margin-bottom: 20px;
  
  &.is-error .custom-input .el-input__inner,
  &.is-error .custom-textarea :deep(textarea),
  &.is-error .custom-select :deep(.el-select__wrapper) {
    border-color: #ff4d4f;
    box-shadow: 0 0 0 4px rgba(255, 77, 79, 0.15);
  }
  
  &.is-success .custom-input .el-input__inner,
  &.is-success .custom-textarea :deep(textarea),
  &.is-success .custom-select :deep(.el-select__wrapper) {
    border-color: #52c41a;
    box-shadow: 0 0 0 4px rgba(82, 196, 26, 0.15);
  }
  
  .el-form-item__error {
    padding-top: 8px;
    font-size: 13px;
    font-weight: 500;
  }
}
</style>