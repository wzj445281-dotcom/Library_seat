from flask import Flask, request, jsonify
import random
import datetime

# 1. 必须先初始化 app，后续的 @app.route 才能用
app = Flask(__name__)

# ----------------------------------------------------
# 接口 1: 热度预测
# ----------------------------------------------------
@app.route('/predict', methods=['POST'])
def predict():
    try:
        data = request.json
        seat_ids = data.get('seatIds', [])

        # 1. 获取时间特征
        tomorrow = datetime.date.today() + datetime.timedelta(days=1)
        weekday = tomorrow.weekday() # 0=周一, 6=周日
        is_weekend = weekday >= 5

        results = []
        for seat_id in seat_ids:
            sid = int(seat_id)

            # --- 模拟算法逻辑 ---
            score = random.uniform(60, 75)

            # 特征1：位置偏好 (假设 ID < 15 的是靠窗/VIP座位)
            if sid < 15:
                score += random.uniform(15, 20)
            elif sid % 2 == 0:
                score -= random.uniform(2, 5)

            # 特征2：周末效应
            if is_weekend:
                score += random.uniform(5, 10)

            final_score = round(max(0, min(99, score)), 1)

            results.append({
                'seatId': seat_id,
                'heatScore': final_score,
                'predictionDate': tomorrow.isoformat()
            })

        return jsonify({'code': 200, 'message': 'success', 'data': results})

    except Exception as e:
        print(f"Error: {e}")
        return jsonify({'code': 500, 'message': str(e)})

# ----------------------------------------------------
# 接口 2: 风控预测 (之前报错就是因为这个放错了位置)
# ----------------------------------------------------
@app.route('/predict/risk', methods=['POST'])
def predict_risk():
    data = request.json
    credit_score = data.get('creditScore', 100)

    # --- 模拟逻辑回归模型 ---
    risk_prob = 0.0

    if credit_score < 80:
        risk_prob += 0.4
    if credit_score < 60:
        risk_prob += 0.3

    risk_prob = min(0.99, max(0.01, risk_prob))

    result = {
        'riskProbability': round(risk_prob, 2),
        'action': 'ALLOW'
    }

    if risk_prob > 0.7:
        result['action'] = 'WARN'

    return jsonify({'code': 200, 'data': result})

if __name__ == '__main__':
    print("AI Prediction Server is running...")
    # 必须监听 0.0.0.0 才能在 Docker 外部访问
    app.run(host='0.0.0.0', port=5000)