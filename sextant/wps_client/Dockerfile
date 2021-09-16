FROM python:3.7

RUN mkdir /app/
WORKDIR /app


COPY requirements.txt /tmp/
RUN pip install --requirement /tmp/requirements.txt

COPY . ./
