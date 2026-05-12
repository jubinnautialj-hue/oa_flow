<template>
  <div class="process-design">
    <el-card>
      <div class="toolbar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="关键字">
            <el-input v-model="searchForm.keyword" placeholder="搜索流程名称/Key/描述" clearable @keyup.enter="loadData" style="width: 240px">
              <template #append>
                <el-button icon="Search" @click="loadData"></el-button>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增流程</el-button>
          </el-form-item>
        </el-form>
      </div>
      <el-table :data="modelList" border style="width: 100%">
        <el-table-column prop="name" label="流程名称" />
        <el-table-column prop="processKey" label="流程Key" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '已部署' : '未部署' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createUser.name" label="创建人" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="380">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleDesign(scope.row)">设计</el-button>
            <el-button type="success" size="small" @click="handleDeploy(scope.row)" v-if="scope.row.status !== 1">部署</el-button>
            <el-button type="warning" size="small" @click="handleUndeploy(scope.row)" v-else>取消部署</el-button>
            <el-button type="info" size="small" @click="handlePermission(scope.row)">权限</el-button>
            <el-dropdown trigger="click" @command="(cmd) => handleMore(cmd, scope.row)">
              <el-button type="default" size="small">更多操作<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">编辑</el-dropdown-item>
                  <el-dropdown-item command="copy">复制</el-dropdown-item>
                  <el-dropdown-item command="export">导出</el-dropdown-item>
                  <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="modelList.length === 0" description="暂无流程模型，请点击新增流程按钮创建" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="流程名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入流程名称" />
        </el-form-item>
        <el-form-item label="流程Key" prop="processKey">
          <el-input v-model="form.processKey" placeholder="请输入流程Key（英文）" :disabled="!!form.id" />
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

    <el-dialog v-model="permissionDialogVisible" title="流程权限设置" width="600px">
      <div class="permission-tabs">
        <el-tabs v-model="permissionTab">
          <el-tab-pane label="可发起用户" name="startUsers">
            <el-checkbox-group v-model="permissionForm.startUsers">
              <el-checkbox v-for="user in userList" :key="user.id" :label="user.id">
                {{ user.name }} ({{ user.username }})
              </el-checkbox>
            </el-checkbox-group>
            <el-empty v-if="userList.length === 0" description="暂无用户" />
          </el-tab-pane>
          <el-tab-pane label="可发起角色" name="startRoles">
            <el-checkbox-group v-model="permissionForm.startRoles">
              <el-checkbox v-for="role in roleList" :key="role.id" :label="role.id">
                {{ role.name }}
              </el-checkbox>
            </el-checkbox-group>
            <el-empty v-if="roleList.length === 0" description="暂无角色" />
          </el-tab-pane>
        </el-tabs>
      </div>
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePermissionSubmit">保存</el-button>
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
const userList = ref([])
const roleList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const permissionDialogVisible = ref(false)
const permissionTab = ref('startUsers')

const searchForm = reactive({
  keyword: ''
})

const form = reactive({
  id: null,
  name: '',
  processKey: '',
  description: ''
})

const permissionForm = reactive({
  modelId: null,
  startUsers: [],
  startRoles: []
})

const rules = {
  name: [{ required: true, message: '请输入流程名称', trigger: 'blur' }],
  processKey: [{ required: true, message: '请输入流程Key', trigger: 'blur' }]
}

const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

const loadData = async () => {
  const params = searchForm.keyword ? { keyword: searchForm.keyword } : {}
  try {
    modelList.value = await request.get('/process-model', { params })
  } catch (error) {
    console.error('加载流程模型列表失败:', error)
    modelList.value = []
  }
}

const loadUsersAndRoles = async () => {
  try {
    userList.value = await request.get('/users')
    roleList.value = await request.get('/roles')
  } catch (error) {
    console.error('加载用户/角色列表失败:', error)
  }
}

const handleAdd = () => {
  Object.assign(form, {
    id: null,
    name: '',
    processKey: '',
    description: ''
  })
  dialogTitle.value = '新增流程'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, {
    id: row.id,
    name: row.name,
    processKey: row.processKey,
    description: row.description || ''
  })
  dialogTitle.value = '编辑流程'
  dialogVisible.value = true
}

const handleDesign = (row) => {
  router.push(`/process-design/${row.id}`)
}

const handleDeploy = async (row) => {
  try {
    await ElMessageBox.confirm('确定要部署该流程吗？', '提示', { type: 'warning' })
    const result = await request.post(`/process-model/${row.id}/deploy`)
    if (result.success) {
      ElMessage.success('部署成功')
      loadData()
    } else {
      ElMessage.error(result.message || '部署失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('部署失败:', e)
    }
  }
}

const handleUndeploy = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消部署该流程吗？', '提示', { type: 'warning' })
    const result = await request.post(`/process-model/${row.id}/undeploy`)
    if (result.success) {
      ElMessage.success('已取消部署')
      loadData()
    } else {
      ElMessage.error(result.message || '取消部署失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('取消部署失败:', e)
    }
  }
}

const handlePermission = (row) => {
  permissionForm.modelId = row.id
  permissionForm.startUsers = []
  permissionForm.startRoles = []
  loadUsersAndRoles()
  permissionDialogVisible.value = true
}

const handlePermissionSubmit = () => {
  ElMessage.success('权限设置成功（演示功能）')
  permissionDialogVisible.value = false
}

const handleCopy = (row) => {
  Object.assign(form, {
    id: null,
    name: row.name + '_副本',
    processKey: row.processKey + '_copy',
    description: row.description || ''
  })
  dialogTitle.value = '复制流程'
  dialogVisible.value = true
}

const handleExport = async (row) => {
  try {
    const model = await request.get(`/process-model/${row.id}`)
    if (model && model.bpmnXml) {
      const blob = new Blob([model.bpmnXml], { type: 'application/xml' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `${model.name || 'process'}.bpmn20.xml`
      a.click()
      URL.revokeObjectURL(url)
      ElMessage.success('导出成功')
    } else {
      ElMessage.warning('该流程暂无设计内容')
    }
  } catch (error) {
    console.error('导出失败:', error)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该流程模型吗？', '提示', { type: 'warning' })
    const result = await request.delete(`/process-model/${row.id}`)
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

const handleMore = (command, row) => {
  switch (command) {
    case 'edit':
      handleEdit(row)
      break
    case 'copy':
      handleCopy(row)
      break
    case 'export':
      handleExport(row)
      break
    case 'delete':
      handleDelete(row)
      break
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    if (form.id) {
      await request.put(`/process-model/${form.id}`, form)
    } else {
      await request.post('/process-model', form)
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

.permission-tabs :deep(.el-tabs__content) {
  min-height: 200px;
  padding: 10px;
}
</style>
