<template>
  <div class="my-posts">
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="我的帖子" name="posts">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>我的帖子</span>
              <el-button type="primary" @click="createPost">发布帖子</el-button>
            </div>
          </template>

          <!-- 帖子列表 -->
          <div class="post-list" v-loading="loading">
            <div v-for="post in posts" :key="post.id" class="post-item">
              <el-card shadow="hover">
                <div class="post-header">
                  <div class="post-meta">
                    <h3 class="post-title" @click="viewPost(post)">{{ post.title }}</h3>
                    <div class="post-info">
                      <el-tag size="small" type="info">{{ getCategoryName(post.category) }}</el-tag>
                      <span class="post-time">{{ formatDate(post.createTime) }}</span>
                    </div>
                  </div>
                  <div class="post-actions">
                    <el-button type="primary" link @click="editPost(post)">编辑</el-button>
                    <el-button type="danger" link @click="deletePost(post)">删除</el-button>
                  </div>
                </div>
                <p class="post-content">{{ post.content }}</p>
                <div class="post-footer">
                  <div class="interactions">
                    <span class="interaction-item">
                      <el-icon><View /></el-icon>
                      {{ post.views }}
                    </span>
                    <span class="interaction-item">
                      <el-icon><ChatDotRound /></el-icon>
                      {{ post.commentCount || post.comments || 0 }}
                    </span>
                    <span class="interaction-item">
                      <el-icon><Star /></el-icon>
                      {{ post.likes }}
                    </span>
                  </div>
                </div>
              </el-card>
            </div>

            <!-- 空状态 -->
            <el-empty
              v-if="!loading && posts.length === 0"
              description="暂无帖子"
            >
              <el-button type="primary" @click="createPost">发布帖子</el-button>
            </el-empty>
          </div>

          <!-- 分页 -->
          <div class="pagination-container" v-if="total > 0">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 30, 50]"
              :total="total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-card>
      </el-tab-pane>
      
      <el-tab-pane label="我的收藏" name="favorites">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>我的收藏</span>
            </div>
          </template>

          <!-- 收藏列表 -->
          <div class="post-list" v-loading="favoritesLoading">
            <div v-for="favorite in favorites" :key="favorite.id" class="post-item">
              <el-card shadow="hover">
                <div class="post-header">
                  <div class="post-meta">
                    <h3 class="post-title" @click="viewPost(favorite)">
                      {{ favorite.postTitle || '无标题' }}
                    </h3>
                    <div class="post-info">
                      <el-tag size="small" type="info">
                        {{ favorite.categoryName || '未分类' }}
                      </el-tag>
                      <span class="post-time">
                        {{ formatDate(favorite.createdAt) }}
                      </span>
                    </div>
                  </div>
                  <div class="post-actions">
                    <el-button type="danger" link @click="unfavoritePost(favorite)">
                      取消收藏
                    </el-button>
                  </div>
                </div>
                <p class="post-content">
                  {{ favorite.content || '无内容' }}
                </p>
                <div class="post-footer">
                  <div class="interactions">
                    <span class="interaction-item">
                      <el-icon><View /></el-icon>
                      {{ favorite.views || 0 }}
                    </span>
                    <span class="interaction-item">
                      <el-icon><ChatDotRound /></el-icon>
                      {{ favorite.commentCount || 0 }}
                    </span>
                    <span class="interaction-item">
                      <el-icon><Star /></el-icon>
                      {{ favorite.likes || 0 }}
                    </span>
                  </div>
                </div>
              </el-card>
            </div>

            <!-- 空状态 -->
            <el-empty
              v-if="!favoritesLoading && favorites.length === 0"
              description="暂无收藏"
            >
              <el-button type="primary" @click="goToForum">去浏览论坛</el-button>
            </el-empty>
          </div>

          <!-- 分页 -->
          <div class="pagination-container" v-if="favoriteTotal > 0">
            <el-pagination
              v-model:current-page="favoritePage"
              v-model:page-size="favoritePageSize"
              :page-sizes="[10, 20, 30, 50]"
              :total="favoriteTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleFavoriteSizeChange"
              @current-change="handleFavoritePageChange"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 编辑帖子对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑帖子' : '发布帖子'"
      width="600px"
    >
      <el-form :model="postForm" :rules="rules" ref="postFormRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="postForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="postForm.category" placeholder="请选择分类">
            <el-option
              v-for="item in categories"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="postForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPost">{{ isEdit ? '保存' : '发布' }}</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, ChatDotRound, Star } from '@element-plus/icons-vue'
