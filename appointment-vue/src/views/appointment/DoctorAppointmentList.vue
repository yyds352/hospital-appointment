<template>
  <div class="app-container">
    <div class="filter-container">
      <el-date-picker
        v-model="queryParams.date"
        type="date"
        placeholder="选择日期"
        @change="handleDateChange"
      />
      <el-select v-model="queryParams.status" placeholder="预约状态" clearable @change="handleFilter">
        <el-option label="待确认" value="PENDING" />
        <el-option label="已确认" value="CONFIRMED" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="已取消" value="CANCELLED" />
      </el-select>
      <el-button type="primary" @click="handleFilter">查询</el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="appointmentList"
      border
      style="width: 100%"
      row-key="id"
    >
      <el-table-column label="预约时间" prop="appointmentTime" width="180">
        <template #default="scope">
          {{ formatDateTime(scope.row.appointmentTime) }}
        </template>
      </el-table-column>
      <el-table-column label="患者姓名" prop="patientName" />
      <el-table-column label="科室" prop="departmentName" />
      <el-table-column label="症状描述" prop="description" show-overflow-tooltip />
      <el-table-column label="状态" prop="status" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === 'PENDING'"
            type="success"
            size="small"
            @click="handleConfirm(scope.row)"
          >
            确认
          </el-button>
          <el-button
            v-if="scope.row.status === 'CONFIRMED'"
            type="primary"
            size="small"
            @click="handleComplete(scope.row)"
          >
            完成就诊
          </el-button>
          <el-button
            type="info"
            size="small"
            @click="handleViewDetails(scope.row)"
          >
            查看详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-show="total > 0"
      :total="total"
      v-model:current-page="queryParams.pageNum"
      v-model:page-size="queryParams.pageSize"
      @current-change="handleCurrentChange"
      @size-change="handleSizeChange"
      layout="total, sizes, prev, pager, next"
    />

    <!-- 预约详情对话框 -->
    <el-dialog
      v-model="detailsVisible"
      title="预约详情"
      width="500px"
    >
      <div v-if="currentAppointment">
        <p><strong>预约时间：</strong>{{ formatDateTime(currentAppointment.appointmentTime) }}</p>
        <p><strong>患者姓名：</strong>{{ currentAppointment.patientName }}</p>
        <p><strong>科室：</strong>{{ currentAppointment.departmentName }}</p>
        <p><strong>症状描述：</strong>{{ currentAppointment.description }}</p>
        <p><strong>状态：</strong>{{ getStatusText(currentAppointment.status) }}</p>
        <p><strong>创建时间：</strong>{{ formatDateTime(currentAppointment.createTime) }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDoctorAppointments, updateAppointmentStatus } from '@/api/appointment'
import { getDoctorByUserId } from '@/api/doctor'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'

const store = useStore()
const router = useRouter()
const loading = ref(false)
const total = ref(0)
const appointmentList = ref([])
const detailsVisible = ref(false)
const currentAppointment = ref(null)
const doctorInfo = ref(null)

const userInfo = computed(() => store.state.user.userInfo)
const userRole = computed(() => userInfo.value?.role)
const userId = computed(() => userInfo.value?.id)

// 获取医生信息
const getDoctorInfo = async () => {
  try {
    const response = await getDoctorByUserId(userId.value)
    console.log('Doctor info response:', response)
    
    if (!response) {
      throw new Error('返回数据为空')
    }

    // 后端返回的数据直接就是医生信息对象，不需要检查data字段
    if (!response.id) {
      throw new Error('医生ID不存在')
    }
    
    // 确保医生记录存在且userId匹配
    if (response.userId !== userId.value) {
      console.warn('医生记录的userId与当前用户不匹配:', { 
        doctorUserId: response.userId, 
        currentUserId: userId.value 
      })
      throw new Error('医生信息与当前用户不匹配')
    }
    
    doctorInfo.value = response
    console.log('Doctor info set:', doctorInfo.value)
    return response
  } catch (error) {
    console.error('获取医生信息失败：', error)
    ElMessage.error(error.message || '获取医生信息失败')
    return null
  }
}

