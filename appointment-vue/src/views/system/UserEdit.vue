<template>
  <div class="user-edit">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>编辑用户</span>
        </div>
      </template>

      <el-form :model="userForm" :rules="formRules" ref="formRef" label-width="120px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" disabled />
        </el-form-item>
        
        <el-form-item label="姓名" prop="name">
          <el-input v-model="userForm.name" placeholder="请输入姓名" />
        </el-form-item>
        
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色">
            <el-option label="学生" value="STUDENT" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="医生" value="DOCTOR" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-switch v-model="userForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">提交</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserById, updateUser } from '@/api/user'

export default {
  name: 'UserEdit',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const formRef = ref(null)
    const loading = ref(false)
    const userLoading = ref(true)
    
    // 用户表单
    const userForm = reactive({
      id: '',
      username: '',
      name: '',
      role: 'STUDENT',
      phone: '',
      email: '',
      status: 1
    })
    
    // 表单验证规则
    const formRules = {
      name: [
        { required: true, message: '请输入姓名', trigger: 'blur' },
        { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
      ],
      role: [
        { required: true, message: '请选择角色', trigger: 'change' }
      ],
      phone: [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
      ]
    }
    
    // 加载用户信息
    const loadUserInfo = async () => {
      const userId = route.params.id
      if (!userId) {
        ElMessage.error('用户ID不能为空')
        router.push('/home/system/users')
        return
      }
      
      try {
        userLoading.value = true
        const response = await getUserById(userId)
        console.log('=== 加载用户信息响应 ===', response)
        
        // 响应拦截器已经处理了数据结构，直接检查数据
        if (response) {
          Object.assign(userForm, response)
        } else {
          ElMessage.error('加载用户信息失败')
          router.push('/home/system/users')
        }
      } catch (error) {
        console.error('加载用户信息失败:', error)
        ElMessage.error('加载用户信息失败: ' + (error.message || '网络错误'))
        router.push('/home/system/users')
      } finally {
        userLoading.value = false
      }
    }
    
    // 提交表单
    const handleSubmit = async () => {
      if (!formRef.value) return
      
      try {
        await formRef.value.validate()
        loading.value = true
        
        console.log('=== 更新用户数据 ===', userForm)
        const response = await updateUser(userForm.id, userForm)
        console.log('=== 更新用户响应 ===', response)
        
        // 响应拦截器已经处理了数据结构，直接检查数据
        if (response) {
          ElMessage.success('用户更新成功')
          router.push('/home/system/users')
        } else {
          ElMessage.error('用户更新失败')
        }
      } catch (error) {
        console.error('更新用户失败:', error)
        ElMessage.error('更新用户失败: ' + (error.message || '网络错误'))
      } finally {
        loading.value = false
      }
    }
    
    // 取消
    const handleCancel = () => {
      router.push('/home/system/users')
    }
    
    // 组件挂载时加载用户信息
    onMounted(() => {
      loadUserInfo()
    })
    
    return {
      userForm,
      formRules,
      formRef,
      loading,
      userLoading,
      handleSubmit,
      handleCancel
    }
  }
}
</script>

<style scoped>
.user-edit {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>