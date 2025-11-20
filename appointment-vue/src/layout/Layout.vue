<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '200px'" class="aside">
      <div class="logo-container">
        <el-icon :size="32" color="#fff"><School /></el-icon>
        <span v-show="!isCollapse" class="title">校医院预约系统</span>
      </div>
      <SideMenu />
    </el-aside>

    <!-- 主要内容区域 -->
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon 
            class="collapse-btn"
            @click="toggleCollapse"
          >
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <breadcrumb />
        </div>
        <div class="header-right">
          <div class="header-actions">
            <!-- 健康提醒功能已移除 -->
            <!-- <HealthReminder class="reminder-icon" /> -->
            <!-- <el-divider direction="vertical" class="divider" /> -->
          </div>
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="avatar-container">
              <el-avatar :size="32" :src="userAvatar">
                {{ userInfo?.name?.charAt(0) }}
              </el-avatar>
              <span class="username">{{ userInfo?.name }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区域 -->
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { Fold, Expand, School } from '@element-plus/icons-vue'
import SideMenu from '@/components/SideMenu.vue'
import Breadcrumb from '@/components/Breadcrumb.vue'
// 健康提醒功能已移除
// import HealthReminder from '@/components/HealthReminder.vue'

const store = useStore()
const router = useRouter()
const isCollapse = ref(false)

const userInfo = computed(() => store.state.user.userInfo)
const userAvatar = computed(() => userInfo.value?.avatar || '')

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'logout':
      store.dispatch('user/logout')
      router.push('/login')
      break
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
  
  .aside {
    background-color: #304156;
    transition: width 0.3s;
    overflow: hidden;

    .logo-container {
      height: 60px;
      display: flex;
      align-items: center;
      padding: 0 16px;
      background-color: #2b2f3a;

      .logo {
        width: 32px;
        height: 32px;
      }

      .title {
        margin-left: 12px;
        color: #fff;
        font-size: 16px;
        font-weight: 600;
        white-space: nowrap;
      }
    }
  }

  .header {
    background-color: #fff;
    border-bottom: 1px solid #dcdfe6;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 16px;

    .header-left {
      display: flex;
      align-items: center;

      .collapse-btn {
        font-size: 20px;
        cursor: pointer;
        margin-right: 16px;
        
        &:hover {
          color: var(--el-color-primary);
        }
      }
    }

    .header-right {
      display: flex;
      align-items: center;
      
      .header-actions {
        display: flex;
        align-items: center;
        margin-right: 10px;
        
        .reminder-icon {
          margin-right: 5px;
        }
        
        .divider {
          margin: 0 10px;
          height: 20px;
        }
      }

      .avatar-container {
        display: flex;
        align-items: center;
        cursor: pointer;

        .username {
          margin-left: 8px;
          font-size: 14px;
        }
      }
    }
  }

  .main {
    background-color: #f0f2f5;
    padding: 16px;
  }
}

// 路由切换动画
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>