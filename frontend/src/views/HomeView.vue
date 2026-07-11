<template>
  <div class="home-view">
    <header class="header">
      <h1>🚚 智能货物装车算法可视化系统</h1>
    </header>
    
    <div class="main-content">
      <div class="left-panel">
        <CargoForm @add="handleAddCargo" />
        <CargoTable
          :cargo-list="cargoList"
          @delete="handleDeleteCargo"
          @clear="handleClearCargo"
        />
        
        <el-card shadow="hover" style="margin-top: 16px">
          <template #header>
            <div class="card-header">
              <span>车厢参数</span>
            </div>
          </template>
          <el-form :model="truck" label-width="100px" size="small">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="长度">
                  <el-input-number v-model="truck.length" :min="100" :max="2000" style="width: 100%" />
                  <div style="font-size: 12px; color: #999">cm</div>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="宽度">
                  <el-input-number v-model="truck.width" :min="50" :max="500" style="width: 100%" />
                  <div style="font-size: 12px; color: #999">cm</div>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="高度">
                  <el-input-number v-model="truck.height" :min="50" :max="500" style="width: 100%" />
                  <div style="font-size: 12px; color: #999">cm</div>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="最大载重">
                  <el-input-number v-model="truck.maxWeight" :min="100" :max="50000" style="width: 100%" />
                  <div style="font-size: 12px; color: #999">kg</div>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-card>

        <el-button
          type="primary"
          size="large"
          style="width: 100%; margin-top: 16px"
          :loading="loading"
          :disabled="cargoList.length === 0"
          @click="handleCalculate"
        >
          开始装车
        </el-button>

        <el-button
          size="large"
          style="width: 100%; margin-top: 8px"
          @click="loadSampleData"
        >
          加载示例数据
        </el-button>
      </div>

      <div class="right-panel">
        <div class="viewer-container">
          <ThreeViewer :truck="truck" :placed-items="result?.placedItems || []" />
        </div>
        <div class="result-container">
          <LoadingResult :result="result" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import CargoForm from '../components/CargoForm.vue'
import CargoTable from '../components/CargoTable.vue'
import LoadingResult from '../components/LoadingResult.vue'
import ThreeViewer from '../components/ThreeViewer.vue'
import { loadingApi } from '../api'
import type { Cargo, Truck, LoadingResult as LoadingResultType } from '../types'

const cargoList = ref<Cargo[]>([])
const truck = ref<Truck>({
  length: 1200,
  width: 240,
  height: 260,
  maxWeight: 20000
})
const result = ref<LoadingResultType | null>(null)
const loading = ref(false)

const handleAddCargo = (cargo: Cargo) => {
  cargoList.value.push(cargo)
}

const handleDeleteCargo = (index: number) => {
  cargoList.value.splice(index, 1)
}

const handleClearCargo = () => {
  cargoList.value = []
  result.value = null
}

const handleCalculate = async () => {
  if (cargoList.value.length === 0) {
    ElMessage.warning('请先添加货物')
    return
  }

  loading.value = true
  try {
    const res = await loadingApi.calculate({
      truck: truck.value,
      cargoList: cargoList.value
    })
    result.value = res
    ElMessage.success(res.message || '计算完成')
  } catch (error) {
    console.error(error)
    ElMessage.error('计算失败，请检查后端服务是否启动')
  } finally {
    loading.value = false
  }
}

const loadSampleData = () => {
  cargoList.value = [
    { id: 1, name: '纸箱A', length: 80, width: 60, height: 50, weight: 20, quantity: 8 },
    { id: 2, name: '木箱B', length: 120, width: 80, height: 100, weight: 50, quantity: 5 },
    { id: 3, name: '货箱C', length: 100, width: 70, height: 60, weight: 35, quantity: 6 },
    { id: 4, name: '货物D', length: 60, width: 50, height: 40, weight: 15, quantity: 10 }
  ]
  result.value = null
  ElMessage.success('示例数据已加载')
}

onMounted(async () => {
  try {
    const defaultTruck = await loadingApi.getDefaultTruck()
    truck.value = defaultTruck
  } catch {
    console.log('使用默认车厢参数')
  }
})
</script>

<style scoped>
.home-view {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.header {
  height: 60px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  padding: 0 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.header h1 {
  color: white;
  font-size: 24px;
  font-weight: 600;
}

.main-content {
  flex: 1;
  display: flex;
  padding: 16px;
  gap: 16px;
  overflow: hidden;
}

.left-panel {
  width: 400px;
  flex-shrink: 0;
  overflow-y: auto;
}

.right-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.viewer-container {
  flex: 1;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  min-height: 400px;
}

.result-container {
  flex-shrink: 0;
}

.card-header {
  font-weight: 600;
}
</style>
