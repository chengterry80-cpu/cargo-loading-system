<template>
  <view class="page">
    <view class="header">
      <text class="title">🎨 装车可视化</text>
      <text class="subtitle">第 {{ currentTruckIndex + 1 }} / {{ totalTrucks }} 辆车</text>
    </view>

    <view class="canvas-wrapper">
      <canvas 
        type="webgl" 
        id="truckCanvas" 
        class="truck-canvas"
      />
    </view>

    <view class="controls-bar">
      <button 
        class="nav-btn" 
        @click="handlePrev"
        :disabled="currentTruckIndex === 0"
      >◀ 上一辆</button>
      <view class="indicators">
        <view 
          v-for="(t, i) in totalTrucks" 
          :key="i"
          class="indicator"
          :class="{ active: i === currentTruckIndex }"
          @click="handleSwitch(i)"
        ></view>
      </view>
      <button 
        class="nav-btn" 
        @click="handleNext"
        :disabled="currentTruckIndex === totalTrucks - 1"
      >下一辆 ▶</button>
    </view>

    <view class="stats-bar">
      <view class="stat">
        <text class="stat-val">{{ currentPlacedItems.length }}</text>
        <text class="stat-label">件数</text>
      </view>
      <view class="stat">
        <text class="stat-val">{{ totalWeight }}</text>
        <text class="stat-label">kg</text>
      </view>
      <view class="stat">
        <text class="stat-val">{{ utilizationRate }}%</text>
        <text class="stat-label">利用率</text>
      </view>
    </view>

    <view class="detail-panel">
      <view class="panel-title">货物详情</view>
      <scroll-view class="detail-list" scroll-y>
        <view v-for="(item, index) in currentPlacedItems" :key="item.name + index" class="detail-item">
          <view class="item-color" :style="{ background: getColor(index) }"></view>
          <view class="item-info">
            <text class="item-name">{{ item.name }}</text>
            <text class="item-pos">位置: ({{ (item.x || 0).toFixed(1) }}, {{ (item.y || 0).toFixed(1) }}, {{ (item.z || 0).toFixed(1) }})</text>
            <text class="item-size">{{ item.length }}×{{ item.width }}×{{ item.height }}cm</text>
          </view>
        </view>
        <view v-if="currentPlacedItems.length === 0" class="empty">暂无装载货物</view>
      </scroll-view>
    </view>

    <button class="btn-back" @click="handleBack">返回结果页</button>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useLoadingStore } from '@/store/loading'
import { storeToRefs } from 'pinia'
import type { PlacedItem } from '@/types'
import createScopedThreejs from 'threejs-miniprogram'

const store = useLoadingStore()
const { loadingResult } = storeToRefs(store)

const currentTruckIndex = ref(0)

let scene: any = null
let camera: any = null
let renderer: any = null
let animationFrameId: number | null = null
let cargoMeshes: any[] = []
let truckLine: any = null
let THREE: any = null

const totalTrucks = computed(() => {
  if (!loadingResult.value || !loadingResult.value.truckResults) return 0
  return loadingResult.value.truckResults.length
})

const currentTruck = computed(() => {
  if (!loadingResult.value || !loadingResult.value.truckResults) return null
  return loadingResult.value.truckResults[currentTruckIndex.value] || null
})

const currentPlacedItems = computed(() => {
  const truck = currentTruck.value
  if (!truck || !truck.placedItems) return []
  return truck.placedItems
})

const totalWeight = computed(() => {
  const truck = currentTruck.value
  return truck ? (truck.totalWeight || 0).toFixed(0) : '0'
})

const utilizationRate = computed(() => {
  const truck = currentTruck.value
  return truck ? (truck.utilizationRate || 0).toFixed(1) : '0'
})

const colors = [
  0xff6b6b, 0x4ecdc4, 0x45b7d1, 0x96ceb4, 0xffeaa7,
  0xff9800, 0xe74c3c, 0x9b59b6, 0x3498db, 0x1abc9c,
  0xf39c12, 0xd35400, 0xc0392b, 0x8e44ad, 0x2980b9
]

