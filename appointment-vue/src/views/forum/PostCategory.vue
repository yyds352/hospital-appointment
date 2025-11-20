<template>
  <div class="category-container">
    <div class="page-header">
      <h1>分类管理</h1>
      <el-button type="primary" @click="showAddDialog">添加分类</el-button>
    </div>

    <el-card class="category-list">
      <el-table
        v-loading="loading"
        :data="categoryList"
        style="width: 100%"
      >
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="分类名称" prop="name" />
        <el-table-column label="描述" prop="description" show-overflow-tooltip />
        <el-table-column label="帖子数量" prop="postCount" width="100" />
        <el-table-column label="创建时间" prop="createdAt" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="showEditDialog(row)">编辑</el-button>
            <el-button 
              type="danger" 
              link 
              @click="handleDelete(row)"
              :disabled="row.id === 1"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑分类对话框 -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.title"
      width="500px"
    >
      <el-form
        ref="categoryFormRef"
        :model="categoryForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="分类名称" prop="name">
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
        <div class="dialog-footer">
          <el-button @click="dialog.visible = false">取消</el-button>
          <el-button type="primary" @click="submitCategory">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllCategories, createCategory, updateCategory, deleteCategory } from '@/api/forum'

// 分类列表
const loading = ref(false)
const categoryList = ref([])

// 对话框控制
const dialog = reactive({
  visible: false,
  title: '添加分类',
  isEdit: false
})

// 表单引用
const categoryFormRef = ref(null)

// 分类表单
const categoryForm = reactive({
  id: null,
  name: '',
  description: ''
})

// 表单校验规则
const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '长度不能超过 200 个字符', trigger: 'blur' }
  ]
}

// 日期格式化
const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 显示添加对话框
const showAddDialog = () => {
  dialog.title = '添加分类'
  dialog.isEdit = false
  dialog.visible = true
  
  // 重置表单
  Object.assign(categoryForm, {
    id: null,
    name: '',
    description: ''
  })
  
  // 重置表单验证
  if (categoryFormRef.value) {
    categoryFormRef.value.resetFields()
  }
}

// 显示编辑对话框
const showEditDialog = (row) => {
  dialog.title = '编辑分类'
  dialog.isEdit = true
  dialog.visible = true
  
  // 填充表单数据
  Object.assign(categoryForm, {
    id: row.id,
    name: row.name,
    description: row.description
  })
}

// 获取分类列表
const getList = async () => {
  try {
    loading.value = true
    
    const response = await getAllCategories()
    console.log('获取分类列表响应:', response)
    
    // 响应拦截器已经返回了data部分，所以response就是数组数据
    if (Array.isArray(response)) {
      categoryList.value = response
      console.log('成功获取分类数据:', categoryList.value.length, '条记录')
    } else if (response && response.code === 200) {
      // 如果还是完整的响应对象格式
      categoryList.value = response.data || []
      console.log('成功获取分类数据:', categoryList.value.length, '条记录')
    } else {
      console.error('获取分类列表失败: 响应格式不正确', response)
      ElMessage.error('获取分类列表失败')
      categoryList.value = []
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败')
    categoryList.value = []
  } finally {
    loading.value = false
  }
}

// 提交分类
const submitCategory = () => {
  categoryFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const submitData = {
          name: categoryForm.name.trim(),
          description: categoryForm.description.trim()
        }

        let response
        if (dialog.isEdit) {
          response = await updateCategory(categoryForm.id, submitData)
        } else {
          response = await createCategory(submitData)
        }

        console.log('提交分类响应:', response)
        // 响应拦截器已经处理了数据，如果成功会返回true或数据
        if (response) {
          ElMessage.success(dialog.isEdit ? '更新分类成功' : '添加分类成功')
          dialog.visible = false
          await getList() // 重新获取分类列表
        } else {
          ElMessage.error(dialog.isEdit ? '更新分类失败' : '添加分类失败')
        }
      } catch (error) {
        console.error('保存分类失败:', error)
        ElMessage.error('操作失败: ' + (error.message || '未知错误'))
      }
    } else {
      return false
    }
  })
}

// 删除分类
const handleDelete = (row) => {
  // 默认分类不可删除
  if (row.id === 1) {
    ElMessage.warning('默认分类不可删除')
    return
  }
  
  ElMessageBox.confirm('确定要删除该分类吗？删除后该分类下的帖子将移至默认分类', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await deleteCategory(row.id)
      console.log('删除分类响应:', response)
      // 响应拦截器已经处理了数据，如果成功会返回true或数据
      if (response) {
        ElMessage.success('删除分类成功')
        await getList() // 重新获取分类列表
      } else {
        ElMessage.error('删除分类失败')
      }
    } catch (error) {
      console.error('删除分类失败:', error)
      ElMessage.error('删除失败: ' + (error.message || '未知错误'))
    }
  }).catch(() => {})
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.category-container {
  padding: 20px;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }
  
  .category-list {
    margin-bottom: 20px;
  }
}
</style>