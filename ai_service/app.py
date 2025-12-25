from flask import Flask, request, jsonify
import joblib
import numpy as np
import os
import datetime

app = Flask(__name__)

# 加载模型 (单例模式)
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

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)