import { 
  getMyPosts, 
  createForumPost, 
  updateForumPost, 
  deleteForumPost, 
  getUserPosts, 
  getUserFavorites, 
  unfavoritePost as removeFromFavorites,
  getCategories 
} from '@/api/forum'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'

// 数据
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const posts = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const router = useRouter()
const store = useStore()

// 收藏相关
const activeTab = ref('posts')
const favorites = ref([])
const favoritesLoading = ref(false)
const favoritePage = ref(1)
const favoritePageSize = ref(10)
const favoriteTotal = ref(0)

// 表单数据
const postFormRef = ref(null)
const postForm = ref({
  id: null,
  title: '',
  category: '',
  content: ''
})

// 分类选项
const categories = ref([])

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' },
    { min: 10, max: 2000, message: '内容长度在 10 到 2000 个字符', trigger: 'blur' }
  ]
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    const response = await getCategories()
    console.log('获取分类列表响应:', response)
    
    // 响应拦截器已经返回了data部分，所以response应该是直接的数组数据
    if (Array.isArray(response)) {
      categories.value = response.map(category => ({
        label: category.name,
        value: category.id
      }))
      console.log('成功获取分类数据:', categories.value.length, '条记录')
    } else if (response && response.code === 200) {
      // 如果还是完整的响应对象格式
      categories.value = (response.data || []).map(category => ({
        label: category.name,
        value: category.id
      }))
      console.log('成功获取分类数据:', categories.value.length, '条记录')
    } else {
      console.error('获取分类列表失败: 响应格式不正确', response)
      ElMessage.error('获取分类列表失败')
      categories.value = []
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败')
    categories.value = []
  }
}

// 处理Tab切换
const handleTabClick = (tab) => {
  if (tab.props.name === 'favorites') {
    fetchFavorites();
  } else if (tab.props.name === 'posts') {
    fetchPosts();
  }
}

