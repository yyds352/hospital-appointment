<template>
  <div class="forum-detail-container" v-loading="loading">
    <div class="back-btn">
      <el-button icon="el-icon-arrow-left" @click="goBack">返回论坛</el-button>
    </div>
    
    <!-- 帖子详情 -->
    <el-card v-if="post" class="post-detail-card">
      <div class="post-header">
        <h1 class="post-title">{{ post.title }}</h1>
        <div class="post-meta">
          <div class="post-author">
            <el-avatar :size="40" :src="post.avatar || defaultAvatar">{{ getInitials(post.authorName) }}</el-avatar>
            <div class="author-info">
              <div class="author-name">{{ post.authorName }}</div>
              <div class="post-time">{{ formatDate(post.createTime) }}</div>
            </div>
          </div>
          <div class="post-tags" v-if="post.tags && post.tags.length">
            <el-tag v-for="tag in post.tags" :key="tag" size="small" class="post-tag">{{ tag }}</el-tag>
          </div>
        </div>
      </div>
      
      <div class="post-content">{{ post.content }}</div>
      
      <div class="post-footer">
        <div class="post-actions">
          <span class="action-item" @click="handleLikePost">
            <el-icon :class="{ 'liked': post.isLiked }"><Star /></el-icon>
            <span>点赞 ({{ post.likeCount || 0 }})</span>
          </span>
          <span class="action-item" @click="scrollToComments">
            <el-icon><ChatLineRound /></el-icon>
            <span>评论 ({{ post.commentCount || 0 }})</span>
          </span>
        </div>
        
        <div class="post-stats">
          <span class="views-count">
            <el-icon><View /></el-icon>
            <span>{{ post.views || 0 }} 次浏览</span>
          </span>
        </div>
      </div>
    </el-card>
    
    <!-- 评论区 -->
    <el-card class="comments-card">
      <template #header>
        <div class="comments-header">
          <span>评论 ({{ comments.length }})</span>
        </div>
      </template>
      
      <!-- 发表评论 -->
      <div class="comment-form">
        <el-input
          v-model="commentForm.content"
          type="textarea"
          :rows="3"
          placeholder="写下你的评论..."
          maxlength="500"
          show-word-limit
        />
        <div class="comment-form-actions">
          <el-button type="primary" @click="submitComment" :disabled="!commentForm.content.trim()">发表评论</el-button>
        </div>
      </div>
      
      <!-- 评论列表 -->
      <div class="comments-list" ref="commentsRef">
        <el-empty description="暂无评论" v-if="comments.length === 0" />
        
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <div class="comment-user">
            <el-avatar :size="36" :src="comment.avatar || defaultAvatar">{{ getInitials(comment.authorName) }}</el-avatar>
            <div class="comment-user-info">
              <div class="comment-user-name">{{ comment.authorName }}</div>
              <div class="comment-time">{{ formatDate(comment.createTime) }}</div>
            </div>
          </div>
          
          <div class="comment-content">{{ comment.content }}</div>
          
          <div class="comment-actions">
            <span class="comment-action" @click="handleLikeComment(comment)">
              <el-icon :class="{ 'liked': comment.isLiked }"><ThumbUp /></el-icon>
              <span>{{ comment.isLiked ? '已赞' : '点赞' }} ({{ comment.likeCount || 0 }})</span>
            </span>
            <span class="comment-action" @click="handleReplyComment(comment)">
              <el-icon><ChatDotRound /></el-icon>
              <span>回复</span>
            </span>
            <span class="comment-action" v-if="canDeleteComment(comment)" @click="handleDeleteComment(comment)">
              <el-icon><Delete /></el-icon>
              <span>删除</span>
            </span>
          </div>
          
          <!-- 回复框 -->
          <div class="reply-form" v-if="comment.showReplyForm">
            <el-input
              v-model="replyForm.content"
              type="textarea"
              :rows="2"
              placeholder="回复评论..."
              maxlength="300"
              show-word-limit
            />
            <div class="reply-form-actions">
              <el-button size="small" @click="cancelReply(comment)">取消</el-button>
              <el-button size="small" type="primary" @click="submitReply(comment)" :disabled="!replyForm.content.trim()">回复</el-button>
            </div>
          </div>
          
          <!-- 评论的回复列表 -->
          <div class="replies-list" v-if="comment.replies && comment.replies.length > 0">
            <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
              <div class="reply-user">
                <el-avatar :size="32" :src="reply.avatar || defaultAvatar">{{ getInitials(reply.authorName) }}</el-avatar>
                <div class="reply-user-info">
                  <div class="reply-user-name">{{ reply.authorName }}</div>
                  <div class="reply-time">{{ formatDate(reply.createTime) }}</div>
                </div>
              </div>
              
              <div class="reply-content">{{ reply.content }}</div>
              
              <div class="reply-actions">
                <span class="reply-action" @click="handleLikeReply(reply)">
                  <el-icon :class="{ 'liked': reply.isLiked }"><ThumbUp /></el-icon>
                  <span>{{ reply.likeCount || 0 }}</span>
                </span>
                <span class="reply-action" v-if="canDeleteComment(reply)" @click="handleDeleteReply(comment, reply)">
                  <el-icon><Delete /></el-icon>
                  <span>删除</span>
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, ChatLineRound, View, ThumbUp, ChatDotRound, Delete } from '@element-plus/icons-vue'
import { getPostDetail, createComment, deleteComment, likePost, unlikePost, likeComment, unlikeComment } from '@/api/forum'

