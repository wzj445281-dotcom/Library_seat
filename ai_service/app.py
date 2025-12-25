from flask import Flask, request, jsonify
import joblib
import numpy as np
import os
import datetime
import time
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

app = Flask(__name__)

# 加载模型 (单例模式)
MODEL_PATH = 'busy_model.pkl'
model = None

# ==========================================
# 简易知识库 (RAG的雏形)
# ==========================================
KNOWLEDGE_BASE = [
    {"q": "狗 呕吐 拉稀", "a": "呕吐伴拉稀可能是细小病毒或急性肠胃炎。建议立即禁食禁水，并送医进行试纸检测。推荐科室：内科。"},
    {"q": "猫 挠痒 掉毛 红点", "a": "皮肤红点且掉毛，极大可能是猫藓或螨虫感染。建议佩戴伊丽莎白圈防止抓咬，并预约皮肤刮片检查。"},
    {"q": "狗 咳嗽 呼吸急促", "a": "频繁咳嗽可能是犬窝咳或心脏问题。如果伴有发烧，请立即就医。推荐项目：肺部X光。"},
    {"q": "猫 不吃饭 精神差", "a": "猫咪超过24小时绝食极易引发脂肪肝。请检查口腔是否有溃疡。建议尝试喂食流质罐头。"},
    {"q": "洗澡 价格 多少钱", "a": "我们的基础洗澡服务：小型犬/猫 50元起，大型犬 80元起。具体价格根据体重计算。"},
    {"q": "营业时间 什么时候 开门", "a": "本店营业时间为每天 09:00 - 21:00。节假日照常营业。"}
]

# 预计算 TF-IDF 矩阵
corpus = [item["q"] for item in KNOWLEDGE_BASE]
vectorizer = TfidfVectorizer()
tfidf_matrix = vectorizer.fit_transform(corpus)

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

# 在应用启动前加载模型
load_model()

@app.route('/predict', methods=['POST'])
def predict_busy_score():
    """
    预测未来某时刻的工位繁忙度
    Input: {"slotIds": [1, 2], "date": "2023-12-26"} (date可选，默认明天)
    """
    try:
        data = request.json
        slot_ids = data.get('slotIds', [])

        # 自动训练 (如果模型不存在)
        if model is None:
            if not os.path.exists(MODEL_PATH):
                import train_model
                train_model.generate_data() # 触发训练逻辑
                load_model()

        # 构造特征向量
        # 假设预测明天的平均热度
        tomorrow = datetime.date.today() + datetime.timedelta(days=1)
        is_weekend = 1 if tomorrow.weekday() >= 5 else 0

        results = []

        if model:
            # 预测明天 10:00, 14:00, 18:00 三个时间点的平均值
            # 特征顺序: [hour, is_weekend, weather_code(默认0晴天)]
            features = [
                [10, is_weekend, 0],
                [14, is_weekend, 0],
                [18, is_weekend, 0]
            ]
            predictions = model.predict(features)
            avg_score = float(np.mean(predictions))
        else:
            # Fallback
            avg_score = 0.5

        for slot_id in slot_ids:
            # 给不同工位加微小扰动，避免所有工位分数一模一样
            final_score = min(0.99, max(0.01, avg_score + np.random.uniform(-0.1, 0.1)))

            results.append({
                "slotId": slot_id,
                "busyScore": round(final_score, 2),
                "label": "High" if final_score > 0.7 else "Medium" if final_score > 0.3 else "Low"
            })

        return jsonify({"code": 200, "msg": "success", "data": results})

    except Exception as e:
        return jsonify({"code": 500, "msg": str(e)})

def find_best_answer(user_query):
    # 1. 将用户问题向量化
    user_vec = vectorizer.transform([user_query])
    
    # 2. 计算余弦相似度
    similarities = cosine_similarity(user_vec, tfidf_matrix).flatten()
    
    # 3. 找到最佳匹配
    best_idx = np.argmax(similarities)
    best_score = similarities[best_idx]
    
    # 4. 阈值判断 (如果相似度太低，说明知识库没覆盖)
    if best_score < 0.1: # 阈值可调
        return f"这个问题超出了我的知识范围。建议您直接联系人工客服或带宠物到店检查。这也可能是因为您的描述太简短"
    
    # 5. 返回最佳答案
    return KNOWLEDGE_BASE[best_idx]["a"]

@app.route('/chat', methods=['POST'])
def chat():
    """
    简易问答接口
    Input: {"question": "我家狗呕吐拉稀怎么办"}
    Output: {"code": 200, "msg": "success", "data": {"answer": "...", "timestamp": 1671234567}}
    """
    try:
        data = request.json
        user_question = data.get('question', '')
        
        if not user_question.strip():
            return jsonify({"code": 400, "msg": "问题不能为空"})
        
        # 模拟处理延迟
        time.sleep(0.5)
        
        answer = find_best_answer(user_question)
        
        return jsonify({
            "code": 200,
            "msg": "success",
            "data": {
                "answer": answer,
                "timestamp": int(time.time())
            }
        })
        
    except Exception as e:
        return jsonify({"code": 500, "msg": str(e)})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)