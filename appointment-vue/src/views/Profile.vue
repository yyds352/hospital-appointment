<template>
  <div class="profile-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="avatar-section">
          <div class="avatar-container">
            <el-avatar :size="80" :src="avatarUrl" class="user-avatar">
              <template #default>
                <span class="avatar-text">{{ userForm.name?.charAt(0) || 'U' }}</span>
              </template>
            </el-avatar>
            <div class="avatar-overlay" @click="handleAvatarClick">
              <i class="el-icon-camera"></i>
            </div>
          </div>
          <div class="user-info">
            <h2 class="user-name">{{ userForm.name }}</h2>
            <p class="user-role">{{ roleText }}</p>
            <p class="user-id">ID: {{ userForm.username }}</p>
          </div>
        </div>
        <div class="header-actions">
          <el-button 
            :type="isEditing ? 'danger' : 'primary'" 
            @click="isEditing ? handleCancel() : handleEdit()"
            :icon="isEditing ? 'el-icon-close' : 'el-icon-edit'"
          >
            {{ isEditing ? '取消编辑' : '编辑资料' }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="content-container">
      <!-- 个人信息卡片 -->
      <el-card class="info-card modern-card">
        <template #header>
          <div class="card-header-modern">
            <div class="header-left">
              <i class="el-icon-user"></i>
              <span>个人信息</span>
            </div>
            <el-tag 
              :type="userForm.role === 'ADMIN' ? 'danger' : 
                     userForm.role === 'DOCTOR' ? 'success' : 'info'"
              effect="dark"
              class="role-tag"
            >
              {{ roleText }}
            </el-tag>
          </div>
        </template>

        <el-form
          ref="formRef"
          :model="userForm"
          :rules="formRules"
          label-width="120px"
          :disabled="!isEditing"
          class="modern-form"
        >
          <div class="form-grid">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="userForm.username" disabled class="form-input">
                <template #prefix>
                  <i class="el-icon-user-solid"></i>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="姓名" prop="name">
              <el-input v-model="userForm.name" class="form-input" :disabled="!isEditing">
                <template #prefix>
                  <i class="el-icon-user"></i>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="手机号" prop="phone">
              <el-input v-model="userForm.phone" class="form-input" :disabled="!isEditing">
                <template #prefix>
                  <i class="el-icon-phone"></i>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input v-model="userForm.email" class="form-input" :disabled="!isEditing">
                <template #prefix>
                  <i class="el-icon-message"></i>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="加入时间">
              <el-input :value="formatDate(userForm.createdAt)" disabled class="form-input">
                <template #prefix>
                  <i class="el-icon-date"></i>
                </template>
              </el-input>
            </el-form-item>
          </div>

          <div v-if="isEditing" class="form-actions">
            <el-button type="primary" @click="handleSave" :loading="saving" class="save-btn">
              <i class="el-icon-check"></i>
              保存修改
            </el-button>
            <el-button @click="handleCancel" class="cancel-btn">
              <i class="el-icon-close"></i>
              取消
            </el-button>
          </div>
        </el-form>
      </el-card>

      <!-- 密码修改卡片 -->
      <el-card class="password-card modern-card">
        <template #header>
          <div class="card-header-modern">
            <div class="header-left">
              <i class="el-icon-lock"></i>
              <span>安全设置</span>
            </div>
            <el-tag type="warning" effect="plain">建议定期更换</el-tag>
          </div>
        </template>

        <div class="security-section">
          <div class="security-item">
            <div class="security-icon">
              <i class="el-icon-key"></i>
            </div>
            <div class="security-content">
              <h4>登录密码</h4>
              <p>定期更换密码可以提高账户安全性</p>
            </div>
            <el-button type="text" @click="showPasswordDialog = true" class="action-btn">
              修改密码
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 账户统计 -->
      <el-card class="stats-card modern-card">
        <template #header>
          <div class="card-header-modern">
            <div class="header-left">
              <i class="el-icon-data-line"></i>
              <span>账户统计</span>
            </div>
          </div>
        </template>

        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-icon">
              <i class="el-icon-date"></i>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ daysSinceJoined }}</div>
              <div class="stat-label">加入天数</div>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon">
              <i class="el-icon-time"></i>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ lastLoginTime }}</div>
              <div class="stat-label">最后登录</div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 密码修改对话框 -->
    <el-dialog
      v-model="showPasswordDialog"
      title="修改密码"
      width="500px"
      :close-on-click-modal="false"
      class="password-dialog"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
        class="password-form"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            show-password
            placeholder="请输入原密码"
          >
            <template #prefix>
              <i class="el-icon-lock"></i>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码"
          >
            <template #prefix>
              <i class="el-icon-key"></i>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
          >
            <template #prefix>
              <i class="el-icon-circle-check"></i>
            </template>
          </el-input>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showPasswordDialog = false" class="cancel-btn">
            取消
          </el-button>
          <el-button type="primary" @click="handleUpdatePassword" :loading="passwordUpdating" class="confirm-btn">
            确认修改
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'