const route = useRoute()
const router = useRouter()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 帖子详情
const loading = ref(false)
const post = ref(null)
const comments = ref([])
const commentsRef = ref(null)

// 评论表单
const commentForm = reactive({
  content: ''
})

// 回复表单
const replyForm = reactive({
  content: '',
  parentId: null
})

// 获取帖子详情
const getPostData = async () => {
  const postId = route.params.id
  if (!postId) {
    ElMessage.error('帖子ID不存在')
    router.push({ name: 'ForumList' })
    return
  }
  
  try {
    loading.value = true
    const { data } = await getPostDetail(postId)
    post.value = data
    comments.value = data.comments || []
    
    // 扩展评论数据
    comments.value.forEach(comment => {
      comment.showReplyForm = false
      if (!comment.replies) {
        comment.replies = []
      }
    })
  } catch (error) {
    console.error('获取帖子详情失败:', error)
    ElMessage.error('获取帖子详情失败: ' + (error.message || '未知错误'))
    router.push({ name: 'ForumList' })
  } finally {
    loading.value = false
  }
}

// 返回论坛
const goBack = () => {
  router.push({ name: 'ForumList' })
}

// 滚动到评论区
const scrollToComments = () => {
  nextTick(() => {
    if (commentsRef.value) {
      commentsRef.value.scrollIntoView({ behavior: 'smooth' })
    }
  })
}

// 提交评论
const submitComment = async () => {
  if (!commentForm.content.trim()) {
    ElMessage.warning('评论内容不能为空')
    return
  }
  
  try {
    const { data } = await createComment({
      postId: post.value.id,
      content: commentForm.content
    })
    
    // 添加评论到列表
    comments.value.unshift({
      ...data,
      authorName: data.authorName || '我',
      showReplyForm: false,
      replies: []
    })
    
    // 增加评论计数
    if (post.value) {
      post.value.commentCount = (post.value.commentCount || 0) + 1
    }
    
    // 清空表单
    commentForm.content = ''
    
    ElMessage.success('评论发表成功')
  } catch (error) {
    console.error('评论发表失败:', error)
    ElMessage.error('评论发表失败: ' + (error.message || '未知错误'))
  }
}

// 处理评论点赞
const handleLikeComment = async (comment) => {
  try {
    if (comment.isLiked) {
      await unlikeComment(comment.id)
      comment.isLiked = false
      comment.likeCount = Math.max((comment.likeCount || 1) - 1, 0)
    } else {
      await likeComment(comment.id)
      comment.isLiked = true
      comment.likeCount = (comment.likeCount || 0) + 1
    }
  } catch (error) {
    console.error('评论点赞操作失败:', error)
    ElMessage.error('操作失败: ' + (error.message || '未知错误'))
  }
}

// 处理回复点赞
const handleLikeReply = async (reply) => {
  try {
    if (reply.isLiked) {
      await unlikeComment(reply.id)
      reply.isLiked = false
      reply.likeCount = Math.max((reply.likeCount || 1) - 1, 0)
    } else {
      await likeComment(reply.id)
      reply.isLiked = true
      reply.likeCount = (reply.likeCount || 0) + 1
    }
  } catch (error) {
    console.error('回复点赞操作失败:', error)
    ElMessage.error('操作失败: ' + (error.message || '未知错误'))
  }
}

