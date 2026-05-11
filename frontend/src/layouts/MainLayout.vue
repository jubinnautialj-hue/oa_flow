<template>
  <el-container class="layout-container">
    <el-aside :width="sidebarWidth" class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="logo">
        <span v-if="!isCollapsed">OA流程审批系统</span>
        <span v-else>OA</span>
      </div>
      <el-menu
        :default-active="$route.path"
        :collapse="isCollapsed"
        router
        class="sidebar-menu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>
            <span>{{ item.title }}</span>
          </template>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container :style="{ marginLeft: sidebarWidth, transition: 'margin-left 0.3s' }">
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleSidebar">
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
          <span class="page-title">{{ currentPageTitle }}</span>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar v-if="authStore.userInfo.avatar" :size="32" :src="authStore.userInfo.avatar" />
              <el-icon v-else><UserFilled /></el-icon>
              <span>{{ authStore.userInfo.name || authStore.userInfo.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isCollapsed = ref(false)

const sidebarWidth = computed(() => {
  return isCollapsed.value ? '64px' : '220px'
})

const menuItems = computed(() => {
  return route.matched[0]?.children?.filter(
    item => !item.meta?.hidden
  ).map(item => ({
    path: item.path,
    title: item.meta?.title || item.name,
    icon: item.meta?.icon || 'Document'
  })) || []
})

const currentPageTitle = computed(() => {
  return route.meta?.title || '首页'
})

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      authStore.logout()
      router.push('/login')
    } catch {}
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

onMounted(async () => {
  if (authStore.isAuthenticated && !authStore.userInfo.id) {
    await authStore.getCurrentUser()
  }
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  z-index: 1000;
  overflow-y: auto;
  overflow-x: hidden;
  transition: width 0.3s;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  border-bottom: 1px solid #1f2d3d;
  white-space: nowrap;
  overflow: hidden;
}

.sidebar.collapsed .logo {
  font-size: 18px;
}

.sidebar-menu {
  border-right: none;
}

.header {
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
  line-height: 60px;
  transition: margin-left 0.3s;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
  padding: 4px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.collapse-btn:hover {
  background-color: #f5f7fa;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #606266;
}

.main-content {
  background-color: #f0f2f5;
  min-height: calc(100vh - 60px);
  padding: 20px;
}
</style>
