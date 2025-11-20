<template>
  <div class="content-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <div class="search-area">
            <el-input
              v-model="searchQuery"
              placeholder="搜索帖子"
              class="search-input"
              clearable
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select v-model="filterCategory" placeholder="分类筛选" clearable @change="handleSearch">
              <el-option
                v-for="item in categories"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
            <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="handleSearch">
              <el-option label="待审核" value="PENDING" />
              <el-option label="已批准" value="APPROVED" />
              <el-option label="已隐藏" value="HIDDEN" />
              <el-option label="已举报" value="REPORTED" />
              <el-option label="全部" value="" />
            </el-select>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
          </div>
          <el-button type="primary" @click="manageCategories">分类管理</el-button>
        </div>
      </template>

      <!-- 帖子管理表格 -->
      <el-table
        v-loading="loading"
        :data="posts"
        style="width: 100%"
        :default-sort="{ prop: 'createdAt', order: 'descending' }"
      >
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <div class="post-title-cell">
              <el-tooltip 
                :content="row.reported ? '该帖子被举报' : ''" 
                :disabled="!row.reported" 
                placement="top"
              >
                <el-badge :is-dot="row.reported" type="danger">
                  <a @click="viewPost(row)" class="post-title">{{ row.title }}</a>
                </el-badge>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="authorName" label="作者" width="120" />
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag 
              :type="getStatusType(row.status)" 
              size="small"
            >
              {{ getStatusText(row.status) }}
            </el-tag>
            <el-badge v-if="row.reportCount > 0" :value="row.reportCount" class="report-badge" type="danger" />
          </template>
        </el-table-column>
        
        <el-table-column prop="category" label="分类" width="120">
          <template #default="{ row }">
            {{ getCategoryName(row.category) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="createdAt" label="发布时间" width="160" sortable>
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button
              type="success"
              size="small"
              plain
              v-if="row.status !== 'APPROVED'"
              @click="approvePost(row)"
              :loading="row.approving"
            >
              批准
            </el-button>
            
            <el-button
              type="warning"
              size="small"
              plain
              v-if="row.status !== 'HIDDEN'"
              @click="hidePost(row)"
              :loading="row.hiding"
            >
              隐藏
            </el-button>
            
            <el-button
              type="primary"
              size="small"
              plain
              @click="viewPost(row)"
            >
              查看
            </el-button>
            
            <el-button
              type="danger"
              size="small"
              plain
              @click="deletePostConfirm(row)"
              :loading="row.deleting"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页器 -->
      <div class="pagination-container">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :current-page="currentPage"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 举报详情对话框 -->
    <el-dialog
      v-model="reportDialogVisible"
      title="举报详情"
      width="600px"
    >
      <div v-if="selectedPost" class="report-dialog-content">
        <h3>帖子：{{ selectedPost.title }}</h3>
        <p>作者：{{ selectedPost.authorName }}</p>
        
        <el-divider content-position="left">举报列表</el-divider>
        
        <el-table
          v-loading="loadingReports"
          :data="reports"
          style="width: 100%"
        >
          <el-table-column prop="reporterName" label="举报人" width="120" />
          <el-table-column prop="reason" label="原因" />
          <el-table-column prop="createdAt" label="举报时间" width="160">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button
                type="danger"
                size="small"
                plain
                @click="dismissReport(row)"
                :loading="row.dismissing"
              >
                驳回
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="reportDialogVisible = false">关闭</el-button>
          <el-button type="success" @click="approveAndDismissAllReports" :loading="processingReports">
            批准帖子并驳回所有举报
          </el-button>
          <el-button type="warning" @click="hideAndCloseAllReports" :loading="processingReports">
            隐藏帖子并关闭所有举报
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 分类管理对话框 -->
    <el-dialog
      v-model="categoryDialogVisible"
      title="分类管理"
      width="500px"
    >
      <div class="category-list">
        <el-table :data="allCategories" style="width: 100%">
          <el-table-column prop="label" label="分类名称" />
          <el-table-column prop="value" label="分类值" />
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button type="primary" link @click="editCategory(row)">编辑</el-button>
              <el-button type="danger" link @click="deleteCategory(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="dialog-footer">
          <el-button type="primary" @click="addCategory">添加分类</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 分类编辑对话框 -->
    <el-dialog
      v-model="categoryEditDialogVisible"
      :title="isEditCategory ? '编辑分类' : '添加分类'"
      width="400px"
    >
      <el-form :model="categoryForm" :rules="categoryRules" ref="categoryFormRef" label-width="80px">
        <el-form-item label="分类名称" prop="label">
          <el-input v-model="categoryForm.label" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类值" prop="value">
          <el-input v-model="categoryForm.value" placeholder="请输入分类值" :disabled="isEditCategory" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="categoryEditDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCategory">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, View, ChatDotRound, Star } from '@element-plus/icons-vue'
import {
  getForumPosts,
  deleteForumPost,
  getAllCategories,
  createCategory,
  updateCategory,
  deleteCategory as deleteCategoryApi,
  approvePost as approvePostAPI,
  hidePost as hidePostAPI,
  getPostReports,
  dismissReport as dismissReportAPI,
  dismissAllReports
} from '@/api/forum'
import { useRouter } from 'vue-router'

// 数据
const loading = ref(false)
const searchQuery = ref('')
const filterCategory = ref('')
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const posts = ref([])
const categoryDialogVisible = ref(false)
const categoryEditDialogVisible = ref(false)
const isEditCategory = ref(false)
const allCategories = ref([])
const reportDialogVisible = ref(false)
const selectedPost = ref(null)
const reports = ref([])
const loadingReports = ref(false)
const processingReports = ref(false)

// 分类表单
const categoryFormRef = ref(null)
const categoryForm = ref({
  label: '',
  value: ''
})

// 分类表单验证规则
const categoryRules = {
  label: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  value: [
    { required: true, message: '请输入分类值', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '只能包含大写字母和下划线', trigger: 'blur' }
  ]
}

// 分类选项
const categories = [
  { label: '健康咨询', value: 'HEALTH_CONSULTATION' },
  { label: '就医经验', value: 'MEDICAL_EXPERIENCE' },
  { label: '医疗资讯', value: 'MEDICAL_NEWS' },
  { label: '其他', value: 'OTHER' }
]

// 路由
const router = useRouter()

// 获取帖子列表
const fetchPosts = async () => {
  loading.value = true
  try {
    console.log('开始获取论坛管理帖子列表，参数:', {
      page: currentPage.value - 1,
      size: pageSize.value,
      query: searchQuery.value,
      category: filterCategory.value,
      status: statusFilter.value
    });
    
    // 检查当前用户角色和权限
    const userInfoStr = localStorage.getItem('userInfo');
    let currentRole = null;
    if (userInfoStr) {
      try {
        const userInfo = JSON.parse(userInfoStr);
        currentRole = userInfo.role;
      } catch (e) {
        console.error('解析用户信息失败:', e);
      }
    }
    const currentToken = localStorage.getItem('token');
    console.log('当前用户角色:', currentRole);
    console.log('Token存在:', !!currentToken);
    console.log('Token内容:', currentToken ? currentToken.substring(0, 20) + '...' : '无');
    
    const response = await getForumPosts({
      page: currentPage.value - 1, // 后端分页从0开始
      size: pageSize.value,
      query: searchQuery.value,
      category: filterCategory.value,
      status: statusFilter.value
    })
    
    console.log('获取论坛管理帖子列表响应:', response);
    
    // 处理不同的响应格式
    let postsData = [];
    let totalItems = 0;
    
    if (response && response.content && Array.isArray(response.content)) {
      // Spring分页对象格式 - 直接返回的分页对象
      postsData = response.content;
      totalItems = response.totalElements;
      console.log('使用直接Spring分页格式');
    } else if (response && response.data) {
      // 如果响应被包装在data属性中
      if (response.data.content && Array.isArray(response.data.content)) {
        // Spring分页对象格式
        postsData = response.data.content;
        totalItems = response.data.totalElements;
        console.log('使用Spring分页格式');
      } else if (Array.isArray(response.data.records)) {
        // MyBatis Plus分页格式
        postsData = response.data.records;
        totalItems = response.data.total;
        console.log('使用MyBatis Plus分页格式');
      } else if (Array.isArray(response.data)) {
        // 数组格式
        postsData = response.data;
        totalItems = response.data.length;
        console.log('使用数组格式');
      } else {
        console.warn('未知响应格式:', response.data);
      }
    } else if (Array.isArray(response)) {
      // 直接返回数组
      postsData = response;
      totalItems = response.length;
      console.log('使用直接数组格式');
    } else {
      console.warn('未能识别的响应格式:', response);
    }
    
    if (postsData.length > 0) {
      posts.value = postsData.map(post => ({
        ...post,
        approving: false,
        hiding: false,
        deleting: false
      }));
      total.value = totalItems;
      console.log(`成功获取${posts.value.length}条管理帖子数据`);
    } else {
      posts.value = [];
      total.value = 0;
      console.warn('未获取到帖子数据');
    }
  } catch (error) {
    console.error('获取帖子列表失败:', error);
    console.error('错误详情:', {
      message: error.message,
      response: error.response,
      status: error.response?.status,
      data: error.response?.data
    });
    
    // 如果是权限错误，给出明确提示
    if (error.response?.status === 403) {
      ElMessage.error('权限不足：需要管理员权限才能访问论坛管理功能');
    } else if (error.response?.status === 401) {
      ElMessage.error('未登录或登录已过期，请重新登录');
    } else {
      ElMessage.error('获取帖子列表失败: ' + error.message);
    }
    
    posts.value = [];
    total.value = 0;
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchPosts()
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

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString()
}

// 查看帖子详情
const viewPost = (post) => {
  if (post.reported) {
    selectedPost.value = post
    reportDialogVisible.value = true
    fetchReports(post.id)
  } else {
    router.push(`/home/forum/post/${post.id}`)
  }
}

// 删除帖子
const deletePost = async (post) => {
  post.deleting = true
  try {
    console.log(`准备删除帖子，ID: ${post.id}`);
    
    // 调用管理员接口删除帖子
    await deleteForumPost(post.id);
    
    // 删除成功后给出提示
    ElMessage({
      type: 'success',
      message: '帖子已成功删除',
      duration: 2000
    });
    
    // 重新加载帖子列表
    fetchPosts();
  } catch (error) {
    console.error('删除帖子失败:', error);
    ElMessage.error('删除帖子失败: ' + (error.message || '未知错误'));
  } finally {
    post.deleting = false
  }
}

// 批准帖子
const approvePost = async (post) => {
  post.approving = true
  try {
    await approvePostAPI(post.id)
    ElMessage.success('帖子已批准')
    post.status = 'APPROVED'
  } catch (error) {
    ElMessage.error('批准帖子失败：' + error.message)
  } finally {
    post.approving = false
  }
}

// 隐藏帖子
const hidePost = async (post) => {
  post.hiding = true
  try {
    await hidePostAPI(post.id)
    ElMessage.success('帖子已隐藏')
    post.status = 'HIDDEN'
  } catch (error) {
    ElMessage.error('隐藏帖子失败：' + error.message)
  } finally {
    post.hiding = false
  }
}

// 删除帖子确认
const deletePostConfirm = (post) => {
  ElMessageBox.confirm('确定要删除此帖子吗？此操作不可逆', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deletePost(post)
  }).catch(() => {
    // 用户取消操作
  })
}

// 获取举报列表
const fetchReports = async (postId) => {
  loadingReports.value = true
  reports.value = []
  
  try {
    const response = await getPostReports(postId)
    if (response && response.data) {
      reports.value = response.data.map(report => ({
        ...report,
        dismissing: false
      }))
    }
  } catch (error) {
    ElMessage.error('获取举报信息失败：' + error.message)
  } finally {
    loadingReports.value = false
  }
}

// 驳回单个举报
const dismissReport = async (report) => {
  report.dismissing = true
  try {
    await dismissReportAPI(report.id)
    ElMessage.success('举报已驳回')
    reports.value = reports.value.filter(r => r.id !== report.id)
    
    // 如果没有更多举报，更新帖子状态
    if (reports.value.length === 0) {
      updatePostReportStatus()
    }
  } catch (error) {
    ElMessage.error('驳回举报失败：' + error.message)
  } finally {
    report.dismissing = false
  }
}

// 批准帖子并驳回所有举报
const approveAndDismissAllReports = async () => {
  if (!selectedPost.value) return
  
  processingReports.value = true
  try {
    // 批准帖子
    await approvePostAPI(selectedPost.value.id)
    
    // 驳回所有举报
    await dismissAllReports(selectedPost.value.id)
    
    ElMessage.success('帖子已批准，所有举报已驳回')
    reportDialogVisible.value = false
    
    // 更新帖子状态
    updatePostReportStatus('APPROVED')
  } catch (error) {
    ElMessage.error('操作失败：' + error.message)
  } finally {
    processingReports.value = false
  }
}

// 隐藏帖子并关闭所有举报
const hideAndCloseAllReports = async () => {
  if (!selectedPost.value) return
  
  processingReports.value = true
  try {
    // 隐藏帖子
    await hidePostAPI(selectedPost.value.id)
    
    // 关闭所有举报
    await dismissAllReports(selectedPost.value.id)
    
    ElMessage.success('帖子已隐藏，所有举报已关闭')
    reportDialogVisible.value = false
    
    // 更新帖子状态
    updatePostReportStatus('HIDDEN')
  } catch (error) {
    ElMessage.error('操作失败：' + error.message)
  } finally {
    processingReports.value = false
  }
}

// 更新列表中的帖子举报状态
const updatePostReportStatus = (newStatus) => {
  if (!selectedPost.value) return
  
  const postIndex = posts.value.findIndex(p => p.id === selectedPost.value.id)
  if (postIndex !== -1) {
    posts.value[postIndex].reported = false
    posts.value[postIndex].reportCount = 0
    
    if (newStatus) {
      posts.value[postIndex].status = newStatus
    }
  }
}

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    'PENDING': 'info',
    'APPROVED': 'success',
    'HIDDEN': 'warning',
    'REPORTED': 'danger'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    'PENDING': '待审核',
    'APPROVED': '已批准',
    'HIDDEN': '已隐藏',
    'REPORTED': '已举报'
  }
  return texts[status] || '未知'
}