const getColor = (index: number) => {
  const hex = colors[index % colors.length]
  return '#' + hex.toString(16).padStart(6, '0')
}

const initThree = () => {
  const query = uni.createSelectorQuery()
  query.select('#truckCanvas')
    .fields({ node: true, size: true })
    .exec((res) => {
      if (!res || !res[0] || !res[0].node) {
        console.warn('Canvas node not found, retrying...')
        setTimeout(initThree, 200)
        return
      }

      const canvas = res[0].node
      const dpr = uni.getSystemInfoSync().pixelRatio
      const width = res[0].width
      const height = res[0].height

      THREE = createScopedThreejs(canvas)

      scene = new THREE.Scene()
      scene.background = new THREE.Color(0xf0f2f5)

      camera = new THREE.PerspectiveCamera(60, width / height, 0.1, 10000)
      camera.position.set(1500, 800, 1500)

      renderer = new THREE.WebGLRenderer({ canvas, antialias: true })
      renderer.setSize(width, height)
      renderer.setPixelRatio(dpr)

      const ambientLight = new THREE.AmbientLight(0xffffff, 0.6)
      scene.add(ambientLight)

      const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8)
      directionalLight.position.set(500, 1000, 500)
      scene.add(directionalLight)

      render()
      updateVisualization()
    })
}

const updateVisualization = () => {
  if (!scene || !camera || !THREE) return

  cargoMeshes.forEach(mesh => {
    scene.remove(mesh)
    mesh.geometry.dispose()
    mesh.material.dispose()
  })
  cargoMeshes = []

  if (truckLine) {
    scene.remove(truckLine)
    truckLine.geometry.dispose()
    truckLine.material.dispose()
    truckLine = null
  }

  const truck = currentTruck.value
  if (!truck) return

  const placedItems = truck.placedItems || []
  
  const truckLength = 1200
  const truckWidth = 240
  const truckHeight = 260

  const truckGeometry = new THREE.BoxGeometry(truckLength, truckHeight, truckWidth)
  const edges = new THREE.EdgesGeometry(truckGeometry)
  const lineMaterial = new THREE.LineBasicMaterial({ color: 0x2c3e50 })
  truckLine = new THREE.LineSegments(edges, lineMaterial)
  truckLine.position.set(truckLength / 2, truckHeight / 2, truckWidth / 2)
  scene.add(truckLine)

  placedItems.forEach((item: PlacedItem, index: number) => {
    const cargoGeometry = new THREE.BoxGeometry(item.length, item.height, item.width)
    const color = colors[index % colors.length]
    const material = new THREE.MeshPhongMaterial({
      color,
      transparent: true,
      opacity: 0.85,
      shininess: 60
    })
    const mesh = new THREE.Mesh(cargoGeometry, material)
    mesh.position.set(
      item.x + item.length / 2,
      item.y + item.height / 2,
      item.z + item.width / 2
    )

    const edgeGeometry = new THREE.EdgesGeometry(cargoGeometry)
    const edgeMaterial = new THREE.LineBasicMaterial({ color: 0x34495e })
    const edge = new THREE.LineSegments(edgeGeometry, edgeMaterial)
    mesh.add(edge)

    scene.add(mesh)
    cargoMeshes.push(mesh)
  })

  const centerX = truckLength / 2
  const centerY = truckHeight / 2
  const centerZ = truckWidth / 2

  const distance = Math.max(truckLength, truckWidth) * 1.5
  const height = truckHeight * 1.2
  camera.position.set(centerX + distance, centerY + height, centerZ + distance)
  camera.lookAt(centerX, centerY, centerZ)
  camera.updateProjectionMatrix()
}

const render = () => {
  if (!renderer || !scene || !camera) return
  renderer.render(scene, camera)
  animationFrameId = requestAnimationFrame(render)
}

const stopRender = () => {
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId)
    animationFrameId = null
  }
}

const handlePrev = () => {
  if (currentTruckIndex.value > 0) {
    currentTruckIndex.value--
    updateVisualization()
  }
}

