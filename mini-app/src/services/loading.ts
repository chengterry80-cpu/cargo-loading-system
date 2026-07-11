import Taro from '@tarojs/taro'
import type { Truck, Cargo, LoadingRequest, MultiTruckLoadingResult } from '../types'

const BASE_URL = 'http://localhost:8080/api'

export const loadingApi = {
  calculate(request: LoadingRequest): Promise<MultiTruckLoadingResult> {
    return Taro.request({
      url: `${BASE_URL}/loading/multi-truck`,
      method: 'POST',
      data: request,
      header: {
        'Content-Type': 'application/json'
      }
    }).then(res => res.data)
  },

  getDefaultTruck(): Promise<Truck> {
    return Taro.request({
      url: `${BASE_URL}/truck/default`,
      method: 'GET'
    }).then(res => res.data)
  },

  validate(cargoList: Cargo[]): Promise<any> {
    return Taro.request({
      url: `${BASE_URL}/loading/validate`,
      method: 'POST',
      data: { cargoList },
      header: {
        'Content-Type': 'application/json'
      }
    }).then(res => res.data)
  },

  getHistory(): Promise<any> {
    return Taro.request({
      url: `${BASE_URL}/history`,
      method: 'GET'
    }).then(res => res.data)
  },

  getHistoryById(id: number): Promise<any> {
    return Taro.request({
      url: `${BASE_URL}/history/${id}`,
      method: 'GET'
    }).then(res => res.data)
  },

  deleteHistory(id: number): Promise<any> {
    return Taro.request({
      url: `${BASE_URL}/history/${id}`,
      method: 'DELETE'
    }).then(res => res.data)
  }
}