// 获取我的帖子列表
const fetchPosts = async () => {
  loading.value = true
  try {
    // 检查是否有登录令牌
    const token = localStorage.getItem('token');
    if (!token) {
      ElMessage.warning('请先登录后查看我的帖子');
      loading.value = false;
      // 显示模拟数据以避免空白页面
      mockPosts();
      return;
    }
    
    // 获取当前用户ID
    const userInfoStr = localStorage.getItem('userInfo');
    let userId = null;
    
    try {
      const userInfoObj = JSON.parse(userInfoStr);
      // 处理不同格式的用户信息
      if (userInfoObj.id) {
        userId = userInfoObj.id;
      } else if (userInfoObj.data && userInfoObj.data.id) {
        userId = userInfoObj.data.id;
      }
      
      console.log('当前用户ID:', userId);
    } catch (e) {
      console.error('解析用户信息失败:', e);
    }

    console.log('使用令牌获取我的帖子:', token.substring(0, 20) + '...');
    
    try {
      // 尝试从localStorage中解析用户信息，确认是否正确登录
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
      console.log('当前登录用户信息:', userInfo);
    } catch (e) {
      console.error('解析用户信息失败:', e);
    }
    
    let response;
    
    // 尝试不同的方法获取我的帖子
    try {
      // 首先尝试直接使用my-posts API
      response = await getMyPosts({
        page: currentPage.value - 1, // 后端分页从0开始
        size: pageSize.value
      });
    } catch (error) {
      console.log('my-posts API失败，尝试使用用户ID获取帖子');
      
      // 如果失败，尝试通过用户ID获取帖子
      if (userId) {
        response = await getUserPosts(userId, {
          page: currentPage.value - 1,
          size: pageSize.value
        });
      } else {
        // 如果没有用户ID，重新抛出错误
        throw error;
      }
    }
    
    console.log('获取我的帖子响应:', response);
    
    // 检查不同的数据格式并适配
    let postsData = [];
    let totalItems = 0;
    
    if (response && response.content && Array.isArray(response.content)) {
      // 直接是Spring分页对象
      postsData = response.content;
      totalItems = response.totalElements;
      console.log('使用直接分页对象格式');
    } else if (response && response.data) {
      // 嵌套在Result中的数据
      if (response.data.content) {
        // 标准Spring分页格式
        postsData = response.data.content;
        totalItems = response.data.totalElements;
        console.log('使用Result<Page>格式');
      } else if (response.data.records) {
        // MyBatis Plus分页格式
        postsData = response.data.records;
        totalItems = response.data.total;
        console.log('使用MyBatis Plus分页格式');
      } else if (Array.isArray(response.data)) {
        // 数组格式
        postsData = response.data;
        totalItems = response.data.length;
        console.log('使用Result<List>格式');
      } else {
        console.warn('返回数据格式不符合预期:', response.data);
      }
    } else if (Array.isArray(response)) {
      // 直接返回数组
      postsData = response;
      totalItems = response.length;
      console.log('使用直接数组格式');
    } else {
      console.warn('响应格式无法识别:', response);
    }
    
    if (postsData.length > 0) {
      posts.value = postsData.map(post => ({
        ...post,
        // 确保关键字段存在，避免UI渲染错误
        commentCount: post.commentCount || 0,
        likes: post.likes || 0,
        views: post.views || 0,
        favoriteCount: post.favoriteCount || 0,
        createTime: post.createTime || post.createdAt
      }));
      total.value = totalItems;
      console.log(`成功获取${postsData.length}条帖子数据`);
    } else {
      posts.value = [];
      total.value = 0;
      console.warn('未获取到帖子数据');
    }
    
  } catch (error) {
    console.error('获取帖子列表失败:', error);
    
    let needsMockData = true;
    
    if (error.response && error.response.status === 401) {
      // 401错误表示认证问题，但我们不强制重定向
      console.log('认证失败 (401)，使用模拟数据');
      
      // 询问用户是否要重新登录，但不强制跳转
      ElMessage({
        message: '您的登录状态可能已过期，请重新登录以查看我的帖子',
        type: 'warning',
        showClose: true,
        duration: 5000
      });
    } else if (error.message === 'Network Error') {
      ElMessage.error('网络连接异常，请检查网络');
    } else {
      ElMessage.error('获取帖子列表失败: ' + error.message);
    }
    
    if (needsMockData) {
      // 显示模拟数据
      mockPosts();
    }
  } finally {
    loading.value = false
  }
}

// 模拟帖子数据
const mockPosts = () => {
  const mockData = Array(5).fill().map((_, i) => ({
    id: i + 1,
    title: `测试帖子标题 ${i + 1}`,
    content: '这是帖子内容，用于测试显示效果。这是帖子内容，用于测试显示效果。',
    category: '健康咨询',
    createTime: new Date(Date.now() - i * 86400000).toISOString(),
    views: Math.floor(Math.random() * 100),
    comments: Math.floor(Math.random() * 20),
    likes: Math.floor(Math.random() * 50)
  }))
  posts.value = mockData
  total.value = mockData.length
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchPosts()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchPosts()
}

