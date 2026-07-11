import React, { useState, useEffect } from 'react'
import { View, Text, Input, Button, ScrollView, Switch } from '@tarojs/components'
import Taro from '@tarojs/taro'
import styles from './index.module.scss'
import { useLoadingStore } from '../../store/loading'
import { loadingApi } from '../../services/loading'
import type { Cargo, Truck } from '../../types'

export default function HomePage() {
  const { cargoList, truck, setCargoList, addCargo, removeCargo, clearCargoList, setTruck, setLoadingResult, setIsLoading } = useLoadingStore()
  
  const [form, setForm] = useState<Cargo>({
    name: '',
    length: 100,
    width: 50,
    height: 80,
    weight: 50,
    quantity: 5
  })
  
  const [inputLength, setInputLength] = useState(truck.length.toString())
  const [inputWidth, setInputWidth] = useState(truck.width.toString())
  const [inputHeight, setInputHeight] = useState(truck.height.toString())
  const [inputMaxWeight, setInputMaxWeight] = useState(truck.maxWeight.toString())

  useEffect(() => {
    loadingApi.getDefaultTruck().then(defaultTruck => {
      setTruck(defaultTruck)
      setInputLength(defaultTruck.length.toString())
      setInputWidth(defaultTruck.width.toString())
      setInputHeight(defaultTruck.height.toString())
      setInputMaxWeight(defaultTruck.maxWeight.toString())
    }).catch(() => {
      console.log('使用默认车厢参数')
    })
  }, [])

  const handleAddCargo = () => {
    if (!form.name.trim()) {
      Taro.showToast({ title: '请输入货物名称', icon: 'none' })
      return
    }
    addCargo({
      ...form,
      id: Date.now()
    })
    Taro.showToast({ title: '添加成功', icon: 'success' })
    setForm({
      name: '',
      length: 100,
      width: 50,
      height: 80,
      weight: 50,
      quantity: 5
    })
  }

  const handleRemoveCargo = (index: number) => {
    Taro.showModal({
      title: '确认删除',
      content: '确定要删除这件货物吗？',
      success: (res) => {
        if (res.confirm) {
          removeCargo(index)
          Taro.showToast({ title: '删除成功', icon: 'success' })
        }
      }
    })
  }

  const handleClearCargo = () => {
    Taro.showModal({
      title: '确认清空',
      content: '确定要清空所有货物吗？',
      success: (res) => {
        if (res.confirm) {
          clearCargoList()
          Taro.showToast({ title: '已清空', icon: 'success' })
        }
      }
    })
  }

  const handleCalculate = async () => {
    if (cargoList.length === 0) {
      Taro.showToast({ title: '请先添加货物', icon: 'none' })
      return
    }

    const updatedTruck: Truck = {
      length: parseFloat(inputLength) || 1200,
      width: parseFloat(inputWidth) || 240,
      height: parseFloat(inputHeight) || 260,
      maxWeight: parseFloat(inputMaxWeight) || 20000
    }
    setTruck(updatedTruck)
    setIsLoading(true)

    try {
      const result = await loadingApi.calculate({
        truck: updatedTruck,
        cargoList
      })
      setLoadingResult(result)
      
      if (result.success) {
        Taro.showToast({ title: '计算完成', icon: 'success' })
        Taro.switchTab({ url: '/pages/result/index' })
      } else {
        Taro.showToast({ title: '部分货物未装载', icon: 'none' })
        Taro.switchTab({ url: '/pages/result/index' })
      }
    } catch (error) {
      console.error('计算失败:', error)
      Taro.showToast({ title: '计算失败，请检查后端服务', icon: 'none' })
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
    Taro.showToast({ title: '示例数据已加载', icon: 'success' })
  }

  return (
    <ScrollView className={styles.page} scrollY>
      <View className={styles.header}>
        <Text className={styles.title}>🚚 智能装车系统</Text>
        <Text className={styles.subtitle}>多起点最佳适配天际线算法</Text>
      </View>

      <View className={styles.card}>
        <View className={styles.cardTitle}>
          <Text className={styles.icon}>📦</Text>
          <Text>添加货物</Text>
        </View>
        
        <View className={styles.formItem}>
          <Text className={styles.label}>货物名称</Text>
          <Input 
            className={styles.input} 
            placeholder="请输入货物名称"
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.detail.value })}
          />
        </View>
        
        <View className={styles.formItem}>
          <Text className={styles.label}>尺寸 (cm)</Text>
          <View className={styles.inputRow}>
            <Input 
              className={styles.input} 
              placeholder="长"
              type="digit"
              value={form.length.toString()}
              onChange={(e) => setForm({ ...form, length: parseFloat(e.detail.value) || 0 })}
            />
            <Input 
              className={styles.input} 
              placeholder="宽"
              type="digit"
              value={form.width.toString()}
              onChange={(e) => setForm({ ...form, width: parseFloat(e.detail.value) || 0 })}
            />
            <Input 
              className={styles.input} 
              placeholder="高"
              type="digit"
              value={form.height.toString()}
              onChange={(e) => setForm({ ...form, height: parseFloat(e.detail.value) || 0 })}
            />
          </View>
        </View>
        
        <View className={styles.formItem}>
          <Text className={styles.label}>重量 (kg)</Text>
          <Input 
            className={styles.input} 
            placeholder="请输入重量"
            type="digit"
            value={form.weight.toString()}
            onChange={(e) => setForm({ ...form, weight: parseFloat(e.detail.value) || 0 })}
          />
        </View>
        
        <View className={styles.formItem}>
          <Text className={styles.label}>数量</Text>
          <Input 
            className={styles.input} 
            placeholder="请输入数量"
            type="number"
            value={form.quantity.toString()}
            onChange={(e) => setForm({ ...form, quantity: parseInt(e.detail.value) || 1 })}
          />
        </View>
        
        <Button className={styles.btnPrimary} onClick={handleAddCargo}>
          添加货物
        </Button>
      </View>

      <View className={styles.card}>
        <View className={styles.cardTitle}>
          <Text className={styles.icon}>📋</Text>
          <Text>货物列表 ({cargoList.length})</Text>
          <Button className={styles.clearBtn} onClick={handleClearCargo} disabled={cargoList.length === 0}>
            清空
          </Button>
        </View>
        
        {cargoList.length === 0 ? (
          <View className={styles.emptyState}>
            <Text className={styles.icon}>📭</Text>
            <Text className={styles.text}>暂无货物，请添加货物</Text>
          </View>
        ) : (
          <ScrollView className={styles.cargoList} scrollY>
            {cargoList.map((item, index) => (
              <View key={item.id || index} className={styles.cargoItem}>
                <View className={styles.info}>
                  <Text className={styles.name}>{item.name}</Text>
                  <Text className={styles.detail}>
                    {item.length}×{item.width}×{item.height}cm | {item.weight}kg × {item.quantity}
                  </Text>
                </View>
                <Button className={styles.deleteBtn} onClick={() => handleRemoveCargo(index)}>
                  删除
                </Button>
              </View>
            ))}
          </ScrollView>
        )}
      </View>

      <View className={styles.card}>
        <View className={styles.cardTitle}>
          <Text className={styles.icon}>🚛</Text>
          <Text>车厢参数</Text>
        </View>
        
        <View className={styles.truckConfig}>
          <View className={styles.configRow}>
            <View className={styles.configItem}>
              <Text className={styles.label}>长度 (cm)</Text>
              <Input 
                className={styles.input} 
                type="digit"
                value={inputLength}
                onChange={(e) => setInputLength(e.detail.value)}
              />
            </View>
            <View className={styles.configItem}>
              <Text className={styles.label}>宽度 (cm)</Text>
              <Input 
                className={styles.input} 
                type="digit"
                value={inputWidth}
                onChange={(e) => setInputWidth(e.detail.value)}
              />
            </View>
          </View>
          <View className={styles.configRow}>
            <View className={styles.configItem}>
              <Text className={styles.label}>高度 (cm)</Text>
              <Input 
                className={styles.input} 
                type="digit"
                value={inputHeight}
                onChange={(e) => setInputHeight(e.detail.value)}
              />
            </View>
            <View className={styles.configItem}>
              <Text className={styles.label}>最大载重 (kg)</Text>
              <Input 
                className={styles.input} 
                type="digit"
                value={inputMaxWeight}
                onChange={(e) => setInputMaxWeight(e.detail.value)}
              />
            </View>
          </View>
        </View>
      </View>

      <Button 
        className={`${styles.btnPrimary} ${cargoList.length === 0 && styles.btnDisabled}`} 
        onClick={handleCalculate}
        loading={useLoadingStore((state) => state.isLoading)}
        disabled={cargoList.length === 0}
      >
        开始装车
      </Button>
      
      <Button className={styles.btnSecondary} onClick={loadSampleData}>
        加载示例数据
      </Button>
    </ScrollView>
  )
}
