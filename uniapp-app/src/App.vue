<script setup lang="ts">
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'

onLaunch(() => {
  console.log('App Launch')
})

onShow(() => {
  console.log('App Show')
})

onHide(() => {
  console.log('App Hide')
})
</script>

<style lang="scss">
page {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  overflow-x: hidden;
}

view, text {
  box-sizing: border-box;
}

button::after {
  border: none;
}

.uni-card {
  background-image: none !important;
}

.uni-card__content {
  padding: 24rpx !important;
}

.uni-card__header {
  padding: 24rpx !important;
}

image {
  display: block;
}
</style>
