<template>
  <div ref="container" class="three-container"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import type { PlacedItem, Truck } from '../types'

const props = defineProps<{
  truck: Truck | null
  placedItems: PlacedItem[]
}>()

const container = ref<HTMLDivElement | null>(null)
let scene: THREE.Scene
let camera: THREE.PerspectiveCamera
let renderer: THREE.WebGLRenderer
let controls: OrbitControls
let cargoMeshes: THREE.Mesh[] = []
let raycaster: THREE.Raycaster
let mouse: THREE.Vector2
let tooltip: HTMLDivElement
let highlightedMesh: THREE.Mesh | null = null

const colors = [
  0xff6b6b, 0x4ecdc4, 0x45b7d1, 0x96ceb4, 0xffeaa7,
  0xffa07a, 0xdda0dd, 0x98d8c8, 0xf7dc6f, 0xbb8fce,
  0x85c1e9, 0x7dcea0, 0xf8c471, 0xbb8fce, 0xf1948a
]

const init = () => {
  if (!container.value) return

  scene = new THREE.Scene()
  scene.background = new THREE.Color(0xf0f2f5)

  camera = new THREE.PerspectiveCamera(
    60,
    container.value.clientWidth / container.value.clientHeight,
    0.1,
    10000
  )
  camera.position.set(1500, 800, 1500)

  renderer = new THREE.WebGLRenderer({ antialias: true })
  renderer.setSize(container.value.clientWidth, container.value.clientHeight)
  renderer.setPixelRatio(window.devicePixelRatio)
  container.value.appendChild(renderer.domElement)

  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.05

  const ambientLight = new THREE.AmbientLight(0xffffff, 0.6)
  scene.add(ambientLight)
  const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8)
  directionalLight.position.set(500, 1000, 500)
  scene.add(directionalLight)

  raycaster = new THREE.Raycaster()
  mouse = new THREE.Vector2()

  tooltip = document.createElement('div')
  tooltip.style.cssText = `
    position: absolute;
    background: rgba(0, 0, 0, 0.8);
    color: white;
    padding: 8px 12px;
    border-radius: 4px;
    font-size: 12px;
    pointer-events: none;
    z-index: 1000;
    display: none;
  `
  container.value.appendChild(tooltip)

  container.value.addEventListener('mousemove', onMouseMove)
  container.value.addEventListener('mouseleave', onMouseLeave)

  animate()
}

const drawTruck = () => {
  const oldTruck = scene.getObjectByName('truck')
  if (oldTruck) scene.remove(oldTruck)

  if (!props.truck) return

  const { length, width, height } = props.truck
  const geometry = new THREE.BoxGeometry(length, height, width)
  const edges = new THREE.EdgesGeometry(geometry)
  const lineMaterial = new THREE.LineBasicMaterial({ color: 0x2c3e50 })
  const line = new THREE.LineSegments(edges, lineMaterial)
  line.name = 'truck'
  line.position.set(length / 2, height / 2, width / 2)
  scene.add(line)

  const helperGeometry = new THREE.BufferGeometry()
  const vertices = new Float32Array([
    0, 0, 0, length, 0, 0,
    length, 0, 0, length, 0, width,
    length, 0, width, 0, 0, width
  ])
  helperGeometry.setAttribute('position', new THREE.BufferAttribute(vertices, 3))
  const helperMaterial = new THREE.LineBasicMaterial({ color: 0x95a5a6 })
  const helper = new THREE.LineSegments(helperGeometry, helperMaterial)
  scene.add(helper)

  camera.lookAt(length / 2, height / 2, width / 2)
}

const drawCargo = () => {
  cargoMeshes.forEach(mesh => scene.remove(mesh))
  cargoMeshes = []

  props.placedItems.forEach((item, index) => {
    const geometry = new THREE.BoxGeometry(item.length, item.height, item.width)
    const color = colors[index % colors.length]
    const material = new THREE.MeshPhongMaterial({
      color,
      transparent: true,
      opacity: 0.85,
      shininess: 60
    })
    const mesh = new THREE.Mesh(geometry, material)
    mesh.position.set(
      item.x + item.length / 2,
      item.y + item.height / 2,
      item.z + item.width / 2
    )

    const edges = new THREE.EdgesGeometry(geometry)
    const edgeMaterial = new THREE.LineBasicMaterial({ color: 0x34495e })
    const edge = new THREE.LineSegments(edges, edgeMaterial)
    mesh.add(edge)

    mesh.userData = { item }
    scene.add(mesh)
    cargoMeshes.push(mesh)
  })
}

const onMouseMove = (event: MouseEvent) => {
  if (!container.value) return
  const rect = container.value.getBoundingClientRect()
  mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1
  mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1

  raycaster.setFromCamera(mouse, camera)
  const intersects = raycaster.intersectObjects(cargoMeshes)

  if (highlightedMesh) {
    ;(highlightedMesh.material as THREE.MeshPhongMaterial).emissive.setHex(0x000000)
    highlightedMesh = null
  }

  if (intersects.length > 0) {
    highlightedMesh = intersects[0].object as THREE.Mesh
    ;(highlightedMesh.material as THREE.MeshPhongMaterial).emissive.setHex(0x222222)

    const item = highlightedMesh.userData.item as PlacedItem
    tooltip.innerHTML = `
      <div><strong>${item.name}</strong></div>
      <div>尺寸: ${item.length}×${item.width}×${item.height} cm</div>
      <div>旋转: ${item.rotation}°</div>
    `
    tooltip.style.left = event.clientX - rect.left + 15 + 'px'
    tooltip.style.top = event.clientY - rect.top + 15 + 'px'
    tooltip.style.display = 'block'
  } else {
    tooltip.style.display = 'none'
  }
}

const onMouseLeave = () => {
  if (highlightedMesh) {
    ;(highlightedMesh.material as THREE.MeshPhongMaterial).emissive.setHex(0x000000)
    highlightedMesh = null
  }
  tooltip.style.display = 'none'
}

const animate = () => {
  requestAnimationFrame(animate)
  controls.update()
  renderer.render(scene, camera)
}

const handleResize = () => {
  if (!container.value) return
  camera.aspect = container.value.clientWidth / container.value.clientHeight
  camera.updateProjectionMatrix()
  renderer.setSize(container.value.clientWidth, container.value.clientHeight)
}

onMounted(() => {
  init()
  drawTruck()
  window.addEventListener('resize', handleResize)
})

watch(() => props.truck, () => {
  drawTruck()
})

watch(() => props.placedItems, () => {
  drawCargo()
}, { deep: true })

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (container.value) {
    container.value.removeEventListener('mousemove', onMouseMove)
    container.value.removeEventListener('mouseleave', onMouseLeave)
  }
})
</script>

<style scoped>
.three-container {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  overflow: hidden;
}
</style>
