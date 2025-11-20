<template>
  <div class="login-container">
    <div class="login-card">
      <!-- 左侧医疗图标装饰 -->
      <div class="card-decoration">
        <div class="medical-icons">
          <div class="icon-item">
            <el-icon size="48" color="#1890ff"><FirstAidKit /></el-icon>
          </div>
          <div class="icon-item">
            <el-icon size="40" color="#52c41a"><CircleCheck /></el-icon>
          </div>
          <div class="icon-item">
            <el-icon size="36" color="#fa8c16"><Clock /></el-icon>
          </div>
        </div>
        <div class="decoration-text">
          <h3>智慧医疗</h3>
          <p>便捷预约 · 专业服务 · 健康管理</p>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="login-form-section">
        <div class="form-header">
          <div class="logo-container">
            <el-icon size="48" color="#1890ff"><FirstAidKit /></el-icon>
          </div>
          <h2>欢迎回来</h2>
          <p class="subtitle">登录华商学院校医院系统</p>
        </div>

        <el-form 
          ref="formRef" 
          :model="form" 
          :rules="rules" 
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <div class="input-group">
              <el-icon class="input-icon"><User /></el-icon>
              <el-input 
                v-model="form.username" 
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
                v-model="form.password" 
                type="password" 
                placeholder="请输入密码"
                class="custom-input"
                size="large"
                show-password
              />
            </div>
          </el-form-item>

          <el-form-item>
            <el-button 
              type="primary" 
              :loading="loading" 
              @click="handleLogin"
              class="login-button"
              size="large"
            >
              <el-icon><Right /></el-icon>
              立即登录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="form-footer">
          <p class="register-text">
            还没有账号？
            <a @click="$router.push('/register')" class="register-link">立即注册</a>
          </p>
          <p class="copyright">© {{ new Date().getFullYear() }} 华商学院校医院</p>
        </div>
      </div>
    </div>

    <!-- 背景动画元素 -->
    <div class="background-animation">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  User,
  Lock,
  Right,
  FirstAidKit,
  CircleCheck,
  Clock
} from '@element-plus/icons-vue'

const store = useStore()
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = ref({
  username: 'admin',
  password: '123456'
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度应在3-20个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应在6-20个字符之间', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    await store.dispatch('user/login', form.value)
    // 登录成功后的跳转由store中的login action处理，这里不再重复跳转
  } catch (error) {
    console.error('Login failed:', error)
    // 改进错误处理，显示更详细的错误信息
    let errorMsg = '登录失败，请检查用户名和密码'
    if (error.response?.data?.message) {
      errorMsg = error.response.data.message
    } else if (error.message) {
      errorMsg = error.message
    } else if (typeof error === 'string') {
      errorMsg = error
    }
    ElMessage.error(errorMsg)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
  font-family: 'Segoe UI', 'Helvetica Neue', sans-serif;
}

.login-card {
  display: flex;
  width: 900px;
  height: 550px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(10px);
  overflow: hidden;
  position: relative;
  z-index: 10;
}

/* 左侧装饰区域 */
.card-decoration {
  flex: 1;
  background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 40px;
  position: relative;
  
  .medical-icons {
    display: flex;
    flex-direction: column;
    gap: 30px;
    margin-bottom: 40px;
    
    .icon-item {
      background: rgba(255, 255, 255, 0.2);
      border-radius: 50%;
      width: 80px;
      height: 80px;
      display: flex;
      align-items: center;
      justify-content: center;
      backdrop-filter: blur(10px);
      animation: float 3s ease-in-out infinite;
      
      &:nth-child(2) {
        animation-delay: 1s;
      }
      
      &:nth-child(3) {
        animation-delay: 2s;
      }
    }
  }
  
  .decoration-text {
    text-align: center;
    color: white;
    
    h3 {
      font-size: 28px;
      font-weight: 600;
      margin-bottom: 15px;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
    }
    
    p {
      font-size: 16px;
      opacity: 0.9;
      line-height: 1.6;
    }
  }
}

/* 右侧登录表单区域 */
.login-form-section {
  flex: 1;
  padding: 60px 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-header {
  text-align: center;
  margin-bottom: 40px;
  
  .logo-container {
    margin-bottom: 20px;
    
    .el-icon {
      animation: pulse 2s ease-in-out infinite;
    }
  }
  
  h2 {
    font-size: 32px;
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

.login-form {
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
    
    .custom-input {
      width: 100%;
      
      :deep(.el-input__inner) {
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
    }
  }
  
  .login-button {
    width: 100%;
    height: 50px;
    border-radius: 12px;
    font-size: 16px;
    font-weight: 600;
    background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
    border: none;
    transition: all 0.3s ease;
    margin-top: 10px;
    
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
  
  .register-text {
    color: #595959;
    font-size: 14px;
    margin-bottom: 20px;
    
    .register-link {
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
  
  .copyright {
    color: #bfbfbf;
    font-size: 12px;
    margin: 0;
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
      top: 20%;
      left: 10%;
      animation-delay: 0s;
    }
    
    &.circle-2 {
      width: 120px;
      height: 120px;
      top: 60%;
      right: 15%;
      animation-delay: 2s;
    }
    
    &.circle-3 {
      width: 60px;
      height: 60px;
      bottom: 20%;
      left: 20%;
      animation-delay: 4s;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .login-card {
    flex-direction: column;
    width: 90%;
    height: auto;
    max-width: 400px;
  }
  
  .card-decoration {
    padding: 30px 20px;
    min-height: 200px;
    
    .medical-icons {
      flex-direction: row;
      justify-content: center;
      gap: 20px;
      margin-bottom: 20px;
      
      .icon-item {
        width: 60px;
        height: 60px;
        
        .el-icon {
          font-size: 24px;
        }
      }
    }
    
    .decoration-text h3 {
      font-size: 24px;
    }
  }
  
  .login-form-section {
    padding: 40px 30px;
  }
  
  .form-header h2 {
    font-size: 28px;
  }
}

/* 表单验证样式优化 */
:deep(.el-form-item) {
  margin-bottom: 25px;
  
  &.is-error .custom-input .el-input__inner {
    border-color: #ff4d4f;
    box-shadow: 0 0 0 4px rgba(255, 77, 79, 0.15);
  }
  
  &.is-success .custom-input .el-input__inner {
    border-color: #52c41a;
    box-shadow: 0 0 0 4px rgba(82, 196, 26, 0.15);
  }
  
  .el-form-item__error {
    padding-top: 8px;
    font-size: 13px;
    font-weight: 500;
  }
}
</style>}]}