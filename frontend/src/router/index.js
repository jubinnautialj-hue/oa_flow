import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/system/UserManagement.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'roles',
        name: 'Roles',
        component: () => import('@/views/system/RoleManagement.vue'),
        meta: { title: '角色管理', icon: 'UserFilled' }
      },
      {
        path: 'permissions',
        name: 'Permissions',
        component: () => import('@/views/system/PermissionManagement.vue'),
        meta: { title: '权限管理', icon: 'Lock' }
      },
      {
        path: 'departments',
        name: 'Departments',
        component: () => import('@/views/system/DepartmentManagement.vue'),
        meta: { title: '部门管理', icon: 'OfficeBuilding' }
      },
      {
        path: 'positions',
        name: 'Positions',
        component: () => import('@/views/system/PositionManagement.vue'),
        meta: { title: '岗位管理', icon: 'Medal' }
      },
      {
        path: 'process-definitions',
        name: 'ProcessDefinitions',
        component: () => import('@/views/workflow/ProcessDefinitions.vue'),
        meta: { title: '流程定义', icon: 'Collection' }
      },
      {
        path: 'process-designer',
        name: 'ProcessDesigner',
        component: () => import('@/views/workflow/ProcessDesigner.vue'),
        meta: { title: '流程设计器', icon: 'Connection' }
      },
      {
        path: 'my-tasks',
        name: 'MyTasks',
        component: () => import('@/views/workflow/MyTasks.vue'),
        meta: { title: '我的待办', icon: 'Tickets' }
      },
      {
        path: 'my-applications',
        name: 'MyApplications',
        component: () => import('@/views/workflow/MyApplications.vue'),
        meta: { title: '我的申请', icon: 'Document' }
      },
      {
        path: 'task-detail/:taskId',
        name: 'TaskDetail',
        component: () => import('@/views/workflow/TaskDetail.vue'),
        meta: { title: '任务详情', icon: 'Document', hidden: true }
      },
      {
        path: 'start-process/:key',
        name: 'StartProcess',
        component: () => import('@/views/workflow/StartProcess.vue'),
        meta: { title: '发起流程', icon: 'Promotion', hidden: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/system/Profile.vue'),
        meta: { title: '个人中心', icon: 'User', hidden: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  document.title = to.meta.title ? `${to.meta.title} - OA流程审批系统` : 'OA流程审批系统'
  
  if (to.meta.requiresAuth !== false && !authStore.isAuthenticated) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else if (to.path === '/login' && authStore.isAuthenticated) {
    next({ path: '/' })
  } else {
    next()
  }
})

export default router
