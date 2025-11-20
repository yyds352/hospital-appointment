<template>
  <div class="forum-container">
    <div class="forum-header">
      <h1>健康论坛</h1>
      <el-button type="primary" @click="showCreatePostDialog">发布帖子</el-button>
    </div>

    <!-- 搜索区域 -->
    <div class="search-container">
            <el-input
        v-model="searchKeyword"
              placeholder="搜索帖子"
              class="search-input"
              clearable
              @keyup.enter="handleSearch"
            >
        <template #append>
          <el-button @click="handleSearch">
                <el-icon><Search /></el-icon>
          </el-button>
              </template>
            </el-input>

      <el-select v-model="queryParams.sortBy" placeholder="排序方式" @change="handleQuery">
        <el-option label="最新发布" value="createTime" />
        <el-option label="最多点赞" value="likes" />
        <el-option label="最多评论" value="commentCount" />
        <el-option label="最多收藏" value="favoriteCount" />
      </el-select>
    </div>

    <!-- 分类筛选 -->
    <div class="category-filter">
      <span class="filter-label">分类筛选：</span>
      <el-select v-model="queryCategoryId" placeholder="选择分类" clearable @change="handleCategoryFilter">
        <el-option
          v-for="category in categories"
          :key="category.id"
          :label="category.name"
          :value="category.id"
        />
      </el-select>
    </div>

    <!-- 内容区域 -->
    <div class="forum-content">
      <!-- 左侧内容 -->
      <div class="forum-main">
        <!-- 标签过滤区 -->
        <div class="tag-filter">
          <span class="filter-label">标签筛选：</span>
          <div class="tag-list">
            <el-tag
              v-for="tag in selectedTags"
              :key="tag.id"
              closable
              :type="getTagType(tag.id)"
              @close="removeTag(tag)"
            >
              {{ tag.name }}
            </el-tag>
          </div>
        </div>

      <!-- 帖子列表 -->
        <div v-loading="loading" class="post-list">
          <div v-if="posts.length === 0" class="empty-posts">
            <el-empty description="暂无帖子" />
          </div>
          <el-card
            v-for="post in posts"
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
                  :type="getTagType(index)"
                  effect="plain"
                  class="post-tag"
                  @click.stop="handleTagClick({id: index, name: tag})"
                >
                  {{ tag }}
                </el-tag>
              </div>
              <div class="post-stats" @click.stop>
                <div class="stat-item">
                  <el-button 
                    text 
                    size="small" 
                    @click="handleLike(post)"
                    :loading="post.likeLoading"
                  >
                    <el-icon :color="post.liked ? '#409EFF' : ''"><ChatDotRound /></el-icon>
                    <span>{{ post.likes || 0 }}</span>
                  </el-button>
                </div>
                
                <div class="stat-item">
                  <el-icon><ChatDotSquare /></el-icon>
                  <span>{{ post.commentCount || 0 }}</span>
                </div>
                
                <div class="stat-item">
                  <el-button 
                    text 
                    size="small" 
                    @click.stop="handleFavorite(post)"
                    :loading="post.favoriteLoading"
                  >
                    <el-icon :color="post.favorited ? '#E6A23C' : ''">
                      <StarFilled v-if="post.favorited" />
                      <Star v-else />
                    </el-icon>
                    <span>{{ post.favoriteCount || 0 }}</span>
                  </el-button>
                </div>
              </div>
            </div>
            <!-- 帖子卡片操作区 -->
            <div class="post-actions">
              <el-button 
                :loading="post.favoriteLoading" 
                size="small" 
                :type="post.favorited ? 'success' : 'default'" 
                @click.stop="toggleFavorite(post)"
              >
                <template #icon>
                  <el-icon><Star /></el-icon>
                </template>
                {{ post.favorited ? '已收藏' : '收藏' }}
              </el-button>

              <el-button size="small" type="primary" @click="viewPost(post)">
                阅读全文
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

      <!-- 右侧边栏 -->
      <div class="forum-sidebar">
        <!-- 论坛统计 -->
        <el-card class="sidebar-card">
          <template #header>
            <div class="sidebar-header">论坛统计</div>
          </template>
          <div class="sidebar-body">
            <p><strong>总帖子数：</strong> {{ statistics.totalPosts || 0 }}</p>
            <p><strong>总用户数：</strong> {{ statistics.totalUsers || 0 }}</p>
            <p><strong>今日发帖：</strong> {{ statistics.todayPosts || 0 }}</p>
          </div>
        </el-card>
        
        <!-- 用户操作 -->
        <el-card class="sidebar-card">
          <template #header>
            <div class="sidebar-header">我的论坛</div>
          </template>
          <div class="sidebar-body forum-actions">
            <router-link to="/home/forum/my-posts">
              <el-button text>我的帖子</el-button>
            </router-link>
            <router-link to="/home/forum/favorites">
              <el-button text>我的收藏</el-button>
            </router-link>
      </div>
    </el-card>
      </div>
    </div>

    <!-- 发帖对话框 -->
    <el-dialog
      v-model="createDialog.visible"
      title="发布帖子"
      width="650px"
    >
      <el-form
        ref="postFormRef"
        :model="postForm"
        :rules="postRules"
        label-position="top"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="postForm.title" placeholder="请输入帖子标题（5-50个字符）" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="postForm.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="postForm.content"
            type="textarea"
            :rows="8"
            placeholder="请输入帖子内容"
          />
        </el-form-item>
        <el-form-item label="标签">
          <el-select
            v-model="postForm.tags"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="请选择或输入标签（最多5个）"
            style="width: 100%"
            :max-collapse-tags="3"
          >
            <el-option
              v-for="tag in commonTags"
              :key="tag"
              :label="tag"
              :value="tag"
            />
          </el-select>
          <div class="form-tips">标签之间用逗号或空格分隔，最多添加5个标签</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="createDialog.loading" @click="submitPost">
            发布
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, View, ChatDotRound, Star, StarFilled, ChatDotSquare } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/modules/user'
import { 
  getPosts, 
  createForumPost,
  getForumStats,
  getCategories,
  searchPosts,
  likePost as likePostAPI,
  favoritePost,
  unfavoritePost,
  checkFavoriteStatus
} from '@/api/forum'

