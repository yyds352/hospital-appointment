<template>
  <div class="post-detail-container">
    <!-- 帖子内容 -->
    <div class="post-content" v-if="post">
      <div class="post-header">
        <h1 class="post-title">{{ post.title }}</h1>
        <div class="post-meta">
          <span class="post-author">{{ post.authorName }}</span>
          <span class="post-time">{{ formatDate(post.createdAt) }}</span>
          <span class="post-views">{{ post.views || 0 }} 次浏览</span>
        </div>
      </div>
      
      <div class="post-body">
        <div class="post-content-text" v-html="post.content"></div>
      </div>
      
      <div class="post-actions">
        <el-button 
          type="primary" 
          @click="likePost" 
          :loading="post.likeLoading"
          :class="{ 'liked': post.liked }">
          <el-icon><StarFilled /></el-icon>
          {{ post.likes || 0 }} 点赞
        </el-button>
        
        <el-button 
          type="warning" 
          @click="toggleFavorite" 
          :loading="post.favoriteLoading"
          :class="{ 'favorited': post.favorited }">
          <el-icon><Collection /></el-icon>
          {{ post.favorited ? '已收藏' : '收藏' }}
        </el-button>
        
        <el-button 
          type="success" 
          @click="scrollToComments">
          <el-icon><ChatDotRound /></el-icon>
          评论
        </el-button>
        
        <el-button 
          v-if="canEditPost"
          type="info" 
          @click="editPost">
          <el-icon><Edit /></el-icon>
          编辑
        </el-button>
        
        <el-button 
          v-if="canDeletePost"
          type="danger" 
          @click="deletePostConfirm">
          <el-icon><Delete /></el-icon>
          删除
        </el-button>
      </div>
    </div>

    <!-- 评论区域 -->
    <div class="comments-section" id="comments-section">
      <h3>💬 评论</h3>

      <!-- 发表评论表单 -->
      <div class="comment-form">
        <el-input
          v-model="commentContent"
          type="textarea"
          :rows="3"
          placeholder="发表你的评论..."
          maxlength="500"
          show-word-limit
        />
        <div style="margin-top: 10px; text-align: right;">
          <el-button type="primary" @click="submitComment" :loading="commentLoading">
            发表评论
          </el-button>
        </div>
      </div>

      <!-- 评论列表 -->
      <div class="comments-list-container">
        <!-- 加载中状态 -->
        <div v-if="commentsLoading" class="loading-container">
          <el-skeleton :rows="3" animated />
        </div>

        <!-- 无评论状态 -->
        <div v-else-if="!comments || comments.length === 0" class="no-comments">
          <el-empty description="暂无评论，快来发表第一条评论吧！" />
        </div>

        <!-- 评论列表 -->
        <div v-else class="comments-list">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="comment-header">
              <div class="comment-author">
                <el-avatar :size="32" :src="comment.authorAvatar || defaultAvatar">
                  {{ comment.authorName ? comment.authorName.charAt(0) : 'U' }}
                </el-avatar>
                <div class="author-info">
                  <span class="author-name">{{ comment.authorName || '匿名用户' }}</span>
                  <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
                </div>
              </div>
              
              <div class="comment-actions">
                <el-button 
                  type="text" 
                  size="small" 
                  @click="likeComment(comment)"
                  :loading="comment.likeLoading"
                  :class="{ 'liked': comment.liked }">
                  <el-icon><StarFilled /></el-icon>
                  {{ comment.likes || 0 }}
                </el-button>
                
                <el-button 
                  v-if="canDeleteComment(comment)"
                  type="text" 
                  size="small" 
                  @click="deleteCommentConfirm(comment)"
                  :loading="comment.deleteLoading">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </div>
            
            <div class="comment-content">
              {{ comment.content }}
            </div>
          </div>
          
          <!-- 加载更多 -->
          <div v-if="hasMoreComments" class="load-more">
            <el-button type="text" @click="loadMoreComments" :loading="commentsLoading">
              加载更多评论
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElSkeleton, ElEmpty, ElAvatar, ElButton, ElInput, ElIcon } from 'element-plus'
import { StarFilled, Delete, Edit, Collection, ChatDotRound } from '@element-plus/icons-vue'
import { getPostDetail, getComments, createComment, likeComment as likeCommentAPI, unlikeComment as unlikeCommentAPI, deleteComment as deleteCommentAPI, likePost as likePostAPI, unlikePost as unlikePostAPI, favoritePost as favoritePostAPI, unfavoritePost as unfavoritePostAPI, deleteForumPost as deletePostAPI } from '@/api/forum'
import { useUserStore } from '@/store/modules/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const post = ref(null)
const comments = ref([])
const commentsLoading = ref(false)
const commentLoading = ref(false)
const commentContent = ref('')
const commentPage = ref(0)
const commentSize = ref(10)
const hasMoreComments = ref(false)
const totalComments = ref(0)
const defaultAvatar = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzIiIGhlaWdodD0iMzIiIHZpZXdCb3g9IjAgMCAzMiAzMiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTE2IDJDMTAuNDc3IDIgNiA2LjQ3NyA2IDEyQzYgMTcuNTIzIDEwLjQ3NyAyMiAxNiAyMkMyMS41MjMgMjIgMjYgMTcuNTIzIDI2IDEyQzI2IDYuNDc3IDIxLjUyMyAyIDE2IDJaIiBmaWxsPSIjOUI5QjlCIi8+CjxwYXRoIGQ9Ik0xNiAyMEMxOS4zMTQgMjAgMjIgMTYuODY2IDIyIDEzQzIyIDkuMTM0IDE5LjMxNCA2IDE2IDZDMTIuNjg2IDYgMTAgOS4xMzQgMTAgMTNDMTAgMTYuODY2IDEyLjY4NiAyMCAxNiAyMFoiIGZpbGw9IiM0QTRBNEEiLz4KPC9zdmc+Cg=='

