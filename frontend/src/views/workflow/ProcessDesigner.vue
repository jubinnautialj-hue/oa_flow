<template>
  <div class="process-designer">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>流程设计器</span>
          <div>
            <el-button type="primary" @click="saveBpmn">导出XML</el-button>
          </div>
        </div>
      </template>
      
      <div class="designer-tips">
        <el-alert type="info" show-icon>
          <template #title>
            使用说明
          </template>
          <p>由于 bpmn-js 较为复杂，这里提供了一个简化的流程设计方式。</p>
          <p>您可以编辑下方的 BPMN XML 模板来创建流程，也可以直接修改 XML。</p>
        </el-alert>
      </div>
      
      <div class="designer-content">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="可视化设计" name="visual">
            <div class="visual-designer">
              <el-form :model="processForm" label-width="100px">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="流程ID">
                      <el-input v-model="processForm.id" placeholder="leave_approval" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="流程名称">
                      <el-input v-model="processForm.name" placeholder="请假审批流程" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-divider content-position="left">审批节点</el-divider>
                <div v-for="(node, index) in processForm.nodes" :key="index" class="node-item">
                  <el-card class="node-card">
                    <div class="node-header">
                      <span>节点 {{ index + 1 }}</span>
                      <el-button 
                        type="danger" 
                        size="small" 
                        @click="removeNode(index)"
                        :disabled="processForm.nodes.length <= 1"
                      >
                        删除
                      </el-button>
                    </div>
                    <el-form :model="node" label-width="80px">
                      <el-row :gutter="10">
                        <el-col :span="8">
                          <el-form-item label="节点ID">
                            <el-input v-model="node.id" :placeholder="'task_' + (index + 1)" />
                          </el-form-item>
                        </el-col>
                        <el-col :span="8">
                          <el-form-item label="节点名称">
                            <el-input v-model="node.name" :placeholder="'第' + (index + 1) + '级审批'" />
                          </el-form-item>
                        </el-col>
                        <el-col :span="8">
                          <el-form-item label="审批人">
                            <el-input v-model="node.assignee" placeholder="用户ID或变量" />
                          </el-form-item>
                        </el-col>
                      </el-row>
                    </el-form>
                  </el-card>
                </div>
                <el-button type="primary" @click="addNode" size="small" style="margin-top: 10px">
                  <el-icon><Plus /></el-icon> 添加审批节点
                </el-button>
              </el-form>
              <div style="margin-top: 20px">
                <el-button type="success" @click="generateBpmn">生成BPMN XML</el-button>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="XML编辑" name="xml">
            <el-input 
              v-model="bpmnXml" 
              type="textarea" 
              :rows="20" 
              placeholder="BPMN XML内容"
            />
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('visual')
const processForm = reactive({
  id: 'leave_approval',
  name: '请假审批流程',
  nodes: [
    { id: 'manager_approve', name: '部门经理审批', assignee: '${initiator}' },
    { id: 'hr_approve', name: 'HR审批', assignee: 'manager' }
  ]
})

const bpmnXml = ref('')

const addNode = () => {
  const index = processForm.nodes.length + 1
  processForm.nodes.push({
    id: `task_${index}`,
    name: `第${index}级审批`,
    assignee: ''
  })
}

const removeNode = (index) => {
  processForm.nodes.splice(index, 1)
}

const generateBpmn = () => {
  if (!processForm.id) {
    ElMessage.warning('请输入流程ID')
    return
  }
  
  let xml = `<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:flowable="http://flowable.org/bpmn"
  targetNamespace="http://flowable.org/bpmn">
  
  <process id="${processForm.id}" name="${processForm.name}" isExecutable="true">
    <startEvent id="start" name="开始">
      <outgoing>flow_start</outgoing>
    </startEvent>
`
  
  processForm.nodes.forEach((node, index) => {
    const previousId = index === 0 ? 'start' : processForm.nodes[index - 1].id
    const nextId = index === processForm.nodes.length - 1 ? 'end' : processForm.nodes[index + 1].id
    const assignee = node.assignee || '${initiator}'
    
    xml += `
    <userTask id="${node.id}" name="${node.name}" flowable:assignee="${assignee}">
      <incoming>flow_${previousId}_${node.id}</incoming>
      <outgoing>flow_${node.id}_${nextId}</outgoing>
    </userTask>
`
  })
  
  xml += `
    <endEvent id="end" name="结束">
      <incoming>flow_${processForm.nodes[processForm.nodes.length - 1].id}_end</incoming>
    </endEvent>
    
    <sequenceFlow id="flow_start_${processForm.nodes[0].id}" sourceRef="start" targetRef="${processForm.nodes[0].id}" />
`
  
  processForm.nodes.forEach((node, index) => {
    if (index < processForm.nodes.length - 1) {
      xml += `    <sequenceFlow id="flow_${node.id}_${processForm.nodes[index + 1].id}" sourceRef="${node.id}" targetRef="${processForm.nodes[index + 1].id}" />
`
    }
  })
  
  xml += `    <sequenceFlow id="flow_${processForm.nodes[processForm.nodes.length - 1].id}_end" sourceRef="${processForm.nodes[processForm.nodes.length - 1].id}" targetRef="end" />
  </process>
</definitions>`
  
  bpmnXml.value = xml
  activeTab.value = 'xml'
  ElMessage.success('BPMN XML 已生成')
}

const saveBpmn = () => {
  if (!bpmnXml.value) {
    ElMessage.warning('请先生成 XML')
    return
  }
  
  const blob = new Blob([bpmnXml.value], { type: 'application/xml' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${processForm.id || 'process'}.bpmn20.xml`
  a.click()
  URL.revokeObjectURL(url)
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.designer-tips {
  margin-bottom: 20px;
}

.visual-designer {
  padding: 20px 0;
}

.node-item {
  margin-bottom: 15px;
}

.node-card {
  background: #f5f7fa;
}

.node-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-weight: bold;
  color: #409eff;
}
</style>