const defaultAvatar = new URL('@/assets/default-avatar.png', import.meta.url).href

const router = useRouter()
const userStore = useUserStore()

// 查询参数
const searchKeyword = ref('')
const queryCategoryId = ref(null)
const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: '',
  categoryId: null,
  tags: [],
  sortBy: 'createTime'
})

// 数据加载状态
const loading = ref(false)
const categoriesLoading = ref(false)
const tagsLoading = ref(false)

// 数据列表
const posts = ref([])
const total = ref(0)
const categories = ref([])
const selectedTags = ref([])
const popularTags = ref([])
const statistics = ref({
  totalPosts: 0,
  totalComments: 0,
  totalUsers: 0,
  postsToday: 0,
  commentsToday: 0
})

// 常用标签列表
const commonTags = ref([
  '健康饮食', '体育锻炼', '心理健康', '医疗常识', 
  '睡眠质量', '疾病预防', '校园健康', '常见疾病',
  '运动指南', '营养科普', '心理咨询', '医疗政策'
])

// 发帖对话框
const createDialog = reactive({
  visible: false,
  loading: false
})

// 发帖表单
const postFormRef = ref(null)
const postForm = reactive({
  title: '',
  content: '',
  categoryId: '',
  tags: []
})

// 表单验证规则
const postRules = {
  title: [
    { required: true, message: '请输入帖子标题', trigger: 'blur' },
    { min: 5, max: 50, message: '标题长度在5到50个字符之间', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择帖子分类', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入帖子内容', trigger: 'blur' },
    { min: 10, max: 5000, message: '内容长度在10到5000个字符之间', trigger: 'blur' }
  ]
}

