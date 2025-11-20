<template>
  <div class="home-container">
    <!-- 轮播图部分 -->
    <el-card class="carousel-card" shadow="hover">
      <el-carousel :interval="4000" type="card" height="300px">
        <el-carousel-item v-for="item in carouselItems" :key="item.id">
          <div class="carousel-content" :style="{ backgroundImage: `url(${item.image})` }">
            <div class="carousel-text">
              <h3>{{ item.title }}</h3>
              <p>{{ item.description }}</p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </el-card>

    <!-- 欢迎信息和快速操作 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :xs="24" :sm="24" :md="8">
        <el-card shadow="hover" class="welcome-card">
          <template #header>
            <div class="card-header">
              <span>个人信息</span>
            </div>
          </template>
          <div class="welcome-content">
            <el-avatar :size="64" :src="userAvatar">{{ userInfo?.name?.charAt(0) }}</el-avatar>
            <div class="user-info">
              <h2>{{ welcomeMessage }}</h2>
              <p class="last-login">上次登录：{{ formatDate(userInfo?.lastLoginTime) }}</p>
            </div>
          </div>
          <div class="quick-actions">
            <el-button type="primary" @click="navigateTo('/profile')">个人中心</el-button>
            <el-button v-if="['STUDENT', 'TEACHER'].includes(userRole)" 
                     type="success" 
                     @click="navigateTo('/appointment/list')">
              立即预约
            </el-button>
            <el-button type="primary" size="large" @click="$router.push('/appointment/quick')">
              <el-icon><Calendar /></el-icon>
              快速预约
            </el-button>
            <el-button type="success" size="large" @click="$router.push('/appointment/one-step')">
              <el-icon><Lightning /></el-icon>
              一键预约
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :md="16">
        <el-row :gutter="20">
          <el-col :span="24" style="margin-bottom: 20px">
            <el-card shadow="hover" class="notice-card">
              <template #header>
                <div class="card-header">
                  <span>通知公告</span>
                  <el-link type="primary">更多</el-link>
                </div>
              </template>
              <div class="notice-list">
                <div v-for="notice in notices" :key="notice.id" class="notice-item">
                  <el-icon><Bell /></el-icon>
                  <span class="notice-title">{{ notice.title }}</span>
                  <span class="notice-time">{{ formatDate(notice.time) }}</span>
                </div>
              </div>
        </el-card>
      </el-col>
    </el-row>
      </el-col>
    </el-row>

    <!-- 功能导航区域 -->
    <el-card shadow="hover" class="function-nav-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>功能导航</span>
        </div>
      </template>
      <el-row :gutter="20">
        <!-- 学生和教师可见的功能 -->
        <template v-if="['STUDENT', 'TEACHER'].includes(userRole)">
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-card shadow="hover" class="function-card" @click="navigateTo('/appointment/list')">
              <el-icon><Calendar /></el-icon>
              <span>预约挂号</span>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-card shadow="hover" class="function-card" @click="navigateTo('/appointment/my')">
              <el-icon><List /></el-icon>
              <span>我的预约</span>
            </el-card>
          </el-col>
        </template>

        <!-- 医生可见的功能 -->
        <template v-if="userRole === 'DOCTOR'">
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-card shadow="hover" class="function-card" @click="navigateTo('/doctor/appointments')">
              <el-icon><Calendar /></el-icon>
              <span>预约管理</span>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-card shadow="hover" class="function-card" @click="navigateTo('/doctor/schedule')">
              <el-icon><Calendar /></el-icon>
              <span>我的排班</span>
            </el-card>
          </el-col>
        </template>

        <!-- 管理员可见的功能 -->
        <template v-if="userRole === 'ADMIN'">
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-card shadow="hover" class="function-card" @click="navigateTo('/department/list')">
              <el-icon><OfficeBuilding /></el-icon>
              <span>科室管理</span>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-card shadow="hover" class="function-card" @click="navigateTo('/doctors/list')">
              <el-icon><User /></el-icon>
              <span>医生管理</span>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-card shadow="hover" class="function-card" @click="navigateTo('/schedule/management')">
              <el-icon><Calendar /></el-icon>
              <span>排班管理</span>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-card shadow="hover" class="function-card" @click="navigateTo('/forum/management')">
              <el-icon><Setting /></el-icon>
              <span>论坛管理</span>
            </el-card>
          </el-col>
        </template>

        <!-- 所有用户可见的功能 -->
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-card shadow="hover" class="function-card" @click="router.push('/forum')">
            <el-icon><ChatDotRound /></el-icon>
            <span>健康论坛</span>
          </el-card>
        </el-col>

      </el-row>
    </el-card>

    <!-- 医院简介 -->
    <el-card shadow="hover" class="hospital-intro-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>医院简介</span>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="8">
          <img :src="hospitalImg" alt="医院外观" class="hospital-image" />
        </el-col>
        <el-col :span="16">
          <div class="hospital-text">
            <h3>校医院简介</h3>
            <p>广州华商学院（Guangzhou Huashang College）位于广东省广州市，是经国家教育部批准设立的民办全日制普通本科高校，是广东省硕士学位授予立项建设单位。而校医院是学校为师生提供医疗服务的医疗机构，医院位于学校校本部，为师生提供医疗服务。</p>
            <div class="hospital-features">
              <el-row :gutter="20">
                <el-col :span="8">
                  <div class="feature">
                    <el-icon><User /></el-icon>
                    <div class="feature-text">
                      <h4>50+</h4>
                      <p>专业医师</p>
                    </div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="feature">
                    <el-icon><OfficeBuilding /></el-icon>
                    <div class="feature-text">
                      <h4>10+</h4>
                      <p>专业科室</p>
                    </div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="feature">
                    <el-icon><Star /></el-icon>
                    <div class="feature-text">
                      <h4>98%</h4>
                      <p>满意度</p>
                    </div>
                  </div>
                </el-col>
              </el-row>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import {
  Calendar,
  List,
  OfficeBuilding,
  User,
  ChatDotRound,
  Management,
  Setting,
  Bell,
  Lightning,
  Star
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'

// 导入图片
import appointmentImg from '@/assets/images/预约挂号图片.jpg'
import doctorTeamImg from '@/assets/images/医生团队图片.jpg'
import hospitalMapImg from '@/assets/images/华商地图.jpg'
import hospitalImg from '@/assets/images/华商校医院.jpg'

const store = useStore()
const router = useRouter()

const userInfo = computed(() => store.state.user.userInfo)
const userRole = computed(() => userInfo.value?.role)
const userAvatar = computed(() => userInfo.value?.avatar || '')

const welcomeMessage = computed(() => {
  const name = userInfo.value?.name || '访客'
  const role = {
    'ADMIN': '管理员',
    'DOCTOR': '医生',
    'TEACHER': '教师',
    'STUDENT': '同学'
  }[userRole.value] || '用户'
  
  return `${name} ${role}，欢迎回来！`
})

// 轮播图数据
const carouselItems = ref([
  {
    id: 1,
    title: '预约挂号更便捷',
    description: '在线预约，省时省力',
    image: appointmentImg
  },
  {
    id: 2,
    title: '专业医师团队',
    description: '优质医疗服务保障',
    image: doctorTeamImg
  },
  {
    id: 3,
    title: '学校医院地址',
    description: '让就医更加便捷',
    image: hospitalMapImg
  }
])

// 通知公告数据
const notices = ref([
  {
    id: 1,
    title: '关于调整门诊时间的通知',
    time: '2024-01-15 10:00:00'
  },
  {
    id: 2,
    title: '2024年寒假期间医院门诊安排',
    time: '2024-01-10 14:30:00'
  },
  {
    id: 3,
    title: '新增心理咨询预约服务的通知',
    time: '2024-01-05 09:00:00'
  }
])

const navigateTo = (path) => {
  router.push(path)
}

const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '暂无记录'
}
</script>

