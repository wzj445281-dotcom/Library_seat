# predict_server.py
from flask import Flask, request, jsonify
import joblib
import requests
import jieba # 即使是加载模型，也需要导入 jieba，因为模型对象里包含了 tokenizer 函数
import websocket
import json
import time
import threading
import hmac
import hashlib
import base64
from urllib.parse import urlencode
from datetime import datetime
from email.utils import formatdate

app = Flask(__name__)

# 加载模型 (注意：需要定义和训练时一样的 tokenizer 函数，否则 pickle 加载会报错)
def jieba_tokenizer(text):
    return jieba.lcut(text)

model = None
try:
    model = joblib.load('chat_model.pkl')
    print("模型加载成功")
except:
    print("模型未找到，将直接使用科大讯飞API")

# 科大讯飞API配置
APPID = "35fbd3d8"
APISecret = "NDQyZTI0Zjc4NjZmYWNhM2MwMTRmNGE3"
APIKey = "556017cfc3f02e9b9fad906b60282f0a"
API_PASSWORD = "wsNBZgHhNeHzLJoOgkpw:pCKvCHJrRxKNmebbpjSS"  # API Password (备用认证方式)

# 科大讯飞API配置 - Spark X1.5版本
API_URL = "https://spark-api.xf-yun.com/v1/x1"

# 科大讯飞WebSocket配置 - Spark X1.5版本
WS_HOST = "spark-api.xf-yun.com"
WS_PATH = "/v1/x1"  # 使用Spark X1.5版本

class SparkWebSocketClient:
    def __init__(self, appid, api_key, api_secret):
        self.appid = appid
        self.api_key = api_key
        self.api_secret = api_secret
        self.host = WS_HOST
        self.path = WS_PATH
        self.ws = None
        self.result = ""
        self.is_completed = False
        self.error = None
        self.query_data = None  # 存储要发送的数据
        self.connection_ready = False  # 连接就绪标志
        
    def generate_auth_url(self):
        """生成WebSocket鉴权URL"""
        # 生成RFC1123格式的时间戳
        rfc1123_date = formatdate(timeval=None, localtime=False, usegmt=True)
        
        # 创建签名原文 - 使用RFC1123格式的时间戳
        signature_origin = f"host: {self.host}\ndate: {rfc1123_date}\nGET {self.path} HTTP/1.1"
        
        # 使用hmac-sha256进行签名
        signature_sha = hmac.new(self.api_secret.encode('utf-8'), signature_origin.encode('utf-8'), hashlib.sha256).digest()
        signature_sha_base64 = base64.b64encode(signature_sha).decode('utf-8')
        
        # 构建authorization_origin
        authorization_origin = f'api_key="{self.api_key}", algorithm="hmac-sha256", headers="host date request-line", signature="{signature_sha_base64}"'
        
        # 对authorization_origin进行base64编码
        authorization = base64.b64encode(authorization_origin.encode('utf-8')).decode('utf-8')
        
        # 构建请求参数
        params = {
            "authorization": authorization,
            "date": rfc1123_date,
            "host": self.host
        }
        
        return f"wss://{self.host}{self.path}?{urlencode(params)}"
    
    def on_open(self, ws):
        """WebSocket连接建立后的回调"""
        print("WebSocket连接已建立")
        self.connection_ready = True
        # 连接建立后立即发送数据
        if self.query_data:
            try:
                ws.send(json.dumps(self.query_data))
                print(f"已发送消息: {self.query_data['payload']['message']['text'][0]['content']}")
            except Exception as e:
                print(f"发送消息失败: {e}")
                self.error = f"发送消息失败: {e}"
                self.is_completed = True
        
    def on_message(self, ws, message):
        """接收到消息的回调"""
        try:
            data = json.loads(message)
            print(f"接收到消息: {data}")
            
            # 检查是否是错误消息
            if 'header' in data and data['header'].get('code') != 0:
                self.error = f"API错误: {data['header'].get('message', '未知错误')}"
                self.is_completed = True
                return
                
            # 处理响应内容
            if 'payload' in data:
                payload = data['payload']
                
                # 处理文本内容（Spark X1.5返回reasoning_content和content，我们只提取最终的content字段）
                if 'choices' in payload and 'text' in payload['choices']:
                    texts = payload['choices']['text']
                    if texts and len(texts) > 0:
                        for item in texts:
                            # 只提取content字段（最终回复），忽略reasoning_content（推理过程）
                            if 'content' in item and item.get('content'):
                                self.result += item['content']
                
                # 检查是否结束
                if 'choices' in payload and 'status' in payload['choices'] and payload['choices']['status'] == 2:
                    self.is_completed = True
                    
        except Exception as e:
            print(f"处理消息时出错: {e}")
            self.error = f"处理消息时出错: {e}"
            self.is_completed = True
    
    def on_error(self, ws, error):
        """WebSocket错误的回调"""
        print(f"WebSocket错误: {error}")
        self.error = f"WebSocket错误: {error}"
        self.is_completed = True
    
    def on_close(self, ws, close_status_code, close_msg):
        """WebSocket关闭的回调"""
        print("WebSocket连接已关闭")
        self.is_completed = True
    
    def chat(self, query, domain="x1", temperature=0.5, max_tokens=1024):
        """发送聊天请求"""
        # 重置状态
        self.result = ""
        self.is_completed = False
        self.error = None
        
        # 生成鉴权URL
        auth_url = self.generate_auth_url()
        print(f"WebSocket连接URL: {auth_url}")
        
        # 创建WebSocket连接
        self.ws = websocket.WebSocketApp(
            auth_url,
            on_open=self.on_open,
            on_message=self.on_message,
            on_error=self.on_error,
            on_close=self.on_close
        )
        
        # 构建请求数据
        data = {
            "header": {
                "app_id": self.appid,
                "uid": "user"
            },
            "parameter": {
                "chat": {
                    "domain": domain,
                    "temperature": temperature,
                    "max_tokens": max_tokens
                }
            },
            "payload": {
                "message": {
                    "text": [
                        {
                            "role": "user",
                            "content": query
                        }
                    ]
                }
            }
        }
        
        # 保存要发送的数据
        self.query_data = data
        self.connection_ready = False
        
        # 在新线程中运行WebSocket
        ws_thread = threading.Thread(target=self.ws.run_forever)
        ws_thread.daemon = True
        ws_thread.start()
        
        # 等待连接建立（最多等待3秒）
        wait_time = 0
        while not self.connection_ready and wait_time < 30:
            time.sleep(0.1)
            wait_time += 0.1
        
        if not self.connection_ready:
            self.error = "WebSocket连接超时"
            return "连接超时，请稍后再试"
        
        # 等待响应完成或超时
        timeout = 30  # 30秒超时
        start_time = time.time()
        while not self.is_completed and (time.time() - start_time) < timeout:
            time.sleep(0.1)
        
        # 关闭连接
        try:
            self.ws.close()
        except:
            pass
        
        # 返回结果或错误信息
        if self.error:
            return self.error
        elif self.result:
            return self.result
        else:
            return "请求超时，未收到响应"

