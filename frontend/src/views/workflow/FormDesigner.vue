<template>
  <div class="form-designer-page">
    <div class="page-header">
      <div class="header-left">
        <el-button link @click="goBack" style="padding: 0; margin-right: 10px">
          <el-icon><ArrowLeft /></el-icon> 返回
        </el-button>
        <h2>{{ formName || '表单设计器' }}</h2>
      </div>
      <div class="header-actions">
        <el-button @click="handlePreview" :disabled="fields.length === 0">预览</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
        <el-button type="success" @click="handleExport">导出JSON</el-button>
      </div>
    </div>

    <div class="page-content">
      <div class="left-panel">
        <el-card shadow="never" class="toolbar-card">
          <template #header>
            <span>组件库</span>
          </template>
          <div class="component-list">
            <div class="component-group">
              <div class="group-title">基础组件</div>
              <div v-for="item in basicComponents" :key="item.type" class="component-item" draggable="true" @dragstart="handleDragStart($event, item)">
                <el-icon><component :is="item.icon" /></el-icon>
                <span>{{ item.label }}</span>
              </div>
            </div>
            <div class="component-group">
              <div class="group-title">高级组件</div>
              <div v-for="item in advancedComponents" :key="item.type" class="component-item" draggable="true" @dragstart="handleDragStart($event, item)">
                <el-icon><component :is="item.icon" /></el-icon>
                <span>{{ item.label }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <div class="center-panel" @drop="handleDrop" @dragover.prevent @dragenter.prevent>
        <el-card shadow="never" class="form-card">
          <template #header>
            <span>表单设计区域</span>
          </template>
          <div class="form-container" v-if="fields.length > 0">
            <div v-for="(field, index) in fields" :key="field.id" class="form-field" :class="{ 'field-selected': selectedIndex === index }" @click="selectField(index)">
              <div class="field-toolbar">
                <el-button-link @click.stop="moveUp(index)" :disabled="index === 0">
                  <el-icon><ArrowUp /></el-icon>
                </el-button-link>
                <el-button-link @click.stop="moveDown(index)" :disabled="index === fields.length - 1">
                  <el-icon><ArrowDown /></el-icon>
                </el-button-link>
                <el-button-link @click.stop="copyField(index)">
                  <el-icon><DocumentCopy /></el-icon>
                </el-button-link>
                <el-button-link @click.stop="deleteField(index)" type="danger">
                  <el-icon><Delete /></el-icon>
                </el-button-link>
              </div>
              <el-form-item :label="field.label" :required="field.required">
                <template v-if="field.type === 'input'">
                  <el-input v-model="field.defaultValue" :placeholder="field.placeholder" disabled />
                </template>
                <template v-else-if="field.type === 'textarea'">
                  <el-input v-model="field.defaultValue" type="textarea" :placeholder="field.placeholder" :rows="field.rows" disabled />
                </template>
                <template v-else-if="field.type === 'number'">
                  <el-input-number v-model="field.defaultValue" :placeholder="field.placeholder" disabled />
                </template>
                <template v-else-if="field.type === 'date'">
                  <el-date-picker v-model="field.defaultValue" :type="field.dateType" placeholder="选择日期" disabled style="width: 100%" />
                </template>
                <template v-else-if="field.type === 'select'">
                  <el-select v-model="field.defaultValue" placeholder="请选择" disabled style="width: 100%">
                    <el-option v-for="opt in field.options" :key="opt.value" :label="opt.label" :value="opt.value" />
                  </el-select>
                </template>
                <template v-else-if="field.type === 'radio'">
                  <el-radio-group v-model="field.defaultValue" disabled>
                    <el-radio v-for="opt in field.options" :key="opt.value" :label="opt.value">{{ opt.label }}</el-radio>
                  </el-radio-group>
                </template>
                <template v-else-if="field.type === 'checkbox'">
                  <el-checkbox-group v-model="field.defaultValue" disabled>
                    <el-checkbox v-for="opt in field.options" :key="opt.value" :label="opt.value">{{ opt.label }}</el-checkbox>
                  </el-checkbox-group>
                </template>
                <template v-else-if="field.type === 'switch'">
                  <el-switch v-model="field.defaultValue" disabled />
                </template>
              </el-form-item>
            </div>
          </div>
          <el-empty v-else description="从左侧拖拽组件到此处" />
        </el-card>
      </div>

      <div class="right-panel">
        <el-card shadow="never" class="properties-card">
          <template #header>
            <span>属性设置</span>
          </template>
          <div v-if="selectedIndex !== -1 && fields[selectedIndex]" class="properties-form">
            <el-form label-width="80px">
              <el-form-item label="标签">
                <el-input v-model="fields[selectedIndex].label" />
              </el-form-item>
              <el-form-item label="字段名">
                <el-input v-model="fields[selectedIndex].field" />
              </el-form-item>
              <el-form-item label="占位符">
                <el-input v-model="fields[selectedIndex].placeholder" />
              </el-form-item>
              <el-form-item label="是否必填">
                <el-switch v-model="fields[selectedIndex].required" />
              </el-form-item>
              <template v-if="fields[selectedIndex].type === 'textarea'">
                <el-form-item label="行数">
                  <el-input-number v-model="fields[selectedIndex].rows" :min="2" :max="10" />
                </el-form-item>
              </template>
              <template v-if="fields[selectedIndex].type === 'date'">
                <el-form-item label="日期类型">
                  <el-select v-model="fields[selectedIndex].dateType">
                    <el-option label="日期" value="date" />
                    <el-option label="日期时间" value="datetime" />
                    <el-option label="时间" value="time" />
                    <el-option label="日期范围" value="daterange" />
                  </el-select>
                </el-form-item>
              </template>
              <template v-if="fields[selectedIndex].type === 'select' || fields[selectedIndex].type === 'radio' || fields[selectedIndex].type === 'checkbox'">
                <el-form-item label="选项">
                  <div class="options-editor">
                    <div v-for="(opt, idx) in fields[selectedIndex].options" :key="idx" class="option-row">
                      <el-input v-model="fields[selectedIndex].options[idx].label" placeholder="标签" size="small" style="width: 100px" />
                      <el-input v-model="fields[selectedIndex].options[idx].value" placeholder="值" size="small" style="width: 100px" />
                      <el-button size="small" type="danger" link @click="fields[selectedIndex].options.splice(idx, 1)">删除</el-button>
                    </div>
                    <el-button size="small" link @click="fields[selectedIndex].options.push({ label: '', value: '' })">+ 添加选项</el-button>
                  </div>
                </el-form-item>
              </template>
            </el-form>
          </div>
          <el-empty v-else description="选择字段后编辑属性" />
        </el-card>
      </div>
    </div>

    <el-dialog v-model="previewVisible" title="表单预览" width="600px">
      <el-form :model="previewData" label-width="100px">
        <el-form-item v-for="field in fields" :key="field.id" :label="field.label" :required="field.required">
          <template v-if="field.type === 'input'">
            <el-input v-model="previewData[field.field]" :placeholder="field.placeholder" />
          </template>
          <template v-else-if="field.type === 'textarea'">
            <el-input v-model="previewData[field.field]" type="textarea" :placeholder="field.placeholder" :rows="field.rows" />
          </template>
          <template v-else-if="field.type === 'number'">
            <el-input-number v-model="previewData[field.field]" :placeholder="field.placeholder" />
          </template>
          <template v-else-if="field.type === 'date'">
            <el-date-picker v-model="previewData[field.field]" :type="field.dateType" placeholder="选择日期" style="width: 100%" />
          </template>
          <template v-else-if="field.type === 'select'">
            <el-select v-model="previewData[field.field]" placeholder="请选择" style="width: 100%">
              <el-option v-for="opt in field.options" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </template>
          <template v-else-if="field.type === 'radio'">
            <el-radio-group v-model="previewData[field.field]">
              <el-radio v-for="opt in field.options" :key="opt.value" :label="opt.value">{{ opt.label }}</el-radio>
            </el-radio-group>
          </template>
          <template v-else-if="field.type === 'checkbox'">
            <el-checkbox-group v-model="previewData[field.field]">
              <el-checkbox v-for="opt in field.options" :key="opt.value" :label="opt.value">{{ opt.label }}</el-checkbox>
            </el-checkbox-group>
          </template>
          <template v-else-if="field.type === 'switch'">
            <el-switch v-model="previewData[field.field]" />
          </template>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  ArrowLeft, ArrowUp, ArrowDown, DocumentCopy, Delete,
  Edit, Calendar, Select, Files, Document, SwitchButton, List, Check
} from '@element-plus/icons-vue'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const modelId = ref(route.params.id)
const formName = ref('新建表单')
const fields = ref([])
const selectedIndex = ref(-1)
const previewVisible = ref(false)
const previewData = ref({})

