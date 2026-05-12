<template>
  <div class="operation-dispatch">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>运维调度</span>
          <div>
            <el-radio-group v-model="filterStatus" @change="loadProcessInstances" size="small">
              <el-radio-button value="">全部</el-radio-button>
              <el-radio-button value="running">运行中</el-radio-button>
            </el-radio-group>
            <el-button type="primary" @click="loadProcessInstances" class="ml-10" size="small">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="processInstances" border v-loading="loading" style="width: 100%">
        <el-table-column prop="processDefinitionName" label="流程名称" />
        <el-table-column prop="processDefinitionKey" label="流程Key" width="180" />
        <el-table-column prop="id" label="流程实例ID" width="280">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="showDiagram(scope.row)">
              {{ scope.row.id }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="businessKey" label="业务Key" width="150">
          <template #default="scope">
            {{ scope.row.businessKey || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="startUserId" label="发起人" width="120" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180">
          <template #default="scope">
            {{ scope.row.endTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'RUNNING' ? 'success' : 'info'">
              {{ scope.row.status === 'RUNNING' ? '运行中' : '已结束' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="showDiagram(scope.row)">
              流程图
            </el-button>
            <el-button type="warning" size="small" @click="showTasks(scope.row)">
              任务
            </el-button>
            <el-button
              v-if="scope.row.status === 'RUNNING'"
              type="danger"
              size="small"
              @click="handleTerminate(scope.row)"
            >
              终止
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="processInstances.length === 0 && !loading" description="暂无流程实例" />
    </el-card>

    <el-dialog v-model="diagramVisible" title="流程图" width="800px">
      <div class="diagram-container" v-loading="diagramLoading">
        <img v-if="diagramUrl" :src="diagramUrl" alt="流程图" class="diagram-image" />
        <el-empty v-else description="暂无流程图" />
      </div>
    </el-dialog>

    <el-dialog v-model="tasksVisible" title="任务列表" width="1000px">
      <el-table :data="currentTasks" border style="width: 100%">
        <el-table-column prop="id" label="任务ID" width="280" />
        <el-table-column prop="name" label="任务名称" width="150" />
        <el-table-column prop="assignee" label="审核人" width="120">
          <template #default="scope">
            {{ scope.row.assignee || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="450" fixed="right">
          <template #default="scope">
            <el-button type="success" size="small" @click="handleComplete(scope.row)">
              通过
            </el-button>
            <el-button type="danger" size="small" @click="handleReject(scope.row)">
              驳回
            </el-button>
            <el-button type="primary" size="small" @click="showChangeAssigneeDialog(scope.row)">
              变更审核人
            </el-button>
            <el-button type="warning" size="small" @click="showTransferDialog(scope.row)">
              转办
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="completeDialogVisible" title="通过任务" width="500px">
      <el-form :model="completeForm" label-width="80px">
        <el-form-item label="审批意见">
          <el-input v-model="completeForm.comment" type="textarea" :rows="4" placeholder="请输入审批意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitComplete">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectDialogVisible" title="驳回任务" width="500px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="驳回原因">
          <el-input v-model="rejectForm.comment" type="textarea" :rows="4" placeholder="请输入驳回原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="submitReject">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="assigneeDialogVisible" title="变更审核人" width="500px">
      <el-form :model="assigneeForm" label-width="80px">
        <el-form-item label="新审核人">
          <el-select v-model="assigneeForm.assignee" placeholder="请选择用户" filterable style="width: 100%">
            <el-option v-for="user in userList" :key="user.username" :label="`${user.name} (${user.username})`" :value="user.username" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assigneeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAssignee">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="transferDialogVisible" title="转办任务" width="500px">
      <el-form :model="transferForm" label-width="80px">
        <el-form-item label="转办给">
          <el-select v-model="transferForm.assignee" placeholder="请选择用户" filterable style="width: 100%">
            <el-option v-for="user in userList" :key="user.username" :label="`${user.name} (${user.username})`" :value="user.username" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="transferDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitTransfer">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const processInstances = ref([])
const loading = ref(false)
const filterStatus = ref('')

const diagramVisible = ref(false)
const diagramLoading = ref(false)
const diagramUrl = ref('')

const tasksVisible = ref(false)
const currentTasks = ref([])
const currentProcessInstance = ref(null)

const completeDialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const assigneeDialogVisible = ref(false)
const transferDialogVisible = ref(false)

const currentTask = ref(null)
const userList = ref([])

const completeForm = reactive({ comment: '' })
const rejectForm = reactive({ comment: '' })
const assigneeForm = reactive({ assignee: '' })
const transferForm = reactive({ assignee: '' })

const loadProcessInstances = async () => {
  loading.value = true
  try {
    const params = filterStatus.value ? { status: filterStatus.value } : {}
    processInstances.value = await request.get('/operation/process-instances', { params })
  } catch (error) {
    console.error('加载流程实例失败:', error)
    ElMessage.error('加载流程实例失败')
  } finally {
    loading.value = false
  }
}

const loadUsers = async () => {
  try {
    userList.value = await request.get('/users')
  } catch (error) {
    console.error('加载用户列表失败:', error)
  }
}

const showDiagram = async (row) => {
  diagramVisible.value = true
  diagramLoading.value = true
  try {
    if (diagramUrl.value) {
      URL.revokeObjectURL(diagramUrl.value)
    }
    const response = await request.get(`/operation/process-instance/${row.id}/diagram`, {
      responseType: 'blob'
    })
    const blob = new Blob([response], { type: 'image/png' })
    diagramUrl.value = URL.createObjectURL(blob)
  } catch (error) {
    console.error('加载流程图失败:', error)
    ElMessage.error('加载流程图失败')
  } finally {
    diagramLoading.value = false
  }
}

const showTasks = async (row) => {
  currentProcessInstance.value = row
  try {
    currentTasks.value = await request.get(`/operation/process-instance/${row.id}/tasks`)
    tasksVisible.value = true
  } catch (error) {
    console.error('加载任务列表失败:', error)
    ElMessage.error('加载任务列表失败')
  }
}

const handleTerminate = async (row) => {
  try {
    await ElMessageBox.confirm('确定要终止该流程实例吗？终止后流程将无法继续。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await request.delete(`/operation/process-instance/${row.id}`, {
      params: { reason: '运维终止' }
    })
    if (res.success) {
      ElMessage.success('终止成功')
      loadProcessInstances()
    }
  } catch {}
}

const handleComplete = (row) => {
  currentTask.value = row
  completeForm.comment = ''
  completeDialogVisible.value = true
}

const submitComplete = async () => {
  if (!currentTask.value) return
  
  try {
    const res = await request.post(`/operation/task/${currentTask.value.id}/complete`, {
      comment: completeForm.comment,
      outcome: 'agree'
    })
    if (res.success) {
      ElMessage.success('审批通过')
      completeDialogVisible.value = false
      showTasks(currentProcessInstance.value)
      loadProcessInstances()
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleReject = (row) => {
  currentTask.value = row
  rejectForm.comment = ''
  rejectDialogVisible.value = true
}

const submitReject = async () => {
  if (!currentTask.value) return
  
  try {
    const res = await request.post(`/operation/task/${currentTask.value.id}/reject`, {
      comment: rejectForm.comment
    })
    if (res.success) {
      ElMessage.success('驳回成功')
      rejectDialogVisible.value = false
      showTasks(currentProcessInstance.value)
      loadProcessInstances()
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const showChangeAssigneeDialog = (row) => {
  currentTask.value = row
  assigneeForm.assignee = ''
  loadUsers()
  assigneeDialogVisible.value = true
}

const submitAssignee = async () => {
  if (!currentTask.value || !assigneeForm.assignee) {
    ElMessage.warning('请选择新审核人')
    return
  }
  
  try {
    const res = await request.post(`/operation/task/${currentTask.value.id}/assignee`, {
      assignee: assigneeForm.assignee
    })
    if (res.success) {
      ElMessage.success('变更审核人成功')
      assigneeDialogVisible.value = false
      showTasks(currentProcessInstance.value)
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const showTransferDialog = (row) => {
  currentTask.value = row
  transferForm.assignee = ''
  loadUsers()
  transferDialogVisible.value = true
}

const submitTransfer = async () => {
  if (!currentTask.value || !transferForm.assignee) {
    ElMessage.warning('请选择转办人')
    return
  }
  
  try {
    const res = await request.post(`/operation/task/${currentTask.value.id}/transfer`, {
      assignee: transferForm.assignee
    })
    if (res.success) {
      ElMessage.success('转办成功')
      transferDialogVisible.value = false
      showTasks(currentProcessInstance.value)
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

onMounted(() => {
  loadProcessInstances()
  loadUsers()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ml-10 {
  margin-left: 10px;
}

.diagram-container {
  text-align: center;
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.diagram-image {
  max-width: 100%;
  max-height: 600px;
}
</style>
