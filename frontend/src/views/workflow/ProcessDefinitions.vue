<template>
  <div class="process-definitions">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>流程定义</span>
            </div>
          </template>
          <el-table :data="definitions" border style="width: 100%">
            <el-table-column prop="name" label="名称" />
            <el-table-column prop="key" label="流程Key" />
            <el-table-column prop="version" label="版本" width="80" />
            <el-table-column label="操作" width="180">
              <template #default="scope">
                <el-button type="primary" size="small" @click="startProcess(scope.row.key)">发起</el-button>
                <el-button type="warning" size="small" @click="viewProcess(scope.row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="definitions.length === 0" description="暂无流程定义，请先部署流程" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>流程部署</span>
              <el-button type="primary" link @click="goToDesigner">打开设计器</el-button>
            </div>
          </template>
          <div class="deploy-section">
            <el-form-item label="流程名称">
              <el-input v-model="deployForm.name" placeholder="请输入流程名称" />
            </el-form-item>
            <el-form-item label="BPMN XML">
              <el-input 
                v-model="deployForm.xml" 
                type="textarea" 
                :rows="15" 
                placeholder="请输入BPMN XML内容，或使用设计器设计后复制过来" 
              />
            </el-form-item>
            <el-button type="primary" @click="deployProcess" :loading="deploying">部署流程</el-button>
            <el-button type="success" @click="loadSample" class="ml-10">加载示例</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const definitions = ref([])
const deploying = ref(false)
const deployForm = reactive({
  name: '',
  xml: ''
})

const sampleProcessXml = `<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:flowable="http://flowable.org/bpmn"
  targetNamespace="http://flowable.org/bpmn">
  
  <process id="leave_approval" name="请假审批流程" isExecutable="true">
    <startEvent id="start" name="开始">
      <outgoing>flow1</outgoing>
    </startEvent>
    
    <userTask id="manager_approve" name="部门经理审批" flowable:assignee="\${initiator}">
      <incoming>flow1</incoming>
      <outgoing>flow2</outgoing>
    </userTask>
    
    <userTask id="hr_approve" name="HR审批" flowable:assignee="manager">
      <incoming>flow2</incoming>
      <outgoing>flow3</outgoing>
    </userTask>
    
    <endEvent id="end" name="结束">
      <incoming>flow3</incoming>
    </endEvent>
    
    <sequenceFlow id="flow1" sourceRef="start" targetRef="manager_approve" />
    <sequenceFlow id="flow2" sourceRef="manager_approve" targetRef="hr_approve" />
    <sequenceFlow id="flow3" sourceRef="hr_approve" targetRef="end" />
  </process>
</definitions>`

const loadDefinitions = async () => {
  try {
    const res = await request.get('/process/definitions')
    definitions.value = res || []
  } catch (error) {
    console.error('加载流程定义失败:', error)
    definitions.value = []
  }
}

const loadSample = () => {
  deployForm.name = '请假审批流程'
  deployForm.xml = sampleProcessXml
}

const goToDesigner = () => {
  router.push('/process-designer')
}

const startProcess = (processKey) => {
  router.push(`/start-process/${processKey}`)
}

const viewProcess = (row) => {
  ElMessage.info(`流程定义ID: ${row.id}`)
}

const deployProcess = async () => {
  if (!deployForm.name) {
    ElMessage.warning('请输入流程名称')
    return
  }
  if (!deployForm.xml) {
    ElMessage.warning('请输入BPMN XML')
    return
  }
  deploying.value = true
  try {
    const result = await request.post('/process/deploy/xml', {
      bpmnXml: deployForm.xml,
      processName: deployForm.name
    })
    if (result.success) {
      ElMessage.success('部署成功')
      loadDefinitions()
    }
  } finally {
    deploying.value = false
  }
}

onMounted(() => {
  loadDefinitions()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.deploy-section {
  padding: 10px 0;
}

.ml-10 {
  margin-left: 10px;
}
</style>
