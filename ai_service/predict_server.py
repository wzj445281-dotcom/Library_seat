from flask import Flask, request, jsonify
import joblib
import pandas as pd
import random
import requests
import os
from datetime import datetime

app = Flask(__name__)

# 配置 DeepSeek API Key
# 建议在 docker-compose.yml 中配置环境变量 DEEPSEEK_API_KEY，如果没有配置则使用默认值（请填入您的 Key）
DEEPSEEK_API_KEY = os.getenv('DEEPSEEK_API_KEY', 'sk-xxxxxxxxxxxxxxxxxxxxxxxx')
DEEPSEEK_URL = "https://api.deepseek.com/chat/completions"

# 尝试加载模型，如果不存在则使用模拟逻辑
try:
    model = joblib.load('model.pkl')
    model_loaded = True
except:
    print("Warning: model.pkl not found. Running in mock mode.")
    model_loaded = False

@app.route('/predict', methods=['POST'])
def predict():
    """
    【功能 1】: 传统的机器学习预测 (保留原有功能)
    用于预测门店客流拥挤度数值。
    """
    if not model_loaded:
        # 模拟返回：随机生成一个拥挤度预测
        return jsonify({'prediction': random.uniform(0, 1), 'status': 'mock'})

    try:
        data = request.json
        # 这里放置实际的特征提取和预测逻辑
        # df = pd.DataFrame(data, index=[0])
        # prediction = model.predict(df)
        return jsonify({'prediction': 0.5, 'status': 'success'})
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/chat', methods=['POST'])
def chat():
    """
    【功能 2】: AI 智能对话 (由 DeepSeek 驱动)
    接收用户消息 -> 注入系统人设 -> 调用 DeepSeek -> 返回结果
    """
    data = request.json
    user_msg = data.get('message', '')

    if not user_msg:
        return jsonify({'reply': '请告诉我您想了解什么？'})

    # 构造请求 DeepSeek 的 payload
    # System Prompt 定义了 AI 的身份和业务规则
    payload = {
        "model": "deepseek-chat",
        "messages": [
            {
                "role": "system",
                "content": "你是一个名为'瑞幸咖啡'的智能点餐助手。你的职责是帮助用户选择合适的产品、解答门店营业时间问题、推荐饮品搭配。营业时间是每天08:00-22:00。请用亲切、简练的中文回答。如果用户询问无法回答的问题，请引导他们去查看菜单。"
            },
            {
                "role": "user",
                "content": user_msg
            }
        ],
        "stream": False,
        "temperature": 0.7
    }

    headers = {
        "Authorization": f"Bearer {DEEPSEEK_API_KEY}",
        "Content-Type": "application/json"
    }

    try:
        # 调用 DeepSeek 接口
        response = requests.post(DEEPSEEK_URL, json=payload, headers=headers, timeout=30)

        if response.status_code == 200:
            result = response.json()
            # 提取 AI 的回复内容
            ai_reply = result['choices'][0]['message']['content']
        else:
            print(f"DeepSeek API Error: {response.text}")
            ai_reply = "抱歉，我的大脑暂时连接不畅，请稍后再试。"

    except Exception as e:
        print(f"Request Exception: {e}")
        ai_reply = "抱歉，由于网络原因我无法回答您的问题。"

    return jsonify({
        'reply': ai_reply,
        'timestamp': datetime.now().isoformat()
    })

if __name__ == '__main__':
    # 监听 5000 端口
    app.run(host='0.0.0.0', port=5000)