FROM python:3.8

RUN mkdir /app/
COPY api_validator /app/
RUN pip install -r /app/requirements.txt
COPY . /app/
RUN sh -c "cd /app/api_validator && pip install -e ."