const basicComponents = [
  { type: 'input', label: '单行输入', icon: Edit },
  { type: 'textarea', label: '多行输入', icon: Document },
  { type: 'number', label: '数字输入', icon: Files },
  { type: 'date', label: '日期选择', icon: Calendar },
]

const advancedComponents = [
  { type: 'select', label: '下拉选择', icon: Select },
  { type: 'radio', label: '单选框组', icon: List },
  { type: 'checkbox', label: '多选框组', icon: Check },
  { type: 'switch', label: '开关', icon: SwitchButton },
]

const generateId = () => {
  return 'field_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
}

const createField = (component) => {
  const field = {
    id: generateId(),
    type: component.type,
    label: component.label,
    field: 'field_' + fields.value.length,
    placeholder: '请输入' + component.label,
    required: false,
    defaultValue: ''
  }
  
  if (component.type === 'select' || component.type === 'radio' || component.type === 'checkbox') {
    field.options = [
      { label: '选项1', value: 'option1' },
      { label: '选项2', value: 'option2' }
    ]
  }
  
  if (component.type === 'textarea') {
    field.rows = 3
  }
  
  if (component.type === 'date') {
    field.dateType = 'date'
  }
  
  if (component.type === 'checkbox') {
    field.defaultValue = []
  }
  
  return field
}

