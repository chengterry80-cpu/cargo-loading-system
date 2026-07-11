# 智能货物装车算法可视化系统

基于 SpringBoot 3.x + Vue 3 + Three.js 的智能货物装车算法可视化系统。

## 技术栈

### 后端
- SpringBoot 3.2.5
- JDK 17+
- Maven

### 前端
- Vue 3
- TypeScript
- Vite
- Element Plus
- Three.js

## 项目结构

```
Cargo_Test/
├── src/main/java/com/loading/
│   ├── config/         # CORS 配置
│   ├── controller/     # REST API 控制器
│   ├── model/          # 数据模型
│   ├── service/        # 核心装车算法
│   └── LoadingApplication.java
└── frontend/           # 前端项目
    ├── src/
    │   ├── api/       # API 请求封装
    │   ├── components/ # Vue 组件
    │   ├── types/     # TypeScript 类型定义
    │   ├── views/     # 页面视图
    │   └── main.ts
    └── package.json
```

## 快速开始

### 启动后端

```bash
# 在项目根目录执行
mvn spring-boot:run
```

后端服务运行在 http://localhost:8080

### 启动前端

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务运行在 http://localhost:5173

## 功能特性

1. **货物管理**
   - 添加/删除货物
   - 设置货物尺寸、重量、数量
   - 批量管理

2. **车厢配置**
   - 自定义车厢尺寸
   - 最大载重设置

3. **智能装车**
   - 基于最大剩余空间的三维装箱算法
   - 支持货物旋转优化
   - 空间利用率计算

4. **3D 可视化**
   - Three.js 3D 场景展示
   - 货物彩色区分
   - 鼠标悬停查看详情
   - 可旋转/缩放/平移

5. **结果展示**
   - 总重量/总体积统计
   - 空间利用率进度条
   - 未装载货物列表

## 核心算法

系统实现了基于最大剩余空间的启发式三维装箱算法：

1. **排序策略**：按体积从大到小排序，体积相同时按重量从大到小
2. **空间管理**：维护可用空间列表
3. **最佳适配**：选择浪费空间最小的放置位置
4. **旋转优化**：尝试 3 种旋转方向以获得最佳放置效果
5. **空间分割**：放置后将剩余空间分割为新的可用空间

## API 接口

- `POST /api/loading/calculate` - 执行装车算法
- `GET /api/truck/default` - 获取默认车厢参数
- `POST /api/loading/validate` - 验证货物数据

## 示例数据

系统内置示例数据，点击"加载示例数据"按钮即可快速体验。

## 开发说明

- 后端默认端口：8080
- 前端默认端口：5173
- CORS 已配置允许 localhost:5173 访问
