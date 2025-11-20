<template>
  <div class="sidebar-container">
    <el-menu
      :default-active="activeMenu"
      :collapse="isCollapse"
      :unique-opened="false"
      :collapse-transition="false"
      mode="vertical"
      router
    >
      <template v-for="route in permissionRoutes" :key="route.path">
        <template v-if="!route.meta?.hidden && hasPermission(route)">
          <sidebar-item :item="route" :base-path="route.path" />
        </template>
      </template>
      <!-- 健康论坛 -->
      <el-sub-menu index="/forum">
        <template #title>
          <el-icon><ChatDotRound /></el-icon>
          <span>健康论坛</span>
        </template>
        <el-menu-item index="/forum">论坛首页</el-menu-item>
        <el-menu-item index="/forum/postlist">旧版列表</el-menu-item>
        <el-menu-item index="/forum/my-posts">我的帖子</el-menu-item>
        <el-menu-item index="/forum/favorites">我的收藏</el-menu-item>
        <el-menu-item v-if="userInfo.role === 'ADMIN'" index="/forum/management">论坛管理</el-menu-item>
      </el-sub-menu>
    </el-menu>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useStore } from 'vuex'
import SidebarItem from './SidebarItem.vue'
import { 
  HomeFilled, 
  Calendar, 
  User, 
  Setting, 
  List, 
  Clock,
  ChatDotRound
} from '@element-plus/icons-vue'

const route = useRoute()
const store = useStore()

const userInfo = computed(() => store.state.user.userInfo || {})

const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) {
    return meta.activeMenu
  }
  return path
})

const isCollapse = computed(() => store.state.app.sidebar.isCollapse)
const permissionRoutes = computed(() => {
  const routes = store.state.permission.routes || []
  return routes.filter(route => route.children)
})

const userRole = computed(() => store.state.user.userInfo?.role)

const hasPermission = (route) => {
  if (!route.meta?.roles) {
    return true
  }
  return route.meta.roles.includes(userRole.value)
}
</script>

<style lang="scss" scoped>
.sidebar-container {
  height: 100%;
  background-color: var(--el-menu-bg-color);
  
  .el-menu {
    border: none;
    height: 100%;
    width: 100% !important;
  }
}
</style> 