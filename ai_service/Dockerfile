# 使用轻量级 Python 基础镜像
FROM python:3.9-slim

# 设置工作目录
WORKDIR /app

# 设置时区为上海
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' > /etc/timezone

# 复制依赖文件
COPY requirements.txt .

# 安装依赖 (使用清华源加速)
RUN pip install --no-cache-dir -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple

# 复制当前目录下的所有文件到容器中
COPY . .

# 暴露 5000 端口
EXPOSE 5000

# 启动命令 (使用 gunicorn 生产环境启动，或者直接 python 启动)
# 这里为了简单直接用 python，生产环境建议用 gunicorn
CMD ["python", "predict_server.py"]