# 使用轻量级 Python 镜像
FROM python:3.9-slim

WORKDIR /app

# 安装依赖
RUN pip install flask

# 复制脚本
COPY predict_server.py .

# 暴露端口
EXPOSE 5000

# 启动命令
CMD ["python", "predict_server.py"]