const handleNext = () => {
  if (currentTruckIndex.value < totalTrucks.value - 1) {
    currentTruckIndex.value++
    updateVisualization()
  }
}

const handleSwitch = (index: number) => {
  currentTruckIndex.value = index
  updateVisualization()
}

const handleBack = () => {
  stopRender()
  uni.navigateBack()
}

watch([loadingResult, currentTruckIndex], () => {
  if (THREE) {
    updateVisualization()
  }
}, { deep: true })

onMounted(() => {
  setTimeout(initThree, 300)
})

onUnmounted(() => {
  stopRender()
  if (renderer) {
    renderer.dispose()
  }
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
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
    margin-bottom: 12rpx;
  }
  
  .subtitle {
    font-size: 24rpx;
    color: rgba(255, 255, 255, 0.6);
    display: block;
  }
}

.canvas-wrapper {
  margin: 0 24rpx;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16rpx;
  padding: 16rpx;
  
  .truck-canvas {
    width: 100%;
    height: 500rpx;
    border-radius: 12rpx;
    background: #f0f2f5;
  }
}

.controls-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 32rpx;
  
  .nav-btn {
    flex: 1;
    height: 72rpx;
    border-radius: 36rpx;
    font-size: 26rpx;
    font-weight: 600;
    background: rgba(255, 255, 255, 0.1);
    color: #ffffff;
    border: 1rpx solid rgba(255, 255, 255, 0.2);
    
    &[disabled] {
      opacity: 0.4;
    }
  }
  
  .indicators {
    display: flex;
    gap: 12rpx;
    margin: 0 20rpx;
    
    .indicator {
      width: 16rpx;
      height: 16rpx;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.3);
      
      &.active {
        background: #4ecdc4;
      }
    }
  }
}

.stats-bar {
  display: flex;
  justify-content: space-around;
  padding: 24rpx;
  margin: 0 24rpx;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16rpx;
  
  .stat {
    text-align: center;
    
    .stat-val {
      font-size: 36rpx;
      font-weight: 700;
      color: #4ecdc4;
      display: block;
      margin-bottom: 4rpx;
    }
    
    .stat-label {
      font-size: 22rpx;
      color: rgba(255, 255, 255, 0.6);
    }
  }
}

.detail-panel {
  margin: 24rpx;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16rpx;
  overflow: hidden;
  
  .panel-title {
    padding: 24rpx;
    font-size: 28rpx;
    font-weight: 600;
    color: #ffffff;
    border-bottom: 1rpx solid rgba(255, 255, 255, 0.1);
  }
  
  .detail-list {
    max-height: 400rpx;
    padding: 16rpx;
  }
  
  .detail-item {
    display: flex;
    gap: 16rpx;
    padding: 16rpx;
    border-bottom: 1rpx solid rgba(255, 255, 255, 0.08);
    
    &:last-child {
      border-bottom: none;
    }
    
    .item-color {
      width: 48rpx;
      height: 48rpx;
      border-radius: 8rpx;
      flex-shrink: 0;
    }
    
    .item-info {
      flex: 1;
      
      .item-name {
        font-size: 28rpx;
        font-weight: 600;
        color: #ffffff;
        display: block;
        margin-bottom: 8rpx;
      }
      
      .item-pos, .item-size {
        font-size: 22rpx;
        color: rgba(255, 255, 255, 0.5);
        display: block;
        margin-bottom: 4rpx;
      }
    }
  }
  
  .empty {
    text-align: center;
    padding: 48rpx;
    color: rgba(255, 255, 255, 0.4);
    font-size: 26rpx;
  }
}

.btn-back {
  position: fixed;
  bottom: 32rpx;
  left: 24rpx;
  right: 24rpx;
  height: 88rpx;
  border-radius: 44rpx;
  font-size: 32rpx;
  font-weight: 600;
  background: linear-gradient(135deg, #4ecdc4 0%, #45b7d1 100%);
  color: #ffffff;
  border: none;
}
</style>