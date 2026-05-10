<template>
  <div class="my-tasks">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的待办任务</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="待办任务" name="todo">
          <el-table :data="todoTasks" border style="width: 100%">
            <el-table-column prop="name" label="任务名称" />
            <el-table-column prop="processDefinitionName" label="流程名称" />
            <el-table-column prop="createTime" label="创建时间" width="200" />
            <el-table-column label="操作" width="180">
              <template #default="scope">
                <el-button type="primary" size="small" @click="goToDetail(scope.row.id)">办理</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="todoTasks.length === 0" description="暂无待办任务" />
        </el-tab-pane>
        <el-tab-pane label="待签收任务" name="candidate">
          <el-table :data="candidateTasks" border style="width: 100%">
            <el-table-column prop="name" label="任务名称" />
            <el-table-column prop="processDefinitionName" label="流程名称" />
            <el-table-column prop="createTime" label="创建时间" width="200" />
            <el-table-column label="操作" width="180">
              <template #default="scope">
                <el-button type="success" size="small" @click="claimTask(scope.row.id)">签收</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="candidateTasks.length === 0" description="暂无待签收任务" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const activeTab = ref('todo')
const todoTasks = ref([])
const candidateTasks = ref([])

const loadTodoTasks = async () => {
  todoTasks.value = await request.get('/task/todo') || []
}

const loadCandidateTasks = async () => {
  candidateTasks.value = await request.get('/task/candidate') || []
}

const claimTask = async (taskId) => {
  const result = await request.post(`/task/${taskId}/claim`)
  if (result.success) {
    ElMessage.success('签收成功')
    loadCandidateTasks()
    loadTodoTasks()
  }
}

const goToDetail = (taskId) => {
  router.push(`/task-detail/${taskId}`)
}

onMounted(() => {
  loadTodoTasks()
  loadCandidateTasks()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