// 获取分类名称
const getCategoryName = (categoryValue) => {
  const category = categories.find(c => c.value === categoryValue)
  return category ? category.label : categoryValue
}

// 分类管理
const manageCategories = async () => {
  try {
    const response = await getAllCategories()
    // 响应拦截器已经返回了data部分，所以response就是数组数据
    if (Array.isArray(response)) {
      allCategories.value = response
    } else if (response && response.data) {
      allCategories.value = response.data
    } else {
      allCategories.value = []
    }
    categoryDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取分类列表失败：' + error.message)
  }
}

// 添加分类
const addCategory = () => {
  isEditCategory.value = false
  categoryForm.value = {
    label: '',
    value: ''
  }
  categoryEditDialogVisible.value = true
}

// 编辑分类
const editCategory = (category) => {
  isEditCategory.value = true
  categoryForm.value = { ...category }
  categoryEditDialogVisible.value = true
}

// 删除分类
const deleteCategory = async (category) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该分类吗？相关帖子的分类将被重置为"其他"。',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteCategoryApi(category.value)
    ElMessage.success('删除成功')
    const response = await getAllCategories()
    // 响应拦截器已经返回了data部分，所以response就是数组数据
    if (Array.isArray(response)) {
      allCategories.value = response
    } else if (response && response.data) {
      allCategories.value = response.data
    } else {
      allCategories.value = []
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + error.message)
    }
  }
}

