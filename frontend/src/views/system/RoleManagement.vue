<template>
  <div class="role-management">
    <el-card>
      <div class="toolbar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="关键字">
            <el-input v-model="searchForm.keyword" placeholder="搜索角色编码/名称" clearable @keyup.enter="handleSearch" style="width: 240px">
              <template #append>
                <el-button icon="Search" @click="handleSearch"></el-button>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增角色</el-button>
          </el-form-item>
          <el-form-item>
            <el-button type="warning" :disabled="selectedRole.length !== 1" @click="handlePermissionAssign">
              <el-icon><Lock /></el-icon>权限分配
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      <el-table :data="roleList" border style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="code" label="角色编码" />
        <el-table-column prop="name" label="角色名称" />
        <el-table-column prop="description" label="描述" />
        <el-table-column label="权限">
          <template #default="scope">
            <el-tag v-for="perm in scope.row.permissions" :key="perm.id" type="success" size="small" style="margin-right: 5px">
              {{ perm.name }}
            </el-tag>
          </template>
        </el-table-column>
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
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入角色编码" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入角色名称" />
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

    <el-dialog v-model="permissionDialogVisible" :title="`权限分配 - ${currentRole?.name || ''}`" width="800px">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="功能权限" name="function">
          <el-checkbox-group v-model="permissionForm.functionPermissionIds">
            <el-checkbox v-for="perm in functionPermissions" :key="perm.id" :label="perm.id">
              {{ perm.name }} ({{ perm.code }})
            </el-checkbox>
          </el-checkbox-group>
        </el-tab-pane>
        <el-tab-pane label="数据权限" name="data">
          <el-checkbox-group v-model="permissionForm.dataPermissionIds">
            <el-checkbox v-for="perm in dataPermissions" :key="perm.id" :label="perm.id">
              {{ perm.name }} ({{ perm.code }})
            </el-checkbox>
          </el-checkbox-group>
        </el-tab-pane>
        <el-tab-pane label="接口权限" name="interface">
          <el-checkbox-group v-model="permissionForm.interfacePermissionIds">
            <el-checkbox v-for="perm in interfacePermissions" :key="perm.id" :label="perm.id">
              {{ perm.name }} ({{ perm.code }})
            </el-checkbox>
          </el-checkbox-group>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePermissionSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const roleList = ref([])
const permissionList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const selectedRole = ref([])
const permissionDialogVisible = ref(false)
const currentRole = ref(null)
const activeTab = ref('function')

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

const permissionForm = reactive({
  functionPermissionIds: [],
  dataPermissionIds: [],
  interfacePermissionIds: []
})

const rules = {
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

const functionPermissions = computed(() => {
  return permissionList.value.filter(p => p.type === 'FUNCTION' || !p.type)
})

const dataPermissions = computed(() => {
  return permissionList.value.filter(p => p.type === 'DATA')
})

const interfacePermissions = computed(() => {
  return permissionList.value.filter(p => p.type === 'INTERFACE')
})

const loadData = async () => {
  const params = searchForm.keyword ? { keyword: searchForm.keyword } : {}
  roleList.value = await request.get('/roles', { params })
  permissionList.value = await request.get('/permissions')
}

const handleSearch = () => {
  loadData()
}

const handleSelectionChange = (selection) => {
  selectedRole.value = selection
}

const handleAdd = () => {
  Object.assign(form, {
    id: null,
    code: '',
    name: '',
    description: '',
    status: 1
  })
  dialogTitle.value = '新增角色'
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
  dialogTitle.value = '编辑角色'
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该角色吗？', '提示', { type: 'warning' })
    await request.delete(`/roles/${row.id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch {}
}

const handlePermissionAssign = () => {
  if (selectedRole.value.length !== 1) {
    ElMessage.warning('请选择单条数据进行权限分配')
    return
  }
  currentRole.value = selectedRole.value[0]
  
  const rolePermissions = currentRole.value.permissions || []
  permissionForm.functionPermissionIds = rolePermissions
    .filter(p => p.type === 'FUNCTION' || !p.type)
    .map(p => p.id)
  permissionForm.dataPermissionIds = rolePermissions
    .filter(p => p.type === 'DATA')
    .map(p => p.id)
  permissionForm.interfacePermissionIds = rolePermissions
    .filter(p => p.type === 'INTERFACE')
    .map(p => p.id)
  
  activeTab.value = 'function'
  permissionDialogVisible.value = true
}

const handlePermissionSubmit = async () => {
  const allPermissionIds = [
    ...permissionForm.functionPermissionIds,
    ...permissionForm.dataPermissionIds,
    ...permissionForm.interfacePermissionIds
  ]
  
  await request.put(`/roles/${currentRole.value.id}`, {
    permissionIds: allPermissionIds
  })
  
  ElMessage.success('权限分配成功')
  permissionDialogVisible.value = false
  loadData()
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    if (form.id) {
      await request.put(`/roles/${form.id}`, form)
    } else {
      await request.post('/roles', form)
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
