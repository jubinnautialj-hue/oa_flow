<template>
  <div class="form-design">
    <el-card>
      <div class="toolbar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="关键字">
            <el-input v-model="searchForm.keyword" placeholder="搜索表单名称/Key/描述" clearable @keyup.enter="loadData" style="width: 240px">
              <template #append>
                <el-button icon="Search" @click="loadData"></el-button>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增表单</el-button>
          </el-form-item>
        </el-form>
      </div>
      <el-table :data="modelList" border style="width: 100%">
        <el-table-column prop="name" label="表单名称" />
        <el-table-column prop="formKey" label="表单Key" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.formSchema ? 'success' : 'info'">
              {{ scope.row.formSchema ? '已设计' : '未设计' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleDesign(scope.row)">设计</el-button>
            <el-button type="success" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="modelList.length === 0" description="暂无表单，请点击新增表单按钮创建" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="表单名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入表单名称" />
        </el-form-item>
        <el-form-item label="表单Key" prop="formKey">
          <el-input v-model="form.formKey" placeholder="请输入表单Key（英文）" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const modelList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

const searchForm = reactive({
  keyword: ''
})

const form = reactive({
  id: null,
  name: '',
  formKey: '',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入表单名称', trigger: 'blur' }],
  formKey: [{ required: true, message: '请输入表单Key', trigger: 'blur' }]
}

const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

const loadData = async () => {
  const params = searchForm.keyword ? { keyword: searchForm.keyword } : {}
  try {
    modelList.value = await request.get('/form-model', { params })
  } catch (error) {
    console.error('加载表单模型列表失败:', error)
    modelList.value = []
  }
}

const handleAdd = () => {
  Object.assign(form, {
    id: null,
    name: '',
    formKey: '',
    description: ''
  })
  dialogTitle.value = '新增表单'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, {
    id: row.id,
    name: row.name,
    formKey: row.formKey,
    description: row.description || ''
  })
  dialogTitle.value = '编辑表单'
  dialogVisible.value = true
}

const handleDesign = (row) => {
  router.push(`/form-design/${row.id}`)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该表单吗？', '提示', { type: 'warning' })
    const result = await request.delete(`/form-model/${row.id}`)
    if (result.success) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('删除失败:', e)
    }
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    if (form.id) {
      await request.put(`/form-model/${form.id}`, form)
    } else {
      await request.post('/form-model', form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    if (error && error.message) {
      ElMessage.error(error.message)
    }
  }
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