// 计算属性
const postId = computed(() => route.params.id)
const isAuthenticated = computed(() => userStore.isAuthenticated)
const canEditPost = computed(() => {
  return post.value && userStore.user && post.value.authorId === userStore.user.id
})
const canDeletePost = computed(() => {
  return post.value && userStore.user && (post.value.authorId === userStore.user.id || userStore.user.role === 'ADMIN')
})

// 生命周期
onMounted(async () => {
  console.log('🔍 ForumPostDetail组件挂载')
  console.log('👤 用户认证状态:', userStore.isAuthenticated)
  console.log('👤 用户信息:', userStore.user)
  console.log('🔑 Token存在:', !!localStorage.getItem('token'))
  console.log('👤 UserInfo存在:', !!localStorage.getItem('userInfo'))
  
  await fetchPostDetail()
  await fetchComments()
})

// 方法
const fetchPostDetail = async () => {
  try {
    const response = await getPostDetail(postId.value)
    post.value = response.data || response
    // 初始化点赞和收藏状态
    if (post.value) {
      post.value.likeLoading = false
      post.value.favoriteLoading = false
      post.value.liked = post.value.liked || false
      post.value.favorited = post.value.favorited || false
    }
    console.log('📄 帖子详情获取成功:', post.value)
  } catch (error) {
    console.error('❌ 获取帖子详情失败:', error)
    ElMessage.error('获取帖子详情失败')
  }
}

const fetchComments = async (page = 0, append = false) => {
  if (!append) {
    comments.value = []
    commentsLoading.value = true
  }
  
  try {
    console.log('🔄 开始获取评论, postId:', postId.value, 'page:', page)
    const response = await getComments(postId.value, {
      page,
      size: commentSize.value,
      sort: 'createdAt,desc'
    })
    
    console.log('📋 API响应:', response)
    const fetchedComments = response.content || response.data || []
    const totalElements = response.totalElements || fetchedComments.length
    
    console.log('📊 获取到评论数量:', fetchedComments.length, '总评论数:', totalElements)
    console.log('📝 评论数据:', fetchedComments)
    
    const processedComments = fetchedComments.map(comment => ({
      ...comment,
      likeLoading: false,
      deleteLoading: false
    }))
    
    if (append) {
      comments.value = [...comments.value, ...processedComments]
    } else {
      comments.value = processedComments
    }
    
    totalComments.value = totalElements
    hasMoreComments.value = comments.value.length < totalElements
    
    console.log('✅ 评论处理完成, 当前评论数:', comments.value.length)
    
  } catch (error) {
    console.error('❌ 获取评论失败:', error)
    ElMessage.error('获取评论失败')
    if (!append) {
      comments.value = []
    }
  } finally {
    commentsLoading.value = false
  }
}

const submitComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  
  commentLoading.value = true
  try {
    const response = await createComment(postId.value, {
      content: commentContent.value.trim()
    })
    
    ElMessage.success('评论发表成功')
    commentContent.value = ''
    
    // 添加新评论到列表顶部
    if (response) {
      const newComment = {
        ...response,
        authorName: userStore.user?.name || '访客',
        authorAvatar: userStore.user?.avatar || '/default-avatar.png',
        likes: 0,
        liked: false,
        likeLoading: false,
        deleteLoading: false,
        createdAt: new Date().toISOString()
      }
      comments.value.unshift(newComment)
      
      // 更新评论数
      if (post.value) {
        post.value.commentCount = (post.value.commentCount || 0) + 1
      }
    }
    
    // 重新获取评论列表
    await fetchComments(0, false)
  } catch (error) {
    console.error('❌ 发表评论失败:', error)
    ElMessage.error('发表评论失败')
  } finally {
    commentLoading.value = false
  }
}

const likeComment = async (comment) => {
  comment.likeLoading = true
  try {
    if (comment.liked) {
      await unlikeCommentAPI(comment.id)
      comment.liked = false
      comment.likes = (comment.likes || 1) - 1
    } else {
      await likeCommentAPI(comment.id)
      comment.liked = true
      comment.likes = (comment.likes || 0) + 1
    }
  } catch (error) {
    console.error('❌ 点赞评论失败:', error)
    ElMessage.error('操作失败')
  } finally {
    comment.likeLoading = false
  }
}