// 获取帖子列表
const fetchPosts = async () => {
  loading.value = true;
  
  // 检查用户是否登录
  const isLoggedIn = !!localStorage.getItem('token');
  
  // 允许未登录用户查看真实帖子数据，只有需要认证的操作才需要登录
  console.log(isLoggedIn ? '用户已登录，获取真实帖子数据' : '用户未登录，获取真实帖子数据');
  
  try {
    // 构建查询参数
    const params = {
      page: queryParams.page - 1, // 后端分页从0开始
      size: queryParams.size,
      sortBy: queryParams.sortBy || undefined
    };
    
    console.log('请求参数:', params);
    console.log('当前token:', localStorage.getItem('token'));
    
    // 添加分类筛选
    if (queryParams.categoryId) {
      params.categoryId = queryParams.categoryId;
    }
    
    // 添加标签筛选
    if (selectedTags.value.length > 0) {
      // 将选中的标签名称拼接为逗号分隔的字符串
      params.tags = selectedTags.value.map(tag => tag.name).join(',');
    }
    
    const response = await getPosts(params);
    
    console.log('帖子列表响应:', response);
    console.log('响应类型:', typeof response);
    console.log('响应结构:', JSON.stringify(response, null, 2));
    
    // 处理不同的数据结构
    let postsData = [];
    let totalItems = 0;
    
    if (response && response.content) {
      // 标准分页结构
      postsData = response.content;
      totalItems = response.totalElements;
      console.log('直接使用分页对象格式');
    } else if (response && Array.isArray(response)) {
      // 直接返回数组
      postsData = response;
      totalItems = response.length;
      console.log('直接使用数组格式');
    } else if (response && response.code === 200 && response.data) {
      // Result<Page<T>>嵌套结构
      if (response.data.content) {
        postsData = response.data.content;
        totalItems = response.data.totalElements;
        console.log('使用Result<Page<T>>格式');
      } else if (Array.isArray(response.data)) {
        postsData = response.data;
        totalItems = response.data.length;
        console.log('使用Result<List<T>>格式');
      }
    } else {
      console.log('未能识别的响应格式:', response);
      mockPosts();
      return;
    }
    
    console.log('提取到的帖子数据:', postsData.length, '条记录');
    
    if (postsData.length > 0) {
      // 映射帖子数据，添加UI所需属性
      posts.value = postsData.map(post => ({
        ...post,
        likeLoading: false,
        favoriteLoading: false,
        favorited: false, // 默认未收藏
        // 确保有默认值，避免UI渲染错误
        commentCount: post.commentCount || 0,
        likes: post.likes || 0,
        views: post.views || 0,
        favoriteCount: post.favoriteCount || 0,
        authorName: post.authorName || '匿名用户',
        authorAvatar: post.authorAvatar || '',
        createTime: post.createTime || post.createdAt || new Date().toISOString(),
        categoryName: post.categoryName || (post.categoryId ? '未知分类' : '未分类')
      }));
      total.value = totalItems;
      
      // 获取所有帖子ID进行批量检查
      const postIds = posts.value.map(post => post.id);
      await checkFavorites(postIds);
    } else {
      posts.value = [];
      total.value = 0;
      console.log('未获取到帖子数据');
    }
  } catch (error) {
    console.error('获取帖子列表失败:', error);
    console.log('使用模拟帖子数据 (API错误)');
    mockPosts();
  } finally {
    loading.value = false;
  }
}



// 获取分类列表
const fetchCategories = async () => {
  categoriesLoading.value = true;
  try {
    const response = await getCategories();
    console.log('分类列表响应:', response);
    
    // 请求工具会自动处理code=200的情况并返回data字段
    // 所以response应该是直接的数组数据
    if (Array.isArray(response) && response.length > 0) {
      categories.value = response;
      console.log('成功获取分类数据:', response.length, '条记录');
    } else {
      console.log('未获取到分类数据，使用模拟数据');
      mockCategories();
    }
  } catch (error) {
    console.error('获取分类失败:', error);
    // 使用模拟数据
    console.log('使用模拟分类数据 (API错误)');
    mockCategories();
  } finally {
    categoriesLoading.value = false;
  }
}

// 获取论坛统计数据
const fetchForumStats = async () => {
  // 检查用户是否登录
  const isLoggedIn = !!localStorage.getItem('token');
  
  if (!isLoggedIn) {
    console.log('用户未登录，使用模拟论坛统计数据');
    mockForumStats();
    return;
  }
  
  try {
    const response = await getForumStats();
    console.log('论坛统计响应:', response);
    
    // 处理不同的数据结构
    let statsData = null;
    
    if (response && typeof response === 'object' && !Array.isArray(response) && !response.data) {
      // 直接是对象，没有data字段
      statsData = response;
      console.log('统计数据是直接对象格式');
    } else if (response && response.code === 200 && response.data) {
      // Result包装的数据
      statsData = response.data;
      console.log('统计数据是Result包装格式');
    }
    
    if (statsData && typeof statsData === 'object') {
      console.log('应用统计数据:', statsData);
      Object.assign(statistics.value, statsData);
    } else {
      console.log('未获取到有效统计数据，使用模拟数据');
      mockForumStats();
    }
  } catch (error) {
    console.error('获取论坛统计失败:', error);
    console.log('使用模拟论坛统计数据 (API错误)');
    mockForumStats();
  }
}

