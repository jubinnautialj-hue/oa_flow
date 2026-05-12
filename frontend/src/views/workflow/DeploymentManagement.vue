<template>
  <div class="deployment-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>部署管理</span>
          <el-button type="primary" @click="refresh">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <el-table :data="deployments" border v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="部署名称" />
        <el-table-column prop="id" label="部署ID" width="280" />
        <el-table-column label="流程定义">
          <template #default="scope">
            <template v-if="getProcessDefinition(scope.row.id)">
              <div>{{ getProcessDefinition(scope.row.id).name }}</div>
              <div style="color: #909399; font-size: 12px;">
                Key: {{ getProcessDefinition(scope.row.id).key }} | 版本: {{ getProcessDefinition(scope.row.id).version }}
              </div>
            </template>
            <span v-else style="color: #909399">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <template v-if="getProcessDefinition(scope.row.id)">
              <el-tag :type="getProcessDefinition(scope.row.id).suspended ? 'danger' : 'success'">
                {{ getProcessDefinition(scope.row.id).suspended ? '已挂起' : '已激活' }}
              </el-tag>
            </template>
            <span v-else style="color: #909399">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="deploymentTime" label="部署时间" width="200" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <template v-if="getProcessDefinition(scope.row.id)">
              <el-button
                v-if="!getProcessDefinition(scope.row.id).suspended"
                type="warning"
                size="small"
                @click="handleSuspend(scope.row)"
              >
                挂起
              </el-button>
              <el-button
                v-else
                type="success"
                size="small"
                @click="handleActivate(scope.row)"
              >
                激活
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="deployments.length === 0 && !loading" description="暂无部署记录" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const deployments = ref([])
const processDefinitions = ref([])
const loading = ref(false)

const getProcessDefinition = (deploymentId) => {
  return processDefinitions.value.find(pd => pd.deploymentId === deploymentId)
}

const loadDeployments = async () => {
  loading.value = true
  try {
    const [deploymentsRes, definitionsRes] = await Promise.all([
      request.get('/process/deployments'),
      request.get('/process/definitions')
    ])
    deployments.value = deploymentsRes || []
    processDefinitions.value = definitionsRes || []
  } catch (error) {
    console.error('加载部署列表失败:', error)
    ElMessage.error('加载部署列表失败')
  } finally {
    loading.value = false
  }
}

const refresh = () => {
  loadDeployments()
}

const handleSuspend = async (row) => {
  const pd = getProcessDefinition(row.id)
  if (!pd) return
  
  try {
    await ElMessageBox.confirm('确定要挂起该流程定义吗？挂起后将无法发起新流程。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await request.post(`/process/definitions/${pd.id}/suspend`)
    if (res.success) {
      ElMessage.success('挂起成功')
      loadDeployments()
    }
  } catch {}
}

const handleActivate = async (row) => {
  const pd = getProcessDefinition(row.id)
  if (!pd) return
  
  try {
    await ElMessageBox.confirm('确定要激活该流程定义吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    const res = await request.post(`/process/definitions/${pd.id}/activate`)
    if (res.success) {
      ElMessage.success('激活成功')
      loadDeployments()
    }
  } catch {}
}

onMounted(() => {
  loadDeployments()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