// 检查用户权限
onMounted(async () => {
  console.log('Current user info:', userInfo.value)
  console.log('User role:', userRole.value)
  console.log('User ID:', userId.value)
  
  if (userRole.value !== 'DOCTOR') {
    ElMessage.error('无权访问此页面')
    router.push('/home')
    return
  }
  if (!userId.value) {
    ElMessage.error('获取用户信息失败')
    router.push('/home')
    return
  }

  try {
    // 获取医生信息
    const doctor = await getDoctorInfo()
    if (!doctor) {
      ElMessage.error('获取医生信息失败')
      router.push('/home')
      return
    }
    await getList()
  } catch (error) {
    console.error('初始化失败：', error)
    ElMessage.error(error.message || '初始化失败')
    router.push('/home')
  }
})

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  date: null,
  status: ''
})

// 获取预约列表
const getList = async () => {
  if (!doctorInfo.value?.id) {
    console.error('医生信息未找到')
    ElMessage.error('获取医生信息失败')
    return
  }

  loading.value = true
  try {
    // 使用医生ID
    const doctorId = doctorInfo.value.id
    console.log('Fetching appointments for doctor:', doctorId)
    
    const params = {
      page: queryParams.value.pageNum ? queryParams.value.pageNum - 1 : 0,
      size: queryParams.value.pageSize || 10,
      sort: 'appointment_time,desc'
    }
    if (queryParams.value.date) {
      params.date = queryParams.value.date
    }
    if (queryParams.value.status) {
      params.status = queryParams.value.status
    }
    console.log('Request params:', params)
    
    const response = await getDoctorAppointments(doctorId, params)
    console.log('Appointments response:', response)
    
    if (!response) {
      throw new Error('返回数据为空')
    }

    // 直接使用response作为响应数据，不再检查data属性
    const content = response.content || []
    const totalElements = response.totalElements || 0
    console.log('Appointments content:', content)
    console.log('Appointments total:', totalElements)
    appointmentList.value = content
    total.value = totalElements
  } catch (error) {
    console.error('获取预约列表失败：', error)
    ElMessage.error(error.message || '获取预约列表失败')
  } finally {
    loading.value = false
  }
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return ''
  const date = new Date(datetime)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 获取状态显示文本
const getStatusText = (status) => {
  const statusMap = {
    PENDING: '待确认',
    CONFIRMED: '已确认',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return statusMap[status] || status
}

// 获取状态标签类型
const getStatusType = (status) => {
  const typeMap = {
    PENDING: 'warning',
    CONFIRMED: 'primary',
    COMPLETED: 'success',
    CANCELLED: 'info'
  }
  return typeMap[status] || ''
}

// 确认预约
const handleConfirm = async (row) => {
  try {
    await ElMessageBox.confirm('确认这个预约吗？')
    await updateAppointmentStatus(row.id, 'CONFIRMED')
    ElMessage.success('预约已确认')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认预约失败：', error)
      ElMessage.error('确认预约失败')
    }
  }
}

// 完成就诊
const handleComplete = async (row) => {
  try {
    await ElMessageBox.confirm('确认完成就诊吗？')
    await updateAppointmentStatus(row.id, 'COMPLETED')
    ElMessage.success('就诊已完成')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('完成就诊失败：', error)
      ElMessage.error('完成就诊失败')
    }
  }
}

// 查看详情
const handleViewDetails = (row) => {
  currentAppointment.value = row
  detailsVisible.value = true
}

// 处理日期变化
const handleDateChange = () => {
  handleFilter()
}

// 处理筛选
const handleFilter = () => {
  queryParams.value.pageNum = 1
  getList()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  queryParams.value.pageNum = val
  getList()
}

// 处理每页条数变化
const handleSizeChange = (val) => {
  queryParams.value.pageSize = val
  queryParams.value.pageNum = 1
  getList()
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.filter-container {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.el-pagination {
  margin-top: 20px;
  justify-content: center;
}
</style>