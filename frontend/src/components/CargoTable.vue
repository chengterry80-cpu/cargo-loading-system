<template>
  <el-card shadow="hover">
    <template #header>
      <div class="card-header">
        <span>货物列表 ({{ cargoList.length }})</span>
        <el-button type="danger" size="small" @click="handleClear" :disabled="cargoList.length === 0">清空</el-button>
      </div>
    </template>
    <el-table :data="cargoList" style="width: 100%" size="small" height="280">
      <el-table-column prop="name" label="货物名称" width="120" />
      <el-table-column label="尺寸 (cm)" width="140">
        <template #default="{ row }">
          {{ row.length }}×{{ row.width }}×{{ row.height }}
        </template>
      </el-table-column>
      <el-table-column prop="weight" label="重量 (kg)" width="90" />
      <el-table-column prop="quantity" label="数量" width="70" />
      <el-table-column label="操作" width="80">
        <template #default="{ $index }">
          <el-button link type="danger" size="small" @click="handleDelete($index)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import type { Cargo } from '../types'
import { ElMessageBox } from 'element-plus'

const props = defineProps<{
  cargoList: Cargo[]
}>()

const emit = defineEmits<{
  delete: [index: number]
  clear: []
}>()

const handleDelete = (index: number) => {
  emit('delete', index)
}

const handleClear = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有货物吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    emit('clear')
  } catch {
    // 用户取消
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}
</style>
