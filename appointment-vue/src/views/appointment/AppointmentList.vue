<template>
  <div class="appointment-list">
    <!-- 快速预约入口 -->
    <el-card class="quick-entrance-card">
      <div class="quick-entrance">
        <div class="entrance-content">
          <el-icon class="entrance-icon"><Clock /></el-icon>
          <div class="entrance-text">
            <h3>快速预约</h3>
            <p>三步完成预约，简单快捷</p>
          </div>
        </div>
        <el-button type="primary" size="large" @click="goToQuickAppointment">
          立即预约
        </el-button>
      </div>
    </el-card>

    <!-- 查询表单 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="科室">
          <el-select
            v-model="searchForm.departmentId"
            placeholder="请选择科室"
            clearable
            @change="handleDepartmentChange"
          >
            <el-option
              v-for="dept in departmentList"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="医生">
          <el-select
            v-model="searchForm.doctorId"
            placeholder="请选择医生"
            clearable
            :disabled="!searchForm.departmentId"
            @change="handleDoctorChange"
          >
            <el-option
              v-for="doctor in doctorList"
              :key="doctor.id"
              :label="doctor.name"
              :value="doctor.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="searchForm.date"
            type="date"
            placeholder="选择日期"
            :disabled-date="disabledDate"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 医生信息展示 -->
    <el-row v-if="selectedDoctor" :gutter="20" class="doctor-info">
      <el-col :span="6">
        <el-card>
          <div class="doctor-profile">
            <el-avatar
              :size="100"
              :src="selectedDoctor.avatar || '/default-avatar.png'"
            />
            <h3>{{ selectedDoctor.name }}</h3>
            <p class="title">{{ selectedDoctor.title }}</p>
            <p class="department">{{ selectedDoctor.departmentName }}</p>
            <div class="specialty">
              <h4>专业特长</h4>
              <p>{{ selectedDoctor.specialty }}</p>
            </div>
            <div class="introduction">
              <h4>简介</h4>
              <p>{{ selectedDoctor.introduction }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="18">
        <el-card>
          <template #header>
            <div class="schedule-header">
              <span>排班信息</span>
              <div class="date-navigation">
                <el-button-group>
                  <el-button @click="changeWeek(-1)">上一周</el-button>
                  <el-button @click="changeWeek(1)">下一周</el-button>
                </el-button-group>
              </div>
            </div>
          </template>
          <el-table
            v-loading="loading"
            :data="scheduleList"
            style="width: 100%"
          >
            <el-table-column prop="scheduleDate" label="日期" width="120">
              <template #default="{ row }">
                {{ formatDate(row.scheduleDate) }}
                <br>
                <small>{{ getDayOfWeek(row.scheduleDate) }}</small>
              </template>
            </el-table-column>
            <el-table-column label="时段" width="100">
              <template #default="{ row }">
                {{ row.period === 'MORNING' ? '上午' : '下午' }}
              </template>
            </el-table-column>
            <el-table-column prop="maxAppointments" label="总号数" width="100" />
            <el-table-column prop="availableAppointments" label="剩余号数" width="100">
              <template #default="{ row }">
                <el-tag :type="getAvailabilityTagType(row.availableAppointments)">
                  {{ row.availableAppointments }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="congestionLevel" label="拥挤度" width="100">
              <template #default="{ row }">
                <el-tag :type="getCongestionTagType(row.congestionLevel)" size="small">
                  {{ getCongestionText(row.congestionLevel) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="bookedAppointments" label="已约/总数" width="120">
              <template #default="{ row }">
                <div style="font-size: 12px; color: #909399;">
                  {{ row.bookedAppointments || 0 }}/{{ row.maxAppointments }}
                </div>
                <el-progress 
                  :percentage="getCongestionPercentage(row)" 
                  :color="getCongestionColor(row.congestionLevel)"
                  :stroke-width="6"
                  :show-text="false"
                />
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="row.status ? 'success' : 'danger'">
                  {{ row.status ? '可预约' : '停诊' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" width="120">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  link
                  :disabled="!canBook(row)"
                  @click="handleBook(row)"
                >
                  预约
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 预约表单对话框 -->
    <el-dialog
      v-model="bookingDialogVisible"
      title="预约确认"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="bookingFormRef"
        :model="bookingForm"
        :rules="bookingRules"
        label-width="100px"
      >
        <el-form-item label="就诊人姓名" prop="patientName">
          <el-input v-model="bookingForm.patientName" />
        </el-form-item>
        <el-form-item label="联系电话" prop="patientPhone">
          <el-input v-model="bookingForm.patientPhone" />
        </el-form-item>
        <el-form-item label="身份证号" prop="patientIdCard">
          <el-input v-model="bookingForm.patientIdCard" />
        </el-form-item>
        <el-form-item label="备注说明">
          <el-input
            v-model="bookingForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息（选填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="bookingDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmBooking">确认预约</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 预约成功提示框 -->
    <el-dialog
      v-model="successDialogVisible"
      title="预约成功"
      width="400px"
      destroy-on-close
    >
      <el-result
        icon="success"
        title="预约成功"
        :sub-title="successMessage"
      >
        <template #extra>
          <el-button type="primary" @click="viewAppointment">查看预约详情</el-button>
        </template>
      </el-result>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { getDepartmentList } from '@/api/department'
import { getDoctorsByDepartment, getDoctorById } from '@/api/doctor'
import { getDoctorSchedules } from '@/api/schedule'
import { createAppointment, getPatientAppointments } from '@/api/appointment'
import { formatDate } from '@/utils/format'
import { Clock } from '@element-plus/icons-vue'

const router = useRouter()
const store = useStore()

// 数据定义
const loading = ref(false)
const departmentList = ref([])
const doctorList = ref([])
const scheduleList = ref([])
const selectedDoctor = ref(null)
const selectedSchedule = ref(null)
const bookingDialogVisible = ref(false)
const successDialogVisible = ref(false)
const successMessage = ref('')
const bookingFormRef = ref(null)

// 表单数据
const searchForm = ref({
  departmentId: '',
  doctorId: '',
  date: ''
})

const bookingForm = ref({
  patientName: '',
  patientPhone: '',
  patientIdCard: '',
  remark: ''
})

// 表单验证规则
const bookingRules = {
  patientName: [
    { required: true, message: '请输入就诊人姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  patientPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  patientIdCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号', trigger: 'blur' }
  ]
}

// 获取科室列表
const fetchDepartments = async () => {
  try {
    console.log('正在获取科室列表')
    const response = await getDepartmentList({ page: 0, size: 100 })
    console.log('getDepartmentList响应:', response)
    
    // 响应直接就是数据本身，不需要检查code
    if (response) {
      // 处理分页数据或者直接数组
      if (response.content) {
        departmentList.value = response.content
      } else if (Array.isArray(response)) {
        departmentList.value = response
      } else {
        departmentList.value = []
      }
    } else {
      console.error('获取科室列表失败，无响应数据')
      ElMessage.error('获取科室列表失败')
    }
  } catch (error) {
    console.error('获取科室列表发生异常:', error)
    ElMessage.error('获取科室列表失败')
  }
}

// 获取医生列表
const fetchDoctors = async (departmentId) => {
  try {
    console.log('正在获取科室ID为', departmentId, '的医生列表')
    const response = await getDoctorsByDepartment(departmentId)
    console.log('getDoctorsByDepartment响应:', response)
    
    // 响应直接就是数据本身，不需要检查code
    if (response) {
      if (response.content) {
        doctorList.value = response.content
      } else if (Array.isArray(response)) {
        doctorList.value = response
      } else {
        doctorList.value = []
      }
    } else {
      console.error('获取医生列表失败，无响应数据')
      ElMessage.error('获取医生列表失败')
    }
  } catch (error) {
    console.error('获取医生列表发生异常:', error)
    ElMessage.error('获取医生列表失败')
  }
}

// 获取医生详情
const fetchDoctorInfo = async (doctorId) => {
  try {
    const response = await getDoctorById(doctorId)
    console.log('getDoctorById响应:', response)
    
    // 响应直接就是数据本身，不需要检查code
    if (response) {
      selectedDoctor.value = response
    } else {
      console.error('获取医生信息失败，无响应数据')
      ElMessage.error('获取医生信息失败')
    }
  } catch (error) {
    console.error('获取医生信息发生异常:', error)
    ElMessage.error('获取医生信息失败')
  }
}

// 获取医生排班信息
const fetchSchedules = async () => {
  if (!searchForm.value.doctorId) return
  
  loading.value = true
  try {
    console.log('正在获取医生排班信息，医生ID:', searchForm.value.doctorId, '请求参数:', {
      departmentId: searchForm.value.departmentId,
      date: searchForm.value.date
    })
    const response = await getDoctorSchedules(searchForm.value.doctorId, {
      departmentId: searchForm.value.departmentId,
      date: searchForm.value.date
    })
    
    console.log('getDoctorSchedules响应:', response)
    
    // 检查响应结构
    let scheduleData = []
    if (response && response.code === 200 && response.data) {
      // 如果响应有code和data结构
      scheduleData = Array.isArray(response.data) ? response.data : []
    } else if (Array.isArray(response)) {
      // 如果响应直接是数组
      scheduleData = response
    } else if (response && response.data) {
      // 如果响应有data字段
      scheduleData = Array.isArray(response.data) ? response.data : []
    } else {
      console.error('获取排班信息失败，响应格式不正确:', response)
      ElMessage.error('获取排班信息失败')
      scheduleList.value = []
      return
    }
    
    console.log('处理后的排班数据:', scheduleData)
    scheduleList.value = scheduleData
    
    // 调试：检查每个排班的可预约状态
    scheduleData.forEach((schedule, index) => {
      console.log(`排班 ${index + 1} 详情:`, {
        id: schedule.id,
        scheduleDate: schedule.scheduleDate,
        period: schedule.period,
        status: schedule.status,
        availableAppointments: schedule.availableAppointments,
        maxAppointments: schedule.maxAppointments,
        canBook: canBook(schedule)
      })
    })
  } catch (error) {
    console.error('获取排班信息失败:', error)
    ElMessage.error('获取排班信息失败')
    scheduleList.value = []
  } finally {
    loading.value = false
  }
}

// 处理科室变化
const handleDepartmentChange = async (value) => {
  searchForm.value.doctorId = ''
  selectedDoctor.value = null
  scheduleList.value = []
  if (value) {
    await fetchDoctors(value)
  }
}

// 处理医生变化
const handleDoctorChange = async (value) => {
  scheduleList.value = []
  if (value) {
    await fetchDoctorInfo(value)
    await fetchSchedules()
  } else {
    selectedDoctor.value = null
  }
}

// 处理查询
const handleSearch = () => {
  if (!searchForm.value.doctorId) {
    ElMessage.warning('请选择医生')
    return
  }
  fetchSchedules()
}

// 重置查询
const resetSearch = () => {
  searchForm.value = {
    departmentId: '',
    doctorId: '',
    date: ''
  }
  selectedDoctor.value = null
  scheduleList.value = []
}

// 切换周次
const changeWeek = (offset) => {
  if (!searchForm.value.date) {
    searchForm.value.date = formatDate(new Date())
  }
  const date = new Date(searchForm.value.date)
  date.setDate(date.getDate() + offset * 7)
  searchForm.value.date = formatDate(date)
  fetchSchedules()
}

// 获取星期几
const getDayOfWeek = (dateStr) => {
  const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const date = new Date(dateStr)
  return days[date.getDay()]
}

// 获取剩余号数标签类型
const getAvailabilityTagType = (number) => {
  if (number <= 0) return 'danger'
  if (number <= 5) return 'warning'
  return 'success'
}

// 获取拥挤度标签类型
const getCongestionTagType = (level) => {
  switch (level) {
    case 'LOW': return 'success'
    case 'MEDIUM': return 'warning' 
    case 'HIGH': return 'danger'
    case 'FULL': return 'danger'
    default: return 'info'
  }
}

// 获取拥挤度文本
const getCongestionText = (level) => {
  switch (level) {
    case 'LOW': return '空闲'
    case 'MEDIUM': return '一般'
    case 'HIGH': return '拥挤'
    case 'FULL': return '已满'
    default: return '未知'
  }
}

// 获取拥挤度百分比
const getCongestionPercentage = (row) => {
  if (!row.maxAppointments || row.maxAppointments <= 0) return 0
  const booked = row.bookedAppointments || 0
  return Math.round((booked / row.maxAppointments) * 100)
}

// 获取拥挤度颜色
const getCongestionColor = (level) => {
  switch (level) {
    case 'LOW': return '#67C23A'
    case 'MEDIUM': return '#E6A23C'
    case 'HIGH': return '#F56C6C'
    case 'FULL': return '#909399'
    default: return '#409EFF'
  }
}

// 判断是否可以预约
const canBook = (schedule) => {
  if (!schedule) return false
  
  // 检查是否在工作时间内（仅对当天的排班应用时间限制）
  const now = new Date()
  const scheduleDate = new Date(schedule.scheduleDate)
  const isSameDay = now.getFullYear() === scheduleDate.getFullYear() &&
                   now.getMonth() === scheduleDate.getMonth() &&
                   now.getDate() === scheduleDate.getDate()
                   
  if (isSameDay) {
    const hour = now.getHours()
    // 上午时段：12点后不能预约当天的上午号
    if (schedule.period === 'MORNING' && hour >= 12) {
      return false
    }
    // 下午时段：17点后不能预约当天的下午号
    if (schedule.period === 'AFTERNOON' && hour >= 17) {
      return false
    }
  }
  
  // 检查排班状态、剩余名额和日期
  // 注意：后端返回的status是数字（1表示可预约，0表示停诊），不是布尔值
  const isAvailable = schedule.status === 1 || schedule.status === true
  const hasAvailableSlots = schedule.availableAppointments > 0
  const isFutureDate = scheduleDate >= new Date(new Date().setHours(0, 0, 0, 0))
  
  console.log('canBook检查:', {
    schedule: schedule,
    isAvailable: isAvailable,
    hasAvailableSlots: hasAvailableSlots,
    isFutureDate: isFutureDate,
    status: schedule.status,
    availableAppointments: schedule.availableAppointments
  })
  
  return isAvailable && hasAvailableSlots && isFutureDate
}

// 处理预约
const handleBook = async (schedule) => {
  selectedSchedule.value = schedule
  const userInfo = store.state.user.userInfo
  
  // 先检查用户是否在该日期已有预约
  try {
    const existingAppointmentsResponse = await getPatientAppointments({
      date: schedule.scheduleDate,
      page: 1,
      limit: 10
    })
    
    console.log('检查用户在该日期的预约:', existingAppointmentsResponse)
    
    let hasExistingAppointment = false
    if (existingAppointmentsResponse?.code === 200 && existingAppointmentsResponse.data?.content) {
      const appointments = existingAppointmentsResponse.data.content
      hasExistingAppointment = appointments.some(apt => apt.appointmentTime.startsWith(schedule.scheduleDate))
    } else if (existingAppointmentsResponse?.data?.content) {
      const appointments = existingAppointmentsResponse.data.content
      hasExistingAppointment = appointments.some(apt => apt.appointmentTime.startsWith(schedule.scheduleDate))
    }
    
    if (hasExistingAppointment) {
      ElMessage.warning({
        message: '您在该日期已有预约，请选择其他日期',
        duration: 5000,
        showClose: true
      })
      return
    }
  } catch (error) {
    console.error('检查用户预约失败:', error)
    // 如果检查失败，仍然允许用户尝试预约，让后端来做最终验证
  }
  
  bookingForm.value = {
    patientName: userInfo?.name || '',
    patientPhone: userInfo?.phone || '',
    patientIdCard: userInfo?.idCard || '',
    remark: ''
  }
  bookingDialogVisible.value = true
}

// 确认预约
const confirmBooking = async () => {
  if (!bookingFormRef.value) return
  
  if (!searchForm.value.departmentId) {
    ElMessage.warning('请选择科室')
    return
  }
  
  if (!searchForm.value.doctorId) {
    ElMessage.warning('请选择医生')
    return
  }
  
  if (!selectedSchedule.value) {
    ElMessage.warning('请选择预约时段')
    return
  }
  
  try {
    await bookingFormRef.value.validate()
    
    // 构建预约时间（使用本地时间）
    const appointmentDate = selectedSchedule.value.scheduleDate;
    const appointmentTime = selectedSchedule.value.period === 'MORNING' ? '09:00:00' : '14:00:00';
    const appointmentDateTime = `${appointmentDate}T${appointmentTime}`;
    
    const response = await createAppointment({
      scheduleId: selectedSchedule.value.id,
      departmentId: searchForm.value.departmentId,
      doctorId: searchForm.value.doctorId,
      appointmentTime: appointmentDateTime,
      ...bookingForm.value
    })
    
    // 由于响应拦截器已经处理了code检查，这里直接处理成功情况
    bookingDialogVisible.value = false
    // 先显示消息提示，让它更明显，并增加显示时间
    ElMessage({
      message: response.successMessage || '预约申请已提交成功，请留意预约确认信息',
      type: 'success',
      duration: 5000,
      showClose: true
    })
    // 然后再显示成功对话框
    setTimeout(() => {
      successDialogVisible.value = true
      successMessage.value = `预约成功！预约号：${response.appointmentNumber || response.id}`
    }, 300)
    await fetchSchedules()
  } catch (error) {
    console.error('预约失败详情:', error)
    
    // 获取后端返回的错误信息
    let errorMessage = '预约失败，请重试'
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message
    } else if (error.response?.data?.msg) {
      errorMessage = error.response.data.msg
    } else if (error.message) {
      errorMessage = error.message
    }
    
    // 特殊处理常见错误情况
    if (errorMessage.includes('已有预约') || errorMessage.includes('重复预约')) {
      errorMessage = '您在该日期已有预约，请选择其他日期或医生'
    } else if (errorMessage.includes('号源已满') || errorMessage.includes('名额已满')) {
      errorMessage = '该时段号源已满，请选择其他时段或医生'
    } else if (errorMessage.includes('已过期') || errorMessage.includes('已结束')) {
      errorMessage = '该排班已过期，请选择其他日期'
    } else if (errorMessage.includes('未开始') || errorMessage.includes('未到时间')) {
      errorMessage = '该排班未到预约时间，请选择其他日期'
    }
    
    ElMessage.error({
      message: errorMessage,
      duration: 5000,
      showClose: true
    })
  }
}

// 查看预约详情
const viewAppointment = () => {
  successDialogVisible.value = false
  router.push('/appointment/my')
}

// 禁用过去的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

// 跳转到快速预约
const goToQuickAppointment = () => {
  router.push('/appointment/quick')
}

// 组件挂载时初始化
onMounted(() => {
  fetchDepartments()
  // 设置默认日期为今天
  searchForm.value.date = formatDate(new Date())
})
</script>

<style lang="scss" scoped>
.appointment-list {
  padding: 20px;

  .quick-entrance-card {
    margin-bottom: 20px;
    background: linear-gradient(135deg, #409EFF 0%, #67C23A 100%);
    
    .quick-entrance {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 10px 20px;
      
      .entrance-content {
        display: flex;
        align-items: center;
        gap: 20px;
        color: white;
        
        .entrance-icon {
          font-size: 48px;
          opacity: 0.8;
        }
        
        .entrance-text {
          h3 {
            margin: 0 0 5px 0;
            font-size: 24px;
            font-weight: 500;
          }
          
          p {
            margin: 0;
            opacity: 0.9;
            font-size: 16px;
          }
        }
      }
    }
  }

  .search-card {
    margin-bottom: 20px;
  }

  .doctor-info {
    margin-bottom: 20px;

    .doctor-profile {
      text-align: center;

      .el-avatar {
        margin-bottom: 15px;
      }

      h3 {
        margin: 10px 0;
        font-size: 18px;
      }

      .title,
      .department {
        color: #666;
        margin: 5px 0;
      }

      .specialty,
      .introduction {
        text-align: left;
        margin-top: 20px;

        h4 {
          font-size: 16px;
          color: #333;
          margin-bottom: 10px;
        }

        p {
          color: #666;
          line-height: 1.6;
        }
      }
    }
  }

  .schedule-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>