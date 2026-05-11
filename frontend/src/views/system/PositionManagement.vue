<template>
  <div class="position-management">
    <el-card>
      <div class="toolbar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="关键字">
            <el-input v-model="searchForm.keyword" placeholder="搜索岗位名称" clearable @keyup.enter="handleSearch" style="width: 240px">
              <template #append>
                <el-button icon="Search" @click="handleSearch"></el-button>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增岗位</el-button>
          </el-form-item>
        </el-form>
      </div>
      <el-table :data="positionList" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="岗位名称" />
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
        <el-form-item label="岗位名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入岗位名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" placeholder="请输入描述" />
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

const positionList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

const searchForm = reactive({
  keyword: ''
})

const form = reactive({
  id: null,
  name: '',
  description: '',
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }]
}

const loadData = async () => {
  const params = searchForm.keyword ? { keyword: searchForm.keyword } : {}
  positionList.value = await request.get('/positions', { params })
}

const handleSearch = () => {
  loadData()
}

const handleAdd = () => {
  Object.assign(form, {
    id: null,
    name: '',
    description: '',
    status: 1
  })
  dialogTitle.value = '新增岗位'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, {
    id: row.id,
    name: row.name,
    description: row.description,
    status: row.status
  })
  dialogTitle.value = '编辑岗位'
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该岗位吗？', '提示', { type: 'warning' })
    await request.delete(`/positions/${row.id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch {}
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    if (form.id) {
      await request.put(`/positions/${form.id}`, form)
    } else {
      await request.post('/positions', form)
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
