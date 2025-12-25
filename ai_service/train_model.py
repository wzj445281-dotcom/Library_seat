import pandas as pd 
import numpy as np 
from sklearn.linear_model import LinearRegression 
from sklearn.ensemble import RandomForestRegressor 
import joblib 
import random 
 
# ========================================== 
# 1. 生成模拟训练数据 (宠物店工位繁忙度) 
# ========================================== 
# 特征: 
#   hour: 小时 (0-23) 
#   is_weekend: 是否周末 (0/1) 
#   weather_code: 天气 (0=晴, 1=雨, 2=雪) - 假设雨雪天人少 
#   pet_type: 0=猫, 1=狗 (狗通常需要更多时间) 
# 目标: 
#   busy_score: 繁忙指数 (0.0 - 1.0) 
 
def generate_data(num_samples=1000): 
    data = [] 
    for _ in range(num_samples): 
        hour = random.randint(9, 21) # 营业时间 
        is_weekend = random.choice([0, 1]) 
        weather_code = random.choice([0, 1, 2]) 
        
        # 基础热度：中午和傍晚人多 
        base_score = 0.3 
        if 11 <= hour <= 14: base_score += 0.4 
        if 18 <= hour <= 20: base_score += 0.3 
        
        # 周末热度加成 
        if is_weekend: base_score += 0.2 
        
        # 天气影响 
        if weather_code > 0: base_score -= 0.3 
        
        # 加上随机波动 
        noise = random.uniform(-0.1, 0.1) 
        final_score = base_score + noise 
        
        # 截断到 0-1 
        final_score = max(0.0, min(1.0, final_score)) 
        
        data.append([hour, is_weekend, weather_code, final_score]) 
        
    return pd.DataFrame(data, columns=['hour', 'is_weekend', 'weather_code', 'busy_score']) 
 
# ========================================== 
# 2. 训练模型 
# ========================================== 
print("Generating training data...") 
df = generate_data(2000) 
 
X = df[['hour', 'is_weekend', 'weather_code']] 
y = df['busy_score'] 
 
print("Training Random Forest Regressor...") 
model = RandomForestRegressor(n_estimators=100, random_state=42) 
model.fit(X, y) 
 
# ========================================== 
# 3. 保存模型 
# ========================================== 
print("Saving model to 'busy_model.pkl'...") 
joblib.dump(model, 'busy_model.pkl') 
print("Done!")