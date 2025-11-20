<template>
  <div v-if="!item.hidden">
    <!-- 如果有子路由并且显示在侧边栏 -->
    <template v-if="hasOneShowingChild(item.children, item) && (!onlyOneChild.children || onlyOneChild.noShowingChildren) && !item.alwaysShow">
      <app-link
        v-if="onlyOneChild.meta"
        :to="resolvePath(onlyOneChild.path)"
      >
        <el-menu-item :index="resolvePath(onlyOneChild.path)" :class="{'submenu-title-noDropdown': !isNest}" :data-path="resolvePath(onlyOneChild.path)">
          <el-icon v-if="onlyOneChild.meta && onlyOneChild.meta.icon">
            <component :is="onlyOneChild.meta.icon"/>
          </el-icon>
          <template #title>{{ onlyOneChild.meta.title }}</template>
        </el-menu-item>
      </app-link>
    </template>

    <!-- 如果有可显示的子路由 -->
    <el-sub-menu v-else ref="subMenu" :index="resolvePath(item.path)" :popper-append-to-body="false">
      <template #title>
        <el-icon v-if="item.meta && item.meta.icon">
          <component :is="item.meta.icon"/>
        </el-icon>
        <span>{{ item.meta.title }}</span>
      </template>
      <sidebar-item
        v-for="child in item.children"
        :key="child.path"
        :is-nest="true"
        :item="child"
        :base-path="resolvePath(child.path)"
        class="nest-menu"
      />
    </el-sub-menu>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { isExternal } from '@/utils/validate'
import AppLink from './Link.vue'
import path from 'path-browserify'

const props = defineProps({
  // 路由对象
  item: {
    type: Object,
    required: true
  },
  // 基础路径
  basePath: {
    type: String,
    default: ''
  },
  // 是否嵌套
  isNest: {
    type: Boolean,
    default: false
  }
})

// 唯一子路由
const onlyOneChild = ref(null)

/**
 * 判断是否有一个显示的子路由
 */
const hasOneShowingChild = (children = [], parent) => {
  const showingChildren = children.filter(item => {
    if (item.hidden) {
      return false
    } else {
      // 递归过滤
      onlyOneChild.value = item
      return true
    }
  })

  // 当只有一个子路由时，默认显示子路由
  if (showingChildren.length === 1) {
    return true
  }

  // 没有子路由时，显示父路由
  if (showingChildren.length === 0) {
    onlyOneChild.value = { ...parent, path: '', noShowingChildren: true }
    return true
  }

  return false
}

/**
 * 解析路径
 */
const resolvePath = (routePath) => {
  if (isExternal(routePath)) {
    return routePath
  }
  if (isExternal(props.basePath)) {
    return props.basePath
  }

  // 特殊处理论坛路径
  if (routePath === 'forum/list' || routePath === '/forum/list') {
    return '/forum'
  }
  
  // 尝试处理完整路径
  if (props.basePath) {
    const fullPath = path.resolve(props.basePath, routePath)
    if (fullPath === '/forum/list') {
      return '/forum'
    }
    return fullPath
  }

  return routePath
}
</script> 
</script>