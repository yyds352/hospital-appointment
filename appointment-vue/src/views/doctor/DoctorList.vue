<template>
  <div class="doctor-container">
    <div class="doctor-header">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="医生姓名">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入医生姓名"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="所属科室">
          <el-select
            v-model="queryParams.departmentId"
            placeholder="请选择科室"
            clearable
          >
            <el-option
              v-for="item in departmentOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="在职" :value="1" />
            <el-option label="离职" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <div>
          <el-button type="primary" @click="showCreateDialog">新增医生</el-button>
        </div>
    </div>

    <el-table v-loading="loading" :data="doctorList" border>
      <el-table-column type="index" label="序号" width="50" />
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="title" label="职称" width="120" />
      <el-table-column prop="departmentName" label="所属科室" width="120" />
      <el-table-column prop="specialty" label="专长" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '在职' : '离职' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="showEditDialog(row)">编辑</el-button>
          <el-button type="primary" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="queryParams.page"
      v-model:page-size="queryParams.size"
      :page-sizes="[10, 20, 30, 50]"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />

    <!-- 添加/修改医生对话框 -->
    <el-dialog
      :title="dialog.title"
      v-model="dialog.visible"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="doctorFormRef"
        :model="doctorForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="姓名" prop="name">
          <el-input v-model="doctorForm.name" placeholder="请输入医生姓名" />
        </el-form-item>
        <el-form-item label="职称" prop="title">
          <el-input v-model="doctorForm.title" placeholder="请输入职称" />
        </el-form-item>
        <el-form-item label="所属科室" prop="departmentId">
          <el-select
            v-model="doctorForm.departmentId"
            placeholder="请选择科室"
            style="width: 100%"
          >
            <el-option
              v-for="item in departmentOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="专长" prop="specialty">
          <el-input
            v-model="doctorForm.specialty"
            type="textarea"
            placeholder="请输入专长"
          />
        </el-form-item>
        <el-form-item label="简介" prop="introduction">
          <el-input
            v-model="doctorForm.introduction"
            type="textarea"
            :rows="2"
            placeholder="请输入简介（可选）"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="doctorForm.status">
            <el-radio :label="1">在职</el-radio>
            <el-radio :label="0">离职</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="doctorForm.username" placeholder="请输入用户名，用于医生账号登录" />
        </el-form-item>
        <el-form-item label="电子邮箱" prop="email">
          <el-input v-model="doctorForm.email" placeholder="请输入电子邮箱" />
        </el-form-item>
        <el-form-item label="电话号码" prop="phone">
          <el-input v-model="doctorForm.phone" placeholder="请输入电话号码" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!doctorForm.id">
          <el-input v-model="doctorForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword" v-if="!doctorForm.id">
          <el-input v-model="doctorForm.confirmPassword" type="password" placeholder="请再次输入密码" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="doctorForm.id">
          <el-input v-model="doctorForm.password" type="password" placeholder="留空表示不修改密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword" v-if="doctorForm.id && doctorForm.password">
          <el-input v-model="doctorForm.confirmPassword" type="password" placeholder="请再次输入密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialog.visible = false">取 消</el-button>
          <el-button type="primary" @click="handleSubmit">确 定</el-button>
        </div>
      </template>
    </el-dialog>


  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

import {
  getDoctorList,
  createDoctor,
  updateDoctor,
  deleteDoctor,
  getDoctorById,
  getUserByUsername
} from '@/api/doctor'
import { getDepartmentList } from '@/api/department'
import { register } from '@/api/user'

const loading = ref(false)
const total = ref(0)
const doctorList = ref([])
const doctorFormRef = ref()
const departmentOptions = ref([])

const queryParams = reactive({
  page: 1,
  size: 10,
  name: '',
  departmentId: undefined,
  status: undefined
})

const dialog = reactive({
  visible: false,
  title: ''
})



