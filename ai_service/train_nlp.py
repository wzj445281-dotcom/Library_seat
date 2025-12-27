# train_nlp.py
import joblib
import jieba
import pandas as pd
from sklearn.feature_extraction.text import CountVectorizer, TfidfTransformer
from sklearn.naive_bayes import MultinomialNB
from sklearn.pipeline import Pipeline

# 1. 定义中文分词函数
def jieba_tokenizer(text):
    return jieba.lcut(text)

# 2. 准备语料 (建议扩充更多样本)
data = [
    # recommend 意图
    ("推荐一下", "recommend"), ("有什么好喝的", "recommend"), ("我想喝咖啡", "recommend"),
    ("最近有什么新品", "recommend"), ("拿铁怎么样", "recommend"), ("来杯招牌", "recommend"),
    ("推荐咖啡", "recommend"), ("推荐一款咖啡", "recommend"), ("推荐一杯咖啡", "recommend"),
    ("有什么咖啡推荐", "recommend"), ("什么咖啡好喝", "recommend"), ("给我推荐咖啡", "recommend"),
    ("咖啡推荐", "recommend"), ("推荐拿铁", "recommend"), ("推荐美式", "recommend"),
    ("推荐卡布奇诺", "recommend"), ("推荐摩卡", "recommend"), ("推荐拿铁咖啡", "recommend"),
    ("推荐美式咖啡", "recommend"), ("推荐卡布奇诺咖啡", "recommend"), ("推荐摩卡咖啡", "recommend"),
    ("推荐饮品", "recommend"), ("推荐一杯饮品", "recommend"), ("有什么饮品推荐", "recommend"),
    ("什么饮品好喝", "recommend"), ("给我推荐饮品", "recommend"), ("饮品推荐", "recommend"),

    # info 意图
    ("几点开门", "info"), ("营业时间", "info"), ("你们在哪里", "info"),
    ("什么时候打烊", "info"), ("店铺位置", "info"), ("有wifi吗", "info"),
    ("地址", "info"), ("位置", "info"), ("怎么去", "info"), ("交通", "info"),
    ("停车", "info"), ("营业", "info"), ("开门", "info"), ("关门", "info"),
    ("打烊", "info"), ("wifi", "info"), ("网络", "info"), ("电源", "info"),
    ("插座", "info"), ("环境", "info"), ("设施", "info"), ("服务", "info"),
    ("你们的地址", "info"), ("店铺在哪儿", "info"), ("怎么到你们店", "info"),
    ("有停车位吗", "info"), ("几点关门", "info"), ("什么时候营业", "info"),
    ("你们的营业时间", "info"), ("店铺地址", "info"), ("店铺位置在哪里", "info"),
    ("有无线网吗", "info"), ("有充电的地方吗", "info"), ("环境怎么样", "info"),
    ("有什么设施", "info"), ("服务怎么样", "info"),

    # chat 意图 (尽量覆盖非业务的闲聊)
    ("你好", "chat"), ("你是谁", "chat"), ("今天天气不错", "chat"),
    ("讲个笑话", "chat"), ("很高兴认识你", "chat"), ("笨蛋", "chat"),
    ("谢谢", "chat"), ("再见", "chat"), ("哈哈", "chat"), ("呵呵", "chat"),
    ("嗯嗯", "chat"), ("好的", "chat"), ("可以", "chat"), ("不行", "chat"),
    ("是的", "chat"), ("不是", "chat"), ("对", "chat"), ("错", "chat"),
    ("喜欢", "chat"), ("讨厌", "chat"), ("爱", "chat"), ("恨", "chat"),
    ("开心", "chat"), ("难过", "chat"), ("高兴", "chat"), ("伤心", "chat"),
    ("生气", "chat"), ("愤怒", "chat"), ("失望", "chat"), ("满意", "chat"),
    ("你好啊", "chat"), ("你是谁啊", "chat"), ("今天天气真好", "chat"),
    ("讲个笑话吧", "chat"), ("很高兴认识你啊", "chat"), ("你是个笨蛋", "chat"),
    ("谢谢你", "chat"), ("再见啦", "chat"), ("哈哈哈", "chat"), ("呵呵呵", "chat"),
    ("嗯嗯嗯", "chat"), ("好的呀", "chat"), ("可以的", "chat"), ("不行的", "chat"),
    ("是的呀", "chat"), ("不是的", "chat"), ("对的呀", "chat"), ("错的呀", "chat"),
    ("我喜欢", "chat"), ("我讨厌", "chat"), ("我爱你", "chat"), ("我恨你", "chat"),
    ("我很开心", "chat"), ("我很难过", "chat"), ("我很高兴", "chat"), ("我很伤心", "chat"),
    ("我很生气", "chat"), ("我很愤怒", "chat"), ("我很失望", "chat"), ("我很满意", "chat")
]
df = pd.DataFrame(data, columns=['text', 'intent'])

# 3. 构建管道，关键是传入 tokenizer=jieba_tokenizer
# 注意：token_pattern=None 是为了避免 sklearn 默认正则过滤掉单字
text_clf = Pipeline([
    ('vect', CountVectorizer(tokenizer=jieba_tokenizer, token_pattern=None)),
    ('tfidf', TfidfTransformer()),
    ('clf', MultinomialNB()),
])

# 4. 训练并保存
text_clf.fit(df['text'], df['intent'])
joblib.dump(text_clf, 'chat_model.pkl')

# 打印各类别的样本数量
print("各类别样本数量:")
print(df['intent'].value_counts())

# 测试模型
test_samples = [
    "推荐咖啡",
    "你们几点开门",
    "你好"
]

print("\n测试模型:")
for text in test_samples:
    predicted = text_clf.predict([text])[0]
    proba = text_clf.predict_proba([text])[0]
    classes = text_clf.classes_
    confidence = max(proba)
    print(f"'{text}' -> 预测意图: {predicted} (置信度: {confidence:.2f})")

print("模型已使用 Jieba 分词优化并保存！")