<template>
  <div class="profile-page">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <div class="avatar-section">
            <div class="avatar-wrapper">
              <el-avatar :size="120" :src="avatarUrl">
                <el-icon><User /></el-icon>
              </el-avatar>
              <div class="avatar-upload-mask" @click="triggerUpload">
                <el-icon><Camera /></el-icon>
              </div>
            </div>
            <input ref="fileInput" type="file" accept="image/*" style="display: none" @change="handleFileChange" />
            <div class="user-name">{{ userInfo.name || userInfo.username }}</div>
            <div class="user-username">@{{ userInfo.username }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基本信息" name="info">
            <el-card>
              <el-form :model="profileForm" :rules="profileRules" ref="profileFormRef" label-width="100px">
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="profileForm.username" disabled />
                </el-form-item>
                <el-form-item label="姓名" prop="name">
                  <el-input v-model="profileForm.name" placeholder="请输入姓名" />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
                </el-form-item>
                <el-form-item label="电话" prop="phone">
                  <el-input v-model="profileForm.phone" placeholder="请输入电话" />
                </el-form-item>
                <el-form-item label="部门">
                  <el-input :value="userInfo.department?.name || '-' " disabled />
                </el-form-item>
                <el-form-item label="岗位">
                  <el-input :value="userInfo.position?.name || '-' " disabled />
                </el-form-item>
                <el-form-item label="角色">
                  <el-tag v-for="role in userInfo.roles" :key="role.id" type="primary" size="small" style="margin-right: 5px">
                    {{ role.name }}
                  </el-tag>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleSaveProfile">保存</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-tab-pane>
          <el-tab-pane label="修改密码" name="password">
            <el-card>
              <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
                <el-form-item label="原密码" prop="oldPassword">
                  <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleChangePassword">修改密码</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import request from '@/utils/request'

const authStore = useAuthStore()
const userInfo = computed(() => authStore.userInfo)
const fileInput = ref()
const profileFormRef = ref()
const passwordFormRef = ref()
const activeTab = ref('info')

const avatarUrl = computed(() => {
  if (userInfo.value.avatar) {
    let avatar = userInfo.value.avatar
    if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
      return avatar
    }
    if (!avatar.startsWith('/')) {
      avatar = '/' + avatar
    }
    return avatar
  }
  return ''
})

const profileForm = reactive({
  username: '',
  name: '',
  email: '',
  phone: ''
})

const profileRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const passwordForm = reactive({
  oldPassword: '',
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
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const loadProfile = () => {
  profileForm.username = userInfo.value.username || ''
  profileForm.name = userInfo.value.name || ''
  profileForm.email = userInfo.value.email || ''
  profileForm.phone = userInfo.value.phone || ''
}

const triggerUpload = () => {
  fileInput.value.click()
}

const handleFileChange = async (e) => {
  const file = e.target.files[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }

  const formData = new FormData()
  formData.append('file', file)

  try {
    const res = await request.post('/upload/avatar', formData)
    if (res.url) {
      await request.put('/users/profile', { avatar: res.url })
      await authStore.getCurrentUser()
      ElMessage.success('头像上传成功')
    }
  } catch (error) {
    ElMessage.error('头像上传失败')
  }

  e.target.value = ''
}

const handleSaveProfile = async () => {
  try {
    await profileFormRef.value.validate()
    const res = await request.put('/users/profile', {
      name: profileForm.name,
      email: profileForm.email,
      phone: profileForm.phone
    })
    authStore.setUserInfo(res)
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleChangePassword = async () => {
  try {
    await passwordFormRef.value.validate()
    await request.post('/users/change-password', {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error) {
    ElMessage.error('密码修改失败')
  }
}

onMounted(() => {
  if (!authStore.userInfo.id) {
    authStore.getCurrentUser()
  }
  loadProfile()
})
</script>

<style scoped>
.profile-page {
  padding: 0;
}

.avatar-section {
  text-align: center;
  padding: 20px 0;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
}

.avatar-upload-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  display: none;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #fff;
  font-size: 24px;
}

.avatar-wrapper:hover .avatar-upload-mask {
  display: flex;
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  margin-top: 16px;
  color: #303133;
}

.user-username {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}
</style>
