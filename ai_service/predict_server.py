from flask import Flask, request, jsonify
import joblib
import pandas as pd
import random
import os
from datetime import datetime

app = Flask(__name__)

# --- 加载本地模型 ---
# 加载用于客流预测的模型
try:
    crowd_model = joblib.load('model.pkl')
    crowd_model_loaded = True
except:
    print("Warning: model.pkl (客流预测) not found. Running in mock mode.")
    crowd_model_loaded = False

# 加载用于对话的 NLP 模型
try:
    chat_model = joblib.load('chat_model.pkl')
    chat_model_loaded = True
    print("Success: Local AI chat model loaded.")
except:
    print("Warning: chat_model.pkl not found. Please run train_nlp.py first.")
    chat_model_loaded = False

@app.route('/predict', methods=['POST'])
def predict():
    """
    【功能 1】: 客流预测 (保持不变)
    """
    if not crowd_model_loaded:
        return jsonify({'prediction': random.uniform(0, 1), 'status': 'mock'})
    try:
        # data = request.json
        # 实际预测逻辑...
        return jsonify({'prediction': 0.5, 'status': 'success'})
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/chat', methods=['POST'])
def chat():
    """
    【功能 2】: 本地 AI 对话 (不再调用 DeepSeek)
    """
    data = request.json
    user_msg = data.get('message', '')

    if not user_msg:
        return jsonify({'reply': '请告诉我您想了解什么？', 'recommendations': []})

    ai_reply = ""
    recommendations = []

    # 1. 使用本地模型预测意图
    intent = "chat" # 默认意图
    if chat_model_loaded:
        try:
            intent = chat_model.predict([user_msg])[0]
        except Exception as e:
            print(f"Prediction error: {e}")

    # 2. 根据意图生成回复 (规则库)
    if intent == "recommend":
        ai_reply = "根据您的口味，我为您推荐以下几款瑞幸爆款饮品，点击即可查看详情哦！☕️"
        # 模拟推荐商品数据 (ID需要对应数据库里的真实ID)
        recommendations = [
            {"pid": 1, "name": "生椰拿铁", "price": 18.0, "image": "/images/product/coconut_latte.jpg", "reason": "人气Top1，YYDS"},
            {"pid": 2, "name": "加浓美式", "price": 13.0, "image": "/images/product/american.jpg", "reason": "提神醒脑必备"},
            {"pid": 7, "name": "拿铁", "price": 16.0, "image": "/images/product/latte.jpg", "reason": "经典奶咖"}
        ]

    elif intent == "info":
        ai_reply = "我们的营业时间是每天 08:00 - 22:00。门店提供免费 Wi-Fi 和充电插座，欢迎光临！"

    else: # chat 或其他
        ai_reply = "您好！我是瑞幸智能助手。您可以问我“有什么推荐”或者“营业时间”哦。"

    # 返回符合 Java 后端要求的 JSON 格式
    return jsonify({
        'reply': ai_reply,
        'recommendations': recommendations,
        'timestamp': datetime.now().isoformat()
    })

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)