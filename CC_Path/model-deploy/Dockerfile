FROM python:3.10

WORKDIR /app

RUN pip install --upgrade pip

COPY . /app

RUN pip install -r requirements.txt

COPY . .

EXPOSE 8080

ENV PYTHONUNBUFFERED=1

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8080"]