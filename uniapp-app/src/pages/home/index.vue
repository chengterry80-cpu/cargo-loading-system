<template>
  <view class="page">
    <view class="header">
      <text class="title">🚚 智能装车系统</text>
      <text class="subtitle">多起点最佳适配天际线算法</text>
    </view>

    <view class="card">
      <view class="card-title">
        <text class="icon">📦</text>
        <text>添加货物</text>
      </view>
      
      <view class="form-item">
        <text class="label">货物名称</text>
        <input 
          class="input" 
          placeholder="请输入货物名称"
          v-model="form.name"
        />
      </view>
      
      <view class="form-item">
        <text class="label">尺寸 (cm)</text>
        <view class="input-row">
          <input 
            class="input" 
            placeholder="长"
            type="digit"
            v-model="form.length"
          />
          <input 
            class="input" 
            placeholder="宽"
            type="digit"
            v-model="form.width"
          />
          <input 
            class="input" 
            placeholder="高"
            type="digit"
            v-model="form.height"
          />
        </view>
      </view>
      
      <view class="form-item">
        <text class="label">重量 (kg)</text>
        <input 
          class="input" 
          placeholder="请输入重量"
          type="digit"
          v-model="form.weight"
        />
      </view>
      
      <view class="form-item">
        <text class="label">数量</text>
        <input 
          class="input" 
          placeholder="请输入数量"
          type="number"
          v-model="form.quantity"
        />
      </view>
      
      <button class="btn-primary" @click="handleAddCargo">添加货物</button>
    </view>

    <view class="card">
      <view class="card-title">
        <text class="icon">📋</text>
        <text>货物列表 ({{ cargoList.length }})</text>
        <button class="clear-btn" @click="handleClearCargo" :disabled="cargoList.length === 0">清空</button>
      </view>
      
      <view v-if="cargoList.length === 0" class="empty-state">
        <text class="icon">📭</text>
        <text class="text">暂无货物，请添加货物</text>
      </view>
      
      <scroll-view v-else class="cargo-list" scroll-y>
        <view v-for="(item, index) in cargoList" :key="item.id || index" class="cargo-item">
          <view class="info">
            <text class="name">{{ item.name }}</text>
            <text class="detail">
              {{ item.length }}×{{ item.width }}×{{ item.height }}cm | {{ item.weight }}kg × {{ item.quantity }}
            </text>
          </view>
          <button class="delete-btn" @click="handleRemoveCargo(index)">删除</button>
        </view>
      </scroll-view>
    </view>

    <view class="card">
      <view class="card-title">
        <text class="icon">🚛</text>
        <text>车厢参数</text>
      </view>
      
      <view class="truck-config">
        <view class="config-row">
          <view class="config-item">
            <text class="label">长度</text>
            <view class="input-wrap">
              <input 
                class="input" 
                type="digit"
                v-model="inputLength"
              />
              <text class="unit">cm</text>
            </view>
          </view>
          <view class="config-item">
            <text class="label">宽度</text>
            <view class="input-wrap">
              <input 
                class="input" 
                type="digit"
                v-model="inputWidth"
              />
              <text class="unit">cm</text>
            </view>
          </view>
        </view>
        <view class="config-row">
          <view class="config-item">
            <text class="label">高度</text>
            <view class="input-wrap">
              <input 
                class="input" 
                type="digit"
                v-model="inputHeight"
              />
              <text class="unit">cm</text>
            </view>
          </view>
          <view class="config-item">
            <text class="label">最大车辆数</text>
            <view class="input-wrap">
              <input 
                class="input" 
                type="number"
                v-model="maxTrucks"
              />
              <text class="unit">辆</text>
            </view>
          </view>
        </view>
        <view class="config-row single">
          <view class="config-item full">
            <text class="label">最大载重</text>
            <view class="input-wrap">
              <input 
                class="input" 
                type="digit"
                v-model="inputMaxWeight"
              />
              <text class="unit">kg</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <button 
      class="btn-primary btn-large" 
      @click="handleCalculate"
      :loading="isLoading"
      :disabled="cargoList.length === 0"
    >
      开始装车
    </button>
    
    <button class="btn-secondary" @click="loadSampleData">加载示例数据</button>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useLoadingStore } from '@/store/loading'
import { loadingApi } from '@/services/loading'
import type { Cargo, Truck } from '@/types'

import { storeToRefs } from 'pinia'

const store = useLoadingStore()
const { cargoList, truck, isLoading } = storeToRefs(store)
const { setCargoList, addCargo, removeCargo, clearCargoList, setTruck, setLoadingResult, setIsLoading } = store

const form = ref<Cargo>({
  name: '',
  length: 100,
  width: 50,
  height: 80,
  weight: 50,
  quantity: 5
})

const inputLength = ref(truck.value.length.toString())
const inputWidth = ref(truck.value.width.toString())
const inputHeight = ref(truck.value.height.toString())
const inputMaxWeight = ref(truck.value.maxWeight.toString())
const maxTrucks = ref('3')

onMounted(() => {
  loadingApi.getDefaultTruck().then((defaultTruck) => {
    inputLength.value = defaultTruck.length.toString()
    inputWidth.value = defaultTruck.width.toString()
    inputHeight.value = defaultTruck.height.toString()
    inputMaxWeight.value = defaultTruck.maxWeight.toString()
  }).catch(() => {
    console.log('使用默认车厢参数')
  })
})

const handleAddCargo = () => {
  if (!form.value.name.trim()) {
    uni.showToast({ title: '请输入货物名称', icon: 'none' })
    return
  }
  addCargo({
    ...form.value,
    id: Date.now()
  })
  uni.showToast({ title: '添加成功', icon: 'success' })
  form.value = {
    name: '',
    length: 100,
    width: 50,
    height: 80,
    weight: 50,
    quantity: 5
  }
}

