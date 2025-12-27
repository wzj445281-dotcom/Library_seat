# predict_server.py
from flask import Flask, request, jsonify
import joblib
import requests
import jieba # 即使是加载模型，也需要导入 jieba，因为模型对象里包含了 tokenizer 函数

app = Flask(__name__)

# 加载模型 (注意：需要定义和训练时一样的 tokenizer 函数，否则 pickle 加载会报错)
def jieba_tokenizer(text):
    return jieba.lcut(text)

try:
    model = joblib.load('chat_model.pkl')
    print("模型加载成功")
except:
    print("模型未找到，请先运行 train_nlp.py")

# 科大讯飞AI配置
APPID = "35fbd3d8"
APISecret = "NDQyZTI0Zjc4NjZmYWNhM2MwMTRmNGE3"
APIKey = "556017cfc3f02e9b9fad906b60282f0a"

# 科大讯飞API URL
API_URL = "https://spark-api.xf-yun.com/v3.1/chat"

def call_llm(query):
    """调用科大讯飞大模型获取回复"""
    import time
    import hashlib
    import hmac
    import base64
    
    # 生成鉴权URL
    timestamp = str(int(time.time()))
    signature_origin = f"host: spark-api.xf-yun.com\ndate: {timestamp}\nGET /v3.1/chat HTTP/1.1"
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
                "domain": "generalv3",
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
    
    try:
        response = requests.post(API_URL, json=data, headers=headers, timeout=10)
        if response.status_code == 200:
            response_data = response.json()
            if 'payload' in response_data and 'choices' in response_data['payload']:
                if 'text' in response_data['payload']['choices'] and len(response_data['payload']['choices']['text']) > 0:
                    return response_data['payload']['choices']['text'][0]['content']
    except Exception as e:
        print(f"科大讯飞调用失败: {e}")
    return "抱歉，我的大脑暂时断线了，请稍后再试。"

@app.route('/chat', methods=['POST'])
def chat():
    data = request.json
    user_msg = data.get('message', '')
    
    # 添加调试信息
    print(f"接收到的原始数据: {data}")
    print(f"提取的消息: '{user_msg}'")
    print(f"消息类型: {type(user_msg)}")
    print(f"消息编码: {user_msg.encode('utf-8')}")

    if not user_msg:
        return jsonify({'reply': '请输入内容'})

    # 1. 本地意图识别
    # predict_proba 可以获取概率，如果最高概率低于某个阈值(如0.4)，也可以强制转给 LLM
    intent = model.predict([user_msg])[0]
    probabilities = model.predict_proba([user_msg])[0]
    max_proba = max(probabilities)

    print(f"用户: {user_msg} -> 意图: {intent} (置信度: {max_proba:.2f})")

    reply = ""
    recommendations = []

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
        # 3. 识别为 chat -> 先尝试调用大模型，失败则使用预设回复
        llm_reply = call_llm(user_msg)
        if llm_reply == "抱歉，我的大脑暂时断线了，请稍后再试。":
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
            reply = llm_reply
    else:
        # 4. 置信度低 -> 调用大模型
        reply = call_llm(user_msg)

    return jsonify({
        'reply': reply,
        'recommendations': recommendations,
        'intent': intent # 方便调试
    })

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)