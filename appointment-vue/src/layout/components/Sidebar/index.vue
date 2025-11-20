<template>
  <div class="sidebar-container">
    <div class="logo">
      <span>{{ isCollapse ? "" : sidebarTitle }}</span>
    </div>
    <el-scrollbar height="calc(100vh - 50px)">
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        :unique-opened="true"
        :background-color="variables.menuBackground"
        :text-color="variables.menuTextColor"
        :active-text-color="variables.menuActiveTextColor"
        mode="vertical"
        @select="handleSelect"
      >
        <sidebar-item
          v-for="route in permissionRoutes"
          :key="route.path"
          :item="route"
          :base-path="route.path"
        />
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import SidebarItem from './SidebarItem.vue'
import variables from '@/styles/variables.scss'

const route = useRoute()
const router = useRouter()
const store = useStore()

const sidebarTitle = ref('校医院预约系统')

// 获取路由列表
const permissionRoutes = computed(() => {
  return store.state.permission.routes
})

// 获取当前激活的菜单
const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) {
    return meta.activeMenu
  }
  return path
})

// 是否折叠
const isCollapse = computed(() => store.state.app.sidebar.isCollapse)

// 处理菜单项点击
const handleSelect = (path) => {
  router.push(path)
}
</script>

<style lang="scss" scoped>
.sidebar-container {
  height: 100vh;
  background-color: v-bind('variables.menuBackground');
  width: v-bind('isCollapse ? "64px" : "210px"');
  transition: width 0.3s;
  overflow: hidden;
  
  .logo {
    height: 50px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 16px;
    font-weight: bold;
    padding: 0 16px;
    overflow: hidden;
    white-space: nowrap;
  }
  
  :deep(.el-scrollbar__wrap) {
    overflow-x: hidden !important;
  }
  
  :deep(.el-menu) {
    border: none;
    width: 100% !important;
  }
  
  :deep(.el-menu--collapse) {
    width: 64px !important;
  }
}
</style> 