<template>
  <div class="process-designer-page">
    <div class="page-header">
      <div class="header-left">
        <el-button link @click="goBack" style="padding: 0; margin-right: 10px">
          <el-icon><ArrowLeft /></el-icon> 返回
        </el-button>
        <h2>{{ processName || '流程设计器' }}</h2>
      </div>
      <div class="header-actions">
        <el-button @click="handleSave" :disabled="!modelerReady">保存</el-button>
        <el-button type="primary" @click="handleDeploy" :disabled="!modelerReady">部署</el-button>
        <el-button type="success" @click="handleExport" :disabled="!modelerReady">导出</el-button>
        <el-button @click="handleZoomIn">放大</el-button>
        <el-button @click="handleZoomOut">缩小</el-button>
        <el-button @click="handleZoomReset">重置</el-button>
      </div>
    </div>

    <div class="page-content">
      <div class="canvas-section">
        <div ref="bpmnWrapper" class="bpmn-canvas">
          <div v-if="!modelerReady && !hasError" class="loading-mask">
            <el-icon class="loading-icon is-loading"><Loading /></el-icon>
            <span>正在加载流程设计器...</span>
          </div>
          <div v-if="hasError" class="error-mask">
            <el-icon class="error-icon"><Warning /></el-icon>
            <div class="error-info">
              <p>{{ errorMessage }}</p>
              <el-button type="primary" size="small" @click="retryInitialize">重试</el-button>
            </div>
          </div>
        </div>
      </div>

      <div class="properties-section">
        <el-card shadow="never">
          <template #header>
            <span>元素属性</span>
          </template>
          <div v-if="!selectedEl" class="empty-state">
            <el-empty description="点击画布上的元素查看属性" />
          </div>
          <el-form v-else :model="currentProps" label-width="70px">
            <el-form-item label="类型">
              <el-tag>{{ currentType }}</el-tag>
            </el-form-item>
            <el-form-item label="ID">
              <el-input v-model="currentProps.id" :disabled="!canEdit" @change="updateId" />
            </el-form-item>
            <el-form-item label="名称">
              <el-input v-model="currentProps.name" @change="updateName" />
            </el-form-item>
            <template v-if="isUserTask">
              <el-form-item label="审批人">
                <el-select v-model="currentProps.assignee" placeholder="选择审批人" style="width: 100%" @change="updateUserTask">
                  <el-option label="流程发起人" value="\${initiator}" />
                  <el-option v-for="u in userList" :key="u.username" :label="u.name" :value="u.username" />
                </el-select>
              </el-form-item>
              <el-form-item label="表单Key">
                <el-select v-model="currentProps.formKey" placeholder="选择表单" style="width: 100%" @change="updateUserTask" filterable>
                  <el-option v-for="f in formList" :key="f.formKey" :label="f.name" :value="f.formKey" />
                </el-select>
              </el-form-item>
            </template>
            <template v-if="isExclusiveGateway">
              <el-form-item label="默认流转">
                <el-input v-model="currentProps.default" placeholder="目标节点ID" @change="updateFlowProps" />
              </el-form-item>
            </template>
            <template v-if="isSequenceFlow">
              <el-form-item label="条件表达式">
                <el-input v-model="currentProps.conditionExpression" type="textarea" :rows="3" placeholder="如: \${days > 3}" @change="updateFlowProps" />
              </el-form-item>
            </template>
            <template v-if="isServiceTask">
              <el-form-item label="类名">
                <el-input v-model="currentProps.class" placeholder="Java类完整路径" @change="updateServiceTask" />
              </el-form-item>
              <el-form-item label="表达式">
                <el-input v-model="currentProps.expression" placeholder="Delegate表达式" @change="updateServiceTask" />
              </el-form-item>
            </template>
            <template v-if="isScriptTask">
              <el-form-item label="脚本格式">
                <el-select v-model="currentProps.scriptFormat" placeholder="选择脚本格式" style="width: 100%" @change="updateScriptTask">
                  <el-option label="JavaScript" value="javascript" />
                  <el-option label="Groovy" value="groovy" />
                  <el-option label="Python" value="python" />
                </el-select>
              </el-form-item>
              <el-form-item label="脚本">
                <el-input v-model="currentProps.script" type="textarea" :rows="5" placeholder="输入脚本内容" @change="updateScriptTask" />
              </el-form-item>
            </template>
          </el-form>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, Warning, ArrowLeft } from '@element-plus/icons-vue'
