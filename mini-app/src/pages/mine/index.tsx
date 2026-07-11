import React, { useState, useEffect } from 'react'
import { View, Text, Button, ScrollView } from '@tarojs/components'
import Taro from '@tarojs/taro'
import styles from './index.module.scss'
import { loadingApi } from '../../services/loading'
import type { LoadingHistory } from '../../types'

export default function MinePage() {
  const [historyList, setHistoryList] = useState<LoadingHistory[]>([])
  const [isLoading, setIsLoading] = useState(false)

  useEffect(() => {
    loadHistory()
  }, [])

  const loadHistory = async () => {
    setIsLoading(true)
    try {
      const history = await loadingApi.getHistory()
      setHistoryList(history)
    } catch (error) {
      console.error('加载历史记录失败:', error)
    } finally {
      setIsLoading(false)
    }
  }

  const handleDeleteHistory = (id: number) => {
    Taro.showModal({
      title: '确认删除',
      content: '确定要删除这条历史记录吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await loadingApi.deleteHistory(id)
            Taro.showToast({ title: '删除成功', icon: 'success' })
            loadHistory()
          } catch (error) {
            Taro.showToast({ title: '删除失败', icon: 'none' })
          }
        }
      }
    })
  }

  const handleClearAllHistory = () => {
    Taro.showModal({
      title: '确认清空',
      content: '确定要清空所有历史记录吗？',
      success: async (res) => {
        if (res.confirm) {
          for (const history of historyList) {
            try {
              await loadingApi.deleteHistory(history.id)
            } catch {
              // 继续删除其他记录
            }
          }
          Taro.showToast({ title: '已清空', icon: 'success' })
          setHistoryList([])
        }
      }
    })
  }

  const handleGoHome = () => {
    Taro.switchTab({ url: '/pages/home/index' })
  }

  const formatDate = (dateStr: string) => {
    try {
      const date = new Date(dateStr)
      return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
    } catch {
      return dateStr
    }
  }

  return (
    <ScrollView className={styles.page} scrollY>
      <View className={styles.header}>
        <Text className={styles.title}>👤 我的</Text>
        <Text className={styles.subtitle}>装车记录与设置</Text>
      </View>

      <View className={styles.card}>
        <View className={styles.cardTitle}>
          <Text className={styles.icon}>📜</Text>
          <Text>历史记录 ({historyList.length})</Text>
          {historyList.length > 0 && (
            <Button className={styles.deleteBtn} onClick={handleClearAllHistory}>
              清空
            </Button>
          )}
        </View>
        
        {isLoading ? (
          <View className={styles.emptyState}>
            <Text className={styles.text}>加载中...</Text>
          </View>
        ) : historyList.length === 0 ? (
          <View className={styles.emptyState}>
            <Text className={styles.icon}>📭</Text>
            <Text className={styles.text}>暂无历史记录</Text>
          </View>
        ) : (
          <ScrollView className={styles.historyList} scrollY>
            {historyList.map((item) => (
              <View key={item.id} className={styles.historyItem}>
                <View className={styles.info}>
                  <Text className={styles.time}>{formatDate(item.createTime)}</Text>
                  <Text className={styles.detail}>
                    {item.cargoList.length}件货物 → {item.result.totalTrucksUsed}辆车
                  </Text>
                </View>
                <Button className={styles.deleteBtn} onClick={() => handleDeleteHistory(item.id)}>
                  删除
                </Button>
              </View>
            ))}
          </ScrollView>
        )}
      </View>

      <View className={styles.card}>
        <View className={styles.about}>
          <Text className={styles.aboutTitle}>📝 关于系统</Text>
          <Text className={styles.aboutContent}>
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
- 前端：Taro + React + TypeScript
- 支持微信/支付宝/抖音小程序
          </Text>
        </View>
      </View>

      <View className={styles.card}>
        <Button className={styles.btnPrimary} onClick={handleGoHome}>
          开始装车
        </Button>
      </View>
    </ScrollView>
  )
}