// 提交分类
const submitCategory = async () => {
  if (!categoryFormRef.value) return
  
  await categoryFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEditCategory.value) {
          await updateCategory(categoryForm.value.value, categoryForm.value)
          ElMessage.success('更新成功')
        } else {
          await createCategory(categoryForm.value)
          ElMessage.success('添加成功')
        }
        categoryEditDialogVisible.value = false
        const response = await getAllCategories()
        // 响应拦截器已经返回了data部分，所以response就是数组数据
        if (Array.isArray(response)) {
          allCategories.value = response
        } else if (response && response.data) {
          allCategories.value = response.data
        } else {
          allCategories.value = []
        }
      } catch (error) {
        ElMessage.error(isEditCategory.value ? '更新失败：' : '添加失败：' + error.message)
      }
    }
  })
}

// 模拟帖子数据
const mockPosts = () => {
  const mockData = [
    {
      id: 1,
      title: '如何保持良好的作息习惯，提高学习效率？',
      authorName: '张同学',
      status: 'APPROVED',
      category: 'HEALTH_CONSULTATION',
      createdAt: new Date(Date.now() - 86400000).toISOString(),
      views: 156,
      likes: 28,
      commentCount: 12,
      reported: false,
      reportCount: 0,
      approving: false,
      hiding: false,
      deleting: false
    },
    {
      id: 2,
      title: '大学生常见的颈椎问题如何预防和改善',
      authorName: '李医生',
      status: 'PENDING',
      category: 'MEDICAL_EXPERIENCE',
      createdAt: new Date(Date.now() - 172800000).toISOString(),
      views: 234,
      likes: 45,
      commentCount: 18,
      reported: true,
      reportCount: 2,
      approving: false,
      hiding: false,
      deleting: false
    },
    {
      id: 3,
      title: '校园常见传染病预防指南',
      authorName: '王护士',
      status: 'REPORTED',
      category: 'MEDICAL_NEWS',
      createdAt: new Date(Date.now() - 259200000).toISOString(),
      views: 189,
      likes: 32,
      commentCount: 15,
      reported: true,
      reportCount: 1,
      approving: false,
      hiding: false,
      deleting: false
    }
  ]
  posts.value = mockData
  total.value = mockData.length
}

