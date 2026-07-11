<template>
  <el-card shadow="hover">
    <template #header>
      <div class="card-header">
        <span>装车结果</span>
        <el-tag :type="result?.success ? 'success' : 'danger'" v-if="result">
          {{ result.success ? '成功' : '部分成功' }}
        </el-tag>
      </div>
    </template>
    <div v-if="result" class="result-container">
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="总重量">{{ result.totalWeight.toFixed(1) }} kg</el-descriptions-item>
        <el-descriptions-item label="总体积">{{ (result.totalVolume / 1000000).toFixed(2) }} m³</el-descriptions-item>
        <el-descriptions-item label="空间利用率">
          <el-progress
            :percentage="result.utilizationRate"
            :color="result.utilizationRate > 70 ? '#67c23a' : result.utilizationRate > 50 ? '#e6a23c' : '#f56c6c'"
            :stroke-width="20"
          />
        </el-descriptions-item>
        <el-descriptions-item label="已装载">
          {{ result.placedItems.length }} 件
        </el-descriptions-item>
        <el-descriptions-item label="未装载" v-if="result.unplacedItems.length > 0">
          <el-tag type="danger" size="small">{{ result.unplacedItems.length }} 件</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <div v-if="result.unplacedItems.length > 0" style="margin-top: 16px">
        <div style="font-size: 14px; color: #909399; margin-bottom: 8px">未装载货物:</div>
        <el-tag
          v-for="item in result.unplacedItems"
          :key="item.id"
          size="small"
          style="margin-right: 8px; margin-bottom: 8px"
        >
          {{ item.name }} × {{ item.quantity }}
        </el-tag>
      </div>
    </div>
    <div v-else class="empty-state">
      点击"开始装车"查看结果
    </div>
  </el-card>
</template>

<script setup lang="ts">
import type { LoadingResult } from '../types'

const props = defineProps<{
  result: LoadingResult | null
}>()
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.result-container {
  min-height: 200px;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #909399;
}
</style>
