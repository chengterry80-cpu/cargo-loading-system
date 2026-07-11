import React from 'react'
import { View, Text, Button, ScrollView } from '@tarojs/components'
import Taro from '@tarojs/taro'
import styles from './index.module.scss'
import { useLoadingStore } from '../../store/loading'

export default function ResultPage() {
  const { loadingResult, cargoList } = useLoadingStore()

  const getProgressColor = (rate: number) => {
    if (rate > 70) return 'high'
    if (rate > 50) return 'medium'
    return 'low'
  }

  const handleRetry = () => {
    Taro.switchTab({ url: '/pages/home/index' })
  }

  if (!loadingResult) {
    return (
      <ScrollView className={styles.page} scrollY>
        <View className={styles.emptyState}>
          <Text className={styles.icon}>📊</Text>
          <Text className={styles.title}>暂无装车结果</Text>
          <Text className={styles.subtitle}>请先添加货物并点击"开始装车"</Text>
          <Button className={styles.btn} onClick={handleRetry}>
            去装车
          </Button>
        </View>
      </ScrollView>
    )
  }

  const totalPlacedItems = loadingResult.truckResults?.reduce((sum, truck) => sum + truck.placedItems.length, 0) || 0
  const totalWeight = loadingResult.truckResults?.reduce((sum, truck) => sum + truck.totalWeight, 0) || 0
  const avgUtilization = loadingResult.truckResults && loadingResult.truckResults.length > 0
    ? loadingResult.truckResults.reduce((sum, truck) => sum + truck.utilizationRate, 0) / loadingResult.truckResults.length
    : 0

  return (
    <ScrollView className={styles.page} scrollY>
      <View className={styles.header}>
        <Text className={styles.title}>📊 装车结果</Text>
        <Text className={styles.subtitle}>共使用 {loadingResult.totalTrucksUsed} 辆车</Text>
      </View>

      <View className={styles.card}>
        <View className={styles.resultHeader}>
          <Text className={styles.cardTitle}>
            <Text className={styles.icon}>✅</Text>
            <Text>装载概览</Text>
          </Text>
          <Text className={`${styles.status} ${loadingResult.success ? 'success' : 'warning'}`}>
            {loadingResult.success ? '全部装载' : '部分装载'}
          </Text>
        </View>
        
        <View className={styles.statistics}>
          <View className={styles.statItem}>
            <Text className={styles.value}>{totalPlacedItems}</Text>
            <Text className={styles.label}>已装载件数</Text>
          </View>
          <View className={styles.statItem}>
            <Text className={styles.value}>{totalWeight.toFixed(0)}</Text>
            <Text className={styles.label}>总重量 (kg)</Text>
          </View>
          <View className={styles.statItem}>
            <Text className={styles.value}>{loadingResult.totalTrucksUsed}</Text>
            <Text className={styles.label}>使用车辆</Text>
          </View>
        </View>
        
        <View className={styles.progressContainer}>
          <View className={styles.label}>
            <Text>平均空间利用率</Text>
            <Text className={styles.rate}>{avgUtilization.toFixed(1)}%</Text>
          </View>
          <View className={styles.progressBar}>
            <View className={`${styles.progressFill} ${getProgressColor(avgUtilization)}`} style={{ width: `${Math.min(avgUtilization, 100)}%` }} />
          </View>
        </View>
      </View>

      <View className={styles.card}>
        <Text className={styles.cardTitle}>
          <Text className={styles.icon}>🚛</Text>
          <Text>车辆详情</Text>
        </Text>
        
        {loadingResult.truckResults?.length > 0 ? (
          <View className={styles.truckList}>
            {loadingResult.truckResults.map((truck, index) => (
              <View key={index} className={styles.truckItem}>
                <View className={styles.truckHeader}>
                  <Text className={styles.truckNum}>第 {truck.truckNumber} 辆车</Text>
                  <Text className={styles.truckUtilization}>{truck.utilizationRate.toFixed(1)}%</Text>
                </View>
                <View className={styles.truckStats}>
                  <Text className={styles.stat}>重量: {truck.totalWeight.toFixed(0)}kg</Text>
                  <Text className={styles.stat}>体积: {(truck.totalVolume / 1000000).toFixed(2)}m³</Text>
                  <Text className={styles.stat}>装载: {truck.placedItems.length}件</Text>
                </View>
                {truck.placedItems.length > 0 && (
                  <View className={styles.placedItems}>
                    {truck.placedItems.slice(0, 5).map((item, idx) => (
                      <View key={idx} className={styles.placedItem}>
                        <Text>{item.name}</Text>
                        <Text>位置: ({item.x.toFixed(0)}, {item.y.toFixed(0)})</Text>
                      </View>
                    ))}
                    {truck.placedItems.length > 5 && (
                      <Text className={styles.placedItem}>
                        <Text>...</Text>
                        <Text>还有 {truck.placedItems.length - 5} 件</Text>
                      </Text>
                    )}
                  </View>
                )}
              </View>
            ))}
          </View>
        ) : (
          <View className={styles.emptyState}>
            <Text className={styles.text}>暂无车辆数据</Text>
          </View>
        )}
      </View>

      {loadingResult.unplacedItems && loadingResult.unplacedItems.length > 0 && (
        <View className={styles.card}>
          <View className={styles.unplacedSection}>
            <Text className={styles.sectionTitle}>❌ 未装载货物 ({loadingResult.unplacedItems.length})</Text>
            {loadingResult.unplacedItems.map((item, index) => (
              <View key={index} className={styles.unplacedItem}>
                <Text className={styles.name}>{item.name}</Text>
                <Text className={styles.quantity}>×{item.quantity}</Text>
              </View>
            ))}
          </View>
        </View>
      )}

      <Button className={styles.btnPrimary} onClick={handleRetry}>
        重新计算
      </Button>
    </ScrollView>
  )
}