# 测试WebSocket连接科大讯飞API
def test_websocket_connection():
    """测试WebSocket连接科大讯飞API"""
    print("===== 测试WebSocket连接科大讯飞API =====")
    test_query = "你好"
    
    try:
        # 创建WebSocket客户端
        ws_client = SparkWebSocketClient(APPID, APIKey, APISecret)
        
        # 发送测试请求
        result = ws_client.chat(test_query)
        
        print(f"WebSocket测试结果: {result}")
        return result
    except Exception as e:
        print(f"WebSocket测试失败: {e}")
        return f"WebSocket测试失败: {e}"

# 测试直接调用科大讯飞API
def test_iflytek_directly():
    """测试直接调用科大讯飞API"""
    import time
    import hashlib
    import hmac
    import base64
    import json
    import urllib.parse
    
    print("===== 测试直接调用科大讯飞API =====")
    test_query = "你好"
    
    # 方法1: 尝试使用APIPassword认证
    try:
        # 构建请求数据
        data = {
            "header": {
                "app_id": APPID,
                "uid": "user"
            },
            "parameter": {
                "chat": {
                    "domain": "x1",
                    "temperature": 0.5,
                    "max_tokens": 1024
                }
            },
            "payload": {
                "message": {
                    "text": [
                        {
                            "role": "user",
                            "content": test_query
                        }
                    ]
                }
            }
        }
        
        # 使用APIPassword认证
        headers = {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {APIKey}"
        }
        
        print(f"尝试APIPassword认证方式，URL: {API_URL}")
        response = requests.post(API_URL, json=data, headers=headers, timeout=30)
        print(f"响应状态码: {response.status_code}")
        print(f"响应内容: {response.text}")
        
        if response.status_code == 200:
            response_data = response.json()
            if 'payload' in response_data and 'choices' in response_data['payload']:
                if 'text' in response_data['payload']['choices'] and len(response_data['payload']['choices']['text']) > 0:
                    return response_data['payload']['choices']['text'][0]['content']
    except Exception as e:
        print(f"APIPassword认证方式失败: {e}")
    
    # 方法2: 使用通用URL鉴权方式
    try:
        # 生成鉴权URL
        timestamp = str(int(time.time()))
        
        # 构建原始字符串
        host = "spark-api.xf-yun.com"
        path = "/v4.0/chat"
        method = "POST"
        
        # 创建签名原文
        signature_origin = f"host: {host}\ndate: {timestamp}\n{method} {path} HTTP/1.1"
        
        # 使用hmac-sha256进行签名
        signature_sha = hmac.new(APISecret.encode('utf-8'), signature_origin.encode('utf-8'), hashlib.sha256).digest()
        signature_sha_base64 = base64.b64encode(signature_sha).decode('utf-8')
        
        # 构建authorization_origin
        authorization_origin = f'api_key="{APIKey}", algorithm="hmac-sha256", headers="host date request-line", signature="{signature_sha_base64}"'
        
        # 对authorization_origin进行base64编码
        authorization = base64.b64encode(authorization_origin.encode('utf-8')).decode('utf-8')
        
        # 构建请求头
        headers = {
            "Authorization": authorization,
            "Content-Type": "application/json",
            "Host": host,
            "Date": timestamp,
            "X-Appid": APPID
        }
        
        # 请求数据
        data = {
            "header": {
                "app_id": APPID,
                "uid": "user"
            },
            "parameter": {
                "chat": {
                    "domain": "x1",
                    "temperature": 0.5,
                    "max_tokens": 1024
                }
            },
            "payload": {
                "message": {
                    "text": [
                        {
                            "role": "user",
                            "content": test_query
                        }
                    ]
                }
            }
        }
        
        print(f"尝试通用URL鉴权方式")
        print(f"请求URL: {API_URL}")
        print(f"请求头: {headers}")
        response = requests.post(API_URL, json=data, headers=headers, timeout=30)
        print(f"响应状态码: {response.status_code}")
        print(f"响应内容: {response.text}")
        
        if response.status_code == 200:
            response_data = response.json()
            if 'payload' in response_data and 'choices' in response_data['payload']:
                if 'text' in response_data['payload']['choices'] and len(response_data['payload']['choices']['text']) > 0:
                    return response_data['payload']['choices']['text'][0]['content']
    except Exception as e:
        print(f"通用URL鉴权方式失败: {e}")
    
    return "API调用失败"

