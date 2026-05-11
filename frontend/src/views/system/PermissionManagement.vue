<template>
  <div class="permission-management">
    <el-card>
      <div class="toolbar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="关键字">
            <el-input v-model="searchForm.keyword" placeholder="搜索权限编码/名称" clearable @keyup.enter="handleSearch" style="width: 240px">
              <template #append>
                <el-button icon="Search" @click="handleSearch"></el-button>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增权限</el-button>
          </el-form-item>
        </el-form>
      </div>
      <el-table :data="permissionList" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="code" label="权限编码" />
        <el-table-column prop="name" label="权限名称" />
        <el-table-column prop="description" label="描述" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="权限编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入权限编码" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="权限名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入权限名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const permissionList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

const searchForm = reactive({
  keyword: ''
})

const form = reactive({
  id: null,
  code: '',
  name: '',
  description: '',
  status: 1
})

const rules = {
  code: [{ required: true, message: '请输入权限编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入权限名称', trigger: 'blur' }]
}

const loadData = async () => {
  const params = searchForm.keyword ? { keyword: searchForm.keyword } : {}
  permissionList.value = await request.get('/permissions', { params })
}

const handleSearch = () => {
  loadData()
}

const handleAdd = () => {
  Object.assign(form, {
    id: null,
    code: '',
    name: '',
    description: '',
    status: 1
  })
  dialogTitle.value = '新增权限'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, {
    id: row.id,
    code: row.code,
    name: row.name,
    description: row.description,
    status: row.status
  })
  dialogTitle.value = '编辑权限'
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该权限吗？', '提示', { type: 'warning' })
    await request.delete(`/permissions/${row.id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch {}
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    if (form.id) {
      await request.put(`/permissions/${form.id}`, form)
    } else {
      await request.post('/permissions', form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch {}
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 20px;
}
</style>
