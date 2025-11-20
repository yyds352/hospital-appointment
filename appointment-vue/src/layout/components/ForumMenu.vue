<template>
  <el-menu-item-group title="健康论坛">
    <el-menu-item :index="'/forum'" :route="{ path: '/forum' }">
      <el-icon><ChatDotRound /></el-icon>
      <span>论坛首页</span>
    </el-menu-item>
    <template v-if="isAdmin">
      <el-menu-item :index="'/forum/category'" :route="{ path: '/forum/category' }">
        <el-icon><Management /></el-icon>
        <span>分类管理</span>
      </el-menu-item>
      <el-menu-item :index="'/forum/management'" :route="{ path: '/forum/management' }">
        <el-icon><Setting /></el-icon>
        <span>论坛管理</span>
      </el-menu-item>
    </template>
  </el-menu-item-group>
</template>

<script setup>
import { computed } from 'vue'
import { useStore } from 'vuex'
import { ChatDotRound, Management, Setting } from '@element-plus/icons-vue'

const store = useStore()
const isAdmin = computed(() => store.state.user.userInfo?.role === 'ADMIN')
</script> 