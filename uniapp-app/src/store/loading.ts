import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Cargo, Truck, MultiTruckLoadingResult } from '../types'

export const useLoadingStore = defineStore('loading', () => {
  const cargoList = ref<Cargo[]>([])
  const truck = ref<Truck>({
    length: 1200,
    width: 240,
    height: 260,
    maxWeight: 20000
  })
  const loadingResult = ref<MultiTruckLoadingResult | null>(null)
  const isLoading = ref(false)

  const setCargoList = (list: Cargo[]) => {
    cargoList.value = list
  }

  const addCargo = (cargo: Cargo) => {
    cargoList.value.push(cargo)
  }

  const removeCargo = (index: number) => {
    cargoList.value.splice(index, 1)
  }

  const clearCargoList = () => {
    cargoList.value = []
    loadingResult.value = null
  }

  const setTruck = (newTruck: Truck) => {
    truck.value = newTruck
  }

  const setLoadingResult = (result: MultiTruckLoadingResult) => {
    loadingResult.value = result
  }

  const setIsLoading = (loading: boolean) => {
    isLoading.value = loading
  }

  return {
    cargoList,
    truck,
    loadingResult,
    isLoading,
    setCargoList,
    addCargo,
    removeCargo,
    clearCargoList,
    setTruck,
    setLoadingResult,
    setIsLoading
  }
})