const deleteCommentConfirm = (comment) => {
  ElMessageBox.confirm(
    '确定要删除这条评论吗？',
    '删除评论',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    await deleteComment(comment)
  })
}

const deleteComment = async (comment) => {
  comment.deleteLoading = true
  try {
    await deleteCommentAPI(comment.id)
    ElMessage.success('评论删除成功')
    
    // 从列表中移除
    const index = comments.value.findIndex(c => c.id === comment.id)
    if (index > -1) {
      comments.value.splice(index, 1)
    }
    
    // 更新帖子评论数
    if (post.value) {
      post.value.commentCount = Math.max((post.value.commentCount || 1) - 1, 0)
    }
  } catch (error) {
    console.error('❌ 删除评论失败:', error)
    ElMessage.error('删除评论失败')
  } finally {
    comment.deleteLoading = false
  }
}

const canDeleteComment = (comment) => {
  return userStore.user && (comment.authorId === userStore.user.id || userStore.user.role === 'ADMIN')
}

const likePost = async () => {
  if (!post.value) return
  
  post.value.likeLoading = true
  try {
    if (post.value.liked) {
      await unlikePostAPI(postId.value)
      post.value.liked = false
      post.value.likes = (post.value.likes || 1) - 1
    } else {
      await likePostAPI(postId.value)
      post.value.liked = true
      post.value.likes = (post.value.likes || 0) + 1
    }
  } catch (error) {
    console.error('❌ 点赞帖子失败:', error)
    ElMessage.error('操作失败')
  } finally {
    post.value.likeLoading = false
  }
}

const toggleFavorite = async () => {
  if (!post.value) return
  
  // 检查用户是否已登录
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录后再收藏')
    router.push('/login')
    return
  }
  
  post.value.favoriteLoading = true
  try {
    if (post.value.favorited) {
      await unfavoritePostAPI(postId.value)
      post.value.favorited = false
      ElMessage.success('取消收藏成功')
    } else {
      await favoritePostAPI(postId.value)
      post.value.favorited = true
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    console.error('❌ 收藏操作失败:', error)
    ElMessage.error('操作失败')
  } finally {
    post.value.favoriteLoading = false
  }
}

const scrollToComments = () => {
  const commentsSection = document.getElementById('comments-section')
  if (commentsSection) {
    commentsSection.scrollIntoView({ behavior: 'smooth' })
  }
}

const editPost = () => {
  router.push(`/forum/edit/${postId.value}`)
}

const deletePostConfirm = () => {
  ElMessageBox.confirm(
    '确定要删除这篇帖子吗？',
    '删除帖子',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    await deletePost()
  })
}

const deletePost = async () => {
  try {
    await deletePostAPI(postId.value)
    ElMessage.success('帖子删除成功')
    router.push('/forum')
  } catch (error) {
    console.error('❌ 删除帖子失败:', error)
    ElMessage.error('删除帖子失败')
  }
}

const loadMoreComments = async () => {
  commentPage.value++
  await fetchComments(commentPage.value, true)
}

const formatDate = (dateString) => {
  if (!dateString) return '未知时间'
  const date = new Date(dateString)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) {
    return '刚刚'
  } else if (diff < 3600000) {
    return Math.floor(diff / 60000) + '分钟前'
  } else if (diff < 86400000) {
    return Math.floor(diff / 3600000) + '小时前'
  } else if (diff < 604800000) {
    return Math.floor(diff / 86400000) + '天前'
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}
</script>

<style scoped>
.post-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.post-content {
  background: white;
  border-radius: 8px;
  padding: 30px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.post-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.post-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
}

.post-meta {
  display: flex;
  gap: 15px;
  color: #666;
  font-size: 14px;
}

.post-body {
  margin: 20px 0;
  line-height: 1.6;
}

.post-content-text {
  font-size: 16px;
  color: #333;
}

.post-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.comments-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.comments-section h3 {
  margin-bottom: 20px;
  color: #333;
  font-size: 18px;
}

.comment-form {
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 6px;
}

.comments-list-container {
  min-height: 200px;
}

.loading-container {
  text-align: center;
  padding: 40px;
}

.no-comments {
  text-align: center;
  padding: 40px;
}

.comments-list {
  space-y: 15px;
}

.comment-item {
  padding: 15px;
  border-bottom: 1px solid #eee;
  transition: background-color 0.2s;
}

.comment-item:hover {
  background-color: #f8f9fa;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.comment-author {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-info {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-weight: 500;
  color: #333;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-actions {
  display: flex;
  gap: 10px;
}

.comment-content {
  margin-left: 42px;
  color: #333;
  line-height: 1.5;
}

.load-more {
  text-align: center;
  padding: 20px;
}

.liked {
  color: #f56c6c !important;
}

.debug-info {
  font-family: 'Courier New', monospace;
}
</style>