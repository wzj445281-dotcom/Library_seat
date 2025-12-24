from flask import Flask, request, jsonify 
import time 
import os

app = Flask(__name__) 

# 模拟 AI 诊断逻辑 (这里你可以接入 LangChain / OpenAI / 本地模型) 
def mock_ai_inference(pet_type, age, symptoms): 
    # 实际项目中，这里调用你的 PyTorch/TensorFlow 模型 
    # 这里做简单的规则模拟，保证演示效果 
    advice = "" 
    if "吐" in symptoms or "vomit" in symptoms: 
        advice = f"检测到{pet_type}有呕吐症状。建议禁食禁水4小时观察。如果持续呕吐或伴有精神萎靡，请立即前往最近的宠物医院。" 
    elif "痒" in symptoms or "scratch" in symptoms: 
        advice = f"可能是皮肤病或寄生虫感染。请检查{pet_type}身上是否有红点或掉毛。建议预约'皮肤刮片检查'服务。" 
    elif "拉" in symptoms or "diarrhea" in symptoms: 
        advice = f"{pet_type}出现腹泻症状，可能是肠胃不适。建议喂食益生菌，观察24小时。如症状加重请及时就医。" 
    elif "咳嗽" in symptoms or "cough" in symptoms: 
        advice = f"{pet_type}有咳嗽症状，可能是呼吸道感染。建议保持环境温暖，避免剧烈运动。如持续咳嗽请就医检查。" 
    elif "不吃饭" in symptoms or "不吃" in symptoms: 
        advice = f"{pet_type}食欲不振，可能是口腔问题或消化不良。建议检查口腔，提供易消化的食物。如超过48小时不进食请就医。" 
    else: 
        advice = f"已收到关于{age}岁{pet_type}的情况。建议保持观察，多喝水。如症状加重请及时就医。" 
    
    return advice 

@app.route('/api/consult', methods=['POST']) 
def consult(): 
    try: 
        data = request.json 
        print(f"收到 Java 端请求: {data}") 
        
        # 提取参数 
        pet_type = data.get('petType', '宠物') 
        age = data.get('petAge', '未知') 
        symptoms = data.get('symptoms', '') 

        # 模拟 AI 思考时间 (让用户感觉在分析) 
        time.sleep(1.0) 
        
        # 获取诊断结果 
        result = mock_ai_inference(pet_type, age, symptoms) 
        
        return jsonify({ 
            "code": 200, 
            "success": True, 
            "data": { 
                "diagnosis": "初步症状分析完成", 
                "advice": result, 
                "recommendedService": "全科体检" # 可以关联你的 sys_service 表 
            } 
        }) 

    except Exception as e: 
        return jsonify({"code": 500, "success": False, "msg": str(e)}) 

@app.route('/health', methods=['GET'])
def health():
    return jsonify({"status": "ok", "message": "Pet AI Service is running"})

if __name__ == '__main__': 
    # 监听 5000 端口 
    app.run(host='0.0.0.0', port=5000, debug=True)