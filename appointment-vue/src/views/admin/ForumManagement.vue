<template>
  <div class="admin-forum-management">
    <a-page-header
      title="论坛内容管理"
      :breadcrumb="{ routes: breadcrumbRoutes }"
      class="page-header"
    />
    
    <a-tabs v-model:activeKey="activeTabKey">
      <a-tab-pane key="posts" tab="帖子管理">
        <a-card :loading="postsLoading">
          <a-form layout="inline" class="filter-form">
            <a-form-item label="关键词">
              <a-input v-model:value="postsFilter.keyword" placeholder="搜索标题/内容" />
            </a-form-item>
            <a-form-item label="分类">
              <a-select
                v-model:value="postsFilter.categoryId"
                placeholder="选择分类"
                style="width: 200px"
                allow-clear
              >
                <a-select-option v-for="category in categories" :key="category.id" :value="category.id">
                  {{ category.name }}
                </a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="searchPosts">
                <template #icon><SearchOutlined /></template>
                搜索
              </a-button>
              <a-button style="margin-left: 8px" @click="resetPostsFilter">
                <template #icon><ReloadOutlined /></template>
                重置
              </a-button>
            </a-form-item>
          </a-form>
          
          <a-table
            :columns="postsColumns"
            :data-source="posts"
            :pagination="postsPagination"
            row-key="id"
            :scroll="{ x: 1500 }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'title'">
                <router-link :to="{ name: 'ForumPostDetail', params: { id: record.id } }">
                  {{ record.title }}
                </router-link>
              </template>
              <template v-else-if="column.key === 'authorName'">
                {{ record.authorName || '未知用户' }}
              </template>
              <template v-else-if="column.key === 'categoryName'">
                {{ record.categoryName || '未分类' }}
              </template>
              <template v-else-if="column.key === 'createdAt'">
                {{ formatDateTime(record.createdAt) }}
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space>
                  <a @click="viewPostComments(record.id)" title="查看评论">
                    <CommentOutlined />
                  </a>
                  <a-popconfirm
                    title="确定要删除这篇帖子吗？"
                    ok-text="确定"
                    cancel-text="取消"
                    @confirm="deletePost(record.id)"
                  >
                    <a><DeleteOutlined /></a>
                  </a-popconfirm>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-tab-pane>
      
      <a-tab-pane key="comments" tab="评论管理">
        <a-card :loading="commentsLoading">
          <a-form layout="inline" class="filter-form">
            <a-form-item label="帖子ID">
              <a-input-number v-model:value="commentsFilter.postId" placeholder="帖子ID" style="width: 200px" />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="searchComments">
                <template #icon><SearchOutlined /></template>
                搜索
              </a-button>
              <a-button style="margin-left: 8px" @click="resetCommentsFilter">
                <template #icon><ReloadOutlined /></template>
                重置
              </a-button>
            </a-form-item>
          </a-form>
          
          <a-table
            :columns="commentsColumns"
            :data-source="comments"
            :pagination="commentsPagination"
            row-key="id"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'content'">
                <div class="comment-content">{{ record.content }}</div>
              </template>
              <template v-else-if="column.key === 'postTitle'">
                <router-link :to="{ name: 'ForumPostDetail', params: { id: record.postId } }">
                  查看帖子
                </router-link>
              </template>
              <template v-else-if="column.key === 'authorName'">
                {{ record.authorName || '未知用户' }}
              </template>
              <template v-else-if="column.key === 'createdAt'">
                {{ formatDateTime(record.createdAt) }}
              </template>
              <template v-else-if="column.key === 'action'">
                <a-popconfirm
                  title="确定要删除这条评论吗？"
                  ok-text="确定"
                  cancel-text="取消"
                  @confirm="deleteComment(record.id)"
                >
                  <a><DeleteOutlined /></a>
                </a-popconfirm>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-tab-pane>
      
      <a-tab-pane key="categories" tab="分类管理">
        <a-card :loading="categoriesLoading">
          <div class="table-operations">
            <a-button type="primary" @click="showCategoryModal(null)">
              <template #icon><PlusOutlined /></template>
              添加分类
            </a-button>
          </div>
          
          <a-table
            :columns="categoriesColumns"
            :data-source="categories"
            :pagination="false"
            row-key="id"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'createdAt'">
                {{ formatDateTime(record.createdAt) }}
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space>
                  <a @click="showCategoryModal(record)">
                    <EditOutlined />
                  </a>
                  <a-popconfirm
                    title="确定要删除这个分类吗？"
                    ok-text="确定"
                    cancel-text="取消"
                    @confirm="deleteCategory(record.id)"
                  >
                    <a><DeleteOutlined /></a>
                  </a-popconfirm>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-tab-pane>
    </a-tabs>
    
    <!-- 分类编辑对话框 -->
    <a-modal
      v-model:visible="categoryModal.visible"
      :title="categoryModal.isEdit ? '编辑分类' : '添加分类'"
      @ok="saveCategoryForm"
      :confirm-loading="categoryModal.loading"
    >
      <a-form
        :model="categoryForm"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 20 }"
      >
        <a-form-item label="名称" name="name">
          <a-input v-model:value="categoryForm.name" placeholder="分类名称" />
        </a-form-item>
        <a-form-item label="描述" name="description">
          <a-textarea v-model:value="categoryForm.description" placeholder="分类描述" :rows="4" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script>