// 获取热门标签
const fetchPopularTags = () => {
  tagsLoading.value = true
  
  // 这里应该调用API获取热门标签，暂时使用默认数据
  setTimeout(() => {
    popularTags.value = [
      { id: 1, name: '健康饮食', count: 28 },
      { id: 2, name: '体育锻炼', count: 24 },
      { id: 3, name: '心理健康', count: 22 },
      { id: 4, name: '医疗常识', count: 19 },
      { id: 5, name: '睡眠质量', count: 16 },
      { id: 6, name: '疾病预防', count: 15 },
      { id: 7, name: '校园健康', count: 14 },
      { id: 8, name: '常见疾病', count: 12 }
    ]
    tagsLoading.value = false
  }, 500)
}

// 生成随机数据用于测试
const mockPosts = () => {
  const mockData = []
  const titles = [
    '如何保持良好的作息习惯，提高学习效率？',
    '大学生常见的颈椎问题如何预防和改善',
    '校园常见传染病预防指南',
    '考试季压力调节：如何保持心理健康',
    '大学生饮食指南：平衡营养、健康饮食',
    '如何科学安排运动时间，提高身体素质',
    '口腔健康小知识：大学生必知的牙齿保护方法',
    '近视防控指南：保护视力从小事做起',
    '感冒与流感的区别及自我处理方法',
    '校园就医指南：如何正确使用校医院资源'
  ]
  
  const contents = [
    '保持规律的作息时间是提高学习效率的关键。建议每天保持7-8小时的睡眠，避免熬夜，早上按时起床。学习时间应该安排在精力充沛的时段，比如上午9-11点和下午3-5点。此外，适当的休息也很重要，建议采用番茄工作法，每专注学习25分钟后休息5分钟。',
    '大学生由于长时间低头看书、使用电脑和手机，容易出现颈椎问题。预防措施包括：保持正确坐姿，屏幕与眼睛平行；每学习一小时起来活动颈部；加强颈部肌肉锻炼；使用符合人体工程学的椅子和桌子。如果已经出现颈椎不适，可以尝试热敷和颈部按摩缓解。',
    '校园是人口密集的场所，容易发生传染病传播。预防措施包括：勤洗手，保持个人卫生；避免密切接触患病同学；保持宿舍通风；增强体质，合理膳食；出现症状及时就医。常见传染病包括流感、水痘、结核病等，了解其症状和传播途径有助于及时预防。',
    '考试季是学生压力最大的时期，做好心理调节很重要。建议：制定合理复习计划，避免临时抱佛脚；保持充足睡眠；适当运动释放压力；学会放松技巧如深呼吸和冥想；与朋友交流分担压力；遇到解决不了的问题及时寻求心理咨询。',
    '大学生正处于身体发育的重要阶段，需要均衡的营养摄入。建议：三餐规律，不暴饮暴食；每天摄入足够的蛋白质、碳水化合物、脂肪、维生素和矿物质；多吃蔬菜水果；少吃垃圾食品和高糖饮料；注意食品卫生安全；根据个人情况调整饮食结构。'
  ]
  
  const authors = [
    { name: '健康达人', avatar: '' },
    { name: '校医李医生', avatar: '' },
    { name: '疾控专家', avatar: '' },
    { name: '心理咨询师', avatar: '' },
    { name: '营养师张老师', avatar: '' }
  ]
  
  const tagsList = [
    ['作息', '学习效率', '时间管理'],
    ['颈椎健康', '姿势矫正', '大学生健康'],
    ['传染病预防', '卫生习惯', '健康防护'],
    ['心理健康', '减压方法', '考试anxiety'],
    ['健康饮食', '营养均衡', '饮食指南']
  ]
  
  for (let i = 0; i < 10; i++) {
    const index = i % 5
    const date = new Date()
    date.setDate(date.getDate() - Math.floor(Math.random() * 30))
    
    mockData.push({
      id: i + 1,
      title: titles[i],
      content: contents[index] || '帖子内容示例，这是一段模拟数据。',
      authorName: authors[index % authors.length].name,
      authorAvatar: authors[index % authors.length].avatar,
      createTime: date.toISOString(),
      updateTime: date.toISOString(),
      views: Math.floor(Math.random() * 500) + 50,
      likes: Math.floor(Math.random() * 100),
      commentCount: Math.floor(Math.random() * 50),
      categoryId: (index % 5) + 1,
      categoryName: categories.value[(index % 5)].name,
      tags: tagsList[index] || []
    })
  }
  
  posts.value = mockData
  total.value = mockData.length
}



