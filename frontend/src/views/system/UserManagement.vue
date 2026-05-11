<template>
  <div class="user-management">
    <el-card>
      <div class="toolbar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="关键字">
            <el-input v-model="searchForm.keyword" placeholder="搜索用户名/姓名/邮箱" clearable @keyup.enter="handleSearch" style="width: 240px">
              <template #append>
                <el-button icon="Search" @click="handleSearch"></el-button>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增用户</el-button>
          </el-form-item>
        </el-form>
      </div>
      <el-table :data="userList" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="电话" />
        <el-table-column prop="department.name" label="部门" />
        <el-table-column prop="position.name" label="岗位" />
        <el-table-column label="角色">
          <template #default="scope">
            <el-tag v-for="role in scope.row.roles" :key="role.id" type="primary" size="small" style="margin-right: 5px">
              {{ role.name }}
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
        <el-table-column label="操作" width="250">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="success" size="small" @click="handleResetPassword(scope.row)">重置密码</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="form.departmentId" placeholder="请选择部门" style="width: 100%">
            <el-option v-for="dept in departmentList" :key="dept.id" :label="dept.name" :value="dept.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="岗位">
          <el-select v-model="form.positionId" placeholder="请选择岗位" style="width: 100%">
            <el-option v-for="pos in positionList" :key="pos.id" :label="pos.name" :value="pos.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option v-for="role in roleList" :key="role.id" :label="role.name" :value="role.id" />
          </el-select>
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

    <el-dialog v-model="passwordDialogVisible" title="重置密码" width="500px">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="用户名">
          <el-input :value="passwordForm.username" disabled />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePasswordSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const userList = ref([])
const roleList = ref([])
const departmentList = ref([])
const positionList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const passwordDialogVisible = ref(false)
const passwordFormRef = ref()

const searchForm = reactive({
  keyword: ''
})

const form = reactive({
  id: null,
  username: '',
  name: '',
  email: '',
  phone: '',
  departmentId: null,
  positionId: null,
  roleIds: [],
  status: 1
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const passwordForm = reactive({
  userId: null,
  username: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const loadData = async () => {
  const params = searchForm.keyword ? { keyword: searchForm.keyword } : {}
  userList.value = await request.get('/users', { params })
  roleList.value = await request.get('/roles')
  departmentList.value = await request.get('/departments')
  positionList.value = await request.get('/positions')
}

const handleSearch = () => {
  loadData()
}

const handleAdd = () => {
  Object.assign(form, {
    id: null,
    username: '',
    name: '',
    email: '',
    phone: '',
    departmentId: null,
    positionId: null,
    roleIds: [],
    status: 1
  })
  dialogTitle.value = '新增用户'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, {
    id: row.id,
    username: row.username,
    name: row.name,
    email: row.email,
    phone: row.phone,
    departmentId: row.department?.id,
    positionId: row.position?.id,
    roleIds: row.roles?.map(r => r.id) || [],
    status: row.status
  })
  dialogTitle.value = '编辑用户'
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', { type: 'warning' })
    await request.delete(`/users/${row.id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch {}
}

const handleResetPassword = (row) => {
  Object.assign(passwordForm, {
    userId: row.id,
    username: row.username,
    newPassword: '',
    confirmPassword: ''
  })
  passwordDialogVisible.value = true
}

const handlePasswordSubmit = async () => {
  try {
    await passwordFormRef.value.validate()
    await request.put(`/users/${passwordForm.userId}/password`, {
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码重置成功')
    passwordDialogVisible.value = false
  } catch {}
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    if (form.id) {
      await request.put(`/users/${form.id}`, form)
    } else {
      await request.post('/users', form)
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
