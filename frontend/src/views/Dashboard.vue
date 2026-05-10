<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statistics" :key="stat.title">
        <el-card class="stat-card" :body-style="{ padding: '20px' }">
          <div class="stat-content">
            <div class="stat-icon" :style="{ background: stat.color }">
              <el-icon :size="32"><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-title">{{ stat.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>快速入口</span>
            </div>
          </template>
          <el-row :gutter="20">
            <el-col :span="8" v-for="item in quickActions" :key="item.title">
              <el-card class="quick-card" :body-style="{ padding: '20px', textAlign: 'center' }" @click="goToPage(item.path)">
                <el-icon :size="40" color="#409eff"><component :is="item.icon" /></el-icon>
                <div class="quick-title">{{ item.title }}</div>
              </el-card>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近待办</span>
              <el-button type="primary" link @click="goToPage('/my-tasks')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="recentTasks" border style="width: 100%">
            <el-table-column prop="name" label="任务名称" />
            <el-table-column prop="createTime" label="创建时间" width="200" />
          </el-table>
          <el-empty v-if="recentTasks.length === 0" description="暂无待办任务" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()

const statistics = ref([
  { title: '待办任务', value: 0, icon: 'Tickets', color: '#409eff' },
  { title: '我的申请', value: 0, icon: 'Document', color: '#67c23a' },
  { title: '流程定义', value: 0, icon: 'Collection', color: '#e6a23c' },
  { title: '待签收任务', value: 0, icon: 'Message', color: '#f56c6c' }
])

const quickActions = [
  { title: '发起流程', icon: 'Promotion', path: '/process-definitions' },
  { title: '我的待办', icon: 'Tickets', path: '/my-tasks' },
  { title: '流程设计', icon: 'Connection', path: '/process-designer' },
  { title: '用户管理', icon: 'User', path: '/users' },
  { title: '角色管理', icon: 'UserFilled', path: '/roles' },
  { title: '部门管理', icon: 'OfficeBuilding', path: '/departments' }
]

const recentTasks = ref([])

const goToPage = (path) => {
  router.push(path)
}

const loadStatistics = async () => {
  try {
    const todoTasks = await request.get('/task/todo')
    statistics.value[0].value = todoTasks.length || 0
    recentTasks.value = (todoTasks || []).slice(0, 5)
    
    const candidateTasks = await request.get('/task/candidate')
    statistics.value[3].value = candidateTasks.length || 0
    
    const definitions = await request.get('/process/definitions')
    statistics.value[2].value = definitions.length || 0
    
    const historyProcesses = await request.get('/task/history/processes')
    statistics.value[1].value = historyProcesses.length || 0
  } catch (e) {
    console.error('加载统计数据失败', e)
  }
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.mt-20 {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.quick-card {
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 10px;
}

.quick-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.quick-title {
  margin-top: 10px;
  font-size: 14px;
  color: #606266;
}
</style>
