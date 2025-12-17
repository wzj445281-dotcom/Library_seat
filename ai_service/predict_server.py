from flask import Flask, request, jsonify
import random
import datetime

app = Flask(__name__)

# 模拟预测接口
# POST /predict
@app.route('/predict', methods=['POST'])
def predict():
    data = request.json
    seat_ids = data.get('seatIds', [])

    results = []
    for seat_id in seat_ids:
        # 模拟算法：随机生成 60-100 的热度分
        # 这里的逻辑可以替换为真实的 sklearn/pytorch 模型调用
        score = round(random.uniform(60, 99), 1)

        # 简单规则：如果是第一排(ID < 10)，分数高一点
        if int(seat_id) < 10:
            score = round(random.uniform(90, 100), 1)

        results.append({
            'seatId': seat_id,
            'heatScore': score,
            'date': (datetime.date.today() + datetime.timedelta(days=1)).isoformat()
        })

    return jsonify({'code': 200, 'data': results})

if __name__ == '__main__':
    print("AI Prediction Server is running on port 5000...")
    app.run(port=5000)