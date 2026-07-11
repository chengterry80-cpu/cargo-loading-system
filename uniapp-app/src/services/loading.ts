import type { Truck, Cargo, LoadingRequest, MultiTruckLoadingResult, LoadingHistory } from '../types'

const BASE_URL = 'https://motion-audible-germless.ngrok-free.dev/api'
const REQUEST_TIMEOUT = 30000

const createTimeoutPromise = <T>(timeout: number): Promise<T> => {
  return new Promise((_, reject) => {
    setTimeout(() => {
      reject(new Error(`请求超时（${timeout}ms）`))
    }, timeout)
  })
}

const request = <T>(options: {
  url: string
  method: 'GET' | 'POST' | 'DELETE'
  data?: any
  header?: any
  timeout?: number
}): Promise<T> => {
  const timeout = options.timeout || REQUEST_TIMEOUT
  
  const requestPromise = new Promise<T>((resolve, reject) => {
    uni.request({
      url: options.url,
      method: options.method,
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        'ngrok-skip-browser-warning': 'true',
        ...options.header
      },
      timeout,
      success: (res) => {
        if (res.statusCode === 200 || res.statusCode === 201) {
          resolve(res.data as T)
        } else {
          reject(new Error(`请求失败，状态码: ${res.statusCode}`))
        }
      },
      fail: (err) => {
        reject(err)
      },
      complete: () => {}
    })
  })

  return Promise.race([requestPromise, createTimeoutPromise<T>(timeout)])
}

export const loadingApi = {
  calculate(data: LoadingRequest): Promise<MultiTruckLoadingResult> {
    return request<MultiTruckLoadingResult>({
      url: `${BASE_URL}/loading/multi-truck`,
      method: 'POST',
      data: data,
      timeout: 60000
    })
  },

  getDefaultTruck(): Promise<Truck> {
    return request<Truck>({
      url: `${BASE_URL}/truck/default`,
      method: 'GET'
    })
  },

  validate(cargoList: Cargo[]): Promise<any> {
    return request<any>({
      url: `${BASE_URL}/loading/validate`,
      method: 'POST',
      data: { cargoList }
    })
  },

  getHistory(): Promise<LoadingHistory[]> {
    return request<LoadingHistory[]>({
      url: `${BASE_URL}/history`,
      method: 'GET'
    })
  },

  getHistoryById(id: number): Promise<LoadingHistory> {
    return request<LoadingHistory>({
      url: `${BASE_URL}/history/${id}`,
      method: 'GET'
    })
  },

  deleteHistory(id: number): Promise<any> {
    return request<any>({
      url: `${BASE_URL}/history/${id}`,
      method: 'DELETE'
    })
  }
}
