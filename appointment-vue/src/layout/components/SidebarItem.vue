<template>
  <div v-if="!item.meta?.hidden">
    <template v-if="hasOneShowingChild(item.children, item)">
      <el-menu-item :index="resolvePath(onlyOneChild.path)" :class="{'submenu-title-noDropdown': !isNest}">
        <el-icon v-if="onlyOneChild.meta?.icon">
          <component :is="resolveIcon(onlyOneChild.meta.icon)" />
        </el-icon>
        <template #title>
          <span>{{ onlyOneChild.meta?.title }}</span>
        </template>
      </el-menu-item>
    </template>

    <el-sub-menu v-else :index="resolvePath(item.path)">
      <template #title>
        <el-icon v-if="item.meta?.icon">
          <component :is="resolveIcon(item.meta.icon)" />
        </el-icon>
        <span>{{ item.meta?.title }}</span>
      </template>
      
      <template v-for="child in item.children" :key="child.path">
        <template v-if="!child.meta?.hidden && hasPermission(child)">
          <sidebar-item
            v-if="child.children && child.children.length > 0"
            :is-nest="true"
            :item="child"
            :base-path="resolvePath(child.path)"
            class="nest-menu"
          />
          <el-menu-item v-else :index="resolvePath(child.path)">
            <el-icon v-if="child.meta?.icon">
              <component :is="resolveIcon(child.meta.icon)" />
            </el-icon>
            <template #title>{{ child.meta?.title }}</template>
          </el-menu-item>
        </template>
      </template>
    </el-sub-menu>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import path from 'path-browserify'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const props = defineProps({
  item: {
    type: Object,
    required: true
  },
  basePath: {
    type: String,
    default: ''
  },
  isNest: {
    type: Boolean,
    default: false
  }
})

const store = useStore()
const userRole = computed(() => store.state.user.userInfo?.role)

const onlyOneChild = ref(null)

const hasPermission = (route) => {
  if (!route.meta?.roles) {
    return true
  }
  return route.meta.roles.includes(userRole.value)
}

const resolveIcon = (iconName) => {
  return ElementPlusIconsVue[iconName]
}

const hasOneShowingChild = (children = [], parent) => {
  if (!children) {
    children = []
  }
  const showingChildren = children.filter(item => {
    if (item.meta?.hidden) {
      return false
    }
    // If you have no permission, don't show it
    if (!hasPermission(item)) {
      return false
    }
    return true
  })

  // When there is only one child router, the child router is displayed by default
  if (showingChildren.length === 1) {
    onlyOneChild.value = showingChildren[0]
    return true
  }

  // Show parent if there are no child router to display
  if (showingChildren.length === 0) {
    onlyOneChild.value = { ...parent, path: '', noShowingChildren: true }
    return true
  }

  return false
}

const resolvePath = (routePath) => {
  if (path.isAbsolute(routePath)) {
    return routePath
  }
  return path.resolve(props.basePath, routePath)
}
</script>

<style lang="scss" scoped>
.nest-menu .el-menu-item {
  min-width: 160px;
}
</style> 