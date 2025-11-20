<template>
  <el-breadcrumb separator="/">
    <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
    <el-breadcrumb-item 
      v-for="(item, index) in breadcrumbs" 
      :key="item.path"
      :to="index === breadcrumbs.length - 1 ? null : { path: item.path }"
    >
      {{ item.meta?.title || '未命名' }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const breadcrumbs = computed(() => {
  return route.matched
    .filter(item => item.meta && item.meta.title)
    .map(item => ({
      path: item.path,
      meta: item.meta
    }))
})
</script>

<style lang="scss" scoped>
.el-breadcrumb {
  line-height: 1;
}
</style> 