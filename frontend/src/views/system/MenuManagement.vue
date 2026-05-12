<template>
  <div class="menu-management">
    <el-card>
      <div class="toolbar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="关键字">
            <el-input v-model="searchForm.keyword" placeholder="搜索菜单名称" clearable @keyup.enter="handleSearch" style="width: 240px">
              <template #append>
                <el-button icon="Search" @click="handleSearch"></el-button>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleAdd(null)"><el-icon><Plus /></el-icon>新增菜单</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <div v-loading="loading" class="menu-tree">
        <div v-for="menu in topLevelMenus" :key="menu.id" class="menu-item-container">
          <div class="menu-item top-level" :class="{ expanded: expandedIds.includes(menu.id) }">
            <div class="menu-item-left">
              <el-icon v-if="hasChildren(menu.id)" class="collapse-icon" @click="toggleExpand(menu.id)">
                <CaretRight v-if="!expandedIds.includes(menu.id)" />
                <CaretBottom v-else />
              </el-icon>
              <el-icon v-else class="placeholder-icon"><Menu /></el-icon>
              <span class="menu-name">{{ menu.name }}</span>
              <el-tag v-if="menu.status === 1" type="success" size="small">启用</el-tag>
              <el-tag v-else type="danger" size="small">禁用</el-tag>
            </div>
            <div class="menu-item-right">
              <span class="menu-path" v-if="menu.path">{{ menu.path }}</span>
              <el-button type="primary" size="small" @click="handleAdd(menu.id)">新增子菜单</el-button>
              <el-button type="primary" size="small" @click="handleEdit(menu)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDelete(menu)">删除</el-button>
            </div>
          </div>
          
          <div v-if="expandedIds.includes(menu.id) && hasChildren(menu.id)" class="children-container">
            <div v-for="child in getChildren(menu.id)" :key="child.id" class="menu-item child-level">
              <div class="menu-item-left">
                <el-icon class="placeholder-icon"><Document /></el-icon>
                <span class="menu-name">{{ child.name }}</span>
                <el-tag v-if="child.status === 1" type="success" size="small">启用</el-tag>
                <el-tag v-else type="danger" size="small">禁用</el-tag>
              </div>
              <div class="menu-item-right">
                <span class="menu-path" v-if="child.path">{{ child.path }}</span>
                <el-button type="primary" size="small" @click="handleEdit(child)">编辑</el-button>
                <el-button type="danger" size="small" @click="handleDelete(child)">删除</el-button>
              </div>
            </div>
          </div>
        </div>
        
        <el-empty v-if="topLevelMenus.length === 0" description="暂无数据" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="菜单名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="上级菜单">
          <el-select v-model="form.parentId" placeholder="请选择上级菜单" clearable style="width: 100%">
            <el-option label="无（一级菜单）" :value="null" />
            <el-option v-for="menu in parentMenuOptions" :key="menu.id" :label="menu.name" :value="menu.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="菜单路径">
          <el-input v-model="form.path" placeholder="请输入菜单路径（如：users）" />
        </el-form-item>
        <el-form-item label="菜单图标">
          <el-input v-model="form.icon" placeholder="请输入图标名称（如：User）" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const menuList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const expandedIds = ref([])

const searchForm = reactive({
  keyword: ''
})

const form = reactive({
  id: null,
  name: '',
  path: '',
  icon: '',
  sort: 0,
  parentId: null,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }]
}

const topLevelMenus = computed(() => {
  return menuList.value.filter(m => m.parentId == null)
})

const parentMenuOptions = computed(() => {
  return menuList.value.filter(m => m.parentId == null)
})

const hasChildren = (parentId) => {
  return menuList.value.some(m => m.parentId === parentId)
}

const getChildren = (parentId) => {
  return menuList.value.filter(m => m.parentId === parentId)
}

const loadData = async () => {
  loading.value = true
  try {
    const params = searchForm.keyword ? { keyword: searchForm.keyword } : {}
    menuList.value = await request.get('/menus', { params })
    if (searchForm.keyword) {
      expandedIds.value = []
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  loadData()
}

const toggleExpand = (id) => {
  const index = expandedIds.value.indexOf(id)
  if (index > -1) {
    expandedIds.value.splice(index, 1)
  } else {
    expandedIds.value.push(id)
  }
}

const handleAdd = (parentId) => {
  Object.assign(form, {
    id: null,
    name: '',
    path: '',
    icon: '',
    sort: 0,
    parentId: parentId,
    status: 1
  })
  dialogTitle.value = parentId ? '新增子菜单' : '新增菜单'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, {
    id: row.id,
    name: row.name,
    path: row.path || '',
    icon: row.icon || '',
    sort: row.sort || 0,
    parentId: row.parentId,
    status: row.status
  })
  dialogTitle.value = '编辑菜单'
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  if (hasChildren(row.id)) {
    ElMessage.warning('请先删除子菜单')
    return
  }
  try {
    await ElMessageBox.confirm('确定要删除该菜单吗？', '提示', { type: 'warning' })
    await request.delete(`/menus/${row.id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch {}
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    if (form.id) {
      await request.put(`/menus/${form.id}`, form)
    } else {
      await request.post('/menus', form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch {}
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 20px;
}

.menu-tree {
  min-height: 200px;
}

.menu-item-container {
  margin-bottom: 4px;
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 4px;
  transition: all 0.2s;
}

.menu-item:hover {
  background: #e6f7ff;
}

.menu-item.top-level {
  background: #ecf5ff;
}

.menu-item.top-level:hover {
  background: #d9ecff;
}

.menu-item.child-level {
  background: #fff;
  margin-left: 36px;
  border-left: 3px solid #409eff;
  margin-top: 4px;
}

.menu-item-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.menu-item-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.collapse-icon {
  cursor: pointer;
  font-size: 16px;
  color: #606266;
  width: 20px;
  text-align: center;
  transition: transform 0.2s;
}

.collapse-icon:hover {
  color: #409eff;
}

.placeholder-icon {
  font-size: 16px;
  color: #909399;
  width: 20px;
  text-align: center;
}

.menu-name {
  font-weight: 500;
  font-size: 14px;
}

.menu-path {
  color: #909399;
  font-size: 12px;
  margin-right: 8px;
}

.children-container {
  padding-left: 0;
}
</style>
