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

    # info 意图
    ("几点开门", "info"), ("营业时间", "info"), ("你们在哪里", "info"),
    ("什么时候打烊", "info"), ("店铺位置", "info"), ("有wifi吗", "info"),

    # chat 意图 (尽量覆盖非业务的闲聊)
    ("你好", "chat"), ("你是谁", "chat"), ("今天天气不错", "chat"),
    ("讲个笑话", "chat"), ("很高兴认识你", "chat"), ("笨蛋", "chat")
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
print("模型已使用 Jieba 分词优化并保存！")