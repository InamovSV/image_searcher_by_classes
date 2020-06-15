FROM python:3.6

RUN pip3 install https://download.pytorch.org/whl/cpu/torch-1.0.1.post2-cp36-cp36m-linux_x86_64.whl && \
 pip3 install aiokafka && \ 
pip3 install torchvision

RUN mkdir /app

COPY . /app

WORKDIR /app

CMD \
sleep 15 && \
python -u nn_app_scala.py