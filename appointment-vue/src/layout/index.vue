<template>
  <div class="app-wrapper">
    <!-- 侧边栏 -->
    <div class="sidebar-container" :class="{ 'is-collapse': isCollapse }">
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        :router="true"
        :collapse="isCollapse"
      >
        <el-menu-item index="/home">
          <el-icon><House /></el-icon>
          <template #title>首页</template>
        </el-menu-item>
        
        <!-- 管理员菜单 -->
        <template v-if="isAdmin">
          <el-menu-item index="/department/list">
            <el-icon><OfficeBuilding /></el-icon>
            <template #title>科室管理</template>
          </el-menu-item>

          <el-menu-item index="/doctors/list">
            <el-icon><User /></el-icon>
            <template #title>医生管理</template>
          </el-menu-item>

          <el-menu-item index="/schedule/management">
            <el-icon><Calendar /></el-icon>
            <template #title>排班管理</template>
          </el-menu-item>
        </template>

        <!-- 预约菜单（所有非管理员用户可见） -->
        <template v-if="!isAdmin">
          <el-menu-item index="/appointment">
            <el-icon><Calendar /></el-icon>
            <template #title>预约挂号</template>
          </el-menu-item>

          <el-menu-item index="/appointment/list">
            <el-icon><Document /></el-icon>
            <template #title>预约记录</template>
          </el-menu-item>
        </template>

        <el-menu-item index="/profile">
          <el-icon><Setting /></el-icon>
          <template #title>个人中心</template>
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 主要内容区 -->
    <div class="main-container" :class="{ 'is-collapse': isCollapse }">
      <!-- 头部导航 -->
      <div class="navbar">
        <div class="left">
          <el-button type="text" @click="toggleSidebar">
            <el-icon><Fold v-if="!isCollapse" /><Expand v-else /></el-icon>
          </el-button>
          <Breadcrumb class="breadcrumb-container" />
        </div>
        <div class="right">
          <el-dropdown @command="handleCommand">
            <span class="user-dropdown">
              {{ userInfo.name }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 内容区 -->
      <div class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import {
  House,
  OfficeBuilding,
  User,
  Setting,
  Fold,
  Expand,
  ArrowDown,
  Calendar,
  Document
} from '@element-plus/icons-vue'
import Breadcrumb from '../components/Breadcrumb.vue'

const router = useRouter()
const route = useRoute()
const store = useStore()

const isCollapse = ref(false)
const userInfo = computed(() => store.state.user.userInfo || {})

// 判断是否是管理员
const isAdmin = computed(() => {
  const role = userInfo.value?.role?.toUpperCase()
  console.log('Checking admin role:', role)
  return role === 'ADMIN'
})

const activeMenu = computed(() => route.path)

const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'logout':
      try {
        await store.dispatch('user/logout')
        router.push('/login')
      } catch (error) {
        console.error('Logout failed:', error)
      }
      break
  }
}
</script>

<style lang="scss" scoped>
.app-wrapper {
  display: flex;
  height: 100vh;
  width: 100%;
}

.sidebar-container {
  background-color: #304156;
  transition: width 0.3s;
  width: 200px;
  height: 100%;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  overflow: hidden;

  &.is-collapse {
    width: 64px;
  }

  .sidebar-menu {
    height: 100%;
    background-color: #304156;
    border-right: none;
  }

  :deep(.el-menu) {
    border-right: none;
  }

  :deep(.el-menu-item) {
    &.is-active {
      background-color: #263445;
    }
  }
}

.main-container {
  flex: 1;
  margin-left: 200px;
  display: flex;
  flex-direction: column;
  min-height: 100%;
  background: #f0f2f5;
  position: relative;
  transition: margin-left 0.3s;

  &.is-collapse {
    margin-left: 64px;
  }
}

.navbar {
  height: 50px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;

  .left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .user-dropdown {
    cursor: pointer;
    display: flex;
    align-items: center;
    font-size: 14px;
    color: #606266;

    .el-icon {
      margin-left: 4px;
    }
  }
}

.app-main {
  padding: 20px;
  flex: 1;
  overflow: auto;
}

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