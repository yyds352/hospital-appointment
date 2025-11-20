<template>
  <div class="one-step-appointment">
    <el-card class="appointment-card">
      <template #header>
        <div class="card-header">
          <span>一键预约</span>
          <el-tag type="success">智能匹配，一步完成</el-tag>
        </div>
      </template>

      <div class="appointment-form">
        <!-- 症状描述 -->
        <div class="form-section">
          <h4>📝 症状描述</h4>
          <el-input
            v-model="symptomDescription"
            type="textarea"
            placeholder="请详细描述您的症状，例如：最近几天一直咳嗽，有痰，感觉有点发烧..."
            :rows="4"
            class="symptoms-input"
          />
          <div class="input-tips">
            <el-icon><InfoFilled /></el-icon>
            <span>描述越详细，推荐越准确</span>
          </div>
        </div>

        <!-- 智能推荐结果 -->
        <div v-if="recommendations.length > 0" class="recommendation-section">
          <h4>🤖 智能推荐</h4>
          <div class="recommendation-cards">
            <el-card 
              v-for="(rec, index) in recommendations" 
              :key="rec.departmentId"
              class="recommendation-card"
              :class="{ 'selected': selectedRecommendation?.departmentId === rec.departmentId }"
              @click="selectRecommendation(rec)"
              shadow="hover"
            >
              <div class="recommendation-header">
                <div class="department-info">
                  <h5>{{ rec.departmentName }}</h5>
                  <p>{{ rec.departmentDescription }}</p>
                </div>
                <div class="match-score">
                  <span class="score-label">匹配度</span>
                  <span class="score-value">{{ rec.matchScore }}%</span>
                  <div class="score-bar">
                    <div 
                      class="score-fill" 
                      :style="{ width: rec.matchScore + '%' }"
                    ></div>
                  </div>
                </div>
              </div>
              <div class="matched-keywords">
                <span class="matched-label">匹配症状：</span>
                <el-tag 
                  v-for="keyword in rec.matchedKeywords" 
                  :key="keyword"
                  size="small"
                  type="info"
                  class="keyword-tag"
                >
                  {{ keyword }}
                </el-tag>
              </div>
              <div class="rank-badge" v-if="index === 0">
                <el-tag type="warning" effect="dark">🥇 最推荐</el-tag>
              </div>
            </el-card>
          </div>
        </div>

        <!-- 预约时间选择 -->
        <div v-if="selectedRecommendation" class="time-selection-section">
          <h4>📅 预约时间</h4>
          <div class="time-form">
            <div class="date-picker-group">
              <label>选择日期：</label>
              <el-date-picker
                v-model="appointmentDate"
                type="date"
                placeholder="选择预约日期"
                :disabled-date="disabledDate"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </div>
            
            <div class="period-selector">
              <label>选择时段：</label>
              <el-radio-group v-model="appointmentPeriod">
                <el-radio label="MORNING">上午 (9:00-12:00)</el-radio>
                <el-radio label="AFTERNOON">下午 (14:00-17:00)</el-radio>
              </el-radio-group>
            </div>

            <!-- 可用时间段 -->
            <div v-if="appointmentDate && appointmentPeriod" class="available-slots">
              <label>可用时间：</label>
              <div class="time-slots">
                <el-tag
                  v-for="slot in availableTimeSlots"
                  :key="slot.time"
                  :type="getTimeSlotType(slot)"
                  class="time-slot"
                  @click="selectTimeSlot(slot)"
                  :effect="selectedTimeSlot === slot.time ? 'dark' : 'light'"
                >
                  {{ slot.time }}
                  <br>
                  <small>{{ getTimeSlotCongestionText(slot) }}</small>
                </el-tag>
              </div>
            </div>
          </div>
        </div>

        <!-- 患者信息确认 -->
        <div class="patient-info-section">
          <h4>👤 患者信息</h4>
          <div class="patient-info">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="姓名">{{ patientInfo.name }}</el-descriptions-item>
              <el-descriptions-item label="性别">{{ patientInfo.gender }}</el-descriptions-item>
              <el-descriptions-item label="年龄">{{ patientInfo.age }}岁</el-descriptions-item>
              <el-descriptions-item label="手机号">{{ patientInfo.phone }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </div>

        <!-- 提交按钮 -->
        <div class="submit-section">
          <el-button 
            type="primary" 
            @click="submitAppointment"
            :loading="submitting"
            :disabled="!canSubmit"
            size="large"
            class="submit-btn"
          >
            <el-icon><Check /></el-icon>
            一键预约
          </el-button>
          <p class="submit-tips">点击后将为您快速完成预约</p>
        </div>
      </div>
    </el-card>

    <!-- 预约成功对话框 -->
    <el-dialog
      v-model="successDialogVisible"
      title="预约成功"
      width="500px"
      center
    >
      <div class="success-content">
        <el-icon class="success-icon" color="#67C23A" :size="48"><CircleCheck /></el-icon>
        <h3>一键预约成功！</h3>
        <p>AI已为您智能匹配最适合的科室和医生</p>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="预约号">{{ appointmentResult.appointmentNumber }}</el-descriptions-item>
          <el-descriptions-item label="科室">{{ appointmentResult.departmentName }}</el-descriptions-item>
          <el-descriptions-item label="医生">{{ appointmentResult.doctorName }} {{ appointmentResult.doctorTitle }}</el-descriptions-item>
          <el-descriptions-item label="预约时间">{{ appointmentResult.appointmentDate }} {{ appointmentResult.formattedAppointmentTime }}</el-descriptions-item>
          <el-descriptions-item label="就诊地点">{{ appointmentResult.location }}</el-descriptions-item>
          <el-descriptions-item label="AI推荐理由">{{ appointmentResult.aiRecommendationReason }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="goToMyAppointments">查看我的预约</el-button>
        <el-button type="primary" @click="continueAppointment">继续预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { 
  analyzeSymptoms,
  getPatientInfo,
  createOneStepAppointment,
  getTimeSlotAvailability 
} from '@/api/appointment'
import { CircleCheck, Check, InfoFilled } from '@element-plus/icons-vue'

const router = useRouter()

// 表单数据
const symptomDescription = ref('')
const recommendations = ref([])
const selectedRecommendation = ref(null)
const appointmentDate = ref('')
const appointmentPeriod = ref('')
const selectedTimeSlot = ref('')
const submitting = ref(false)
const successDialogVisible = ref(false)
const appointmentResult = ref({})

// 患者信息
const patientInfo = ref({
  name: '张三',
  gender: '男',
  age: 28,
  phone: '138****8888'
})

// 可用时间段
const availableTimeSlots = ref([])

// 是否可以提交
const canSubmit = computed(() => {
  return selectedRecommendation.value && 
         appointmentDate.value && 
         appointmentPeriod.value && 
         selectedTimeSlot.value &&
         symptomDescription.value.trim()
})

// 监听症状变化，自动进行AI分析
let analysisTimeout = null
const handleSymptomChange = () => {
  clearTimeout(analysisTimeout)
  if (symptomDescription.value.trim().length > 5) {
    analysisTimeout = setTimeout(() => {
      analyzeSymptomsAI()
    }, 1000) // 延迟1秒分析，避免频繁请求
  }
}

// AI症状分析
const analyzeSymptomsAI = async () => {
  if (!symptomDescription.value.trim()) return
  
  try {
    const response = await analyzeSymptoms({
      symptoms: symptomDescription.value
    })
    
    // 由于响应拦截器已经处理了code检查，这里直接处理成功情况
    recommendations.value = response.recommendations || []
    if (recommendations.value.length > 0 && !selectedRecommendation.value) {
      selectedRecommendation.value = recommendations.value[0] // 默认选择第一个推荐
    }
  } catch (error) {
    console.error('症状分析失败:', error)
  }
}

// 选择推荐科室
const selectRecommendation = (recommendation) => {
  selectedRecommendation.value = recommendation
}

// 选择时间段
const selectTimeSlot = (slot) => {
  if (slot.available > 0) {
    selectedTimeSlot.value = slot.time
  }
}

// 获取时间段类型
const getTimeSlotType = (slot) => {
  if (slot.available <= 0) return 'danger'
  if (slot.available <= 2) return 'warning'
  return 'success'
}

// 获取时间段拥挤度文本
const getTimeSlotCongestionText = (slot) => {
  if (slot.available <= 0) return '已满'
  if (slot.available <= 2) return '紧张'
  return `剩余${slot.available}个`
}

// 禁用日期
const disabledDate = (time) => {
  const date = new Date(time)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date < today || date.getDay() === 0 || date.getDay() === 6
}

// 获取可用时间段
const fetchAvailableTimeSlots = async () => {
  if (!appointmentDate.value || !appointmentPeriod.value || !selectedRecommendation.value) return
  
  try {
    const response = await getTimeSlotAvailability({
      departmentId: selectedRecommendation.value.departmentId,
      date: appointmentDate.value,
      period: appointmentPeriod.value
    })
    
    // 由于响应拦截器已经处理了code检查，这里直接处理成功情况
    availableTimeSlots.value = response || []
  } catch (error) {
    console.error('获取时间段失败:', error)
  }
}

// 提交预约
const submitAppointment = async () => {
  if (!canSubmit.value) return
  
  submitting.value = true
  try {
    const appointmentDateTime = new Date(appointmentDate.value)
    const [hours, minutes] = selectedTimeSlot.value.split(':')
    appointmentDateTime.setHours(parseInt(hours), parseInt(minutes), 0, 0)

    const response = await createOneStepAppointment({
      departmentId: selectedRecommendation.value.departmentId,
      symptoms: symptomDescription.value,
      appointmentTime: appointmentDateTime.toISOString(),
      aiRecommendation: {
        departmentId: selectedRecommendation.value.departmentId,
        matchScore: selectedRecommendation.value.matchScore,
        matchedKeywords: selectedRecommendation.value.matchedKeywords
      }
    })

    // 由于响应拦截器已经处理了code检查，这里直接处理成功情况
    appointmentResult.value = response
    successDialogVisible.value = true
    ElMessage.success('一键预约成功！AI已为您匹配最适合的科室和医生')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '预约失败')
  } finally {
    submitting.value = false
  }
}

// 查看我的预约
const goToMyAppointments = () => {
  successDialogVisible.value = false
  router.push('/appointment/my')
}

// 继续预约
const continueAppointment = () => {
  successDialogVisible.value = false
  // 重置表单
  symptomDescription.value = ''
  recommendations.value = []
  selectedRecommendation.value = null
  appointmentDate.value = ''
  appointmentPeriod.value = ''
  selectedTimeSlot.value = ''
}

// 监听时间选择变化
watch([appointmentDate, appointmentPeriod], () => {
  fetchAvailableTimeSlots()
})

// 监听症状描述变化
watch(symptomDescription, () => {
  handleSymptomChange()
})

onMounted(() => {
  // 获取患者信息
  getPatientInfo().then(response => {
    // 由于响应拦截器已经处理了code检查，这里直接处理成功情况
    patientInfo.value = response
  })
})

/**
 * 格式化日期时间
 */
function formatDateTime(dateTime) {
  const dt = new Date(dateTime)
  const MM = String(dt.getMonth() + 1).padStart(2, '0')
  const dd = String(dt.getDate()).padStart(2, '0')
  const HH = String(dt.getHours()).padStart(2, '0')
  const mm = String(dt.getMinutes()).padStart(2, '0')
  return `${MM}月${dd}日 ${HH}:${mm}`
}
</script>

<style lang="scss" scoped>
.one-step-appointment {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.appointment-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-section {
  margin-bottom: 30px;
  
  h4 {
    color: #2c3e50;
    margin-bottom: 16px;
    font-size: 18px;
  }
}

.symptoms-input {
  width: 100%;
  font-size: 14px;
}

.input-tips {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.recommendation-section {
  margin-bottom: 30px;
  
  h4 {
    color: #2c3e50;
    margin-bottom: 16px;
    font-size: 18px;
  }
}

.recommendation-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
}

.recommendation-card {
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
  
  &.selected {
    border-color: #409eff;
    background: linear-gradient(135deg, #f0f7ff 0%, #ffffff 100%);
  }
}

.recommendation-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.department-info {
  flex: 1;
  
  h5 {
    color: #2c3e50;
    font-size: 16px;
    margin-bottom: 4px;
  }
  
  p {
    color: #7f8c8d;
    font-size: 14px;
    margin: 0;
  }
}

.match-score {
  text-align: center;
  min-width: 80px;
}

.score-label {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.score-value {
  display: block;
  font-size: 20px;
  font-weight: bold;
  color: #27ae60;
  margin-bottom: 4px;
}

.score-bar {
  width: 60px;
  height: 4px;
  background: #e4e7ed;
  border-radius: 2px;
  overflow: hidden;
  margin: 0 auto;
}

.score-fill {
  height: 100%;
  background: linear-gradient(90deg, #e74c3c 0%, #f39c12 50%, #27ae60 100%);
  transition: width 0.3s;
}

.matched-keywords {
  margin-top: 12px;
  
  .matched-label {
    font-size: 12px;
    color: #909399;
    margin-right: 8px;
  }
}

.keyword-tag {
  margin-right: 6px;
  margin-bottom: 4px;
}

.rank-badge {
  position: absolute;
  top: 12px;
  right: 12px;
}

.time-selection-section {
  margin-bottom: 30px;
  
  h4 {
    color: #2c3e50;
    margin-bottom: 16px;
    font-size: 18px;
  }
}

.time-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.date-picker-group, .period-selector {
  display: flex;
  align-items: center;
  gap: 12px;
  
  label {
    font-weight: 500;
    color: #2c3e50;
    min-width: 80px;
  }
}

.available-slots {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  
  label {
    font-weight: 500;
    color: #2c3e50;
    min-width: 80px;
    margin-top: 8px;
  }
}

.time-slots {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.time-slot {
  cursor: pointer;
  text-align: center;
  min-width: 80px;
  
  small {
    display: block;
    font-size: 10px;
    margin-top: 2px;
  }
}

.patient-info-section {
  margin-bottom: 30px;
  
  h4 {
    color: #2c3e50;
    margin-bottom: 16px;
    font-size: 18px;
  }
}

.submit-section {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.submit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  font-size: 16px;
  font-weight: 500;
  padding: 12px 32px;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  }
  
  &:disabled {
    opacity: 0.6;
    transform: none;
    box-shadow: none;
  }
}

.submit-tips {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.success-content {
  text-align: center;
  
  .success-icon {
    margin-bottom: 16px;
  }
  
  h3 {
    color: #67c23a;
    margin-bottom: 8px;
  }
  
  p {
    color: #909399;
    margin-bottom: 20px;
  }
}

@media (max-width: 768px) {
  .recommendation-cards {
    grid-template-columns: 1fr;
  }
  
  .recommendation-header {
    flex-direction: column;
    gap: 12px;
  }
  
  .time-form {
    gap: 16px;
  }
  
  .date-picker-group, .period-selector, .available-slots {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>