# 启动时不再自动测试，避免影响服务启动速度
# 如需测试，请运行: python test_iflytek_api.py

def call_llm(query):
    """调用科大讯飞大模型获取回复"""
    try:
        # 使用WebSocket协议调用科大讯飞API
        print(f"使用WebSocket协议调用科大讯飞API: {query}")
        ws_client = SparkWebSocketClient(APPID, APIKey, APISecret)
        result = ws_client.chat(query)
        
        if result and not result.startswith("API错误") and not result.startswith("WebSocket错误") and result != "请求超时，未收到响应":
            return result
        else:
            print(f"WebSocket调用失败: {result}")
            # 如果WebSocket失败，尝试HTTP方式作为备用
            return call_llm_http_fallback(query)
    except Exception as e:
        print(f"WebSocket调用异常: {e}")
        # 如果WebSocket异常，尝试HTTP方式作为备用
        return call_llm_http_fallback(query)

def call_llm_http_fallback(query):
    """HTTP备用方式调用科大讯飞大模型"""
    import time
    import hashlib
    import hmac
    import base64
    import json
    
    print("使用HTTP备用方式调用科大讯飞API")
    
    # 方法1: 尝试使用APIPassword认证
    try:
        # 构建请求数据
        data = {
            "header": {
                "app_id": APPID,
                "uid": "user"
            },
            "parameter": {
                "chat": {
                    "domain": "x1",
                    "temperature": 0.5,
                    "max_tokens": 1024
                }
            },
            "payload": {
                "message": {
                    "text": [
                        {
                            "role": "user",
                            "content": query
                        }
                    ]
                }
            }
        }
        
        # 使用APIPassword认证
        headers = {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {APIKey}"
        }
        
        print(f"尝试APIPassword认证方式，URL: {API_URL}")
        response = requests.post(API_URL, json=data, headers=headers, timeout=30)
        print(f"响应状态码: {response.status_code}")
        print(f"响应内容: {response.text}")
        
        if response.status_code == 200:
            response_data = response.json()
            if 'payload' in response_data and 'choices' in response_data['payload']:
                if 'text' in response_data['payload']['choices'] and len(response_data['payload']['choices']['text']) > 0:
                    return response_data['payload']['choices']['text'][0]['content']
    except Exception as e:
        print(f"APIPassword认证方式失败: {e}")
    
    # 方法2: 原始认证方式
    try:
        # 生成鉴权URL
        timestamp = str(int(time.time()))
        signature_origin = f"host: spark-api.xf-yun.com\ndate: {timestamp}\nGET /v4.0/chat HTTP/1.1"
        signature_sha = hmac.new(APISecret.encode('utf-8'), signature_origin.encode('utf-8'), hashlib.sha256).digest()
        signature_sha_base64 = base64.b64encode(signature_sha).decode('utf-8')
        
        authorization_origin = f'api_key="{APIKey}", algorithm="hmac-sha256", headers="host date request-line", signature="{signature_sha_base64}"'
        authorization = base64.b64encode(authorization_origin.encode('utf-8')).decode('utf-8')
        
        headers = {
            "Authorization": authorization,
            "Content-Type": "application/json",
            "Host": "spark-api.xf-yun.com",
            "Date": timestamp,
            "X-Appid": APPID
        }
        
        data = {
            "header": {
                "app_id": APPID,
                "uid": "user"
            },
            "parameter": {
                "chat": {
                    "domain": "x1",
                    "temperature": 0.5,
                    "max_tokens": 1024
                }
            },
            "payload": {
                "message": {
                    "text": [
                        {
                            "role": "user",
                            "content": query
                        }
                    ]
                }
            }
        }
        
        response = requests.post(API_URL, json=data, headers=headers, timeout=30)
        if response.status_code == 200:
            response_data = response.json()
            if 'payload' in response_data and 'choices' in response_data['payload']:
                if 'text' in response_data['payload']['choices'] and len(response_data['payload']['choices']['text']) > 0:
                    return response_data['payload']['choices']['text'][0]['content']
    except Exception as e:
        print(f"原始认证方式失败: {e}")
    
    return "抱歉，我的大脑暂时断线了，请稍后再试。"

