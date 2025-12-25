/**
 * WebSocket 工具类
 * 用于订单状态实时推送
 * 完全本地运行，无需外部网络
 */

// 本地WebSocket地址
const WS_BASE_URL = 'ws://localhost:8080';

class WebSocketManager {
    constructor() {
        this.socketTask = null;
        this.reconnectTimer = null;
        this.reconnectCount = 0;
        this.maxReconnectCount = 5;
        this.reconnectInterval = 3000; // 3秒
        this.listeners = new Map(); // 存储事件监听器
        this.isConnecting = false;
    }

    /**
     * 连接 WebSocket
     * @param {string} orderNo - 订单号
     * @param {function} onMessage - 消息回调
     * @param {function} onError - 错误回调
     */
    connect(orderNo, onMessage, onError) {
        if (this.isConnecting || (this.socketTask && this.socketTask.readyState === 1)) {
            console.log('WebSocket 已连接或正在连接中');
            return;
        }

        this.isConnecting = true;
        
        // 使用本地WebSocket地址
        const wsUrl = `${WS_BASE_URL}/ws/order/${orderNo}`;
        console.log('正在连接 WebSocket:', wsUrl);

        try {
            this.socketTask = wx.connectSocket({
                url: wsUrl,
                success: () => {
                    console.log('WebSocket 连接成功');
                    this.isConnecting = false;
                    this.reconnectCount = 0;
                },
                fail: (err) => {
                    console.error('WebSocket 连接失败:', err);
                    this.isConnecting = false;
                    if (onError) {
                        onError(err);
                    }
                    // 自动重连
                    this.scheduleReconnect(orderNo, onMessage, onError);
                }
            });

            // 监听消息
            this.socketTask.onMessage((res) => {
                console.log('收到 WebSocket 消息:', res.data);
                try {
                    const data = JSON.parse(res.data);
                    if (onMessage) {
                        onMessage(data);
                    }
                } catch (e) {
                    console.error('解析 WebSocket 消息失败:', e);
                }
            });

            // 监听错误
            this.socketTask.onError((err) => {
                console.error('WebSocket 错误:', err);
                this.isConnecting = false;
                if (onError) {
                    onError(err);
                }
                // 自动重连
                this.scheduleReconnect(orderNo, onMessage, onError);
            });

            // 监听关闭
            this.socketTask.onClose((res) => {
                console.log('WebSocket 连接关闭:', res);
                this.isConnecting = false;
                this.socketTask = null;
                
                // 如果不是主动关闭，则重连
                if (res.code !== 1000 && this.reconnectCount < this.maxReconnectCount) {
                    this.scheduleReconnect(orderNo, onMessage, onError);
                }
            });

            // 监听打开
            this.socketTask.onOpen(() => {
                console.log('WebSocket 已打开');
                this.isConnecting = false;
                this.reconnectCount = 0;
            });

        } catch (err) {
            console.error('创建 WebSocket 连接异常:', err);
            this.isConnecting = false;
            if (onError) {
                onError(err);
            }
        }
    }

    /**
     * 安排重连
     */
    scheduleReconnect(orderNo, onMessage, onError) {
        if (this.reconnectCount >= this.maxReconnectCount) {
            console.log('WebSocket 重连次数已达上限');
            return;
        }

        if (this.reconnectTimer) {
            clearTimeout(this.reconnectTimer);
        }

        this.reconnectCount++;
        console.log(`WebSocket 将在 ${this.reconnectInterval}ms 后重连 (${this.reconnectCount}/${this.maxReconnectCount})`);

        this.reconnectTimer = setTimeout(() => {
            this.connect(orderNo, onMessage, onError);
        }, this.reconnectInterval);
    }

    /**
     * 发送消息
     */
    send(data) {
        if (this.socketTask && this.socketTask.readyState === 1) {
            this.socketTask.send({
                data: JSON.stringify(data),
                success: () => {
                    console.log('WebSocket 消息发送成功');
                },
                fail: (err) => {
                    console.error('WebSocket 消息发送失败:', err);
                }
            });
        } else {
            console.warn('WebSocket 未连接，无法发送消息');
        }
    }

    /**
     * 关闭连接
     */
    close() {
        if (this.reconnectTimer) {
            clearTimeout(this.reconnectTimer);
            this.reconnectTimer = null;
        }

        if (this.socketTask) {
            this.socketTask.close({
                code: 1000,
                reason: '主动关闭'
            });
            this.socketTask = null;
        }

        this.reconnectCount = 0;
        this.isConnecting = false;
    }
}

// 导出单例
const wsManager = new WebSocketManager();

module.exports = wsManager;