// 处理帖子点赞
const handleLikePost = async () => {
  if (!post.value) return
  
  try {
    if (post.value.isLiked) {
      await unlikePost(post.value.id)
      post.value.isLiked = false
      post.value.likeCount = Math.max((post.value.likeCount || 1) - 1, 0)
    } else {
      await likePost(post.value.id)
      post.value.isLiked = true
      post.value.likeCount = (post.value.likeCount || 0) + 1
    }
  } catch (error) {
    console.error('帖子点赞操作失败:', error)
    ElMessage.error('操作失败: ' + (error.message || '未知错误'))
  }
}

// 显示回复表单
const handleReplyComment = (comment) => {
  // 关闭所有其他回复表单
  comments.value.forEach(item => {
    item.showReplyForm = false
  })
  
  // 打开当前评论的回复表单
  comment.showReplyForm = true
  replyForm.content = ''
  replyForm.parentId = comment.id
}

// 取消回复
const cancelReply = (comment) => {
  comment.showReplyForm = false
  replyForm.content = ''
  replyForm.parentId = null
}

// 提交回复
const submitReply = async (comment) => {
  if (!replyForm.content.trim()) {
    ElMessage.warning('回复内容不能为空')
    return
  }
  
  try {
    const { data } = await createComment({
      postId: post.value.id,
      parentId: comment.id,
      content: replyForm.content
    })
    
    // 添加回复
    if (!comment.replies) {
      comment.replies = []
    }
    
    comment.replies.push({
      ...data,
      authorName: data.authorName || '我'
    })
    
    // 增加帖子的评论计数
    if (post.value) {
      post.value.commentCount = (post.value.commentCount || 0) + 1
    }
    
    // 清空回复表单并隐藏
    replyForm.content = ''
    comment.showReplyForm = false
    
    ElMessage.success('回复成功')
  } catch (error) {
    console.error('回复失败:', error)
    ElMessage.error('回复失败: ' + (error.message || '未知错误'))
  }
}

// 删除评论
const handleDeleteComment = (comment) => {
  ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteComment(comment.id)
      
      // 从评论列表中移除
      const index = comments.value.findIndex(item => item.id === comment.id)
      if (index !== -1) {
        comments.value.splice(index, 1)
      }
      
      // 减少帖子的评论计数
      if (post.value) {
        const replyCount = comment.replies ? comment.replies.length : 0
        post.value.commentCount = Math.max((post.value.commentCount || 0) - 1 - replyCount, 0)
      }
      
      ElMessage.success('评论已删除')
    } catch (error) {
      console.error('删除评论失败:', error)
      ElMessage.error('删除评论失败: ' + (error.message || '未知错误'))
    }
  }).catch(() => {})
}

// 删除回复
const handleDeleteReply = (comment, reply) => {
  ElMessageBox.confirm('确定要删除这条回复吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteComment(reply.id)
      
      // 从回复列表中移除
      if (comment.replies) {
        const index = comment.replies.findIndex(item => item.id === reply.id)
        if (index !== -1) {
          comment.replies.splice(index, 1)
        }
      }
      
      // 减少帖子的评论计数
      if (post.value) {
        post.value.commentCount = Math.max((post.value.commentCount || 0) - 1, 0)
      }
      
      ElMessage.success('回复已删除')
    } catch (error) {
      console.error('删除回复失败:', error)
      ElMessage.error('删除回复失败: ' + (error.message || '未知错误'))
    }
  }).catch(() => {})
}

// 判断是否可以删除评论或回复
const canDeleteComment = (item) => {
  // TODO: 实际项目中应该根据用户ID或权限判断
  // 这里简单处理，假设自己的评论可以删除
  return item.isAuthor === true || item.authorId === getCurrentUserId()
}

// 获取当前用户ID
const getCurrentUserId = () => {
  // TODO: 实际项目中应该从用户状态或token中获取
  return null
}

