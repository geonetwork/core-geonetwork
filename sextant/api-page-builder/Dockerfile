FROM python:3.8

RUN mkdir /app/
WORKDIR /app

COPY ./requirements.txt ./requirements.txt
RUN pip install -r ./requirements.txt

COPY . ./
