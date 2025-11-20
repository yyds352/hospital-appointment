<!-- 帖子列表页面 -->
<template>
  <div class="post-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>健康论坛</span>
          <div class="search-box">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索帖子..."
              clearable
              @keyup.enter="handleSearch"
            >
              <template #append>
                <el-button @click="handleSearch">
                  <el-icon><Search /></el-icon>
                </el-button>
              </template>
            </el-input>
          </div>
          <el-button type="primary" @click="showCreateDialog">发布帖子</el-button>
        </div>
      </template>

      <!-- 帖子分类 -->
      <div class="post-categories">
        <el-radio-group v-model="currentCategory" @change="handleCategoryChange">
          <el-radio-button label="all">全部帖子</el-radio-button>
          <el-radio-button label="hot">热门帖子</el-radio-button>
          <el-radio-button label="latest">最新帖子</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 分类选择 -->
      <div class="category-select">
        <el-select v-model="selectedCategoryId" placeholder="选择分类" clearable @change="handleCategorySelect">
          <el-option
            v-for="category in categories"
            :key="category.id"
            :label="category.name"
            :value="category.id"
          />
        </el-select>
      </div>

      <!-- 帖子列表 -->
      <el-table v-loading="loading" :data="postList" style="width: 100%">
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <router-link :to="{ name: 'PostDetail', params: { id: row.id }}" class="post-title">
              {{ row.title }}
            </router-link>
          </template>
        </el-table-column>
        <el-table-column prop="authorName" label="作者" width="120" />
        <el-table-column prop="views" label="浏览" width="80" />
        <el-table-column prop="likes" label="点赞" width="80" />
        <el-table-column prop="createdAt" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>

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

    <!-- 发布帖子对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="发布帖子"
      width="50%"
    >
      <el-form
        ref="postFormRef"
        :model="postForm"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="postForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="postForm.categoryId" placeholder="请选择分类">
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
            :rows="6"
            placeholder="请输入内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">发布</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPosts, createForumPost, searchPosts, getCategories } from '@/api/forum'
import { formatDateTime } from '@/utils/format'
import { Search } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const postList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const dialogType = ref('create')
const postFormRef = ref(null)
const searchKeyword = ref('')
const isSearching = ref(false)
const currentCategory = ref('all')
const categories = ref([])
const selectedCategoryId = ref(null)

const postForm = ref({
  title: '',
  content: '',
  categoryId: null
})

const rules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在2-100个字符之间', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' },
    { min: 10, message: '内容不能少于10个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ]
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    const response = await getCategories()
    console.log('Categories response:', response)
    if (!response || !response.data) {
      console.error('Invalid categories response:', response)
      ElMessage.error('获取分类列表失败: 无效的响应数据')
      return
    }
    categories.value = response.data
    console.log('Categories set to:', categories.value)
    
    // 如果当前没有选择分类，设置第一个分类为默认值
    if (!postForm.value.categoryId && categories.value.length > 0) {
      postForm.value.categoryId = categories.value[0].id
      console.log('Set default category:', postForm.value.categoryId)
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error(error.message || '获取分类列表失败')
  }
}

// 获取帖子列表
const fetchPosts = async () => {
  loading.value = true
  try {
    const response = await getPosts({
      page: currentPage.value - 1,
      size: pageSize.value,
      categoryId: selectedCategoryId.value === 'all' ? null : selectedCategoryId.value
    })
    postList.value = response.data.content || []
    total.value = response.data.totalElements || 0
  } catch (error) {
    console.error('获取帖子列表失败:', error)
    ElMessage.error(error.message || '获取帖子列表失败')
  } finally {
    loading.value = false
  }
}



// 获取最新帖子
const fetchLatestPosts = async () => {
  loading.value = true
  try {
    const response = await getPosts({
      page: 0,
      size: 10,
      sortBy: 'createTime,desc'
    })
    console.log('Latest posts response:', response)
    postList.value = response.data.content || []
    total.value = response.data.totalElements || 0
  } catch (error) {
    console.error('获取最新帖子失败:', error)
    ElMessage.error(error.message || '获取最新帖子失败')
  } finally {
    loading.value = false
  }
}

// 处理页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchPosts()
}

// 处理每页条数变化
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchPosts()
}

// 显示创建对话框
const showCreateDialog = async () => {
  dialogVisible.value = true // 先显示对话框
  await fetchCategories() // 然后获取分类
  console.log('Categories after fetch:', categories.value)
  
  if (!categories.value || categories.value.length === 0) {
    ElMessage.warning('暂无可用分类，请先创建分类')
    dialogVisible.value = false
    return
  }
  
  // 确保设置默认分类
  const defaultCategoryId = categories.value[0]?.id
  if (!defaultCategoryId) {
    console.error('No valid default category found')
    ElMessage.error('无法设置默认分类，请刷新页面重试')
    dialogVisible.value = false
    return
  }
  
  postForm.value = {
    title: '',
    content: '',
    categoryId: defaultCategoryId
  }
  console.log('Initial postForm:', postForm.value)
}

