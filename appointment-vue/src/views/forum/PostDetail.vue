<template>
  <div class="post-detail-container">
    <div class="post-header">
      <h1>{{ post.title }}</h1>
      <div class="post-meta">
        <span><el-icon><User /></el-icon> {{ post.authorName }}</span>
        <span><el-icon><Timer /></el-icon> {{ formatDate(post.createdAt) }}</span>
        <span><el-icon><View /></el-icon> {{ post.views }} 浏览</span>
      </div>
    </div>
    
    <div class="post-content">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="10" animated />
      </div>
      
      <div v-else>
        <div class="post-body" v-html="post.content"></div>
        
        <div class="post-tags">
          <el-tag v-for="tag in post.tags" :key="tag.id" size="small" type="info" class="post-tag">
            {{ tag.name }}
          </el-tag>
        </div>
        
        <div class="post-actions">
          <el-button 
            type="primary" 
            :icon="post.liked ? 'Thumb' : 'Thumb'" 
            size="small"
            :class="{ 'is-liked': post.liked }"
            @click="likePost"
          >
            {{ post.liked ? '已点赞' : '点赞' }} ({{ post.likes }})
          </el-button>
          
          <el-button 
            type="primary" 
            :icon="post.favorited ? 'Star' : 'Star'" 
            size="small"
            :class="{ 'is-favorited': post.favorited }"
            @click="toggleFavorite"
          >
            {{ post.favorited ? '已收藏' : '收藏' }} ({{ post.favoriteCount }})
          </el-button>
          
          <el-button 
            type="primary" 
            icon="ChatDotRound" 
            size="small"
            @click="scrollToComments"
          >
            评论 ({{ post.commentCount }})
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 评论区 -->
    <div class="comments-section" ref="commentsSection">
      <h2>评论 ({{ post.commentCount || 0 }})</h2>
      
      <div v-if="commentsLoading" class="loading-container">
        <el-skeleton :rows="3" animated />
      </div>
      
      <div v-else>
        <div v-if="comments.length === 0" class="no-comments">
          暂无评论，发表第一条评论吧！
        </div>
        
        <div v-else class="comments-list">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="comment-header">
              <div class="comment-user">
                <el-avatar :size="40" icon="User"></el-avatar>
                <div class="comment-user-info">
                  <div class="comment-username">{{ comment.authorName }}</div>
                  <div class="comment-time">{{ formatDate(comment.createdAt) }}</div>
                </div>
              </div>
            </div>
            <div class="comment-content">{{ comment.content }}</div>
          </div>
        </div>
        
        <div class="pagination-container" v-if="comments.length > 0">
          <el-pagination
            layout="prev, pager, next"
            :total="commentTotal"
            :page-size="commentPageSize"
            :current-page="commentCurrentPage"
            @current-change="handleCommentPageChange"
          />
        </div>
      </div>
      
      <!-- 评论表单 -->
      <div class="comment-form">
        <h3>发表评论</h3>
        <el-form :model="commentForm" :rules="commentRules" ref="commentFormRef">
          <el-form-item prop="content">
            <el-input
              type="textarea"
              v-model="commentForm.content"
              :rows="4"
              placeholder="请输入您的评论"
            ></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitComment" :loading="submittingComment">
              发表评论
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Timer, View, Star, ChatDotRound, Thumb } from '@element-plus/icons-vue'
import { formatDate } from '@/utils/format'

