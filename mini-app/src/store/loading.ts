import { create } from 'zustand'
import type { Cargo, Truck, MultiTruckLoadingResult } from '../types'

interface LoadingState {
  cargoList: Cargo[]
  truck: Truck
  loadingResult: MultiTruckLoadingResult | null
  isLoading: boolean
  
  setCargoList: (list: Cargo[]) => void
  addCargo: (cargo: Cargo) => void
  removeCargo: (index: number) => void
  clearCargoList: () => void
  setTruck: (truck: Truck) => void
  setLoadingResult: (result: MultiTruckLoadingResult) => void
  setIsLoading: (loading: boolean) => void
}

export const useLoadingStore = create<LoadingState>((set) => ({
  cargoList: [],
  truck: {
    length: 1200,
    width: 240,
    height: 260,
    maxWeight: 20000
  },
  loadingResult: null,
  isLoading: false,
  
  setCargoList: (list) => set({ cargoList: list }),
  addCargo: (cargo) => set((state) => ({ cargoList: [...state.cargoList, cargo] })),
  removeCargo: (index) => set((state) => {
    const newList = [...state.cargoList]
    newList.splice(index, 1)
    return { cargoList: newList }
  }),
  clearCargoList: () => set({ cargoList: [], loadingResult: null }),
  setTruck: (truck) => set({ truck }),
  setLoadingResult: (result) => set({ loadingResult: result }),
  setIsLoading: (loading) => set({ isLoading: loading })
}))
