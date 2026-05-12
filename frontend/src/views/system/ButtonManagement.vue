<template>
  <div class="button-management">
    <el-card>
      <div class="toolbar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="关键字">
            <el-input v-model="searchForm.keyword" placeholder="搜索按钮编码/名称" clearable @keyup.enter="handleSearch" style="width: 240px">
              <template #append>
                <el-button icon="Search" @click="handleSearch"></el-button>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增按钮</el-button>
          </el-form-item>
        </el-form>
      </div>
      <el-table :data="buttonList" border style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="buttonCode" label="按钮编码" width="150" />
        <el-table-column prop="buttonName" label="按钮名称" width="120" />
        <el-table-column prop="buttonType" label="按钮类型" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.buttonType" :type="getButtonTypeTag(scope.row.buttonType)">
              {{ getButtonTypeLabel(scope.row.buttonType) }}
            </el-tag>
            <span v-else style="color: #909399">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="processDefinitionKey" label="流程Key" width="150">
          <template #default="scope">
            {{ scope.row.processDefinitionKey || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="taskDefinitionKey" label="节点Key" width="150">
          <template #default="scope">
            {{ scope.row.taskDefinitionKey || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="buttonList.length === 0 && !loading" description="暂无按钮数据" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="按钮编码" prop="buttonCode">
          <el-input v-model="form.buttonCode" placeholder="请输入按钮编码（如：agree）" />
        </el-form-item>
        <el-form-item label="按钮名称" prop="buttonName">
          <el-input v-model="form.buttonName" placeholder="请输入按钮名称（如：同意）" />
        </el-form-item>
        <el-form-item label="按钮类型">
          <el-select v-model="form.buttonType" placeholder="请选择按钮类型" clearable style="width: 100%">
            <el-option label="审批通过" value="APPROVE" />
            <el-option label="驳回" value="REJECT" />
            <el-option label="转办" value="TRANSFER" />
            <el-option label="委托" value="DELEGATE" />
            <el-option label="撤回" value="WITHDRAW" />
            <el-option label="终止" value="TERMINATE" />
            <el-option label="提交" value="SUBMIT" />
            <el-option label="自定义" value="CUSTOM" />
          </el-select>
        </el-form-item>
        <el-form-item label="流程Key">
          <el-input v-model="form.processDefinitionKey" placeholder="流程定义Key，为空表示全局适用" />
        </el-form-item>
        <el-form-item label="节点Key">
          <el-input v-model="form.taskDefinitionKey" placeholder="任务节点Key，为空表示所有节点适用" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const buttonList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

const searchForm = reactive({
  keyword: ''
})

const form = reactive({
  id: null,
  buttonCode: '',
  buttonName: '',
  buttonType: '',
  processDefinitionKey: '',
  taskDefinitionKey: '',
  sort: 0,
  status: 1,
  description: ''
})

const rules = {
  buttonCode: [{ required: true, message: '请输入按钮编码', trigger: 'blur' }],
  buttonName: [{ required: true, message: '请输入按钮名称', trigger: 'blur' }]
}

const buttonTypeMap = {
  'APPROVE': { label: '审批通过', tag: 'success' },
  'REJECT': { label: '驳回', tag: 'danger' },
  'TRANSFER': { label: '转办', tag: 'warning' },
  'DELEGATE': { label: '委托', tag: 'info' },
  'WITHDRAW': { label: '撤回', tag: '' },
  'TERMINATE': { label: '终止', tag: 'danger' },
  'SUBMIT': { label: '提交', tag: 'primary' },
  'CUSTOM': { label: '自定义', tag: 'info' }
}

const getButtonTypeLabel = (type) => {
  return buttonTypeMap[type]?.label || type
}

const getButtonTypeTag = (type) => {
  const tag = buttonTypeMap[type]?.tag
  return tag || ''
}

const loadData = async () => {
  loading.value = true
  try {
    const params = searchForm.keyword ? { keyword: searchForm.keyword } : {}
    buttonList.value = await request.get('/process-buttons', { params })
  } catch (error) {
    console.error('加载按钮列表失败:', error)
    ElMessage.error('加载按钮列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  loadData()
}

const handleAdd = () => {
  Object.assign(form, {
    id: null,
    buttonCode: '',
    buttonName: '',
    buttonType: '',
    processDefinitionKey: '',
    taskDefinitionKey: '',
    sort: 0,
    status: 1,
    description: ''
  })
  dialogTitle.value = '新增按钮'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, {
    id: row.id,
    buttonCode: row.buttonCode,
    buttonName: row.buttonName,
    buttonType: row.buttonType || '',
    processDefinitionKey: row.processDefinitionKey || '',
    taskDefinitionKey: row.taskDefinitionKey || '',
    sort: row.sort || 0,
    status: row.status,
    description: row.description || ''
  })
  dialogTitle.value = '编辑按钮'
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该按钮吗？', '提示', { type: 'warning' })
    await request.delete(`/process-buttons/${row.id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch {}
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    if (form.id) {
      await request.put(`/process-buttons/${form.id}`, form)
    } else {
      await request.post('/process-buttons', form)
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
