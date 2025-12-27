import requests
import json

# Test the AI assistant with different intents
test_cases = [
    "推荐咖啡",
    "你们几点开门",
    "你好",
    "推荐拿铁",
    "店铺地址",
    "今天天气不错"
]

url = "http://localhost:8080/api/app/ai/chat"

for message in test_cases:
    payload = {"message": message}
    headers = {"Content-Type": "application/json; charset=utf-8"}
    
    print(f"\n测试消息: {message}")
    try:
        response = requests.post(url, json=payload, headers=headers)
        result = response.json()
        
        if result.get("code") == 200:
            data = result.get("data", {})
            reply = data.get("reply", "")
            recommendations = data.get("recommendations", [])
            intent = data.get("intent", "")
            
            print(f"识别意图: {intent}")
            print(f"AI回复: {reply}")
            if recommendations:
                print(f"推荐商品: {recommendations}")
        else:
            print(f"请求失败: {result.get('msg', '未知错误')}")
            print(f"完整响应: {result}")
    except Exception as e:
        print(f"请求异常: {str(e)}")
        print(f"响应状态码: {response.status_code if 'response' in locals() else 'N/A'}")
        if 'response' in locals():
            print(f"响应内容: {response.text}")