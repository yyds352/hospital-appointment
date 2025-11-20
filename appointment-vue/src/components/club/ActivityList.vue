<template>
  <div class="activity-list-component">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="3" animated />
      <el-skeleton :rows="3" animated />
    </div>
    
    <div v-else-if="activities.length === 0" class="empty-container">
      <!-- 空状态由父组件处理 -->
    </div>
    
    <div v-else class="activity-grid">
      <el-card 
        v-for="activity in activities" 
        :key="activity.id" 
        class="activity-card"
        :body-style="{ padding: '0px' }"
      >
        <img 
          :src="activity.imageUrl || defaultImage" 
          class="activity-image"
          @error="handleImageError" 
        />
        
        <div class="activity-content">
          <el-tag :type="getStatusType(activity)" class="status-tag">
            {{ getStatusText(activity) }}
          </el-tag>
          
          <h3 class="activity-title" @click="$emit('view-detail', activity.id)">
            {{ activity.title }}
          </h3>
          
          <div class="activity-info">
            <div class="info-item">
              <el-icon><Calendar /></el-icon>
              <span>{{ formatDateTime(activity.startTime) }}</span>
            </div>
            <div class="info-item">
              <el-icon><Location /></el-icon>
              <span>{{ activity.location }}</span>
            </div>
            <div class="info-item">
              <el-icon><User /></el-icon>
              <span>{{ activity.currentParticipants }}/{{ activity.maxParticipants }} 人</span>
            </div>
          </div>
          
          <div v-if="activity.registrationId" class="registration-info">
            <div v-if="activity.registeredAt" class="registered-date">
              <el-icon><Calendar /></el-icon>
              <span>报名时间: {{ formatDate(activity.registeredAt) }}</span>
            </div>
            
            <div v-if="activity.feedback" class="feedback-display">
              <el-rate v-model="activity.rating" disabled text-color="#ff9900" />
              <el-tooltip :content="activity.feedback" placement="top">
                <el-tag type="success" size="small">已评价</el-tag>
              </el-tooltip>
            </div>
          </div>
          
          <div class="action-buttons">
            <el-button 
              type="primary" 
              size="small" 
              @click="$emit('view-detail', activity.id)"
            >
              查看详情
            </el-button>
            
            <el-button 
              v-if="activity.canCancel" 
              type="danger" 
              size="small" 
              @click="$emit('cancel-registration', activity.registrationId)"
            >
              取消报名
            </el-button>
            
            <el-button 
              v-if="activity.canRate" 
              type="success" 
              size="small" 
              @click="$emit('rate-activity', activity)"
            >
              评价
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { Calendar, Location, User } from '@element-plus/icons-vue';

const props = defineProps({
  activities: {
    type: Array,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['view-detail', 'cancel-registration', 'rate-activity']);

const defaultImage = 'https://via.placeholder.com/300x150?text=活动';

// 处理图片加载错误
const handleImageError = (e) => {
  e.target.src = defaultImage;
};

// 获取活动状态类型
const getStatusType = (activity) => {
  const now = new Date();
  const startTime = new Date(activity.startTime);
  const endTime = new Date(activity.endTime);
  
  if (endTime < now) {
    return 'info';  // 已结束
  } else if (startTime > now) {
    return 'primary';  // 未开始
  } else {
    return 'success';  // 进行中
  }
};

// 获取活动状态文本
const getStatusText = (activity) => {
  const now = new Date();
  const startTime = new Date(activity.startTime);
  const endTime = new Date(activity.endTime);
  
  if (endTime < now) {
    return '已结束';
  } else if (startTime > now) {
    return '未开始';
  } else {
    return '进行中';
  }
};

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return '-';
  
  const date = new Date(dateStr);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  
  return `${year}-${month}-${day} ${hours}:${minutes}`;
};

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-';
  
  const date = new Date(dateStr);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  
  return `${year}-${month}-${day}`;
};
</script>

<style scoped>
.activity-list-component {
  margin-top: 15px;
}

.loading-container, .empty-container {
  padding: 20px 0;
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.activity-card {
  transition: transform 0.3s, box-shadow 0.3s;
  overflow: hidden;
}

.activity-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.activity-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
  display: block;
}

.activity-content {
  padding: 15px;
  position: relative;
}

.status-tag {
  position: absolute;
  top: -12px;
  right: 10px;
  z-index: 1;
}

.activity-title {
  margin: 10px 0;
  font-size: 16px;
  cursor: pointer;
}

.activity-title:hover {
  color: #409EFF;
}

.activity-info {
  margin-bottom: 15px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
  color: #606266;
}

.info-item .el-icon {
  margin-right: 5px;
}

.registration-info {
  margin-bottom: 15px;
  padding-top: 10px;
  border-top: 1px dashed #ebeef5;
}

.registered-date {
  display: flex;
  align-items: center;
  color: #909399;
  font-size: 12px;
  margin-bottom: 5px;
}

.registered-date .el-icon {
  margin-right: 5px;
}

.feedback-display {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 5px;
}

.action-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .activity-grid {
    grid-template-columns: 1fr;
  }
}
</style> 