<template>
  <div class="user-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="handleAddUser">添加用户</el-button>
        </div>
      </template>

      <!-- 搜索和筛选 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="请选择角色" clearable>
            <el-option label="全部" value="" />
            <el-option label="学生" value="STUDENT" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="医生" value="DOCTOR" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 用户列表 -->
      <el-table :data="userList" v-loading="loading" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="scope">
            <el-tag :type="getRoleType(scope.row.role)">
              {{ formatRole(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="handleView(scope.row)">查看</el-button>
            <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="warning" link @click="handleResetPassword(scope.row)">重置密码</el-button>
            <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; text-align: right;"
      />
    </el-card>

    <!-- 用户详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="用户详情"
      width="600px"
    >
      <el-form :model="currentUser" label-width="100px">
        <el-form-item label="用户ID">{{ currentUser.id }}</el-form-item>
        <el-form-item label="用户名">{{ currentUser.username }}</el-form-item>
        <el-form-item label="姓名">{{ currentUser.name }}</el-form-item>
        <el-form-item label="角色">{{ formatRole(currentUser.role) }}</el-form-item>
        <el-form-item label="手机号">{{ currentUser.phone }}</el-form-item>
        <el-form-item label="邮箱">{{ currentUser.email }}</el-form-item>
        <el-form-item label="状态">{{ currentUser.status === 1 ? '启用' : '禁用' }}</el-form-item>
        <el-form-item label="创建时间">{{ currentUser.createdAt }}</el-form-item>
        <el-form-item label="更新时间">{{ currentUser.updatedAt }}</el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, deleteUser, resetUserPassword, updateUserStatus } from '@/api/user'

export default {
  name: 'UserManagement',
  setup() {
    // 路由
    const router = useRouter()
    
    // 数据
    const loading = ref(false)
    const userList = ref([])
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    const detailDialogVisible = ref(false)
    const currentUser = ref({})

    // 搜索表单
    const searchForm = reactive({
      username: '',
      name: '',
      role: ''
    })

    // 方法
    const loadUserList = async () => {
      loading.value = true
      try {
        const params = {
          page: currentPage.value - 1,
          size: pageSize.value,
          ...searchForm
        }
        
        console.log('=== 获取用户列表参数 ===', params)
        const response = await getUserList(params)
        console.log('=== 获取用户列表响应 ===', response)
        
        // 注意：响应拦截器已经处理了数据结构，直接检查数据
        if (response && response.content) {
          userList.value = response.content
          total.value = response.totalElements
          console.log('✓ 用户列表加载成功，总数:', total.value)
        } else {
          console.error('✗ 获取用户列表数据格式错误:', response)
          ElMessage.error('获取用户列表数据格式错误')
        }
      } catch (error) {
        console.error('✗ 获取用户列表异常:', error)
        console.error('错误详情:', {
          message: error.message,
          response: error.response,
          request: error.request,
          config: error.config
        })
        ElMessage.error('获取用户列表失败: ' + (error.message || '网络错误'))
      } finally {
        loading.value = false
      }
    }

    const handleSearch = () => {
      currentPage.value = 1
      loadUserList()
    }

    const handleReset = () => {
      searchForm.username = ''
      searchForm.name = ''
      searchForm.role = ''
      handleSearch()
    }

    const handleSizeChange = (val) => {
      pageSize.value = val
      loadUserList()
    }

    const handleCurrentChange = (val) => {
      currentPage.value = val
      loadUserList()
    }

    const handleView = (row) => {
      currentUser.value = { ...row }
      detailDialogVisible.value = true
    }

    const handleEdit = (row) => {
      // 跳转到编辑页面或打开编辑对话框
      router.push(`/home/system/user-edit/${row.id}`)
    }

    const handleAddUser = () => {
      // 跳转到添加用户页面或打开添加对话框
      router.push('/home/system/user-add')
    }

    const handleStatusChange = async (row) => {
      try {
        const response = await updateUserStatus(row.id, row.status)
        // 响应拦截器已经处理了数据结构，直接检查数据
        if (response && (response.success === true || response.status === 200)) {
          ElMessage.success('状态更新成功')
        } else {
          ElMessage.error('状态更新失败')
          // 恢复状态
          row.status = row.status === 1 ? 0 : 1
        }
      } catch (error) {
        console.error('状态更新失败:', error)
        ElMessage.error('状态更新失败')
        // 恢复状态
        row.status = row.status === 1 ? 0 : 1
      }
    }

    const handleResetPassword = async (row) => {
      try {
        await ElMessageBox.confirm(
          `确定要重置用户 "${row.name}" 的密码吗？`,
          '重置密码确认',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )

        const response = await resetUserPassword(row.id)
        // 响应拦截器已经处理了数据结构，直接检查数据
        if (response && (response.success === true || response.status === 200)) {
          ElMessage.success('密码重置成功，新密码已发送到用户邮箱')
        } else {
          ElMessage.error('密码重置失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('密码重置失败:', error)
          ElMessage.error('密码重置失败')
        }
      }
    }

    const handleDelete = async (row) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除用户 "${row.name}" 吗？`,
          '删除确认',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )

        const response = await deleteUser(row.id)
        // 响应拦截器已经处理了数据结构，删除接口成功时返回null是正常的
        ElMessage.success('删除成功')
        loadUserList()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败:', error)
          ElMessage.error('删除失败')
        }
      }
    }

    // 工具方法
    const formatRole = (role) => {
      const roleMap = {
        'STUDENT': '学生',
        'TEACHER': '教师',
        'DOCTOR': '医生',
        'ADMIN': '管理员'
      }
      return roleMap[role] || role
    }

    const getRoleType = (role) => {
      const typeMap = {
        'STUDENT': 'info',
        'TEACHER': 'success',
        'DOCTOR': 'warning',
        'ADMIN': 'danger'
      }
      return typeMap[role] || 'info'
    }

    // 生命周期
    onMounted(() => {
      loadUserList()
    })

    return {
      loading,
      userList,
      currentPage,
      pageSize,
      total,
      searchForm,
      detailDialogVisible,
      currentUser,
      loadUserList,
      handleSearch,
      handleReset,
      handleSizeChange,
      handleCurrentChange,
      handleView,
      handleEdit,
      handleAddUser,
      handleStatusChange,
      handleResetPassword,
      handleDelete,
      formatRole,
      getRoleType
    }
  }
}
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.el-table {
  margin-top: 20px;
}
</style>