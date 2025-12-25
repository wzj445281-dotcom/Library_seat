FROM python:3.9-slim

WORKDIR /app

# 复制依赖文件 (如果有 requirements.txt)
# COPY requirements.txt .
# RUN pip install --no-cache-dir -r requirements.txt

# 如果没有 requirements.txt，直接安装常用 AI 库 (根据你的 predict_server.py 实际需求调整)
# 假设用到了 flask/fastapi, numpy, torch 等
RUN pip install --no-cache-dir flask requests numpy

# 复制源码
COPY . .

# 暴露端口 (假设你的 python 服务运行在 5000 端口)
EXPOSE 5000

# 启动命令 (请根据实际文件名调整)
CMD ["python", "predict_server.py"]