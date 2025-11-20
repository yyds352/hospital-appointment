<template>
  <div class="doctor-schedule">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>我的排班</span>
          <div class="header-controls">
            <el-date-picker
              v-model="selectedMonth"
              type="month"
              placeholder="选择月份"
              @change="handleMonthChange"
            />
            <el-button type="primary" @click="openScheduleDialog" class="add-btn">添加排班</el-button>
          </div>
        </div>
      </template>
      
      <!-- 搜索区域 -->
      <div class="search-area">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="排班日期">
            <el-date-picker
              v-model="searchForm.date"
              type="date"
              placeholder="选择具体日期"
              @change="handleDateSearch"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleDateSearch">搜索</el-button>
            <el-button @click="clearDateSearch">清除</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 表格形式展示排班信息 -->
      <div class="schedule-table-container">
        <el-empty v-if="displaySchedules.length === 0" description="暂无排班信息" />
        
        <el-table
          v-else
          :data="displaySchedules"
          style="width: 100%"
          border
        >
          <el-table-column label="日期" prop="date" min-width="120">
            <template #default="{ row }">
              <div :class="{ 'is-today': isToday(row.date) }">
                {{ formatDateDisplay(row.date) }}
                <el-tag size="small" v-if="isToday(row.date)" type="danger">今天</el-tag>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="星期" min-width="80">
            <template #default="{ row }">
              {{ getDayOfWeek(row.date) }}
            </template>
          </el-table-column>
          
          <el-table-column label="上午" min-width="120">
            <template #default="{ row }">
              <div v-if="row.morning">
                <el-tag type="success">上午</el-tag>
                <span class="slots">可预约: {{ row.morning.availableAppointments }}/{{ row.morning.maxAppointments }}</span>
              </div>
              <el-tag v-else type="info">未排班</el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="下午" min-width="120">
            <template #default="{ row }">
              <div v-if="row.afternoon">
                <el-tag type="warning">下午</el-tag>
                <span class="slots">可预约: {{ row.afternoon.availableAppointments }}/{{ row.afternoon.maxAppointments }}</span>
              </div>
              <el-tag v-else type="info">未排班</el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" min-width="120">
            <template #default="{ row }">
              <!-- 这里可以添加编辑和删除按钮 -->
              <el-button v-if="row.morning || row.afternoon" 
                        type="danger" 
                        size="small" 
                        @click="handleCancelSchedule(row)">
                取消排班
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 添加排班对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="添加排班"
      width="500px"
    >
      <el-form :model="scheduleForm" label-width="100px">
        <el-form-item label="日期">
          <el-date-picker
            v-model="scheduleForm.date"
            type="date"
            placeholder="选择日期"
            :disabled-date="disabledDate"
          />
        </el-form-item>
        <el-form-item label="时间段">
          <el-select v-model="scheduleForm.periods" multiple placeholder="选择时间段">
            <el-option
              v-for="period in periods"
              :key="period.value"
              :label="period.label"
              :value="period.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAddSchedule">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDoctorSchedules, checkTimeSlotAvailability, getDoctorSchedulesByMonth } from '@/api/appointment'
import { getDoctorByUserId } from '@/api/doctor'
import { useStore } from 'vuex'

const store = useStore()
const schedules = ref([])
const dialogVisible = ref(false)
const selectedMonth = ref(new Date())
const searchForm = ref({
  date: null
})

const scheduleForm = ref({
  date: '',
  periods: []
})

const periods = [
  { label: '上午', value: 'MORNING' },
  { label: '下午', value: 'AFTERNOON' }
]

// 根据搜索条件显示的排班数据
const displaySchedules = computed(() => {
  if (searchForm.value.date) {
    // 如果有选择具体日期，则只显示该日期的排班
    const dateStr = formatDateForAPI(searchForm.value.date);
    return groupedSchedules.value.filter(schedule => schedule.date === dateStr);
  }
  
  // 否则显示所有排班
  return groupedSchedules.value;
});

// 按日期分组的排班数据
const groupedSchedules = computed(() => {
  if (!schedules.value || schedules.value.length === 0) return []
  
  const startDate = new Date(selectedMonth.value.getFullYear(), selectedMonth.value.getMonth(), 1)
  const endDate = new Date(selectedMonth.value.getFullYear(), selectedMonth.value.getMonth() + 1, 0)
  
  // 创建当月所有日期的数组
  const daysInMonth = []
  for (let d = new Date(startDate); d <= endDate; d.setDate(d.getDate() + 1)) {
    daysInMonth.push(formatDateForAPI(new Date(d)))
  }
  
  // 创建结果数组
  return daysInMonth.map(date => {
    const daySchedules = getSchedulesByDate(date)
    
    return {
      date: date,
      morning: daySchedules.find(s => s.period === 'MORNING'),
      afternoon: daySchedules.find(s => s.period === 'AFTERNOON')
    }
  })
})