const handleDragStart = (event, component) => {
  event.dataTransfer.setData('component', JSON.stringify(component))
}

const handleDrop = (event) => {
  const componentData = event.dataTransfer.getData('component')
  if (componentData) {
    const component = JSON.parse(componentData)
    const field = createField(component)
    fields.value.push(field)
    selectedIndex.value = fields.value.length - 1
  }
}

const selectField = (index) => {
  selectedIndex.value = index
}

const moveUp = (index) => {
  if (index > 0) {
    const temp = fields.value[index]
    fields.value[index] = fields.value[index - 1]
    fields.value[index - 1] = temp
    selectedIndex.value = index - 1
  }
}

const moveDown = (index) => {
  if (index < fields.value.length - 1) {
    const temp = fields.value[index]
    fields.value[index] = fields.value[index + 1]
    fields.value[index + 1] = temp
    selectedIndex.value = index + 1
  }
}

const copyField = (index) => {
  const field = JSON.parse(JSON.stringify(fields.value[index]))
  field.id = generateId()
  field.field = field.field + '_copy'
  fields.value.splice(index + 1, 0, field)
  selectedIndex.value = index + 1
}

const deleteField = (index) => {
  fields.value.splice(index, 1)
  if (selectedIndex.value >= fields.value.length) {
    selectedIndex.value = fields.value.length - 1
  }
}

const goBack = () => {
  router.push('/form-design')
}

const loadModel = async () => {
  if (!modelId.value) return
  try {
    const model = await request.get(`/form-model/${modelId.value}`)
    if (model) {
      formName.value = model.name
      if (model.formSchema && model.formSchema.trim()) {
        try {
          const schema = JSON.parse(model.formSchema)
          if (Array.isArray(schema)) {
            fields.value = schema
          }
        } catch (e) {
          console.warn('解析表单Schema失败:', e)
        }
      }
    }
  } catch (e) {
    console.warn('加载表单模型失败:', e)
  }
}

const handleSave = async () => {
  if (!modelId.value) return
  try {
    const schema = JSON.stringify(fields.value)
    const result = await request.put(`/form-model/${modelId.value}/schema`, {
      formSchema: schema
    })
    
    if (result.success) {
      ElMessage.success('保存成功')
    } else {
      ElMessage.error(result.message || '保存失败')
    }
  } catch (e) {
    console.error('保存失败:', e)
  }
}

const handlePreview = () => {
  previewData.value = {}
  fields.value.forEach(field => {
    previewData.value[field.field] = field.defaultValue
  })
  previewVisible.value = true
}

const handleExport = () => {
  const schema = JSON.stringify(fields.value, null, 2)
  const blob = new Blob([schema], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${formName.value || 'form'}.json`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

onMounted(() => {
  loadModel()
})
</script>

<style scoped>
.form-designer-page {
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
}

.left-panel {
  width: 220px;
  flex-shrink: 0;
}

.center-panel {
  flex: 1;
}

.right-panel {
  width: 320px;
  flex-shrink: 0;
}

.toolbar-card, .properties-card {
  height: 100%;
}

.form-card {
  height: 100%;
  overflow-y: auto;
}

.component-list {
  padding: 10px;
}

.component-group {
  margin-bottom: 16px;
}

.group-title {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
  padding-left: 4px;
}

.component-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin-bottom: 8px;
  cursor: move;
  transition: all 0.2s;
}

.component-item:hover {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.component-item .el-icon {
  font-size: 16px;
  color: #409eff;
}

.form-container {
  padding: 20px;
}

.form-field {
  position: relative;
  padding: 10px;
  border: 1px solid transparent;
  border-radius: 4px;
  margin-bottom: 10px;
  transition: all 0.2s;
}

.form-field:hover {
  border-color: #409eff;
}

.field-selected {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.field-toolbar {
  position: absolute;
  right: 5px;
  top: 5px;
  display: none;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  z-index: 10;
}

.form-field:hover .field-toolbar,
.field-selected .field-toolbar {
  display: flex;
}

.properties-form {
  padding: 10px;
}

.options-editor {
  max-height: 200px;
  overflow-y: auto;
}

.option-row {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 8px;
}
</style>
