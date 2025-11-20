<template>
  <div class="department-container">
    <div class="department-header">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="科室名称">
          <el-input v-model="queryParams.name" placeholder="请输入科室名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-button type="primary" @click="handleAdd">新增科室</el-button>
    </div>

    <el-table v-loading="loading" :data="departmentList" border>
      <el-table-column type="index" label="序号" width="50" />
      <el-table-column prop="name" label="科室名称" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleUpdate(row)">编辑</el-button>
          <el-button type="primary" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="queryParams.page"
      v-model:page-size="queryParams.size"
      :page-sizes="[10, 20, 30, 50]"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />

    <!-- 添加/修改科室对话框 -->
    <el-dialog
      :title="dialog.title"
      v-model="dialog.visible"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form
        ref="departmentFormRef"
        :model="departmentForm"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="科室名称" prop="name">
          <el-input v-model="departmentForm.name" placeholder="请输入科室名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="departmentForm.description"
            type="textarea"
            placeholder="请输入描述"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="departmentForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialog.visible = false">取 消</el-button>
          <el-button type="primary" @click="submitForm">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getDepartmentList,
  createDepartment,
  updateDepartment,
  deleteDepartment
} from '@/api/department'

const loading = ref(false)
const total = ref(0)
const departmentList = ref([])
const departmentFormRef = ref()

const queryParams = reactive({
  page: 1,
  size: 10,
  name: '',
  status: undefined
})

const dialog = reactive({
  visible: false,
  title: ''
})

const departmentForm = reactive({
  id: undefined,
  name: '',
  description: '',
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入科室名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 查询科室列表
const getList = async () => {
  try {
    loading.value = true
    const response = await getDepartmentList({
      ...queryParams,
      page: queryParams.page - 1 // 后端页码从0开始
    })
    
    console.log('科室数据响应:', response);
    
    // 根据不同的响应结构处理数据
    if (response && response.content) {
      // 标准分页结构: {content: [], totalElements: 10}
      departmentList.value = response.content;
      total.value = response.totalElements;
    } else if (response && Array.isArray(response)) {
      // 直接返回数组
      departmentList.value = response;
      total.value = response.length;
    } else if (response && response.data) {
      // 嵌套结构: {data: {content: [], totalElements: 10}}
      if (response.data.content) {
        departmentList.value = response.data.content;
        total.value = response.data.totalElements;
      } else if (Array.isArray(response.data)) {
        departmentList.value = response.data;
        total.value = response.data.length;
      } else {
        departmentList.value = [];
        total.value = 0;
      }
    } else {
      departmentList.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error('获取科室列表失败:', error);
    departmentList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

// 查询按钮操作
const handleQuery = () => {
  queryParams.page = 1
  getList()
}

// 重置按钮操作
const resetQuery = () => {
  queryParams.name = ''
  queryParams.status = undefined
  handleQuery()
}

// 新增按钮操作
const handleAdd = () => {
  dialog.title = '添加科室'
  dialog.visible = true
  Object.assign(departmentForm, {
    id: undefined,
    name: '',
    description: '',
    status: 1
  })
}

// 修改按钮操作
const handleUpdate = (row) => {
  dialog.title = '修改科室'
  dialog.visible = true
  // 确保status字段正确转换
  Object.assign(departmentForm, {
    ...row,
    status: row.status === 1 ? 1 : 0
  })
}

// 删除按钮操作
const handleDelete = (row) => {
  ElMessageBox.confirm('确认要删除该科室吗？', '警告', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteDepartment(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('删除科室失败:', error)
    }
  })
}

// 表单提交
const submitForm = async () => {
  if (!departmentFormRef.value) return
  
  try {
    await departmentFormRef.value.validate()
    
    // 确保status是整数类型
    const submitData = {
      ...departmentForm,
      status: departmentForm.status ? 1 : 0
    }
    
    if (departmentForm.id) {
      await updateDepartment(departmentForm.id, submitData)
      ElMessage.success('修改成功')
    } else {
      await createDepartment(submitData)
      ElMessage.success('新增成功')
    }
    
    dialog.visible = false
    getList()
  } catch (error) {
    console.error('提交表单失败:', error)
  }
}

// 对话框关闭时重置表单
const handleDialogClose = () => {
  if (departmentFormRef.value) {
    departmentFormRef.value.resetFields()
  }
}

// 分页大小改变
const handleSizeChange = (val) => {
  queryParams.size = val
  getList()
}

// 页码改变
const handleCurrentChange = (val) => {
  queryParams.page = val
  getList()
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.department-container {
  padding: 20px;

  .department-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }

  :deep(.el-pagination) {
    margin-top: 20px;
    justify-content: flex-end;
  }
}
</style>