import request from '@/utils/request'

import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css'
import './bpmn-custom.css'

const route = useRoute()
const router = useRouter()
const modelId = ref(route.params.id)

const bpmnWrapper = ref(null)
const processName = ref('新流程')
const modelerReady = ref(false)
const hasError = ref(false)
const errorMessage = ref('')
const selectedEl = ref(null)
const userList = ref([])
const formList = ref([])

let bpmnInstance = null
let eventList = []

const currentProps = reactive({
  id: '',
  name: '',
  assignee: '',
  formKey: '',
  default: '',
  conditionExpression: '',
  class: '',
  expression: '',
  scriptFormat: '',
  script: ''
})

const canEdit = ref(false)
const isUserTask = ref(false)
const isExclusiveGateway = ref(false)
const isSequenceFlow = ref(false)
const isServiceTask = ref(false)
const isScriptTask = ref(false)

const defaultBpmnXml = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
  xmlns:flowable="http://flowable.org/bpmn"
  id="Definitions_1"
  targetNamespace="http://flowable.org/bpmn">
  <bpmn:process id="Process_1" name="新流程" isExecutable="true">
    <bpmn:startEvent id="Start_1" name="开始">
      <bpmn:outgoing>Flow_1</bpmn:outgoing>
    </bpmn:startEvent>
    <bpmn:userTask id="Task_1" name="审批任务">
      <bpmn:incoming>Flow_1</bpmn:incoming>
      <bpmn:outgoing>Flow_2</bpmn:outgoing>
    </bpmn:userTask>
    <bpmn:endEvent id="End_1" name="结束">
      <bpmn:incoming>Flow_2</bpmn:incoming>
    </bpmn:endEvent>
    <bpmn:sequenceFlow id="Flow_1" sourceRef="Start_1" targetRef="Task_1" />
    <bpmn:sequenceFlow id="Flow_2" sourceRef="Task_1" targetRef="End_1" />
  </bpmn:process>
  <bpmndi:BPMNDiagram id="Diagram_1">
    <bpmndi:BPMNPlane id="Plane_1" bpmnElement="Process_1">
      <bpmndi:BPMNShape id="Start_1_Shape" bpmnElement="Start_1">
        <dc:Bounds x="180" y="120" width="36" height="36" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Task_1_Shape" bpmnElement="Task_1">
        <dc:Bounds x="290" y="100" width="100" height="80" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="End_1_Shape" bpmnElement="End_1">
        <dc:Bounds x="460" y="120" width="36" height="36" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="Flow_1_Edge" bpmnElement="Flow_1">
        <di:waypoint x="216" y="138" />
        <di:waypoint x="290" y="140" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_2_Edge" bpmnElement="Flow_2">
        <di:waypoint x="390" y="140" />
        <di:waypoint x="460" y="138" />
      </bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`

const typeNames = {
  'bpmn:StartEvent': '开始事件',
  'bpmn:EndEvent': '结束事件',
  'bpmn:UserTask': '用户任务',
  'bpmn:ServiceTask': '服务任务',
  'bpmn:ScriptTask': '脚本任务',
  'bpmn:ExclusiveGateway': '排他网关',
  'bpmn:ParallelGateway': '并行网关',
  'bpmn:InclusiveGateway': '包容网关',
  'bpmn:SequenceFlow': '连接线',
  'bpmn:Process': '流程'
}

const currentType = computed(() => {
  if (!selectedEl.value || !selectedEl.value.businessObject) return ''
  const type = selectedEl.value.businessObject.$type
  return typeNames[type] || '其他'
})

const loadUsers = async () => {
  try {
    userList.value = await request.get('/users')
  } catch (e) {
    console.warn('加载用户列表失败:', e)
  }
}

const loadForms = async () => {
  try {
    formList.value = await request.get('/form-model')
  } catch (e) {
    console.warn('加载表单列表失败:', e)
  }
}

const loadModel = async () => {
  if (!modelId.value) return
  try {
    const model = await request.get(`/process-model/${modelId.value}`)
    if (model) {
      processName.value = model.name
      if (model.bpmnXml && model.bpmnXml.trim()) {
        return model.bpmnXml
      }
    }
  } catch (e) {
    console.warn('加载流程模型失败:', e)
  }
  return null
}

const goBack = () => {
  router.push('/process-design')
}

const clearEvents = () => {
  eventList.forEach(item => {
    try {
      item.bus.off(item.event, item.handler)
    } catch (e) {
      console.warn('移除事件失败:', e)
    }
  })
  eventList = []
}

const destroyModeler = () => {
  clearEvents()
  if (bpmnInstance) {
    try {
      bpmnInstance.destroy()
    } catch (e) {
      console.warn('销毁 modeler 失败:', e)
    }
    bpmnInstance = null
  }
}

const initializeBpmn = async (xml) => {
  if (!bpmnWrapper.value) {
    hasError.value = true
    errorMessage.value = '画布容器未找到'
    return
  }

  try {
    modelerReady.value = false
    hasError.value = false
    errorMessage.value = ''
    selectedEl.value = null

    console.log('[Designer] 开始初始化 bpmn-js...')

    destroyModeler()

    const BpmnModule = await import('bpmn-js/lib/Modeler')
    const BpmnModeler = BpmnModule.default || BpmnModule

    if (!BpmnModeler) {
      throw new Error('无法加载 bpmn-js 模块')
    }

    console.log('[Designer] 创建 BpmnModeler 实例')

    bpmnInstance = new BpmnModeler({
      container: bpmnWrapper.value
    })

    const eventBus = bpmnInstance.get('eventBus')

    const onElementClick = (evt) => {
      try {
        const el = evt.element
        if (el && el.businessObject) {
          handleSelectElement(el)
        }
      } catch (e) {
        console.warn('处理元素点击失败:', e)
      }
    }

    const onElementChanged = (evt) => {
      try {
        const el = evt.element
        if (selectedEl.value && selectedEl.value.id === el.id) {
          handleSelectElement(el)
        }
      } catch (e) {
        console.warn('处理元素变更失败:', e)
      }
    }

    const onCanvasClick = () => {
      selectedEl.value = null
    }

    eventBus.on('element.click', onElementClick)
    eventList.push({ bus: eventBus, event: 'element.click', handler: onElementClick })

    eventBus.on('element.changed', onElementChanged)
    eventList.push({ bus: eventBus, event: 'element.changed', handler: onElementChanged })

    eventBus.on('canvas.click', onCanvasClick)
    eventList.push({ bus: eventBus, event: 'canvas.click', handler: onCanvasClick })

    console.log('[Designer] 导入流程...')
    const bpmnXml = xml || defaultBpmnXml
    const result = await bpmnInstance.importXML(bpmnXml)
    
    if (result.warnings && result.warnings.length > 0) {
      console.log('[Designer] 导入警告:', result.warnings)
    }

    const canvas = bpmnInstance.get('canvas')
    canvas.zoom('fit-viewport')

    modelerReady.value = true
    console.log('[Designer] 初始化成功！')

  } catch (err) {
    console.error('[Designer] 初始化失败:', err)
    hasError.value = true
    errorMessage.value = err.message || '流程设计器初始化失败'
    destroyModeler()
  }
}

const handleSelectElement = (element) => {
  selectedEl.value = element
  const bo = element.businessObject
  if (!bo) return

  currentProps.id = bo.id || ''
  currentProps.name = bo.name || ''

  const type = bo.$type
  const editableTypes = [
    'bpmn:Process',
    'bpmn:UserTask',
    'bpmn:ServiceTask',
    'bpmn:ScriptTask',
    'bpmn:StartEvent',
    'bpmn:EndEvent',
    'bpmn:ExclusiveGateway',
    'bpmn:ParallelGateway',
    'bpmn:InclusiveGateway',
    'bpmn:SequenceFlow'
  ]
  canEdit.value = editableTypes.includes(type)
  isUserTask.value = type === 'bpmn:UserTask'
  isExclusiveGateway.value = type === 'bpmn:ExclusiveGateway'
  isSequenceFlow.value = type === 'bpmn:SequenceFlow'
  isServiceTask.value = type === 'bpmn:ServiceTask'
  isScriptTask.value = type === 'bpmn:ScriptTask'

  currentProps.assignee = ''
  currentProps.formKey = ''
  currentProps.default = ''
  currentProps.conditionExpression = ''
  currentProps.class = ''
  currentProps.expression = ''
  currentProps.scriptFormat = ''
  currentProps.script = ''

  if (isUserTask.value) {
    currentProps.assignee = bo.assignee || ''
    currentProps.formKey = bo.formKey || ''
  } else if (isExclusiveGateway.value) {
    currentProps.default = bo.default || ''
  } else if (isSequenceFlow.value) {
    currentProps.conditionExpression = bo.conditionExpression?.body || ''
  } else if (isServiceTask.value) {
    currentProps.class = bo['class'] || ''
    currentProps.expression = bo.expression || ''
  } else if (isScriptTask.value) {
    currentProps.scriptFormat = bo.scriptFormat || ''
    currentProps.script = bo.script || ''
  }
}

const updateName = () => {
  if (!selectedEl.value || !bpmnInstance) return
  try {
    const modeling = bpmnInstance.get('modeling')
    const registry = bpmnInstance.get('elementRegistry')
    const el = registry.get(selectedEl.value.id)
    if (el) {
      modeling.updateLabel(el, currentProps.name)
    }
  } catch (e) {
    console.warn('更新名称失败:', e)
  }
}

const updateId = () => {
  if (!selectedEl.value || !canEdit.value || !bpmnInstance) return
  try {
    const modeling = bpmnInstance.get('modeling')
    const registry = bpmnInstance.get('elementRegistry')
    const el = registry.get(selectedEl.value.id)
    if (el) {
      modeling.updateProperties(el, { id: currentProps.id })
      selectedEl.value.id = currentProps.id
    }
  } catch (e) {
    console.warn('更新ID失败:', e)
  }
}

const updateUserTask = () => {
  if (!selectedEl.value || !isUserTask.value || !bpmnInstance) return
  try {
    const modeling = bpmnInstance.get('modeling')
    const registry = bpmnInstance.get('elementRegistry')
    const el = registry.get(selectedEl.value.id)
    if (el) {
      modeling.updateProperties(el, {
        assignee: currentProps.assignee,
        formKey: currentProps.formKey
      })
    }
  } catch (e) {
    console.warn('更新用户任务失败:', e)
  }
}

const updateServiceTask = () => {
  if (!selectedEl.value || !isServiceTask.value || !bpmnInstance) return
  try {
    const modeling = bpmnInstance.get('modeling')
    const registry = bpmnInstance.get('elementRegistry')
    const el = registry.get(selectedEl.value.id)
    if (el) {
      const props = {}
      if (currentProps.class) props['class'] = currentProps.class
      if (currentProps.expression) props.expression = currentProps.expression
      modeling.updateProperties(el, props)
    }
  } catch (e) {
    console.warn('更新服务任务失败:', e)
  }
}

const updateScriptTask = () => {
  if (!selectedEl.value || !isScriptTask.value || !bpmnInstance) return
  try {
    const modeling = bpmnInstance.get('modeling')
    const registry = bpmnInstance.get('elementRegistry')
    const el = registry.get(selectedEl.value.id)
    if (el) {
      const props = {}
      if (currentProps.scriptFormat) props.scriptFormat = currentProps.scriptFormat
      if (currentProps.script) props.script = currentProps.script
      modeling.updateProperties(el, props)
    }
  } catch (e) {
    console.warn('更新脚本任务失败:', e)
  }
}

const updateFlowProps = () => {
  if (!selectedEl.value || !bpmnInstance) return
  try {
    const modeling = bpmnInstance.get('modeling')
    const registry = bpmnInstance.get('elementRegistry')
    const el = registry.get(selectedEl.value.id)
    if (el) {
      const props = {}
      if (isExclusiveGateway.value && currentProps.default) {
        props.default = currentProps.default
      }
      if (isSequenceFlow.value && currentProps.conditionExpression) {
        const moddle = bpmnInstance.get('moddle')
        props.conditionExpression = moddle.create('bpmn:FormalExpression', {
          body: currentProps.conditionExpression
        })
      }
      if (Object.keys(props).length > 0) {
        modeling.updateProperties(el, props)
      }
    }
  } catch (e) {
    console.warn('更新属性失败:', e)
  }
}

const handleSave = async () => {
  if (!bpmnInstance || !modelId.value) return
  try {
    const result = await bpmnInstance.saveXML({ format: true })
    const xml = result.xml
    
    const saveResult = await request.put(`/process-model/${modelId.value}/bpmn-xml`, {
      bpmnXml: xml
    })
    
    if (saveResult.success) {
      ElMessage.success('保存成功')
    } else {
      ElMessage.error(saveResult.message || '保存失败')
    }
  } catch (e) {
    console.error('保存失败:', e)
  }
}

const handleDeploy = async () => {
  if (!bpmnInstance || !modelId.value) return
  try {
    await ElMessageBox.confirm('确定要部署该流程吗？', '提示', { type: 'warning' })
    
    const result = await bpmnInstance.saveXML({ format: true })
    const xml = result.xml
    
    await request.put(`/process-model/${modelId.value}/bpmn-xml`, {
      bpmnXml: xml
    })
    
    const deployResult = await request.post(`/process-model/${modelId.value}/deploy`)
    
    if (deployResult.success) {
      ElMessage.success('部署成功')
    } else {
      ElMessage.error(deployResult.message || '部署失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('部署失败:', e)
    }
  }
}

const handleExport = async () => {
  if (!bpmnInstance) return
  try {
    const result = await bpmnInstance.saveXML({ format: true })
    const xml = result.xml
    const blob = new Blob([xml], { type: 'application/xml' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${processName.value || 'process'}.bpmn20.xml`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    console.error('导出失败:', e)
  }
}