<style lang="scss" scoped>
.home-container {
  padding: 20px;

  .carousel-card {
    .carousel-content {
      height: 100%;
      background-size: cover;
      background-position: center;
      display: flex;
      align-items: flex-end;
      
      .carousel-text {
        width: 100%;
        padding: 20px;
        background: linear-gradient(to top, rgba(0,0,0,0.7), transparent);
        color: white;

        h3 {
          margin: 0;
          font-size: 24px;
        }

        p {
          margin: 10px 0 0;
          font-size: 16px;
        }
      }
    }
  }

  .welcome-card {
    height: 100%;

    .welcome-content {
      display: flex;
      align-items: center;
      gap: 20px;
      margin-bottom: 20px;

      .user-info {
        h2 {
          margin: 0;
          color: #409EFF;
        }

        .last-login {
          margin: 5px 0 0;
          color: #909399;
          font-size: 14px;
        }
      }
    }

    .quick-actions {
      display: flex;
      gap: 10px;
    }
  }

  .notice-card {
    height: 100%;

    .notice-list {
      .notice-item {
        display: flex;
        align-items: center;
        padding: 10px 0;
        border-bottom: 1px solid #EBEEF5;

        &:last-child {
          border-bottom: none;
        }

        .el-icon {
          color: #409EFF;
          margin-right: 10px;
        }

        .notice-title {
          flex: 1;
          margin-right: 20px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .notice-time {
          color: #909399;
          font-size: 14px;
        }
      }
    }
  }

  .function-card {
    height: 120px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 10px;
    cursor: pointer;
    margin-bottom: 20px;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    }

    .el-icon {
      font-size: 24px;
      color: #409EFF;
    }

    span {
      font-size: 16px;
      color: #303133;
    }
  }

  .hospital-intro-card {
    .hospital-image {
      width: 100%;
      height: 200px;
      object-fit: cover;
      border-radius: 4px;
    }

    .hospital-text {
      h3 {
        margin: 0 0 15px;
        color: #303133;
      }

      p {
        margin: 0 0 20px;
        color: #606266;
        line-height: 1.6;
      }

      .hospital-features {
        .feature {
          display: flex;
          align-items: center;
          gap: 10px;

          .el-icon {
            font-size: 24px;
            color: #409EFF;
          }

          .feature-text {
            h4 {
              margin: 0;
              color: #409EFF;
              font-size: 20px;
            }

            p {
              margin: 5px 0 0;
              color: #606266;
              font-size: 14px;
            }
          }
        }
      }
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>