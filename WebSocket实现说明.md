# WebSocket 实时推送功能实现说明

## ✅ 已完成的功能

### 1. 后端实现

#### 1.1 添加依赖
- `spring-boot-starter-websocket` - Spring WebSocket 支持
- `javax.websocket-api` - WebSocket API
- `tyrus-container-spring` - WebSocket 容器实现

#### 1.2 WebSocket 配置
- **文件**: `seat_server/src/main/java/com/example/zhizuo/config/WebSocketConfig.java`
- 使用原生 WebSocket（支持微信小程序）
- 自动注册 `@ServerEndpoint` 注解的类

#### 1.3 WebSocket 端点
- **文件**: `seat_server/src/main/java/com/example/zhizuo/api/websocket/OrderWebSocketEndpoint.java`
- **路径**: `/ws/order/{orderNo}`
- 支持按订单号订阅订单状态更新

#### 1.4 订单状态推送
- **文件**: `seat_server/src/main/java/com/example/zhizuo/api/websocket/OrderWebSocketController.java`
- 在 `OrderServiceImpl.updateOrderStatus()` 中自动推送状态更新
- 提供测试接口: `/api/websocket/test/{orderNo}`

#### 1.5 Security 配置
- 已放行 WebSocket 路径: `/ws/**`, `/app/**`, `/topic/**`, `/queue/**`

### 2. 前端实现

#### 2.1 WebSocket 工具类
- **文件**: `seat_client/utils/websocket.js`
- 支持自动重连（最多5次）
- 支持消息接收和发送
- 支持连接状态管理

#### 2.2 订单详情页集成
- **文件**: `seat_client/pages/orders/detail/detail.js`
- 页面加载时自动连接 WebSocket
- 页面卸载时自动断开连接
- 收到状态更新时自动刷新页面数据

## 🔧 使用方法

### 后端测试

1. **启动后端服务**
   ```bash
   cd seat_server
   mvn spring-boot:run
   ```

2. **测试推送接口**
   ```bash
   # 替换 {orderNo} 为实际订单号
   curl http://localhost:8080/api/websocket/test/{orderNo}
   ```

### 前端使用

1. **打开订单详情页**
   - 进入订单列表
   - 点击某个订单进入详情页
   - WebSocket 会自动连接

2. **查看连接状态**
   - 打开微信开发者工具控制台
   - 查看 "WebSocket 连接成功" 日志

3. **测试状态更新**
   - 在后端调用 `/api/admin/order/status` 更新订单状态
   - 前端会自动收到推送并更新页面

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

## ⚠️ 注意事项

### 1. 微信开发者工具配置
- 必须勾选 "不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书"
- WebSocket 连接地址使用 `ws://` 协议（开发环境）

### 2. 网络配置
- WebSocket 地址基于 `request.js` 中的 `BASE_URL`
- 当前配置: `ws://192.168.78.196:8080/ws/order/{orderNo}`
- 如果 IP 地址变化，需要修改 `request.js` 中的 `BASE_URL`

### 3. 生产环境
- 生产环境需要使用 `wss://` (WebSocket Secure)
- 需要配置 SSL 证书
- 需要配置域名白名单

## 🐛 故障排查

### 问题1: WebSocket 连接失败

**可能原因：**
1. 后端服务未启动
2. 网络地址配置错误
3. 防火墙阻止连接
4. 微信开发者工具未配置不校验域名

**解决方法：**
1. 检查后端服务是否运行: `netstat -ano | findstr :8080`
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
1. WebSocket 工具类已实现自动重连
2. 检查后端服务稳定性
3. 检查网络连接质量

## 🎉 功能特点

1. **实时推送** - 订单状态更新立即推送到前端
2. **自动重连** - 连接断开后自动重连（最多5次）
3. **按订单订阅** - 每个订单独立连接，互不干扰
4. **优雅降级** - 连接失败不影响页面正常使用
5. **状态同步** - 收到推送后自动更新页面数据

## 📚 相关文件

### 后端
- `seat_server/pom.xml` - 添加 WebSocket 依赖
- `seat_server/src/main/java/com/example/zhizuo/config/WebSocketConfig.java` - WebSocket 配置
- `seat_server/src/main/java/com/example/zhizuo/api/websocket/OrderWebSocketEndpoint.java` - WebSocket 端点
- `seat_server/src/main/java/com/example/zhizuo/api/websocket/OrderWebSocketController.java` - WebSocket 控制器
- `seat_server/src/main/java/com/example/zhizuo/core/service/impl/OrderServiceImpl.java` - 订单服务（集成推送）

### 前端
- `seat_client/utils/websocket.js` - WebSocket 工具类
- `seat_client/pages/orders/detail/detail.js` - 订单详情页（集成 WebSocket）