const handleRemoveCargo = (index: number) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这件货物吗？',
    success: (res) => {
      if (res.confirm) {
        removeCargo(index)
        uni.showToast({ title: '删除成功', icon: 'success' })
      }
    }
  })
}

const handleClearCargo = () => {
  uni.showModal({
    title: '确认清空',
    content: '确定要清空所有货物吗？',
    success: (res) => {
      if (res.confirm) {
        clearCargoList()
        uni.showToast({ title: '已清空', icon: 'success' })
      }
    }
  })
}

const handleCalculate = async () => {
  if (cargoList.value.length === 0) {
    uni.showToast({ title: '请先添加货物', icon: 'none' })
    return
  }

  const updatedTruck: Truck = {
    length: parseFloat(inputLength.value) || 1200,
    width: parseFloat(inputWidth.value) || 240,
    height: parseFloat(inputHeight.value) || 260,
    maxWeight: parseFloat(inputMaxWeight.value) || 20000
  }
  setTruck(updatedTruck)
  setIsLoading(true)

  try {
    const plainCargoList = JSON.parse(JSON.stringify(cargoList.value))
    const result = await loadingApi.calculate({
      truck: updatedTruck,
      cargoList: plainCargoList,
      truckCount: parseInt(maxTrucks.value) || 3
    })
    setLoadingResult(result)
    
    if (result.success) {
      uni.showToast({ title: '计算完成', icon: 'success' })
    } else {
      uni.showToast({ title: '部分货物未装载', icon: 'none' })
    }
    uni.switchTab({ url: '/pages/result/index' })
  } catch (error: any) {
    console.error('计算失败:', error)
    const errorMessage = error && error.message || '计算失败'
    const isTimeout = errorMessage.includes('超时')
    
    if (isTimeout) {
      uni.showModal({
        title: '请求超时',
        content: '网络连接超时，请检查：\n1. 后端服务是否启动\n2. 网络连接是否正常\n3. 是否开启了"不校验合法域名"',
        showCancel: false
      })
    } else {
      uni.showToast({ title: '计算失败，请检查后端服务', icon: 'none' })
    }
  } finally {
    setIsLoading(false)
  }
}

const loadSampleData = () => {
  const sampleData: Cargo[] = [
    { id: 1, name: '纸箱A', length: 80, width: 60, height: 50, weight: 20, quantity: 8 },
    { id: 2, name: '木箱B', length: 120, width: 80, height: 100, weight: 50, quantity: 5 },
    { id: 3, name: '货箱C', length: 100, width: 70, height: 60, weight: 35, quantity: 6 },
    { id: 4, name: '货物D', length: 60, width: 50, height: 40, weight: 15, quantity: 10 }
  ]
  setCargoList(sampleData)
  uni.showToast({ title: '示例数据已加载', icon: 'success' })
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
  
  .title {
    font-size: 40rpx;
    font-weight: 700;
    color: #ffffff;
    text-align: center;
    display: block;
    margin-bottom: 16rpx;
  }
  
  .subtitle {
    font-size: 24rpx;
    color: rgba(255, 255, 255, 0.6);
    text-align: center;
    display: block;
  }
}

.card {
  background: #ffffff;
  border-radius: 16rpx;
  margin: 24rpx 32rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
  max-width: calc(100% - 64rpx);
  overflow: hidden;
  box-sizing: border-box;
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

.form-item {
  margin-bottom: 24rpx;
  
  .label {
    font-size: 24rpx;
    color: #4e5969;
    margin-bottom: 8rpx;
    display: block;
  }
  
  .input {
    padding: 16rpx 24rpx;
    border: 1rpx solid #e5e6eb;
    border-radius: 12rpx;
    font-size: 28rpx;
    color: #1d2129;
    background: #f5f6f7;
  }
  
  .input-row {
    display: flex;
    gap: 16rpx;
    
    .input {
      flex: 1;
    }
  }
}

.cargo-list {
  max-height: 400rpx;
}

.cargo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f2f3f5;
  
  &:last-child {
    border-bottom: none;
  }
  
  .info {
    flex: 1;
    
    .name {
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

.truck-config {
  .config-row {
    display: flex;
    gap: 16rpx;
    margin-bottom: 20rpx;
    
    &.single {
      .config-item {
        flex: none;
        width: 100%;
      }
    }
    
    .config-item {
      flex: 1;
      
      .label {
        font-size: 24rpx;
        color: #4e5969;
        margin-bottom: 8rpx;
        display: block;
      }
      
      .input-wrap {
        display: flex;
        align-items: center;
        background: #f5f6f7;
        border: 1rpx solid #e5e6eb;
        border-radius: 12rpx;
        padding: 0 16rpx;
        
        .input {
          flex: 1;
          padding: 16rpx 0;
          font-size: 26rpx;
          color: #1d2129;
          background: transparent;
          border: none;
        }
        
        .unit {
          font-size: 22rpx;
          color: #86909c;
          margin-left: 8rpx;
        }
      }
    }
    
    .config-item.full {
      width: 100%;
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
  margin: 32rpx 32rpx 0;
  border: none;
  width: calc(100% - 64rpx);
  box-sizing: border-box;
  
  &:active {
    opacity: 0.9;
    transform: scale(0.98);
  }
  
  &.btn-large {
    height: 96rpx;
    font-size: 32rpx;
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
  box-sizing: border-box;
  
  &:active {
    opacity: 0.9;
    transform: scale(0.98);
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