// 日期搜索
const handleDateSearch = () => {
  if (!searchForm.value.date) {
    ElMessage.warning('请选择日期');
    return;
  }
  
  const searchDate = formatDateForAPI(searchForm.value.date);
  console.log('搜索日期:', searchDate);
  
  // 如果搜索的日期不在当前月份内，则更新选中月份
  const currentMonth = selectedMonth.value.getMonth();
  const searchMonth = searchForm.value.date.getMonth();
  
  if (currentMonth !== searchMonth) {
    selectedMonth.value = new Date(searchForm.value.date);
    fetchSchedules();
  }
}

// 清除日期搜索
const clearDateSearch = () => {
  searchForm.value.date = null;
}

// 月份变更处理
const handleMonthChange = () => {
  fetchSchedules()
}

// 获取当前医生的排班
const fetchSchedules = async () => {
  try {
    // 获取当前用户ID
    const userId = store.state.user.userInfo.id
    console.log('当前用户ID:', userId)
    
    // 获取医生信息
    const doctorResponse = await getDoctorByUserId(userId)
    console.log('医生信息响应:', doctorResponse)
    
    let doctorInfo
    if (doctorResponse.data) {
      doctorInfo = doctorResponse.data
    } else {
      doctorInfo = doctorResponse
    }
    console.log('医生信息:', doctorInfo)
    
    // 确保使用正确的医生ID字段
    const doctorId = doctorInfo.id
    console.log('医生ID:', doctorId)
    
    if (!doctorId) {
      throw new Error('无法获取医生ID')
    }
    
    // 使用选中的月份查询排班
    const year = selectedMonth.value.getFullYear()
    const month = selectedMonth.value.getMonth() + 1 // 月份从0开始，所以需要+1
    
    console.log(`查询${year}年${month}月的排班数据，医生ID: ${doctorId}`)
    
    // 创建当月第一天的日期对象，确保是当月而不是上个月或下个月
    const firstDayOfMonth = new Date(year, month - 1, 1) // 月份参数是0-11，所以月份需要-1
    console.log('查询日期 (当月第一天):', firstDayOfMonth.toISOString())
    
    // 使用月份查询接口
    const response = await getDoctorSchedulesByMonth(doctorId, year, month)
    console.log('排班查询响应:', response)
    
    // 处理响应结果
    let scheduleData = []
    if (Array.isArray(response)) {
      scheduleData = response
    } else if (response && response.data && Array.isArray(response.data)) {
      scheduleData = response.data
    } else if (response && response.data && response.data.data && Array.isArray(response.data.data)) {
      scheduleData = response.data.data
    } else if (response && response.data) {
      scheduleData = [response.data]
    }
    
    console.log('处理后的排班数据:', scheduleData)
    schedules.value = scheduleData
    console.log(`获取到${schedules.value.length}条排班记录`)
  } catch (error) {
    console.error('获取排班信息失败:', error)
    ElMessage.error('获取排班信息失败：' + (error.message || '未知错误'))
    schedules.value = []
  }
}

// 取消排班
const handleCancelSchedule = (row) => {
  ElMessageBox.confirm(
    `确定要取消 ${formatDateDisplay(row.date)} 的排班吗？`,
    '取消排班',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      // 这里添加取消排班的API调用
      const scheduleIds = []
      if (row.morning) scheduleIds.push(row.morning.id)
      if (row.afternoon) scheduleIds.push(row.afternoon.id)
      
      // 模拟删除API
      for (const id of scheduleIds) {
        await fetch(`/api/schedule/${id}`, {
          method: 'DELETE',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });
      }
      
      ElMessage.success('取消排班成功')
      fetchSchedules()
    } catch (error) {
      console.error('取消排班失败:', error)
      ElMessage.error('取消排班失败：' + (error.message || '未知错误'))
    }
  }).catch(() => {})
}

// 判断是否是今天
const isToday = (day) => {
  if (!day) return false
  const today = new Date()
  const date = new Date(day)
  return date.toDateString() === today.toDateString()
}

