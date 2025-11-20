<template>
  <el-menu
    :default-active="activeMenu"
    class="side-menu"
    :collapse="isCollapse"
    @select="handleSelect"
  >
    <!-- 学生和教师可见的功能 -->
    <template v-if="['STUDENT', 'TEACHER'].includes(userRole)">
      <el-menu-item index="/appointment/list">
        <el-icon><Calendar /></el-icon>
        <template #title>预约挂号</template>
      </el-menu-item>
      <el-menu-item index="/appointment/my">
        <el-icon><List /></el-icon>
        <template #title>我的预约</template>
      </el-menu-item>
    </template>

    <!-- 医生可见的功能 -->
    <template v-if="userRole === 'DOCTOR'">
      <el-menu-item index="/doctor/appointments">
        <el-icon><Calendar /></el-icon>
        <template #title>预约管理</template>
      </el-menu-item>
      <el-menu-item index="/doctor/schedule">
        <el-icon><Calendar /></el-icon>
        <template #title>我的排班</template>
      </el-menu-item>
    </template>

    <!-- 管理员可见的功能 -->
    <template v-if="userRole === 'ADMIN'">
      <el-sub-menu index="1">
        <template #title>
          <el-icon><Management /></el-icon>
          <span>系统管理</span>
        </template>
        <el-menu-item index="/system/users">
          <el-icon><UserFilled /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
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
      </el-sub-menu>
    </template>

    <!-- 所有用户可见的功能 -->
    <el-sub-menu index="/forum">
      <template #title>
        <el-icon><ChatDotRound /></el-icon>
        <span>健康论坛</span>
      </template>
      <el-menu-item index="/forum">浏览论坛</el-menu-item>
      <el-menu-item index="/forum/my-posts">我的帖子</el-menu-item>
    </el-sub-menu>



    <!-- 管理员的论坛管理功能 -->
    <template v-if="userRole === 'ADMIN'">
      <el-sub-menu index="3">
        <template #title>
          <el-icon><Setting /></el-icon>
          <span>论坛管理</span>
        </template>
        <el-menu-item index="/forum/category">
          <el-icon><Management /></el-icon>
          <template #title>分类管理</template>
        </el-menu-item>
        <el-menu-item index="/forum/management">
          <el-icon><Setting /></el-icon>
          <template #title>内容管理</template>
        </el-menu-item>
      </el-sub-menu>
    </template>

    <!-- 个人中心 -->
    <el-menu-item index="/profile">
      <el-icon><User /></el-icon>
      <template #title>个人中心</template>
    </el-menu-item>
  </el-menu>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter, useRoute } from 'vue-router'
import {
  Calendar,
  List,
  OfficeBuilding,
  User,
  UserFilled,
  ChatDotRound,
  Management,
  Setting,
  Document
} from '@element-plus/icons-vue'

const store = useStore()
const router = useRouter()
const route = useRoute()

const isCollapse = ref(false)
const userRole = computed(() => store.state.user.userInfo?.role)

const activeMenu = computed(() => route.path)

const handleSelect = (index) => {
  router.push(index)
}
</script>

<style lang="scss" scoped>
.side-menu {
  height: 100%;
  border-right: none;

  :deep(.el-menu-item) {
    &.is-active {
      background-color: var(--el-menu-hover-bg-color);
    }
  }

  .el-menu-item, .el-sub-menu__title {
    .el-icon {
      margin-right: 8px;
    }
  }
}
</style>