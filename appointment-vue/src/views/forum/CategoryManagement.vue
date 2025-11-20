<template>
  <div class="category-management-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>分类管理</span>
          <el-button type="primary" @click="showCreateDialog">新增分类</el-button>
        </div>
      </template>

      <!-- 分类列表 -->
      <el-table v-loading="loading" :data="categories" style="width: 100%">
        <el-table-column prop="name" label="分类名称" width="180" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑分类对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'create' ? '新增分类' : '编辑分类'"
      width="500px"
    >
      <el-form
        ref="categoryFormRef"
        :model="categoryForm"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="categoryForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入分类描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllCategories, createCategory, updateCategory, deleteCategory } from '@/api/forum'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const categories = ref([])
const dialogVisible = ref(false)
const dialogType = ref('create')
const categoryFormRef = ref(null)

const categoryForm = ref({
  id: undefined,
  name: '',
  description: ''
})

const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在2到50个字符之间', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入分类描述', trigger: 'blur' },
    { max: 200, message: '描述不能超过200个字符', trigger: 'blur' }
  ]
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    loading.value = true
    const response = await getAllCategories()
    console.log('Categories response:', response)
    // 响应拦截器已经返回了data部分，所以response就是数组数据
    if (Array.isArray(response)) {
      categories.value = response
    } else if (response && response.code === 200) {
      // 如果还是完整的响应对象格式
      categories.value = response.data || []
    } else {
      console.error('获取分类列表失败: 响应格式不正确', response)
      ElMessage.error('获取分类列表失败')
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error(error.response?.message || '获取分类列表失败')
  } finally {
    loading.value = false
  }
}

// 显示新增对话框
const showCreateDialog = () => {
  dialogType.value = 'create'
  categoryForm.value = {
    id: undefined,
    name: '',
    description: ''
  }
  dialogVisible.value = true
}

// 显示编辑对话框
const handleEdit = (row) => {
  dialogType.value = 'edit'
  categoryForm.value = {
    id: row.id,
    name: row.name,
    description: row.description
  }
  dialogVisible.value = true
}

// 处理删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个分类吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteCategory(row.id)
    ElMessage.success('删除成功')
    await fetchCategories()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除分类失败:', error)
      ElMessage.error(error.response?.message || '删除失败')
    }
  }
}

// 处理提交
const handleSubmit = async () => {
  if (!categoryFormRef.value) return
  
  try {
    await categoryFormRef.value.validate()
    const submitData = {
      name: categoryForm.value.name.trim(),
      description: categoryForm.value.description.trim()
    }

    let response
    if (dialogType.value === 'create') {
      response = await createCategory(submitData)
    } else {
      response = await updateCategory(categoryForm.value.id, submitData)
    }

    console.log('Submit category response:', response)
    // 响应拦截器已经处理了数据，如果成功会返回true或数据
    if (response) {
      ElMessage.success(dialogType.value === 'create' ? '创建成功' : '更新成功')
      dialogVisible.value = false
      await fetchCategories()
    } else {
      ElMessage.error(dialogType.value === 'create' ? '创建失败' : '更新失败')
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(error.response?.message || '提交失败，请检查表单')
  }
}

onMounted(() => {
  fetchCategories()
})
</script>

<style lang="scss" scoped>
.category-management-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>