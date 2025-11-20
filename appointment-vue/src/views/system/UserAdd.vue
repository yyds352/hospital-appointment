<template>
  <div class="user-add">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>添加用户</span>
        </div>
      </template>

      <el-form :model="userForm" :rules="formRules" ref="formRef" label-width="120px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="userForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
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
        
        <!-- 医生特有信息 -->
        <el-divider v-if="userForm.role === 'DOCTOR'">医生信息</el-divider>
        
        <el-form-item v-if="userForm.role === 'DOCTOR'" label="职称" prop="doctorInfo.title">
          <el-input v-model="userForm.doctorInfo.title" placeholder="请输入职称" />
        </el-form-item>
        
        <el-form-item v-if="userForm.role === 'DOCTOR'" label="科室" prop="doctorInfo.departmentId">
          <el-select v-model="userForm.doctorInfo.departmentId" placeholder="请选择科室" filterable>
            <el-option v-for="dept in departmentOptions" :key="dept.id" :label="dept.name" :value="dept.id" />
          </el-select>
        </el-form-item>
        
        <el-form-item v-if="userForm.role === 'DOCTOR'" label="专业特长" prop="doctorInfo.specialty">
          <el-input v-model="userForm.doctorInfo.specialty" placeholder="请输入专业特长" />
        </el-form-item>
        
        <el-form-item v-if="userForm.role === 'DOCTOR'" label="个人简介" prop="doctorInfo.introduction">
          <el-input v-model="userForm.doctorInfo.introduction" type="textarea" :rows="3" placeholder="请输入个人简介" />
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-switch v-model="userForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">提交</el-button>
          <el-button @click="handleCancel">取消</el-button>
          <el-button type="info" @click="testDepartmentList">测试科室列表</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addUser } from '@/api/user'
import { getDepartmentList } from '@/api/department'

export default {
  name: 'UserAdd',
  setup() {
    const router = useRouter()
    const formRef = ref(null)
    const loading = ref(false)
    const departmentOptions = ref([])
    
    // 用户表单
    const userForm = reactive({
      username: '',
      password: '',
      confirmPassword: '',
      name: '',
      role: 'STUDENT',
      phone: '',
      email: '',
      status: 1,
      doctorInfo: {
        title: '',
        departmentId: null,
        specialty: '',
        introduction: ''
      }
    })
    
    // 表单验证规则
    const formRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请确认密码', trigger: 'blur' },
        { validator: (rule, value, callback) => {
            if (value !== userForm.password) {
              callback(new Error('两次输入的密码不一致'))
            } else {
              callback()
            }
          }, trigger: 'blur' }
      ],
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
      ],
      'doctorInfo.title': [
        { required: () => userForm.role === 'DOCTOR', message: '请输入职称', trigger: 'blur' }
      ],
      'doctorInfo.departmentId': [
        { required: () => userForm.role === 'DOCTOR', message: '请选择科室', trigger: 'change' }
      ],
      'doctorInfo.specialty': [
        { required: () => userForm.role === 'DOCTOR', message: '请输入专业特长', trigger: 'blur' }
      ]
    }
    
    // 获取科室列表
    const getDepartments = async () => {
      try {
        console.log('开始获取科室列表...');
        const response = await getDepartmentList({ page: 0, size: 100 }) // 后端页码从0开始
        console.log('科室列表响应:', response);
        
        // 兼容不同的数据格式
        let departmentData = [];
        if (response && response.content) {
          // Spring Data Page对象格式: {content: [], totalElements: 10}
          departmentData = response.content;
        } else if (response && Array.isArray(response)) {
          // 直接返回数组格式
          departmentData = response;
        } else if (response && response.data) {
          // 可能的包装格式: {data: []}
          departmentData = response.data;
        } else {
          console.warn('科室列表数据格式不符合预期:', response)
          ElMessage.warning('科室列表数据格式可能有问题，请检查后端接口')
        }
        
        // 验证数据结构
        console.log('科室数据处理后:', departmentData);
        console.log('科室数据长度:', departmentData.length);
        if (departmentData.length > 0) {
          console.log('第一个科室数据结构:', departmentData[0]);
        }
        
        departmentOptions.value = departmentData;
      } catch (error) {
        console.error('获取科室列表失败:', error)
        ElMessage.error('获取科室列表失败: ' + (error.message || '网络错误'))
      }
    }
    
    // 当角色切换为医生时，加载科室列表
    watch(() => userForm.role, (newRole) => {
      if (newRole === 'DOCTOR') {
        getDepartments()
      }
    })
    
    // 测试科室列表获取功能
    const testDepartmentList = async () => {
      console.log('=== 开始测试科室列表获取 ===');
      try {
        const response = await getDepartmentList({ page: 0, size: 100 });
        console.log('测试响应:', response);
        
        // 直接在页面上显示科室数据
        if (response && response.content) {
          ElMessage.success(`成功获取${response.content.length}个科室`);
          console.log('科室详情:', response.content);
        } else {
          ElMessage.warning('获取科室数据格式可能有问题');
        }
      } catch (error) {
        console.error('测试获取科室列表失败:', error);
        ElMessage.error('测试获取科室列表失败: ' + error.message);
      }
    }
    
    // 提交表单
    const handleSubmit = async () => {
      if (!formRef.value) return
      
      try {
        await formRef.value.validate()
        loading.value = true
        
        console.log('=== 添加用户数据 ===', userForm)
        const response = await addUser(userForm)
        console.log('=== 添加用户响应 ===', response)
        
        // 响应拦截器已经处理了数据结构，直接检查数据
        if (response) {
          ElMessage.success('用户添加成功')
          router.push('/home/system/users')
        } else {
          ElMessage.error('用户添加失败')
        }
      } catch (error) {
        console.error('添加用户失败:', error)
        ElMessage.error('添加用户失败: ' + (error.message || '网络错误'))
      } finally {
        loading.value = false
      }
    }
    
    // 取消
    const handleCancel = () => {
      router.push('/home/system/users')
    }
    
    return {
      userForm,
      formRules,
      formRef,
      loading,
      departmentOptions,
      handleSubmit,
      handleCancel,
      testDepartmentList
    }
  }
}
</script>

<style scoped>
.user-add {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>