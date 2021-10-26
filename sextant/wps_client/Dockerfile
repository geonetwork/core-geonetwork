FROM python:3.7

RUN mkdir /app/
WORKDIR /app

COPY requirements.txt /tmp/
RUN python -m pip install --upgrade pip
RUN pip install --requirement /tmp/requirements.txt

COPY . ./