const doctorForm = reactive({
  id: undefined,
  name: '',
  title: '',
  departmentId: undefined,
  specialty: '',
  introduction: '',
  photoUrl: '',
  status: 1,
  username: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

const rules = {
  name: [{ required: true, message: '请输入医生姓名', trigger: 'blur' }],
  title: [{ required: true, message: '请输入职称', trigger: 'blur' }],
  departmentId: [{ required: true, message: '请选择所属科室', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { type: 'email', message: '请输入正确的电子邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3456789]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  password: [{ 
    required: (rule, value, callback) => !doctorForm.id, 
    message: '请输入密码', 
    trigger: 'blur' 
  }],
  confirmPassword: [
    { 
      required: (rule, value, callback) => !doctorForm.id || (doctorForm.id && doctorForm.password),
      message: '请再次输入密码', 
      trigger: 'blur' 
    },
    { 
      validator: (rule, value, callback) => {
        if (value !== doctorForm.password) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      }, 
      trigger: 'blur' 
    }
  ]
}

// 获取科室列表
const fetchDepartmentOptions = async () => {
  try {
    const response = await getDepartmentList({ page: 0, size: 100 })
    console.log('科室数据响应:', response);
    
    // 根据不同的响应结构处理数据
    if (response && response.content) {
      // 标准分页结构
      departmentOptions.value = response.content;
    } else if (response && Array.isArray(response)) {
      // 直接返回数组
      departmentOptions.value = response;
    } else if (response && response.data) {
      // 嵌套结构
      if (response.data.content) {
        departmentOptions.value = response.data.content;
      } else if (Array.isArray(response.data)) {
        departmentOptions.value = response.data;
      } else {
        departmentOptions.value = [];
      }
    } else {
      departmentOptions.value = [];
    }
  } catch (error) {
    console.error('获取科室列表失败:', error)
    departmentOptions.value = [];
  }
}

// 查询医生列表
const getList = async () => {
  try {
    loading.value = true
    const response = await getDoctorList({
      ...queryParams,
      page: queryParams.page - 1 // 后端页码从0开始
    })
    console.log('医生数据响应:', response);
    
    // 根据不同的响应结构处理数据
    if (response && response.content) {
      // 标准分页结构
      doctorList.value = response.content;
      total.value = response.totalElements;
    } else if (response && Array.isArray(response)) {
      // 直接返回数组
      doctorList.value = response;
      total.value = response.length;
    } else if (response && response.data) {
      // 嵌套结构
      if (response.data.content) {
        doctorList.value = response.data.content;
        total.value = response.data.totalElements;
      } else if (Array.isArray(response.data)) {
        doctorList.value = response.data;
        total.value = response.data.length;
      } else {
        doctorList.value = [];
        total.value = 0;
      }
    } else {
      doctorList.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error('获取医生列表失败:', error)
    doctorList.value = [];
    total.value = 0;
  } finally {
    loading.value = false
  }
}

// 查询按钮操作
const handleQuery = () => {
  queryParams.page = 1
  getList()
}

// 重置按钮操作
const resetQuery = () => {
  queryParams.name = ''
  queryParams.departmentId = undefined
  queryParams.status = undefined
  handleQuery()
}

// 显示创建对话框
const showCreateDialog = () => {
  // 重置表单数据
  Object.keys(doctorForm).forEach(key => {
    if (typeof doctorForm[key] === 'string') {
      doctorForm[key] = '';
    } else if (typeof doctorForm[key] === 'boolean') {
      doctorForm[key] = true;
    } else if (typeof doctorForm[key] === 'number') {
      doctorForm[key] = undefined;
    } else {
      doctorForm[key] = undefined;
    }
  });
  
  doctorForm.id = null;
  doctorForm.status = 1;
  
  // 重置验证
  if (doctorFormRef.value) {
    doctorFormRef.value.clearValidate();
  }
  
  dialog.title = '添加医生';
  dialog.visible = true;
};

// 显示编辑对话框
const showEditDialog = async (row) => {
  try {
    loading.value = true;
    
    // 获取医生的完整信息
    const response = await getDoctorById(row.id);
    const doctorData = response.data || response;
    
    // 复制数据到表单
    Object.keys(doctorForm).forEach(key => {
      if (key in doctorData) {
        doctorForm[key] = doctorData[key];
      }
    });
    
    // 特殊处理密码字段 - 编辑时不显示密码
    doctorForm.password = '';
    doctorForm.confirmPassword = '';
    
    // 重置验证
    if (doctorFormRef.value) {
      doctorFormRef.value.clearValidate();
    }
    
    dialog.title = '编辑医生';
    dialog.visible = true;
  } catch (error) {
    console.error('获取医生详情失败:', error);
    ElMessage.error('获取医生详情失败');
  } finally {
    loading.value = false;
  }
};

// 删除按钮操作
const handleDelete = (row) => {
  ElMessageBox.confirm('确认要删除该医生吗？', '警告', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteDoctor(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('删除医生失败:', error)
    }
  })
}

// 监听密码变化，确保confirmPassword始终与password一致
watch(() => doctorForm.password, (newVal) => {
  // 始终保持确认密码与密码一致
  doctorForm.confirmPassword = newVal
})

// 处理表单提交
const handleSubmit = () => {
  doctorFormRef.value.validate(async valid => {
    if (valid) {
      const submitData = { ...doctorForm };
      loading.value = true;
      
      try {
        if (submitData.id) {
          // 编辑模式
          const response = await updateDoctor(submitData.id, submitData);
          ElMessage.success('医生信息更新成功');
          dialog.visible = false;
          getList();
        } else {
          // 新增模式 - 直接调用后端的create接口，它会同时创建用户和医生
          // 确保必要的字段有值
          if (!submitData.password) {
            submitData.password = '123456';
            submitData.confirmPassword = '123456';
          }
          if (!submitData.email) {
            submitData.email = `${submitData.username}@hospital.com`;
          }
          if (!submitData.phone) {
            submitData.phone = '13800000000';
          }
          
          const response = await createDoctor(submitData);
          ElMessage.success('医生创建成功');
          dialog.visible = false;
          getList();
        }
      } catch (error) {
        console.error('操作失败:', error);
        const errorMsg = error.response?.data?.message || error.message || '未知错误';
        ElMessage.error(`操作失败: ${errorMsg}`);
      } finally {
        loading.value = false;
      }
    } else {
      return false;
    }
  });
};

// 对话框关闭时重置表单
const handleDialogClose = () => {
  if (doctorFormRef.value) {
    doctorFormRef.value.resetFields()
  }
}

// 分页大小改变
const handleSizeChange = (val) => {
  queryParams.size = val
  getList()
}

// 页码改变
const handleCurrentChange = (val) => {
  queryParams.page = val
  getList()
}

// 上传头像之前的钩子
const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('上传头像图片只能是 JPG/PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 头像上传成功的回调
const handleAvatarSuccess = (response) => {
  doctorForm.photoUrl = response.data
}



onMounted(() => {
  fetchDepartmentOptions()
  getList()
})
</script>

<style lang="scss" scoped>
.doctor-container {
  padding: 20px;

  .doctor-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }

  :deep(.el-pagination) {
    margin-top: 20px;
    justify-content: flex-end;
  }
}

.avatar-uploader {
  :deep(.el-upload) {
    border: 1px dashed var(--el-border-color);
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);

    &:hover {
      border-color: var(--el-color-primary);
    }
  }
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
  line-height: 100px;
}

.avatar {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
}
</style>