@app.route('/chat', methods=['POST'])
def chat():
    # 处理编码问题
    try:
        data = request.get_json(force=True)
    except:
        data = request.json
        
    user_msg = data.get('message', '')
    
    # 确保正确处理中文编码
    if isinstance(user_msg, str):
        try:
            # 尝试重新编码为UTF-8，解决可能的编码问题
            user_msg = user_msg.encode('utf-8').decode('utf-8')
        except:
            pass
    elif isinstance(user_msg, bytes):
        user_msg = user_msg.decode('utf-8')
    
    # 添加调试信息
    print(f"接收到的原始数据: {data}")
    print(f"提取的消息: '{user_msg}'")
    print(f"消息类型: {type(user_msg)}")
    print(f"消息编码: {user_msg.encode('utf-8')}")

    if not user_msg:
        return jsonify({'reply': '请输入内容'})

    reply = ""
    recommendations = []
    intent = "chat"  # 默认意图
    
    # 1. 检查模型是否可用
    if model is not None:
        # 1. 本地意图识别
        # predict_proba 可以获取概率，如果最高概率低于某个阈值(如0.4)，也可以强制转给 LLM
        intent = model.predict([user_msg])[0]
        probabilities = model.predict_proba([user_msg])[0]
        max_proba = max(probabilities)

        print(f"用户: {user_msg} -> 意图: {intent} (置信度: {max_proba:.2f})")

        # 2. 业务逻辑分流
        if intent == 'recommend' and max_proba > 0.3:
            reply = "为您推荐我们的当季爆款！生椰拿铁永远的神！"
            recommendations = [
                {"id": 1, "name": "生椰拿铁", "price": 18, "image": "/assets/images/coconut.jpg"},
                {"id": 2, "name": "碧螺知春拿铁", "price": 20, "image": "/assets/images/tea_latte.jpg"}
            ]
        elif intent == 'info' and max_proba > 0.3:
            reply = "我们门店位于软件园二期，营业时间是每天 8:00 - 22:00，提供免费WiFi哦。"
        elif intent == 'chat':
            # 3. 识别为 chat -> 直接调用科大讯飞API
            print(f"调用科大讯飞API: {user_msg}")
            reply = call_llm(user_msg)
            if reply == "抱歉，我的大脑暂时断线了，请稍后再试。":
                # 使用预设回复
                preset_replies = [
                    "您好！我是瑞幸咖啡的AI店员，有什么可以帮助您的吗？",
                    "欢迎光临瑞幸咖啡！我们有很多美味的饮品哦~",
                    "您好呀！想喝点什么吗？我可以为您推荐哦！",
                    "瑞幸咖啡竭诚为您服务！请问有什么可以帮您？",
                    "嗨！我是您的专属咖啡顾问，需要推荐吗？"
                ]
                import random
                reply = random.choice(preset_replies)
        else:
            # 4. 置信度低 -> 调用大模型
            reply = call_llm(user_msg)
    else:
        # 模型不可用，直接调用科大讯飞API
        print(f"模型不可用，直接调用科大讯飞API: {user_msg}")
        reply = call_llm(user_msg)

    return jsonify({
        'reply': reply,
        'recommendations': recommendations,
        'intent': intent # 方便调试
    })

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)