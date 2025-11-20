<template>
  <div class="schedule-management-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>排班管理</span>
          <el-button type="primary" @click="handleAdd">新增排班</el-button>
        </div>
      </template>

      <el-form :model="queryForm" inline class="filter-form">
        <el-form-item label="科室">
          <el-select
            v-model="queryForm.departmentId"
            placeholder="请选择科室"
            @change="handleDepartmentChange"
          >
            <el-option
              v-for="dept in departmentOptions"
              :key="dept.value"
              :label="dept.label"
              :value="dept.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="医生">
          <el-select
            v-model="queryForm.doctorId"
            placeholder="请选择医生"
            :disabled="!queryForm.departmentId"
            @change="handleDoctorChange"
          >
            <el-option
              v-for="doctor in doctorOptions"
              :key="doctor.value"
              :label="doctor.label"
              :value="doctor.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="queryForm.date"
            type="date"
            placeholder="选择日期"
            :disabled="!queryForm.doctorId"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="scheduleList" style="width: 100%">
        <el-table-column prop="departmentName" label="科室" width="120" />
        <el-table-column prop="doctorName" label="医生" width="120" />
        <el-table-column prop="scheduleDate" label="日期" width="120">
          <template #default="{ row }">
            {{ formatDisplayDate(row.scheduleDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="period" label="时段" width="120">
          <template #default="{ row }">
            {{ row.period === 'MORNING' ? '上午' : '下午' }}
          </template>
        </el-table-column>
        <el-table-column prop="maxAppointments" label="最大预约数" width="120" />
        <el-table-column prop="availableAppointments" label="剩余名额" width="120" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status ? 'success' : 'danger'">
              {{ row.status ? '正常' : '停诊' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑排班对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增排班' : '编辑排班'"
      width="500px"
    >
      <el-form
        ref="scheduleFormRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="科室" prop="departmentId">
          <el-select
            v-model="form.departmentId"
            placeholder="请选择科室"
            @change="handleDepartmentChange"
          >
            <el-option
              v-for="dept in departmentOptions"
              :key="dept.value"
              :label="dept.label"
              :value="dept.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="医生" prop="doctorId">
          <el-select
            v-model="form.doctorId"
            placeholder="请选择医生"
            :disabled="!form.departmentId"
            @change="handleDoctorChange"
          >
            <el-option
              v-for="doctor in doctorOptions"
              :key="doctor.value"
              :label="doctor.label"
              :value="doctor.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期" prop="scheduleDate">
          <el-date-picker
            v-model="form.scheduleDate"
            type="date"
            placeholder="选择日期"
            :disabled="!form.doctorId"
            :options="datePickerOptions"
          />
        </el-form-item>
        <el-form-item label="时段" prop="period">
          <el-select v-model="form.period" placeholder="请选择时段">
            <el-option label="上午" value="MORNING" />
            <el-option label="下午" value="AFTERNOON" />
          </el-select>
        </el-form-item>
        <el-form-item label="最大预约数" prop="maxAppointments">
          <el-input-number v-model="form.maxAppointments" :min="1" :max="50" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDepartmentList } from '@/api/department'
import { getDoctorList, getDoctorsByDepartment } from '@/api/doctor'
import { 
  getScheduleList, 
  createSchedule, 
  updateSchedule, 
  deleteSchedule,
  getDoctorSchedules,
  getDepartmentSchedules,
  getRecentSchedules
} from '@/api/schedule'
import { formatDate } from '@/utils/format'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogType = ref('add')
const scheduleFormRef = ref(null)
const scheduleList = ref([])
const departmentOptions = ref([])
const doctorOptions = ref([])

const queryForm = ref({
  departmentId: '',
  doctorId: '',
  date: ''
})

const form = ref({
  id: '',
  departmentId: '',
  departmentName: '',
  doctorId: '',
  doctorName: '',
  scheduleDate: '',
  period: 'MORNING',
  maxAppointments: 20,
  availableAppointments: 20,
  status: 1
})

const rules = {
  departmentId: [
    { required: true, message: '请选择科室', trigger: 'change' }
  ],
  doctorId: [
    { required: true, message: '请选择医生', trigger: 'change' }
  ],
  scheduleDate: [
    { required: true, message: '请选择日期', trigger: 'change' }
  ],
  period: [
    { required: true, message: '请选择时段', trigger: 'change' }
  ],
  maxAppointments: [
    { required: true, message: '请输入最大预约数', trigger: 'blur' },
    { type: 'number', min: 1, max: 50, message: '预约数必须在1-50之间', trigger: 'blur' }
  ]
}

// 日期选择器配置
const datePickerOptions = {
  disabledDate(time) {
    return time.getTime() < Date.now() - 8.64e7 // 禁用过去的日期
  }
}

// 获取科室列表
const fetchDepartments = async () => {
  try {
    const response = await getDepartmentList()
    console.log('科室列表响应:', response)
    // 响应直接就是数据本身，不需要检查code
    if (response) {
      if (response.content) {
        // 分页数据
        departmentOptions.value = response.content.map(dept => ({
          value: dept.id,
          label: dept.name
        }))
      } else if (Array.isArray(response)) {
        // 数组数据
        departmentOptions.value = response.map(dept => ({
          value: dept.id,
          label: dept.name
        }))
      }
    }
  } catch (error) {
    console.error('获取科室列表失败:', error)
    ElMessage.error('获取科室列表失败')
  }
}

// 获取医生列表
const fetchDoctors = async (departmentId) => {
  try {
    // 使用getDoctorsByDepartment以按科室过滤医生
    const response = await getDoctorsByDepartment(departmentId)
    console.log('科室医生列表响应:', response)
    // 响应直接就是数据本身，不需要检查code
    if (response) {
      // 检查 response 是否包含 content 属性（分页数据）
      const doctors = response.content || response
      doctorOptions.value = doctors.map(doctor => ({
        value: doctor.id,
        label: doctor.name,
        departmentId: doctor.departmentId || departmentId, // 确保有departmentId
        departmentName: doctor.departmentName || '' // 防止未定义
      }))
    }
  } catch (error) {
    console.error('获取医生列表失败:', error)
    ElMessage.error('获取医生列表失败')
  }
}

// 获取排班列表
const fetchSchedules = async () => {
  if (!queryForm.value.doctorId || !queryForm.value.date) {
    return
  }

  loading.value = true
  try {
    const formattedDate = formatDate(queryForm.value.date)
    console.log('查询排班，医生ID:', queryForm.value.doctorId, '日期:', formattedDate)
    const response = await getDoctorSchedules(queryForm.value.doctorId, {
      date: formattedDate
    })
    
    console.log('排班响应:', response)
    // 响应直接就是数据本身，不需要检查code
    if (response) {
      scheduleList.value = response
    } else {
      scheduleList.value = []
      ElMessage.warning('未找到排班数据')
    }
  } catch (error) {
    console.error('获取排班列表失败:', error)
    ElMessage.error('获取排班列表失败')
    scheduleList.value = []
  } finally {
    loading.value = false
  }
}

// 查询按钮点击事件
const handleSearch = async () => {
  if (!queryForm.value.doctorId) {
    ElMessage.warning('请选择医生')
    return
  }
  if (!queryForm.value.date) {
    ElMessage.warning('请选择日期')
    return
  }
  await fetchSchedules()
}

// 获取最近的排班数据，不需要选择科室和医生
const fetchRecentSchedules = async () => {
  loading.value = true
  try {
    const response = await getRecentSchedules(10)
    console.log('最近排班响应:', response)
    // 响应直接就是数据本身，不需要检查code
    if (response) {
      scheduleList.value = response
    } else {
      scheduleList.value = []
    }
  } catch (error) {
    console.error('获取最近排班数据失败:', error)
    scheduleList.value = []
  } finally {
    loading.value = false
  }
}

// 处理科室变更
const handleDepartmentChange = async (departmentId) => {
  console.log('科室变更:', departmentId);
  
  // 清空医生选择
  if (dialogVisible.value) {
    form.value.doctorId = '';
    // 如果在弹窗中，设置当前选择的科室名称
    const selectedDept = departmentOptions.value.find(dept => dept.value === departmentId);
    if (selectedDept) {
      form.value.departmentName = selectedDept.label;
    }
  } else {
    queryForm.value.doctorId = '';
  }
  
  // 只有当选择了有效科室时才获取医生列表
  if (departmentId) {
    await fetchDoctors(departmentId);
  } else {
    // 如果没有选择科室，清空医生选项
    doctorOptions.value = [];
  }
};

// 处理医生变更
const handleDoctorChange = (doctorId) => {
  if (!doctorId) return;
  
  // 找到选中的医生，设置医生名称
  const selectedDoctor = doctorOptions.value.find(doc => doc.value === doctorId);
  if (selectedDoctor && dialogVisible.value) {
    form.value.doctorName = selectedDoctor.label;
  }
};

// 初始化表单数据
const initForm = () => {
  form.value = {
    id: '',
    departmentId: '',
    departmentName: '',
    doctorId: '',
    doctorName: '',
    scheduleDate: '',
    period: 'MORNING',
    maxAppointments: 20,
    availableAppointments: 20,
    status: 1
  }
}

// 处理新增
const handleAdd = () => {
  dialogType.value = 'add'
  dialogVisible.value = true
  initForm()
}

// 处理编辑
const handleEdit = (row) => {
  dialogType.value = 'edit'
  dialogVisible.value = true
  form.value = {
    ...row,
    departmentId: row.departmentId || queryForm.value.departmentId,
    doctorId: row.doctorId || queryForm.value.doctorId,
    status: row.status === true ? 1 : (row.status === false ? 0 : (row.status || 1))
  }
}

// 处理删除
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该排班吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteSchedule(row.id)
      ElMessage.success('删除成功')
      fetchSchedules()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 处理提交
const handleSubmit = async () => {
  if (!scheduleFormRef.value) return
  
  await scheduleFormRef.value.validate(async (valid) => {
    if (valid) {
      // 验证医生ID和科室ID是否有效
      if (!form.value.doctorId || !form.value.departmentId) {
        ElMessage.error('请选择有效的医生和科室')
        return
      }
      
      // 查找选中的医生，确保医生属于所选科室
      const selectedDoctor = doctorOptions.value.find(doc => doc.value === form.value.doctorId)
      if (!selectedDoctor) {
        ElMessage.error('所选医生不存在')
        return
      }
      
      if (selectedDoctor.departmentId !== form.value.departmentId) {
        ElMessage.warning('所选医生不属于当前科室，请重新选择')
        return
      }
      
      try {
        const scheduleData = {
          ...form.value,
          scheduleDate: formatDate(form.value.scheduleDate),
          availableAppointments: form.value.maxAppointments,
          departmentId: form.value.departmentId,
          // 将布尔值状态转换为整数：true -> 1, false -> 0
          status: form.value.status === true ? 1 : (form.value.status === false ? 0 : (form.value.status || 1)),
          // 添加更多信息以确保数据完整
          doctorName: selectedDoctor.label,
          departmentName: departmentOptions.value.find(dept => dept.value === form.value.departmentId)?.label || ''
        }
        
        console.log('提交排班数据:', scheduleData)
        
        if (dialogType.value === 'add') {
          await createSchedule(scheduleData)
          ElMessage.success('创建排班成功')
        } else {
          await updateSchedule(form.value.id, scheduleData)
          ElMessage.success('更新排班成功')
        }
        
        dialogVisible.value = false
        
        // 更新查询表单，确保能看到新添加/修改的排班
        if (form.value.doctorId) {
          queryForm.value.doctorId = form.value.doctorId
        }
        if (form.value.departmentId) {
          queryForm.value.departmentId = form.value.departmentId
        }
        if (form.value.scheduleDate) {
          queryForm.value.date = new Date(form.value.scheduleDate)
        }
        
        // 刷新排班列表
        await fetchSchedules()
      } catch (error) {
        console.error('提交排班失败:', error)
        if (error.message && error.message.includes('foreign key constraint fails')) {
          ElMessage.error('提交失败: 所选医生不存在或不可用，请选择其他医生')
        } else {
          ElMessage.error('提交排班失败: ' + (error.message || error))
        }
      }
    }
  })
}

// 在模板中使用的日期格式化函数
const formatDisplayDate = (dateString) => {
  if (!dateString) return ''
  // 如果已经是格式化的字符串，直接返回
  if (typeof dateString === 'string' && dateString.includes('-')) return dateString
  // 否则创建日期对象并格式化
  const date = new Date(dateString)
  return formatDate(date)
}

onMounted(() => {
  fetchDepartments()
  // 初始加载时获取最近的排班数据
  fetchRecentSchedules()
})
</script>

<style lang="scss" scoped>
.schedule-management-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .filter-form {
    margin-bottom: 20px;
  }
}
</style>