// 获取姓名首字母作为头像默认内容
const getInitials = (name) => {
  if (!name) return '用户'
  return name.substring(0, 1)
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  
  const d = new Date(date)
  const now = new Date()
  const diff = now - d
  
  // 小于1分钟显示"刚刚"
  if (diff < 60000) {
    return '刚刚'
  }
  
  // 小于1小时显示"X分钟前"
  if (diff < 3600000) {
    return Math.floor(diff / 60000) + '分钟前'
  }
  
  // 小于24小时显示"X小时前"
  if (diff < 86400000) {
    return Math.floor(diff / 3600000) + '小时前'
  }
  
  // 小于30天显示"X天前"
  if (diff < 2592000000) {
    return Math.floor(diff / 86400000) + '天前'
  }
  
  // 否则显示具体日期
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

onMounted(() => {
  getPostData()
})
</script>

<style lang="scss" scoped>
.forum-detail-container {
  padding: 20px;
  
  .back-btn {
    margin-bottom: 20px;
  }
  
  .post-detail-card {
    margin-bottom: 20px;
    
    .post-header {
      margin-bottom: 20px;
      
      .post-title {
        margin-top: 0;
        margin-bottom: 15px;
        font-size: 24px;
        color: #303133;
      }
      
      .post-meta {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .post-author {
          display: flex;
          align-items: center;
          
          .author-info {
            margin-left: 10px;
            
            .author-name {
              font-weight: bold;
              font-size: 16px;
            }
            
            .post-time {
              font-size: 12px;
              color: #909399;
            }
          }
        }
        
        .post-tags {
          .post-tag {
            margin-left: 5px;
          }
        }
      }
    }
    
    .post-content {
      line-height: 1.8;
      color: #303133;
      margin-bottom: 30px;
      word-break: break-word;
    }
    
    .post-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 15px;
      border-top: 1px solid #ebeef5;
      
      .post-actions {
        display: flex;
        
        .action-item {
          display: flex;
          align-items: center;
          margin-right: 20px;
          cursor: pointer;
          color: #606266;
          
          &:hover {
            color: #409EFF;
          }
          
          .el-icon {
            margin-right: 5px;
            
            &.liked {
              color: #409EFF;
            }
          }
        }
      }
      
      .post-stats {
        .views-count {
          display: flex;
          align-items: center;
          color: #909399;
          
          .el-icon {
            margin-right: 5px;
          }
        }
      }
    }
  }
  
  .comments-card {
    .comments-header {
      font-weight: bold;
      font-size: 18px;
    }
    
    .comment-form {
      margin-bottom: 30px;
      
      .comment-form-actions {
        display: flex;
        justify-content: flex-end;
        margin-top: 10px;
      }
    }
    
    .comments-list {
      .comment-item {
        padding: 20px 0;
        border-bottom: 1px solid #ebeef5;
        
        &:last-child {
          border-bottom: none;
        }
        
        .comment-user {
          display: flex;
          align-items: center;
          margin-bottom: 10px;
          
          .comment-user-info {
            margin-left: 10px;
            
            .comment-user-name {
              font-weight: bold;
            }
            
            .comment-time {
              font-size: 12px;
              color: #909399;
            }
          }
        }
        
        .comment-content {
          margin-bottom: 10px;
          line-height: 1.6;
          word-break: break-word;
        }
        
        .comment-actions {
          display: flex;
          margin-bottom: 10px;
          
          .comment-action {
            display: flex;
            align-items: center;
            margin-right: 15px;
            font-size: 13px;
            color: #909399;
            cursor: pointer;
            
            &:hover {
              color: #409EFF;
            }
            
            .el-icon {
              margin-right: 5px;
              
              &.liked {
                color: #409EFF;
              }
            }
          }
        }
        
        .reply-form {
          background-color: #f7f7f7;
          padding: 10px;
          border-radius: 4px;
          margin-bottom: 15px;
          
          .reply-form-actions {
            display: flex;
            justify-content: flex-end;
            margin-top: 10px;
          }
        }
        
        .replies-list {
          background-color: #f7f7f7;
          padding: 10px;
          border-radius: 4px;
          
          .reply-item {
            padding: 10px 0;
            border-bottom: 1px dashed #dcdfe6;
            
            &:last-child {
              border-bottom: none;
            }
            
            .reply-user {
              display: flex;
              align-items: center;
              margin-bottom: 8px;
              
              .reply-user-info {
                margin-left: 8px;
                
                .reply-user-name {
                  font-weight: bold;
                  font-size: 14px;
                }
                
                .reply-time {
                  font-size: 12px;
                  color: #909399;
                }
              }
            }
            
            .reply-content {
              margin-bottom: 8px;
              word-break: break-word;
            }
            
            .reply-actions {
              display: flex;
              
              .reply-action {
                display: flex;
                align-items: center;
                margin-right: 15px;
                font-size: 12px;
                color: #909399;
                cursor: pointer;
                
                &:hover {
                  color: #409EFF;
                }
                
                .el-icon {
                  margin-right: 3px;
                  
                  &.liked {
                    color: #409EFF;
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
</style> 