// 模拟分类数据
const mockCategories = () => {
  console.log('使用模拟分类数据')
  categories.value = [
    { id: 1, name: '健康咨询' },
    { id: 2, name: '医疗常识' },
    { id: 3, name: '心理健康' },
    { id: 4, name: '饮食营养' },
    { id: 5, name: '运动健身' }
  ]
}

// 模拟论坛统计数据
const mockForumStats = () => {
  console.log('使用模拟论坛统计数据')
  Object.assign(statistics.value, {
    totalPosts: 158,
    totalComments: 328,
    totalUsers: 96,
    postsToday: 12,
    commentsToday: 24
  })
}

// 处理搜索
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    // 如果关键词为空，恢复正常列表
    queryParams.page = 1;
    return fetchPosts();
  }

  loading.value = true;
  try {
    console.log('搜索关键词:', searchKeyword.value);
    const response = await searchPosts(searchKeyword.value, {
      page: queryParams.page - 1,
      size: queryParams.size,
      sortBy: queryParams.sortBy
    });
    
    console.log('搜索结果响应:', response);
    
    // 处理不同的数据结构
    let searchData = [];
    let totalItems = 0;
    
    if (response && response.content) {
      // 标准分页结构
      searchData = response.content;
      totalItems = response.totalElements;
    } else if (response && Array.isArray(response)) {
      // 直接返回数组
      searchData = response;
      totalItems = response.length;
    } else if (response && response.data) {
      // 嵌套结构
      if (response.data.content) {
        searchData = response.data.content;
        totalItems = response.data.totalElements;
      } else if (Array.isArray(response.data)) {
        searchData = response.data;
        totalItems = response.data.length;
      }
    }
    
    if (searchData.length > 0) {
      posts.value = searchData.map(post => ({
        ...post,
        likeLoading: false,
        favoriteLoading: false
      }));
      total.value = totalItems;
    } else {
      ElMessage.warning('未找到匹配的结果');
      posts.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error('搜索失败:', error);
    ElMessage.error('搜索失败，请稍后重试');
    posts.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

// 处理查询
const handleQuery = () => {
  queryParams.page = 1
  fetchPosts()
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  queryParams.size = size
  fetchPosts()
}

// 处理页码变化
const handleCurrentChange = (page) => {
  queryParams.page = page
  fetchPosts()
}

// 显示创建帖子对话框
const showCreatePostDialog = () => {
  createDialog.visible = true
  
  // 重置表单
  Object.assign(postForm, {
    title: '',
    content: '',
    categoryId: '',
    tags: []
  })
  
  // 重置表单验证
  if (postFormRef.value) {
    postFormRef.value.resetFields()
  }
}

// 提交帖子
const submitPost = () => {
  postFormRef.value.validate(async (valid) => {
    if (valid) {
      createDialog.loading = true
      try {
        // 将标签数组转换为逗号分隔的字符串
        const postData = {
          ...postForm,
          tags: Array.isArray(postForm.tags) ? postForm.tags.join(',') : ''
        };
        
        console.log('发送帖子数据:', postData);
        const response = await createForumPost(postData);
        
        ElMessage.success('发布成功')
        createDialog.visible = false
        
        // 刷新帖子列表
        queryParams.page = 1
        fetchPosts()
        
        // 如果发布成功，跳转到帖子详情页
        if (response.data && response.data.id) {
          router.push('/home/forum/post/' + response.data.id)
        }
      } catch (error) {
        console.error('发布帖子失败:', error)
        ElMessage.error('发布失败: ' + (error.message || '未知错误'))
      } finally {
        createDialog.loading = false
      }
    }
  })
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

// 截断内容
const truncateContent = (content) => {
  if (!content) return ''
  return content.length > 100 ? content.substring(0, 100) + '...' : content
}

// 获取帖子标签
const getPostTags = (post) => {
  if (!post.tags) return []
  
  if (typeof post.tags === 'string') {
    return post.tags.split(/[,，\s]+/).filter(tag => tag.trim() !== '')
  }
  
  return post.tags
}

// 获取分类名称
const getCategoryName = (categoryId) => {
  if (!categoryId) return '未分类'
  const category = categories.value.find(c => c.id === categoryId)
  return category ? category.name : '未分类'
}

// 根据ID生成不同的标签类型
const getTagType = (id) => {
  if (!id) return ''
  const types = ['', 'success', 'warning', 'danger', 'info']
  return types[id % types.length]
}

// 处理标签点击
const handleTagClick = (tag) => {
  // 检查是否已经选中
  const isSelected = selectedTags.value.some(item => item.id === tag.id);
  if (!isSelected) {
    selectedTags.value.push(tag);
    // 更新查询，保持其他参数不变
    queryParams.page = 1; // 重置到第一页
    fetchPosts();
  }
}

// 移除标签筛选
const removeTag = (tag) => {
  selectedTags.value = selectedTags.value.filter(item => item.id !== tag.id);
  // 重置到第一页并重新加载数据
  queryParams.page = 1;
  fetchPosts();
}

// 查看帖子详情
const viewPost = (post) => {
  router.push({
    path: `/forum/post/${post.id}`
  });
}

// 点赞帖子
const handleLike = async (post) => {
  // 检查是否已登录
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录')
    return
  }
  
  // 防止重复点击
  if (post.likeLoading) return
  post.likeLoading = true
  
  try {
    await likePostAPI(post.id)
    
    // 更新点赞状态和计数
    post.liked = !post.liked
    if (post.liked) {
      post.likes = (post.likes || 0) + 1
    } else {
      post.likes = Math.max(0, (post.likes || 1) - 1)
    }
    
    ElMessage.success(post.liked ? '点赞成功' : '取消点赞成功')
  } catch (error) {
    console.error('点赞操作失败:', error)
    ElMessage.error('点赞操作失败，请稍后重试')
  } finally {
    post.likeLoading = false
  }
}

// 收藏帖子
const handleFavorite = async (post) => {
  // 检查是否已登录
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录')
    return
  }
  
  // 防止重复点击
  if (post.favoriteLoading) return
  post.favoriteLoading = true
  
  try {
    if (post.favorited) {
      await unfavoritePost(post.id)
      post.favoriteCount = Math.max(0, (post.favoriteCount || 1) - 1)
    } else {
      await favoritePost(post.id)
      post.favoriteCount = (post.favoriteCount || 0) + 1
    }
    
    post.favorited = !post.favorited
    ElMessage.success(post.favorited ? '收藏成功' : '取消收藏成功')
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('收藏操作失败，请稍后重试')
  } finally {
    post.favoriteLoading = false
  }
}

// 处理分类筛选
const handleCategoryFilter = () => {
  queryParams.categoryId = queryCategoryId.value;
  queryParams.page = 1;
  fetchPosts();
}

// 切换收藏状态
const toggleFavorite = async (post) => {
  if (!localStorage.getItem('token')) {
    ElMessage.warning('请先登录后再收藏帖子')
    return
  }
  
  try {
    post.favoriteLoading = true
    
    if (post.favorited) {
      await unfavoritePost(post.id)
      post.favoriteCount = Math.max((post.favoriteCount || 0) - 1, 0)
    } else {
      await favoritePost(post.id)
      post.favoriteCount = (post.favoriteCount || 0) + 1
    }
    
    post.favorited = !post.favorited
    ElMessage.success(post.favorited ? '收藏成功' : '取消收藏成功')
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('收藏操作失败，请稍后重试')
  } finally {
    post.favoriteLoading = false
  }
}

// 检查帖子收藏状态
const checkFavorites = async (postIds) => {
  if (!postIds.length || !localStorage.getItem('token')) return
  
  try {
    for (const postId of postIds) {
      const post = posts.value.find(p => p.id === postId)
      if (!post) continue
      
      try {
        const response = await checkFavoriteStatus(postId)
        // 处理不同的响应格式
        if (response === true || response === false) {
          post.favorited = response
        } else if (response && response.data !== undefined) {
          post.favorited = response.data
        }
      } catch (e) {
        console.warn(`检查帖子ID:${postId}的收藏状态失败:`, e)
      }
    }
  } catch (error) {
    console.error('批量检查收藏状态失败:', error)
  }
}

// 初始化数据
onMounted(async () => {
  console.log('论坛列表组件挂载，开始初始化数据...');
  console.log('当前token:', localStorage.getItem('token'));
  console.log('当前路由路径:', window.location.pathname);
  console.log('当前查询参数:', window.location.search);
  
  try {
    // 加载分类列表（不需要登录）
    console.log('开始获取分类列表...');
    await fetchCategories();
    console.log('分类列表获取完成');
    
    // 加载热门标签（使用模拟数据）
    console.log('开始获取热门标签...');
    fetchPopularTags();
    console.log('热门标签获取完成');
    
    // 检查用户是否登录
    const isLoggedIn = !!localStorage.getItem('token');
    console.log('用户登录状态:', isLoggedIn);
    
    // 所有用户都可以查看帖子和论坛统计，只有需要认证的操作才需要登录
    console.log('开始获取帖子列表...');
    await fetchPosts();
    console.log('帖子列表获取完成');
    
    console.log('开始获取论坛统计...');
    await fetchForumStats();
    console.log('论坛统计获取完成');
    
    console.log(isLoggedIn ? '用户已登录，加载完整数据' : '用户未登录，加载公共数据');
  } catch (error) {
    console.error('初始化论坛数据失败:', error);
    console.error('错误详情:', error.message);
    console.error('错误堆栈:', error.stack);
    // 确保即使发生错误，页面仍然能显示
    console.log('使用模拟数据作为后备方案');
    mockCategories();
    mockPosts();
    mockForumStats();
  }
  
  console.log('初始化完成，当前帖子数量:', posts.value.length);
});
</script>

<style lang="scss" scoped>
.forum-container {
  padding: 20px;
  
  .forum-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    h1 {
      margin: 0;
    }
  }
  
  .search-container {
      display: flex;
    margin-bottom: 20px;
      
      .search-input {
      margin-right: 10px;
        width: 300px;
      }
  }
  
  .category-filter {
    display: flex;
    align-items: center;
    margin-bottom: 15px;
    
    .filter-label {
      margin-right: 10px;
      color: #606266;
      font-weight: bold;
    }
    
    .el-select {
      width: 200px;
    }
  }
  
  .forum-content {
    display: flex;
    gap: 20px;
    
    .forum-main {
      flex: 1;
      
      .tag-filter {
        display: flex;
        align-items: center;
        margin-bottom: 15px;
        background-color: #fff;
        padding: 10px 15px;
        border-radius: 4px;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        
        .filter-label {
          margin-right: 10px;
          color: #606266;
          font-weight: bold;
        }
        
        .tag-list {
          display: flex;
          flex-wrap: wrap;
          gap: 5px;
    }
  }

  .post-list {
      margin-bottom: 20px;
        
        .post-card {
          margin-bottom: 20px;
          cursor: pointer;
          transition: all 0.3s;
          
          &:hover {
            transform: translateY(-5px);
            box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1);
          }

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
                cursor: pointer;
              }
            }
            
            .post-stats {
              display: flex;
              gap: 15px;
              
              .stat-item {
                display: flex;
                align-items: center;
                gap: 5px;
                color: #606266;
                
                .el-button {
                  display: flex;
                  align-items: center;
                }
          }
        }
      }
    }
  }

      .pagination {
        margin-top: 20px;
    display: flex;
    justify-content: center;
      }
    }
    
    .forum-sidebar {
      width: 300px;
      
      .sidebar-card {
        margin-bottom: 20px;
        
        .sidebar-header {
          font-weight: bold;
        }
        
        .sidebar-body {
          padding: 10px;
          
          p {
            margin: 0;
            margin-bottom: 10px;
            
            strong {
              font-weight: bold;
            }
          }
        }
      }
      
      .forum-actions {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
      
      .sidebar-item {
        margin-bottom: 20px;
      }
    }
  }
  
  .empty-posts {
    padding: 40px 0;
    background-color: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }
  
  .form-tips {
    font-size: 12px;
    color: #909399;
    margin-top: 5px;
  }
}

@media (max-width: 1200px) {
  .forum-content {
    flex-direction: column;
    
    .forum-sidebar {
      width: 100%;
    }
  }
}
</style>