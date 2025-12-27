import joblib
import pandas as pd
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.feature_extraction.text import TfidfTransformer
from sklearn.naive_bayes import MultinomialNB
from sklearn.pipeline import Pipeline

# 1. 准备训练数据 (语料库)
# 这里定义了用户常说的话 (text) 和 对应的意图 (intent)
data = [
    # --- 推荐意图 ---
    ("推荐一下", "recommend"),
    ("有什么好喝的", "recommend"),
    ("介绍一下菜单", "recommend"),
    ("想喝咖啡", "recommend"),
    ("拿铁怎么样", "recommend"),
    ("来杯最火的", "recommend"),
    ("生椰拿铁", "recommend"),
    ("美式", "recommend"),
    ("我不喜欢甜的", "recommend"),

    # --- 营业信息意图 ---
    ("几点开门", "info"),
    ("几点关门", "info"),
    ("营业时间", "info"),
    ("什么时候打烊", "info"),
    ("在哪里", "info"),
    ("地址", "info"),

    # --- 闲聊/问候 ---
    ("你好", "chat"),
    ("在吗", "chat"),
    ("嗨", "chat"),
    ("你是谁", "chat"),
    ("早上好", "chat"),
    ("谢谢", "chat"),
]

# 转换为 DataFrame
df = pd.DataFrame(data, columns=['text', 'intent'])

# 2. 构建训练管道 (Pipeline)
# 分词(CountVectorizer) -> 计算权重(Tfidf) -> 分类器(贝叶斯)
text_clf = Pipeline([
    ('vect', CountVectorizer()),
    ('tfidf', TfidfTransformer()),
    ('clf', MultinomialNB()),
])

# 3. 开始训练
print("正在训练本地 AI 模型...")
text_clf.fit(df['text'], df['intent'])

# 4. 保存模型
model_path = 'chat_model.pkl'
joblib.dump(text_clf, model_path)
print(f"模型已保存至: {model_path}")

# 5. 简单测试
test_phrases = ["推荐个好喝的", "你们几点关门", "你好呀"]
for phrase in test_phrases:
    predicted = text_clf.predict([phrase])[0]
    print(f"测试: '{phrase}' -> 识别意图: {predicted}")