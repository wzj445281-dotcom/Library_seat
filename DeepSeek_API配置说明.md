# DeepSeek API 配置说明

## 问题分析

AI 助手功能已经实现了 DeepSeek API 的调用，但是因为 API Key 未配置，所以回退到了 Mock 模式。

## 配置方法

### 方法 1：在 application.yml 中配置（推荐）

编辑 `seat_server/src/main/resources/application.yml` 文件：

```yaml
ai:
  deepseek:
    key: sk-your-actual-deepseek-api-key-here  # 替换为您的实际 API Key
    url: https://api.deepseek.com/chat/completions
    model: deepseek-chat
```

### 方法 2：通过环境变量配置

在启动后端服务时设置环境变量：

**Windows (PowerShell):**
```powershell
$env:AI_DEEPSEEK_KEY="sk-your-actual-deepseek-api-key-here"
cd seat_server
mvn spring-boot:run
```

**Windows (CMD):**
```cmd
set AI_DEEPSEEK_KEY=sk-your-actual-deepseek-api-key-here
cd seat_server
mvn spring-boot:run
```

**Linux/Mac:**
```bash
export AI_DEEPSEEK_KEY="sk-your-actual-deepseek-api-key-here"
cd seat_server
mvn spring-boot:run
```

### 方法 3：在 IDE 中配置

如果使用 IntelliJ IDEA 或 Eclipse：

1. 打开运行配置
2. 在 "Environment variables" 中添加：
   - 变量名：`AI_DEEPSEEK_KEY`
   - 变量值：`sk-your-actual-deepseek-api-key-here`

## 获取 DeepSeek API Key

1. 访问 [DeepSeek 官网](https://www.deepseek.com/)
2. 注册/登录账号
3. 进入 API 管理页面
4. 创建新的 API Key
5. 复制 API Key（格式：`sk-xxxxxxxxxxxxxxxxxxxxxxxx`）

## 验证配置

配置完成后，重启后端服务，然后：

1. 在小程序中打开 AI 助手页面
2. 发送一条消息（如"推荐"）
3. 如果返回的是真实的 AI 回复（而不是 Mock 数据），说明配置成功

## 代码说明

### API 调用流程

1. **检查 API Key**：如果未配置或无效，返回 Mock 数据
2. **构建请求**：
   - 从数据库获取商品列表作为知识库
   - 获取用户收藏和历史订单作为用户画像
   - 构建 System Prompt 和 User Message
3. **调用 DeepSeek API**：
   - URL: `https://api.deepseek.com/chat/completions`
   - Model: `deepseek-chat`
   - 使用 JSON 格式输出
4. **解析响应**：提取 AI 回复和推荐商品

### 关键代码位置

- **配置文件**：`seat_server/src/main/resources/application.yml`
- **控制器**：`seat_server/src/main/java/com/example/zhizuo/api/app/AiAssistantController.java`
- **API Key 检查**：第 116 行
- **API 调用**：第 121-148 行

## 注意事项

1. **API Key 安全**：
   - 不要将 API Key 提交到 Git 仓库
   - 建议使用环境变量或配置文件（不提交到版本控制）
   - 可以在 `.gitignore` 中添加 `application-local.yml`

2. **API 限制**：
   - DeepSeek API 有调用频率限制
   - 建议在生产环境中添加缓存和限流

3. **错误处理**：
   - 如果 API 调用失败，会自动降级到 Mock 模式
   - 查看后端日志可以了解具体错误信息

## 故障排除

### Q1: 仍然返回 Mock 数据
- 检查 API Key 是否正确配置
- 检查环境变量是否正确设置
- 确认后端服务已重启

### Q2: API 调用失败
- 检查网络连接
- 检查 API Key 是否有效
- 查看后端日志中的错误信息

### Q3: 返回格式错误
- 检查 DeepSeek API 响应格式
- 查看 `parseAiJson` 方法的日志

---

**参考文档**：[DeepSeek API 文档](https://api-docs.deepseek.com/zh-cn/api/create-chat-completion)

