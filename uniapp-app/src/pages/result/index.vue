<template>
  <view class="page">
    <view class="header">
      <text class="title">📊 装车结果</text>
      <text class="subtitle">共使用 {{ loadingResult && loadingResult.totalTrucksUsed || 0 }} 辆车</text>
    </view>

    <view v-if="loadingResult" class="card">
      <view class="result-header">
        <view class="card-title">
          <text class="icon">✅</text>
          <text>装载概览</text>
        </view>
        <text :class="['status', loadingResult.success ? 'success' : 'warning']">
          {{ loadingResult.success ? '全部装载' : '部分装载' }}
        </text>
      </view>
      
      <view class="statistics">
        <view class="stat-item">
          <text class="value">{{ totalPlacedItems }}</text>
          <text class="label">已装载件数</text>
        </view>
        <view class="stat-item">
          <text class="value">{{ totalWeight.toFixed(0) }}</text>
          <text class="label">总重量 (kg)</text>
        </view>
        <view class="stat-item">
          <text class="value">{{ loadingResult.totalTrucksUsed }}</text>
          <text class="label">使用车辆</text>
        </view>
      </view>
      
      <view class="progress-container">
        <view class="label">
          <text>平均空间利用率</text>
          <text class="rate">{{ avgUtilization.toFixed(1) }}%</text>
        </view>
        <view class="progress-bar">
          <view :class="['progress-fill', getProgressColor(avgUtilization)]" :style="{ width: Math.min(avgUtilization, 100) + '%' }" />
        </view>
      </view>
    </view>

    <view v-if="loadingResult" class="card">
      <text class="card-title">
        <text class="icon">🚛</text>
        <text>车辆详情</text>
      </text>
      
      <view v-if="loadingResult.truckResults && loadingResult.truckResults.length > 0" class="truck-list">
        <view v-for="(truck, index) in loadingResult.truckResults" :key="index" class="truck-item">
          <view class="truck-header">
            <text class="truck-num">第 {{ truck.truckNumber }} 辆车</text>
            <text class="truck-utilization">{{ truck.utilizationRate.toFixed(1) }}%</text>
          </view>
          <view class="truck-stats">
            <text class="stat">重量: {{ truck.totalWeight.toFixed(0) }}kg</text>
            <text class="stat">体积: {{ (truck.totalVolume / 1000000).toFixed(2) }}m³</text>
            <text class="stat">装载: {{ truck.placedItems.length }}件</text>
          </view>
          <view v-if="truck.placedItems.length > 0" class="placed-items">
            <view v-for="(item, idx) in truck.placedItems.slice(0, 5)" :key="idx" class="placed-item">
              <text>{{ item.name }}</text>
              <text>位置: ({{ item.x.toFixed(0) }}, {{ item.y.toFixed(0) }})</text>
            </view>
            <view v-if="truck.placedItems.length > 5" class="placed-item">
              <text>...</text>
              <text>还有 {{ truck.placedItems.length - 5 }} 件</text>
            </view>
          </view>
        </view>
      </view>
      
      <view v-else class="empty-state">
        <text class="text">暂无车辆数据</text>
      </view>
    </view>

    <view v-if="loadingResult && loadingResult.unplacedItems && loadingResult.unplacedItems.length > 0" class="card">
      <view class="unplaced-section">
        <text class="section-title">❌ 未装载货物 ({{ loadingResult.unplacedItems.length }})</text>
        <view v-for="(item, index) in loadingResult.unplacedItems" :key="index" class="unplaced-item">
          <text class="name">{{ item.name }}</text>
          <text class="quantity">×{{ item.quantity }}</text>
        </view>
      </view>
    </view>

    <view v-if="!loadingResult" class="empty-state">
      <text class="icon">📊</text>
      <text class="title">暂无装车结果</text>
      <text class="subtitle">请先添加货物并点击"开始装车"</text>
      <button class="btn-primary" @click="handleRetry">去装车</button>
    </view>

    <button v-if="loadingResult" class="btn-secondary" @click="handleVisualize">查看可视化</button>
    <button v-if="loadingResult" class="btn-primary btn-large" @click="handleRetry">重新计算</button>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useLoadingStore } from '@/store/loading'
import { storeToRefs } from 'pinia'

const store = useLoadingStore()
const { loadingResult } = storeToRefs(store)

const totalPlacedItems = computed(() => {
  if (!loadingResult.value || !loadingResult.value.truckResults) return 0
  return loadingResult.value.truckResults.reduce((sum: number, truck: any) => sum + truck.placedItems.length, 0)
})

const totalWeight = computed(() => {
  if (!loadingResult.value || !loadingResult.value.truckResults) return 0
  return loadingResult.value.truckResults.reduce((sum: number, truck: any) => sum + truck.totalWeight, 0)
})

const avgUtilization = computed(() => {
  if (!loadingResult.value || !loadingResult.value.truckResults || loadingResult.value.truckResults.length === 0) {
    return 0
  }
  return loadingResult.value.truckResults.reduce((sum: number, truck: any) => sum + truck.utilizationRate, 0) / loadingResult.value.truckResults.length
})

const getProgressColor = (rate: number) => {
  if (rate > 70) return 'high'
  if (rate > 50) return 'medium'
  return 'low'
}

const handleRetry = () => {
  uni.switchTab({ url: '/pages/home/index' })
}

