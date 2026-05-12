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
        :unique-opened="true"
      >
        <template v-for="menu in menuTree" :key="menu.id">
          <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="String(menu.id)">
            <template #title>
              <el-icon><component :is="menu.icon || 'Menu'" /></el-icon>
              <span>{{ menu.name }}</span>
            </template>
            <el-menu-item v-for="child in menu.children" :key="child.id" :index="getMenuPath(child)">
              <el-icon><component :is="child.icon || 'Document'" /></el-icon>
              <template #title>
                <span>{{ child.name }}</span>
              </template>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="getMenuPath(menu)">
            <el-icon><component :is="menu.icon || 'Document'" /></el-icon>
            <template #title>
              <span>{{ menu.name }}</span>
            </template>
          </el-menu-item>
        </template>
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
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isCollapsed = ref(false)
const menuTree = ref([])

const sidebarWidth = computed(() => {
  return isCollapsed.value ? '64px' : '220px'
})

const currentPageTitle = computed(() => {
  return route.meta?.title || '首页'
})

const getMenuPath = (menu) => {
  if (!menu.path) return '/'
  if (menu.path.startsWith('/')) return menu.path
  return '/' + menu.path
}

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

const loadMenuTree = async () => {
  try {
    menuTree.value = await request.get('/menus/tree')
  } catch (error) {
    console.error('加载菜单失败:', error)
    menuTree.value = []
  }
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
  if (authStore.isAuthenticated) {
    await loadMenuTree()
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

.sidebar-menu :deep(.el-sub-menu__title),
.sidebar-menu :deep(.el-menu-item) {
  color: #bfcbd9;
}

.sidebar-menu :deep(.el-sub-menu__title:hover),
.sidebar-menu :deep(.el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.05);
}

.sidebar-menu :deep(.el-sub-menu .el-menu-item) {
  background-color: #1f2d3d;
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
