export default defineAppConfig({
  pages: [
    'pages/home/index',
    'pages/result/index',
    'pages/mine/index'
  ],
  window: {
    backgroundTextStyle: 'light',
    navigationBarBackgroundColor: '#1a1a2e',
    navigationBarTitleText: '智能装车系统',
    navigationBarTextStyle: 'white',
    backgroundColor: '#f5f6f7'
  },
  tabBar: {
    color: '#86909c',
    selectedColor: '#165dff',
    backgroundColor: '#ffffff',
    borderStyle: 'white',
    list: [
      {
        pagePath: 'pages/home/index',
        text: '装车'
      },
      {
        pagePath: 'pages/result/index',
        text: '结果'
      },
      {
        pagePath: 'pages/mine/index',
        text: '我的'
      }
    ]
  }
})