const store = useStore()
const formRef = ref()
const passwordFormRef = ref()
const isEditing = ref(false)
const saving = ref(false)
const showPasswordDialog = ref(false)
const passwordUpdating = ref(false)

// 用户表单数据
const userForm = ref({
  id: '',
  username: '',
  name: '',
  role: '',
  phone: '',
  email: '',
  createdAt: '',
  updatedAt: ''
})

// 密码表单数据
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 头像URL
const avatarUrl = ref('')

// 角色文本
const roleText = computed(() => {
  const roles = {
    ADMIN: '管理员',
    DOCTOR: '医生',
    STUDENT: '学生',
    TEACHER: '教师',
    PATIENT: '患者'
  }
  return roles[userForm.value.role] || userForm.value.role
})

// 加入天数
const daysSinceJoined = computed(() => {
  if (!userForm.value.createdAt) return 0
  const joinDate = new Date(userForm.value.createdAt)
  const today = new Date()
  const diffTime = Math.abs(today - joinDate)
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return diffDays
})

// 最后登录时间
const lastLoginTime = computed(() => {
  // 这里可以根据实际情况获取最后登录时间
  return '刚刚'
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度应在2-20个字符之间', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

// 密码验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value === passwordForm.value.oldPassword) {
          callback(new Error('新密码不能与原密码相同'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 获取用户信息
const initUserInfo = () => {
  const userInfo = store.getters['user/userInfo']
  
  if (userInfo) {
    userForm.value = {
      id: userInfo.id,
      username: userInfo.username || '',
      name: userInfo.name || '',
      role: userInfo.role || '',
      phone: userInfo.phone || '',
      email: userInfo.email || '',
      createdAt: userInfo.createdAt || '',
      updatedAt: userInfo.updatedAt || ''
    }
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '未知'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

// 头像点击
const handleAvatarClick = () => {
  ElMessage.info('头像上传功能开发中...')
}

// 编辑
const handleEdit = () => {
  isEditing.value = true
}

// 保存
const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    saving.value = true
    await formRef.value.validate()
    
    if (!userForm.value.id) {
      throw new Error('用户ID不能为空')
    }
    
    const updatedUserInfo = await store.dispatch('user/updateUserInfo', userForm.value)
    
    userForm.value = {
      ...userForm.value,
      ...updatedUserInfo
    }
    
    isEditing.value = false
    ElMessage.success('个人信息更新成功！')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error(error.message || error.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

// 取消
const handleCancel = () => {
  isEditing.value = false
  initUserInfo()
}

// 修改密码
const handleUpdatePassword = async () => {
  if (!passwordFormRef.value) return
  
  try {
    passwordUpdating.value = true
    await passwordFormRef.value.validate()
    
    await store.dispatch('user/updatePassword', {
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    
    ElMessage.success('密码修改成功！')
    showPasswordDialog.value = false
    passwordForm.value = {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error(error.response?.data?.message || '修改密码失败')
  } finally {
    passwordUpdating.value = false
  }
}

// 组件挂载时初始化用户信息
onMounted(() => {
  initUserInfo()
})
</script>

<style lang="scss" scoped>
.profile-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

// 页面头部
.page-header {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 25px;
}

.avatar-container {
  position: relative;
  cursor: pointer;
  transition: transform 0.3s ease;
  
  &:hover {
    transform: scale(1.05);
    
    .avatar-overlay {
      opacity: 1;
    }
  }
}

.user-avatar {
  border: 4px solid #fff;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.avatar-text {
  font-size: 32px;
  font-weight: bold;
  color: #fff;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  
  i {
    color: #fff;
    font-size: 24px;
  }
}

.user-info {
  h2 {
    margin: 0 0 8px 0;
    font-size: 28px;
    font-weight: 700;
    color: #2c3e50;
  }
  
  .user-role {
    margin: 0 0 5px 0;
    font-size: 16px;
    color: #7f8c8d;
    font-weight: 500;
  }
  
  .user-id {
    margin: 0;
    font-size: 14px;
    color: #95a5a6;
  }
}

.header-actions {
  display: flex;
  gap: 15px;
}

// 内容容器
.content-container {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 30px;
  
  @media (max-width: 1200px) {
    grid-template-columns: 1fr;
  }
}

// 现代卡片样式
.modern-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  border: none;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15);
  }
  
  :deep(.el-card__header) {
    background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
    padding: 20px 30px;
  }
}

.card-header-modern {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
    
    i {
      font-size: 20px;
      color: #667eea;
    }
    
    span {
      font-size: 18px;
      font-weight: 600;
      color: #2c3e50;
    }
  }
}

.role-tag {
  font-weight: 600;
  border-radius: 20px;
}

// 表单样式
.modern-form {
  padding: 30px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 25px;
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.form-input {
  :deep(.el-input__inner) {
    height: 50px;
    border-radius: 12px;
    border: 2px solid #e1e8ed;
    padding-left: 45px;
    font-size: 15px;
    transition: all 0.3s ease;
    
    &:focus {
      border-color: #667eea;
      box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
    }
  }
  
  :deep(.el-input__prefix) {
    left: 15px;
    display: flex;
    align-items: center;
    color: #667eea;
    font-size: 16px;
  }
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 40px;
  padding-top: 30px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.save-btn, .cancel-btn {
  min-width: 120px;
  height: 45px;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  }
}

// 安全设置
.security-section {
  padding: 30px;
}

.security-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 25px;
  background: #f8f9fa;
  border-radius: 15px;
  margin-bottom: 20px;
  transition: all 0.3s ease;
  
  &:hover {
    background: #e9ecef;
    transform: translateX(5px);
  }
}

.security-icon {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
}

.security-content {
  flex: 1;
  
  h4 {
    margin: 0 0 5px 0;
    font-size: 16px;
    font-weight: 600;
    color: #2c3e50;
  }
  
  p {
    margin: 0;
    font-size: 14px;
    color: #7f8c8d;
  }
}

.action-btn {
  font-weight: 600;
  color: #667eea;
  
  &:hover {
    color: #764ba2;
  }
}

// 统计卡片
.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 25px;
  padding: 30px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 25px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 15px;
  color: #fff;
  transition: transform 0.3s ease;
  
  &:hover {
    transform: scale(1.05);
  }
}

.stat-icon {
  font-size: 28px;
  opacity: 0.8;
}

.stat-content {
  .stat-number {
    font-size: 24px;
    font-weight: 700;
    margin-bottom: 5px;
  }
  
  .stat-label {
    font-size: 14px;
    opacity: 0.9;
  }
}

// 密码对话框
.password-dialog {
  :deep(.el-dialog) {
    border-radius: 20px;
    box-shadow: 0 25px 50px rgba(0, 0, 0, 0.2);
  }
  
  :deep(.el-dialog__header) {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    margin: 0;
    padding: 25px 30px;
    border-radius: 20px 20px 0 0;
    
    .el-dialog__title {
      color: #fff;
      font-size: 20px;
      font-weight: 600;
    }
  }
}

.password-form {
  padding: 30px 20px;
  
  :deep(.el-input__inner) {
    height: 50px;
    border-radius: 12px;
    border: 2px solid #e1e8ed;
    padding-left: 45px;
    
    &:focus {
      border-color: #667eea;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  padding: 20px 0 10px;
  
  .confirm-btn {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    border-radius: 12px;
    font-weight: 600;
    
    &:hover {
      opacity: 0.9;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .profile-page {
    padding: 15px;
  }
  
  .header-content {
    flex-direction: column;
    text-align: center;
  }
  
  .avatar-section {
    flex-direction: column;
    text-align: center;
  }
  
  .content-container {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .form-grid {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
}

// 动画效果
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modern-card {
  animation: fadeInUp 0.6s ease forwards;
  
  &:nth-child(1) { animation-delay: 0.1s; }
  &:nth-child(2) { animation-delay: 0.2s; }
  &:nth-child(3) { animation-delay: 0.3s; }
}
</style>