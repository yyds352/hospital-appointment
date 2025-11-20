<template>
  <div class="user-favorites-container">
    <div class="favorites-header">
      <h1>我的收藏</h1>
      <el-button @click="$router.push('/forum')" icon="ArrowLeft">返回论坛</el-button>
    </div>

    <div v-loading="loading" class="favorites-list">
      <div v-if="favoritesList.length === 0" class="empty-favorites">
        <el-empty description="暂无收藏帖子" />
        <el-button type="primary" @click="$router.push('/forum')">去浏览帖子</el-button>
      </div>
      
      <el-card
        v-for="post in favoritesList"
        :key="post.id"
        class="post-card"
        shadow="hover"
      >
        <div class="post-header">
          <div class="user-info">
            <el-avatar :size="40" :src="post.authorAvatar || defaultAvatar"></el-avatar>
            <div class="author-info">
              <div class="author-name">{{ post.authorName || '匿名用户' }}</div>
              <div class="post-time">{{ formatDate(post.createTime) }}</div>
            </div>
          </div>
          <div class="post-category">
            <el-tag size="small">{{ getCategoryName(post.categoryId) }}</el-tag>
          </div>
        </div>
        
        <h3 class="post-title" @click="viewPost(post)">{{ post.title }}</h3>
        <div class="post-content">{{ truncateContent(post.content) }}</div>
        
        <div class="post-footer">
          <div class="post-tags">
            <el-tag
              v-for="(tag, index) in getPostTags(post)"
              :key="index"
              size="small"
              effect="plain"
              class="post-tag"
            >
              {{ tag }}
            </el-tag>
          </div>
          
          <div class="post-stats">
            <div class="stat-item">
              <el-icon><ChatDotSquare /></el-icon>
              <span>{{ post.commentCount || 0 }}</span>
            </div>
            
            <div class="stat-item">
              <el-icon><View /></el-icon>
              <span>{{ post.views || 0 }}</span>
            </div>
            
            <div class="stat-item">
              <el-icon><Star /></el-icon>
              <span>{{ post.likes || 0 }}</span>
            </div>
          </div>
        </div>
        
        <!-- 帖子操作 -->
        <div class="post-actions">
          <el-button 
            type="danger" 
            size="small" 
            @click.stop="removeFavorite(post)" 
            :loading="post.loading"
          >
            <el-icon><Delete /></el-icon> 取消收藏
          </el-button>
          
          <el-button 
            type="primary" 
            size="small" 
            @click.stop="viewPost(post)"
          >
            <el-icon><View /></el-icon> 查看详情
          </el-button>
        </div>
      </el-card>
      
      <!-- 分页 -->
      <el-pagination
        v-if="total > 0"
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :page-sizes="[5, 10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        background
        class="pagination"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotSquare, Star, View, Delete, ArrowLeft } from '@element-plus/icons-vue'
import { getUserFavorites, unfavoritePost, getCategories } from '@/api/forum'

const defaultAvatar = new URL('@/assets/default-avatar.png', import.meta.url).href

const router = useRouter()

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
})

// 数据加载状态
const loading = ref(false)

// 数据列表
const favoritesList = ref([])
const total = ref(0)
const categories = ref([])

// 获取收藏列表
const fetchFavorites = async () => {
  loading.value = true;
  try {
    const params = {
      ...queryParams,
      page: queryParams.page - 1, // 后端从0开始计数
    };
    
    // 检查token
    const token = localStorage.getItem('token');
    if (!token) {
      console.warn('获取收藏列表：用户未登录');
      loading.value = false;
      return;
    }
    
    console.log('正在获取收藏列表，参数:', params);
    const response = await getUserFavorites(params);
    console.log('收藏列表响应:', response);
    
    // 处理不同的数据结构
    let favoritesData = [];
    let totalItems = 0;
    
    if (response && response.content && Array.isArray(response.content)) {
      // 直接是Spring分页对象
      favoritesData = response.content;
      totalItems = response.totalElements;
      console.log('使用直接分页对象格式');
    } else if (response && response.data) {
      // 嵌套在Result中的数据
      if (response.data.content && Array.isArray(response.data.content)) {
        // 标准Spring分页格式
        favoritesData = response.data.content;
        totalItems = response.data.totalElements;
        console.log('使用Result<Page>格式');
      } else if (Array.isArray(response.data)) {
        // 数组格式
        favoritesData = response.data;
        totalItems = response.data.length;
        console.log('使用Result<List>格式');
      } else if (response.data.records && Array.isArray(response.data.records)) {
        // MyBatis Plus分页格式
        favoritesData = response.data.records;
        totalItems = response.data.total;
        console.log('使用MyBatis Plus分页格式');
      } else if (typeof response.data === 'object') {
        // 单个对象
        favoritesData = [response.data];
        totalItems = 1;
        console.log('使用单个对象格式');
      }
    } else if (Array.isArray(response)) {
      // 直接返回数组
      favoritesData = response;
      totalItems = response.length;
      console.log('使用直接数组格式');
    } else {
      console.warn('未知响应格式:', response);
      favoritesData = [];
      totalItems = 0;
    }
    
    // 处理收藏数据，确保post字段存在或直接使用favorite本身
    favoritesList.value = favoritesData.map(favorite => {
      // 检查数据结构
      console.log('处理收藏项:', favorite);
      
      // 获取文章ID，优先使用postId
      const postId = favorite.postId || (favorite.post ? favorite.post.id : null) || favorite.id;
      
      // 获取作者ID
      const authorId = favorite.userId || (favorite.post ? favorite.post.authorId : null);
      
      // 获取作者名称
      const authorName = favorite.username || (favorite.post ? favorite.post.authorName : null) || '未知用户';
      
      // 获取文章标题
      const title = favorite.postTitle || (favorite.post ? favorite.post.title : null) || '未知标题';
      
      // 获取文章内容
      const content = favorite.content || (favorite.post ? favorite.post.content : null) || '未知内容';
      
      // 获取分类ID
      const categoryId = favorite.categoryId || (favorite.post ? favorite.post.categoryId : null) || 0;
      
      // 获取创建时间
      const createTime = favorite.createdAt || (favorite.post ? favorite.post.createdAt : null) || new Date().toISOString();
      
      return {
        id: favorite.id || 0,
        postId: postId,
        title: title,
        content: content,
        authorId: authorId,
        authorName: authorName,
        createTime: createTime,
        categoryId: categoryId,
        views: favorite.views || (favorite.post ? favorite.post.views : null) || 0,
        likes: favorite.likes || (favorite.post ? favorite.post.likes : null) || 0,
        commentCount: favorite.commentCount || (favorite.post ? favorite.post.commentCount : null) || 0,
        tags: favorite.tags || (favorite.post ? favorite.post.tags : null) || [],
        favoriteId: favorite.id,
        loading: false
      };
    });
    
    total.value = totalItems;
    console.log('处理后的收藏列表:', favoritesList.value);
  } catch (error) {
    console.error('获取收藏列表失败:', error);
    ElMessage.error('获取收藏列表失败，请稍后重试');
    favoritesList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    const response = await getCategories();
    if (response && response.data) {
      categories.value = response.data;
    } else {
      categories.value = [];
    }
  } catch (error) {
    console.error('获取分类列表失败:', error);
    categories.value = [];
  }
}

