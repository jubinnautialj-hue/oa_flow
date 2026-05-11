<template>
  <div class="process-designer-page">
    <div class="page-header">
      <h2>流程设计器</h2>
      <div class="header-actions">
        <el-input v-model="processName" placeholder="流程名称" style="width: 200px; margin-right: 10px" />
        <el-button @click="handleNew">新建</el-button>
        <el-button type="primary" @click="handleDeploy" :disabled="!modelerReady">部署</el-button>
        <el-button type="success" @click="handleExport" :disabled="!modelerReady">导出</el-button>
      </div>
    </div>

    <div class="page-tips">
      <el-alert type="info" show-icon :closable="false">
        <template #title>使用说明</template>
        <p>使用左侧工具栏拖拽元素到画布，连接线连接节点，点击元素可在右侧编辑属性。</p>
      </el-alert>
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
                  <el-option label="流程发起人" value="${initiator}" />
                  <el-option v-for="u in userList" :key="u.username" :label="u.name" :value="u.username" />
                </el-select>
              </el-form-item>
              <el-form-item label="表单Key">
                <el-input v-model="currentProps.formKey" placeholder="表单标识" @change="updateUserTask" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, Warning } from '@element-plus/icons-vue'
import request from '@/utils/request'

const bpmnWrapper = ref(null)
const processName = ref('新流程')
const modelerReady = ref(false)
const hasError = ref(false)
const errorMessage = ref('')
const selectedEl = ref(null)
const userList = ref([])

let bpmnInstance = null
let eventList = []

const currentProps = reactive({
  id: '',
  name: '',
  assignee: '',
  formKey: ''
})

const canEdit = ref(false)
const isUserTask = ref(false)

const defaultBpmnXml = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
  id="MyDefinitions"
  targetNamespace="http://flowable.org/bpmn">
  <bpmn:process id="MyProcess" name="新流程" isExecutable="true">
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
    <bpmndi:BPMNPlane id="Plane_1" bpmnElement="MyProcess">
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

const initializeBpmn = async () => {
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

    console.log('[Designer] 导入默认流程...')
    const result = await bpmnInstance.importXML(defaultBpmnXml)
    
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
    'bpmn:StartEvent',
    'bpmn:EndEvent',
    'bpmn:ExclusiveGateway',
    'bpmn:SequenceFlow'
  ]
  canEdit.value = editableTypes.includes(type)
  isUserTask.value = type === 'bpmn:UserTask'

  if (isUserTask.value) {
    currentProps.assignee = bo.assignee || ''
    currentProps.formKey = bo.formKey || ''
  } else {
    currentProps.assignee = ''
    currentProps.formKey = ''
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

const handleNew = async () => {
  if (!bpmnInstance) return
  try {
    await ElMessageBox.confirm('确定要新建流程吗？当前修改将丢失。', '提示', { type: 'warning' })
    processName.value = '新流程'
    selectedEl.value = null
    await bpmnInstance.importXML(defaultBpmnXml)
    const canvas = bpmnInstance.get('canvas')
    canvas.zoom('fit-viewport')
    ElMessage.success('已创建新流程')
  } catch {}
}

const handleDeploy = async () => {
  if (!bpmnInstance) return
  try {
    if (!processName.value) {
      ElMessage.warning('请输入流程名称')
      return
    }
    const result = await bpmnInstance.saveXML({ format: true })
    const xml = result.xml
    
    const deployResult = await request.post('/process/deploy/xml', {
      bpmnXml: xml,
      processName: processName.value
    })
    
    if (deployResult.success) {
      ElMessage.success('部署成功')
    } else {
      ElMessage.error('部署失败: ' + (deployResult.message || '未知错误'))
    }
  } catch (e) {
    console.error('部署失败:', e)
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

const retryInitialize = () => {
  initializeBpmn()
}

onMounted(async () => {
  console.log('[Designer] 组件已挂载')
  await loadUsers()
  await nextTick()
  await nextTick()
  await nextTick()
  console.log('[Designer] 开始初始化')
  initializeBpmn()
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

.page-header h2 {
  margin: 0;
  font-size: 18px;
}

.header-actions {
  display: flex;
  align-items: center;
}

.page-tips {
  margin-bottom: 16px;
}

.page-content {
  flex: 1;
  display: flex;
  gap: 16px;
  min-height: 500px;
}

.canvas-section {
  flex: 1;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background: #fff;
  position: relative;
  overflow: hidden;
}

.bpmn-canvas {
  width: 100%;
  height: 100%;
  min-height: 500px;
  position: relative;
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
  z-index: 100;
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
  width: 300px;
  flex-shrink: 0;
}

.empty-state {
  padding: 20px 0;
}
</style>