// 初始化
onMounted(() => {
  // 检查用户是否登录
  const token = localStorage.getItem('token');
  const userInfoStr = localStorage.getItem('userInfo');
  let currentRole = null;
  
  if (userInfoStr) {
    try {
      const userInfo = JSON.parse(userInfoStr);
      currentRole = userInfo.role;
    } catch (e) {
      console.error('解析用户信息失败:', e);
    }
  }
  
  const isLoggedIn = !!token;
  
  console.log('ContentManagement组件挂载，登录状态:', isLoggedIn, 'token存在:', !!token);
  console.log('当前用户角色:', currentRole);
  
  if (isLoggedIn) {
    // 用户已登录，检查是否有管理员权限
    if (currentRole === 'ADMIN') {
      console.log('管理员用户已登录，开始加载论坛管理数据');
      fetchPosts();
    } else {
      console.log('用户已登录但非管理员，显示权限不足提示');
      ElMessage.warning('您没有管理员权限，无法访问论坛管理功能');
      // 可以在这里添加跳转到其他页面的逻辑
      // router.push('/forum');
      mockPosts(); // 为了演示，仍然显示模拟数据
    }
  } else {
    // 用户未登录
    console.log('用户未登录，ContentManagement显示权限不足');
    ElMessage.warning('请先登录管理员账户才能访问论坛管理功能');
    // 显示模拟数据用于界面展示
    mockPosts();
  }
})
</script>

<style lang="scss" scoped>
.content-management {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .search-area {
      display: flex;
      gap: 12px;
      
      .search-input {
        width: 300px;
      }
    }
  }

  .post-list {
    margin: 20px 0;

    .post-stats {
      display: flex;
      gap: 16px;

      .stat-item {
        display: flex;
        align-items: center;
        gap: 4px;
        color: #666;
      }
    }
  }

  .pagination-container {
    display: flex;
    justify-content: center;
    margin-top: 20px;
  }
}

.category-list {
  .dialog-footer {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.post-title-cell {
  display: flex;
  align-items: center;
}

.post-title {
  color: #409EFF;
  cursor: pointer;
  text-decoration: none;
}

.post-title:hover {
  text-decoration: underline;
}

.report-badge {
  margin-left: 8px;
}

.report-dialog-content {
  max-height: 500px;
  overflow-y: auto;
}
</style>