// 移除收藏
const removeFavorite = async (post) => {
  try {
    post.loading = true;
    // 使用postId作为API参数
    const postId = post.postId || post.id;
    console.log('取消收藏, postId:', postId);
    await unfavoritePost(postId);
    ElMessage.success('取消收藏成功');
    fetchFavorites(); // 刷新列表
  } catch (error) {
    console.error('取消收藏失败:', error);
    ElMessage.error('取消收藏失败，请稍后重试');
  } finally {
    post.loading = false;
  }
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  queryParams.size = size;
  fetchFavorites();
}

// 处理页码变化
const handleCurrentChange = (page) => {
  queryParams.page = page;
  fetchFavorites();
}

// 查看帖子详情
const viewPost = (post) => {
  const postId = post.postId || post.id;
  router.push(`/home/forum/post/${postId}`);
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-';
  
  const date = new Date(dateStr);
  const now = new Date();
  const diff = now - date;
  
  // 小于1分钟显示"刚刚"
  if (diff < 60 * 1000) {
    return '刚刚';
  }
  
  // 小于1小时显示"xx分钟前"
  if (diff < 60 * 60 * 1000) {
    return `${Math.floor(diff / (60 * 1000))}分钟前`;
  }
  
  // 小于1天显示"xx小时前"
  if (diff < 24 * 60 * 60 * 1000) {
    return `${Math.floor(diff / (60 * 60 * 1000))}小时前`;
  }
  
  // 小于7天显示"x天前"
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    return `${Math.floor(diff / (24 * 60 * 60 * 1000))}天前`;
  }
  
  // 其他情况显示完整日期
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}

// 截断内容
const truncateContent = (content) => {
  if (!content) return '';
  return content.length > 100 ? content.substring(0, 100) + '...' : content;
}

// 获取帖子标签
const getPostTags = (post) => {
  if (!post.tags) return [];
  
  if (typeof post.tags === 'string') {
    return post.tags.split(/[,，\s]+/).filter(tag => tag.trim() !== '');
  }
  
  return post.tags;
}

// 获取分类名称
const getCategoryName = (categoryId) => {
  if (!categoryId) return '未分类';
  const category = categories.value.find(c => c.id === categoryId);
  return category ? category.name : '未分类';
}

// 初始化数据
onMounted(async () => {
  await fetchCategories();
  fetchFavorites();
});
</script>

<style lang="scss" scoped>
.user-favorites-container {
  padding: 20px;
  
  .favorites-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    h1 {
      margin: 0;
    }
  }
  
  .favorites-list {
    margin-bottom: 20px;
    
    .empty-favorites {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 30px;
      background-color: #fff;
      border-radius: 4px;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
      
      .el-button {
        margin-top: 20px;
      }
    }
    
    .post-card {
      margin-bottom: 20px;
      
      .post-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 15px;

        .user-info {
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
              color: #999;
            }
          }
        }
        
        .post-category {
          .el-tag {
            font-size: 12px;
          }
        }
      }

      .post-title {
        margin-top: 0;
        margin-bottom: 10px;
        color: #303133;
        cursor: pointer;
        
        &:hover {
          color: #409EFF;
        }
      }

      .post-content {
        color: #606266;
        margin-bottom: 10px;
        line-height: 1.5;
      }

      .post-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-top: 15px;
        
        .post-tags {
          display: flex;
          flex-wrap: wrap;
          gap: 5px;
          
          .post-tag {
            margin-right: 5px;
          }
        }
        
        .post-stats {
          display: flex;
          gap: 15px;
          align-items: center;
          
          .stat-item {
            display: flex;
            align-items: center;
            gap: 5px;
            color: #606266;
          }
        }
      }
      
      .post-actions {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-top: 15px;
        
        .el-button {
          margin-left: 5px;
        }
      }
    }
    
    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: center;
    }
  }
}
</style>