// 获取我的收藏列表
const fetchFavorites = async () => {
  favoritesLoading.value = true;
  try {
    // 检查是否有登录令牌
    const token = localStorage.getItem('token');
    if (!token) {
      ElMessage.warning('请先登录后查看我的收藏');
      favoritesLoading.value = false;
      return;
    }
    
    const response = await getUserFavorites({
      page: favoritePage.value - 1, // 后端分页从0开始
      size: favoritePageSize.value
    });
    
    console.log('获取我的收藏响应:', response);
    
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
      if (response.data.content) {
        // 标准Spring分页格式
        favoritesData = response.data.content;
        totalItems = response.data.totalElements;
        console.log('使用Result<Page>格式');
      } else if (Array.isArray(response.data)) {
        // 数组格式
        favoritesData = response.data;
        totalItems = response.data.length;
        console.log('使用Result<List>格式');
      }
    } else if (Array.isArray(response)) {
      // 直接返回数组
      favoritesData = response;
      totalItems = response.length;
      console.log('使用直接数组格式');
    }
    
    if (favoritesData.length > 0) {
      favorites.value = favoritesData.map(favorite => {
        // 确保favorites处理为标准格式，添加必要的默认值
        return {
          id: favorite.id,
          postId: favorite.postId,
          postTitle: favorite.postTitle || '无标题',
          userId: favorite.userId,
          username: favorite.username || '未知用户',
          categoryName: favorite.categoryName || '未分类',
          content: favorite.content || '无内容',
          createdAt: favorite.createdAt,
          views: favorite.views || 0,
          commentCount: favorite.commentCount || 0,
          likes: favorite.likes || 0
        };
      });
      favoriteTotal.value = totalItems;
      console.log(`成功获取${favoritesData.length}条收藏数据`);
    } else {
      favorites.value = [];
      favoriteTotal.value = 0;
      console.log('未获取到收藏数据');
    }
  } catch (error) {
    console.error('获取收藏列表失败:', error);
    favorites.value = [];
    favoriteTotal.value = 0;
    ElMessage.error('获取收藏列表失败: ' + error.message);
  } finally {
    favoritesLoading.value = false;
  }
}

// 取消收藏
const unfavoritePost = async (favorite) => {
  try {
    // 使用postId而非id
    const postId = favorite.postId || favorite.id;
    await removeFromFavorites(postId);
    ElMessage.success('取消收藏成功');
    // 重新加载收藏列表
    fetchFavorites();
  } catch (error) {
    console.error('取消收藏失败:', error);
    ElMessage.error('取消收藏失败: ' + error.message);
  }
}

// 收藏分页处理
const handleFavoriteSizeChange = (val) => {
  favoritePageSize.value = val;
  fetchFavorites();
}

const handleFavoritePageChange = (val) => {
  favoritePage.value = val;
  fetchFavorites();
}

// 前往论坛
const goToForum = () => {
  router.push('/forum');
}

// 获取分类名称
const getCategoryName = (categoryId) => {
  if (!categoryId) return '未分类'
  const category = categories.value.find(c => c.value === categoryId)
  return category ? category.label : '未分类'
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  
  // 小于1分钟显示"刚刚"
  if (diff < 60 * 1000) {
    return '刚刚'
  }
  
  // 小于1小时显示"xx分钟前"
  if (diff < 60 * 60 * 1000) {
    return `${Math.floor(diff / (60 * 1000))}分钟前`
  }
  
  // 小于1天显示"xx小时前"
  if (diff < 24 * 60 * 60 * 1000) {
    return `${Math.floor(diff / (60 * 60 * 1000))}小时前`
  }
  
  // 小于7天显示"x天前"
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    return `${Math.floor(diff / (24 * 60 * 60 * 1000))}天前`
  }
  
  // 其他情况显示完整日期
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 查看帖子详情
const viewPost = (post) => {
  // 根据是否为收藏项判断使用哪个ID
  const postId = post.postId || post.id;
  router.push('/home/forum/post/' + postId);
}

// 创建帖子
const createPost = () => {
  isEdit.value = false
  postForm.value = {
    id: null,
    title: '',
    category: '',
    content: ''
  }
  dialogVisible.value = true
}

