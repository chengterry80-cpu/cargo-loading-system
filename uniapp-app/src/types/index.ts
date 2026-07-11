export interface Cargo {
  id?: number
  name: string
  length: number
  width: number
  height: number
  weight: number
  quantity: number
}

export interface Truck {
  length: number
  width: number
  height: number
  maxWeight: number
}

export interface PlacedItem {
  cargoId: number
  name: string
  x: number
  y: number
  z: number
  length: number
  width: number
  height: number
  rotation: number
}

export interface LoadingResult {
  success: boolean
  totalWeight: number
  totalVolume: number
  utilizationRate: number
  placedItems: PlacedItem[]
  unplacedItems: Cargo[]
  message?: string
}

export interface LoadingRequest {
  truck: Truck
  cargoList: Cargo[]
  truckCount?: number
}

export interface TruckLoadResult {
  truckNumber: number
  success: boolean
  totalWeight: number
  totalVolume: number
  utilizationRate: number
  placedItems: PlacedItem[]
}

export interface MultiTruckLoadingResult {
  success: boolean
  truckResults: TruckLoadResult[]
  unplacedItems: Cargo[]
  message: string
  totalTrucksUsed: number
}

export interface LoadingHistory {
  id: number
  createTime: string
  truck: Truck
  cargoList: Cargo[]
  truckCount: number
  result: MultiTruckLoadingResult
}