import { 
  SearchOutlined, 
  ReloadOutlined, 
  DeleteOutlined, 
  EditOutlined, 
  CommentOutlined, 
  PlusOutlined 
} from '@ant-design/icons-vue';
import { formatDateTime } from '@/utils/dateUtils';

export default {
  name: 'ForumManagement',
  components: {
    SearchOutlined,
    ReloadOutlined,
    DeleteOutlined,
    EditOutlined,
    CommentOutlined,
    PlusOutlined
  },
  data() {
    return {
      // 面包屑导航
      breadcrumbRoutes: [
        { path: '/', breadcrumbName: '首页' },
        { path: '/admin', breadcrumbName: '管理中心' },
        { path: '/admin/forum', breadcrumbName: '论坛管理' },
      ],
      
      // 标签页
      activeTabKey: 'posts',
      
      // 帖子管理
      postsLoading: false,
      posts: [],
      postsFilter: {
        keyword: '',
        categoryId: undefined,
      },
      postsPagination: {
        current: 1,
        pageSize: 10,
        total: 0,
        onChange: this.handlePostsPageChange,
      },
      postsColumns: [
        { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
        { title: '标题', dataIndex: 'title', key: 'title', width: 300 },
        { title: '作者', dataIndex: 'authorName', key: 'authorName', width: 120 },
        { title: '分类', dataIndex: 'categoryName', key: 'categoryName', width: 120 },
        { title: '浏览数', dataIndex: 'views', key: 'views', width: 100 },
        { title: '点赞数', dataIndex: 'likes', key: 'likes', width: 100 },
        { title: '评论数', dataIndex: 'commentCount', key: 'commentCount', width: 100 },
        { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 180 },
        { title: '操作', key: 'action', fixed: 'right', width: 120 },
      ],
      
      // 评论管理
      commentsLoading: false,
      comments: [],
      commentsFilter: {
        postId: undefined,
      },
      commentsPagination: {
        current: 1,
        pageSize: 10,
        total: 0,
        onChange: this.handleCommentsPageChange,
      },
      commentsColumns: [
        { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
        { title: '内容', dataIndex: 'content', key: 'content', ellipsis: true },
        { title: '帖子', dataIndex: 'postId', key: 'postTitle', width: 100 },
        { title: '作者', dataIndex: 'authorName', key: 'authorName', width: 120 },
        { title: '点赞数', dataIndex: 'likes', key: 'likes', width: 100 },
        { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 180 },
        { title: '操作', key: 'action', width: 100 },
      ],
      
      // 分类管理
      categoriesLoading: false,
      categories: [],
      categoriesColumns: [
        { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
        { title: '名称', dataIndex: 'name', key: 'name' },
        { title: '描述', dataIndex: 'description', key: 'description', ellipsis: true },
        { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 180 },
        { title: '操作', key: 'action', width: 120 },
      ],
      
      // 分类编辑对话框
      categoryModal: {
        visible: false,
        isEdit: false,
        loading: false,
      },
      categoryForm: {
        id: null,
        name: '',
        description: '',
      },
    };
  },
  created() {
    this.loadCategories();
    this.loadPosts();
  },
  watch: {
    activeTabKey(newKey) {
      if (newKey === 'posts' && this.posts.length === 0) {
        this.loadPosts();
      } else if (newKey === 'comments' && this.comments.length === 0) {
        this.loadComments();
      } else if (newKey === 'categories' && this.categories.length === 0) {
        this.loadCategories();
      }
    }
  },
  methods: {
    // 工具方法
    formatDateTime,
    
    // 帖子管理方法
    async loadPosts() {
      this.postsLoading = true;
      try {
        const params = {
          page: this.postsPagination.current - 1,
          size: this.postsPagination.pageSize,
          ...this.postsFilter
        };
        
        const res = await this.$http.get('/api/admin/forum/posts', { params });
        
        if (res && res.data) {
          this.posts = res.data.content || [];
          this.postsPagination.total = res.data.totalElements || 0;
        }
      } catch (error) {
        console.error('获取帖子列表失败', error);
        this.$message.error('获取帖子列表失败，请稍后重试');
      } finally {
        this.postsLoading = false;
      }
    },
    
    async deletePost(id) {
      try {
        await this.$http.delete(`/api/admin/forum/posts/${id}`);
        this.$message.success('删除成功');
        this.loadPosts();
      } catch (error) {
        console.error('删除帖子失败', error);
        this.$message.error('删除帖子失败，请稍后重试');
      }
    },
    
    viewPostComments(postId) {
      this.activeTabKey = 'comments';
      this.commentsFilter.postId = postId;
      this.searchComments();
    },
    
    searchPosts() {
      this.postsPagination.current = 1;
      this.loadPosts();
    },
    
    resetPostsFilter() {
      this.postsFilter = {
        keyword: '',
        categoryId: undefined,
      };
      this.searchPosts();
    },
    
    handlePostsPageChange(page) {
      this.postsPagination.current = page;
      this.loadPosts();
    },
    
    // 评论管理方法
    async loadComments() {
      this.commentsLoading = true;
      try {
        const params = {
          page: this.commentsPagination.current - 1,
          size: this.commentsPagination.pageSize,
          ...this.commentsFilter
        };
        
        const res = await this.$http.get('/api/admin/forum/comments', { params });
        
        if (res && res.data) {
          this.comments = res.data.content || [];
          this.commentsPagination.total = res.data.totalElements || 0;
        }
      } catch (error) {
        console.error('获取评论列表失败', error);
        this.$message.error('获取评论列表失败，请稍后重试');
      } finally {
        this.commentsLoading = false;
      }
    },
    
    async deleteComment(id) {
      try {
        await this.$http.delete(`/api/admin/forum/comments/${id}`);
        this.$message.success('删除成功');
        this.loadComments();
      } catch (error) {
        console.error('删除评论失败', error);
        this.$message.error('删除评论失败，请稍后重试');
      }
    },
    
    searchComments() {
      this.commentsPagination.current = 1;
      this.loadComments();
    },
    
    resetCommentsFilter() {
      this.commentsFilter = {
        postId: undefined,
      };
      this.searchComments();
    },
    
    handleCommentsPageChange(page) {
      this.commentsPagination.current = page;
      this.loadComments();
    },
    
    // 分类管理方法
    async loadCategories() {
      this.categoriesLoading = true;
      try {
        const res = await this.$http.get('/api/admin/forum/categories');
        if (res && res.data) {
          this.categories = res.data || [];
        }
      } catch (error) {
        console.error('获取分类列表失败', error);
        this.$message.error('获取分类列表失败，请稍后重试');
      } finally {
        this.categoriesLoading = false;
      }
    },
    
    showCategoryModal(category) {
      if (category) {
        // 编辑分类
        this.categoryModal.isEdit = true;
        this.categoryForm = { ...category };
      } else {
        // 添加分类
        this.categoryModal.isEdit = false;
        this.categoryForm = {
          id: null,
          name: '',
          description: '',
        };
      }
      this.categoryModal.visible = true;
    },
    
    async saveCategoryForm() {
      if (!this.categoryForm.name) {
        this.$message.warning('请输入分类名称');
        return;
      }
      
      this.categoryModal.loading = true;
      try {
        if (this.categoryModal.isEdit) {
          // 更新分类
          await this.$http.put(`/api/admin/forum/categories/${this.categoryForm.id}`, this.categoryForm);
          this.$message.success('更新成功');
        } else {
          // 创建分类
          await this.$http.post('/api/admin/forum/categories', this.categoryForm);
          this.$message.success('创建成功');
        }
        
        this.categoryModal.visible = false;
        this.loadCategories();
      } catch (error) {
        console.error('保存分类失败', error);
        this.$message.error('操作失败，请稍后重试');
      } finally {
        this.categoryModal.loading = false;
      }
    },
    
    async deleteCategory(id) {
      try {
        await this.$http.delete(`/api/admin/forum/categories/${id}`);
        this.$message.success('删除成功');
        this.loadCategories();
      } catch (error) {
        console.error('删除分类失败', error);
        this.$message.error('删除分类失败，请稍后重试');
      }
    },
  },
};
</script>

<style scoped>
.admin-forum-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.filter-form {
  margin-bottom: 20px;
}

.table-operations {
  margin-bottom: 16px;
}

.comment-content {
  max-width: 300px;
  white-space: normal;
  word-break: break-all;
}
</style> 