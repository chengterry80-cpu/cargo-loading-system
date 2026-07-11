<template>
  <el-card shadow="hover" style="margin-bottom: 16px">
    <template #header>
      <div class="card-header">
        <span>添加货物</span>
      </div>
    </template>
    <el-form :model="form" label-width="80px" size="small">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="货物名称">
            <el-input v-model="form.name" placeholder="请输入货物名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="数量">
            <el-input-number v-model="form.quantity" :min="1" :max="100" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="16">
        <el-col :span="8">
          <el-form-item label="长度">
            <el-input-number v-model="form.length" :min="1" :max="1200" style="width: 100%" />
            <div style="font-size: 12px; color: #999">cm</div>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="宽度">
            <el-input-number v-model="form.width" :min="1" :max="240" style="width: 100%" />
            <div style="font-size: 12px; color: #999">cm</div>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="高度">
            <el-input-number v-model="form.height" :min="1" :max="260" style="width: 100%" />
            <div style="font-size: 12px; color: #999">cm</div>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="重量">
        <el-input-number v-model="form.weight" :min="1" :max="20000" style="width: 200px" />
        <span style="margin-left: 8px; color: #999">kg</span>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleAdd">添加货物</el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { Cargo } from '../types'
import { ElMessage } from 'element-plus'

const emit = defineEmits<{
  add: [cargo: Cargo]
}>()

const form = ref<Cargo>({
  name: '',
  length: 100,
  width: 50,
  height: 80,
  weight: 50,
  quantity: 5
})

let idCounter = 1

const handleAdd = () => {
  if (!form.value.name.trim()) {
    ElMessage.warning('请输入货物名称')
    return
  }
  emit('add', {
    ...form.value,
    id: idCounter++
  })
  ElMessage.success('货物添加成功')
  resetForm()
}

const resetForm = () => {
  form.value = {
    name: '',
    length: 100,
    width: 50,
    height: 80,
    weight: 50,
    quantity: 5
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
