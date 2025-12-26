# WebSocket 网络连接配置说明

## ✅ 已完成的配置

### 1. 前端 WebSocket 配置

**文件：** `seat_client/utils/websocket.js`

- ✅ 已从 `request.js` 动态获取 `BASE_URL`
- ✅ 自动将 HTTP URL 转换为 WebSocket URL
  - `http://` → `ws://`
  - `https://` → `wss://`
- ✅ 自动移除 `/api` 后缀
- ✅ 支持自动重连（最多5次）
- ✅ 支持连接状态管理

### 2. 后端 WebSocket 配置

**文件：** `seat_server/src/main/java/com/example/zhizuo/config/WebSocketConfig.java`

- ✅ 已启用 WebSocket 支持
- ✅ 自动注册 `@ServerEndpoint` 注解的类

**文件：** `seat_server/src/main/java/com/example/zhizuo/api/websocket/OrderWebSocketEndpoint.java`

- ✅ WebSocket 端点：`/ws/order/{orderNo}`
- ✅ 支持按订单号订阅订单状态更新
- ✅ 自动推送订单状态变更

**文件：** `seat_server/src/main/java/com/example/zhizuo/config/SecurityConfig.java`

- ✅ 已放行 WebSocket 路径：`/ws/**`

## 🔧 配置步骤

### 步骤1：确认服务器地址

**文件：** `seat_client/utils/request.js`

当前配置：
```javascript
const BASE_URL = 'http://localhost:8080/api';
```

**说明：**
- **本地开发（模拟器）**：使用 `http://localhost:8080/api`
- **真机调试**：需要使用局域网IP，例如 `http://192.168.1.100:8080/api`
- **生产环境**：使用实际域名，例如 `https://api.example.com/api`

### 步骤2：WebSocket 自动配置

WebSocket 地址会自动从 `BASE_URL` 转换：

**示例：**
- `BASE_URL = 'http://localhost:8080/api'` 
  → WebSocket: `ws://localhost:8080/ws/order/{orderNo}`
  
- `BASE_URL = 'http://192.168.1.100:8080/api'`
  → WebSocket: `ws://192.168.1.100:8080/ws/order/{orderNo}`
  
- `BASE_URL = 'https://api.example.com/api'`
  → WebSocket: `wss://api.example.com/ws/order/{orderNo}`

**无需手动配置 WebSocket 地址！**

### 步骤3：微信开发者工具配置

1. **打开微信开发者工具**
2. **点击右上角"详情"**
3. **切换到"本地设置"标签**
4. **勾选：**
   - ✅ **不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书**
   - ✅ **不校验安全域名（TLS版本）**

### 步骤4：启动后端服务

```bash
cd seat_server
mvn spring-boot:run
```

确保后端服务运行在 `http://localhost:8080`（或你配置的地址）

## 🎯 使用方法

### 在订单详情页使用

**文件：** `seat_client/pages/orders/detail/detail.js`

WebSocket 会在页面加载时自动连接：

```javascript
onLoad: function (options) {
    const orderNo = options.orderNo || options.id;
    if (orderNo) {
        this.connectWebSocket(orderNo);
    }
}
```

**功能：**
- 自动连接 WebSocket
- 接收订单状态更新推送
- 自动更新页面数据
- 页面卸载时自动断开连接

## 📝 WebSocket 消息格式

### 推送消息格式

```json
{
  "type": "ORDER_STATUS_UPDATE",
  "orderNo": "ORDER123456789",
  "status": "READY",
  "timestamp": 1703123456789
}
```

### 状态值说明

- `PENDING` - 待支付
- `PAID` - 制作中
- `READY` - 待取餐
- `COMPLETED` - 已完成
- `CANCELLED` - 已取消

## 🐛 故障排查

### 问题1: WebSocket 连接失败

**可能原因：**
1. 后端服务未启动
2. `BASE_URL` 配置错误
3. 防火墙阻止连接
4. 微信开发者工具未配置不校验域名

**解决方法：**
1. 检查后端服务是否运行：`netstat -ano | findstr :8080`
2. 检查 `request.js` 中的 `BASE_URL` 是否正确
3. 检查微信开发者工具设置
4. 查看控制台错误信息

### 问题2: 收到消息但页面未更新

**可能原因：**
1. 消息格式不正确
2. 订单号不匹配
3. 页面数据更新逻辑错误

**解决方法：**
1. 查看控制台日志，确认收到消息
2. 检查消息中的 `orderNo` 是否与当前订单匹配
3. 检查 `connectWebSocket` 方法中的更新逻辑

### 问题3: 连接频繁断开

**可能原因：**
1. 网络不稳定
2. 后端服务重启
3. WebSocket 超时

**解决方法：**
1. WebSocket 工具类已实现自动重连（最多5次）
2. 检查后端服务稳定性
3. 检查网络连接质量

## 🎉 功能特点

1. **动态配置** - WebSocket 地址自动从 HTTP 地址转换
2. **实时推送** - 订单状态更新立即推送到前端
3. **自动重连** - 连接断开后自动重连（最多5次）
4. **按订单订阅** - 每个订单独立连接，互不干扰
5. **优雅降级** - 连接失败不影响页面正常使用
6. **状态同步** - 收到推送后自动更新页面数据

## 📚 相关文件

### 前端
- `seat_client/utils/websocket.js` - WebSocket 工具类
- `seat_client/utils/request.js` - HTTP 请求工具（包含 BASE_URL）
- `seat_client/pages/orders/detail/detail.js` - 订单详情页（集成 WebSocket）

### 后端
- `seat_server/src/main/java/com/example/zhizuo/config/WebSocketConfig.java` - WebSocket 配置
- `seat_server/src/main/java/com/example/zhizuo/api/websocket/OrderWebSocketEndpoint.java` - WebSocket 端点
- `seat_server/src/main/java/com/example/zhizuo/api/websocket/OrderWebSocketController.java` - WebSocket 控制器
- `seat_server/src/main/java/com/example/zhizuo/core/service/impl/OrderServiceImpl.java` - 订单服务（集成推送）
- `seat_server/src/main/java/com/example/zhizuo/config/SecurityConfig.java` - 安全配置（放行 WebSocket）

## 🔄 更新日志

### 2025-12-26
- ✅ 修改 WebSocket 工具类，从 `request.js` 动态获取 `BASE_URL`
- ✅ 实现 HTTP URL 到 WebSocket URL 的自动转换
- ✅ 支持 `http://` → `ws://` 和 `https://` → `wss://` 转换
- ✅ 自动移除 `/api` 后缀