const handleVisualize = () => {
  uni.navigateTo({ url: '/pages/visualization/index' })
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  padding-bottom: 120rpx;
}

.header {
  padding: 32rpx;
  text-align: center;
  
  .title {
    font-size: 40rpx;
    font-weight: 700;
    color: #ffffff;
    display: block;
    margin-bottom: 16rpx;
  }
  
  .subtitle {
    font-size: 24rpx;
    color: rgba(255, 255, 255, 0.6);
    display: block;
  }
}

.card {
  background: #ffffff;
  border-radius: 16rpx;
  margin: 24rpx 32rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
}

.card-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 24rpx;
  display: flex;
  align-items: center;
  
  .icon {
    margin-right: 16rpx;
    font-size: 36rpx;
  }
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32rpx;
  
  .status {
    font-size: 28rpx;
    padding: 8rpx 24rpx;
    border-radius: 999rpx;
    
    &.success {
      background: rgba(0, 180, 42, 0.1);
      color: #00b42a;
    }
    
    &.warning {
      background: rgba(255, 125, 0, 0.1);
      color: #ff7d00;
    }
  }
}

.statistics {
  display: flex;
  justify-content: space-around;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f2f3f5;
  
  .stat-item {
    text-align: center;
    
    .value {
      font-size: 36rpx;
      font-weight: 700;
      color: #165dff;
      display: block;
      margin-bottom: 8rpx;
    }
    
    .label {
      font-size: 24rpx;
      color: #86909c;
    }
  }
}

.progress-container {
  padding: 24rpx 0;
  
  .label {
    font-size: 28rpx;
    color: #4e5969;
    margin-bottom: 16rpx;
    display: flex;
    justify-content: space-between;
    
    .rate {
      color: #165dff;
      font-weight: 600;
    }
  }
  
  .progress-bar {
    height: 24rpx;
    background: #f2f3f5;
    border-radius: 999rpx;
    overflow: hidden;
    
    .progress-fill {
      height: 100%;
      border-radius: 999rpx;
      transition: width 0.3s ease;
      
      &.high {
        background: linear-gradient(90deg, #67c23a 0%, #85ce61 100%);
      }
      
      &.medium {
        background: linear-gradient(90deg, #e6a23c 0%, #ebb563 100%);
      }
      
      &.low {
        background: linear-gradient(90deg, #f56c6c 0%, #f89898 100%);
      }
    }
  }
}

.truck-list {
  margin-top: 24rpx;
}

.truck-item {
  background: #f5f6f7;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  
  .truck-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16rpx;
    
    .truck-num {
      font-size: 28rpx;
      font-weight: 600;
      color: #1d2129;
    }
    
    .truck-utilization {
      font-size: 24rpx;
      color: #165dff;
    }
  }
  
  .truck-stats {
    display: flex;
    gap: 32rpx;
    margin-bottom: 16rpx;
    
    .stat {
      font-size: 24rpx;
      color: #86909c;
    }
  }
  
  .placed-items {
    max-height: 200rpx;
    overflow-y: auto;
  }
  
  .placed-item {
    font-size: 24rpx;
    color: #4e5969;
    padding: 8rpx 0;
    display: flex;
    justify-content: space-between;
  }
}

.unplaced-section {
  margin-top: 32rpx;
  
  .section-title {
    font-size: 28rpx;
    font-weight: 600;
    color: #f53f3f;
    margin-bottom: 16rpx;
    display: block;
  }
  
  .unplaced-item {
    display: flex;
    justify-content: space-between;
    padding: 16rpx 0;
    border-bottom: 1rpx solid #f2f3f5;
    
    &:last-child {
      border-bottom: none;
    }
    
    .name {
      font-size: 28rpx;
      color: #1d2129;
    }
    
    .quantity {
      font-size: 28rpx;
      color: #f53f3f;
      font-weight: 600;
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 96rpx 0;
  
  .icon {
    font-size: 120rpx;
    display: block;
    margin-bottom: 32rpx;
  }
  
  .title {
    font-size: 36rpx;
    font-weight: 600;
    color: #1d2129;
    display: block;
    margin-bottom: 16rpx;
  }
  
  .subtitle {
    font-size: 28rpx;
    color: #86909c;
    text-align: center;
    display: block;
    margin-bottom: 32rpx;
  }
  
  .btn-primary {
    height: 80rpx;
    padding: 0 64rpx;
    border-radius: 48rpx;
    font-size: 28rpx;
    background: #165dff;
    color: #ffffff;
    border: none;
    
    &:active {
      opacity: 0.9;
    }
  }
}

.btn-primary {
  height: 80rpx;
  border-radius: 48rpx;
  font-size: 30rpx;
  font-weight: 600;
  background: linear-gradient(135deg, #165dff 0%, #4080ff 100%);
  color: #ffffff;
  margin: 32rpx;
  border: none;
  
  &:active {
    opacity: 0.9;
    transform: scale(0.98);
  }
  
  &.btn-large {
    height: 96rpx;
    font-size: 32rpx;
    width: calc(100% - 64rpx);
  }
}

.btn-secondary {
  height: 80rpx;
  border-radius: 48rpx;
  font-size: 30rpx;
  font-weight: 600;
  background: #f2f3f5;
  color: #4e5969;
  margin: 16rpx 32rpx;
  border: none;
  width: calc(100% - 64rpx);
  
  &:active {
    opacity: 0.9;
    transform: scale(0.98);
  }
}
</style>
