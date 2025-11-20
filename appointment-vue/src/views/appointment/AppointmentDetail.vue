<template>
  <div class="appointment-detail-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>预约详情</span>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <el-descriptions v-loading="loading" :column="2" border>
        <el-descriptions-item label="预约编号">{{ appointment.id }}</el-descriptions-item>
        <el-descriptions-item label="预约状态">
          <el-tag :type="getStatusType(appointment.status)">
            {{ getStatusText(appointment.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="科室">{{ appointment.departmentName }}</el-descriptions-item>
        <el-descriptions-item label="医生">{{ appointment.doctorName }}</el-descriptions-item>
        <el-descriptions-item label="预约时间">
          {{ formatDate(appointment.appointmentTime) }}
          {{ appointment.period === 'MORNING' ? '上午' : '下午' }}
        </el-descriptions-item>
        <el-descriptions-item label="预约费用">¥{{ appointment.fee }}</el-descriptions-item>
        <el-descriptions-item label="患者姓名">{{ appointment.patientName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ appointment.patientPhone }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ appointment.patientIdCard }}</el-descriptions-item>
        <el-descriptions-item label="预约时间">{{ formatDate(appointment.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ appointment.remark || '无' }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="appointment.status === 'PENDING'" class="operation-container">
        <el-button type="danger" @click="handleCancel">取消预约</el-button>
      </div>
    </el-card>

    <!-- 取消预约确认对话框 -->
    <el-dialog
      v-model="cancelDialogVisible"
      title="取消预约确认"
      width="400px"
    >
      <p>确定要取消此次预约吗？取消后将无法恢复。</p>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmCancel">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAppointment, cancelAppointment } from '@/api/appointment'
import { formatDate } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const appointment = ref({})
const cancelDialogVisible = ref(false)

// 获取预约详情
const fetchAppointmentDetail = async () => {
  loading.value = true
  try {
    const response = await getAppointment(route.params.id)
    // 由于响应拦截器已经处理了code检查，这里直接处理成功情况
    appointment.value = response
  } catch (error) {
    console.error('获取预约详情失败:', error)
    ElMessage.error('获取预约详情失败')
  } finally {
    loading.value = false
  }
}

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    'PENDING': 'warning',
    'CONFIRMED': 'success',
    'COMPLETED': 'info',
    'CANCELLED': 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待确认',
    'CONFIRMED': '已确认',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 处理取消预约
const handleCancel = () => {
  cancelDialogVisible.value = true
}

// 确认取消预约
const confirmCancel = async () => {
  try {
    const response = await cancelAppointment(appointment.value.id)
    // 由于响应拦截器已经处理了code检查，这里直接处理成功情况
    ElMessage.success('预约已取消')
    cancelDialogVisible.value = false
    await fetchAppointmentDetail()
  } catch (error) {
    console.error('取消预约失败:', error)
    ElMessage.error(error.response?.data?.message || '取消预约失败')
  }
}

onMounted(() => {
  fetchAppointmentDetail()
})
</script>

<style lang="scss" scoped>
.appointment-detail-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .operation-container {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }
}
</style>