<template>
  <view class="page">
    <view class="header">
      <text class="title">👤 我的</text>
      <text class="subtitle">装车记录与设置</text>
    </view>

    <view class="card">
      <view class="card-title">
        <text class="icon">📜</text>
        <text>历史记录 ({{ historyList.length }})</text>
        <button v-if="historyList.length > 0" class="clear-btn" @click="handleClearAllHistory">清空</button>
      </view>
      
      <view v-if="isLoading" class="empty-state">
        <text class="text">加载中...</text>
      </view>
      
      <view v-else-if="historyList.length === 0" class="empty-state">
        <text class="icon">📭</text>
        <text class="text">暂无历史记录</text>
      </view>
      
      <scroll-view v-else class="history-list" scroll-y>
        <view v-for="item in historyList" :key="item.id" class="history-item">
          <view class="info">
            <text class="time">{{ formatDate(item.createTime) }}</text>
            <text class="detail">
              {{ item.cargoList.length }}件货物 → {{ item.result.totalTrucksUsed }}辆车
            </text>
          </view>
          <button class="delete-btn" @click="handleDeleteHistory(item.id)">删除</button>
        </view>
      </scroll-view>
    </view>

    <view class="card">
      <view class="about">
        <text class="about-title">📝 关于系统</text>
        <text class="about-content">
智能货物装车系统
基于MS-BFS（多起点最佳适配天际线）算法

核心功能：
- 支持多货物批量装车
- 自动计算最佳装载方案
- 支持多种排序策略
- 智能空间利用率优化

算法特点：
- 6种排序策略自适应
- 回填优化提升装载率
- 天际线动态更新
- 支持货物旋转放置

技术栈：
- 后端：Spring Boot + Java
- 前端：UniApp + Vue3 + TypeScript
- 支持微信/支付宝/抖音小程序
        </text>
      </view>
    </view>

    <view class="card">
      <button class="btn-primary" @click="handleGoHome">开始装车</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { loadingApi } from '@/services/loading'
import type { LoadingHistory } from '@/types'

const historyList = ref<LoadingHistory[]>([])
const isLoading = ref(false)

onMounted(() => {
  loadHistory()
})

const loadHistory = async () => {
  isLoading.value = true
  try {
    const history = await loadingApi.getHistory()
    historyList.value = history
  } catch (error) {
    console.error('加载历史记录失败:', error)
    historyList.value = []
  } finally {
    isLoading.value = false
  }
}

const handleDeleteHistory = (id: number) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这条历史记录吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await loadingApi.deleteHistory(id)
          uni.showToast({ title: '删除成功', icon: 'success' })
          loadHistory()
        } catch (error) {
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

const handleClearAllHistory = () => {
  uni.showModal({
    title: '确认清空',
    content: '确定要清空所有历史记录吗？',
    success: async (res) => {
      if (res.confirm) {
        for (const history of historyList.value) {
          try {
            await loadingApi.deleteHistory(history.id)
          } catch {
            // 继续删除其他记录
          }
        }
        uni.showToast({ title: '已清空', icon: 'success' })
        historyList.value = []
      }
    }
  })
}

const handleGoHome = () => {
  uni.switchTab({ url: '/pages/home/index' })
}

const formatDate = (dateStr: string) => {
  try {
    const date = new Date(dateStr)
    return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
  } catch {
    return dateStr
  }
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

.history-list {
  max-height: 500rpx;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f2f3f5;
  
  &:last-child {
    border-bottom: none;
  }
  
  .info {
    flex: 1;
    
    .time {
      font-size: 28rpx;
      font-weight: 500;
      color: #1d2129;
      display: block;
      margin-bottom: 8rpx;
    }
    
    .detail {
      font-size: 24rpx;
      color: #86909c;
    }
  }
  
  .delete-btn {
    padding: 8rpx 16rpx;
    font-size: 24rpx;
    color: #f53f3f;
    background: rgba(245, 63, 63, 0.1);
    border-radius: 8rpx;
    border: none;
    margin: 0;
  }
}

.empty-state {
  text-align: center;
  padding: 64rpx 0;
  color: #86909c;
  
  .icon {
    font-size: 80rpx;
    display: block;
    margin-bottom: 16rpx;
  }
  
  .text {
    font-size: 28rpx;
  }
}

.about {
  margin-top: 16rpx;
  
  .about-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #1d2129;
    margin-bottom: 24rpx;
    display: block;
  }
  
  .about-content {
    font-size: 24rpx;
    color: #4e5969;
    line-height: 1.8;
    white-space: pre-line;
  }
}

.btn-primary {
  height: 80rpx;
  border-radius: 48rpx;
  font-size: 30rpx;
  font-weight: 600;
  background: linear-gradient(135deg, #165dff 0%, #4080ff 100%);
  color: #ffffff;
  margin-top: 16rpx;
  border: none;
  width: 100%;
  
  &:active {
    opacity: 0.9;
    transform: scale(0.98);
  }
}

.clear-btn {
  font-size: 24rpx;
  color: #f53f3f;
  padding: 8rpx 16rpx;
  margin: 0;
  border: none;
  background: transparent;
  
  &:active {
    opacity: 0.7;
  }
}
</style>
