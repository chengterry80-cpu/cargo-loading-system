import axios from 'axios'
import type { Truck, Cargo, LoadingRequest, LoadingResult } from '../types'

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
})

export const loadingApi = {
  calculate(request: LoadingRequest): Promise<LoadingResult> {
    return api.post('/loading/calculate', request).then(res => res.data)
  },
  
  getDefaultTruck(): Promise<Truck> {
    return api.get('/truck/default').then(res => res.data)
  },
  
  validate(cargoList: Cargo[]): Promise<any> {
    return api.post('/loading/validate', { cargoList }).then(res => res.data)
  }
}