const handleZoomIn = () => {
  if (bpmnInstance) {
    const canvas = bpmnInstance.get('canvas')
    canvas.zoom(canvas.zoom() * 1.2, { x: 0, y: 0 })
  }
}

const handleZoomOut = () => {
  if (bpmnInstance) {
    const canvas = bpmnInstance.get('canvas')
    canvas.zoom(canvas.zoom() * 0.8, { x: 0, y: 0 })
  }
}

const handleZoomReset = () => {
  if (bpmnInstance) {
    const canvas = bpmnInstance.get('canvas')
    canvas.zoom('fit-viewport')
  }
}

const retryInitialize = () => {
  initializeBpmn()
}

onMounted(async () => {
  console.log('[Designer] 组件已挂载')
  await loadUsers()
  await loadForms()
  await nextTick()
  await nextTick()
  
  const xml = await loadModel()
  console.log('[Designer] 开始初始化')
  initializeBpmn(xml)
})

onBeforeUnmount(() => {
  console.log('[Designer] 组件卸载，清理资源')
  destroyModeler()
})
</script>

<style scoped>
.process-designer-page {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}

.page-header .header-left {
  display: flex;
  align-items: center;
}

.page-header h2 {
  margin: 0;
  font-size: 18px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-content {
  flex: 1;
  display: flex;
  gap: 16px;
  min-height: 500px;
  overflow: hidden;
}

.canvas-section {
  flex: 1;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background: #fff;
  position: relative;
  overflow: hidden;
  min-height: 500px;
}

.bpmn-canvas {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
}

.loading-mask, .error-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.95);
  z-index: 1000;
  gap: 12px;
}

.loading-icon {
  font-size: 36px;
  color: #409eff;
}

.error-icon {
  font-size: 36px;
  color: #f56c6c;
}

.error-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.properties-section {
  width: 320px;
  flex-shrink: 0;
}

.empty-state {
  padding: 20px 0;
}
</style>
