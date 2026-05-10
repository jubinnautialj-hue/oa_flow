<template>
  <div class="start-process">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>发起流程 - {{ processDefinitionKey }}</span>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>
      
      <el-form :model="form" ref="formRef" label-width="100px" style="max-width: 600px">
        <el-form-item label="流程标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入流程标题" />
        </el-form-item>
        <el-form-item label="业务编号">
          <el-input v-model="form.businessKey" placeholder="请输入业务编号（可选）" />
        </el-form-item>
        <el-divider content-position="left">申请内容</el-divider>
        <el-form-item label="申请事由">
          <el-input v-model="form.reason" type="textarea" :rows="4" placeholder="请输入申请事由" />
        </el-form-item>
        <el-form-item label="申请天数">
          <el-input-number v-model="form.days" :min="1" :max="365" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submit" :loading="loading">提交</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const processDefinitionKey = route.params.key
const formRef = ref()
const loading = ref(false)
const form = reactive({
  title: '',
  businessKey: '',
  reason: '',
  days: 1
})

const submit = async () => {
  if (!form.title) {
    ElMessage.warning('请输入流程标题')
    return
  }
  
  loading.value = true
  try {
    const result = await request.post('/process/start', {
      processDefinitionKey,
      businessKey: form.businessKey || Date.now().toString(),
      title: form.title
    })
    if (result.success) {
      ElMessage.success('流程已启动')
      router.push('/my-tasks')
    }
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
