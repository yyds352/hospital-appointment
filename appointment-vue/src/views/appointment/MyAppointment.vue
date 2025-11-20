<template>
  <div class="my-appointment">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>我的预约记录</span>
        </div>
      </template>
      
      <el-table
        v-loading="loading"
        :data="appointments"
        style="width: 100%"
      >
        <el-table-column prop="departmentName" label="科室" />
        <el-table-column prop="doctorName" label="医生" />
        <el-table-column prop="appointmentTime" label="预约时间">
          <template #default="scope">
            {{ formatDateTime(scope.row.appointmentTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 'PENDING'"
              type="danger"
              size="small"
              @click="handleCancel(scope.row)"
            >
              取消预约
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useStore } from 'vuex'
import { getPatientAppointments, cancelAppointment } from '@/api/appointment'

const store = useStore()
const loading = ref(false)
const appointments = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const formatDateTime = (datetime) => {
  if (!datetime) return ''
  const date = new Date(datetime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getStatusType = (status) => {
  const types = {
    'PENDING': 'warning',
    'CONFIRMED': 'success',
    'CANCELLED': 'info',
    'COMPLETED': 'success'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    'PENDING': '待确认',
    'CONFIRMED': '已确认',
    'CANCELLED': '已取消',
    'COMPLETED': '已完成'
  }
  return texts[status] || status
}

const fetchAppointments = async () => {
  loading.value = true
  try {
    const response = await getPatientAppointments({
      page: currentPage.value - 1,
      size: pageSize.value
    })
    
    console.log('预约记录响应:', response);
    
    // 处理不同的数据结构
    if (response && response.content) {
      // 标准分页结构
      appointments.value = response.content;
      total.value = response.totalElements;
    } else if (response && Array.isArray(response)) {
      // 直接返回数组
      appointments.value = response;
      total.value = response.length;
    } else if (response && response.data) {
      // 嵌套结构
      if (response.data.content) {
        appointments.value = response.data.content;
        total.value = response.data.totalElements;
      } else if (Array.isArray(response.data)) {
        appointments.value = response.data;
        total.value = response.data.length;
      } else {
        appointments.value = [];
        total.value = 0;
      }
    } else {
      appointments.value = [];
      total.value = 0;
      ElMessage.warning('没有找到预约记录');
    }
  } catch (error) {
    console.error('获取预约记录失败:', error);
    ElMessage.error('获取预约记录失败：' + (error.message || '未知错误'));
    appointments.value = [];
    total.value = 0;
  } finally {
    loading.value = false
  }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消这个预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await cancelAppointment(row.id)
    ElMessage.success('预约已取消')
    fetchAppointments()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消预约失败：' + error.message)
    }
  }
}

const handleSizeChange = (val) => {
  pageSize.value = val
  fetchAppointments()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchAppointments()
}

onMounted(() => {
  fetchAppointments()
})
</script>

<style lang="scss" scoped>
.my-appointment {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style> 