// 获取某天的排班信息
const getSchedulesByDate = (day) => {
  if (!day || !schedules.value || schedules.value.length === 0) return [];
  
  try {
    // 转换day为日期字符串格式 YYYY-MM-DD
    const dateString = typeof day === 'string' ? day.split('T')[0] : '';
    
    return schedules.value.filter(schedule => {
      // 处理schedule.date或schedule.scheduleDate
      const scheduleDate = schedule.scheduleDate || schedule.date;
      if (!scheduleDate) return false;
      
      // 转换排班日期为YYYY-MM-DD格式
      const scheduleDateStr = typeof scheduleDate === 'string' 
        ? scheduleDate.split('T')[0] 
        : (scheduleDate instanceof Date 
            ? `${scheduleDate.getFullYear()}-${String(scheduleDate.getMonth() + 1).padStart(2, '0')}-${String(scheduleDate.getDate()).padStart(2, '0')}`
            : scheduleDate.toString());
      
      return scheduleDateStr === dateString;
    });
  } catch (error) {
    console.error('获取排班出错:', error);
    return [];
  }
}

// 格式化显示日期 (YYYY-MM-DD 格式为 YYYY年MM月DD日)
const formatDateDisplay = (date) => {
  if (!date) return '';
  
  const dateObj = new Date(date);
  return `${dateObj.getFullYear()}年${String(dateObj.getMonth() + 1).padStart(2, '0')}月${String(dateObj.getDate()).padStart(2, '0')}日`;
}

// 获取星期几
const getDayOfWeek = (date) => {
  if (!date) return '';
  
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];
  const dateObj = new Date(date);
  return weekdays[dateObj.getDay()];
}

// 禁用过去的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

// 打开添加排班对话框
const openScheduleDialog = () => {
  scheduleForm.value = {
    date: '',
    periods: []
  }
  dialogVisible.value = true
}

// 添加排班
const handleAddSchedule = async () => {
  if (!scheduleForm.value.date || scheduleForm.value.periods.length === 0) {
    ElMessage.warning('请选择日期和时间段')
    return
  }

  try {
    // 获取当前用户ID
    const userId = store.state.user.userInfo.id
    
    // 获取医生信息
    const doctorResponse = await getDoctorByUserId(userId)
    let doctorInfo
    if (doctorResponse.data) {
      doctorInfo = doctorResponse.data
    } else {
      doctorInfo = doctorResponse
    }
    
    // 获取医生ID
    const doctorId = doctorInfo.id
    
    if (!doctorId) {
      throw new Error('无法获取医生ID')
    }
    
    // 检查每个时间段是否可用
    for (const period of scheduleForm.value.periods) {
      const response = await checkTimeSlotAvailability(
        doctorId,
        scheduleForm.value.date,
        period
      )
      if (response.data && !response.data.available) {
        ElMessage.warning(`${scheduleForm.value.date} ${getPeriodText(period)}已被占用`)
        return
      }
    }

    // 调用添加排班的API
    for (const period of scheduleForm.value.periods) {
      // 模拟添加排班API调用
      await fetch('/api/schedule', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify({
          doctorId: doctorId,
          departmentId: doctorInfo.departmentId,
          scheduleDate: formatDateForAPI(scheduleForm.value.date),
          period: period,
          maxAppointments: 20,
          availableAppointments: 20,
          status: 1
        })
      });
    }
    
    ElMessage.success('添加排班成功')
    dialogVisible.value = false
    fetchSchedules()
  } catch (error) {
    console.error('添加排班失败详情:', error)
    ElMessage.error('添加排班失败：' + error.message)
  }
}

// 将日期格式化为API所需的字符串格式 YYYY-MM-DD
const formatDateForAPI = (date) => {
  if (!date) return '';
  
  if (typeof date === 'string') {
    // 如果已经是YYYY-MM-DD格式
    if (date.match(/^\d{4}-\d{2}-\d{2}$/)) {
      return date;
    }
    // 如果是其他字符串格式，尝试转换
    const dateObj = new Date(date);
    return `${dateObj.getFullYear()}-${String(dateObj.getMonth() + 1).padStart(2, '0')}-${String(dateObj.getDate()).padStart(2, '0')}`;
  }
  
  if (date instanceof Date) {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  }
  
  // 其他情况返回空字符串
  return '';
}

// 获取时间段文本
const getPeriodText = (period) => {
  return period === 'MORNING' ? '上午' : '下午'
}

// 初始化
fetchSchedules()

// 监听月份变化
watch(selectedMonth, () => {
  fetchSchedules()
})
</script>

<style lang="scss" scoped>
.doctor-schedule {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .header-controls {
      display: flex;
      align-items: center;
      
      .add-btn {
        margin-left: 12px;
      }
    }
  }
  
  .search-area {
    margin: 10px 0 20px;
    padding: 15px;
    background-color: #f5f7fa;
    border-radius: 4px;
    
    .search-form {
      display: flex;
      flex-wrap: wrap;
    }
  }

  .schedule-table-container {
    margin-top: 20px;
    
    .is-today {
      color: var(--el-color-primary);
      font-weight: bold;
    }
    
    .slots {
      margin-left: 8px;
      color: #606266;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>