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

# DeepSeek API 配置 (以 DeepSeek 为例，OpenAI 同理)
API_KEY = "sk-your-deepseek-api-key"
API_URL = "https://api.deepseek.com/chat/completions" # 或者是 OpenAI 的 URL

def call_llm(query):
    """调用大模型获取回复"""
    headers = {
        "Authorization": f"Bearer {API_KEY}",
        "Content-Type": "application/json"
    }
    data = {
        "model": "deepseek-chat",
        "messages": [
            {"role": "system", "content": "你是一个瑞幸咖啡的AI店员，语气热情活泼。请简短回答用户的问题。"},
            {"role": "user", "content": query}
        ],
        "stream": False
    }
    try:
        response = requests.post(API_URL, json=data, headers=headers, timeout=10)
        if response.status_code == 200:
            return response.json()['choices'][0]['message']['content']
    except Exception as e:
        print(f"LLM调用失败: {e}")
    return "抱歉，我的大脑暂时断线了，请稍后再试。"

@app.route('/chat', methods=['POST'])
def chat():
    data = request.json
    user_msg = data.get('message', '')

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
    if intent == 'recommend' and max_proba > 0.5:
        reply = "为您推荐我们的当季爆款！生椰拿铁永远的神！"
        recommendations = [
            {"id": 1, "name": "生椰拿铁", "price": 18, "image": "/assets/images/coconut.jpg"},
            {"id": 2, "name": "碧螺知春拿铁", "price": 20, "image": "/assets/images/tea_latte.jpg"}
        ]
    elif intent == 'info' and max_proba > 0.5:
        reply = "我们门店位于软件园二期，营业时间是每天 8:00 - 22:00，提供免费WiFi哦。"
    else:
        # 3. 识别为 chat 或 置信度低 -> 调用大模型
        reply = call_llm(user_msg)

    return jsonify({
        'reply': reply,
        'recommendations': recommendations,
        'intent': intent # 方便调试
    })

if __name__ == '__maixn__':
    app.run(host='0.0.0.0', port=5000, debug=True)