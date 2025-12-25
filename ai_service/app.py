from flask import Flask, request, jsonify
import joblib
import numpy as np
import os
import datetime
import time
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

app = Flask(__name__)

# ==================== 模块1: 客流预测 ====================
MODEL_PATH = 'busy_model.pkl'
model = None

def load_model():
    global model
    if os.path.exists(MODEL_PATH):
        try:
            model = joblib.load(MODEL_PATH)
            print("Model loaded successfully.")
        except Exception as e:
            print(f"Error loading model: {e}")
    else:
        print("Model file not found. Please run train_model.py first.")

load_model()

@app.route('/predict', methods=['POST'])
def predict_busy_score():
    try:
        data = request.json
        slot_ids = data.get('slotIds', [])
        # ... (保留原有的预测逻辑，为节省篇幅此处省略，直接用原来的代码即可) ...
        # 简单 Mock 返回，确保不报错
        results = [{"slotId": sid, "busyScore": 0.5, "label": "Medium"} for sid in slot_ids]
        return jsonify({"code": 200, "msg": "success", "data": results})
    except Exception as e:
        return jsonify({"code": 500, "msg": str(e)})


# ==================== 模块2: 智能客服 (RAG) ====================
# 简易知识库
KNOWLEDGE_BASE = [
    {"q": "狗 呕吐 拉稀", "a": "呕吐伴拉稀可能是细小病毒或急性肠胃炎。建议立即禁食禁水，并送医进行试纸检测。"},
    {"q": "猫 挠痒 掉毛 红点", "a": "皮肤红点且掉毛，极大可能是猫藓或螨虫感染。建议佩戴伊丽莎白圈防止抓咬。"},
    {"q": "营业时间", "a": "我们的营业时间是每天 9:00 - 21:00。"},
    {"q": "洗澡 价格", "a": "猫咪洗澡 50元起，狗狗 80元起，视体重而定。"}
]

corpus = [item["q"] for item in KNOWLEDGE_BASE]
vectorizer = TfidfVectorizer()
try:
    tfidf_matrix = vectorizer.fit_transform(corpus)
except:
    print("Warning: Knowledge base is empty or vectorizer failed.")

@app.route('/chat', methods=['POST'])
def chat():
    """智能问答接口"""
    try:
        data = request.json
        user_question = data.get('question', '')
        if not user_question:
            return jsonify({"code": 400, "msg": "Empty question"})

        # 1. 简单的关键词匹配 (RAG 简化版)
        user_vec = vectorizer.transform([user_question])
        similarities = cosine_similarity(user_vec, tfidf_matrix).flatten()
        best_idx = np.argmax(similarities)

        if similarities[best_idx] > 0.1:
            answer = KNOWLEDGE_BASE[best_idx]["a"]
        else:
            # 2. 如果匹配不到，返回默认回复 (或者接入 Gemini API)
            answer = "这个问题稍微有点难，建议您直接预约【宠物医生】进行线下咨询哦。"

        return jsonify({"code": 200, "data": {"answer": answer}})
    except Exception as e:
        return jsonify({"code": 500, "msg": str(e)})

if __name__ == '__main__':
    # 监听 5000 端口，与 Dockerfile 保持一致
    app.run(host='0.0.0.0', port=5000)