// 编辑帖子
const editPost = async (post) => {
  isEdit.value = true
  
  // 确保分类数据已加载
  if (categories.value.length === 0) {
    await fetchCategories()
  }
  
  // 找到对应的分类ID
  const categoryItem = categories.value.find(cat => cat.label === post.categoryName || cat.label === post.category)
  const categoryId = categoryItem ? categoryItem.value : (post.categoryId || categories.value[0]?.value)
  
  postForm.value = {
    id: post.id,
    title: post.title,
    category: categoryId,
    content: post.content
  }
  dialogVisible.value = true
}

// 删除帖子
const deletePost = async (post) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这篇帖子吗？删除后不可恢复',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await deleteForumPost(post.id)
    ElMessage.success('删除成功')
    fetchPosts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除帖子失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 提交帖子
const submitPost = async () => {
  if (!postFormRef.value) return
  
  await postFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 准备表单数据，确保categoryId是数字类型
        const formData = {
          title: postForm.value.title.trim(),
          content: postForm.value.content.trim(),
          categoryId: parseInt(postForm.value.category, 10)
        };
        
        // 验证分类ID
        if (!formData.categoryId || formData.categoryId <= 0) {
          ElMessage.error('请选择有效的分类')
          return
        }
        
        if (isEdit.value) {
          await updateForumPost(postForm.value.id, formData)
          ElMessage.success('更新成功')
        } else {
          await createForumPost(formData)
          ElMessage.success('发布成功')
        }
        dialogVisible.value = false
        fetchPosts()
      } catch (error) {
        console.error('提交帖子失败:', error)
        ElMessage.error(isEdit.value ? '更新失败：' : '发布失败：' + (error.message || '未知错误'))
      }
    }
  })
}

// 检查并尝试刷新token
const checkAndRefreshToken = async () => {
  const token = localStorage.getItem('token');
  if (!token) {
    console.log('没有token，需要登录');
    return false;
  }
  
  // 检查用户信息是否存在
  const userInfoStr = localStorage.getItem('userInfo');
  if (!userInfoStr) {
    console.log('没有用户信息');
    return false;
  }
  
  try {
    // 解析用户信息
    const userInfo = JSON.parse(userInfoStr);
    console.log('当前用户信息:', userInfo);
    
    // 尝试获取当前用户信息来验证token是否有效
    try {
      await store.dispatch('user/getCurrentUser');
      console.log('token验证成功');
      return true; // token有效
    } catch (e) {
      console.error('获取当前用户信息失败，token可能已失效:', e);
      // 如果是因为未登录的错误，清除本地存储
      if (e.message?.includes('未登录') || e.response?.status === 401) {
        localStorage.removeItem('token');
        localStorage.removeItem('userInfo');
        store.commit('user/CLEAR_USER_INFO');
      }
      return false;
    }
  } catch (e) {
    console.error('解析用户信息失败:', e);
    return false;
  }
}

// 初始化
onMounted(async () => {
  // 先尝试刷新token，然后获取分类和帖子
  await checkAndRefreshToken();
  await fetchCategories(); // 先获取分类数据
  fetchPosts();
})
</script>

<style lang="scss" scoped>
.my-posts {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .post-list {
    margin: 20px 0;

    .post-item {
      margin-bottom: 20px;

      .post-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 12px;

        .post-meta {
          .post-title {
            margin: 0 0 8px;
            cursor: pointer;
            color: var(--el-color-primary);

            &:hover {
              text-decoration: underline;
            }
          }

          .post-info {
            display: flex;
            align-items: center;
            gap: 12px;

            .post-time {
              font-size: 12px;
              color: #999;
            }
          }
        }
      }

      .post-content {
        color: #666;
        margin-bottom: 12px;
        display: -webkit-box;
        -webkit-line-clamp: 3;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }

      .post-footer {
        .interactions {
          display: flex;
          gap: 24px;

          .interaction-item {
            display: flex;
            align-items: center;
            gap: 4px;
            color: #999;
          }
        }
      }
    }
  }

  .pagination-container {
    display: flex;
    justify-content: center;
    margin-top: 20px;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>