export default {
  name: 'PostDetail',
  components: {
    User,
    Timer,
    View,
    Star,
    ChatDotRound,
    Thumb
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const commentsSection = ref(null)
    const commentFormRef = ref(null)
    
    // 帖子数据
    const loading = ref(true)
    const post = ref({
      id: null,
      title: '',
      content: '',
      authorId: null,
      authorName: '',
      categoryId: null,
      categoryName: '',
      tags: [],
      views: 0,
      likes: 0,
      favoriteCount: 0,
      commentCount: 0,
      createdAt: null,
      updatedAt: null,
      liked: false,
      favorited: false
    })
    
    // 评论数据
    const commentsLoading = ref(false)
    const comments = ref([])
    const commentTotal = ref(0)
    const commentPageSize = 10
    const commentCurrentPage = ref(1)
    
    // 评论表单
    const commentForm = reactive({
      content: ''
    })
    const commentRules = {
      content: [
        { required: true, message: '请输入评论内容', trigger: 'blur' },
        { min: 3, max: 500, message: '评论长度应在3到500个字符之间', trigger: 'blur' }
      ]
    }
    const submittingComment = ref(false)
    
    // 加载帖子详情
    const loadPostDetail = async () => {
      loading.value = true
      try {
        const postId = route.params.id
        const response = await fetch(`/api/forum/posts/${postId}`, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': localStorage.getItem('token')
          }
        })
        
        if (!response.ok) {
          throw new Error('获取帖子详情失败')
        }
        
        const data = await response.json()
        post.value = data
      } catch (error) {
        console.error('加载帖子详情失败:', error)
        ElMessage.error('加载帖子详情失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
    
    // 加载评论
    const loadComments = async () => {
      commentsLoading.value = true
      try {
        const postId = route.params.id
        const response = await fetch(`/api/forum/posts/${postId}/comments?page=${commentCurrentPage.value - 1}&size=${commentPageSize}`, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': localStorage.getItem('token')
          }
        })
        
        if (!response.ok) {
          throw new Error('获取评论失败')
        }
        
        const data = await response.json()
        comments.value = data.content || []
        commentTotal.value = data.totalElements || 0
      } catch (error) {
        console.error('加载评论失败:', error)
        ElMessage.error('加载评论失败，请稍后重试')
      } finally {
        commentsLoading.value = false
      }
    }
    
    // 评论分页
    const handleCommentPageChange = (page) => {
      commentCurrentPage.value = page
      loadComments()
    }
    
    // 滚动到评论区
    const scrollToComments = async () => {
      await nextTick()
      commentsSection.value.scrollIntoView({ behavior: 'smooth' })
    }
    
    // 提交评论
    const submitComment = async () => {
      if (!commentFormRef.value) return
      
      commentFormRef.value.validate(async (valid) => {
        if (!valid) return
        
        submittingComment.value = true
        try {
          const postId = route.params.id
          const response = await fetch(`/api/forum/posts/${postId}/comments`, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
              'Authorization': localStorage.getItem('token')
            },
            body: JSON.stringify({
              content: commentForm.content
            })
          })
          
          if (!response.ok) {
            throw new Error('发表评论失败')
          }
          
          ElMessage.success('评论发表成功')
          commentForm.content = ''
          
          // 重新加载评论和帖子详情（以更新评论数）
          await loadPostDetail()
          commentCurrentPage.value = 1
          await loadComments()
          
          // 自动滚动到评论区
          scrollToComments()
        } catch (error) {
          console.error('发表评论失败:', error)
          ElMessage.error('发表评论失败，请稍后重试')
        } finally {
          submittingComment.value = false
        }
      })
    }
    
    // 点赞帖子
    const likePost = async () => {
      try {
        const postId = route.params.id
        const url = `/api/forum/posts/${postId}/like`
        const method = post.value.liked ? 'DELETE' : 'POST'
        
        const response = await fetch(url, {
          method,
          headers: {
            'Content-Type': 'application/json',
            'Authorization': localStorage.getItem('token')
          }
        })
        
        if (!response.ok) {
          throw new Error(post.value.liked ? '取消点赞失败' : '点赞失败')
        }
        
        // 更新状态
        post.value.liked = !post.value.liked
        post.value.likes += post.value.liked ? 1 : -1
        
        ElMessage.success(post.value.liked ? '点赞成功' : '已取消点赞')
      } catch (error) {
        console.error('点赞操作失败:', error)
        ElMessage.error('操作失败，请稍后重试')
      }
    }
    
    // 收藏帖子
    const toggleFavorite = async () => {
      try {
        const postId = route.params.id
        const url = `/api/forum/posts/${postId}/${post.value.favorited ? 'unfavorite' : 'favorite'}`
        const method = post.value.favorited ? 'DELETE' : 'POST'
        
        const response = await fetch(url, {
          method,
          headers: {
            'Content-Type': 'application/json',
            'Authorization': localStorage.getItem('token')
          }
        })
        
        if (!response.ok) {
          throw new Error(post.value.favorited ? '取消收藏失败' : '收藏失败')
        }
        
        // 更新状态
        post.value.favorited = !post.value.favorited
        post.value.favoriteCount += post.value.favorited ? 1 : -1
        
        ElMessage.success(post.value.favorited ? '收藏成功' : '已取消收藏')
      } catch (error) {
        console.error('收藏操作失败:', error)
        ElMessage.error('操作失败，请稍后重试')
      }
    }
    
    onMounted(() => {
      loadPostDetail()
      loadComments()
    })
    
    return {
      loading,
      post,
      commentsLoading,
      comments,
      commentTotal,
      commentPageSize,
      commentCurrentPage,
      commentForm,
      commentRules,
      submittingComment,
      commentsSection,
      commentFormRef,
      handleCommentPageChange,
      scrollToComments,
      submitComment,
      likePost,
      toggleFavorite,
      formatDate
    }
  }
}
</script>

<style scoped>
.post-detail-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.post-header {
  margin-bottom: 20px;
}

.post-header h1 {
  margin-bottom: 10px;
  font-size: 24px;
}

.post-meta {
  display: flex;
  align-items: center;
  color: #606266;
  font-size: 14px;
}

.post-meta span {
  display: flex;
  align-items: center;
  margin-right: 20px;
}

.post-meta .el-icon {
  margin-right: 5px;
}

.post-content {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.post-body {
  margin-bottom: 20px;
  line-height: 1.6;
}

.post-tags {
  margin-bottom: 20px;
}

.post-tag {
  margin-right: 10px;
  margin-bottom: 5px;
}

.post-actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

.is-liked,
.is-favorited {
  background-color: #67c23a;
  border-color: #67c23a;
}

.comments-section {
  margin-top: 30px;
}

.comments-section h2 {
  font-size: 20px;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.no-comments {
  color: #909399;
  text-align: center;
  padding: 20px 0;
}

.comment-item {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.comment-header {
  margin-bottom: 10px;
}

.comment-user {
  display: flex;
}

.comment-user-info {
  margin-left: 10px;
}

.comment-username {
  font-weight: bold;
}

.comment-time {
  font-size: 12px;
  color: #909399;
}

.comment-content {
  line-height: 1.6;
  word-break: break-word;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

.comment-form {
  margin-top: 30px;
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.comment-form h3 {
  font-size: 18px;
  margin-bottom: 20px;
}

.loading-container {
  padding: 20px;
}
</style> 