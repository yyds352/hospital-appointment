# 论坛管理页面
<template>
  <div class="forum-management-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>论坛管理</span>
          <el-radio-group v-model="activeTab" @change="handleTabChange">
            <el-radio-button label="posts">帖子管理</el-radio-button>
            <el-radio-button label="comments">评论管理</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <!-- 帖子管理 -->
      <div v-if="activeTab === 'posts'">
        <el-table v-loading="loading" :data="posts" style="width: 100%">
          <el-table-column prop="title" label="标题" min-width="200">
            <template #default="{ row }">
              <router-link :to="{ name: 'PostDetail', params: { id: row.id }}" class="post-title">
                {{ row.title }}
              </router-link>
            </template>
          </el-table-column>
          <el-table-column prop="authorName" label="作者" width="120" />
          <el-table-column prop="categoryName" label="分类" width="120" />
          <el-table-column prop="views" label="浏览" width="80" />
          <el-table-column prop="likes" label="点赞" width="80" />
          <el-table-column prop="createdAt" label="发布时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button type="danger" link @click="handleDeletePost(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 评论管理 -->
      <div v-else>
        <el-table v-loading="loading" :data="comments" style="width: 100%">
          <el-table-column prop="content" label="评论内容" min-width="300" />
          <el-table-column prop="authorName" label="评论者" width="120" />
          <el-table-column prop="postTitle" label="所属帖子" width="200" />
          <el-table-column prop="likes" label="点赞" width="80" />
          <el-table-column prop="createdAt" label="评论时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button type="danger" link @click="handleDeleteComment(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPosts, deleteForumPost, getComments, deleteComment } from '@/api/forum'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const activeTab = ref('posts')
const posts = ref([])
const comments = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 获取帖子列表
const fetchPosts = async () => {
  try {
    loading.value = true
    const response = await getPosts({
      page: currentPage.value - 1, // 后端分页从0开始
      size: pageSize.value
    })
    console.log('Posts response:', response)
    posts.value = response.data.content || []
    total.value = response.data.totalElements || 0
  } catch (error) {
    console.error('获取帖子列表失败:', error)
    ElMessage.error(error.message || '获取帖子列表失败')
  } finally {
    loading.value = false
  }
}

// 获取评论列表
const fetchComments = async () => {
  try {
    loading.value = true
    const response = await getComments({
      page: currentPage.value - 1, // 后端分页从0开始
      size: pageSize.value
    })
    console.log('Comments response:', response)
    comments.value = response.data.content || []
    total.value = response.data.totalElements || 0
  } catch (error) {
    console.error('获取评论列表失败:', error)
    ElMessage.error(error.message || '获取评论列表失败')
  } finally {
    loading.value = false
  }
}

// 处理删除帖子
const handleDeletePost = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这篇帖子吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteForumPost(row.id)
    ElMessage.success('删除成功')
    await fetchPosts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除帖子失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 处理删除评论
const handleDeleteComment = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteComment(row.id)
    ElMessage.success('删除成功')
    await fetchComments()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评论失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 处理标签页切换
const handleTabChange = () => {
  currentPage.value = 1
  if (activeTab.value === 'posts') {
    fetchPosts()
  } else {
    fetchComments()
  }
}

// 处理页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  if (activeTab.value === 'posts') {
    fetchPosts()
  } else {
    fetchComments()
  }
}

// 处理每页条数变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  if (activeTab.value === 'posts') {
    fetchPosts()
  } else {
    fetchComments()
  }
}

// 监听标签页变化
watch(activeTab, () => {
  handleTabChange()
})

onMounted(() => {
  fetchPosts()
})
</script>

<style lang="scss" scoped>
.forum-management-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .post-title {
    color: #409EFF;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }
}
</style> 