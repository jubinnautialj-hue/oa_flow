<template>
  <div class="my-applications">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的申请</span>
        </div>
      </template>
      <el-table :data="applications" border style="width: 100%">
        <el-table-column prop="name" label="流程名称" />
        <el-table-column prop="businessKey" label="业务编号" />
        <el-table-column label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.endTime ? 'info' : 'success'">
              {{ scope.row.endTime ? '已结束' : '进行中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="200" />
        <el-table-column prop="endTime" label="结束时间" width="200">
          <template #default="scope">
            {{ scope.row.endTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewDetail(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="applications.length === 0" description="暂无申请记录" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const applications = ref([])

const loadData = async () => {
  applications.value = await request.get('/task/history/processes') || []
}

const viewDetail = (row) => {
  console.log('查看详情', row)
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
</style>
