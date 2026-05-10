<template>
  <div class="task-detail">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>任务详情</span>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>
      
      <el-descriptions v-if="task" border :column="2">
        <el-descriptions-item label="任务ID">{{ task.id }}</el-descriptions-item>
        <el-descriptions-item label="任务名称">{{ task.name }}</el-descriptions-item>
        <el-descriptions-item label="流程实例ID">{{ task.processInstanceId }}</el-descriptions-item>
        <el-descriptions-item label="流程定义ID">{{ task.processDefinitionId }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ task.createTime }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ task.assignee }}</el-descriptions-item>
      </el-descriptions>
      
      <el-divider />
      
      <div class="variables-section" v-if="variables && Object.keys(variables).length > 0">
        <h3>流程变量</h3>
        <el-descriptions border :column="2">
          <el-descriptions-item v-for="(value, key) in variables" :key="key" :label="key">
            {{ value }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <el-divider />
      
      <el-card class="handle-card" v-if="task && task.assignee">
        <template #header>
          <span>办理任务</span>
        </template>
        <el-form :model="form" ref="formRef" label-width="80px">
          <el-form-item label="审批意见">
            <el-input v-model="form.comment" type="textarea" :rows="4" placeholder="请输入审批意见" />
          </el-form-item>
          <el-form-item label="审批结果">
            <el-radio-group v-model="form.outcome">
              <el-radio label="agree">同意</el-radio>
              <el-radio label="reject">驳回</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="completeTask" :loading="loading">提交</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const task = ref(null)
const variables = ref({})
const formRef = ref()
const loading = ref(false)
const form = reactive({
  comment: '',
  outcome: 'agree',
  variables: {}
})

const loadData = async () => {
  const taskId = route.params.taskId
  task.value = await request.get(`/task/${taskId}`)
  variables.value = await request.get(`/task/${taskId}/variables`)
}

const completeTask = async () => {
  loading.value = true
  try {
    const result = await request.post('/task/complete', {
      taskId: route.params.taskId,
      comment: form.comment,
      outcome: form.outcome,
      variables: {
        approved: form.outcome === 'agree'
      }
    })
    if (result.success) {
      ElMessage.success('审批完成')
      router.push('/my-tasks')
    }
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.variables-section h3 {
  margin-bottom: 15px;
}

.handle-card {
  margin-top: 20px;
}
</style>