// 处理提交
const handleSubmit = async () => {
  if (!postFormRef.value) return
  
  try {
    await postFormRef.value.validate()
    
    // 确保 categoryId 存在且为数字类型
    if (!postForm.value.categoryId) {
      console.error('No category selected')
      ElMessage.warning('请选择分类')
      return
    }
    
    const categoryId = parseInt(postForm.value.categoryId, 10)
    if (isNaN(categoryId) || categoryId <= 0) {
      console.error('Invalid category ID:', postForm.value.categoryId)
      ElMessage.warning('分类ID无效，请重新选择分类')
      return
    }
    
    const submitData = {
      title: postForm.value.title.trim(),
      content: postForm.value.content.trim(),
      categoryId: categoryId // 保持驼峰命名，在 API 层会转换为下划线
    }
    
    console.log('Submitting post data:', submitData)
    console.log('Category ID type:', typeof submitData.categoryId)
    
    const response = await createForumPost(submitData)
    console.log('Create post response:', response)
    
    if (response.code === 500) {
      throw new Error(response.message || '发布失败：服务器错误')
    }
    
    if (!response.data) {
      throw new Error('发布失败：服务器响应无效')
    }
    
    ElMessage.success('发布成功')
    dialogVisible.value = false
    await fetchPosts()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(error.message || '提交失败，请检查表单')
  }
}

// 处理搜索
const handleSearch = () => {
  isSearching.value = Boolean(searchKeyword.value.trim())
  currentPage.value = 1
  fetchPosts()
}

// 处理分类变更
const handleCategoryChange = (category) => {
  currentPage.value = 1
  
  if (category === 'latest') {
    fetchLatestPosts()
  } else {
    fetchPosts()
  }
}

// 处理分类选择
const handleCategorySelect = () => {
  currentPage.value = 1
  currentCategory.value = 'all' // 重置到全部帖子
  fetchPosts()
}

// 模拟帖子数据
const mockPosts = () => {
  const mockData = [
    {
      id: 1,
      title: '如何保持良好的作息习惯，提高学习效率？',
      content: '大学生正处于身体发育的重要阶段，需要均衡的营养摄入。建议：三餐规律，不暴饮暴食；每天摄入足够的蛋白质、碳水化合物、脂肪、维生素和矿物质；多吃蔬菜水果；少吃垃圾食品和高糖饮料；注意食品卫生安全。',
      categoryId: 1,
      categoryName: '健康咨询',
      createTime: new Date(Date.now() - 86400000).toISOString(),
      views: 156,
      likes: 28,
      commentCount: 12
    },
    {
      id: 2,
      title: '大学生常见的颈椎问题如何预防和改善',
      content: '长时间低头看书、使用电脑和手机是导致大学生颈椎问题的主要原因。预防建议：保持正确坐姿，屏幕与眼睛平行；每30-40分钟起身活动，做颈部伸展运动；使用符合人体工学的桌椅；睡觉时选择合适高度的枕头。',
      categoryId: 2,
      categoryName: '医疗常识',
      createTime: new Date(Date.now() - 172800000).toISOString(),
      views: 234,
      likes: 45,
      commentCount: 18
    },
    {
      id: 3,
      title: '校园常见传染病预防指南',
      content: '校园是人员密集场所，传染病防控尤为重要。主要措施：勤洗手，使用肥皂和流动水；咳嗽或打喷嚏时用纸巾或肘部遮挡；保持宿舍通风；不共用毛巾、水杯等个人物品；出现发热、咳嗽等症状及时就医；接种疫苗。',
      categoryId: 3,
      categoryName: '心理健康',
      createTime: new Date(Date.now() - 259200000).toISOString(),
      views: 189,
      likes: 32,
      commentCount: 15
    }
  ]
  postList.value = mockData
  total.value = mockData.length
}

// 模拟分类数据
const mockCategories = () => {
  categories.value = [
    { id: 1, name: '健康咨询' },
    { id: 2, name: '医疗常识' },
    { id: 3, name: '心理健康' },
    { id: 4, name: '饮食营养' },
    { id: 5, name: '运动健身' }
  ]
}

onMounted(() => {
  // 检查用户是否登录
  const isLoggedIn = !!localStorage.getItem('token');
  
  if (isLoggedIn) {
    // 用户已登录，正常加载数据
    fetchCategories()
    fetchPosts()
  } else {
    // 用户未登录，使用模拟数据
    console.log('用户未登录，PostList使用模拟数据');
    mockCategories();
    mockPosts();
  }
})
</script>

<style lang="scss" scoped>
.post-list-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .search-box {
      width: 300px;
      margin: 0 20px;
    }
  }

  .post-categories {
    margin: 20px 0;
    display: flex;
    justify-content: center;
  }

  .category-select {
    margin: 20px 0;
    display: flex;
    justify-content: center;

    .el-select {
      width: 200px;
    }
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